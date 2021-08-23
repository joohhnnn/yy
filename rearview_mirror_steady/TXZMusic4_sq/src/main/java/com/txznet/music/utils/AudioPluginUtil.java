package com.txznet.music.utils;

import com.txznet.audio.player.PluginBean;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.plugin.PluginLoader;

/**
 * Created by ASUS User on 2016/11/30.
 */

public class AudioPluginUtil {

    @SuppressWarnings("unchecked")
    /**
     * 从插件获取相应的值
     */
    public static <T> T getObjFromPlugin(String path, String className, PluginBean bean, Class<T> clazz) {
        T audioPlayer = null;
        try {
            Object obj = PluginLoader.loadPlugin(path, className, bean != null ? JsonHelper.toJson(bean).getBytes() : null);
            if (obj != null && clazz != null) {
                LogUtil.logd("装载新插件成功" + obj.toString());
                audioPlayer = clazz.isInstance(obj) ? ((T) obj) : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return audioPlayer;
    }
}
