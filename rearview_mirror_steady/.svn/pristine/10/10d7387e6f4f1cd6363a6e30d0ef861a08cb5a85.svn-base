package com.txznet.comm.ui.resloader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.util.ScreenUtils.ScreenSizeChangeListener;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import dalvik.system.DexClassLoader;

/**
 * 资源加载优先级：/sdcard/txz/resource/prior/ResHolder.apk(优先读取，例如下发主题等)   <br>
 * 					大于 /etc/txz/resource/ResHolder.apk 或者 /system/txz/resource/ResHolder.apk(第三方设置，也可以通过InitParam setResApkPath(String path)来设置) <br>
 * 						大于 /data/data/com.txznet.txz/data/ResHolder.apk(默认资源)
 */
public class UIResLoader {

	private static UIResLoader sInstance = new UIResLoader();
	
	
	public static final String PACKAGE_SOURCE = "com.txznet.resholder"; //UI2.0存放资源文件的包名
	private String mResApkPath = null;
	public boolean isPriorRes = false;

	// *****************资源ID类型*******************//
	public static final String LAYOUT = "layout";
	public static final String ID = "id";
	public static final String DRAWABLE = "drawable";
	public static final String STYLE = "style";
	public static final String STRING = "string";
	public static final String COLOR = "color";
	public static final String DIMEN = "dimen";
	public static final String BOOL = "bool";
	public static final String ARRAY = "array";
	public static final String ANIMATION = "anim";
	
	private ResLoadedListener mLoadedListener;
	
	public interface ResLoadedListener{	
		public void onResLoaded();
		public void onException(String errorDsp);
	}

	/**
	 * 开始加载皮肤包dex及resource
	 * @param listener
	 * @param forceDefault
	 * 			是否强制使用默认的皮肤包
	 */
	public void startLoadResource(ResLoadedListener listener,boolean forceDefault){
		FilePathConstants.SKIN_FILE_RESOURCE_DEFAULT = GlobalContext.get().getApplicationInfo().dataDir+"/data/ResHolder.apk";
		mLoadedListener = listener;
		String strResourceFile = forceDefault ? FilePathConstants.SKIN_FILE_RESOURCE_DEFAULT : FilePathConstants.getSkinResourceFile();
		isPriorRes = FilePathConstants.SKIN_FILE_PRIOR_RESOURCE.equals(strResourceFile);
		Resources superRes = GlobalContext.get().getResources();
		loadResourceResources((Application) GlobalContext.get().getApplicationContext(), superRes, strResourceFile);
		if (mResourceResources != null) {
			ScreenUtils.addSceenSizeChangeListener(mScreenSizeChangeListener);
		}
	}
	
	private ScreenSizeChangeListener mScreenSizeChangeListener = new ScreenSizeChangeListener() {
		@Override
		public void onScreenSizeChange(int width, int height) {
			width = ScreenUtils.getAddedValue(width);
			height = ScreenUtils.getAddedValue(height);
			Configuration configuration = mResourceResources.getConfiguration();
			configuration.screenWidthDp = width;
			configuration.screenHeightDp = height;
			mResourceResources.updateConfiguration(configuration, mResourceResources.getDisplayMetrics());
		}
	};
	
	public boolean isUserResExist(){
		if (FilePathConstants.mSkinUserConfigPath != null) {
			File userConfigFile = new File(FilePathConstants.mSkinUserConfigPath);
			if (userConfigFile.exists()) {
				return true;
			}
			LogUtil.logw("resApkPath:" + FilePathConstants.mSkinUserConfigPath + " is set but file not exist!");
		}

		List<String> filePaths = FilePathConstants.getUserSkinPath();

		for (String filePath : filePaths) {
			File userFile = new File(filePath);
			if (userFile.exists()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPriorResExist(){
		File priorFile = new File(FilePathConstants.SKIN_FILE_PRIOR_RESOURCE);
		if(priorFile.exists()){
			return true;
		}
		return false;
	}
	
	/**
	 * 用户指定的ResApk路径
	 * @param path
	 */
	public void setUserConfigResApkPath(String path) {
		LogUtil.logd("setUserConfigResApkPath:" + path);
		FilePathConstants.mSkinUserConfigPath = path;
	}
	
	public void release(){
		if(mResourceResources!=null){
			mResourceResources.flushLayoutCache();
		}
	}
	
	public String getResApkPath(){
		return mResApkPath;
	}
	
	
	public Object getViewInstance(String viewClassName){
		return getClassInstance(viewClassName);
	}
	
	/**
	 *  得到资源包Class的Instance，通过getInstance得到单例 
	 */
	public Object getClassInstance(String className) {
		if (mResourceClassLoader == null) {
			LogUtil.loge("mResourceClassLoader null,failed getClassInstance");
			return null;
		}
		Object object = null;
		try {
			Class<?>  classView = mResourceClassLoader.loadClass(className);
			Method instanceMethod = classView.getDeclaredMethod("getInstance");
			object = instanceMethod.invoke("getInstance"); 
		} catch (Exception e) {
			LogUtil.loge("failed getClassInstance:" + className);
		}
		return object;
	}
	
	/**
	 * 得到资源包Config的Instance，通过new的方式得到
	 */
	public Object getConfigInstance(String configClassName){
		if (mResourceClassLoader == null) {
			LogUtil.loge("mResourceClassLoader null,failed getClassInstance");
			return null;
		}
		Object object = null;
		try {
			Class<?>  classView = mResourceClassLoader.loadClass(configClassName);
			object = classView.newInstance();
		} catch (Exception e) {
			LogUtil.loge("failed getConfigInstance:" + configClassName);
		}
		return object;
	}
	
	/**
	 * 根据类名加载对应的Class 
	 */
	public Class<?> getClass(String className) {
		if (mResourceClassLoader == null) {
			LogUtil.loge("mResourceClassLoader null,failed getClassInstance");
			return null;
		}
		Class<?> clazz = null;
		try {
			clazz = mResourceClassLoader.loadClass(className);
		} catch (Exception e) {
			LogUtil.loge("failed getClass:" + className);
		}
		return clazz;
	}
	
	public static UIResLoader getInstance(){
		return sInstance;
	}
	
	private UiClassLoader mResourceClassLoader = null;
	private Resources mResourceResources = null;
	
	private void loadResourceResources(Application app, Resources superRes,String resourceFile){
		LogUtil.logd("[UI2.0]start loadRes path:"+resourceFile);
		File sourceFile = new File(resourceFile);
		if (sourceFile.exists()) {
			String dexOutputDir = app.getApplicationInfo().dataDir+"/dex";
			File outFile = new File(dexOutputDir);
			if (!outFile.exists()) {
				outFile.mkdir();
			}
			mResourceClassLoader = new UiClassLoader(sourceFile.getAbsolutePath(),
					dexOutputDir, null, this.getClass().getClassLoader());
			try {
				AssetManager assetManager = AssetManager.class.newInstance();
				Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
				addAssetPath.invoke(assetManager, sourceFile.getAbsolutePath());
				mResourceResources = new Resources(assetManager, superRes.getDisplayMetrics(),
						superRes.getConfiguration());
				
				mResApkPath = resourceFile;
				if (mLoadedListener != null) {
					mLoadedListener.onResLoaded();
				}
			} catch (Exception e) {
				// 不起作用，暂时屏蔽
				// if (!TextUtils.isEmpty(FILE_RESOURCE_DEFAULT) &&
				// FILE_RESOURCE_DEFAULT.equals(resourceFile)) {
				// LogUtil.logw("load "+resourceFile +"failed,"+"start load
				// default source file");
				// loadResourceResources(app, superRes, FILE_RESOURCE_DEFAULT);
				// return;
				// }
				MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR_CORE_LOAD_DEX);
				LogUtil.loge("[UI2.0] load " + resourceFile+" dex error!");
				if (mLoadedListener != null) {
					mLoadedListener.onException("load resource dex error!");
				}
			}
		} else {
			// 不起作用，暂时屏蔽
			// if (!TextUtils.isEmpty(FILE_RESOURCE_DEFAULT) &&
			// !FILE_RESOURCE_DEFAULT.equals(resourceFile)) {
			// LogUtil.logw("load "+FILE_RESOURCE_DEFAULT +" failed,"+"start
			// load default source file");
			// loadResourceResources(app, superRes, FILE_RESOURCE_DEFAULT);
			// return;
			// }
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR_CORE_FILE_NOT_EXIST);
			LogUtil.loge("[UI2.0] " + resourceFile + "not exist!");
			if (mLoadedListener != null) {
				mLoadedListener.onException("UI2.0 resources file not exist!");
			}
		}
	}

	public XmlResourceParser getAnimation(String name) {
		int id = getResIdByName(name, ANIMATION);
		if (id != 0) {
			try {
				return mResourceResources.getAnimation(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, ANIMATION, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getAnimation(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getAnimation " + name);
		return null;
	}

	public XmlResourceParser getLayout(String name) {
		int id = getResIdByName(name, LAYOUT);
		if (id != 0) {
			try {
				return mResourceResources.getLayout(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, LAYOUT, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getLayout(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getLayout " + name);
		return null;
	}
	
	public Drawable getDrawable(String name){
		int id = getResIdByName(name, DRAWABLE);
		if (id != 0) {
			return mResourceResources.getDrawable(id);
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, DRAWABLE, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getDrawable(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getDrawable "+name);
		return null;
	}
	
	public String[] getStringArray(String name){
		int id = getResIdByName(name, ARRAY);
		if(id!=0){
			try {
				return mResourceResources.getStringArray(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, ARRAY, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getStringArray(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getStringArray "+name);
		return null;
	}
	
	public Boolean getBoolean(String name){
		int id = getResIdByName(name, BOOL);
		if(id!=0){
			try {
				return mResourceResources.getBoolean(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, BOOL, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getBoolean(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getBoolean "+name);
		return null;
	}
	
	public int getColor(String name) {
		int id = getResIdByName(name, COLOR);
		if (id != 0) {
			try {
				return mResourceResources.getColor(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, COLOR, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getColor(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getColor "+name);
		return 0;
	}
	
	public ColorStateList getColorStateList(String name){
		int id = getResIdByName(name, COLOR);
		if(id!=0){
			try {
				return mResourceResources.getColorStateList(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, COLOR, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getColorStateList(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getColorStateList "+name);
		return null;
	}
    public float getDimension(String name ){
	    return getDimension(name, false);
    }
	public float getDimension(String name ,boolean firstResHolder) {
		// 如果是x、y、m这种dimens文件，优先读取comm下的
		Pattern re = Pattern.compile("^[x,y,m]\\d+$");
		Matcher r = re.matcher(name);
		int id = 0;
		if (firstResHolder || !r.matches()) {
			id = getResIdByName(name, DIMEN);
			if (id != 0) {
				try {
					return mResourceResources.getDimension(id);
				} catch (NotFoundException e) {
				}
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, DIMEN, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getDimension(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getDimension "+name);
		return 0;
	}
	
	public String getString(String name){
		int id = getResIdByName(name, STRING);
		if (id != 0) {
			try {
				return mResourceResources.getString(id);
			} catch (NotFoundException e) {
			}
		}
		try {
			id = GlobalContext.get().getResources().getIdentifier(name, STRING, GlobalContext.get().getPackageName());
			if (id != 0) {
				return GlobalContext.get().getResources().getString(id);
			}
		} catch (Exception e) {
			LogUtil.loge("UIResLoader", e);
		}
		LogUtil.loge("failed getString "+name);
		return null;
	}
	
	public int getIdByName(String name, String type) {
		int id = getResIdByName(name, type);
		if (id != 0) {
			return id;
		}
		return GlobalContext.get().getResources().getIdentifier(name, type, GlobalContext.get().getPackageName());
	}

	public int getResIdByName(String name, String type) {
		if (mResourceResources != null) {
			try {
				return mResourceResources.getIdentifier(name, type, PACKAGE_SOURCE);
			} catch (Exception e) {
				LogUtil.loge("[UI2.0] get id failed:" + type + "," + name);
			}
		}
		LogUtil.loge("failed Id "+name);
		return 0;
	}

	private Context mModifyContext;
	public Context getModifyContext(){
		if (mModifyContext == null) {
			synchronized (UIResLoader.class) {
				if (mModifyContext == null) {
					mModifyContext = new UiContext(GlobalContext.get(), android.R.style.Theme, mResourceClassLoader, mResourceResources);
				}
			}
		}
		return mModifyContext;
	}
	
}
