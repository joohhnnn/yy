package com.txznet.txz.component.tts.yunzhisheng_3_0;

import android.util.Log;

import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;

public class SysAudioTrack implements ITxzAudioTrack {
	private TXZAudioTrack mAudioTrack = null;

	@Override
	public int open(int streamType, int sampleRateInHz, int channelConfig,
			int audioFormat, int mode) {
		int bufferSizeInBytes = TXZAudioTrack.getMinBufferSize(sampleRateInHz,
				channelConfig, audioFormat);
		int buffersize = sampleRateInHz * 2 / 10; // 100ms单声道16位音频需要多少字节
		if (bufferSizeInBytes <= buffersize) {
			bufferSizeInBytes = bufferSizeInBytes * 2;
		} else if (bufferSizeInBytes <= buffersize * 2) {
			bufferSizeInBytes = buffersize * 2;
		}
		//如果上次没有释放，本次就会释放上一次的资源，否则Do Nothing
		//open 和 close没有加锁，因为上层调用的地方加锁了
		if (mAudioTrack != null){
			JNIHelper.logd("mAudioRecord is not NULL, it is necessary to release the source not closed last time");
			close();
		}
		
		try {
			mAudioTrack = TXZAudioTrack.createAudioTrack(streamType, sampleRateInHz,
					channelConfig, audioFormat, bufferSizeInBytes, mode);
			// mAudioTrack = new TXZAudioTrack(streamType, sampleRateInHz,
			// 		channelConfig, audioFormat, bufferSizeInBytes, mode);
			if (mAudioTrack.getState() == TXZAudioTrack.STATE_INITIALIZED) {
				mAudioTrack.play();
				JNIHelper.logd("SysAudioTrack open success");
				return 0;
			}
		} catch (Exception e) {
			JNIHelper.loge("SysAudioTrack exception : " + e.toString());
		}
		JNIHelper.loge("SysAudioTrack open fail");
		return -1;
	}

	@Override
	public int write(byte[] data, int size) {
		int nRet = 0;
		if (mAudioTrack != null) {
			float r = TtsManager.getInstance().getVolumeRate();
			// 防止空指针异常
			try {
				mAudioTrack.setStereoVolume(r, r);
				nRet = mAudioTrack.write(data, 0, size);
			} catch (Exception e) {
				JNIHelper.loge("SysAudioTrack exception : " + e.toString());
			}
		}
		if (nRet < 0){
			Log.e("SysAudioTrack", "SysAudioTrack write ocurr error nRet = " + nRet);//调用LogCat打印,避免错误的时候, 打印过多,阻塞TXZ消息队列
		}
		return nRet;
	}

	@Override
	public void close() {
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == TXZAudioTrack.PLAYSTATE_PLAYING) {
				try {
					mAudioTrack.stop();
					JNIHelper.logd("SysAudioTrack close :  stop");
				} catch (Exception e) {
					JNIHelper.logd("SysAudioTrack close :  exception : " + e.toString());
				}
			}
//			mAudioTrack.flush();
			mAudioTrack.release();
			mAudioTrack = null;
			JNIHelper.logd("SysAudioTrack close :  release");
		}
	}
}
