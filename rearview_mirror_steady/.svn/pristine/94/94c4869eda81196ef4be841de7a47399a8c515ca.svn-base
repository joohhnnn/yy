package com.txznet.audio.player;

/**
 * Created by telenewbie on 2017/6/6.
 */

public interface PlayerServiceConstants {
    String KEY_AUDIO = "audio";
    String KEY_PID = "pid";
    String KEY_KEY = "key";
    String KEY_SID = "sid";
    String KEY_SEEK_TIME = "seek_time";
    //    String KEY_PERCENT = "percent";
    String KEY_DURATION = "duration";
    String KEY_POSTION = "position";
    String KEY_FORCE_MORE = "force_more_data";
    String KEY_REDUCE_VOLUME = "key_reduce_volume";
    String KEY_ERR = "errInfo";
    String KEY_BUFFERS = "buffers";
    String KEY_PLAY_ITEM = "play_item";

    /**
     * client
     */
    int CLIENT_ACTION_BIND = 86998;    //客户端绑定
    int CLIENT_ACTION_UNBIND = 86999;    //客户端解绑
    int CLIENT_ACTION_THEURL = 87000;    //设置新URL并播放
    int CLIENT_ACTION_THEURL_SUB = 87001;    //设置新URL给子播放器并播放
    int CLIENT_ACTION_PLAY = 87002;    //通知播放器播放
    int CLIENT_ACTION_PAUSE = 87003;    //通知播放器暂停
    int CLIENT_ACTION_STOP = 87004;    //通知播放器停止
    int CLIENT_ACTION_DESTROY = 87005;    //通知播放器销毁
    int CLIENT_ACTION_SEEK = 87010;    //设置seek并播放
    int CLIENT_ACTION_RESET = 87011;    //通知播放器重置
    int CLIENT_ACTION_SET_AUDIO = 87012;    //客户端绑定音频。
    int CLIENT_ACTION_FORCE_NEED_MORE_DATA = 87013;    //强制加载更多数据。
    int CLIENT_ACTION_EXIT = 87014;    //客户端退出
    int CLIENT_ACTION_REDUCE_VOLUME = 87015;    //降低音量

    /**
     * service
     */

    int SERVICE_ACTION_PAUSED = 88000;    //通知客户端暂停
    int SERVICE_ACTION_STOPED = 88001;    //通知客户端停止
    int SERVICE_ACTION_PLAYING = 88002;    //通知客户端播放
    int SERVICE_ACTION_DISTORIED = 88003;    //通知客户端已销毁
    int SERVICE_ACTION_ERRORINFO = 88004;    //通知客户端有错误消息
    int SERVICE_ACTION_COMPLETED = 88005;    //通知客户端播放结束（不一定是整个音频播放到结尾，请根据其它参数结合着处理）
    int SERVICE_ACTION_PREPARING = 88006;    // 播放器准备阶段n

    //     int SERVICE_ACTION_RELEASE = 88015;    //马上释放
//     int SERVICE_ACTION_LAZYPLAY = 88007;    //延迟播放
//     int SERVICE_ACTION_PREPARE_BUFFER = 88009;    //准备ＢＵＦＦＥＲ发送
//     int SERVICE_ACTION_SEND_BUFFER = 88010;    //服务端ＢＵＦＦＥＲ发送
//     int SERVICE_ACTION_PREPARE_UPDATE = 88011;    //准备UPDATE发送
//     int SERVICE_ACTION_SEND_UPDATE = 88012;    //服务端UPDATE发送
//     int SERVICE_ACTION_SEND_PAUSED = 88013;    //服务端pause发送
    int SERVICE_ACTION_BUFFERING = 88014;
    int SERVICE_ACTION_BUFFER_READY = 88015;    //Buffer ready
    //    int SERVICE_ACTION_SEEK_START = 88022;     //Seek start
    int SERVICE_ACTION_SEEK_READY = 88016;     //Seek ready
    int SERVICE_ACTION_DOWNLOADING = 88017;    // 下载进度
    int SERVICE_ACTION_LOAD_LOCALFILE = 88018; // 是否读取本地预缓存文件
    int SERVICE_ACTION_PROGRESS = 88019; // 音频播放进度

    int SERVICE_ACTION_BUFFERING_START = 88020; // M3U8音频缓冲开始
    int SERVICE_ACTION_BUFFERING_END = 88021; // M3U8音频缓冲结束
    int SERVICE_ACTION_BUFFERING_DOWNLOAD = 88022; // M3U8音频缓冲结束
}
