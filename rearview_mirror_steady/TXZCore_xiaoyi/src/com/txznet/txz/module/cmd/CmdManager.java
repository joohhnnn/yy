package com.txznet.txz.module.cmd;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.component.nav.txz.NavTxzImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

/**
 * 全局命令字管理模块，负责全局命令字注册和处理
 * 
 * @author bihongpi
 *
 */
public class CmdManager extends IModule {
	static CmdManager sModuleInstance = new CmdManager();

	private CmdManager() {

	}

	public static CmdManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		// regCommand("UI_TAB_MSG");
		regCommand("UI_TAB_CALL");
		// regCommand("UI_TAB_GUIDE");
		regCommand("UI_TAB_NAV");
		// regCommand("UI_TAB_ME");
		regCommand("UI_TAB_MUSIC");

		regCommandWithResult("NAV_CMD_STOP");

		// regCommand("GLOBAL_CMD_BACK");
		// regCommand("GLOBAL_CMD_OPEN_FM");
		// regCommand("GLOBAL_CMD_CLOSE_FM");
		// regCommand("GLOBAL_CMD_OPEN_BT");
		// regCommand("GLOBAL_CMD_CLOSE_BT");
		regCommand("GLOBAL_CMD_OPEN_WIFI");
		regCommand("GLOBAL_CMD_CLOSE_WIFI");
		// regCommand("GLOBAL_CMD_MUTE_RECORD");
		// regCommand("GLOBAL_CMD_RETURN_HOME");
		// regCommand("GLOBAL_CMD_TAKE_CAMERA");
		// regCommand("GLOBAL_CMD_OPEN_EDOG_VOICE");
		// regCommand("GLOBAL_CMD_CLOSE_EDOG_VOICE");
		// regCommand("GLOBAL_CMD_OPEN_EDOG");

		regCommand("FEEDBACK_CMD_MUTE_ON");
		regCommand("FEEDBACK_CMD_MUTE_OFF");
		regCommand("FEEDBACK_CMD_VOL_MAX");
		regCommand("FEEDBACK_CMD_VOL_MIN");
		regCommand("FEEDBACK_CMD_VOL_UP");
		regCommand("FEEDBACK_CMD_VOL_DOWN");
		regCommand("FEEDBACK_CMD_LIGHT_MAX");
		regCommand("FEEDBACK_CMD_LIGHT_MIN");
		regCommand("FEEDBACK_CMD_LIGHT_UP");
		regCommand("FEEDBACK_CMD_LIGHT_DOWN");

		regCommand("ASK_FOR_NAME");
		regCommand("NAV_FOR_REPAIR");

		return super.initialize_AfterStartJni();
	}

	public int onASK_FOR_NAME() {
		String[] names = WakeupManager.getInstance().mWakeupKeywords_User;
		if (names != null && names.length > 0
				&& (names[0].startsWith("你好") || names[0].startsWith("召见"))) {
			String txt = NativeData.getResString("RS_VOICE_MY_NAME_IS");
			RecorderWin.speakTextWithClose(
					txt.replace("%NAME%", names[0].substring(2)).replace("%KW%", names[0]), null);
			return 0;
		}
		String[] kws = WakeupManager.getInstance().getWakeupKeywords_Sdk();
		if (kws != null && kws.length > 0) {
			String txt = NativeData.getResString("RS_VOICE_WAKEUP_KEYWORDS_IS");
			StringBuffer sb = new StringBuffer(kws[0]);
			for (int i = 1; i < kws.length; ++i) {
				sb.append('，');
				sb.append(kws[i]);
			}
			RecorderWin.speakTextWithClose(txt.replace("%KW%", sb), null);
			return 0;
		}
		String txt = NativeData.getResString("RS_VOICE_NO_NAME");
		RecorderWin.open(txt);
		return 0;
	}

	public int onNAV_FOR_REPAIR(){
		String spk = NativeData.getResString("RS_CMD_SEARCH_4S");
		RecorderWin.addSystemMsg(spk);
		TtsManager.getInstance().speakText(spk, PreemptType.PREEMPT_TYPE_NEXT,new ITtsCallback() {
			@Override
			public void onSuccess() {
				NavigateInfo info = new NavigateInfo();
				info.strTargetName = "4S店";
				NavManager.getInstance().navigateByName(info, false,PoiAction.ACTION_NAVI, false);
				super.onEnd();
			}				
		});
		return 0;
	}
	public int onUI_TAB_CALL() { 
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);

		if (!CallManager.getInstance().checkMakeCall()) {
			return 0;
		}

		String txt = NativeData
				.getResString("RS_VOICE_WHO_DO_YOU_WANT_TO_MAKE_CALL");
		RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_MAKE_CALL);
		return 0;
	}

	public int onUI_TAB_NAV() {
		String reason = NavManager.getInstance().getDisableResaon();

		if (!TextUtils.isEmpty(reason)) {
			RecorderWin.speakTextWithClose(reason, null);
			return 0;
		}

		if (!(NavManager.getInstance().getLocalNavImpl() instanceof NavTxzImpl)
				|| NavManager.getInstance().isNavi()) {
			String spk = NativeData.getResString("RS_CMD_OPEN_NAV");
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					NavManager.getInstance().invokeTXZNav(null, "enterNav",
							null);
				}
			});
			return 0;
		}

		final String NAV_CACHE_NAME = ".txz_nav_info_cache";
		File file = new File(Environment.getExternalStorageDirectory(),
				NAV_CACHE_NAME);
		if (file.exists()) {
			String spk = NativeData.getResString("RS_CMD_OPEN_NAV");
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					NavManager.getInstance().invokeTXZNav(null, "enterNav",
							null);
				}
			});
			return 0;
		}

		String txt = NativeData
				.getResString("RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE");

		RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_NAVIGATE, new Runnable() {
			@Override
			public void run() {
				AsrManager.getInstance().mSenceRepeateCount = 0;
			}
		});
		return 0;
	}

	public int onUI_TAB_MUSIC() {
		RecorderWin.speakTextWithClose(
				NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC"),
				new Runnable() {
					@Override
					public void run() {
						MusicManager.getInstance().invokeTXZMusic(null, "play",
								null);
					}
				});
		return 0;
	}

	public int onNAV_CMD_STOP(String keywords, String voiceString) {
		JSONBuilder json = new JSONBuilder();
		json.put("sence", "nav");
		json.put("text", voiceString);
		// json.put("keywords", keywords);
		json.put("action", "exit");
		if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
			return 0;
		}
		
		NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if(nta != null && nta instanceof NavCldImpl){
			if (!((NavCldImpl) nta).enableExitNav()) {
				// String answer =
				// NativeData.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
				// RecorderWin.speakTextWithClose(
				// answer.replace("%CONTENT%",
				// NativeData.getResString("RS_VOICE_USUAL_SPEAK_GRAMMAR")),
				// null);
				((NavCldImpl)nta).onNavCommand(false, "CANCEL", "取消路径");
				return 0;
			}
		}
		String spk = NativeData.getResString("RS_CMD_EXIT_NAV");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				NavManager.getInstance().exitAllNavTool();
			}
		});
		return 0;
	}

	public int onGLOBAL_CMD_BACK() {
		String spk = NativeData.getResString("RS_CMD_BACK");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						// Runtime runtime = Runtime.getRuntime();
						// runtime.exec("input keyevent " +
						// KeyEvent.KEYCODE_BACK);

						Instrumentation inst = new Instrumentation();
						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
					}
				}, 50);
			}
		});
		return 0;
	}

	public int onFEEDBACK_CMD_MUTE_ON() {
		if (SenceManager.getInstance().noneedProcCommand("vol_off", "关闭声音"))
			return 0;
		String spk = NativeData.getResString("RS_CMD_MUTE_ON");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				VolumeManager.getInstance().mute(true);
			}
		});
		return 0;
	}

	public int onFEEDBACK_CMD_MUTE_OFF() {
		if (SenceManager.getInstance().noneedProcCommand("vol_on", "打开声音"))
			return 0;
		VolumeManager.getInstance().mute(false);
		String spk = NativeData.getResString("RS_CMD_MUTE_OFF");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onFEEDBACK_CMD_VOL_UP() {
		if (SenceManager.getInstance().noneedProcCommand("vol_up", "增加音量"))
			return 0;
		VolumeManager.getInstance().mute(false);
		if (VolumeManager.getInstance().incVolume()){
			String spk = NativeData.getResString("RS_CMD_VOL_UP");
			RecorderWin.speakTextWithClose(spk, null);
		}else{
			String spk = NativeData.getResString("RS_CMD_VOL_MAX");
			RecorderWin.speakTextWithClose(spk, null);
		}
			
		return 0;
	}

	public int onFEEDBACK_CMD_VOL_DOWN() {
		if (SenceManager.getInstance().noneedProcCommand("vol_down", "降低音量"))
			return 0;
		if (VolumeManager.getInstance().decVolume()){
			String spk = NativeData.getResString("RS_CMD_VOL_DOWN");
			RecorderWin.speakTextWithClose(spk, null);
		}else{
			String spk = NativeData.getResString("RS_CMD_VOL_MIN");
			RecorderWin.speakTextWithClose(spk, null);
		}
			
		return 0;
	}

	public int onFEEDBACK_CMD_VOL_MAX() {
		if (SenceManager.getInstance().noneedProcCommand("vol_max", "最大音量"))
			return 0;
		VolumeManager.getInstance().mute(false);
		VolumeManager.getInstance().maxVolume();
		String spk = NativeData.getResString("RS_CMD_VOL_MAX_SETTING");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onFEEDBACK_CMD_VOL_MIN() {
		if (SenceManager.getInstance().noneedProcCommand("vol_min", "最小音量"))
			return 0;
		String spk = NativeData.getResString("RS_CMD_VOL_MIN_SETTING");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				VolumeManager.getInstance().minVolume();
			}
		});
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_UP() {
		if (SenceManager.getInstance().noneedProcCommand("light_up", "增加亮度"))
			return 0;
		int n = Settings.System.getInt(
				GlobalContext.get().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		if (n < 255) {
			n += 50;
			if (n > 255)
				n = 255;
			Settings.System.putInt(GlobalContext.get().getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, n);
			String spk = NativeData.getResString("RS_CMD_LIGHT_UP");
			RecorderWin.speakTextWithClose(spk, null);
		} else{
			String spk = NativeData.getResString("RS_CMD_LIGHT_MAX");
			RecorderWin.speakTextWithClose(spk, null);
		}
			
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_DOWN() {
		if (SenceManager.getInstance().noneedProcCommand("light_down", "降低亮度"))
			return 0;
		int n = Settings.System.getInt(
				GlobalContext.get().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		if (n > 80) {
			n -= 50;
			if (n < 80)
				n = 80;
			Settings.System.putInt(GlobalContext.get().getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS, n);
			String spk = NativeData.getResString("RS_CMD_LIGHT_DOWN");
			RecorderWin.speakTextWithClose(spk, null);
		} else{
			String spk = NativeData.getResString("RS_CMD_LIGHT_MIN");
			RecorderWin.speakTextWithClose(spk, null);
		}			
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_MAX() {
		if (SenceManager.getInstance().noneedProcCommand("light_max", "最大亮度"))
			return 0;
		Settings.System.putInt(GlobalContext.get().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		String spk = NativeData.getResString("RS_CMD_LIGHT_MAX_SETTING");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_MIN() {
		if (SenceManager.getInstance().noneedProcCommand("light_min", "最小亮度"))
			return 0;
		Settings.System.putInt(GlobalContext.get().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 80);
		String spk = NativeData.getResString("RS_CMD_LIGHT_MIN_SETTING");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onGLOBAL_CMD_OPEN_FM() {
		Intent it = new Intent();
		it.setAction("action.FM_ON_OR_OFF");
		it.putExtra("FM_FLAG", "FM_ON");// 打开
		GlobalContext.get().sendBroadcast(it);
		return 0;
	}

	public int onGLOBAL_CMD_CLOSE_FM() {
		Intent it = new Intent();
		it.setAction("action.FM_ON_OR_OFF");
		it.putExtra("FM_FLAG", "FM_OFF");// 关闭
		GlobalContext.get().sendBroadcast(it);
		return 0;
	}

	public int onGLOBAL_CMD_OPEN_BT() {
		Intent intent = new Intent();
		intent.setAction("bluetooth_open_close_from_third_action");
		intent.putExtra("open", true);
		GlobalContext.get().sendBroadcast(intent);
		return 0;
	}

	public int onGLOBAL_CMD_CLOSE_BT() {
		Intent intent = new Intent();
		intent.setAction("bluetooth_open_close_from_third_action");
		intent.putExtra("open", false);
		GlobalContext.get().sendBroadcast(intent);
		return 0;
	}

	public int onGLOBAL_CMD_OPEN_WIFI() {
		if (SenceManager.getInstance().noneedProcCommand("wifi_on", "打开无线网络"))
			return 0;
		String spk = NativeData.getResString("RS_CMD_WIFI_ON");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				WifiManager mWifiManager;
				mWifiManager = (WifiManager) GlobalContext.get()
						.getSystemService(Context.WIFI_SERVICE);
				try {
					Intent intent = new Intent(
							android.provider.Settings.ACTION_WIFI_SETTINGS);
					if (intent != null
							&& mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						GlobalContext.get().startActivity(intent);
					}
				} catch (Exception e) {
				}
				mWifiManager.setWifiEnabled(true);
			}
		});
		return 0;
	}

	public int onGLOBAL_CMD_CLOSE_WIFI() {
		if (SenceManager.getInstance().noneedProcCommand("wifi_off", "关闭无线网络"))
			return 0;
		WifiManager mWifiManager;
		mWifiManager = (WifiManager) GlobalContext.get().getSystemService(
				Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(false);
		String spk = NativeData.getResString("RS_CMD_WIFI_OFF");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onGLOBAL_CMD_MUTE_RECORD() {
		GlobalContext.get().sendBroadcast(
				new Intent("action.FIRE_UPDATE_MUTE_STATE"));
		return 0;
	}

	public int onGLOBAL_CMD_RETURN_HOME() {
		GlobalContext.get().startActivity(
				new Intent(Intent.ACTION_MAIN).addCategory(
						Intent.CATEGORY_LAUNCHER).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
		return 0;
	}

	public int onGLOBAL_CMD_TAKE_CAMERA() {
		GlobalContext.get().startActivity(
				new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		return 0;
	}

	public int onGLOBAL_CMD_OPEN_EDOG_VOICE() {
		Intent intentODV = new Intent("com.szcx.edog.voice.on");
		GlobalContext.get().sendBroadcast(intentODV);
		return 0;
	}

	public int onGLOBAL_CMD_CLOSE_EDOG_VOICE() {
		Intent intentCDV = new Intent("com.szcx.edog.voice.off");
		GlobalContext.get().sendBroadcast(intentCDV);
		return 0;
	}

	public int onGLOBAL_CMD_OPEN_EDOG() {
		ComponentName componet2 = new ComponentName("com.szcx.tugou",
				"com.szcx.tugou.DogMainActivity");
		Intent intentOD = new Intent();
		intentOD.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentOD.setComponent(componet2);
		GlobalContext.get().startActivity(intentOD);
		return 0;
	}
	
	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		JNIHelper.logd("recive: " + cmd + "-" + voiceString);

		Method m = null;
		try {
			m = CmdManager.class.getMethod("on" + cmd, String.class, String.class);
		} catch (Exception e) {
			JNIHelper.loge("no register command with result: " + cmd);
		}

		if (m != null) {
			try {
				return (Integer) m.invoke(this, keywords, voiceString);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return super.onCommand(cmd, keywords, voiceString);
	}

	@Override
	public int onCommand(String cmd) {
		JNIHelper.logd("recive: " + cmd);

		Method m = null;
		try {
			m = CmdManager.class.getMethod("on" + cmd);
		} catch (Exception e) {
			JNIHelper.loge("no register command: " + cmd);
		}

		if (m != null) {
			try {
				return (Integer) m.invoke(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return super.onCommand(cmd);
	}
}
