package com.txznet.resholder.wave.rec;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.txznet.comm.ui.util.LayouUtil;

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

        waveMinHeight = (int) LayouUtil.getDimen("y5");
        waveMaxHeight = (int) LayouUtil.getDimen("y35");
        itemMargin = (int) LayouUtil.getDimen("x6");
        
        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                SoundWaveView.this.getViewTreeObserver().removeOnPreDrawListener(this);

                waveItemViews = new ArrayList<WaveItemView>();
                setOrientation(LinearLayout.HORIZONTAL);
                int itemWidth = (getWidth() - waveCount * itemMargin) / waveCount;
//                L.e("223333", "width = " + getWidth() + ", itemMargin = " + itemMargin + ", itemWidth = " + itemWidth);
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
        if(bStarted) {
            return;
        }

        bStarted = true;
        lastStartWave = -1;
        isWaving = true;
        removeUiTask(startWaveTask);
        runOnUiThread(startWaveTask, waveDelay);
    }
    
    public void startLoading() {
        if(bStarted) {
            return;
        }

        bStarted = true;
        lastStartWave = -1;
        isWaving = true;
        removeUiTask(startWaveLoadingTask);
        runOnUiThread(startWaveLoadingTask, waveDelay);
    }

    private int lastStartWave = 0;
    private boolean isWaving = false;
    private int waveDelay = 120;
    private int loadingDelay = 60;
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
    float[] sY = {0.5f,0.4f,0.3f,0.2f,0.2f,0.2f,0.2f,0.2f};
    Runnable startWaveLoadingTask = new Runnable() {
        @Override
        public void run() {
            synchronized (SoundWaveView.class) {
                if (isWaving) {
                    lastStartWave++;
                    if (lastStartWave > waveCount+2) {
						lastStartWave = 0;
					}
                    for (int i = 0; i < waveCount ; i++) {
                    	WaveItemView itemView = (WaveItemView) getChildAt(i);
                    	if (lastStartWave >= i) {
                    		itemView.setScaleY(sY[lastStartWave - i]);
						}else if (lastStartWave < i) {
							itemView.setScaleY(0.2f);
						}else {
							int tmp = lastStartWave - i;
							if (tmp == 1) {
								itemView.setScaleY(sY[1]);
							}else if (tmp == 2) {
								itemView.setScaleY(sY[2]);
							}else {
								itemView.setScaleY(0.2f);
							}
						}
					}
                    invalidate();
                    runOnUiThread(startWaveLoadingTask, loadingDelay);
                }
            }
        }
    };
    
    public void stopLoading() {
        if(!bStarted) {
            return;
        }

        bStarted = false;
        removeUiTask(startWaveLoadingTask);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                for (int i = 0; i < waveCount; i++) {
//                    try {
//                        WaveItemView itemView = (WaveItemView) getChildAt(i);
//                        
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
                invalidate();
            }
        }, 0);
    }
    

    public void stop() {
        stop(false);
    }


    public void stop(final boolean reset) {
        if(!bStarted) {
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

	public boolean isAniming() {
		return bStarted;
	}
}
