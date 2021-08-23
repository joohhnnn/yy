package com.txznet.audio.player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.TXZAudioPlayer.OnBufferingUpdateListener;
import com.txznet.audio.player.TXZAudioPlayer.OnCompletionListener;
import com.txznet.audio.player.TXZAudioPlayer.OnErrorListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPlayProgressListener;
import com.txznet.audio.player.TXZAudioPlayer.OnPreparedListener;
import com.txznet.audio.player.TXZAudioPlayer.OnSeekCompleteListener;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.factory.PlayAudioFactory;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;

public class AudioPlayerService extends Service {
	private static final String TAG = "[MUSIC][Player]";
	public static class AudioPlayerBinder extends IAudioPlayer.Stub {
		private TXZAudioPlayer mAudioPlayer;
		private int mSid;

		public AudioPlayerBinder() {
		}

		@Override
		public int getDuration(int sid) throws RemoteException {
			return getPlayer(sid).getDuration();
		}

		@Override
		public float getPlayPercent(int sid) throws RemoteException {
			return getPlayer(sid).getPlayPercent();
		}

		@Override
		public float getBufferingPercent(int sid) throws RemoteException {
			return getPlayer(sid).getBufferingPercent();
		}

		@Override
		public boolean isPlaying(int sid) throws RemoteException {
			return getPlayer(sid).isPlaying();
		}

		@Override
		public boolean isBuffering(int sid) throws RemoteException {
			return getPlayer(sid).isBuffering();
		}

		@Override
		public boolean needMoreData(int sid) throws RemoteException {
			return getPlayer(sid).needMoreData();
		}

		@Override
		public long getDataPieceSize(int sid) throws RemoteException {
			return getPlayer(sid).getDataPieceSize();
		}

		@Override
		public void setVolume(int sid, float leftVol, float rightVol)
				throws RemoteException {
			if (getPlayer(sid) != null) {
				getPlayer(sid).setVolume(leftVol, rightVol);
			}
		}

		@Override
		public void prepareAsync(int sid) throws RemoteException {
			LogUtil.logd(TAG+"[Server]SERVICE_SESSION_PREPAREASYNC(" + sid + ")");
			if (getPlayer(sid) != null) {
				getPlayer(sid).prepareAsync();
			}else{
				byte[] data = null;
				try {
					MediaError err=new MediaError(MediaError.ERR_GET_AUDIO, "重新加载中...", "获取Audio失败");
					data = new JSONBuilder().put("sid", sid)
							.put("err", JsonHelper.toJson(err)).toBytes();
				} catch (Exception e) {
				}
				ServiceManager.getInstance().sendInvoke(
						ServiceManager.MUSIC,
						"music.remote.callback.onError", data, null);
			}
		}

		@Override
		public void start(int sid) throws RemoteException {
			LogUtil.logd(TAG+"[Server]SERVICE_SESSION_START(" + sid + ")");
			if (getPlayer(sid) != null) {
				LogUtil.logd(TAG+"[Server]SERVICE_SESSION_START(" + sid + ")!=null");
				getPlayer(sid).start();
			}else{
				LogUtil.logd(TAG+"[Server]SERVICE_SESSION_START(" + sid + ") is null");
				byte[] data = null;
				try {
					MediaError err=new MediaError(MediaError.ERR_GET_AUDIO, "重新加载中...", "获取Audio失败");
					data = new JSONBuilder().put("sid", sid)
							.put("err", JsonHelper.toJson(err)).toBytes();
				} catch (Exception e) {
				}
				ServiceManager.getInstance().sendInvoke(
						ServiceManager.MUSIC,
						"music.remote.callback.onError", data, null);
			}
		}

		@Override
		public void pause(int sid) throws RemoteException {
			LogUtil.logd(TAG+"[Server]SERVICE_SESSION_PAUSE(" + sid + ")");
			if (getPlayer(sid) != null) {
				getPlayer(sid).pause();
			}
		}

		@Override
		public void stop(int sid) throws RemoteException {
			if (getPlayer(sid) != null) {
				getPlayer(sid).stop();
			}
		}

		@Override
		public void seekTo(int sid, float percent) throws RemoteException {
			if (getPlayer(sid) != null) {
				getPlayer(sid).seekTo(percent);
			}
		}

		@Override
		public void release(int sid) throws RemoteException {
			LogUtil.logd(TAG+"enter release" +SystemClock.currentThreadTimeMillis());
			releasePlayer(sid);
			LogUtil.logd(TAG+"out release" +SystemClock.currentThreadTimeMillis());
		}

		@Override
		public int createAudioPlayer(byte[] audioBytes, int pid, byte[] key,
				int sid) throws RemoteException {
			// int sid = intent.getIntExtra("sid", 0);
			// int pid = intent.getIntExtra("pid", 0);
			LogUtil.logd(Constant.SPENDTIME+"create player begin");
			Audio currentAudio = JsonHelper.toObject(Audio.class, new String(
					audioBytes));
			// 比较客户端进程是否发生改变
			// 开启线程判断主进程是否存在与以前的比较
			int cur_pid = getProcessIdByPkgName(ServiceManager.MUSIC);
			if (cur_pid != -1) {
				if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
					killSelf();
					return sid;
				} else {
					AppLogic.removeSlowGroundCallback(checkMainThread);
					AppLogic.runOnSlowGround(checkMainThread, 2000);
				}
			}

			mLastClientPid = pid;
			DataInterfaceBroadcastHelper.setRandomKey(pid, key);
			mSid = sid;
			PlayerAudio playAudio = PlayAudioFactory
					.createPlayAudio(currentAudio);
			mAudioPlayer = createPlayer(sid, playAudio);
			LogUtil.logd(Constant.SPENDTIME+"create player end");
			LogUtil.logd(TAG+"[Server]REMOTTE_SESSION create " + playAudio.getAudioName());
			if (Constant.ISTEST) {
				LogUtil.logd(TAG+"[Server]REMOTTE_SESSION playurl " + currentAudio.getStrProcessingUrl());
			}
			mAudioPlayer
					.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
						@Override
						public void onDownloading(TXZAudioPlayer ap,
								List<LocalBuffer> buffers) {
							byte[] data = null;
							try {
								data = new JSONBuilder()
										.put("sid", mSid)
										.put("buffers",
												JsonHelper.toJson(buffers))
										.toBytes();
							} catch (Exception e) {
							}
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.remote.callback.onDownloading",
									data, null);
						}

						@Override
						public void onBufferingUpdate(TXZAudioPlayer ap,
								float percent) {
							byte[] data = null;
							try {
								data = new JSONBuilder().put("sid", mSid)
										.put("percent", percent).toBytes();
							} catch (Exception e) {
							}
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.remote.callback.onBufferingUpdate",
									data, null);
						}
					});
			mAudioPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(TXZAudioPlayer ap) {
					LogUtil.logd(TAG + "onCompletion:" + mAudioPlayer.hashCode());
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC,
							"music.remote.callback.onCompletion",
							("" + mSid).getBytes(), null);
				}
			});
			mAudioPlayer.setOnErrorListener(new OnErrorListener() {
				@Override
				public boolean onError(TXZAudioPlayer ap, MediaError err) {
					byte[] data = null;
					try {
						data = new JSONBuilder().put("sid", mSid)
								.put("err", JsonHelper.toJson(err)).toBytes();
					} catch (Exception e) {
					}
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC,
							"music.remote.callback.onError", data, null);
					return false;
				}
			});
			mAudioPlayer
					.setOnPlayProgressListener(new OnPlayProgressListener() {
						@Override
						public boolean onPlayProgress(TXZAudioPlayer ap,
								float percent) {
							byte[] data = null;
							try {
								data = new JSONBuilder().put("sid", mSid)
										.put("percent", percent).toBytes();
							} catch (Exception e) {
							}
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.remote.callback.onPlayProgress",
									data, null);
							return false;
						}
					});
			mAudioPlayer.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(TXZAudioPlayer ap) {
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.MUSIC,
							"music.remote.callback.onPrepared",
							("" + mSid).getBytes(), null);
				}
			});
			mAudioPlayer
					.setOnSeekCompleteListener(new OnSeekCompleteListener() {
						@Override
						public void onSeekComplete(TXZAudioPlayer ap) {
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.remote.callback.onSeekComplete",
									("" + mSid).getBytes(), null);
						}
					});
			return mSid;
		}

		@Override
		public void forceNeedMoreData(boolean isForce) throws RemoteException {
			LogUtil.logd(TAG+"enter forceNeedMoreData" +SystemClock.currentThreadTimeMillis());
			if (null!=mAudioPlayer) {
				mAudioPlayer.forceNeedMoreData(isForce);
			}
			LogUtil.logd(TAG+"out forceNeedMoreData" +SystemClock.currentThreadTimeMillis());
		}
	}



	AudioPlayerBinder mBinder = null;
	private static Integer mLastClientPid = null;

	@Override
	public IBinder onBind(Intent intent) {
		LogUtil.logd(TAG+"[Server]REMOTTE_SESSION bind " + intent);
		try {
			mBinder = new AudioPlayerBinder();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogUtil.logd(TAG+"[Server]REMOTTE_SESSION unbind " + intent);
		// if (mBinder != null) {
		// try {
		// mBinder.release();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		return super.onUnbind(intent);
	}

	static Map<Integer, TXZAudioPlayer> mAudioPlayers = new HashMap<Integer, TXZAudioPlayer>();

	private static TXZAudioPlayer createPlayer(int sessionID, PlayerAudio audio) {
		TXZAudioPlayer audioPlayer = mAudioPlayers.get(sessionID);
		if (null == audioPlayer) {
			audioPlayer = SessionManager.getInstance().createPlayer(audio);
			mAudioPlayers.put(sessionID, audioPlayer);
		}
		LogUtil.logd(TAG+"[Server]SERVICE_SESSION_CREATE(" + sessionID + ")");
		return audioPlayer;
	}

	private static void releasePlayer(int sessionID) {
		TXZAudioPlayer audioPlayer = mAudioPlayers.remove(sessionID);
		if (audioPlayer != null) {
			LogUtil.logd(TAG+"begin  release");
			audioPlayer.release();
		}
		LogUtil.logd(TAG+"[Server]SERVICE_SESSION_RELEASE(" + sessionID + ")");
	}

	public static TXZAudioPlayer getPlayer(int sid) {
		TXZAudioPlayer audioPlayer = mAudioPlayers.get(sid);
		if (audioPlayer == null) {
			LogUtil.loge("AudioPlayerService is null");
			// throw exception;
		}
		return audioPlayer;
	}

	private static int getProcessIdByPkgName(String pkgName) {
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

	private static Runnable checkMainThread = new Runnable() {

		@Override
		public void run() {
			int cur_pid = getProcessIdByPkgName(ServiceManager.MUSIC);
			if (cur_pid != -1) {
				if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
					killSelf();
				} else {
					AppLogic.runOnSlowGround(this, 2000);
				}
			} else {
				killSelf();
			}
		}
	};

	private static void killSelf() {
		byte[] data = null;
		try {
			MediaError err = new MediaError(MediaError.ERR_REMOTE,
					"music client pid changed", "客户端进程挂掉");
			data = new JSONBuilder().put("sid", 0)
					.put("err", JsonHelper.toJson(err)).toBytes();
		} catch (Exception e) {
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.remote.callback.onError", data, null);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
