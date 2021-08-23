package com.txznet.comm.resource;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import dalvik.system.DexClassLoader;

/**
 * Skin apk读取规则：
 *  默认从/system/txz/包名.skin.apk读取(方案公司差异化)，包名为  当前apk包名.skin(例如com.txznet.txz.skin)
 * 优先从/sdcard/txz/skin/包名.skin.apk读取（下发时使用），包名为 当前apk包名.skin.prior(例如com.txznet.skin.prior)
 */
public class ResLoaderImpl implements ResLoader {

	public static final String TAG = "ResLoaderImpl";

	public static final String PACKAGE_SUFFIX_DEFAULT = ".skin";
	public static final String PACKAGE_SUFFIX_PRIOR = ".skin.prior";
	
	public static final String FILE_SUFFIX_DEFAULT = ".skin.apk";
	public static final String FILE_SUFFIX_PRIOR = ".skin.prior.apk";

	public static final String PATH_DEFAULT = "system" + File.separator + "txz" + File.separator;
	public static final String PATH_PRIOR = Environment.getExternalStorageDirectory() + File.separator + "txz" + File.separator + "skin"
			+ File.separator;
	
	@Override
	public synchronized void reloadResources() {
		Application app = (Application) GlobalContext.get();
		if (app != null) {
			try {
				AssetManager assets = app.getAssets();
				Resources superRes = app.getResources();
				if (!(superRes instanceof TXZResources)) {// 避免superRes就是自身
					TXZResources.mSuperResources = superRes;
				}
				ScreenUtils.loadScreenConfig();
				int width = ScreenUtils.getScreenWidthDp();
				int height = ScreenUtils.getScreenHeightDp();
				Configuration configuration = superRes.getConfiguration();
				if (width > 0 && height > 0) {
					configuration.screenWidthDp = width;
					configuration.screenHeightDp = height;
				}
				TXZResources txzResources = new TXZResources(assets, superRes.getDisplayMetrics(), configuration);
				Field f = app.getClass().getDeclaredField("mResources");
				f.setAccessible(true);
				f.set(app, txzResources);
				txzResources.setSuperClassLoader(app.getClassLoader());
				String packageName = app.getPackageName();
				txzResources.setPackageName(packageName);
				txzResources.setDefaultPackageName(packageName + PACKAGE_SUFFIX_DEFAULT);
				txzResources.setPriorPackageName(packageName + PACKAGE_SUFFIX_PRIOR);

				loadPriorSkinApk(app, superRes);
				if (mPriorSkinClassLoader != null && mPriorSkinResources != null) {
					txzResources.setPriorSkinResources(mPriorSkinResources);
					txzResources.setPriorClassLoader(mPriorSkinClassLoader);
				}
				
				loadDefaultSkinApkResources(app, superRes);
				if (mDefaultSkinResources != null && mDefaultSkinClassLoader != null) {
					txzResources.setDefaultSkinResources(mDefaultSkinResources);
					txzResources.setDefaultClassLoader(mDefaultSkinClassLoader);
				}
				
				txzResources.updateSkinConfig();
			} catch (Exception e) {
				LogUtil.loge(TAG, e);
			}
		}
	}

	private DexClassLoader mDefaultSkinClassLoader = null;
	private Resources mDefaultSkinResources = null;
	
	private DexClassLoader mPriorSkinClassLoader = null;
	private Resources mPriorSkinResources = null;


	private void loadDefaultSkinApkResources(Application app, Resources superRes) {
		HashMap<String, String> path = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_PATH_SKIN_APK);
		File skinApkFile = null;
		if (path != null && path.get(TXZFileConfigUtil.KEY_PATH_SKIN_APK) != null) {
			LogUtil.logd("ResLoad start load :" + path.get(TXZFileConfigUtil.KEY_PATH_SKIN_APK));
			skinApkFile = new File(path.get(TXZFileConfigUtil.KEY_PATH_SKIN_APK));
		}
		if (skinApkFile == null || !skinApkFile.exists()) {
			String fileName = app.getPackageName() + FILE_SUFFIX_DEFAULT;
			LogUtil.logd("ResLoad start load :" + PATH_DEFAULT + fileName);
			skinApkFile = new File(PATH_DEFAULT + fileName);
		}
		if (skinApkFile.exists()) {
			String dexOutputDir = app.getApplicationInfo().dataDir+"/dex";
			mDefaultSkinClassLoader = new DexClassLoader(skinApkFile.getAbsolutePath(),
					dexOutputDir, null, this.getClass().getClassLoader());
			try {
				AssetManager assetManager = AssetManager.class.newInstance();
				Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
				addAssetPath.invoke(assetManager, skinApkFile.getAbsolutePath());
				mDefaultSkinResources = new Resources(assetManager, superRes.getDisplayMetrics(),
						superRes.getConfiguration());
			} catch (Exception e) {
				LogUtil.logw("reload default skin resources failed!");
			}
		} else {
			LogUtil.logw("default skin file not exist!");
		}
	}

	
	
	private void loadPriorSkinApk(Application app,Resources superRes){
		String fileName = app.getPackageName() + FILE_SUFFIX_PRIOR;
		File skinApkFile = new File(PATH_PRIOR + fileName);
		if (skinApkFile.exists()) {
			String dexOutputDir = app.getApplicationInfo().dataDir+"/dex";
			mPriorSkinClassLoader = new DexClassLoader(skinApkFile.getAbsolutePath(),
					dexOutputDir, null, this.getClass().getClassLoader());
			try {
				AssetManager assetManager = AssetManager.class.newInstance();
				Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
				addAssetPath.invoke(assetManager, skinApkFile.getAbsolutePath());
				mPriorSkinResources = new Resources(assetManager, superRes.getDisplayMetrics(),
						superRes.getConfiguration());
			} catch (Exception e) {
				LogUtil.logw("reload prior skin resources failed!");
			}
		} else {
			LogUtil.logw("prior skin file not exist!");
		}
	}

	
	
	
	// /**
	// * 之前从文件读取时用到，更改为从apk读取后弃用
	// */
	// HashMap<String, Integer> colors = new HashMap<String,Integer>();
	// HashMap<String, Drawable> drawables = new HashMap<String,Drawable>();
	// HashMap<String, Float> dimens = new HashMap<String,Float>();
	//
	//
	// @Override
	// public synchronized void loadResConfig(Application app) {
	// ResConfigLoader.getInstance().loadConfig(app);
	// colors = ResConfigLoader.getInstance().getColors();
	// drawables = ResConfigLoader.getInstance().getDrawables();
	// try {
	// TXZResourceBase txzResourceBase = (TXZResourceBase) app.getResources();
	// txzResourceBase.updateColors(colors,app);
	// txzResourceBase.updateDrawables(drawables,app);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
}
