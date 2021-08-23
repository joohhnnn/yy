package com.txznet.music.albumModule.logic;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by telen on 2018/5/17.
 */

public class AlbumLogic {
    private static final String TAG = "music:AlbumLogic:";

    private static class MyInstance {
        public static AlbumLogic logic = new AlbumLogic();
    }

    private AlbumLogic() {
    }

    public static AlbumLogic getInstance() {
        return MyInstance.logic;
    }


    public void getCarFmLogic(final Album album, final boolean needPlay) {
        final Album albumById = DBManager.getInstance().findAlbumById(album.getPid(), album.getSid());
//                    AlbumEngine.getInstance().queryAlbum(album.getPid(), 1);
        if (albumById == null) {
            Logger.d(TAG, "getparentAlbum ,but not find");
        }
        //请求分时段主题
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                NetManager.getInstance().requestAlbum(album.getPid(), 1, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {
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
                                            album.setParentAlbum(albumById);
                                            albums.add(album);
                                        }
                                    }
                                    PlayInfoManager.setCarFmAlbums(albums);
                                    // 请求时段主题

                                    if (albums.indexOf(album) >= 0) {
                                        AlbumEngine.getInstance().playAlbumFMWithBreakpoint(EnumState.Operation.manual, albums.get(albums.indexOf(album)), album.getCategoryId(), needPlay);
                                    } else {
                                        TtsUtil.speakResource(Constant.RS_VOICE_SPEAK_CLICK_ALBUM_DIS_ENABLE_TIPS, Constant.RS_VOICE_SPEAK_CLICK_ALBUM_DIS_ENABLE_TIPS);
                                        Logger.d(TAG, "click not " + Constant.RS_VOICE_SPEAK_CLICK_ALBUM_DIS_ENABLE_TIPS);
                                    }
                                }
                                return;
                            }
                        }
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        LogUtil.e(TAG + " request album error:" + error.getErrorCode());
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NET_POOR);
                    }
                });
            }
        }, 0);
    }

}
