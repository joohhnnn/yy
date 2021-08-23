package com.txznet.comm.ui;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.keyevent.KeyEventManager;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.resloader.UIResLoader.ResLoadedListener;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.txz.util.TXZHandler;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * UI2.0各个模块初始化
 */
public class UI2Manager {

	private static UI2Manager sInstance = new UI2Manager();
	
	public static final int INIT_SUCCESS = 0; //初始化成功
	public static final int INIT_ERROR_MINOR = -1; // 初始化时遇到错误，但是不影响使用
	public static final int INIT_ERROR_ABORT = -2; // 初始化遇到不可恢复的错误
	
	private UI2Manager(){
	}

	public static UI2Manager getInstance(){
		return sInstance;
	}
	
	private static Handler mUIHandler = new Handler(Looper.getMainLooper());;
	
	public static void runOnUIThread(Runnable r, int delay) {
		if (delay > 0) {
			mUIHandler.postDelayed(r, delay);
		} else {
			mUIHandler.post(r);
		}
	}
	
	public static void removeUIThread(Runnable runnable){
		if(mUIHandler!=null){
			mUIHandler.removeCallbacks(runnable);
		}
	}
	
	protected static HandlerThread mBackThread;
	protected static TXZHandler mBackHandler;
	
	public static void runOnBackGround(Runnable r, long delay) {
		if (delay > 0) {
			mBackHandler.postDelayed(r, delay);
		} else {
			mBackHandler.post(r);
		}
	}
	
	public static void removeBackGroundCallback(Runnable r) {
		mBackHandler.removeCallbacks(r);
	}
	
	public interface UIInitListener{
		/**
		 * 初始化成功
		 */
		public void onSuccess();

		/**
		 * 正常初始化失败，将开始用默认资源进行加载
		 */
		public void onError();

		/**
		 * 用默认资源加载仍然失败，切换到UI1.0框架
		 */
		public void onErrorError();
	}
	
	private UIInitListener mInitListener;
	
	public void init(UIInitListener listener) {
		mInitListener = listener;
		// 后台进程
		mBackThread = new HandlerThread("UI2Back");
		mBackThread.start();
		mBackHandler = new TXZHandler(mBackThread.getLooper());
		// 初始化ImageLoader
		ImageLoaderInitialize.initImageLoader(GlobalContext.get());
		initNormal();
	}

	public void initBySDK(UIInitListener listener){
		LogUtil.logd("initBySDK");	
		init(listener);
	}
	
	
	//正常初始化
	public void initNormal(){
		LogUtil.logd("#####UI2.0####### initNormal");
		// 初始化资源文件
		UIResLoader.getInstance().startLoadResource(new ResLoadedListener() {
			@Override
			public void onResLoaded() {
				LogUtil.logd("onResLoaded");
				runOnUIThread(new Runnable() {
					@Override
					public void run() {
						try {
							ConfigUtil.initThemeType(Integer.valueOf(LayouUtil.getString("theme_code")));
							ConfigUtil.isPriorityResHolderRes(true);
							RecordWin2.getInstance().init();
							boolean disableThirdWin = UIResLoader.getInstance().isPriorRes;// 如果是Prior资源包且加载成功了，则屏蔽disable第三方界面
							RecordWin2Manager.getInstance().disableThirdWin(disableThirdWin);
							ViewConfiger.getInstance().initThemeConfig(); // 需要让先RecordWin2读取到分辨率等信息，然后才能加载对应的资源文件
							ThemeConfigManager.getInstance().initThemeConfig();// 初始化主题间距配置
							RecordWin2Manager.getInstance().init();
							UIVersionManager.getInstance().init();

							runOnBackGround(new Runnable() {
								@Override
								public void run() {
									//耗时操作，新开线程，执行完后再切到主线程进行其他初始化
									try {
										WinLayoutManager.getInstance().init(); // 初始化当前layout所需要的各个View
										KeyEventManager.getInstance().init(); // 初始化按键处理相关
									} catch (Exception e) {
										LogUtil.loge("UI2.0:", e);
										LogUtil.loge("UI2.0 normal init error!");
										MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR_CORE_INIT_VIEW);
										if (mInitListener != null) {
											mInitListener.onError();
										}
										return;
									}
									runOnUIThread(new Runnable() {
										@Override
										public void run() {
											LogUtil.logd("UI2.0 init success");
											RecordWin2.getInstance().updateWinLayout(WinLayoutManager.getInstance().getLayout()); // 初始化窗口
											if (mInitListener != null) {
												mInitListener.onSuccess();
											}
										}
									}, 0);
								}
							}, 0);
						} catch (Exception e) {
							LogUtil.loge("UI2.0:", e);
							LogUtil.loge("UI2.0 normal init error!");
							if (mInitListener != null) {
								mInitListener.onError();
							}
						}
					}
				}, 0);
			}
			@Override
			public void onException(String errorDsp) {
				LogUtil.loge("load skin apk error:" + errorDsp);
				if (mInitListener != null) {
					mInitListener.onError();
				}
			}
		}, false);
	}
	
	//遇到异常时尝试用apk assets下面的资源包进行加载，暂时未使用
	// public void initInError(){
	// LogUtil.logd("#####UI2.0####### initInError");
	// // 初始化资源文件
	// UIResLoader.getInstance().startLoadResource(new ResLoadedListener() {
	// @Override
	// public void onResLoaded() {
	// LogUtil.logd("onResLoaded");
	// runOnUIThread(new Runnable() {
	// @Override
	// public void run() {
	// try {
	// ConfigUtil.initThemeType(Integer.valueOf(LayouUtil.getString("theme_code")));
	// RecordWin2.getInstance();
	// ViewConfiger.getInstance().initThemeConfig(); //
	// 需要让先RecordWin2读取到分辨率等信息，然后才能加载对应的资源文件
	// ThemeConfigManager.getInstance().initThemeConfig();// 初始化主题间距配置
	// RecordWin2Manager.getInstance().init();
	// WinLayoutManager.getInstance().init(); // 初始化当前layout所需要的各个View
	// RecordWin2.getInstance().updateWinLayout(WinLayoutManager.getInstance().getLayout());
	// // 初始化窗口
	// if (mInitListener != null) {
	// mInitListener.onSuccess();
	// }
	// } catch (Exception e) {
	// LogUtil.loge("UI2.0 in error init error!");
	// if (mInitListener != null) {
	// mInitListener.onErrorError();
	// }
	// }
	// }
	// }, 0);
	// }
	// @Override
	// public void onException(String errorDsp) {
	// LogUtil.loge("load res file error:" + errorDsp);
	// if (mInitListener != null) {
	// mInitListener.onErrorError();
	// }
	// }
	// },true);
	// }
}
