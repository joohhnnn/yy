package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IFeedbackView extends MsgViewBase {

	@Override
	public ViewAdapter getView(ViewData data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() {
		mViewType = ViewData.TYPE_CHAT_FEEDBACK;
	}

}
