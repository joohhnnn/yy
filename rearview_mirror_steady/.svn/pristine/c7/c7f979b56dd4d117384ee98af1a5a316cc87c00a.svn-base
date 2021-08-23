package com.txznet.webchat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.txz.service.IService;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.sp.PowerSp;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.ui.base.UIHandler;


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
            } else if (command.equals("wx.client.sleep")) {
                L.i("powerAction", "doSleep");
            } else if (command.equals("wx.client.wakeup")) {
                L.i("powerAction", "wake action triggered, save status");
                PowerSp.getInstance(GlobalContext.get()).setWakeActionTriggered(true);
            } else if (command.equals("sdk.init.success")) {
                L.i("powerAction", "sdk.init.success");
            } else if (command.equalsIgnoreCase("wx.contact.need.login")) { // 打开微信二维码登录页面
                if ((null != data) && Boolean.parseBoolean(new String(data))) {
                    UIHandler.getInstance().showAppStart(true);
                } else {
                    UIHandler.getInstance().showAppStart(false);
                }
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
        } else if (command.equals("exit")) {
            ActivityStack.getInstance().exit();
        }
    }

    private void invokeWechatSetting(String pkgName, String command, byte[] data) {
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
                if (((uint64Flag & 0x1) == 1)) {
                    AppStatusActionCreator.get().getWxServerConfig();
                }
                if (((uint64Flag & (0x1 << 5)) >> 5) == 1) {
                    AppStatusActionCreator.get().enableWxEntry();
                } else {
                    AppStatusActionCreator.get().disableWxEntry();
                }
                if (((uint64Flag & (0x1 << 11)) >> 11) == 0) {
                    AppStatusActionCreator.get().enableControlEntry();
                } else {
                    AppStatusActionCreator.get().disableControlEntry();
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
