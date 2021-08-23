package com.txznet.music.playerModule.logic.scene;

public class SceneFactory {


    //##创建一个单例类##
    private volatile static SceneFactory singleton;

    private SceneFactory() {
    }

    public static SceneFactory getInstance() {
        if (singleton == null) {
            synchronized (SceneFactory.class) {
                if (singleton == null) {
                    singleton = new SceneFactory();
                }
            }
        }
        return singleton;
    }

    public IScene getScene() {
        //根据不同的场景，做出不同的区分

        return null;
    }

}
