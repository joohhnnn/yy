package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.SortType;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.LocalAudioSortUtil;
import com.txznet.music.util.ScanUtil;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;

import java.util.List;

/**
 * 本地音乐Store
 *
 * @author zackzhou
 * @date 2018/12/3,11:23
 */
public class LocalAudioStore extends Store {
    private MutableLiveData<List<LocalAudio>> mAudios = new MutableLiveData<>(); // 原始本地数据
    private MutableLiveData<Boolean> isScanning = new MutableLiveData<>(); // 是否处于扫描中
    private MutableLiveData<SortType> mSortType = new MutableLiveData<>(); // 排序模式
    private MediatorLiveData<List<LocalAudio>> mSortedAudios = new MediatorLiveData<>(); // 排序后的结果
    private MediatorLiveData<Integer> mScanCount = new MediatorLiveData<>(); // 扫描个数

    public LocalAudioStore() {
        mSortedAudios.addSource(mSortType, sortType -> {
            List<LocalAudio> list = mAudios.getValue();
            if (list == null) {
                return;
            }
            if (SortType.SORT_BY_NAME_ASC == mSortType.getValue()) { // 名称排序
                LocalAudioSortUtil.sortAudiosByName(list);
            } else if (SortType.SORT_BY_TIME_DESC == mSortType.getValue()) { // 时间排序
                LocalAudioSortUtil.sortAudiosByTime(list);
            }
            mSortedAudios.setValue(list);
        });
        mSortedAudios.addSource(mAudios, audioList -> {
            if (SortType.SORT_BY_NAME_ASC == mSortType.getValue()) { // 名称排序
                LocalAudioSortUtil.sortAudiosByName(audioList);
            } else if (SortType.SORT_BY_TIME_DESC == mSortType.getValue()) { // 时间排序
                LocalAudioSortUtil.sortAudiosByTime(audioList);
            }
            mSortedAudios.setValue(audioList);
        });
        mSortType.setValue(SharedPreferencesUtils.isLocalSortByTime() ? SortType.SORT_BY_TIME_DESC : SortType.SORT_BY_NAME_ASC);
        isScanning.setValue(ScanUtil.isScanning());
    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_GET_LOCAL,
                ActionType.ACTION_SCAN,
                ActionType.ACTION_SCAN_CANCEL,
                ActionType.ACTION_SCAN_COUNT,
                ActionType.ACTION_LOCAL_SORT_BY_NAME,
                ActionType.ACTION_LOCAL_SORT_BY_TIME,
                ActionType.ACTION_LOCAL_DELETE,
                ActionType.ACTION_MEDIA_SCANNER_STARTED,
                ActionType.ACTION_FAVOUR_EVENT_FAVOUR,
                ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR
        };
    }

    /**
     * 获取本地音频列表
     */
    public LiveData<List<LocalAudio>> getAudioList() {
        return mSortedAudios;
    }

    /**
     * 是否处于扫描中
     */
    public LiveData<Boolean> isScanning() {
        return isScanning;
    }

    /**
     * 获取扫描个数
     */
    public LiveData<Integer> getScanCount() {
        return mScanCount;
    }

    /**
     * 获取排序方式
     */
    public LiveData<SortType> getSortType() {
        return mSortType;
    }

    @Override
    protected void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_MEDIA_SCANNER_STARTED:
            case ActionType.ACTION_SCAN:
                isScanning.setValue(true);
                break;
            case ActionType.ACTION_SCAN_CANCEL:
                isScanning.setValue(false);
                break;
            case ActionType.ACTION_LOCAL_SORT_BY_NAME:
                SharedPreferencesUtils.setLocalSortByTime(false);
                mSortType.setValue(SortType.SORT_BY_NAME_ASC);
                break;
            case ActionType.ACTION_LOCAL_SORT_BY_TIME:
                SharedPreferencesUtils.setLocalSortByTime(true);
                mSortType.setValue(SortType.SORT_BY_TIME_DESC);
                break;
            case ActionType.ACTION_SCAN_COUNT:
                mScanCount.setValue((Integer) action.data.get(Constant.LocalConstant.KEY_SCAN_COUNT));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onData(RxAction action) {
        List<LocalAudio> audioList;
        switch (action.type) {
            case ActionType.ACTION_GET_LOCAL:
                mAudios.setValue((List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO));
                break;
            case ActionType.ACTION_MEDIA_SCANNER_STARTED:
            case ActionType.ACTION_SCAN:
                isScanning.setValue(false);
                mAudios.setValue((List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO));
                break;
            case ActionType.ACTION_LOCAL_DELETE:
                audioList = mAudios.getValue();
                if (audioList != null) {
                    audioList.removeAll((List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO));
                    mAudios.setValue(audioList);
                }
                break;
            case ActionType.ACTION_FAVOUR_EVENT_FAVOUR:
                audioList = mAudios.getValue();
                if (audioList != null) {
                    FavourAudio favour = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                    for (LocalAudio audio : audioList) {
                        if (audio.id == favour.id && audio.sid == favour.sid) {
                            audio.isFavour = true;
                            mAudios.setValue(audioList);
                            break;
                        }
                    }
                }
                break;
            case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR:
                audioList = mAudios.getValue();
                if (audioList != null) {
                    FavourAudio unFavour = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                    for (LocalAudio audio : audioList) {
                        if (audio.id == unFavour.id && audio.sid == unFavour.sid) {
                            audio.isFavour = false;
                            mAudios.setValue(audioList);
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
    protected void onError(RxAction action, Throwable throwable) {
    }
}
