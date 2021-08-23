package com.txznet.music.albumModule.ui.adapter;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/14.
 */

public class ItemAlbumClassifyAdapter extends ItemAlbumBaseAdapter {
    Fragment mFragment = null;

    public ItemAlbumClassifyAdapter(Context ctx, Fragment fragment, List<Album> albums) {
        super(ctx, albums);
        this.mFragment = fragment;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, Album album) {
        ClassifyViewHolder viewHolder = (ClassifyViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(mFragment, album.getLogo(), viewHolder.ivType, R.drawable.fm_item_default_2);
        viewHolder.setPlayingStatus(album.equals(getCurrentPlayingAlbum()), PlayEngineFactory.getEngine().isPlaying());
//        viewHolder.tvIntro.setText(album.getName());
    }

    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_album_classify, parent, false);
        return new ClassifyViewHolder(v);
    }


}
