package com.txznet.music.service.push;

import android.view.View;

import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.rxflux.Operation;

/**
 * @author telen
 * @date 2018/12/27,14:29
 */
public interface IPushInvoker {

    void initData(PushResponse pushResponse, PlayListData data);

    /**
     * 获取展示的界面
     *
     * @return 需要显示的界面
     */
    View getView();

    /**
     * 是否处于展示中
     */
    boolean isShowing();

    /**
     * 展示界面
     */
    void onShowView();

    /**
     * 首栏点击
     */
    void onClickFirstItem(Operation operation);

    /**
     * 次栏点击
     */
    void onClickSecondItem(Operation operation);

    /**
     * 取消选择
     */
    void onCancel(boolean shouldExit);


    /**
     * 开始播报
     */
    void onPlayBegin(AudioV5 audio);


    /**
     * 播报结束(可能是tts播报结束,或者是audio播报结束)
     */
    void onPlayEnd();

    /**
     * 释放资源
     */
    void release();
}
