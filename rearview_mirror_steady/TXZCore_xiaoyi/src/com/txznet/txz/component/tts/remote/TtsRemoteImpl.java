package com.txznet.txz.component.tts.remote;

import java.util.Locale;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;

public class TtsRemoteImpl implements ITts {
	private static String mRemoteTtsService = null;
	private static int mSessionId = 0;
	private static boolean mIsBusy = false;

	public static void setRemoteTtsService(String serviceName) {
		synchronized (TtsRemoteImpl.class) {
			mRemoteTtsService = serviceName;

			JNIHelper.logd("update remote tts service: " + mRemoteTtsService);

			ServiceManager.getInstance().sendInvoke(mRemoteTtsService, "",
					null, null);
		}
	}
	
	public static boolean useRemoteTtsTool() {
		return !TextUtils.isEmpty(mRemoteTtsService);
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		synchronized (TtsRemoteImpl.class) {
			ServiceManager.getInstance().sendInvoke(mRemoteTtsService,
					"tool.tts.init", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							oRun.onInit(data != null);
						}
					});
		}
		return 0;
	}

	@Override
	public void release() {
	}

	static ITtsCallback mITtsCallback = null;

	private static void async_processRmtResponse(final String command,
			final byte[] data) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				synchronized (TtsRemoteImpl.class) {
					if (data == null) {
						return;
					}
					JSONBuilder json = new JSONBuilder(data);
					if (json.getVal("id", Integer.class, mSessionId + 1) != mSessionId) {
						return;
					}

					if (command.equals("onSuccess")) {
						mIsBusy = false;
						ITtsCallback cb = mITtsCallback;
						mITtsCallback = null;
						if (cb != null) {
							cb.onSuccess();
						}
						return;
					}
					if (command.equals("onError")) {
						mIsBusy = false;
						ITtsCallback cb = mITtsCallback;
						mITtsCallback = null;
						if (cb != null) {
							cb.onError(ITts.ERROR_UNKNOW);
						}
						return;
					}
					if (command.equals("onCancel")) {
						mIsBusy = false;
						ITtsCallback cb = mITtsCallback;
						mITtsCallback = null;
						if (cb != null) {
							cb.onCancel();
						}
						return;
					}
				}
			}
		};
		//切到和TTSManager同一个线程工作，避免mCurrTask空指异常
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	public static byte[] procRemoteResponse(String serviceName, String command,
			byte[] data) {
		synchronized (TtsRemoteImpl.class) {
			if ("setTool".equals(command)) {
				setRemoteTtsService(serviceName);
				return null;
			}
			
			if ("clearTool".equals(command)) {
				setRemoteTtsService(null);
				return null;
			}

			if (!serviceName.equals(mRemoteTtsService))
				return null;
			if (null == mITtsCallback)
				return null;
			async_processRmtResponse(command, data);
			return null;
		}
	}
	
	

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		mIsBusy = true;
		JSONBuilder json = new JSONBuilder();
		synchronized (TtsRemoteImpl.class) {
			mITtsCallback = oRun;
			json.put("id", ++mSessionId);
		}
		json.put("stream", iStream);
		json.put("text", sText);
		
		ServiceManager.getInstance().addConnectionListener(new ConnectionListener() {
			int session = mSessionId;
			@Override
			public void onDisconnected(String serviceName) {
				ServiceManager.getInstance().removeConnectionListener(this);
				
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						ITtsCallback cb = mITtsCallback;
						mITtsCallback = null;
						if (cb != null) {
							cb.onError(ITts.ERROR_UNKNOW);
						}
					}
				}, 0);
			}
			
			@Override
			public void onConnected(String serviceName) {
			}
		});
		
		ServiceManager.getInstance().sendInvoke(mRemoteTtsService,
				"tool.tts.start", json.toBytes(), new GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data == null) {
							ITtsCallback cb;
							synchronized (TtsRemoteImpl.class) {
								mIsBusy = false;
								cb = mITtsCallback;
								mITtsCallback = null;
							}
							//切到和TTSManager同一个线程工作，避免mCurrTask空指异常
							final ITtsCallback async_cb = cb;
							Runnable oRun = new Runnable() {
								@Override
								public void run() {
									if (async_cb != null) {
										async_cb.onError(ITts.ERROR_UNKNOW);
									}
								}
							};
							AppLogic.runOnBackGround(oRun, 0);
						}
					}
				});

		return ERROR_SUCCESS;
	}

	@Override
	public int pause() {
		return ERROR_UNKNOW;
	}

	@Override
	public int resume() {
		return ERROR_UNKNOW;
	}

	@Override
	public void stop() {
		synchronized (TtsRemoteImpl.class) {
			ServiceManager.getInstance().sendInvoke(mRemoteTtsService,
					"tool.tts.cancel", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							ITtsCallback cb;
							synchronized (TtsRemoteImpl.class) {
								mIsBusy = false;
								cb = mITtsCallback;
								mITtsCallback = null;
							}
							//切到和TTSManager同一个线程工作，避免mCurrTask空指异常
							final ITtsCallback async_cb = cb;
							Runnable oRun = new Runnable() {
								@Override
								public void run() {
									if (async_cb != null) {
										async_cb.onCancel();
									}
								}
							};
							AppLogic.runOnBackGround(oRun, 0);
						}
					});
		}
	}

	@Override
	public boolean isBusy() {
		return mIsBusy;
	}

	@Override
	public int setLanguage(Locale loc) {
		return 0;
	}

	@Override
	public void setTtsModel(String ttsModel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setVoiceSpeed(int speed) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getVoiceSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}
}
