package com.txznet.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.bean.WechatMessage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TXZWechatManager {
    private static TXZWechatManager sInstance = new TXZWechatManager();
    private static final String ACTION_IMAGE_READY = "com.txznet.webchat.action.SDK_DOWNLOAD_IMG_COMPLETE";

    private boolean mHasSetWechatTool = false;
    private boolean bBlockUI = false;
    private WechatTool mWechatTool;

    private boolean mHasSetNotificationTool = false;
    private NotificationTool mNotificationTool;

    private BroadcastReceiver mImageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            String img = intent.getStringExtra("img");

            if (!StringUtils.isEmpty(id)) {
                if (mImageListenerMap.containsKey(id)) {
                    mImageListenerMap.get(id).onImageReady(id, img);
                    mImageListenerMap.remove(id);
                }
            }

        }
    };

    private TXZWechatManager() {
        // 注册下载图片完成的BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IMAGE_READY);
        GlobalContext.get().registerReceiver(mImageReceiver, filter);
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TXZWechatManager getInstance() {
        return sInstance;
    }

    void onReconnectTXZ() {
        if (mHasSetWechatTool) {
            setWechatTool(mWechatTool, bBlockUI);
        }
        if (mHasSetNotificationTool) {
            setNotificationTool(mNotificationTool);
        }
        if (mAuto != null) {
            enableAutoSpeak(mAuto);
        }
    }

    /**
     * 跳过当前消息播报，可用作手势或方控处理微信消息
     */
    public void skipCurrentMessage() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.skipCurrentMessage", null, null);
    }

    /**
     * 重听最后一条消息播报，可用作手势或方控处理微信消息
     */
    public void repeatLastMessage() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.repeatLastMessage", null, null);
    }

    /**
     * 调起录音，发送给上次发送的用户
     */
    public void repeatSendMessage(String tip) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("tip", tip);
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.repeat.send", builder.toBytes(), null);
    }

    /**
     * 调起录音，发送给最近联系人（最近一个发送给/播报消息的用户）
     */
    public void sendToRecentContact(String tip) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("tip", tip);
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.repeat.recent", builder.toBytes(), null);
    }

    /**
     * 调起录音，回复最近一个收到消息的联系人
     */
    public void replyToRecentContact(String tip) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("tip", tip);
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.reply.recent", builder.toBytes(), null);
    }

    /**
     * 调起录音, 发送给当前焦点会话(正在播报消息的联系人, 或聊天界面当前选中的联系人)
     *
     * @param tip 发送前播报的tts
     */
    public void replyToCurrentContact(String tip) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("tip", tip);
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.reply.current", builder.toBytes(), null);
    }

    /**
     * 临时屏蔽当前联系人播报，可用作手势或方控处理微信消息
     */
    public void blockCurrentContact() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.blockCurrentContact", null, null);
    }

    private Boolean mAuto = null;

    /**
     * 开关微信自动播报，可用作手势或方控处理微信消息
     */
    public void enableAutoSpeak(boolean auto) {
        mAuto = auto;

        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.enableAutoSpeak", ("" + auto).getBytes(), null);
    }

    /**
     * 退出应用
     *
     * @param doLogout 控制是否注销车载微信，false则只退出界面
     */
    public void exit(boolean doLogout) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.exit", ("" + doLogout).getBytes(), null);
    }

    /**
     * 取消录音
     */
    public void cancelRecord() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.cancelRecord", null, null);
    }

    /**
     * 结束录音并发送
     */
    public void finishRecord() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.finishRecord", null, null);
    }

    /**
     * 撤回最后一条发送的消息
     */
    public void revokeLastMessage() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.revokeLastMsg", null, null);
    }

    /**
     * 是否自动登陆
     *
     * @param enable
     */
    public void enableWakeupLogin(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.wakeupLogin", ("" + enable).getBytes(), null);
    }

    /**
     * 是否启用界面唤醒指令
     */
    public void enableWakupAsrCmd(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.enableWakupAsrCmd", ("" + enable).getBytes(), null);
    }

    /**
     * 设置是否启用导航消息处理(未启用导航消息处理的情况下, 收到导航消息按普通消息处理, 不提示导航过去, 也不会
     * 响应用户说的导航过去指令)
     *
     * @param enable
     */
    public void setLocMsgEnabled(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.setting.enableLocMsg", ("" + enable).getBytes(), null);
    }

    /**
     * 设置是否启用位置分享功能(位置分享关闭状态下不支持"分享当前位置"的语音指令, 声控界面进行位置分享会提示"当
     * 前设备不支持位置分享功能")
     *
     * @param enable
     */
    public void setLocShareEnabled(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.setting.enableLocShare", ("" + enable).getBytes(), null);
    }

    /**
     * 设置是否全屏显示微信界面
     *
     * @param enable
     * @deprecated 已废弃, 建议使用微信配置文件进行相关设置
     */
    @Deprecated
    public void enableUIFullScreen(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.set.ui_fullscreen", ("" + enable).getBytes(), null);
    }

    public void exit() {
        exit(true);
    }

    public void refreshQR() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.qr.refresh", null, null);
    }

    /**
     * 获取当前车载微信的登录情况
     */
    public boolean isLogin() {
        try {
            ServiceData data = ServiceManager.getInstance().sendInvokeSync(
                    ServiceManager.WEBCHAT, "wechat.status.isLogin", null);
            return data.getBoolean();
        } catch (Exception e) {
            LogUtil.logd("wechat sdk invoke failed, cause=" + e.getMessage());
        }
        return false;
    }

    private Map<String, ImageListener> mImageListenerMap = new HashMap<String, TXZWechatManager.ImageListener>();

    // sdk
    public static interface ImageListener {
        void onImageReady(String id, String imgPath);
    }

    public void getUsericon(String id, ImageListener listener) {
        File head = new File(Environment.getExternalStorageDirectory()
                + "/txz/webchat/cache/Head/" + id);

        if (head.exists()) {
            listener.onImageReady(id, head.getPath());
        } else {
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                    "wechat.ctrl.loadImage", id.getBytes(), null);
            mImageListenerMap.put(id, listener);
        }

    }

    /**
     * 设置屏蔽群消息
     *
     * @param enable
     */
    public void setFilterGroupMessage(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.filter.groupmsg", ("" + enable).getBytes(), null);
    }

    /**
     * 设置屏蔽群联系人（开启后微信联系人选择中不会出现群联系人）
     *
     * @param enable
     */
    public void setFilterGroupContact(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.filter.groupcon", ("" + enable).getBytes(), null);
    }

    /**
     * 设置微信扫码登录语音提示的文本
     *
     * @param text
     * @deprecated 转用setVoiceText("WECHAT_TIP_NEED_LOGIN", "xxx")
     */
    @Deprecated
    public void setLoginTipText(String text) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.ctrl.tip.login", text.getBytes(), null);
    }

    /**
     * 设置语音提示的文字内容、
     *
     * @param key   提示key
     * @param value 文字内容，传null恢复默认
     */
    public void setVoiceText(String key, String value) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("key", key);
        builder.put("value", value);

        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.set.voice_tip", builder.toBytes(), null);
    }

    /**
     * 设置微信指定tip的文本
     *
     * @param key   提示key
     * @param value 文字内容，传null恢复默认
     */
    public void setWechatTipText(String key, String value) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("key", key);
        builder.put("value", value);

        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.set.tip", builder.toBytes(), null);
    }

    /**
     * 微信工具
     */
    public static interface WechatTool {

        /**
         * 打开微信界面
         */
        void launch();

        /**
         * 显示二维码
         *
         * @param url 二维码图片
         */
        void showQR(String url);

        /**
         * 更新二维码
         *
         * @param url 二维码图片
         */
        void updateQR(String url);

        /**
         * 二维码被扫描
         *
         * @param userIcon Base64编码的用户头像
         */
        void QRScanned(String userIcon);

        /**
         * 确认登录
         */
        void login();

        /**
         * 更新当前登录用户信息
         *
         * @param id   用户id
         * @param nick 昵称
         */
        void updateSelf(String id, String nick);

        /**
         * 退出登录
         */
        void logout();

        /**
         * 更新新消息提示窗
         *
         * @param msgId      消息id
         * @param senderId   消息发送者id
         * @param isGroup    是否来自群聊天
         * @param senderNick 消息发送者的昵称
         * @param casting    是否正在播报
         */
        void updateNotify(String msgId, String senderId, String senderNick,
                          boolean isGroup, boolean casting);

        /**
         * 关闭新消息提示
         */
        void dismissNotify();

        /**
         * 更新录音窗口状态
         *
         * @param timeRemain 距离自动发送剩余秒数，<1时为发送中
         * @param id         发送目标的id
         * @param nick       发送目标的昵称
         */
        void updateRecordWin(int timeRemain, String id, String nick);

        /**
         * 关闭录音窗口
         */
        void dismissRecordWin(boolean isSuccess);

        /**
         * 显示聊天界面
         */
        void showChat(String contactId, String contactName,
                      List<WechatMessage> msgList);

        /**
         * 更新自动播报状态
         *
         * @param enabled 开启状态
         */
        void updateNotifyStatus(boolean enabled);
    }

    public static interface NotificationTool {
        /**
         * 更新新消息提示窗
         *
         * @param msgId      消息id
         * @param senderId   消息发送者id
         * @param isGroup    是否来自群聊天
         * @param senderNick 消息发送者的昵称
         * @param casting    是否正在播报
         */
        void updateNotify(String msgId, String senderId, String senderNick, boolean isGroup, boolean casting);

        /**
         * 关闭新消息提示
         */
        void dismissNotify();
    }

    public void setNotificationTool(final NotificationTool tool) {
        mNotificationTool = tool;

        if (null == tool) {
            mHasSetNotificationTool = false;
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "txz.webchat.ntool.clear", null, null);
            return;
        }
        mHasSetNotificationTool = true;

        setWechatTool(null);
        TXZService.setCommandProcessor("tool.wechat.", new CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                if (command.equals("notify.show")) {
                    JSONBuilder builder = new JSONBuilder(data);
                    String msgId = builder.getVal("msgId", String.class);
                    String senderId = builder.getVal("id", String.class);
                    boolean casting = builder.getVal("hasSpeak", boolean.class);
                    boolean isGroup = builder.getVal("isGroup", boolean.class);
                    String senderNick = builder.getVal("nick", String.class);
                    tool.updateNotify(msgId, senderId, senderNick, isGroup, casting);
                } else if (command.equals("notify.cancel")) {
                    tool.dismissNotify();
                }
                return null;
            }
        });
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "txz.webchat.ntool.set", null, null);
    }

    public void setWechatTool(final WechatTool tool) {
        setWechatTool(tool, true);
    }

    public void setWechatTool(final WechatTool tool, boolean blockUI) {
        mWechatTool = tool;

        if (null == tool) {
            mHasSetWechatTool = false;
            bBlockUI = false;
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                    "txz.webchat.tool.clear", null, null);
            return;
        }
        mHasSetWechatTool = true;
        bBlockUI = blockUI;
        setNotificationTool(null);
        TXZService.setCommandProcessor("tool.wechat.", new CommandProcessor() {

            @Override
            public byte[] process(String packageName, String command,
                                  byte[] data) {
                LogUtil.logd("wxsdk::on cmd: " + command);
                if (command.equals("launch")) {
                    tool.launch();
                } else if (command.equals("qr.show")) {
                    String str = new JSONBuilder(data).getVal("qrcode",
                            String.class);
                    tool.showQR(str);
                } else if (command.equals("qr.update")) {
                    String str = new JSONBuilder(data).getVal("qrcode",
                            String.class);
                    tool.updateQR(str);
                } else if (command.equals("qr.scanned")) {
                    String uIcon = new JSONBuilder(data).getVal("icon",
                            String.class);
                    tool.QRScanned(uIcon);
                } else if (command.equals("login")) {
                    tool.login();
                } else if (command.equals("update.self")) {
                    JSONBuilder builder = new JSONBuilder(data);
                    String id = builder.getVal("id", String.class);
                    String nick = builder.getVal("nick", String.class);
                    tool.updateSelf(id, nick);
                } else if (command.equals("logout")) {
                    tool.logout();
                } else if (command.equals("record.update")) {
                    JSONBuilder builder = new JSONBuilder(data);
                    int timeRemain = builder.getVal("time", Integer.class);
                    String id = builder.getVal("id", String.class);
                    String nick = builder.getVal("nick", String.class);

                    tool.updateRecordWin(timeRemain, id, nick);
                } else if (command.equals("record.dismiss")) {
                    Boolean isErr = Boolean.parseBoolean(new String(data));
                    tool.dismissRecordWin(isErr);
                } else if (command.equals("notify.status")) {
                    boolean enabled = new JSONBuilder(data).getVal("enabled",
                            Boolean.class);
                    tool.updateNotifyStatus(enabled);
                } else if (command.equals("chat.show")) {
                    JSONBuilder builder = new JSONBuilder(data);
                    Log.d("demo", builder.toString());
                    String contactId = builder
                            .getVal("contactId", String.class);
                    String contactNick = builder.getVal("contactNick",
                            String.class);
                    String msgArr = builder.getVal("message", String.class);
                    List<WechatMessage> msgList = JSON.parseArray(msgArr,
                            WechatMessage.class);
                    tool.showChat(contactId, contactNick, msgList);
                } else if (command.equals("notify.show")) {
                    JSONBuilder builder = new JSONBuilder(data);
                    String msgId = builder.getVal("msgId", String.class);
                    String senderId = builder.getVal("id", String.class);
                    boolean casting = builder.getVal("hasSpeak", boolean.class);
                    boolean isGroup = builder.getVal("isGroup", boolean.class);
                    String senderNick = builder.getVal("nick", String.class);
                    tool.updateNotify(msgId, senderId, senderNick, isGroup, casting);
                } else if (command.equals("notify.cancel")) {
                    tool.dismissNotify();
                }
                return null;
            }
        });

        JSONBuilder builder = new JSONBuilder();
        builder.put("blockUI", blockUI);
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "txz.webchat.tool.set", builder.toBytes(), null);
    }
}
