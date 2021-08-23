package com.txznet.txz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.widget.Toast;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.feedback.FeedbackManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.widget.QiWuTicketReminderView;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZCommUtil;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderManager;

public class TXZPowerControl {
	public static final int RELEASE_TYPE_DEEP = 0;
	public static final int RELEASE_TYPE_SHALLOW = 1;
	private static final String TAG = "TXZPowerControl::";

	public static int getReleaseType() {
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance()
				.getServerConfig();
		if (pbServerConfig == null
				|| pbServerConfig.uint64Flags == null
				|| ((pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_SHALLOW_SLEEP) == 0)) {
			JNIHelper.logd("deep sleep type");
			return RELEASE_TYPE_DEEP;
		}
		JNIHelper.logd("shallow sleep type");
		return RELEASE_TYPE_SHALLOW;
	}

	private static Runnable mRunnableDelayRelease = new Runnable() {
		@Override
		public void run() {
			switch (getReleaseType()) {
			case RELEASE_TYPE_SHALLOW: {
				TXZSourceRecorderManager.stop();
				ModuleManager.getInstance().release_DelayRelease();
				break;
			}
			case RELEASE_TYPE_DEEP:

			default: {
				// AppLogic.exit();
				break;
			}
			}
		}
	};

	private static Runnable mRunnableReleaseTXZ = new Runnable() {
		@Override
		public void run() {
			RecorderWin.dismiss();
			NewsManager.getInstance().stop();//休眠新闻需要停止,长安欧尚需求
			// 广播通知其他模块释放
			notifyRlease();
			// 取消识别
			AsrManager.getInstance().cancel();
			// 彻底关闭唤醒
			WakeupManager.getInstance().stopComplete();
			// 隐藏悬浮按钮
			SDKFloatView.getInstance().close();
			// 执行各个模块的释放逻辑
			ModuleManager.getInstance().release_InstandRelease();
			AppLogic.runOnUiGround(mRunnableDelayRelease, 2000);
			//如果满足条件,主动发送一次心跳
			if (bEnableSleepHeartHeat){
				oHeartBeatRun.run();
			}
			// 如果是深度休眠模式，强制2秒后退出，无论是否被取消，防止发生休眠异常bug
			switch (getReleaseType()) {
			case RELEASE_TYPE_SHALLOW: {
				break;
			}
			case RELEASE_TYPE_DEEP:
			default: {
				TXZSourceRecorderManager.stop();
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						AppLogic.exit();
					}
				}, 2000);
				break;
			}
			}
			
		}
	};

	private static Runnable mRunnableReinitTXZ = new Runnable() {
		@Override
		public void run() {
			// 恢复各个模块
			ModuleManager.getInstance().reinit();
			// 恢复录音
			TXZSourceRecorderManager.start();
			// 恢复唤醒
			WakeupManager.getInstance().start();
			// 校验是否初始化成功，并决定是否显示悬浮图标
			TXZService.checkSdkInitResult();
			// 通知其他服务重新初始化
			notifyInit();
		}
	};

	public static long mLastReleaseTime = 0;

	public static boolean hasReleased() {
		return mLastReleaseTime != 0;
	}

	public static void releaseTXZ() {
		mLastReleaseTime = SystemClock.elapsedRealtime();
		AppLogic.removeUiGroundCallback(mRunnableReinitTXZ);
		AppLogic.removeUiGroundCallback(mRunnableReleaseTXZ);
		AppLogic.removeUiGroundCallback(mRunnableDelayRelease);
		AppLogic.runOnUiGround(mRunnableReleaseTXZ, 0);
	}

	public static void reinitTXZ() {
		mLastReleaseTime = 0;
		AppLogic.removeUiGroundCallback(mRunnableReinitTXZ);
		AppLogic.removeUiGroundCallback(mRunnableReleaseTXZ);
		AppLogic.removeUiGroundCallback(mRunnableDelayRelease);
		AppLogic.runOnUiGround(mRunnableReinitTXZ, 0);
	}
	
	//ui线程
	public static void notifyRlease() {
		Intent intent = new Intent("com.txznet.txz.power.notify");
		intent.putExtra("type", "release");
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static boolean bEnableSleepHeartHeat = false; 
	private static Runnable oHeartBeatRun = new Runnable() {

		@Override
		public void run() {
			boolean released = hasReleased();
			String strNow = TXZCommUtil.timeToDate(System.currentTimeMillis() / 1000, "yyyy_MM_dd hh:mm:ss");
			JNIHelper.logd(strNow + ", heartbeat_released=" + released);
			//休眠中才需要通过alarm的方式主动维持心跳
			if (released && bEnableSleepHeartHeat){
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_HEARTBEAT);
			}
		}
	};
	
	public static void initSleepHeartBeatAlarm(){
		if (bEnableSleepHeartHeat){
			StandbyManager.getInstance().init(GlobalContext.get(), oHeartBeatRun);
		}
	}
	
	//ui线程
	public static void notifyInit() {
		Intent intent = new Intent("com.txznet.txz.power.notify");
		intent.putExtra("type", "init");
		GlobalContext.get().sendBroadcast(intent);
	}
	
	static {
		IntentFilter intentFilter = new IntentFilter("com.txznet.txz.power.query");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// ui 线程
				boolean released = hasReleased();
				boolean inited = AppLogic.isInited();
				LogUtil.logd("power query released=" + released + ", inited="+inited);
				if (released || inited == false) {
					notifyRlease();
				} else {
					notifyInit();
				}
			}
		}, intentFilter);
	}

	private static boolean mEnterReverse = false;
	private static int mLastFloatViewType = SDKFloatView.TYPE_FLOAT_NORMAL;
	private static Integer currentFloatViewType;
	private static boolean mNeedShowTips = false;
	private static byte[] mLastNavData;//微信下发的导航信息
	private static byte[] mLastRechargeResData;//流量充值结果信息
	private static byte[] mLastSimInfoChangeData;//流量卡信息变动信息，包含实名认证信息
	private static byte[] mLastSimFlowChangeData;//流量卡流量信息变动
	private static byte[] mLastSimCommonPushData;//流量卡通用推送消息，包含一些弹窗
	private static byte[] mLastReimderPushData;//提醒功能推送消息

	/**
	 * 通知当前的声控图标状态，方便恢复
	 * @param floatViewType
	 */
	public static void notifyFloatViewTypeUpdate(int floatViewType){
		mLastFloatViewType = floatViewType;
	}

	/**
	 a)	停止使用语音唤醒（包含主唤醒与免唤醒）
	 b)	停止调用语音播报接口，如：第三方应用推送播报消息等
	 c)	后台推送的位置消息待退出倒车时再显示和播报
	 d)	隐藏语音助手浮标
	 e)	如倒车时已处于语音交互中（包含录音中、播报中、处理中），终止当前操作，退出语音助手，并弹出toast提示：倒车中，语音助手暂停使用。退出倒车时弹出toast提示：语音助手已恢复使用。Toast显示3s后自动隐藏。
	 注意事项：
	 1.禁用掉的标志位记得还原
	 2.界面改变相关记得切到UI线程
	 */
	public static void setEnterReverse(boolean mEnterReverse) {
		if (mEnterReverse != TXZPowerControl.mEnterReverse) {
			TXZPowerControl.mEnterReverse = mEnterReverse;
			if (mEnterReverse){
				NewsManager.getInstance().stop();//倒车新闻需要停止,长安欧尚需求
				WakeupManager.getInstance().stop();
				RecordManager.getInstance().setEnableRecording(false);
				//停止tts播报
				TtsManager.getInstance().pause();
				FeedbackManager.getInstance().cancel();
				QiWuTicketReminderView.closePushView();
				mLastFloatViewType = SDKFloatView.getInstance().getFloatViewType();
				currentFloatViewType = mLastFloatViewType;
				if (mLastFloatViewType != SDKFloatView.TYPE_FLOAT_NONE) {
					AppLogic.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							SDKFloatView.getInstance().setFloatToolType(SDKFloatView.TYPE_FLOAT_NONE , true);
							mLastFloatViewType = currentFloatViewType;
						}
					});
				}
				if (RecorderWin.isOpened()) {
					mNeedShowTips = true;
					RecorderWin.close();
					AppLogic.showToast(NativeData.getResString("RS_TIPS_POWER_ENTER_REVERSE"), Toast.LENGTH_LONG);
				}

				WeixinManager.getInstance().dismissConfirm();
				GlobalObservableSupport.getRevereObservable().onReverse();

			}else {
				//播条空的tts，激活tts播报队列
				TtsManager.getInstance().speakText("");
				WakeupManager.getInstance().start();
				RecordManager.getInstance().setEnableRecording(true);
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						SDKFloatView.getInstance().setFloatToolType(mLastFloatViewType , true);
					}
					});
				if (mNeedShowTips) {
					mNeedShowTips = false;
					AppLogic.showToast(NativeData.getResString("RS_TIPS_POWER_QUIT_REVERSE"),Toast.LENGTH_LONG);
				}
				if (mLastNavData != null) {
					WeixinManager.getInstance().notifyNavigation(mLastNavData);
					mLastNavData = null;
				}
				if (mLastRechargeResData != null) {
					LogUtil.logd(TAG + "showRechargeResult");
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SIM_RECHARGE_RESULT, mLastRechargeResData);
					mLastRechargeResData = null;
				}
				if (mLastSimInfoChangeData != null) {
					LogUtil.logd(TAG + "handleSimInfoChanged");
					SimManager.getInstance().handleSimInfoChanged(mLastSimInfoChangeData);
					mLastSimInfoChangeData = null;
				}
				if(mLastSimFlowChangeData != null) {
					LogUtil.logd(TAG + "handleSimFlowChange");
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SIM_FLOW_CHANGED, mLastSimFlowChangeData);
					mLastSimFlowChangeData = null;
				}
				if(mLastSimCommonPushData != null) {
					LogUtil.logd(TAG + "handleSimCommonPush");
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SIM_PUSH, mLastSimCommonPushData);
					mLastSimCommonPushData = null;
				}
				if(mLastReimderPushData != null) {
					LogUtil.logd(TAG + "handleReminderPush");
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_REMINDER_PUSH, mLastReimderPushData);
					mLastReimderPushData = null;
				}
			}
		}
	}

	public static boolean isEnterReverse() {
		return mEnterReverse;
	}

	public static void setLastNavData(byte[] mLastNavData) {
		TXZPowerControl.mLastNavData = mLastNavData;
	}

	public static void setLastRechargeResult(byte[] mLastData) {
		TXZPowerControl.mLastRechargeResData = mLastData;
	}

	public static void setLastSimInfoChangeData(byte[] mLastData){
		TXZPowerControl.mLastSimInfoChangeData = mLastData;
	}

	public static void setmLastSimFlowChangeData(byte[] mLastData) {
		TXZPowerControl.mLastSimFlowChangeData = mLastData;
	}

	public static void setmLastSimCommonPushData(byte[] mLastData) {
		TXZPowerControl.mLastSimCommonPushData = mLastData;
	}
	
	public static void setmLastReimderPushData(byte[] mLastData) {
		TXZPowerControl.mLastReimderPushData = mLastData;
	}
}
