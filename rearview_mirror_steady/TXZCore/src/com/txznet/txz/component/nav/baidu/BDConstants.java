package com.txznet.txz.component.nav.baidu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.che.codriver.sdk.oem.NaviControllerListener;
import com.baidu.che.codriver.sdk.oem.NaviControllerManager;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.JBuilder;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.NavPathInfo;
import com.txznet.txz.jni.JNIHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public interface BDConstants {
//	// 与百度深度定制的最低版本
//	public static final int LOWER_VERSION = 4716;
//	// 与百度深度定制的最高版本
//	public static final int HIGHT_VERSION = 4800;
//	// 异步SDK的版本号
//	public static final int USE_NEW_SDK_VERSION = 4717;

	public static final long DEFAULT_DELAY_TIME = 2000;
	public static final long DELAY_TIME_TO_QUERY = 3000;
	public static final int VIEW_TIME_OUT = 10000;
	// 比较耗时操作的超时时间
	public static final long TIME_OUT_DELAY = 5000;

	public static final String DIALOG_ID = "_TXZ_DIALOG_ID_";
	public static final int STYLE_TUIJIAN = 1;
	public static final int STYLE_SHAOZOUGAOSU = 4;
	public static final int STYLE_SHAOSHOUFEI = 8;
	public static final int STYLE_DUOBIYONGDU = 16;
	public static final int STYLE_SHIJIANYOUXIAN = 256;
	public static final int STYLE_GAOSUYOUXIAN = 512;

//	public static final String PACKAGE_NAME = "com.baidu.navi";
	// 百度使用Int类型的坐标传递
	public static final float GCJ_BD_RATE = 100000.0f;
	public static final int JOIN_VERSION_NUM = 64;
	// 获取版本返回的字符串
	public static final String JOIN_VERSION_DESC = "4.7.13";

	public static final String WAY_TYPE_GAS = "Gas_Station";
	public static final String WAY_TYPE_BANK = "Bank";
	public static final String WAY_TYPE_TOILET = "Toilet";
	public static final String WAY_TYPE_SPOTS = "Spots";
	public static final String WAY_TYPE_HOTEL = "Hotel";
	public static final String WAY_TYPE_RESTAURANT = "Restaurant";
	public static final String WAY_TYPE_SERVICE = "Service";
	public static final String WAY_TYPE_PARK = "Park";

	public static final int MAP_CONTROL_SUCCESS = 0;
	// 状态错误，免责声明界面后发出指令的回复
	public static final int MAP_CONTROL_STATE_ERROR = 3;
	// 返回参数错误状态值
	public static final int MAP_CONTROL_PARAMS_ERROR = 5;

	/**
	 * 重试延时
	 */
	public static final int RETRY_NAV_DELAY = 5000;

	public static final String[] sTTSRole = new String[] { "mengmengda", "jinsha" };

	/**
	 * 选择左边按钮
	 */
	public static final int CoDriver_Dialog_FIRST_BTN = -1;
	/**
	 * 选择右边按钮
	 */
	public static final int CoDriver_Dialog_SECOND_BTN = -2;
	/**
	 * 弹框通知
	 */
	public static final String FUN_NAVI_DIALOG_NOTIFY = "fun_navi_dialog_notify";
	/**
	 * 弹框消失
	 */
	public static final String FUN_NAVI_DIALOG_CANCEL = "fun_navi_dialog_cancel";
	/**
	 * 返回选项funcname
	 */
	public static final String FUN_NAVI_DIALOG_RESPONS = "fun_navi_dialog_response";
	/**
	 * 退出导航
	 */
	public static final String FUN_NAVI_APP_CONTROL = "fun_navi_app_control";
	/**
	 * 路线规划成功后推送的路线信息
	 */
	public static final String FUN_NAVI_ROUTE_PLAN = "fun_navi_route_plan";
	/**
	 * 查询当前路线信息
	 */
	public static final String FUN_NAVI_NAVI_STATE = "fun_navi_navi_state";
	/**
	 * 重新规划路线
	 */
	public static final String FUN_NAVI_NAVI_SET = "fun_navi_navi_set";
	/**
	 * Dialog弹出来的funcname
	 */
	public static final String FUN_NAVI_Dialog_Notify = "fun_navi_dialog_notify";
	/**
	 * Dialog被点击取消funcname
	 */
	public static final String FUN_NAVI_Dialog_Cencel = "fun_navi_dialog_cencel";
	/**
	 * 开始导航
	 */
	public static final String FUN_NAVI_START_TASK = "fun_navi_start_task";

	/**
	 * 获取版本特征
	 */
	public static final String FUN_NAVI_GET_VERSION_INFO = "fun_navi_get_version_info";
	/**
	 * 地图控制
	 */
	public static final String FUN_NAVI_MAP_CONTROL = "fun_navi_map_control";
	/**
	 * 开始导航
	 */
	public static final String FUN_NAVI_MAP_STARTNAVI = "fun_navi_map_startnavi";
	/**
	 * 沿途搜索
	 */
	public static final String FUN_NAVI_VIA_SEARCH = "fun_navi_via_search";
	/**
	 * 停车场推荐
	 */
	public static final String FUN_NAVI_PARK_REC = "fun_navi_park_rec";
	/**
	 * 沿途导航：导航过程中增加途径点
	 */
	public static final String FUN_NAVI_ADD_VIA_POINT = "fun_navi_add_via_point";
	/**
	 * 导航状态同步
	 */
	public static final String FUN_NAVI_STATUS_SYNC = "fun_navi_status_sync";
	/**
	 * TTS播报通知
	 */
	public static final String FUN_NAVI_TTS = "fun_navi_tts";
	/**
	 * 用户操作
	 */
	public static final String FUN_NAVI_USER_ACTION = "fun_navi_user_action";
	/**
	 * 离线搜索
	 */
	public static final String FUN_NAVI_OFFLINE_POI = "fun_navi_offline_poi";
	/**
	 * 显示Poi点的路况
	 */
	public static final String FUN_NAVI_QUERY_TRAFFIC = "fun_navi_query_traffic";
	/**
	 * 当前限速
	 */
	public static final String FUN_NAVI_LIMIT_SPEED = "fun_navi_limit_speed";
	/**
	 * 声控电子狗
	 */
	public static final String FUN_NAVI_CRUISE = "fun_navi_cruise";
	/**
	 * 语音切换
	 */
	public static final String FUN_NAVI_TTS_CONTROL = "fun_navi_tts_control";
	/**
	 * 设置家和公司地址
	 */
	public static final String FUN_NAVI_SYN_ADDRESS = "fun_navi_syn_address";
	/**
	 * 打开地图通知
	 */
	public static final String NAVI_APP_LAUNCH = "navi_app_launch";
	/**
	 * 退出地图通知
	 */
	public static final String NAVI_APP_EXIT = "navi_app_exit";
	/**
	 * 导航开始通知
	 */
	public static final String NAVI_START = "navi_start";
	/**
	 * 导航结束通知
	 */
	public static final String NAVI_END = "navi_end";
	/**
	 * 导航前台通知
	 */
	public static final String NAVI_FRONT = "navi_front";
	/**
	 * 导航后台通知
	 */
	public static final String NAVI_BACKGROUND = "navi_background";
	/**
	 * 巡航打开通知
	 */
	public static final String CRUISE_START = "cruise_start";
	/**
	 * 巡航关闭通知
	 */
	public static final String CRUISE_END = "cruise_end";
	/**
	 * 导航开始播报通知
	 */
	public static final String NAVI_TTS_START = "navi_tts_start";
	/**
	 * 导航播报结束通知
	 */
	public static final String NAVI_TTS_END = "navi_tts_end";
	/**
	 * 家的类型
	 */
	public static final String TYPE_HOME_ADDRESS = "type_home_address";
	/**
	 * 公司的类型
	 */
	public static final String TYPE_COMPANY_ADDRESS = "type_company_address";

	/**
	 * 路况查询
	 */
	public static final String FUN_NAVI_TRAFFIC_CONDITION = "fun_navi_traffic_condition";

	/**
	 * 前方怎么走
	 */
	public static final String FUN_NAVI_ASK_FORWARD = "fun_navi_ask_forward";

	/**
	 * 剩余时间和距离
	 */
	public static final String FUN_NAVI_REST_INFO="fun_rest_info";
	/**
	 * 显示模式的切换通知
	 * 手动或自动切换自动、白天、黑夜模式
	 */
	public static final String FUN_THEME_SYNC="fun_theme_sync";

	public static class BDHelper {
		public static final String TAG = "BDHepler";
		public static final boolean ENABLE_LOG = true;

		public static boolean isNewSDKVersion() {
			return BaiduVersion.isSupportProt(false);
		}

		public static boolean isServiceConnect() {
			return NaviControllerManager.getInstance().isServiceConnected();
		}

		public static boolean isServiceBind() {
			boolean isBind = com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().isServiceConnected();
			JNIHelper.logd("NavBaidu ServiceBind:" + isBind);
			return isBind;
		}

		public static void init(Context context, NaviControllerListener listener) {
			JNIHelper.logd("init NaviControllerListener");
			NaviControllerManager.getInstance().init(context, listener);
		}

		public static void initNewSDK(Context context,
				com.baidu.navicontroller.sdk.NaviControllerListener onSDKListener) {
			try {
				JNIHelper.logd("initNewSDK packageName:" + BaiduVersion.refeshPackageName());
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().setLogOpen(true);
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().init(context, BaiduVersion.getCurPackageName(),
						onSDKListener);
			} catch (Exception e) {
				JNIHelper.loge("baidu navi initNewSDK error :" + e.toString());
			}
		}

		public static void startNavApp(Context context) {
			try {
				if (isNewSDKVersion()) {
					com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().startNaviAPP(context);
					return;
				}
				NaviControllerManager.getInstance().startNaviAPP(context);
			} catch (Exception e) {
				JNIHelper.loge("baidu navi startApp error :" + e.toString());
			}
		}

		public static String notifyMapControl(String order, String requestId) {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_MAP_CONTROL, makeJson(order), requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_MAP_CONTROL, makeJson(order));
			return requestId;
		}

		public static String notifyControlDog(boolean isOpen, String requestId) {
			String params = new JBuilder().put("event", isOpen ? "open" : "close").build();
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_CRUISE, params,
					requestId);
		}

		public static String queryLimitSpeed(String requestId) {
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_LIMIT_SPEED,
					null, requestId);
		}

		public static String queryOfflinePois(String requestId, String keywords) {
			String params = new JSONBuilder().put("key", keywords).toString();
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_OFFLINE_POI,
					params, requestId);
		}

		public static String switchTtsRole(String requestId, String role) {
			String params = new JSONBuilder().put("order", "fun_navi_change_voice_speaker")
					.put("data", new JSONBuilder().put("taskId", role).build()).toString();
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_TTS_CONTROL,
					params, requestId);
		}

		public static String notifyQueryTraffic(String city, String addr,String reqId) {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_QUERY_TRAFFIC,
						makeTrafficStr(addr),reqId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_QUERY_TRAFFIC, makeTrafficStr(addr));
			return "";
		}

		public static void updatePointLocation(NavigateInfo info, String type) {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_SET,
						makeNavigateInfoStr(info, type));
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_SYN_ADDRESS, makeNavigateInfoStr(info, type));
		}

		public static String queryByNaviWayPoint(String requestId, String poiType) {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_VIA_SEARCH,
						new JBuilder().put("type", poiType).build(), requestId);
			}
			NaviControllerManager.getInstance().callAsynchronously(requestId, FUN_NAVI_VIA_SEARCH,
					new JBuilder().put("type", poiType).build());
			return "";
		}

		public static NavPathInfo queryNaviRoadInfo() {
			String navPathInfo = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_get_destination_viapoint").build());
			return NavBaiduFactory.getNavInfoFromQueryResult(navPathInfo);
		}

		public static String queryNaviRoadInfoNewSDK(String requestId) {
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_get_destination_viapoint").build(), requestId);
		}

		public static void exitNavStatus() {
			logBdInfo("exitNavStatus");
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_APP_CONTROL,
						new JBuilder().put("order", "end_navi").build());
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_APP_CONTROL,
					new JBuilder().put("order", "end_navi").build());
		}

		public static void exitNavApp() {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_APP_CONTROL,
						new JBuilder().put("order", "end_app").build());
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_APP_CONTROL,
					new JBuilder().put("order", "end_app").build());
		}

		public static String getVersionText(String requestId) {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_GET_VERSION_INFO, "", requestId);
			}
			String json = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_GET_VERSION_INFO, "");
			if (!TextUtils.isEmpty(json)) {
				JSONBuilder jb = new JSONBuilder(json);
				return jb.getVal("version_info", String.class);
			}
			return "";
		}

		public static String getRequestId() {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().getRequestId();
			}
			return NaviControllerManager.getInstance().getRequestId();
		}

		public static int getVersionNum() {
			String text = getVersionText(getRequestId());
			if (!TextUtils.isEmpty(text)) {
				int ppos = text.indexOf("_");
				int apos = text.lastIndexOf("_");
				if (ppos != apos) {
					try {
						String code = text.substring(ppos + 1, apos);
						return Integer.parseInt(code);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			return -1;
		}

		public static String getVersionDesc() {
			String text = getVersionText(getRequestId());
			if (!TextUtils.isEmpty(text)) {
				return text.substring(text.lastIndexOf("_") + 1);
			}
			return "";
		}

		public static boolean notifyInsertPoint(Poi poi, String requestId) {
			try {
				JSONObject jo = new JSONObject();
				if (poi.getLat() / GCJ_BD_RATE >= 1){
					jo.put("lat", poi.getLat());
				}else {
					jo.put("lat",  convertInteger(poi.getLat()));
				}
				if (poi.getLng() / GCJ_BD_RATE >= 1){
					jo.put("lng", poi.getLng());
				}else {
					jo.put("lng",  convertInteger(poi.getLng()));
				}
				if (isNewSDKVersion()) {
					com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_ADD_VIA_POINT,
							jo.toString(), requestId);
					return true;
				}
				NaviControllerManager.getInstance().notify(FUN_NAVI_ADD_VIA_POINT, jo.toString());
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return false;
		}

		public static boolean isInNavi() {
			String strData = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_is_innavi").build());
			if (!TextUtils.isEmpty(strData)) {
				JSONBuilder jb = new JSONBuilder(strData);
				String val = jb.getVal("is_innavi", String.class);
				if ("true".equals(val)) {
					return true;
				}
			}
			return false;
		}

		public static String requestInNavi(String requestId) {
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_is_innavi").build(), requestId);
		}

		public static boolean isForground() {
			String params = new JBuilder().put("order", "type_foreground").build();
			String strData = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_NAVI_STATE, params);
			if (!TextUtils.isEmpty(strData)) {
				JSONBuilder jb = new JSONBuilder(strData);
				String val = jb.getVal("foreground", String.class);
				if ("true".equals(val)) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 查询是否处于前台可见
		 * 
		 * @param requestId
		 * @return
		 */
		public static String requestForground(String requestId) {
			String params = new JBuilder().put("order", "type_foreground").build();
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_STATE,
					params, requestId);
		}

		/**
		 * 查询是否处于巡航模式
		 * 
		 * @param requestId
		 * @return
		 */
		public static String requestIsDog(String requestId) {
			String params = new JBuilder().put("order", "type_is_incruise").build();
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_STATE,
					params, requestId);
		}

		public static String startNavigate(String name, double lat, double lng, int style, String requestId) {
			int slat = convertInteger(lat);
			int slng = convertInteger(lng);
			String params = "";
			if (style != -1) {
				params = new JBuilder().put("dest", new JBuilder().put("lat", slat).put("lng", slng).buildObj())
						.put("dest_name", name).put("preference", style).build();
			} else {
				params = new JBuilder().put("dest", new JBuilder().put("lat", slat).put("lng", slng).buildObj())
						.put("dest_name", name).build();
			}
			logBdInfo("startNavigate:" + params);
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_START_TASK,
						params, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_START_TASK, params);
			return "";
		}

		public static String startNavigate(Poi startPoi, Poi endPoi,
				List<TXZNavManager.PathInfo.WayInfo> wayInfos, int style, String requestId) {
			int slat = convertInteger(endPoi.getLat());
			int slng = convertInteger(endPoi.getLng());
			JBuilder params = null;
			if (style != -1) {
				params = new JBuilder()
						.put("dest", new JBuilder().put("lat", slat).put("lng", slng).buildObj())
						.put("dest_name", endPoi.getName()).put("preference", style);
			} else {
				params = new JBuilder()
						.put("dest", new JBuilder().put("lat", slat).put("lng", slng).buildObj())
						.put("dest_name", endPoi.getName());
			}
			if (wayInfos != null && !wayInfos.isEmpty()) {
				JSONArray jsonArray = new JSONArray();
				for (TXZNavManager.PathInfo.WayInfo wayInfo : wayInfos) {
					jsonArray.put(new JBuilder().put("lat", convertInteger(wayInfo.lat))
							.put("lng", convertInteger(wayInfo.lng)).buildObj());
				}
				params.put("pass_point", jsonArray);
			}

			logBdInfo("startNavigate:" + params.build());
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_START_TASK,
								params.build(), requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_START_TASK, params.build());
			return "";
		}

		public static int queryCurrentStyle() {
			String strData = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_navi_preference").build());
			if (!TextUtils.isEmpty(strData)) {
				JSONBuilder jb = new JSONBuilder(strData);
				String type = jb.getVal("navi_preference", String.class);
				return Integer.parseInt(type);
			}
			return -1;
		}

		public static String queryCurrentStyleNewSDK(String requestId) {
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_STATE,
					new JBuilder().put("order", "type_navi_preference").build(), requestId);
		}

		// TODO 确认单词
		public static String rePlanWithStyle(int style, String requestId) {
			String params = new JBuilder().put("order", "type_reset_navi_bypreference")
					.put("data", new JBuilder().put("navi_preference", "" + style).buildObj()).build();
			logBdInfo(params);
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_SET,
						params, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_NAVI_SET, params);

			// 设置默认的策略
			setDefaultStyle(style);
			return requestId;
		}

		public static void setDefaultStyle(int style) {
			String params = new JBuilder().put("order", "type_set_navi_preference")
					.put("data", new JBuilder().put("navi_preference", "" + style).buildObj()).build();
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_NAVI_SET, params);
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_NAVI_SET, params);
		}

		public static String switchTTSClass(String modeData, String requestId) {
			int mode = 233;
			if ("MEADWAR".equals(modeData)) {
				mode = 233;
			} else if ("EXPERT".equals(modeData)) {
				mode = 234;
			} else if ("MUTE".equals(modeData)) {
				mode = 206;
			}

			String params = new JBuilder().put("order", mode).build();
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_MAP_CONTROL, params, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_MAP_CONTROL, params);
			return requestId;
		}

		public static Poi queryHomeAddress() {
			String homeJson = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_SYN_ADDRESS,
					new JBuilder().put("order", TYPE_HOME_ADDRESS).build());
			return convertPoi(homeJson);
		}

		public static String queryAddress(boolean isHome, String requestId) {
			return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_SYN_ADDRESS,
					new JBuilder().put("order", isHome ? TYPE_HOME_ADDRESS : TYPE_COMPANY_ADDRESS).build(), requestId);
		}

		public static Poi queryCompanyAddress() {
			String companyJson = NaviControllerManager.getInstance().callSynchronously(FUN_NAVI_SYN_ADDRESS,
					new JBuilder().put("order", TYPE_COMPANY_ADDRESS).build());
			return convertPoi(companyJson);
		}

		public static void selectRoute(int index) {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_ROUTE_PLAN,
						new JBuilder().put("select", index).build());
				return;
			}

			NaviControllerManager.getInstance().notify(FUN_NAVI_ROUTE_PLAN,
					new JBuilder().put("select", index).build());
		}

		public static void selectPlanCancel() {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_ROUTE_PLAN,
						new JBuilder().put("event", "close").build());
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_ROUTE_PLAN,
					new JBuilder().put("event", "close").build());
		}

		public static void selectStartNavi() {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_ROUTE_PLAN,
						new JBuilder().put("event", "start").build());
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_ROUTE_PLAN,
					new JBuilder().put("event", "start").build());
		}

		public static void doWakeupControlDialog(String requestId, String dialogId, int order) {
			LogUtil.logd("requestId:" + requestId + ",dialogId:" + dialogId + ",order:" + order);
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendNotification(
						FUN_NAVI_DIALOG_RESPONS,
						new JBuilder().put("dialogid", dialogId).put("order", String.valueOf(order)).build());
				return;
			}
			NaviControllerManager.getInstance().callAsynchronously(requestId, FUN_NAVI_DIALOG_RESPONS,
					new JBuilder().put("order", String.valueOf(order)).build());
		}

		public static void parkHere() {
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().sendRequest(FUN_NAVI_PARK_REC, "");
				return;
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_PARK_REC, "");
		}

		public static String showDialog(String type, String requestId, String title, String msg, String firstBtnStr,
				String secondBtnStr) {
			String params = new JBuilder()
					.put("dialogid", DIALOG_ID).put("type", type).put("value", new JBuilder().put("title", title)
							.put("content", msg).put("firstbtn", firstBtnStr).put("secondbtn", secondBtnStr).buildObj())
					.build();
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendNotification(FUN_NAVI_DIALOG_NOTIFY, params);
				return DIALOG_ID;
			}
			NaviControllerManager.getInstance().callAsynchronously(requestId, FUN_NAVI_DIALOG_NOTIFY, params);
			return "SHOW_DIALOG_ID";
		}

		public static void dismissDialog(String requestId) {
			String params = new JBuilder().put("dialogid", DIALOG_ID).build();
			if (isNewSDKVersion()) {
				com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendNotification(FUN_NAVI_DIALOG_CANCEL, params);
				return;
			}
			NaviControllerManager.getInstance().callAsynchronously(requestId, FUN_NAVI_DIALOG_CANCEL, params);
		}

		public static Poi convertPoi(String json) {
			if (TextUtils.isEmpty(json)) {
				return null;
			}

			try {
				JSONBuilder jb = new JSONBuilder(json);
				JSONObject jo = jb.getVal("data", JSONObject.class);
				Poi poi = new Poi();
				poi.setName(jo.optString("name"));
				poi.setGeoinfo(jo.optString("address"));
				poi.setLat(convertDouble(jo.optInt("lat")));
				poi.setLng(convertDouble(jo.optInt("lng")));
				return poi;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		public static String getStrDataFromKey(String srcStr, String key) {
			JSONBuilder jb = new JSONBuilder(srcStr);
			return jb.getVal(key, JSONObject.class).toString();
		}

		public static String makeJson(String value) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("order", value);
				return obj.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static String makeJson(String value, String strData) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("order", value);
				obj.put("strData", strData);
				return obj.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static String makeTrafficStr(String addr) {
			try {
				JSONObject obj = new JSONObject();
				obj.put("poi", addr);
				return obj.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static String makeNavigateInfoStr(NavigateInfo info, String type) {
			GpsInfo gpsInfo = info.msgGpsInfo;
			if (gpsInfo == null) {
				return "";
			}

			try {
				JSONObject jObj = new JSONObject();
				jObj.put("order", type.equals("home") ? TYPE_HOME_ADDRESS : TYPE_COMPANY_ADDRESS);

				JSONObject obj = new JSONObject();
				obj.put("name", info.strTargetName);
				obj.put("address", info.strTargetAddress);
				obj.put("lat", convertInteger(gpsInfo.dblLat));
				obj.put("lng", convertInteger(gpsInfo.dblLng));
				obj.put("type", type);

				jObj.put("data", obj);
				return jObj.toString();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return "";
		}

		public static double convertDouble(int gpsInteger) {
			return gpsInteger / GCJ_BD_RATE;
		}

		public static int convertInteger(double gpsDouble) {
			return (int) (gpsDouble * GCJ_BD_RATE);
		}

		public static void logBdInfo(String msg) {
			if (BDHelper.ENABLE_LOG) {
				JNIHelper.logd(BDHelper.TAG + ":" + msg);
			}
		}

		public static String checkMapCondition(String requestId) {
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_TRAFFIC_CONDITION, null, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_TRAFFIC_CONDITION, null);
			return requestId;
		}

		public static String checkRestInfo(String requestId){
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_REST_INFO, null, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_REST_INFO, null);
			return requestId;
		}

		public static String checkHowNavi(String requestId){
			if (isNewSDKVersion()) {
				return com.baidu.navicontroller.sdk.NaviControllerManager.getInstance()
						.sendRequest(FUN_NAVI_ASK_FORWARD, null, requestId);
			}
			NaviControllerManager.getInstance().notify(FUN_NAVI_ASK_FORWARD, null);
			return requestId;
		}
	}
}
