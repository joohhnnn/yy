package com.txznet.txz.module.nav.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeySource;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZAsrKeyManager.AsrSources;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable3;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

public class NavCmdRegister {
	public static final int TYPE_ONSTART = 1;
	public static final int TYPE_ONEND = 2;
	public static final int TYPE_FOREGROUND = 3;
	public static final int TYPE_BACKGROUND = 4;
	public static final int TYPE_EXIT = 5;
	public static final int TYPE_UPDATE = 6;
	public static final int TYPE_ENABLE = 7;
	public static final int TYPE_CMD_CHANGE = 8;
	public static final int TYPE_RESTART = 9;
	public static final int TYPE_STOP_WAKEUP_ASR = 10;
	public static final int TYPE_RECOVER_WAKEUP_ASR = 11;
	public static final int TYPE_TTS_BEGIN = 12;
	public static final int TYPE_TTS_END =13;

	public static final int MARK_NAV = 1 << 0;
	public static final int MARK_FORE = 1 << 1;
	public static final int MARK_EXIT = 1 << 2;

	private class NavRegisterRecord {
		// 当前导航状态
		int navState = MARK_EXIT;
		// 工具包名
		public String navPkn;
		// 对应的工具
		NavThirdComplexApp mApp;
		// 是否注册了
		private boolean isRegistered;
		// 是否需要更新指令
		private boolean needUpdate = true;
		// 命令表
		public Map<String, String[]> regCmds = new HashMap<String, String[]>();
		// 唤醒任务引用
		private AsrComplexSelectCallback mWakeAsrCallback;

		public NavRegisterRecord(String navPkn) {
			this.navPkn = navPkn;
			mApp = (NavThirdComplexApp) NavAppManager.getInstance().getNavToolByName(navPkn);
		}

		public void clear() {
			needUpdate = true;
			navState = MARK_EXIT;
			regCmds.clear();
			unRegister();
		}

		public int flatState(int navState, int mark, boolean isClear) {
			int state = navState;
			if (isClear) {
				state &= ~mark;
			} else {
				state |= mark;
			}
			return state;
		}

		public void onNavStateChange(int navState) {
			if (this.navState == navState) {
				LogUtil.logd("same navState return！");
				return;
			}

			boolean restart = needUpdate;
			if ((this.navState & MARK_NAV) != (navState & MARK_NAV)) {
				// 导航状态发生改变的情况下需要更新唤醒词
				restart = true;
			}

			LogUtil.logd(navPkn + " state update from:" + this.navState + " to:" + navState + ",restart:" + restart);
			this.navState = navState;

			if (restart) {
				updateRegCmds();
				unRegister();
				// 更新后赋值
				needUpdate = false;
			}
			//
			register();
		}

		public void registerWithUpdate(boolean update) {
			if (update) {
				updateRegCmds();
			}

			unRegister();
			register();
		}

		public void register() {
			boolean isFocus = ((navState & MARK_FORE) == MARK_FORE) && (navState & MARK_EXIT) != MARK_EXIT;
			boolean isInNav = ((navState & MARK_NAV) == MARK_NAV);

			LogUtil.logd("mIsRegisterCmds:" + mIsRegisterCmds + ",forceRegister:" + mIsForceRegister + ",mHasRegister:"
					+ isRegistered + ",mIsFocus:" + isFocus + ",isInNav:" + isInNav + ",mIsEnableWakeup:"
					+ mIsEnableWakeup);
			HelpGuideManager.getInstance().notifyNavStatus(isFocus, isInNav);
			
			if (!mIsRegisterCmds) {
				LogUtil.loge("not register nav cmds！");
				return;
			}

			if (mIsForceRegister) {
				if (isRegistered) {
					return;
				}

				isRegistered = true;
				regNavCommand();
				return;
			}

			if (isFocus) {
				if (isRegistered) {
					return;
				}
				isRegistered = true;
				regNavCommand();
			} else {
				unRegister();
			}
		}

		private void regNavCommand() {
			if (processRemoteSence(mIsEnableWakeup ? 1 : 2, "mapCmdRegister")) {
				return;
			}

			if (NavCmdRegister.this.mIsEnableWakeup) {
				regWakeup();
			} else {
				regAsr();
			}
		}

		private void regWakeup() {
			AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

				@Override
				public boolean needAsrState() {
					return false;
				}

				@Override
				public String getTaskId() {
					return "CTRL_NAV$" + navPkn;
				}

				@Override
				public void onCommandSelected(String type, String command) {
					AppLogic.runOnBackGround(new Runnable3<Boolean, String, String>(isWakeupResult(), type, command) {

						@Override
						public void run() {
							NavCmdRegister.this.onCommandSelect(mApp, mP1, mP2, mP3, 1);
						}
					}, 0);
				}
			};

			int count = mApp.onWakeupRegister(acsc);
			synchronized (regCmds) {
				boolean isUse = count > 0;
				final List<String> wakeCmds = new ArrayList<String>();
				if (regCmds.size() > 0) {
					for (Entry<String, String[]> entry : regCmds.entrySet()) {
						String key = entry.getKey();
						String[] val = entry.getValue();
						acsc.addCommand(key, val);
						if (val != null && val.length > 0 && !AsrKeyType.START_NAVI.equals(key)) {
							LogUtil.logd("key:" + key + ",val:" + val[0]);
							wakeCmds.add(val[0]);
						}
					}

					isUse = true;
				} else {
					if (isUse) {
						String[] kws = acsc.genKeywords();
						wakeCmds.addAll(Arrays.asList(kws));
					}
				}
				if (isUse) {
					mWakeAsrCallback = acsc;
					HelpGuideManager.getInstance().notifyNavHelp(wakeCmds);
					WakeupManager.getInstance().useWakeupAsAsr(acsc);
				} else {
					acsc = null;
					mWakeAsrCallback = null;
				}
			}
		}

		private List<String> hasRegisterCmds = new ArrayList<String>();

		private IAsrRegCmdCallBack iccb = new IAsrRegCmdCallBack() {

			@Override
			public void notify(String text, byte[] data) {
				onCommandSelect(mApp, false, new String(data), text, 2);
			}
		};

		private void regAsr() {
			if (hasRegisterCmds.size() > 0) {
				AsrUtil.unregCmd(hasRegisterCmds.toArray(new String[hasRegisterCmds.size()]));
				hasRegisterCmds.clear();
			}

			synchronized (regCmds) {
				Set<Entry<String, String[]>> sets = regCmds.entrySet();
				for (Entry<String, String[]> entry : sets) {
					String[] cmds = entry.getValue();
					if (entry.getKey().equals(AsrKeyType.BACK_HOME) || entry.getKey().equals(AsrKeyType.GO_COMPANY)) {
						continue;
					}

					AsrUtil.regCmd(cmds, entry.getKey(), iccb);
					for (String val : cmds) {
						hasRegisterCmds.add(val);
					}
				}
				HelpGuideManager.getInstance().notifyNavHelp(new ArrayList<String>());

				Collection<String> colles = mApp.onAsrCmdsRegister(iccb);
				if (colles != null) {
					hasRegisterCmds.addAll(colles);
				}
			}
		}

		public void unRegister() {
			if (isRegistered) {
				LogUtil.logd("unRegisterCmds");
				isRegistered = false;
				processRemoteSence(0, "mapCmdUnRegister");
				mWakeAsrCallback = null;
				WakeupManager.getInstance().recoverWakeupFromAsr("CTRL_NAV$" + navPkn);
				if (!hasRegisterCmds.isEmpty()) {
					AsrUtil.unregCmd(hasRegisterCmds.toArray(new String[hasRegisterCmds.size()]));
					hasRegisterCmds.clear();
				}

				mApp.onUnRegisterCmds();
				HelpGuideManager.getInstance().notifyNavHelp(null);
			}
		}

		// 先反注册
		public void stopWakeAsr() {
			if (isRegistered) {
				LogUtil.logd("stopWakeAsr:" + navPkn);
				WakeupManager.getInstance().recoverWakeupFromAsr("CTRL_NAV$" + navPkn);
				if (!hasRegisterCmds.isEmpty()) {
					AsrUtil.unregCmd(hasRegisterCmds.toArray(new String[hasRegisterCmds.size()]));
					hasRegisterCmds.clear();
				}
			}
		}

		// 恢复
		public void recoverWakeAsr() {
			if (isRegistered) {
				LogUtil.logd("recoverWakeAsr:" + navPkn);
				if (NavCmdRegister.this.mIsEnableWakeup) {
					if (mWakeAsrCallback != null) {
						WakeupManager.getInstance().useWakeupAsAsr(mWakeAsrCallback);
					}
				} else {
					regAsr();
				}
			}
		}

		private void updateRegCmds() {
			NavThirdComplexApp ntca = mApp;
			if (ntca.banNavWakeup()) {
				LogUtil.logd("updateCmds has ban all cmds...");
				return;
			}

			// boolean isInNav = ntca.isInNav();
			boolean isInNav = (this.navState & MARK_NAV) == MARK_NAV;
			boolean separate = ntca.needSeparateNavStatus();
			LogUtil.logd(navPkn + " updateNavCmds isInNav:" + isInNav);

			String[] sTypes = ntca.getSupportCmds(); // 支持的所有指令
			List<String> sBanCmds = ntca.getBanCmds();// 不支持的指令
			List<String> sNavTypes = ntca.getCmdNavOnly();// 导航中支持的命令

			Set<String> tmpCmds = new HashSet<String>();
			if (sTypes != null) {
				synchronized (mBanCmds) {
					for (String type : sTypes) {
						if (sBanCmds != null && sBanCmds.contains(type)) {
							continue;
						}

						if (mBanCmds.contains(type)) {
							continue;
						}

						if (separate) {
							if (!isInNav && sNavTypes != null) {
								if (sNavTypes.contains(type)) {
									continue;
								}
							}
						}

						tmpCmds.add(type);
					}
				}

				if (!tmpCmds.isEmpty()) {
					updateCmd(tmpCmds, false);
				} else {
					LogUtil.logw("updateCmd tmpCmd is isEmpty...");
				}
			} else {
				LogUtil.loge("updateCmd support cmds is null...");
			}
		}

		private void updateCmd(Set<String> cmdTypes, boolean isNoAsr) {
			List<String> keys = new ArrayList<String>();
			Map<String, String[]> tmpCmdMap = new HashMap<String, String[]>();
			synchronized (mModifyCmdMap) {
				for (String type : cmdTypes) {
					String[] cds = mModifyCmdMap.get(type);
					if (cds != null) {
						tmpCmdMap.put(type, cds);
						continue;
					}

					int i = 0;
					keys.clear();
					for (;;) {
						String s = NativeData.getResString(AsrKeyType.NAV_RES_PREFIX + type, i);
						if (s == null || s.length() == 0) {
							s = NativeData.getResString(type, i);
						}

						if (s == null || s.length() == 0)
							break;
						++i;
						if (!isNoAsr && WakeupManager.getInstance().isSimilarAsrKeyword(s)) {
							continue;
						}
						keys.add(s);
					}

					if (!keys.isEmpty()) {
						tmpCmdMap.put(type, keys.toArray(new String[keys.size()]));
						LogUtil.logw("type:" + type + "key"+keys.toString() );
					} else {
						LogUtil.logw("type:" + type + ", is empty cmds！");
					}
				}
			}

			synchronized (regCmds) {
				regCmds.clear();
				regCmds.putAll(tmpCmdMap);
			}
		}
	}

	// 记录当前正常使用的导航包名
	private String mCurrNavPkn;
	// 是否强制注册导航唤醒（退出后不注册，后台注册）
	private boolean mIsForceRegister;
	// 是否注册为唤醒方式，false为只响应命令
	private boolean mIsEnableWakeup = true;
	// 是否注册导航命令，false不注册唤醒和指令
	private boolean mIsRegisterCmds = true;

	/** 禁用的命令 **/
	private List<String> mBanCmds = new ArrayList<String>();
	/** 修改的命令 **/
	private Map<String, String[]> mModifyCmdMap = new HashMap<String, String[]>();
	private Map<String, NavRegisterRecord> mNavRegisterMap = new HashMap<String, NavRegisterRecord>();
	/**屏蔽指令的免唤醒但保留命令字的方式**/
	private List<String> mShieldWpKeys = new ArrayList<String>();

	private Handler mHandler;
	private HandlerThread mHandlerThread;

	private void ensureHandler() {
		if (mHandler == null) {
			synchronized (this){
				if(mHandler == null){
					mHandlerThread = new HandlerThread("NavCmdRegisterThread");
					mHandlerThread.start();
					mHandler = new Handler(mHandlerThread.getLooper()) {
						@Override
						public void handleMessage(Message msg) {
							handleMsg(msg);
						}
					};
				}
			}
		}
	}

	// 同一个线程，不用加锁
	private void stopWakeupAsrInner() {
		LogUtil.logd("NavRegisterRecord stopWakeupAsrInner");
		if (mNavRegisterMap != null) {
			for (NavRegisterRecord record : mNavRegisterMap.values()) {
				if (record != null) {
					record.stopWakeAsr();
				}
			}
		}
	}

	private void recoverWakeupAsrInner() {
		LogUtil.logd("NavRegisterRecord recoverWakeupAsrInner");
		if (mNavRegisterMap != null) {
			for (NavRegisterRecord record : mNavRegisterMap.values()) {
				if (record != null) {
					record.recoverWakeAsr();
				}
			}
		}
	}

	private void handleMsg(Message msg) {
		switch (msg.what) {
		case TYPE_STOP_WAKEUP_ASR:
			stopWakeupAsrInner();
			return;
		case TYPE_RECOVER_WAKEUP_ASR:
			recoverWakeupAsrInner();
			return;
		default:
			break;
		}

		String navPkn = msg.obj != null && msg.obj instanceof String ? (String) msg.obj : "";
		NavRegisterRecord record = null;
		if (!TextUtils.isEmpty(navPkn)) {
			record = mNavRegisterMap.get(navPkn);
			if (record == null) {
				NavThirdApp navThirdApp = NavAppManager.getInstance().getNavToolByName(navPkn);
				if (navThirdApp == null || !(navThirdApp instanceof NavThirdComplexApp)) {
					LogUtil.loge(navPkn + navThirdApp + " is not super NavThirdComplexApp!");
					return;
				}

				record = new NavRegisterRecord(navPkn);
				mNavRegisterMap.put(navPkn, record);
			}
		} else if (!TextUtils.isEmpty(mCurrNavPkn)) {
			record = mNavRegisterMap.get(mCurrNavPkn);
		}

		int navState = record != null ? record.navState : MARK_EXIT;

		switch (msg.what) {
		case TYPE_ONSTART:
			if (record != null) {
				navState = record.flatState(record.navState, MARK_NAV, false);
			}
			break;
		case TYPE_ONEND:
			if (record != null) {
				navState = record.flatState(record.navState, MARK_NAV, true);
			}
			break;
		case TYPE_FOREGROUND:
			mCurrNavPkn = navPkn;
			if (record != null) {
				navState = record.flatState(record.navState, MARK_FORE, false);
				navState = record.flatState(navState, MARK_EXIT, true);
			}
			break;
		case TYPE_BACKGROUND:
			// 如果是一样的包名，则已经退到了后台
//			if (navPkn.equals(mCurrNavPkn)) {
//				mCurrNavPkn = null;
//			}

			if (record != null) {
				navState = record.flatState(record.navState, MARK_FORE, true);
			}
			break;
		case TYPE_EXIT:
			if (record != null) {
				navState = record.flatState(record.navState, MARK_EXIT, false);
				navState = record.flatState(navState, MARK_FORE, true);
				navState = record.flatState(navState, MARK_NAV, true);
			}
			break;
		case TYPE_UPDATE:
			if (record != null) {
				Bundle bundle = msg.getData();
				record.registerWithUpdate(bundle.getBoolean("isUpdate"));
			}
			return;
		case TYPE_ENABLE:
			AppLogic.removeBackGroundCallback(mEnableTask);
			AppLogic.runOnBackGround(mEnableTask, 1 * 1000);
			return;
		case TYPE_CMD_CHANGE:
			mHandler.removeMessages(TYPE_RESTART);

			msg = Message.obtain();
			msg.what = TYPE_RESTART;
			msg.obj = true;
			mHandler.sendMessageDelayed(msg, 1 * 1000);
			return;
		case TYPE_RESTART:
			boolean update = msg.obj != null && msg.obj instanceof Boolean ? (Boolean) msg.obj : false;
			if (record != null) {
				record.registerWithUpdate(update);
			}
			return;
		default:
			break;
		}

		if (record != null) {
			record.onNavStateChange(navState);
			return;
		}
	}

	Runnable mRestartUpdateTask = new Runnable() {

		@Override
		public void run() {
			Message msg = Message.obtain();
			msg.what = TYPE_CMD_CHANGE;
			mHandler.sendMessage(msg);
		}
	};

	Runnable mEnableTask = new Runnable() {

		@Override
		public void run() {
			Message message = Message.obtain();
			message.what = TYPE_RESTART;
			message.obj = false;
			mHandler.sendMessage(message);
		}
	};

	public void onStart(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_ONSTART;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onEnd(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_ONEND;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onForeground(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_FOREGROUND;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onBackground(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_BACKGROUND;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onExitApp(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_EXIT;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onStatusUpdate(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_UPDATE;
		msg.obj = navPkn;
		Bundle bundle = new Bundle();
		bundle.putBoolean("isUpdate", NavAppManager.getInstance().needUpdateCmds());
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	public void stopWakeupAsr() {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_STOP_WAKEUP_ASR;
		mHandler.sendMessage(msg);
	}

	public void recoverWakeupAsr() {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_RECOVER_WAKEUP_ASR;
		mHandler.sendMessage(msg);
	}

	public void onTtsStart(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_TTS_BEGIN;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public void onTtsEnd(String navPkn) {
		ensureHandler();
		Message msg = Message.obtain();
		msg.what = TYPE_TTS_END;
		msg.obj = navPkn;
		mHandler.sendMessage(msg);
	}

	public boolean enableWakeup() {
		return mIsEnableWakeup;
	}

	private boolean processRemoteSence(int registerType, String action) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "nav");
		jb.put("action", action);
		jb.put("registerType", registerType);
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	/**
	 * 命中后的回调
	 * 
	 * @param isWakeupResult
	 * @param type
	 * @param command
	 */
	private void onCommandSelect(NavThirdComplexApp app, boolean isWakeupResult, String type, String command,
			int eventType) {
		// 反复注册可能会消耗性能，在选择界面先不做识别响应
		boolean isSelecting = ChoiceManager.getInstance().isSelecting();
		boolean isCoexit = ChoiceManager.getInstance().isCoexistAsrAndWakeup() || InterruptTts.getInstance().isInterruptTTS();
		if (isSelecting && !isCoexit) {
			LogUtil.loge("nav onCommandSelect isSelecting");
			return;
		}

		// 反复注册可能会消耗性能，在语音聊天界面先不响应唤醒
		if (RecorderWin.isOpened() && isWakeupResult && !isCoexit) {
			LogUtil.loge("nav isWakeupResult with RecorderWin open");
			return;
		}

		if (isWakeupResult && mShieldWpKeys.contains(type)) {
			LogUtil.loge("shieldWp key:" + type);
			return;
		}

		try {
			MtjModule.getInstance().event(MtjModule.EVENTID_NAV_ABOUT);
			if (processRemoteSence(isWakeupResult, type, command, eventType)) {
				return;
			}

			if (AsrKeyType.BACK_HOME.equals(type)) {
				NavManager.getInstance().NavigateHome();
				return;
			}

			if (AsrKeyType.GO_COMPANY.equals(type)) {
				NavManager.getInstance().NavigateCompany();
				return;
			}

			// 如果地图没打开，则打开导航
			if (!app.isInFocus()) {
				app.enterNav();
			}

			app.onNavCommand(isWakeupResult, type, command);
		} catch (Exception e) {
			JNIHelper.loge(e.toString());
		}
	}

	/**
	 * 支持远程场景
	 * 
	 * @param isWakeupResult
	 * @param type
	 * @param speech
	 * @param eventType
	 * @return
	 */
	public boolean processRemoteSence(boolean isWakeupResult, String type, String speech, int eventType) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("isWakeupResult", isWakeupResult);
		jb.put("type", type);
		jb.put("speech", speech);
		jb.put("aimType", eventType);
		jb.put("action", "mapVoiceControl");
		jb.put("scene", "nav");
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	public byte[] invokeCmds(String packageName, String command, byte[] data) {
		ensureHandler();
		if ("forbidKeys".equals(command)) {
			String json = new String(data);
			if (!TextUtils.isEmpty(json)) {
				JNIHelper.logd("forbigKeys:" + json);
				String[] arrays = json.split(",");
				if (arrays != null) {
					synchronized (mBanCmds) {
						for (String key : arrays) {
							mBanCmds.add(key);
						}
					}

					AppLogic.removeBackGroundCallback(mRestartUpdateTask);
					AppLogic.runOnBackGround(mRestartUpdateTask, 1 * 1000);
				}
			}
			return null;
		}
		if ("unForbidKeys".equals(command)) {
			String json = new String(data);
			if (!TextUtils.isEmpty(json)) {
				JNIHelper.logd("unForbidKeys:" + json);
				String[] arrays = json.split(",");
				if (arrays != null) {
					synchronized (mBanCmds) {
						for (String key : arrays) {
							if (mBanCmds.contains(key)) {
								mBanCmds.remove(key);
							}
						}
					}

					AppLogic.removeBackGroundCallback(mRestartUpdateTask);
					AppLogic.runOnBackGround(mRestartUpdateTask, 1 * 1000);
				}
			}
			return null;
		}
		if ("modify".equals(command)) {
			AsrSources as = AsrSources.assign(data);
			List<AsrKeySource> akss = as.getAsrKeySources();
			if (akss != null) {
				synchronized (mModifyCmdMap) {
					for (AsrKeySource aks : akss) {
						JNIHelper.logd("modify:" + aks.getKeyType() + ",cmds:" +Arrays.toString(aks.getKeyCmds()));
						mModifyCmdMap.put(aks.getKeyType(), aks.getKeyCmds());
					}
				}

				// 刷新当前的注册
				AppLogic.removeBackGroundCallback(mRestartUpdateTask);
				AppLogic.runOnBackGround(mRestartUpdateTask, 1 * 1000);
			}
			return null;
		}
		if ("shieldWpKeys".equals(command)) {
			try {
				String json = new String(data);
				LogUtil.loge("shielWpKeys:" + json);
				if (!TextUtils.isEmpty(json)) {
					String[] keys = json.split(",");
					if (keys != null) {
						for (String key : keys) {
							mShieldWpKeys.add(key);
						}
					} else {
						mShieldWpKeys.clear();
					}
				} else {
					mShieldWpKeys.clear();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		if ("forceRegister".equals(command)) {
			mIsForceRegister = Boolean.parseBoolean(new String(data));
			LogUtil.logd("forceRegister:" + mIsForceRegister);
			// 刷新当前注册
			AppLogic.removeBackGroundCallback(mEnableTask);
			AppLogic.runOnBackGround(mEnableTask, 500);
			return null;
		}
		if ("enablecmd".equals(command)) {
			JSONBuilder jsonBuilder = new JSONBuilder(data);
			Boolean b = jsonBuilder.getVal("enableCmd", Boolean.class);
			if (b != null) {
				mIsEnableWakeup = b;
				// 刷新当前注册
				AppLogic.removeBackGroundCallback(mEnableTask);
				AppLogic.runOnBackGround(mEnableTask, 500);
			}
			LogUtil.logd("enableNavCmdWakeup:" + b);
			return null;
		}
		if ("enableWakeupNav".equals(command)) {
			mIsEnableWakeup = Boolean.parseBoolean(new String(data));
			// 刷新当前注册
			AppLogic.removeBackGroundCallback(mEnableTask);
			AppLogic.runOnBackGround(mEnableTask, 500);
			LogUtil.logd("enableNavCmdWakeup:" + mIsEnableWakeup);
			return null;
		}
		return null;
	}
}