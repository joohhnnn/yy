package com.txznet.txz.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.json.JSONObject;
import android.util.Log;

public class UserVoiceConfig {
	public final static String TAG = "UserConfig";
	public final static String USERCONFIG_PATH = "/system/txz/voiceconf.json";
	public static Double sVoiceGainRate = null;
	private static boolean sInited = false;
	private static void init(String sJson){
		Log.d(TAG, "sJson : " + sJson);
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(sJson);
		} catch (Exception e) {
			Log.w(TAG, e.toString());
			return;
		}
		
		
		try {
			sVoiceGainRate = jsonObj.getDouble("voice.gain.rate");
		} catch (Exception e) {
		}
	}
	
	public synchronized static void init(){
		if (sInited){
			Log.d(TAG, "sInited : " + sInited);
			return;
		}
		
		sInited = true;
		try {
			init(new File(USERCONFIG_PATH));
		} catch (Exception e) {

		}
	}
	
	private static void init(File f){
		if (f == null){
			Log.d(TAG, "f = null");
			return;
		}
		
		if (!f.exists() || !f.isFile()){
			Log.d(TAG, "f is not exist");
			return;
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(f));
		} catch (Exception e) {
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		String s = null;
		do {
			try {
				s = reader.readLine();
			} catch (Exception e) {
				break;
			}

			if (s == null) {
				break;
			}
			
			builder.append(s);
		} while (true);
		
		if (reader != null){
			try {
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		init(builder.toString());
	}
}
