package com.txznet.txz.component.choice.page;

import java.util.ArrayList;
import java.util.List;

import com.txznet.txz.component.choice.list.MusicWorkChoice.MusicData;
import com.txznet.txz.module.music.bean.AudioShowData;

public abstract class ResMusicPage extends ResourcePage<MusicData, AudioShowData> {

	public ResMusicPage(MusicData resources) {
		super(resources, resources.datas.size());
	}

	@Override
	protected void clearCurrRes(MusicData currRes) {
		if (currRes != null && currRes.datas != null) {
			currRes.datas.clear();
			currRes.datas = null;
		}
	}

	@Override
	protected MusicData notifyPage(int sIdx, int len, MusicData sourceRes) {
		List<AudioShowData> asds = sourceRes.datas;
		MusicData md = new MusicData();
		md.continuePlay = sourceRes.continuePlay;
		md.delayTime = sourceRes.delayTime;
		md.isAuto = sourceRes.isAuto;
		md.datas = new ArrayList<AudioShowData>();
		if (asds != null) {
			for (int i = sIdx; i < sIdx + len; i++) {
				if (i >= 0 && i < asds.size()) {
					md.datas.add(asds.get(i));
				}
			}
		}
		return md;
	}

	@Override
	protected int getCurrResSize(MusicData currRes) {
		if (currRes != null && currRes.datas != null) {
			return currRes.datas.size();
		}
		return 0;
	}

	@Override
	public AudioShowData getItemFromCurrPage(int idx) {
		MusicData md = getResource();
		if (md != null && md.datas != null && idx >= 0 && idx < md.datas.size()) {
			return md.datas.get(idx);
		}
		return null;
	}

	@Override
	public AudioShowData getItemFromSource(int idx) {
		MusicData md = mSourceRes;
		if (md != null && md.datas != null && idx >= 0 && idx < md.datas.size()) {
			return md.datas.get(idx);
		}
		return null;
	}
}