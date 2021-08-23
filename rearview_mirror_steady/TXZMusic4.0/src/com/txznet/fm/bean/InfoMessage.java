package com.txznet.fm.bean;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Audio;

import java.util.List;

/**
 * 观察的消息传递类
 *
 * @author ASUS User
 */
public class InfoMessage {

    //刷新本地播放列表
    public static final String REFRESH_LOCAL_AUDIO_LIST = "refresh_local_audio_list";

    //刷新最近音乐列表
    public static final String REFRESH_HISTORY_MUSIC_LIST = "refresh_history_music_list";
    //刷新最近音乐列表
    public static final String REQUERY_HISTORY_MUSIC_LIST = "requery_history_music_list";

    //刷新最近电台列表
    public static final String REFRESH_HISTORY_RADIO_LIST = "refresh_history_radio_list";

    //刷新不存在的歌曲列表
    public static final String REFRESH_NOT_EXIT_AUDIO = "refresh_not_exit_audio";

    // 扫描完成
    public static final String SCAN_FINISHED = "SCAN_FINISHED";

    // 扫描开始
    public static final String SCAN_STATED = "SCAN_STATED";

    //播放状态
    public static final String PLAY = "PLAY";

    //暂停状态
    public static final String PAUSE = "PAUSE";

    // 播放指定歌曲
    public static final String PLAYCHOICE = "PLAYCHOICE";

    // 更新缓冲进度条
    public static final String UPDATE_BUFFER = "UPDATE_BUFFER";
    // 播放完成
    public static final String PLAY_FINISHED = "PLAY_FINISHED";

    // 播放发生错误
    public static final String PLAY_ERROR = "PLAY_ERROR";
    // 播放进度条
    public static final String PLAY_PROGRESS = "PLAY_PROGRESS";
    public static final String PLAY_DOWNLOAD_COMPLETE = "PLAY_DOWNLOAD_COMPLETE";
    // 删除本地音乐
    public static final String DELETE_LOCAL_MUSIC = "DELETE_LOCAL_MUSIC";
    // 删除本地音乐
    public static final String DELETE_HISTORY = "DELETE_HISTORY";

    // 获取全部的分类
    public static final String REQ_CATEGORY_ALL = "REQ_CATEGORY_ALL";

    // 获取单个分类的数据
    public static final String REQ_CATEGORY_SINGLE = "REQ_CATEGORY_SINGLE";
    //Album的相应
    public static final String RESP_ALBUM = "RESP_ALBUM";
    //InterestTag的响应
    public static final String RESP_INTEREST_TAG = "RESP_INTEREST_TAG";
    //IS_SET_MUSIC_INTEREST_TAG 是否设置过音乐兴趣标签
    public static final String IS_SET_MUSIC_INTEREST_TAG = "IS_SET_MUSIC_INTEREST_TAG";
    //IS_SET_RADIO_INTEREST_TAG 是否设置过电台兴趣标签
    public static final String IS_SET_RADIO_INTEREST_TAG = "IS_SET_RADIO_INTEREST_TAG";
    //AlbumList请求返回未知错误
    public static final String RESP_ALBUM_LIST_ERROR_UNKNOWN = "RESP_ALBUM_LIST_ERROR_UNKNOWN";
    //AlbumList请求没有网络
    public static final String RESP_ALBUM_LIST_ERROR_NO_NET = "RESP_ALBUM_LIST_ERROR_NO_NET";
    //AlbumList请求超时
    public static final String RESP_ALBUM_LIST_ERROR_TIMEOUT = "RESP_ALBUM_LIST_ERROR_TIMEOUT";
    //AlbumList为空
    public static final String RESP_ALBUM_LIST_ERROR_NO_DATA = "RESP_ALBUM_LIST_ERROR_NO_DATA";

    //AlbumList请求返回未知错误
    public static final String RESP_ALBUM_AUDIO_ERROR_UNKNOWN = "RESP_ALBUM_AUDIO_ERROR_UNKNOWN";
    //AlbumList请求没有网络
    public static final String RESP_ALBUM_AUDIO_ERROR_NO_NET = "RESP_ALBUM_AUDIO_ERROR_NO_NET";
    //AlbumList请求超时
    public static final String RESP_ALBUM_AUDIO_ERROR_TIMEOUT = "RESP_ALBUM_AUDIO_ERROR_TIMEOUT";
    //AlbumList为空
    public static final String RESP_ALBUM_AUDIO_ERROR_NO_DATA = "RESP_ALBUM_AUDIO_ERROR_NO_DATA";
    //网络不通
    public static final String NET_ERROR = "NET_ERROR";
    //网络超时
    public static final String NET_TIMEOUT_ERROR = "NET_TIMEOUT_ERROR";

    /////////////////////////////////播放列表的通知
    //
    public static final String PLAY_LIST_RESP_ALBUM_AUDIO_ERROR_NO_DATA = "PLAY_LIST_RESP_ALBUM_AUDIO_ERROR_NO_DATA";
    //
    public static final String PLAY_LIST_NET_ERROR = "PLAY_LIST_NET_ERROR";
    //
    public static final String PLAY_LIST_NET_TIMEOUT_ERROR = "PLAY_LIST_NET_TIMEOUT_ERROR";
    //正常的响应
    public static final String PLAY_LIST_NORMAL = "PLAY_LIST_NORMAL";
    //加载
    public static final String PLAY_LIST_LOADING = "PLAY_LIST_LOADING";
    //重新加载的回掉
    public static final String PLAY_LIST_RETRY = "PLAY_LIST_RETRY";
    ////////////////////////////////

    //通知本地音乐位置
    public static final String NOTIFY_LOCAL_AUDIO = "NOTIFY_LOCAL_AUDIO";


    //释放掉资源
    public static final String RELEASE = "RELEASE";
    //刷新本地播放位置
    public static final String REFRESH_LOCAL_POSITION = "REFRESH_LOCAL_POSITION";
    //播放列表中没有歌曲
    public static final String PLAYER_NO_SONGS = "PLAYER_NO_SONGS";
    //当前播放的歌曲
    public static final String PLAYER_CURRENT_AUDIO = "PLAYER_CURRENT_AUDIO";
    //缓冲新的音频列表
    public static final String PLAYER_LOADING = "PLAYER_LOADING";
    //更新播放列表中的数据
    public static final String PLAYER_LIST = "PLAYER_LIST";
    //播放器初始化
    public static final String PLAYER_INIT = "PLAYER_INIT";
    //播放模式变化
    public static final String PLAYER_MODE_SINGLE = "PLAYER_MODE_SINGLE";//单曲循环
    //播放模式变化
    public static final String PLAYER_MODE_SEQUENCE = "PLAYER_MODE_SEQUENCE";//顺序播放
    //播放模式变化
    public static final String PLAYER_MODE_RANDOM = "PLAYER_MODE_RANDOM";//随机播放

    //	public static final String PLAYER_MODE_CHANGE = "PLAYER_MODE_CHANGE";
    public static final String PLAYER_LIST_EMPTY = "PLAYER_LIST_EMPTY"; //播放列表为空


    public static final String REQUEST_AUDIO_RESPONSE = "REQUEST_AUDIO_RESPONSE"; //请求Audio列表返回
    public static final String SET_PLAY_LIST_TOTAL_NUM = "SET_PLAY_LIST_TOTAL_NUM"; //设置播放列表音频的总数量

    public static final String ALBUM_REQUEST_MANNUAL_START = "ALBUM_REQUEST_MANNUAL_START"; // 开始请求专辑

    public static final String SHOW_PLAY_LIST = "SHOW_PLAY_LIST"; //显示播放列表


    public static final String WAKEUP_ENABLE = "WAKEUP_ENABLE"; //打开免唤醒
    public static final String WAKEUP_DISABLE = "WAKEUP_DISABLE"; //关闭免唤醒

    public static final String FAVOUR_MUSIC = "FAVOUR_MUSIC"; //收藏音乐
    public static final String UNFAVOUR_MUSIC = "UNFAVOUR_MUSIC"; //取消收藏音乐
    public static final String SUBSCRIBE_RADIO = "SUBSCRIBE_RADIO"; //订阅电台
    public static final String UNSUBSCRIBE_RADIO = "UNSUBSCRIBE_RADIO"; //取消订阅电台
    public static final String UNSUBSCRIBE_MULTI_RADIO = "UNSUBSCRIBE_MULTI_RADIO"; //取消订阅电台


    public static final String MESSAGE_NEW_UNREAD = "MESSAGE_UNREAD"; //有新的未读消息
    public static final String MESSAGE_NEW_READ = "MESSAGE_READ";   //有新的已读消息
    public static final String MESSAGE_CLEAR_UNREAD = "MESSAGE_CLEAR_UNREAD"; //清除未读标志


    // -- 触发逻辑，未实际执行
    public static final String BE_PLAY_OR_PAUSE = "BE_PLAY_OR_PAUSE"; // 即将播放或暂停
    public static final String BE_PLAY_NEXT = "BE_PLAY_NEXT"; // 即将播放下一首
    public static final String BE_PLAY_LAST = "BE_PLAY_LAST"; // 即将播放下一首
    public static final String UPDATE_CAR_FM_CURRENT_TIME = "UPDATE_CAR_FM_CURRENT_TIME";

    public static final String SCREEN_UPDATE = "SCREEN_UPDATE";


    public static final String SCREEN_TYPE_CHANGED = "SCREEN_TYPE_CHANGED"; // screenType发生改变


    // 当前消息的类型
    private String type;

    private Audio mAudio;

    // 缓冲进度
    private List<LocalBuffer> buffers;

    // 存放的内容
    private Object obj;

    private int errCode;
    private String errMessage;

    public InfoMessage() {

    }

    public InfoMessage(String type) {
        super();
        this.type = type;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public List<LocalBuffer> getBuffers() {
        return buffers;
    }

    public void setBuffers(List<LocalBuffer> buffers) {
        this.buffers = buffers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Audio getmAudio() {
        return mAudio;
    }

    public void setmAudio(Audio mAudio) {
        this.mAudio = mAudio;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

}
