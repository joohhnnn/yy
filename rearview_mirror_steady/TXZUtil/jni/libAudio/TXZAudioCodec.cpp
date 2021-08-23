#include "OSAL_log.h"
#include "TXZAudioCodec.h"

#include "OpusAudioDecoder.h"

/**
 * 创建编码/解码器
 */
JNIEXPORT jobject JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeCreateDecoder(
        JNIEnv *env,
        jclass clsDecoder,
        jstring url,
        jbyteArray data,
        jlong offset,
        jlong len,
        jlong total)
{

    //根据jobjOption分配编码/解码器
    {
        AudioDecoder *codec = new OpusAudioDecoder(env);

        jbyte* buf = env->GetByteArrayElements(data, NULL);
        jobject ret = codec->create(env, clsDecoder, buf + offset, len, total);
        env->ReleaseByteArrayElements(data, buf, 0);

        if (ret == NULL)
        {
            codec->destroy(env, NULL);
            delete codec;
        }
        else
        {
            return ret;
        }
    }
    return NULL;
}

/**
 * 转换数据
 */
JNIEXPORT jbyteArray JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeDecode(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec,
        jbyteArray data,
        jlong offset,
        jlong len)
{
    AudioDecoder * codec = (AudioDecoder *) jlngCodec;
    if (codec == NULL) return NULL;
    if (codec->Bad()) return NULL;
    codec->setCancelFlag(false);
    jbyte* buf = env->GetByteArrayElements(data, NULL);
    codec->decode(env, decoder, buf + offset, len);
    env->ReleaseByteArrayElements(data, buf, 0);
    return 0;
}

JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeCancel(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec)
{
    AudioDecoder * codec = (AudioDecoder *) jlngCodec;
    if (codec == NULL) return;
    if (codec->Bad()) return;
    codec->setCancelFlag(true);
}

JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeFlush(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec)
{
    AudioDecoder * codec = (AudioDecoder *) jlngCodec;
    if (codec == NULL) return;
    if (codec->Bad()) return;
    codec->flush(env, decoder);
}

JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeRead(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec)
{
    AudioDecoder * codec = (AudioDecoder *) jlngCodec;
    if (codec == NULL) return;
    if (codec->Bad()) return;
    codec->read(env, decoder);
}

/**
 * 销毁编码/解码器
 */
JNIEXPORT jbyteArray JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeRelease(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec)
{
    AudioDecoder * codec = (AudioDecoder *) jlngCodec;
    if (codec == NULL) return NULL;
    if (codec->Bad()) return NULL;
    codec->destroy(env, decoder);
    delete codec;
    return 0;
}
