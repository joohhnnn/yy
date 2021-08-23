package com.txznet.comm.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.os.Environment;

/**
 * 资源、配置文件存放路径常量及获取
 * 
 * @author Terry
 *
 */
public class FilePathConstants {

	private FilePathConstants(){}
	
	
	/**
	 * 文件存放路径1(给方案商使用)
	 */
	public static final String BASE_PATH_USER1 = "/etc/txz/";
	/**
	 * 文件存放路径2(给方案商使用)
	 */
	public static final String BASE_PATH_USER2 = "/system/txz/";
	/**
	 * 文件存放路径3(给方案商使用)(方易通嫌新建目录拷贝文件麻烦，希望放在/system/app/下跟apk一起拷贝过去)
	 */
	public static final String BASE_PATH_USER3 = "/system/app/";
	/**
	 * 文件存放路径4(给方案商使用)(达讯嫌放在/system/路径下需要修改系统分区，/custom/etc/是mtk推荐的配置文件存放位置)
	 */
	public static final String BASE_PATH_USER4 = "/custom/etc/";
	/**
	 * 文件存放路径5(给方案商使用)(浩科-北斗星通，OS和App是分开打包的，App所有的配置文件都要求放/vendor/txz)
	 */
	public static final String BASE_PATH_USER5 = "/vendor/txz/";
	/**
	 * 文件存放路径6(给方案商使用)(四维-百变车机，不接受创建现有的目录路径)
	 */
	public static final String BASE_PATH_USER6 = "/etc/";
	/**
	 * 文件存放路径7(给方案商使用)(方易通说配置文件会影响他们ota升级，所以我们只能将配置文件放在和apk一个路径下)
	 */
	public static final String BASE_PATH_USER7 = "/oem/app/";

	/**
	 * 文件存放路径8(给方案商使用)(艾米说这个是他们设置好的目录结构，不能随便更改，需要新增一个路径)
	 */
	public static final String BASE_PATH_USER8 = "/system/etc/customize/txz/";

	/**
	 * 文件存放路径9(给方案商使用)之前tts有自定义这个路径
	 */
	public static final String BASE_PATH_USER9 = "/system/etc/";

	/**
	 * 文件存放路径9(给方案商使用) 【卓兴威】他们Android10平台上需要加一个新的配置文件目录： odm/app
	 */
	public static final String BASE_PATH_USER10 = "/odm/app/";

	
	/**
	 * 文件存放优先路径(预留给我们用于下发或者运营)
	 */
	public static final String BASE_PATH_PRIOR = Environment.getExternalStorageDirectory() +"/txz/";

	/**
	 * 所有自定义用户路径的数组，每增加一个路径，都需要在这边增加
	 */
	public static final String[] BASE_PATH_USER_ARRAY = {
			BASE_PATH_USER1,
			BASE_PATH_USER2,
			BASE_PATH_USER3,
			BASE_PATH_USER4,
			BASE_PATH_USER5,
			BASE_PATH_USER6,
			BASE_PATH_USER7,
			BASE_PATH_USER8,
			BASE_PATH_USER9,
			BASE_PATH_USER10
	};
	
	// ////////////////////// 皮肤包资源//////////////////////////////
	public static final String SKIN_FILE_PRIOR_RESOURCE = BASE_PATH_PRIOR + "resource/ResHolder.apk"; // 优先读取资源路径
	public static String SKIN_FILE_RESOURCE_DEFAULT; // 默认资源文件路径/data/ResHolder.apk
	public static String mSkinUserConfigPath = null; // 用户自定义的皮肤包路径

	/**
	 * 获取用户自定义皮肤包的路径
	 * @return
	 */
	public static ArrayList<String> getUserSkinPath(){
		ArrayList<String> mSkinFileResource = new ArrayList<String>();
		for (String basePath: BASE_PATH_USER_ARRAY ) {
			mSkinFileResource.add(basePath + "resource/ResHolder.apk");
		}
		return mSkinFileResource;
	}
	
	/**
	 * 查找应用位于系统目录下的某文件
	 * <br>
	 * <b>
	 * 注意：只要找到结果立刻返回，多目录下配置同名文件会导致配置文件无法生效<br>
	 * 更新添加系统配置文件是需要修改该方法
	 * </b>
	 * @param fileName 配置文件根目录的相对路径
	 * @return 查找到文件可读时才返回文件目录，如果没有找到返回{@code null}
	 */
	public static String getSystemConfigPath(String fileName) {
		String filePath;
		for (String basePath : BASE_PATH_USER_ARRAY) {
			filePath = basePath + fileName;
			File file = new File(filePath);
			if (file.canRead()) {
				return filePath;
			}
		}
		return null;
	}
	
	/**
	 * 优先查找SD卡目录下的某文件
	 * <br>
	 * <b>
	 * 注意：只要找到结果立刻返回，多目录下配置同名文件会导致配置文件无法生效<br>
	 * </b>
	 * @param fileName 配置文件根目录的相对路径
	 * @return 查找到文件可读时才返回文件目录，如果没有找到返回{@code null}
	 */
	public static String getConfigPathPriorityOfSD(String fileName) {
		String filePath = BASE_PATH_PRIOR + fileName;
		File file = new File(filePath);
		if (file.canRead()) {
			return filePath;
		}
		return getSystemConfigPath(fileName);
	}
	
	public static String getSkinResourceFile() {
		File priorFile = new File(SKIN_FILE_PRIOR_RESOURCE);
		if(priorFile.exists()){
			return SKIN_FILE_PRIOR_RESOURCE;
		}
		if (mSkinUserConfigPath != null) {
			File userConfigFile = new File(mSkinUserConfigPath);
			if (userConfigFile.exists()) {
				return mSkinUserConfigPath;
			}
			LogUtil.logw("resApkPath:" + mSkinUserConfigPath + " is set but file not exist!");
		}

		List<String> userFilePaths = getUserSkinPath();

		for (String userFilePath : userFilePaths) {
			File userFile = new File(userFilePath);
			if(userFile.exists()){
				return userFilePath;
			}
		}
		SKIN_FILE_RESOURCE_DEFAULT = GlobalContext.get().getApplicationInfo().dataDir+"/data/ResHolder.apk";
		return SKIN_FILE_RESOURCE_DEFAULT;
	}
	
	//	/////////////////////////// UI字体及颜色 //////////////////////
	public static final String UI_THEME_FILE_PRIOR = BASE_PATH_PRIOR + "theme.cfg";

	/**
	 * 获取用户配置主题的路径列表
	 * @return
	 */
	public static ArrayList<String > getUserUiThemePath(){
		ArrayList<String > mUiThemePath = new ArrayList<String>();
		for (String basePath: BASE_PATH_USER_ARRAY ) {
			mUiThemePath.add(basePath + "theme.cfg");
		}
		return mUiThemePath;
	}
	
	// ////////////////////////////配置文件///////////////////////////
	// 给方案公司使用的位置: /system/txz/ /system/app/ /etc/txz/ /custom/etc/
	public static final String CONFIG_PATH_PRIOR = BASE_PATH_PRIOR; // 用户后续下发运营
	public static final String CONFIG_PATH_UPGRADE = BASE_PATH_PRIOR; // 用于升级的文件路径
	public static String CONFIG_PATH_DEFAULT; // 默认资源文件路径 data/data/com.txznet.xxx/data/cfg
	public static String mConfigFileName = null; // 包名+.cfg com.txznet.txz.cfg
	public static final String FILE_NAME_COMM_CONFIG = "comm.txz.cfg"; // comm 配置文件，多个app共同用到的配置项
	public static String mConfigFileNameUpgrade = null; // 包名+.cfg.upgrade   com.txznet.txz.cfg.upgrade

	/**
	 * 获取用户配置文件列表
	 * @return
	 */
	public static ArrayList<String > getUserConfigPath(){
		ArrayList<String> mUserConfigPath = new ArrayList<String>();
		mUserConfigPath.addAll(Arrays.asList(BASE_PATH_USER_ARRAY));
		return mUserConfigPath;
	}
	
	//	/////////////////////////// TTS语音包 //////////////////////
	/**配置文件自定义tts主题路径*/
	public static final String TTS_THEME_PATH_CUSTOM = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_TTS_THEME_CUSTOM_PATH, String.class, "/system/etc/tts_role_null/");

	/**
	 * 获取tts存储路径列表
	 * @return
	 */
	public static ArrayList<String > getUserTtsThemePath(){
		ArrayList<String > mUserTtsThemePath = new ArrayList<String>();
		for (String basePath : BASE_PATH_USER_ARRAY ) {
			mUserTtsThemePath.add(basePath + "tts_role" + File.separator);
		}
		mUserTtsThemePath.add(TTS_THEME_PATH_CUSTOM);
		return mUserTtsThemePath;
	}

	/**
	 * 获取tts存储路径列表
	 * 优先加入默认的路径
	 * @return
	 */
	public static ArrayList<String > getUserTtsThemePathRoot(){
		ArrayList<String > mUserTtsThemePath = new ArrayList<String>();
		mUserTtsThemePath.add(TTS_THEME_PATH_PRIOR);
		for (String basePath : BASE_PATH_USER_ARRAY ) {
			mUserTtsThemePath.add(basePath + "tts_role" + File.separator);
		}
		mUserTtsThemePath.add(TTS_THEME_PATH_CUSTOM);
		return mUserTtsThemePath;
	}

	/** 本地路径 */
	public static final String TTS_THEME_PATH_PRIOR = BASE_PATH_PRIOR + "tts_role/";
	
	///////////////////////////////解压的设置apk路径////////////////////
	public static final String TXZ_SETTING_PATH = BASE_PATH_PRIOR + "apk/TXZSetting.apk";

	///////////////////////////////解压的帮助详情///////////////////////
	//自定义帮助存放目录
	public static final String DEFAULT_HELP_DIR = BASE_PATH_PRIOR +"help";
	//解压后的帮助目录
	public static final String DEFAULT_HELP_FILE_TEMP_DIR = DEFAULT_HELP_DIR + File.separator +"helptmp";
	public static final String DEFAULT_HELP_FILE_TEMP_PATH = DEFAULT_HELP_FILE_TEMP_DIR + File.separator +"help.txt";
	//最终的帮助目录
	public static final String DEFAULT_HELP_FILE_DIR = DEFAULT_HELP_DIR + File.separator +"help";
	public static final String DEFAULT_HELP_FILE_PATH = DEFAULT_HELP_FILE_DIR + File.separator +"help.txt";
	//帮助的zip文件
	public static final String DEFAULT_HELP_FILE = DEFAULT_HELP_DIR + File.separator + "help.zip";
	//帮助详情存放目录备份
	public static final String DEFAULT_HELP_FILE_BACKUP_DIR = DEFAULT_HELP_DIR + File.separator + "helpbak";

	//增加从系统路径中读取帮助内容，help.txt和imgs 放在这个目录下/etc/txz/help/help/
	public static ArrayList<String> getUserHelpPath(){
		ArrayList<String > mUserHelpPath = new ArrayList<String>();
		for (String basePath: BASE_PATH_USER_ARRAY ) {
			mUserHelpPath.add(basePath + "help" + File.separator + "help");
		}
		return mUserHelpPath;
	}

	/**
	 * 获取外部媒体工具装载路径列表
	 * @return 路径列表
	 */
	public static ArrayList<String> getMediaToolPath() {
		ArrayList<String> pathList = new ArrayList<String>(BASE_PATH_USER_ARRAY.length);
		for (String path : BASE_PATH_USER_ARRAY) {
			pathList.add(path + "media_tool/");
		}

		return pathList;
	}

}
