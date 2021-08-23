package com.txznet.sdk;

import java.util.List;

import org.json.JSONObject;

import com.alibaba.fastjson.JSONArray;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * 类名：系统级管理器
 * 类描述：针对需要root权限的功能和兼容自定义ROM与原生接口不一致的地方
 *         包含AppMgrTool(应用开关工具)、VolumeMgrTool(音量调整工具)、ScreenLightTool(亮度调整工具)、
 *         WakeLockTool(屏幕唤醒锁工具)、MuteAllTool(静音逻辑工具)、ScreenSleepTool(屏幕休眠工具)
 *         以及新手引导功能相关操作
 */
public class TXZSysManager {
	private static TXZSysManager sInstance;

	private TXZSysManager() {
	}

	/**
	 * 此类为单例模式
	 *
	 * @return 实例
	 */
	public static TXZSysManager getInstance() {
		if (sInstance == null) {
			synchronized (TXZSysManager.class) {
				if (sInstance == null) {
					sInstance = new TXZSysManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetVolumeMgrTool) {
			if (mVolumeMgrTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.volume.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.volume.settool", null, null);
				if (mMinVolumeValue != null && mMaxVolumeValue != null) {
					setVolumeDistance(mMinVolumeValue, mMaxVolumeValue);
				}
			}
		}
		if (mHasSetWakeLockTool) {
			if (mWakeLockTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.wakelock.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.wakelock.settool", null, null);
			}
		}
		if (mHasSetAppMgrTool) {
			if (mAppMgrTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.appmgr.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.appmgr.settool", null, null);
			}
		}
		if (mHasSetSleepTool) {
			if (mScreenSleepTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.screensleep.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.screensleep.settool", null, null);
			}
		}
		if (mHasSetMuteAllTool) {
			if (mMuteAllTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.muteall.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.sys.muteall.settool", null, null);
			}
		}
		if (mHasSetScreenLightTool) {
			setScreenLightTool(mScreenLightTool);
		}
		syncAppInfoListAgain();
	}

	/**
	 * 接口名：音量控制工具
	 * 接口描述：安卓原生音量接口不适用时，可实现此接口
	 */
	public static interface VolumeMgrTool {
		/**
		 * 方法名：增大音量
		 * 方法描述：增大音量操作逻辑，语义理解为增大音量相关时会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		void incVolume();

		/**
		 * 方法名：减小音量
		 * 方法描述：减小音量操作逻辑，语义理解为减小音量相关时会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		void decVolume();

		/**
		 * 方法名：最大音量
		 * 方法描述：最大音量操作逻辑，语义理解为最大音量相关时会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		void maxVolume();

		/**
		 * 方法名：最小音量
		 * 方法描述：最小音量操作逻辑，语义理解为最小音量相关时会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		void minVolume();

		/**
		 * 方法名：静音逻辑
		 * 方法描述：静音逻辑，语义理解为静音相关时会回调此方法，根据参数实现逻辑
		 *
		 * @param enable true时需要系统静音，反之
		 */
		void mute(boolean enable);

		/**
		 * 方法名：是否为最大音量
		 * 方法描述：当前系统音量状态，系统增大音量时会调用此方法，根据返回值调整语音播报
		 *
		 * @return true 当前系统音量为最大，反之
		 */
		public boolean isMaxVolume();

		/**
		 * 方法名：是否为最小音量
		 * 方法描述：静音逻辑，语义理解为静音相关时会回调此方法，根据参数实现逻辑
		 *
		 * @return true 是最小 false 不是最小
		 */
		public boolean isMinVolume();

		/**
		 * 方法名：减小指定音量
		 * 方法描述：音量调整逻辑，语义理解音量减小X相关时回调，根据返回值适应此逻辑，实现前需要设置音量范围值
		 *
		 * @param decValue 需要系统减小的音量值
		 * @return 是否需要此语义，true则适用相关语义，false时相关语义不适用，被提示不支持
		 */
		public boolean decVolume(int decValue);

		/**
		 * 方法名：增大指定音量
		 * 方法描述：音量调整逻辑，语义理解音量减小X相关时回调，根据返回值适应此逻辑，实现前需要设置音量范围值
		 *
		 * @param incValue 需要系统增加的音量值
		 * @return 是否需要此语义，true则适用相关语义，false时相关语义不适用，被提示不支持
		 */
		public boolean incVolume(int incValue);

		/**
		 * 方法名：设置到指定音量
		 * 方法描述：音量调到到X值，根据返回值适应此逻辑，实现前需要设置音量范围值
		 *
		 * @param value 设置音量值
		 * @return 是否需要此语义，true则适用相关语义，false时相关语义不适用，被提示不支持
		 */
		public boolean setVolume(int value);
	}

	/*
	 * 给音量工具用的同步回调 因为需要返回值
	 *
	 */
	public interface VolumeSettingCallBack {
		/*
		 * 是否成功执行
		 * @param isOperateSuccess
		 */
		public void onOperateResult(boolean isOperateSuccess);

		/*
		 * 同步调用失败
		 */
		public void onError(int errorCode);
	}

	private Integer mMaxVolumeValue;
	private Integer mMinVolumeValue;

	/**
	 * 方法名：设置音量的调节范围
	 * 方法描述：音量的范围设置，需要用到调节音量值相关语义时，必须设置此值
	 *
	 * @param minVolumeValue 设定系统音量的下界
	 * @param maxVolumeValue 设定系统音量的上界
	 * @return 范围值是否设置成功
	 */
	public boolean setVolumeDistance(int minVolumeValue,int maxVolumeValue){
		if (minVolumeValue >= 0 && maxVolumeValue > minVolumeValue) {
			mMaxVolumeValue = maxVolumeValue;
			mMinVolumeValue = minVolumeValue;
			JSONBuilder json = new JSONBuilder();
			json.put("maxVal", mMaxVolumeValue);
			json.put("minVal", mMinVolumeValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.volume.setvolumedistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}

	private boolean mHasSetVolumeMgrTool = false;
	private VolumeMgrTool mVolumeMgrTool = null;

	/**
	 * 方法名：设置音量控制工具
	 * 方法描述：安卓原生音量接口不适用时，通过此方法实现音量工具接口
	 *
	 * @param volumeMgrTool 音量工具实例
	 */
	public void setVolumeMgrTool(VolumeMgrTool volumeMgrTool) {
		mHasSetVolumeMgrTool = true;
		mVolumeMgrTool = volumeMgrTool;
		if (mVolumeMgrTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.volume.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.volume.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("decVolume")) {
					if (data != null && data.length > 0) {
						data = ( mVolumeMgrTool.decVolume(new JSONBuilder(data)
								.getVal("data", Integer.class, 0)) + "").getBytes() ;
					} else {
						mVolumeMgrTool.decVolume();
					}
				} else if (command.equals("incVolume")) {
					if (data != null && data.length > 0) {
						data = ( mVolumeMgrTool.incVolume(new JSONBuilder(data)
								.getVal("data", Integer.class, 0)) + "").getBytes();
					} else {
						mVolumeMgrTool.incVolume();
					}
				} else if (command.equals("maxVolume")) {
					mVolumeMgrTool.maxVolume();
				} else if (command.equals("minVolume")) {
					mVolumeMgrTool.minVolume();
				} else if (command.equals("isMaxVolume")) {
					data = (mVolumeMgrTool.isMaxVolume()+"").getBytes();
				} else if (command.equals("isMinVolume")) {
					data = (mVolumeMgrTool.isMinVolume()+"").getBytes();
				} else if (command.equals("mute")) {
					try {
						JSONObject doc = new JSONObject(new String(data));
						boolean enable = doc.getBoolean("enable");
						mVolumeMgrTool.mute(enable);
					} catch (Exception e) {
					}
				} else if (command.equals("setVolume")) {
					if (data != null && data.length > 0) {
						data = ( mVolumeMgrTool.setVolume(new JSONBuilder(data)
								.getVal("data", Integer.class, 0)) + "").getBytes();
					}
				}
				return data;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sys.volume.settool", null, null);
	}

	/**
	 * 接口名：静音工具
	 * 接口描述：语音内部静音逻辑不满足需求时，需要实现此接口
	 */
	public static interface MuteAllTool {
		/**
		 * 设置静音
		 *
		 * @param enable
		 *            设置为true则静音，false则恢复声音
		 */
		void mute(boolean enable);
	}

	private boolean mHasSetMuteAllTool = false;
	private MuteAllTool mMuteAllTool = null;

	/**
	 * 方法名：设置静音音量逻辑工具
	 * 方法描述：语音内部静音逻辑不满足需求时，需要通过此方法实现静音逻辑接口
	 *
	 * @param muteAllTool 静音工具实例
	 */
	public void setMuteAllTool(MuteAllTool muteAllTool) {
		mHasSetMuteAllTool = true;
		mMuteAllTool = muteAllTool;
		if (mMuteAllTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.muteall.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.muteall.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("mute")) {
					mMuteAllTool.mute(true);
				} else if (command.equals("unmute")) {
					mMuteAllTool.mute(false);
				}
				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sys.muteall.settool", null, null);
	}

	/**
	 * 接口名：屏幕唤醒锁工具
	 * 接口描述：屏幕唤醒锁工具，系统屏幕开关操作无权限时，需要实现此接口
	 */
	public static interface WakeLockTool {
		/**
		 * 方法名：申请屏幕锁，即申请打开屏幕
		 * 方法描述：请求屏幕唤醒锁，调用该接口应该点亮屏幕，直到调用release为止，屏幕不应该主动关闭
		 */
		void acquire();

		/**
		 * 方法名：释放屏幕锁
		 * 方法描述：释放屏幕唤醒锁
		 */
		void release();
	}

	private boolean mHasSetWakeLockTool = false;
	private WakeLockTool mWakeLockTool = null;

	/**
	 * 方法名：设置唤醒锁工具
	 * 方法描述：屏幕唤醒锁工具，系统屏幕开关操作无权限时，通过此方法设置屏幕唤醒锁逻辑
	 *
	 * @param wakeLockTool 需要设置的唤醒锁工具
	 */
	public void setWakeLockTool(WakeLockTool wakeLockTool) {
		mHasSetWakeLockTool = true;
		mWakeLockTool = wakeLockTool;
		if (mWakeLockTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.wakelock.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.wakelock.",
				new CommandProcessor() {
					@Override
					public byte[] process(String packageName, String command,
							byte[] data) {
						if (command.equals("acquire")) {
							mWakeLockTool.acquire();
							return "true".getBytes();
						} else if (command.equals("release")) {
							mWakeLockTool.release();
							return "true".getBytes();
						}
						return null;
					}
				});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sys.wakelock.settool", null, null);
	}

	/**
	 * 类名：应用管理工具
	 * 类描述：应用管理工具，语音默认权限无法操作打开或关闭App时，可以重写此逻辑
	 */
	public static abstract class AppMgrTool {
		/**
		 * 方法名：打开对应包名的应用
		 * 方法描述：语音打开App时，会使用此默认逻辑，若无权限可重写
		 *
		 * @param packageName 需要打开的应用进程名（包名）
		 */
		public void openApp(String packageName) {
			if (GlobalContext.get() != null) {
				Intent in = GlobalContext.get().getPackageManager()
						.getLaunchIntentForPackage(packageName);
				if (in != null) {
					in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					GlobalContext.get().startActivity(in);
				}
			}
		}

		/**
		 * 方法名：关闭对应包名的应用
		 * 方法描述：语音关闭App时，会使用此默认逻辑，若无权限可重写
		 *
		 * @param packageName 需要关闭的应用进程名（包名）
		 */
		public void closeApp(final String packageName) {
			if (GlobalContext.get() != null) {
				Runnable r = new Runnable() {
					int n = 0;

					@Override
					public void run() {
						boolean running = isAppRunning(GlobalContext.get(),
								packageName);
						if (running) {
							try {
								n++;
								ActivityManager am = (ActivityManager) GlobalContext
										.get().getSystemService(
												Context.ACTIVITY_SERVICE);
								am.killBackgroundProcesses(packageName);
							} catch (Exception e) {
							}

							if (n < 50)
								new Handler(Looper.getMainLooper())
										.postDelayed(this, 0);
							return;
						}

						// 2秒后再检查次
						if (n > 0) {
							n = 0;
							new Handler(Looper.getMainLooper()).postDelayed(
									this, 2000);
						}
					}
				};
				new Handler(Looper.getMainLooper()).postDelayed(r, 0);
			}
		}
	}

	private static boolean isAppRunning(Context context, String packageName) {
		try {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
			for (RunningAppProcessInfo rapi : infos) {
				if (rapi.processName.equals(packageName))
					return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private boolean mHasSetAppMgrTool = false;
	private AppMgrTool mAppMgrTool = null;

	/**
	 * 方法名：设置应用管理工具
	 * 方法描述：应用管理工具，语音默认权限无法操作打开或关闭App时，可以通过此方法重写App管理工具逻辑
	 *
	 * @param appMgrTool 应用管理工具实例
	 */
	public void setAppMgrTool(AppMgrTool appMgrTool) {
		mHasSetAppMgrTool = true;
		mAppMgrTool = appMgrTool;
		if (mAppMgrTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.appmgr.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.appmgr.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("closeApp")) {
					try {
						JSONObject doc = new JSONObject(new String(data));
						String pkg = doc.getString("pkgName");
						mAppMgrTool.closeApp(pkg);
					} catch (Exception e) {
					}
				} else if (command.equals("openApp")) {
					try {
						JSONObject doc = new JSONObject(new String(data));
						String pkg = doc.getString("pkgName");
						mAppMgrTool.openApp(pkg);
					} catch (Exception e) {
					}
				}
				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sys.appmgr.settool", null, null);
	}

	/**
	 * 类名：应用信息类
	 * 类描述：包含应用的相关信息
	 */
	public static class AppInfo {
		/**
		 * 可选, 应用入口的Activity名，需要全称
		 */
		private String strActivityName;
		/**
		 * 应用的可读名(Label), 必填
		 */
		public String strAppName;
		/**
		 * 应用的包名, 必填
		 */
		public String strPackageName;
		/**
		 * 可选,其它相关参数
		 */
		private String strParams;
	}

	/**
	 * 方法名：同步应用信息列表
	 * 方法描述：将系统当前全部已经安装应用列表同步给语音，语音会自动处理打开、关闭应用相关语义
	 *
	 * @param appInfos 语音信息列表
	 */
	public void syncAppInfoList(AppInfo[] appInfos) {
		try {
			JSONArray jInfos = new JSONArray();
			for (AppInfo info : appInfos) {
				jInfos.add(new JSONBuilder().put("strAppName", info.strAppName)
						.put("strPackageName", info.strPackageName).build());
			}
			LogUtil.logd("syncAppInfoList list=" + jInfos);
			mLastAppInfoList = new JSONBuilder().put("infos", jInfos).toBytes();
			syncAppInfoListAgain();
		} catch (Exception e) {
		}
	}

	private byte[] mLastAppInfoList;

	// 重新同步应用列表
	private void syncAppInfoListAgain() {
		if (mLastAppInfoList != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.pkg.sync", mLastAppInfoList, null);
		}
	}

	/**
	 * 接口名：屏幕休眠工具
	 * 接口描述：屏幕休眠工具，语音默认权限无法关闭屏幕时，可以通过此方法重写关闭屏幕逻辑
	 */
	public static interface ScreenSleepTool {
		/**
		 * 方法名：通知屏幕关闭
		 * 方法描述：屏幕休眠 ，需要注意的是调用该接口时不等同于设备休眠
		 */
		public void goToSleep();
	}

	private boolean mHasSetSleepTool = false;
	private ScreenSleepTool mScreenSleepTool = null;

	/**
	 * 方法名：设置屏幕休眠工具
	 * 方法描述：安卓默认关屏接口无法适配时，通过此方法实现关闭屏幕逻辑
	 *
	 * @param screenSleepTool 设置屏幕休眠工具实例
	 */
	public void setScreenSleepTool(ScreenSleepTool screenSleepTool) {
		mHasSetSleepTool = true;
		mScreenSleepTool = screenSleepTool;
		if (mScreenSleepTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.screensleep.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.screensleep.",
				new CommandProcessor() {
					@Override
					public byte[] process(String packageName, String command,
							byte[] data) {
						if (command.equals("goToSleep")) {
							mScreenSleepTool.goToSleep();
						}
						return null;
					}
				});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sys.screensleep.settool", null, null);
	}

	/**
	 * 接口名：亮度工具
	 * 接口描述：安卓默认亮度接口无法适配时，需要实现此接口
	 */
	public static interface ScreenLightTool {
		/**
		 * 方法名：提高亮度
		 * 方法描述：提高亮度需要实现的逻辑。使用此接口后，提高亮度相关语义会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		public void incLight();

		/**
		 * 方法名：降低亮度
		 * 方法描述：降低亮度需要实现的逻辑。使用此接口后，降低亮度相关语义会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		public void decLight();

		/**
		 * 方法名：最大亮度
		 * 方法描述：最大亮度需要实现的逻辑。使用此接口后，最大亮度相关语义会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		public void maxLight();

		/**
		 * 方法名：最小亮度
		 * 方法描述：最小亮度需要实现的逻辑。使用此接口后，最小亮度相关语义会回调此方法，此方法内语音已处理播报，不需要额外TTS播报
		 */
		public void minLight();

		/**
		 * 方法名：当前系统亮度是否为最高
		 * 方法描述：判断当前系统是否亮度为最高，语音会根据此状态作出合理播报
		 *
		 * @return true 当前系统亮度已经为最高值，反之
		 */
		public boolean isMaxLight();

		/**
		 * 方法名：当前系统亮度是否为最低
		 * 方法描述：判断当前系统是否亮度为最低，语音会根据此状态作出合理播报
		 *
		 * @return true 当前系统亮度已经为最低值，反之
		 */
		public boolean isMinLight();

	}

	private boolean mHasSetScreenLightTool = false;
	private ScreenLightTool mScreenLightTool;

	/**
	 * 方法名：设置亮度工具
	 * 方法描述：安卓默认亮度接口无法适配时，通过此方法实现亮度逻辑
	 *
	 * @param tool 亮度工具实例
	 */
	public void setScreenLightTool(ScreenLightTool tool) {
		mHasSetScreenLightTool = true;
		mScreenLightTool = tool;
		if (mScreenLightTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.sys.screenlight.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.light.", new CommandProcessor() {

			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("light_up")) {
					mScreenLightTool.incLight();
				} else if (command.equals("light_down")) {
					mScreenLightTool.decLight();
				} else if (command.equals("light_max")) {
					mScreenLightTool.maxLight();
				} else if (command.equals("light_min")) {
					mScreenLightTool.minLight();
				} else if (command.equals("isMaxLight")) {
					data = (mScreenLightTool.isMaxLight()+"").getBytes();
				} else if (command.equals("isMinLight")) {
					data = (mScreenLightTool.isMinLight()+"").getBytes();
				}
				return data;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sys.screenlight.settool", null, null);
	}

	/**
	 * 方法名：打开新手引导动画
	 * 方法描述：语音版本2.9.0以下不支持，以上版本时，可以通过此接口打开新手引导动画
	 */
	public void openGuideAnim() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sys.openGuideAnim", null, null);
	}
}
