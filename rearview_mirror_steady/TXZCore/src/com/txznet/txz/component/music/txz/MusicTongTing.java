package com.txznet.txz.component.music.txz;

import android.content.pm.PackageInfo;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.audio.txz.AudioTongTing;
import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.ui.win.record.RecorderWin;

/**
 * 同听适配工具
 * Created by J on 2018/5/4.
 */

public class MusicTongTing extends AbsMusicTool {
    private static final String LOG_TAG = "MusicTongTing::";
    private static int mVersion = 0;// 音乐的版本
    private PLAYER_STATUS mStatus = PLAYER_STATUS.IDLE;

    private MediaModel mPlayingModel;

    /**
     * 同听model类型(音乐/电台)
     */
    public enum TT_MODEL_TYPE {
        MUSIC, AUDIO
    }


    /**
     * 用于同步同听播放状态的接口
     * <p>
     * 同听的播放状态是通过远程调用同步至Core的, MusicManager内处理远程调用后转调此方法同步
     * 同听的播放状态变化
     *
     * @param status 新的播放状态
     */
    public void updateStatus(PLAYER_STATUS status) {
        boolean changed;
        if (getMusicVersionCode() < 440) {
            log("update status(old): " + status.name());
            changed = updateStatusOld(status);
        } else {
            log("update status(V440): " + status.name());
            changed = updateStatusV440(status);
        }

        // 兼容原MusicManager内逻辑, 播放状态改变时发送广播
        // TODO: 2018/5/23 这个广播不确定具体有什么用途, 后续可以看下能不能删除
        if (changed) {
            if (PLAYER_STATUS.PLAYING == status || PLAYER_STATUS.BUFFERING == status) {
                onBeginMusic();
            } else {
                onEndMusic();
            }
        }
    }

    private boolean updateStatusOld(PLAYER_STATUS status) {
        boolean statusChanged = (mStatus == status);
        mStatus = status;
        notifyPlayerStatusChange(status);

        return statusChanged;
    }

    private boolean updateStatusV440(PLAYER_STATUS status) {
        // 新版本同听独立了电台和音乐模块, 需要根据调用不同更新音乐或电台的播放状态
        if (AudioTongTing.getInstance().getPlayingModel() != null) {
            this.mStatus = PLAYER_STATUS.PAUSED;
            return AudioTongTing.getInstance().updateStatus(status);
        } else {
            AudioTongTing.getInstance().updateStatus(PLAYER_STATUS.PAUSED);
            return updateStatusOld(status);
        }
    }

    public void updatePlayingModel(MediaModel model, TT_MODEL_TYPE type) {
        if (getMusicVersionCode() < 440) {
            // 旧版本同听当做音乐+电台工具
            this.mPlayingModel = model;
            AudioTongTing.getInstance().updatePlayingModel(model);
        } else {
            // 新版本同听独立了电台和音乐模块, 需要根据调用不同更新音乐或电台的播放状态
            if (TT_MODEL_TYPE.MUSIC == type) {
                this.mPlayingModel = model;
                AudioTongTing.getInstance().updatePlayingModel(null);
            } else {
                this.mPlayingModel = null;
                AudioTongTing.getInstance().updatePlayingModel(model);
            }
        }
    }

    /**
     * 发送开始播放广播
     * <p>
     * 这个方法是从MusicManager内复制过来的, 保持原逻辑和log tag
     */
    private void onBeginMusic() {
        JNIHelper.logd("CORE:MUSIC:onBeginMusic");
        String command = "comm.status.onBeginMusic";
        ServiceManager.getInstance().broadInvoke(command, null);
    }

    /**
     * 发送停止播放广播
     * <p>
     * 这个方法是从MusicManager内复制过来的, 保持原逻辑和log tag
     */
    private void onEndMusic() {
        JNIHelper.logd("CORE:MUSIC:onEndMusic");
        String command = "comm.status.onEndMusic";
        ServiceManager.getInstance().broadInvoke(command, null);
    }

    /**
     * 旧版本兼容逻辑
     */
    public void hateAudio() {
        if (getMusicVersionCode() > 300) {
            ServiceManager.getInstance()
                    .sendInvoke(ServiceManager.MUSIC, "music.hate.audio", null, null);
        } else {
            String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
            RecorderWin.speakText(spk, null);
        }
    }

    /**
     * 旧版本兼容逻辑
     */
    public void requestHistory(String type) {
        if (supportRequestHistory()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("type", type);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                    "music.sound.history.find", jsonBuilder.toBytes(), null);
            RecorderWin.addCloseRunnable(new Runnable() {
                @Override
                public void run() {
                    ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                            "music.sound.history.cancelfind", null, null);
                }
            });
        } else {
            String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
            RecorderWin.speakText(spk, null);
        }
    }

    /**
     * 旧版本兼容逻辑
     */
    public boolean supportRequestHistory() {
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(
                ServiceManager.MUSIC, "music.history.support", null);
        if (serviceData == null) {
            return false;
        }
        byte[] result = serviceData.getBytes();
        if (result != null) {
            try {
                return Boolean.parseBoolean(new String(result));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 旧版本兼容逻辑
     */
    public void playRandom() {
        // JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
        // UiMusic.SUBEVENT_MEDIA_REFRESH_MUSIC_LIST);
        ServiceManager.getInstance()
                .sendInvoke(ServiceManager.MUSIC, "music.playRandom", null, null);
    }

    @Override
    public void setSearchTimeout(final long timeout) {

    }

    @Override
    public void setShowSearchResult(final boolean show) {

    }

    @Override
    public void cancelRequest() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.sound.cancelfind", null, null);
    }

    @Override
    public String getPackageName() {
        return ServiceManager.MUSIC;
    }

    @Override
    public int getPriority() {
        return MediaToolConstants.PRIORITY_TONGTING;
    }

    @Override
    public void open(final boolean play) {
        if (play) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open.play",
                    null, null);
        } else {
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open", null, null);
        }
    }

    @Override
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.exit", null, null);
        RecorderWin.close();
    }

    @Override
    public void play(final MediaModel model) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.find",
                model.toJsonObject().toString().getBytes(), null);
    }

    @Override
    public void stop() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.pause", null, null);
        RecorderWin.close();
    }

    @Override
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.pause", null, null);
        RecorderWin.close();
    }

    @Override
    public void continuePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.play", null, null);
        RecorderWin.close();
    }

    @Override
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.next", null, null);
        RecorderWin.close();
    }

    @Override
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.prev", null, null);
        RecorderWin.close();
    }

    @Override
    public void switchLoopMode(final LOOP_MODE mode) {
        switch (mode) {
            case LIST_LOOP:
            case SEQUENTIAL:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeLoopAll", null, null);
                break;

            case SINGLE_LOOP:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeLoopOne", null, null);
                break;

            case SHUFFLE:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeRandom", null, null);
                break;
        }
        RecorderWin.close();
    }

    @Override
    public void collect() {
        JSONObject json = new JSONObject();
        json.put("audio", "");
        json.put("favour", "true");
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
                json.toJSONString().getBytes(), null);
    }

    @Override
    public void unCollect() {
        JSONObject json = new JSONObject();
        json.put("audio", "");
        json.put("favour", "false");
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.updateFavour",
                json.toJSONString().getBytes(), null);
    }

    @Override
    public void playCollection() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play.favour",
                null, null);
        RecorderWin.close();
    }

    @Override
    public void subscribe() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.addSubscribe",
                null, null);
    }

    @Override
    public void unSubscribe() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.unSubscribe",
                null, null);
    }

    @Override
    public void playSubscribe() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play.subscribe",
                null, null);
        RecorderWin.close();
    }

    @Override
    public PLAYER_STATUS getStatus() {
        if (mPlayingModel == null) {
            return PLAYER_STATUS.IDLE;
        }
        return mStatus;
    }

    @Override
    public MediaModel getPlayingModel() {
        return mPlayingModel;
    }

    @Override
    public boolean supportLoopMode(final LOOP_MODE mode) {
        return true;
    }

    @Override
    public boolean supportCollect() {
        return getMusicVersionCode() > 0;
    }

    @Override
    public boolean supportUnCollect() {
        return getMusicVersionCode() > 0;
    }

    @Override
    public boolean supportPlayCollection() {
        return getMusicVersionCode() >= 440;
    }

    @Override
    public boolean supportSubscribe() {
        return getMusicVersionCode() > 0;
    }

    @Override
    public boolean supportUnSubscribe() {
        return getMusicVersionCode() > 0;
    }

    @Override
    public boolean supportPlaySubscribe() {
        return getMusicVersionCode() >= 440;
    }

    @Override
    public boolean supportSearch() {
        return true;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public boolean hasPrev() {
        return true;
    }

    @Override
    public boolean interceptRecordWinControl(MEDIA_TOOL_OP op) {
        boolean intercept = false;
        switch (op) {
            case PLAY:
                // 搜索场景由同听自己处理(需要展示结果列表)
                intercept = true;
                break;

            case OPEN:
            case COLLECT:
            case UNCOLLECT:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
                // 收藏订阅相关操作根据同听版本进行兼容性处理
                intercept = interceptCollectAndSubscribe();
                break;
            case PLAY_COLLECTION:
            case PLAY_SUBSCRIBE:
                if (getMusicVersionCode() >= 440) {
                    ServiceManager.ServiceData serviceData = ServiceManager.getInstance()
                            .sendInvokeSync(ServiceManager.MUSIC, "intercept.play", null);
                    if (null != serviceData && serviceData.getBoolean() != null) {
                        intercept = serviceData.getBoolean();
                    }
                }
                break;
        }

        return intercept;
    }

    @Override
    public boolean equals(final Object obj) {
        if (getMusicVersionCode() < 440) {
            return (obj instanceof AudioTongTing) || super.equals(obj);
        }

        return super.equals(obj);
    }

    /**
     * 是否需要拦截收藏和订阅相关场景的声控界面处理
     * <p>
     * 同听4.4.0(440)版本开始不再针对收藏和订阅相关操作播报自己的提示, 针对440之前的版本拦截掉相关场景
     * 由同听自己控制播报逻辑
     *
     * @return
     */
    private boolean interceptCollectAndSubscribe() {
        return getMusicVersionCode() < 440;
    }

    public int getMusicVersionCode() {
        int versionCode = 0;
        PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
        if (apkInfo != null && apkInfo.versionCode >= 300) {// 3.0.0版本开始支持
            versionCode = apkInfo.versionCode;
        }
        JNIHelper.logd(LOG_TAG + "getMusicVersion " + versionCode);
        return versionCode;
    }

    public static int getVersion() {
        if (mVersion == 0) {
            PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(ServiceManager.MUSIC);
            if (apkInfo != null) {
                mVersion = apkInfo.versionCode;
            }
        }
        return mVersion;
    }

    //----------- single instance -----------
    private static volatile MusicTongTing sInstance;

    public static MusicTongTing getInstance() {
        if (null == sInstance) {
            synchronized (MusicTongTing.class) {
                if (null == sInstance) {
                    sInstance = new MusicTongTing();
                }
            }
        }

        return sInstance;
    }

    private MusicTongTing() {

    }
    //----------- single instance -----------

    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + msg);
    }
}
