package com.txznet.txz.module.music.focus;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZStatusManager.AudioLogicType;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.volume.AudioServiceAdapter;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.text.TextUtils;

import static com.txznet.sdk.TXZStatusManager.AudioLogicType.AUDIO_LOGIC_NONE;

/**
 * 音频焦点控制类
 * @author telenewbie
 *
 */
public class MusicFocusManager {
	//##创建一个单例类##
    private volatile static MusicFocusManager singleton;
    private MusicFocusManager (){}
    public static MusicFocusManager getInstance() {
        if (singleton == null) {
            synchronized (MusicFocusManager.class) {
                if (singleton == null) {
                    singleton = new MusicFocusManager();
                }
            }
        }
        return singleton;
    }
	public boolean requestAudioFocus(AudioLogicType type) {
		if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
			JNIHelper.logd(MusicManager.TAG + "intercept sence:Focus by remote:" + true);
			ServiceManager.getInstance().sendInvoke(mAudioFocusLogicService, "status.focus.RequestAudioFocus", null,
					null);
			return true;
		}
		boolean ret = requestAudioFocus(type, afChangeListener,
				mAudioFocusStreamType == EMPTY_FOCUS_TYPE ? AudioManager.STREAM_MUSIC : mAudioFocusStreamType);

		if (ret == false) {
			JNIHelper.logw(MusicManager.TAG + "request audio focus failed");
			onLostAudioFocus();
		} else {
			mHasAudioFocus = true;
		}

		return ret;
	}

	private AudioLogicType mAudioLogicWhenTts = AudioLogicType.AUDIO_LOGIC_PAUSE;
	private AudioLogicType mAudioLogicWhenAsr = AudioLogicType.AUDIO_LOGIC_PAUSE;
	private AudioLogicType mAudioLogicWhenCall = AudioLogicType.AUDIO_LOGIC_PAUSE;

	public AudioLogicType getAudioLogicWhenTts() {
		return mAudioLogicWhenTts;
	}

	public void setAudioLogicWhenTts(AudioLogicType audioLogicWhenTts) {
		mAudioLogicWhenTts = audioLogicWhenTts;
	}

	public AudioLogicType getAudioLogicWhenAsr() {
		return mAudioLogicWhenAsr;
	}

	public void setAudioLogicWhenAsr(AudioLogicType audioLogicWhenAsr) {
		mAudioLogicWhenAsr = audioLogicWhenAsr;
	}

	public AudioLogicType getAudioLogicWhenCall() {
		return mAudioLogicWhenCall;
	}

	public void setAudioLogicWhenCall(AudioLogicType audioLogicWhenCall) {
		mAudioLogicWhenCall = audioLogicWhenCall;
	}

	private boolean isNeedReleaseAudioFocus(AudioLogicType audioLogicType) {
    	if (hasAudioFocus()) {
    		return true;
		}
		if (audioLogicType == AUDIO_LOGIC_NONE) {
    		return false;
		}
    	return true;
	}

	private boolean requestAudioFocus(AudioLogicType type, OnAudioFocusChangeListener listener, int stream) {
		boolean ret = true;
		switch (type) {
		case AUDIO_LOGIC_NONE:
			break;
		case AUDIO_LOGIC_DUCK:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm.requestAudioFocus(listener, stream,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			AudioServiceAdapter.enableFocusControlTemp(false);
			break;
		case AUDIO_LOGIC_PAUSE:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm.requestAudioFocus(listener, stream,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			AudioServiceAdapter.enableFocusControlTemp(false);
			break;
		case AUDIO_LOGIC_STOP:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm.requestAudioFocus(listener, stream,
					AudioManager.AUDIOFOCUS_GAIN);
			AudioServiceAdapter.enableFocusControlTemp(false);
			break;
		case AUDIO_LOGIC_MUTE:
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AudioServiceAdapter.enableFocusControlTemp(true);
			ret = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAm.requestAudioFocus(listener, stream,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
			AudioServiceAdapter.enableFocusControlTemp(false);
			break;
		default:
			break;
		}
		JNIHelper.logd(MusicManager.TAG + "REQUESTFOCUS:" + type.toString() + " stream=" + stream + "/" + ret);
		return ret;
	}

	private Runnable mRunnableReleaseAudioFocus = new Runnable() {
		@Override
		public void run() {

			WakeupManager.getInstance().checkUsingAsr(null, new Runnable() {
				@Override
				public void run() {
					JNIHelper.logd(MusicManager.TAG + "releaseFocus  /" + mAudioFocusLogicService);
					if (!TtsManager.getInstance().isBusy() && !AsrManager.getInstance().isBusy()
							&& (mAudioFocusStreamType != 0 || CallManager.getInstance().isIdle())
							&& !RecorderWin.isOpened() && !AdvertisingManager.getInstance().openAdvertisingIsShow()
							&& !NewsManager.getInstance().isPlaying()
							&& !NewsManager.getInstance().isBuffering()
					) {
						if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
							ServiceManager.getInstance().sendInvoke(mAudioFocusLogicService,
									"status.focus.AbandonAudioFocus", null, null);
						} else {
							AudioManager mAm = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
							mAm.abandonAudioFocus(afChangeListener);
							mHasAudioFocus = false;
						}
						JNIHelper.logd(MusicManager.TAG + "abandonAudioFocus");
						// 音频焦点释放时尝试去掉“停止播报”的注册 by pppi 2017-12-25
	                    RecorderWin.delInterruptKws();
					}
					JNIHelper.logd(MusicManager.TAG + "releaseFocus  end");
				}
			});
		}
	};

	public void releaseAudioFocusImmediately() {
		AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
		mRunnableReleaseAudioFocus.run();
	}

	public void releaseAudioFocus(AudioLogicType audioLogicType) {
		if (isNeedReleaseAudioFocus(audioLogicType)) {
			AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
			AppLogic.runOnBackGround(mRunnableReleaseAudioFocus, 500);
		}
	}

	AudioManager mAm = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);

	boolean mHasAudioFocus = false;

	OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			JNIHelper.logd(MusicManager.TAG + "foucsChange:" + focusChange);
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				mHasAudioFocus = false;
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				mHasAudioFocus = false;
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
				mHasAudioFocus = true;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				mHasAudioFocus = true;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
				mHasAudioFocus = true;
			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
				mHasAudioFocus = true;
			}
			
			NewsManager.getInstance().onAudioFocusChange(focusChange);//新闻业务需要这个焦点
			
			if (!mHasAudioFocus) {
				JNIHelper.logw("onLostAudioFocus");
				onLostAudioFocus();
			}
		}
	};

	public boolean hasAudioFocus() {
        if (mAudioLogicWhenTts == AudioLogicType.AUDIO_LOGIC_NONE) {
            return true;
        }
		return mHasAudioFocus;
	}

	void onLostAudioFocus() {
		JNIHelper.logw(MusicManager.TAG + "onLostAudioFocus");
		mHasAudioFocus = false;
		if (mAudioFocusStreamType == EMPTY_FOCUS_TYPE)
			return;
		mAm.abandonAudioFocus(afChangeListener);
		TtsManager.getInstance().errorCurTask();
		AsrManager.getInstance().cancel();
		// RecordManager.getInstance().stop();
		RecordManager.getInstance().cancel();
		WinRecord.getInstance().dismiss();
	}
	

	String mAudioFocusLogicService = null;
	public static final int EMPTY_FOCUS_TYPE=-1;
	int mAudioFocusStreamType = EMPTY_FOCUS_TYPE;
	public void setFocusLogic(String packageName){
		mAudioFocusLogicService=packageName;
	}
	public void clearFocusLogic(){
		mAudioFocusLogicService=null;
	}
	public void setmAudioFocusStreamType(int mAudioFocusStreamType) {
		this.mAudioFocusStreamType = mAudioFocusStreamType;
	}
	public int getmAudioFocusStreamType() {
		return mAudioFocusStreamType;
	}
	
	public int speakDelay(){
		return mAudioFocusStreamType==EMPTY_FOCUS_TYPE ? 0 : 500;
	}
	
	public void notifyAudioFocusChangeByRemote(int focusChange){
		JNIHelper.logd(MusicManager.TAG + "foucsChange:" + focusChange);
		if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
			mHasAudioFocus = false;
		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			mHasAudioFocus = false;
		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
			mHasAudioFocus = true;
		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
			mHasAudioFocus = true;
		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
			mHasAudioFocus = true;
		} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
			mHasAudioFocus = true;
		}

		NewsManager.getInstance().onAudioFocusChange(focusChange);//新闻业务需要这个焦点

		if (!mHasAudioFocus) {
			JNIHelper.logw("onLostAudioFocus");
			onLostAudioFocus();
		}
	}
}
