package com.txznet.proxy;

public class Constant {
    public static final String SPEND_TAG = "proxy:spend:: ";
    public static final String OOM_TAG = "proxy:oom: ";
    public static final String PRELOAD_TAG = "proxy:preload:: ";
    public static final boolean ISTESTDATA = false;// TXZFileConfigUtil.getBooleanSingleConfig("isTest", false);//
    public static final boolean ISTEST = ISTESTDATA;//TXZFileConfigUtil.getBooleanSingleConfig("isTest", false);// 测试数据（写数据到文件测试）

    public static final int PREPARE_BUFFER_DATA_TIME = 2 * 60 * 1000; // 预缓冲数据时长，决定碎片大小，默认30s
    public static final int NEED_BUFFER_DATA_TIME = 60 * 1000 /* 15000 */; // 需要开始缓冲的数据时长，默认15s

    //monitor上报字段
    public static final String M_SOCKET_READ_EMPTY = "fm.empty.w.socket.read";// Socket读取不到数据,读取到的数据为0
    public static final String M_URL_PLAY_ERROR = "fm.play.E.url";// 播放路径问题


    //请求一块碎片的大小
    public static final int PROXY_DATA_PIECE_SIZE = 6 * PREPARE_BUFFER_DATA_TIME;//1ms大概6个字节，此处设置请求一块为60s的数据
    public static final int DATA_PIECE_SIZE_MIN = 1024 * 4 * 8 * 2;//原本32k


}
