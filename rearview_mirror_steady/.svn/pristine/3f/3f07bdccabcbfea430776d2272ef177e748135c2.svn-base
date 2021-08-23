package com.txznet.txz.component.choice.page;

import java.util.ArrayList;

import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.choice.list.PoiWorkChoice.PoisData;

public abstract class ResPoiPage extends ResourcePage<PoisData, Poi> {

	public ResPoiPage(PoisData resources) {
		super(resources, resources.mPois.size());
	}

	@Override
	protected void clearCurrRes(PoisData currRes) {
		if (currRes != null && currRes.mPois != null && !currRes.mPois.isEmpty()) {
			currRes.mPois.clear();
		}
	}

	@Override
	protected PoisData notifyPage(int sIdx, int len, PoisData sourceRes) {
		PoisData poisData = new PoisData();
		poisData.action = sourceRes.action;
		poisData.city = sourceRes.city;
		poisData.isBus = sourceRes.isBus;
		poisData.tips = sourceRes.tips;
		poisData.keywords = sourceRes.keywords;
		poisData.mPois = new ArrayList<Poi>();
		poisData.mPois.addAll(sourceRes.mPois.subList(sIdx, sIdx + len));
		return poisData;
	}

	@Override
	protected int getCurrResSize(PoisData currRes) {
		return currRes.mPois != null ? currRes.mPois.size() : 0;
	}

	@Override
	public Poi getItemFromCurrPage(int idx) {
		if (getResource() != null) {
			int size = getCurrResSize(getResource());
			if (size > idx && idx >= 0) {
				return getResource().mPois.get(idx);
			}
		}
		return null;
	}

	@Override
	public Poi getItemFromSource(int idx) {
		if (mSourceRes != null) {
			int size = getCurrResSize(mSourceRes);
			if (size > idx && idx >= 0) {
				return mSourceRes.mPois.get(idx);
			}
		}
		return null;
	}
}
