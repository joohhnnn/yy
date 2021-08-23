package com.txznet.music.ui.helper;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.TXZObserver;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CarFmLogicHelper {


    //##创建一个单例类##
    private volatile static CarFmLogicHelper singleton;

    private CarFmLogicHelper() {
    }

    public static CarFmLogicHelper getInstance() {
        if (singleton == null) {
            synchronized (CarFmLogicHelper.class) {
                if (singleton == null) {
                    singleton = new CarFmLogicHelper();
                }
            }
        }
        return singleton;
    }


    /**
     * @param parentParent 车主FM的主专辑
     * @param needPlay
     */
    public void getCurrentCarFmLogic(EnumState.Operation operation, final Album parentParent, final boolean needPlay) {
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(1);
        reqSearchAlbum.setCategoryId(parentParent.getId());
        RxNet.request(Constant.GET_SEARCH_LIST, reqSearchAlbum, ResponseSearchAlbum.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new TXZObserver<ResponseSearchAlbum>() {
            @Override
            public void onResponse(ResponseSearchAlbum data) {
                if (data != null) {
                    if (data.getArrAlbum() != null) {
                        if (data.getArrAlbum().size() == 0) {
                            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                        } else {
                            List<Album> albums = new ArrayList<Album>();
                            if (CollectionUtils.isNotEmpty(data.getArrAlbum())) {
                                for (Album album : data.getArrAlbum()) {
                                    album.setParentAlbum(parentParent);
                                    albums.add(album);
                                }
                            }
                            PlayInfoManager.setCarFmAlbums(albums);
                            // 请求时段主题
                            AlbumEngine.getInstance().playAlbumFMWithBreakpoint(EnumState.Operation.manual, CarFmUtils.getInstance().getNeedPlayAlbum(albums), parentParent.getCategoryId(), needPlay);
                        }
                    } else {
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                    }
                }
            }

            @Override
            public boolean showOtherException(int code) {
                return super.showOtherException(code);
            }
        });
    }

    /**
     * 给每个ALbum添加自己所属的父类专辑
     *
     * @param albums
     */
    public void addAlbumParent(List<Album> albums, Album parentAlbum) {
        if (CollectionUtils.isNotEmpty(albums)) {
            for (Album album : albums) {
                album.setParentAlbum(parentAlbum);
            }
        }
        PlayInfoManager.setCarFmAlbums(albums);
    }


}
