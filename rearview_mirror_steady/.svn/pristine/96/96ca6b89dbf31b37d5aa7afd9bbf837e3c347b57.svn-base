package com.txznet.txz.component.audio.xmly;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProcessUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.component.audio.AudioSelector;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.audio.AudioSelector.AudioSelectorListener;
import com.txznet.txz.component.selector.Selector;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.MediaControlUtil;
import com.ximalaya.speechcontrol.IMainDataCallback;
import com.ximalaya.speechcontrol.SpeechControler;
import com.ximalaya.ting.android.opensdk.model.track.SearchTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AudioXmly implements IAudio {
	public static boolean SHOW_SEL_LIST = false;
	public static final String PACKAGE_NAME = "com.ximalaya.ting.android.car";
	
	private static final long SEARCH_LIST_TIMEOUT = 5000;

//	private String appSecret = "35933e072735ed7d2afb91cfa5b7a823";
//	private String appKey = "d266c98e229b8ebf8104102a7cf069c8";
//	private String pkgId = "com.txznet.txz";

	private String appSecret = "ca0f507f2959480a95cb2fb7fa9e0049";
	private String appKey = "fc5d4e2f704fa6a09728d104ca4c1a9c";
	private String pkgId = "com.ximalaya.ting.android.alihoushijing";

	/*
	 * private String appSecret = "79a7db4c396bf9bdae9fd22352c62645"; private
	 * String appKey = "aecfbf1bb90c0ce3a3ccecc322132f05"; private String pkgId
	 * = "com.ximalaya.ting.android.kld";
	 */

	private SpeechControler mControler;

	private static AudioXmly sInstance;

	public static AudioXmly getInstance() {
		if (null == sInstance) {
			synchronized (AudioXmly.class) {
				if (null == sInstance) {
					sInstance = new AudioXmly();
				}
			}
		}

		return sInstance;
	}

	private AudioXmly() {
		try {
			mControler = SpeechControler.getInstance(GlobalContext.get());
			mControler.init(appSecret, appKey, pkgId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setAppkey(String secret, String key, String pkg) {
		appSecret = secret;
		appKey = key;
		pkgId = pkg;

		reconnectSDK();
	}

	/**
	 * 重新初始化sdk
	 */
	private void reconnectSDK() {
		if (mControler != null) {
			mControler.destory();
		}
		mControler = SpeechControler.getInstance(GlobalContext.get());
		mControler.init(appSecret, appKey, pkgId);
	}

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public void start() {
		if (mControler != null) {
			openPager();

			checkConnectionAndRun(new Runnable() {

				@Override
				public void run() {
					mControler.play();
				}
			});

		}
	}

	private void checkConnectionAndRun(final Runnable r) {
		checkConnectionAndRun(r, 1000, 1000);
	}

	private void checkConnectionAndRun(final Runnable r,
			final long connectDelay, final long runDelay) {
		if (null != mControler && mControler.checkConnectionStatus()) {
			// sdk已连接，直接run
			log("sdk connected, run instantly");
			AppLogic.runOnBackGround(r, runDelay);
		} else {
			// 重连sdk并执行指定操作
			log("sdk not connected, reconnect & run");
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					reconnectSDK();
					AppLogic.runOnBackGround(r, runDelay);
				}

			}, connectDelay);
		}
	}

	private void openPager() {
		if (ProcessUtil.isForeground(getPackageName())) {
			return;
		}

		try {
			Intent intent = new Intent();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.setData(Uri.parse("tingcar://open"));
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
	}

	@Override
	public void pause() {
		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				mControler.stop();
			}
		});
	}

	@Override
	public void playFm(String jsonData) {
		final String keyWord = genSearchKeyword(jsonData);
		
		if (TextUtils.isEmpty(keyWord)) {
			log("keyword is empty! jsonData = " + jsonData);
			return;
		}

		if (SHOW_SEL_LIST) {
			checkConnectionAndRun(new Runnable() {

				@Override
				public void run() {
					searchSelectList(keyWord);
				}
			});

			return;
		}

		openPager();

		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				mControler.setPlayModel(keyWord, 0,
						new IMainDataCallback<String>() {

							@Override
							public void successCallBack(String arg0) {
								log("set play model success: " + keyWord);
							}

							@Override
							public void errCallBack(String arg0) {
								log("set play model failed: " + arg0);
							}
						});
			}
		});
	}
	
	private String genSearchKeyword(String jsonData){
		JSONBuilder builder = new JSONBuilder(jsonData);
		
		final String keyWord = StringUtils.toString(builder.getVal("keywords",
				String[].class));
		if(!TextUtils.isEmpty(keyWord)){
			return keyWord;
		}
		
		final String title = builder.getVal("title", String.class);
		if (!TextUtils.isEmpty(title)){
			return title;
		}
		
		final String artists = StringUtils.toString(builder.getVal("artists", String[].class));
		if(!TextUtils.isEmpty(artists)){
			return artists;
		}
		
		final String album = builder.getVal("album", String.class);
		if (!TextUtils.isEmpty(album)){
			return album;
		}
		
		final String category = builder.getVal("category", String.class);
		if (!TextUtils.isEmpty(category)){
			return category;
		}
		
		return "";
	}

	private boolean bSearchTaskCanceled = false;

	private void searchSelectList(final String keyword) {
		bSearchTaskCanceled = false;
		mControler.getSourseLists(keyword, 0, 1, 8, 2,
				new IMainDataCallback<SearchTrackList>() {

					@Override
					public void errCallBack(String arg0) {
						log("search track list failed: " + arg0);
						AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
						String spk = NativeData.getResString(
								"RS_VOICE_SEARCH_ERROR").replace("%CMD%", "");
						procError(spk);
					}

					@Override
					public void successCallBack(final SearchTrackList arg0) {
						AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
						if (bSearchTaskCanceled) {
							log("search track list task canceled, return");
							return;
						}
						
						if(null == arg0 || null == arg0.getTracks() || 0 == arg0.getTracks().size()){
							procError("未找到相关结果");
							return;
						}

						log("search track list success: "
								+ arg0.getTracks().size());

						List<AudioSelector.Music> musicList = new ArrayList<AudioSelector.Music>();

						for (Track track : arg0.getTracks()) {
							AudioSelector.Music model = new AudioSelector.Music();
							model.audioName = track.getTrackTitle();
							model.authorName = track.getAnnouncer()
									.getNickname();
							model.sourceName = track.getAlbum().getAlbumTitle();

							model.audioId = track.getAlbum().getAlbumId() + "";
							model.includeTrackCount = track.getAlbum()
									.describeContents();
							model.albumIntro = track.getTrackIntro();
							musicList.add(model);
						}

						Selector.entryAudioSelector(musicList, keyword,
								new AudioSelectorListener() {

									@Override
									public void onAudioSelected(
											AudioSelector.Music music, int index) {
										playTrack(arg0.getTracks().get(index));
									}
								});
					}
				});
		
		AppLogic.removeBackGroundCallback(mSearchTimeoutTask);
		AppLogic.runOnBackGround(mSearchTimeoutTask, SEARCH_LIST_TIMEOUT);

	}

	private Runnable mSearchTimeoutTask = new Runnable() {

		@Override
		public void run() {
			bSearchTaskCanceled = true;
			AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
			RecorderWin.speakTextWithClose(
					NativeData.getResString("RS_MUSIC_SELECT_TIMEOUT"), null);
		}
	};

	private void playTrack(final Track track) {
		openPager();

		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				mControler.setPlayByTrack(track, null);
			}
		}, 1000, 500);
	}

	private void procError(String hint) {
		AsrManager.getInstance().cancel();
		RecorderWin.addSystemMsg(hint);
		TtsManager.getInstance().speakText(hint, new TtsUtil.ITtsCallback() {

			@Override
			public void onSuccess() {
				AsrManager.getInstance().start();
			};
		});
	}

	@Override
	public void exit() {
		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				mControler.stopAndExitApp();
			}
		}, 500, 500);
	}

	@Override
	public void next() {
		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				if (mControler.hasNext()) {
					mControler.playNext();
				}
			}
		});
	}

	@Override
	public void prev() {
		checkConnectionAndRun(new Runnable() {

			@Override
			public void run() {
				if (mControler.hasPre()) {
					mControler.playPre();
				}
			}
		});
	}

	@Override
	public String getCurrentFmName() {
		if (mControler != null) {
			Track track = mControler.getCurrentTrack();
			if (track != null) {
				return track.getTrackTitle();
			}
		}
		return "";
	}

	@Override
	public void cancelRequest() {
	}

	// logger
	private static final String TAG = "AudioXmly";

	private void log(String log) {
		LogUtil.logd(TAG + " :: " + log);
	}
}