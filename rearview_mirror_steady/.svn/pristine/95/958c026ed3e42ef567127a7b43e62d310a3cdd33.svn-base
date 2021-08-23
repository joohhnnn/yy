package com.txznet.music;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;
import android.os.Environment;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.fm.bean.Configuration;
import com.txznet.loader.AppLogic;
import com.txznet.music.bean.response.Category;
import com.txznet.music.utils.SharedPreferencesUtils;

/**
 * 静态变量
 * 
 * @author ASUS User
 *
 */
public class Constant {

	public static final boolean ISTESTDATA =Configuration.getInstance().getBoolean(Configuration.TXZ_TEST);//
	public static final boolean ISTEST = Configuration.getInstance().getBoolean(Configuration.TXZ_TEST);// 测试数据（写数据到文件测试）
	public static boolean ISNEED = true;// 循环打印日志，默认关闭

	//计算播放歌曲耗时时间的前缀
	public  static final String SPENDTIME="music:spendTime:";
	
	
	public static final String PLACEHODLER = "%CMD%";

	public static boolean deleteMe = false;//
	public static float currentSound = 0.8f;// 播放器音量
	public static Map<Integer, Category> defaultCategorys = new HashMap<Integer, Category>();

	// 调试数据专用
	public static final int REQTIMEOUT = 35000;// 超时提示

	public static int SoundSessionID = -1;// 声控的会话ID
	public static int ManualSessionID = 0;// 手动点击的回话ID
	public static int RecommandID = 0;// 推荐的ID

	public static enum PlayMode {
		SEQUENCE, SINGLE_CIRCLE, RANDOM
	}

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

	public static String SOURCE = "source";
	public static String categoryID;// 当前播放器请求的分类ID,因为声控的Category有问题。
	private static boolean isExit = true;// 标志是否退出播放器，以使唤醒词无效
	
	public static synchronized void setIsExit(boolean b) {
		if (isExit && !b) {
			if (AppLogic.isMainProcess() ) {
				if ( SharedPreferencesUtils.getFatalExit()) {
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
	
	public static  boolean getIsExit() {
		return isExit;
	}

	public final static String JUNIORID = "juniorID";
	public static final String PAGENAMEEXTRA = "pagenameextra";

	public final static String SONG_STATE = "STATE_SONG";

	// 请求的路径
	public final static String GET_CATEGORY = "/category/get";// 首页获取分类数据
	public final static String GET_SEARCH_LIST = "/album/list";// 从分类进入歌单
	public final static String GET_ALBUM_AUDIO = "/album/audio";// 根据歌单获取歌曲数据
	public final static String GET_SEARCH = "/text/search";// 搜索歌曲
	public final static String GET_TAG = "/conf/check";// 获取版本号
	public final static String GET_REPORT = "/report/report";// 上报数据给服务器
	public final static String GET_PROCESSING = "/text/preprocessing";// 上报数据给服务器
	public final static String GET_STATS = "/report/statistic";// 上报统计数据给服务器
	public final static String GET_FAKE_SEARCH = "/text/fake_request";// 假请求
	public final static String GET_REPORT_ERROR = "/report/abnormal";// 上报错误数据

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
	public static final Typeface typeFace = /* null; */Typeface
			.createFromAsset(GlobalContext.get().getAssets(),
					"fonts/songti.ttf");

	public static final String RS_VOICE_SPEAK_NODATA_TIPS = "已无更多内容";
	public static final String RS_VOICE_SPEAK_NETNOTCON_TIPS = "请求失败，请检查网络连接是否正常";
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
	public static final String RS_VOICE_SPEAKNOTLOCAL_TIPS = "你可以说我要听刘德华的歌来搜索歌曲" /* "本地没有歌曲，您可以说：我想听刘德华的歌" */;
	public static final String RS_VOICE_SPEAK_NET_POOR = "当前网络环境较差";
	public static final String RS_VOICE_SPEAK_NONE_NET = "设备未连接网络";

	public static final String RS_VOICE_SPEAK_TIPS_OPEN = "现在没有正在播放的音频,你可以尝试我要听刘德华的歌";
	public static final String RS_VOICE_SPEAK_TIPS_NO_SONG = "当前没有可以播放的音频";
	public static final String RS_VOICE_SPEAK_TIPS_TIMEOUT = "加载超时，请稍后重试";
	public static final String RS_VOICE_SPEAK_PLAYER_NOAUDIO = "当前播放器没有音频，即将为您退出播放器";
	public static final String RS_VOICE_SPEAK_SUPPORT_NOT_LIVE = "直播不支持上下首切换";
	public static final String RS_VOICE_SPEAK_CLOSE_PLAYER = "即将为您关闭播放器";
	public static final String RS_VOICE_SPEAK_OPEN_PLAYER = "即将为您打开播放器";
	public static final String RS_VOICE_SPEAK_PLAY_MUSIC = "即将为您播放音乐";
	public static final String RS_VOICE_SPEAK_PLAY_AUDIO = "即将为您播放电台";
	public static final String RS_VOICE_SPEAK_PLAY_NEXT = "即将为你播放下一首";
	public static final String RS_VOICE_SPEAK_PLAY_PREV = "即将为你播放上一首";
	public static final String RS_VOICE_SPEAK_PLAY_PAUSE = "即将为您暂停播放";
	public static final String RS_VOICE_SPEAK_PLAY_PLAY = "即将为您继续播放";
	public static final String RS_VOICE_SPEAK_PLAY_ALREADY = "当前正在播放音频";
	public static final String RS_VOICE_SPEAK_FINSISH_SOUND_REDUCE = "已为您降低音量";
	public static final String RS_VOICE_SPEAK_FINSISH_SOUND_UP = "已为您增加音量";
	public static final String RS_VOICE_SPEAK_ROOM_NOT_FREE = "存储空间不足，请尽快清除本地数据";
	public static final String RS_VOICE_SPEAK_WILL_PLAY = "即将播放"+PLACEHODLER;
	public static final String RS_VOICE_SPEAK_PLAY_FINISH = PLACEHODLER+"播放完毕";
	public static final String RS_VOICE_SPEAK_PARSE_ERROR = "数据异常,请稍后重试";
	public static final String RS_VOICE_MUSIC_SPEAK_NOT_LOGIN = "服务器繁忙";

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

	public static final String M_SOUND_FIND = "fm.sound.I.find";// 声控搜索
	public static final String M_SOUND_CANCLE = "fm.sound.I.cancle";// 声控获取不到想要的数据
	public static final String M_TIMEOUT_REQ = "fm.resp.W.";// 声控获取不到想要的数据

}
