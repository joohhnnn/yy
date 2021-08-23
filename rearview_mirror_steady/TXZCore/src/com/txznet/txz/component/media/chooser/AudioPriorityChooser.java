package com.txznet.txz.component.media.chooser;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.txz.component.media.MediaPrioritySp;
import com.txznet.txz.component.media.base.AbsAudioTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.loader.MediaToolManager;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.jni.JNIHelper;

import java.util.List;

/**
 * 电台工具选择器
 * Created by J on 2018/5/7.
 */

public class AudioPriorityChooser extends AbsMediaPriorityChooser<AbsAudioTool> {
    @NonNull
    @Override
    protected List<AbsAudioTool> getMediaToolList() {
        return MediaToolManager.getInstance().getAudioToolList();
    }

    @Override
    protected void savePriorityPackageName(final String packageName) {
        MediaPrioritySp.getInstance().updatePriorityAudio(packageName);
    }

    @Override
    protected String restorePriorityPackageName() {
        return MediaPrioritySp.getInstance().getPriorityAudio();
    }

    /**
     * 设置默认音乐工具, 供sdk调用
     *
     * @param toolName 媒体工具的名称(不是包名)
     * @see MediaToolConstants#TYPE_AUDIO_TONGTING
     * @see MediaToolConstants#TYPE_AUDIO_KL
     * @see MediaToolConstants#TYPE_AUDIO_XMLY
     * @see MediaToolConstants#TYPE_AUDIO_REMOTE
     */
    public void setDefaultTool(String toolName) {
        JNIHelper.logd("setting default audio tool to: " + toolName);
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
        if (MediaToolConstants.TYPE_AUDIO_REMOTE.equals(toolName)) {
            RemoteAudioTool.getInstance().setShowSearchResult(showResult);
            RemoteAudioTool.getInstance().setSearchTimeout(timeout);
        } else {
            String packageName = getPackageByName(toolName);
            if (!TextUtils.isEmpty(packageName)) {
                MediaToolManager.getInstance().setSearchConfig(packageName, showResult, timeout);
            }
        }
    }

    public String getPackageByName(String toolName) {
        String packageName;
        if (TextUtils.isEmpty(toolName)) {
            packageName = null;
        } else if (MediaToolConstants.TYPE_AUDIO_TONGTING.equals(toolName)) {
            packageName = MediaToolConstants.PACKAGE_TONGTING;
        } else if (MediaToolConstants.TYPE_AUDIO_XMLY.equals(toolName)) {
            packageName = MediaToolConstants.PACKAGE_AUDIO_XMLY;
        } else if (MediaToolConstants.TYPE_AUDIO_KL.equals(toolName)) {
            packageName = MediaToolConstants.PACKAGE_AUDIO_KAOLA;
        } else if (MediaToolConstants.TYPE_AUDIO_REMOTE.equals(toolName)) {
            packageName = RemoteAudioTool.getInstance().getPackageName();
        } else {
            JNIHelper.loge("cannot find audio tool for name: " + toolName);
            packageName = null;
        }

        return packageName;
    }

    //----------- single instance -----------
    private static volatile AudioPriorityChooser sInstance;

    public static AudioPriorityChooser getInstance() {
        if (null == sInstance) {
            synchronized (AudioPriorityChooser.class) {
                if (null == sInstance) {
                    sInstance = new AudioPriorityChooser();
                }
            }
        }

        return sInstance;
    }

    private AudioPriorityChooser() {
    }
    //----------- single instance -----------
}
