package com.txznet.music.model;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.SearchResult;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqSearch;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.Logger;
import com.txznet.music.util.TXZAppUtils;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.action.ActionType.ACTION_SEARCH_KEY_JUST_INVOKE;
import static com.txznet.music.action.ActionType.ACTION_SEARCH_KEY_KEYWORD;

/**
 * @author telen
 * @date 2018/12/3,16:53
 */
public class SearchModel extends RxWorkflow {
    private static final String TAG = Constant.LOG_TAG_SEARCH + ":Model";

    private static final int SEARCH_SIZE = 20;

    private Runnable mSpeakSearchTipsRunnable;
    private int mSearchTipsTtsId = TtsUtil.INVALID_TTS_TASK_ID;

    private SearchResult mSearchResult; // 搜索结果
    private Throwable mThrowable;
    private RxAction mRxAction;
    private RxAction mRxActionGOPLAY;


    /**
     * 搜索的json串
     */
    private JSONArray mJSONArray;

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_SEARCH_GET_DATA:
                search(action.operation, (String) action.data.get(ACTION_SEARCH_KEY_KEYWORD), (Boolean) action.data.get(ACTION_SEARCH_KEY_JUST_INVOKE));
                break;
            case ActionType.ACTION_SEARCH_EVENT_CHOICE_SEARCH_RESULT:
                choiceSearchResult(action.operation, (Integer) action.data.get(ActionType.ACTION_SEARCH_KEY_SEARCH_CHOICE));
                break;
            case ActionType.ACTION_SEARCH_CANCEL:
                DisposableManager.get().remove("getSearchDataV2");
                break;
            default:
                break;
        }
    }

    private void choiceSearchResult(Operation operation, int index) {
        if (mSearchResult != null) {
            Object result = mSearchResult.choice(index);

            if (result != null) {
                //判断选择了音频还是专辑
                // FIxMe: 2018/11/26 播放逻辑错误,插入播放列表错误
                if (result instanceof AudioV5) {
                    if (mSearchResult.keyword != null) {
                        TXZReqSearch reqSearch = TXZMusicDataSource.createSearchObj(mSearchResult.keyword);
                        if (TextUtils.isEmpty(reqSearch.audioName)) {
                            // 声控播放xx歌手的歌
                            // 我要听流行音乐
                            choiceAudio(operation, (AudioV5) result, mSearchResult.convert2AudioList());
                            return;
                        }
                    }
                    choiceAudio(operation, (AudioV5) result, null);
                } else if (result instanceof Album) {
                    choiceAlbum(operation, (Album) result);
                } else {
                    Logger.d(TAG, "please:choice:" + result.toString());
                }
            } else {
                throw new RuntimeException("telenewbie;choiceIndex is ,content: is null");
            }
        } else {
            throw new RuntimeException("telenewbie;searchResult is null? why");
        }
    }

    private void search(Operation operation, String keyword, boolean justInvoke) {
        if (justInvoke) {
            getSearchDataV2(operation, keyword);
        } else {
            beginCheckSearchResultTimeout();
            getSearchData(operation, keyword);
        }
    }

    // 异步检测搜索结果超时
    private void beginCheckSearchResultTimeout() {
        int delayTime = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_SEARCH_TIPS_DELAY, 0);
        mSpeakSearchTipsRunnable = () -> mSearchTipsTtsId = TtsUtil.speakVoice(TtsUtil.DEFAULT_STREAM_TYPE, "RS_VOICE_SPEAK_SEARCHDATA_TIPS", null,
                Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS, null, TtsUtil.PreemptType.PREEMPT_TYPE_NEXT, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onEnd() {
                        mSearchTipsTtsId = TtsUtil.INVALID_TTS_TASK_ID;
                    }

                    @Override
                    public void onSuccess() {
                        synchronized (SearchModel.this) {
                            mSearchTipsTtsId = TtsUtil.INVALID_TTS_TASK_ID; // 避免onEnd异步问题
                            // 播报完的时候，搜索结果有没有
                            invokeGoPlay();
                            invokeErrorReport(null, null);
                            showListToCore();
                        }
                    }
                });
        Logger.d(TAG, "searchTips:delay:" + delayTime);
        AppLogic.runOnUiGround(mSpeakSearchTipsRunnable, delayTime);
    }

    private void initData() {
        AppLogic.removeUiGroundCallback(mSpeakSearchTipsRunnable);
    }

    private void invokeErrorReport(RxAction action, Throwable throwable) {
        initData();
        if (throwable != null) {
            mThrowable = throwable;
        }
        if (action != null) {
            mRxAction = action;
        }
        if (!isSearchingBroadcast() && mRxAction != null && mThrowable != null) {//发生了异常
            postRxError(mRxAction, mThrowable);
            mThrowable = null;
            mRxAction = null;
        }
    }

    /**
     * 将GOPLAY类型的直接处理掉
     */
    private void invokeGoPlay() {
        initData();
        if (!isSearchingBroadcast() && mRxActionGOPLAY != null) {  // 有
            TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_PLAY_AUDIO", Constant.RS_VOICE_MUSIC_PLAY_AUDIO, null, true, () -> {
                if (mRxActionGOPLAY != null) {
                    postRxData(mRxActionGOPLAY); // ACTION_GET_SEARCH_RESULT
                    mRxActionGOPLAY = null;
                }
            });
        }
    }

    private void showListToCore() {
        initData();
        if (!isSearchingBroadcast() && mJSONArray != null) {
            displayShowData(mJSONArray);
            //显示完毕就清空
            mJSONArray = null;
        }
    }


    /**
     * 是否播报中,"正在为你搜索"
     *
     * @return true 为在播报 false 为没在播报
     */
    private boolean isSearchingBroadcast() {
        return mSearchTipsTtsId != TtsUtil.INVALID_TTS_TASK_ID;
    }


    //{"field":1,"text":"我要听小苹果","title":"小苹果","keywords":[],"artist":[],"album":""}
    private void getSearchData(Operation operation, String keyword) {
        final RxAction action = RxAction.type(ActionType.ACTION_GET_SEARCH_RESULT).operation(operation).build();
        Observable<SearchResult> searchResultObservable;
        if (hasNet()) {
            searchResultObservable = TXZMusicDataSource.get().listSearch(keyword, 1);
        } else {
            searchResultObservable = TXZMusicDataSource.get().searchLocal(keyword);
        }
        Disposable subscribe = searchResultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(searchResult -> {
            Logger.d(Constant.LOG_TAG_SEARCH, "accept->" + searchResult);
            //通过Dispatcher进行传递
            //展示给Core
            mSearchResult = searchResult;
            mSearchResult.keyword = keyword;

            //如果直接播放,则不需要传递给Core进行展示
            if (searchResult.playType == SearchResult.GOPLAY) {
                action.data.put("value", searchResult);
                mRxActionGOPLAY = action; // ACTION_GET_SEARCH_RESULT
                invokeGoPlay();
                return;
            }
            synchronized (SearchModel.this) {
                mJSONArray = new JSONArray();
                createJsonMix(mJSONArray, SEARCH_SIZE, searchResult);

                if (mJSONArray.length() <= 0) {
                    mJSONArray = null;
                    if (hasNet()) {
                        invokeErrorReport(action, new Error(ErrCode.ERROR_CLIENT_NET_EMPTY_DATA));
                    } else {
                        invokeErrorReport(action, new Error(ErrCode.ERROR_CLIENT_NET_OFFLINE));
                    }
                    return;
                }
                //将结果显示在搜索结果列表界面(Core上面)
                showListToCore();
            }

        }, throwable -> {
            Logger.d(TAG, "throwable " + throwable);
            invokeErrorReport(action, throwable);
        });
        //如果直接播放,则不需要传递给Core进行展示
        addRxAction(action, subscribe);
    }

    /**
     * 执行搜索，并将结果反馈给core
     *
     * @param operation
     * @param keyword
     */
    private void getSearchDataV2(Operation operation, String keyword) {
        Observable<SearchResult> searchResultObservable;
        if (hasNet()) {
            searchResultObservable = TXZMusicDataSource.get().listSearch(keyword, 1);
        } else {
            searchResultObservable = TXZMusicDataSource.get().searchLocal(keyword);
        }
        Disposable disposable = searchResultObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(searchResult -> {
            mSearchResult = searchResult;
            mSearchResult.keyword = keyword;
            JSONArray jArr = new JSONArray();
            createJsonMix(jArr, SEARCH_SIZE, searchResult);
            displayShowData(jArr);
        }, throwable -> {
            displayShowData(new JSONArray());
        });
        DisposableManager.get().add("getSearchDataV2", disposable);
    }


    private boolean hasNet() {
        return NetworkUtil.isNetworkAvailable(GlobalContext.get());
    }


    private void displayShowData(JSONArray showData) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        sendListToCore(jsonBuilder, showData);
    }

    private void sendListToCore(JSONBuilder jsonBuilder, JSONArray jsonArray) {
        // 如果支持v2版本就发v2的信息
        if (TXZAppUtils.getCoreVerCode() >= 264) {
            jsonBuilder.put("list", jsonArray.toString());
            Logger.d(TAG, " send:" + jsonBuilder.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist.v2", jsonBuilder.toBytes(), null);
        } else {
            Logger.d(TAG, " send:v1:" + jsonArray.toString());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncmusiclist", jsonArray.toString().getBytes(), null);
        }
    }

    /**
     * @param jsonArray 发送给Core来展示的数据
     * @param maxLength 最大展示的条数
     */
    private void createJsonMix(JSONArray jsonArray, int maxLength, SearchResult responseSearch) {
        int length = maxLength;
        if (responseSearch.returnType == SearchResult.TYPE_MIX && CollectionUtils.isNotEmpty(responseSearch.arrMix)) {
            if (responseSearch.arrMix.size() < maxLength) {
                length = responseSearch.arrMix.size();
            }

            for (int i = 0; i < length; i++) {

                SearchResult.Mix baseAudio = responseSearch.arrMix.get(i);
                if (null == baseAudio) {
                    continue;
                }
                JSONObject jsonObject = new JSONObject();
                if (baseAudio.album != null) {
                    Album album = baseAudio.album;
//                    DBManager.getInstance().insertAlbumIfNotExist(album);
                    jsonObject = getAlbumJsonObject(album, responseSearch);
                } else if (baseAudio.audio != null) {
                    AudioV5 audio = baseAudio.audio;
                    jsonObject = getAudioJsonObject(audio, responseSearch);
                }
                jsonArray.put(jsonObject);
            }
        }
    }

    private JSONObject getAudioJsonObject(AudioV5 audio, SearchResult responseSearch) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", audio.name);
            jsonObject.put("name", StringUtils.toString(audio.artist));
            jsonObject.put("id", audio.id);
            jsonObject.put("report", StringUtils.isEmpty(audio.announce) ? audio.name : audio.announce);
            if (responseSearch != null && responseSearch.playType == SearchResult.DELAYPLAY) {
                jsonObject.put("delayTime", responseSearch.delayTime);
            }

            Collection extraKey = audio.getExtraKey(Constant.AudioExtra.SEARCH_WAKE_UP);
            if (CollectionUtils.isNotEmpty(extraKey)) {
                JSONArray jsonArray = new JSONArray();
                for (Object item : extraKey) {
                    jsonArray.put(item);
                }
                jsonObject.put("wakeUp", jsonArray);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getAudioJsonObject: " + e.getMessage());
        }
        return jsonObject;
    }

    private JSONObject getAlbumJsonObject(Album album, SearchResult responseSearch) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", album.name);
            CharSequence extraKey = album.getExtraKey(Constant.AlbumExtra.SEARCH_SUBTITLE);
            if (!TextUtils.isEmpty(extraKey)) {
                jsonObject.put("name", extraKey);
            } else {
                jsonObject.put("name", StringUtils.toString(album
                        .arrArtistName));
            }
            jsonObject.put("id", album.id);
            jsonObject.put("report", album.getExtraKey(Constant.AlbumExtra.SEARCH_REPORT));
            Long listened = album.getExtraKey(Constant.AlbumExtra.SEARCH_LAST_LISTEN);
            jsonObject.put("listened", listened != null);
            jsonObject.put("novelStatus", album.getExtraKey(Constant.AlbumExtra.SEARCH_NOVEL_STATUS));
//            jsonObject.put("showDetail", AlbumUtils.isShowDetail(album));// 该字段用于声控界面二级交互,eg:我要听脱口秀;5.0版本产品说不要,说太智障
            if (responseSearch.playType == SearchResult.DELAYPLAY) {
                jsonObject.put("delayTime",
                        responseSearch.delayTime);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 搜索结果选择某首音频，
     *
     * @param operation 操作类型
     * @param audioV5   选择音频
     * @param audioList 若需要覆盖歌单，则传入audioList，(本地搜索xx歌手的歌传入audioList，播放列表变更为audioList)
     */
    private void choiceAudio(Operation operation, AudioV5 audioV5, @Nullable List<? extends AudioV5> audioList) {
        PlayHelper.get().playChoiceAudio(audioV5, audioList);
    }

    private void choiceAlbum(Operation operation, Album album) {
        PlayHelper.get().playChoiceAlbum(album);
    }

}
