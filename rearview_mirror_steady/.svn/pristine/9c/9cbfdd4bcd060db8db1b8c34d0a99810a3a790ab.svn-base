package com.txznet.music.model;

import android.os.SystemClock;
import android.support.v4.util.ArraySet;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.LocalAudioDao;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.BlackListAudio;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.Nothing;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.SyncCoreData;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.FileUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ScanUtil;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.ToastUtils;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 本地音频模块
 *
 * @author zackzhou
 */
public class LocalAudioModel extends RxWorkflow {

    public LocalAudioModel() {
    }

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_GET_LOCAL: // 查询本地
                getLocal(action);
                break;
            case ActionType.ACTION_LOCAL_DELETE: // 删除本地
                deleteLocal(action);
                break;
            case ActionType.ACTION_MEDIA_EJECT:
                ToastUtils.showShortOnUI("SD卡已删除");
                break;
            case ActionType.ACTION_MEDIA_SCANNER_STARTED:
            case ActionType.ACTION_SCAN: // 开始扫描
                scanLocal(action);
                break;
            case ActionType.ACTION_SCAN_CANCEL: // 取消扫描
                cancelScan(action);
                break;
            case ActionType.ACTION_PROXY_DOWNLOAD_COMPLETE:
                if (StorageUtil.isDiskSpaceEnough()) {
                    SharedPreferencesUtils.setDiskSpaceInsufficientTipCount(0);
                    PlayerErrorModel.hasTipDiskSpaceInsufficient = false;
                }
                insertIntoAudio(action);
                break;
            default:
                break;
        }
    }

    private void insertIntoAudio(RxAction oriAction) {
        AudioV5 audio = (AudioV5) oriAction.data.get(Constant.ProxyConstant.KEY_AUDIO);
        if (audio != null) {
            AppLogic.runOnBackGround(() -> {
                LocalAudio localAudio = AudioConverts.convert2LocalAudio(audio);
                localAudio.createTime = TimeManager.getInstance().getTimeMillis();
                DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().saveOrUpdate(localAudio);
                LocalActionCreator.get().getLocalAudio(Operation.AUTO);
            });
            SyncCoreData.updateLocalMusic(audio);
        }
    }

    private void getLocal(RxAction oriAction) {
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<LocalAudio>>) emitter -> {
            List<LocalAudio> list = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().listAll();
            for (LocalAudio audio : list) {
                FavourAudio favourAudio = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().get(audio.id, audio.sid);
                audio.isFavour = favourAudio != null;
            }
            emitter.onNext(list);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(localAudios -> {
                    oriAction.data.put(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO, localAudios);
                    postRxData(oriAction);
                }, throwable -> postRxError(oriAction, throwable));
        addRxAction(oriAction, disposable);
    }

    private void deleteLocal(RxAction oriAction) {
        List<LocalAudio> localAudioList = (List<LocalAudio>) oriAction.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO);
        Disposable disposable = Observable.create((ObservableOnSubscribe<Nothing>) emitter -> {
            DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().delete(localAudioList);
            List<LocalAudio> cantDeleteAudios = new ArrayList<>();
            for (LocalAudio audio : localAudioList) {
                boolean success = false;
                try {
                    success = FileUtils.delFile(AudioUtils.getLocalUrl(audio), Configuration.ThirdPath.PATHS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!success) {
                    cantDeleteAudios.add(audio);
                }
            }
            //  加入扫描黑名单
            if (!cantDeleteAudios.isEmpty()) {
                DBUtils.getDatabase(GlobalContext.get()).getBlackListAudioDao().saveOrUpdate(AudioConverts.convert2List(cantDeleteAudios, AudioConverts::convert2BlackListAudio));
                Logger.d(Constant.LOG_TAG_LOGIC, "cantDeleteAudios=" + cantDeleteAudios);
            }

            // 在执行本地删除后，检测tmd文件是不是小于500m或者空间是不是大于50m，是的话，归0
            if (StorageUtil.isDiskSpaceEnough() && StorageUtil.getTmdSize() >= StorageUtil.getMaxTMDSize()) {
                SharedPreferencesUtils.setDiskSpaceInsufficientTipCount(0);
                PlayerErrorModel.hasTipDiskSpaceInsufficient = false;
            }

            emitter.onNext(Nothing.NONE);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(localAudios -> {
                    postRxData(oriAction);
                }, throwable -> postRxError(oriAction, throwable));
        addRxAction(oriAction, disposable);
    }

    private void scanLocal(RxAction oriAction) {
        if (ScanUtil.isScanning()) {
            Logger.w(Constant.LOG_TAG_LOGIC, "is scanning now, intercept current");
            AppLogic.runOnSlowGround(() -> {
                if (ScanUtil.isScanning()) {
                    ScanUtil.interceptScanSync();
                    LocalActionCreator.get().scan(Operation.AUTO);
                }
            });
            return;
        }
        long startTime = SystemClock.elapsedRealtime();

        Disposable disposable = Observable.create(new ObservableOnSubscribe<List<LocalAudio>>() {
            final Set<LocalAudio> audioCache = new ArraySet<>();

            @Override
            public void subscribe(ObservableEmitter<List<LocalAudio>> emitter) throws Exception {
                synchronized (audioCache) {
                    audioCache.clear();
                }
                List<BlackListAudio> blackListAudioList = DBUtils.getDatabase(GlobalContext.get()).getBlackListAudioDao().listAll();
                ScanUtil.scanRecursively(audioCache, blackListAudioList, new ScanUtil.ScanCallback() {
                    {
                        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SCAN_COUNT)
                                .bundle(Constant.LocalConstant.KEY_SCAN_COUNT, 0)
                                .build());
                    }

                    @Override
                    public void onScanCountChanged(int count) {
                        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SCAN_COUNT)
                                .bundle(Constant.LocalConstant.KEY_SCAN_COUNT, audioCache.size())
                                .build());
                    }

                    @Override
                    public void onScanFinish(boolean isIntercept) {
                        AppLogic.runOnSlowGround(() -> {
                            // FIXME: 2019/3/13 对重复扫描优化
                            if (ScanUtil.isScanning() && isIntercept) {
                                return;
                            }
                            if (isIntercept && audioCache.size() == 0) {
                                return;
                            }
                            synchronized (audioCache) {
                                long now = SystemClock.elapsedRealtime();
                                LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
                                // 查询当前数据库
                                List<LocalAudio> dbData = localAudioDao.listAll();
                                // 剔除不存在的数据
                                Iterator<LocalAudio> iterator = dbData.iterator();
                                List<LocalAudio> bDelete = new ArrayList<>();
                                while (iterator.hasNext()) {
                                    LocalAudio localAudio = iterator.next();
                                    if (localAudio == null) {
                                        iterator.remove();
                                        continue;
                                    }

                                    // 检测该文件当前是否存在
                                    if (AudioUtils.isLocalSong(localAudio.sid) && !FileUtils.isExist(localAudio.sourceUrl)) {
                                        iterator.remove();
                                        bDelete.add(localAudio);
                                        continue;
                                    }

                                    // 非打断式完成扫描
                                    if (!isIntercept) {
                                        if (!audioCache.contains(localAudio)) {
                                            iterator.remove();
                                            bDelete.add(localAudio);
                                            continue;
                                        }
                                    }
                                    // 合并收藏状态
                                    FavourAudio favourAudio = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().get(localAudio.id, localAudio.sid);
                                    localAudio.isFavour = favourAudio != null;
                                }
                                localAudioDao.delete(bDelete);
                                // 遍历新扫描出来的结果
                                List<LocalAudio> bAdd = new ArrayList<>();
                                iterator = audioCache.iterator();
                                List<LocalAudio> bSave = new ArrayList<>();
                                while (iterator.hasNext()) {
                                    LocalAudio audio = iterator.next();

                                    if (audio == null) {
                                        iterator.remove();
                                        continue;
                                    }

                                    // 扫描过程中可能会穿插删除操作
                                    // 检测该文件当前是否存在
                                    if (AudioUtils.isLocalSong(audio.sid) && !FileUtils.isExist(audio.sourceUrl)) {
                                        iterator.remove();
                                        continue;
                                    } else {
                                        // 校验tmd文件存在性
                                        if (AudioUtils.isNetSong(audio.sid) && !FileUtils.isExist(AudioUtils.getAudioTMDFile(audio))) {
                                            iterator.remove();
                                            continue;
                                        }
                                    }

                                    // 不存在当前列表中
                                    if (!dbData.contains(audio)) {
                                        bAdd.add(audio);
                                        bSave.add(audio);
                                    }
                                }
                                localAudioDao.saveOrUpdate(bSave);
                                dbData.addAll(bAdd);

                                Logger.w(Constant.LOG_TAG_LOGIC, "scan merge finish, cost time=" + (SystemClock.elapsedRealtime() - now));
                                emitter.onNext(dbData);
                                emitter.onComplete();
                                audioCache.clear();
                            }
                        });
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single()).doOnNext(SyncCoreData::updateMusicModel)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(localAudios -> {
                    // FIXME: 2019/3/13 对重复扫描优化
                    if (ScanUtil.isScanning()) {
                        return;
                    }
                    PlayHelper.get().mergeQueueWithLocalAudio(localAudios);
                    oriAction.data.put(Constant.LocalConstant.KEY_IS_INTERCEPTED, ScanUtil.isIntercepted());
                    oriAction.data.put(Constant.LocalConstant.KEY_COST_TIME, SystemClock.elapsedRealtime() - startTime);
                    Logger.w(Constant.LOG_TAG_LOGIC, "scan finish, cost time=" + (SystemClock.elapsedRealtime() - startTime));
                    oriAction.data.put(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO, localAudios);
                    postRxData(oriAction);
                }, throwable -> {
                    postRxError(oriAction, throwable);
                });
        addRxAction(oriAction, disposable);
    }


    // FIXME 产品定义，取消扫描时，已扫描了多少列表就多少
    private void cancelScan(RxAction oriAction) {
        ScanUtil.interceptScan(); // 打算扫描，如果有处于阻塞中的scanRecursively就会立即完成
    }

}
