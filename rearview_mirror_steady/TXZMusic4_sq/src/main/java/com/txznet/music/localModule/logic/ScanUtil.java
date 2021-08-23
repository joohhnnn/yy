package com.txznet.music.localModule.logic;

import android.text.TextUtils;

import com.txznet.audio.player.audio.TmdFile;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by brainBear on 2018/1/20.
 */

public class ScanUtil {

    private static final String TAG = "ScanUtil:";

    private static FileFilter sFilter = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            } else {
                return Pattern.matches(".*\\.(tmd|aac|m4a|mp3|nomedia)", pathname.getName());
            }
        }
    };

    private ScanUtil() {

    }


    public static void scanRecursively(String path, Set<Audio> audios) {
        File file = new File(path);
        File[] files = file.listFiles(sFilter);
        if (null == files || files.length <= 0 || TextUtils.equals(files[0].getName(), ".nomedia")) {
            return;
        }

        for (File f : files) {
            if (f.exists()) {
                if (f.isDirectory()) {
                    scanRecursively(f.getAbsolutePath(), audios);
                } else {
                    if (checkLocalFile(f)) {
                        Audio audio = convertFileToAudio(f);
                        if (null != audio) {
                            if (!audios.add(audio)) {
                                Logger.i(TAG, "more one same audios :" + audio.getStrDownloadUrl());
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean checkLocalFile(File file) {
        if (file != null && file.exists()) {
            if (file.length() > SharedPreferencesUtils.getSearchSize()) {
                return true;
            }
        }
        return false;
    }


    private static Audio convertFileToAudio(File file) {
        Audio audio = null;
        TmdFile openFile = null;
        // 如果是tmd文件的情况下
        if (file.getAbsolutePath().endsWith(".tmd")) {
            try {
                openFile = TmdFile.openFile(file, -1, false);
                if (openFile == null) {
                    audio = PlayEngineFactory.getEngine().getCurrentAudio();
                } else {
                    audio = JsonHelper.toObject(Audio.class, new String(openFile.loadInfo()));
                }
            } catch (Exception e) {
                LogUtil.loge(TAG + "path=" + file.getAbsolutePath(), e);
            } finally {
                if (openFile != null) {
                    openFile.closeQuitely();
                }
            }
        } else {
            audio = new Audio();
            audio.setSid(0);
            // audio.setDuration(file);
            audio.setStrDownloadUrl(file.getAbsolutePath());
            audio.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
            audio.setDownloadType("0");
            audio.setSourceFrom("本地音乐");
        }
        if (audio == null) {
            return null;
        }

        audio.setLocal(true);
        if (audio.getId() == 0) {
            audio.setId(Math.abs(audio.getName().hashCode()));// 使用标题名称作为id
        }
        //历史原因:版本3.0,属于和考拉共同开发的,数据来源属于考拉.考拉对id进行修改了(暂且认为全部id都在前面加了100000).故认为是本地音乐,否则对收藏有影响
        if (String.valueOf(audio.getId()).startsWith("100000")) {
            audio.setSid(0);
        }
//                audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
        // 小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode
        // 正表达式的前缀。中括号内的“P”表示Unicode 字符集七个字符属性之一：标点字符。
        String desc = audio.getName().replaceAll("[\\p{P}]", "");
        audio.setDesc(desc);

        audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_SUPPORT, Audio.POS_SUPPORT_FAVOUR));//可以支持收藏
        if (FavorHelper.isFavour(audio)) {
            audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_FAVOUR, Audio.POS_FAVOUR));//是否已经收藏
        } else {
            audio.setFlag(Utils.setDataWithPosition(audio.getFlag(), Audio.FLAG_UNFAVOUR, Audio.POS_FAVOUR));//是否已经收藏
        }

        return audio;
    }
}
