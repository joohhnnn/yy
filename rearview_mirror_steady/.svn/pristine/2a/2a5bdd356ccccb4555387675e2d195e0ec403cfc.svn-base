package com.txznet.txz.component.nav.txz;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.NavThirdApp;

public class NavTxzImpl extends NavThirdApp {
	public static boolean mIsInNav = false;

	public static void setInNav(boolean b) {
		mIsInNav = b;
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				oRun.onInit(true);
			}
		}, 0);
		return 0;
	}

	@Override
	public void release() {
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		byte[] bs = NavigateInfo.toByteArray(info);
		ServiceManager.getInstance().sendInvoke(getPackageName(), "nav.action.startnavi", bs, null);
		return true;
	}

	@Override
	public boolean isInNav() {
		return mIsInNav;
	}

	@Override
	public void enterNav() {
		ServiceManager.getInstance().sendInvoke(getPackageName(), "nav.action.open", null, null);
	}

	@Override
	public void exitNav() {
		ServiceManager.getInstance().sendInvoke(getPackageName(), "nav.action.stopnavi", null, null);

		// 退出了应用
		onExitApp();
	}

	@Override
	public String getPackageName() {
		return ServiceManager.NAV;
	}

}
