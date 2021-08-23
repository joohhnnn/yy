package com.txznet.txz.component.tts.iflyAF;

import java.util.Locale;

import android.os.RemoteException;

import com.iflytek.aftts.AFTTSPlayer;
import com.iflytek.aftts.ITtsListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.ITts;

public class TtsIflyAFImpl implements ITts {

	// ////////////////////////////////////////////////////////////

	AFTTSPlayer mAFTTSPlayer;

	class AFTtsListener extends ITtsListener.Stub {
		ITtsCallback mCallback;

		public AFTtsListener(ITtsCallback callback) {
			mCallback = callback;
		}

		@Override
		public void onPlayBegin() throws RemoteException {
			mIsBusy = true;
		}

		@Override
		public void onError(int err) throws RemoteException {
			mIsBusy = false;
			mCallback.onError(err);
		}

		@Override
		public void onPlayCompleted() throws RemoteException {
			mIsBusy = false;
			mCallback.onSuccess();
		}

		@Override
		public void onPlayInterrupted() throws RemoteException {
			mIsBusy = false;
			mCallback.onCancel();
		}

		@Override
		public void onProgress(int arg0) throws RemoteException {
		}

	};

	// ////////////////////////////////////////////////////////////

	@Override
	public int initialize(final IInitCallback oRun) {
		mAFTTSPlayer = AFTTSPlayer.getInstance(GlobalContext.get());

		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				oRun.onInit(mAFTTSPlayer != null);
			}
		}, 0);

		return ERROR_SUCCESS;
	}

	@Override
	public void release() {
		mAFTTSPlayer.destroy();
		mAFTTSPlayer = null;
	}

	boolean mIsBusy = false;

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		stop();

		mAFTTSPlayer.speak(sText, new AFTtsListener(oRun));

		return ERROR_SUCCESS;
	}

	@Override
	public int pause() {
		mAFTTSPlayer.pause();
		return ERROR_SUCCESS;
	}

	@Override
	public int resume() {
		mAFTTSPlayer.resume();
		return ERROR_SUCCESS;
	}

	@Override
	public void stop() {
		mAFTTSPlayer.stop();
	}

	@Override
	public boolean isBusy() {
		return mIsBusy;
	}

	@Override
	public int setLanguage(Locale loc) {
		// TODO 讯飞tts语种设置转换
		return 0;
	}

	@Override
	public void setTtsModel(String ttsModelRole) {
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

	@Override
	public void setOption(TTSOption oOption) {
		// TODO Auto-generated method stub
		
	}

}
