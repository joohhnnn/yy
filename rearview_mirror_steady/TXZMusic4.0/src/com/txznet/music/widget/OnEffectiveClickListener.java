package com.txznet.music.widget;

import android.os.SystemClock;
import android.view.View;

import com.txznet.music.baseModule.Constant;

/**
 * Created by brainBear on 2017/9/28.
 */

public abstract class OnEffectiveClickListener implements View.OnClickListener {
    private long mLastClickTime;

    @Override
    public void onClick(View v) {
        long nowTime = SystemClock.elapsedRealtime();
        if (nowTime - mLastClickTime > getTimeInterval()) {
            onEffectiveClick(v);
            mLastClickTime = nowTime;
        }
    }

    public abstract void onEffectiveClick(View v);

    /**
     * 返回两次点击的时间必须大于多少毫秒
     *
     * @return 时间间隔 单位ms
     */
    public long getTimeInterval() {
        return Constant.CLICK_TIME_INTERVAL;
    }
}
