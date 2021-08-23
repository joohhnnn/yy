package com.txznet.txz.module.launch;

import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.ui.win.help.WinHelpDetail;
import com.txznet.txz.ui.win.help.WinHelpTops;
import com.txznet.txz.ui.win.record.RecorderWin;

/**
 * 启动管理器，负责启动入口管理
 * 
 * @author bihongpi
 *
 */
public class LaunchManager extends IModule {
	static LaunchManager sModuleInstance = null;

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
		// 从默认入口启动
		return true;
	}

	public boolean launchWithNav() {
		// 从导航启动
		return true;
	}

	public boolean launchWithMusic() {
		// 从音乐启动
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.open", null, null);
		return true;
	}

	public boolean launchWithHelpDetail() {
		// 从帮助启动
		WinHelpDetail.getInstance().show();
		return true;
	}

	public boolean launchWithWakeup(String style) {
		RecordManager.getInstance().stop();

		if (LicenseManager.getInstance().checkInited() == false) {
			return true;
		}

		if (AsrManager.getInstance().isBusy()) {
			return true;
		}

		// 处于电话中禁止声控
		if (!CallManager.getInstance().isIdle()) {
			return true;
		}

		ResourceManager.getInstance().setTmpStyle(style);

		WinHelpDetail.getInstance().dismiss();

		WinHelpTops.getInstance().dismiss();
		
		{
			// 回音消除设备加上唤醒取消上次的延时任务
			NavManager.getInstance().cancelAllPoiSearch();
			MusicManager.getInstance().cancelSearchMedia();
			AudioManager.getInstance().cancelAllRequest();
//			LocationManager.getInstance().cancelReverseGeo();
			LocationManager.getInstance().cancelQueryTraffic();
			LocationManager.getInstance().cancelRequestGeoCode();
		}

		RecorderWin.open();
		return true;
	}

	public boolean launchWithRecord() {
		RecordManager.getInstance().stop();

		if (LicenseManager.getInstance().checkInited() == false) {
			return true;
		}

		if (!AsrManager.getInstance().isInitSuccessed()
				|| !TtsManager.getInstance().isInitSuccessed()
				|| !WakeupManager.getInstance().isInitSuccessed()) {
			AppLogic.showToast("语音初始化没有完成");
			return true;
		}

		// 从录音按钮启动
		if (AsrManager.getInstance().isBusy() || RecorderWin.isOpened()) {
			RecorderWin.close();
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("core").setAction("close").buildTouchReport());
			AsrManager.getInstance().cancel();
			return true;
		}

		WinHelpDetail.getInstance().dismiss();
		WinHelpTops.getInstance().dismiss();

		// 处于电话中禁止声控
		if (!CallManager.getInstance().isIdle()) {
			AppLogic.showToast("电话中无法使用声控");
			return true;
		}

		if (WakeupManager.getInstance().mBindStyleWithWakeup) {
			ResourceManager.getInstance().setTmpStyle("");
		} else {
			ResourceManager.getInstance().setTmpStyle(null);
		}

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
			WakeupPcmHelper.savePcm(sb.toString(), 2);
		}
		RecorderWin.open();
		// 数据上报
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("core").setAction("open").buildTouchReport());
		return true;
	}

	public boolean launchWithUpdated() {
		String strHint = GlobalContext.get().getString(R.string.app_desc);
		if (strHint != null && strHint.isEmpty() == false) {
			// 更新提示
			new WinNotice() {
				@Override
				public void onClickOk() {
				}
			}.setMessage(strHint).show();
			String spk = NativeData.getResString("RS_LAUNCH_UPDATE");
			TtsManager.getInstance().speakText(spk);
		}
		return true;
	}

	public boolean launchWithWeixinAssistor() {
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
