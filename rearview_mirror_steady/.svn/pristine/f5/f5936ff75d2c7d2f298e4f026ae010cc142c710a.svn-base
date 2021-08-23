package com.txznet.sdk.media.base;

import com.txznet.sdk.media.TXZMediaModel;

import java.util.List;

/**
 * 媒体搜索工具
 * Created by J on 2018/8/6.
 */

public interface ITXZMediaSearchTool {
    /**
     * 是否开启搜索结果列表显示
     *
     * @return 显示返回true
     */
    boolean showSearchResult();

    /**
     * 获取搜索结果列表
     *
     * @param searchModel 用于搜索的媒体信息
     * @return 搜索结果列表
     */
    void search(TXZMediaModel searchModel, SearchCallback callback);

    /**
     * 获取搜索超时时间, 声控发起搜索时, 会在此超时时间后认为搜索失败, 不再响应此次搜索的结果
     * 返回值为int类型, 因为不会出现超时时间很长的情况
     *
     * @return 超时时间, 单位为ms
     */
    int getSearchTimeoout();

    /**
     * 播放节目
     *
     * @param index 结果列表中的编号(从0开始)
     * @param model 对应编号的搜索结果
     */
    void playSearchResult(int index, TXZMediaModel model);

    interface SearchCallback {
        void onSuccess(List<TXZMediaModel> result);
        void onError(String cause);
    }
}
