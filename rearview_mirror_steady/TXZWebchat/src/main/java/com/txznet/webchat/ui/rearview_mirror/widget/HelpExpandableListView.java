package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.ui.rearview_mirror.HelpActivity;

/**
 * Created by J on 2016/10/31.
 */

public class HelpExpandableListView extends ExpandableListView implements IFocusView, IFocusOperationPresenter {
    public HelpExpandableListView(Context context) {
        super(context);
    }

    public HelpExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelpExpandableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //导航按键相关代码
    @Override
    public boolean onNavOperation(int operation) {
        HelpActivity.NavExpandableListAdapter adapter = (HelpActivity.NavExpandableListAdapter) getExpandableListAdapter();
        int focus;
        switch (operation) {
            case FocusSupporter.NAV_BTN_CLICK:
                adapter.performClick();
                return true;

            case FocusSupporter.NAV_BTN_BACK:
                return false;


            case FocusSupporter.NAV_BTN_PREV:
                adapter.performPrev();
                adapter.notifyDataSetChanged();
                invalidateViews();
                int targetPos = adapter.getShiftedGroupFocusPosition();
                smoothScrollToPos(targetPos, targetPos);
                return true;

            case FocusSupporter.NAV_BTN_NEXT:
                adapter.performNext();
                adapter.notifyDataSetChanged();
                smoothScrollToPos(adapter.getShiftedCurrentFocusPosition() + 1,
                        adapter.getShiftedGroupFocusPosition() - 1);
                return true;
        }

        return false;
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
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {

    }
}
