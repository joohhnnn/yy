package com.txznet.txz.component.audio.tingting;

import android.app.ActivityManager;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.audio.tingtingcar.play.interfaces.OnSearchListener;
import com.tingtingfm.cc.CControlCore;
import com.tingtingfm.cc.OnTitleListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.tts.TtsManager;

public class AudioTingTingImpl implements IAudio {

	public static final String PACKAGE_NAME = "com.audio.tingtingcar";

	private int mSession;
	private String mCurrentPlayingName = "";
	private CControlCore mTTAPI = CControlCore.getInstance();

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public void start() {
		JNIHelper.logd("TTAPI.toPlay");
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mTTAPI.toPlay(GlobalContext.get());
	}

	@Override
	public void pause() {
		JNIHelper.logd("TTAPI.toPause");
		// 按照酷我音乐的规格
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				mTTAPI.toPause(GlobalContext.get());
			}
		}, 1000);
	}

	Runnable mExitRunnable = new Runnable() {
		@Override
		public void run() {
			mTTAPI.exitApp(GlobalContext.get());
		}
	};

	@Override
	public void exit() {
		JNIHelper.logd("TTAPI.exit 2000 after");
		// 按照酷我音乐的规格
		MusicManager.getInstance().releaseAudioFocusImmediately();
		AppLogic.removeBackGroundCallback(mExitRunnable);
		AppLogic.runOnBackGround(mExitRunnable, 2000);
	}

	@Override
	public void next() {
		JNIHelper.logd("TTAPI.toNext");
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mTTAPI.toNext(GlobalContext.get());
	}

	@Override
	public void prev() {
		JNIHelper.logd("TTAPI.toPre");
		AppLogic.removeBackGroundCallback(mExitRunnable);
		mTTAPI.toPre(GlobalContext.get());
	}

	private boolean isMusicVisible() {
		try {
			ActivityManager mActManager = (ActivityManager) GlobalContext.get()
					.getSystemService(Context.ACTIVITY_SERVICE);
			String topProcess = mActManager.getRunningTasks(1).get(0).topActivity.getPackageName();
			if (PACKAGE_NAME.equals(topProcess)) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void playFm(String jsonData) {
		JNIHelper.logd("TTAPI.playFm keywords:" + jsonData);
		JSONBuilder builder = new JSONBuilder(jsonData);
		final String keyWord = StringUtils.toString(builder.getVal("keywords", String[].class));

		mSession = 0;
		mTTAPI.search(GlobalContext.get(), keyWord, new OnSearchListener() {

			@Override
			public IBinder asBinder() {
				return null;
			}

			@Override
			public void onSearchReturn(String arg0) throws RemoteException {
				if (TextUtils.isEmpty(arg0)) {
					String tingting = NativeData
							.getResString("RS_VOICE_TINGTING");
					String spk = NativeData.getResPlaceholderString(
							"RS_VOICE_SEARCH_ERROR", "%CMD%", tingting);
					TtsManager.getInstance().speakText(spk);
				}
			}
		});
	}

	@Override
	public String getCurrentFmName() {
		JNIHelper.logd("TTAPI.getTitle");
		mTTAPI.getTitle(GlobalContext.get(), new OnTitleListener() {

			@Override
			public void getTitle(String arg0) {
				mCurrentPlayingName = arg0;
			}
		});

		return mCurrentPlayingName;
	}

	@Override
	public void cancelRequest() {
		mSession++;
	}
}