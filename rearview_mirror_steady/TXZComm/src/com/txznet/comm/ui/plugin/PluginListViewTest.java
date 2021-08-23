package com.txznet.comm.ui.plugin;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.ConfigUtil;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PluginListViewTest {

	public static class PluginListViewTitleTest extends PluginListViewTitle {

		@Override
		public OnClickListener getOnTitleClickListener() {
			return new OnClickListener() {

				@Override
				public void onClick(View v) {
					LogUtil.logd(TAG + " OnTitleClickListener");
				}
			};
		}


		@Override
		public Drawable getBackageDrawable() {
			return new ColorDrawable(Color.GRAY);
		}


		@Override
		public OnClickListener getOnPrePageClickListener() {
			return null;
		}


		@Override
		public OnClickListener getOnNextPageClickListener() {
			return null;
		}

	}

	public static void test() {
		RecordWin2Manager.getInstance().show();
		PluginListView<String> pluginListView = new PluginListView<String>();
		pluginListView.setItemImpl(new PluginListViewItemTest2());
		pluginListView.setTitleImpl(new PluginListViewTitleTest());
		PluginListViewData<String> testData = new PluginListViewData<String>();
		testData.curPage = 0;
		testData.maxPage = 3;
		testData.prefixTitle = "为您找到";
		testData.title = "世界之窗";
		testData.suffixTitle = "如下结果";
		List<String> items = new ArrayList<String>();
		for (int i = 0; i < 4; i++) {
			String item = new String("某一个子项" + i);
			items.add(item);
		}
		testData.items = items;
		View view = pluginListView.generateView(testData);
		RecordWin2Manager.getInstance().addView(20, view);

	}

	private static final String TAG = PluginListViewItemText.class.getSimpleName();

	public static class PluginListViewItemTest extends PluginListViewItemText {

		@Override
		public OnClickListener getOnItemClickListener() {
			return new OnClickListener() {

				@Override
				public void onClick(View v) {
					LogUtil.logd(TAG + " OnItemClickListener " + v.getTag());
				}
			};
		}

		@Override
		public View createItemView(int position, String itemData, boolean showDivider) {
			return super.createItemView(position, itemData, showDivider);
		}
	}
	
	
	public static class PluginListViewItemTest2 extends PluginCommonListViewItem<String>{

		@Override
		public View createContentItemView(int position, String itemData) {
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			
			TextView textView = new TextView(GlobalContext.get());
			textView.setText(itemData);
			linearLayout.addView(textView);
			
			TextView textView1 = new TextView(GlobalContext.get());
			textView1.setText(itemData);
			linearLayout.addView(textView1);
			return linearLayout;
		}

		@Override
		public OnClickListener getOnItemClickListener() {
			return null;
		}
		
	}

}
