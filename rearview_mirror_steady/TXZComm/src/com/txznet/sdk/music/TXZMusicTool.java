package com.txznet.sdk.music;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ApplicationInfo;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZMusicManager.MusicToolStatusListener;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;
import com.txznet.sdkinner.TXZServiceCommandDispatcher.CommandProcessor;

public class TXZMusicTool implements TXZMusicManager.MusicTool {
	private TXZMusicTool() {
	}

	static TXZMusicTool sInstance = new TXZMusicTool();

	/**
	 * θ·εεδΎ
	 * 
	 * @return
	 */
	public static TXZMusicTool getInstance() {
		try {
			ApplicationInfo info = GlobalContext
					.get()
					.getPackageManager()
					.getApplicationInfo(
							ServiceManager.MUSIC,
							android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);
			if (info != null)
				return sInstance;
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void play() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.play", null, null);
	}

	@Override
	public void continuePlay() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.play.extra", null, null);
	}

	@Override
	public void pause() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.pause", null, null);
	}

	@Override
	public void exit() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.exit", null, null);
	}

	@Override
	public void next() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.next", null, null);
	}

	@Override
	public void prev() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.prev", null, null);
	}

	@Override
	public void switchModeLoopAll() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopAll", null, null);
	}

	@Override
	public void switchModeLoopOne() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeLoopOne", null, null);
	}

	@Override
	public void switchModeRandom() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchModeRandom", null, null);
	}

	@Override
	public void switchSong() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.switchSong", null, null);
	}

	@Override
	public void playRandom() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.txztool.playRandom", null, null);
	}

	@Override
	public void playFavourMusic() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.txztool.playFavourMusic", null, null);
	}

	@Override
	public void playMusic(MusicModel musicModel) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.txztool.playMusic",
				musicModel.toString().getBytes(), null);
	}

	@Override
	public void favourMusic() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.txztool.favourMusic", null, null);
	}

	@Override
	public void unfavourMusic() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.music.txztool.unfavourMusic", null, null);
	}

	static MusicToolStatusListener mListener = null;

	static Runnable mRunnableRefreshStatus = new Runnable() {
		@Override
		public void run() {
			// θ?’ιηΆζ
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.status.subscrib", null, null);
			// θ·εζζ°ηζ­ζΎηΆζ
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.isPlaying", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mRemote_isPlaying = data.getBoolean();
							if (mListener != null) {
								mListener.onStatusChange();
							}
						}
					});
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.isBufferProccessing", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mRemote_isBufferProccessing = data.getBoolean();
							if (mListener != null) {
								mListener.onStatusChange();
							}
						}
					});
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.getProgress", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mRemote_Progress = data.getDouble();
							if (mListener != null
									&& mListener instanceof TXZMusicStatusListener) {
								((TXZMusicStatusListener) mListener)
										.onProgressChange();
							}
						}
					});
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.getCurrentMusicIndex", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							mRemote_Index = data.getInt();
							if (mListener != null) {
								mListener.onStatusChange();
							}
						}
					});
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.getMusicList", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							parseMusicListFromData(data.getBytes());
						}
					});
			ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
					"music.getPlayMode", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							parsePlayModeFromData(data.getBytes());
						}
					});
		}
	};

	@Override
	public void setStatusListener(MusicToolStatusListener listener) {
		mListener = listener;
		TXZServiceCommandDispatcher.setCommandProcessor("musicStatus.",
				new CommandProcessor() {
					@Override
					public byte[] process(String packageName, String command,
							byte[] data) {
						// LogUtil.logd("recive TXZMusicTool command: " +
						// command);
						if (command.equals("updateProgress")) {
							mRemote_Progress = Double.parseDouble(new String(
									data));
							if (mListener != null
									&& mListener instanceof TXZMusicStatusListener) {
								((TXZMusicStatusListener) mListener)
										.onProgressChange();
							}
							return null;
						}
						if (command.equals("isPlaying")) {
							mRemote_isPlaying = Boolean
									.parseBoolean(new String(data));
							ServiceManager.getInstance().sendInvoke(
									ServiceManager.MUSIC,
									"music.getCurrentMusicIndex", null,
									new GetDataCallback() {
										@Override
										public void onGetInvokeResponse(
												ServiceData data) {
											mRemote_Index = data.getInt();
											if (mListener != null) {
												mListener.onStatusChange();
											}
										}
									});
							return null;
						}
						if (command.equals("isBufferProccessing")) {
							mRemote_isBufferProccessing = Boolean
									.parseBoolean(new String(data));
							if (mListener != null) {
								mListener.onStatusChange();
							}
							return null;
						}
						if (command.equals("updateMusicList")) {
							parseMusicListFromData(data);

							return null;
						}
						if (command.equals("updatePlayMode")) {
							parsePlayModeFromData(data);
							return null;
						}
						return null;
					}
				});
		ServiceManager.getInstance().keepConnection(ServiceManager.MUSIC,
				mRunnableRefreshStatus);
		mRunnableRefreshStatus.run();
	}

	/**
	 * ηΆζηε¬ε¨
	 *
	 */
	public static interface TXZMusicStatusListener extends
			MusicToolStatusListener {
		/**
		 * ζ­ζΎηΆζεε
		 */
		@Override
		public void onStatusChange();

		/**
		 * ζ­ζΎθΏεΊ¦εε
		 */
		public void onProgressChange();

		/**
		 * ζ­ζΎεθ‘¨εε
		 */
		public void onListChange();

		/**
		 * ζ­ζΎζ¨‘εΌεε
		 */
		public void onModeChange();
	}

	static boolean mRemote_isPlaying = false;

	@Override
	public boolean isPlaying() {
		return mRemote_isPlaying;
	}

	static boolean mRemote_isBufferProccessing = false;

	/**
	 * ζ―ε¦ηΌε²δΈ­
	 * 
	 * @return
	 */
	public boolean isBufferProccessing() {
		return mRemote_isBufferProccessing;
	}

	static double mRemote_Progress = 0.0;

	/**
	 * θ·εζ­ζΎθΏεΊ¦
	 * 
	 * @return
	 */
	public double getProgress() {
		return mRemote_Progress;
	}

	@Override
	public MusicModel getCurrentMusicModel() {
		try {
			synchronized (TXZMusicTool.class) {
				return mRemote_MusicList.get(mRemote_Index);
			}
		} catch (Exception e) {
			return null;
		}
	}

	static int mRemote_Index = 0;

	/**
	 * θ·εε½εηζ­ζΎη΄’εΌ
	 * 
	 * @return
	 */
	public int getCurrentMusicIndex() {
		return mRemote_Index;
	}

	/**
	 * θ·εε½εζ­ζΎζ¨‘εΌ
	 */
	public static enum PlayMode {
		/**
		 * εζ²εΎͺη―
		 */
		PLAY_MODE_LOOP_SINGLE,
		/**
		 * ιζΊζ­ζΎ
		 */
		PLAY_MODE_RANDOM,
		/**
		 * ε¨ι¨εΎͺη―
		 */
		PLAY_MODE_LOOP_ALL,
	};

	static PlayMode mRemote_PlayMode = PlayMode.PLAY_MODE_LOOP_SINGLE;

	static boolean parsePlayModeFromData(byte[] data) {
		try {
			String mode = new String(data);
			if (mode.equals("single")) {
				mRemote_PlayMode = PlayMode.PLAY_MODE_LOOP_SINGLE;
				return true;
			}
			if (mode.equals("all")) {
				mRemote_PlayMode = PlayMode.PLAY_MODE_LOOP_ALL;
				return true;
			}
			if (mode.equals("random")) {
				mRemote_PlayMode = PlayMode.PLAY_MODE_RANDOM;
				return true;
			}
		} catch (Exception e) {
		} finally {
			if (mListener != null
					&& mListener instanceof TXZMusicStatusListener) {
				((TXZMusicStatusListener) mListener).onModeChange();
			}
		}
		return false;
	}

	/**
	 * θ·εε½εζ­ζΎζ¨‘εΌ
	 * 
	 * @return
	 */
	public PlayMode getPlayMode() {
		return mRemote_PlayMode;
	}

	/**
	 * εθ‘θι³δΉζ¨‘ε
	 *
	 */
	public static class TXZMusicModel extends MusicModel {
		protected boolean favour;

		/**
		 * θ·εζΆθζ εΏ
		 * 
		 * @return
		 */
		public boolean getFavour() {
			return favour;
		}

		/**
		 * θ?Ύη½?ζΆθζ εΏ
		 * 
		 * @param favour
		 */
		public void setFavour(boolean favour) {
			this.favour = favour;
		}
	}

	static List<TXZMusicModel> mRemote_MusicList = new ArrayList<TXZMusicModel>();

	static boolean parseMusicListFromData(byte[] data) {
		try {
			MediaList lst = MediaList.parseFrom(data);
			List<TXZMusicModel> ret = new ArrayList<TXZMusicModel>();
			for (MediaItem item : lst.rptMediaItem) {
				TXZMusicModel mod = new TXZMusicModel();
				mod.setTitle(item.msgMedia.strTitle);
				mod.setAlbum(item.msgMedia.strAlbum);
				mod.setArtist(item.msgMedia.rptStrArtist);
				mod.setKeywords(item.msgMedia.rptStrKeywords);
				mod.setFavour(item.msgMedia.bFavourite == null ? false
						: item.msgMedia.bFavourite);
				ret.add(mod);
			}
			synchronized (TXZMusicTool.class) {
				mRemote_MusicList = ret;
			}
			if (mListener != null
					&& mListener instanceof TXZMusicStatusListener) {
				((TXZMusicStatusListener) mListener).onListChange();
			}
		} catch (InvalidProtocolBufferNanoException e) {
			return false;
		}
		return true;
	}

	/**
	 * θ·ει³δΉεθ‘¨
	 */
	public List<TXZMusicModel> getMusicList() {
		synchronized (TXZMusicTool.class) {
			return mRemote_MusicList;
		}
	}

	/**
	 * ζ­ζΎε½εεθ‘¨ζε?η΄’εΌηι³δΉ
	 * 
	 * @param index
	 */
	public void playIndex(int index) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.playIndex", ("" + index).getBytes(), null);
	}

	/**
	 * ζΆθζε?η΄’εΌηι³δΉ
	 * 
	 * @param index
	 *            η΄’εΌ
	 * @param favour
	 *            trueζΆθοΌfalseδΈΊεζΆζΆθ
	 */
	public void favourIndex(int index, boolean favour) {
		JSONBuilder json = new JSONBuilder();
		json.put("index", index);
		json.put("favour", favour);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.favourIndex", json.toBytes(), null);
	}

	/**
	 * ε ι€ζε?η΄’εΌηι³δΉ
	 * 
	 * @param index
	 *            η΄’εΌ
	 * @param deleteFile
	 *            ζ―ε¦ε½»εΊε ι€ζδ»ΆοΌθΏζ―δ»δ»ε½εεθ‘¨η§»ι€
	 */
	public void deleteIndex(int index, boolean deleteFile) {
		JSONBuilder json = new JSONBuilder();
		json.put("index", index);
		json.put("deleteFile", deleteFile);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
				"music.deleteIndex", json.toBytes(), null);
	}
}
