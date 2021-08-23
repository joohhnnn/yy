package com.txznet.txz.component.media.util;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.loader.LoadedAudioTool;
import com.txznet.txz.component.media.loader.LoadedMusicTool;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供媒体工具搜索相关功能的处理
 *
 * 1.搜索tts和显示搜索列表的同步逻辑
 * 2.搜索和自动取消搜索的同步处理
 *
 * --------------------------------
 *
 * 需要显示搜索列表的整体流程:
 * 1. 播报"正在为您搜索.."的tts, 同时执行搜索逻辑
 * 2. 播报完tts && 搜索完毕 && 搜索未被取消, 显示搜索结果
 * 2* 搜索超时时进行错误提示
 *
 * 对于不需要显示搜索列表的情况, 搜索成功后直接播放结果列表中的第一个结果
 *
 * --------------------------------
 *
 * 使用此工具类需要自己实现调用sdk进行搜索的逻辑和将sdk返回的搜索结果转换为声控界面用于显示的结果类型的逻辑
 *
 * @param <T> 对应媒体工具搜索结果的数据类型
 *
 * @see MediaSearchPresenter#getSearchResult(MediaModel, SearchCallback)
 * @see MediaSearchPresenter#play(List, int)
 */

public abstract class MediaSearchPresenter<T> {
    private static final String LOG_TAG = "MediaSearch::";

    /**
     * IMediaTool
     */
    protected IMediaTool mContextMediaTool;
    /**
     * "正在为您搜索..."的tts是否播报完成
     */
    private boolean bSearchTipFinished;
    /**
     * 是否已经处理完毕
     * 这个标志位仅用来标识搜索处理是否完毕, 即无论搜索成功还是失败, 都会将此标志位置true
     */
    private boolean bSearchMusicFinished;
    /**
     * 搜索是否已被取消
     */
    private boolean bSearchCancelled;

    /**
     * 搜索是否已处理完毕
     */
    private AtomicBoolean aBSearchResultProcessed = new AtomicBoolean(false);

    /**
     * 搜索结果缓存
     */
    private List<T> mSearchResultList;

    /**
     * 是否显示搜索结果列表
     */
    private boolean bShowSearchResult = false;

    /**
     * 搜索超时时间
     */
    private long mSearchTimeout = 5000;

    private MediaModel mMediaModel = null;

    /**
     * 搜索超时处理
     */
    private Runnable mSearchTimeoutTask = new Runnable() {
        @Override
        public void run() {
            log("search timeout check");
            if (aBSearchResultProcessed.compareAndSet(false, true)) {
                monitorSearchTimeout();
                procFailure("timeout");
                // 清除缓存的搜索列表
                mSearchResultList = null;
            }
        }
    };

    public MediaSearchPresenter(@NonNull IMediaTool tool) {
        this.mContextMediaTool = tool;
    }

    /**
     * 调用对应sdk进行搜索
     *
     * @param model    需要搜索的媒体
     * @param callback 搜索结果回调
     */
    public abstract void getSearchResult(MediaModel model, SearchCallback<T> callback);

    /**
     * 调用对应sdk播放指定model
     *
     * @param list  结果列表
     * @param index 歌曲index
     */
    public abstract void play(List<T> list, int index);

    /**
     * 将对应sdk的媒体model转为用于声控界面进行结果展示的model
     *
     * @param resultModel 搜索结果的model
     * @return 转换后的Music model
     * @see AudioShowData
     */
    public abstract @NonNull
    AudioShowData getShowModel(T resultModel);

    /**
     * 设置搜索超时
     *
     * @param timeout 超时时间, 单位为ms
     */
    public void setSearchTimeout(long timeout) {
        mSearchTimeout = timeout;
    }

    /**
     * 取消播放音乐
     *
     * 这个方法应该在 (显示搜索结果列表 && 声控界面被关闭)的情况下被调用, 不显示搜索结果不应该调用取消逻辑
     */
    public void cancelPlayMusic() {
        log("force cancelPlayMusic!");
        bSearchCancelled = true;
        AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
    }

    /**
     * 播放音乐
     *
     * @param model
     * @param showSearchResult
     */
    public void playMusic(final MediaModel model, final boolean showSearchResult) {
        monitorSearchEnter();
        bShowSearchResult = showSearchResult;
        reset();

        if (bShowSearchResult) {
            String spk = "正在为你搜索";

            RecorderWin.addSystemMsg(spk);
            TtsManager.getInstance().speakText(spk, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    log("playMusic::tts finished!");
                    bSearchTipFinished = true;
                    // tts播完时如果搜索已经完成, 直接显示搜索列表
                    if (bSearchMusicFinished) {
                        procSearchResult();
                    } else {
                        // 需要显示搜索列表的情况tts播报完毕后再开始超时计时
                        startTimeoutCheck();
                    }
                }
            });
        } else {
            // 不需要显示搜索列表的情况默认按tts播报完毕处理且直接开始超时计时
            bSearchTipFinished = true;
            startTimeoutCheck();
        }

        final long searchStartTime = SystemClock.elapsedRealtime();
        mMediaModel = model;
        getSearchResult(model, new SearchCallback<T>() {
            @Override
            public void onSuccess(final List<T> result) {
                bSearchMusicFinished = true;
                long searchCostTime = SystemClock.elapsedRealtime() - searchStartTime;
                log(String.format("search finished! time cost: %sms", searchCostTime));
                reportSearchCost(searchCostTime, true);

                // 缓存搜索结果列表用于展示
                mSearchResultList = result;
                procSearchResult();
                mMediaModel = null;
            }

            @Override
            public void onFailure(final String tip) {
                bSearchMusicFinished = true;
                long searchCostTime = SystemClock.elapsedRealtime() - searchStartTime;
                log(String.format("search failed! time cost: %sms", searchCostTime));
                reportSearchCost(searchCostTime, false);
                procSearchResult();
                mMediaModel = null;
            }
        });
    }

    private void reportSearchCost(long cost, boolean success) {
        ReportUtil.Report report = new ReportUtil.Report.Builder().setType("media")
                .setAction("search").setSessionId()
                .putExtra("toolName", mContextMediaTool.getPackageName())
                .putExtra("cost", cost)
                .putExtra("success", success)
                .buildCommReport();

        ReportUtil.doReport(report);
        log("report: " + report.getData());
    }

    private void reset() {
        // 重置超时和取消相关的标志位
        AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
        aBSearchResultProcessed.getAndSet(false);
        bSearchCancelled = false;
        mSearchResultList = null;
        bSearchMusicFinished = false;
        bSearchTipFinished = false;
    }

    private void startTimeoutCheck() {
        AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
        AppLogic.runOnBackGround(mSearchTimeoutTask, mSearchTimeout);
    }

    private void procSearchResult() {
        if (bSearchCancelled) {
            log("procSearchResult::search is already cancelled");
            return;
        }

        if (!bSearchTipFinished || !bSearchMusicFinished) {
            return;
        }

        if (aBSearchResultProcessed.compareAndSet(false, true)) {
            if (null == mSearchResultList || 0 == mSearchResultList.size()) {
                log("procSearchResult::result is empty!");
                monitorSearchEmpty();
                procFailure("result is empty");
                return;
            }

            monitorSearchSuccess();

            // 如果不需要显示搜索结果列表, 直接播放
            if (!bShowSearchResult) {
                play(mSearchResultList, 0);
                return;
            }

            // 构造展示列表
            final ArrayList<AudioShowData> showList =
                    new ArrayList<AudioShowData>(mSearchResultList.size());

            // 维持原逻辑, 最多显示20个搜索结果, 避免声控界面处理异常
            int count = mSearchResultList.size() > 20 ? 20 : mSearchResultList.size();
            for (int i = 0; i < count; i++) {
                showList.add(getShowModel(mSearchResultList.get(i)));
            }

            ChoiceManager.getInstance().showMusicList(showList,
                    new OnItemSelectListener<AudioShowData>() {
                        @Override
                        public boolean onItemSelected(final boolean isPreSelect,
                                                      final AudioShowData audioShowData,
                                                      final boolean fromPage, final int idx,
                                                      final String fromVoice) {
                            if (isPreSelect) {
                                return false;
                            }

                            int listIndex = showList.indexOf(audioShowData);
                            play(mSearchResultList, listIndex);
                            return true;
                        }
                    });
        }
    }

    private void procFailure(String cause) {
        log("proc failure: " + cause);

        if (!bSearchCancelled) {
            // "没有找到相关结果"
            AsrManager.getInstance().setNeedCloseRecord(true);
            String tts = generateRespTts();
            RecorderWin.speakTextWithClose(tts, null);
        }
    }

    private String generateRespTts() {
        String strKeyword = "";
        if (mMediaModel != null) {
            if (mMediaModel.getTitle() != null) {
                strKeyword = mMediaModel.getTitle();
            } else if (mMediaModel.getAlbum() != null) {
                strKeyword = mMediaModel.getAlbum();
            } else if (mMediaModel.getArtists() != null && mMediaModel.getArtists().length > 0) {
                strKeyword = mMediaModel.getArtists()[0] + "的歌";
            }
        }
        if (!TextUtils.isEmpty(strKeyword)) {
            return NativeData.getResString("RS_MUSIC_SELECT_NO_RESULT_WITH_SORRY")
                    .replace("%KEYWORD%", strKeyword);
        } else {
            return NativeData.getResString("RS_MUSIC_SELECT_NO_RESULT");
        }
    }

    public interface SearchCallback<T> {
        void onSuccess(List<T> result);

        void onFailure(String tip);
    }

    // logger
    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + msg);
    }

    private void monitorSearchEnter() {
        monitor(MonitorUtil.MEDIA_SEARCH_ENTER_PREFIX + getMonitorTokenForMediaTool());
    }

    private void monitorSearchEmpty() {
        monitor(MonitorUtil.MEDIA_SEARCH_EMPTY_PREFIX + getMonitorTokenForMediaTool());
    }

    private void monitorSearchTimeout() {
        monitor(MonitorUtil.MEDIA_SEARCH_TIMEOUT_PREFIX + getMonitorTokenForMediaTool());
    }

    private void monitorSearchSuccess() {
        monitor(MonitorUtil.MEDIA_SEARCH_SUCCESS_PREFIX + getMonitorTokenForMediaTool());
    }

    private String getMonitorTokenForMediaTool() {
        String mediaToolPackageName = mContextMediaTool.getPackageName();
        String token;

        if (MediaToolConstants.PACKAGE_MUSIC_KUWO.equals(mediaToolPackageName)) {
            token = "_kuwo";
        } else if (MediaToolConstants.PACKAGE_AUDIO_KAOLA.equals(mediaToolPackageName)) {
            token = "_kl";
        } else if (MediaToolConstants.PACKAGE_AUDIO_XMLY.equals(mediaToolPackageName)) {
            token = "_xmly";
        } else if (MediaToolConstants.PACKAGE_TONGTING.equals(mediaToolPackageName)) {
            token = "_txz";
        } else {
            token = "";
        }

        // 添加外部装载的适配的标记
        if (mContextMediaTool instanceof LoadedMusicTool
                || mContextMediaTool instanceof LoadedAudioTool) {
            token += "_plugin";
        }

        log("cannot resolve monitor token for package: " + mediaToolPackageName);
        return token;
    }


    // monitor
    private void monitor(String monitorMsg) {
        MonitorUtil.monitorCumulant(monitorMsg);
    }
}
