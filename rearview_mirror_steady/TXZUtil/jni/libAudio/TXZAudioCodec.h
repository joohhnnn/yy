#ifndef __TXZ_AUDIO_CODEC_H__
#define __TXZ_AUDIO_CODEC_H__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 创建解码器
 */
JNIEXPORT jobject JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeCreateDecoder(
        JNIEnv *env,
        jclass,
        jstring url,
        jbyteArray data,
        jlong offset,
        jlong len,
        jlong total);

/**
 * 解码数据
 */
JNIEXPORT jbyteArray JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeDecode(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec,
        jbyteArray data,
        jlong offset,
        jlong len);

/**
 * 取消当前解码
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeCancel(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec);

/**
 * 清理数据
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeFlush(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec);

/**
 * 读取解码数据
 */
JNIEXPORT void JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeRead(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec);

/**
 * 获取已经解码的数据量
 */
JNIEXPORT jlong JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeGetDecodeSize(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec);

/**
 * 释放解码器
 */
JNIEXPORT jbyteArray JNICALL Java_com_txznet_audio_codec_TXZAudioDecoder_nativeRelease(
        JNIEnv *env,
        jobject decoder,
        jlong jlngCodec);

#ifdef __cplusplus
}
#endif

#endif

