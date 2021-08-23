package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.txz.equipment_manager.EquipmentManager.TTSTheme_Info;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;

public class TtsWorkChoice extends WorkChoice<List<TTSTheme_Info>, TTSTheme_Info> {

	public TtsWorkChoice(CompentOption<TTSTheme_Info> option) {
		super(option);
	}

	@Override
	public void showChoices(List<TTSTheme_Info> data) {
		if (data == null) {
			LogUtil.loge("showChoices tts isEmpty！");
			return;
		}
		RecorderWin.showUserText();
		boolean bPro = false;
		if (data.size() == 1 || (data.size() == 2 && enableFilter(data.get(1)))) {
			bPro = true;
			getOption().setCanSure(true);
			getOption().setTtsText(NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_LIST_ONE_SPK"));
		} else {
			getOption().setTtsText(NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_LIST_MORE_SPK"));
		}
		
		Integer delay = getOption().getProgressDelay();
		if (bPro) {
			if (delay == null || delay < 1) {
				getOption().setProgressDelay(TOTAL_AUTO_CALL_TIME);
			}
		} else {
			getOption().setProgressDelay(null);
		}
		if (!is2_0Version()) {
			getOption().setNumPageSize(data.size());
		}

		super.showChoices(data);
	}

	// 更多的选项也要使用进度条
	private boolean enableFilter(TTSTheme_Info obj) {
		if (obj instanceof TTSTheme_Info) {
			TTSTheme_Info info = obj;
			if (info.uint32ThemeId == TtsManager.TTS_THEME_INVALID_THEME_ID) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, List<TTSTheme_Info> data) {
		super.onAddWakeupAsrCmd(acsc, data);
		List<String> cmds = new ArrayList<String>();
		for (int i = 0; i < mData.size(); i++) {
			Object object = mData.get(i);
			if (object != null && object instanceof TTSTheme_Info) {
				TTSTheme_Info info = (TTSTheme_Info) object;
				String strName = new String(info.strThemeName);
				if (TextUtils.isEmpty(strName)) {
					strName = "默认主题";
				}
				cmds.add(strName);
				cmds.add(strName + "那个");
			}
		}
		if (!cmds.isEmpty()) {
			String[] array = new String[cmds.size()];
			acsc.addCommand("CMD_THEME_NAME", cmds.toArray(array));
		}

		if (mData.size() == 2 && enableFilter(mData.get(1))) {
			acsc.addCommand("SURE", "确定");
		}
	}

	@Override
	protected boolean onCommandSelect(String type, String command) {
		if ("CMD_THEME_NAME".equals(type)) {
			command = command.replace("那个", "");
			return selectItem(command);
		}
		return super.onCommandSelect(type, command);
	}

	private boolean selectItem(String name) {
		boolean bRet = false;
		String speakText = name;
		if ("默认主题".equals(name)) {
			name = "";
		}

		for (int i = 0; i < mData.size(); i++) {
			Object object = mData.get(i);
			if (object != null && object instanceof TTSTheme_Info) {
				TTSTheme_Info info = (TTSTheme_Info) object;
				String strName = new String(info.strThemeName);
				if (TextUtils.equals(strName, name)) {
					selectItem(info, name);
					return true;
				}
			}
		}
		return bRet;
	}

	@Override
	public String getReportId() {
		return "Tts_Select";
	}

	@Override
	protected void onConvToJson(List<TTSTheme_Info> ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", RecorderWin.TtsThemeSence);
		jsonBuilder.put("title", "更多主题");
		jsonBuilder.put("action", "tts_themes");
		jsonBuilder.put("count", ts.size());

		List<JSONObject> objs = new ArrayList<JSONObject>();
		for (int i = 0; i < ts.size(); i++) {
			TTSTheme_Info info = ts.get(i);
			String strName = new String(info.strThemeName);
			if (TextUtils.isEmpty(strName)) {
				strName = NativeData.getResString("RS_DISPLAY_TTS_THEME_DEFAULT_THEME_NAME");
			} else if (info.uint32ThemeId == TtsManager.TTS_THEME_INVALID_THEME_ID) {
				strName = NativeData.getResString("RS_DISPLAY_TTS_THEME_MORE_THEME_NAME");
			}

			JSONObject obj = new JSONBuilder().put("name", strName).put("id", info.uint32ThemeId).build();
			objs.add(obj);
		}

		jsonBuilder.put("themes", objs.toArray());
		jsonBuilder.put("prefix", getOption().getTtsText());
		jsonBuilder.put("vTips",getTips());
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getCurrPageSize() == 1) {
					tips =  NativeData.getResString(mPage.getCurrPage() == 0 ? "RS_VOICE_TIPS_TTS_ONE" : "RS_VOICE_TIPS_TTS_ONE_LAST");
				} else if (mPage.getCurrPageSize() == 2) {
					tips = NativeData.getResString("RS_VOICE_TIPS_TTS_TWO");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_TTS_MORE");
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_TTS_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_TTS_OTHER_PAGE");
			}
		}
		return tips;
	}

	@Override
	protected void onSelectIndex(TTSTheme_Info item, boolean isFromPage, int idx, String fromVoice) {
		selectItem(item, fromVoice);
	}

	private void selectItem(final TTSTheme_Info info, String fromVoice) {
		clearIsSelecting();
		// 更多主题直接跳转
		if (info.uint32ThemeId == TtsManager.TTS_THEME_INVALID_THEME_ID) {
			TtsManager.getInstance().gotoMoreThemes();
			return;
		}

		JNIHelper.logd("fromVoice : " + fromVoice);
		if (info.strThemeName != null && info.strThemeName.length > 0 && !TextUtils.isEmpty(new String(info.strThemeName))) {
			fromVoice = new String(info.strThemeName);
		}
		/*
		 * if (!fromVoice.startsWith("第")){ fromVoice += "的"; }
		 */
		String strText = NativeData.getResPlaceholderString("RS_VOICE_TTS_THEME_SWITCH_DONE", "%THEME%",
				TextUtils.isEmpty(fromVoice) ? "默认" : fromVoice);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(strText, new Runnable() {
			@Override
			public void run() {
				TtsManager.getInstance().switchTTSTheme(info);
			}
		});
	}

	@Override
	protected ResourcePage<List<TTSTheme_Info>, TTSTheme_Info> createPage(List<TTSTheme_Info> sources) {
		return new ResListPage<TTSTheme_Info>(sources) {

			@Override
			protected int numOfPageSize() {
				return getOption().getNumPageSize();
			}
		};
	}
	
	@Override
	protected String convItemToString(TTSTheme_Info item) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("strThemeName", new String(item.strThemeName));
		jsonBuilder.put("uint32State", item.uint32State);
		jsonBuilder.put("uint32ThemeId", item.uint32ThemeId);
		jsonBuilder.put("uint32Version", item.uint32Version);
		return jsonBuilder.toString();
	}
}
