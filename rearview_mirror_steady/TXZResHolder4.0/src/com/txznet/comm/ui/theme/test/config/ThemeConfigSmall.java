package com.txznet.comm.ui.theme.test.config;

import java.util.HashMap;

import com.txznet.comm.ui.theme.BaseThemeConfig;
import com.txznet.comm.ui.util.ConfigUtil;

public class ThemeConfigSmall extends ThemeConfig {
	
	static{
		configs.put("base_size1", 30);
		configs.put("base_size2", 24);
		configs.put("base_size3", 22);
		configs.put("base_size4", 20);
		configs.put("list_indexSize1", 30);
		configs.put("weather_tmpSize1", 120);
		configs.put("weather_tmpSize2", 30);
		configs.put("weather_stateSize1", 24);
		configs.put("weather_airSize1", 24);
		configs.put("share_nameSize1", 26);
		configs.put("share_nameSize2", 20);
		configs.put("share_valueSize1", 50);
		configs.put("share_riseSize1", 22);
		configs.put("share_itemSize1", 21);
		configs.put("list_introSize1", 20);
		configs.put("list_pageSize1", 20);
		configs.put("sim_itemSize1", 20);
		configs.put("sim_itemSize2", 22);
		configs.put("sim_itemSize3", 18);
		configs.put("list_list_itemSize1", 28);
		configs.put("list_list_itemSize2", 22);
		configs.put("chat_size1", 26);
	}
	
	public static HashMap<String, Object> getConfig(){
		return configs;
	}
	
	
	public ThemeConfigSmall() {
		// <!-- little 短屏 -->
		LIST_ITEM_TXTNUM_MARGINLEFT = 15;
		LIST_ITEM_TXTNUM_WIDTH = 48;
		LIST_ITEM_TXTNUM_HEIGHT = 48;
		LIST_ITEM_WX_HEAD_WIDTH = 56;
		LIST_ITEM_WX_HEAD_HEIGHT = 56;

		// <!-- poi -->
		LIST_ITEM_CONTENT_MARGINTOP = 2;
		LIST_ITEM_CONTENT_MARGINBOTTOM = 2;
		LIST_ITEM_DIVIDER_HEIGHT = 1;
		LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT = 15;
		LIST_ITEM_TXTCONTENT_MARGINLEFT = 6;

		// <!-- help -->
		LIST_ITEM_HELP_ICON_WIDTH = 56;
		LIST_ITEM_HELP_ICON_HEIGHT = 56;
		HELP_ICON_WIDTH = 48;
		HELP_ICON_HEIGHT = 48;

		// <!-- chat -->
		LIST_ITEM_TXTCHAT_MINHEIGHT = 56;

		// <!-- stock -->
		STOCK_PIC_WIDTH = 210;
		STOCK_PIC_HEIGHT = 210;
		STOCK_INFO_LY_WIDTH = 130;

		// weather
		LIST_ITEM_WEATHER_ICON_WIDTH = 60;
		LIST_ITEM_WEATHER_ICON_HEIGHT = 60;
		LIST_ITEM_WEATHER_HEAD_MARGINTOP = 0;
	    LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM = 10;
	    LIST_ITEM_WEATHER_TITLE_MARGINTOP = 0;
	    LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM = 10;
	    LIST_ITEM_WEATHER_TITLE_MARGINLEFT = 4;

		// 下面这两个是像素
		STOCK_MAXWIDTH = 800;
		WEATHER_MAXWIDTH = 800;
	}
}
