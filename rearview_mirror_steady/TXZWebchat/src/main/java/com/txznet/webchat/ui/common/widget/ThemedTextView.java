package com.txznet.webchat.ui.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.txznet.webchat.R;

/**
 * Created by J on 2018/1/22.
 */

public class ThemedTextView extends TextView {
    private static final int STAT_DEFAULT = 0;
    private static final int STAT_PRIMARY = 1;
    private static final int STAT_ACCENT = 2;
    private static final int STAT_PRIMARY_DARK = 3;

    private int mStatus;

    public ThemedTextView(Context context) {
        super(context);
    }

    public ThemedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTheme(attrs);
    }

    public ThemedTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTheme(attrs);
    }

    public void initTheme(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ThemedTextView);
        mStatus = ta.getInt(R.styleable.ThemedTextView_theme_color, STAT_DEFAULT);

        int colorResId = -1;
        switch (mStatus) {
            case STAT_PRIMARY:
                colorResId = R.color.color_text_primary;
                break;

            case STAT_ACCENT:
                colorResId = R.color.color_text_accent;
                break;

            case STAT_PRIMARY_DARK:
                colorResId = R.color.color_text_primary_dark;
                break;
        }

        if (colorResId > 0) {
            setTextColor(getContext().getResources().getColor(colorResId));
        }
    }
}
