package com.txznet.txz.module.ui;

import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.ui.WinManager.RecordInvokeAdapter;
import com.txznet.txz.ui.widget.SimAlertDialog;
import com.txznet.txz.ui.widget.SimAlertDialog.Builder;
import com.txznet.txz.util.SDCardUtil;

public class TXZUITester {
	
	/*
	 * 打开录音窗口
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type open
	 * 关闭录音窗口
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type close
	 * 更换录音窗口的录音状态，0为空闲状态，1为录音中，2为处理中
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type state --ei state 0 
	 * 测试音量，time为音量变化时间间隔，单位ms，0表示取消
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type volume --ei time 1000
	 * 
	 * 自动测试 true开始，false取消
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type autotest --ez start true
	 * adb shell am broadcast -a com.txznet.txz.test.ui --es type autotest --ez start false
	 */
	static Runnable mRunRefreshVol = new Runnable() {
		@Override
		public void run() {
			RecordInvokeAdapter adpter = WinManager.getInstance().getAdapter();
			adpter.refreshVolume(new Random().nextInt() % 100);
			
			if (RefreshVolTime > 0) {
				AppLogic.runOnBackGround(mRunRefreshVol, RefreshVolTime);
			}
		}
	};
	
	private static boolean isShow = false;
	private static int step = 0;
	private static boolean isTest = false;
	public static final String DATA_TEST_POI = "{\"titlefix\":\"世界之窗\",\"maxPage\":2,\"keywords\":\"世界之窗\",\"count\":4,\"poitype\":\"\",\"curPage\":0,\"prefix\":\"找到\",\"action\":\"nav\",\"aftfix\":\"的结果\",\"pois\":[{\"distance\":2425,\"geo\":\"深南大道9037号\",\"poitype\":1,\"source\":3,\"website\":\"www.szwwco.com\",\"name\":\"世界之窗\",\"lng\":113.975311,\"telephone\":\"0755-26608000;0755-82152156\",\"coordtype\":\"GCJ02\",\"lat\":22.535543,\"city\":\"深圳市\"},{\"distance\":2322,\"geo\":\"1号线\\/罗宝线;2号线\\/蛇口线\",\"poitype\":1,\"source\":3,\"website\":\"\",\"name\":\"世界之窗(地铁站)\",\"lng\":113.974407,\"telephone\":\"\",\"coordtype\":\"GCJ02\",\"lat\":22.536846,\"city\":\"深圳市\"},{\"distance\":2463,\"geo\":\"深南大道9037号世界之窗\",\"poitype\":1,\"source\":3,\"website\":\"\",\"name\":\"世界之窗(入口)\",\"lng\":113.975727,\"telephone\":\"\",\"coordtype\":\"GCJ02\",\"lat\":22.536087,\"city\":\"深圳市\"},{\"distance\":2362,\"geo\":\"深南大道9037-2号附近\",\"poitype\":1,\"source\":3,\"website\":\"\",\"name\":\"世界之窗(出口)\",\"lng\":113.97469,\"telephone\":\"\",\"coordtype\":\"GCJ02\",\"lat\":22.535485,\"city\":\"深圳市\"}],\"type\":2,\"city\":\"深圳市\"}";
	static Runnable mRunAutoTest = new Runnable() {
		@Override
		public void run() {
			LogUtil.logd("test: " + step + " isShow:" + isShow);
			AppLogic.removeUiGroundCallback(mRunAutoTest);
			if (!isTest) {
				WinManager.getInstance().getAdapter().dismiss();
				return;
			}
			if (!isShow) {
				WinManager.getInstance().getAdapter().show();
				isShow = true;
				step = 0;
			} else {
				if (step == 0) {
					WinManager.getInstance().getAdapter().addListMsg(DATA_TEST_POI);
					step = 1;
				} else if (step == 1) {
					WinManager.getInstance().getAdapter().addMsg(0, "有什么可以帮您");
					step = 2;
				} else if (step == 2) {
					WinManager.getInstance().getAdapter().dismiss();
					isShow = false;
					step = 0;
				}
			}
			if (isTest) {
				AppLogic.runOnUiGround(mRunAutoTest, 2000);
			}
		}
	};
	
	static int RefreshVolTime = 0;
	
	public static void initTest() {
		IntentFilter intentFilter = new IntentFilter("com.txznet.txz.test.ui");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String type = intent.getStringExtra("type");
				RecordInvokeAdapter adpter = WinManager.getInstance().getAdapter();
				if ("open".equals(type)) {
					adpter.show();
					return;
				}
				if ("close".equals(type)) {
					adpter.dismiss();
					return;
				}
				if ("state".equals(type)) {
					int state = intent.getIntExtra("state", 0);
					adpter.refreshState("record", state);
					return;
				}
				if ("volume".equals(type)) {
					int RefreshVolTime = intent.getIntExtra("time", 0);
					AppLogic.runOnBackGround(mRunRefreshVol, RefreshVolTime);
					return;
				}
				if ("autotest".equals(type)) {
					boolean start = intent.getBooleanExtra("start", true);
					if (start) {
						isTest = true;
						step = 0;
						AppLogic.removeUiGroundCallback(mRunAutoTest);
						AppLogic.runOnUiGround(mRunAutoTest, 2000);
						return;
					} else {
						isTest = false;
						AppLogic.removeUiGroundCallback(mRunAutoTest);
						step = 0;
						return;
					}
				}
				if("testSDCardError".equals(type)){
					SDCardUtil.testSDCardError();
					return;
				}
				if("testUpgradeDialog".equals(type)){
					
					return;
				}
				if("testSimCharge".equals(type)){
					String text = "您的8M/月的流量包订购成功，将为您立刻充值";
					if(text.contains("M")){
						text = text.replace("M", "兆");
					}
					Builder builder = new SimAlertDialog.Builder();
					builder.setHintTts(text);
					builder.setTtsCallBack(new ITtsCallback() {
						public void onEnd() {
							
						};
					});
					builder.setTitle("充值成功").setContent("您的8M/月的流量包订购成功，将为您立刻充值")
							.setIconId(R.drawable.sim_success).setDelay(8000)
							.setShowBtn(false).getDialog().show();
					return;
				}
			}
		}, intentFilter);
	}
}
