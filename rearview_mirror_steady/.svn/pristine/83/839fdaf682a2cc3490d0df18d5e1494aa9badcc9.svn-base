package com.txznet.audio;

import static com.txznet.comm.err.Error.ERROR_CODE_MASK;

public interface ErrCode {
    /**
     * 来源于播放器模块
     */
    int SOURCE_PLAYER = 3;

    /*
     100 创建AudioTrack失败
     101 网络请求超时
     102 离线环境
     103 音频资源无法访问
     104 进程错乱
     105 系统播放器异常
     106 播放器创建失败
  */
    int ERROR_AUDIO_TRACK_ERROR = SOURCE_PLAYER * ERROR_CODE_MASK + 100;
    int ERROR_NET_TIMEOUT = SOURCE_PLAYER * ERROR_CODE_MASK + 101;
    int ERROR_NET_OFFLINE = SOURCE_PLAYER * ERROR_CODE_MASK + 102;
    int ERROR_MEDIA_NOT_FOUND = SOURCE_PLAYER * ERROR_CODE_MASK + 103;
    int ERROR_MEDIA_CROSS_PROCESS = SOURCE_PLAYER * ERROR_CODE_MASK + 104;
    int ERROR_MEDIA_SYS_PLAYER = SOURCE_PLAYER * ERROR_CODE_MASK + 105;
    int ERROR_CREATE_PLAYER = SOURCE_PLAYER * ERROR_CODE_MASK + 106;
}
