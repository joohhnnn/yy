package com.txznet.music.localModule.logic;

import android.os.Environment;
import android.util.Log;

import com.txznet.audio.player.audio.TmdFile;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

/**
 * Created by brainBear on 2018/1/20.
 */

public class ScanUtil {

    private static final String TAG = "ScanUtil:";

    static FilenameFilter filenameFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            if (name.endsWith(".nomedia")) {
                return true;
            }
            if (name.endsWith(".tmd")) {
                return true;
            }
            if (name.endsWith(".mp3")) {
                return true;
            }
            if (name.endsWith(".m4a")) {
                return true;
            }
            if (name.endsWith(".aac")) {
                return true;
            }
            if (name.endsWith(".wav")) {
                return true;
            }
            if (name.endsWith(".flac")) {
                return true;
            }
            if (!name.contains(".")) {
                return true;
            }

            return false;
        }
    };


    private ScanUtil() {

    }

    private static File txz = new File(Environment.getExternalStorageDirectory(), "txz");

    public static void scanRecursively(String path, Set<Audio> audios) {
        File file = new File(path);
        //listFile ，在全盘扫描的时候，慎用！！会导致内存增长，比方说万个文件，使用string进行 拼接 方式好
        //  使用正则表达式进行文本比较不划算，在《比较》不复杂的时候 慎用
        String[] list = file.list(filenameFilter);

        if (list == null) {
            return;
        }
        for (String s : list) {
            if (s.endsWith(".nomedia")) {
                //去掉nomedia的
                return;
            }
        }

        for (String s : list) {
            if (!s.contains(".")) {
                //目录
                scanRecursively(path + File.separator + s, audios);
            } else {
                Audio tmp  = convertFileToAudio(new File(path + File.separator + s));
                if (tmp != null) {
                    audios.add(tmp);
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

    public static long jsonCountSize = 0;

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
                    String s = new String(openFile.loadInfo());
                    jsonCountSize += s.length();
                    audio = JsonHelper.toObject(Audio.class, s);
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
            audio.setName(file.getName().substring(0, file.getName().lastIndexOf(".")));
            audio.setDownloadType("0");
            audio.setSourceFrom("未知");
        }
        if (audio == null) {
            return null;
        }
        //离线识别,这里需要传递存在的路径,否则会被剔除
        audio.setStrDownloadUrl(file.getAbsolutePath());
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
