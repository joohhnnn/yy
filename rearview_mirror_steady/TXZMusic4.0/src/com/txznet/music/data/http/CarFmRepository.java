package com.txznet.music.data.http;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.data.http.req.ReqCarFmCurTops;
import com.txznet.music.data.http.resp.RespCarFmCurTops;
import com.txznet.music.net.rx.RxNet;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 车主FM相关请求
 */
public class CarFmRepository {


    //##创建一个单例类##
    private volatile static CarFmRepository singleton;

    private CarFmRepository() {
    }

    public static CarFmRepository getInstance() {
        if (singleton == null) {
            synchronized (CarFmRepository.class) {
                if (singleton == null) {
                    singleton = new CarFmRepository();
                }
            }
        }
        return singleton;
    }


    /**
     * @param albumId
     * @param click_time 单位ms
     * @return
     */
    public Observable<RespCarFmCurTops> getCurTimeTops(long albumId, long click_time) {
        ReqCarFmCurTops reqCarFmCurTops = new ReqCarFmCurTops(albumId, click_time);
        return RxNet.request(Constant.GET_CAR_FM_CUR, reqCarFmCurTops, RespCarFmCurTops.class);
    }

    /**
     * /fm/SuperFm?action=checkNextAlbum&album_id=1000757&click_time=1&send_time=29
     * album_id 可以填0
     * {
     * 'next_category_id': 200001,
     * 'next_album_id': 1000757,
     * 'next_album_name': 0,
     * 'remain_time': 3600
     * }
     */
    public Observable<RespCarFmCurTops> checkNextAlbumName(long albumId, long click_time) {
        ReqCarFmCurTops reqCarFmCurTops = new ReqCarFmCurTops(albumId, click_time);
        reqCarFmCurTops.setAction(ReqCarFmCurTops.CHECK_NEXT_ALBUM_NAME);
        return RxNet.request(Constant.GET_CAR_FM_CUR, reqCarFmCurTops, RespCarFmCurTops.class);
    }

    public Observable<List<Album>> getTypeFmObservable(long categoryId) {
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(1);
        reqSearchAlbum.setCategoryId(categoryId);
        return AlbumRepository.getInstance().getAlbums(categoryId, 1, true);
    }

}
