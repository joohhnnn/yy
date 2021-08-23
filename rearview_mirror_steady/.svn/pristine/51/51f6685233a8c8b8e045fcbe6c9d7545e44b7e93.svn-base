package com.txznet.music.model;

import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.Constant;
import com.txznet.music.ErrCode;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.PushItemDao;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.AudioUtils;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author telen
 * @date 2018/12/22,11:11
 */
public class WxPushModel extends RxWorkflow {

    private TXZMusicApi mTXZMusicApi = TXZMusicApiImpl.getDefault();

    public WxPushModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_WXPUSH_EVENT_GET:
                getWxPushData(action);
                break;
            //可以直接被store接受到
//            case ActionType.ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO:
//                updateQrcodeInfo(action);
//                break;
            case ActionType.ACTION_WXPUSH_EVENT_GET_QRCODE:
                getWxPushQRCode(action);
                break;
            case ActionType.ACTION_WXPUSH_EVENT_SAVE:
                savePushItem(action, (List<PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS));
                break;
            case ActionType.ACTION_WXPUSH_EVENT_DELETE:
                deletePushItems(action, (List<PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS));
                break;
            case ActionType.ACTION_PLAYER_ON_INFO_CHANGE:
                AudioV5 audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                if (audioV5 != null) {
                    if (PlayScene.WECHAT_PUSH == PlayHelper.get().getCurrPlayScene() || AudioUtils.isPushItem(audioV5.sid)) {
                        markPushItemkReaded(audioV5.sid, audioV5.id);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void markPushItemkReaded(int sid, long id) {
        Disposable subscribe = Observable.create(emitter -> {
            PushItemDao pushItemDao = DBUtils.getDatabase(GlobalContext.get()).getPushItemDao();
            PushItem wxPush = pushItemDao.findBySidAndId(sid, id);
            if (wxPush != null) {
                wxPush.status = PushItem.STATUS_READ;
                pushItemDao.saveOrUpdate(wxPush);
            }
            emitter.onNext(wxPush);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            //原封不动的返回
        }, throwable -> {
        });
    }

    private void deletePushItems(RxAction action, List<PushItem> pushItems) {
//        Disposable subscribe = Observable.create(emitter -> {
//
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
//
//        }, throwable -> {
//
//        });

        Disposable subscribe = Observable.create(emitter -> {
            DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().delete(pushItems);
            emitter.onNext(pushItems);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            //原封不动的返回
            postRxData(action);
        }, throwable -> {
            postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG, throwable));
        });
        addRxAction(action, subscribe);
    }

    private void savePushItem(RxAction action, List<PushItem> pushItems) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<PushItem>>) emitter -> {
            DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().saveOrUpdate(pushItems);
//            if (resultCode > 0) {
            //保存成功
            emitter.onNext(pushItems);
//            } else {
//                emitter.onError(new RuntimeException("save pushItem error:" + resultCode));
//            }
            emitter.onComplete();

        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(pushItem1 -> {
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG, throwable));
                });
        addRxAction(action, subscribe);
    }


    /**
     * 获取微信推送
     *
     * @param action
     */
    private void getWxPushData(RxAction action) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<List<PushItem>>) emitter -> {
            emitter.onNext(DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().listAll());
            emitter.onComplete();
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(pushItems -> {
                    action.data.put(Constant.WxPushConstant.KEY_AUDIOS, pushItems);
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, new Error(ErrCode.ERROR_INNER_WRONG, throwable));
                });
        addRxAction(action, subscribe);
    }


    public void getWxPushQRCode(RxAction action) {
        mTXZMusicApi.getWxLinkQRCode();
    }
}
