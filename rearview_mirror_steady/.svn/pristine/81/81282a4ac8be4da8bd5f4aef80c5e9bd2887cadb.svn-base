package com.txznet.comm.ui.theme.test.dialog;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.resholder.R;

/**
 * 说明：在卡片上显示的对话框
 *
 * @author xiaolin
 * create at 2020-08-31 14:56
 */
public abstract class IDialog {

    private Context context;

    private View mView;
    private ViewGroup layoutDialogMask;
    private ViewGroup layoutDialogContentWrap;

    private View contentView;


    private OnDismissListener onDismissListener;
    private boolean canceledOnTouchOutside = true;
    private ViewBase viewBase;
    private boolean mIsShow = false;

    public IDialog(Context context) {
        this.context = context;
        init();
    }

    /**
     *
     * @param context
     * @param viewBase 如果这个参数不为空，对话框只在这个界面显示
     */
    public IDialog(Context context, ViewBase viewBase) {
        this(context);
        this.viewBase = viewBase;
    }

    private void init() {
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_base, (ViewGroup) null);
        layoutDialogMask = mView.findViewById(R.id.layoutDialogMask);
        layoutDialogContentWrap = mView.findViewById(R.id.layoutDialogContentWrap);

        /*
         * 点击对话框外部
         */
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canceledOnTouchOutside){
                    dismiss();
                }
            }
        });
        /*
         * 点击对话框内部（可见部分）
         */
        layoutDialogContentWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setContentView(View view) {
        this.contentView = view;
        this.layoutDialogContentWrap.addView(contentView);
    }

    public Context getContext() {
        return context;
    }

    public View getContentView() {
        return contentView;
    }

    public View getView() {
        return mView;
    }

    /**
     * 清除对话框遮罩层
     */
    public void clearMask(){
        layoutDialogMask.setBackground(null);
    }

    /**
     * 设置庶
     * @param resId
     */
    public void setMask(@DrawableRes int resId){
        layoutDialogMask.setBackgroundResource(resId);
    }

    /**
     * 设置遮罩层高度
     *
     * @param height
     */
    public void setMaskHeight(int height) {
        /*对话框高度*/
        ViewGroup.LayoutParams lp = layoutDialogMask.getLayoutParams();
        lp.height = height;
        layoutDialogMask.setLayoutParams(lp);
    }

    /**
     * 调用此方法显示对话框
     */
    public void show() {
        if(mIsShow){
            return;
        }
        WinLayout.getInstance().showDialog(this);
        mIsShow = true;
    }

    /**
     * 调用此方法关闭对话框
     */
    public void dismiss() {
        if(!mIsShow){
            return;
        }
        WinLayout.getInstance().dismissDialog(this);
        if (onDismissListener != null) {
            onDismissListener.onDismiss();
        }
        mIsShow = false;
    }

    /**
     * 点击对话框外部是否关闭对话框
     *
     * @param canceled
     */
    public void setCanceledOnTouchOutside(boolean canceled) {
        canceledOnTouchOutside = canceled;
    }

    /**
     * 关闭监听
     *
     * @param listener
     */
    public void setOnDismissListener(OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    public ViewBase getViewBase(){
        return viewBase;
    }

    /**
     * 关闭监听
     */
    public interface OnDismissListener {
        void onDismiss();
    }

    public boolean isShow(){
        return mIsShow;
    }

}
