package com.txznet.webchat.ui.car.t700;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTopActivity5;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.wrappers.SimplePaddingWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.TXZTtsStore;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactFocusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.stores.WxServerConfigStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.base.widgets.ConfirmCancelDialog;
import com.txznet.webchat.ui.base.widgets.FocusRecyclerView;
import com.txznet.webchat.ui.car.adapter.CarChatMessageAdapter;
import com.txznet.webchat.ui.car.adapter.CarSettingsAdapter;
import com.txznet.webchat.ui.car.adapter.HelpListAdapter;
import com.txznet.webchat.ui.car.widget.ResourceButton;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.util.SmileyParser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 主界面
 * Created by J on 2016/10/11.
 */

public class Car_MainActivity_T700 extends AppBaseActivity {
    private static final String INTENT_PARAM_OPENID = "openid";
    private static final String INTENT_PARAM_REPEAT_LAST_MESSAGE = "tts_last_msg";
    private static final String INTENT_PARAM_OPERATION = "operation";
    private static final String INTENT_DATA_OPEN_SETTING = "open_setting";
    private static final String INTENT_DATA_OPEN_CHAT = "open_chat";

    @Bind(R.id.rl_root)
    RelativeLayout mRlRoot;
    @Bind(R.id.rl_car_main_content)
    RelativeLayout mRlContent;
    @Bind(R.id.rl_car_main_icon_content)
    PercentFrameLayout mRlWechatIconContent;
    @Bind(R.id.rl_car_main_contact)
    RelativeLayout mRlContact;
    @Bind(R.id.prv_car_main_session)
    RecyclerView mRvSessionList;
    @Bind(R.id.rv_car_main_chat)
    RecyclerView mRvChatList;
    @Bind(R.id.rl_car_main_chat)
    RelativeLayout mRlChatContainer;
    @Bind(R.id.btn_car_chat_reply)
    FrameLayout mBtnChatReply;
    @Bind(R.id.view_car_chat_msg_bg)
    View mViewChatBackground;
    @Bind(R.id.fl_car_main_chat_title)
    FrameLayout mFlChatTitle;
    @Bind(R.id.tv_car_main_chat_title)
    TextView mTvChatTitle;
    @Bind(R.id.iv_car_chat_reply_icon)
    ImageView mIvChatReplyIcon;

    // 设置页面
    @Bind(R.id.rl_car_main_setting)
    RelativeLayout mRlSetting;
    @Bind(R.id.rl_car_main_icon_setting)
    PercentFrameLayout mRlWechatIconSetting;
    @Bind(R.id.view_car_main_help_expand)
    ResourceButton mBtnHelpExpand;
    @Bind(R.id.rl_car_main_title_normal)
    RelativeLayout mRlTitleNormal;
    @Bind(R.id.rl_car_main_title_setting)
    RelativeLayout mRlTitleSetting;
    // 绑定页面
    @Bind(R.id.rl_car_main_bind)
    PercentRelativeLayout mRlBind;
    @Bind(R.id.iv_car_main_bind_qrcode)
    ImageView mIvBindQr;
    @Bind(R.id.tv_car_main_bind_title)
    TextView mTvBindTitle;
    @Bind(R.id.pb_car_main_bind_progress)
    ProgressWheel mPbBindQr;
    @Bind(R.id.btn_car_main_bind_refresh)
    FrameLayout mBtnBindRefresh;
    //side menu
    @Bind(R.id.view_car_main_menu_toggle)
    ResourceButton mBtnMenuToggle;
    @Bind(R.id.rv_car_main_menu)
    FocusRecyclerView mRvMainMenu;
    @Bind(R.id.rl_car_main_help)
    RelativeLayout mRlListHelp;
    @Bind(R.id.elv_car_main_help)
    ExpandableListView mElvHelp;
    @Bind(R.id.iv_car_main_title_avatar)
    ImageView mIvAvatar;
    @Bind(R.id.view_car_main_menu_toggle_setting)
    ResourceButton mBtnMenuToggleSetting;

    // recycler view adapter
    CarSessionListAdapter_T700 mSessionListAdapter;
    LinearLayoutManager mSessionLayoutManager;
    CarSettingsAdapter mMainMenuAdapter;

    // chat list
    CarChatMessageAdapter mChatMessageAdapter;
    LinearLayoutManager mChatLayoutManager;

    private ConfirmCancelDialog mExitDialog;
    private ConfirmCancelDialog mClearCacheDialog;

    HelpListAdapter mHelpAdapter;

    private boolean bSettingsShowing = false; // 设置界面是否显示
    private boolean bChatPanelShowing = false; // 聊天页面是否显示

    private boolean bHideBackButton;// 设置按钮处于返回状态时是否需要隐藏

    @Override
    protected int getLayout() {
        return R.layout.activity_car_main_portrait_t700;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                WxContactStore.getInstance(),
                WxContactFocusStore.getInstance(),
                WxResourceStore.get(),
                WxMessageStore.getInstance(),
                AppStatusStore.get(),
                TXZTtsStore.getInstance(), TXZBindStore.get(),
        };
    }

    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            case WxContactStore.EVENT_TYPE_ALL:
                updateSessionList();
                updateChatList(false);
                break;

            case WxContactFocusStore.EVENT_TYPE_ALL:
                updateSessionFocus();
                break;

            case WxResourceStore.EVENT_TYPE_ALL:
                updateSessionList();
                updateLoginUserAvatar();
                updateChatList(false);
                break;

            case WxMessageStore.EVENT_TYPE_ALL:
                updateSessionList();
                updateChatList(true);
                break;

            case AppStatusStore.EVENT_TYPE_ALL:
                // 更新菜单状态
                if (null != mMainMenuAdapter) {
                    mMainMenuAdapter.notifyDataSetChanged();
                }

                if (null != mSessionListAdapter) {
                    mSessionListAdapter.notifyDataSetChanged();
                }

                if (AppStatusStore.get().isUIAsrEnabled()) {
                    if (hasFocus()) {
                        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrMainCtrlCallback);
                    }
                } else {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_MAIN_CTRL");
                }
                break;

            case TXZTtsStore.EVENT_TYPE_ALL:
                if (null != mChatMessageAdapter) {
                    mChatMessageAdapter.notifyDataSetChanged();
                }
                break;

            case TXZBindStore.EVENT_TYPE_ALL:
                updateBindStatus();
                break;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resolveIntent(getIntent());

        // 知豆双屏处理
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);
    }

    @SuppressLint("NewApi")
    @Override
    protected void init(Bundle savedInstanceState) {
        initSessionList();
        initChatList();
        initMenu();
        initHelpList();
        initBindPage();
        initTheme();
    }

    @Override
    protected void initFocusViewList() {

    }

    @Override
    protected void refreshFocusViewList() {
        if (!isDpadSupportEnabled()) {
            return;
        }

        if (FocusSupporter.NAV_MODE_TWO_WAY == WxNavBtnHelper.getInstance().getNavMode()) {
            refreshFocusListTwoWay();
        } else {
            refreshFocusListOneWay();
        }
    }

    private boolean bChatPanelFocused = false;
    private boolean bHelpPanelFocused = false;

    private void refreshFocusListOneWay() {
        List vList = new ArrayList();

        if (bSettingsShowing) {
            if (bHelpPanelFocused) {
                vList.add(mElvHelp);
            } else {
                vList.add(mRvMainMenu);
            }
        } else {
            if (bChatPanelFocused) {
                if (mChatMessageAdapter.getItemCount() > 0) {
                    vList.add(mRvChatList);
                }
                vList.add(mBtnChatReply);
            } else {
                vList.add(mBtnMenuToggle);
                vList.add(mRvSessionList);
            }
        }

        getNavBtnSupporter().setViewList(vList);

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered() && null == getNavBtnSupporter()
                .getCurrentFocus()) {
            if (bSettingsShowing) {
                if (bHelpPanelFocused) {
                    getNavBtnSupporter().setCurrentFocus(mElvHelp);
                } else {
                    getNavBtnSupporter().setCurrentFocus(mRvMainMenu);
                }
            } else {
                if (bChatPanelFocused) {
                    getNavBtnSupporter().setCurrentFocus(mBtnChatReply);
                } else {
                    if (-1 != mSessionListAdapter.getCurrentSelectionPosition()) {
                        getNavBtnSupporter().setCurrentFocus(mRvSessionList);
                    } else {
                        getNavBtnSupporter().setCurrentFocus(mBtnMenuToggle);
                    }
                }
            }
        }
    }

    private void refreshFocusListTwoWay() {
        List vList = new ArrayList<>();
        if (bSettingsShowing) {
            vList.add(mRvMainMenu);

            if (View.VISIBLE == mRlListHelp.getVisibility()) {
                vList.add(mElvHelp);
            }
        } else {
            vList.add(mBtnMenuToggle);
            vList.add(mRvSessionList);

            if (View.VISIBLE == mRlChatContainer.getVisibility()) {
                if (mChatMessageAdapter.getItemCount() > 0) {
                    vList.add(mRvChatList);
                    mBtnChatReply.setNextFocusUpId(mRvChatList.getId());
                } else {
                    mBtnChatReply.setNextFocusUpId(mBtnMenuToggle.getId());
                }

                vList.add(new SimplePaddingWrapper(mBtnChatReply, getResources().getDrawable(R
                        .drawable.ic_nav_indicator_round), new int[]{2, 2, 2, 2}));
            }
        }

        getNavBtnSupporter().setViewList(vList);

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered() && null == getNavBtnSupporter()
                .getCurrentFocus()) {
            if (View.VISIBLE == mRlSetting.getVisibility()) {
                getNavBtnSupporter().setCurrentFocus(mRvMainMenu);
            } else {
                getNavBtnSupporter().setCurrentFocus(mBtnMenuToggle);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLoginUserAvatar();
        updateSessionList();
        updateSessionFocus();

        // 刷新下menu各item状态, 确保不会因界面切到后台导致各开关状态不正确
        if (null != mMainMenuAdapter) {
            mMainMenuAdapter.notifyDataSetChanged();
        }

        procLastMsg();

        // 页面重新切换到前台时自动滑动到当前焦点会话
        int focus = mSessionListAdapter.getCurrentSelectionPosition();
        if (focus >= 0) {
            mRvSessionList.scrollToPosition(focus);
        }

        refreshFocusViewList();

        /*
        * 小蚁设备声控界面采用Activity实现, 微信登陆成功跳转主界面时若声控界面正在显示, 主界面可能遮挡声控界面
        * 导致出现交互问题, 此处发送广播通知声控界面规避此问题
        * */
        Intent foregroundIntent = new Intent(ACTION_ACTIVITY_FOREGROUND);
        foregroundIntent.putExtra(ACTION_KEY_ACTIVITY_NAME,
                "com.txznet.webchat.ui.car.Car_MainActivity");
        GlobalContext.get().sendBroadcast(foregroundIntent);
    }

    @SuppressLint("NewApi")
    private void initTheme() {

    }

    // 用标志位记录下是否需要播报最后一条消息, onResume中再进行播报
    private boolean bProcLastMsg = false;

    @Override
    protected void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        if (null == intent || null == intent.getExtras()) {
            return;
        }

        String operation = intent.getStringExtra(INTENT_PARAM_OPERATION);
        if (TextUtils.isEmpty(operation) || INTENT_DATA_OPEN_CHAT.equals(operation)) {
            if (bSettingsShowing) {
                changeMenuStatus();
            }

            if (intent.getBooleanExtra(INTENT_PARAM_REPEAT_LAST_MESSAGE, false)) {
                bProcLastMsg = true;
            }
        } else if (INTENT_DATA_OPEN_SETTING.equals(operation)) {
            // 打开设置页面
            if (!bSettingsShowing) {
                changeMenuStatus();
            }
        }
    }

    private void procLastMsg() {

        if (bProcLastMsg) {
            if (null == mChatMessageAdapter) {
                return;
            }

            int count = mChatMessageAdapter.getItemCount();

            if (count > 0) {
                // find last message & proc
                String selfId = WxContactStore.getInstance().getLoginUser().mUserOpenId;

                for (int i = count - 1; i >= 0; i--) {
                    WxMessage msg = mChatMessageAdapter.getMsgList().get(i);

                    if (!msg.mSenderUserId.equals(selfId)) {
                        TtsActionCreator.get().repeatMessage(msg);
                        break;
                    }
                }

            }
        }

        bProcLastMsg = false;
    }

    public static void showChat(Context context, String openId, boolean ttsLast) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_OPENID, openId);
        intent.putExtra(INTENT_PARAM_REPEAT_LAST_MESSAGE, ttsLast);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(GlobalContext.get(), ReserveSingleTopActivity5.class);
        GlobalContext.get().startActivity(intent);
    }

    public static void showSettings() {
        Intent intent = new Intent();
        intent.putExtra(INTENT_PARAM_OPERATION, INTENT_DATA_OPEN_SETTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(GlobalContext.get(), ReserveSingleTopActivity5.class);
        GlobalContext.get().startActivity(intent);
    }

    private void initSessionList() {
        mSessionLayoutManager = new LinearLayoutManager(this);
        mSessionListAdapter = new CarSessionListAdapter_T700(this);

        mRvSessionList.setLayoutManager(mSessionLayoutManager);
        mRvSessionList.setAdapter(mSessionListAdapter);

        mSessionListAdapter.setContactList(WxContactStore.getInstance().getSessionList());
        mSessionListAdapter.setOnItemClickListener(new CarSessionListAdapter_T700.OnItemClickListener() {
            @Override
            public void onItemClicked(View v, String id) {
                bChatPanelFocused = true;
                ContactActionCreator.get().switchFocusSession(id);
            }
        });

        mRvSessionList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    mSessionListAdapter.setLoadHeadIcon(true);
                } else {
                    mSessionListAdapter.setLoadHeadIcon(false);
                }
            }
        });

    }

    private void initChatList() {
        mChatMessageAdapter = new CarChatMessageAdapter(this);
        mChatLayoutManager = new LinearLayoutManager(this);

        mRvChatList.setLayoutManager(mChatLayoutManager);
        mRvChatList.setAdapter(mChatMessageAdapter);

        mBtnChatReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnChatReply.setEnabled(false);
                AppLogic.removeUiGroundCallback(mEnableReplyBtnTask);
                AppLogic.runOnUiGround(mEnableReplyBtnTask, 1000);
                MessageActionCreator.get()
                        .replyVoice(WxContactFocusStore.getInstance().getFocusedSession(), false);
            }
        });

        //updateChatList();
    }

    private Runnable mEnableReplyBtnTask = new Runnable() {
        @Override
        public void run() {
            mBtnChatReply.setEnabled(true);
        }
    };

    private void initHelpList() {
        mHelpAdapter = new HelpListAdapter(this);
        mElvHelp.setDividerHeight(0);
        mElvHelp.setGroupIndicator(null);
        mElvHelp.setSelector(R.drawable.selector_none);
        mElvHelp.setAdapter(mHelpAdapter);
        mHelpAdapter.setExpandableListView(mElvHelp);

        mHelpAdapter.setStateChangeListener(
                new HelpListAdapter.ExpandableListStateChangeListener() {
                    @Override
                    public void expandStatChanged(int newStat) {
                        if (mHelpAdapter.isAllGroupExpanded()) {
                            mBtnHelpExpand.setText(getResources().getString(R.string
                                    .lb_setting_help_collapse_all));
                        } else {
                            mBtnHelpExpand.setText(getResources().getString(R.string
                                    .lb_setting_help_expand_all));
                        }
                    }
                });

        mBtnHelpExpand.setOnBtnClickListener(new ResourceButton.OnBtnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelpAdapter.isAllGroupExpanded()) {
                    collapseAllHelpItems();
                } else {
                    expandAllHelpItems();
                }
            }
        });
    }

    private void initBindPage() {
        mBtnBindRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!TXZBindStore.get().isWaitingResp()) {
                    TXZBindActionCreator.get().subscribeBindInfo();
                }
            }
        });
    }

    private void expandAllHelpItems() {
        int groupCount = mHelpAdapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            mElvHelp.expandGroup(i);
        }
    }

    private void collapseAllHelpItems() {
        int groupCount = mHelpAdapter.getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            mElvHelp.collapseGroup(i);
        }
    }

    private void initMenu() {
        mRlSetting.setVisibility(View.GONE);
        mRlTitleNormal.setVisibility(View.VISIBLE);
        mRlTitleSetting.setVisibility(View.GONE);
        bHideBackButton = !WxConfigStore.getInstance().isBackButtonEnabled();

        mExitDialog = new ConfirmCancelDialog(this, getResources().getString(R.string
                .lb_exit_dialog_title));
        mExitDialog.setDialogListener(new ConfirmCancelDialog.DialogListener() {
            @Override
            public void onCommit() {
                mExitDialog.dismiss();
                mManuallyExit = true;
                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_MAINPAGE_EXIT);
                LoginActionCreator.get().doLogout(true);
            }

            @Override
            public void onCancel() {
                mExitDialog.dismiss();
            }
        });

        mClearCacheDialog = new ConfirmCancelDialog(this, getResources().getString(R.string
                .lb_clear_cache_dialog_title));
        mClearCacheDialog.setDialogListener(new ConfirmCancelDialog.DialogListener() {
            @Override
            public void onCommit() {
                mClearCacheDialog.dismiss();
                LoginActionCreator.get().clearUserCache();
            }

            @Override
            public void onCancel() {
                mClearCacheDialog.dismiss();
            }
        });

        mBtnMenuToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenuStatus();
            }
        });

        mBtnMenuToggleSetting.setOnBtnClickListener(new ResourceButton.OnBtnClickListener() {
            @Override
            public void onClick(final View v) {
                changeMenuStatus();
            }
        });

        mMainMenuAdapter = new CarSettingsAdapter(this);
        mMainMenuAdapter.setOnMenuItemClickListener(
                new CarSettingsAdapter.OnMenuItemClickListner() {
                    @Override
                    public void onMenuItemClick(final CarSettingsAdapter.MENU_ITEM item) {
                        switch (item) {
                            case AUTO_LOGIN:
                                changeAutoLoginStatus();
                                break;

                            case AUTO_BROAD:
                                changeNotifyStatus();
                                break;

                            case HELP:
                                showMenuHelp();
                                break;

                            case CLEAR_LOGIN_CACHE:
                                mClearCacheDialog.show();
                                break;

                            case EXIT:
                                mExitDialog.show();
                                break;

                            case BIND:
                                showMenuBind();
                                break;
                        }
                    }
                });

        mMainMenuAdapter.setMenuItemList(getMenuItemList());
        mRvMainMenu.setLayoutManager(new LinearLayoutManager(this));
        mRvMainMenu.setAdapter(mMainMenuAdapter);
    }

    private List<CarSettingsAdapter.MENU_ITEM> getMenuItemList() {
        boolean pushLoginEnabled = WxServerConfigStore.getInstance().isPushLoginEnabled();
        boolean bindEnabled = WxConfigStore.getInstance().isMainControlEntryEnabled();
        List<CarSettingsAdapter.MENU_ITEM> list = new ArrayList<>();
        list.add(CarSettingsAdapter.MENU_ITEM.TITLE);
        if (pushLoginEnabled) {
            list.add(CarSettingsAdapter.MENU_ITEM.AUTO_LOGIN);
        }
        list.add(CarSettingsAdapter.MENU_ITEM.AUTO_BROAD);
        list.add(CarSettingsAdapter.MENU_ITEM.HELP);
        if (bindEnabled) {
            list.add(CarSettingsAdapter.MENU_ITEM.BIND);
        }
        if (pushLoginEnabled) {
            list.add(CarSettingsAdapter.MENU_ITEM.BIND);
        }
        list.add(CarSettingsAdapter.MENU_ITEM.EXIT);

        return list;
    }

    private void showMenuHelp() {
        mRlListHelp.setVisibility(View.VISIBLE);
        mRlBind.setVisibility(View.GONE);
        mRlWechatIconSetting.setVisibility(View.GONE);
        bHelpPanelFocused = true;
        refreshFocusViewList();
    }

    private void showMenuBind() {
        mRlListHelp.setVisibility(View.GONE);
        mRlBind.setVisibility(View.VISIBLE);
        mRlWechatIconSetting.setVisibility(View.GONE);

        updateBindStatus();
    }

    private void updateBindStatus() {
        if (View.VISIBLE == mRlBind.getVisibility()) {
            if (TXZBindStore.get().isException()) {
                mTvBindTitle.setText(R.string.lb_login_control_failed);
            } else if (TXZBindStore.get().isWaitingResp()) {
                mPbBindQr.setVisibility(View.VISIBLE);
            } else {
                showControlQR();

                // 判断绑定信息
                L.d("getting bind info: isBind = " + TXZBindStore.get().hasBind());
                if (TXZBindStore.get().hasBind()) {
                    mTvBindTitle.setText(
                            String.format(getResources().getString(R.string.lb_login_control_bound),
                                    TXZBindStore.get().getBindNick()));
                } else {
                    mTvBindTitle.setText(R.string.lb_login_control_unbind);
                }
            }
        }
    }

    private void showControlQR() {
        //hide progress bar
        mPbBindQr.setVisibility(View.GONE);

        String QRStr = TXZBindStore.get().getBindUrl();
        if (!TextUtils.isEmpty(QRStr)) {
            final Bitmap mQRBitmap;
            try {
                mQRBitmap = QRUtil.createQRCodeBitmap(QRStr,
                        (int) getResources().getDimension(R.dimen.y180));
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        mIvBindQr.setImageBitmap(mQRBitmap);
                    }
                }, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changeMenuStatus() {
        if (bSettingsShowing) {
            mRlSetting.setVisibility(View.GONE);
            mRlListHelp.setVisibility(View.GONE);
            mRlBind.setVisibility(View.GONE);
            mRlTitleSetting.setVisibility(View.GONE);
            mRlTitleNormal.setVisibility(View.VISIBLE);

            // show chat
            mRlContent.setVisibility(View.VISIBLE);
            if (bChatPanelShowing) {
                ContactActionCreator.get().openSession(mChatMessageAdapter.getCurrentSession());
            }

            bSettingsShowing = false;
        } else {
            mRlWechatIconSetting.setVisibility(View.VISIBLE);
            mRlListHelp.setVisibility(View.GONE);
            mRlBind.setVisibility(View.GONE);
            mRlSetting.setVisibility(View.VISIBLE);
            mRlTitleSetting.setVisibility(View.VISIBLE);
            mRlTitleNormal.setVisibility(View.GONE);

            // hide chat
            mRlContent.setVisibility(View.GONE);
            if (bChatPanelShowing) {
                ContactActionCreator.get().closeSession(mChatMessageAdapter.getCurrentSession());
            }

            bSettingsShowing = true;
        }

        refreshFocusViewList();
    }

    private void changeAutoLoginStatus() {
        if (AppStatusStore.get().isAutoLoginEnabled()) {
            AppStatusActionCreator.get().disableAutoLogin();
        } else {
            AppStatusActionCreator.get().enableAutoLogin();
        }
    }

    private void changeNotifyStatus() {
        if (AppStatusStore.get().isAutoBroadEnabled()) {
            TXZReportActionCreator.getInstance()
                    .report(ReportMessage.REPORT_UI_MAINPAGE_NOTIFY_DISABLE);
            AppStatusActionCreator.get().disableAutoSpeak();
        } else {
            TXZReportActionCreator.getInstance()
                    .report(ReportMessage.REPORT_UI_MAINPAGE_NOTIFY_ENABLE);
            AppStatusActionCreator.get().enableAutoSpeak();
        }
    }

    private void updateChatList(boolean scrollToEnd) {
        WxContact session = WxContactStore.getInstance()
                .getContact(WxContactFocusStore.getInstance().getFocusedSession());

        if (session != null && !TextUtils.isEmpty(session.mUserOpenId)) {
            bChatPanelShowing = true;
            mRlChatContainer.setVisibility(View.VISIBLE);
            mRlWechatIconContent.setVisibility(View.GONE);
            mTvChatTitle.setText(SmileyParser.getInstance(this).parser(session.getRawDisplayName()));

            String curSession = mChatMessageAdapter.getCurrentSession();
            if (!TextUtils.isEmpty(curSession) && curSession.equals(session.mUserOpenId)) {
                if (mChatMessageAdapter.getMsgList() != null) {
                    mChatMessageAdapter.notifyDataSetChanged();
                } else {
                    mChatMessageAdapter.setSession(session.mUserOpenId);
                }

                if (scrollToEnd && mChatMessageAdapter.getItemCount() > 0) {
                    mRvChatList.smoothScrollToPosition(mChatMessageAdapter.getItemCount() - 1);
                }

            } else {
                mChatMessageAdapter.setSession(session.mUserOpenId);
                if (scrollToEnd && mChatMessageAdapter.getItemCount() > 0) {
                    mRvChatList.scrollToPosition(mChatMessageAdapter.getItemCount() - 1);
                }

                ContactActionCreator.get().openSession(session.mUserOpenId);
            }

            WxStatusHelper.getInstance().notifyChatModeStatusChanged(true);
        }

        refreshFocusViewList();
    }

    private void updateLoginUserAvatar() {
        WxContact loginUser = WxContactStore.getInstance().getLoginUser();

        if (null != loginUser) {
            WxImageLoader.loadHead(this, loginUser, mIvAvatar);
        }
    }

    private void updateSessionList() {
        if (mSessionListAdapter != null) {
            mSessionListAdapter.setContactList(WxContactStore.getInstance().getSessionList());
            mSessionListAdapter.notifyDataSetChanged();
        }

    }

    private void updateSessionFocus() {
        String focusedId = WxContactFocusStore.getInstance().getFocusedSession();

        if (!TextUtils.isEmpty(focusedId)) {
            if (null != mSessionListAdapter) {
                mSessionListAdapter.setCurrentSelectionId(focusedId);

                // 滚动到当前焦点会话
                int focus = mSessionListAdapter.getCurrentSelectionPosition();
                if (focus >= 0) {
                    mRvSessionList.scrollToPosition(focus);

                    // update chat list
                    updateChatList(true);
                }
            }

            updateChatList(true);
        }

    }

    /**
     * 获取页面初始化时用于展示的"合适"焦点会话
     * 因为车镜版本主题不同于后视镜版本, 主界面中会展示会话界面, 当前焦点联系人在关闭会话时会被清空, 为保证
     * 车机版界面关闭后再次打开时能显示上次查看的会话, 获取用于展示的焦点会话时, 先检查当前焦点联系人是否存在,
     * 不存在时使用上次的焦点联系人
     *
     * @return
     */
    /*private String getFocusedSession() {
        String uid = WxContactStore.getInstance().getFocusedSession();

        if (TextUtils.isEmpty(uid)) {
            uid = WxContactStore.getInstance().getLastFocusedSession();
        }

        return uid;
    }*/


    /// Asr callbacks
    private TXZAsrManager.AsrComplexSelectCallback mAsrMainCtrlCallback =
            new TXZAsrManager.AsrComplexSelectCallback() {
                {
                    addCommand("REPLY", "回复微信");
                }

                @Override
                public void onCommandSelected(String type, String command) {
                    if ("REPLY".equals(type)) {
                        if (View.VISIBLE == mRlChatContainer.getVisibility() && !bSettingsShowing) {
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    mBtnChatReply.performClick();
                                }
                            }, 0);
                        }
                    }
                }

                @Override
                public String getTaskId() {
                    return "TASK_ASR_MAIN_CTRL";
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
            TXZAsrManager.getInstance().useWakeupAsAsr(mAsrMainCtrlCallback);
        }
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
        if (AppStatusStore.get().isUIAsrEnabled()) {
            TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_MAIN_CTRL");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        WxStatusHelper.getInstance().notifyChatModeStatusChanged(false);

        if (bChatPanelShowing) {
            ContactActionCreator.get().closeSession(mChatMessageAdapter.getCurrentSession());
        }
    }

    @Override
    public void onBackPressed() {
        if (bSettingsShowing) {
            if (bHelpPanelFocused && WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
                bHelpPanelFocused = false;
                refreshFocusViewList();

                // 在单向方控交互模式中, 焦点在帮助列表时按返回键, 应将焦点设置到左侧菜单中的帮助项
                if (FocusSupporter.NAV_MODE_ONE_WAY == WxNavBtnHelper.getInstance().getNavMode()) {
                    if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
                        mRvMainMenu.setCurrentFocusPosition(3);
                    } else {
                        mRvMainMenu.setCurrentFocusPosition(2);
                    }
                }
            } else {
                changeMenuStatus();
            }

            return;
        } else if (bChatPanelFocused && WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            bChatPanelFocused = false;
            refreshFocusViewList();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 知豆双屏处理
        sendBroadcast(new Intent("com.txznet.webchat.main_finish"));
    }
}
