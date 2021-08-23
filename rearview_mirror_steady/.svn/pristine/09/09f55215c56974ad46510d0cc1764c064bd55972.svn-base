package com.txznet.music.service.push;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.WxPushActionCreator;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.http.api.txz.entity.TXZAlbum;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.ProxyHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.service.push.player.PushPlayerHelper;
import com.txznet.music.ui.push.PushNotification;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.Operation;
import com.txznet.sdk.TXZAsrManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author telen
 * @date 2018/12/27,15:25
 */
public class AppPushInvoker implements IPushInvoker {
    public static final String TAG = Constant.LOG_TAG_PUSH + ":PushInvoker";

    /**
     * 单例对象
     */
    private static AppPushInvoker singleton = new AppPushInvoker();


    private AppPushInvoker() {
    }

    public static AppPushInvoker getInstance() {
        return singleton;
    }

    private PushNotification view;

    private AudioPlayer mAudioPlayer;

    private PushResponse mPushResponse;

    private static final String ASR_CMD_LISTEN = "CMD_LISTEN";
    private static final String ASR_CMD_CLOSE = "CMD_CLOSE";

    private static final String ASR_TASK_ID = "ASR_TASK_ID_QUICK_REPORT";

    private AudioV5 mAudioV5;

    //为了加快遍历速度
    private boolean isOk = false;

    private PlayListData mPlayListData;

    public synchronized AudioPlayer getAudioPlayer() {
        if (mAudioPlayer == null) {
            mAudioPlayer = PushPlayerHelper.getInstance().createPlayer("", true);
        }
        return mAudioPlayer;
    }

    @Override
    public synchronized void initData(PushResponse pushResponse, PlayListData data) {
        mPushResponse = pushResponse;
        mPlayListData = data;
    }

    @Override
    public View getView() {
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "getView:" + view);
        }

        int style = PushNotification.STYLE_DOUBLE;
        //产品说：显示二栏信息有条件，相同专辑或相同内容则不弹
        boolean isNeedSecondInfo = true;
        do {
            if (mPushResponse.getService().equals(PushResponse.PUSH_SERVICE_STORY)) {
                break;
            }
            if (mPushResponse.getService().equals(PushResponse.PUSH_LOCAL_COMMAND)) {
                isNeedSecondInfo = false;
                break;
            }
            if (mPlayListData == null) {
                isNeedSecondInfo = false;
                break;
            }
            Album album = mPlayListData.album;
            if (album != null) {
                // 判断推送专辑是否与上次播放一致
                List<PushResponse.AlbumWrapper> arrAlbumWrappers = mPushResponse.getArrAlbumWrappers();
                if (arrAlbumWrappers != null && arrAlbumWrappers.size() > 0) {

                    PushResponse.AlbumWrapper albumWrapper = arrAlbumWrappers.get(0);
                    TXZAlbum album1 = albumWrapper.getAlbum();
                    if (BuildConfig.DEBUG) {
                        Logger.d(TAG, "getView：" + album1 + " , " + album);
                    }
                    if (album1.sid == album.sid && album1.id == album.id) {
                        isNeedSecondInfo = false;
                    }
                }

                // 推送的音频是微信，且上次播放的是新闻，不显示次栏
                if (AlbumUtils.isNews(album)) {
                    List<TXZAudio> arrAudio = mPushResponse.getArrAudio();
                    if (arrAudio != null && arrAudio.size() > 0) {
                        TXZAudio audio = arrAudio.get(0);
                        if (AudioUtils.isPushItem(audio.sid)) {
                            isNeedSecondInfo = false;
                        }
                    }
                }
            } else {
                // 判断推送音频是否与上次播放一致
                Audio audio1 = mPlayListData.audio;
                if (audio1 != null) {
                    List<TXZAudio> arrAudio = mPushResponse.getArrAudio();
                    if (arrAudio != null && arrAudio.size() > 0) {
                        TXZAudio audio = arrAudio.get(0);
                        if (BuildConfig.DEBUG) {
                            Logger.d(TAG, "getView：" + audio1 + " , " + audio);
                        }
                        if (audio1.sid == audio.sid && audio1.id == audio.id) {
                            isNeedSecondInfo = false;
                        }
                        // 微信特殊处理，内容不一致也只显示一栏
                        if (AudioUtils.isPushItem(audio1.sid) && AudioUtils.isPushItem(audio.sid)) {
                            isNeedSecondInfo = false;
                        }
                    }
                }
            }
        } while (false);

        if (!isNeedSecondInfo) {
            style = PushNotification.STYLE_SINGLE;
            view = new PushNotification(style);
        } else {
            if (TextUtils.equals(mPushResponse.getService(), PushResponse.PUSH_SERVICE_STORY)) {
                view = new PushNotification(style);
            } else {
                PushItemData pushItemData = PushItemDataFactory.getPushItemData(mPlayListData);
                // 推送内容标题为空，显示一栏
                if (com.txznet.comm.util.StringUtils.isEmpty(pushItemData.title)) {
                    style = PushNotification.STYLE_SINGLE;
                }
                view = new PushNotification(style);
                if (com.txznet.comm.util.StringUtils.isNotEmpty(pushItemData.title)) {
                    if (com.txznet.comm.util.StringUtils.isEmpty(pushItemData.iconUrl)) {
                        view.setSecondIcon(pushItemData.iconUrlId);
                    } else {
                        view.setSecondIcon(pushItemData.iconUrl);
                    }
                    view.setSecondTitle(pushItemData.title);
                    view.setOnItemSecondClickListener(v -> {
                        invokeSecondItemClick(Operation.MANUAL, pushItemData);
                    });
                }
            }
        }
        //优先级更高
        if (TextUtils.equals(mPushResponse.getService(), PushResponse.PUSH_SERVICE_STORY)) {
            view.setSecondTitle(mPushResponse.getSubTitle());
            view.setSecondIcon(R.drawable.window_guess_like_icon);
            view.setOnItemSecondClickListener(v -> {
                onClickSecondItem(Operation.MANUAL);
            });
        }
        view.setCloseText(mPushResponse.getArrKeys().get(1).getText());
        if (PushResponse.PUSH_LOCAL_COMMAND.equals(mPushResponse.getService())) {
            view.setFirstIcon(R.drawable.window_local_icon);
        } else if (com.txznet.comm.util.StringUtils.isNotEmpty(mPushResponse.getIconUrl())) {
            view.setFirstIcon(mPushResponse.getIconUrl());
        } else {
            view.setFirstIcon(R.drawable.window_launch_icon);
        }
        view.setFirstTitle(mPushResponse.getTitle());
        view.setOnItemFirstClickListener(view -> {
            onClickFirstItem(Operation.MANUAL);
        });


        view.setCloseListener(v -> {
            ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_VOICE_AI_CANCEL);
            onCancel(true);
        });


        return view;
    }

    @Override
    public synchronized boolean isShowing() {
        return view != null && view.isShow();
    }

    // 次栏点击处理
    private synchronized void invokeSecondItemClick(Operation operation, PushItemData pushItemData) {
        ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_AI_SUB_BAR);
        onCancel(false);
        if (pushItemData.playListData.album == null) {
            if (pushItemData.playListData.scene != null) {
                switch (pushItemData.playListData.scene) {
                    case LOCAL_MUSIC: // 本地音乐
                        PlayerActionCreator.get().playLocal(operation, AudioConverts.convert2Audio(pushItemData.playListData.audio));
                        break;
                    case FAVOUR_MUSIC: // 收藏音乐
                        PlayerActionCreator.get().playFavour(operation, AudioConverts.convert2Audio(pushItemData.playListData.audio));
                        break;
                    case HISTORY_MUSIC: // 历史音乐
                        PlayerActionCreator.get().playHistoryMusic(operation, AudioConverts.convert2Audio(pushItemData.playListData.audio));
                        break;
                    case WECHAT_PUSH: // 微信推送
                        PlayerActionCreator.get().playWxPush(operation, AudioConverts.convert2Audio(pushItemData.playListData.audio));
                        break;
                    case AI_RADIO: // AI电台
                        PlayerActionCreator.get().playAi(operation);
                        break;

                }
            }
            PlayerActionCreator.get().play(Operation.MANUAL);
        } else {
            if (AlbumUtils.isMusic(pushItemData.playListData.album)) {
                PlayHelper.get().playListData(pushItemData.playListData, true);
            } else {
                PlayerActionCreator.get().playAlbum(Operation.MANUAL, pushItemData.playListData.album);
            }
        }
    }

    @Override
    public synchronized void onShowView() {
        if (mPushResponse == null) {
            return;
        }
        ((PushNotification) getView()).show();
    }

    @Override
    public synchronized void onClickFirstItem(Operation operation) {
        Logger.d(TAG, "onClickFirstItem, operation=" + operation);
        invokePush(operation);
        saveMessage(PushItem.STATUS_READ);
        //释放所有资源
        release();
    }

    private synchronized void invokePush(Operation operation) {
        if (mPushResponse != null) {
            // 首栏是自动播放的内容；
            // 次栏点击肯定是打断操作；
            // 点击首栏横幅会消失；
            // 所以横幅打开状态，如果当前有内容播放，能触发播放肯定是点击了首栏
            if (isShowing()
                    && (PlayHelper.get().getCurrAudio() != null || PlayHelper.get().getCurrAlbum() != null)) {
                Logger.w(TAG, "invokePush pass, already play");
                return;
            }

            Logger.d(TAG, "invokePush:" + mPushResponse.toString());
            ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_AI);
            // 微信推送
            if (PushResponse.PUSH_SERVICE_AUDIOS.equals(mPushResponse.getService())) {
                List<PushItem> pushItem = getPushItem(PushItem.STATUS_READ);
                if (PlaySceneUtils.isAiScene()) {
                    // 当前为AI模式，则打断当前播放内容，播放微信推送的内容，
                    int size = pushItem.size();
                    if (size > 1) {
                        playWxScreenData(operation, pushItem);
                    } else if (size == 1) {
                        PlayerActionCreator.get().playImmediately(operation, pushItem.get(0));
                    } else {
                        Logger.w(TAG, "push item is empty");
                    }
                } else {
                    //当前为普通模式，则播放列表更新为微信推送内容列表。
                    playWxScreenData(operation, pushItem);
                }
            } else if (PushResponse.PUSH_SERVICE_SHORT_PLAY.equals(mPushResponse.getService())) {
                if (mPushResponse.getArrAlbumWrappers() != null) {
                    if (mPushResponse.getArrAlbumWrappers().get(0) != null) {
                        if (mPushResponse.getArrAlbumWrappers().get(0).getAlbum() != null) {
                            Album album = AlbumConverts.convert2Album(mPushResponse.getArrAlbumWrappers().get(0).getAlbum());
                            if (album.logo == null) {
                                album.logo = mPushResponse.getIconUrl();
                            }
                            PlayerActionCreator.get().playAlbum(operation, album);
                        }
                    }
                }
            } else if (PushResponse.PUSH_LOCAL_COMMAND.equals(mPushResponse.getService())) {
                PlayerActionCreator.get().playLocalWithLastPlay(operation);
            } else if (PushResponse.PUSH_SERVICE_FORCE_PLAY.equals(mPushResponse.getService())) {
                // 未知逻辑
            } else if (PushResponse.PUSH_SERVICE_UPDATE.equals(mPushResponse.getService())) {
                if (mPushResponse.getArrAlbumWrappers() != null) {
                    if (mPushResponse.getArrAlbumWrappers().get(0) != null) {
                        if (mPushResponse.getArrAlbumWrappers().get(0).getAlbum() != null) {
                            Album album = AlbumConverts.convert2Album(mPushResponse.getArrAlbumWrappers().get(0).getAlbum());
                            if (album.logo == null) {
                                album.logo = mPushResponse.getIconUrl();
                            }
                            PlayerActionCreator.get().playAlbum(operation, album);
                        }
                    }
                }
            } else if (PushResponse.PUSH_SERVICE_STORY.equals(mPushResponse.getService())) {
                //  播放小说
                if (mPushResponse.getArrAlbumWrappers() != null) {
                    if (mPushResponse.getArrAlbumWrappers().get(0) != null) {
                        if (mPushResponse.getArrAlbumWrappers().get(0).getAlbum() != null) {
                            Album album = AlbumConverts.convert2Album(mPushResponse.getArrAlbumWrappers().get(0).getAlbum());
                            if (album.logo == null) {
                                album.logo = mPushResponse.getIconUrl();
                            }
                            PlayerActionCreator.get().playAlbum(operation, album);
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized void onClickSecondItem(Operation operation) {
        Logger.d(TAG, "onClickSecondItem, operation=" + operation);
        Album album = mPushResponse.getSubAlbum();
        onCancel(false);
        if (album == null) {
            PlayerActionCreator.get().playAi(Operation.MANUAL);
        } else {
            PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
        }
        ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_AI_SUB_BAR);
    }

    /**
     * 播放微信推送
     *
     * @param pushItems
     */
    private synchronized void playWxScreenData(Operation operation, List<PushItem> pushItems) {
        Observable.create(emitter -> {
            //查询数据库
            Logger.d(TAG, "playWxScreenData:original:" + CollectionUtils.toString(pushItems));

            List<PushItem> pushItems1 = DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().listAll();
            pushItems1.removeAll(pushItems);
            pushItems1.addAll(0, pushItems);

            Logger.d(TAG, "playWxScreenData:distinct:" + CollectionUtils.toString(pushItems1));

            PlayerActionCreator.get().playWxPush(operation, pushItems1, 0);
            emitter.onComplete();
        }).observeOn(Schedulers.io()).subscribeOn(Schedulers.io())
                .subscribe();

    }


    /**
     * 保存数据库
     *
     * @param read 是否已读
     */
    private synchronized void saveMessage(int read) {
        WxPushActionCreator.getInstance().saveWxPushData(getPushItem(read));
    }

    private synchronized List<PushItem> getPushItem(int read) {
        return PushUtils.getPushItem(mPushResponse, audio -> {
            if (!isOk &&
                    ((audio == null && mAudioV5 == null)
                            || (audio != null && mAudioV5 != null && audio.sid == mAudioV5.sid && audio.id == mAudioV5.id))) {
                isOk = true;
                return AudioConverts.convert2PushItem(audio, read);
            }
            return AudioConverts.convert2PushItem(audio, PushItem.STATUS_UNREAD);
        });
    }

    @Override
    public synchronized void onCancel(boolean shouldExit) {
        if (mPushResponse == null) {
            return;
        }
        Logger.d(TAG, "onCancel shouldExit=" + shouldExit);
        saveMessage(PushItem.STATUS_UNREAD);
        release();
        if (shouldExit) {
            Utils.exitApp(null, false);
        }
    }

    @Override
    public synchronized void onPlayBegin(AudioV5 audio) {
        mAudioV5 = audio;
        getAudioPlayer().play(AudioConverts.convert2MediaAudio(audio));
        getAudioPlayer().setAudioPlayerStateChangeListener(new AudioPlayer.AudioPlayerStateChangeListener() {
            @Override
            public void onAudioChanged(Audio audio, boolean willEnd) {

            }

            @Override
            public void onQueuePlayEnd() {

            }

            @Override
            public void onPlayStateChanged(int state) {

            }

            @Override
            public void onProgressChanged(long position, long duration) {

            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onCompletion() {
                Logger.d(TAG, "player:onCompletion: ");
                onPlayEnd();
            }

            @Override
            public void onError(Error error) {
                Logger.d(TAG, "player:onError: ");
                onPlayEnd();
            }
        });
    }


    Disposable subscribe;

    @Override
    public synchronized void onPlayEnd() {
        if (mPushResponse == null) {
            return;
        }
        if (!TextUtils.isEmpty(mPushResponse.getEndTip()) && PushUtils.checkTipFileExists(mPushResponse.getEndTip())) {
            PushUtils.speakText(() -> TtsUtil.speakVoice(PushUtils.getCacheFile(mPushResponse.getEndTip()).getAbsolutePath(), new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    onPlayEndInner();
                }
            }));
        } else {
            onPlayEndInner();
        }
    }

    private synchronized void onPlayEndInner() {
        if (mPushResponse == null) {
            return;
        }
        //开启倒计时
        startCountDown();
        //注册免唤醒词
        registerAsrCmd(mPushResponse);
        invokePush(Operation.AUTO);
    }

    int count = 0;

    /**
     * 开启倒计时
     */
    private synchronized void startCountDown() {
        Logger.d(TAG, "startCountDown");

        count = 11;
        subscribe = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    Logger.d(TAG, "startCountDown：" + count + "," + (view != null) + "," + (count <= 0));

                    //设置倒计时时间
                    if (--count <= 0) {
                        if (!subscribe.isDisposed()) {
                            subscribe.dispose();
                            subscribe = null;
                        }
                        //默认取消的操作
                        onCancel(false);
                        return;
                    }
                    view.setCountDown(count);

                });
    }

    @Override
    public synchronized void release() {
        Logger.d(TAG, "release");

        if (mPushResponse == null) {
            return;
        }

        if (subscribe != null && !subscribe.isDisposed()) {
            subscribe.dispose();
            subscribe = null;
        }
        if (mAudioPlayer != null) {
            mAudioPlayer.destroy();
            mAudioPlayer = null;
        }
        if (view != null) {
            view.dismiss();
            view = null;
        }

//        删除代理
        if (mAudioV5 != null) {
            ProxyHelper.releaseProxyRequest(mAudioV5.sid, mAudioV5.id);
        }

        TXZAsrManager.getInstance().recoverWakeupFromAsr(ASR_TASK_ID);
        PushUtils.cancelSpeak();

        mPushResponse = null;
        mAudioV5 = null;
        mPlayListData = null;
        isOk = false;


    }

    private synchronized void registerAsrCmd(PushResponse pushResponse) {
        if (pushResponse == null || !pushResponse.checkValidKeys()) {
            return;
        }

        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {

            @Override
            public void onCommandSelected(String type, String command) {
                super.onCommandSelected(type, command);

                Logger.d(TAG, "type:" + type + " command:" + command + " isWakeup:" + isWakeupResult());
                switch (type) {
                    case ASR_CMD_CLOSE:
                        ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_VOICE_AI_CANCEL);
                        onCancel(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public String getTaskId() {
                return ASR_TASK_ID;
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        };
        // FIXME: 2019/3/25 5.0只有取消指令
        asrCallBack.addCommand(ASR_CMD_CLOSE, pushResponse.getArrKeys().get(1).getArrCms().toArray(new String[0]));

        TXZAsrManager.getInstance().useWakeupAsAsr(asrCallBack);
    }


    private static class PushItemData {
        public String title;
        public String iconUrl;
        public int iconUrlId;
        public PlayListData playListData;
    }

    private static class PushItemDataFactory {
        public static @NonNull
        PushItemData getPushItemData(PlayListData playListData) {
            PushItemData pushItemData = new PushItemData();
            pushItemData.playListData = playListData;
            if (playListData.scene == PlayScene.ALBUM || playListData.scene == PlayScene.HISTORY_ALBUM || playListData.scene == PlayScene.FAVOUR_ALBUM) {
                if (playListData.album != null) {
                    if (playListData.album.albumType == Album.ALBUM_TYPE_NEWS) {
                        //新闻
                        pushItemData.title = "";
                    } else if (playListData.album.albumType == Album.ALBUM_TYPE_MUSIC) {
                        pushItemData.title = "上次收听：歌单《" + playListData.album.name + "》";
                        pushItemData.iconUrl = playListData.album.logo;
                    } else {
                        pushItemData.title = "上次收听：专辑《" + playListData.album.name + "》";
                        pushItemData.iconUrl = playListData.album.logo;
                    }
                }
            } else if (playListData.scene == PlayScene.LOCAL_MUSIC) {
                pushItemData.title = "上次收听：我的本地音乐《" + playListData.audio.name + "》";
                pushItemData.iconUrlId = R.drawable.window_local_icon;
            } else if (playListData.scene == PlayScene.FAVOUR_MUSIC) {
                pushItemData.title = "上次收听：我收藏的音乐《" + playListData.audio.name + "》";
                pushItemData.iconUrlId = R.drawable.window_favour_icon;
            } else if (playListData.scene == PlayScene.HISTORY_MUSIC) {
                pushItemData.title = "上次收听：我最近播放的音乐《" + playListData.audio.name + "》";
                pushItemData.iconUrlId = R.drawable.window_history_icon;
            } else if (playListData.scene == PlayScene.WECHAT_PUSH) {
                pushItemData.title = "上次收听：微信推送《" + playListData.audio.name + "》";
                pushItemData.iconUrlId = R.drawable.window_wechat_icon;
            } else if (playListData.scene == PlayScene.AI_RADIO) {
                pushItemData.title = "猜你喜欢：根据你的收听喜好推荐的内容";
                pushItemData.iconUrlId = R.drawable.window_guess_like_icon;
            }

            return pushItemData;
        }
    }

}
