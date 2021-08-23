package com.txznet.webchat.ui.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.txznet.webchat.R;

/**
 * Created by J on 2018/1/23.
 */

public class ThemedImageView extends ImageView {
    private static int mSrcId;

    public ThemedImageView(final Context context) {
        super(context);
    }

    public ThemedImageView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        initTheme(attrs);
    }

    public ThemedImageView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTheme(attrs);
    }

    private void initTheme(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ThemedImageView);
        mSrcId = ta.getResourceId(R.styleable.ThemedTextView_theme_color, R.drawable.transparent_background);

        setImageResource(mSrcId);
    }
}
