package com.txznet.txz.component.choice.page;

import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.txz.component.choice.list.WxWorkChoice;
import com.txznet.txz.component.choice.list.WxWorkChoice.WxData;

public abstract class ResWxPage extends ResourcePage<WxWorkChoice.WxData, WeChatContact> {

	public ResWxPage(WxWorkChoice.WxData resources, int totalSize) {
		super(resources, totalSize);
	}

	@Override
	protected void clearCurrRes(WxData currRes) {
		if (currRes != null) {
			currRes.cons.cons = null;
		}
	}

	@Override
	protected WxData notifyPage(int sIdx, int len, WxData sourceRes) {
		final WeChatContacts cons = sourceRes.cons;
		WeChatContact[] conArray = cons.cons;
		WxData wxData = new WxData();
		wxData.cons = new WeChatContacts();
		wxData.event = sourceRes.event;
		wxData.ttsSpk = sourceRes.ttsSpk;
		wxData.cons.cons = new WeChatContact[len];
		if (conArray != null) {
			for (int i = 0; i < len; i++) {
				if (i >= 0 && i < conArray.length) {
					wxData.cons.cons[i] = conArray[sIdx + i];
				}
			}
		}
		return wxData;
	}

	@Override
	protected int getCurrResSize(WxData currRes) {
		if (currRes != null && currRes.cons != null && currRes.cons.cons != null) {
			return currRes.cons.cons.length;
		}
		return 0;
	}

	@Override
	public WeChatContact getItemFromCurrPage(int idx) {
		WxData wxData = getResource();
		if (wxData != null && wxData.cons != null && wxData.cons.cons != null) {
			return wxData.cons.cons[idx];
		}
		return null;
	}

	@Override
	public WeChatContact getItemFromSource(int idx) {
		WxData wxData = mSourceRes;
		if (wxData != null && wxData.cons != null && wxData.cons.cons != null) {
			return wxData.cons.cons[idx];
		}
		return null;
	}
}