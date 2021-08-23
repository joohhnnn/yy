package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;

import butterknife.Bind;

/**
 * 退出车载微信确认对话框
 * Created by J on 2016/3/23.
 */
public class ExitDialog extends AppBaseWinDialog implements View.OnClickListener {
    @Bind(R.id.tv_exit_commit)
    TextView mTvCommit;
    @Bind(R.id.tv_exit_cancel)
    TextView mTvCancel;

    private ExitDialogListener mListener;

    public ExitDialog(Context context) {
        super(false);
    }

    @Override
    public int getLayout() {
        return R.layout.layout_exit_dialog;
    }

    @Override
    public void init() {
        this.setCanceledOnTouchOutside(false);
        mTvCommit.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(
                new SimpleDrawableWrapper(mTvCommit, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_left_full)),
                new SimpleDrawableWrapper(mTvCancel, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_right_full))
        );

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mTvCancel);
        }
    }

    public void setExitDialogListener(ExitDialogListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_exit_commit:
                if (mListener != null) {
                    mListener.onCommit();
                }
                break;

            case R.id.tv_exit_cancel:
                if (mListener != null) {
                    mListener.onCancel();
                }
                break;
        }
    }

    public void performClickCommit() {
        if (mTvCommit != null) {
            mTvCommit.performClick();
        }
    }

    public void performClickCancel() {
        if (mTvCancel != null) {
            mTvCancel.performClick();
        }
    }

    public interface ExitDialogListener {
        void onCommit();

        void onCancel();
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
