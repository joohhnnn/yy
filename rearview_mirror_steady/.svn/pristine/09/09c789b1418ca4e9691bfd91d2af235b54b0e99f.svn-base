package com.txznet.comm.ui.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.defaul.ThemeConfig;
import com.txznet.comm.ui.theme.defaul.ThemeConfigCar;
import com.txznet.comm.ui.theme.defaul.ThemeConfigLarge;
import com.txznet.comm.ui.theme.defaul.ThemeConfigSmall;
import com.txznet.comm.ui.theme.defaul.ThemeConfigUI1;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * View配置管理,用来管理生成各个默认View的配置管理类
 * View配置读取逻辑
 * 	   1.生成时会有默认配置
 * 	   2.用户可以通过配置文件配置某些属性，优先级高于默认配置
 *     3.下发配置的优先级最高
 *     4.精确度越高则优先级越高
 *     默认配置：ThemeConfigDefault等保存的
 *     用户配置:/etc/txz/theme.cfg 或者 /system/txz/theme.cfg 
 *     下发配置:/sdcard/txz/theme.cfg
 * 
 * @author TerryYang
 *
 */
public class ViewConfiger {

	private static ViewConfiger sInstance;

	private static final String TAG = "ViewConfiger ";
	HashMap<String, Object> mThemeConfig = null;

	private ViewConfiger() {
	}
	
	private Object mAccessLock = new Object();

	private Float mSizeRatio = null;
	
	public void initThemeConfig() {
		synchronized (mAccessLock) {
			loadDefaultConfig();
			if (!RecordWin2Manager.getInstance().isDisableThirdWin()) {
				loadUserConfig();
			}
			loadPriorConfig();
		}
	}

	/**
	 * 使用UI1.0时初始化字体
	 */
	public void initRecordWin1ThemeConfig() {
		LogUtil.logd(TAG + "mSizeRatio:" + mSizeRatio);
		synchronized (mAccessLock) {
			if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
				mThemeConfig = ThemeConfigLarge.getConfig();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
				mThemeConfig = ThemeConfigSmall.getConfig();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
				mThemeConfig = ThemeConfigCar.getConfig();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL) {
				mThemeConfig = ThemeConfig.getConfig();
			}
			HashMap<String, Object> UI1configs = ThemeConfigUI1.getConfig();
			for (String key : UI1configs.keySet()) {
				mThemeConfig.put(key, UI1configs.get(key));
			}
			loadUserConfig();
			loadPriorConfig();
		}
	}
	
	
	/**
	 * 通用部分趁早加载，避免弹出框的问题
	 */
	public void initCommConfig() {
		HashMap<String, String> configs = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_TEXT_SIZE_RATIO);
		if (configs != null && configs.get(TXZFileConfigUtil.KEY_TEXT_SIZE_RATIO) != null) {
			try {
				mSizeRatio = Float.parseFloat(configs.get(TXZFileConfigUtil.KEY_TEXT_SIZE_RATIO));
			} catch (Exception e) {
				LogUtil.logw(TAG + "parse size ratio error:" + e.getMessage());
			}
		}
		synchronized (mAccessLock) {
			mThemeConfig = new HashMap<String, Object>();
			if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
				mThemeConfig = ThemeConfigLarge.getConfig();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
				mThemeConfig = ThemeConfigSmall.getConfig();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
				mThemeConfig = ThemeConfigCar.getConfig();
			} else {
				mThemeConfig = ThemeConfig.getConfig();
			}
		}
	}
	

	public static ViewConfiger getInstance(){
		if(sInstance==null){
			synchronized (ViewConfiger.class) {
				if (sInstance == null) {
					sInstance = new ViewConfiger();
				}
			}
		}
		return sInstance;
	}
	
	public void disableThirdConfig(boolean disable){
		synchronized (mAccessLock) {
			mThemeConfig = new HashMap<String, Object>();
			loadDefaultConfig();
			if (!disable) {
				loadUserConfig();
			}
			loadPriorConfig();
		}
	}
	
	
	private void loadPriorConfig() {
		mThemeConfig = ConfigUtil.loadJsonFile(mThemeConfig, FilePathConstants.UI_THEME_FILE_PRIOR);
	}

	private void loadUserConfig() {
		for (String path: FilePathConstants.getUserUiThemePath()) {
			mThemeConfig = ConfigUtil.loadJsonFile(mThemeConfig, path);
		}
	}
	
	private void loadDefaultConfig() {
		String mThemeConfigPrefix = ConfigUtil.getThemeConfigPrefix();
		if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
			mThemeConfig = ThemeConfigLarge.getConfig();
		} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
			mThemeConfig = ThemeConfigSmall.getConfig();
		} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
			mThemeConfig = ThemeConfigCar.getConfig();
		} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL) {
			mThemeConfig = ThemeConfig.getConfig();
		}
		HashMap<String, Object> configs = new HashMap<String, Object>();
		Class<?> classConfig = null;
		do {
			if (TextUtils.isEmpty(mThemeConfigPrefix)) {
				break;
			}
			try {
				if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
					classConfig = UIResLoader.getInstance().getClass(mThemeConfigPrefix + "ThemeConfigLarge");
				} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
					classConfig = UIResLoader.getInstance().getClass(mThemeConfigPrefix + "ThemeConfigSmall");
				} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
					classConfig = UIResLoader.getInstance().getClass(mThemeConfigPrefix + "ThemeConfigCar");
				} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL) {
					classConfig = UIResLoader.getInstance().getClass(mThemeConfigPrefix + "ThemeConfig");
				}
				if (classConfig == null) {
					break;
				}
				try {
					Method getConfig = classConfig.getMethod("getConfig");
					configs = (HashMap<String, Object>) getConfig.invoke("getConfig");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (false);
		if (configs == null) {
			return;
		}
		for (String key : configs.keySet()) {
			mThemeConfig.put(key, configs.get(key));
		}
	}
	
	public int DEFAULT_COLOR = 0xFF000000;
	public int DEFAULT_SIZE = 15;
	
	/**
	 * 得到希望得到的属性信息,逐层解析poi_color1.list_color1.base_color1,
	 * 会先查询poi_color1.list_color1.base_color1，如果不存在时再查询list_color1.base_color1,
	 * 仍不存在时查询 base_color1,仍得不到时返回默认color返回值
	 */
	public Object getConfig(String attrName) {
		synchronized (mAccessLock) {
			if (TextUtils.isEmpty(attrName)) {
				return null;
			}
			if (mThemeConfig == null) {
				if (ConfigUtil.getAttrType(attrName) == ConfigUtil.ATTR_SIZE) {
					return getSize(DEFAULT_SIZE);
				}
				if (ConfigUtil.getAttrType(attrName) == ConfigUtil.ATTR_COLOR) {
					return DEFAULT_COLOR;
				}
			}
			if (mThemeConfig != null && mThemeConfig.get(attrName) != null) {
				if (ConfigUtil.getAttrType(attrName) == ConfigUtil.ATTR_SIZE) {
					return getSize((Integer) mThemeConfig.get(attrName));
				}
				return mThemeConfig.get(attrName);
			}
			int splitIndex = attrName.indexOf(".");
			if (splitIndex == -1 || splitIndex >= attrName.length()) {
				if (ConfigUtil.getAttrType(attrName) == ConfigUtil.ATTR_COLOR) {
					return DEFAULT_COLOR;
				} else if (ConfigUtil.getAttrType(attrName) == ConfigUtil.ATTR_SIZE) {
					return getSize(DEFAULT_SIZE);
				}
				return null;
			}
			String parentAttrName = attrName.substring(splitIndex + 1, attrName.length());
			return getConfig(parentAttrName);
		}
	}
	
	public int getThemeColor(String colorName) {
		synchronized (mAccessLock) {
			if (TextUtils.isEmpty(colorName)) {
				return DEFAULT_COLOR;
			}
			if (mThemeConfig != null && mThemeConfig.get(colorName) != null) {
				try {
					return (Integer) mThemeConfig.get(colorName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return getDefaultThemeColor(colorName);
		}
	}
	
	private int getDefaultThemeColor(String colorName) {
		if (COLOR_THEME_COLOR1.equals(colorName)) {
			return ConfigUtil.getColorValue("#14211A");
		}else if (COLOR_THEME_COLOR2.equals(colorName)) {
			return ConfigUtil.getColorValue("#4C3736");
		}else if (COLOR_THEME_COLOR2.equals(colorName)) {
			return ConfigUtil.getColorValue("#FFFFFF");
		}
		return DEFAULT_COLOR;
	}
	
	
	public float getSize(int size) {
		float ysize = LayouUtil.getDimen("y" + size);
		float xsize = LayouUtil.getDimen("x" + size);
		float ssize = ysize;
		if (xsize != 0 && xsize < ysize) {
			ssize = xsize;
		}
		if (ssize == 0) {
			return size;
		}
		if (mSizeRatio != null && mSizeRatio >= 0) {
			return size * mSizeRatio;
		}
		if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
			return (ssize * 0.8f);
		} else {
			return ssize;
		}
	}
	
	// 主题色
	public static final String COLOR_THEME_COLOR1 = "theme_color1";
	public static final String COLOR_THEME_COLOR2 = "theme_color2";
	public static final String COLOR_THEME_COLOR3 = "theme_color3";
	
	
	// 颜色相关
	public static final String COLOR_CHAT_TITLE = "title_color1.chat_color1.base_color1";
	public static final String COLOR_CHAT_FROM_SYS = "fromSys_color1.chat_color2.base_color2";
	public static final String COLOR_CHAT_TO_SYS = "toSys_color1.chat_color3.base_color3";
	public static final String COLOR_CHAT_TO_SYS_PART = "toSys_part_color1.chat_color3.base_color3";
	// POI
	public static final String COLOR_POI_INTRO_COLOR1 = "poi_introColor1.list_introColor1.base_color1";
	public static final String COLOR_POI_INTRO_CLOR2 = "poi_introColor2.list_introColor2.base_color2";
	public static final String COLOR_POI_PAGE_COLOR1 = "poi_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_POI_PAGE_COLOR2 = "poi_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_POI_INDEX_COLOR1 = "poi_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_POI_ITEM_COLOR1 = "poi_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_POI_ITEM_COLOR2 = "poi_itemColor2.list_itemColor2.base_color4";
	// AUDIO
	public static final String COLOR_AUDIO_INTRO_COLOR1 = "audio_introColor1.list_introColor1.base_color1";
	public static final String COLOR_AUDIO_PAGE_COLOR1 = "audio_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_AUDIO_PAGE_COLOR2 = "audio_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_AUDIO_INDEX_COLOR1 = "audio_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_AUDIO_ITEM_COLOR1 = "audio_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_AUDIO_ITEM_COLOR2 = "audio_itemColor2.list_itemColor2.base_color4";
	// WX
	public static final String COLOR_WX_INTRO_COLOR1 = "wx_introColor1.list_introColor1.base_color1";
	public static final String COLOR_WX_PAGE_COLOR1 = "wx_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_WX_PAGE_COLOR2 = "wx_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_WX_INDEX_COLOR1 = "wx_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_WX_ITEM_COLOR1 = "wx_itemColor1.list_itemColor1.base_color1";
	// CALL
	public static final String COLOR_CALL_INTRO_COLOR1 = "call_introColor1.list_introColor1.base_color1";
	public static final String COLOR_CALL_PAGE_COLOR1 = "call_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_CALL_PAGE_COLOR2 = "call_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_CALL_INDEX_COLOR1 = "call_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_CALL_ITEM_COLOR1 = "call_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_CALL_ITEM_COLOR2 = "call_itemColor2.list_itemColor2.base_color4";
	// WEATHER
	public static final String COLOR_WEATHER_CITY_COLOR1 = "weather_cityColor1.base_color1";
	public static final String COLOR_WEATHER_DATE_COLOR1 = "weather_dateColor1.base_color1";
	public static final String COLOR_WEATHER_TMP_COLOR1 = "weather_tmpColor1.base_color1";
	public static final String COLOR_WEATHER_TMP_COLOR2 = "weather_tmpColor2.base_color1";
	public static final String COLOR_WEATHER_AIR_COLOR1 = "weather_airColor1.base_color1";
	public static final String COLOR_WEATHER_STATE_COLOR1 = "weather_stateColor1.base_color1";
	public static final String COLOR_WEATHER_ITEM_COLOR1 = "weather_itemColor1.base_color1";
	public static final String COLOR_WEATHER_ITEM_COLOR2 = "weather_itemColor2.base_color1";
	// SHARE
	public static final String COLOR_SHARE_NAME_COLOR1 = "share_nameColor1.base_color1";
	public static final String COLOR_SHARE_NAME_COLOR2 = "share_nameColor2.base_color4";
	public static final String COLOR_SHARE_VALUE_COLOR1 = "share_valueColor1.share_baseColor1";
	public static final String COLOR_SHARE_VALUE_COLOR2 = "share_valueColor2.share_baseColor2";
	public static final String COLOR_SHARE_RISE_COLOR1 = "share_riseColor1.share_baseColor1";
	public static final String COLOR_SHARE_RISE_COLOR2 = "share_riseColor2.share_baseColor2";
	public static final String COLOR_SHARE_ITEM_COLOR1 = "share_itemColor1.base_color4";
	public static final String COLOR_SHARE_ITEM_COLOR2 = "share_itemColor2.base_color1";
	public static final String COLOR_SHARE_ITEM_COLOR3 = "share_itemColor3.share_baseColor1";
	// HELP
	public static final String COLOR_HELP_INTRO_COLOR1 = "help_introColor1.list_introColor1.base_color1";
	public static final String COLOR_HELP_PAGE_COLOR1 = "help_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_HELP_PAGE_COLOR2 = "help_pageColor2.list_pageColor2";
	public static final String COLOR_HELP_ITEM_COLOR1 = "help_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_HELP_ITEM_COLOR2 = "help_itemColor2.list_itemColor2.base_color4";
	public static final String COLOR_HELP_ITEM_COLOR3 = "help_itemColor3.list_itemColor2.base_color4";
	// TTSTHEME
	public static final String COLOR_TTS_INTRO_COLOR1 = "tts_introColor1.list_introColor1.base_color1";
	public static final String COLOR_TTS_PAGE_COLOR1 = "tts_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_TTS_PAGE_COLOR2 = "tts_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_TTS_INDEX_COLOR1 = "tts_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_TTS_ITEM_COLOR1 = "tts_itemColor1.list_itemColor1.base_color1";
	// SIMRECHARGE
	public static final String COLOR_SIM_INTRO_COLOR1 = "sim_introColor1.list_introColor1.base_color1";
	public static final String COLOR_SIM_PAGE_COLOR1 = "sim_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_SIM_PAGE_COLOR2 = "sim_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_SIM_INDEX_COLOR1 = "sim_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_SIM_ITEM_COLOR1 = "sim_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_SIM_ITEM_COLOR2 = "sim_itemColor2.list_itemColor1.base_color1";
	public static final String COLOR_SIM_ITEM_COLOR3 = "sim_itemColor3.list_itemColor1.base_color1";
	// REMINDERS
	public static final String COLOR_REMINDER_INTRO_COLOR1 = "reminder_introColor1.list_introColor1.base_color1";
	public static final String COLOR_REMINDER_PAGE_COLOR1 = "reminder_pageColor1.list_pageColor1.base_color4";
	public static final String COLOR_REMINDER_PAGE_COLOR2 = "reminder_pageColor1.list_pageColor2.base_color1";
	public static final String COLOR_REMINDER_INDEX_COLOR1 = "reminder_indexColor1.list_indexColor1.base_color1";
	public static final String COLOR_REMINDER_ITEM_COLOR1 = "reminder_itemColor1.list_itemColor1.base_color1";
	public static final String COLOR_REMINDER_ITEM_COLOR2 = "reminder_itemColor2.list_itemColor1.base_color1";
	public static final String COLOR_REMINDER_ITEM_COLOR3 = "reminder_itemColor3.list_itemColor1.base_color1";
	
	
	

	// 字体大小
	public static final String SIZE_CHAT_TITLE = "title_size1.chat_size1.base_size1";
	public static final String SIZE_CHAT_FROM_SYS = "fromSys_size1.chat_size1.base_size2";
	public static final String SIZE_CHAT_TO_SYS = "toSys_size1.chat_size1.base_size2";
	public static final String SIZE_CHAT_TO_SYS_PART = "toSys_part_size1.chat_size2.base_size2";
	// POI
	public static final String SIZE_POI_INTRO_SIZE1 = "poi_introSize1.list_introSize1.base_size4";
	public static final String SIZE_POI_INTRO_SIZE2 = "poi_introSize2.list_introSize2.base_size3";
	public static final String SIZE_POI_PAGE_SIZE1 = "poi_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_POI_PAGE_SIZE2 = "poi_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_POI_INDEX_SIZE1 = "poi_indexSize1.list_indexSize1";
	public static final String SIZE_POI_ITEM_SIZE1 = "poi_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_POI_ITEM_SIZE2 = "poi_itemSize2.list_itemSize2.base_size4";
	// AUDIO
	public static final String SIZE_AUDIO_INTRO_SIZE1 = "audio_introSize1.list_introSize1.base_size4";
	public static final String SIZE_AUDIO_PAGE_SIZE1 = "audio_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_AUDIO_PAGE_SIZE2 = "audio_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_AUDIO_INDEX_SIZE1 = "audio_indexSize1.list_indexSize1";
	public static final String SIZE_AUDIO_ITEM_SIZE1 = "audio_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_AUDIO_ITEM_SIZE2 = "audio_itemSize2.list_itemSize2.base_size4";
	// WX
	public static final String SIZE_WX_INTRO_SIZE1 = "wx_introSize1.list_introSize1.base_size4";
	public static final String SIZE_WX_PAGE_SIZE1 = "wx_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_WX_PAGE_SIZE2 = "wx_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_WX_INDEX_SIZE1 = "wx_indexSize1.list_indexSize1";
	public static final String SIZE_WX_ITEM_SIZE1 = "wx_itemSize1.list_itemSize1.base_size2";
	// CALL
	public static final String SIZE_CALL_INTRO_SIZE1 = "call_introSize1.list_introSize1.base_size4";
	public static final String SIZE_CALL_PAGE_SIZE1 = "call_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_CALL_PAGE_SIZE2 = "call_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_CALL_INDEX_SIZE1 = "call_indexSize1.list_indexSize1";
	public static final String SIZE_CALL_ITEM_SIZE1 = "call_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_CALL_ITEM_SIZE2 = "call_itemSize2.list_itemSize2.base_size4";
	// WEATHER
	public static final String SIZE_WEATHER_CITY_SIZE1 = "weather_citySize1.base_size3";
	public static final String SIZE_WEATHER_DATE_SIZE1 = "weather_dateSize1.base_size3";
	public static final String SIZE_WEATHER_TMP_SIZE1 = "weather_tmpSize1";
	public static final String SIZE_WEATHER_TMP_SIZE2 = "weather_tmpSize2";
	public static final String SIZE_WEATHER_AIR_SIZE1 = "weather_airSize1";
	public static final String SIZE_WEATHER_STATE_SIZE1 = "weather_stateSize1";
	public static final String SIZE_WEATHER_ITEM_SIZE1 = "weather_itemSize1.base_size3";
	public static final String SIZE_WEATHER_ITEM_SIZE2 = "weather_itemSize2.base_size2";
	// SHARE
	public static final String SIZE_SHARE_NAME_SIZE1 = "share_nameSize1";
	public static final String SIZE_SHARE_NAME_SIZE2 = "share_nameSize2";
	public static final String SIZE_SHARE_VALUE_SIZE1 = "share_valueSize1";
	public static final String SIZE_SHARE_RISE_SIZE1 = "share_riseSize1";
	public static final String SIZE_SHARE_ITEM_SIZE1 = "share_itemSize1";
	// HELP
	public static final String SIZE_HELP_INTRO_SIZE1 = "help_introSize1.list_introSize1.base_size4";
	public static final String SIZE_HELP_PAGE_SIZE1 = "help_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_HELP_PAGE_SIZE2 = "help_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_HELP_INDEX_SIZE1 = "help_indexSize1.list_indexSize1";
	public static final String SIZE_HELP_ITEM_SIZE1 = "help_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_HELP_ITEM_SIZE2 = "help_itemSize2.list_itemSize2.base_size4";
	public static final String SIZE_HELP_LABEL_ITEM_SIZE1 = "help_list_label_itemSize1";
	// TTSTHEME
	public static final String SIZE_TTS_INTRO_SIZE1 = "tts_introSize1.list_introSize1.base_size4";
	public static final String SIZE_TTS_PAGE_SIZE1 = "tts_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_TTS_PAGE_SIZE2 = "tts_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_TTS_INDEX_SIZE1 = "tts_indexSize1.list_indexSize1";
	public static final String SIZE_TTS_ITEM_SIZE1 = "tts_itemSize1.list_itemSize1.base_size2";
	// SIMRECHARGE
	public static final String SIZE_SIM_INTRO_SIZE1 = "sim_introSize1.list_introSize1.base_size4";
	public static final String SIZE_SIM_PAGE_SIZE1 = "sim_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_SIM_PAGE_SIZE2 = "sim_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_SIM_INDEX_SIZE1 = "sim_indexSize1.list_indexSize1";
	public static final String SIZE_SIM_ITEM_SIZE1 =  "sim_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_SIM_ITEM_SIZE2 =  "sim_itemSize2.list_itemSize1.base_size2";
	public static final String SIZE_SIM_ITEM_SIZE3 =  "sim_itemSize3.list_itemSize1.base_size2";
	// REMINDERS
	public static final String SIZE_REMINDER_INTRO_SIZE1 = "reminder_introSize1.list_introSize1.base_size4";
	public static final String SIZE_REMINDER_PAGE_SIZE1 = "reminder_pageSize1.list_pageSize1.base_size4";
	public static final String SIZE_REMINDER_PAGE_SIZE2 = "reminder_pageSize2.list_pageSize2.base_size4";
	public static final String SIZE_REMINDER_INDEX_SIZE1 = "reminder_indexSize1.list_indexSize1";
	public static final String SIZE_REMINDER_ITEM_SIZE1 =  "reminder_itemSize1.list_itemSize1.base_size2";
	public static final String SIZE_REMINDER_ITEM_SIZE2 =  "reminder_itemSize2.list_itemSize1.base_size2";
	public static final String SIZE_REMINDER_ITEM_SIZE3 =  "reminder_itemSize3.list_itemSize1.base_size2";
	
	// 退出导航等弹窗
	public static final String SIZE_MESSAGE_TITLE = "message_titleSize";
	public static final String SIZE_MESSAGE_MESSAGE = "message_messageSize";
	public static final String SIZE_MESSAGE_SCROLL = "message_scrollSize";
	public static final String SIZE_MESSAGE_BTN_LEFT = "message_btnLeftSize";
	public static final String SIZE_MESSAGE_BTN_MIDDLE = "message_btnMiddleSize";
	public static final String SIZE_MESSAGE_BTN_RIGHT = "message_btnRightSize";
	
}
