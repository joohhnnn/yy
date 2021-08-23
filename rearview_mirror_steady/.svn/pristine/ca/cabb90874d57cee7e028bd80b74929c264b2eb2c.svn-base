package com.txznet.comm.ui.plugin;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.loader.AppLogicBase;

import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

/**
 * 用来显示推送的插件通知的View，同种type的View只能存在一个， 大小及样式都是传递过来的参数指定的
 * 用法：可以直接调用，也可以发pluginCmd调用，默认不混淆 note:使用时通过静态方法调用，不要getInstance()后调用
 * 
 * @author Terry
 */
public class WinPlugin extends WinDialog {

	// private static WinPlugin sInstance = new WinPlugin();
	// public static WinPlugin getInstance() {
	// return sInstance;
	// }
	private int mType = 0;
	private View mDefaultView;

	protected WinPlugin(int type) {
		super();
		this.mType = type;
	}
	
	protected void updateContentView(View view, LayoutParams layoutParams) {
		if (view == null) {
			view = mDefaultView;
		}
		if (layoutParams == null) {
			layoutParams = getWindow().getAttributes();
		}
		getWindow().setContentView(view, layoutParams);
	}
	
	@Override
	protected View createView() {
		TextView textView = new TextView(getContext());
		textView.setText("默认View");
		mDefaultView = textView;
		return mDefaultView;
	}


	protected int getType() {
		return mType;
	}

	// ///////////////////////对外提供的静态方法///////////////////
	
	
	private static SparseArray<WinPlugin> mMapPlugin = new SparseArray<WinPlugin>();
	
	public static void showPluginView(View view, LayoutParams layoutParams) {
		showPluginView(view, layoutParams, 0);
	}

	/**
	 * 固定入口，预留参数，避免对外接口的不必要改动
	 * 
	 * @param view
	 */
	public static void showPluginView(final View view, final LayoutParams layoutParams, final int type, Object... objects) {
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				WinPlugin winPlugin = mMapPlugin.get(type);
				if (winPlugin == null) {
					winPlugin = new WinPlugin(type);
					mMapPlugin.put(type, winPlugin);
				}
				winPlugin.updateContentView(view, layoutParams);
				winPlugin.show();
			}
		}, 0);
	}

	/**
	 * 固定入口，之后不做改动
	 */
	public static void showWin(final int type) {
		if (mMapPlugin.get(type) == null) {
			LogUtil.loge("plugin win not exist,type:" + type);
			return;
		}
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mMapPlugin.get(type).show();
			}
		}, 0);
	}

	/**
	 * 固定入口，之后不做改动
	 */
	public static void dismissWin(final int type) {
		if (mMapPlugin.get(type) == null) {
			LogUtil.loge("plugin win not exist,type:" + type);
			return;
		}
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mMapPlugin.get(type).dismiss();
			}
		}, 0);
	}

	/**
	 * 固定入口，之后不做改动
	 */
	public static void updateViewData(int type, Object... objects) {
		// 预留接口，同类通知过多时只更新显示数据
		if (mMapPlugin.get(type) == null) {
			LogUtil.loge("plugin win not exist,type:" + type);
			return;
		}

	}
}
