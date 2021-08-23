package com.txznet.music.baseModule.plugin;

import com.txznet.txz.plugin.IExecPlugin;
import com.txznet.txz.plugin.PluginManager;

/**
 * Created by ASUS User on 2016/11/30.
 */

public class TestPlayerPluginEngine implements IExecPlugin {

    public TestPlayerPluginEngine() {
        PluginManager.addCommandProcessor(CommandString.PLUGIN_ENGINE, new PluginManager.CommandProcessor() {
            @Override
            public Object invoke(String command, Object[] args) {
                //回调到相应的测试类（在混淆代码中)
//播放，暂停
//                if(CommandString.OPE_PLAY.equals(command)){
//                    PluginManager.invoke(CommandString.ENGINE_PLAYER+command,args);
//                } if(CommandString.OPE_PAUSE.equals(command)){
//                    PluginManager.invoke(CommandString.ENGINE_PLAYER+command,args);
//                } if(CommandString.OPE_RELEASE.equals(command)){
//                    PluginManager.invoke(CommandString.ENGINE_PLAYER+command,args);
//                } if(CommandString.OPE_SEEK.equals(command)){
//                    PluginManager.invoke(CommandString.ENGINE_PLAYER+command,args);
//                }
                return null;
            }
        });
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public Object execute(ClassLoader loader, String path, byte[] data) {
        return null;
    }
}
