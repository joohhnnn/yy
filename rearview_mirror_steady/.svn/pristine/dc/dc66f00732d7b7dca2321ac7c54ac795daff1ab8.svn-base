package com.txznet.txz.component.nav.kgo;

import android.content.Intent;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.kgo.internal.KgoBroadcastManager;
import com.txznet.txz.component.nav.kgo.internal.KgoBroadcastManager.OnNavStatusUpdateListener;
import com.txznet.txz.component.nav.kgo.internal.KgoBroadcastSender;
import com.txznet.txz.component.nav.kgo.internal.KgoKeyConstants;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import java.util.ArrayList;
import java.util.List;

public class NavKgoImpl extends NavThirdComplexApp {
	private String mRealNavPkn = null;

	private OnNavStatusUpdateListener mListener = new OnNavStatusUpdateListener() {

		@Override
		public void onNavChange(boolean isInNav) {
			if (isInNav) {
				onStart();
			} else {
				onEnd(false);
			}
		}

		@Override
		public void onFocusChange(boolean isFocus) {
			if (isFocus) {
				onResume();
			} else {
				onPause();
			}
		}

		@Override
		public void onAppExit() {
			onExitApp();
		}

		@Override
		public void onNavInfoUpdate(NavInfo navInfo) {
			mNavInfo = navInfo;
			if (navInfo != null) {
				broadNaviInfo(navInfo.toJson());
			}
		}

		@Override
		public void onMapVersionUpdate(String version, String channel) {
			LogUtil.logd("NavKgoImpl:" + version + ",channel:" + channel);
		}

		@Override
		public void onNavPathUpdate(PathInfo pathInfo) {
			mPathInfo = pathInfo;
		}

		@Override
		public void onNavConfirmDialogUpdate(int state) {
			// 0为对话框显示，1为选择继续，2为选择取消
			if (state == 0) {
				WakeupManager.getInstance().useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
					@Override
					public boolean needAsrState() {
						return false;
					}

					@Override
					public String getTaskId() {
						return "TASK_KGO_DIALOG";
					}

					@Override
					public void onCommandSelected(String type, String command) {
						super.onCommandSelected(type, command);
						if(RecorderWin.isOpened()){
							//增加在声控界面中说取消/确定后没有关闭声控界面的逻辑
							String spk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE");
							if ("TYPE_CONTINUE_NAV".equals(type)) {
								AsrManager.getInstance().setNeedCloseRecord(true);
								RecorderWin.speakTextWithClose(spk, new Runnable() {
									@Override
									public void run() {
										sendSelectDialog(true);
									}
								});
							} else if ("TYPE_CANCEL_NAV".equals(type)) {
								AsrManager.getInstance().setNeedCloseRecord(true);
								RecorderWin.speakTextWithClose(spk, new Runnable() {
									@Override
									public void run() {
										sendSelectDialog(false);
									}
								});
							}
						}else{
							if ("TYPE_CONTINUE_NAV".equals(type)) {
								sendSelectDialog(true);
							} else if ("TYPE_CANCEL_NAV".equals(type)) {
								sendSelectDialog(false);
							}
						}
					}
				}.addCommand("TYPE_CONTINUE_NAV", "确定")
						.addCommand("TYPE_CANCEL_NAV", "取消"));
			} else {
				WakeupManager.getInstance().recoverWakeupFromAsr("TASK_KGO_DIALOG");
			}
		}
	};

	private void sendSelectDialog(boolean isSure) {
		int val = 0;
		if (isSure) {
			val = 1;
		} else {
			val = 2;
		}
		Intent intent = new Intent();
		intent.setAction("CLDNAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10049);
		intent.putExtra("EXTRA_ENDURANCE_TYPE", val);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	@Override
	public int initialize(IInitCallback oRun) {
		if (isReachable()) {
			KgoBroadcastManager.getInstance().init(GlobalContext.get());
			KgoBroadcastManager.getInstance().assignParent(this);
			KgoBroadcastManager.getInstance().addNavStatusUpdateListener(mListener);
			checkNavStatus();
		} else {
			if (oRun != null) {
				oRun.onInit(false);
			}
			return 0;
		}
		return super.initialize(oRun);
	}
	
	private void checkNavStatus() {
		AppLogic.removeBackGroundCallback(mCheckFocusTask);
		AppLogic.runOnBackGround(mCheckFocusTask, 0);
		AppLogic.removeBackGroundCallback(mCheckInNavTask);
		AppLogic.runOnBackGround(mCheckInNavTask, 2000);
	}
	
	Runnable mCheckFocusTask = new Runnable() {
		
		@Override
		public void run() {
			KgoBroadcastSender.getInstance().queryIsInFocus();
		}
	};
	
	Runnable mCheckInNavTask = new Runnable() {
		
		@Override
		public void run() {
			KgoBroadcastSender.getInstance().queryIsInNav();
		}
	};
	
	@Override
	public void enterNav() {
		KgoBroadcastSender.getInstance().openNav();
	}
	
	@Override
	public void exitNav() {
		// 先取消当前路径，1s后关闭程序
		AppLogic.removeBackGroundCallback(cancelNav);
		AppLogic.removeBackGroundCallback(exitNav);
		AppLogic.runOnBackGround(cancelNav, 0);
		AppLogic.runOnBackGround(exitNav, 1000);
	}
	
	Runnable cancelNav = new Runnable() {
		
		@Override
		public void run() {
			KgoBroadcastSender.getInstance().cancelNav();
		}
	};
	
	Runnable exitNav = new Runnable() {
		
		@Override
		public void run() {
			KgoBroadcastSender.getInstance().exitNav();
			onExitApp();
		}
	};
	
	@Override
	public boolean isInNav() {
		return mIsStarted;
	}
	
	@Override
	public void handleIntent(Intent intent) {
		KgoBroadcastManager.getInstance().handle(intent);
	}
	
	@Override
	public void onNavCommand(boolean fromWakeup, String cmd, String speech) {
		if (AsrKeyType.EXIT_NAV.equals(cmd) || AsrKeyType.CLOSE_MAP.equals(cmd)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", speech);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}
		LogUtil.logd("onNavCommSelect:[" + fromWakeup + "," + cmd + "," + speech + "," + getPackageName() + "]");
		
		if (AsrKeyType.ZOOM_IN.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_ZOOMIN", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance().zoomMap(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.ZOOM_OUT.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_ZOOMOUT", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance().zoomMap(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.NIGHT_MODE.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_NIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setNavDayNightMode(KgoKeyConstants.ACTION_STATUS_TYPE.DAY_NIGHT.MODE_NIGHT);
				}
			}, false);
			return;
		}
		if (AsrKeyType.LIGHT_MODE.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_LIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setNavDayNightMode(KgoKeyConstants.ACTION_STATUS_TYPE.DAY_NIGHT.MODE_DAY);
				}
			}, false);
			return;
		}
		if (AsrKeyType.AUTO_MODE.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_AUTO_MODE", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setNavDayNightMode(KgoKeyConstants.ACTION_STATUS_TYPE.DAY_NIGHT.MODE_AUTO);
				}
			}, false);
			return;
		}
		if (AsrKeyType.VIEW_ALL.equals(cmd)) {
			String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
//			tts = tts.replace("%COMMAND%", speech);
			Runnable task = new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance().zoomAllMap(true);
				}
			};

			if (fromWakeup) {
				task.run();
//				tts = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, null);
			} else {
//				tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, task);
			}
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(cmd)) {
			doRePlanWakeup(cmd, speech, "RS_MAP_DUOBIYONGDU", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.changeRoute(KgoKeyConstants.ACTION_STATUS_TYPE.CHANGE_ROUTE.PLAN_DUOBIYONGDU);
					JNIHelper.logd("NavKgoImpl start DUOBIYONGDU");
				}
			});
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(cmd)) {
			doRePlanWakeup(cmd, speech, "RS_MAP_BUZOUGAOSU", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
					.changeRoute(KgoKeyConstants.ACTION_STATUS_TYPE.CHANGE_ROUTE.PLAN_BUZOUGAOSU);
					JNIHelper.logd("NavKgoImpl start BUZOUGAOSU");
				}
			});
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(cmd)) {
			doRePlanWakeup(cmd, speech, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
					.changeRoute(KgoKeyConstants.ACTION_STATUS_TYPE.CHANGE_ROUTE.PLAN_GAOSUYOUXIAN);
					JNIHelper.logd("NavKgoImpl start GAOSUYOUXIAN");
				}
			});
			return;
		}
		if (AsrKeyType.LESS_MONEY.equals(cmd)) {
			doRePlanWakeup(cmd, speech, "RS_MAP_LESS_MONEY", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.changeRoute(KgoKeyConstants.ACTION_STATUS_TYPE.CHANGE_ROUTE.PLAN_LESS_MONEY);
					JNIHelper.logd("NavKgoImpl start LESS_MONEY");
				}
			});
			return;
		}
		if (AsrKeyType.BACK_NAVI.equals(cmd)) {
			KgoBroadcastSender.getInstance().zoomAllMap(false);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_KGO_BACK_NAV_OK").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					sendSelectDialog(true);//客户要求：增加一个新逻辑，继续导航时也会尝试去关掉导航弹窗
				}
			});
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(cmd)) {
			speakHowNavi(fromWakeup);
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(cmd)) {
			speakAskRemain(fromWakeup);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(cmd)) {
			if (!fromWakeup) {
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						startNavByInner();
					}
				});
				return;
			}
			startNavByInner();
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance().setTrafficStatus(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance().setTrafficStatus(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.THREE_MODE.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_THREE_MODE", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setMapViewMode(KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_VIEWMODE_3D);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CAR_DIRECT.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_CAR_DIRECT", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setMapViewMode(KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_VIEWMODE_CAR);
				}
			}, false);
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(cmd)) {
			doConfirmShow(cmd, speech, "RS_MAP_NORTH_DIRECT", new Runnable() {

				@Override
				public void run() {
					KgoBroadcastSender.getInstance()
							.setMapViewMode(KgoKeyConstants.ACTION_STATUS_TYPE.MAP_CONTROL.OPERA_VIEWMODE_NORTH);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CANCEL_NAV.equals(cmd)) {
			if (!fromWakeup && !isInNav()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doExitConfirm(cmd,
					isInNav() ? NativeData.getResString("RS_MAP_NAV_STOP") : NativeData.getResString("RS_MAP_NAV_EXIT"),
					new Runnable() {

						@Override
						public void run() {
							if (!isInNav()) {
								NavManager.getInstance().exitAllNavTool();
								return;
							}
							// 取消路径
							KgoBroadcastSender.getInstance().cancelNav();
						}
					});
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(cmd) || AsrKeyType.CLOSE_MAP.equals(cmd)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			if (!enableWakeupExitNav) {
				return;
			}
			doExitConfirm(cmd, NativeData.getResString("RS_MAP_NAV_EXIT"), new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}
		speakNoSupport();//当前限速
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		if (info != null && info.msgGpsInfo != null) {
			final String name = info.strTargetName;
			final double lat = info.msgGpsInfo.dblLat;
			final double lng = info.msgGpsInfo.dblLng;
			if (mDestinationPoi == null) {
				mDestinationPoi = new Poi();
			}
			mDestinationPoi.setName(name);
			mDestinationPoi.setLat(lat);
			mDestinationPoi.setLng(lng);
			KgoBroadcastSender.getInstance().navigateTo(name, lat, lng);
			return true;
		}
		return false;
	}
	
	@Override
	public void startNavByInner() {
		KgoBroadcastSender.getInstance().startNav();
	}

	@Override
	public List<String> getBanCmds() {
		return null;
	}
	
	@Override
	public boolean speakLimitSpeech() {
		queryLimitSpeed();
		return true;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[]{
				AsrKeyType.ZOOM_IN, 
				AsrKeyType.ZOOM_OUT, 
				AsrKeyType.NIGHT_MODE, 
				AsrKeyType.LIGHT_MODE,
				AsrKeyType.AUTO_MODE, 
				AsrKeyType.EXIT_NAV, 
				AsrKeyType.CANCEL_NAV,
				AsrKeyType.CLOSE_MAP, 
				AsrKeyType.VIEW_ALL,
				AsrKeyType.DUOBIYONGDU, 
				AsrKeyType.BUZOUGAOSU, 
				AsrKeyType.GAOSUYOUXIAN, 
				AsrKeyType.LESS_MONEY,
				AsrKeyType.LESS_DISTANCE,
				AsrKeyType.HOW_NAVI, 
				AsrKeyType.ASK_REMAIN, 
				AsrKeyType.LIMIT_SPEED,
				AsrKeyType.BACK_NAVI, 
				AsrKeyType.START_NAVI, 
				AsrKeyType.OPEN_TRAFFIC, 
				AsrKeyType.CLOSE_TRAFFIC,
				AsrKeyType.THREE_MODE, 
				AsrKeyType.CAR_DIRECT, 
				AsrKeyType.NORTH_DIRECT
		};
	}
	
	@Override
	public List<String> getCmdNavOnly() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(AsrKeyType.HOW_NAVI);
		cmds.add(AsrKeyType.LIMIT_SPEED);
		cmds.add(AsrKeyType.ASK_REMAIN);
		cmds.add(AsrKeyType.DUOBIYONGDU);
		cmds.add(AsrKeyType.BUZOUGAOSU);
		cmds.add(AsrKeyType.GAOSUYOUXIAN);
		cmds.add(AsrKeyType.LESS_MONEY);
		return cmds;
	}
	
	private List<Poi> mTjPois;
	
	@Override
	public String disableProcJingYouPoi() {
		if (mTjPois != null && mTjPois.size() >= 2) {
			return NativeData.getResString("RS_MAP_POINT_TOO_MUCH");
		}
		return "";
	}
	
	@Override
	public boolean procJingYouPoi(Poi... pois) {
		super.procJingYouPoi(pois);
		if (mTjPois == null) {
			mTjPois = new ArrayList<Poi>();
		}

		Poi des = getDestinationPoi();
		if (des == null) {
			return false;
		}

		// 目的地发生改变的时候需要清空途经点
		mTjPois.add(pois[0]);
		KgoBroadcastSender.getInstance().naviWithWayPoi(mTjPois, des);

		return true;
	}

	@Override
	public String disableDeleteJingYou() {
		return "";
	}

	@Override
    public boolean deleteJingYou(Poi poi) {
        if (mTjPois != null && !mTjPois.isEmpty()) {
            mTjPois.remove(poi);
        }
        return super.deleteJingYou(poi);
    }

    Poi mDestinationPoi;
	public Poi getDestinationPoi() {
		// TODO 获取目的地
		return mDestinationPoi;
	}
	
	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		if (navigateInfo != null && navigateInfo.msgGpsInfo != null) {
			double lat = navigateInfo.msgGpsInfo.dblLat;
			double lng = navigateInfo.msgGpsInfo.dblLng;
			String name = navigateInfo.strTargetName;
			String address = navigateInfo.strTargetAddress;
			KgoBroadcastSender.getInstance().setHcAddress(true, lat, lng, name, address);
		}
	}
	
	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		if (navigateInfo != null && navigateInfo.msgGpsInfo != null) {
			double lat = navigateInfo.msgGpsInfo.dblLat;
			double lng = navigateInfo.msgGpsInfo.dblLng;
			String name = navigateInfo.strTargetName;
			String address = navigateInfo.strTargetAddress;
			KgoBroadcastSender.getInstance().setHcAddress(false, lat, lng, name, address);
		}
	}
	
	@Override
	public void setPackageName(String packageName) {
		LogUtil.logd("kgo set pkn:" + packageName);
		mRealNavPkn = packageName;
	}

	@Override
	public String getPackageName() {
		return mRealNavPkn;
	}
	
	@Override
	public void queryHomeCompanyAddr() {
		KgoBroadcastSender.getInstance().queryHCAddress(true);
		KgoBroadcastSender.getInstance().queryHCAddress(false);
	}

	public void speakNoSupport(){
		if(RecorderWin.isOpened()){
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE_2"),null);
		}
	}
}