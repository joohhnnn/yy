#include "OSAL_log.h"
#include "TXZAudioProcess.h"

#include "speex/speex_echo.h"
#include "speex/speex_preprocess.h"

class TXZAudioPreProcessor
{
public:
    SpeexPreprocessState* state;
    jbyte* buf;
    TXZAudioPreProcessor(
            int frame_size,
            int sampling_rate)
    {
        state = speex_preprocess_state_init(frame_size, sampling_rate);
        buf = new jbyte[frame_size * 2];
    }

    ~TXZAudioPreProcessor()
    {
        speex_preprocess_state_destroy(state);
        if (buf != NULL)
        {
            delete[] buf;
        }
    }
};

/**
 * 创建处理器
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_createPreprocessor(
        JNIEnv *env,
        jclass,
        jint frame_size,
        jint sampling_rate)
{
    TXZAudioPreProcessor *processor = new TXZAudioPreProcessor(frame_size, sampling_rate);
    if (processor != NULL)
    {
        if (processor->state == NULL)
        {
            delete processor;
            return 0;
        }
    }
    return (jlong) processor;
}

/**
 * 销毁处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_destroyPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor)
{
    delete ((TXZAudioPreProcessor *) processor);
}

/**
 * 设置处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_controlPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jint request,
        jbyteArray param)
{
    jbyte* data = env->GetByteArrayElements(param, NULL);
    LOGD("control preprocessor: %d=[%d/%f]", request, *((int*)data), *((float*)data));
    env->ReleaseByteArrayElements(param, data, 0);
    jint ret = speex_preprocess_ctl(((TXZAudioPreProcessor *) processor)->state, request, data);
    return ret;
}

/**
 * 更新处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_updatePreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jbyteArray data,
        jint offset,
        jint len)
{
    jbyte *buf = env->GetByteArrayElements(data, NULL);
    memcpy(((TXZAudioPreProcessor *) processor)->buf, buf + offset, len);
    env->ReleaseByteArrayElements(data, buf, 0);
    speex_preprocess_estimate_update(((TXZAudioPreProcessor *) processor)->state, (spx_int16_t *) ((TXZAudioPreProcessor *) processor)->buf);
    env->SetByteArrayRegion(data, offset, len, ((TXZAudioPreProcessor *) processor)->buf);
}

/**
 * 运行处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_runPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jbyteArray data,
        jint offset,
        jint len)
{
    jbyte *buf = env->GetByteArrayElements(data, NULL);
    memcpy(((TXZAudioPreProcessor *) processor)->buf, buf + offset, len);
    env->ReleaseByteArrayElements(data, buf, 0);
    jint ret = speex_preprocess_run(((TXZAudioPreProcessor *) processor)->state, (spx_int16_t *) ((TXZAudioPreProcessor *) processor)->buf);
    env->SetByteArrayRegion(data, offset, len, ((TXZAudioPreProcessor *) processor)->buf);
    return ret;
}

