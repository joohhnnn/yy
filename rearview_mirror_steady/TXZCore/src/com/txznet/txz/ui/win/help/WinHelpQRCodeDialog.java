package com.txznet.txz.ui.win.help;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.txz.R;
import com.txznet.txz.util.QRUtil;

public class WinHelpQRCodeDialog extends Win2Dialog {
    private LinearLayout mRootLayout;

    public WinHelpQRCodeDialog(boolean fullScreen, Object... objects) {
        super(true,fullScreen,objects);
    }
    @Override
    protected View createView(Object... objects) {
        String title = (String) objects[0];
        String url = (String) objects[1];
        String desc = (String) objects[2];
        Boolean isFromFile = (Boolean) objects[3];
        mRootLayout = new LinearLayout(GlobalContext.get());
        mRootLayout.setOrientation(LinearLayout.VERTICAL);
        mRootLayout.setGravity(Gravity.CENTER);
        mRootLayout.setBackgroundColor(Color.parseColor("#E51A1D23"));
        mRootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = (int) LayouUtil.getDimen("y15");
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText(title);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m29"));
        tvTitle.setLayoutParams(titleParams);
        mRootLayout.addView(tvTitle);

        ImageView imageView = new ImageView(GlobalContext.get());
        int width = (int) LayouUtil.getDimen("m180");
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(width,width);
        imageView.setLayoutParams(ivParams);
        if (url.startsWith("qrcode:")) {
            try {
                imageView.setImageBitmap(QRUtil.createQRCodeBitmap(url.replace("qrcode:", ""), width));
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } else if (isFromFile) {
            ImageLoader.getInstance().displayImage("file://" + url, new ImageViewAware(imageView));
        } else {
            imageView.setImageDrawable(LayouUtil.getDrawable(url));
        }
        mRootLayout.addView(imageView);

        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("y18");
        descParams.leftMargin = (int) LayouUtil.getDimen("x30");
        descParams.rightMargin = (int) LayouUtil.getDimen("x30");
        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setGravity(Gravity.CENTER);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m22"));
        tvDesc.setLayoutParams(descParams);
        mRootLayout.addView(tvDesc);

        return mRootLayout;
    }

    @Override
    public void show() {
        super.show();
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        GlobalContext.get().registerReceiver(mHomeReceiver, intentFilter);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        GlobalContext.get().unregisterReceiver(mHomeReceiver);
    }

    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.logd("onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    dismiss();
                }
            }
        }
    };
}
