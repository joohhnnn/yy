package com.txznet.launcher.widget.image;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.widget.IImage;

/**
 * Created by TXZ-METEORLUO on 2018/2/26.
 * 小个的小欧，就是导航时出现的小欧
 */

public class SQImageLittle extends FrameLayout implements IImage {
    private SQCharacter mImageIv;

    private int mSmallImageWidth;
    private int mSmallImageHeight;

    public SQImageLittle(@NonNull Context context) {
        this(context, null);
    }

    public SQImageLittle(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SQImageLittle(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSmallImageWidth = (int) getResources().getDimension(R.dimen.dimen_small_image_w);
        mSmallImageHeight = (int) getResources().getDimension(R.dimen.dimen_small_image_h);
        smallSize();
    }

    private void initSmallView() {
        removeAllViews();
        View.inflate(getContext(), R.layout.sq_image_widget_small_ly, this);
        mImageIv = (SQCharacter) findViewById(R.id.image_iv);
        mImageIv.littleMode();
    }

    @Override
    public void updateState(final int state) {
        LogUtil.logi("updateState: state="+state);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mImageIv.updateState(state);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    updateState(state);
                }
            });
        }
    }

    @Override
    public void toggleScreen(final boolean isFullScreen) {

    }

    private void smallSize() {
        initSmallView();
        setImageParams(mSmallImageWidth, mSmallImageHeight);
    }

    private void setImageParams(int w, int h) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mImageIv.getLayoutParams();
        if (params == null) {
            params = new LinearLayout.LayoutParams(w, h);
        } else {
            params.width = w;
            params.height = h;
        }
        LogUtil.logd("prepareImageSize w:" + w + ",h:" + h);
        mImageIv.setLayoutParams(params);
    }

    @Override
    public void showDescText(final String descText) {

    }
}