package com.txznet.music.widget;

import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;

import com.txznet.music.baseModule.Constant;

/**
 * Created by brainBear on 2017/9/25.
 */

public abstract class OnItemEffectiveClickListener implements AdapterView.OnItemClickListener {

    private long mLastClickTime;

    public abstract void onItemEffectiveClick(AdapterView<?> parent, View view, int position, long id);

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - mLastClickTime > getTimeInterval()) {
            onItemEffectiveClick(parent, view, position, id);
            mLastClickTime = nowTime;
        }
    }

    /**
     * 返回两次点击的时间必须大于多少毫秒
     *
     * @return 时间间隔 单位ms
     */
    public long getTimeInterval() {
        return Constant.CLICK_TIME_INTERVAL;
    }
}
