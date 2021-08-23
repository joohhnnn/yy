package com.txznet.txz.jni;

import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.txz.jni.data.UIData;
import com.txznet.txz.module.ModuleManager;

import android.util.Log;

/**
 * native接口类，实现与native通讯
 * 
 * @author bihongpi
 *
 */
public class CommJNI {
	public static String m_testIp = "";// 192.168.0.120";

	// 启动和停止
	static native boolean start();

	static native boolean stop();

	// 日志接口
	public final static int LOG_DEBUG = Log.DEBUG;
	public final static int LOG_INFO = Log.INFO;
	public final static int LOG_WARN = Log.WARN;
	public final static int LOG_ERROR = Log.ERROR;
	public final static int LOG_FATAL = Log.ASSERT;

	static native int log(String module, int level, String tag, String content);
	static native int logWithPid(int pid, long tid, String module, int level, String tag, String content);

	// 设置控制台日志等级
	static native int setConsoleLogLevel(int level);

	//设置文件日志等级
	static native int setFileLogLevel(int level);

	// 注册和注销事件
	static native int regEvent(int eventId);

	static native int regSubEvent(int eventId, int subEventId);

	static native int unregEvent(int eventId);

	static native int unregSubEvent(int eventId, int subEventId);

	static native int doReport(int type, byte[] jsonData);
	
	static native boolean doVoiceReport(int type, long voiceId, byte[] jsonData);

	// 获取数据
	protected static native byte[] getNativeData(int dataId, byte[] param);

	public static byte[] getUIData(int dataId, byte[] param) {
		return UIData.getUIData(dataId, param);
	}

	public static native int sendEvent(int eventId, int subEventId, byte[] data);

	
	private static boolean mThreadNameNeedSet = true;
	
	public static int onEvent(int eventId, int subEventId, byte[] data) {
		if (mThreadNameNeedSet) {
			Thread.currentThread().setName("TXZNativeMessageQueue");
			mThreadNameNeedSet = false;
		}
		return ModuleManager.getInstance().onEvent(eventId, subEventId, data);
	}

	// 应该不会执行到，因为现在jni回调java只是简单的post到主线程
	static void unCaughtException(Throwable t) {
		// JNIHelper.logd("ExcetionOnJniCallback\n" + t.getMessage() + " "
		// + Log.getStackTraceString(t));
		CrashCommonHandler.getInstance().uncaughtException(
				Thread.currentThread(), t);
	}

}
