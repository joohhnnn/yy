package com.txznet.music.favor;

import com.txz.ui.tts.UiTts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.bean.BeSendBean;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.ReqBesendBean;
import com.txznet.music.favor.bean.ReqFavour;
import com.txznet.music.favor.bean.RespFavour;
import com.txznet.music.favor.bean.ResponseFavourBean;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by telenewbie on 2017/11/27.
 * 1.存放数据库
 * 2.上报后台
 * 3.更新界面
 * 4.如果上报失败则2min之后在上报,这里需要做一个最大超时上报条数(20条)
 */

public class FavorHelper {
    private static long toBeSendBeanDelay = 2 * 60 * 1000;//2分钟的同步时间

    public static boolean isSupportFavour(Audio audio) {
        if (Utils.isLocalSong(audio.getSid())) {
            return true;
        } else {
            return Utils.getDataWithPosition(audio.getFlag(), Audio.POS_SUPPORT_FAVOUR) == Audio.FLAG_SUPPORT;
        }
    }

    public static boolean isSupportSubscribe(Album album) {
        return Utils.getDataWithPosition(album.getFlag(), Album.POS_SUPPORT_SUBSCRIBE) == Album.FLAG_SUPPORT;
    }

    public static boolean isSubscribe(Album album) {
        return null != DBManager.getInstance().findNetSubscribe(album);
    }

    public static boolean isFavour(Audio audio) {
        return null != DBManager.getInstance().findFavorMusic(audio);
    }

    /**
     * 收藏音乐接口
     *
     * @return
     */
    public static int favor(Audio audio, EnumState.Operation operation) {
        if (!Utils.isLocalSong(audio.getSid())) {
            sendFavourOpeToNet(audio, true, operation);
        } else {
            audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_FAVOUR, Audio.POS_FAVOUR));
            favourSuccess(audio, true, operation);
        }

        return -1;
    }

    private static void sendFavourOpeToNet(Audio audio, boolean isFavour, EnumState.Operation operation) {
        List<BeSendBean> beSendBeans = new ArrayList<>();
        final BeSendBean beSendBean = new BeSendBean();
        beSendBean.setId(audio.getId());
        beSendBean.setSid(audio.getSid());
        beSendBean.setTimestamp(getTimestamp());
        beSendBean.setOperation(isFavour ? BeSendBean.FAVOUR : BeSendBean.UNFAVOUR);
        beSendBeans.add(beSendBean);
        sendToNetPre(beSendBeans, audio, isFavour, operation);
    }

    private static void favourSuccess(Audio audio, boolean isFavour, EnumState.Operation operation) {
        FavourBean favourBean = new FavourBean();
        favourBean.setId(audio.getId());
        favourBean.setSid(audio.getSid());
        favourBean.setAudioDbId(String.format(Locale.getDefault(), "%d-%d", audio.getId(), audio.getSid()));
        favourBean.setAudio(audio);
        favourBean.setTimestamp(getTimestamp());
        Logger.d("music:favour:success:", favourBean);
        if (isFavour) {
            audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_FAVOUR, Audio.POS_FAVOUR));
            audio.setOperTime(favourBean.getTimestamp());
            ObserverManage.getObserver().send(InfoMessage.FAVOUR_MUSIC, audio);
            DBManager.getInstance().saveFavorMusicItem(favourBean);
            DBManager.getInstance().updateAudioFavour(audio);
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_FAVOUR_TTS", Constant.RS_VOICE_MUSIC_FAVOUR_TTS, null);
            }
        } else {
            audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_UNFAVOUR, Audio.POS_FAVOUR));
            audio.setOperTime(favourBean.getTimestamp());
            ObserverManage.getObserver().send(InfoMessage.UNFAVOUR_MUSIC, audio);
            DBManager.getInstance().deleteFavor(favourBean);
            DBManager.getInstance().updateAudioFavour(audio);
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_UNFAVOUR_TTS", Constant.RS_VOICE_MUSIC_UNFAVOUR_TTS, null);
            }
        }
    }

    private static void favourFailure(boolean isFavour, EnumState.Operation operation) {
        if (isFavour) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_FAVOUR_ERROR_TTS);
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_FAVOUR_ERROR_TTS", Constant.RS_VOICE_MUSIC_FAVOUR_ERROR_TTS, null);
            }
        } else {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_UNFAVOUR_ERROR_TTS);
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_UNFAVOUR_ERROR_TTS", Constant.RS_VOICE_MUSIC_UNFAVOUR_ERROR_TTS, null);
            }
        }
    }

    private static void subscribeSuccess(Album album, boolean isSubscribe, EnumState.Operation operation) {
        SubscribeBean subscribeBean = new SubscribeBean();
        subscribeBean.setId(album.getId());
        subscribeBean.setSid(album.getSid());
        subscribeBean.setAlbumDbId(String.format(Locale.getDefault(), "%d-%d", album.getSid(), album.getId()));
        subscribeBean.setAlbum(album);
        subscribeBean.setTimestamp(getTimestamp());
        if (isSubscribe) {
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_SUB_TTS", Constant.RS_VOICE_MUSIC_SUB_TTS, null);
            }

            album.setFlag(Utils.setDataWithPosition(album.getFlag(), Album.FLAG_SUBSCRIBE, Album.POS_SUBSCRIBE));
            album.setOperTime(subscribeBean.getTimestamp());
            ObserverManage.getObserver().send(InfoMessage.SUBSCRIBE_RADIO, album);
            DBManager.getInstance().saveSubscribeItem(subscribeBean);
            DBManager.getInstance().updateAlbumSubscribe(album);
        } else {
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_UNSUB_TIPS", Constant.RS_VOICE_MUSIC_UNSUB_TIPS, null);
            }
            album.setOperTime(subscribeBean.getTimestamp());
            album.setFlag(Utils.setDataWithPosition(album.getFlag(), Album.FLAG_UNSUBSCRIBE, Album.POS_SUBSCRIBE));
            ObserverManage.getObserver().send(InfoMessage.UNSUBSCRIBE_RADIO, album);
            DBManager.getInstance().deleteSubscribe(subscribeBean);
            DBManager.getInstance().updateAlbumSubscribe(album);
        }
    }

    private static void subscribeFailure(boolean isSubscribe, EnumState.Operation operation) {
        if (isSubscribe) {
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_SUB_ERROR_TIPS", Constant.RS_VOICE_MUSIC_SUB_ERROR_TIPS, null);
            }

            ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_SUB_ERROR_TIPS);
        } else {
            if (EnumState.Operation.sound == operation) {
                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_UNSUB_ERROR_TIPS", Constant.RS_VOICE_MUSIC_UNSUB_ERROR_TIPS, null);
            }

            ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_UNSUB_ERROR_TIPS);
        }

    }


    /**
     * 取消收藏
     *
     * @return
     */
    public static int unfavor(Audio audio, EnumState.Operation operation) {
        if (!Utils.isLocalSong(audio.getSid())) {
            sendFavourOpeToNet(audio, false, operation);
        } else {
            favourSuccess(audio, false, operation);
        }

        return 1;
    }

    /**
     * 获取所有收藏的音乐列表
     *
     * @return
     */
    private static List<FavourBean> getAllFavorMusic() {
        return DBManager.getInstance().findAllFavorMusic();
    }

    /**
     * 获取所有订阅的电台列表
     *
     * @return
     */
    private static List<SubscribeBean> getAllSubscribeRadio() {
        return DBManager.getInstance().findAllSubscribe();
    }

    /**
     * 订阅的接口
     *
     * @return
     */
    public static int subscribeRadio(Album album, EnumState.Operation operation) {
        List<Album> albums = new ArrayList<>();
        albums.add(album);
        reqData(albums, true, operation);
        return -1;
    }

    /**
     * 取消收藏,取消订阅
     *
     * @return
     */
    public static int unSubscribeRadio(final List<Album> albums, final EnumState.Operation operation) {

        reqData(albums, false, operation);
        return 1;
    }

    /**
     * 取消收藏,取消订阅
     *
     * @return
     */
    public static int unSubscribeRadio(final Album album, final EnumState.Operation operation) {
        List<Album> albums = new ArrayList<>();
        albums.add(album);
        reqData(albums, false, operation);
        return 1;
    }

    private static void reqData(final List<Album> albums, final boolean isSubscribe, final EnumState.Operation operation) {
        ReqBesendBean reqBesendBean = new ReqBesendBean();
        List<BeSendBean> beSendBeans = new ArrayList<>();
        for (Album album : albums) {
            if (null == album) {
                continue;
            }
            BeSendBean beSendBean = new BeSendBean();
            beSendBean.setId(album.getId());
            beSendBean.setSid(album.getSid());
            beSendBean.setTimestamp(getTimestamp());
            beSendBean.setOperation(isSubscribe ? BeSendBean.FAVOUR : BeSendBean.UNFAVOUR);
            beSendBeans.add(beSendBean);
        }
        if (CollectionUtils.isEmpty(beSendBeans)) {
            subscribeFailure(isSubscribe, operation);
            return;
        }
        reqBesendBean.setArr_store_oper(beSendBeans);
        NetManager.getInstance().sendRequestToCore(Constant.GET_UPON_FAVOUR, reqBesendBean, new RequestCallBack<ReqBesendBean>(ReqBesendBean.class) {

            @Override
            public void onResponse(ReqBesendBean data) {
                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                if (currentAlbum != null && albums.contains(currentAlbum)) {
                    subscribeSuccess(currentAlbum, isSubscribe, operation);
                }
                if (!isSubscribe) {
                    deleteSubscribeDataSuccess(albums, operation);
                }
            }

            @Override
            public void onError(String cmd, Error error) {
                subscribeFailure(isSubscribe, operation);
            }
        });
    }

    private static void deleteSubscribeDataSuccess(List<Album> albums, EnumState.Operation operation) {
        List<SubscribeBean> subscribeBeans = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(albums)) {
            for (Album album : albums) {
                SubscribeBean subscribeBean = new SubscribeBean();
                subscribeBean.setId(album.getId());
                subscribeBean.setSid(album.getSid());
                subscribeBean.setAlbumDbId(String.format(Locale.getDefault(), "%d-%d", album.getSid(), album.getId()));
                subscribeBean.setAlbum(album);
                subscribeBean.setTimestamp(getTimestamp());
                subscribeBeans.add(subscribeBean);

                album.setFlag(Utils.setDataWithPosition(album.getFlag(), Album.FLAG_UNSUBSCRIBE, Album.POS_SUBSCRIBE));
            }
        }
        ObserverManage.getObserver().send(InfoMessage.UNSUBSCRIBE_MULTI_RADIO, albums);


        DBManager.getInstance().deleteSubscribe(subscribeBeans);
        DBManager.getInstance().saveAlbumOrReplace(albums);
    }

    private static void sendToNetPre(final List<BeSendBean> beSendBeans, final Audio audio, final boolean isFavour, final EnumState.Operation operation) {
        ReqBesendBean reqBesendBean = new ReqBesendBean();
        reqBesendBean.setArr_store_oper(beSendBeans);
        NetManager.getInstance().sendRequestToCore(Constant.GET_UPON_FAVOUR, reqBesendBean, new RequestCallBack<ReqBesendBean>(ReqBesendBean.class) {
            @Override
            public void onResponse(ReqBesendBean data) {
                //提示收藏成功
                if (null != audio) {
                    favourSuccess(audio, isFavour, operation);
                }
            }

            @Override
            public void onError(String cmd, Error error) {
                if (null != audio) {
                    favourFailure(isFavour, operation);
                }
            }
        });
    }

    /**
     * 发送所有未同步的数据给到后台,在每一次的请求收藏音乐之前
     *
     * @param sendFavourListener
     */
    private static void sendToNetAll(final SendFavourListener sendFavourListener) {
        final List<BeSendBean> toBeSendBeans = DBManager.getInstance().findToBeSendBean(200);
        if (CollectionUtils.isEmpty(toBeSendBeans)) {
            sendFavourListener.onResponse(null);
        } else {
            ReqBesendBean reqBesendBean = new ReqBesendBean();
            reqBesendBean.setArr_store_oper(toBeSendBeans);
            NetManager.getInstance().sendRequestToCore(Constant.GET_UPON_FAVOUR, reqBesendBean, new RequestCallBack<ReqBesendBean>(ReqBesendBean.class) {
                @Override
                public void onResponse(ReqBesendBean data) {
                    if (CollectionUtils.isNotEmpty(data.getArr_store_oper())) {
                        DBManager.getInstance().deleteToBeSendBeans(data.getArr_store_oper());
                    }
                    sendFavourListener.onResponse(null);
                }

                @Override
                public void onError(String cmd, Error error) {
                    sendFavourListener.onError();
                }

            });
        }
    }

    /**
     * 返回毫秒数
     *
     * @return
     */
    private static long getTimestamp() {
        return TimeManager.getInstance().getTimeMillis();
    }

    /**
     * 获取最后一个音频的收藏时间,如果没有则返回0
     *
     * @param needSize 需要满足的个数,如果不满足,则认为没有多余的数据了,则返回0
     * @return
     */
    private static long getFavourAudiosLastTime(List<ResponseFavourBean> responseFavourBeans, int needSize) {
        if (CollectionUtils.isNotEmpty(responseFavourBeans) && responseFavourBeans.size() == needSize) {
            return responseFavourBeans.get(responseFavourBeans.size() - 1).getOperTime();
        } else {
            return 0;
        }
    }

    /**
     * @param sid
     * @param id
     * @param reqTime 请求的时间戳
     * @param count
     */
    public static void getFavourData(final int sid, final long id, final long reqTime, final int count, final SendFavourListener sendFavourListener) {
        if (isFirstGet(sid, id)) {//表明第一次
            sendToNetAll(new SendFavourListener() {
                @Override
                public void onResponse(List list) {
                    getFavourDataFromNet(sid, id, reqTime, count, sendFavourListener);
                }

                @Override
                public void onError() {
                    getFavourDataFromLocal(sendFavourListener);
                }
            });
        } else {
            getFavourDataFromNet(sid, id, reqTime, count, sendFavourListener);
        }
    }

    private static void getFavourDataFromNet(final int sid, final long id, final long reqTime, int count, final SendFavourListener sendFavourListener) {

        Assert.assertNotNull(sendFavourListener);
        final ReqFavour reqFavour = new ReqFavour();
        reqFavour.setCount(count);
        reqFavour.setId(id);
        reqFavour.setSid(sid);
        reqFavour.setOperTime(reqTime);
        reqFavour.setStoreType(ReqFavour.AUDIO_TYPE);
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            //有网则请求
            NetManager.getInstance().sendRequestToCore(Constant.GET_FAVOUR_LIST, reqFavour, new RequestCallBack<RespFavour>(RespFavour.class) {


                @Override
                public void onResponse(RespFavour data) {
                    List<ResponseFavourBean> responseFavourBeans = CollectionUtils.expertNullItem(data.getArrAudioStore());
                    long favourAudiosLastTime = getFavourAudiosLastTime(responseFavourBeans, reqFavour.getCount());
                    DBManager.getInstance().deleteNetFavor(data.getOperTime(), favourAudiosLastTime);

                    //将本地的数据进行合并然后去重
                    if (CollectionUtils.isNotEmpty(responseFavourBeans)) {
                        //删除该段时间戳之内的除本地(SD卡收藏)的所有的数据全部删除掉
                        List<FavourBean> favourBeans = new ArrayList<>();
                        for (int i = 0; i < responseFavourBeans.size(); i++) {
                            if (responseFavourBeans.get(i) != null) {
//                                saveAudioTemps.add(responseFavourBeans.get(i).getAudio());
                                FavourBean favourBean = new FavourBean();
                                favourBean.setId(responseFavourBeans.get(i).getAudio().getId());
                                favourBean.setSid(responseFavourBeans.get(i).getAudio().getSid());
                                favourBean.setTimestamp(responseFavourBeans.get(i).getOperTime());
                                favourBean.setAudio(responseFavourBeans.get(i).getAudio());
                                favourBeans.add(favourBean);

                                //通知界面去更新
                                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                                if (currentAudio != null && currentAudio.getId() == favourBean.getId() && currentAudio.getSid() == favourBean.getSid()) {
                                    ObserverManage.getObserver().send(InfoMessage.FAVOUR_MUSIC, currentAudio);
                                }
                            }
                        }
                        DBManager.getInstance().saveFavorMusicItems(favourBeans);
                    }
                    PlayInfoManager.getInstance().notifyPlayListChanged();
                    List<FavourBean> favorMusics = DBManager.getInstance().findFavorMusics(data.getOperTime(), favourAudiosLastTime);
                    LogUtil.logd("music:favour:get size from favour databases:" + favorMusics.size() + "from startTime =" + data.getOperTime() + ",endTime=" + favourAudiosLastTime);
                    sendFavourListener.onResponse(favorMusics);
                }

                @Override
                public void onError(String cmd, Error error) {
                    sendFavourListener.onError();
                }
            });
        } else {
            //无网,则直接显示本地的数据
            getFavourDataFromLocal(sendFavourListener);
        }
    }

    private static void getFavourDataFromLocal(final SendFavourListener sendFavourListener) {
        Assert.assertNotNull(sendFavourListener);
        //无网,则直接显示本地的数据
        List<FavourBean> allFavorMusic = DBManager.getInstance().findAllFavorMusic();
        sendFavourListener.onResponse(allFavorMusic);
    }

    public static void getSubscribeData(final int sid, final long id, final int count, final SendFavourListener sendFavourListener) {
        if (isFirstGet(sid, id)) {//表明第一次
            sendToNetAll(new SendFavourListener() {
                @Override
                public void onResponse(List list) {
                    getSubscribeDataFromNet(sid, id, count, sendFavourListener);
                }

                @Override
                public void onError() {
                    getSubscribeDataFromLocal(sendFavourListener);
                }
            });
        } else {
            getSubscribeDataFromNet(sid, id, count, sendFavourListener);
        }
    }

    private static boolean isFirstGet(int sid, long id) {
        return sid == id && id == 0;
    }

    private static void getSubscribeDataFromLocal(final SendFavourListener sendFavourListener) {
        Assert.assertNotNull(sendFavourListener);
        List<SubscribeBean> allSubscribe = DBManager.getInstance().findAllSubscribe();
        sendFavourListener.onResponse(allSubscribe);
    }

    private static void getSubscribeDataFromNet(final int sid, final long id, int count, final SendFavourListener sendFavourListener) {
        Assert.assertNotNull(sendFavourListener);

        final ReqFavour reqFavour = new ReqFavour();
        reqFavour.setCount(count);
        reqFavour.setId(id);
        reqFavour.setSid(sid);
        reqFavour.setStoreType(ReqFavour.ALBUM_TYPE);
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            //有网则请求
            NetManager.getInstance().sendRequestToCore(Constant.GET_FAVOUR_LIST, reqFavour, new RequestCallBack<RespFavour>(RespFavour.class) {


                @Override
                public void onResponse(RespFavour data) {
                    List<ResponseFavourBean> responseFavourBeans = CollectionUtils.expertNullItem(data.getArrAudioStore());
                    long favourAudiosLastTime = getFavourAudiosLastTime(responseFavourBeans, reqFavour.getCount());
                    DBManager.getInstance().deleteNetSubscribe(data.getOperTime(), favourAudiosLastTime);
                    //将本地的数据进行合并然后去重
                    List<SubscribeBean> subscribeBeans = new ArrayList<>();
                    if (CollectionUtils.isNotEmpty(responseFavourBeans)) {
                        //删除该段时间戳之内的除本地(SD卡收藏)的所有的数据全部删除掉

                        for (int i = 0; i < responseFavourBeans.size(); i++) {
                            if (responseFavourBeans.get(i) != null) {
                                SubscribeBean subscribeBean = new SubscribeBean();
                                subscribeBean.setId(responseFavourBeans.get(i).getAlbum().getId());
                                subscribeBean.setSid(responseFavourBeans.get(i).getAlbum().getSid());
                                subscribeBean.setTimestamp(responseFavourBeans.get(i).getOperTime());
                                subscribeBean.setAlbum(responseFavourBeans.get(i).getAlbum());
                                subscribeBeans.add(subscribeBean);
                            }
                        }
                        DBManager.getInstance().saveSubscribeItems(subscribeBeans);
                    }
                    List<SubscribeBean> netSubscribe = DBManager.getInstance().findNetSubscribe(data.getOperTime(), favourAudiosLastTime);
                    LogUtil.logd("music:favour:get size from Subscribe databases:" + netSubscribe.size());
                    sendFavourListener.onResponse(netSubscribe);
                }

                @Override
                public void onError(String cmd, Error error) {
                    sendFavourListener.onError();
                }
            });
        } else {
            //无网,则直接显示本地的数据
            getSubscribeDataFromLocal(sendFavourListener);
        }
    }

    public interface SendFavourListener<T> {
        void onResponse(List<T> ts);//成功

        void onError();//失败
    }


}
