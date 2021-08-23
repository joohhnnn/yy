package com.txznet.music.search;

import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.historyModule.ui.HistoryDataSource;
import com.txznet.music.localModule.logic.AlbumUtils;
import com.txznet.music.net.BaseReq;
import com.txznet.music.net.BaseResponse;
import com.txznet.music.net.NetManager;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.soundControlModule.bean.BaseAudio;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;
import com.txznet.music.soundControlModule.logic.net.request.ReqHistorySearch;
import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseHistorySearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseSearch;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Created by brainBear on 2017/6/20.
 */

public class SearchEngine {

    private static final String TAG = "music:search:";
    private static final int LENGTH = 20;// 上传给Core来播报的数据
    private static final int MAX_COUNT_ALBUM_AUDIOS = 20;
    private static SearchEngine sInstance;
    private BaseReq mReq = null;
    private Album mReqAlbum = null;
    private BaseResponse mResponse = null;
    private List<Audio> audios;
    private boolean isSearchingData = false;// 声控搜索中
    private boolean isLocal = false;
    private String rawText = "";
    private int mRequestId;

    private SearchEngine() {
    }

    public static SearchEngine getInstance() {
        if (null == sInstance) {
            synchronized (SearchEngine.class) {
                if (null == sInstance) {
                    sInstance = new SearchEngine();
                }
            }
        }
        return sInstance;
    }


    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    /**
     * 搜索收听历史，目前只支持电台历史
     *
     * @param searchData
     */
    public void doSearchHistory(String searchData) {
        isSearchingData = true;
        JSONBuilder jsonBuilder = new JSONBuilder(searchData);
        final ReqHistorySearch reqHistorySearch = new ReqHistorySearch();
        String type = jsonBuilder.getVal("type", String.class);
        if (!TextUtils.isEmpty(type)) {
            if ("novel".equals(type)) {
                reqHistorySearch.setType(1);
            }
        } else {
            // 现在type这个字段还没用
            reqHistorySearch.setType(0);
        }
        reqHistorySearch.setPage(jsonBuilder.getVal("page", Integer.class, 0));
        reqHistorySearch.setOffset(jsonBuilder.getVal("offset", Integer.class, 0));
        mReq = reqHistorySearch;
        // 没有网络连接则直接返回本地历史
        if (!Utils.isNetworkConnected(GlobalContext.get())) {

            HistoryDataSource.getInstance().requestAlbumHistory()
                    .subscribe(new Consumer<List<HistoryData>>() {
                        @Override
                        public void accept(List<HistoryData> historyData) throws Exception {
                            final List<Album> albumHistory = new ArrayList<>();
                            for (HistoryData data : historyData) {
                                albumHistory.add(data.getAlbum());
                            }
                            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                                    Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                                    false, new Runnable1<List<Album>>(albumHistory) {
                                        @Override
                                        public void run() {
                                            handleHistorySearch(albumHistory);
                                        }
                                    });
                        }
                    });
        } else {
            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                    Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                    false, new Runnable1<ReqHistorySearch>(reqHistorySearch) {
                        @Override
                        public void run() {
                            mRequestId = NetManager.getInstance().requestHistory(mP1);
                        }
                    });
        }

    }


    public void doSoundFind(int sourceId, String soundData) {
        mResponse = null;//清空上次的内容
        isSearchingData = true;
        MonitorUtil.monitorCumulant(Constant.M_SOUND_FIND);
        JSONArray array = new JSONArray();
        // 声控获取
        final ReqSearch reqData = new ReqSearch();
//		soundData.replaceAll(" ", "");
        LogUtil.logd(TAG + "sound.find:::" + soundData);

        JSONBuilder jsonBuilder = new JSONBuilder(soundData);
        reqData.setAudioName(jsonBuilder.getVal("title", String.class));
        reqData.setAlbumName(jsonBuilder.getVal("album", String.class));
        reqData.setArtist(StringUtils.toString(jsonBuilder.getVal("artist", String[].class)));
        reqData.setCategory(StringUtils.StringFilter(StringUtils.toString(jsonBuilder.getVal("keywords", String[].class))));
        reqData.setField(jsonBuilder.getVal("field", int.class, 0));// 1。表示歌曲，2.表示电台
        reqData.setSubCategory(jsonBuilder.getVal("subcategory", String.class));
        rawText = jsonBuilder.getVal("text", String.class);
        reqData.setText(jsonBuilder.getVal("text", String.class));
        reqData.getArrMeasure().clear();
        String[] audioIndices = jsonBuilder.getVal("audioIndex", String[].class, null);
        boolean reqAlbumAudioUrl = false;
//        if (!ArrayUtils.isEmpty(audioIndices)) {
//            reqAlbumAudioUrl = true;
//            for (int i = 0; i < audioIndices.length; i += 2) {
//                reqData.getArrMeasure().add(new ReqChapter(Integer.parseInt(audioIndices[i]), audioIndices[i + 1]));
//                if ("部".equals(audioIndices[i + 1])) {
//                    reqAlbumAudioUrl = false;
//                }
//            }
//        }


        if (reqAlbumAudioUrl) {
            if ((StringUtils.isEmpty(reqData.getAudioName()) && StringUtils.isEmpty(reqData.getAlbumName()))) {
                if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                    TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                            Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                            false, true, new Runnable() {

                                @Override
                                public void run() {
                                    doSelectAlbum(PlayInfoManager.getInstance().getCurrentAlbum(), reqData.getArrMeasure());
                                }
                            });
                } else {
                    speakTTSNOStart();
                }
                return;
            }
        }

        if (CollectionUtils.isNotEmpty(reqData.getArrMeasure())) {
            if (StringUtils.isEmpty(reqData.getAudioName()) && StringUtils.isEmpty(reqData.getAlbumName())) {
                if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                    reqData.setAlbumName(PlayInfoManager.getInstance().getCurrentAlbum().getName());
                } else {
                    speakTTSNOStart();
                }
                return;
            }
        }
        // 后台说:只认category
        if (StringUtils.isNotEmpty(reqData.getSubCategory())) {
            reqData.setCategory(reqData.getSubCategory());
        }

        if (StringUtils.isEmpty(reqData.getCategory())) {
            reqData.setCategory(jsonBuilder.getVal("category", String.class));
        }


        mReq = reqData;
        LogUtil.logd(TAG + "sound.find:::ReqSearch::" + reqData.toString());

        if (!Utils.isNetworkConnected(GlobalContext.get())) {
            audios = DBManager.getInstance().findLocalAudioForSearch(reqData);
            if (CollectionUtils.isNotEmpty(audios)) {
                isLocal = true;
                if (audios.size() == 1) {// 就只有一条数据
                    StringBuilder speakString = new StringBuilder();
                    if (CollectionUtils.isNotEmpty(audios.get(0).getArrArtistName())
                            && !"未知艺术家".equals(audios.get(0).getArrArtistName().get(0))) {
                        speakString.append(StringUtils.toString(audios.get(0).getArrArtistName()));
                        if (speakString.length() > 0) {
                            speakString.append("的");
                        }
                    }
                    String name = speakString.toString() + audios.get(0).getName();
                    TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
                            Constant.RS_VOICE_SPEAK_WILL_PLAY,
                            new String[]{Constant.PLACEHODLER, name}, true,
                            new Runnable() {
                                @Override
                                public void run() {
                                    choiceIndex(0, false);
//                                    SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
//                                    List<Audio> localAudios = DBManager.getInstance().findAllLocalAudios();
////                                    PlayEngineFactory.getEngine().release(EnumState.OperState.sound);
//                                    PlayEngineFactory.getEngine().setAudios(EnumState.OperState.sound, localAudios, null, localAudios.indexOf(audios.get(0)), PlayInfoManager.DATA_LOCAL);
//                                    PlayEngineFactory.getEngine().play(EnumState.OperState.sound);

                                }
                            });
                } else {
                    JSONArray array2 = new JSONArray();
                    for (int i = 0; i < audios.size(); i++) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("title", audios.get(i).getName());
                            jsonObject.put("name", StringUtils.toString(audios
                                    .get(i).getArrArtistName()));
                            jsonObject.put("id", audios.get(i).getId());
                            array2.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "txz.music.syncmusiclist",
                            array2.toString().getBytes(), null);
                }
            } else {
                speakVoice(sourceId, "RS_VOICE_SPEAK_NETNOTCON_TIPS",
                        Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS, null, false, true, null);
            }
        } else {
            isLocal = false;
            audios = null;
            speakVoice(sourceId, "RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                    Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                    false, new Runnable() {

                        @Override
                        public void run() {
                            mRequestId = NetManager.getInstance().requestSearch((ReqSearch) mReq);
                        }
                    });
        }
    }

    private void speakVoice(int sourceId, String resId, String sText, String[] resArgs, boolean close, boolean needAsr, final Runnable oRun) {
        Log.d(TAG, "error search audio null,from = " + sourceId);
        if (sourceId == -2) {//表明从广播进入
            TtsUtilWrapper.speakVoice(-1, resId, resArgs, sText, null, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    if (oRun != null) {
                        oRun.run();
                    } else {
                        Log.d(TAG, "error search audio null");
                    }
                }
            });
        } else {
            TtsUtilWrapper.speakTextOnRecordWin(resId, sText, resArgs, close, needAsr, oRun);
        }
    }

    public void cancelSearch() {
        NetManager.getInstance().cancelRequest(mRequestId);
    }


    public void handleError() {
        TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, true, null);
    }

    public void handleHistorySearch(List<Album> albums) {
        LogUtil.logd(TAG + "handleHistorySearch albums:" + (albums == null ? 0 : albums.size()));
        if (mReq == null || !(mReq instanceof ReqHistorySearch)) {
            LogUtil.logw(TAG + "request null or not history request!");
            return;
        }
        if (CollectionUtils.isEmpty(albums)) {
            String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS", tips, false, null);
            return;
        }
        ReqHistorySearch reqHistorySearch = (ReqHistorySearch) mReq;
        int type = reqHistorySearch.getType();
        JSONArray array = new JSONArray();
        int count = 0;
        List<Album> finalAlbums = new ArrayList<>();
        for (Album album : albums) {
            if (album != null) {
                // 查询小说播放历史
                if (type == 1) {
                    if (!AlbumUtils.isNovel(album)) {
                        continue;
                    }
                }
                // 更改为只要在历史列表里，就认为是上次收听
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("title", album.getName());
                    jsonObject.put("name", StringUtils.toString(album
                            .getArrArtistName()));
                    jsonObject.put("id", album.getId());
                    jsonObject.put("report", album.getReport());
                    jsonObject.put("listened", true);
                    jsonObject.put("novelStatus", album.getSerialize());
                    count++;
                    // 最多显示20条
                    if (count >= 20) {
                        break;
                    }

                    finalAlbums.add(album);
                    array.put(jsonObject);
                } catch (JSONException e) {
                    LogUtil.loge(TAG + "handleHistorySearch", e);
                }
            }
        }
        ((ResponseHistorySearch) mResponse).setArrAlbum(finalAlbums);
        if (count == 0) {
            String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS", tips, false, null);
        } else {
            LogUtil.logd(TAG + array.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist", array.toString().getBytes(), null);
        }
    }


    public void handleHistorySearch(final ResponseHistorySearch responseHistorySearch) {
        if (CollectionUtils.isNotEmpty(responseHistorySearch.getArrAlbum())) {
            handleHistorySearch(responseHistorySearch.getArrAlbum());
            mResponse = responseHistorySearch;
            return;
        }
//        LogUtil.logd(TAG + "net history response is null,start request local history!");
//        List<Audio> radioHistory = HistoryEngine.getInstance().getRadioHistorySync();
//        LogUtil.logd(TAG + "radioHistory:" + (radioHistory == null ? 0 : radioHistory.size()));
//        final List<Album> albumHistory = Utils.getAlbumByAudios(radioHistory);
//        LogUtil.logd(TAG + "albumHistory:" + (albumHistory == null ? 0 : albumHistory.size()));
//        responseHistorySearch.setArrAlbum(albumHistory);
//        mResponse = responseHistorySearch;
//        handleHistorySearch(albumHistory);


        HistoryDataSource.getInstance().requestAlbumHistory()
                .subscribe(new Consumer<List<HistoryData>>() {
                    @Override
                    public void accept(List<HistoryData> historyData) throws Exception {
                        List<Album> albumHistory = new ArrayList<>();
                        for (HistoryData data : historyData) {
                            albumHistory.add(data.getAlbum());
                        }
                        responseHistorySearch.setArrAlbum(albumHistory);
                        mResponse = responseHistorySearch;
                        handleHistorySearch(albumHistory);
                    }
                });
    }


    /**
     * 二级交互界面才会走到这里。
     * 跟后台协商的是第一个是上次收听的那个，第二个为最新的那个。能走到这里一定是这样的
     * y
     *
     * @param responseAlbumAudio
     */
    public void handleAlbumAudioSearch(ResponseAlbumAudio responseAlbumAudio) {
        if (responseAlbumAudio == null || CollectionUtils.isEmpty(responseAlbumAudio.getArrAudio())) {
            String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
            //TODO 播报未找到的具体东西
            MonitorUtil.monitorCumulant(Constant.M_EMPTY_SOUND);
            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
                    tips, false, null);
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        JSONArray array = new JSONArray();
        List<Audio> audios = responseAlbumAudio.getArrAudio();
        int length = audios.size() < MAX_COUNT_ALBUM_AUDIOS ? audios.size() : MAX_COUNT_ALBUM_AUDIOS;
        try {
            for (int i = 0; i < length; i++) {
                Audio audio = audios.get(i);
                if (null == audio) {
                    continue;
                }
                LogUtil.logd(TAG + " albumAudio:" + audio.toString());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", audio.getName());
                jsonObject.put("name", StringUtils.toString(audio
                        .getArrArtistName()));
                jsonObject.put("id", audio.getId());
                jsonObject.put("report", audio.getReport());
                if (i == 0) {
                    jsonObject.put("listened", true);
                }
                if (i == 1) {
                    jsonObject.put("latest", true);
                }
                if (CollectionUtils.isNotEmpty(audio.getWakeUp())) {
                    JSONArray jsonArray = new JSONArray();
                    for (String item : audio.getWakeUp()) {
                        jsonArray.put(item);
                    }
                    jsonObject.put("wakeUp", jsonArray);
                }
                array.put(jsonObject);
            }
        } catch (JSONException e) {
            LogUtil.loge(TAG + "handleAlbumAudioSearch", e);
        }
        jsonBuilder.put("continuePlay", true);
        sendListToCore(jsonBuilder, array);
        mResponse = responseAlbumAudio;
    }

    /**
     * 将专辑内上次收听的放在第一个
     *
     * @param audios
     * @return
     */
    private List<Audio> sortAudios(List<Audio> audios) {
        List<Audio> sortedAudios = new ArrayList<>();
        List<Audio> listenedAudios = DBManager.getInstance().findBreakpointByAlbumId(Long.parseLong(audios.get(0).getAlbumId()));
        if (CollectionUtils.isEmpty(listenedAudios)) {
            return audios;
        }
        Audio lastListenAudio = listenedAudios.get(0);
        sortedAudios.addAll(audios);
        for (int i = 0; i < audios.size(); i++) {
            if (audios.get(i) == null) {
                continue;
            }
            if (audios.get(i).getId() == lastListenAudio.getId()) {
                sortedAudios.remove(audios.get(i));
                sortedAudios.add(0, audios.get(i));
            }
        }
        return sortedAudios;
    }

    public void handleAudioSearchError(int errorCode) {
        TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, true, null);
    }

    private JSONObject getAudioJsonObject(Audio audio, ResponseSearch responseSearch) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", audio.getName());
            jsonObject.put("name", StringUtils.toString(audio
                    .getArrArtistName()));
            jsonObject.put("id", audio.getId());
            jsonObject.put("report", audio.getReport());
            if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
                jsonObject.put("delayTime",
                        responseSearch.getDelayTime());
            }
            if (CollectionUtils.isNotEmpty(audio.getWakeUp())) {
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

    private JSONObject getAlbumJsonObject(Album album, ResponseSearch responseSearch) {
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
            if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
                jsonObject.put("delayTime",
                        responseSearch.getDelayTime());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public void speakTTSNotFound() {
        LogUtil.logd(TAG + "speak not found");
        String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
        MonitorUtil.monitorCumulant(Constant.M_EMPTY_SOUND);
        TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
                tips, false, null);
    }

    public void handleSearch(final ResponseSearch responseSearch) {
        mResponse = responseSearch;
        JSONBuilder jsonBuilder = new JSONBuilder();
        JSONArray array = new JSONArray();
        if (responseSearch == null || (CollectionUtils.isEmpty(responseSearch.getArrAudio())
                && CollectionUtils.isEmpty(responseSearch.getArrAlbum())
                && CollectionUtils.isEmpty(responseSearch.getArrMix())
                && CollectionUtils.isEmpty(audios))) {
            speakTTSNotFound();
            return;
        }
        if (isLocal && CollectionUtils.isNotEmpty(audios)) {// 本地搜索出来的结果
            if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
                // 剔除相同歌名和歌手的数据
                List<Audio> arrAudio = responseSearch.getArrAudio();
                for (Audio audio : audios) {
                    for (int i = arrAudio.size() - 1; i >= 0; i--) {
                        if (audio.getId() == arrAudio.get(i).getId()) {
                            arrAudio.remove(i);
                        }
                    }
                }
                responseSearch.getArrAudio().addAll(0, audios);
            } else {
                responseSearch.setArrAudio(audios);
            }
        }


        // Core 只需要三个数据：title，name,id
        int length = LENGTH;
        int countAudio = 0;
        int countAlbum = 0;

        if (CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
            if (responseSearch.getArrMix().size() < (LENGTH - countAudio)) {
                length = responseSearch.getArrMix().size();
            }

            for (int i = 0; i < length; i++) {

                BaseAudio baseAudio = responseSearch.getArrMix().get(i);
                if (null == baseAudio) {
                    continue;
                }
                countAudio++;
                JSONObject jsonObject = new JSONObject();
                if (baseAudio.getAlbum() != null) {
                    Album album = baseAudio.getAlbum();
                    DBManager.getInstance().insertAlbumIfNotExist(album);
                    jsonObject = getAlbumJsonObject(album, responseSearch);
                } else if (baseAudio.getAudio() != null) {
                    Audio audio = baseAudio.getAudio();
                    jsonObject = getAudioJsonObject(audio, responseSearch);
                }
                array.put(jsonObject);
            }
        }
        if (CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
            length = 0;
            if (responseSearch.getArrAudio().size() < (LENGTH - countAudio)) {
                length = responseSearch.getArrAudio().size();
            }

            for (int i = 0; i < length; i++) {
                JSONObject jsonObject;
                Audio audio = responseSearch.getArrAudio().get(i);
                if (null == audio) {
                    continue;
                }
                countAudio++;
                jsonObject = getAudioJsonObject(audio, responseSearch);
                array.put(jsonObject);
            }
        }
        if (CollectionUtils.isNotEmpty(responseSearch.getArrAlbum())) {
            length = 0;
            if (responseSearch.getArrAlbum().size() < (LENGTH - countAudio)) {
                length = responseSearch.getArrAlbum().size();
            }
            for (int i = 0; i < length; i++) {
                countAlbum++;
                JSONObject jsonObject;
                Album album = responseSearch.getArrAlbum().get(i);
                if (album == null) {// 服务器有可能返回null对象
                    continue;
                }
                DBManager.getInstance().insertAlbumIfNotExist(album);
                jsonObject = getAlbumJsonObject(album, responseSearch);
                array.put(jsonObject);
            }
        }
        speakTTSOrNot();
        // 直接播放
        if (responseSearch.getPlayType() == ResponseSearch.GOPLAY) {
            isSearchingData = false;
            String name = Constant.RS_VOICE_SPEAK_PARSE_ERROR;
            try {
                if (responseSearch.getReturnType() == 1) {
                    Audio returnAudio = responseSearch.getArrAudio().get(
                            responseSearch.getPlayIndex());
                    if (CollectionUtils.isNotEmpty(returnAudio.getArrArtistName()) && !TextUtils.isEmpty(returnAudio.getArrArtistName().get(0))) {
                        name = returnAudio.getArrArtistName().get(0) + "的" + returnAudio.getReport();
                    } else {
                        name = returnAudio.getReport();
                    }
                } else if (responseSearch.getReturnType() == 2) {
                    name = responseSearch.getArrAlbum()
                            .get(responseSearch.getPlayIndex()).getReport();
                } else if (responseSearch.getReturnType() == 3) {
                    if (responseSearch.getArrMix()
                            .get(responseSearch.getPlayIndex()).getType() == 1) {

                        Audio returnAudio = responseSearch.getArrMix()
                                .get(responseSearch.getPlayIndex())
                                .getAudio();
                        if (CollectionUtils.isNotEmpty(returnAudio.getArrArtistName()) && !TextUtils.isEmpty(returnAudio.getArrArtistName().get(0))) {
                            name = returnAudio.getArrArtistName().get(0) + "的" + returnAudio.getReport();
                        } else {
                            name = returnAudio.getReport();
                        }
                    } else {
                        name = responseSearch.getArrMix()
                                .get(responseSearch.getPlayIndex())
                                .getAlbum().getReport();
                    }
                } else {
                    TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
                    return;
                }
            } catch (Exception e) {
                LogUtil.loge(TAG + ":parse error:" + e.toString());
                TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_PARSE_ERROR", name, true, null);
                return;
            }
            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_WILL_PLAY",
                    Constant.RS_VOICE_SPEAK_WILL_PLAY, new String[]{Constant.PLACEHODLER, name}, true,
                    new Runnable() {
                        @Override
                        public void run() {
                            //只有在自动播放的时候才上报
                            choiceIndex(responseSearch.getPlayIndex(), false);
                        }
                    });
        } else if (responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
            // 延时播放
            sendListToCore(jsonBuilder, array);
        } else if (responseSearch.getPlayType() == ResponseSearch.SELECTPLAY) {
            // 选择播放
            sendListToCore(jsonBuilder, array);
        } else {
            speakTTSNotFound();
        }

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


    public void speakTTSOrNot() {
        if (mResponse instanceof ResponseSearch) {
            ResponseSearch responseSearch = (ResponseSearch) mResponse;
            if (responseSearch.getErrMeasure() != 0 && CollectionUtils.isNotEmpty(responseSearch.getArrMeasure())) {
                TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_SPEAK_NOT_FOUND_ACCURATE", Constant.RS_VOICE_SPEAK_NOT_FOUND_ACCURATE, false, null);
            }
        }
    }

    public void speakTTSNOStart() {
//        if (mResponseSearch.getErrMeasure() != 0 && CollectionUtils.isNotEmpty(mResponseSearch.getArrMeasure())) {
        LogUtil.logd(TAG + "album:" + PlayInfoManager.getInstance().getCurrentAlbum() + ",audio:" + PlayInfoManager.getInstance().getCurrentAudio());

        TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_MUSIC_NO_CONTEXT", Constant.RS_VOICE_MUSIC_NO_CONTEXT, false, null);
//        }
    }

    public void choiceIndex(int index, boolean viewDetail) {
        LogUtil.logd(TAG + " choiceIndex :" + index + ",viewDetail:" + viewDetail);
        LogUtil.logd(TAG + " mResponse:" + mResponse);

        ResponseSearch responseSearch = null;
        ResponseHistorySearch responseHistorySearch = null;
        ResponseAlbumAudio responseAlbumAudio = null;
        if (mResponse instanceof ResponseSearch) {
            responseSearch = (ResponseSearch) mResponse;
        }
        if (mResponse instanceof ResponseHistorySearch) {
            responseHistorySearch = (ResponseHistorySearch) mResponse;
        }
        if (mResponse instanceof ResponseAlbumAudio) {
            responseAlbumAudio = (ResponseAlbumAudio) mResponse;
        }
        if (viewDetail) { // 如需查看详情则查询该专辑的整个内容并返回
            do {
                if (isLocal) {
                    break;
                }
                if (null == mResponse) {
                    break;
                }
                if (mResponse instanceof ResponseAlbumAudio) {
                    break;
                }
                Album album = null;
                if (responseSearch != null) {
                    if (responseSearch.getReturnType() == 1) { // 不是专辑，直接播放
                        break;
                    }

                    if (responseSearch.getReturnType() == 2) {
                        if (responseSearch.getArrAlbum() != null && responseSearch.getArrAlbum().size() > index) {
                            album = responseSearch.getArrAlbum().get(index);
                        }
                    }
                    if (responseSearch.getReturnType() == 3) {
                        BaseAudio baseAudio = responseSearch.getArrMix().get(index);
                        if (baseAudio.getType() != BaseAudio.ALBUM_TYPE) { // 不是专辑，直接播放
                            break;
                        }
                        album = baseAudio.getAlbum();
                    }
                }
                if (responseHistorySearch != null) {
                    LogUtil.logd(TAG + "responseHistorySearch.getArrAlbum().size():" + responseHistorySearch.getArrAlbum().size());
                    if (responseHistorySearch.getArrAlbum() != null && responseHistorySearch.getArrAlbum().size() > index) {
                        album = responseHistorySearch.getArrAlbum().get(index);
                    }
                }
                if (album != null) {
                    ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
                    reqAlbumAudio.setSid(album.getSid());
                    reqAlbumAudio.setId(album.getId());
                    reqAlbumAudio.setUp(0);
                    reqAlbumAudio.setCategoryId(album.getCategoryId());
                    reqAlbumAudio.setOffset(Constant.PAGECOUNT);
                    reqAlbumAudio.setType(1);
                    mReq = reqAlbumAudio;
                    mReqAlbum = album;
                    NetManager.getInstance().searchAudio(reqAlbumAudio);
                    return;
                }
            } while (false);
        }

        try {
            isSearchingData = false;
            // PlayEngineFactory.getEngine().setCurrentAlbumName("");
            PlayEngineFactory.getEngine().release(EnumState.Operation.sound);// 暂停上一首歌曲的数据
            if (isLocal) {
                // 如果是专辑的话
                List<Audio> audios = this.audios.subList(index, index + 1);
                LogUtil.logd(TAG + "playmusiclist.index::localMusic::" + audios.toString());
                if (CollectionUtils.isEmpty(audios)) {
                    return;
                }
                List<Audio> localAudios = DBManager.getInstance().findAllLocalAudios();
                PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, localAudios, null, localAudios.indexOf(audios.get(0)), PlayInfoManager.DATA_LOCAL);
                PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.sound);
                return;
            }
            // 默认搜素
            if (null != responseSearch) {
                SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
                if (responseSearch.getReturnType() == 2) {// 专辑
                    Album album = responseSearch.getArrAlbum().get(index);
                    doSelectAlbum(album, responseSearch.getArrMeasure());
                } else if (responseSearch.getReturnType() == 1) {
                    doSelectAudio(responseSearch.getArrAudio().get(index),
                            responseSearch.getArrAudio(), null, index);
                } else
                    // 有可能有音频或专辑
                    if (responseSearch.getReturnType() == 3) {
                        BaseAudio baseAudio = responseSearch.getArrMix().get(index);
                        // Audio
                        if (baseAudio.getType() == BaseAudio.MUSIC_TYPE) {
                            List<Audio> searchAudios = new ArrayList<>();
                            for (int i = 0; i < responseSearch.getArrMix().size(); i++) {
                                if (responseSearch.getArrMix().get(i).getType() == BaseAudio.MUSIC_TYPE) {
                                    searchAudios.add(responseSearch.getArrMix()
                                            .get(i).getAudio());
                                }
                            }
                            doSelectAudio(baseAudio.getAudio(), searchAudios, null, searchAudios.indexOf(baseAudio.getAudio()));
                        } else
                            // Album
                            if (baseAudio.getType() == BaseAudio.ALBUM_TYPE) {
                                doSelectAlbum(baseAudio.getAlbum(), responseSearch.getArrMeasure());
                            }
                    }
            }
            // 收听历史搜素
            if (null != responseHistorySearch) {
                SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
                if (!CollectionUtils.isEmpty(responseHistorySearch.getArrAlbum()) && responseHistorySearch.getArrAlbum().size() > index) {
                    doSelectAlbum(responseHistorySearch.getArrAlbum().get(index), null);
                }
            }
            // 某个专辑音频的搜素
            if (null != responseAlbumAudio) {
                SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
                if (CollectionUtils.isNotEmpty(responseAlbumAudio.getArrAudio()) && responseAlbumAudio.getArrAudio().size() > index) {
                    doSelectAudio(responseAlbumAudio.getArrAudio().get(index), responseAlbumAudio.getArrAudio(), mReqAlbum, index);
                }
            }
        } finally {
            // 打开界面
            Utils.jumpTOMediaPlayerAct(false);
        }
    }


    /**
     * 播放音频
     *
     * @param audio    带歌曲的时候直接播放该歌曲 播放列表为历史播放+该歌曲，否则为声控里面所有的歌曲
     * @param playList 播放列表 当带歌曲的时候可以为null
     * @param album    用户是否搜索的是某个专辑，然后选择的第几个
     * @param index    播放播放列表中的第几个歌曲
     */
    private void doSelectAudio(Audio audio, List<Audio> playList, Album album, int index) {
        LogUtil.logd(TAG + "choice audio " + playList.get(index).toString() + ",album:" + album);
        // 如果搜索的是专辑，则走播放专辑的逻辑
        if (album != null) {
            AlbumEngine.getInstance().playAlbumWithAudio(EnumState.Operation.sound, album, audio, true);
            return;
        }
        if (mReq != null && mReq instanceof ReqSearch && StringUtils.isNotEmpty(((ReqSearch) mReq).getAudioName())) {
//            List<Integer> sids = Utils.getSongSid();
//            List<Audio> localMusic = DBManager.getInstance().findHistoryAudioBySid(sids);
//            if (CollectionUtils.isNotEmpty(localMusic)) {
//                if (localMusic.contains(audio)) {
//                    localMusic.remove(audio);
//                }
//            }
//            localMusic.add(0, audio);
//            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, localMusic, null, 0, PlayInfoManager.DATA_SEARCH);

            List<HistoryData> musicHistory = DBManager.getInstance().findMusicHistory();
            List<Audio> audios = DBManager.getInstance().convertHistoryDataToAudio(musicHistory);
            if (CollectionUtils.isNotEmpty(audios)) {
                if (audios.contains(audio)) {
                    audios.remove(audio);
                }
            }
            audios.add(0, audio);
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, null, 0, PlayInfoManager.DATA_SEARCH);
        } else {
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, playList, null, index, PlayInfoManager.DATA_SEARCH);
        }
        //声控搜索为音频的则不支持上下拉刷新
        PlayInfoManager.getInstance().setFirstEnd(true);
        PlayInfoManager.getInstance().setLastEnd(true);
        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
    }


    /**
     * 播放选中的专辑
     */
    private void doSelectAlbum(Album album, List<ReqChapter> chapters) {
        LogUtil.logd(TAG + "choice album " + album.toString());

        long categoryId = 0;
        if (null != album.getArrCategoryIds() && !album.getArrCategoryIds().isEmpty()) {
            categoryId = album.getArrCategoryIds().get(0);
            album.setCategoryId((int) categoryId);
        }

        if (CollectionUtils.isNotEmpty(chapters)) {
            AlbumEngine.getInstance().playAlbumWithChapters(EnumState.Operation.sound, album, categoryId, chapters);
        } else {
            AlbumEngine.getInstance().playAlbumWithBreakpoint(EnumState.Operation.sound, album, categoryId);//这里之前是manual,现在改为sound
        }
        DBManager.getInstance().saveAlbum(album);
    }


    public void searchPreloadIndex(int position) {
        Audio audio = null;
        ResponseSearch responseSearch = null;
        if (isLocal || mResponse == null) {
            return;//本地的则不用进行预加载
        }
        if (mResponse instanceof ResponseSearch) {
            responseSearch = (ResponseSearch) mResponse;
        }
        if (responseSearch != null) {
            if (responseSearch.getReturnType() == 1 && CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
                audio = responseSearch.getArrAudio().get(position);
            } else if (responseSearch.getReturnType() == 3 && CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
                BaseAudio baseAudio = responseSearch.getArrMix().get(position);
                if (baseAudio.getAudio() != null) {
                    audio = baseAudio.getAudio();
                }
            }
        }

        //只有音频才缓存
        if (audio != null) {
            LogUtil.logd(Constant.PRELOAD_TAG + ":sound:audio:" + position + "," + audio.getName());
//            DataInterfaceBroadcastHelper.sendStartPreloadNextAudioInfo(audio);
        } else {
            LogUtil.logd(Constant.PRELOAD_TAG + ":sound:album:" + position + ",can't load");
        }
    }
}
