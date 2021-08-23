package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.FloatPointSP;
import com.txznet.comm.ui.theme.test.config.SceneInfoForward;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.dialog.IDialog;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;


/**
 * 录音界面
 * 无屏模式采用的布局
 */
public class WinLayoutNone2 extends IWinLayout {

    private static WinLayoutNone2 sInstance = new WinLayoutNone2();

    private WinLayoutNone2() {
    }

    public static WinLayoutNone2 getInstance() {
        return sInstance;
    }


    private Context mContext;
    private FrameLayout mRootLayout;

    private int mHistoryX;
    private int mHistoryY;
    private int mHorGravity = -1;
    private int mVerGravity = -1;

    private int mTalkHeight;    //对话框高度

    /*卡片最大宽度*/
    private int cardMaxWidth;
    /*卡片最大高度*/
    private int cardMaxHeight;
    private int isShowHelpButton = 0;
    private IDialog mDialog;

    private ExtViewAdapter mLastExtViewAdapter;


    private static class Holder {

        private View view;

        private ViewGroup cardWrap;
        private ViewGroup talkWrap;
        private ViewGroup llHome;
        private RelativeLayout relativeLayoutRecordView;
        private ViewGroup containerLayout;
        private ViewGroup extContainer;
        private View divider;  // 分隔线
        private ImageView ivHelp;               // 帮助按钮
        private ImageView ivSpeechBroadcast;    // 播报动画

        private ViewGroup layoutDialogWrap;             // 对话框

        Holder(View view) {
            this.view = view;
            cardWrap = view.findViewById(R.id.rootWrap);
            containerLayout = view.findViewById(R.id.container);
            extContainer = view.findViewById(R.id.extContainer);
            relativeLayoutRecordView = view.findViewById(R.id.rlRecordView);
            talkWrap = view.findViewById(R.id.talkWrap);
            llHome = view.findViewById(R.id.llHome);
            divider = view.findViewById(R.id.horDivider);
            ivHelp = view.findViewById(R.id.ivHelp);
            ivSpeechBroadcast = view.findViewById(R.id.ivSpeechBroadcast);
            ivSpeechBroadcast.setVisibility(View.GONE);

            layoutDialogWrap = view.findViewById(R.id.layoutDialogWrap);
            layoutDialogWrap.setVisibility(View.GONE);

            // 消费掉点击事件，点击可见View不会关闭声控界面
            cardWrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
//            extContainer.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

            // 点击帮助按钮
            ivHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP, 0, 0);

                    ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("voice_center").setType("touch_voice_center")
                            .putExtra("style", "help").setSessionId().buildCommReport());
                }
            });
        }
    }

    private Holder leftTopHolder;
    private Holder mHolder;

    @Override
    public void init() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.init()");

        //初始化页面列表数量
        SizeConfig.getInstance().init(3);
        // RecordWin2Manager.getInstance().updateDisplayArea(0, 0, SizeConfig.screenWidth, SizeConfig.screenHeight);
        Context context = UIResLoader.getInstance().getModifyContext();
        mContext = context;
        Resources resources = context.getResources();

        /*卡片最大宽度*/
        cardMaxWidth = (int) resources.getDimension(R.dimen.win_layout_card_width);
        /*卡片最大高度*/
        {
            float a = SizeConfig.screenHeight - resources.getDimension(R.dimen.win_layout_card_margin_ver_screen) * 2;
            float b = resources.getDimension(R.dimen.talk_logo_size) + 1 + resources.getDimension(R.dimen.item_height_normal) * 4F;
            float c = resources.getDimension(R.dimen.talk_logo_size) + 1 + resources.getDimension(R.dimen.item_height_large) * 3F;
            cardMaxHeight = (int) Math.min(a, Math.max(b, c));
        }


        //对话框高度80dp
        mTalkHeight = (int) resources.getDimension(R.dimen.talk_logo_size);

        if (mRootLayout == null) {
            leftTopHolder = new Holder(LayoutInflater.from(context).inflate(R.layout.win_none_left_top, (ViewGroup) null));

            mRootLayout = new FrameLayout(context);
            // 点击空白View关闭声控
            mRootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TXZAsrManager.getInstance().cancel();
                }
            });

            /* 声控界面默认左上角，除非用户定制 */
            mHolder = leftTopHolder;
            mRootLayout.addView(mHolder.view);

            mHistoryX = FloatPointSP.getInstance().getX();
            mHistoryY = FloatPointSP.getInstance().getY();

            changeLayoutBaseLocation();
        }
    }

    /**
     * 设置背景
     */
    @Override
    public void setBackground(Drawable drawable) {
        get().setBackground(drawable);
    }

    public void updateScreenType(int type) {
        updateScreenType(type, null);
    }

    /**
     * @param type 0: 只有助手说的文字
     *             1: 含content更多内容
     */
    public void updateScreenType(int type, ExtViewAdapter viewAdapter) {
        int measuredWidth = 0;
        int measuredHeight = 0;// 卡片高度，包括录音图标

        int viewType = -1;
        if (viewAdapter != null) {
            viewType = viewAdapter.type;
        }

        if (type == 0) {
            mHolder.cardWrap.measure(0, 0);
            measuredWidth = mHolder.cardWrap.getMeasuredWidth();
            measuredHeight = mHolder.cardWrap.getMeasuredHeight();
        } else {
            View view = mHolder.containerLayout.getChildAt(0);
            if (view != null) {
                view.measure(View.MeasureSpec.makeMeasureSpec(cardMaxWidth, View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(cardMaxHeight - mTalkHeight - 1, View.MeasureSpec.AT_MOST));
                measuredWidth = view.getMeasuredWidth();
                measuredHeight = view.getMeasuredHeight() + mTalkHeight + 1;
            }

            mHolder.containerLayout.measure(0, 0);
            measuredWidth = mHolder.containerLayout.getMeasuredWidth();
        }

        // 对话框卡片高度
        int dialogMaskHeight = measuredHeight;

        // 有卡片内容
        if (type == 1) {
            ViewGroup.LayoutParams containerLayoutLayoutParams = mHolder.containerLayout.getLayoutParams();
            containerLayoutLayoutParams.width = cardMaxWidth;
            containerLayoutLayoutParams.height = Math.min(measuredHeight - mTalkHeight - 1, cardMaxHeight - mTalkHeight - 1);

            dialogMaskHeight = Math.min(measuredHeight - mTalkHeight - 1, cardMaxHeight - mTalkHeight - 1);

            if (viewAdapter != null) {
                // 卡片高度调到最大
                if (viewAdapter.cardHeightType == ExtViewAdapter.SIZE_TYPE.MATCH_PARENT) {
                    containerLayoutLayoutParams.height = cardMaxHeight - mTalkHeight - 1;
                    dialogMaskHeight = cardMaxHeight;
                }
                // 录音图标都没有
                if (!viewAdapter.showRecordView) {
                    containerLayoutLayoutParams.height = cardMaxHeight;
                    dialogMaskHeight = cardMaxHeight;
                }
            }

            mHolder.containerLayout.setLayoutParams(containerLayoutLayoutParams);

            ViewGroup.LayoutParams lp = mHolder.cardWrap.getLayoutParams();
            lp.width = cardMaxWidth;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;// TODO
            mHolder.cardWrap.setLayoutParams(lp);

            // 有第二张卡片, 第二张卡片高度跟第一张一样
            if (mHolder.extContainer.getChildCount() > 0) {
                ViewGroup.LayoutParams extContainerLayoutParams = mHolder.extContainer.getLayoutParams();
                extContainerLayoutParams.height = containerLayoutLayoutParams.height + mTalkHeight + 1;
            }
        } else {
            ViewGroup.LayoutParams lp = mHolder.cardWrap.getLayoutParams();
            if (mHolder.containerLayout.getChildAt(0) == null) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {// 更新打字机文字，有卡片内容则固定宽度
                lp.width = cardMaxWidth;
            }
            mHolder.cardWrap.setLayoutParams(lp);
        }

        if (type == 0) {
            if (mDialog != null && mDialog.isShow()) {
                mDialog.dismiss();
            }
        } else {
            /*
             * 对话框庶罩层高度
             */
            if (mDialog != null && mDialog.isShow()) {
                mDialog.setMaskHeight(dialogMaskHeight);
            }
        }

        // 显示喇叭或帮助按钮，减小文字右边距
        if (mHolder.ivSpeechBroadcast.getVisibility() == View.VISIBLE
                || mHolder.ivHelp.getVisibility() == View.VISIBLE) {
            mHolder.talkWrap.setPadding(0, 0, 0, 0);
        } else {
            int p = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.mdp20);
            mHolder.talkWrap.setPadding(0, 0, p, 0);
        }
    }

    /**
     * 显示对话界面
     * 重置状态可以放到这里
     */
    public void openWin() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.showChatView() " + "");
        // 显示帮助按钮
        isShowHelpButton = 0;
        // 隐藏播报中按钮
        setSpeechBroadcast(false);
        LogUtil.logd(WinLayout.logTag + "setSpeechBroadcast()");
        // 移除第二张卡片内容
        mHolder.extContainer.removeAllViews();
        mHolder.extContainer.setVisibility(View.GONE);
        // 显示帮助按钮
        mHolder.ivHelp.setVisibility(View.VISIBLE);

        if (mHolder.talkWrap != null && mHolder.containerLayout != null) {
            mHolder.containerLayout.removeAllViews();
            mHolder.containerLayout.setVisibility(View.GONE);
            mHolder.talkWrap.removeAllViews();
            mHolder.talkWrap.setVisibility(View.VISIBLE);
            updateScreenType(0);
        }

    }

    /**
     * 对布局进行位置调整
     */
    private void changeLayoutBaseLocation() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.changeLayoutBaseLocation() " + "");
        int horGravity = mHistoryX < SizeConfig.screenWidth / 2 ? Gravity.START : Gravity.END;
        int verGravity = mHistoryY < SizeConfig.screenHeight / 2 ? Gravity.TOP : Gravity.BOTTOM;

        if (mHorGravity != horGravity || mVerGravity != verGravity) {
            mHorGravity = horGravity;
            mVerGravity = verGravity;
            // 声控界面默认左上角，除非用户定制
//            mRootLayout.removeAllViews();
//            mRootLayout.addView(mHolder.view);
        }
    }

    /**
     * 添加录音图标
     */
    @Override
    public void addRecordView(View recordView) {
        if (recordView != null) {
            // 这种形式的录音动画直接在聊天View里面的
            LogUtil.logd(WinLayout.logTag + "WinLayoutNone.addRecordView() " + "");

            int tempX = FloatPointSP.getInstance().getX();
            int tempY = FloatPointSP.getInstance().getY();
            if ((mHistoryX != tempX) || (mHistoryY != tempY)) {
                mHistoryX = tempX;
                mHistoryY = tempY;
                changeLayoutBaseLocation();
            }

            ViewGroup parent = (ViewGroup) recordView.getParent();
            if (parent == null) {
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                mHolder.relativeLayoutRecordView.addView(recordView, lp);
            } else if (parent != mHolder.relativeLayoutRecordView) {
                parent.removeAllViews();
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                mHolder.relativeLayoutRecordView.addView(recordView, lp);
            }

            updateScreenType(0);
        }

    }

    @Override
    public View get() {
        return mRootLayout;
    }

    /**
     * 添加View到对应的地方
     *
     * @param targetView    {@link RecordWinController}
     * @param view          {@link ViewBase}
     * @param winManLayoutP
     */
    @Override
    public Object addView(int targetView, View view, ViewGroup.LayoutParams winManLayoutP) {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone2.addView vTips:" + WinLayout.getInstance().vTips);
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone2.addView targetView:" + targetView);

        {
            // 显示录音图标
            mHolder.llHome.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = mHolder.llHome.getLayoutParams();
            lp.height = mTalkHeight;
            mHolder.llHome.setLayoutParams(lp);
        }


        /*录音动画*/
        if (targetView == RecordWinController.TARGET_VIEW_MIC) {
            return null;
        }

        int type = -1;
        ViewBase viewBase = null;
        ExtViewAdapter extViewAdapter = null;

        Object tag = view.getTag();
        if (tag instanceof ExtViewAdapter) {
            extViewAdapter = (ExtViewAdapter) tag;
        }
        if (tag instanceof ViewAdapter) {
            ViewAdapter viewAdapter = (ViewAdapter) tag;
            viewBase = (ViewBase) viewAdapter.object;
            type = viewAdapter.type;
        } else if (tag instanceof Integer) {
            type = (int) tag;
        }

        LogUtil.logd(WinLayout.logTag + "WinLayoutNone2.addView targetView:" + targetView + ", type:" + type);


        // 移除对话框逻辑
        if (mDialog != null && mDialog.getViewBase() != null && mDialog.getViewBase() != viewBase) {
            mDialog.dismiss();
        }

        switch (type) {
            case ViewData.TYPE_CHAT_HELP_TIPS:// 23 帮助提示，不显示
                break;
            case ViewData.TYPE_CHAT_TO_SYS_PART:// 27: 打字消息

                speechBroadcastIsSpeak = true;// 在播报中说了话
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);

                if (isShowHelpButton >= 1) {
                    // 隐藏帮助按钮
                    mHolder.ivHelp.setVisibility(View.GONE);
                }
                isShowHelpButton++;

                mHolder.talkWrap.removeAllViews();
                mHolder.talkWrap.setVisibility(View.VISIBLE);
                mHolder.talkWrap.addView(view, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (mLastExtViewAdapter != null
                        && !mLastExtViewAdapter.isListView
                        && !mLastExtViewAdapter.showRecordView) {

                    mHolder.containerLayout.setVisibility(View.GONE);
                    if (mHolder.containerLayout.getChildCount() > 0) {
                        mHolder.containerLayout.removeAllViews();
                        if (mLastExtViewAdapter != null && mLastExtViewAdapter.callback != null) {
                            mLastExtViewAdapter.callback.dismiss();
                        }
                    }
                }
                updateScreenType(0);
                break;
            case ViewData.TYPE_CHAT_FROM_SYS:// 1
            case ViewData.TYPE_CHAT_TO_SYS:// 2
            case ViewData.TYPE_CHAT_FROM_SYS_HL:// 21: 高亮标注的系统文本

                speechBroadcastIsSpeak = true;// 在播报中说了话
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);

                if (isShowHelpButton >= 1) {
                    // 隐藏帮助按钮
                    mHolder.ivHelp.setVisibility(View.GONE);
                }
                isShowHelpButton++;

                mHolder.containerLayout.setVisibility(View.GONE);
                if (mHolder.containerLayout.getChildCount() > 0) {
                    mHolder.containerLayout.removeAllViews();
                    if (extViewAdapter != null && extViewAdapter.callback != null) {
                        extViewAdapter.callback.dismiss();
                    }
                }

                // 隐藏分隔线
                mHolder.divider.setVisibility(View.GONE);
                mHolder.talkWrap.removeAllViews();
                mHolder.talkWrap.setVisibility(View.VISIBLE);
                mHolder.talkWrap.addView(view, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                if(mLastExtViewAdapter != null
//                        && !mLastExtViewAdapter.isListView
//                        && !mLastExtViewAdapter.showRecordView){
//                    mHolder.containerLayout.removeAllViews();
//                    mHolder.containerLayout.setVisibility(View.GONE);
//                }
                updateScreenType(0);
                break;
            /* 大段文字不显示，直接播报*/
            case ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT:// 22: 带打断提示的系统文本
                mHolder.divider.setVisibility(View.GONE);
                mHolder.containerLayout.setVisibility(View.GONE);
                if (mHolder.containerLayout.getChildCount() > 0) {
                    mHolder.containerLayout.removeAllViews();
                    if (mLastExtViewAdapter != null && mLastExtViewAdapter.callback != null) {
                        mLastExtViewAdapter.callback.dismiss();
                    }
                }

                if (extViewAdapter != null && extViewAdapter.callback != null) {
                    extViewAdapter.callback.dismiss();
                }

                // 移除第二张卡片内容
                mHolder.extContainer.removeAllViews();
                mHolder.extContainer.setVisibility(View.GONE);

                // 显示播报小喇叭
                setSpeechBroadcast(true);

                updateScreenType(0);
                break;
            default:
                setGuideText();

                //隐藏播报按钮
                mHolder.ivSpeechBroadcast.setVisibility(View.GONE);

                // 移除第二张卡片内容
                mHolder.extContainer.removeAllViews();
                mHolder.extContainer.setVisibility(View.GONE);

                /* 隐藏帮助按钮 */
                isShowHelpButton++;
                mHolder.ivHelp.setVisibility(View.GONE);
                // 显示分隔线
                mHolder.divider.setVisibility(View.VISIBLE);

                /*卡片内容*/

                mHolder.containerLayout.removeAllViews();
                if (mHolder.containerLayout.getChildCount() > 0) {
                    if (mLastExtViewAdapter != null && mLastExtViewAdapter.callback != null) {
                        mLastExtViewAdapter.callback.dismiss();
                    }
                }
                mHolder.containerLayout.setVisibility(View.VISIBLE);
                mHolder.containerLayout.addView(view, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                if (tag instanceof ExtViewAdapter) {
                    ExtViewAdapter adapter = (ExtViewAdapter) tag;

                    // 第二张卡片
                    if (adapter.extView != null) {
                        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        mHolder.extContainer.addView(adapter.extView, lp);
                        mHolder.extContainer.setVisibility(View.VISIBLE);
                    }

                    // 录音图标
                    if (!adapter.showRecordView) {
                        mHolder.llHome.setVisibility(View.GONE);
                    }
                }

                updateScreenType(1, extViewAdapter);
                break;
        }

        // 卡片没有内容则去掉每二张卡片
        if (mHolder.containerLayout.getChildAt(0) == null) {
            mHolder.extContainer.removeAllViews();
            mHolder.extContainer.setVisibility(View.GONE);
        }

        mLastExtViewAdapter = extViewAdapter;

        return null;
    }

    @Override
    public Object removeLastView() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.removeLastView()");
        return null;
    }

    long lastResetTime;

    /**
     * 重置聊天记录，界面关闭时会调用
     */
    @Override
    public void reset() {
        long now = SystemClock.elapsedRealtime();
        if ((now - lastResetTime) < 50) {
            return;
        }
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.reset()");
        lastResetTime = now;

        if (mHolder.talkWrap != null) {
            mHolder.talkWrap.removeAllViews();
        }

        if (mHolder.containerLayout != null) {
            mHolder.containerLayout.removeAllViews();
            mHolder.containerLayout.setVisibility(View.GONE);
        }

        if (mHolder.extContainer != null) {
            mHolder.extContainer.removeAllViews();
            mHolder.extContainer.setVisibility(View.GONE);
        }

        // 关闭对话框
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mLastExtViewAdapter != null && mLastExtViewAdapter.callback != null) {
            mLastExtViewAdapter.callback.dismiss();
        }
    }

    /**
     * 释放内存，界面关闭时会调用
     */
    @Override
    public void release() {
        LogUtil.logd(WinLayout.logTag + "winLayoutNone.release()");
        // TODO
//        mRootLayout.removeAllViews();
//        mRootLayout = null;
//        mHolder.relativeLayoutRecordView.removeAllViews();
    }

    /**
     * 显示引导语
     */
    public void setGuideText() {
        ChatFromSysViewData tipViewData = new ChatFromSysViewData();
        ViewAdapter viewAdapter = null;
        LogUtil.logd(WinLayout.logTag + "none setGuideText:" + WinLayout.getInstance().vTips);
        if (WinLayout.getInstance().vTips != null) {
            String text = WinLayout.getInstance().vTips;
            WinLayout.getInstance().vTips = null;
            if (text.startsWith("请说")) {
                int end = text.indexOf('”');
                SpannableString spannableString = new SpannableString(text);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF16CFFF"));
                spannableString.setSpan(colorSpan, 3, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewAdapter = ChatFromSysView.getInstance().getView(tipViewData, spannableString);
            } else if ("播报中".equals(text) || text.startsWith("打开语音助手")) {
                tipViewData.textContent = text;
                viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);
            } else {
                String[] texts = text.split("；");
                // 最多显示两个气泡
                if (texts.length > 2) {
                    texts = new String[]{texts[0], texts[1]};
                }
                viewAdapter = ChatFromSysView.getInstance().getView(texts);
            }
            mHolder.talkWrap.removeAllViews();
            mHolder.talkWrap.setVisibility(View.VISIBLE);
            LayoutParams llParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llParams.gravity = Gravity.CENTER;
            mHolder.talkWrap.addView(viewAdapter.view, llParams);
            WinLayout.getInstance().vTips = null;
        }
    }

    /**
     * 是否是播报中
     *
     * @param isSpeechBroadcast
     */
    public void setSpeechBroadcast(boolean isSpeechBroadcast) {
        Message msg = handler.obtainMessage();
        msg.arg1 = isSpeechBroadcast ? 1 : 0;
        handler.sendMessageDelayed(msg, 1);
    }

    public void setSpeechBroadcastNow(boolean isSpeechBroadcast) {
        if (isSpeechBroadcast && mHolder.containerLayout.getChildAt(0) == null) {
            mHolder.ivSpeechBroadcast.setVisibility(View.VISIBLE);
            AnimationDrawable animationDrawable = (AnimationDrawable) mHolder.ivSpeechBroadcast.getBackground();
            if (!animationDrawable.isRunning()) {
                animationDrawable.start();
            }
            WinLayout.getInstance().vTips = "播报中";
            setGuideText();
        } else {
            mHolder.ivSpeechBroadcast.setVisibility(View.GONE);
        }

        if (!isSpeechBroadcast) {
            LogUtil.logd(WinLayout.logTag + "播报完 vTip:" + WinLayout.getInstance().vTips);
            // 播报完后删除"播报中"的文字，天气、星座运势播报完后不删除打断词
            if ("播报中".equals(WinLayout.getInstance().vTips)) {
                WinLayout.getInstance().vTips = null;
                mHolder.talkWrap.removeAllViews();
                mHolder.talkWrap.setVisibility(View.GONE);
            } else {
                try {
                    // 播报完后删除"播报中"的文字，天气、星座运势播报完后不删除打断词
                    View view = mHolder.talkWrap.getChildAt(0);
                    if (view != null) {
                        if (view instanceof TextView) {
                            TextView tv = (TextView) view;
                            String text = tv.getText().toString();
                            if ("播报中".equals(text)) {
                                LogUtil.logd(WinLayout.logTag + "播报完删除'播报中'" + WinLayout.getInstance().vTips);
                                mHolder.talkWrap.removeAllViews();
                                mHolder.talkWrap.setVisibility(View.GONE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 显示喇叭或帮助按钮，减小文字右边距
        if (mHolder.ivSpeechBroadcast.getVisibility() == View.VISIBLE
                || mHolder.ivHelp.getVisibility() == View.VISIBLE) {
            mHolder.talkWrap.setPadding(0, 0, 0, 0);
        } else {
            int p = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.mdp20);
            mHolder.talkWrap.setPadding(0, 0, p, 0);
        }
    }

    /**
     * 要在队列中更新播报状态，不然会出列小喇叭不显示
     */
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            boolean isSpeechBroadcast = (msg.arg1 == 1);
            LogUtil.logd(WinLayout.logTag + "WinLayoutNone.setSpeechBroadcast():" + isSpeechBroadcast);
            setSpeechBroadcastNow(isSpeechBroadcast);
        }
    };

    /**
     * 显示对话框
     *
     * @param dialog
     */
    public void showDialog(IDialog dialog) {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.showDialog()");
        this.mDialog = dialog;
        mHolder.layoutDialogWrap.removeAllViews();
        mHolder.layoutDialogWrap.addView(dialog.getView());
        mHolder.layoutDialogWrap.setVisibility(View.VISIBLE);
        mDialog.setMaskHeight(View.MeasureSpec.getSize(mHolder.cardWrap.getHeight()));
    }

    /**
     * 关闭对话框
     *
     * @param dialog
     */
    public void dismissDialog(IDialog dialog) {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.dismissDialog()");
        this.mDialog = null;
        mHolder.layoutDialogWrap.removeAllViews();
        mHolder.layoutDialogWrap.setVisibility(View.GONE);
    }

    /**
     * 播报中用户是否说了话
     */
    private boolean speechBroadcastIsSpeak = false;

    /**
     * 更新录音界面状态
     * <p>
     * 这里是主线程调用
     *
     * @param state {@link com.txznet.comm.ui.viewfactory.view.IRecordView}
     */
    public void onUpdateState(int state) {
        switch (state) {
            case IRecordView.STATE_NORMAL: // 0: 播报

                break;
            case IRecordView.STATE_RECORD_START: // 1: 录音

                break;
            case IRecordView.STATE_RECORD_END: // 2: 处理

                break;
            case IRecordView.STATE_WIN_OPEN: // 3: 窗口打开
                speechBroadcastIsSpeak = false;
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);
                break;
            case IRecordView.STATE_WIN_CLOSE: // 4: 窗口关闭
                break;
            case IRecordView.STATE_SPEAK_START: { // 5: TTS播报
                speechBroadcastIsSpeak = false;
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);
                break;
            }
            case IRecordView.STATE_SPEAK_END: { // 6: TTS播报结束
                if (speechBroadcastIsSpeak) {
                    break;
                }
                /*没有卡片内容*/
                if (mHolder.containerLayout.getChildCount() == 0) {
                    AppLogicBase.runOnUiGround(closeSingleLineUI, 3 * 1000);
                }
                /*有卡片内容*/
                else {
                    Object tag = mHolder.containerLayout.getChildAt(0).getTag();
                    if (tag instanceof ExtViewAdapter) {
                        ExtViewAdapter adapter = (ExtViewAdapter) tag;
                        /*指定场景自动关闭*/
                        switch (adapter.type) {
                            case ViewData.TYPE_CHAT_WEATHER:// 天气
                            case ViewData.TYPE_CHAT_SHARE:// 股票
                            case ViewData.TYPE_CHAT_COMPETITION_DETAIL:// 赛事
                            case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:// 星座匹配
                            case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:// 星座运势
                                AppLogicBase.runOnUiGround(closeNormalUI, 5 * 1000);
                                break;
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 关闭单行文本界面
     * <p>
     * eg: 3 + 4 = ？
     */
    private Runnable closeSingleLineUI = new Runnable() {
        @Override
        public void run() {
            mHolder.talkWrap.removeAllViews();
            mHolder.talkWrap.setVisibility(View.GONE);
            mHolder.ivHelp.setVisibility(View.GONE);
            updateScreenType(0);
        }
    };

    /**
     * 关闭有卡片的闲聊界面
     * <p>
     * eg: 天气、股票
     */
    private Runnable closeNormalUI = new Runnable() {
        @Override
        public void run() {
            mHolder.talkWrap.removeAllViews();
            mHolder.talkWrap.setVisibility(View.GONE);
            mHolder.ivHelp.setVisibility(View.GONE);
            mHolder.containerLayout.removeAllViews();
            mHolder.containerLayout.setVisibility(View.GONE);
            updateScreenType(0);
        }
    };

}
