package com.txznet.music.playerModule.logic.scene;

import com.txznet.music.albumModule.bean.Audio;

import java.util.Observable;

public interface IScene {


    //请求更多的数据
//    public void requestMoreData();

    //FavorHelper.getFavourData(0, 0, 0L, Constant.PAGECOUNT
    public Observable requestData(int sid, long id, long reqTime);


    /**
     * 是否在更新播放列表数据的时候，刷新到指定位置
     */
    public boolean needMovePos();

}
