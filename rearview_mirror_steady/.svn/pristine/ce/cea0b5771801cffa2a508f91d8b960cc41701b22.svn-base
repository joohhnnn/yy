package com.txznet.txz.component.nav;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeySource;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZAsrKeyManager.AsrSources;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.n.INavAsr;
import com.txznet.txz.component.nav.n.INavHighLevel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.content.Intent;
import android.text.TextUtils;

public abstract class NavThirdComplexApp extends NavThirdApp implements INavHighLevel, INavHighLevelInterface, INavAsr {
	public static final String NAV_BACKGROUND = "com.txznet.nav.autoamap.onpause";
	public static final String NAV_FRONTGROUND = "com.txznet.nav.autoamap.onresume";

	// 当前是否属于经由地导航
	protected boolean mIsJingYouPlan = false;
	// 路径规划失败后是否提醒
	protected boolean mSpeechAfterPlan = false;
	// 是否允许唤醒退出导航
	protected boolean enableWakeupExitNav = true;
	// 路径规划失败提示文本
	protected String mPlanFailSpeechText = "";
	protected List<Runnable> mPlanEnds = new ArrayList<Runnable>();

	// 外部禁用的类型
	protected Set<String> mBanCmds = new HashSet<String>();
	// 命令字支持的类型
	protected Set<String> mCmdTypes = new HashSet<String>();

	private Map<String, String[]> mThirdModifyMap = new HashMap<String, String[]>();

	public NavThirdComplexApp() {
		mNavCmdControl = new NavCmdControl();
	}

	public void setSpeechTTSText(String speechText) {
		mPlanFailSpeechText = speechText;
	}

	public void setSpeechAfterPlan(boolean isSpeech) {
		mSpeechAfterPlan = isSpeech;
	}

	Intent broadIntent = null;

	@Override
	public void broadNaviInfo(String navJson) {
		if (broadIntent == null) {
			broadIntent = new Intent(NAVI_INFO_ACTION);
		}
		broadIntent.putExtra(EXTRA_KEY_NAVI_INFO, navJson);
		GlobalContext.get().sendBroadcast(broadIntent);
	}

	@Override
	public void setPackageName(String packageName) {
		mRemotePackageName = packageName;
	}

	@Override
	public boolean procJingYouPoi(Poi... pois) {
		return false;
	}

	@Override
	public String disableProcJingYouPoi() {
		return "";
	}

	public void addPlanEndRunnable(Runnable run) {
		synchronized (mPlanEnds) {
			mPlanEnds.add(run);
		}
	}

	public void notifyPlanEnd() {
		synchronized (mPlanEnds) {
			for (Runnable run : mPlanEnds) {
				run.run();
			}

			mPlanEnds.clear();
		}
	}

	@Override
	public void setBanCmds(String... cmds) {
		if (mBanCmds == null) {
			mBanCmds = new HashSet<String>();
		}

		mBanCmds.clear();
		for (String cmd : cmds) {
			mBanCmds.add(cmd);
		}

		if (mNavCmdControl != null) {
			mNavCmdControl.updateCmds();
		}
	}

	public void addBanCmds(String... cmds) {
		if (mBanCmds == null) {
			mBanCmds = new HashSet<String>();
		}
		for (String cmd : cmds) {
			mBanCmds.add(cmd);
		}
	}

	public abstract List<String> getBanCmds();

	public abstract String[] getSupportCmds();

	public List<String> getCmdNavOnly() {
		return null;
	}
	
	@Override
	public void onStart() {
		JNIHelper.logd("onStart");
		if (NavManager.getInstance().hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(NavManager.getInstance().getStatusListenerServiceName(),
					"status.nav.start", getPackageName().getBytes(), null);
		}
		
		mIsStarted = true;
		checkInNav();
	}
	
	@Override
	public void onEnd(boolean arrive) {
		JNIHelper.logd("onEnd");
		if (NavManager.getInstance().hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(NavManager.getInstance().getStatusListenerServiceName(),
					"status.nav.end", getPackageName().getBytes(), null);
		}
		mIsPlaned = false;
		mIsStarted = false;
		checkInNav();
	}
	
	@Override
	public void onPlanComplete() {
		JNIHelper.logd("onPlanComplete");
		notifyPlanEnd();
//		mIsStarted = true;
		mIsPlaned = true;
		mSpeechAfterPlan = false;
		checkInNav();
	}
	
	@Override
	public void onPlanError(int errCode, String errDesc) {
		JNIHelper.logd("onPlanError");
		mIsPlaned = false;
//		mIsStarted = false;
		checkInNav();
		if (mSpeechAfterPlan) {
			mSpeechAfterPlan = false;
			if (TextUtils.isEmpty(mPlanFailSpeechText)) {
				mPlanFailSpeechText = NativeData.getResString("RS_MAP_PATH_FAIL");
			}
			TtsManager.getInstance().speakText(mPlanFailSpeechText);
		}
	}
	
	@Override
	public void onResume() {
		JNIHelper.logd("onResume");
		if (NavManager.getInstance().hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(NavManager.getInstance().getStatusListenerServiceName(),
					"status.nav.foreground", getPackageName().getBytes(), null);
		}
		broadAmapGround(true);
		mIsFocus = true;
		checkInNav();
	}

	@Override
	public void onPause() {
		JNIHelper.logd("onPause");
		if (NavManager.getInstance().hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(NavManager.getInstance().getStatusListenerServiceName(),
					"status.nav.background", getPackageName().getBytes(), null);
		}
		broadAmapGround(false);
		mIsFocus = false;
		checkInNav();
	}

	private void broadAmapGround(boolean isShow) {
		String action = "com.txznet.nav.autoamap.onpause";
		if (isShow) {
			action = "com.txznet.nav.autoamap.onresume";
		}
		Intent intent = new Intent(action);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public Set<String> getCmdTypes() {
		Set<String> cmds = new HashSet<String>();

		List<String> banCmds = getBanCmds();
		String[] allCmds = getSupportCmds();
		if (allCmds == null) {
			return cmds;
		}

		boolean registerAll = needSeparateNavStatus() ? false : true;
		for (String cmd : allCmds) {
			if (banCmds != null && banCmds.contains(cmd)) {
				continue;
			}

			if (mBanCmds != null && mBanCmds.contains(cmd)) {
				continue;
			}

			if (!registerAll) {
				List<String> navCmds = getCmdNavOnly();
				if (!mIsStarted && navCmds != null) {
					if (navCmds.contains(cmd)) {
						continue;
					}
				}
			}
			cmds.add(cmd);
		}

		return cmds;
	}

	@Override
	public void addStatusListener() {
	}

	@Override
	public void removeBanCmds(String... types) {
		for (int i = 0; i < mBanCmds.size(); i++) {
			for (String t : types) {
				if (mBanCmds.contains(t)) {
					mBanCmds.remove(t);
					break;
				}
			}
		}
	}

	@Override
	public boolean needSeparateNavStatus() {
		return true;
	}

	@Override
	public boolean banNavWakeup() {
		return false;
	}

	public void onCommTypeSelect(boolean isWakeup, String resId, Runnable task) {
		String tts = NativeData.getResString(resId);
		if (!TextUtils.isEmpty(tts) && !tts.contains("已为您") && !tts.contains("将为您")) {
			if (isWakeup) {
				tts = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", tts);
			} else {
				tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
			}
		}

		if (isWakeup) {
			if (task != null) {
				task.run();
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(tts, null);
		} else {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(tts, task);
		}
	}

	public void onCommTypeSelect(String hintTts, Runnable pR, Runnable aR) {
		if (pR != null) {
			pR.run();
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(hintTts, aR);
	}

	protected String[] getConfirmRes(String resId, String speech, String prefix) {
		String res = NativeData.getResString(resId);
		String sure = NativeData.getResString("RS_MAP_CONFIRM_SURE_ASR");
		String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
		String spk = NativeData.getResString("RS_MAP_DIALOG_HINT").replace("%COMMAND%", res);
		return new String[] { spk, prefix + res, sure, cancel };
	}

	public void onDialogShow(final String[] resArray, final Runnable endRun) {
		if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
			return;
		}

		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mWinConfirmAsr = createWinConfirmAsr(resArray, "DIALOG", endRun);
				mWinConfirmAsr.show();
			}
		}, 0);
	}

	public String[] getConfirmDialogRes(String speech, String resId) {
		String resComm = null;
		try {
			resComm = NativeData.getResString(resId);
		} catch (Exception e1) {
			if (resComm == null) {
				resComm = speech;
			}
		}

		try {
			String hint = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK");
			hint = hint.replace("%COMMAND%", resComm);
			String okHint = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK");
			okHint = okHint.replace("%COMMAND%", resComm);
			String sure = NativeData.getResString("RS_MAP_CONFIRM_SURE_ASR");
			String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
			return new String[] { hint, okHint, sure, cancel };
		} catch (Exception e) {
			String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%", speech);
			String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%", speech);
			return new String[] { hintSpk, sureSpk, "确定", "取消" };
		}
	}

	public String[] getExitDialogRes(String speech) {
		String hint = "";
		String okHint = "";
		String sure = "";
		String cancel = "";
		try {
			hint = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT");
			hint = hint.replace("%COMMAND%", speech);
			okHint = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE");
			okHint = okHint.replace("%COMMAND%", speech);
			sure = NativeData.getResString("RS_MAP_CONFIRM_SURE_ASR");
			cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
		} catch (Exception e) {
			hint = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%", speech);
			okHint = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%", speech);
			sure = "确定";
			cancel = "取消";
		}

		return new String[] { hint, okHint, sure, cancel };
	}

	public void onConfirmDialogShow(final String speech, final String resId, final Runnable run) {
		if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
			return;
		}

		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				// resId为空定为退出导航
				String[] resStr = null;
				if (TextUtils.isEmpty(resId)) {
					resStr = getExitDialogRes(speech);
				} else {
					resStr = getConfirmDialogRes(speech, resId);
				}
				final String[] res = resStr;

				createWinConfirmAsr(res, resId, run).show();
			}
		}, 0);
	}

	public WinConfirmAsr createWinConfirmAsr(final String[] res, final String resId, final Runnable run) {
		return mWinConfirmAsr = new WinConfirmAsr() {

			@Override
			public void onClickOk() {
				if (TextUtils.isEmpty(resId)) {
					TtsManager.getInstance().speakText(res[1], new ITtsCallback() {

						@Override
						public void onEnd() {
							run.run();
						}
					});
					return;
				}

				TtsManager.getInstance().speakText(res[1]);
				if (run != null) {
					run.run();
				}
			}

			@Override
			public void onClickCancel() {
			}

		}.setSureText(res[2], new String[] { res[2], res[2] + res[2] })
				.setCancelText(res[3], new String[] { res[3], res[3] + res[3] }).setHintTts(res[0]).setMessage(res[0]);
	}

	@Override
	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("enableWakeupExit")) {
			enableWakeupExitNav = Boolean.parseBoolean(new String(data));
			JNIHelper.logd("enableWakeupExit:" + enableWakeupExitNav);
		}
		if (command.equals("forbidKeys")) {
			String json = new String(data);
			if (!TextUtils.isEmpty(json)) {
				JNIHelper.logd("forbigKeys:" + json);
				String[] arrays = json.split(",");
				if (arrays != null) {
					setBanCmds(arrays);
					AppLogic.removeBackGroundCallback(mCheckRegisterAgain);
					AppLogic.runOnBackGround(mCheckRegisterAgain, 100);
				}
			}
			return null;
		}
		if ("syncKeySources".equals(command)) {

		}
		if ("modify".equals(command)) {
			AsrSources as = AsrSources.assign(data);
			List<AsrKeySource> akss = as.getAsrKeySources();
			if (akss != null) {
				for (AsrKeySource aks : akss) {
					JNIHelper.logd("modify:" + aks.getKeyType());
					// JSONBuilder jb = new JSONBuilder();
					// jb.put(AsrKeyType.NAV_RES_PREFIX + aks.getKeyType(),
					// aks.getKeyCmds());
					// ResourceManager.getInstance().invokeTXZResource(null,
					// "updateResource", jb.toBytes());
					mThirdModifyMap.put(aks.getKeyType(), aks.getKeyCmds());
				}
			}

			AppLogic.removeBackGroundCallback(mCheckRegisterAgain);
			AppLogic.runOnBackGround(mCheckRegisterAgain, 100);
		}
		if ("forceRegister".equals(command)) {
			mIsForceRegister = Boolean.parseBoolean(new String(data));
			JNIHelper.logd("forceRegisterCommand:" + mIsForceRegister);
			NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
			if (nta != null && nta.getPackageName().equals(getPackageName())) {
				AppLogic.removeBackGroundCallback(mCheckRegisterAgain);
				AppLogic.runOnBackGround(mCheckRegisterAgain, 100);
			}
		}
		if (command.equals("enablecmd")) {
			JSONBuilder jb = new JSONBuilder(data);
			mIsRegisterWakeup = jb.getVal("enableCmd", Boolean.class);
			JNIHelper.logd("mIsRegisterWakeup:" + mIsRegisterWakeup);
			AppLogic.removeBackGroundCallback(mCheckRegisterAgain);
			AppLogic.runOnBackGround(mCheckRegisterAgain, 100);
			return null;
		}
		if (command.equals("enableWakeupNav")) {
			mIsRegisterWakeup = Boolean.parseBoolean(new String(data));
			JNIHelper.logd("mIsRegisterWakeup:" + mIsRegisterWakeup);

			AppLogic.removeBackGroundCallback(mCheckRegisterAgain);
			AppLogic.runOnBackGround(mCheckRegisterAgain, 100);
			return null;
		}
		return super.invokeTXZNav(packageName, command, data);
	}

	Runnable mCheckRegisterAgain = new Runnable() {

		@Override
		public void run() {
			if (mNavCmdControl != null) {
				mNavCmdControl.unRegisterCmds();
				mNavCmdControl.checkRegisterCmds();
			}
		}
	};

	boolean mLastIsStartNav;

	protected void checkInNav() {
		if (mLastIsStartNav != mIsStarted) {
			mLastIsStartNav = mIsStarted;
			mNavCmdControl.resetSession();
		}

		mNavCmdControl.checkRegisterCmds();
	}

	// 是否强制注册
	protected boolean mIsForceRegister;
	// 是否注册为唤醒
	protected boolean mIsRegisterWakeup = true;
	// 导航命令注册器
	protected NavCmdControl mNavCmdControl;

	protected IAsrRegCmdCallBack mICCB = new IAsrRegCmdCallBack() {

		@Override
		public void notify(String text, byte[] data) {
			try {
			if (AsrKeyType.BACK_HOME.equals(new String(data))) {
				RecorderWin.cancelClose();
				NavManager.getInstance().NavigateHome();
				return;
			}

			if (AsrKeyType.GO_COMPANY.equals(new String(data))) {
				RecorderWin.cancelClose();
				NavManager.getInstance().NavigateCompany();
				return;
			}

				onNavCommand(false, new String(data), text);
			} catch (Exception e) {
				JNIHelper.loge(e.toString());
			}
		}
	};

	public class NavCmdControl {
		// 是否已经注册过
		boolean mHasRegister;
		boolean mSession;
		boolean mIsUpdating;
		List<String> mRegistCmds = new ArrayList<String>();
		Map<String, String[]> mCmds = new HashMap<String, String[]>();

		public NavCmdControl() {
		}

		Runnable mRegisterRunnable = new Runnable() {

			@Override
			public void run() {
				if (banNavWakeup()) {
					JNIHelper.logw("has ban all cmds！");
					return;
				}
				if (mIsUpdating) {
					JNIHelper.logw("mIsUpdating is true");
					return;
				}
				JNIHelper.logd("forceRegister:" + mIsForceRegister + ",mHasRegister:" + mHasRegister + ",mIsFocus:"
						+ mIsFocus + ",mIsUpdating:" + mIsUpdating + ",mIsStarted:" + mIsStarted + ",mIsRegisterWakeup:"
						+ mIsRegisterWakeup);

				if (mIsForceRegister) {
					if (mHasRegister) {
						return;
					}

					mHasRegister = true;
					regNavCommand(mIsRegisterWakeup);
					return;
				}

				if (mIsFocus) {
					if (mHasRegister) {
						return;
					}
					mHasRegister = true;
					regNavCommand(mIsRegisterWakeup);
				} else {
					if (mHasRegister) {
						mHasRegister = false;
						unRegisterCmds();
					}
				}
			}
		};

		public void resetSession() {
			JNIHelper.logd("resetSession");
			mSession = false;
			mHasRegister = false;
		}

		public void checkRegisterCmds() {
			if (!mSession) {
				mSession = true;
				updateCmds();
			}

			AppLogic.removeBackGroundCallback(mRegisterRunnable);
			AppLogic.runOnBackGround(mRegisterRunnable, 100);
		}

		private void regNavCommand(boolean isWakeup) {
			if (isWakeup) {
				AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

					@Override
					public boolean needAsrState() {
						return false;
					}

					@Override
					public String getTaskId() {
						return "CTRL_NAV$" + getPackageName();
					}

					@Override
					public void onCommandSelected(String type, String command) {
						try {
						if (AsrKeyType.BACK_HOME.equals(type)) {
							NavManager.getInstance().NavigateHome();
							return;
						}

						if (AsrKeyType.GO_COMPANY.equals(type)) {
							NavManager.getInstance().NavigateCompany();
							return;
						}

							onNavCommand(isWakeupResult(), type, command);
						} catch (Exception e) {
							JNIHelper.loge(e.toString());
						}
					}
				};

				Set<Entry<String, String[]>> sets = mCmds.entrySet();
				int count = onWakeupRegister(acsc);
				if (sets.size() < 1 && count < 1) {
					return;
				}

				for (Entry<String, String[]> entry : sets) {
					acsc.addCommand(entry.getKey(), entry.getValue());
				}

				WakeupManager.getInstance().useWakeupAsAsr(acsc);
			} else {
				if (mRegistCmds != null && mRegistCmds.size() > 0) {
					AsrUtil.unregCmd(mRegistCmds.toArray(new String[mRegistCmds.size()]));
				}
				mRegistCmds.clear();
				Set<Entry<String, String[]>> sets = mCmds.entrySet();
				for (Entry<String, String[]> entry : sets) {
					String[] cmds = entry.getValue();
					if (entry.getKey().equals(AsrKeyType.BACK_HOME) 
							|| entry.getKey().equals(AsrKeyType.GO_COMPANY)) {
						continue;
					}
					
					AsrUtil.regCmd(cmds, entry.getKey(), mICCB);
					for (String val : cmds) {
						mRegistCmds.add(val);
					}
				}
				mRegistCmds.addAll(onAsrCmdsRegister());
			}
		}

		public void unRegisterCmds() {
			mHasRegister = false;
			JNIHelper.logd("unRegisterCmds");
			WakeupManager.getInstance().recoverWakeupFromAsr("CTRL_NAV$" + getPackageName());
			if (mRegistCmds != null && mRegistCmds.size() > 0) {
				AsrUtil.unregCmd(mRegistCmds.toArray(new String[mRegistCmds.size()]));
			}

			onUnRegisterCmds();
			updateCmds();
		}

		/**
		 * 更新一下最新的可注册命令
		 */
		private void updateCmds() {
			if (mIsUpdating) {
				JNIHelper.logd("isupdating ...");
				return;
			}

			mIsUpdating = true;
			JNIHelper.logd("start update");
			Set<String> cmds = getCmdTypes();
			mCmds.clear();

			List<String> keys = new ArrayList<String>();
			for (String type : cmds) {
				JNIHelper.loge("updateCmds:" + type);
				// 预先选用被外界修改的唤醒词
				String[] cds = mThirdModifyMap.get(type);
				if (cds != null) {
					mCmds.put(type, cds);
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
					keys.add(s);
					++i;
				}

				if (keys.size() > 0) {
					mCmds.put(type, keys.toArray(new String[keys.size()]));
				} else {
					JNIHelper.loge("type:" + type + ", is empty cmds！");
				}
			}

			mIsUpdating = false;
			JNIHelper.logd("end update");
		}
	}

	protected int onWakeupRegister(AsrComplexSelectCallback acsc) {
		return 0;
	}

	// 第三方修改的唤醒词
	protected void addWakeupCommand(AsrComplexSelectCallback acsc, String type, String... cmds) {
		String[] cmd = mThirdModifyMap.get(type);
		if (cmd == null) {
			acsc.addCommand(type, cmds);
		} else {
			acsc.addCommand(type, cmd);
		}
	}

	protected Collection<String> onAsrCmdsRegister() {
		return new ArrayList<String>();
	}

	protected void onUnRegisterCmds() {

	}
}
