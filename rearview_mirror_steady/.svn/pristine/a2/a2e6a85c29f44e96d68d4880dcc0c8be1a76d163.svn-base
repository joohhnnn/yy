#ifndef __OPUS_AUDIO_DECODER_H__
#define __OPUS_AUDIO_DECODER_H__

#include "AudioDecoder.h"

#include "ogg/ogg.h"
#include "opus.h"
#include "opus_header.h"
#include "opus_multistream.h"
#include "speex_resampler.h"
#include "diag_range.h"

class OpusAudioDecoder: public AudioDecoder
{
private:
    struct shapestate
    {
        float * b_buf;
        float * a_buf;
        int fs;
        int mute;
    };

    static const int MAX_FRAME_SIZE = (960 * 6);

private:
    ogg_sync_state oy;
    ogg_page og;
    ogg_packet op;
    ogg_stream_state os;

    float *output;
    int frame_size;
    OpusMSDecoder *st;opus_int64 packet_count;
    int total_links;
    int stream_init;
    ogg_int64_t page_granule;
    ogg_int64_t link_out;
    int eos;
    ogg_int64_t audio_size;
    float manual_gain;
    int channels;
    int mapping_family;
    int rate;
    int wav_format;
    int preskip;
    int gran_offset;
    int has_opus_stream;
    int has_tags_packet;
    ogg_int32_t opus_serialno;
    int dither;
    int fp;
    shapestate shapemem;
    SpeexResamplerState *resampler;
    float gain;
    int streams;

    uint64_t mDecodeOffset;
    uint64_t mHeadSize;

    uint64_t mPcmSize;
    uint64_t mDecodeSizeBeforeFlush; //清理前已解码的数据量，与audio_size算动态时长
    int64_t mTotalFileSize; //整个文件的大小
    bool mCompleteDurnation; //是否已经计算完整的时长
public:
    OpusAudioDecoder(
            JNIEnv* _env);

    virtual ~OpusAudioDecoder();

private:
    void shape_dither_toshort(
            shapestate *_ss,
            short *_o,
            float *_i,
            int _n,
            int _CC);

    opus_int64 audio_write(
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
            int fp);

    OpusMSDecoder *process_header(
            ogg_packet *op,
            opus_int32 *rate,
            int *mapping_family,
            int *channels,
            int *preskip,
            float *gain,
            float manual_gain,
            int *streams,
            int wav_format);

    int write(
            const void *data,
            int len);

    int process(
            JNIEnv* _env,
            jclass clsDecoder,
            jobject& decoder);

    virtual jobject createFromData(
            JNIEnv* _env,
            jclass clsDecoder,
            const void* data,
            size_t len,
            size_t total);

    void calDurnation(
            JNIEnv* _env,
            jobject decoder);

public:

    virtual int decode(
            JNIEnv* _env,
            jobject decoder,
            const void* data,
            size_t len);

    virtual void flush(
            JNIEnv* _env,
            jobject decoder);

    virtual void destroy(
            JNIEnv* _env,
            jobject decoder);

};

#endif

