package com.txznet.txz.component.reminder;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.R;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.BeepPlayer;

import org.json.JSONException;
import org.json.JSONObject;

public class ReminderPushView extends LinearLayout{
	private View mLayout;
	private TextView tvContent, tvBtn;
	protected WindowManager.LayoutParams mLp;
	protected WindowManager mWinManager;
	protected int mWidth;
	protected int mHeight;
	private static boolean mIsOpening = false;
	private String mText = "";
	public static int mSpeechTaskId;
	
	private static final String TASK_REMINDER_KWS = "ReminderKws";

	private static ReminderPushView mInstance;

	private ReminderPushView(Context context) {
		super(context);
		mWinManager = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
		mLayout = getLayoutView();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) getResources().getDimension(R.dimen.x150);
		params.rightMargin = (int) getResources().getDimension(R.dimen.x150);
		addView(mLayout, params);
	}

	public static void showPushView(String strJson){
		if(mInstance == null){
			mInstance = new ReminderPushView(GlobalContext.get());
		}
		String text = "";
		try {
			JSONObject json = new JSONObject(strJson);
			text = json.optString("text");
		} catch (JSONException e) {
		}
		mInstance.setText(text);
		mInstance.open();
	}

	public static void closePushView(){
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		if(mInstance != null && mInstance.isShowing()){
			mInstance.dismiss();
		}
	}

	private View getLayoutView() {
		View layout = View.inflate(getContext(), R.layout.reminder_push_view, null);
		tvContent = (TextView) layout.findViewById(R.id.tv_reminder_push_title);
		tvBtn = (TextView) layout.findViewById(R.id.tv_reminder_push_tips);
		tvBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TtsManager.getInstance().cancelSpeak(TtsManager.getInstance().getCurTaskId());
				dismiss();
			}
		});
		return layout;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				mWidth = mLayout.getLayoutParams().width;
				mHeight = mLayout.getLayoutParams().height;
				mLp.width = mWidth;
				mLp.height = mHeight;
				mWinManager.updateViewLayout(ReminderPushView.this, mLp);
				return false;
			}
		});
	}
	
	/**
	 * 生成唤醒识别任务
	 */
	private void genAsrTask(){
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			@Override
			public boolean needAsrState() {
				return false;
			}
			
			@Override
			public String getTaskId() {
				return TASK_REMINDER_KWS;
			}
			
			@Override
			public void onCommandSelected(String type, String command) {
				if("closeReminder".equals(type)){
					dismiss();
					TtsManager.getInstance().cancelSpeak(TtsManager.getInstance().getCurTaskId());
				}else if("navi".equals(type)){
					ReminderManager.getInstance().naviReminder();
				}
			}
		}.addCommand("closeReminder", new String[]{"退出","关闭","取消"}).addCommand("navi", new String[]{"导航过去"});
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}
	
	public void open() {
		if(mIsOpening){
			return;
		}
		mIsOpening = true;
		mLp = new WindowManager.LayoutParams();
		mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
		mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
		mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
		mLp.horizontalMargin = getResources().getDimension(R.dimen.x50);
		mLp.flags = 40;
		mLp.format = PixelFormat.RGBA_8888;
		mLp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
		mWinManager.addView(this, mLp);
		genAsrTask();
		speakText();
	}
	
	public void setText(String text){
		mText = text;
	}
	
	private void speakText() {
		BeepPlayer.play(new Runnable() {
			@Override
			public void run() {
				mSpeechTaskId = TtsManager.getInstance().speakText(mText, new ITtsCallback() {
					@Override
					public void onCancel() {
						dismiss();
					}

					@Override
					public void onError(int iError) {
						dismiss();
					}

					@Override
					public void onSuccess() {
						AppLogic.runOnUiGround(new Runnable() {

							@Override
							public void run() {
								dismiss();
							}
						}, 3000);
					}
				});
			}
		});
	}

	public boolean isShowing() {
		return this.mIsOpening;
	}

	Runnable disMissRunable = new Runnable() {
		@Override
		public void run() {
			if (mIsOpening) {
				mWinManager.removeView(ReminderPushView.this);
				mIsOpening = false;
				WakeupManager.getInstance().recoverWakeupFromAsr(TASK_REMINDER_KWS);
				AppLogic.removeUiGroundCallback(disMissRunable);
			}
		}
	};

	public void dismiss() {
		AppLogic.runOnUiGround(disMissRunable);
	}
	
}
