//package com.txznet.music.localModule.logic;
//
//import android.os.Environment;
//
//import com.txznet.audio.player.audio.TmdFile;
//import com.txznet.comm.remote.GlobalContext;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.comm.util.CollectionUtils;
//import com.txznet.comm.util.JSONBuilder;
//import com.txznet.comm.util.StringUtils;
//import com.txznet.fm.bean.InfoMessage;
//import com.txznet.fm.manager.ObserverManage;
//import com.txznet.jni.TXZStrComparator;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.IFinishCallBack;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.favor.FavorHelper;
//import com.txznet.music.playerModule.logic.PlayInfoManager;
//import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
//import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
//import com.txznet.music.utils.FileUtils;
//import com.txznet.music.utils.JsonHelper;
//import com.txznet.music.utils.LoadUtils;
//import com.txznet.music.utils.MyAsyncTask;
//import com.txznet.music.utils.SDUtils;
//import com.txznet.music.utils.ScanFileUtils;
//import com.txznet.music.utils.SharedPreferencesUtils;
//import com.txznet.music.utils.ToastUtils;
//import com.txznet.music.utils.UpdateToCoreUtil;
//import com.txznet.music.utils.Utils;
//import com.txznet.txz.util.StorageUtil;
//
//import java.io.File;
//import java.io.FilenameFilter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created by ASUS User on 2016/11/3.
// */
//public class LocalMusicEngine {
//
//    public static final long MAX_CACHE_SIZE = 1024 * 1024 * 500;
//    public static final long MIN_DISK_SIZE = 1024 * 1024 * 50;
//    public static final long MAX_TMD_SIZE = 1024 * 1024 * 500;
//    private static final String TAG = "LocalMusicEngine:";
//    private static LocalMusicEngine mEngine;
//    private boolean mIsScanning = false;
//    private boolean mIsQuerying = false;
//    private List<Audio> mLocalAudios;
//
//    private LocalMusicEngine() {
//        mLocalAudios = new ArrayList<Audio>();
//    }
//
//    public static LocalMusicEngine getInstance() {
//        if (mEngine == null) {
//            synchronized (LocalMusicEngine.class) {
//                if (mEngine == null) {
//                    mEngine = new LocalMusicEngine();
//                }
//            }
//        }
//        return mEngine;
//    }
//
//    /**
//     * ?????????????????????????????????
//     *
//     * @param audio
//     * @return ?????????????????????true ????????????false
//     */
//    public boolean isValid(Audio audio) {
//        if (audio == null) {
//            return false;
//        }
//        if (audio.getStrDownloadUrl().startsWith("http")) {
//            return true;
//        } else {
//            return audio.isLocal();
//        }
////
////
////        if (audio.getStrDownloadUrl().endsWith(".tmd")){
////
////        }else if (FileUtils.isExist(audio.getStrDownloadUrl())){
////
////        }else{
////
////        }
////
////
////        if (audio.getSid() == Constant.LOCAL_MUSIC_TYPE && !FileUtils.isExist(audio.getStrDownloadUrl())) {
//////			TtsUtilWrapper.speakResource("RS_VOICE_SPEAKNOTEXIST_TIPS", Constant.RS_VOICE_SPEAKNOTEXIST_TIPS);
////            return false;
////        }
////        return true;
//////        return audio.isLocal();
//    }
//
//
//    public List<String> getSdcardPaths() {
//        List<String> allExterSdcardPath = com.txznet.music.localModule.logic.StorageUtil.getVolumeState(GlobalContext.get());
//        if (StringUtils.isNotEmpty(SharedPreferencesUtils.getLocalPaths())) {
//            JSONBuilder builder = new JSONBuilder(SharedPreferencesUtils.getLocalPaths());
//            String[] val = builder.getVal("data", String[].class);
//
//            for (int i = 0; i < val.length; i++) {
//                allExterSdcardPath.add(val[i]);
//            }
//        }
//
//        LogUtil.logd(TAG + "[path]exterSdcardPath=" + allExterSdcardPath.toString() + ",innerSDcardPath=" + StorageUtil.getInnerSDCardPath());
//        if (CollectionUtils.isNotEmpty(allExterSdcardPath)) {
//            if (!allExterSdcardPath.contains(StorageUtil.getInnerSDCardPath())) {
//                allExterSdcardPath.add(StorageUtil.getInnerSDCardPath());
//            }
//        }
//        return allExterSdcardPath;
//    }
//
//    public synchronized List<Audio> getDataFromSDCard() {
//
//        List<File> resultFiles = new ArrayList<File>();
//        List<String> sdcardPaths = getSdcardPaths();
//
//        //??????SD????????????
//        if (CollectionUtils.isNotEmpty(sdcardPaths)) {
//            SharedPreferencesUtils.setSdcardPath(JsonHelper.toJson(sdcardPaths));
//            for (int i = 0; i < sdcardPaths.size(); i++) {
//                resultFiles.addAll(ScanFileUtils.getFiles(sdcardPaths.get(i)));
//            }
//        }
////        resultFiles.addAll(ScanFileUtils.getFiles(StorageUtil.getInnerSDCardPath()));
//        // ?????????????????????
//        HashSet<Audio> audios = new HashSet<Audio>();
//        List<Audio> list = new ArrayList<Audio>();
//        LogUtil.d(TAG, "get data from sd card size:" + resultFiles.size());
//        DBManager.getInstance().removeAllLocalAudios();
//        if (resultFiles != null && resultFiles.size() > 0) {
//            // ?????????????????????
//            for (File file : resultFiles) {
//                Audio audio = null;
//                TmdFile openFile = null;
//                // ?????????tmd??????????????????
//                if (file.getAbsolutePath().endsWith(".tmd")) {
//                    try {
//                        openFile = TmdFile.openFile(file, -1, false);
//                        if (openFile == null) {
//                            audio = PlayEngineFactory.getEngine().getCurrentAudio();
//                        } else {
//                            audio = JsonHelper.toObject(Audio.class, new String(openFile.loadInfo()));
//                        }
//                        if (audio != null) {
//                            audio.setStrDownloadUrl(file.getAbsolutePath());
//                        }
//
//                    } catch (Exception e) {
//                        LogUtil.loge(TAG + "path=" + file.getAbsolutePath(), e);
//                    } finally {
//                        if (openFile != null) {
//                            openFile.closeQuitely();
//                        }
//                    }
//                } else {
//                    audio = new Audio();
//                    audio.setSid(0);
//                    // audio.setDuration(file);
//                    audio.setStrDownloadUrl(file.getAbsolutePath());
//                    audio.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
//                    audio.setDownloadType("0");
////                    audio.setSourceFrom("????????????");
//                }
//                if (audio == null) {
//                    continue;
//                }
//                audio.setLocal(true);
//                if (audio.getId() == 0) {
//                    audio.setId(Math.abs(audio.getName().hashCode()));// ????????????????????????id
//                }
//                //????????????:??????3.0,??????????????????????????????,????????????????????????.?????????id???????????????(??????????????????id??????????????????100000).????????????????????????,????????????????????????
//                if (String.valueOf(audio.getId()).startsWith("100000")) {
//                    audio.setSid(0);
//                }
////                audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
//                // ?????? p ??? property ?????????????????? Unicode ??????????????? Unicode
//                // ??????????????????????????????????????????P?????????Unicode ???????????????????????????????????????????????????
//                String desc = audio.getName().replaceAll("[\\p{P}]", "");
//                audio.setDesc(desc);
//                if (!audios.add(audio)) {
//                    LogUtil.logd(TAG + "more one same audios :" + audio.getStrDownloadUrl());
//                }
//                audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), 1, 1));//??????????????????
//                if (FavorHelper.isFavourFromDB(audio)) {
//                    audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), 1, 2));//??????????????????
//                } else {
//                    audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), 0, 2));//??????????????????
//                }
//            }
//            // Set ???List
//            Iterator<Audio> iterator = audios.iterator();
//            while (iterator.hasNext()) {
//                Audio audio = (Audio) iterator.next();
//                if (new File(audio.getStrDownloadUrl()).exists()) {
//                    list.add(audio);
//                } else {
//                    LogUtil.logd(TAG + "not exist file:" + audio.getStrDownloadUrl());
//                }
//            }
//
//
//            DBManager.getInstance().saveLocalAudios(list);
//            UpdateToCoreUtil.updateMusicModel(list);
//        }
//
//        LogUtil.d(TAG, "get data from sd card final size:" + list.size());
//        return list;
//    }
//
//
//    private void notifyLocalAudioChanged(final List<Audio> audios) {
//        AppLogic.runOnUiGround(new Runnable() {
//            @Override
//            public void run() {
//                mLocalAudios.clear();
//                mLocalAudios.addAll(audios);
//                InfoMessage info = new InfoMessage();
//                info.setType(InfoMessage.SCAN_FINISHED);
//                ObserverManage.getObserver().setMessage(info);
//            }
//        });
//    }
//
//    public void getData() {
//        if (mIsQuerying) {
//            return;
//        }
//        mIsQuerying = true;
//        AppLogic.runOnBackGround(new Runnable() {
//            @Override
//            public void run() {
//                final List<Audio> allLocalAudios = DBManager.getInstance().findAllLocalAudios();
//                processOrder(allLocalAudios);
//                LogUtil.d(TAG, "get data size:" + allLocalAudios);
//                AppLogic.runOnUiGround(new Runnable() {
//                    @Override
//                    public void run() {
//                        mIsQuerying = false;
//                        notifyLocalAudioChanged(allLocalAudios);
//                    }
//                });
//            }
//        });
//    }
//
//
//    /**
//     * ??????
//     *
//     * @return
//     */
//    public List<Audio> processOrder(List<Audio> audios) {
//        int retryTime = 0;
//        while (true) {
//            int loadPinyinFileCode = LoadUtils.initPinyin();
//            if (loadPinyinFileCode != 0 && retryTime <= 5) {
//                LoadUtils.loadPinyinData();
//                retryTime++;
//            } else {
//                break;
//            }
//        }
//        if (retryTime <= 5) {
//            // ??????
//            Collections.sort(audios, new Comparator<Audio>() {
//
//                @Override
//                public int compare(Audio lhs, Audio rhs) {
//                    return TXZStrComparator.compareChinese(lhs.getName(), rhs.getName());
//                }
//            });
//        } else {
//            LogUtil.loge(TAG + "pinyin:loadpinyindataFile is error");
//            Collections.sort(audios, new Comparator<Audio>() {
//
//                @Override
//                public int compare(Audio lhs, Audio rhs) {
//                    return lhs.getName().compareTo(rhs.getName());
//                }
//            });
//        }
//        return audios;
//    }
//
//
//    public void scanLocalMusic() {
//        if (mIsScanning) {
//            ToastUtils.showShortOnUI("?????????????????????...");
//            return;
//        }
//        mIsScanning = true;
//        new MyAsyncTask<Integer, List<Audio>>() {
//            protected void onPreExecute() {
//                InfoMessage info = new InfoMessage();
//                info.setType(InfoMessage.SCAN_STATED);
//                ObserverManage.getObserver().setMessage(info);
//            }
//
//            @Override
//            protected List<Audio> doInBackground(Integer... params) {
//                List<Audio> dataFromSDCard = getDataFromSDCard();
//                processOrder(dataFromSDCard);
//                LogUtil.logd(TAG + "scan size =" + dataFromSDCard.size());
//                return dataFromSDCard;
//            }
//
//            @Override
//            protected void onPostExecute(List<Audio> result) {
//                mIsScanning = false;
//                notifyLocalAudioChanged(result);
//            }
//
//
//        }.execute();
//    }
//
//
//    public void deleteNotExistFile(final List<Audio> notExist,
//                                   final IFinishCallBack<Audio> callback) {
//        AppLogic.runOnBackGround(new Runnable() {
//
//            @Override
//            public void run() {
//                if (null != notExist) {
//                    LogUtil.logd("remain  begin num:" + (CollectionUtils.isNotEmpty(notExist) ? notExist.size() : 0));
//                    synchronized (notExist) {
//
//                        int count = notExist.size() - 1;
//                        for (int i = count; i >= 0; i--) {
//                            Audio notExistSong = notExist.get(i);
//                            if (!isValid(notExistSong)) {
//                                LogUtil.logd(TAG + "delete not exist file :" + notExistSong.getStrDownloadUrl());
//                                PlayInfoManager.getInstance().removePlayListAudio(notExistSong);
//                                DBManager.getInstance().removeLocalAudios(notExistSong);
//                                DBManager.getInstance().removeHistoryAudio(notExistSong);
//                                notExist.remove(i);
//                            }
//                        }
//                    }
//                }
//                if (callback != null) {
//                    AppLogic.runOnUiGround(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            LogUtil.logd("remain end  num:" + (CollectionUtils.isNotEmpty(notExist) ? notExist.size() : 0));
//                            callback.onComplete(notExist);
//                            //????????????
//                            ObserverManage.getObserver().send(InfoMessage.PLAYER_LIST);
//                        }
//                    }, 0);
//                }
//            }
//        }, 0);
//    }
//
//    public List<Audio> getLocalAudios() {
//        return mLocalAudios;
//    }
//
//
//    public List<Audio> findLocalAudioForSearch(ReqSearch reqData) {
//        return DBManager.getInstance().findLocalAudioForSearch(reqData);
//    }
//
//    /**
//     * ???????????????????????????
//     *
//     * @return ?????????????????????
//     */
//    public String getSongCacheDir() {
//        return Environment.getExternalStorageDirectory() + "/txz/cache/song";
//    }
//
//    /**
//     * ???????????????(??????????????????????????????)???????????????????????????
//     *
//     * @return ?????????????????????????????????
//     */
//    public String getOtherCacheDir() {
//        return Environment.getExternalStorageDirectory() + "/txz/cache/other";
//    }
//
//
//    /**
//     * ??????TMD????????????
//     *
//     * @return TMD????????????
//     */
//    public String getTmdDir() {
//        return Environment.getExternalStorageDirectory() + "/txz/audio/song";
//    }
//
//
//    /**
//     * ???????????????tmp????????????
//     *
//     * @return ?????????tmp????????????
//     */
//    public long getCacheSize() {
//        long songCacheSize = 0;
//        long otherCacheSize = 0;
//
//        FilenameFilter tmpFileNameFilter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                if (name.endsWith(".tmp")) {
//                    return true;
//                }
//                return false;
//            }
//        };
//
//        File songCacheFile = new File(getSongCacheDir());
//        if (songCacheFile.exists()) {
//            File[] files = songCacheFile.listFiles(tmpFileNameFilter);
//            if (null != files) {
//                for (File f : files) {
//                    songCacheSize += f.length();
//                }
//            }
//        }
//
//        File otherCacheFile = new File(getOtherCacheDir());
//        if (otherCacheFile.exists()) {
//            File[] files = otherCacheFile.listFiles(tmpFileNameFilter);
//            if (null != files) {
//                for (File f : files) {
//                    otherCacheSize += f.length();
//                }
//            }
//        }
//        LogUtil.d(TAG + "song cache size " + SDUtils.formatSize(songCacheSize) + " other cache size:" + SDUtils.formatSize(otherCacheSize));
//        return songCacheSize + otherCacheSize;
//    }
//
//    /**
//     * ?????????????????????tmd???????????????
//     *
//     * @return ???????????????tmd???????????????
//     */
//    public long getTmdSize() {
//        long size = 0;
//        File tmdFile = new File(getTmdDir());
//        if (tmdFile.exists()) {
//            File[] files = tmdFile.listFiles(new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String name) {
//                    if (name.endsWith(".tmd")) {
//                        return true;
//                    }
//                    return false;
//                }
//            });
//            if (null != files) {
//                for (File f : files) {
//                    size += f.length();
//                }
//            }
//        }
//        LogUtil.d(TAG + "tmd size " + SDUtils.formatSize(size));
//        return size;
//    }
//
//
//    /**
//     * ???????????????tmp??????
//     *
//     * @param percent ?????????????????????????????????????????????????????????0-1
//     */
//    public void deleteCache(float percent) {
//        long totalSize = 0;
//        List<File> fileList = new ArrayList<>();
//        FilenameFilter tmpFileNameFilter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                if (name.endsWith(".tmp")) {
//                    return true;
//                }
//                return false;
//            }
//        };
//
//        File songCacheFile = new File(getSongCacheDir());
//        if (songCacheFile.exists()) {
//            File[] files = songCacheFile.listFiles(tmpFileNameFilter);
//            if (null != files) {
//                for (File f : files) {
//                    totalSize += f.length();
//                    fileList.add(f);
//                }
//            }
//        }
//
//        File otherCacheFile = new File(getOtherCacheDir());
//        if (otherCacheFile.exists()) {
//            File[] files = otherCacheFile.listFiles(tmpFileNameFilter);
//            if (null != files) {
//                for (File f : files) {
//                    totalSize += f.length();
//                    fileList.add(f);
//                }
//            }
//        }
//
//        Collections.sort(fileList, new Comparator<File>() {
//            @Override
//            public int compare(File lhs, File rhs) {
//                if (lhs.lastModified() == rhs.lastModified()) {
//                    return 0;
//                }
//                return lhs.lastModified() < rhs.lastModified() ? -1 : 1;
//            }
//        });
//
//        LogUtil.d(TAG + "delete cache, total size:" + SDUtils.formatSize(totalSize) + " percent:" + percent);
//        long size = (long) (totalSize * percent);
//        for (File f : fileList) {
//            LogUtil.d(TAG + "delete cache " + f.getName() + " " + SDUtils.formatSize(f.length()));
//            if (f.delete()) {
//                size -= f.length();
//            }
//            if (size <= 0) {
//                break;
//            }
//        }
//
//    }
//
//
//    /**
//     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//     *
//     * @return ???????????????????????????????????????
//     */
//    public boolean checkCacheSize() {
//        if (SDUtils.getAvailableSize() <= MIN_DISK_SIZE) {
//            deleteCache(1.0f);
//        } else if (getCacheSize() > MAX_CACHE_SIZE) {
//            deleteCache(0.3f);
//        }
//        return SDUtils.getAvailableSize() > MIN_DISK_SIZE;
//    }
//
//
//    /**
//     * ????????????????????????????????????
//     *
//     * @return ??????????????????????????????
//     */
//    public boolean isDiskSpaceEnough() {
//        return SDUtils.getAvailableSize() > MIN_DISK_SIZE;
//    }
//
//}
