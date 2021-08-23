package com.txznet.music;

import static com.txznet.comm.err.Error.ERROR_CODE_MASK;

public class ErrCode implements
        com.txznet.audio.ErrCode,
        com.txznet.proxy.ErrCode {

    /**
     * 来源于core，例如网络请求底层框架返回错误
     */
    public static final int SOURCE_CORE = 1;

    /**
     * 来源于客户端
     */
    public static final int SOURCE_CLIENT = 4;


    /*
        SOURCE_CODE = 1 (10xxx)，Core返回错误参照
       001 没有找到服务业务不可达
       002 业务超时
       003 业务过载
       004 请求队列已满
       005 没有登陆态
       006 服务器繁忙
       007 协议解析失败
       008 参数错误
       009 客户端请求超时 本地错误
       010 客户端请求发送异常 本地
       011 需要网络才能发送请求
    */
    public static final int ERROR_CORE_NET_NO_SERVER = SOURCE_CORE * ERROR_CODE_MASK + 1;
    public static final int ERROR_CORE_SERVER_TIMEOUT = SOURCE_CORE * ERROR_CODE_MASK + 2;
    public static final int ERROR_CORE_SERVER_OVERLOAD = SOURCE_CORE * ERROR_CODE_MASK + 3;
    public static final int ERROR_CORE_REQUEST_QUEUE_FULL = SOURCE_CORE * ERROR_CODE_MASK + 4;
    public static final int ERROR_CORE_NO_LOGIN = SOURCE_CORE * ERROR_CODE_MASK + 5;
    public static final int ERROR_CORE_SERVER_BUSY = SOURCE_CORE * ERROR_CODE_MASK + 6;
    public static final int ERROR_CORE_PROTOCOL_RESOLUTION_FAILURE = SOURCE_CORE * ERROR_CODE_MASK + 7;
    public static final int ERROR_CORE_PARAM_ERROR = SOURCE_CORE * ERROR_CODE_MASK + 8;
    public static final int ERROR_CORE_NET_TIMEOUT = SOURCE_CORE * ERROR_CODE_MASK + 9;
    public static final int ERROR_CORE_REQUEST_ERROR = SOURCE_CORE * ERROR_CODE_MASK + 10;
    public static final int ERROR_CORE_NOT_NET = SOURCE_CORE * ERROR_CODE_MASK + 11;


    /*
     001 客户端网络请求超时
     002 客户端没有联网
     003 客户端网络请求返回数据为空
     102 文件校验失败
     103 播放地址错误
     104 请求失败/400
     105 资源不可用/403
     106 资源不存在/404
     107 网关异常
     108 IO异常
     109 音频数据访问异常
     110 获取Audio异常
     111 远端播放器进程异常
     112 请求服务异常
     113 请求服务超时
     114 系统播放器异常
     115 空指针异常
     116 文件在播放的过程中路径发生变化
     117 获取播放项超时
     118 获取专辑错误
     119 获取分类错误
     120 播放列表播放到结尾
     121 json解析错误
    public static final int ERROR_CLIENT_NET_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_MASK + 1;
    public static final int ERROR_CLIENT_NET_OFFLINE = SOURCE_CLIENT * ERROR_CODE_MASK + 2;
    public static final int ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL = SOURCE_CLIENT * ERROR_CODE_MASK + 102;
    public static final int ERROR_CLIENT_MEDIA_WRONG_URL = SOURCE_CLIENT * ERROR_CODE_MASK + 103;
    public static final int ERROR_CLIENT_MEDIA_BAD_REQUEST = SOURCE_CLIENT * ERROR_CODE_MASK + 104;
    public static final int ERROR_CLIENT_MEDIA_FILE_FORBIDDEN = SOURCE_CLIENT * ERROR_CODE_MASK + 105;
    public static final int ERROR_CLIENT_MEDIA_NOT_FOUND = SOURCE_CLIENT * ERROR_CODE_MASK + 106;
    public static final int ERROR_CLIENT_MEDIA_GATE_WAY = SOURCE_CLIENT * ERROR_CODE_MASK + 107;
    public static final int ERROR_CLIENT_MEDIA_ERR_IO = SOURCE_CLIENT * ERROR_CODE_MASK + 108;
    public static final int ERROR_CLIENT_MEDIA_BAD_DATA = SOURCE_CLIENT * ERROR_CODE_MASK + 109;
    public static final int ERROR_CLIENT_MEDIA_GET_AUDIO = SOURCE_CLIENT * ERROR_CODE_MASK + 110;
    public static final int ERROR_CLIENT_MEDIA_REMOTE = SOURCE_CLIENT * ERROR_CODE_MASK + 111;
    public static final int ERROR_CLIENT_MEDIA_REQ_SERVER = SOURCE_CLIENT * ERROR_CODE_MASK + 112;
    public static final int ERROR_CLIENT_MEDIA_REQ_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_MASK + 113;
    public static final int ERROR_CLIENT_MEDIA_SYS_PLAYER = SOURCE_CLIENT * ERROR_CODE_MASK + 114;
    public static final int ERROR_CLIENT_MEDIA_NULL_STATE = SOURCE_CLIENT * ERROR_CODE_MASK + 115;
    public static final int ERROR_CLIENT_MEDIA_URL_CHANGE = SOURCE_CLIENT * ERROR_CODE_MASK + 116;
    public static final int ERROR_GET_PLAY_ITEM_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_MASK + 117;
    public static final int ERROR_CLIENT_QUERY_ALBUM_ERROR = SOURCE_CLIENT * ERROR_CODE_MASK + 118; //请求Album返回错误
    public static final int ERROR_CLIENT_QUERY_CATEGORY_ERROR = SOURCE_CLIENT * ERROR_CODE_MASK + 119; //请求category返回出错
    public static final int ERROR_CLIENT_PLAYER_LIST_END = SOURCE_CLIENT * ERROR_CODE_MASK + 120; //播放列表播放到结尾
    public static final int ERROR_CLIENT_JSON_PARSER = SOURCE_CLIENT * ERROR_CODE_MASK + 121;
    */

    /*
      SOURCE_CODE = 4 (40xxx)，内部用这份
     001 客户端网络请求超时
     002 客户端网络请求超时
     003 客户端网络请求数据为空
     004 没有找到相关数据
     106 json解析错误
     107 Core响应异常
     108 服务端响应异常
     40200 //内部错误
     40201 //操作错误
     40202 //时间戳不对
    */
    public static final int ERROR_CLIENT_NET_TIMEOUT = SOURCE_CLIENT * ERROR_CODE_MASK + 1;
    public static final int ERROR_CLIENT_NET_OFFLINE = SOURCE_CLIENT * ERROR_CODE_MASK + 2;
    public static final int ERROR_CLIENT_NET_EMPTY_DATA = SOURCE_CLIENT * ERROR_CODE_MASK + 3;
    public static final int ERROR_CLIENT_NOT_FOUND = SOURCE_CLIENT * ERROR_CODE_MASK + 4;
    public static final int ERROR_JSON_PARSER = SOURCE_CLIENT * ERROR_CODE_MASK + 106;
    public static final int ERROR_CORE_RESP_WRONG = SOURCE_CLIENT * ERROR_CODE_MASK + 107;
    public static final int ERROR_SVR_RESP_WRONG = SOURCE_CLIENT * ERROR_CODE_MASK + 108;

    public static final int ERROR_INNER_WRONG = SOURCE_CLIENT * ERROR_CODE_MASK + 200;
    public static final int ERROR_OPERATION_WRONG = SOURCE_CLIENT * ERROR_CODE_MASK + 201;

    public static final int ERROR_INNER_WRONG_TIME = SOURCE_CLIENT * ERROR_CODE_MASK + 202;


}
