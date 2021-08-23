package com.txznet.txz.jni;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.google.protobuf.nano.MessageNano;
import com.txz.report_manager.ReportManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.version.TXZVersion;
import com.txznet.txz.module.version.VersionManager;

/**
 * JNI辅助调用工具，统一日志封装，初始化及接口保护
 * 
 * @author bihongpi
 *
 */
public class JNIHelper {

	// ////////////////////////////////////////////////////////////

	public static boolean mInited = false;

	// //////////////////////////////////////////////////////////////////////////////

	private static Integer mConsoleLogLevel = null;
	private static Integer mFileLogLevel = null;

	/**
	 * 加载动态库
	 */
	public static void initNativeLibrary() {
		// logd("meminfo: " + SystemInfo.getMemInfo());
		System.loadLibrary("txzComm");

		ModuleManager.getInstance().setEventHelper(new JNIHelper());
		mInited = true;

		if (mConsoleLogLevel != null) {
			setConsoleLogLevel(mConsoleLogLevel);
		}
		if (mFileLogLevel != null) {
			setFileLogLevel(mFileLogLevel);
		}
		// logd("meminfo: " + SystemInfo.getMemInfo());

		logd(VersionManager.getInstance().getUserVersionNumber()
				+ " VersionInfo : " + NativeData.getVersion() + " # " + "UI_"
				+ TXZVersion.PACKTIME + "_" + TXZVersion.COMPUTER + "_"
				+ TXZVersion.SVNVERSION);

		synchronized (mEventList) {
			for (EventRecord rec : mEventList) {
				CommJNI.sendEvent(rec.eventId, rec.subEventId, rec.data);
			}
			mEventList.clear();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////

	static StackTraceElement mStackTraceElement = null;

	/**
	 * 记录日志栈
	 * 
	 * @param depth
	 *            栈深度，0表示当前当前调用位置，1表示上一级
	 */
	public static void recordLogStack(int depth) {
		if (mNeedStack) {
			mStackTraceElement = new Throwable().getStackTrace()[depth + 1];
		}
	}

	public static int _logRaw(String module, int level, String tag,
			String content) {
		if (mInited)
			return CommJNI.log(module, level, tag, content);
		if (LogUtil.needConsoleLog(level)) {
			return Log.println(level, tag, content);
		}
		return 0;
	}
	
	public static int _logRaw(int pid, long tid, String module, int level, String tag,
			String content) {
		if (mInited)
			return CommJNI.logWithPid(pid, tid, module, level, tag, content);
		if (LogUtil.needConsoleLog(level)) {
			return Log.println(level, tag, content);
		}
		return 0;
	}

	// 设置控制台日志等级
	public static int setConsoleLogLevel(int level) {
		mConsoleLogLevel = level;
		if (mInited) {
			if (DebugCfg.ENABLE_LOG)
				return CommJNI.setConsoleLogLevel(Log.VERBOSE);
			else
				return CommJNI.setConsoleLogLevel(level);
		}
		return 0;
	}

	// 设置文件日志等级
	public static int setFileLogLevel(int level) {
		mFileLogLevel = level;
		if (mInited) {
			if (DebugCfg.ENABLE_LOG)
				return CommJNI.setFileLogLevel(Log.DEBUG);
			else
				return CommJNI.setFileLogLevel(level);
		}
		return 0;
	}

	private static boolean mNeedStack = ProjectCfg.class.getName().equals(
			"com.txznet.txz.cfg.ProjectCfg");

	public static int log(int level, String content) {
		String module = ""; // TXZ的模块名留空，底层就会打日志到logcat
		String tag = "TXZ";
		try {
			if (mNeedStack) {
				if (null == mStackTraceElement)
					recordLogStack(1);

				tag = mStackTraceElement.getClassName() + "::"
						+ mStackTraceElement.getMethodName();
				// int n = tag.lastIndexOf('.');
				// if (n >= 0)
				// tag = tag.substring(n + 1);
				// String prefix = "[" + tag + "::" +
				// mStackTraceElement.getMethodName() + "]";
				String suffix = "[" + mStackTraceElement.getFileName() + ":"
						+ mStackTraceElement.getLineNumber() + "]";
				content = content + suffix;
			}
		} catch (Exception e) {
		} finally {
			mStackTraceElement = null;
		}
		return _logRaw(module, level, tag, content);
	}

	public static int logd(String content) {
		if (null == mStackTraceElement)
			recordLogStack(1);
		return log(CommJNI.LOG_DEBUG, content);
	}

	public static int logi(String content) {
		if (null == mStackTraceElement)
			recordLogStack(1);
		return log(CommJNI.LOG_INFO, content);
	}

	public static int logw(String content) {
		if (null == mStackTraceElement)
			recordLogStack(1);
		return log(CommJNI.LOG_WARN, content);
	}

	public static int loge(String content) {
		if (null == mStackTraceElement)
			recordLogStack(1);
		return log(CommJNI.LOG_ERROR, content);
	}

	public static int logf(String content) {
		if (null == mStackTraceElement)
			recordLogStack(1);
		return log(CommJNI.LOG_FATAL, content);
	}

	// //////////////////////////////////////////////////////////////////////////////

	public static int doReport(int type, byte[] jsonData) {
		if (!mInited)
			return 0;
		if (DebugCfg.DISABLE_REPORT_ACTION || jsonData == null){
			return 0;
		}
		if(DebugCfg.ENABLE_REPORT_LOG){
			LogUtil.logd("doReport " + new String(jsonData));
		}
		return CommJNI.doReport(type, jsonData);
	}

	public static int doReport(int type, String json) {
		return doReport(type, json.getBytes());
	}

	public static int doReport(int type, JSONObject jsonContent) {
		String s = jsonContent.toString();
		return doReport(type, s);
	}

	public static int doReport(int type, Bundle bundle) {
		JSONObject jsonContent = new JSONObject();

		Set<String> keySet = bundle.keySet(); // 获取所有的Key,
		for (String key : keySet) {
			try {
				jsonContent.put(key, bundle.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return doReport(type, jsonContent);
	}
	
	public static boolean doReportImmediate(int type, byte[] json) {
		if(!mInited){
			return false;
		}
		if (DebugCfg.DISABLE_REPORT_ACTION || json == null){
			return true;
		}
		if(DebugCfg.ENABLE_REPORT_LOG){
			LogUtil.logd("doReportImmediate " + new String(json));
		}
		ReportManager.UserAction pbUserAction = new ReportManager.UserAction();
		pbUserAction.uint32Type = type;
		pbUserAction.strJsonData = json;
		sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REPORT_USERACTION_IMMEDIATE, pbUserAction);
		return true;
	}
	
	public static boolean doVoiceReport(int recognitionType, long voiceId, byte[] jsonData) {
	    if(!mInited){
	        return false;
	    }
		if (DebugCfg.DISABLE_REPORT_CORPUS || jsonData == null){
			return true;
		}
		if(DebugCfg.ENABLE_REPORT_LOG){
			LogUtil.logd("doVoiceReport " + new String(jsonData));
		}
	    return CommJNI.doVoiceReport(recognitionType, voiceId, jsonData);
	}

	// //////////////////////////////////////////////////////////////////////////////

	public static int monitor(int type, int val, String[] attrs) {
		if (!mInited)
			return 0;
		ReportManager.Req_ReportDeviceLogicErrorInfo err = new ReportManager.Req_ReportDeviceLogicErrorInfo();
		err.rptMsgDeviceLogicErrorInfo = new ReportManager.DeviceLogicErrorInfo[attrs.length];
		for (int i = 0; i < attrs.length; ++i) {
			err.rptMsgDeviceLogicErrorInfo[i] = new ReportManager.DeviceLogicErrorInfo();
			err.rptMsgDeviceLogicErrorInfo[i].strAttribute = attrs[i];
			err.rptMsgDeviceLogicErrorInfo[i].uint32VarType = type;
			err.rptMsgDeviceLogicErrorInfo[i].uint32Val = val;
		}
		return sendEvent(UiEvent.EVENT_MONITOR, 0, err);
	}

	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * 启动native框架
	 * 
	 * @return
	 */
	public static boolean start() {
		if (!mInited)
			return false;
		return CommJNI.start();
	}

	/**
	 * 停止native框架
	 * 
	 * @return
	 */
	public static boolean stop() {
		if (!mInited)
			return false;
		return CommJNI.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////

	public int _regEvent(int eventId) {
		if (!mInited)
			return -1;
		return CommJNI.regEvent(eventId);
	}

	public int _regEvent(int eventId, int subEventId) {
		if (!mInited)
			return -1;
		return CommJNI.regSubEvent(eventId, subEventId);
	}

	public int _unregEvent(int eventId) {
		if (!mInited)
			return -1;
		return CommJNI.unregEvent(eventId);
	}

	public int _unregEvent(int eventId, int subEventId) {
		if (!mInited)
			return -1;
		return CommJNI.unregSubEvent(eventId, subEventId);
	}

	// //////////////////////////////////////////////////////////////////////////////

	// 发送事件
	public static int sendEvent(int eventId, int subEventId) {
		return sendEvent(eventId, subEventId, "");
	}

	public static int sendEvent(int eventId, int subEventId, String data) {
		return sendEvent(eventId, subEventId, data.getBytes());
	}

	public static int sendEvent(int eventId, int subEventId, MessageNano message) {
		return sendEvent(eventId, subEventId, MessageNano.toByteArray(message));
	}

	static class EventRecord {
		int eventId;
		int subEventId;
		byte[] data;

		EventRecord(int eventId, int subEventId, byte[] data) {
			this.eventId = eventId;
			this.subEventId = subEventId;
			this.data = data;
		}
	};

	static List<EventRecord> mEventList = new ArrayList<EventRecord>();

	public static int sendEvent(int eventId, int subEventId, byte[] data) {
		if (!mInited) {
			// 没初始化完成时先保存消息队列
			synchronized (mEventList) {
				mEventList.add(new EventRecord(eventId, subEventId, data));
			}
			return -1;
		}
		return CommJNI.sendEvent(eventId, subEventId, data);
	}
}
