package com.txznet.txz.notification;

import org.json.JSONException;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.notification.NotificationInfo;
import com.txznet.comm.notification.TrafficNotificationInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.text.TextSemanticAnalysis;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class TrafficNotification extends INotification  implements View.OnClickListener {

	final static int ICON_STATUS_PLAY= 1;
	final static int ICON_STATUS_DEALING= 2;
	final static int ICON_STATUS_RECORD= 3;
	
	private IRecordView mRecorde;
	private TextView mTextView;
	private int speakTextId=-1;
//	private IChatFromSysView chat;
	private TextView mTxMsg;
	private ImageView mIvIcon;
	

	public TrafficNotification(Context context,NotificationInfo notificationInfo) {
		super(context, notificationInfo);
		initView(context);
	}

	@Override
	public void initView(Context context) {
		int i=0;
		
		DisplayMetrics dm = getResources().getDisplayMetrics();
		int displayWidth = dm.widthPixels;
		int displayHeight = dm.heightPixels;
		View content=null;
		TrafficNotificationInfo info = (TrafficNotificationInfo) mNotificationInfo;
		LayoutInflater.from(context).inflate(R.layout.win_traffice_notification, this);
		try {
			content = (View)findViewById(R.id.rl_traffic);
			mIvIcon = (ImageView)findViewById(R.id.iv_icon);
			mTxMsg=(TextView)findViewById(R.id.tx_msg);

			Float textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_FROM_SYS);
			int textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_FROM_SYS);
			mTxMsg.setTextSize(textSize);
			mTxMsg.setTextColor(textColor);
			mIvIcon.setImageResource(R.drawable.notification_traffiction);
			if(displayWidth<=1000)
				mWidth = displayWidth;
			else
				mWidth = (int)(displayWidth*0.6);
			
			if(displayHeight<=490)
				mHeight = 88;
			else
				mHeight = 100;
	        
	        changetheMsg(info.msgId,ICON_STATUS_PLAY);
	        
	        this.setOnClickListener(this);
	        this.setEnabled(true);
		} catch (Exception e) {
			
			e.printStackTrace();
		} 
	}

	public void show(){
		super.show();
		TrafficNotificationInfo info = (TrafficNotificationInfo) mNotificationInfo;
		speakTextId = TtsManager.getInstance().speakText(info.trafficDetail,new ITtsCallback() {
			@Override
			public void onCancel() {
				super.onCancel();
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						dismiss();
					}
				});
			}

			@Override
			public void onSuccess() {
				setEnabled(false);
				setClickable(false);
				speakTextId=-1;
				new Handler().postDelayed(new Runnable(){
					
					@Override
					public void run() {
						speakNavPoi();					
					}
				}, 0);
			super.onSuccess();
			}
		});
		if (!mRegisted) {
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
		}
	}
	private boolean mRegisted = false;
	private HomeObservable.HomeObserver mHomeObserver = new HomeObservable.HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss();
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			dismiss();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private boolean mFromClick = false;

	private void speakNavPoi(){
		this.setEnabled(false);
		setClickable(false);
		if(speakTextId!=-1){		
			TtsManager.getInstance().cancelSpeak(speakTextId);
			speakTextId=-1;
			mFromClick = true;
		}
//		dismiss();
		if(NavManager.getInstance().getLocalNavImpl().isInNav() || WeixinManager.getInstance().getIsInQuickNav()){
			dismiss();
			return;
		}
		final String spk= NativeData.getResString("RS_TRAFFIC_RESULT_PLAYEND");
		changetheMsg(spk,ICON_STATUS_RECORD);
		speakTextId = TtsManager.getInstance().speakText(spk,new ITtsCallback(){
			@Override
			public void onCancel() {
				super.onCancel();
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						dismiss();
					}
				});
			}

			@Override
			public void onSuccess() {		
				AsrOption asrOption = new AsrOption();
				TextSemanticAnalysis.needHandleSemanticAnalysisResult = false;
				AsrManager.getInstance().start(asrOption.setCallback(new IAsrCallback() {

					@Override
					public void onSuccess(AsrOption option,
							VoiceParseData oVoiceParseData) {
						String text=null;
						JNIHelper.logd("AsrManager onSuccess:oVoiceParseData.strVoiceData  "+oVoiceParseData.strVoiceData);
						//为了解决onSuccess回调回来未处理后的结果，原来的判断有问题导致重复播报
						boolean bEmpty = false;
						try {
							JSONBuilder js = new JSONBuilder(oVoiceParseData.strVoiceData);
							if(js.build().has("text")){
								text = js.getVal("text", String.class);
								bEmpty = TextUtils.isEmpty(text);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						if(bEmpty){
							RecorderWin.close();
							String spk = NativeData.getResString("RS_VOICE_EMPTY_CLOSE");
							changetheMsg(spk, ICON_STATUS_RECORD);
							speakTextId = TtsManager.getInstance().speakText(spk, new ITtsCallback() {

								@Override
								public void onEnd() {
									dismiss();
								}
							});
							return ;
						}
						AsrManager.getInstance().cancel();
						if (!RecorderWin.isOpened()) {
							RecorderWin.show();
						}
						RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
					}
					@Override
					public void onEnd(AsrOption option) {
						super.onEnd(option);
						JNIHelper.logd("AsrManager onEnd:speech ");
//						changetheMsg("处理中", ICON_STATUS_DEALING);
					}
					@Override
					public void onError(AsrOption option, int error,
							String desc, String speech, int error2) {
						// TODO Auto-generated method stub
						super.onError(option, error, desc, speech, error2);
						JNIHelper.logd("AsrManager onError:speech "+speech+" desc "+desc+" error2="+error2+" error="+error);
						String spk = null;
						switch (error2) {
						case IAsr.ERROR_ASR_NET_REQUEST:
							spk = NativeData.getResString("RS_VOICE_UNKNOW_LOCAL");
							break;
						case IAsr.ERROR_NO_SPEECH:
							spk = NativeData.getResString("RS_VOICE_EMPTY_CLOSE");
							break;
						default:
							break;
						}
						changetheMsg(spk, ICON_STATUS_RECORD);
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								dismiss();
							}
						};
						AppLogic.runOnUiGround(runnable, 3000);
					}

					@Override
					public void onCancel(AsrOption option) {
						super.onCancel(option);
						JNIHelper.logd("AsrManager onCancel:speech ");
						AppLogic.runOnUiGround(new Runnable() {
							@Override
							public void run() {
								dismiss();
							}
						});
					}

					@Override
					public void onAbort(AsrOption option, int error) {
						super.onAbort(option, error);
						JNIHelper.logd("AsrManager onAbort:speech ");
						AppLogic.runOnUiGround(new Runnable() {
							@Override
							public void run() {
								dismiss();
							}
						});
					}

				}));
			}
		});
	}
	private void changetheMsg(String str,int recordStatus){
		mTxMsg.setText(str);
		int id=0;
		switch (recordStatus) {
		case ICON_STATUS_DEALING:
		case ICON_STATUS_PLAY:
			id=R.drawable.notification_traffiction;
			break;
		case ICON_STATUS_RECORD:
			id=R.drawable.notification_recording;
			break;

		default:
			id=R.drawable.notification_traffiction;
			break;
		}
		mIvIcon.setImageResource(id);
	}
	@Override
	public void notifyMessage(NotificationInfo notificationInfo) {
		
	}

	@Override
	public void onClick(View v) {
		speakNavPoi();
	}
	@Override
	public void dismiss() {
		if(mFromClick){
			mFromClick = false;
			return;
		}
		AsrManager.getInstance().cancel();
		TtsManager.getInstance().cancelSpeak(speakTextId);
		NavAmapValueService.getInstance().setEnableAutoPupUp(true);
		TextSemanticAnalysis.needHandleSemanticAnalysisResult = true;
		if (mRegisted) {
			mRegisted = false;
			try {
				GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			}catch (Exception e) {

			}
		}
		super.dismiss();
	}

}
