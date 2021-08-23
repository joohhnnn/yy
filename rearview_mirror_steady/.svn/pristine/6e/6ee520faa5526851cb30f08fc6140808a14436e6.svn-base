package com.txznet.txz.component.tts.baidu;

import com.baidu.tts.answer.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.tts.R;
import com.txznet.txz.component.tts.ITts;

public class BaiduTts  implements ITts{

    private SpeechSynthesizer mSpeechSynthesizer;
    private String mSampleDirPath;
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private boolean bInited = false;
    
    private synchronized int initTts() {
    	if (bInited){
    		LogUtil.logw("bInited = " + bInited);
    		return 0;
    	}
    	LoggerProxy.printable(true); 
    	this.mSampleDirPath = AppLogic.getApp().getApplicationInfo().dataDir + "/data";
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        this.mSpeechSynthesizer.setContext(AppLogic.getApp());
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(new SpeechSynthesizerListener() {
			@Override
			public void onSynthesizeStart(String arg0) {
				
			}
			
			@Override
			public void onSynthesizeFinish(String arg0) {
				
			}
			
			@Override
			public void onSynthesizeDataArrived(String arg0, byte[] arg1, int arg2) {
				
			}
			
			@Override
			public void onSpeechStart(String arg0) {
	
			}
			
			@Override
			public void onSpeechProgressChanged(String arg0, int arg1) {
				
			}
			
			@Override
			public void onSpeechFinish(String arg0) {
				 doSuccess();
			}
			
			@Override
			public void onError(String arg0, SpeechError arg1) {
				 doError();
			}
		});
        LogUtil.logd("mSampleDirPath : " + mSampleDirPath);
        // 文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        // 声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        // 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(AppLogic.getApp().getString(R.string.app_id));
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(AppLogic.getApp().getString(R.string.api_key), AppLogic.getApp().getString(R.string.api_secret));
        // 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
        
        // 授权检测接口(可以不使用，只是验证授权是否成功)
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
        if (authInfo.isSuccess()) {
            LogUtil.logd("auth success");
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            LogUtil.logd("auth failed errorMsg=" + errorMsg);
        }
        
        // 初始化tts
        int nRet = mSpeechSynthesizer.initTts(TtsMode.MIX);
        LogUtil.logd("nRet = " + nRet);
        bInited = nRet >= 0;
        return nRet;
    }
    
	@Override
	public void init(final IInitCallBack cb) {
		final int nRet = initTts();
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
			    if (cb != null){
			    	cb.onInit(nRet >= 0);
			    }
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	private ITtsCallBack mCallBack = null;
	@Override
	public void start(int stream, String text, ITtsCallBack cb) {
		if (!bInited){
			initTts();
		}
		mCallBack = cb;
		int nRet = -1;
		LogUtil.logd("speak " + text);
	    this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME,  "9");
	    this.mSpeechSynthesizer.setAudioStreamType(stream);
		nRet = mSpeechSynthesizer.speak(text);
		if (nRet < 0){
			LogUtil.loge("nRet = " + nRet);
			doError();
		}
	}

	@Override
	public void stop() {
		if (!bInited){
			return;
		}
		mSpeechSynthesizer.stop();
	}
	
	private void doError(){
		if (mCallBack != null){
			mCallBack.onError();
		}
	}
	
	private void doSuccess(){
		if (mCallBack != null){
			mCallBack.onSuccess();
		}
	}
}
