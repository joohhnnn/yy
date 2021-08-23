package com.txznet.txz.module.record;

import java.io.FileOutputStream;
import java.io.IOException;
import android.text.TextUtils;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.yunzhisheng_3_0.TxzAudioSourceImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.MP3Encoder;

public class Recorder {
	static {
		System.loadLibrary("mp3lame");
	}

	private long mSession = MP3Encoder.INVALID_SESSION_ID;
	private String mSavePathPrefix;
	FileOutputStream mp3Stream = null;

	public Recorder() {

	}

	public Recorder(String savePathPrefix) {
		mSavePathPrefix = savePathPrefix;
		mSession = MP3Encoder.openSession(TxzAudioSourceImpl.FREQUENCY_16K);
		if (!TextUtils.isEmpty(mSavePathPrefix)) {
			try {
				mp3Stream = new FileOutputStream(mSavePathPrefix + ".mp3");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void write(byte[] data, int len) {
		if (len <= 0 || null == data) {
			return;
		}
		final int shortLen = (len + 1) / 2;
		final short[] buffer = new short[shortLen];
		for (int i = 0; i < shortLen; i++) {
			int j = 2 * i;
			// 小端模式 低字节存放于低地址
			// 与或等按位操作都是先将操作数转为int,多出的高位上的值与原来的变量的最高位一样
			buffer[i] = 0;
			if (j + 1 > len - 1) {
				buffer[i] = (short) (data[j] & 0xff);
			} else {
				buffer[i] = (short) (((data[j + 1] << 8) & 0xff00) | (data[j] & 0xff));
			}
		}

		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				encode(buffer, shortLen);
			}
		};
		AppLogic.runOnUiGround(oRun, 0);
	}

	private void encode(short[] buffer, int lenInShort) {
		if (null == mp3Stream) {
			return;
		}

		byte[] encodeData;
		if (mSession != MP3Encoder.INVALID_SESSION_ID) {
			encodeData = MP3Encoder.encodeSession(mSession, buffer, 0, lenInShort);
			if (mp3Stream != null) {
				try {
					mp3Stream.write(encodeData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public void close() {
		JNIHelper.logd("RecordText");
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				closeInner();
			}
		};

		AppLogic.runOnUiGround(oRun, 0);
	}

	private void closeInner() {
		if (null == mp3Stream) {
			return;
		}
		mSavePathPrefix = null;
		byte[] encodeData;
		if (mSession != MP3Encoder.INVALID_SESSION_ID) {
			encodeData = MP3Encoder.closeSession(mSession);
			if (mp3Stream != null) {
				try {
					mp3Stream.write(encodeData);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mSession = MP3Encoder.INVALID_SESSION_ID;
		}

		if (null != mp3Stream) {
			try {
				mp3Stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mp3Stream = null;
			}
		}
	}

}
