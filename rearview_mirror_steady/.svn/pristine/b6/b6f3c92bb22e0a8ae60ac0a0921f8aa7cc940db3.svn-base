package com.txznet.music.albumModule.ui;

import android.app.Activity;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.adapter.BaseSwipeAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.data.entity.Category;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.report.ReportEvent.TYPE_MANUAL;

/**
 * Created by brainBear on 2018/2/24.
 */

public class AlbumListPresenter implements AlbumListContract.Presenter, Observer {

    private AlbumListContract.View mView;
    private Category mCategory;
    private List<Album> mAlbums;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int mPageOff;

    public AlbumListPresenter(AlbumListContract.View view, Category category) {
        this.mView = view;
        this.mCategory = category;
    }

    @Override
    public void register() {
        ObserverManage.getObserver().addObserver(this);
    }

    @Override
    public void unregister() {
        ObserverManage.getObserver().deleteObserver(this);
        compositeDisposable.clear();
        mView = null;
    }

    @Override
    public void requestAlbum(boolean isLoadMore) {
        if (!isLoadMore) {
            mView.showLoading();
            mPageOff = 1;
        } else {
            mView.showLoadMore();
            mPageOff++;
        }
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(mPageOff);//从第一页开始
        reqSearchAlbum.setCategoryId(mCategory.getCategoryId());
        RxNet.request(Constant.GET_SEARCH_LIST, reqSearchAlbum, ResponseSearchAlbum.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new TXZObserver<ResponseSearchAlbum>() {


            @Override
            public void onSubscribe(Disposable d) {
                super.onSubscribe(d);
                compositeDisposable.add(d);
            }

            @Override
            public void onResponse(ResponseSearchAlbum responseAlbum) {
                mPageOff = responseAlbum.getPageId();
                if (mPageOff == 1) {// 如果是第一页
                    if (CollectionUtils.isNotEmpty(responseAlbum.getArrAlbum())) {
                        mAlbums = new LinkedList<Album>();
                        mAlbums.addAll(responseAlbum.getArrAlbum());
                        mView.showAlbums(responseAlbum.getArrAlbum(), false);
                    } else {
                        mView.showEmpty();
                    }
                } else {
                    mView.hideLoadMore();
                    if (mAlbums != null) {
                        mAlbums.addAll(responseAlbum.getArrAlbum());
                    }
                    mView.showAlbums(responseAlbum.getArrAlbum(), true);
                }
            }

            @Override
            public boolean showOtherException(int code) {
                mView.showError(Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                return true;
            }
        });
    }

    @Override
    public void showAlbum(Album album) {

        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null != currentAlbum
                && currentAlbum.getId() == album.getId()
                && currentAlbum.getSid() == album.getSid()) {
            return;
        }

        long categoryId;
        if (!CollectionUtils.isEmpty(album.getArrCategoryIds())) {
            categoryId = album.getArrCategoryIds().get(0);
        } else {
            categoryId = 0;
        }

        PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
        if (Utils.isSong(album.getSid())) {
            AlbumEngine.getInstance().playAlbum(PlayInfoManager.DATA_ALBUM, EnumState.Operation.manual, album, categoryId, false, null);
        } else {
            AlbumEngine.getInstance().playAlbumWithBreakpoint(PlayInfoManager.DATA_ALBUM, EnumState.Operation.manual, album, categoryId, false);
        }
    }

    @Override
    public void playAlbum(Album album) {
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null != currentAlbum
                && currentAlbum.getId() == album.getId()
                && currentAlbum.getSid() == album.getSid()) {

            if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {
                PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
            }
            return;
        }

        long categoryId;
        if (!CollectionUtils.isEmpty(album.getArrCategoryIds())) {
            categoryId = album.getArrCategoryIds().get(0);
        } else {
            categoryId = 0;
        }

        PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
        if (Utils.isSong(album.getSid())) {
            AlbumEngine.getInstance().playAlbum(PlayInfoManager.DATA_ALBUM, EnumState.Operation.manual, album, categoryId, null);
        } else {
            AlbumEngine.getInstance().playAlbumWithBreakpoint(PlayInfoManager.DATA_ALBUM, EnumState.Operation.manual, album, categoryId);
        }
    }

    @Override
    public void jumpToDetail(int screen, Album album, Category category, int position) {
        final long categoryID;
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
            categoryID = album.getArrCategoryIds().get(0);
        } else {
            categoryID =/*0;*/category.getCategoryId();
        }
        if (album.getAlbumType() == Album.ALBUM_TYPE_CAR_FM) {
            ReportEvent.reportClickCarFM(String.valueOf(TYPE_MANUAL));
            mView.jumpToSuperRadioView(album, categoryID);
        } else if (album.getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
            ReportEvent.clickRadioAlbumIconPlay(album, position, null);
            mView.jumpToTypeRadioView(album, categoryID);
        } else {
            ReportEvent.clickRadioAlbumIconPlay(album, position, null);
            mView.jumpToPlayerDetailView(screen, album, categoryID);
        }
    }

    @Override
    public BaseSwipeAdapter getAlbumAdapter(Category mCategory, Activity activity) {
        return null;
    }

//    @Override
//    public ItemAlbumBaseAdapter getAlbumAdapter(Category mCategory, Activity activity) {
//        ItemAlbumBaseAdapter mAdapter=null
//        if (mCategory.getCategoryId() != CATEGORY_NOVEL) {
//            switch (mCategory.getShowStyle()) {
//                case ItemAlbumBaseAdapter.SHOWTYPE_SINGER:
//                    mAdapter = new ItemAlbumSingerAdapter(getActivity(), mAlbums);
//                    break;
//                case ItemAlbumBaseAdapter.SHOWTYPE_RECOMMAND:
//                    mAdapter = new ItemAlbumRecommandAdapter(getActivity(), mAlbums);
//                    break;
//                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_LIST:
//                    mAdapter = new ItemAlbumRankListAdapter(getActivity(), mAlbums);
//                    break;
//                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_CLASSIFY:
//                    mAdapter = new ItemAlbumClassifyAdapter(getActivity(), mAlbums);
//                    break;
//                case ItemAlbumBaseAdapter.SHOWTYPE_UNDEFINE:
//                    mAdapter = new ItemAlbumFragmentAdapter(getActivity(), mAlbums);
//                    break;
//                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_OTHER:
//                    mAdapter = new ItemAlbumFragmentAdapter(getActivity(), mAlbums);
//                    break;
//            }
//            // 设置mAdapter
//            if (null == mAdapter) {
//                mAdapter = new ItemAlbumFragmentAdapter(getActivity(), mAlbums);
//            }
//            mAdapter.setOnItemClickListener(getAlbumItemIconClickListener());
//            mAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
//            mRvList.setAdapter(mAdapter);
//            mPresenter.requestAlbum(false);
//        } else {
//            mCategoryAlbumAdapter = new CategoryAlbumAdapter(this, mCategory.getArrChild());
//            mCategoryAlbumAdapter.setOnItemClickListener(getCategoryAlbumClick());
//            mRvList.setAdapter(mCategoryAlbumAdapter);
//            mCategoryAlbumAdapter.notifyDataSetChanged();
//        }
//
//
//        return null;
//    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            switch (info.getType()) {
                case InfoMessage.NET_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET:
                    mView.showError(Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_DATA:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_DATA:
//                    updateCurrentAlbum(PlayEngineFactory.getEngine().getCurrentAlbum());
                    break;
                case InfoMessage.NET_TIMEOUT_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT:
                    mView.showError(Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN:
                    mView.showError(Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.PLAY:
                case InfoMessage.PAUSE:
//                    if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
//                        playindex = albums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
//                        LogUtil.logd(getFragmentId() + "notify item index i=" + playindex);
//                        adapter.notifyItemChanged(playindex);
//                    }
                    mView.updateItemPlayStatus();
                    break;

                case InfoMessage.PLAYER_LIST:
                case InfoMessage.PLAYER_LOADING:
//                    if (playindex >= 0) {
//                        LogUtil.logd(getFragmentId() + "notify item index " + playindex);
//                        adapter.notifyItemChanged(playindex);
//                        playindex = -1;
//                    }
                    break;
                case InfoMessage.UPDATE_CAR_FM_CURRENT_TIME:
                    if (mAlbums != null && ((Album) info.getObj()).getParentAlbum() != null) {
                        int i = mAlbums.indexOf(((Album) info.getObj()).getParentAlbum());
                        if (i >= 0) {
                            mAlbums.get(i).setName(((Album) info.getObj()).getName());
                            mView.showAlbums(mAlbums, false);
                        }

                    }
                    break;
            }
        }
    }

}
