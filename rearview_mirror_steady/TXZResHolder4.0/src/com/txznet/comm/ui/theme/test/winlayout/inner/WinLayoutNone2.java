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
 * ????????????
 * ???????????????????????????
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

    private int mTalkHeight;    //???????????????

    /*??????????????????*/
    private int cardMaxWidth;
    /*??????????????????*/
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
        private View divider;  // ?????????
        private ImageView ivHelp;               // ????????????
        private ImageView ivSpeechBroadcast;    // ????????????

        private ViewGroup layoutDialogWrap;             // ?????????

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

            // ????????????????????????????????????View????????????????????????
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

            // ??????????????????
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

        //???????????????????????????
        SizeConfig.getInstance().init(3);
        // RecordWin2Manager.getInstance().updateDisplayArea(0, 0, SizeConfig.screenWidth, SizeConfig.screenHeight);
        Context context = UIResLoader.getInstance().getModifyContext();
        mContext = context;
        Resources resources = context.getResources();

        /*??????????????????*/
        cardMaxWidth = (int) resources.getDimension(R.dimen.win_layout_card_width);
        /*??????????????????*/
        {
            float a = SizeConfig.screenHeight - resources.getDimension(R.dimen.win_layout_card_margin_ver_screen) * 2;
            float b = resources.getDimension(R.dimen.talk_logo_size) + 1 + resources.getDimension(R.dimen.item_height_normal) * 4F;
            float c = resources.getDimension(R.dimen.talk_logo_size) + 1 + resources.getDimension(R.dimen.item_height_large) * 3F;
            cardMaxHeight = (int) Math.min(a, Math.max(b, c));
        }


        //???????????????80dp
        mTalkHeight = (int) resources.getDimension(R.dimen.talk_logo_size);

        if (mRootLayout == null) {
            leftTopHolder = new Holder(LayoutInflater.from(context).inflate(R.layout.win_none_left_top, (ViewGroup) null));

            mRootLayout = new FrameLayout(context);
            // ????????????View????????????
            mRootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TXZAsrManager.getInstance().cancel();
                }
            });

            /* ???????????????????????????????????????????????? */
            mHolder = leftTopHolder;
            mRootLayout.addView(mHolder.view);

            mHistoryX = FloatPointSP.getInstance().getX();
            mHistoryY = FloatPointSP.getInstance().getY();

            changeLayoutBaseLocation();
        }
    }

    /**
     * ????????????
     */
    @Override
    public void setBackground(Drawable drawable) {
        get().setBackground(drawable);
    }

    public void updateScreenType(int type) {
        updateScreenType(type, null);
    }

    /**
     * @param type 0: ????????????????????????
     *             1: ???content????????????
     */
    public void updateScreenType(int type, ExtViewAdapter viewAdapter) {
        int measuredWidth = 0;
        int measuredHeight = 0;// ?????????????????????????????????

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

        // ?????????????????????
        int dialogMaskHeight = measuredHeight;

        // ???????????????
        if (type == 1) {
            ViewGroup.LayoutParams containerLayoutLayoutParams = mHolder.containerLayout.getLayoutParams();
            containerLayoutLayoutParams.width = cardMaxWidth;
            containerLayoutLayoutParams.height = Math.min(measuredHeight - mTalkHeight - 1, cardMaxHeight - mTalkHeight - 1);

            dialogMaskHeight = Math.min(measuredHeight - mTalkHeight - 1, cardMaxHeight - mTalkHeight - 1);

            if (viewAdapter != null) {
                // ????????????????????????
                if (viewAdapter.cardHeightType == ExtViewAdapter.SIZE_TYPE.MATCH_PARENT) {
                    containerLayoutLayoutParams.height = cardMaxHeight - mTalkHeight - 1;
                    dialogMaskHeight = cardMaxHeight;
                }
                // ?????????????????????
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

            // ??????????????????, ???????????????????????????????????????
            if (mHolder.extContainer.getChildCount() > 0) {
                ViewGroup.LayoutParams extContainerLayoutParams = mHolder.extContainer.getLayoutParams();
                extContainerLayoutParams.height = containerLayoutLayoutParams.height + mTalkHeight + 1;
            }
        } else {
            ViewGroup.LayoutParams lp = mHolder.cardWrap.getLayoutParams();
            if (mHolder.containerLayout.getChildAt(0) == null) {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            } else {// ??????????????????????????????????????????????????????
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
             * ????????????????????????
             */
            if (mDialog != null && mDialog.isShow()) {
                mDialog.setMaskHeight(dialogMaskHeight);
            }
        }

        // ???????????????????????????????????????????????????
        if (mHolder.ivSpeechBroadcast.getVisibility() == View.VISIBLE
                || mHolder.ivHelp.getVisibility() == View.VISIBLE) {
            mHolder.talkWrap.setPadding(0, 0, 0, 0);
        } else {
            int p = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.mdp20);
            mHolder.talkWrap.setPadding(0, 0, p, 0);
        }
    }

    /**
     * ??????????????????
     * ??????????????????????????????
     */
    public void openWin() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.showChatView() " + "");
        // ??????????????????
        isShowHelpButton = 0;
        // ?????????????????????
        setSpeechBroadcast(false);
        LogUtil.logd(WinLayout.logTag + "setSpeechBroadcast()");
        // ???????????????????????????
        mHolder.extContainer.removeAllViews();
        mHolder.extContainer.setVisibility(View.GONE);
        // ??????????????????
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
     * ???????????????????????????
     */
    private void changeLayoutBaseLocation() {
        LogUtil.logd(WinLayout.logTag + "WinLayoutNone.changeLayoutBaseLocation() " + "");
        int horGravity = mHistoryX < SizeConfig.screenWidth / 2 ? Gravity.START : Gravity.END;
        int verGravity = mHistoryY < SizeConfig.screenHeight / 2 ? Gravity.TOP : Gravity.BOTTOM;

        if (mHorGravity != horGravity || mVerGravity != verGravity) {
            mHorGravity = horGravity;
            mVerGravity = verGravity;
            // ????????????????????????????????????????????????
//            mRootLayout.removeAllViews();
//            mRootLayout.addView(mHolder.view);
        }
    }

    /**
     * ??????????????????
     */
    @Override
    public void addRecordView(View recordView) {
        if (recordView != null) {
            // ??????????????????????????????????????????View?????????
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
     * ??????View??????????????????
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
            // ??????????????????
            mHolder.llHome.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = mHolder.llHome.getLayoutParams();
            lp.height = mTalkHeight;
            mHolder.llHome.setLayoutParams(lp);
        }


        /*????????????*/
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


        // ?????????????????????
        if (mDialog != null && mDialog.getViewBase() != null && mDialog.getViewBase() != viewBase) {
            mDialog.dismiss();
        }

        switch (type) {
            case ViewData.TYPE_CHAT_HELP_TIPS:// 23 ????????????????????????
                break;
            case ViewData.TYPE_CHAT_TO_SYS_PART:// 27: ????????????

                speechBroadcastIsSpeak = true;// ?????????????????????
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);

                if (isShowHelpButton >= 1) {
                    // ??????????????????
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
            case ViewData.TYPE_CHAT_FROM_SYS_HL:// 21: ???????????????????????????

                speechBroadcastIsSpeak = true;// ?????????????????????
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);

                if (isShowHelpButton >= 1) {
                    // ??????????????????
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

                // ???????????????
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
            /* ????????????????????????????????????*/
            case ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT:// 22: ??????????????????????????????
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

                // ???????????????????????????
                mHolder.extContainer.removeAllViews();
                mHolder.extContainer.setVisibility(View.GONE);

                // ?????????????????????
                setSpeechBroadcast(true);

                updateScreenType(0);
                break;
            default:
                setGuideText();

                //??????????????????
                mHolder.ivSpeechBroadcast.setVisibility(View.GONE);

                // ???????????????????????????
                mHolder.extContainer.removeAllViews();
                mHolder.extContainer.setVisibility(View.GONE);

                /* ?????????????????? */
                isShowHelpButton++;
                mHolder.ivHelp.setVisibility(View.GONE);
                // ???????????????
                mHolder.divider.setVisibility(View.VISIBLE);

                /*????????????*/

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

                    // ???????????????
                    if (adapter.extView != null) {
                        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT);
                        mHolder.extContainer.addView(adapter.extView, lp);
                        mHolder.extContainer.setVisibility(View.VISIBLE);
                    }

                    // ????????????
                    if (!adapter.showRecordView) {
                        mHolder.llHome.setVisibility(View.GONE);
                    }
                }

                updateScreenType(1, extViewAdapter);
                break;
        }

        // ??????????????????????????????????????????
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
     * ?????????????????????????????????????????????
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

        // ???????????????
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mLastExtViewAdapter != null && mLastExtViewAdapter.callback != null) {
            mLastExtViewAdapter.callback.dismiss();
        }
    }

    /**
     * ???????????????????????????????????????
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
     * ???????????????
     */
    public void setGuideText() {
        ChatFromSysViewData tipViewData = new ChatFromSysViewData();
        ViewAdapter viewAdapter = null;
        LogUtil.logd(WinLayout.logTag + "none setGuideText:" + WinLayout.getInstance().vTips);
        if (WinLayout.getInstance().vTips != null) {
            String text = WinLayout.getInstance().vTips;
            WinLayout.getInstance().vTips = null;
            if (text.startsWith("??????")) {
                int end = text.indexOf('???');
                SpannableString spannableString = new SpannableString(text);
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF16CFFF"));
                spannableString.setSpan(colorSpan, 3, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                viewAdapter = ChatFromSysView.getInstance().getView(tipViewData, spannableString);
            } else if ("?????????".equals(text) || text.startsWith("??????????????????")) {
                tipViewData.textContent = text;
                viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);
            } else {
                String[] texts = text.split("???");
                // ????????????????????????
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
     * ??????????????????
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
            WinLayout.getInstance().vTips = "?????????";
            setGuideText();
        } else {
            mHolder.ivSpeechBroadcast.setVisibility(View.GONE);
        }

        if (!isSpeechBroadcast) {
            LogUtil.logd(WinLayout.logTag + "????????? vTip:" + WinLayout.getInstance().vTips);
            // ??????????????????"?????????"???????????????????????????????????????????????????????????????
            if ("?????????".equals(WinLayout.getInstance().vTips)) {
                WinLayout.getInstance().vTips = null;
                mHolder.talkWrap.removeAllViews();
                mHolder.talkWrap.setVisibility(View.GONE);
            } else {
                try {
                    // ??????????????????"?????????"???????????????????????????????????????????????????????????????
                    View view = mHolder.talkWrap.getChildAt(0);
                    if (view != null) {
                        if (view instanceof TextView) {
                            TextView tv = (TextView) view;
                            String text = tv.getText().toString();
                            if ("?????????".equals(text)) {
                                LogUtil.logd(WinLayout.logTag + "???????????????'?????????'" + WinLayout.getInstance().vTips);
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

        // ???????????????????????????????????????????????????
        if (mHolder.ivSpeechBroadcast.getVisibility() == View.VISIBLE
                || mHolder.ivHelp.getVisibility() == View.VISIBLE) {
            mHolder.talkWrap.setPadding(0, 0, 0, 0);
        } else {
            int p = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.mdp20);
            mHolder.talkWrap.setPadding(0, 0, p, 0);
        }
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
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
     * ???????????????
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
     * ???????????????
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
     * ??????????????????????????????
     */
    private boolean speechBroadcastIsSpeak = false;

    /**
     * ????????????????????????
     * <p>
     * ????????????????????????
     *
     * @param state {@link com.txznet.comm.ui.viewfactory.view.IRecordView}
     */
    public void onUpdateState(int state) {
        switch (state) {
            case IRecordView.STATE_NORMAL: // 0: ??????

                break;
            case IRecordView.STATE_RECORD_START: // 1: ??????

                break;
            case IRecordView.STATE_RECORD_END: // 2: ??????

                break;
            case IRecordView.STATE_WIN_OPEN: // 3: ????????????
                speechBroadcastIsSpeak = false;
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);
                break;
            case IRecordView.STATE_WIN_CLOSE: // 4: ????????????
                break;
            case IRecordView.STATE_SPEAK_START: { // 5: TTS??????
                speechBroadcastIsSpeak = false;
                AppLogicBase.removeUiGroundCallback(closeSingleLineUI);
                AppLogicBase.removeUiGroundCallback(closeNormalUI);
                break;
            }
            case IRecordView.STATE_SPEAK_END: { // 6: TTS????????????
                if (speechBroadcastIsSpeak) {
                    break;
                }
                /*??????????????????*/
                if (mHolder.containerLayout.getChildCount() == 0) {
                    AppLogicBase.runOnUiGround(closeSingleLineUI, 3 * 1000);
                }
                /*???????????????*/
                else {
                    Object tag = mHolder.containerLayout.getChildAt(0).getTag();
                    if (tag instanceof ExtViewAdapter) {
                        ExtViewAdapter adapter = (ExtViewAdapter) tag;
                        /*????????????????????????*/
                        switch (adapter.type) {
                            case ViewData.TYPE_CHAT_WEATHER:// ??????
                            case ViewData.TYPE_CHAT_SHARE:// ??????
                            case ViewData.TYPE_CHAT_COMPETITION_DETAIL:// ??????
                            case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:// ????????????
                            case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:// ????????????
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
     * ????????????????????????
     * <p>
     * eg: 3 + 4 = ???
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
     * ??????????????????????????????
     * <p>
     * eg: ???????????????
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
