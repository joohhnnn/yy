package com.txznet.webchat.ui.car;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.ui.dialog.WinProcessing;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTopActivity3;
import com.txznet.reserve.activity.ReserveSingleTopActivity4;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.txz.util.focus_supporter.wrappers.SimplePaddingWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.stores.WxServerConfigStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.car.widget.TabView;
import com.txznet.webchat.ui.common.widget.user_picker.UserPickerDialog;
import com.txznet.webchat.ui.rearview_mirror.BindReasonActivity;
import com.txznet.webchat.ui.rearview_mirror.widget.ViewPagerEx;
import com.txznet.webchat.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 扫码和远程控制页面
 * Created by J on 2016/10/8.
 */

public class Car_QRCodeActivity extends AppBaseActivity {
    public static final String INTENT_KEY_TARGET_PAGE = "intent_key_target_page";
    public static final String TARGET_PAGE_WECHAT = "wechat";
    public static final String TARGET_PAGE_CONTROL = "remote_control";

    @Bind(R.id.view_car_qr_tab)
    TabView mViewTab;
    @Bind(R.id.vp_car_qr)
    ViewPagerEx mViewPager;

    // views in ViewPager
    View mViewLoginWechat;
    View mViewLoginControl;

    // view components in login wechat page
    RelativeLayout mRlPanelPicker;
    RelativeLayout mRlPanelScan;
    RelativeLayout mRlPanelConfirm;
    // view components in picker
    ImageView mIvPushAvatar;
    FrameLayout mBtnPushConfirm;
    // view components in scan
    ImageView mIvQRCode;
    View mViewQRCodeClickHint;
    FrameLayout mBtnRefreshWechatQR;
    ProgressWheel mPbWechat;
    TextView mTvQRErrorTip;
    //view components in confirm
    TextView mTvConfirmTitle;
    TextView mTvConfirmTip;
    ImageView mIvUserAvatar;
    FrameLayout mBtnRefreshControlQR;
    Button mBtnBack;

    // view components in login control page
    ImageView mIvControlQRCode;
    TextView mTvControlTip; // 二维码旁边的文字提示
    TextView mTvControlHelp; // "扫描二维码能做什么"
    ProgressWheel mPbControl; // 二维码加载进度

    // 提示dialog
    private WinProcessing mWinProcessing;
    // 获取远程控制绑定二维码失败对话框
    public WinNotice mWinGetFailure;

    // 用户选择dialog
    private UserPickerDialog mUserPicker;

    // 只有真正应用退出时才需要广播通知, 因为有扫码登录微信跳转主页面后finish的情况,
    // 需要用此标志位进行下记录, 跳转主页面的情况不进行广播通知
    private boolean bBroadOnDestroy = true;

    @Override
    protected int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.activity_car_qr_portrait;
        }

        return R.layout.activity_car_qr;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                WxLoginStore.get(),
                TXZBindStore.get(),
                AppStatusStore.get(),
                WxQrCodeStore.get(),
                WxConfigStore.getInstance(),
        };
    }

    public static void show(Context context) {
        show(context, TARGET_PAGE_WECHAT);
    }

    public static void show(Context context, String targetPage) {
        Intent intent = new Intent();

        // 如果不是指定远程控制页面, 统一默认指向登录微信
        if (TARGET_PAGE_CONTROL.equals(targetPage)) {
            intent.putExtra(INTENT_KEY_TARGET_PAGE, TARGET_PAGE_CONTROL);
        } else {
            intent.putExtra(INTENT_KEY_TARGET_PAGE, TARGET_PAGE_WECHAT);
        }

        intent.setClass(context, ReserveSingleTopActivity3.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        if (WxLoginStore.get().isLogin()) {
            intentMainPage();
        } else {
            // 根据免扫码开关决定更新二维码还是用户列表
            if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
                LoginActionCreator.get().getUserCacheList();
            } else {
                LoginActionCreator.get().refreshQRCode();
            }

            initViewPager();
            updateBindingInfo();
            showWechatQR();
            updateTabEntry();
            initTheme();
        }
    }

    @Override
    protected void initFocusViewList() {
        refreshFocusViewList();
    }

    @Override
    protected void refreshFocusViewList() {
        if (!isDpadSupportEnabled()) {
            return;
        }

        List<Object> vList = new ArrayList<>();
        if (0 == mViewPager.getCurrentItem()) {
            // 根据当前页面状态设置ViewList
            if (View.VISIBLE == mRlPanelPicker.getVisibility()) {
                // 用户选择页
                vList.add(new SimpleDrawableWrapper(mIvPushAvatar, getResources().getDrawable(R.drawable.ic_nav_indicator_round)));
                vList.add(new SimplePaddingWrapper(mBtnPushConfirm, getResources().getDrawable(R.drawable.ic_nav_indicator_round), new int[]{2, 2, 2, 2}));
            } else if (View.VISIBLE == mRlPanelScan.getVisibility()) {
                // 扫码页面
                if (bUserPickerEnabled) {
                    vList.add(new SimpleDrawableWrapper(mIvQRCode, getResources().getDrawable(R.drawable.ic_nav_indicator_rect)));
                }
                vList.add(new SimplePaddingWrapper(mBtnRefreshWechatQR, getResources().getDrawable(R.drawable.ic_nav_indicator_round), new int[]{2, 2, 2, 2}));
            } else {
                // 确认登录页面
                vList.add(new SimplePaddingWrapper(mBtnBack, getResources().getDrawable(R.drawable.ic_nav_indicator_rect), new int[]{10, 6, 10, 6}));
            }
        } else {
            vList.add(new SimplePaddingWrapper(mBtnRefreshControlQR, getResources().getDrawable(R.drawable.ic_nav_indicator_round), new int[]{2, 2, 2, 2}));
        }

        getNavBtnSupporter().setViewList(vList);

        // 根据当前显示的ViewPager页面设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            if (0 == mViewPager.getCurrentItem()) {
                if ((View.VISIBLE == mRlPanelScan.getVisibility() && !bUserPickerEnabled) || View.VISIBLE == mRlPanelConfirm.getVisibility()) {
                    getNavBtnSupporter().setCurrentFocus(vList.get(0));
                } else {
                    getNavBtnSupporter().setCurrentFocus(vList.get(1));
                }

            } else {
                getNavBtnSupporter().setCurrentFocus(mBtnRefreshControlQR);
            }
        }

    }

    private void initIntentTargetPage() {
        Intent intent = getIntent();

        if (null != intent) {
            String targetPage = intent.getStringExtra(INTENT_KEY_TARGET_PAGE);

            // 不是指定远程控制的intent都显示默认页面
            if (TARGET_PAGE_CONTROL.equals(targetPage)) {
                mViewTab.switchToTab(1, false);
            }
        }
    }

    @SuppressLint("NewApi")
    private void initTheme() {
        mPbControl.setBarColor(getResources().getColor(R.color.color_accent));
        mPbWechat.setBarColor(getResources().getColor(R.color.color_accent));

        mBtnRefreshWechatQR.setBackground(getResources().getDrawable(R.drawable.selector_car_btn));
        mBtnRefreshControlQR.setBackground(getResources().getDrawable(R.drawable.selector_car_btn));
        mBtnPushConfirm.setBackground(getResources().getDrawable(R.drawable.selector_car_btn));
        mBtnBack.setTextColor(getResources().getColor(R.color.color_accent));
        mTvControlHelp.setTextColor(getResources().getColor(R.color.color_accent));

        if (!WxConfigStore.getInstance().isTabVisible()) {
            mViewTab.setVisibility(View.GONE);
        }

        mViewPager.setBackground(getResources().getDrawable(R.drawable.shape_activity_background_login));
    }

    @Override
    protected boolean onFocusOperation(int op) {
        if (null != getNavBtnSupporter().getCurrentFocus()) {
            if (FocusSupporter.NAV_BTN_RIGHT == op && mViewPager.getCurrentItem() == 0) {
                mViewTab.switchToTab(1);
                return true;
            } else if (FocusSupporter.NAV_BTN_LEFT == op && mViewPager.getCurrentItem() == 1) {
                mViewTab.switchToTab(0);
                return true;
            } else if (FocusSupporter.NAV_BTN_NEXT == op) {
                if (getNavBtnSupporter().isOnFocus(mBtnRefreshWechatQR) || getNavBtnSupporter().isOnFocus(mBtnPushConfirm)) {
                    mViewTab.switchToTab(1);
                    return true;
                }

            } else if (FocusSupporter.NAV_BTN_PREV == op) {
                if (getNavBtnSupporter().isOnFocus(mBtnRefreshControlQR)) {
                    mViewTab.switchToTab(0);
                    return true;
                }
            }
        }

        return super.onFocusOperation(op);
    }

    private void initNavBtnSupporter() {
        refreshFocusViewList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoginActionCreator.get().doWakeup();

        if (WxLoginStore.get().isLogin()) {
            intentMainPage();
        } else {
            //LoginActionCreator.get().getQrCode();
            updateQRStatus();
        }

        updateTabEntry();
        updateBindingInfo();
        initIntentTargetPage();
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            // 远程控制绑定信息改变
            case TXZBindStore.EVENT_TYPE_ALL:
                updateBindingInfo();
                break;

            // 二维码信息发生改变
            case WxQrCodeStore.EVENT_TYPE_ALL:
                updateQRStatus();
                break;

            // 微信配置发生改变
            case AppStatusStore.EVENT_TYPE_ALL:
                //updateTabEntry();
                if (AppStatusStore.get().isUIAsrEnabled()) {
                    if (hasFocus()) {
                        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrLoginPageCtrlCallback);
                    }
                } else {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_LOGIN_PAGE_CTRL");
                }
                break;

            // 微信tab开关状态发生改变
            case WxConfigStore.EVENT_TYPE_ALL:
                updateTabEntry();
                break;

            // 微信登陆信息发生改变
            case WxLoginStore.EVENT_TYPE_ALL:
                checkLoginStatus();
                break;
        }
    }

    private void checkLoginStatus() {
        L.d("checkLoginStatus");
        if (!WxLoginStore.get().isLogin()) {
            L.d("didn't login");
            return;
        }
        intentMainPage();
    }

    private void intentMainPage() {
        Intent intent = new Intent();
        intent.setClass(this, ReserveSingleTopActivity4.class);
        startActivity(intent);
        bBroadOnDestroy = false;
        this.finish();
    }


    /**
     * 更新微信二维码状态
     */
    private void updateQRStatus() {
        if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
            updateUserPicker();
        }

        if (WxQrCodeStore.get().isPushLoginRequesting() || WxQrCodeStore.get().isPushLoginSuccess()) {
            showPushLogin();
        } else if (WxQrCodeStore.get().isScanned()) {
            // 用户已扫码，显示信息
            showUserAvatar();
        } else if (null != WxQrCodeStore.get().getCurrentUserCache() && WxServerConfigStore.getInstance().isPushLoginEnabled()) {
            showUserSelect();
        } else {
            // 二维码正在获取/已经获取，显示扫码界面
            showWechatQR();

            // 如果还停留在扫码成功页面，自动后退到扫码页面
            if (View.VISIBLE == mRlPanelConfirm.getVisibility()) {
                mBtnBack.performClick();
            }
        }
    }

    private boolean bUserPickerEnabled = false;

    private void updateUserPicker() {
        // 更新UserPickerDialog数据
        List<WxUserCache> cacheList = WxQrCodeStore.get().getUserCacheList();

        if (null != cacheList && !cacheList.isEmpty()) {
            bUserPickerEnabled = true;
            // 初始化UserPicker
            if (null == mUserPicker) {
                mUserPicker = new UserPickerDialog();
            }
            mUserPicker.setUserList(cacheList);
        } else {
            bUserPickerEnabled = false;
        }

        // 更新二维码下的箭头标志
        if (null != mViewQRCodeClickHint) {
            mViewQRCodeClickHint.setVisibility(bUserPickerEnabled ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 显示用户选择
     */
    private void showUserSelect() {
        // 判断本地缓存是否有效
        WxUserCache cache = WxQrCodeStore.get().getCurrentUserCache();
        if (null != cache) {
            mRlPanelPicker.setVisibility(View.VISIBLE);
            mRlPanelScan.setVisibility(View.GONE);
            mRlPanelConfirm.setVisibility(View.GONE);

            // 显示用户信息
            /*mIvUserAvatar.post(new Runnable() {
                @Override
                public void run() {
                    mIvPushAvatar.setImageBitmap(Base64Converter.string2Bitmap(WxQrCodeStore.get().getCurrentUserCache().getUserHead()));
                }
            });*/

            ImageUtil.showImageString(mIvPushAvatar, WxQrCodeStore.get().getCurrentUserCache().getUserHead(), R.drawable.default_headimage);

            refreshFocusViewList();

            // 设置默认焦点
            //// TODO: 2017/5/3 nav
            /*if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
                getNavBtnSupporter().setCurrentFocusIndex(1);
            }*/
        }
    }

    /**
     * 用户扫码成功，提示手机确认
     */
    private void showUserAvatar() {
        mRlPanelPicker.setVisibility(View.GONE);
        mRlPanelScan.setVisibility(View.GONE);
        mRlPanelConfirm.setVisibility(View.VISIBLE);

        // 设置tip
        mTvConfirmTitle.setText(getString(R.string.lb_login_wechat_scan_success));
        mTvConfirmTip.setText(getString(R.string.lb_login_wechat_scan_success2));

        /*mIvUserAvatar.post(new Runnable() {
            @Override
            public void run() {
                mIvUserAvatar.setImageBitmap(Base64Converter.string2Bitmap(WxQrCodeStore.get().getScannerPicStr()));
            }
        });*/

        ImageUtil.showImageString(mIvUserAvatar, WxQrCodeStore.get().getScannerPicStr(), R.drawable.default_headimage);

        refreshFocusViewList();

        // 设置焦点View
        // // TODO: 2017/5/3 nav
        /*if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocusIndex(0);
        }*/
    }

    /**
     * 显示推送登录状态
     */
    private void showPushLogin() {
        mRlPanelPicker.setVisibility(View.GONE);
        mRlPanelScan.setVisibility(View.GONE);
        mRlPanelConfirm.setVisibility(View.VISIBLE);

        if (WxQrCodeStore.get().isPushLoginRequesting()) {
            mTvConfirmTitle.setText(getString(R.string.lb_login_wechat_push_login_tip_title));
            mTvConfirmTip.setText(getString(R.string.lb_login_wechat_push_login_tip));
        } else if (WxQrCodeStore.get().isPushLoginSuccess()) {
            mTvConfirmTitle.setText(getString(R.string.lb_login_wechat_push_login_tip_title));
            mTvConfirmTip.setText(getString(R.string.lb_login_wechat_scan_success2));
        }

        /*mIvUserAvatar.post(new Runnable() {
            @Override
            public void run() {
                mIvUserAvatar.setImageBitmap(Base64Converter.string2Bitmap(WxQrCodeStore.get().getScannerPicStr()));
            }
        });*/

        ImageUtil.showImageString(mIvUserAvatar, WxQrCodeStore.get().getScannerPicStr(), R.drawable.default_headimage);

        refreshFocusViewList();

        // 设置焦点View
        //// TODO: 2017/5/3 nav
        /*if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocusIndex(0);
        }*/
    }


    /**
     * 显示微信登陆二维码
     */
    private void showWechatQR() {
        // hide user avatar panel & show qrcode panel
        mRlPanelPicker.setVisibility(View.GONE);
        mRlPanelConfirm.setVisibility(View.GONE);
        mRlPanelScan.setVisibility(View.VISIBLE);


        if (WxQrCodeStore.get().isRetrieving()) {
            mTvQRErrorTip.setVisibility(View.GONE);
            mPbWechat.setVisibility(View.VISIBLE);
        } else if (WxQrCodeStore.get().isQrCodeInvalid()) {
            mTvQRErrorTip.setVisibility(View.VISIBLE);
        } else {
            mTvQRErrorTip.setVisibility(View.GONE);
            // re-enable refresh QRCode button
            AppLogic.removeUiGroundCallback(mEnableRefreshQRCodeTask);
            mBtnRefreshWechatQR.setEnabled(true);

            // hide progress bar
            mPbWechat.setVisibility(View.GONE);

            // show QRCode
            String QRStr = WxQrCodeStore.get().getQrCode();
            if (!TextUtils.isEmpty(QRStr)) {
                final Bitmap mQRBitmap;
                try {
                    mQRBitmap = QRUtil.createQRCodeBitmap(QRStr, (int) getResources().getDimension(R.dimen.y180));
                    //mQRBitmap = QRCodeHandler.createQRCode(QRStr, (int) getResources().getDimension(R.dimen.y180));
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mIvQRCode.setImageBitmap(mQRBitmap);
                        }
                    }, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mPbWechat.setVisibility(View.VISIBLE);
            }
        }

        refreshFocusViewList();

        // 设置默认焦点
        //// TODO: 2017/5/3 nav
        /*if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            if (bUserPickerEnabled) {
                getNavBtnSupporter().setCurrentFocusIndex(1);
            } else {
                getNavBtnSupporter().setCurrentFocusIndex(0);
            }

        }*/
    }

    /**
     * 更新远程控制页面显示信息
     */
    private void updateBindingInfo() {
        // 检查是否第一次获取二维码
        if (TXZBindStore.get().isFirstTimeLoading()) {
            mWinProcessing = new WinProcessing(getResources().getString(R.string.tip_login_control_get_bind_info)) {
                @Override
                public void onCancelProcess() {
                    dismiss();
                }
            };
            mWinProcessing.show();
        } else {
            if (mWinProcessing != null && mWinProcessing.isShowing()) {
                mWinProcessing.dismiss();
                mWinProcessing = null;
            }
        }

        // 若是第一次绑定失败， 显示退出对话框
        if (TXZBindStore.get().isFirstTimeException()) {
            mWinGetFailure = new WinNotice(false) {
                @Override
                public void onClickOk() {
                    // 释放资源
                    this.dismiss();
                    if (mWinProcessing != null) {
                        mWinProcessing.dismiss();
                        mWinProcessing = null;
                    }
                    ActivityStack.getInstance().exit();
                }

            };
            mWinGetFailure.setMessage(getResources().getString(R.string.tip_login_control_get_bind_info_failed)).show();

            if (AppLogic.isForeground()) {
                // 发送获取绑定信息失败广播
                WxStatusHelper.getInstance().notifyGetBindMsgFailed();
            }

            return;
        }

        // 更新二维码和右边提示信息
        if (TXZBindStore.get().isException()) {
            mTvControlTip.setText(R.string.lb_login_control_failed);
        } else if (TXZBindStore.get().isWaitingResp()) {
            mPbControl.setVisibility(View.VISIBLE);
        } else {
            showControlQR();

            // 判断绑定信息
            L.d("getting bind info: isBind = " + TXZBindStore.get().hasBind());
            if (TXZBindStore.get().hasBind()) {
                mTvControlTip.setText(String.format(getResources().getString(R.string.lb_login_control_bound), TXZBindStore.get().getBindNick()));
            } else {
                mTvControlTip.setText(R.string.lb_login_control_unbind);
            }
        }
    }

    /**
     * 显示远程控制二维码
     */
    private void showControlQR() {
        //hide progress bar
        mPbControl.setVisibility(View.GONE);

        String QRStr = TXZBindStore.get().getBindUrl();
        if (!TextUtils.isEmpty(QRStr)) {
            final Bitmap mQRBitmap;
            try {
                mQRBitmap = QRUtil.createQRCodeBitmap(QRStr, (int) getResources().getDimension(R.dimen.y180));
                //mQRBitmap = QRCodeHandler.createQRCode(QRStr, (int) getResources().getDimension(R.dimen.y180));
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        //mIvQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
                        mIvControlQRCode.setImageBitmap(mQRBitmap);
                    }
                }, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更新远程控制页面入口信息
     */
    private void updateTabEntry() {
        /*if (BuildConfig.FORCE_WECHAT_MODE) {
            mViewTab.hideTab(1);
            mViewTab.showTab(0);
            refreshNavViewList();
            return;
        }
        if (AppStatusStore.get().bShowWxEntryTab()) {
            mViewTab.showTab(0);
        } else {
            mViewTab.hideTab(0);
        }*/

        if (WxConfigStore.getInstance().isWxTabEnabled()) {
            mViewTab.showTab(0);
        } else {
            mViewTab.hideTab(0);
            mViewTab.switchToTab(1, false);
        }

        if (WxConfigStore.getInstance().isControlTabEnabled()) {
            mViewTab.showTab(1);
        } else {
            mViewTab.hideTab(1);
            mViewTab.switchToTab(0, false);
        }

        refreshFocusViewList();
    }

    private void initViewPager() {
        List<View> mViewList = new ArrayList<View>();
        //inflate layouts
        initPagerViews();
        mViewList.add(mViewLoginWechat);
        mViewList.add(mViewLoginControl);

        //init pager adapter
        Car_QRCodeActivity.ViewPagerAdapter mViewPagerAdapter = new Car_QRCodeActivity.ViewPagerAdapter(this, mViewList);

        //bind adapter & tab
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(1);
        mViewTab.setViewPager(mViewPager);

        mViewPager.setAllowScroll(false);

        /*mViewTab.setOnTabChangeListener(new TabView.OnTabChangeListener() {
            @Override
            public void onTabChange(int newIndex) {
                //// TODO: 2017/5/3 nav
                if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
                    if (0 == newIndex) {
                        // 当前焦点不在mRefreshControlQRDelegate说明是通过点击进入page0的，这种情况下
                        // 才设置默认焦点到用户头像上
                        if (getNavBtnSupporter().getCurrentFocusView() == mRefreshControlQRDelegate) {
                            getNavBtnSupporter().setCurrentFocusIndex(0);
                        }
                    } else {
                        getNavBtnSupporter().setCurrentFocusView(mRefreshControlQRDelegate);
                    }
                }
            }
        });*/

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshFocusViewList();

                /*if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
                    if (position == 0) {
                        getNavBtnSupporter().setCurrentFocus(mBtnPushConfirm);
                    }else {
                        getNavBtnSupporter().setCurrentFocus(mBtnRefreshControlQR);
                    }
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                refreshFocusViewList();
            }
        });

        updateTabEntry();
    }

    private void initPagerViews() {
        if (WxThemeStore.THEME_CAR_PORTRAIT.equals(WxThemeStore.get().getCurrentTheme())) {
            mViewLoginWechat = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_wechat_portrait, null);
            mViewLoginControl = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_control_portrait, null);
        } else {
            mViewLoginWechat = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_wechat, null);
            mViewLoginControl = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_control, null);
        }

        // bind view components
        mRlPanelPicker = (RelativeLayout) mViewLoginWechat.findViewById(R.id.rl_car_qr_wechat_pick);
        mRlPanelScan = (RelativeLayout) mViewLoginWechat.findViewById(R.id.rl_car_qr_wechat_scan);
        mRlPanelConfirm = (RelativeLayout) mViewLoginWechat.findViewById(R.id.rl_car_qr_wechat_confirm);
        mIvPushAvatar = (ImageView) mViewLoginWechat.findViewById(R.id.iv_car_qr_wechat_picker_avatar);
        mBtnPushConfirm = (FrameLayout) mViewLoginWechat.findViewById(R.id.btn_car_qr_wechat_picker_confirm);
        mIvQRCode = (ImageView) mViewLoginWechat.findViewById(R.id.iv_car_qr_wechat_qrcode);
        mViewQRCodeClickHint = mViewLoginWechat.findViewById(R.id.view_car_qr_wechat_click_hint);
        mTvQRErrorTip = (TextView) mViewLoginWechat.findViewById(R.id.tv_car_qr_wechat_qr_error);
        mPbWechat = (ProgressWheel) mViewLoginWechat.findViewById(R.id.pb_car_qr_wechat_progress);
        mIvUserAvatar = (ImageView) mViewLoginWechat.findViewById(R.id.iv_car_qr_wechat_avatar);
        mTvConfirmTitle = (TextView) mViewLoginWechat.findViewById(R.id.tv_car_qr_wechat_confirm_title);
        mTvConfirmTip = (TextView) mViewLoginWechat.findViewById(R.id.tv_car_qr_wechat_confirm_subtitle);
        mBtnBack = (Button) mViewLoginWechat.findViewById(R.id.btn_car_qr_confirm_return);
        mBtnRefreshWechatQR = (FrameLayout) mViewLoginWechat.findViewById(R.id.btn_car_qr_wechat_refresh);
        // bind view components in login control page
        mIvControlQRCode = (ImageView) mViewLoginControl.findViewById(R.id.iv_car_qr_control_qrcode);
        mBtnRefreshControlQR = (FrameLayout) mViewLoginControl.findViewById(R.id.btn_car_qr_control_refresh);
        mPbControl = (ProgressWheel) mViewLoginControl.findViewById(R.id.pb_car_qr_control_progress);
        mTvControlTip = (TextView) mViewLoginControl.findViewById(R.id.tv_car_qr_control_title);
        mTvControlHelp = (TextView) mViewLoginControl.findViewById(R.id.tv_car_qr_control_help);

        // 初始化二维码点击提示可见性
        if (bUserPickerEnabled) {
            mViewQRCodeClickHint.setVisibility(View.VISIBLE);
        } else {
            mViewQRCodeClickHint.setVisibility(View.GONE);
        }

        mIvPushAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUserPicker.show();
            }
        });

        mIvQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserPicker();
            }
        });

        mBtnPushConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActionCreator.get().pushLogin(WxQrCodeStore.get().getCurrentUserCache().getUin());
            }
        });

        mBtnRefreshWechatQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setBackground(getResources().getDrawable(R.drawable.shape_activity_background_login));
                mBtnRefreshWechatQR.setEnabled(false);
                AppLogic.removeUiGroundCallback(mEnableRefreshQRCodeTask);
                AppLogic.runOnUiGround(mEnableRefreshQRCodeTask, 2000);

                mPbWechat.setVisibility(View.VISIBLE);
                LoginActionCreator.get().refreshQRCode();
            }
        });

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoginActionCreator.get().refreshQRCode();
                /*showWechatQR();


                //若当前焦点在返回登陆上，将焦点设置到刷新二维码
                if (getNavBtnSupporter().getCurrentFocusView() == mBtnBack) {
                    refreshFocusViewList();
                    getNavBtnSupporter().setCurrentFocusView(mBtnRefreshWechatQR);
                } else {
                    refreshFocusViewList();
                }*/
            }
        });

        mBtnRefreshControlQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TXZBindStore.get().isWaitingResp()) {
                    TXZBindActionCreator.get().subscribeBindInfo();
                }
            }
        });

        mTvControlHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BindReasonActivity.show(Car_QRCodeActivity.this, true);
            }
        });
    }

    Runnable mEnableRefreshQRCodeTask = new Runnable() {
        @Override
        public void run() {
            if (mBtnRefreshWechatQR != null) {
                mBtnRefreshWechatQR.setEnabled(true);
            }
        }
    };

    private void showUserPicker() {
        if (bUserPickerEnabled) {
            mUserPicker.show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WxStatusHelper.getInstance().notifyOpen();
        // 知豆双屏处理
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        WxStatusHelper.getInstance().notifyOpen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        WxStatusHelper.getInstance().notifyClose();
    }

    private class ViewPagerAdapter extends PagerAdapter {

        List<View> mViewList;

        public ViewPagerAdapter(Context context, List<View> views) {
            mViewList = views;
            if (mViewList == null) {
                mViewList = new ArrayList<View>();
            }
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(mViewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(mViewList.get(position), 0);
            return mViewList.get(position);
        }
    }

    private int mCtrlHintTtsId = TXZTtsManager.INVALID_TTS_TASK_ID;

    private TXZAsrManager.AsrComplexSelectCallback mAsrLoginPageCtrlCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        {
            addCommand("LOGIN", "登录微信");
        }

        @Override
        public void onCommandSelected(String type, String command) {
            if (mViewPager == null) {
                return;
            }
            TtsUtil.cancelSpeak(mCtrlHintTtsId);
            if ("LOGIN".equals(type)) {
                int curView = mViewPager.getCurrentItem();
                if (curView == 0 && View.VISIBLE == mRlPanelPicker.getVisibility()) {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mBtnPushConfirm.performClick();
                        }
                    }, 0);
                }
            }
        }

        @Override
        public String getTaskId() {
            return "TASK_ASR_LOGIN_PAGE_CTRL";
        }

        @Override
        public boolean needAsrState() {
            return false;
        }
    };

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
        if (AppStatusStore.get().isUIAsrEnabled()) {
            TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_LOGIN_PAGE_CTRL");
        }
    }

    @Override
    public void onGetFocus() {
        super.onGetFocus();
        if (AppStatusStore.get().isUIAsrEnabled()) {
            TXZAsrManager.getInstance().useWakeupAsAsr(mAsrLoginPageCtrlCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != mUserPicker && mUserPicker.isShowing()) {
            mUserPicker.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bBroadOnDestroy) {
            // 知豆双屏处理
            sendBroadcast(new Intent("com.txznet.webchat.main_finish"));
        }
    }
}
