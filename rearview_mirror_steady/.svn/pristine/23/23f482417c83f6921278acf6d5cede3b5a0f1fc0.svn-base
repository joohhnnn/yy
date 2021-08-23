package com.txznet.webchat.ui.car.t700;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.otto.Subscribe;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.ui.dialog.WinProcessing;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTopActivity6;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.SimplePaddingWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sp.WxConfigSp;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.car.widget.TabView;
import com.txznet.webchat.ui.rearview_mirror.BindReasonActivity;
import com.txznet.webchat.ui.rearview_mirror.widget.ViewPagerEx;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 扫码和远程控制页面
 * Created by J on 2016/10/8.
 */

public class Car_QRCodeActivity_T700 extends AppBaseActivity {
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
    // view components in picker
    // view components in scan
    //view components in confirm
    FrameLayout mBtnRefreshControlQR;

    TextView mControlCloseTip;

    // view components in login control page
    ImageView mIvControlQRCode;
    TextView mTvControlTip; // 二维码旁边的文字提示
    TextView mTvControlHelp; // "扫描二维码能做什么"
    ProgressWheel mPbControl; // 二维码加载进度

    // 提示dialog
    private WinProcessing mWinProcessing;
    // 获取远程控制绑定二维码失败对话框
    public WinNotice mWinGetFailure;

    // 只有真正应用退出时才需要广播通知, 因为有扫码登录微信跳转主页面后finish的情况,
    // 需要用此标志位进行下记录, 跳转主页面的情况不进行广播通知
    private boolean bBroadOnDestroy = true;

    @Override
    protected int getLayout() {
        return R.layout.activity_car_qr_portrait_t700;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                TXZBindStore.get(),
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

        intent.setClass(context, ReserveSingleTopActivity6.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        initViewPager();
        updateBindingInfo();
        updateTabEntry();
        initTheme();
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
        } else {
            vList.add(new SimplePaddingWrapper(mBtnRefreshControlQR, getResources().getDrawable(R.drawable.ic_nav_indicator_round), new int[]{2, 2, 2, 2}));
        }

        getNavBtnSupporter().setViewList(vList);

        // 根据当前显示的ViewPager页面设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            if (0 == mViewPager.getCurrentItem()) {
                getNavBtnSupporter().setCurrentFocus(vList.get(0));
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
                mViewTab.switchToTab(1);
                return true;
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

            // 微信tab开关状态发生改变
            case WxConfigStore.EVENT_TYPE_ALL:
                updateTabEntry();
                break;

        }
    }

    private boolean bUserPickerEnabled = false;

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
        Car_QRCodeActivity_T700.ViewPagerAdapter mViewPagerAdapter = new Car_QRCodeActivity_T700.ViewPagerAdapter(this, mViewList);

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
        mViewLoginWechat = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_wechat_portrait_t700, null);
        mViewLoginControl = LayoutInflater.from(this).inflate(R.layout.layout_car_qr_control_portrait_t700, null);

        mControlCloseTip = mViewLoginWechat.findViewById(R.id.tv_control_close_tip);
        mControlCloseTip.setText(WxConfigSp.getInstance().getWxCloseDesc(getResources().getString(R.string.lb_login_wechat_ban)));

        // bind view components in login control page
        mIvControlQRCode = (ImageView) mViewLoginControl.findViewById(R.id.iv_car_qr_control_qrcode);
        mBtnRefreshControlQR = (FrameLayout) mViewLoginControl.findViewById(R.id.btn_car_qr_control_refresh);
        mPbControl = (ProgressWheel) mViewLoginControl.findViewById(R.id.pb_car_qr_control_progress);
        mTvControlTip = (TextView) mViewLoginControl.findViewById(R.id.tv_car_qr_control_title);
        mTvControlHelp = (TextView) mViewLoginControl.findViewById(R.id.tv_car_qr_control_help);

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
                BindReasonActivity.show(Car_QRCodeActivity_T700.this, true);
            }
        });
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

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
    }

    @Override
    public void onGetFocus() {
        super.onGetFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
