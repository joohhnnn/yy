package com.txznet.fm.manager;


import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.net.NetCacheManager;
import com.txznet.music.net.RequestCallBack;

/**
 * Created by telen on 2018/5/16.
 * 调用方不需要关心切换线程
 * 这边的逻辑，全部都以SDK的形式进行开发，
 * 逻辑这边全部写好，提供成功和错误的回调给到监听器
 * <p>
 * eg：请求专辑，
 * <p>
 * requsetAlbum(Callback callback){
 * //切换线程
 * 执行请求
 * onSuccess{
 * //存放数据库
 * //执行数据缓存
 * //成功回调
 * callback.onSuccess()
 * }
 * onError{
 * String errorMessage=getErrorMessage(errorCode);
 * callback.onError(errorMessage);
 * }
 * <p>
 * }
 */

public class AlbumManager extends AManager implements IManager {

    /**
     * 获取当前分类底下的专辑
     * public void getChannelAsync(int channelId, int pageNo, int pageSize, boolean cache, RadioListener listener)
     */
    public void getAlbum(long categoryID, int pageNo, int pageSize, boolean cache, final CallbackManager listener) {
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(pageNo);
        reqSearchAlbum.setPageCount(pageSize);
        reqSearchAlbum.setCategoryId(categoryID);

        //支持切换网络框架，网络框架应该是一整套的，而不是单一的切换

        NetCacheManager.getInstance().requestCache(Constant.GET_SEARCH_LIST, reqSearchAlbum, true, new RequestCallBack<ResponseSearchAlbum>(ResponseSearchAlbum.class) {

            @Override
            public void onResponse(ResponseSearchAlbum data) {

                //保存数据，有问题，还是需要调用方，主动去切换贤臣


            }

            @Override
            public void onError(String cmd, Error error) {
                if (listener != null) {
                    listener.onError(getErrorMessage(error));
                }
            }
        });
    }
}
