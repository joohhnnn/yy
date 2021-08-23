#ifndef __TXZ_AUDIO_ECHO_CANCEL_H__
#define __TXZ_AUDIO_ECHO_CANCEL_H__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 创建处理器
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_createEchoCancel(
        JNIEnv *env,
        jclass,
        jint frame_size,
        jint filter_length);

/**
 * 销毁处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_destroyEchoCancel(
        JNIEnv *env,
        jclass,
        jlong handler);

/**
 * 运行处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioEchoCancel_runEchoCancel(
        JNIEnv *env,
        jclass,
        jlong handler,
        jbyteArray dataIn,
        jint offsetIn,
        jint lenIn,
        jbyteArray dataCmp,
        jint offsetCmp,
        jbyteArray dataOut,
        jint offsetOut);

#ifdef __cplusplus
}
#endif

#endif

