package com.txznet.webchat.plugin.preset.logic.action;

/*
    所有用户操作、API返回触发的事件
    通过Dispatcher转发，系统只有一个唯一的输入源
 */
public interface ActionType {

    String WX_DOWNLOAD_IMAGE_RESP = "wx_download_image_resp"; // 图片回调
    String WX_DOWNLOAD_VOICE_REQ = "wx_download_voice_req"; // 音频请求
    String WX_DOWNLOAD_VOICE_RESP = "wx_download_voice_resp"; // 音频回调
    String WX_DOWNLOAD_VOICE_RESP_ERROR = "wx_download_voice_resp_error"; // 音频回调错误

    String WX_SEND_MSG_REQ = "wx_send_msg_req"; // 发送消息
    String WX_SEND_MSG_RESP = "wx_send_msg_resp"; // 发送消息响应
    String WX_SEND_MSG_RESP_ERROR = "wx_send_msg_resp_error"; // 发送消息响应失败


    // -------------------------插件事件-----------------------------

    // 核心逻辑
    String WX_QRCODE_REQUEST = "wx_qrcode_request"; //开始请求二维码
    String WX_QRCODE_SUCCESS = "wx_qrcode_success"; //请求二维码成功
    String WX_QRCODE_FAIL = "wx_qrcode_fail"; //请求二维码失败

    String WX_QRCODE_SCAN = "wx_qrcode_scanned"; //用户扫码

    String WX_LOGIN_SUCCESS = "wx_login_success";   //微信登陆成功
    String WX_LOGIN_FAIL = "wx_login_fail"; //微信登陆失败

    String WX_LOGOUT_REQUEST = "wx_logout_request"; //请求退出登陆
    String WX_LOGOUT_SUCCESS = "wx_logout_success"; //退出成功
    String WX_LOGOUT_FAIL = "wx_logout_fail";   //退出失败

    // 重置状态
    String WX_PLUGIN_LOGIC_RESET = "wx_plugin_logic_reset"; // 微信登录状态丢失，需要重置数据

    // contact
    String WX_PLUGIN_SYNC_SESSION = "wx_plugin_sync_session";
    String WX_PLUGIN_SYNC_SESSION_LIST = "wx_plugin_sync_session_list";
    String WX_PLUGIN_SYNC_CONTACT = "wx_plugin_sync_contact";
    String WX_PLUGIN_SYNC_GROUP_CONTACT = "wx_plugin_sync_group_contact";
    String WX_PLUGIN_SYNC_USER = "wx_plugin_sync_user";
    String WX_PLUGIN_SYNC_TOP_SESSION = "wx_plugin_sync_top_session";
    String WX_PLUGIN_SYNC_MOD_CONTACT = "wx_plugin_sync_mod_contact";
    String WX_PLUGIN_SYNC_DEL_CONTACT = "wx_plugin_sync_del_contact";

    // message
    String WX_PLUGIN_MSG_ADD_MSG = "wx_plugin_msg_add_msg";
    String WX_PLUGIN_REVOKE_MSG_REQUEST = "wx_plugin_revoke_msg_request";
    String WX_PLUGIN_REVOKE_MSG_SUCCESS = "wx_plugin_revoke_msg";
    String WX_PLUGIN_REVOKE_MSG_FAILED = "wx_plugin_revoke_msg_failed";


    // 推送登录相关事件
    String WX_PLUGIN_PUSH_LOGIN_REQUEST = "wx_plugin_push_login_request"; // 开始推送登录请求
    String WX_PLUGIN_AUTO_LOGIN_REQUEST = "wx_plugin_auto_login_request"; // 唤醒自动登录请求
    String WX_PLUGIN_PUSH_LOGIN_SUCCESS = "wx_plugin_push_login"; // 推送登录成功
    String WX_PLUGIN_PUSH_LOGIN_FAILED = "wx_plugin_push_login_failed"; // 推送登录失败
    String WX_PLUGIN_PUSH_LOGIN_SYNC_CONTACT = "wx_plugin_push_login_sync_contact"; // 同步本地账户列表
    String WX_PLUGIN_PUSH_LOGIN_SWITCH_CONTACT = "wx_push_login_switch_contact"; // 切换推送用户

    // 更新资源加载cookie
    String WX_PLUGIN_UPDATE_RESOURCE_COOKIE = "wx_plugin_update_resource_cookie";

}
