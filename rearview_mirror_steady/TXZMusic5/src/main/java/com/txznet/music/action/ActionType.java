package com.txznet.music.action;

/**
 * 行为定义表
 * 所有行为在这里定义
 *
 * @author zackzhou
 * @date 2018/12/3,10:54
 */
public class ActionType {
    // 系统相关
    public static final String ACTION_RECORD_WIN_SHOW = "ACTION_RECORD_WIN_SHOW";
    public static final String ACTION_RECORD_WIN_DISMISS = "ACTION_RECORD_WIN_DISMISS";
    public static final String ACTION_MEDIA_MOUNTED = "ACTION_MEDIA_MOUNTED"; // 设备安装
    public static final String ACTION_MEDIA_EJECT = "ACTION_MEDIA_EJECT"; // 设备卸载
    public static final String ACTION_MEDIA_SCANNER_STARTED = "ACTION_MEDIA_SCANNER_STARTED"; // 系统开始扫描

    // 主页
    public static final String ACTION_HOME_GET_RECOMMEND_PAGE_DATA = "ACTION_HOME_GET_RECOMMEND_PAGE_DATA"; // 获取主页推荐数据
    public static final String ACTION_HOME_GET_MUSIC_PAGE_DATA = "ACTION_HOME_GET_MUSIC_PAGE_DATA"; // 获取主页音乐数据
    public static final String ACTION_HOME_GET_RADIO_PAGE_DATA = "ACTION_HOME_GET_RADIO_PAGE_DATA"; // 获取主页电台数据

    // 本地音乐
    public static final String ACTION_GET_LOCAL = "ACTION_GET_LOCAL"; // 查询本地音频
    public static final String ACTION_SCAN = "ACTION_SCAN"; // 扫描
    public static final String ACTION_SCAN_CANCEL = "ACTION_SCAN_CANCEL"; // 中断扫描
    public static final String ACTION_SCAN_COUNT = "ACTION_SCAN_COUNT"; // 扫描文件数广播
    public static final String ACTION_LOCAL_SORT_BY_TIME = "ACTION_LOCAL_SORT_BY_TIME"; // 按时间排序
    public static final String ACTION_LOCAL_SORT_BY_NAME = "ACTION_LOCAL_SORT_BY_NAME"; // 按名称排序
    public static final String ACTION_LOCAL_DELETE = "ACTION_LOCAL_DELETE"; // 删除本地

    // 历史音乐
    public static final String ACTION_GET_HISTORY_MUSIC = "ACTION_GET_HISTORY_MUSIC"; // 获取历史音乐
    public static final String ACTION_GET_DEL_ITEM_HISTORY_MUSIC = "ACTION_GET_DEL_ITEM_HISTORY_MUSIC"; // 删除历史音乐
    public static final String ACTION_GET_ADD_ITEM_HISTORY_MUSIC = "ACTION_GET_ADD_ITEM_HISTORY_MUSIC"; // 添加历史音乐

    // 历史电台
    public static final String ACTION_GET_HISTORY_ALBUM = "ACTION_GET_HISTORY_ALBUM"; // 获取历史电台
    public static final String ACTION_GET_DEL_ITEM_HISTORY_ALBUM = "ACTION_GET_DEL_ITEM_HISTORY_ALBUM"; // 删除历史电台
    public static final String ACTION_GET_ADD_ITEM_HISTORY_ALBUM = "ACTION_GET_ADD_ITEM_HISTORY_ALBUM"; // 添加历史电台


    // 搜索
    public static final String ACTION_SEARCH_GET_DATA = "ACTION_SEARCH_GET_DATA";
    public static final String ACTION_SEARCH_KEY_KEYWORD = "ACTION_SEARCH_KEY_KEYWORD";
    public static final String ACTION_SEARCH_EVENT_CHOICE_SEARCH_RESULT = "ACTION_SEARCH_EVENT_CHOICE_SEARCH_RESULT";
    public static final String ACTION_SEARCH_KEY_SEARCH_CHOICE = "ACTION_SEARCH_KEY_SEARCH_CHOICE";
    public static final String ACTION_SEARCH_KEY_JUST_INVOKE = "ACTION_SEARCH_KEY_JUST_INVOKE";
    public static final String ACTION_SEARCH_CANCEL = "ACTION_SEARCH_CANCEL";


    //收藏
    public static final String ACTION_FAVOUR_EVENT_GET = "ACTION_FAVOUR_EVENT_GET";
    public static final String ACTION_FAVOUR_EVENT_FAVOUR = "ACTION_FAVOUR_EVENT_FAVOUR";
    public static final String ACTION_FAVOUR_EVENT_UNFAVOUR = "ACTION_FAVOUR_EVENT_UNFAVOUR";

    //订阅
    public static final String ACTION_SUBSCRIBE_EVENT_GET = "ACTION_SUBSCRIBE_EVENT_GET";
    public static final String ACTION_SUBSCRIBE_EVENT_SUBSCRIBE = "ACTION_SUBSCRIBE_EVENT_SUBSCRIBE";
    public static final String ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE = "ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE";

    //设置
    public static final String ACTION_SETTING_CLICK_BOOT_PLAY = "ACTION_SETTING_CLICK_BOOT_PLAY";
    public static final String ACTION_SETTING_CLICK_CHANGE_FLOAT_PLAYER = "ACTION_SETTING_CLICK_CHANGE_FLOAT_PLAYER";
    public static final String ACTION_SETTING_CLICK_OPEN_ASR = "ACTION_SETTING_CLICK_OPEN_ASR";
    public static final String ACTION_SETTING_CLICK_CLEAR_MEMORY = "ACTION_SETTING_CLICK_CLEAR_MEMORY";
    public static final String ACTION_SETTING_CLICK_HELP = "ACTION_SETTING_CLICK_HELP";

    //微信推送
    public static final String ACTION_WXPUSH_EVENT_GET = "ACTION_WXPUSH_EVENT_GET";
    public static final String ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO = "ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO";
    public static final String ACTION_WXPUSH_EVENT_GET_QRCODE = "ACTION_WXPUSH_EVENT_GET_QRCODE";
    public static final String ACTION_WXPUSH_EVENT_SAVE = "ACTION_WXPUSH_EVENT_SAVE";
    public static final String ACTION_WXPUSH_EVENT_DELETE = "ACTION_WXPUSH_EVENT_DELETE";

    //二级专辑详情界面
    public static final String ACTION_ALBUM_EVENT_GET_FROM_CATEGORY = "ACTION_ALBUM_EVENT_GET_FROM_CATEGORY";
    public static final String ACTION_ALBUM_EVENT_POST_ALBUM = "ACTION_ALBUM_EVENT_POST_ALBUM";


    // 播放器
    public static final String ACTION_PLAYER_PLAY = "ACTION_PLAYER_PLAY"; // 播放
    public static final String ACTION_PLAYER_PLAY_MUSIC = "ACTION_PLAYER_PLAY_MUSIC"; // 播放音乐
    public static final String ACTION_PLAYER_PLAY_RADIO = "ACTION_PLAYER_PLAY_RADIO"; // 播放电台
    public static final String ACTION_PLAYER_PAUSE = "ACTION_PLAYER_PAUSE"; // 暂停
    public static final String ACTION_PLAYER_START = "ACTION_PLAYER_START"; // 开始播放
    public static final String ACTION_PLAYER_STOP = "ACTION_PLAYER_STOP"; // 停止
    public static final String ACTION_PLAYER_SEEK_TO = "ACTION_PLAYER_SEEK_TO"; // 跳播
    public static final String ACTION_PLAYER_PLAY_NEXT = "ACTION_PLAYER_PLAY_NEXT"; // 下一首
    public static final String ACTION_PLAYER_PLAY_PREV = "ACTION_PLAYER_PLAY_PREV"; // 上一首
    public static final String ACTION_PLAYER_SET_VOL = "ACTION_PLAYER_SET_VOL"; // 设置音量
    public static final String ACTION_PLAYER_PLAY_OR_PAUSE = "ACTION_PLAYER_PLAY_OR_PAUSE"; // 播放或暂停
    public static final String ACTION_PLAYER_SET_QUEUE = "ACTION_PLAYER_SET_QUEUE"; // 设置队列
    public static final String ACTION_PLAYER_PLAY_ITEM = "ACTION_PLAYER_PLAY_ITEM"; // 播放item
    public static final String ACTION_PLAYER_PLAY_ALBUM = "ACTION_PLAYER_PLAY_ALBUM"; // 播放专辑

    public static final String ACTION_PLAYER_PLAY_LOCAL = "ACTION_PLAYER_PLAY_LOCAL"; // 播放本地
    public static final String ACTION_PLAYER_PLAY_SUBSCRIBE = "ACTION_PLAYER_PLAY_SUBSCRIBE"; // 播放订阅
    public static final String ACTION_PLAYER_PLAY_FAVOUR = "ACTION_PLAYER_PLAY_FAVOUR"; // 播放收藏音乐
    public static final String ACTION_PLAYER_PLAY_HISTORY_MUSIC = "ACTION_PLAYER_PLAY_HISTORY_MUSIC"; // 播放历史音乐
    public static final String ACTION_PLAYER_PLAY_HISTORY_ALBUM = "ACTION_PLAYER_PLAY_HISTORY_ALBUM"; // 播放历史电台
    public static final String ACTION_PLAYER_PLAY_AI = "ACTION_PLAYER_PLAY_AI"; // 播放AI电台
    public static final String ACTION_PLAYER_PLAY_WX_PUSH = "ACTION_PLAYER_PLAY_WX_PUSH"; // 播放微信推送

    // 播放器状态
    public static final String ACTION_PLAYER_ON_AUDIO_FOCUS_CHANGE = "ACTION_PLAYER_ON_AUDIO_FOCUS_CHANGE"; // 音频焦点改变 - extra focusChange: int
    public static final String ACTION_PLAYER_ON_PROGRESS_CHANGE = "ACTION_PLAYER_ON_PROGRESS_CHANGE"; // 播放进度改变 - extra position : Long, duration : Long
    public static final String ACTION_PLAYER_ON_INFO_CHANGE = "ACTION_PLAYER_ON_INFO_CHANGE"; // 播放信息改变 - extra info: AudioV5
    public static final String ACTION_PLAYER_ON_STATE_CHANGE = "ACTION_PLAYER_ON_STATE_CHANGE"; // 播放状态改变 - extra state: Int
    public static final String ACTION_PLAYER_ON_SEEK_COMPLETE = "ACTION_PLAYER_ON_SEEK_COMPLETE"; // 跳转完毕
    public static final String ACTION_PLAYER_ON_COMPLETION = "ACTION_PLAYER_ON_COMPLETION"; // 播放完毕
    public static final String ACTION_PLAYER_ON_ERROR = "ACTION_PLAYER_ON_ERROR"; // 播放出错 -- extra error : Error
    public static final String ACTION_PLAYER_ON_ALBUM_CHANGE = "ACTION_PLAYER_ON_ALBUM_CHANGE"; // 播放专辑改变
    public static final String ACTION_PLAYER_GET_PLAY_INFO = "ACTION_PLAYER_GET_PLAY_INFO"; // 同步播放状态
    public static final String ACTION_PLAY_MODE_CHANGED = "ACTION_PLAYER_MODE_CHANGED"; // 播放模式发生改变

    // AI电台
    public static final String ACTION_AI_RADIO_PUSH = "ACTION_AI_RADIO_PUSH"; // AI电台推送
    public static final String ACTION_AI_RADIO_DELETE = "ACTION_AI_RADIO_DELETE"; // AI电台推送删除

    // 断点记录
    public static final String ACTION_BREAK_POINT_UPDATE = "ACTION_BREAK_POINT_UPDATE"; // 断点记录发生更改

    // 代理
    public static final String ACTION_PROXY_ERROR = "ACTION_PROXY_ERROR"; // 代理错误
    public static final String ACTION_PROXY_DOWNLOAD_COMPLETE = "ACTION_PROXY_DOWNLOAD_COMPLETE"; // 缓冲下载完毕
    public static final String ACTION_PROXY_BUFFERING_UPDATE = "ACTION_PROXY_BUFFERING_UPDATE"; // 缓冲进度改变

    // 播放队列
    public static final String ACTION_PLAYER_QUEUE_ON_CHANGED = "ACTION_PLAYER_QUEUE_ON_CHANGED"; // 播放队列发生变化
    public static final String ACTION_PLAYER_QUEUE_GET = "ACTION_PLAYER_QUEUE_GET"; // 播放队列发生变化
    public static final String ACTION_PLAYER_QUEUE_ON_PLAY_END = "ACTION_PLAYER_QUEUE_ON_PLAY_END"; // 播放队列播放结束
    public static final String ACTION_PLAYER_QUEUE_LOAD_MORE = "ACTION_PLAYER_QUEUE_LOAD_MORE"; // 播放队列加载更多

    // 资源拉取
    public static final String ACTION_GET_SEARCH_RESULT = "ACTION_GET_SEARCH_RESULT";

    //声控指令
    public static final String ACTION_COMMAND_FAVOUR_SUBSCRIBE = "ACTION_COMMAND_FAVOUR_SUBSCRIBE";
    public static final String ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE = "ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE";

    //歌词
    public static final String ACTION_LYRIC_GET = "ACTION_LYRIC_GET";

    // 休眠/唤醒、倒车
    public static final String ACTION_POWER_SLEEP = "ACTION_POWER_SLEEP";
    public static final String ACTION_POWER_WAKEUP = "ACTION_POWER_WAKEUP";
    public static final String ACTION_POWER_ENTER_REVERSE = "ACTION_POWER_ENTER_REVERSE";
    public static final String ACTION_POWER_EXIT_REVERSE = "ACTION_POWER_EXIT_REVERSE";
}
