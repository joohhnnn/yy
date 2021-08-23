package com.txznet.launcher.module.wechat;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.NetworkUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.sdk.TXZWechatManagerV2;
import com.txznet.txz.util.QRUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ASUS User on 2018/3/20.
 * 微信登录界面
 */

public class WechatQrModule extends BaseModule {

    @Bind(R.id.tv_wechat_title)
    TextView tvWechatTitle;
    @Bind(R.id.iv_wechat_qrcode)
    ImageView ivWechatQrcode;
    @Bind(R.id.iv_wechat_qrcode_mask)
    ImageView ivWechatQrcodeMask;
    @Bind(R.id.iv_wechat_refresh_qrcode)
    ImageView ivWechatRefreshQrcode;
    @Bind(R.id.tv_wechat_tip)
    TextView tvWechatTip;

    private int mCurrentType = -1;
    private int mLastType = -1;

    private String url = "";
    private int mTtsTaskId = -1;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        parseData(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_wechat_qr, null);

        ButterKnife.bind(this, view);

        refreshContainer();

        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        parseData(data);

        refreshContainer();
    }

    @Override
    public void onResume() {
        super.onResume();
        long qrTimeout = PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_WECHAT_QR_TIMEOUT, PreferenceUtil.DEFAULT_WECHAT_QR_TIMEOUT);
        if (qrTimeout > 0) {
            AppLogic.runOnUiGround(qrTimeoutRunnable, qrTimeout);
        }
        regAsrCmd();
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        AppLogic.removeUiGroundCallback(qrTimeoutRunnable);
        TtsUtil.cancelSpeak(mTtsTaskId);
        isUserAsrRefresh = false;
        unRegAsrCmd();

        // 复位卡片
        isUserAsrRefresh = false;
        ivWechatRefreshQrcode.clearAnimation();
        ivWechatRefreshQrcode.setVisibility(View.GONE);
        ivWechatQrcodeMask.setVisibility(View.GONE);
        AppLogic.removeUiGroundCallback(mShowQrCodeErrorTask);
        ivWechatQrcode.setImageBitmap(null);
        tvWechatTip.setText("微信“扫一扫”，登录微信助手");
    }

    private Runnable qrTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().launchBack();

            if (isNavInFocusBeforeDisplay) {
                NavManager.getInstance().enterNav();
            }
        }
    };

    private void parseData(String data) {
        mLastType = mCurrentType;
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        mCurrentType = jsonBuilder.getVal("type", Integer.class);
        switch (mCurrentType) {
            case WechatManager.TYPE_WECHAT_SHOW_QRCODE:
                url = jsonBuilder.getVal("data", String.class);
                break;
            case WechatManager.TYPE_WECHAT_UPDATE_QRCODE:
                url = jsonBuilder.getVal("data", String.class);
                break;
        }
    }


    private void refreshContainer() {
        LogUtil.e("refreshContainer: mCurrentType="+mCurrentType);
        switch (mCurrentType) {
            case WechatManager.TYPE_WECHAT_SHOW_QRCODE:
            case WechatManager.TYPE_WECHAT_UPDATE_QRCODE:
                tvWechatTitle.setText("登录微信");
                tvWechatTitle.setCompoundDrawables(null, null, null, null);
                updateQrcodeView();
                break;
        }
    }

    private void updateQrcodeView() {
        ivWechatRefreshQrcode.clearAnimation();
        ivWechatRefreshQrcode.setVisibility(View.GONE);
        ivWechatQrcodeMask.setVisibility(View.GONE);
        try {
            if (TextUtils.isEmpty(url)) {
                //显示二维码的事件中url可能为空，手动刷新一下
                if (NetworkUtils.isNetworkConnected(GlobalContext.get())) { // 有网络
                    AppLogic.runOnUiGround(mShowQrCodeErrorTask, 5000);
                    if (mCurrentType == WechatManager.TYPE_WECHAT_SHOW_QRCODE) {
                        TXZWechatManagerV2.getInstance().refreshQR();
                    }
                } else {
                    showQrcodeError(ivWechatQrcode, ivWechatRefreshQrcode);
                }
            } else {
                if (isUserAsrRefresh) {
                    mTtsTaskId = TtsUtil.speakText("已为您刷新二维码");
                    isUserAsrRefresh = false;
                }
                AppLogic.removeUiGroundCallback(mShowQrCodeErrorTask);
                ivWechatQrcode.setImageBitmap(QRUtil.createQRCodeBitmap(url, 120));
                tvWechatTip.setText("微信“扫一扫”，登录微信助手");
            }
        } catch (Exception e) {
            LogUtil.loge("createQRCodeBitmap : " + e.getMessage());
            showQrcodeError(ivWechatQrcode, ivWechatRefreshQrcode);
        }
    }

    private Runnable mShowQrCodeErrorTask = new Runnable() {
        @Override
        public void run() {
            showQrcodeError(ivWechatQrcode, ivWechatRefreshQrcode);
        }
    };

    /**
     * TODO 提示可以声控刷新二维码
     */
    private void showQrcodeError(ImageView ivWechatQrcode, ImageView ivWechatRefreshQrcode) {
        try {
            ivWechatQrcode.setImageBitmap(QRUtil.createQRCodeBitmap("error", 120));
            ivWechatRefreshQrcode.setVisibility(View.VISIBLE);
            ivWechatQrcodeMask.setVisibility(View.VISIBLE);
            ivWechatRefreshQrcode.startAnimation(AnimationUtils.loadAnimation(GlobalContext.get(), R.anim.anim_rotation_fast));
            String errorTip = "网络异常，请说“刷新二维码”";
            mTtsTaskId = TtsUtil.speakText(errorTip);
            tvWechatTip.setText(errorTip);
        } catch (Exception e) {
        }
    }

    private TXZAsrManager.AsrComplexSelectCallback mAsrComplexSelectCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        @Override
        public String getTaskId() {
            return "TASK_ID_WECHAT";
        }

        @Override
        public boolean needAsrState() {
            return true;
        }

        @Override
        public void onCommandSelected(String type, String command) {
            super.onCommandSelected(type, command);
            switch (type) {
                case "REFRESH_QR_CODE":
                    isUserAsrRefresh = true;
                    TXZWechatManager.getInstance().refreshQR();
                    // 如果5秒都没有反应则认为是刷新失败了。
                    AppLogic.runOnUiGround(mShowQrCodeErrorTask, 5000);
                    break;
            }
        }
    }.addCommand("REFRESH_QR_CODE", "刷新二维码");

    private boolean isUserAsrRefresh; // 用户触发刷新二维码逻辑

    private void regAsrCmd() {
        LogUtil.e("regAsrCmd: ");
        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrComplexSelectCallback);
    }

    private void unRegAsrCmd() {
        TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ID_WECHAT");
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_WX_SCANNED: // 用户扫码后，取消自动退出逻辑
                AppLogic.removeUiGroundCallback(qrTimeoutRunnable);
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        ImgLoader.loadCircleImage(WechatManager.WECHAT_USER_ICON_FILE, ivWechatQrcode, true);
                        tvWechatTip.setText("扫描成功，请在手机确认登录");
                    }
                });
                break;
            case EventTypes.EVENT_WX_LOGIN:
                //登陆成功先退出扫码界面
                AppLogic.runOnUiGround(qrTimeoutRunnable);
                break;
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{EventTypes.EVENT_WX_SCANNED ,EventTypes.EVENT_WX_LOGIN};
    }
}
