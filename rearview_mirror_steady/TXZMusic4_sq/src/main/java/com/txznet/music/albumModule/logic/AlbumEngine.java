package com.txznet.music.albumModule.logic;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestAudioCallBack;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ASUS User on 2016/11/7.
 */
public class AlbumEngine {
    private static final String TAG = "Music:AlbumEngine:";
    private static AlbumEngine mInstance;
    private static boolean isNeedBroadcast = true;

    private AlbumEngine() {

    }

    public static AlbumEngine getInstance() {
        if (mInstance == null) {
            synchronized (AlbumEngine.class) {
                if (mInstance == null) {
                    mInstance = new AlbumEngine();
                }
            }
        }
        return mInstance;
    }

    public static void sortResponseAudios(List<Audio> audios) {
        Collections.sort(audios, new Comparator<Audio>() {
            @Override
            public int compare(Audio o1, Audio o2) {
                if (o1.getScore() > o2.getScore()) {
                    return -1;
                } else if (o1.getScore() < o2.getScore()) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public void queryAlbum(final int categoryId, final int pageOff) {
        LogUtil.logd(TAG + " start query album:" + categoryId + "," + pageOff);
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
//                if (pageOff == 1) {
//                    List<Album> albums = DBManager.getInstance().findAlbumsByCategory(categoryId);
//                    if (albums != null && !albums.isEmpty()) {
//                        ResponseSearchAlbum responseAlbum = new ResponseSearchAlbum();
//                        responseAlbum.setArrAlbum(albums);
//                        responseAlbum.setCategoryId(String.valueOf(categoryId));
//                        responseAlbum.setPageId(pageOff);
//                        ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM, responseAlbum);
//                    }
//                }
//				RequestHelpe.reqAlbum(categoryId, pageOff);
                NetManager.getInstance().requestAlbum(categoryId, pageOff, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {
                    @Override
                    public void onResponse(ResponseSearchAlbum data) {
                        handleAlbumList(data);
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        LogUtil.e(TAG + " request album error:" + error.getErrorCode());
                        handleAlbumListError(error.getErrorCode());
                    }
                });
            }
        }, 0);
    }

    public void handleAlbumListError(int errorCode) {
        switch (errorCode) {
            case Error.ERROR_CLIENT_NET_OFFLINE:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET);
                break;
            case Error.ERROR_CLIENT_NET_TIMEOUT:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT);
                break;
            case 6:
            default:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN);
        }
    }

    public void handleAlbumList(final ResponseSearchAlbum responseSearchAlbum) {
        if (responseSearchAlbum == null) {
            LogUtil.loge(TAG + "album list is null");
            return;
        }
        if (responseSearchAlbum.getErrCode() != 0) {
            LogUtil.loge(TAG + "album error code is " + responseSearchAlbum.getErrCode());
            return;
        }
        if (null == responseSearchAlbum.getCategoryId()) {
            LogUtil.e(TAG + "response category id is null");
            return;
        }
        ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM, responseSearchAlbum);
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                List<Album> arrAlbum = responseSearchAlbum.getArrAlbum();
                DBManager.getInstance().saveToAlbum(arrAlbum
                        , Integer.valueOf(responseSearchAlbum.getCategoryId())
                        , responseSearchAlbum.getPageId());
                if (arrAlbum != null) {
                    List<SubscribeBean> subscribeBeans = new ArrayList<>();
                    List<SubscribeBean> unSubscribeBeans = new ArrayList<>();
                    for (Album album : arrAlbum) {

                        SubscribeBean subscribeBean = new SubscribeBean(album);
                        if (Utils.getDataWithPosition(album.getFlag(), Album.POS_SUBSCRIBE) == Album.FLAG_SUBSCRIBE) {
                            subscribeBeans.add(subscribeBean);
                        } else {
                            unSubscribeBeans.add(subscribeBean);
                        }
                    }

                    //存Subscribe
                    DBManager.getInstance().deleteSubscribe(unSubscribeBeans);
                    DBManager.getInstance().saveSubscribeItems(subscribeBeans);
                }
            }
        }, 0);
    }

    public void handleAlbumAudioError(int errorCode) {
        ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);
        switch (errorCode) {
            case Error.ERROR_CLIENT_NET_OFFLINE:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET);
                break;
            case Error.ERROR_CORE_NET_TIMEOUT:
            case Error.ERROR_CLIENT_NET_TIMEOUT:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT);
                break;
            case Error.ERROR_CLIENT_NET_EMPTY_DATA:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_DATA);
                break;
            default:
                ObserverManage.getObserver().send(InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_DATA);
                break;
        }
    }

    private boolean isNeedTips(EnumState.Operation operation) {
        if (EnumState.Operation.manual == operation) {
            return true;
        }
        return false;
    }

    public void handleResponseAlbumAudios(final ResponseAlbumAudio responseAlbumAudio, final RequestAudioCallBack callBack, final Album album) {
        ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);
        if (responseAlbumAudio.getEnd() == 1) {
            if (responseAlbumAudio.getUp() == 1) {
                PlayInfoManager.getInstance().setFirstEnd(true);
            } else {
                PlayInfoManager.getInstance().setLastEnd(true);
            }
        }

        LogUtil.d(TAG + "total num:" + responseAlbumAudio.getTotalNum() + " type:" + responseAlbumAudio.getField());

        final List<Audio> arrAudio = responseAlbumAudio.getArrAudio();

//        //测试代码 TXZ-8921
//        for (Audio audio : arrAudio) {
//            LogUtil.d(TAG + "response " + audio.getName() + " " + audio.getScore());
//        }
        if (arrAudio == null) {
            if (null != callBack) {
                callBack.onError(Constant.GET_ALBUM_AUDIO, new Error(Error.ERROR_CLIENT_NET_EMPTY_DATA));
            }
            return;
        }

        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (null == album) {
                    LogUtil.e(TAG + "album is null, return");
                    return;
                }
                LogUtil.e(TAG + "album is " + album.toString());

                if (CollectionUtils.isNotEmpty(arrAudio)) {
                    List<FavourBean> favourAudios = new ArrayList<>();
                    List<FavourBean> unFavourAudios = new ArrayList<>();
                    for (int i = 0; i < arrAudio.size(); i++) {
                        Audio audio = arrAudio.get(i);
                        if (audio != null) {
                            audio.setStrCategoryId(String.valueOf(responseAlbumAudio.getCategoryId()));
                            audio.setAlbumId(String.valueOf(responseAlbumAudio.getId()));
                            if (TextUtils.isEmpty(audio.getAlbumName())) {
                                audio.setAlbumName(album.getName());
                            }

                            setAudioGray(audio, responseAlbumAudio.getField());

                            FavourBean favourBean = new FavourBean(audio);
                            if (Utils.getDataWithPosition(audio.getFlag(), Audio.POS_FAVOUR) == Audio.FLAG_FAVOUR) {
                                favourAudios.add(favourBean);
                            } else {
                                unFavourAudios.add(favourBean);
                            }
                        }
                    }

                    //更新收藏列表
                    DBManager.getInstance().deleteFavors(unFavourAudios);

                    DBManager.getInstance().saveAudios(arrAudio);
                    DBManager.getInstance().saveFavorMusicItems(favourAudios);
                }

                final List<Audio> audioList = arrAudio;

                if (responseAlbumAudio.needSort()) {
                    sortResponseAudios(arrAudio);
                }

                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (null != callBack) {
                            callBack.onResponse(audioList, album, responseAlbumAudio);
                            PlayInfoManager.getInstance().setPlayListTotalNum(responseAlbumAudio.getTotalNum());
                            ObserverManage.getObserver().send(InfoMessage.SET_PLAY_LIST_TOTAL_NUM);
                        }
                    }
                }, 0);
            }
        }, 0);

    }


    /**
     * 音频置灰逻辑，因为需要查询数据库，所以必须在子线程调用，否则容易导致ANR
     *
     * @param audio 音频
     * @param field 音频类型
     */
    private void setAudioGray(Audio audio, int field) {
        if (null == audio) {
            return;
        }
        Audio localAudio = DBManager.getInstance().findAudio(audio);
        if (null != localAudio) {
            audio.setClientListenNum(localAudio.getClientListenNum());

            if (field == ResponseAlbumAudio.FIELD_TYPE_RECOMMEND) {
                if (audio.getScore() > 1000 && audio.getClientListenNum() > 0) {
                    LogUtil.d(TAG + "force set score " + audio.getName());
                    audio.setScore(0);
                }

                if (audio.getScore() <= 1000 && audio.getClientListenNum() <= 0) {
                    LogUtil.d(TAG + "force set client listen num " + audio.getName());
                    audio.setClientListenNum(1);
                }
            }
        }
    }

    /**
     * 直接播放专辑
     *
     * @param album      要播放的专辑
     * @param categoryId 专辑所属的分类id
     * @param callback   回调，传null的话使用默认的回调
     */
    public void playAlbum(final EnumState.Operation operation, Album album, long categoryId, RequestCallBack<ResponseAlbumAudio> callback) {
        playAlbum(operation, album, categoryId, true, callback);
    }

    /**
     * 直接播放专辑
     *
     * @param album      要播放的专辑
     * @param categoryId 专辑所属的分类id
     * @param callback   回调，传null的话使用默认的回调
     */
    public void playAlbum(final EnumState.Operation operation, Album album, long categoryId, final boolean needPlay, RequestCallBack<ResponseAlbumAudio> callback) {
        if (null == callback) {
            callback = new RequestAudioCallBack(album) {
                @Override
                public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                    TimeUtils.endTime(Constant.SPEND_TAG + "quick:http:request:album");
                    if (null != audios && !audios.isEmpty()) {
                        PlayEngineFactory.getEngine().setAudios(operation, audios, album, 0, PlayInfoManager.DATA_NET);
                        if (needPlay) {
                            PlayEngineFactory.getEngine().play(operation);
                        }
                    } else {
                        TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                        handleAlbumAudioError(Error.ERROR_CLIENT_NET_EMPTY_DATA);
                    }
                }
            };
        }
        NetManager.getInstance().requestAudio(operation, album, null, true, categoryId, null, callback);
    }

    /**
     * 播放某个专辑的某个音频
     *
     * @param album
     * @param audio
     * @param fillListAuto 是否填充音频前后列表。因为只请求之后的话会显得播放的音频是最新的一个
     */
    public void playAlbumWithAudio(final EnumState.Operation operation, final Album album, final Audio audio, final boolean fillListAuto) {
        playAlbumWithAudio(operation, album, audio, fillListAuto, true);
    }

    /**
     * 获取某一个专辑,根据needplay来判断是否需要播放
     *
     * @param album
     * @param audio
     * @param fillListAuto 是否填充音频前后列表。因为只请求之后的话会显得播放的音频是最新的一个
     */
    public void playAlbumWithAudio(final EnumState.Operation operation, final Album album, final Audio audio, final boolean fillListAuto, final boolean needPlay) {
        if (album == null) {
            LogUtil.loge(TAG + "album is null!");
            return;
        }
        LogUtil.d(TAG, "play album with audio:" + audio.getName());
        NetManager.getInstance().requestAudio(operation, album, audio, true, album.getCategoryId(), null, new RequestAudioCallBack(album) {
            @Override
            public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                isNeedBroadcast = false;
                //由于用audio去请求返回的列表中不包含这个audio，所以需要手动加进去
                audios.add(0, audio);
                PlayEngineFactory.getEngine().setAudios(operation, audios, album, 0, PlayInfoManager.DATA_NET);
                if (needPlay) {
                    PlayEngineFactory.getEngine().play(operation);
                }
                ObserverManage.getObserver().send(InfoMessage.SET_PLAY_LIST_TOTAL_NUM);

                if (fillListAuto) {
                    requestMoreAudio(operation, album, audio, album.getCategoryId(), false, null);
                }
                isNeedBroadcast = true;
            }

            @Override
            public void onError(String cmd, Error error) {
                super.onError(cmd, error);
                TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
            }
        });
    }


    /**
     * 先判断该专辑是否需要播放最新的数据，如果不需要（例如小说等专辑）就从上次断点处开始播放
     *
     * @param operation
     * @param album
     * @param categoryId
     */
    public void playAlbumWithBreakpoint(final EnumState.Operation operation, final Album album, final long categoryId) {
        playAlbumWithBreakpoint(operation, album, categoryId, true);
    }

    /**
     * 先判断该专辑是否需要播放最新的数据，如果不需要（例如小说等专辑）就从上次断点处开始播放
     *
     * @param operation
     * @param album
     * @param categoryId
     */
    public void playAlbumWithBreakpoint(final EnumState.Operation operation, final Album album, final long categoryId, final boolean needPlay) {
        if (operation == EnumState.Operation.manual) {
            ObserverManage.getObserver().send(InfoMessage.ALBUM_REQUEST_MANNUAL_START);
        }
        if (album.getBreakpoint() == 1) {
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    List<Audio> audios = DBManager.getInstance().findBreakpointByAlbumId(album.getId());
                    if (null != audios && !audios.isEmpty()) {
                        Audio audio = DBManager.getInstance().findAudio(audios.get(0));
                        LogUtil.d(TAG, "play breakpoint " + audio.getName() + " " + audio.getLastPlayTime());
                        playAlbumWithAudio(operation, album, audio, true, needPlay);
                        return;
                    }
                    LogUtil.d(TAG + album.getName() + " didn't has breakpoint");
                    playAlbum(operation, album, categoryId, needPlay, null);
                }
            });
        } else {
            playAlbum(operation, album, categoryId, needPlay, new RequestAudioCallBack(album) {
                @Override
                public void onResponse(final List<Audio> audios, final Album album, final ResponseAlbumAudio responseAlbumAudio) {
                    if (null != audios && !audios.isEmpty()) {
                        final Audio audio = audios.get(0);
                        AppLogic.runOnBackGround(new Runnable() {
                            @Override
                            public void run() {
                                if (responseAlbumAudio.getField() != ResponseAlbumAudio.FIELD_TYPE_RECOMMEND
                                        && DBManager.getInstance().findBreakpoint(audio) != null) {
                                    List<Audio> breakpointAudios = DBManager.getInstance().findBreakpointByAlbumId(album.getId());
                                    if (breakpointAudios.get(0) != null) {
                                        Audio breakpointAudio = DBManager.getInstance().findAudio(breakpointAudios.get(0));
                                        playAlbumWithAudio(operation, album, breakpointAudio, true, needPlay);
                                    }
                                } else {
                                    PlayEngineFactory.getEngine().setAudios(operation, audios, album, 0, PlayInfoManager.DATA_NET);
                                    if (needPlay) {
                                        PlayEngineFactory.getEngine().play(operation);
                                    }
                                }
                            }
                        });
                    } else {
                        TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                        handleAlbumAudioError(Error.ERROR_CLIENT_NET_EMPTY_DATA);
                    }
                }
            });
        }
    }


    public void playAlbumWithChapters(final EnumState.Operation operation, final Album album, final long category, final List<ReqChapter> chapters) {
        NetManager.getInstance().requestAudio(operation, album, null, true, category, chapters, new RequestAudioCallBack(album) {
            @Override
            public void onResponse(List<Audio> audios, final Album album, ResponseAlbumAudio responseAlbumAudio) {
                LogUtil.logd(TAG + ">>" + StringUtils.toString(responseAlbumAudio.getArrMeasure()) + "," + responseAlbumAudio.getErrMeasure());
                if (CollectionUtils.isNotEmpty(responseAlbumAudio.getArrMeasure())) {
                    if (responseAlbumAudio.getErrMeasure() != 0) {
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (currentAlbum.getSid() == responseAlbumAudio.getSid()
                                && currentAlbum.getId() == responseAlbumAudio.getId()) {
                            TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_MUSIC_NOT_FOUND_CHAPTERS"
                                    , Constant.RS_VOICE_MUSIC_NOT_FOUND_CHAPTERS
                                    , true
                                    , new Runnable() {
                                        @Override
                                        public void run() {
                                            PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
                                        }
                                    });
                            return;
                        } else {
                            List<Audio> breakpointByAlbumId = DBManager.getInstance().findBreakpointByAlbumId(album.getId());
                            if (breakpointByAlbumId != null && !breakpointByAlbumId.isEmpty()) {
                                final Audio breakpointAudio = DBManager.getInstance().findAudio(breakpointByAlbumId.get(0));
                                String reportString = StringUtils.getReportString(breakpointAudio.getReport(), breakpointAudio.getName());
                                TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_PLAY_AUDIO + reportString
                                        , true
                                        , new Runnable() {
                                            @Override
                                            public void run() {
                                                playAlbumWithAudio(operation, album, breakpointAudio, false);
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                final List<Audio> arrAudio = responseAlbumAudio.getArrAudio();
                String reportString = StringUtils.getReportString(arrAudio.get(0).getReport(), arrAudio.get(0).getName());
                TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_PLAY_AUDIO + reportString
                        , true
                        , new Runnable() {
                            @Override
                            public void run() {
                                isNeedBroadcast = false;
                                PlayEngineFactory.getEngine().setAudios(operation, arrAudio, album, 0, PlayInfoManager.DATA_NET);
                                PlayEngineFactory.getEngine().play(operation);

                                if (arrAudio.size() < 3) {
                                    requestMoreAudio(operation, album, arrAudio.get(0), album.getCategoryId(), false, null);
                                }
                                isNeedBroadcast = true;
                            }
                        });
            }

            @Override
            public void onError(String cmd, Error error) {
                TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, false, null);
            }
        }, false);
    }


    public boolean isNeedBroadcast() {
        return isNeedBroadcast;
    }

    /**
     * 获取专辑的更多数据，上拉和下拉刷新用
     *
     * @param album      要获取数据的专辑
     * @param audio      用作索引的audio
     * @param categoryId 专辑的分类id
     * @param isNext     向上获取还是向下获取数据
     * @param callback   回调，传null的话使用默认的回调
     */
    public void requestMoreAudio(final EnumState.Operation operation, Album album, Audio audio, long categoryId, final boolean isNext, RequestCallBack<ResponseAlbumAudio> callback) {






        if (null == callback) {
            callback = new RequestAudioCallBack(album) {
                @Override
                public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                    if (null != audios && !audios.isEmpty()) {
                        PlayEngineFactory.getEngine().addAudios(operation, audios, isNext);
                    }
                }
            };
        }



        NetManager.getInstance().requestAudio(operation, album, audio, isNext, categoryId, null, callback);
    }


    private int findAudioIndex(List<Audio> audios, Audio audio) {
        if (null == audio || null == audios || audios.isEmpty()) {
            return -1;
        }
        int index = -1;
        for (int i = 0; i < audios.size(); i++) {
            Audio a = audios.get(i);
            if (a.getId() == audio.getId() && a.getSid() == audio.getSid()) {
                index = i;
                break;
            }
        }
        return index;
    }

}
