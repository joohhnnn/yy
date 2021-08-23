package com.txznet.txz.component.ttsplayer.yzs;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.yunzhisheng_3_0.TxzAudioSourceImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.YZSErrorCode;
import com.unisound.client.ErrorCode;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

import java.io.File;
import java.util.Locale;

/**
 * 这个是支持暂停和继续播放的tts实现类
 */
public class TtsYunzhishengImpl implements ITts {
    TXZTtsPlayerManager.ITtsCallback mTtsCallback;

    private SpeechSynthesizer mTTSPlayer;
    private boolean mIsBusy = false;

    public static final String VOICER_NAME = "xiaoli";
    public static final boolean USE_LOCAL_TTS = true;
    private int VOICE_SPEED = 70;
    public static final int VOICE_PITCH = 50;
    public static final int VOICE_VOLUME = 100;
    public static final int SAMPLE_RATE = 8000;
    public static final int START_BUFFER_TIME = 300;//引擎默认值
    public static final String DEFAULT_BACKEND_MODEL =
            GlobalContext.get().getApplicationInfo().dataDir + "/data/backend_female";
    public static final String DEFAULT_FRONTEND_MODEL =
            GlobalContext.get().getApplicationInfo().dataDir + "/data/frontend_model";

    private static final int BACK_SILENCE_TIME =
            TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_TTS_BACK_BUFFER_TIME, 100);
            // 最大值为1000ms, 默认100ms
    private static final int FRONT_SILENCE_TIME =
            TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_TTS_FRONT_BUFFER_TIME, 0);
            // 默认0ms

    private final static int MAX_ALLOWED_OUT_FATAL_TIMES = 3;//最多允许连续出现打开音频输出设备出错的次数
    private int mOutFatalTimes = 0;//连续出现打开音频输出设备出错的次数

    private IAudioSource mAudioSource = null;
    IInitCallback mInitCallback = null;
    private final static String TTS_DICT_NAME = "tts_dict";

    @Override
    public void setVoiceSpeed(int speed) {
        if (speed < 0) {
            VOICE_SPEED = 0;
        } else if (speed > 100) {
            VOICE_SPEED = 100;
        } else {
            if (speed == 50) {
                VOICE_SPEED = 51;
            } else {
                VOICE_SPEED = speed;
            }
        }
        JNIHelper.logd("speed = " + speed + ", VOICE_SPEED = " + VOICE_SPEED);
    }

    @Override
    public int getVoiceSpeed() {
        return VOICE_SPEED;
    }

    @Override
    public int initialize(IInitCallback oRun) {
        mInitCallback = oRun;

        //AppID为空时
        if (ProjectCfg.getYunzhishengAppId() == null || ProjectCfg.getYunzhishengSecret() == null ||
                ProjectCfg.getYunzhishengAppId().isEmpty() ||
                ProjectCfg.getYunzhishengSecret().isEmpty()) {
            mTTSPlayer = null;
            JNIHelper.logw("AppId or Secret is Empty!!!");
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (mInitCallback != null) {
                        mInitCallback.onInit(true);
                    }
                }
            };
            AppLogic.runOnBackGround(run, 0);
            return 0;
        }
        mOutFatalTimes = 0;

        mTTSPlayer = new SpeechSynthesizer(GlobalContext.get(),
                ProjectCfg.getYunzhishengAppId(),
                ProjectCfg.getYunzhishengSecret());
        mTTSPlayer.setOption(SpeechConstants.TTS_SERVICE_MODE,
                SpeechConstants.TTS_SERVICE_MODE_LOCAL);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONTEND_MODEL_PATH, DEFAULT_FRONTEND_MODEL);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACKEND_MODEL_PATH, DEFAULT_BACKEND_MODEL);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_IS_DEBUG, true);

        /*****TTS支持用户字典，配置多音字发音*******/
        do {
            int yzs_sdk_version = ProjectCfg.YZS_SDK_VERSION;
            if (0 == yzs_sdk_version) {
                JNIHelper.logd("low version does not support tts_dict");
                break;
            }

            String strDictPath =
                    AppLogic.getApp().getApplicationInfo().dataDir + "/data/" + TTS_DICT_NAME;
            File file = new File(strDictPath);
            if (!file.exists()) {
                JNIHelper.logd("tts_dict does not exist");
                break;
            }

            JNIHelper.logd("load tts_dict");
            mTTSPlayer.setOption(SpeechConstants.TTS_KEY_USER_DICT_FILE_PATH, strDictPath);

        } while (false);


        mTTSPlayer.setTTSListener(new SpeechSynthesizerListener() {

            @Override
            public void onEvent(int type) {
                switch (type) {
                    case SpeechConstants.TTS_EVENT_INIT:
                        JNIHelper.logd("TTS_EVENT_INIT");
                        mAudioSource = new TxzAudioSourceImpl();
                        mTTSPlayer.setAudioSource(mAudioSource);
                        if (mInitCallback != null) {
                            Runnable oRun = new Runnable() {
                                @Override
                                public void run() {
                                    mInitCallback.onInit(true);
                                    mInitCallback = null;
                                }
                            };
                            AppLogic.runOnBackGround(oRun, 0);
                        }
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_START:
                        // 开始合成回调
                        JNIHelper.logd("beginSynthesizer");
                        break;
                    case SpeechConstants.TTS_EVENT_SYNTHESIZER_END:
                        // 合成结束回调
                        JNIHelper.logd("endSynthesizer");
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_BEGIN:
                        // 开始缓存回调
                        JNIHelper.logd("beginBuffer");
                        break;
                    case SpeechConstants.TTS_EVENT_BUFFER_READY:
                        // 缓存完毕回调
                        JNIHelper.logd("bufferReady");
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_START:
                        // 开始播放回调
                        JNIHelper.logd("onPlayBegin");
                        break;
                    case SpeechConstants.TTS_EVENT_PLAYING_END:
                        mOutFatalTimes = 0;//成功过就直接重置为0
                        // 播放完成回调
                        JNIHelper.logd("onPlayEnd");
                        if (mTtsCallback != null) {
                            Runnable oRun = new Runnable() {
                                @Override
                                public void run() {
                                    final TXZTtsPlayerManager.ITtsCallback cb = mTtsCallback;
                                    if (cb != null) {
                                        cb.onSuccess();
                                    }
                                    mIsBusy = false;
                                }
                            };
                            AppLogic.runOnBackGround(oRun, 0);
                        }
                        break;
                    case SpeechConstants.TTS_EVENT_PAUSE:
                        // 暂停回调
                        JNIHelper.logd("pause");
                        if (mTtsCallback != null) {
                            Runnable oRun = new Runnable() {
                                @Override
                                public void run() {
                                    final TXZTtsPlayerManager.ITtsCallback cb = mTtsCallback;
                                    if (cb != null) {
                                        cb.onPause();
                                    }
                                }
                            };
                            AppLogic.runOnBackGround(oRun, 0);
                        }
                        break;
                    case SpeechConstants.TTS_EVENT_RESUME:
                        // 恢复回调
                        JNIHelper.logd("resume");
                        if (mTtsCallback != null) {
                            Runnable oRun = new Runnable() {
                                @Override
                                public void run() {
                                    final TXZTtsPlayerManager.ITtsCallback cb = mTtsCallback;
                                    if (cb != null) {
                                        cb.onResume();
                                    }
                                }
                            };
                            AppLogic.runOnBackGround(oRun, 0);
                        }
                        break;
                    case SpeechConstants.TTS_EVENT_STOP:
                        // 停止回调
                        JNIHelper.logd("stop");
                        mIsBusy = false;
                        break;
                    case SpeechConstants.TTS_EVENT_RELEASE:
                        // 释放资源回调
                        JNIHelper.logd("release");
                        break;
                    case SpeechConstants.TTS_EVENT_SWITCH_FRONTEND_MODEL_SUCCESS:
                        // 切换TTS模型成功
                        JNIHelper.logd("TTS_EVENT_SWITCH_FRONTEND_MODEL_SUCCESS");
                        break;
                    default:
                        JNIHelper.logd("type =" + type);
                        break;
                }

            }

            @Override
            public void onError(int type, String errorMSG) {
                // 语音合成错误回调
                // if (mInitCallback != null) {
                //     JNIHelper.loge("onInit onError " + type + ": " + errorMSG);
                //     mInitCallback.onInit(false);
                //     mInitCallback = null;
                //     return;
                // }

                final int error = type;
                JNIHelper.loge("onError " + type + ": " + errorMSG);

                //处理切换离线TTS发音人失败的错误:设置回默认发音人
                YZSErrorCode oErrorCode = new YZSErrorCode(errorMSG);
                switch (oErrorCode.ErrorCode()) {
                    case ErrorCode.TTS_ERROR_OFFLINE_CHANGE_SPEAKER_FAIL:
                        JNIHelper
                                .logw("change offline tts speaker fail, we will recovery to " +
										"default speaker");
                        setTtsModel(null);
                        return;
                    case ErrorCode.TTS_ERROR_AUDIOSOURCE_OPEN://{"errorCode":-91101,
						// "errorMsg":"播放线程打开audioSource出错"}
                        mOutFatalTimes++;
                        JNIHelper.loge("open audiosource occur serious fatal mOutFatalTimes : " +
                                mOutFatalTimes);
                        if (mOutFatalTimes > MAX_ALLOWED_OUT_FATAL_TIMES) {
                            JNIHelper
                                    .loge("open audiosource occur serious fatal too much times, " +
											"we will restart process");
                            AppLogic.exit();
                        } else {
                            //回调出错的话，会导致没法进入识别等流程
                            if (mTtsCallback != null) {
                                Runnable oRun = new Runnable() {
                                    @Override
                                    public void run() {
                                        final TXZTtsPlayerManager.ITtsCallback cb = mTtsCallback;
                                        if (cb != null) {
                                            cb.onSuccess();
                                        }
                                        mIsBusy = false;
                                    }
                                };
                                AppLogic.runOnBackGround(oRun, 0);
                            }
                        }
                        return;
                    default:
                        JNIHelper.logw("other error : " + errorMSG);
                }

                if (mTtsCallback != null) {
                    Runnable oRun = new Runnable() {
                        @Override
                        public void run() {
                            final TXZTtsPlayerManager.ITtsCallback cb = mTtsCallback;
                            if (cb != null) {
                                cb.onError(error);
                            }
                            mIsBusy = false;
                        }
                    };
                    AppLogic.runOnBackGround(oRun, 0);
                }
            }
        });
        JNIHelper.logd("TTS init version : [" + mTTSPlayer.getVersion() + "]");
        int nRet = -1;
        // 初始化合成引擎
        nRet = mTTSPlayer.init(null);
        JNIHelper.logd("nRet = " + nRet);
        return 0;
    }

    @Override
    public void release() {
        //mTTSPlayer.release(arg0, arg1);
        mTTSPlayer = null;
    }

    @Override
    public int start(int iStream, String sText, TtsUtil.ITtsCallback oRun) {
        sText = ttsTextReplaceTel(sText);
        if (mTTSPlayer == null) {
            JNIHelper.logw("mTTSPlayer == null");
            mTtsCallback = (TXZTtsPlayerManager.ITtsCallback) oRun;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (mTtsCallback != null) {
                        mTtsCallback.onSuccess();
                        mIsBusy = false;
                    }
                }
            };
            AppLogic.runOnBackGround(run, 0);
            return 0;
        }

        JNIHelper.logd("streamtype = " + iStream);
        TxzAudioSourceImpl.setStreamType(iStream);
        // 参数设置
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_STREAM_TYPE, iStream);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_NAME, VOICER_NAME);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, VOICE_SPEED);

        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONT_SILENCE, FRONT_SILENCE_TIME);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACK_SILENCE, BACK_SILENCE_TIME);

        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_VOLUME, VOICE_VOLUME);

        //设置播报的缓冲时间
        if (mOption != null && mOption.mPlayStartBufferTime != null) {
            mTTSPlayer.setOption(SpeechConstants.TTS_KEY_PLAY_START_BUFFER_TIME,
                    mOption.mPlayStartBufferTime);
        }
        JNIHelper.logd("BUFFER_TIME : " +
                mTTSPlayer.getOption(SpeechConstants.TTS_KEY_PLAY_START_BUFFER_TIME));
        //mTTSPlayer.setAudioSource(new AudioSourceImpl());
        // 记录回调对象
        mTtsCallback = (TXZTtsPlayerManager.ITtsCallback) oRun;
        if (!sText.endsWith("。")) {
            sText = sText + "。";
        }
        JNIHelper.logd("speakText sText = " + sText);
        mTTSPlayer.playText(sText);

        mIsBusy = true;
        return ERROR_SUCCESS;

    }

    @Override
    public int pause() {
        if (mTTSPlayer == null) {
            JNIHelper.logw("mTTSPlayer == null");
            return ERROR_UNKNOW;
        }
        mTTSPlayer.pause();
        return ERROR_SUCCESS;
    }

    @Override
    public int resume() {
        if (mTTSPlayer == null) {
            JNIHelper.logw("mTTSPlayer == null");
            return ERROR_UNKNOW;
        }
        mTTSPlayer.resume();
        return ERROR_SUCCESS;
    }

    @Override
    public void stop() {
        if (mTTSPlayer == null) {
            JNIHelper.logw("mTTSPlayer == null");
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    if (mTtsCallback != null) {
                        mTtsCallback.onCancel();
                        mIsBusy = false;
                    }
                }
            };
            AppLogic.runOnBackGround(run, 0);
            return;
        }

        TXZTtsPlayerManager.ITtsCallback callback = mTtsCallback;
        mTtsCallback = null;
        mTTSPlayer.cancel();// to do ...
        mIsBusy = false;

        if (callback != null) {
            callback.onCancel();
        }
    }

    @Override
    public boolean isBusy() {
        return mIsBusy;
    }

    @Override
    public int setLanguage(Locale loc) {
        return ERROR_UNKNOW;
    }

    @Override
    public void setTtsModel(String ttsModel) {
        JNIHelper.logd("setTtsModel : " + ttsModel);
        if (mTTSPlayer == null) {
            return;
        }
        String backModel = "";
        if (TextUtils.isEmpty(ttsModel)) {
            backModel = DEFAULT_BACKEND_MODEL;
        } else {
            backModel = ttsModel;
        }
        File f = new File(backModel);
        if (!f.exists()) {
            JNIHelper.loge("model : " + ttsModel + " is not exist");
            return;
        }
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_SWITCH_BACKEND_MODEL_PATH, backModel);
    }

    private TTSOption mOption = new TTSOption();

    @Override
    public void setOption(TTSOption oOption) {
        if (oOption != null) {
            //设置缓冲时间
            if (oOption.mPlayStartBufferTime != null) {
                JNIHelper.logd("setOption  mPlayStartBufferTime : " + oOption.mPlayStartBufferTime);
                mOption.mPlayStartBufferTime = oOption.mPlayStartBufferTime;
            }

        }
    }

    //云知声引擎强制将需要播报电话号码的播报强制播报为一串数字，解决云知声数字串播报错误问题
    private String ttsTextReplaceTel(String text) {
        if (text.indexOf("电话") > -1 || text.indexOf("来电") > -1) {
            int temp[] = findLongestNumSubstring(text);
            //数字串长度大于5是才认为他是电话号码
            if (temp[0] >= 0 && (temp[1] - temp[0]) >= 8) {
                text = text.replace(text.substring(temp[0], temp[1]),
                        "<tel>" + text.substring(temp[0], temp[1]) + "</tel>");
            }
        }
        return text;
    }

    private int[] findLongestNumSubstring(String input) {
        // If the string is empty, return [-1, -1] directly.
        if (input == null || input.length() == 0) {
            return new int[]{-1, -1};
        }

        int index = 0;
        int[] ret = new int[]{-1, -1}; //[start_index, length]
        int currLen = 0;
        while (index < input.length()) {
            currLen = 0;

            while (index < input.length() && Character.isDigit(input.charAt(index))) {
                currLen++;
                index++;
            }
            if (currLen != 0 && ret[1] <= currLen) {
                ret[0] = index - currLen;
                ret[1] = currLen + ret[0];
            }
            index++;
        }

        return ret;
    }

}
