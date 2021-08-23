package com.txznet.comm.ui.dialog;

import android.view.View;

/**
 * 等待处理处理，用户按返回关闭，按确定执行某操作
 *
 * @author bihongpi
 */
public abstract class WinConfirm extends WinMessageBox {
    public WinConfirm() {
        super();
        setLeftButton("确定");
        setRightButton("取消");
        m_focus = new View[2];
        m_focus[0] = mViewHolder.mLeft;
        m_focus[1] = mViewHolder.mRight;
    }

    public WinConfirm(boolean isSystem) {
        super(isSystem);
        setLeftButton("确定");
        setRightButton("取消");
        m_focus = new View[2];
        m_focus[0] = mViewHolder.mLeft;
        m_focus[1] = mViewHolder.mRight;
    }

    public WinConfirm setTitle(String s) {
        mViewHolder.mTitle.setVisibility(View.VISIBLE);
        mViewHolder.mTitle.setText(s);
        return this;
    }

    public WinConfirm setMessage(String s) {
        super.setMessage(s);
        return this;
    }

    public WinConfirm setMessageData(Object data) {
        super.setMessageData(data);
        return this;
    }

    public WinConfirm setCancelText(String s) {
        super.setRightButton(s);
        return this;
    }

    public WinConfirm setSureText(String s) {
        super.setLeftButton(s);
        return this;
    }

    /**
     * 确定
     */
    public abstract void onClickOk();

    /**
     * 取消
     */
    public void onClickCancel() {

    }

    @Override
    public void onClickLeft() {
        onClickOk();
        dismiss();
    }

    @Override
    public void onClickRight() {
        onClickCancel();
        dismiss();
    }

    @Override
    public void onClickBlank() {
        onClickRight();
    }

    @Override
    public void onBackPressed() {
        onClickCancel();
        dismiss();
    }
}
