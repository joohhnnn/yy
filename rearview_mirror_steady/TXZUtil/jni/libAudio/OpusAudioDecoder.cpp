#include "OpusAudioDecoder.h"

#ifdef HAVE_LRINTF
# define float2int(x) lrintf(x)
#else
# define float2int(flt) ((int)(floor(.5+flt)))
#endif
#define MINI(_a,_b)      ((_a)<(_b)?(_a):(_b))
#define MAXI(_a,_b)      ((_a)>(_b)?(_a):(_b))
#define CLAMPI(_a,_b,_c) (MAXI(_a,MINI(_b,_c)))
#if !defined(__LITTLE_ENDIAN__) && ( defined(WORDS_BIGENDIAN) || defined(__BIG_ENDIAN__) )
#define le_short(s) ((short) ((unsigned short) (s) << 8) | ((unsigned short) (s) >> 8))
#define be_short(s) ((short) (s))
#else
#define le_short(s) ((short) (s))
#define be_short(s) ((short) ((unsigned short) (s) << 8) | ((unsigned short) (s) >> 8))
#endif

OpusAudioDecoder::OpusAudioDecoder(
        JNIEnv* _env) :
    AudioDecoder(_env)
{
    frame_size = 0;
    st = NULL;
    packet_count = 0;
    total_links = 0;
    stream_init = 0;
    page_granule = 0;
    link_out = 0;
    eos = 0;
    audio_size = 0;
    manual_gain = 0;
    channels = -1;
    rate = 11025; //采样率11025
    wav_format = 0;
    preskip = 0;
    gran_offset = 0;
    has_opus_stream = 0;
    has_tags_packet = 0;
    dither = 1;
    fp = 0;
    resampler = NULL;
    gain = 1;
    streams = 0;

    output = 0;
    shapemem.a_buf = 0;
    shapemem.b_buf = 0;
    shapemem.mute = 960;
    shapemem.fs = 0;

    wav_format = 1;

    mDecodeOffset = 0;
    mHeadSize = 0;
    mPcmSize = 0;
    mCompleteDurnation = false;
    mDecodeSizeBeforeFlush = 0;
    mTotalFileSize = 0;

    ogg_sync_init(&(oy));
}

OpusAudioDecoder::~OpusAudioDecoder()
{
    pthread_mutex_destroy(&lockBuffer);
}

static unsigned int rngseed = 22222;
static inline unsigned int fast_rand(
        void)
{
    rngseed = (rngseed * 96314165) + 907633515;
    return rngseed;
}

void OpusAudioDecoder::shape_dither_toshort(
        shapestate *_ss,
        short *_o,
        float *_i,
        int _n,
        int _CC)
{
    const float gains[3] = { 32768.f - 15.f, 32768.f - 15.f, 32768.f - 3.f };
    const float fcoef[3][8] = { { 2.2374f, -.7339f, -.1251f, -.6033f, 0.9030f, .0116f, -.5853f, -.2571f }, /* 48.0kHz noise shaping filter sd=2.34*/
    { 2.2061f, -.4706f, -.2534f, -.6214f, 1.0587f, .0676f, -.6054f, -.2738f }, /* 44.1kHz noise shaping filter sd=2.51*/
    { 1.0000f, 0.0000f, 0.0000f, 0.0000f, 0.0000f, 0.0000f, 0.0000f, 0.0000f }, /* lowpass noise shaping filter sd=0.65*/
    };
    int i;
    int rate = _ss->fs == 44100 ? 1 : (_ss->fs == 48000 ? 0 : 2);
    float gain = gains[rate];
    float *b_buf;
    float *a_buf;
    int mute = _ss->mute;
    b_buf = _ss->b_buf;
    a_buf = _ss->a_buf;
    /*In order to avoid replacing digital silence with quiet dither noise
     we mute if the output has been silent for a while*/
    if (mute > 64) memset(a_buf, 0, sizeof(float) * _CC * 4);
    for (i = 0; i < _n; i++)
    {
        int c;
        int pos = i * _CC;
        int silent = 1;
        for (c = 0; c < _CC; c++)
        {
            int j, si;
            float r, s, err = 0;
            silent &= _i[pos + c] == 0;
            s = _i[pos + c] * gain;
            for (j = 0; j < 4; j++)
                err += fcoef[rate][j] * b_buf[c * 4 + j] - fcoef[rate][j + 4] * a_buf[c * 4 + j];
            memmove(&a_buf[c * 4 + 1], &a_buf[c * 4], sizeof(float) * 3);
            memmove(&b_buf[c * 4 + 1], &b_buf[c * 4], sizeof(float) * 3);
            a_buf[c * 4] = err;
            s = s - err;
            r = (float) fast_rand() * (1 / (float) UINT_MAX) - (float) fast_rand() * (1 / (float) UINT_MAX);
            if (mute > 16) r = 0;
            /*Clamp in float out of paranoia that the input will be >96 dBFS and wrap if the
             integer is clamped.*/
            _o[pos + c] = si = float2int(fmaxf(-32768, fminf(s + r, 32767)));
            /*Including clipping in the noise shaping is generally disastrous:
             the futile effort to restore the clipped energy results in more clipping.
             However, small amounts-- at the level which could normally be created by
             dither and rounding-- are harmless and can even reduce clipping somewhat
             due to the clipping sometimes reducing the dither+rounding error.*/
            b_buf[c * 4] = (mute > 16) ? 0 : fmaxf(-1.5f, fminf(si - s, 1.5f));
        }
        mute++;
        if (!silent) mute = 0;
    }
    _ss->mute = MINI(mute, 960);
}

opus_int64 OpusAudioDecoder::audio_write(
        JNIEnv* _env,
        jobject decoder,
        float *pcm,
        int channels,
        int frame_size,
        FILE *fout,
        SpeexResamplerState *resampler,
        int *skip,
        shapestate *shapemem,
        int file,
        opus_int64 maxout,
        int fp)
{
    opus_int64 sampout = 0;
    int i, ret, tmp_skip;
    unsigned out_len;
    short *out;
    float *buf;
    float *output;
    out = (short *) alloca(sizeof(short)*MAX_FRAME_SIZE*channels);
    buf = (float *) alloca(sizeof(float)*MAX_FRAME_SIZE*channels);
    maxout = maxout < 0 ? 0 : maxout;
    do
    {
        if (skip)
        {
            tmp_skip = (*skip > frame_size) ? (int) frame_size : *skip;
            *skip -= tmp_skip;
        }
        else
        {
            tmp_skip = 0;
        }
        if (resampler)
        {
            unsigned in_len;
            output = buf;
            in_len = frame_size - tmp_skip;
            out_len = 1024 < maxout ? 1024 : maxout;
            speex_resampler_process_interleaved_float(resampler, pcm + channels * tmp_skip, &in_len, buf, &out_len);
            pcm += channels * (in_len + tmp_skip);
            frame_size -= in_len + tmp_skip;
        }
        else
        {
            output = pcm + channels * tmp_skip;
            out_len = frame_size - tmp_skip;
            frame_size = 0;
        }

        if (!file || !fp)
        {
            /*Convert to short and save to output file*/
            if (shapemem)
            {
                shape_dither_toshort(shapemem, out, output, out_len, channels);
            }
            else
            {
                for (i = 0; i < (int) out_len * channels; i++)
                    out[i] = (short) float2int(fmaxf(-32768, fminf(output[i] * 32768.f, 32767)));
            }
            if ((le_short(1) != 1) && file)
            {
                for (i = 0; i < (int) out_len * channels; i++)
                    out[i] = le_short(out[i]);
            }
        }

        if (maxout > 0)
        {
            {
                //ret = fwrite(fp ? (char *) output : (char *) out, (fp ? 4 : 2) * channels, out_len < maxout ? out_len : maxout, fout);
                ret = out_len < maxout ? out_len : maxout;
                jsize newLen = (fp ? 4 : 2) * channels * ret;
                mPcmSize += newLen;
                appendOutput(_env, decoder, fp ? (char *) output : (char *) out, newLen);
            }
            sampout += ret;
            maxout -= ret;
        }
    } while (frame_size > 0 && maxout > 0);
    return sampout;
}

OpusMSDecoder *OpusAudioDecoder::process_header(
        ogg_packet *op,
        opus_int32 *rate,
        int *mapping_family,
        int *channels,
        int *preskip,
        float *gain,
        float manual_gain,
        int *streams,
        int wav_format)
{
    int err;
    OpusMSDecoder * st;
    OpusHeader header;

    if (opus_header_parse(op->packet, op->bytes, &header) == 0)
    {
        // fprintf(stderr, "Cannot parse header\n");
        return NULL;
    }

    *mapping_family = header.channel_mapping;
    *channels = header.channels;
    //if (wav_format) adjust_wav_mapping(*mapping_family, *channels, header.stream_map);

    if (!*rate) *rate = header.input_sample_rate;
    /*If the rate is unspecified we decode to 48000*/
    if (*rate == 0) *rate = 48000;
    if (*rate < 8000 || *rate > 192000)
    {
        //fprintf(stderr,"Warning: Crazy input_rate %d, decoding to 48000 instead.\n",*rate);
        *rate = 48000;
    }

    *preskip = header.preskip;
    st = opus_multistream_decoder_create(48000, header.channels, header.nb_streams, header.nb_coupled, header.stream_map, &err);
    if (err != OPUS_OK)
    {
        //fprintf(stderr, "Cannot create decoder: %s\n", opus_strerror(err));
        return NULL;
    }
    if (!st)
    {
        //fprintf (stderr, "Decoder initialization failed: %s\n", opus_strerror(err));
        return NULL;
    }

    *streams = header.nb_streams;

    if (header.gain != 0 || manual_gain != 0)
    {
        /*Gain API added in a newer libopus version, if we don't have it
         we apply the gain ourselves. We also add in a user provided
         manual gain at the same time.*/
        int gainadj = (int) (manual_gain * 256.) + header.gain;
#ifdef OPUS_SET_GAIN
        err = opus_multistream_decoder_ctl(st, OPUS_SET_GAIN(gainadj));
        if (err == OPUS_UNIMPLEMENTED)
        {
#endif
            *gain = pow(10., gainadj / 5120.);
#ifdef OPUS_SET_GAIN
        }
        else if (err != OPUS_OK)
        {
            //fprintf (stderr, "Error setting gain: %s\n", opus_strerror(err));
            return NULL;
        }
#endif
    }

    return st;
}

int OpusAudioDecoder::write(
        const void *data,
        int len)
{
    if (oy.returned > 0)
    {
        mDecodeOffset += oy.returned;
    }

    char *buf = ogg_sync_buffer(&oy, len);
    memcpy(buf, data, len);
    ogg_sync_wrote(&oy, len);

    return 0;
}

int OpusAudioDecoder::process(
        JNIEnv* _env,
        jclass clsDecoder,
        jobject& decoder)
{
    {
        /*Loop for all complete pages we got (most likely only one)*/
        while (ogg_sync_pageout(&oy, &og) == 1)
        {
            int page_offset = ((long) og.body) - ((long) oy.data);

            if (stream_init == 0)
            {
                ogg_stream_init(&os, ogg_page_serialno(&og));
                stream_init = 1;
            }
            if (ogg_page_serialno(&og) != os.serialno)
            {
                /* so all streams are read. */
                ogg_stream_reset_serialno(&os, ogg_page_serialno(&og));
            }

            /*Add page to the bitstream*/
            ogg_stream_pagein(&os, &og);
            page_granule = ogg_page_granulepos(&og);
            /*Extract all available packets*/
            while (ogg_stream_packetout(&os, &op) == 1)
            {
                page_offset += op.bytes;
                mDecodeSize = mDecodeOffset + page_offset;
                //LOGD("header=%llu, offset=%llu, page=%d, decode=%llu", mHeadSize, mDecodeOffset , page_offset , mDecodeSize);

                if (mDecodeSize < 0) mDecodeSize = 0;

                bool isOpusHead = false;
                //LOGD("mDecodeSize=%u", mDecodeSize);
                //LOGD("opus_serialno=[ %d : %lld : %ld : %lld  ]", opus_serialno, os.packetno, os.pageno, audio_size);
                /*OggOpus streams are identified by a magic string in the initial
                 stream header.*/
                if (op.b_o_s && op.bytes >= 8 && !memcmp(op.packet, "OpusHead", 8))
                {
                    isOpusHead = true;
                    if (has_opus_stream && has_tags_packet && st == NULL)
                    {
                        /*If we're seeing another BOS OpusHead now it means
                         the stream is chained without an EOS.*/
                        has_opus_stream = 0;
                        if (st) opus_multistream_decoder_destroy(st);
                        st = NULL;
                        // fprintf (stderr,"\nWarning: stream %" I64FORMAT " ended without EOS and a new stream began.\n",(long long)os.serialno);
                    }
                    if (!has_opus_stream)
                    {
                        if (packet_count > 0 && opus_serialno == os.serialno)
                        {
                            LOGE("error: packet_count > 0 && opus_serialno == os.serialno");
                            // fprintf(stderr,"\nError: Apparent chaining without changing serial number (%" I64FORMAT "==%" I64FORMAT ").\n", (long long)opus_serialno,(long long)os.serialno);
                            return -1;
                        }
                        opus_serialno = os.serialno;
                        has_opus_stream = 1;
                        has_tags_packet = 0;
                        link_out = 0;
                        packet_count = 0;
                        eos = 0;
                        total_links++;
                    }
                    else
                    {
                        //fprintf  (stderr,"\nWarning: ignoring opus stream %" I64FORMAT "\n",(long long)os.serialno);
                    }
                }
                if (!has_opus_stream || os.serialno != opus_serialno)
                {
                    LOGW("warning: !has_opus_stream[%d] || os.serialno[%ld] != opus_serialno[%d]", has_opus_stream, os.serialno, opus_serialno);
                    break;
                }
                /*If first packet in a logical stream, process the Opus header*/
                if (packet_count == 0) //flush后不会再进入这里了
                {
                    st = process_header(&op, &rate, &mapping_family, &channels, &preskip, &gain, manual_gain, &streams, wav_format);
                    if (!st)
                    {
                        LOGE("error: process_header");
                        return -1;
                    }

                    if (ogg_stream_packetout(&os, &op) != 0 || og.header[og.header_len - 1] == 255)
                    {
                        /*The format specifies that the initial header and tags packets are on their
                         own pages. To aid implementors in discovering that their files are wrong
                         we reject them explicitly here. In some player designs files like this would
                         fail even without an explicit test.*/
                        // fprintf(stderr, "Extra packets on initial header page. Invalid stream.\n");
                        LOGE("error: Extra packets on initial header page. Invalid stream");
                        return -1;
                    }

                    /*Remember how many samples at the front we were told to skip
                     so that we can adjust the timestamp counting.*/
                    gran_offset = preskip;

                    /*Setup the memory for the dithered output*/
                    if (!shapemem.a_buf)
                    {
                        shapemem.a_buf = (float*) calloc(channels, sizeof(float) * 4);
                        shapemem.b_buf = (float*) calloc(channels, sizeof(float) * 4);
                        shapemem.fs = rate;
                    }
                    if (!output) output = (float*) malloc(sizeof(float) * MAX_FRAME_SIZE * channels);

                    /*Normal players should just play at 48000 or their maximum rate,
                     as described in the OggOpus spec.  But for commandline tools
                     like opusdec it can be desirable to exactly preserve the original
                     sampling rate and duration, so we have a resampler here.*/
                    if (rate != 48000 && resampler == NULL)
                    {
                        int err;
                        resampler = speex_resampler_init(channels, 48000, rate, 5, &err);
                        // if (err != 0) fprintf(stderr, "resampler error: %s\n", speex_resampler_strerror(err));
                        speex_resampler_skip_zeros(resampler);
                    }
                    // if (!fout) fout = out_file_open(outFile, &wav_format, rate, mapping_family, &channels, fp);
                    decoder = createTXZAudioDecoder(_env, clsDecoder, channels, rate, 16);
                    mHeadSize = mDecodeSize;
                    _env->SetIntField(decoder, mTXZAudioDecoder.mHeadSize, 0); //无法知道头大小
                    _env->SetIntField(decoder, mTXZAudioDecoder.mDurnation, 0);
                }
                else if (packet_count == 1)
                {
#if 0
                    has_tags_packet = 1;
#endif
                    if (ogg_stream_packetout(&os, &op) != 0 || og.header[og.header_len - 1] == 255)
                    {
                        // fprintf(stderr, "Extra packets on initial tags page. Invalid stream.\n");
                        LOGW("error: Extra packets on initial header page. Invalid stream");
                        // return -1;
                    }
                }
                else if (!isOpusHead)
                {
                    int ret;
                    opus_int64 maxout;
                    opus_int64 outsamp;

                    /*End of stream condition*/
                    if (op.e_o_s && os.serialno == opus_serialno) eos = 1; /* don't care for anything except opus eos */

                    /*Decode Opus packet*/
                    ret = opus_multistream_decode_float(st, (unsigned char*) op.packet, op.bytes, output, MAX_FRAME_SIZE, 0);

                    /*If the decoder returned less than zero, we have an error.*/
                    if (ret < 0)
                    {
                        // fprintf(stderr, "Decoding error: %s\n", opus_strerror(ret));
                        LOGE("error: Decoding error");
                        return -1;
                    }
                    frame_size = ret;

                    /*Apply header gain, if we're not using an opus library new
                     enough to do this internally.*/
                    if (gain != 0)
                    {
                        for (int i = 0; i < frame_size * channels; i++)
                            output[i] *= gain;
                    }

                    if (rate != 48000 && resampler == NULL)
                    {
                        int err;
                        resampler = speex_resampler_init(channels, 48000, rate, 5, &err);
                        // if (err != 0) fprintf(stderr, "resampler error: %s\n", speex_resampler_strerror(err));
                        speex_resampler_skip_zeros(resampler);
                    }

                    /*This handles making sure that our output duration respects
                     the final end-trim by not letting the output sample count
                     get ahead of the granpos indicated value.*/
                    maxout = ((page_granule - gran_offset) * rate / 48000) - link_out;
                    outsamp = audio_write(_env, decoder, output, channels, frame_size, NULL, resampler, &preskip, dither ? &shapemem : 0, 0,
                            0 > maxout ? 0 : maxout, fp);
                    link_out += outsamp;
                    audio_size += (fp ? 4 : 2) * outsamp * channels;
                    calDurnation(_env, decoder);
                }
                packet_count++;
                // LOGD("packet_count: %lld", packet_count);
            }
            /*We're done, drain the resampler if we were using it.*/
            if (eos && resampler)
            {
                float *zeros;
                int drain;

                zeros = (float *) calloc(100 * channels, sizeof(float));
                drain = speex_resampler_get_input_latency(resampler);
                do
                {
                    opus_int64 outsamp;
                    int tmp = drain;
                    if (tmp > 100) tmp = 100;
                    outsamp = audio_write(_env, decoder, zeros, channels, tmp, NULL, resampler, NULL, &shapemem, 0,
                            ((page_granule - gran_offset) * rate / 48000) - link_out, fp);
                    link_out += outsamp;
                    audio_size += (fp ? 4 : 2) * outsamp * channels;
                    calDurnation(_env, decoder);
                    drain -= tmp;
                } while (drain > 0);
                free(zeros);
                speex_resampler_destroy(resampler);
                resampler = NULL;
            }
            if (eos)
            {
                //TODO 可以计算持续时间了
            }
            //            结束时不是放opus解码器
            //            if (eos)
            //            {
            //                has_opus_stream = 0;
            //                if (st) opus_multistream_decoder_destroy(st);
            //                st = NULL;
            //            }

            if (mCanceled)
            {
                LOGW("decode canceled");
                break;
            }
        }

    }

    return -999;
}

void OpusAudioDecoder::calDurnation(
        JNIEnv* _env,
        jobject decoder)
{
    if (mCompleteDurnation) return;

    if (mDecodeSizeBeforeFlush + mDecodeSize > 0)
    {
        //粗略计算持续时间，audio_size是有多少帧数据
        int64_t durnation = (mPcmSize * 1000 / (rate * channels * 16 / 8)) * mTotalFileSize / (mDecodeSizeBeforeFlush + mDecodeSize);
        //如果包接受完整，并且没有清理过

        if ((int64_t) mDecodeSize >= mTotalFileSize && mDecodeSizeBeforeFlush == 0)
        {
            _env->SetIntField(decoder, mTXZAudioDecoder.mDurnation, durnation);
            LOGD("complete durnation: %lld", durnation);
            mCompleteDurnation = true;
        }
        else
        {
            _env->SetIntField(decoder, mTXZAudioDecoder.mDurnation, -durnation);
        }
    }
}

jobject OpusAudioDecoder::createFromData(
        JNIEnv* _env,
        jclass clsDecoder,
        const void* data,
        size_t len,
        size_t total)
{
    mTotalFileSize = total;
    write(data, len);
    jobject decoder = NULL;
    process(_env, clsDecoder, decoder);
    return decoder;
}

/**
 * 解码接口，返回解码用掉的数据，负数表示出错
 */
int OpusAudioDecoder::decode(
        JNIEnv* _env,
        jobject decoder,
        const void* data,
        size_t len)
{
    //LOGD("decode data size=%u", len);
    write(data, len);
    process(_env, NULL, decoder);
    return 0;
}

void OpusAudioDecoder::flush(
        JNIEnv* _env,
        jobject decoder)
{
    mDecodeSizeBeforeFlush += mDecodeSize;
    mDecodeSize = 0;

    mDecodeOffset = 0;

    AudioDecoder::flush(_env, decoder);
    if (stream_init)
    {
        ogg_stream_clear(&os);
    }
    ogg_sync_reset(&oy);
    oy.unsynced = 1;

    stream_init = 0;

    //        LOGD("%lld|%d|%lld|%lld|%d|%lld|%f|%d|%d|%d|%d|%d|%f|%d"
    //                ,packet_count
    //                ,total_links
    //                ,page_granule
    //                ,link_out
    //                ,eos
    //                ,audio_size
    //                , manual_gain
    //                ,preskip
    //                ,gran_offset
    //                ,has_opus_stream
    //                ,has_tags_packet
    //                ,dither
    //                ,gain
    //                ,streams);

    //packet_count = 0;
    //    total_links = 0;
    page_granule = 0; //flush时这个操作也很关键
    link_out = 0;
    eos = 0; //全部流获取完了
    // audio_size = 0;
    //    manual_gain = 0;
    //    preskip = 0;
    //    gran_offset = 0;
    has_opus_stream = 1; //强制置成1，否则会在eos置0
    //    has_tags_packet = 0;
    //    dither = 1;
    //    fp = 0;
    //    gain = 1;
    //    streams = 0;
}

void OpusAudioDecoder::destroy(
        JNIEnv* _env,
        jobject decoder)
{
    if (stream_init) ogg_stream_clear(&os);
    ogg_sync_clear(&oy);

    if (shapemem.a_buf)
    {
        free(shapemem.a_buf);
        shapemem.a_buf = NULL;
    }
    if (shapemem.b_buf)
    {
        free(shapemem.b_buf);
        shapemem.b_buf = NULL;
    }

    if (resampler != NULL)
    {
        speex_resampler_destroy(resampler);
        resampler = NULL;
    }

    if (st)
    {
        opus_multistream_decoder_destroy(st);
        st = NULL;
    }

    if (output)
    {
        free(output);
        output = NULL;
    }
}

