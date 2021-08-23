#include "OSAL_log.h"
#include "TXZAudioResampler.h"

#include "speex_resampler.h"

class TXZAudioResampler
{
public:
    SpeexResamplerState* state;
    jbyte* buf;
    jint len;
    jint rateIn;
    jint rateOut;

    TXZAudioResampler(
            jint quality,
            jint channels,
            jint rIn,
            jint rOut) :
        buf(NULL), len(0)
    {
        int err = 0;

        LOGD("create handler option: quality=%d, channels=%d, rate=[%d>>%d]", quality, channels, rIn, rOut);

        state = speex_resampler_init(channels, rateIn = rIn, rateOut = rOut, quality, &err);

        if (state == NULL)
        {
            LOGE("create handler error %d: %s", err, speex_resampler_strerror(err));
        }
    }

    void resizeBufferForDataIn(
            jint lenIn)
    {
        jint need = ((lenIn >> 1) * rateOut / rateIn + 1) << 1;
        if (need > len)
        {
            if (buf != NULL)
            {
                delete[] buf;
            }
            buf = new jbyte[len = need];
            LOGD("alloc handler buffer %d: %p", len, buf);
        }
    }

    ~TXZAudioResampler()
    {
        if (state != NULL)
        {
            speex_resampler_destroy(state);
            state = NULL;
        }
        if (buf != NULL)
        {
            delete[] buf;
            buf = NULL;
        }
    }
};

/**
 * 创建处理器
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_createResampler(
        JNIEnv *env,
        jclass,
        jint quality,
        jint channels,
        jint rateIn,
        jint rateOut)
{

    TXZAudioResampler *handler = new TXZAudioResampler(quality, channels, rateIn, rateOut);

    if (handler != NULL && handler->state == NULL)
    {
        delete handler;
        handler = NULL;
    }

    LOGD("create handler: %p", handler);

    return (jlong) handler;
}

/**
 * 销毁处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_destroyResampler(
        JNIEnv *env,
        jclass,
        jlong handlerIn)
{
    TXZAudioResampler *handler = (TXZAudioResampler *) handlerIn;

    LOGD("destroy handler: %p", handler);

    if (handler != NULL)
    {
        delete handler;
    }
}

/**
 * 运行处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_runResampler(
        JNIEnv *env,
        jclass,
        jlong handlerIn,
        jbyteArray dataIn,
        jint offsetIn,
        jint lenIn,
        jbyteArray dataOut,
        jint offsetOut)
{
    TXZAudioResampler *handler = (TXZAudioResampler *) handlerIn;
    handler->resizeBufferForDataIn(lenIn);

    jbyte * data = env->GetByteArrayElements(dataIn, NULL);

    spx_uint32_t in = lenIn >> 1;
    spx_uint32_t out = handler->len >> 1;
    int ret = speex_resampler_process_interleaved_int(handler->state, (spx_int16_t *) (data + offsetIn), &in, (spx_int16_t *) handler->buf, &out);

    env->ReleaseByteArrayElements(dataIn, data, 0);

    if (ret != RESAMPLER_ERR_SUCCESS)
    {
        LOGE("run handler error %d: %s", ret, speex_resampler_strerror(ret));
        return -1;
    }

    out <<= 1;
    env->SetByteArrayRegion(dataOut, offsetOut, out, handler->buf);

    return out;
}

