#include "OSAL_log.h"
#include "TXZJsonParser.h"
#include "rapidjson/rapidjson.h"
#include "rapidjson/document.h"
#include "rapidjson/error/en.h"
#include <unistd.h>
#include <string>
#include <vector>
#include <pthread.h>
#include <linux/prctl.h>
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<sys/types.h>

//#define LOGParse LOGD
#define LOGParse(...) do{}while(0)

//启用最小内存
#define PARSE_BY_MIN_MEM

typedef struct
{
    jclass ISax;

    jmethodID onNull;
    jmethodID onBool;
    jmethodID onInt;
    jmethodID onLong;
    jmethodID onDouble;
    jmethodID onString;

    jmethodID onObjectStart;
    jmethodID onObjectKey;
    jmethodID onObjectEnd;

    jmethodID onArrayStart;
    jmethodID onArrayEnd;

    jmethodID onSuccess;
    jmethodID onCancel;
    jmethodID onError;
} TSaxInterface;

static TSaxInterface ISax = { 0 };

struct TXZJsonParserSession
{
private:
    JavaVM* jvm;
    JNIEnv *envCreate;
    JNIEnv *env;
    static const long MAGIC_NUM = 0x19871224;
    long m_lCheck; //会话地址校验
    jobject jobjSax;
    ::rapidjson::Reader reader;
    bool m_bCancel;
    bool m_bComplete;
    int m_fdPipe[2];

    /*************************************InputStream*********************************/
public:
    typedef char Ch; //!< 流的字符类型
    //! 从流读取当前字符，不移动读取指针（read cursor）

private:
    ::std::basic_string< Ch > m_strBuffer;
    size_t m_nBufferSkip;
    size_t m_nPos;

    bool m_bSkipKey;
    int m_iSkipCount;

private:
    int _ReadPipe(
            void* pData,
            int iLen)
    {
        int n = 0, t = 0;
        while (t < iLen)
        {
            n = read(m_fdPipe[0], ((char*) pData) + t, iLen - t);
            if (n < 0)
            {
                return n;
            }
            t += n;
        }
        return iLen;
    }

    void _Read()
    {
        if (m_bCancel) return;
        if (m_bComplete) return;
        size_t nLen = 0;
        if (sizeof(nLen) != _ReadPipe(&nLen, sizeof(nLen)) || nLen == 0)
        {
            m_bComplete = true;
            return;
        }
        Ch* buf = new Ch[nLen];
        if ((signed) nLen != _ReadPipe(buf, nLen))
        {
            m_bComplete = true;
        }
        else
        {
#ifdef PARSE_BY_MIN_MEM
            if (m_nPos - m_nBufferSkip > 0)
            {
                m_strBuffer.erase(0, m_nPos - m_nBufferSkip);
                m_nBufferSkip = m_nPos;
            }
#endif
            m_strBuffer.append(buf, nLen);
        }
        delete[] buf;
    }
public:
    Ch Peek()
    {
        //LOGD("reader Peek enter:%u", m_nPos);
        if (m_nBufferSkip + m_strBuffer.size() <= m_nPos)
        {
            _Read();
        }
        //LOGD("reader Peek:%u", m_nPos);
        if (m_bCancel) return 0;
        if (m_bComplete) return 0;
        return m_strBuffer[m_nPos - m_nBufferSkip];
    }
    //! 从流读取当前字符，移动读取指针至下一字符。
    Ch Take()
    {
        //LOGD("reader Take enter:%u", m_nPos);
        if (m_nBufferSkip + m_strBuffer.size() <= m_nPos)
        {
            _Read();
        }
        //LOGD("reader Take:%u", m_nPos);
        if (m_bCancel) return 0;
        if (m_bComplete) return 0;
        return m_strBuffer[(m_nPos++) - m_nBufferSkip];
    }
    //! 获取读取指针。
    //! \return 从开始以来所读过的字符数量。
    size_t Tell()
    {
        LOGD("reader Tell:%u", m_nPos);
        return m_nPos;
    }
    Ch* PutBegin()
    {
        LOGF("crash PutBegin");
        RAPIDJSON_ASSERT(false);
        return 0;
    }
    void Put(
            Ch)
    {
        LOGF("crash Put");
        RAPIDJSON_ASSERT(false);
    }
    void Flush()
    {
        LOGF("crash Flush");
        RAPIDJSON_ASSERT(false);
    }
    size_t PutEnd(
            Ch*)
    {
        LOGF("crash PutEnd");
        RAPIDJSON_ASSERT(false);
        return 0;
    }
    /*************************************InputStream*********************************/

public:
    /*************************************Handler*********************************/
#define CHECK_SKIP_VALUE() do { \
    if (m_bSkipKey) \
    { \
        LOGD("CHECK_SKIP_VALUE: %d", m_iSkipCount); \
        if (m_iSkipCount == 0)m_bSkipKey = false; \
        return true; \
    } \
}while(0)

    bool Null()
    {
        LOGParse("onParse: %p[%d]NULL", this, m_bCancel);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onNull);
        return ret == 0;
    }
    bool Bool(
            bool b)
    {
        LOGParse("onParse: %p[%d]Bool=%d", this, m_bCancel, b);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onBool, (jboolean) b);
        return ret == 0;
    }
    bool Int(
            int i)
    {
        LOGParse("onParse: %p[%d]Int=%d", this, m_bCancel, i);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onInt, (jint) i);
        return ret == 0;
    }
    bool Uint(
            unsigned u)
    {
        LOGParse("onParse: %p[%d]Uint=%u", this, m_bCancel, u);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onInt, (jint) u);
        return ret == 0;
    }
    bool Int64(
            int64_t i)
    {
        LOGParse("onParse: %p[%d]Int64=%" PRIi64, this, m_bCancel, i);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onLong, (jlong) i);
        return ret == 0;
    }
    bool Uint64(
            uint64_t u)
    {
        LOGParse("onParse: %p[%d]Uint64=%" PRIu64, this, m_bCancel, u);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onLong, (jlong) u);
        return ret == 0;
    }
    bool Double(
            double d)
    {
        LOGParse("onParse: %p[%d]Double=%lf", this, m_bCancel, d);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jint ret = env->CallIntMethod(jobjSax, ISax.onDouble, (jdouble) d);
        return ret == 0;
    }
    bool String(
            const char* str,
            ::rapidjson::SizeType length,
            bool copy)
    {
        LOGParse("onParse: %p[%d]String=%s", this, m_bCancel, str);
        if (m_bCancel) return false;
        CHECK_SKIP_VALUE();
        jbyteArray bs = env->NewByteArray(length);
        env->SetByteArrayRegion(bs, 0, length, (const jbyte*) str);
        jint ret = env->CallIntMethod(jobjSax, ISax.onString, bs);
        env->DeleteLocalRef(bs);
        return ret == 0;
    }

#define CHECK_SKIP_CONTAINER(_enter) do { \
    if (m_bSkipKey) \
    { \
        if (_enter) { \
            ++m_iSkipCount;\
            LOGD("CHECK_SKIP_CONTAINER enter: %d", m_iSkipCount); \
        } \
        else {\
            if ((--m_iSkipCount) == 0)m_bSkipKey = false; \
            LOGD("CHECK_SKIP_CONTAINER exit: %d", m_iSkipCount); \
        }\
        return true; \
    } \
}while(0)

    bool StartObject()
    {
        LOGParse("onParse: %p[%d]StartObject", this, m_bCancel);
        if (m_bCancel) return false;
        CHECK_SKIP_CONTAINER(true);
        jint ret = env->CallIntMethod(jobjSax, ISax.onObjectStart);
        return ret == 0;
    }
    bool Key(
            const char* str,
            ::rapidjson::SizeType length,
            bool copy)
    {
        LOGParse("onParse: %p[%d]ObjectKey[%s]", this, m_bCancel, str);
        if (m_bCancel) return false;
        if (m_bSkipKey) return true;
        jbyteArray bs = env->NewByteArray(length);
        env->SetByteArrayRegion(bs, 0, length, (const jbyte*) str);
        jint ret = env->CallIntMethod(jobjSax, ISax.onObjectKey, bs);
        env->DeleteLocalRef(bs);
        if (ret == 1)
        {
            m_bSkipKey = true;
            m_iSkipCount = 0;
            return true;
        }
        return ret == 0;
    }
    bool EndObject(
            ::rapidjson::SizeType memberCount)
    {
        LOGParse("onParse: %p[%d]EndObject[%u]", this, m_bCancel, memberCount);
        if (m_bCancel) return false;
        CHECK_SKIP_CONTAINER(false);
        jint ret = env->CallIntMethod(jobjSax, ISax.onObjectEnd, (jint) memberCount);
        return ret == 0;
    }
    bool StartArray()
    {
        LOGParse("onParse: %p[%d]StartArray", this, m_bCancel);
        if (m_bCancel) return false;
        CHECK_SKIP_CONTAINER(true);
        jint ret = env->CallIntMethod(jobjSax, ISax.onArrayStart);
        return ret == 0;
    }
    bool EndArray(
            ::rapidjson::SizeType elementCount)
    {
        LOGParse("onParse: %p[%d]EndArray[%u]", this, m_bCancel, elementCount);
        if (m_bCancel) return false;
        CHECK_SKIP_CONTAINER(false);
        jint ret = env->CallIntMethod(jobjSax, ISax.onArrayEnd, (jint) elementCount);
        return ret == 0;
    }
    /*************************************Handler*********************************/

    TXZJsonParserSession(
            JNIEnv* e,
            jobject sax,
            size_t nBufferSize,
            bool thread)
    {
        LOGD("new session: %p, env[%p], sax[%p]", this, e, sax);

        envCreate = env = e;
        env->GetJavaVM(&jvm);

        if (ISax.ISax == NULL)
        {
            ISax.ISax = (jclass) env->NewGlobalRef(env->FindClass("com/txznet/algorithm/TXZJsonParser$ISax"));
            ISax.onNull = env->GetMethodID(ISax.ISax, "onNull", "()I");
            ISax.onBool = env->GetMethodID(ISax.ISax, "onBool", "(Z)I");
            ISax.onInt = env->GetMethodID(ISax.ISax, "onInt", "(I)I");
            ISax.onLong = env->GetMethodID(ISax.ISax, "onLong", "(J)I");
            ISax.onDouble = env->GetMethodID(ISax.ISax, "onDouble", "(D)I");
            ISax.onString = env->GetMethodID(ISax.ISax, "onString", "([B)I");
            ISax.onObjectStart = env->GetMethodID(ISax.ISax, "onObjectStart", "()I");
            ISax.onObjectKey = env->GetMethodID(ISax.ISax, "onObjectKey", "([B)I");
            ISax.onObjectEnd = env->GetMethodID(ISax.ISax, "onObjectEnd", "(I)I");
            ISax.onArrayStart = env->GetMethodID(ISax.ISax, "onArrayStart", "()I");
            ISax.onArrayEnd = env->GetMethodID(ISax.ISax, "onArrayEnd", "(I)I");
            ISax.onSuccess = env->GetMethodID(ISax.ISax, "onSuccess", "()I");
            ISax.onCancel = env->GetMethodID(ISax.ISax, "onCancel", "()I");
            ISax.onError = env->GetMethodID(ISax.ISax, "onError", "(IILjava/lang/String;)I");
        }

        m_bComplete = m_bCancel = false;
        m_lCheck = (MAGIC_NUM ^ (long) this);
        m_strBuffer.reserve(nBufferSize);
        m_nBufferSkip = m_nPos = 0;
        m_bSkipKey = false;
        m_iSkipCount = 0;

        pipe(m_fdPipe);

        if (thread)
        {
            jobjSax = env->NewGlobalRef(sax);
            pthread_t id;
            pthread_create(&id, 0, ThreadProc, this);
        }
        else
        {
            jobjSax = NULL;
        }
    }

    ~TXZJsonParserSession()
    {
        LOGD("delete session: %p", this);

        m_lCheck = MAGIC_NUM;

        if (jobjSax != NULL)
        {
            envCreate->DeleteGlobalRef(jobjSax);
            jobjSax = NULL;
        }

        ReleasePipe();
    }

    void ReleasePipe()
    {
        if (m_fdPipe[0] != -1)
        {
            close(m_fdPipe[0]);
            m_fdPipe[0] = -1;
        }
        if (m_fdPipe[1] != -1)
        {
            close(m_fdPipe[1]);
            m_fdPipe[1] = -1;
        }
    }

    static void* ThreadProc(
            void* param)
    {
        TXZJsonParserSession *pSession = (TXZJsonParserSession*) param;

        if (NULL == pSession->jvm) return NULL;
        int statusXYZ = pSession-> jvm->GetEnv((void**) &pSession->env, JNI_VERSION_1_6);
        if (statusXYZ < 0) pSession->jvm->AttachCurrentThread(&pSession->env, NULL);

        pSession->Parse();

        if (pSession->jvm != NULL) pSession-> jvm->DetachCurrentThread();

        return NULL;
    }

    bool Process(
            JNIEnv *env,
            jobject sax)
    {
        LOGD("process session: %p=[%p/%p]", this, this->jobjSax, sax);
        if (this->jobjSax != NULL) return false;
        this->env = env;
        this->jobjSax = sax;
        this->Parse();
        this->jobjSax = NULL;
        return true;
    }

    bool Bad()
    {
        return m_lCheck != (MAGIC_NUM ^ (long) this);
    }

    bool Write(
            const void* pData,
            size_t nLen)
    {
        LOGD("write session: %p=%u", this, nLen);

        if (nLen <= 0) return false;

        write(m_fdPipe[1], &nLen, sizeof(nLen));
        write(m_fdPipe[1], pData, nLen);

        return true;
    }

    void Cancel()
    {
        LOGD("cancel session: %p", this);

        m_bCancel = true;
        ReleasePipe();
    }

    void Complete()
    {
        LOGD("complete session: %p", this);

        size_t nLen = 0;
        write(m_fdPipe[1], &nLen, sizeof(nLen));
    }

    int Parse()
    {
        ::rapidjson::ParseResult result = reader.Parse(*this, *this);

        LOGD("end session: %p", this);

        if (m_bCancel)
        {
            env->CallIntMethod(jobjSax, ISax.onCancel);
            return -1;
        }

        if (result.IsError())
        {
            jstring errDesc = env->NewStringUTF(::rapidjson::GetParseError_En(result.Code()));
            env->CallIntMethod(jobjSax, ISax.onError, (jint) result.Offset(), (jint) result.Code(), errDesc);
            env->DeleteLocalRef(errDesc);
            return 1;
        }

        env->CallIntMethod(jobjSax, ISax.onSuccess);

        return 0;
    }
};

/**
 * 创建json解析会话
 */
JNIEXPORT jlong JNICALL Java_com_txznet_algorithm_TXZJsonParser_create(
        JNIEnv *env,
        jclass,
        jobject jobjSax,
        jint jintBufferSize,
        jboolean thread)
{
    TXZJsonParserSession* pobjSession = new TXZJsonParserSession(env, jobjSax, jintBufferSize, thread);
    return ((long) pobjSession);
}

JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_process(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId,
        jobject jobjSax)
{
    if (jlngSessionId == 0)
    {
        return false;
    }
    TXZJsonParserSession* pobjSession = (TXZJsonParserSession*) jlngSessionId;
    if (pobjSession->Bad())
    {
        return false;
    }
    return pobjSession->Process(env, jobjSax);
}

/**
 * 写入json数据
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_write(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId,
        jbyteArray data)
{
    if (jlngSessionId == 0)
    {
        return false;
    }
    TXZJsonParserSession* pobjSession = (TXZJsonParserSession*) jlngSessionId;
    if (pobjSession->Bad())
    {
        return false;
    }
    jbyte* buf = env->GetByteArrayElements(data, NULL);
    pobjSession->Write(buf, env->GetArrayLength(data));
    env->ReleaseByteArrayElements(data, buf, 0);
    return true;
}

/**
 * 取消json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_cancel(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId)
{
    if (jlngSessionId == 0)
    {
        return false;
    }
    TXZJsonParserSession* pobjSession = (TXZJsonParserSession*) jlngSessionId;
    if (pobjSession->Bad())
    {
        return false;
    }
    pobjSession->Cancel();
    return true;
}

/**
 * 结束json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_complete(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId)
{
    if (jlngSessionId == 0)
    {
        return false;
    }
    TXZJsonParserSession* pobjSession = (TXZJsonParserSession*) jlngSessionId;
    if (pobjSession->Bad())
    {
        return false;
    }
    pobjSession->Complete();
    return true;
}

/**
 * 销毁json解析会话
 */
JNIEXPORT jboolean JNICALL Java_com_txznet_algorithm_TXZJsonParser_destroy(
        JNIEnv *env,
        jclass,
        jlong jlngSessionId)
{
    if (jlngSessionId == 0)
    {
        return false;
    }
    TXZJsonParserSession* pobjSession = (TXZJsonParserSession*) jlngSessionId;
    if (pobjSession->Bad())
    {
        return false;
    }
    delete pobjSession;
    return true;
}
