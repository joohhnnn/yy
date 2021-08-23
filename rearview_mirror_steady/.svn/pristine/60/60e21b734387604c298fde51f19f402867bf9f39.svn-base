package com.txznet.music.model;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqAudio;
import com.txznet.music.data.http.api.txz.entity.resp.PullData;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.util.Logger;
import com.txznet.sdk.music.MusicInvokeConstants;
import com.txznet.txz.util.runnables.Runnable3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 推送的拦截类,提供给三方用的中间层
 */
public class PushInterceptor {

    private static final String TAG = Constant.LOG_TAG_PUSH + ":Interceptor";

    /**
     * 新闻推送
     */
    public final static int INTERCEPT_NEWS = 1;
    /**
     * 专辑更新提醒
     */
    public final static int INTERCEPT_UPDATE = 1 << 1;
    /**
     * 微信推动
     */
    public final static int INTERCEPT_AUDIO = 1 << 2;


    //##创建一个单例类##
    private volatile static PushInterceptor singleton;

    private PushInterceptor() {
    }

    public static PushInterceptor getInstance() {
        if (singleton == null) {
            synchronized (PushInterceptor.class) {
                if (singleton == null) {
                    singleton = new PushInterceptor();
                }
            }
        }
        return singleton;
    }

    private int mInterceptType = 0;
    private String mInterceptPackage = "";

    public void setListener(String packageName, int interceptType) {
        mInterceptType = interceptType;
        mInterceptPackage = packageName;
        Logger.d(TAG, "init:listener:" + mInterceptType + "/" + mInterceptPackage);

        //下面会使用到同步调用，同步调用之前必须要先绑定

        ServiceManager.getInstance().sendInvoke(mInterceptPackage, "test", null, new ServiceManager.GetDataCallback() {
            @Override
            public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                Logger.d(TAG, "init:listener:bind:success:" + data);
            }
        });
    }

    private boolean isNeedMoreAudio = false;

    /**
     * 设置需要请求Audios
     *
     * @param needAudiosInfo
     */
    public void setNeedAudios(boolean needAudiosInfo) {
        isNeedMoreAudio = needAudiosInfo;
    }

    public boolean isNeedMoreAudios() {
        return isNeedMoreAudio;
    }


    public void clearListener() {
        mInterceptType = 0;
    }

    /**
     * 是否需要交给三方工具拦截
     *
     * @return true 表明拦截
     */
    private boolean isIntercept() {
        return (mInterceptType & currentType) != 0;
    }

    private int currentType = 0;
    private boolean isShowIntercept = false;

    public void showData(int type, PushResponse response, INormalCallback<Boolean> booleanINormalCallback) {
        currentType = getTypeFromPullData(type);
        //这里做请求的逻辑
        if (isNeedMoreAudios()) {
            sendMoreAudios(response);
        }

        if (needWait()) {
            delayTask = new Runnable3<Integer, PushResponse, INormalCallback>(type, response, booleanINormalCallback) {
                @Override
                public void run() {
                    delayTask = null;
                    doShowDataInner(mP1, mP2, mP3);
                }
            };
        } else {
            doShowDataInner(type, response, booleanINormalCallback);
        }
    }

    private int getTypeFromPullData(int pullDataType) {
        if (pullDataType == PullData.TYPE_AUDIOS) {
            return INTERCEPT_AUDIO;
        } else if (pullDataType == PullData.TYPE_UPDATE) {
            return INTERCEPT_UPDATE;
        } else if (pullDataType == PullData.TYPE_NEWS) {
            return INTERCEPT_NEWS;
        }
        return 0;
    }

    private Runnable3 delayTask = null;

    public void resumeDelayTask() {
        Logger.d(TAG, "resumeDelayTask:" + delayTask);
        if (delayTask != null) {
            delayTask.run();
        }
    }

    private void doShowDataInner(int type, final PushResponse response, final INormalCallback<Boolean> booleanINormalCallback) {
        if (isIntercept()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put(MusicInvokeConstants.KEY_TITLE, response.getTitle());
            jsonBuilder.put(MusicInvokeConstants.KEY_SUB_TITLE, response.getSubTitle());
            if (CollectionUtils.isNotEmpty(response.getArrKeys())) {
                jsonBuilder.put(MusicInvokeConstants.KEY_PUSH_CONFIRM, response.getArrKeys().get(0).getText());
                jsonBuilder.put(MusicInvokeConstants.KEY_PUSH_CANCEL, response.getArrKeys().get(1).getText());
            }
            jsonBuilder.put(MusicInvokeConstants.KEY_PUSH_ICON, response.getIconUrl());

            //需要对方来表明是否需要拦截，同步调用
            sendDataIndicateIntercept(MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH + MusicInvokeConstants.PUSH_SHOW_DATA, jsonBuilder.toBytes(), booleanINormalCallback);
        } else {
            booleanINormalCallback.onSuccess(false);
            Logger.w(TAG, "showView:intercept:false");
        }
    }

    private void sendMoreAudios(PushResponse response) {
        Album album = null;
        List<AudioV5> audios = null;
        switch (response.getPostAction()) {
            case PushResponse.POST_ACTION_PLAY_ALBUM:
                album = new Album();
                album.sid = (int) response.getSid();
                album.id = Long.parseLong(response.getPostFlag());
                album.name = response.getAlbumName();
                break;
            case PushResponse.POST_ACTION_PLAY_ALBUMS:
                List<PushResponse.AlbumWrapper> arrAlbumWrappers = response.getArrAlbumWrappers();
                if (null == arrAlbumWrappers || arrAlbumWrappers.isEmpty()) {
                    Logger.e(TAG, "arrAlbumWrappers is empty!");
                    break;
                }
                if (arrAlbumWrappers.size() == 1) {
                    album = AlbumConverts.convert2Album(arrAlbumWrappers.get(0).getAlbum());
                }
                break;
            case PushResponse.POST_ACTION_PLAY_MUSIC_LIST:
                audios = AudioConverts.convert2List(response.getArrAudio(), AudioConverts::convert2Audio);
                if (null == audios || audios.isEmpty()) {
                    Logger.e(TAG, "audios is empty!");
                    break;
                }

                break;
            default:
                break;
        }
        if (album != null) {
            TXZReqAudio txzReqAudio = new TXZReqAudio();
            txzReqAudio.sid = album.sid;
            txzReqAudio.id = album.id;


            Disposable disposable = TXZMusicApiImpl.getDefault().getAudios(txzReqAudio).subscribeOn(Schedulers.io()).subscribe(txzRespAudio -> {

                if (CollectionUtils.isNotEmpty(txzRespAudio.arrAudio)) {
                    onNextAudios(AudioConverts.convert2List(txzRespAudio.arrAudio, AudioConverts::convert2Audio));
                    return;
                }
                Logger.w(TAG, "error:onNextAudios:showData:null");

            }, throwable -> {
//                    Logger.w(TAG, "error:onNextAudios:" + error.toString());
                Logger.w(TAG, "error:onNextAudios:showData:" + throwable.getMessage());
                onNextAudios(null);
            });

        } else if (CollectionUtils.isNotEmpty(audios)) {
            onNextAudios(audios);
        }

    }

    public void showView(final INormalCallback<Boolean> booleanINormalCallback) {
        if (isIntercept()) {
            sendDataIndicateIntercept(MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH + MusicInvokeConstants.PUSH_SHOW_VIEW, null, booleanINormalCallback);
        } else {
            booleanINormalCallback.onSuccess(false);
            Logger.d(TAG, "showView:intercept:false");
        }
    }

    private void sendDataIndicateIntercept(final String command, final byte[] data, final INormalCallback<Boolean> booleanINormalCallback) {
        Disposable subscribe = Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            boolean result = false;
            //需要对方来表明是否需要拦截，同步调用
            ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(mInterceptPackage, command, data);
            if (serviceData != null) {
                Boolean aBoolean = serviceData.getBoolean();
                if (aBoolean != null) {
                    result = aBoolean;
                    isShowIntercept = result;
                }
            } else {
                Logger.d(TAG, "showdata:intercept:bind:error:还没有绑定对方的进程" + mInterceptPackage);
            }

            e.onNext(result);
            e.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            booleanINormalCallback.onSuccess(aBoolean);
            Logger.d(TAG, "showView:intercept:" + aBoolean);
        });
    }

    public boolean dismissView() {
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH + MusicInvokeConstants.PUSH_DISMISS_VIEW, null, null);
        return isShowIntercept;
    }

    public boolean needWait() {
        ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH + MusicInvokeConstants.PUSH_IS_NEED_WAIT, null);
        if (data != null) {
            return data.getBoolean();
        }
        return false;
    }

    public void clickContinue() {
        // TODO: 2018/12/27 方法没有实现
//        PushManager.getInstance().clickContinue();
    }

    public void clickCancel() {
        // TODO: 2018/12/27 方法没有实现
//        PushManager.getInstance().clickCancel();
    }

    public void onProgressChange(long progress, long duration) {

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(MusicInvokeConstants.KEY_PROGRESS, progress);
        jsonBuilder.put(MusicInvokeConstants.KEY_DURATION, duration);

        ServiceManager.getInstance().sendInvoke(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + MusicInvokeConstants.PUSH_PROGRESS, jsonBuilder.toBytes(), null);
    }

    public void onInfoChange(AudioV5 data) {
        if (data == null) {
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(MusicInvokeConstants.KEY_INFO, data.name);
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + MusicInvokeConstants.PUSH_INFO, jsonBuilder.toBytes(), null);
    }

    public void onStatusChange(int status) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(MusicInvokeConstants.KEY_STATUS, status);
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + MusicInvokeConstants.PUSH_STATUS, jsonBuilder.toBytes(), null);
    }

    public void onNextAudios(List<AudioV5> audios) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        if (CollectionUtils.isNotEmpty(audios)) {
            JSONArray jsonArray = new JSONArray();
            for (AudioV5 audio : audios) {
                if (audio != null) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(MusicInvokeConstants.KEY_TITLE, audio.name);
                        jsonObject.put(MusicInvokeConstants.KEY_ARTISTS, audio.artist);
                        jsonObject.put(MusicInvokeConstants.KEY_ALBUM_NAME, audio.albumName);
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            jsonBuilder.put(MusicInvokeConstants.KEY_NEXT_AUDIOS, jsonArray);
        }
        Logger.d(TAG, "onNextAudios:nextAudios:" + jsonBuilder.toString());
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, MusicInvokeConstants.INVOKE_PREFIX_TONGTING_PUSH_NEXT_AUDIOS + MusicInvokeConstants.PUSH_NEXT_AUDIOS, jsonBuilder.toBytes(), null);
    }

    public boolean regCmd() {

        return false;
    }
}
