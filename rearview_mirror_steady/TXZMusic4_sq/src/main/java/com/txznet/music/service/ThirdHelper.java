package com.txznet.music.service;

import android.text.Html;
import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.data.DataErrorException;
import com.txznet.music.data.bean.AdapterAudio;
import com.txznet.music.data.bean.FunctionKaolaResp;
import com.txznet.music.data.kaola.KaoLaSDK;
import com.txznet.music.data.kaola.net.bean.KaolaAlbum;
import com.txznet.music.data.kaola.net.bean.KaolaAudio;
import com.txznet.music.data.kaola.net.bean.KaolaCategory;
import com.txznet.music.data.kaola.net.bean.KaolaHostBean;
import com.txznet.music.data.kaola.net.bean.RespItem;
import com.txznet.music.data.kaola.net.bean.RespParent;
import com.txznet.music.data.netease.NeteaseSDK;
import com.txznet.music.data.netease.net.bean.NeteaseAudio;
import com.txznet.music.data.netease.net.bean.RespRecommandSong;
import com.txznet.music.data.netease.net.bean.RespSearch;
import com.txznet.music.data.utils.OnGetData;
import com.txznet.music.localModule.logic.AlbumUtils;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.util.TestUtil;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsUtilWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by telenewbie on 2018/3/5.
 */

public class ThirdHelper {

    public static final String TAG = "music:line:test:";
    //##创建一个单例类##
    private volatile static ThirdHelper singleton;

    private ThirdHelper() {
    }

    public static ThirdHelper getInstance() {
        if (singleton == null) {
            synchronized (ThirdHelper.class) {
                if (singleton == null) {
                    singleton = new ThirdHelper();
                }
            }
        }
        return singleton;
    }

    private void resetData() {
        albums = null;
        audios = null;
    }

    /**
     * 搜索网易
     */
    public void searchNetease(String keywords, final OnGetData<List<Audio>> callback) {
        NeteaseSDK.getInstance().getSearch(keywords, new OnGetData<RespSearch>() {
            @Override
            public void success(RespSearch respSearch) {
                LogUtil.d("music:search", respSearch);

                List<String> artists = null;
                List<Audio> audios = new ArrayList<>();
                int errorCode = 0;
                if (respSearch.getCode() == 200) {
                    List<NeteaseAudio> mediaList = respSearch.getData().getMediaList();
                    if (CollectionUtils.isNotEmpty(mediaList)) {
                        for (NeteaseAudio neteaseAudio : mediaList) {
                            AdapterAudio adapterAudio = new AdapterAudio(neteaseAudio);
                            audios.add(adapterAudio.getAudio());
                        }
                    }
                } else {
                    errorCode = respSearch.getCode();
                }
                if (callback != null) {
                    if (errorCode == 0) {
                        callback.success(audios);
                    } else {
                        callback.failed(errorCode);
                    }
                }
            }

            @Override
            public void failed(int errorCode) {
                if (callback != null) {
                    callback.failed(errorCode);
                }
//                sendSearchAudioData(null, packageName, sessionId, 0);
            }
        });

    }

    public byte[] invokeMusic(final String packageName, String substring, byte[] data) {
        LogUtil.logd("music:" + "from:" + packageName + ",command:" + substring + ",value:" + new String(data));
        if ("search".equals(substring)) {
            resetData();
            JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
            final Long sessionId = jsonBuilder.getVal("sessionId", Long.class, 0L);
            int field = jsonBuilder.getVal("field", int.class, 0);// 1。表示歌曲，2.表示电台

            StringBuilder keyword = new StringBuilder();
            String[] keywords = jsonBuilder.getVal("keywords", String[].class);
            char split = ' ';
            if (ArrayUtils.isEmpty(keywords)) {
                String title = jsonBuilder.getVal("title", String.class);

                if (StringUtils.isNotEmpty(title)) {
                    keyword.append(title);
                }

                try {
                    String artist = StringUtils.toString(jsonBuilder.getVal("artist", String[].class), split);
                    if (StringUtils.isNotEmpty(artist)) {
                        if (keyword.length() > 0) {
                            keyword.append(split);
                        }
                        keyword.append(artist);
                    }
                } catch (Exception e) {
                    Logger.e("!@#", e.toString());
                }

            } else {
                keyword.append(StringUtils.toString(keywords, split));
            }


            if (field == 2) { // 电台
                KaoLaSDK.getInstance().search(keyword.toString(), new OnGetData<RespItem<KaolaAlbum>>() {
                    @Override
                    public void success(RespItem<KaolaAlbum> kaolaAlbumRespItem) {

                        LogUtil.d("music:search", kaolaAlbumRespItem);

                        List<String> artists;
                        List<Album> albums = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(kaolaAlbumRespItem.getDataList())) {
                            for (KaolaAlbum kaolaAlbum : kaolaAlbumRespItem.getDataList()) {
                                Album album = new Album();
                                album.setId(kaolaAlbum.getId());
                                album.setSid(AdapterAudio.TYPE_KAOLA);//kaola
                                album.setName(Html.fromHtml(kaolaAlbum.getName()).toString());
                                album.setLogo(kaolaAlbum.getImg());
                                if (CollectionUtils.isNotEmpty(kaolaAlbum.getHost())) {
                                    artists = new ArrayList<>();
                                    for (KaolaHostBean kaolaHostBean : kaolaAlbum.getHost()) {
                                        artists.add(Html.fromHtml(kaolaHostBean.getName()).toString());
                                    }
                                    album.setArrArtistName(artists);
                                }
                                albums.add(album);
                            }
                        }
                        TestUtil.printList("music:list:albums:", albums);
                        sendSearchAlbumData(albums, packageName, sessionId, 0);
                    }

                    @Override
                    public void failed(int errorCode) {
                        sendSearchAlbumData(null, packageName, sessionId, errorCode);
                        TtsUtil.speakTextOnRecordWin("没有找到相关结果", true, null);
                    }
                });
            } else if (field == 1) { // 音乐
                KaoLaSDK.getInstance().search(keyword.toString(), new OnGetData<RespItem<KaolaAlbum>>() {
                    @Override
                    public void success(RespItem<KaolaAlbum> kaolaAlbumRespItem) {

                        LogUtil.d("music:search", kaolaAlbumRespItem);

                        List<String> artists;
                        List<Album> albums = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(kaolaAlbumRespItem.getDataList())) {
                            for (KaolaAlbum kaolaAlbum : kaolaAlbumRespItem.getDataList()) {
                                Album album = new Album();
                                album.setId(kaolaAlbum.getId());
                                album.setSid(AdapterAudio.TYPE_KAOLA);//kaola
                                album.setName(Html.fromHtml(kaolaAlbum.getName()).toString());
                                album.setLogo(kaolaAlbum.getImg());
                                if (CollectionUtils.isNotEmpty(kaolaAlbum.getHost())) {
                                    artists = new ArrayList<>();
                                    for (KaolaHostBean kaolaHostBean : kaolaAlbum.getHost()) {
                                        artists.add(Html.fromHtml(kaolaHostBean.getName()).toString());
                                    }
                                    album.setArrArtistName(artists);
                                }
                                albums.add(album);
                            }
                            sendSearchAlbumData(albums, packageName, sessionId, 0);
                        } else {
                            TtsUtil.speakTextOnRecordWin("暂不支持搜索歌曲，敬请期待", true, null);
                        }
                        TestUtil.printList("music:list:albums:", albums);
                    }

                    @Override
                    public void failed(int errorCode) {
                        TtsUtil.speakTextOnRecordWin("暂不支持搜索歌曲，敬请期待", true, null);
//                        sendSearchAlbumData(null, packageName, sessionId, errorCode);
                    }
                });


//                searchNetease(keyword.toString(), new OnGetData<List<Audio>>() {
//                    @Override
//                    public void success(List<Audio> audios) {
//                        TestUtil.printList("music:list:audios:", audios);
//                        sendSearchAudioData(audios, packageName, sessionId, 0);
//                    }
//
//                    @Override
//                    public void failed(int errorCode) {
//                        sendSearchAudioData(null, packageName, sessionId, errorCode);
//                    }
//                });
//                TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_SUPPORT_NOT_SEARCH_FUNCTION, true, null);
            }
            return null;
        }
        //取消搜索
        if ("search.cancel".equals(substring)) {
            albums = null;
            audios = null;
            return null;
        }

        //选择第几个

        if ("choice".equals(substring)) {
            final int choiceIndex = Integer.parseInt(new String(data));
            if (CollectionUtils.isNotEmpty(albums)) {
                PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_OPEN);
                final Album album = albums.get(choiceIndex);
                KaoLaSDK.getInstance().getAudiosObservable(album.getId(), 0, 1).subscribeOn(Schedulers.io()).map(new FunctionKaolaResp<RespParent<RespItem<KaolaAudio>>, List<Audio>>() {
                                                                                                                     @Override
                                                                                                                     public List<Audio> onSuccess(RespParent<RespItem<KaolaAudio>> respParent) {
                                                                                                                         List<Audio> audios = new ArrayList<>();
                                                                                                                         //转换
                                                                                                                         for (KaolaAudio kaolaAudio : respParent.getResult().getDataList()) {
                                                                                                                             AdapterAudio adapterAudio = new AdapterAudio(kaolaAudio, album);
                                                                                                                             audios.add(adapterAudio.getAudio());
                                                                                                                         }
                                                                                                                         return audios;
                                                                                                                     }
                                                                                                                 }
                ).subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, album, 0, PlayInfoManager.DATA_SEARCH);
                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.loge("RS_VOICE_SPEAK_TIPS_UNKNOWN, cause=", throwable);
                        TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_TIPS_UNKNOWN", Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);


                        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
                    }
                });
            } else if (CollectionUtils.isNotEmpty(audios)) {
                playAudios(audios, choiceIndex);
//                PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, null, choiceIndex, PlayInfoManager.DATA_SEARCH);
//                //声控搜索为音频的则不支持上下拉刷新
//                PlayInfoManager.getInstance().setFirstEnd(true);
//                PlayInfoManager.getInstance().setLastEnd(true);
//                PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
            } else {
                TtsUtil.speakTextOnRecordWin("TTS_CLOSE", "搜索发生异常,请稍后重试", true, null);
            }
            resetData();

            return null;
        }

        return null;
    }

    public void playAudios(List<Audio> audios, int index) {
        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, null, index, PlayInfoManager.DATA_SEARCH);
        //声控搜索为音频的则不支持上下拉刷新
        PlayInfoManager.getInstance().setFirstEnd(true);
        PlayInfoManager.getInstance().setLastEnd(true);
        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
    }

    public List<Audio> audios;
    public List<Album> albums;


    public void sendSearchAlbumData(List<Album> albums, String packageName, long sessionId, int errorCode) {
        this.albums = albums;
//        ResponseWrapper<ResponseSearch> wrapper = new ResponseWrapper<>();
//        ResponseSearch search = new ResponseSearch();
//        if (errorCode == 0) {
//            search.setArrAlbum(albums);
//        } else {
//            search.setErrCode(errorCode);
//        }
//
//        wrapper.setData(search);
//        wrapper.setSessionId(sessionId);
        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(albums)) {
            for (Album album : albums) {
                jsonArray.put(getAlbumJsonObject(album));
            }
            JSONBuilder jsonBuilder = new JSONBuilder();
//            ServiceManager.getInstance().sendInvoke(packageName, "data.music.search", jsonArray.toString().getBytes(), null);
            sendListToCore(jsonBuilder, jsonArray);
        } else {
            TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_PARSE_ERROR, true, null);
        }
//        ServiceManager.getInstance().sendInvoke(packageName, "data.music.search", JsonHelper.toJson(wrapper).getBytes(), null);
    }

    private void sendListToCore(JSONBuilder jsonBuilder, JSONArray jsonArray) {
        // 如果支持v2版本就发v2的信息
        if (AppLogic.txzVersion >= 264) {
            jsonBuilder.put("list", jsonArray.toString());
            LogUtil.logd(TAG + " send:" + jsonBuilder.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist.v2", jsonBuilder.toBytes(), null);
        } else {
            LogUtil.logd(TAG + " send:" + jsonArray.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist", jsonArray.toString().getBytes(), null);
        }
    }

    public void sendSearchAudioData(List<Audio> audios, String packageName, long sessionId, int errorCode) {
        this.audios = audios;
//        ResponseWrapper<ResponseSearch> wrapper = new ResponseWrapper<>();
//        ResponseSearch search = new ResponseSearch();
//        if (errorCode == 0) {
//            search.setArrAudio(audios);
//        } else {
//            search.setErrCode(errorCode);
//        }
//
//        wrapper.setData(search);
//        wrapper.setSessionId(sessionId);

        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(audios)) {
            for (Audio audio : audios) {
                jsonArray.put(getAudioJsonObject(audio));
            }
            JSONBuilder jsonBuilder = new JSONBuilder();
//            ServiceManager.getInstance().sendInvoke(packageName, "data.music.search", jsonArray.toString().getBytes(), null);
            sendListToCore(jsonBuilder, jsonArray);
        } else {
            TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_PARSE_ERROR, true, null);
        }

    }

    private JSONObject getAudioJsonObject(Audio audio) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", audio.getName());
            jsonObject.put("name", StringUtils.toString(audio
                    .getArrArtistName()));
            jsonObject.put("id", audio.getId());
            jsonObject.put("report", audio.getReport());

            if (com.txznet.comm.util.CollectionUtils.isNotEmpty(audio.getWakeUp())) {
                JSONArray jsonArray = new JSONArray();
                for (String item : audio.getWakeUp()) {
                    jsonArray.put(item);
                }
                jsonObject.put("wakeUp", jsonArray);
            }
        } catch (JSONException e) {
        }
        return jsonObject;
    }


    private JSONObject getAlbumJsonObject(Album album) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", album.getName());
            if (!TextUtils.isEmpty(album.getTips())) {
                jsonObject.put("name", album.getTips());
            } else {
                jsonObject.put("name", StringUtils.toString(album
                        .getArrArtistName()));

            }
            jsonObject.put("id", album.getId());
            jsonObject.put("report", album.getReport());
            jsonObject.put("listened", album.getLastListen() > 0);
            jsonObject.put("novelStatus", album.getSerialize());
//            if ((album.isTalkShow() || album.getType() == Album.TYPE_TALKSHOW) &&
//                    album.getLastListen() > 0) {
//                jsonObject.put("showDetail", true);
//            }
            jsonObject.put("showDetail", AlbumUtils.isShowDetail(album));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public void playRecommandSong() {
        LogUtil.logd("player:play music, begin request");
        NeteaseSDK.getInstance().getRecommandSongs(50, false, new OnGetData<RespRecommandSong>() {
            @Override
            public void success(RespRecommandSong respRecommandSong) {
                try {
                    Audio[] audios = new Audio[respRecommandSong.getData().size()];
                    int randomOffset = (int) (Math.random() * audios.length);
                    for (int i = 0, len = respRecommandSong.getData().size(); i < len; i++) {
                        audios[(i + randomOffset) % (audios.length - 1)] = new AdapterAudio(respRecommandSong.getData().get(i)).getAudio();
                    }
                    playAudios(new ArrayList<>(Arrays.asList(audios)), 0);
                } catch (Exception e) {
                    LogUtil.loge("playRecommandSong, parse data error msg=" + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(int errorCode) {
//                ToastUtils.showShortOnUI("网易云接口：数据获取失败（errorCode=" + errorCode + ")");
                LogUtil.loge("player:play music, request failed");
                // 本地音乐没歌，弹toast提示“网络不好，请稍后再试”且语音播报，然后退出音乐应用
                if (!PlayHelper.playLocalMusic(EnumState.Operation.sound)) {
                    LogUtil.loge("player:play music, play local failed, player exit");
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NET_POOR);
                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                    PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
                }
            }
        });
    }

    Album album;


    /**
     * 播放第一个分类的第一个专辑
     */
    public void playRecommandRadio(final EnumState.Operation operation) {
        LogUtil.logd("player:play radio, begin search");
        Function<RespParent<List<KaolaCategory>>, ObservableSource<RespParent<List<KaolaCategory>>>> mapper = new FunctionKaolaResp<RespParent<List<KaolaCategory>>, ObservableSource<RespParent<List<KaolaCategory>>>>() {
            @Override
            public ObservableSource<RespParent<List<KaolaCategory>>> onSuccess(RespParent<List<KaolaCategory>> listRespParent) {
                return KaoLaSDK.getInstance().getSubCategoryObservable(listRespParent.getResult().get(1).getCid());//0是推荐，1 是头条，0取不到专辑
            }
        };
        Disposable subscribe = KaoLaSDK.getInstance().getCategoryObservable().subscribeOn(Schedulers.io()).flatMap(mapper).flatMap(new FunctionKaolaResp<RespParent<List<KaolaCategory>>, ObservableSource<RespParent<RespItem<KaolaAlbum>>>>() {
            @Override
            public ObservableSource<RespParent<RespItem<KaolaAlbum>>> onSuccess(RespParent<List<KaolaCategory>> listRespParent) {
                return KaoLaSDK.getInstance().getAlbumsObservable(listRespParent.getResult().get(0).getCid());
            }
        }).flatMap(new FunctionKaolaResp<RespParent<RespItem<KaolaAlbum>>, ObservableSource<RespParent<RespItem<KaolaAudio>>>>() {
            @Override
            public ObservableSource<RespParent<RespItem<KaolaAudio>>> onSuccess(RespParent<RespItem<KaolaAlbum>> respItemRespParent) {
                List<String> artists = new ArrayList<>();
                KaolaAlbum kaolaAlbum = respItemRespParent.getResult().getDataList().get(0);
                album = new Album();
                album.setId(kaolaAlbum.getId());
                album.setSid(AdapterAudio.TYPE_KAOLA);//kaola
                album.setName(Html.fromHtml(kaolaAlbum.getName()).toString());
                album.setLogo(kaolaAlbum.getImg());
                if (CollectionUtils.isNotEmpty(kaolaAlbum.getHost())) {
                    artists = new ArrayList<>();
                    for (KaolaHostBean kaolaHostBean : kaolaAlbum.getHost()) {
                        artists.add(Html.fromHtml(kaolaHostBean.getName()).toString());
                    }
                    album.setArrArtistName(artists);
                }
                return ThirdHelper.getInstance().getAudiosObservable(kaolaAlbum.getId(), 0, 1);
            }
        }).timeout(3, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<RespParent<RespItem<KaolaAudio>>>() {
                    @Override
                    public void accept(RespParent<RespItem<KaolaAudio>> respItemRespParent) throws Exception {
                        if (respItemRespParent != null) {
                            if (respItemRespParent.getResult() != null) {
                                List<KaolaAudio> dataList = respItemRespParent.getResult().getDataList();
                                if (com.txznet.comm.util.CollectionUtils.isNotEmpty(dataList)) {
                                    List<Audio> audios = new ArrayList<>();
                                    for (KaolaAudio kaolaAudio : dataList) {
                                        AdapterAudio adapterAudio = new AdapterAudio(kaolaAudio, album);
                                        audios.add(adapterAudio.getAudio());
                                    }
                                    PlayEngineFactory.getEngine().setAudios(operation, audios, album, 0, PlayInfoManager.DATA_NET);
                                    PlayEngineFactory.getEngine().playOrPause(operation);
                                }
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, throwable);

                        if (throwable instanceof DataErrorException) {
                            if ("40300".equals(throwable.getMessage())) {
                                KaoLaSDK.getInstance().clearOpenId();
                            }
                        }

//                        TtsUtilWrapper.speakResource("", "考拉接口：搜索异常");
//                        if (throwable instanceof DataErrorException) {
//                            ToastUtils.showShortOnUI(throwable.getMessage());
//                        }
                        LogUtil.loge("player:play radio failed, cause=" + throwable.getMessage() + ", player exit");
                        // 弹toast提示“网络不好，请稍后再试”且语音播报，然后退出音乐应用
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NET_POOR);
                        TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
                    }
                });

    }


    public Observable<RespParent<RespItem<KaolaAudio>>> getAudiosObservable(long albumId, long audioId, int page) {
        return KaoLaSDK.getInstance().getAudiosObservable(albumId, audioId, page);
    }


    public static void test() {

    }


}
