package com.txznet.txz.component.nav.kgo.internal;

public interface KgoKeyConstants {
	// 导航包名
	public static String NAVI_PACKAGE_NAME = "cld.navi.kgomap";

	interface BROADCAST_ACTION {
		// 发送给导航的广播ACTION
		public static final String BROADCAST_SEND = "CLDNAVI_STANDARD_BROADCAST_RECV";
		// 接收导航的广播ACTION
		public static final String BROADCAST_RECV = "CLDNAVI_STANDARD_BROADCAST_SEND";
	}

	/**
	 * 字段常量
	 */
	interface KEY {
		public static final String SESSION_ID = "SESSION_ID";
		public static final String ACTION = "KEY_TYPE";
		public static final String SOURCE_APP = "SOURCE_APP";

		public static final String EXTRA_BIGLIGHT_STATE = "EXTRA_HEADLIGHT_STATE";
		public static final String EXTRA_DAY_NIGHT_MODE = "EXTRA_DAY_NIGHT_MODE";
		public static final String EXTRA_VOICE_ROLE = "VOICE_ROLE";

		public static final String EXTRA_TYPE = "EXTRA_TYPE";
		public static final String EXTRA_OPERA = "EXTRA_OPERA";
		public static final String EXTRA_MUTE = "EXTRA_MUTE";
		public static final String EXTRA_VALUE = "EXTRA_VALUE";
		public static final String EXTRA_IS_SHOW = "EXTRA_IS_SHOW";
		public static final String EXTRA_POI_DATA = "EXTRA_POI_DATA";
		public static final String NAVI_ROUTE_PREFER = "NAVI_ROUTE_PREFER";

		public static final String POINAME = "POINAME";
		public static final String LON = "LON";
		public static final String LAT = "LAT";
		public static final String ADDRESS = "ADDRESS";
		public static final String DEV = "DEV";
		public static final String CATEGORY = "CATEGORY";
		public static final String DISTANCE = "DISTANCE";
		public static final String EXTRA_RESPONSE_CODE = "EXTRA_RESPONSE_CODE";
		public static final String EXTRA_CHANGE_ROAD = "EXTRA_CHANGE_ROAD";
		
		public static final String poiName = "poiName";
		public static final String latitude = "latitude";
		public static final String longitude = "longitude";
		public static final String dev = "dev";
		public static final String poiType = "poiType";
		public static final String subIndex = "subIndex";

		public static final String TYPE = "TYPE";
		public static final String CUR_ROAD_NAME = "CUR_ROAD_NAME";
		public static final String NEXT_ROAD_NAME = "NEXT_ROAD_NAME";
		public static final String SAPA_DIST = "SAPA_DIST";
		// 电子眼类型
		public static final String CAMERA_TYPE = "CAMERA_TYPE";
		public static final String CAMERA_SPEED = "CAMERA_SPEED";
		// 转弯方向
		public static final String ICON = "ICON";
		public static final String ROUTE_REMAIN_DIS = "ROUTE_REMAIN_DIS";
		public static final String ROUTE_REMAIN_TIME = "ROUTE_REMAIN_TIME";
		public static final String SEG_REMAIN_DIS = "SEG_REMAIN_DIS";
		public static final String SEG_REMAIN_TIME = "SEG_REMAIN_TIME";
		public static final String LIMITED_SPEED = "LIMITED_SPEED";
		public static final String ROUTE_ALL_DIS = "ROUTE_ALL_DIS";
		public static final String ROUTE_ALL_TIME = "ROUTE_ALL_TIME";
		public static final String CUR_SPEED = "CUR_SPEED";
		public static final String ROAD_TYPE = "ROAD_TYPE";
		// 是否到达目的地
		public static final String ARRIVE_STATUS = "ARRIVE_STATUS";
		public static final String VERSION_NUM = "VERSION_NUM";
		public static final String CHANNEL_NUM = "CHANNEL_NUM";
		public static final String EXTRA_ZOOM_TYPE = "EXTRA_ZOOM_TYPE";
		public static final String EXTRA_CAN_ZOOM = "EXTRA_CAN_ZOOM";
		public static final String NAVI_HIDE = "NAVI_HIDE";
		public static final String EXTRA_ROUTE = "EXTRA_ROUTE";
	}

	/**
	 * 广播事件
	 */
	interface ACTION_TYPE {
		public static final int ACTION_RECV_NAVINFO = 10001;
		public static final int ACTION_REQUEST_REPLAN = 10005;
		public static final int ACTION_REQUEST_ZOOM_ALL = 10006;
		public static final int ACTION_REQUEST_START_NAV = 10009;
		public static final int ACTION_REQUEST_CANCEL_NAV = 10010;
		public static final int ACTION_REQUEST_BIG_LIGHT = 10016;
		public static final int ACTION_RECV_BIG_LIGHT = 10017;
		public static final int ACTION_REQUEST_EXITNAV = 10021;
		public static final int ACTION_REQUEST_MAP_CONTROL = 10027;
		public static final int ACTION_REQUEST_HIDENAV = 10031;
		public static final int ACTION_REQUEST_JINGYOUDI_NAV = 10032;
		public static final int ACTION_REQUEST_OPENNAV = 10034;
		public static final int ACTION_REQUEST_NAVIGATETO = 10038;
		public static final int ACTION_RECV_MAPVERSION = 10041;
		public static final int ACTION_REQUEST_BROADROLE = 10044;
		public static final int ACTION_REQUEST_QUERY_HC = 10045;
		public static final int ACTION_RECV_QUERY_HC = 10046;
		public static final int ACTION_REQUEST_MUTENAV = 10047;
		public static final int ACTION_REQUEST_DAYNIGHT = 10048;
		public static final int ACTION_RECV_CONTINUE_NAVI = 10049;
		public static final int ACTION_REQUEST_SELECT_ROAD = 10055;
		public static final int ACTION_RECV_CURRENT_ROUTE_UPDATE = 10056;
		public static final int ACTION_REQUEST_SETADDRESS = 10058;
		public static final int ACTION_RECV_SETADDRESS = 10059;
		public static final int ACTION_RECV_ZOOM = 10074;
		public static final int ACTION_RECV_NAV_TTS = 100001;
		public static final int ACTION_REQUEST_FRONT_GROUND = 100401;
		public static final int ACTION_REQ_RESP_FRONT_GROUND = 100402;
		public static final int ACTION_REQUEST_NAVI_STATE = 100701;
		public static final int ACTION_REQ_RESP_NAVI_STATE = 100702;
	}

	interface ACTION_STATUS_TYPE {

		/**
		 * 昼夜模式状态值
		 */
		interface DAY_NIGHT {
			public static final int MODE_DAY = 0;
			public static final int MODE_NIGHT = 1;
			public static final int MODE_AUTO = 2;
			public static final int MODE_BIGLIGHT = 3;
		}

		/**
		 * 播报角色
		 */
		interface VOICE_ROLE {
			public static final int PTH = 0;
			public static final int TWH = 1;
			public static final int SCH = 2;
			public static final int GDH = 3;
		}

		/**
		 * 图面操作
		 */
		interface MAP_CONTROL {
			public static final int TYPE_TRAFFIC = 0;
			public static final int TYPE_ZOOM = 1;
			public static final int TYPE_VIEWMODE = 2;

			public static final int OPERA_TRAFFIC_OPEN = 0;
			public static final int OPERA_TRAFFIC_CLOSE = 1;

			public static final int OPERA_ZOOM_IN = 0;
			public static final int OPERA_ZOOM_OUT = 1;

			public static final int OPERA_VIEWMODE_CAR = 0;
			public static final int OPERA_VIEWMODE_NORTH = 1;
			public static final int OPERA_VIEWMODE_3D = 2;
		}

		interface MUTE_NAV {
			public static final int MUTE = 0;
			public static final int UNMUTE = 1;
		}

		interface HOMECOMPANY {
			public static final int HOME = 0;
			public static final int COMPANY = 1;
		}

		interface GPS_TYPE {
			public static final int GCJ02 = 0;
			public static final int WGS84 = 1;
		}

		interface RESP_CODE {
			public static final int SUCCESS = 0;
			public static final int FAIL = 1;
		}

		interface TTS_TYPE {
			public static final int START = 0;
			public static final int END = 1;
		}
		
		interface ZOOM_TYPE {
			public static final int ZOOM_IN = 0;
			public static final int ZOOM_OUT = 1;
		}
		
		interface NAV_HIDE_TYPE {
			public static final boolean HIDE = true;
			public static final boolean VISIBLE = false;
		}
		
		interface ZOOMALL_TYPE {
			public static final int MODE_ZOOM_ALL = 1;
			public static final int MODE_ZOOM_BACK = 2;
			public static final int MODE_START_NAV = 3;
			public static final int MODE_CONT_NAV = 4;
		}
		
		interface CHANGE_ROUTE {
			public static final int PLAN_DUOBIYONGDU = 10;
			public static final int PLAN_GAOSUYOUXIAN = 11;
			public static final int PLAN_BUZOUGAOSU = 10;
			public static final int PLAN_LESS_MONEY = 10;
		}
	}

	/**
	 * 导航通知的状态
	 */
	interface NAVI_STATUS {

	}

	/**
	 * 错误码
	 */
	interface ERROR_CODE {
		public static final int ERROR_TIMEOUT = -1;
		public static final int ERROR_SUCCESS = 0;
	}
}