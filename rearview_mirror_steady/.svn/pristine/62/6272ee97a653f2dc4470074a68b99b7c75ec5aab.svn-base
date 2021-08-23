package com.txznet.music.ui.album;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.music.R;
import com.txznet.music.data.entity.Album;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.ui.base.BasePlayerAdapter;
import com.txznet.music.ui.base.IPlayerStateViewHolder;
import com.txznet.music.widget.PlayingStateView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author telen
 * @date 2019/1/14,11:11
 */
public class AlbumAdapter extends BasePlayerAdapter<Album, AlbumAdapter.MyHolder> {

    private Fragment fragment;

    public AlbumAdapter(Fragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }

    public AlbumAdapter(Context context, Album[] objects) {
        super(context, objects);
    }

    public AlbumAdapter(Context context, List<Album> objects) {
        super(context, objects);
    }

    @Override
    protected void changePlayObj(@NonNull MyHolder holder, int position) {
        holder.animationView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void changeUnPlayingStatus(@NonNull MyHolder holder, int position) {
        holder.animationView.setVisibility(View.GONE);
    }


    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(parent);
    }

    public class MyHolder extends BaseViewHolder<Album> implements IPlayerStateViewHolder {
        @Bind(R.id.iv_album_logo)
        ImageView ivAlbumLogo;
        @Bind(R.id.tv_album_title)
        TextView tvAlbumTitle;
        @Bind(R.id.tv_album_played)
        TextView tvAlbumPlayed;
        @Bind(R.id.tv_album_update)
        TextView tvAlbumUpdate;
        @Bind(R.id.animation_view)
        PlayingStateView animationView;

        public MyHolder(ViewGroup parent) {
            super(parent, R.layout.album_item_view);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(Album data) {
            super.setData(data);
            GlideHelper.loadWithCorners(fragment, data.logo, R.drawable.home_default_cover_icon_normal_corners, ivAlbumLogo);
            tvAlbumTitle.setText(data.name);
            tvAlbumPlayed.setText(String.format("播放量：%s", data.listenNumText));
            tvAlbumUpdate.setText(String.format("更新期数：%s", data.audiosNum));
        }

        @Override
        public PlayingStateView getPlayingStateView() {
            return animationView;
        }
    }
}
