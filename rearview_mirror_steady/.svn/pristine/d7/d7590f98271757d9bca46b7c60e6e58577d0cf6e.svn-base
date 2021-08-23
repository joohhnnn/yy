package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResMusicPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;

public class MusicWorkChoice extends WorkChoice<MusicWorkChoice.MusicData, AudioShowData> {
	public static class MusicData {
		public Boolean isAuto;
		public Long delayTime = (long) 8000;
		public Boolean continuePlay = false;
		public List<AudioShowData> datas;
		public Boolean isMusic;
	}

	public MusicWorkChoice(CompentOption<AudioShowData> option) {
		super(option);
	}

	@Override
	public void showChoices(MusicData data) {
		if (data == null || data.datas == null) {
			return;
		}

		if(data.datas.size()==1){
			getOption().setCanSure(true);
		}
		if (!is2_0Version()) {
			getOption().setNumPageSize(data.datas.size());
		}

		if (data.isAuto != null) {
			getOption().setProgressDelay(data.isAuto
					? data.delayTime != null && data.delayTime > 0 ? data.delayTime.intValue() : getProgressDelay()
					: null);
		} else {
			if (!is2_0Version()) {
				getOption().setProgressDelay(null);
			} else {
				Integer delay = getOption().getProgressDelay();
				if (delay == null) {
					getOption().setProgressDelay(getProgressDelay());
				}
			}
		}

		if (getOption().getTtsText() == null) {
			getOption().setTtsText(getTtsText(data));
		}

		super.showChoices(data);
	}
	
	private int getProgressDelay() {
		return 8000;
	}

	private String getTtsText(MusicData data) {
		if (data.datas.size() > 1) {
			if (data.continuePlay) {
				return NativeData.getResString("RS_MUSIC_DISPLAY_CONTINUE_PLAY");
			}
			return NativeData.getResString("RS_MUSIC_SELECT_LIST_SPK").replace("%COUNT%",String.valueOf(data.datas.size()));
		} else if (data.datas.size() == 1) {
			return NativeData.getResString("RS_MUSIC_SELECT_SINGLE_SPK").replace("%AUDIONAME%",
					data.datas.get(0).getTitle());
		}
		return NativeData.getResString("RS_MUSIC_SELECT_NO_RESULT");
	}

	@Override
	public String getReportId() {
		return "Music_Select";
	}

	@Override
	protected void onConvToJson(MusicData ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", 4);
		jsonBuilder.put("count", ts.datas.size());
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < ts.datas.size(); i++) {
			AudioShowData asd = ts.datas.get(i);
			jsonArray.put(asd.toJsonObjet());
		}
		jsonBuilder.put("audios", jsonArray);
		String showTxt = null;
		try {
			if (mData.continuePlay) {
				showTxt = NativeData.getResString("RS_MUSIC_DISPLAY_CONTINUE_PLAY");
			} else {
				showTxt = NativeData.getResString("RS_MUSIC_DISPLAY_HINT");
				showTxt = showTxt.replace("%KEYWORDS%的", "");
				showTxt = showTxt.replace("%COUNT%", mData.datas.size() + "");
			}
		} catch (Exception e) {
		}
		jsonBuilder.put("prefix", showTxt);
		jsonBuilder.put("vTips",getTips());
		jsonBuilder.put("isMusic", mData.isMusic);
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getMaxPage() == 1) {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_ONE");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_TWO");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_MORE");
					}
				} else {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_ONE_LAST");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_TWO_LAST");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_MORE_LAST");
					}
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_MUSIC_OTHER_PAGE");
			}
		}
		return tips;
	}

	@Override
	protected void onSelectIndex(AudioShowData item, boolean fromPage, int idx, String fromVoice) {
		clearIsSelecting();
		selectItem(item, idx, fromVoice);
	}

	private void selectItem(final AudioShowData asd, int index, final String fromVoice) {
		String hintTxt = "";
		String title = asd.getTitle();
		String report = asd.getReport();
		if (TextUtils.isEmpty(report)) {
			if (!TextUtils.isEmpty(title) && title.contains("(")) {
				hintTxt = NativeData.getResString(asd.isShowDetail() ? "RS_MUSIC_SEARCH_MUSIC" : "RS_MUSIC_WILL_PLAY")
						.replace("%MUSIC%", title.substring(0, title.indexOf("(")));
			} else {
				hintTxt = NativeData.getResString(asd.isShowDetail() ? "RS_MUSIC_SEARCH_MUSIC" : "RS_MUSIC_WILL_PLAY")
						.replace("%MUSIC%", title);
			}
		} else {
			hintTxt = NativeData.getResString(asd.isShowDetail() ? "RS_MUSIC_SEARCH_MUSIC" : "RS_MUSIC_WILL_PLAY")
					.replace("%MUSIC%", report);
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
		// 针对音乐客户端音乐预加载的功能.
		MusicManager.getInstance().sendPreloadIndex(index);
		final int idx = mData.datas.indexOf(asd);
		mSpeechTaskId = TtsManager.getInstance().speakText(hintTxt, new TtsUtil.ITtsCallback() {

			@Override
			public void onEnd() {
				TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			}

			@Override
			public void onSuccess() {
				select(idx, asd, fromVoice);
			}
		});
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, MusicData data) {
		super.onAddWakeupAsrCmd(acsc, data);
		for (int i = 0; i < mData.datas.size(); i++) {
			AudioShowData audioShowData = (AudioShowData) mData.datas.get(i);
			if (!TextUtils.isEmpty(audioShowData.getName())) {
				acsc.addIndex(i, audioShowData.getName());
			}
			if (!TextUtils.isEmpty(audioShowData.getTitle())) {
				acsc.addIndex(i, audioShowData.getTitle());
			}
			if (audioShowData.getWakeUp() != null && audioShowData.getWakeUp().length > 0) {
				acsc.addIndex(i, audioShowData.getWakeUp());
			}
		}
		if (mData.continuePlay && mData.datas.size() >= 2 && mPage.getCurrPage() == 0) {
			acsc.addIndex(0, "继续");
			acsc.addIndex(1, "最新");
		}
	}

	@Override
	protected boolean onIndexSelect(final List<Integer> indexs, String command) {
		if (indexs.size() > 1) {
			String newCommand;
			if (command.endsWith("那个"))
				newCommand = command.substring(0, command.length() - 2);
			else {
				newCommand = command;
			}
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					List<AudioShowData> mtempAudios = new ArrayList<AudioShowData>();
					for (Integer idx : indexs) {
						if (idx < mData.datas.size()) {
							mtempAudios.add((AudioShowData) mData.datas.get(idx));
						}
					}
					mData.datas = mtempAudios;
					refreshData(mData);
				}
			}, 0);
			String mLastHintText = newCommand + "有" + indexs.size() + "个结果，请重新选择";
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.getInstance().speakVoice(mLastHintText, TtsManager.BEEP_VOICE_URL);
			return true;
		}
		return super.onIndexSelect(indexs, command);
	}

	private void select(int index, AudioShowData audioShowData, String fromVoice) {
		if (notifySelectSuccess(audioShowData, true, index, fromVoice)) {
			RecorderWin.close();
			return;
		}

		MusicManager.getInstance().sendResult(index, audioShowData.isShowDetail());
		if (!audioShowData.isShowDetail()) {
			RecorderWin.close();
		}
	}

	@Override
	protected ResourcePage<MusicData, AudioShowData> createPage(MusicData sources) {
		return new ResMusicPage(sources) {

			@Override
			protected int numOfPageSize() {
                JNIHelper.logd("skyward:size=" + getOption().getNumPageSize());
				return getOption().getNumPageSize();
			}
		};
	}
	
	@Override
	protected String convItemToString(AudioShowData item) {
		return item.toString();
	}
}