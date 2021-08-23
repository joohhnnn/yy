package com.txznet.txz.component.tts;

import java.util.Locale;

import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;

public interface ITts {
	/**
	 * 成功
	 */
	public static final int ERROR_SUCCESS = 0;
	/**
	 * 取消
	 */
	public static final int ERROR_CANCLE = 1;
	/**
	 * 未知错误
	 */
	public static final int ERROR_UNKNOW = 2;

	/**
	 * 超时
	 */
	public static final int ERROR_TIMEOUT = 3;
	
	public static class TTSOption{
		public Integer mPlayStartBufferTime = null;
	}

	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public int initialize(final IInitCallback oRun);

	public void release();

	public int start(int iStream, String sText, ITtsCallback oRun);

	public int pause();

	public int resume();

	public void stop();

	public boolean isBusy();
	
    public void setTtsModel(String ttsModel);
	
	public int setLanguage(final Locale loc);
	
	public void setVoiceSpeed(int speed);
	
	public int getVoiceSpeed();
	
	public void setOption(TTSOption oOption);
}
