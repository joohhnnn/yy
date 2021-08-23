package com.txznet.music.playerModule.logic.scene;

import java.util.Observable;

public class FavourScene implements IScene {
    @Override
    public Observable requestData(int sid, long id, long reqTime) {
        return null;
    }

    @Override
    public boolean needMovePos() {
        return false;
    }
}
