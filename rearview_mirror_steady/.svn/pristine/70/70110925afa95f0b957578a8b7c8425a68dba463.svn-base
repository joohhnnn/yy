package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;
import com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent;

import java.util.List;

/**
 * @author zackzhou
 * @date 2018/12/13,15:44
 */

public class PlayerQueueStore extends Store {
    private MutableLiveData<List<AudioV5>> mQueue = new MutableLiveData<>();
    private SingleLiveEvent<Status> mLoadMoreStatus = new SingleLiveEvent<>();

    /**
     * 加载状态
     */
    public enum Status {
        /**
         * 加载失败
         */
        LOAD_FAILD,
        /**
         * 加载成功
         */
        LOAD_SUCCESS,
        /**
         * 没有更多
         */
        LOAD_EMPTY
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_PLAYER_QUEUE_ON_CHANGED,
                ActionType.ACTION_PLAYER_QUEUE_GET,
                ActionType.ACTION_BREAK_POINT_UPDATE,
                ActionType.ACTION_PLAYER_QUEUE_LOAD_MORE
        };
    }

    public LiveData<List<AudioV5>> getQueue() {
        return mQueue;
    }

    public LiveData<Status> getLoadMoreStatus() {
        return mLoadMoreStatus;
    }

    @Override
    protected void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_PLAYER_QUEUE_ON_CHANGED:
                mQueue.setValue((List<AudioV5>) action.data.get(Constant.PlayQueueConstant.KEY_AUDIO_LIST));
                break;
            case ActionType.ACTION_BREAK_POINT_UPDATE:
                AudioV5 breakpointItem = (AudioV5) action.data.get(Constant.BreakpointConstant.KEY_AUDIO);
                List<AudioV5> queueList = mQueue.getValue();
                if (queueList != null) {
                    for (AudioV5 audioV5 : queueList) {
                        if (audioV5.equals(breakpointItem)) {
                            if (!audioV5.hasPlay || audioV5.progress != breakpointItem.progress) {
                                audioV5.hasPlay = true;
                                audioV5.progress = breakpointItem.progress;
                                mQueue.setValue(queueList);
                            }
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_PLAYER_QUEUE_GET:
                mQueue.setValue((List<AudioV5>) action.data.get(Constant.PlayQueueConstant.KEY_AUDIO_LIST));
                break;
            case ActionType.ACTION_PLAYER_QUEUE_LOAD_MORE:
                List<Audio> audioList = (List<Audio>) action.data.get(Constant.PlayQueueConstant.KEY_AUDIO_LIST);
                if (audioList.isEmpty()) {
                    mLoadMoreStatus.setValue(Status.LOAD_EMPTY);
                } else {
                    mLoadMoreStatus.setValue(Status.LOAD_SUCCESS);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
        switch (action.type) {
            case ActionType.ACTION_PLAYER_QUEUE_LOAD_MORE:
                mLoadMoreStatus.setValue(Status.LOAD_FAILD);
                break;
            default:
                break;
        }
    }
}
