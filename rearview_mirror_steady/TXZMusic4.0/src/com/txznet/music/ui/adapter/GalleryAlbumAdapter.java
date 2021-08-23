package com.txznet.music.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.ImageFactory;

import java.util.List;

/**
 * Created by Terry on 2017/5/12.
 */

public class GalleryAlbumAdapter extends BaseAdapter {

    private BaseFragment baseFragment;
    private List<Album> albums;
    private Album currentAlbum;// 当前播放的专辑，用于变色
    private boolean isShowLoading;

    public GalleryAlbumAdapter(BaseFragment baseFragment,List<Album> albums){
        super();
        this.baseFragment = baseFragment;
        this.albums = albums;
    }

    @Override
    public int getCount() {
        if (null == albums) {
            return 0;
        }
        return albums.size();
    }

    private int selectIndex = 0;

    public void setSelected(int position){
        this.selectIndex = position;
    }

    @Override
    public Object getItem(int position) {
        if (null == albums || albums.size() < position) {
            return null;
        }
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Album album = (Album) getItem(position);
        if(album==null){
            return null;
        }
        if(convertView==null){
            convertView = LayoutInflater.from(baseFragment.getActivity()).inflate(R.layout.item_gallery_album,null);
        }
        ImageView ivLogo = (ImageView) convertView.findViewById(R.id.iv_logo);
        TextView tvDesp = (TextView) convertView.findViewById(R.id.tv_desp);
        tvDesp.setText(album.getName());
        ImageFactory.getInstance().display(baseFragment,album.getLogo(), ivLogo, R.drawable.fm_item_default);
        Gallery.LayoutParams layoutParams = null;
//        float baseWidth = baseFragment.getActivity().getResources().getDimension(R.dimen.x129);
//        float baseHeight = baseFragment.getActivity().getResources().getDimension(R.dimen.y240);
        if (position == selectIndex) {
            convertView.setScaleX(1.2f);
            convertView.setScaleY(1.2f);
//            layoutParams = new Gallery.LayoutParams((int) (baseWidth*1.2),(int) (baseHeight*1.2));
        } else if (position == selectIndex - 1 || position == selectIndex + 1) {
            convertView.setScaleX(1.1f);
            convertView.setScaleY(1.1f);
//            layoutParams = new Gallery.LayoutParams((int) (baseWidth*1.1),(int) (baseHeight*1.1));
        }else {
            convertView.setScaleX(1.0f);
            convertView.setScaleY(1.0f);
//            layoutParams = new Gallery.LayoutParams((int) (baseWidth),(int) (baseHeight));
        }
//        convertView.setLayoutParams(layoutParams);
        return convertView;
    }
}
