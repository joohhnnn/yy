package com.txznet.webchat.ui.base.widgets;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
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
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.webchat.R;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.util.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 推送登录提示Dialog
 * Created by J on 2016/10/19.
 */

public class PushLoginNotificationDialog extends LinearLayout {
    private static final int KEYCODE_DEFAULT_CONFIRM = 23;
    private static final int KEYCODE_DEFAULT_BACK = 4;

    private static final int SHOW_TIME_MAX = 21 * 1000; // 最长显示时间，超时自动关闭

    @Bind(R.id.view_push_login_notification_avatar)
    ImageView mIvAvatar;
    @Bind(R.id.tv_push_login_notification_title)
    TextView mTvTitle;
    @Bind(R.id.rl_push_login_notification_root)
    RelativeLayout mRlRoot;

    private boolean bShowing;
    private ScreenLock mScreenLock;
    private WindowManager mWinManager;

    private static PushLoginNotificationDialog sInstance;

    private PushLoginNotificationDialog(Context context) {
        super(context);
        initView();
    }

    public static PushLoginNotificationDialog getInstance() {
        if (null == sInstance) {
            synchronized (PushLoginNotificationDialog.class) {
                if (null == sInstance) {
                    sInstance = new PushLoginNotificationDialog(GlobalContext.get());
                }
            }
        }

        return sInstance;
    }

    private void initView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_push_login_notification_win, this);
        ButterKnife.bind(this, v);

        init();
    }

    private void init() {
        mWinManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mScreenLock = new ScreenLock(getContext());
        mRlRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                UIHandler.getInstance().showAppStart(false);
            }
        });
    }

    private String mAvatarStr = "";
    private void updatePushStatus() {
        String avatarStr = WxQrCodeStore.get().getScannerPicStr();
        if (!TextUtils.isEmpty(avatarStr) && !avatarStr.equals(mAvatarStr)) {
            mAvatarStr = avatarStr;
        }

        /*mIvAvatar.post(new Runnable() {
            @Override
            public void run() {
                mIvAvatar.setImageBitmap(Base64Converter.string2Bitmap(mAvatarStr));
            }
        });*/

        ImageUtil.showImageString(mIvAvatar, mAvatarStr, R.drawable.default_headimage);

        if (WxQrCodeStore.get().isPushLoginRequesting()) {
            mTvTitle.setText(R.string.lb_push_login_dialog_pushing);
        } else {
            mTvTitle.setText(R.string.lb_push_login_dialog_success);
        }
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        if (event.getType().equals(WxQrCodeStore.EVENT_TYPE_ALL)) {
            updatePushStatus();
        }
    }

    public boolean isShowing() {
        return bShowing;
    }

    public void show(String avatar) {
        if (bShowing) {
            return;
        }
        this.mAvatarStr = avatar;
        WxQrCodeStore.get().register(this);
        bShowing = true;
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
        layoutParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParam.width = LayoutParams.WRAP_CONTENT;
        layoutParam.height = LayoutParams.WRAP_CONTENT;
        layoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mWinManager.addView(this, layoutParam);
        mScreenLock.lock();
        // 注册方控监听
        TXZWheelControlManager.getInstance().registerWheelControlListener(mControlListener);

        AppLogic.removeUiGroundCallback(mDismissTask);
        AppLogic.runOnUiGround(mDismissTask, SHOW_TIME_MAX);
    }

    public void dismiss() {
        if (bShowing) {
            AppLogic.removeUiGroundCallback(mDismissTask);
            bShowing = false;
            mWinManager.removeView(this);
            mScreenLock.release();

            WxQrCodeStore.get().unregister(this);

            // 取消注册方控监听
            TXZWheelControlManager.getInstance().unregisterWheelControlListener(mControlListener);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {

            case KEYCODE_DEFAULT_BACK:
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    dismiss();
                }

                return true;

            case KEYCODE_DEFAULT_CONFIRM:
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    mRlRoot.performClick();
                }

                return true;
        }


        return super.dispatchKeyEvent(event);
    }

    private TXZWheelControlManager.OnTXZWheelControlListener mControlListener = new TXZWheelControlManager.OnTXZWheelControlListener() {
        @Override
        public void onKeyEvent(final int eventId) {

            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    switch (eventId) {
                        case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
                            dismiss();
                            break;

                        case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
                            mRlRoot.performClick();
                            break;
                    }
                }
            }, 0);

        }
    };

    private Runnable mDismissTask = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };
}
