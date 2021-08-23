package com.txznet.music.model;

import android.util.Log;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.BeSendDataDao;
import com.txznet.music.data.db.dao.SubscribeAlbumDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.BeSendData;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavourOperation;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFavour;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author telen
 * @date 2018/12/4,17:44
 */
public class SubscribeModel extends RxWorkflow {
    private TXZMusicApi mTXZMusicApi = TXZMusicApiImpl.getDefault();


    /**
     * 是否在请求最新的数据
     */
    private static boolean isRequestNeweastFavourData = false;

    /**
     * 最新的一次请求最后的一条数据
     */
    private static SubscribeAlbum neweastReqeustLastData = null;

    public SubscribeModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE:
                //执行订阅的逻辑
                invokeSubscribe(action, (SubscribeAlbum) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM));
                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE:
                //执行取消订阅的逻辑
                invokeUnSubscribe(action, (SubscribeAlbum) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM));
                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_GET:
                getSubscribeData(action);
                break;
            case ActionType.ACTION_COMMAND_FAVOUR_SUBSCRIBE:
                //声控订阅,收藏
                doSubscribe(action);
                break;
            case ActionType.ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE:
                //声控取消订阅,取消收藏
                doUnSubscribe(action);
                break;
            default:
                break;
        }
    }

    private Album getCurrentAlbum() {
        //判断当前播放的是音乐还是电台
        if (PlayHelper.get().getCurrAlbum() == null) {
            return null;
        }

        AudioV5 currentAudio = PlayHelper.get().getCurrAudio();
        if (currentAudio != null) {
            if (!AudioUtils.isSong(currentAudio.sid)) {
                //当前播放的是电台
                return PlayHelper.get().getCurrAlbum();
            }
        }
        return null;
    }

    private void doSubscribe(RxAction action) {
        Album currentAlbum = getCurrentAlbum();
        if (currentAlbum != null) {
            // AI电台
            AudioV5 currAudio = PlayHelper.get().getCurrAudio();
            if (AlbumUtils.isAiRadio(currentAlbum) && currAudio != null) {
                Album album = new Album();
                album.id = currAudio.albumId;
                album.sid = currAudio.albumSid;
                album.name = currAudio.albumName;
                album.logo = currAudio.logo;
                invokeSubscribe(action, AlbumConverts.convert2subscribe(album, 0));
            } else {
                invokeSubscribe(action, AlbumConverts.convert2subscribe(currentAlbum, 0));
            }
            Boolean isWakeup = (Boolean) action.data.get(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP);
            if (isWakeup != null && isWakeup) {
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE);
            }
        }


    }

    private void doUnSubscribe(RxAction action) {
        //判断当前播放的是音乐还是电台
        Album currentAlbum = getCurrentAlbum();
        if (currentAlbum != null) {
            // AI电台
            AudioV5 currAudio = PlayHelper.get().getCurrAudio();
            if (AlbumUtils.isAiRadio(currentAlbum) && currAudio != null) {
                Album album = new Album();
                album.id = currAudio.albumId;
                album.sid = currAudio.albumSid;
                album.name = currAudio.albumName;
                invokeUnSubscribe(action, AlbumConverts.convert2subscribe(album, 0));
            } else {
                invokeUnSubscribe(action, AlbumConverts.convert2subscribe(currentAlbum, 0));
            }

            Boolean isWakeup = (Boolean) action.data.get(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP);
            if (isWakeup != null && isWakeup) {
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE_CANCEL", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_CANCEL);
            }

        }
    }


    /**
     * 获取收藏的数据
     * //最终的方案:
     * //顺序执行：
     * //1. 显示本地Favour表数据。
     * //2. 同步BeSendData数据，直到为空。
     * //3. 检测Favour表是否完全执行过拉取操作，
     * //若是，结束流程。
     * //若否，执行拉取操作，拉取完毕后标记为完全拉取，刷新界面。
     *
     * @param action 操作
     */
    private void getSubscribeData(RxAction action) {

        Disposable subscribe = Observable.create(emitter -> {
            //1. 显示本地Favour表数据。获取本地Favour表数据
            List<SubscribeAlbum> favourAudios = DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().listAll();
            emitter.onNext(favourAudios);
            emitter.onComplete();


        }).subscribeOn(Schedulers.io()).subscribe(result -> {
            action.data.put(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS, result);
        }, e -> {
            postRxError(action, e);
        }, () -> {
            postRxData(action);
            List<BeSendData> beSendDataList = DBUtils.getDatabase(GlobalContext.get()).getBeSendDataDao().listAll();
            if (CollectionUtils.isNotEmpty(beSendDataList)) {
                //全部重新请求
                reqFavour(action, beSendDataList);
            } else {
                List<SubscribeAlbum> result = (List<SubscribeAlbum>) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS);
                if (result == null || CollectionUtils.isEmpty(result)) {
                    //开始请求后台favour数据数据
                    getFavourDataFromNet(action, null);
                } else if (CollectionUtils.isNotEmpty(result)) {
                    if (isRequestNeweastFavourData) {
                        //上次请求没有请求完毕
//                        继续请求
                        getFavourDataFromNet(action, result.get(result.size() - 1));
                    }
                }
            }
        });
        addRxAction(action, subscribe);
    }

    /**
     * 获取最后一个音频的收藏时间,如果没有则返回0
     *
     * @param needSize 需要满足的个数,如果不满足,则认为没有多余的数据了,则返回0
     * @return
     */
    private static long getSubscribeAlbumsLastTime(List<TXZRespFavour.FavourBean> responseFavourBeans, int needSize) {
        if (CollectionUtils.isNotEmpty(responseFavourBeans) && responseFavourBeans.size() == needSize) {
            return responseFavourBeans.get(responseFavourBeans.size() - 1).operTime;
        } else {
            return 0;
        }
    }


    private void getFavourDataFromNet(RxAction action, SubscribeAlbum favourAudio) {
        //发起请求
        TXZReqFavour reqFavour = new TXZReqFavour(TXZReqFavour.AUDIO_TYPE);
        reqFavour.count = Configuration.DefVal.REQ_FAVOUR_SIZE;
        if (favourAudio != null) {
            reqFavour.id = favourAudio.id;
            reqFavour.sid = favourAudio.sid;
            reqFavour.operTime = favourAudio.timestamp;
        } else {
            isRequestNeweastFavourData = true;
        }
        Disposable subscribe = mTXZMusicApi.getSubscribe(reqFavour).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(txzRespFavour -> {
            SubscribeAlbumDao subscribeAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao();
            //信赖后台返回的数据
            //1. 将本地的相应时间戳内的Favour数据删除
            subscribeAlbumDao.invokeBySql(SubscribeAlbum.getDeleteSupportSQLiteQuery(txzRespFavour.operTime, getSubscribeAlbumsLastTime(txzRespFavour.arrAudioStore, Configuration.DefVal.REQ_FAVOUR_SIZE)));
            //经过数据库的洗礼
            List<SubscribeAlbum> favourAudios = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(txzRespFavour.arrAudioStore)) {
                //遍历
                for (TXZRespFavour.FavourBean favourBean : txzRespFavour.arrAudioStore) {
                    SubscribeAlbum favourAudio1 = AlbumConverts.convert2subscribe(AlbumConverts.convert2Album(favourBean.album), 0);
                    favourAudio1.timestamp = favourBean.operTime;
                    favourAudios.add(favourAudio1);
                }
                subscribeAlbumDao.saveOrUpdate(favourAudios);
            }
            SharedPreferencesUtils.setHasSyncSubscribeData(true);
            return Observable.just(favourAudios);
        }).observeOn(Schedulers.io()).subscribe(txzRespFavour -> {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",getFavourDataFromNet:success:" + (txzRespFavour != null ? txzRespFavour.size() : 0));
            }

            //如果获取到的数据还没有结束则继续请求
            if (CollectionUtils.isNotEmpty(txzRespFavour)) {
                //如果获取到的数据没有满足需求[请求到的数据等于自己需要的大小],就表明请求到头了,不需要重新请求
                if (txzRespFavour.size() == Configuration.DefVal.REQ_FAVOUR_SIZE) {
                    //重新请求
                    //取最后一个进行请求
                    getFavourDataFromNet(action, txzRespFavour.get(txzRespFavour.size() - 1));
                    return;
                }
            }
            //如果出错了,下次也要重新请求
            // 请求结束
            isRequestNeweastFavourData = false;

//            从数据库中取出所有的数据同步给界面

            action.data.put(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUMS, DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().listAll());
            postRxData(action);
        }, e -> {
            postRxError(action, e);
        });
        addRxAction(action, subscribe);

    }


    private void invokeUnSubscribe(RxAction action, SubscribeAlbum subscribeAlbum) {
        if (BuildConfig.DEBUG) {
            Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",invokeUnSubscribe:" + subscribeAlbum);
        }
        Disposable subscribe = Observable.create(emitter -> {
            changeTimestamp2Now(subscribeAlbum);
            //保存数据到待发送的数据库
            BeSendData beSendData = saveSendData2DB(subscribeAlbum, BeSendData.UNFAVOUR);
            //保存到订阅的数据库中(用于后面直接查找)
            DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().delete(subscribeAlbum);

            emitter.onNext(beSendData);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {

            postRxData(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM, subscribeAlbum).build());

        }, e -> {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",invokeUnSubscribe:" + e.toString());
            }

            postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG));
        });
        addRxAction(action, subscribe);
    }

    /**
     * 执行收藏的操作
     *
     * @param action         传递的数据
     * @param subscribeAlbum 收藏的曲目
     */
    private void invokeSubscribe(RxAction action, SubscribeAlbum subscribeAlbum) {
        if (BuildConfig.DEBUG) {
            Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",invokeSubscribe:" + subscribeAlbum);
        }

        Disposable subscribe = Observable.create(emitter -> {
            changeTimestamp2Now(subscribeAlbum);
            //保存数据到待发送的数据库
            BeSendData beSendData = saveSendData2DB(subscribeAlbum, BeSendData.FAVOUR);
            //保存到订阅的数据库中(用于后面直接查找)
            DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao().saveOrUpdate(subscribeAlbum);

            emitter.onNext(beSendData);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
            postRxData(RxAction.type(ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE).bundle(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM, subscribeAlbum).build());
        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));
        addRxAction(action, subscribe);
    }

    private void reqFavour(RxAction action, List<BeSendData> sendData) {
        //创建请求参数
        TXZReqFavourOperation reqFavourOperation = new TXZReqFavourOperation();
        reqFavourOperation.arr_store_oper = new ArrayList<>();
        reqFavourOperation.arr_store_oper.addAll(sendData);


        //如果网络请求回来,则删除待发送的数据
        Disposable subscribe = mTXZMusicApi.subscribeAlbum(reqFavourOperation).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(result -> {
            BeSendDataDao beSendDataDao = DBUtils.getDatabase(GlobalContext.get()).getBeSendDataDao();
            if (result != null && CollectionUtils.isNotEmpty(result.arr_store_oper)) {
                //将待发送的数据删除
                beSendDataDao.delete(result.arr_store_oper);
            }
            // FIXME: 2018/12/12 这样写会不会导致攻击后台的风险,比方说后台哪一天没有返回BeSendData的情况
            //如果数据库里面还有数据,则需要重新请求一遍
            //重新判断是否需要在次回调,
            List<BeSendData> beSendDataList = beSendDataDao.listAll();
            if (CollectionUtils.isNotEmpty(beSendDataList)) {
                reqFavour(action, beSendDataList);
            } else {
                //全部同步完成,则进行请求后台数据的操作
                getFavourDataFromNet(action, null);
            }


        }, e -> postRxError(action, e));
        addRxAction(action, subscribe);
    }


    /**
     * 保存数据到待发送的数据库
     *
     * @param subscribeAlbum 待保存到数据库中的数据
     * @return 可以同步到后台的请求数据
     */
    private BeSendData saveSendData2DB(SubscribeAlbum subscribeAlbum, @BeSendData.OperationType int operationType) {
        //保存到待发送的数据库中
        BeSendData sendData = AlbumConverts.convertSubscribeAlbum2BeSendData(subscribeAlbum, PlayHelper.get().getCurrAudio(), operationType);
        DBUtils.getDatabase(GlobalContext.get()).getBeSendDataDao().saveOrUpdate(sendData);
        return sendData;
    }


    /**
     * 修改操作时间为现在
     *
     * @param favourAudio 待修改的audio
     */
    private void changeTimestamp2Now(SubscribeAlbum favourAudio) {
        if (favourAudio != null) {
            favourAudio.timestamp = TimeManager.getInstance().getTimeMillis();
        }
    }

}
