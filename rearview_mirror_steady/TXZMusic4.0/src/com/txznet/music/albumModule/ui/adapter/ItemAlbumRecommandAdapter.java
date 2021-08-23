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
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.Utils;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/14.
 */

public class ItemAlbumRecommandAdapter extends ItemAlbumBaseAdapter {

    Fragment mFragment = null;

    public ItemAlbumRecommandAdapter(Context ctx, Fragment fragment, List<Album> albums) {
        super(ctx, albums);
        this.mFragment = fragment;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, Album album) {
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(mFragment, album.getLogo(), viewHolder.getImageView(), R.drawable.fm_item_default);
        Album currentAlbum = null;
        String text = album.getName();
        if (getCurrentPlayingAlbum() != null) {
            if (getCurrentPlayingAlbum().getParentAlbum() != null) {
                currentAlbum = getCurrentPlayingAlbum().getParentAlbum();
            } else {
                currentAlbum = getCurrentPlayingAlbum();
            }
            if (album.equals(currentAlbum)) {
                text = getCurrentPlayingAlbum().getName();
            }
        }
        if (Utils.isCarFm(album)) {
            if (null != CarFmUtils.getInstance().getIsPlayingAlbum()) {
                text = GlobalContext.get().getResources().getString(R.string.str_car_fm);
            }
        }


        viewHolder.setPlayingStatus(album.equals(currentAlbum), PlayEngineFactory.getEngine().isPlaying());
        viewHolder.setIntro(text);
        viewHolder.updateNovelStatus(album.getSerialize());
    }

    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (ScreenUtils.isPhonePortrait()) {
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_album_recommand_portrait, parent, false);
        }else{
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_album_recommand, parent, false);
        }
        return new AlbumViewHolder(v);
    }
}
