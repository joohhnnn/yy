package com.txznet.comm.resource;

import java.util.HashMap;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import dalvik.system.DexClassLoader;

/**
 * 更新资源文件
 * 
 * @author TerryYang
 *
 */
public abstract class TXZResourceBase extends Resources {
	
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
	
	
	
	//当前apk初始资源
	public ClassLoader mSuperClassLoader = null;
	public static Resources mSuperResources = null;
	protected String mAppPackageName = null;
	//默认皮肤替换资源
	protected DexClassLoader mDefaultSkinClassLoader = null;
	protected Resources mDefaultSkinResources = null;
	protected String mDefaultSkinPackageName = null;
	//优先皮肤替换资源
	protected DexClassLoader mPriorSkinClassLoader = null;
	protected Resources mPriorSkinResources = null;
	protected String mPriorSkinPackageName = null;
	
	public TXZResourceBase(AssetManager assets,
			DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
	}
	
	public void setSuperClassLoader(ClassLoader superClassLoader){
		this.mSuperClassLoader = superClassLoader;
	}
	
	public void setDefaultClassLoader(DexClassLoader skinClassLoader){
		this.mDefaultSkinClassLoader = skinClassLoader;
	}
	
	public void setPriorClassLoader(DexClassLoader skinClassLoader){
		this.mPriorSkinClassLoader = skinClassLoader;
	}
	
	
	public void setSuperResources(Resources superResources){
		TXZResourceBase.mSuperResources = superResources;
	}
	
	public void setDefaultSkinResources(Resources skinResources){
		this.mDefaultSkinResources = skinResources;
	}
	
	public void setPriorSkinResources(Resources priorResources){
		this.mPriorSkinResources = priorResources;
	}
	
	/**
	 * 设置packageName
	 */
	public void setPackageName(String packageName){
		this.mAppPackageName = packageName;
	}
	
	public void setDefaultPackageName(String packageName){
		this.mDefaultSkinPackageName = packageName;
	}
	
	public void setPriorPackageName(String packageName){
		this.mPriorSkinPackageName = packageName;
	}
	
	
	public abstract void updateSkinConfig();
	
	
	/**
	 * 更改为从apk读取后弃用
	 */
	// public abstract void updateColors(HashMap<String, Integer>
	// colors,Application application);
	//
	// public abstract void updateDrawables(HashMap<String, Drawable>
	// drawables,Application application);
	//
	// public abstract void updateDimens(HashMap<String, Float>
	// dimens,Application application);
	
}
