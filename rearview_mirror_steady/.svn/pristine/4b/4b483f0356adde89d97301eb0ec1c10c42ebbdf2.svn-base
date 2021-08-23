package com.txznet.music.model;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.BeSendDataDao;
import com.txznet.music.data.db.dao.FavourAudioDao;
import com.txznet.music.data.entity.BeSendData;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavour;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFavourOperation;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFavour;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AudioConverts;
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
public class FavourModel extends RxWorkflow {

    private TXZMusicApiImpl mTXZMusicApi = TXZMusicApiImpl.getDefault();

    /**
     * 是否在请求最新的数据
     */
    private static boolean isRequestNeweastFavourData = false;

    public FavourModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_FAVOUR_EVENT_FAVOUR:
//                收藏
                invokeFavour(action, ((FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO)));
                break;
            case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR:
                //取消收藏
                invokeUnFavour(action, ((FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO)));
                break;
            case ActionType.ACTION_MEDIA_SCANNER_STARTED:
            case ActionType.ACTION_FAVOUR_EVENT_GET:
                getFavourData(RxAction.type(ActionType.ACTION_FAVOUR_EVENT_GET).build());
                break;
            case ActionType.ACTION_COMMAND_FAVOUR_SUBSCRIBE:
                //声控订阅,收藏
                doFavour(action);
                break;
            case ActionType.ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE:
                //声控取消订阅,取消收藏
                doUnFavour(action);
                break;
            default:
                break;
        }
    }

    /**
     * 取消收藏当前播放的内容
     *
     * @param action
     */
    private void doUnFavour(RxAction action) {
        //判断当前播放的是音乐还是电台

        Audio currentAudio = AudioPlayer.getDefault().getCurrentAudio();
        if (currentAudio != null) {
            if (AudioUtils.isSong(currentAudio.sid)) {
                //当前播放的是音乐
                invokeUnFavour(action, AudioConverts.convertAudio2FavourAudio(AudioConverts.convert2Audio(currentAudio)));

                Boolean isWakeup = (Boolean) action.data.get(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP);
                if (isWakeup != null && isWakeup) {
                    TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_FAVOUR_CANCEL", Constant.RS_VOICE_SPEAK_ASR_FAVOUR_CANCEL);
                }
            } else {
                //当前播放的是电台
                //不处理,交给Subscribe自己处理
            }
        }

    }

    /**
     * 收藏当前播放的内容
     *
     * @param action
     */
    private void doFavour(RxAction action) {
        //判断当前播放的是音乐还是电台

        Audio currentAudio = AudioPlayer.getDefault().getCurrentAudio();
        if (currentAudio != null) {
            if (AudioUtils.isSong(currentAudio.sid)) {
                //当前播放的是音乐
                invokeFavour(action, AudioConverts.convertAudio2FavourAudio(AudioConverts.convert2Audio(currentAudio)));

                Boolean isWakeup = (Boolean) action.data.get(Constant.FavourConstant.KEY_FAVOUR_IS_WAKEUP);
                if (isWakeup != null && isWakeup) {
                    TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_FAVOUR", Constant.RS_VOICE_SPEAK_ASR_FAVOUR);
                }

            } else {
                //当前播放的是电台
                //不处理,交给Subscribe自己处理
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
    private void getFavourData(RxAction action) {
        Disposable subscribe = Observable.create(emitter -> {
            //1. 显示本地Favour表数据。获取本地Favour表数据
            List<FavourAudio> favourAudios = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().listAll();
            AudioUtils.removeLocalNotExists(favourAudios);
            emitter.onNext(favourAudios);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).subscribe(result -> {
            action.data.put(Constant.FavourConstant.KEY_FAVOUR_AUDIOS, result);
        }, e -> {
            postRxError(action, e);
        }, () -> {
            //                    2. 同步BeSendData数据，直到为空。
            postRxData(action);
            //如果BeSendData有数据,则请求
            //如果BeSendData,以及Favour表中没有数据,则请求
            List<BeSendData> beSendDataList = DBUtils.getDatabase(GlobalContext.get()).getBeSendDataDao().listAll();
            if (CollectionUtils.isNotEmpty(beSendDataList)) {
                //全部重新请求
                reqFavour(action, beSendDataList);
            } else {
                List<FavourAudio> result = (List<FavourAudio>) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIOS);
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
    private static long getFavourAudiosLastTime(List<TXZRespFavour.FavourBean> responseFavourBeans, int needSize) {
        if (CollectionUtils.isNotEmpty(responseFavourBeans) && responseFavourBeans.size() == needSize) {
            return responseFavourBeans.get(responseFavourBeans.size() - 1).operTime;
        } else {
            return 0;
        }
    }


    private void getFavourDataFromNet(RxAction action, FavourAudio favourAudio) {
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
        Disposable subscribe = mTXZMusicApi.getFavours(reqFavour).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(txzRespFavour -> {
            FavourAudioDao favourAudioDao = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao();
            //信赖后台返回的数据
            //1. 将本地的相应时间戳内的Favour数据删除,除了本地，sid !=0 的情况（因为媒资匹配）和微信推送 sid != 24
            long endTime = getFavourAudiosLastTime(txzRespFavour.arrAudioStore, Configuration.DefVal.REQ_FAVOUR_SIZE);
            favourAudioDao.invokeBySql(FavourAudio.getDeleteSupportSQLiteQuery(txzRespFavour.operTime, endTime));


            //经过数据库的洗礼
            List<FavourAudio> favourAudios = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(txzRespFavour.arrAudioStore)) {
                //遍历
                for (TXZRespFavour.FavourBean favourBean : txzRespFavour.arrAudioStore) {
                    FavourAudio favourAudio1 = AudioConverts.convertAudio2FavourAudio(AudioConverts.convert2Audio(favourBean.audio), favourBean.operTime);
                    favourAudios.add(favourAudio1);
                }
                favourAudioDao.saveOrUpdate(favourAudios);
            }
            SharedPreferencesUtils.setHasSyncFavourData(true);
            //在从数据库里面去
            List<FavourAudio> favourAudios1 = favourAudioDao.invokeBySql(FavourAudio.getSelectSupportSQLiteQuery(txzRespFavour.operTime, endTime));
            return Observable.just(favourAudios1);
        }).observeOn(Schedulers.io()).subscribe(txzRespFavour -> {
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

            List<FavourAudio> favourAudioList = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().listAll();
            AudioUtils.removeLocalNotExists(favourAudioList);
            action.data.put(Constant.FavourConstant.KEY_FAVOUR_AUDIOS, favourAudioList);
            postRxData(action);
        }, e -> postRxError(action, e));
        addRxAction(action, subscribe);

    }


    private void invokeUnFavour(RxAction action, FavourAudio favourAudio) {
        Disposable subscribe = Observable.create(emitter -> {
            changeTimestamp2Now(favourAudio);
            if (AudioUtils.isLocalSong(favourAudio.sid) || AudioUtils.isPushItem(favourAudio.sid)) {
                //不保存本地相关的数据
            } else {
                //保存数据到待发送的数据库
                saveSendData2DB(favourAudio, BeSendData.UNFAVOUR);
            }
            //保存到订阅的数据库中(用于后面直接查找)
            DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().delete(favourAudio);

            emitter.onNext(favourAudio);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {

            postRxData(RxAction.type(ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR).bundle(Constant.FavourConstant.KEY_FAVOUR_AUDIO, favourAudio).build());

        }, e -> postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG)));
        addRxAction(action, subscribe);
    }

    /**
     * 执行收藏的操作
     *
     * @param action      传递的数据
     * @param favourAudio 收藏的曲目
     */
    private void invokeFavour(RxAction action, FavourAudio favourAudio) {
        Disposable subscribe = Observable.create(emitter -> {
            changeTimestamp2Now(favourAudio);
            if (AudioUtils.isLocalSong(favourAudio.sid) || AudioUtils.isPushItem(favourAudio.sid)) {
                //不保存本地相关的数据
            } else {
                //保存数据到待发送的数据库
                saveSendData2DB(favourAudio, BeSendData.FAVOUR);
            }
            //保存到订阅的数据库中(用于后面直接查找)
            DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().saveOrUpdate(favourAudio);

            emitter.onNext(favourAudio);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
            postRxData(RxAction.type(ActionType.ACTION_FAVOUR_EVENT_FAVOUR).bundle(Constant.FavourConstant.KEY_FAVOUR_AUDIO, favourAudio).build());
//            //从待发送的数据库中删除数据
//            action.data.put(Constant.FavourConstant.KEY_FAVOUR_AUDIO, result);
//
//            //发起请求
//            reqFavour(action, (BeSendData) result);
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
     * @param favourAudio 待保存到数据库中的数据
     * @return 可以同步到后台的请求数据
     */
    private BeSendData saveSendData2DB(FavourAudio favourAudio, @BeSendData.OperationType int operationType) {
        //保存到待发送的数据库中
        BeSendData sendData = AudioConverts.convert2BeSendData(favourAudio, operationType);
        DBUtils.getDatabase(GlobalContext.get()).getBeSendDataDao().saveOrUpdate(sendData);
        return sendData;
    }


    /**
     * 修改操作时间为现在
     *
     * @param favourAudio 待修改的audio
     */
    private void changeTimestamp2Now(FavourAudio favourAudio) {
        if (favourAudio != null) {
            favourAudio.timestamp = TimeManager.getInstance().getTimeMillis();
        }
    }

}
