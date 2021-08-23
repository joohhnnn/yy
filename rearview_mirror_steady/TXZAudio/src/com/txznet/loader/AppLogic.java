package com.txznet.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.SongListFragment;
import com.txznet.music.fragment.logic.LocalLogic;
import com.txznet.music.fragment.manager.DBManage;
import com.txznet.music.receiver.HeadSetHelper;
import com.txznet.music.receiver.HeadSetHelper.OnHeadSetListener;
import com.txznet.music.receiver.MusicManager;
import com.txznet.music.receiver.UIHelper;
import com.txznet.music.ui.MainActivity;
import com.txznet.music.utils.AudioManagerHelper;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZAsrManager.CommandListener;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class AppLogic extends AppLogicBase {

	private static final String TAG = "[MUSIC][APP] ";
	

	@Override
	public void onCloseApp() {
		UIHelper.exit();
	}

	private DisplayMetrics displayMetrics;

	private boolean isFatal;
	public static int width;
	// public static int height;
	public static float density;
	public static RefWatcher watcher;
	private final static int PREDATA = 5;// 音量增减的倍数

	@Override
	public void onCreate() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onCreate()");
		super.onCreate();
		
		Constant.setIsExit(false);
		
		// 注册监听器
		DataInterfaceBroadcastHelper.initListeners();
		if (!isMainProcess()) {
			return;
		}
		regOwnerComm();

		configImageLoader();
		DBManage.getInstance();
		WindowManager manager = (WindowManager) getApp().getSystemService(
				Context.WINDOW_SERVICE);
		if (displayMetrics == null) {
			displayMetrics = new DisplayMetrics();
			manager.getDefaultDisplay().getMetrics(displayMetrics);
		}

		width = displayMetrics.widthPixels;
		// height = displayMetrics.heightPixels;
		density = displayMetrics.density;

		if (BuildConfig.DEBUG) {
			watcher = LeakCanary.install(getApp());
			// StrictMode.setThreadPolicy(new
			// StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
			// StrictMode.setVmPolicy(new
			// StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
		}

		String config = SharedPreferencesUtils.getConfig();
		if (TextUtils.isEmpty(config)) {
			config = "{\"arrPlay\":[{\"bNeedProcess\":false,\"play\":1,\"sid\":1,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":2,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":3,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":4,\"type\":2},{\"bNeedProcess\":false,\"play\":1,\"sid\":5,\"type\":3},{\"bNeedProcess\":false,\"play\":1,\"sid\":6,\"type\":1},{\"bNeedProcess\":false,\"play\":1,\"sid\":7,\"type\":1}],\"logoTag\":7}";
			SharedPreferencesUtils.setConfig(config);
		}
		
		LogUtil.logd(TAG + "last exit play::" + SharedPreferencesUtils.getIsPlay() + ",set play::"
				+ SharedPreferencesUtils.getAppFirstPlay());
		if (SharedPreferencesUtils.getIsPlay()
				&& SharedPreferencesUtils.getAppFirstPlay()) {
			MediaPlayerActivityEngine.getInstance().play();
		}else{
			LogUtil.logd(TAG + "set is play false");
			SharedPreferencesUtils.setIsPlay(false);
		}

		
		MusicManager.getInstance();
		// runOnBackGround(MusicManager.mRunnableRefreshMediaList, 1000);
		runOnSlowGround(NetHelp.checkTimeOut, 30000);

//		HeadSetHelper.getInstance().open(getApp());
		// 音乐上报数据
		NetHelp.sendReportData(Action.ACT_LOGIN);
		// 自动扫描sd卡中的数据
		AppLogic.runOnSlowGround(new Runnable() {

			@Override
			public void run() {
				LocalLogic.getDataFromSDCard();
			}
		}, 6 * 1000);

		AppLogic.runOnSlowGround(new Runnable() {

			@Override
			public void run() {
				AppLogic.removeSlowGroundCallback(this);
				checkForDistSpace();
				AppLogic.runOnSlowGround(this, 30 * 1000 * 60);
			}
		}, 30 * 1000 * 60);

		SyncCoreData.syncCurStatusFullStyle();

		if (AppLogic.isMainProcess()) {
			IntentFilter filter = new IntentFilter(
					"com.txznet.music.action.REQ_SYNC");
			getApp().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					SyncCoreData.syncCurMusicModel();
					SyncCoreData.syncCurPlayerStatus();
				}
			}, filter);
		}
	}

	/**
	 * 注册自己的命令字
	 */
	public static void regOwnerComm() {
		LogUtil.logd(TAG + "reg::success::");
		regAsrCommand();
	}

	public static void regAsrCommand() {
		if (SharedPreferencesUtils.getNeedAsr()) {
			AsrComplexSelectCallback callback = new AsrComplexSelectCallback() {

				@Override
				public void onCommandSelected(String type, String command) {
					if (Constant.getIsExit()) {
						LogUtil.logd(TAG + "music  miss wake up");// 误唤醒
						return;
					}
					LogUtil.logd(TAG + TAG + "type:" + type + ",command:"
							+ command+"/"+isWakeupResult());
					if ("CMD_PLAYER_PREVIOUS".equals(type)) {
						if (isWakeupResult() == true) {
							if (MediaPlayerActivityEngine.getInstance()
									.isPlaying()) {
								MediaPlayerActivityEngine.getInstance().last();
							}
						} else {
							TtsUtil.speakTextOnRecordWin(
									"RS_VOICE_SPEAK_PLAY_PREV",
									Constant.RS_VOICE_SPEAK_PLAY_PREV, true,
									new Runnable() {

										@Override
										public void run() {
											MediaPlayerActivityEngine
													.getInstance().last();
										}
									});
						}
						// TXZResourceManager.getInstance().dissmissRecordWin();
					}
					if ("CMD_PLAYER_NEXT".equals(type)) {
						if (isWakeupResult() == true) {
							// 只有在播放状态才支持上/下一首
							if (MediaPlayerActivityEngine.getInstance()
									.isPlaying()) {
								MediaPlayerActivityEngine.getInstance().next();
							}
						} else {
							TtsUtil.speakTextOnRecordWin(
									"RS_VOICE_SPEAK_PLAY_NEXT",
									Constant.RS_VOICE_SPEAK_PLAY_NEXT, true,
									new Runnable() {

										@Override
										public void run() {
											MediaPlayerActivityEngine
													.getInstance().next();
										}
									});
						}
					}
					if ("CMD_PLAYER_REDUCE".equals(type)) {
						if (MediaPlayerActivityEngine.getInstance().isPlaying()) {
							if (isWakeupResult() == false) {
								TtsUtil.speakTextOnRecordWin(
										"RS_VOICE_SPEAK_FINSISH_SOUND_REDUCE",
										Constant.RS_VOICE_SPEAK_FINSISH_SOUND_REDUCE,
										true, null);
							}
							AudioManagerHelper.reduceSound();
							Utils.showSoundView();
						}
					}
					if ("CMD_PLAYER_ADD_VOLUM".equals(type)) {
						if (MediaPlayerActivityEngine.getInstance().isPlaying()) {
							if (isWakeupResult() == false) {
								TtsUtil.speakTextOnRecordWin(
										"RS_VOICE_SPEAK_FINSISH_SOUND_UP",
										Constant.RS_VOICE_SPEAK_FINSISH_SOUND_UP,
										true, null);
							}
							AudioManagerHelper.addSound();
							Utils.showSoundView();
						}
					}
					if ("CMD_PLAYER_PLAY".equals(type)) {
						if (isWakeupResult() == false) {
							if (MediaPlayerActivityEngine.getInstance()
									.isPlaying()) {
								TtsUtil.speakTextOnRecordWin(
										"RS_VOICE_SPEAK_PLAY_ALREADY",
										Constant.RS_VOICE_SPEAK_PLAY_ALREADY,
										false, null);
								return;
							}
							TtsUtil.speakTextOnRecordWin(
									"RS_VOICE_SPEAK_PLAY_PLAY",
									Constant.RS_VOICE_SPEAK_PLAY_PLAY, true,
									null);
						}
						MediaPlayerActivityEngine.getInstance().play();
					}
					if ("CMD_PLAYER_PAUSE".equals(type)) {
						if (isWakeupResult() == false) {
							TtsUtil.speakTextOnRecordWin(
									"RS_VOICE_SPEAK_PLAY_PAUSE",
									Constant.RS_VOICE_SPEAK_PLAY_PAUSE, true,
									null);
						}
						MediaPlayerActivityEngine.getInstance().pause();
					}
					super.onCommandSelected(type, command);
				}

				@Override
				public boolean needAsrState() {
					return false;
				}

				@Override
				public String getTaskId() {
					return "SPEAK_MUSIC_PLAYER_TEXT";
				}
			};
			callback.addCommand("CMD_PLAYER_PAUSE", "暂停暂停")
					.addCommand("CMD_PLAYER_PLAY", "播放播放")
					.addCommand("CMD_PLAYER_NEXT", "下一首")
					.addCommand("CMD_PLAYER_PREVIOUS", "上一首");
			if (!SharedPreferencesUtils.isCloseVolume()) {
				callback.addCommand("CMD_PLAYER_ADD_VOLUM", "增大音量", "放大声音"
				/*
				 * , "放大音量", "增加音量", "调大音量", "调大声音"
				 */).addCommand("CMD_PLAYER_REDUCE", "减小音量", "减小声音"
				/*
				 * , "缩小声音", "减小声音", "缩小声音", "调小声音", "调小音量", "降低音量"
				 */);
			}
			AsrUtil.useWakeupAsAsr(callback);
		}
	}

	public static RefWatcher getRefWatcher() {
		return watcher;
	}

	private static RequestQueue requestQueue;

	/**
	 * 获取到一个全局的RequestQueue
	 *
	 * @return
	 */
	public static RequestQueue getServerQueue() {
		if (null == requestQueue) {
			requestQueue = Volley.newRequestQueue(getApp());
			requestQueue.start();
		}
		return requestQueue;
	}

	// /**
	// * 配置ImageLoader
	// */
	// private void configImageLoader() {
	// ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getApp()));
	// }

	/**
	 * 配置ImageLoader
	 */
	private void configImageLoader() {
		File discCacheDir = StorageUtils.getOwnCacheDirectory(getApp(),
				"/txz/Cache/images");
		int memClass = ((android.app.ActivityManager) getApp()
				.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		memClass = memClass > 32 ? 32 : memClass;
		// 使用可用内存的1/8作为图片缓存
		final int cacheSize = 1024 * 1024 * memClass / 4;
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApp()).memoryCacheExtraOptions(240, 240)
				.taskExecutor(Executors.newFixedThreadPool(10))
				.memoryCache(new LruMemoryCache(cacheSize))
				.diskCache(new UnlimitedDiskCache(discCacheDir))
				// .writeDebugLogs()// 输出Debug信息，释放版本的时候，不需要这句
				.build();
		ImageLoader.getInstance().init(config);

	}

	@Override
	public void onTerminate() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onTerminate()");
		super.onTerminate();
	}

	@Override
	public void caughtException() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::caughtException()");
		super.caughtException();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		LogUtil.logd(TAG + "[life][" + this.hashCode()
				+ "]::onConfigurationChanged()" + newConfig);
		SyncCoreData.syncCurStatusFullStyle();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onLowMemory() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onLowMemory()");
		HeadSetHelper.getInstance().close(getApp());
		MusicManager.getInstance().unregister();
		super.onLowMemory();
	}

	@Override
	public void onTrimMemory(int level) {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::onTrimMemory()，"
				+ level);
		super.onTrimMemory(level);
	}

	@Override
	public void destroy() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::destroy()");
		super.destroy();
	}

	@Override
	public ClassLoader getClassLoader() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getClassLoader()");
		return super.getClassLoader();
	}

	@Override
	public AssetManager getAssets() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getAssets()");
		return super.getAssets();
	}

	@Override
	public Resources getResources() {
		LogUtil.logd(TAG + "[life][" + this.hashCode() + "]::getResources()");
		return super.getResources();
	}

	// 播放列表共同维护同一份列表
	// public static List<Audio> audios = new ArrayList<Audio>();
	// public static List<HistoryAudio> historyAudios = new
	// ArrayList<HistoryAudio>();

	public static String getSongDir() {
		return Environment.getExternalStorageDirectory() + "/txz/cache/song";
	}

	private static final long AVAILABLE_SIZE_LIMIT = 1024 * 1024 * 100L * 10; // 100mb
	private static final long MAX_BUFF_SIZE = 1024 * 1024 * 100L; //
	private WinNotice mSpaceWarn = null;

	private void checkForDistSpace() {
		File sdcard = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(sdcard.getAbsolutePath());
		long blockSize = sf.getBlockSize();
		long availCount = sf.getAvailableBlocks();
		if (availCount * blockSize < AVAILABLE_SIZE_LIMIT) {
			// TtsUtil.speakResource("RS_VOICE_SPEAK_ROOM_NOT_FREE",
			// Constant.RS_VOICE_SPEAK_ROOM_NOT_FREE);
			// runOnUiGround(new Runnable() {
			// @Override
			// public void run() {
			// if (mSpaceWarn == null) {
			// mSpaceWarn = new WinNotice(true) {
			// @Override
			// public void onClickOk() {
			// cancel();
			// }
			// }.setMessage(Constant.RS_VOICE_SPEAK_ROOM_NOT_FREE);
			// }
			// mSpaceWarn.show();
			// }
			// }, 0);
		} else {
			long totalSize = 0;
			List<File> buffers = new LinkedList<File>();
			File file2 = new File(getSongDir());
			if (file2 != null) {
				File[] listFiles = file2.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						if (filename.endsWith(".tmp")
								|| filename.endsWith(".cfg")) {
							return true;
						}
						return false;
					}
				});
				if (listFiles != null && listFiles.length > 0) {
					for (File f : listFiles) {
						if (f != null) {
							totalSize += f.length();
							buffers.add(f);
						}
					}
				}
			}
			File file3 = new File(getOtherResDir());
			if (file3 != null) {
				File[] listFiles = new File(getOtherResDir()).listFiles();
				if (listFiles != null && listFiles.length > 0) {
					for (File f : listFiles) {
						totalSize += f.length();
						if (!f.getName().endsWith(".nomedia")) {
							buffers.add(f);
						}
					}
				}
			}
			if (totalSize > MAX_BUFF_SIZE) {
				Collections.sort(buffers, new Comparator<File>() {
					@Override
					public int compare(File lhs, File rhs) {
						if (lhs.lastModified() == rhs.lastModified()) {
							return 0;
						}
						return lhs.lastModified() < rhs.lastModified() ? -1 : 1;
					}
				});
				long offset = (long) (totalSize * 0.3f);// 删除30%的数据
				for (int i = 0; i < buffers.size(); i++) {
					File file = buffers.get(i);
					LogUtil.logd(TAG + "checkForDistSpace::" + file.getName()
							+ ", offset=" + (offset) + ", filesize="
							+ file.length());
					offset -= file.length();
					file.delete();
					buffers.remove(i);
					i--;
					if (offset <= 0) {
						break;
					}
				}
			} else {
				buffers = null;
			}
		}
	}
	private String getOtherResDir() {
		return Environment.getExternalStorageDirectory() + "/txz/cache/other";
	}
}
