package com.txznet.txz.module.music;

import android.content.pm.PackageInfo;
import android.text.TextUtils;
import android.util.Log;

import com.google.protobuf.nano.MessageNano;
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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZStatusManager.AudioLogicType;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.chooser.MusicPriorityChooser;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.component.music.txz.AudioTxzImpl;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.component.selector.ISelectControl.OnItemSelectListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.music.sdcard.MusicSdcardManager;
import com.txznet.txz.module.music.util.StringInfoUtils;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.tts.TtsManager.TtsTask;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.STATE;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ?????????????????????????????????????????????????????????
 *
 * @author bihongpi
 */
public class MusicManager extends IModule {

    public static final String TAG = "CORE:MUSIC:";

    private static final String MusicPluginVersion = "1.0";

    protected static final String PLUGIN_TAG = "music:plugin:invoke:";

    static MusicManager sModuleInstance = new MusicManager();

    private MusicManager() {
        MusicSdcardManager.registerSdcardListener();
    }

    private void init() {
    }

    public static MusicManager getInstance() {
        return sModuleInstance;
    }

    // /////////////////////////////////////////////////////////////////////

    MediaModel mSearchMediaModel = null;
    boolean mSearchMoreResult = false;

    public void cancelSearchMedia() {
        mSearchMediaModel = null;
        mSearchMoreResult = false;

        // ???????????????????????????
        MediaPriorityManager.getInstance().cancelSearchMedia();
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
    // /////////////////////////////////////////////////////////////////////

    @Override
    public int initialize_addPluginCommandProcessor() {

        PluginManager.addCommandProcessor("com.music.command.", new CommandProcessor() {

            @Override
            public Object invoke(String command, Object[] params) {
                LogUtil.logd(PLUGIN_TAG + "command:" + command);
                if (StringUtils.isNotEmpty(command)) {
                    if (params != null && params.length > 0) {
                        return MusicManager.getInstance().invokeTXZMusic("com.txznet.music.plugin", command,
                                (byte[]) params[0]);
                    }
                    if ("tool.packageName".equals(command)) {
                        IMediaTool tool = MediaPriorityManager.getInstance().getMediaToolWithPriority(
                                MediaPriorityManager.PRIORITY_TYPE.MUSIC, null);
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
        // ????????????
        PluginManager.addCommandProcessor("com.music.default.", new CommandProcessor() {

            @Override
            public Object invoke(String command, Object[] params) {
                if ("showMusicList".equals(command)) {
                    List<AudioShowData> audioShowDatas = (List<AudioShowData>) params[0];
                    int delayTime = (Integer) params[1];
                    final OnItemSelectListener listener = (OnItemSelectListener) params[2];
                    //					SelectorHelper.entryMusicSelector(audioShowDatas,
                    // delayTime != 0, delayTime, listener,false);
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
                    IMediaTool tool = MediaPriorityManager.getInstance().getMediaToolWithPriority(
                            MediaPriorityManager.PRIORITY_TYPE.MUSIC, null);
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

        return super.initialize_addPluginCommandProcessor();
    }

    public boolean onBeginTts(int stream, TtsTask ttsTask) {
        JNIHelper.logd(TAG + "onBeginTts");
        BeepPlayer.cancelMusic();

        // ??????AsrEnd
        if (ProjectCfg.needStopWkWhenTts() || (ttsTask != null && ttsTask.isForceStopWakeup())) {
            WakeupManager.getInstance().enbaleVoiceChannel(false);
            // WakeupManager.getInstance().stop();
            // VolumeManager.getInstance().muteAll(false);
        }

        // ??????TtsBegin
        boolean result = false;
        if (MusicFocusManager.getInstance().getAudioLogicWhenTts() != AudioLogicType.AUDIO_LOGIC_NONE) {
            result = MusicFocusManager.getInstance().requestAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenTts());
        }
        String command = "comm.status.onBeginTts";
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
        ServiceManager.getInstance().broadInvoke(command, null);

        WakeupManager.getInstance().enbaleVoiceChannel(true);
        // WakeupManager.getInstance().startDelay(300);
        /*
         * ??????????????????????????????????????????????????????????????? if (ProjectCfg.mEnableAEC) { return; }
		 */

        WakeupManager.getInstance().checkUsingAsr(new Runnable() {
            @Override
            public void run() {
                if (!InterruptTts.getInstance().isInterruptTTS()) {
                    if (WakeupManager.getInstance().isBusy())
                        RecorderWin.setState(STATE.STATE_WAKEUP_RECORD);
                    VolumeManager.getInstance().muteAll(true);
                }
            }
        }, null);
        if (MusicFocusManager.getInstance().getAudioLogicWhenTts() != AudioLogicType.AUDIO_LOGIC_NONE) {
            MusicFocusManager.getInstance().releaseAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenTts());
        }
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
        boolean result = MusicFocusManager.getInstance().requestAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenAsr());
        BeepPlayer.cancelMusic();

        String command = "comm.status.onBeginAsr";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        ServiceManager.getInstance().broadInvoke(command, null);

        AppLogic.removeBackGroundCallback(mRunnableProtectEndAsr);
        AppLogic.runOnBackGround(mRunnableProtectEndAsr, 5000);

		/*
         * ??????????????????????????????????????????????????????????????? if (ProjectCfg.mEnableAEC) { return; }
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
				 * ??????????????????????????????????????????????????????????????? if (ProjectCfg.mEnableAEC) { return; }
				 */
                if (!InterruptTts.getInstance().isInterruptTTS()) {
                    VolumeManager.getInstance().muteAll(false);
                }
                MusicFocusManager.getInstance().releaseAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenAsr());
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
				 * ??????????????????????????????????????????????????????????????? if (ProjectCfg.mEnableAEC) { return; }
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

        if (MusicFocusManager.getInstance()
                .getmAudioFocusStreamType() == MusicFocusManager.getInstance().EMPTY_FOCUS_TYPE) {
            MusicFocusManager.getInstance().requestAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenCall());
        }
        TtsManager.getInstance().pause();
        HelpGuideManager.getInstance().onBeginCall();
    }

    public void onEndCall() {
        JNIHelper.logd(TAG + "onEndCall");
        MusicFocusManager.getInstance().releaseAudioFocus(MusicFocusManager.getInstance().getAudioLogicWhenCall());
        TtsManager.getInstance().resume();
        String command = "comm.status.onEndCall";
        // ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
        // command,
        // null, null);
        ReminderManager.getInstance().triggerPush();
        ServiceManager.getInstance().broadInvoke(command, null);
        HelpGuideManager.getInstance().resumeGuideAnim();
    }

    // /////////////////////////////////////////////////////////////////////

    @Override
    public int initialize_BeforeStartJni() {
        //regEvent(UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SYNC_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE_RESULT);

        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT);


        regEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_RESP_DATA_INTERFACE);
        regEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_DATA_PUSH_INTERFACE);

        regEvent(UiEvent.EVENT_NETWORK_CHANGE);

        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        // ??????????????????
        regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_PLAY_MEDIA);

        // ??????3.0?????????????????????
        // ?????????????????????????????????
        regCommand("MUSIC_CMD_HISTORY_NOVEL");
        regCommand("MUSIC_CMD_HISTORY");

        // TODO: 2018/5/13 ????????????????????????, ???????????????????????????
        // ??????????????????
        /*AdapterKaola.regModule();
		IMusic music = getMusicByPackageName(MusicKuwoImpl.PACKAGE_NAME);
		if (music != null) {
			((MusicKuwoImpl) music).registerExitCmd();
		}*/

        return super.initialize_AfterStartJni();
    }

    @Override
    public int initialize_AfterInitSuccess() {
    	AndroidMediaLibrary.enableScanMediaLibrary = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_ENABLE_SCAN_MEDIA_LIBRARY , true);
		if(AndroidMediaLibrary.enableScanMediaLibrary) {
            LogUtil.logd("enableScanMediaLibrary " + AndroidMediaLibrary.enableScanMediaLibrary);
	        AndroidMediaLibrary.refreshSystemMedia();
		}
        //        MusicAsrManager.registerFreeWakeUp();
        return super.initialize_AfterInitSuccess();
    }

    @Override
    public int onCommand(String cmd, String keywords, String voiceString) {
        return super.onCommand(cmd, keywords, voiceString);
    }

    private void requestHistory(String type) {
        Log.i("requestHistory", "requestHistory :" + type);
        if (SenceManager.getInstance().procSenceByRemote("music", type))
            return;

        MusicTongTing.getInstance().requestHistory(type);
    }

    @Override
    public int onCommand(String cmd) {
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);

        // ??????????????????/????????????, ???????????????
        if ("MUSIC_CMD_HISTORY".equals(cmd)) {
            requestHistory("all");
        } else if ("MUSIC_CMD_HISTORY_NOVEL".equals(cmd)) {
            requestHistory("novel");
        }

		/*IMusic music = getMusicByPackageName(MusicKuwoImpl.PACKAGE_NAME);
		if (music != null) {
			((MusicKuwoImpl) music).onCommand(cmd);
		}*/
        // TODO: 2018/5/13 ????????????????????????, ???????????????????????????
        //AdapterKaola.getAdapter().onCommand(cmd);

        return 0;
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

    private void reportMusic(String action, String toolName) {
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("music")
                .setAction(action).setSessionId().putExtra("toolName", toolName).buildCommReport());
    }
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
                        String title = "??????";
                        try {
                            MediaList mediaList = MediaList.parseFrom(data);
                            if (mediaList.msgSearchFilter != null) {
                                String keywrods = mediaList.msgSearchFilter.strType;
                                if (TextUtils.isEmpty(keywrods) && mediaList.msgSearchFilter.rptStrKeywords != null
                                        && mediaList.msgSearchFilter.rptStrKeywords.length > 0) {
                                    keywrods = mediaList.msgSearchFilter.rptStrKeywords[0];
                                }
                                title = StringInfoUtils.genMediaModelTitle(mediaList.msgSearchFilter.strTitle,
                                        mediaList.msgSearchFilter.strAlbum, mediaList.msgSearchFilter.rptStrArtist, keywrods,
                                        "??????");
                            }
                        } catch (Exception e) {
                        }
                        if (TextUtils.isEmpty(title)) {
                            // ????????????????????????
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

            case UiEvent.EVENT_SYSTEM_MUSIC:

                //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????????????????????????????????????????????????????U??????????????????????????????????????????
                //AsrManager.getInstance().setNeedCloseRecord(true);
                switch (subEventId) {
                    case UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE:
                        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                        if (mSearchMoreResult)
                            break;
                        try {
                            // ???????????????????????????
                            mSearchMediaModel = MediaModel.parseFrom(data);
                            // TtsManager.getInstance().speakText("??????????????????????????????????????????");
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
                                // ????????????????????????
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
                                    // ????????????
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.rptStrArtist[0]);
                                } else if ((lst.msgSearchFilter.rptStrArtist == null
                                        || lst.msgSearchFilter.rptStrArtist.length <= 0)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strTitle)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strAlbum)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strType) == false) {
                                    // ????????????
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.strType);
                                } else if ((lst.msgSearchFilter.rptStrArtist == null
                                        || lst.msgSearchFilter.rptStrArtist.length <= 0)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strTitle)
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strAlbum) == false
                                        && ProtoBufferUtil.isStringEmpty(lst.msgSearchFilter.strType)) {
                                    // ????????????
                                    hint = NativeData.getResString("RS_MUSIC_FOUND_DETAIL")
                                            .replace("%NUM%", lst.rptMediaItem.length + "")
                                            .replace("%ART%", lst.msgSearchFilter.strAlbum);
                                } else {
                                    hint = NativeData.getResPlaceholderString("RS_MUSIC_FOUND", "%MUSIC%",
                                            StringInfoUtils.getMediaSpeakInfo(lst.rptMediaItem[lst.int32CurIndex].msgMedia));
                                }

                                lst.msgSearchFilter = null; // ???null????????????????????????????????????
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
                                        }, MusicFocusManager.getInstance().speakDelay());
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
                            // ??????????????????????????????
                            reportMusic("play", MusicTongTing.getInstance().getPackageName());
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.playMediaList",
                                    MessageNano.toByteArray(m), null);
                        } catch (Exception e) {
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_SYNC_LIST:
                        try {
                            //AsrManager.getInstance().setNeedCloseRecord(false);
                            MediaList m = MediaList.parseFrom(data);
                            procMediaPath(m);
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.syncMediaList",
                                    MessageNano.toByteArray(m), null);
                        } catch (Exception e) {
                        }
                        break;
                    case UiMusic.SUBEVENT_MEDIA_CATEGORY_LIST_UPDATED: {
                        try {
                            //AsrManager.getInstance().setNeedCloseRecord(false);
                            MediaCategoryList lst = MediaCategoryList.parseFrom(data);
                            procMediaPath(lst);
                            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.update.syncMediaCategoryList",
                                    MessageNano.toByteArray(lst), null);
                        } catch (Exception e) {
                        }
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_NOTIFY_DOWNLOAD_FINISH: {
                        //AsrManager.getInstance().setNeedCloseRecord(false);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.notifyDownloadFinish", data, null);
                        try {
                            MediaModel model = MediaModel.parseFrom(data);
                            // AppLogic.showToast(getMediaSpeakInfo(model) + "????????????");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case UiMusic.SUBEVENT_MEDIA_NOTIFY_LOCAL_MEDIA_SERVER_PORT: {
                        //AsrManager.getInstance().setNeedCloseRecord(false);
                        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.notifyMediaServerPort", data,
                                null);
                        break;
                    }
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

    /**
     * ???????????????????????????????????????????????????, ???????????????????????????????????????????????????????????????????????????????????????,
     * ??????????????????????????????????????????????????????????????????????????????, ??????????????????true?????????????????????????????????????????????
     * ???????????????????????????.
     *
     * ?????????????????????????????????, ??????????????????????????????.
     *
     * @return
     */
    @Deprecated
    public boolean hasRemoteProcTool() {
        return true;
    }

    public String getDisableResaon() {
        IMediaTool tool = MusicPriorityChooser.getInstance().getMediaTool(null);
        if (null != tool) {
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
            if (serviceName.equals(RemoteMusicTool.getInstance().getPackageName())) {
                invokeTXZMusic(null, "cleartool", null);
            }
        }
    };

    // String mAudioFocusLogicService = null;
    // Integer mAudioFocusStreamType = null;

    public byte[] invokeTXZMusic(final String packageName, String command, final byte[] data) {
        JNIHelper.logd(TAG + "receiver:command:" + command + ",from:" + packageName);
        if (command.startsWith("tongting.")) {
            return TongtingManager.getInstance().preInvoke(packageName, command.substring("tongting.".length()), data);
        }
        if (command.startsWith("sdk.")) {
            return RemoteMusicTool.getInstance().onMusicSdkInvoke(packageName, command.substring
                    ("sdk.".length()), data);
        }

        if (command.equals("dataInterface")) {
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_AUDIO, UiAudio.SUBEVENT_REQ_DATA_INTERFACE, data);
            return null;
        }

        if ("getUid".equals(command)) {
            return String.valueOf(ProjectCfg.getUid() == null ? 0 : ProjectCfg.getUid()).getBytes();
        }
        if ("getAppid".equals(command)) {
            return (StringUtils.isNotEmpty(LicenseManager.getInstance().getAppId())
                    ? LicenseManager.getInstance().getAppId() : "").getBytes();
        }
        if ("getCoreVersion".equals(command)) {
            return GlobalContext.get().getString(R.string.app_version).getBytes();
        }
        if ("setTTMusicControlTaskId".equals(command)) {
            HelpGuideManager.getInstance().setCustomMusicTaskId(new String(data));
            return null;
        }

        // ??????????????????????????????
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
                if (PluginManager.invoke("com.music.selector.do", MusicPluginVersion) != null) {// ???????????????
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

        // syncmusiclist ??????????????????????????????json array?????????????????????
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
                if (PluginManager.invoke("com.music.selector.do", MusicPluginVersion) != null) {//???????????????
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
        // ??????????????????
        if (command.equals("audioLogic.tts")) {
            try {
                MusicFocusManager.getInstance().setAudioLogicWhenTts(AudioLogicType.valueOf(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("audioLogic.asr")) {
            try {
                MusicFocusManager.getInstance().setAudioLogicWhenAsr(AudioLogicType.valueOf(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("audioLogic.call")) {
            try {
                MusicFocusManager.getInstance().setAudioLogicWhenCall(AudioLogicType.valueOf(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if ("setAudioFocusStreamType".equals(command)) {
            try {
                MusicFocusManager.getInstance().setmAudioFocusStreamType(Integer.parseInt(new String(data)));
            } catch (Exception e) {
            }
            return null;
        }
        if ("setAudioFocusLogic".equals(command)) {
            MusicFocusManager.getInstance().setFocusLogic(packageName);
            return null;
        }
        if ("clearAudioFocusLogic".equals(command)) {
            MusicFocusManager.getInstance().setFocusLogic("");
            return null;
        }
        if ("notifyAudioFocusChange".equals(command)) {
            try {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                Integer focusChange = jsonBuilder.getVal("focusChange", Integer.class, null);
                if (null != focusChange) {
                    MusicFocusManager.getInstance().notifyAudioFocusChangeByRemote(focusChange);
                }
            } catch (Exception e) {
                LogUtil.loge("notifyAudioFocusChange Exception " + e.getMessage());
            }
            return null;
        }

        // ????????????????????????????????????????????????
        //final IMusic musicTool = getMusicTool();
        // ????????????????????????
        if (command.equals("isPlaying")) {
            return String.valueOf(IMediaTool.PLAYER_STATUS.PLAYING ==
                    MediaPriorityManager.getInstance().getCurrentMediaToolStatus()).getBytes();
        }
        // ????????????????????????
        if (command.equals("isBuffering")) {
            return String.valueOf(IMediaTool.PLAYER_STATUS.BUFFERING ==
                    MediaPriorityManager.getInstance().getCurrentMediaToolStatus()).getBytes();
        }
        // ????????????????????????
        if (command.equals("getCurrentMusicModel")) {
            // if (!ProjectCfg.isFixCallFunction()) {
            // if (!TextUtils.isEmpty(mMusicToolServiceName)) {
            // return mRemoteMusicToolgetCurrentMusicModel;
            // }
            // }
            return null;
        }
        // music??????????????????
        if (command.equals("inner.isPlaying")) {
            // ???????????????????????????
            MusicTongTing.getInstance().updateStatus(Boolean.parseBoolean(new String(data)) ?
                    IMediaTool.PLAYER_STATUS.PLAYING : IMediaTool.PLAYER_STATUS.PAUSED);
            /*musicTool.setStatus(Boolean.parseBoolean(new String(data)) ? MusicToolStatusListener.STATE_START_PLAY
                    : MusicToolStatusListener.STATE_PAUSE_PLAY);
            if (TextUtils.isEmpty(MusicRemoteImpl.getInstance().getPackageName())) {
                if (musicTool.isPlaying()) {
                    onBeginMusic();
                } else {
                    onEndMusic();
                }
            }*/
            return null;
        }
        if (command.equals("inner.progress")) {
            /*if (((AbstractMusicComm) musicTool).getStatusListener() != null) {
                JSONBuilder json = new JSONBuilder(data);
                ((AbstractMusicComm) musicTool).getStatusListener().onProgress(
                        json.getVal(MusicToolStatusListener.PROCESS_POSITION, int.class),
                        json.getVal(MusicToolStatusListener.PROCESS_DURATION, int.class));
            }*/
            return null;
        }

        // music??????????????????
        if (command.equals("inner.isBuffering")) {
            // ???????????????????????????
            MusicTongTing.getInstance().updateStatus(IMediaTool.PLAYER_STATUS.BUFFERING);
           /* musicTool.setStatus(Boolean.parseBoolean(new String(data)) ? MusicToolStatusListener.STATE_BUFFERING
                    : MusicToolStatusListener.STATE_PAUSE_PLAY);
            if (TextUtils.isEmpty(MusicRemoteImpl.getInstance().getPackageName())) {
                if (musicTool.isBuffering()) {
                    onBeginMusic();
                } else {
                    onEndMusic();
                }
            }*/
			return null;
		}
		if (command.equals("inner.musicModel")) {
			TXZMusicManager.MusicModel musicModel =
					TXZMusicManager.MusicModel.fromString(new String(data));
            MusicTongTing.getInstance().updatePlayingModel(com.txznet.txz.component.media.model
                    .MediaModel.fromMusicModel(musicModel), MusicTongTing.TT_MODEL_TYPE.MUSIC);
            /*MusicTongTing.getInstance().updatePlayingModel(com.txznet.txz.component.media.model
                    .MediaModel.fromMusicModel(musicModel));
            AudioTongTing.getInstance().updatePlayingModel(null);*/
			return null;
		}
		if (command.equals("inner.audioModel")) {
			TXZMusicManager.MusicModel musicModel =
					TXZMusicManager.MusicModel.fromString(new String(data));
            MusicTongTing.getInstance().updatePlayingModel(com.txznet.txz.component.media.model
                    .MediaModel.fromMusicModel(musicModel), MusicTongTing.TT_MODEL_TYPE.AUDIO);
            /*MusicTongTing.getInstance().updatePlayingModel(null);
            AudioTongTing.getInstance().updatePlayingModel(com.txznet.txz.component.media.model
					.MediaModel.fromMusicModel(musicModel));*/
            return null;
		}

        /*if (command.equals("inner.audioModel.next")) {// ?????????
            // ??????????????????????????????????????????????????????????????????
            if (musicTool instanceof ITxzMedia) {
                ((ITxzMedia) musicTool).nextAudio(MusicModel.fromString(new String(data)));
            }
            return null;
        }*/
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
                MusicTongTing.getInstance().collect();
                // MediaModel m = MediaModel.parseFrom(data);
                // favouriteMusic(m, true);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("inner.unfavourMusic")) {
            try {
                MusicTongTing.getInstance().unCollect();
                // MediaModel m = MediaModel.parseFrom(data);
                // favouriteMusic(m, false);
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
            MusicTongTing.getInstance().playRandom();
            return null;
        }
        if (command.equals("txztool.playFavourMusic")) {
            if (MusicTongTing.getInstance().supportPlayCollection()) {
                MusicTongTing.getInstance().playCollection();
            }
            return null;
        }
        if (command.equals("txztool.favourMusic")) {
            if (MusicTongTing.getInstance().supportCollect()) {
                MusicTongTing.getInstance().collect();
            }
            return null;
        }
        if (command.equals("txztool.unfavourMusic")) {
            if (MusicTongTing.getInstance().supportUnCollect()) {
                MusicTongTing.getInstance().unCollect();
            }
            return null;
        }
        if (command.equals("txztool.playMusic")) {
            try {
                MusicTongTing.getInstance().play(
                        com.txznet.txz.component.media.model.MediaModel.fromMusicModel(
                                MusicModel.fromString(new String(data))));
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
        // ???????????????
        if (command.equals("cleartool")) {
            RemoteMusicTool.getInstance().clearPackageName();
            ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
            invokeTXZMusic(null, "notifyMusicStatusChange", null); // ?????????????????????????????????
            return null;
        }
        if (command.equals("settool")) {
            // ???????????????????????????????????????, ???????????????????????????
            // ?????????????????????????????????
            //MusicPriorityChooser.getInstance().setDefaultTool("");
            /*
            * ????????????????????????
            * 1. ?????????sdk?????????settool????????????????????????(null == data)
            * 2. ???????????????sdk?????????????????????????????????boolean??????(????????????tts)
            * 3. ?????????sdk?????????????????????????????????json, e.g. {"version"=2,"interceptTts"=false}
            * */

            // ????????????, ???????????????sdk
            if (null == data) {
                RemoteMusicTool.getInstance().setPackageName(packageName, 1, null);
                return null;
            }

            // ?????????json??????????????????
            JSONBuilder paramBuilder = new JSONBuilder(data);
            Integer version = paramBuilder.getVal(InvokeConstants.PARAM_SDK_VERSION, Integer.class);
            // ????????????json???????????????json???????????????
            if (null != version) {
                RemoteMusicTool.getInstance().setPackageName(packageName, version, paramBuilder);
                return null;
            } else {
                // json????????????, ???boolean??????????????????
                RemoteMusicTool.getInstance().setPackageName(packageName, 1, null);
            }

            ServiceManager.getInstance().addConnectionListener(mConnectionListener);

            return null;
        }
        if (command.equals("setInnerTool")) {
            String tool = new String(data);
            // ???????????????????????????????????????, ???????????????????????????
            // RemoteMusicTool.getInstance().clearPackageName();
            MusicPriorityChooser.getInstance().setDefaultTool(tool);
            return null;
        }
        // ????????????????????????
        if (command.equals("notifyMusicStatusChange")) {
            ServiceManager.getInstance().runOnServiceThread(new Runnable() {
                @Override
                public void run() {
                    if (!TextUtils.isEmpty(RemoteMusicTool.getInstance().getPackageName())) {
                        ServiceManager.getInstance().sendInvoke(RemoteMusicTool.getInstance().getPackageName(),
                                "tool.music.isPlaying", null, new ServiceManager.GetDataCallback() {
                                    @Override
                                    public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                                        try {
                                            if (data != null)
                                            RemoteMusicTool.getInstance().updateRemotePlayerStatus(
                                                    data.getBoolean() ?
                                                            IMediaTool.PLAYER_STATUS.PLAYING
                                                            : IMediaTool.PLAYER_STATUS.PAUSED);
                                        } catch (Exception e) {
                                        }
                                    }
                                });
                        ServiceManager.getInstance().sendInvoke(RemoteMusicTool.getInstance().getPackageName(),
                                "tool.music.getCurrentMusicModel", null, new ServiceManager.GetDataCallback() {
                                    @Override
                                    public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                                        if (data != null) {
                                            // ???sdk?????????????????????TXZMusicManager.MusicModel?????????JSON
                                            // ????????????, ????????????MediaModel
                                            TXZMusicManager.MusicModel model = TXZMusicManager
                                                    .MusicModel.fromString(
                                                            new String(data.getBytes()));
                                            RemoteMusicTool.getInstance().updateRemotePlayingModel(
                                                    com.txznet.txz.component.media.model.MediaModel
                                                            .fromMusicModel(model));
                                        }
                                    }
                                });
                    }
                }
            }, 0);
            return null;
        }

        if (command.equals("showKuwoSearchResult")) {
            Boolean showSearchResult = Boolean.parseBoolean(new String(data));
            MusicPriorityChooser.getInstance().setSearchConfig(MediaToolConstants.PACKAGE_MUSIC_KUWO,
                    showSearchResult, 10000);
            return null;
        }

        if (InvokeConstants.INVOKE_SEARCH_CONFIG.equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String type = builder.getVal(InvokeConstants.PARAM_SEARCH_TOOL_TYPE, String.class);
            boolean showResult = builder.getVal(InvokeConstants.PARAM_SEARCH_SHOW_RESULT,
                    boolean.class);
            int timeout = builder.getVal(InvokeConstants.PARAM_SEARCH_TIMEOUT, int.class);
            MusicPriorityChooser.getInstance().setSearchConfig(type, showResult, timeout);
            return null;
        }

        if (command.equals("syncMuicList")) {
            AndroidMediaLibrary.syncMusicList(MusicModel.collecionFromString(new String(data)));
            return null;
        }
        if (command.equals("syncExMuicList")) {
            //TXZ-10461,???????????????.???????????????????????????,???????????????????????????????????????,???????????????????????????????????????
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
                /*AbstractMusicComm.handleMusicStatusListener(packageName, command.substring("musiclistener.".length()),
                        data);*/
            } catch (Exception e) {
            }
            return null;
        }
        // ?????????????????????????????????
        if (command.equals("play")) {
            MediaPriorityManager.getInstance().open(true, MediaPriorityManager.PRIORITY_TYPE.MUSIC,
                    true);
            return null;
        }
        if (command.equals("cont")) {
            MediaPriorityManager.getInstance().continuePlay(true,
                    MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        // ?????????????????????????????????
        if (command.equals("play.extra")) {
            MediaPriorityManager.getInstance().continuePlay(true,
                    MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        if (command.equals("pause")) {
            MediaPriorityManager.getInstance().pause(true,
                    MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        if (command.equals("exit")) {
            MediaPriorityManager.getInstance().exit(true);
            return null;
        }
        if ("exitAllMusicToolImmediately".equals(command)) {
            JNIHelper.logd("exitAllMusicToolImmediately begin");
            MediaPriorityManager.getInstance().exit(true);
            JNIHelper.logd("exitAllMusicToolImmediately end");
            return null;
        }
        if (command.equals("next")) {
            MediaPriorityManager.getInstance().next(true,
                    MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        if (command.equals("prev")) {
            MediaPriorityManager.getInstance().prev(true,
                    MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        if (command.equals("playFavourMusic")) {
            MediaPriorityManager.getInstance().playCollection(true);
            return null;
        }
        if (command.equals("favourMusic")) {
            MediaPriorityManager.getInstance().collect(true);
            return null;
        }
        if (command.equals("unfavourMusic")) {
            MediaPriorityManager.getInstance().unCollect(true);
            return null;
        }
        if (command.equals("switchModeLoopAll")) {
            MediaPriorityManager.getInstance().switchLoopMode(true, IMediaTool.LOOP_MODE.LIST_LOOP);
            return null;
        }
        if (command.equals("switchModeLoopOnce")) {
            MediaPriorityManager.getInstance().switchLoopMode(true, IMediaTool.LOOP_MODE.SEQUENTIAL);
            return null;
        }
        if (command.equals("addSubscribe")) {
            MediaPriorityManager.getInstance().subscribe(true);
            return null;
        }
        if (command.equals("unSubscribe")) {
            MediaPriorityManager.getInstance().unsubscribe(true);
            return null;
        }
        if (command.equals("open")) {
            MediaPriorityManager.getInstance().open(true, MediaPriorityManager.PRIORITY_TYPE.MUSIC,
                    true);
            return null;
        }
        if ("novel".equals(command)) {
            MusicTongTing.getInstance().requestHistory(command);
            return null;
        }
        if ("all".equals(command)) {
            MusicTongTing.getInstance().requestHistory(command);
            return null;
        }
        if (command.equals("switchModeLoopOne")) {
            MediaPriorityManager.getInstance().switchLoopMode(true, IMediaTool.LOOP_MODE.SINGLE_LOOP);
            return null;
        }
        if (command.equals("switchModeRandom")) {
            MediaPriorityManager.getInstance().switchLoopMode(true, IMediaTool.LOOP_MODE.SHUFFLE);
            return null;
        }
        if (command.equals("switchSong")) {
            MediaPriorityManager.getInstance().next(true, MediaPriorityManager.PRIORITY_TYPE.MUSIC);
            return null;
        }
        if (command.equals("playRandom")) {
            MediaPriorityManager.getInstance().open(true, MediaPriorityManager.PRIORITY_TYPE.MUSIC,
                    true);
            return null;
        }
        if (command.equals("hateMusic")) {
            MusicTongTing.getInstance().hateAudio();
            return null;
        }
        return null;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param index
     * @param showDetail ???????????????????????????????????????
     */
    public void sendResult(int index, boolean showDetail) {
        PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
        if (apkInfo == null) {
            return;
        }
        // 4.0?????????????????????????????????????????????????????????????????????
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
        // ????????????library
        AudioTxzImpl.getAppVersion();
        return super.initialize_AfterLoadLibrary();
    }

    public void sendPreloadIndex(int index) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.preload.index", ("" + index).getBytes(),
                null);
    }

    private String mStartAsrMusicTool = "";

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param mStartAsrMusicTool
     */
    public void setStartAsrMusicTool(String mStartAsrMusicTool) {
        this.mStartAsrMusicTool = mStartAsrMusicTool;
    }

}
