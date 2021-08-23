#include "OSAL_log.h"
#include "TXZAudioEchoCancel.h"

#include "speex/speex_echo.h"

class TXZAudioEchoCancel
{
public:
    SpeexEchoState* state;
    jbyte* buf;
    jint len;
    jint frame_size;
    jint filter_length;

    TXZAudioEchoCancel(
            jint frame_size,
            jint filter_length) :
        buf(NULL), len(0)
    {
        LOGD("create handler option: frame_size=%d, filter_length=%d", frame_size, filter_length);

        state = speex_echo_state_init(frame_size, filter_length);
    }

    void resizeBufferForDataIn(
            jint lenIn)
    {
        jint need = lenIn;
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

    ~TXZAudioEchoCancel()
    {
        if (state != NULL)
        {
            speex_echo_state_destroy(state);
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
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_createEchoCancel(
        JNIEnv *env,
        jclass,
        jint frame_size,
        jint filter_length)
{

    TXZAudioEchoCancel *handler = new TXZAudioEchoCancel(frame_size, filter_length);

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
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_destroyEchoCancel(
        JNIEnv *env,
        jclass,
        jlong handlerIn)
{
    TXZAudioEchoCancel *handler = (TXZAudioEchoCancel *) handlerIn;

    LOGD("destroy handler: %p", handler);

    if (handler != NULL)
    {
        delete handler;
    }
}

/**
 * 运行处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_runEchoCancel(
        JNIEnv *env,
        jclass,
        jlong handlerIn,
        jbyteArray dataIn,
        jint offsetIn,
        jint lenIn,
        jbyteArray dataCmp,
        jint offsetCmp,
        jbyteArray dataOut,
        jint offsetOut)
{
    TXZAudioEchoCancel *handler = (TXZAudioEchoCancel *) handlerIn;
    handler->resizeBufferForDataIn(lenIn);

    jbyte * pIn = env->GetByteArrayElements(dataIn, NULL);
    jbyte * pCmp = env->GetByteArrayElements(dataCmp, NULL);

    speex_echo_cancellation(handler->state, (spx_int16_t *) (pIn + offsetIn), (spx_int16_t *) (pCmp + offsetCmp), (spx_int16_t *) handler->buf);

    env->ReleaseByteArrayElements(dataIn, pIn, 0);
    env->ReleaseByteArrayElements(dataCmp, pCmp, 0);

    env->SetByteArrayRegion(dataOut, offsetOut, lenIn, handler->buf);
}

