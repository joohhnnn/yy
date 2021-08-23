package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-09-30 11:36
 */
public class FloatViewRelativeLayout extends RelativeLayout {

    private static final String TAG = "FloatViewRelativeLayout";

    // 默认logo大小
    private float defaultSize;
    // 界面大小，宽和高取小的
    private int mSize = 0;


    public FloatViewRelativeLayout(Context context) {
        super(context);
        init();
    }

    public FloatViewRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatViewRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        defaultSize = DimenUtils.dp2px(UIResLoader.getInstance().getModifyContext(), 80);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getMeasureSize(widthMeasureSpec);
        int height = getMeasureSize(heightMeasureSpec);
        mSize = Math.min(width, height);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));

        setMeasuredDimension(width, height);

//        int childCount = getChildCount();
//        for(int i=0; i<childCount; i++){
//            View view = getChildAt(i);
//            view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
//                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
//        }
        Log.d(TAG, String.format("-width:%d, -height:%d", width, height));
    }

    private int getMeasureSize(int measureSpec) {
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {// 确切的大小
            return MeasureSpec.getSize(measureSpec);
        } else if (mode == MeasureSpec.AT_MOST) {// 大小不超过某数值，如：wrap_content
            return (int) Math.min(defaultSize, size);
        } else if (mode == MeasureSpec.UNSPECIFIED) {// 不对View大小做限制，如：ListView，ScrollView
            return (int) defaultSize;
        }
        return (int) defaultSize;
    }

    public float getSize(){
        return mSize;
    }

}
