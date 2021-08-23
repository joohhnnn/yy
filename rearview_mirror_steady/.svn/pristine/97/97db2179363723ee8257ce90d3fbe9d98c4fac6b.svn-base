package com.txznet.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.bean.WechatContactV2;
import com.txznet.sdk.wechat.ITXZWechatTool;
import com.txznet.sdk.wechat.InvokeConstants;

import java.util.HashMap;
import java.util.List;

/**
 * 微信SDK Manager V2
 *
 * Created by J on 2018/4/8.
 */

public class TXZWechatManagerV2 {
    private static final String LOG_TAG = "TXZWechatManagerV2";

    private static final String ACTION_IMAGE_READY =
            "com.txznet.webchat.action.SDK_DOWNLOAD_IMG_COMPLETE";

    private ITXZWechatTool mWechatTool;
    private boolean bBlockWechatUI;
    private Boolean bEnableNotification;
    private Boolean bEnableRecordWin;

    private HashMap<String, ImageListener> mImageListenerMap = new HashMap<String, ImageListener>();

    private BroadcastReceiver mImageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra("id");
            String img = intent.getStringExtra("img");

            if (!StringUtils.isEmpty(id)) {
                ImageListener listener = mImageListenerMap.remove(id);
                if (null != listener) {
                    listener.onImageReady(id, img);
                }
            }
        }
    };

    public void onReconnectTXZ() {
        if (null != mWechatTool) {
            setWechatTool(mWechatTool, bBlockWechatUI);
        }

        if (null != bEnableNotification) {
            setNotificationEnabled(bEnableNotification);
        }

        if (null != bEnableRecordWin) {
            setRecordWindowEnabled(bEnableRecordWin);
        }
    }

    /**
     * 获取当前车载微信的登录情况
     */
    public boolean getLoginStatus() {
        return sendSdkInvokeSync(InvokeConstants.SDK_CMD_GET_LOGIN_STATUS, null, false);
    }

    /**
     * 刷新登录二维码
     */
    public void refreshQR() {
        sendSdkInvoke(InvokeConstants.SDK_CMD_REFRESH_QR, null);
    }

    /**
     * 请求用户头像
     *
     * @param id       用户id
     * @param listener 头像下载成功后会通过此listener
     */
    public void getUsericon(String id, ImageListener listener) {
        mImageListenerMap.put(id, listener);

        Parcel p = Parcel.obtain();
        p.writeString(id);
        sendSdkInvoke(InvokeConstants.SDK_CMD_DOWNLOAD_AVATAR, p.marshall());
        p.recycle();
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
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.cancelRecord",
                null, null);
    }

    /**
     * 结束录音并发送
     */
    public void finishRecord() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.finishRecord",
                null, null);
    }

    /**
     * 撤回最后一条发送的消息
     */
    public void revokeLastMessage() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.revokeLastMsg",
                null, null);
    }

    /**
     * 设置是否启用导航消息处理(未启用导航消息处理的情况下, 收到导航消息按普通消息处理, 不提示导航过去, 也不会
     * 响应用户说的导航过去指令)
     *
     * @param enable
     */
    public void setLocMsgEnabled(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.setting.enableLocMsg", ("" + enable).getBytes(), null);
    }

    /**
     * 设置是否启用位置分享功能(位置分享关闭状态下不支持"分享当前位置"的语音指令, 声控界面进行位置分享会提示"当
     * 前设备不支持位置分享功能")
     *
     * @param enable
     */
    public void setLocShareEnabled(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                "wechat.setting.enableLocShare", ("" + enable).getBytes(), null);
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
     * 设置语音提示的文字内容、
     *
     * @param key   提示key
     * @param value 文字内容，传null恢复默认
     */
    public void setVoiceText(String key, String value) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("key", key);
        builder.put("value", value);

        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.set" +
                ".voice_tip", builder.toBytes(), null);
    }

    /**
     * 请求同步联系人列表
     *
     * 调用此方法后, 会通过WechatTool异步返回车载微信联系人列表中指定数目的前 {@param count} 个联系人,
     * 见 {@link com.txznet.sdk.wechat.AbsTXZWechatTool#updateContactList(List)}
     *
     * @param count 需要同步的联系人数目, 最多10个
     */
    public void syncContactList(int count) {
        Parcel p = Parcel.obtain();
        p.writeInt(count);

        sendSdkInvoke(InvokeConstants.SDK_CMD_SYNC_CONTACT, p.marshall());
        p.recycle();
    }

    /**
     * 请求同步消息列表
     *
     * 调用此方法后, 会通过WechatTool异步返回车载微信对应会话中的倒数 {@param count} 条消息,
     * 见 {@link com.txznet.sdk.wechat.AbsTXZWechatTool#updateMessageList(WechatContactV2, List)}
     *
     * @param sessionId 对应的会话id
     * @param count     最大消息数目, 传0同步全部
     */
    public void syncMessageList(String sessionId, int count) {
        Parcel p = Parcel.obtain();
        p.writeString(sessionId);
        p.writeInt(count);

        sendSdkInvoke(InvokeConstants.SDK_CMD_SYNC_MESSAGE, p.marshall());
        p.recycle();
    }

    /**
     * 设置WechatTool
     *
     * 设置WechatTool后, 车载微信相关状态会回调到 tool 相关方法
     *
     * @param tool          WechatTool
     * @param blockWechatUI 是否屏蔽微信界面, 传true后微信界面不会再显示
     * @see com.txznet.sdk.wechat.AbsTXZWechatTool
     */
    public void setWechatTool(ITXZWechatTool tool, boolean blockWechatUI) {
        mWechatTool = tool;
        bBlockWechatUI = blockWechatUI;

        TXZService.setCommandProcessor(InvokeConstants.WX_CMD_PREFIX,
                new TXZService.CommandProcessor() {
                    @Override
                    public byte[] process(final String packageName, final String command,
                                          final byte[] data) {
                        return mWechatTool.procSdkInvoke(packageName, command, data);
                    }
                });

        Parcel p = Parcel.obtain();
        p.writeInt(blockWechatUI ? 1 : 0);
        /*
        * 写入sdk版本号
        *
        * 因为需要做版本兼容, 所以涉及到sdk 通信字段发生变动时, 如果无法做到与旧版本直接兼容, 需要
        * 提高sdk版本号, 由微信端做兼容处理
        *
        * 因为微信协议的特殊性, 默认与sdk配合使用的都是当前最新版本的微信, 不再考虑sdk对旧版本微信的兼容性
        *
        * 修改记录:
        * (1): 初始版本, 初始版本未包含此字段
        * 2: setWechatTool调用中增加版本信息(此字段), 增加屏蔽状态变化回调, dismissRecordWin增加字段
        *    用于标识是否是主动取消的发送, 新增发送消息开始和结束的状态回调
        * */
        p.writeInt(tool.getSdkVersion());
        sendSdkInvoke(InvokeConstants.SDK_CMD_SET_TOOL, p.marshall());
        p.recycle();
    }

    /**
     * 清除WechatTool
     */
    public void clearWechatTool() {
        mWechatTool = null;

        sendSdkInvoke(InvokeConstants.SDK_CMD_CLEAR_TOOL, null);
    }

    /**
     * 设置是否显示微信原生的消息提示界面
     *
     * @param enabled 传true显示, 传false屏蔽
     */
    public void setNotificationEnabled(boolean enabled) {
        bEnableNotification = enabled;
        Parcel p = Parcel.obtain();
        p.writeInt(enabled ? 1 : 0);

        sendSdkInvoke(InvokeConstants.SDK_CMD_SET_NOTIFICATION_ENABLED, p.marshall());
        p.recycle();
    }

    /**
     * 设置是否显示微信原生的录音界面
     *
     * @param enabled 传true显示, 传false屏蔽
     */
    public void setRecordWindowEnabled(boolean enabled) {
        bEnableRecordWin = enabled;
        Parcel p = Parcel.obtain();
        p.writeInt(enabled ? 1 : 0);

        sendSdkInvoke(InvokeConstants.SDK_CMD_SET_RECORD_WINDOW_ENABLED, p.marshall());
        p.recycle();
    }

    private void sendSdkInvoke(String cmd, byte[] data) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
                InvokeConstants.WX_INVOKE_PREFIX + cmd, data, null);
    }

    private boolean sendSdkInvokeSync(String cmd, byte[] data, boolean defValue) {
        ServiceManager.ServiceData ret = ServiceManager.getInstance().sendInvokeSync(
                ServiceManager.WEBCHAT, InvokeConstants.WX_INVOKE_PREFIX + cmd, data);

        if (null == ret) {
            return defValue;
        }

        return (null == ret.getBoolean()) ? defValue : ret.getBoolean();
    }

    //----------- single instance -----------
    private static volatile TXZWechatManagerV2 sInstance;

    public static TXZWechatManagerV2 getInstance() {
        if (null == sInstance) {
            synchronized (TXZWechatManagerV2.class) {
                if (null == sInstance) {
                    sInstance = new TXZWechatManagerV2();
                }
            }
        }

        return sInstance;
    }

    private TXZWechatManagerV2() {
        // 注册下载图片完成的BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_IMAGE_READY);
        GlobalContext.get().registerReceiver(mImageReceiver, filter);
    }
    //----------- single instance -----------

    /**
     * 用于用户头像加载的listener
     */
    public interface ImageListener {
        void onImageReady(String id, String imgPath);
    }
}
