package com.txznet.music.baseModule.plugin;

import android.os.Environment;

import com.txznet.audio.player.PluginBean;
import com.txznet.fm.bean.Configuration;
import com.txznet.music.utils.AudioPluginUtil;
import com.txznet.music.utils.JsonHelper;
import com.txznet.txz.plugin.PluginManager;

import java.io.File;

/**
 * Created by ASUS User on 2016/11/30.
 */

public class PluginMusicManager {

    private PluginMusicManager() {
        //插件的形式，通过互为命令字来互相调用
//        if (scanLocalPlugin()) {
//            LogUtil.logd(TAG+"装载插件成功");
        scanLocalPlugin();
        PluginManager.addCommandProcessor(CommandString.CLIENT_ENGINE, new PluginManager.CommandProcessor() {
            @Override
            public Object invoke(String command, Object[] args) {
                if ("toJson".equals(command)) {//初始化插件，插件需要预处理一些逻辑
                    return JsonHelper.toJson(args[0]).getBytes();
                }

                return null;
            }
        });
//        PluginPlayer.getInstance();/*播放器插件*/
//        }else{
//            LogUtil.logd(TAG+"没有装载插件");
//        }
    }

    /**
     * 扫描本地插件，有则加载
     */
    public boolean scanLocalPlugin() {
        if (new File(Environment.getExternalStorageDirectory() + Configuration.getInstance().getString(Configuration.TXZ_PLUGIN_PATH)).exists()) {
            //加载插件
            PluginBean mBean = new PluginBean();
            mBean.setCmd(CommandString.LOADPLUGIN);
            AudioPluginUtil.getObjFromPlugin(Environment.getExternalStorageDirectory() + Configuration.getInstance().getString(Configuration.TXZ_PLUGIN_PATH), Configuration.getInstance().getString(Configuration.TXZ_PLUGIN_CLASS_NAME), mBean, null);
            return true;
        } else {
            //不处理
            return false;

        }
    }

    //##创建一个单例类##
    private volatile static PluginMusicManager singleton;

    public static PluginMusicManager getInstance() {
        if (singleton == null) {
            synchronized (PluginMusicManager.class) {
                if (singleton == null) {
                    singleton = new PluginMusicManager();
                }
            }
        }
        return singleton;
    }
}
