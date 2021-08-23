package com.txznet.music.push;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.INormalCallback;
import com.txznet.music.data.http.AudioRepository;
import com.txznet.music.push.bean.PushResponse;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.txz.util.runnables.Runnable3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.sdk.music.MusicInvokeConstants.*;

/**
 * 推送的拦截类,提供给三方用的中间层
 */
public class PushIntercepter {

    private static final String TAG = "music:push:intercept:";

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
    private volatile static PushIntercepter singleton;

    private PushIntercepter() {
    }

    public static PushIntercepter getInstance() {
        if (singleton == null) {
            synchronized (PushIntercepter.class) {
                if (singleton == null) {
                    singleton = new PushIntercepter();
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
    private boolean isIntercepte() {
        return (mInterceptType & currentType) != 0;
    }

    private int currentType = 0;
    private boolean isShowIntercept = false;

    public void showData(int type, PushResponse response, INormalCallback<Boolean> booleanINormalCallback) {
        currentType = type;
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

    private Runnable3 delayTask = null;

    public void resumeDelayTask() {
        Logger.d("PushIntercepter", "resumeDelayTask:" + delayTask);
        if (delayTask != null) {
            delayTask.run();
        }
    }

    private void doShowDataInner(int type, final PushResponse response, final INormalCallback<Boolean> booleanINormalCallback) {
        if (isIntercepte()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put(KEY_TITLE, response.getTitle());
            jsonBuilder.put(KEY_SUB_TITLE, response.getSubTitle());
            if (CollectionUtils.isNotEmpty(response.getArrKeys())) {
                jsonBuilder.put(KEY_PUSH_CONFIRM, response.getArrKeys().get(0).getText());
                jsonBuilder.put(KEY_PUSH_CANCEL, response.getArrKeys().get(1).getText());
            }
            jsonBuilder.put(KEY_PUSH_ICON, response.getIconUrl());

            //需要对方来表明是否需要拦截，同步调用
            sendDataIndicateIntercept(INVOKE_PREFIX_TONGTING_PUSH + PUSH_SHOW_DATA, jsonBuilder.toBytes(), booleanINormalCallback);
        } else {
            booleanINormalCallback.onSuccess(false);
            Logger.d(TAG, "showView:intercept:false");
        }
    }

    private void sendMoreAudios(PushResponse response) {
        Album album = null;
        List<Audio> audios = null;
        switch (response.getPostAction()) {
            case PushResponse.POST_ACTION_PLAY_ALBUM:
                album = new Album();
                album.setSid((int) response.getSid());
                album.setId(Long.parseLong(response.getPostFlag()));
                album.setName(response.getAlbumName());
                break;
            case PushResponse.POST_ACTION_PLAY_ALBUMS:
                List<PushResponse.AlbumWrapper> arrAlbumWrappers = response.getArrAlbumWrappers();
                if (null == arrAlbumWrappers || arrAlbumWrappers.isEmpty()) {
                    LogUtil.e(TAG, "arrAlbumWrappers is empty!");
                    break;
                }
                if (arrAlbumWrappers.size() == 1) {
                    album = arrAlbumWrappers.get(0).getAlbum();
                }
                break;
            case PushResponse.POST_ACTION_PLAY_MUSIC_LIST:
                audios = response.getArrAudio();
                if (null == audios || audios.isEmpty()) {
                    LogUtil.e(TAG, "audios is empty!");
                    break;
                }

                break;
            default:
                break;
        }
        if (album != null) {

            AudioRepository.getInstance().getAudios(album, null, true, false).subscribeOn(Schedulers.io()).subscribe(new Consumer<List<Audio>>() {
                @Override
                public void accept(List<Audio> audios) throws Exception {
                    if (CollectionUtils.isNotEmpty(audios)) {
                        onNextAudios(audios);
                        return;
                    }
                    Logger.w(TAG, "error:onNextAudios:showData:null哦");
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
//                    Logger.w(TAG, "error:onNextAudios:" + error.toString());
                    Logger.w(TAG, "error:onNextAudios:showData:" + throwable.getMessage());
                    onNextAudios(null);
                }
            });

        } else if (CollectionUtils.isNotEmpty(audios)) {
            onNextAudios(audios);
        }
    }

    public void showView(final INormalCallback<Boolean> booleanINormalCallback) {
        if (isIntercepte()) {
            sendDataIndicateIntercept(INVOKE_PREFIX_TONGTING_PUSH + PUSH_SHOW_VIEW, null, booleanINormalCallback);
        } else {
            booleanINormalCallback.onSuccess(false);
            Logger.d(TAG, "showView:intercept:false");
        }
    }

    private void sendDataIndicateIntercept(final String command, final byte[] data, final INormalCallback<Boolean> booleanINormalCallback) {
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
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
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                booleanINormalCallback.onSuccess(aBoolean);
                Logger.d(TAG, "showView:intercept:" + aBoolean);
            }
        });
    }

    public boolean dismissView() {
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH + PUSH_DISMISS_VIEW, null, null);
        return isShowIntercept;
    }

    public boolean needWait() {
        ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH + PUSH_IS_NEED_WAIT, null);
        if (data != null) {
            return data.getBoolean();
        }
        return false;
    }

    public void clickContinue() {
        PushManager.getInstance().clickContinue();
    }

    public void clickCancel() {
        PushManager.getInstance().clickCancel();
    }

    public void onProgressChange(long progress, long duration) {

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_PROGRESS, progress);
        jsonBuilder.put(KEY_DURATION, duration);

        ServiceManager.getInstance().sendInvoke(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + PUSH_PROGRESS, jsonBuilder.toBytes(), null);
    }

    public void onInfoChange(Audio data) {
        if (data == null) {
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_INFO, data.getName());
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + PUSH_INFO, jsonBuilder.toBytes(), null);
    }

    public void onStatusChange(int status) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(KEY_STATUS, status);
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH_CALLBACK_STATUS + PUSH_STATUS, jsonBuilder.toBytes(), null);
    }

    public void onNextAudios(List<Audio> audios) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        if (CollectionUtils.isNotEmpty(audios)) {
            JSONArray jsonArray = new JSONArray();
            for (Audio audio : audios) {
                if (audio != null) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(KEY_TITLE, audio.getName());
                        jsonObject.put(KEY_ARTISTS, audio.getArrArtistName());
                        jsonObject.put(KEY_ALBUM_NAME, audio.getAlbumName());
                        jsonArray.put(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            jsonBuilder.put(KEY_NEXT_AUDIOS, jsonArray);
        }
        Logger.d(TAG, "onNextAudios:nextAudios:" + jsonBuilder.toString());
        ServiceManager.getInstance().sendInvoke(mInterceptPackage, INVOKE_PREFIX_TONGTING_PUSH_NEXT_AUDIOS + PUSH_NEXT_AUDIOS, jsonBuilder.toBytes(), null);
    }

    public boolean regCmd() {

        return false;
    }
}
