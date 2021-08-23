package com.txznet.webchat.ui.base;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.txznet.comm.util.ScreenUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.Config;
import com.txznet.webchat.R;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxUIConfig;
import com.txznet.webchat.plugin.WxLoadedPluginInfo;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.stores.WxWindowConfigStore;
import com.txznet.webchat.ui.car.Car_QRCodeActivity;
import com.txznet.webchat.ui.car.t700.Car_QRCodeActivity_T700;
import com.txznet.webchat.ui.rearview_mirror.QRCodeActivity;

import java.lang.reflect.Field;

import butterknife.ButterKnife;

/**
 * Activity基类
 * <p/>
 * Created by J on 2016/3/15.
 */
public abstract class AppBaseActivity extends com.txznet.comm.base.BaseActivity {
    private static final int KEYCODE_DEFAULT_LEFT = 21;
    private static final int KEYCODE_DEFAULT_RIGHT = 22;
    private static final int KEYCODE_DEFAULT_UP = 19;
    private static final int KEYCODE_DEFAULT_DOWN = 20;
    private static final int KEYCODE_DEFAULT_PREV = 260;
    private static final int KEYCODE_DEFAULT_NEXT = 261;
    private static final int KEYCODE_DEFAULT_CONFIRM = 23;
    private static final int KEYCODE_DEFAULT_CONFIRM_ENTER = 66;
    private static final int KEYCODE_DEFAULT_BACK = 4;
    protected static final String ACTION_ACTIVITY_FOREGROUND =
            "com.txznet.webchat.action.ACTIVITY_FOREGROUND";
    protected static final String ACTION_KEY_ACTIVITY_NAME = "activity_name";

    protected String TAG = this.getClass().getSimpleName();
    protected boolean mManuallyExit = false; // 用户手动退出app将此标志位置true
    private TextView mTestMark; // 测试水印
    protected boolean mIgnoreLoginStatus = false;

    private FocusSupporter mNavBtnSupporter;

    /**
     * 设置layout
     *
     * @return layout resource id
     */
    protected abstract int getLayout();

    /**
     * 设置Activity订阅的Store列表
     *
     * @return Store列表
     */
    protected abstract Store[] getRegisterStores();

    /**
     * 界面初始化
     *
     * @param savedInstanceState
     */
    protected abstract void init(Bundle savedInstanceState);

    /**
     * 初始化焦点支持
     * 初始化焦点View列表, 设置给
     */
    protected abstract void initFocusViewList();

    /**
     * 根据当前界面状态刷新焦点View列表
     */
    protected void refreshFocusViewList() {

    }

    /**
     * 注册的Store发生变化时调用
     * 注意： 子类必须重写此方法来响应Store改变事件，以保证logout事件被正确处理
     * Otto不支持@SubScribe继承，所以需要子类自己添加 @SubScribe 来注册
     *
     * @param event
     */
    public void onStoreChange(Store.StoreChangeEvent event) {
        if (WxLoginStore.EVENT_TYPE_ALL.equals(event.getType())) {
            checkLoginStatus();
        } else if (WxWindowConfigStore.EVENT_TYPE_ALL.equals(event.getType())) {
            refreshWindowParams(true);
        }
    }

    protected final FocusSupporter getNavBtnSupporter() {
        return mNavBtnSupporter;
    }

    /**
     * 处理方控按键事件
     * 子类可以通过重写此方法来干预方控按键的处理
     *
     * @param op 方控按键事件
     * @return 是否消费事件
     */
    protected boolean onFocusOperation(int op) {
        return false;
    }

    /**
     * 是否启用了方控按键支持
     * 子类在进行方控支持相关操作时, 需要先调用此方法确认当前环境已开启方控支持
     *
     * @return
     */
    protected final boolean isDpadSupportEnabled() {
        return WxNavBtnHelper.getInstance().isDpadSupportEnabled();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setType(WxConfigStore.getInstance().getScreenWindowType());
        getWindow().addFlags(WxConfigStore.getInstance().getScreenWindowFlag());

        if (!UIHandler.getInstance().getWxUIEnabled()) {
            finish();
            return;
        }

        if (0 != getLayout()) {
            setContentView(getLayout());
            ButterKnife.bind(this);

            // Activity创建时不需要重启, 因为总是按当前即最新的UIConfig适配的
            refreshWindowParams(false);

            // init NavBtnSupporter
            if (isDpadSupportEnabled()) {
                mNavBtnSupporter =
                        FocusSupporter.attach(this, WxNavBtnHelper.getInstance().getNavMode())
                                .setFocusDrawable(getResources()
                                        .getDrawable(R.drawable.ic_nav_indicator_round))
                                .setLogEnabled(true);
                mNavBtnSupporter.setOnFocusEventListener(new FocusSupporter.OnFocusEventListener() {
                    @Override
                    public boolean onFocusOperation(int op) {
                        return AppBaseActivity.this.onFocusOperation(op);
                    }
                });
            }
        }

        init(savedInstanceState);

        if (isDpadSupportEnabled() && !this.isFinishing()) {
            initFocusViewList();
        }
    }

    @Override
    protected void setTheme() {
        if (WxConfigStore.getInstance().isTransparentBackgroundEnabled()) {
            super.setTheme();
        } else {
            try {
                Class<?> clsRstyle = Class.forName("com.txznet.webchat.R$style");
                Field f = clsRstyle.getDeclaredField("AppNoTransparentTheme");
                this.setTheme(f.getInt(null));
                getApplication().setTheme(f.getInt(null));
            } catch (Exception e) {
            }
        }
    }

    private WxUIConfig mUIConfig;

    private void refreshWindowParams(boolean enableRestart) {
        WxUIConfig uiConfig = WxWindowConfigStore.getInstance().getUIConfig();
        if (null != uiConfig) {
            L.e("AppBaseActivity", "ui config: " + uiConfig.toString());
            boolean sizeChanged = checkUISizeChanged(uiConfig);
            mUIConfig = uiConfig;
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = mUIConfig.width;
            lp.height = mUIConfig.height;
            lp.x = mUIConfig.x;
            lp.y = mUIConfig.y;
            lp.gravity = mUIConfig.gravity;
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            ScreenUtils.updateScreenSize(mUIConfig.width, mUIConfig.height, true);
            getWindow().setAttributes(lp);

            if (sizeChanged && enableRestart) {
                recreate();
            }
        }
    }

    private boolean checkUISizeChanged(WxUIConfig newConfig) {
        if (null == mUIConfig) {
            return true;
        }

        return !(mUIConfig.width == newConfig.width && mUIConfig.height == newConfig.height);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!interceptDispatchKeyEvent(event)) {
            return super.dispatchKeyEvent(event);
        }

        if (KeyEvent.ACTION_DOWN == event.getAction()) {
            switch (event.getKeyCode()) {
                case KEYCODE_DEFAULT_LEFT:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performLeft();
                    break;

                case KEYCODE_DEFAULT_RIGHT:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performRight();
                    break;

                case KEYCODE_DEFAULT_UP:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performUp();
                    break;

                case KEYCODE_DEFAULT_DOWN:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performDown();
                    break;

                case KEYCODE_DEFAULT_PREV:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performPrev();
                    break;

                case KEYCODE_DEFAULT_NEXT:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performNext();
                    break;

                case KEYCODE_DEFAULT_CONFIRM:
                case KEYCODE_DEFAULT_CONFIRM_ENTER:
                    WxNavBtnHelper.getInstance().setNativeNavBtnTriggered(true);
                    mNavBtnSupporter.performClick();
                    break;

                case KEYCODE_DEFAULT_BACK:
                    mNavBtnSupporter.performBack();
                    break;
            }
        }

        return true;
    }

    private boolean interceptDispatchKeyEvent(KeyEvent event) {
        if (!isDpadSupportEnabled()) {
            return false;
        }

        int triggeredKeyCode = event.getKeyCode();
        return (KEYCODE_DEFAULT_BACK == triggeredKeyCode
                || KEYCODE_DEFAULT_CONFIRM == triggeredKeyCode
                || KEYCODE_DEFAULT_RIGHT == triggeredKeyCode
                || KEYCODE_DEFAULT_LEFT == triggeredKeyCode
                || KEYCODE_DEFAULT_UP == triggeredKeyCode
                || KEYCODE_DEFAULT_DOWN == triggeredKeyCode
                || KEYCODE_DEFAULT_PREV == triggeredKeyCode
                || KEYCODE_DEFAULT_NEXT == triggeredKeyCode
                || KEYCODE_DEFAULT_CONFIRM_ENTER == triggeredKeyCode);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // subscribe stores
        if (getRegisterStores() != null) {
            for (Store store : getRegisterStores()) {
                store.register(this);
            }
        }

        // 若没有注册WxLoginStore, 添加注册
        // WxLoginStore保存了登录状态, 当登录失效时所有界面(除了扫码页面)都应该自行finish
        try {
            WxLoginStore.get().register(this);
        } catch (IllegalArgumentException e) {

        }
        // 若没有注册WxWindowConfigStore, 添加注册
        // WxWindowConfigStore保存了界面显示参数, 发生改变时所有界面都要根据新参数刷新界面
        try {
            WxWindowConfigStore.getInstance().register(this);
        } catch (IllegalArgumentException e) {

        }
    }

    private TXZWheelControlManager.OnTXZWheelControlListener mControlListener =
            new TXZWheelControlManager.OnTXZWheelControlListener() {
                @Override
                public void onKeyEvent(final int eventId) {

                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            switch (eventId) {
                                case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performBack();
                                    break;

                                case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performClick();
                                    break;

                                case TXZWheelControlEvent.LEVOROTATION_EVENTID:
                                    mNavBtnSupporter.performPrev();
                                    break;

                                case TXZWheelControlEvent.DEXTROROTATION_EVENTID:
                                    mNavBtnSupporter.performNext();
                                    break;

                                case TXZWheelControlEvent.LEFT_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performLeft();
                                    break;

                                case TXZWheelControlEvent.RIGHT_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performRight();
                                    break;

                                case TXZWheelControlEvent.UP_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performUp();
                                    break;

                                case TXZWheelControlEvent.DOWN_KEY_CLICKED_EVENTID:
                                    mNavBtnSupporter.performDown();
                                    break;
                            }
                        }
                    }, 0);

                }
            };


    @Override
    protected void onStop() {
        super.onStop();
        // un subscribe stores
        try {
            if (getRegisterStores() != null) {
                for (Store store : getRegisterStores()) {
                    store.unregister(this);
                }
            }
        } catch (Exception e) {
            L.e(TAG, "unregister stores encountered error: " + e.toString());
        }

        try {
            WxLoginStore.get().unregister(this);
        } catch (IllegalArgumentException e) {

        }

        try {
            WxWindowConfigStore.getInstance().unregister(this);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    protected void onPause() {
        // remove debug tag
        if (mTestMark != null) {
            getWindowManager().removeView(mTestMark);
            mTestMark = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (null != mNavBtnSupporter) {
            mNavBtnSupporter.detach();
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 设置显示参数
        refreshWindowParams(true);

        if (Config.INNER_TEST) {
            printTestMark();
        }

        // 若不忽略登录状态且当前Activity不是AppStartActivity，检测登录状态
        if (!(mIgnoreLoginStatus
                || this instanceof QRCodeActivity
                || this instanceof Car_QRCodeActivity
                || this instanceof Car_QRCodeActivity_T700)) {
            checkLoginStatus();
        }
    }

    /**
     * 检查登录状态，若已退出登录，执行相应逻辑
     */
    private void checkLoginStatus() {
        if (!WxLoginStore.get().isLogin()) {
            L.e("base logout, manually = " + mManuallyExit);
            if (!mManuallyExit) {
                Intent intent = new Intent();
                intent.setClass(this, WxThemeStore.get().getClassForQRActivity());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                // 知豆双屏处理
                sendBroadcast(new Intent("com.txznet.webchat.main_finish"));
                finish();
            }
        }
    }

    private void printTestMark() {
        try {
            if (mTestMark != null) {
                return;
            }

            if (null != mUIConfig) {
                return;
            }

            // get app version
            StringBuilder debugTextBuilder = new StringBuilder(
                    String.format("此版本为同行者内部测试版: ver: %s_%s",
                            BuildConfig.VERSION_NAME, BuildConfig.SVN_VERSION));

            for (WxLoadedPluginInfo plugin : WxPluginManager.getInstance().getPluginList()) {
                debugTextBuilder.append("\n" + plugin.getPlugin().getToken() + " : " +
                        plugin.getPlugin().getVersionName());
                debugTextBuilder.append("(" + plugin.getLoadType().getTypeName() + ")");
            }


            mTestMark = new TextView(this);
            mTestMark.setText(debugTextBuilder.toString());
            mTestMark.setTextSize(16);
            mTestMark.setTextColor(Color.parseColor("#ccffffff"));
            WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();
            int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            mTestMark.measure(w, h);
            mLp.width = mTestMark.getMeasuredWidth();
            mLp.height = mTestMark.getMeasuredHeight();
            mLp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLp.format = PixelFormat.RGBA_8888;
            mLp.gravity = Gravity.LEFT | Gravity.TOP;
            mLp.x = 10;
            mLp.y = 10;

            getWindowManager().addView(mTestMark, mLp);

        } catch (Exception e) {
        }
    }

    @Override
    public void onGetFocus() {
        super.onGetFocus();

        if (isDpadSupportEnabled()) {
            // 注册方控事件
            TXZWheelControlManager.getInstance().registerWheelControlListener(mControlListener);
        }
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();

        if (isDpadSupportEnabled()) {
            // 反注册方控事件
            TXZWheelControlManager.getInstance().unregisterWheelControlListener(mControlListener);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (null != mUIConfig) {
            outState.putParcelable("ui_config", mUIConfig);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey("ui_config")) {
            mUIConfig = savedInstanceState.getParcelable("ui_config");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean enableMaskFromChild() {
        return false;
    }
}