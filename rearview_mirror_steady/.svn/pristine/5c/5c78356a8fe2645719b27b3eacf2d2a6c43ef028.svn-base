package com.txznet.txz.component.tts.mix;

import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.module.tts.TtsManager;

public abstract class TtsEngine {
	
	public final static String OUTER_ENGINE_ASSETS_DIR = TtsManager.TTS_ROLE_ROOT + "assets/";
	public final static String OUTER_ENGINE_PACKAGE_DATA = "data";
	public final static String OUTER_ENGINE_PACKAGE_INFO = TtsTheme.TTS_THEME_PKT_FILE_TXZ;

	public static final String LANGUAGE_PUTONGHUA = TtsTheme.LANGUAGE_PUTONGHUA;
	
	public static final int SEX_MALE = TtsTheme.SEX_MALE;
	public static final int SEX_FEMALE = TtsTheme.SEX_FEMALE;
	
	public static final int AGE_CHILD = TtsTheme.AGE_CHILD;
	public static final int AGE_YOUTH = TtsTheme.AGE_YOUTH;
	public static final int AGE_MIDDLE = TtsTheme.AGE_MIDDLE;
	public static final int AGE_OLD = TtsTheme.AGE_OLD;

	public static enum TtsType {
		INNER, OUTER
	}

	private int mId;
	private int mVersion;
	private String mName;
	// ITts实现类的完全限定名
	protected String mClassName;
	
	protected String mFilePath;
	
	// 语种
	private String mLanguage = LANGUAGE_PUTONGHUA;
	// 童10 青20 中40 老60（默认0）
	private int age;
	// 女female 1 |男male 0（默认 男）
	private int mSex;
	// 引擎优先级
	private int mPriority;
	// 是否初始化
	private boolean mInited = false;
	// 是否初始化成功
	private boolean mInitSuccessed = false;

	protected ITts mTtsEngine = null;

	public abstract ITts getEngine();

	public abstract TtsType getType();
	
	public void setEngine(ITts tts) {
		 mTtsEngine = tts;
	}

	public void release() {
		if (mTtsEngine != null) {
			mTtsEngine.release();
			mTtsEngine = null;
		}
		mInited = false;
		mInitSuccessed = false;
	}
	
	public void clearEngine() {
		if (mTtsEngine != null) {
			mTtsEngine.release();
			mTtsEngine = null;
		}
	}

	public int getVersion() {
		return mVersion;
	}

	public void setVersion(int nVersion) {
		mVersion = nVersion;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String strName) {
		mName = strName;
	}

	public int getSex() {
		return mSex;
	}

	public void setSex(int nSex) {
		mSex = nSex;
	}

	public String getLanguage() {
		return mLanguage;
	}

	public void setLanguage(String language) {
		mLanguage = language;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getPriority() {
		return mPriority;
	}

	public void setPriority(int priority) {
		mPriority = priority;
	}

	public void setClassName(String strClassName) {
		mClassName = strClassName;
	}

	public String getClassName() {
		return mClassName;
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

	public boolean isInited() {
		return mInited;
	}

	public void setInited(boolean inited) {
		mInited = inited;
	}

	public boolean isInitSuccessed() {
		return mInitSuccessed;
	}

	public void setInitSuccessed(boolean initSuccessed) {
		mInitSuccessed = initSuccessed;
	}
}
