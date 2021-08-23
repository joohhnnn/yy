package com.txznet.music.playerModule.logic.factory;

import com.txznet.music.playerModule.logic.IPlayerEngine;
import com.txznet.music.playerModule.logic.PlayerEngineCoreDecorator;
import com.txznet.music.playerModule.logic.PlayerEngineObserverDecorator;
import com.txznet.music.playerModule.logic.PlayerEngineReportDecorator;
import com.txznet.music.playerModule.logic.PlayerEngingLogDecorator;

public class PlayEngineFactory {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_CORE = 1;
    private static IPlayerEngine engine = null;
    private static int sCurrentType = 0;


    public synchronized static IPlayerEngine createEngine() {
        if (engine == null) {
            //增加一个打印日志
            engine = new PlayerEngingLogDecorator(new PlayerEngineObserverDecorator(new PlayerEngineReportDecorator(
                    new PlayerEngineCoreDecorator())));
        }
        //proxy模式
        return engine;
    }


    public synchronized static IPlayerEngine getEngine() {//双重锁
        if (engine == null) {
            return createEngine();
        }
        return engine;
    }


}
