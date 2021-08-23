package com.txznet.music.albumModule.bean;

import android.view.View;

/**
 * Created by telen on 2018/5/15.
 * 不同类型的专辑,策略模式好吗？
 */

public abstract class ITypeAlbum {

    Album mAlbum;

    public void setAlbum(Album album) {
        this.mAlbum = album;
    }

    public String getAlbumName(){
        return mAlbum.getName();
    }

    public Album getAlbum(){
        return mAlbum;
    }

    public abstract View.OnClickListener getOnClickListener();
}
