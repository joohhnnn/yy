package com.txznet.music.baseModule.plugin;

/**
 * 命令字类 不混淆
 * Created by ASUS User on 2016/11/30.
 */
public class CommandString {
    //插件请求客户端的前缀##CLIENT_##开头
    public final static String CLIENT_ENGINE = "music.client.engine.";
    public final static String CLIENT_PLAYER = "music.client.player.";

    //客户端支持的操作
    public final static String PLAYER_REMOTE = "create_remote_player";
    public final static String PLAYER_FFMPEG = "create_ffmpeg_player";
    public final static String PLAYER_KAOLA = "create_kala_player";
    public final static String PLAYER_SYS = "create_system_player";//系统播放器，Mediaplayer


    //客户端请求插件的前缀##PLUGIN_##开头
    public final static String PLUGIN_ENGINE = "music.plugin.engine.";
    public final static String PLUGIN_PLAYER_DOUBLE = "music.plugin.player.double.";//多进程
    public final static String PLUGIN_PLAYER_SINGLE = "music.plugin.player.single.";//单进程

    public final static String PLUGIN_SETTING_AUDIOTRACK = "music.plugin.setting.";//设置
    public static final String AUDIOTRACK = "audioTrack";
    public static final String OPENUI = "openUi";
    public static final String OPEN_UI_PROCESS = "open_ui_process";

    //相应的操作
    public static final String LOADPLUGIN= "loadplugin";//加载插件
    public static final String GETAUDIOPLAYER = "getAudioPlayer";
    public static final String CREATEPLAYER = "createplayer";
    public static final String INIT = "init";
    public static final String PAUSE = "pause";
    public static final String PLAY = "play";
    public static final String PLAYORPAUSE = "playOrPause";
    public static final String PLAYAUDIO = "playAudio";
    public static final String RELEASE = "release";
    public static final String NEXT = "next";
    public static final String LAST = "last";
    public static final String CHANGEMODE = "changeMode";
    public static final String SEEKTO = "seekTo";
    public static final String SETVOLUME = "setVolume";
    public static final String SEARCHLISTDATA = "searchListData";
    public static final String GETCURRENTALBUM = "getCurrentAlbum";
    public static final String GETCURRENTAUDIO = "getCurrentAudio";
    public static final String SETAUDIOS = "setAudios";
    public static final String ADDAUDIOS = "addAudios";
    public static final String ISPLAYING = "isPlaying";
    public static final String PREPAREASYNC = "prepareAsync";


}
