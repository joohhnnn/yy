package com.txznet.webchat.ui.rearview_mirror;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTaskActivity0;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.ReportMessage;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZTtsStore;
import com.txznet.webchat.stores.WxContactFocusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.rearview_mirror.adapter.MirrorChatMessageAdapter;
import com.txznet.webchat.util.SmileyParser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * 聊天界面
 * Created by J on 2016/3/25.
 */
public class ChatActivity extends AppBaseActivity {
    private static final String INTENT_DATA_OPENID = "intent_data_openid";
    private static final String INTENT_TTS_LAST_MESSAGE = "intent_tts_last_message";

    @Bind(R.id.tv_chat_title)
    TextView mTvTitle;
    @Bind(R.id.rv_chat_list)
    RecyclerView mRvList;
    @Bind(R.id.btn_chat_back)
    ImageButton mBtnBack;
    @Bind(R.id.btn_chat_reply)
    FrameLayout mBtnReply;
    @Bind(R.id.view_chat_mask)
    View mViewMask;

    WxContact mContact; //chat target

    private MirrorChatMessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private boolean mAdapterInitied = false; // Adapter初始化标记位， 防止重复set
    private boolean mEnableWakeUpCtrl = true; // 是否允许当前界面的语音唤醒，防止与新消息播报完毕的唤醒命令冲突
    private boolean mUsedReply = false; // 是否使用过回复微信功能，用于微信上报

    public static void show(Context context, String openId) {
        show(context, openId, false);
    }

    /**
     * 显示ChatActivity
     *
     * @param context
     * @param openId  openid
     * @param ttsLast 是否播报最后一条消息
     */
    public static void show(Context context, String openId, boolean ttsLast) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_DATA_OPENID, openId);
        intent.putExtra(INTENT_TTS_LAST_MESSAGE, ttsLast);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, ReserveSingleTaskActivity0.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_chat;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                WxMessageStore.getInstance(),
                WxResourceStore.get(),
                AppStatusStore.get(),
                TXZTtsStore.getInstance(),
                WxContactFocusStore.getInstance(),
        };
    }


    Runnable mEnableRepyTask = new Runnable() {
        @Override
        public void run() {
            if (mBtnReply != null) {
                mBtnReply.setEnabled(true);
            }
        }
    };

    @Override
    protected void init(Bundle savedInstanceState) {
        initUserData(getIntent());
        //init RecyclerView
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new MirrorChatMessageAdapter(this);
        mRvList.setLayoutManager(mLayoutManager);
        mRvList.setAdapter(mAdapter);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBtnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                AppLogic.removeUiGroundCallback(mEnableRepyTask);
                AppLogic.runOnUiGround(mEnableRepyTask, 2000);
                MessageActionCreator.get().replyVoice(mContact.mUserOpenId, false);

                TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_CHAT_SEND);
                mUsedReply = true;
            }
        });

        mViewMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageActionCreator.get().cancelCurSpeak();
                MessageActionCreator.get().cancelNotify();
            }
        });

        updateMsgList();
        scrollToEnd(false);
        procLastMsg(getIntent());
    }

    @Override
    protected void initFocusViewList() {
        refreshFocusViewList();

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mBtnReply);
        }
    }

    @Override
    protected void refreshFocusViewList() {
        if (!isDpadSupportEnabled()) {
            return;
        }

        ArrayList<View> vList = new ArrayList<>();
        boolean bChatListEmpty = (null == mAdapter || mAdapter.getItemCount() == 0);

        if (!bChatListEmpty) {
            vList.add(mRvList);
        }

        vList.add(mBtnReply);

        getNavBtnSupporter().setViewList(vList);

        // add rules
        if (!bChatListEmpty) {
            getNavBtnSupporter().addRule(mBtnReply, mRvList, FocusSupporter.NAV_BTN_NEXT);
            getNavBtnSupporter().addRule(mRvList, mBtnReply, FocusSupporter.NAV_BTN_PREV);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initUserData(intent);

        mAdapter = new MirrorChatMessageAdapter(this);
        mAdapterInitied = false; // 重置聊天内容
        mRvList.setAdapter(mAdapter);
        updateMsgList();

        scrollToEnd(false);
        procLastMsg(intent);
    }

    private void initUserData(Intent intent) {
        //get openid
        String openid = intent.getStringExtra(INTENT_DATA_OPENID);
        initUserData(openid);
    }

    private void initUserData(String openid) {
        if (null != mContact && openid.equals(mContact.mUserOpenId)) {
            return;
        }

        mContact = WxContactStore.getInstance().getContact(openid);

        /*
        * 修改: 此处不应该判断用户昵称是否为空, 某些用户昵称格式化后可能为空串(如暂不支持显示的emoji表情),
        * 可能导致按获取用户信息失败处理, 导致聊天界面打不开
        * */
        //if (mContact != null && !StringUtils.isEmpty(mContact.getDisplayName())) {
        if (mContact != null) {
            CharSequence displayName = SmileyParser.getInstance(this).parser(mContact.getRawDisplayName());
            mTvTitle.setText(displayName);
            ContactActionCreator.get().openSession(mContact.mUserOpenId);

            return;
        }

        // 处理获取用户信息失败的情况
        L.e("ChatActivity", "error getting user information for id: " + openid);
        finish();
    }

    private void procLastMsg(Intent intent) {
        boolean shouldProc = intent.getBooleanExtra(INTENT_TTS_LAST_MESSAGE, false);

        if (shouldProc) {
            if (null == mAdapter) {
                return;
            }

            int count = mAdapter.getItemCount();

            if (count > 0) {
                // find last message & proc
                String selfId = WxContactStore.getInstance().getLoginUser().mUserOpenId;

                for (int i = count - 1; i >= 0; i--) {
                    WxMessage msg = mAdapter.getMsgList().get(i);

                    if (!msg.mSenderUserId.equals(selfId)) {
                        TtsActionCreator.get().repeatMessage(msg);
                        break;
                    }
                }

            }
        }
    }


    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            case WxMessageStore.EVENT_TYPE_ALL:
                updateMsgList();
                scrollToEnd(true);
                break;

            case WxResourceStore.EVENT_TYPE_ALL:
                updateMsgList();
                break;

            case AppStatusStore.EVENT_TYPE_ALL:
                if (AppStatusStore.get().isUIAsrEnabled()) {
                    if (hasFocus()) {
                        registerWakeupControl();
                    }
                } else {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("CHAT_CONTROL");
                }
                break;

            case TXZTtsStore.EVENT_TYPE_ALL:
                if (null != mAdapter) {
                    mAdapter.notifyDataSetChanged();
                }

                break;

            case WxContactFocusStore.EVENT_TYPE_ALL:
                doContactFocusChange();
                break;
        }
    }

    private void doContactFocusChange() {
        String focusedSession = WxContactFocusStore.getInstance().getFocusedSession();
        if (!TextUtils.isEmpty(focusedSession)) {
            initUserData(focusedSession);
            updateMsgList();
        }

    }

    private void updateMsgList() {
        if (mContact != null) {
            List<WxMessage> msgList = WxMessageStore.getInstance().getMessageList(mContact.mUserOpenId);

            mAdapter.setMsgList(msgList);
            mAdapterInitied = true;
            mAdapter.notifyDataSetChanged();

            // 刷新NavViewList
            refreshFocusViewList();
        }
    }

    private void scrollToEnd(boolean smoothScroll) {
        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            if (smoothScroll) {
                mRvList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            } else {
                mRvList.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        AsrUtil.recoverWakeupFromAsr("CHAT_CONTROL");
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (null != mContact) {
            ContactActionCreator.get().closeSession(mContact.mUserOpenId);
        }
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();

        if (AppStatusStore.get().isUIAsrEnabled()) {
            AsrUtil.recoverWakeupFromAsr("CHAT_CONTROL");
        }
    }

    @Override
    public void onGetFocus() {
        super.onGetFocus();

        if (AppStatusStore.get().isUIAsrEnabled()) {
            registerWakeupControl();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 更新下消息列表, 防止在聊天页面切到后台后切回来导致列表更新不及时
        updateMsgList();
        scrollToEnd(false);
    }

    public void registerWakeupControl() {
        AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, final String command) {

                if (type.equals("CMD_REPLY_WX")) {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mBtnReply.setEnabled(false);
                            AppLogic.removeUiGroundCallback(mEnableRepyTask);
                            AppLogic.runOnUiGround(mEnableRepyTask, 2000);
                            MessageActionCreator.get().replyVoice(mContact.mUserOpenId, false);

                            TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_VOICE_SEND);
                            mUsedReply = true;
                        }
                    }, 0);
                } else if (type.equals("CMD_CLOSE_CHAT")) {
                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mBtnBack.performClick();
                        }
                    }, 0);
                }

            }

            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return "CHAT_CONTROL";
            }
        }.addCommand("CMD_REPLY_WX", "回复微信").addCommand("CMD_CLOSE_CHAT", "关闭界面"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mUsedReply) {
            TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_CHAT_WITH_SEND);
        } else {
            TXZReportActionCreator.getInstance().report(ReportMessage.REPORT_UI_CHAT_WITHOUT_SEND);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        TtsActionCreator.get().skipRepeatMessage();
    }
}
