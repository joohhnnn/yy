package com.txznet.txz.component.nav.daodaotong;


import java.util.ArrayList;
import java.util.List;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZAsrManager.AsrComplexSelectCallback;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
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
import android.net.Uri;
import android.text.TextUtils;
/**
 * 
 * 道道通
 *
 */
public class NavDdtImpl extends NavThirdApp {
	
	public static final String DDT_PACKAGE_NAME = "cn.ritu.rtnavi";
	private static final String DDT_DATA_PRE = "rtnavi://mapOpera?"; // DATA = rtnavi://mapOpera?key=value
	private static final String DDT_ACTION = "android.intent.action.VIEW";
	private static final String DDT_CATEGORY = "android.intent.category.DEFAULT";
	
	boolean mHasRegistCmds;

	private WinConfirmAsr mWinConfirmWithAsr;
	
	public static float sAsrWakeupThresHold = -3.65f;
	
	//实时路况
	private static final String KEY_REAL_TIME = "realTimeTraffic"; 
	private static final int VALUE_REAL_TIME_OPEN = 1;
	private static final int VALUE_REAL_TIME_CLOSE = 0;
	
	//切换路线
	private static final String KEY_ROUTE_TYPE = "changeRouteType";
	private static final int VALUE_ROUTE_TYPE_RECOMMEND = 0; // 推荐
	private static final int VALUE_ROUTE_TYPE_FAST = 1; // 高速
	private static final int VALUE_ROUTE_TYPE_CHEAP = 2; // 经济
	private static final int VALUE_ROUTE_TYPE_SHORT = 3; // 最短
	
	//查看地址薄
	private static final String KEY_VIEW_ADDR = "viewAddressBook"; 
	private static final int VALUE_VIEW_ADDR_DEFAULT = 1;
	
	//查看历史记录
	private static final String KEY_VIEW_HISTORY = "viewHistory"; 
	private static final int VALUE_VIEW_HISTORY_DEFAULT = 1;
	
	//回家回单位
	private static final String KEY_COMMON_POI = "goCommonPoint"; 
	private static final int VALUE_COMMON_POI_HOME = 0;
	private static final int VALUE_COMMON_POI_COMPANY = 1;
	
	//查看预览路线
	private static final String KEY_VIEW_ROUTE = "viewRoute";
	private static final int VALUE__VIEW_ROUTE_DEFAULT = 1;
	
	//返回地图 ???
	private static final String KEY_GOTO_MAP = "gotoMap";
	private static final int VALUE_GOTO_MAP_DEFAULT = 1;
	
	//打开关闭鹰眼模式
	private static final String KEY_EAGLE_VIEW = "eagleView";
	private static final int VALUE_EAGLE_VIEW_CLOSE = 0;
	private static final int VALUE_EAGLE_VIEW_OPEN = 1;
	
	//打开关闭双屏
	private static final String KEY_DOUBLE_SCREEN = "doubleScreen";
	private static final int VALUE_DOUBLE_SCREEN_CLOSE = 0;
	private static final int VALUE_DOUBLE_SCREEN_OPEN = 1;
	
	//查看地址详情
	private static final String KEY_VIEW_DETAIL = "viewDetail";
	private static final int VALUE_VIEW_DETAIL_DEFAULT = 1;
	
	//地图模式切换
	private static final String KEY_CHANGE_VIEW_MODE = "changeViewMode";
	private static final int VALUE_CHANGE_VIEW_MODE_DEFAULT = 1;
	
	private static final String KEY_SCALE_MAP = "scaleMap";
	private static final int VALUE_SCALE_ZOOM_IN = 1;
	private static final int VALUE_SCALE_ZOOM_OUT = 0;
	
	//询问和播报上次语音
	private static final String KEY_BROADCAST_AGAIN = "broadcastAgain";
	private static final int VALUE_BROADCAST_AGAIN_DEFAULT = 1;
	
	//下一服务区距离播报
	private static final String KEY_NEXT_SERVICE = "nextService";
	private static final int VALUE_NEXT_SERVICE_DEFAULT = 1;
	 
	//语音翻页
	private static final String KEY_CHANGE_PAGE = "changePage";
	private static final int VALUE_CHANGE_PAGE_PRE = 0;
	private static final int VALUE_CHANGE_PAGE_NEXT = 1;
	
	//语音选择
	private static final String KEY_SELECT_ITEM = "selectItem";
	
	public NavDdtImpl() {
		super();
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("cn.ritu.rtnavi");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String type = intent.getStringExtra("type");
				LogUtil.logd("NavDdtImpl >> onReceive type "+type);
				if("showNavi".equals(type)||"showNav".equals(type)){
					if(intent.getIntExtra("action", -1)==1){
						regNavUiCommands();
					}else if (intent.getIntExtra("action", -1)==0) {
						unregNavUiCommands();
					}else {
						if(isAppOnTop()){
							regNavUiCommands();
						}else {
							unregNavUiCommands();
						}
					}
				}else if ("viewAddressBook".equals(type)) {
					final int count = intent.getIntExtra("count", 0);
					String hint = NativeData.getResString("RS_MAP_ADDRESS_OPEN");
					if(count>1){
						hint+= NativeData.getResString("RS_MAP_SELECT_PAGES");
					}
					TtsManager.getInstance().speakText(hint, new TtsUtil.ITtsCallback() {
						public void onSuccess() {
							regSelectorCmd(count);
						}
					});			
				}else if ("hideAddressBook".equals(type)) {
					unregSelectorCmd();
				}else if ("viewHistory".equals(type)) {
					final int count = intent.getIntExtra("count", 0);
					String hint = NativeData.getResString("RS_MAP_HISTORY_OPEN");
					if(count>1){
						hint+= NativeData.getResString("RS_MAP_SELECT_PAGES");
					}
					TtsManager.getInstance().speakText(hint, new TtsUtil.ITtsCallback() {
						public void onSuccess() {
							regSelectorCmd(count);
						}
					});		
				}else if ("hideHistory".equals(type)) {
					unregSelectorCmd();
				}else if ("updateHome".equals(type)) {
					Poi poi = getPoiFromMsg(intent.getStringExtra("location"), "家");
					if(poi!=null){
						TXZNavManager.getInstance().setHomeLocation(poi);
					}
				}else if ("updateCompany".equals(type)) {
					Poi poi = getPoiFromMsg(intent.getStringExtra("location"), "公司");
					if(poi!=null){
						TXZNavManager.getInstance().setHomeLocation(poi);
					}
				}
			}
		}, iFilter);

		LogUtil.logd("NavMXImpl >>isAppTop:" + isAppOnTop());
		if (isAppOnTop()) {
			regNavUiCommands();
		}
	}

	
	@Override
	public void enterNav() {
		sendCmdToDdt(KEY_GOTO_MAP, VALUE_GOTO_MAP_DEFAULT);
	}


	private com.txznet.sdk.bean.Poi getPoiFromMsg(String msg,String defaultName) {
		com.txznet.sdk.bean.Poi poi = null;
		if (msg != null && !"".equals(msg)) {
			String[] addrInfos = msg.split("@");
			if (!TextUtils.isEmpty(addrInfos[1])
					&& !TextUtils.isEmpty(addrInfos[2])) {
				poi = new com.txznet.sdk.bean.Poi();
				poi.setLng(Float.parseFloat(addrInfos[0]));
				poi.setLat(Float.parseFloat(addrInfos[1]));
				poi.setDistance(0);
				poi.setCity("");
				poi.setName(TextUtils.isEmpty(addrInfos[0])?defaultName:addrInfos[0]);
				poi.setGeoinfo(TextUtils.isEmpty(addrInfos[0])?defaultName:addrInfos[0]);
			}
		}
		return poi;
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
	public String getPackageName() {
		return DDT_PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		LogUtil.logd("NavDdtImpl >>  NavigateTo");
		int route = VALUE_ROUTE_TYPE_RECOMMEND;
		if (plan == NavPlanType.NAV_PLAN_TYPE_RECOMMEND) {
			route = VALUE_ROUTE_TYPE_RECOMMEND;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_COST) {
			route = VALUE_ROUTE_TYPE_CHEAP;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_DISTANCE) {
			route = VALUE_ROUTE_TYPE_SHORT;
		} else if (plan == NavPlanType.NAV_PLAN_TYPE_LEAST_TIME) {
			route = VALUE_ROUTE_TYPE_FAST;
		}
		if(info==null||info.msgGpsInfo==null){
			return false;
		}
		
		Intent intent = new Intent();
		intent.setAction(DDT_ACTION);
		intent.addCategory(DDT_CATEGORY);
		intent.setData(Uri.parse("rtnavi://navi?poiname="+info.strTargetName+"&lat="+info.msgGpsInfo.dblLat+"&lon="+info.msgGpsInfo.dblLng));
		intent.setPackage(DDT_PACKAGE_NAME);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogUtil.logd("NavigateTo "+ "rtnavi://navi?poiname="+info.strTargetName+"&lat="+info.msgGpsInfo.dblLat+"&lon="+info.msgGpsInfo.dblLng);
		return true;
	}

	@Override
	public void exitNav() {
		dismissDialog();
		unregNavUiCommands();
		Intent intent = new Intent("cn.ritu.rtnavi.exit");
		GlobalContext.get().sendBroadcast(intent);
		super.exitNav();
	}

	
	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		Intent intent = new Intent();
		String location = navigateInfo.strTargetName+"@"+navigateInfo.msgGpsInfo.dblLat+
				"@"+navigateInfo.msgGpsInfo.dblLng;
		intent.setAction("cn.ritu.rtnavi.txz");
		intent.putExtra("type", "updateCompany");
		intent.putExtra("location", location);
		JNIHelper.logd("NavDdtImpl updateCompanyLocation type updateCompany location "+location);
		GlobalContext.get().sendBroadcast(intent);
	}


	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		Intent intent = new Intent();
		String location = navigateInfo.strTargetName+"@"+navigateInfo.msgGpsInfo.dblLat+
				"@"+navigateInfo.msgGpsInfo.dblLng;
		intent.setAction("cn.ritu.rtnavi.txz");
		intent.putExtra("type", "updateHome");
		intent.putExtra("location", location);
		JNIHelper.logd("NavDdtImpl updateHomeLocation type updateHome location "+location);
		GlobalContext.get().sendBroadcast(intent);
	}


	private void regNavUiCommands() {
		LogUtil.logd("NavDdtImpl regNavUiCommands");
		WakeupManager.getInstance().setWakeupThreshhold(sAsrWakeupThresHold);
		WakeupManager.getInstance().useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {

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
				if (!com.txznet.txz.module.app.PackageManager.getInstance().isAppRunning(getPackageName())) {
					unregNavUiCommands();
					return;
				}
				invokeNavCommand(type);
			}
		}.addCommand("OPEN_REAL_TIME", "打开实时路况", "显示实时路况", "开启实时路况")
				.addCommand("CLOSE_REAL_TIME", "关闭实时路况","退出实时路况","关闭实时路况")
				.addCommand("ROUTE_RECOMMAND", "切换到推荐路线","推荐路线").addCommand("ROUTE_FAST", "切换到高速路线","高速路线")
				.addCommand("ROUTE_CHEAP", "切换到经济路线","经济路线").addCommand("ROUTE_SHORT", "切换到最短路线","最短路线")
				.addCommand("VIEW_ADDR", "查看地址簿", "打开我的地址", "地址簿","选择地址","查看保存的地址")
				.addCommand("VIEW_HISTORY", "历史记录","打开历史记录","查看历史记录")
				.addCommand("ZOOM_IN", "放大","地图放大","放大地图").addCommand("ZOOM_OUT", "缩小","地图缩小","缩小地图")
				.addCommand("EXIT_NAV", "退出导航", "放弃导航", "关闭导航", "离开导航")
				.addCommand("VIEW_ROUTE", "查看经过道路","查看路线","查看全程","经过道路")
				.addCommand("EAGLE_VIEW_OPEN", "打开鹰眼模式")
				.addCommand("EAGLE_VIEW_CLOSE", "关闭鹰眼模式")
				.addCommand("DOUBLE_SCREEN_OPEN", "打开双屏","打开双屏模式")
				.addCommand("DOUBLE_SCREEN_CLOSE", "关闭双屏","关闭双屏模式")
				.addCommand("VIEW_DETAIL", "查看地址详情","查看详情","打开详情")
				.addCommand("CHANGE_VIEW_MODE", "切换地图模式","切换显示方式","切换视图")
				.addCommand("BROOADCAST_AGAIN", "再说一次","重新播报","我没听清楚")
				.addCommand("NEXT_SERVICE", "距离下一服务区多远","服务区还有多远","前方服务区距离")
				.addCommand("GO_HOME", "回家","我要回家")
				.addCommand("GO_COMPANY", "回单位","单位","去单位","公司","去公司")
				);

		
		mHasRegistCmds = true;
	}
	
	private void invokeNavCommand(String command){
		if ("EXIT_NAV".equals(command)) {
			JSONBuilder json = new JSONBuilder();
			json.put("sence", "nav");
			json.put("text", "退出导航");
			// json.put("keywords", keywords);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}
		if("OPEN_REAL_TIME".equals(command)){
			String openRealtime = NativeData
					.getResString("RS_MAP_OPEN_REALTIME");
			String text = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", openRealtime);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_REAL_TIME, VALUE_REAL_TIME_OPEN);
					RecorderWin.close();
				}
			});
		}else if ("CLOSE_REAL_TIME".equals(command)) {
			String closeRealtime = NativeData
					.getResString("RS_MAP_CLOSE_REALTIME");
			String text = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", closeRealtime);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_REAL_TIME, VALUE_REAL_TIME_CLOSE);
					RecorderWin.close();
				}
			});
		}else if ("ROUTE_RECOMMAND".equals(command)) {
			String recommend = NativeData
					.getResString("RS_MAP_SWITCH_RECOMMEND");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND")
					.replace("%CMD%", recommend);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_ROUTE_TYPE, VALUE_ROUTE_TYPE_RECOMMEND);
					RecorderWin.close();
				}
			});
		}else if ("ROUTE_FAST".equals(command)) {
			String fast = NativeData.getResString("RS_MAP_SWITCH_FAST");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", fast);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_ROUTE_TYPE, VALUE_ROUTE_TYPE_FAST);
					RecorderWin.close();
				}
			});
		}else if ("ROUTE_CHEAP".equals(command)) {
			String cheap = NativeData.getResString("RS_MAP_SWITCH_CHEAP");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", cheap);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_ROUTE_TYPE, VALUE_ROUTE_TYPE_CHEAP);
					RecorderWin.close();
				}
			});
		}else if ("ROUTE_SHORT".equals(command)) {
			String shortest = NativeData.getResString("RS_MAP_SWITCH_SHORT");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", shortest);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_ROUTE_TYPE, VALUE_ROUTE_TYPE_SHORT);
					RecorderWin.close();
				}
			});
		}else if ("VIEW_ADDR".equals(command)) {
			String address = NativeData.getResString("RS_MAP_OPEN_ADDRESS");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", address);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_VIEW_ADDR, VALUE_VIEW_ADDR_DEFAULT);
					RecorderWin.close();
				}
			});
		}else if ("VIEW_HISTORY".equals(command)) {
			String history = NativeData.getResString("RS_MAP_OPEN_HISTORY");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", history);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_VIEW_HISTORY, VALUE_VIEW_HISTORY_DEFAULT);
					RecorderWin.close();
				}
			});
		}else if ("GO_HOME".equals(command)) {
			String home = NativeData.getResString("RS_MAP_PATH_HOME");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", home);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_COMMON_POI, VALUE_COMMON_POI_HOME);
					RecorderWin.close();
				}
			});			
		}else if ("GO_COMPANY".equals(command)) {
			String company = NativeData.getResString("RS_MAP_PATH_COMPANY");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", company);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_COMMON_POI, VALUE_COMMON_POI_COMPANY);
					RecorderWin.close();
				}
			});		
		}else if ("ZOOM_IN".equals(command)) {
			String zoomIn = NativeData.getResString("RS_MAP_ZOOMIN");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", zoomIn);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					sendCmdToDdt(KEY_SCALE_MAP, VALUE_SCALE_ZOOM_IN);
					RecorderWin.close();
				}
			});
		}else if ("ZOOM_OUT".equals(command)) {
			String zoomOut = NativeData.getResString("RS_MAP_ZOOMOUT");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", zoomOut);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					RecorderWin.close();
					AppLogic.runOnUiGround(new Runnable() {
						
						@Override
						public void run() {
							AppLogicBase.runOnUiGround(new Runnable() {
								
								@Override
								public void run() {
									sendCmdToDdt(KEY_SCALE_MAP, VALUE_SCALE_ZOOM_OUT);
								}
							}, 0);					
						}
					}, 0);
				}
			});
		}else if ("EXIT_NAV".equals(command)) {
			String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
			String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", exitNav);
			TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
				public void onSuccess() {
					//TODO 退出导航
					//sendCmdToDdt(KEY_VIEW_ADDR, VALUE_VIEW_ADDR_DEFAULT);
					RecorderWin.close();
					exitNav();
				}
			});
		}else if ("VIEW_ROUTE".equals(command)) {
			String viewRoute = NativeData.getResString("RS_MAP_VIEW_ROUTE");
			TtsManager.getInstance().speakText(viewRoute,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_VIEW_ROUTE, VALUE__VIEW_ROUTE_DEFAULT);
				}		
			});
		}else if ("EAGLE_VIEW_OPEN".equals(command)) {
			String open = NativeData.getResString("RS_MAP_OPEN_EAGLE_EYE");
			String spk = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", open);
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_EAGLE_VIEW, VALUE_EAGLE_VIEW_OPEN);
				}		
			});
		}else if ("EAGLE_VIEW_CLOSE".equals(command)) {
			String close = NativeData.getResString("RS_MAP_CLOSE_EAGLE_EYE");
			String spk = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", close);
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_EAGLE_VIEW, VALUE_EAGLE_VIEW_CLOSE);
				}		
			});
		}else if ("DOUBLE_SCREEN_OPEN".equals(command)) {
			String open = NativeData.getResString("RS_MAP_OPEN_DOUBLE_SCREEN");
			String spk = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", open);
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_DOUBLE_SCREEN, VALUE_DOUBLE_SCREEN_OPEN);
				}		
			});
		}else if ("DOUBLE_SCREEN_CLOSE".equals(command)) {
			String close = NativeData.getResString("RS_MAP_CLOSE_DOUBLE_SCREEN");
			String spk = NativeData.getResPlaceholderString(
					"RS_VOICE_WILL_DO_COMMAND", "%CMD%", close);
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_DOUBLE_SCREEN, VALUE_DOUBLE_SCREEN_CLOSE);
				}		
			});
		}else if ("VIEW_DETAIL".equals(command)) {
			String spk = NativeData.getResString("RS_MAP_ADDRSS_DETAIL");
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_VIEW_DETAIL, VALUE_VIEW_DETAIL_DEFAULT);
				}		
			});
		}else if ("CHANGE_VIEW_MODE".equals(command)) {
			String spk = NativeData.getResString("RS_MAP_SWITCH_MODEL");
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_CHANGE_VIEW_MODE, VALUE_CHANGE_VIEW_MODE_DEFAULT);
				}		
			});
		}else if ("BROOADCAST_AGAIN".equals(command)) {
			String spk = NativeData.getResString("RS_MAP_BROADCAST_AGAIN");
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_BROADCAST_AGAIN, VALUE_BROADCAST_AGAIN_DEFAULT);
				}		
			});
		}else if ("NEXT_SERVICE".equals(command)) {
			String spk = NativeData.getResString("RS_MAP_NEXT_SERVICE");
			TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
				@Override
				public void onEnd() {
					RecorderWin.close();
					sendCmdToDdt(KEY_NEXT_SERVICE, VALUE_NEXT_SERVICE_DEFAULT);
				}		
			});
		}
		LogUtil.logd("NavDdtImpl  invokeNavCommand command>>"+command);;
	}
	
	private void unregNavUiCommands(){
		LogUtil.logd("NavDdtImpl  unregNavUiCommands");
		WakeupManager.getInstance().recoverWakeupFromAsr("task_ddt_selector");
		WakeupManager.getInstance().recoverWakeupFromAsr("NAV_CTRL#" + getPackageName());
	}
	
	
	private void regSelectorCmd(int count){
		if(count<=0){
			return;
		}
		AsrComplexSelectCallback complexSelectCallback = new AsrComplexSelectCallback() {
			
			@Override
			public boolean needAsrState() {
				
				return false;
			}
			
			@Override
			public String getTaskId() {
				
				return "task_ddt_selector";
			}

			@Override
			public void onCommandSelected(String type, String command) {
				if(type.equals("CMD_TYPE_DDT_CHOICE")){
					int index = Integer.parseInt((command.replace("第", "")).replace("个", ""));
					sendCmdToDdt(KEY_SELECT_ITEM, index);
					RecorderWin.close();
				}else if (type.equals("CMD_TYPE_PAGE_CHOICE")) {
					if(command.equals("上一页")){
						RecorderWin.close();
						sendCmdToDdt(KEY_CHANGE_PAGE, VALUE_CHANGE_PAGE_PRE);
					}else if (command.equals("下一页")) {
						RecorderWin.close();
						sendCmdToDdt(KEY_CHANGE_PAGE, VALUE_CHANGE_PAGE_NEXT);
					}
				}
				super.onCommandSelected(type, command);
			}

		};
		List<String> wakeUpCmds = new ArrayList<String>();
		for(int i=0;i<count;i++){
			wakeUpCmds.add("第"+(i+1)+"个");
		}
		complexSelectCallback.addCommand("CMD_TYPE_PAGE_CHOICE", "上一页", "下一页")
			                .addCommand("CMD_TYPE_DDT_CHOICE", wakeUpCmds.toArray(new String[]{}));
		WakeupManager.getInstance().useWakeupAsAsr(complexSelectCallback);
	}
	
	private void unregSelectorCmd(){
		WakeupManager.getInstance().recoverWakeupFromAsr("task_ddt_selector");
	}
	
	private Intent msgIntent = null;
	private void sendCmdToDdt(String key,int value){
		if(msgIntent==null){
			msgIntent = new Intent();
			msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			msgIntent.setAction("android.intent.action.VIEW");
			msgIntent.addCategory("android.intent.category.DEFAULT");
			msgIntent.setPackage(getPackageName());
		}
		msgIntent.setData(Uri.parse(DDT_DATA_PRE+key+"="+value));
		try {
			GlobalContext.get().startActivity(msgIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		JNIHelper.logd("NavDdtImpl sendCmdToDdt >>"+DDT_DATA_PRE+key+"="+value);
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

	
}
