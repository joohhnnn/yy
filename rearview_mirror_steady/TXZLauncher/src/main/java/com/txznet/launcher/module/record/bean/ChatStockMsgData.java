package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

/**
 * Created by ASUS User on 2018/3/5.
 * 股票数据
 */

public class ChatStockMsgData extends BaseMsgData {
    public StockData mStockData;
    public ChatStockMsgData() {
        super(TYPE_CHAT_STOCK);
    }

    public class StockData{
        public String strName;
        public String strCode;
        public String strUrl;
        public String strCurrentPrice;
        public String strChangeAmount;
        public String strChangeRate;
        public String strHighestPrice;
        public String strLowestPrice;
        public String strTradingVolume;
        public String strYestodayClosePrice;
        public String strTodayOpenPrice;
        public String strUpdateTime;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        mStockData = new StockData();
        mStockData.strName = jsData.getVal("mName", String.class);
        mStockData.strCode = jsData.getVal("mCode", String.class);
        mStockData.strUrl = jsData.getVal("mChartImgUrl", String.class);
        mStockData.strCurrentPrice = jsData.getVal("mCurrentPrice", String.class);
        mStockData.strChangeAmount = jsData.getVal("mChangeAmount",String.class );
        mStockData.strChangeRate = jsData.getVal("mChangeRate",String.class );
        mStockData.strHighestPrice = jsData.getVal("mHighestPrice", String.class);
        mStockData.strLowestPrice = jsData.getVal("mLowestPrice", String.class);
        mStockData.strTradingVolume = jsData.getVal("mtradingVolume", String.class);
        mStockData.strYestodayClosePrice = jsData.getVal("mYesterdayClosingPrice",String.class);
        mStockData.strTodayOpenPrice = jsData.getVal("mTodayOpeningPrice",String.class);
        mStockData.strUpdateTime = jsData.getVal("mUpdateTime",String.class );
    }
}
