#ifndef __AUDIO_DECODER_H__
#define __AUDIO_DECODER_H__

#include "OSAL_log.h"
#include <jni.h>
#include <unistd.h>
#include <string>
#include <vector>
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<sys/types.h>
#include <math.h>

class AudioDecoder
{
private:
    static const long MAGIC_NUM = 0x19871224;
    long check;

protected:
    JNIEnv* envCreate;

    typedef struct
    {
        jmethodID TXZAudioDecoder;
        jfieldID nativeCodec;
        jfieldID mHeadSize;
        jfieldID mDurnation;
        jfieldID mChannel;
        jfieldID mSampleRate;
        jfieldID mAudioFormat;
        jfieldID mLastDecodeData;
        jfieldID mLastDecodeDataSize;
        jfieldID mLastDecodeRemainSize;
        jfieldID mLastDecodeDataOffset;
    } TTXZAudioDecoder;

    static TTXZAudioDecoder mTXZAudioDecoder;

    ::std::string tmpBuffer;
    ::std::string outBuffer;
    pthread_mutex_t lockBuffer;

    bool mCanceled;
    uint64_t mDecodeSize;
    uint64_t mDecodeMinPacketSize;

    AudioDecoder(
            JNIEnv* env);

    static inline void initDecoderClass(
            JNIEnv* _env,
            jclass clsDecoder)
    {
        static bool need = true;
        if (need)
        {
            need = false;
            mTXZAudioDecoder.TXZAudioDecoder = _env->GetMethodID(clsDecoder, "<init>", "(III)V");
            mTXZAudioDecoder.nativeCodec = _env->GetFieldID(clsDecoder, "nativeCodec", "J");
            mTXZAudioDecoder.mHeadSize = _env->GetFieldID(clsDecoder, "mHeadSize", "I");
            mTXZAudioDecoder.mDurnation = _env->GetFieldID(clsDecoder, "mDurnation", "I");
            mTXZAudioDecoder.mChannel = _env->GetFieldID(clsDecoder, "mChannel", "I");
            mTXZAudioDecoder.mSampleRate = _env->GetFieldID(clsDecoder, "mSampleRate", "I");
            mTXZAudioDecoder.mAudioFormat = _env->GetFieldID(clsDecoder, "mAudioFormat", "I");
            mTXZAudioDecoder.mLastDecodeDataSize = _env->GetFieldID(clsDecoder, "mLastDecodeDataSize", "I");
            mTXZAudioDecoder.mLastDecodeRemainSize = _env->GetFieldID(clsDecoder, "mLastDecodeRemainSize", "I");
            mTXZAudioDecoder.mLastDecodeDataOffset = _env->GetFieldID(clsDecoder, "mLastDecodeDataOffset", "J");
            mTXZAudioDecoder.mLastDecodeData = _env->GetFieldID(clsDecoder, "mLastDecodeData", "[B");
        }
    }

    jobject inline createTXZAudioDecoder(
            JNIEnv* _env,
            jclass clsDecoder,
            int channel,
            int sample_rate,
            int audio_format)
    {
        LOGD("createTXZAudioDecoder: env[%p], cls[%p], channel[%d], rate[%d], format[%d]", _env, clsDecoder, channel, sample_rate, audio_format);

        jobject decoder = _env->NewObject(clsDecoder, mTXZAudioDecoder.TXZAudioDecoder, sample_rate, channel, audio_format);
        //_env->SetIntField(decoder, mTXZAudioDecoder.mChannel, channel);
        //_env->SetIntField(decoder, mTXZAudioDecoder.mSampleRate, sample_rate);
        //_env->SetIntField(decoder, mTXZAudioDecoder.mAudioFormat, audio_format);
        _env->SetLongField(decoder, mTXZAudioDecoder.nativeCodec, (jlong) this);
        jbyteArray data = (jbyteArray) _env->GetObjectField(decoder, mTXZAudioDecoder.mLastDecodeData);
        mDecodeMinPacketSize = _env->GetArrayLength(data);
        _env->DeleteLocalRef(data);
        return decoder;
    }

    virtual jobject createFromData(
            JNIEnv* _env,
            jclass clsDecoder,
            const void* data,
            size_t len,
            size_t total) = 0;

    void appendOutput(
            JNIEnv* _env,
            jobject decoder,
            const void* data,
            size_t len);

public:

    inline void setCancelFlag(
            bool cancelFlag)
    {
        mCanceled = cancelFlag;
    }

    /**
     * 创建接口，返回是否通过数据创建成功
     */
    jobject create(
            JNIEnv* _env,
            jclass clsDecoder,
            const void* data,
            size_t len,
            size_t total)
    {
        initDecoderClass(_env, clsDecoder);

        return createFromData(_env, clsDecoder, data, len, total);
    }

    /**
     * 解码接口，返回解码用掉的数据，负数表示出错
     */
    virtual int decode(
            JNIEnv* _env,
            jobject decoder,
            const void* data,
            size_t len) = 0;
    /**
     * 清空接口
     */
    virtual void flush(
            JNIEnv* _env,
            jobject decoder);

    /**
     * 读取解码数据
     */
    virtual void read(
            JNIEnv* _env,
            jobject decoder);

    /**
     * 销毁接口
     */
    virtual void destroy(
            JNIEnv* _env,
            jobject decoder) = 0;

    virtual ~AudioDecoder();

    bool Bad()
    {
        return check != (MAGIC_NUM ^ ((long) this));
    }
};

#endif
