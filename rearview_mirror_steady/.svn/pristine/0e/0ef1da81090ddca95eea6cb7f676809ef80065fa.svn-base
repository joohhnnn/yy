package com.txznet.music.data.source;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.LocalAudioDao;
import com.txznet.music.data.db.dao.PlayUrlInfoDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Category;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.MusicPageData;
import com.txznet.music.data.entity.PlayConfs;
import com.txznet.music.data.entity.PlayUrlInfo;
import com.txznet.music.data.entity.RadioPageData;
import com.txznet.music.data.entity.RecommendPageData;
import com.txznet.music.data.entity.SearchResult;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.http.api.txz.entity.TXZCategory;
import com.txznet.music.data.http.api.txz.entity.TXZSearchData;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqAudio;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPageData;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPlayConf;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqPreProcessing;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqSearch;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespPlayConf;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespSearch;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.exception.NetErrorException;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.Converter;
import com.txznet.music.helper.TXZUri;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.HttpUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.VolleyHttpReq;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class TXZMusicDataSource implements IMusicDataSource {

    private TXZMusicApi mTXZMusicApi = TXZMusicApiImpl.getDefault();

    private SoftReference<List<Category>> mCategoryCache;
    private LocalAudioDao mLocalAudioDao;
    private PlayUrlInfoDao mPlayUrlInfoDao;

    private static final class Holder {
        private static final TXZMusicDataSource INSTANCE = new TXZMusicDataSource();
    }

    private TXZMusicDataSource() {
        mLocalAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
        mPlayUrlInfoDao = DBUtils.getDatabase(GlobalContext.get()).getPlayUrlInfoDao();
    }

    public static TXZMusicDataSource get() {
        return Holder.INSTANCE;
    }

    // 获取所有的分类信息
    private Observable<List<Category>> listCategory() {
        return mTXZMusicApi.getCategory(0, true).map(categoryResp -> {
            if (categoryResp.errCode != 0) {
                throw new NetErrorException("request error:", new Error(categoryResp.errCode));
            }
            return categoryResp.arrCategory;
        }).map(txzCategories -> {
            List<Category> result = new ArrayList<>(txzCategories.size());
            for (TXZCategory category : txzCategories) {
                if (category.categoryId == 1) {
                    continue;
                }
                if (category.categoryId == 100000) {
                    if (category.arrChild != null) {
                        for (TXZCategory innerCategory : category.arrChild) {
                            Category item = new Category();
                            item.categoryId = innerCategory.categoryId;
                            item.name = innerCategory.desc;
                            item.logo = innerCategory.logo;
                            result.add(item);
                        }
                    }
                }
                Category item = new Category();
                item.categoryId = category.categoryId;
                item.name = category.desc;
                item.logo = category.logo;
                result.add(item);
            }
            return result;
        }).doOnNext(categoryList -> mCategoryCache = new SoftReference<>(categoryList));
    }

    private Observable<List<Category>> listCategoryWithCache() {
        Observable<List<Category>> obsFromCache = Observable.create(emitter -> {
            if (mCategoryCache != null && mCategoryCache.get() != null) {
                emitter.onNext(mCategoryCache.get());
            } else {
                emitter.onNext(new ArrayList<>());
            }
            emitter.onComplete();
        });
        return Observable.concat(obsFromCache, listCategory()).filter(categoryList -> categoryList != null && !categoryList.isEmpty()).firstElement().toObservable();


    }

    @Override
    public Observable<RecommendPageData> getRecommendPageData() {
        TXZReqPageData reqPageData = new TXZReqPageData();
        reqPageData.tabId = 10;
        return mTXZMusicApi.getPageData(reqPageData)
                .map(pageData -> {
                    RecommendPageData recommendPageData = new RecommendPageData();
                    recommendPageData.userRadioAlbum = pageData.userRadioAlbum;
                    recommendPageData.userMusicAlbum = pageData.userMusicAlbum;
                    recommendPageData.dailyRecAlbum = pageData.dailyRecAlbum;
                    recommendPageData.aiAlbum = pageData.aiAlbum;
                    recommendPageData.commRecAlbums = pageData.commRecAlbums;
                    recommendPageData.billboard = pageData.billboard;
                    return recommendPageData;
                }).onErrorReturn(throwable -> {
                    String cache = SharedPreferencesUtils.getRecPageCache();
                    if (cache != null) {
                        Logger.w(Constant.LOG_TAG_DB, "getRecommendPageData use cache");
                        return JsonHelper.fromJson(cache, RecommendPageData.class);
                    }
                    return null;
                }).doOnNext(pageData -> {
                    // 数据校验
                    if (pageData.userRadioAlbum != null
                            && pageData.userMusicAlbum != null
                            && pageData.dailyRecAlbum != null
                            && pageData.aiAlbum != null
                            && pageData.commRecAlbums != null
                            && pageData.billboard != null) {
                        SharedPreferencesUtils.setRecPageCache(JsonHelper.toJson(pageData));
                    }
                    if (pageData.aiAlbum != null && pageData.aiAlbum.logo != null) {
                        SharedPreferencesUtils.setAiRadioLogoUrl(pageData.aiAlbum.logo);
                    }

                    if (pageData.dailyRecAlbum != null && pageData.dailyRecAlbum.logo != null) {
                        SharedPreferencesUtils.setRecommendLogoUrl(pageData.dailyRecAlbum.logo);
                    }
                });
    }


    @Override
    public Observable<MusicPageData> getMusicPageData() {
        TXZReqPageData reqPageData = new TXZReqPageData();
        reqPageData.tabId = 20;
        return mTXZMusicApi.getPageData(reqPageData)
                .map(pageData -> {
                    MusicPageData musicPageData = new MusicPageData();
                    musicPageData.choiceData = pageData.choice;
                    musicPageData.categoryData = pageData.arrMusicCategory;
                    return musicPageData;
                }).onErrorReturn(throwable -> {
                    String cache = SharedPreferencesUtils.getMusicPageCache();
                    if (cache != null) {
                        Logger.w(Constant.LOG_TAG_DB, "getMusicPageData use cache");
                        return JsonHelper.fromJson(cache, MusicPageData.class);
                    }
                    return null;
                }).doOnNext(pageData -> {
                    // 数据校验
                    if (pageData.choiceData != null
                            && pageData.categoryData != null) {
                        SharedPreferencesUtils.setMusicPageCache(JsonHelper.toJson(pageData));
                    }
                });
    }

    @Override
    public Observable<RadioPageData> getRadioPageData() {
        TXZReqPageData reqPageData = new TXZReqPageData();
        reqPageData.tabId = 30;
        return mTXZMusicApi.getPageData(reqPageData)
                .map(pageData -> {
                    RadioPageData radioPageData = new RadioPageData();
                    radioPageData.billboard = pageData.billboard;
                    radioPageData.categoryList = pageData.arrRadioCategory;
                    radioPageData.choiceList = pageData.arrChoice;
                    return radioPageData;
                }).onErrorReturn(throwable -> {
                    String cache = SharedPreferencesUtils.getRadioPageCache();
                    if (cache != null) {
                        Logger.w(Constant.LOG_TAG_DB, "getRadioPageData use cache");
                        return JsonHelper.fromJson(cache, RadioPageData.class);
                    }
                    return null;
                }).doOnNext(pageData -> {
                    // 数据校验
                    if (pageData.billboard != null
                            && pageData.categoryList != null
                            && pageData.choiceList != null) {
                        SharedPreferencesUtils.setRadioPageCache(JsonHelper.toJson(pageData));
                    }
                });
    }

    @Override
    public Observable<List<Category>> listMusicCategory() {
        return listCategoryWithCache().map(categoryList -> {
            List<Category> result = new ArrayList<>();
            for (Category category : categoryList) {
                if (category.categoryId < 200000) {
                    result.add(category);
                }
            }
            return result;
        });
    }

    @Override
    public Observable<List<Category>> listRadioCategory() {
        return listCategoryWithCache().map(categoryList -> {
            List<Category> result = new ArrayList<>();
            for (Category category : categoryList) {
                if (category.categoryId >= 200000) {
                    result.add(category);
                }
            }
            return result;
        });
    }

    @Override
    public Observable<List<Album>> listAlbum(int sid, Category category, int pageId, int pagesize) {
        return mTXZMusicApi.getAlbum(sid, category.categoryId, pageId, pagesize).map(txzRespAlbum -> txzRespAlbum.arrAlbum);
    }

    @Override
    public Observable<SearchResult> listSearch(final String keywordJson, int searchType) {
        //同行者根据自己需要获取@Param keywordJson 这里面的东西
        //转换成需要的参数
        Logger.d(Constant.LOG_TAG_SEARCH, "listSearch:" + searchType + "," + keywordJson);
        TXZReqSearch convert = new Converter() {

            @Override
            public TXZReqSearch convert(String json) {
                //全部转换，暂定，可以采用裁剪的方式进行剥离
                return createSearchObj(json);
            }
        }.convert(keywordJson);

        return mTXZMusicApi.findSearch(convert)
                .flatMap((Function<TXZRespSearch, ObservableSource<TXZRespSearch>>) txzRespSearch -> {
                    if (txzRespSearch.arrMix.size() > 0) {
                        return Observable.just(txzRespSearch);
                    }
                    return Observable.empty();
                })
                .observeOn(Schedulers.io())
                .switchIfEmpty(searchLocal(convert.audioName, convert.artist, convert.albumName))
                .map(txzRespSearch -> {
                    Logger.d(Constant.LOG_TAG_SEARCH, "apply->" + txzRespSearch.toString());
                    SearchResult searchResult = new SearchResult();
                    if (txzRespSearch.returnType == TXZRespSearch.TYPE_MIX) {
                        AudioConverts.convert2List(txzRespSearch.arrMix, searchResult.arrMix, txzSearchData -> {
                            SearchResult.Mix mix = new SearchResult.Mix();
                            mix.album = AlbumConverts.convert2Album(txzSearchData.album);
                            mix.audio = AudioConverts.convert2Audio(txzSearchData.audio);
                            return mix;
                        });
                    }
                    searchResult.delayTime = txzRespSearch.delayTime;
                    searchResult.playIndex = txzRespSearch.playIndex;
                    searchResult.playType = txzRespSearch.playType;
                    searchResult.returnType = txzRespSearch.returnType;

                    return searchResult;
                });
    }

    public Observable<SearchResult> searchLocal(String keywordJson) {
        TXZReqSearch convert = new Converter() {

            @Override
            public TXZReqSearch convert(String json) {
                //全部转换，暂定，可以采用裁剪的方式进行剥离
                return createSearchObj(json);
            }
        }.convert(keywordJson);
        return searchLocal(convert.audioName, convert.artist, convert.albumName).map(txzRespSearch -> {
            Logger.d(Constant.LOG_TAG_SEARCH, "apply->" + txzRespSearch.toString());
            SearchResult searchResult = new SearchResult();
            if (txzRespSearch.returnType == TXZRespSearch.TYPE_MIX) {
                AudioConverts.convert2List(txzRespSearch.arrMix, searchResult.arrMix, txzSearchData -> {
                    SearchResult.Mix mix = new SearchResult.Mix();
                    mix.album = AlbumConverts.convert2Album(txzSearchData.album);
                    mix.audio = AudioConverts.convert2Audio(txzSearchData.audio);
                    return mix;
                });
            }
            searchResult.delayTime = txzRespSearch.delayTime;
            searchResult.playIndex = txzRespSearch.playIndex;
            searchResult.playType = txzRespSearch.playType;
            searchResult.returnType = txzRespSearch.returnType;

            return searchResult;
        });
    }


    public Observable<TXZRespSearch> searchLocal(final String audioName, final String artists, final String albumName) {

        return Observable.create(e -> {
            List<LocalAudio> byCondition = mLocalAudioDao.findBySql(LocalAudio.getSearchQuery(audioName, artists, albumName));

            TXZRespSearch txzRespSearch = new TXZRespSearch();
            txzRespSearch.arrMix = AudioConverts.convert2List(byCondition, localAudio -> {
                TXZSearchData searchData = new TXZSearchData();
                searchData.audio = AudioConverts.convert2TXZAudio(AudioConverts.convert2MediaAudio(localAudio));
                return searchData;
            });
            txzRespSearch.returnType = TXZRespSearch.TYPE_MIX;
            // xx歌手的歌，交互调整为自动播放
            if (audioName == null && txzRespSearch.arrMix.size() > 0) {
                txzRespSearch.playType = TXZRespSearch.GOPLAY;
            }
            e.onNext(txzRespSearch);
            e.onComplete();
        });
    }

    @Override
    public Observable<PlayConfs> getPlayConf() {
        return mTXZMusicApi.getPlayConfig(new TXZReqPlayConf())
                .map((Function<? super TXZRespPlayConf, PlayConfs>) txzRespPlayConf -> txzRespPlayConf)
                .observeOn(Schedulers.io())
                .doOnNext(txzRespPlayConf -> {
                    Logger.d(Constant.LOG_TAG_SPLASH, "write cache=" + txzRespPlayConf.launchPage);
                    if (txzRespPlayConf.launchPage != null) {
                        for (PlayConfs.FlashPage flashPage : txzRespPlayConf.launchPage) {
                            if (!TextUtils.isEmpty(flashPage.url)) {
                                File jpgFile = new File(Environment.getExternalStorageDirectory(), "txz/audio/splash/" + flashPage.hashCode() + ".jpg");
                                long len = HttpUtils.getFileLength(flashPage.url);
                                if (!jpgFile.exists() || jpgFile.length() != len) {
                                    if (!jpgFile.getParentFile().exists()) {
                                        jpgFile.getParentFile().mkdirs();
                                    }
                                    int result = HttpUtils.downloadFile(flashPage.url, jpgFile.getAbsolutePath());
                                    if (result == 0) {
                                        flashPage.url = jpgFile.getAbsolutePath();
                                        Logger.d(Constant.LOG_TAG_SPLASH, "write cache complete, path=" + flashPage.url);
                                    }
                                } else {
                                    flashPage.url = jpgFile.getAbsolutePath();
                                }
                            }
                        }
                    }

                    Logger.d(Constant.LOG_TAG_SPLASH, "write icon cache=" + txzRespPlayConf.launchLogo);
                    if (txzRespPlayConf.launchLogo != null) {
                        List<String> localLogo = new ArrayList<>(txzRespPlayConf.launchLogo.size());
                        for (String logoUrl : txzRespPlayConf.launchLogo) {
                            if (!TextUtils.isEmpty(logoUrl)) {
                                File jpgFile = new File(Environment.getExternalStorageDirectory(), "txz/audio/splash_icon/" + logoUrl.hashCode() + ".jpg");
                                long len = HttpUtils.getFileLength(logoUrl);
                                if (!jpgFile.exists() || jpgFile.length() != len) {
                                    if (!jpgFile.getParentFile().exists()) {
                                        jpgFile.getParentFile().mkdirs();
                                    }
                                    int result = HttpUtils.downloadFile(logoUrl, jpgFile.getAbsolutePath());
                                    if (result == 0) {
                                        logoUrl = jpgFile.getAbsolutePath();
                                        Logger.d(Constant.LOG_TAG_SPLASH, "write icon cache complete, path=" + logoUrl);
                                    }
                                } else {
                                    logoUrl = jpgFile.getAbsolutePath();
                                }
                                localLogo.add(logoUrl);
                            }
                        }
                        txzRespPlayConf.launchLogo = localLogo;
                    }


                    String cache = JsonHelper.toJson(txzRespPlayConf);
                    SharedPreferencesUtils.setConfig(cache);
                    AudioUtils.resetPlayConfs(txzRespPlayConf);

                    // 缓存清理策略
                    File cacheDir = new File(Environment.getExternalStorageDirectory(), "txz/audio/splash/");
                    if (cacheDir.exists()) {
                        File[] childs = cacheDir.listFiles();
                        if (childs != null) {
                            for (File file : childs) {
                                // 删除三天前的缓存
                                if (file.lastModified() < TimeManager.getInstance().getTimeMillis() - 1000 * 60 * 60 * 24 * 3) {
                                    Logger.d(Constant.LOG_TAG_SPLASH, "delete cache=" + file);
                                    file.delete();
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public Observable<List<AudioV5>> listAudios(Album album, AudioV5 audioV5, String audioId, int pagesize) {
        return listAudios(album, audioV5, audioId, pagesize, false);
    }

    @Override
    public Observable<List<AudioV5>> listAudios(Album album, AudioV5 audioV5, String audioId, int pagesize, boolean fromFirst) {
        TXZReqAudio reqAlbumAudio = new TXZReqAudio();
        reqAlbumAudio.sid = album.sid;
        reqAlbumAudio.id = album.id;
        int order = 0;
        if (audioId != null) {
            String[] splitArr = audioId.split(TXZAudio.splitChar);
            if (splitArr.length == 3) {
                order = Integer.parseInt(splitArr[0]);// 只能为0为向下 或1为向上
                reqAlbumAudio.audioSid = Integer.parseInt(splitArr[1]);
                reqAlbumAudio.audioId = Long.parseLong(splitArr[2]);
            } else if (splitArr.length == 4) {
                order = Integer.parseInt(splitArr[0]);// 只能为0为向下 或1为向上
                reqAlbumAudio.audioSid = Integer.parseInt(splitArr[1]);
                reqAlbumAudio.audioId = Long.parseLong(splitArr[2]);
                reqAlbumAudio.txz_album_id = Long.parseLong(splitArr[3]);
            }
        }
        String svrData = album.getExtraKey(Constant.AlbumExtra.SVR_DATA);
        if (TextUtils.isEmpty(svrData) && audioV5 != null) {
            svrData = audioV5.getExtraKey(Constant.AudioExtra.SVR_DATA);
        }
        reqAlbumAudio.svrData = svrData;
        reqAlbumAudio.up = order;

        int pageCount = album.getExtraKey(Constant.AlbumExtra.PAGE_SIZE, pagesize);
        if (pageCount < 1) {
            pageCount = Configuration.DefVal.PAGE_COUNT;
        }
        reqAlbumAudio.offset = pageCount;
        if (fromFirst) {
            reqAlbumAudio.type = 2;
        }
        return mTXZMusicApi.getAudios(reqAlbumAudio).map(txzRespAudio -> {
            if (txzRespAudio.field != null) {
                album.albumType = txzRespAudio.field;
            }
            album.setExtraKey(Constant.AlbumExtra.TOTAL_NUM, txzRespAudio.totalNum);
            List<TXZAudio> audioList = txzRespAudio.arrAudio;
            if (txzRespAudio.needSort()) {
                Collections.sort(audioList, (o1, o2) -> {
                    if (o1.score > o2.score) {
                        return -1;
                    } else if (o1.score < o2.score) {
                        return 1;
                    }
                    return 0;
                });
            }
            List<AudioV5> audioV5s = new ArrayList<>();
            AudioConverts.convert2List(audioList, audioV5s, AudioConverts::convert2Audio);
            return audioV5s;
        });
    }

    @Override
    public Observable<PlayUrlInfo> getAudioPlayUrls(@NonNull AudioV5 audio) {
        return getPlayUrlInfoFromLocal(audio.sid, audio.id).flatMap((Function<PlayUrlInfo, ObservableSource<PlayUrlInfo>>) playUrlInfo -> {
            if (playUrlInfo != null && playUrlInfo != PlayUrlInfo.NONE) {
                return Observable.just(playUrlInfo);
            }
            return Observable.empty();
        }).switchIfEmpty(getPlayUrlInfoFromNet(audio));
    }

    @Override
    public Observable<Long> getServerTime() {
        return mTXZMusicApi.getSeverTime().map(txzRespGetTime -> txzRespGetTime.time);
    }

    @Override
    public Observable<String> getLyric(@NonNull AudioV5 audio) {
        return mTXZMusicApi.getLyricData(audio).flatMap(txzRespLyricData -> Observable.create(emitter -> {
            if (txzRespLyricData.code == 0) {
                HttpUtils.sendGetRequest(txzRespLyricData.msg, null, new HttpUtils.HttpCallbackListener() {
                    @Override
                    public void onSuccess(String response) {
                        if (emitter.isDisposed()) {
                            return;
                        }
                        emitter.onNext(response);
                        emitter.onComplete();
                    }

                    @Override
                    public void onError(int errorCode) {
                        if (emitter.isDisposed()) {
                            return;
                        }
                        emitter.onError(new Error(ErrCode.ERROR_CLIENT_NOT_FOUND));
                    }
                });
            } else {
                emitter.onNext("");
                emitter.onComplete();
            }
        }));
    }

    private Observable<PlayUrlInfo> getPlayUrlInfoFromLocal(int sid, long id) {
        return Observable.create(emitter -> {
            PlayUrlInfo playUrlInfo = mPlayUrlInfoDao.findBySidAndId(sid, id);
            if (playUrlInfo == null || TimeManager.getInstance().isTimeout(playUrlInfo.iExpTime)) {
                playUrlInfo = PlayUrlInfo.NONE;
            }
            emitter.onNext(playUrlInfo);
            emitter.onComplete();
        });
    }

    private Observable<PlayUrlInfo> getPlayUrlInfoFromNet(AudioV5 audio) {
        TXZUri uri = TXZUri.parse(audio.sourceUrl);
        return Observable.create((ObservableOnSubscribe<String>) emitter -> {
            VolleyHttpReq req = new VolleyHttpReq(uri.progressUrl, resp -> {
                emitter.onNext(resp);
                emitter.onComplete();
            }, uri.processIsPost == 1, uri.processHeader);
            req.doRequest();
        }).flatMap((Function<String, ObservableSource<PlayUrlInfo>>) s -> {
            TXZReqPreProcessing reqPreProcessing = new TXZReqPreProcessing();
            reqPreProcessing.processingContent = s;
            reqPreProcessing.strProcessingUrl = uri.progressUrl;
            reqPreProcessing.strDownloadUrl = uri.downloadUrl;
            reqPreProcessing.sid = audio.sid;
            reqPreProcessing.audioId = audio.id;
            return mTXZMusicApi.getTXZPlayUrl(reqPreProcessing).map(txzRespPreProcessing -> txzRespPreProcessing);
        }).retry(1000, throwable -> true).observeOn(Schedulers.io()).doOnNext(playUrlInfo -> {
            playUrlInfo.sid = audio.sid;
            mPlayUrlInfoDao.saveOrUpdate(playUrlInfo);
        });
    }

    public static TXZReqSearch createSearchObj(String soundData) {
        final TXZReqSearch reqData = new TXZReqSearch();
        JSONBuilder jsonBuilder = new JSONBuilder(soundData);
        reqData.audioName = jsonBuilder.getVal("title", String.class);
        reqData.albumName = jsonBuilder.getVal("album", String.class);
        reqData.artist = StringUtils.toString(jsonBuilder.getVal("artist", String[].class));
        reqData.category = StringUtils.toString(jsonBuilder.getVal("keywords", String[].class));
        // FIXME: 2019/3/18 后台category只认空格分隔符
        reqData.category = com.txznet.music.util.StringUtils.stringFilter(reqData.category);
        reqData.field = jsonBuilder.getVal("field", int.class, 0);// 1。表示歌曲，2.表示电台
        reqData.subCategory = jsonBuilder.getVal("subcategory", String.class);
        reqData.text = jsonBuilder.getVal("text", String.class);


        // 后台说:只认category
        if (StringUtils.isNotEmpty(reqData.subCategory)) {
            reqData.category = reqData.subCategory;
        }

        if (StringUtils.isEmpty(reqData.category)) {
            reqData.category = jsonBuilder.getVal("category", String.class);
        }

        return reqData;
    }
}
