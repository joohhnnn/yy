package com.txznet.music.localModule;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.jni.TXZStrComparator;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.dao.DaoManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.localModule.logic.ScanUtil;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.utils.LoadUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ThreadUtil;
import com.txznet.music.utils.UpdateToCoreUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2018/1/10.
 */

public class LocalAudioDataSource implements LocalContract.DataSource {

    private static final String TAG = "LocalAudioDataSource";
    private static LocalAudioDataSource sInstance;
    private List<Audio> mLocalAudios;
    private Set<Audio> mAudioSet;
    private boolean isScanning;
    private List<LocalContract.ILocalScanListener> mListeners;
    private Disposable mCountDisposable;

    private LocalAudioDataSource() {
        mLocalAudios = new ArrayList<>();
        mListeners = new ArrayList<>();
        mAudioSet = new HashSet<>();

    }

    public static LocalAudioDataSource getInstance() {
        if (null == sInstance) {
            synchronized (LocalAudioDataSource.class) {
                if (null == sInstance) {
                    sInstance = new LocalAudioDataSource();
                }
            }
        }
        return sInstance;
    }

    @Override
    public List<Audio> getCache() {
        return mLocalAudios;
    }

    @Override
    public void scanLocal(LocalContract.ILocalScanListener listener) {
        Logger.i(TAG, "scan local " + (listener == null));
        //只能在主线程调用扫描

        ThreadUtil.checkMainThread("scanLocal");

        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }

        if (isScanning()) {
            return;
        }

        isScanning = true;
        mAudioSet.clear();

        mCountDisposable = Observable.interval(400, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        for (LocalContract.ILocalScanListener listener : mListeners) {
                            listener.onScanCount(mAudioSet.size());
                        }
                    }
                });

        Observable.just(getPathList())
                .observeOn(Schedulers.io())
                .map(new Function<List<String>, List<Audio>>() {
                    @Override
                    public List<Audio> apply(List<String> strings) throws Exception {
                        for (String path : strings) {
                            ScanUtil.scanRecursively(path, mAudioSet);
                        }
                        Logger.i(TAG, "scan size:%d", mAudioSet.size());

                        List<Audio> audios = new ArrayList<>();
                        audios.addAll(mAudioSet);

                        sortAudios(audios);

                        return audios;
                    }
                })
                .doOnNext(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        UpdateToCoreUtil.updateMusicModel(audios);
                        DBManager.getInstance().removeAllLocalAudios();

                        DBManager.getInstance().saveLocalAudios(audios);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        isScanning = false;
                        mCountDisposable.dispose();

                        mAudioSet.clear();
                        mLocalAudios.clear();
                        mLocalAudios.addAll(audios);
                        for (LocalContract.ILocalScanListener listener : mListeners) {
                            listener.onScanFinish(mLocalAudios);
                        }
                        mListeners.clear();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "scan exception:" + throwable.toString());

                        isScanning = false;
                        mCountDisposable.dispose();

                        mAudioSet.clear();
                        mLocalAudios.clear();
                        for (LocalContract.ILocalScanListener listener : mListeners) {
                            listener.onScanFinish(mLocalAudios);
                        }
                        mListeners.clear();
                    }
                });

    }


    private void sortAudios(List<Audio> audios) {
        int retryTime = 0;
        while (true) {
            int loadPinyinFileCode = LoadUtils.initPinyin();
            if (loadPinyinFileCode != 0 && retryTime <= 5) {
                LoadUtils.loadPinyinData();
                retryTime++;
            } else {
                break;
            }
        }
        if (retryTime <= 5) {
            // 排序
            Collections.sort(audios, new Comparator<Audio>() {

                @Override
                public int compare(Audio lhs, Audio rhs) {
                    return TXZStrComparator.compareChinese(lhs.getName(), rhs.getName());
                }
            });
        } else {
            Logger.e(TAG, "load pingyin file error");
            Collections.sort(audios, new Comparator<Audio>() {

                @Override
                public int compare(Audio lhs, Audio rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
        }
    }

    private List<String> getPathList() {
        List<String> pathList = new ArrayList<>();
        List<String> volumePath = StorageUtil.getVolumeState(GlobalContext.get());

        Logger.i(TAG, "volume path:%s", volumePath);
        pathList.addAll(volumePath);

        String localPaths = SharedPreferencesUtils.getLocalPaths();
        if (!TextUtils.isEmpty(localPaths)) {
            JSONBuilder jsonBuilder = new JSONBuilder(localPaths);
            String[] data = jsonBuilder.getVal("data", String[].class);

            if (null != data && data.length > 0) {
                Logger.i(TAG, "local path:%s", (Object[]) data);

                for (String path : data) {
                    if (!pathList.contains(path)) {
                        pathList.add(path);
                    }
                }
            }
        }

        String innerSDCardPath = com.txznet.txz.util.StorageUtil.getInnerSDCardPath();
        Logger.i(TAG, "inner path:%s", innerSDCardPath);

        if (!pathList.contains(innerSDCardPath)) {
            pathList.add(innerSDCardPath);
        }

        return pathList;
    }

    @Override
    public boolean isScanning() {
        return isScanning;
    }

    @Override
    public boolean isAvailable(Audio audio) {
        if (audio == null) {
            return false;
        }
        if (audio.getStrDownloadUrl().startsWith("http")) {
            return true;
        } else {
            return audio.isLocal();
        }
    }

    @Override
    public void deleteNotAvailableAudios(final LocalContract.ILocalScanListener listener) {
        Observable.just(getCache())
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<Audio>, List<Audio>>() {
                    @Override
                    public List<Audio> apply(List<Audio> audios) throws Exception {
                        List<Audio> unavailableAudios = new ArrayList<>();
                        Iterator<Audio> iterator = audios.iterator();
                        while (iterator.hasNext()) {
                            Audio audio = iterator.next();
                            if (!isAvailable(audio)) {
                                unavailableAudios.add(audio);
                                iterator.remove();
                            }
                        }
                        listener.onScanFinish(mLocalAudios);
                        return unavailableAudios;
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        DBManager.getInstance().removeLocalAudios(audios);
                        for (Audio audio : audios) {
                            HistoryData data = new HistoryData();
                            data.setId(audio.getId());
                            data.setSid(audio.getSid());
                            DBManager.getInstance().deleteHistory(data);
//                            DBManager.getInstance().removeHistoryAudio(audio);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "delete unavailable audio error:" + throwable.toString());
                    }
                });
    }

}
