package com.txznet.txz.ui.win.help;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.ui.win.record.RecorderWin;

public class HelpMsgManager {
	private static final String KEY_RS_HELP_MSG = "RS_HELP_MSG";
	private static final String KEY_RS_HELP_MSG_CMD_ITEMS = "RS_HELP_MSG_CMD_ITEMS";
	private static final String KEY_RS_HELP_MSG_BRIEF = "RS_HELP_MSG_BRIEF";
	private static final String KEY_RS_HELP_MSG_FAILED_WHEN_LOCAL_OK = "RS_HELP_MSG_FAILED_WHEN_LOCAL_OK";
	private static final String KEY_RS_HELP_MSG_FAILED_WHEN_LOCAL_PROC = "RS_HELP_MSG_FAILED_WHEN_LOCAL_PROC";
	private static final String KYE_RS_HELP_MSG_FAILED_WHEN_LOCAL_ABORT = "RS_HELP_MSG_FAILED_WHEN_LOCAL_ABORT";
	
	private HelpMsgManager(){}
	
	// 获取声控命令提示
	public static Map<String, List<String>> getCmdMsgs(){
		Map<String, List<String>> cmdMsgs = new LinkedHashMap<String, List<String>>();
		try{
			String rawData = NativeData.getResString(KEY_RS_HELP_MSG);
			JSONBuilder doc = new JSONBuilder(rawData);
			JSONArray jCmdItems = doc.getVal(KEY_RS_HELP_MSG_CMD_ITEMS, JSONArray.class);
			for(int i = 0; i < jCmdItems.length(); i++){
				JSONObject jCmdItem = jCmdItems.getJSONObject(i);
				String title = jCmdItem.getString("title");
				JSONArray jItems = jCmdItem.getJSONArray("item");
				List<String> items = new ArrayList<String>();
				for(int j = 0; j < jItems.length(); j++){
					items.add(jItems.getString(j));
				}
				cmdMsgs.put(title, items);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return cmdMsgs;
	}
	
	public static String[] getBriefMsgs(){
		try{
			String rawData = NativeData.getResString(KEY_RS_HELP_MSG);
			JSONBuilder doc = new JSONBuilder(rawData);
			return doc.getVal(KEY_RS_HELP_MSG_BRIEF, String[].class);
		}catch(Exception e){e.printStackTrace();}
		return new String[]{};
	}
	
	public static String[] getFailedMsgWhenLocalOk(){
		try{
			String rawData = NativeData.getResString(KEY_RS_HELP_MSG);
			JSONBuilder doc = new JSONBuilder(rawData);
			return doc.getVal(KEY_RS_HELP_MSG_FAILED_WHEN_LOCAL_OK, String[].class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] getFailedMsgWhenLocalProc(){
		try{
			String rawData = NativeData.getResString(KEY_RS_HELP_MSG);
			JSONBuilder doc = new JSONBuilder(rawData);
			return doc.getVal(KEY_RS_HELP_MSG_FAILED_WHEN_LOCAL_PROC, String[].class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String[] getFailedMsgWhenLocalAbort(){
		try{
			String rawData = NativeData.getResString(KEY_RS_HELP_MSG);
			JSONBuilder doc = new JSONBuilder(rawData);
			return doc.getVal(KYE_RS_HELP_MSG_FAILED_WHEN_LOCAL_ABORT, String[].class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void openHelpDetail(long delay){
		AppLogic.runOnUiGround(new Runnable(){
			@Override
			public void run() {
				RecorderWin.close();
				WinHelpDetailTops.getInstance().show();
			}
		}, delay);
	}
	
	public static byte[] invokeTXZHelpMsg(String packageName, String command, byte[] data){
		if(command.equals("txz.help.ui.detail.open")){
			openHelpDetail(0);
			// 上报数据
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("helper").setAction("open").buildTouchReport());
		}else if (command.equals("txz.help.ui.detail.back")) {
			AppLogic.runOnUiGround(new Runnable() {				
				@Override
				public void run() {
					WinHelpDetailTops.getInstance().dismiss();
					RecorderWin.open();
				}
			}, 0);
		}
		return null;
	}
}
