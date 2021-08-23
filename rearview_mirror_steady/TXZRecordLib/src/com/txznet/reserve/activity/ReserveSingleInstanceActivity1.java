package com.txznet.reserve.activity;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.ScrollObservable.OnSizeObserver;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatContentAdapter;
import com.txznet.record.adapter.ChatContentAdapter.ChatMsgListener;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.PluginMessage;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.bean.PluginMessage.PluginData;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.lib.R;
import com.txznet.record.setting.MainActivity;
import com.txznet.record.ui.DisplayListRefresher;
import com.txznet.record.ui.StockRefresher;
import com.txznet.record.ui.WeatherRefresher;
import com.txznet.record.util.SPUtil;
import com.txznet.record.view.DisplayLvRef;
import com.txznet.txz.util.runnables.Runnable1;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;

/**
 * UI1.0 语音界面
 * @author Terry
 *
 */
public class ReserveSingleInstanceActivity1 extends BaseActivity {
	
	
	public static ReserveSingleInstanceActivity1 sInstance;

	public static final int SHOW_DISPLAY = 0x01;
	public static final int HIDE_DISPLAY = 0x02;
	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画
	
	public static ChatMessage sPreLoadMsg; // 第一条消息添加时activity还未实例化，先保存起来
	public static Integer sPreState; // 录音状态


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

	private View mClose; // 退出按钮
	private View mImgHelpView;//帮助
	private View mSettings;//设置
	private View mSettingsTop;//设置
	private boolean mIsShowLv;
	// private Handler mUiHandler;

	// 防止多次点击帮助按钮的锁
	private boolean mMultiHelpClickLock = false;
	private boolean mUseMockBar;
	
	private View mView;
	
	public ReserveSingleInstanceActivity1(){
		super();
		sInstance = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTheme(R.style.AppTransparentTheme);
		mView = createView();
		setContentView(mView);
		initWinRecord();
		if (sPreLoadMsg != null) {
			addMsg(sPreLoadMsg);
			sPreLoadMsg = null;
		}
		if (sPreState != null) {
			notifyUpdateLayout(sPreState);
			sPreState = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ConfigUtil.unregisterIconStateChangeListener(getIconStateChangeListener());
		sInstance = null;
		sPreLoadMsg = null;
		sPreState = null;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return KeyEventManagerUI1.getInstance().onKeyEvent(event);
	}
	
	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// LogUtil.logd("[UI1.0] onKeyDown:" + keyCode);
	// return KeyEventManagerUI1.getInstance().onKeyEvent(keyCode);
	// }
	
	/**
	 * 真正的初始化窗口
	 */
	public void realInit(){
		try {
			Rect outRect = new Rect();
			getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
			int width = outRect.width();
			int height = outRect.height();
			LogUtil.logd("width:"+width+";height:"+height+";mWidth:"+mWidth+";mHeight:"+mHeight);
			if (mWidth != width || mHeight != height) {
			}
		} catch (Exception e) {
			LogUtil.logd("realInit WinRecord failed");
		}
	}
	private int mWidth;
	private int mHeight;

	private void initWinRecord() {
		mUseMockBar = getResources().getBoolean(R.bool.useMockBar);
		initChatMsgs(null);
		mScreenLock = new ScreenLock(this);
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
		View v1 = new View(this);
		v1.setBackgroundColor(getResources().getColor(R.color.mockbarColor));
		int barHeight = getStatusBarHeight(this);
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

	private IconStateChangeListener getIconStateChangeListener(){
		if (mIconStateChangeListener == null) {
			mIconStateChangeListener = new IconStateChangeListener() {
				@Override
				public void onStateChanged(int type, boolean enable) {
					AppLogicBase.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							if (mClose == null || mSettings == null || mSettingsTop == null) {
								return;
							}
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
					}, 0);
				}
			};
		}
		return mIconStateChangeListener;
	}

	IconStateChangeListener mIconStateChangeListener;
	
	@SuppressLint("InflateParams")
	protected View createView() {
		Rect outRect = new Rect();
		getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		mWidth = outRect.width();
		mHeight = outRect.height();
		ScreenUtil.initScreenType(getWindow().getDecorView());
		//直接将1.0计算到的屏幕类型赋值给ConfigUtil.mCurType，字体大小，颜色还是从2.0的框架获取的
		com.txznet.comm.ui.util.ConfigUtil.mCurType = ScreenUtil.getScreenType();
		ViewConfiger.getInstance().initRecordWin1ThemeConfig();
		   
		GlobalContext.get().getTheme().applyStyle(getScreenTheme(), true);
		GlobalContext.get().getTheme().applyStyle(getLayoutTheme(), true);
		if (this != GlobalContext.get()) {
			this.getTheme().applyStyle(getScreenTheme(), true);
			this.getTheme().applyStyle(getLayoutTheme(), true);
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
		View content = LayoutInflater.from(GlobalContext.get()).inflate(mLayoutId, null);
		
		rlRecord_Wave_Panel = (RelativeLayout) content.findViewById(R.id.rlRecord_Wave_Panel);
		ivRecordShade = (ImageView) content.findViewById(R.id.record_shade);
		ivRecordBack = (ImageView) content.findViewById(R.id.record_back);
		ivRecordModule = (ImageView) content.findViewById(R.id.record_module);
		
		rocketAnimation = new AnimationDrawable();
		rocketAnimation.addFrame(this.getResources().getDrawable(R.drawable.mic1), 150);
		rocketAnimation.addFrame(this.getResources().getDrawable(R.drawable.mic2), 150);
		rocketAnimation.addFrame(this.getResources().getDrawable(R.drawable.mic3), 150);
		rocketAnimation.addFrame(this.getResources().getDrawable(R.drawable.mic2), 150);
		rocketAnimation.setOneShot(false);
		
		mChatContent = (ListView) content.findViewById(R.id.lvRecord_ChatContent);
		// mWaveIcon = (ImageView) content.findViewById(R.id.imgRecord_Icon);
		// mWaiting = content.findViewById(R.id.prgRecord_ProgressBar);
		// mWaitingFullStyle =
		// content.findViewById(R.id.prgRecord_ProgressBar_Full_Mask);
		mDisplayChatLv = (DisplayLvRef) content.findViewById(R.id.lvRecord_dll);
		mPluginLayout = (LinearLayout) content.findViewById(R.id.plugin_ly);
		mClose = content.findViewById(R.id.imgRecord_Close);
		if (mClose != null) {
			mClose.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					sendBroadcast(new Intent("com.txznet.txz.record.dismiss.button"));
					finish();
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
				Intent startRecordSetting = new Intent(AppLogicBase.getApp(), MainActivity.class);
				startRecordSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				AppLogicBase.getApp().startActivity(startRecordSetting);
				finish();
			}
		});
		mSettingsTop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LogUtil.loge("setting click ....");
				Intent startRecordSetting = new Intent(AppLogicBase.getApp(), MainActivity.class);
				startRecordSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				AppLogicBase.getApp().startActivity(startRecordSetting);
				finish();
			}
		});
		
		
		if (mDisplayListRefresher == null) {
			mDisplayListRefresher = new DisplayListRefresher();
		}
		mDisplayListRefresher.initDisplayLv(mDisplayChatLv);
		SPUtil.updateHelpCount(this);
		
		ivRecordBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pauseRecord();
			}
		});
		
		ConfigUtil.registerIconStateChangeListener(getIconStateChangeListener());
		
		// content.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// pauseRecord();
		// }
		// });
		content.setBackground(GlobalContext.get().getResources().getDrawable(R.drawable.bg));
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
				finish();
			}
		});
		mChatContent.setAdapter(mChatAdapter);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addMsg(ChatMessage chatMsg) {
		if (chatMsg == null) {
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
				((BaseDisplayMsg) lastMsg).mKeywords = ((BaseDisplayMsg) lastMsg).mKeywords;
				((BaseDisplayMsg) lastMsg).mOnItemClickListener = ((BaseDisplayMsg) lastMsg).mOnItemClickListener;
				((BaseDisplayMsg) lastMsg).mItemList = ((BaseDisplayMsg) lastMsg).mItemList;
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
//		maxVolume = maxVolume > volume ? maxVolume : volume;
//		if (SystemClock.elapsedRealtime() - lastUpdate < 300) {
//			return;
//		}
//		float amplitude = (maxVolume - 8) * 1f / 4f;
//		if (amplitude < 1.0f) {
//			amplitude = 1.0f;
//		} else if (amplitude > 2.0f) {
//			amplitude = 2.0f;
//		}
//		curAmplitude = amplitude;

//		if (mRecStepView != null) {
//			mRecStepView.updateVol(volume);
//		}
	}

	// Runnable mUpdateAmplitudeTask = new Runnable() {
	// @Override
	// public void run() {
	// // 间隔一段时间根据采样值刷
	// ValueAnimator animator =
	// ValueAnimator.ofFloat(mWaveformView.getAmplitude(), curAmplitude);
	// maxVolume = 0;
	// animator.setInterpolator(new AccelerateInterpolator());
	// animator.setDuration(300);
	// animator.addUpdateListener(new AnimatorUpdateListener() {
	// @Override
	// public void onAnimationUpdate(ValueAnimator animation) {
	// mWaveformView.updateAmplitude((Float) animation.getAnimatedValue());
	// }
	// });
	// animator.start();
	// AppLogicBase.removeUiGroundCallback(mUpdateAmplitudeTask);
	// AppLogicBase.runOnUiGround(mUpdateAmplitudeTask, 300);
	// }
	// };
	
	
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
			
			AppLogicBase.removeUiGroundCallback(runLoadingAnim);
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
				
		if(prevStatus == status)
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
			finish();
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


	// 是否注册了监听器
	private boolean mRegisted;
	// 是否锁定了屏幕锁
	private boolean mHasScreenLock;
	


	protected void onResume() {
		super.onResume();
		if (!mRegisted) {
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().registerObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				GlobalObservableSupport.getWinRecordObserver().registerObserver(mCycleObserver);
			}
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.txznet.webchat.action.DOWNLOAD_IMG_COMPLETE");
			this.registerReceiver(mWxResReceiver, filter);
		}
		if (!mHasScreenLock) {
			mScreenLock.lock();
			mHasScreenLock = true;
		}
		if (enableAnim)
			getWindow().setWindowAnimations(R.style.SlideDialogAnimation);
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
		if (mView == null) {
			ScreenUtil.checkViewRect(getWindow().getDecorView());
		} else {
			ScreenUtil.checkViewRect(mView);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
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
					release();
				}
			}, 0);
		} else {
			release();
		}
				
		AppLogicBase.removeUiGroundCallback(runRecordAnim);
		AppLogicBase.removeUiGroundCallback(runLoadingAnim);
		ivRecordBack.clearAnimation();
		rocketAnimation.stop();
		prevStatus = -1;
	}
	
	private void release() {
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
				GlobalObservableSupport.getWinRecordObserver().unregisterObserver(mCycleObserver);
			}
			this.unregisterReceiver(mWxResReceiver);
			mRegisted = false;
		}

		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().clearDiskCache();
			ImageLoader.getInstance().clearMemoryCache();
		}
		System.gc();
	}

	// 复位
	private void resume() {
		mChatAdapter.resetAnimation();
		mMultiHelpClickLock = false;
//		ChatMessage helpMsg = ChatMsgFactory.getSysHelpMsg("您好，有什么可以帮您", this, false,
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
		Log.d(this.getClass().getSimpleName(), "Pasue!!!!!!!!!!!!!!!");
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

	public void onLoseFocus() {
		GlobalObservableSupport.getWinRecordObserver().onLoseFocus();
	}

	public void onGetFocus() {
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
	}

	private static WinRecordCycleObserver mCycleObserver = null;
	
	public static void setWinRecordObserver(WinRecordCycleObserver observer) {
		mCycleObserver = observer;
	}

	private void broadCastDisplayLvVisible(boolean isVisible) {
		Intent intent = new Intent("com.txznet.txz.DisplayLv_ACTION");
		intent.putExtra("visible", isVisible);
		GlobalContext.get().sendBroadcast(intent);
	}

	public void newWinInstance() {
		release();
	}
	
}
