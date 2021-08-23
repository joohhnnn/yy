package com.txznet.music.SubscribeModule.ui.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.ui.adapter.AlbumViewHolder;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumBaseAdapter;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.message.Message;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by telenewbie on 2017/12/14.
 */

public class ItemAlbumSubscribeAdapter extends ItemAlbumBaseAdapter {
    Activity baseActivity;
    public boolean enterChoice;


    List<Album> toDeleteAlbums = new ArrayList<>();

    public ItemAlbumSubscribeAdapter(Activity activity, List<Album> albums) {
        super(activity, albums);
        this.baseActivity = activity;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, Album album) {
        AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(baseActivity, album.getLogo(), viewHolder.getImageView(), R.drawable.fm_item_default);
        if (isEnterChoiceMode()) {
            if (isChoiceAlbum(album)) {
                viewHolder.getPlayingView().setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.fm_muti_choice_yes));
            } else {
                viewHolder.getPlayingView().setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.fm_muti_choice_no));
            }
        } else {
            viewHolder.setPlayingStatus(album.equals(PlayInfoManager.getInstance().getCurrentAlbum()), PlayEngineFactory.getEngine().isPlaying());
        }
        viewHolder.setIntro(album.getName());
        viewHolder.updateNovelStatus(album.getSerialize());

        viewHolder.ivIconNew.setVisibility(isUnread(album) ? View.VISIBLE : View.GONE);

    }

    private boolean isUnread(Album album) {
        Message message = DBManager.getInstance().findMessage(album.getId(), album.getSid());
        if (message != null && message.getStatus() == Message.STATUS_UNREAD) {
            return true;
        }
        return false;
    }

    /**
     * 进入选择模式
     *
     * @param enter false 表示退出
     */
    public void enterChoiceMode(boolean enter) {
        enterChoice = enter;
        if (!enter) {//如果退出了模式
            toDeleteAlbums.clear();
        }
    }

    public boolean isEnterChoiceMode() {
        return enterChoice;
    }

    //选中了摸一个专辑
    public void setChoiceAlbum(Album choiceAlbum, boolean isChoice) {
        if (isChoice) {
            toDeleteAlbums.add(choiceAlbum);
        } else {
            toDeleteAlbums.remove(choiceAlbum);
        }
    }

    public boolean isChoiceAlbum(Album album) {
        if (toDeleteAlbums.contains(album)) {
            return true;
        }
        return false;
    }

    public void choiceAll(boolean isChoiceAll) {
        toDeleteAlbums.clear();
        if (isChoiceAll) {
            toDeleteAlbums.addAll(albums);
        }
    }

    public List<Album> getDeleteAlbums() {
        return toDeleteAlbums;
    }


    @Override
    public RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_recommand, parent, false);
        return new AlbumViewHolder(v);
    }
}
