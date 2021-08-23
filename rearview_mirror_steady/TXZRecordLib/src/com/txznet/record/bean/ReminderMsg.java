package com.txznet.record.bean;

import com.txznet.record.adapter.ChatReminderAdapter;


public class ReminderMsg extends BaseDisplayMsg<ChatReminderAdapter.ReminderItem>{

	public ReminderMsg() {
		super(BaseDisplayMsg.TYPE_FROM_SYS_REMINDER_LIST);
	}

}
