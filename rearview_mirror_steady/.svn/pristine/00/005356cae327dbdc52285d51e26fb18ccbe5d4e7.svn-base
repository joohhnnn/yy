package com.txznet.webchat.ui.car.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.ui.car.adapter.HelpListAdapter;

/**
 * Created by J on 2016/10/31.
 */

public class CarHelpExpandableListView extends ExpandableListView implements IFocusView, IFocusOperationPresenter {
    public CarHelpExpandableListView(Context context) {
        super(context);
    }

    public CarHelpExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarHelpExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //导航按键相关代码
    @Override
    public boolean onNavOperation(int operation) {
        HelpListAdapter adapter = (HelpListAdapter) getExpandableListAdapter();
        boolean consume = adapter.performNavOperation(operation);
        boolean isDownScroll = FocusSupporter.NAV_BTN_DOWN == operation
                || FocusSupporter.NAV_BTN_NEXT == operation;

        if (isDownScroll) {
            // ExpandableListView滚动时, 目标position已在可视区域时可能导致滚动停在无法将指定position完全
            // 显示的位置, 所以将目标设置为实际目标position的下一个位置; 同理滚动仅保证bound position不会
            // 完全滚出可视区域, 但不会保证bound position元素完整显示, 所以将boundPosition向前推一位
            smoothScrollToPos(adapter.getShiftedCurrentFocusPosition() + 1,
                    adapter.getShiftedGroupFocusPosition() - 1);
        } else {
            smoothScrollToPos(adapter.getShiftedGroupFocusPosition(),
                    adapter.getShiftedGroupFocusPosition());
        }


        return consume;
    }

    private void smoothScrollToPos(final int position, final int boundPosition) {
        smoothScrollToPosition(position, boundPosition);

        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    setOnScrollListener(null);
                    setSelection(boundPosition);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        HelpListAdapter adapter = (HelpListAdapter) getExpandableListAdapter();
        adapter.onNavGainFocus(rawFocus, operation);
        int targetPos = adapter.getShiftedCurrentFocusPosition();
        smoothScrollToPos(targetPos, targetPos);
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        HelpListAdapter adapter = (HelpListAdapter) getExpandableListAdapter();
        adapter.onNavLoseFocus(newFocus, operation);
        // 失去焦点时不需要将列表滚动状态重置, 屏蔽此处逻辑
        //int targetPos = adapter.getShiftedCurrentFocusPosition();
        //smoothScrollToPos(targetPos, targetPos);
    }
}
