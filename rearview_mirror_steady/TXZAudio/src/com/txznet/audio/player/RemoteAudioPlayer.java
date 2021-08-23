package com.txznet.audio.player;

import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

public class RemoteAudioPlayer extends TXZAudioPlayer {
	private static final String TAG = "[MUSIC][remote]";
	private static RemoteAudioPlayer mLastPlayerRef;
	private static IAudioPlayer mPlayer;
	private static Audio mAudio;
	private static HandlerThread handlerThread = new HandlerThread(
			"remotePlayer");
	private static Handler handler = null;
	static {
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case RELEASECODE:// release
					if (mPlayer != null) {
						try {
							mPlayer.release(session_id);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				case CREATECODE:

					break;
				}
			};
		};
	}
	private final static int RELEASECODE = 1;
	private final static int CREATECODE = 2;
	// ------------------------------------

	private boolean mPrepared = false;
	boolean prepared = false;
	private static int session_id;
	private static int service_id;

	private static ServiceConnection mConn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtil.logd(TAG+" onServiceDisconnected");
			mPlayer = null;
			service_id = -1;
			onRemoteError();
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.logd(TAG+" onServiceConnected");
			mPlayer = IAudioPlayer.Stub.asInterface(service);
			service_id = Utils.getProcessIdByPkgName("com.txznet.music:player");
		}
	};

	public synchronized static TXZAudioPlayer createAudioPlayer(Audio audio) {
		mAudio = audio;
		LogUtil.logd(Constant.SPENDTIME+"create remote player begin");
		if (null != mPlayer) {// 如果绑定成功
			try {
				mLastPlayerRef=new RemoteAudioPlayer();
				LogUtil.logd(TAG+"create player success"+mLastPlayerRef);
				return mLastPlayerRef;
//				return new RemoteAudioPlayer();
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.loge("[Audio][RemoteAudioPlayer]:createAudioPlayer:",e);
			}finally{
				LogUtil.logd(Constant.SPENDTIME+"create remote player end");
			}
		} else {
			initRemotePlayer();
		}
		return null;
	}

	private RemoteAudioPlayer() throws Exception {
		super(null, AudioManager.STREAM_MUSIC);
		// 判断是否绑定
//		this.mOnBufferingUpdateListenerSet=null;
		session_id = mPlayer.createAudioPlayer(JsonHelper.toJson(mAudio)
				.getBytes(), Process.myPid(), DataInterfaceBroadcastHelper
				.getRandomKey(), RemoteAudioPlayer.this.hashCode());
	}

	private static void onRemoteError() {
		MediaError error = new MediaError(MediaError.ERR_REMOTE,
				"remote service have been  kelled", "远程服务挂掉");
		if (mLastPlayerRef!= null) {
			invokeRemotePlayerCallback(
					"",
					"onError",
					new JSONBuilder()
					.put("sid", mLastPlayerRef.hashCode())
					.put("err", JsonHelper.toJson(error))
					.toBytes());
		}
//		LogUtil.logd(TAG+"player is null");
//		mLastPlayerRef = null;
		initRemotePlayer();
	}

	private static Intent mLastIntent;

	public static void initRemotePlayer() {
		mLastIntent = new Intent(GlobalContext.get(), AudioPlayerService.class);
		// AppLogic.runOnUiGround(mTimeoutRebindTask, 4000); // 超时没有连上则重连
		GlobalContext.get().bindService(mLastIntent, mConn,
				Service.BIND_AUTO_CREATE);
	}

	private void notifyAidlError() {
		MediaError err = new MediaError(MediaError.ERR_REMOTE, "remote io err",
				"媒体服务发生异常");
		notifyError(err);
	}

	private void notifyInvalidStateError() {
		MediaError err = new MediaError(MediaError.ERR_SYS_PLAYER,
				" calling this method in an invalid state", "媒体服务发生异常");
		notifyError(err);
	}

	private void notifyNullStateError() {
		MediaError err = new MediaError(MediaError.ERR_NULL_STATE,
				" calling this method in an null object", "空指针发生");
		notifyError(err);
	}

	@Override
	public int getDuration() {
		if (mPlayer == null || !mPrepared) {
			return 0;
		}
		try {
			return mPlayer.getDuration(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return 0;
	}

	@Override
	public float getPlayPercent() {
		if (mPlayer == null) {
			return 0;
		}
		try {
			return mPlayer.getPlayPercent(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return 0;
	}

	@Override
	public float getBufferingPercent() {
		if (mPlayer == null) {
			return 0;
		}
		try {
			return mPlayer.getBufferingPercent(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return 0;
	}

	@Override
	public boolean isPlaying() {
		if (mPlayer == null) {
			return false;
		}
		try {
			return mPlayer.isPlaying(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return false;
	}

	@Override
	public boolean isBuffering() {
		if (mPlayer == null) {
			return false;
		}
		try {
			return mPlayer.isBuffering(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return false;
	}

	@Override
	public boolean needMoreData() {
		if (mPlayer == null) {
			return false;
		}
		try {
			return mPlayer.needMoreData(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return false;
	}

	@Override
	public long getDataPieceSize() {
		if (mPlayer == null) {
			return 0;
		}
		try {
			return mPlayer.getDataPieceSize(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
		return 0;
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		if (mPlayer == null) {
			return;
		}
		try {
			mPlayer.setVolume(session_id, leftVolume, rightVolume);
		} catch (Exception e) {
			notifyAidlError();
		}
	}

	@Override
	public void prepareAsync() {
		try {
			mPlayer.prepareAsync(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
	}

	@Override
	public void start() {
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "SESSION_" + RemoteAudioPlayer.this.hashCode()
					+ "_" + (mPlayer == null ? "null" : mPlayer.hashCode())
					+ "::start");
		}
		try {
			mPlayer.start(session_id);
		} catch (RemoteException e) {
			notifyAidlError();
		} catch (Exception e) {
			notifyNullStateError();
		}
	}

	@Override
	public void pause() {
		if (null == mPlayer) {
			return;
		}
		try {
			mPlayer.pause(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
	}

	@Override
	public void stop() {
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "SESSION_" + RemoteAudioPlayer.this.hashCode()
					+ "_" + (mPlayer == null ? "null" : mPlayer.hashCode())
					+ "::stop");
		}
		try {
			mPlayer.stop(session_id);
		} catch (Exception e) {
			notifyAidlError();
		}
	}

	@Override
	public void seekTo(float percent) {
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "SESSION_" + RemoteAudioPlayer.this.hashCode()
					+ "_" + (mPlayer == null ? "null" : mPlayer.hashCode())
					+ "::seekTo");
		}
		try {
			mPlayer.seekTo(session_id, percent);
		} catch (Exception e) {
			notifyAidlError();
		}
	}

	private static boolean isBlock = false;// 是否阻塞

	static Runnable checkRunnable = new Runnable() {

		@Override
		public void run() {
//			ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);  
			int processIdByPkgName = Utils.getProcessIdByPkgName("com.txznet.music:player");
			LogUtil.logd(TAG+"XXX"+processIdByPkgName);
			android.os.Process.killProcess(processIdByPkgName);
//			manager.killBackgroundProcesses("com.txznet.sdkdemo"); 
//			GlobalContext.get().unbindService(mConn);
			isBlock = true;
		}
	};

	@Override
	public synchronized void release() {
			LogUtil.logd(TAG + "SESSION_" + RemoteAudioPlayer.this.hashCode()
					+ "_" + (mPlayer == null ? "null" : mPlayer.hashCode())
					+ "::release");
		// handler.sendEmptyMessage(RELEASECODE);
		if (mPlayer != null) {
			LogUtil.logd("service_id=" + service_id + "/"
					+ Utils.getProcessIdByPkgName("com.txznet.music:player"));
			if (service_id == Utils
					.getProcessIdByPkgName("com.txznet.music:player")) {
				try {
					isBlock = false;
					AppLogic.runOnSlowGround(checkRunnable, 1000);
					mPlayer.release(session_id);
					AppLogic.removeSlowGroundCallback(checkRunnable);
					// 开启一个守护线程
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				mPlayer = null;
			}
		}
		LogUtil.logd(TAG+"release player is null");
		mLastPlayerRef = null;
		mOnPreparedListener = null;
		mOnBufferingUpdateListenerSet = null;
		mOnCompletionListener = null;
		mOnErrorListenerSet = null;
		mOnPlayProgressListener = null;
		mOnSeekCompleteListener = null;
	}

	private OnPreparedListener mOnPreparedListener;

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		this.mOnPreparedListener = listener;
	}

	private OnCompletionListener mOnCompletionListener;

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		this.mOnCompletionListener = listener;
	}

	private OnSeekCompleteListener mOnSeekCompleteListener;

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		this.mOnSeekCompleteListener = listener;
	}

	private OnPlayProgressListener mOnPlayProgressListener;

	@Override
	public void setOnPlayProgressListener(OnPlayProgressListener listener) {
		this.mOnPlayProgressListener = listener;
	}

	public static byte[] invokeRemotePlayerCallback(String packageName,
			String command, byte[] data) {
		RemoteAudioPlayer player = mLastPlayerRef;
		if (player == null) {
			LogUtil.logd(TAG+"player:"+mPlayer+"/"+mLastPlayerRef);
			LogUtil.logd(TAG+"SESSION_" + command + "_is null");
			return null;
		}
		try {
			if (command.equals("onDownloading")) {
				if (player.mOnBufferingUpdateListenerSet != null) {
					JSONBuilder doc = new JSONBuilder(data);
					int sid = doc.getVal("sid", Integer.class);
					if (sid == player.hashCode()) {
						player.mOnBufferingUpdateListenerSet.onDownloading(
								player,
								(List<LocalBuffer>) JsonHelper.toObject(
										doc.getVal("buffers", String.class),
										new TypeToken<List<LocalBuffer>>() {
										}.getType()));
						// player, JSON.parseArray(
						// doc.getVal("buffers", String.class),
						// LocalBuffer.class));
					}
				}
			} else if (command.equals("onBufferingUpdate")) {
				if (player.mOnBufferingUpdateListenerSet != null) {
					JSONBuilder doc = new JSONBuilder(data);
					int sid = doc.getVal("sid", Integer.class);
					if (sid == player.hashCode()) {
						player.mOnBufferingUpdateListenerSet.onBufferingUpdate(
								player, doc.getVal("percent", Float.class));
					}
				}
			} else if (command.equals("onCompletion")) {
				if (player.mOnCompletionListener != null) {
					int sid = Integer.parseInt(new String(data));
					if (sid == player.hashCode()) {
						player.mOnCompletionListener.onCompletion(player);
						if (Constant.ISTEST) {
							LogUtil.logd(TAG
									+ "SESSION_"
									+ "null"
									+ "_"
									+ (player == null ? "null" : player
											.hashCode()
											+ "::Callback::onCompletion"));
						}
					}
				}
			} else if (command.equals("onError")) {
				if (player.mOnErrorListenerSet != null) {
					JSONBuilder doc = new JSONBuilder(data);
					int sid = doc.getVal("sid", Integer.class);
					if (sid == player.hashCode()) {
						MediaError err = JsonHelper.toObject(MediaError.class,
								doc.getVal("err", String.class));
						if (!isBlock) {// 如果不是阻塞的bug，就放过
							player.mOnErrorListenerSet.onError(player, err);
						} else {
							isBlock = false;
						}
						if (Constant.ISTEST) {
							LogUtil.logd(TAG
									+ "SESSION_"
									+ "null"
									+ "_"
									+ (player == null ? "null" : player
											.hashCode()
											+ "::Callback::onError["
											+ err
											+ "]")+"_"+isBlock);
						}
					}
				}
			} else if (command.equals("onPlayProgress")) {
				if (player.mOnPlayProgressListener != null) {
					JSONBuilder doc = new JSONBuilder(data);
					int sid = doc.getVal("sid", Integer.class);
					if (sid == player.hashCode()) {
						player.mOnPlayProgressListener.onPlayProgress(player,
								doc.getVal("percent", Float.class));
					}
				}
			} else if (command.equals("onPrepared")) {
				if (player.mOnPreparedListener != null) {
					int sid = Integer.parseInt(new String(data));
					if (sid == player.hashCode()) {
						player.mPrepared = true;
						player.mOnPreparedListener.onPrepared(player);
						if (Constant.ISTEST) {
							LogUtil.logd(TAG
									+ "SESSION_"
									+ "null"
									+ "_"
									+ (player == null ? "null" : player
											.hashCode()
											+ "::Callback::onPrepared"));
						}
					}
				}
			} else if (command.equals("onSeekComplete")) {
				if (player.mOnSeekCompleteListener != null) {
					int sid = Integer.parseInt(new String(data));
					if (sid == player.hashCode()) {
						player.mOnSeekCompleteListener.onSeekComplete(player);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void forceNeedMoreData(boolean isForce) {
		try {
			mPlayer.forceNeedMoreData(isForce);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
