package com.txznet.sdk.bean;

/**
 *  微信设备绑定状态
 * Created by J on 2018/4/24.
 */

public final class WechatBindInfo {
    private boolean bHasBind;
    private String mBindQrUrl;
    private String mBindUserNick;
    private String mHeadUrl;

    public WechatBindInfo(boolean hasBind, String qrUrl, String bindUserNick, String headUrl) {
        this.bHasBind = hasBind;
        this.mBindQrUrl = qrUrl;
        this.mHeadUrl = headUrl;
        this.mBindUserNick = bindUserNick;
    }

    /**
     * 是否已有用户绑定此设备
     *
     * @return 已有绑定返回true
     */
    public boolean hasBind() {
        return bHasBind;
    }

    /**
     * 获取用于扫码绑定的微信二维码
     *
     * @return 二维码url字串
     */
    public String getBindQr() {
        return mBindQrUrl;
    }

    /**
     * 获取绑定用户昵称
     *
     * @return 若已有用户绑定此设备, 返回绑定用户的昵称
     */
    public String getBindUserNick() {
        return mBindUserNick;
    }

    /**
     * 获取公众号绑定的微信头像
     * @return
     */
    public String getHeadUrl() {
        return mHeadUrl;
    }
}
