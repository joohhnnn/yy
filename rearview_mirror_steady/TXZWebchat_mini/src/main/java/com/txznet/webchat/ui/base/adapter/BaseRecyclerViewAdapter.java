package com.txznet.webchat.ui.base.adapter;

import android.support.v7.widget.RecyclerView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.helper.ListFocusHelper;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.helper.WxNavBtnHelper;

/**
 * 支持方控焦点处理的RecyclerView Adapter基类
 * Created by J on 2017/5/8.
 */

public abstract class BaseRecyclerViewAdapter<T extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<T>
        implements IFocusView, IFocusOperationPresenter {

    public static final int LAYOUT_ORIENTATION_VERTICAL = 0x01;
    public static final int LAYOUT_ORIENTATION_HORIZONTAL = 0x10;

    private ListFocusHelper mFocusHelper = new ListFocusHelper(1, 1, getLayoutOrientation()) {
        @Override
        protected int getItemCount() {
            return BaseRecyclerViewAdapter.this.getItemCount();
        }

        @Override
        protected void notifyDatasetChanged() {
            BaseRecyclerViewAdapter.this.notifyDataSetChanged();
        }

        @Override
        protected boolean onFocusClick(int position) {
            BaseRecyclerViewAdapter.this.onItemClick(position);
            return true;
        }

        @Override
        protected boolean onFocusLongClick(int position) {
            BaseRecyclerViewAdapter.this.onItemLongClick(position);
            return true;
        }

        @Override
        protected boolean onFocusReturn() {
            return BaseRecyclerViewAdapter.this.onReturn();
        }
    };

    public BaseRecyclerViewAdapter() {

    }

    /**
     * item点击处理
     * 方控的点击操作会调用此方法处理
     *
     * @param position item位置
     */
    protected abstract void onItemClick(int position);

    /**
     * item长按处理
     * 方控的长按操作会调用此方法处理
     *
     * @param position item位置
     */
    protected abstract void onItemLongClick(int position);

    /**
     * 方控的返回按钮处理
     *
     * @return 是否拦截此返回操作, 拦截后此方控返回按键事件不会再向上传递
     */
    protected boolean onReturn() {
        return false;
    }

    /**
     * 返回item的layout方向
     *
     * @return
     * @see BaseRecyclerViewAdapter#LAYOUT_ORIENTATION_HORIZONTAL
     * @see BaseRecyclerViewAdapter#LAYOUT_ORIENTATION_VERTICAL
     */
    protected int getLayoutOrientation() {
        return LAYOUT_ORIENTATION_VERTICAL;
    }

    /**
     * 获取当前方控焦点所处位置
     *
     * @return 方控焦点位置
     */
    public final int getCurrentFocusPosition() {
        return mFocusHelper.getCurrentFocusPosition();
    }

    /**
     * 判断指定位置的item是否获取了方控焦点
     * @param position 指定位置
     * @return 获取了方控焦点时返回true
     */
    protected final boolean isOnFocus(int position) {
        return mFocusHelper.isOnFocus(position);
    }

    /**
     * 设置方控焦点位置
     *
     * @param position 方控焦点位置
     * @return 是否设置成功
     */
    public boolean setCurrentFocusPosition(int position) {
        return mFocusHelper.setCurrentFocusPosition(position);
    }

    @Override
    public boolean onNavOperation(int operation) {
        boolean consume = mFocusHelper.onNavOperation(operation);

        // 双轴交互模式下滚轮操作永远消费，避免滚轮滚出当前list
        if ((FocusSupporter.NAV_BTN_PREV == operation || FocusSupporter.NAV_BTN_NEXT == operation)
                && FocusSupporter.NAV_MODE_TWO_WAY == WxNavBtnHelper.getInstance().getNavMode()) {
            consume = true;
        }

        return consume;
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        mFocusHelper.onNavGainFocus(rawFocus, operation);
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        mFocusHelper.onNavLoseFocus(newFocus, operation);
    }
}
