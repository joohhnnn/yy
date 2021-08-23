package com.txznet.comm.ui.util;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.advertising.BaseAdvertisingControl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * 用来解析主题配置文件
 *
 */
public class ConfigUtil {

	private ConfigUtil(){
	}
	
	private static final String TAG = "ConfigUtil ";
	
	// 主题类型
	public static final int THEME_TYPE_SIRI = 1;
	public static final int THEME_TYPE_IRONMAN = 2;
	public static final int THEME_TYPE_WAVE = 3;
	 
	private static int mThemeType = THEME_TYPE_SIRI;
	private static String mThemeViewPrefix;
	private static String mThemeConfigPrefix;
	private static String mThemeLayoutPrefix;
	private static String mClassKeyEvent;
	private static Boolean mUseExternalHelp;
	private static Boolean mShowHelpTips;
	private static Boolean mUseTypingEffect;
	private static Boolean mUseSceneInfo;
	private static Boolean mUseFullScreenFlag;
	private static Boolean mPriorityResHolderRes;
	private static Boolean mEnableHelpListBackIcon;

	
	public static final int SCREEN_TYPE_LITTLE = 1;
	public static final int SCREEN_TYPE_NORMAL = 2;
	public static final int SCREEN_TYPE_LARGE = 3;
	public static final int SCREEN_TYPE_CAR = 4;
	
	public static int mCurType = SCREEN_TYPE_NORMAL;
	
	static boolean isAutoItemHeight = true;
	static int mMaxItemHeight = 120;
	//布局方式，使用横向布局或者竖向布局
	public static final int LAYOUT_TYPE_HORIZONTAL = 1;
	public static final int LAYOUT_TYPE_VERTICAL = 2;
	private static BaseSceneInfoForward mBaseSceneInfoForward;
	private static BaseAdvertisingControl mBaseAdvertisingControl;
	
	
	
	public static void initScreenType(View view){
		Integer mItemCount = getIntValue("max_visible_count");
		if (null != mItemCount && mItemCount > 0) {
			ScreenUtil.mSetItemCount = mItemCount;
		}
		
		Integer winHeight = getIntValue("win_height");
		if (null != winHeight && winHeight > 0) {
			ScreenUtil.mSetWinHeight = winHeight;
		}
		
		Integer itemHeight = getIntValue("list_item_height");
		if (null != itemHeight && itemHeight > 0) {
			ScreenUtil.mSetItemHeight = itemHeight;
		}
		
		Integer mItemHeight = getIntValue("max_item_height");
		if (null != mItemHeight  && mItemHeight > 0) {
			ScreenUtil.mMaxItemHeight_h = mItemHeight;
			ScreenUtil.mMaxItemHeight_v = mItemHeight;
		}
		
		Integer mCinemaItemCount = getIntValue("cinema_visible_count");
		if (null != mCinemaItemCount  && mCinemaItemCount > 0) {
			ScreenUtil.sSetCinemaItemCount = mCinemaItemCount;
		}

		String mAutoItemHeight = LayouUtil.getString("config_list_full_screen");
		if (null != mAutoItemHeight) {
			if (TextUtils.equals(mAutoItemHeight,"true")) {
				ScreenUtil.mSetAutoItemHeight = true;
			} else if (TextUtils.equals(mAutoItemHeight,"false")) {
				ScreenUtil.mSetAutoItemHeight = false;
			}
		}

		
		String mOrientation = LayouUtil.getString("orientation");
		if (TextUtils.equals(mOrientation, "horizontal")) {
			ScreenUtil.initHorizontalScreenType(view);
		} else if (TextUtils.equals(mOrientation, "vertical")) {
			ScreenUtil.initVerticalScreenType(view);
		} else {//默认是auto
			ScreenUtil.initScreenType(view);
		}
	}
	
	
	private static Integer getIntValue(String key) {
		String strData = LayouUtil.getString(key);
		Integer ret = null;
		if (!TextUtils.isEmpty(strData)) {
			try {
				ret = Integer.valueOf(strData);
			} catch (Exception e) {
			}
		}
		return ret;
	}
	
	
	public static int getScreenType(){
		return ScreenUtil.mScreenType;
	}
	
	/**
	 * @return 获取横向还是竖向布局
	 */
	public static int getLayoutType() {
		return ScreenUtil.mLayoutType;
	}
	
	public static void initThemeType(int type) {
		switch (type) {
		case THEME_TYPE_IRONMAN:
		case THEME_TYPE_SIRI:
		case THEME_TYPE_WAVE:
			mThemeType = type;
			break;
		default:
			mThemeType = THEME_TYPE_SIRI;
			break;
		}
		ScreenUtil.mThemeType = mThemeType;
	}
	
	
	public static int getThemeType() {
		return mThemeType;
	}

	private static String mThemePackage = null;
	
	
	public static String getThemeWinRecordClassName() {
		if(TextUtils.isEmpty(mThemePackage)){
			mThemePackage = LayouUtil.getString("theme_package");
			LogUtil.logd("mThemePackage:" + mThemePackage);
		}
		if (!TextUtils.isEmpty(mThemePackage)) {
			return mThemePackage + ".winrecord.WinRecordImpl";
		}
		return null;
	}
	
	/**
	 * 得到ThemeView类的前缀 
	 */
	public static String getThemeViewPrefix() {
		if (mThemeViewPrefix == null) {
			if(TextUtils.isEmpty(mThemePackage)){
				mThemePackage = LayouUtil.getString("theme_package");
				LogUtil.logd("mThemePackage:" + mThemePackage);
			}
			if(!TextUtils.isEmpty(mThemePackage)){
				mThemeViewPrefix = mThemePackage+".view.";
			}
		}
		return mThemeViewPrefix;
	}

	/**
	 * 得到ThemeLayout类名的前缀 xxx.layout.WinLayout
	 * @return
	 */
	public static String getThemeLayoutPrefix() {
		if (mThemeLayoutPrefix == null) {
			if(TextUtils.isEmpty(mThemePackage)){
				mThemePackage = LayouUtil.getString("theme_package");
			}
			if(!TextUtils.isEmpty(mThemePackage)){
				mThemeLayoutPrefix = mThemePackage+".winlayout.";
			}
		}
		return mThemeLayoutPrefix;
	}

	/**
	 *	得到Theme配置类名的前缀 
	 */
	public static String getThemeConfigPrefix(){
		if(mThemeConfigPrefix == null){
			if(TextUtils.isEmpty(mThemePackage)){
				mThemePackage = LayouUtil.getString("theme_package");
			}
			if(!TextUtils.isEmpty(mThemePackage)){
				mThemeConfigPrefix = mThemePackage+".config.";
			}
		}
		return mThemeConfigPrefix;
	}

	public static boolean isUseExternalHelp(){
		if (mUseExternalHelp == null) {
			if (TextUtils.equals(LayouUtil.getString("config_use_external_help"),"true")) {
				mUseExternalHelp = true;
			}else {
				mUseExternalHelp = false;
			}
		}
		return  mUseExternalHelp;
	}

	public static boolean isShowHelpTips(){
		if (mShowHelpTips == null) {
			if (TextUtils.equals(LayouUtil.getString("show_help_tips"),"true")) {
				mShowHelpTips = true;
			} else {
				mShowHelpTips = false;
			}
		}
		return mShowHelpTips;
	}

	public static boolean useTypingEffect(boolean forceUpdate){
		if (mUseTypingEffect == null || forceUpdate) {
			if (TextUtils.equals(LayouUtil.getString("use_typing_effect"),"true")) {
				mUseTypingEffect = true;
			} else {
				mUseTypingEffect = false;
			}
		}
		return mUseTypingEffect;
	}

	public static boolean useTypingEffect(){
		return useTypingEffect(false);
	}

	/**
	 * 得到getKeyEventDispatcher的类名
	 */
	public static String getKeyEventDispatcher(){
		if (mClassKeyEvent == null) {
			if (TextUtils.isEmpty(mThemePackage)) {
				mThemePackage = LayouUtil.getString("theme_package");
			}
			if(!TextUtils.isEmpty(mThemePackage)){
				mClassKeyEvent = mThemePackage+".keyevent.KeyEventDispatcher";
			}
		}
		return mClassKeyEvent;
	}
	
	
	
	public static void checkViewRect(View contentView) {
		ScreenUtil.checkViewRect(contentView);
	}

	
	public static int getDisplayLvItemH(boolean needReset) {
		return ScreenUtil.getDisplayLvItemH(needReset);
	}
	
	public static int getVisbileCount() {
		return ScreenUtil.getVisbileCount();
	}

	/**
	 * @return
	 */
	public static int getCinemaHeight() {
		return getDisplayLvItemH(false) * ((getVisbileCount() > 4)?4:getVisbileCount());
	}
	
	/**
	 * @return 获取电影条目显示的个数，默认4个
	 */
	public static int getCinemaItemCount() {
		return ScreenUtil.getCinemaItemCount();
	}
	
	private static Boolean mIsDelayAddWkWords = null;
	public static boolean isDelayAddWkWords() {
		if (mIsDelayAddWkWords == null) {
			String str = LayouUtil.getString("config_delay_addwkWords");
			mIsDelayAddWkWords = "true".equals(str);
		}
		return mIsDelayAddWkWords;
	}
	

	private static Integer mLayoutRecordWeight = null;
	private static int mLayoutRecordWeightDefault = 1;
	public static Integer getRecordWeight() {
		if (mLayoutRecordWeight == null) {
			try {
				String str = LayouUtil.getString("layout_record_weight");
				if (!TextUtils.isEmpty(str)) {
					mLayoutRecordWeight = Integer.parseInt(str);
					ScreenUtil.mRecordWeight = mLayoutRecordWeight;
				}
			} catch (Exception e) {
				LogUtil.loge("getRecordWeight error!",e);
			}
		}
		if (mLayoutRecordWeight == null) {
			return mLayoutRecordWeightDefault;
		}
		return mLayoutRecordWeight;
	}

	private static Integer mLayoutContentWeight = null;
	private static int mLayoutContentWeightDefault = 3;
	public static Integer getContentWeight() {
		if (mLayoutContentWeight == null) {
			try {
				String str = LayouUtil.getString("layout_content_weight");
				if (!TextUtils.isEmpty(str)) {
					mLayoutContentWeight = Integer.parseInt(str);
					ScreenUtil.mContentWeight =  mLayoutContentWeight;
				}
			} catch (Exception e) {
				LogUtil.loge("getContentWeight error!",e);
			}
		}
		if (mLayoutContentWeight == null) {
			return mLayoutContentWeightDefault;
		}
		return mLayoutContentWeight;
	}
	
	
	private static Integer mLayoutRecordHeight = null;
	
	public static Integer getRecordHeight() {
		if (mLayoutRecordHeight == null) {
			try {
				String str = LayouUtil.getString("layout_record_height");
				if (!TextUtils.isEmpty(str)) {
					mLayoutRecordHeight = Integer.parseInt(str);
				}
			} catch (Exception e) {
				LogUtil.loge("getRecordHeight error!",e);
			}
		}
		return mLayoutRecordHeight;
	}
	

	/**
	 * 解析配置的json
	 * @param config
	 * @param jsonString
	 */
	public static void loadJsonConfig(HashMap<String, Object> config,String jsonString){
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray attrArray = jsonObject.getJSONArray("attrs");
			if (config == null) {
				config = new HashMap<String, Object>();
			}
			for (int i = 0; i < attrArray.length(); i++) {
				JSONObject attrItem = attrArray.getJSONObject(i);
				Iterator iterator = attrItem.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					switch (getAttrType(key)) {
					case ATTR_COLOR:
						Integer colorValue = getColorValue(""+attrItem.get(key));
						if(colorValue!=null){
							config.put(key, colorValue);
						}
						break;
					case ATTR_SIZE:
						int sizeValue =  attrItem.getInt(key);
						config.put(key, sizeValue);
						break;
					case ATTR_INVALID:

						break;
					case ATTR_COMMENT:

						break;
					default:
						break;
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 加载配置文件
	 * @param config
	 * @param path
	 */
	public static HashMap<String, Object> loadJsonFile(HashMap<String, Object> config,String path){
		HashMap<String, Object> configs = config;
		File file = new File(path);
		if (path == null || !file.exists()) {
			return configs;
		}
		String jsonString = getFileContent(file);
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray attrArray = jsonObject.getJSONArray("attrs");
			if (configs == null) {
				configs = new HashMap<String, Object>();
			}
			for (int i = 0; i < attrArray.length(); i++) {
				JSONObject attrItem = attrArray.getJSONObject(i);
				Iterator iterator = attrItem.keys();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					switch (getAttrType(key)) {
					case ATTR_COLOR:
						Integer colorValue = getColorValue(""+attrItem.get(key));
						if(colorValue!=null){
							configs.put(key, colorValue);
						}
						break;
					case ATTR_SIZE:
						int sizeValue =  attrItem.getInt(key);
						configs.put(key, sizeValue);
						break;
					case ATTR_INVALID:

						break;
					case ATTR_COMMENT:

						break;
					default:
						break;
					}

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return configs;
	}

	/**
	 * Parse the color string, and return the corresponding color-int.
	 * If the string cannot be parsed, throws an IllegalArgumentException
	 * exception. Supported formats are:
	 * #RRGGBB
	 * #AARRGGBB
	 * or one of the following names:
	 * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
	 * 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey',
	 * 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple',
	 * 'silver', 'teal'.
	 */
	public static Integer getColorValue(String color) {
		
		Integer colorValue = null;
		try {
			colorValue = Color.parseColor(color);
		} catch (Exception e) {
			LogUtil.loge("get color value error:" + color);
		}
		return colorValue;
	}
	
	
	public static final int ATTR_COLOR = 1; 
	public static final int ATTR_SIZE = 2;
	public static final int ATTR_INVALID = -1;
	public static final int ATTR_COMMENT = -2;
	
	
	/**
	 * 根据名称返回对应类型
	 * @param attrName
	 */
	public static int getAttrType(String attrName) {
		attrName = attrName.toLowerCase(Locale.CHINA);
		if (TextUtils.isEmpty(attrName)) {
			return ATTR_INVALID;
		}
		if ("comment".equals(attrName)) {
			return ATTR_COMMENT;
		}
		if (attrName.contains("color")) {// 过于简单
			return ATTR_COLOR;
		}
		if (attrName.contains("size")) {
			return ATTR_SIZE;
		}
		return ATTR_INVALID;
	}
	
	private static boolean isLegalAttrName(String value) {
		if (TextUtils.isEmpty(value) || value.equals("comment")) { // comment表明该键值对是个注释
			return false;
		}
		return true;
	}
	
	
	public static String getFileContent(File file){
		FileInputStream fis = null;
		InputStreamReader reader = null;
		StringBuilder content = new StringBuilder("");
		if(!file.exists()){
			return "";
		}
		try {
			fis = new FileInputStream(file);
			reader = new InputStreamReader(fis);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String lineTxt = null;
			while ((lineTxt=bufferedReader.readLine())!=null) {
				content.append(lineTxt);
			}
			return content.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null)
					fis.close();
				if(reader!=null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	private static String mBgName = null;
	
	public static String getDialogBackgroundName(){
		if (mBgName == null) {
			try {
				mBgName = LayouUtil.getString("dialog_background");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mBgName;
	}
	
	private static Boolean mChatAnimation = null;
	
	public static boolean getChatAnimation(boolean defvalue){
		if (mChatAnimation == null) {
			try {
				mChatAnimation = defvalue;
				String animation = LayouUtil.getString("chat_list_animation");
				if ("true".equals(animation)) {
					mChatAnimation = true;
				}else if ("false".equals(animation)) {
					mChatAnimation = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mChatAnimation;
	}

	public static void enableChatAnimation(boolean enable) {
		mChatAnimation = enable;
		mCharAnimationAtFirst = enable;
	}

	private static Boolean mCharAnimationAtFirst = null;

	public static boolean getChatAnimationAtFirst(boolean defvalue){
		if (mCharAnimationAtFirst == null) {
			try {
				mCharAnimationAtFirst = defvalue;
				String animation = LayouUtil.getString("chat_list_animation_at_first");
				if ("true".equals(animation)) {
					mCharAnimationAtFirst = true;
				}else if ("false".equals(animation)) {
					mCharAnimationAtFirst = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mCharAnimationAtFirst;
	}

	private static Integer mLayoutPaddingLeft = null;
	
	public static Integer getLayoutPaddingLeft(){
		if (mLayoutPaddingLeft == null) {
			mLayoutPaddingLeft = 0;
			try {
				String str = LayouUtil.getString("layout_padding_left");
				if (!TextUtils.isEmpty(str)) {
					mLayoutPaddingLeft = Integer.parseInt(str);
				}
			} catch (Exception e) {
				LogUtil.loge("getPaddingLeft error!");
				e.printStackTrace();
			}
		}
		return mLayoutPaddingLeft;
	}
	
	private static Integer mLayoutPaddingTop = null;
	
	public static Integer getLayoutPaddingTop(){
		if (mLayoutPaddingTop == null) {
			mLayoutPaddingTop = 0;
			try {
				String str = LayouUtil.getString("layout_padding_top");
				if (!TextUtils.isEmpty(str)) {
					mLayoutPaddingTop = Integer.parseInt(str);
				}
			} catch (Exception e) {
				LogUtil.loge("getPaddingTop error!");
				e.printStackTrace();	
			}
		}
		return mLayoutPaddingTop;
	}
	
	private static Integer mLayoutPaddingRight = null;
	
	public static Integer getLayoutPaddingRight(){
		if (mLayoutPaddingRight == null) {
			mLayoutPaddingRight = 0;
			try {
				String str = LayouUtil.getString("layout_padding_right");
				if (!TextUtils.isEmpty(str)) {
					mLayoutPaddingRight = Integer.parseInt(str);
				}
			} catch (Exception e) {
				LogUtil.loge("getPaddingRight error!");
				e.printStackTrace();	
			}
		}
		return mLayoutPaddingRight;
	}
	
	private static Integer mLayoutPaddingBottom = null;
	
	public static Integer getLayoutPaddingBottom(){
		if (mLayoutPaddingBottom == null) {
			mLayoutPaddingBottom = 0;
			try {
				String str = LayouUtil.getString("layout_padding_bottom");
				if (!TextUtils.isEmpty(str)) {
					mLayoutPaddingBottom = Integer.parseInt(str);
				}
			} catch (Exception e) {
				LogUtil.loge("getPaddingBottom error!");
				e.printStackTrace();	
			}
		}
		return mLayoutPaddingBottom;
	}


	public static Integer mDisplayAreaX = null;
	public static Integer mDisplayAreaY = null;
	public static Integer mDisplayAreaWidth = null;
	public static Integer mDisplayAreaHeight = null;
	public static void updateDisplayArea(int x, int y, int width, int height) {
		mDisplayAreaX = x;
		mDisplayAreaY = y;
		mDisplayAreaWidth = width;
		mDisplayAreaHeight = height;
	}

	/** 支持自定义声控图标
	 * @return
	 */
	public static boolean isCustomFloatView(){
		if (WinLayoutManager.getInstance().getFloatView() == null) {
			return false;
		}
		return true;
	}

	private static Boolean mSupportMoreRecordState = null;
	public static boolean supportMoreRecordState(){
		return supportMoreRecordState(false);
	}

	public static boolean supportMoreRecordState(boolean forceUpdate){
		if (mSupportMoreRecordState == null || forceUpdate) {
			mSupportMoreRecordState = false;
			try {
				String str = LayouUtil.getString("support_more_record_state");
				mSupportMoreRecordState = TextUtils.equals(str,"true");
			} catch (Exception e) {
				LogUtil.loge("get mSupportMoreRecordState error!");
			}
		}
		return mSupportMoreRecordState;
	}

	public static BaseSceneInfoForward getSceneInfoForward(){
		if(mBaseSceneInfoForward == null){
			String themeConfigPrefix = getThemeConfigPrefix();
			mBaseSceneInfoForward = ((BaseSceneInfoForward) UIResLoader.getInstance().getClassInstance(themeConfigPrefix+"SceneInfoForward"));
		}
		return mBaseSceneInfoForward;
	}

	public static BaseAdvertisingControl getBaseAdvertisingControl(){
		if(mBaseAdvertisingControl == null){
			String themeConfigPrefix = getThemeConfigPrefix();
			mBaseAdvertisingControl = ((BaseAdvertisingControl) UIResLoader.getInstance().getClassInstance(themeConfigPrefix+"AdvertisingControl"));
		}
		return mBaseAdvertisingControl;
	}

	public static boolean isUseSceneInfo(){
		if (mUseSceneInfo == null) {
			if (TextUtils.equals(LayouUtil.getString("config_use_scene_info"),"true")) {
				mUseSceneInfo = true;
			}else {
				mUseSceneInfo = false;
			}
		}
		return  mUseSceneInfo;
	}

	public static boolean isUseFullScreenFlag(){
		if (mUseFullScreenFlag == null) {
			if (TextUtils.equals(LayouUtil.getString("use_fullscreen_flag"),"false")) {
				mUseFullScreenFlag = false;
			}else {
				mUseFullScreenFlag = true;
			}
		}
		return  mUseFullScreenFlag;
	}
	public static boolean isPriorityResHolderRes(){
		return isPriorityResHolderRes(false);
	}
	public static boolean isPriorityResHolderRes(boolean bForce){
		if (mPriorityResHolderRes == null || bForce) {
			if (TextUtils.equals(LayouUtil.getString("priority_ResHolder_res"),"true")) {
				mPriorityResHolderRes = true;
			}else {
				mPriorityResHolderRes = false;
			}
		}
		return mPriorityResHolderRes;
	}


    public static boolean enableHelpListBackIcon() {
		if (mEnableHelpListBackIcon == null) {
			if (TextUtils.equals(LayouUtil.getString("enable_help_list_back_icon"),"true")) {
				mEnableHelpListBackIcon = true;
			}else {
				mEnableHelpListBackIcon = false;
			}
		}
		return mEnableHelpListBackIcon;
    }
}
