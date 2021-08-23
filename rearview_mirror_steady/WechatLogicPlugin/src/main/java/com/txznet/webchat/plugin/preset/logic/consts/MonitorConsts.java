package com.txznet.webchat.plugin.preset.logic.consts;

/**
 * 用于监控上报的常量
 * Created by J on 2017/3/6.
 */

public class MonitorConsts {
    // 用户扫描二维码
    public static final String WX_LOGIN_QR_SCANNED = "wx3.login.I.qr_scanned";
    // 登录成功
    public static final String WX_LOGIN_SUCCESS = "wx3.login.I.suc";
    // 登陆失败
    public static final String WX_LOGIN_FAILED_CHECK_AUTH = "wx3.login.W.auth_err"; // 鉴权失败
    public static final String WX_LOGIN_FAILED_INIT = "wx3.login.E.init_err"; // 初始化失败
    // 唤醒自动登录
    public static final String WX_LOGIN_SUCCESS_RESTORE = "wx3.login.I.restore_suc";
    public static final String WX_LOGIN_FAILED_RESTORE = "wx3.login.E.restore_err";

    // 免扫码登录(PushLogin)
    public static final String WX_LOGIN_PUSH_ENTER = "wx3.login.I.push_enter";
    public static final String WX_LOGIN_PUSH_FAILED_NET = "wx3.login.E.push_err_net";
    public static final String WX_LOGIN_PUSH_FAILED_RET = "wx3.login.E.push_err_ret";
    public static final String WX_LOGIN_PUSH_SUCCESS = "wx3.login.I.push_suc";

    // 普通文本消息发送成功
    public static final String WX_TEXT_SEND_SUCCESS = "wx3.text.I.send_suc";
    // 普通文本消息发送失败
    public static final String WX_TEXT_SEND_FAILED = "wx3.text.E.send_err";

    // 语音发送成功
    public static final String WX_VOICE_SEND_SUCCESS = "wx3.voice.I.send_suc";
    // 语音发送失败
    public static final String WX_VOICE_SEND_FAILED = "wx3.voice.E.send_err";

    // 图片发送成功
    public static final String WX_IMG_SEND_SUCCESS = "wx3.img.I.send_suc";
    // 图片发送失败
    public static final String WX_IMG_UPLOAD_FAILED = "wx3.img.E.upload_err";
    public static final String WX_IMG_SEND_FAILED = "wx3.img.E.send_err";

    // 位置分享成功
    public static final String WX_LOC_SEND_SUCCESS = "wx3.loc.I.send_suc";
    // 位置分享失败
    public static final String WX_LOC_SEND_FAILED = "wx3.loc.E.send_err";

    // 语音消息接收
    public static final String WX_VOICE_DOWNLOAD_SUCCESS = "wx3.voice.I.download_suc";
    public static final String WX_VOICE_DOWNLOAD_FAILED = "wx3.voice.E.download_err";
    public static final String WX_VOICE_DOWNLOAD_FAILED_SAVE = "wx3.voice.E.save_err";
    // 位置消息接收
    public static final String WX_LOC_RESOLVE_SUCCESS = "wx3.loc.I.res_suc"; // 位置解析成功
    public static final String WX_LOC_RESOLVE_FAILED = "wx3.loc.E.res_err"; // 位置解析失败
    public static final String WX_LOC_RESOLVE_URL_FAILED = "wx3.loc.E.url_err"; // 地址url解析失败
    public static final String WX_LOC_XML_RESOLVE_SUCCESS = "wx3.loc.I.res_suc_xml"; // 位置消息xml解析成功
    public static final String WX_LOC_XML_RESOLVE_FAILED = "wx3.loc.E.res_err_xml"; //位置消息xml解析失败

    // 头像下载失败
    public static final String WX_RES_GET_AVATAR_FAILED = "wx3.res.get_head_err";

    // sync重试次数达到上限
    public static final String WX_SYNC_FAILED_RETRY_LIMIT = "wx3.logic.E.sync_limit";
    // sync change重试次数达到上限
    public static final String WX_SYNC_CHANGE_FAILED_RETRY_LIMIT = "wx3.logic.sync_change_limit";

}
