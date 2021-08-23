package com.txznet.webchat.ui.base.widgets;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.util.ScreenLock;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.R;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.model.WxUIConfig;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxWindowConfigStore;

import butterknife.ButterKnife;

/**
 * 微信Dialog基类
 * 集成了导航按键支持和显示参数改变的响应
 *
 * NOTE: 子类需要自己为OnStoreChanged方法添加@Subscribe注解来正确订阅WxWindowConfigStore, 从而
 *       正确响应界面显示参数改变
 *
 * Created by J on 2017/2/17.
 */

public abstract class AppBaseWinDialog extends WinDialog {
    private static final int KEYCODE_DEFAULT_LEFT = 21;
    private static final int KEYCODE_DEFAULT_RIGHT = 22;
    private static final int KEYCODE_DEFAULT_UP = 19;
    private static final int KEYCODE_DEFAULT_DOWN = 20;
    private static final int KEYCODE_DEFAULT_PREV = 260;
    private static final int KEYCODE_DEFAULT_NEXT = 261;
    private static final int KEYCODE_DEFAULT_CONFIRM = 23;
    private static final int KEYCODE_DEFAULT_CONFIRM_ENTER = 66;
    private static final int KEYCODE_DEFAULT_BACK = 4;

    private FocusSupporter mNavBtnSupporter;
    private ScreenLock mScreenLock;

    /**
     * 设置Dialog layout
     *
     * @return layout
     */
    protected abstract int getLayout();

    /**
     * 初始化操作
     */
    protected abstract void init();

    /**
     * 初始化方控
     */
    protected abstract void initFocusViewList();

    /**
     * 是否需要屏幕锁
     *
     * @return
     */
    protected boolean needScreenLock() {
        return false;
    }

    /**
     * 是否忽略全局的显示位置配置
     * 某些dialog需要按照整个屏幕坐标系进行布局, 不响应显示位置配置(广播), 如后台录音的Dialog
     *
     * @return 屏蔽返回true
     */
    protected boolean isSystemDialog() {
        return false;
    }

    protected void refreshFocusViewList() {

    }

    /**
     * 设置Dialog的WindowType
     * 以配置文件中指定的配置项优先, 默认TYPE_PHONE
     *
     * @return WindowType
     */
    protected int getWindowType() {
        if (isSystemDialog()) {
            return WxConfigStore.getInstance().getSystemDialogWindowType();
        }
        return WxConfigStore.getInstance().getDialogWindowType();
    }

    /**
     * 设置Dialog需要添加的Flags
     * 以配置文件中指定的配置项优先, 默认不添加Flag
     *
     * @return
     */
    protected int getWindowFlags() {
        if (isSystemDialog()) {
            return WxConfigStore.getInstance().getSystemDialogWindowFlag();
        }
        return WxConfigStore.getInstance().getDialogWindowFlag();
    }

    protected FocusSupporter getNavBtnSupporter() {
        return mNavBtnSupporter;
    }

    public AppBaseWinDialog(boolean isSystem) {
        super(isSystem);

        if (needScreenLock()) {
            mScreenLock = new ScreenLock(getContext());
        }

        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setType(getWindowType());
        getWindow().addFlags(getWindowFlags());

        super.onCreate(savedInstanceState);
    }

    @Override
    protected View createView() {
        View view = LayoutInflater.from(GlobalContext.getModified()).inflate(getLayout(), null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void show() {
        super.show();

        if (needScreenLock()) {
            mScreenLock.lock();
        }

        refreshWindowParam();

        if (WxNavBtnHelper.getInstance().isDpadSupportEnabled()) {
            mNavBtnSupporter = FocusSupporter.attach(this).setFocusDrawable(getContext().getResources().getDrawable(R.drawable.shape_item_car_session_back_selected)).setLogEnabled(true);
            initFocusViewList();

            // 注册方控事件
            TXZWheelControlManager.getInstance().registerWheelControlListener(mControlListener);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();

        if (needScreenLock()) {
            mScreenLock.release();
        }

        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (null != mNavBtnSupporter) {
                    mNavBtnSupporter.detach();
                }
            }
        }, 0);

        // 取消注册方控事件
        TXZWheelControlManager.getInstance().unregisterWheelControlListener(mControlListener);
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
        if (!WxNavBtnHelper.getInstance().isDpadSupportEnabled()) {
            return false;
        }

        int triggeredKeyCode = event.getKeyCode();
        return (KEYCODE_DEFAULT_BACK == triggeredKeyCode || KEYCODE_DEFAULT_CONFIRM == triggeredKeyCode || KEYCODE_DEFAULT_RIGHT == triggeredKeyCode || KEYCODE_DEFAULT_LEFT == triggeredKeyCode || KEYCODE_DEFAULT_UP == triggeredKeyCode || KEYCODE_DEFAULT_DOWN == triggeredKeyCode || KEYCODE_DEFAULT_PREV == triggeredKeyCode || KEYCODE_DEFAULT_NEXT == triggeredKeyCode || KEYCODE_DEFAULT_CONFIRM_ENTER == triggeredKeyCode);
    }

    private TXZWheelControlManager.OnTXZWheelControlListener mControlListener = new TXZWheelControlManager.OnTXZWheelControlListener() {
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
    protected void onStart() {
        super.onStart();

        if (!isSystemDialog()) {
            WxWindowConfigStore.getInstance().register(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isSystemDialog()) {
            WxWindowConfigStore.getInstance().unregister(this);
        }
    }

    /**
     * 因EventBus不支持@Subscribe注解继承
     * 所有子类需要自己提供带有@Subscribe注解的onStoreChange方法, 调用基类的onStoreChange
     *
     * @param event
     */
    protected void onStoreChange(Store.StoreChangeEvent event) {
        refreshWindowParam();
    }



    private void refreshWindowParam() {
        if (isSystemDialog()) {
            /*
            * 对于系统级别的Dialog, 只关注offset配置(通过padding实现)
            * 不对显示位置配置进行响应
            * */
            getWindow().getDecorView().setPadding(
                    WxConfigStore.getInstance().getSystemDialogOffsetX(),
                    WxConfigStore.getInstance().getSystemDialogOffsetY(), 0, 0);
        } else {
            WxUIConfig uiConfig = WxWindowConfigStore.getInstance().getUIConfig();
            if (null != uiConfig) {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.width = uiConfig.width;
                lp.height = uiConfig.height;
                lp.x = uiConfig.x;
                lp.y = uiConfig.y;
                lp.gravity = uiConfig.gravity;
                lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                ScreenUtils.updateScreenSize(uiConfig.width, uiConfig.height, true);
                getWindow().setAttributes(lp);
            }
        }

    }
}
