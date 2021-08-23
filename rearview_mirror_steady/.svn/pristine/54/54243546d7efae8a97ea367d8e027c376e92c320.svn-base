package com.txznet.webchat.actions;

/**
 * 用于微信插件的调用命令
 *
 * 注: 不要修改此接口中已定义的action!!!!
 * 此处定义的常量字段只为在车载微信客户端发起插件调用时方便标识Action, 插件中维护了同样一套常量用于
 * 接收客户端发起的调用. 所有未同步的修改都可能导致微信客户端与插件的兼容性问题.
 *
 * 新增action需要在对应的微信插件中添加对应逻辑对新action进行响应.
 *
 * 注2: 为保证插件调用的正确处理, 约定对此接口的任意修改需要在修改位置添加注释, 标明时间点和修改原因, 同步到
 * 插件/客户端维护的另外一份副本中, 并对客户端与插件的兼容性进行评估
 *
 * Created by J on 2016/11/21.
 * Modified by J on 2017/10/16: 更新类注释
 */

public interface PluginInvokeAction {
    // 插件提供的调用命令
    String INVOKE_CMD_START = "wechat.plugin.cmd.start";
    String INVOKE_CMD_REFRESH_LOGIN_QR = "wechat.plugin.cmd.refresh_login_qr";
    String INVOKE_CMD_START_SYNC = "wechat.plugin.cmd.start_sync";
    String INVOKE_CMD_LOGOUT = "wechat.plugin.cmd.logout";
    String INVOKE_CMD_SLEEP = "wechat.plugin.cmd.sleep";
    String INVOKE_CMD_WAKEUP = "wechat.plugin.cmd.wakeup";
    String INVOKE_CMD_GET_USER_HEAD = "wechat.plugin.cmd.get_contact_image";
    String INVOKE_CMD_GET_VOICE = "wechat.plugin.cmd.get_voice";
    String INVOKE_CMD_SEND_MSG = "wechat.plugin.cmd.send_msg";
    String INVOKE_CMD_REVOKE_MSG = "wechat.plugin.cmd.revoke_msg";

    // 推送登录相关
    String INVOKE_CMD_GET_USER_CACHE = "wechat.plugin.cmd.get_user_cache"; // 获取本地用户列表
    String INVOKE_CMD_PUSH_LOGIN = "wechat.plugin.cmd.push_login"; // 开始推送登录信息
    String INVOKE_CMD_PUSH_LOGIN_CANCEL = "wechat.plugin.cmd.push_login_cancel"; // 取消推送登录任务
    String INVOKE_CMD_PUSH_GET_QRCODE = "wechat.plugin.cmd.get_qrcode"; // 获取二维码
    String INVOKE_CMD_CLEAR_CACHE = "wechat.plugin.cmd.clear_cache"; // 清除本地缓存
}
