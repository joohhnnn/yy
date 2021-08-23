package com.txznet.txz.component.choice.page;

import java.util.ArrayList;
import java.util.List;

import com.txznet.txz.component.choice.list.FlightWorkChoice.FlightDataBean;
import com.txznet.txz.component.choice.list.FlightWorkChoice.FlightItem;

public abstract class ResFlightPage extends ResourcePage<FlightDataBean, FlightItem> {

	public ResFlightPage(FlightDataBean dataBean) {
		super(dataBean, dataBean.datas.size());
	}

	/**
	 * 拿不到所有数据情况下
	 * 
	 * @param totalSize
	 */
	public ResFlightPage(int totalSize) {
		super(null, totalSize);
	}

	@Override
	protected void clearCurrRes(FlightDataBean dataBean) {
		if (dataBean != null && dataBean.datas != null) {
			dataBean.datas.clear();
			dataBean.datas = null;
			dataBean.arrivalCity = "";
			dataBean.departCity = "";
			dataBean.date = "";
		}
	}

	@Override
	protected FlightDataBean notifyPage(int sIdx, int len, FlightDataBean dataBean) {
		List<FlightItem> datas = dataBean.datas;
		FlightDataBean newDataBean = new FlightDataBean();
		newDataBean.arrivalCity = dataBean.arrivalCity;
		newDataBean.departCity = dataBean.departCity;
		newDataBean.date = dataBean.date;
		newDataBean.datas = new ArrayList<FlightItem>();
		if(dataBean != null){
			for (int i = sIdx; i < sIdx + len; i++) {
				if (i >= 0 && i < datas.size()) {
					newDataBean.datas.add(datas.get(i));
				}
			}
		}
		return newDataBean;
	}

	@Override
	protected abstract int numOfPageSize();

	@Override
	protected int getCurrResSize(FlightDataBean dataBean) {
		if (dataBean != null && dataBean.datas != null) {
			return dataBean.datas.size();
		}
		return 0;
	}

	@Override
	public FlightItem getItemFromCurrPage(int idx) {
		FlightDataBean dataBean = getResource();
		if(dataBean != null && dataBean.datas != null && idx >= 0 && idx < dataBean.datas.size()){
			return dataBean.datas.get(idx);
		}
		return null;
	}

	@Override
	public FlightItem getItemFromSource(int idx) {
		FlightDataBean dataBean = mSourceRes;
		if(dataBean != null && dataBean.datas != null && idx >= 0 && idx < dataBean.datas.size()){
			return dataBean.datas.get(idx);
		}
		return null;
	}
}
