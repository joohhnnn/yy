package com.txznet.txz.component.nav;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager.NavStatusListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class NavThirdApp implements INav {
	public static final String NAV_APP_ACTION = "com.txznet.txz.NAVI_ACTION";
	public static final String CLOSE_WIN_ACTION = "com.txznet.txz.close.win";
	public static final int CMD_CLOSE_WINASR = 1;

	protected boolean mIsFocus;
	protected boolean mIsStarted;
	protected boolean mIsPlaned;
	protected int mPlanStyle;
	protected boolean mEnableSave;

	protected String mRemotePackageName;
	protected NavStatusListener mNavStatusListener = null;
	protected WinConfirmAsr mWinConfirmAsr = null;

	public NavThirdApp() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CLOSE_WIN_ACTION);
		intentFilter.addAction(NAV_APP_ACTION);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				dismiss();
			}
		}, intentFilter);
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
	
	/**
	 * 更新家的地址
	 * 
	 * @param navigateInfo
	 */
	public void updateHomeLocation(NavigateInfo navigateInfo){
	}

	/**
	 * 更新公司的地址
	 * 
	 * @param navigateInfo
	 */
	public void updateCompanyLocation(NavigateInfo navigateInfo){
		
	}
	
	/**
	 * 查询家和公司地址（应在选择导航工具之后操作）
	 */
	public void queryHomeCompanyAddr(){
		
	}
	
	public boolean willNavAfterSet() {
		return true;
	}

	public abstract String getPackageName();

	public void setRemotePackageName(String pkn) {
		mRemotePackageName = pkn;
	}

	public void startNavByInner() {

	}

	@Override
	public boolean isInNav() {
		return mIsStarted && mIsPlaned;
	}
	
	public boolean isInFocus(){
		return mIsFocus;
	}

	@Override
	public void enterNav() {
		PackageManager.getInstance().openApp(getPackageName());
	}

	@Override
	public void exitNav() {
		PackageManager.getInstance().closeApp(getPackageName());
	}

	/**
	 * 获取导航的版本号信息
	 * 
	 * @return
	 */
	public int getMapVersion() {
		return PackageManager.getInstance().getVerionCode(getPackageName());
	}

	@Override
	public void setNavStatusListener(NavStatusListener listener) {
		this.mNavStatusListener = listener;
	}

	Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				mWinConfirmAsr.dismiss();
			}
		}
	};

	public boolean showTraffic(double lat, double lng) {
		return false;
	}

	public boolean showTraffic(String city, String addr) {
		return false;
	}

	public void dismiss() {
		AppLogic.removeUiGroundCallback(mDismissRunnable);
		AppLogic.runOnUiGround(mDismissRunnable, 0);
	}

	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("savePlan")) {
			try {
				mEnableSave = Boolean.parseBoolean(new String(data));
				JNIHelper.logd("mEnableSave:" + mEnableSave);
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	public String getRemainTime(Integer rt){
		if (rt != null) {
			int rtd = rt;
			return getRemainTime(rtd);
		}
		return "";
	}

	public String getRemainTime(Long rt) {
		if (rt == null) {
			return "";
		}

		if (rt <= 0) {
			return "";
		}

		if (rt > 60) {
			if (rt >= 3600) {
				int r = (int) (rt % 3600);
				int h = (int) (rt / 3600);
				int m = r / 60;
				return h + "小时" + (m > 0 ? m + "分钟" : "");
			} else {
				return (rt / 60) + "分钟";
			}
		} else {
			return rt + "秒";
		}
	}
	
	public String getRemainDistance(Integer distance) {
		if (distance != null) {
			int dd = distance;
			return getRemainDistance(dd);
		}
		return "";
	}

	public String getRemainDistance(Long distance) {
		if (distance == null) {
			return "";
		}
		if (distance <= 0) {
			return "";
		}

		if (distance > 1000) {
			return (Math.round(distance / 100.0) / 10.0) + "公里";
		} else {
			return distance + "米";
		}
	}
}
