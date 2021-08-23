package com.txznet.music.baseModule;

import android.os.Environment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.fm.bean.Configuration;
import com.txznet.music.R;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.ui.SplashActivity;
import com.txznet.music.utils.SharedPreferencesUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 静态变量
 *
 * @author ASUS User
 */
public class Constant {
    public static final long CLICK_TIME_INTERVAL = 500;

    public static final String TAG = "music:constant:: ";

    public static final String PACKAGE_MAIN = "com.txznet.music";
    public static final String PACKAGE_PLAYER = "com.txznet.music:svr0";

    public static final boolean ISTESTDATA = Configuration.getInstance().getBoolean(Configuration.TXZ_TEST);//
    public static final boolean ISTEST =true;// Configuration.getInstance().getBoolean(Configuration.TXZ_TEST);// 测试数据（写数据到文件测试）
    public static final String SPEND_TAG = "music:spend:: ";
    public static final String OOM_TAG = "music:oom: ";
    public static final String PRELOAD_TAG = "music:preload:: ";

    public static boolean ISNEED = false;// 循环打印日志，默认关闭
    public static int TIME_UNIT=1;//时间的单位，用于播放进度条等，因为威仕特设备有一些影响。

    public static final String PLACEHODLER = "%CMD%";

    public static boolean deleteMe = false;//
    public static float currentSound = 0.8f;// 播放器音量
    public static Map<Integer, Category> defaultCategorys = new HashMap<Integer, Category>();

    // 调试数据专用
    public static final int REQTIMEOUT = 35000;// 超时提示

    public static final String NOVELNAME = "novelName";
    public final static String SONG_EXTRAS = "song_extras";

    public final static String ACTION_SHOW_BUTTON = "com.txznet.music.SHOW_BUTTON";
    public static final String ACTION_MUSIC_PAUSE = "com.txznet.music.ACTION_MUSIC_PAUSE";
    public static final String ACTION_MAIN_FINISH = "com.txznet.music.ACTION_MAIN_FINISH";
    public static final String ACTION_MEDIA_FINISH = "com.txznet.music.ACTION_MEDIA_FINISH";
    public static final String REFRESH_HOMEPAGE_DATA = "com.txznet.music.REFRESH_HOMEPAGE_DATA";
    public static final String PARAM_PAUSE_OR_NOT = "pause_or_not";

    // public final static String TYPE_SOUND = "sound";
    // public final static String TYPE_HISTORY = "history";
    // public final static String TYPE_SHOW = "show";
    // public final static String TYPE_LOCAL = "local";
    // public final static String TYPE_BUTTON = "button";

    // public static int TypeSource = 0;

    public final static int LOCAL_MUSIC_TYPE = 0;
    public final static int HISTORY_TYPE = 1;
    public final static int TYPE_SOUND = 2;
    public final static int TYPE_SHOW = 3;
    public final static int TYPE_BUTTON = 4;

    /**
     * 空字符串
     */
    public static final String BLANK_STR = "";

    public static String SOURCE = "source";
    public static String categoryID;// 当前播放器请求的分类ID,因为声控的Category有问题。
    private static boolean isExit = true;// 标志是否退出播放器，以使唤醒词无效

    public static synchronized void setIsExit(boolean b) {
        LogUtil.logd(TAG + "setIsExit:" + b);
        if (b) {
            if (AppLogic.isMainProcess()) {
                SplashActivity.sIsFirstLaunch = true;
                // 退出时主动清除缓存并GC一次
                if (ImageLoader.getInstance().isInited()) {
                    ImageLoader.getInstance().clearDiskCache();
                    ImageLoader.getInstance().clearMemoryCache();
                }
            }
            System.gc();
        }
        if (isExit && !b) {
            if (AppLogic.isMainProcess()) {
                if (SharedPreferencesUtils.getFatalExit()) {
                    MonitorUtil.monitorCumulant(Constant.M_EXIT_EXCEPTION);
                }
                MonitorUtil.monitorCumulant(Constant.M_ENTER_SUCCESS);
                SharedPreferencesUtils.setFatalExit(true);
            }
        } else if (!isExit && b) {
            SharedPreferencesUtils.setFatalExit(false);
            MonitorUtil.monitorCumulant(Constant.M_EXIT_SUCCESS);
        }
        isExit = b;
    }

    public static boolean getIsExit() {
        return isExit;
    }

    public final static String JUNIORID = "juniorID";
    public static final String PAGENAMEEXTRA = "pagenameextra";

    public final static String SONG_STATE = "STATE_SONG";

    // 请求的路径
    public final static String GET_WAY = "txz.music.dataInterface";//
    public final static String GET_CATEGORY = "/category/get";// 首页获取分类数据
    public final static String GET_SEARCH_LIST = "/album/list";// 从分类进入歌单
    public final static String GET_ALBUM_AUDIO = "/album/audio";// 根据歌单获取歌曲数据
    public final static String GET_SEARCH = "/text/search";// 搜索歌曲
    public final static String GET_HISTORY = "/album/history"; // 查询历史
    public final static String GET_TAG = "/conf/check";// 获取版本号
    public final static String GET_REPORT = "/report/report";// 上报数据给服务器
    public final static String GET_PROCESSING = "/text/preprocessing";// 上报数据给服务器
    public final static String GET_STATS = "/report/statistic";// 上报统计数据给服务器
    public final static String GET_FAKE_SEARCH = "/text/fake_request";// 假请求
    public final static String GET_REPORT_ERROR = "/report/abnormal";// 上报错误数据
    public final static String GET_SHORT_PLAY = "/text/pushShortPlay"; //获取快报推送
    public final static String REFRESH_GPS = "/report/refresh_gps"; //刷新GPS数据
    public final static String TEST_PUSH = "/report/test_push"; //测试推送
    public final static String GET_UPON_FAVOUR = "/report/storeOper"; //上传收藏内容
    public final static String GET_FAVOUR = "/text/favour"; //获取收藏内容
    public final static String GET_FAVOUR_LIST = "/album/historyStore"; //获取收藏内容
    public final static String GET_TIME = "/conf/get_time"; //获取服务器时间

    // Intent 传递的关键字
    public final static String INTENT_CATEGORY = "category";// 分类的关键字
    public final static String INTENT_REQUEST = "request";// 分类的关键字
    public static final String Version = "1.0";
    public static final int PAGECOUNT = 10;// 去服务端请求十条数据
    public static final String QQ = "1";
    public static final String KAOLA = "2";
    public static final int QQINT = 1;
    public static final int KAOLAINT = 2;
    public static final int XMLY = 3;

    public static final String SAVE_PATH = Environment
            .getExternalStorageDirectory() + "/txz/audio/";
    public static final String SAVE_FILE = "currentAudio";

    public static final int MODESEQUENCE = 0;
    public static final int MODESINGLECIRCLE = 1;
    public static final int MODERANDOM = 2;

    public static final String RS_VOICE_SPEAK_NODATA_TIPS = "已无更多内容";
    public static final String RS_VOICE_SPEAK_NETNOTCON_TIPS = "请求失败，请检查网络连接是否正常";
    public static final String RS_VOICE_SPEAK_LOCAL_NO_SONGS_TIPS = "请连接网络才能上网搜索该歌曲";
    public static final String RS_VOICE_SPEAK_NOTLOGIN_NETNOTCON_TIPS = "没有登录态，请检查网络连接是否正常";
    // public static final String SPEAK_REQDATAERR_TIPS = "请求失败";
    public static final String RS_VOICE_SPEAK_JSONERR_TIPS = "数据解析异常";
    // public static final String SPEAK_NOCURAUDIO_TIPS = "当前播放器没有歌曲";
    public static final String RS_VOICE_SPEAK_NODATAFOUND_TIPS = "没有找到相关数据";
    public static final String RS_VOICE_SPEAK_NODATAFOUND_WITH_TIPS = "没有找到相关数据"/* "该音频可能已被下架" */;
    public static final String RS_VOICE_SPEAK_SEARCHDATA_TIPS = "正在为你搜索...";
    public static final String RS_VOICE_SPEAK_SEAVERERR_TIPS = "服务器异常,请稍候再试";
    public static final String RS_VOICE_SPEAK_CLIENTERR_TIPS = "播放器异常,请稍候再试";
    public static final String RS_VOICE_SPEAK_NOAUDIOS_TIPS = "当前专辑为空";
    public static final String RS_VOICE_SPEAK_NEXTNOAUDIOS_TIPS = "没有音频了";
    public static final String RS_VOICE_SPEAN_NOAUDIOFOUND_TIPS = "当前歌曲文件不存在";
    public static final String RS_VOICE_SPEAK_CANTSUPPORT_TIPS = "电台节目只支持顺序播放";
    public static final String RS_VOICE_SPEAK_AUDIO_LOADING = "歌单中还没有歌曲";
    public static final String RS_VOICE_SPEAK_CLEANHISTORYDATA_TIPS = "历史数据被清空，请重新选择音频进行播放";
    public static final String RS_VOICE_SPEAKNOTEXIST_TIPS = "该歌曲不存在";
    public static final String RS_VOICE_SPEAK_SUPPORT_NOT_RANDOM_MODE = "不支持随机播放模式";
    public static final String RS_VOICE_SPEAKNOTLOCAL_TIPS = "你可以说我要听刘德华的歌来搜索歌曲" /* "本地没有歌曲，您可以说：我想听刘德华的歌" */;
    public static final String RS_VOICE_SPEAK_NET_POOR = "网络不好，请稍后再试";
    public static final String RS_VOICE_SPEAK_NONE_NET = "设备未连接网络";

    public static final String RS_VOICE_SPEAK_TIPS_OPEN = "现在没有正在播放的音频,你可以尝试我要听刘德华的歌";
    public static final String RS_VOICE_SPEAK_TIPS_NO_SONG = "当前没有可以播放的音频";
    public static final String RS_VOICE_SPEAK_TIPS_TIMEOUT = "加载超时，请稍后重试";
    public static final String RS_VOICE_SPEAK_TIPS_UNKNOWN = "出错了，请稍后重试";
    public static final String RS_VOICE_SPEAK_PLAYER_NOAUDIO = "当前播放器没有音频，即将为您退出播放器";
    public static final String RS_VOICE_SPEAK_SUPPORT_NOT_LIVE = "直播不支持上下首切换";
    public static final String RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION = "该版本不支持该功能";
    public static final String RS_VOICE_SPEAK_SUPPORT_NOT_SEARCH_FUNCTION = "暂不支持搜索歌曲，敬请期待";
    public static final String RS_VOICE_SPEAK_CLOSE_PLAYER = "即将为您关闭播放器";
    public static final String RS_VOICE_SPEAK_OPEN_PLAYER = "即将为您打开" + AppLogic.getApp().getResources().getString(R.string.app_name);
    public static final String RS_VOICE_SPEAK_PLAY_MUSIC = "即将为您播放音乐";
    public static final String RS_VOICE_SPEAK_PLAY_AUDIO = "即将为您播放电台";
    public static final String RS_VOICE_SPEAK_PLAY_NEXT = "即将播放下一首";
    public static final String RS_VOICE_SPEAK_PLAY_PREV = "即将播放上一首";
    public static final String RS_VOICE_SPEAK_PLAY_PAUSE = "即将暂停播放";
    public static final String RS_VOICE_SPEAK_PLAY_PLAY = "即将继续播放";
    public static final String RS_VOICE_SPEAK_PLAY_ALREADY = "当前正在播放音频";
    public static final String RS_VOICE_SPEAK_FINSISH_SOUND_REDUCE = "已为您降低音量";
    public static final String RS_VOICE_SPEAK_FINSISH_SOUND_UP = "已为您增加音量";
    public static final String RS_VOICE_SPEAK_ROOM_NOT_FREE = "存储空间不足，请尽快清除本地数据";
    public static final String RS_VOICE_SPEAK_WILL_PLAY = "即将播放" + PLACEHODLER;
    public static final String RS_VOICE_SPEAK_NOT_FOUND_ACCURATE = "没有找到该数据，为您找到相关数据";
    public static final String RS_VOICE_SPEAK_PLAY_FINISH = PLACEHODLER + "播放完毕";
    public static final String RS_VOICE_SPEAK_PARSE_ERROR = "数据异常,请稍后重试";
    public static final String RS_VOICE_MUSIC_SPEAK_NOT_LOGIN = "服务器繁忙";
    public static final String RS_VOICE_MUSIC_NO_MORE_DATA = "没有更多的数据";
    public static final String RS_VOICE_MUSIC_FAVOUR_TTS = "已收藏";
    public static final String RS_VOICE_MUSIC_FAVOUR_ERROR_TTS = "网络故障，无法收藏";
    public static final String RS_VOICE_MUSIC_UNFAVOUR_TTS = "已取消";
    public static final String RS_VOICE_MUSIC_UNFAVOUR_ERROR_TTS = "网络故障，无法取消收藏";
    public static final String RS_VOICE_MUSIC_FAVOUR_TIPS = "已加入我的收藏";
    public static final String RS_VOICE_MUSIC_UNFAVOUR_TIPS = "已移除我的收藏";
    public static final String RS_VOICE_MUSIC_SUB_TTS = "已订阅";
    public static final String RS_VOICE_MUSIC_UNSUB_ERROR_TIPS = "网络故障，无法取消订阅";
    public static final String RS_VOICE_MUSIC_SUB_ERROR_TIPS = "网络故障，无法订阅";
    public static final String RS_VOICE_MUSIC_UNSUB_TTS = "已取消";
    public static final String RS_VOICE_MUSIC_SUB_TIPS = "已加入我的订阅";
    public static final String RS_VOICE_MUSIC_UNSUB_TIPS = "已移除我的订阅";
    public static final String RS_VOICE_MUSIC_HATE = "好，马上换歌";//即将为您取消收藏并切歌
    public static final String RS_VOICE_MUSIC_PLAY_FAVOUR_SONGS = "即将为您播放收藏的歌曲";
    public static final String RS_VOICE_MUSIC_PLAY_AUDIO = "即将为您播放";
    public static final String RS_VOICE_MUSIC_NO_CONTEXT = "没有上下文";
    public static final String RS_VOICE_MUSIC_BREAKPOINT_TIPS = "即将从上次停止处开始播放";
    public static final String RS_VOICE_MUSIC_WAKEUP_PLAY_NEXT = RS_VOICE_SPEAK_PLAY_NEXT+",如需关闭免唤醒指令请前往同听设置页面";
    public static final String RS_VOICE_MUSIC_WAKEUP_PLAY_PREV = RS_VOICE_SPEAK_PLAY_PREV+"，如需关闭免唤醒指令请前往同听设置页面";
    public static final String RS_VOICE_MUSIC_WAKEUP_PLAY = RS_VOICE_SPEAK_PLAY_PLAY+",如需关闭免唤醒指令请前往同听设置页面";
    public static final String RS_VOICE_MUSIC_WAKEUP_PAUSE = RS_VOICE_SPEAK_PLAY_PAUSE+",如需关闭免唤醒指令请前往同听设置页面";
    public static final String RS_VOICE_MUSIC_CACHE_SIZE = "音乐缓存已达上限";
    public static final String RS_VOICE_MUSIC_DISK_SPACE_INSUFFICIENT = "音乐磁盘空间不足";
    public static final String RS_VOICE_MUSIC_NO_NET_RADIO = "当前没有连接网络，不支持播放电台界面";
    public static final String RS_VOICE_MUSIC_NO_NET_TIPS = "网络异常，请稍后再试";
    public static final String RS_VOICE_MUSIC_NOT_FOUND_CHAPTERS = "没有找到相关章节";
    public static final String RS_VOICE_MUSIC_CLICK_RETRY = "点击重试";


    public static final String M_GETTICKETERROR = "fm.ticket.E.prep";// 请求ticket问题
    public static final String M_URL_PLAY_ERROR = "fm.play.E.url";// 播放路径问题
    public static final String M_URL_PLAY_SUCCESS = "fm.play.I.url";// 正常播放

    public static final String M_EXIT_EXCEPTION = "fm.exit.E.exception";// 异常退出
    public static final String M_EXIT_SUCCESS = "fm.exit.I.success";// 正常退出

    public static final String M_ENTER_SUCCESS = "fm.enter.I.success";// 正常进入

    public static final String M_LOGIN_SUCCESS = "fm.login.I.success";// 开始播放-退出电台
    // 为一次

    public static final String M_EMPTY_CATEGORY = "fm.empty.W.category";// 分类为空
    public static final String M_EMPTY_AUDIO = "fm.empty.W.audio";// 音频为空
    public static final String M_EMPTY_ALBUM = "fm.empty.W.album";// 专辑为空
    public static final String M_EMPTY_SOUND = "fm.empty.W.sound";// 声控获取不到想要的数据
    public static final String M_SOCKET_READ_EMPTY = "fm.empty.w.socket.read";// Socket读取不到数据,读取到的数据为0

    public static final String M_SOUND_FIND = "fm.sound.I.find";// 声控搜索
    public static final String M_SOUND_CANCLE = "fm.sound.I.cancle";// 声控获取不到想要的数据
    public static final String M_SOUND_HISTORY_REQ = "fm.sound.I.history.req";// 声控查看历史
    public static final String M_SOUND_HISTORY_CANCEL = "fm.sound.I.history.cancel";// 声控查看历史
    public static final String M_SOUND_HISTORY_FAILED = "fm.sound.I.history.failed";// 声控查看历失败
    public static final String M_TIMEOUT_REQ = "fm.resp.W.";// 声控获取不到想要的数据
    public static final String M_ENTER_PAGE = "fm.enter.I.";// 声控获取不到想要的数据
    public static final String M_LAUNCH_APP = "fm.launch.I.";// 声控获取不到想要的数据
    public static final String M_SEARCH_MOUDLE = "fm.search.I.";// 声控获取不到想要的数据
    public static final String M_CLICK_MODULE = "fm.click.I.";// 声控获取不到想要的数据

    //快报
    public static final String RS_VOICE_MUSIC_WILL_CLOSE_SHORT_PLAY = "将为您关闭快报";
    public static final String RS_VOICE_MUSIC_WILL_PLAY_SHORT_PLAY = "将为您播放新闻";

}
