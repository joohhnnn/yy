package com.txznet.txz.util.recordcenter;

public interface IRecorderFactory{
    public ITXZRecorder create(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes);
}
