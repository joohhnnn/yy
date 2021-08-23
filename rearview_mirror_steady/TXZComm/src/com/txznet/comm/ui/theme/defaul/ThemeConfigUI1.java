package com.txznet.comm.ui.theme.defaul;

import java.util.HashMap;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ConfigUtil;

public class ThemeConfigUI1 extends ThemeConfig {
	public static HashMap<String, Object> getConfig(){
		HashMap<String, Object> configs = new HashMap<String, Object>();
		configs.put("base_color2", ConfigUtil.getColorValue("#4BD2FD"));
		configs.put("base_color3", ConfigUtil.getColorValue("#F8E71C"));

		configs.put("fromSys_color1.chat_color2.base_color2", ConfigUtil.getColorValue("#FFFFFF"));
		configs.put("toSys_color1.chat_color3.base_color3", ConfigUtil.getColorValue("#00B9FF"));
		return configs;
	}
	
}
