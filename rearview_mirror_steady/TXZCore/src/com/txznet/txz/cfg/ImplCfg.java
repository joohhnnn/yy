package com.txznet.txz.cfg;

import android.text.TextUtils;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.mix.AsrLocalEngineController;
import com.txznet.txz.component.asr.mix.VoicePreProcessorController;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;

/**
 * 实现类配置管理
 * 
 * @author User
 *
 */
public class ImplCfg {
	// static String mTtsClass =
	// "com.txznet.txz.component.tts.ifly.TtsIflyImpl";
	// static String mAsrClass =
	// "com.txznet.txz.component.asr.ifly.AsrIflyImpl";
	static String mTtsClass = "com.txznet.txz.component.tts.yunzhisheng_3_0.TtsYunzhishengImpl";
	static String mAsrClass = "com.txznet.txz.component.asr.mix.AsrMixImpl";
	//static String mWakeupClass = "com.txznet.txz.component.wakeup.yunzhishengremote.WakeupYunzhishengRemoteImpl";
	static String mWakeupClass = "com.txznet.txz.component.wakeup.yunzhisheng_3_0.WakeupYunzhishengImpl";
	static String mPreWakeupClass = "com.txznet.txz.component.wakeup.sence.WakeupSenceProxy";
	static String mCallCalss = "com.txznet.txz.component.call.dxwy.CallToolImpl";

	public static void setTtsImplClass(String c) {
		LogUtil.logd("setTtsImplClass = " + c);
		if (c.contains("yunzhisheng")) {
			// default;
		} else {
			mTtsClass = c;
		}
	}

	public static String getTtsImplClass() {
		return mTtsClass;
	}

	public static void setAsrImplClass(String c) {
		mAsrClass = c;
	}

	public static String getAsrImplClass() {
		return mAsrClass;
	}

	public static void setWakeupImplClass(String c) {
		mWakeupClass = c;
	}

	public static String getWakeupImpClass() {
		return mWakeupClass;
	}
	
	public static void setPreWakeupImplClass(String c) {
		mPreWakeupClass = c;
	}

	public static String getPreWakeupImpClass() {
		return mPreWakeupClass;
	}
	

	public static void setCallImplClass(String c) {
		mCallCalss = c;
	}

	public static String getCallImplClass() {
		return mCallCalss;
	}

	public static boolean useHobotAec(){
		if (!enableHobot()){
			LogUtil.d("unable to use hobot");
			return false;
		}
		return VoicePreProcessorController.getInstance().getPreAECType() == UiEquipment.VPT_AEC_HRSC;
	}

	private static boolean enableHobot() {
		return new File(AppLogic.getApp().getApplicationInfo().dataDir + "/data/license.txt").exists() && new File(AppLogic.getApp().getApplicationInfo().dataDir + "/data/hisf_config.ini").exists();
	}

	public static boolean useUVoiceAsr() {
		LogUtil.d("AsrContainer", "determine if use UVoice Engine");
		if (AsrLocalEngineController.getInstance().getLocalAsrEngineType() == UiEquipment.AET_UVOICE_FIX) {
			if(!hasUVoiceResource()){
				LogUtil.logd("the UVoice model does not exist");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 检查声瀚声学模型、poi语言模型文件是否存在
	 * @return
	 */
	private static boolean hasUVoiceResource() {
		String decoderPath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_POI_DECODER_PATH);
		String rescorePath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ASR_UVOICE_POI_RESCORE_PATH);
		if (TextUtils.isEmpty(decoderPath)||TextUtils.isEmpty(rescorePath)){
			LogUtil.logi("the model path is null");
			return false;
		}
		boolean bAcoustic = new File(AppLogic.getApp().getApplicationInfo().dataDir + "/data/mix_chn_16k_20181128_v2_asrq08_vadq08.bin" ).exists();
		if (!bAcoustic){
			LogUtil.logi("the UVoice acoustic model does not exist");
			return false;
		}
		boolean bModel = new File(decoderPath).exists();
		boolean bRescore = new File(rescorePath).exists();
		return bModel & bRescore;
	}

	public static boolean enableLoadHrscLib(){
		return TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_HOBOT_NEED_LOAD_HRSC, true);
	}
}
