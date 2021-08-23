package com.txznet.music.bean;

import android.view.View.OnClickListener;

import com.txznet.music.adpter.MyCategoryAdapter.ItemClickListener;

/**
 * 首页的GridView的对象
 * 
 * @author telenewbie
 *
 */
public class Menu {
	private int drawableId;
	private String name;

	public OnClickListener listener;

	public Menu(int drawableId, OnClickListener listener) {
		super();
		this.drawableId = drawableId;
		this.listener = listener;
	}

	public Menu(int drawableId, String name, OnClickListener listener) {
		super();
		this.drawableId = drawableId;
		this.name = name;
		this.listener = listener;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int drawableId) {
		this.drawableId = drawableId;
	}

	public OnClickListener getListener() {
		return listener;
	}

	public void setListener(OnClickListener listener) {
		this.listener = listener;
	}

}
