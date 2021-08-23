package com.txznet.music.data.http;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.rx.RxNet;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class AlbumRepository implements IAlbumDataSource {


    //##创建一个单例类##
    private volatile static AlbumRepository singleton;

    private AlbumRepository() {
    }

    public static AlbumRepository getInstance() {
        if (singleton == null) {
            synchronized (AlbumRepository.class) {
                if (singleton == null) {
                    singleton = new AlbumRepository();
                }
            }
        }
        return singleton;
    }

    @Override
    public Observable<List<Album>> getAlbums(long categoryId, int pageOff, boolean isCache) {
// NetManager.getInstance().requestAlbum(categoryId, pageOff, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {
        final ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(pageOff);
        reqSearchAlbum.setCategoryId(categoryId);
        return RxNet.request(Constant.GET_SEARCH_LIST, reqSearchAlbum, ResponseSearchAlbum.class).flatMap(new TXZFunction<ResponseSearchAlbum, ObservableSource<List<Album>>>() {
            @Override
            public ObservableSource<List<Album>> handleData(ResponseSearchAlbum responseSearchAlbum) {
//                if (!responseSearchAlbum.getArrAlbum().isEmpty()) {
                return Observable.fromIterable(responseSearchAlbum.getArrAlbum()).toList().toObservable();
//                }
            }
        });
    }

    public Observable<Album> getAlbumFromCache(final long albumId, final int sid) {
        return Observable.create(new ObservableOnSubscribe<Album>() {
            @Override
            public void subscribe(ObservableEmitter<Album> e) throws Exception {
                e.onNext(DBManager.getInstance().findAlbumById(albumId, sid));
                e.onComplete();
            }
        });
    }

    public Observable<Album> getAlbumInfoFromNet(long albumId, int sid) {
        ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
        reqAlbumAudio.setSid(sid);
        reqAlbumAudio.setId(albumId);
        return RxNet.request(Constant.GET_ALBUM_INFO, reqAlbumAudio, Album.class);
    }

}
