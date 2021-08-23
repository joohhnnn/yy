package com.txznet.txz.module.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.app.UiApp;
import com.txz.ui.audio.UiAudio;
import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaCategoryList;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZStatusManager.AudioLogicType;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.audio.txz.AudioImpl;
import com.txznet.txz.component.music.IMusic;
import com.txznet.txz.component.music.IMusic.MusicToolStatusListener;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.component.music.kaola.AdapterKaola;
import com.txznet.txz.component.music.kaola.MusicKaolaImpl;
import com.txznet.txz.component.music.kuwo.MusicKuwoImpl;
import com.txznet.txz.component.music.txz.AudioTxzImpl;
import com.txznet.txz.component.music.txz.MusicToAudioImpl;
import com.txznet.txz.component.music.txz.MusicTxzImpl;
import com.txznet.txz.component.selector.ISelectControl.OnItemSelectListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.tts.TtsManager.TtsTask;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.volume.AudioServiceAdapter;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.MediaControlUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.text.TextUtils;

/**
 * 音乐管理器，负责音乐逻辑处理及事件处理
 *
 * @author bihongpi
 */
public class MusicManager extends IModule {

    public static final String TAG = "CORE:MUSIC:";

    private static final String MusicPluginVersion = "1.0";

    protected static final String PLUGIN_TAG = "music:plugin:invoke:";

    List<String> mStatusPackages = new ArrayList<String>();

    static MusicManager sModuleInstance = new MusicManager();

    Runnable mRunnableRefreshMediaList = new Runnable() {
        @Override
        public void run() {
            AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
            JNIHelper.logd(TAG + "begin refresh media list");
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEED_REFRESH_MEDIA_LIST);
            AndroidMediaLibrary.refreshSystemMedia();
        }
    };

    BroadcastReceiver mRecviceSdcardEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            JNIHelper.logd(TAG + "recive sdcard event: " + intent.getAction());
            AppLogic.removeBackGroundCallback(mRunnableRefreshMediaList);
            AppLogic.runOnBackGround(mRunnableRefreshMediaList, 2000);
        }
    };

    MusicToolStatusListener mMusicStatusListener = new MusicToolStatusListener() {
        JSONBuilder jsonBuilder = new JSONBuilder();

        @Override
        public void playMusic(MusicModel mm) {
            String dataJson = mm != null ? mm.toString() : "";
            for (String pkn : mStatusPackages) {
                ServiceManager.getInstance().sendInvoke(pkn, "tool.music.status.playMusic", dataJson.getBytes(), null);
            }
        }

        @Override
        public void onStatusChange(int state) {
            for (String pkn : mStatusPackages) {
                ServiceManager.getInstance().sendInvoke(pkn, "tool.music.status.onStatusChange",
                        (state + "").getBytes(), null);
            }
        }

        @Override
        public void endMusic(MusicModel nextMudule) {
            String dataJson = nextMudule != null ? nextMudule.toString() : "";
            for (String pkn : mStatusPackages) {
                ServiceManager.getInstance().sendInvoke(pkn, "tool.music.status.endMusic", dataJson.getBytes(), null);
            }
        }

        @Override
        public void onProgress(int position, int duration) {
            jsonBuilder.put(PROCESS_POSITION, position);
            jsonBuilder.put(PROCESS_DURATION, duration);
            for (String pkn : mStatusPackages) {
                ServiceManager.getInstance().sendInvoke(pkn, "tool.music.status.onProgress", jsonBuilder.toBytes(),
                        null);
            }
        }
    };

    private MusicManager() {
        AudioTxzImpl.getAppVersion();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
        filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
        filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
        filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
        filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        GlobalContext.get().registerReceiver(mRecviceSdcardEvent, filter);

        final MusicKuwoImpl mmk = new MusicKuwoImpl();
        mmk.setStatusListener(mMusicStatusListener);

        final MusicKaolaImpl mkl = new MusicKaolaImpl();
        mkl.setStatusListener(mMusicStatusListener);

        mMusicInnerToolMap.put(MusicKuwoImpl.PACKAGE_NAME, mmk);
        mMusicInnerToolMap.put(MusicKaolaImpl.PACKAGE_NAME, mkl);
        LogUtil.logd(TAG + "AudioTxzImpl.newVersion::init()" + AudioTxzImpl.isNewVersion());
        if (AudioTxzImpl.isNewVersion()) {
            mMusicTxzImpl = new AudioTxzImpl();
        } else {
            mMusicTxzImpl = new MusicTxzImpl();
        }
        mMusicTxzImpl.setStatusListener(mMusicStatusListener);
        mMusicInnerToolMap.put(ServiceManager.MUSIC, mMusicTxzImpl);
    }

    IMusic mMusicTxzImpl;

    public static MusicManager getInstance() {
        return sModuleInstance;
    }

    // /////////////////////////////////////////////////////////////////////

    MediaModel mSearchMediaModel = null;
    boolean mSearchMoreResult = false;

    public void cancelSearchMedia() {
        mSearchMediaModel = null;
        mSearchMoreResult = false;

        // 取消其它音乐的搜索
        for (String musicKey : mMusicInnerToolMap.keySet()) {
            IMusic m = mMusicInnerToolMap.get(musicKey);
            if (m != null && PackageManager.getInstance().checkAppExist(m.getPackageName())) {
                m.cancelRequest();
            }
        }
    }

    public void searchMoreMedia(MediaModel model) {
        mSearchMediaModel = model;
        mSearchMoreResult = true;
        JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE, model);
    }

    // /////////////////////////////////////////////////////////////////////

    public void updateMusicList(byte[] path) {
        JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, 0x10, path);
    }

    /**
     * 是否正在播放音乐
     *
     * @return
     */
    public boolean isPlaying() {
        if (!ProjectCfg.isFixCallFunction()) {
            if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                return mRemoteMusicToolisPlaying;
            }
        }
        IMusic musicTool = getMusicTool();
        if (musicTool != null) {
            return musicTool.isPlaying();
        }
        return false;
    }

    /**
     * 是否正在缓冲中
     *
     * @return
     */
    public boolean isBuffering() {
        IMusic musicTool = getMusicTool();
        if (musicTool != null) {
            return musicTool.isBuffering();
        }
        return false;
    }

    // /////////////////////////////////////////////////////////////////////

    AudioLogicType mAudioLogicWhenTts = AudioLogicType.AUDIO_LOGIC_PAUSE;
    AudioLogicType mAudioLogicWhenAsr = AudioLogicType.AUDIO_LOGIC_PAUSE;
    AudioLogicType mAudioLogicWhenCall = AudioLogicType.AUDIO_LOGIC_PAUSE;

    private boolean requestAudioFocus(AudioLogicType type) {
        if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
            ServiceManager.getInstance().sendInvoke(mAudioFocusLogicService, "status.focus.RequestAudioFocus", null,
                    null);
            return true;
        }
        boolean ret = requestAudioFocus(type, afChangeListener,
                mAudioFocusStreamType == null ? AudioManager.STREAM_MUSIC : mAudioFocusStreamType);

        if (ret == false) {
            JNIHelper.logw(TAG + "request audio focus failed");
            onLostAudioFocus();
        } else {
            mHasAudioFocus = true;
        }

        return ret;
    }


    @Override
    public int initialize_addPluginCommandProcessor() {

        PluginManager.addCommandProcessor("com.music.command.", new CommandProcessor() {

            @Override
            public Object invoke(String command, Object[] params) {
                LogUtil.logd(PLUGIN_TAG + "command:" + command);
                if (StringUtils.isNotEmpty(command)) {
                    if (params != null && params.length > 0) {
                        return MusicManager.getInstance().invokeTXZMusic("com.txznet.music.plugin", command, (byte[]) params[0]);
                    }
                    if ("tool.packageName".equals(command)) {
                        IMusic tool = getMusicTool();
                        if (tool != null) {
                            return tool.getPackageName();
                        }
                        return null;
                    }
                    return MusicManager.getInstance().invokeTXZMusic("com.txznet.music.plugin", command, null);
                }

                return null;
            }
        });
        //音乐显示
        PluginManager.addCommandProcessor("com.music.default.", new CommandProcessor() {

            @Override
            public Object invoke(String command, Object[] params) {
                if ("showMusicList".equals(command)) {
                    List<AudioShowData> audioShowDatas = (List<AudioShowData>) params[0];
                    int delayTime = (Integer) params[1];
                    final OnItemSelectListener listener = (OnItemSelectListener) params[2];
//					SelectorHelper.entryMusicSelector(audioShowDatas, delayTime != 0, delayTime, listener,false);
                    ChoiceManager.getInstance().showMusicList(audioShowDatas, delayTime != 0, delayTime, new com.txznet.txz.component.choice.OnItemSelectListener<AudioShowData>() {

                        @Override
                        public boolean onItemSelected(boolean isPreSelect, AudioShowData v, boolean fromPage, int idx, String fromVoice) {
                            if (isPreSelect) {
                                return false;
                            }

                            if (listener != null) {
                                listener.onItemSelect(null, idx, v);
                            }
                            return false;
                        }
                    }, false);
                }
                return null;
            }
        });
        PluginManager.addCommandProcessor("txz.music.", new CommandProcessor() {
            @Override
            public Object invoke(String arg0, Object[] arg1) {
                if ("tool.packageName".equals(arg0)) {
                    IMusic tool = getMusicTool();
                    if (tool != null) {
                        return tool.getPackageName();
                    }
                    return null;
                }
                return null;
            }
        });

        PluginManager.addCommandProcessor("com.txznet.music.command.", new CommandProcessor() {

            @Override
            public Object invoke(String command, Object[] params) {
                JNIHelper.logd(TAG + "plugin:" + command);
                if (StringUtils.isNotEmpty(command)) {
                    if (params != null && params.length > 0) {
                        return MusicManager.getInstance().invokeTXZMusic("com.txznet.music.plugin", command,
                                (byte[]) params[0]);
                    }
                    return MusicManager.getInstance().invokeTXZMusic("com.txznet.music.plugin", command, null);
                }
                return null;
            }
        });
//		PluginManager.addCommandProcessor("com.txznet.music。resource", new CommandProcessor() {
//			
//			@Override
//			public Object invoke(String arg0, Object[] arg1) {
//				return null;
//			}
//		});


        return super.initialize_addPluginCommandProcessor();
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
        JNIHelper.logd(TAG + "REQUESTFOCUS:" + type.toString() + " stream=" + stream + "/" + ret);
        return ret;
    }

    private Runnable mRunnableReleaseAudioFocus = new Runnable() {
        @Override
        public void run() {

            WakeupManager.getInstance().checkUsingAsr(null, new Runnable() {
                @Override
                public void run() {
                    JNIHelper.logd(TAG + "releaseFocus  /" + mAudioFocusLogicService);
                    if (!TtsManager.getInstance().isBusy() && !AsrManager.getInstance().isBusy()
                            && (mAudioFocusStreamType != null || CallManager.getInstance().isIdle())
                            && !RecorderWin.isOpened()) {
                        JNIHelper.logd(TAG + "abandonAudioFocus,tts=" + (TtsManager.getInstance().isBusy()) + ",asr=" + (AsrManager.getInstance().isBusy()) + ",win=" + RecorderWin.isOpened() + ""
                                + ",focusStream=" + (mAudioFocusStreamType != null) + ",call=" + (CallManager.getInstance().isIdle()));
                        if (!TextUtils.isEmpty(mAudioFocusLogicService)) {
                            ServiceManager.getInstance().sendInvoke(mAudioFocusLogicService,
                                    "status.focus.AbandonAudioFocus", null, null);
                        } else {
	                        AudioManager mAm = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
	                        try {
	                            mAm.abandonAudioFocus(afChangeListener);
	                        } catch (Exception ignore) {
	                            JNIHelper.logd("Musicmanager : abandonAudioFocus Exception: " + ignore.getLocalizedMessage());
	                        }
	                        mHasAudioFocus = false;	                        
                        }
                        JNIHelper.logd(TAG + "abandonAudioFocus");
                        
                        // 音频焦点释放时尝试去掉“停止播报”的注册 by pppi 2017-12-25
                        RecorderWin.delInterruptKws();
                    }
                    JNIHelper.logd(TAG + "releaseFocus  end");
                }
            });
        }
    };

    public void releaseAudioFocusImmediately() {
        AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
        mRunnableReleaseAudioFocus.run();
    }

    private void releaseAudioFocus() {
        AppLogic.removeBackGroundCallback(mRunnableReleaseAudioFocus);
        AppLogic.runOnBackGround(mRunnableReleaseAudioFocus, 500);
    }

    AudioManager mAm = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);

    public boolean onBeginTts(int stream, TtsTask ttsTask) {
        JNIHelper.logd(TAG + "onBeginTts");
        BeepPlayer.cancelMusic();

        // 先发AsrEnd
        if (ProjectCfg.needStopWkWhenTts() || (ttsTask != null && ttsTask.isForceStopWakeup())) {
            WakeupManager.getInstance().enbaleVoiceChannel(false);
            // WakeupManager.getInstance().stop();
            // VolumeManager.getInstance().muteAll(false);
        }

        // 再发TtsBegin
        String command = "comm.status.onBeginTts";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        boolean result = requestAudioFocus(mAudioLogicWhenTts);
        ServiceManager.getInstance().broadInvoke(command, null);

        VolumeManager.getInstance().muteAll(false);
        return result;
    }

    public void onEndTts() {
        JNIHelper.logd(TAG + "onEndTts");
        BeepPlayer.playWaitMusic();
        TtsManager.getInstance().setTtsText("");
        TtsManager.getInstance().stopProWakeup();

        String command = "comm.status.onEndTts";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        ServiceManager.getInstance().broadInvoke(command, null);

        WakeupManager.getInstance().enbaleVoiceChannel(true);
        // WakeupManager.getInstance().startDelay(300);
        /*
         * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
		 */

        WakeupManager.getInstance().checkUsingAsr(new Runnable() {
            @Override
            public void run() {
                if (!InterruptTts.getInstance().isInterruptTTS()) {
                    if (WakeupManager.getInstance().isBusy())
                        RecorderWin.setState(STATE.STATE_RECORD);
                    VolumeManager.getInstance().muteAll(true);
                }
            }
        }, null);
        releaseAudioFocus();
    }

    boolean mHasAudioFocus = false;

    OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            JNIHelper.logd(TAG + "foucsChange:" + focusChange);
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mHasAudioFocus = false;
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                mHasAudioFocus = false;
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                mHasAudioFocus = false;
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mHasAudioFocus = true;
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                mHasAudioFocus = true;
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                mHasAudioFocus = true;
            }

            if (!mHasAudioFocus) {
                JNIHelper.logw("onLostAudioFocus");
                onLostAudioFocus();
            }
        }
    };

    public boolean hasAudioFocus() {
        return mHasAudioFocus;
    }

    void onLostAudioFocus() {
        JNIHelper.logw(TAG + "onLostAudioFocus");
        mHasAudioFocus = false;
        if (mAudioFocusStreamType == null)
            return;
        try {
            mAm.abandonAudioFocus(afChangeListener);
        } catch (Exception ignore) {
            JNIHelper.logd("Musicmanager : abandonAudioFocus Exception: " + ignore.getLocalizedMessage());
        }
        TtsManager.getInstance().errorCurTask();
        AsrManager.getInstance().cancel();
        // RecordManager.getInstance().stop();
        RecordManager.getInstance().cancel();
        WinRecord.getInstance().dismiss();
    }

    Runnable mRunnableProtectEndAsr = new Runnable() {
        @Override
        public void run() {
            AppLogic.removeBackGroundCallback(this);
            AppLogic.runOnBackGround(this, 5000);
            onEndAsr();
        }
    };

    public boolean onBeginAsr() {
        JNIHelper.logd(TAG + "onBeginAsr");
        boolean result = requestAudioFocus(mAudioLogicWhenAsr);
        BeepPlayer.cancelMusic();

        String command = "comm.status.onBeginAsr";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        ServiceManager.getInstance().broadInvoke(command, null);

        AppLogic.removeBackGroundCallback(mRunnableProtectEndAsr);
        AppLogic.runOnBackGround(mRunnableProtectEndAsr, 5000);

		/*
         * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
		 */

        return result;
    }

    public void onEndAsr() {
        JNIHelper.logd(TAG + "onEndAsr");
        if (AsrManager.getInstance().isBusy())
            return;

        Runnable runFalse = new Runnable() {

            @Override
            public void run() {
                if (RecordManager.getInstance().isBusy()) {
                    return;
                }
                AppLogic.removeBackGroundCallback(mRunnableProtectEndAsr);
                String command = "comm.status.onEndAsr";
                // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                // command,
                // null, null);
                ServiceManager.getInstance().broadInvoke(command, null);
                /*
                 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
				 */
                if (!InterruptTts.getInstance().isInterruptTTS()) {
                    VolumeManager.getInstance().muteAll(false);
                }
                releaseAudioFocus();
            }
        };
        if (!WakeupManager.getInstance().isBusy())
            runFalse.run();
        else
            WakeupManager.getInstance().checkUsingAsr(null, runFalse);

    }

    public void onEndBeep() {
        JNIHelper.logd(TAG + "onEndBeep");
        Runnable runTrue = new Runnable() {
            @Override
            public void run() {
                String command = "comm.status.onBeepEnd";
                ServiceManager.getInstance().broadInvoke(command, null);
                /*
                 * 识别的时候还是需要禁掉其他的声音，不然别扭 if (ProjectCfg.mEnableAEC) { return; }
				 */
                if (!InterruptTts.getInstance().isInterruptTTS()) {
                    VolumeManager.getInstance().muteAll(true);
                }
            }
        };

        if (AsrManager.getInstance().isBusy()) {
            runTrue.run();
        } else {
            WakeupManager.getInstance().checkUsingAsr(runTrue, null);
        }
    }

    public void onBeginCall() {
        JNIHelper.logd(TAG + "onBeginCall");
        String command = "comm.status.onBeginCall";
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, command, null, null);
        ServiceManager.getInstance().broadInvoke(command, null);

        if (mAudioFocusStreamType == null) {
            requestAudioFocus(mAudioLogicWhenCall);
        }
        TtsManager.getInstance().pause();
    }

    public void onEndCall() {
        JNIHelper.logd(TAG + "onEndCall");
        releaseAudioFocus();
        TtsManager.getInstance().resume();
        String command = "comm.status.onEndCall";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        ServiceManager.getInstance().broadInvoke(command, null);
    }

    public void onBeginMusic() {
        JNIHelper.logd(TAG + "onBeginMusic");
        String command = "comm.status.onBeginMusic";
        ServiceManager.getInstance().broadInvoke(command, null);
    }

    public void onEndMusic() {
        JNIHelper.logd(TAG + "onEndMusic");
        String command = "comm.status.onEndMusic";
        ServiceManager.getInstance().broadInvoke(command, null);
    }

    // /////////////////////////////////////////////////////////////////////

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SYNC_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE_RESULT);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PAUSE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_EXIT);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEXT);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PREV);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_RANDOM);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_HATE_CUR);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_ALL);

        regEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_RESP_DATA_INTERFACE);
        regEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_DATA_PUSH_INTERFACE);

        regEvent(UiEvent.EVENT_NETWORK_CHANGE);

        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        // 音乐的命令字全部注册为远程音乐场景，底层特殊处理远程音乐场景

        // regCommand("MEDIA_CMD_PLAY");
        regCommandWithResult("MEDIA_CMD_PLAY");
        regCommand("MUSIC_CMD_OPEN");
        //同听3.0新增功能与说法
        regCommand("MUSIC_ADD_DESCRIPE");
        regCommand("MUSIC_CMD_HISTORY_NOVEL");
        regCommand("MUSIC_CMD_HISTORY");

        regCustomCommand("MUSIC_CMD_PLAY", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PLAY);
        regCustomCommand("MUSIC_CMD_STOP", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PAUSE);
        regCustomCommand("MUSIC_CMD_EXIT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_EXIT);
        regCustomCommand("MUSIC_CMD_NEXT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_NEXT);
        regCustomCommand("MUSIC_CMD_PREV", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PREV);
        regCustomCommand("MUSIC_CMD_RANDOM", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_RANDOM);
        regCustomCommand("MUSIC_CMD_SWITCH_LOOP_ONE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
        regCustomCommand("MUSIC_CMD_SWITCH_LOOP_ALL", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
        regCustomCommand("MUSIC_CMD_SWITCH_RANDOM", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);

        regCustomCommand("MUSIC_FAVOURITE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
        regCustomCommand("MUSIC_CANCEL_FAVOURITE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
        regCustomCommand("MUSIC_HATE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_HATE_CUR);
        regCustomCommand("MUSIC_PLAY_FAVOURITE_LIST", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
        regCustomCommand("REFRESH_MEDIA_LIST", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST);
        regCustomCommand("REFRESH_MUSIC_LIST", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);

        // 播放音乐请求
        regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_PLAY_MEDIA);
        // 注册考拉相关
        AdapterKaola.getAdapter().regModule();

        return super.initialize_AfterStartJni();
    }

    @Override
    public int initialize_AfterInitSuccess() {
        AndroidMediaLibrary.refreshSystemMedia();
        return super.initialize_AfterInitSuccess();
    }


    @Override
    public int onCommand(String cmd, String keywords, String voiceString) {
        if (cmd.equals("MEDIA_CMD_PLAY")) {
            continuePlay(voiceString);
        }

        return super.onCommand(cmd, keywords, voiceString);
    }

    private void requestHistory(String type) {
        if (procSenceByRemote("requestHistory"))
            return;
        IMusic tool = getMusicTool();
        if (null != tool && tool instanceof ITxzMedia && ((ITxzMedia) tool).supportRequestHistory()) {
            ((ITxzMedia) tool).requestHistory(type);
            return;
        }
        RecorderWin.speakText("不支持该操作", null);
    }

    private void addSubscribe() {
        if (procSenceByRemote("addSubscribe"))
            return;
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
        invokeTXZMusic(null, "addSubscribe", null);

    }

    private void switchPlayMode() {
        if (procSenceByRemote("switchModeLoopOnce"))
            return;
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_SWICTH_MODE_LOOP_ONCE"), new Runnable() {
            @Override
            public void run() {
                invokeTXZMusic(null, "switchModeLoopOnce", null);
            }
        });
    }

    @Override
    public int onCommand(String cmd) {
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
        if ("MUSIC_CMD_OPEN".equals(cmd)) {
            // 打开指令
            final IMusic tool = getMusicTool();
            if (null != tool && tool instanceof ITxzMedia) {
                ITxzMedia txzMedia = (ITxzMedia) tool;
                txzMedia.openApp();//同听客户端处理播报并关闭
            } else {
                if (tool != null) {
                    String spk = NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC");
                    RecorderWin.speakTextWithClose(spk, new Runnable() {
                        @Override
                        public void run() {
                            tool.start();
                        }
                    });
                } else {
                    UiApp.AppInfo pobjApp = PackageManager.getInstance().getUIAppInfo("播放器");
                    if (pobjApp != null) {
                        JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_APP, UiApp.SUBEVENT_OPEN_APP, pobjApp);
                    } else {
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
                    }
                }
            }
        } else if (cmd.equals("MUSIC_ADD_DESCRIPE")) {
            addSubscribe();
        } else if ("MUSIC_CMD_HISTORY".equals(cmd)) {
            requestHistory("all");
        } else if ("MUSIC_CMD_HISTORY_NOVEL".equals(cmd)) {
            requestHistory("novel");
        }

        AdapterKaola.getAdapter().onCommand(cmd);

        return 0;
    }

    /**
     * 继续播放
     */
    public void continuePlay(String text) {
		if (SenceManager.getInstance().noneedProcSence("music",
				new JSONBuilder().put("action", "continue").put("scene", "music").put("text", text).toBytes())) {
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("procSence").setSessionId()
					.putExtra("sence", "music").buildCommReport());
			return;
		}
        if (isAudioToolSet() && TextUtils.isEmpty(mMusicToolServiceName)) {
            String spk = NativeData.getResString("RS_MUSIC_COUNTINUE");
            RecorderWin.speakTextWithClose(spk, new Runnable() {
                @Override
                public void run() {
                    MediaControlUtil.play();
                }

            });
        } else {
            // 判断是否有可用音乐工具
            IMusic tool = getMusicTool();
            if (!TextUtils.isEmpty(mMusicToolServiceName) || getMusicTool() != null) {
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC"), new Runnable() {
                    @Override
                    public void run() {
                        JSONBuilder jsonBuilder = new JSONBuilder();
                        jsonBuilder.put("continue", true);
                        invokeTXZMusic(null, "play", jsonBuilder.toBytes());
                    }
                });
            } else {
                String spk = NativeData.getResString("RS_MUSIC_NOT_TOOL");
                RecorderWin.speakTextWithClose(spk, null);
            }

        }

    }

    abstract class PlayMediaConfirmTtsCallback extends ITtsCallback {
        MediaList mMediaList;

        public PlayMediaConfirmTtsCallback(MediaList lst) {
            mMediaList = lst;
        }
    }

    abstract class PlayMediaConfirmAsrCallback extends IAsrCallback {
        MediaList mMediaList;

        public PlayMediaConfirmAsrCallback(MediaList lst) {
            mMediaList = lst;
        }
    }

    // 通过语音收藏当前播放的歌曲
    public boolean favouriteMusicFromVoice(boolean favourite, boolean hint) {
        if (MusicTxzImpl.mTxzMusicToolisPlaying && !AudioTxzImpl.isNewVersion()) {// 不是最新版本
            if (MusicTxzImpl.mTxzMusicToolCurrentMusicModel != null) {
                MediaModel mediaModel = MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia;
                if (mediaModel != null) {
                    favouriteMusic(mediaModel, favourite);
                    if (hint) {
                        if (favourite) {
                            String spk = NativeData.getResString("RS_MUSIC_FAVOURITE_ADD");
                            RecorderWin.speakTextWithClose(spk, null);
                        } else {
                            String spk = NativeData.getResString("RS_MUSIC_FAVOURITE_CANCEL");
                            RecorderWin.speakTextWithClose(spk, null);
                        }
                    }
                    return true;
                }
            }
        } else if (AudioTxzImpl.isNewVersion()) {
            String spk = NativeData.getResString("RS_MUSIC_FUNCTION_UNSUPPORT");
            RecorderWin.speakText(spk, null);
        }
        if (hint) {
            String spk = NativeData.getResString("RS_MUSIC_NO_PLAY");
            RecorderWin.speakTextWithClose(spk, null);
        }
        return false;
    }

    // 收藏指定的歌曲
    public void favouriteMusic(MediaModel mediaModel, boolean favourite) {
        if (AudioTxzImpl.isNewVersion()) {
            String spk = NativeData.getResString("RS_MUSIC_FUNCTION_UNSUPPORT");
            RecorderWin.speakText(spk, null);
            return;
        }

        if (null == mediaModel)
            return;
        mediaModel.bFavourite = favourite;
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
                MessageNano.toByteArray(mediaModel), null);
        if (favourite) {
            JNIHelper.logd("favourite music: " + mediaModel.strPath);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_FAVOURITE, mediaModel);
        } else {
            JNIHelper.logd("cancel favourite music: " + mediaModel.strPath);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE, mediaModel);
        }
        return;
    }

    public void procMediaPath(MediaModel m) {
        if (m == null)
            return;
        m.strPath = NativeData.getMeidaPlayUrl(m.strPath);
    }

    public void procMediaPath(MediaItem m) {
        if (m == null)
            return;
        if (m.msgMedia != null) {
            procMediaPath(m.msgMedia);
        }
        for (int i = 0; m.rptMsgReferenceMedia != null && i < m.rptMsgReferenceMedia.length; ++i) {
            procMediaPath(m.rptMsgReferenceMedia[i]);
        }
    }

    public void procMediaPath(MediaList m) {
        for (int i = 0; m.rptMediaItem != null && i < m.rptMediaItem.length; ++i) {
            procMediaPath(m.rptMediaItem[i]);
        }
    }

    public void procMediaPath(MediaCategoryList m) {
        for (int i = 0; m.rptMsgCategoryList != null && i < m.rptMsgCategoryList.length; ++i) {
            if (m.rptMsgCategoryList[i] == null)
                continue;
            procMediaPath(m.rptMsgCategoryList[i].msgMediaList);
        }
    }

    public String genMediaModelTitle(String jsonModel) {
        MusicModel musicModel = new MusicModel();
        JSONBuilder builder = new JSONBuilder(jsonModel);
        musicModel.setArtist(builder.getVal("artists", String[].class));
        musicModel.setKeywords(new String[]{builder.getVal("tag", String.class)});
        musicModel.setAlbum(builder.getVal("album", String.class));
        musicModel.setTitle(builder.getVal("title", String.class));

        return genMediaModelTitle(musicModel);
    }

    public String genMediaModelTitle(MusicModel musicModel) {
        String[] kws = musicModel.getKeywords();
        String kw = "";
        if (kws != null && kws.length > 0) {
            kw = kws[0];
        }
        return genMediaModelTitle(musicModel.getTitle(), musicModel.getAlbum(), musicModel.getArtist(), kw,
                musicModel.getField() == AudioTxzImpl.MUSICFLAG ? "歌曲" : "节目");
    }

    public String genMediaModelTitle(String title, String album, String[] artists, String keywrod, String type) {
        StringBuilder artist = new StringBuilder();
        if (artists != null) {
            for (int i = 0; i < artists.length; ++i) {
                if (artist.length() == 0) {
                    artist.append(artists[i]);
                } else if (i == artists.length - 1) {
                    artist.append("和");
                    artist.append(artists[i]);
                } else {
                    artist.append("、");
                    artist.append(artists[i]);
                }
            }
        }
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
            if (TextUtils.isEmpty(album)) {
                if (!TextUtils.isEmpty(keywrod)) {
                    return keywrod + "类型" + type;
                }
            } else {
                return album;
            }
        }
        if (TextUtils.isEmpty(artist) == false) {
            artist.append("的");
        }
        if (TextUtils.isEmpty(title)) {
            if (!TextUtils.isEmpty(artist)) {
                artist.append(type);
            }
        } else {
            artist.append(title);
        }
        JNIHelper.logd(TAG + "search:modle" + artist.toString());
        return artist.toString();
    }

    // public byte[] currentData;

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        JNIHelper.logd(TAG + "event[" + eventId + "|" + subEventId + "]");
        switch (eventId) {
            case UiEvent.EVENT_ACTION_AUDIO: {
                switch (subEventId) {
                    case UiAudio.SUBEVENT_RESP_DATA_INTERFACE: {
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.dataInterface", data, null);
                        break;
                    }
                    case UiAudio.SUBEVENT_DATA_PUSH_INTERFACE: {
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.dataPushInterface", data, null);
                        break;
                    }
                }
                break;
            }
            case UiEvent.EVENT_VOICE: {
                switch (subEventId) {
                    case VoiceData.SUBEVENT_VOICE_PLAY_MEDIA: {
                        String title = "歌曲";
                        try {
                            MediaList mediaList = MediaList.parseFrom(data);
                            if (mediaList.msgSearchFilter != null) {
                                String keywrods = mediaList.msgSearchFilter.strType;
                                if (TextUtils.isEmpty(keywrods) && mediaList.msgSearchFilter.rptStrKeywords != null
                                        && mediaList.msgSearchFilter.rptStrKeywords.length > 0) {
                                    keywrods = mediaList.msgSearchFilter.rptStrKeywords[0];
                                }
                                title = genMediaModelTitle(mediaList.msgSearchFilter.strTitle,
                                        mediaList.msgSearchFilter.strAlbum, mediaList.msgSearchFilter.rptStrArtist, keywrods,
                                        "歌曲");
                            }
                        } catch (Exception e) {
                        }
                        if (TextUtils.isEmpty(title)) {
                            // 没有找到相关数据
                            RecorderWin.speakTextWithClose(
                                    NativeData.getResPlaceholderString("RS_VOICE_SPEAK_NODATAFOUND_TIPS"), null);
                        } else {
                            String spk = NativeData.getResPlaceholderString("RS_MUSIC_WILL_PLAY", "%MUSIC%", title);
                            RecorderWin.speakTextWithClose(spk, new Runnable1<byte[]>(data) {
                                @Override
                                public void run() {
                                    JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_LIST, mP1);
                                }
                            });
                        }
                        break;
                    }
                }
                break;
            }
            case UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC: {
                MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                String kw = null;
                JSONBuilder json = new JSONBuilder(data);
                json.put("field", AudioTxzImpl.MUSICFLAG);
                String[] kws = json.getVal("keywords", String[].class);
                if (kws != null && kws.length > 0) {
                    kw = kws[0];
                }
                if (!TextUtils.isEmpty(mMusicToolServiceName)) {

                    String title = genMediaModelTitle(json.getVal("title", String.class),
                            json.getVal("album", String.class), json.getVal("artist", String[].class), kw, "歌曲");
                    if (mIsNotNeedTts) {
                        if (RecorderWin.isOpened()) {
                            RecorderWin.close();
                        }

                        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playMusic", json.toBytes(), null);
					reportMusic("play","remote"+mMusicToolServiceName);
                        break;
                    }
                    if (TextUtils.isEmpty(title)) {
                        // 没有找到相关数据
                        RecorderWin.speakTextWithClose(
                                NativeData.getResPlaceholderString("RS_VOICE_SPEAK_NODATAFOUND_TIPS"), null);
                    } else {
                        String spk = NativeData.getResPlaceholderString("RS_MUSIC_WILL_PLAY", "%MUSIC%", title);
                        RecorderWin.speakTextWithClose(spk, new Runnable1<byte[]>(json.toBytes()) {
                            @Override
                            public void run() {
                                ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playMusic", mP1,
                                        null);
							reportMusic("play","remote"+mMusicToolServiceName);
                            }
                        });
                    }
                    break;
                }
                final IMusic musicTool = getMusicTool();
                if (musicTool != null) {
                    MusicModel model = new MusicModel();
                    model.setTitle(json.getVal("title", String.class));
                    model.setArtist(json.getVal("artist", String[].class));
                    model.setAlbum(json.getVal("album", String.class));
                    model.setText(json.getVal("text", String.class));
                    model.setKeywords(kws);
                    model.setField(AudioTxzImpl.MUSICFLAG);
                    String title = genMediaModelTitle(model.getTitle(), model.getAlbum(), model.getArtist(), kw, "歌曲");
                    if (musicTool instanceof MusicKaolaImpl && MusicKaolaImpl.SHOW_SEARCH_RESULT) {
                        musicTool.playMusic(model);
					reportMusic("play",musicTool.getPackageName());
                        break;
                    }
                    if (musicTool instanceof AudioTxzImpl) {
                        musicTool.playMusic(model);
					reportMusic("play", musicTool.getPackageName());
                        break;
                    }
                    if (musicTool instanceof MusicKuwoImpl && MusicKuwoImpl.SHOW_SEARCH_RESULT) {
                        musicTool.playMusic(model);
                        String spk = NativeData.getResPlaceholderString("RS_MUSIC_SEARCHING", "%MUSIC%", title);
                        RecorderWin.addSystemMsg(spk);
                        TtsManager.getInstance().speakText(spk);
					reportMusic("play",musicTool.getPackageName());
                        break;
                    }
                    if (TextUtils.isEmpty(title)) {
                        // 没有找到相关数据
                        RecorderWin.speakTextWithClose(
                                NativeData.getResPlaceholderString("RS_VOICE_SPEAK_NODATAFOUND_TIPS"), null);
                    } else {
                        String spk = NativeData.getResPlaceholderString("RS_MUSIC_WILL_PLAY", "%MUSIC%", title);
                        RecorderWin.speakTextWithClose(spk, new Runnable2<IMusic, MusicModel>(musicTool, model) {
                            @Override
                            public void run() {
                                AppLogic.runOnBackGround(new Runnable() {

                                    @Override
                                    public void run() {
                                        musicTool.playMusic(mP2);
									reportMusic("play",musicTool.getPackageName());
                                    }
                                }, mAudioFocusStreamType == null ? 0 : 500);
                            }
                        });
                    }
                    break;
                } else {
                    AsrManager.getInstance().setNeedCloseRecord(false);
                    String spk = NativeData.getResString("RS_MUSIC_NOT_TOOL");
                    RecorderWin.speakTextWithClose(spk, null);
                }
                break;
            }
            case UiEvent.EVENT_SYSTEM_MUSIC:
                AsrManager.getInstance().setNeedCloseRecord(true);
                switch (subEventId) {
                    case UiMusic.SUBEVENT_MEDIA_PLAY:
                        if (procSenceByRemote("play"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_PLAY_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "play", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PAUSE:
                        if (procSenceByRemote("pause"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_STOP_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "pause", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_EXIT:
                        if (procSenceByRemote("exit"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_EXIT_MUSIC"), new Runnable() {

                            @Override
                            public void run() {
                                invokeTXZMusic(null, "exit", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_NEXT:
                        if (procSenceByRemote("next"))
                            break;
                        IMusic music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_NEXT_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "next", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PREV:
                        if (procSenceByRemote("prev"))
                            break;
                        music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_PREV_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "prev", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_RANDOM:
                        if (procSenceByRemote("switchSong"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_SWITCH_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "switchSong", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE:
                        if (procSenceByRemote("switchModeLoopOne"))
                            break;
                        music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_SWICTH_MODE_LOOP_ONE"),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        invokeTXZMusic(null, "switchModeLoopOne", null);
                                    }
                                });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL:
                        if (procSenceByRemote("switchModeLoopAll"))
                            break;
                        music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_SWICTH_MODE_LOOP_ALL"),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        invokeTXZMusic(null, "switchModeLoopAll", null);
                                    }
                                });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_RANDOM:
                        if (procSenceByRemote("switchModeRandom"))
                            break;
                        music = getMusicTool();
                        if (music instanceof MusicKuwoImpl){
                        	boolean isRunning = ((MusicKuwoImpl)music).isKuwoRunning();
                        	if (!isRunning){
                        		RecorderWin.speakText(NativeData.getResString("RS_VOICE_KUWO_NO_OPEN"), null);
                        		break;
                        	}
                        }
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_SWICTH_MODE_RANDOM"),
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        invokeTXZMusic(null, "switchModeRandom", null);
                                    }
                                });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST:
                        if (procSenceByRemote("playFavourMusic"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        invokeTXZMusic(null, "playFavourMusic", null);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST:
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST:
                        if (procSenceByRemote("playRandom"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_WILL_REFRESH_MUSIC"), new Runnable() {
                            @Override
                            public void run() {
                                invokeTXZMusic(null, "playRandom", null);
                            }
                        });
                        break;
                    case UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE:
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        if (mSearchMoreResult)
                            break;
                        try {
                            // 打开音乐搜索中界面
                            mSearchMediaModel = MediaModel.parseFrom(data);
                            // TtsManager.getInstance().speakText("本地未找到，即将上网进行搜索");
                            if (mSearchMediaModel == null || (TextUtils.isEmpty(mSearchMediaModel.strTitle)
                                    && TextUtils.isEmpty(mSearchMediaModel.strAlbum)
                                    && TextUtils.isEmpty(mSearchMediaModel.strType))) {
                                boolean bEmpty = true;
                                if (mSearchMediaModel != null) {
                                    for (int i = 0; bEmpty && i < mSearchMediaModel.rptStrArtist.length; ++i) {
                                        if (TextUtils.isEmpty(mSearchMediaModel.rptStrArtist[i]) == false) {
                                            bEmpty = false;
                                        }
                                    }
                                    for (int i = 0; bEmpty && i < mSearchMediaModel.rptStrKeywords.length; ++i) {
                                        if (TextUtils.isEmpty(mSearchMediaModel.rptStrKeywords[i]) == false) {
                                            bEmpty = false;
                                        }
                                    }
                                }
                                if (bEmpty) {
                                    if (TextUtils.isEmpty(mSearchMediaModel.strSearchText)) {
                                        String spk = NativeData.getResString("RS_MUSIC_EMPTY");
                                        RecorderWin.speakTextWithClose(spk, null);
                                    } else {
                                        String spk = NativeData.getResString("RS_MUSIC_EMPTY_ABOUT");
                                        RecorderWin.speakTextWithClose(spk, null);
                                    }
                                    break;
                                }
                            }
                            String spk = NativeData.getResString("RS_MUSIC_NOT_FOUND");
                            RecorderWin.speakTextWithClose(spk, null);
                        } catch (Exception e) {
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE_RESULT:
                        if (mSearchMediaModel != null) {
                            try {
                                final MediaList lst = MediaList.parseFrom(data);
                                if (ProtoBufferUtil.isStringEmpty(lst.strErrMsg) == false) {
                                    mSearchMoreResult = false;
                                    JNIHelper.loge("recive search result error: " + lst.strErrMsg);
                                    AppLogic.showToast(lst.strErrMsg);
                                    TtsManager.getInstance().speakText(lst.strErrMsg);
                                    break;
                                }
                                if (ProtoBufferUtil.isEqual(lst.msgSearchFilter, mSearchMediaModel) == false) {
                                    JNIHelper.logw("recive search result not match");
                                    break;
                                }
                                // 本次搜索响应已回
                                mSearchMediaModel = null;
                                if (lst.rptMediaItem == null || lst.rptMediaItem.length == 0) {
                                    JNIHelper.logd("empty media list");
                                    String spk = NativeData.getResString("RS_MUSIC_SEARCH_NULL");
                                    TtsManager.getInstance().speakText(spk);
                                    mSearchMoreResult = false;
                                    break;
                                }
                                if (mSearchMoreResult) {
                                    mSearchMoreResult = false;
                                    procMediaPath(lst);
                                    ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                                            "music.update.appendMediaList", MessageNano.toByteArray(lst), null);
                                    break;
                                }
                                if (lst.int32CurIndex == null || lst.int32CurIndex < 0
                                        || lst.int32CurIndex > lst.rptMediaItem.length)
                                    lst.int32CurIndex = 0;
                                String hint;
                                if (lst.msgSearchFilter.rptStrArtist != null && lst.msgSearchFilter.rptStrArtist.length > 0
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strTitle)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strAlbum)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strType)) {
                                    // 歌手搜索
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.rptStrArtist[0]);
                                } else if ((lst.msgSearchFilter.rptStrArtist == null
                                        || lst.msgSearchFilter.rptStrArtist.length <= 0)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strTitle)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strAlbum)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strType) == false) {
                                    // 分类搜索
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.strType);
                                } else if ((lst.msgSearchFilter.rptStrArtist == null
                                        || lst.msgSearchFilter.rptStrArtist.length <= 0)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strTitle)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strAlbum) == false
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strType)) {
                                    // 分类搜索
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.strAlbum);
                                } else {
                                    hint = NativeData.getResPlaceholderString("RS_MUSIC_FOUND", "%MUSIC%",
                                            getMediaSpeakInfo(lst.rptMediaItem[lst.int32CurIndex].msgMedia));
                                }

                                lst.msgSearchFilter = null; // 置null，不允许下次查找更多结果
                                procMediaPath(lst);

                                TtsManager.getInstance().speakText(hint, new ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        AppLogic.runOnBackGround(new Runnable() {
                                            @Override
                                            public void run() {
                                                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                                                        "music.update.playMediaList", MessageNano.toByteArray(lst), null);
                                            }
                                        }, mAudioFocusStreamType == null ? 0 : 500);
                                    }
                                });
                            } catch (Exception e) {
                            }
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PLAY_LIST:
                        try {
                            MediaList m = MediaList.parseFrom(data);
                            procMediaPath(m);
					reportMusic("play", "remote"+ServiceManager.MUSIC);
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.playMediaList",
                                    MessageNano.toByteArray(m), null);
                        } catch (Exception e) {
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_SYNC_LIST:
                        try {
                            AsrManager.getInstance().setNeedCloseRecord(false);
                            MediaList m = MediaList.parseFrom(data);
                            procMediaPath(m);
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.syncMediaList",
                                    MessageNano.toByteArray(m), null);
                        } catch (Exception e) {
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR:
                        if (procSenceByRemote("favourMusic"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                            RecorderWin.speakTextWithClose("", new Runnable() {

                                @Override
                                public void run() {
                                    invokeTXZMusic(null, "favourMusic", null);
                                }
                            });
                            break;
                        }
                        invokeTXZMusic(null, "favourMusic", null);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR:
                        if (procSenceByRemote("unfavourMusic"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                            RecorderWin.speakTextWithClose("", new Runnable() {

                                @Override
                                public void run() {
                                    invokeTXZMusic(null, "unfavourMusic", null);
                                }
                            });
                            break;
                        }
                        invokeTXZMusic(null, "unfavourMusic", null);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_HATE_CUR:
                        if (procSenceByRemote("hateMusic"))
                            break;
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                            RecorderWin.speakTextWithClose("", new Runnable() {

                                @Override
                                public void run() {
							reportMusic("unfavourMusic","remote"+mMusicToolServiceName);
                                    ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.unfavourMusic",
                                            null, null);
                                    ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.switchSong",
                                            null, null);
                                }
                            });
                            break;
                        }
                        IMusic musicTool = getMusicTool();
                        if (musicTool != null) {
                            if (musicTool instanceof ITxzMedia) {
                                ((ITxzMedia) musicTool).hateAudio();
                                break;
                            }
                            musicTool.unfavourMusic();
                            musicTool.switchSong();
                            break;
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED: {
                        try {
                            AsrManager.getInstance().setNeedCloseRecord(false);
                            MediaCategoryList lst = MediaCategoryList.parseFrom(data);
                            procMediaPath(lst);
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.syncMediaCategoryList",
                                    MessageNano.toByteArray(lst), null);
                        } catch (Exception e) {
                        }
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO: {
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        speakMusicInfo();
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH: {
                        AsrManager.getInstance().setNeedCloseRecord(false);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.notifyDownloadFinish", data, null);
                        try {
                            MediaModel model = MediaModel.parseFrom(data);
                            // AppLogic.showToast(getMediaSpeakInfo(model) + "下载完成");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT: {
                        AsrManager.getInstance().setNeedCloseRecord(false);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.notifyMediaServerPort", data,
                                null);
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_PLAY_ALL:
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        AsrManager.getInstance().setNeedCloseRecord(true);
                        continuePlay(new String(data));
                        break;
                    default:
                        break;
                }
                break;
				 case UiEvent.EVENT_NETWORK_CHANGE: {
                    AndroidMediaLibrary.onNetworkChange(NetworkManager.getInstance().hasNet());
                    break;
                }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    public String getMediaSpeakInfo(MediaModel mediaModel) {
        StringBuilder singer = new StringBuilder();
        String name = "";

        if (null != mediaModel) {
            if (mediaModel.rptStrArtist != null) {
                for (int i = 0; i < mediaModel.rptStrArtist.length; ++i) {
                    if (mediaModel.rptStrArtist[i].length() <= 0)
                        continue;
                    if (singer.length() > 0) {
                        if (i == mediaModel.rptStrArtist.length - 1) {
                            singer.append("和");
                        } else {
                            singer.append("、");
                        }
                    }
                    singer.append(mediaModel.rptStrArtist[i]);
                }
            }
            if (mediaModel.strTitle != null) {
                name = mediaModel.strTitle;
            }
            if (name.length() <= 0) {
                if (mediaModel.strFileName != null) {
                    singer = new StringBuilder();
                    name = mediaModel.strFileName; // 使用文件名
                }
            }
        }

        if (singer.length() > 0)
            singer.append("的");

        if (name.length() <= 0) {
            if (singer.length() <= 0)
                name = "未知音乐";
            else
                name = "歌";
        }
        return singer.toString() + name;
    }

	private void reportMusic(String action, String toolName) {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("music")
				.setAction(action).setSessionId().putExtra("toolName",toolName).buildCommReport());
	}
    // 播报音乐信息
    public void speakMusicInfo() {
        if (isPlaying()) {
            MediaModel model = null;
            if (TextUtils.isEmpty(mMusicToolServiceName)) {
                IMusic musicTool = getMusicTool();
                if (musicTool != null) {
                    MusicModel m = musicTool.getCurrentMusicModel();
                    if (m != null) {
                        model = new MediaModel();
                        model.strAlbum = m.getAlbum();
                        model.strTitle = m.getTitle();
                        model.rptStrArtist = m.getArtist();
                        model.rptStrKeywords = m.getKeywords();
                    }
                }
            } else {
                JSONObject json;
                try {
                    MusicModel m;

                    IMusic musicTool = getMusicTool();
                    if (musicTool != null && (m = musicTool.getCurrentMusicModel()) != null) {
                        json = new JSONObject(m.toString());
                    } else {
                        json = new JSONObject(new String(mRemoteMusicToolgetCurrentMusicModel));
                    }
                    model = new MediaModel();
                    if (json.has("title"))
                        model.strTitle = json.getString("title");
                    if (json.has("album"))
                        model.strAlbum = json.getString("album");
                    if (json.has("artist")) {
                        JSONArray arr = json.getJSONArray("artist");
                        model.rptStrArtist = new String[arr.length()];
                        for (int i = 0; i < arr.length(); ++i) {
                            model.rptStrArtist[i] = arr.getString(i);
                        }
                    }
                } catch (JSONException e) {
                    model = null;
                }
            }
            if (model != null) {
                LogUtil.logd(TAG + "speakmodel=" + model.strTitle + "," + model);
                String spk = NativeData.getResPlaceholderString("RS_MUSIC_IS_PLAY", "%MUSIC%",
                        getMediaSpeakInfo(model));
                RecorderWin.speakTextWithClose(spk, null);
                return;
            }
        }
        String spk = NativeData.getResString("RS_MUSIC_NO_PLAY");
        RecorderWin.speakTextWithClose(spk, null);
    }

    private IMusic getTXZMusicTool() {
        if (AudioTxzImpl.isNewVersion()) {
            if (!(mMusicTxzImpl instanceof AudioTxzImpl)) {
                mMusicTxzImpl = new AudioTxzImpl();
                synchronized (mMusicInnerToolMap) {
                    return mMusicInnerToolMap.put(ServiceManager.MUSIC, mMusicTxzImpl);
                }
            }
        } else {
            if ((mMusicTxzImpl instanceof AudioTxzImpl)) {
                mMusicTxzImpl = new MusicTxzImpl();
                synchronized (mMusicInnerToolMap) {
                    return mMusicInnerToolMap.put(ServiceManager.MUSIC, mMusicTxzImpl);
                }
            }
        }
        return mMusicTxzImpl;
    }

    public IMusic getMusicToolInner() {
        IMusic musicTool;
        String type = mMusicToolApp;
        if (!TextUtils.isEmpty(type) && !PackageManager.getInstance().checkAppExist(type)) {
            type = null;
        }
        if (TextUtils.isEmpty(type)) {
            if (PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC)) {
                type = ServiceManager.MUSIC;
            } else if (PackageManager.getInstance().checkAppExist(MusicKuwoImpl.PACKAGE_NAME)) {
                type = MusicKuwoImpl.PACKAGE_NAME;
            } else if (PackageManager.getInstance().checkAppExist(MusicKaolaImpl.PACKAGE_NAME)) {
                type = MusicKaolaImpl.PACKAGE_NAME;
            }
        }
        getTXZMusicTool();
        synchronized (mMusicInnerToolMap) {
            musicTool = mMusicInnerToolMap.get(type);
        }

        return musicTool;
    }

    public IMusic getMusicTool() {
        IMusic musicTool = (IMusic) PluginManager.invoke("com.music.model.get", MusicPluginVersion);
        if (musicTool == null) {
            musicTool = getMusicToolInner();
        }

        if (musicTool == null) {
            IAudio audioTool = com.txznet.txz.module.audio.AudioManager.getInstance().getLocalAudioToolInner();
            if (audioTool != null)
                musicTool = new MusicToAudioImpl(audioTool);
        }
        JNIHelper.logd(TAG + "musicTool:" + (musicTool != null ? musicTool.getPackageName() : ""));
        return musicTool;
    }

    private boolean mIsNotNeedTts = false;
    private String mMusicToolServiceName;
    private Map<String, IMusic> mMusicInnerToolMap = new HashMap<String, IMusic>();
    private String mMusicToolApp = null;
    private boolean mRemoteMusicToolisPlaying = false;
    private byte[] mRemoteMusicToolgetCurrentMusicModel;

    public boolean hasRemoteProcTool() {
        if (!TextUtils.isEmpty(mMusicToolServiceName))
            return true;
        if (ServiceManager.MUSIC.equals(mMusicToolApp)
                && PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC)) {
            return AudioTxzImpl.isNewVersion();
        }
        if (!TextUtils.isEmpty(mMusicToolApp))
            return true;
        // 未设置音乐工具时
        if (PackageManager.getInstance().checkAppExist(ServiceManager.MUSIC)) {
            return AudioTxzImpl.isNewVersion();
        }
        if (PackageManager.getInstance().checkAppExist(MusicKuwoImpl.PACKAGE_NAME))
            return true;
        if (PackageManager.getInstance().checkAppExist(MusicKaolaImpl.PACKAGE_NAME))
            return true;
        if (getMusicTool() != null)
            return true;
        return false;
    }

    public String getDisableResaon() {
        if (!TextUtils.isEmpty(mMusicToolServiceName))
            return "";
        IMusic musicTool = getMusicTool();
        if (musicTool != null) {
            if (PackageManager.getInstance().checkAppExist(musicTool.getPackageName()))
                return "";
        }
        return NativeData.getResString("RS_VOICE_NO_MUSIC_TOOL");
    }

    ConnectionListener mConnectionListener = new ConnectionListener() {
        @Override
        public void onConnected(String serviceName) {
        }

        @Override
        public void onDisconnected(String serviceName) {
            if (serviceName.equals(mMusicToolServiceName)) {
                invokeTXZMusic(null, "cleartool", null);
            }
        }
    };

    String mAudioFocusLogicService = null;
    Integer mAudioFocusStreamType = null;

    public IMusic getTongTingMusicTool() {
        IMusic musicTool = null;
        getTXZMusicTool();
        synchronized (mMusicInnerToolMap) {
            musicTool = mMusicInnerToolMap.get(ServiceManager.MUSIC);
        }
        return musicTool;
    }

    public byte[] invokeTXZMusic(final String packageName, String command, final byte[] data) {
        JNIHelper.logd(TAG + "receiver:command:" + command + ",from:" + packageName);
        if (command.startsWith("tongting.")) {
            return TongtingManager.getInstance().preInvoke(packageName, command.substring("tongting.".length()), data);
        }
        if (command.equals("dataInterface")) {
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_REQ_DATA_INTERFACE, data);
            return null;
        }

        if ("getUid".equals(command)) {
            return String.valueOf(ProjectCfg.getUid() == null ? 0 : ProjectCfg.getUid()).getBytes();
        }
        if ("getAppid".equals(command)) {
            return (StringUtils.isNotEmpty(LicenseManager.getInstance().getAppId()) ? LicenseManager.getInstance().getAppId() : "").getBytes();
        }
        if ("getCoreVersion".equals(command)) {
            return GlobalContext.get().getString(R.string.app_version).getBytes();
        }

        // 所传输的数据结构不同
        if ("syncmusiclist.v2".equals(command)) {
            LogUtil.logd("show data :" + new String(data));
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            String strArray = jsonBuilder.getVal("list", String.class);
            JSONArray array = null;
            try {
                array = new JSONArray(strArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            boolean continuePlay = jsonBuilder.getVal("continuePlay", Boolean.class, false);
            List<AudioShowData> audioShowDatas = new ArrayList<AudioShowData>();
            long delayTime = 0;
            try {
                if (null != array && array.length() >= 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONBuilder jsonBuilder2 = new JSONBuilder(array.getString(i));
                        AudioShowData audioShowData = new AudioShowData();
                        audioShowData.setTitle(jsonBuilder2.getVal("title", String.class, null));
                        audioShowData.setId(jsonBuilder2.getVal("id", Long.class, 0L));
                        audioShowData.setName(jsonBuilder2.getVal("name", String.class, null));
                        audioShowData.setReport(jsonBuilder2.getVal("report", String.class, null));
                        delayTime = jsonBuilder2.getVal("delayTime", Long.class, 0L);
                        audioShowData.setNovelStatus(jsonBuilder2.getVal("novelStatus", Integer.class, 0));
                        audioShowData.setWakeUp(jsonBuilder2.getVal("wakeUp", String[].class, null));
                        audioShowData.setListened(jsonBuilder2.getVal("listened", Boolean.class, false));
                        audioShowData.setPaid(jsonBuilder2.getVal("paid", Boolean.class, false));
                        audioShowData.setShowDetail(jsonBuilder2.getVal("showDetail", Boolean.class, false));
                        audioShowData.setLatest(jsonBuilder2.getVal("latest", Boolean.class, false));
                        audioShowDatas.add(audioShowData);
                    }
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
            for (AudioShowData d : audioShowDatas) {
                LogUtil.logd(TAG + "showlist:" + d.toString());
            }

            if (RecorderWin.isOpened()) {
                if (PluginManager.invoke("com.music.selector.do", MusicPluginVersion) != null) {// 插件中获取
                    PluginManager.invoke("com.music.selector.show", data, MusicPluginVersion);
                    List<AudioShowData> asds = (List<AudioShowData>) PluginManager
                            .invoke("com.music.selector.generateShowList", MusicPluginVersion);
                    ChoiceManager.getInstance().showMusicList(asds, delayTime != 0, delayTime, null, false);
                } else {
                    if (audioShowDatas.size() == 1 && delayTime == 0) {
                        ChoiceManager.getInstance().showMusicList(audioShowDatas, true, 4000, null, continuePlay);
                    } else {
                        ChoiceManager.getInstance().showMusicList(audioShowDatas, delayTime != 0, delayTime, null,
                                continuePlay);
                    }
                }

                // MusicSelectControl.showContactSelectList(
                // UiEvent.EVENT_ACTION_AUDIO, audioShowDatas);
            } else {
                LogUtil.logd("RecorderWin isclosed");
            }
            return null;
        }

        // syncmusiclist 携带的数据必须是一个json array，不能满足需求
        if (command.equals("syncmusiclist")) {
            LogUtil.logd("------------------this is show data =" + new String(data));
            JSONArray array = null;
            try {
                array = new JSONArray(new String(data));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<AudioShowData> audioShowDatas = new ArrayList<AudioShowData>();
            long delayTime = 0;
            boolean continuePlay = false;
            if (null != array && array.length() >= 0) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject jsonObject = array.optJSONObject(i);
                    AudioShowData audioShowData = new AudioShowData();
                    if (jsonObject.has("title")) {
                        audioShowData.setTitle(jsonObject.optString("title"));
                    }
                    if (jsonObject.has("id")) {
                        audioShowData.setId(jsonObject.optLong("id"));
                    }
                    if (jsonObject.has("name")) {
                        audioShowData.setName(jsonObject.optString("name"));
                    }
                    if (jsonObject.has("report")) {
                        audioShowData.setReport(jsonObject.optString("report"));
                    }
                    if (jsonObject.has("delayTime")) {
                        delayTime = jsonObject.optInt("delayTime");
                    }
                    if (jsonObject.has("novelStatus")) {
                        audioShowData.setNovelStatus(jsonObject.optInt("novelStatus", 0));
                    }
                    if (jsonObject.has("lastPlay")) {
                        audioShowData.setLastPlay(jsonObject.optBoolean("lastPlay", false));
                    }
                    if (jsonObject.has("latest")) {
                        audioShowData.setLatest(jsonObject.optBoolean("latest", false));
                    }
                    if (jsonObject.has("listened")) {
                        audioShowData.setListened(jsonObject.optBoolean("listened", false));
                    }
                    if (jsonObject.has("paid")) {
                        audioShowData.setPaid(jsonObject.optBoolean("paid", false));
                    }
                    if (jsonObject.has("showDetail")) {
                        audioShowData.setShowDetail(jsonObject.optBoolean("showDetail", false));
                    }
                    audioShowDatas.add(audioShowData);
                }
            }
            for (AudioShowData d : audioShowDatas) {
                LogUtil.logd(TAG + "showlist:" + d.toString());
            }

            if (RecorderWin.isOpened()) {
                if (PluginManager.invoke("com.music.selector.do", MusicPluginVersion) != null) {//插件中获取
                    PluginManager.invoke("com.music.selector.show", data, MusicPluginVersion);
                    List<AudioShowData> asds = (List<AudioShowData>) PluginManager.invoke("com.music.selector.generateShowList", MusicPluginVersion);
                    ChoiceManager.getInstance().showMusicList(asds, delayTime != 0, delayTime, null, false);
                } else {
                    if (audioShowDatas.size() == 1 && delayTime == 0) {
                        ChoiceManager.getInstance().showMusicList(audioShowDatas, true, 4000, null, continuePlay);
                    } else {
                        ChoiceManager.getInstance().showMusicList(audioShowDatas, delayTime != 0, delayTime, null,
                                continuePlay);
                    }
                }

                // MusicSelectControl.showContactSelectList(
                // UiEvent.EVENT_ACTION_AUDIO, audioShowDatas);
            } else {
                LogUtil.logd("RecorderWin isclosed");
            }
            return null;
        }
        if (command.equals("isNewVersion")) {
            AudioTxzImpl.setVersion(true);
        }
        // 设置音频逻辑
        if (command.equals("audioLogic.tts")) {
            try {
                mAudioLogicWhenTts = AudioLogicType.valueOf(new String(data));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("audioLogic.asr")) {
            try {
                mAudioLogicWhenAsr = AudioLogicType.valueOf(new String(data));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("audioLogic.call")) {
            try {
                mAudioLogicWhenCall = AudioLogicType.valueOf(new String(data));
            } catch (Exception e) {
            }
            return null;
        }
        if ("setAudioFocusStreamType".equals(command)) {
            try {
                mAudioFocusStreamType = Integer.parseInt(new String(data));
            } catch (Exception e) {
            }
            return null;
        }
        if ("setAudioFocusLogic".equals(command)) {
            mAudioFocusLogicService = packageName;
            return null;
        }
        if ("clearAudioFocusLogic".equals(command)) {
            mAudioFocusLogicService = null;
            return null;
        }

        // 同步播放状态获取
        if (command.equals("isPlaying")) {
            return ("" + isPlaying()).getBytes();
        }
        // 同步缓冲状态获取
        if (command.equals("isBuffering")) {
            return ("" + isBuffering()).getBytes();
        }
        // 同步音乐模型获取
        if (command.equals("getCurrentMusicModel")) {
            if (!ProjectCfg.isFixCallFunction()) {
                if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                    return mRemoteMusicToolgetCurrentMusicModel;
                }
            }
            IMusic musicTool = getMusicTool();
            if (musicTool != null && musicTool.getCurrentMusicModel() != null) {
                return musicTool.getCurrentMusicModel().toString().getBytes();
            }
            return null;
        }
        // music应用通知更新
        if (command.equals("inner.isPlaying")) {
            if (AudioTxzImpl.isNewVersion()) {
                AudioTxzImpl.mTxzMusicToolisPlaying = Boolean.parseBoolean(new String(data));
                if (null != mMusicStatusListener) {
                    try {
                        mMusicStatusListener.onStatusChange(AudioTxzImpl.mTxzMusicToolisPlaying
                                ? MusicToolStatusListener.STATE_START_PLAY : MusicToolStatusListener.STATE_PAUSE_PLAY);
                    } catch (Exception e) {
                        LogUtil.loge("current music model inner isPlaying is " + AudioTxzImpl.mTxzMusicToolisPlaying);
                    }
                }
            } else {
                MusicTxzImpl.mTxzMusicToolisPlaying = Boolean.parseBoolean(new String(data));
            }
            JNIHelper.logd("music status update: isPlaying="
                    + (MusicTxzImpl.mTxzMusicToolisPlaying | AudioTxzImpl.mTxzMusicToolisPlaying));
            if (TextUtils.isEmpty(mMusicToolServiceName)) {
                if (MusicTxzImpl.mTxzMusicToolisPlaying || AudioTxzImpl.mTxzMusicToolisPlaying) {
                    onBeginMusic();
                } else {
                    onEndMusic();
                }
            }
            return null;
        }
        if (command.equals("inner.progress")) {
            if (null != mMusicStatusListener) {
                try {
                    JSONBuilder json = new JSONBuilder(data);
                    mMusicStatusListener.onProgress(json.getVal(MusicToolStatusListener.PROCESS_POSITION, int.class),
                            json.getVal(MusicToolStatusListener.PROCESS_DURATION, int.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        // music应用通知更新
        if (command.equals("inner.isBuffering")) {
            if (AudioTxzImpl.isNewVersion()) {
                AudioTxzImpl.mTxzMusicToolisBuffering = Boolean.parseBoolean(new String(data));
                if (null != mMusicStatusListener) {
                    try {
                        mMusicStatusListener.onStatusChange(AudioTxzImpl.mTxzMusicToolisBuffering
                                ? MusicToolStatusListener.STATE_BUFFERING : MusicToolStatusListener.STATE_START_PLAY);
                    } catch (Exception e) {
                        LogUtil.loge(
                                "current music model inner isBuffering is  " + AudioTxzImpl.mTxzMusicToolisBuffering);
                    }
                }
            } else {
                MusicTxzImpl.mTxzMusicToolisBuffering = Boolean.parseBoolean(new String(data));
            }
            JNIHelper.logd("music status update: isBuffering="
                    + (MusicTxzImpl.mTxzMusicToolisBuffering | AudioTxzImpl.mTxzMusicToolisBuffering));
            if (TextUtils.isEmpty(mMusicToolServiceName)) {
                if (MusicTxzImpl.mTxzMusicToolisBuffering || AudioTxzImpl.mTxzMusicToolisBuffering) {
                    onBeginMusic();
                } else {
                    onEndMusic();
                }
            }
            return null;
        }
        if (command.equals("inner.musicModel")) {
            try {
                MusicTxzImpl.mTxzMusicToolCurrentMusicModel = MediaItem.parseFrom(data);
                // if (TextUtils.isEmpty(mMusicToolServiceName)) {
                // if (MusicTxzImpl.mTxzMusicToolisPlaying) {
                // onBeginMusic();
                // } else {
                // onEndMusic();
                // }
                // }
                JNIHelper.logd("music status update: Model="
                        + getMediaSpeakInfo(MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.audioModel")) {// 音频对象
            try {
                LogUtil.logd("innerModer=" + new String(data) + ",mMusicToolServiceName=" + mMusicToolServiceName);
                AudioTxzImpl.mTxzMusicToolCurrentMusicModel = new String(data);
                AudioImpl.mTxzMusicToolCurrentMusicModel = new String(data);
                // if (TextUtils.isEmpty(mMusicToolServiceName)) {
                // if (MusicTxzImpl.mTxzMusicToolisPlaying) {
                // onBeginMusic();
                // } else {
                // onEndMusic();
                // }
                // }
                if (AudioTxzImpl.isNewVersion()) {
                    JNIHelper.logd(
                            "music status update:new version= Model=" + AudioTxzImpl.mTxzMusicToolCurrentMusicModel);
                    if (null != mMusicStatusListener) {
                        try {
                            mMusicStatusListener
                                    .playMusic(MusicModel.fromString(AudioTxzImpl.mTxzMusicToolCurrentMusicModel));
                        } catch (Exception e) {
                            LogUtil.loge("current music model format is error "
                                    + AudioTxzImpl.mTxzMusicToolCurrentMusicModel);
                        }
                    }
                } else {
                    JNIHelper.logd("music status update: Model="
                            + getMediaSpeakInfo(MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia));
                }
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.audioModel.next")) {// 下一首
            // 仅限新版本的音乐客户端（电台之家）才有此命令
            try {
                LogUtil.logd("next audio is :" + new String(data) + ",mMusicToolServiceName=" + mMusicToolServiceName);
                if (AudioTxzImpl.isNewVersion()) {
                    if (null != mMusicStatusListener) {
                        try {
                            mMusicStatusListener.endMusic(MusicModel.fromString(new String(data)));
                        } catch (Exception e) {
                            LogUtil.loge("current music model format is error "
                                    + AudioTxzImpl.mTxzMusicToolCurrentMusicModel);
                        }
                    }
                } else {
                    JNIHelper.logd("music status update: Model="
                            + getMediaSpeakInfo(MusicTxzImpl.mTxzMusicToolCurrentMusicModel.msgMedia));
                }
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.deleteMusic")) {
            try {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_DELETE, data);
                JNIHelper.logd("music delete path=" + (data == null ? "null" : new String(data)));
            } catch (Exception e) {

            }
            return null;
        }

        if (command.equals("inner.favourMusic")) {
            try {
                MediaModel m = MediaModel.parseFrom(data);
                favouriteMusic(m, true);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.unfavourMusic")) {
            try {
                MediaModel m = MediaModel.parseFrom(data);
                favouriteMusic(m, false);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.cancelSerachMedia")) {
            cancelSearchMedia();
            return null;
        }
        if (command.equals("inner.searchMoreMedia")) {
            try {
                MediaModel m = MediaModel.parseFrom(data);
                searchMoreMedia(m);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.refreshMusicList")) {
            try {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.updateMusicList")) {
            updateMusicList(data);
        }
        if (command.equals("inner.syncMusicList")) {
            try {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEED_REFRESH_MEDIA_LIST);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.refreshCategoryList")) {
            try {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CATEGORY_GET_LIST);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("txztool.playRandom")) {
            try {
                getTXZMusicTool().playRandom();
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("txztool.playFavourMusic")) {
            try {
                getTXZMusicTool().playFavourMusic();
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("txztool.favourMusic")) {
            try {
                favouriteMusicFromVoice(true, false);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("txztool.unfavourMusic")) {
            try {
                favouriteMusicFromVoice(false, false);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("txztool.playMusic")) {
            try {
                getTXZMusicTool().playMusic(MusicModel.fromString(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.getSongListByCategory")) {
            try {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
                        UiMusic.SUBEVENT_MEDIA_CATEGORY_GET_SONG_LIST_BY_CATEGORY, data);
            } catch (Exception e) {
            }
            return null;
        }
        // 工具类接口
        if (command.equals("cleartool")) {
            mMusicToolServiceName = null;
            mMusicToolApp = null;
            mIsNotNeedTts = false;
            ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
            invokeTXZMusic(null, "notifyMusicStatusChange", null); // 设置工具时刷新一下状态
            return null;
        }
        if (command.equals("settool")) {
            mMusicToolServiceName = null;
            mMusicToolApp = null;
            try {
                mIsNotNeedTts = !Boolean.parseBoolean(new String(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ServiceManager.getInstance().addConnectionListener(mConnectionListener);
            ServiceManager.getInstance().sendInvoke(packageName, "notifyMusicStatusChange", null,
                    new GetDataCallback() {
                        @Override
                        public void onGetInvokeResponse(ServiceData data) {
                            // 记录工具
                            if (data != null)
                                mMusicToolServiceName = packageName;
                        }
                    });
            return null;
        }
        if (command.equals("setInnerTool")) {
            mMusicToolServiceName = null;
            mMusicToolApp = null;
            String tool = new String(data);
            if ("MUSIC_TOOL_KUWO".equals(tool)) {
                mMusicToolApp = MusicKuwoImpl.PACKAGE_NAME;
                return null;
            }
            if ("MUSIC_TOOL_TXZ".equals(tool)) {
                mMusicToolApp = ServiceManager.MUSIC;
                return null;
            }
            if ("MUSIC_TOOL_KAOLA".equals(tool)) {
                mMusicToolApp = MusicKaolaImpl.PACKAGE_NAME;
                return null;
            }
            return null;
        }
        // 状态变更通知接口
        if (command.equals("notifyMusicStatusChange")) {
            ServiceManager.getInstance().runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.isPlaying", null,
                                new GetDataCallback() {
                                    @Override
                                    public void onGetInvokeResponse(ServiceData data) {
                                        try {
                                            if (data != null)
                                                mRemoteMusicToolisPlaying = data.getBoolean();
                                        } catch (Exception e) {
                                        }
                                        if (mRemoteMusicToolisPlaying) {
                                            onBeginMusic();
                                        } else {
                                            onEndMusic();
                                        }
                                    }
                                });
                        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName,
                                "tool.music.getCurrentMusicModel", null, new GetDataCallback() {
                                    @Override
                                    public void onGetInvokeResponse(ServiceData data) {
                                        if (data != null)
                                            mRemoteMusicToolgetCurrentMusicModel = data.getBytes();
                                    }
                                });
                    }
                }
            }, 0);
            return null;
        }

        if (command.equals("showKuwoSearchResult")) {
            Boolean showSearchResult = Boolean.parseBoolean(new String(data));
            MusicKuwoImpl.SHOW_SEARCH_RESULT = showSearchResult;
            return null;
        }

        if (command.equals("syncMuicList")) {
            AndroidMediaLibrary.syncMusicList(MusicModel.collecionFromString(new String(data)));
            return null;
        }
        if (command.equals("syncExMuicList")) {
            //TXZ-10461,超时的问题.可能是由于此处耗时,想来这里应该没有时序的问题,故可以进行线程的切换的操作
            AppLogic.runOnSlowGround(new Runnable() {
                @Override
                public void run() {
                    AndroidMediaLibrary.syncExMusicList(MusicModel.collecionFromString(new String(data)));
                }
            });
            return null;
        }
        if (command.startsWith("musiclistener.")) {
            try {
                handleMusicStatusListener(packageName, command.substring("musiclistener.".length()), data);
            } catch (Exception e) {
            }
            return null;
        }

        final IMusic musicTool = getMusicTool();
        if (musicTool != null) {
            if (command.equals("addSubscribe")) {
                if (musicTool instanceof ITxzMedia) {
                    RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ADD_SUBSCRIBE_MUSIC"), new Runnable() {
                        @Override
                        public void run() {
                            ((ITxzMedia) musicTool).addSubscribe();
                        }
                    });

                } else {
                    RecorderWin.speakText(NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT"), null);
                }
                return null;
            }
        }

        boolean useTool = (packageName == null || !ProjectCfg.isFixCallFunction());
        if (useTool) {
            // 使用工具执行操作
            if (!TextUtils.isEmpty(mMusicToolServiceName)) {
                ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music." + command, data, null);
                return null;
            }
        }

        if (musicTool == null)
            return null;

        JNIHelper.logd("getMusicTool " + musicTool.getPackageName());
        // 同行者音乐远程接口实现
        if (command.equals("play")) {
			reportMusic("play",musicTool.getPackageName());
            musicTool.start();
            return null;
        }
        if (command.equals("cont")) {
            if (musicTool instanceof MusicKuwoImpl) {
                ((MusicKuwoImpl) musicTool).continuePlay();
            }
            return null;
        }
        // 同行者音乐远程接口实现
        if (command.equals("play.extra")) {
            if (musicTool instanceof ITxzMedia) {
//				MusicModel currentMusicModel = musicTool.getCurrentMusicModel();
//				if (null != currentMusicModel) {
//					MediaControlUtil.play();
//				} else {
//					musicTool.start();
//				}
                ((ITxzMedia) musicTool).play();
            } else {
                musicTool.start();
            }
            return null;
        }
        if (command.equals("pause")) {
            if (isAudioToolSet()) {
                MediaControlUtil.pause();
            } else {
				reportMusic("pause",musicTool.getPackageName());
                musicTool.pause();
            }
            return null;
        }
        if (command.equals("exit")) {
            musicTool.pause();
            musicTool.exit();
			reportMusic("exit",musicTool.getPackageName());
            return null;
        }
        if ("exitAllMusicToolImmediately".equals(command)) {
            JNIHelper.logd("exitAllMusicToolImmediately begin");
            synchronized (mMusicInnerToolMap) {
                for (IMusic tool : mMusicInnerToolMap.values()) {
                    tool.exitImmediately();
                }
            }
            JNIHelper.logd("exitAllMusicToolImmediately end");
            return null;
        }
        if (command.equals("next")) {
            // 紧急处理凯立德众鸿问题
            if (packageName != null && packageName.startsWith("com.zhonghong.")) {
				reportMusic("next", musicTool.getPackageName());
                musicTool.next();
                return null;
            }

            if (isAudioToolSet()) {
                MediaControlUtil.next();
            } else {
				reportMusic("next",musicTool.getPackageName());
                musicTool.next();
            }

            return null;
        }
        if (command.equals("prev")) {
            // 紧急处理凯立德众鸿问题
            if (packageName != null && packageName.startsWith("com.zhonghong.")) {
				reportMusic("prev",musicTool.getPackageName());
                musicTool.prev();
                return null;
            }
            if (isAudioToolSet()) {
                MediaControlUtil.prev();
            } else {
				reportMusic("prev",musicTool.getPackageName());
                musicTool.prev();
            }

            return null;
        }
        if (command.equals("playFavourMusic")) {
			reportMusic("playFavourMusic",musicTool.getPackageName());
            musicTool.playFavourMusic();
            return null;
        }
        if (command.equals("favourMusic")) {
            musicTool.favourMusic();
			reportMusic("favourMusic",musicTool.getPackageName());
            return null;
        }
        if (command.equals("unfavourMusic")) {
            musicTool.unfavourMusic();
			reportMusic("unfavourMusic",musicTool.getPackageName());
            return null;
        }
        if (command.equals("switchModeLoopAll")) {
            musicTool.switchModeLoopAll();
			reportMusic("switchModeLoopAll",musicTool.getPackageName());
            return null;
        }
        if (command.equals("switchModeLoopOnce")) {
            if (musicTool instanceof ITxzMedia) {
                ((ITxzMedia) musicTool).switchPlayModeToOnce();
            } else {
                musicTool.switchModeLoopAll();
            }
            return null;
        }

        if (command.equals("switchModeLoopOne")) {
            musicTool.switchModeLoopOne();
			reportMusic("switchModeLoopOne",musicTool.getPackageName());
            return null;
        }
        if (command.equals("switchModeRandom")) {
            musicTool.switchModeRandom();
			reportMusic("switchModeRandom",musicTool.getPackageName());
            return null;
        }
        if (command.equals("switchSong")) {
            if (isAudioToolSet()) {
                MediaControlUtil.next();
                return null;
            }
			reportMusic("switchSong",musicTool.getPackageName());
            musicTool.switchSong();
            return null;
        }
        if (command.equals("playRandom")) {
            musicTool.playRandom();
			reportMusic("playRandom",musicTool.getPackageName());
            return null;
        }
        return null;
    }

    private void handleMusicStatusListener(String packageName, String command, byte[] data) {
        if (command.equals("playMusic")) {

        }
        if (command.equals("endMusic")) {

        }
        if (command.equals("set")) {
            JNIHelper.logd(TAG + "MusicListener addPackage:" + packageName);
            mStatusPackages.add(packageName);
        }
        if (command.equals("clear")) {
            JNIHelper.logd(TAG + "MusicListener removePackage:" + packageName);
            mStatusPackages.remove(packageName);
        }
    }

    private boolean isAudioToolSet() {
        return com.txznet.txz.module.audio.AudioManager.getInstance().isAudioToolSet();
    }

    /**
     * 向音乐客户端发送第几个的数据
     *
     * @param index
     * @param showDetail 是继续查询详情还是直接播放
     */
    public void sendResult(int index, boolean showDetail) {
        PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
        // 4.0版本之后才支持查看详情，之前版本只支持直接播放
        if (apkInfo.versionCode >= 400) {
            JSONBuilder json = new JSONBuilder();
            json.put("index", index);
            json.put("showDetail", showDetail);
            LogUtil.logd("sendInvoke: music.playmusiclist.index" + ",data:" + json.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.playmusiclist.index",
                    json.toBytes(), null);
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.playmusiclist.index",
                ("" + index).getBytes(), null);
    }

    @Override
    public int initialize_AfterLoadLibrary() {
        // 初始化，library
        AudioTxzImpl.getAppVersion();
        return super.initialize_AfterLoadLibrary();
    }

    private boolean procSenceByRemote(String action) {
        JSONObject root = new JSONObject();
        try {
            root.put("scene", "music");
            root.put("action", action);
            root.put("text", TextResultHandle.getInstance().getParseText());
            byte[] b_isProc = SenceManager.getInstance().procSenceByRemote("music", root.toString().getBytes());
            boolean isProc = Boolean.parseBoolean(new String(b_isProc));
            JNIHelper.logd(TAG + "intercept sence music by remote:" + isProc);
			if(isProc){
				ReportUtil.doReport(new ReportUtil.Report.Builder().setType("procSence").setSessionId()
						.putExtra("sence", "music").setAction(action).buildCommReport());
			}
            return isProc;
        } catch (JSONException e) {
        }
        return false;
    }

    public void sendPreloadIndex(int index) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.preload.index",
                ("" + index).getBytes(), null);
    }

    private String mStartAsrMusicTool = "";

    /**
     * 在音乐工具中启动声控，音乐结果的回调该音乐工具
     *
     * @param mStartAsrMusicTool
     */
    public void setStartAsrMusicTool(String mStartAsrMusicTool) {
        this.mStartAsrMusicTool = mStartAsrMusicTool;
    }
}
