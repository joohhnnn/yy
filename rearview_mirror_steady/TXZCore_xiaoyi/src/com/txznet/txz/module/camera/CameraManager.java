package com.txznet.txz.module.camera;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.JSONObject;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZCameraManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import android.text.TextUtils;
import android.util.Log;

public class CameraManager extends IModule {
	static CameraManager sModuleInstance = null;

	private CameraManager() {

	}

	public static CameraManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (CameraManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new CameraManager();
			}
		}
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_UPLOAD_VIDEO);
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:

			switch (subEventId) {
			case UiEquipment.SUBEVENT_NOTIFY_UPLOAD_VIDEO:
				// 抓拍视频
				captureVideo(data);
				break;

			default:
				break;
			}

			break;

		default:
			break;
		}
		return 0;
	}

	String mRemoteCameraService;
	public boolean mUseWakeupCapturePhoto = false;

	public boolean hasRemoteProcTool() {
		return !TextUtils.isEmpty(mRemoteCameraService);
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			if (serviceName.equals(mRemoteCameraService)) {
				invokeTXZCamera(null, "cleartool", null);
			}
		}
	};

	public byte[] invokeTXZCamera(final String packageName, String command,
			byte[] data) {
		if (command.equals("cleartool")) {
			// 记录抓拍工具
			mRemoteCameraService = null;
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);

			clearGlobalCaptureCommand();

			return null;
		}
		if (command.equals("settool")) {
			ServiceManager.getInstance().addConnectionListener(
					mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName, "", null,
					new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// 记录工具
							if (data != null)
								mRemoteCameraService = packageName;
							addGlobalCaptureCommand();
						}
					});

			return null;
		}
		if (command.equals("capturePicture.onSave")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {
				@Override
				public void run() {
					CapturePictureListener l = null;
					try {
						JSONObject json = new JSONObject(new String(mP1));
						int reqId = json.getInt("id");
						l = mCaptureRequests.get(reqId);
						mCaptureRequests.remove(reqId);
						if (l != null)
							l.onSave(json.getString("path"));
					} catch (Exception e) {
						JNIHelper.loge("capturePicture data="
								+ (mP1 == null ? "null" : new String(mP1))
								+ ", error[" + e.getClass() + "::"
								+ e.getMessage() + "]");
						if (l != null) {
							int errCode = 7101;
							String errDesc = NativeData
									.getResString("RS_CAMERA_CAPTURE_ERROR");
							l.onError(errCode, errDesc);
						}
					}
				}
			}, 0);
			return null;
		}
		if (command.equals("capturePicture.onError")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {
				@Override
				public void run() {
					CapturePictureListener l = null;
					try {
						JSONObject json = new JSONObject(new String(mP1));
						int reqId = json.getInt("id");
						l = mCaptureRequests.get(reqId);
						mCaptureRequests.remove(reqId);
						if (l != null) {
							int errCode = 7101;
							if (json.has("errCode"))
								errCode = json.getInt("errCode");
							String errDesc = NativeData
									.getResString("RS_CAMERA_CAPTURE_ERROR");
							if (json.has("errDesc"))
								errDesc = json.getString("errDesc");
							l.onError(errCode, errDesc);
						}
					} catch (Exception e) {
						JNIHelper.loge("capturePicture data="
								+ (mP1 == null ? "null" : new String(mP1))
								+ ", error[" + e.getClass() + "::"
								+ e.getMessage() + "]");
						if (l != null) {
							int errCode = 7101;
							String errDesc = NativeData
									.getResString("RS_CAMERA_CAPTURE_ERROR");
							l.onError(errCode, errDesc);
						}
					}
				}
			}, 0);

			return null;
		}
		if ("captureVideo.onSave".equals(command)) {
			reportCaptureVideoSuccess(data);
		}
		if ("captureVideo.onError".equals(command)) {
			reportCaptureVideoError(data);
		}
		if ("useWakeupCapturePhoto".equals(command)) {
			mUseWakeupCapturePhoto = Boolean.parseBoolean(new String(data));
			addGlobalCaptureCommand();
			return null;
		}

		if ("capturePhoto".equals(command)) {
			WeixinManager.getInstance().capturePhoto(
					System.currentTimeMillis() - 1000 * 2, false, null);
		}
		return null;
	}

	public void clearGlobalCaptureCommand() {
		WakeupManager.getInstance().recoverWakeupFromAsr("WAKE_UP_TAKE_PHOTO");
	}

	public void addGlobalCaptureCommand() {
		if (!mUseWakeupCapturePhoto || TextUtils.isEmpty(mRemoteCameraService)) {
			clearGlobalCaptureCommand();
			return;
		}
		WakeupManager.getInstance().useWakeupAsAsr(
				new AsrUtil.AsrComplexSelectCallback() {
					@Override
					public String getTaskId() {
						return "WAKE_UP_TAKE_PHOTO";
					}

					@Override
					public boolean needAsrState() {
						return false;
					}

					@Override
					public void onCommandSelected(String type, String command) {
						if (type.equals("TAKE_PHOTO")) {
							RecorderWin.close();
							clearGlobalCaptureCommand();
							WeixinManager.getInstance().capturePhoto(
									System.currentTimeMillis() - 1000 * 2,
									false, new Runnable() {
										@Override
										public void run() {
											addGlobalCaptureCommand();
										}
									});
						}
					};
				}.addCommand("TAKE_PHOTO", "我要拍照", "抓拍照片", "抓拍图片"));
	}

	/**
	 * 抓拍图片回调
	 * 
	 * @author txz
	 *
	 */
	public static abstract class CapturePictureListener {
		long time = System.currentTimeMillis(); // 可用于抓拍超时处理

		/**
		 * 保存图片回调
		 * 
		 * @param path
		 */
		public abstract void onSave(String path);

		/**
		 * 出错回调
		 */
		public abstract void onError(int errCode, String errDesc);

		/**
		 * 超时
		 */
		public abstract void onTimeout();
	}

	private int mCaptureRequestId = new Random().nextInt();
	private Map<Integer, CapturePictureListener> mCaptureRequests = new HashMap<Integer, CapturePictureListener>();

	public void capturePicture(final long time,
			final CapturePictureListener listener, final long timeout) {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (TextUtils.isEmpty(mRemoteCameraService)) {
					// TtsManager.getInstance().speakText("没有设置抓拍工具");
					int errCode = 7103;
					String errDesc = "没有设置抓拍工具";
					listener.onError(errCode, errDesc);
					return;
				}
				try {
					JSONObject json = new JSONObject();
					json.put("time", time);
					json.put("id", mCaptureRequestId);
					ServiceManager.getInstance().sendInvoke(
							mRemoteCameraService, "tool.camera.capturePicture",
							json.toString().getBytes(), new GetDataCallback() {
								@Override
								public void onGetInvokeResponse(ServiceData data) {
									if (data == null) {
										int errCode = 7101;
										String errDesc = NativeData
												.getResString("RS_CAMERA_RESPONSE_ERROR");
										listener.onError(errCode, errDesc);
									}
								}
							});
					mCaptureRequests.put(mCaptureRequestId, listener);
					// 设置10秒超时
					AppLogic.runOnBackGround(new Runnable1<Integer>(
							mCaptureRequestId) {
						@Override
						public void run() {
							if (mCaptureRequests.containsKey(mP1)) {
								mCaptureRequests.remove(mP1);
								listener.onTimeout();
							}
						}
					}, timeout);
					mCaptureRequestId++;
				} catch (Exception e) {
					JNIHelper.loge("capturePicture error[" + e.getClass()
							+ "::" + e.getMessage() + "]");
					StringWriter sWriter = new StringWriter();
					e.printStackTrace(new PrintWriter(sWriter));
					JNIHelper.loge("capturePicture exception: "
							+ sWriter.toString());
					int errCode = 7101;
					String errDesc = NativeData
							.getResString("RS_CAMERA_CAPTURE_ERROR");
					listener.onError(errCode, errDesc);
				}
			}
		}, 0);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// 抓拍视频

	private static final int CAPTURE_VIDEO_TIMEOUT = 30000;
	private long mLastCaptureVideoPushMid = -1; // 当前抓拍视频请求taskId，由后台传入更新
	private boolean mIsFrontReported = false;
	private boolean mIsBackReported = false;

	private Runnable mCaptureVideoTimeoutTask = new Runnable() {

		@Override
		public void run() {
			long taskId = mLastCaptureVideoPushMid;
			mLastCaptureVideoPushMid = -1;

			if (!mIsFrontReported) {
				Log.d("asd", "front time out");
				reportCaptureVideoError(taskId,
						UiEquipment.EC_UPLOAD_VIDEO_CATCH_TIMEOUT, "抓拍超时",
						TXZCameraManager.CAMERA_FRONT);
			}

			if (!mIsBackReported) {
				Log.d("asd", "back time out");
				reportCaptureVideoError(taskId,
						UiEquipment.EC_UPLOAD_VIDEO_CATCH_TIMEOUT, "抓拍超时",
						TXZCameraManager.CAMERA_BACK);
			}
		}
	};

	private void captureVideo(byte[] data) {
		try {
			AppLogic.removeBackGroundCallback(mCaptureVideoTimeoutTask);
			// 解析请求参数
			UiEquipment.Notify_UploadVideo param = UiEquipment.Notify_UploadVideo
					.parseFrom(data);
			final long taskId = param.uint64PushMid;

			mLastCaptureVideoPushMid = taskId;

			if (TextUtils.isEmpty(mRemoteCameraService)) {
				// TtsManager.getInstance().speakText("没有设置抓拍工具");
				int errCode = UiEquipment.EC_UPLOAD_VIDEO_NO_SUPPORT;
				String errDesc = NativeData.getResString("RS_CAMERA_NO_TOOL");
				reportCaptureVideoError(taskId, errCode, errDesc);
				return;
			}

			JSONObject json = new JSONObject();
			json.put(TXZCameraManager.REMOTE_NAME_TASK_ID, taskId);
			ServiceManager.getInstance().sendInvoke(mRemoteCameraService,
					"tool.camera.captureVideo", json.toString().getBytes(),
					new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							if (data == null) {
								int errCode = UiEquipment.EC_UPLOAD_VIDEO_UNKNOWN;
								String errDesc = NativeData
										.getResString("RS_CAMERA_RESPONSE_ERROR");
								reportCaptureVideoError(taskId, errCode,
										errDesc);
							}
						}
					});

			mIsFrontReported = false;
			mIsBackReported = false;
			
			AppLogic.runOnBackGround(mCaptureVideoTimeoutTask, CAPTURE_VIDEO_TIMEOUT);

		} catch (Exception e) {
			JNIHelper.loge("captureVideo error[" + e.getClass() + "::"
					+ e.getMessage() + "]");
			StringWriter sWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(sWriter));
			JNIHelper.loge("captreVideo exception: " + sWriter.toString());
		}

	}

	private void reportCaptureVideoSuccess(byte[] data) {
		try {
			// 解析回传参数
			JSONObject json = new JSONObject(new String(data));
			long taskId = json.getLong(TXZCameraManager.REMOTE_NAME_TASK_ID);

			if (taskId == mLastCaptureVideoPushMid) {
				int position = json
						.getInt(TXZCameraManager.REMOTE_NAME_CAMERA_POSITION);
				String videoPath = json
						.getString(TXZCameraManager.REMOTE_NAME_VIDEO_PATH);
				String thumbnailPath = json
						.getString(TXZCameraManager.REMOTE_NAME_VIDEO_THUMBNAIL_PATH);
				
				updateVideoTimeoutFlag(position);

				UiEquipment.Req_UploadVideo req = new UiEquipment.Req_UploadVideo();
				req.uint32Ground = position;
				req.strVideoPath = videoPath;
				req.uint64PushMid = taskId;
				req.strPicPath = thumbnailPath;
				// 获取视频文件类型（后缀名）
				String videoType = "";
				int index = videoPath.lastIndexOf(".");
				if (index >= 0) {
					videoType = videoPath.substring(index + 1);
				}
				req.strVideoType = videoType;

				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
						UiEquipment.SUBEVENT_REQ_UPLOAD_VIDEO, req);
			}

		} catch (Exception e) {

		}

	}

	private void reportCaptureVideoError(byte[] data) {
		try {
			JSONObject json = new JSONObject(new String(data));
			long taskId = json.getLong(TXZCameraManager.REMOTE_NAME_TASK_ID);

			if (taskId == mLastCaptureVideoPushMid) {
				int position = json
						.getInt(TXZCameraManager.REMOTE_NAME_CAMERA_POSITION);

				int errCode = 7124;
				if (json.has(TXZCameraManager.REMOTE_NAME_ERROR_CODE))
					errCode = json
							.getInt(TXZCameraManager.REMOTE_NAME_ERROR_CODE);
				String errDesc = NativeData.getResString("RS_CAMERA_CAPTURE_ERROR");
				if (json.has(TXZCameraManager.REMOTE_NAME_ERROR_MESSAGE))
					errDesc = json
							.getString(TXZCameraManager.REMOTE_NAME_ERROR_MESSAGE);

				reportCaptureVideoError(taskId, errCode, errDesc, position);
			}
		} catch (Exception e) {

		}

	}

	private void reportCaptureVideoError(long taskId, int errCode,
			String errDesc) {
		reportCaptureVideoError(taskId, errCode, errDesc, 1);
		reportCaptureVideoError(taskId, errCode, errDesc, 2);
	}

	private void reportCaptureVideoError(long taskId, int errCode,
			String errDesc, int cameraPosition) {
		UiEquipment.Req_UploadVideo req = new UiEquipment.Req_UploadVideo();
		req.uint32ErrCode = convertVideoErrCode(errCode, cameraPosition);
		req.strErrMsg = errDesc;
		req.uint32Ground = cameraPosition;
		
		updateVideoTimeoutFlag(cameraPosition);

		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_UPLOAD_VIDEO, req);

	}

	/**
	 * 转换错误码， sdk抓拍接口没有区分视频和照片抓拍错误码，视频抓拍时需要对远程传过来的错误码进行相应转换
	 * 
	 * @param errCode
	 */
	private int convertVideoErrCode(int errCode, int position) {
		if (errCode == TXZCameraManager.CAPTURE_ERROR_IO_ERROR) {
			return UiEquipment.EC_UPLOAD_VIDEO_IO_ERROR;
		} else if (errCode == TXZCameraManager.CAPTURE_ERROR_NO_CAMERA) {
			return (position == TXZCameraManager.CAMERA_FRONT) ? UiEquipment.EC_UPLOAD_VIDEO_NO_FRONT_CAMERA
					: UiEquipment.EC_UPLOAD_VIDEO_NO_BACK_CAMERA;
		} else if (errCode == TXZCameraManager.CAPTURE_ERROR_NO_SUPPORT) {
			return UiEquipment.EC_UPLOAD_VIDEO_NO_SUPPORT;
		} else if (errCode == TXZCameraManager.CAPTURE_ERROR_NOT_FOUND) {
			return UiEquipment.EC_UPLOAD_VIDEO_NOT_FOUND;
		} else {
			return errCode;
		}
	}

	private void updateVideoTimeoutFlag(int position) {
		if (position == TXZCameraManager.CAMERA_FRONT) {
			mIsFrontReported = true;
		}

		if (position == TXZCameraManager.CAMERA_BACK) {
			mIsBackReported = true;
		}
	}
}
