package com.txznet.txz.component.media.util;

import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.TXZMediaModel;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.music.bean.AudioShowData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于远程音乐/电台工具处理搜索流程的MediaSearchPresenter
 *
 * 会自动将搜索流程转为对sdk的远程调用, 并自动对sdk返回的搜索结果进行处理
 * Created by J on 2018/8/7.
 */

public class RemoteMediaSearchPresenter extends MediaSearchPresenter<TXZMediaModel> {
    private static final String LOG_TAG = "RemoteMediaSearchPresenter";
    // 当前搜索流程的taskId
    private long mLastSearchTaskId = -1L;
    // 当前搜索流程的callback
    private MediaSearchPresenter.SearchCallback<TXZMediaModel> mLastSearchCallback;

    /**
     * 获取用于ipc调用的命令字前缀
     *
     * @return
     */
    protected String getRemoteInvokePrefix() {
        if (mContextMediaTool instanceof RemoteAudioTool) {
            return InvokeConstants.INVOKE_PREFIX_AUDIO;
        }

        return InvokeConstants.INVOKE_PREFIX_MUSIC;
    }

    public RemoteMediaSearchPresenter(@NonNull final RemoteMusicTool tool) {
        super(tool);
    }

    public RemoteMediaSearchPresenter(@NonNull final RemoteAudioTool tool) {
        super(tool);
    }

    /**
     * 不允许调用基类构造函数
     */
    private RemoteMediaSearchPresenter(@NonNull final IMediaTool tool) {
        super(tool);
    }

    public void procRemoteInvoke(String cmd, JSONBuilder param) {
        if (InvokeConstants.CMD_NOTIFY_SEARCH_SUCCESS.equals(cmd)) {
            procSearchSuccess(param);
        } else if (InvokeConstants.CMD_NOTIFY_SEARCH_ERROR.equals(cmd)) {
            procSearchError(param);
        }
    }

    /**
     * 取消搜索逻辑
     */
    public void cancelSearch() {
        cancelPlayMusic();
        mLastSearchTaskId = -1L;
        mLastSearchCallback = null;
    }

    @Override
    public void getSearchResult(final MediaModel model,
                                final SearchCallback<TXZMediaModel> callback) {
        // 更新搜索流程相关标志, 因sdk调用ipc可能有时序问题, 需要搜索结果返回时需要校验taskId
        // 是否正确, 对于非当前流程的搜索结果不进行响应
        mLastSearchTaskId = SystemClock.elapsedRealtime();
        mLastSearchCallback = callback;

        TXZMediaModel searchModel = model.toTXZMediaModel();
        JSONBuilder paramBuilder = new JSONBuilder();
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_MODEL,
                searchModel.toJsonObject());
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_TASK_ID, mLastSearchTaskId);

        sendInvoke(InvokeConstants.INVOKE_SEARCH_MEDIA, paramBuilder.toBytes());
    }

    @Override
    public void play(final List<TXZMediaModel> list, final int index) {
        JSONBuilder paramBuilder = new JSONBuilder();
        paramBuilder.put(InvokeConstants.PARAM_PLAY_RESULT_MODEL,
                list.get(index).toJsonObject());
        paramBuilder.put(InvokeConstants.PARAM_PLAY_RESULT_INDEX, index);
        sendInvoke(InvokeConstants.INVOKE_PLAY_SEARCH_RESULT, paramBuilder.toBytes());
    }

    @NonNull
    @Override
    public AudioShowData getShowModel(final TXZMediaModel resultModel) {
        AudioShowData model = new AudioShowData();
        model.setTitle(resultModel.getTitle());
        model.setName((null == resultModel.getArtists()) ?
                "" : resultModel.getArtists()[0]);
        model.setAlbumName(resultModel.getAlbum());

        return model;
    }

    private void procSearchSuccess(JSONBuilder paramBuilder) {
        Long taskId = paramBuilder.getVal(InvokeConstants.PARAM_SEARCH_TASK_ID, Long.class, -1L);

        // 非当前流程的搜索结果不进行响应
        if (taskId != mLastSearchTaskId) {
            log(String.format(
                    "procSearchSuccess: taskId mismatch: %s | %s", taskId, mLastSearchTaskId));
            return;
        }

        JSONArray resultListJson = paramBuilder.getVal(
                InvokeConstants.PARAM_SEARCH_MEDIA_RESULT, JSONArray.class);

        ArrayList<TXZMediaModel> resultList = new ArrayList<TXZMediaModel>();
        try {
            for (int i = 0, len = resultListJson.length(); i < len; i++) {
                resultList.add(TXZMediaModel
                        .fromJSONObject(resultListJson.getJSONObject(i)));
            }
        } catch (JSONException e) {

        }

        if (mLastSearchCallback != null) {
            mLastSearchCallback.onSuccess(resultList);
        }
    }

    private void procSearchError(JSONBuilder paramBuilder) {
        Long taskId = paramBuilder.getVal(InvokeConstants.PARAM_SEARCH_TASK_ID, Long.class, -1L);

        // 非当前流程的搜索结果不进行响应
        if (taskId != mLastSearchTaskId) {
            log(String.format(
                    "procSearchError: taskId mismatch: %s | %s", taskId, mLastSearchTaskId));
            return;
        }

        String cause = paramBuilder.getVal(InvokeConstants.PARAM_SEARCH_ERROR_CAUSE,
                String.class, "");

        if (mLastSearchCallback != null) {
            mLastSearchCallback.onFailure(cause);
        }
    }

    private void sendInvoke(String cmd, byte[] data) {
        ServiceManager.getInstance().sendInvoke(mContextMediaTool.getPackageName(),
                getRemoteInvokePrefix() + cmd, data, null);
    }

    // logger
    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + "::" + msg);
    }
}
