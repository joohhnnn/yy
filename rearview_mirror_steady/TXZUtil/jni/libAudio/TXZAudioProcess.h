#ifndef __TXZ_AUDIO_PROCESS_H__
#define __TXZ_AUDIO_PROCESS_H__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 创建处理器
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_createPreprocessor(
        JNIEnv *env,
        jclass,
        jint frame_size,
        jint sampling_rate);

/**
 * 销毁处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_destroyPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor);

/**
 * 设置处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_controlPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jint request,
        jbyteArray param);

/**
 * 更新处理器
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_updatePreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jbyteArray data,
        jint offset,
        jint len);

/**
 * 运行处理器
 */
JNIEXPORT jint JNICALL Java_com_txznet_audio_codec_TXZAudioProcessor_runPreprocessor(
        JNIEnv *env,
        jclass,
        jlong processor,
        jbyteArray data,
        jint offset,
        jint len);

#ifdef __cplusplus
}
#endif

#endif

