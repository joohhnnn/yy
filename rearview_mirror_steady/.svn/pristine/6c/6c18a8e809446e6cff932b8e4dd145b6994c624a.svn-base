package com.txznet.music.ui.base.adapter;

import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.music.R;

import butterknife.Bind;

public abstract class UnifyCheckHolder<T> extends BaseCheckViewHolder<T> {

    @Bind(R.id.tv_index)
    public TextView tvIndex;
    @Bind(R.id.iv_playing)
    public ImageView ivPlaying;
    @Bind(R.id.tv_name)
    public TextView tvName;
    @Bind(R.id.tv_artist)
    public TextView tvArtist;
    @Bind(R.id.cb_favour)
    public CheckBox cbFavour;

    public UnifyCheckHolder(ViewGroup parent) {
        super(parent, R.layout.base_recycle_item_audio);
    }

    @Override
    public void setData(T data) {
        super.setData(data);
    }
}