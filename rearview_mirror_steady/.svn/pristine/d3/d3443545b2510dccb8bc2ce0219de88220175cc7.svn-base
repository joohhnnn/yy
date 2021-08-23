package com.txznet.comm.ui.theme;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.sp.CommonSp;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.defaul.ThemeConfig;
import com.txznet.comm.ui.theme.defaul.ThemeConfigCar;
import com.txznet.comm.ui.theme.defaul.ThemeConfigLarge;
import com.txznet.comm.ui.theme.defaul.ThemeConfigSmall;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ThemeConfigManager{
	private static ThemeConfigManager sInstance;
	private ThemeStyle.Style mStyle;
	
	private ThemeConfigManager(){
	}
	
	public void initThemeConfig() {
		loadDefaultConfig();
		loadThemeStyle();
	}

	public static ThemeConfigManager getInstance(){
		if(sInstance==null){
			synchronized (ThemeConfigManager.class) {
				if (sInstance == null) {
					sInstance = new ThemeConfigManager();
				}
			}
		}
		return sInstance;
	}

	public ThemeStyle.Style getStyle() {
		return mStyle;
	}

	private void loadThemeStyle(){
		if (!TextUtils.isEmpty(mThemeConfigPrefix)) {
			try {
				ThemeStyle.Style mSelectStyle = ThemeStyleSP.getInstance(GlobalContext.get()).getSelectStyle();
				BaseStyleConfig mBaseStyleConfig= ((BaseStyleConfig)UIResLoader.getInstance().getClassInstance(mThemeConfigPrefix+"StyleConfig"));
				ThemeStyle themeStyle = mBaseStyleConfig.getThemeStyle();
				if (themeStyle != null) {
					int i = 0 ;
					for(ThemeStyle.Style style : themeStyle.getStyles()) {
						if (i == 0) {
							mStyle = style;
						} else if (style.isDefault()) {
							mStyle = style;
						}
						if (null != mSelectStyle && style.equals(mSelectStyle)) {
							mStyle = mSelectStyle;
							break;
						}
						++i;
					}
					if (mStyle != null) {
						ThemeStyleSP.getInstance(GlobalContext.get()).setSelectStyle(mStyle);
						mBaseStyleConfig.setSelectStyle(mStyle);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setSelectStyle(ThemeStyle.Style mStyle){
		try {
			BaseStyleConfig mBaseStyleConfig= ((BaseStyleConfig)UIResLoader.getInstance().getClassInstance(mThemeConfigPrefix+"StyleConfig"));
			ThemeStyleSP.getInstance(GlobalContext.get()).setSelectStyle(mStyle);
			mBaseStyleConfig.setSelectStyle(mStyle);
			this.mStyle = mStyle;
		}catch (Exception e) {

		}
	}

	public ThemeStyle getThemeStyle(){
		try {
			BaseStyleConfig mBaseStyleConfig= ((BaseStyleConfig)UIResLoader.getInstance().getClassInstance(mThemeConfigPrefix+"StyleConfig"));
			return mBaseStyleConfig.getThemeStyle();
		}catch (Exception e) {
			return null;
		}
	}
	
	
	String mThemeConfigPrefix = "";
	private void loadDefaultConfig() {
		BaseThemeConfig themeConfig = null;
		Class<?>  configClass = null;
		do {
			mThemeConfigPrefix = ConfigUtil.getThemeConfigPrefix();
			if (TextUtils.isEmpty(mThemeConfigPrefix)) {
				break;
			}
			try {
				if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
					themeConfig = (BaseThemeConfig) UIResLoader.getInstance().getConfigInstance(mThemeConfigPrefix+"ThemeConfigLarge");
				} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
					themeConfig = (BaseThemeConfig) UIResLoader.getInstance().getConfigInstance(mThemeConfigPrefix+"ThemeConfigSmall");
				} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
					themeConfig = (BaseThemeConfig) UIResLoader.getInstance().getConfigInstance(mThemeConfigPrefix+"ThemeConfigCar");
				}else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL) {
					themeConfig = (BaseThemeConfig) UIResLoader.getInstance().getConfigInstance(mThemeConfigPrefix+"ThemeConfig");
				}
			} catch (Exception e) {
				LogUtil.loge("load theme config error");
				e.printStackTrace();
			}
			
		} while (false);

		do {
			try {
				if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
					configClass = UIResLoader.getInstance().getClass(mThemeConfigPrefix+"ThemeConfigHorizontal");
					if (configClass == null) {
						LogUtil.logw("get ThemeConfigVertical error!");
						configClass = BaseThemeConfigHorizontal.class;
					}
				}else if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_VERTICAL) {
					configClass = UIResLoader.getInstance().getClass(mThemeConfigPrefix+"ThemeConfigVertical");
					if (configClass == null) {
						LogUtil.logw("get ThemeConfigVertical error!");
						configClass = BaseThemeConfigHorizontal.class;
					}
				}
				RECORD_WIN_CIRCLE_LY_WIDTH = configClass.getField("RECORD_WIN_CIRCLE_LY_HEIGHT").getInt(null);
				RECORD_WIN_CIRCLE_LY_HEIGHT = configClass.getField("RECORD_WIN_CIRCLE_LY_HEIGHT").getInt(null);
				RECORD_WIN_VOICE_VIEW_WIDTH = configClass.getField("RECORD_WIN_VOICE_VIEW_WIDTH").getInt(null);
				RECORD_WIN_VOICE_VIEW_HEIGHT = configClass.getField("RECORD_WIN_VOICE_VIEW_HEIGHT").getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		} while (false);
		
		LogUtil.logd("themeConfig:" + themeConfig);
		if (themeConfig == null) {
			if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LARGE) {
				themeConfig = new ThemeConfigLarge();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
				themeConfig = new ThemeConfigSmall();
			} else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_CAR) {
				themeConfig = new ThemeConfigCar();
			}else if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL) {
				themeConfig = new ThemeConfig();
			}
		}
		cloneConfig(themeConfig);
		if (configClass == null) {
			if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
				configClass = BaseThemeConfigHorizontal.class;
			} else if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_VERTICAL) {
				configClass = BaseThemeConfigVertical.class;
			}
			try {
				RECORD_WIN_CIRCLE_LY_WIDTH = configClass.getField("RECORD_WIN_CIRCLE_LY_HEIGHT").getInt(null);
				RECORD_WIN_CIRCLE_LY_HEIGHT = configClass.getField("RECORD_WIN_CIRCLE_LY_HEIGHT").getInt(null);
				RECORD_WIN_VOICE_VIEW_WIDTH = configClass.getField("RECORD_WIN_VOICE_VIEW_WIDTH").getInt(null);
				RECORD_WIN_VOICE_VIEW_HEIGHT = configClass.getField("RECORD_WIN_VOICE_VIEW_HEIGHT").getInt(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LogUtil.logd("RECORD_WIN_CIRCLE_LY_WIDTH:"+RECORD_WIN_CIRCLE_LY_WIDTH+"RECORD_WIN_CIRCLE_LY_HEIGHT:"+RECORD_WIN_CIRCLE_LY_HEIGHT
				+"RECORD_WIN_VOICE_VIEW_WIDTH:"+RECORD_WIN_VOICE_VIEW_WIDTH+"RECORD_WIN_VOICE_VIEW_HEIGHT:"+RECORD_WIN_VOICE_VIEW_HEIGHT);
	}
	
	public static float getX(int size) {
		return LayouUtil.getDimen("x"+size);
	}
	
	public static float getY(int size) {
		return LayouUtil.getDimen("y"+size);
	}

	public static class ThemeStyleSP extends CommonSp {
		private static final String spName = "theme_style";
		private static ThemeStyleSP sInstance ;
		protected ThemeStyleSP(Context context) {
			super(context, spName);
		}

		public static ThemeStyleSP getInstance(Context context) {
			if (sInstance == null) {
				synchronized (ThemeStyleSP.class) {
					if (sInstance == null) {
						sInstance = new ThemeStyleSP(context);
					}
				}
			}
			return sInstance;
		}

		private static final String KEY_SELECT_STYLE = "KEY_SELECT_STYLE";
		private static final String KEY_LAST_SELECT_STYLE_INDEX = "KEY_LAST_SELECT_STYLE_INDEX";

		public void setSelectStyle(ThemeStyle.Style selectStyle) {
			LogUtil.logd("setSelectStyle : " + selectStyle.getName() + "; theme : " + selectStyle.getTheme().getName());
			setValue(KEY_SELECT_STYLE, Object2String(selectStyle));
		}

		public ThemeStyle.Style getSelectStyle() {
			String str = getValue(KEY_SELECT_STYLE,"");
			if (!TextUtils.isEmpty(str)) {
				Object obj = String2Object(str);
				if (obj != null) {
					return (ThemeStyle.Style) obj;
				}
			}
			return null;
		}

		public void setLastSelectStyleIndex(int index){
			LogUtil.logd("setLastSelectStyleIndex : " + index);
			setValue(KEY_LAST_SELECT_STYLE_INDEX, index);
		}

		public int getLastSelectStyleIndex() {
			int index = getValue(KEY_LAST_SELECT_STYLE_INDEX, 0);
			return index;
		}

		/**
		 * writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
		 * 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
		 *
		 *  object 待加密的转换为String的对象
		 *  String   加密后的String
		 */
		private static String Object2String(Object object) {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream;
			try {
				objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
				objectOutputStream.writeObject(object);
				String string = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
				objectOutputStream.close();
				return string;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		/**
		 * 使用Base64解密String，返回Object对象
		 *
		 *  objectString 待解密的String
		 *  object      解密后的object
		 */
		private static Object String2Object(String objectString) {
			byte[] mobileBytes = Base64.decode(objectString.getBytes(), Base64.DEFAULT);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
			ObjectInputStream objectInputStream;
			try {
				objectInputStream = new ObjectInputStream(byteArrayInputStream);
				Object object = objectInputStream.readObject();
				objectInputStream.close();
				return object;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

		}
	}
	
	//x
	public static int LIST_ITEM_TXTNUM_MARGINLEFT;
	//y
    public static int LIST_ITEM_TXTNUM_WIDTH;
    //y
    public static int LIST_ITEM_TXTNUM_HEIGHT;
    //y
    public static int LIST_ITEM_WX_HEAD_WIDTH;
    //y
    public static int LIST_ITEM_WX_HEAD_HEIGHT;
    //y
    public static int LIST_CONTENT_PADDINGTOP;
    //y
    public static int LIST_CONTENT_PADDINGBOTTOM;
    //x
    public static int LIST_CONTENT_PADDINGLEFT;
    //x
    public static int LIST_CONTENT_PADDINGRIGHT;
    //y
    public static int LIST_CONTENT_ITEM_MARGINTOP;

    //<!-- poi -->
    //y
    public static int LIST_ITEM_CONTENT_MARGINTOP;
    //y
    public static int LIST_ITEM_CONTENT_MARGINBOTTOM;
    
    //y
    public static int LIST_ITEM_DIVIDER_HEIGHT;
    //x
    public static int LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT;
    //x
    public static int LIST_ITEM_TXTCONTENT_MARGINLEFT;

    //<!-- help -->
    //y
    public static int LIST_ITEM_HELP_ICON_WIDTH;
    //y
    public static int LIST_ITEM_HELP_ICON_HEIGHT;
    //y
    public static int HELP_ICON_WIDTH;
    //y
    public static int HELP_ICON_HEIGHT;

    //<!-- chat -->
    //y
    public static int LIST_ITEM_TXTCHAT_MINHEIGHT;

    //<!-- stock -->
    //x
    public static int STOCK_PIC_WIDTH;
    //y
    public static int STOCK_PIC_HEIGHT;
    //x
    public static int STOCK_INFO_LY_WIDTH;
    
    //weather
    //y
    public static int LIST_ITEM_WEATHER_ICON_WIDTH;
    //y
    public static int LIST_ITEM_WEATHER_ICON_HEIGHT;
    //y
    public static int LIST_ITEM_WEATHER_HEAD_MARGINTOP;
    //y
    public static int LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM;
    //y
    public static int LIST_ITEM_WEATHER_TITLE_MARGINTOP;
    //y
    public static int LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM;
    //x
    public static int LIST_ITEM_WEATHER_TITLE_MARGINLEFT;
    
    //下面两个是像素
    public static int STOCK_MAXWIDTH;
    public static int WEATHER_MAXWIDTH;
    
    //横向和纵向布局
    public static int RECORD_WIN_CIRCLE_LY_WIDTH;
    public static int RECORD_WIN_CIRCLE_LY_HEIGHT;
    public static int RECORD_WIN_VOICE_VIEW_WIDTH;
    public static int RECORD_WIN_VOICE_VIEW_HEIGHT;
    
    public void cloneConfig(BaseThemeConfig src) {
		LIST_ITEM_TXTNUM_MARGINLEFT = src.LIST_ITEM_TXTNUM_MARGINLEFT;
		LIST_ITEM_TXTNUM_WIDTH = src.LIST_ITEM_TXTNUM_WIDTH;
		LIST_ITEM_TXTNUM_HEIGHT = src.LIST_ITEM_TXTNUM_HEIGHT;
		LIST_ITEM_WX_HEAD_WIDTH = src.LIST_ITEM_WX_HEAD_WIDTH;
		LIST_ITEM_WX_HEAD_HEIGHT = src.LIST_ITEM_WX_HEAD_HEIGHT;
		LIST_CONTENT_PADDINGTOP = src.LIST_CONTENT_PADDINGTOP;
		LIST_CONTENT_PADDINGBOTTOM = src.LIST_CONTENT_PADDINGBOTTOM;
		LIST_CONTENT_PADDINGLEFT= src.LIST_CONTENT_PADDINGLEFT;
		LIST_CONTENT_PADDINGRIGHT = src.LIST_CONTENT_PADDINGRIGHT;
		LIST_CONTENT_ITEM_MARGINTOP = src.LIST_CONTENT_ITEM_MARGINTOP;

		// <!-- poi -->
		LIST_ITEM_CONTENT_MARGINTOP = src.LIST_ITEM_CONTENT_MARGINTOP;
		LIST_ITEM_CONTENT_MARGINBOTTOM = src.LIST_ITEM_CONTENT_MARGINTOP;
		LIST_ITEM_DIVIDER_HEIGHT = src.LIST_ITEM_DIVIDER_HEIGHT;
		LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT = src.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT;
		LIST_ITEM_TXTCONTENT_MARGINLEFT = src.LIST_ITEM_TXTCONTENT_MARGINLEFT;

		// <!-- help -->
		LIST_ITEM_HELP_ICON_WIDTH = src.LIST_ITEM_HELP_ICON_WIDTH;
		LIST_ITEM_HELP_ICON_HEIGHT = src.LIST_ITEM_HELP_ICON_HEIGHT;
		HELP_ICON_WIDTH = src.HELP_ICON_WIDTH;
		HELP_ICON_HEIGHT = src.HELP_ICON_HEIGHT;

		// <!-- chat -->
		LIST_ITEM_TXTCHAT_MINHEIGHT = src.LIST_ITEM_TXTCHAT_MINHEIGHT;

		// <!-- stock -->
		STOCK_PIC_WIDTH = src.STOCK_PIC_WIDTH;
		STOCK_PIC_HEIGHT = src.STOCK_PIC_HEIGHT;
		STOCK_INFO_LY_WIDTH = src.STOCK_INFO_LY_WIDTH;

		// weather
		LIST_ITEM_WEATHER_ICON_WIDTH = src.LIST_ITEM_WEATHER_ICON_WIDTH;
		LIST_ITEM_WEATHER_ICON_HEIGHT = src.LIST_ITEM_WEATHER_ICON_HEIGHT;
		
		LIST_ITEM_WEATHER_HEAD_MARGINTOP = src.LIST_ITEM_WEATHER_HEAD_MARGINTOP;
	    LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM = src.LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM;
	    LIST_ITEM_WEATHER_TITLE_MARGINTOP = src.LIST_ITEM_WEATHER_TITLE_MARGINTOP;
	    LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM = src.LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM;
	    LIST_ITEM_WEATHER_TITLE_MARGINLEFT = src.LIST_ITEM_WEATHER_TITLE_MARGINLEFT;

		// 下面两个是像素
		STOCK_MAXWIDTH = src.STOCK_MAXWIDTH;
		WEATHER_MAXWIDTH = src.WEATHER_MAXWIDTH;
	}
}
