package com.txznet.webchat.ui.car.widget;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.sp.TipManager;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.interfaces.IRecordWin;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;
import com.txznet.webchat.ui.common.widget.VoiceView;

import butterknife.Bind;

/**
 * 微信前台录音界面
 *
 * 微信处于前台状态下启动录音时显示的录音界面
 *
 * Created by J on 2016/10/20.
 */

public class CarRecordDialogInner extends AppBaseWinDialog implements IRecordWin {
    private static CarRecordDialogInner sInstance;

    @Bind(R.id.rl_car_record_inner_root)
    RelativeLayout mRlRoot;
    @Bind(R.id.ll_car_record_inner_dialog_container)
    LinearLayout mLlDialogContainer;
    @Bind(R.id.tv_car_record_inner_time)
    TextView mTvTime;
    @Bind(R.id.fl_car_record_inner_end)
    FrameLayout mFlEnd;
    @Bind(R.id.view_car_record_inner_sound)
    VoiceView mViewSound;

    private boolean bSending;

    // 录音提示文本, 此文本可能包含用于剩余发送时间替换的占位符, 所以在用于界面显示之前需要做对应处理
    private String mTipTextRaw;

    public static CarRecordDialogInner getInstance() {
        if (null == sInstance) {
            synchronized (CarRecordDialogInner.class) {
                if (null == sInstance) {
                    sInstance = new CarRecordDialogInner();
                }
            }
        }

        return sInstance;
    }

    @Override
    protected boolean needScreenLock() {
        return true;
    }

    private CarRecordDialogInner() {
        super(true);
    }

    @Override
    public int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.layout_car_record_dialog_foreground_portrait;
        }

        return R.layout.layout_car_record_dialog_foreground;
    }

    @Override
    public void init() {
        mFlEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bSending) {
                    bSending = true;
                    MessageActionCreator.get().sendVoice(true);
                }
            }
        });

        mRlRoot.setClickable(true);
        mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSending = false;
                MessageActionCreator.get().cancelReply(true);
            }
        });

        initTheme();
    }

    private void initTheme() {
        mLlDialogContainer.setBackground(getContext().getResources().getDrawable(R.drawable.shape_car_dialog_bg));
        mFlEnd.setBackground(getContext().getResources().getDrawable(R.drawable.selector_car_dialog_button_bg));
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(new SimpleDrawableWrapper(mFlEnd, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_commit)));

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mFlEnd);
        }
    }

    @Override
    public void updateTargetInfo(String openId) {

    }

    @Override
    public void refreshTimeRemain(final int seconds) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {

                if (seconds >= 3) {
                    mTvTime.setText(getCountdownText(seconds));
                    mViewSound.start();
                } else if (seconds > 0) {
                    mViewSound.stop();
                    mTvTime.setVisibility(View.VISIBLE);
                    mTvTime.setText(getCountdownText(seconds));
                } else {
                    mViewSound.stop();
                    mTvTime.setVisibility(View.VISIBLE);
                    mTvTime.setText("正在发送");
                }

            }
        }, 0);
    }

    private String getCountdownText(int countdown) {
        return mTipTextRaw.replaceAll(TipManager.PLACEHOLDER_ARG0, countdown + "");
    }

    @Override
    public void show() {
        if (isShowing()) {
            return;
        }

        // 每次show时更新下录音提示文本相关状态
        updateTipStatus();

        bSending = false;
        super.show();
        mViewSound.start();
    }

    private void updateTipStatus() {
        mTipTextRaw = TipManager.getTip(TipManager.KEY_TIP_UI_RECORD_COUNTDOWN_TEXT);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MessageActionCreator.get().cancelReply(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mViewSound.stop();
        MessageActionCreator.get().cancelReply(true);
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
