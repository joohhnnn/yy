package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IReminderListView extends IListView {
	@Override
	public void init() {
		mViewType = ViewData.TYPE_FULL_LIST_REMINDER;
	}


}
