package com.txznet.webchat.ui.car.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.strategies.IContextProxy;
import com.txznet.txz.util.focus_supporter.widgets.FocusView;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.txz.util.focus_supporter.wrappers.SimplePaddingWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.interfaces.IMessageNotification;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.util.StringUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 车机版主题消息提示界面
 * Created by J on 2016/10/19.
 */

public class CarNotificationDialog extends LinearLayout
        implements IMessageNotification, IContextProxy {
    private static final String LOG_TAG = "CarNotificationDialog";
    private static final int KEYCODE_DEFAULT_LEFT = 21;
    private static final int KEYCODE_DEFAULT_RIGHT = 22;
    private static final int KEYCODE_DEFAULT_UP = 19;
    private static final int KEYCODE_DEFAULT_DOWN = 20;
    private static final int KEYCODE_DEFAULT_PREV = 260;
    private static final int KEYCODE_DEFAULT_NEXT = 261;
    private static final int KEYCODE_DEFAULT_CONFIRM = 23;
    private static final int KEYCODE_DEFAULT_CONFIRM_ENTER = 66;
    private static final int KEYCODE_DEFAULT_BACK = 4;

    @Bind(R.id.view_car_notification_avatar)
    ImageView mIvAvatar;
    @Bind(R.id.tv_car_notification_name)
    TextView mTvName;
    @Bind(R.id.iv_car_notification_voice)
    ImageView mIvVoice;
    @Bind(R.id.view_car_notification_reply)
    ResourceButton mBtnReply;
    @Bind(R.id.rl_car_notification_root)
    RelativeLayout mRlRoot;
    @Bind(R.id.view_car_notification_root)
    View mViewRoot;

    private String targetUser;
    private OnNotificationClickListener mListener;
    private boolean bShowing;
    private ScreenLock mScreenLock;
    private WindowManager mWinManager;

    private FocusSupporter mNavBtnSupporter;
    FocusView mFocusIndicator;
    private boolean bPortraitTheme;
    private boolean bEnableDpadSupport;

    private static CarNotificationDialog sInstance;

    private CarNotificationDialog(Context context) {
        super(context);
        bEnableDpadSupport = WxNavBtnHelper.getInstance().isDpadSupportEnabled();
        bPortraitTheme = WxThemeStore.get().isPortraitTheme();
    }

    public static CarNotificationDialog getInstance() {
        if (null == sInstance) {
            synchronized (CarNotificationDialog.class) {
                if (null == sInstance) {
                    sInstance = new CarNotificationDialog(GlobalContext.get());
                }
            }
        }

        return sInstance;
    }

    private int mCurrentScreenWidthDp = -1;
    private int mCurrentScreenHeightDp = -1;
    private void initView() {
        Configuration configuration = GlobalContext.getModified().getResources().getConfiguration();
        if (mCurrentScreenWidthDp == configuration.screenWidthDp
                && mCurrentScreenHeightDp == configuration.screenHeightDp) {
            Log.i(LOG_TAG, "configuration not changed, skip re-init view");
            return;
        }

        mCurrentScreenWidthDp = configuration.screenWidthDp;
        mCurrentScreenHeightDp = configuration.screenHeightDp;
        this.removeAllViews();
        View v;
        if (bPortraitTheme) {
            v = LayoutInflater.from(GlobalContext.getModified())
                    .inflate(R.layout.layout_car_notification_win_portrait, this);
        } else {
            v = LayoutInflater.from(GlobalContext.getModified())
                    .inflate(R.layout.layout_car_notification_win, this);
        }

        ButterKnife.bind(this, v);

        init();
    }

    @SuppressLint("NewApi")
    private void init() {
        // init theme
        mBtnReply.setIconNormal(getContext().getResources()
                .getDrawable(R.drawable.src_car_record_end_icon_normal));
        mBtnReply.setIconPressed(getContext().getResources()
                .getDrawable(R.drawable.src_car_record_end_icon_pressed));

        if (bPortraitTheme) {
            mRlRoot.setBackground(getContext().getResources()
                    .getDrawable(R.drawable.shape_car_top_dialog_bg_portrait));
        } else {
            mRlRoot.setBackground(getContext().getResources()
                    .getDrawable(R.drawable.src_car_top_dialog_bg));
        }

        mIvVoice.setBackground(getContext().getResources()
                .getDrawable(R.drawable.src_car_notification_voice_anim));

        initFocusViewList();

        mWinManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mScreenLock = new ScreenLock(getContext());
        mViewRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissNotify();
                if (mListener != null) {
                    mListener.onClick(targetUser);
                }
            }
        });

        mIvAvatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissNotify();
                mViewRoot.performClick();
            }
        });

        mBtnReply.setOnBtnClickListener(new ResourceButton.OnBtnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onReply(targetUser);
                }
                WxStatusHelper.getInstance().notifyChatModeStatusChanged(false);
                dismissNotify();
            }
        });
    }

    private void initFocusViewList() {
        if (bEnableDpadSupport) {
            mFocusIndicator = new FocusView(getContext());
        }
    }

    private void refreshUserIcon() {
        if (!TextUtils.isEmpty(targetUser)) {
            WxContact con = WxContactStore.getInstance().getContact(targetUser);

            WxImageLoader.loadHead(getContext(), con, mIvAvatar);
        }

    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        if (event.getType().equals(WxResourceStore.EVENT_TYPE_ALL)) {
            refreshUserIcon();
        }
    }

    @Override
    public void showNotify(final WxNotificationInfo info) {
        initView();
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (info.hasSpeak) {
                    mTvName.setText(StringUtil.handleLength(info.nick, 18, "..."));
                    targetUser = info.openId;
                    startVoiceAnimation();
                    refreshUserIcon();

                    if (!bShowing) {
                        show();
                    }

                } else {
                    stopVoiceAnimation();
                }
            }
        }, 0);

    }

    @Override
    public void dismissNotify() {
        if (!bShowing) {
            return;
        }

        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                stopVoiceAnimation();
                dismiss();
            }
        }, 0);

    }

    private void show() {
        if (bShowing) {
            return;
        }

        WxResourceStore.get().register(this);
        bShowing = true;
        mScreenLock.lock();

        WindowManager.LayoutParams layoutParam = genLayoutParams();
        mWinManager.addView(this, layoutParam);

        if (bEnableDpadSupport) {
            // 添加焦点指示
            WindowManager.LayoutParams focusLayoutParam = genFocusLayoutParams();
            mWinManager.addView(mFocusIndicator, focusLayoutParam);


            initNavBtnSupporter();
            // 注册方控监听
            TXZWheelControlManager.getInstance().registerWheelControlListener(mControlListener);
        }
    }

    private WindowManager.LayoutParams mFocusParams;

    private WindowManager.LayoutParams genFocusLayoutParams() {
        if (null == mFocusParams) {
            mFocusParams = new WindowManager.LayoutParams();
            mFocusParams.type = WxConfigStore.getInstance().getSystemDialogWindowType();
            mFocusParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
            mFocusParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
            mFocusParams.format = PixelFormat.RGBA_8888;
            mFocusParams.flags = mLayoutParams.flags
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }

        return mFocusParams;
    }

    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager.LayoutParams genLayoutParams() {
        if (null == mLayoutParams) {
            int width, height;
            if (bPortraitTheme) {
                width = (int) GlobalContext.getModified().getResources().getDimension(R.dimen.x800);
                height = (int) GlobalContext.getModified().getResources().getDimension(R.dimen.y80);
            } else {
                width = (int) GlobalContext.getModified().getResources().getDimension(R.dimen.x496);
                height = (int) GlobalContext.getModified().getResources().getDimension(R.dimen.y80);
            }

            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.type = WxConfigStore.getInstance().getSystemDialogWindowType();
            mLayoutParams.width = width;
            mLayoutParams.height = height;
            mLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mLayoutParams.flags |= WxConfigStore.getInstance().getSystemDialogWindowFlag();

            mLayoutParams.x = WxConfigStore.getInstance().getSystemDialogOffsetX();
            mLayoutParams.y = WxConfigStore.getInstance().getSystemDialogOffsetY();

            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        }

        return mLayoutParams;
    }


    private void initNavBtnSupporter() {
        mNavBtnSupporter = FocusSupporter.attach(this).setFocusDrawable(
                GlobalContext.get().getResources().getDrawable(R.drawable.ic_nav_indicator_round))
                .setLogEnabled(true);
        mNavBtnSupporter.setViewList(
                new SimplePaddingWrapper(mIvAvatar,
                        getResources().getDrawable(R.drawable.ic_nav_indicator_round),
                        new int[]{5, 5, 5, 5}),
                new SimpleDrawableWrapper(mBtnReply,
                        getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_right)));

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            mNavBtnSupporter.setCurrentFocus(mBtnReply);
        }
    }

    private void dismiss() {
        if (bShowing) {
            bShowing = false;
            mWinManager.removeView(this);
            mScreenLock.release();

            WxResourceStore.get().unregister(this);

            if (bEnableDpadSupport) {
                mWinManager.removeView(mFocusIndicator);
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        mNavBtnSupporter.detach();
                    }
                }, 0);

                // 取消注册方控监听
                TXZWheelControlManager.getInstance()
                        .unregisterWheelControlListener(mControlListener);
            }
        }
    }


    @Override
    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        mListener = listener;
    }

    private void startVoiceAnimation() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable ad = (AnimationDrawable) mIvVoice.getBackground();
                ad.selectDrawable(0);
                ad.start();
            }
        }, 0);

    }

    private void stopVoiceAnimation() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable ad = (AnimationDrawable) mIvVoice.getBackground();
                ad.stop();
                ad.selectDrawable(0);
            }
        }, 0);

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
        if (!bEnableDpadSupport) {
            if (KEYCODE_DEFAULT_BACK == event.getKeyCode()) {
                performBack();
            }

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
    public void onAttach() {
    }

    @Override
    public void onDetach() {
    }

    @Override
    public void updateFocusIndicator(int l, int t, int r, int b) {
        mFocusIndicator.setVisibility(View.VISIBLE);

        mFocusIndicator.setFocusPosition(l, t, r, b);
    }

    @Override
    public void showFocusIndicator() {
        mFocusIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFocusIndicator() {
        mFocusIndicator.setVisibility(View.GONE);
    }

    @Override
    public View getViewById(int id) {
        return findViewById(id);
    }

    @Override
    public void performBack() {
        MessageActionCreator.get().skipCurrentMessage();
        dismissNotify();
    }

    @Override
    public void setIndicatorDrawable(Drawable drawable) {
        mFocusIndicator.setFocusDrawable(drawable);
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
}
