package com.txznet.txz.cfg;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.remote.TtsRemoteImpl;

public class DebugCfg {
	public static File getDebugRoot() {
		try {
			return new File(Environment.getExternalStorageDirectory(), "txz");
		} catch (Exception e) {
			return new File(".");
		}
	}
	
	public static boolean TENCENT_LOG_AI_DEBUG = new File(getDebugRoot(),
			"tencent_log.debug").exists();
	
	public static boolean YZS_LOG_DEBUG = new File(getDebugRoot(),
			"yzs_log.debug").exists();
	// public final static String IFLYTECK_LOG_DEBUG =
	// "/sdcard/txz/iflyteck_log.debug";

	// 保存唤醒的原始PCM缓存
	public static boolean SAVE_RAW_PCM_CACHE = new File(getDebugRoot(),
			"pcm_enable.debug").exists();
	
	//是否保存录音
	public static boolean SAVE_VOICE = new File(getDebugRoot(),
			"save_voice.debug").exists();

	// 禁用远程tts工具
	public static boolean DISABLE_REMOTE_TTS_TOOL = new File(getDebugRoot(),
			"disable_remote_tts_tool.debug").exists();
	
	public static boolean POISEARCH_ONWAY_TAG= new File(getDebugRoot(),
			"POISEARCH_ONWAY_TAG.debug").exists();
	
	// 禁用远程设置资源
	public static boolean DISABLE_REMOTE_SET_RES = new File(getDebugRoot(),
			"disable_remote_set_res.debug").exists();
	
	public static boolean ENABLE_TRACE_GPS = new File(getDebugRoot(),
			"ENABLE_TRACE_GPS.debug").exists();
	// 开启log
	public static boolean ENABLE_LOG = new File(getDebugRoot(),
			"log_enable_file").exists();
	//依照上庆，自动化测试功能的要求增加日志
	public static boolean ENABLE_TEST_LOG = new File(getDebugRoot(),
			"shangqing.av").exists();
	//开启上报日志
	public static boolean ENABLE_REPORT_LOG = new File(getDebugRoot(),
			"enable_report_log.debug").exists();
	//关闭语料上报
	public static boolean DISABLE_REPORT_CORPUS = new File(getDebugRoot(),
			"disable_report_corpus.debug").exists();
	//关闭除语料之外的行为上报
	public static boolean DISABLE_REPORT_ACTION = new File(getDebugRoot(),
			"disable_report_action.debug").exists();
	//调整上报的间隔为很短（上报间隔由10min调整至10s）
	public static boolean ENABLE_REPORT_INTERVAL_SHORT = new File(getDebugRoot(),
			"enable_report_interval_short.debug").exists();

	
	// 开启导航广播数据log
	public static boolean ENABLE_NAV_LOG = new File(getDebugRoot(), 
			"ENABLE_TRACE_NAV_INFO.debug").exists();

	public static boolean DISABLE_WAKEUPKW_THRESHOULD = new File(getDebugRoot(),
			"DISABLE_WAKEUPKW_THRESHOULD.debug").exists();

	public static boolean debug_yzs() {
		return YZS_LOG_DEBUG;
	}
	public static boolean PACHIRA_ENGINE_DEBUG = new File(getDebugRoot(),
			"pachira_engine.debug").exists();
	public static boolean ROADTRAFFIC_DIRECTION_DEBUG = new File(getDebugRoot(),
			"roadtraffic_direction.debug").exists();	
	public static boolean TYPING_EFFECT_DEBUG = new File(getDebugRoot(),
			"typing_effect.debug").exists();

	//开启地平线aec引擎的log
	public static boolean HRS_LOG_DEBUG = new File(getDebugRoot(),
			"hobot_log.debug").exists();

	public static boolean ENABLE_CONTACT_DEBUG = new File(getDebugRoot(),"contact_test.debug").exists();

	static {
		IntentFilter filter = new IntentFilter("com.txznet.txz.debugFlags");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String type = intent.getStringExtra("type");
				if ("pcm_enable.debug".equals(type)) {
					SAVE_RAW_PCM_CACHE = true;
				} else if ("yzs_log.debug".equals(type)) {
					YZS_LOG_DEBUG = true;
				} else if ("disable_remote_tts_tool.debug".equals(type)) {
					DISABLE_REMOTE_TTS_TOOL = true;
					TtsRemoteImpl.setRemoteTtsService(null);
				} else if ("disable_remote_set_res.debug".equals(type)) {
					DISABLE_REMOTE_SET_RES = true;
				}
			}
		}, filter);
	}
	
	public static void showDebugToast(String strTip){
		if (debug_yzs()){
			AppLogic.showToast(strTip);
			Intent intent = new Intent();
			intent.setAction("com.txznet.alldemo.intent.action.cmd");
			intent.putExtra("cmd", "parse_text");
			intent.putExtra("text_args", "<1>" + strTip);
			AppLogic.getApp().sendBroadcast(intent);
		}
	}
}
