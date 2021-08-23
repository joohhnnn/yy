package com.txznet.record.ui;

import java.text.DecimalFormat;

import android.text.TextUtils;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txz.ui.voice.VoiceData.StockInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.record.adapter.ChatContentAdapter.StockViewHolder;
import com.txznet.record.lib.R;
import com.txznet.txz.util.LanguageConvertor;

public class StockRefresher {

	private static StockRefresher mInstance;

	public static StockRefresher getInstance() {
		if (mInstance == null) {
			synchronized (StockRefresher.class) {
				if (mInstance == null) {
					mInstance = new StockRefresher();
				}
			}
		}
		return mInstance;
	}

	private StockInfo mStockInfo;

	public void updateData(StockInfo infos, StockViewHolder v) {
		if (infos == null)
			return;

		mStockInfo = infos;

		try {
			v.mName.setText(LanguageConvertor.toLocale(mStockInfo.strName));
			v.mCode.setText(mStockInfo.strCode);

			String price = mStockInfo.strCurrentPrice;
			if (price.length() > 6) {
				price = price.substring(0, 5);
			}
			char end = price.charAt(price.length() - 1);
			if ('.' == end) {
				price = price.substring(0, price.length() - 1);
			}
			v.mPrice.setText(subZeroAndDot(format(price)));
			v.mPrice.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strCurrentPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));
			refreshUpAndDown(v);

			
			if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
				v.mChangeRate.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
				v.mChangeRate.setTextColor(getColorByPrice(
						Float.parseFloat(mStockInfo.strCurrentPrice),
						Float.parseFloat(mStockInfo.strYestodayClosePrice)));
			}else {
//				v.mChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
//				v.mChangeAmount.setTextColor(getColorByPrice(
//						Float.parseFloat(mStockInfo.strCurrentPrice),
//						Float.parseFloat(mStockInfo.strYestodayClosePrice)));
			}

			v.mYestodayClosePrice.setText(format(mStockInfo.strYestodayClosePrice));

			v.mTodayOpenPrice.setText(format(mStockInfo.strTodayOpenPrice));
			v.mTodayOpenPrice.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strTodayOpenPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));

			v.mHighestPrice.setText(format(mStockInfo.strHighestPrice));
			v.mHighestPrice.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strHighestPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));

			v.mLowestPrice.setText(format(mStockInfo.strLowestPrice));
			v.mLowestPrice.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strLowestPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));

			v.mTradingVolume.setText(getTradingCountDes(mStockInfo.strTradingVolume));

			ImageLoader.getInstance().displayImage(mStockInfo.strUrl, v.mPic);
		} catch (Exception e) {
			LogUtil.loge("mStockInfo error!");
		}
	}
	private String addMark(String value){
		if (!value.startsWith("-")) {
			value = "+"+value;
		}
		return value;
	}
	private String format(String value) {
		if (TextUtils.isEmpty(value)) {
			return "";
		}
		return String.format("%.2f", Float.parseFloat(value));
	}
	public static String subZeroAndDot(String s){    
        if(s.indexOf(".") > 0){    
            s = s.replaceAll("0+?$", "");//去掉多余的0    
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉    
        }    
        return s;    
    }

	private int getColorByPrice(float curprice, float compareprice) {
		if (curprice > compareprice)
			return (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR1);
		else if (curprice < compareprice)
			return (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR2);
		else
			return (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_ITEM_COLOR2);
	}

	private String getTradingCountDes(String tradeDes) {
		String sTxt = mStockInfo.strTradingVolume;
		String origalTxt = sTxt;
		sTxt = sTxt.replaceAll("[,股]", "");
		try {
			long account = Long.parseLong(sTxt);
			if (account >= 100000000) {
//				int yi = (int) (account / 100000000);
//				int re = (int) (account % 100000000);
//				sTxt = yi + "."
//						+ (String.valueOf(re).length() > 2 ? String.valueOf(re).subSequence(0, 2) : String.valueOf(re))
//						+ "亿";
				sTxt = String.format("%.2f亿", account / 100000000.0f);
			} else if (account >= 10000) {
				int wan = (int) (account / 10000);
				int re = (int) (account % 10000);
				sTxt = wan + "."
						+ (String.valueOf(re).length() > 2 ? String.valueOf(re).subSequence(0, 2) : String.valueOf(re))
						+ "万";
			} else {
				sTxt = account + "股";
			}

		} catch (Exception e) {
			e.printStackTrace();
			sTxt = origalTxt;
		}
		return sTxt;
	}

	private void refreshUpAndDown(StockViewHolder v) {
		float todayOpenPrice = Float
				.parseFloat(mStockInfo.strYestodayClosePrice);
		float currentPrice = Float.parseFloat(mStockInfo.strCurrentPrice);
		v.mUpAndDown.setVisibility(View.VISIBLE);
		if (currentPrice > todayOpenPrice) {
			v.mUpAndDown.setImageResource(R.drawable.stock_up_icon);
		} else if (currentPrice < todayOpenPrice) {
			v.mUpAndDown.setImageResource(R.drawable.stock_down_icon);
		} else {
			v.mUpAndDown.setVisibility(View.INVISIBLE);
		}
	}

	public void release() {
		if (mStockInfo == null) {
			return;
		}
		synchronized (mInstance) {
			synchronized (mStockInfo) {
				mStockInfo = null;
			}
			mInstance = null;
		}
	}
}
