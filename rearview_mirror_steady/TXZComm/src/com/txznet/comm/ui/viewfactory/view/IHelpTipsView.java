package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IHelpTipsView extends MsgViewBase {
	@Override
	public void init() {
		mViewType = ViewData.TYPE_CHAT_HELP_TIPS;
	}
}
