package com.txznet.music.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.ui.SplashActivity;
import com.txznet.music.ui.bean.FlashPage;
import com.txznet.music.ui.bean.PlayConf;
import com.txznet.music.ui.net.response.RespCheck;
import com.txznet.sdk.TXZNavManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * @author telenewbie
 * @version 创建时间：2016年2月29日 上午10:50:21
 */
public class Utils {

    public static final String UNDERLINE = "_";
    public static final String TMD_POSTFIX = ".tmd";
    private static final String TAG_JUMP = "music:jump:open:";
    private static String TAG = "music:util:";
    private static WeakReference<RespCheck> sRefCheck;

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
        List<RunningAppProcessInfo> infos = activityManager
                .getRunningAppProcesses();
        for (RunningAppProcessInfo info : infos) {
            if (info.processName.equals(pkgName)) {
                return info.pid;
            }
        }
        return -1;
    }

    /**
     * 是否强制跳转到播放器界面
     *
     * @param force
     */
    public static void jumpTOMediaPlayerAct(final boolean force) {/*
        if (true) {
            return;
        }
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
                Intent it = null;
                it = new Intent(GlobalContext.get(), SplashActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    GlobalContext.get().startActivity(it);
                } catch (Exception e) {
                    LogUtil.loge("open mainactivity error!");
                }
            }
        }, 1000);*/
    }

    public static ComponentName getTopActivity() {
        ActivityManager manager = (ActivityManager) GlobalContext.get()
                .getSystemService(android.content.Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

        if (runningTaskInfos != null) {
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
        if (sRefCheck == null || sRefCheck.get() == null) {
            RespCheck check = JsonHelper.toObject(RespCheck.class, SharedPreferencesUtils.getConfig());
            sRefCheck = new WeakReference<>(check);
        }

        return sRefCheck.get();
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
}
