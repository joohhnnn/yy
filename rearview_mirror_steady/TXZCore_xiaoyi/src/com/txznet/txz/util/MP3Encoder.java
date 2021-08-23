package com.txznet.txz.util;

public class MP3Encoder {
	public static native String getVersion();

	// 文件转换
	public static native int pcm2mp3(String pcmFile, String mp3File, int sample);

	// 转换会话
	public static final long INVALID_SESSION_ID = 0;
	
	public static native long openSession(int sample);

	public static native byte[] closeSession(long session);

	public static native byte[] encodeSession(long session, short[] input, int start, int len);
}
