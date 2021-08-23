package com.txznet.music.ui.net.request;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.fm.bean.Configuration;
import com.txznet.music.utils.QQMusicUtil;

import java.util.ArrayList;
import java.util.List;

public class ReqCheck {
	private long logoTag; // tag, 客户端tag
	private List<Integer> arrApp = new ArrayList<Integer>(); // 安装的音乐源列表,默认所有的。
	private int version; // 版本
	private int width;//当前屏幕的宽
	private int height;//当前屏幕的高

	public ReqCheck() {
		WindowManager wm = (WindowManager) GlobalContext.get().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		version = Configuration.getInstance().getInteger(Configuration.TXZ_Search_VERSION);
	}

	public long getLogoTag() {
		return logoTag;
	}

	public void setLogoTag(long logoTag) {
		this.logoTag = logoTag;
	}

	public List<Integer> getArrApp() {
		if (QQMusicUtil.checkQQMusicInstalled()) {
			arrApp.add(1);
		}
		return arrApp;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
