package com.txznet.txz.module.download;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.text.TextUtils;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZDownloadManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable2;

public class DownloadManager extends IModule {
	private static DownloadManager sModuleInstance = new DownloadManager();
	private final static String MONITOR_MODULE = "download";
	private final static String EVENT_ENTER = "enter";
	private final static String EVENT_RESP_ENTER = "resp_enter";
	private final static String EVENT_END = "end";
	private final static String RETCODE_SUCCESS = "success";
	private final static String RETCODE_FAIL_IO = "fail_io";
	private final static String RETCODE_FAIL_HTTP = "fail_http";
	private final static String RETCODE_FAIL_REQUEST = "fail_request";
	

	private DownloadManager() {
		AppLogic.runOnSlowGround(new Runnable() {
			@Override
			public void run() {
				freeCrashFiles();
				AppLogic.runOnSlowGround(this, 5 * 60 * 1000);
			}
		}, 0);
	}

	public static DownloadManager getInstance() {
		return sModuleInstance;
	}

	public static final File LOG_FILE_ROOT = new File(
			Environment.getExternalStorageDirectory(), "txz/log"); // 日志根目录
	public static final long CACHE_CLEAR_TIME = (long) 30 * 24 * 60 * 60 * 1000;
	public static final File CACHE_FILE_ROOT = new File(
			Environment.getExternalStorageDirectory(), "txz/cache"); // 缓存根目录
	public static final File DOWNLOAD_FILE_ROOT = new File(CACHE_FILE_ROOT,
			"download"); // 下载根目录
	public static final File RESERVE_BUFFER_FILE = new File(DOWNLOAD_FILE_ROOT,
			".reserve.dat"); // 卸载预留目录
	public static final int RESERVE_BUFFER_SIZE = 100 * 1024 * 1024; // 预留空间大小
	public static final int RESERVE_DISK_SIZE = 1 * 1024 * 1024; // 为分区保留空间

	public static final int TIMEOUT_CONNECT = 10 * 1000; //连接超时10s
	public static final int TIMEOUT_READ_DATA = 10 * 1000; //读取超时10s
	public static final int MAX_RETRY_TIMES = 3; // 最大重试次数
	public static final int CHECK_CACHE_SIZE = 128; // 校验缓存长度

	public static final int READ_BUFFER_SIZE = 1 * 1024 * 1024; // 下载缓冲区大小
	private byte[] mBuffer = null;
	HttpRequestBase mHttpReq;
	HttpClient mHttpClient;
	private HandlerThread mDownloadHttpFileTaskThread = null;
	private TXZHandler mDownloadHttpFileTaskHandler = null;

    private HandlerThread mDownloadProgressDispatchThread;
    private Handler mDownloadProgressDispatchHandler; // listener处理

	public int mRetryRateTime = DEF_RETRY_RATE_TIME; // 默认重试时间
	public static final int DEF_RETRY_RATE_TIME = 3 * 1000; // 默认重试时间
	public static final int MAX_RETRY_RATE_TIME = 30 * 1000; // 最大重试时间

	private void createHandler() {
		if (mDownloadHttpFileTaskThread == null) {
			mBuffer = new byte[READ_BUFFER_SIZE];
			mDownloadHttpFileTaskThread = new HandlerThread("DownloadThread");
			mDownloadHttpFileTaskThread.start();
			mDownloadHttpFileTaskHandler = new TXZHandler(
					mDownloadHttpFileTaskThread.getLooper());
		}
	}

	private void releaseHandler() {
		if (mDownloadHttpFileTaskThread != null) {
			mBuffer = null;
			mDownloadHttpFileTaskThread.quit();
			mDownloadHttpFileTaskThread = null;
			mDownloadHttpFileTaskHandler = null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_INNER_NET,
				UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ);
		regEvent(UiEvent.EVENT_INNER_NET,
				UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_STOP);
		regEvent(UiEvent.EVENT_INNER_NET,
				UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		reserveDwonloadSpace();
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_INNER_NET: {
			switch (subEventId) {
			case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ: {
				try {
					UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask
							.parseFrom(data);
					addTask(task);
				} catch (Exception e) {
				}
				break;
			}
			case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_STOP: {
				try {
					UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask
							.parseFrom(data);
					removeTask(task);
				} catch (Exception e) {
				}
				break;
			}
			case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP: {
				try {
					UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask
							.parseFrom(data);
					synchronized (mSDKTaskIds) {
						if (task.strTaskId.startsWith(TXZDownloadManager.DOWNLOAD_TASK_ID_PREFIX)) {
							if (mSDKTaskIds.remove(task.strTaskId)) {
								String pkg = task.strDefineParam;
								int errCode = TXZDownloadManager.EC_DOWNLOAD_UNKNOWN;
								switch (task.int32ResultCode) {
									case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS:
										errCode = TXZDownloadManager.EC_DOWNLOAD_SUCCESS;
										break;
									case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP:
										errCode = TXZDownloadManager.EC_DOWNLOAD_SERVER;
										break;
									case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO:
										errCode = TXZDownloadManager.EC_DOWNLOAD_IO;
										break;
									case UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST:
										errCode = TXZDownloadManager.EC_DOWNLOAD_NETWORK;
										break;
								}
								Parcel p = Parcel.obtain();
								p.writeInt(errCode);
								p.writeString(task.strTaskId);
								p.writeString(task.strFile);
								ServiceManager.getInstance().sendInvoke(pkg, TXZDownloadManager.DOWNLOAD_CMD_PREFIX + TXZDownloadManager.DOWNLOAD_RESULT, p.marshall(), null);
								p.recycle();
							} else {
								LogUtil.logd("can not find taskId: " + task.strTaskId);
							}
						}
					}
				} catch (Exception e) {
				}
				break;
			}
			default:
				break;
			}
		}
		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	private final ArrayList<String> mSDKTaskIds = new ArrayList<String>();

	public byte[] invokeCommDownload(final String packageName, String command, final byte[] data){
		String cmd = command.substring(TXZDownloadManager.DOWNLOAD_INVOKE_PREFIX.length());
		Parcel p = Parcel.obtain();
		if (null != data) {
			p.unmarshall(data, 0, data.length);
			p.setDataPosition(0);

			if (TextUtils.equals(cmd,TXZDownloadManager.DOWNLOAD_START)) {
				UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
				task.strTaskId = p.readString();
				task.strUrl = p.readString();
				task.strDefineParam = packageName;
				boolean needProgress = p.readByte() == 1;
				if (needProgress) {
					registerDownloadTaskStatusChangeListener(task.strTaskId, new DownloadTaskProgressChangeListener() {
						@Override
						public void onProgressChange(UiInnerNet.DownloadHttpFileTask task) {
							Parcel p = Parcel.obtain();
							p.writeString(task.strTaskId);
							p.writeInt(task.uint32DlProgress);
							ServiceManager.getInstance().sendInvoke(packageName, TXZDownloadManager.DOWNLOAD_CMD_PREFIX + TXZDownloadManager.DOWNLOAD_NOTIFY, p.marshall(), null);
							p.recycle();
						}
					});
				} else {
					unregisterDownloadTaskStatusChangeListener(task.strTaskId);
				}
				synchronized (mSDKTaskIds) {
					mSDKTaskIds.add(task.strTaskId);
				}
				addTask(task);
			} else if (TextUtils.equals(cmd, TXZDownloadManager.DOWNLOAD_STOP)) {
				UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
				task.strTaskId = p.readString();
				unregisterDownloadTaskStatusChangeListener(task.strTaskId);
				synchronized (mSDKTaskIds) {
					mSDKTaskIds.remove(task.strTaskId);
				}
				removeTask(task);
			}
		}

		p.recycle();
		return null;
	}




	private List<UiInnerNet.DownloadHttpFileTask> mTaskList = new ArrayList<UiInnerNet.DownloadHttpFileTask>();
	private UiInnerNet.DownloadHttpFileTask mCurrentTask = null;
	private int mRetryCount = 0;

	public void addTask(UiInnerNet.DownloadHttpFileTask task) {
		JNIHelper.logd("add download http file task: " + task.strUrl);

		synchronized (mTaskList) {
			mTaskList.add(task);
		}

		processTaskList();
	}

	public void removeTask(UiInnerNet.DownloadHttpFileTask task) {
		removeTask(task.strTaskId);
	}

	public void removeTask(String id) {
		JNIHelper.logd("remove download http file task: " + id);

		synchronized (mTaskList) {
			for (int i = 0; i < mTaskList.size();) {
				if (mTaskList.get(i).strTaskId.equals(id)) {
					JNIHelper.logd("remove download http file task: "
							+ mTaskList.get(i).strUrl);
					mTaskList.remove(i);
				} else {
					++i;
				}
			}
			if (mCurrentTask.strTaskId.equals(id)) {
				JNIHelper.logd("remove current download http file task: "
						+ mCurrentTask.strUrl);
				mCurrentTask.int32ResultCode = UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_STOPED;
				abortCurrentHttpRequest();
			}
		}

		processTaskList();
	}

	public void processTaskList() {
		reserveDwonloadSpace();

		synchronized (mTaskList) {
			if (mCurrentTask == null) {

				if (mTaskList.isEmpty()) {
					JNIHelper.logd("all download task completed");
					releaseHandler();
					return;
				}

				mRetryCount = 0;

				mRetryRateTime = DEF_RETRY_RATE_TIME;

				createHandler();

				mCurrentTask = mTaskList.remove(0);
				mCurrentTask.int32ResultCode = UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS;
				
				monitor(mCurrentTask.strTaskId, EVENT_ENTER, 0);
				
				JNIHelper.logd("begin download http file task: "
						+ mCurrentTask.strTaskId + "-" + mCurrentTask.strUrl);
				
				mDownloadHttpFileTaskHandler
						.removeCallbacks(mRunnableProcessCurrentTask);
				mDownloadHttpFileTaskHandler.post(mRunnableProcessCurrentTask);
			}
		}
	}

	private void closeCloseable(Closeable obj) {
		try {
			if (obj != null)
				obj.close();
		} catch (Exception e) {
		}
	}

	private void abortCurrentHttpRequest() {
		try {
			if (mHttpReq != null) {
				mHttpReq.abort();
				mHttpReq = null;
			}
			mHttpClient = null;
		} catch (Exception e) {
		}
	}

	private void sendCurrentTaskSuccessResponse() {
		synchronized (mTaskList) {
			if (mCurrentTask == null)
				return;
			File fFinal = new File(DOWNLOAD_FILE_ROOT, mCurrentTask.strTaskId);
			mCurrentTask.strFile = fFinal.getPath();
			mCurrentTask.uint32Size = (int) fFinal.length();
		}

		sendCurrentTaskResponse(
				UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS, 0);
	}

	private void sendCurrentTaskResponse(int resultCode, int statusCode) {
		synchronized (mTaskList) {
			if (mCurrentTask == null)
				return;

			if (mCurrentTask.int32ResultCode != UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_STOPED) {
				mCurrentTask.int32ResultCode = resultCode;
				mCurrentTask.int32StatusCode = statusCode;
			}

			JNIHelper.logd("complete http task ["
					+ mCurrentTask.int32ResultCode + "/"
					+ mCurrentTask.int32StatusCode + "]: "
					+ mCurrentTask.strUrl);
			
			monitor(mCurrentTask.strTaskId, EVENT_END, resultCode);
			
			JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET,
					UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP, mCurrentTask);
			
			mCurrentTask = null;
			
		}

		processTaskList();
	}

	/**
	 * @param resultCode  错误码
	 * @param statusCode
	 * @param retryCount 重试次数
	 */
	private void retryCurrentTask(int resultCode, int statusCode, final int retryCount) {
		boolean stoped = false;
		synchronized (mTaskList) {
			if (mCurrentTask.int32ResultCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_STOPED) {
				stoped = true;
			}
		}
		if (!stoped && (retryCount == -1 || ++mRetryCount < retryCount)) {
			mDownloadHttpFileTaskHandler.postDelayed(
					mRunnableProcessCurrentTask, mRetryRateTime);
			mRetryRateTime *= 2;
			if (mRetryRateTime > MAX_RETRY_RATE_TIME) {
				mRetryRateTime = MAX_RETRY_RATE_TIME;
			}
		} else {
			sendCurrentTaskResponse(resultCode, statusCode);
		}
	}

	/**
	 * 返回0成功，-1需要清理缓存，1需要重试，2网络错误，需要重试，3read出错，需要重试
	 */
	private int readStreamData(UiInnerNet.DownloadHttpFileTask task,
			InputStream data, RandomAccessFile cache, CRC32 crc32,
			long downloadSize, long totalSize, byte[] checks) {
		try {
			// 最后的字节校验
			if (checks != null) {
				int t = 0;
				while (t < checks.length) {
					int r = data.read(mBuffer, t, checks.length - t);
					if (r < 0) {
						JNIHelper.loge("read stream data error");
						return 1;
					}
					t += r;
				}
				while (t > 0) {
					--t;
					if (checks[t] != mBuffer[t]) {
						JNIHelper.loge("check buffer error at ["
								+ (downloadSize + t) + "]");
						return -1;
					}
				}
			}

			//启动下载前先更新下进度
			task.uint32DlProgress = (int) ((downloadSize * 1f / totalSize) * 100);
			dispatchTaskProgressChange(task);

			// 开始正式数据下载
			int statistic = 0;
			while (downloadSize < totalSize) {
				// TODO 考虑一下限流
				mDownloadHttpFileTaskHandler.heartbeat();
				int r = data.read(mBuffer);
				if (r < 0) {
					JNIHelper.loge("read stream data error");
					return 3;
				} else {
					mRetryRateTime = DEF_RETRY_RATE_TIME;
				}
				statistic += r;
				if (statistic > 1 * 1024 * 1024) {
					JNIHelper.logd("recive data " + statistic + "+"
							+ downloadSize + "/" + totalSize + " for task: "
							+ task.strUrl);
					statistic = 0;
					dispatchTaskProgressChange(task);
				}
				if (r > 0) {
					cache.seek(downloadSize);
					cache.write(mBuffer, 0, r);
					crc32.update(mBuffer, 0, r);
					cache.seek(totalSize);
					downloadSize += r;
					if (downloadSize > totalSize) {
						return -1;
					}
					cache.writeLong(crc32.getValue());
					cache.writeLong(downloadSize);
					cache.writeLong(totalSize);
					task.uint32DlProgress = (int) ((downloadSize * 1f / totalSize) * 100);
				}
			}
		} catch (Exception e) {
			JNIHelper.loge("read stream data exception: " + e.getMessage());
			if (e instanceof SocketException
					|| e instanceof SocketTimeoutException) {
				return 2; // 区分网络类型错误
			}
			return 1;
		}

		return 0;
	}

	private void downloadFileAgain(UiInnerNet.DownloadHttpFileTask task,
			File fCache) {
		JNIHelper.logd("download file again: " + task.strUrl);

		// fCache.delete(); //不用删除，节省大文件的文件簇分配时间

		mHttpClient = new DefaultHttpClient();
		mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_CONNECT);
		mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_READ_DATA);
		mHttpReq = new HttpGet(task.strUrl);
		mHttpReq.addHeader("Connection","keep-alive");

		// TODO 如果不支持断点续传，出错需要删除缓存文件

		try {
			HttpResponse res = mHttpClient.execute(mHttpReq);
			int resCode = res.getStatusLine().getStatusCode();
			if (resCode != 200) {
				JNIHelper.logw("down response " + resCode + " task: "
						+ task.strUrl);
				sendCurrentTaskResponse(
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP,
						resCode);
				return;
			}
			
			monitor(task.strTaskId, EVENT_RESP_ENTER, 0);
			
			HttpEntity entity = res.getEntity();
			long len = entity.getContentLength();
			{
				// 预占缓存空间
				reserveDwonloadSpace();
				// 考虑源文件已经够大小则也不需要重新分配
				long free = DOWNLOAD_FILE_ROOT.getFreeSpace() + fCache.length();
				if (fCache.length() < len && free < len + RESERVE_DISK_SIZE) {
					//是否禁止使用预留的磁盘空间
					boolean bForbid = false;
					if (task.bForbidUseReservedSpace != null) {
						try {
							bForbid = task.bForbidUseReservedSpace;
						} catch (Exception e) {
						}
					}
					//TODO  不使用100M占位空间判断
					if (bForbid || len > RESERVE_BUFFER_FILE.length() + (free- RESERVE_DISK_SIZE > 0 ? free - RESERVE_DISK_SIZE:0)) {
						// 空间不足
						JNIHelper.loge("not enough free space " + free
								+ " for " + len + ": " + task.strUrl);
						sendCurrentTaskResponse(
								UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
								0);
						return;
					}
					synchronized (RESERVE_BUFFER_FILE) {
						RESERVE_BUFFER_FILE.renameTo(fCache);
					}
				}
				RandomAccessFile outCache = null;
				try {
					outCache = new RandomAccessFile(fCache, "rw");
					outCache.setLength(len + 24);
					outCache.seek(len);
					outCache.writeLong(0);
					outCache.writeLong(0);
					outCache.writeLong(len);
				} catch (Exception e) {
					closeCloseable(outCache);
					outCache = null;
					fCache.delete();
					JNIHelper.loge("download file again file exception: "
							+ e.getMessage());
					sendCurrentTaskResponse(
							UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
							0);
					return;
				} finally {
					closeCloseable(outCache);
					reserveDwonloadSpace();
				}
			}
			InputStream inStream = entity.getContent();
			long downloadSize = 0;
			CRC32 crc32 = new CRC32();
			RandomAccessFile outCache = null;
			int ret = 0;
			try {
				outCache = new RandomAccessFile(fCache, "rw");
				ret = readStreamData(task, inStream, outCache, crc32,
						downloadSize, len, null);
				if (ret == 0) {
					outCache.setLength(len);
				} else {
					abortCurrentHttpRequest();
				}
			} catch (Exception e) {
				JNIHelper.loge("download file again stream exception: "
						+ e.getMessage());
				retryCurrentTask(
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
						0, MAX_RETRY_TIMES);
				return;
			} finally {
				closeCloseable(outCache);
				closeCloseable(inStream);
			}
			switch (ret) {
			case 0:
				// 下载成功
				fCache.renameTo(new File(DOWNLOAD_FILE_ROOT, task.strTaskId));
				sendCurrentTaskSuccessResponse();
				break;
			case 1:
			case 2: // 网络类型错误
				// 重试当前下载任务
				retryCurrentTask(
						ret == 1 ?
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO : UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
						0, MAX_RETRY_TIMES);
				break;
			case 3: //数据读取失败
				retryCurrentTask(UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP , 0 ,-1);
				break;
			case -1:
				retryCurrentTask(
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
						0, MAX_RETRY_TIMES);
				break;
			}
		} catch (Exception e) {
			if (e instanceof UnknownHostException) {
				//断开网络时报这个错
			}
			JNIHelper.loge("download fail request: " + e.getMessage());
			retryCurrentTask(
					UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
					0, MAX_RETRY_TIMES);
			return;
		} finally {
			abortCurrentHttpRequest();
		}
	}

	private boolean checkDownloadedFile(UiInnerNet.DownloadHttpFileTask task) {
		// 文件已全部下载完成，验证下文件的长度和最后CHECK_CACHE_SIZE个字节
		File fFinal = new File(DOWNLOAD_FILE_ROOT, task.strTaskId);
		if (fFinal.exists()) {
			long checkLength = CHECK_CACHE_SIZE > fFinal.length() ? fFinal
					.length() : CHECK_CACHE_SIZE;
			mHttpClient = new DefaultHttpClient();
			mHttpReq = new HttpGet(task.strUrl);
			mHttpReq.addHeader("Range", "bytes="
					+ (fFinal.length() - checkLength) + "-"
					+ (fFinal.length() - 1));
			try {
				HttpResponse res = mHttpClient.execute(mHttpReq);
				Header header = res.getFirstHeader("Content-Range");
				String contentRange = header.getValue();
				long retTotal = Long.parseLong(contentRange.split("/")[1]);
				if (retTotal != fFinal.length()) {
					JNIHelper.logw("download file not match length[" + retTotal
							+ "/" + fFinal.length() + "]");
					// 删除原来的文件，重新下载
					fFinal.delete();
					mDownloadHttpFileTaskHandler.postDelayed(
							mRunnableProcessCurrentTask, 0);
					return true;
				} else {
					// 校验内容
					HttpEntity entity = res.getEntity();
					long len = entity.getContentLength();
					if (len != checkLength) {
						retryCurrentTask(
								UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
								0, MAX_RETRY_TIMES);
						return true;
					}
					// 读取返回的字节
					byte[] retBytes = new byte[(int) checkLength];
					InputStream retStream = entity.getContent();
					try {
						int t = 0;
						while (t < retBytes.length) {
							int r = retStream.read(retBytes, t, retBytes.length
									- t);
							t += r;
						}
					} finally {
						closeCloseable(retStream);
					}
					byte[] readBytes = new byte[(int) checkLength];
					FileInputStream inFile = new FileInputStream(fFinal);
					try {
						inFile.skip(fFinal.length() - checkLength);
						int t = 0;
						while (t < readBytes.length) {
							int r = inFile.read(readBytes, t, readBytes.length
									- t);
							t += r;
						}
						for (int i = 0; i < checkLength; ++i) {
							if (retBytes[i] != readBytes[i]) {
								JNIHelper
										.loge("check file data failed at last ["
												+ i
												+ "/"
												+ checkLength
												+ "] byte");
								// 删除原来的文件，重新下载
								fFinal.delete();
								mDownloadHttpFileTaskHandler.postDelayed(
										mRunnableProcessCurrentTask, 0);
							}
						}
					} finally {
						closeCloseable(inFile);
					}
				}
				JNIHelper.logd("http file already download complete: "
						+ task.strUrl);
				// 下载成功
				sendCurrentTaskSuccessResponse();
				return true;
			} catch (Exception e) {
				JNIHelper.loge("download fail request: " + e.getMessage());
				retryCurrentTask(
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
						0, MAX_RETRY_TIMES);
				return true;
			} finally {
				abortCurrentHttpRequest();
			}
			// return true;
		}

		return false;
	}

	private Runnable mRunnableProcessCurrentTask = new Runnable() {
		@Override
		public void run() {
			UiInnerNet.DownloadHttpFileTask task;
			synchronized (mTaskList) {
				if (mCurrentTask == null)
					return;
				task = mCurrentTask;
			}
			JNIHelper.logd("retry[" + mRetryCount + "] task: " + task.strUrl);

			abortCurrentHttpRequest();

			// 校验已下载的全量文件
			if (checkDownloadedFile(task)) {
				return;
			}

			/**
			 * 下载任务结构，DATA+CRC32(long)+DWONLOAD(long)+TOTAL(long)
			 */
			CRC32 crc32 = new CRC32();
			long downloadSize = 0;
			long totalSize = 0;
			long curCrc32 = 0;
			byte[] checkBytes = new byte[CHECK_CACHE_SIZE];

			File fCache = new File(DOWNLOAD_FILE_ROOT, mCurrentTask.strTaskId
					+ ".tmp");
			if (fCache.exists()) {
				// 尺寸不够
				if (fCache.length() < CHECK_CACHE_SIZE + 24 /*
															 * CRC32(long)+DWONLOAD
															 * (long)+TOTAL(long
															 * )
															 */) {
					JNIHelper.loge("cache file is too small");
					downloadFileAgain(task, fCache);
					return;
				}
				// 获取长度和校验
				RandomAccessFile inFile = null;
				boolean checked = true;
				try {
					inFile = new RandomAccessFile(fCache, "rw");
					inFile.seek(fCache.length() - 24);
					curCrc32 = inFile.readLong();
					downloadSize = inFile.readLong();
					totalSize = inFile.readLong();
					if (totalSize + 24 != fCache.length()
							|| downloadSize >= totalSize) {
						// 长度校验不通过
						JNIHelper.loge("cache file length not match: length="
								+ inFile.length() + ", total=" + totalSize
								+ ", download=" + downloadSize);
						checked = false;
					} else if (downloadSize >= CHECK_CACHE_SIZE) {
						// crc32校验
						inFile.seek(0);
						long total = 0;
						while (total < downloadSize) {
							int r = (int) ((downloadSize - total) < mBuffer.length ? (downloadSize - total)
									: mBuffer.length);
							r = inFile.read(mBuffer, 0, r);
							if (r < 0) {
								// 读取异常
								JNIHelper.loge("read file error: " + total
										+ "/" + downloadSize);
								checked = false;
								break;
							}
							total += r;
							crc32.update(mBuffer, 0, r);
						}
						if (checked && crc32.getValue() != curCrc32) {
							JNIHelper
									.loge("cache file check not match: sumBytes="
											+ crc32.getValue()
											+ ", sumRead="
											+ curCrc32);
							checked = false;
						}

						if (checked) {
							// 读取最后的几个校验字节
							inFile.seek(downloadSize - checkBytes.length);
							int t = 0;
							while (t < checkBytes.length) {
								int r = inFile.read(checkBytes, t,
										checkBytes.length - t);
								if (r < 0) {
									// 读取异常
									JNIHelper.loge("read file error: " + total
											+ "/" + downloadSize);
									checked = false;
									break;
								}
								t += r;
							}
						}
					}
				} catch (Exception e) {
					JNIHelper.loge("read cache file expcetion: " + e.getMessage());
					checked = false;
				} finally {
					closeCloseable(inFile);
				}
				if (checked == false) {
					downloadFileAgain(task, fCache);
					return;
				}
			}

			// 全部重下
			if (downloadSize < CHECK_CACHE_SIZE) {
				JNIHelper.logw("abandon download cache size=" + downloadSize);
				downloadFileAgain(task, fCache);
				return;
			}

			mHttpClient = new DefaultHttpClient();
			mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_CONNECT);
			mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_READ_DATA);
			mHttpReq = new HttpGet(task.strUrl);
			mHttpReq.addHeader("Connection","keep-alive");
			if (downloadSize > 0) {
				JNIHelper.logd("download from: " + downloadSize
						+ ", CHECK_CACHE_SIZE=" + CHECK_CACHE_SIZE);
				mHttpReq.addHeader("Range", "bytes="
						+ (downloadSize - CHECK_CACHE_SIZE) + "-");
			}
			try {
				HttpResponse res = mHttpClient.execute(mHttpReq);
				int resCode = res.getStatusLine().getStatusCode();
				if (resCode != 206) {
					// 碎片获取失败
					downloadFileAgain(task, fCache);
					return;
				}

				String contentRange = res.getFirstHeader("Content-Range")
						.getValue();
				long retTotal = Long.parseLong(contentRange.split("/")[1]);
				if (retTotal != totalSize) {
					// 校验大小失败
					downloadFileAgain(task, fCache);
					return;
				}
				HttpEntity httpEntity = res.getEntity();
				if (httpEntity.getContentLength() != totalSize - downloadSize
						+ CHECK_CACHE_SIZE) {
					JNIHelper.logw("Content-Length error: "
							+ httpEntity.getContentLength() + "!=" + totalSize
							+ "-" + downloadSize + "+" + CHECK_CACHE_SIZE);
					retryCurrentTask(
							UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
							0, MAX_RETRY_TIMES);
					return;
				}
				RandomAccessFile inFile = null;
				int ret = 0;
				try {
					inFile = new RandomAccessFile(fCache, "rw");
					InputStream inStream = null;
					try {
						inStream = httpEntity.getContent();
						ret = readStreamData(task, inStream, inFile, crc32,
								downloadSize, totalSize, checkBytes);
						if (ret == 0) {
							inFile.setLength(totalSize);
						} else {
							abortCurrentHttpRequest();
						}
					} catch (Exception e) {
						JNIHelper.loge("download process stream exception: "
								+ e.getMessage());
						retryCurrentTask(
								UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
								0, MAX_RETRY_TIMES);
						return;
					} finally {
						closeCloseable(inStream);
					}
				} catch (Exception e) {
					JNIHelper.loge("download process file exception: "
							+ e.getMessage());
					sendCurrentTaskResponse(
							UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO,
							0);
					return;
				} finally {
					closeCloseable(inFile);
				}
				switch (ret) {
				case 0:
					// 下载成功
					fCache.renameTo(new File(DOWNLOAD_FILE_ROOT, task.strTaskId));
					sendCurrentTaskSuccessResponse();
					break;
				case 1:
				case 2:  // 网络类型错误
					// 重试当前下载任务
					retryCurrentTask(
							ret == 1 ?
									UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO : UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
							0, MAX_RETRY_TIMES);
					break;
				case 3:
					retryCurrentTask(UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP, 0, -1);
					break;
				case -1:
					// 需要清理缓存，重新下载
					mDownloadHttpFileTaskHandler
							.postDelayed(
									new Runnable2<UiInnerNet.DownloadHttpFileTask, File>(
											task, fCache) {
										@Override
										public void run() {
											downloadFileAgain(mP1, mP2);
										}
									}, 3000);
					break;
				}
			} catch (Exception e) {
				if (e instanceof UnknownHostException) {
					//断开网络时报这个错
				}
				JNIHelper.loge("download fail request: " + e.getMessage());
				retryCurrentTask(
						UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST,
						0, MAX_RETRY_TIMES);
				return;
			} finally {
				abortCurrentHttpRequest();
			}
		}
	};

	public void freeCacheFiles(File root) {
		File[] fs = root.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				// 删除超过30天的缓存文件
				if (System.currentTimeMillis() - pathname.lastModified() >= CACHE_CLEAR_TIME)
					return true;
				return false;
			}
		});
		if (fs != null) {
			for (File f : fs) {
				if (f.isDirectory()) {
					freeCacheFiles(f);
				} else {
					JNIHelper.logd("clear cache file: " + f.getPath());
					f.delete();
				}
			}
		}
		// root.delete();
	}

	public void freeCacheFiles() {
		File d = new File(Environment.getExternalStorageDirectory(),
				"txz/cache");
		freeCacheFiles(d);
	}

	public void freeLogFiles() {
		for (int i = 9; i > 0; --i) {
			File f = new File(LOG_FILE_ROOT, "text_all_" + i);
			f.delete();
			f = new File(LOG_FILE_ROOT, "format_all_" + i);
			f.delete();
		}
	}

	public void freeCrashFiles() {
		try {
			File d = new File(Environment.getExternalStorageDirectory(),
					"txz/report");
			File[] fs = d.listFiles();
			Map<String, Set<File>> mapReportFiles = new HashMap<String, Set<File>>();
			if (fs != null) {
				for (File f : fs) {
					AppLogic.heartbeatSlowGround();
					
					if (f.isDirectory()) {
						continue;
					}

					String suffix = ".";
					int n = f.getName().lastIndexOf('.');
					if (n > 0) {
						suffix = f.getName().substring(n);
					}
					String key = "" + f.length() + suffix;
					Set<File> set = mapReportFiles.get(key);
					if (set != null) {
						if (set.size() >= 3) {
							File fd = set.iterator().next();
							JNIHelper.logd("delete report file: "
									+ fd.getAbsolutePath());
							fd.delete();
						}
						set.add(f);
					} else {
						set = new HashSet<File>();
						set.add(f);
						mapReportFiles.put(key, set);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	Runnable mRunnableReserveDownloadSpace = new Runnable() {
		@Override
		public void run() {
			reserveDwonloadSpace();
		}
	};

	/**
	 * 抢占预留空间
	 * 
	 * @return 返回是否达到预留大小
	 */
	public boolean reserveDwonloadSpace() {
		AppLogic.removeBackGroundCallback(mRunnableReserveDownloadSpace);
		boolean ret = true;
		synchronized (RESERVE_BUFFER_FILE) {
			DOWNLOAD_FILE_ROOT.mkdirs();
			long len = RESERVE_BUFFER_FILE.length();
			if (len < RESERVE_BUFFER_SIZE) {
				long reserve = RESERVE_DISK_SIZE; // 继续保留至少1M的空间
				long need = RESERVE_BUFFER_SIZE - len;
				long free = DOWNLOAD_FILE_ROOT.getFreeSpace();
				if (free < need + reserve) {
					freeCacheFiles();
					freeLogFiles();
				}
				free = DOWNLOAD_FILE_ROOT.getFreeSpace() - reserve;
				if (free > 0 && free < need) {
					need = free;
					ret = false;
				}
				long total = len + need;
				RandomAccessFile f = null;
				try {
					f = new RandomAccessFile(RESERVE_BUFFER_FILE, "rw");
					f.setLength(total);
				} catch (Exception e) {
					JNIHelper.logw("total=" + total + ", free=" + free + "/"
							+ DOWNLOAD_FILE_ROOT.getFreeSpace() + ", need="
							+ need);
					e.printStackTrace();
					ret = false;
				} finally {
					closeCloseable(f);
				}
			}
			if (ret == false) {
				JNIHelper.logw("reserve space is not enough");
				// 如果预分配空间失败，5分钟后尝试预分配一次
				AppLogic.runOnBackGround(mRunnableReserveDownloadSpace,
						5 * 60 * 1000);
			}
			return ret;
		}
	}
	
	public void monitor(String strTaskId, String strEvent, int retCode){
		if (NetworkManager.getInstance().hasNet()){
			String strTaskType = getTaskType(strTaskId);
			String strRetCode = getRetCode(strEvent, retCode);
			String strAttr = MONITOR_MODULE + "." + strTaskType + "." + strEvent + "." + strRetCode;
			JNIHelper.logd("monitor_attr:" + strAttr);
			MonitorUtil.monitorCumulant(strAttr);
		}
	}
	
	private String getRetCode(String strEvent, int retCode){
		String strRetCode = "" + retCode;
		if(EVENT_END.equals(strEvent)){
			if(retCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS){
				strRetCode = RETCODE_SUCCESS;
			}else if(retCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_IO){
				strRetCode = RETCODE_FAIL_IO;
			}else if(retCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_HTTP){
				strRetCode = RETCODE_FAIL_HTTP;
			}else if(retCode == UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_FAIL_REQUEST){
				strRetCode = RETCODE_FAIL_REQUEST;
			}else{
				strRetCode = "" + retCode;
			}
		}else if(EVENT_ENTER.equals(strEvent)){
			strRetCode = "all";
		}else if(EVENT_RESP_ENTER.equals(strEvent)){
			strRetCode = "all";
		}
		return strRetCode;
	}
	
	private String getTaskType(String strTaskId){
		String strType = "other";
		
		if (TextUtils.isEmpty(strTaskId)){
			strType = "empty";
		}else if(strTaskId.startsWith(".upgrade_")){
			strType = "upgrade";
		}else if(strTaskId.startsWith(".plugin_")){
			strType = "plugin";
		}
		
		return strType;
	}
    // 下载进度监听
    private final HashMap<String, DownloadTaskProgressChangeListener> mDownloadTaskProgressChangeListeners = new HashMap<String, DownloadTaskProgressChangeListener>();

    public void registerDownloadTaskStatusChangeListener(String strTaskId, DownloadTaskProgressChangeListener listener) {
        synchronized (mDownloadTaskProgressChangeListeners) {
            mDownloadTaskProgressChangeListeners.put(strTaskId, listener);
        }
        if (mDownloadProgressDispatchHandler == null) {
            mDownloadProgressDispatchThread = new HandlerThread("DownloadStatusDispatch");
            mDownloadProgressDispatchThread.start();
            mDownloadProgressDispatchHandler = new Handler(mDownloadProgressDispatchThread.getLooper());
        }
    }

    public void unregisterDownloadTaskStatusChangeListener(String strTaskId) {
        synchronized (mDownloadTaskProgressChangeListeners) {
            mDownloadTaskProgressChangeListeners.remove(strTaskId);
        }
        if (mDownloadTaskProgressChangeListeners.isEmpty() && mDownloadProgressDispatchThread != null) {
            mDownloadProgressDispatchThread.quit();
            mDownloadProgressDispatchThread = null;
            mDownloadProgressDispatchHandler = null;
        }
    }

    public interface DownloadTaskProgressChangeListener {
        void onProgressChange(UiInnerNet.DownloadHttpFileTask task);
    }

    private void dispatchTaskProgressChange(final UiInnerNet.DownloadHttpFileTask task) {
        if (mDownloadProgressDispatchHandler == null) {
            return;
        }
        mDownloadProgressDispatchHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mDownloadTaskProgressChangeListeners) {
                    DownloadTaskProgressChangeListener listener = mDownloadTaskProgressChangeListeners.get(task.strTaskId);
                    if (listener != null) {
                        listener.onProgressChange(task);
                    }
                }
            }
        });
    }


	/**
	 * 用来存储下载任务的原始大小和已下载的大小
	 */
	public static class DownloadFileSizeInfo {
		public DownloadFileSizeInfo(long mFullSize , long mCacheSize) {
			this.mCacheSize = mCacheSize;
			this.mFullSize = mFullSize;
		}

		public long mCacheSize;
    	public long mFullSize;
	}

    public DownloadFileSizeInfo getDownloadFileSizeInfo(String taskId, String strUrl){

		/**
		 * 下载任务结构，DATA+CRC32(long)+DWONLOAD(long)+TOTAL(long)
		 */
		CRC32 crc32 = new CRC32();
		long downloadSize = 0;
		long totalSize = 0;
		long curCrc32 = 0;
		byte[] checkBytes = new byte[CHECK_CACHE_SIZE];

		File fCache = new File(DOWNLOAD_FILE_ROOT, taskId
				+ ".tmp");
		if (fCache.exists()) {
			// 尺寸不够
			if (fCache.length() < CHECK_CACHE_SIZE + 24 /*
															 * CRC32(long)+DWONLOAD
															 * (long)+TOTAL(long
															 * )
															 */) {
				JNIHelper.loge("cache file is too small");
				return new DownloadFileSizeInfo(getRealTotalSize(strUrl),0);
			}
			// 获取长度和校验
			RandomAccessFile inFile = null;
			boolean checked = true;
			byte[] mBuffer = new byte[READ_BUFFER_SIZE];
			try {
				inFile = new RandomAccessFile(fCache, "rw");
				inFile.seek(fCache.length() - 24);
				curCrc32 = inFile.readLong();
				downloadSize = inFile.readLong();
				totalSize = inFile.readLong();
				if (totalSize + 24 != fCache.length()
						|| downloadSize >= totalSize) {
					// 长度校验不通过
					JNIHelper.loge("cache file length not match: length="
							+ inFile.length() + ", total=" + totalSize
							+ ", download=" + downloadSize);
					checked = false;
				} else if (downloadSize >= CHECK_CACHE_SIZE) {
					// crc32校验
					inFile.seek(0);
					long total = 0;
					while (total < downloadSize) {
						int r = (int) ((downloadSize - total) < mBuffer.length ? (downloadSize - total)
								: mBuffer.length);
						r = inFile.read(mBuffer, 0, r);
						if (r < 0) {
							// 读取异常
							JNIHelper.loge("read file error: " + total
									+ "/" + downloadSize);
							checked = false;
							break;
						}
						total += r;
						crc32.update(mBuffer, 0, r);
					}
					if (checked && crc32.getValue() != curCrc32) {
						JNIHelper
								.loge("cache file check not match: sumBytes="
										+ crc32.getValue()
										+ ", sumRead="
										+ curCrc32);
						checked = false;
					}

					if (checked) {
						// 读取最后的几个校验字节
						inFile.seek(downloadSize - checkBytes.length);
						int t = 0;
						while (t < checkBytes.length) {
							int r = inFile.read(checkBytes, t,
									checkBytes.length - t);
							if (r < 0) {
								// 读取异常
								JNIHelper.loge("read file error: " + total
										+ "/" + downloadSize);
								checked = false;
								break;
							}
							t += r;
						}
					}
				}
			} catch (Exception e) {
				JNIHelper.loge("read cache file expcetion: " + e.getMessage());
				checked = false;
			} finally {
				closeCloseable(inFile);
			}
			if (!checked) {
				return new DownloadFileSizeInfo(getRealTotalSize(strUrl),0);
			}
		}

		// 全部重下
		if (downloadSize < CHECK_CACHE_SIZE) {
			JNIHelper.logw("abandon download cache size=" + downloadSize);
			return new DownloadFileSizeInfo(getRealTotalSize(strUrl),0);
		}

		DefaultHttpClient mHttpClient = new DefaultHttpClient();
		mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_CONNECT);
		mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_READ_DATA);
		HttpGet mHttpReq = new HttpGet(strUrl);
		if (downloadSize > 0) {
			JNIHelper.logd("download from: " + downloadSize
					+ ", CHECK_CACHE_SIZE=" + CHECK_CACHE_SIZE);
			mHttpReq.addHeader("Range", "bytes="
					+ (downloadSize - CHECK_CACHE_SIZE) + "-");
		}
		try {
			HttpResponse res = mHttpClient.execute(mHttpReq);
			int resCode = res.getStatusLine().getStatusCode();
			HttpEntity httpEntity = res.getEntity();
			if (resCode != 206) {
				// 碎片获取失败
				return new DownloadFileSizeInfo(httpEntity.getContentLength(),0);
			}

			String contentRange = res.getFirstHeader("Content-Range")
					.getValue();
			long retTotal = Long.parseLong(contentRange.split("/")[1]);
			if (retTotal != totalSize) {
				// 校验大小失败
				return new DownloadFileSizeInfo(retTotal, 0);
			}

			if (httpEntity.getContentLength() != totalSize - downloadSize
					+ CHECK_CACHE_SIZE) {
				JNIHelper.logw("Content-Length error: "
						+ httpEntity.getContentLength() + "!=" + totalSize
						+ "-" + downloadSize + "+" + CHECK_CACHE_SIZE);
				return new DownloadFileSizeInfo(retTotal,0);
			}
			//检测到有部分下载成功的
			return new DownloadFileSizeInfo(totalSize, downloadSize - CHECK_CACHE_SIZE);
		}catch (Exception e) {

		}
		return new DownloadFileSizeInfo(getRealTotalSize(strUrl),0);
	}

	private long getRealTotalSize(String downloadUrl){
		if (downloadUrl == null || "".equals(downloadUrl)) {
			return 0L;
		}
		DefaultHttpClient mHttpClient = null;
		HttpGet mHttpReq = null;
		try {
			mHttpClient = new DefaultHttpClient();
			mHttpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			mHttpReq = new HttpGet(downloadUrl);
			HttpResponse res = mHttpClient.execute(mHttpReq);
			int resCode = res.getStatusLine().getStatusCode();
			if (resCode == 200) {
				HttpEntity entity = res.getEntity();
				return entity.getContentLength();
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (mHttpReq != null) {
					mHttpReq.abort();
					mHttpReq = null;
				}
				mHttpClient = null;
			} catch (Exception e) {
			}
		}

		return 0;
	}
}
