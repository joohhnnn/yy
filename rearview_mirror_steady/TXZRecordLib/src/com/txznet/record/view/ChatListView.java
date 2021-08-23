package com.txznet.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ChatListView extends ListView {
    public ChatListView(Context context) {
        super(context);
    }

    public ChatListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        return false;
//    }
//
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return false;
//    }

    // @Override
    // public boolean dispatchTouchEvent(MotionEvent ev) {
    // switch (ev.getAction()) {
    // case MotionEvent.ACTION_MOVE:
    // return false;
    // }
    // return super.dispatchTouchEvent(ev);
    // }
}
