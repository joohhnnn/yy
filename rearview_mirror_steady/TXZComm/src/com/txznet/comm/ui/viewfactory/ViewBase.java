package com.txznet.comm.ui.viewfactory;

import java.util.List;

import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;

import android.view.View;

public abstract class ViewBase {

	public Integer mFlags = null;
	/**
	 * 是否支持更新数据
	 */
	public static final int MASK_UPDATEABLE = 0x00000001;
	/**
	 * 支持更新数据（可以不重新生成View直接更新）
	 */
	public static final int UPDATEABLE = 0x00000001;
	/**
	 * 不支持更新数据
	 */
	public static final int NOT_UPDATEABLE = 0x00000000;
	/**
	 * 支持显示删除按钮
	 */
	public static final int SUPPORT_DELETE = 0x00000010;
	/**
	 * 标题支持城市显示
	 */
	public static final int SUPPORT_CITY_TITLE = 0x00000020;
	
	
	public abstract ViewAdapter getView(ViewData data);

	public abstract void init();

	public boolean onKeyEvent(int keyEvent) {
		return false;
	}

	public boolean supportKeyEvent() {
		return false;
	}

	/**
	 * 支持焦点的所有View
	 */
	public List<View> getFocusViews() {
		return null;
	}
	
	public void release(){
	}

	/**
	 * 更新View显示内容
	 * @param data
	 * @return
	 */
	public Object updateView(ViewData data) {
		return null;
	}

	/**
	 * View一些能力或其他的标注位
	 * @return
	 */
	public Integer getFlags() {
		return mFlags;
	}
	
}
