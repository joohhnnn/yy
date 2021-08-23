package com.txznet.comm.ui.viewfactory.data;

public class ViewData {
	private int type = 0;
	public static final int TYPE_CHAT_FROM_SYS = 1;
	public static final int TYPE_CHAT_TO_SYS = 2;
	public static final int TYPE_CHAT_WEATHER = 3;
	public static final int TYPE_CHAT_SHARE = 4; // 股票
	public static final int TYPE_FULL_LIST_POI = 5;
	public static final int TYPE_FULL_LIST_CALL = 6;//联系人
	public static final int TYPE_FULL_LIST_WECHAT = 7;//微信联系人
	public static final int TYPE_FULL_LIST_AUDIO = 8;//音乐列表
	public static final int TYPE_FULL_LIST_SIM = 9;//流量充值
	public static final int TYPE_FULL_LIST_TTS = 10;//tts主题
	public static final int TYPE_FULL_LIST_HELP = 11;//帮助列表
	public static final int TYPE_FULL_NO_TTS_QRCORD = 12;//tts主题扫描二维码界面
	public static final int TYPE_FULL_LIST_CINEMA = 13;//最新电影
	public static final int TYPE_LEFT_RECORD_VIEW = 14;//左边声控动画
	public static final int TYPE_BOTTOM_RECORD_VIEW = 15;//下边声控动画
	public static final int TYPE_LIST_TITLE_VIEW = 16;//上边的Title
	public static final int TYPE_FULL_LIST_HELP_DETAIL = 17;//帮助列表详情
	public static final int TYPE_CHAT_MAP = 18; // 地图
	public static final int TYPE_FULL_LIST_MAPPOI = 19; //地图模式的POI 
	public static final int TYPE_QRCODE = 20; // 二维码
	public static final int TYPE_CHAT_FROM_SYS_HL = 21; // 高亮标注的系统文本
	public static final int TYPE_CHAT_FROM_SYS_INTERRUPT = 22; // 带打断提示的系统文本
	public static final int TYPE_CHAT_HELP_TIPS = 23; // 列表中显示的帮助信息
	public static final int TYPE_FULL_LIST_HELP_IMAGE_DETAIL = 24; // 帮助图片详情
	public static final int TYPE_FULL_LIST_SIMPLE_LIST = 25;// 简单的数据列表展示（导航APP选择列表）
	public static final int TYPE_FULL_LIST_REMINDER = 26; // 提醒列表
	public static final int TYPE_CHAT_TO_SYS_PART = 27;//打字消息
	public static final int TYPE_SEARCH_EDIT_VIEW = 28;//编辑poi关键字界面
	public static final int TYPE_SELECT_CITY_VIEW = 29;//编辑poi城市选择界面
	public static final int TYPE_FULL_LIST_FLIGHT = 30;//机票列表
	public static final int TYPE_FULL_LIST_TRAIN = 32;//火车票列表
	public static final int TYPE_FULL_LIST_STYLE = 33;//皮肤包主题列表
	public static final int TYPE_FLOAT_VIEW = 34;//悬浮图标
	public static final int TYPE_AUTHORIZATION_VIEW = 35;
	public static final int TYPE_FULL_MOVIE_THEATER_LIST = 36;//电影票场景中的电影院列表
	public static final int TYPE_FULL_FILM_LIST = 37;//电影票场景中的电影列表
	public static final int TYPE_FULL_MOVIE_TIME_LIST = 38;//电影票场景中的电影场次（放映时间）列表
	public static final int TYPE_FULL_MOVIE_SEATING_PLAN = 39; //电影票场景中的影厅座位图。
	public static final int TYPE_FULL_MOVIE_PHONE_NUM_QRCODE = 40;//电影票场景中验证电话号码二维码图。
	public static final int TYPE_FULL_MOVIE_WAITING_PAY_QRCODE = 41;//电影票场景中等待支付的二维码页面
	public static final int TYPE_CHAT_BIND_DEVICE_QRCODE = 42; //绑定设备二维码
	public static final int TYPE_CHAT_CONSTELLATION_FORTUNE = 43; // 星座运势
	public static final int TYPE_CHAT_CONSTELLATION_MATCHING = 44; // 星座配对
	public static final int TYPE_FULL_LIST_COMPETITION = 45; //赛事界面
	public static final int TYPE_CHAT_COMPETITION_DETAIL = 46; //赛事界面
	public static final int TYPE_CHAT_FEEDBACK = 47; //我要反馈界面
	public static final int TYPE_FULL_LIST_TRAIN_TICKET = 48; //齐悟火车票
	public static final int TYPE_FULL_LIST_FLIGHT_TICKET = 49; //齐悟飞机票
	public static final int TYPE_FULL_TICKET_PAY = 50; // 齐悟火车票待支付界面
	public static final int TYPE_CHAT_LOGO_QRCODE = 51;//带Logo的二维码页面
	public static final int TYPE_CHAT_OFFLINE_PROMOTE = 52;//文本+LOGO的离线促活页面
	public ViewData(int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
}
