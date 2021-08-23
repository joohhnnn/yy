package com.txznet.txz.component.media.chooser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.base.IRemoteMediaTool;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

import java.util.List;

/**
 * 媒体工具选择器
 *
 * 负责维护高优先媒体工具相关逻辑(更新/持久化/恢复)
 * 提供根据传入的条件 {@link MediaToolChecker} 选择合适媒体工具的接口
 * 子类需要重写对应方法传入支持的媒体工具列表, 且自己处理高优先工具持久化和恢复的逻辑
 *
 * @see AbsMediaPriorityChooser#getMediaToolList()
 * @see AbsMediaPriorityChooser#savePriorityPackageName(String)
 * @see AbsMediaPriorityChooser#restorePriorityPackageName()
 *
 * Created by J on 2018/5/7.
 */

public abstract class AbsMediaPriorityChooser<T extends IMediaTool> {
    private final String LOG_TAG = getClass().getSimpleName() + "::";

    /**
     * 默认音乐工具
     */
    private String mDefaultToolPackageName;
    /**
     * 高优先级音乐工具
     */
    private T mHighPriorityTool;

    /**
     * 获取支持的媒体工具列表
     *
     * @return 所有支持的媒体工具列表, 以优先级从高到低排序
     */
    @NonNull
    protected abstract List<T> getMediaToolList();

    /**
     * 持久化高优先工具
     *
     * @param packageName 新的高优先工具包名
     */
    protected abstract void savePriorityPackageName(String packageName);

    /**
     * 恢复高优先工具
     *
     * @return 恢复的高优先工具包名
     */
    protected abstract String restorePriorityPackageName();

    /**
     * 设置默认媒体工具(默认媒体工具具有最高优先级)
     *
     * @param tool 默认工具包名
     */
    public void setDefaultToolPackageName(String tool) {
        mDefaultToolPackageName = tool;
    }

    /**
     * 是否需要拦截媒体工具选择
     *
     * MediaChooser选择拦截选择逻辑时, 会处于整个工具选择流程的最高优先级, 屏蔽所有其他的工具选择流程
     *
     * @return 选定的媒体工具, 不拦截返回null
     */
    public T interceptMediaToolChoose() {
        // 需要检查默认工具是否存在, 避免默认工具不存在导致处理逻辑丢失的问题
        T defaultTool = getToolByPackageName(mDefaultToolPackageName);
        if (null != defaultTool && checkToolExist(defaultTool)) {
            return defaultTool;
        }

        return null;
    }

    /**
     * 根据传入的选择器, 按照优先级选择最符合的MediaTool
     *
     * @param checker 选择器
     * @return 符合指定checker条件的媒体工具
     */
    @Nullable
    public T getMediaTool(final MediaToolChecker<IMediaTool> checker) {
        // 优先考虑高优先工具
        if (mHighPriorityTool != null) {
            if (null == checker || checker.check(mHighPriorityTool)) {
                return mHighPriorityTool;
            }
        }

        T defaultTool = getToolByPackageName(mDefaultToolPackageName);
        // 如果有指定默认音乐工具, 优先级最高
        if (null != defaultTool && defaultTool != mHighPriorityTool) {
            if (checkMediaTool(checker, defaultTool)) {
                return defaultTool;
            }
        }

        // 按优先级逐级查找合适的MusicTool
        for (T tool : getMediaToolList()) {
            // 默认工具已经检查过, 不需要重新检查
            if (null != defaultTool && tool == defaultTool) {
                continue;
            }

            // 高优先工具已经检查过, 不需要重新检查
            if (tool == mHighPriorityTool) {
                continue;
            }

            if (checkMediaTool(checker, tool)) {
                return tool;
            }
        }

        return null;
    }

    private T getToolByPackageName(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        for (T tool : getMediaToolList()) {
            if (packageName.equals(tool.getPackageName())) {
                return tool;
            }
        }

        return null;
    }

    /**
     * 更新高优先级工具
     *
     * @param tool 新的高优先工具
     */
    public void updatePriorityTool(T tool) {
        log("updating priority to: " + ((null == tool) ? "null" : tool.getPackageName()));

        if (mHighPriorityTool == tool) {
            return;
        }

        if (tool != null && !getMediaToolList().contains(tool)) {
            log("ignoring update priority to illegal tool: " + tool.getPackageName());
            return;
        }

        mHighPriorityTool = tool;
        savePriorityPackageName((null == mHighPriorityTool) ?
                null : mHighPriorityTool.getPackageName());
    }

    /**
     * 通知媒体工具被删除
     *
     * @param packageName 被删除的包名
     */
    public void onMediaToolUninstalled(String packageName) {
        if (null != mHighPriorityTool &&
                packageName.equals(mHighPriorityTool.getPackageName())) {
            updatePriorityTool(null);
        }
    }

    /**
     * 获取高优先工具
     *
     * @return 当前的高优先工具
     */
    @Nullable
    public IMediaTool getHighPriorityTool() {
        return mHighPriorityTool;
    }

    /**
     * 从持久化缓存中恢复工具优先级, 用于Core启动时的初始化
     */
    public void restorePriority() {
        final String priorPackageName = restorePriorityPackageName();
        T playingTool = null;
        for (T tool : getMediaToolList()) {
            // 如果工具正处于播放状态, 直接设置为高优先工具
            if (isMediaToolPlaying(tool)) {
                playingTool = tool;
                break;
            }

            if (tool.getPackageName() != null && tool.getPackageName().equals(priorPackageName)) {
                // 检查下工具是否确实存在
                // 缓存的高优先工具如果是远程工具, 此时应该是不存在的(适配还未调用setTool), 不过稍后适配初始
                // 化后会调用setTool重新设置远程工具, 能够保证远程工具优先级恢复正常
                if (checkToolExist(tool)) {
                    mHighPriorityTool = tool;
                } else {
                    log(String.format("cannot restore priority to %s, tool is not exist!",
                            tool.getPackageName()));
                }
            }
        }

        if (playingTool != null) {
            MediaPriorityManager.getInstance().notifyPriorityChange(playingTool);
        }

        log("restoring priority to: " + (null == mHighPriorityTool ?
                "null" : mHighPriorityTool.getPackageName()));
    }

    /**
     * 检查指定应用是否存在
     *
     * 对于远程工具, 返回是否处于启用状态(是否有适配程序注册了远程音乐/电台工具)
     * 对于Core内部适配的工具, 返回是否安装了对应的音乐/电台应用
     *
     * @param tool 需要检查的Tool
     * @return 工具是否存在
     */
    private boolean checkToolExist(IMediaTool tool) {
        if (tool instanceof IRemoteMediaTool) {
            return ((IRemoteMediaTool) tool).isEnabled();
        }

        return PackageManager.getInstance().checkAppExist(tool.getPackageName());
    }

    /**
     * 检查对应的tool是否符合要求
     *
     * checker为null时认为是符合要求的, 会返回true
     *
     * @param checker 对应的checker
     * @param tool    需要检查的tool
     * @return 符合要求返回true
     */
    private boolean checkMediaTool(MediaToolChecker<IMediaTool> checker, IMediaTool tool) {
        return (checkToolExist(tool) && (null == checker || checker.check(tool)));
    }

    private boolean isMediaToolPlaying(IMediaTool tool) {
        IMediaTool.PLAYER_STATUS status = tool.getStatus();

        return (status == IMediaTool.PLAYER_STATUS.BUFFERING
                || status == IMediaTool.PLAYER_STATUS.PLAYING);
    }

    // logger
    private void log(String cmd) {
        JNIHelper.logd(LOG_TAG + cmd);
    }

    /**
     * MediaTool checker
     *
     * 用于检查媒体工具是否符合需要
     */
    public abstract static class MediaToolChecker<T> {
        /**
         * 指定tool是否符合要求
         *
         * @param tool 待检查的tool
         * @return 符合要求返回true
         */
        public abstract boolean check(T tool);
    }
}
