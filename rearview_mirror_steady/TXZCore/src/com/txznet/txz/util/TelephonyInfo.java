package com.txznet.txz.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

public final class TelephonyInfo {

	private static TelephonyInfo telephonyInfo;
	private String strIMEI1;
	private String strIMEI2;
	private String strIMSI1;
	private String strIMSI2;

	private boolean bIsMTKDualCard = false;
	private boolean bIsGaotongDualCard = false;

	public String getIMSI1() {
		return strIMSI1;
	}

	public String getIMSI2() {
		return strIMSI2;
	}

	public boolean isDualCard() {
		return strIMSI2 != null;
	}

	private TelephonyInfo() {
	}

	public static TelephonyInfo getInstance() {

		if (telephonyInfo == null) {
			synchronized (TelephonyInfo.class) {
				if (telephonyInfo == null) {
					telephonyInfo = new TelephonyInfo();

					TelephonyManager telephonyManager = ((TelephonyManager) GlobalContext.get()
							.getSystemService(Context.TELEPHONY_SERVICE));

					try {
						// 尝试获取MTK双卡双待信息
						Class<?> c = Class
								.forName("com.android.internal.telephony.Phone");
						// Field[] fs = c.getDeclaredFields();
						// for (int i = 0; i < fs.length; ++i)
						// {
						// String s = fs[i].getName();
						// Log.d("pbh", "field: " + s);
						// }
						// Method[] ms = c.getMethods();
						// for (int i = 0; i < ms.length; ++i)
						// {
						// String s = ms[i].getName();
						// Log.d("pbh", "method: " + s);
						// }
						Field fields1 = c.getField("GEMINI_SIM_1");
						fields1.setAccessible(true);
						Integer simId1 = ((Integer) fields1.get(null));
						Field fields2 = c.getField("GEMINI_SIM_2");
						fields2.setAccessible(true);
						Integer simId2 = ((Integer) fields2.get(null));
						Method mIMSI = TelephonyManager.class
								.getDeclaredMethod("getSubscriberIdGemini",
										int.class);
						telephonyInfo.strIMSI1 = ((String) mIMSI.invoke(
								telephonyManager, simId1));
						telephonyInfo.strIMSI2 = ((String) mIMSI.invoke(
								telephonyManager, simId2));

						Method mIMEI = TelephonyManager.class
								.getDeclaredMethod("getDeviceIdGemini",
										int.class);
						telephonyInfo.strIMEI1 = ((String) mIMEI.invoke(
								telephonyManager, simId1));
						telephonyInfo.strIMEI2 = ((String) mIMEI.invoke(
								telephonyManager, simId2));
						telephonyInfo.bIsMTKDualCard = true;
					} catch (Exception eMTK) {
						try {
							Class<?> cx = Class
									.forName("android.telephony.MSimTelephonyManager");
							Object obj = GlobalContext.get().getSystemService(
									"phone_msim");

							Method md = cx.getMethod("getDeviceId", int.class);
							Method ms = cx.getMethod("getSubscriberId",
									int.class);

							telephonyInfo.strIMSI1 = ((String) ms
									.invoke(obj, 0));
							telephonyInfo.strIMSI2 = ((String) ms
									.invoke(obj, 1));
							telephonyInfo.strIMEI1 = ((String) md
									.invoke(obj, 0));
							telephonyInfo.strIMEI2 = ((String) md
									.invoke(obj, 1));
						} catch (Exception eGaotong) {
							try {
								telephonyInfo.strIMEI1 = telephonyManager
										.getDeviceId();
								telephonyInfo.strIMEI2 = null;
								telephonyInfo.strIMSI1 = telephonyManager
										.getSubscriberId();
								telephonyInfo.strIMSI2 = null;
							} catch (Exception e) {
								LogUtil.loge("getIMEI error", e);
							}
						}
					}

					try {
						telephonyInfo.strIMEI2 = telephonyInfo.strIMEI1 = ((TelephonyManager)GlobalContext.get().getSystemService(
										Context.TELEPHONY_SERVICE)).getDeviceId();
					} catch (Exception e) {
						LogUtil.loge("getIMEI error", e);
					}
				}
			}
		}

		return telephonyInfo;
	}

}