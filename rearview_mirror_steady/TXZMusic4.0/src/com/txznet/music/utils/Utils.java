package com.txznet.music.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.dao.DaoManager;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.ui.SplashActivity;
import com.txznet.music.ui.bean.FlashPage;
import com.txznet.music.ui.bean.PlayConf;
import com.txznet.music.ui.net.response.RespCheck;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity9;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.tongting.TongTingUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * @author telenewbie
 * @version 创建时间：2016年2月29日 上午10:50:21
 */
public class Utils {

    public static final String UNDERLINE = "_";
    public static final String TMD_POSTFIX = ".tmd";
    private static final String TAG_JUMP = "music:jump:open:";
    private static String TAG = "music:util:";
    //    private static WeakReference<RespCheck> sRefCheck;
    private static RespCheck mRespCheck;
    public static final String KEY_TYPE = "KEY_TYPE";


    /**
     * @param context
     * @return
     * @desc <pre>
     * 获取网络对象
     * </pre>
     * @author Erich Lee
     * @date Mar 7, 2013
     */
    public static NetworkInfo getNetworkInfo(Context context) {
        if (null == context) {
            return null;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivity.getActiveNetworkInfo();
    }

    /**
     * @param context
     * @return
     * @desc <pre>
     * 检查网络是否连接
     * </pre>
     * @date Mar 7, 2013
     */
    public static boolean isNetworkConnected(Context context) {
        boolean netSataus = false;
        NetworkInfo networkInfo = getNetworkInfo(context);
        if (networkInfo != null) {
            netSataus = networkInfo.isAvailable();
        }
        return netSataus;
    }

    /**
     * @return
     * @desc <pre>
     * 判断当前的网络状态
     * </pre>
     */
    public static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        LogUtil.logd("telephonyManager.getNetworkType()::"
                + telephonyManager.getNetworkType());
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false; // ~ 14-64 kbps
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false; // ~ 50-100 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true; // ~ 400-1000 kbps
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true; // ~ 600-1400 kbps
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false; // ~ 100 kbps
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true; // ~ 2-14 Mbps
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true; // ~ 700-1700 kbps
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true; // ~ 1-23 Mbps
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true; // ~ 400-7000 kbps
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false; // ~25 kbps
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    public static List<Album> getAlbumByAudios(List<Audio> audios) {
        if (audios == null || audios.size() == 0) {
            return null;
        }
        List<Album> albums = new ArrayList<>();
        for (Audio audio : audios) {
            Album album = DBManager.getInstance().findAlbumById(Long.parseLong(audio.getAlbumId()), audio.getSid());
            if (album != null && !albums.contains(album)) {
                albums.add(album);
            }
        }
        return albums;
    }

    public static FlashPage getFlashPage() {
        FlashPage flashPage = null;
        String config = SharedPreferencesUtils.getConfig();
        if (StringUtils.isNotEmpty(config)) {
            RespCheck respCheck = JsonHelper.toObject(RespCheck.class, config);
            flashPage = respCheck.getFlashPage();
        }
        return flashPage;
    }

    /**
     * 通过sid判断音频是否为音乐
     *
     * @param sid sid
     * @return 如果是音乐则返回true
     */
    public static boolean isSong(int sid) {
        boolean localSong = isLocalSong(sid);
        if (localSong) {
            return true;
        }
        boolean netSong = isNetSong(sid);
        if (netSong) {
            return true;
        }
        return false;
    }

    /**
     * 通过sid判断音频是否为音乐
     *
     * @param sid sid
     * @return 如果是音乐则返回true
     */
    public static boolean isNetSong(int sid) {
        if (getConfig() != null && getConfig().getArrPlay() != null) {
            for (PlayConf playConf : getConfig().getArrPlay()) {
                if (playConf.getSid() == sid) {
                    return playConf.getType() == PlayConf.MUSIC_TYPE;
                }
            }
        }
        return false;
    }

    /**
     * 是否需要跟后台去换地址
     *
     * @param audio
     * @return
     */
    public static boolean isNeedPreLoad(Audio audio) {
        if (null == audio) {
            return false;
        }
        return TextUtils.equals(audio.getDownloadType(), "1");
    }

    public static boolean isLocalSong(int sid) {
        if (sid == 0) {// 本地歌曲
            return true;
        }
        return false;
    }

    /**
     * 获取歌曲的全部sid
     *
     * @return sid
     */
    public static List<Integer> getSongSid() {
        List<Integer> sids = new ArrayList<Integer>();
        sids.add(0);
        sids.addAll(getSongSidFromNet());
        return sids;
    }

    public static List<Integer> getSongSidFromNet() {
        List<Integer> sids = new ArrayList<Integer>();
        if (getConfig() != null && getConfig().getArrPlay() != null) {
            for (PlayConf playConf : getConfig().getArrPlay()) {
                if (playConf.getType() == PlayConf.MUSIC_TYPE) {
                    sids.add(playConf.getSid());
                }
            }
        }
        LogUtil.d("music:sid:", sids);
        return sids;
    }

    /**
     * 获得电台的源ID
     *
     * @return fm的sid
     */
    public static List<Integer> getFMSid() {
        List<Integer> sids = new ArrayList<Integer>();
        if (getConfig() != null && getConfig().getArrPlay() != null) {
            for (PlayConf playConf : getConfig().getArrPlay()) {
                if (playConf.getType() != PlayConf.MUSIC_TYPE) {
                    sids.add(playConf.getSid());
                }
            }
        }
        return sids;
    }

    /**
     * 根据不同的类型返回不同的源
     *
     * @param type 音乐，电台，直播等
     * @return sid
     */
    public static List<Integer> getSidByType(int type) {
        List<Integer> songSids = new ArrayList<Integer>();
        if (getConfig() != null && getConfig().getArrPlay() != null) {
            for (PlayConf playConf : getConfig().getArrPlay()) {
                if (playConf.getType() == type) {
                    songSids.add(playConf.getSid());
                }
            }
        }
        return songSids;
    }


    public static SpannableString getTitleAndArtists(String title,
                                                     String artists) {
        StringBuffer sBuffer = new StringBuffer();
        if (StringUtils.isNotEmpty(artists)) {
            sBuffer.append(artists);
        }
        if (sBuffer.length() > 0) {
            sBuffer.insert(0, "-");
        }

        SpannableString spannableString = new SpannableString(title
                + sBuffer.toString());
        spannableString.setSpan(new AbsoluteSizeSpan(30), 0, title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ColorStateList color = ColorStateList.valueOf(GlobalContext.get()
                .getResources().getColor(R.color.gray));
        spannableString.setSpan(new TextAppearanceSpan(null, Typeface.NORMAL,
                        24, color, null), title.length(), spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }


    public static SpannableString getTitleForAudioNameWithArtists(String title, String subTitle, boolean isHighlight) {
        SpannableString spannableString;
        int songColor;
        int artistColor;
        if (isHighlight) {
            songColor = GlobalContext.get().getResources().getColor(R.color.color_selected);
            artistColor = GlobalContext.get().getResources().getColor(R.color.color_selected);
        } else {
            songColor = Color.WHITE;
            artistColor = Color.parseColor("#939393");
        }

        String name = String.format(Locale.getDefault(), "%s - %s", title, subTitle);
        spannableString = new SpannableString(name);
        spannableString.setSpan(new ForegroundColorSpan(songColor), 0, title.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(artistColor)
                , title.length() + 1, name.length()
                , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    public static SpannableString getTitleForAudioNameWithArtists(String title, String subTitle, int textStyle) {
        SpannableString spannableString;
        int songColor = 0;
        int artistColor = 0;
        if (textStyle == 1) {
            songColor = GlobalContext.get().getResources().getColor(R.color.color_selected);
            artistColor = GlobalContext.get().getResources().getColor(R.color.color_selected);
        } else if (textStyle == 0) {
            songColor = Color.WHITE;
            artistColor = Color.parseColor("#939393");
        } else if (textStyle == 2) {
            songColor = GlobalContext.get().getResources().getColor(R.color.play_list_item_name_grey);
            artistColor = GlobalContext.get().getResources().getColor(R.color.play_list_item_name_grey);
        }

        String name = String.format(Locale.getDefault(), "%s - %s", title, subTitle);
        spannableString = new SpannableString(name);
        spannableString.setSpan(new ForegroundColorSpan(songColor), 0, title.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(artistColor)
                , title.length() + 1, name.length()
                , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    /**
     * 判断服务是否后台运行
     *
     * @param mContext  Context
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRun(Context mContext, String className) {
        boolean isRun = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(40);
        int size = serviceList.size();
        for (int i = 0; i < size; i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRun = true;
                break;
            }
        }
        return isRun;
    }

    public static int getProcessIdByPkgName(String pkgName) {
        ActivityManager activityManager = (ActivityManager) GlobalContext.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<RunningAppProcessInfo> infos = activityManager
                    .getRunningAppProcesses();
            if (CollectionUtils.isNotEmpty(infos)) {
                for (RunningAppProcessInfo info : infos) {
                    if (info.processName.equals(pkgName)) {
                        return info.pid;
                    }
                }
                return -1;
            }
        }
        return android.os.Process.myPid();
    }


    /**
     * @param force 是否强制打开
     * @param type  0 表示音乐 1 表示电台,
     */
    public static void jumpTOMediaPlayerAct(final boolean force, final int type) {
        String openAppName = "";
        if (!force) {
            // 当前界面是导航界面，则不弹
            // 判断当前界面是否是导航界面
            if (getTopActivity() != null) {
                String naviPackage = getTopActivity().getPackageName();
                openAppName = SharedPreferencesUtils.getNotOpenAppPName();
                if (StringUtils.isEmpty(openAppName)) {
                    LogUtil.logd(TAG_JUMP + "you not setting open app package name,");
                    return;
                }
                if (openAppName.equals(naviPackage)) {
                    LogUtil.logd(TAG_JUMP + "don't need open app,you setting is " + openAppName);
                    return;
                } else {
                    if (TXZNavManager.getInstance().isInNav()) {
                        LogUtil.logd(TAG_JUMP + "don't need open app,navi package is" + openAppName);
                        return;
                    } else {
                        LogUtil.logd(TAG_JUMP + "can't open with inNav in the meantime," + openAppName);
                    }
                }
            }
        }
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                /**
                 * 如果已经是再电台或者音乐了，那么就没有必要打开了
                 * 如果没有Activity的存在，则需要打开界面
                 */
                boolean needJump = false;

                if (ActivityStack.getInstance().getsForegroundActivityCount() <= 0) {
                    needJump = true;
                } else if (HomeActivity.mTabIndex != type) {
                    needJump = true;
                }
                if (needJump) {
                    if (type == HomeActivity.MUSIC_i) {
                        //如果当前无网络，则跳转到本地音乐，如果本地没有音乐则跳转回音乐tab
                        //【【同听4.4.0】【打开音乐】离线情况下在本地界面暂停播放音乐，此时声控随意听听，界面跳转至音乐一栏】
                        //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001003670
                        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                            jumpToMusicTab();
                        } else if (DaoManager.getInstance().hasLocalAudio()) {
                            jumpToLocalTab();
                        } else {
                            //【【同听4.4.0】【打开电台】在播放页等非电台一级栏目界面，声控“打开电台”均跳转至电台一级栏目界面】
                            //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001003678
                            jumpToMusicTab();
                        }
                    } else if (type == HomeActivity.RADIO_i) {
                        jumpToRadioTab();
                    } else if (type == HomeActivity.LOCAL_i) {
                        jumpToLocalTab();
                    }
                }
            }
        }, 1000);
    }

    /**
     * 产品逻辑是否需要跳转
     */
    private static boolean isLogicIsNeedJump(int type) {

        //如果当前已经在指定的tab底下，则不用跳转

//
//        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
//        Activity activity = ActivityStack.getInstance().currentActivity();
//        if (activity != null && currentAudio != null) {
//            boolean isNeedJump = false;
//            if (Utils.isSong(currentAudio.getSid()) && type == HomeActivity.MUSIC_i) {
//                isNeedJump = true;
//            }
//
//            if (!Utils.isSong(currentAudio.getSid()) && type == HomeActivity.RADIO_i) {
//                isNeedJump = true;
//            }
//
//            if (isNeedJump && (activity instanceof PlayDetailsActivity || activity instanceof PlayRadioRecommendActivity)) {
//                Logger.d(TAG, "you need to jump to radio tab," + currentAudio + " , " + activity.getClass());
//                return false;
//            }
//        }
        return true;
    }

    private static void jumpToLocalTab() {
        jumpToTab(HomeActivity.LOCAL_i);
    }


    public static void jumpToRadioTab() {
        jumpToTab(HomeActivity.RADIO_i);
    }

    public static void jumpToMusicTab() {
        jumpToTab(HomeActivity.MUSIC_i);
    }

    public static void jumpToTab(int type) {

        if (!isLogicIsNeedJump(type)) {
            return;
        }
        Intent it = null;
        it = new Intent(GlobalContext.get(), SplashActivity.class);
        it.putExtra(KEY_TYPE, type);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            GlobalContext.get().startActivity(it);
        } catch (Exception e) {
            LogUtil.loge("open mainactivity error!");
        }
    }


    /**
     * 是否强制跳转到播放器界面
     *
     * @param force
     */
    public static void jumpTOMediaPlayerAct(final boolean force) {
        jumpTOMediaPlayerAct(force, HomeActivity.MUSIC_i);
    }

    public static ComponentName getTopActivity() {
        ActivityManager manager = (ActivityManager) GlobalContext.get()
                .getSystemService(android.content.Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null && runningTaskInfos.size() > 0) {
            int mRunningCount = runningTaskInfos.get(0).numRunning;
            return runningTaskInfos.get(0).topActivity;
        } else
            return null;
    }

    /**
     * 后台给出值，客户端获取相应位数的值
     *
     * @param src      原值
     * @param position 需要获取相应位置处的值 0开始
     * @return 相应位置处的值
     */
    public static int getDataWithPosition(int src, int position) {
        if (position < 0) {
            return 0;
        }
        int temp = (int) Math.pow(10, position);

        return src / temp % 10;
    }

    /**
     * 修改某一个位置的值
     *
     * @param src      原值
     * @param position 需要获取相应位置处的值 0开始
     * @return 相应位置处的值
     */
    public static int setDataWithPosition(int src, int content, int position) {
        if (position < 0) {
            return 0;
        }
        int temp = (int) Math.pow(10, position);

        int lag = src % (temp);//尾数
        int head = src / (temp * 10);//头部

        int result = head * (temp * 10) + content * temp + lag;
        return result;
    }

    private static RespCheck getConfig() {
//        if (sRefCheck == null || sRefCheck.get() == null) {
//            RespCheck check = JsonHelper.toObject(RespCheck.class, SharedPreferencesUtils.getConfig());
//            sRefCheck = new WeakReference<>(check);
//        }
        if (mRespCheck == null) {
            mRespCheck = JsonHelper.toObject(RespCheck.class, SharedPreferencesUtils.getConfig());
        }
        return mRespCheck;

//        return sRefCheck.get();
    }

    public static void forceRefreshConfig(RespCheck respCheck) {
        if (respCheck != null) {
            mRespCheck = respCheck;
        }
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {     //API 19
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight(); //earlier version
        }
    }


    public static File getAudioTMDFile(Audio audio) {
        if (Utils.isSong(audio.getSid())) {
            return new File(StorageUtil.getTmdDir(), audio.getId() + UNDERLINE + audio.getSid() + TMD_POSTFIX);
        }
        return null;
    }

    /**
     * 判断是否是小说
     *
     * @param sid
     * @return
     */
    public static boolean isNovel(int sid) {

        return false;
    }


    public static boolean isCarFm(Album album) {
        if (album == null) {
            return false;
        }

        return album.getAlbumType() == Album.ALBUM_TYPE_CAR_FM || (album.getPid() != 0 && album.getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM);
    }

    public static int getFavourFlag(Album album, Audio audio) {
        int support_fav = 0;
        int favour = 0;
        int support_sub = 0;
        int subscribe = 0;


        if (audio != null) {
            support_fav = Utils.getDataWithPosition(audio.getFlag(), Audio.POS_SUPPORT_FAVOUR);
            favour = Utils.getDataWithPosition(audio.getFlag(), Audio.POS_FAVOUR);
        }
        if (album != null) {
            support_sub = Utils.getDataWithPosition(album.getFlag(), Album.POS_SUPPORT_SUBSCRIBE);
            subscribe = Utils.getDataWithPosition(album.getFlag(), Album.POS_SUBSCRIBE);
        }
        return TongTingUtils.getFavourState(support_fav, favour, support_sub, subscribe);
    }


    public static void jumpToPlayerUI(Context ctx, int screen, Album album, long categoryId) {
        //todo跳转界面
        final long categoryID;
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
            categoryID = album.getArrCategoryIds().get(0);
        } else {
            categoryID = categoryId;
        }
        if (album.getAlbumType() == Album.ALBUM_TYPE_CAR_FM) {
            //判断是否父类
            Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity9.class);
            ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, album, categoryID, Constant.ALBUM_SHOW_URL, PlayInfoManager.TYPE_CAR_FM));
        } else if (album.getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
            if (album.getpSid() == 0 || album.getPid() == 0) {
                Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity9.class);
                ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, screen, album, categoryID, Constant.ALBUM_SHOW_URL, PlayInfoManager.TYPE_NORMAL_FM));
            } else {
                final Album parentAlbum = new Album();
                parentAlbum.setId(album.getPid());
                parentAlbum.setSid(album.getpSid());

                Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity9.class);
                ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, album, parentAlbum, categoryID, Constant.ALBUM_SHOW_URL, PlayInfoManager.TYPE_CAR_FM));
            }
        } else {
            Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity2.class);
            ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, screen, album, categoryID, null, PlayInfoManager.TYPE_NORMAL_ALBUM));
        }
    }
}
