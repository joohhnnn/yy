package com.txznet.txz.voice.aec;

import android.os.Handler;
import android.os.HandlerThread;

import com.hobot.hrs_api.HRSAPI;
import com.hobot.hrs_api.HRSAudioOutListener;
import com.hobot.hrs_api.HRSEventListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.util.recordcenter.QueueBlockingCache;
import com.txznet.txz.voice.IVoiceProcessor;

import java.util.Locale;

public class HobotAecImpl implements IVoiceProcessor {
	private static long mHrsHandler = 0;

	private String mLogFileDir = "/sdcard/hrsc/log"; //log保存目录
    private String mConfigDir = AppLogic.getApp().getApplicationInfo().dataDir + "/data/"; //配置文件路径
    private String mCfgFileDir = mConfigDir; //配置文件目录
    private String mLicFileDir = mConfigDir; //license目录

    private static final int BUFFER_SIZE = 512;

    static {
        try{
            if (ImplCfg.enableLoadHrscLib()) {
                LogUtil.logd("load hrsc");
                System.loadLibrary("hrsc");
            }
        } catch (Exception e){
            LogUtil.e("load hobot so failed");
        }
    }

    private HRSEventListener mEventListener = new HRSEventListener() {

        @Override
        public int OnEvent(int event, int val) {
            switch (event) {
                case HRSAPI.HRS_CB_EVENT_AUTH:
                    if (val == 1){
                        LogUtil.logd("hobot aec auth success");
                        LogUtil.d("hobot aec auth success");
                        final IEvent iEvent = mCallback;
                        if (iEvent != null){
                            iEvent.onEvent(IVoiceProcessor.AUTH_SUCCESS);
                        }
                    } else {
                        LogUtil.loge("hobot aec auth failed, code="+val);
                        LogUtil.e("hobot aec auth failed, code="+val);
                        final IEvent iEvent = mCallback;
                        if (iEvent != null){
                            iEvent.onEvent(IVoiceProcessor.AUTH_FAIL);
                        }
                    }
                    break;

                default:
                    break;
            }

            return 0;
        }
    };

    private HRSAudioOutListener mAudioOutListener = new HRSAudioOutListener() {
        @Override
        public int OnAudioOut(byte[] bytes, int i) {
            mReadBuffer.write(bytes, 0, i);
            return 0;
        }
    };

    private static HandlerThread mWorkThread;
    private static Handler mWorkHandler;
	/**
	 * 地平线声音预处理
	 * @param bufferSize - 输出的buffer大小，一般设为每次读取的长度的两倍
	 * @param refChannelRight - 参考信号所在的位置。false：左；true：右
	 */
	public HobotAecImpl(int bufferSize, boolean refChannelRight, IEvent callback) throws Exception {
        mCallback = callback; //为mCallback赋值需要放在设置回调之前
        if (mWorkHandler == null) {
            mWorkThread = new HandlerThread("HrscWorkThread");
            mWorkThread.start();
            mWorkHandler = new Handler(mWorkThread.getLooper());
        }
	    if (mHrsHandler == 0) {
            LogUtil.logd("init Hobot aec");
            HRSAPI.hrsPrepare(AppLogic.getApp().getApplicationContext());
            mHrsHandler = HRSAPI.hrsCreate();
            if (mHrsHandler <= 0) {
                LogUtil.logd("hobot aec init fail");
                throw new Exception("hobot.init.fail");
            }
            String version = HRSAPI.hrsGetStringOpt(mHrsHandler, HRSAPI.HRS_PARAM_VERSION_STRING);
            String uuid = String.format(Locale.CHINA, "%016d", ProjectCfg.getUid());
            HRSAPI.hrsSetStringOpt(mHrsHandler, HRSAPI.HRS_PARAM_UUID, uuid);
            LogUtil.logd("Hobot aec version: " + version + ", uuid = " + uuid);
            HRSAPI.hrsSetPath(mHrsHandler, mLogFileDir, mCfgFileDir, mLicFileDir);
            HRSAPI.hrsSetEventCB(mHrsHandler, mEventListener, "OnEvent"); //因为担心回调会在调用start方法之前调用，所以回调要放在start方法之前设置
            // if you use async audio output callback, set to 1, else set to 0 to use hrsRead to read data
            // when use hrsRead sync mode, the max read size is no longer than 80mS
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_ASYNC_OUTPUT, 1);
            // set output buffer size, generally set the size to 2x of one time read
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_AOUT_BUF_SIZE, bufferSize);
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_AIN_BUF_SIZE, bufferSize);
            // enable audio out
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_AUDIO_OUT, 1);
            /* Log信息打印级别 */
            /* 0 打印最多，同时记录格式转换前后音频数据和进出算法库的音频数据 */
            /* 1 打印最多，同时记录进出算法库的音频数据 */
            /* 如果不设置，则不打开log */
            int logLevel = DebugCfg.HRS_LOG_DEBUG ? 0 : 11;
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_LOG_LEVEL, logLevel);
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_MIC_CNT, 1);
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_REF_CNT, 1);
            // ref signal: 0: left channel, 1: right channel
            HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_REF_CHANNEL_1, refChannelRight ? 1 : 0);
            HRSAPI.hrsSetAudioOutCB(mHrsHandler, mAudioOutListener, "OnAudioOut");
            LogUtil.d("start");
            int code = HRSAPI.hrsStart(mHrsHandler);
            LogUtil.d("hobot start code = " + code);
        } else {
            HRSAPI.hrsSetEventCB(mHrsHandler, mEventListener, "OnEvent"); //为了让每次new的时候都能正确回调传进来的监听，所以每次都要设置一下回调
            emptyBuffer();
        }
	}

	private void write(byte[] data, int length) {
        int ret;
            ret = HRSAPI.hrsWrite(mHrsHandler, data, length);
            if (ret <= 0) {
                LogUtil.logd("hobot write failed, ret = " + ret);
            }
    }


	private void emptyBuffer(){
	    //停止处理音频时调用
        HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_FLAG_RESET_BUFI, 0);
        //重新开始处理音频时调用
        HRSAPI.hrsSetIntOpt(mHrsHandler, HRSAPI.HRS_PARAM_FLAG_RESET_BUFO, 0);
    }

	private static int offset = 30; //跳过30次的数据
    private static QueueBlockingCache mWriteBuffer = new QueueBlockingCache(32000);
    private static QueueBlockingCache mReadBuffer = new QueueBlockingCache(32000 * 5);
    @Override
    public byte[] process(byte[] audioIn, byte[] bytes1) {
        if (offset > 0) {
            offset --;
            return new byte[0];
        }
        mWriteBuffer.write(audioIn, 0, audioIn.length);
        mWorkHandler.post(mWriteTask);

        byte[] out = new byte[mReadBuffer.size()];
        if (out.length > 0) {
            mReadBuffer.read(out, 0, out.length, out.length);
        }
        return out;
    }

    private Runnable mWriteTask = new Runnable() {
        @Override
        public void run() {
            int n = mWriteBuffer.size() / 1024;
            if (n > 0) {
                byte[] data = new byte[1024 * n];
                int ret = mWriteBuffer.read(data, 0, data.length);
                if (ret > 0) {
                    write(data, ret);
                }
            }
        }
    };

    @Override
	public void release() {
	    LogUtil.logd("release Hobot aec");
        mWorkHandler.removeCallbacks(mWriteTask);
	}

	private IEvent mCallback;
    @Override
    public void setCallback(IEvent callback) {
        mCallback = callback;
    }

    @Override
    public int getType() {
        return 0;
    }
}
