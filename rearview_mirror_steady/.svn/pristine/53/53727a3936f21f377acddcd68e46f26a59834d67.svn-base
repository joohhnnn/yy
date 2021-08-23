package com.txznet.txz.component.offlinepromote;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.txz.R;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.util.DeviceInfo;

public class OfflinePromoteFloatWindow extends AbsFloatWindow {
    private static final String TAG = "OfflinePromoteFloat";
    private LinearLayout mRootLayout;
    private int mWidth;

    public OfflinePromoteFloatWindow(Context context) {
        super(context);
        initView();
    }

    public void initView(){
        mRootLayout = new LinearLayout(GlobalContext.get());
        mRootLayout.setOrientation(HORIZONTAL);
        mRootLayout.setGravity(Gravity.CENTER);
        mRootLayout.setBackgroundResource(R.drawable.shape_offline_promote_float);
//        mRootLayout.setScaleX(getScale());
//        mRootLayout.setScaleY(getScale());
        mWidth = 137;
        int height = 80;
        LayoutParams params = new LayoutParams(mWidth, height);
        params.gravity = Gravity.CENTER_VERTICAL;
        mRootLayout.setLayoutParams(params);
        addView(mRootLayout);

        //关闭
        ImageView ivClose = new ImageView(GlobalContext.get());
        ivClose.setImageResource(R.drawable.offline_promote_close);
        int closeWidth = 18;
        params = new LayoutParams(closeWidth,closeWidth);
        params.leftMargin = 19;
        params.rightMargin = 24;
        ivClose.setLayoutParams(params);

        mRootLayout.addView(ivClose);
        //悬浮图标
        ImageView ivFloat = new ImageView(GlobalContext.get());
        ivFloat.setImageResource(R.drawable.offline_promote_hot);
        int floatWidth = 60;
        params = new LayoutParams(floatWidth,floatWidth);
        ivFloat.setLayoutParams(params);
        mRootLayout.addView(ivFloat);

    }

    @Override
    protected View dragView() {
        return mRootLayout;
    }

    @Override
    protected void onClick(float x) {
        float width = mWidth;
        float closeIntervalX = rootWidth - width + width * 0.445f;
        if (x < closeIntervalX) {
            OfflinePromoteManager.getInstance().manualCloseFloatWindow();
        } else {
            OfflinePromoteManager.getInstance().clickFloatWindow();
        }
    }

    public float getScale() {
        return Math.min((float) DeviceInfo.getScreenWidth() / 1024, (float) DeviceInfo.getScreenHeight() / 600);
    }
}
