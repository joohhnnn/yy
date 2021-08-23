package com.txznet.sdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.bean.NavVoicePlugin;
import com.txznet.sdk.bean.NaviInfo;
import com.txznet.sdk.bean.Poi;

import android.text.TextUtils;

/**
 * 导航管理器
 *
 */
public class TXZNavManager {
	private static TXZNavManager sInstance = new TXZNavManager();

	private TXZNavManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZNavManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetTool) {
			if (mNavTool == null) {
				setNavTool((NavToolType) null);
			} else if (mNavTool instanceof NavTool) {
				setNavTool((NavTool) mNavTool);
			} else if (mNavTool instanceof NavToolType) {
				setNavTool((NavToolType) mNavTool);
			}
		}
		if (mDefaultNav != null) {
			setNavDefaultTool(mDefaultNav);
		}

		if (mSetEnable) {
			enableAutoAMapCmd(mEnableCmd, mTraffic, mIsNavi3D, mIsNorth);
		}
		
		if (mEnableWakeupNav != null) {
			enableWakeupNavCmds(mEnableWakeupNav);
		}

		if (mWakeupExitNavAmapAuto != null) {
			enableWakeupExitNav(mWakeupExitNavAmapAuto);
		}
		
		if(mExitItcWhenBack != null){
			exitInteractiveWhenBackPoi(mExitItcWhenBack);
		}
		
		if (mForceRegister != null){
			forceRegsiterMapOrder(mForceRegister);
		}
		
		if (mRemotePkn != null && !TextUtils.isEmpty(mRemotePkn)) {
			setNavCldPackageName(mRemotePkn);
		}

		if (mPlan != null) {
			enableSavePlanAfterPlan(mPlan);
		}
		
		if (mHasStatusListener) {
			sendSetNavStatusCmd();
		}
		
		if (mUseActiveNav != null) {
			setUseActiveNav(mUseActiveNav);
		}
		
		if (mAutoNaviDelay != null) {
			setPlanAutoNaviDelay(mAutoNaviDelay);
		}
		
		if (mRemoveNavDialog != null) {
			setRemoveNavConfirmDialog(mRemoveNavDialog);
		}
		
		if (mBanNavTool != null) {
			banNavAbility(mBanNavTool);
		}
		
		if (mAlwayAskNav != null) {
			setAlwayAskNav(mAlwayAskNav);
		}
		
		if (mEnableMulti != null) {
			enableMultiNavigation(mEnableMulti);
		}
		
		if (mEnableNavCmd != null) {
			enableNavCmd(mEnableNavCmd);
		}

		if (isCloseWhenSetHcAddr != null) {
			setIsCloseWhenSetHcAddr(isCloseWhenSetHcAddr);
		}

		if (mTmcTool != null) {
			setTmcTool(mTmcTool);
		}

		if(mClickOutSizeCancelDialog != null){
			setClickOutSizeCancelDialog(mClickOutSizeCancelDialog);
		}
		
		setVoicePlugin();
	}
	
	/**
	 * 导航工具状态监听器，第三方自定义导航工具要通知各类导航状态，防止功能丢失
	 */
	public static interface NavToolStatusListener {
		/**
		 * 开始导航，导航开始后才能使用插入途经点的功能
		 */
		public void onStart();

		/**
		 * 结束导航
		 */
		public void onEnd();
		
		/**
		 * 导航处于可见状态
		 */
		public void onGetFocus();
		
		/**
		 * 导航失焦点
		 */
		public void onLoseFocus();

		/**
		 * 退出了导航应用，用来区分引导用户打开导航的提醒
		 */
		public void onExitApp();
		
		/**
		 * 退出导航的时候是否退出所有被管理的导航
		 * @param enable
		 */
		public void enableExitAllNavTool(boolean enable);
		
		/**
		 * 开始导航后通知路径信息（目的地等信息）
		 */
		public void onPathUpdate(PathInfo pathInfo);

		/**
		 * 通知当前存在的途经点
		 */
		public void onJingYouUpdate(List<Poi> tjs);
	}
	
	/**
	 * 增加可配置功能接口开关
	 */
	public static abstract class NavToolStatusHighListener implements NavToolStatusListener {
		/**
		 * 途经点掩码
		 * 设置是否支持途经点功能处理
		 * 如果支持的话，语音识别到途经点的地址将通过
		 * navToLoc方法Poi参数中的getAction=PoiAction.ACTION_JINGYOU类型Poi给出
		 */
		public static final int MARK_WAYPOI = 1 << 0;
		
		/**
		 * 当前限速掩码，默认当前限速取得是内置导航的引导数据，
		 * 如果自定义了导航工具并且需要支持当前限速播报，请开启本功能
		 * 识别到后会回调navtool的speakLimitSpeed方法
		 */
        public static final int MARK_LIMITSPEED = 1 << 1;

        /**
         * 发起导航时携带途经点
		 * 设置是否支持发起导航时携带途经点
		 * navigateWithWayPois 返回pathinfo
         */
        public static final int MARK_NAV_PASS_WAYPOIS = 1 << 2;

		private volatile int mFlag;

		/**
		 * 开启掩码对应的功能
		 * 
		 * @param flag
		 */
		public void addFlag(int flag) {
			mFlag |= flag & MARK_WAYPOI;
			mFlag |= flag & MARK_LIMITSPEED;
			mFlag |= flag & MARK_NAV_PASS_WAYPOIS;
			onFlagChange();
		}

		/**
		 * 清空掩码支持的功能
		 * @param flag
		 */
		public void clearFlag(int flag) {
			mFlag &= ~(flag & MARK_WAYPOI | flag & MARK_LIMITSPEED | flag & MARK_NAV_PASS_WAYPOIS);
			onFlagChange();
		}

		private void onFlagChange() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.setRemoteFlag",
					(mFlag + "").getBytes(), null);
		}
	}
	
	/**
	 * 导航路径信息类
	 */
	public static class PathInfo {
		/**
		 * 途经点
		 */
		public static class WayInfo {
			public double lat;
			public double lng;
			public String name;
			public String addr;

			public String toString() {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("lat", lat);
					jsonObject.put("lng", lng);
					jsonObject.put("name", name);
					jsonObject.put("addr", addr);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return jsonObject.toString();
			}
			
			public static WayInfo fromJson(String json) {
				WayInfo pathInfo = new WayInfo();
				try {
					JSONObject jsonObject = new JSONObject(json);
					pathInfo.lat = jsonObject.optDouble("lat");
					pathInfo.lng = jsonObject.optDouble("lng");
					pathInfo.name = jsonObject.optString("name");
					pathInfo.addr = jsonObject.optString("addr");
				} catch (JSONException e) {
					e.printStackTrace();
					pathInfo = null;
				}
				return pathInfo;
			}
		}
		// 代表记录的ID
		public int _id;
		// 起始LAT
		public double fromPoiLat;
		// 起始LNG
		public double fromPoiLng;
		// 目的LAT
		public double toPoiLat;
		// 目的LNG
		public double toPoiLng;
		// 起点地址
		public String fromPoiAddr;
		// 起点名称
		public String fromPoiName;
		// 目的地地址
		public String toPoiAddr;
		// 目的地名称
		public String toPoiName;
		// 目的地城市
		public String toCity;
		// 距离目的地总距离
		public Integer totalDistance = 0;
		// 距离目的地总时间
		public Integer totalTime = 0;
		// 途经点数据
		String wayInfoStr;
		// 途经点
		public List<WayInfo> wayInfos;
		
		public void clear() {
			fromPoiAddr = null;
			fromPoiLat = 0;
			fromPoiLng = 0;
			fromPoiName = null;
			toCity = null;
			toPoiAddr = null;
			toPoiLat = 0;
			toPoiLng = 0;
			toPoiName = null;
			totalDistance = 0;
			totalTime = 0;
			wayInfoStr = null;
			wayInfos = null;
		}
		
		public String toString() {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("fromPoiLat", fromPoiLat);
				jsonObject.put("fromPoiLng", fromPoiLng);
				jsonObject.put("toPoiLat", toPoiLat);
				jsonObject.put("toPoiLng", toPoiLng);
				jsonObject.put("fromPoiAddr", fromPoiAddr);
				jsonObject.put("fromPoiName", fromPoiName);
				jsonObject.put("toPoiAddr", toPoiAddr);
				jsonObject.put("toPoiName", toPoiName);
				jsonObject.put("toCity", toCity);
				jsonObject.put("totalDistance", totalDistance);
				jsonObject.put("totalTime", totalTime);
				if (wayInfos != null) {
					JSONArray jsonArray = new JSONArray();
					for (WayInfo info : wayInfos) {
						jsonArray.put(info.toString());
					}
					wayInfoStr = jsonArray.toString();
					jsonObject.put("wayInfoStr", wayInfoStr);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return jsonObject.toString();
		}
		
		public static PathInfo fromString(String json) {
			PathInfo pathInfo = new PathInfo();
			try {
				JSONObject jsonObject = new JSONObject(json);
				pathInfo.fromPoiLat = jsonObject.optDouble("fromPoiLat");
				pathInfo.fromPoiLng = jsonObject.optDouble("fromPoiLng");
				pathInfo.toPoiLat = jsonObject.optDouble("toPoiLat");
				pathInfo.toPoiLng = jsonObject.optDouble("toPoiLng");
				pathInfo.fromPoiAddr = jsonObject.optString("fromPoiAddr");
				pathInfo.fromPoiName = jsonObject.optString("fromPoiName");
				pathInfo.toPoiAddr = jsonObject.optString("toPoiAddr");
				pathInfo.toPoiName = jsonObject.optString("toPoiName");
				pathInfo.toCity = jsonObject.optString("toCity");
				pathInfo.totalDistance = jsonObject.optInt("totalDistance");
				pathInfo.totalTime = jsonObject.optInt("totalTime");
				pathInfo.wayInfoStr = jsonObject.optString("wayInfoStr");
				pathInfo.wayInfos = new ArrayList<WayInfo>();
				if (!TextUtils.isEmpty(pathInfo.wayInfoStr)) {
					JSONArray jsonArray = new JSONArray(pathInfo.wayInfoStr);
					for (int i = 0; i < jsonArray.length(); i++) {
						pathInfo.wayInfos.add(WayInfo.fromJson(jsonArray.getString(i)));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				pathInfo = null;
			}
			return pathInfo;
		}
	}
	
	public static interface NavStatusListener {
		
		/**
		 * 进入导航
		 * @param packageName 导航包名
		 */
		public void onEnter(String packageName);
		
		/**
		 * 退出导航
		 * @param packageName 导航包名
		 */
		public void onExit(String packageName);

		/**
		 * 通过目的地发起导航
		 * @param packageName 导航包名
		 * @param poi
		 */
		public void onBeginNav(String packageName, Poi poi);

		/**
		 * 路径规划成功回调
		 * 只有一些导航工具是有规划成功的通知的，所以这个方法有可能会不回调
		 */
		public void onPlanSucc(String packageName);

		/**
		 * 路径规划失败
		 * 只有一些导航工具是有规划失败的通知的，所以这个方法有可能会不回调
		 * 截至2019/01/18，没有一个导航工具有调用这个方法。
		 *
		 * @param errorCode 错误码
		 * @param reason    错误原因
		 */
		public void onPlanFail(String packageName, int errorCode, String reason);

		/**
		 * 开始导航
		 * @param navPkg 导航包名
		 * @param navPkg
		 * 			导航的包名
		 */
		public void onStart(String navPkg);
		
		/**
		 * 导航结束
		 * @param navPkg 导航包名
		 * @param navPkg
		 * 			导航的包名
		 */
		public void onEnd(String navPkg);
		
		/**
		 * 前后台变化
		 * @param packageName 导航包名
		 * @param isForeground
		 * 			true:前台
		 * 			false:后台
		 */
		public void onForeground(String packageName, boolean isForeground);
		
		/**
		 * 状态通知
		 */
		public void onStatusUpdate(String pkn);

		/**
		 * 语音通知设置了默认导航
		 * 
		 * @param pkn
		 */
		public void onDefaultNavHasSeted(String pkn);

		/**
		 * 百度语音播报开始或关闭
		 */
		public void onTtsStartOrEnd(String pkn,boolean isTts);
	}

	/**
	 * 导航工具
	 * 
	 * @author txz
	 *
	 */
	public static interface NavTool {
		/**
		 * 导航到指定点，Poi中的Action代表Poi类型，默认值为nav，代表导航到该点
		 * 
		 * @param point
		 *            目标点
		 */
		public void navToLoc(Poi point);
		
		/**
		 * 播报当前限速信息
		 */
		public void speakLimitSpeed();

		/**
		 * 回家，废弃，不必外部实现
		 */
		@Deprecated
		public void navHome();
		
		/**
		 * 语音设置家的地址
		 * @param hPoi
		 */
		public void setHomeLoc(Poi hPoi);

		/**
		 * 去公司，废弃，不必外部实现
		 */
		@Deprecated
		public void navCompany();
		
		/**
		 * 语音设置公司的地址
		 * @param cPoi
		 */
		public void setCompanyLoc(Poi cPoi);

		/**
		 * 是否正在导航中（使用途经点功能的时候要正确返回当前是否处于导航中）
		 */
		public boolean isInNav();

		/**
		 * 进入导航
		 */
		public void enterNav();

		/**
		 * 退出导航
		 */
		public void exitNav();

		/**
		 * 在自动模式下
		 * 通过大灯等使用场景设置黑夜模式@
		 */
		public void setNightMode();

		/**
		 * 取消黑夜模式
		 */
		public void cancelNightMode();

        /**
         * 发起导航携带途经点
         */
        void navigateWithWayPois(PathInfo pathInfo);

		/**
		 * 设置状态监听器
		 */
		@Deprecated
		public void setStatusListener(NavToolStatusListener listener);
		
		/**
		 * 设置状态监听器，监听器可通知语音开启部分功能，详见NavToolStatusHighListener中的掩码
		 * @param listener
		 */
		public void setStatusListener(NavToolStatusHighListener listener);
	}

	private boolean mHasSetTool = false;
	private Object mNavTool = null;

	/**
	 * 设置导航工具，默认使用同行者工具
	 * 
	 * @param tool
	 *            使用的工具对象
	 */
	public void setNavTool(NavTool tool) {
		mHasSetTool = true;
		mNavTool = tool;

		if (null == tool) {
			setNavTool((NavToolType) null);
			return;
		}

		tool.setStatusListener(new NavToolStatusListener() {
			@Override
			public void onStart() {
				ServiceManager.getInstance()
						.sendInvoke(ServiceManager.TXZ,
								"txz.nav.notifyNavStatus",
								("" + true).getBytes(), null);
			}

			@Override
			public void onEnd() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.nav.notifyNavStatus", ("" + false).getBytes(),
						null);
			}
			
			@Override
			public void enableExitAllNavTool(boolean enable) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyExitAllNav",
						(enable + "").getBytes(), null);
			}

			@Override
			public void onExitApp() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyExitApp", "true".getBytes(), null);
			}

			@Override
			public void onGetFocus() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyIsFocus", "true".getBytes(), null);
			}
			
			@Override
			public void onLoseFocus() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyIsFocus", "false".getBytes(), null);
			}

			@Override
			public void onPathUpdate(PathInfo pathInfo) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyPathInfo",
						(pathInfo != null ? pathInfo.toString().getBytes() : null), null);
			}

			@Override
			public void onJingYouUpdate(List<Poi> tjs) {
				JSONArray jsonBuilder = new JSONArray();
				if (tjs != null && !tjs.isEmpty()) {
					for (Poi poi : tjs) {
						jsonBuilder.put(poi.toString());
					}
				}
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyJingYous", jsonBuilder.toString().getBytes(), null);
			}
		});
		tool.setStatusListener(new NavToolStatusHighListener() {
			
			@Override
			public void onStart() {
				ServiceManager.getInstance()
				.sendInvoke(ServiceManager.TXZ,
						"txz.nav.notifyNavStatus",
						("" + true).getBytes(), null);
			}
			
			@Override
			public void onEnd() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.nav.notifyNavStatus", ("" + false).getBytes(),
						null);
			}
			
			@Override
			public void enableExitAllNavTool(boolean enable) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyExitAllNav",
						(enable + "").getBytes(), null);
			}

			@Override
			public void onExitApp() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyExitApp", "true".getBytes(), null);
			}

			@Override
			public void onGetFocus() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyIsFocus", "true".getBytes(), null);
			}
			
			@Override
			public void onLoseFocus() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyIsFocus", "false".getBytes(), null);
			}

			@Override
			public void onPathUpdate(PathInfo pathInfo) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyPathInfo",
						(pathInfo != null ? pathInfo.toString().getBytes() : null), null);
			}

			@Override
			public void onJingYouUpdate(List<Poi> tjs) {
				JSONArray jsonBuilder = new JSONArray();
				if (tjs != null && !tjs.isEmpty()) {
					for (Poi poi : tjs) {
						jsonBuilder.put(poi.toString());
					}
				}
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.notifyJingYous", jsonBuilder.toString().getBytes(), null);
			}
		});

		TXZService.setCommandProcessor("tool.nav.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (mNavTool == null || !(mNavTool instanceof NavTool)) {
					if (command.equals("isInNav"))
						return ("" + false).getBytes();
					return null;
				}
				NavTool tool = (NavTool) mNavTool;
				if (command.equals("isInNav")) {
					return ("" + tool.isInNav()).getBytes();
				}
				if (command.equals("navHome")) {
					tool.navHome();
					return null;
				}
				if (command.equals("navCompany")) {
					tool.navCompany();
					return null;
				}
				if (command.equals("navTo")) {
					tool.navToLoc(Poi.fromString(new String(data)));
					return null;
				}
				if(command.equals("speakLimitSpeed")){
					tool.speakLimitSpeed();
					return null;
				}
				if (command.equals("setHomeLoc")){
					tool.setHomeLoc(Poi.fromString(new String(data)));
					return null;
				}
				if (command.equals("setCompanyLoc")){
					tool.setCompanyLoc(Poi.fromString(new String(data)));
					return null;
				}
				if (command.equals("exitNav")) {
					tool.exitNav();
					return null;
				}
				if (command.equals("enterNav")) {
					tool.enterNav();
					return null;
				}
				if(command.equals("navigateWithWayPois")){
					tool.navigateWithWayPois(PathInfo.fromString(new String(data)));
					return null;
                }
				if(command.equals("setNightMode")){
					tool.setNightMode();
					return null;
				}
				if (command.equals("cancelNightMode")){
					tool.cancelNightMode();
				}
				return null;
			}
		});

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.settool", null, null);
	}

	private boolean mHasStatusListener = false;
	private List<NavStatusListener> mStatusListeners;
	/**
	 * 设置导航状态监听
	 * @param listener
	 * 			导航状态监听回调
	 */
	public void setNavStatusListener(NavStatusListener listener){
		if(listener==null){
			return;
		}
		if (mStatusListeners == null) {
			mStatusListeners = new ArrayList<NavStatusListener>();
		}
		mHasStatusListener = true;
		mStatusListeners.add(listener);
		sendSetNavStatusCmd();
	}
	
	private void sendSetNavStatusCmd(){
		TXZService.setCommandProcessor("status.nav.", new CommandProcessor() {
			
			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (mStatusListeners == null || mStatusListeners.size()<=0) {
					return null;
				}
				if (command.equals("enter")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onEnter(pkn);
					}
					return null;
				}
				if (command.equals("exit")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onExit(pkn);
					}
					return null;
				}
				if (command.equals("beginNav")) {
					JSONBuilder jsonBuilder = new JSONBuilder(data);
					Poi poi = Poi.fromString(jsonBuilder.getVal("poi", String.class));
					String pkn = jsonBuilder.getVal("packageName", String.class, null);
					for (NavStatusListener listener : mStatusListeners) {
						listener.onBeginNav(pkn, poi);
					}
					return null;
				}
				if (command.equals("foreground")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onForeground(pkn, true);
					}
					return null;
				}
				if (command.equals("background")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onForeground(pkn, false);
					}
					return null;
				}
				if (command.equals("start")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onStart(pkn);
					}
					return null;
				}
				if (command.equals("end")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onEnd(pkn);
					}
					return null;
				}
				if (command.equals("defaultNav")) {
					String pkn = data != null ? new String(data):null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onDefaultNavHasSeted(pkn);
					}
					return null;
				}
				if (command.equals("planSucc")) {
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onPlanSucc(pkn);
					}
					return null;
				}
				if (command.equals("planFail")) {
					JSONBuilder jsonBuilder = new JSONBuilder(data);
					String navPkn = jsonBuilder.getVal("navPkn", String.class, null);
					int errorCode = jsonBuilder.getVal("errorCode", Integer.class, 0);
					String reason = jsonBuilder.getVal("reason", String.class, null);
					for (NavStatusListener listener : mStatusListeners) {
						listener.onPlanFail(navPkn, errorCode, reason);
					}
					return null;
				}
				if(command.equals("ttsBegin")){
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onTtsStartOrEnd(pkn, true);
					}
					return null;
				}
				if(command.equals("ttsEnd")){
					String pkn = data != null ? new String(data) : null;
					for (NavStatusListener listener : mStatusListeners) {
						listener.onTtsStartOrEnd(pkn, false);
					}
					return null;
				}
				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.setStatusListener", null, null);
	}
	
	
	/**
	 * 内置导航工具
	 * 
	 * @author txz
	 *
	 */
	public static enum NavToolType {
		/**
		 * 同行者导航
		 */
		NAV_TOOL_TXZ,
		/**
		 * 百度地图
		 */
		NAV_TOOL_BAIDU_MAP,
		/**
		 * 百度导航
		 */
		NAV_TOOL_BAIDU_NAV,
		/**
		 * 百度导航HD
		 */
		NAV_TOOL_BAIDU_NAV_HD,
		/**
		 * 高德地图（minimap）
		 */
		NAV_TOOL_GAODE_MAP,
		/**
		 * 高德地图车机版（车镜版）
		 */
		NAV_TOOL_GAODE_MAP_CAR,
		/**
		 * 高德导航
		 */
		NAV_TOOL_GAODE_NAV,
		/**
		 * 凯立德导航
		 */
		NAV_TOOL_KAILIDE_NAV,
		/**
		 * 美行地图
		 */
		NAV_TOOL_MX_NAV,
		/**
		 * 好搜地图
		 */
		NAV_TOOL_QIHOO,
		/**
		 * 腾讯导航
		 */
		NAV_TOOL_TX,
		/**
		 * 凯行地图
		 */
		NAV_TOOL_KGO,
		/**
		 * 同行者标准导航工具
		 */
		NAV_TOOL_TXZ_COMM,
		/**
		 * 百度地图
		 */
		NAV_TOOL_BAIDU_MAPAUTO
	}

	/**
	 * 设置导航工具，默认(null)按优先级使用同行者工具->凯立德导航->高德导航->百度导航HD->百度导航->百度地图->高德地图
	 * 
	 * @param type
	 *            设置内置导航工具类型
	 */
	public void setNavTool(NavToolType type) {
		mHasSetTool = true;
		mNavTool = type;

		if (type == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.nav.settool", "".getBytes(), null);
			return;
		}

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.settool",
				getNavPackageNameByType(type).getBytes(), null);
	}
	
	private NavToolType mDefaultNav = null;
	
	/**
	 * 设置默认导航，优先级比setNavTool高
	 * 
	 * @param type
	 */
	public void setNavDefaultTool(NavToolType type) {
		mDefaultNav = type;
		if (type == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.clearDefaultNav", null, null);
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.setDefaultNav",
				getNavPackageNameByType(type).getBytes(), null);
	}
	
	static String getNavPackageNameByType(NavToolType type) {
		String nav = "";
		switch (type) {
		case NAV_TOOL_BAIDU_MAP:
			nav = "BAIDU_MAP";
			break;
		case NAV_TOOL_BAIDU_NAV:
			nav = "BAIDU_NAV";
			break;
		case NAV_TOOL_BAIDU_NAV_HD:
			nav = "BAIDU_NAV_HD";
			break;
		case NAV_TOOL_GAODE_MAP:
			nav = "GAODE_MAP";
			break;
		case NAV_TOOL_GAODE_MAP_CAR:
			nav = "GAODE_MAP_CAR";
			break;
		case NAV_TOOL_GAODE_NAV:
			nav = "GAODE_NAV";
			break;
		case NAV_TOOL_KAILIDE_NAV:
			nav = "KAILIDE_NAV";
			break;
		case NAV_TOOL_TXZ:
			nav = "TXZ";
			break;
		case NAV_TOOL_MX_NAV:
			nav = "MX_NAV";
			break;
		case NAV_TOOL_QIHOO:
			nav = "QIHOO_NAV";
			break;
		case NAV_TOOL_TX:
			nav = "TX_NAV";
			break;
		case NAV_TOOL_KGO:
			nav = "KGO_NAV";
			break;
		case NAV_TOOL_TXZ_COMM:
			nav = "TXZ_COMM";
			break;
		case NAV_TOOL_BAIDU_MAPAUTO:
			nav = "BAIDU_MAPAUTO";
			break;
		default:
			nav = "";
			break;
		}
		return nav;
	}
	
	/**
	 * 根据导航包名来设置导航工具
	 * @param pkg 
	 * 			导航地图包名
	 * @return
	 * 			同行者是否支持当前导航
	 */
	public boolean setNavTool(String pkg){
		if(pkg==null){
			return false;
		}
		if("com.baidu.BaiduMap".equals(pkg)){
			setNavTool(NavToolType.NAV_TOOL_BAIDU_MAP);
			return true;
		}else if ("com.baidu.navi".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_BAIDU_NAV);
			return true;
		}else if ("com.baidu.navi.hd".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_BAIDU_NAV_HD);
			return true;
		}else if ("com.autonavi.minimap".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_GAODE_MAP);
			return true;
		}else if ("com.autonavi.amapauto".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_GAODE_MAP_CAR);
			return true;
		}else if ("com.autonavi.xmgd.navigator".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_GAODE_NAV);
			return true;
		}else if ("com.txznet.nav".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_TXZ);
			return true;
		}else if ("com.mxnavi.mxnavi".equals(pkg)) {
			setNavTool(NavToolType.NAV_TOOL_MX_NAV);
			return true;
		}else if("cld.navi.kgomap".equals(pkg)){
			setNavTool(NavToolType.NAV_TOOL_KGO);
		} else if (pkg.matches("^cld\\.navi\\.\\S+\\.mainframe$")) {
			setNavTool(NavToolType.NAV_TOOL_KAILIDE_NAV);
			return true;
		}
		return false;
	}
	
	Boolean mUseActiveNav;
	
	/**
	 * 开启后，如果当前不是默认导航，并且当前的导航处于可见界面或者后台，语音会使用该导航做为下一次导航工具
	 * 
	 * @param useActive
	 */
	public void setUseActiveNav(boolean useActive) {
		mUseActiveNav = useActive;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.useActiveNav", (useActive + "").getBytes(),
				null);
	}

	private Boolean mBanNavTool;
	
	/**
	 * 是否禁用导航相关功能
	 * 
	 * @param isBan
	 */
	public void banNavAbility(boolean isBan) {
		mBanNavTool = isBan;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.banNavTool", (isBan + "").getBytes(),
				null);
	}
	
	private Boolean mAlwayAskNav;
	
	/**
	 * 设置发起导航时是否总是轮询选择导航工具
	 * @param isAlwayAsk
	 */
	public void setAlwayAskNav(boolean isAlwayAsk) {
		mAlwayAskNav = isAlwayAsk;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.alwayAsk", (isAlwayAsk + "").getBytes(),
				null);
	}
	
	private Boolean mEnableMulti;
	
	/**
	 * 是否启用多导航功能
	 * 
	 * @param enable
	 */
	public void enableMultiNavigation(boolean enable) {
		mEnableMulti = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.multinav", (enable + "").getBytes(), null);
	}

	private Integer mAutoNaviDelay = null;

	/**
	 * 进入路径规划界面后自动进入导航的时间， -1表示不自动执行
	 * 
	 * @param delay
	 */
	public void setPlanAutoNaviDelay(int delay) {
		mAutoNaviDelay = delay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.autoNaviDelay", (delay + "").getBytes(),
				null);
	}

	/**
	 * 导航到指定点
	 * 
	 * @param point
	 *            目标点
	 */
	public void navToLoc(Poi point) {
		try {
			JSONObject json = new JSONObject();
			json.put("lat", point.getLat());
			json.put("lng", point.getLng());
			json.put("city", point.getCity());
			json.put("name", point.getName());
			json.put("geo", point.getGeoinfo());
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.nav.navTo", json.toString().getBytes(), null);
		} catch (Exception e) {
		}
	}

	/**
	 * 弹出提示导航到指定点
	 * 
	 * @param text
	 *            提示对话框文本
	 * @param tts
	 *            语音提示内容
	 * @param point
	 *            目标点
	 */
	public void navToLocWithHint(String text, String tts, Poi point) {
		try {
			JSONObject json = new JSONObject();
			json.put("text", text);
			json.put("tts", tts);
			json.put("lat", point.getLat());
			json.put("lng", point.getLng());
			json.put("city", point.getCity());
			json.put("name", point.getName());
			json.put("geo", point.getGeoinfo());
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.nav.navToLocWithHint", json.toString().getBytes(),
					null);
		} catch (Exception e) {
		}
	}

	/**
	 * 回家
	 */
	public void navHome() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.navHome", null, null);
	}

	/**
	 * 去公司
	 */
	public void navCompany() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.navCompany", null, null);
	}

	/**
	 * 是否正在导航中
	 * 
	 * @return
	 */
	public boolean isInNav() {
		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.nav.isInNav", null);
		if (data == null)
			return false;
		return Boolean.parseBoolean(new String(data));
	}

	/**
	 * 获取同行者设置的家位置
	 */
	public Poi getHomeLocation() {
		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.nav.getHomeLocation", null);
		if (data == null)
			return null;
		return Poi.fromString(new String(data));
	}

	/**
	 * 设置同行者的家位置
	 */
	public void setHomeLocation(Poi poi) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.setHomeLocation", poi.toString().getBytes(), null);
	}

	/**
	 * 清空同行者的家位置
	 */
	public void clearHomeLocation() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.clearHomeLocation", null, null);
	}

	/**
	 * 获取同行者设置的公司位置
	 */
	public Poi getCompanyLocation() {
		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.nav.getCompanyLocation", null);
		if (data == null)
			return null;
		return Poi.fromString(new String(data));
	}

	/**
	 * 设置同行者的公司位置
	 */
	public void setCompanyLocation(Poi poi) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.setCompanyLocation", poi.toString().getBytes(), null);
	}

	/**
	 * 清空同行者的公司位置
	 */
	public void clearCompanyLocation() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.clearCompanyLocation", null, null);
	}

	/**
	 * 进入导航
	 */
	public void enterNav() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.enterNav", null, null);
	}

	/**
	 * 退出导航
	 */
	public void exitNav() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.exitNav", null, null);
	}

	/**
	 * 在自动模式下
	 * 通过大灯等使用场景设置黑夜模式
	 */
	public void setNightMode(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.setNightMode", null, null);
	}

	/**
	 * 取消黑夜模式
	 */
	public void cancelNightMode(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.cancelNightMode", null, null);
	}

	boolean mSetEnable;
	boolean mTraffic = true;
	boolean mIsNavi3D = false;
	boolean mIsNorth = true;
	boolean mEnableCmd = true;

	/**
	 * 是否启用地图界面唤醒指令，目前仅支持高德地图车机版
	 * 
	 * @param enableCmd
	 *            是否启用命令控制
	 * @param enableTraffic
	 *            是否启用开关路况命令
	 * @param enable3D
	 *            是否启用2/3D切换命令
	 * @param enableDirect
	 *            是否启用车方向命令
	 *            
	 * @see enableWakeupNavCmds
	 */
	@Deprecated
	public void enableAutoAMapCmd(boolean enableCmd, boolean enableTraffic, boolean enable3D, boolean enableDirect) {
		mSetEnable = true;
		mEnableCmd = enableCmd;
		mTraffic = enableTraffic;
		mIsNavi3D = enable3D;
		mIsNorth = enableDirect;
		try {
			JSONBuilder jb = new JSONBuilder();
			jb.put("enableCmd", enableCmd);
			jb.put("enableTraffic", enableTraffic);
			jb.put("enable3D", enable3D);
			jb.put("enableDirect", enableDirect);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.enablecmd", jb.toBytes(), null);
		} catch (Exception e) {
		}
	}
	
	Boolean mEnableWakeupNav;
	
	/**
	 * 是否启用唤醒控制导航命令
	 * @param enableWakeup
	 */
	public void enableWakeupNavCmds(boolean enableWakeup) {
		mEnableWakeupNav = enableWakeup;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.enableWakeupNav",
				("" + enableWakeup).getBytes(), null);
	}

	@Deprecated
	public void flingPager(int pos) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.selector.selection", (pos + "").getBytes(),
				null);
	}

	public void exitInteractiveWhenBackPoi(boolean isExit) {
		mExitItcWhenBack = isExit;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.selector.exitBack", (isExit + "").getBytes(), null);
	}

	public interface GetTxzNaviInfoListener {
		public void onUpdateNaviInfo(NaviInfo naviInfo);
	}

	Boolean mExitItcWhenBack;
	Boolean mWakeupExitNavAmapAuto;
	
	/**
	 * 是否允许导航唤醒退出，设置为false之后导航将不能直接唤醒退出导航
	 * @param enable
	 */
	public void enableWakeupExitNav(boolean enable){
		mWakeupExitNavAmapAuto = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.enableWakeupExit", (enable + "").getBytes(), null);
	}
	
	Boolean mForceRegister = null;
	
	/**
	 * 是否强制注册导航唤醒词（全局）
	 * @param isForce
	 */
	public void forceRegsiterMapOrder(boolean isForce) {
		mForceRegister = isForce;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.forceRegister",
				(isForce + "").getBytes(), null);
	}
	
	Boolean mEnableNavCmd = null;
	
	/**
	 * 是否使用导航命令
	 * @param enable
	 */
	public void enableNavCmd(boolean enable) {
		this.mEnableNavCmd = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.enableNavCmd",
				(enable + "").getBytes(), null);
	}
	
	String mRemotePkn;
	
	public void setNavCldPackageName(String pkn){
		this.mRemotePkn = pkn;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.cldpkn", mRemotePkn.getBytes(), null);
	}
	
	Boolean mRemoveNavDialog;
	
	/**
	 * 设置是否去掉导航的所有弹框
	 */
	public void setRemoveNavConfirmDialog(boolean isRemove) {
		mRemoveNavDialog = isRemove;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.cutNavDialog", (isRemove + "").getBytes(),
				null);
	}

	Boolean mClickOutSizeCancelDialog;

	/**
	 * 设置点击外部时是否关闭弹窗   false不关闭
	 * @param isCancel
	 */
	public void setClickOutSizeCancelDialog(boolean isCancel){
		mClickOutSizeCancelDialog = isCancel;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.clickOutSizeCancelDialog", (isCancel + "").getBytes(),
				null);
	}

	Boolean mPlan;
	
	public void enableSavePlanAfterPlan(boolean enable){
		mPlan = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.app.savePlan", (mPlan+"").getBytes(), null);
	}

	Boolean isCloseWhenSetHcAddr;

	/**
	 * 设置修改家或公司地址后是否直接退出交互
	 * @param isCloseWhenSetHcAddr
	 */
	public void setIsCloseWhenSetHcAddr(boolean isCloseWhenSetHcAddr) {
		this.isCloseWhenSetHcAddr = isCloseWhenSetHcAddr;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.isCloseWhenSetHcAddr", (isCloseWhenSetHcAddr + "").getBytes(), null);
	}
	
	NavVoicePlugin mVoicePlugin;

	/**
	 * 获取导航历史数据
	 * @param size
	 * @return
	 */
	public String getNavHistoryJson(int size) {
		ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ,
				"txz.nav.getNavHistory", (size + "").getBytes());
		if (data != null) {
			return data.getString();
		}
		return "";
	}

	/**
	 * 删除指定的导航历史
	 *
	 * @param dataStr
	 */
	public void removeNavHistory(String dataStr) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.removeNavHistory", dataStr.getBytes(), null);
	}

	/**
	 * 获取默认导航包名
	 * @return
	 */
	public String getDefaultNavTool() {
		ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.nav.getDefaultNavTool", null);
		if (data != null) {
			return data.getString();
		}
		return null;
	}

	/**
	 * 获取可用的导航包名
	 *
	 * @return
	 */
	public String getNavAppPkns() {
		ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.nav.getNavApps", null);
		if (data != null) {
			return data.getString();
		}
		return "";
	}

	private Integer mNavType;

	/**
	 * case 1:// 躲避拥堵
	   case 2:// 避免收费
	   case 3:// 不走高速
	   case 4:// 高速优先
	 设置导航的默认策略
	 * @param type
	 */
	public void setNavPlanType(int type) {
		mNavType = type;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.setNavPlanType",
				(type + "").getBytes(), null);
	}

	/**
	 * case 1:// 躲避拥堵
	 case 2:// 避免收费
	 case 3:// 不走高速
	 case 4:// 高速优先
	 * 获取导航的导航策略
	 * @return
	 */
	public int getNavPlanType() {
		ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ,
				"txz.nav.getNavPlanType", null);
		if (data != null) {
			return data.getInt();
		}
		return 0;
	}
	
	/**
	 * 启用导航播报TTS切换工具
	 * 
	 * @param callBack
	 */
	public void setNavVoiceCmdCallback(CallBack callBack) {
		if (mVoicePlugin == null) {
			mVoicePlugin = new NavVoicePlugin();
		}

		if (callBack == null) {
			mVoicePlugin.unRegisterVoiceCmds();
			return;
		}

		mVoicePlugin.setNavVoiceCmdCallback(callBack);
		setVoicePlugin();
	}
	
	private void setVoicePlugin() {
		if (mVoicePlugin != null) {
			setNavStatusListener(new NavStatusListener() {

				@Override
				public void onStart(String navPkg) {
					mVoicePlugin.registerVoiceCmds(navPkg);
				}

				@Override
				public void onForeground(String pkn, boolean isForeground) {
					if (isForeground) {
						mVoicePlugin.registerAgain();
					} else {
						mVoicePlugin.unRegisterVoiceCmds();
					}
				}

				@Override
				public void onExit(String pkn) {
					mVoicePlugin.unRegisterVoiceCmds();
					mVoicePlugin.resetAsrTask();
				}

				@Override
				public void onEnter(String pkn) {
				}

				@Override
				public void onEnd(String navPkg) {
					mVoicePlugin.unRegisterVoiceCmds();
					mVoicePlugin.resetAsrTask();
				}

				@Override
				public void onBeginNav(String pkn, Poi poi) {
				}

				@Override
				public void onStatusUpdate(String pkn) {
				}

				@Override
				public void onPlanSucc(String navPkn) {
				}

				@Override
				public void onPlanFail(String navPkn, int errorCode, String reason) {
				}

				@Override
				public void onDefaultNavHasSeted(String pkn) {
				}

				@Override
				public void onTtsStartOrEnd(String pkn, boolean isTts) {
				}
			});
		}
	}

	private TmcTool mTmcTool = null;

	public void setTmcTool(TmcTool tmcTool) {
		this.mTmcTool = tmcTool;
		if (tmcTool == null) {
			return;
		}

		tmcTool.setOperateListener(new TmcTool.OperateListener() {
			@Override
			public void startNotifyTraffic() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.tmc.operate", null, null);
			}

			@Override
			public void dismiss() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.tmc.dismiss", null, null);
			}
		});
		TXZService.setCommandProcessor("tool.tmc.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				boolean ret = false;
				if (command.equals("needWait")) {
					ret = mTmcTool.needWait();
				} else if (command.equals("isIgnore")) {
					ret = mTmcTool.isIgnore();
				} else if (command.equals("onSmartTraffic")) {
					try {
						ret = mTmcTool.onSmartTraffic(EquipmentManager.SmartTravel.parseFrom(data));
					} catch (InvalidProtocolBufferNanoException e) {
						e.printStackTrace();
					}
				} else if (command.equals("onViewDataUpdate")) {
					JSONBuilder jsonBuilder = new JSONBuilder(data);
					String title = jsonBuilder.getVal("title", String.class);
					String dat = jsonBuilder.getVal("data", String.class);
					ret = mTmcTool.onViewDataUpdate(title, dat);
				} else if (command.equals("onDismissDialog")) {
					mTmcTool.onDismissDialog();
				}
				return (ret + "").getBytes();
			}
		});

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.tmc.settool", null, null);
	}

	public interface TmcTool {
		interface OperateListener {
			/**
			 * 通知可以提醒路况信息
			 */
			void startNotifyTraffic();

			/**
			 * 关闭通知栏
			 */
			void dismiss();
		}

		/**
		 * 设置回调监听器
		 *
		 * @param listener
		 */
		void setOperateListener(OperateListener listener);

		/**
		 * 是否需要等待
		 *
		 * @return
		 */
		boolean needWait();

		/**
		 * 是否忽略本次提醒
		 *
		 * @return
		 */
		boolean isIgnore();

		/**
		 * 外部接管后续操作
		 *
		 * @param travel
		 * @return
		 */
		boolean onSmartTraffic(EquipmentManager.SmartTravel travel);

		/**
		 * 是否重写界面数据回调
		 */
		boolean onViewDataUpdate(String title, String data);

		/**
		 * 通知栏消失
		 */
		void onDismissDialog();
	}

	public static interface CallBack {
		public String[] getTypeCmds(String type);
	}
}