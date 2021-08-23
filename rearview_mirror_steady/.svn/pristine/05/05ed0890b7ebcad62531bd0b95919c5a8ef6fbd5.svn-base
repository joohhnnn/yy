package com.txznet.sdk.wechat;

import android.os.Parcel;

import com.txznet.sdk.TXZWechatManagerV2;
import com.txznet.sdk.bean.WechatContactV2;
import com.txznet.sdk.bean.WechatMessageV2;

import java.util.ArrayList;
import java.util.List;

/**
 * 同行者微信工具
 * Created by J on 2018/8/14.
 */

public abstract class AbsTXZWechatTool implements ITXZWechatTool {
    /**
     * 打开微信界面
     *
     * @param loginUser 登录用户信息(若未登录为null)
     * @param qr        登录二维码对应的url(若已登录为"")
     */
    public abstract void launch(WechatContactV2 loginUser, String qr);

    /**
     * 显示二维码
     *
     * @param qr 二维码图片对应的url
     */
    public abstract void updateQR(String qr);

    /**
     * 二维码被扫描
     *
     * @param userIcon 用户头像
     */
    public abstract void QRScanned(String userIcon);

    /**
     * 确认登录
     */
    public abstract void login();

    /**
     * 更新当前登录用户信息
     *
     * @param user 当前登录的用户
     */
    public abstract void updateLoginUser(WechatContactV2 user);

    /**
     * 退出登录
     */
    public abstract void logout();

    /**
     * 更新新消息提示窗
     *
     * @param message 对应的消息
     * @param casting 是否正在播报
     */
    public abstract void updateNotify(WechatMessageV2 message, boolean casting);

    /**
     * 关闭新消息提示
     */
    public abstract void dismissNotify();

    /**
     * 更新录音窗口状态
     *
     * @param timeRemain 距离自动发送剩余秒数，<1时为发送中
     * @param targetUser 发送的目标联系人
     */
    public abstract void updateRecordWin(int timeRemain, WechatContactV2 targetUser);

    /**
     * 关闭录音窗口
     *
     * @param isSuccess   录音是否发送成功
     * @param isCancelled 录音是否被取消
     */
    public abstract void dismissRecordWin(boolean isSuccess, boolean isCancelled);

    /**
     * 显示聊天界面
     *
     * @param session 对应的会话信息
     * @param msgList 消息列表
     */
    public abstract void showChat(WechatContactV2 session,
                                     List<WechatMessageV2> msgList);

    /**
     * 更新自动播报状态
     *
     * @param enabled 开启状态
     */
    public abstract void updateNotifyStatus(boolean enabled);

    // ----------- 异步消息同步接口 -----------

    /**
     * 同步联系人列表
     *
     * 此回调是对 {@link TXZWechatManagerV2#syncContactList(int)} 方法的响应, 其他
     * 情况下不会主动回调此方法
     *
     * NOTE: 返回的list的大小可能小于请求数目
     *
     * @param list 联系人列表, 若调用时微信未登录, 会返回空列表(size == 0)
     */
    public abstract void updateContactList(List<WechatContactV2> list);

    /**
     * 同步消息列表
     *
     * 此回调是对 {@link TXZWechatManagerV2#syncMessageList(String, int) } 方法的响应, 其他
     * 情况下不会主动回调此方法
     *
     * NOTE: 返回的list的大小可能小于请求数目
     *
     * @param session 对应的会话信息
     * @param msgList 消息列表
     */
    public abstract void updateMessageList(WechatContactV2 session,
                                              List<WechatMessageV2> msgList);

    /**
     * 修改联系人
     *
     * 联系人昵称变动/屏蔽状态变动等情况下会主动回调此接口, 将更新后的联系人信息重新进行通知
     *
     * @param contact 新的联系人信息
     */
    public abstract void modContact(WechatContactV2 contact);

    /**
     * 删除联系人
     *
     * 联系人被删除时会主动调用此接口, 通知被删除的联系人id
     *
     * @param id 被删除的联系人id
     */
    public abstract void delContact(String id);

    /**
     * 开始发送消息
     *
     * @param message 发送的消息
     */
    public void onSendMessageStart(WechatMessageV2 message) {
        // 新增接口默认空实现
    }

    /**
     * 结束发送消息
     *
     * @param message 发送的消息
     * @param success 是否发送成功
     */
    public void onSendMessageResult(WechatMessageV2 message, boolean success) {
        // 新增接口默认空实现
    }


    @Override
    public final int getSdkVersion() {
        return 2;
    }

    @Override
    public final byte[] procSdkInvoke(final String packageName, final String command,
                                      final byte[] data) {
        Parcel p = Parcel.obtain();
        if (null != data) {
            p.unmarshall(data, 0, data.length);
            p.setDataPosition(0);
        }

        if (InvokeConstants.WX_CMD_LAUNCH.equals(command)) {
            String qr = p.readString();
            launch(null, qr);
        } else if (InvokeConstants.WX_CMD_LAUNCH_WHEN_LOGGEDIN.equals(command)) {
            WechatContactV2 loginUser = WechatContactV2.CREATOR.createFromParcel(p);
            launch(loginUser, "");
        } else if (InvokeConstants.WX_CMD_QR_UPDATE.equals(command)) {
            String qrCode = p.readString();
            updateQR(qrCode);
        } else if (InvokeConstants.WX_CMD_QR_SCAN.equals(command)) {
            String avatar = p.readString();
            QRScanned(avatar);
        } else if (InvokeConstants.WX_CMD_LOGIN.equals(command)) {
            login();
        } else if (InvokeConstants.WX_CMD_LOGOUT.equals(command)) {
            logout();
        } else if (InvokeConstants.WX_CMD_UPDATE_USER.equals(command)) {
            WechatContactV2 user = WechatContactV2.CREATOR.createFromParcel(p);
            updateLoginUser(user);
        } else if (InvokeConstants.WX_CMD_SHOW_CHAT.equals(command)) {
            WechatContactV2 session = WechatContactV2.CREATOR.createFromParcel(p);
            List<WechatMessageV2> msgList = new ArrayList<WechatMessageV2>();
            p.readTypedList(msgList, WechatMessageV2.CREATOR);
            showChat(session, msgList);
        } else if (InvokeConstants.WX_CMD_RECORD_UPDATE.equals(command)) {
            int timeRemain = p.readInt();
            WechatContactV2 targetSession = WechatContactV2.CREATOR.createFromParcel(p);
            updateRecordWin(timeRemain, targetSession);
        } else if (InvokeConstants.WX_CMD_RECORD_DISMISS.equals(command)) {
            boolean isSuccess = (1 == p.readInt());
            boolean isCancelled = (1 == p.readInt());
            dismissRecordWin(isSuccess, isCancelled);
        } else if (InvokeConstants.WX_CMD_NOTIFICATION_UPDATE.equals(command)) {
            WechatMessageV2 msg = WechatMessageV2.CREATOR.createFromParcel(p);
            boolean isCasting = (1 == p.readInt());
            updateNotify(msg, isCasting);
        } else if (InvokeConstants.WX_CMD_NOTIFICATION_DISMISS.equals(command)) {
            dismissNotify();
        } else if (InvokeConstants.WX_CMD_SYNC_CONTACT.equals(command)) {
            List<WechatContactV2> list = new ArrayList<WechatContactV2>();
            p.readTypedList(list, WechatContactV2.CREATOR);
            updateContactList(list);
        } else if (InvokeConstants.WX_CMD_SYNC_MESSAGE.equals(command)) {
            WechatContactV2 syncSession = WechatContactV2.CREATOR.createFromParcel(p);
            List<WechatMessageV2> syncMsgList = new ArrayList<WechatMessageV2>();
            p.readTypedList(syncMsgList, WechatMessageV2.CREATOR);
            updateMessageList(syncSession, syncMsgList);
        } else if (InvokeConstants.WX_CMD_MSG_BROADCAST_ENABLED.equals(command)) {
            boolean enabled = (1 == p.readInt());
            updateNotifyStatus(enabled);
        } else if (InvokeConstants.WX_CMD_MOD_CONTACT.equals(command)) {
            WechatContactV2 targetSession = WechatContactV2.CREATOR.createFromParcel(p);
            modContact(targetSession);
        } else if (InvokeConstants.WX_CMD_DEL_CONTACT.equals(command)) {
            String id = p.readString();
            delContact(id);
        } else if (InvokeConstants.WX_CMD_SEND_MSG_BEGIN.equals(command)) {
            WechatMessageV2 msg = WechatMessageV2.CREATOR.createFromParcel(p);
            onSendMessageStart(msg);
        } else if (InvokeConstants.WX_CMD_SEND_MSG_RESULT.equals(command)) {
            WechatMessageV2 msg = WechatMessageV2.CREATOR.createFromParcel(p);
            boolean success = (p.readInt() != 0);
            onSendMessageResult(msg, success);
        }

        p.recycle();
        return null;
    }
}
