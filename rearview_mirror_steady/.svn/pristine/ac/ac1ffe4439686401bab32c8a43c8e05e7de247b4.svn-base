package com.txznet.txz.component.choice.page;

import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;

import java.util.ArrayList;

public abstract class ResCompetitionPage extends ResourcePage<CompetitionData, CompetitionBean> {

	public ResCompetitionPage(CompetitionData resources) {
		super(resources, resources.mCompetitionBeans.size());
	}

	@Override
	protected void clearCurrRes(CompetitionData currRes) {
		if (currRes == null) {
			return;
		}
		currRes.mCompetition = "";
		currRes.mDatetime = "";
		if (currRes.mCompetitionBeans == null || currRes.mCompetitionBeans.size() == 0) {
			return;
		}
		currRes.mCompetitionBeans.clear();
	}

	@Override
	protected CompetitionData notifyPage(int sIdx, int len, CompetitionData sourceRes) {
		CompetitionData data = null;
		if (sourceRes == null) {
			return data;
		}
		data = new CompetitionData();
		data.mCompetition = sourceRes.mCompetition;
		data.mDatetime = sourceRes.mDatetime;
		data.mCompetitionBeans = sourceRes.mCompetitionBeans;
		if (sourceRes.mCompetitionBeans == null) {
			return data;
		}
		data.mCompetitionBeans = new ArrayList<CompetitionBean>(len);
		for (int i = sIdx; i < sIdx + len; i++) {
			if (i >= 0 && i < sourceRes.mCompetitionBeans.size()) {
				data.mCompetitionBeans.add(sourceRes.mCompetitionBeans.get(i));
			}
		}
		return data;
	}

	@Override
	protected int getCurrResSize(CompetitionData currRes) {
		if (currRes == null || currRes.mCompetitionBeans == null || currRes.mCompetitionBeans.size() == 0) {
			return 0;
		}
		return currRes.mCompetitionBeans.size();
	}

	@Override
	public CompetitionBean getItemFromCurrPage(int idx) {
		CompetitionData currRes = getResource();
		int size = getCurrResSize(currRes);
		if (idx >= 0 && idx < size) {
			return currRes.mCompetitionBeans.get(idx);
		}
		return null;
	}

	@Override
	public CompetitionBean getItemFromSource(int idx) {
		CompetitionData currRes = mSourceRes;
		int size = getCurrResSize(currRes);
		if (idx >= 0 && idx < size) {
			return currRes.mCompetitionBeans.get(idx);
		}
		return null;
	}

}
