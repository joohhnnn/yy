package com.txznet.music.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.txznet.music.R;

/**
 * 专辑播放状态显示
 *
 * @author zackzhou
 * @date 2019/7/29,15:04
 */

public class PlayingStateView extends android.support.v7.widget.AppCompatImageView {
    private boolean isPlaying;

    public PlayingStateView(Context context) {
        super(context);
        init();
    }

    public PlayingStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayingStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        onPause();
    }

    public void onPlay() {
        setImageResource(R.drawable.home_item_pause_icon);
        isPlaying = true;
    }

    public void onPause() {
        setImageResource(R.drawable.home_item_play_icon);
        isPlaying = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
