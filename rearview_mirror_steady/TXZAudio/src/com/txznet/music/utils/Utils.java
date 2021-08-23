package com.txznet.music.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.fm.bean.Configuration;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.PlayConf;
import com.txznet.music.bean.response.RespCheck;
import com.txznet.music.ui.MediaPlayerActivity;

/**
 * @author telenewbie
 * @version 创建时间：2016年2月29日 上午10:50:21
 * 
 */
public class Utils {

	/**
	 * @desc <pre>
	 * 获取网络对象
	 * </pre>
	 * @author Erich Lee
	 * @date Mar 7, 2013
	 * @param context
	 * @return
	 */
	public static NetworkInfo getNetworkInfo(Context context) {
		if (null == context) {
			return null;
		}
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return connectivity.getActiveNetworkInfo();
	}

	/**
	 * @desc <pre>
	 * 检查网络是否连接
	 * </pre>
	 * @date Mar 7, 2013
	 * @param context
	 * @return
	 */
	public static boolean isNetworkConnected(Context context) {
		boolean netSataus = false;
		NetworkInfo networkInfo = getNetworkInfo(context);
		if (networkInfo != null) {
			netSataus = networkInfo.isAvailable();
		}
		return netSataus;
	}

	/**
	 * @desc <pre>
	 * 判断当前的网络状态
	 * </pre>
	 * @return
	 */
	public static boolean isFastMobileNetwork(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		LogUtil.logd("telephonyManager.getNetworkType()::"
				+ telephonyManager.getNetworkType());
		switch (telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_1xRTT:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_CDMA:
			return false; // ~ 14-64 kbps
		case TelephonyManager.NETWORK_TYPE_EDGE:
			return false; // ~ 50-100 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
			return true; // ~ 400-1000 kbps
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
			return true; // ~ 600-1400 kbps
		case TelephonyManager.NETWORK_TYPE_GPRS:
			return false; // ~ 100 kbps
		case TelephonyManager.NETWORK_TYPE_HSDPA:
			return true; // ~ 2-14 Mbps
		case TelephonyManager.NETWORK_TYPE_HSPA:
			return true; // ~ 700-1700 kbps
		case TelephonyManager.NETWORK_TYPE_HSUPA:
			return true; // ~ 1-23 Mbps
		case TelephonyManager.NETWORK_TYPE_UMTS:
			return true; // ~ 400-7000 kbps
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return false; // ~25 kbps
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
			return false;
		default:
			return false;
		}
	}

	/**
	 * 
	 * @param sid
	 * @return
	 */
	public static boolean isSong(int sid) {
		if (sid == 0) {// 本地歌曲
			return true;
		}
		if (StringUtils.isNotEmpty(SharedPreferencesUtils.getConfig())) {
			RespCheck check = JsonHelper.toObject(RespCheck.class,
					SharedPreferencesUtils.getConfig());
			if (check != null && check.getArrPlay() != null) {
				Iterator<PlayConf> iterator = check.getArrPlay().iterator();
				if (iterator != null) {
					while (iterator.hasNext()) {
						PlayConf playConf = (PlayConf) iterator.next();
						if (playConf.getSid() == sid) {
								return playConf.getType() == 1;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * 获得歌曲的源ID
	 * 
	 * @return
	 */
	public static List<Integer> getSongSid() {
		List<Integer> sids=new ArrayList<Integer>();
		sids.add(0);
		if (StringUtils.isNotEmpty(SharedPreferencesUtils.getConfig())) {
			RespCheck check = JsonHelper.toObject(RespCheck.class,
					SharedPreferencesUtils.getConfig());
			if (check != null && check.getArrPlay() != null) {
				Iterator<PlayConf> iterator = check.getArrPlay().iterator();
				if (iterator != null) {
					while (iterator.hasNext()) {
						PlayConf playConf = (PlayConf) iterator.next();
						if (playConf.getType() == 1) {
							sids.add(playConf.getSid());
						}
					}
				}
			}
		}
		return sids;
	}
	/**
	 * 获得电台的源ID
	 * 
	 * @return
	 */
	public static List<Integer> getfmSid() {
		List<Integer> sids=new ArrayList<Integer>();
		if (StringUtils.isNotEmpty(SharedPreferencesUtils.getConfig())) {
			RespCheck check = JsonHelper.toObject(RespCheck.class,
					SharedPreferencesUtils.getConfig());
			if (check != null && check.getArrPlay() != null) {
				Iterator<PlayConf> iterator = check.getArrPlay().iterator();
				if (iterator != null) {
					while (iterator.hasNext()) {
						PlayConf playConf = (PlayConf) iterator.next();
						if (playConf.getType() != 1) {
							sids.add(playConf.getSid());
						}
					}
				}
			}
		}
		return sids;
	}


	/**
	 * 根据不同的类型返回不同的源
	 * 
	 * @param type
	 *            音乐，电台，直播等
	 * @return
	 */
	public static List<Integer> getSidType(int type) {
		List<Integer> songSids = new ArrayList<Integer>();
		if (StringUtils.isNotEmpty(SharedPreferencesUtils.getConfig())) {
			RespCheck check = JsonHelper.toObject(RespCheck.class,
					SharedPreferencesUtils.getConfig());
			if (check != null && check.getArrPlay() != null) {
				Iterator<PlayConf> iterator = check.getArrPlay().iterator();
				if (iterator != null) {
					while (iterator.hasNext()) {
						PlayConf playConf = (PlayConf) iterator.next();
						if (playConf.getType() != 0) {
							if (playConf.getType() == type) {
								songSids.add(playConf.getSid());
							}
						}
					}
				}
			}
		}

		return songSids;
	}

	public static String parseString(String ids) {
		if (StringUtils.isEmpty(ids)) {
			return "";
		}
		return ids.split(",")[0];
	}

	/**
	 * 将字符串转换成为long,以“，”进行分割取第一个，转换失败则返回当前时间
	 * 
	 * @param value
	 * @return
	 */
	public static long toLong(String value) {
		long returnValue = 0;
		try {
			if (!TextUtils.isEmpty(value)) {
				returnValue = Long.parseLong(parseString(value));
			}
		} catch (Exception e) {
			LogUtil.logw("to parse Long error::" + value);
		}
		return returnValue;

	}

	public static SpannableString getTitleAndArtists(String title,
			String artists) {
		StringBuffer sBuffer = new StringBuffer();
		if (StringUtils.isNotEmpty(artists)) {
			sBuffer.append(artists);
		}
		if (sBuffer.length() > 0) {
			sBuffer.insert(0, "-");
		}

		SpannableString spannableString = new SpannableString(title
				+ sBuffer.toString());
		spannableString.setSpan(new AbsoluteSizeSpan(30), 0, title.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		ColorStateList color = ColorStateList.valueOf(GlobalContext.get()
				.getResources().getColor(R.color.gray));
		spannableString.setSpan(new TextAppearanceSpan(null, Typeface.NORMAL,
				24, color, null), title.length(), spannableString.length(),
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannableString;
	}

	/**
	 * 判断服务是否后台运行
	 * 
	 * @param context
	 *            Context
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRun(Context mContext, String className) {
		boolean isRun = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(40);
		int size = serviceList.size();
		for (int i = 0; i < size; i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRun = true;
				break;
			}
		}
		return isRun;
	}

	public static int getProcessIdByPkgName(String pkgName) {
		ActivityManager activityManager = (ActivityManager) GlobalContext.get()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> infos = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo info : infos) {
			if (info.processName.equals(pkgName)) {
				return info.pid;
			}
		}
		return -1;
	}

	/**
	 * 是否强制跳转到播放器界面
	 * 
	 * @param force
	 */
	public static void jumpTOMediaPlayerAct(final boolean force) {
		LogUtil.logd("music:jumpTOMediaPlayerAct:" + force);
		if (!force) {
			// 当前界面是导航界面，则不弹
			// 判断当前界面是否是导航界面
			if (getTopActivity() != null) {
				String naviPackage = getTopActivity().getPackageName();
				LogUtil.logd("music:jumpTOMediaPlayerAct top packageName is " + naviPackage);
				if (TextUtils.isEmpty(SharedPreferencesUtils
						.getNotOpenAppPName())) {
					LogUtil.logd("music:jumpTOMediaPlayerAct didn't set not open app packages name");
					return;
				}
				JSONBuilder builder = new JSONBuilder(
						SharedPreferencesUtils.getNotOpenAppPName());
				String[] val = builder.getVal("data", String[].class);
				if (val == null) {// 默认不打开界面
					LogUtil.logd("music:jumpTOMediaPlayerAct packages name data is null");
					return;
				}
				if (null != val && val.length > 0) {
					for (int i = 0; i < val.length; i++) {
						LogUtil.logd("get data" + val[i]);
						if (val[i] != null) {
							if (val[i].equals(naviPackage)) {
								LogUtil.logd("music:jumpTOMediaPlayerAct not open package name is " + naviPackage);
								return;
							}
						}
					}
				} else {
					if ("com.autonavi.minimap".equals(naviPackage)
							|| "com.baidu.BaiduMap".equals(naviPackage)
							|| "cld.navi.c3027.mainframe".equals(naviPackage)
							|| "cld.navi.k3618.mainframe".equals(naviPackage)
							|| "cn.ritu.rtnavi".equals(naviPackage)
							|| "com.mxnavi.mxnavi".equals(naviPackage)
							|| "com.txznet.amap".equals(naviPackage)
							|| "com.autonavi.amapauto".equals(naviPackage)
							|| "com.autonavi.xmgd.navigator"
									.equals(naviPackage)) {
						return;
					} else {
						LogUtil.logd("current navi package is:" + naviPackage);
					}
				}
			}
		}
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				Intent it = null;
				if (StringUtils.isEmpty(Configuration.getInstance().getString(
						"main"))) {
					it = new Intent(GlobalContext.get(),
							MediaPlayerActivity.class);
				} else {
					try {
						it = new Intent(GlobalContext.get(), Class
								.forName(Configuration.getInstance().getString(
										"main")));
					} catch (ClassNotFoundException e) {
						LogUtil.loge("[Exception] " + " ClassNotFoundException");
						return;
					}
				}
				it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					GlobalContext.get().startActivity(it);
				} catch (Exception e) {
					LogUtil.loge("open mainactivity error!");
				}
			}
		}, 1000);
	}

	private static WinDialog winDialog;
	private static TextView tvSound;
	private static View soundView;

	public static void showSoundView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				try {
					if (winDialog == null) {
						soundView = View.inflate(GlobalContext.get(),
								R.layout.soundview, null);
						tvSound = (TextView) soundView
								.findViewById(R.id.tv_sound);

						winDialog = new WinDialog(true) {

							@Override
							protected View createView() {
								return soundView;
							}
						};
					}
					winDialog.show();
					tvSound.setText(((int) (Constant.currentSound * 10 + 0.5) * 10)
							+ "%");
					AppLogic.removeUiGroundCallback(dismissDialogRunnable);
					AppLogic.runOnUiGround(dismissDialogRunnable, 1000);
				} catch (Exception e) {
					ToastUtils.showShort("show Dialog error:" + e.getMessage());
					winDialog = null;
				}
			}
		}, 0);
	}

	public static void dismissSoundView() {
		if (winDialog != null && winDialog.isShowing()) {
			winDialog.dismiss();
		}
	}

	static Runnable dismissDialogRunnable = new Runnable() {

		@Override
		public void run() {
			dismissSoundView();
		}
	};

	public static ComponentName getTopActivity() {
		ActivityManager manager = (ActivityManager) GlobalContext.get()
				.getSystemService(GlobalContext.get().ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);

		if (runningTaskInfos != null) {
			int mRunningCount = runningTaskInfos.get(0).numRunning;
			return runningTaskInfos.get(0).topActivity;
		} else
			return null;
	}
}
