package com.txznet.music.albumModule.ui.adapter;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;

/**
 * Created by telenewbie on 2017/9/25.
 */

public abstract class AlbumBaseViewHolder extends RecyclerView.ViewHolder implements ItemAlbumFragmentAdapter.MyClickSetter {

    ImageView ivPlaying;

    public AlbumBaseViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener) {
        if (null != listener) {
            getClickRange().setOnClickListener(listener);
        }
    }
    @Override
    public void setOnIconClickListener(View.OnClickListener listener){
        if (null != listener) {
            getPlayingView().setOnClickListener(listener);
        }
    }

    /**
     * @param isPlayAlbum  当前专辑是否是所播放专辑
     * @param isPlayStatus 是否处于正在播放状态
     */
    public void setPlayingStatus(boolean isPlayAlbum, boolean isPlayStatus) {
        ivPlaying = getPlayingView();
//        if (ivPlaying.getVisibility() == View.VISIBLE && !isPlayAlbum) {
//            ivPlaying.setVisibility(View.GONE);
//        }
//        if (ivPlaying.getVisibility() == View.GONE && isPlayAlbum) {
//            ivPlaying.setVisibility(View.VISIBLE);
//        }
        if (isPlayAlbum && isPlayStatus) {
            ivPlaying.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.fm_album_item_pause));
        } else {
            ivPlaying.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.fm_album_item_play));
        }

    }

    public abstract ImageView getPlayingView();

    public abstract View getClickRange();


}
