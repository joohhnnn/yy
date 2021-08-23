package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IChatToSysView extends MsgViewBase {


	@Override
	public void init() {
		mViewType = ViewData.TYPE_CHAT_TO_SYS;

	}

}
