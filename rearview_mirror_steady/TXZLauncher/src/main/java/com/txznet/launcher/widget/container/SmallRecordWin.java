package com.txznet.launcher.widget.container;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by TXZ-METEORLUO on 2018/3/16.
 */

public class SmallRecordWin extends ViewContainer {

    public SmallRecordWin(Context context) {
        super(context);
        testContainer();
    }

    private void testContainer() {
        View view = new View(getContext());
        view.setBackgroundColor(Color.GREEN);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(100, 100);
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.GREEN);
        setImageView(view);
        View content = new View(getContext());
        content.setBackgroundColor(Color.RED);
        params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
        content.setLayoutParams(params);
        content.setBackgroundColor(Color.RED);
        setContentView(content);
    }

    @Override
    protected LayoutParams createImageLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        return params;
    }

    @Override
    protected LayoutParams createContentLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 50);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        return params;
    }

    @Override
    protected LayoutParams createStatusBarLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return params;
    }
}
