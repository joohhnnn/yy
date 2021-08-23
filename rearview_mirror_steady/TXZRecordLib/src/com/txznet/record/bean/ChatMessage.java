package com.txznet.record.bean;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

import com.txznet.record.adapter.ChatContactListAdapter;

import java.util.List;

public class ChatMessage {
	public static final int TYPE_FROM_SYS_TEXT = 1; // 从系统发送来的信息
	public static final int TYPE_FROM_SYS_HELP_WITH_ICON = 2; // 从系统发来的帮助信息（不带气泡），带图标
	public static final int TYPE_FROM_SYS_CONTACT = 3; // 从系统发送来的列表信息
	public static final int TYPE_TO_SYS_TEXT = 4; // 发送给系统的信息
	public static final int TYPE_FROM_SYS_WX_PICKER = 5; // 从系统发生过来的微信选择信息
	public static final int TYPE_FROM_SYS_POI = 6;
	public static final int TYPE_FROM_SYS_AUDIO = 7;
	public static final int TYPE_FROM_SYS_MUSIC = 8;// 音频（新版音乐）,telenewbie.
	public static final int TYPE_FROM_TRAFFIC = 9;
	public static final int TYPE_FROM_PLUGIN = 10;
	public static final int TYPE_FROM_TTS_THEME = 11;
	public static final int TYPE_FROM_SYS_SIM_RECHARGE = 12;
	public static final int TYPE_FROM_SYS_DISPLAY_CONTAINER = 13;
	public static final int TYPE_FROM_SYS_SIMPLE_LIST = 14;
	public static final int TYPE_FROM_SYS_NAVS_LIST = 15;
	public static final int TYPE_FROM_SYS_QRCODE = 16;
	public static final int TYPE_FROM_SYS_TEXT_HL = 17;
	public static final int TYPE_FROM_SYS_TEXT_WITH_INTERRUPT_TIPS = 18;//带打断提示的系统文本
	public static final int TYPE_FROM_SYS_HELP_TIPS = 19; //帮助引导
	public static final int TYPE_FROM_SYS_REMINDER_LIST = 20; //提醒列表
	public static final int TYPE_TO_SYS_PART_TEXT = 21; //打字消息
	public static final int TYPE_FROM_SYS_FLIGHT = 22; //航班信息
	public static final int TYPE_FROM_SYS_NEWS = 23; //新闻信息
	public static final int TYPE_FROM_SYS_TRAIN = 24; //火车票信息
	public static final int TYPE_FROM_SYS_MOVIE_THEATER = 25; //电影院信息
	public static final int TYPE_FROM_SYS_MOVIE_TIME = 26; //电影场次
	public static final int TYPE_FROM_SYS_MOVIE_SEAT_PLAN = 27;//电影票座位图
	public static final int TYPE_FROM_SYS_MI_HOME = 28; // 桑德车控家
	public static final int TYPE_FROM_SYS_BIND_SERVICE = 29; // 绑定设备二维码
	public static final int TYPE_FROM_SYS_CONSTELLATION_FORTUNE = 31; // 星座查询
	public static final int TYPE_FROM_SYS_CONSTELLATION_MATCHING  = 32; // 星座配对

	public static final int TYPE_FROM_SYS_COMPETITION_LIST = 33;//赛事选择界面
	public static final int TYPE_FROM_SYS_COMPETITION_DETAIL = 34;//赛事单个详情界面
	public static final int TYPE_FROM_SYS_FEEDBACK = 35;//我要反馈界面
	public static final int TYPE_CHAT_LOGO_QRCODE = 36;//带LOGO二维码
	public static final int TYPE_CHAT_OFFLINE_PROMOTE = 37;//文本+LOGO的离线促活页面

	public static final int TYPE_FROM_SYS_TRAIN_TICKET = 36; //齐悟火车票
	public static final int TYPE_FROM_SYS_FLIGHT_TICKET = 37; //齐悟飞机票
	public static final int TYPE_FROM_SYS_TICKET_PAY = 38; // 齐悟火车票待支付界面


	public static final int OWNER_SYS = 0;
	public static final int OWNER_USER = 1;
	public static final int OWNER_USER_PART = 2;

	public ChatMessage(int type) {
		this.type = type;
	}

	public int owner;
	public int type; // 信息类型
	public String text; // 文本

	public Drawable icon; // 图标
	public OnClickListener iconCallback; // 图标点击事件

	public String title;
	public List<ChatContactListAdapter.ContactItem> items;

	public List<WxContact> wxItems;
}
