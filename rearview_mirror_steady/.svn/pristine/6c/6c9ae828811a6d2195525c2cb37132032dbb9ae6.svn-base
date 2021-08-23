package com.txznet.sdk;

import org.json.JSONObject;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZService.CommandProcessor;

/**
 * 录像管理器
 * 如果需要抓拍前后摄像头照片需要设置摄像头类型
 * {@link #setSupportCameraType(CameraType)}
 * 前置照片{@link CapturePictureListener#onFrontPicture(String)}
 * 后置照片{@link CapturePictureListener#onBackPicture(String)}
 */
public class TXZCameraManager {
	public static final int CAMERA_FRONT = 1; // 前置摄像头
	public static final int CAMERA_BACK = 2; // 后置摄像头

	// 远程调用参数名
	public static final String REMOTE_NAME_VIDEO_PATH = "path"; // 抓拍视频地址
	public static final String REMOTE_NAME_VIDEO_THUMBNAIL_PATH = "thumbnail"; // 抓拍视频缩略图地址
	public static final String REMOTE_NAME_CAMERA_POSITION = "position"; // 摄像头位置
	public static final String REMOTE_NAME_TASK_ID = "taskId"; // 抓拍视频taskId
	public static final String REMOTE_NAME_ERROR_CODE = "errCode"; // 错误码
	public static final String REMOTE_NAME_ERROR_MESSAGE = "errMessage"; // 错误信息

    private Boolean mUseWakeupCapturePhoto; // 是否启用抓拍照片命令
    private Boolean mUseWakeupCaptureVideo; // 是否启用抓拍视频命令

	private static TXZCameraManager sInstance = new TXZCameraManager();

	private TXZCameraManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZCameraManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetTool) {
            setCameraTool(mCameraTool);
        }

		if (mUseWakeupCapturePhoto != null) {
            useWakeupCapturePhoto(mUseWakeupCapturePhoto);
        }

        if (mUseWakeupCaptureVideo != null) {
		    useWakeupCaptureVideo(mUseWakeupCaptureVideo);
        }

		if (mTimeout!=null ){
			setCapturePhotoTimeout(mTimeout);
		}
		
		if (null != mCaptureVideoTimeout) {
			setCaptureVideoTimeout(mCaptureVideoTimeout);
		}
		
		if (null != mSupportCameraType) {
			setSupportCameraType(mSupportCameraType);
		}
	}

	/**
	 * 未知异常
	 */
	public final static int CAPTURE_ERROR_UNKNOW = 7101;
	/**
	 * 没有摄像头
	 */
	public final static int CAPTURE_ERROR_NO_CAMERA = 7102;
	/**
	 * 设备不支持
	 */
	public final static int CAPTURE_ERROR_NO_SUPPORT = 7103;
	/**
	 * IO发生异常
	 */
	public final static int CAPTURE_ERROR_IO_ERROR = 7104;
	/**
	 * 没有对应时间戳抓拍
	 */
	public final static int CAPTURE_ERROR_NOT_FOUND = 7105;

	final static int CAPTURE_ERROR_BEGIN = CAPTURE_ERROR_UNKNOW;
	final static int CAPTURE_ERROR_END = CAPTURE_ERROR_NOT_FOUND;

	/**
	 * 抓拍图片回调
	 * 
	 * @author txz
	 *
	 */
	public static interface CapturePictureListener {
		/**
		 * 保存图片回调
		 * 
		 * @param path
		 */
		public void onSave(String path);

		/**
		 * 出错回调
		 */
		public void onError(int errCode, String errDesc);
		
		/** 前置摄像头照片 */
		public void onFrontPicture(String path);
		/** 后置摄像头照片 */
		public void onBackPicture(String path);
	}

	public static interface CaptureVideoListener {
		public void onSave(String Path, String thumbnailPath);

		public void onError(int errCode, String errDesc);
	}

	/**
	 * 摄像头工具类
	 * 
	 * @author txz
	 *
	 */
	public static interface CameraTool {

		/**
		 * 抓取行车记录仪照片
		 * 
		 * @param time
		 *            抓拍图片的时间戳，应从System.currentTimeMillis()获取，需要注意的是0表示当前。
		 * @param listener
		 * @return
		 */
		public boolean capturePicure(long time, CapturePictureListener listener);

		/**
		 * 调用行车记录仪抓拍视频
		 * 
		 * @param frontListener
		 *            前置摄像头抓拍回调
		 * @param backListener
		 *            后置摄像头抓拍回调
		 * @return
		 */
		public boolean captureVideo(CaptureVideoListener frontListener,
				CaptureVideoListener backListener);
	}

	private boolean mHasSetTool = false;
	private CameraTool mCameraTool = null;

	/**
	 * 设置摄像头工具
	 * 
	 * @param tool
	 */
	public void setCameraTool(CameraTool tool) {
		mHasSetTool = true;
		mCameraTool = tool;

		if (tool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.camera.cleartool", null, null);
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.settool", null, null);
		TXZService.setCommandProcessor("tool.camera.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("capturePicture")) {
					try {
						final JSONObject json = new JSONObject(new String(data));
						mCameraTool.capturePicure(json.getLong("time"),
								new CapturePictureListener() {
									@Override
									public void onSave(String path) {
										try {
											json.put("path", path);
											ServiceManager
													.getInstance()
													.sendInvoke(
															ServiceManager.TXZ,
															"txz.camera.capturePicture.onSave",
															json.toString()
																	.getBytes(),
															null);
										} catch (Exception e) {
										}
									}

									@Override
									public void onError(int errCode,
											String errDesc) {
										try {
											if (errCode < CAPTURE_ERROR_BEGIN
													|| errCode > CAPTURE_ERROR_END) {
												errCode = CAPTURE_ERROR_UNKNOW;
											}
											json.put("errCode", errCode);
											json.put("errDesc", errDesc);
											ServiceManager
													.getInstance()
													.sendInvoke(
															ServiceManager.TXZ,
															"txz.camera.capturePicture.onError",
															json.toString()
																	.getBytes(),
															null);
										} catch (Exception e) {
										}
									}

									@Override
									public void onFrontPicture(String path) {
										try {
											json.put("path", path);
											json.put("position", CAMERA_FRONT);
											ServiceManager
													.getInstance()
													.sendInvoke(
															ServiceManager.TXZ,
															"txz.camera.capturePicture.onSave",
															json.toString()
																	.getBytes(),
															null);
										} catch (Exception e) {
										}
									}

									@Override
									public void onBackPicture(String path) {
										try {
											json.put("path", path);
											json.put("position", CAMERA_BACK);
											ServiceManager
													.getInstance()
													.sendInvoke(
															ServiceManager.TXZ,
															"txz.camera.capturePicture.onSave",
															json.toString()
																	.getBytes(),
															null);
										} catch (Exception e) {
										}
									}
								});
					} catch (Exception e) {
					}
				}

				if (command.equals("captureVideo")) {
					try {
						final JSONObject json = new JSONObject(new String(data));
						final long taskId = json.getLong(REMOTE_NAME_TASK_ID);

						mCameraTool.captureVideo(new CaptureVideoListener() {

							@Override
							public void onSave(String path, String thumbnailPath) {
								reportCaptureVideoSuccess(taskId, CAMERA_FRONT, path, thumbnailPath);
							}

							@Override
							public void onError(int errCode, String errDesc) {
								if (errCode < CAPTURE_ERROR_BEGIN
										|| errCode > CAPTURE_ERROR_END) {
									errCode = CAPTURE_ERROR_UNKNOW;
								}
								
								reportCaptureVideoError(taskId, CAMERA_FRONT, errCode, errDesc);
							}
						}, new CaptureVideoListener() {

							@Override
							public void onSave(String path, String thumbnailPath) {
								reportCaptureVideoSuccess(taskId, CAMERA_BACK, path, thumbnailPath);
							}

							@Override
							public void onError(int errCode, String errDesc) {
								if (errCode < CAPTURE_ERROR_BEGIN
										|| errCode > CAPTURE_ERROR_END) {
									errCode = CAPTURE_ERROR_UNKNOW;
								}
								
								reportCaptureVideoError(taskId, CAMERA_BACK, errCode, errDesc);
							}
						});
					} catch (Exception e) {

					}

				}
				return null;
			}
		});
	}

	private void reportCaptureVideoSuccess(long taskId, int position,
			String videoPath, String thumbnailPath) {
		try {
			final JSONObject json = new JSONObject();
			json.put(REMOTE_NAME_TASK_ID, taskId);
			json.put(REMOTE_NAME_CAMERA_POSITION, position);
			json.put(REMOTE_NAME_VIDEO_PATH, videoPath);
			json.put(REMOTE_NAME_VIDEO_THUMBNAIL_PATH, thumbnailPath);

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.camera.captureVideo.onSave",
					json.toString().getBytes(), null);
		} catch (Exception e) {

		}

	}

	private void reportCaptureVideoError(long taskId, int position, int errCode,
			String errDesc) {
		try {
			final JSONObject json = new JSONObject();
			json.put(REMOTE_NAME_TASK_ID, taskId);
			json.put(REMOTE_NAME_ERROR_CODE, errCode);
			json.put(REMOTE_NAME_ERROR_MESSAGE, errDesc);
			json.put(REMOTE_NAME_CAMERA_POSITION, position);

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.camera.captureVideo.onError",
					json.toString().getBytes(), null);
		} catch (Exception e) {

		}

	}

	/**
	 * 使用全局唤醒词进行抓拍
	 * 
	 * @param enable
	 */
	public void useWakeupCapturePhoto(boolean enable) {
		mUseWakeupCapturePhoto = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.useWakeupCapturePhoto", String.valueOf(enable).getBytes(), null);
	}

    /**
     * 使用全局唤醒词字发起视频抓拍
     * @param enable
     */
    public void useWakeupCaptureVideo(boolean enable) {
        mUseWakeupCaptureVideo = enable;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.camera.useWakeupCaptureVideo", String.valueOf(enable).getBytes(), null);
    }

	/**
	 * 抓拍照片并上传
	 */
	public void capturePhoto() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.capturePhoto", null, null);
	}
	
	
	public Long mTimeout =  null;
	/**
	 * 设置抓拍超时时间
	 * @param timeout
	 */
	public void setCapturePhotoTimeout(long timeout){
		mTimeout = timeout;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.setCameraTimeout", (""+mTimeout).getBytes(), null);
	}
	
	public Long mCaptureVideoTimeout =  null;
	/**
	 * 设置抓拍视频超时时间，单位ms
	 * @param timeout
	 */
	public void setCaptureVideoTimeout(long timeout){
		mCaptureVideoTimeout = timeout;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.setCaptureVideoTimeout", (""+mCaptureVideoTimeout).getBytes(), null);
	}
	
	public CameraType mSupportCameraType = null;
	
	public enum CameraType {
		/** 一个摄像头-前置 */
		SINGLE_CAMERA,
		/** 两个摄像头 */
		DUAL_CAMERA
	}
	
	/**
	 * 设备支持拍照摄像头类型
	 * @param cameraType
	 */
	public void setSupportCameraType(CameraType cameraType) {
		if (null == cameraType) {
			return;
		}
		mSupportCameraType = cameraType;
		int camera = 0;
		switch (cameraType) {
		case SINGLE_CAMERA:
			camera = 1;
			break;
		case DUAL_CAMERA:
			camera = 2;
			break;
		}
		if (camera == 0) {
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.camera.setSupportCameraType", (""+camera).getBytes(), null);
	}
}
