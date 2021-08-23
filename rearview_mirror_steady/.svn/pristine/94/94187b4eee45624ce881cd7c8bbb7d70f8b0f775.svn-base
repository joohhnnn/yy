package com.txznet.music.data.http.api.txz;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.txznet.comm.err.Error;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.api.txz.entity.req.Location;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqAudio;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqError;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFake;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavourOperation;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPageData;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPlayConf;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPreProcessing;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqSearch;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespAlbum;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespAudio;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespCategory;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFake;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFavour;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespGetTime;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespHistory;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespLyricData;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPageData;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPlayConf;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPreProcessing;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespSearch;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespTag;
import com.txznet.music.helper.TXZNetRequest;
import com.txznet.music.service.impl.NetCommand;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.ProgramUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

import static com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour.ALBUM_TYPE;
import static com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour.AUDIO_TYPE;

public class TXZMusicApiImpl implements TXZMusicApi {

    private static TXZMusicApiImpl sInstance = new TXZMusicApiImpl();

    public static TXZMusicApiImpl getDefault() {
        return sInstance;
    }

    @Override
    public Observable<TXZRespPageData> getPageData(TXZReqPageData reqPageData) {
        return Observable.create(emitter -> {
            String reqJson = JsonHelper.toJson(reqPageData);
            TXZNetRequest.get().sendSeqRequestToCore(GET_PAGE_DATA, reqJson.getBytes(), 1000 * 10, new TXZNetRequest.RequestCallBack<TXZRespPageData>(TXZRespPageData.class) {

                @Override
                public void onResponse(TXZRespPageData data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed() || error == null) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespCategory> getCategory(final int categoryId, final boolean bAll) {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("categoryId", categoryId);
            jBuilder.put("bAll", bAll ? 1 : 0);
            jBuilder.put("arrApp", new ArrayList<>());
            jBuilder.put("version", TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_CATEGORY_VERSION, Configuration.DefVal.CATEGORY_VERSION));
            jBuilder.put("logoTag", 0);
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_CATEGORY, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespCategory>(TXZRespCategory.class) {

                @Override
                public void onResponse(TXZRespCategory data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespAlbum> getAlbum(final int sid, final long categoryId, final int pageId, Integer offset) {
        return Observable.create((ObservableOnSubscribe<TXZRespAlbum>) emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("sid", sid);
            jBuilder.put("categoryId", categoryId);
            jBuilder.put("pageId", pageId);
            jBuilder.put("offset", offset == null ? Configuration.DefVal.PAGE_COUNT : offset); // 一页请求几个数据
            jBuilder.put("version", TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_ALBUM_LIST_VERSION, Configuration.DefVal.ALBUM_LIST_VERSION));
            jBuilder.put("orderType", 1); // 排序方式
            jBuilder.put("arrApp", new ArrayList<>());
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_ALBUM_LIST, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespAlbum>(TXZRespAlbum.class) {

                @Override
                public void onResponse(TXZRespAlbum data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        }).doOnNext(new Consumer<TXZRespAlbum>() {
            @Override
            public void accept(TXZRespAlbum txzRespAlbum) {
                if (BuildConfig.DEBUG) {
                    Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + "/album/list,req:categoryId=" + categoryId + ", pageId = " + pageId + ",response:" + txzRespAlbum.categoryId);
                }

                txzRespAlbum.categoryId = categoryId + "";
            }
        });
    }

    @Override
    public Observable<TXZRespAudio> getAudios(@NonNull final TXZReqAudio reqAudioParam) {
        return Observable.create(emitter -> {
            if (emitter.isDisposed()) {
                return;
            }
            String reqJson = JsonHelper.toJson(reqAudioParam);
            TXZNetRequest.get().sendSeqRequestToCore(GET_ALBUM_AUDIO, reqJson.getBytes(), 10 * 1000, new TXZNetRequest.RequestCallBack<TXZRespAudio>(TXZRespAudio.class) {
                @Override
                public void onResponse(TXZRespAudio data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespTag> ctrlMusicTag(@NonNull final String action, @Nullable final String[] tagIds) {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("action", action);
            if (tagIds != null) {
                jBuilder.put("tagIds", tagIds);
            }
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_MUSIC_INTEREST_TAG, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespTag>(TXZRespTag.class) {
                @Override
                public void onResponse(TXZRespTag data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespTag> ctrlFmTag(@NonNull final String action, @Nullable final String[] tagIds) {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("action", action);
            if (tagIds != null) {
                jBuilder.put("tagIds", tagIds);
            }
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_FM_INTEREST_TAG, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespTag>(TXZRespTag.class) {
                @Override
                public void onResponse(TXZRespTag data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespPlayConf> getPlayConfig(@NonNull TXZReqPlayConf reqCheck) {
        return Observable.create(emitter -> {
            String reqJson = JsonHelper.toJson(reqCheck);
            TXZNetRequest.get().sendSeqRequestToCore(GET_CONFIG, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespPlayConf>(TXZRespPlayConf.class) {
                @Override
                public void onResponse(TXZRespPlayConf data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespHistory> findHistory(final int type, final int page, final int offset) {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("type", type);
            jBuilder.put("page", page);
            jBuilder.put("offset", offset);
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_HISTORY, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespHistory>(TXZRespHistory.class) {
                @Override
                public void onResponse(TXZRespHistory data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespSearch> findSearch(@NonNull final TXZReqSearch reqSearch) {
        return Observable.create(emitter -> {
            String reqJson = JsonHelper.toJson(reqSearch);
            TXZNetRequest.get().sendSeqRequestToCore(GET_SEARCH, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespSearch>(TXZRespSearch.class) {
                @Override
                public void onResponse(TXZRespSearch data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespPreProcessing> getTXZPlayUrl(@NonNull TXZReqPreProcessing reqPreProcessing) {
        return Observable.create(emitter -> {
            String reqJson = JsonHelper.toJson(reqPreProcessing);
            TXZNetRequest.get().sendSeqRequestToCore(GET_PROCESSING, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespPreProcessing>(TXZRespPreProcessing.class) {
                @Override
                public void onResponse(TXZRespPreProcessing data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespFavour> getFavours(TXZReqFavour reqdata) {
        return Observable.create(emitter -> {
            reqdata.storeType = AUDIO_TYPE;
            String reqJson = JsonHelper.toJson(reqdata);
            TXZNetRequest.get().sendSeqRequestToCore(GET_FAVOUR_LIST, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespFavour>(TXZRespFavour.class) {
                @Override
                public void onResponse(TXZRespFavour data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    if (!emitter.isDisposed()) {
                        emitter.onError(error);
                    }
                }
            });
        });
    }

    @Override
    public Observable<TXZReqFavourOperation> favourAudio(TXZReqFavourOperation favour) {
        return null;
    }

    @Override
    public Observable<TXZReqFavourOperation> subscribeAlbum(TXZReqFavourOperation sendData) {
        return Observable.create(emitter -> {

            String reqJson = JsonHelper.toJson(sendData);

            //
            TXZNetRequest.get().sendSeqRequestToCore(GET_UPON_FAVOUR, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZReqFavourOperation>(TXZReqFavourOperation.class) {
                @Override
                public void onResponse(TXZReqFavourOperation data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                    emitter.onComplete();
                }
            });
        });
    }


    @Override
    public Observable<TXZRespFavour> getSubscribe(TXZReqFavour reqdata) {
        return Observable.create(emitter -> {
            reqdata.storeType = ALBUM_TYPE;
            String reqJson = JsonHelper.toJson(reqdata);
            TXZNetRequest.get().sendSeqRequestToCore(GET_FAVOUR_LIST, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespFavour>(TXZRespFavour.class) {
                @Override
                public void onResponse(TXZRespFavour data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    if (!emitter.isDisposed()) {
                        emitter.onError(error);
                    }
                }
            });
        });
    }

    @Override
    public Observable<TXZRespGetTime> getSeverTime() {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_TIME, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespGetTime>(TXZRespGetTime.class) {
                @Override
                public void onResponse(TXZRespGetTime data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<TXZRespLyricData> getLyricData(AudioV5 audioV5) {
        return Observable.create(emitter -> {
            JSONBuilder jBuilder = new JSONBuilder();
            jBuilder.put("audioId", audioV5.id);
            jBuilder.put("sid", audioV5.sid);
            jBuilder.put("cliType", ProgramUtils.isProgram() ? 1 : 2);
            TXZNetRequest.get().sendSeqRequestToCore(GET_LYRIC, jBuilder.toBytes(), new TXZNetRequest.RequestCallBack<TXZRespLyricData>(TXZRespLyricData.class) {
                @Override
                public void onResponse(TXZRespLyricData data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public Observable<PushResponse> getPushData(String url, Location location) {
        return Observable.create(emitter -> {
//            JSONBuilder jBuilder = new JSONBuilder();
//            jBuilder.put("id", audioV5.id);
//            jBuilder.put("sid", audioV5.sid);

            String s = JsonHelper.toJson(location);

            TXZNetRequest.get().sendSeqRequestToCore(url, s.getBytes(), new TXZNetRequest.RequestCallBack<PushResponse>(PushResponse.class) {
                @Override
                public void onResponse(PushResponse data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }


    @Override
    public Observable<TXZRespFake> fakeRequest(TXZReqFake reqFake) {
        return Observable.create(emitter -> {
            String reqJson = JsonHelper.toJson(reqFake);
            TXZNetRequest.get().sendSeqRequestToCore(GET_FAKE_SEARCH, reqJson.getBytes(), new TXZNetRequest.RequestCallBack<TXZRespFake>(TXZRespFake.class) {
                @Override
                public void onResponse(TXZRespFake data) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onNext(data);
                    emitter.onComplete();
                }

                @Override
                public void onError(String cmd, Error error) {
                    if (emitter.isDisposed()) {
                        return;
                    }
                    emitter.onError(error);
                }
            });
        });
    }

    @Override
    public void getWxLinkQRCode() {
        NetCommand.getInstance().request(NetCommand.mStrReqQrcodeCMD, null);
    }

    @Override
    public void reportError(TXZReqError txzReqError) {
        String json = JsonHelper.toJson(txzReqError);
        TXZNetRequest.get().sendSeqRequestToCore(GET_REPORT_ERROR, json.getBytes(), null);
    }
}
