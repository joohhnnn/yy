package com.txznet.comm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

public class PropertyUtil {

	private static Properties sProperties;
	private static PropertyUtil sUtil = new PropertyUtil();

	private PropertyUtil() {
		sProperties = new Properties();
	}

	public static PropertyUtil getInstance() {
		return sUtil;
	}

	public void loadFile(int rawId) {
		InputStream in = GlobalContext.get().getResources().openRawResource(rawId);
		try {
			if (in != null) {
				sProperties.load(in);
			} else {
				LogUtil.loge("loadFile fail！");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getStringProperty(String key) {
		return sProperties.getProperty(key);
	}

	public boolean getBooleanProperty(String key, boolean defValue) {
		String value = sProperties.getProperty(key);
		return (value != null && !value.equals("")) ? Boolean.parseBoolean(value) : defValue;
	}

	public int getIntegerProperty(String key, int defValue) {
		String value = sProperties.getProperty(key);
		return (value != null && !value.equals("")) ? Integer.parseInt(value) : defValue;
	}
}