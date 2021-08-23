package com.txznet.record.bean;

import com.txznet.record.adapter.ChatTtsThemeAdapter.TtsThemeItem;

public class TtsThemeMsg extends BaseDisplayMsg<TtsThemeItem> {

	public TtsThemeMsg() {
		super(BaseDisplayMsg.TYPE_FROM_TTS_THEME);
	}
}