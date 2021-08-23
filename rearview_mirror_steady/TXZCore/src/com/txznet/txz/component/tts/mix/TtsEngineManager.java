package com.txznet.txz.component.tts.mix;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.text.TextUtils;

import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallback;
import com.txznet.txz.component.tts.ITts.TTSOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.tts.TtsManager;

/**
 * 引擎切换逻辑：
 * 1. 切换主进程TTS引擎，在new引擎初始化成功后，停止释放子进程TTS引擎，再去停止释放old引擎
 * 2. 切换子进程TTS引擎，停止释放子进程TTS引擎，然后初始化new引擎
 */
public class TtsEngineManager {
	
	/** 当前播报的引擎，在停止播报时使用。优先使用代理引擎播报文本。 */
	private TtsEngine mLastTtsEngine = null;

	/** 默认的TTS引擎 or 内部引擎*/
	private TtsEngine mDefaultEngine = null;
	
	/** 代理引擎，子进程中的引擎 */
	private TtsEngine mProxyEngine = null;

	private static TtsEngineManager sInstance = new TtsEngineManager();

	private TtsEngineManager() {
	}

	public static TtsEngineManager getInstance() {
		return sInstance;
	}

	private void loadOuterEngines(List<TtsEngine> engineList) {
		if (engineList == null) {
			JNIHelper.logd("engineList == null");
			return;
		} 
		for (String ttsThemePath : FilePathConstants.getUserTtsThemePathRoot()) {
			File file = new File(ttsThemePath);
			if (file.exists() && file.isDirectory() && file.canRead()) {
				String[] list = file.list();
				for (String strName : list) {
					File zipFile = new File(file, strName);
					// 规避一个坑爹的问题，文件名显示乱码，找不到这个文件
					if (!zipFile.exists()) {
						JNIHelper.logd("tts theme : " + zipFile.getPath() + "not exists");
						continue;
					}
					TtsEngine engine = OuterTtsEngine.getTtsEngine(zipFile);
					if (engine != null) {
						engineList.add(engine);
					}
				}
			} else {
				JNIHelper.logd("tts theme : " + file.getPath() + " not readable or directory or exist");
			}
		}
	}
	
	private TtsEngine getInnerEngine() {
		TtsEngine inner = new InnerTtsEngine();
		inner.setId(TtsTheme.TTS_THEME_DEFAULT_THEME_ID);
		inner.setName(TtsTheme.TTS_THEME_DEFAULT_THEME_NAME);
		inner.setLanguage(TtsTheme.LANGUAGE_PUTONGHUA);
		inner.setSex(TtsTheme.SEX_FEMALE);
		inner.setAge(TtsTheme.AGE_YOUTH);
		inner.setPriority(60);
		String className = null;
		if (TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
			className = "com.txznet.txz.component.tts.yunzhisheng_3_0.TtsYunzhishengImpl";
		} else {
			className = ImplCfg.getTtsImplClass();
		}
		inner.setClassName(className);
		return inner;
	}

	public int initInnerEngine(final IInitCallback oRun) {
		JNIHelper.logd("tts theme: init tts engine : inner tts engine");
		
		if (mDefaultEngine != null && mDefaultEngine.getId() == TtsTheme.TTS_THEME_DEFAULT_THEME_ID) {
			if (mLastTtsEngine != null) {
				mLastTtsEngine.getEngine().stop();
				mLastTtsEngine = null;
			}
			if (mProxyEngine != null) {
				mProxyEngine.release();
				mProxyEngine = null;
			}
			// 解决本地云知声标准女声优先级最低问题，
			mDefaultEngine.getEngine().setTtsModel(null);
			if (oRun != null) {
				oRun.onInit(true);
			}
			return ITts.ERROR_SUCCESS;
		}
		
		final TtsEngine inner = getInnerEngine();
	
		return inner.getEngine().initialize(new IInitCallback() {
	
			@Override
			public void onInit(boolean bSuccess) {
				inner.setInited(true);
				inner.setInitSuccessed(bSuccess);
				if (bSuccess) {
					if (mLastTtsEngine != null) {
						mLastTtsEngine.getEngine().stop();
						mLastTtsEngine = null;
					}
					if (mProxyEngine != null) {
						mProxyEngine.release();
						mProxyEngine = null;
					}
					// 初始化成功，设为默认引擎
					if (mDefaultEngine != null) {
						mDefaultEngine.release();
					}
					mDefaultEngine = inner;
					// 解决本地云知声标准女声优先级最低问题，
					mDefaultEngine.getEngine().setTtsModel(null);
				}
				if (oRun != null) {
					oRun.onInit(bSuccess);
				}
			}
		});
	}
	
	public int initializeTtsEngine(String filePath, IInitCallback oRun) {
		JNIHelper.logd("tts theme: init tts engine: " + filePath);

		TtsEngine ttsEngine = OuterTtsEngine.getTtsEngine(new File(filePath));
		if (ttsEngine == null) {
			// 没有找到相关引擎
			return initInnerEngine(oRun);
		} else {
			return initializeTtsEngine(ttsEngine, oRun);
		}
	}
	
	public int initializeTtsEngine(String language, int sex, int age, int priority, IInitCallback oRun) {
		JNIHelper.logd("tts theme: init tts engine :" + language + " / " + sex + " / " + age +" / " + priority);
		TtsEngine ttsEngine = getTtsEngine(language, sex, age, priority);
		if (ttsEngine == null) {
			// 没有找到相关引擎
			return initInnerEngine(oRun);
		} else {
			return initializeTtsEngine(ttsEngine, oRun);
		}
	}

	/**
	 * 开机启动或切换主题时初始化引擎，保证tts引擎存在
	 * 
	 * @param ttsEngine
	 * @param oRun
	 * @return
	 */
	private int initializeTtsEngine(final TtsEngine ttsEngine, final IInitCallback oRun) {
		JNIHelper.logd("tts theme: init tts engine " + ttsEngine.getName());
		// 初始化默认TTS引擎
		if (ttsEngine.getId() == TtsTheme.TTS_THEME_DEFAULT_THEME_ID) {
			return initInnerEngine(oRun);
		}
		// 主进程TTS引擎没有初始化，在主进程中初始化一个默认的TTS引擎
		if (mDefaultEngine == null) { 
			if (ttsEngine.getEngine() == null) {
				return initInnerEngine(oRun);
			} else {
				return ttsEngine.getEngine().initialize(new IInitCallback() {

					@Override
					public void onInit(boolean bSuccess) {
						JNIHelper.logd("tts theme: init tts : default engine : " + bSuccess);
						ttsEngine.setInited(true);
						ttsEngine.setInitSuccessed(bSuccess);
						if (bSuccess) {
							mDefaultEngine = ttsEngine;
							if (oRun != null) {
								oRun.onInit(bSuccess);
							}
						} else {
							initInnerEngine(oRun);
						}
					}

				});
			}
		}
		
		// 与当前正在使用代理引擎相同
		if (mProxyEngine != null && ttsEngine.getId() == mProxyEngine.getId()) {
			oRun.onInit(true);
			return ITts.ERROR_SUCCESS;
		}
		
		// 切换代理引擎，先清除以前的代理
		if (mProxyEngine != null) {
			mProxyEngine.getEngine().stop();
			mProxyEngine.release();
			mProxyEngine = null;
		}
		
		if (mDefaultEngine != null && ttsEngine.getId() == mDefaultEngine.getId()) {
			oRun.onInit(true);
			return ITts.ERROR_SUCCESS;
		}
		
		// 使用代理，在子进程初始化 TTS 引擎，为代理引擎设置外部主题包路径
		TtsProxy proxy = TtsProxy.getInstance();
		proxy.setTtsEngineFilePath(ttsEngine.getFilePath());
		ttsEngine.setEngine(proxy);
		
		return ttsEngine.getEngine().initialize(new IInitCallback() {

			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("tts theme: init tts : proxy engine : " + bSuccess);
				ttsEngine.setInited(true);
				ttsEngine.setInitSuccessed(bSuccess);
				if (bSuccess) {
					// 初始化成功，设为默认引擎
					mProxyEngine = ttsEngine;
				} else {
					// 初始化失败，释放引擎资源
					ttsEngine.clearEngine();
				}
				if (oRun != null) {
					oRun.onInit(bSuccess);
				}
			}
		});
	}

	/**
	 * 根据传入的特性属性找出最接近的tts引擎
	 * @param language
	 * @param sex
	 * @param age
	 * @param priority
	 * @return
	 */
	private TtsEngine getTtsEngine(String language, int sex, int age, int priority) {
		if (TextUtils.isEmpty(language)) {
			return null;
		}
		List<TtsEngine> mEngineList = new LinkedList<TtsEngine>();
		// 添加内置的TTS引擎,内部引擎只使用一个
		mEngineList.add(getInnerEngine());
		// 添加外部TTS引擎
		loadOuterEngines(mEngineList);

		List<TtsEngine> engineList = new ArrayList<TtsEngine>();
		int len = mEngineList.size();
		for (int i = 0; i < len; i++) {
			if (language.equalsIgnoreCase(mEngineList.get(i).getLanguage())) {
				engineList.add(mEngineList.get(i));
			}
		}
		if (engineList.size() == 0) {
			if (language.equalsIgnoreCase(TtsEngine.LANGUAGE_PUTONGHUA)) {
				return null;
			} 
			
			for (int i = 0; i < len; i++) {
				if (TtsEngine.LANGUAGE_PUTONGHUA.equalsIgnoreCase(mEngineList.get(i).getLanguage())) {
					engineList.add(mEngineList.get(i));
				}
			}
			
			if (engineList.size() == 0) {
				return null;
			}
		}
		if (engineList.size() == 1) {
			return engineList.get(0);
		}

		// 过滤性别
		List<TtsEngine> engineList1 = new ArrayList<TtsEngine>();
		for (int i = 0; i < engineList.size(); i++) {
			if (sex == engineList.get(i).getSex()) {
				engineList1.add(engineList.get(i));
			}
		}
		if (engineList1.size() == 1) {
			return engineList1.get(0);
		}
		if (engineList1.size() == 0) {
			engineList1 = engineList;
		}

		// 过滤年龄（找相同年龄的，没有则找年长的，没有则向下找。）
		List<TtsEngine> engineList2 = getTtsEngineByAge(engineList1,age);
		if (engineList2.size() == 1) {
			return engineList2.get(0);
		}
		if (engineList2.size() == 0) {
			engineList2 = engineList1;
		}

		// 根据权重(取最接近的)
		int diff = Integer.MAX_VALUE;
		int index = 0;
		for (int i = 0; i < engineList2.size(); i++) {
			if (diff > Math.abs(priority - engineList2.get(i).getPriority())) {
				diff = Math.abs(priority - engineList2.get(i).getPriority());
				index = i;
			}
		}
		return engineList2.get(index);
	}

	private List<TtsEngine> getTtsEngineByAge(List<TtsEngine> engineList,int age) {
		List<TtsEngine> engineList2 = new ArrayList<TtsEngine>();
		for (int i = 0; i < engineList.size(); i++) {
			if (age == engineList.get(i).getAge()) {
				engineList2.add(engineList.get(i));
			}
		}
		if (engineList2.size() != 0) {
			return engineList2;
		}
		
		if (age >= TtsEngine.AGE_OLD) {
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_OLD, Integer.MAX_VALUE);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_MIDDLE, TtsEngine.AGE_OLD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_YOUTH, TtsEngine.AGE_MIDDLE - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_CHILD, TtsEngine.AGE_YOUTH - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, 0, TtsEngine.AGE_CHILD - 1);
			}
		} else if (age >= TtsEngine.AGE_MIDDLE) {
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_MIDDLE, TtsEngine.AGE_OLD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_OLD, Integer.MAX_VALUE);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_YOUTH, TtsEngine.AGE_MIDDLE - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_CHILD, TtsEngine.AGE_YOUTH - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, 0, TtsEngine.AGE_CHILD - 1);
			}
		} else if (age >= TtsEngine.AGE_YOUTH) {
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_YOUTH, TtsEngine.AGE_MIDDLE - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_MIDDLE, TtsEngine.AGE_OLD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_OLD, Integer.MAX_VALUE);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_CHILD, TtsEngine.AGE_YOUTH - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, 0, TtsEngine.AGE_CHILD - 1);
			}
		} else if (age >= TtsEngine.AGE_CHILD) {
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_CHILD, TtsEngine.AGE_YOUTH - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_YOUTH, TtsEngine.AGE_MIDDLE - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_MIDDLE, TtsEngine.AGE_OLD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_OLD, Integer.MAX_VALUE);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, 0, TtsEngine.AGE_CHILD - 1);
			}
		} else {
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, 0, TtsEngine.AGE_CHILD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_CHILD, TtsEngine.AGE_YOUTH - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_YOUTH, TtsEngine.AGE_MIDDLE - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_MIDDLE, TtsEngine.AGE_OLD - 1);
			}
			if (engineList2.size() == 0) {
				engineList2 = getTtsEngineByAge(engineList, TtsEngine.AGE_OLD, Integer.MAX_VALUE);
			}
		}
		return engineList2;
	}
	
	private List<TtsEngine> getTtsEngineByAge(List<TtsEngine> engineList,int l, int r) {
		List<TtsEngine> engineList2 = new ArrayList<TtsEngine>();
		for (int i = 0; i < engineList.size(); i++) {
			int age = engineList.get(i).getAge();
			if (l <= age && age <= r) {
				engineList2.add(engineList.get(i));
			}
		}
		return engineList2;
	}
	
	/**
	 * 获取当前可使用的播报文本的引擎
	 * 当子进程的引擎存在时，使用子进程播报，否则使用默认的播报。
	 */
	private TtsEngine getTtsEngine() {
		if (mProxyEngine != null && mProxyEngine.isInitSuccessed()) {
			return mProxyEngine;
		}

		return mDefaultEngine;
	}

	public int speakText(int iStream, String sText, ITtsCallback oRun) {
		mLastTtsEngine = getTtsEngine();
		if (mLastTtsEngine == null) {
			JNIHelper.logd("inner engine is null, not init tts");
			return ITts.ERROR_UNKNOW;
		}
		mLastTtsEngine.getEngine().start(iStream, sText, oRun);
		return 0;
	}


	public void release() {
		mLastTtsEngine = null;
		if (mProxyEngine != null) {
			mProxyEngine.release();
			mProxyEngine = null;
		}
		if (mDefaultEngine != null) {
			mDefaultEngine.release();
			mDefaultEngine = null;
		}
	}

	public void stop() {
		if (mLastTtsEngine != null) {
			mLastTtsEngine.getEngine().stop();
			mLastTtsEngine = null;
		}
	}

	public boolean isBusy() {
		if (mLastTtsEngine != null) {
			return mLastTtsEngine.getEngine().isBusy();
		}
		return false;
	}

	public int pause() {
		if (mLastTtsEngine != null) {
			return mLastTtsEngine.getEngine().pause();
		}
		return 0;
	}

	public int resume() {
		if (mLastTtsEngine != null) {
			return mLastTtsEngine.getEngine().resume();
		}
		return 0;
	}

	public void setVoiceSpeed(int speed) {
		TtsEngine ttsEngine = getTtsEngine();
		if (ttsEngine != null) {
			ITts tts = ttsEngine.getEngine();
			if (tts != null) {
				tts.setVoiceSpeed(speed);
			}
		}
	}

	public int getVoiceSpeed() {
		TtsEngine ttsEngine = getTtsEngine();
		if (ttsEngine != null) {
			ITts tts = ttsEngine.getEngine();
			if (tts != null) {
				return tts.getVoiceSpeed();
			}
		}
		return 60;
	}
	
	public void setTtsModel(String ttsModel) {
		TtsEngine ttsEngine = getTtsEngine();
		if (ttsEngine != null) {
			ITts tts = ttsEngine.getEngine();
			if (tts != null) {
				tts.setTtsModel(ttsModel);
			}
		}
	}
	
	public int setLanguage(Locale loc) {
		TtsEngine ttsEngine = getTtsEngine();
		if (ttsEngine != null) {
			ITts tts = ttsEngine.getEngine();
			if (tts != null) {
				tts.setLanguage(loc);
			}
		}
		return 0;
	}
	
	public void setOption(TTSOption oOption) {
		TtsEngine ttsEngine = getTtsEngine();
		if (ttsEngine != null) {
			ITts tts = ttsEngine.getEngine();
			if (tts != null) {
				tts.setOption(oOption);
			}
		}
		return;
	}
}
