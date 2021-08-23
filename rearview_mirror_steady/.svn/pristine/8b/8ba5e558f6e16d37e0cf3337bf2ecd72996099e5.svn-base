package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;

public class AsrUtil {
	private static final String TAG = "AsrUtil";
	/**
	 * 成功
	 */
	public static final int ERROR_SUCCESS = 0;
	/**
	 * 取消操作
	 */
	public static final int ERROR_CANCLE = 1;
	/**
	 * 启动异常
	 */
	public static final int ERROR_ABORT = 2;
	/**
	 * 未知错误
	 */
	public static final int ERROR_CODE = -1;

	public static final int ERROR_NO_MATCH = -2; // 没有匹配的接口
	public static final int ERROR_NO_SPEECH = -3; // 没有说话
	public static final int ERROR_ASR_ISBUSY = -4;// 语音识别正在进行中
	
	private static TXZAsrManager.IAsrCallback mAsrCallBack = null;
	private static AsrOption asrOption = null;

	private static IAsrRegCmdCallBack mAsrRegCmdCallBack = null;

	public abstract static class IAsrRegCmdCallBack {
		public abstract void notify(String text, byte[] data);
	}

	/**
	 * 识别回调接口
	 * 
	 * @author ASUS User
	 *
	 */
	public abstract static class IAsrCallback {
		public abstract void onSuccess(String data);

		public abstract void onError(int error);

		public abstract void onAbort(int error);

		public abstract void onCancel();

		/**
		 * 开始录音
		 */
		public abstract void onStart();

		/**
		 * 录音结束，开始使用在线或离线识别
		 */
		public abstract void onEnd();

		public abstract void onVolume(int volume);
	}

	public static class AsrOption {
		/**
		 * 识别方式
		 * 
		 * @author txz
		 *
		 */
		public static enum AsrType {
			/**
			 * 自动识别模式，默认使用Auto，当出现高cpu占用并且网络可用时，自动切换为在线识别
			 */
			ASR_AUTO,
			/**
			 * 混合识别模式
			 */
			ASR_MIX,
			/**
			 * 在线
			 */
			ASR_ONLINE,
			/**
			 * 本地
			 */
			ASR_LOCAL
		}

		public Integer mId; // 回调标志，需要的话由使用方自己填写
		public Boolean mManual = true; // 手动触发标志
		public String mLanguage = "zh-cn";
		public String mAccent = "mandarin"; // 方言
		public Integer mBOS = null;
		public Integer mEOS = 700;
		public Integer mKeySpeechTimeout = 5000; // 录音总长度
		public Integer mGrammar = VoiceData.GRAMMAR_SENCE_DEFAULT; // 使用的语法场景
		public AsrType mAsrType = AsrType.ASR_AUTO;

		public AsrOption setManual(boolean Manual) {
			this.mManual = Manual;
			return this;
		}

		public AsrOption setLanguage(String Language) {
			this.mLanguage = Language;
			return this;
		}

		public AsrOption setAccent(String Accent) {
			this.mAccent = Accent;
			return this;
		}

		public AsrOption setBOS(int BOS) {
			this.mBOS = BOS;
			return this;
		}

		public AsrOption setEOS(int EOS) {
			this.mEOS = EOS;
			return this;
		}

		public AsrOption setKeySpeechTimeout(int KeySpeechTimeout) {
			this.mKeySpeechTimeout = KeySpeechTimeout;
			return this;
		}

		public AsrOption setGrammar(int Grammar) {
			this.mGrammar = Grammar;
			return this;
		}

		public AsrOption check() {
			if (this.mBOS == null) {
				if (this.mManual)
					this.mBOS = 3000;
				else
					this.mBOS = 5000;
			}
			return this;
		}
	}

	private AsrUtil() {

	}

	public static void startWithRecordWin(String hint) {
		if (TextUtils.isEmpty(hint)) {
			hint = "";
		}
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.startWithRecordWin", hint.getBytes(), null);
	}

	public static void start() {
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.start", null, null);
	}
	
	public static void startWithRawText(String rawText){
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.startWithRawText", rawText.getBytes(), null);
	}

	public static void startOnly(TXZAsrManager.IAsrCallback callBack) {
		mAsrCallBack = callBack;
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.startOnly", null, null);
	}
	
	public static void start(AsrOption option, TXZAsrManager.IAsrCallback callBack) {
		asrOption = option;
		mAsrCallBack = callBack;

		if (asrOption == null) {
			asrOption = new AsrOption();
		}
		asrOption.check();
		String asrType = null;
		switch (asrOption.mAsrType) {
		case ASR_AUTO:
			asrType = "ASR_AUTO";
			break;
		case ASR_LOCAL:
			asrType = "ASR_LOCAL";
			break;
		case ASR_ONLINE:
			asrType = "ASR_ONLINE";
			break;
		case ASR_MIX:
			asrType = "ASR_MIX";
			break;
		}
		JSONBuilder jsonData = new JSONBuilder();
		jsonData.put("ID", asrOption.mId);
		jsonData.put("BOS", asrOption.mBOS);
		jsonData.put("EOS", asrOption.mEOS);
		jsonData.put("Accent", asrOption.mAccent);
		jsonData.put("AsrType", asrType);
		jsonData.put("Grammar", asrOption.mGrammar);
		jsonData.put("KeySpeechTimeout", asrOption.mKeySpeechTimeout);
		jsonData.put("Language", asrOption.mLanguage);
		jsonData.put("Manual", asrOption.mManual);

		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.start", jsonData.toString().getBytes(), null);
	}

	public static void stop() {
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.stop", null, null);
	}

	public static void cancel() {
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.cancel", null, null);
	}

	private static Map<String, String> mMapRemoteCommands = new HashMap<String, String>();
	private static Set<String> mKeys = new HashSet<String>();
	private static Runnable mRunnableRegCmdAgain = null;

	public static void regCmd(String[] cmds, String data, IAsrRegCmdCallBack callBack) {

		synchronized (mMapRemoteCommands) {
			for (String cmd : cmds) {
				mMapRemoteCommands.put(cmd, data);
			}
		}

		mAsrRegCmdCallBack = callBack;

		JSONBuilder json = new JSONBuilder();
		json.put("cmds", cmds);
		json.put("data", data);

		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.regcmd", json.toString().getBytes(), null);

		if (mRunnableRegCmdAgain == null) {
			mRunnableRegCmdAgain = new Runnable() {
				@Override
				public void run() {
					regCmdAgain();
				}
			};
			ServiceManager.getInstance().keepConnection(TXZ, mRunnableRegCmdAgain);
		}
	}

	public static void unregCmd(String[] cmds) {
		synchronized (mMapRemoteCommands) {
			for (String cmd : cmds) {
				mMapRemoteCommands.remove(cmd);
			}
		}

		JSONBuilder json = new JSONBuilder();
		json.put("cmds", cmds);
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.unregcmd", json.toString().getBytes(), null);
	}

	public static void regCmdWithNoCmds(String key, IAsrRegCmdCallBack callBack) {

		synchronized (mKeys) {
			mKeys.add(key);
		}

		mAsrRegCmdCallBack = callBack;

		JSONBuilder json = new JSONBuilder();
		json.put("data", key);
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.regcmdwithnocmds", json.toBytes(), null);

		if (mRunnableRegCmdAgain == null) {
			mRunnableRegCmdAgain = new Runnable() {
				@Override
				public void run() {
					regCmdAgain();
				}
			};
			ServiceManager.getInstance().keepConnection(TXZ, mRunnableRegCmdAgain);
		}
	}


	public static void regCmdWithNoCmds(Set<String> keys, IAsrRegCmdCallBack callBack) {

		synchronized (mKeys) {
			mKeys.addAll(keys);
		}

		mAsrRegCmdCallBack = callBack;
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.regcmdwithnocmds", generateRegCmdJsonString(keys).getBytes(), null);

		if (mRunnableRegCmdAgain == null) {
			mRunnableRegCmdAgain = new Runnable() {
				@Override
				public void run() {
					regCmdAgain();
				}
			};
			ServiceManager.getInstance().keepConnection(TXZ, mRunnableRegCmdAgain);
		}
	}

	private static String generateRegCmdJsonString(Set<String> keys) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		JSONArray array = new JSONArray();
		for (String key : keys) {
			array.put(key);
		}
		jsonBuilder.put("dataArray", array);
		return jsonBuilder.toString();
	}

	public static void unregCmdWithNoCmds(String key) {
		synchronized (mKeys) {
			mKeys.remove(key);
		}
		JSONBuilder json = new JSONBuilder();
		json.put("data", key);
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.unregcmdwithnocmds", json.toString().getBytes(), null);
	}

	public static void unregCmdWithNoCmds(Set<String> keys) {
		synchronized (mKeys) {
			mKeys.removeAll(keys);
		}
		ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.unregcmdwithnocmds", generateRegCmdJsonString(keys).getBytes(), null);
	}




	private static Runnable mRunnableRegCmdAgainInner = new Runnable() {
		@Override
		public void run() {
			synchronized (mMapRemoteCommands) {

				Map<String, Set<String>> tempCommands = new HashMap<String, Set<String>>();
				Set<Entry<String, String>> set = mMapRemoteCommands.entrySet();
				for (Entry<String, String> entry : set) {
					String key = entry.getKey();
					String value = entry.getValue();

					Set<String> tmp = tempCommands.get(value);
					if (tmp == null) {
						tmp = new HashSet<String>();
						tempCommands.put(value, tmp);
					}
					tmp.add(key);
				}

				for (String key : tempCommands.keySet()) {
					JSONBuilder json = new JSONBuilder();
					json.put("cmds", tempCommands.get(key));
					json.put("data", key);
					ServiceManager.getInstance()
							.sendInvoke(TXZ, "comm.asr.regcmd",
									json.toString().getBytes(), null);
				}
				ServiceManager.getInstance().sendInvoke(TXZ, "comm.asr.regcmdwithnocmds", generateRegCmdJsonString(mKeys).getBytes(), null);
			}
			synchronized (mWakeupAsrMap) {
				for (Map.Entry<String, IWakeupAsrCallback> entry : mWakeupAsrMap.entrySet()) {
					useWakeupAsAsr(entry.getValue());
				}
			}
		}
	};

	/**
	 * 断开连接后重新注册所有的命令词
	 */
	public static void regCmdAgain() {
		ServiceManager.getInstance().removeOnServiceThread(mRunnableRegCmdAgainInner);
		ServiceManager.getInstance().runOnServiceThread(mRunnableRegCmdAgainInner, 100);
	}

	public static void notifyCallback(String event, byte[] data) {
		if (event.equals("regnotify")) {
			if (mAsrRegCmdCallBack != null) {
				JSONBuilder json = new JSONBuilder(data);
				mAsrRegCmdCallBack.notify(json.getVal("cmd", String.class),
						json.getVal("data", String.class).getBytes());
			}
			return;
		}

		if (mAsrCallBack == null) {
			Log.i(TAG, "mAsrCallBack == null");
			return;
		}

		if (event.equals("success")) {
			mAsrCallBack.onSuccess(new String(data));
		} else if (event.equals("cancel")) {
			mAsrCallBack.onCancel();
		} else if (event.equals("start")) {
			mAsrCallBack.onStart();
		} else if (event.equals("end")) {
			mAsrCallBack.onEnd();
		} else if (event.equals("abort")) {
			mAsrCallBack.onAbort(-1);
		} else if (event.equals("error")) {
			int err = 0;//
			try {
				err = Integer.parseInt(new String(data));
			} catch (NumberFormatException e) {
				Log.e(TAG, "convert string to int error");
			}
			mAsrCallBack.onError(err);
		}
	}

	// ///////////////////////////////////////////////////////////////////////

	public final static String[] COMMON_INDEX_KEYWORDS = new String[] { "第一个", "第二个", "第三个", "第四个", "第五个", "第六个", "第七个",
			"第八个", "第九个", "第十个" };
	public final static String[] COMMON_NEW_INDEX_KEYWORDS = new String[] { "一", "二", "三", "四", "五", "六", "七",
		"八", "九", "十" };
	public final static String[] COMMON_FISRT_KEYWORDS = new String[] { "最前面那个" };
	public final static String[] COMMON_LAST_KEYWORDS = new String[] { "最后一个", "最后那个" };

	public static String[] genSelectKeywords(int n, String[] suffix, String[] ext1, String[] ext2, String[] ext3,
			String[] ext4) {
		if (n > 10)
			n = 10;
		if (suffix == null)
			suffix = new String[] {};
		if (ext1 == null)
			ext1 = new String[] {};
		if (ext2 == null)
			ext2 = new String[] {};
		if (ext3 == null)
			ext3 = new String[] {};
		if (ext4 == null)
			ext4 = new String[] {};

		String[] ret = new String[n * (suffix.length + 2) + ext1.length + ext2.length + ext3.length + ext4.length];
		int x = 0;
		for (int i = 0; i < n; ++i) {
			ret[x++] = COMMON_INDEX_KEYWORDS[i];
			ret[x++] = "UI_SELECT_" + (i + 1);
			for (int j = 0; j < suffix.length; ++j) {
				ret[x++] = COMMON_INDEX_KEYWORDS[i] + suffix[j];
			}
		}
		for (int i = 0; i < ext1.length; ++i)
			ret[x++] = ext1[i];
		for (int i = 0; i < ext2.length; ++i)
			ret[x++] = ext2[i];
		for (int i = 0; i < ext3.length; ++i)
			ret[x++] = ext3[i];
		for (int i = 0; i < ext4.length; ++i)
			ret[x++] = ext4[i];

		return ret;
	}

	/**
	 * 默认的唤醒优先级
	 */
	public static final int WKASR_PRIORITY_DEFAULT = 0;
	/**
	 * 唤醒时暂时屏蔽免唤醒命令字，note:只有当needAsrState为true时才有作用
	 * @deprecated use WKASR_PRIORITY_NO_INSTANT_WK instead
	 */
	public static final int WKASR_PRIORITY_NO_ASR = 1;
	public static final int WKASR_PRIORITY_NO_INSTANT_WK = 1;
	/**
	 *  只用唤醒，不使用识别功能
	 */
	public static final int WKASR_PRIORITY_WK_NO_ASR = 2;
	
	public static abstract class IWakeupAsrCallback {
		boolean mIsWakeupResult = true;
		boolean mIsFromCore = true;
		
		public void setIsFromCore(boolean fromCore) {
			mIsFromCore = fromCore;
		}
		public boolean isFromCore() {
			return mIsFromCore;
		}

		public void setIsWakeupResult(boolean b) {
			mIsWakeupResult = b;
		}

		public boolean isWakeupResult() {
			return mIsWakeupResult;
		}

		public abstract boolean needAsrState();

		public void onVolume(int volume) {

		}
		public void onSpeechBegin(){
		}
		public void onSpeechEnd(){
		}

		public boolean onAsrResult(String text) {
			return false;
		}

		public String[] genKeywords() {
			return new String[] {};
		}

		public String needTts() {
			return null;
		}

		public void onTtsBegin() {
		}
		
		public void onTtsEnd() {
		}
		
		public int getPriority() {
			return WKASR_PRIORITY_DEFAULT;
		}

		public abstract String getTaskId();
	}

	public static abstract class AsrConfirmCallback extends IWakeupAsrCallback {
		String[] mSureCmds = new String[] {};
		String[] mCancelCmds = new String[] {};

		public AsrConfirmCallback(String[] mSureCmds, String[] mCancelCmds) {
			this.mSureCmds = mSureCmds;
			this.mCancelCmds = mCancelCmds;
		}

		@Override
		public boolean onAsrResult(String text) {
			for (int i = 0; i < mSureCmds.length; ++i) {
				if (text.equals(mSureCmds[i])) {
					onSure();
					return true;
				}
			}
			for (int i = 0; i < mCancelCmds.length; ++i) {
				if (text.equals(mCancelCmds[i])) {
					onCancel();
					return true;
				}
			}
			return false;
		}

		@Override
		public String[] genKeywords() {
			return genSelectKeywords(0, null, mSureCmds, mCancelCmds, null, null);
		}

		public abstract void onSure();

		public abstract void onCancel();
	}

	public static abstract class AsrSelectCallback extends IWakeupAsrCallback {
		int mCount = 0;
		String[] mSuffix = null;
		String[] mSureCmds = null;
		String[] mCancelCmds = null;
		String[] mFristCmds = null;
		String[] mLastCmds = null;

		public AsrSelectCallback(int n) {
			mCount = n;
		}

		public AsrSelectCallback setSuffix(String[] cmds) {
			mSuffix = cmds;
			return this;
		}

		public AsrSelectCallback setSureCmds(String[] cmds) {
			mSureCmds = cmds;
			return this;
		}

		public AsrSelectCallback setCancelCmds(String[] cmds) {
			mCancelCmds = cmds;
			return this;
		}

		public AsrSelectCallback setFristCmds(String[] cmds) {
			mFristCmds = cmds;
			return this;
		}

		public AsrSelectCallback setFristCmds() {
			mFristCmds = COMMON_FISRT_KEYWORDS;
			return this;
		}

		public AsrSelectCallback setLastCmds(String[] cmds) {
			mLastCmds = cmds;
			return this;
		}

		public AsrSelectCallback setLastCmds() {
			mLastCmds = COMMON_LAST_KEYWORDS;
			return this;
		}

		@Override
		public boolean onAsrResult(String text) {
			if (mSureCmds != null)
				for (int i = 0; i < mSureCmds.length; ++i) {
					if (text.equals(mSureCmds[i])) {
						onSure();
						return true;
					}
				}
			if (mCancelCmds != null)
				for (int i = 0; i < mCancelCmds.length; ++i) {
					if (text.equals(mCancelCmds[i])) {
						onCancel();
						return true;
					}
				}
			for (int i = 0; i < mCount; ++i) {
				if (text.endsWith(COMMON_INDEX_KEYWORDS[i])) {
					onSelectResult(i, COMMON_INDEX_KEYWORDS[i]);
					return true;
				}
			}

			if (mCount > 1) {
				if (mFristCmds != null)
					for (int i = 0; i < mFristCmds.length; ++i) {
						if (text.equals(mFristCmds[i])) {
							onSelectResult(0, mFristCmds[i]);
							return true;
						}
					}
				if (mLastCmds != null)
					for (int i = 0; i < mLastCmds.length; ++i) {
						if (text.equals(mLastCmds[i])) {
							onSelectResult(mCount - 1, mLastCmds[i]);
							return true;
						}
					}
			}
			return false;
		}

		@Override
		public String[] genKeywords() {
			return genSelectKeywords(mCount, mSuffix, mSureCmds, mCancelCmds, mFristCmds, mLastCmds);
		}

		public void onSure() {
			LogUtil.loge("onSure no implement");
		}

		public void onCancel() {
			LogUtil.loge("onCancel no implement");
		}

		public abstract void onSelectResult(int n, String idxStr);
	}

	public static abstract class AsrComplexSelectCallback extends IWakeupAsrCallback {
		private SparseArray<Set<String>> mMapSelectIndex = new SparseArray<Set<String>>();
		private Map<String, Set<String>> mMapSelectCommand = new HashMap<String, Set<String>>();

		private void addCommandToSet(Set<String> cmdSet, String cmd) {
			LogUtil.logd("wakeup add asr command: " + cmd);
			if (TextUtils.isDigitsOnly(cmd) && cmd.length() < 3) {
				// 电话号码可能插入3-4长度的纯数字
				LogUtil.logd("skip the only digit wakeup add asr command: " + cmd);
				return;
			}
			cmdSet.add(cmd);
		}

		private void addCommandToSet(Set<String> cmdSet, String[] cmds) {
			for (String cmd : cmds)
				addCommandToSet(cmdSet, cmd);
		}

		public AsrComplexSelectCallback addCommand(String type, String... cmds) {
			Set<String> cmdSet = mMapSelectCommand.get(type);
			if (cmdSet == null)
				mMapSelectCommand.put(type, cmdSet = new HashSet<String>());
			addCommandToSet(cmdSet, cmds);
			return this;
		}

		public AsrComplexSelectCallback addIndex(int index, String... cmds) {
			// TODO 去掉个数限制，用于当列表大于限制数时，不能插关键字的情况
//			if (index < 0 || index > 9)
//				return this;
			Set<String> cmdSet = mMapSelectIndex.get(index);
			if (cmdSet == null)
				mMapSelectIndex.append(index, cmdSet = new HashSet<String>());
			addCommandToSet(cmdSet, cmds);
			return this;
		}

		@Override
		public boolean onAsrResult(String text) {
			for (Entry<String, Set<String>> entry : mMapSelectCommand.entrySet()) {
				Set<String> cmdSet = entry.getValue();
				for (String cmd : cmdSet) {
					if (cmd.equals(text)) {
						onCommandSelected(entry.getKey(), text);
						return true;
					}
				}
			}
			List<Integer> indexs = new ArrayList<Integer>();
			for (int i = 0; i < mMapSelectIndex.size(); ++i) {
				Set<String> cmdSet = mMapSelectIndex.valueAt(i);
				for (String cmd : cmdSet) {
					if (cmd.equals(text)) {
						indexs.add(mMapSelectIndex.keyAt(i));
						break;
					}
				}
			}
			if (!indexs.isEmpty()) {
				onIndexSelected(indexs, text);
				return true;
			}
			return false;
		}

		@Override
		public String[] genKeywords() {
			Set<String> setKeywords = new HashSet<String>();
			for (Entry<String, Set<String>> entry : mMapSelectCommand.entrySet()) {
				setKeywords.addAll(entry.getValue());
			}
			for (int i = 0; i < mMapSelectIndex.size(); ++i) {
				setKeywords.addAll(mMapSelectIndex.valueAt(i));
			}
			String[] ret = new String[setKeywords.size()];
			setKeywords.toArray(ret);
			return ret;
		}

		public void onIndexSelected(List<Integer> indexs, String command) {
			LogUtil.loge("onIndexSelected no implement");
		}

		public void onCommandSelected(String type, String command) {
			LogUtil.loge("onCommandSelected no implement");
		}
	}

	static Map<String, IWakeupAsrCallback> mWakeupAsrMap = new ConcurrentHashMap<String, IWakeupAsrCallback>();

	public static void notifyWakeupAsrResult(String data) {
		JSONBuilder json = new JSONBuilder(data);
		String taskId = json.getVal("taskId", String.class);
		IWakeupAsrCallback cb = mWakeupAsrMap.get(taskId);
		if (cb != null) {
			cb.setIsFromCore(false);
			cb.setIsWakeupResult(json.getVal("isWakeupResult", Boolean.class, true));
			cb.onAsrResult(json.getVal("text", String.class));
		}
	}

	public static void notifyOnTtsBeginResult(String data) {
		JSONBuilder json = new JSONBuilder(data);
		String taskId = json.getVal("taskId", String.class);
		IWakeupAsrCallback cb = mWakeupAsrMap.get(taskId);
		if (cb != null) {
			cb.onTtsBegin();
		}
	}
	
	public static void notifyOnTtsEndResult(String data) {
		JSONBuilder json = new JSONBuilder(data);
		String taskId = json.getVal("taskId", String.class);
		IWakeupAsrCallback cb = mWakeupAsrMap.get(taskId);
		if (cb != null) {
			cb.onTtsEnd();
		}
	}

	public static void notifyonSpeechBeginResult(String data) {
		JSONBuilder json = new JSONBuilder(data);
		String taskId = json.getVal("taskId", String.class);
		IWakeupAsrCallback cb = mWakeupAsrMap.get(taskId);
		if (cb != null) {
			cb.onSpeechBegin();
		}
	}

	public static void notifyonSpeechEndResult(String data) {
		JSONBuilder json = new JSONBuilder(data);
		String taskId = json.getVal("taskId", String.class);
		IWakeupAsrCallback cb = mWakeupAsrMap.get(taskId);
		if (cb != null) {
			cb.onSpeechEnd();
		}
	}

	public static void useWakeupAsAsr(AsrUtil.IWakeupAsrCallback callback) {
		synchronized (mWakeupAsrMap) {
			mWakeupAsrMap.put(callback.getTaskId(), callback);
		}

		JSONBuilder json = new JSONBuilder();
		json.put("cmds", callback.genKeywords());
		String tts = callback.needTts();
		if (tts != null)
			json.put("tts", tts);
		json.put("state", callback.needAsrState());
		json.put("taskId", callback.getTaskId());
		json.put("priority", callback.getPriority());
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.useWakeupAsAsr", json.toBytes(), null);
	}

	public static void recoverWakeupFromAsr(String taskId) {
		synchronized (mWakeupAsrMap) {
			mWakeupAsrMap.remove(taskId);
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.recoverWakeupFromAsr", taskId.getBytes(),
				null);
	}

	public static void openRecordWinLock() {
//		try {
//			File f = new File(Environment.getExternalStorageDirectory().getPath(), ".txz_record_win_lock");
//
//			f.createNewFile();
//			f.deleteOnExit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static void closeRecordWinLock() {
//		try {
//			File f = new File(Environment.getExternalStorageDirectory().getPath(), ".txz_record_win_lock");
//			f.delete();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

//	public static boolean isRecordWinOpen() {
//		try {
//			File f = new File(Environment.getExternalStorageDirectory().getPath(), ".txz_record_win_lock");
//			return f.exists();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

	/**
	 * 设置识别录音文件路径
	 * 
	 * @param rawAudioPath
	 * @return
	 */
	public static void setAutoAsrRawAudio(String rawAudioPath, boolean isPlay) {
		if (rawAudioPath == null) {
			return;
		}
		JSONObject json = new JSONObject();
		try {
			json.put("audioSourcePath", rawAudioPath);
			json.put("isPaly", isPlay);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.set.rawaudio", json.toString().getBytes(),
				null);
	}
	
	public static void addPluginCommandProcessor(){
		PluginManager.addCommandProcessor("comm.asr.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if("start".equals(command)){
						start();
					}else if("stop".equals(command)){
						stop();
					}else if("cancel".equals(command)){
						cancel();
					}else if("startWithRawText".equals(command)){
						if(!(args[0] instanceof String)){
							return null;
						}
						String rawText = (String) args[0];
						startWithRawText(rawText);
					}
				} catch (Exception e) {
				}
				return null;
			}
		});
	}
	
	
}
