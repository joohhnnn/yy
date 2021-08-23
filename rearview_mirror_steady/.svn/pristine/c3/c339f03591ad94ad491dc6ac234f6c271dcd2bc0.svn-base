package com.txznet.txz.component.tts.mix;

import java.io.File;

import com.txznet.txz.util.MD5Util;

/**
 * <pre>
 * TTS语音包基本信息类:
 * {
 *     "id": 104, 
 *     "name": "宫廷版", 
 *     "version": 1, 
 *     "type": 1,
 *     "className": "com.txznet.txz.component.tts.baidu.TtsBaiduEmotionmaleImpl", 
 *     "roles": [
 *         "宫廷版", 
 *         "太监版"
 *     ], 
 *     "宫廷版": {
 *         "language": "putonghua", 
 *         "sex": 1, 
 *         "age": 20, 
 *         "priority": 100
 *     }, 
 *     "太监版": {
 *         "language": "putonghua", 
 *         "sex": 3, 
 *         "age": 60, 
 *         "priority": 100
 *     }
 * }
 * 对应字段说明
id：       主题ID（整型）
name：     主题名（字符型）
version：  主题版本号（整型）
type：     主题类型（整型，1 录音类型 | 2 引擎类型 | 3 混合类型）
roles：    主题包含角色（字符型）
宫廷版：   角色名，必须与roles中的值对应（字符型）
角色特性值：
language;  语种，默认普通话（字符型）
sex; 女female 1 |男male 2（整型）
age;  童10 青20 中40 老60（整型）
priority;  权重（整型）
 * </pre>
 * 
 * @author cain
 */
public class TtsTheme {

	/**
	 * 主题包包名后缀名
	 */
	public static final String TTS_THEME_PKT_SUFFIX = ".zip";
	/**
	 * 主题包标志文件名，里面包含主题相关的特性值
	 */
	public static final String TTS_THEME_PKT_FILE_TXZ = "txz";
	/**
	 * 主题为录音类型时存在，包含角色的特有的资源文本
	 */
	public static final String TTS_THEME_PKT_FILE_TEXT= "text";
	/**
	 * 主题包中包含TTS引擎时可能存在，加密文件
	 */
	public static final String TTS_THEME_PKT_FILE_DATA= "data";
	

	// id： 主题ID（整型）
	public static final String TTS_THEME_PKT_KEY_ID = "id";
	// name： 主题名（字符型）
	public static final String TTS_THEME_PKT_KEY_NAME = "name";
	// version： 主题版本号（整型）
	public static final String TTS_THEME_PKT_KEY_VERSION = "version";
	// type： 主题类型（整型，1 录音类型 | 2 引擎类型 | 3 混合类型）
	public static final String TTS_THEME_PKT_KEY_TYPE = "type";
	// roles： 主题包中的角色名（字符数组），主角名将作为key值去取角色对应的角色特性
	public static final String TTS_THEME_PKT_KEY_ROLES = "roles";
	// className：类完全限定名（字符型），只有在type类型为特定的值时，该属性才会存在
	public static final String TTS_THEME_PKT_KEY_CLASSNAME = "className";
	// suffix： 主题音频后缀  mp3 pcm wav
	public static final String TTS_THEME_PKT_KEY_SUFFIX = "suffix";
	// magic： 音频是否加密 MD5Util.generateMD5(nThemeId + "").charAt(themeType)
	public static final String TTS_THEME_PKT_KEY_MAGIC = "magic";

	// 角色特性值：
	// language; 语种，默认普通话（字符型）
	public static final String TTS_THEME_PKT_KEY_ROLE_LANGUAGE = "language";
	// sex; 女female 1 |男male 2（整型）|
	public static final String TTS_THEME_PKT_KEY_ROLE_SEX = "sex";
	// age; 童10 青20 中40 老60（整型）
	public static final String TTS_THEME_PKT_KEY_ROLE_AGE = "age";
	// priority; 权重（整型）
	public static final String TTS_THEME_PKT_KEY_ROLE_PRIORITY = "priority";

	public static final int TTS_THEME_DEFAULT_THEME_ID = 1;	// 默认主题主题ID
	public static final String TTS_THEME_DEFAULT_THEME_NAME = "";// 默认主题主题名称
	public static final String TTS_THEME_DEFAULT_THEME_ROLE = "";//默认主题角色

	public static final int TTS_THEME_TYPE_NONE = 0;
	public static final int TTS_THEME_TYPE_AUDIO = 1;
	public static final int TTS_THEME_TYPE_ENGINE = 2;
	public static final int TTS_THEME_TYPE_MIX = 3;
	
	public static final String TTS_THEME_SUFFIX_MP3 = ".mp3";
	public static final String TTS_THEME_SUFFIX_WAV = ".wav";
	public static final String TTS_THEME_SUFFIX_PCM = ".pcm";

	public static final String LANGUAGE_PUTONGHUA = "putonghua";

	public static final int SEX_FEMALE = 1;
	public static final int SEX_MALE = 2;

	public static final int AGE_CHILD = 10;
	public static final int AGE_YOUTH = 20;
	public static final int AGE_MIDDLE = 40;
	public static final int AGE_OLD = 60;

	/** 主题ID */
	private int mThemeId;
	/** 主题名称 */
	private String mThemeName;
	/** 主题包文件路径 */
	private String mFilePath;
	/** 主题包存放目录 */
	private String mDirPath;
	/** 主题类型 */
	private int mThemeType;
	/** 主题音频后缀 */
	private String mThemeAudioSuffix = TTS_THEME_SUFFIX_MP3;
	/** 主题音频后缀 */
	private String mThemeRole;
	
	private boolean isEncrypt = true;

	public TtsTheme() {
		super();
	}
	
	/**
	 * @return 主题包中是否存在音频音频数据
	 */
	public boolean hasAudio() {
		return mThemeType == TTS_THEME_TYPE_AUDIO || mThemeType == TTS_THEME_TYPE_MIX;
	}
	
	public void setDirPath(String parentPath) {
		mDirPath = parentPath;
	}
	
	public String getAudioPath(String strText) {
		return mDirPath + mThemeId + File.separator + MD5Util.generateMD5("TTS" + mThemeId + strText + "TXZ");
	}

	public int getThemeId() {
		return mThemeId;
	}

	public void setThemeId(int id) {
		mThemeId = id;
	}

	public String getThemeName() {
		return mThemeName;
	}

	public void setThemeName(String themeName) {
		mThemeName = themeName;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

	public int getThemeType() {
		return mThemeType;
	}

	public void setThemeType(int themeType) {
		mThemeType = themeType;
	}

	public String getThemeAudioSuffix() {
		return mThemeAudioSuffix;
	}

	public void setThemeAudioSuffix(String themeAudioSuffix) {
		mThemeAudioSuffix = themeAudioSuffix;
	}

	public String getThemeRole() {
		return mThemeRole;
	}

	public void setThemeRole(String themeRole) {
		mThemeRole = themeRole;
	}

	public boolean isEncrypt() {
		return isEncrypt;
	}

	public void setEncrypt(boolean isEncrypt) {
		this.isEncrypt = isEncrypt;
	}

}
