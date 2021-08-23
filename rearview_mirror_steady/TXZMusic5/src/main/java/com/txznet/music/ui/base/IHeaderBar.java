package com.txznet.music.ui.base;

/**
 * @author zackzhou
 * @date 2019/3/21,12:01
 */

public interface IHeaderBar {

    void removeHeader();

    void addHeader(IHeaderView headerView);

    boolean hasHeader();
}
