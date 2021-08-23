package com.txznet.music.baseModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.request.ReqWST;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.TXZObserver;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.data.http.AlbumRepository;
import com.txznet.music.data.http.AudioRepository;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.playerModule.PlayListItem;
import com.txznet.music.playerModule.PlayListItemDataSource;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.util.TestUtil;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.tongting.IConstantCmd;
import com.txznet.sdk.tongting.IConstantData;
import com.txznet.sdk.tongting.TongTingAudio;

import org.json.JSONArray;

import java.util.List;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ManufacturerInvoker implements IConstantData, IConstantCmd, PlayerInfoUpdateListener, PlayListItemDataSource.PlayListItemChangedListener {

    private static final String TAG = "music:extra:";
    private String mPackageName;

    //##创建一个单例类##
    private volatile static ManufacturerInvoker singleton;

    private ManufacturerInvoker(String packageName) {
        setSDKPackageName(packageName);
        mDisposable = new CompositeDisposable();
        initData();
    }

    public void unRegisterListener() {
        PlayListItemDataSource.getInstance().removeOnPlayListItemChangedListener(this);
        PlayInfoManager.getInstance().removePlayerInfoUpdateListener(this);
    }

    public void registerListener() {
        setPlayListListener();
        setPlayStatusListener();
    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        if (audio == null) {
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_TITLE, audio.getName());
        jsonBuilder.put(KEY_ID, audio.getId());
        jsonBuilder.put(KEY_SID, audio.getSid());
        jsonBuilder.put(KEY_ARTISTS, CollectionUtils.toString(audio.getArrArtistName()));
        if (album != null) {
            jsonBuilder.put(KEY_ALBUMNAME, album.getName());
        } else {
            jsonBuilder.put(KEY_ALBUMNAME, audio.getAlbumName());
        }
        jsonBuilder.put(KEY_LOGO, audio.getLogo());
        jsonBuilder.put(KEY_SOURCE_FROM, audio.getSourceFrom());
        jsonBuilder.put(KEY_FLAG, Utils.getFavourFlag(album, audio));
        jsonBuilder.put(KEY_STATE, PlayEngineFactory.getEngine().getState());

        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONPLAYINFOUPDATED, jsonBuilder.toBytes(), null);
    }

    @Override
    public void onProgressUpdated(long position, long duration) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_PROGRESS, position);
        jsonBuilder.put(KEY_DURATION, duration);
        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONPROGRESSUPDATED, jsonBuilder.toBytes(), null);
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_AUDIO_MODE, mode);
        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONPLAYERMODEUPDATED, jsonBuilder.toBytes(), null);
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_STATE, status);

        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONPLAYERSTATUSUPDATED, jsonBuilder.toBytes(), null);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {


//                ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONBUFFERPROGRESSUPDATED, null, null);
    }

    @Override
    public void onFavourStatusUpdated(int favourState) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_FAVOUR, favourState);
        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONFAVOURSTATUSUPDATED, jsonBuilder.toBytes(), null);
    }

    private void setPlayStatusListener() {
        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this);
    }

    @Override
    public void onPlayListItemChanged(List<PlayListItem> playListItems) {
        TestUtil.printList(TAG + "playlist:", playListItems);

        //播放列表
        JSONBuilder jsonBuilder = new JSONBuilder();
        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(playListItems)) {
            for (PlayListItem audio : playListItems) {
                TongTingAudio tongTingAudio = new TongTingAudio(audio.getAudio().getId(), audio.getAudio().getSid(), audio.getAudio().getName(), audio.getAudio().getFlag());
                jsonArray.put(JsonHelper.toJson(tongTingAudio));
            }
        }

        jsonBuilder.put(KEY_DATA, jsonArray);
        ServiceManager.getInstance().sendInvoke(mPackageName, REC_CALLBACK_PERFIX + CALLBACK_ONPLAYLISTCHANGED, jsonBuilder.toBytes(), null);
    }

    @Override
    public void onPlayItemChanged(int pos) {
//                Logger.d(TAG, "onPlayItemChanged:" + pos);
//                //播放列表
//                JSONBuilder jsonBuilder = new JSONBuilder();
//                jsonBuilder.put(KEY_DATA, pos);
//                ServiceManager.getInstance().sendInvoke(mPackageName, REC_CMD_PERFIX + REC_CMD_PLAYLIST_INDEX, jsonBuilder.toBytes(), null);
    }

    private void setPlayListListener() {

        PlayListItemDataSource.getInstance().addOnPlayListItemChangedListener(this);
    }


    public static ManufacturerInvoker getInstance(String packageName) {
        if (singleton == null) {
            synchronized (ManufacturerInvoker.class) {
                if (singleton == null) {
                    singleton = new ManufacturerInvoker(packageName);
                }
            }
        }
        return singleton;
    }


    public void invoke(final String packageName, final String command, byte[] data) {
        final int sequence;
        if (data != null) {
            Logger.d(TAG, new String(data));
        }
        if (SEND_CMD_GETRECOMMENDALBUM.equals(command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);

            sequence = jsonBuilder.getVal(KEY_SEQUENCE, Integer.class, 0);

            ReqWST reqWST = new ReqWST();
            reqWST.setLimit(jsonBuilder.getVal(KEY_LIMIT, Integer.class, 10));
            reqWST.setType(jsonBuilder.getVal(KEY_TYPE, Integer.class, 0));
            reqWST.setUp(jsonBuilder.getVal(KEY_DIRECTION, Integer.class, 0));
            reqWST.setAlbumId(jsonBuilder.getVal(KEY_ALBUM_ID, Long.class, 0L));


            RxNet.request(Constant.GET_ALBUM_LIST, reqWST, ResponseSearchAlbum.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new TXZObserver<ResponseSearchAlbum>() {
                        JSONBuilder jsonBuilder = new JSONBuilder();

                        {
                            jsonBuilder.put(KEY_SEQUENCE, sequence);
                        }

                        @Override
                        public void onResponse(ResponseSearchAlbum data) {
                            if (CollectionUtils.isNotEmpty(data.getArrAlbum())) {
                                TestUtil.printList(TAG + "albums:", data.getArrAlbum());

                                List<Album> albums = CollectionUtils.expertNullItem(data.getArrAlbum());

                                JSONArray jsonArray = new JSONArray();

                                for (Album album : albums) {
                                    jsonArray.put(JsonHelper.toJson(album));
                                }
                                jsonBuilder.put(KEY_DATA, jsonArray);
                            } else {
                                // TODO: 2018/7/11    后台没有数据给到的情况
                            }
                            ServiceManager.getInstance().sendInvoke(packageName, REC_CMD_PERFIX + REC_CMD_GETRECOMMENDALBUM, jsonBuilder.toBytes(), null);
                        }

                        @Override
                        public boolean showOtherException(int code) {
                            jsonBuilder.put(KEY_ERRORCODE, code);
                            ServiceManager.getInstance().sendInvoke(packageName, REC_CMD_PERFIX + REC_CMD_GETRECOMMENDALBUM, jsonBuilder.toBytes(), null);
                            return true;
                        }
                    });

        } else if (SEND_CMD_PLAY_ALBUM.equals(command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            long id = jsonBuilder.getVal(KEY_ID, Long.class, 0L);//Long (null) --->long
            int sid = jsonBuilder.getVal(KEY_SID, Integer.class, 0);
            long categoryId = jsonBuilder.getVal(KEY_CATEGORYID, Long.class, 0L);

            final ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
            reqAlbumAudio.setId(id);
            reqAlbumAudio.setCategoryId(categoryId);
            reqAlbumAudio.setSid(sid);
            Disposable disposable = AlbumRepository.getInstance().getAlbumInfoFromNet(id, sid).flatMap(new Function<Album, ObservableSource<List<Audio>>>() {

                @Override
                public ObservableSource<List<Audio>> apply(Album resultAlbum) throws Exception {
                    realAlbum = resultAlbum;
                    return AudioRepository.getInstance().getAudios(reqAlbumAudio);
                }
            })
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<Audio>>() {
                        @Override
                        public void accept(List<Audio> o) throws Exception {
                            //播放
                            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.extra, o, realAlbum, 0, PlayInfoManager.DATA_ALBUM_MANUFACTORY);
                            PlayEngineFactory.getEngine().play(EnumState.Operation.extra);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            ToastUtils.showShortOnUI("请求专辑数据发生异常");
                        }
                    });
            mDisposable.add(disposable);
        } else if (SEND_CMD_PLAY_AUDIO.equals(command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            long id = jsonBuilder.getVal(KEY_ID, Long.class, 0L);
            int sid = jsonBuilder.getVal(KEY_SID, Integer.class, 0);

            Audio audio = new Audio();
            audio.setId(id);
            audio.setSid(sid);
            int i = PlayInfoManager.getInstance().playListIndexOf(audio);
            if (i >= 0) {
                PlayEngineFactory.getEngine().playAudio(EnumState.Operation.extra, PlayInfoManager.getInstance().getPlayListAudio(i));
            } else {
                Logger.d(TAG, sid + "_" + sid);
                Logger.d(TAG, PlayInfoManager.getInstance().getPlayList());
                ToastUtils.showShortOnUI("播放项不在播放列表内");
            }
        } else if (SEND_CMD_ADDLISTENER.equals(command)) {
            setSDKPackageName(packageName);

            unRegisterListener();
            registerListener();

        }
    }

    Album realAlbum;
    CompositeDisposable mDisposable = null;

    /**
     * 初始化数据
     */
    private void initData() {
        realAlbum = null;
        mDisposable.clear();
    }

    public void setSDKPackageName(String packageName) {
        mPackageName = packageName;
        SharedPreferencesUtils.setSDKListenerPackageName(packageName);
    }


}
