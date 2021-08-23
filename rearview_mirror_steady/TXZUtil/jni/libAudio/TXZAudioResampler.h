#ifndef __TXZ_AUDIO_RESAMPLER_H__
#define __TXZ_AUDIO_RESAMPLER_H__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 创建处理器
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_createResampler(
        JNIEnv *env,
        jclass,
        jint quality,
        jint channels,
        jint rateIn,
        jint rateOut);

/**
 * 销毁处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_destroyResampler(
        JNIEnv *env,
        jclass,
        jlong handler);

/**
 * 运行处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioResampler_runResampler(
        JNIEnv *env,
        jclass,
        jlong handler,
        jbyteArray dataIn,
        jint offsetIn,
        jint lenIn,
        jbyteArray dataOut,
        jint offsetOut);

#ifdef __cplusplus
}
#endif

#endif

