#include "AudioDecoder.h"
#include <pthread.h>

AudioDecoder::TTXZAudioDecoder AudioDecoder::mTXZAudioDecoder = { 0 };

AudioDecoder::AudioDecoder(
        JNIEnv* env) :
    envCreate(env), mCanceled(false), mDecodeSize(0)
{
    check = MAGIC_NUM ^ ((long) this);

    pthread_mutex_init(&lockBuffer, NULL);
}

AudioDecoder::~AudioDecoder()
{
    check = MAGIC_NUM;
    pthread_mutex_destroy(&lockBuffer);
}

void AudioDecoder::flush(
        JNIEnv* _env,
        jobject decoder)
{
    mDecodeSize = 0;
    pthread_mutex_lock(&lockBuffer);
    tmpBuffer.clear();
    outBuffer.clear();
    pthread_mutex_unlock(&lockBuffer);
}

void AudioDecoder::appendOutput(
        JNIEnv* _env,
        jobject decoder,
        const void* data,
        size_t len)
{
    //LOGD("decode audio size=%d, %llu", len, mDecodeSize);
    pthread_mutex_lock(&lockBuffer);
    tmpBuffer.append((const char*) data, len);
    len = tmpBuffer.size();
    if (len >= mDecodeMinPacketSize)
    {
        outBuffer.append((const char*) &mDecodeSize, sizeof(mDecodeSize));
        outBuffer.append(tmpBuffer.data(), mDecodeMinPacketSize);
        tmpBuffer.erase(0, mDecodeMinPacketSize);
    }
    _env->SetIntField(decoder, mTXZAudioDecoder.mLastDecodeRemainSize, outBuffer.size());
    pthread_mutex_unlock(&lockBuffer);
}

void AudioDecoder::read(
        JNIEnv* _env,
        jobject decoder)
{
    jbyteArray data = (jbyteArray) _env->GetObjectField(decoder, mTXZAudioDecoder.mLastDecodeData);

    pthread_mutex_lock(&lockBuffer);
    if (outBuffer.size() < mDecodeMinPacketSize + sizeof(size_t))
    {
        if (tmpBuffer.empty())
        {
            _env->SetIntField(decoder, mTXZAudioDecoder.mLastDecodeDataSize, 0);
        }
        else
        {
            size_t len = tmpBuffer.size();
            //考虑双声道，16位
            len = (len >> 2) << 2;
            if (len > 0)
            {
                _env->SetByteArrayRegion(data, 0, len, (const jbyte*) tmpBuffer.data());
                tmpBuffer.erase(0, len);
                _env->SetLongField(decoder, mTXZAudioDecoder.mLastDecodeDataOffset, mDecodeSize);
            }
            _env->SetIntField(decoder, mTXZAudioDecoder.mLastDecodeDataSize, len);
        }
    }
    else
    {
        size_t offset = *((size_t*) (outBuffer.data()));
        _env->SetByteArrayRegion(data, 0, mDecodeMinPacketSize, (const jbyte*) outBuffer.data() + sizeof(mDecodeSize));
        outBuffer.erase(0, mDecodeMinPacketSize + sizeof(mDecodeSize));
        _env->SetLongField(decoder, mTXZAudioDecoder.mLastDecodeDataOffset, offset);
        _env->SetIntField(decoder, mTXZAudioDecoder.mLastDecodeDataSize, mDecodeMinPacketSize);
    }
    _env->SetIntField(decoder, mTXZAudioDecoder.mLastDecodeRemainSize, outBuffer.size());
    pthread_mutex_unlock(&lockBuffer);

    //LOGD("read audio data size: %d", len);

    _env->DeleteLocalRef(data);
}
