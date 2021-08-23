package com.txznet.music.albumModule.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/21.
 */

public class CategoryAlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    BaseFragment fragment;
    List<Category> categories;

    public CategoryAlbumAdapter(BaseFragment fragment, List<Category> categories) {
        this.fragment = fragment;
        this.categories = categories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_album_recommand, parent, false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Category category = categories.get(position);
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(fragment, category.getLogo(), viewHolder.getImageView(), R.drawable.fm_item_default);
        viewHolder.setIntro(category.getDesc());
        viewHolder.getPlayingView().setVisibility(View.GONE);
        if (null != listener) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onItemClick(null, v, position, getItemId(position));
                    }
                }
            });
            viewHolder.getClickRange().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != listener) {
                        listener.onItemClick(null, v, position, getItemId(position));
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(categories)) {
            return categories.size();
        }
        return 0;
    }

    private AdapterView.OnItemClickListener listener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }
}
