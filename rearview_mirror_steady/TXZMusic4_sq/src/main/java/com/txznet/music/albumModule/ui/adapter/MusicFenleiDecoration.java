package com.txznet.music.albumModule.ui.adapter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Terry on 2017/5/6.
 */

public class MusicFenleiDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public MusicFenleiDecoration(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = 0;
        outRect.bottom = 0;

        int position = parent.getChildAdapterPosition(view);
        if (position % 2 == 0) {
            outRect.top = 0;
        } else {
            outRect.top = space/2;
        }
        outRect.right = space;
    }
}
