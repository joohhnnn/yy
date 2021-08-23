package com.txznet.txz.module.launch;

import com.txz.ui.data.UiData.TTime;
import com.txz.ui.event.UiEvent;
import com.txz.ui.record.UiRecord;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;

/**
 * 启动管理器，负责启动入口管理
 * 
 * @author bihongpi
 *
 */
public class LaunchManager extends IModule {
	static LaunchManager sModuleInstance = null;

	public static final String TAG = "LaunchManager ";

	private LaunchManager() {

	}

	public static LaunchManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (LaunchManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new LaunchManager();
			}
		}
		return sModuleInstance;
	}

	// ///////////////////////////////////////////////////////////////////////////////

	public void onMainActivityCreate() {
	}

	// ///////////////////////////////////////////////////////////////////////////////

	public boolean launchDefault() {
		LogUtil.logd(TAG + "launchDefault");
		// 从默认入口启动
		return true;
	}

	public boolean launchWithNav() {
		LogUtil.logd(TAG + "launchWithNav");
		// 从导航启动
		return true;
	}

	public boolean launchWithMusic() {
		LogUtil.logd(TAG + "launchWithMusic");
		// 从音乐启动
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.open", null, null);
		return true;
	}

	public boolean launchWithHelpDetail() {
		LogUtil.logd(TAG + "launchWithHelpDetail");
		// 从帮助启动
		WinHelpManager.getInstance().show(new JSONBuilder().put("type",WinHelpManager.TYPE_OPEN_FROM_CLICK).toString());
		return true;
	}

	public boolean launchWithWakeup(String style, String text,long wakeupId) {
		LogUtil.logd(TAG + "launchWithWakeup");
		MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_WEAKUP);
		AsrManager.getInstance().mStartFromWeakup = true;
		AsrManager.getInstance().mAsrFromWakeup = true;
		RecordManager.getInstance().cancel();

		if (LicenseManager.getInstance().checkInited() == false) {
			return true;
		}

		//在倒车影像中，不允许启动声控
		if (TXZPowerControl.isEnterReverse()) {
			return true;
		}

		if (AsrManager.getInstance().isBusy()) {
			return true;
		}

		// 处于电话中禁止声控
		if (!CallManager.getInstance().isIdle()) {
			return true;
		}
		// 录音功能已禁用
		if (!ProjectCfg.isEnableRecording()) {
			return true;
		}
		// 如果当前没有在引导动画，则标记已经跳过引导
		if (HelpGuideManager.getInstance().isAniming()) {
			return true;
		}
		// 启动语音引导界面
		if (HelpGuideManager.getInstance().isNeedGuideAnim()) {
			HelpGuideManager.getInstance().execGuideAnim();
			return true;
		}
		// 唤醒语音计数
		HelpGuideManager.getInstance().incWakeupCount();

		ResourceManager.getInstance().setTmpStyle(style);



		{
			// 回音消除设备加上唤醒取消上次的延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
			LocationManager.getInstance().cancelReverseGeo();
			LocationManager.getInstance().cancelQueryTraffic();
			LocationManager.getInstance().cancelRequestGeoCode();
		}
		
		ChoiceManager.getInstance().clearIsSelecting();

		ReportUtil.setSessionId(NativeData.getMilleServerTime().uint64Time);
		ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setKeywords(text)
						.setAction("wakeup").setRecordType(UiRecord.RECORD_TYPE_WAKEUP_KW)
						 .setTaskID(wakeupId+"").setSessionId().buildWakeupReport(),
				UiRecord.RECORD_TYPE_WAKEUP_KW, wakeupId);
		WakeupPcmHelper.savePcm(text, UiRecord.RECORD_TYPE_WAKEUP_KW, wakeupId+"");
		RecorderWin.open();
		return true;
	}

	
	public boolean mStartWithInstanceAsr = false; //是否由免唤醒词启动
	
	/**
	 * 免唤醒识别
	 * @return
	 */
	public boolean launchWithInstantAsr(String style, String text, long wakeupId) {
		LogUtil.logd(TAG + "launchWithInstantAsr");
		MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_WEAKUP);
		AsrManager.getInstance().mStartFromWeakup = true;
		RecordManager.getInstance().cancel();

		if (LicenseManager.getInstance().checkInited() == false) {
			return true;
		}

		//在倒车影像中，不允许启动声控
		if (TXZPowerControl.isEnterReverse()) {
			return true;
		}

		if (AsrManager.getInstance().isBusy()) {
			return true;
		}

		// 处于电话中禁止声控
		if (!CallManager.getInstance().isIdle()) {
			return true;
		}
		// 录音功能已禁用
		if (!ProjectCfg.isEnableRecording()) {
			return true;
		}
		// 处于引导动画中禁止声控
		if (HelpGuideManager.getInstance().isAniming()) {
			return true;
		}
		// 启动语音引导界面
		if (HelpGuideManager.getInstance().isNeedGuideAnim()) {
			HelpGuideManager.getInstance().execGuideAnim();
			return true;
		}
		ResourceManager.getInstance().setTmpStyle(style);



		{
			// 回音消除设备加上唤醒取消上次的延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
			LocationManager.getInstance().cancelReverseGeo();
			LocationManager.getInstance().cancelQueryTraffic();
			LocationManager.getInstance().cancelRequestGeoCode();
		}
		ChoiceManager.getInstance().clearIsSelecting();
		ReportUtil.doReport(new ReportUtil.Report.Builder().setKeywords(text).setAction("wakeup")
				.setRecordType(UiRecord.RECORD_TYPE_WAKEUP_KW).setTaskID(wakeupId+"")
				.setType("oneshot").buildWakeupReport());
		WakeupPcmHelper.savePcm(text, UiRecord.RECORD_TYPE_WAKEUP_KW, wakeupId+"");
		RecorderWin.openInstantAsr();
		
		mStartWithInstanceAsr = true;
		
		return true;
	}

	public boolean launchWithRecord() {
		LogUtil.logd(TAG + "launchWithRecord");
		if (!RecorderWin.isOpened()) {
			MtjModule.getInstance().event(MtjModule.EVENTID_VOICE_MANUAL);
		}
		//RecordManager.getInstance().stop();
		RecordManager.getInstance().cancel();

		if (LicenseManager.getInstance().checkInited() == false) {
			return true;
		}
		//路况播报的
		FakeReqManager.getInstance().dismissDialog();

		//在倒车影像中，不允许启动声控
		if (TXZPowerControl.isEnterReverse()) {
			return true;
		}
		
		// 这里识别引擎初始化会延迟
		if (/*!AsrManager.getInstance().isInitSuccessed()
				|| */!TtsManager.getInstance().isInitSuccessed()
				/*|| !WakeupManager.getInstance().isInitSuccessed()*/) {
			AppLogic.showToast("语音初始化没有完成");
			return true;
		}
//		SelectCityDialog.getInstance().isShowing();
		// 从录音按钮启动
		if (AsrManager.getInstance().isBusy()
				|| (RecorderWin.isOpened() && !SearchEditManager.getInstance().isShowing())) {
			RecorderWin.close();
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("core").setSessionId()
					.setAction("close").buildTouchReport());
			AsrManager.getInstance().cancel();
			return true;
		}


		// 处于电话中禁止声控
		if (!CallManager.getInstance().isIdle()) {
			AppLogic.showToast("电话中无法使用声控");
			// 经常会出现来电后无法录音的问题，让客户点击图标来保存录音，以方便拉取录音看问题
			saveClickVoice();  
			return true;
		}
		// 录音功能已禁用
		if (!ProjectCfg.isEnableRecording()) {
			AppLogic.showToast("录音功能已禁用");
			return true;
		}
		// 处于引导动画中禁止声控
		if (HelpGuideManager.getInstance().isAniming()) {
			AppLogic.showToast("引导中无法使用声控");
			return true;
		}
		// 启动语音引导界面
		if (HelpGuideManager.getInstance().isNeedGuideAnim()) {
			HelpGuideManager.getInstance().execGuideAnim();
			return true;
		}

		if (WakeupManager.getInstance().mBindStyleWithWakeup) {
			ResourceManager.getInstance().setTmpStyle("");
		} else {
			ResourceManager.getInstance().setTmpStyle(null);
		}
		saveClickVoice();
		RecorderWin.open();
		return true;
	}

    public void saveClickVoice() {
        TTime tTime = NativeData.getMilleServerTime();
		long Id = tTime.uint64Time;
		{
			StringBuilder sb = new StringBuilder("点击启动:");
			String[] kws = WakeupManager.getInstance().mWakeupKeywords_Sdk;
			if (kws != null) {
				for (String s : kws) {
					sb.append(" " + s);
				}
			}
			kws = WakeupManager.getInstance().mWakeupKeywords_User;
			if (kws != null) {
				for (String s : kws) {
					sb.append(" " + s);
				}
			}
			WakeupPcmHelper.savePcm(sb.toString(), UiRecord.RECORD_TYPE_CLICK, Id+"");
		}
		// 数据上报
		ReportUtil.setSessionId(NativeData.getMilleServerTime().uint64Time);
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("core").setRecordType(UiRecord.RECORD_TYPE_CLICK)
				.setAction("open").setTaskID(Id+"").setSessionId().buildTouchReport());

//		ReportUtil.doVoiceReport(new ReportUtil.Report.Builder().setRecordType(UiRecord.RECORD_TYPE_CLICK)
//		        .setTaskID(Id+"").setAction("open").buildVoiceReport(), UiRecord.RECORD_TYPE_CLICK, Id);
	}

	public boolean launchWithUpdated() {
		LogUtil.logd(TAG + "launchWithUpdated");
		String strHint = GlobalContext.get().getString(R.string.app_desc);
		if (strHint != null && strHint.isEmpty() == false) {
			// 更新提示
			new WinNotice(new WinNotice.WinNoticeBuildData().setMessageText(strHint,false)) {
				@Override
				public void onClickOk() {
				}

				@Override
				public String getReportDialogId() {
					return "show_app_desc";
				}
			}.show();

			String spk = NativeData.getResString("RS_LAUNCH_UPDATE");
			TtsManager.getInstance().speakText(spk);
		}
		return true;
	}

	public boolean launchWithWeixinAssistor() {
		LogUtil.logd(TAG + "launchWithWeixinAssistor");
		// 从微信助手启动
		// WinManager.getInstance().entry(WinWeixinAssitor.getInstance(), true);
		// WinManager.getInstance().entry(WinWebchat.getInstance(), true);
		return true;
	}

	int mInitTtsId = TtsManager.INVALID_TTS_TASK_ID;

	boolean mAllKeywordsReady = false;

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_SYSTEM_IDLE);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_SYSTEM_IDLE: {
			if (mAllKeywordsReady == false) {
				mAllKeywordsReady = true;
			}
			break;
		}
		}
		return super.onEvent(eventId, subEventId, data);
	}
}
