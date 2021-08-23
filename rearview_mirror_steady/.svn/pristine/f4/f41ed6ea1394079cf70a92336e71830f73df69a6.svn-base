package com.txznet.comm.ui.plugin;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PluginListView<T> {

	PluginListViewItem mPluginItemImpl;
	PluginListViewTitle mPluginTitleImpl;
	OnClickListener mOnClickListener;

	private static final String TAG = PluginListView.class.getSimpleName();
	
	public View generateView(PluginListViewData<T> data) {
		if (mPluginItemImpl == null || mPluginTitleImpl == null) {
			LogUtil.loge(TAG + "View Impl not set");
			return null;
		}
		if (data == null || data.items.size() == 0) {
			LogUtil.loge(TAG + "data is null");
			return null;
		}
		View titleView = mPluginTitleImpl.createTitleView(data);
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				mPluginTitleImpl.getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleView, layoutParams);
		
		int itemHeight = 0;
		int itemCount = 0;
		if (ConfigUtil.getDisplayLvItemH(false) != 0) {
			itemHeight = ConfigUtil.getDisplayLvItemH(false);
			itemCount = ConfigUtil.getVisbileCount();
		} else {
			itemHeight = ScreenUtil.getDisplayLvItemH(false);
			itemCount = ScreenUtil.getVisbileCount();
		}

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight * itemCount);
		llLayout.addView(llContent,layoutParams);
		llContent.setLayoutAnimation(getLayoutAnimation());
		int count = data.items.size();
		for (int i = 0; i < count; i++) {
			View itemView = mPluginItemImpl.createItemView(i, data.items.get(i), i != itemCount - 1);
			// TextView itemView = new TextView(GlobalContext.get());
			// itemView.setText("测试测试" + i);
			// itemView.setBackgroundColor(Color.RED);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			llContent.addView(itemView, layoutParams);
		}
		return llLayout;
	}

	public LayoutAnimationController getLayoutAnimation(){
		return ListViewItemAnim.getAnimationController();
	}
	
	public void setTitleImpl(PluginListViewTitle pluginListViewTitle) {
		this.mPluginTitleImpl = pluginListViewTitle;
	}

	public void setItemImpl(PluginListViewItem pluginListViewItem) {
		this.mPluginItemImpl = pluginListViewItem;
	}
	
}
