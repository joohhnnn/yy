package com.txznet.txz.component.nav.kgo.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.sdk.bean.Poi;

public class KgoBroadcastSender {

	private static KgoBroadcastSender sSender = new KgoBroadcastSender();

	private KgoBroadcastSender() {
	}

	public static KgoBroadcastSender getInstance() {
		return sSender;
	}

	/**
	 * 请求大灯状态
	 */
	public void queryBigLight() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_BIG_LIGHT, 0);
	}
	
	/**
	 * 查询导航是否处于前台
	 */
	public void queryIsInFocus() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_FRONT_GROUND, 0);
	}

	/**
	 * TODO 查询导航是否处于导航中
	 */
	public void queryIsInNav() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_NAVI_STATE);
	}
	
	/**
	 * 模拟点击导航的开始导航
	 */
	public void startNav(){
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_START_NAV);
	}
	
	/**
	 * TODO 查询当前路线信息
	 */
	public void queryCurrNavInfo() {

	}

	/**
	 * 打开导航
	 */
	public void openNav() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_OPENNAV);
	}

	/**
	 * 导航最小化
	 */
	public void hideNav() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_HIDENAV);
	}

	/**
	 * 退出导航程序
	 */
	public void exitNav() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_EXITNAV);
	}

	/**
	 * 
	 * @param mode
	 *            参考 DAY_NIGHT_STATUS
	 */
	public void setNavDayNightMode(int mode) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_DAY_NIGHT_MODE, mode);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_DAYNIGHT, kvs, 0);
	}

	/**
	 * 切换语音播报角色
	 * 
	 * @param role
	 */
	public void setNavBroadcastRole(int role) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_VOICE_ROLE, role);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_BROADROLE, kvs, 0);
	}

	/**
	 * 打开关闭路况
	 * 
	 * @param isOpen
	 */
	public void setTrafficStatus(boolean isOpen) {
		int opera = isOpen ? KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_TRAFFIC_OPEN
				: KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_TRAFFIC_CLOSE;
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_TYPE, KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.TYPE_TRAFFIC);
		kvs.put(KgoKeyConstants.KEY.EXTRA_OPERA, opera);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_MAP_CONTROL, kvs, 0);
	}

	/**
	 * 放大缩小地图
	 * 
	 * @param isZoomIn
	 */
	public void zoomMap(boolean isZoomIn) {
		int opera = isZoomIn ? KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_ZOOM_IN
				: KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_ZOOM_OUT;
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_TYPE, KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.TYPE_ZOOM);
		kvs.put(KgoKeyConstants.KEY.EXTRA_OPERA, opera);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_MAP_CONTROL, kvs, 0);
	}

	/**
	 * 车头朝上 正北朝上 3D模式
	 * 
	 * @param opera
	 *            参考 MAP_CONTROL OPERA_VIEWMODE
	 */
	public void setMapViewMode(int opera) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_TYPE, KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.TYPE_VIEWMODE);
		kvs.put(KgoKeyConstants.KEY.EXTRA_OPERA, opera);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_MAP_CONTROL, kvs, 0);
	}

	/**
	 * 设置导航播报是否静音
	 * 
	 * @param isMute
	 */
	public void setNavMute(boolean isMute) {
		int mute = isMute ? KgoKeyConstants.ACTION_STATUS_TYPE.MUTE_NAV.MUTE
				: KgoKeyConstants.ACTION_STATUS_TYPE.MUTE_NAV.UNMUTE;
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_MUTE, mute);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_MUTENAV, kvs, 0);
	}

	/**
	 * 设置家或者公司的地址
	 * 
	 * @param isHome
	 * @param lat
	 * @param lng
	 * @param name
	 * @param address
	 */
	public void setHcAddress(boolean isHome, double lat, double lng, String name, String address) {
		int type = isHome ? KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.HOME
				: KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.COMPANY;
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_TYPE, type);
		kvs.put(KgoKeyConstants.KEY.LAT, lat);
		kvs.put(KgoKeyConstants.KEY.LON, lng);
		kvs.put(KgoKeyConstants.KEY.POINAME, name);
		kvs.put(KgoKeyConstants.KEY.ADDRESS, address);
		// 默认发送国测局坐标
		kvs.put(KgoKeyConstants.KEY.DEV, KgoKeyConstants.ACTION_STATUS_TYPE.GPS_TYPE.GCJ02);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_SETADDRESS, kvs, 0);
	}

	/**
	 * 查询家或者公司的地址
	 * 
	 * @param isHome
	 */
	public void queryHCAddress(boolean isHome) {
//		int type = isHome ? KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.HOME
//				: KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.COMPANY;
		int type = isHome ? 1:2; // 1为家的地址 2为公司的地址
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_TYPE, type);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_QUERY_HC, kvs, 0);
	}

	/**
	 * 导航到目的地（不带路径偏好导航）
	 * 
	 * @param name
	 * @param lat
	 * @param lng
	 */
	public void navigateTo(String name, double lat, double lng) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.LAT, lat);
		kvs.put(KgoKeyConstants.KEY.LON, lng);
		kvs.put(KgoKeyConstants.KEY.POINAME, name);
		// 默认发送国测局坐标
		kvs.put(KgoKeyConstants.KEY.DEV, KgoKeyConstants.ACTION_STATUS_TYPE.GPS_TYPE.GCJ02);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_NAVIGATETO, kvs, 0);
	}
	
	/**
	 * 列表最后的Poi为目的地
	 * 
	 * @param tjs
	 */
	public void naviWithWayPoi(List<Poi> tjs,Poi destin) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		JSONArray jsonArray = new JSONArray();
		try {
			for (int i = 0; i < tjs.size(); i++) {
				final Poi poi = tjs.get(i);
				JSONObject obj = new JSONObject();
				obj.put(KgoKeyConstants.KEY.poiName, poi.getName());
				obj.put(KgoKeyConstants.KEY.latitude, poi.getLat());
				obj.put(KgoKeyConstants.KEY.longitude, poi.getLng());
				obj.put(KgoKeyConstants.KEY.dev, 0);
				obj.put(KgoKeyConstants.KEY.poiType, 2);// 途经点
				obj.put(KgoKeyConstants.KEY.subIndex, i);
				jsonArray.put(obj);
			}
			JSONObject obj = new JSONObject();
			obj.put(KgoKeyConstants.KEY.poiName, destin.getName());
			obj.put(KgoKeyConstants.KEY.latitude, destin.getLat());
			obj.put(KgoKeyConstants.KEY.longitude, destin.getLng());
			obj.put(KgoKeyConstants.KEY.dev, 0);
			obj.put(KgoKeyConstants.KEY.poiType, 1);// 目的地
			obj.put(KgoKeyConstants.KEY.subIndex, 0);
			jsonArray.put(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		kvs.put(KgoKeyConstants.KEY.EXTRA_POI_DATA, jsonArray.toString());
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_JINGYOUDI_NAV, kvs, 0);
	}

	/**
	 * 查看全程
	 * @param isAll
	 */
	public void zoomAllMap(boolean isAll) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_IS_SHOW, isAll ? KgoKeyConstants.ACTION_STATUS_TYPE.ZOOMALL_TYPE.MODE_ZOOM_ALL
				: KgoKeyConstants.ACTION_STATUS_TYPE.ZOOMALL_TYPE.MODE_ZOOM_BACK);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_ZOOM_ALL, kvs, 0);
	}
	
	/**
	 * 重新规划路径
	 * 
	 * @param plan
	 * @see KgoKeyConstants.ACTION_STATUS_TYPE.CHANGE_ROUTE
	 */
	public void changeRoute(int plan) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.NAVI_ROUTE_PREFER, plan);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_REPLAN, kvs, 0);
	}

	/**
	 * 路径规划界面选择哪一条路径
	 * 
	 * @param index
	 *            1为第一条 2为第二条 3为第三条
	 */
	public void selectNavRoad(int index) {
		Map<String, Object> kvs = new HashMap<String, Object>();
		kvs.put(KgoKeyConstants.KEY.EXTRA_CHANGE_ROAD, index);
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_SELECT_ROAD, kvs, 0);
	}

	/**
	 * 结束当前导航
	 */
	public void cancelNav() {
		KgoBroadcastDispatcher.getInstance().action(KgoKeyConstants.ACTION_TYPE.ACTION_REQUEST_CANCEL_NAV);
	}
}