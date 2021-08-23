//package com.txznet.audio.codec;
//
///**
// * 直播解码
// *
// * @author ASUS User
// *
// */
//public class CopyOfTXZAudioDirectDecoder {
//
//	/**
//	 * 创建解码器
//	 *
//	 * @return 回话ID
//	 */
//	public static native long createDecoder();
//
//	/**
//	 * 解码数据
//	 *
//	 * @param sessionID
//	 * @param obj 通过反射为该类成员赋值
//	 * @param path
//	 *            解码路径
//	 * @return
//	 */
//	public static native int beginDecode(long sessionID, Object obj, String path);
//
//	/**
//	 *
//	 * @param sessionID
//	 * @param params
//	 * @param data
//	 * @param offset
//	 * @return 读取的字节数 或者0（没有读取到）， 或者负数（出错）
//	 */
//	public static native int readFrame(long sessionID, int[] params,
//			byte[] data, int offset);
//
//	/**
//	 * 开始释放，通知JNI释放资源
//	 *
//	 * @param sessionID
//	 * @return
//	 */
//	public static native int beginRelease(long sessionID);
//
//	public static native int seek(long sessionID, long time);
//
//	/**
//	 * 释放全部资源
//	 *
//	 * @param sessionID
//	 * @return
//	 */
//	public static native int destory(long sessionID);
//	/**
//	 * 释放网络资源
//	 *
//	 * @param sessionID
//	 * @return
//	 */
//	public static native int releaseNet(long sessionID);
//
//
//
//	static {
//		System.loadLibrary("avcodec-56");
//		System.loadLibrary("avdevice-56");
//		System.loadLibrary("avfilter-5");
//		System.loadLibrary("avformat-56");
//		System.loadLibrary("avutil-54");
//		System.loadLibrary("postproc-53");
//		System.loadLibrary("swresample-1");
//		System.loadLibrary("swscale-3");
//		System.loadLibrary("sffdecoder");
//	}
//
//}
