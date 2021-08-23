package com.txznet.music.albumModule.ui.adapter;

import android.app.Fragment;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.AttrUtils;

import java.util.List;

public class ItemAlbumFragmentAdapter extends ItemAlbumBaseAdapter {
    Fragment mFragment = null;

    public ItemAlbumFragmentAdapter(Context ctx, Fragment fragment, List<Album> albums) {
        super(ctx, albums);
        this.mFragment = fragment;
    }


    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, Album album) {
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(mFragment, album.getLogo(), viewHolder.getImageView(), R.drawable.fm_item_default);
        viewHolder.setPlayingStatus(album.equals(getCurrentPlayingAlbum()), PlayEngineFactory.getEngine().isPlaying());
        viewHolder.setIntro(album.getName());
        viewHolder.updateNovelStatus(album.getSerialize());
    }

    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_album, parent, false);
        return new AlbumViewHolder(v);
    }
}
