package com.txznet.txz.component.media.chooser;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.txz.component.media.MediaPrioritySp;
import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.loader.MediaToolManager;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.jni.JNIHelper;

import java.util.List;

/**
 * 音乐工具选择器
 * Created by J on 2018/5/7.
 */

public class MusicPriorityChooser extends AbsMediaPriorityChooser<AbsMusicTool> {
    @NonNull
    @Override
    protected List<AbsMusicTool> getMediaToolList() {
        return MediaToolManager.getInstance().getMusicToolList();
    }

    @Override
    protected void savePriorityPackageName(final String packageName) {
        MediaPrioritySp.getInstance().updatePriorityMusic(packageName);
    }

    @Override
    protected String restorePriorityPackageName() {
        return MediaPrioritySp.getInstance().getPriorityMusic();
    }

    /**
     * 设置默认音乐工具, 供sdk调用
     *
     * @param toolName 媒体工具的名称(不是包名)
     * @see MediaToolConstants#TYPE_MUSIC_TONGTING
     * @see MediaToolConstants#TYPE_MUSIC_KUWO
     * @see MediaToolConstants#TYPE_MUSIC_REMOTE
     */
    public void setDefaultTool(String toolName) {
        JNIHelper.logd("setting default tool to: " + toolName);

        setDefaultToolPackageName(getPackageByName(toolName));
    }

    /**
     * 设置指定工具的搜索配置
     * @param toolName 工具名
     * @param showResult 是否声控界面显示搜索结果
     * @param timeout 搜索超时时间
     */
    public void setSearchConfig(String toolName, boolean showResult, long timeout) {
        // 远程工具需要特殊处理下
        if (MediaToolConstants.TYPE_MUSIC_REMOTE.equals(toolName)) {
            RemoteMusicTool.getInstance().setShowSearchResult(showResult);
            RemoteMusicTool.getInstance().setSearchTimeout(timeout);
        } else {
            String packageName = getPackageByName(toolName);
            if (!TextUtils.isEmpty(packageName)) {
                MediaToolManager.getInstance().setSearchConfig(packageName, showResult, timeout);
            }
        }
    }

    private String getPackageByName(String toolName) {
        String packageName;
        if (TextUtils.isEmpty(toolName)) {
            packageName = null;
        } else if (MediaToolConstants.TYPE_MUSIC_TONGTING.equals(toolName)) {
            packageName = MediaToolConstants.PACKAGE_TONGTING;
        } else if (MediaToolConstants.TYPE_MUSIC_KUWO.equals(toolName)) {
            packageName = MediaToolConstants.PACKAGE_MUSIC_KUWO;
        } else if (MediaToolConstants.TYPE_MUSIC_REMOTE.equals(toolName)) {
            packageName = RemoteMusicTool.getInstance().getPackageName();
        } else {
            JNIHelper.loge("cannot find tool for name: " + toolName);
            packageName = null;
        }

        return packageName;
    }


    //----------- single instance -----------
    private static volatile MusicPriorityChooser sInstance;

    public static MusicPriorityChooser getInstance() {
        if (null == sInstance) {
            synchronized (MusicPriorityChooser.class) {
                if (null == sInstance) {
                    sInstance = new MusicPriorityChooser();
                }
            }
        }

        return sInstance;
    }

    private MusicPriorityChooser() {
    }
    //----------- single instance -----------
}
