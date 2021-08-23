package com.txznet.launcher.module.wechat;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.NetworkUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZBindManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.bean.WechatBindInfo;
import com.txznet.txz.util.QRUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ASUS User on 2018/3/20.
 * 绑定设备界面
 */

public class WechatBindModule extends BaseModule {


    @Bind(R.id.tv_wechat_bind_title)
    TextView tvWechatBindTitle;
    @Bind(R.id.iv_wechat_bind_qrcode)
    ImageView ivWechatBindQrcode;
    @Bind(R.id.iv_wechat_bind_refresh_qrcode)
    ImageView ivWechatBindRefreshQrcode;
    @Bind(R.id.tv_wechat_bind_tips)
    TextView tvWechatBindTips;

    private static final String TASK_ID_WECHAT_BIND = "task_id_wechat_bind";

    private void registerWakeupTask() {
        TXZAsrManager.getInstance().useWakeupAsAsr(new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return TASK_ID_WECHAT_BIND;
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                super.onCommandSelected(type, command);
                if (TextUtils.equals("LAUNCHER_DESKTOP", type)) {
                    AppLogic.removeUiGroundCallback(closeRunnable);
                    closeRunnable.run();
                }
            }
        }.addCommand("LAUNCHER_DESKTOP", "返回桌面"));
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().showAtImageBottom("");
            LaunchManager.getInstance().launchDesktop();
        }
    };

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        registerWakeupTask();
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_wechat_bind, null);

        ButterKnife.bind(this, view);
        AppLogic.runOnUiGround(refreshContainer);

        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        registerWakeupTask();
        AppLogic.runOnUiGround(refreshContainer);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLogic.removeUiGroundCallback(closeRunnable);
        AppLogic.runOnUiGround(closeRunnable, PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_WECHAT_BIND_QR_TIMEOUT, PreferenceUtil.DEFAULT_WECHAT_BIND_QR_TIMEOUT));
        registerWakeupTask();
        TXZBindManager.getInstance().setOnBindStatusChangeListener(mOnBindStatusChangeListener);
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        isAlreadySpeak = false;
        TtsUtil.cancelSpeak(mLastTtsId);
        AppLogic.removeUiGroundCallback(closeRunnable);
        LaunchManager.getInstance().enableTime(true);
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_WECHAT_BIND);
        TXZBindManager.getInstance().clearOnBindStatusChangeListener();
    }

    private TXZBindManager.OnBindStatusChangeListener mOnBindStatusChangeListener = new TXZBindManager.OnBindStatusChangeListener() {
        @Override
        public void onBindStatusChanged(WechatBindInfo newBindInfo) {
            AppLogic.runOnUiGround(refreshContainer);
        }
    };


    private boolean isAlreadySpeak;

    /**
     * 就这个方法来说，获取绑定状态的方法只有一个，就是直接获取绑定信息。
     * 当没有绑定信息并且有网络的情况，去微信那里获取一次绑定状态。这个状态的获取结果会通过{@link TXZBindManager}的statusListener回调给listener。
     * 我们注册了一个listener，会调用这个Runnable。然后又一次取绑定的信息。
     */
    private Runnable refreshContainer = new Runnable() {
        @Override
        public void run() {
            boolean hasNet = NetworkUtils.isNetworkConnected(GlobalContext.get());
            LaunchManager.getInstance().enableTime(false);
            ivWechatBindRefreshQrcode.clearAnimation();
            ivWechatBindRefreshQrcode.setVisibility(View.GONE);
            WechatBindInfo mBindStatus = TXZBindManager.getInstance().getBindStatus();
            //设备已经被绑定了的情况
            if (mBindStatus.hasBind()) {
                String txt = String.format("设备已绑定微信号-%s，\r\n如需解绑，请前往公众号操作", mBindStatus.getBindUserNick());
                tvWechatBindTips.setText(txt);
            } else {
                tvWechatBindTips.setText("使用微信应用内扫一扫功能\r\n扫描上面二维码绑定设备");
                LaunchManager.getInstance().showAtImageBottom("如已绑定设备\r\n请说\"返回桌面\"");
            }
            try {
                ivWechatBindQrcode.setImageBitmap(QRUtil.createQRCodeBitmap(mBindStatus.getBindQr(), 120));
                AppLogic.removeUiGroundCallback(mRefreshQrTimeoutRunnable);
                if (!mBindStatus.hasBind() && !isAlreadySpeak) {
                    mLastTtsId = TXZTtsManager.getInstance().speakText("使用微信应用内扫一扫功能，扫描上面二维码绑定设备");
                    isAlreadySpeak = true;
                }
            } catch (Exception e) {
                // 当二维码内容为空的时候，会出异常。
                ivWechatBindRefreshQrcode.setVisibility(View.VISIBLE);
                if (hasNet) {
                    // 转圈，10秒后提示异常
                    ivWechatBindRefreshQrcode.startAnimation(AnimationUtils.loadAnimation(GlobalContext.get(), R.anim.anim_rotation_fast));
                    AppLogic.removeUiGroundCallback(mRefreshQrTimeoutRunnable);
                    AppLogic.runOnUiGround(mRefreshQrTimeoutRunnable, 1000 * 10);
                    // 如果没能直接获取到绑定状态，就强制去微信请求一次。只能有网的时候才去请求，不然会进入死循环。
                    TXZBindManager.getInstance().refreshBindStatus();
                } else {
                    // 没网，直接提示异常。
                    AppLogic.runOnUiGround(mRefreshQrTimeoutRunnable);
                }
            }
        }
    };


    private int mLastTtsId;
    private Runnable mRefreshQrTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            ivWechatBindRefreshQrcode.clearAnimation();
            String tip = "网络异常，请稍后再试";
            tvWechatBindTips.setText(tip);
            mLastTtsId = TtsUtil.speakText(tip);
        }
    };

}
