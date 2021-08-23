package com.txznet.txz.component.asr.mix;

import android.os.Bundle;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.AsrType;

public class AsrMsgConstants {
	public final static String ENGINE_TYPE_YZS_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrYunzhishengImpl";
	public final static String ENGINE_TYPE_YZS_LOCAL_IMPL = "com.txznet.txz.component.asr.mix.local.LocalAsrYunzhishengImpl";
	public final static String ENGINE_TYPE_YZS_MIX_IMPL = "";
	public final static String ENGINE_TYPE_IFLYTEK_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrIflytekImpl";
	public final static String ENGINE_TYPE_SOGOU_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrSogouImpl";
	public final static String ENGINE_TYPE_BAIDU_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrBaiduImpl";
	public final static String ENGINE_TYPE_PACHIRA_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrPachiraImpl";
	public final static String ENGINE_TYPE_TENCENT_NET_IMPL = "com.txznet.txz.component.asr.mix.net.NetAsrTencentImpl";
	public final static String ENGINE_TYPE_UVOICE_LOCAL_IMPL = "com.txznet.txz.component.asr.mix.local.LocalAsrUVoiceImpl";
	public final static String ENGINE_TYPE_TXZASR_NET_IMPL = "com.txznet.txz.component.asr.txzasr.AsrTxzImpl";
	public final static int ENGINE_TYPE_YZS_NET = UiEquipment.AET_YZS;
	public final static int ENGINE_TYPE_YZS_LOCAL = UiEquipment.AET_YZS_FIX;
	public final static int ENGINE_TYPE_YZS_MIX = 101; 
	public final static int ENGINE_TYPE_IFLYTEK_NET = UiEquipment.AET_IFLY;
	public final static int ENGINE_TYPE_SOUGOU_NET = UiEquipment.AET_SOGOU;
	public final static int ENGINE_TYPE_BAIDU_NET = UiEquipment.AET_BAIDU;
	public final static int ENGINE_TYPE_PACHIRA_NET = UiEquipment.AET_PACHIRA;
	public final static int ENGINE_TYPE_TENCENT_NET = UiEquipment.AET_TENCENT;
	public final static int ENGINE_TYPE_UVOICE_LOCAL = UiEquipment.AET_UVOICE_FIX;
	
	public final static String ENGINE_TYPE_INT = "engine_type";
	//讯飞使用
	public final static String APPID_STR = "appId";
	
	//云知声使用
	public final static String APPKEY_STR = "appKey";
	public final static String SECRET_STR= "secret";
	
	public final static String ASR_INIT_RESULT_BOOL = "asr_init_result";
	public final static String ASR_VOLUME_CHANGE_INT = "asr_volume_change";
	public final static String ASR_RESULT_STR = "asr_rslt";
	public final static String ASR_RESULT_DATATYPE_INT = "asr_rslt_datatype";
	public final static String ASR_RESULT_VOICE_BYTEARRAY = "asr_rslt_voice";
	public final static String ASR_ERROR_INT = "asr_err";
	public final static String ASR_IMPORT_RESULT_BOOL = "asr_import_rslt";
	public final static String ASR_IMPORT_RESULT_CODE_INT = "asr_import_rslt_code";
	public final static String ASR_IMPORT_WORD_TYPE_STR= "asr_import_word_type";
	public final static String ASR_IMPORT_WORD_CONTENT_STR = "asr_import_word_content";
	public final static String ASR_IMPORT_KEYWORD_BYTEARRAY = "asr_import_keyword";
	
	public final static String YZS_ACTIVATOR = "yzs_activator";
	
	// asr_option
	public final static String ASR_OPTION_JSON_STR = "asr_opt_json";
	public final static String ASR_OPTION_ID_INT = "opt_id";
	public final static String ASR_OPTION_MANUAL_BOOL = "opt_manual";
	public final static String ASR_OPTION_LANGUAGE_STR = "opt_language";
	public final static String ASR_OPTION_ACCENT_STR = "opt_accent";
	public final static String ASR_OPTION_BOS_INT = "opt_bos";
	public final static String ASR_OPTION_EOS_INT = "opt_eos";
	public final static String ASR_OPTION_SPEECH_TIMEOUT_INT = "opt_speech_timeout";
	public final static String ASR_OPTION_GRAMMAR_INT = "opt_grammar";
	public final static String ASR_OPTION_ASRTYPE_STR = "opt_asrtype";
	public final static String ASR_OPTION_NEEDSTOPWAKEUP_BOOL = "opt_needstopwakeup";
	public final static String ASR_OPTION_VOICEID_LONG = "opt_voiceid";
	public final static String ASR_OPTION_BEGIN_SPEECH_TIME_LONG = "opt_begin_speech_time";
	public final static String ASR_OPTION_TTS_ID_INT = "opt_tts_id";
	public final static String ASR_OPTION_SERVER_TIME = "opt_server_time";
	public final static String ASR_OPTION_UID = "opt_uid";
	public final static String ASR_OPTION_PLAY_BEEP_SOUND_BOOL = "opt_play_beep_sound";

	public final static String ASR_ARGUMENT_GENERAL_CITY_STR = "asr_arg_general_city";
	public final static String ASR_ARGUMENT_GENERAL_GPSINFO_STR = "asr_arg_general_gpsinfo";
	public final static String ASR_ARGUMENT_GENERAL_ENCRYPT_KEY = "asr_arg_general_encryptkey";
	public final static String ASR_ARGUMENT_GENERAL_SAVE_VOICE = "asr_arg_general_save_voice";
	public final static String ASR_ARGUMENT_GENERAL_SAVE_RAW_PCM = "asr_arg_general_save_raw_pcm";
	public final static String ASR_ARGUMENT_USE_SE_PREPROCESSED_DATA_BOOLEAN = "use_se_preprocessed_data";
	
	public final static String ASR_PROJECT_CFG_AEC_TYPE_INT = "asr_project_cfg_aec_type";
	
	public final static String ASR_MONITOR_ATTR = "asr_monitor_attr";

	public final static String ASR_PARTIAL_RESULT_KEY = "asr_partial_result_key";

	public final static int MSG_REQ_EXIT = 0;//退出
	public final static int MSG_REQ_INIT_WITH_APP_ID = 1; // 使用AppId初始化
	public final static int MSG_REQ_START = 2; // 启动识别
	public final static int MSG_REQ_STOP = 3; // 停止录音
	public final static int MSG_REQ_CANCEL = 4; // 取消识别
    public final static int MSG_REQ_IMPORT_WORDS = 5;//编译词表
    
	public final static int MSG_NOTIFY_INIT_RESULT = 100; // 初始化结果通知
	public final static int MSG_NOTIFY_SET_IMPORT_WORDS_DONE = 101; // 编译词表结束通知

	public final static int MSG_NOTIFY_VOLUME = 200; // 音量通知
	
	public final static int MSG_NOTIFY_RESULT = 201; //识别结果通知
	public final static int MSG_NOTIFY_ERROR = 202; //识别出错通知
	public final static int MSG_NOTIFY_CANCEL = 203; //识别取消通知
	public final static int MSG_NOTIFY_ABORT = 204; //识别异常通知
	public final static int MSG_NOTIFY_PARTIAL_RESULT = 205;//流式识别结果
	
	public final static int MSG_NOTIFY_SPEECH_BEGIN = 400; //检测到用户开始说话
	public final static int MSG_NOTIFY_SPEECH_END = 401; // 检测到用户说话结束
	
	public final static int MSG_NOTIFY_RECORDING_BEGIN = 402; //录音开始
	public final static int MSG_NOTIFY_RECORDING_END = 403; //录音结束
	
    public final static int MSG_NOTIFY_MONITOR = 500;//监控上传
	
	public static void fillOption(AsrOption oOption, Bundle b){
		oOption.mId = b.getInt(AsrMsgConstants.ASR_OPTION_ID_INT);
		oOption.mAccent = b.getString(AsrMsgConstants.ASR_OPTION_ACCENT_STR);
		oOption.mBOS = b.getInt(AsrMsgConstants.ASR_OPTION_BOS_INT);
		oOption.mEOS = b.getInt(AsrMsgConstants.ASR_OPTION_EOS_INT);
		oOption.mAsrType = AsrType.valueOf(b.getString(AsrMsgConstants.ASR_OPTION_ASRTYPE_STR));
		oOption.mGrammar = b.getInt(AsrMsgConstants.ASR_OPTION_GRAMMAR_INT);
		oOption.mKeySpeechTimeout = b.getInt(AsrMsgConstants.ASR_OPTION_SPEECH_TIMEOUT_INT);
		oOption.mLanguage = b.getString(ASR_OPTION_LANGUAGE_STR);
		oOption.mManual = b.getBoolean(AsrMsgConstants.ASR_OPTION_MANUAL_BOOL);
		oOption.mNeedStopWakeup = b.getBoolean(AsrMsgConstants.ASR_OPTION_NEEDSTOPWAKEUP_BOOL);
		oOption.mTtsId = b.getInt(ASR_OPTION_TTS_ID_INT);
		oOption.mPlayBeepSound = b.getBoolean(ASR_OPTION_PLAY_BEEP_SOUND_BOOL);
	}
	
	public static void fillBundle(Bundle b, AsrOption oOption){
		b.putInt(AsrMsgConstants.ASR_OPTION_ID_INT, oOption.mId);
		b.putString(AsrMsgConstants.ASR_OPTION_ACCENT_STR, oOption.mAccent);
		b.putInt(AsrMsgConstants.ASR_OPTION_BOS_INT, oOption.mBOS);
		b.putInt(AsrMsgConstants.ASR_OPTION_EOS_INT, oOption.mEOS);
		b.putString(AsrMsgConstants.ASR_OPTION_ASRTYPE_STR, oOption.mAsrType.toString());
		b.putInt(AsrMsgConstants.ASR_OPTION_GRAMMAR_INT, oOption.mGrammar);
		b.putInt(AsrMsgConstants.ASR_OPTION_SPEECH_TIMEOUT_INT, oOption.mKeySpeechTimeout);
		b.putString(AsrMsgConstants.ASR_OPTION_LANGUAGE_STR, oOption.mLanguage);
		b.putBoolean(AsrMsgConstants.ASR_OPTION_MANUAL_BOOL, oOption.mManual);
		b.putBoolean(AsrMsgConstants.ASR_OPTION_NEEDSTOPWAKEUP_BOOL, oOption.mNeedStopWakeup);
		b.putInt(AsrMsgConstants.ASR_OPTION_TTS_ID_INT, oOption.mTtsId);
		b.putInt(AsrMsgConstants.ASR_OPTION_PLAY_BEEP_SOUND_BOOL, oOption.mTtsId);
	}
	
	public static String OptionToJson(AsrOption oOption){
		JSONBuilder jsonObject = new JSONBuilder();
		jsonObject.put(AsrMsgConstants.ASR_OPTION_ID_INT, oOption.mId);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_ACCENT_STR, oOption.mAccent);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_BOS_INT, oOption.mBOS);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_EOS_INT, oOption.mEOS);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_ASRTYPE_STR, oOption.mAsrType.toString());
		jsonObject.put(AsrMsgConstants.ASR_OPTION_GRAMMAR_INT, oOption.mGrammar);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_SPEECH_TIMEOUT_INT, oOption.mKeySpeechTimeout);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_LANGUAGE_STR, oOption.mLanguage);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_MANUAL_BOOL, oOption.mManual);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_NEEDSTOPWAKEUP_BOOL, oOption.mNeedStopWakeup);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_BEGIN_SPEECH_TIME_LONG, oOption.mBeginSpeechTime);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_VOICEID_LONG, oOption.mVoiceID);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_TTS_ID_INT, oOption.mTtsId);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_UID, oOption.mUID);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_SERVER_TIME, oOption.mServerTime);
		jsonObject.put(AsrMsgConstants.ASR_OPTION_PLAY_BEEP_SOUND_BOOL, oOption.mPlayBeepSound);
		// LogUtil.logd("strJsonOption : " + jsonObject.toString());
		return jsonObject.toString();
	}
	
	public static AsrOption JsonToOption(String json){
		// LogUtil.logd("strJsonOption : " + json);
		AsrOption oOption = new AsrOption();
		JSONBuilder jsonObject = new JSONBuilder(json);
		oOption.mId = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_ID_INT, Integer.class);
		oOption.mAccent = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_ACCENT_STR, String.class);
		oOption.mBOS = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_BOS_INT, Integer.class);
		oOption.mEOS = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_EOS_INT, Integer.class);
		oOption.mAsrType = AsrType.valueOf(jsonObject.getVal(AsrMsgConstants.ASR_OPTION_ASRTYPE_STR, String.class));
		oOption.mGrammar = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_GRAMMAR_INT, Integer.class);
		oOption.mKeySpeechTimeout = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_SPEECH_TIMEOUT_INT, Integer.class);
		oOption.mLanguage = jsonObject.getVal(ASR_OPTION_LANGUAGE_STR, String.class);
		oOption.mManual = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_MANUAL_BOOL, Boolean.class);
		oOption.mNeedStopWakeup = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_NEEDSTOPWAKEUP_BOOL, Boolean.class);
		oOption.mBeginSpeechTime = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_BEGIN_SPEECH_TIME_LONG, Long.class);
		oOption.mVoiceID = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_VOICEID_LONG, Long.class);
		oOption.mTtsId = jsonObject.getVal(AsrMsgConstants.ASR_OPTION_TTS_ID_INT, Integer.class);
		oOption.mUID = jsonObject.getVal(ASR_OPTION_UID, Integer.class);
		oOption.mServerTime = jsonObject.getVal(ASR_OPTION_SERVER_TIME, Long.class);
		oOption.mPlayBeepSound = jsonObject.getVal(ASR_OPTION_PLAY_BEEP_SOUND_BOOL, Boolean.class);
		return oOption;
	}
}
