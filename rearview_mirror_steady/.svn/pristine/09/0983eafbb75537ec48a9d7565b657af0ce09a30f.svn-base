package com.txznet.txz.module.ac;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.txz.ui.carcontrol.CarControlData;
import com.txz.ui.carcontrol.CarControlData.ACSettingData;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.ExchangeHelper;

/**
 * 空调管理模块，负责空调的适配
 */
public class ACManager extends IModule {

	private static ACManager sModuleInstance;

	private ACManager() {
	}

	public static ACManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (ACManager.class) {
				if (sModuleInstance == null)
					sModuleInstance = new ACManager();
			}
		}
		return sModuleInstance;
	}
	
	public static final String AC_MGR = "tool.acmgr";
	private static String mRemoteAcMgrToolImpl = null;
	public static boolean hasRemoteAcMgrToolImpl() {
		return !TextUtils.isEmpty(mRemoteAcMgrToolImpl);
	}
	
	@Override
	public int regEvent(int eventId, int subEventId) {
		return super.regEvent(eventId, subEventId);
	}

	@Override
	public int initialize_BeforeStartJni() {
		return super.initialize_BeforeStartJni();
	}
	
	@Override
	public int initialize_AfterStartJni() {

		regCommandWithResult("AC_CMD_TEMPERATURE_CTRLTO");
		regCommandWithResult("AC_CMD_WIND_SPEED_CTRLTO");
		regCommandWithResult("AC_CMD_TEMPERATURE_CTRLUP");
		regCommandWithResult("AC_CMD_TEMPERATURE_CTRLDOWN");
		
		return super.initialize_AfterStartJni();
	}
	
	@Override
	public int onCommand(String cmd) {
		String method = null;
		String data = null;
		if (TextUtils.equals(cmd, "AC_CMD_OPEN")) {
			method = "openAC";
		}else if (TextUtils.equals(cmd, "AC_CMD_EXIT")) {
			method = "closeAC";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_DEFROSTER_F")) {
			method = "openFDef";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_DEFROSTER_A")) {
			method = "openADef";
		}else if (TextUtils.equals(cmd, "AC_CMD_EXIT_DEFROSTER_F")) {
			method = "closeFDef";
		}else if (TextUtils.equals(cmd, "AC_CMD_EXIT_DEFROSTER_A")) {
			method = "closeADef";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_INNER_LOOP")) {
			method = "innerLoop";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_OUTPUT_LOOP")) {
			method = "outLoop";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_AC")) {
			method = "openCompressor";
		}else if (TextUtils.equals(cmd, "AC_CMD_EXIT_AC")) {
			method = "closeCompressor";
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_MAX")) {
			method = "maxTemp";
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_MIN")) {
			method = "minTemp";
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_UP")) {
			method = "incTemp";
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_DOWN")) {
			method = "decTemp";
		}else if (TextUtils.equals(cmd, "AC_CMD_WIND_SPEED_UP")) {
			method = "incWSpeed";
		}else if (TextUtils.equals(cmd, "AC_CMD_WIND_SPEED_DOWN")) {
			method = "decWSpeed";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_BLOW_FACE")) {
			method = "selectMode";
			data = "MODE_BLOW_FACE";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_BLOW_FACE_FOOT")) {
			method = "selectMode";
			data = "MODE_BLOW_FACE_FOOT";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_BLOW_FOOT")) {
			method = "selectMode";
			data = "MODE_BLOW_FOOT";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_BLOW_FOOT_DEFROST")) {
			method = "selectMode";
			data = "MODE_BLOW_FOOT_DEFROST";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_DEFROST")) {
			method = "selectMode";
			data = "MODE_DEFROST";
		}else if (TextUtils.equals(cmd, "AC_CMD_OPEN_AUTO")) {
			method = "selectMode";
			data = "MODE_AUTO";
		}else if (TextUtils.equals(cmd, "AC_CMD_WIND_SPEED_MAX")) {
			method = "maxWSpeed";
		}else if (TextUtils.equals(cmd, "AC_CMD_WIND_SPEED_MIN")) {
			method = "minWSpeed";
		}
		if (method != null) {
			if (!procByRemoteTool(AC_MGR, method,data)) {
				RecorderWin.speakTextWithClose(
						NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
			}
		}
		
		return super.onCommand(cmd);
	}
	
	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		String method = null;
		String data = null;
		String spk = null;
		if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_CTRLTO")) {
			method = "ctrlToTemp";
			data = new JSONBuilder(voiceString).getVal("tempValue", String.class);
			if (!isNumeric(data)) {
				try {
					data = ExchangeHelper.chineseToNumber(data) + "";
				} catch (Exception e) {
				}
			}
			if (mMinTempValue != null && mMaxTempValue != null) {
				try {
						
					int tempValue = Integer.valueOf(data);
					if (tempValue < mMinTempValue) {
						spk = NativeData.getResPlaceholderString("RS_AC_TEMP_MIN",
								"%MIN%",
								mMinTempValue+"");
					}else if (tempValue > mMaxTempValue) {
						spk = NativeData.getResPlaceholderString("RS_AC_TEMP_MAX",
								"%MAX%",
								mMaxTempValue+"");
					}
				} catch (Exception e) {
				}
			}
		}else if (TextUtils.equals(cmd, "AC_CMD_WIND_SPEED_CTRLTO")) {
			method = "ctrlToWSpeed";
			data = new JSONBuilder(voiceString).getVal("gearValue", String.class);
			if (!isNumeric(data)) {
				try {
					data = ExchangeHelper.chineseToNumber(data) + "";
				} catch (Exception e) {
				}
			}
			if (mMinWSpeedValue != null && mMaxWSpeedValue != null) {
				try {
					int gearValue = Integer.valueOf(data);
					if (gearValue < mMinWSpeedValue) {
						spk = NativeData.getResPlaceholderString("RS_AC_WSPEED_MIN",
								"%MIN%",
								mMinWSpeedValue+"");
					}else if (gearValue > mMaxWSpeedValue) {
						spk = NativeData.getResPlaceholderString("RS_AC_WSPEED_MAX",
								"%MAX%",
								mMaxWSpeedValue+"");
					}
				} catch (Exception e) {
				}
			}
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_CTRLUP")) {
			method = "incTemp";
			data = new JSONBuilder(voiceString).getVal("tempRateValue", String.class);
			if (!isNumeric(data)) {
				try {
					data = ExchangeHelper.chineseToNumber(data) + "";
				} catch (Exception e) {
				}
			}
		}else if (TextUtils.equals(cmd, "AC_CMD_TEMPERATURE_CTRLDOWN")) {
			method = "decTemp";
			data = new JSONBuilder(voiceString).getVal("tempRateValue", String.class);
			if (!isNumeric(data)) {
				try {
					data = ExchangeHelper.chineseToNumber(data) + "";
				} catch (Exception e) {
				}
			}
		}
		if (method != null) {
			if (!TextUtils.isEmpty(spk)) {
				RecorderWin.speakTextWithClose(spk, null);
			}else if (!procByRemoteTool(AC_MGR, method,data)) {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), null);
			}
		}
		return super.onCommand(cmd, keywords, voiceString);
	}

	public boolean isNumeric(String str){
		   Pattern pattern = Pattern.compile("[0-9]*");
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false;
		   }
		   return true;
	}

	public byte[] invokeTXZAC(final String packageName, String command,
			byte[] data){
		if (command.equals("acmgr.settool")) {
			mRemoteAcMgrToolImpl = packageName;
			regCmdString("AC_CMD_OPEN", NativeData.getResStringArray("AC_CMD_OPEN"));
			regCmdString("AC_CMD_EXIT", NativeData.getResStringArray("AC_CMD_EXIT"));
			regCmdString("AC_CMD_OPEN_DEFROSTER_F", NativeData.getResStringArray("AC_CMD_OPEN_DEFROSTER_F"));
			regCmdString("AC_CMD_OPEN_DEFROSTER_A", NativeData.getResStringArray("AC_CMD_OPEN_DEFROSTER_A"));
			regCmdString("AC_CMD_EXIT_DEFROSTER_F", NativeData.getResStringArray("AC_CMD_EXIT_DEFROSTER_F"));
			regCmdString("AC_CMD_EXIT_DEFROSTER_A", NativeData.getResStringArray("AC_CMD_EXIT_DEFROSTER_A"));
			regCmdString("AC_CMD_OPEN_INNER_LOOP", NativeData.getResStringArray("AC_CMD_OPEN_INNER_LOOP"));
			regCmdString("AC_CMD_OPEN_OUTPUT_LOOP", NativeData.getResStringArray("AC_CMD_OPEN_OUTPUT_LOOP"));
			regCmdString("AC_CMD_OPEN_AC", NativeData.getResStringArray("AC_CMD_OPEN_AC"));
			regCmdString("AC_CMD_EXIT_AC", NativeData.getResStringArray("AC_CMD_EXIT_AC"));
			regCmdString("AC_CMD_TEMPERATURE_MAX", NativeData.getResStringArray("AC_CMD_TEMPERATURE_MAX"));
			regCmdString("AC_CMD_TEMPERATURE_MIN", NativeData.getResStringArray("AC_CMD_TEMPERATURE_MIN"));
			regCmdString("AC_CMD_TEMPERATURE_UP", NativeData.getResStringArray("AC_CMD_TEMPERATURE_UP"));
			regCmdString("AC_CMD_TEMPERATURE_DOWN", NativeData.getResStringArray("AC_CMD_TEMPERATURE_DOWN"));
			regCmdString("AC_CMD_WIND_SPEED_UP", NativeData.getResStringArray("AC_CMD_WIND_SPEED_UP"));
			regCmdString("AC_CMD_WIND_SPEED_DOWN", NativeData.getResStringArray("AC_CMD_WIND_SPEED_DOWN"));
			regCmdString("AC_CMD_WIND_SPEED_MAX", NativeData.getResStringArray("AC_CMD_WIND_SPEED_MAX"));
			regCmdString("AC_CMD_WIND_SPEED_MIN", NativeData.getResStringArray("AC_CMD_WIND_SPEED_MIN"));
			regCmdString("AC_CMD_OPEN_BLOW_FACE", NativeData.getResStringArray("AC_CMD_OPEN_BLOW_FACE"));
			regCmdString("AC_CMD_OPEN_BLOW_FACE_FOOT", NativeData.getResStringArray("AC_CMD_OPEN_BLOW_FACE_FOOT"));
			regCmdString("AC_CMD_OPEN_BLOW_FOOT", NativeData.getResStringArray("AC_CMD_OPEN_BLOW_FOOT"));
			regCmdString("AC_CMD_OPEN_BLOW_FOOT_DEFROST", NativeData.getResStringArray("AC_CMD_OPEN_BLOW_FOOT_DEFROST"));
			regCmdString("AC_CMD_OPEN_DEFROST", NativeData.getResStringArray("AC_CMD_OPEN_DEFROST"));
			regCmdString("AC_CMD_OPEN_AUTO", NativeData.getResStringArray("AC_CMD_OPEN_AUTO"));
		}else if (command.equals("acmgr.cleartool")) {
			mRemoteAcMgrToolImpl = null;
		}else if ("settempdistance".equals(command)) {
			if (data == null) {
				return null;
			}
			try {
				String strJson = new String(data);
				JSONBuilder json = new JSONBuilder(strJson);
				int minValue = json.getVal("minVal", Integer.class);
				int maxValue = json.getVal("maxVal", Integer.class);
				setTEMPDistance(minValue, maxValue);

			} catch (Exception e) {

			}

		}else if ("setwspeeddistance".equals(command)) {
			if (data == null) {
				return null;
			}
			try {
				String strJson = new String(data);
				JSONBuilder json = new JSONBuilder(strJson);
				int minValue = json.getVal("minVal", Integer.class);
				int maxValue = json.getVal("maxVal", Integer.class);
				setWSpeedDistance(minValue, maxValue);

			} catch (Exception e) {

			}

		}
		return null;
	}
	private Integer mMinTempValue;
	private Integer mMaxTempValue;
	private void setTEMPDistance(int minValue, int maxValue) {
		mMaxTempValue = maxValue;
		mMinTempValue = minValue;
		ACSettingData tempSettingData = new ACSettingData();
		tempSettingData.uint32MaxValue = mMaxTempValue;
		tempSettingData.uint32MinValue = mMinTempValue;
		JNIHelper.sendEvent(UiEvent.EVENT_CAR_CONTROL, CarControlData.SUBEVENT_TEMP_SETTING, tempSettingData);
	}
	
	private Integer mMaxWSpeedValue;
	private Integer mMinWSpeedValue;
	private void setWSpeedDistance(int minValue, int maxValue){
		mMaxWSpeedValue = maxValue;
		mMinWSpeedValue = minValue;
		ACSettingData wspeedSettingData = new ACSettingData();
		wspeedSettingData.uint32MaxValue = mMaxWSpeedValue;
		wspeedSettingData.uint32MinValue = mMinWSpeedValue;
		JNIHelper.sendEvent(UiEvent.EVENT_CAR_CONTROL, CarControlData.SUBEVENT_WSPEED_SETTING, wspeedSettingData);
	}
	
	public static boolean procByRemoteTool(String tool, String command,
			Object... data) {
		String remoteService = null;
		String remoteCmd = null;
		byte[] remoteData = null;
		if (tool.equals(AC_MGR)
				&& hasRemoteAcMgrToolImpl()) {
			remoteService = mRemoteAcMgrToolImpl;
			remoteCmd = tool + "." + command;
			if (data != null && data.length > 0) {
				if (data[0] != null) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("data", data[0]);
					remoteData = doc.toBytes();
				}
			}
		}
		if (remoteService != null) {
			ServiceManager.getInstance().sendInvoke(remoteService, remoteCmd,
					remoteData, null);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("AC").setSessionId()
					.putExtra("cmd", remoteCmd).buildCommReport());
			return true;
		} else {
			return false;
		}
	}
	
	
	
}
