package com.txznet.txz.component.nav.mx;

import java.util.List;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.INavHighLevelInterface;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

public class NavMXImpl extends NavThirdApp implements INavHighLevelInterface {
	private static final String RECEIVER_ACTION = "NAVI_TO_VC_COMMUNICATE_MESSAGE";
	private static final String ACTION_TYPE = "CMD_TYPE_ENUM";
	private static final String ACTION_CONTENT = "COMMUNICATE_INFO_CONTENT";
	public static final String MX_PACKAGE_NAME = "com.mxnavi.mxnavi";

	// 退出导航
	private static final int CMD_SHUT_DOWN = 1;
	// 放大
	private static final int CMD_SETTING_SCALE_UP = 5;
	// 缩小
	private static final int CMD_SETTING_SCALE_DOWN = 6;
	// 查询剩余距离
	private static final int CMD_QUERY_DISTANCE = 83;
	// 查询剩余时间
	private static final int CMD_QUERY_TIME = 84;

	private MXNaviInfo mMXNaviInfo;
	private WinConfirmAsr mWinConfirmWithAsr;

	private static String[] MX_DIRECTION = new String[] { "直行", "左转", "左前方", "左后方", "左调头", "右转", "右前方", "右后方", "右调头",
			"隧道入口" };

	public NavMXImpl() {
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("com.mxnavi.mxnavi.NToC_NAVI_SHOW");
		iFilter.addAction("com.mxnavi.mxnavi.NToC_NAVI_HIDE");
		iFilter.addAction("com.mxnavi.mxnavi.TO_CTRL_TURNING_INFO");
		iFilter.addAction(RECEIVER_ACTION);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				LogUtil.logd("NavMXImpl onReceive action:" + action);
				if (action.equals("com.mxnavi.mxnavi.NToC_NAVI_SHOW")) {
					onResume();
					return;
				} else if (action.equals("com.mxnavi.mxnavi.NToC_NAVI_HIDE")) {
					onPause();
					return;
				} else if (action.equals("com.mxnavi.mxnavi.TO_CTRL_TURNING_INFO")) {
					// 导航转向协议
					/**
					 * 0 （直行） 1 （左转） 2 （左前方） 3 （左后方） 4 （左调头） 5 （右转） 6 （右前方） 7
					 * （右后方） 8 （右调头） 32（隧道入口）
					 */
					if (mMXNaviInfo == null) {
						mMXNaviInfo = new MXNaviInfo();
					}
					try {
						mMXNaviInfo.turnId = intent.getIntExtra("turnID", 0);
						mMXNaviInfo.remainDistance = intent.getIntExtra("destdistance", -1);
						mMXNaviInfo.curName = intent.getStringExtra("roadname");
						mMXNaviInfo.nextName = intent.getStringExtra("nextroadname");
						mMXNaviInfo.destName = intent.getStringExtra("destname");
						mMXNaviInfo.remainTime = intent.getIntExtra("desttime", -1);
						mMXNaviInfo.turnDistance = intent.getIntExtra("turndistance", -1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Bundle bundle = intent.getExtras();
				int type = bundle.getInt(ACTION_TYPE);
				String[] params = bundle.getStringArray(ACTION_CONTENT);
			}
		}, iFilter);

		LogUtil.logd("NavMXImpl >>isAppTop:" + isAppOnTop());
		if (isAppOnTop()) {
			regNavUiCommands();
		}
	}

	private void dismissDialog() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mWinConfirmWithAsr != null && mWinConfirmWithAsr.isShowing()) {
					mWinConfirmWithAsr.dismiss();
				}
			}
		}, 0);
	}

	@Override
	public void exitNav() {
		try {
			sendCommandToNav(CMD_SHUT_DOWN, null);
			dismissDialog();
			super.exitNav();
		} catch (Exception e) {
		}
	}

	private boolean isAppOnTop() {
		ActivityManager am = (ActivityManager) GlobalContext.get().getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (topActivity.getPackageName().equals(getPackageName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getPackageName() {
		return MX_PACKAGE_NAME;
	}

	private static final int CMD_SEARCH_WAY_RECOMEND = 1; // 推荐
	private static final int CMD_SEARCH_WAY_NEAR = 2; // 最短
	private static final int CMD_SEARCH_WAY_QUICK = 3; // 最快
	private static final int CMD_SEARCH_WAY_CHEAP = 4; // 经济

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		int cat = CMD_SEARCH_WAY_RECOMEND;
		if (plan == NavPlanType.NAV_PLAN_TYPE_RECOMMEND) {
			cat = CMD_SEARCH_WAY_RECOMEND;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_COST) {
			cat = CMD_SEARCH_WAY_CHEAP;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_DISTANCE) {
			cat = CMD_SEARCH_WAY_NEAR;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_TIME) {
			cat = CMD_SEARCH_WAY_QUICK;
		}

		if (info == null || info.msgGpsInfo == null) {
			return false;
		}
		LogUtil.logd("NavMXImpl >>  NavigateTo");
		String lat = String.valueOf(info.msgGpsInfo.dblLat);
		String lng = String.valueOf(info.msgGpsInfo.dblLng);
		String dp = lng + "," + lat;
		Intent i = new Intent("com.mxnavi.mxnavi.ONE_KEY_MSG");
		i.putExtra("Destination", info.strTargetAddress);
		i.putExtra("SearchWay", cat);
		i.putExtra("DestPoint", dp);
		i.putExtra("WayPoint", "");
		GlobalContext.get().sendBroadcast(i);
		return true;
	}
	
	public boolean NavigateByMCode(String name,String mcode){
		String params = new JSONBuilder().put("name", name).put("mcode", mcode).toString();
		sendCommandToNav(71, new String[]{"1.0",params,"0"});
		return true;
	}

	private void sendCommandToNav(int type, String[] params) {
		Bundle b = new Bundle();
		b.putInt("CMD_TYPE_ENUM", type);
		if (params != null) {
		} else {
			params = new String[3];
			params[0] = "1.0";
			params[1] = "";
			params[2] = "0";
		}
		b.putStringArray("COMMUNICATE_INFO_CONTENT", params);
		Intent intent = new Intent("VC_TO_NAVI_COMMUNICATE_MESSAGE");
		intent.putExtras(b);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("NavMXImpl >> start sendCommandToNav action:VC_TO_NAVI_COMMUNICATE_MESSAGE,  type:" + type);
	}

	public void regNavUiCommands() {
		AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "NAV_CTRL#" + getPackageName();
			}

			@Override
			public void onCommandSelected(String type, String command) {
				onNavCommand(isWakeupResult(), type, command);
			}
		};

		callback.addCommand("ZOOM_IN", "放大地图", "地图放大").addCommand("ASK_REMAIN", "还有多久", "多久到", "还要多久", "还有多远")
				.addCommand("ASK_HOW", "前面怎么走", "前面往哪走", "前面哪条路").addCommand("ZOOM_OUT", "缩小地图", "地图缩小")
				.addCommand("VIEW_ALL", "全览", "查看全程").addCommand("TUIJIANLUXIAN", "推荐路线")
				.addCommand("ZUIKUAILUXIAN", "最快路线", "躲避拥堵").addCommand("ZUIDUANLUCHENG", "最短路线", "少路程")
				.addCommand("JINGJI", "经济路线", "少收费").addCommand("EXIT_NAV", "退出导航", "放弃导航", "关闭导航", "离开导航","停止导航");
		WakeupManager.getInstance().useWakeupAsAsr(callback);
	}

	public void unregNavUiCommands() {
		WakeupManager.getInstance().recoverWakeupFromAsr("NAV_CTRL#" + getPackageName());
	}

	private class MXNaviInfo {
		// 转向的ID图标
		public int turnId = -1;
		// 还剩余多少距离到达目的地
		public int remainDistance;
		// 当前道路名称
		public String curName;
		// 下一跳道路名称
		public String nextName;
		// 目的地名称
		public String destName;
		// 剩余的时间
		public int remainTime;
		// 距离转向点距离
		public int turnDistance;
	}

	@Override
	public void addStatusListener() {
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onEnd(boolean arrive) {
	}

	@Override
	public void onPlanComplete() {
	}

	@Override
	public void onPlanError(int errCode, String errDesc) {
	}

	@Override
	public void onResume() {
		regNavUiCommands();
	}

	@Override
	public void onPause() {
		dismissDialog();
		unregNavUiCommands();
	}

	@Override
	public void onNavCommand(boolean isWakeup, String type, final String command) {
		if ("EXIT_NAV".equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("sence", "nav");
			json.put("text", command);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}
		
		LogUtil.logd("NavMXImpl >> onCommandSelected type:" + type + ",cmd:" + command);
		if ("ZOOM_IN".equals(type)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose("将为您" + command, new Runnable() {

				@Override
				public void run() {
					sendCommandToNav(CMD_SETTING_SCALE_UP, null);
				}
			});
			return;
		}
		if ("ZOOM_OUT".equals(type)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose("将为您" + command, new Runnable() {

				@Override
				public void run() {
					sendCommandToNav(CMD_SETTING_SCALE_DOWN, null);
				}
			});
			return;
		}
		if ("ASK_REMAIN".equals(type)) {
			if (mMXNaviInfo == null) {
				sendCommandToNav(CMD_QUERY_DISTANCE, null);
				sendCommandToNav(CMD_QUERY_TIME, null);
				RecorderWin.close();
				return;
			}

			Integer remainTime = mMXNaviInfo.remainTime;
			Integer remainDistance = mMXNaviInfo.remainDistance;

			String rt = getRemainTime(remainTime);
			String rd = getRemainDistance(remainDistance);
			String hint = "距离目的地还有";
			if (TextUtils.isEmpty(rt) && TextUtils.isEmpty(rd)) {
				hint = "";
			}
			if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
				hint += rd + "，大约" + rt;
			} else if (!TextUtils.isEmpty(rd)) {
				hint += rd;
			} else if (!TextUtils.isEmpty(rt)) {
				hint = "距离目的地还剩" + rt;
			}

			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, null);
			return;
		}
		if ("ASK_HOW".equals(type)) {
			if (mMXNaviInfo == null) {
				RecorderWin.close();
				return;
			}

			int index = mMXNaviInfo.turnId;
			if (index == -1) {
				RecorderWin.close();
				return;
			}

			String dirTxt = MX_DIRECTION[0];
			if (index == 32) {
				dirTxt = MX_DIRECTION[MX_DIRECTION.length];
			} else {
				if (index < MX_DIRECTION.length) {
					dirTxt = MX_DIRECTION[index];
				}
			}

			long remainDistance = mMXNaviInfo.turnDistance;
			String distance = "";
			if (remainDistance > 1000) {
				distance = (Math.round(remainDistance / 100.0) / 10.0) + "公里";
			} else if (remainDistance > 0) {
				distance = remainDistance + "米";
			} else {
				RecorderWin.close();
				return;
			}

			String nextRoad = mMXNaviInfo.nextName;
			String hint = "前方" + distance + dirTxt;
			if (!TextUtils.isEmpty(nextRoad)) {
				hint += ",进入" + nextRoad;
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, null);
			return;
		}
		if ("EXIT_NAV".equals(type)) {
			if (!isWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("将为您退出导航", new Runnable() {
					@Override
					public void run() {
						exitNav();
					}
				});
				return;
			}
			if (mWinConfirmWithAsr != null && mWinConfirmWithAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mWinConfirmWithAsr = new WinConfirmAsr() {
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText("好的，将为您退出导航", new ITtsCallback() {
								@Override
								public void onEnd() {
									exitNav();
								};
							});
						}
					}.setSureText("确定", new String[] { "确定", "退出" }).setCancelText("取消", new String[] { "取消" })
							.setHintTts("确定要退出导航吗？").setMessage("确定要退出导航吗？");
					mWinConfirmWithAsr.show();
				}
			}, 0);
			return;
		}

		if ("VIEW_ALL".equals(type)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose("将为您" + command, new Runnable() {

				@Override
				public void run() {
					sendCommandToNav(85, null);
				}
			});
			return;
		}
		if ("TUIJIANLUXIAN".equals(type)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					mWinConfirmAsr = new WinConfirmAsr() {

						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText("好的，将为您重新规划系统推荐路线", new ITtsCallback() {

								public void onSuccess() {
									sendCommandToNav(86, null);
								}
							});
						}
					}.setSureText("确定", new String[] { "确定" }).setCancelText("取消", new String[] { "取消" })
							.setHintTts("确定要重新规划系统推荐路线吗？").setMessage("确定要重新规划系统推荐路线吗？");

					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}
		if ("ZUIKUAILUXIAN".equals(type)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					String hint = "躲避拥堵";
					if ("最快路线".equals(command)) {
						hint = "最快";
					}

					final String ttsTxt = hint;

					mWinConfirmAsr = new WinConfirmAsr() {

						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText("好的，将为您重新规划" + ttsTxt + "路线", new ITtsCallback() {
								public void onSuccess() {
									sendCommandToNav(87, null);
								}
							});
						}
					}.setSureText("确定", new String[] { "确定" }).setCancelText("取消", new String[] { "取消" })
							.setHintTts("确定要重新规划" + ttsTxt + "路线吗？").setMessage("确定要重新规划" + ttsTxt + "路线吗？");
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}
		if ("ZUIDUANLUCHENG".equals(type)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					String hint = "少路程";
					if ("最短路线".equals(command)) {
						hint = "最短";
					}

					final String ttsTxt = hint;

					mWinConfirmAsr = new WinConfirmAsr() {

						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText("好的，将为您重新规划" + ttsTxt + "路线", new ITtsCallback() {

								public void onSuccess() {
									sendCommandToNav(88, null);
								}
							});
						}
					}.setSureText("确定", new String[] { "确定" }).setCancelText("取消", new String[] { "取消" })
							.setHintTts("确定要重新规划" + ttsTxt + "路线吗？").setMessage("确定要重新规划" + ttsTxt + "路线吗？");
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}
		if ("JINGJI".equals(type)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					mWinConfirmAsr = new WinConfirmAsr() {

						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText("好的，将为您重新规划经济路线", new ITtsCallback() {

								public void onSuccess() {
									sendCommandToNav(89, null);
								}
							});
						}
					}.setSureText("确定", new String[] { "确定" }).setCancelText("取消", new String[] { "取消" })
							.setHintTts("确定要重新规划经济路线吗？").setMessage("确定要重新规划经济路线吗？");
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if (RecorderWin.isOpened()) {
			RecorderWin.close();
		}
	}
}