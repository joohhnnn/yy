package com.txznet.webchat.ui.car.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.txznet.webchat.R;

import java.util.ArrayList;
import java.util.List;

public class SoundWaveView extends LinearLayout {

    private boolean bStarted;


    public SoundWaveView(Context context) {
        this(context, null);
    }

    public SoundWaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SoundWaveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        uiHandler = new Handler(context.getMainLooper());
    }


    private Handler uiHandler;

    private void runOnUiThread(Runnable runnable, long delay) {
        if (uiHandler != null) {
            uiHandler.postDelayed(runnable, delay);
        }
    }

    private void removeUiTask(Runnable task) {
        if (uiHandler != null) {
            uiHandler.removeCallbacks(task);
        }
    }

    private int waveCount = 5;
    private Drawable itemDrawable = null;
    private int waveMinHeight, waveMaxHeight;
    private List<WaveItemView> waveItemViews;
    private int itemMargin = 10;
    private LayoutParams itemParams;

    public void init(Context context, AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.soundWaveView);
        try {
            itemDrawable = t.getDrawable(R.styleable.soundWaveView_itemDrawable);
            waveCount = t.getInt(R.styleable.soundWaveView_count, 5);
            waveMinHeight = t.getDimensionPixelSize(R.styleable.soundWaveView_minHeight, 50);
            waveMaxHeight = t.getDimensionPixelSize(R.styleable.soundWaveView_maxHeight, 40);
            itemMargin = t.getDimensionPixelSize(R.styleable.soundWaveView_itemMargin, 10);
        } finally {
            t.recycle();
        }

        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                SoundWaveView.this.getViewTreeObserver().removeOnPreDrawListener(this);

                waveItemViews = new ArrayList<WaveItemView>();
                setOrientation(LinearLayout.HORIZONTAL);
                int itemWidth = (getWidth() - waveCount * itemMargin) / waveCount;
                itemParams = new LayoutParams(itemWidth, waveMaxHeight);
                itemParams.setMargins(itemMargin / 2, 0, itemMargin / 2, 0);
                itemParams.gravity = Gravity.CENTER_VERTICAL;
                for (int i = 0; i < waveCount; i++) {
                    WaveItemView waveItemView = new WaveItemView(getContext(), waveMinHeight, waveMaxHeight, itemDrawable);
                    addView(waveItemView, itemParams);
                    waveItemViews.add(waveItemView);
                }

                return true;
            }
        });

    }


    public void start() {
        if (bStarted) {
            return;
        }

        bStarted = true;
        lastStartWave = -1;
        isWaving = true;
        removeUiTask(startWaveTask);
        runOnUiThread(startWaveTask, waveDelay);
    }

    private int lastStartWave = 0;
    private boolean isWaving = false;
    private int waveDelay = 120;
    Runnable startWaveTask = new Runnable() {
        @Override
        public void run() {
            synchronized (SoundWaveView.class) {
                if (isWaving && lastStartWave < waveCount - 1) {
                    lastStartWave++;
                    WaveItemView itemView = (WaveItemView) getChildAt(lastStartWave);
                    itemView.start();
                    invalidate();
                    runOnUiThread(startWaveTask, waveDelay);
                }
            }
        }
    };

    public void stop() {
        stop(false);
    }


    public void stop(final boolean reset) {
        if (!bStarted) {
            return;
        }

        bStarted = false;
        removeUiTask(startWaveTask);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < waveCount; i++) {
                    try {
                        WaveItemView itemView = (WaveItemView) getChildAt(i);
                        itemView.stop(reset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                invalidate();
            }
        }, 0);
    }
}
