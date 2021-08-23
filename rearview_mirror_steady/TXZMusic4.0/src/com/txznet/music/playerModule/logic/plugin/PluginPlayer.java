//package com.txznet.music.playerModule.logic.plugin;
//
//import android.media.AudioManager;
//
//import com.txznet.audio.bean.SessionInfo;
//import com.txznet.audio.player.FFMPEGAudioPlayer;
//import com.txznet.audio.player.RemoteAudioPlayer;
//import com.txznet.audio.player.SysAudioPlayer;
//import com.txznet.audio.player.audio.PlayerAudio;
//import com.txznet.audio.player.factory.PlayAudioFactory;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.music.baseModule.plugin.CommandString;
//import com.txznet.music.playerModule.logic.PlayInfoManager;
//import com.txznet.txz.plugin.PluginManager;
//
///**
// * 接收插件的请求
// * Created by ASUS User on 2016/12/1.
// */
//
//public class PluginPlayer {
//
//
//    private final static String TAG = ":MUSIC:Plugin: ";
//    //单例
////##创建一个单例类##
//    private volatile static PluginPlayer singleton;
//
//    private PluginPlayer() {
//        PluginManager.addCommandProcessor(CommandString.CLIENT_PLAYER, new PluginManager.CommandProcessor() {
//            @Override
//            public Object invoke(String command, Object[] args) {
//                if (CommandString.PLAYER_REMOTE.equals(command)) {
//                    LogUtil.logd(TAG + "you use RemotePlayer now");
//                    return RemoteAudioPlayer.createAudioPlayer(PlayInfoManager.getInstance().getCurrentAudio());
//                } else if (CommandString.PLAYER_KAOLA.equals(command)) {
////                    LogUtil.logd(TAG + "you use KAOLAPlayer now");
////                    SessionInfo sess = null;
////                    String localUrl = null;
////                    if (args[0] == null || !(args[0] instanceof SessionInfo)) {
////                        //自己创建一个
////                        PlayerAudio playerAudio = PlayAudioFactory.createPlayAudio(PlayInfoManager.getInstance().getCurrentAudio());
////                        sess = new SessionInfo(playerAudio);
////                        localUrl = PlayInfoManager.getInstance().getCurrentAudio() != null ? PlayInfoManager.getInstance().getCurrentAudio().getStrDownloadUrl() : "";
////                    } else {
////                        /*使用它的*/
////                        sess = (SessionInfo) args[0];
////                        localUrl = (String) args[1];
////                    }
////                    return new KaolaTxzPlayer(sess, localUrl);
//                    return null;
//                } else if (CommandString.PLAYER_FFMPEG.equals(command)) {
//                    LogUtil.logd(TAG + "you use FFMPEGPlayer now");
//                    SessionInfo sess = null;
//                    String localUrl = null;
//                    if (args[0] == null || !(args[0] instanceof SessionInfo)) {
//                        //自己创建一个
//                        PlayerAudio playerAudio = PlayAudioFactory.createPlayAudio(PlayInfoManager.getInstance().getCurrentAudio());
//                        sess = new SessionInfo(playerAudio);
//                        localUrl = PlayInfoManager.getInstance().getCurrentAudio() != null ? PlayInfoManager.getInstance().getCurrentAudio().getStrDownloadUrl() : "";
//                    } else {
//                        /*使用它的*/
//                        sess = (SessionInfo) args[0];
//                        localUrl = (String) args[1];
//                    }
//                    return new FFMPEGAudioPlayer(sess, AudioManager.STREAM_MUSIC, localUrl);
//                } else if (CommandString.PLAYER_SYS.equals(command)) {
//                    LogUtil.logd(TAG + "you use MediaPlayer now");
//                    SessionInfo sess = null;
//                    String localUrl = null;
//                    if (args[0] == null || !(args[0] instanceof SessionInfo)) {
//                        //自己创建一个
//                        PlayerAudio playerAudio = PlayAudioFactory.createPlayAudio(PlayInfoManager.getInstance().getCurrentAudio());
//                        sess = new SessionInfo(playerAudio);
//                        localUrl = PlayInfoManager.getInstance().getCurrentAudio() != null ? PlayInfoManager.getInstance().getCurrentAudio().getStrDownloadUrl() : "";
//                    } else {
//                        /*使用它的*/
//                        sess = (SessionInfo) args[0];
//                        localUrl = (String) args[1];
//                    }
//                    return new SysAudioPlayer(sess, AudioManager.STREAM_MUSIC, localUrl);
//                }
//                return null;
//            }
//        });
//    }
//
//    public static PluginPlayer getInstance() {
//        if (singleton == null) {
//            synchronized (PluginPlayer.class) {
//                if (singleton == null) {
//                    singleton = new PluginPlayer();
//                }
//            }
//        }
//        return singleton;
//    }
//
//}
