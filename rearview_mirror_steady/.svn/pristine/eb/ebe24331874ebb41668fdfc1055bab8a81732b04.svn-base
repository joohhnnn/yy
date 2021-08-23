package com.txznet.comm.ui.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.resource.TXZResourceBase;
import com.txznet.comm.ui.resloader.UIResLoader;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

public class LayouUtil {

	private static UIResLoader resLoader = UIResLoader.getInstance();

	
	public static void release(){
		resLoader.release();
	}
	
	/**
	 * 根据layoutName从第三方包中加载layout
	 */
	public static XmlResourceParser getLayout(String layoutName) {
		return resLoader.getLayout(layoutName);
	}

	public static View getView(String layoutName) {
		XmlResourceParser parser = getLayout(layoutName);
		if (parser != null) {
			return LayoutInflater.from(GlobalContext.get()).inflate(parser, null);
		}
		LogUtil.loge(layoutName + " not exist!");
		return null;
	}

	public static View getModifyView(String layoutName){
		XmlResourceParser parser = getLayout(layoutName);
		if (parser != null) {
			try {
				return LayoutInflater.from(getModifyContext()).inflate(parser, null);
			}catch (Exception e) {
				return LayoutInflater.from(GlobalContext.get()).inflate(parser, null);
			}
		}
		LogUtil.loge(layoutName + " not exist!");
		return null;
	}

	public static Context getModifyContext(){
		return resLoader.getModifyContext();
	}

	public static Object findViewByName(String name, View view) {
		try {
			View target = null;
			int id = resLoader.getIdByName(name, TXZResourceBase.ID);
			target =  view.findViewById(id);
			if(target!=null){
				return target;
			}
			id = GlobalContext.get().getResources().getIdentifier(name, TXZResourceBase.ID, GlobalContext.get().getPackageName());
			target = view.findViewById(id);
			return target;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 类似于resources.getAnimation(int id)
	 */
	public static XmlResourceParser getAnimation(String name) {
		try {
			return resLoader.getAnimation(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 类似于resources.getDrawable(int id)
	 */
	public static Drawable getDrawable(String name) {
		try {
			return resLoader.getDrawable(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 类似于resources.getColor(int id)
	 */
	public static float getColor(String name) {
		try {
			return resLoader.getColor(name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 类似于resources.getDimension(int id)
	 */
	public static float getDimen(String name) {
		try {
			return resLoader.getDimension(name ,ConfigUtil.isPriorityResHolderRes());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @param name
	 * @param firstResHolder 优先使用皮肤包中的dimen，没有时再使用core中的
	 * @return
	 */
	public static float getDimen(String name, boolean firstResHolder) {
		try {
			return resLoader.getDimension(name, firstResHolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static Boolean getBoolean(String name) {
		return resLoader.getBoolean(name);
	}

	public static ColorStateList getColorStateList(String name) {
		return resLoader.getColorStateList(name);
	}

	public static String[] getStringArray(String name) {
		return resLoader.getStringArray(name);
	}

	public static String getString(String name) {
		return resLoader.getString(name);
	}

}
