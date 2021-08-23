package com.txznet.music.search;

import android.annotation.SuppressLint;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
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
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.dao.DaoManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.historyModule.ui.HistoryDataSource;
import com.txznet.music.localModule.logic.AlbumUtils;
import com.txznet.music.data.http.req.BaseReq;
import com.txznet.music.data.http.resp.BaseResponse;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.rx.NetErrorException;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.soundControlModule.bean.BaseAudio;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;
import com.txznet.music.soundControlModule.logic.net.request.ReqHistorySearch;
import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseHistorySearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseSearch;
import com.txznet.music.utils.ArrayUtils;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
    //    private String rawText = "";
    private SearchRecord searchRecord = new SearchRecord();
    private int mRequestId;
    private long sendTime;

    private boolean speakSearchTTs = false;//是否播报TTs（逻辑：如果播报了，则等播报完毕之后再播报搜索结果的反馈）

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
        return searchRecord.rawText;
    }

    public void setRawText(String rawText) {
        searchRecord.rawText = rawText;
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
                            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
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
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                    Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                    false, new Runnable1<ReqHistorySearch>(reqHistorySearch) {
                        @Override
                        public void run() {
                            mRequestId = NetManager.getInstance().requestHistory(mP1);
                        }
                    });
        }

    }

    private ReqSearch createSearchObj(String soundData) {
        Logger.d(TAG, "searchFromCore:" + soundData);
        final ReqSearch reqData = new ReqSearch();
        JSONBuilder jsonBuilder = new JSONBuilder(soundData);
        reqData.setAudioName(jsonBuilder.getVal("title", String.class));
        reqData.setAlbumName(jsonBuilder.getVal("album", String.class));
        reqData.setArtist(StringUtils.toString(jsonBuilder.getVal("artist", String[].class)));
        reqData.setCategory(StringUtils.StringFilter(StringUtils.toString(jsonBuilder.getVal("keywords", String[].class))));
        reqData.setField(jsonBuilder.getVal("field", int.class, 0));// 1。表示歌曲，2.表示电台
        reqData.setSubCategory(jsonBuilder.getVal("subcategory", String.class));
        reqData.setText(jsonBuilder.getVal("text", String.class));
        reqData.getArrMeasure().clear();
        String[] audioIndices = jsonBuilder.getVal("audioIndex", String[].class, null);
        if (!ArrayUtils.isEmpty(audioIndices)) {
            for (int i = 0; i < audioIndices.length; i += 2) {
                reqData.getArrMeasure().add(new ReqChapter(Integer.parseInt(audioIndices[i]), audioIndices[i + 1]));
            }
        }

        // 后台说:只认category
        if (StringUtils.isNotEmpty(reqData.getSubCategory())) {
            reqData.setCategory(reqData.getSubCategory());
        }

        if (StringUtils.isEmpty(reqData.getCategory())) {
            reqData.setCategory(jsonBuilder.getVal("category", String.class));
        }

        return reqData;
    }

//    public void doSearchLocal(ReqSearch reqdata) {
//        searchLocal(reqdata.getAudioName(), reqdata.getArtist(), reqdata.getAlbumName()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<Audio>>() {
//            @Override
//            public void accept(List<Audio> audios) throws Exception {
//
//            }
//        }, new Consumer<Throwable>() {
//            @Override
//            public void accept(Throwable throwable) throws Exception {
//
//            }
//        });
//    }

    private boolean hasNet() {
        return NetworkUtil.isNetworkAvailable(GlobalContext.get());
    }


    public class SearchResp {

    }

    public Consumer<ResponseSearch> getResultConsumer() {
        return new Consumer<ResponseSearch>() {
            @Override
            public void accept(final ResponseSearch responseSearch) throws Exception {
                mResponse = responseSearch;
                mySearchResultRunnable = new SearchResultRunnable() {
                    @Override
                    protected void handlerEvent() {
                        //结果
                        //后台可以进行配置。是否直接显示，以及显示多久

                        //1.如果是一条记录则，直接显示
                        // 2.如果是>1条记录则，给Core 显示
                        //3.如果是没有记录则，提示
                        JSONArray showData = createShowData(responseSearch);


                        if (showData != null) {
                            if (responseSearch.getPlayType() == ResponseSearch.GOPLAY) {
                                TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_PLAY_AUDIO
                                        , true
                                        , new Runnable() {
                                            @Override
                                            public void run() {
                                                choiceIndex(responseSearch.getPlayIndex(), false);
                                            }
                                        });
                                return;
                            }
                            displayShowData(false, showData);
                        } else {
                            speakTTSNotFound();
                        }
                    }
                };
                executeSearchResult(mySearchResultRunnable);

//                AppLogic.runOnUiGround(mySearchResultRunnable);
            }
        };
    }

    /**
     * 执行搜索结果的反馈
     */
    private void executeSearchResult(Runnable runnable) {
        //这里可以增加统一的处理
        if (speakSearchTipsRunnable != null) {
            AppLogic.removeUiGroundCallback(speakSearchTipsRunnable);
            speakSearchTipsRunnable = null;
        }

        if (mySearchResultRunnable != null) {
            AppLogic.removeUiGroundCallback(mySearchResultRunnable);

        }
        if (speakSearchTTs) {
            //等待
        } else {
            mySearchResultRunnable = runnable;
            AppLogic.runOnUiGround(runnable);
        }
    }

    private abstract class BaseThrowableConsumer implements Consumer<Throwable> {

        @Override
        public void accept(final Throwable throwable) throws Exception {
            clearRunnableWhenSpeak();
            AppLogic.runOnUiGround(new SearchResultRunnable() {
                @Override
                protected void handlerEvent() {
                    if (throwable instanceof NetErrorException) {
                        speakTTSSearchTimeOut();
                    } else {
                        try {
                            subAccept(throwable);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public abstract void subAccept(Throwable throwable) throws Exception;
    }

    @SuppressLint("CheckResult")
    public void search(String soundData) {
        mResponse = null;
        mReq = createSearchObj(soundData);
        ReqSearch reqData = ((ReqSearch) mReq);
        //正常的请求
        if (hasNet()) {
            if (containNameFromSearch(reqData)) {
                subscribe = searchOnline(reqData)
                        .flatMap(new Function<ResponseSearch, ObservableSource<ResponseSearch>>() {
                            @Override
                            public ObservableSource<ResponseSearch> apply(ResponseSearch responseSearch) throws Exception {
                                if (responseSearch.getErrCode() == 0) {
                                    JSONArray showData = createShowData(responseSearch);
                                    if (showData != null) {
                                        return Observable.just(responseSearch);
                                    }
                                }
                                return Observable.empty();
                            }
                        })
                        .switchIfEmpty(searchLocal(reqData.getAudioName(), reqData.getArtist(), reqData.getAlbumName()))
                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                                subscribe(getResultConsumer(), new BaseThrowableConsumer() {
                                    @Override
                                    public void subAccept(Throwable throwable) throws Exception {
                                        LogUtil.loge(TAG + "在线情况：搜索本地", throwable);
                                        speakTTSSearchException();
                                    }
                                });
                return;
            } else if (containChapterData(reqData)) {
                final Album album = PlayInfoManager.getInstance().getCurrentAlbum();
                if (album != null && !Utils.isSong(album.getSid())) {
                    searchChapter(reqData, album).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ResponseAlbumAudio>() {
                        @Override
                        public void accept(final ResponseAlbumAudio responseAlbumAudio) throws Exception {
                            mySearchResultRunnable = new SearchResultRunnable() {
                                @Override
                                protected void handlerEvent() {
                                    handlerCapter(responseAlbumAudio, album);
                                }
                            };
                            AppLogic.runOnUiGround(mySearchResultRunnable);
                        }
                    }, new BaseThrowableConsumer() {
                        @Override
                        public void subAccept(Throwable throwable) throws Exception {
                            speakTTSSearchException();
                        }
                    });
                }
                return;
            }
            Observable.create(new ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                    e.onError(new RuntimeException());
                    e.onComplete();
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) throws Exception {
                    //不需要处理，因为肯定出错，之所以这么做，是为了统一处理错误
                }
            }, new BaseThrowableConsumer() {
                @Override
                public void subAccept(Throwable throwable) throws Exception {
                    speakTTSNOStart();
                }
            });

        } else {
            subscribe = searchLocal(reqData.getAudioName(), reqData.getArtist(), reqData.getAlbumName()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(getResultConsumer(),
                    new BaseThrowableConsumer() {
                        @Override
                        public void subAccept(Throwable throwable) throws Exception {
                            LogUtil.loge(TAG + "离线情况：搜索本地", throwable);
                            speakTTSSearchException();
                        }
                    });
        }
    }

    private void handlerCapter(ResponseAlbumAudio responseAlbumAudio, Album album) {
        if (responseAlbumAudio.getErrMeasure() != 0) {
            Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
            if (currentAlbum.getSid() == responseAlbumAudio.getSid() && currentAlbum.getId() == responseAlbumAudio.getId()) {
                speakNotFoundForChapter();
            } else {
                speakNotFoundChapterAndPlayHistory(album);
            }
        } else {
            speakFoundForChapter(responseAlbumAudio, album);
        }
    }

    private void speakFoundForChapter(ResponseAlbumAudio responseAlbumAudio, final Album album) {
        final List<Audio> arrAudio = responseAlbumAudio.getArrAudio();
        String reportString = StringUtils.getReportString(arrAudio.get(0).getReport(), arrAudio.get(0).getName());
        TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_PLAY_AUDIO + reportString
                , true
                , new Runnable() {
                    @Override
                    public void run() {
                        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, arrAudio, album, 0, PlayInfoManager.DATA_SEARCH);
                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                    }
                });
    }

    private void speakFoundForAudio(ResponseAlbumAudio responseAlbumAudio, final Audio audio) {

    }


    private void speakNotFoundChapterAndPlayHistory(final Album album) {
        List<Audio> breakpointByAlbumId = DBManager.getInstance().findBreakpointByAlbumId(album.getId());
        if (breakpointByAlbumId != null && !breakpointByAlbumId.isEmpty()) {
            final Audio breakpointAudio = DBManager.getInstance().findAudio(breakpointByAlbumId.get(0));
            String reportString = StringUtils.getReportString(breakpointAudio.getReport(), breakpointAudio.getName());
            TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_PLAY_AUDIO + reportString
                    , true
                    , new Runnable() {
                        @Override
                        public void run() {
                            AlbumEngine.getInstance().playAlbumWithAudio(PlayInfoManager.DATA_SEARCH, EnumState.Operation.sound, album, breakpointAudio, false);
                        }
                    });
        } else {
            speakTTSNotFound();
        }
    }

    private void speakNotFoundForChapter() {
        TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_NOT_FOUND_CHAPTERS"
                , Constant.RS_VOICE_MUSIC_NOT_FOUND_CHAPTERS
                , true
                , new Runnable() {
                    @Override
                    public void run() {
                        PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
                    }
                });
    }

    private boolean containNameFromSearch(ReqSearch reqData) {
        return (StringUtils.isNotEmpty(reqData.getAudioName()) || StringUtils.isNotEmpty(reqData.getAlbumName()) || StringUtils.isNotEmpty(reqData.getCategory()) || StringUtils.isNotEmpty(reqData.getArtist()));
    }

    private boolean containChapterData(ReqSearch reqData) {
        return CollectionUtils.isNotEmpty(reqData.getArrMeasure());
    }

    public Consumer getLocalConsumer() {
        return new Consumer<List<Audio>>() {
            @Override
            public void accept(List<Audio> audios) throws Exception {
                JSONArray jsonArray = new JSONArray();
                int count = createJsonAudio(jsonArray, LENGTH, audios);
                if (count <= 0) {
                    speakTTSNotFound();
                } else {
                    displayShowData(false, jsonArray);
                }
            }
        };
    }

    private void displayShowData(boolean continuePlay, JSONArray showData) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        if (continuePlay) {
            jsonBuilder.put("continuePlay", true);
        }
        sendListToCore(jsonBuilder, showData);
    }

    private JSONArray createShowData(ResponseSearch responseSearch) {
        JSONArray jsonArray = new JSONArray();
        int hasShowCount = 0;
        if ((hasShowCount = createJsonMix(jsonArray, LENGTH, responseSearch)) < LENGTH) {
            if ((hasShowCount += createJsonAudio(jsonArray, LENGTH - hasShowCount, responseSearch)) < LENGTH) {
                if ((hasShowCount += createJsonAlbum(jsonArray, LENGTH - hasShowCount, responseSearch)) < LENGTH) {
                    if (hasShowCount <= 0) {
                        // TODO: 2018/6/6 播报异常
                        return null;
                    }
                }
            }
        }

        return jsonArray;
    }

    /**
     * @param jsonArray
     * @return 添加的个数
     */
    private int createJsonMix(JSONArray jsonArray, int maxLength, ResponseSearch responseSearch) {
        int addCount = 0;
        int length = maxLength;
        if (responseSearch.getReturnType() == ResponseSearch.TYPE_MIX && CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
            if (responseSearch.getArrMix().size() < maxLength) {
                length = responseSearch.getArrMix().size();
            }

            for (int i = 0; i < length; i++) {

                BaseAudio baseAudio = responseSearch.getArrMix().get(i);
                if (null == baseAudio) {
                    continue;
                }
                addCount++;
                JSONObject jsonObject = new JSONObject();
                if (baseAudio.getAlbum() != null) {
                    Album album = baseAudio.getAlbum();
                    DBManager.getInstance().insertAlbumIfNotExist(album);
                    jsonObject = getAlbumJsonObject(album, responseSearch);
                } else if (baseAudio.getAudio() != null) {
                    Audio audio = baseAudio.getAudio();
                    jsonObject = getAudioJsonObject(audio, responseSearch);
                }
                jsonArray.put(jsonObject);
            }
        }
        return addCount;
    }

    private int createJsonAudio(JSONArray jsonArray, int maxLength, ResponseSearch responseSearch) {
        int addCount = 0;
        int length = maxLength;
        if (responseSearch.getReturnType() == ResponseSearch.TYPE_AUDIO && CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
            if (responseSearch.getArrAudio().size() < maxLength) {
                length = responseSearch.getArrAudio().size();
            }

            for (int i = 0; i < length; i++) {
                JSONObject jsonObject;
                Audio audio = responseSearch.getArrAudio().get(i);
                if (null == audio) {
                    continue;
                }
                addCount++;
                jsonObject = getAudioJsonObject(audio, responseSearch);
                jsonArray.put(jsonObject);
            }
        }
        return addCount;
    }

    private int createJsonAudio(JSONArray jsonArray, int maxLength, List<Audio> audios) {
        int addCount = 0;
        int length = maxLength;
        if (CollectionUtils.isNotEmpty(audios)) {
            if (audios.size() < maxLength) {
                length = audios.size();
            }

            for (int i = 0; i < length; i++) {
                JSONObject jsonObject;
                Audio audio = audios.get(i);
                if (null == audio) {
                    continue;
                }
                addCount++;
                jsonObject = getAudioJsonObject(audio, null);
                jsonArray.put(jsonObject);
            }
        }
        return addCount;
    }

    private int createJsonAlbum(JSONArray jsonArray, int maxLength, ResponseSearch responseSearch) {
        int addCount = 0;
        int length = maxLength;
        if (responseSearch.getReturnType() == ResponseSearch.TYPE_ALBUM && CollectionUtils.isNotEmpty(responseSearch.getArrAlbum())) {
            if (responseSearch.getArrAlbum().size() < maxLength) {
                length = responseSearch.getArrAlbum().size();
            }
            for (int i = 0; i < length; i++) {
                addCount++;
                JSONObject jsonObject;
                Album album = responseSearch.getArrAlbum().get(i);
                if (album == null) {// 服务器有可能返回null对象
                    continue;
                }
                DBManager.getInstance().insertAlbumIfNotExist(album);
                jsonObject = getAlbumJsonObject(album, responseSearch);
                jsonArray.put(jsonObject);
            }
        }
        return addCount;
    }

    /**
     * 搜索章节回
     */
    private Observable<ResponseAlbumAudio> searchChapter(ReqSearch reqData, Album album) {
        ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
        reqAlbumAudio.setSid(album.getSid());
        reqAlbumAudio.setId(album.getId());
        reqAlbumAudio.setUp(0);
        reqAlbumAudio.setCategoryId(album.getCategoryId());
        reqAlbumAudio.setOffset(Constant.PAGECOUNT);
        reqAlbumAudio.setArrMeasure(reqData.getArrMeasure());

        //发起搜索
        return RxNet.request(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, ResponseAlbumAudio.class);
    }


    public void searchError() {

    }

    public void searchSuccess() {

    }

    //    Runnable1 searchResultRunnable = ;
    Runnable mySearchResultRunnable = null;

    Runnable speakSearchTipsRunnable = null;

    /**
     * 必须在backGround中执行，否则，去除不掉播报tts的runnable
     */
    private abstract class SearchResultRunnable implements Runnable {
        @Override
        public void run() {
            //这里可以增加统一的处理
            if (speakSearchTipsRunnable != null) {
                AppLogic.removeUiGroundCallback(speakSearchTipsRunnable);
                speakSearchTipsRunnable = null;
            }
            if (mySearchResultRunnable != null) {
                AppLogic.removeUiGroundCallback(mySearchResultRunnable);
                mySearchResultRunnable = null;
            }

            handlerEvent();
        }

        protected abstract void handlerEvent();
    }


    public void doSoundFind(final int sourceId, final String soundData) {
        int delayTime = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SEARCH_TIPS_DELAY, 0);

        speakSearchTipsRunnable = new Runnable() {
            @Override
            public void run() {
                speakVoice(sourceId, "RS_VOICE_SPEAK_SEARCHDATA_TIPS",
                        Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, false,
                        false, new Runnable() {
                            @Override
                            public void run() {
                                AppLogic.removeUiGroundCallback(this);
                                if (mySearchResultRunnable == null) {
                                    //重新执行延时执行
                                    AppLogic.runOnUiGround(this, 8000);
                                } else {
                                    AppLogic.removeUiGroundCallback(mySearchResultRunnable);
                                    AppLogic.runOnUiGround(mySearchResultRunnable);
                                }
                            }
                        });
            }
        };

        Logger.d(TAG, "searchTips:delay:" + delayTime);
        AppLogic.runOnUiGround(speakSearchTipsRunnable, delayTime);
        search(soundData);
    }

    Disposable subscribe = null;

    private void speakVoice(int sourceId, String resId, String sText, String[] resArgs, boolean close, final boolean needAsr, final Runnable oRun) {
        Log.d(TAG, "error search audio null,from = " + sourceId);
        if (sourceId == -2) {//表明从广播进入
            TtsUtil.speakVoice(-1, resId, resArgs, sText, null, TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY, new TtsUtil.ITtsCallback() {
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
            TtsUtil.speakVoice(TtsUtil.DEFAULT_STREAM_TYPE, resId, null, sText, null, TtsUtil.PreemptType.PREEMPT_TYPE_NEXT, new TtsUtil.ITtsCallback() {
                @Override
                public void onBegin() {
                    super.onBegin();
                    speakSearchTTs = true;
                }

                @Override
                public void onEnd() {
                    super.onEnd();
                    speakSearchTTs = false;
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    if (oRun != null) {
                        oRun.run();
                    }
                }
            });


//            TtsUtil.speakTextOnRecordWin(resId, sText, resArgs, close, needAsr, oRun);
        }
    }

    public void cancelSearch() {
        NetManager.getInstance().cancelRequest(mRequestId);
        if (subscribe != null) {
            subscribe.dispose();
            subscribe = null;
        }
    }


    public void handleError() {
        TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, true, null);
    }

    public void handleHistorySearch(List<Album> albums) {
        LogUtil.logd(TAG + "handleHistorySearch albums:" + (albums == null ? 0 : albums.size()));
        if (mReq == null || !(mReq instanceof ReqHistorySearch)) {
            LogUtil.logw(TAG + "request null or not history request!");
            return;
        }
        if (CollectionUtils.isEmpty(albums)) {
            String tips = Constant.RS_VOICE_SPEAK_NODATAFOUND_TIPS;
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS", tips, false, null);
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
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS", tips, false, null);
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
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
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
        TtsUtil.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, true, null);
    }

    private JSONObject getAudioJsonObject(Audio audio, ResponseSearch responseSearch) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", audio.getName());
            jsonObject.put("name", StringUtils.toString(audio
                    .getArrArtistName()));
            jsonObject.put("id", audio.getId());
            jsonObject.put("report", audio.getReport());
            if (responseSearch != null && responseSearch.getPlayType() == ResponseSearch.DELAYPLAY) {
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
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NODATAFOUND_TIPS",
                tips, false, null);
    }

    public void speakTTSSearchException() {
        String tips = Constant.RS_VOICE_SPEAK_SEARCH_EXCEPTION;
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SEARCH_EXCEPTION", tips, false, null);
    }

    public void speakTTSSearchTimeOut() {
        String tips = Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS;
        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS", tips, false, null);
    }


    private void sendListToCore(JSONBuilder jsonBuilder, JSONArray jsonArray) {
        // 如果支持v2版本就发v2的信息
        if (AppLogic.txzVersion >= 264) {
            jsonBuilder.put("list", jsonArray.toString());
            LogUtil.logd(TAG + " send:" + jsonBuilder.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist.v2", jsonBuilder.toBytes(), null);
        } else {
            LogUtil.logd(TAG + " send:v1:" + jsonArray.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist", jsonArray.toString().getBytes(), null);
        }
    }


    public void clearRunnableWhenSpeak() {
        if (speakSearchTipsRunnable != null) {
            AppLogic.removeUiGroundCallback(speakSearchTipsRunnable);
            speakSearchTipsRunnable = null;
        }
        if (mySearchResultRunnable != null) {
            AppLogic.removeUiGroundCallback(mySearchResultRunnable);
            mySearchResultRunnable = null;
        }
    }

    public void speakTTSNOStart() {
        clearRunnableWhenSpeak();
        LogUtil.logd(TAG + "album:" + PlayInfoManager.getInstance().getCurrentAlbum() + ",audio:" + PlayInfoManager.getInstance().getCurrentAudio());
        TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_NO_CONTEXT", Constant.RS_VOICE_MUSIC_NO_CONTEXT, false, null);
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
                    if (responseSearch.getReturnType() == ResponseSearch.TYPE_AUDIO) { // 不是专辑，直接播放
                        break;
                    }

                    if (responseSearch.getReturnType() == ResponseSearch.TYPE_ALBUM) {
                        if (responseSearch.getArrAlbum() != null && responseSearch.getArrAlbum().size() > index) {
                            album = responseSearch.getArrAlbum().get(index);
                        }
                    }
                    if (responseSearch.getReturnType() == ResponseSearch.TYPE_MIX) {
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

//            PlayEngineFactory.getEngine().release(EnumState.Operation.sound);// 暂停上一首歌曲的数据
            if (isLocal) {
                // 如果是专辑的话
                List<Audio> audios = this.audios.subList(index, index + 1);
                LogUtil.logd(TAG + "playmusiclist.index::localMusic::" + audios.toString());
                if (CollectionUtils.isEmpty(audios)) {
                    return;
                }
                List<Audio> localAudios = DBManager.getInstance().findAllLocalAudios();
                doSelectAudio(audios.get(0), localAudios, null, localAudios.indexOf(audios.get(0)));
                return;
            }
            // 默认搜素
            if (null != responseSearch) {
                SharedPreferencesUtils.setAudioSource(Constant.TYPE_SOUND);
                if (responseSearch.getReturnType() == ResponseSearch.TYPE_ALBUM) {// 专辑
                    Album album = responseSearch.getArrAlbum().get(index);
                    doSelectAlbum(album, responseSearch.getArrMeasure());
                } else if (responseSearch.getReturnType() == ResponseSearch.TYPE_AUDIO) {
                    doSelectAudio(responseSearch.getArrAudio().get(index),
                            responseSearch.getArrAudio(), null, index);
                } else
                    // 有可能有音频或专辑
                    if (responseSearch.getReturnType() == ResponseSearch.TYPE_MIX) {
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

    public boolean isAudioChoice() {
        if (mResponse != null) {
            if (mResponse instanceof ResponseSearch) {
                if (((ResponseSearch) mResponse).getPlayType() == ResponseSearch.DELAYPLAY) {
                    if (((ResponseSearch) mResponse).getDelayTime() > SystemClock.elapsedRealtime() - sendTime) {
                        return true;
                    }
                } else if (((ResponseSearch) mResponse).getPlayType() == ResponseSearch.GOPLAY) {
                    return true;
                }
            }
        }
        return false;
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
        if (isAudioChoice()) {
            ReportEvent.reportClickSearchDataAuto(audio, album, searchRecord.json);
        } else {
            ReportEvent.reportClickSearchDataUser(audio, album, searchRecord.json);
        }
        // 如果搜索的是专辑，则走播放专辑的逻辑
        if (album != null) {
            PlayEngineFactory.getEngine().release(EnumState.Operation.sound);// 暂停上一首歌曲的数据
            AlbumEngine.getInstance().playAlbumWithAudio(PlayInfoManager.DATA_SEARCH, EnumState.Operation.sound, album, audio, true);
            return;
        }
        if (mReq != null && mReq instanceof ReqSearch && StringUtils.isNotEmpty(((ReqSearch) mReq).getAudioName())) {
            choiceAudio(audio);
        } else {
            PlayEngineFactory.getEngine().release(EnumState.Operation.sound);// 暂停上一首歌曲的数据
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, playList, null, index, PlayInfoManager.DATA_SEARCH);
            //声控搜索为音频的则不支持上下拉刷新
            PlayInfoManager.getInstance().setFirstEnd(true);
            PlayInfoManager.getInstance().setLastEnd(true);
            PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
        }

    }


    /**
     * 播放选中的专辑
     */
    private void doSelectAlbum(Album album, List<ReqChapter> chapters) {
        LogUtil.logd(TAG + "choice album " + album.toString());
        if (isAudioChoice()) {
            ReportEvent.reportClickSearchDataAuto(null, album, searchRecord.json);
        } else {
            ReportEvent.reportClickSearchDataUser(null, album, searchRecord.json);
        }
        if (CollectionUtils.isNotEmpty(chapters)) {
            AlbumEngine.getInstance().playAlbumWithChapters(PlayInfoManager.DATA_SEARCH, EnumState.Operation.sound, album, album.getCategoryId(), chapters);
        } else {
            AlbumEngine.getInstance().playAlbumWithBreakpoint(PlayInfoManager.DATA_SEARCH, EnumState.Operation.sound, album, album.getCategoryId());//这里之前是manual,现在改为sound
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
            if (responseSearch.getReturnType() == ResponseSearch.TYPE_AUDIO && CollectionUtils.isNotEmpty(responseSearch.getArrAudio())) {
                audio = responseSearch.getArrAudio().get(position);
            } else if (responseSearch.getReturnType() == ResponseSearch.TYPE_MIX && CollectionUtils.isNotEmpty(responseSearch.getArrMix())) {
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


    //播放指定的歌曲
    public void choiceAudio(Audio audio) {
        //将选中的碎片,如果播单为音乐播单,则插入到播单中,并且还需要支持上下拉加载,获取到最新的数据
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        audio.setIsInsert(1);//用于数据上报

        if (currentAudio != null && currentAudio.equals(audio)) {
            //正在播放的和搜索到的为同一首则，不变化 -- 0705调整为继续播放
            PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
            return;
        }


        if (currentAudio != null && Utils.isSong(currentAudio.getSid())) {


            //插入到播单中
            PlayInfoManager.getInstance().removePlayListAudio(audio);
            PlayInfoManager.getInstance().addAudio(audio, PlayInfoManager.getInstance().playListIndexOf(currentAudio) + 1);

            //播放下一首歌曲
            PlayEngineFactory.getEngine().playAudio(EnumState.Operation.sound, audio);
        } else {
            List<Audio> audios = new ArrayList<>();
            audios.add(audio);
            //新起一个播单,播单中就一首歌
            choiceAudios(audios, 0);
        }

    }

    /**
     * @param audios 新的播单
     * @param index
     */
    public void choiceAudios(List<Audio> audios, int index) {
        //来源不变
        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, null, index, PlayInfoManager.DATA_SEARCH);
        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
    }


//    public void searchLocal(String audioName, String artists, IFinishCallBack callBack) {
//
//    }


    public Observable<ResponseSearch> searchLocal(final String audioName, final String artists, final String albumName) {

        return Observable.create(new ObservableOnSubscribe<ResponseSearch>() {
            @Override
            public void subscribe(ObservableEmitter<ResponseSearch> e) throws Exception {
                Logger.d(TAG, "本地搜索：" + audioName + "," + artists + "," + albumName);
                List<Audio> localAudioForSearch = DaoManager.getInstance().findLocalAudioForSearch(audioName, artists, albumName);
                ResponseSearch responseSearch = new ResponseSearch();
                responseSearch.setReturnType(ResponseSearch.TYPE_AUDIO);
                responseSearch.setArrAudio(localAudioForSearch);
                e.onNext(responseSearch);
                e.onComplete();
            }
        });
//
//
//        if (CollectionUtils.isNotEmpty(localAudioForSearch)) {
//            return Observable.just(localAudioForSearch).flatMap(new Function<List<Audio>, ObservableSource<ResponseSearch>>() {
//                @Override
//                public ObservableSource<ResponseSearch> apply(List<Audio> audios) throws Exception {
//                    ResponseSearch responseSearch = new ResponseSearch();
//                    responseSearch.setArrAudio(audios);
//                    return Observable.just(responseSearch);
//                }
//            });
//        } else {
//            return Observable.empty();
//        }
    }

    public Observable<ResponseSearch> searchOnline(ReqSearch search) {
        return RxNet.request(Constant.GET_SEARCH, search, ResponseSearch.class);
    }
}
