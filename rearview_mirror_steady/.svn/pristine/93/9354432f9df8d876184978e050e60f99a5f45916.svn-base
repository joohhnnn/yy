package com.txznet.webchat.ui.base.widgets;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxThemeStore;

import butterknife.Bind;

/**
 * 提供确定取消两个按钮的Dialog
 * <p>
 * Created by J on 2016/10/18.
 */

public class ConfirmCancelDialog extends AppBaseWinDialog implements View.OnClickListener {
    @Bind(R.id.ll_dialog_container)
    LinearLayout mLlDialogContainer;
    @Bind(R.id.tv_confirm)
    TextView mTvConfirm;
    @Bind(R.id.tv_cancel)
    TextView mTvCancel;
    @Bind(R.id.tv_title)
    TextView mTvTitle;

    private String[] mArrDefaultConfirmWords = {"确定"};
    private String[] mArrDefaultCancelWords = {"取消"};

    RecordStatusObservable.StatusObserver mRecordStatusObserver;

    private DialogListener mListener;

    protected String[] getConfirmWakeupKeyword() {
        return mArrDefaultConfirmWords;
    }

    protected String[] getCancelWakeupKeyword() {
        return mArrDefaultCancelWords;
    }

    @Override
    protected int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.layout_confirm_cancel_dialog_portrait;
        }

        return R.layout.layout_confirm_cancel_dialog;
    }

    @Override
    protected void init() {
        // init theme
        mLlDialogContainer.setBackground(getContext().getResources().getDrawable(R.drawable.shape_car_dialog_bg));
        mTvConfirm.setBackground(getContext().getResources().getDrawable(R.drawable.selector_car_dialog_button_bg_left));
        mTvCancel.setBackground(getContext().getResources().getDrawable(R.drawable.selector_car_dialog_button_bg_right));

        mTvConfirm.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);

        mRecordStatusObserver = new RecordStatusObservable.StatusObserver() {
            @Override
            public void onStatusChanged(boolean isShowing) {
                if (!AppStatusStore.get().isUIAsrEnabled()) {
                    return;
                }
                if (isShowing) {
                    TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
                } else {
                    if (isShowing()) {
                        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrExitCallback);
                    }
                }
            }
        };
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(
                new SimpleDrawableWrapper(mTvConfirm, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_left)),
                new SimpleDrawableWrapper(mTvCancel, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_right))
        );

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mTvCancel);
        }
    }

    public ConfirmCancelDialog(Context context, String title) {
        super(false);

        mTvTitle.setText(title);
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

            case R.id.tv_cancel:
                if (mListener != null) {
                    dismiss();
                    mListener.onCancel();
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

        AppLogic.registerRecordStatusObserver(mRecordStatusObserver);
        TXZAsrManager.getInstance().useWakeupAsAsr(mAsrExitCallback);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        try {
            AppLogic.unregisterRecordStatusObserver(mRecordStatusObserver);
        } catch (Exception e) {

        }

        TXZAsrManager.getInstance().recoverWakeupFromAsr("TASK_ASR_EXIT_CONFIRM");
    }

    public interface DialogListener {
        void onCommit();

        void onCancel();
    }

    /// Asr callback
    private TXZAsrManager.AsrComplexSelectCallback mAsrExitCallback = new TXZAsrManager.AsrComplexSelectCallback() {
        {
            addCommand("CONFIRM", "确定");
            addCommand("CANCEL", "取消");
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
                    } else if ("CANCEL".equals(type)) {
                        mTvCancel.performClick();
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

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}

