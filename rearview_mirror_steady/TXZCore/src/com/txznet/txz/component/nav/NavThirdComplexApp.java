package com.txznet.txz.component.nav;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.dialog2.WinConfirmAsr.WinConfirmAsrBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.component.nav.n.INavHighLevel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Intent;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

public abstract class NavThirdComplexApp extends NavThirdApp implements INavHighLevel, INavHighLevelInterface{
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
		
		if (DebugCfg.ENABLE_NAV_LOG) {
			JNIHelper.logd("broadNaviInfo:" + navJson);
		}

		broadIntent.putExtra(EXTRA_KEY_NAVI_INFO, navJson);
		GlobalContext.get().sendBroadcast(broadIntent);
	}

	public NavInfo mNavInfo;
	
	/**
	 * 查询限速信息
	 * @return
	 */
	public boolean speakLimitSpeech() {
		return false;
	}
	
	/**
	 * 播报当前限速信息
	 */
	protected void queryLimitSpeed() {
		if (!isInNav() || mNavInfo == null) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_NO_SPEEDLIMIT");
			if (!isInNav()) {
				spk = NativeData.getResString("RS_MAP_LIMITSPEED_NONAV");
			}
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		long speed = mNavInfo.currentLimitedSpeed;
		if (speed < 1) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_NO_SPEEDLIMIT");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		AsrManager.getInstance().setNeedCloseRecord(true);
		String spk = NativeData.getResPlaceholderString("RS_MAP_SPEEDLIMIT", "%SPEED%", speed + "");
		RecorderWin.speakTextWithClose(spk, null);
	}

	protected boolean preNavCancelCommand(boolean isWakeupResult, String command) {
		if (!isInNav()) {
			String tts = NativeData.getResString("RS_VOICE_NO_PLAN_ROUTE");
			RecorderWin.speakText(tts, null);
			return true;
		}
		return false;
	}

	/**
	 * 播报前面怎么走
	 * @param isWakeupResult
	 */
	public void speakHowNavi(boolean isWakeupResult) {
		if (!isInNav()) {
			JNIHelper.loge("hownavi isInav false！");
			if (!isWakeupResult) {
				RecorderWin.close();
			}
			return;
		}
		
		String errHint = NativeData.getResString("RS_VOICE_NAV_INFO_ERROR");
		if (mNavInfo == null || TextUtils.isEmpty(mNavInfo.getDirectionDes())) {
			if (!TextUtils.isEmpty(errHint)) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(errHint, null);
			} else {
				JNIHelper.logd("errHint is empty！");
				RecorderWin.close();
			}
			return;
		}

		long remainDistance = mNavInfo.dirDistance;
		String dirTxt = mNavInfo.getDirectionDes();
		String distance = getRemainDistance(remainDistance);

		String nextRoad = mNavInfo.nextRoadName;
		String hint = NativeData.getResPlaceholderString("RS_MAP_FRONT", "%DISTANCE%", distance + dirTxt);
		if (!TextUtils.isEmpty(nextRoad)) {
			hint = NativeData.getResString("RS_MAP_FRONT_INTO").replace("%DISTANCE%", distance + dirTxt)
					.replace("%ROAD%", nextRoad);
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(hint, null);
	}
	
	/**
	 * 播报还有多久
	 * @param isWakeupResult
	 */
	public void speakAskRemain(boolean isWakeupResult) {
		if (!isInNav() || mNavInfo == null) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose("", null);
			return;
		}

		Long remainTime = mNavInfo.remainTime;
		Long remainDistance = mNavInfo.remainDistance;

		String rt = getRemainTime(remainTime);
		String rd = getRemainDistance(remainDistance);
		String hint = "";
		if (TextUtils.isEmpty(rt) && TextUtils.isEmpty(rd)) {
			hint = "";
		}
		if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
			hint = NativeData.getResString("RS_MAP_DESTINATION_ABOUT").replace("%DISTANCE%", rd).replace("%TIME%", rt);
		} else if (!TextUtils.isEmpty(rd)) {
			hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_DIS", "%DISTANCE%", rd);
		} else if (!TextUtils.isEmpty(rt)) {
			hint = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_TIME", "%TIME%", rt);
		}

		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(hint, null);
	}

	protected List<Poi> jyList = null;

	@Override
	@CallSuper
	public boolean procJingYouPoi(Poi... pois) {
		if (isInNav()) {
			if (jyList == null) {
				jyList = new ArrayList<Poi>();
			}
			jyList.add(pois[0]);
		}
		return false;
	}

	@Override
	public List<Poi> getJingYouPois() {
		if (!isInNav() && jyList != null && jyList.size() > 0) {
            jyList.clear();
        }
		return jyList;
	}

	@Override
	public boolean deleteJingYou(Poi poi) {
		if (jyList != null && !jyList.isEmpty()) {
			jyList.remove(poi);
		}
		return false;
	}

	@Override
	public String disableDeleteJingYou() {
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_DEL_JINGYOU");
	}

	@Override
	public String disableProcJingYouPoi() {
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_THROUGH");
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

	public abstract List<String> getBanCmds();

	public abstract String[] getSupportCmds();

	public List<String> getCmdNavOnly() {
		return null;
	}

	@Override
	public void onStart() {
		mIsStarted = true;
		JNIHelper.logd("onStart");
		String pkn = getPackageName();
		if (mNavStatusListener != null) {
			mNavStatusListener.onStart(pkn);
		}
		NavManager.getInstance().dealWithNavStatus("navStart");
	}

	@Override
	public void onEnd(boolean arrive) {
		mIsPlaned = false;
		mIsStarted = false;
		JNIHelper.logd("onEnd");
		String pkn = getPackageName();
		if (mNavStatusListener != null) {
			mNavStatusListener.onEnd(pkn);
		}
		if (jyList != null) {
			jyList.clear();
		}

		NavManager.getInstance().dealWithNavStatus("navEnd");
	}

	@Override
	public void onPlanComplete() {
		mIsPlaned = true;
		mSpeechAfterPlan = false;
		JNIHelper.logd("onPlanComplete");
		if (mNavStatusListener != null) {
			mNavStatusListener.onPlanSucc(getPackageName());
		}
		notifyPlanEnd();
	}

	@Override
	public void onPlanError(int errCode, String errDesc) {
		mIsPlaned = false;
		JNIHelper.logd("onPlanError");
		if (mNavStatusListener != null) {
			mNavStatusListener.onPlanFail(getPackageName(), errCode, errDesc);
		}
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
		mHasBeenOpen = true;
		mIsFocus = true;
		JNIHelper.logd("onResume");
		String pkn = getPackageName();
		if (mNavStatusListener != null) {
			mNavStatusListener.onForeground(pkn, true);
		}
		broadAmapGround(true);
	}

	@Override
	public void onPause() {
		mIsFocus = false;
		JNIHelper.logd("onPause");
		String pkn = getPackageName();
		if (mNavStatusListener != null) {
			mNavStatusListener.onForeground(pkn, false);
		}
		broadAmapGround(false);

		// 关闭对话框
		dismiss();
	}

	@Override
	public void addStatusListener() {
	}

	public boolean needSeparateNavStatus() {
		return true;
	}

	public boolean banNavWakeup() {
		return false;
	}

    public void onSelect(boolean isWakeup, String resId, Runnable task) {
        String tts = NativeData.getResString(resId);
        if (!TextUtils.isEmpty(tts) && !tts.contains("已为您") && !tts.contains("将为您")) {
            if (isWakeup) {
                String alreadyCommandId = reCheckHintResIDForAlreadyDo(resId);
                tts = NativeData.getResString(alreadyCommandId).replace("%CMD%", tts);
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

	@Override
	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("enableWakeupExit")) {
			enableWakeupExitNav = Boolean.parseBoolean(new String(data));
			JNIHelper.logd("enableWakeupExit:" + enableWakeupExitNav);
			return null;
		}
		return super.invokeTXZNav(packageName, command, data);
	}

	protected void checkInNav() {
		if (mNavStatusListener != null) {
			mNavStatusListener.onStatusUpdate(getPackageName());
		}
	}

	/**
	 * 重新更新并注册命令
	 */
	protected void forceCheckInNav() {
		LogUtil.logd("forceCheckInNav");
		NavAppManager.getInstance().setNeedUpdateCmd(true);
		if (mNavStatusListener != null) {
			mNavStatusListener.onStatusUpdate(getPackageName());
		}
	}

	public int onWakeupRegister(AsrComplexSelectCallback acsc) {
		return 0;
	}

	/**
	 * 判断导航TTS是否播报
	 */
	protected void checkTtsStartOrEnd(boolean isTts) {
		LogUtil.logd("checkTtsStartOrEnd");
		if (mNavStatusListener != null) {
			mNavStatusListener.onTtsStartOrEnd(getPackageName(),isTts);
		}
	}

	// 第三方修改的唤醒词
	protected void addWakeupCommand(AsrComplexSelectCallback acsc, String type, String... cmds) {
		String[] cmd = null;// mThirdModifyMap.get(type);
		if (cmd == null) {
			acsc.addCommand(type, cmds);
		} else {
			acsc.addCommand(type, cmd);
		}
	}

	public Collection<String> onAsrCmdsRegister(IAsrRegCmdCallBack callBack) {
		return null;
	}

	public void onUnRegisterCmds() {

	}

	protected boolean processRemoteIsConfirm(String type) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "nav");
		jb.put("action", "mapConfirm");
		jb.put("type", type);
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	/**
	 * V2版本，之前方式会导致修改默认值时，之前拦截场景的值true，会成为false
	 * @param type
	 * @return
	 */
	protected boolean processRemoteIsConfirmV2(String type) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("scene", "nav");
		jb.put("action", "mapConfirmV2");
		jb.put("type", type);
		if (SenceManager.getInstance().noneedProcSence("nav", jb.toBytes())) {
			return true;
		}
		return false;
	}

	/**
	 * 重新规划路线的弹框
	 * 
	 * @param type
	 * @param speech
	 * @param hintResSlotId
	 * @param task
	 */
	protected void doRePlanWakeup(String type, String speech, String hintResSlotId, Runnable task) {
		boolean isNeedConfirm = true;
		NavWakeupTask nwt=null;
		if (processRemoteIsConfirm(type)) {
			isNeedConfirm = true;
		}
		if (processRemoteIsConfirmV2(type)) {
			isNeedConfirm = false;
		}

		if(type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION)||
				type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_ATM)||
				type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_REPAIR)||
				type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET)){
			if( !NavManager.getInstance().enableWayPoiSearch()){
				String  spk =NavManager.getInstance().disableSetJTPoi();
				if(!TextUtils.isEmpty(spk)){
					RecorderWin.speakTextWithClose(spk, true, new Runnable() {
						@Override
						public void run() {
						}
					});
					return;
				}
				if(RecorderWin.isOpened()){
					String answer = NativeData
							.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin
							.speakTextWithClose(
									answer,	null);
				}
				return;
			}
		}
		if(type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION)){
			nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_ONWAY_HINT_SPK", "RS_MAP_JIAYOUZHANG", "",
					isNeedConfirm, "RS_MAP_ONWAY_ADD_SURE_SPK");				
		}else if(type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_ATM)){
			nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_ONWAY_HINT_SPK", "RS_MAP_ATM", "",
					isNeedConfirm, "RS_MAP_ONWAY_ADD_SURE_SPK");				
		}else if(type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_REPAIR)){
			nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_ONWAY_HINT_SPK", "RS_MAP_WEIXIUZHAN", "",
					isNeedConfirm, "RS_MAP_ONWAY_ADD_SURE_SPK");				
		}else if(type.equals(AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET)){
			nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_ONWAY_HINT_SPK", "RS_MAP_CESUO", "",
					isNeedConfirm, "RS_MAP_ONWAY_ADD_SURE_SPK");				
		}else{
			nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_CONFIRM_HINT_SPK", hintResSlotId, "",
					isNeedConfirm, "RS_VOICE_DOING_COMMAND");
		}
		 
		if (nwt == null) {
			return;
		}

		AppLogic.runOnUiGround(new Runnable1<NavWakeupTask>(nwt) {

			@Override
			public void run() {
				mWinConfirmAsr = mP1.doTask(false);
				if (mWinConfirmAsr != null) {
					if (RecorderWin.isOpened()) {
						RecorderWin.close();
					}
					mWinConfirmAsr.show();
					if (DIALOG_TIME_OUT != -1) {
						mWinConfirmAsr.clickCancelCountDown(DIALOG_TIME_OUT / 1000);
					}
				}
			}
		});
	}

	protected void doExitConfirm(String type, String speech, Runnable task) {
		boolean isNeedConfirm = true;
		if (processRemoteIsConfirm(type)) {
			isNeedConfirm = false;
		}
		NavWakeupTask nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_CONFIRM_EXIT_HINT", "", "", isNeedConfirm,
				"RS_MAP_CONFIRM_EXIT_SURE");
		if (nwt == null) {
			return;
		}
		AppLogic.runOnUiGround(new Runnable1<NavWakeupTask>(nwt) {

			@Override
			public void run() {
				mWinConfirmAsr = mP1.doTask(false);
				if (mWinConfirmAsr != null) {
					if (RecorderWin.isOpened()) {
						RecorderWin.close();
					}
					mWinConfirmAsr.show();
					if (DIALOG_TIME_OUT != -1) {
						mWinConfirmAsr.clickCancelCountDown(DIALOG_TIME_OUT / 1000);
					}
				}
			}
		});
	}

	/**
	 * 确定要%COMMAND%
	 * 
	 * @param type
	 * @param speech
	 * @param task
	 */
	protected void doConfirmShow(String type, String speech, String hintResSlotId, Runnable task,
			boolean isDefaultNeedConfirm) {
		doConfirmShow(type, speech, hintResSlotId, task, "", isDefaultNeedConfirm);
	}

	protected void doConfirmShow(String type, String speech, String hintResSlotId, Runnable task, String okHint,
			boolean isDefaultNeedConfirm) {
		boolean isNeedConfirm = isDefaultNeedConfirm;
		if (processRemoteIsConfirm(type)) {
			isNeedConfirm = !isNeedConfirm;
		}

		NavWakeupTask nwt = doCmdWakeup(true, type, speech, task, "RS_MAP_DIALOG_HINT", hintResSlotId, "",
				isNeedConfirm, okHint);
		if (nwt == null) {
			return;
		}
		AppLogic.runOnUiGround(new Runnable1<NavWakeupTask>(nwt) {

			@Override
			public void run() {
				mWinConfirmAsr = mP1.doTask(true);
				if (mWinConfirmAsr != null) {
					if (RecorderWin.isOpened()) {
						RecorderWin.close();
					}
					mWinConfirmAsr.show();
					if (DIALOG_TIME_OUT != -1) {
						mWinConfirmAsr.clickCancelCountDown(DIALOG_TIME_OUT / 1000);
					}
				}
			}
		});
	}

	protected NavWakeupTask doCmdWakeup(boolean isWakeupResult, String type, String speech, Runnable task,
			String spkResId, String hintResSlotId, String prefixSlot, boolean isNeedConfirm, String okHintResId) {
		if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
			LogUtil.logd("WinConfirmAsr has showing...");
			RecorderWin.close();
			return null;
		}
		NavWakeupTask nwt = new NavWakeupTask() {
			public WinConfirmAsr createWinAsrDialog(boolean isSync, String doneSpk, String cancelSpk, Runnable task,WinConfirmAsrBuildData data) {
				return createWinInstance(isSync, doneSpk, cancelSpk, task, data);
			};
		};
		nwt.setType(type).setSpeech(speech).setTask(task).setSpkResId(spkResId).setHintResSlotId(hintResSlotId)
				.setPrefixSlot(prefixSlot).setNeedConfirm(isNeedConfirm).setOkHintResId(okHintResId);
		return nwt;
	}

	protected WinConfirmAsr createWinInstance(final boolean isSync, final String doneSpk, final String cancelSpk,
			final Runnable task,WinConfirmAsrBuildData data) {
		return new WinConfirmAsr(data) {
			
			@Override
			public String getReportDialogId() {
				return "NavThirdComplexDialog";
			}
			
			@Override
			public void onClickOk() {
				execTask(isSync, doneSpk, task);
			}
			
			@Override
			public void onClickCancel() {
				TtsManager.getInstance().speakText(cancelSpk, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
			}
		};
	}

	public abstract class NavWakeupTask {
		public String type;
		public String speech;
		public Runnable task;
		public String spkResId;
		public String hintResSlotId;
		public String prefixSlot;
		public boolean isWakeupResult;
		public boolean isNeedConfirm;

		public String okHintResId;
		public String leftSlot;
		public String rightSlot;
		public String cancelOkResId;

		public NavWakeupTask setType(String type) {
			this.type = type;
			return this;
		}

		public NavWakeupTask setSpeech(String speech) {
			this.speech = speech;
			return this;
		}

		public NavWakeupTask setTask(Runnable run) {
			this.task = run;
			return this;
		}

		public NavWakeupTask setHintResSlotId(String resId) {
			this.hintResSlotId = resId;
			return this;
		}

		public NavWakeupTask setSpkResId(String resId) {
			this.spkResId = resId;
			return this;
		}

		public NavWakeupTask setPrefixSlot(String slot) {
			this.prefixSlot = slot;
			return this;
		}

		public NavWakeupTask setWakeupResult(boolean isWakeResult) {
			this.isWakeupResult = isWakeResult;
			return this;
		}

		public NavWakeupTask setNeedConfirm(boolean confirm) {
			this.isNeedConfirm = confirm;
			return this;
		}

		public NavWakeupTask setOkHintResId(String resId) {
			this.okHintResId = resId;
			return this;
		}

		public NavWakeupTask setCancelOkResId(String resId) {
			this.cancelOkResId = resId;
			return this;
		}

		public NavWakeupTask setLeftSlot(String slot) {
			this.leftSlot = slot;
			return this;
		}

		public NavWakeupTask setRightSlot(String slot) {
			this.rightSlot = slot;
			return this;
		}

		public WinConfirmAsr doTask(boolean isSync) {
			// 配置取消所有弹窗
			if (NavManager.getInstance().mRemoveNavDialog != null) {
				if (NavManager.getInstance().mRemoveNavDialog) {
//					isSync = true;
					isNeedConfirm = false;
					LogUtil.logd("nav no needConfirm");
				} else {
					isSync = true;
					isNeedConfirm = true;
					LogUtil.logd("nav needConfirm");
				}
			}
			
			String aimSlot = NativeData.getResString(hintResSlotId);
			if (TextUtils.isEmpty(aimSlot)) {
				aimSlot = speech;
			}

			String text = "";
			if (!TextUtils.isEmpty(okHintResId)) {
				text = NativeData.getResString(okHintResId).replace("%COMMAND%",
						!TextUtils.isEmpty(aimSlot) ? aimSlot : speech);
			}

            String alreadyCommandId = reCheckHintResIDForAlreadyDo(hintResSlotId);
            if (TextUtils.isEmpty(text)) {
                if (isSync) {
                    text = NativeData.getResString(alreadyCommandId).replace("%CMD%", aimSlot);
                } else {
                    text = NativeData.getResString("RS_VOICE_DOING_COMMAND").replace("%CMD%", aimSlot);
                }
            }

			if (!isNeedConfirm) {
				if (isSync) {
					task.run();
					if (RecorderWin.isOpened()) {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(text, null);
					} else {
						TtsManager.getInstance().speakText(text, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
					}
					return null;
				}
				if (RecorderWin.isOpened()) {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(text, task);
				} else {
					TtsManager.getInstance().speakText(text, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY, new TtsUtil.ITtsCallback() {
						@Override
						public void onEnd() {
							task.run();
						}
					});
				}
				return null;
			}

			// 确定要Slot
			String spk = "";
			spk = reCheckHintResIdForSureDo(hintResSlotId, spkResId);
            if(TextUtils.isEmpty(spk)){
				spk = NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
						!TextUtils.isEmpty(aimSlot) ? aimSlot : speech);

			}
			String sure = NativeData.getResString("RS_MAP_CONFIRM_SURE_ASR");
			String cancel = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
			if (!TextUtils.isEmpty(leftSlot)) {
				sure = NativeData.getResString(leftSlot);
			}
			if (!TextUtils.isEmpty(rightSlot)) {
				cancel = NativeData.getResString(rightSlot);
			}

			String cancelSpk = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_SURE");
			if (!TextUtils.isEmpty(cancelOkResId)) {
				cancelSpk = NativeData.getResString(cancelOkResId);
			}

			WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
			data.setCancelText(cancel, new String[]{cancel});
			if (NavManager.getInstance().mClickOutSizeCancelDialog != null) {
				data.setCancelOutside(NavManager.getInstance().mClickOutSizeCancelDialog);
			}
			data.setSureText(sure, new String[]{sure});
			data.setHintTts(spk, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
			data.setMessageText(spk);
			return createWinAsrDialog(isSync, text, cancelSpk, task, data);
		}

		public abstract WinConfirmAsr createWinAsrDialog(final boolean isSync, final String doneSpk,
				final String cancelSpk, Runnable task,WinConfirmAsrBuildData data);

	}

	private void broadAmapGround(boolean isShow) {
		Intent intent = new Intent(isShow ? NAV_FRONTGROUND : NAV_BACKGROUND);
		GlobalContext.get().sendBroadcast(intent);
	}

	public void execTask(boolean isSync, String doneSpk, final Runnable task) {
		if (!isSync) {
			TtsManager.getInstance().speakText(doneSpk, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY, new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					task.run();
				}
			});
			return;
		}

		task.run();
		TtsManager.getInstance().speakText(doneSpk,TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
	}
	@Override
	public String getDestinationCity() {
		return null;
	}

    /**
     * 根据新版本ID重新区分"已为您%CMD%"这句字符串的ID
     * @param hintResID id
     * @return 新字符ID
     */
    public String reCheckHintResIDForAlreadyDo(String hintResID){
        if ("RS_MAP_ZOOMIN".equals(hintResID)) {
            return  "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_ZOOM_IN";
        } else if ("RS_MAP_ZOOMOUT".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_ZOOM_OUT";
        } else if ("RS_MAP_OPEN_TRAFFIC".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_OPEN_TRAFFIC";
        } else if ("RS_MAP_CLOSE_TRAFFIC".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_CLOSE_TRAFFIC";
        } else if ("RS_MAP_TWO_MODE".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_TWO_MODE";
        } else if ("RS_MAP_THREE_MODE".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_THREE_MODE";
        } else if ("RS_MAP_AUTO_MODE".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_AUTO_MODE";
        } else if ("RS_MAP_CAR_DIRECT".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_CAR_DIRECT";
        } else if ("RS_MAP_NORTH_DIRECT".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_NORTH_DIRECT";
        } else if ("RS_MAP_LIGHT_MODE".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_LIGHT_MODE";
        } else if ("RS_MAP_NIGHT_MODE".equals(hintResID)) {
            return "RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_NIGHT_MODE";
        } else if("RS_MAP_EXPERT_MODE".equals(hintResID)||"RS_MAP_MEADWAR_MODE".equals(hintResID)){
			return "RS_MAP_CHANGE_MEADWAR_OR_EXPERT_MODE";
		} else if("RS_MAP_OPEN_SIMPLE_MODE".equals(hintResID)){
        	return "RS_MAP_OPEN_TRAFFIC";
		} else if("RS_MAP_CLOSE_SIMPLE_MODE".equals(hintResID)){
			return "RS_MAP_CLOSE_TRAFFIC";
		}else if("RS_MAP_REFRESH_PATH".equals(hintResID)){
        	return "RS_MAP_OK";
		}
        return "RS_VOICE_ALREAD_DO_COMMAND";
    }

	/**
	 * 根据新版本ID重新区分"你确定要%COMMAND%"这句字符串的ID
	 * @param hintResID
	 * @param spkResId 您确定要%COMMAND%吗
	 * @return 新字符文本
	 */
	public String reCheckHintResIdForSureDo(String hintResID, String spkResId) {
		if ("RS_MAP_TWO_MODE".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_TWO_MODE"));
		} else if ("RS_MAP_THREE_MODE".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_THREE_MODE"));
		} else if ("RS_MAP_CAR_DIRECT".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_CAR_DIRECT"));
		} else if ("RS_MAP_NORTH_DIRECT".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_NORTH_DIRECT"));
		} else if ("RS_MAP_OPEN_TRAFFIC".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_OPEN_TRAFFIC"));
		} else if ("RS_MAP_CLOSE_TRAFFIC".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_CLOSE_TRAFFIC"));
		} else if ("RS_MAP_ZOOMIN".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_ZOOMIN"));
		} else if ("RS_MAP_ZOOMOUT".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_ZOOMOUT"));
		} else if ("RS_MAP_NIGHT_MODE".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_NIGHT_MODE"));
		} else if ("RS_MAP_LIGHT_MODE".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_LIGHT_MODE"));
		} else if ("RS_MAP_AUTO_MODE".equals(hintResID)) {
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CHANGE_AUTO_MODE"));
		} else if("RS_MAP_CLOSE_SIMPLE_MODE".equals(hintResID)){
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_CLOSE_SIMPLE_MODE"));
		} else if("RS_MAP_OPEN_SIMPLE_MODE".equals(hintResID)){
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_OPEN_SIMPLE_MODE"));
		} else if("RS_MAP_REFRESH_PATH".equals(hintResID)){
			return NativeData.getResPlaceholderString(spkResId, "%COMMAND%",
					NativeData.getResString("RS_MAP_REFRESH_PATH"));
		}
		return "";
	}

}
