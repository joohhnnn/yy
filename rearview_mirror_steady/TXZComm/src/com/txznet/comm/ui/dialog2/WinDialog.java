package com.txznet.comm.ui.dialog2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.IKeepClass;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 对话框基础类
 * 
 * @author pppi
 */
public abstract class WinDialog implements IKeepClass {
	/**
	 * 默认播报空文本（避免抢占音频焦点），统一处理，
	 */
	private final static String DEFAULT_HINT_TTS = "";

	/**
	 * 默认视图ID
	 */
	public final static String DEFAULT_VIEW_REPORT_ID = "unknow";
	/**
	 * 默认倒计时后缀
	 */
	public final static String DEFAULT_COUNT_DOWN_SUFFIX = " (%TIME%)";
	
	public static int mType = -1;

	public static String dialogTool = "";

	/**
	 * 获取默认配置方案，需要重写从配置文件读取
	 */
	public static int getSystemDialogWindowType() {
		return WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
	}

	/**
	 * 从UI线程删除Runnable
	 * 
	 * @param r
	 *            要删除的Runnable
	 */
	public static void removeUiGroundCallback(Runnable r) {
		AppLogicBase.removeUiGroundCallback(r);
	}

	/**
	 * 在UI线程执行Runnable
	 * 
	 * @param r
	 *            要执行的Runnable
	 * @param delay
	 *            执行时延，单位：毫秒
	 */
	public static void runOnUiGround(Runnable r, long delay) {
		AppLogicBase.runOnUiGround(r, delay);
	}

	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * 内部对话框实现类
	 * 
	 * @author pppi
	 *
	 */
	protected class DialogInner extends Dialog {
		public DialogInner(Context context, int theme) {
			super(context, theme);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			WinDialog.this.onCreate(savedInstanceState);
		}

		public void super_onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@Override
		protected void onStart() {
			WinDialog.this.onStart();
		}

		public void super_onStart() {
			super.onStart();
		}

		@Override
		protected void onStop() {
			WinDialog.this.onStop();
		}

		public void super_onStop() {
			super.onStop();
		}

		@Override
		public void onWindowFocusChanged(boolean newFocus) {
			WinDialog.this.onWindowFocusChanged(newFocus);
			super.onWindowFocusChanged(newFocus);
		}

		@Override
		public void onBackPressed() {
			WinDialog.this.doReport(REPORT_ACTION_TYPE_BACK);
			WinDialog.this.onBackPressed();
		}

		public void super_onBackPressed() {
			super.onBackPressed();
		}

		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			WinDialog.this.doReport(REPORT_ACTION_TYPE_KEY, "" + event.getKeyCode());
			return WinDialog.this.onKeyDown(keyCode, event);
		}

		public boolean super_onKeyDown(int keyCode, KeyEvent event) {
			return super.onKeyDown(keyCode, event);
		}

		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			return WinDialog.this.onKeyUp(keyCode, event);
		}

		public boolean super_onKeyUp(int keyCode, KeyEvent event) {
			return super.onKeyUp(keyCode, event);
		}

		@Override
		public boolean onKeyLongPress(int keyCode, KeyEvent event) {
			return WinDialog.this.onKeyLongPress(keyCode, event);
		}

		public boolean super_onKeyLongPress(int keyCode, KeyEvent event) {
			return super.onKeyLongPress(keyCode, event);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			return WinDialog.this.onTouchEvent(event);
		}

		public boolean super_onTouchEvent(MotionEvent event) {
			return super.onTouchEvent(event);
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			return WinDialog.this.dispatchKeyEvent(event);
		}

		public boolean super_dispatchKeyEvent(KeyEvent event) {
			return super.dispatchKeyEvent(event);
		}

		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			return WinDialog.this.dispatchTouchEvent(ev);
		}

		public boolean super_dispatchTouchEvent(MotionEvent ev) {
			return super.dispatchTouchEvent(ev);
		}
	}

	/**
	 * 生命周期：创建对话框时回调
	 * 
	 * @param savedInstanceState
	 */
	protected void onCreate(Bundle savedInstanceState) {
		mDialog.super_onCreate(savedInstanceState);
	}

	/**
	 * 生命周期：启动对话框时回调
	 */
	protected void onStart() {
		mDialog.super_onStart();
	}

	/**
	 * 生命周期：停止对话框时回调
	 */
	protected void onStop() {
		mDialog.super_onStop();
	}

	/**
	 * 生命周期：首次显示对话框时回调
	 */
	protected void onShow() {
	}

	private void onDismissInner() {
		endCountDownInner(COUNT_DOWN_END_REASON_DISMISS);

		if (mHasFocus) {
			mHasFocus = false;
			onLoseFocusInner();
		}
		cancelHintTts();
		WinDialog.this.cancelScreenLock();
		TXZWheelControlManager.getInstance().unregisterWheelControlListener(onTXZWheelControlListener);
		if (mFirstGetFocus == false) {
			LogUtil.logd("onDismiss: " + WinDialog.this.getDebugString());
			mFirstGetFocus = true;
			// 清理上报时间
			mTimeShow = mTimeBeginCountDown = mTimeBeginTts = mTimeEndTts = 0;
			mFocusViews = null;
			mFocusPosition = -1;
			WinDialog.this.onDismiss();
			getContext().sendBroadcast(
					new Intent("com.txznet.txz.action.FLOAT_WIN_DISMISS"));
		}
	}

	/**
	 * 生命周期：撤销对话框时回调
	 */
	protected void onDismiss() {
	}

	/**
	 * 按返回键时回调
	 */
	public void onBackPressed() {
		mDialog.super_onBackPressed();
	}

	/**
	 * 按键按下时回调
	 * 
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 如果设置了mFocusViews，则直接处理并分发焦点事件
		if (mFocusViews != null) {
			switch (keyCode){
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_DPAD_CENTER:
					onNavKeyCode(keyCode);
					return true;
			}
		}
		return mDialog.super_onKeyDown(keyCode, event);
	}

	private void onNavKeyCode(int keyCode){
		if (mFocusViews == null || mFocusViews.size() == 0) {
			LogUtil.loge("WinDialog mFocusViews empty");
			return;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_UP:
			if (mFocusPosition > 0 && mFocusPosition < mFocusViews.size()) {
				mFocusPosition--;
			} else {
				mFocusPosition = mFocusViews.size() - 1;
			}
			updateFocus();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mFocusPosition >= 0 && mFocusPosition < mFocusViews.size() - 1) {
				mFocusPosition++;
			} else {
				mFocusPosition = 0;
			}
			updateFocus();
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (mFocusPosition >= 0 && mFocusPosition < mFocusViews.size()) {
				mFocusViews.get(mFocusPosition).mView.post(new Runnable1<Integer>(mFocusPosition) {
					@Override
					public void run() {
						doReport(REPORT_ACTION_TYPE_FOCUS, "clickFocus", mFocusViews.get(mP1).mId);
						mFocusViews.get(mP1).mView.performClick();
					}
				});
			}
		}
	}

	private void updateFocus() {
		if (mFocusViews == null || mFocusViews.size() == 0) {
			LogUtil.loge("WinDialog mFocusViews empty");
			return;
		}
		if(mFocusPosition>mFocusViews.size()){
			LogUtil.loge("WinDialog mFocusPosition out of range.mFocusPosition:" + mFocusPosition + ",focus size:"
					+ mFocusViews.size());
			return;
		}
		LogUtil.logd("update focus :" + mFocusViews.size());
		for (int i = 0; i < mFocusViews.size(); i++) {
			mFocusViews.get(i).mView.post(new Runnable1<Integer>(i) {
				@Override
				public void run() {
					mFocusViews.get(mP1).mView.setFocusable(mFocusPosition == mP1);
					mFocusViews.get(mP1).mView.setFocusableInTouchMode(mFocusPosition == mP1);
					if (mP1 == mFocusPosition) {
						LogUtil.logd("update focus position:" + mP1 + ",id:" + mFocusViews.get(mP1).mId);
						doReport(REPORT_ACTION_TYPE_FOCUS, "obtainFocus", mFocusViews.get(mP1).mId);
						mFocusViews.get(mP1).mView.requestFocus();
					}
				}
			});
		}
	}


	public boolean onWheelControlKeyEvent(int event){
		return false;
	}

	/**
	 * 按键弹起时回调
	 * 
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return mDialog.super_onKeyUp(keyCode, event);
	}

	/**
	 * 按键长按时回调
	 * 
	 * @param keyCode
	 * @param event
	 * @return
	 */
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return mDialog.super_onKeyLongPress(keyCode, event);
	}

	/**
	 * 触屏时回调
	 * 
	 * @param event
	 * @return
	 */
	public boolean onTouchEvent(MotionEvent event) {
		return mDialog.super_onTouchEvent(event);
	}

	/**
	 * 分发按键事件
	 * 
	 * @param event
	 * @return
	 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		return mDialog.super_dispatchKeyEvent(event);
	}

	/**
	 * 分发触屏事件
	 * 
	 * @param ev
	 * @return
	 */
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return mDialog.super_dispatchTouchEvent(ev);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 默认：有TTS文本时，根据TTS的时长点亮(被打断即释放)，没有TTS文本时，弹出时点亮一下
	 */
	public final static int SCREEN_LOCK_TYPE_DEFAULT = 0;
	/**
	 * 弹出时点亮一下，系统自动控制熄灭
	 */
	public final static int SCREEN_LOCK_TYPE_POPUP = -1;
	/**
	 * 对话框显示时保持点亮
	 */
	public final static int SCREEN_LOCK_TYPE_DURING = -2;
	/**
	 * 对话框焦点时保持点亮
	 */
	public final static int SCREEN_LOCK_TYPE_FOCUS = -3;
	/**
	 * 对话框不点亮
	 */
	public final static int SCREEN_LOCK_TYPE_NONE = -4;

	/**
	 * 识别回调
	 */
	public static interface DialogAsrCallback {
		/**
		 * 获取数据上报的id
		 */
		public String getReportId(WinDialog win);

		/**
		 * 识别到命令的回调
		 * 
		 * @param cmd
		 *            识别到的文字
		 */
		public void onSpeak(WinDialog win, String cmd);
	}

	/**
	 * ID类语音回调
	 */
	public static abstract class DialogAsrIdCallback implements
			DialogAsrCallback {
		private String mId;

		public DialogAsrIdCallback(String id) {
			mId = id;
		}

		public abstract void onSpeak(WinDialog win, String cmd);

		@Override
		public String getReportId(WinDialog win) {
			return mId;
		}
	}

	/**
	 * 视图类语音回调
	 * 
	 * @author pppi
	 *
	 */
	public static class DialogAsrViewCallback implements DialogAsrCallback {
		private int mViewId;

		public DialogAsrViewCallback(int viewId) {
			mViewId = viewId;
		}

		@Override
		public void onSpeak(WinDialog win, String cmd) {
			win.clickView(mViewId, true);
			win.dismissInner();
		}

		@Override
		public String getReportId(WinDialog win) {
			return win.getReportViewId(mViewId);
		}
	}

	/**
	 * 对话框构造结构体，只允许构造时使用
	 */
	public static class DialogBuildData {
		Context mContext;
		WinDialog mThis;
		Object mData;
		Integer mWinType;
		Integer mWinFlag;
		boolean mIsFull;
		boolean mIsSystem;
		Boolean mCancelable;
		Boolean mCancelOutside;
		String mHintTts;
		PreemptType mPreemptType;
		HashMap<String, DialogAsrCallback> mAsrTaskList;
		int mScreenLockTime = SCREEN_LOCK_TYPE_DEFAULT; // <=0则按类型策略，大于0则按时间策略
		boolean mStopCountDownWhenLoseFocus;
		JSONObject mBuildInfo;


		public String getBuildData(){
			return mBuildInfo.toString();
		}

		/**
		 * 检查数据，可以在此填充默认值
		 */
		public void check() {
			// 默认上下文设置
			if (this.mContext == null) {
				this.mContext = GlobalContext.getModified();
				this.mIsSystem = true;
			}

			// 默认打断类型设置
			if (mPreemptType == null) {
				mPreemptType = PreemptType.PREEMPT_TYPE_NONE;
			}

			// 默认窗口层级设置
			if (null == this.mWinType) {
				if (ActivityStack.getInstance().currentActivity() == null) {
					this.mWinType = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
				}
				if (this.mIsSystem) {
					this.mWinType = getSystemDialogWindowType();
				}
				if (mType != -1) {
					this.mWinType = mType;
				}
			}
			// 合并文件配置，文件配置优先度最高
			WinDialogOptions.mergeLocalConfig(this);

			addExtraInfo("mWinType", mWinType);
			addExtraInfo("mIsFull", mIsFull);
			addExtraInfo("mIsSystem", mIsSystem);
			addExtraInfo("mCancelable", mCancelable);
			addExtraInfo("mCancelOutside", mCancelOutside);
			addExtraInfo("mHintTts", mHintTts);
			addExtraInfo("mScreenLockTime", mScreenLockTime);
			addExtraInfo("mStopCountDownWhenLoseFocus", mStopCountDownWhenLoseFocus);
			addExtraInfo("mPreemptType", mPreemptType.name());
			addExtraInfo("dialogType", WinDialog.class.getSimpleName());
		}

		/**
		 * 设置使用的上下文
		 * 
		 * @param context
		 * @return
		 */
		public DialogBuildData setContext(Context context) {
			this.mContext = context;
			return this;
		}

		/**
		 * 设置对话附带的自定义数据，可以使用getData获取
		 * 
		 * @param data
		 * @return
		 */
		public DialogBuildData setData(Object data) {
			this.mData = data;
			return this;
		}

		/**
		 * 设置使用的系统弹窗层级
		 * 
		 * @param type
		 * @return
		 */
		public DialogBuildData setWindowType(int type) {
			this.mWinType = type;
			return this;
		}

		public DialogBuildData setWindowFlag(Integer flag) {
			if(null == flag){
				return this;
			}
			this.mWinFlag = flag;
			return this;
		}

		/**
		 * 设置是否全屏
		 * 
		 * @param flag
		 * @return
		 */
		public DialogBuildData setFullScreen(boolean flag) {
			this.mIsFull = flag;
			return this;
		}

		/**
		 * 设置是否为系统弹窗
		 * 
		 * @param flag
		 * @return
		 */
		public DialogBuildData setSystemDialog(boolean flag) {
			this.mIsSystem = flag;
			return this;
		}

		/**
		 * 设置是否可取消
		 * 
		 * @param flag
		 * @return
		 */
		public DialogBuildData setCancelable(boolean flag) {
			this.mCancelable = flag;
			return this;
		}

		/**
		 * 设置是否可以点击外部取消
		 * 
		 * @param flag
		 * @return
		 */
		public DialogBuildData setCancelOutside(boolean flag) {
			this.mCancelOutside = flag;
			return this;
		}

		/**
		 * 设置显示对话框时的弹窗提示
		 * 
		 * @param tts
		 *            需要播报的文本
		 * @return
		 */
		public DialogBuildData setHintTts(String tts) {
			this.mHintTts = tts;
			return this;
		}

		/**
		 * 设置显示对话框时的弹窗提示，等待TTS队列则会等播报开始后才显示对话框
		 * 
		 * @param tts
		 *            需要播报的文本
		 * @param type
		 *            TTS打断类型，由此确定弹窗真正显示的时间
		 * @return
		 */
		public DialogBuildData setHintTts(String tts, PreemptType type) {
			this.mHintTts = tts;
			this.mPreemptType = type;
			return this;
		}

		/**
		 * 设置TTS的打断类型
		 * 
		 * @param type
		 *            TTS打断类型，由此确定弹窗真正显示的时间
		 * @return
		 */
		public DialogBuildData setHintType(PreemptType type) {
			this.mPreemptType = type;
			return this;
		}

		/**
		 * 添加识别任务
		 * 
		 * @param callback
		 *            识别到指令时执行的任务
		 * @param cmds
		 *            识别的命令字列表
		 * @return
		 */
		public DialogBuildData addAsrTask(DialogAsrCallback callback,
				String... cmds) {
			if (this.mAsrTaskList == null) {
				this.mAsrTaskList = new HashMap<String, DialogAsrCallback>();
			}
			for (String cmd : cmds) {
				this.mAsrTaskList.put(cmd, callback);
			}
			return this;
		}

		/**
		 * 添加附带的额外信息
		 * @param key
		 * @param value
		 * @return
		 */
		public DialogBuildData addExtraInfo(String key, Object val) {
			if (this.mBuildInfo == null) {
				this.mBuildInfo = new JSONObject();
			}
			try {
				if (val == null) {

				}else if (val.getClass().isArray()) {
					Object[] objArr = (Object[]) val;
					JSONArray jArr = new JSONArray();
					for (Object obj : objArr) {
						jArr.put(obj);
					}
					mBuildInfo.put(key, jArr);
				} else if (val instanceof Collection) {
					JSONArray jArr = new JSONArray((Collection) val);
					mBuildInfo.put(key, jArr);
				} else {
					mBuildInfo.put(key, val);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return this;
		}

		/**
		 * 设置屏幕点亮的时间，单位毫秒，或传递SCREE_LOCK_TYPE_***常量
		 * 
		 * @param time
		 * @return
		 */
		public DialogBuildData setScreenLockTime(int time) {
			mScreenLockTime = time;
			return this;
		}

		/**
		 * 设置当丢失焦点时是否停止倒计时
		 * 
		 * @param flag
		 *            是否停止倒计时
		 */
		public void setStopCountDownWhenLoseFocus(boolean flag) {
			this.mStopCountDownWhenLoseFocus = flag;
		}
	}

	/**
	 * 对话框构建数据
	 */
	protected DialogBuildData mBuildData;
	/**
	 * 真实对话框对象
	 */
	protected DialogInner mDialog;
	/**
	 * 创建的视图View对象
	 */
	protected View mView;
	/**
	 * TTS的任务ID
	 */
	private int mHintTtsTaskId = TtsUtil.INVALID_TTS_TASK_ID;
	/**
	 * 唤醒识别任务
	 */
	private AsrUtil.IWakeupAsrCallback mAsrTask;
	/**
	 * 屏幕锁
	 */
	private ScreenLock mScreenLock;

	/**
	 * 对话框显示时间
	 */
	private long mTimeShow;
	/**
	 * TTS开始播报事件
	 */
	private long mTimeBeginTts;
	/**
	 * TTS播报时间
	 */
	private long mTimeEndTts;
	/**
	 * 倒计时开始时间
	 */
	private long mTimeBeginCountDown;
	/**
	 * 支持方控可获取焦点的View
	 */
	private List<FocusView> mFocusViews;
	/**
	 * 1表示正处于焦点
	 */
	private int mFocusPosition;

	/**
	 * 倒计时的Runnable类型
	 */
	private static abstract class CountDownRunnable implements Runnable {
		int mCur;
		int mCount;

		public CountDownRunnable(int time) {
			mCount = mCur = time;
		}

		public int getCount() {
			return mCount;
		}
	}

	/**
	 * 倒计时的Runnable
	 */
	private CountDownRunnable mRunnableCountDown;

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinDialog(DialogBuildData data) {
		this(data, true);
	}

	/**
	 * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
	 * 
	 * @param data
	 *            构建数据
	 * @param init
	 *            是否初始化，自己构造时传true，派生类构造时传false
	 */
	protected WinDialog(DialogBuildData data, boolean init) {
		this.mBuildData = data;
		data.mThis = this;

		if (init) {
			initDialog();
		}
	}

	/**
	 * 初始化对话框，如果需要
	 */
	protected void initDialog() {
		// 检验构建数据
		this.mBuildData.check();

		// 生成唤醒识别任务，可由派生类重写
		genAsrTask();

		// 在UI线程构建对话框
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				buildDialog();
			}
		}, 0);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 构造对话框时在对话框初始化完成后回调
	 */
	protected void onInitDialog() {

	}

	/**
	 * 构建对话框
	 */
	@SuppressLint("InlinedApi")
	private void buildDialog() {
		// 构造真实对话框
		this.mDialog = new DialogInner(this.mBuildData.mContext,
				this.mBuildData.mIsFull ? R.style.TXZ_Dialog_Style_Full
						: R.style.TXZ_Dialog_Style);

		// 设置窗口层级
		if (null != this.mBuildData.mWinType) {
			mDialog.getWindow().setType(this.mBuildData.mWinType);
		}
		if(null != this.mBuildData.mWinFlag){
			WindowManager.LayoutParams attrs = mDialog.getWindow().getAttributes();
			attrs.flags = this.mBuildData.mWinFlag;
			mDialog.getWindow().setAttributes(attrs);
		}
		
		//设置对话框是否可以响应BACK键
		Boolean bCancelable = this.mBuildData.mCancelable;
		if (null != bCancelable){
			LogUtil.logd("DialogBuildData mCancelable : " + bCancelable);
			mDialog.setCancelable(bCancelable);
		}
		
		//设置对话框是否可以响应对话框之外的touch事件
		Boolean bCancelOutside = this.mBuildData.mCancelOutside;
		if (null != bCancelOutside){
			LogUtil.logd("DialogBuildData mCancelOutside : " + bCancelOutside);
			mDialog.setCanceledOnTouchOutside(bCancelOutside);
		}

		// 调整对话框布局
		mDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		// 设置对话框关闭回调
		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				WinDialog.this.onDismissInner();
			}
		});

		// createView里可以重新调整参数
		mView = createView();
		mDialog.setContentView(mView);

		// 设置全屏状态
		updateFullScreen();

		// 回调对话框初始化完成
		onInitDialog();
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取对话框使用的上下文
	 * 
	 * @return
	 */
	protected Context getContext() {
		return this.mBuildData.mContext;
	}

	/**
	 * 是否已经显示
	 * 
	 * @return
	 */
	public boolean isShowing() {
		return this.mDialog != null && this.mDialog.isShowing();
	}

	/**
	 * 设置是否可以取消，已废弃，建议使用构建数据初始化设置
	 * 
	 * @param flag
	 */
	@Deprecated
	public void setCancelable(boolean flag) {
		this.mBuildData.mCancelable = flag;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mDialog.setCancelable(WinDialog.this.mBuildData.mCancelable);
			}
		}, 0);
	}

	/**
	 * 设置点击外部是否可取消，已废弃，建议使用构建数据初始化设置
	 * 
	 * @param cancel
	 */
	@Deprecated
	public void setCanceledOnTouchOutside(final boolean cancel) {
		this.mBuildData.mCancelOutside = cancel;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mDialog.setCanceledOnTouchOutside(WinDialog.this.mBuildData.mCancelOutside);
			}
		}, 0);
	}

	/**
	 * 更新全屏状态
	 */
	@SuppressLint("InlinedApi")
	private void updateFullScreen() {
		if (this.mBuildData.mIsFull) {
			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			// | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_FULLSCREEN);
			mDialog.getWindow()
					.getDecorView()
					.setOnSystemUiVisibilityChangeListener(
							new OnSystemUiVisibilityChangeListener() {
								private long lastTime = 0;

								@Override
								public void onSystemUiVisibilityChange(
										int visibility) {
									LogUtil.logd("onSystemUiVisibilityChange:"
											+ visibility);
									if (WinDialog.this.mBuildData.mIsFull
											&& (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
										long t = SystemClock.elapsedRealtime();
										if (t - lastTime > 1000) {
											lastTime = t;
											mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
													// |
													// View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
													| View.SYSTEM_UI_FLAG_FULLSCREEN);
										} else {
											// 如果1秒内重复设置，则延迟500ms执行，防止出现死循环
											runOnUiGround(new Runnable() {
												@Override
												public void run() {
													lastTime = SystemClock
															.elapsedRealtime();
													if (WinDialog.this.mBuildData.mIsFull) {
														mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
																// |
																// View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
																| View.SYSTEM_UI_FLAG_FULLSCREEN);
													}
												}
											}, 500);
										}
									}
								}
							});
		} else {
			int visibility = mView.getSystemUiVisibility() & ~(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN); 
			mView.setSystemUiVisibility(visibility);
			mDialog.getWindow().getDecorView()
					.setOnSystemUiVisibilityChangeListener(null);
		}
	}

	/**
	 * 设置是否全屏，已废弃，建议使用构建数据初始化设置
	 * 
	 * @param isFullScreen
	 */
	@Deprecated
	public void setIsFullSreenDialog(boolean isFullScreen) {
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		this.mBuildData.mIsFull = isFullScreen;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateFullScreen();
			}
		}, 0);
	}

	/**
	 * 更新弹窗类型，已废弃，建议使用构建数据初始化设置
	 * 
	 * @param type
	 */
	@Deprecated
	public void updateDialogType(int type) {
		LogUtil.logd("updateDialogType type:" + type);
		WinDialog.this.mBuildData.mWinType = type;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mDialog.getWindow().setType(WinDialog.this.mBuildData.mWinType);
			}
		}, 0);
	}

	/**
	 * 创建view，由具体dialog来实现
	 * 
	 * @return
	 */
	protected abstract View createView();

	/**
	 * 生成唤醒识别任务，可重写
	 */
	protected void genAsrTask() {
		if (mBuildData.mAsrTaskList != null
				&& mBuildData.mAsrTaskList.size() > 0) {
			mAsrTask = new IWakeupAsrCallback() {
				@Override
				public boolean needAsrState() {
					return true;
				}

				@Override
				public String getTaskId() {
					return WinDialog.this.getDialogType() + "@"
							+ WinDialog.this.hashCode();
				}

				@Override
				public String needTts() {
					return null;
				};

				@Override
				public boolean onAsrResult(final String text) {
					final DialogAsrCallback r = WinDialog.this.mBuildData.mAsrTaskList
							.get(text);
					if (r != null) {
						runOnUiGround(new Runnable() {
							@Override
							public void run() {
								WinDialog.this.doReport(
										REPORT_ACTION_TYPE_SPEAK,
										r.getReportId(WinDialog.this), text);
								r.onSpeak(WinDialog.this, text);
							}
						}, 0);
						return true;
					}
					return false;
				};

				@Override
				public String[] genKeywords() {
					Set<String> kws = WinDialog.this.mBuildData.mAsrTaskList
							.keySet();
					return kws.toArray(new String[kws.size()]);
				};
			};
		}
	}

	/**
	 * 获取自定义数据
	 * 
	 * @return
	 */
	public Object getData() {
		return this.mBuildData.mData;
	}

	/**
	 * 获取指定类型的数据
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(Class<T> clazz) {
		return (T) this.mBuildData.mData;
	}

	private Runnable mRequestScreenLockRunnable = new Runnable() {
		@Override
		public void run() {
			if (mScreenLock == null) {
				LogUtil.logd("RequestScreenLock: "
						+ WinDialog.this.getDebugString());
				mScreenLock = new ScreenLock(mDialog.getContext());
			}
		}
	};

	private Runnable mCancelScreenLockRunnable = new Runnable() {
		@Override
		public void run() {
			if (mScreenLock != null) {
				LogUtil.logd("CancelScreenLock: "
						+ WinDialog.this.getDebugString());
				mScreenLock.release();
				mScreenLock = null;
			}
		}
	};

	/**
	 * 申请屏幕锁
	 */
	public void requestScreenLock() {
		removeUiGroundCallback(mRequestScreenLockRunnable);
		removeUiGroundCallback(mCancelScreenLockRunnable);
		runOnUiGround(mRequestScreenLockRunnable, 0);
	}

	private void cancelScreenLockInner(long delay) {
		removeUiGroundCallback(mRequestScreenLockRunnable);
		removeUiGroundCallback(mCancelScreenLockRunnable);
		runOnUiGround(mCancelScreenLockRunnable, delay);
	}

	/**
	 * 取消屏幕锁
	 */
	public void cancelScreenLock() {
		cancelScreenLockInner(0);
	}

	/**
	 * 取消TTS提示
	 */
	public void cancelHintTts() {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mTimeBeginTts = mTimeEndTts = SystemClock.elapsedRealtime();
				if (TtsUtil.INVALID_TTS_TASK_ID != mHintTtsTaskId) {
					TtsUtil.cancelSpeak(mHintTtsTaskId);
					mHintTtsTaskId = TtsUtil.INVALID_TTS_TASK_ID;
				}
			}
		}, 0);
	}

	/**
	 * 倒计时到期结束
	 */
	public final static int COUNT_DOWN_END_REASON_EXPIRED = -1;
	/**
	 * 倒计时取消结束
	 */
	public final static int COUNT_DOWN_END_REASON_CANCEL = -2;
	/**
	 * 倒计时丢失焦点结束
	 */
	public final static int COUNT_DOWN_END_REASON_LOSE_FOCUS = -3;
	/**
	 * 倒计时撤销弹窗结束
	 */
	public final static int COUNT_DOWN_END_REASON_DISMISS = -4;
	/**
	 * 倒计时新任务导致结束
	 */
	public final static int COUNT_DOWN_END_REASON_NEW_TASK = -5;

	/**
	 * 倒计时回调，可重写做一些处理
	 * 
	 * @param time
	 *            倒计时时间或结束原因，参考COUNT_DOWN_END_REASON
	 */
	public void onCountDown(int time) {

	}

	/**
	 * 带原因结束倒计时
	 * 
	 * @param reason
	 *            原因码
	 */
	private void endCountDownInner(final int reason) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mTimeBeginCountDown = 0;
				if (null != mRunnableCountDown) {
					removeUiGroundCallback(mRunnableCountDown);
					mRunnableCountDown = null;
					onCountDown(reason);
				}
			}
		}, 0);
	}

	/**
	 * 取消倒计时
	 */
	public void cancelCountDown() {
		endCountDownInner(COUNT_DOWN_END_REASON_CANCEL);
	}

	public boolean clickView(int viewId, boolean fromVoice) {
		return false;
	}

	/**
	 * 更新文本框内容，为null则隐藏
	 * 
	 * @param view
	 * @param text
	 */
	protected void updateTextView(TextView view, CharSequence text) {
		if (text != null) {
			view.setText(LanguageConvertor.toLocale(text.toString()));
			view.setVisibility(View.VISIBLE);
		} else {
			view.setText("");
			view.setVisibility(View.GONE);
		}
	}

	/**
	 * 更新倒计时状态
	 * 
	 * @param view
	 *            更新的视图
	 * @param text
	 *            格式化文本，可以传null，在onCountDown里处理对应
	 * @param time
	 *            倒计时时间，单位：秒
	 * @param end
	 *            倒计时完成后执行的操作
	 */
	protected void updateCountDown(final TextView view, final String text,
			final int time, final Runnable end) {
		final CharSequence rawText = (view != null && text != null) ? view
				.getText() : null;
		WinDialog.this.endCountDownInner(COUNT_DOWN_END_REASON_NEW_TASK);
		// 异步插入任务，等前一个任务先取消
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mTimeBeginCountDown = SystemClock.elapsedRealtime();
				mRunnableCountDown = new CountDownRunnable(time) {
					@Override
					public void run() {
						removeUiGroundCallback(this);
						if (mRunnableCountDown != this) {
							return;
						}

						if (mCur <= 0) {
							if (view == null) {
								WinDialog.this.doReport(
										REPORT_ACTION_TYPE_COUNTDOWN,
										DEFAULT_VIEW_REPORT_ID);
							} else {
								WinDialog.this.doReport(
										REPORT_ACTION_TYPE_COUNTDOWN,
										getReportViewId(view.getId()));
							}
							// 还原成倒计时前的文本
							if (text != null && view != null) {
								WinDialog.this.updateTextView(view, rawText);
							}
							WinDialog.this.onCountDown(mCur);
							WinDialog.this.endCountDownInner(COUNT_DOWN_END_REASON_EXPIRED);
							if (end != null) {
								end.run();
							}
						} else {
							// 组装计时文本
							if (text != null && view != null) {
								int n = text.indexOf("%TIME%");
								if (n >= 0) {
									WinDialog.this.updateTextView(
											view,
											text.substring(0, n)
													+ mCur
													+ text.substring(n
															+ "%TIME%".length()));
								} else {
									WinDialog.this.updateTextView(view, text);
								}
							}
							WinDialog.this.onCountDown(mCur);
							--mCur;
							runOnUiGround(this, 1000);
						}
					}
				};
				mRunnableCountDown.run();
			}
		}, 0);
	}

	TXZWheelControlManager.OnTXZWheelControlListener onTXZWheelControlListener = new TXZWheelControlManager.OnTXZWheelControlListener() {
		@Override
		public void onKeyEvent(int eventId) {
			doReport(REPORT_ACTION_TYPE_WHEEL_CONTROL, "onKeyEvent", "" + eventId);
			if (onWheelControlKeyEvent(eventId)) {
				return;
			}
			switch (eventId) {
				case TXZWheelControlEvent.LEVOROTATION_EVENTID:
				case TXZWheelControlEvent.LEFT_KEY_CLICKED_EVENTID:
					onNavKeyCode( KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case TXZWheelControlEvent.DEXTROROTATION_EVENTID:
				case TXZWheelControlEvent.RIGHT_KEY_CLICKED_EVENTID:
					onNavKeyCode( KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
				case TXZWheelControlEvent.UP_KEY_CLICKED_EVENTID:
					onNavKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case TXZWheelControlEvent.DOWN_KEY_CLICKED_EVENTID:
					onNavKeyCode(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
					onKeyUp(KeyEvent.KEYCODE_DPAD_CENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
					break;
				case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
					onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
				default:
					break;
			}
		}
	};

	/**
	 * 弹出对话框
	 * 
	 * @param imediately
	 *            是否立即显示，否则按tts排队策略
	 */
	private void showInner(boolean imediately) {
		// 是否是立即打断的类型
		boolean preemptImediately = (PreemptType.PREEMPT_TYPE_IMMEADIATELY
				.equals(mBuildData.mPreemptType)
				|| PreemptType.PREEMPT_TYPE_FLUSH
						.equals(mBuildData.mPreemptType) || PreemptType.PREEMPT_TYPE_IMMEADIATELY_WITHOUT_CANCLE
				.equals(mBuildData.mPreemptType));
		// 会立即打断的则取消识别和录音
		if (preemptImediately || imediately) {
			AsrUtil.cancel();
			RecorderUtil.cancel();
		}
		
		TtsUtil.ITtsCallback callback = new TtsUtil.ITtsCallback() {
			@Override
			public void onBegin() {
				runOnUiGround(new Runnable() {
					@Override
					public void run() {
						WinDialog.this.mDialog.show();
						if (!WinDialog.this.mHasFocus) {
							WinDialog.this.mHasFocus = true;
							WinDialog.this.onGetFocusInner();
						}

						LogUtil.logd("onBeginTts: "
								+ WinDialog.this.getDebugString());

						mTimeBeginTts = SystemClock.elapsedRealtime();
						WinDialog.this.onBeginTts();
					}
				}, 0);
			}

			@Override
			public void onEnd() {
				runOnUiGround(new Runnable() {
					@Override
					public void run() {
						if (WinDialog.this.mBuildData.mScreenLockTime == SCREEN_LOCK_TYPE_DEFAULT) {
							WinDialog.this.cancelScreenLock();
						}

						LogUtil.logd("onEndTts: "
								+ WinDialog.this.getDebugString());
						mTimeEndTts = SystemClock.elapsedRealtime();
						WinDialog.this.onEndTts();
					}
				}, 0);
			}
		};

		PreemptType preemptType = mBuildData.mPreemptType;
		if (imediately && !preemptImediately) {
			preemptType = PreemptType.PREEMPT_TYPE_IMMEADIATELY;
		}

		// 不需要播报而需要立即显示
		if (mBuildData.mHintTts == null && (imediately || preemptImediately)) {
			if (mAsrTask != null) {
				mHintTtsTaskId = TtsUtil.speakVoice(TtsUtil.BEEP_VOICE_URL,
						preemptType, callback);
			} else {
				callback.onBegin();
				callback.onEnd();
			}
			return;
		}

		String tts = mBuildData.mHintTts != null ? mBuildData.mHintTts
				: DEFAULT_HINT_TTS;
		if (mAsrTask != null) {
			mHintTtsTaskId = TtsUtil.speakVoice(tts, TtsUtil.BEEP_VOICE_URL,
					preemptType, callback);
		} else {
			mHintTtsTaskId = TtsUtil.speakText(tts, preemptType, callback);
		}
	}

	/**
	 * 显示弹窗
	 */
	public void show() {
		if(!TextUtils.isEmpty(dialogTool)){
			mBuildData.addExtraInfo("reportId", getReportDialogId());
			mBuildData.addExtraInfo("clickCallback", "txz.dialog.click." + this.hashCode());
			ServiceManager.getInstance().sendInvoke( dialogTool,
					"tool.dialog.show." + this.hashCode(),
					mBuildData.getBuildData().getBytes(), null);
		}else {
			runOnUiGround(new Runnable() {
				@Override
				public void run() {
					showInner(false);
				}
			}, 0);
		}
	}

	/**
	 * 立即显示弹窗
	 */
	public final void showImediately() {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				showInner(true);
			}
		}, 0);
	}

	/**
	 * 撤销弹窗，由框架调用，不做数据上报
	 */
	protected final void dismissInner() {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				onDismissInner();
				try {
					// TXZ-13938，声控界面采用Activity实现时，概率性crash，增加判断保护
					Context context = mDialog.getContext();
					if (context instanceof Activity) {
						Activity act = (Activity) context;
						boolean isValidCtx = act.isFinishing();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
							isValidCtx |= act.isDestroyed();
						}
						if (isValidCtx) {
							mDialog.dismiss();
						}
					} else {
						mDialog.dismiss();
					}
				} catch (Exception e) {
					LogUtil.logd("dismissInner error, msg=" + e.getMessage());
				}
			}
		}, 0);
	}

	/**
	 * 撤销弹窗，提供弹窗原因，上报弹窗原因
	 */
	public final void dismiss(String reason) {
		doReport(REPORT_ACTION_TYPE_DISMISS, reason);
		dismissInner();
	}


	public static class FocusView {
		public View mView;
		public String mId;

		public FocusView(View view, String id) {
			if (view == null || TextUtils.isEmpty(id)) {
				throw new RuntimeException("view or id can't be null!");
			}
			mView = view;
			mId = id;
		}
	}

	/**
	 * 设置支持方控的Views<br/>
	 * View得到焦点或者失去焦点会在onFocusChange有回调，可以在回调中添加对应方控选中的效果，
	 * 或者直接在xml selector中定义也可以。
	 */
	public void setFocusViews(List<FocusView> views) {
		setFocusViews(views, 0);
	}

	/**
	 *
	 * @param views
	 * @param curFocusPosition
	 *            当前正处于的焦点位置，默认0，如果不希望立刻获取焦点可以设为-1
	 */
	public void setFocusViews(List<FocusView> views, int curFocusPosition) {
		mFocusViews = views;
		if (mFocusViews == null || mFocusViews.size() == 0) {
			mFocusPosition = -1;
			return;
		}
		mFocusPosition = curFocusPosition;
		updateFocus();
	}


	protected boolean mHasFocus = false;

	/**
	 * 是否处于焦点
	 * 
	 * @return
	 */
	public boolean hasFocus() {
		return mHasFocus;
	}

	/**
	 * 窗口焦点变化时回调
	 * 
	 * @param newFocus
	 */
	private void onWindowFocusChanged(boolean newFocus) {
		LogUtil.logd(this.getDebugString() + " onWindowFocusChanged: from "
				+ mHasFocus + " to " + newFocus);

		if (mHasFocus != newFocus) {
			mHasFocus = newFocus;
			if (mHasFocus) {
				onGetFocusInner();
			} else {
				onLoseFocusInner();
			}
		}
	}

	/**
	 * 丢失焦点
	 */
	private void onLoseFocusInner() {
		cancelHintTts();
		if (mAsrTask != null) {
			AsrUtil.recoverWakeupFromAsr(mAsrTask.getTaskId());
		}
		if (mBuildData.mScreenLockTime == SCREEN_LOCK_TYPE_FOCUS) {
			cancelScreenLock();
		}

		LogUtil.logd("onLoseFocus: " + this.getDebugString());
		onLoseFocus();

		if (this.mBuildData.mStopCountDownWhenLoseFocus) {
			// 丢失焦点时停止倒计时
			endCountDownInner(COUNT_DOWN_END_REASON_LOSE_FOCUS);
		}
	}

	private boolean mFirstGetFocus = true;

	/**
	 * 获取焦点
	 */
	private void onGetFocusInner() {
		if (mAsrTask != null) {
			AsrUtil.useWakeupAsAsr(mAsrTask);
		}
		TXZWheelControlManager.getInstance().registerWheelControlListener(onTXZWheelControlListener);
		if (mFirstGetFocus) {
			LogUtil.logd("onShow: " + WinDialog.this.getDebugString());

			mFirstGetFocus = false;
			if (mBuildData.mScreenLockTime != SCREEN_LOCK_TYPE_NONE
					&& mBuildData.mScreenLockTime != SCREEN_LOCK_TYPE_FOCUS) {
				mRequestScreenLockRunnable.run(); // 这个运行在ui线程，直接调用runnable
				if (mBuildData.mScreenLockTime > 0) {
					cancelScreenLockInner(mBuildData.mScreenLockTime);
				} else if (mBuildData.mScreenLockTime == SCREEN_LOCK_TYPE_POPUP) {
					// 点亮屏幕后立即释放
					cancelScreenLock();
				}
			}
			mTimeShow = SystemClock.elapsedRealtime();
			onShow();
			WinDialog.this.mDialog.getContext().sendBroadcast(
					new Intent("com.txznet.txz.action.FLOAT_WIN_SHOW"));
		}
		if (mBuildData.mScreenLockTime == SCREEN_LOCK_TYPE_FOCUS) {
			requestScreenLock();
		}

		LogUtil.logd("onGetFocus: " + this.getDebugString());
		onGetFocus();
	}

	/**
	 * 失去焦点的回调
	 */
	protected void onLoseFocus() {

	}

	/**
	 * 获得焦点的回调
	 */
	protected void onGetFocus() {

	}

	/**
	 * 播报开始回调
	 */
	protected void onBeginTts() {

	}

	/**
	 * 播报开始回调
	 */
	protected void onEndTts() {

	}

	/**
	 * 获取调试字符串
	 * 
	 * @return
	 */
	public String getDebugString() {
		return this.toString() + "[" + mBuildData.mHintTts + "]";
	}

	/**
	 * 获取对话框的数据上报的ID，业务对话框必须重写这个方法区分对话框的应用场景
	 * 
	 * @return
	 */
	public abstract String getReportDialogId();

	/**
	 * 返回对话框基础类型
	 * 
	 * @return
	 */
	public String getDialogType() {
		Class<?> clazz = this.getClass();
		while (clazz.isAnonymousClass()) {
			clazz = clazz.getSuperclass();
		}
		return clazz.getSimpleName();
	}

	/**
	 * 视图转成上报的id
	 * 
	 * @param viewId
	 *            将发生事件的视图对象转换为参数
	 * @return 视图命名id
	 */
	public String getReportViewId(int viewId) {
		return DEFAULT_VIEW_REPORT_ID;
	}

	/**
	 * 点击行为类型，参数携带点击的按钮id，每个对话框自己定义，内置空白处"blank"
	 */
	public final static String REPORT_ACTION_TYPE_CLICK = "click";
	/**
	 * 点击的目标区域为空白处
	 */
	public final static String REPORT_ACTION_PARAM_CLICK_BLANK = "blank";
	/**
	 * 语音行为类型，参数携带识别的语音文本和触发的按钮id
	 */
	public final static String REPORT_ACTION_TYPE_SPEAK = "speak";
	/**
	 * 方控或按键触发操作，参数携带触发的点击的按钮id
	 */
	public final static String REPORT_ACTION_TYPE_KEY = "key";
	/**
	 * 返回按键，不携带参数
	 */
	public final static String REPORT_ACTION_TYPE_BACK = "back";
	/**
	 * HOME按键，不携带参数
	 */
	public final static String REPORT_ACTION_TYPE_HOME = "home";
	/**
	 * 倒计时到期，不用携带参数
	 */
	public final static String REPORT_ACTION_TYPE_COUNTDOWN = "countdown";
	/**
	 * 业务操作主动关闭，不用携带参数
	 */
	public final static String REPORT_ACTION_TYPE_DISMISS = "dismiss";
	/**
	 * View焦点变化
	 */
	public final static String REPORT_ACTION_TYPE_FOCUS = "focus";
	/**
	 * WheelControl腾讯方控上报
	 */
	public final static String REPORT_ACTION_TYPE_WHEEL_CONTROL = "wheelControl";

	/**
	 * 构造通用上报参数
	 * 
	 * @return
	 */
	protected JSONBuilder getCommReport() {
		long t = SystemClock.elapsedRealtime();
		JSONBuilder json = new JSONBuilder();
		// 填写对话框类型
		json.put("id", getReportDialogId());
		if (mTimeShow > 0) {
			// 窗口显示经过的时间
			json.put("showTime", t - mTimeShow);
		}
		if (mBuildData.mHintTts != null) {
			// 窗口播报的文本
			json.put("tts", mBuildData.mHintTts);
			if (mTimeEndTts > 0) {
				// TTS播报结束经过的时间，负数
				json.put("ttsTime", mTimeEndTts - t);
			} else if (mTimeBeginTts > 0) {
				// TTS播报结束经过的时间
				json.put("ttsTime", t - mTimeBeginTts);
			}
		}
		if (mTimeBeginCountDown > 0) {
			CountDownRunnable run = mRunnableCountDown;
			// 倒计时经过的时间
			if (run != null) {
				json.put("countdown", run.getCount());
				json.put("countdownTime", t - mTimeBeginCountDown);
			}
		}
		return json;
	}

	/**
	 * 上报数据
	 * 
	 * @param type
	 *            上报的对话框事件类型
	 * @param param
	 *            上报的对话框事件类型的参数列表
	 */
	public void doReport(String type, String... param) {
		JSONBuilder json = getCommReport();
		json.put("type", type);
		json.put("param", param);
		ReportUtil.doReport(ReportManager.UAT_DIALOG, json.toBytes());
		// LogUtil.logd("doReport:\n" + json.toString());
	}
}