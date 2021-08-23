package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.txznet.comm.err.Error;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PageItemData;
import com.txznet.music.data.entity.PageItemDataGroup;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespAlbum;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author telen
 * @date 2019/1/14,15:33
 */
public class AlbumStore extends Store {

    private MutableLiveData<TXZRespAlbum> mRespAlbumMutableLiveData = new MutableLiveData<>();
    private SingleLiveEvent<Status> mStatusLiveEvent = new SingleLiveEvent<>();

    public enum Status {
        NO_NET,
        ERROR,
    }


    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_ALBUM_EVENT_POST_ALBUM
        };
    }

    @Override
    protected void onAction(RxAction action) {

    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_ALBUM_EVENT_POST_ALBUM:
                TXZRespAlbum txzRespAlbum = (TXZRespAlbum) action.data.get(Constant.AlbumConstant.KEY_DATA_RESPONSE);

                mRespAlbumMutableLiveData.setValue(txzRespAlbum);

                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_ALBUM_EVENT_POST_ALBUM:
                if (throwable instanceof Error) {
                    if (((Error) throwable).errorCode == ErrCode.ERROR_CLIENT_NET_OFFLINE) {
                        mStatusLiveEvent.setValue(Status.NO_NET);
                        return;
                    }
                }
                mStatusLiveEvent.setValue(Status.ERROR);

                break;
            default:
                break;
        }
    }

    public LiveData<TXZRespAlbum> getRespAlbum() {
        return mRespAlbumMutableLiveData;
    }

    private LiveData<PageItemDataGroup> mPageItemDataGroupLiveData = Transformations.map(mRespAlbumMutableLiveData, input -> {
        PageItemDataGroup group = new PageItemDataGroup();
        try {
            group.categoryId = Long.parseLong(input.categoryId);
        } catch (Exception e) {
        }
        group.pageId = input.pageId;
        group.pageNum = input.totalNum;

        List<PageItemData> pageItemDataList = new ArrayList<>();
        for (Album album : input.arrAlbum) {
            PageItemData pageItemData = new PageItemData();
            pageItemData.albumType = album.albumType;
            pageItemData.arrArtistName = album.arrArtistName;
            pageItemData.name = album.name;
            pageItemData.sid = album.sid;
            pageItemData.id = album.id;
            pageItemData.logo = album.logo;
            pageItemData.report = album.report;
            pageItemData.posId = album.posId;
            pageItemDataList.add(pageItemData);
        }
        group.arrAlbum = pageItemDataList;
        return group;
    });

    public LiveData<PageItemDataGroup> getPageItemDataGroup() {
        return mPageItemDataGroupLiveData;
    }

    public SingleLiveEvent<Status> getStatusLiveEvent() {
        return mStatusLiveEvent;
    }
}
