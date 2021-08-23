package com.txznet.txz.component.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class WxSelectorControl extends ISelectControl {
	private static final int SUBEVENT_DIRECT_MASK = 0x1;

	private int mSourceEvent;
	private boolean mCallMakeSure;

	private WeChatContacts mWccs;
	private List<WeChatContact> mWccList;

	public WxSelectorControl(int pageCount) {
		super(pageCount);
	}

	public void showWxContactsList(int event, WeChatContacts cons, String spk) {
		JNIHelper.logd("showWxContactsList:" + cons);
		this.mWccs = cons;
		this.mSourceEvent = event;
		if (mWccs == null) {
			return;
		}
		if (mWccs.cons == null) {
			// TODO 处理为空的情况
			return;
		}

		mWccList = new ArrayList<WeChatContact>();
		mWccList = Arrays.asList(mWccs.cons);

		if (mWccList.size() == 1 && (mSourceEvent & SUBEVENT_DIRECT_MASK) != 0) {
			onItemSelect(mWccList.get(0), 0, getVoiceTts(mWccList, 0, ""));
			return;
		}
		//mUseAutoPerform = false;

		beginSelectorParse(mWccList, spk);
	}

	@Override
	protected String getBeginSelectorHint() {
		int size = mWccList.size();
		String hintTxt = "";
		if ((mSourceEvent & SUBEVENT_DIRECT_MASK) == 0) {
			if (size == 0) {
				// TODO 处理找不到结果
				return "";
			} else if (size == 1) {
				hintTxt = NativeData.getResString("RS_WX_SELECT_SINGLE_SPK");
				mCallMakeSure = true;
			} else {
				hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SPK").replace("%COUNT%", size+"");
				//mUseAutoPerform = false;
				mCallMakeSure = false;
			}
		} else {
			hintTxt = NativeData.getResString("RS_WX_SELECT_LIST_SPK").replace("%COUNT%", size+"");
		}

		return hintTxt;
	}

	private String getVoiceTts(List fromList, int index, String fromVoice) {
		int size = fromList.size();
		String text = "";
		if (index >= 0 && index < size) {
			WeChatContact wcc = (WeChatContact) fromList.get(index);
			String name = wcc.name;
			mVoiceUrl = "";
			if (fromVoice != null) {

				boolean needChatMsg = false;
				switch (WeixinManager.getInstance().mWeChatChoice) {
				case WeixinManager.ON_SHIELD:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_SHILED").replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_PLACE:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_PLACE").replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_HISTORY:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_HISTORY").replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_PHOTO:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_PHOTO").replace("%NAME%", name);
					needChatMsg = true;
					break;
				case WeixinManager.ON_UNSHILED:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_UNSHILED").replace("%NAME%", name);
					needChatMsg = true;
					break;
				default:
					text = NativeData.getResString("RS_WX_HINT_ACTION_ON_RECORD").replace("%NAME%", name);
					mVoiceUrl = TtsManager.BEEP_VOICE_URL;
					if (size == 1) {
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
	protected void onSrcListUpdate(List tmp) {
		JNIHelper.logd("onSrcListUpdate:" + tmp);
		JSONBuilder jb = new JSONBuilder();
		jb.put("type", RecorderWin.WeChatSence);
		jb.put("title", mLastHintTxt);
		jb.put("action", "wechat_contacts");
		jb.put("count", tmp.size());
		jb.put("curPage", mPageHelper.getCurPager());
		jb.put("maxPage", mPageHelper.getMaxPager());

		List<JSONObject> objs = new ArrayList<JSONObject>();
		for (int i = 0; i < tmp.size(); i++) {
			WeChatContact wcc = (WeChatContact) tmp.get(i);
			JSONObject obj = new JSONBuilder().put("name", wcc.name).put("id", wcc.id).build();
			objs.add(obj);
		}
		jb.put("contacts", objs.toArray());
		
		try {
			String showTxt = NativeData.getResString("RS_WX_DISPLAY_HINT").replace("%COUNT%", mSourceList.size() + "");
			jb.put("prefix", showTxt);
		} catch (Exception e) {
			jb.put("prefix", mLastHintTxt);
		}
		
		RecorderWin.sendSelectorList(jb.toString());
//		RecordInvokeFactory.getAdapter().addListMsg(jb.toString());
	}

	@Override
	protected String getAsrTaskId() {
		return "CRTL_WEIXIN_SELECTOR_ID";
	}

	@Override
	protected int getSenceGrammar() {
		return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL;
	}

	@Override
	protected void onItemSelect(Object obj, int index, String fromVoice) {
		if (!WeixinManager.getInstance().checkEnabled()) {
			String spk = NativeData.getResString("RS_WX_EXIT_SORRY");
			TtsUtil.speakText(spk);
			selectCancel(false);
			return;
		}
		final WeChatContact wcc = (WeChatContact) obj;
		if (TextUtils.isEmpty(fromVoice)) {
			WeixinManager.getInstance().makeSession(wcc.name, wcc.id);
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("wechat").setAction("select").putExtra("index", index).buildTouchReport());
		} else {
			mSpeechTaskId = TtsManager.getInstance().speakVoice(fromVoice, mVoiceUrl,
					PreemptType.PREEMPT_TYPE_IMMEADIATELY, new ITtsCallback() {
						@Override
						public void onSuccess() {
							selectCancel(false);
							WeixinManager.getInstance().makeSession(wcc.name, wcc.id);
							super.onSuccess();
						}
					});
			return;
		}
		RecorderWin.close();
	}

	@Override
	protected void onAsrComplexSelect(AsrComplexSelectCallback acsc) {
		acsc.addCommand("CMD_SEND", "发送");
	}

	@Override
	public void selectSure(boolean fromVoice) {
		if (!mUseAutoPerform && !mCallMakeSure) {
			return;
		}
		selectSureDirect();
	}

	@Override
	protected boolean onWakeupItemSelect(boolean isWakeupResult, String type, String command) {
		if ("CMD_SEND".equals(type)) {
			selectSure(false);
			return true;
		}
		return false;
	}

	@Override
	protected boolean onWakeupIndexSelect(boolean isWakeupResult, List<Integer> indexs, String command) {
		return false;
	}

	@Override
	protected void onCommandSelect(List tmp, int index, String speech) {
		String hintTxt = getVoiceTts(tmp, index, "");
		onItemSelect(tmp.get(index), index, hintTxt);
	}
}
