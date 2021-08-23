package com.txznet.music.albumModule.ui.adapter;

import android.app.Activity;
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

public class ItemAlbumRecommandActAdapter extends ItemAlbumBaseAdapter {

    Activity baseActivity;

    public ItemAlbumRecommandActAdapter(Activity baseActivity, List<Album> albums) {
        super(baseActivity,albums);
        this.baseActivity = baseActivity;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, Album album) {
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(baseActivity, album.getLogo(), viewHolder.getImageView(), R.drawable.fm_item_default);
        viewHolder.setPlayingStatus(album.equals(getCurrentPlayingAlbum()), PlayEngineFactory.getEngine().isPlaying());
        viewHolder.setIntro(album.getName());
        viewHolder.updateNovelStatus(album.getSerialize());
    }

    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_album_recommand, parent, false);
        return new AlbumViewHolder(v);
    }
}
