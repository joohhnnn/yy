package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.lang.reflect.Method;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.plugin.PluginManager;

public class MonitorUtil {
	private static Class<?> mClsJniHelper = null;
	private static Method mMethodMonitor = null;
	private static int monitor(int type, int val, String[] attrs) {
		if (GlobalContext.isTXZ()) {
			try {
				if (mClsJniHelper == null || mMethodMonitor == null) {
					mClsJniHelper = Class.forName("com.txznet.txz.jni.JNIHelper");
					mMethodMonitor = mClsJniHelper.getMethod("monitor", int.class, int.class, String[].class);
				}
				mMethodMonitor.invoke(mClsJniHelper, type, val, attrs);
			} catch (Exception e) {
			}
		} else {
			try {
				JSONBuilder json = new JSONBuilder();
				json.put("attrs", attrs);
				json.put("type", type);
				json.put("val", val);
				ServiceManager.getInstance().sendInvoke(TXZ, "comm.monitor",
						json.toString().getBytes(), null);
			} catch (Exception e) {
			}
		}

		return 0;
	}

	public static int monitorCumulant(int val, String... attrs) {
		return monitor(ReportManager.VAR_TYPE_CUMULANT, val, attrs);
	}

	public static int monitorCumulant(String... attrs) {
		return monitor(ReportManager.VAR_TYPE_CUMULANT, 1, attrs);
	}
	
	public static void addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("comm.monitor.",monitorProcessor);
	}
	private static MonitorProcessor monitorProcessor = new MonitorProcessor(); 
	private static class MonitorProcessor implements PluginManager.CommandProcessor {

		@Override
		public Object invoke(String command, Object[] args) {
			if (command.equals("cumulant")) {
				MonitorUtil.monitorCumulant((Integer)args[0],(String)args[1]);
			}
			return null;
		}
		
	}
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @定义规则：业务类型.模块.错误等级.接口
	 * @错误等级：F/E/W/I/N
	 */

	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * POI搜索进入量
	 */
	public final static String POISEARCH_ENTER_ALL = "poi.enter.I.all"; // 全部进入量
	public final static String POISEARCH_ENTER_ACTION = "poi.enter.I.action"; // 搜索行为进入量
	public final static String POISEARCH_ENTER_BACKUP = "poi.enter.I.backup"; // 备用搜索进入量
	public final static String POISEARCH_ENTER_CROSS_CITY = "poi.enter.I.crosscity"; // 跨城市搜索进入量
	public final static String POISEARCH_ENTER_CITY = "poi.enter.I.city"; // 城市搜索进入量
	public final static String POISEARCH_ENTER_NEARBY = "poi.enter.I.nearby"; // 周边搜索进入量
	public final static String POISEARCH_ENTER_BUSSINESS_NEARBY = "poi.enter.I.buss_near"; // 周边商圈搜索进入量
	public final static String POISEARCH_ENTER_BUSSINESS_CITY = "poi.enter.I.buss_city"; // 城市商圈搜索进入量
	public final static String POISEARCH_ENTER_CENTER = "poi.enter.I.center"; // 中心搜索进入量
	public final static String POISEARCH_SUCCESS_ALL = "poi.success.I.all"; // 最终成功量
	public final static String POISEARCH_SUGGEST_ALL = "poi.suggest.I.all"; // 搜索建议量，过程量
	public final static String POISEARCH_ERROR_ALL = "poi.error.E.all"; // 最终出错
	public final static String POISEARCH_TIMEOUT_ALL = "poi.timeout.E.all"; // 最终超时
	public final static String POISEARCH_EMPTY_ALL = "poi.empty.W.all"; // 最终为空
	public final static String POISEARCH_RESULT_ONLY = "poi.result.W.only"; // 只有一个结果
	public final static String POISEARCH_RESULT_LE3 = "poi.result.I.l3"; // 结果小于等于3个
	public final static String POISEARCH_RESULT_RADIUS_MORE = "poi.result.I.radius_more"; // 3公里没有结果，3公里外周边搜索却有结果
	/**
	 * 大众点评搜索进入量
	 */
	public final static String POISEARCH_ENTER_DZDP = "poi.enter.I.dzdp";
	public final static String POISEARCH_SUCCESS_DZDP = "poi.success.I.dzdp";
	public final static String POISEARCH_EMPTY_DZDP = "poi.empty.W.dzdp";
	public final static String POISEARCH_ERROR_DZDP = "poi.error.E.dzdp";
	public final static String POISEARCH_TIMEOUT_DZDP = "poi.timeout.E.dzdp";
	/**
	 * 高德搜索进入量
	 */
	public final static String POISEARCH_ENTER_GAODE = "poi.enter.I.gaode";
	public final static String POISEARCH_SUCCESS_GAODE = "poi.success.I.gaode";
	public final static String POISEARCH_EMPTY_GAODE = "poi.empty.W.gaode";
	public final static String POISEARCH_ERROR_GAODE = "poi.error.E.gaode";
	public final static String POISEARCH_TIMEOUT_GAODE = "poi.timeout.E.gaode";
	/**
	 * 高德离线搜索进入量
	 */
	public final static String POISEARCH_ENTER_GAODE_OFFLINE = "poi.enter.I.gaode_offline";
	public final static String POISEARCH_SUCCESS_GAODE_OFFLINE = "poi.success.I.gaode_offline";
	public final static String POISEARCH_EMPTY_GAODE_OFFLINE = "poi.empty.W.gaode_offline";
	public final static String POISEARCH_ERROR_GAODE_OFFLINE = "poi.error.E.gaode_offline";
	public final static String POISEARCH_TIMEOUT_GAODE_OFFLINE = "poi.timeout.E.gaode_offline";
	/**
	 * 百度离线搜索进入量
	 */
	public final static String POISEARCH_ENTER_BAIDU_OFFLINE = "poi.enter.I.baidu_offline";
	public final static String POISEARCH_SUCCESS_BAIDU_OFFLINE = "poi.success.I.baidu_offline";
	public final static String POISEARCH_EMPTY_BAIDU_OFFLINE = "poi.empty.W.baidu_offline";
	public final static String POISEARCH_ERROR_BAIDU_OFFLINE = "poi.error.E.baidu_offline";
	public final static String POISEARCH_TIMEOUT_BAIDU_OFFLINE = "poi.timeout.E.baidu_offline";
	/**
	 * 美行离线搜索进入量
	 */
	public final static String POISEARCH_ENTER_MEIXING_OFFLINE = "poi.enter.I.baidu_offline";
	public final static String POISEARCH_SUCCESS_MEIXING_OFFLINE = "poi.success.I.baidu_offline";
	public final static String POISEARCH_EMPTY_MEIXING_OFFLINE = "poi.empty.W.baidu_offline";
	public final static String POISEARCH_ERROR_MEIXING_OFFLINE = "poi.error.E.baidu_offline";
	public final static String POISEARCH_TIMEOUT_MEIXING_OFFLINE = "poi.timeout.E.baidu_offline";
	/**
	 * 高德WEB搜索进入量
	 */
	public final static String POISEARCH_ENTER_GAODE_WEB = "poi.enter.I.gaode_web";
	public final static String POISEARCH_SUCCESS_GAODE_WEB = "poi.success.I.gaode_web";
	public final static String POISEARCH_EMPTY_GAODE_WEB = "poi.empty.W.gaode_web";
	public final static String POISEARCH_ERROR_GAODE_WEB = "poi.error.E.gaode_web";
	public final static String POISEARCH_TIMEOUT_GAODE_WEB = "poi.timeout.E.gaode_web";
	/**
	 * 百度搜索进入量
	 */
	public final static String POISEARCH_ENTER_BAIDU = "poi.enter.I.baidu";
	public final static String POISEARCH_SUCCESS_BAIDU = "poi.success.I.baidu";
	public final static String POISEARCH_EMPTY_BAIDU = "poi.empty.W.baidu";
	public final static String POISEARCH_ERROR_BAIDU = "poi.error.E.baidu";
	public final static String POISEARCH_TIMEOUT_BAIDU = "poi.timeout.E.baidu";
	/**
	 * 百度Web搜索进入量
	 */
	public final static String POISEARCH_ENTER_BAIDU_WEB = "poi.enter.I.baidu_web";
	public final static String POISEARCH_SUCCESS_BAIDU_WEB = "poi.success.I.baidu_web";
	public final static String POISEARCH_EMPTY_BAIDU_WEB = "poi.empty.W.baidu_web";
	public final static String POISEARCH_ERROR_BAIDU_WEB = "poi.error.E.baidu_web";
	public final static String POISEARCH_TIMEOUT_BAIDU_WEB = "poi.timeout.E.baidu_web";
	/**
	 * TXZ搜索进入量
	 */
	public final static String POISEARCH_ENTER_TXZ= "poi.enter.I.txz";
	public final static String POISEARCH_SUCCESS_TXZ = "poi.success.I.txz";
	public final static String POISEARCH_EMPTY_TXZ= "poi.empty.W.txz";
	public final static String POISEARCH_ERROR_TXZ= "poi.error.E.txz";
	public final static String POISEARCH_TIMEOUT_TXZ= "poi.timeout.E.txz";
	/**
	 * TXZPOI搜索进入量
	 */
	public final static String POISEARCH_ENTER_TXZPOI= "poi.enter.I.txzpoi";
	public final static String POISEARCH_SUCCESS_TXZPOI = "poi.success.I.txzpoi";
	public final static String POISEARCH_EMPTY_TXZPOI= "poi.empty.W.txzpoi";
	public final static String POISEARCH_ERROR_TXZPOI= "poi.error.E.txzpoi";
	public final static String POISEARCH_TIMEOUT_TXZPOI= "poi.timeout.E.txzpoi";
	/**
	 * 奇虎搜索进入量
	 */
	public final static String POISEARCH_ENTER_QIHOO = "poi.enter.I.qihoo";
	public final static String POISEARCH_SUCCESS_QIHOO = "poi.success.I.qihoo";
	public final static String POISEARCH_EMPTY_QIHOO = "poi.empty.W.qihoo";
	public final static String POISEARCH_ERROR_QIHOO = "poi.error.E.qihoo";
	public final static String POISEARCH_TIMEOUT_QIHOO = "poi.timeout.E.qihoo";
	/**
	 * 外部搜索进入量
	 */
	public final static String POISEARCH_ENTER_REMOTE = "poi.enter.I.remote";
	public final static String POISEARCH_SUCCESS_REMOTE = "poi.success.I.remote";
	public final static String POISEARCH_EMPTY_REMOTE = "poi.empty.W.remote";
	public final static String POISEARCH_ERROR_REMOTE = "poi.error.E.remote";
	public final static String POISEARCH_TIMEOUT_REMOTE = "poi.timeout.E.remote";
	
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 升级进入量统计前缀
	 */
	public final static String UPGRADE_ENTER_PREFIX = "upgrd.enter.I."; //升级进入
	public final static String UPGRADE_SILENT_PREFIX = "upgrd.silent.I."; //静默ApkInstaller升级
	public final static String UPGRADE_LOADER_PREFIX = "upgrd.load.I."; //静默热装载升级
	public final static String UPGRADE_DOWN_PREFIX = "upgrd.down.I."; //下载升级
	public final static String UPGRADE_INSTALL_PREFIX = "upgrd.install.I."; //安装升级
	public final static String UPGRADE_HINT_PREFIX = "upgrd.hint.I."; //强制提示框升级
	public final static String UPGRADE_CLOSE_PREFIX = "upgrd.close.I."; //强制提示框升级，确定点击
	public final static String UPGRADE_CONFIRM_PREFIX = "upgrd.confirm.I."; //确认框升级
	public final static String UPGRADE_SURE_PREFIX = "upgrd.sure.I.";//确认框升级，确认点击
	public final static String UPGRADE_CANCEL_PREFIX = "upgrd.cancel.I."; //确认框升级，取消点击
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * UI相关进入量
	 */
	public final static String UI_INIT_START = "ui.init.start.I.all"; // 开始初始化
	public final static String UI_INIT_ERROR = "ui.init.error.E.all"; // 初始化失败
	public final static String UI_INIT_ERROR_CORE = "ui.init.error.I.core"; // Core端初始化失败
	public final static String UI_INIT_ERROR_SDK = "ui.init.error.I.sdk"; // SDK端初始化失败
	public final static String UI_INIT_SUCCESS_ALL = "ui.init.success.I.all"; // 初始化成功
	public final static String UI_INIT_SUCCESS_CORE = "ui.init.success.I.core"; // Core端初始化成功
	public final static String UI_INIT_SUCCESS_SDK = "ui.init.success.I.sdk"; // SDK端初始化成功
	public final static String UI_INIT_SKIN_TXZ = "ui.skin.I.txz"; // 使用TXZ皮肤包
	public final static String UI_INIT_SKIN_SIRI = "ui.skin.I.siri"; // 使用TXZ Siri主题
	public final static String UI_INIT_SKIN_IRONMAN = "ui.skin.I.ironman"; // 钢铁侠主题
	public final static String UI_INIT_SKIN_WAVE = "ui.skin.I.wave"; // 波动主题
	public final static String UI_INIT_SKIN_USER ="ui.skin.I.user"; // 客户自己的主题
 	public final static String UI_INIT_USE_1 = "ui.init.use1.I"; // 使用UI1.0
	public final static String UI_INIT_USE_2 = "ui.init.use2.I"; // 使用UI2.0
	public final static String UI_INIT_APK_DEFAULT = "ui.init.apk.I.default"; // 使用默认路径的主题包进行初始化
	public final static String UI_INIT_APK_USER = "ui.init.apk.I.user"; // 使用user路径下的主题进行初始化
	public final static String UI_INIT_APK_PRIOR = "ui.init.apk.I.prior"; // 使用prior路径下的主题进行初始化
	
	public final static String UI_INIT_ERROR_CORE_FILE_NOT_EXIST = "ui.init.error.E.file"; // 文件不存在
	public final static String UI_INIT_ERROR_CORE_LOAD_DEX = "ui.init.error.E.dex"; // load dex出现问题 
	public final static String UI_INIT_ERROR_CORE_INIT_VIEW = "ui.init.error.E.view"; // init view出现问题 
	
	// /////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 抓拍照片
	 */
	public final static String SDK_START_CAPTURE_PICTURE_ALL = "sdk.capPic.start.I.all"; // 全部抓拍照片行为进入量
	public final static String SDK_START_CAPUTRE_PICTURE_PLUGIN = "sdk.capPic.start.I.plugin"; // plugin抓拍行为
	public final static String SDK_START_CAPTURE_PICTURE_GLOBAL = "sdk.capPic.start.I.global"; // 全局唤醒词抓拍照片
	public final static String SDK_START_CAPTURE_PICTURE_API = "sdk.capPic.start.I.api"; // API抓拍照片
	public final static String SDK_START_CAPTURE_PICTURE_WEBCHAT = "sdk.capPic.start.I.wx"; // 微信公众号抓拍照片
	public final static String SDK_START_CAPTURE_PICTURE_PECCANCY = "sdk.capPic.start.I.pecc"; // 违章抓拍
	public final static String SDK_START_CAPTURE_PICTURE_ACCTDENT = "sdk.capPic.start.I.acc"; // 交通事故
	public final static String SDK_START_CAPTURE_PICTURE_FACILITIES = "sdk.capPic.start.I.fac"; // 交通设施
	public final static String SDK_GOTTON_CAPTURE_PICTURE_ALL = "sdk.capPic.gotton.I"; // 得到抓拍照片
	public final static String SDK_ERROR_CAPTURE_PICTURE_ALL = "sdk.capPic.E.all"; // 全部的抓拍失败
	public final static String SDK_TIMEOUT_CAPTURE_PICTURE = "sdk.capPic.E.timeout"; // 抓拍超时
	public final static String SDK_ONERROR_CAPTURE_PICTURE = "sdk.capPic.E.onerror"; // 适配程序onError
	public final static String SDK_INVALIDPATH_CAPTURE_PICTURE = "sdk.capPic.E.path"; // 返回无效路径
	public final static String SDK_UNKNOWN_CAPTURE_PICTURE = "sdk.capPic.E.unknown"; // 未知错误
//	public final static String SDK_UPLOAD_CAPTURE_PICTURE = "sdk.capPic.upload.E"; //上传超时 
	public final static String SDK_NOTSUPPORT_CAPTURE_PICTURE = "sdk.capPic.E.support"; // 不支持抓拍照片
	public final static String SDK_START_UPLOAD_CAPTURE_PICTURE = "sdk.capPic.start.I.upload"; // 开始上传照片

    // 抓拍视频进入(后台push)
	public static final String VIDEO_CAPTURE_ENTER_PUSH = "video.cap.I.enter_push";
	// 抓拍视频进入(声控发起)
	public static final String VIDEO_CAPTURE_ENTER_VOICE = "video.cap.I.enter_voice";
	// 视频上传被跳过(未绑定设备)
	public static final String VIDEO_CAPTURE_UPLOAD_NOT_BIND = "video.upload.W.not_bind";
	// 前置摄像抓拍视频上传进入
	public static final String VIDEO_CAPTURE_UPLOAD_ENTER_FRONT = "video.upload.I.enter_front";
	// 后置摄像抓拍视频进入
	public static final String VIDEO_CAPTURE_UPLOAD_ENTER_BACK = "video.upload.I.enter_back";
	// 前置摄像抓拍视频超时
	public static final String VIDEO_CAPTURE_TIMEOUT_FRONT = "video.cap.W.timeout_front";
	// 后置摄像抓拍视频超时
	public static final String VIDEO_CAPTURE_TIMEOUT_BACK = "video.cap.W.timeout_back";
	// 前后录抓拍视频都超时(抓拍失败)
	public static final String VIDEO_CAPTURE_TIMEOUT_ALL = "video.cap.E.timeout_all";
	
	/**
	 * 配置文件相关
	 */
	public final static String CFG_FILE_COPY_ENTER = "cfg.upgrade.I.enter"; // 开始拷贝配置文件
	public final static String CFG_FILE_COPY_SUCC = "cfg.upgrade.I.success"; // 拷贝成功
	public final static String CFG_FILE_COPY_ERROR = "cfg.upgrade.I.error"; // 拷贝失败

	////////////////////////////////////////////////////////////////////////////////////////////////
    // 声控播放器相关

	// 搜索
	public final static String MEDIA_SEARCH_ENTER_PREFIX = "media.search.I.enter"; // 进入搜索
	public final static String MEDIA_SEARCH_SUCCESS_PREFIX = "media.search.I.success"; // 搜索成功
	public final static String MEDIA_SEARCH_EMPTY_PREFIX = "media.search.W.empty"; // 搜索结果为空
	public final static String MEDIA_SEARCH_TIMEOUT_PREFIX = "media.search.E.timeout"; // 搜索失败

    ////////////////////////////////////////////////////////////////////////////////////////////////
}
