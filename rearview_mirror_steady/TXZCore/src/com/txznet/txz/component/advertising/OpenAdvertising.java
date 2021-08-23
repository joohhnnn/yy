package com.txznet.txz.component.advertising;

import com.txznet.advertising.base.IOpenAdvertisingTool;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.advertising.view.OpenAdvertisingView;
import com.txznet.txz.component.advertising.view.OpenAdvertisingWin;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.DeviceInfo;

/**
 * 开屏广告
 */
public class OpenAdvertising implements IOpenAdvertisingTool {
    private static OpenAdvertising sInstance = new OpenAdvertising();
    private int mType = 0;//广告类型
    private String mUrl = "";//广告展示内容的地址
    private String mBtnText = "";//关闭按钮的文字
    private String mRedirectUrl = "";//跳转的地址
    private OpenAdvertisingWin mOpenAdvertisingWin;
    private static final String TASK_OPEN_AD = "taskOpenAd";
    private OpenAdvertising() {

    }

    public static OpenAdvertising getInstance() {
        return sInstance;
    }

    @Override
    public void show() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mOpenAdvertisingWin == null) {
                    mOpenAdvertisingWin = new OpenAdvertisingWin();
                }
                if (mOpenAdvertisingWin.isShowing()) {
                    return;
                }
                LogUtil.d("open ad show.");
                registerWakeupWork();
                mOpenAdvertisingWin.setOpenAdvertisingView(OpenAdvertisingView.createView(mType, mUrl, mBtnText, mRedirectUrl));
                mOpenAdvertisingWin.show();
            }
        });

    }

    @Override
    public void dismiss() {
        if (mOpenAdvertisingWin != null && mOpenAdvertisingWin.isShowing()) {
            LogUtil.d("open ad dismiss.");
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    mOpenAdvertisingWin.dismiss();
                }
            });
            WakeupManager.getInstance().recoverWakeupFromAsr(TASK_OPEN_AD);
            MusicFocusManager.getInstance().releaseAudioFocusImmediately();
        }
    }

    @Override
    public boolean isSupportShow() {
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
        if (mOpenAdvertisingWin != null) {
            return mOpenAdvertisingWin.isShowing();
        }
        return false;
    }

    @Override
    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public void setCloseBtnText(String btnText) {
        mBtnText = btnText;
    }

    @Override
    public void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    @Override
    public void setType(int type) {
        mType = type;
    }

    private void registerWakeupWork() {
        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return TASK_OPEN_AD;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                log("onCommand type:" + type + ",command:" + command);
                dismiss();
            }
        }.addCommand("SKIP_OPEN_AD", "跳过");
        WakeupManager.getInstance().useWakeupAsAsr(callback);
    }

    private void log(String log) {
        LogUtil.d(log);
    }
}
