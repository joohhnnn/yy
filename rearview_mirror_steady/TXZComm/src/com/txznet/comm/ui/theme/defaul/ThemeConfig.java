package com.txznet.comm.ui.theme.defaul;

import java.util.HashMap;

import com.txznet.comm.ui.theme.BaseThemeConfig;
import com.txznet.comm.ui.util.ConfigUtil;
/**
 * 
 * 
 */
public class ThemeConfig extends BaseThemeConfig {
	
	static {
		configs.put("base_color1", ConfigUtil.getColorValue("#FFFFFFFF"));
		configs.put("base_color2", ConfigUtil.getColorValue("#4BD2FD"));
		configs.put("base_color3", ConfigUtil.getColorValue("#F8E71C"));
		configs.put("base_color4", ConfigUtil.getColorValue("#80FFFFFF"));
		configs.put("share_baseColor1", ConfigUtil.getColorValue("#FFF54545"));
		configs.put("share_baseColor2", ConfigUtil.getColorValue("#FF00CF4A"));
		configs.put("share_itemColor2", ConfigUtil.getColorValue("#FFFFFF"));
		configs.put("help_itemColor2.list_itemColor2.base_color4",ConfigUtil.getColorValue("#CCFFFFFF"));
		configs.put("help_itemColor3.list_itemColor2.base_color4",ConfigUtil.getColorValue("#80FFFFFF"));
		configs.put("toSys_part_color1.chat_color3.base_color3",ConfigUtil.getColorValue("#FF00B9FF"));
		
		configs.put("base_size1", 30);
		configs.put("base_size2", 26);
		configs.put("base_size3", 24);
		configs.put("base_size4", 20);
		configs.put("list_indexSize1", 36);
		configs.put("weather_tmpSize1", 96);
		configs.put("weather_tmpSize2", 28);
		configs.put("weather_stateSize1", 26);
		configs.put("weather_airSize1", 26);
		configs.put("share_nameSize1", 36);
		configs.put("share_nameSize2", 28);
		configs.put("share_valueSize1", 85);
		configs.put("share_riseSize1", 36);
		configs.put("share_itemSize1", 28);
		configs.put("list_introSize1", 20);
		configs.put("list_pageSize1", 20);
		configs.put("sim_itemSize1", 26);
		configs.put("sim_itemSize2", 28);
		configs.put("sim_itemSize3", 24);

		configs.put("message_titleSize", 36);
		configs.put("message_messageSize", 30);
		configs.put("message_scrollSize", 32);
		configs.put("message_btnLeftSize", 28);
		configs.put("message_btnMiddleSize", 28);
		configs.put("message_btnRightSize", 28);
		configs.put("help_list_label_itemSize1", 30);
		configs.put("toSys_part_size1.chat_size2.base_size2", 42);
	}

	public static HashMap<String, Object> getConfig(){
		return configs;
	}
	
	public ThemeConfig() {
		LIST_ITEM_TXTNUM_MARGINLEFT = 15;
		LIST_ITEM_TXTNUM_WIDTH = 44;
		LIST_ITEM_TXTNUM_HEIGHT = 44;
		LIST_ITEM_WX_HEAD_WIDTH = 56;
		LIST_ITEM_WX_HEAD_HEIGHT = 56;

		// <!-- poi -->
		LIST_ITEM_CONTENT_MARGINTOP = 2;
		LIST_ITEM_CONTENT_MARGINBOTTOM = 2;
		LIST_ITEM_DIVIDER_HEIGHT = 1;
		LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT = 15;
		LIST_ITEM_TXTCONTENT_MARGINLEFT = 6;

		// <!-- help -->
		LIST_ITEM_HELP_ICON_WIDTH = 44;
		LIST_ITEM_HELP_ICON_HEIGHT = 44;
		HELP_ICON_WIDTH = 48;
		HELP_ICON_HEIGHT = 48;

		// <!-- chat -->
		LIST_ITEM_TXTCHAT_MINHEIGHT = 56;

		// <!-- stock -->
		STOCK_PIC_WIDTH = 352;
		STOCK_PIC_HEIGHT = 220;
		STOCK_INFO_LY_WIDTH = 187;

		// weather
		LIST_ITEM_WEATHER_ICON_WIDTH = 48;
		LIST_ITEM_WEATHER_ICON_HEIGHT = 48;
		
		LIST_ITEM_WEATHER_HEAD_MARGINTOP = 0;
	    LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM = 10;
	    LIST_ITEM_WEATHER_TITLE_MARGINTOP = 0;
	    LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM = 10;
	    LIST_ITEM_WEATHER_TITLE_MARGINLEFT = 4;

		// 下面两个是像素
		STOCK_MAXWIDTH = 1000;
		WEATHER_MAXWIDTH = 1000;
	}
}
