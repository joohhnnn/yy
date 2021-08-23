package com.txznet.launcher.module.login;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.R;
import com.txznet.launcher.data.http.ApiConst;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.login.LoginManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.Conditions;
import com.txznet.launcher.utils.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.util.QRUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ASUS User on 2018/3/20.
 * 安吉星的登录界面
 */

public class LoginModule extends BaseModule {

    @Bind(R.id.tv_login_title)
    TextView tvLoginTitle;
    @Bind(R.id.iv_login_qrcode)
    ImageView ivLoginQrcode;
    @Bind(R.id.iv_login_refresh_qrcode)
    ImageView ivLoginRefreshQrcode;
    @Bind(R.id.tv_login_tips)
    TextView tvLoginTips;
    @Bind(R.id.iv_login_state)
    ImageView ivLoginState;

    /**
     * 登录的二维码
     */
    public static final int TYPE_LOGIN_QR = 1;
    /**
     * 登录成功
     */
    public static final int TYPE_LOGIN_SUCCESS = 2;
    /**
     * 登录失败
     */
    public static final int TYPE_LOGIN_ERROR = 3;
    /**
     * 下载二维码
     */
    public static final int TYPE_DOWNLOAD_QR = 4;

    public static final String PARAMS_RETURN_LOGIN_AFTER_CALL = "params_return_login_after_call";// 传递的参数：是否从电话界面返回

    private static final String TASK_ID_LOGIN = "task_id_login";

    private int mCurrentType = TYPE_LOGIN_QR;
    private boolean mNeedSpeakText = true;
    private String mData="";//传递过来的data。由于data是用来获取状态的，但在onCreateView也要设置状态，为了不改变原来的逻辑，将data保存下载，等到要使用状态的时候才从data中获取。

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        mData = data;
        mNeedSpeakText = !LoginManager.getInstance().isLogout();
        registerWakeupTask();
    }

    /**
     * 注册唤醒词，屏蔽了小欧小欧的唤醒。这些注册要在界面移除和登录成功的时候要一起移除。
     */
    private void registerWakeupTask() {
        TXZAsrManager.AsrComplexSelectCallback asrComplexSelectCallback = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public String getTaskId() {
                return TASK_ID_LOGIN;
            }

            @Override
            public boolean needAsrState() {
                return true;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                super.onCommandSelected(type, command);
                mNeedSpeakText = true;
                if (!"REMOVE_NAME".equals(type)) {
                    TXZTtsManager.getInstance().cancelSpeak(mLastTtsId);
                }
                if (TextUtils.equals("SHOW_LOGIN", type)) {
                    if (mCurrentType != TYPE_LOGIN_QR) {
                        mCurrentType = TYPE_LOGIN_QR;
                        AppLogic.runOnUiGround(refreshContainer);
                    }
                } else if (TextUtils.equals("SHOW_DOWNLOAD", type)) {
                    if (mCurrentType != TYPE_DOWNLOAD_QR) {
                        mCurrentType = TYPE_DOWNLOAD_QR;
                        AppLogic.runOnUiGround(refreshContainer);
                    }
                }
                if (BuildConfig.DEBUG) {
                    if (TextUtils.equals("TEST_LOGIN_SUCCESS", type)) {
                        mCurrentType = TYPE_LOGIN_SUCCESS;
                        AppLogic.runOnUiGround(refreshContainer);
                    } else if (TextUtils.equals("TEST_LOGIN_ERROR", type)) {
                        mCurrentType = TYPE_LOGIN_ERROR;
                        AppLogic.runOnUiGround(refreshContainer);
                    }
                }
            }
        }
                .addCommand("SHOW_LOGIN", "登录设备")
                .addCommand("SHOW_DOWNLOAD", "下载安吉星")
                .addCommand("REMOVE_NAME", "小欧小欧");
        if (BuildConfig.DEBUG) {
            asrComplexSelectCallback
                    .addCommand("TEST_LOGIN_SUCCESS", "登录成功")
                    .addCommand("TEST_LOGIN_ERROR", "登录失败");
        }
        TXZAsrManager.getInstance().useWakeupAsAsr(asrComplexSelectCallback);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_login, null);

        ButterKnife.bind(this, view);
//        mCurrentType = TYPE_LOGIN_QR;
        mCurrentType=getTypeFromData(mData);
        AppLogic.runOnUiGround(refreshContainer);

        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        mData = data;
        registerWakeupTask();
        mNeedSpeakText = false;
//        mCurrentType = TYPE_LOGIN_QR;
        mCurrentType=getTypeFromData(mData);
        AppLogic.runOnUiGround(refreshContainer);
    }

    /**
     * 根据传递的状态，确定使用的type
     * @param data
     * @return 状态。如下载、登录等。
     */
    private int getTypeFromData(String data) {
        int type=TYPE_LOGIN_QR;
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        Boolean returnLoginAfterCall = jsonBuilder.getVal(PARAMS_RETURN_LOGIN_AFTER_CALL, Boolean.class);
        LogUtil.e("return after call:"+returnLoginAfterCall);
        // 如果是在拨打安吉星客服后返回的登录界面，按需求要保存上一次的状态。
        if (returnLoginAfterCall != null && returnLoginAfterCall) {
            // 如果是下载状态，那么还是用下载状态；其他的都用登录状态。
            LogUtil.e("type before return:" + mCurrentType);
            switch (mCurrentType) {
                case TYPE_DOWNLOAD_QR:
                    type = TYPE_DOWNLOAD_QR;
                    break;
                default:
                    type = TYPE_LOGIN_QR;
                    break;
            }
        } else {
            type = TYPE_LOGIN_QR;
        }
        return type;
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        TXZTtsManager.getInstance().cancelSpeak(mLastTtsId);
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_LOGIN);
        RecordWinManager.getInstance().ctrlRecordWinDismiss();
        LaunchManager.getInstance().enableTime(true);
    }

    private Runnable refreshContainer = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().enableTime(false);
            switch (mCurrentType) {
                case TYPE_LOGIN_QR:
                    tvLoginTitle.setText("登录设备");
                    ivLoginState.setVisibility(View.GONE);
                    ivLoginRefreshQrcode.setVisibility(View.GONE);
                    try {
                        ivLoginQrcode.setImageBitmap(QRUtil.createQRCodeBitmap(StringUtils.getDeviceQrCode(), 120));
                    } catch (WriterException e) {
                        ivLoginRefreshQrcode.setVisibility(View.VISIBLE);
                    }
                    if (mNeedSpeakText) {
                        TXZTtsManager.getInstance().cancelSpeak(mLastTtsId);
                        mLastTtsId = TXZTtsManager.getInstance().speakText("使用安吉星应用内扫一扫功能，扫描上面二维码登录设备");
                    }
                    tvLoginTips.setText("使用安吉星app内扫一扫功能\r\n扫描上面二维码登录设备");
                    LaunchManager.getInstance().showAtImageBottom("如没有安吉星app\r\n请说\"下载安吉星\"");
                    LoginManager.getInstance().checkBindState();
                    break;
                case TYPE_LOGIN_SUCCESS:
                    ivLoginRefreshQrcode.setVisibility(View.GONE);
                    ivLoginState.setVisibility(View.VISIBLE);
                    ivLoginState.setImageResource(R.drawable.ic_login_success);
                    tvLoginTips.setText("登录成功");
                    LaunchManager.getInstance().showAtImageBottom("");
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_LOGIN);
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_ANJIXING_LOGIN);
                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_OPERATION_COMPLETE);
                        }
                    }, 2000);
                    break;
                case TYPE_LOGIN_ERROR:
                    ivLoginRefreshQrcode.setVisibility(View.GONE);
                    ivLoginState.setVisibility(View.VISIBLE);
                    ivLoginState.setImageResource(R.drawable.ic_login_failed);
                    tvLoginTips.setText("登录失败");
                    LaunchManager.getInstance().showAtImageBottom("");
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentType = TYPE_LOGIN_QR;
                            refreshContainer.run();
                        }
                    }, 2000);
                    break;
                case TYPE_DOWNLOAD_QR:
                    tvLoginTitle.setText("下载应用");
                    LoginManager.getInstance().cancelCheckBindState();
                    ivLoginState.setVisibility(View.GONE);
                    ivLoginRefreshQrcode.setVisibility(View.GONE);
                    try {
                        //TODO 下载的二维码需要生成
                        ivLoginQrcode.setImageBitmap(QRUtil.createQRCodeBitmap(Conditions.useAnjixingTestEnvironment() ? ApiConst.DOWNLOAD_URL_ANJIXING_TEST : ApiConst.DOWNLOAD_URL_ANJIXING_PRODUCT, 120));
                    } catch (WriterException e) {
                        ivLoginRefreshQrcode.setVisibility(View.VISIBLE);
                    }
                    if (mNeedSpeakText) {
                        TXZTtsManager.getInstance().cancelSpeak(mLastTtsId);
                        mLastTtsId = TXZTtsManager.getInstance().speakText("扫描上面二维码下载安吉星应用，如已下载安吉星应用，请说登录设备");
                    }
                    tvLoginTips.setText("扫描上面二维码\r\n下载安吉星app");
                    LaunchManager.getInstance().showAtImageBottom("如已下载安吉星app\r\n请说\"登录设备\"");
                    break;
            }
        }
    };

    private int mLastTtsId;

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_ANJIXING_HAS_BIND
        };
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_ANJIXING_HAS_BIND:
                TtsUtil.cancelSpeak(mLastTtsId);
                TtsUtil.speakText("登录成功");
                mCurrentType = TYPE_LOGIN_SUCCESS;
                AppLogic.runOnUiGround(refreshContainer);
                break;
        }
    }
}
