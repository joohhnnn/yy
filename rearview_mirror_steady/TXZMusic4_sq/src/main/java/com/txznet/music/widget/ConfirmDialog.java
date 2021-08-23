package com.txznet.music.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.txznet.music.R;

/**
 * Created by telenewbie on 2017/3/20.
 */

public class ConfirmDialog extends Dialog {

    private  Context mContext;

    public ConfirmDialog(Context context) {
        this(context, R.style.TXZ_Dialog_Style);
    }

    public ConfirmDialog(Context context, int themeResId) {
        super(context, themeResId);
        mContext=context;
        init();
    }

    protected ConfirmDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext=context;
        init();
    }


    /**
     * 初始化视图的宽高
     */
    public void init() {
        //设置宽高
        Window window = getWindow();
        window.setLayout(mContext.getResources().getDimensionPixelOffset(R.dimen.dim_home_dialog_back_width), mContext.getResources().getDimensionPixelOffset(R.dimen.dim_home_dialog_back_height));
        this.setContentView(R.layout.dialog_confirm);
    }

    public void setSureText(CharSequence text, View.OnClickListener clickListener) {
        TextView tvSure = (TextView) findViewById(R.id.dialog_sure);
        tvSure.setText(text);
        if (clickListener != null) {
            tvSure.setOnClickListener(clickListener);
        }
    }

    public void setCancleText(CharSequence text, View.OnClickListener clickListener) {
        TextView tvCancle = (TextView) findViewById(R.id.dialog_cancle);
        tvCancle.setText(text);
        if (clickListener != null) {
            tvCancle.setOnClickListener(clickListener);
        }
    }

    public void setContentText(CharSequence text, View.OnClickListener clickListener) {
        TextView tvContent = (TextView) findViewById(R.id.dialog_content);
        tvContent.setText(text);
        if (clickListener != null) {
            tvContent.setOnClickListener(clickListener);
        }
    }

}
