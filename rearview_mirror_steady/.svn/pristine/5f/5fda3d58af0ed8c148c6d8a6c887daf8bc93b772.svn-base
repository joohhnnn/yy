package com.txznet.txz.module.cmd;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.txz.ui.data.UiData;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZSysManager.VolumeSettingCallBack;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.baidu.NavBaiduDeepImpl;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.component.nav.gaode.NavAmapAutoNavImpl;
import com.txznet.txz.component.nav.txz.NavApiImpl;
import com.txznet.txz.component.nav.txz.NavCommImpl;
import com.txznet.txz.component.nav.txz.NavTxzImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.constellation.ConstellationViewManager;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sys.SysTool;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.KeyEvent;

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
		regCommand("UI_TAB_BACK_NAV");
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
		regCommand("NAV_FOR_CHARGE");
		regCommand("SEARCH_USER_ID");
		regCommand("OPEN_FRONT_CAMERA");
		regCommand("OPEN_BACK_CAMERA");
		regCommand("CLOSE_FRONT_CAMERA");
		regCommand("CLOSE_BACK_CAMERA");
		regCommand("CHECK_LOGIN_STATE");
//		regCustomCommand("SIM_ASK_FLOW", UiEvent.EVENT_ACTION_EQUIPMENT,
//				UiEquipment.SUBEVENT_NOTIFY_SIM_QUERY_FLOW);
//		regCustomCommand("SIM_RECHANGE", UiEvent.EVENT_ACTION_EQUIPMENT,
//				UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE);
		regCommand("OPEN_NAV_NEWSPAPER");
		regCommand("CLOSE_NAV_NEWSPAPER");

		regCommandWithResult(SysTool.CUSTOM_CMD_OPEN_SCREEN);
		regCommandWithResult(SysTool.CUSTOM_CMD_CLOSE_SCREEN);
		regCommandWithResult(SysTool.CUSTOM_CMD_BACK_HOME);
//		regCommandWithResult("CUSTOM_CMD_BACK_NAVI");

		//音量设置
		regCommandWithResult("FEEDBACK_CMD_VOL_CTRLTO");
		regCommandWithResult("FEEDBACK_CMD_VOL_CTRLDOWN");
		regCommandWithResult("FEEDBACK_CMD_VOL_CTRLUP");


		//导航
		//前方路况
		regCommandWithResult("NAV_FRONT_TRAFFIC");
		//主辅路切换
		regCommandWithResult("NAV_SWITCH_MAIN_ROAD");
		regCommandWithResult("NAV_SWITCH_SIDE_ROAD");
		//刷新路线
		regCommandWithResult("NAV_REFRESH_PATH");
		//导航去收藏点
		regCommandWithResult("NAV_QUERY_COLLECTION_POINT");
		//打开精简导航
		regCommandWithResult("NAV_OPEN_SIMPLE_MODE");
		//关闭精简导航
		regCommandWithResult("NAV_CLOSE_SIMPLE_MODE");
		//进入组队界面
		regCommandWithResult("NAV_INTO_TEAM");

		return super.initialize_AfterStartJni();
	}

	/**
	 * 注册语音设置的指令
	 */
	public void addSettingCmd(){
		String[] cmd = NativeData.getResStringArray("RS_CMD_SETTING_OPEN");
		if (cmd != null && cmd.length > 0) {
			regCmdString("OPEN_VOICE_SETTING", cmd);
		}
	}

	@Override
	public int initialize_AfterInitSuccess() {
		if (!TextUtils.isEmpty(ProjectCfg.getSDKSettingPackage()) || PackageManager.getInstance().mInstalledSetting) {
			addSettingCmd();
		}
		return super.initialize_AfterInitSuccess();
	}

	@Override
	public int initialize_addPluginCommandProcessor() {
		addPluginCommandProcessor();
		return super.initialize_addPluginCommandProcessor();
	}

	public static ArrayMap<String, List<String>> sPluginCmdMap = new ArrayMap<String, List<String>>();

	public static void addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.cmd.", new CommandProcessor() {

			private List<String> objArrayTransform(Object[] objs) {
				if (objs != null && objs.length > 0) {
					List<String> strs = new ArrayList<String>();
					for (Object obj : objs) {
						if (obj instanceof String) {
							strs.add((String) obj);
						}
					}
					return strs;
				}
				return null;
			}

			@Override
			public Object invoke(String command, Object[] args) {
				if (TextUtils.equals(command, "regCommand")) {
					List<String> strs = objArrayTransform(args);
					if (strs != null && strs.size() > 1) {
						List<String> subList = strs.subList(1, strs.size());
						LogUtil.logd("reg cmd " + subList.toString()
								+ " prefix:" + strs.get(0));
						LogUtil.logd("reg cmd " + subList.toString()
								+ " prefix:" + strs.get(0));
						LogUtil.logd("plugin reg cmd " + subList.toString()
								+ " prefix:" + strs.get(0));
						sPluginCmdMap.put(strs.get(0), subList);
						ModuleManager.getInstance().regString(
								CmdManager.getInstance(), strs.get(0),
								subList.toArray(new String[subList.size()]));
					}
				} else if (TextUtils.equals(command, "unregCommand")) {
				}
				return null;
			}
		});
	}

	public int onTXZ_SERVER_CMD(String keywords, String voiceString) {
		VoiceParseData parseData = new VoiceParseData();
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		parseData.strText = voiceString;
		parseData.floatTextScore = TextResultHandle.TEXT_SCORE_INVALID;
		parseData.uint32Sence = VoiceData.GRAMMAR_SENCE_DEFAULT;
		RecorderWin.setState(RecorderWin.STATE.STATE_PROCESSING);
		TextResultHandle.getInstance().parseVoiceData(
				parseData,
				TextResultHandle.MODULE_YUNZHISHENG_MASK
						| TextResultHandle.MODULE_LOCAL_NORMAL_MASK, null);
		return 0;
	}

	public int onASK_FOR_NAME() {
		String[] names = WakeupManager.getInstance().mWakeupKeywords_User;
		if (names != null && names.length > 0
				&& (names[0].startsWith("你好") || names[0].startsWith("召见"))) {
			String txt = NativeData.getResString("RS_VOICE_MY_NAME_IS");
			String display = NativeData
					.getResString("RS_VOICE_MY_NAME_IS_DISPLAY");
			display = display.replace("%NAME%", names[0].substring(2)).replace(
					"%KW%", names[0]);
			String spk = txt.replace("%NAME%", names[0].substring(2)).replace(
					"%KW%", names[0]);
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextNotEqualsDisplay(spk, display);
			return 0;
		}
		String[] kws = WakeupManager.getInstance().getWakeupKeywords_Sdk();
		if (kws != null && kws.length > 0) {
			String txt = NativeData.getResString("RS_VOICE_WAKEUP_KEYWORDS_IS");
			String display = NativeData
					.getResString("RS_VOICE_WAKEUP_KEYWORDS_IS_DISPLAY");
			StringBuffer sb = new StringBuffer(kws[0]);
			for (int i = 1; i < kws.length; ++i) {
				sb.append('，');
				sb.append(kws[i]);
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextNotEqualsDisplay(txt.replace("%KW%", sb),
					display.replace("%KW%", sb));
			return 0;
		}
		String txt = NativeData.getResString("RS_VOICE_NO_NAME");
		RecorderWin.open(txt);
		return 0;
	}

	public int onNAV_FOR_REPAIR(){
		NavigateInfo info = new NavigateInfo();
		info.strTargetName = "4S店";
		NavManager.getInstance().navigateByName(info, false,PoiAction.ACTION_NAVI, false, true);
		return 0;
	}
	public int onNAV_FOR_CHARGE(){
		NavigateInfo info = new NavigateInfo();
		info.strTargetName = "充电桩";
		NavManager.getInstance().navigateByName(info, false,PoiAction.ACTION_NAVI, false, true);
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
        final String reason = NavManager.getInstance().getDisableResaon();

        if (!TextUtils.isEmpty(reason)) { // 没有导航工具
            if (NavManager.getInstance().preInvokeWhenNavNotExists(new Runnable() {
                @Override
                public void run() {
                    RecorderWin.speakTextWithClose(reason, null);
                }
            }));
            return 0;
        }

		if (!(NavManager.getInstance().getLocalNavImpl() instanceof NavTxzImpl)
				|| NavManager.getInstance().isNavi()) {
			NavManager.getInstance().openNav(false);
			return 0;
		}

		final String NAV_CACHE_NAME = ".txz_nav_info_cache";
		File file = new File(Environment.getExternalStorageDirectory(),
				NAV_CACHE_NAME);
		if (file.exists()) {
			String spk = NativeData.getResString("RS_CMD_OPEN_NAV");
			AsrManager.getInstance().setNeedCloseRecord(true);
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

    public int onUI_TAB_BACK_NAV() {
        final String reason = NavManager.getInstance().getDisableResaon();
        if (!TextUtils.isEmpty(reason)) { // 没有导航工具
            if (NavManager.getInstance().preInvokeWhenNavNotExists(new Runnable() {
                @Override
                public void run() {
                    RecorderWin.speakTextWithClose(reason, null);
                }
            }));
            return 0;
        }

		NavManager.getInstance().openNav(true);
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
		json.put("scene", "nav");
		json.put("text", voiceString);
		// json.put("keywords", keywords);
		json.put("action", "exit");
		if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
			return 0;
		}

		NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta != null && nta instanceof NavCldImpl) {
			if (!((NavCldImpl) nta).enableExitNav()) {
				// String answer =
				// NativeData.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
				// RecorderWin.speakTextWithClose(
				// answer.replace("%CONTENT%",
				// NativeData.getResString("RS_VOICE_USUAL_SPEAK_GRAMMAR")),
				// null);
				((NavCldImpl) nta).onNavCommand(false, "CANCEL", "取消路径");
				return 0;
			}
		}
		if (nta instanceof NavAmapAutoNavImpl) {
			if (nta.isInFocus()) {
				if (nta.isInNav()) {
					((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.CANCEL_NAV, keywords);
				} else {
					((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.EXIT_NAV, keywords);
				}
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
//        String spk = NativeData.getResString("RS_VOICE_MEDIA_CONTROL_CONFIRM");
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
		//被三方工具处理或原生返回true(正常增加音量)
		if (VolumeManager.getInstance().incVolume(new VolumeSettingCallBack() {

			@Override
			public void onOperateResult(boolean isMaxVolume) {
				String spk = null;
				if(isMaxVolume) {
					spk = NativeData.getResString("RS_CMD_VOL_MAX") ;
				} else{
					spk = NativeData.getResString("RS_CMD_VOL_UP");
				}
				RecorderWin.speakTextWithClose(spk, null);
			}

			@Override
			public void onError(int code) {
				RecorderWin.speakTextWithClose("音量调整失败", null);
			}
			
		})) {
			// 此处区分是true时是原生处理的还是三方处理的，如果没有设置工具，则需要进行增加音量的播报
			// 如是是原生处理则不能播报，否则回调里面会再报一次
			if (!SysTool.hasRemoteVolumeTool()) {
				String spk = NativeData.getResString("RS_CMD_VOL_UP");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}else{
			//音量管理返回false，交由默认处理
			String spk = NativeData.getResString("RS_CMD_VOL_MAX");
			RecorderWin.speakTextWithClose(spk, null);
		}
		return 0;
	}

	public int onFEEDBACK_CMD_VOL_DOWN() {
		if (SenceManager.getInstance().noneedProcCommand("vol_down", "降低音量"))
			return 0;
		if(VolumeManager.getInstance().decVolume(new VolumeSettingCallBack() {
			
			@Override
			public void onOperateResult(boolean isMinVolume) {
				String spk = null;
				if(isMinVolume){
					spk = NativeData.getResString("RS_CMD_VOL_MIN");
				}else{
					spk = NativeData.getResString("RS_CMD_VOL_DOWN");
				}
				RecorderWin.speakTextWithClose(spk, null);
			}
			
			@Override
			public void onError(int code) {
				RecorderWin.speakTextWithClose("音量调整失败", null);
			}
		})){
			// 此处区分是true时是原生处理的还是三方处理的，如果没有设置工具，则需要进行增加音量的播报
			// 如是是原生处理则不能播报，否则回调里面会再报一次
			if(!SysTool.hasRemoteVolumeTool()){
				String spk = NativeData.getResString("RS_CMD_VOL_DOWN");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}else{
			//音量管理返回false，交由默认处理
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

	public int onFEEDBACK_CMD_VOL_CTRLUP(String keywords, String value){
		if (!VolumeManager.getInstance().incVolume(new VolumeSettingCallBack() {
			@Override
			public void onOperateResult(boolean isOperateSuccess) {
				if (!isOperateSuccess) {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
				}
			}

			@Override
			public void onError(int errorCode) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
			}
        }, new JSONBuilder().put("data", value).toString())) {
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
		}
		return 0;
	}
	public int onFEEDBACK_CMD_VOL_CTRLDOWN(String keywords, String value){
		if (!VolumeManager.getInstance().decVolume(new VolumeSettingCallBack() {
			@Override
			public void onOperateResult(boolean isOperateSuccess) {
				if (!isOperateSuccess) {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
				}
			}

			@Override
			public void onError(int errorCode) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
			}
        }, new JSONBuilder().put("data", value).toString())) {
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
		}
		return 0;
	}
	public int onFEEDBACK_CMD_VOL_CTRLTO(String keywords, String value){
		if (!VolumeManager.getInstance().setVolume(new VolumeSettingCallBack() {
			@Override
			public void onOperateResult(boolean isOperateSuccess) {
				if (!isOperateSuccess) {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
				}
			}

			@Override
			public void onError(int errorCode) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
			}
        }, new JSONBuilder().put("data", value).toString())) {
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_CMD_VOL_ERROR"), null);
		}
		return 0;
	}


	public int onFEEDBACK_CMD_LIGHT_UP() {
		// 被场景处理
		if (SenceManager.getInstance().noneedProcCommand("light_up", "增加亮度"))
			return 0;
		if (SysTool.hasRemoteScreenLightTool()) {
			ServiceManager.getInstance().sendInvoke(SysTool.getRemoteScreenLightTool(),SysTool.SCREEN_LIGHT + "." + "isMaxLight", null,new GetDataCallback() {//

						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// data为空，兼容以前版本，无isMaxLight接口(超时)
							if (data == null) {
								if (isTimeout()){//是否为超时
									LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse time out,code = -1");
									String spk = "亮度调整失败";
									RecorderWin.speakTextWithClose(spk, null);
									return;
								} else {//兼容情况
									LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse getBytes = null,code = -2");
									String spk = "亮度调整失败";
									RecorderWin.speakTextWithClose(spk, null);
									return;
								}
							}
							if (data.getBytes() == null) {
								//兼容老版本，只能继续增加
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_up");
								String spk = NativeData.getResString("RS_CMD_LIGHT_UP");
								RecorderWin.speakTextWithClose(spk, null);
								return;
							}
							Boolean isMaxLight = data.getBoolean();
							if (isMaxLight == null) {// 正常情况到这一定会有，保险
								LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse getBoolean = null");
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_up");
								String spk = NativeData.getResString("RS_CMD_LIGHT_UP");
								RecorderWin.speakTextWithClose(spk, null);
								return;
							}
							if(isMaxLight){
								//已达到最大亮度
								String spk = NativeData.getResString("RS_CMD_LIGHT_MAX");
								RecorderWin.speakTextWithClose(spk, null);
							}else{
								//未达到最大亮度
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_up");
								String spk = NativeData.getResString("RS_CMD_LIGHT_UP");
								RecorderWin.speakTextWithClose(spk, null);
							}
						}
					});
			//返回，已经由三方工具处理
			return 0;
		}
		// 交由默认处理
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
		} else {
			String spk = NativeData.getResString("RS_CMD_LIGHT_MAX");
			RecorderWin.speakTextWithClose(spk, null);
		}
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_DOWN() {
		if (SenceManager.getInstance().noneedProcCommand("light_down", "降低亮度"))
			return 0;
		if (SysTool.hasRemoteScreenLightTool()) {
			ServiceManager.getInstance().sendInvoke(SysTool.getRemoteScreenLightTool(),SysTool.SCREEN_LIGHT + "." + "isMinLight", null,new GetDataCallback() {//

						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// data为空，兼容以前版本，无isMaxLight接口(超时)
							if (data == null) {
								if (isTimeout()){//是否为超时
									LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse time out,code = -1");
									String spk = "亮度调整失败";
									RecorderWin.speakTextWithClose(spk, null);
									return;
								} else {//兼容情况
									LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse getBytes = null,code = -2");
									String spk = "亮度调整失败";
									RecorderWin.speakTextWithClose(spk, null);
									return;
								}
							}
							if (data.getBytes() == null) {
								//兼容老版本，只能继续降低
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_down");
								String spk = NativeData.getResString("RS_CMD_LIGHT_DOWN");
								RecorderWin.speakTextWithClose(spk, null);
								return;
							}
							Boolean isMinLight = data.getBoolean();
							if (isMinLight == null) {// 正常情况到这一定会有，保险
								LogUtil.logd("onFEEDBACK_CMD_LIGHT_UP onGetInvokeResponse getBoolean = null");
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_down");
								String spk = NativeData.getResString("RS_CMD_LIGHT_DOWN");
								RecorderWin.speakTextWithClose(spk, null);
								return;
							}
							if(isMinLight){
								//已达到最小亮度
								String spk = NativeData.getResString("RS_CMD_LIGHT_MIN");
								RecorderWin.speakTextWithClose(spk, null);
							}else{
								//未达到最小亮度
								SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_down");
								String spk = NativeData.getResString("RS_CMD_LIGHT_DOWN");
								RecorderWin.speakTextWithClose(spk, null);
							}
						}
					});
			//返回，已经由三方工具处理
			return 0;
		}
		//由Android默认处理
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
		} else {
			String spk = NativeData.getResString("RS_CMD_LIGHT_MIN");
			RecorderWin.speakTextWithClose(spk, null);
		}
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_MAX() {
		if (SenceManager.getInstance().noneedProcCommand("light_max", "最大亮度"))
			return 0;
		if (SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_max")) {
			String spk = NativeData.getResString("RS_CMD_LIGHT_MAX_SETTING");
			RecorderWin.speakTextWithClose(spk, null);
			return 0;
		}
		Settings.System.putInt(GlobalContext.get().getContentResolver(),
				Settings.System.SCREEN_BRIGHTNESS, 255);
		String spk = NativeData.getResString("RS_CMD_LIGHT_MAX_SETTING");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onFEEDBACK_CMD_LIGHT_MIN() {
		if (SenceManager.getInstance().noneedProcCommand("light_min", "最小亮度"))
			return 0;
		if (SysTool.procByRemoteTool(SysTool.SCREEN_LIGHT, "light_min")) {
			String spk = NativeData.getResString("RS_CMD_LIGHT_MIN_SETTING");
			RecorderWin.speakTextWithClose(spk, null);
			return 0;
		}
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
        //String spk = NativeData.getResString("RS_CMD_WIFI_ON");
        String spk = NativeData.getResString("RS_CMD_WIFI_ON");
        final WifiManager mWifiManager;
        mWifiManager = (WifiManager) GlobalContext.get()
                .getSystemService(Context.WIFI_SERVICE);
        final int status =  mWifiManager.getWifiState();
        if(status != WifiManager.WIFI_STATE_DISABLED){
            spk = NativeData.getResString("RS_CMD_WIFI_ALREADY_ON");
        }
        RecorderWin.speakTextWithClose(spk, new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(
                            android.provider.Settings.ACTION_WIFI_SETTINGS);
                    if (intent != null
                            && status == WifiManager.WIFI_STATE_DISABLED) {
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
        String spk = NativeData.getResString("RS_CMD_WIFI_OFF");
        if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
            spk = NativeData.getResString("RS_CMD_WIFI_ALREADY_OFF");
        }
        mWifiManager.setWifiEnabled(false);
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

	public int onSEARCH_USER_ID() {
		if (SenceManager.getInstance().noneedProcCommand("search_uid", "设备号查询"))
			return 0;
		String spk = NativeData.getResString("EC_TIPS_SERACH_UID").replace(
				"%UID%", NativeData.getUID() + "");
		RecorderWin.speakTextWithClose(spk, null);
		return 0;
	}

	public int onOPEN_FRONT_CAMERA() {
		String spkHint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
		if (SysTool.procByRemoteToolSync(SysTool.COMMCMDS,
				"handle_front_camera", "true".getBytes())) {
			spkHint = NativeData.getResString("RS_CMD_OPEN_FRONT_CAMERA");
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spkHint, null);
		return 0;
	}

	public int onCLOSE_FRONT_CAMERA() {
		String spkHint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
		if (SysTool.procByRemoteToolSync(SysTool.COMMCMDS,
				"handle_front_camera", "false".getBytes())) {
			spkHint = NativeData.getResString("RS_CMD_CLOSE_FRONT_CAMERA");
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spkHint, null);
		return 0;
	}

	public int onOPEN_BACK_CAMERA() {
		String spkHint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
		if (SysTool.procByRemoteToolSync(SysTool.COMMCMDS,
				"handle_back_camera", "true".getBytes())) {
			spkHint = NativeData.getResString("RS_CMD_OPEN_FRONT_CAMERA");
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spkHint, null);
		return 0;
	}

	public int onCLOSE_BACK_CAMERA() {
		String spkHint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
		if (SysTool.procByRemoteToolSync(SysTool.COMMCMDS,
				"handle_back_camera", "false".getBytes())) {
			spkHint = NativeData.getResString("RS_CMD_CLOSE_BACK_CAMERA");
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spkHint, null);
		return 0;
	}
	
	public int onOPEN_VOICE_SETTING() {
        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.openVoiceSetting(true);
		return 0;
	}

	public int onCHECK_LOGIN_STATE() {
		boolean bRet = false;
		if (NetworkManager.getInstance().hasNet()) {
			try {
				byte[] byteData = NativeData.getNativeData(UiData.DATA_ID_CHECK_LOGIN);
				bRet = Integer.parseInt(new String(byteData)) != 0;
			} catch (Exception e) {
				bRet = false;
			}
		}

		if (bRet) {
			RecorderWin.speakText(NativeData.getResString("RS_CHECK_LOGIN_SUCCESS"),null);
		}else {
			RecorderWin.speakText(NativeData.getResString("RS_CHECK_LOGIN_FAIL"),null);
		}
		return 0;
	}
	
	public int onCLOSE_NAV_NEWSPAPER() {
		FakeReqManager.getInstance().setSmartTrafficSetting(false, false);
		return 0;
	}

	public int onOPEN_NAV_NEWSPAPER() {
		FakeReqManager.getInstance().setSmartTrafficSetting(true, false);
		return 0;
	}

	private boolean isExecuteNavAction(){
        //没有可用导航工具
        String resaon = NavManager.getInstance().getDisableResaon();
        if (!TextUtils.isEmpty(resaon)) {
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(resaon, null);
            return false;
        }
        //工具没有打开
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
        if (NavAppManager.getInstance().isAlreadyExitNav()) {
			AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ANSWER_OPEN_NAV"), null);
			return false;
        }
        return true;
    }

	public int onNAV_FRONT_TRAFFIC(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
		//可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 260) {
				((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.FRONT_TRAFFIC, keyword);
				return 0;
			}
		} else if (nta instanceof NavApiImpl) {
			((NavApiImpl) nta).onNavCommand(false, AsrKeyType.FRONT_TRAFFIC, keyword);
			return 0;
		} else if (nta instanceof NavCommImpl) {
			((NavCommImpl) nta).onNavCommand(false, AsrKeyType.FRONT_TRAFFIC, keyword);
			return 0;
		} else if (nta instanceof NavBaiduDeepImpl){
			//如果是百度导航的话应该单独处理，百度导航
			//先判断导航是否在导航中，如果不在导航中不可以查询前方路况
			if (nta.isInNav()){
				((NavBaiduDeepImpl) nta).speakFrontTraffic();
			}else {
				AsrManager.getInstance().setNeedCloseRecord(false);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_PLAN_ROUTE"), null);
			}
			return 0;
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_SWITCH_MAIN_ROAD(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
        //可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 294) {
				((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.SWITCH_MAIN_ROAD, keyword);
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_SWITCH_SIDE_ROAD(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
		//可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 294) {
				((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.SWITCH_SIDE_ROAD, keyword);
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_REFRESH_PATH(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
		//可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 294) {
				if (nta.isInNav()) {
					((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.REFRESH_PATH, keyword);
				} else {
					String spk = NativeData.getResString("RS_VOICE_NO_PLAN_ROUTE");
					RecorderWin.speakText(spk,null);
				}
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_QUERY_COLLECTION_POINT(String keyword,String value){
		//没有可用导航工具
		String resaon = NavManager.getInstance().getDisableResaon();
		if (!TextUtils.isEmpty(resaon)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(resaon, null);
			return 0;
		}
        //可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
        if (nta instanceof NavAmapAutoNavImpl) {
            if (((NavAmapAutoNavImpl) nta).getMapCode() >= 201) {
                ((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.QUERY_COLLECTION_POINT, keyword);
                return 0;
            }
        }

        //不支持该能力
        String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.speakTextWithClose(spk,null);
	    return 0;
    }

	public int onNAV_OPEN_SIMPLE_MODE(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
        //可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
        if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 201 && NavAmapAutoNavImpl.PACKAGE_NAME_LITE.equals(nta.getPackageName())) {
                if (nta.isInNav()) {
                	nta.enterNav();
                    ((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.OPEN_SIMPLE_MODE, keyword);
                } else {
                    String spk = NativeData.getResString("RS_VOICE_NO_PLAN_ROUTE");
                    RecorderWin.speakText(spk,null);
                }
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_CLOSE_SIMPLE_MODE(String keyword,String value){
        if (!isExecuteNavAction()) {
            return 0;
        }
        //可用工具
        NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
        if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 201 && NavAmapAutoNavImpl.PACKAGE_NAME_LITE.equals(nta.getPackageName())) {
                if (nta.isInNav()) {
                    ((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.CLOSE_SIMPLE_MODE, keyword);
                } else {
                    String spk = NativeData.getResString("RS_VOICE_NO_PLAN_ROUTE");
                    RecorderWin.speakText(spk,null);
                }
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}

	public int onNAV_INTO_TEAM(final String keyword, String value){
		//没有可用导航工具
		String resaon = NavManager.getInstance().getDisableResaon();
		if (!TextUtils.isEmpty(resaon)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(resaon, null);
			return 0;
		}
		//可用工具
		final NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta instanceof NavAmapAutoNavImpl) {
			if (((NavAmapAutoNavImpl) nta).getMapCode() >= 430) {
				final String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, new Runnable() {
					@Override
					public void run() {
						nta.enterNav();
						((NavAmapAutoNavImpl) nta).onNavCommand(false, AsrKeyType.INTO_TEAM, keyword);
					}
				});
				return 0;
			}
		}

		//不支持该能力
		String spk = NativeData.getResString("RS_VOICE_MAP_UNSUPPORT");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk,null);
		return 0;
	}
	
	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		JNIHelper.logd("recive: " + cmd + "-" + voiceString);

		if (sPluginCmdMap.containsKey(cmd)) {
			LogUtil.logd("beasr:contains");
			PluginManager.invoke(cmd + "onCommand", keywords, voiceString);
			return 0;
		}
		
		if (cmd.startsWith("CUSTOM_CMD_")) {
			onCustomCmds(cmd, voiceString);
			return 0;
		}
		
		Method m = null;
		try {
			m = CmdManager.class.getMethod("on" + cmd, String.class,
					String.class);
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
	
	private void onCustomCmds(String cmd,String command) {
		SysTool.invokeCmdSelect(false, cmd, command);
	}

	@Override
	public int onCommand(String cmd) {
		MtjModule.getInstance().event(MtjModule.EVENTID_CMD);
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
