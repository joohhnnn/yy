package com.txznet.comm.ui.theme.test.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.resholder.R;

/**
 * 说明：加载对话框
 *
 * @author xiaolin
 * create at 2020-08-31 14:57
 */
public class LoadingDialog extends IDialog {

    private View mView;
    private TextView tvMessage;

    public LoadingDialog(Context context) {
        super(context);
        init();
    }

    public LoadingDialog(Context context, ViewBase viewBase) {
        super(context, viewBase);
        init();
    }

    private void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_loading, (ViewGroup) null);
        tvMessage = mView.findViewById(R.id.tvMessage);

        setContentView(mView);
        setCanceledOnTouchOutside(false);
    }

    public void setMessage(String message){
        tvMessage.setText(message);
    }

}
