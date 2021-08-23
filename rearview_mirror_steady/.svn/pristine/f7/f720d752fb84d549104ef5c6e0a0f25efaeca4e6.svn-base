package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * list数据的基类，将解析title的方法封装了
 */

public abstract class BaseListMsgData<T> extends BaseMsgData {
    public TitleInfo mTitleInfo;
    public ArrayList<T> mDatas;

    public BaseListMsgData(int type) {
        super(type);
    }

    public void parseTitle(JSONBuilder jsData) {
        mTitleInfo = new TitleInfo();
        mTitleInfo.count = jsData.getVal("count", Integer.class,0);
        mTitleInfo.curPage = jsData.getVal("curPage", Integer.class);
        mTitleInfo.maxPage = jsData.getVal("maxPage", Integer.class);
        mTitleInfo.action = jsData.getVal("action", String.class);
        mTitleInfo.prefix = jsData.getVal("prefix", String.class);
        mTitleInfo.titlefix = jsData.getVal("titlefix", String.class);
        mTitleInfo.aftfix = jsData.getVal("aftfix", String.class);
        mTitleInfo.cityfix = jsData.getVal("city", String.class);
        mTitleInfo.midfix = jsData.getVal("midfix", String.class);
    }
}