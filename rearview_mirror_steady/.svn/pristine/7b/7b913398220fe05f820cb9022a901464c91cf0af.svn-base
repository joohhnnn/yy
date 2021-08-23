package com.txznet.music.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayerConfigHelper;
import com.txznet.music.ui.SplashActivity;
import com.txznet.music.ui.base.DialogFragmentStack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * @author telenewbie
 * @version 创建时间：2016年2月29日 上午10:50:21
 */
public class Utils {

    public static final String UNDERLINE = "_";
    public static final String TMD_POSTFIX = ".tmd";
    public static final String KEY_TYPE = "KEY_TYPE";


    public static SpannableString getTitleAndArtists(String title,
                                                     String artists) {
        StringBuilder sBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(artists)) {
            sBuilder.append(artists);
        }
        if (sBuilder.length() > 0) {
            sBuilder.insert(0, "-");
        }

        SpannableString spannableString = new SpannableString(title
                + sBuilder.toString());
        spannableString.setSpan(new AbsoluteSizeSpan(30), 0, title.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ColorStateList color = ColorStateList.valueOf(Color.parseColor("#707070"));
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
            songColor = Color.parseColor("#E82526");
            artistColor = Color.parseColor("#E82526");
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
            songColor = Color.parseColor("#E82526");
            artistColor = Color.parseColor("#E82526");
        } else if (textStyle == 0) {
            songColor = Color.WHITE;
            artistColor = Color.parseColor("#939393");
        } else if (textStyle == 2) {
            songColor = Color.parseColor("#7FFFFFFF");
            artistColor = Color.parseColor("#7FFFFFFF");
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
            if (serviceList.get(i).service.getClassName().equals(className)) {
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

        return head * (temp * 10) + content * temp + lag;
    }

    /**
     * 去重
     *
     * @param audios
     */
    public static <T extends AudioV5> void deleteSameAudiosFromSource(List<T> audios) {
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(audios)) {
            Set<T> audioSet = new LinkedHashSet<>(audios);
            audios.clear();
            audios.addAll(audioSet);
        }
    }


    /**
     * 返回到推荐页
     */
    public static void back2Home() {
        if (!DialogFragmentStack.get().isEmpty()) {
            DialogFragmentStack.get().exit();
        }
        Intent intent = new Intent(GlobalContext.get(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("type", 0);
        GlobalContext.get().startActivity(intent);
    }

    /**
     * 返回到推荐页
     */
    public static void back2HomeWithMusic() {
        if (!DialogFragmentStack.get().isEmpty()) {
            DialogFragmentStack.get().exit();
        }
        Intent intent = new Intent(GlobalContext.get(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("type", 1);
        GlobalContext.get().startActivity(intent);
    }

    /**
     * 返回到推荐页
     */
    public static void back2HomeWithRadio() {
        if (!DialogFragmentStack.get().isEmpty()) {
            DialogFragmentStack.get().exit();
        }
        Intent intent = new Intent(GlobalContext.get(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("type", 2);
        GlobalContext.get().startActivity(intent);
    }


    public static void sleep() {
        AudioPlayer.getDefault().destroy();
        PlayHelper.get().clearNotNotify();
        DialogFragmentStack.get().exit();
        ActivityStack.getInstance().exit();
        TtsHelper.cancel();
    }

    public static void wakeup() {
        PlayerConfigHelper.get().initPlayer();
    }

    public static void exitApp(boolean shouldKill) {
        exitApp(null, shouldKill);
    }

    public static void exitApp(Activity activity) {
        exitApp(activity, true);
    }

    public static void exitApp(Activity activity, boolean shouldKill) {
        DisposableManager.get().clear();

        AudioPlayer.getDefault().destroy();
        PlayerConfigHelper.get().initPlayer();
        PlayHelper.get().clearNotNotify();
        DialogFragmentStack.get().exit();
        ActivityStack.getInstance().exit();
        TtsHelper.cancel();
        if (activity != null) {
            activity.finish();
        }
        if (ProgramUtils.isProgram()) {

        } else {
            if (shouldKill) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    public static void onBackRun(Activity activity) {
        if (activity != null) {
            activity.moveTaskToBack(true);
        }
    }

    public static void hideUi() {
        if (ActivityStack.getInstance().has()) {
            ActivityStack.getInstance().currentActivity().moveTaskToBack(true);
        }
    }
}
