package com.txznet.txz.component.nav.baidu;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

import android.content.pm.PackageInfo;
import android.text.TextUtils;

public class BaiduVersion {
	// 与百度导航深度定制的最低版本
	public static final int LOWER_VERSION = 4716;
	// 与百度导航深度定制的最高版本
	public static final int HIGHT_VERSION = 4800;
	// 百度导航异步SDK的版本号
	public static final int USE_NEW_SDK_VERSION = 4717;
	// 汽车版导航的版本号信息
	public static final float CAR_LOWER_VERSION = 0.5f;

	public static final String BAIDU_NAVI_PACKAGE = "com.baidu.navi";
	public static final String BAIDU_AUTONAVI_PACKAGE = "com.baidu.naviauto";

	static String mCurrNavPackageName;
	static List<String> sSuppPackageNames = new ArrayList<String>();

	static {
		sSuppPackageNames.clear();
		sSuppPackageNames.add(BAIDU_AUTONAVI_PACKAGE);
		sSuppPackageNames.add(BAIDU_NAVI_PACKAGE);
		mCurrNavPackageName = refeshPackageName();
	}

	public static String refeshPackageName() {
		for (String pkn : sSuppPackageNames) {
			JNIHelper.logd("BaiduVersion refreshPackageName:" + sSuppPackageNames);
			if (PackageManager.getInstance().checkAppExist(pkn)) {
				mCurrNavPackageName = pkn;
				JNIHelper.loge("mCurrNavPackageName:" + mCurrNavPackageName);
				return pkn;
			} else {
				JNIHelper.logw("app " + pkn + " not exist");
			}
		}
		return null;
	}

	public static String getCurPackageName() {
		if (TextUtils.isEmpty(mCurrNavPackageName)) {
			refeshPackageName();
		}
		return mCurrNavPackageName;
	}

	static Boolean sIsSupportSDK;

	public static boolean isSupportProt(boolean needCheck) {
		JNIHelper.logd("isSupportBaidu :" + sIsSupportSDK);

		if (sIsSupportSDK != null && !needCheck) {
			return sIsSupportSDK;
		}
		sIsSupportSDK = false;

		if (needCheck || TextUtils.isEmpty(mCurrNavPackageName)) {
			mCurrNavPackageName = refeshPackageName();
		}

		if (BAIDU_NAVI_PACKAGE.equals(mCurrNavPackageName)) {
			int vc = getFourNum(mCurrNavPackageName);
			if (vc >= LOWER_VERSION && vc < HIGHT_VERSION) {
				if (vc >= USE_NEW_SDK_VERSION) {
					sIsSupportSDK = true;
				}
			}

		} else if (BAIDU_AUTONAVI_PACKAGE.equals(mCurrNavPackageName)) {
			sIsSupportSDK = true;
		}
		return sIsSupportSDK;
	}

	public static int getFourNum(String packageName) {
		try {
			PackageInfo info = GlobalContext.get().getPackageManager().getPackageInfo(packageName, 0);
			if (info != null) {
				String vn = info.versionName;
				if (!TextUtils.isEmpty(vn)) {
					Pattern pattern = Pattern.compile("([0-9]+)");
					Matcher matcher = pattern.matcher(vn);
					StringBuilder sb = new StringBuilder();
					while (matcher.find()) {
						sb.append(matcher.group());
					}

					while (sb.length() < 4) {
						sb.append("0");
					}
					if (sb.length() != 4) {
						String v = sb.substring(0, 4);
						return Integer.parseInt(v);
					} else {
						return Integer.parseInt(sb.toString());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
