package com.txznet.webchat.plugin.preset.logic.model;

public class MsgConst {
    public static final int MSG_TYPE_TEXT = 1; //文本消息
    public static final int MSG_TYPE_HTML = 2; //HTML消息
    public static final int MSG_TYPE_IMG = 3; //图片消息
    public static final int MSG_TYPE_VOICEMSG = 34; //语音消息
    public static final int MSG_TYPE_CONTACT = 42; // 联系人
    public static final int MSG_TYPE_ANIM_IMG = 47; // 动画图片
    public static final int MSG_TYPE_URL = 49; // 链接
    public static final int MSG_TYPE_STATUSNOTIFY = 51; // 状态通知消息
    public static final int MSG_TYPE_SIGHT = 43; // 小视频
    public static final int MSG_TYPE_SYSTEM = 10000; // 系统通知

    // MSG_TYPE_TEXT的子类型
    public static final int SUBMSG_TYPE_TEXT = 0; //文本消息
    public static final int SUBMSG_TYPE_LOCATION = 48; //位置消息

    // MSG_TYPE_URL的子类型
    public static final int APPMSG_TYPE_TRANSFER = 2000; // 转账
    public static final int APPMSG_TYPE_ARTICLE = 5; // 文章
    public static final int APPMSG_TYPE_URL = 1; // 网址
    public static final int APPMSG_TYPE_FILE = 6; // 文件

    public static final int STATUS_NOTIFY_CODE_SYNC_CONV = 4; // 状态通知码：同步会话
    public static final int STATUS_NOTIFY_CODE_TOP_SESSION = 2; // 状态通知码：置顶会话

    public static final int MSG_SYNC_RET_LOGOUT_FROM_MOBILE = 1101; //手机侧注销
    public static final int MSG_SYNC_RET_LOGOUT_RET_CODE_1100 = 1100; //未知
    public static final int MSG_SYNC_RET_LOGOUT_RET_CODE_1102 = 1102; //未知
}