package com.txznet.txz.component.choice.page;

import android.util.Log;

import com.txznet.txz.module.ticket.TrainTicketData;
import com.txznet.txz.module.ticket.TrainTicketData.ResultBean.TicketListBean;

import java.util.ArrayList;

public abstract class ResTrainPage extends ResourcePage<TrainTicketData, TrainTicketData.ResultBean.TicketListBean> {

	public ResTrainPage(TrainTicketData resources) {
		super(resources, resources.result.ticketList.size());
	}

	@Override
	protected void clearCurrRes(TrainTicketData currRes) {
		if (currRes == null) {
			return;
		}
		currRes.destination = "";
		currRes.origin = "";
		currRes.departDate = "";
		currRes.departTime = "";
		if (currRes.result == null || currRes.result.ticketList == null || currRes.result.ticketList.size() == 0) {
			return;
		}
		currRes.result.ticketList.clear();
	}

	@Override
	protected TrainTicketData notifyPage(int sIdx, int len, TrainTicketData sourceRes) {
		TrainTicketData data = null;
		if (sourceRes == null) {
			return data;
		}
		data = new TrainTicketData();
		data.origin = sourceRes.origin;
		data.destination = sourceRes.destination;
		data.departDate = sourceRes.departDate;
		data.departTime = sourceRes.departTime;
		if (sourceRes.result == null) {
			return data;
		}
		data.result = new TrainTicketData.ResultBean();
		if (sourceRes.result.ticketList == null) {
			return data;
		}
		data.result.ticketList = new ArrayList<TicketListBean>(len);
		for (int i = sIdx; i < sIdx + len; i++) {
			if (i >= 0 && i < sourceRes.result.ticketList.size()) {
				data.result.ticketList.add(sourceRes.result.ticketList.get(i));
			}
		}
		return data;
	}

	@Override
	protected int getCurrResSize(TrainTicketData currRes) {
		if (currRes == null | currRes.result == null || currRes.result.ticketList == null || currRes.result.ticketList.size() == 0) {
			return 0;
		}
		return currRes.result.ticketList.size();
	}

	@Override
	public TicketListBean getItemFromCurrPage(int idx) {
		TrainTicketData currRes = getResource();
		int size = getCurrResSize(currRes);
		if (idx >= 0 && idx < size) {
			return currRes.result.ticketList.get(idx);
		}
		return null;
	}

	@Override
	public TicketListBean getItemFromSource(int idx) {
		TrainTicketData currRes = mSourceRes;
		int size = getCurrResSize(currRes);
		if (idx >= 0 && idx < size) {
			return currRes.result.ticketList.get(idx);
		}
		return null;
	}

}
