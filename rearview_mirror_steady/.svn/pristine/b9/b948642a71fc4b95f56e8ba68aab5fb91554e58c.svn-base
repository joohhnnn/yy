package com.txznet.music.playerModule.logic.factory;

import com.txznet.music.playerModule.logic.IPlayerEngine;
import com.txznet.music.playerModule.logic.PlayEngineCoreDecorator;
import com.txznet.music.playerModule.logic.PlayerEngineObserverDecorator;
import com.txznet.music.playerModule.logic.PlayerEngineReportDecorator;

public class PlayEngineFactory {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_CORE = 1;
    private static IPlayerEngine engine = null;
    private static int sCurrentType = 0;


    public synchronized static IPlayerEngine createEngine() {
        if (engine == null) {
            engine = new PlayerEngineObserverDecorator(new PlayerEngineReportDecorator(
                    new PlayEngineCoreDecorator()));
        }
        return engine;
    }


    public synchronized static IPlayerEngine getEngine() {//双重锁
        if (engine == null) {
            return createEngine();
        }
        return engine;
    }


}
