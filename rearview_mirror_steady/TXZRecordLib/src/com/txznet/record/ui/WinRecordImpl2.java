package com.txznet.record.ui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.ScrollObservable.OnSizeObserver;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatContentAdapter;
import com.txznet.record.adapter.ChatContentAdapter.ChatMsgListener;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.HelpTipMsg;
import com.txznet.record.bean.PluginMessage;
import com.txznet.record.bean.PluginMessage.PluginData;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.lib.R;
import com.txznet.record.util.SPUtil;
import com.txznet.record.view.DisplayLvRef;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

public class WinRecordImpl2 extends WinDialog implements IWinRecord{
	public static final int SHOW_DISPLAY = 0x01;
	public static final int HIDE_DISPLAY = 0x02;
	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画

	private static WinRecordImpl2 mInstance;

	private LinkedList<ChatMessage> mChatMsgs;
	private ChatContentAdapter mChatAdapter;
	private ScreenLock mScreenLock;

	// private WaveformView mWaveformView; // 声纹动画
//	private RecStepView mRecStepView;
	private ListView mChatContent; // 聊天列表
	// private ImageView mWaveIcon; // 录音图标
	// private View mWaiting; // 等待处理
	// private View mWaitingFullStyle; // 满圈遮罩
	private DisplayLvRef mDisplayChatLv;
	private DisplayListRefresher mDisplayListRefresher;

	private LinearLayout mPluginLayout;
	private LinearLayout mBannerAdvertisingLayout;//banner广告

	private View mClose; // 退出按钮
	private View mImgHelpView;//帮助
	private View mSettings;//设置
	private View mSettingsTop;//设置
	private boolean mIsShowLv;
	private ImageView mHelpNewTag;
	// private Handler mUiHandler;

	// 防止多次点击帮助按钮的锁
	private boolean mMultiHelpClickLock = false;
	private boolean mUseMockBar;
	
	private boolean mIfSetWinBg = false; // 是否将背景图设置为窗口背景
	
	private static int bgColor = 0;
	
	private static boolean mIsFullScreen = false;
	private static int mType = -1;
	private static int mWinFlags = 0;	
	private static int  mContentWidth = 0;
	private static int mWinSoft = 0;
	
	private static Boolean mDialogCancelable = null;

	public static WinRecordImpl2 getInstance() {
		if (mInstance == null) {
			synchronized (WinRecordImpl2.class) {
				if (mInstance == null) {
					mInstance = new WinRecordImpl2();
				}
			}
		}
		return mInstance;
	}

	public void setWinType(int type) {
		LogUtil.logd("setWinType :" + type);
		mType = type;
		getWindow().setType(type);
	}
	
	@Override
	public void setWinSoft(int soft) {
		LogUtil.logd("setWinSoft :" + soft);
		mWinSoft = soft;
		getWindow().setSoftInputMode(soft);
		LogUtil.logd("setWinSoft over :" + soft);
	}

	private static Float mWinBgAlpha = null;
	
	public void setWinBgAlpha(Float winBgAlpha) {
		if (winBgAlpha < 0.0f || winBgAlpha > 1.0f)
			return;
		mWinBgAlpha = winBgAlpha;
		LogUtil.logi("setWinBgAlpha:" + winBgAlpha);
		
		//将用户设置过来的透明度， 和原来的 argb值 计算
		int iWinBgAlpha = (int) (mWinBgAlpha * 255);
		String adapterAlpha = Integer.toHexString(iWinBgAlpha);
		String coreColor = Integer.toHexString(GlobalContext.getModified().getResources().getColor(R.color.win_bg));
		adapterAlpha = adapterAlpha + coreColor.substring(2);
//		bgColor = Integer.valueOf(adapterAlpha , 16);  // 这个转换  99000000的会出错， 
		bgColor = Color.parseColor("#" + adapterAlpha.trim());
		LogUtil.logi("iAlpha = " + iWinBgAlpha + " adapterAlpha=" + adapterAlpha + " coreColor=" + coreColor + " bgColor=" + bgColor);

	}

	public void setWinFlags(int flags) {
		LogUtil.logd("setWinFlags :" + flags);
		mWinFlags = flags;
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags = flags;
		getWindow().setAttributes(attrs);
	}
	
	@Override
	public void setIsFullSreenDialog(boolean isFullScreen) {
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		if (isFullScreen != mIsFullScreen) {
			mInstance = new WinRecordImpl2(isFullScreen);
			if (mCycleObserver != null) {
				mInstance.setWinRecordObserver(mCycleObserver);
			}
			mIsFullScreen = isFullScreen;
		}
	}

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	public WinRecordImpl2() {
		this(false);
	}

	public WinRecordImpl2(boolean isFull) {
		super(true, isFull);
		initWinRecord();
		if(bgColor == 0) {
			bgColor = GlobalContext.getModified().getResources().getColor(R.color.win_bg);
		}
		if (mWinFlags != 0) {
			setWinFlags(mWinFlags);
		}
		if (mType != -1) {
			setWinType(mType);
		}
		if(mWinSoft!=0){
			setWinSoft(mWinSoft);
		}
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return KeyEventManagerUI1.getInstance().onKeyEvent(event);
	}

	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 LogUtil.logd("[UI1.0] onKeyDown:" + keyCode);
//		 return KeyEventManagerUI1.getInstance().onKeyEvent(keyCode);
		 return onKeyDown(keyCode,event);
	 }

	/**
	 * 真正的初始化窗口
	 */
	public void realInit(){
		try {
			Rect outRect = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		} catch (Exception e) {
			LogUtil.logd("realInit WinRecord failed");
		}
	}
	private int mWidth;
	private int mHeight;

	private void initWinRecord() {
		mUseMockBar = getContext().getResources().getBoolean(R.bool.useMockBar);
		if (mUseMockBar) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
					| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
					| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		} else {
			getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
					| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE
					| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
		if (mDialogCancelable != null) {
			setCancelable(mDialogCancelable);
		}
		initChatMsgs(null);
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.back", null,
						null);
				dismiss();
			}
		});
		mScreenLock = new ScreenLock(getContext());
		if (mUseMockBar) {
			insertCustomBarView();
		}
		/*
		 * mUiHandler = new Handler(Looper.getMainLooper()){
		 * 
		 * @Override public void handleMessage(Message msg) { if (msg.what ==
		 * SHOW_DISPLAY) { mIsShowLv = true;
		 * mChatContent.setVisibility(View.INVISIBLE);
		 * mDisplayListRefresher.setDisplayVisible(View.VISIBLE);
		 * broadCastDisplayLvVisible(true); }
		 * 
		 * if (msg.what == HIDE_DISPLAY) { mIsShowLv = false;
		 * mDisplayListRefresher.setDisplayVisible(View.INVISIBLE);
		 * mChatContent.setVisibility(View.VISIBLE);
		 * broadCastDisplayLvVisible(false); } } };
		 */
	}

	private void setDisplayListVisible(final boolean show) {
		AppLogicBase.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (show) {
					mIsShowLv = true;
					mChatContent.setVisibility(View.GONE);
					mPluginLayout.setVisibility(View.GONE);
					mDisplayListRefresher.setDisplayVisible(View.VISIBLE);
					broadCastDisplayLvVisible(true);
				} else {
					mIsShowLv = false;
					mDisplayListRefresher.setDisplayVisible(View.GONE);
					mPluginLayout.setVisibility(View.GONE);
					mChatContent.setVisibility(View.VISIBLE);
					broadCastDisplayLvVisible(false);
				}
			}
		}, 0);
	}

	private void insertCustomBarView() {
		// 插入一个背景色
		ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
		View v1 = new View(getContext());
		v1.setBackgroundColor(getContext().getResources().getColor(R.color.mockbarColor));
		int barHeight = getStatusBarHeight(getContext());
		View v = findViewById(R.id.inner_Content);
		FrameLayout.LayoutParams lp = (LayoutParams) v.getLayoutParams();
		lp.topMargin = barHeight;
		v.setLayoutParams(lp);
		lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, barHeight);
		mContentView.addView(v1, 0, lp);
	}

	private int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	BroadcastReceiver mWxResReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mDisplayListRefresher != null) {
				mDisplayListRefresher.notifyDataSetChanged();
			}
		}
	};
	
	private View content;
	
	private static RelativeLayout rlRecord_Wave_Panel = null;
	private static ImageView ivRecordShade = null;
	private static ImageView ivRecordBack = null;
	private static ImageView ivRecordModule = null;
	private static AnimationDrawable rocketAnimation = null;
	private static View llRecordBackground = null;

	private  IconStateChangeListener getIconStateChangeListener() {
		if(mIconStateChangeListener == null) {
			mIconStateChangeListener = new IconStateChangeListener() {
				@Override
				public void onStateChanged(int type, boolean enable) {
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							if (mClose == null || mSettings == null || mSettingsTop == null || mHelpNewTag == null) {
								return;
							}
							LogUtil.logd("isShowCloseIcon = " + ConfigUtil.isShowCloseIcon() + "; isShowSettings = " + ConfigUtil.isShowSettings());
							if (ConfigUtil.isShowCloseIcon()) {
								mClose.setVisibility(View.VISIBLE );
								if (ConfigUtil.isShowSettings()) {
									mSettingsTop.setVisibility(View.GONE);
									mSettings.setVisibility(View.VISIBLE);
								}else {
									mSettingsTop.setVisibility(View.GONE);
									mSettings.setVisibility(View.GONE);
								}
							}else {
								mClose.setVisibility(View.GONE );
								if (ConfigUtil.isShowSettings()) {
									mSettingsTop.setVisibility(View.VISIBLE);
									mSettings.setVisibility(View.GONE);
								}else {
									mSettingsTop.setVisibility(View.GONE);
									mSettings.setVisibility(View.GONE);
								}
							}

							if (mImgHelpView != null) {
								if (ConfigUtil.isShowHelpInfos()) {
									mImgHelpView.setVisibility(View.VISIBLE);
								} else {
									mImgHelpView.setVisibility(View.GONE);
								}
							}
							//需要延时500毫秒
							mHelpNewTag.removeCallbacks(updateHelpNew);
							mHelpNewTag.postDelayed(updateHelpNew, 500);
						}
					}, 0);
				}
			};
		}
		return mIconStateChangeListener;
	}
	
	IconStateChangeListener mIconStateChangeListener;

	private int getConfigValue(String strData,int def){
		int iData = def;
		if (!TextUtils.isEmpty(strData)) {
			try {
				int data = Integer.parseInt(strData);
				if (data > 0 ) {
					iData = data;
				}
			}catch (Exception e) {
			}
		}
		return iData;
	}

	@SuppressLint({ "InflateParams", "NewApi" })
	@Override
	protected View createView() {
		Rect outRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		mWidth = outRect.width();
		mHeight = outRect.height();
		ScreenUtil.initScreenType(getWindow().getDecorView());
		//直接将1.0计算到的屏幕类型赋值给ConfigUtil.mCurType，字体大小，颜色还是从2.0的框架获取的
		com.txznet.comm.ui.util.ConfigUtil.mCurType = ScreenUtil.getScreenType();
		   
		GlobalContext.getModified().getTheme().applyStyle(getScreenTheme(), true);
		GlobalContext.getModified().getTheme().applyStyle(getLayoutTheme(), true);
		GlobalContext.get().getTheme().applyStyle(getScreenTheme(), true);
		GlobalContext.get().getTheme().applyStyle(getLayoutTheme(), true);
		if (getContext() != GlobalContext.get()) {
			getContext().getTheme().applyStyle(getScreenTheme(), true);
			getContext().getTheme().applyStyle(getLayoutTheme(), true);
		}
		int mLayoutId = R.layout.win_record;
		switch (ScreenUtil.getLayoutType()) {
		case ScreenUtil.LAYOUT_TYPE_HORIZONTAL:
			mLayoutId = R.layout.win_record;
			break;
		case ScreenUtil.LAYOUT_TYPE_VERTICAL:
			mLayoutId = R.layout.win_record_vertical;
			break;
		}
		View content = LayoutInflater.from(GlobalContext.getModified()).inflate(mLayoutId, null);
		mHelpNewTag  = (ImageView) content.findViewById(R.id.imgHelpNewTag);
		
		rlRecord_Wave_Panel = (RelativeLayout) content.findViewById(R.id.rlRecord_Wave_Panel);
		ivRecordShade = (ImageView) content.findViewById(R.id.record_shade);
		ivRecordBack = (ImageView) content.findViewById(R.id.record_back);
		ivRecordModule = (ImageView) content.findViewById(R.id.record_module);
		llRecordBackground = (View) content.findViewById(R.id.inner_Content);

		HashMap<String, String> cfg = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_SCREEN_PADDING_LEFT,
				TXZFileConfigUtil.KEY_SCREEN_PADDING_TOP,
				TXZFileConfigUtil.KEY_SCREEN_PADDING_RIGHT,
				TXZFileConfigUtil.KEY_SCREEN_PADDING_BOTTOM);
		String strPaddingLeft = cfg.get(TXZFileConfigUtil.KEY_SCREEN_PADDING_LEFT);
		String strPaddingTop = cfg.get(TXZFileConfigUtil.KEY_SCREEN_PADDING_TOP);
		String strPaddingRight = cfg.get(TXZFileConfigUtil.KEY_SCREEN_PADDING_RIGHT);
		String strPaddingBottom = cfg.get(TXZFileConfigUtil.KEY_SCREEN_PADDING_BOTTOM);

		int iPaddingLeft = getConfigValue(strPaddingLeft,0);
		int iPaddingTop = getConfigValue(strPaddingTop,0);
		int iPaddingRight = getConfigValue(strPaddingRight,0);
		int iPaddingBottom = getConfigValue(strPaddingBottom,0);
		llRecordBackground.setPadding(iPaddingLeft,iPaddingTop,iPaddingRight,iPaddingBottom);

		rocketAnimation = new AnimationDrawable();
		rocketAnimation.addFrame(getContext().getResources().getDrawable(R.drawable.mic1), 150);
		rocketAnimation.addFrame(getContext().getResources().getDrawable(R.drawable.mic2), 150);
		rocketAnimation.addFrame(getContext().getResources().getDrawable(R.drawable.mic3), 150);
		rocketAnimation.addFrame(getContext().getResources().getDrawable(R.drawable.mic2), 150);
		rocketAnimation.setOneShot(false);
		
		mChatContent = (ListView) content.findViewById(R.id.lvRecord_ChatContent);
		// mWaveIcon = (ImageView) content.findViewById(R.id.imgRecord_Icon);
		// mWaiting = content.findViewById(R.id.prgRecord_ProgressBar);
		// mWaitingFullStyle =
		// content.findViewById(R.id.prgRecord_ProgressBar_Full_Mask);
		mDisplayChatLv = (DisplayLvRef) content.findViewById(R.id.lvRecord_dll);
		mPluginLayout = (LinearLayout) content.findViewById(R.id.plugin_ly);
		mBannerAdvertisingLayout = (LinearLayout) content.findViewById(R.id.ll_advertising);
		mClose = content.findViewById(R.id.imgRecord_Close);
		if (mClose != null) {
			mClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.record.back", null, null);
					// getContext().sendBroadcast(new Intent("com.txznet.txz.record.dismiss.button"));
					// dismiss();
				}
			});
		}
		
		mImgHelpView = content.findViewById(R.id.imgHelp);
		if (mImgHelpView !=null) {
			mImgHelpView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mMultiHelpClickLock) {
						return;
					}
					ConfigUtil.setShowHelpNewTag(false);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.open", null,
							new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mMultiHelpClickLock = false;
						}
					});
				}
			});
		}
		
		mSettings = content.findViewById(R.id.imgSetting);
		mSettingsTop = content.findViewById(R.id.imgSettingTop);
		mSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LogUtil.loge("setting click ....");
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.setting", null, null);
			}
		});
		mSettingsTop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LogUtil.loge("setting click ....");
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.setting", null, null);
			}
		});
		
		
		if (mDisplayListRefresher == null) {
			mDisplayListRefresher = new DisplayListRefresher();
		}
		mDisplayListRefresher.initDisplayLv(mDisplayChatLv);
		SPUtil.updateHelpCount(getContext());
		
		ivRecordBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pauseRecord();
			}
		});
		
		ConfigUtil.registerIconStateChangeListener(getIconStateChangeListener());
		
//		 content.setOnClickListener(new View.OnClickListener() {
//		 @Override
//		 public void onClick(View v) {
//		 pauseRecord();
//		 }
//		 });

		mChatContent.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				llRecordBackground.onTouchEvent(event);
				return false;
			}
		});
		llRecordBackground.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(final View v, final MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:break;

					case MotionEvent.ACTION_MOVE:break;

					case MotionEvent.ACTION_UP:
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.background", null, null);
						break;

				}
				return false;
			}
		});

		if (mContentWidth > 0) {
			RelativeLayout contentWrapper = (RelativeLayout) LayoutInflater.from(GlobalContext.getModified()).inflate(R.layout.win_record_wrapper, null);
			RelativeLayout.LayoutParams layoutParams2 = new RelativeLayout.LayoutParams(mContentWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
			layoutParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
			contentWrapper.addView(content, layoutParams2);
			return contentWrapper;
		}
		return content;
	}
	public static int getLayoutTheme() {
		int theme = R.style.style_horizontal;
		switch (com.txznet.comm.ui.util.ScreenUtil.getLayoutType()) {
		case com.txznet.comm.ui.util.ScreenUtil.LAYOUT_TYPE_HORIZONTAL:
			theme = R.style.style_horizontal;
			break;
			
		case com.txznet.comm.ui.util.ScreenUtil.LAYOUT_TYPE_VERTICAL:
			theme = R.style.style_vertical;
			break;
		}
		return theme;
	}
	
	public static int getScreenTheme() {
		int theme = R.style.style_little;
		switch (com.txznet.comm.ui.util.ScreenUtil.getScreenType()) {
		case com.txznet.comm.ui.util.ScreenUtil.SCREEN_TYPE_CAR:
			theme = R.style.style_car;
			break;
			
		case com.txznet.comm.ui.util.ScreenUtil.SCREEN_TYPE_LITTLE:
			theme = R.style.style_little;
			break;
		case com.txznet.comm.ui.util.ScreenUtil.SCREEN_TYPE_LARGE:
			theme = R.style.style_large;
			break;
		}
		return theme;
	}

	private void initChatMsgs(List<ChatMessage> chatMsgs) {
		mChatMsgs = (LinkedList<ChatMessage>) (chatMsgs == null ? new LinkedList<ChatMessage>() : chatMsgs);
		mChatAdapter = new ChatContentAdapter(this, mChatMsgs);
		mChatAdapter.setChatMsgListener(new ChatMsgListener() {
			@Override
			public void onListMsgItemClicked(int msgType, int index) {
				String data = new JSONBuilder().put("index", index)
						.put("type", msgType == ChatMessage.TYPE_FROM_SYS_CONTACT ? 0 : 1).toString();
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
						data.getBytes(), null);
			}

			@Override
			public void onListMsgCancel(int msgType) {
				String data = new JSONBuilder().put("type", msgType == ChatMessage.TYPE_FROM_SYS_CONTACT ? 0 : 1)
						.toString();
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.cancel",
						data.getBytes(), null);
				dismiss();
			}
		});
		mChatContent.setAdapter(mChatAdapter);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addMsg(ChatMessage chatMsg) {
		if (!isShowing() || chatMsg == null) {
			return;
		}

		if (mDisplayListRefresher.procBySingleView(chatMsg)) {
			setDisplayListVisible(true);
			return;
		} else if (chatMsg instanceof PluginMessage) {
			PluginMessage pm = (PluginMessage) chatMsg;
			if (pm.mPluginData.mIsDepend) {
				AppLogicBase.runOnUiGround(new Runnable1<View>(pm.mPluginData.mView) {
					@Override
					public void run() {
						mIsShowLv = true;
						mPluginLayout.removeAllViews();
						mPluginLayout.addView(mP1);

						mChatContent.setVisibility(View.INVISIBLE);
						mDisplayChatLv.setVisibility(View.INVISIBLE);
						mPluginLayout.setVisibility(View.VISIBLE);
					}
				}, 0);
				return;
			}
		}

		if (mIsShowLv) {
			setDisplayListVisible(false);
		}


		if (mChatMsgs.size() > 0) {

			// 最后一条是系统列表
			ChatMessage lastMsg = mChatMsgs.get(mChatMsgs.size() - 1);
			if (lastMsg.type == ChatMessage.TYPE_FROM_SYS_CONTACT) {
				// 新来的消息也是列表
				if (chatMsg.type == ChatMessage.TYPE_FROM_SYS_CONTACT) {
					lastMsg.title = chatMsg.title;
					lastMsg.items = chatMsg.items;
					refresh();
					return;
				}
			}

			if (lastMsg instanceof BaseDisplayMsg && chatMsg instanceof BaseDisplayMsg) {
				((BaseDisplayMsg) lastMsg).action = ((BaseDisplayMsg) chatMsg).action;
				((BaseDisplayMsg) lastMsg).mKeywords = ((BaseDisplayMsg) chatMsg).mKeywords;
				((BaseDisplayMsg) lastMsg).mOnItemClickListener = ((BaseDisplayMsg) chatMsg).mOnItemClickListener;
				((BaseDisplayMsg) lastMsg).mItemList = ((BaseDisplayMsg) chatMsg).mItemList;
				if (lastMsg instanceof PoiMsg) {
					((PoiMsg) lastMsg).mIsBusiness = ((PoiMsg) chatMsg).mIsBusiness;
					((PoiMsg) lastMsg).mOnTitleClickListener = ((PoiMsg) chatMsg).mOnTitleClickListener;
				}
				refresh();
				return;
			}

			if (lastMsg instanceof PluginMessage && chatMsg instanceof PluginMessage) {
				PluginData pd = ((PluginMessage) chatMsg).mPluginData;
				PluginData pData = ((PluginMessage) lastMsg).mPluginData;
				if (pd.typeId == pData.typeId && pd.mReplace) {
					mChatMsgs.remove(lastMsg);
				}
			}

			if (lastMsg instanceof HelpTipMsg  ) {
				if (chatMsg instanceof HelpTipMsg) {
					HelpTipMsg lastHelpTipMsg = (HelpTipMsg) lastMsg;
					HelpTipMsg newHelpTipMsg = (HelpTipMsg) chatMsg;
					lastHelpTipMsg.mCount = newHelpTipMsg.mCount;
					lastHelpTipMsg.mTitle = newHelpTipMsg.mTitle;
					lastHelpTipMsg.mHelpTipBeen = newHelpTipMsg.mHelpTipBeen;
					refresh();
					return;
				} else {
					mChatMsgs.remove(lastMsg);
				}

			}

			if (lastMsg.type == ChatMessage.TYPE_TO_SYS_PART_TEXT) {
				if (chatMsg.type == ChatMessage.TYPE_TO_SYS_PART_TEXT) {
					lastMsg.text =chatMsg.text;
					refresh();
					return;
				}else {
					mChatMsgs.remove(lastMsg);
				}
			}

		}
		if (mChatMsgs.size() > 6) {
			mChatMsgs.removeFirst();
		}
		mChatMsgs.add(chatMsg);

		// 处理屏幕分辨率问题
		if (ScreenUtil.checkScreenSizeChangeForChat()) {
			initChatMsgs(mChatMsgs);
		}

		refresh();
	}

	
	private void refresh() {
		if (Looper.myLooper() != Looper.getMainLooper()) {
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mChatAdapter.notifyDataSetChanged();
					scrollToEnd();
				}
			});
			return;
		}
		mChatAdapter.notifyDataSetChanged();
		scrollToEnd();
	}

	private void scrollToEnd() {
		mChatContent.setSelection(mChatAdapter.getCount() - 1);
		mChatContent.requestLayout();
		mChatContent.scrollTo(0, mChatContent.getBottom() - mChatContent.getHeight());
	}

	private long lastUpdate = -1;
	private float curAmplitude = 1.0f;
	private int maxVolume = -1;

	// TODO 优化过渡
	public void notifyUpdateVolume(int volume) {

	}

	
	
	private int prevStatus = -1; 
	private int rlRecordWidth = -1;
	private int rlRecordHeight = -1;
	
	// 根据父布局大小，按比例计算录音动画大小
	private void Resize() {
		float standard = 224;
		float scale = 1;

		switch (ScreenUtil.getLayoutType()) {
		
		case ScreenUtil.LAYOUT_TYPE_HORIZONTAL:
			int parentWidth = rlRecord_Wave_Panel.getWidth();
			if (parentWidth == rlRecordWidth)
				return;
			rlRecordWidth = parentWidth;
			standard = (float) (parentWidth * 0.85);
			scale = standard / 224;
			break;

		case ScreenUtil.LAYOUT_TYPE_VERTICAL:
			int parentHeight = rlRecord_Wave_Panel.getHeight();
			if (parentHeight == rlRecordHeight)
				return;
			rlRecordHeight = parentHeight;
			standard = (float) (parentHeight * 0.99);
			scale = standard / 210;
			break;
		}

		android.widget.RelativeLayout.LayoutParams ivLP = (android.widget.RelativeLayout.LayoutParams) ivRecordShade
				.getLayoutParams();
		ivLP.width = (int) standard;
		ivLP.height = (int) standard;
		ivRecordShade.setLayoutParams(ivLP);

		ivLP = (android.widget.RelativeLayout.LayoutParams) ivRecordBack
				.getLayoutParams();
		ivLP.width = (int) (120 * scale);
		ivLP.height = (int) (120 * scale);
		ivRecordBack.setLayoutParams(ivLP);

		ivLP = (android.widget.RelativeLayout.LayoutParams) ivRecordModule
				.getLayoutParams();
		ivLP.width = (int) (35 * scale);
		ivLP.height = (int) (50 * scale);
		ivRecordModule.setLayoutParams(ivLP);

	}
	
	Runnable runTTS = new Runnable() {
		@Override
		public void run() {
			AppLogicBase.removeUiGroundCallback(runRecordAnim);
			AppLogicBase.removeUiGroundCallback(runLoadingAnim);
			ivRecordBack.setRotation(0);
			ivRecordShade.setScaleX(1.0F);
			ivRecordShade.setScaleY(1.0F);
			ivRecordShade.setVisibility(View.INVISIBLE);
			ivRecordModule.setVisibility(View.VISIBLE);
			Resize();
			
			ivRecordBack.setImageResource(R.drawable.slice);
			if (rocketAnimation.isRunning()) {
				rocketAnimation.stop();
			}
			ivRecordModule.setImageDrawable(rocketAnimation);
			rocketAnimation.start();	
			
		}
	};
	
	private float[] ss = new float[]{1.0F,0.96F,0.92F,0.88F,0.84F,0.80F,0.84F,0.88F,0.92F,0.96F};
	private int si = 0;
	Runnable runRecordAnim = new Runnable() {
		@Override
		public void run() {

			ivRecordShade.setScaleX(ss[si%ss.length]);
			ivRecordShade.setScaleY(ss[si%ss.length]);
			si++;
			AppLogicBase.runOnUiGround(this, 75);
		}
	};
	
	Runnable runRecord = new Runnable() {
		@Override
		public void run() {
			
			AppLogicBase.removeUiGroundCallback(runLoadingAnim);
			ivRecordBack.clearAnimation();
			rocketAnimation.stop();
			ivRecordBack.setRotation(0);
			ivRecordShade.setVisibility(View.VISIBLE);
			ivRecordModule.setVisibility(View.INVISIBLE);

			ivRecordShade.setImageResource(R.drawable.mic_bg);
			ivRecordBack.setImageResource(R.drawable.mic);
			si = 0;
			AppLogicBase.runOnUiGround(runRecordAnim, 0);
		}
	};
	
	
	float ro = 0;
	Runnable runLoadingAnim = new Runnable() {
		@Override
		public void run() {

			ivRecordBack.setRotation(ro);
			ro += 45;
			AppLogicBase.runOnUiGround(this, 125);
		}
	};
	
	Runnable runLoading = new Runnable() {

		@Override
		public void run() {
			
			AppLogicBase.removeUiGroundCallback(runRecordAnim);
			ivRecordBack.clearAnimation();
			rocketAnimation.stop();
			ivRecordShade.setScaleX(1.0F);
			ivRecordShade.setScaleY(1.0F);
			ivRecordShade.setVisibility(View.INVISIBLE);
			ivRecordModule.setVisibility(View.VISIBLE);

			ivRecordBack.setImageResource(R.drawable.loading);
			ivRecordModule.setImageResource(R.drawable.mic5);
			ro = 0;
			AppLogicBase.runOnUiGround(runLoadingAnim, 0);
		}
	};


	// 刷新状态的回调
	public void notifyUpdateLayout(int status) {
				
		if(!isShowing() || prevStatus == status)
			return;

		prevStatus = status;
			
		switch (status) {
		
		case STATE_NORMAL:
			// 正常状态
			AppLogicBase.runOnUiGround(runTTS, 0);
			break;

		case STATE_RECORD_START:
			// 录音开始
			AppLogicBase.runOnUiGround(runRecord, 0);
			break;
			
		case STATE_RECORD_END:
			// 处理中
			AppLogicBase.runOnUiGround(runLoading, 0);
			break;
		case IRecordView.STATE_WIN_OPEN:
			if (mIsShowLv) {
				setDisplayListVisible(false);
			}
			break;

		}
	}

	
	private int mLastVal = 0;
	private int mLastSelection = 0;
	// 更新进度的回调
	public void notifyUpdateProgress(int val, int selection) {
		if (val == mLastVal && mLastSelection == selection) {
			return;
		}
		mLastVal = val;
		mLastSelection = selection;
		mDisplayListRefresher.updateProgress(val, selection);
		mChatAdapter.notifyUpdateProgress(val);
	}

	private HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss();
		}
	};

	boolean enableAnim = false;

	/**
	 * 是否开启进入时动画
	 * 
	 * @param enable
	 */
	public void enableAnim(boolean enable) {
		enableAnim = enable;
	}

	private OnSizeObserver mOnSizeObserver = new OnSizeObserver() {

		@Override
		public void onResSize() {
			scrollToEnd();
		}
	};

	private WinRecordCycleObserver mCycleObserver = null;

	// 是否注册了监听器
	private boolean mRegisted;
	// 是否锁定了屏幕锁
	private boolean mHasScreenLock;
	
	
	private Runnable updateHelpNew = new Runnable() {
		@Override
		public void run() {
			if (mHelpNewTag != null) {
				if (ConfigUtil.isShowHelpInfos()) {
					if (ConfigUtil.isShowHelpNewTag()) {
						mHelpNewTag.setVisibility(View.VISIBLE);
					} else {
						mHelpNewTag.setVisibility(View.GONE);
					}
				}else {
					mHelpNewTag.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public void show() {
		if (!mRegisted) {
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().registerObserver(mOnSizeObserver);
			try {
				if (mCycleObserver != null) {
					GlobalObservableSupport.getWinRecordObserver().registerObserver(mCycleObserver);
				}
			} catch (Exception e) {
			}

			IntentFilter filter = new IntentFilter();
			filter.addAction("com.txznet.webchat.action.DOWNLOAD_IMG_COMPLETE");
			getContext().registerReceiver(mWxResReceiver, filter);
		}
		if (!mHasScreenLock) {
			mScreenLock.lock();
			mHasScreenLock = true;
		}
		if (enableAnim)
			getWindow().setWindowAnimations(R.style.SlideDialogAnimation);

		LogUtil.logd("mClose == null : " + (mClose == null) + "isShowCloseIcon = " + ConfigUtil.isShowCloseIcon() + "; isShowSettings = " + ConfigUtil.isShowSettings());
		if (mClose != null) {
			if (ConfigUtil.isShowCloseIcon()) {
				mClose.setVisibility(View.VISIBLE );
				if (ConfigUtil.isShowSettings()) {
					mSettingsTop.setVisibility(View.GONE);
					mSettings.setVisibility(View.VISIBLE);
				}else {
					mSettingsTop.setVisibility(View.GONE);
					mSettings.setVisibility(View.GONE);
				}
			}else {
				mClose.setVisibility(View.GONE );
				if (ConfigUtil.isShowSettings()) {
					mSettingsTop.setVisibility(View.VISIBLE);
					mSettings.setVisibility(View.GONE);
				}else {
					mSettingsTop.setVisibility(View.GONE);
					mSettings.setVisibility(View.GONE);
				}
			}
		}
		
		if (mImgHelpView != null) {
			if (ConfigUtil.isShowHelpInfos()) {
				if (ConfigUtil.isShowHelpNewTag()) {
					mHelpNewTag.setVisibility(View.VISIBLE);
				}else {
					mHelpNewTag.setVisibility(View.GONE);
				}
				mImgHelpView.setVisibility(View.VISIBLE);
			} else {
				mImgHelpView.setVisibility(View.GONE);
				mHelpNewTag.setVisibility(View.GONE);
			}
		}
		
		
//		if (mWinBgAlpha != null) {
//			int iWinBgAlpha = (int) ((1 - mWinBgAlpha) * 255);
//			String s1 = Integer.toHexString(iWinBgAlpha);
//			String s2 = Integer.toHexString(GlobalContext.getModified().getResources().getColor(R.color.win_bg));
//			s1 = s1 + s2.substring(2);
//			bgColor = Integer.valueOf(s1,16);
//			
//		}
		
		if (mIfSetWinBg) {		
			getWindow().setBackgroundDrawable(new ColorDrawable(bgColor));
		} else {
			mView.setBackground(new ColorDrawable(bgColor));
		}
		
		if (mView == null) {
			ScreenUtil.checkViewRect(getWindow().getDecorView());
		} else {
			ScreenUtil.checkViewRect(mView);
		}

		super.show();
		resume();
	}

	@Override
	public void dismiss() {
		AsrUtil.closeRecordWinLock();
		if (mHasScreenLock) {
			mScreenLock.release();
			mHasScreenLock = false;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.dismiss", null, null);
		if (Looper.myLooper() != Looper.getMainLooper()) {
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					dismissInner();
				}
			}, 0);
		} else {
			dismissInner();
		}
	}

	private void dismissInner() {
		WinRecordImpl2.super.dismiss();
		releaseResouce();
		AppLogicBase.removeUiGroundCallback(runRecordAnim);
		AppLogicBase.removeUiGroundCallback(runLoadingAnim);
		ivRecordBack.clearAnimation();
		rocketAnimation.stop();
		prevStatus = -1;
		mBannerAdvertisingLayout.setVisibility(View.GONE);
	}
	
	private void releaseResouce() {
		setDisplayListVisible(false);
		mChatMsgs.clear();
		mChatAdapter.notifyDataSetChanged();
		StockRefresher.getInstance().release();
		WeatherRefresher.getInstance().release();
		
		KeyEventManagerUI1.getInstance().release();
		mDisplayListRefresher.release();

		if (mRegisted) {
			mRegisted = false;
			GlobalObservableSupport.getWinRecordObserver().onDismiss();
			GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().unregisterObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				// 有可能show之后这里才非空的,没有register直接unregister会报错
				try {
					GlobalObservableSupport.getWinRecordObserver().unregisterObserver(mCycleObserver);
				} catch (Exception e) {
				}
			}
			getContext().unregisterReceiver(mWxResReceiver);
			mRegisted = false;
		}

		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().clearDiskCache();
			ImageLoader.getInstance().clearMemoryCache();
		}
//		WinPoiShow.getIntance().dismiss();
		System.gc();
	}

	// 复位
	private void resume() {
		mChatAdapter.resetAnimation();
		mMultiHelpClickLock = false;
//		ChatMessage helpMsg = ChatMsgFactory.getSysHelpMsg("您好，有什么可以帮您", getContext(), false,
//				new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						if (mMultiHelpClickLock) {
//							return;
//						}
//						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.open", null,
//								new GetDataCallback() {
//							@Override
//							public void onGetInvokeResponse(ServiceData data) {
//								mMultiHelpClickLock = false;
//							}
//						});
//					}
//				});
//		addMsg(helpMsg);
		
		mChatAdapter.notifyDataSetChanged();
		notifyUpdateLayout(STATE_NORMAL);
		GlobalObservableSupport.getWinRecordObserver().onShow();
	}

	private boolean isJustPause = false;

	private void pauseRecord() {
		if (isJustPause)
			return;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.record", null, null);
		isJustPause = true;
		AppLogicBase.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				isJustPause = false;
			}
		}, 1000);
	}

	Runnable mCheckToScrollEndRunnable = new Runnable() {

		@Override
		public void run() {
			scrollToEnd();
		}
	};

	@Override
	protected void onLoseFocus() {
		LogUtil.logd("onLoseFocus");
		GlobalObservableSupport.getWinRecordObserver().onLoseFocus();
	}

	@Override
	protected void onGetFocus() {
		LogUtil.logd("onGetFocus");
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
		if (mIsFullScreen && mView != null) {
			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN);
		}
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mCycleObserver = observer;
		try {
			if (mCycleObserver != null) {
				GlobalObservableSupport.getWinRecordObserver().registerObserver(mCycleObserver);
			}
		} catch (Exception e) {
		}
	}

	private void broadCastDisplayLvVisible(boolean isVisible) {
		Intent intent = new Intent("com.txznet.txz.DisplayLv_ACTION");
		intent.putExtra("visible", isVisible);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void newWinInstance() {
		if (isShowing()) {
			LogUtil.loge("current win is showing,can't new instance!");
			return;
		}
		releaseResouce();
		ConfigUtil.unregisterIconStateChangeListener(getIconStateChangeListener());
		mInstance = new WinRecordImpl2(mIsFullScreen);
		if (mCycleObserver != null) {
			mInstance.setWinRecordObserver(mCycleObserver);
		}
	}

	@Override
	public void setContentWidth(int width) {
		if (mContentWidth != width) {
			mContentWidth = width;
			releaseResouce();
			mInstance = new WinRecordImpl2(mIsFullScreen);
			if (mCycleObserver != null) {
				mInstance.setWinRecordObserver(mCycleObserver);
			}
		}
	}

	@Override
	public void setIfSetWinBg(boolean ifSet) {
		mIfSetWinBg = ifSet;
	}

	@Override
	public void updateDisplayArea(int x, int y, int width, int height) {
		if (getWindow() != null) {
			WindowManager.LayoutParams lp = getWindow().getAttributes();
			if (lp != null) {
				lp.x = x;
				lp.y = y;
				lp.width = width;
				lp.height = height;
				if (x != 0 || y != 0) {
					lp.gravity = Gravity.LEFT | Gravity.TOP;
				}
				getWindow().setAttributes(lp);
			}
		}
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		mBannerAdvertisingLayout.removeAllViews();
		mBannerAdvertisingLayout.setVisibility(View.VISIBLE);
		mBannerAdvertisingLayout.addView(view);
	}

	@Override
	public void removeBannerAdvertisingView() {
		mBannerAdvertisingLayout.removeAllViews();
		mBannerAdvertisingLayout.setVisibility(View.GONE);
	}

	@Override
	public void setBackground(Drawable drawable) {
		mView.setBackground(drawable);
	}


	@Override
	public void setDialogCancel(boolean flag) {
		LogUtil.logd("set dialog cacelable " + flag);
		mDialogCancelable = flag;
		setCancelable(flag);
	}
	
	@Override
	public void setSystemUiVisibility(int type) {
		LogUtil.logd("setSystemUiVisibility :" + type);
		mSystemUiVisibility = type;
		if(mView != null) {
			mView.setSystemUiVisibility(type);
		}
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {

	}

	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		// TODO Auto-generated method stub

	}

	public void removeHelpView(){
		if (mChatMsgs == null || mChatMsgs.size() <= 1) {
			return;
		}
		if(mChatMsgs.get(1).type == ChatMessage.TYPE_FROM_SYS_HELP_TIPS){
			mChatMsgs.remove(1);
            LogUtil.logd("removeHelpView UI1.0 type:"+ChatMessage.TYPE_FROM_SYS_HELP_TIPS);
            mChatAdapter.notifyDataSetChanged();
        }
	}
}