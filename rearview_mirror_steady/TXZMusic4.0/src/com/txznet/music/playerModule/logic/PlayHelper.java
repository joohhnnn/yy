package com.txznet.music.playerModule.logic;

import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.AlbumLogic;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayListData;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.dao.DaoManager;
import com.txznet.music.data.entity.Category;
import com.txznet.music.data.http.req.ReqCategory;
import com.txznet.music.data.http.resp.Homepage;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.localModule.LocalAudioDataSource;
import com.txznet.music.net.NetCacheManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.utils.AudioUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.Objects;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/1.
 */

public class PlayHelper {

    private static final String TAG = "PlayHelper:";

    private static final int PLAY_OK = 0;
    private static final int PLAY_ALREADY = 1;
    private static final int PLAY_FAIL = -1;


    private PlayHelper() {

    }


    public static void playRadio(int screen, final EnumState.Operation operation) {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            int i = playLastRadio(operation);
            if (i == PLAY_FAIL) {//???????????????????????????(????????????????????????)
                if (!playHistoryRadio(operation, true)) {//??????????????????(???????????????????????????)
                    //????????????????????????????????????
                    playRandomRadio(screen, operation);
                }
            } else if (i == PLAY_ALREADY) {
                TtsUtil.speakText(Constant.RS_VOICE_SPEAK_PLAY_RADIO_ALREADY);
            }
        } else {
            TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
        }
    }

    /**
     * //???????????????????????????(????????????????????????)
     *
     * @param operation
     * @return 0 ????????????????????? 1 ???????????????????????????????????????????????? -1 ??????????????????
     */
    private static int playLastRadio(EnumState.Operation operation) {
        if (null != PlayInfoManager.getInstance().getCurrentAudio() && !Utils.isSong(PlayInfoManager.getInstance().getCurrentAudio().getSid())) {
            int state = PlayEngineFactory.getEngine().getState();
            if (state == PlayerInfo.PLAYER_STATUS_PAUSE || state == PlayerInfo.PLAYER_STATUS_RELEASE) {
                PlayEngineFactory.getEngine().playOrPause(operation);
                return PLAY_OK;//
            } else {
                return PLAY_ALREADY;
            }
        }
        return PLAY_FAIL;
    }


    /**
     * //??????????????????(???????????????????????????)
     *
     * @param operation
     * @return
     */
    public static boolean playHistoryRadio(EnumState.Operation operation, boolean needPlay) {
        List<HistoryData> albumHistory = DBManager.getInstance().findAlbumHistory();
        if (CollectionUtils.isEmpty(albumHistory)) {
            return false;
        }
        HistoryData historyData = albumHistory.get(0);
        Album album = historyData.getAlbum();
        Audio audio = historyData.getAudio();

        if (album == null || audio == null) {
            LogUtil.d(TAG, "????????????:" + "occur error:" + (audio != null ? audio.toString() : "null"));
            return false;
        }

        Logger.d(TAG, album);
        Logger.d(TAG, audio);
        if (album.getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
            //??????????????????
            if (album.getpSid() == 0 || album.getPid() == 0) {
            } else {
                AlbumLogic.getInstance().getCarFmLogic(album, needPlay);
                return true;
            }
        }

//        List<Audio> audiosByAlbumId = DBManager.getInstance().findAudiosByAlbumId(album.getId());
//        if (com.txznet.music.utils.CollectionUtils.isEmpty(audiosByAlbumId)) {
//            LogUtil.e(TAG, "title:" + album.toString());
//            return false;
//        }
        AlbumEngine.getInstance().playAlbumWithAudio(operation, PlayInfoManager.DATA_ALBUM, album, audio, false, needPlay);

//        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.auto, audiosByAlbumId, album, audiosByAlbumId.indexOf(audio), PlayInfoManager.DATA_ALBUM);
//        if (needPlay) {
//            PlayEngineFactory.getEngine().playOrPause(operation);
//        }
        return true;

    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param operation
     * @return
     */
    private static boolean playRandomRadio(final int screen, EnumState.Operation operation) {
        final ReqCategory reqCategory = new ReqCategory();
        reqCategory.setbAll(1);
        NetCacheManager.getInstance().requestCache(Constant.GET_CATEGORY, reqCategory, true, new RequestCallBack<String>(String.class) {
            @Override
            public void onError(String cmd, Error error) {
                //???????????????????????????
                if (!operation.equals(EnumState.Operation.sound)) {
                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                }
                LogUtil.e(TAG + " request category error:" + error.getErrorCode());
            }

            @Override
            public void onResponse(String data) {
                final Homepage<Category> homepage = JsonHelper.toObject(data, new TypeToken<Homepage<Category>>() {
                }.getType());

                if (homepage == null || CollectionUtils.isEmpty(homepage.getArrCategory())) {
                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_PARSE_ERROR);
                    LogUtil.e(TAG + " request category error:" + "result:null");
                    return;
                }


                long categoryID = 0;

                for (Category category : homepage.getArrCategory()) {
                    if (category.getCategoryId() != 100000 && category.getCategoryId() != 1) {
                        categoryID = category.getArrChild().get(0).getCategoryId();//?
                        break;
                    }
                }
                ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
                reqSearchAlbum.setPageId(1);
                reqSearchAlbum.setCategoryId(categoryID);
                NetCacheManager.getInstance().requestCache(Constant.GET_SEARCH_LIST, reqSearchAlbum, false, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {
                    @Override
                    public void onResponse(ResponseSearchAlbum data) {
                        if (data == null || CollectionUtils.isEmpty(data.getArrAlbum())) {
                            TtsUtil.speakText(Constant.RS_VOICE_SPEAK_PARSE_ERROR);
                            LogUtil.e(TAG + " request album error:" + "result:null");
                            return;
                        }
                        Album album = data.getArrAlbum().get(0);
                        //??????FM ???????????????
                        //categorys -???categorys[0]->Constant.GET_SEARCH_LIST->??????
                        if (album.getAlbumType() == Album.ALBUM_TYPE_CAR_FM) {
                            ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
                            reqSearchAlbum.setPageId(1);
                            reqSearchAlbum.setCategoryId(album.getId());

                            NetCacheManager.getInstance().requestCache(Constant.GET_SEARCH_LIST, reqSearchAlbum, false, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {

                                @Override
                                public void onError(String cmd, Error error) {
                                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                                    LogUtil.e(TAG + " request album error:" + error.getErrorCode());
                                }

                                @Override
                                public void onResponse(ResponseSearchAlbum data) {
                                    List<Album> albums = new ArrayList<Album>();
                                    if (com.txznet.music.utils.CollectionUtils.isNotEmpty(data.getArrAlbum())) {
                                        for (Album childAlbum : data.getArrAlbum()) {
                                            childAlbum.setParentAlbum(album);
                                            albums.add(childAlbum);
                                        }
                                    }
                                    PlayInfoManager.setCarFmAlbums(albums);
                                    Album needPlayAlbum = CarFmUtils.getInstance().getNeedPlayAlbum(albums);
                                    AlbumEngine.getInstance().playAlbum(PlayInfoManager.DATA_CHEZHU_FM, EnumState.Operation.manual, needPlayAlbum, needPlayAlbum.getCategoryId(), null);
                                }
                            });
                        } else {
                            AlbumEngine.getInstance().playAlbum(screen, EnumState.Operation.manual, album, album.getCategoryId(), null);
                        }
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                        LogUtil.e(TAG + " request album error:" + error.getErrorCode());
                    }
                });

            }
        });

        return true;
    }


    /**
     * ??????????????????????????????
     *
     * @return s????????????????????????
     */
    public static int playLastMusic(EnumState.Operation operation) {
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        Logger.d(TAG, "playLastMusic:" + Objects.getObj2String(currentAudio));
        if (null != currentAudio && Utils.isSong(currentAudio.getSid())) {
            int state = PlayEngineFactory.getEngine().getState();
            if (state == PlayerInfo.PLAYER_STATUS_PAUSE || state == PlayerInfo.PLAYER_STATUS_RELEASE) {
                PlayEngineFactory.getEngine().playOrPause(operation);
                return PLAY_OK;
            } else {
                LogUtil.logd(TAG + "[music]audioplayer can't support ,to "
                        + PlayEngineFactory.getEngine().getState() + ",currentAudio is "
                        + currentAudio.getName());
            }
            return PLAY_ALREADY;
        }
        PlayEngineFactory.getEngine().release(EnumState.Operation.sound);
        return PLAY_FAIL;
    }

    public static void playMusic(int screen, EnumState.Operation operation) {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            int i = playLastMusic(operation);
            if (i == PLAY_FAIL) {
                if (!playLastPlayListMusic(operation, true)) {
                    playRecommend(screen, operation);
                }
            } else if (i == PLAY_ALREADY) {
                //?????????????????????????????????,???????????????????????????.
                TtsUtil.speakText(Constant.RS_VOICE_SPEAK_PLAY_ALREADY);
            } else {
                //????????????
            }
        } else {
            if (!playLocalMusicWithBreakpoint(operation)) {
                TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
            }
        }
    }

    public static boolean playLocalMusicWithBreakpoint(EnumState.Operation operation) {
        return playLocalMusic(operation, DBManager.getInstance().convertHistoryDataToAudio(DaoManager.getInstance().findlasterMusicHistory()), null, true);
    }

//    /**
//     * ???????????????????????????
//     *
//     * @param operation
//     * @param needPlay
//     * @return
//     */
//    public static boolean playLastSceneMusic(EnumState.Operation operation, boolean needPlay) {
////
//        IPlayResponse response = new IPlayResponse() {
//
//            @Override
//            public void onResult(boolean isExecute, String errorMessage) {
//                if (!isExecute) {
//                    playRecommend(PlayInfoManager.DATA_ALBUM, operation);
//                }
//            }
//        };
//        List<HistoryData> musicHistory = DBManager.getInstance().findMusicHistory();
//        if (CollectionUtils.isNotEmpty(musicHistory)) {
//            Audio audio = musicHistory.get(0).getAudio();
//            Album album = musicHistory.get(0).getAlbum();
//            if (SharedPreferencesUtils.getPlayListScence() == PlayInfoManager.DATA_LOCAL) {
//                playLocalMusic(operation, audio, response);
//                return true;
//            }
//            if (SharedPreferencesUtils.getPlayListScence() == PlayInfoManager.DATA_FAVOUR) {
//                playCollect(operation, response);
//                return true;
//            }
//            if (SharedPreferencesUtils.getPlayListScence() == PlayInfoManager.DATA_HISTORY) {
//                return playHistoryMusic(operation, true);
//            }
//            //??????????????????????????????????????????
//            if (album != null) {
//                AlbumEngine.getInstance().playAlbumWithAudio(operation, PlayInfoManager.DATA_ALBUM, album, audio, false, true);
//                return true;
//            }
//            return false;
//        }
//
//
//        return false;
//    }

    /**
     * ???????????????????????????
     *
     * @param operation
     * @param isNeedPlay
     * @return
     */
    public static boolean playLastPlayListMusic(EnumState.Operation operation, boolean isNeedPlay) {
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????

        //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????json?????????????????????
        //??????????????????????????????????????????????????????

        PlayListData playListData = DBManager.getInstance().findPlayListData();

        Logger.d(TAG, "playLastPlayListMusic:" + Objects.getObj2String(playListData));

        if (playListData != null && playListData.getAudioStr() != null && !playListData.getAudioStr().isEmpty()) {
            if (playListData.getDataOri() == PlayInfoManager.DATA_LOCAL) {
                //??????????????????????????????
                // ?????????????????????????????????
                // 1.????????????????????????????????????
                //????????????4.4.1???????????????????????????????????????B??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004292
                LocalAudioDataSource.getInstance().deleteCantPlayAudios(playListData.getAudioStr());
                if (CollectionUtils.isEmpty(playListData.getAudioStr())) {
                    return false;
                }
            }
            PlayEngineFactory.getEngine().setAudios(operation, playListData.getAudioStr(), playListData.getAlbum(), playListData.getAudioStr().indexOf(playListData.getAudio()), playListData.getDataOri());
            PlayEngineFactory.getEngine().playOrPause(operation);
            return true;
        }


        return false;

    }

    public static void playLastPlaylistObservable(EnumState.Operation operation) {
        io.reactivex.Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
                boolean playHistory = false;
                if (newestHistory != null) {
                    Logger.i(TAG, "newest history:" + newestHistory.toString());
                    if (newestHistory.getType() == HistoryData.TYPE_AUDIO) {
                        playHistory = playLastPlayListMusic(operation, true);
                    } else {
                        // sdk????????????????????????????????????????????????????????????????????????????????????
                        if (EnumState.Operation.extra == operation && !NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                            playHistory = PlayHelper.playLocalMusic(operation, null, null, true);
                        } else {
                            playHistory = PlayHelper.playHistoryRadio(operation, true);
                        }
                    }
                }
                if (!playHistory) {
                    PlayHelper.playRecommend(PlayInfoManager.DATA_ALBUM, operation);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }
//
//    public static boolean playLastPlayList(EnumState.Operation operation) {
//        HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
//        boolean playHistory = false;
//        if (newestHistory != null) {
//            Logger.i(TAG, "newest history:" + newestHistory.toString());
//            if (newestHistory.getType() == HistoryData.TYPE_AUDIO) {
//                playHistory = playLastPlayListMusic(operation, true);
//            } else {
//                playHistory = PlayHelper.playHistoryRadio(operation, true);
//            }
//        }
//        return playHistory;
//    }


    /**
     * ????????????
     */
    public static boolean playHistoryMusic(EnumState.Operation operation, boolean needPlay) {
        List<HistoryData> musicHistory = DBManager.getInstance().findMusicHistory();
        if (CollectionUtils.isNotEmpty(musicHistory)) {
            Album album = musicHistory.get(0).getAlbum();
            Audio audio = musicHistory.get(0).getAudio();
//            if (album != null) {
//                AlbumEngine.getInstance().playAlbumWithAudio(operation, PlayInfoManager.DATA_ALBUM, album, audio, false, true);
//            } else {
            List<Audio> audios = DBManager.getInstance().convertHistoryDataToAudios(musicHistory);
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.auto, audios, null, audios.indexOf(audio), PlayInfoManager.DATA_HISTORY);
            //??????????????????????????????????????????4.4.0????????????????????????
//                return false;
//            }
            if (needPlay) {
                PlayEngineFactory.getEngine().playOrPause(operation);
            }
            return true;
        } else {
            return false;
        }

    }


    /**
     * ????????????
     */
    public static boolean playRecommend(final int screen, final EnumState.Operation operation) {
        Logger.d(TAG, "playRecommend:");
        return playRecommend(screen, operation, true);
    }

    private static void ttsSpeakOrToast(EnumState.Operation operation, String tts) {
        //????????????launcher??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        //https://www.tapd.cn/21865291/bugtrace/bugs/view/1121865291001008213
        if (operation == EnumState.Operation.extra) {
            return;
        }

        if (operation == EnumState.Operation.manual) {
            ToastUtils.showShort(tts);
        } else {
            TtsUtil.speakText(tts);
        }
    }

    /**
     * ????????????
     */
    public static boolean playRecommend(final int screen, final EnumState.Operation operation, final boolean isPlay) {
        if (EnumState.Operation.extra == operation && !isPlay) {
            bBlockOnlineQuery = false;
            bBlockLocalQuery = true;
        }

        //?????????????????????????????????????????????
        //?????????????????????
        final ReqCategory reqCategory = new ReqCategory();
        reqCategory.setbAll(1);
        NetCacheManager.getInstance().requestCache(Constant.GET_CATEGORY, reqCategory, true, new RequestCallBack<String>(String.class) {
            @Override
            public void onResponse(String data) {
                if (EnumState.Operation.extra == operation && !isPlay && bBlockOnlineQuery) {
                    return;
                }

                final Homepage<Category> homepage = JsonHelper.toObject(data, new TypeToken<Homepage<Category>>() {
                }.getType());

                if (homepage == null || CollectionUtils.isEmpty(homepage.getArrCategory())) {
                    ttsSpeakOrToast(operation, Constant.RS_VOICE_SPEAK_PARSE_ERROR);
                    LogUtil.e(TAG + " request category error:" + "result:null");
                    return;
                }


                long categoryID = 0;

                for (Category category : homepage.getArrCategory()) {
                    if (category.getCategoryId() == 100000) {
                        categoryID = category.getArrChild().get(0).getCategoryId();
                        break;
                    }
                }
                ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
                reqSearchAlbum.setPageId(1);
                reqSearchAlbum.setCategoryId(categoryID);
                NetCacheManager.getInstance().requestCache(Constant.GET_SEARCH_LIST, reqSearchAlbum, false, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {
                    @Override
                    public void onResponse(ResponseSearchAlbum data) {
                        if (EnumState.Operation.extra == operation && !isPlay && bBlockOnlineQuery) {
                            return;
                        }

                        if (data == null || CollectionUtils.isEmpty(data.getArrAlbum())) {
                            ttsSpeakOrToast(operation, Constant.RS_VOICE_SPEAK_PARSE_ERROR);
                            LogUtil.e(TAG + " request album error:" + "result:null");
                            return;
                        }
                        Album album = data.getArrAlbum().get(0);
                        AlbumEngine.getInstance().playAlbum(screen, operation, album, album.getCategoryId(), isPlay, null);
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        if (EnumState.Operation.extra == operation && !isPlay && bBlockOnlineQuery) {
                            return;
                        }

                        ttsSpeakOrToast(operation, Constant.RS_VOICE_SPEAK_NET_POOR);
                        LogUtil.e(TAG + " request album error:" + error.getErrorCode());
                    }
                });
            }

            @Override
            public void onError(String cmd, final Error error) {
                if (EnumState.Operation.extra == operation && !isPlay && bBlockOnlineQuery) {
                    return;
                }

                ttsSpeakOrToast(operation, Constant.RS_VOICE_SPEAK_NET_POOR);
                LogUtil.e(TAG + " request category error:" + error.getErrorCode());
            }
        });
        return true;
    }

    /**
     * ????????????
     */
    public static boolean playLocalMusic(EnumState.Operation operation, Audio audio, IPlayResponse response, boolean isPlay) {
        if (EnumState.Operation.extra == operation && !isPlay) {
            bBlockLocalQuery = false;
            bBlockOnlineQuery = true;
        }
        List<Audio> audios = new ArrayList<>();
        Disposable disposable = LocalAudioDataSource.getInstance().findLocalDBDate()
                .subscribe(v -> {
                    audios.add(v);
                }, e -> {
                    if (EnumState.Operation.extra == operation && !isPlay && bBlockLocalQuery) {
                        return;
                    }
//                        mView.showEmpty();
                }, () -> {
                    if (EnumState.Operation.extra == operation && !isPlay && bBlockLocalQuery) {
                        return;
                    }

                    Logger.d(TAG, "playLocalMusic:");
                    if (audios.isEmpty()) {
                        if (EnumState.Operation.extra != operation || isPlay) {
                            TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
                        }
                        callPlayResponse(response, false, Constant.RS_VOICE_SPEAK_NONE_NET);
                    } else {
                        int index = 0;
                        if (audio != null) {
                            index = audios.indexOf(audio);
                        }
                        PlayEngineFactory.getEngine().setAudios(operation, audios, null, index, PlayInfoManager.DATA_LOCAL);

                        if (isPlay) {
                            PlayEngineFactory.getEngine().playOrPause(operation);
                        } else {
                            //???????????????????????????????????????????????????????????????
                            Audio playItem = PlayEngineFactory.getEngine().getCurrentAudio();
                            PlayerCommunicationManager.getInstance().sendPlayItemChanged(playItem);
                        }
                        callPlayResponse(response, true, "");
                    }
                });
        return true;
    }

    private static boolean bBlockLocalQuery; // ????????????????????????
    private static boolean bBlockOnlineQuery;// ????????????????????????

    public static boolean isBlockOnlineQuery() {
        return bBlockOnlineQuery;
    }

    /**
     * ????????????
     *
     * @return
     */
    public static void playRandom(int screen, EnumState.Operation operation) {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            playRecommend(screen, operation);
        } else {
            if (!playLocalMusicWithBreakpoint(operation)) {
                TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
            }
        }
    }

    public static void searchPlayCollect(final EnumState.Operation operation) {
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            playCollect(operation, null);
        } else {
            TtsUtil.speakResource("RS_VOICE_MUSIC_PLAY_ERROR_TTS", Constant.RS_VOICE_MUSIC_PLAY_ERROR_TTS);
        }

    }


    /**
     * ??????????????????
     */
    private static void playCollect(EnumState.Operation operation, IPlayResponse response) {

        FavorHelper.getFavourData(0, 0, 0L, Constant.PAGECOUNT, new FavorHelper.SendFavourListener<FavourBean>() {
            @Override
            public void onResponse(List<FavourBean> favourBeans) {
                if (CollectionUtils.isNotEmpty(favourBeans)) {
                    List<Audio> audios = AudioUtils.fromFavourBeans(favourBeans);
                    PlayEngineFactory.getEngine().setAudios(operation, audios, null, 0, PlayInfoManager.DATA_FAVOUR);
                    PlayEngineFactory.getEngine().play(operation);
                    callPlayResponse(response, true, "");
                } else {
                    TtsUtil.speakResource("RS_VOICE_MUSIC_FAVOUR_EMPTY_TTS", Constant.RS_VOICE_MUSIC_FAVOUR_EMPTY_TTS);
                    callPlayResponse(response, false, Constant.RS_VOICE_MUSIC_FAVOUR_EMPTY_TTS);
                }
            }

            @Override
            public void onError() {
                TtsUtil.speakResource("RS_VOICE_MUSIC_PLAY_ERROR_TTS", Constant.RS_VOICE_MUSIC_PLAY_ERROR_TTS);
                callPlayResponse(response, false, Constant.RS_VOICE_MUSIC_PLAY_ERROR_TTS);
            }
        });
    }


    /**
     * ?????????????????????
     *
     * @param operation
     */
    public static void searchPlaySubscribe(final EnumState.Operation operation, final int sid, final long id) {
        //TODO ???????????????????????????????????????????????????????????????
        Log.e(TAG, "searchPlaySubscribe: " + sid + "|" + id);
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            playSubscribe(operation);
        } else {
            TtsUtil.speakResource("RS_VOICE_MUSIC_PLAY_ERROR_TTS", Constant.RS_VOICE_MUSIC_PLAY_ERROR_TTS);
        }
    }

    /**
     * ?????????????????????
     */
    private static void playSubscribe(final EnumState.Operation operation) {

        FavorHelper.getSubscribeData(0, 0, 0L, 1, new FavorHelper.SendFavourListener<SubscribeBean>() {
            @Override
            public void onResponse(List<SubscribeBean> list) {
                if (CollectionUtils.isNotEmpty(list)) {
                    //?????????????????????????????????????????????????????????
                    Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                    if (currentAlbum != null && FavorHelper.isSubscribe(currentAlbum)) {
                        if (PlayInfoManager.getInstance().isPause()) {
                            PlayEngineFactory.getEngine().playOrPause(operation);
                        } else {
                            //?????????????????????
                            TtsUtil.speakResource("RS_VOICE_SPEAK_PLAY_ALREADY", Constant.RS_VOICE_SPEAK_PLAY_ALREADY);
                        }
                    } else {
                        SubscribeBean subscribeBean = list.get(0);
                        Album album = subscribeBean.getAlbum();
                        AlbumEngine.getInstance().playAlbumWithBreakpoint(operation, PlayInfoManager.DATA_SUBSCRIBE, album, album.getCategoryId(), true);
                    }
                } else {
                    TtsUtil.speakResource("RS_VOICE_MUSIC_SUB_EMPTY_TTS", Constant.RS_VOICE_MUSIC_SUB_EMPTY_TTS);
                }
            }

            @Override
            public void onError() {
                TtsUtil.speakResource("RS_VOICE_MUSIC_PLAY_ERROR_TTS", Constant.RS_VOICE_MUSIC_PLAY_ERROR_TTS);
            }
        });


    }


    /**
     * ??????????????????TongtingManager.start()
     */
    public static void start(EnumState.Operation operation) {


    }


    private interface IPlayResponse {
        void onResult(boolean isExecute, String errorMessage);
    }

    private static void callPlayResponse(IPlayResponse response, boolean isExecute, String errorMessage) {
        if (response != null) {
            response.onResult(isExecute, errorMessage);
        }
    }

}
