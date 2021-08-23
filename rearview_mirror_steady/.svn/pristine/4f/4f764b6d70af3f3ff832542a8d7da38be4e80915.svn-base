package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResWxPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.text.TextUtils;

public class WxWorkChoice extends WorkChoice<WxWorkChoice.WxData, WeChatContact> {
	private static final int SUBEVENT_DIRECT_MASK = 0x1;

	public static class WxData {
		public int event;
		public WeChatContacts cons;
		public String ttsSpk;
	}
	
	private WxData mWxData;

	public WxWorkChoice(CompentOption<WeChatContact> option) {
		super(option);
	}

	@Override
	public void showChoices(WxData data) {
		if (data == null || data.cons == null || data.cons.cons == null) {
			LogUtil.loge("WxData is null！");
			return;
		}
		mWxData = data;

		if (data.cons.cons.length == 1 && (data.event & SUBEVENT_DIRECT_MASK) != 0) {
			onItemSelected(data.cons.cons[0], 0, getVoiceTts(data.cons.cons[0], ""));
			return;
		}

		// 不使用进度条
		getOption().setProgressDelay(null);
		if (getOption().getTtsText() == null) {
			getOption().setTtsText(getTtsText());
		}

		if ((data.event & SUBEVENT_DIRECT_MASK) == 0) {
			if (data.cons.cons.length == 1) {
				getOption().setCanSure(true);
			}
		}

		super.showChoices(data);
	}

	private String getTtsText() {
		int size = mWxData.cons.cons.length;
		int mSourceEvent = mWxData.event;
		String hintTxt = "";
		if ((mSourceEvent & SUBEVENT_DIRECT_MASK) == 0) {
			if (size == 0) {
				return "";
			} else if (size == 1) {
				hintTxt = NativeData.getResString("RS_WX_SELECT_SINGLE_SPK");
			} else {
				switch (WeixinManager.getInstance().mWeChatChoice){
					case WeixinManager.ON_SHIELD://屏蔽消息
						hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SHIED").replace("%COUNT%", size+"");
						break;
					case WeixinManager.ON_HISTORY://历史记录
						hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_HISTORY").replace("%COUNT%", size+"");
						break;
					case WeixinManager.ON_UNSHILED://解除屏蔽
						hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_UNSHIED").replace("%COUNT%", size+"");
						break;
					default :
						hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SPK");
						break;
				}
				/*hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SPK").replace("%COUNT%", size + "");*/
			}
		} else {
			if (WeixinManager.getInstance().mWeChatChoice == WeixinManager.ON_UNSHILED) {
				hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_UNSHIED").replace("%COUNT%", size+"");
			} else {
				hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SEND").replace("%COUNT%", size + "");
			}
		}

		return hintTxt;
	}

	String mVoiceUrl = "";

	private String getVoiceTts(WeChatContact wcc, String fromVoice) {
		String text = "";
		if (wcc != null) {
			String name = wcc.name;
			mVoiceUrl = "";
			if (fromVoice != null) {

				boolean needChatMsg = false;
				switch (WeixinManager.getInstance().mWeChatChoice) {
				case WeixinManager.ON_SHIELD:
					text = "";
					needChatMsg = true;
					break;
				case WeixinManager.ON_PLACE:
					text = "";
					needChatMsg = true;
					break;
				case WeixinManager.ON_HISTORY:
					//text = NativeData.getResString("RS_WX_HINT_ACTION_ON_HISTORY").replace("%NAME%", name);
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_HISTORY");
					needChatMsg = true;
					break;
				case WeixinManager.ON_PHOTO:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_PHOTO").replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_UNSHILED:
					text = "";
					needChatMsg = true;
					break;
				case WeixinManager.ON_EXPRESSION:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_EXPRESSION");
					//		.replace("%EXPRESSION%", WeixinManager.getInstance().mExpression).replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_SHARE_POI:
					needChatMsg = false;
					break;
				default:
					//text = NativeData.getResString("RS_WX_HINT_ACTION_ON_RECORD").replace("%NAME%", name);
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_RECORD");
					mVoiceUrl = TtsManager.BEEP_VOICE_URL;
					if (mWxData.cons.cons.length == 1) {
						needChatMsg = true;
					}
					break;
				}

				if (needChatMsg) {
					RecorderWin.addSystemMsg(text);
				}
			}
		}
		return text;
	}
	
	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, WxData data) {
		super.onAddWakeupAsrCmd(acsc, data);
		acsc.addCommand("CMD_SEND", "发送");
	}
	
	@Override
	protected boolean onCommandSelect(String type, String command) {
		if ("CMD_SEND".equals(type)) {
			// 直接发送，不播报
			selectSure(null);
			return true;
		}
		return super.onCommandSelect(type, command);
	}

	@Override
	public String getReportId() {
		return "WX_Select";
	}

	@Override
	protected void onSelectIndex(WeChatContact item, boolean fromPage, int idx, String fromVoice) {
		onItemSelected(item, idx, getVoiceTts(item, ""));
	}
	
	@Override
	protected void onItemSelect(WeChatContact item, boolean isFromPage, int idx, String fromVoice) {
		// 不用默认的上报
	}

	private void onItemSelected(final WeChatContact wcc, final int idx, final String fromVoice) {
		if (!WeixinManager.getInstance().checkEnabled()) {
			clearIsSelecting();
			String spk = NativeData.getResString("RS_WX_EXIT_SORRY");
			RecorderWin.open(spk);
			doReportSelectFinish(false, spk != null ? SELECT_TYPE_VOICE : SELECT_TYPE_CLICK, spk);
			return;
		}
		if (TextUtils.isEmpty(fromVoice)) {
			WeixinManager.getInstance().makeSession(wcc.name, wcc.id);
			putReport(KEY_INDEX, idx + "");
			putReport(KEY_DETAIL, convItemToString(wcc));
			doReportSelectFinish(true, SELECT_TYPE_UNKNOW, null);
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("wechat").setAction("select")
					.putExtra("index", idx).buildTouchReport());
		} else {
			final boolean isSendSession = isSendWxSessionImmedi();
			mSpeechTaskId = TtsManager.getInstance().speakVoice(fromVoice, mVoiceUrl,
					PreemptType.PREEMPT_TYPE_IMMEADIATELY, new ITtsCallback() {
						@Override
						public void onSuccess() {
							putReport(KEY_INDEX, idx + "");
							putReport(KEY_DETAIL, convItemToString(wcc));
							doReportSelectFinish(true, SELECT_TYPE_VOICE, fromVoice);
							RecorderWin.close();
							if (!isSendSession) {
								WeixinManager.getInstance().makeSession(wcc.name, wcc.id);
							}
							super.onSuccess();
						}
					});
			if (isSendSession) {
				WeixinManager.getInstance().makeSession(wcc.name, wcc.id);
			}
			return;
		}
		RecorderWin.close();
	}

	private boolean isSendWxSessionImmedi() {
		return WeixinManager.getInstance().mWeChatChoice == WeixinManager.ON_PLACE
				|| WeixinManager.getInstance().mWeChatChoice == WeixinManager.ON_EXPRESSION;
	}

	@Override
	protected ResourcePage<WxData, WeChatContact> createPage(WxData sources) {
		return new ResWxPage(sources, sources.cons.cons.length) {

			@Override
			protected int numOfPageSize() {
				if (!is2_0Version()) {
					return getTotalSize();
				}
				return mCompentOption.getNumPageSize();
			}
		};
	}

	@Override
	protected void onConvToJson(WxData ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", RecorderWin.WeChatSence);
		jsonBuilder.put("title", mCompentOption.getTtsText());
		jsonBuilder.put("action", "wechat_contacts");
		jsonBuilder.put("count", ts.cons.cons.length);

		List<JSONObject> objs = new ArrayList<JSONObject>();
		for (int i = 0; i < ts.cons.cons.length; i++) {
			WeChatContact wcc = ts.cons.cons[i];
			JSONObject obj = new JSONBuilder().put("name", wcc.name).put("id", wcc.id).build();
			objs.add(obj);
		}
		jsonBuilder.put("contacts", objs.toArray());

		try {
			String sTxt = "RS_WX_DISPLAY_HINT";
			if (mPage.getTotalSize() <= 1) {
				sTxt = "RS_WX_DISPLAY_SINGLE_HINT";
			}
			String showTxt = NativeData.getResString(sTxt).replace("%COUNT%", mPage.getTotalSize() + "");
			jsonBuilder.put("prefix", showTxt);
		} catch (Exception e) {
			jsonBuilder.put("prefix", mCompentOption.getTtsText());
		}
		jsonBuilder.put("vTips",getTips());
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getMaxPage() == 1) {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_ONE");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_TWO");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_MORE");
					}
				} else {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_ONE_LAST");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_TWO_LAST");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_WX_MORE_LAST");
					}
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_WX_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_WX_OTHER_PAGE");
			}
		}
		return tips;
	}
	
	@Override
	protected String convItemToString(WeChatContact item) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("id", item.id);
		jsonBuilder.put("name", item.name);
		jsonBuilder.put("score", item.score);
		jsonBuilder.put("uint32LastTimeContacted", item.uint32LastTimeContacted);
		jsonBuilder.put("uint32LastTimeUpdated", item.uint32LastTimeUpdated);
		jsonBuilder.put("uint32TimesContacted", item.uint32TimesContacted);
		jsonBuilder.put("uint32Type", item.uint32Type);
		return jsonBuilder.toString();
	}
}