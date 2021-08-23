package com.txznet.audio.codec;

public interface ITXZAudioDecoder {

	/**
	 * 
	 * @param obj
	 *            回调监听
	 * @see ITXZDecoderCallBack
	 * @return
	 */
	public long createDecoder(Object obj);

	public int destoryDecoder(long sessionId);

	/**
	 * 开始解码
	 */
	public int startDecoder(long sessionId, String path);

	/**
	 * 结束解码
	 */
	public int stopDecoder(long sessionId);

	public int readDecoder(long sessionId, int[] params, byte[] data, int offset);

	public int seekDecoder(long sessionId, long seekTime,long seekPosition);

	public void onGetDuration(long duration);

	public interface ITXZDecoderCallBack {

		/**
		 * 当前时长
		 * 
		 * @param duration
		 *            不一定准确（有可能解码不到duration字段，考拉aac）
		 */
		void onGetDuration(long duration);


		/**
		 * 拖动完成
		 */
		void onSeekCompleteListener(long seekTime);

	}

}
