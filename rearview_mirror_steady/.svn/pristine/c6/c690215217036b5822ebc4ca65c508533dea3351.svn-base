package com.txznet.txz.component.nav.tx.internal;

public class ExternalDefaultBroadcastKey {
	/** 导航包名 **/
	public static final String PACKAGE_NAME = "com.tencent.wecarnavi";

	/** 广播 */
	public final class BROADCAST_ACTION {
		public static final String SEND = "WECARNAVIAUTO_STANDARD_BROADCAST_SEND";// 腾讯主动发送信息的广播
		public static final String RECV = "WECARNAVIAUTO_STANDARD_BROADCAST_RECV";// 腾讯接收的广播
		public static final String FEEDBACK = "WECARNAVIAUTO_STANDARD_BROADCAST_FEEDBACK";// 腾讯发送反馈的广播
	}

	/** 事件 */
	public final class TYPE {
		public static final int STOP_COMMAND = 999;// 停止指令操作

		public static final int TIMEOUT = 1000;// 响应超时

		public static final int MAP = TIMEOUT + 1;// 地图操作
		public static final int NAVI_OPEN_CLOSE = TIMEOUT + 2;// 启动或退出导航app
		public static final int ROUTE_PLAN_START_STOP_NAVI = TIMEOUT + 3;// 开始结束导航
		public static final int NAVI_TO_HOME_COMPANY = TIMEOUT + 4;// 回家去公司
		public static final int RG_FULLVIEW = TIMEOUT + 5;// 查看和退出全览图
		public static final int ROUTE_PLAN_SELECT = TIMEOUT + 6;// 路线选择页，路线选择
		public static final int NAVI_BACKGROUND = TIMEOUT + 7;// 导航app 最小化
		public static final int NAVI_TO_POI = TIMEOUT + 8;// 导航到目的地
		public static final int REQ_CUR_ADDRESS = TIMEOUT + 9;// 请求获取当前地址
		public static final int REQ_REMAIN_TIME_DISTANCE = TIMEOUT + 10;// 请求获取距离目的地所剩时间和距离
		public static final int NAVI_CONFIRM_DIALOG_SHOW = TIMEOUT + 11; // 对话框展示
		public static final int NAVI_CONFIRM_DIALOG_DISMISS = TIMEOUT + 12; // 对话框消失
		public static final int NAVI_CONFIRM_DIALOG_ORDER = TIMEOUT + 13; // 对话框发送命令
		public static final int NAVI_QUERY_HOME_COMPANY_ADDR = TIMEOUT + 14;// 检索家和公司地址
		public static final int NAVI_SEND_HOME_COMPANY_ADDR = TIMEOUT + 15;//发送家和公司地址
		public static final int NAVI_SET_HOME_COMPANY_ADDR = TIMEOUT + 16;// 设置家和公司地址
		public static final int NAVI_BROADCAST_SPEECH_MODE = TIMEOUT + 17;//设置新手模式详细模式
		public static final int NAVI_SPEECH_MUTE_MODE = TIMEOUT + 18;//设置静音模式
		public static final int NAVI_PUSH_WXPOI_DIALOG_SHOW = TIMEOUT + 20; // 微信我的车发送的信息弹框
		public static final int NAVI_PUSH_WXPOI_DIALOG_HIDE = TIMEOUT + 21;// 微信我的车弹框消失
		public static final int NAVI_PUSH_WXPOI_DIALOG_ORDER = TIMEOUT + 22;// 微信我的车弹框发送指令
		public static final int NAVI_PUSH_WXPOI_DIALOG_AUTO_DISMISS = TIMEOUT + 23;// 微信我的车弹框发送指令
		public static final int NAVI_REPLAN_ROUTE = TIMEOUT + 24;// 重新规划路径的类型

		public static final int NAVI_STATUS = 2000;// 导航状态值变化

	}

	public final class KEY {
		public static final String SESSION_ID = "key_session_id";// 会话ID
		public static final String RAW_TEXT = "key_raw_text";// 原始文本
		public static final String ACTION = "key_action";

		public static final String KEY_TYPE = "KEY_TPYE";// 类型关键字
		public static final String EXTRA_TYPE = "EXTRA_TYPE";
		public static final String EXTRA_OPERA = "EXTRA_OPERA";
		public static final String EXTRA_EXIT_TYPE = "EXTRA_EXIT_TYPE";
		public static final String EXTRA_NEED_FEEDBACK = "NEED_FEEDBACK";// 是否需要反馈的key
		public static final String EXTRA_NOTIMEOUT = "notimeout";
		public static final String EXTRA_AUTO_START_NAVI = "";

		public static final String SOURCE_APP = "SOURCE_APP";
		public static final String POINAME = "POINAME";
		public static final String LAT = "LAT";
		public static final String LON = "LON";
		public static final String COORD = "COORD";
		public static final String ADDRESS = "ADDRESS";
		public static final String TAG = "TAG";
		public static final String TIME = "TIME";
		public static final String DISTANCE = "DISTANCE";
		public static final String FEEDBACK_CODE = "FEEDBACK_CODE";// 返回错误码
		public static final String FEEDBACK_WORD = "FEEDBACK_WORD";// 返回错误码
		public static final String TYPE = "TYPE";
		public static final String RST = "RST";

		public static final String NAME = "NAME";// 名称关键字
		public static final String INDEX = "INDEX";// 索引
		public static final String ISDELAY = "ISDEALY";// 是否延迟

		public static final String FEEDBACK_SESSION_ID = "key_feedback_sessionid";// 反馈会话ID
		public static final String FEEDBACK_TTS_ID = "key_tts_template_id";// TTS播报模板ID
		public static final String FEEDBACK_PARAM = "key_param";// 参数(数组, 可选)
		public static final String FEEDBACK_NAME = "key_feedback_name";// 参数(数组,
																		// 可选)
		public static final String FEEDBACK_PARAM_DEST_NAME = "key_feedback_dest_name";// 终点名称
		public static final String FEEDBACK_PARAM_DEST_REMAIN_DISTANCE = "key_feedback_dest_remain_distance";// 距离终点距离
		public static final String FEEDBACK_PARAM_DEST_REMAIN_TIME = "key_feedback_dest_remain_time";// 距离终点时间

	}

	/** 反馈错误码定义 */
	public final class FEEDBACK_CODE {
		public static final int NOT_SUPPORT = -1;// 指令不支持
		public static final int SUCCUESS = 0;// 成功执行
		public static final int FAILED = 1;// 执行失败(网络异常
		public static final int CONTINUE_SELECT = 2;// 二次交互-选择第几个
		public static final int RETRY = 3;// 二次交互-重试
	}

	public final class STATUS_TYPE {
		public static final int INIT_BEGIN = 0;// 初始化开始
		public static final int INIT_END = INIT_BEGIN + 1;// 初始化结束
		public static final int EXIT = INIT_BEGIN + 2;// 程序退出
		public static final int FOREGROUND = INIT_BEGIN + 3;// 进入前台
		public static final int BACKGROUD = INIT_BEGIN + 4;// 进入后台
		public static final int PLAN_SUCCESS = INIT_BEGIN + 5;// 算路成功
		public static final int START_NAV = INIT_BEGIN + 6;// 开始导航
		public static final int END_NAV = INIT_BEGIN + 7;// 结束导航
	}

	/**
	 * 自定义Session字段，用于处理反馈文本
	 */
	public static class FB_SESSION_DEFAULT {
		public static final String VIEW_ALL_SESSION = "SESSION_VIEW_ALL";
	}
}
