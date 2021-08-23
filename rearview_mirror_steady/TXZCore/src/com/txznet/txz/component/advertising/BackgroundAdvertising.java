package com.txznet.txz.component.advertising;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;


import com.txznet.advertising.base.IBackgroundAdvertisingTool;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.advertising.BaseAdvertisingControl;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.txz.component.advertising.util.AdvertisingUtils;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;

public class BackgroundAdvertising implements IBackgroundAdvertisingTool {
    private static BackgroundAdvertising sInstance = new BackgroundAdvertising();
    private String mUrl;
    private boolean isShowing;
    private float mOpacity;//蒙版透明度值n = 0-100 ，对应 1-n%的透明度
    private String[] mOpacityStr = {"FF", "F2", "E5", "D8", "CC", "BF", "B2", "A5", "99", "8c", "7F",
            "72", "66", "59", "4c", "3F", "33", "21", "19", "0c", "00"};//FF全透明、00不透明

    //    00%=FF（不透明）    5%=F2    10%=E5    15%=D8    20%=CC    25%=BF    30%=B2    35%=A5    40%=99    45%=8c    50%=7F
//            55%=72    60%=66    65%=59    70%=4c    75%=3F    80%=33    85%=21    90%=19    95%=0c    100%=00（全透明）
    private BackgroundAdvertising() {

    }

    public static BackgroundAdvertising getInstance() {
        return sInstance;
    }

    @Override
    public void show() {
        LogUtil.d("background ad show.");
        if (RecorderWin.isOpened() && !TextUtils.isEmpty(mUrl) && !isShowing) {
            isShowing = true;
            if (AdvertisingUtils.compareWidthAndHeight(mUrl)) {
                Drawable drawable = AdvertisingUtils.getDrawable(mUrl, getHeight(), getWidth());
                int index = (int) ((100 - mOpacity) / 5);//根据蒙版透明度计算坐标
                String colorStr = "#" + mOpacityStr[index] + "000000";//覆盖一层透明黑
                LogUtil.d("background ad colorstr:" + colorStr);
                drawable.setColorFilter(Color.parseColor(colorStr), PorterDuff.Mode.SRC_OVER);//DARKEN
                WinManager.getInstance().setBackground(drawable);
            } else {
                Bitmap bitmap = AdvertisingUtils.getBitmap(mUrl,getHeight(),getWidth());
                if (bitmap == null) {
                    return;
                }
                Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(result);
                canvas.drawColor(Color.parseColor("#1C1C23"));
                bitmap = AdvertisingUtils.setBitmapSize(bitmap, (int) (getWidth() * 0.64), getHeight());
                canvas.drawBitmap(bitmap, (float) (getWidth() * 0.18), 0, null);
                Drawable drawable = AdvertisingUtils.bitmap2Drawable(result);
                int index = (int) ((100 - mOpacity) / 5);//根据蒙版透明度计算坐标
                String colorStr = "#" + mOpacityStr[index] + "000000";//覆盖一层透明黑
                LogUtil.d("background ad colorstr:" + colorStr);
                drawable.setColorFilter(Color.parseColor(colorStr), PorterDuff.Mode.SRC_OVER);
                WinManager.getInstance().setBackground(drawable);
            }
        }
    }

    @Override
    public void dismiss() {
        LogUtil.d("background ad dismiss.");
        if (isShowing) {
            isShowing = false;
            WinManager.getInstance().setBackground(null);
        }
    }

    @Override
    public boolean isSupportShow() {
        BaseAdvertisingControl baseAdvertisingControl = ConfigUtil.getBaseAdvertisingControl();
        if (baseAdvertisingControl != null && !baseAdvertisingControl.supportAdvertising()) {
            LogUtil.logd("background ad Advertising control false.");
            return false;
        }
        return true;
    }

    @Override
    public int getWidth() {
        return DeviceInfo.getScreenWidth();
    }

    @Override
    public int getHeight() {
        return DeviceInfo.getScreenHeight();
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
    public void setOpacity(float opacity) {
        mOpacity = opacity;
    }
}
