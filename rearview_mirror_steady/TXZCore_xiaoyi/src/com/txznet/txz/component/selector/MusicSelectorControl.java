package com.txznet.txz.component.selector;

import java.util.List;

import org.json.JSONArray;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;

public class MusicSelectorControl extends ISelectControl {

	private List<AudioShowData> mAudioShowDatas;

	public MusicSelectorControl(int pageCount) {
		super(pageCount);
	}

	public void showMusicSelects(List<AudioShowData> asds) {
		JNIHelper.logd("showMusicSelects:" + asds);
		this.mAudioShowDatas = asds;
		if (mAudioShowDatas == null) {
			return;
		}
		
		if (mUseNewSelector) {
			//mUseAutoPerform = true;
		} else {
			//mUseAutoPerform = false;
		}

		beginSelectorParse(mAudioShowDatas, "");
	}

	@Override
	// 进度条时长
	protected long getProgressDelay() {
		return 8000;
	}

	@Override
	protected String getBeginSelectorHint() {
		if (mAudioShowDatas.size() > 1) {
			return NativeData.getResString("RS_MUSIC_SELECT_LIST_SPK");
		} else if (mAudioShowDatas.size() == 1) {
			return NativeData.getResString("RS_MUSIC_SELECT_SINGLE_SPK").replace("%AUDIONAME%",mAudioShowDatas.get(0).getTitle());
		}
		return NativeData.getResString("RS_MUSIC_SELECT_NO_RESULT");
	}

	@Override
	protected void onSrcListUpdate(List tmp) {
		JNIHelper.logd("onSrcListUpdate:" + tmp);
		if (mAudioShowDatas == null) {
			return;
		}

		JSONBuilder jb = new JSONBuilder();
		jb.put("type", 4);
		jb.put("count", tmp.size());
		jb.put("curPage", mPageHelper.getCurPager());
		jb.put("maxPage", mPageHelper.getMaxPager());
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < tmp.size(); i++) {
			AudioShowData asd = (AudioShowData) tmp.get(i);
			jsonArray.put(asd.toJsonObjet());
		}
		jb.put("audios", jsonArray);
		
		try {
			String showTxt = NativeData.getResString("RS_MUSIC_DISPLAY_HINT");
			showTxt = showTxt.replace("%KEYWORDS%的", "");
			showTxt = showTxt.replace("%COUNT%", mSourceList.size()+"");
			jb.put("prefix", showTxt);
		} catch (Exception e) {
		}
		
		RecorderWin.sendSelectorList(jb.toString());
//		RecordInvokeFactory.getAdapter().addListMsg(jb.toString());
	}

	@Override
	protected String getAsrTaskId() {
		return "CTRL_MUSIC_SELECTOR";
	}

	@Override
	protected int getSenceGrammar() {
		return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL;
	}

	@Override
	protected void onItemSelect(Object obj, int index, String fromVoice) {
		if (obj == null || (obj instanceof AudioShowData) == false) {
			return;
		}

		if (index < 0 || index >= mTmpList.size()) {
			JNIHelper.logd("index out");
			return;
		}

		String hintTxt = "";
		final int idx = mUseNewSelector ? mPageHelper.getCurPager() * mPageCount + index : index;
		
		if (mAudioShowDatas.size() > idx) {
			AudioShowData asd = mAudioShowDatas.get(idx);
			String title = asd.getTitle();
			if (!TextUtils.isEmpty(title) && title.contains("(")) {
				hintTxt = NativeData.getResString("RS_MUSIC_WILL_PLAY").replace("%CMD%",
						title.substring(0, title.indexOf("(")));
			} else {
				hintTxt = NativeData.getResString("RS_MUSIC_WILL_PLAY").replace("%CMD%", title);
			}
		}
		
		if (TextUtils.isEmpty(fromVoice)) {
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("music").setAction("select")
					.putExtra("index", index).buildTouchReport());
		}

		// if (fromVoice != null) {
		RecorderWin.addSystemMsg(hintTxt);
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakText(hintTxt, new TtsUtil.ITtsCallback() {

			@Override
			public void onEnd() {
				TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			}

			@Override
			public void onSuccess() {
				select(idx);
			}
		});
		return;
		// }
		// select(idx);
	}

	private void select(int index) {
		if (mOnItemSelectListener != null) {
			mOnItemSelectListener.onItemSelect(mAudioShowDatas, index, mAudioShowDatas.get(index));
		}

		MusicManager.getInstance().sendResult(index);
		RecorderWin.close();
	}

	@Override
	protected void onAsrComplexSelect(AsrComplexSelectCallback acsc) {
	}

	@Override
	protected boolean onWakeupItemSelect(boolean isWakeupResult, String type, String command) {
		return false;
	}

	@Override
	protected boolean onWakeupIndexSelect(boolean isWakeupResult, List<Integer> indexs, String command) {
		return false;
	}

	@Override
	protected void onCommandSelect(List tmp, int index, String speech) {
		onItemSelect(tmp.get(index), index, speech);
	}
}
