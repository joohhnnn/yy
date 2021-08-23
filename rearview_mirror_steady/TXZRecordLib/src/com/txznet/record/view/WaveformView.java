package com.txznet.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ASUS User on 2015/9/14.
 */
public abstract class WaveformView extends View {

    public WaveformView(Context context) {
        super(context);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void updateAmplitude(float amplitude);

    public abstract float getAmplitude();
    
    public abstract void onStart();
    
    public abstract void onEnd();
    
    public abstract void onIdle();
}
