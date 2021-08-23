package com.txznet.txz.component.text.txz;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.TestResp;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txz.version.VersionManager.NewVersionInfo;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.util.runnables.Runnable1;

public class TextTxzHighLocalImpl implements IText {
	// 高优先级处理掩码
	private final int WAKEUP_WORK_MASK = 0x1;
	private final int REMOTE_WORK_MASK = 0x2;
	private final int LOCAL_WORK_MASK = 0x4;
	private final int REMOTE_OVER_MASK = WAKEUP_WORK_MASK | REMOTE_WORK_MASK;
	private final int WORK_MASK = 0x7;

	private ITextCallBack mCallback;
	private boolean isEnd;
	private int mStatue;
	private TestResp mTestResp;
	private int mSessionId;
	private Vector<Thread> mThreads;
	private VoiceParseData mParseData;
	private static final int DATA_ID_TEST_BEGIN = UiData.DATA_ID_TEST_WAKEUP;
	private static final int DATA_ID_TEST_END = UiData.DATA_ID_TEST_LOCAL_REGULAR;

	private static final String MONITOR_ERROR_BEGIN = "text.txzcmd.E.";
	private static final String MONITOR_INFO_BEGIN = "text.txzcmd.I.";

	@Override
	public int initialize(IInitCallback oRun) {
		mSessionId = 0;
		mThreads = new Vector<Thread>(DATA_ID_TEST_END
				- DATA_ID_TEST_BEGIN);
		oRun.onInit(true);
		return 0;
	}
	public static String getRealText(String strText) {
		String text = "";
		String expr = "^(?:(?:我要|我想要|我想)|(?:(?:请|麻烦)?(?:帮我|你替我|替我|你给我|给我|帮忙|帮)))?(?:(..+?))$";
		Pattern p = Pattern.compile(expr);
		Matcher m = p.matcher(strText);
		if (m.find()) { // Find each match in turn; String can't do this.
			text = m.group(1); // Access a submatch group; String can't do this.
		}
		return text;
	}
	@Override
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack) {
		mCallback = callBack;
		mSessionId++;
		isEnd = false;
		mTestResp = null;
		mThreads.removeAllElements();
		mStatue = 0;
		if (parseData == null || TextUtils.isEmpty(parseData.strText)) {
			mTestResp = new TestResp();
			mTestResp.success = false;
			mTestResp.type = DATA_ID_TEST_BEGIN;
			onResult(callBack, mTestResp, mPriority);
//			callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
			MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
					+ TxzErrorInputNULLStr);
			return 0;
		}
		String text = parseData.strText;//getRealText(parseData.strText);
		if (text.isEmpty()) {
			mTestResp = new TestResp();
			mTestResp.success = false;
			mTestResp.type = DATA_ID_TEST_BEGIN;
			onResult(callBack, mTestResp, mPriority);
//			callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
			return 0;
		}
		try {
			mParseData = VoiceParseData.parseFrom(VoiceParseData.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}

		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN
				+ ALL);

		for (int i = DATA_ID_TEST_BEGIN; i < DATA_ID_TEST_END; i++) {
			LocalHandleThread thread = null;
			if (i == UiData.DATA_ID_TEST_WAKEUP)
			thread = new LocalHandleThread(i, parseData.strText,
					mSessionId);
			else
			thread = new LocalHandleThread(i, text,
					mSessionId);
			thread.start();
			mThreads.add(thread);
		}
		return 0;
	}
	
	@Override
	public int setText(String text, ITextCallBack callBack) {
		mCallback = callBack;
		mSessionId++;
		isEnd = false;
		mTestResp = null;
		mThreads.removeAllElements();
		mStatue = 0;
		mParseData = new VoiceParseData();
		mParseData.strText = text;

		MonitorUtil.monitorCumulant(MONITOR_INFO_BEGIN
				+ ALL);

		for (int i = DATA_ID_TEST_BEGIN; i < DATA_ID_TEST_END; i++) {
			LocalHandleThread thread = new LocalHandleThread(i, text,
					mSessionId);
			thread.start();
			mThreads.add(thread);
		}
		return 0;
	}

	@Override
	public void cancel() {
		isEnd = true;
		JNIHelper.logd("cancel!");
		for (int i = 0; i < mThreads.size(); i++) {
			mThreads.get(i).interrupt();
		}
	}

	@Override
	public void release() {
		mThreads.removeAllElements();
	}

	private void handleLocalResult(TestResp resp, ITextCallBack callBack, int id) {
		if (mSessionId != id || isEnd)
			return;
		JNIHelper.logd("handleLocal,success=" + resp.success + ",type="
				+ resp.type);
		switch (resp.type) {
		case UiData.DATA_ID_TEST_WAKEUP:
			mStatue |= WAKEUP_WORK_MASK;
			// 没有命中远程时考虑使用唤醒词
			if (resp.success && (mTestResp == null || mTestResp.type != UiData.DATA_ID_TEST_REMOTE_KEYWORD))
				mTestResp = resp;
			break;
		case UiData.DATA_ID_TEST_REMOTE_KEYWORD:
			mStatue |= REMOTE_WORK_MASK;
			// 命中远程命令字立刻返回请求，优先级最高
			if (resp.success)
				mTestResp = resp;
			break;
		case UiData.DATA_ID_TEST_LOCAL_KEYWORD:
			mStatue |= LOCAL_WORK_MASK;
			// 只有其他关键字还没命中时才考虑使用本地命令字
			if (resp.success && mTestResp == null)
				mTestResp = resp;
			break;
		default:
			break;
		}
		handleStatue(callBack);
	}

	private void handleStatue(ITextCallBack callBack) {
		JNIHelper.logd("handleStatue,mStatue="+mStatue+",mTestResp=null?"+(mTestResp == null));
		/*if ((mStatue & WAKEUP_WORK_MASK) == WAKEUP_WORK_MASK) {
			if (mTestResp != null
					&& mTestResp.type == UiData.DATA_ID_TEST_WAKEUP) {
//				callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
				onResult(callBack, mTestResp, mPriority);
				isEnd = true;
				return;
			}
		}*/
		if ((mStatue & REMOTE_WORK_MASK) == REMOTE_WORK_MASK) {
			// 只要命中了远程命令字及使用远程命令字
			if (mTestResp != null && mTestResp.type == UiData.DATA_ID_TEST_REMOTE_KEYWORD) {
				onResult(callBack, mTestResp, mPriority);
				isEnd = true;
				return;
			}
		}
		if ((mStatue & REMOTE_OVER_MASK) == REMOTE_OVER_MASK) {
			if (mTestResp != null
					&& mTestResp.type == UiData.DATA_ID_TEST_WAKEUP) {
//				callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
				onResult(callBack, mTestResp, mPriority);
				isEnd = true;
				return;
			}
		}
		if ((mStatue & WORK_MASK) == WORK_MASK) {
			if (mTestResp == null) {
				mTestResp = new TestResp();
				mTestResp.success = false;
				mTestResp.type = DATA_ID_TEST_BEGIN;
//				callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
				onResult(callBack, mTestResp, mPriority);
			}
			else
				onResult(callBack, mTestResp, mPriority);
//				callBack.onResult(TestResp.toByteArray(mTestResp),mPriority);
			isEnd = true;
		}
	}
	public void onResult(ITextCallBack callBack,TestResp resp,int priority) {
		if (SimManager.getInstance().mAsrDelay != -1) {
			SystemClock.sleep(SimManager.getInstance().mAsrDelay * 1000);
		}
		if (!resp.success) {
			MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
					+ TxzErrorDataNullStr);
			callBack.onResult(TestResp.toByteArray(resp), priority);
			return ;
		}
		if (mParseData == null)
			mParseData = new VoiceParseData();
		JSONObject object = new JSONObject();
		mParseData.floatTextScore = TextResultHandle.TEXT_SCORE_MAX; 
		object.put("event",resp.event);
		object.put("subEvent", resp.subEvent);
//		object.put("data", new String(resp.data));
		object.put("scene","command");
		object.put("action", "event");
		object.put("text", mParseData.strText);
		object.put("type", resp.type);
		try {
			if (resp.type == UiData.DATA_ID_TEST_REMOTE_KEYWORD) {
				VoiceData.RmtCmdInfo rmtInfo = VoiceData.RmtCmdInfo.parseFrom(resp.data);
				object.put("cmd", rmtInfo.rmtCmd);
				object.put("data", rmtInfo.rmtData);
				
				object.put("service", rmtInfo.rmtServName);
			}
			else if (resp.event == UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW) {
				VoiceData.CmdWord cmdWord = VoiceData.CmdWord.parseFrom(resp.data);
				object.put("cmd", cmdWord.cmdData);
				object.put("string", cmdWord.stringData);
				object.put("voice", cmdWord.voiceData);
			}
			else if (resp.type == UiData.DATA_ID_TEST_WAKEUP) {
				String cmd = new String(resp.data);
				object.put("cmd", cmd);
				if (!TextUtils.isEmpty(cmd)) {
					mParseData.strText = cmd;
					object.put("text", cmd);
					JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RECORD_SHOW_USER_TEXT, cmd);
				}
			} else {
				object.put("cmd", new String(resp.data));
			}
		} catch (InvalidProtocolBufferNanoException e) {
			MonitorUtil.monitorCumulant(MONITOR_ERROR_BEGIN
					+ TxzErrorUnknownStr);
		}
		mParseData.strVoiceData = object.toJSONString();
		JNIHelper.logd("nlp:onLocalResult");
		callBack.onResult(mParseData, priority);
	}

	class LocalHandleThread extends Thread {
		int mType;
		String mText;
		boolean isCancel;
		int mId;

		public LocalHandleThread(int type, String text, int id) {
			mType = type;
			mText = text;
			isCancel = false;
			mId = id;
		}

		@Override
		public void run() {
			JNIHelper.logd("nlp:onLocalParse");
			UiData.Test_Keyword testKeyword = new UiData.Test_Keyword();
			testKeyword.strText = mText;
			testKeyword.id = mId;
			byte[] result = NativeData.getNativeData(mType, testKeyword);
			try {
				TestResp resp = TestResp.parseFrom(result);
				AppLogic.runOnBackGround(new Runnable1<TestResp>(resp) {
					@Override
					public void run() {
						if (!isCancel)
							handleLocalResult(mP1, mCallback, mId);
					}
				}, 0);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			super.run();
		}

		@Override
		public void interrupt() {
			isCancel = true;
			super.interrupt();
		}
	}
	private int mPriority = PRIORITY_LEVEL_LOCAL_HIGH;

	@Override
	public void setPriority(int priority) {
		mPriority = priority;
	}
	@Override
	public int getPriority() {
		return mPriority;
	}

}
