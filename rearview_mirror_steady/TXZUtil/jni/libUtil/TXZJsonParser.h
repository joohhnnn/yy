#ifndef __TXZ_JSON_PARSER_H__
#define __TXZ_JSON_PARSER_H__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

/**
 * 创建json解析会话
 */
JNIEXPORT jlong JNICALL Java_com_txznet_algorithm_TXZJsonParser_create(
        JNIEnv *env,
        jclass,
        jobject jobjSax,
        jint jintBufferSize,
        jboolean thread);

/**
 * 开始处理数据
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_process(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId,
        jobject jobjSax);

/**
 * 写入json数据
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_write(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId,
        jbyteArray data);

/**
 * 取消json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_cancel(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId);

/**
 * 结束json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_complete(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId);

/**
 * 销毁json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_destroy(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId);

#ifdef __cplusplus
}
#endif

#endif

