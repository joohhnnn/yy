package com.txznet.txz.component.nav.cld;

import com.txznet.comm.remote.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public abstract class NaviGuidanceReceiver extends BroadcastReceiver {
	private static final String ACTION = "CLD.NAVI.MSG.GUIDANCEINFO";
	private static final String GUIDANCE_ARRAY_PARAM = "GUIDANCE_ARRAY_PARAM";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String action = intent.getAction();
			if (ACTION.equals(action)) {
				Bundle mBundle = intent.getExtras();
				if (mBundle == null) {
					return;
				}

				String[] contents = mBundle.getStringArray(GUIDANCE_ARRAY_PARAM);
				String[] infos = intent.getStringArrayExtra(GUIDANCE_ARRAY_PARAM);
				String[] content = null;
				boolean mRead = false;
				if (contents != null || infos != null) {
					do {
						int size = 0;
						if (contents != null) {
							size = contents.length;
							if (size < 1) {
								if (infos != null) {
									size = infos.length;
									if (size < 1) {
										break;
									}
									content = infos;
								}
							} else {
								content = contents;
							}
						}

						StringBuilder printTxt = new StringBuilder();
						for (String info : content) {
							printTxt.append(info);
							printTxt.append(",");
						}
						LogUtil.logd("cldNaviInfo:" + printTxt);
						CldDataStore.getInstance().reset();
						for (int index = 0; index < size - 1; index++) {
							switch (index) {
							case 0:
								if (TextUtils.isEmpty(content[0])) {
									content[0] = "-1";
								}
								CldDataStore.getInstance().lDirection = Long.parseLong(content[0]);
								break;

							case 1:
								if (TextUtils.isEmpty(content[1])) {
									content[1] = "-1";
								}
								CldDataStore.getInstance().lDistance = Long.parseLong(content[1]);
								break;

							case 2:
								if (TextUtils.isEmpty(content[2])) {
									content[2] = "-1";
								}
								CldDataStore.getInstance().lRemainDistance = Long.parseLong(content[2]);
								break;

							case 3:
								if (TextUtils.isEmpty(content[3])) {
									content[3] = "-1";
								}
								CldDataStore.getInstance().lTotalDistance = Long.parseLong(content[3]);
								break;

							case 4:
								if (TextUtils.isEmpty(content[4])) {
									content[4] = "-1";
								}
								CldDataStore.getInstance().lRemainTime = Long.parseLong(content[4]);
								break;

							case 5:
								if (TextUtils.isEmpty(content[5])) {
									content[5] = "-1";
								}
								CldDataStore.getInstance().lTotalTime = Long.parseLong(content[5]);
								break;

							case 6:
								if (TextUtils.isEmpty(content[6])) {
									content[6] = "-1";
								}
								CldDataStore.getInstance().szCurrentRoadName = content[6];
								break;

							case 7:
								if (TextUtils.isEmpty(content[7])) {
									content[7] = "-1";
								}
								CldDataStore.getInstance().szNextRoadName = content[7];
								break;

							case 8:
								if (TextUtils.isEmpty(content[8])) {
									content[8] = "-1";
								}
								CldDataStore.getInstance().lCurrentRoadType = Long.parseLong(content[8]);
								break;

							case 9:
								if (TextUtils.isEmpty(content[9])) {
									content[9] = "-1";
								}
								CldDataStore.getInstance().lCurrentSpeed = Long.parseLong(content[9]);
								break;

							case 10:
								if (TextUtils.isEmpty(content[10])) {
									content[10] = "-1";
								}
								CldDataStore.getInstance().lCurrentLimitedSpeed = Long.parseLong(content[10]);
								break;

							case 11:
								if (TextUtils.isEmpty(content[11])) {
									content[11] = "-1";
								}
								CldDataStore.getInstance().lCurrentGPSAngle = Long.parseLong(content[11]);
								break;

							case 12:
								if (TextUtils.isEmpty(content[12])) {
									content[12] = "-1";
								}
								CldDataStore.getInstance().lExitIndexRoads = Long.parseLong(content[12]);
								break;

							case 13:
								if (TextUtils.isEmpty(content[13])) {
									content[13] = "-1";
								}
								CldDataStore.getInstance().lNumOfOutRoads = Long.parseLong(content[13]);
								break;

							case 14:
								if (TextUtils.isEmpty(content[14])) {
									content[14] = "-1";
								}
								long reserver = Long.parseLong(content[14]);
								CldDataStore.getInstance().lReserve = reserver;
								if (reserver == -2) {
									// 导航退出，清空数据
									CldDataStore.getInstance().reset();
								}
								break;
							}
						}
						mRead = true;
					} while (false);
				}

				if (mRead) {
					onNavInfoUpdate(CldDataStore.getInstance());
				} else {
					onNavInfoUpdate(null);
				}
			}
		} catch (Exception e) {
		}
	}

	public abstract void onNavInfoUpdate(CldDataStore cds);
}
