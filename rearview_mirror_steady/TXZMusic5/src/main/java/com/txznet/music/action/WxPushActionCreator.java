package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.QrCodeInfo;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

import java.util.List;

/**
 * @author telen
 * @date 2018/12/22,11:07
 */
public class WxPushActionCreator {


    /**
     * 单例对象
     */
    private volatile static WxPushActionCreator singleton;

    private WxPushActionCreator() {
    }

    public static WxPushActionCreator getInstance() {
        if (singleton == null) {
            synchronized (WxPushActionCreator.class) {
                if (singleton == null) {
                    singleton = new WxPushActionCreator();
                }
            }
        }
        return singleton;
    }

    /**
     * 获取微信推送消息
     */
    public void getWxPushData() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_WXPUSH_EVENT_GET).build());
    }

    /**
     * 获取车车互联二维码
     */
    public void getWxPushQRCode() {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_WXPUSH_EVENT_GET_QRCODE).build());
    }

    /**
     * 获取车车互联二维码
     */
    public void updateWxPushQRCode(QrCodeInfo qrCodeInfo) {
        if (qrCodeInfo.uint64Flag == 0
                && (qrCodeInfo.nick == null || "null".equals(qrCodeInfo.nick))
                && (qrCodeInfo.qrcode == null || "null".equals(qrCodeInfo.qrcode))
                && !qrCodeInfo.issuccess
                && !qrCodeInfo.isbind) {
            return;
        }
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO).bundle(Constant.WxPushConstant.KEY_QRCODE_INFO, qrCodeInfo).build());
    }

    public void saveWxPushData(List<PushItem> pushItems) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_WXPUSH_EVENT_SAVE).bundle(Constant.WxPushConstant.KEY_AUDIOS, pushItems).build());
    }

    public void deleteWxPushDatas(List<PushItem> pushItems) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_WXPUSH_EVENT_DELETE).bundle(Constant.WxPushConstant.KEY_AUDIOS, pushItems).build());
    }

}

