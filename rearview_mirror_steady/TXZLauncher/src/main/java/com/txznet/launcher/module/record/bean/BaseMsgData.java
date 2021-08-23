package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

/**
 * Created by ASUS User on 2018/3/5.
 * record相关数据类的积累，定义了类型，以及解析data的抽象方法
 */

public abstract class BaseMsgData {
    public static final int TYPE_CHAT_FROM_SYS = 1;
    public static final int TYPE_CHAT_TO_SYS = 2;
    public static final int TYPE_CHAT_WEATHER = 3;
    public static final int TYPE_CHAT_STOCK = 4; // 股票
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


    public int type;

    public BaseMsgData(int type) {
        this.type = type;
    }

    public abstract void parseData(JSONBuilder jsData);
}
