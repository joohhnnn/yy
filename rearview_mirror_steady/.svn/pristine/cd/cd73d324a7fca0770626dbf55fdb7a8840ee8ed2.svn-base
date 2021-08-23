package com.txznet.txz.component.tts.yunzhisheng_3_0;

import android.media.AudioTrack;

import com.txznet.txz.module.tts.TtsManager;

public class SysAudioTrack implements ITxzAudioTrack{
    private AudioTrack mAudioTrack = null;
	@Override
	public int open(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int mode){
		int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		mAudioTrack = new AudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
		if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
			mAudioTrack.play();
			return 0;
		}
		return -1;
	}

	@Override
	public int write(byte[] data, int size) {
		int nRet = 0;
		if (mAudioTrack != null) {
			float r = TtsManager.getInstance().getVolumeRate();
			//防止空指针异常
			try {
				mAudioTrack.setStereoVolume(r, r);
				nRet = mAudioTrack.write(data, 0, size);
			} catch (Exception e) {

			}
		}
		return nRet;
	}

	@Override
	public void close() {
		if (mAudioTrack != null) {
			if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
				mAudioTrack.stop();
			}
			mAudioTrack.flush();
			mAudioTrack.release();
			mAudioTrack = null;
		}
	}
}
