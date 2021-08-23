package com.txznet.music.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.music.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 删除歌曲对话框
 *
 * @author zackzhou
 * @date 2018/12/25,14:28
 */

public class DeleteAudioDialog extends Dialog {

    public interface OnClickCallback {
        /**
         * 点击确定键
         */
        void onConfirm();

        /**
         * 点击取消键
         */
        void onCancel();
    }

    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_sub_title)
    TextView tvSubTitle;
    @Bind(R.id.tv_confirm)
    TextView tvConfirm;
    @Bind(R.id.tv_cancel)
    TextView tvCancel;
    @Bind(R.id.fl_content)
    FrameLayout flContent;

    OnClickCallback callback;

    public DeleteAudioDialog(@NonNull Context context, OnClickCallback callback) {
        super(context, R.style.TXZ_Dialog_Style_Full);
        this.callback = callback;
    }

    public String mTitle;
    public String mSubTitle;

    public DeleteAudioDialog setTitle(String title) {
        mTitle = title;
        return this;
    }

    public DeleteAudioDialog setSubTitle(String subTitle) {
        mSubTitle = subTitle;
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_dialog_delete_audio);
        ButterKnife.bind(this);
        if (mTitle != null) {
            tvTitle.setText(mTitle);
        }
        if (mSubTitle != null) {
            tvSubTitle.setText(mSubTitle);
        }
        flContent.setOnClickListener(v -> {
            dismiss();
        });
    }


    @OnClick({R.id.tv_confirm, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_confirm:
                if (callback != null) {
                    callback.onConfirm();
                }
                dismiss();
                break;
            case R.id.tv_cancel:
                if (callback != null) {
                    callback.onCancel();
                }
                dismiss();
                break;
        }
    }
}
