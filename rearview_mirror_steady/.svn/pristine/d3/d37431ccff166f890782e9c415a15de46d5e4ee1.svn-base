package com.txznet.music.localModule;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.jni.TXZStrComparator;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.localModule.logic.ScanUtil;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.LoadUtils;
import com.txznet.music.utils.MyLog;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ThreadUtil;
import com.txznet.music.utils.UpdateToCoreUtil;
import com.txznet.music.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
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
    private List<Audio> mLocalCheckAudios;
    private Set<Audio> mAudioSet;
    private boolean isScanning;
    private List<LocalContract.ILocalScanListener> mListeners;
//    private Disposable mCountDisposable;

    private LocalAudioDataSource() {
        mLocalAudios = new ArrayList<>();
        mLocalCheckAudios = new ArrayList<>();
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
    public List<Audio> getCheckIndexs() {
        return mLocalCheckAudios;
    }

    @Override
    public void scanLocal(LocalContract.ILocalScanListener listener) {
        MyLog.printCurrentMemory("Scan method start");
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

//        mCountDisposable = Observable.interval(1, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        for (LocalContract.ILocalScanListener listener : mListeners) {
//                            listener.onScanCount(mAudioSet.size());
//                        }
//                    }
//                });

        Observable.just(getPathList())
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<List<String>, List<Audio>>() {
                    @Override
                    public List<Audio> apply(List<String> strings) throws Exception {
                        MyLog.printCurrentMemory("recursively method start");
                        ScanUtil.jsonCountSize = 0;
                        for (String path : strings) {
                            ScanUtil.scanRecursively(path, mAudioSet);
                            System.gc();
                        }
                        MyLog.printCurrentMemory("recursively method end");
                        Logger.i(TAG, "scan size:%d , %d", mAudioSet.size(), ScanUtil.jsonCountSize);

                        mLocalAudios.clear();
                        mLocalAudios.addAll(mAudioSet);
                        mAudioSet.clear();
                        MyLog.printCurrentMemory("add audios method end");
                        sortAudios(mLocalAudios);
                        MyLog.printCurrentMemory("sort audios method end");
                        return mLocalAudios;
                    }
                })
                .doOnNext(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        MyLog.printCurrentMemory("save db method start");
                        UpdateToCoreUtil.updateMusicModel(audios);
                        DBManager.getInstance().removeAllLocalAudios();

                        DBManager.getInstance().saveLocalAudios(audios);
                        MyLog.printCurrentMemory("save db method end");
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        MyLog.printCurrentMemory("scan finish method start");
                        isScanning = false;
//                        mCountDisposable.dispose();

//                        mAudioSet.clear();
//                        mLocalAudios.clear();
//                        mLocalAudios.addAll(audios);
//                        TestUtil.printList("test:getLocalData:ScanFinish:", mLocalAudios);
                        for (LocalContract.ILocalScanListener listener : mListeners) {
                            listener.onScanFinish(mLocalAudios);
                        }
                        mListeners.clear();
                        MyLog.printCurrentMemory("scan finish method end");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "scan exception:" + throwable.toString());

                        isScanning = false;
//                        mCountDisposable.dispose();

                        mAudioSet.clear();
                        mLocalAudios.clear();
                        for (LocalContract.ILocalScanListener listener : mListeners) {
                            listener.onScanFinish(mLocalAudios);
                        }
                        mListeners.clear();
                    }
                });
        MyLog.printCurrentMemory("Scan method end");
    }

    @Override
    public Observable<Audio> findLocalDBDate() {
        return io.reactivex.Observable.fromIterable(DBManager.getInstance().findAllLocalAudios())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
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
                        mLocalAudios.clear();
                    }
                })
                .subscribeOn(Schedulers.io())//
                .sorted(new Comparator<Audio>() {

                    @Override
                    public int compare(Audio lhs, Audio rhs) {
                        try {
                            return TXZStrComparator.compareChinese(lhs.getName(), rhs.getName());
                        } catch (Throwable throwable) {
                            Logger.w(TAG, "load,pinyin so failed ,sort by string.compareTo()");
                            return lhs.getName().compareTo(rhs.getName());
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//
                .cache()
                .doOnNext(v -> {
                    mLocalAudios.add(v);
                })
                ;//
//                .subscribe(v -> {
//                    audios.add(v);
//                }, e -> {
//                    mView.showEmpty();
//                }, () -> {
//                    if (audios.isEmpty()) {
//                        mView.showEmpty();
//                    } else {
//                        mView.showContent();
//                        mView.showLocalData(audios);
//                    }
//                    //如果有新增的歌曲则需要添加进播单
//                    PlayerBizLogic.getInstance().updatePlayerList(audios, PlayInfoManager.DATA_LOCAL);
//                });


//        mLocalAudios.clear();
//        mLocalAudios.addAll(DBManager.getInstance().findAllLocalAudios());
//        return mLocalAudios;
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
            TXZStrComparator.release();
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
//        pathList.add(StorageUtil.getTmdDir());

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
        return FileUtils.isExist(getLocalUrl(audio));
    }

    public String getLocalUrl(Audio audio) {
        if (audio == null) {
            return "";
        }
        if (Utils.isLocalSong(audio.getSid())) {
            //真实本地音乐，来源于SD卡中的，自带音乐
            return audio.getStrDownloadUrl();
        } else {
            //来源于缓存的同听音乐
            return StorageUtil.getTmdDir() + File.separator + audio.getId() + Utils.UNDERLINE + audio.getSid() + Utils.TMD_POSTFIX;
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
                        deleteNotExistHistoryData(audios);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "delete unavailable audio error:" + throwable.toString());
                    }
                });
    }

    /**
     * 删除不存在的历史记录
     *
     * @param audios
     */
    public void deleteNotExistHistoryData(List<Audio> audios) {
        for (Audio audio : audios) {
            //【【同听4.4.0】【历史音乐】删除同听缓存的音乐后，对应的历史记录也被删除了，应保留该历史记录】
            //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=112171188100100383
            if (Utils.isLocalSong(audio.getSid()) && !isAvailable(audio)) {
                HistoryData data = new HistoryData();
                data.setId(audio.getId());
                data.setSid(audio.getSid());
                DBManager.getInstance().deleteHistory(data);
            }
        }
    }

    /**
     * 删除不可以播放的audio
     * 不可以播放，仅为播放的本地数据，不存在的情况，
     * 如果播放的是在线的数据，则认为可以播放，不删除
     *
     * @param audios
     */
    public void deleteCantPlayAudios(List<Audio> audios) {
        //需要删除路径不存在的本地音乐数据
        deleteNotExistHistoryData(audios);//这里没有切换线程！！！
        for (int i = audios.size() - 1; i >= 0; i--) {
            Audio audio = audios.get(i);
            if (Utils.isLocalSong(audio.getSid()) && !isAvailable(audio)) {
                //来源于sd卡中的
                audios.remove(audio);
            }
            //来源于其他途径的不删除
        }
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                LocalAudioDataSource.getInstance().scanLocal(null);
            }
        });
    }

    @Override
    public void deleteAudios(List<Audio> audios, boolean isNeedDeleteFinalFile, LocalContract.IDeleteListener listener) {
        List<Audio> audios1 = new ArrayList<>(audios);
        //不能使用audio，因为audio对象在外面被清空了，这里是引用传递，故需要复制一份出来
        Observable.just(audios1).flatMap(new Function<List<Audio>, ObservableSource<? extends List<Audio>>>() {
            @Override
            public ObservableSource<? extends List<Audio>> apply(List<Audio> audios) throws Exception {
                DBManager.getInstance().removeLocalAudios(audios);
                List<Audio> cantDeleteAudios = new ArrayList<>();
                for (Audio audio : audios) {
                    if (isNeedDeleteFinalFile) {
                        boolean success = FileUtils.delFile(getLocalUrl(audio));
                        if (!success) {
                            cantDeleteAudios.add(audio);
                        } else {
                            ReportEvent.clickLocalDelete(audio, audio.getName());//上报删除
                        }
                    }
                }
                deleteNotExistHistoryData(audios);
                return Observable.just(cantDeleteAudios);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(v -> {
            if (listener != null) {
                if (CollectionUtils.isNotEmpty(v)) {
                    listener.onError(v);
                } else {
                    listener.onSuccess(audios1);
                }
            }
        }, e -> {
            if (listener != null) {
                listener.onError(Collections.EMPTY_LIST);
            }
        });
    }

    @Override
    public Observable<List<Audio>> addAudio(Audio audio) {
        ArrayList<Audio> audios = new ArrayList<>(getCache());
        audios.add(audio);
        return Observable.just(audios)
                .map(new Function<List<Audio>, List<Audio>>() {
                    @Override
                    public List<Audio> apply(List<Audio> audios) throws Exception {
                        sortAudios(audios);
                        mLocalAudios.clear();
                        mLocalAudios.addAll(audios);
                        return mLocalAudios;
                    }
                });
    }

}
