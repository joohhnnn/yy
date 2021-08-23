package com.txznet.launcher.widget.container;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by TXZ-METEORLUO on 2018/3/15.
 */

public class PageContainer extends ViewContainer {

    public PageContainer(Context context) {
        this(context, null);
    }

    public PageContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//
        testContainer();
    }

    private void testContainer() {
        View view = new View(getContext());
        view.setBackgroundColor(Color.GREEN);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
        view.setLayoutParams(params);
        setImageView(view);
        View content = new View(getContext());
        content.setBackgroundColor(Color.RED);
        setContentView(content);
    }

    @Override
    protected LayoutParams createImageLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        return params;
    }

    private ViewGroup mLayout;

    @Override
    protected ViewGroup createImageLayout() {
        if (mLayout == null) {
            mLayout = super.createImageLayout();
            mLayout.setId(View.generateViewId());
            mLayout.setBackgroundColor(Color.GREEN);
        }
        return mLayout;
    }

    @Override
    protected ViewGroup createContentLayout() {
        ViewGroup group = super.createContentLayout();
        group.setBackgroundColor(Color.RED);
        return group;
    }

    @Override
    protected LayoutParams createContentLayoutParams() {
        if (mLayout == null) {
            return null;
        }
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, mLayout.getId());
        return params;
    }

    @Override
    protected LayoutParams createStatusBarLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return params;
    }
}
