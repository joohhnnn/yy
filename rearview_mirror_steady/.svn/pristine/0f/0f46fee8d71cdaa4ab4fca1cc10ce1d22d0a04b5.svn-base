package com.txznet.record.setting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import android.content.Context;
import android.widget.Toast;

public class TXZSettingConfig {

	/**
	 * 从配置文件读取参数
	 * 
	 * @param context
	 * @param configFile
	 * @return
	 */
	public static  Properties loadConfig(Context context, String configFile) {
		Properties properties = new Properties();
		try {
			FileInputStream read = new FileInputStream(configFile);
			properties.load(read);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return properties;
	}

	/**
	 * 保存配置文件
	 * 
	 * @param context
	 * @param configFile
	 * @param properties
	 * @return
	 */
	public static boolean saveConfig(Context context, String configFile,
			Properties properties) {
		try {
			File file = new File(configFile);
			if (!file.exists()) {
				file.createNewFile();
				FileOutputStream write=new FileOutputStream(file);
				properties.store(write, "同行者设置配置文件");
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 保存唤醒词到配置文件
	 */
	public static  boolean saveCommand(Properties properties,ArrayList<String> mCommands,Context context) {
		boolean isSaveSuccess = false;
		if (properties == null) {
			properties = new Properties();
		}
		properties.put("mCommands", mCommands);
		isSaveSuccess = TXZSettingConfig.saveConfig(context,
				MainActivity.TXZ_COMMAND_CONFIG_FILE, properties);
		return isSaveSuccess;

	}
	
	/**
	 * 从配置文件获取唤醒词
	 */
	public static  void loadCommand(Properties properties,ArrayList<String> mCommands,Context context) {
		if (mCommands == null) {
			mCommands = new ArrayList<String>();
		}
		mCommands.clear();
		 properties = TXZSettingConfig.loadConfig(
				 context, MainActivity.TXZ_COMMAND_CONFIG_FILE);
		if (properties == null) {
			Toast.makeText(context, "您未设置同行者配置文件",
					Toast.LENGTH_LONG).show();
		} else {
			mCommands = (ArrayList<String>) properties.get("mCommands");
			if (mCommands.isEmpty()) {
				Toast.makeText(context, "您未设置唤醒词",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
