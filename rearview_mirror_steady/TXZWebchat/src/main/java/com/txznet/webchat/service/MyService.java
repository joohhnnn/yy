package com.txznet.webchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.txz.service.IService;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.actions.UploadVoiceActionCreator;
import com.txznet.webchat.helper.TXZCmdHelper;
import com.txznet.webchat.helper.TXZSyncHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.sp.PowerSp;
import com.txznet.webchat.sp.TipManager;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.stores.TXZRecordStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.util.ContactEncryptUtil;


public class MyService extends Service {
    private static final String TAG = "Webchat:";

    public class WebchatBinder extends IService.Stub {
        @Override
        public byte[] sendInvoke(String packageName, final String command, byte[] data)
                throws RemoteException {
            try {
                return _sendInvoke(packageName, command, data);
            } catch (Exception e) {
                CrashCommonHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }

            return null;
        }

        public byte[] _sendInvoke(String packageName, final String command, byte[] data)
                throws RemoteException {
            byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
            if (command.startsWith("wechat.ctrl.")) {
                invokeWechatCtrl(packageName, command.substring("wechat.ctrl.".length()), data);
            } else if (command.startsWith("wechat.setting.")) {
                invokeWechatSetting(packageName, command.substring("wechat.setting.".length()),
                        data);
            } else if (command.startsWith(InvokeConstants.WX_INVOKE_PREFIX)) {
                ret = invokeWechatSDK(packageName, command.substring(
                        InvokeConstants.WX_INVOKE_PREFIX.length()), data);
            } else if (command.equals("wx.info.qrcode")) {
                refreshBindInfo(packageName, command, data);
            } else if (command.equals("wx.info.nick")) {
                TXZBindActionCreator.get().updateBindInfo(new String(data));
            } else if (command.equals("wx.upload.voice.success")) {
                UploadVoiceActionCreator.get().notifyUploadVoiceSucc(new String(data), 0);
            } else if (command.equals("wx.upload.voice.error")) {
                UploadVoiceActionCreator.get().notifyUploadVoiceError(new String(data));
            } else if (command.equals("wx.session.expression")) { //发送表情
                L.d(TAG + "session expression");
                if (WxLoginStore.get().isLogin()) {
                    JSONBuilder doc = new JSONBuilder(data);
                    String id = doc.getVal("id", String.class);
                    String expression = doc.getVal("data", String.class);
                    L.d(TAG + "expression::" + expression + " id::" + ContactEncryptUtil.decrypt
                            (id));
                    if (id != null) {
                        MessageActionCreator.get().sendTextMsg(ContactEncryptUtil.decrypt(id),
                                expression);
                    }
                } else {
                    L.e(TAG + "logout");
                }
            } else if (command.equals("wx.session.make")) {
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_SEND);
                // 回复微信
                if (WxLoginStore.get().isLogin()) {
                    JSONBuilder doc = new JSONBuilder(data);
                    String id = doc.getVal("id", String.class);
                    if (id != null) {
                        MessageActionCreator.get().replyVoice(ContactEncryptUtil.decrypt(id),
                                false);
                    }
                }
            } else if (command.equals("wx.session.open")) { // 用户点击微信新消息播报
                MessageActionCreator.get().cancelNotify(); // 取消
                // 打开微信会话
                if (WxLoginStore.get().isLogin()) {
                    JSONBuilder doc = new JSONBuilder(data);
                    String id = doc.getVal("id", String.class);
                    if (!StringUtils.isEmpty(id)) {
                        UIHandler.getInstance().showChat(ContactEncryptUtil.decrypt(id), false);
                    }
                }
            } else if (command.equals("wx.session.history")) { // 查看微信历史消息
                TXZReportActionCreator.getInstance().report(ReportMessage
                        .REPORT_VOICE_OPEN_HISTORY);
                if (WxLoginStore.get().isLogin()) {
                    JSONBuilder doc = new JSONBuilder(data);
                    String id = doc.getVal("id", String.class);
                    if (!StringUtils.isEmpty(id)) {
                        UIHandler.getInstance().showChat(ContactEncryptUtil.decrypt(id), true);
                    }
                }
            } else if (command.equals("wx.msg.repeat")) {
                if (WxLoginStore.get().isLogin()) {
                    try {
                        JSONBuilder doc = new JSONBuilder(data);
                        String openid = doc.getVal("id", String.class);
                        long msgId = Long.parseLong(doc.getVal("msgId", String.class));
                        MessageActionCreator.get().repeatMessage(ContactEncryptUtil.decrypt
                                (openid), msgId);
                    } catch (Exception e) {
                    }
                }
            } else if (command.equals("wx.contact.recentsession.request")) {
                TXZSyncHelper.getInstance().notifyUpdateRecentSession();
            } else if (command.equals("wx.contact.maskedsession.request")) {
                TXZSyncHelper.getInstance().notifyUpdateBlockedSession();
            } else if (command.equals("wx.contact.session_for_mask.request")) {
                // 请求用于屏蔽的联系人列表（不会包含已屏蔽的联系人）
                TXZSyncHelper.getInstance().notifyUpdateSessionForBlock();
            } else if (command.equals("wx.client.exit")) {
                TXZReportActionCreator.getInstance().reportLogout(true);
                if (data != null) {
                    boolean doLogout = Boolean.parseBoolean(new String(data));
                    LoginActionCreator.get().doLogout(doLogout);
                } else {
                    LoginActionCreator.get().doLogout(true);
                }
            } else if (command.equals("wx.client.sleep")) {
                L.i("powerAction", "doSleep");
                LoginActionCreator.get().doSleep();
            } else if (command.equals("wx.client.wakeup")) {
                L.i("powerAction", "wake action triggered, save status");
                PowerSp.getInstance(GlobalContext.get()).setWakeActionTriggered(true);
                LoginActionCreator.get().doWakeup();
            } else if (command.equals("sdk.init.success")) {
                L.i("powerAction", "sdk.init.success");
                TXZSyncHelper.getInstance().syncContact();
                TXZCmdHelper.getInstance().registerWxCmd();
                AppStatusActionCreator.get().notifyTXZReStart();
            } else if ("wx.client.enter_reverse".equals(command)) {
                AppStatusActionCreator.get().enterReverse();
                L.i("powerAction", "enterReverse");
            } else if ("wx.client.quit_reverse".equals(command)) {
                AppStatusActionCreator.get().exitReverse();
                L.i("powerAction", "exitReverse");
            } else if (command.equals("wx.session.mask")) {
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_SHIELD);
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (id != null) {
                    ContactActionCreator.get().filterSpeak(ContactEncryptUtil.decrypt(id), false);
                }
            } else if (command.equals("wx.session.unmask")) { // 解除消息屏蔽
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (id != null) {
                    ContactActionCreator.get().unfilterSpeak(ContactEncryptUtil.decrypt(id), false);
                }
            } else if (command.equals("wx.session.sharePlace")) {
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (id != null) {
                    MessageActionCreator.get().sendLocationMsg(ContactEncryptUtil.decrypt(id));
                }
            } else if (command.equals("wx.session.sharePhoto")) {
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (id != null) {
                    String url = doc.getVal("data", String.class);
                    MessageActionCreator.get().sendImgMsg(ContactEncryptUtil.decrypt(id), url);
                }
            } else if ("wx.session.share_poi".equals(command)) {
                L.d("testApi", "start sharePoi");
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (!TextUtils.isEmpty(id)) {
                    double lat = doc.getVal("lat", double.class);
                    double lng = doc.getVal("lng", double.class);
                    String addr = doc.getVal("addr", String.class);
                    L.d("testApi", String.format("start sharePoi(%s, %s, %s)", lat, lng, addr));
                    MessageActionCreator.get().sendLocationMsg(ContactEncryptUtil.decrypt(id),
                            lat, lng, addr);
                }
            } else if (command.equalsIgnoreCase("wx.contact.need.login")) { // 打开微信二维码登录页面
                if ((null != data) && Boolean.parseBoolean(new String(data))) {
                    UIHandler.getInstance().showAppStart(true);
                } else {
                    UIHandler.getInstance().showAppStart(false);
                }

            } else if (command.equals("wx.state.login.req")) {
                TXZSyncHelper.getInstance().reportLoginStatus();
            } else if (command.equals("wx.session.refresh")) {
                JSONBuilder doc = new JSONBuilder(data);
                String id = doc.getVal("id", String.class);
                if (id != null) {
                    WxSDKManager.getInstance().loadAvatar(id);
                }
            } else if (command.equals("wechat.status.isLogin")) {
                ret = ("" + WxLoginStore.get().isLogin()).getBytes();
            } else if (command.equals("txz.webchat.tool.clear")) {
                WxSDKManager.getInstance().clearRemotePackageName();
                UIHandler.getInstance().setUIEnabled(true);
                UIHandler.getInstance().setNotificationEnabled(true);
                L.d("wxsdk :: tool clear");
            } else if (command.equals("txz.webchat.tool.set")) {
                WxSDKManager.getInstance().setRemotePackageName(packageName, 1);
                if (null == data) {
                    UIHandler.getInstance().setUIEnabled(false);
                    UIHandler.getInstance().setNotificationEnabled(false);
                } else {
                    JSONBuilder doc = new JSONBuilder(data);
                    boolean blockUI = doc.getVal("blockUI", boolean.class);
                    UIHandler.getInstance().setUIEnabled(!blockUI);
                    UIHandler.getInstance().setUIEnabled(!blockUI);
                }
                L.d("wxsdk :: tool set, pkg = " + packageName);
            } else if (command.equals("txz.webchat.ntool.set")) {
                WxSDKManager.getInstance().setRemotePackageName(packageName, 1);
                UIHandler.getInstance().setUIEnabled(true);
                UIHandler.getInstance().setNotificationEnabled(false);
                L.d("wxsdk :: ntool set, pkg = " + packageName);
            } else if (command.equals("txz.webchat.ntool.clear")) {
                WxSDKManager.getInstance().clearRemotePackageName();
                UIHandler.getInstance().setUIEnabled(true);
                UIHandler.getInstance().setNotificationEnabled(true);
                L.d("wxsdk :: ntool clear");
            }
            return ret;
        }
    }

    private void invokeWechatCtrl(String pkgName, String command, byte[] data) {
        if (command.equals("launch")) { // "打开微信"
            UIHandler.getInstance().launchUI();
        } else if (command.equals("skipCurrentMessage")) {
            MessageActionCreator.get().skipCurrentMessage();
        } else if (command.equals("repeatLastMessage")) {
            MessageActionCreator.get().repeatLastMessage();
        } else if (command.equals("blockCurrentContact")) {
            ContactActionCreator.get().filterCurSpeak();
        } else if (command.equals("enableAutoSpeak")) {
            if (WxLoginStore.get().isLogin() && data != null) {
                boolean auto = Boolean.parseBoolean(new String(data));
                if (auto) {
                    AppStatusActionCreator.get().enableAutoSpeak();
                } else {
                    AppStatusActionCreator.get().disableAutoSpeak();
                }
            }
        } else if (command.equals("exit")) {
            ActivityStack.getInstance().exit();
            if (data != null) {
                boolean doLogout = Boolean.parseBoolean(new String(data));
                if (doLogout) {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            LoginActionCreator.get().doLogout(true);
                        }
                    }, 500);
                }
            }
        } else if (command.equals("cancelRecord")) {
            if (WxLoginStore.get().isLogin()) {
                MessageActionCreator.get().cancelReply(true);
            }
        } else if ("finishRecord".equals(command)) {
            if (TXZRecordStore.get().isReplying()) {
                MessageActionCreator.get().sendVoice(true);
            }
        } else if ("revokeLastMsg".equals(command)) {
            MessageActionCreator.get().revokeLastMsg();
        } else if ("loadAvatar".equals(command) || "loadImage".equals(command)) {
            String id = new String(data);
            WxSDKManager.getInstance().loadAvatar(id);
        } else if (command.equals("qr.refresh")) {
            LoginActionCreator.get().doWakeup();
            LoginActionCreator.get().refreshQRCode();
        } else if (command.equals("filter.groupmsg")) {
            boolean enable = Boolean.parseBoolean(new String(data));
            if (enable) {
                AppStatusActionCreator.get().disableGroupMsgSpeak();
            } else {
                AppStatusActionCreator.get().enableGroupMsgSpeak();
            }

        } else if (command.equals("filter.groupcon")) {
            boolean enable = Boolean.parseBoolean(new String(data));
            if (enable) {
                AppStatusActionCreator.get().disableGroupContact();
            } else {
                AppStatusActionCreator.get().enableGroupContact();
            }

        } else if (command.equals("tip.login")) {
            //Config.TIP_NEED_LOGIN = new String(data);
            TipManager.setTip(TipManager.KEY_TIP_NEED_LOGIN, new String(data));
        } else if (command.equals("repeat.send")) {
            JSONBuilder builder = new JSONBuilder(data);
            String tip = builder.getVal("tip", String.class);
            UIHandler.getInstance().repeatSendMessage(tip);
        } else if ("repeat.recent".equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String tip = builder.getVal("tip", String.class);
            UIHandler.getInstance().repeatSendMessageToRecent(tip);
        } else if ("reply.recent".equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String tip = builder.getVal("tip", String.class);
            /////UIHandler.replyToRecent(tip);
        } else if ("reply.current".equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String tip = builder.getVal("tip", String.class);
            UIHandler.getInstance().sendMessageToCurrentContact(tip);
        } else if (command.equals("enableWakupAsrCmd")) {
            boolean enable = Boolean.parseBoolean(new String(data));
            AppStatusActionCreator.get().enableWakeupAsrCmd(enable);
        } else if ("wakeupLogin".equals(command)) {
            boolean enable = Boolean.parseBoolean(new String(data));
            PowerSp.getInstance(GlobalContext.get()).setWakeupLoginEnabled(enable);
        } else if ("set.voice_tip".equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String key = builder.getVal("key", String.class);
            String val = builder.getVal("value", String.class);

            TipManager.setTip(key, val);
        } else if ("set.tip".equals(command)) {
            JSONBuilder builder = new JSONBuilder(data);
            String key = builder.getVal("key", String.class);
            String val = builder.getVal("value", String.class);

            TipManager.setTip(key, val);
        }
    }

    private void invokeWechatSetting(String pkgName, String command, byte[] data) {
        if ("enableLocMsg".equals(command)) {
            boolean enable = Boolean.parseBoolean(new String(data));
            if (enable) {
                AppStatusActionCreator.get().enableLocMsg();
            } else {
                AppStatusActionCreator.get().disableLocMsg();
            }
        } else if ("enableLocShare".equals(command)) {
            boolean enable = Boolean.parseBoolean(new String(data));
            if (enable) {
                AppStatusActionCreator.get().enableLocShare();
            } else {
                AppStatusActionCreator.get().disableLocShare();
            }
        }
    }

    private byte[] invokeWechatSDK(String pkgName, String command, byte[] data) {
       return WxSDKManager.getInstance().onSdkInvoke(pkgName, command, data);
    }

    private void refreshBindInfo(String packageName, String command, byte[] data) {
        JSONBuilder doc = new JSONBuilder(data);
        boolean issuccess = doc.getVal("issuccess", Boolean.class);
        if (issuccess) {
            boolean isbind = doc.getVal("isbind", Boolean.class);
            // 更新绑定状态
            String qrUrl = doc.getVal("qrcode", String.class);
            String nick = doc.getVal("nick", String.class);
            // 绑定信息发生改变
            if (isbind != WebChatSp.getInstance(GlobalContext.get()).isBinding(false)) {
                WebChatSp.getInstance(GlobalContext.get()).setBinding(isbind);
                if (AppLogic.isForeground()) {
                    //发送绑定信息改变广播
                    WxStatusHelper.getInstance().notifyGetBindMsgChanged(isbind, nick);
                }
            }
            TXZBindActionCreator.get().updateBindInfo(isbind, nick, qrUrl);
            Long uint64Flag = doc.getVal("uint64Flag", Long.class);
            if (uint64Flag != null) {
                if (((uint64Flag & (0x1 << 5)) >> 5) == 1) {
                    AppStatusActionCreator.get().enableWxEntry();
                } else {
                    AppStatusActionCreator.get().disableWxEntry();
                }
                L.d("WinControl::notifyResp, uint64Flag=" + uint64Flag);
            }
            // 发送广播通知
            WxStatusHelper.getInstance().notifyGetBindMsgSucc(isbind, nick);
        } else {
            TXZBindActionCreator.get().updateBindInfoException();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new WebchatBinder();
    }

}
