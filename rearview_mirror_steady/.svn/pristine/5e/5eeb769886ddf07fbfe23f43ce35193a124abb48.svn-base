package com.txznet.webchat.actions;

/*
    所有用户操作、API返回触发的事件
    通过Dispatcher转发，系统只有一个唯一的输入源
 */
public interface ActionType {
    String TXZ_BIND_INFO_REQ = "txz_bind_info_req"; // 请求绑定信息
    String TXZ_BIND_INFO_RESP = "txz_bind_info_resp"; // 绑定信息响应
    String TXZ_BIND_INFO_RESP_ONLY_NICK = "txz_bind_info_resp_only_nick"; // 绑定信息改变
    String TXZ_BIND_INFO_RESP_ERROR = "txz_bind_info_resp_error"; // 绑定信息响应失败

    String SYSTEM_POWER_SLEEP = "system_power_sleep"; // 系统休眠
    String SYSTEM_POWER_WAKEUP = "system_power_wakeup"; // 系统休眠
    String SYSTEM_POWER_REVERSE_ENTER = "system_power_reverse_enter"; // 进入倒车
    String SYSTEM_POWER_REVERSE_EXIT = "system_power_reverse_exit"; // 退出倒车
    String REQUEST_APP_EXIT = "request_app_exit"; // 应用关闭
    String TXZ_RESTART = "txz_restart"; // 同行者重启

    String WX_ENTRY_ENABLE = "wx_entry_enable"; // 微信登录入口开启
    String WX_ENTRY_DISABLE = "wx_entry_disable"; // 微信登录入口关闭

    String WX_AUTO_BROAD_ENABLE = "wx_auto_broad_enable"; // 启动自动播报
    String WX_AUTO_BROAD_DISABLE = "wx_auto_broad_disable"; // 关闭自动播报

    String WX_GROUP_MSG_BROAD_ENABLE = "wx_group_msg_broad_enable"; // 开启群消息播报
    String WX_GROUP_MSG_BROAD_DISABLE = "wx_group_msg_broad_disable"; // 屏蔽群消息播报

    String WX_LOC_MSG_PROC_ENABLE = "wx_loc_msg_proc_enable"; // 启用位置消息处理
    String WX_LOC_MSG_PROC_DISABLE = "wx_loc_msg_proc_disable"; // 禁用位置消息处理

    String WX_LOC_SHARE_ENABLE = "wx_loc_share_enable"; // 启用位置分享功能
    String WX_LOC_SHARE_DISABLE = "wx_loc_share_disable"; // 禁用位置分享功能

    String WX_GROUP_CONTACT_ENABLE = "wx_group_contact_enable"; // 取消群联系人展示屏蔽
    String WX_GROUP_CONTACT_DISABLE = "wx_group_contact_disable"; // 屏蔽群联系人展示

    String WX_WAKEUP_ASR_CMD_ENABLE = "wx_wakeup_asr_cmd_enable"; // 开启唤醒指令
    String WX_WAKEUP_ASR_CMD_DISABLE = "wx_wakeup_asr_cmd_disable"; // 关闭唤醒指令

    String WX_AUTO_LOGIN_ENABLE = "wx_auto_login_enable"; // 开启微信开机登录
    String WX_AUTO_LOGIN_DISABLE = "wx_auto_login_disabled"; // 关闭微信开机登录

    String WX_INIT_REPORT_REQ = "wx_init_report_req"; // 微信状态上报
    String WX_INIT_REPORT_RESP = "wx_init_report_resp";
    String WX_INIT_REPORT_RESP_ERROR = "wx_init_report_resp_error";

    String WX_SWITCH_SESSION = "wx_switch_session"; // 切换会话
    String WX_OPEN_CHAT = "wx_open_chat"; // 打开聊天界面
    String WX_CLOSE_CHAT = "wx_close_chat"; // 关闭聊天界面

    String WX_BLOCK_CONTACT = "wx_block_contact"; // 屏蔽消息
    String WX_UNBLOCK_CONTACT = "wx_unblock_contact"; // 取消屏蔽

    String WX_DOWNLOAD_IMAGE_RESP = "wx_download_image_resp"; // 图片回调
    String WX_DOWNLOAD_VOICE_REQ = "wx_download_voice_req"; // 音频请求
    String WX_DOWNLOAD_VOICE_RESP = "wx_download_voice_resp"; // 音频回调
    String WX_DOWNLOAD_VOICE_RESP_ERROR = "wx_download_voice_resp_error"; // 音频回调错误

    String WX_DOWNLOAD_FILE_ADD = "wx_download_file_add"; // 添加文件下载任务
    String WX_DOWNLOAD_FILE_REQ = "wx_download_file_req"; // 文件开始下载
    String WX_DOWNLOAD_FILE_RESP = "wx_download_file_resp"; // 文件下载成功
    String WX_DOWNLOAD_FILE_CANCEL = "wx_download_file_cancel"; // 文件下载取消
    String WX_DOWNLOAD_FILE_RESP_ERROR = "wx_download_file_resp_error"; // 文件下载失败

    String WX_REPEAT_MSG = "wx_repeat_msg"; // 重复播报
    String WX_SKIP_REPEAT_MSG = "wx_skip_repeat"; // 跳过消息重播
    String WX_SKIP_MSG = "wx_skip_msg"; // 跳过播报

    String TXZ_TTS_QUEUE_PROC = "txz_tts_queue_proc"; // 处理tts队列
    String TXZ_TTS_QUEUE_ADD = "txz_tts_queue_add"; //  添加tts播报
    String TXZ_TTS_QUEUE_INSERT = "txz_tts_queue_insert"; //  插入tts播报
    String TXZ_TTS_CANCEL_CUR = "txz_tts_cancel_cur"; // 取消当前播报

    String WX_REPLY_VOICE_START = "wx_reply_voice_start"; // 开始回复微信
    String WX_REPLY_VOICE_SEND = "wx_reply_voice_send"; // 发送
    String WX_REPLY_VOICE_END = "wx_reply_voice_end"; // 回复微信结束
    String WX_REPLY_VOICE_PARSE = "wx_reply_voice_parse"; // 回复微信网络识别结束
    String WX_REPLY_VOICE_MUTE = "wx_reply_voice_mute"; // 回复微信结束
    String WX_REPLY_VOICE_ERROR = "wx_reply_voice_error"; // 回复微信失败
    String WX_REPLY_VOICE_CANCEL = "wx_reply_voice_cancel"; // 取消回复微信

    String WX_SEND_MSG_REQ = "wx_send_msg_req"; // 发送消息
    String WX_SEND_MSG_RESP = "wx_send_msg_resp"; // 发送消息响应
    String WX_SEND_MSG_RESP_ERROR = "wx_send_msg_resp_error"; // 发送消息响应失败

    String TXZ_NOTIFY_SHOW = "txz_notify_show"; // 展示通知
    String TXZ_NOTIFY_DISMISS = "txz_notify_dismiss"; // 关闭通知
    String TXZ_NOTIFY_CANCEL = "txz_notify_cancel"; // 取消通知

    String VOICE_UPLOAD_REQ = "voice_upload_req"; // 语音上传请求
    String VOICE_UPLOAD_RESP = "voice_upload_resp"; //  语音上传响应
    String VOICE_UPLOAD_RESP_ERROR = "voice_upload_resp_error"; // 语音上传异常

    String WX_MEDIA_FOCUS_CHANGED = "wx_media_focus_changed"; // 第三方媒体焦点占用状态发生变化

    String WX_WINDOW_PARAM_CHANGED = "wx_window_param_changed"; // 界面显示参数发生变化
    String WX_APP_VISIBILITY_CHANGED = "wx_app_visibility_changed"; // 界面可见性发生变化(前后台切换/息屏/亮屏)
    String WX_SERVER_CONFIG_CHANGED = "wx_server_config_changed"; // 服务器下发配置发生变化
    String WX_PLUGIN_DOWNLOAD_SUCCESS = "wx_plugin_download_success"; // 微信插件下载完成


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
