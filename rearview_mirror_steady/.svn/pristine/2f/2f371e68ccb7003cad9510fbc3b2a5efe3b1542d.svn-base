package com.txznet.sdk.bean;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZNavManager.CallBack;
import com.txznet.sdk.TXZResourceManager;

import android.content.Intent;
import android.util.Log;

public class NavVoicePlugin {
	public static final String RS_MAP_HINT_GUOYU_MM = "将为您切换国语女声";
	public static final String RS_MAP_HINT_GUOYU_GG = "将为您切换国语男声";
	public static final String RS_MAP_HINT_ZHOUXINGXING = "将为您切换周星星的声音";
	public static final String RS_MAP_HINT_GUANGDONGHUA = "将为您切换广东话";
	public static final String RS_MAP_HINT_LINZHILIN = "将为您切换林志玲的声音";
	public static final String RS_MAP_HINT_GUODEGANG = "将为您切换郭德纲的声音";
	public static final String RS_MAP_HINT_DONGBEIHUA = "将为您切换东北话";
	public static final String RS_MAP_HINT_HENANHUA = "将为您切换河南话";
	public static final String RS_MAP_HINT_HUNANHUA = "将为您切换湖南话";
	public static final String RS_MAP_HINT_SICHUANHUA = "将为您切换四川话";
	public static final String RS_MAP_HINT_TAIWANHUA = "将为您切换台湾话";

	public static final String RS_MAP_HINT_IS_GUOYU_MM = "现在已是国语女声在为您播报";
	public static final String RS_MAP_HINT_IS_GUOYU_GG = "现在已是国语男声在为您播报";
	public static final String RS_MAP_HINT_IS_ZHOUXINGXING = "现在已是周星驰在为您播报";
	public static final String RS_MAP_HINT_IS_GUANGDONGHUA = "现在已是广东话在为您播报";
	public static final String RS_MAP_HINT_IS_LINZHILIN = "现在已是林志玲在为您播报";
	public static final String RS_MAP_HINT_IS_GUODEGANG = "现在已是郭德纲在为您播报";
	public static final String RS_MAP_HINT_IS_DONGBEIHUA = "现在已是东北话在为您播报";
	public static final String RS_MAP_HINT_IS_HENANHUA = "现在已是河南话在为您播报";
	public static final String RS_MAP_HINT_IS_HUNANHUA = "现在已是湖南话在为您播报";
	public static final String RS_MAP_HINT_IS_SICHUANHUA = "现在已是四川话在为您播报";
	public static final String RS_MAP_HINT_IS_TAIWANHUA = "现在已是台湾话在为您播报";

	public static final int ROLE_GUOYU_MM = 0;
	public static final int ROLE_GUOYU_GG = 1;
	public static final int ROLE_ZHOUXINGXING = 2;
	public static final int ROLE_GUANGDONGHUA = 3;
	public static final int ROLE_LINZHILIN = 4;
	public static final int ROLE_GUODEGANG = 5;
	public static final int ROLE_DONGBEIHUA = 6;
	public static final int ROLE_HENANHUA = 7;
	public static final int ROLE_HUNANHUA = 8;
	public static final int ROLE_SICHUANHUA = 9;
	public static final int ROLE_TAIWANHUA = 10;

	CallBack mCallBack;
	boolean mIsRegister;
	AsrComplexSelectCallback mAcsc;

	public void setNavVoiceCmdCallback(CallBack callBack) {
		mCallBack = callBack;
	}

	public void registerVoiceCmds(final String packageName) {
		if (mIsRegister) {
			return;
		}

		if (!packageName.startsWith("com.autonavi.")) {
			return;
		}

		mAcsc = new AsrComplexSelectCallback() {

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "TASK_VOICE_CMD";
			}

			@Override
			public void onCommandSelected(String type, String command) {
				if (AsrKeyType.GUOYU_MM.equals(type)) {
					switchRole(isWakeupResult(), ROLE_GUOYU_MM);
					return;
				}
				if (AsrKeyType.GUOYU_GG.equals(type)) {
					switchRole(isWakeupResult(), ROLE_GUOYU_GG);
					return;
				}
				if (AsrKeyType.ZHOUXINGXING.equals(type)) {
					switchRole(isWakeupResult(), ROLE_ZHOUXINGXING);
					return;
				}
				if (AsrKeyType.GUANGDONGHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_GUANGDONGHUA);
					return;
				}
				if (AsrKeyType.LINZHILIN.equals(type)) {
					switchRole(isWakeupResult(), ROLE_LINZHILIN);
					return;
				}
				if (AsrKeyType.GUODEGANG.equals(type)) {
					switchRole(isWakeupResult(), ROLE_GUODEGANG);
					return;
				}
				if (AsrKeyType.DONGBEIHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_DONGBEIHUA);
					return;
				}
				if (AsrKeyType.HENANHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_HENANHUA);
					return;
				}
				if (AsrKeyType.HUNANHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_HUNANHUA);
					return;
				}
				if (AsrKeyType.SICHUANHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_SICHUANHUA);
					return;
				}
				if (AsrKeyType.TAIWANHUA.equals(type)) {
					switchRole(isWakeupResult(), ROLE_TAIWANHUA);
					return;
				}
				if (AsrKeyType.SWITCH_ROLE.equals(type)) {
					int role = mRole + 1;
					if (role > 10) {
						role = 0;
					}
					switchRole(isWakeupResult(), role);
					return;
				}
			}
		};

		// 高德
		for (int i = 0; i < cmdTypes.length; i++) {
			String[] cmds = mCallBack.getTypeCmds(cmdTypes[i]);
			if (cmds == null || cmds.length == 0) {
				mAcsc.addCommand(cmdTypes[i], typeCmds[i]);
			} else {
				mAcsc.addCommand(cmdTypes[i], cmds);
			}
		}
		mIsRegister = true;
		AsrUtil.useWakeupAsAsr(mAcsc);
	}

	int mRole;

	private void switchRole(boolean isWakeup, int role) {
		if (mRole == role) {
			TXZResourceManager.getInstance().speakTextOnRecordWin(getTTS(role), true, null);
			return;
		}

		mRole = role;
		if (!isWakeup) {
			TXZResourceManager.getInstance().speakTextOnRecordWin(getSetTTS(role), true, new Runnable() {

				@Override
				public void run() {
					setTtsRole(mRole);
				}
			});
			return;
		}

		setTtsRole(mRole);
	}

	private void setTtsRole(int role) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10044);
		intent.putExtra("VOICE_ROLE", role);
		Log.d("TtsRole", "TtsRole:" + role);
		GlobalContext.get().sendBroadcast(intent);
	}

	private String[] cmdTypes = { AsrKeyType.SWITCH_ROLE, AsrKeyType.GUOYU_MM, AsrKeyType.GUOYU_GG,
			AsrKeyType.ZHOUXINGXING, AsrKeyType.GUANGDONGHUA, AsrKeyType.LINZHILIN, AsrKeyType.GUODEGANG,
			AsrKeyType.DONGBEIHUA, AsrKeyType.HENANHUA, AsrKeyType.HUNANHUA, AsrKeyType.SICHUANHUA,
			AsrKeyType.TAIWANHUA };

	private String[][] typeCmds = { { "切换导航声音" }, { "切换为国语女声", "国语女声来播" }, { "切换为国语男声", "国语男声来播" },
			{ "切换为周星驰", "周星星来播", "周星驰来播", "周星驰出来", "周星星出来", "我想听周星星来播", "我想听周星驰来播" }, { "切换为广东话", "广东话来播" },
			{ "切换为林志玲", "林志玲来播", "林志玲出来", "我想听林志玲来播" }, { "切换为郭德纲", "郭德纲来播", "郭德纲出来", "我想听郭德纲来播" },
			{ "切换为东北话", "东北话来播" }, { "切换为河南话", "河南话来播" }, { "切换为湖南话", "湖南话来播" }, { "切换为四川话", "四川话来播" },
			{ "切换为台湾话", "台湾话来播" } };

	public void registerAgain() {
		if (mAcsc != null && !mIsRegister) {
			mIsRegister = true;
			AsrUtil.useWakeupAsAsr(mAcsc);
		}
	}

	public void unRegisterVoiceCmds() {
		if (mIsRegister) {
			mIsRegister = false;
			AsrUtil.recoverWakeupFromAsr("TASK_VOICE_CMD");
		}
	}

	public void resetAsrTask() {
		mAcsc = null;
		mIsRegister = false;
	}

	public String getTTS(int role) {
		switch (role) {
		case 0:
			return RS_MAP_HINT_IS_GUOYU_MM;
		case 1:
			return RS_MAP_HINT_IS_GUOYU_GG;
		case 2:
			return RS_MAP_HINT_IS_ZHOUXINGXING;
		case 3:
			return RS_MAP_HINT_IS_GUANGDONGHUA;
		case 4:
			return RS_MAP_HINT_IS_LINZHILIN;
		case 5:
			return RS_MAP_HINT_IS_GUODEGANG;
		case 6:
			return RS_MAP_HINT_IS_DONGBEIHUA;
		case 7:
			return RS_MAP_HINT_IS_HENANHUA;
		case 8:
			return RS_MAP_HINT_IS_HUNANHUA;
		case 9:
			return RS_MAP_HINT_IS_SICHUANHUA;
		case 10:
			return RS_MAP_HINT_IS_TAIWANHUA;
		}
		return "";
	}

	public String getSetTTS(int role) {
		switch (role) {
		case 0:
			return RS_MAP_HINT_GUOYU_MM;
		case 1:
			return RS_MAP_HINT_GUOYU_GG;
		case 2:
			return RS_MAP_HINT_ZHOUXINGXING;
		case 3:
			return RS_MAP_HINT_GUANGDONGHUA;
		case 4:
			return RS_MAP_HINT_LINZHILIN;
		case 5:
			return RS_MAP_HINT_GUODEGANG;
		case 6:
			return RS_MAP_HINT_DONGBEIHUA;
		case 7:
			return RS_MAP_HINT_HENANHUA;
		case 8:
			return RS_MAP_HINT_HUNANHUA;
		case 9:
			return RS_MAP_HINT_SICHUANHUA;
		case 10:
			return RS_MAP_HINT_TAIWANHUA;
		}
		return "";
	}
}
