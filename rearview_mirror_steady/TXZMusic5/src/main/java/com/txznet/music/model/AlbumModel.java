package com.txznet.music.model;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.action.ActionType.ACTION_ALBUM_EVENT_GET_FROM_CATEGORY;

/**
 * @author telen
 * @date 2019/1/14,14:37
 */
public class AlbumModel extends RxWorkflow {

    private TXZMusicApi mMusicApi = TXZMusicApiImpl.getDefault();

    public AlbumModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ACTION_ALBUM_EVENT_GET_FROM_CATEGORY:
                getDataFromCategory((Integer) action.data.get(Constant.AlbumConstant.KEY_ALBUM_PAGE_INDEX),
                        (Integer) action.data.get(Constant.AlbumConstant.KEY_ALBUM_SID),
                        (Long) action.data.get(Constant.AlbumConstant.KEY_ALBUM_CATEGORY_ID),
                        (Integer) action.data.get(Constant.AlbumConstant.KEY_ALBUM_PAGE_OFFSET));
                break;
            default:
                break;
        }
    }

    private void getDataFromCategory(int pageIndex, int sid, long categoryID, @Nullable Integer offset) {
        // TODO: 2019/1/14 是否要做网络缓存？
        Disposable disposable = mMusicApi.getAlbum(sid, categoryID, pageIndex, offset).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(txzRespAlbum -> {
                    //必定抛异常
                    postRxData(RxAction.type(ActionType.ACTION_ALBUM_EVENT_POST_ALBUM).bundle(Constant.AlbumConstant.KEY_DATA_RESPONSE, txzRespAlbum).build());
                }, throwable -> {
                    postRxError(RxAction.type(ActionType.ACTION_ALBUM_EVENT_POST_ALBUM).build(), throwable);
                });
    }


}
