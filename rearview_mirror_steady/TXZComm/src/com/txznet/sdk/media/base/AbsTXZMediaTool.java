package com.txznet.sdk.media.base;

import android.util.Log;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.media.AbsTXZAudioTool;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.constant.PlayerLoopMode;
import com.txznet.sdk.media.constant.PlayerStatus;
import com.txznet.sdk.media.TXZMediaModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * 同行者远程媒体工具
 * Created by J on 2018/8/6.
 */

public abstract class AbsTXZMediaTool implements ITXZMediaTool, ITXZMediaSearchTool {
    /**
     * 获取sdk版本号, 发生需要处理兼容性的修改时需要同时修改版本号
     *
     * @return sdk版本号
     */
    public abstract int getSDKVersion();

    /**
     * 获取用于ipc调用的前缀
     *
     * @return
     */
    public abstract String getRemoteInvokePrefix();

    /**
     * 通知播放器状态变化
     *
     * 播放器状态发生变化时, 应主动调用此方法对新播放状态进行通知
     *
     * @param status 新的播放状态
     * @see PlayerStatus
     */
    public final void onPlayerStatusChanged(PlayerStatus status) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("status", status.toStatusString());
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                getRemoteInvokePrefix() + InvokeConstants.CMD_NOTIFY_PLAYER_STATUS,
                builder.toBytes(), null);
    }

    /**
     * 通知播放节目发生变化
     *
     * @param model 新的播放节目
     */
    public final void onPlayingModelChanged(TXZMediaModel model) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("model", model.toJsonObject());
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                getRemoteInvokePrefix() + InvokeConstants.CMD_NOTIFY_PLAYING_MODEL,
                builder.toBytes(), null);
    }

    /**
     * 是否拦截声控界面处理, 默认不拦截
     *
     * @return 拦截返回true
     */
    @Override
    public boolean interceptTts() {
        return false;
    }

    /**
     * 是否显示搜索结果列表
     *
     * 设置了显示搜索结果列表后, 执行歌曲搜索时会调用
     * {@link AbsTXZAudioTool#search(TXZMediaModel, SearchCallback)}方法, 需要执行搜索后返回搜索结果列表
     *
     * @return 显示返回true
     */
    @Override
    public boolean showSearchResult() {
        return false;
    }

    /**
     * 设置搜索超时时间, 默认10s
     *
     * @return
     */
    @Override
    public int getSearchTimeoout() {
        return 10000;
    }

    /**
     * 获取搜索结果列表
     *
     * @param searchModel 用于搜索的媒体信息
     * @return 搜索结果列表
     */
    @Override
    public void search(final TXZMediaModel searchModel, SearchCallback callback) {
    }

    /**
     * 播放指定节目, 对应上一次搜索列表中用户选择的节目
     *
     * @param index 结果列表中的编号(从0开始)
     * @param model 对应编号的搜索结果
     */
    @Override
    public void playSearchResult(final int index, final TXZMediaModel model) {

    }

    /**
     * 处理声控发起的sdk相关调用
     *
     * @param packageName
     * @param command
     * @param data
     * @return
     */
    public byte[] procSdkInvoke(final String packageName, final String command, final byte[] data) {
        byte[] ret = null;
        JSONBuilder params = new JSONBuilder(data);
        if (InvokeConstants.INVOKE_OPEN.equals(command)) {
            boolean startPlaying = params.getVal(InvokeConstants.PARAM_OPEN_PLAY,
                    boolean.class);
            open(startPlaying);
        } else if (InvokeConstants.INVOKE_PLAY.equals(command)) {
            TXZMediaModel model = TXZMediaModel.fromJSONObject(params.getJSONObject());
            play(model);
        } else if (InvokeConstants.INVOKE_CONTINUE_PLAY.equals(command)) {
            continuePlay();
        } else if (InvokeConstants.INVOKE_PAUSE.equals(command)) {
            pause();
        } else if (InvokeConstants.INVOKE_EXIT.equals(command)) {
            exit();
        } else if (InvokeConstants.INVOKE_NEXT.equals(command)) {
            next();
        } else if (InvokeConstants.INVOKE_PREV.equals(command)) {
            prev();
        } else if (InvokeConstants.INVOKE_SWITCH_LOOP_MODE.equals(command)) {
            String mode = params.getVal(InvokeConstants.PARAM_PLAY_MODE, String.class);
            switchLoopMode(PlayerLoopMode.fromModeStr(mode));
        } else if (InvokeConstants.INVOKE_COLLECT.equals(command)) {
            collect();
        } else if (InvokeConstants.INVOKE_UNCOLLECT.equals(command)) {
            unCollect();
        } else if (InvokeConstants.INVOKE_PLAY_COLLECTION.equals(command)) {
            playCollection();
        } else if (InvokeConstants.INVOKE_SUBSCRIBE.equals(command)) {
            subscribe();
        } else if (InvokeConstants.INVOKE_UNSUBSCRIBE.equals(command)) {
            unSubscribe();
        } else if (InvokeConstants.INVOKE_PLAY_SUBSCRIBE.equals(command)) {
            playSubscribe();
        } else if (InvokeConstants.INVOKE_GET_PLAYER_STATUS.equals(command)) {
            ret = getStatus().toStatusString().getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_LOOP_MODE.equals(command)) {
            String mode = params.getVal(InvokeConstants.PARAM_PLAY_MODE, String.class);
            ret = ("" + supportLoopMode(PlayerLoopMode.fromModeStr(mode))).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_SUBSCRIBE.equals(command)) {
            ret = ("" + supportSubscribe()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_UNSUBSCRIBE.equals(command)) {
            ret = ("" + supportUnSubscribe()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_COLLECT.equals(command)) {
            ret = ("" + supportCollect()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_UNCOLLECT.equals(command)) {
            ret = ("" + supportUnCollect()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_PLAY_SUBSCRIBE.equals(command)) {
            ret = ("" + supportPlaySubscribe()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_PLAY_COLLECTION.equals(command)) {
            ret = ("" + supportPlayCollection()).getBytes();
        } else if (InvokeConstants.INVOKE_SUPPORT_SEARCH.equals(command)) {
            ret = ("" + supportSearch()).getBytes();
        } else if (InvokeConstants.INVOKE_HAS_NEXT.equals(command)) {
            ret = ("" + hasNext()).getBytes();
        } else if (InvokeConstants.INVOKE_HAS_PREV.equals(command)) {
            ret = ("" + hasPrev()).getBytes();
        } else if (InvokeConstants.INVOKE_SEARCH_MEDIA.equals(command)) {
            performSearch(params);
        } else if (InvokeConstants.INVOKE_PLAY_SEARCH_RESULT.equals(command)) {
            int index = params.getVal(InvokeConstants.PARAM_PLAY_RESULT_INDEX, int.class);
            TXZMediaModel model = TXZMediaModel.fromJSONObject(
                    params.getVal(InvokeConstants.PARAM_PLAY_RESULT_MODEL,
                            JSONObject.class));

            playSearchResult(index, model);
        }

        return ret;
    }

    /**
     * 解析Core传来的搜索
     *
     * @param paramsBuilder
     */
    private void performSearch(JSONBuilder paramsBuilder) {
        if (null == paramsBuilder) {
            Log.e("mediaSdk", "media search: param is null");
            return;
        }

        final Long taskId = paramsBuilder.getVal(InvokeConstants.PARAM_SEARCH_TASK_ID, Long.class, -1L);
        Log.d("mediaSdk", "performSearch: taskId = " + taskId);
        if (-1L == taskId) {
            Log.e("mediaSdk", "media search: cannot resolve taskId");
            return;
        }

        TXZMediaModel searchModel = resolveSearchModel(paramsBuilder);
        if (null == searchModel) {
            notifySearchError(taskId, "cannot resolve searchModel");
            return;
        }

        search(searchModel, new SearchCallback() {
            @Override
            public void onSuccess(final List<TXZMediaModel> result) {
                notifySearchSuccess(taskId, result);
            }

            @Override
            public void onError(final String cause) {
                notifySearchError(taskId, cause);
            }
        });
    }

    private void notifySearchSuccess(long taskId, List<TXZMediaModel> resultList) {
        JSONBuilder paramBuilder = new JSONBuilder();
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_TASK_ID, taskId);
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_MEDIA_RESULT,
                generateSearchResultData(resultList));

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, getRemoteInvokePrefix() +
                InvokeConstants.CMD_NOTIFY_SEARCH_SUCCESS, paramBuilder.toBytes(), null);
    }

    private void notifySearchError(long taskId, String cause) {
        JSONBuilder paramBuilder = new JSONBuilder();
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_TASK_ID, taskId);
        paramBuilder.put(InvokeConstants.PARAM_SEARCH_ERROR_CAUSE, cause);

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, getRemoteInvokePrefix() +
                InvokeConstants.CMD_NOTIFY_SEARCH_ERROR, paramBuilder.toBytes(), null);
    }

    private TXZMediaModel resolveSearchModel(JSONBuilder paramsBuilder) {
        JSONObject jsonSearchModel = paramsBuilder.getVal(InvokeConstants.PARAM_SEARCH_MODEL,
                JSONObject.class, null);
        if (null == jsonSearchModel) {
            return null;
        }

        return TXZMediaModel.fromJSONObject(jsonSearchModel);
    }

    private JSONArray generateSearchResultData(List<TXZMediaModel> resultList) {
        JSONArray jsonArray = new JSONArray();

        if (null != resultList) {
            for (TXZMediaModel model : resultList) {
                jsonArray.put(model.toJsonObject());
            }
        }

        return jsonArray;
    }
}
