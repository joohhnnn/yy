package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.data.UiData;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResMobilePage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.Contact;
import com.txznet.txz.util.CallUtil;
import com.txznet.txz.util.KeywordsParser;

import android.text.TextUtils;

public class CallWorkChoice extends WorkChoice<CallWorkChoice.Contacts, MobileContact> {
	static final int TOTAL_AUTO_CALL_TIME = 4000;
	public static class Contacts {
		public int event;
		public MobileContacts cons;
	}

	private String strPrefix;
	private String strName;
	private String strSuffix;
	private boolean isSelectAgain;
	private boolean isMultilName = false;
	public static String mLastHintTts = null;
	private static boolean bHoldOriginal = false;

	public CallWorkChoice(CompentOption<MobileContact> option) {
		super(option);
	}
	
	public static void setHoldOriginal(boolean bHold) {
		LogUtil.logd("setHoldOriginal:" + bHold);
		bHoldOriginal = bHold;
	}

	@Override
	public void showChoices(Contacts con) {
		RecorderWin.showUserText();
		int mSourceEvent = con.event;
		LogUtil.logd("showConChoice event:" + mSourceEvent + ",bHoldOriginal:" + bHoldOriginal);
		MobileContacts mMobileContacts = con.cons;
		String strSpeakText = "";

		strName = mMobileContacts.cons[0].name;
		strSuffix = "";
		isMultilName = false;

		boolean mCallMakeSureAsr = false;
		boolean mCanAutoCall = false;

		Boolean canProgress = CallManager.getInstance().canCallProgress();
		boolean isProgress = canProgress == null || canProgress;
		int numCount = 0;//电话号码数量

		for(int i = 0; i < mMobileContacts.cons.length; i++){
			if(mMobileContacts.cons[i] != null && mMobileContacts.cons[i].phones != null){
				numCount += mMobileContacts.cons[i].phones.length;
			}
		}
		switch (mSourceEvent) {
		case UiMakecall.SUBEVENT_MAKE_CALL_DIRECT:
			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
			if (!TextUtils.isEmpty(strName)) {
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK");
				strSpeakText = strSpeakText.replace("%NAME%", strName);
			}else {
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK_OLD");
			}
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			if (!isProgress) {
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_FOUND");
				strSpeakText = strSpeakText.replace("%NAME%", strName);
			}
			mCallMakeSureAsr = true;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER:
			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY");
			if (isSelectAgain) {
				strPrefix = strName + NativeData.getResString("RS_CALL_MULITIPLE_SELECT").replace("%NUM%",
						String.valueOf(mMobileContacts.cons[0].phones.length));
			}
			String phoneNum = mMobileContacts.cons[0].phones[0];
			UiData.Resp_PhoneArea phoneArea = NativeData.getPhoneInfo(phoneNum);
			String numberInfo = phoneNum;
			String numberNum = "" + mMobileContacts.cons[0].phones.length;
			boolean bSameHead = false, bSameTail = false, bSameArea = false, bSameIsp = false, bIsShort = false;
			if (phoneNum.length() >= 3 && phoneNum.length() <= 6) {
				bIsShort = true;
			}
			if (phoneArea == null || phoneArea.strProvince == null || phoneArea.strCity == null) {
				bSameArea = true;
			}
			if (phoneArea == null || phoneArea.strIsp == null) {
				bSameIsp = true;
			}
			for (int i = 1; i < mMobileContacts.cons[0].phones.length; ++i) {
				String phoneNumIndex = mMobileContacts.cons[0].phones[i];
				UiData.Resp_PhoneArea phoneAreaIndex = NativeData.getPhoneInfo(phoneNumIndex);
				if (phoneNumIndex.length() >= 3 && phoneNum.length() >= 3
						&& phoneNumIndex.substring(0, 3).equals(phoneNum.substring(0, 3))) {
					bSameHead = true;
				}
				if (bSameIsp == false && phoneAreaIndex != null && phoneArea.strIsp.equals(phoneAreaIndex.strIsp)) {
					bSameIsp = true;
				}
				if (bSameArea == false && phoneAreaIndex != null
						&& phoneArea.strProvince.equals(phoneAreaIndex.strProvince)
						&& phoneArea.strCity.equals(phoneAreaIndex.strCity)) {
					bSameArea = true;
				}
				if (phoneNumIndex.length() >= 4 && phoneNum.length() >= 4 && phoneNumIndex
						.substring(phoneNumIndex.length() - 4).equals(phoneNum.substring(phoneNum.length() - 4))) {
					bSameTail = true;
				}
				if (bIsShort == true && phoneNumIndex.length() >= 3 && phoneNumIndex.length() <= 6) {
					bIsShort = false;
				}
			}
			if (!bSameArea) {
				numberInfo = phoneArea.strProvince + phoneArea.strCity;
			} else if (!bSameIsp) {
				numberInfo = phoneArea.strIsp;
			} else if (bIsShort) {
				numberInfo = "短号";
			} else if (!bSameHead) {
				if (phoneNum.length() >= 3) {
					numberInfo = CallUtil.converToSpeechDigits(phoneNum.substring(0, 3)) + "开头";
				}
			} else if (!bSameTail) {
				if (phoneNum.length() >= 4) {
					numberInfo = CallUtil.converToSpeechDigits(phoneNum.substring(phoneNum.length() - 4)) + "结尾";
				}
			}
			String sst = NativeData.getResString("RS_CALL_MAKE_CALL_LIST_NUMBER")
					.replace("%COUNT%", numberNum);
			if (isSelectAgain) {
				sst = NativeData.getResString("RS_CALL_MAKE_CALL_AUTO").replace("%NAME%", strName);
				sst = sst.replace("%COUNT%", numberNum);
				sst = sst.replace("%NUMBER%", numberInfo);
			}
			if (!isProgress) {
				sst = NativeData.getResString("RS_CALL_MAKE_CALL_MULTI_NOPROGRESS").replace("%NAME%", strName);
				sst = sst.replace("%COUNT%", numberNum);
				sst += NativeData.getResString("RS_CALL_MAKE_CALL_SELECT_CALL");
				mCallMakeSureAsr = false;
			}
			strSpeakText = sst;
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_CHECK:
			if (mMobileContacts.cons[0].phones.length > 1) {
				strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY");
				String num = "" + mMobileContacts.cons[0].phones.length;
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_LIST_NUMBER")
						.replace("%COUNT%", num);
			} else {
				strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK");
				strSpeakText = strSpeakText.replace("%NAME%", strName);
				mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
				mCallMakeSureAsr = true;
			}
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER_DIRECT:
			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK");
			strSpeakText = strSpeakText.replace("%NAME%", strName);
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			mCallMakeSureAsr = true;
			if (!isProgress) {
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_FOUND").replace("%NAME%", strName);
			}
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_NUMBER:
			mCallMakeSureAsr = true;
//			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
			strName = mMobileContacts.cons[0].phones[0];
//			strPrefix = "请核对号码" + strName;
			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_CHECK_HINT").replace("%NUMBER%", strName);
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_CHECK").replace("%NUMBER%",
					CallUtil.converToSpeechDigits(strName));
//			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
//			strName = mMobileContacts.cons[0].phones[0];
//			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK");
			mLastHintTts = NativeData.getResString("RS_CALL_MAKE_CALL_SURE").replace("%NAME%", strName);
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_LIST:
			strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY");
			strName = "";
			strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_LIST_NUMBER")
					.replace("%COUNT%", numCount+"");
			isMultilName = true;
			if (!bHoldOriginal) {
				resolvedContact(mMobileContacts);
			}
			break;
		case UiMakecall.SUBEVENT_MAKE_CALL_CANDIDATE:
			strName = "";
			isMultilName = true;
			if (!bHoldOriginal) {
				resolvedContact(mMobileContacts);
			}

			if (mMobileContacts.cons.length > 1) {
				strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY");
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_LIST_NUMBER")
						.replace("%COUNT%", numCount+"");

			} else {
				mCallMakeSureAsr = true;
				strPrefix = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_DISPLAY");
				strSpeakText = NativeData.getResString("RS_CALL_MAKE_CALL_DIRECT_ASK_OLD").replace("%NAME%", strName);
			}
//			mCanAutoCall = true;
			if (!isProgress) {
				strSpeakText = NativeData.getResString("RS_CALL_FIND_CONTACTS");
				strSpeakText += NativeData.getResString("RS_CALL_WITCH");
			}
			break;
		}
		
		if (isProgress) {
			Integer delay = getOption().getProgressDelay();
			if (mCanAutoCall) {
				if (delay == null) {
					getOption().setProgressDelay(TOTAL_AUTO_CALL_TIME);
				}
			} else {
				getOption().setProgressDelay(null);
			}
		} else {
			mCanAutoCall = false;
			getOption().setProgressDelay(null);
		}
		
		if (getOption().getCanSure() == null) {
			getOption().setCanSure(mCallMakeSureAsr);
		}
		if (getOption().getTtsText() == null) {
			getOption().setTtsText(strSpeakText);
		}
		if (getOption().getTimeout() == null) {
			getOption().setTimeout(Long.valueOf(SELECT_OUT_TIME + ""));
		}

		super.showChoices(con);
	}
	
	private void resolvedContact(MobileContacts cons) {
		if (cons == null || cons.cons == null) {
			return;
		}

		for (int i = 0; i < cons.cons.length; i++) {
			MobileContact con = cons.cons[i];
			resolvedContact(cons, con, i);
		}
	}
	
	private void resolvedContact(MobileContacts cons, MobileContact con, int idx) {
		if (con.phones == null || con.phones.length <= 1) {
			return;
		}
		List<MobileContact> conList = Arrays.asList(cons.cons);
		conList = new ArrayList<MobileContact>(conList);
		conList.remove(idx);

		int len = con.phones.length;
		for (int i = 0; i < len; i++) {
			MobileContact mc = new MobileContact();
			mc.name = con.name;
			mc.score = con.score;
			mc.phones = new String[] { con.phones[i] };
			mc.uint32LastTimeContacted = con.uint32LastTimeContacted;
			mc.uint32LastTimeUpdated = con.uint32LastTimeUpdated;
			mc.uint32TimesContacted = con.uint32TimesContacted;
			conList.add(idx++, mc);
		}
		cons.cons = conList.toArray(new MobileContact[conList.size()]);
	}

	private static class CmdHelper {
		public String aimKw;
		// 判断是否是同样的字段
		public boolean bSame = true;
		private List<String> cmds = new ArrayList<String>();
		private List<Integer> idxs = new ArrayList<Integer>();

		public CmdHelper(String aimKw) {
			this.aimKw = aimKw;
			cmds.add(aimKw);
		}
		
		public void addCmd(String cmd) {
			if (cmds.contains(cmd)) {
				return;
			}
			if (cmds.size() > 0 && !cmds.contains(cmd)) {
				bSame = false;
			}
			this.cmds.add(cmd);
		}
		
		public void addIdx(int idx) {
			idxs.add(idx);
		}
		
		public List<String> getCmds(){
			return cmds;
		}
		
		public List<Integer> getIdxs(){
			return idxs;
		}

		public void regWakeupCmd(AsrComplexSelectCallback acsc) {
			for (Integer id : idxs) {
				acsc.addIndex(id, cmds.toArray(new String[cmds.size()]));
			}
		}
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, Contacts data) {
		super.onAddWakeupAsrCmd(acsc, data);
		if (needSureCmd()) {
			acsc.addCommand("SURE", "呼叫", "确定","确认");
		}
		acsc.addCommand("CANCEL", NativeData.getResStringArray("RS_CMD_CALL_SELECT_CANCEL"));

		// 插词的时候用全部的数据，而不是分页的数据
		final MobileContacts cons = mData.cons;
		do {
			boolean bSameName = false;
			if (cons.cons.length > 1) {
				bSameName = true;
				for (int i = 1; i < cons.cons.length; ++i) {
					if (!TextUtils.equals(cons.cons[0].name,cons.cons[i].name)){
						bSameName = false;
						break;
					}
				}
				if (!bSameName) {
					Map<String, Integer> names = new HashMap<String, Integer>();
					List<CmdHelper> helpers = new ArrayList<CallWorkChoice.CmdHelper>();
					for (int i = 0; i < cons.cons.length; ++i) {
						for (String kw : KeywordsParser.splitKeywords(cons.cons[i].name))
							// wakeupAsr.addIndex(i, kw);
							onAddName(names, /*sames, */i, kw, helpers);
						//				String phoneNum = cons.cons[i].phones[0];
						//				if (phoneNum.length() > 3) {
						//					if (phoneNum.startsWith("+86")) {
						//						phoneNum = phoneNum.substring(3);
						//					}
						//					phoneNum = phoneNum.replaceAll("-", "");
						//					String headNum = phoneNum.substring(0, 3);
						//					acsc.addIndex(i, headNum, headNum + "开头");
						//					if (phoneNum.length() > 4) {
						//						acsc.addIndex(i, phoneNum.substring(phoneNum.length() - 4));
						//						acsc.addIndex(i, phoneNum.substring(phoneNum.length() - 4) + "结尾");
						//						if (phoneNum.length() == 11) {
						//							acsc.addIndex(i, phoneNum.substring(3, phoneNum.length() - 4));
						//						}
						//					}
						//				}
						//				// 座机规则那个
						//				int phoneType = NativeData.getPhoneType(phoneNum);
						//				JNIHelper.logd(phoneNum + " phone type: " + phoneType);
						//				switch (phoneType) {
						//				case 1:
						//					acsc.addIndex(i, "短号");
						//					break;
						//				case 2:
						//					acsc.addIndex(i, "手机");
						//					break;
						//				case 3:
						//					acsc.addIndex(i, "座机");
						//					break;
						//				}
						UiData.Resp_PhoneArea phoneArea = getContactPhoneInfo(cons.cons[i]);
						if (phoneArea != null) {
							if (phoneArea.strCity != null) {
								acsc.addIndex(i, phoneArea.strCity);
							}
							if (phoneArea.strProvince != null)
								acsc.addIndex(i, phoneArea.strProvince);
							// 插入ISP运营商
							if (phoneArea.strIsp != null)
								acsc.addIndex(i, phoneArea.strIsp);
						}
					}
					addNameWakeAsr(acsc, names, /*sames, */helpers);
					break;
				}
			}
			int phoneSize = cons.cons[0].phones.length;
			String[] phones = cons.cons[0].phones;
			if (bSameName) {
				phoneSize = cons.cons.length;
				phones = new String[phoneSize];
				for (int i = 0; i < phoneSize; i++) {
					phones[i] = cons.cons[i].phones[0];
				}
			}
			if (phoneSize > 1) {
				for (int i = 0; i < phoneSize; ++i) {
					// 号码规则
					String phoneNum = phones[i];
					LogUtil.logd("onAddWakeupAsrCmd phoneNum:" + phoneNum);
					if (phoneNum.length() > 3) {
						if (phoneNum.startsWith("+86")) {
							phoneNum = phoneNum.substring(3);
						}
						phoneNum = phoneNum.replaceAll("-", "");
						String headNum = phoneNum.substring(0, 3);
						acsc.addIndex(i, headNum, headNum + "开头","开头是"+headNum);
						//再插入一份1读成妖的
						if ( !TextUtils.isEmpty(headNum) && (headNum.indexOf('1') != -1)) {
							headNum = CallUtil.converToWakeupKeyword(headNum);
							acsc.addIndex(i, headNum, headNum + "开头","开头是"+headNum);
						}
						if (phoneNum.length() > 4) {
							//先插入一份普通的数字
							String lastNum = phoneNum.substring(phoneNum.length() - 4);
							acsc.addIndex(i, lastNum, lastNum + "结尾", "尾号是" + lastNum);
							//再插入一份1读成妖的
							if ( !TextUtils.isEmpty(lastNum) && (lastNum.indexOf('1') != -1)) {
								lastNum = CallUtil.converToWakeupKeyword(lastNum);
								acsc.addIndex(i, lastNum, lastNum + "结尾", "尾号是" + lastNum);
							}
							if (phoneNum.length() == 11) {
								acsc.addIndex(i, phoneNum.substring(3, phoneNum.length() - 4));
							}
						}
					}
					// 座机规则那个
					//				int phoneType = NativeData.getPhoneType(phoneNum);
					//				JNIHelper.logd(phoneNum + " phone type: " + phoneType);
					//				switch (phoneType) {
					//				case 1:
					//					acsc.addIndex(i, "短号");
					//					break;
					//				case 2:
					//					acsc.addIndex(i, "手机");
					//					break;
					//				case 3:
					//					acsc.addIndex(i, "座机");
					//					break;
					//				}
					// 归属地运营商规则
					UiData.Resp_PhoneArea phoneArea = NativeData.getPhoneInfo(phoneNum);
					if (phoneArea != null && phoneArea.bResult != null && phoneArea.bResult) {
						if (phoneArea.strIsp != null)
							acsc.addIndex(i, phoneArea.strIsp);
						if (phoneArea.strCity != null) {
							acsc.addIndex(i, phoneArea.strCity);
						}
						if (phoneArea.strProvince != null)
							acsc.addIndex(i, phoneArea.strProvince);
					}
				}
			}

		} while (false);

		if (ProjectCfg.isSupportCorrectSpeech()) {
			acsc.addCommand(TYPE_WAKEUP_CORRECT_SPEECH, NativeData.getResStringArray("RS_CALL_CMD_CORRECT_SPEECH"));
		}
	}

	private static final String TYPE_WAKEUP_CORRECT_SPEECH = "CORRECT_SPEECH";

	private static void onAddName(Map<String, Integer> names, /*List<String> sames, */int index, String kws,
			List<CmdHelper> helpers) {
		if (names.isEmpty()) {
			names.put(kws, index);
			return;
		}

		boolean found = false;
		for (String name : names.keySet()) {
			boolean bSame = NativeData.compareStringWithPinyin(name, kws, 6867);
			if (bSame) {
				found = true;
				CmdHelper aimHelper = null;
				for (CmdHelper helper : helpers) {
					if (helper.aimKw.equals(name)) {
						aimHelper = helper;
						break;
					}
				}
				if (aimHelper != null) {
					aimHelper.addCmd(kws);
					aimHelper.addIdx(index);
				} else {
					aimHelper = new CmdHelper(name);
					aimHelper.addCmd(kws);
					aimHelper.addIdx(index);
					helpers.add(aimHelper);
				}
				LogUtil.logd("same name:" + name + ",kws:" + kws + ",idx:" + index);
				break;
			}
		}

		if (!found) {
			names.put(kws, index);
		}
	}

	Random mRandom = new Random();
	@Override
	protected boolean onCommandSelect(String type, String command) {
		if (TYPE_WAKEUP_CORRECT_SPEECH.equals(type)) {
			clearIsSelecting();
			RecorderWin.open(NativeData.getResString("RS_VOICE_CORRECT_SPEECH_TIPS"), VoiceData.GRAMMAR_SENCE_MAKE_CALL);
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.putExtra("scene", "call")
					.putExtra("command", command)
					.buildCommReport());
			return true;
		}
		return super.onCommandSelect(type, command);
	}

	private static void addNameWakeAsr(AsrComplexSelectCallback acsc, Map<String, Integer> names, /*List<String> sames,*/
			List<CmdHelper> helpers) {
		if (helpers.size() == 1 && names.size() == 1) {
			if (helpers.get(0).bSame) {
				for (String n : names.keySet()) {
					if (helpers.get(0).aimKw.equals(n)) {
						// 同名的不注册
						return;
					}
				}
			}
		}
		
		for (CmdHelper helper : helpers) {
			helper.regWakeupCmd(acsc);
		}
		for (String name : names.keySet()) {
			acsc.addIndex(names.get(name), name);
		}
	}
	
	@Override
	protected void onClearSelecting() {
		isSelectAgain = false;
		super.onClearSelecting();
	}

	@Override
	protected void onConvToJson(Contacts con, JSONBuilder jsonBuilder) {
		final MobileContacts ts = con.cons;
		List<Contact> list = new ArrayList<Contact>();
		int count = isMultilName ? ts.cons.length : ts.cons[0].phones.length;
//		if (count > 9) {
//			count = 9;
//		}

		for (int i = 0; i < count; i++) {
			Contact info = new Contact();
			if (isMultilName) {
				info.name = ts.cons[i].name;
				info.number = ts.cons[i].phones[0];
				UiData.Resp_PhoneArea phoneInfo = getContactPhoneInfo(ts.cons[i]);
				if (phoneInfo != null) {
					info.province = phoneInfo.strProvince;
					info.city = phoneInfo.strCity;
					info.isp = phoneInfo.strIsp;
				}
//				if (TextUtils.isEmpty(info.province)) {
//					int phoneType = NativeData.getPhoneType(info.number);
//					switch (phoneType) {
//					case 1:
//						info.province = "短号";
//						break;
//					case 2:
//						info.province = "手机";
//						break;
//					case 3:
//						info.province = "座机";
//						break;
//					}
//				}
			} else {
				info.name = ts.cons[0].name;
				info.number = ts.cons[0].phones[i];
				UiData.Resp_PhoneArea phoneInfo = NativeData.getPhoneInfo(info.number);
				if (phoneInfo != null) {
					info.province = phoneInfo.strProvince;
					info.city = phoneInfo.strCity;
					info.isp = phoneInfo.strIsp;
				}
//				if (TextUtils.isEmpty(info.province)) {
//					int phoneType = NativeData.getPhoneType(info.number);
//					switch (phoneType) {
//					case 1:
//						info.province = "短号";
//						break;
//					case 2:
//						info.province = "手机";
//						break;
//					case 3:
//						info.province = "座机";
//						break;
//					}
//				}
			}
			list.add(info);
		}

		putContactInfo(jsonBuilder, list);
	}

	@Override
	protected void onSelectIndex(MobileContact item, boolean fromPage, int idx, String fromVoice) {
		clearIsSelecting();
		if (item == null) {
			JNIHelper.loge("wrong contact data");
			return;
		}
		if (!TextUtils.isEmpty(fromVoice)) {
			if (fromVoice.contains("那个")) {
				fromVoice = fromVoice.replace("那个", "的");
			}
		}
		clearProgress();
		String hintTts = null;
		String hintShow = null;
		CallAfterTts callAfterTts = null;
		String number = "";

		if (isMultilName) {
			if (item.phones.length == 1) {
				clearProgress();
				number = item.phones[0];
				callAfterTts = new CallAfterTts(item.phones[0], item.name);
				String tts = item.name;
				if (tts != null && tts.length() >= 30) {
					tts = "";
				}
				if (fromVoice != null && Pattern.matches("^\\d{3}.*", fromVoice)) {
					tts = fromVoice + "号码";
				}
				if (fromVoice != null) {
					//hintTts = NativeData.getResString("RS_CALL_NAME").replace("%NAME%", tts);
					hintTts = NativeData.getResString("RS_CALL_NUMBER");
				}
			} else {
				isSelectAgain = true;
				mData.event = UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER;
				MobileContact con = item;
				mData.cons.cons = new MobileContact[1];
				mData.cons.cons[0] = con;
				getOption().setCanSure(null);
				getOption().setTtsText(null);
				showChoices(mData);
				if (TextUtils.isEmpty(fromVoice)) {
					// 数据上报
					ReportUtil.doReport(new ReportUtil.Report.Builder().setType("call").setAction("select")
							.putExtra("index", idx).buildTouchReport());
				}
				return;
			}
		} else if (idx >= 0 && idx < item.phones.length) {
			clearProgress();
			number = item.phones[idx];
			callAfterTts = new CallAfterTts(item.phones[idx], item.name);
			if (fromVoice != null) {
				if (item.phones.length > 1) {
					//hintTts = NativeData.getResString("RS_CALL_NUMBER").replace("%NUMBER%",
					//		CallUtil.converToSpeechDigits(fromVoice));
					hintTts = NativeData.getResString("RS_CALL_NUMBER");
					//hintShow = NativeData.getResString("RS_CALL_NUMBER").replace("%NUMBER%", fromVoice);
					hintShow = hintTts;//NativeData.getResString("RS_CALL_NUMBER");
				} else {
					String tts = item.name;
					if (tts != null && tts.length() >= 30) {
						tts = "";
					}
					if (tts == null) {
						tts = item.phones[idx].replace("-", "").trim();
					}
					hintTts = NativeData.getResString("RS_CALL_NAME").replace("%NAME%", CallUtil.converToSpeechDigits(tts));
					hintShow = NativeData.getResString("RS_CALL_NAME").replace("%NAME%", tts);
				}
			}
		}
		if (hintShow == null) {
			hintShow = hintTts;
		}

		if (fromVoice != null) {
			if (TextUtils.isEmpty(number)) {
				AsrManager.getInstance().setNeedCloseRecord(false);
				RecorderWin.speakTextWithClose("该联系人未添加电话号码",null);
			} else {
				RecorderWin.addSystemMsg(hintShow);
				mSpeechTaskId = TtsManager.getInstance().speakText(hintTts, PreemptType.PREEMPT_TYPE_IMMEADIATELY,
						callAfterTts);
			}
		} else {
			if (TextUtils.isEmpty(number)) {
				AsrManager.getInstance().setNeedCloseRecord(false);
				RecorderWin.speakTextWithClose("该联系人未添加电话号码",null);
			} else {
				if (callAfterTts != null) {
					callAfterTts.onSuccess();
				}
			}

		}

		if (TextUtils.isEmpty(fromVoice)) {
			// 数据上报
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("call").setAction("select")
					.putExtra("page", mPage.getCurrPage()).putExtra("index", idx).buildTouchReport());
		}
	}

	static class CallAfterTts extends ITtsCallback {
		String mNumber;
		String mName;

		public CallAfterTts(String num, String name) {
			mNumber = num;
			mName = name;
		}

		@Override
		public void onSuccess() {

			CallManager.getInstance().makeCall(mNumber, mName);
			RecorderWin.close();
			super.onSuccess();
		}
	};
	
	@Override
	protected int getSenceGrammar() {
		return VoiceData.GRAMMAR_SENCE_CALL_SELECT;
	}

	@Override
	protected boolean onIndexSelect(List<Integer> indexs, String command) {
		final MobileContacts cons = mData.cons;
		if (indexs.size() != 1) {
			if (cons == null || cons.cons == null)
				return true;
			String newCommand;
			if (command.matches("^\\d+((开头)|(结尾)?那个)?$")) {
				newCommand = CallUtil.converToSpeechDigits(command);
			} else if (command.endsWith("那个"))
				newCommand = command.substring(0, command.length() - 2);
			else {
				newCommand = command;
			}
			MobileContacts newCons = new MobileContacts();
			int newEvent = 0;
			if (cons.cons.length > 1) {
				newCons.cons = new MobileContact[indexs.size()];
				for (int i = 0; i < indexs.size(); ++i) {
					newCons.cons[i] = cons.cons[indexs.get(i)];
				}
				newEvent = UiMakecall.SUBEVENT_MAKE_CALL_LIST;
			} else {
				if (cons.cons.length == 0)
					return true;
				if (cons.cons[0] == null || cons.cons[0].phones == null || cons.cons[0].phones.length < 2)
					return true;
				newCons.cons = new MobileContact[1];
				newCons.cons[0] = cons.cons[0];
				String[] newPhones = new String[indexs.size()];
				for (int i = 0; i < indexs.size(); ++i) {
					newPhones[i] = cons.cons[0].phones[indexs.get(i)];
				}
				isSelectAgain = true;
				newCons.cons[0].phones = newPhones;
				newEvent = UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER;
			}
			String spk = NativeData.getResPlaceholderString("RS_CALL_RESELECR", "%NUM%", indexs.size() + "");
			mData.event = newEvent;
			mData.cons = newCons;
			mCompentOption.setTtsText(spk);
			updateCompentOption(mCompentOption, true);
			clearCallMakeSureAsr();
			return true;
		}
		return super.onIndexSelect(indexs, command);
	}

	private void clearCallMakeSureAsr() {
		mCompentOption.setCanSure(false);
		clearProgress();
	}

	private JSONBuilder putContactInfo(JSONBuilder doc, List<Contact> list) {
		doc.put("type", RecorderWin.CallSence);
		doc.put("strPrefix", strPrefix);
		doc.put("strName", strName);
		doc.put("strSuffix", strSuffix);
		doc.put("isMultiName", isMultilName);
		doc.put("prefix", strPrefix);
		doc.put("count",list.size());

		if (mPage.getTotalSize() == ProjectCfg.getMaxShowContactCount() && mPage.getCurrPage() == (mPage.getMaxPage() -1)){
			doc.put("strPrefix", NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY_END"));
			doc.put("prefix", NativeData.getResString("RS_CALL_MAKE_CALL_FINDLIST_DISPLAY_END"));
		}

		List<JSONObject> contacts = new ArrayList<JSONObject>();
		for (int i = 0; i < list.size(); i++) {
			Contact info = list.get(i);
			JSONObject contact = new JSONBuilder().put("name", info.name).put("number", info.number)
					.put("province", info.province).put("city", info.city).put("isp", info.isp).build();
			contacts.add(contact);
		}

		doc.put("contacts", contacts.toArray());
		doc.put("vTips",getTips());
		return doc;
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getMaxPage() == 1) {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_ONE");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_TWO");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_MORE");
					}
				} else {
					if (mPage.getCurrPageSize() == 1) {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_ONE_LAST");
					} else if (mPage.getCurrPageSize() == 2) {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_TWO_LAST");
					} else {
						tips = NativeData.getResString("RS_VOICE_TIPS_CALL_MORE_LAST");
					}
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_CALL_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_CALL_OTHER_PAGE");
			}
		}
		return tips;
	}

	// 获取联系人的手机信息
	public static UiData.Resp_PhoneArea getContactPhoneInfo(MobileContact con) {
		if (con == null)
			return null;
		if (con.phones == null || con.phones.length == 0)
			return null;
		UiData.Resp_PhoneArea ret = NativeData.getPhoneInfo(con.phones[0]);
		if (ret == null || ret.bResult == null || ret.bResult == false)
			return null;
		if (ret.strProvince == null || ret.strCity == null)
			return null;
		for (int i = 1; i < con.phones.length; ++i) {
			UiData.Resp_PhoneArea reti = NativeData.getPhoneInfo(con.phones[i]);
			if (reti == null || reti.bResult == null || reti.bResult == false)
				return null;
			if (!ret.strProvince.equals(reti.strProvince))
				return null;
			if (!ret.strCity.equals(reti.strCity))
				return null;
			if (ret.strIsp != null && !ret.strIsp.equals(reti.strIsp))
				ret.strIsp = null;
		}
		return ret;
	}

	@Override
	protected ResourcePage<Contacts, MobileContact> createPage(Contacts cons) {
		int totalSize = isMultilName ? cons.cons.cons.length : cons.cons.cons[0].phones.length;
		return new ResMobilePage(cons, totalSize, isMultilName) {
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
	public String getReportId() {
		return "Call_Contact_Select";
	}
	
	@Override
	protected String convItemToString(MobileContact item) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("name", item.name);
		jsonBuilder.put("phones", item.phones);
		jsonBuilder.put("score", item.score);
		jsonBuilder.put("uint32LastTimeContacted", item.uint32LastTimeContacted);
		jsonBuilder.put("uint32LastTimeUpdated", item.uint32LastTimeUpdated);
		jsonBuilder.put("uint32TimesContacted", item.uint32TimesContacted);
		return jsonBuilder.toString();
	}
}
