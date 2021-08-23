package com.txznet.txz.component.tts.mix;

public class TtsMsgConstants {
	
	public final static int MSG_REQ_INIT = 1;	// 初始化TTS引擎
	public final static int MSG_REQ_START = 2;	// 播放tts文本
	public final static int MSG_REQ_STOP = 3;	// 停止播报tts
    public final static int MSG_REQ_RELEASE = 4;// 释放引擎资源

    public final static int MSG_NOTIFY_INIT_RESULT = 100;	// 初始化结果通知
    
    public final static int MSG_NOTIFY_CALLBACKE_END = 201;
    public final static int MSG_NOTIFY_CALLBACKE_CANCEL = 202;
    public final static int MSG_NOTIFY_CALLBACKE_SUCCESS = 203;
    public final static int MSG_NOTIFY_CALLBACKE_ERROR = 204;
    
    
    public final static String TTS_PKT_FILE_PATH_STR = "tts_pkt_file_path";
    public final static String TTS_START_STREAM_INT = "tts_stream";
    public final static String TTS_START_TEXT_STR = "tts_text";
    
	//讯飞使用
	public final static String APPID_STR = "appId";
	//云知声使用
	public final static String APPKEY_STR = "appKey";
	public final static String SECRET_STR= "secret";
	
    
    public final static String TTS_INIT_RESULT_BOOL = "tts_init_rslt";
    public final static String TTS_CALLBACK_ERROR_INT = "tts_callback_err";
    
    
 // tts引擎为空
    public final static int ERROR_CODE_SERVICE_DISCONNECTED = 9526;
    // tts引擎为空
    public final static int ERROR_CODE_TTS_NULL = 9527;
    

}
