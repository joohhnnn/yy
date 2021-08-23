package com.txznet.comm.ui.viewfactory.data;

import com.txz.ui.voice.VoiceData.StockInfo;
import com.txznet.comm.util.JSONBuilder;

public class ChatShockViewData extends ViewData {
	public StockInfo mStockInfo;
	public String vTips;
	public ChatShockViewData() {
		super(TYPE_CHAT_SHARE);
	}
	public void parseData(String data) {
		mStockInfo = new StockInfo();
		JSONBuilder jBuilder = new JSONBuilder(data);
//		jBuilder.getVal("action", "addMsg");
//		jBuilder.getVal("type", "shock");
		mStockInfo.strName = jBuilder.getVal("strName", String.class);
		mStockInfo.strCode = jBuilder.getVal("strCode", String.class);
		mStockInfo.strUrl = jBuilder.getVal("strUrl", String.class);
		mStockInfo.strCurrentPrice = jBuilder.getVal("strCurrentPrice", String.class);
		mStockInfo.strChangeAmount = jBuilder.getVal("strChangeAmount",String.class );
		mStockInfo.strChangeRate = jBuilder.getVal("strChangeRate",String.class );
		mStockInfo.strHighestPrice = jBuilder.getVal("strHighestPrice", String.class);
		mStockInfo.strLowestPrice = jBuilder.getVal("strLowestPrice", String.class);
		mStockInfo.strTradingVolume = jBuilder.getVal("strTradingVolume", String.class);
		mStockInfo.strYestodayClosePrice = jBuilder.getVal("strYestodayClosePrice",String.class);
		mStockInfo.strTodayOpenPrice = jBuilder.getVal("strTodayOpenPrice",String.class);
		mStockInfo.strUpdateTime = jBuilder.getVal("strUpdateTime",String.class );
		vTips = jBuilder.getVal("vTips",String.class);
	}
}
