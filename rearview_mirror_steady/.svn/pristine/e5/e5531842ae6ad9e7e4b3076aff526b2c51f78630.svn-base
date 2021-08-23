package com.txznet.txz.util.focus_supporter.strategies;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.txznet.txz.util.focus_supporter.widgets.FocusView;

/**
 * Created by J on 2016/11/13.
 */

public class DialogProxy implements IContextProxy {
    private Dialog mDialog;
    private WindowManager mWinManager;
    FocusView mFocusIndicator;

    public DialogProxy(Dialog dialog) {
        this.mDialog = dialog;
    }

    @Override
    public void onAttach() {
        mWinManager = (WindowManager) mDialog.getContext().getSystemService(Context.WINDOW_SERVICE);
        mFocusIndicator = new FocusView(mDialog.getContext());

        // add focus view to window
        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
        layoutParam.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParam.height = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                /*
                * 为避免Focus叠加层与attach到的Dialog flag不一致导致显示出现问题(如Dialog设置了
                * FLAG_FULLSCREEN, 导致叠加层显示时系统状态栏重新显示), 需要额外附加下Dialog
                * 中已设置的flag
                * */
                | getFlagsFromContext();

        // 2017/11/20 修改: 焦点绘制层的type改为与attach的Dialog相同, 避免默认的SYSTEM_ALERT优先级导致
        // UI显示问题, 同时保证焦点绘制层不会被原Dialog覆盖
        layoutParam.type = mDialog.getWindow().getAttributes().type;
        //layoutParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWinManager.addView(mFocusIndicator, layoutParam);
    }

    private int getFlagsFromContext() {
        Window contextWindow = mDialog.getWindow();

        if (null != contextWindow) {
            // 移除可能导致叠加层不透明的flag, 避免焦点绘制层影响原界面显示
            return contextWindow.getAttributes().flags
                    & ~WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    & ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        }

        return 0;
    }

    @Override
    public void onDetach() {
        mWinManager.removeViewImmediate(mFocusIndicator);
        mWinManager = null;
        mDialog = null;
    }

    @Override
    public void updateFocusIndicator(int l, int t, int r, int b) {
        mFocusIndicator.setVisibility(View.VISIBLE);

        mFocusIndicator.setFocusPosition(l, t, r, b);
    }

    @Override
    public void showFocusIndicator() {
        mFocusIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFocusIndicator() {
        mFocusIndicator.setVisibility(View.GONE);
    }

    @Override
    public View getViewById(int id) {
        return mDialog.findViewById(id);
    }

    @Override
    public void performBack() {
        mDialog.onBackPressed();
    }

    @Override
    public void setIndicatorDrawable(Drawable drawable) {
        mFocusIndicator.setFocusDrawable(drawable);
    }
}
