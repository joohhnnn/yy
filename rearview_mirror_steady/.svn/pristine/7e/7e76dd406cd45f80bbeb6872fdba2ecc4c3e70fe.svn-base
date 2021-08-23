package com.txznet.comm.resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;


import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TypedValue;
import dalvik.system.DexClassLoader;

@SuppressLint("NewApi")
public class TXZResources extends TXZResourceBase {
	
	final Object mAccessLock = new Object();
	
	public TXZResources(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
	}

	/**
	 * 当前app资源Id->皮肤apk资源Id
	 */
	SparseIntArray mMapDefaultColors = null;
	SparseIntArray mMapDefaultDrawable = null;
	SparseIntArray mMapDefaultDimens = null;
	SparseIntArray mMapDefaultBooleans = null;
	SparseIntArray mMapDefaultTexts = null;
	SparseIntArray mMapDefaultStrings = null;
	SparseIntArray mMapDefaultArrays = null;
	SparseIntArray mMapDefaultAnims = null;
	
	SparseIntArray mMapPriorColors = null;
	SparseIntArray mMapPriorDrawables = null;
	SparseIntArray mMapPriorDimens = null;
	SparseIntArray mMapPriorStrings = null;
	SparseIntArray mMapPriorBooleans = null;
	SparseIntArray mMapPriorTexts = null;
	SparseIntArray mMapPriorArrays = null;
	SparseIntArray mMapPriorAnims = null;
	
	
	@Override
	public void updateSkinConfig() {
		synchronized (mAccessLock) {
			if (mPriorSkinClassLoader != null && mPriorSkinResources != null
					&& !TextUtils.isEmpty(mPriorSkinPackageName)) {
				mMapPriorColors = loadPriorSkinId(COLOR);
				mMapPriorDimens = loadPriorSkinId(DIMEN);
				mMapPriorDrawables = loadPriorSkinId(DRAWABLE);
				mMapPriorStrings = loadPriorSkinId(STRING);
				mMapPriorArrays = loadPriorSkinId(ARRAY);
				mMapPriorAnims = loadPriorSkinId(ANIMATION);
				mMapPriorBooleans = loadPriorSkinId(BOOL);
			}
			if (mDefaultSkinClassLoader != null && mDefaultSkinResources != null
					&& !TextUtils.isEmpty(mDefaultSkinPackageName)
					/**
					 * 包名相同时会报Fatal SIGBUS的错
					 */
					&& !(mDefaultSkinPackageName == mPriorSkinPackageName)) {
				mMapDefaultDrawable = loadDefaultSkinId(DRAWABLE);
				mMapDefaultColors = loadDefaultSkinId(COLOR);
				mMapDefaultDimens = loadDefaultSkinId(DIMEN);
				mMapDefaultStrings = loadDefaultSkinId(STRING);
				mMapDefaultArrays = loadDefaultSkinId(ARRAY);
				mMapDefaultAnims = loadDefaultSkinId(ANIMATION);
				mMapDefaultBooleans = loadDefaultSkinId(BOOL);
			}
		}
	}


	private SparseIntArray loadDefaultSkinId(String type) {
		SparseIntArray sparseIntArray = null;
		try {
			Class clazz = mDefaultSkinClassLoader.loadClass(mDefaultSkinPackageName+".R$"+type);
			Field[] drawableFields = clazz.getDeclaredFields();
			sparseIntArray = new SparseIntArray();
			for(Field field:drawableFields){
				int curApkId = getIdentifier(field.getName(), type, mAppPackageName);
				if (curApkId != 0) {
					Integer skinApkId = (Integer) field.get(null);
					sparseIntArray.put(curApkId, skinApkId);
				}
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
		return sparseIntArray;
	}
	
	private SparseIntArray loadPriorSkinId(String type) {
		SparseIntArray sparseIntArray = null;
		try {
			Class clazz = mPriorSkinClassLoader.loadClass(mPriorSkinPackageName + ".R$" + type);
			Field[] drawableFields = clazz.getDeclaredFields();
			sparseIntArray = new SparseIntArray();
			for (Field field : drawableFields) {
				int curApkId = getIdentifier(field.getName(), type, mAppPackageName);
				if (curApkId != 0) {
					Integer skinApkId = (Integer) field.get(null);
					sparseIntArray.put(curApkId, skinApkId);
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return sparseIntArray;
	}

	@Override
	public float getDimension(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			Float dimen = null;
			if(mMapPriorDimens!=null&&mPriorSkinResources!=null){
				try {
					dimen = mPriorSkinResources.getDimension(mMapPriorDimens.get(id));
					if (dimen!=null) {
						return dimen;
					}
				} catch (Exception e) {
				}
			}
			if(mMapDefaultDimens!=null&&mDefaultSkinResources!=null){
				try {
					dimen = mDefaultSkinResources.getDimension(mMapDefaultDimens.get(id));
					if (dimen!=null) {
						return dimen;
					}
				} catch (Exception e) {
				}
			}
			return mSuperResources.getDimension(id);
		}
	}
	
	@Override
	public String[] getStringArray(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			try {
				if (mMapPriorArrays != null && mPriorSkinResources != null) {
					String[] aStrings = mPriorSkinResources.getStringArray(mMapPriorArrays.get(id));
					if (aStrings != null) {
						return aStrings;
					}
				}
				if (mMapDefaultArrays != null && mDefaultSkinResources != null) {
					String[] bStrings = mDefaultSkinResources.getStringArray(mMapDefaultArrays.get(id));
					if (bStrings != null) {
						return bStrings;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mSuperResources.getStringArray(id);
	}
	

	@Override
	public Drawable getDrawable(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			Drawable drawable = null;
			if (mMapPriorDrawables != null && mPriorSkinResources != null) {
				if (0 != mMapPriorDrawables.get(id)) {
					try {
						drawable = mPriorSkinResources.getDrawable(mMapPriorDrawables.get(id));
					} catch (Exception e) {
					}
				}
			}
			if (drawable != null) {
				return drawable;
			}
			if (mMapDefaultDrawable != null && mDefaultSkinResources != null) {
				if (0 != mMapDefaultDrawable.get(id)) {
					try {
						drawable = mDefaultSkinResources.getDrawable(mMapDefaultDrawable.get(id));
					} catch (Exception e) {
					}
				}
			}
			if (drawable != null) {
				return drawable;
			}
			return mSuperResources.getDrawable(id);
		} 
	}
	
	@Override
	public String getString(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			String string = null;
			if (mMapPriorStrings != null && mPriorSkinResources != null) {
				if (0 != mMapPriorStrings.get(id)) {
					try {
						string = mPriorSkinResources.getString(mMapPriorStrings.get(id));
					} catch (Exception e) {
					}
				}
			}
			if(string!=null){
				return string;
			}
			if (mMapDefaultStrings != null && mDefaultSkinResources != null) {
				if (0 != mMapDefaultStrings.get(id)) {
					try {
						string = mDefaultSkinResources.getString(mMapDefaultStrings.get(id));
					} catch (Exception e) {
					}
				}
			}
			if (string != null) {
				return string;
			}
		}
		return mSuperResources.getString(id);
	}
	
	
	
	@Override
	public int getColor(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			Integer color = null;
			if (mMapPriorColors != null && mPriorSkinResources != null) {
				if (0 != mMapPriorColors.get(id)) {
					try {
						color = mPriorSkinResources.getColor(mMapPriorColors.get(id));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
			if (color != null) {
				return color;
			}
			if (mMapDefaultColors != null && mDefaultSkinResources != null) {
				if (0 != mMapDefaultColors.get(id)) {
					try {
						color = mDefaultSkinResources.getColor(mMapDefaultColors.get(id));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
			if (color != null) {
				return color;
			}
			return mSuperResources.getColor(id);
		}
	}
	

	@Override
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			ColorStateList color = null;
			if (mMapPriorColors != null && mPriorSkinResources != null) {
				if (0 != mMapPriorColors.get(id)) {
					try {
						color = mPriorSkinResources.getColorStateList(mMapPriorColors.get(id));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
			if (color != null) {
				return color;
			}
			if (mMapDefaultColors != null && mDefaultSkinResources != null) {
				if (0 != mMapDefaultColors.get(id)) {
					try {
						color = mDefaultSkinResources.getColorStateList(mMapDefaultColors.get(id));
					} catch (Exception e) {
//						e.printStackTrace();
					}
				}
			}
			if (color != null) {
				return color;
			}
			return mSuperResources.getColorStateList(id);
		}
	}

	@Override
	public XmlResourceParser getAnimation(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			try {
				XmlResourceParser parser = null;
				if (mMapPriorAnims != null && mPriorSkinResources != null) {
					if (0 != mMapPriorAnims.get(id)) {
						parser = mPriorSkinResources.getAnimation(mMapPriorAnims.get(id));
					}
				}
				if (parser != null) {
					return parser;
				}
				if (mMapDefaultAnims != null && mDefaultSkinResources != null) {
					if (0 != mMapDefaultAnims.get(id)) {
						parser = mDefaultSkinResources.getAnimation(mMapDefaultAnims.get(id));
					}
				}
				if (parser != null) {
					return parser;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mSuperResources.getAnimation(id);
	}
	
	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		synchronized (mAccessLock) {
			try {
				Boolean boolean1 = null;
				if (mMapPriorBooleans != null && mPriorSkinResources != null) {
					if (0 != mMapPriorBooleans.get(id)) {
						boolean1 = mPriorSkinResources.getBoolean(mMapPriorBooleans.get(id));
					}
				}
				if (boolean1 != null) {
					return boolean1;
				}
				if (mMapDefaultBooleans != null && mDefaultSkinResources != null) {
					if (0 != mMapDefaultBooleans.get(id)) {
						boolean1 = mDefaultSkinResources.getBoolean(mMapDefaultBooleans.get(id));
					}
				}
				if (boolean1 != null) {
					return boolean1;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mSuperResources.getBoolean(id);
	}

	@Override
	public CharSequence getText(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getText");
		return mSuperResources.getText(id);
	}

	@Override
	public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getQuantityText");
		return mSuperResources.getQuantityText(id, quantity);
	}

	@Override
	public String getString(int id, Object... formatArgs) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getString");
		return mSuperResources.getString(id, formatArgs);
	}

	@Override
	public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getQuantityString");
		return mSuperResources.getQuantityString(id, quantity, formatArgs);
	}

	@Override
	public String getQuantityString(int id, int quantity) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getQuantityString");
		return mSuperResources.getQuantityString(id, quantity);
	}

	@Override
	public CharSequence getText(int id, CharSequence def) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getText");
		return mSuperResources.getText(id, def);
	}

	@Override
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getTextArray");
		return mSuperResources.getTextArray(id);
	}

	@Override
	public int[] getIntArray(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getIntArray");
		return mSuperResources.getIntArray(id);
	}

	@Override
	public TypedArray obtainTypedArray(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "obtainTypedArray");
		return mSuperResources.obtainTypedArray(id);
	}

	@Override
	public int getDimensionPixelOffset(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getDimensionPixelOffset");
		return mSuperResources.getDimensionPixelOffset(id);
	}

	@Override
	public int getDimensionPixelSize(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getDimensionPixelSize");
		return mSuperResources.getDimensionPixelSize(id);
	}

	@Override
	public float getFraction(int id, int base, int pbase) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getFraction");
		return mSuperResources.getFraction(id, base, pbase);
	}

	@SuppressLint("NewApi")
	@Override
	public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getDrawableForDensity");
		return mSuperResources.getDrawableForDensity(id, density);
	}

	@Override
	public Movie getMovie(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getMovie");
		return mSuperResources.getMovie(id);
	}


	@Override
	public int getInteger(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getInteger");
		return mSuperResources.getInteger(id);
	}

	@Override
	public XmlResourceParser getLayout(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getLayout");
		return mSuperResources.getLayout(id);
	}



	@Override
	public XmlResourceParser getXml(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getXml");
		return mSuperResources.getXml(id);
	}

	@Override
	public InputStream openRawResource(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "openRawResource");
		return mSuperResources.openRawResource(id);
	}

	@Override
	public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "openRawResource");
		return mSuperResources.openRawResource(id, value);
	}

	@Override
	public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "openRawResourceFd");
		return mSuperResources.openRawResourceFd(id);
	}

	@Override
	public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
		// TODO Auto-generated method stub
		mSuperResources.getValue(id, outValue, resolveRefs);
		//Log.i(TAG, "getValue");
	}

	@SuppressLint("NewApi")
	@Override
	public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs)
			throws NotFoundException {
		// TODO Auto-generated method stub
		mSuperResources.getValueForDensity(id, density, outValue, resolveRefs);
		//Log.i(TAG, "getValueForDensity");
	}

	@Override
	public void getValue(String name, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
		// TODO Auto-generated method stub
		mSuperResources.getValue(name, outValue, resolveRefs);
		//Log.i(TAG, "getValue");
	}

	@Override
	public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "obtainAttributes");
		return mSuperResources.obtainAttributes(set, attrs);
	}

	@Override
	public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "updateConfiguration111111");
		if(mSuperResources!=null){
			mSuperResources.updateConfiguration(config, metrics);
		}else {
			super.updateConfiguration(config, metrics);
		}
	}

	@Override
	public DisplayMetrics getDisplayMetrics() {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getDisplayMetrics");
		return mSuperResources.getDisplayMetrics();
	}

	@Override
	public Configuration getConfiguration() {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getConfiguration");
//		Log.d(TAG, Log.getStackTraceString(new Throwable()));
		return mSuperResources.getConfiguration();
	}

	@Override
	public int getIdentifier(String name, String defType, String defPackage) {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getIdentifier");
		
		return mSuperResources.getIdentifier(name, defType, defPackage);
	}

	@Override
	public String getResourceName(int resid) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getResourceName");
		return mSuperResources.getResourceName(resid);
	}

	@Override
	public String getResourcePackageName(int resid) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getResourcePackageName");
		return mSuperResources.getResourcePackageName(resid);
	}

	@Override
	public String getResourceTypeName(int resid) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getResourceTypeName");
		return mSuperResources.getResourceTypeName(resid);
	}

	@Override
	public String getResourceEntryName(int resid) throws NotFoundException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "getResourceEntryName");
		return mSuperResources.getResourceEntryName(resid);
	}

	@Override
	public void parseBundleExtras(XmlResourceParser parser, Bundle outBundle)
			throws XmlPullParserException, IOException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "parseBundleExtras");
		mSuperResources.parseBundleExtras(parser, outBundle);
	}

	@Override
	public void parseBundleExtra(String tagName, AttributeSet attrs, Bundle outBundle) throws XmlPullParserException {
		// TODO Auto-generated method stub
		//Log.i(TAG, "parseBundleExtra");
		mSuperResources.parseBundleExtra(tagName, attrs, outBundle);
	}
}
