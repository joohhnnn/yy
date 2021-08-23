package com.txznet.txz.component.nav.kgo.internal;

import java.util.ArrayList;

import org.json.JSONObject;

import com.txz.ui.map.UiMap;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.kgo.internal.KgoBroadcastDispatcher.OnReceiverListener;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavInterceptTransponder;

import android.content.Context;
import android.content.Intent;

/**
 * 凯行广播处理管理
 * 
 * @author TXZ-METEORLUO
 *
 */
public class KgoBroadcastManager {
	private NavThirdApp mParentApp;
	private ArrayList<OnNavStatusUpdateListener> mUpdateListeners = new ArrayList<KgoBroadcastManager.OnNavStatusUpdateListener>();
	private static KgoBroadcastManager broadcastManager = new KgoBroadcastManager();

	private KgoBroadcastManager() {
	}

	public static KgoBroadcastManager getInstance() {
		return broadcastManager;
	}
	
	public void assignParent(NavThirdApp app) {
		mParentApp = app;
	}

	public void init(Context context) {
		KgoBroadcastDispatcher.getInstance().init(context);
		KgoBroadcastDispatcher.getInstance().setOnReceiverListener(receiver);
	}

	private OnReceiverListener receiver = new OnReceiverListener() {

		@Override
		public void onReceiver(int errorCode, Intent intent) {
			if (errorCode == KgoKeyConstants.ERROR_CODE.ERROR_TIMEOUT) {
				LogUtil.logw("kgo onReceiver timeout");
				return;
			}

			handle(intent);
		}
	};

	/**
	 * 注册一个监听器
	 * 
	 * @param listener
	 */
	public void addNavStatusUpdateListener(OnNavStatusUpdateListener listener) {
		if (!mUpdateListeners.contains(listener)) {
			mUpdateListeners.add(listener);
		}
	}

	/**
	 * 反注册一个监听器
	 * 
	 * @param listener
	 */
	public void removeNavStatusUpdateListener(OnNavStatusUpdateListener listener) {
		if (mUpdateListeners.contains(listener)) {
			mUpdateListeners.remove(listener);
		}
	}

	/**
	 * 处理导航的广播
	 * 
	 * @param intent
	 */
	public void handle(Intent intent) {
		int action = intent.getIntExtra(KgoKeyConstants.KEY.ACTION, -999);
		if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_BIG_LIGHT) {
			int state = intent.getIntExtra(KgoKeyConstants.KEY.EXTRA_BIGLIGHT_STATE, -999);
			handleBigLight(state);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_NAVINFO) {
			handleNavInfo(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_SETADDRESS) {
			handleSetAddress(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_QUERY_HC) {
			handleNavAddress(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_NAV_TTS) {
			handleNavTTS(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_MAPVERSION) {
			handleMapVersion(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_ZOOM) {
			handleMapZoomState(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_REQ_RESP_FRONT_GROUND) {
			handleNavFocusUpdate(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_REQ_RESP_NAVI_STATE) {
			handleNavStatusUpdate(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_CURRENT_ROUTE_UPDATE) {
			handleCurrentRouteUpdate(intent);
		} else if (action == KgoKeyConstants.ACTION_TYPE.ACTION_RECV_CONTINUE_NAVI) {
			handleContinueNavDialog(intent);
		}
	}

	private void handleContinueNavDialog(Intent intent) {
		int eType = intent.getIntExtra("EXTRA_ENDURANCE_TYPE", -1);
		LogUtil.logd("handleContinueNavDialog eType:" + eType);
//		if (eType == 0) {
//
//		} else if (eType == 1) {
//
//		} else if (eType == 2) {
//
//		}
		synchronized (mUpdateListeners) {
			for (OnNavStatusUpdateListener listener : mUpdateListeners) {
				listener.onNavConfirmDialogUpdate(eType);
			}
		}
	}

	/**
	 * 大灯状态通知
	 * 
	 * @param state
	 */
	private void handleBigLight(int state) {

	}

	/**
	 * 处理导航引导信息通知
	 * 
	 * @param intent
	 */
	private void handleNavInfo(Intent intent) {
		NavInfo navInfo = new NavInfo();
		navInfo.parseKgo(intent);
		synchronized (mUpdateListeners) {
			for (OnNavStatusUpdateListener listener : mUpdateListeners) {
				listener.onNavInfoUpdate(navInfo);
			}
		}
	}

	/**
	 * 处理导航前后台切换
	 * 
	 * @param intent
	 */
	private void handleNavFocusUpdate(Intent intent) {
		boolean state = intent.getBooleanExtra(KgoKeyConstants.KEY.NAVI_HIDE, false);
		boolean isFocus = state == KgoKeyConstants.ACTION_STATUS_TYPE.NAV_HIDE_TYPE.HIDE ? false : true;
		if (mParentApp != null) {
			if (NavInterceptTransponder.getInstance().interceptGroundIntent(mParentApp, intent, isFocus)) {
				LogUtil.logd("kgomap intercept isFocus:" + isFocus);
				return;
			}
		}
		synchronized (mUpdateListeners) {
			for (OnNavStatusUpdateListener listener : mUpdateListeners) {
				listener.onFocusChange(isFocus);
			}
		}
	}

	/**
	 * 处理导航状态切换
	 * 
	 * @param intent
	 */
	private void handleNavStatusUpdate(Intent intent) {
		boolean isInNav = intent.getBooleanExtra(KgoKeyConstants.KEY.EXTRA_ROUTE, false);
		synchronized (mUpdateListeners) {
			for (OnNavStatusUpdateListener listener : mUpdateListeners) {
				listener.onNavChange(isInNav);
			}
		}
	}
	
	private void handleCurrentRouteUpdate(Intent intent) {
		LogUtil.logd("handleCurrentRouteUpdate:" + intent.getExtras());
		try {
			String roadInfo = intent.getStringExtra("EXTRA_ROAD_INFO");
			JSONObject jsonObject = new JSONObject(roadInfo);
			PathInfo pathInfo = new PathInfo();
			if (jsonObject.has("FromPoiName")) {
				pathInfo.fromPoiName = jsonObject.optString("FromPoiName");
			}
			if (jsonObject.has("FromPoiLatitude")) {
				pathInfo.fromPoiLat = jsonObject.optDouble("FromPoiLatitude");
			}
			if (jsonObject.has("FromPoiLongitude")) {
				pathInfo.fromPoiLng = jsonObject.optDouble("FromPoiLongitude");
			}
			if (jsonObject.has("ToPoiName")) {
				pathInfo.toPoiName = jsonObject.optString("ToPoiName");
			}
			if (jsonObject.has("ToPoiLatitude")) {
				pathInfo.toPoiLat = jsonObject.optDouble("ToPoiLatitude");
			}
			if (jsonObject.has("ToPoiLongitude")) {
				pathInfo.toPoiLng = jsonObject.optDouble("ToPoiLongitude");
			}
			
			synchronized (mUpdateListeners) {
				for (OnNavStatusUpdateListener listener : mUpdateListeners) {
					listener.onNavPathUpdate(pathInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 语音设置家和公司地址的时候导航返回处理结果
	 * 
	 * @param intent
	 */
	private void handleSetAddress(Intent intent) {
		int category = intent.getIntExtra(KgoKeyConstants.KEY.CATEGORY, -999);
		int code = intent.getIntExtra(KgoKeyConstants.KEY.EXTRA_RESPONSE_CODE, -999);
		LogUtil.logd("handleSetAddress category:" + category + ",code:" + code);
		switch (category) {
		case KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.HOME:
			if (code == KgoKeyConstants.ACTION_STATUS_TYPE.RESP_CODE.FAIL) {
				// TODO 清空语音保存家的地址
//				NavManager.getInstance().clearHomeLocation();
				// 设置失败后重新请求当前导航家的地址
				KgoBroadcastSender.getInstance().queryHCAddress(true);
			}
			break;

		case KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.COMPANY:
			if (code == KgoKeyConstants.ACTION_STATUS_TYPE.RESP_CODE.FAIL) {
				// TODO 清空语音保存公司的地址
//				NavManager.getInstance().clearCompanyLocation();
				// 设置失败后重新请求当前导航公司的地址
				KgoBroadcastSender.getInstance().queryHCAddress(false);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 查询家和公司地址返回
	 * 
	 * @param intent
	 */
	private void handleNavAddress(Intent intent) {
		double lat = intent.getDoubleExtra(KgoKeyConstants.KEY.LAT, -999);
		double lng = intent.getDoubleExtra(KgoKeyConstants.KEY.LON, -999);
		int distance = intent.getIntExtra(KgoKeyConstants.KEY.DISTANCE, -999);
		String name = intent.getStringExtra(KgoKeyConstants.KEY.POINAME);
		String address = intent.getStringExtra(KgoKeyConstants.KEY.ADDRESS);
		// 错误的拼写，凯立德将错就错
		int category = intent.getIntExtra("CETEGORY", -999); // 1为家 2为公司
		if (category == -999) {
			category = intent.getIntExtra("CATEGORY", -999);
		}
		LogUtil.logd("handleNavAddress query add back :" + lat + "," + lng + "," + distance + "," + name + "," + address
				+ "," + category);

		switch (category) {
//		case KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.HOME:
		case 1:
			// TODO 设置和清空家的地址
			NavManager.getInstance().setHomeLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
			break;
//		case KgoKeyConstants.ACTION_STATUS_TYPE.HOMECOMPANY.COMPANY:
		case 2:
			NavManager.getInstance().setCompanyLocation(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
			break;
		default:
			break;
		}
	}

	/**
	 * 导航TTS开始和结束
	 * 
	 * @param intent
	 */
	private void handleNavTTS(Intent intent) {
		int state = intent.getIntExtra(KgoKeyConstants.KEY.EXTRA_VALUE, -999);
		if (state == KgoKeyConstants.ACTION_STATUS_TYPE.TTS_TYPE.START) {
			// TODO 导航开始播报TTS
			TXZMediaFocusManager.getInstance().requestFocus();
		} else if (state == KgoKeyConstants.ACTION_STATUS_TYPE.TTS_TYPE.END) {
			// TODO 导航播报TTS结束
			TXZMediaFocusManager.getInstance().releaseFocus();
		}
		LogUtil.logd("handleNavTTS:" + state);
	}
	
	/**
	 * 解析导航版本号信息
	 * @param intent
	 */
	private void handleMapVersion(Intent intent) {
		String version = intent.getStringExtra(KgoKeyConstants.KEY.VERSION_NUM);
		String channel = intent.getStringExtra(KgoKeyConstants.KEY.CHANNEL_NUM);
		LogUtil.logd("handleMapVersion version:" + version + ",channel:" + channel);
		synchronized (mUpdateListeners) {
			for (OnNavStatusUpdateListener listener : mUpdateListeners) {
				listener.onMapVersionUpdate(version, channel);
			}
		}
	}
	
	/**
	 * 处理地图放大和缩小反馈
	 * @param intent
	 */
	private void handleMapZoomState(Intent intent) {
		int zoomType = intent.getIntExtra(KgoKeyConstants.KEY.EXTRA_ZOOM_TYPE, -999);
		boolean canZoom = intent.getBooleanExtra(KgoKeyConstants.KEY.EXTRA_CAN_ZOOM, true);
	}
	
	public static interface OnNavStatusUpdateListener {
		/**
		 * 焦点状态通知
		 * 
		 * @param isFocus
		 */
		public void onFocusChange(boolean isFocus);

		/**
		 * 导航状态通知
		 * 
		 * @param isInNav
		 */
		public void onNavChange(boolean isInNav);

		/**
		 * 导航退出
		 */
		public void onAppExit();

		/**
		 * 导航引导数据更新通知
		 * 
		 * @param navInfo
		 */
		public void onNavInfoUpdate(NavInfo navInfo);
		
		/**
		 * 导航版本信息的推送
		 * @param version
		 * @param channel
		 */
		public void onMapVersionUpdate(String version,String channel);

		/**
		 * 导航路径信息的更新
		 * 
		 * @param pathInfo
		 */
		public void onNavPathUpdate(PathInfo pathInfo);

		/**
		 * 导航续航对话框通知
		 *
		 * @param state
		 */
		public void onNavConfirmDialogUpdate(int state);
	}
}