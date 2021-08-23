package com.txznet.music.baseModule.logic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.plugin.PluginMusicManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.listener.WinListener;
import com.txznet.music.net.NetManager;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.power.PowerManager;
import com.txznet.music.push.PushManager;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.service.ThirdHelper;
import com.txznet.music.soundControlModule.asr.AsrManager;
import com.txznet.music.ui.InfoPopView;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZTtsManager;

/**
 * Created by ASUS User on 2016/11/21.
 */
public class ServiceEngine {

    private static final String TAG = "Music:ServiceEngine:";
    private static ServiceEngine mInstance;
    private final CommandHandler mHandler;
    private boolean isClose = false;
    private boolean isPlayRandom = false;
    private boolean isSearchingData = false;// 声控搜索中


    private ServiceEngine() {
        HandlerThread service = new HandlerThread("Service");
        service.start();
        mHandler = new CommandHandler(service.getLooper());
    }

    public static ServiceEngine getInstance() {
        if (mInstance == null) {
            synchronized (ServiceEngine.class) {
                if (mInstance == null) {
                    mInstance = new ServiceEngine();
                }
            }
        }
        return mInstance;
    }

    public boolean isClose() {
        return isClose;
    }

    public byte[] sendInvoke(final String packageName, final String command, final byte[] data) {
        byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
        if (command.startsWith("music.")) {
            ret = invokeMusic(packageName, command.substring("music.".length()), data);
        } else if (command.equals("sdk.init.success")) {
            SyncCoreData.syncCurStatusFullStyle();
            AsrManager.getInstance().regCMD();
        } else if (command.startsWith("audio.")) {
            invokeAudio(packageName, command.substring("audio.".length()), data);
        }
        return ret;
    }


    private void invokeAudio(String packageName, String command, byte[] data) {
        mHandler.sendAudioInvoke(packageName, command, data);
    }


    private byte[] invokeMusic(final String packageName, String command, byte[] data) {
        if (command.startsWith("tongting.")) {
            String substring = command.substring("tongting.".length());
            if (substring.equals("continuePlay")) {
                PlayEngineFactory.getEngine().play(EnumState.Operation.extra);
                return null;
            }
            return invokeMusic(packageName, substring, data);
        }
        //TxzTongtingManager.getInstance().getCurrentModel可以调用到该命令字
        if (command.equals("getCurrentMusicModel")) {
            Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();

            if (currentAudio != null) {

                TXZMusicManager.MusicModel musicModel = new TXZMusicManager.MusicModel();
                musicModel.setTitle(currentAudio.getName());
                musicModel.setAlbum(currentAudio.getAlbumName());
                if (CollectionUtils.isNotEmpty(currentAudio.getArrArtistName())) {
//                    ArrayList<String> arrayList = new ArrayList<>();
//                    List<String> list = currentAudio.getArrArtistName();
//                    for (int i = 0; i < list.size(); i++) {
//                        arrayList.add(currentAudio.getArrArtistName().get(i));
//                    }
                    musicModel.setArtist(currentAudio.getArrArtistName().toArray(new String[currentAudio.getArrArtistName().size()]));
                }

                return musicModel.toString().getBytes();
            } else {
                return null;
            }
        }
        if ("isShowUI".equals(command)) {
            if (ActivityStack.getInstance().getsForegroundActivityCount() != 0) {
                return "true".getBytes();
            } else {
                return "false".getBytes();
            }
        }

        if (command.equals("isPlaying")) {
            return String.valueOf(PlayEngineFactory.getEngine().isPlaying()).getBytes();
        }
        if (command.equals("get.version")) {
            return String.valueOf(true).getBytes();
        }

        if (command.equals("dataInterface")) {
            return NetManager.getInstance().handleDataInterface(data);
        }

        //推送通道
        if (command.equals("dataPushInterface")) {
            //2018年5月30日18点10分，应产品要求这里去掉30s的功能
//            PushManager.getInstance().handleDataPushInterface(data);
            return null;
        }
        if ("history.support".equals(command)) {
            return ("" + true).getBytes();
        }

        mHandler.sendMusicInvoke(packageName, command, data);
        return null;
    }


    private class CommandHandler extends Handler {

        private static final String KEY_PACKAGE_NAME = "package_name";
        private static final String KEY_COMMAND = "command";
        private static final String KEY_DATA = "data";

        private static final int WHAT_MUSIC_INVOKE = 1;
        private static final int WHAT_AUDIO_INVOKE = 2;

        public CommandHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String packageName = bundle.getString(KEY_PACKAGE_NAME);
            String command = bundle.getString(KEY_COMMAND);
            byte[] data = bundle.getByteArray(KEY_DATA);

            switch (msg.what) {
                case WHAT_MUSIC_INVOKE:
                    handleMusicInvoke(packageName, command, data);
                    break;
                case WHAT_AUDIO_INVOKE:
                    handleAudioInvoke(packageName, command, data);
                    break;
            }
            super.handleMessage(msg);
        }

        private void handleAudioInvoke(String packageName, String command, byte[] data) {
            if (command.equals("play")) {// 播放电台，需要打开界面
                PlayHelper.playRadio(EnumState.Operation.sound);
            } else if (command.equals("open")) {
                PlayHelper.playRadio(EnumState.Operation.sound);
            } else if (command.equals("open.play")) {
                PlayHelper.playRadio(EnumState.Operation.sound);
            }
        }

        private void handleMusicInvoke(String packageName, String command, final byte[] data) {

            if (command.equals("client.sleep")) {
                LogUtil.logd(TAG + "POWER:: client sleep");
                PowerManager.getInstance().notifySleep();
                return;
            }
            if (command.equals("client.wakeup")) {
                PowerManager.getInstance().notifyWakeUp();
                return;
            }

            if (command.equals("client.exit")) {
                LogUtil.logd(TAG + "POWER:: client exit");
                PowerManager.getInstance().notifyExit();
                return;
            }

            if (command.equals("client.enter_reverse")) {
                PowerManager.getInstance().notifyReverseStart();
                return;
            }

            if (command.equals("client.quit_reverse")) {
                PowerManager.getInstance().notifyReverseEnd();
                return;
            }

            if (command.equals("playmusiclist.index")) {
                LogUtil.logd(TAG + " :" + new String(data));
                int index = -1;
//                boolean viewDetail = false;
//                try {
//                    // 新版本传的是json
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                index = jsonBuilder.getVal("index", Integer.class, -1);
//                    viewDetail = jsonBuilder.getVal("showDetail", Boolean.class, false);
//                } catch (Exception e) {
//                }
//                if (index == -1) {
//                    index = Integer.parseInt(new String(data));
//                }
//                SearchEngine.getInstance().choiceIndex(index, viewDetail);

                ThirdHelper.getInstance().invokeMusic(packageName, "choice", String.valueOf(index).getBytes());
                return;
            }

            if (command.equals("sound.find")) {
                isClose = false;
                isPlayRandom = false;
                isSearchingData = true;
                String soundData = new String(data);
//                SearchEngine.getInstance().doSoundFind(-1, soundData);

                /*
                TtsUtilWrapper.speakVoice(-1, resId, resArgs, sText, null, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    if (oRun != null) {
                        oRun.run();
                    } else {
                        Log.d(TAG, "error search audio null");
                    }
                 */
                TXZTtsManager.getInstance().speakText(Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, new TXZTtsManager.ITtsCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        ThirdHelper.getInstance().invokeMusic(ServiceManager.TXZ, "search", data);
                    }
                });
                return;
            }


            if (command.equals("play")) {
                PlayEngineFactory.getEngine().play(EnumState.Operation.extra);
                return;
            }

            if (command.equals("preload.index")) {
                int position = Integer.parseInt(new String(data));
                SearchEngine.getInstance().searchPreloadIndex(position);
                return;
            }

            if (command.equals("favourMusic")) {
                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
                return;
            }
            if (command.equals("unfavourMusic")) {
                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
                return;
            }

            //command.equals("start"),方案商执行TXZTongtingManager.getInstance().play()的时候会调用
            if (command.equals("play.inner") || command.equals("playRandom") || command.equals("start")) {
                try {
                    isPlayRandom = true;
                    LogUtil.logd(TAG + "audioplayer play sound");
                    PlayHelper.openMusic(EnumState.Operation.sound);
                } finally {
                    Utils.jumpTOMediaPlayerAct(false);
                }
                return;
            }
            if (command.equals("pause")) {
                LogUtil.logd(TAG + "audioplayer pause sound");
                PlayEngineFactory.getEngine().pause(EnumState.Operation.extra);
                return;
            }
            if (command.equals("prev")) {
                PlayEngineFactory.getEngine().last(EnumState.Operation.extra);
                return;
            }
            if (command.equals("next")) {
                PlayEngineFactory.getEngine().next(EnumState.Operation.extra);
                return;
            }
            if (command.equals("exit")) {
                UIHelper.exit();
                return;
            }
            if (command.equals("switchModeLoopAll")) {
                PlayEngineFactory.getEngine().changeMode(EnumState.Operation.sound, PlayerInfo.PLAYER_MODE_SEQUENCE);
                return;
            }
            if (command.equals("switchModeLoopOne")) {
                PlayEngineFactory.getEngine().changeMode(EnumState.Operation.sound, PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE);
                return;
            }
            if (command.equals("switchModeRandom")) {
                PlayEngineFactory.getEngine().changeMode(EnumState.Operation.sound, PlayerInfo.PLAYER_MODE_RANDOM);
                return;
            }
            if (command.equals("switchSong")) {
                PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                return;
            }
            if (command.equals("startappplay")) {
                String value = new String(data);
                LogUtil.logd(TAG + "startappplay::" + value);
                SharedPreferencesUtils.setAppFirstPlay(Boolean.parseBoolean(value));
                return;
            }
            if (command.equals("needAsr")) {
                String value = new String(data);
                LogUtil.logd(TAG + "from" + packageName + ",needAsr::" + value);
                boolean needAsr = Boolean.parseBoolean(value);
                SharedPreferencesUtils.setNeedAsr(needAsr);
                if (!needAsr) {
                    AsrManager.getInstance().unregCMD();
                } else {
                    AsrManager.getInstance().regCMD();
                }
                return;
            }
            if (command.equals("wakeup_default")) {
                String value = new String(data);
                LogUtil.logd(TAG + "from" + packageName + ",wakeup_default::" + value);
                boolean defaultValue = Boolean.parseBoolean(value);
                SharedPreferencesUtils.setWakeupDefaultValue(defaultValue);

                if (defaultValue && SharedPreferencesUtils.getNeedAsr()) {
                    AsrManager.getInstance().regCMD();
                }
                return;
            }
            if (command.equals("wakeup_value")) {
                String value = new String(data);
                LogUtil.logd(TAG + "from" + packageName + " wakeup::" + value);
                boolean bValue = Boolean.parseBoolean(value);
                SharedPreferencesUtils.setWakeupEnable(bValue);
                if (bValue) {
                    AsrManager.getInstance().regCMD();
                    ObserverManage.getObserver().send(InfoMessage.WAKEUP_ENABLE);
                } else {
                    AsrManager.getInstance().unregCMD();
                    ObserverManage.getObserver().send(InfoMessage.WAKEUP_DISABLE);
                }
                return;
            }

            if (command.equals("resume_auto_play")) {
                String value = new String(data);
                LogUtil.d(TAG, "set resume auto play " + value);
                SharedPreferencesUtils.setResumeAutoPlay(Boolean.parseBoolean(value));
                return;
            }
            if (command.equals("searchSize")) {
                String searchSize = new String(data);
                LogUtil.logd(TAG + "searchSize::" + searchSize);
                try {
                    SharedPreferencesUtils.setSearchSize(Long.parseLong(searchSize));
                } catch (Exception e) {
                    LogUtil.logd(TAG + "set search size error：" + searchSize);
                }
                return;
            }
            if ("shortPlayEnable".equals(command)) {
                Boolean isEnable = Boolean.valueOf(new String(data));
                LogUtil.d("shortPlayEnable:" + isEnable);
                SharedPreferencesUtils.setDefaultShortPlayEnable(isEnable);
                return;
            }
            if (command.equals("sound.cancelfind")) {
                if (isSearchingData) {
                    isSearchingData = false;
                    isClose = true;
                    LogUtil.logd(TAG + "sound.cancelfind");
                    MonitorUtil.monitorCumulant(Constant.M_SOUND_CANCLE);
//                Constant.SoundSessionID = -1;
                }
                return;
            }
            if (command.equals("update.playMediaList")) {// 将为您来一大波歌曲
                PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                return;
            }

            if (command.equals("notOpenAppPName")) {
                String openAppName = "";
                LogUtil.logd(TAG + "not openApp package name =" + new String(data));

                JSONBuilder builder = new JSONBuilder(new String(data));
                String[] val = builder.getVal("data", String[].class, null);
                if (!ArrayUtils.isEmpty(val)) {
                    openAppName = val[0];
                }
                SharedPreferencesUtils.setNotOpenAppPName(openAppName);
                return;
            }

            if (command.equals("releaseAudioFocus")) {
                String value = new String(data);
                LogUtil.logd(TAG + "[Audio]set releaseAudioFocus:" + value);
                Boolean isRelease = Boolean.valueOf(value);
                SharedPreferencesUtils.setReleaseAudioFocus(isRelease);
            }

            if (command.equals("maxVolume")) {
                if (!SharedPreferencesUtils.isCloseVolume()) {
                    PlayEngineFactory.getEngine().setVolume(EnumState.Operation.sound, 1.0f);
                }
                return;
            }

            if (command.equals("closeVolume")) {
                boolean close = Boolean.parseBoolean(new String(data));
                SharedPreferencesUtils.setCloseVolume(close);
                // 反注册掉全局唤醒词，在重新注册
                if (close) {
                    AsrManager.getInstance().regCMD();
                }
                AsrManager.getInstance().unregCMD();

                return;
            }
            if ("open.play".equals(command) || "open".equals(command)) {

                if (data != null) {
                    //如果里面有值的话
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    String key = jsonBuilder.getVal("target", String.class, "");
                    if ("audio".equals(key)) {
                        handleAudioInvoke(packageName, command, data);
                        return;
                    }
                }


//                TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_PLAY_MUSIC",
//                        Constant.RS_VOICE_SPEAK_PLAY_MUSIC, true, new Runnable() {
//
//                            @Override
//                            public void run() {
                                PlayHelper.openMusic(EnumState.Operation.sound);
//                            }
//                        });
                return;
            }

//            if ("open".equals(command)) {
//                if (PlayEngineFactory.getEngine().getCurrentAudio() != null) {
//                    TtsUtilWrapper.speakTextOnRecordWin(
//                            "RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER",
//                            Constant.RS_VOICE_SPEAK_OPEN_PLAYER, true, null);
//                    Utils.jumpTOMediaPlayerAct(true);
//                } else {
//                    TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_TIPS_OPEN",
//                            Constant.RS_VOICE_SPEAK_TIPS_OPEN, false, null);
//                }
//                return;
//            }

            if ("searchPath".equals(command)) {
                // 设置本地搜索路径
                String string = new String(data);
                SharedPreferencesUtils.setLocalPaths(string);
                LogUtil.logd(TAG + "[Service] set local paths:" + string);
                return;
            }

            //加载插件
            if ("loadPlugin".equals(command)) {
                LogUtil.logd(TAG + "load plugin");
                PluginMusicManager.getInstance().scanLocalPlugin();
                return;
            }
            if ("param.tips.show".equals(command)) {
                InfoPopView.getInstance().removeObserver();
                return;
            }
            if ("sound.history.find".equals(command)) {
                isClose = false;
                isPlayRandom = false;
                String soundData = new String(data);
                SearchEngine.getInstance().doSearchHistory(soundData);
                return;
            }
            if ("sound.history.cancelfind".equals(command)) {
                if (isSearchingData) {
                    isSearchingData = false;
                    isClose = true;
                    LogUtil.logd(TAG + "sound.cancelfind");
                    MonitorUtil.monitorCumulant(Constant.M_SOUND_CANCLE);
                }
                return;
            }
            //	"MUSIC_FAVOURITE" : ["收藏","收藏歌曲","收藏音乐","收藏当前歌曲","收藏当前音乐","收藏这首歌","喜欢这首歌","我要收藏","收藏音乐","收藏这首歌","加入收藏","收藏歌曲"],
            //"MUSIC_CANCEL_FAVOURITE" : ["取消收藏","取消收藏这首歌","取消收藏当前歌曲"],
            if ("updateFavour".equals(command)) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                Boolean favour = Boolean.parseBoolean(jsonBuilder.getVal("favour", String.class, "false"));
                final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                Album currentAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
                if (currentAudio != null) {
                    if (Utils.isSong(currentAudio.getSid())) {
                        if (WinListener.isShowSoundUI) {
                            if (favour) {
                                TtsUtilWrapper.speakTextOnRecordWin("即将为您收藏", true, new Runnable() {
                                    @Override
                                    public void run() {
                                        FavorHelper.favor(currentAudio, EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtilWrapper.speakTextOnRecordWin("即将为您取消收藏", true, new Runnable() {
                                    @Override
                                    public void run() {
                                        FavorHelper.unfavor(currentAudio, EnumState.Operation.sound);
                                    }
                                });
                            }
                        } else {
                            if (favour) {
                                FavorHelper.favor(currentAudio, EnumState.Operation.sound);
                            } else {
                                FavorHelper.unfavor(currentAudio, EnumState.Operation.sound);
                            }
                        }
                    } else {
                        if (currentAlbum != null) {
                            if (favour) {
                                TtsUtilWrapper.speakText("您应该说订阅哦");
                                FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.sound);
                            } else {
                                TtsUtilWrapper.speakText("您应该说取消订阅哦");
                                FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.sound);
                            }
                        }
                    }
                } else {
                    TtsUtilWrapper.speakText("当前还没有播放任何歌曲");
                }
                return;
            }

            //"MUSIC_ADD_DESCRIPE" : ["添加快捷方式","订阅","订阅这节目","订阅当前电台","订阅这个专辑","添加到首页","订阅这个电台","我要订阅","订阅电台","订阅这个节目","加入订阅","订阅这个栏目"],
            if ("addSubscribe".equals(command)) {
                Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                if (null != currentAudio && Utils.isSong(currentAudio.getSid())) {
                    TtsUtilWrapper.speakText("您应该说收藏哦");
                    FavorHelper.favor(currentAudio, EnumState.Operation.sound);
                } else if (null != currentAlbum) {
                    FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.sound);
                } else {
                    TtsUtilWrapper.speakText("当前没有播放节目哦");
                }
                return;
            }
            //"MUSIC_ADD_UNSUBSCRIBE" : ["取消订阅"],
            if ("unSubscribe".equals(command)) {
                Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                if (null != currentAudio && Utils.isSong(currentAudio.getSid())) {
                    TtsUtilWrapper.speakText("您应该说收藏哦");
                    FavorHelper.unfavor(currentAudio, EnumState.Operation.sound);
                } else if (null != currentAlbum) {
                    FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.sound);
                } else {
                    TtsUtilWrapper.speakText("当前没有播放节目哦");
                }
                return;
            }
            //"MUSIC_PLAY_FAVOURITE_LIST" : ["播放收藏","播放收藏列表","播放收藏音乐","播放收藏音乐列表","播放收藏歌曲","播放收藏歌曲列表"],
            if ("play.favour".equals(command)) {
                TtsUtilWrapper.speakText("暂不支持哦");
                return;
            }
            //"MUSIC_HATE" : ["不喜欢这首歌","讨厌这首歌","不好听","这首歌不好听"],
            if ("hate.audio".equals(command)) {
//                FavorHelper.unfavor()
                //TODO:取消收藏和切歌
                PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                return;
            }


        }


        private void sendInvoke(int what, String packageName, String command, byte[] data) {
            Message message = Message.obtain();
            message.what = what;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_PACKAGE_NAME, packageName);
            bundle.putString(KEY_COMMAND, command);
            bundle.putByteArray(KEY_DATA, data);
            message.setData(bundle);
            sendMessage(message);
        }

        public void sendMusicInvoke(String packageName, String command, byte[] data) {
            sendInvoke(WHAT_MUSIC_INVOKE, packageName, command, data);
        }

        public void sendAudioInvoke(String packageName, String command, byte[] data) {
            sendInvoke(WHAT_AUDIO_INVOKE, packageName, command, data);
        }
    }
}
