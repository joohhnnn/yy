package com.txznet.nav.ui;

import java.util.Vector;

import com.baidu.navisdk.comapi.tts.IBNTTSPlayerListener;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;

public class NavTtsPlayer implements IBNTTSPlayerListener {

	Vector<Integer> mTaskList = new Vector<Integer>();
	boolean mEnable = true;

	class TtsCallback extends ITtsCallback {
		@Override
		public void onCancel() {
			super.onCancel();
			mTaskList.remove(Integer.valueOf(mTaskId));
		}

		@Override
		public void onSuccess() {
			super.onSuccess();
			mTaskList.remove(Integer.valueOf(mTaskId));
		}

		@Override
		public void onError(int iError) {
			super.onError(iError);
			mTaskList.remove(Integer.valueOf(mTaskId));
		}
	}

	@Override
	public int getTTSState() {
		return IBNTTSPlayerListener.PLAYER_STATE_IDLE;
	}

	@Override
	public void phoneCalling() {
	}

	@Override
	public void phoneHangUp() {
	}

	@Override
	public int playTTSText(String speech, int bPreempt) {
		if (speech.equals("导航结束。"))
			return 0;

		if (speech.equals("叮。"))
			return 0;

		// TODO 判断是否电话中
		// if (CallManager.getInstance().isIdle() == false) {
		// return 0;
		// }

		int t = TtsUtil.speakText(speech,
				bPreempt == 1 ? PreemptType.PREEMPT_TYPE_NEXT
						: PreemptType.PREEMPT_TYPE_NONE);
		mTaskList.add(t);
		return 0;
	}

	public void clear() {
		for (int i = 0; i < mTaskList.size(); ++i) {
			TtsUtil.cancelSpeak(mTaskList.get(i));
		}
		mTaskList.clear();
	}

	public void disbaled() {
		mEnable = false;
		clear();
	}
}
