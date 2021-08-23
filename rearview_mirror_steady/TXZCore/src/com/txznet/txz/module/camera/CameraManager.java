package com.txznet.txz.module.camera;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZCameraManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.ImageUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.os.Environment;
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
				UiEquipment.SUBEVENT_NOTIFY_UPLOAD_PIC);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_UPLOAD_PIC);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_UPLOAD_VIDEO);
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}
	
	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.camera.", new PluginManager.CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				if(TextUtils.equals(command, "capture_picture")){
					if(null == args || args.length < 2){
						LogUtil.loge("CameraManager Plugin Error: arguments eroor");
						return null;
					}
					//使用参考 PluginManager.invoke("txz.camera.capture_picture", "txz.camera.test", 5000); 
					//第二个参数为 抓拍回调的命令字
					//第三个参数为 超时时间
					MonitorUtil.monitorCumulant(MonitorUtil.SDK_START_CAPUTRE_PICTURE_PLUGIN);
					final String src = (String) args[0];
					int timeout = (Integer) args[1];
					capturePicture(0L, System.currentTimeMillis(), new CapturePictureListener() {
						
						@Override
						public void onTimeout() {
							MonitorUtil.monitorCumulant(MonitorUtil.SDK_TIMEOUT_CAPTURE_PICTURE);
							JSONObject jsonObject = new JSONObject();
							try {
								jsonObject.put("success", false);
								jsonObject.put("errCode", 0);
								jsonObject.put("errDesc", "time out");
							} catch (JSONException e) {
								LogUtil.loge("CameraManager Plugin Error:", e);
								e.printStackTrace();
							}
							PluginManager.invoke(src, jsonObject.toString());
						}
						
						@Override
						public void onSave(String path, int position) {
							if (position != TXZCameraManager.CAMERA_FRONT) {
								// 非前置摄像头不回调给插件
								return;
							}
							MonitorUtil.monitorCumulant(MonitorUtil.SDK_GOTTON_CAPTURE_PICTURE_ALL);
							JSONObject jsonObject = new JSONObject();
							try {
								jsonObject.put("success", true);
								jsonObject.put("path", path);
							} catch (JSONException e) {
								LogUtil.loge("CameraManager Plugin Error:", e);
								e.printStackTrace();
							}
							PluginManager.invoke(src, jsonObject.toString());
						}
						
						@Override
						public void onError(int errCode, String errDesc) {
							MonitorUtil.monitorCumulant(MonitorUtil.SDK_ONERROR_CAPTURE_PICTURE);
							JSONObject jsonObject = new JSONObject();
							try {
								jsonObject.put("success", false);
								jsonObject.put("errCode", errCode);
								jsonObject.put("errDesc", errDesc);
							} catch (JSONException e) {
								LogUtil.loge("CameraManager Plugin Error:", e);
								e.printStackTrace();
							}
							PluginManager.invoke(src, jsonObject.toString());
						}
					}, timeout);
				}
				return null;
			}
		});
		
		return super.initialize_addPluginCommandProcessor();
	}
	
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_NOTIFY_UPLOAD_PIC: {
				try {
					UiEquipment.Notify_UploadPic notifyUploadPic = UiEquipment.Notify_UploadPic.parseFrom(data);
					int type = -1;
					if (notifyUploadPic.uint32UploadType != null) {
						// 目前后台的逻辑为必须上传大图，单独上传缩略图无效
						type = notifyUploadPic.uint32UploadType;
					}
					JNIHelper.logd("capturePicture notify, type=" + type);
					capturePhoto(notifyUploadPic.uint64PushMid , 0, true, type, null);
				} catch (InvalidProtocolBufferNanoException e) {
					LogUtil.loge("capturePicture InvalidProtocolBufferNanoException");
				}
				break;
			}
			case UiEquipment.SUBEVENT_RESP_UPLOAD_PIC:
				break;
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
	private boolean mUseWakeupCapturePhoto = false;
	private boolean mUseWakeupCaptureVideo = false;


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
			clearGlobalCaptureVideoCommand();

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
					reportCapturePictureSuccess(mP1);
					/*CapturePictureListener l = null;
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
					}*/
				}
			}, 0);
			return null;
		}
		if (command.equals("capturePicture.onError")) {
			AppLogic.runOnBackGround(new Runnable1<byte[]>(data) {
				@Override
				public void run() {
					reportCapturePictureError(mP1);
					/*CapturePictureListener l = null;
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
					}*/
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
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    addGlobalCaptureCommand();
                }
            });
		}

		if ("useWakeupCaptureVideo".equals(command)) {
            mUseWakeupCaptureVideo = Boolean.parseBoolean(new String(data));
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    addGlobalCaptureVideoCommand();
                }
            });
        }

		if ("capturePhoto".equals(command)) {
			MonitorUtil.monitorCumulant(MonitorUtil.SDK_START_CAPTURE_PICTURE_ALL,MonitorUtil.SDK_START_CAPTURE_PICTURE_API);
			CameraManager.this.capturePhoto(System.currentTimeMillis() - 1000 * 2, false, null);
		}

		if ("setCameraTimeout".equals(command)) {
			if (data == null) {
				return null;
			}
			try {
				long mTimeout = Long.parseLong(new String(data));
				JNIHelper.logd("setCameraTimeout::" + mTimeout);
				CameraManager.this.mTimeOutCapturePhoto = mTimeout;
			} catch (Exception e) {
				e.printStackTrace();
				JNIHelper.logw("setCameraTimeout::" + e.toString());
			}
		}
		if ("setCaptureVideoTimeout".equals(command)) {
			try {
				long mTimeout = Long.parseLong(new String(data));
				JNIHelper.logd("setCaptureVideoTimeout=" + mTimeout);
				mCaptureVideoTimeout = mTimeout;
			} catch (Exception e) {
				e.printStackTrace();
				JNIHelper.logw("setCaptureVideoTimeout=" + e.toString());
			}
		}
		if ("setSupportCameraType".equals(command)) {
			try {
				int cameratype = Integer.parseInt(new String(data));
				JNIHelper.logd("setSupportCameraType=" + cameratype);
				mSupportCameraType = cameratype;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				JNIHelper.logw("setSupportCameraType=" + e.toString());
			}
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
							MonitorUtil.monitorCumulant(MonitorUtil.SDK_START_CAPTURE_PICTURE_ALL,MonitorUtil.SDK_START_CAPTURE_PICTURE_GLOBAL);
							CameraManager.this.capturePhoto(
									System.currentTimeMillis() - 1000 * 2,
									false, new Runnable() {
										@Override
										public void run() {
											addGlobalCaptureCommand();
										}
									});
						}
					};
				}.addCommand("TAKE_PHOTO", "我要拍照", "抓拍照片"));
	}

	private void clearGlobalCaptureVideoCommand() {
        WakeupManager.getInstance().recoverWakeupFromAsr("WAKEUP_CAPTURE_VIDEO");
    }

	private void addGlobalCaptureVideoCommand() {
        if (!mUseWakeupCaptureVideo || TextUtils.isEmpty(mRemoteCameraService)) {
            clearGlobalCaptureVideoCommand();
            return;
        }

        WakeupManager.getInstance().useWakeupAsAsr(
                new AsrUtil.AsrComplexSelectCallback() {
                    @Override
                    public boolean needAsrState() {
                        return false;
                    }

                    @Override
                    public String getTaskId() {
                        return "WAKEUP_CAPTURE_VIDEO";
                    }

                    @Override
                    public void onCommandSelected(final String type, final String command) {
                        if ("CAPTURE_VIDEO".equals(type)) {
                            captureVideoFromVoice();
                        }
                    }
                }.addCommand("CAPTURE_VIDEO", "抓拍视频", "录制视频")
        );
    }



	/**
	//O===[::::::::::::::::: 抓拍照片 :::::::::::::::::::::::::>

	/**
	 * 抓拍图片回调
	 * 
	 * @author txz
	 *
	 */
	public static abstract class CapturePictureListener {
		public static final int CAMERA_POSITION_FRONT = 1;
		public static final int CAMERA_POSITION_BACK = 2;
		
		long time = System.currentTimeMillis(); // 可用于抓拍超时处理

		/**
		 * 保存图片回调
		 * 
		 * @param path
		 */
		public abstract void onSave(String path, int position);

		/**
		 * 出错回调
		 */
		public abstract void onError(int errCode, String errDesc);

		/**
		 * 超时
		 */
		public abstract void onTimeout();
	}

	private int mSupportCameraType = 1; // 适配程序支持拍照摄像头数量
	private static final long CAPTURE_PHOTO_TIMEOUT = 10000L;
//	private int mCaptureRequestId = new Random().nextInt();
//	private Map<Integer, CapturePictureListener> mCaptureRequests = new HashMap<Integer, CapturePictureListener>();
	private long mTimeOutCapturePhoto = CAPTURE_PHOTO_TIMEOUT;
	private long mLastCapturePicturePushMid = -1; // 当前抓拍照片请求taskId，由后台传入更新
	private CapturePictureListener mCapturePictureListener = null;

	private void capturePhoto(long time, boolean shouldUploadWithError, Runnable runEnd) {
		capturePhoto(0L, time,shouldUploadWithError, runEnd);
	}
	
	private void capturePhoto(long mPushId, long time, boolean shouldUploadWithError, Runnable runEnd) {
		capturePhoto(mPushId, time,shouldUploadWithError, -1, runEnd);
	}
	
	private void capturePhoto(final long mPushId, long time, final boolean shouldUploadWithError, final int uploadType, final Runnable runEnd) {
		capturePicture(mPushId, time,
				new CapturePictureListener() {
					private void onEnd() {
						if (runEnd != null)
							runEnd.run();
						JNIHelper.logd("capturePicture onEnd");
					}

					@Override
					public void onSave(String path, int position) {
						JNIHelper.logd("capturePicture onSave, path=" + path);
						if (new File(path).exists() == false) {
							JNIHelper
									.logd("capturePicture file not exists, path="
											+ path);
							onError(7104, "请检查SD卡状态是否正常");
							return;
						}
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						String dirPath = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/txz/cache/";
						new File(dirPath).mkdirs();
						String upload_path = dirPath + position + "~upload.jpg";
						String thumb_path = dirPath + position + "~upload_thumb.jpg";
						String origin_path = dirPath + position + "~upload_origin.jpg";

						if (uploadType > 0) {
							// 图片上传类型，每一位表示一种图片类型（前置摄像头0：正常图片；1：缩略图；2：原图）（后置摄像头3：正常图片；4：缩略图；5：原图）
							switch (position) {
							case TXZCameraManager.CAMERA_FRONT:
								if ((uploadType & (1 << 0)) == 0) {
									upload_path = null;
								}
								if ((uploadType & (1 << 1)) == 0) {
									thumb_path = null;
								}
								if ((uploadType & (1 << 2)) == 0) {
									origin_path = null;
								}
								break;
							case TXZCameraManager.CAMERA_BACK:
								if ((uploadType & (1 << 3)) == 0) {
									upload_path = null;
								}
								if ((uploadType & (1 << 4)) == 0) {
									thumb_path = null;
								}
								if ((uploadType & (1 << 5)) == 0) {
									origin_path = null;
								}
								break;
							default:
								upload_path = null;
								thumb_path = null;
								origin_path = null;
								break;
							}
						}

						try {
							if (!TextUtils.isEmpty(upload_path)) {
								ImageUtil.resizeImage(path, upload_path, 0.5f,
										80);
							}
							if (!TextUtils.isEmpty(thumb_path)) {
								ImageUtil.resizeImageAlignWidth(path,
										thumb_path, 320, 50);
							}
							if (!TextUtils.isEmpty(origin_path)) {
								FileUtil.copyFile(path, origin_path);
							}
						} catch (Throwable e) {
							JNIHelper.loge("capturePicture resizeImage error["
									+ e.getClass() + "::" + e.getMessage()
									+ "]");
						}
						req.strOriginPicPath = origin_path;
						req.strPicPath = upload_path;
						req.strThumbPicPath = thumb_path;
						req.uint64PushMid = mPushId;
						req.uint32CameraPosition = position;
						req.uint32CameraType = mSupportCameraType;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);
						if (!shouldUploadWithError) {
							String spk = NativeData
									.getResString("RS_WX_CAPTURE_PICTURE");
							TtsManager.getInstance().speakText(spk);
						}

						if (WeixinManager.getInstance().isSharePhotoEnabled())
							WeixinManager.getInstance().startSharePhoto(
									upload_path);

						onEnd();
					}

					@Override
					public void onError(int errCode, String errDesc) {
						if (!shouldUploadWithError) {
							String spk = NativeData.getResPlaceholderString(
									"RS_WX_CAPTURE_ERROR", "%ERROR%", errDesc);
							TtsManager.getInstance().speakText(spk);
							onEnd();
							return;
						}
						JNIHelper.logw("capture error: " + errCode + "-"
								+ errDesc);
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						req.uint32ErrCode = errCode;
						req.uint64PushMid = mPushId;
						req.uint32CameraType = mSupportCameraType;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);

						onEnd();
					}

					@Override
					public void onTimeout() {
						if (!shouldUploadWithError) {
							String spk = NativeData
									.getResString("RS_WX_CAPTURE_FAIL");
							TtsManager.getInstance().speakText(spk);
							onEnd();
							return;
						}
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						req.uint32ErrCode = 7121;
						req.uint64PushMid = mPushId;
						req.uint32CameraType = mSupportCameraType;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);

						onEnd();
					}
				}, mTimeOutCapturePhoto);
	}
	
	private void capturePicture(final long mPushId, final long time,
			final CapturePictureListener listener, final long timeout) {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (TextUtils.isEmpty(mRemoteCameraService)) {
					// TtsManager.getInstance().speakText("没有设置抓拍工具");
					int errCode = 7103;
					String errDesc = "没有设置抓拍工具";
					listener.onError(errCode, errDesc);
					MonitorUtil.monitorCumulant(MonitorUtil.SDK_ERROR_CAPTURE_PICTURE_ALL,MonitorUtil.SDK_NOTSUPPORT_CAPTURE_PICTURE);
					mLastCapturePicturePushMid = -1L;
					return;
				}
				
				if (mLastCapturePicturePushMid > 0) {
					JNIHelper.logw("capture error: 正在抓取中");
					UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
					req.uint32ErrCode = EquipmentManager.EC_UPLOAD_PIC_CATCHING;
					req.uint64PushMid = mPushId;
					req.uint32CameraType = mSupportCameraType;
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
							UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);
					return;
				}
				mLastCapturePicturePushMid = mPushId;
				mCapturePictureListener = listener;
				
				try {
					JSONObject json = new JSONObject();
					json.put("time", time);
					json.put("taskId", mPushId);
//					json.put("id", mCaptureRequestId);
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
										MonitorUtil.monitorCumulant(MonitorUtil.SDK_ERROR_CAPTURE_PICTURE_ALL,MonitorUtil.SDK_UNKNOWN_CAPTURE_PICTURE);
										mLastCapturePicturePushMid = -1L;
										mCapturePictureListener = null;
									}
								}
							});
//					mCaptureRequests.put(mCaptureRequestId, listener);
					// 设置10秒超时
//					AppLogic.runOnBackGround(new Runnable1<Integer>(
//							mCaptureRequestId) {
					AppLogic.runOnBackGround(new Runnable1<Long>(
							mPushId) {
						@Override
						public void run() {
//							if (mCaptureRequests.containsKey(mP1)) {
//								mCaptureRequests.remove(mP1);
//								listener.onTimeout();
//							}
							if (mP1 == mLastCapturePicturePushMid) {
								CapturePictureListener l = mCapturePictureListener;
								if (l != null) {
									l.onTimeout();
								}
								mLastCapturePicturePushMid = -1L;
								mCapturePictureListener = null;
							}
						}
					}, timeout);
//					mCaptureRequestId++;
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
					mLastCapturePicturePushMid = -1L;
					mCapturePictureListener = null;
				}
			}
		}, 0);
	}
	
	private void reportCapturePictureSuccess(byte[] data) {
		CapturePictureListener l = mCapturePictureListener;
		try {
			JSONObject json = new JSONObject(new String(data));
			long pushId = json.getLong("taskId");
			if (mLastCapturePicturePushMid > 0 && mLastCapturePicturePushMid != pushId) {
				// 返回照片任务id与当前执行id不匹配
				// 可能是当前任务已反馈其他结果id重置, 成功回调不需要考虑重置问题
				return;
			}
			if (l != null)
				l.onSave(json.getString("path"), json.optInt("position", TXZCameraManager.CAMERA_FRONT));
		} catch (Exception e) {
			JNIHelper.loge("capturePicture data="
					+ (data == null ? "null" : new String(data))
					+ ", error[" + e.getClass() + "::"
					+ e.getMessage() + "]");
			if (l != null) {
				int errCode = 7101;
				String errDesc = NativeData
						.getResString("RS_CAMERA_CAPTURE_ERROR");
				l.onError(errCode, errDesc);
			}
		}
		mLastCapturePicturePushMid = -1L;
		// 成功时当前任务监听回调不能置空，需要考虑到前后摄像头问题
//		mCapturePictureListener = null;
	}
	
	private void reportCapturePictureError(byte[] data) {
		CapturePictureListener l = mCapturePictureListener;
		try {
			JSONObject json = new JSONObject(new String(data));
			long pushId = json.getLong("taskId");
			if (mLastCapturePicturePushMid != pushId) {
				// 返回照片任务id与当前执行id不匹配
				// 可能是当前任务已反馈其他结果id重置
				return;
			}
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
					+ (data == null ? "null" : new String(data))
					+ ", error[" + e.getClass() + "::"
					+ e.getMessage() + "]");
			if (l != null) {
				int errCode = 7101;
				String errDesc = NativeData
						.getResString("RS_CAMERA_CAPTURE_ERROR");
				l.onError(errCode, errDesc);
			}
		}
		mLastCapturePicturePushMid = -1L;
		mCapturePictureListener = null;
	}

	// /////////////////////////////////////////////////////////////////////////
	// 抓拍视频
    private static final String LOG_TAG_VIDEO = "captureVideo::";
	private static final long CAPTURE_VIDEO_TIMEOUT = 30000L;
	private static final long CAPTURE_VIDEO_NO_TASK = -1;
	private long mCaptureVideoTimeout = CAPTURE_VIDEO_TIMEOUT;
	private long mLastCaptureVideoPushMid = -1; // 当前抓拍视频请求taskId，由后台传入更新
	private boolean mIsFrontReported = false;
	private boolean mIsBackReported = false;

	private Runnable mCaptureVideoTimeoutTask = new Runnable() {

		@Override
		public void run() {
			long taskId = mLastCaptureVideoPushMid;
			mLastCaptureVideoPushMid = CAPTURE_VIDEO_NO_TASK;

			// 前后录都超时, 额外上报
			if (!mIsFrontReported && !mIsBackReported) {
				monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_TIMEOUT_ALL);
			}

			if (!mIsFrontReported) {
				logCaptureVideo("front camera timeout");
				reportCaptureVideoError(taskId,
						UiEquipment.EC_UPLOAD_VIDEO_CATCH_TIMEOUT, "抓拍超时",
						TXZCameraManager.CAMERA_FRONT);
				monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_TIMEOUT_FRONT);
			}

			if (!mIsBackReported) {
                logCaptureVideo("back camera timeout");
				reportCaptureVideoError(taskId,
						UiEquipment.EC_UPLOAD_VIDEO_CATCH_TIMEOUT, "抓拍超时",
						TXZCameraManager.CAMERA_BACK);
				monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_TIMEOUT_BACK);
			}
		}
	};

	private void captureVideoFromVoice() {
	    if (mLastCaptureVideoPushMid != CAPTURE_VIDEO_NO_TASK) {
	        // 提示已在抓拍中
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_WX_CAPTURE_VIDEO_CAPTURING"),
                    null);
            logCaptureVideo("captureVideoFromVoice: already capturing");
            return;
        }

        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.speakTextWithClose(NativeData.getResString("RS_WX_CAPTURE_VIDEO"),
                new Runnable() {
                    @Override
                    public void run() {
                        captureVideo(0L);
                    }
                });
        monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_ENTER_VOICE);
    }

	private void captureVideo(byte[] data) {
		try {
            UiEquipment.Notify_UploadVideo param = UiEquipment.Notify_UploadVideo
                    .parseFrom(data);
            captureVideo(param.uint64PushMid);
            monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_ENTER_PUSH);
        } catch (Exception e) {
            logCaptureVideo("captureVideo: error parsing push param: " + e.toString());
        }
	}

	private AtomicBoolean aBVideoUploadNotified = new AtomicBoolean(false);

	private void captureVideo(final long pushMid) {
		try {
			AppLogic.removeBackGroundCallback(mCaptureVideoTimeoutTask);

			mLastCaptureVideoPushMid = pushMid;
			mIsFrontReported = false;
			mIsBackReported = false;
			aBVideoUploadNotified.getAndSet(false);

			if (TextUtils.isEmpty(mRemoteCameraService)) {
				// TtsManager.getInstance().speakText("没有设置抓拍工具");
				logCaptureVideo("captureVideo failed, remote CameraTool is empty");
				int errCode = UiEquipment.EC_UPLOAD_VIDEO_NO_SUPPORT;
				String errDesc = NativeData.getResString("RS_CAMERA_NO_TOOL");
				reportCaptureVideoError(pushMid, errCode, errDesc);
				return;
			}

			JSONObject json = new JSONObject();
			json.put(TXZCameraManager.REMOTE_NAME_TASK_ID, pushMid);
			ServiceManager.getInstance().sendInvoke(mRemoteCameraService,
					"tool.camera.captureVideo", json.toString().getBytes(),
					new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							if (data == null) {
								int errCode = UiEquipment.EC_UPLOAD_VIDEO_UNKNOWN;
								String errDesc = NativeData
										.getResString("RS_CAMERA_RESPONSE_ERROR");
								reportCaptureVideoError(pushMid, errCode,
                                        errDesc);
							}
						}
					});

			AppLogic.runOnBackGround(mCaptureVideoTimeoutTask, mCaptureVideoTimeout);

		} catch (Exception e) {
			logCaptureVideo("captureVideo: error: " + e.toString());
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

				long lastId = mLastCaptureVideoPushMid;
                updateVideoReportFlag(position);

				if (!WeixinManager.getInstance().hasBind()) {
					logCaptureVideo("captureVideo upload skipped(no bind)");
					monitorCaptureVideo(MonitorUtil.VIDEO_CAPTURE_UPLOAD_NOT_BIND);
					return;
				}

				logCaptureVideo("reportCaptureVideoSuccess, lastId = "
						+ lastId);
                if (0 == lastId) {
                    if (!RecorderWin.isOpened()) {
                        if (aBVideoUploadNotified.compareAndSet(false, true)) {
                            TtsManager.getInstance().speakText(
                                    NativeData.getResString("RS_WX_CAPTURE_VIDEO_UPLOAD_NOTIFY"));
                        }
                    }
                }

                // 上传视频
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

				monitorCaptureVideo(TXZCameraManager.CAMERA_FRONT == position ?
						MonitorUtil.VIDEO_CAPTURE_UPLOAD_ENTER_FRONT
						: MonitorUtil.VIDEO_CAPTURE_UPLOAD_ENTER_BACK);
			} else {
			    logCaptureVideo(String.format("reportCaptureVideoSuccess for task: %s skipped, " +
                        "current task = %s", taskId, mLastCapturePicturePushMid));
            }

		} catch (Exception e) {
            logCaptureVideo("reportCaptureVideoSuccess encountered error: " + e.toString());
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
			} else {
                logCaptureVideo(String.format("reportCaptureVideoError for task: %s skipped, " +
                        "current task = %s", taskId, mLastCapturePicturePushMid));
            }
		} catch (Exception e) {
            logCaptureVideo("reportCaptureVideoError encountered error: " + e.toString());
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
		req.uint64PushMid = taskId;
		req.uint32Ground = cameraPosition;

		updateVideoReportFlag(cameraPosition);

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

	private void updateVideoReportFlag(int position) {
		if (position == TXZCameraManager.CAMERA_FRONT) {
			mIsFrontReported = true;
		}

		if (position == TXZCameraManager.CAMERA_BACK) {
			mIsBackReported = true;
		}

		if (mIsFrontReported && mIsBackReported) {
		    // 处理完毕, 重置标志位
            AppLogic.removeBackGroundCallback(mCaptureVideoTimeoutTask);
            mLastCaptureVideoPushMid = CAPTURE_VIDEO_NO_TASK;
        }
	}

	// logger for video
    private void logCaptureVideo(String msg) {
	    //Log.i(LOG_TAG_VIDEO, msg);
        JNIHelper.logd(LOG_TAG_VIDEO + msg);
    }

    private void monitorCaptureVideo(String msg) {
		MonitorUtil.monitorCumulant(msg);
		logCaptureVideo("monitor::" + msg);
	}
}
