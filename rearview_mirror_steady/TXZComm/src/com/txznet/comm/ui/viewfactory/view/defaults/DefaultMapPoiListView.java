package com.txznet.comm.ui.viewfactory.view.defaults;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.MapPoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IMapPoiListView;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DefaultMapPoiListView extends IMapPoiListView {

	public static int MAP_ACTION_LOADING = 1;
	public static int MAP_ACTION_ENLARGE= 2;
	public static int MAP_ACTION_NARROW = 3;
	public static int MAP_ACTION_MAP= 4;
	public static int MAP_ACTION_LIST = 5;
	
	private static DefaultMapPoiListView sInstance = new DefaultMapPoiListView();

	private DefaultMapPoiListView() {
		mFlags = Integer.valueOf(0);
		// 表明当前View支持更新
		mFlags = mFlags | UPDATEABLE;
	}

	public static DefaultMapPoiListView getInstance() {
		return sInstance;
	}
	 
	public static interface MapPoiConTrol{
		public ViewAdapter getView(MapPoiListViewData data);
		public void updata(ViewData data);
		public void dismiss();
		public void setNumberOnClickListener(TextClickListener listener);
		public void checkPoiDeal(int index);
	}
	
	private MapPoiConTrol mMapPoiControl = null;
	public void setMapPoiContril(MapPoiConTrol control){
		mMapPoiControl = control;
	}
	
	private ViewBase mMapPoiView;
	@Override
	public ViewAdapter getView(ViewData data) {
		ViewAdapter adapter = new ViewAdapter();
		adapter.flags = Integer.valueOf(0);
		adapter.object = getInstance();
		adapter.type = ViewData.TYPE_FULL_LIST_MAPPOI;
		adapter.view = createView(data); 
		return adapter;
	}
	LinearLayout mPoiListLayout = null; 
	LinearLayout.LayoutParams mPoilayoutParams  = null;
	LinearLayout llLayoutLoading  = null;
	ImageView mLoadingImg = null;
	private View createView(ViewData data) {
		MapPoiListViewData mapPoiViewData = (MapPoiListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
		mCurPage = mapPoiViewData.mTitleInfo.curPage;
		mMaxPage = mapPoiViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		int contentHeight = ScreenUtil.mListViewRectHeight;
		RelativeLayout rlLayout = new RelativeLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		llLayout.addView(rlLayout, layoutParams);
		
		ViewAdapter mapViewAdapter = mMapPoiControl.getView(mapPoiViewData);
		RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);	
		rlLayout.addView(mapViewAdapter.view, rlLayoutParams);
		
		boolean isVertical = ScreenUtil.isVerticalDevice();
		
		if(isVertical){
			mPoiListLayout = new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.VERTICAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);		
			rlLayout.addView(mPoiListLayout, layoutParams);
			
			ViewAdapter listViewAdapter = PoiListView.getInstance().getView(data);
			mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
			
			View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
			mPoiListLayout.addView(view, mPoilayoutParams);		
		}else{		
			mPoiListLayout= new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.HORIZONTAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);		
			rlLayout.addView(mPoiListLayout, layoutParams);
			
			ViewAdapter listViewAdapter = PoiListView.getInstance().getView(data);
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
			
			View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
			mPoiListLayout.addView(view, mPoilayoutParams);	
		}
		
		llLayoutLoading = new LinearLayout(GlobalContext.get());
		llLayoutLoading.setGravity(Gravity.CENTER);
		llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
		llLayoutLoading.setVisibility(View.GONE);
		llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
		rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);	
		rlLayout.addView(llLayoutLoading, layoutParams);
		
		
		mLoadingImg = new ImageView(GlobalContext.get());
		mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llLayoutLoading.addView(mLoadingImg, layoutParams);
		PoiListView.getInstance().setNumberOnClickListener(new  TextClickListener() {
			
			@Override
			public void onClick(int index) {
				mMapPoiControl.checkPoiDeal(index);
			}
		});
		mMapPoiControl.setNumberOnClickListener( new TextClickListener() {
			
			@Override
			public void onClick(int index) {
				PoiListView.getInstance().checkPoiDeal(index);			
			}
		});
		return llLayout;
	}
	public interface TextClickListener{
		public void onClick(int index);
	}
	@Override
	public void init() {
		super.init();
		PoiListView.getInstance().init();
		ListTitleView.getInstance().init();
	}
	
	@Override
	public void release() {
		mMapPoiControl.dismiss();
		ListTitleView.getInstance().dismiss();
		mPoiListLayout = null;
		mPoilayoutParams = null;
		mLoadingImg= null;
		llLayoutLoading = null;
		super.release();
	}

	@Override
	public Object updateView(ViewData data) {
		
		llLayoutLoading.setVisibility(View.GONE);
		MapPoiListViewData viewData =(MapPoiListViewData) data;
		Log.d("zsbin","updateView viewData.mMapAction="+viewData.mMapAction);
		if(viewData.mMapAction != null && viewData.mMapAction == MAP_ACTION_LOADING){
			llLayoutLoading.setVisibility(View.VISIBLE);
			((AnimationDrawable) mLoadingImg.getDrawable()).start();
		}
		mMapPoiControl.updata(data);

		if(viewData.mMapAction == null){
			ListTitleView.getInstance().updata(data);
			mPoiListLayout.removeAllViewsInLayout();
			ViewAdapter listViewAdapter = PoiListView.getInstance().getView(data);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);		
			View view = new View(GlobalContext.get());
			mPoiListLayout.addView(view, mPoilayoutParams);	
		}
		
		
		
		return super.updateView(data);
	}

	@Override
	public Integer getFlags() {
		return super.getFlags();
	}

	@Override
	public void updateProgress(int progress, int selection) {
		
	}

	@Override
	public void snapPage(boolean next) {
		
	}

	@Override
	public void updateItemSelect(int selection) {

	}

}
