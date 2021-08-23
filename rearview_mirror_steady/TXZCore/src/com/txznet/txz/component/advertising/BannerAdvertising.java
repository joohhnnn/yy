package com.txznet.txz.component.advertising;

import com.txznet.advertising.base.IBannerAdvertisingTool;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.advertising.BaseAdvertisingControl;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.advertising.view.BannerAdvertisingView;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;

public class BannerAdvertising implements IBannerAdvertisingTool {
    private static BannerAdvertising sInstance = new BannerAdvertising();
    private int mType;
    private String mUrl;
    private boolean isShowing;

    private BannerAdvertising(){

    }

    public static BannerAdvertising getInstance() {
        return sInstance;
    }


    @Override
    public void show() {
        LogUtil.d("banner ad show.");
        if (RecorderWin.isOpened()) {
            isShowing = true;
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    WinManager.getInstance().setBannerAdvertisingView(BannerAdvertisingView.createView(mType, mUrl));
                }
            });
        }
    }

    @Override
    public void dismiss() {
        LogUtil.d("banner ad dismiss.");
        if (isShowing) {
            isShowing = false;
            WinManager.getInstance().removeBannerAdvertisingView();
        }
    }

    @Override
    public boolean isSupportShow() {
        if (DeviceInfo.getScreenHeight() > DeviceInfo.getScreenWidth()) {
            LogUtil.logd("banner ad don't support show,because device is portrait screen.");
            return false;
        }
        BaseAdvertisingControl baseAdvertisingControl = ConfigUtil.getBaseAdvertisingControl();
        if (baseAdvertisingControl != null && !baseAdvertisingControl.supportAdvertising()) {
            LogUtil.logd("banner ad Advertising control false.");
            return false;
        }
        return true;
    }

    @Override
    public int getWidth() {
        return getHeight() * 4;
    }

    @Override
    public int getHeight() {
        return DeviceInfo.getScreenHeight() / 4;
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void setType(int type) {
        mType = type;
    }
}
