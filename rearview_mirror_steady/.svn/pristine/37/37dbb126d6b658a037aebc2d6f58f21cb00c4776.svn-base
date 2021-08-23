package com.txznet.comm.resource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.txznet.comm.remote.util.LogUtil;

import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;


/**
 * 更改为从apk读取，此部分代码不再使用
 * 
 * 
 * 加载资源配置文件，资源读取顺序： 
 *1、默认从 /etc/com.txznet.txz.skin读取配置
 *2、优先从/sdcard/txz/skin/com.txznet.txz.skin读取后台推送的主题
 *3、配置的图片路径支持相对路径和绝对路径，支持占位符
 *如%APK_ROOT%，是安装根目录
 *如%SDCARD%，是sd卡根目录
 *如%CFG_PATH%，是皮肤配置文件所在目录
 * 
 * @author ASUS User
 *
 */
public class ResConfigLoader {
	
	//配置文件保存目录
//	public static final String DEFAULT_SKIN_PATH = GlobalContext.get().getApplicationInfo().dataDir+"/etc/com.txznet.skin";
	public String DEFAULT_SKIN_PATH = "";
	public String PRIOR_SKIN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/txz/skin/com.txznet.skin";
	private static ResConfigLoader sInstance = new ResConfigLoader();		

	//配置文件名称
	public static String COLOR_FILE = "colors.xml";
	public static String DRAWABLE_FILE = "drawables.xml";
	public static String DIMEN_FILE = "dimenss.xml";
	
	//占位符
	public static final String REPLACE_INSTALL = "%APK_ROOT%"; //安装根目录
	public static final String REPLACE_SDCARD = "%SDCARD%"; //SDCARD根目录
	public static final String REPLACE_SKIN = "%CFG_PATH%"; //皮肤配置文件所在目录
	
	
	public static final int LOAD_SUCCESS = 0;
	public static final int LOAD_NO_EXIST = -1;
	public static final int LOAD_UNKONWN_ERROR = -2;
	
	public String mSkinCfgDir = null;
	
	public static ResConfigLoader getInstance(){
		return sInstance;
	}

	public int loadConfig(Application application) {
		initCfgPath(application);
		return loadConfigFile(application);
	}
	
	private void initCfgPath(Application application) {
		DEFAULT_SKIN_PATH = "/etc/" + application.getApplicationInfo().packageName + ".skin";
		PRIOR_SKIN_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/skin/"
				+ application.getApplicationInfo().packageName + ".skin";
	}

	private HashMap<String, Integer> colors = null;
	private HashMap<String, Drawable> drawables = null;
	
	
	public HashMap<String, Integer> getColors(){
		return colors;
	}
	
	public HashMap<String, Drawable> getDrawables(){
		return drawables;
	}
	
	private int loadConfigFile(Application application){
		LogUtil.logd("DEFAULT_SKIN_PATH:"+DEFAULT_SKIN_PATH+
				",PRIOR_SKIN_PATH:"+PRIOR_SKIN_PATH+
				",COLOR_FILE "+COLOR_FILE);
		mSkinCfgDir = getSkinConfigDir();
		if(TextUtils.isEmpty(mSkinCfgDir)){
			LogUtil.loge("TXZResources configDir NULL");
			return -1;
		}
		File file = new File(mSkinCfgDir);
		if(!file.exists()||!file.isDirectory()){
			LogUtil.loge("TXZResources configDir not exist");
			return -1;
		}
		colors = new HashMap<String, Integer>();
		drawables = new HashMap<String, Drawable>();
		File colorCfgFile = new File(mSkinCfgDir+"/"+COLOR_FILE);
		if (colorCfgFile.exists()) {
			colors = readColorConfig(colorCfgFile);
		}
		if (colors != null) {
			LogUtil.logd("TXZResources  colors " + colors.size());
		}
		File drawableCfgFile = new File(mSkinCfgDir+"/"+DRAWABLE_FILE);
		if (drawableCfgFile.exists()) {
			drawables = readDrawableCfgFile(drawableCfgFile, application);
		}
		return 0;
	}

	/**
	 * 从Drawable配置文件读取对应的color
	 */
	private HashMap<String, Drawable> readDrawableCfgFile(File drawableCfgFile,Application application) {
		HashMap<String, Drawable> drawables = new HashMap<String, Drawable>();
		DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
			Document document = dBuilder.parse(drawableCfgFile);
			NodeList list = document.getElementsByTagName("drawable");
			for(int i=0;i<list.getLength();i++){
				Element element = (Element) list.item(i);
				String nameString = element.getAttribute("name");
				String contentString = element.getTextContent();
				ColorDrawable colorDrawable = getColorDrawable(contentString);
				if(colorDrawable!=null){
					drawables.put(nameString, colorDrawable);
				}else {
					Drawable drawable = getFileDrawable(contentString,application);
					if(drawable!=null)
						drawables.put(nameString, drawable);
				}
			}
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return drawables;
	}

	private Drawable getFileDrawable(String contentString,Application application) {
		//TODO 未测试占位符是否可用
		contentString = contentString.replace(REPLACE_INSTALL, application.getApplicationInfo().dataDir);
		contentString = contentString.replace(REPLACE_SDCARD, Environment.getExternalStorageDirectory().getAbsolutePath());
		if (!TextUtils.isEmpty(mSkinCfgDir)) {
			contentString = contentString.replace(REPLACE_SKIN, mSkinCfgDir);
		}
		BitmapDrawable bitmapDrawable = new BitmapDrawable(application.getResources(), contentString);
		return bitmapDrawable;
	}


	private ColorDrawable getColorDrawable(String contentString) {
		if(contentString.startsWith("#")){
			long color;
			try {
				String colorStr = contentString.replace("#", "");
				if(colorStr.length() == 6){
					colorStr = "FF"+colorStr;
				}else if (colorStr.length() == 8) {
					
				}else {
					return null;
				}
				color = Long.parseLong(colorStr);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return null;
			}
			return new ColorDrawable((int) color);
		}
		return null;
	}

	/**
	 * 从颜色配置文件读取对应的color
	 */
	private HashMap<String, Integer> readColorConfig(File colorCfgFile) {
		HashMap<String, Integer> colors = new HashMap<String, Integer>();
		DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
			Document document = dBuilder.parse(colorCfgFile);
			NodeList list = document.getElementsByTagName("color");
			for(int i=0;i<list.getLength();i++){
				Element element = (Element) list.item(i);
				String nameString = element.getAttribute("name");
				String contentString = element.getTextContent();
				long color = Long.parseLong(contentString.replace("#", ""),16); //#FFFFFFFF直接解析会越界
				colors.put(nameString, (int) color);
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return colors;
	}

	/**
	 * 根据优先级得到皮肤配置文件的目录
	 * @return
	 */
	private String getSkinConfigDir() {
		File priorFile = new File(PRIOR_SKIN_PATH);
		if (priorFile.exists() && priorFile.isDirectory()) {
			return priorFile.getAbsolutePath();
		}
		File defaultFile = new File(DEFAULT_SKIN_PATH);
		if (defaultFile.exists() && defaultFile.isDirectory()) {
			return defaultFile.getAbsolutePath();
		}
		return null;
	}
	
}
