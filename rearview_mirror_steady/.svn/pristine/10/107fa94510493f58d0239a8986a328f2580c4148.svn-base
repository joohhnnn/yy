package com.txznet.webchat.ui.base.widgets;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxThemeStore;

import butterknife.Bind;

/**
 * 提供确定按钮的Dialog
 * <p>
 * Created by J on 2016/10/18.
 */

public class ConfirmDialog extends AppBaseWinDialog implements View.OnClickListener {
    @Bind(R.id.ll_dialog_container)
    LinearLayout mLlDialogContainer;
    @Bind(R.id.tv_confirm)
    TextView mTvConfirm;
    @Bind(R.id.tv_title)
    TextView mTvTitle;

    private String[] mArrDefaultConfirmWords = {"确定", "我知道了"};

    RecordStatusObservable.StatusObserver mRecordStatusObserver;

    private DialogListener mListener;

    private int mTtsTaskId = -1;

    protected String[] getConfirmWakeupKeyword() {
        return mArrDefaultConfirmWords;
    }

    protected String getTtsText() {
        return null;
    }

    @Override
    protected int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.layout_confirm_dialog_portrait;
        }

        return R.layout.layout_confirm_dialog;
    }

    @Override
    protected void init() {
        // init theme
        mLlDialogContainer.setBackground(getContext().getResources().getDrawable(R.drawable.shape_car_dialog_bg));

        mTvConfirm.setOnClickListener(this);

        mRecordStatusObserver = new RecordStatusObservable.StatusObserver() {
            @Override
            public void onStatusChanged(boolean isShowing) {
                if (isShowing) {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_CONFIRM");
                } else {
                    if (isShowing()) {
                        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrCallback);
                    }
                }
            }
        };
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(
                new SimpleDrawableWrapper(mTvConfirm, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_left))
        );

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mTvConfirm);
        }
    }

    public ConfirmDialog() {
        super(false);
    }

    public void setDialogListener(DialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_confirm:
                if (mListener != null) {
                    dismiss();
                    mListener.onCommit();
                }
                break;
        }
    }

    @Override
    public void show() {
        if (isShowing()) {
            return;
        }

        super.show();

        if (!TextUtils.isEmpty(getTtsText())) {
            mTtsTaskId = TtsUtil.speakText(getTtsText(), TtsUtil.PreemptType.PREEMPT_TYPE_NEXT);
        }

        AppLogic.registerRecordStatusObserver(mRecordStatusObserver);
        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrCallback);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        try {
            AppLogic.unregisterRecordStatusObserver(mRecordStatusObserver);
        } catch (Exception e) {

        }

        if (mTtsTaskId != -1) {
            TtsUtil.cancelSpeak(mTtsTaskId);
        }

        TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
    }

    public interface DialogListener {
        void onCommit();
    }

    /// Asr callback
    private TXZAsrManager.AsrComplexSelectCallback mAsrCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        {
            addCommand("CONFIRM", getConfirmWakeupKeyword());
        }

        @Override
        public void onCommandSelected(final String type, String command) {
            if (!isShowing()) {
                return;
            }
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    if ("CONFIRM".equals(type)) {
                        mTvConfirm.performClick();
                    }
                }
            }, 0);
        }

        @Override
        public String getTaskId() {
            return "TASK_ASR_CONFIRM";
        }

        @Override
        public boolean needAsrState() {
            return false;
        }
    };

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}

