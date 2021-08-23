package com.txznet.audio.codec;

public class TXZAudioDecoder implements ITXZAudioDecoder {

	/**
	 * 创建解码器
	 * @param obj 回调
	 * @return
	 */
	public native long createDecoder(Object obj);

	/**
	 * @param sessionId 指针，创建时回调参数
	 * @return
	 */
	public native int destoryDecoder(long sessionId);

	/**
	 * 阻塞时
	 * @param sessionId
	 * @param path
	 * @return
	 */
	public native int startDecoder(long sessionId, String path);

	/**
	 * @param sessionId
	 * @return
	 */
	public native int stopDecoder(long sessionId);

	/**
	 * @param sessionId
	 * @param params
	 * @param data
	 * @param offset 默认0
	 * @return size
	 */
	public native int readDecoder(long sessionId, int[] params, byte[] data, int offset);

	/**
	 * 谨慎使用
	 * @param sessionId
	 * @param seekTime
	 * @param seekPosition
	 * @return
	 */
	public native int seekDecoder(long sessionId, long seekTime,long seekPosition);
	
	@Override
	public void onGetDuration(long duration) {

	}

	static {
		System.loadLibrary("TXZCoreCodec");
	}
}
