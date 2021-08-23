package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.StyleListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IStyleListView;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 说明：新手/半屏/熟手模式风格列表
 *
 * @author xiaolin
 * create at 2020-09-09 15:53
 */
@SuppressLint("NewApi")
public class StyleListView extends IStyleListView {

	private static StyleListView sInstance = new StyleListView();

	private List<View> mItemViews;

	private StyleListView() {
	}

	public static StyleListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {

	}

	@Override
	public ViewAdapter getView(ViewData data) {
		StyleListViewData styleListViewData = (StyleListViewData) data;
		WinLayout.getInstance().vTips = styleListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "styleListViewData.vTips: "+styleListViewData.vTips);

		View view = createViewNone(styleListViewData);

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = StyleListView.getInstance();
		return viewAdapter;
	}

	private View createViewNone(StyleListViewData viewData){
		Context context = UIResLoader.getInstance().getModifyContext();
		int maxPage = viewData.mTitleInfo.maxPage;
		int curPage = viewData.mTitleInfo.curPage;

		ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
		ViewGroup view = listContainer.rootView;
		ViewGroup container = listContainer.container;

		ArrayList<StyleListViewData.StyleBean> dataAry = viewData.getData();

		mItemViews = new ArrayList<>();
		for (int i = 0; i < viewData.count; i++) {
			View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
			container.addView(itemView);
			mItemViews.add(itemView);
		}

		// 添加空视图填充空间
		int re = SizeConfig.pageCount - viewData.count;
		for (int i = 0; i < re; i++) {
			View itemView = createItemView(context, i, null, false);
			container.addView(itemView);
		}
		return view;
	}

	private View createItemView(Context context, int pos, StyleListViewData.StyleBean row, boolean showDivider){
		View view = LayoutInflater.from(context).inflate(R.layout.style_list_item, (ViewGroup)null);
		if(row == null){
			view.setVisibility(View.INVISIBLE);
			return view;
		}

		TextView tvTitle = view.findViewById(R.id.tvTitle);
		tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", pos+1, row.name));
		View divider = view.findViewById(R.id.divider);

		// 分隔线
		if (!showDivider) {
			divider.setVisibility(View.GONE);
		}

		// 设置列表点击
		ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, pos);

		return view;
	}

	@Override
	public void init() {
		super.init();

	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){

	}

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	
	@Override
	public boolean supportKeyEvent() {
		return true;
	}
	
	
	/**
	 * 是否含有动画
	 * @return
	 */
	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}

	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "train updateItemSelect " + index);
		showSelectItem(index);
	}

	private void showSelectItem(int index){
		for (int i = 0;i< mItemViews.size();i++){
			if (i == index){
				mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
			}else {
				mItemViews.get(i).setBackground(null);
			}
		}
	}
	
	
}
