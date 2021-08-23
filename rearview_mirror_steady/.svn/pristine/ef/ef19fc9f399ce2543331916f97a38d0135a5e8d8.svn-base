package com.txznet.webchat.ui.car.widget;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.sp.TipManager;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.interfaces.IRecordWin;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.ui.common.widget.VoiceView;

import butterknife.Bind;

/**
 * 微信后台录音界面
 *
 * 微信处于后台运行状态时启动录音, 显示的录音界面
 *
 * Created by J on 2016/10/17.
 */

public class CarRecordDialog extends AppBaseWinDialog implements IRecordWin {
    @Bind(R.id.ll_car_record_root)
    LinearLayout mLlRoot;
    @Bind(R.id.view_car_record_avatar)
    ImageView mIvAvatar;
    @Bind(R.id.view_car_record_end)
    ResourceButton mBtnEnd;
    @Bind(R.id.tv_car_record_time)
    TextView mTvTime;
    @Bind(R.id.view_car_record_sound)
    VoiceView mViewSound;
    @Bind(R.id.rl_car_record_bar)
    RelativeLayout mRlBar;

    private static CarRecordDialog sInstance;

    // 录音提示文本, 此文本可能包含用于剩余发送时间替换的占位符, 所以在用于界面显示之前需要做对应处理
    private String mTipTextRaw;
    // 录音提示文本是否需要自动隐藏, 因为sdk可能修改录音的提示文本, 若修改后的文本中不包含剩余自动发送秒数
    // 的占位符, 此时用户说话自动隐藏提示文本的交互会很奇怪, 所以用此标志位做下记录, 对应场景下不再对提示文本
    // 做自动隐藏处理
    private boolean bEnableTipVisibilityChange;

    private boolean bSending = false;

    public static CarRecordDialog getInstance() {
        if (null == sInstance) {
            synchronized (CarRecordDialog.class) {
                if (null == sInstance) {
                    sInstance = new CarRecordDialog();
                }
            }
        }

        return sInstance;
    }

    private CarRecordDialog() {
        super(true);
    }

    @Override
    protected boolean isSystemDialog() {
        return true;
    }

    @Override
    protected boolean needScreenLock() {
        return true;
    }

    @Override
    public int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.layout_car_record_win_portrait;
        }

        return R.layout.layout_car_record_win;
    }

    @SuppressLint("NewApi")
    @Override
    public void init() {
        // init theme
        if (WxThemeStore.get().isPortraitTheme()) {
            mRlBar.setBackground(getContext().getResources()
                    .getDrawable(R.drawable.shape_car_top_dialog_bg_portrait));
        } else {
            mRlBar.setBackground(getContext().getResources()
                    .getDrawable(R.drawable.src_car_top_dialog_bg));
        }


        mBtnEnd.setIconNormal(getContext().getResources()
                .getDrawable(R.drawable.src_car_record_end_icon_normal));
        mBtnEnd.setIconPressed(getContext().getResources()
                .getDrawable(R.drawable.src_car_record_end_icon_pressed));


        mBtnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bSending) {
                    bSending = true;
                    MessageActionCreator.get().sendVoice(true);
                }
            }
        });

        mLlRoot.setClickable(true);
        mLlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bSending = false;
                MessageActionCreator.get().cancelReply(true);
            }
        });
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(
                new SimpleDrawableWrapper(mBtnEnd, getContext().getResources()
                        .getDrawable(R.drawable.ic_nav_indicator_dialog_right)));

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mBtnEnd);
        }
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
        // 若tip不包含剩余自动发送时间的占位符, 就不开启自动隐藏逻辑
        bEnableTipVisibilityChange = mTipTextRaw.contains(TipManager.PLACEHOLDER_ARG0);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mViewSound.stop();
        MessageActionCreator.get().cancelReply(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MessageActionCreator.get().cancelReply(true);
    }

    @Override
    public void updateTargetInfo(String openId) {
        final WxContact con = WxContactStore.getInstance().getContact(openId);

        WxImageLoader.loadHead(getContext(), con, mIvAvatar);
    }

    @Override
    public void refreshTimeRemain(final int seconds) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (seconds >= 3) {
                    mBtnEnd.setAlpha(1f);
                    mBtnEnd.setEnabled(true);
                    if (bEnableTipVisibilityChange) {
                        mTvTime.setVisibility(View.GONE);
                    }
                    mViewSound.start();
                } else if (seconds > 0) {
                    mTvTime.setVisibility(View.VISIBLE);
                    mViewSound.stop();
                    mTvTime.setText(getCountdownText(seconds));
                } else {
                    mBtnEnd.setAlpha(0.2f);
                    mBtnEnd.setEnabled(false);
                    bSending = true;
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

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
