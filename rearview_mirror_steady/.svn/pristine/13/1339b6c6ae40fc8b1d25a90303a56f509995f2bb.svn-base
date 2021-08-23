package com.txznet.dvr;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZHandler;

public class DVRScaner {
	
	private static DVRScaner sInstance = new DVRScaner();
	
	public static DVRScaner getInstance() {
		return sInstance;
	}

	/**
	 * SD卡广播事件接收器
	 */
	private static BroadcastReceiver sSdcardEventReceiver;

	/**
	 * 视频扫描和抓拍处理线程
	 */
	private static HandlerThread sCaptureThread;
	private static TXZHandler sCaptureThreadHandler;

	private DVRScaner() {
		init();
	}
	/**
	 * 初始化
	 */
	private void init() {
		if (sCaptureThread == null) {
			sCaptureThread = new HandlerThread("DVRScan");
			sCaptureThread.start();
			sCaptureThreadHandler = new TXZHandler(sCaptureThread.getLooper());

			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_MEDIA_SHARED);// 如果SDCard未安装,并通过USB大容量存储共享返回
			filter.addAction(Intent.ACTION_MEDIA_MOUNTED);// 表明sd对象是存在并具有读/写权限
			filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// SDCard已卸掉,如果SDCard是存在但没有被安装
			filter.addAction(Intent.ACTION_MEDIA_CHECKING); // 表明对象正在磁盘检查
			filter.addAction(Intent.ACTION_MEDIA_EJECT); // 物理的拔出 SDCARD
			filter.addAction(Intent.ACTION_MEDIA_REMOVED); // 完全拔出
			filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
			sSdcardEventReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// SD卡事件5秒后开始扫描
					scanDVRHome(5 * 1000);
				}
			};
			GlobalContext.get().registerReceiver(sSdcardEventReceiver, filter);
			// 30s后开始扫描
			scanDVRHome(30 * 1000);
		}
	}

	/**
	 * 行车记录仪的根目录
	 */
	private static ArrayList<File> sDVRHome = new ArrayList<File>();

	/**
	 * @param f
	 *            要检查的文件
	 * @return 是否为视频文件
	 */
	public boolean isVideoFile(File f) {
		String name = f.getName().toLowerCase(Locale.SIMPLIFIED_CHINESE);
		return name.endsWith(".mp4") || name.endsWith(".3gp")
				|| name.endsWith(".mov");
	}

	/**
	 * 判断一个目录是否为DVR目录
	 */
	public boolean isDVRHome(File dir) {
		if (dir.isDirectory() == false) {
			return false;
		}
		File[] fs = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return isVideoFile(pathname);
			}
		});
		if (fs != null && fs.length > 0) {
			return true;
		}
		File[] ds = dir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return isDVRHome(pathname);
			}
		});
		if (ds != null && ds.length > 0) {
			return true;
		}
		return false;
	}

	private Runnable sRunScanHome = new Runnable() {
		@Override
		public void run() {
			LogUtil.logd("cain: run");
			try {
				sCaptureThreadHandler.removeCallbacks(sRunScanHome);
				StorageManager sm = (StorageManager) GlobalContext.get()
						.getSystemService(Context.STORAGE_SERVICE);

				// 获取sdcard的路径：外置和内置
				Method methodGetPaths = sm.getClass().getMethod(
						"getVolumePaths");
				String[] paths = (String[]) methodGetPaths.invoke(sm);

				// 清理之前扫描的目录
				sDVRHome.clear();

				for (int i = 0; i < paths.length; ++i) {
					File f = new File(paths[i]);
					File[] fs = f.listFiles(new FileFilter() {
						@Override
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});
					if (fs == null) {
						continue;
					}
					for (File d : fs) {
						if (isDVRHome(d)) {
							int n = 0;
							String insert = d.getAbsolutePath();
							String insertlower = insert
									.toLowerCase(Locale.SIMPLIFIED_CHINESE);
							int insertfb = insertlower.contains("front") ? 2
									: (insertlower.contains("back") ? 0 : 1);
							boolean insertTrust = sTrustRoot.contains(insert);
							for (; n < sDVRHome.size(); ++n) {
								String cur = sDVRHome.get(n).getAbsolutePath();
								String curlower = cur
										.toLowerCase(Locale.SIMPLIFIED_CHINESE);
								boolean curTrust = sTrustRoot.contains(cur);
								// 可信任目录优先靠前
								if (insertTrust && !curTrust) {
									break;
								}
								if (!insertTrust && curTrust) {
									continue;
								}
								// 前置目录优先靠前
								int curfb = curlower.contains("front") ? 2
										: (curlower.contains("back") ? 0 : 1);
								if (insertfb > curfb) {
									break;
								}
								if (insertfb < curfb) {
									continue;
								}
								// 最后按修改时间排
								if (d.lastModified() >= sDVRHome.get(n)
										.lastModified()) {
									break;
								}
							}
							sDVRHome.add(n, d);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			procTaskList();
		}
	};

	/**
	 * 扫描行车记录仪目录
	 */
	public void scanDVRHome(long delay) {
		sCaptureThreadHandler.removeCallbacks(sRunScanHome);
		sCaptureThreadHandler.postDelayed(sRunScanHome, delay);
	}

	/**
	 * 截图处理器
	 */
	public static interface CaptureProcessor {
		/**
		 * 处理位图列表
		 * 
		 * @param bmps
		 *            位图列表
		 */
		public void onCapture(CaptureResult[] res);
	}

	/**
	 * 抓拍成功
	 */
	public final static int ERROR_SUCCESS = 0;
	/**
	 * 没有处理
	 */
	public final static int ERROR_NOT_PROC = 1;
	/**
	 * 解码错误
	 */
	public final static int ERROR_DECODE = 2;
	/**
	 * 超时
	 */
	public final static int ERROR_TIMEOUT = 3;
	/**
	 * 没有找到
	 */
	public final static int ERROR_NOT_FOUND = 4;

	/**
	 * 抓拍结果
	 */
	public static class CaptureResult {
		public long time;
		public File picFile;
		public File src;
		public int err = ERROR_NOT_PROC;
	}

	/**
	 * 抓拍任务
	 */
	private static class CaptureTask {
		/**
		 * 需要截取的毫秒时间戳序列
		 */
		long[] timeMs;
		/**
		 * 捕获结果
		 */
		CaptureResult[] res;
		/**
		 * 从开机计算的超时时间点，0表示不超时
		 */
		long timeout;
		/**
		 * 回调任务
		 */
		CaptureProcessor processor;
	}

	/**
	 * 抓图任务列表，单线程执行，无需锁
	 */
	private static ArrayList<CaptureTask> sCaptureTaskList = new ArrayList<CaptureTask>();

	/**
	 * 最小1分钟
	 */
	private final static long MIN_VIDEO_DUR = 1;
	/**
	 * 最大10分钟
	 */
	private final static long MAX_VIDEO_DUR = 10;
	/**
	 * 最大误差5秒钟
	 */
	private final static long MAX_APPEND_DUR = 5;

	/**
	 * 视频信息缓存
	 */
	private static class VideoInfo {
		/**
		 * 起始时间戳
		 */
		long beginTime;
		/**
		 * 结束时间戳
		 */
		long endTime;
		/**
		 * 可信度分数，满分100
		 */
		int score = 0;
	}

	/**
	 * 视频文件信息缓存
	 */
	private static class VideoFileInfo {
		/**
		 * 视频信息
		 */
		VideoInfo info;
		/**
		 * 文件对象
		 */
		File file;
		/**
		 * 解码器对象缓存
		 */
//		MediaMetadataRetriever media;
	}

	/**
	 * 视频信息缓存
	 */
	private static HashMap<String, VideoInfo> sVideoInfoCacheMap = new HashMap<String, VideoInfo>();
	/**
	 * 无效视频
	 */
	private static HashSet<String> sInvalidVideoFileSet = new HashSet<String>();
	/**
	 * 高可信目录
	 */
	private static HashSet<String> sTrustRoot = new HashSet<String>();

	/**
	 * 清理视频信息缓存
	 */
	private void cleanVideoInfoCache() {
		Iterator<Entry<String, VideoInfo>> it = sVideoInfoCacheMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			if (isVideoFile(new File(it.next().getKey())) == false) {
				it.remove();
			}
		}
	}

	/**
	 * 获取视频文件信息
	 * 
	 * @param root
	 *            文件根目录
	 * @param f
	 *            文件对象
	 * @return 返回视频信息
	 */
	private static VideoInfo getVideoInfo(File root, File f) {
		VideoInfo info = sVideoInfoCacheMap.get(f.getAbsolutePath());
		if (info != null) {
			return info;
		}

		sCaptureThreadHandler.heartbeat();

		long mTime = f.lastModified();
		Long cTime = null;
		long len = 0;

		MediaMetadataRetriever m = new MediaMetadataRetriever();
		try {
			m.setDataSource(f.getAbsolutePath());
			len = Long
					.parseLong(m
							.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			try {
				String createDate = m
						.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE);
				if (!TextUtils.isEmpty(createDate)) {
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyyMMdd'T'HHmmss'.'SSS'Z'",
							Locale.SIMPLIFIED_CHINESE);
					Date d = df.parse(createDate);
					java.util.Calendar cal = java.util.Calendar.getInstance();
					int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
					cTime = d.getTime() + zoneOffset;
				}
			} catch (Exception e) {
				LogUtil.logw("parse date error: " + e.getLocalizedMessage());
			}
		} catch (Exception e) {
			LogUtil.logd("except Video file: " + f.getAbsolutePath());
			sInvalidVideoFileSet.add(f.getAbsolutePath());
			return null;
		} finally {
			m.release();
		}

		info = new VideoInfo();
		if (cTime == null) {
			// 没有创建时间，已最后修改时间作为录制结束时间
			info.endTime = f.lastModified();
			info.beginTime = info.endTime - len;
			info.score = 50;
		} else if (cTime - mTime >= -MAX_APPEND_DUR * 1000
				&& cTime - mTime <= MAX_APPEND_DUR * 1000) {
			// 创建时间为结束录制时间
			info.endTime = cTime;
			info.beginTime = info.endTime - len;
			info.score = 100 - ((int) Math.abs(cTime - mTime)) / 1000;
		} else if ((cTime + len) - mTime >= -MAX_APPEND_DUR * 1000
				&& (cTime + len) - mTime <= MAX_APPEND_DUR * 1000) {
			// 创建时间为开始录制时间
			info.beginTime = cTime;
			info.endTime = cTime + len;
			info.score = 100 - ((int) Math.abs((cTime + len) - mTime)) / 1000;
		} else {
			// 时间不可信，按创建时间为结束录制时间
			info.endTime = cTime;
			info.beginTime = info.endTime - len;
			info.score = 0;
		}

		// 行车记录仪一般是按分钟落地，误差小于MAX_APPEND_DUR秒，最长不超过MAX_Video_DUR分钟，最小不小于MIN_Video_DUR分钟
		len /= 1000;
		long min = Math.round(len / 60.0); // 持续分钟数，单位：分钟
		long dis = len - (min * 60); // 误差，单位：秒钟
		if (min >= MIN_VIDEO_DUR && min <= MAX_VIDEO_DUR // 视频长度限制
				&& dis >= -MAX_APPEND_DUR && dis <= MAX_APPEND_DUR // 误差长度限制
		) {
			info.score -= Math.abs(dis);
		} else {
			info.score = 0;
		}

		if (info.score < 80 && sTrustRoot.contains(root)) {
			info.score = 80;
		}

		sVideoInfoCacheMap.put(f.getAbsolutePath(), info);
		return info;
	}

	/**
	 * @param f
	 *            视频文件
	 * @return 是否有效
	 */
	private VideoInfo isValidVideoFile(File root, File f) {
		if (!isVideoFile(f)) {
			return null;
		}
		if (sInvalidVideoFileSet.contains(f.getAbsolutePath())) {
			return null;
		}

		VideoInfo info = getVideoInfo(root, f);
		if (info != null) {
			return info;
		}
		sInvalidVideoFileSet.add(f.getAbsolutePath());
		return null;
	}

	/**
	 * 从目录扫描视频文件，按最后修改时间反向排序，优先查找最近时间
	 * 
	 * @param d
	 *            目录
	 * @param fs
	 *            输出的文件信息列表
	 * @param minTime
	 *            最小时间
	 * @param taskTimeSet
	 *            任务时间集
	 * @param root
	 *            根目录
	 * @return 返回高可信视频数量
	 */
	private int scanVideoFiles(File d, ArrayList<VideoFileInfo> fs,
			long minTime, HashSet<Long> taskTimeSet, File root) {
		int ret = 0;
		File[] ds = d.listFiles();
		if (ds != null) {
			// 反向查找提升性能，修改时间越近的优先遍历，优先完成任务则退出
			Arrays.sort(ds, new Comparator<File>() {
				@Override
				public int compare(File lhs, File rhs) {
					// 优先前置摄像头录像获取
					String lpath = lhs.getAbsolutePath().toLowerCase(
							Locale.SIMPLIFIED_CHINESE);
					String rpath = rhs.getAbsolutePath().toLowerCase(
							Locale.SIMPLIFIED_CHINESE);
					int lfb = lpath.contains("front") ? 2 : (lpath
							.contains("back") ? 0 : 1);
					int rfb = rpath.contains("front") ? 2 : (rpath
							.contains("back") ? 0 : 1);
					if (lfb > rfb) {
						return -1;
					}
					if (lfb < rfb) {
						return 1;
					}
					if (lhs.lastModified() > rhs.lastModified()) {
						return -1;
					}
					if (lhs.lastModified() < rhs.lastModified()) {
						return 1;
					}
					return 0;
				}
			});
			// 遍历文件
			for (File fd : ds) {
				if (fd.isDirectory()) {
					// 目录继续递归
					ret += scanVideoFiles(fd, fs, minTime, taskTimeSet, root);
				} else if (fd.isFile()) {
					// 如果最后修改时间比需要找的文件还小，则不用在扫描结果内
					if (fd.lastModified() < minTime) {
						break;
					}
					// 文件有效性判断
					VideoInfo info = isValidVideoFile(root, fd);
					if (info != null) {
						VideoFileInfo file = new VideoFileInfo();
						file.file = fd;
						file.info = info;
						int i = 0;
						for (; i < fs.size(); ++i) {
							VideoFileInfo cur = fs.get(i);
							// 按最后修改时间反向排序，优先查找最近时间
							if (cur.info.beginTime < info.beginTime) {
								break;
							}
							if (cur.info.beginTime > info.beginTime) {
								continue;
							}
							// 优先查找高分的
							if (cur.info.score <= info.score) {
								break;
							}
						}
						fs.add(i, file);
						if (info.score >= 80) {
							Iterator<Long> it = taskTimeSet.iterator();
							while (it.hasNext()) {
								long time = it.next();
								// 找到了时间段内高度可信的视频文件
								if (time >= info.beginTime
										&& time <= info.endTime) {
									it.remove();
								}
							}
							++ret;
						}
					}
				}

				// 需要的任务时间文件已经全部找到
				if (taskTimeSet.isEmpty()) {
					break;
				}
			}
		}

		return ret;
	}

	/**
	 * 处理抓拍任务列表
	 */
	private void procTaskList() {
		sCaptureThreadHandler.removeCallbacks(mRunnableRelease);
		// 远程调用是否可用
		if (!DVRProxy.getInstance().enableInvoke()) {
			DVRProxy.getInstance().bindService();
			return;
		}
		// 扫描目录
		if (sDVRHome.isEmpty()) {
			// TODO 存在循环调用的风险，本地没有视频文件
			scanDVRHome(0);
			return;
		}

		// 清理缓存
		cleanVideoInfoCache();

		// 任务按索引信息
		class TaskInfo {
			public TaskInfo(CaptureTask task, int index) {
				this.task = task;
				this.index = index;
			}

			CaptureTask task;
			int index;
		}

		// 查找的文件要支持的最小时间
		long minTime = 0;
		long curTime = SystemClock.elapsedRealtime();
		TreeMap<Long, ArrayList<TaskInfo>> tasks = new TreeMap<Long, ArrayList<TaskInfo>>();
		HashSet<Long> taskTimeSet = new HashSet<Long>();
		// 遍历任务
		for (int i = 0; i < sCaptureTaskList.size(); ++i) {
			CaptureTask task = sCaptureTaskList.get(i);
			// 超时任务
			if (task.timeout > 0 && curTime >= task.timeout) {
				for (int j = 0; j < task.timeMs.length; ++j) {
					if (minTime == 0 || task.timeMs[j] < minTime) {
						minTime = task.timeMs[j];
					}
					if (task.res[j].err == ERROR_NOT_PROC) {
						task.res[j].err = ERROR_TIMEOUT;
					}
				}
				continue;
			}
			// 按抓拍时间排序
			for (int j = 0; j < task.timeMs.length; ++j) {
				long time = task.timeMs[j];
				if (minTime == 0 || time < minTime) {
					minTime = time;
				}
				ArrayList<TaskInfo> lst = tasks.get(time);
				if (null == null) {
					lst = new ArrayList<TaskInfo>();
					tasks.put(time, lst);
					taskTimeSet.add(time);
				}
				lst.add(new TaskInfo(task, j));
			}
		}

		// 扫描所有视频文件，并过滤视频文件，按最后修改时间反向排序，优先查找最近时间
		ArrayList<VideoFileInfo> fs = new ArrayList<VideoFileInfo>();
		for (File d : sDVRHome) {
			scanVideoFiles(d, fs, minTime, taskTimeSet, d);

			int n = 0;
			for (Entry<String, VideoInfo> entry : sVideoInfoCacheMap.entrySet()) {
				if (entry.getValue().score >= 80
						&& entry.getKey().startsWith(d.getAbsolutePath())) {
					++n;
				}
			}
			// 返回的高可信视频大于3时，则认为目录是个可信目录
			if (n > 3) {
				if (sTrustRoot.add(d.getAbsolutePath())) {
					LogUtil.logd("add trust dvr home: " + d.getAbsolutePath());
				}
			}
		}

		// TODO 去掉时间区间交叉的有问题的视频
		// TODO 去掉分辨率可能有问题的视频

		// 最大的时间戳
		long maxTime = System.currentTimeMillis()
				- (MAX_VIDEO_DUR * 60 + MAX_APPEND_DUR) * 1000;

		if (fs.size() > 0) {
			long lastTime = fs.get(0).info.endTime;
			if (lastTime > maxTime) {
				maxTime = lastTime;
			}
		}

		// 遍历抓取
		Iterator<Entry<Long, ArrayList<TaskInfo>>> it = tasks.descendingMap()
				.entrySet().iterator();
		int fsi = 0;
		ArrayList<TaskInfo> lst = null;
		long time = 0;
		VideoFileInfo f = null;
		while (true) {
			sCaptureThreadHandler.heartbeat();

			if (lst == null) {
				if (it.hasNext()) {
					Entry<Long, ArrayList<TaskInfo>> entry = it.next();
					time = entry.getKey();
					lst = entry.getValue();
				} else {
					// 任务全部遍历完了
					break;
				}
			}
			if (f == null) {
				if (fsi >= fs.size()) {
					// 文件全部遍历完了
					break;
				}
				f = fs.get(fsi);
				++fsi;
			}

			long begin = f.info.beginTime;
			long end = f.info.endTime;
			if (time < begin) {
				// 下一个视频文件
				f = null;
				continue;
			}

			boolean isGetFrame = false;
			if (time >= begin && time < end) {
				String upload_path = null;
				try {
					upload_path = getOutFilePath(f.file,(time - begin));
					isGetFrame = getFrameAtTime(f.file.getAbsolutePath(),upload_path,(time - begin));
				} finally {
					File outFile = new File(upload_path);
					if (!outFile.exists()) {
						isGetFrame = false;
					}
					for (TaskInfo info : lst) {
						CaptureResult res = info.task.res[info.index];
						res.picFile = isGetFrame ? outFile : null;
						res.err = (isGetFrame == false ? ERROR_DECODE : ERROR_SUCCESS);
						res.src = f.file;
					}
				}
			}
			// 下一个截取时间点
			lst = null;
		}

		curTime = SystemClock.elapsedRealtime();
		long nextTimeout = curTime + TIMEOUT_SCAN_DVR_HOME_WITH_TASK;
		// 预测下一个视频文件的落地时间进行扫描
		if (fs.size() > 0) {
			VideoInfo info = fs.get(0).info;
			long len = info.endTime - info.beginTime;
			long tryTime;
			if (curTime - info.endTime < len) {
				tryTime = info.endTime + len + 200;
			} else {
				tryTime = curTime + len;
			}
			if (tryTime < nextTimeout) {
				nextTimeout = tryTime;
			}
		}

		// 遍历任务处理结果
		for (int i = 0; i < sCaptureTaskList.size();) {
			final CaptureTask task = sCaptureTaskList.get(i);
			boolean end = true;
			for (int j = 0; j < task.timeMs.length; ++j) {
				time = task.timeMs[j];
				if (time <= maxTime && task.res[j].err == ERROR_NOT_PROC) {
					// 改这里可以在没有找到时是否要尝试重新扫描
					task.res[j].err = ERROR_NOT_FOUND;
				}
				if (task.res[j].err == ERROR_NOT_PROC) {
					end = false;
					break;
				}
			}
			if (end) {
				// 回调处理结果并清理任务
				sCaptureThreadHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						task.processor.onCapture(task.res);
					}
				}, 0);
				sCaptureTaskList.remove(i);
				continue;
			}
			if (task.timeout > 0 && task.timeout < nextTimeout) {
				nextTimeout = task.timeout;
			}
			++i;
		}

		// 判断下一次扫描DVR目录的时机
		if (sCaptureTaskList.isEmpty()) {
			scanDVRHome(TIMEOUT_SCAN_DVR_HOME_NO_TASK);
		} else if (nextTimeout > curTime) {
			scanDVRHome(nextTimeout - curTime);
		} else {
			scanDVRHome(0);
		}
		sCaptureThreadHandler.postDelayed(mRunnableRelease, 5 * 60 * 1000);
	}

/*	static {
		File file = new File("/data/data/com.txznet.txz/data/ffmpeg");
		file.setExecutable(true);
		file.setWritable(true,true);
		file.setReadable(true,true);
	}*/

	private static String getOutFilePath(File file, long time){
		String dirPath = Environment
				.getExternalStorageDirectory()
				.getPath()
				+ "/txz/cache/other/";
		File dir = new File(dirPath);
		if (!dir.exists()){
			dir.mkdirs();
		}
		return dirPath + UUID.randomUUID() + ".tmp";
	}

	private static boolean getFrameAtTime(String inFile , String outFile,  long time){
		return DVRProxy.getInstance().getFrameAtTime(inFile, outFile, time);
/*		boolean result = false;
		try {
			Process process = Runtime.getRuntime().exec("/data/data/com.txznet.txz/data/ffmpeg -ss " + ((double)time/1000) + " -i " + inFile + " -y -f image2 -vframes 1 " + outFile);
			process.waitFor();//等待执行完毕
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;*/
	}

	/**
	 * 默认扫描目录间隔，1小时扫描一次
	 */
	private final static long TIMEOUT_SCAN_DVR_HOME_NO_TASK = 1 * 60 * 60 * 1000;
	/**
	 * 有任务时的默认扫描间隔，20s扫描一次
	 */
	private final static long TIMEOUT_SCAN_DVR_HOME_WITH_TASK = 20 * 1000;

	/**
	 * 获取某时间点的图片数据
	 * 
	 * @param timeMs
	 *            毫秒值时间点
	 * @param processor
	 *            回调处理器
	 * @param timeout
	 *            超时时长，单位ms
	 */
	public void getPictureAtTime(long[] timeMs,
			CaptureProcessor processor, long timeout) {
		final CaptureTask task = new CaptureTask();
		task.timeMs = timeMs;
		task.processor = processor;
		if (timeout > 0) {
			task.timeout = SystemClock.elapsedRealtime() + timeout;
		}
		task.res = new CaptureResult[timeMs.length];
		for (int i = 0; i < timeMs.length; ++i) {
			(task.res[i] = new CaptureResult()).time = timeMs[i];
		}
		sCaptureThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				sCaptureTaskList.add(task);
				procTaskList();
			}
		}, 0);
	}
	
	public void procTasks() {
		sCaptureThreadHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				procTaskList();
			}
		}, 0);
	}
	
	private static Runnable mRunnableRelease = new Runnable() {
		
		@Override
		public void run() {
			DVRProxy.release();
		}
	};
}
