package com.txznet.txz.component.choice.list;

import android.text.TextUtils;


import com.txz.equipment_manager.EquipmentManager.TTSTheme_Info;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.ThemeStyle;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StyleWorkChoice extends WorkChoice<List<ThemeStyle.Style>, ThemeStyle.Style> {

	public StyleWorkChoice(CompentOption<ThemeStyle.Style> option) {
		super(option);
	}

	@Override
	public void showChoices(List<ThemeStyle.Style> data) {
		if (data == null) {
			LogUtil.loge("showChoices ThemeStyle isEmpty！");
			return;
		}
		if (data.size() > 1) {
			getOption().setTtsText(NativeData.getResString("RS_VOICE_STYLE_SELECT_LIST_MORE_SPK"));
		} else {
			getOption().setTtsText(NativeData.getResString("RS_VOICE_STYLE_SELECT_LIST_SPK"));
		}

		if (!is2_0Version()) {
			getOption().setNumPageSize(data.size());
		}

		super.showChoices(data);
	}

	@Override
	public String getReportId() {
		return "style_Select";
	}

	@Override
	protected void onConvToJson(List<ThemeStyle.Style> ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", RecorderWin.STYLE_SENCE);
		jsonBuilder.put("title", "更多主题");
		jsonBuilder.put("action", "theme_style");
		jsonBuilder.put("count", ts.size());

		List<JSONObject> objs = new ArrayList<JSONObject>();
		for (int i = 0; i < ts.size(); i++) {
			ThemeStyle.Style info = ts.get(i);

			JSONObject obj = new JSONBuilder()
					.put("name", info.getName())
					.put("model", info.getModel().getName())
					.put("theme", info.getTheme().getName())
					.build();
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
                if (mPage.getMaxPage() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_STYLE_ONE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_STYLE_LAST");
                }
            } else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                tips = NativeData.getResString("RS_VOICE_TIPS_STYLE_FIRST");
            } else { //其他中间页
                tips = NativeData.getResString("RS_VOICE_TIPS_STYLE");
            }
        }
        return tips;
    }

	@Override
	protected void onSelectIndex(ThemeStyle.Style item, boolean isFromPage, int idx, String fromVoice) {
		clearIsSelecting();
		selectItem(item, fromVoice);
	}

	private void selectItem(final ThemeStyle.Style info, String fromVoice) {
		JNIHelper.logd("fromVoice : " + fromVoice);

		String strText = NativeData.getResPlaceholderString("RS_VOICE_STYLE_SWITCH_DONE", "%STYLE%", info.getModel().getName());
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(strText, true, false, new Runnable() {
			@Override
			public void run() {
				ThemeConfigManager.getInstance().setSelectStyle(info);
				ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("from_list").setType("skin_usage")
						.putExtra("mode", info.getModel().getName())
						.putExtra("theme", info.getTheme().getName())
						.putExtra("screenType", info.getName()).setSessionId().buildCommReport());
				WinManager.getInstance().saveSelectedStyle();
			}
		});
	}

	@Override
	protected ResourcePage<List<ThemeStyle.Style>, ThemeStyle.Style> createPage(List<ThemeStyle.Style> sources) {
		return new ResListPage<ThemeStyle.Style>(sources) {

			@Override
			protected int numOfPageSize() {
				return getOption().getNumPageSize();
			}
		};
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, List<ThemeStyle.Style> data) {
		getOption().setCanSure(false);
		if (data!=null && data.size() == 1) {
			getOption().setCanSure(true);
		}
		super.onAddWakeupAsrCmd(acsc, data);
	}

	@Override
	protected String convItemToString(ThemeStyle.Style item) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("themeName", item.getTheme().getName());
		jsonBuilder.put("styleName", item.getName());
		jsonBuilder.put("modelName", item.getModel().getName());
		return jsonBuilder.toString();
	}
}
