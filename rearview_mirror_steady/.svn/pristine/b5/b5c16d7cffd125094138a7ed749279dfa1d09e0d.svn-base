package com.txznet.music.soundControlModule.logic;

/**
 * Created by telenewbie on 2016/12/23.
 */

public interface ISoundSearch extends ISound {

       /**
     * 搜索内容
     * @param json 由云之声等经过转义，特殊情况下，可能不转义
     */
    public  void  searchContent(String json);


    /**
     * 搜索结果回来
     * @param result 搜索结果
     */
    public void searchResult(byte[] result);


    /**
     * 声控选中第几个搜索结果
     * @param position 位置
     */
    public  void  searchChoiceIndex(int position);
    /**
     * 声控选中第几个搜索结果
     * @param position 位置
     */
    public  void  searchPreloadIndex(int position);
}
