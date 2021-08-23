package com.txznet.txz.component.nav.txz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZNavManager.NavStatusListener;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.INavInquiryRoadTraffic;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

public class NavCommImpl extends NavThirdComplexApp implements INavInquiryRoadTraffic {
	public static final String PACKAGE_NAME = "com.txznet.txz.comm.nav";
	public static final String RECV_ACTION = "com.txznet.txz.nav.comm.recv";

	private IInitCallback mInitCbRun;
	private String mLastUsePackageName;
	private Map<String, NavApiImpl> mToolsMap = new HashMap<String, NavApiImpl>();

	public NavCommImpl() {
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			doReceive(intent);
		}
	};

	@Override
	public int initialize(IInitCallback oRun) {
		mInitCbRun = oRun;
//		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
//
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				doReceive(intent);
//			}
//		}, new IntentFilter(RECV_ACTION));
		GlobalContext.get().registerReceiver(mReceiver,new IntentFilter(RECV_ACTION));
		startCheckNavTool();
		return 0;
	}

	//应用卸载时注销广播
	@Override
	public void release() {
		super.release();
		GlobalContext.get().unregisterReceiver(mReceiver);
	}

	@Override
	public void setNavStatusListener(NavStatusListener listener) {
		super.setNavStatusListener(listener);
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			app.setNavStatusListener(listener);
		}
	}

	/**
	 * 广播测试广播，初始化工具
	 */
	private void startCheckNavTool() {
		Intent intent = new Intent(NavApiImpl.SEND_ACTION);
		intent.putExtra(NavApiImpl.KEY_TYPE, 10015);
		intent.putExtra(NavApiImpl.SOURCE_APP, "txz");
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("send check nav broadcast");
	}

	/**
	 * 获取当前使用的导航工具
	 * 
	 * @return
	 */
	private NavApiImpl getCurrApiImpl() {
		if (mToolsMap.containsKey(mLastUsePackageName)) {
			return mToolsMap.get(mLastUsePackageName);
		}

		// TODO 优先级选取
		if (mToolsMap.size() > 0) {
			for (String key : mToolsMap.keySet()) {
				return mToolsMap.get(key);
			}
		}

		return null;
	}

	@Override
	public void exitNav() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			impl.exitNav();
		}
	}

	@Override
	public void enterNav() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			impl.enterNav();
		}
	}

	@Override
	public boolean isInNav() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.isInNav();
		}
		return super.isInNav();
	}

	@Override
	public boolean isInFocus() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.isInFocus();
		}
		return super.isInFocus();
	}

	private void doReceive(Intent intent) {
		int key_type = intent.getIntExtra(NavApiImpl.KEY_TYPE, -1);
		String pk = intent.getStringExtra(NavApiImpl.SOURCE_APP);
		LogUtil.logd("NavCommImpl doReceive key_type:" + key_type + ",pk:" + pk + ",bundle:" + intent.getExtras());
		if (key_type == -1) {
			return;
		}

		if (TextUtils.isEmpty(pk)) {
			JNIHelper.loge("recv source_app is empty！");
			return;
		}
		mLastUsePackageName = pk;

		if (key_type == 10015) {
			JNIHelper.logd("link pk:" + pk);
//			AppLogic.removeBackGroundCallback(mLinkTimeOutTask);
			if (pk != null) {
				synchronized (mToolsMap) {
					if (!mToolsMap.containsKey(pk)) {
						int flag = intent.getIntExtra("FLAGS",0);
						NavApiImpl nta = new NavApiImpl(pk, flag);
						nta.setNavStatusListener(mNavStatusListener);
						mToolsMap.put(pk, nta);
					}
				}

				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						if (mInitCbRun != null) {
							mInitCbRun.onInit(true);
							mInitCbRun = null;
						}
					}
				});
			}
			return;
		}

		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			impl.handleRecv(intent);
		}
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String type, String command) {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			impl.onNavCommand(fromWakeup, type, command);
		}
	}

	@Override
	public String disableProcJingYouPoi() {
		return getCurrApiImpl().disableProcJingYouPoi();
	}

	@Override
	public boolean procJingYouPoi(Poi... pois) {
		super.procJingYouPoi(pois);
		return getCurrApiImpl().procJingYouPoi(pois);
	}

	@Override
	public List<Poi> getJingYouPois() {
		return getCurrApiImpl().getJingYouPois();
	}

	@Override
	public boolean deleteJingYou(Poi poi) {
		super.deleteJingYou(poi);
		return getCurrApiImpl().deleteJingYou(poi);
	}

	@Override
	public String disableDeleteJingYou() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return getCurrApiImpl().disableDeleteJingYou();
		}
		return super.disableDeleteJingYou();
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.NavigateTo(plan, info);
		}
		return false;
	}

	@Override
	public List<String> getBanCmds() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.getBanCmds();
		}
		return null;
	}

	@Override
	public String[] getSupportCmds() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.getSupportCmds();
		}
		return null;
	}

	@Override
	public List<String> getCmdNavOnly() {
		NavApiImpl impl = getCurrApiImpl();
		if (impl != null) {
			return impl.getCmdNavOnly();
		}
		return null;
	}

	@Override
	public String getPackageName() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.getPackageName();
		}
		return PACKAGE_NAME;
	}

	@Override
	public boolean isReachable() {
		final NavThirdApp nta = getCurrApiImpl();
		if (mToolsMap.size() <= 0 || nta == null) {
			return false;
		}

		return PackageManager.getInstance().checkAppExist(nta.getPackageName());
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			app.updateHomeLocation(navigateInfo);
		}
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			app.updateCompanyLocation(navigateInfo);
		}
	}

	@Override
	public void broadNaviInfo(String navJson) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			((NavThirdComplexApp) app).broadNaviInfo(navJson);
		}
	}

	@Override
	public boolean speakLimitSpeech() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return ((NavThirdComplexApp) app).speakLimitSpeech();
		}
		return super.speakLimitSpeech();
	}

	@Override
	public void speakHowNavi(boolean isWakeupResult) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			((NavThirdComplexApp) app).speakHowNavi(isWakeupResult);
		}
	}

	@Override
	public void speakAskRemain(boolean isWakeupResult) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			((NavThirdComplexApp) app).speakAskRemain(isWakeupResult);
		}
	}

	@Override
	public void queryHomeCompanyAddr() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			((NavThirdComplexApp) app).queryHomeCompanyAddr();
		}
	}

	@Override
	public double[] getDestinationLatlng() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.getDestinationLatlng();
		}
		return null;
	}

	@Override
	public String getDestinationCity() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.getDestinationCity();
		}
		return null;
	}

	@Override
	public PathInfo getCurrentPathInfo() {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			return app.getCurrentPathInfo();
		}
		return null;
	}

	@Override
	public String disableNavWithFromPoi() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.disableNavWithFromPoi();
		}
		return super.disableNavWithFromPoi();
	}

	@Override
	public String disableNavWithWayPoi() {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.disableNavWithWayPoi();
		}
		return super.disableNavWithWayPoi();
	}

	@Override
	public boolean navigateWithWayPois(Poi startPoi, Poi endPoi, List<PathInfo.WayInfo> pois) {
		NavThirdApp app = getCurrApiImpl();
		if (app != null) {
			return app.navigateWithWayPois(startPoi, endPoi, pois);
		}
		return super.navigateWithWayPois(startPoi, endPoi, pois);
	}

	@Override
	public boolean inquiryRoadTrafficByFront() {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			return app.inquiryRoadTrafficByFront();
		}
		return false;
	}

	@Override
	public boolean inquiryRoadTrafficByNearby(String city, String keywords) {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			return app.inquiryRoadTrafficByNearby(city, keywords);
		}
		return false;
	}

	@Override
	public boolean inquiryRoadTrafficByPoi(String city, String keywords) {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			return app.inquiryRoadTrafficByPoi(city, keywords);
		}
		return false;
	}

	@Override
	public boolean isInquiryRoadTrafficSupported() {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			return app.isInquiryRoadTrafficSupported();
		}
		return false;
	}

	@Override
	public void registerInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener) {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			app.registerInquiryRoadTrafficResultListener(listener);
		}
	}

	@Override
	public void unregisterInquiryRoadTrafficResultListener(OnInquiryRoadTrafficResultListener listener) {
		NavApiImpl app = getCurrApiImpl();
		if (app != null) {
			app.unregisterInquiryRoadTrafficResultListener(listener);
		}
	}
}