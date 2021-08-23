package com.txznet.txz.component.choice.list;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ReminderWorkChoice extends WorkChoice<List<ReminderWorkChoice.ReminderItem>, ReminderWorkChoice.ReminderItem> {

	public ReminderWorkChoice(CompentOption<ReminderItem> option) {
		super(option);
	}
	
	public static class ReminderItem {
		public String id;
		public String content;
		public String time;
		public String position;
	}
	
	@Override
	public String getReportId() {
		return "reminder_select";
	}
	
	@Override
	public void showChoices(List<ReminderItem> data) {
		getOption().setTtsText(NativeData.getResString("RS_VOICE_REMINDER_LIST_TIPS"));
		super.showChoices(data);
	}

	@Override
	protected void onConvToJson(List<ReminderItem> ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", RecorderWin.ReminderSence);
		jsonBuilder.put("count", ts.size());
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < ts.size(); i++) {
			ReminderItem item = ts.get(i);
			JSONObject jsonItem = new JSONObject();
			try {
				jsonItem.put("content", item.content);
				jsonItem.put("id", item.id);
				jsonItem.put("time", item.time);
				jsonItem.put("position", item.position);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonItem);
		}
		jsonBuilder.put("reminders", jsonArray);
		jsonBuilder.put("prefix", getOption().getTtsText());
		jsonBuilder.put("vTips",getTips());
	}

	@Override
	protected String convItemToString(ReminderItem item) {
		JSONObject jsonItem = new JSONObject();
		try {
			jsonItem.put("content", item.content);
			jsonItem.put("id", item.id);
			jsonItem.put("time", item.time);
			jsonItem.put("position", item.position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonItem.toString();
	}

	@Override
	protected void onSelectIndex(ReminderItem item, boolean isFromPage,
			int idx, String fromVoice) {
		
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getCurrPageSize() == 1) {
					tips =  NativeData.getResString(mPage.getCurrPage() == 0 ? "RS_VOICE_TIPS_REMINDER_ONE" : "RS_VOICE_TIPS_REMINDER_ONE_LAST");
				} else if (mPage.getCurrPageSize() == 2) {
					tips = NativeData.getResString("RS_VOICE_TIPS_REMINDER_TWO");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_REMINDER_MORE");
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_REMINDER_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_REMINDER_OTHER_PAGE");
			}
		}
		return tips;
	}
	
	@Override
	protected ResourcePage<List<ReminderItem>, ReminderItem> createPage(
			List<ReminderItem> sources) {
		final int allSize = sources.size();
		return new ResListPage<ReminderWorkChoice.ReminderItem>(sources) {

			@Override
			protected int numOfPageSize() {
				if (!is2_0Version()) {
					return allSize;
				}
				return getOption().getNumPageSize();
			}
		};
	}
	
	@Override
	protected void onClearSelecting() {
		super.onClearSelecting();
	}
	
}
