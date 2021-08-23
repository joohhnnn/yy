package com.txznet.txz.module.ui.parse;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.ViewPluginUtil;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.ui.view.PluginViewFactory;
import com.txznet.txz.module.ui.view.plugin.sample.TTSNoResultView;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.module.weixin.WeixinManager.OnQRCodeListener;
import com.txznet.txz.plugin.interfaces.AbsTextJsonParse;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.runnables.Runnable2;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class TTSNoResultParse extends AbsTextJsonParse implements IChoice<String> {
	public TTSNoResultView mBPView;
	private String data;
	private OnQRCodeListener mListener = new OnQRCodeListener() {

		@Override
		public void onGetQrCode(boolean isBind, String url) {
			if (!mIsShowing) {
				return;
			}

			if (WinManager.getInstance().isRecordWin2()) {
				if (!isBind) {
					JSONBuilder jsonBuilder = new JSONBuilder(data);
					JSONBuilder value = new JSONBuilder(jsonBuilder.getVal("value", String.class));
					value.put("qrCode", url);
					jsonBuilder.put("value", value.toString());
					RecorderWin.showData(jsonBuilder.toString());
				} else {
					clearIsSelecting();
					String text = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_HAS_BINDQR");
					AsrManager.getInstance().setNeedCloseRecord(true);
					speechId = RecorderWin.speakTextWithClose(text, null);
				}
			} else {
				JNIHelper.logd("onGetQrCode:[isBind:" + isBind + ",url:" + url + "]");
				if (!isBind && mBPView != null) {
					mBPView.refreshQRCodeView(url);
				} else {
					clearIsSelecting();
					String text = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_HAS_BINDQR");
					AsrManager.getInstance().setNeedCloseRecord(true);
					speechId = RecorderWin.speakTextWithClose(text, null);
				}
			}
		}
	};

	public TTSNoResultParse() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {

			@Override
			public void onShow() {
			}

			@Override
			public void onDismiss() {
				clearIsSelecting();
			}
		});

		WeixinManager.getInstance().addOnQrCodeListener(mListener);
	}
	
	/**
	 * 显示二维码
	 * @param hasThirdImpl
	 * @param strData
	 * @return
	 */
	public boolean showQR(boolean hasThirdImpl, String strData) {
		if (!acceptText(hasThirdImpl, strData)) {
			return false;
		}

		parseStrData(strData);
		return true;
	}

	@Override
	public boolean acceptText(boolean hasThirdImpl, String strData) {
		JSONBuilder jb = new JSONBuilder(strData);
		Integer key = jb.getVal("key", Integer.class);
		if (key != null && key == AbsTextJsonParse.TYPE_TTS_NO_RESULT) {
			return true;
		}

		return false;
	}

	@Override
	public int parseStrData(String value) {
		data = value;

		if (WinManager.getInstance().isRecordWin2()) {
			RecorderWin.showData(value);
			mIsShowing = true;
			RecorderWin.show();
			flushQr();
		}else {
			JSONBuilder jb = new JSONBuilder(value);
			value = jb.getVal("value", String.class);
			mBPView = (TTSNoResultView) PluginViewFactory.genPluginViewByJson(PluginViewFactory.TYPE_TTS_NO_RESULT_VIEW,
					value);
			mBPView.findViewById(TTSNoResultView.ID_QRCODE_VIEW).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					flushQr();

					RecorderWin.dealRecorderUIEvent("txz.record.ui.event.list.ontouch",
							(MotionEvent.ACTION_DOWN + "").getBytes());
				}
			});
			mIsShowing = true;
			RecorderWin.show();
			flushQr();
			
			ViewPluginUtil vpu = WinManager.getInstance().getViewPluginUtil();
			vpu.invokePluginCommand("addPlugin", "TYPE_TTS_NORESULT_VIEW_ID", mBPView, true, true);
		}
		
		beginWakeupAsr();
		String txt = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_NO_RESULT_SPK");
		TtsManager.getInstance().cancelSpeak(speechId);
		speechId = TtsManager.INVALID_TTS_TASK_ID;
		speechId = TtsManager.getInstance().speakVoice(txt, InterruptTts.getInstance().isInterruptTTS()?"":TtsManager.BEEP_VOICE_URL,new TtsUtil.ITtsCallback() {
			@Override
			public boolean isNeedStartAsr() {
				return true;
			}
		});
		return TextParseResult.ERROR_SUCCESS;
	}
	
	static int speechId = TtsManager.INVALID_TTS_TASK_ID;

	private void beginWakeupAsr() {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			Runnable removeBackground = null;
			Runnable2<Object, String> taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;
			@Override
			public boolean needAsrState() {
				if (InterruptTts.getInstance().isInterruptTTS()) {//如果是识别模式，就不需要开启beep音
					return false;
				}else {
					return true;
				}
			}

			@Override
			public String getTaskId() {
				return "TASK_TTS_NORESULT";
			}
			
			@Override
			public void onSpeechEnd() {
				super.onSpeechEnd();
				mLastSpeechEndTime = SystemClock.elapsedRealtime();
				if(removeBackground != null){
					AppLogic.removeBackGroundCallback(removeBackground);
				}
			}

			@Override
			public void onCommandSelected(String type, String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				
				taskRunnable = new Runnable2<Object, String>(type,command) {
					
					@Override
					public void run() {
						
						JNIHelper.logd("do onCommandSelected");
						
						String type = (String) mP1;
						String command = mP2;
						isEnd = true;
						if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
							//唤醒结果执行时，如果还在录音，则取消掉
							if (AsrManager.getInstance().isBusy()) {
								AsrManager.getInstance().cancel();
							}
						}
						if (type.equals(getTaskId() + "$QUXIAO")) {
							clearIsSelecting();
							RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
						} else if (type.equals(getTaskId() + "$FLUSHQR")) {
							// 刷新二维码
							flushQr();
						}
					}
				};
				removeBackground = new Runnable() {
	
					@Override
					public void run() {
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
					if (isWakeupResult()) {//是唤醒的结果
						isEnd = false;
						//判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						}else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);							
						}
					} else if (!isEnd) {//识别到的唤醒词并且唤醒没有执行完成
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				}else {
					taskRunnable.run();
				}
			}
		};
		acsc.addCommand(acsc.getTaskId() + "$QUXIAO", NativeData.getResStringArray("RS_TTS_THEME_CMD_CANCEL"));
		acsc.addCommand(acsc.getTaskId() + "$FLUSHQR", NativeData.getResStringArray("RS_TTS_THEME_CMD_FLUSH"));

		mHasWakeupAsr = true;
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}

	private void flushQr() {
		JNIHelper.logd("TTSNoResultParse flushQr");
		// 刷新二维码
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL);
	}

	public static boolean mHasWakeupAsr;
	private boolean mIsShowing;

//	public static void clear() {
//		if (mHasWakeupAsr) {
//			mHasWakeupAsr = false;
//			mIsShowing = false;
//			TtsManager.getInstance().cancelSpeak(speechId);
//			speechId = TtsManager.INVALID_TTS_TASK_ID;
//			WakeupManager.getInstance().recoverWakeupFromAsr("TASK_TTS_NORESULT");
//		}
//	}

	@Override
	public void showChoices(String data) {
	}

	@Override
	public boolean isSelecting() {
		return mIsShowing;
	}

	@Override
	public void clearIsSelecting() {
		if (mHasWakeupAsr) {
			mHasWakeupAsr = false;
			mIsShowing = false;
			TtsManager.getInstance().cancelSpeak(speechId);
			speechId = TtsManager.INVALID_TTS_TASK_ID;
			WakeupManager.getInstance().recoverWakeupFromAsr("TASK_TTS_NORESULT");
		}
	}
}