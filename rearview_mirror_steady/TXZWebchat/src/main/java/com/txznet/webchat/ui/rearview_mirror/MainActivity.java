package com.txznet.webchat.ui.rearview_mirror;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTopActivity2;
import com.txznet.reserve.activity.ReserveStandardActivity1;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.SimplePaddingWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.ui.rearview_mirror.widget.CareDialog;
import com.txznet.webchat.ui.rearview_mirror.widget.ExitDialog;
import com.txznet.webchat.ui.rearview_mirror.widget.IconTextStateBtn;
import com.txznet.webchat.util.SmileyParser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 微信主界面
 */
public class MainActivity extends AppBaseActivity implements IconTextStateBtn.OnClickListener {
    @Bind(R.id.view_main_btn_recent)
    IconTextStateBtn mBtnRecent;
    @Bind(R.id.view_main_btn_help)
    IconTextStateBtn mBtnHelp;
    @Bind(R.id.view_main_btn_setting)
    IconTextStateBtn mBtnSetting;
    @Bind(R.id.view_main_btn_care)
    IconTextStateBtn mBtnCare;
    @Bind(R.id.view_main_btn_exit)
    IconTextStateBtn mBtnExit2;
    @Bind(R.id.civ_main_user_icon)
    ImageView mViewUsericon;
    @Bind(R.id.tv_main_user_nick)
    TextView mTvUsername;
    @Bind(R.id.btn_main_exit)
    Button mBtnExit;

    private ExitDialog mExitDialog;
    private CareDialog mCareDialog;

    private WxContact mSelf;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                WxResourceStore.get(),
                WxContactStore.getInstance(),
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 知豆双屏处理
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        //set user profile if possible
        if (WxContactStore.getInstance().getLoginUser() != null) {
            mSelf = WxContactStore.getInstance().getLoginUser();
            mTvUsername.setText(SmileyParser.getInstance(MainActivity.this).parser(mSelf.getRawDisplayName()));
            refreshUserIcon();
        }

        mExitDialog = new ExitDialog(MainActivity.this);
        mCareDialog = new CareDialog();

        mBtnRecent.setOnBtnClickListener(this);
        mBtnHelp.setOnBtnClickListener(this);
        mBtnSetting.setOnBtnClickListener(this);
        mBtnCare.setOnBtnClickListener(this);
        mBtnExit2.setOnBtnClickListener(this);

        if (WxConfigStore.getInstance().isMainControlEntryEnabled()) {
            mBtnCare.setVisibility(View.VISIBLE);
            mBtnExit2.setVisibility(View.GONE);
            mBtnExit.setVisibility(View.VISIBLE);
        } else {
            mBtnCare.setVisibility(View.GONE);
            mBtnExit2.setVisibility(View.VISIBLE);
            mBtnExit.setVisibility(View.GONE);
        }

        mBtnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExitDialog.show();
                TXZAsrManager.getInstance().useWakeupAsAsr(mAsrExitCallback);
            }
        });

        mExitDialog.setExitDialogListener(new ExitDialog.ExitDialogListener() {
            @Override
            public void onCommit() {
                TXZTtsManager.getInstance().cancelSpeak(mExitHintTtsId);
                mExitHintTtsId = TXZTtsManager.INVALID_TTS_TASK_ID;
                TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
                mExitDialog.dismiss();
                mManuallyExit = true;
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_MAINPAGE_EXIT);
                LoginActionCreator.get().doLogout(true);
            }

            @Override
            public void onCancel() {
                TXZTtsManager.getInstance().cancelSpeak(mExitHintTtsId);
                mExitHintTtsId = TXZTtsManager.INVALID_TTS_TASK_ID;
                TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
                mExitDialog.dismiss();
            }
        });
    }


    @Override
    protected void initFocusViewList() {
        if (!isDpadSupportEnabled()) {
            return;
        }

        List<Object> vList = new ArrayList<>();
        vList.add(new SimplePaddingWrapper(mBtnRecent, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));
        vList.add(new SimplePaddingWrapper(mBtnHelp, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));

        if (WxConfigStore.getInstance().isMainControlEntryEnabled()) {
            vList.add(new SimplePaddingWrapper(mBtnExit, getResources().getDrawable(R.drawable.shape_item_car_session_back_selected), new int[]{25, 10, 25, 10}));
            vList.add(new SimplePaddingWrapper(mBtnSetting, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));
            vList.add(new SimplePaddingWrapper(mBtnCare, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));
        } else {
            vList.add(new SimplePaddingWrapper(mBtnSetting, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));
            vList.add(new SimplePaddingWrapper(mBtnExit2, getResources().getDrawable(R.drawable.ic_nav_indicator_rect_full), new int[]{-5, -5, -5, -5}));
        }

        getNavBtnSupporter().setViewList(vList);

        // add rules
        if (WxConfigStore.getInstance().isMainControlEntryEnabled()) {
            getNavBtnSupporter().addRule(mBtnCare, mBtnRecent, FocusSupporter.NAV_BTN_NEXT);
            getNavBtnSupporter().addRule(mBtnRecent, mBtnCare, FocusSupporter.NAV_BTN_PREV);
        } else {
            getNavBtnSupporter().addRule(mBtnExit2, mBtnRecent, FocusSupporter.NAV_BTN_NEXT);
            getNavBtnSupporter().addRule(mBtnRecent, mBtnExit2, FocusSupporter.NAV_BTN_PREV);
        }

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mBtnRecent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppLogic.registerRecordStatusObserver(mRecordStatusObserver);

        /*
        * 小蚁设备声控界面采用Activity实现, 微信登陆成功跳转主界面时若声控界面正在显示, 主界面可能遮挡声控界面
        * 导致出现交互问题, 此处发送广播通知声控界面规避此问题
        * */
        Intent intent = new Intent(ACTION_ACTIVITY_FOREGROUND);
        intent.putExtra(ACTION_KEY_ACTIVITY_NAME, "com.txznet.webchat.ui.rearview_mirror.MainActivity");
        GlobalContext.get().sendBroadcast(intent);
    }

    @Override
    public void onClick(View v, String state) {
        switch (v.getId()) {
            case R.id.view_main_btn_help:
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_MAINPAGE_HELP);
                intent(ReserveStandardActivity1.class);
                break;

            case R.id.view_main_btn_recent:
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_MAINPAGE_SESSION);
                intent(ReserveSingleTopActivity2.class);
                break;

            case R.id.view_main_btn_setting:
                showSettings();
                break;

            case R.id.view_main_btn_care:
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_MAINPAGE_CARE);
                showCareDialog();
                break;

            case R.id.view_main_btn_exit:
                mBtnExit.performClick();
                break;

        }
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            case WxResourceStore.EVENT_TYPE_ALL:
                //// TODO: 2016/8/25 解决头像重复刷新的问题
                refreshUserIcon();
                break;

            case WxContactStore.EVENT_TYPE_ALL:
                updateSelf();
                break;
        }
    }

    private void updateSelf() {
        WxContact contact = WxContactStore.getInstance().getLoginUser();

        if (null == contact) {
            return;
        }

        if (null == mSelf || TextUtils.isEmpty(mSelf.getDisplayName()) || !mSelf.getDisplayName().equals(contact.getDisplayName())) {
            mSelf = contact;
            mTvUsername.setText(SmileyParser.getInstance(MainActivity.this).parser(mSelf.getRawDisplayName()));
        }
    }

    private void showCareDialog() {
        mCareDialog.show();
    }

    private void showSettings() {
        SettingsActivity.show(this);
    }


    private void refreshUserIcon() {
        WxImageLoader.loadHead(this, mSelf, mViewUsericon);
    }

    private void intent(Class target) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, target);
        startActivity(intent);
    }

    private int mExitHintTtsId = TXZTtsManager.INVALID_TTS_TASK_ID;

    private TXZAsrManager.AsrComplexSelectCallback mAsrExitCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        {
            addCommand("CONFIRM", "确定");
            addCommand("CANCEL", "取消");
        }

        @Override
        public void onCommandSelected(final String type, String command) {
            if (mExitDialog == null || !mExitDialog.isShowing()) {
                return;
            }
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    if ("CONFIRM".equals(type)) {
                        mExitDialog.performClickCommit();
                    } else if ("CANCEL".equals(type)) {
                        mExitDialog.performClickCancel();
                    }
                }
            }, 0);
        }

        @Override
        public String getTaskId() {
            return "TASK_ASR_EXIT_CONFIRM";
        }

        @Override
        public boolean needAsrState() {
            return false;
        }
    };

    @Override
    public void onGetFocus() {
        super.onGetFocus();
        if (AppStatusStore.get().isUIAsrEnabled()) {
            //TXZAsrManager.getInstance().useWakeupAsAsr(mAsrMainCtrlCallback);
            if (mExitDialog != null && mExitDialog.isShowing()) {
                TXZAsrManager.getInstance().useWakeupAsAsr(mAsrExitCallback);
            }
        }
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
        /*if (AppStatusStore.get().isUIAsrEnabled()) {
            TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_MAIN_CTRL");
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppLogic.unregisterRecordStatusObserver(mRecordStatusObserver);

        if (null != mCareDialog && mCareDialog.isShowing()) {
            mCareDialog.dismiss();
        }

        if (null != mExitDialog && mExitDialog.isShowing()) {
            mExitDialog.dismiss();
        }
    }

    RecordStatusObservable.StatusObserver mRecordStatusObserver = new RecordStatusObservable.StatusObserver() {
        @Override
        public void onStatusChanged(boolean isShowing) {
            if (!AppStatusStore.get().isUIAsrEnabled()) {
                return;
            }
            if (isShowing) {
                TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
            } else {
                if (mExitDialog != null && mExitDialog.isShowing()) {
                    TXZAsrManager.getInstance().useWakeupAsAsr(mAsrExitCallback);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 知豆双屏处理
        sendBroadcast(new Intent("com.txznet.webchat.main_finish"));
    }
}
