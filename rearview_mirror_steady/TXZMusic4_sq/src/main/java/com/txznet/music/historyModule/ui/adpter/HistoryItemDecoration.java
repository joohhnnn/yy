package com.txznet.music.historyModule.ui.adpter;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.localModule.ui.adapter.LocalAudioAdapter;

/**
 * Created by Terry on 2017/9/28.
 */

public class HistoryItemDecoration extends RecyclerView.ItemDecoration {

    private int mItemSpace;

    public HistoryItemDecoration() {
        mItemSpace = (int) GlobalContext.get().getResources().getDimension(R.dimen.x34);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        int type = parent.getAdapter().getItemViewType(position);
        if (type == LocalAudioAdapter.ITEM_TYPE_NORMAL || type == LocalAudioAdapter.ITEM_TYPE_BLANK) {
            if (position % 2 == 0) {
                outRect.right = mItemSpace;
            } else if (position % 2 == 1) {
                outRect.left = mItemSpace;
            }
        }
    }
}
