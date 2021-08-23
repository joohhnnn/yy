package com.txznet.music.baseModule.logic;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.ServiceHandler;
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
import com.txznet.music.net.NetManager;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.power.PowerManager;
import com.txznet.music.push.PushIntercepter;
import com.txznet.music.push.PushLogicHelper;
import com.txznet.music.push.PushManager;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.asr.AsrManager;
import com.txznet.music.soundControlModule.logic.SoundCommand;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.ui.InfoPopView;
import com.txznet.music.util.TtsUtilCompatible;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.tongting.IConstantCmd;

import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_INTERCEPT;
import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_NEED_MORE_AUDIOS;
import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_VERSION;

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
    private String isInterceptPlay = "true"; //声控播放收藏和声控播放订阅


    private ServiceEngine() {
//        HandlerThread service = new HandlerThread("Service");
//        service.start();
        mHandler = new CommandHandler(Looper.getMainLooper());
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
        //Note:这里和第三方应用进行了绑定，理论上，除非

        byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
        if (command.startsWith("music.")) {
            ret = invokeMusic(packageName, command.substring("music.".length()), data);
        } else if (command.equals("sdk.init.success")) {
            invokeSetting(packageName, command, null);
        } else if (command.startsWith("audio.")) {
            invokeAudio(packageName, command.substring("audio.".length()), data);
        } else if (command.startsWith("intercept.play")) {
            return isInterceptPlay.getBytes();
        } else if (command.startsWith(IConstantCmd.CMD_PREFIX)) {
            invokeManufacture(packageName, command, data);
        }
        return ret;
    }


    private void invokeAudio(String packageName, String command, byte[] data) {
        mHandler.sendAudioInvoke(packageName, command, data);
    }

    private void invokeSetting(String packageName, String command, byte[] data) {
        mHandler.sendSettingInvoke(packageName, command, data);
    }

    private void invokeManufacture(String packageName, String command, byte[] data) {
        mHandler.sendManufacureInvoke(packageName, command, data);
    }


    private byte[] invokeMusic(final String packageName, String command, byte[] data) {
        if (command.startsWith("tongting.")) {
            String substring = command.substring("tongting.".length());
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
        private static final int WHAT_SETTING_INVOKE = 3;
        private static final int WHAT_MANUFACTURE_INVOKE = 4;

        public CommandHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String packageName = bundle.getString(KEY_PACKAGE_NAME);
            String command = bundle.getString(KEY_COMMAND);
            byte[] data = bundle.getByteArray(KEY_DATA);
            LogUtil.logd(TAG + "from:" + packageName + "/" + command + ",what = " + msg.what);
            switch (msg.what) {
                case WHAT_MUSIC_INVOKE:
                    handleMusicInvoke(packageName, command, data);
                    break;
                case WHAT_AUDIO_INVOKE:
                    handleAudioInvoke(packageName, command, data);
                    break;
                case WHAT_SETTING_INVOKE:
                    handleSettingInvoke(packageName, command, data);
                    break;
                case WHAT_MANUFACTURE_INVOKE:
                    handleManufactureInvoke(packageName, command, data);
                    break;
            }
            super.handleMessage(msg);
        }

        private void handleAudioInvoke(String packageName, String command, byte[] data) {
            if (command.equals("play")) {// 播放电台，需要打开界面
                try {
                    PlayHelper.playRadio(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
                } finally {
                    Utils.jumpTOMediaPlayerAct(false, HomeActivity.RADIO_i);
                }
            } else if (command.equals("open")) {
                Utils.jumpTOMediaPlayerAct(true, HomeActivity.RADIO_i);
            } else if (command.equals("open.play")) {
                try {
                    PlayHelper.playRadio(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
                } finally {
                    Utils.jumpTOMediaPlayerAct(true, HomeActivity.RADIO_i);
                }
            }
        }

        private void handleMusicInvoke(String packageName, String command, byte[] data) {
            //推送通道
            if (command.equals("dataPushInterface")) {
                PushManager.getInstance().handleDataPushInterface(data);
                return;
            }
            //["打开音乐", "播放音乐", "听音乐", "播音乐", "播放歌曲", "听歌曲", "播歌曲", "播歌", "听歌", "我要听音乐", "随便听听", "随意听听", "随便来首歌", "随便来首音乐", "随便来点歌", "你随便唱吧", "好听的歌有哪些", "放首歌听", "放首歌听听", "放首歌"],
            if ("open.play".equals(command) || "open".equals(command)/*这个是之前打开音乐指令的回调*/) {
                TtsUtilCompatible.speakTextOnRecordWin("RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER",
                        Constant.RS_VOICE_SPEAK_OPEN_PLAYER, true, new Runnable() {
                            @Override
                            public void run() {
                                SoundCommand.getInstance().playMusic(data);
                            }
                        });

                return;
            }


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
                boolean viewDetail = false;
                try {
                    // 新版本传的是json
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    index = jsonBuilder.getVal("index", Integer.class, -1);
                    viewDetail = jsonBuilder.getVal("showDetail", Boolean.class, false);
                } catch (Exception e) {
                }
                if (index == -1) {
                    index = Integer.parseInt(new String(data));
                }
                SearchEngine.getInstance().choiceIndex(index, viewDetail);
                return;
            }

            if (command.equals("sound.find")) {
                isClose = false;
                isPlayRandom = false;
                isSearchingData = true;
                String soundData = new String(data);
                SearchEngine.getInstance().doSoundFind(-1, soundData);
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
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                if (currentAudio != null) {
                    if (Utils.isSong(currentAudio.getSid())) {
                        FavorHelper.favor(currentAudio, EnumState.Operation.extra);
                    } else {
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (currentAlbum != null) {
                            FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.extra);
                        }
                    }
                }
                return;
            }
            if (command.equals("unfavourMusic")) {
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                if (currentAudio != null) {
                    if (Utils.isSong(currentAudio.getSid())) {
                        FavorHelper.unfavor(currentAudio, EnumState.Operation.extra);
                    } else {
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (currentAlbum != null) {
                            FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.extra);
                        }
                    }
                }

                return;
            }

            // command.equals("play.inner") "播放音乐"
            if (command.equals("play.inner")) {
                try {
                    LogUtil.logd(TAG + "audioplayer play sound");
                    PlayHelper.playMusic(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
                } finally {
                    Utils.jumpTOMediaPlayerAct(false);
                }
                return;
            }
            //command.equals("start"),方案商执行TXZTongtingManager.getInstance().play()的时候会调用
            if (command.equals("playRandom")) {
                try {
                    isPlayRandom = true;
                    LogUtil.logd(TAG + "audioplayer play sound");
                    PlayHelper.playRandom(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
                } finally {
                    Utils.jumpTOMediaPlayerAct(false);
                }
                return;
            }
            if (command.equals("start")) {
                try {
                    isPlayRandom = true;
                    LogUtil.logd(TAG + "audioplayer play sound");
                    PlayHelper.start(EnumState.Operation.sound);
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

            if ("open".equals(command)) {
                if (PlayEngineFactory.getEngine().getCurrentAudio() != null) {
                    TtsUtil.speakTextOnRecordWin(
                            "RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER",
                            Constant.RS_VOICE_SPEAK_OPEN_PLAYER, true, null);
                    Utils.jumpTOMediaPlayerAct(true);
                } else {
                    TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_TIPS_OPEN",
                            Constant.RS_VOICE_SPEAK_TIPS_OPEN, false, null);
                }
                return;
            }

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
                boolean favour = Boolean.parseBoolean(jsonBuilder.getVal("favour", String.class, "false"));
                final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                final Album currentAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
                if (favour) {
                    FavorHelper.favourAudio(currentAudio, currentAlbum);
                } else {
                    FavorHelper.unFavourAudio(currentAudio, currentAlbum);
                }
                return;
            }

            //"MUSIC_ADD_DESCRIPE" : ["添加快捷方式","订阅","订阅这节目","订阅当前电台","订阅这个专辑","添加到首页","订阅这个电台","我要订阅","订阅电台","订阅这个节目","加入订阅","订阅这个栏目"],
            if ("addSubscribe".equals(command)) {
                final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                final Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                FavorHelper.favourAudio(currentAudio, currentAlbum);
                return;
            }
            //"MUSIC_ADD_UNSUBSCRIBE" : ["取消订阅"],
            if ("unSubscribe".equals(command)) {
                final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                final Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                FavorHelper.unFavourAudio(currentAudio, currentAlbum);
                return;
            }
            // "MEDIA_CMD_PLAY_COLLECT" : ["播放收藏", "播放收藏列表", "播放收藏音乐", "播放收藏音乐列表", "播放收藏歌曲", "播放收藏歌曲列表"],
            if ("play.favour".equals(command)) {
                TtsUtil.speakTextOnRecordWin("", "", true, null);
                PlayHelper.searchPlayCollect(EnumState.Operation.sound);
                return;
            }

            // "MEDIA_CMD_PLAY_SUBSCRIBE" : ["播放订阅", "播放订阅列表", "播放订阅节目", "播放订阅专辑", "播放订阅栏目", "播放订阅电台"],
            if ("play.subscribe".equals(command)) {
//                PlayHelper.playSubscribe(EnumState.Operation.sound);
                TtsUtil.speakTextOnRecordWin("", "", true, null);
                PlayHelper.searchPlaySubscribe(EnumState.Operation.sound, 0, 0);
                return;
            }


            //"MUSIC_HATE" : ["不喜欢这首歌","讨厌这首歌","不好听","这首歌不好听"],
            if ("hate.audio".equals(command)) {
//                FavorHelper.unfavor()
                //TODO:取消收藏和切歌
                PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                return;
            }

            if ("continuePlay".equals(command)) {
                PlayEngineFactory.getEngine().play(EnumState.Operation.extra);
                return;
            }

            if ("playOnlineMusic".equals(command)) {
                PlayHelper.playRandom(PlayInfoManager.DATA_ALBUM, EnumState.Operation.extra);
                return;
            }

            if ("queryOnlineMusic".equals(command)) {
                Log.e("music:sendBroadcast:", "handleMusicInvoke: queryOnlineMusic");
                PlayHelper.playRecommend(PlayInfoManager.DATA_ALBUM, EnumState.Operation.extra, false);
                return;
            }

            if ("queryLocalMusic".equals(command)) {
                Log.e("music:sendBroadcast:", "handleMusicInvoke: queryLocalMusic");
                PlayHelper.playLocalMusic(EnumState.Operation.extra, null, null, false);
                return;
            }

            if ("push.tool.set".equals(command)) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                int version = jsonBuilder.getVal(KEY_PUSH_VERSION, int.class, 0);
                if (version == 1) {
                    int intercept = jsonBuilder.getVal(KEY_PUSH_INTERCEPT, int.class, 0);
                    PushIntercepter.getInstance().setListener(packageName, intercept);
                }
                return;
            }
            if ("push.tool.resume.play".equals(command)) {
                PushIntercepter.getInstance().resumeDelayTask();
                return;
            }
            if ("push.tool.clear".equals(command)) {
                PushIntercepter.getInstance().clearListener();
                return;
            }
            if ("push.tool.need.audios".equals(command)) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                boolean isNeed = jsonBuilder.getVal(KEY_PUSH_NEED_MORE_AUDIOS, boolean.class, false);
                PushIntercepter.getInstance().setNeedAudios(isNeed);
                return;
            }
            if ("push.click.continue".equals(command)) {
                PushIntercepter.getInstance().clickContinue();
                return;
            }
            if ("push.click.cancel".equals(command)) {
                PushIntercepter.getInstance().clickCancel();
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

        public void sendSettingInvoke(String packageName, String command, byte[] data) {
            sendInvoke(WHAT_SETTING_INVOKE, packageName, command, data);
        }

        public void sendManufacureInvoke(String packageName, String command, byte[] data) {
            sendInvoke(WHAT_MANUFACTURE_INVOKE, packageName, command, data);
        }
    }

    private void handleSettingInvoke(String packageName, String command, byte[] data) {
        if ("sdk.init.success".equals(command)) {
            SyncCoreData.syncCurStatusFullStyle();
            AsrManager.getInstance().regCMD();
            if (SharedPreferencesUtils.isOpenPush() && !PushLogicHelper.getInstance().isExecuteShowView()) {
                PushLogicHelper.getInstance().setExecuteShowView(true);
                //将标志位重置，未弹出过界面
                SharedPreferencesUtils.setCanNotShowPushWin(false);
            }
            return;
        }
    }

    private void handleManufactureInvoke(String packageName, String command, byte[] data) {
        ManufacturerInvoker.getInstance(packageName).invoke(packageName, command, data);
    }
}
