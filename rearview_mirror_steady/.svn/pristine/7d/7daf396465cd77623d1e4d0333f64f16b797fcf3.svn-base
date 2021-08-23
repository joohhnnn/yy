package com.txznet.comm.ui.theme.test.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.resholder.R;

/**
 * 说明：确认对话框
 *
 * @author xiaolin
 * create at 2020-08-31 14:57
 */
public class ConfirmDialog extends IDialog {

    private View mView;
    private TextView tvTitle;
    private TextView tvMessage;
    private Button btnDone;
    private Button btnCancel;

    public ConfirmDialog(Context context) {
        super(context);
        init();
    }

    public ConfirmDialog(Context context, ViewBase viewBase){
        super(context, viewBase);
        init();
    }

    private void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirm, (ViewGroup) null);
        tvTitle = mView.findViewById(R.id.tvTitle);
        tvMessage = mView.findViewById(R.id.tvMessage);
        btnDone = mView.findViewById(R.id.btnDone);
        btnCancel = mView.findViewById(R.id.btnCancel);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        setContentView(mView);
    }

    public void setTitle(String title){
        tvTitle.setText(title);
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }

    public void setDoneButton(String text, final View.OnClickListener listener){
        btnDone.setText(text);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(listener != null){
                    listener.onClick(v);
                }
            }
        });
    }

    public void setCancelButton(String text, final View.OnClickListener listener){
        btnCancel.setText(text);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if(listener != null){
                    listener.onClick(v);
                }
            }
        });
    }
}
