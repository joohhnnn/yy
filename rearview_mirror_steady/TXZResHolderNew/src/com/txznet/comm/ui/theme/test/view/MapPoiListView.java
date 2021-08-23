package com.txznet.comm.ui.theme.test.view;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.mappoi.MapPoiPoiList;
import com.txznet.comm.ui.theme.test.mappoi.MapPoiView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutFull;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutHalf;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutNone;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.MapPoiListViewData;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IMapPoiListView;
import com.txznet.sdk.bean.Poi;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

public class MapPoiListView extends IMapPoiListView {

	public static int MAP_ACTION_LOADING = 1;
	public static int MAP_ACTION_ENLARGE= 2;
	public static int MAP_ACTION_NARROW = 3;
	public static int MAP_ACTION_MAP= 4;
	public static int MAP_ACTION_LIST = 5;
	
	private static MapPoiListView sInstance = new MapPoiListView();

	private MapPoiListView() {
		mFlags = Integer.valueOf(0);
		// 表明当前View支持更新
		mFlags = mFlags | UPDATEABLE;
	}

	public static MapPoiListView getInstance() {
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

        MapPoiListViewData mapPoiViewData = (MapPoiListViewData) data;
		LogUtil.logd(WinLayout.logTag+ "mapPoiViewData: isBus:"+ mapPoiViewData.isBus+"--mapPoiViewData" + mapPoiViewData.count);

		View view = null;
		ListTitleView.getInstance().isBusinessTitle = mapPoiViewData.isBus;

		if (llPager.getParent() != null){
			ViewGroup viewGroup = (ViewGroup)llPager.getParent();
			viewGroup.removeAllViews();
		}

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(data);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(data);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(data);
				break;
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.flags = Integer.valueOf(0);
		adapter.object = getInstance();
		adapter.type = ViewData.TYPE_FULL_LIST_MAPPOI;
		adapter.view = view;
		adapter.isListView = true;
		adapter.view.setTag(data.getType());
		return adapter;
	}

	LinearLayout mPoiListLayout = null;
	//LinearLayout.LayoutParams mPoilayoutParams  = null;
	//LinearLayout.LayoutParams mPoilayoutParamsList  = null;
	LinearLayout llLayoutLoading  = null;
	ImageView mLoadingImg = null;

	PageView llPager = new PageView(GlobalContext.get());

	private View createViewFull(ViewData data) {
		MapPoiListViewData mapPoiViewData = (MapPoiListViewData) data;
		WinLayout.getInstance().vTips = mapPoiViewData.vTips;
		//ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
		//ListTitleView.getInstance().setIvIcon("nav");
		ViewAdapter titleViewAdapter;
		if (Poi.PoiAction.ACTION_NAV_HISTORY.equals(mapPoiViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}

		mCurPage = mapPoiViewData.mTitleInfo.curPage;
		mMaxPage = mapPoiViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents, layoutParams);

		RelativeLayout rlLayout = new RelativeLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(rlLayout, layoutParams);

		llPager.updatePage(mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);
		
		ViewAdapter mapViewAdapter = mMapPoiControl.getView(mapPoiViewData);
		RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);	
		rlLayout.addView(mapViewAdapter.view, rlLayoutParams);
		
		boolean isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;
		
		//if(isVertical){
			mPoiListLayout = new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.VERTICAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);		
			rlLayout.addView(mPoiListLayout, rlLayoutParams);

		View view = new View(GlobalContext.get());
		LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
		mPoiListLayout.addView(view, mPoilayoutParams);
			
		ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
		//mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
		mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
		/*}else{
			mPoiListLayout= new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.HORIZONTAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);		
			rlLayout.addView(mPoiListLayout, rlLayoutParams);
			
			ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
			
			View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
			mPoiListLayout.addView(view, mPoilayoutParams);	
		}*/

		llLayoutLoading = new LinearLayout(GlobalContext.get());
		llLayoutLoading.setGravity(Gravity.CENTER);
		llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
		llLayoutLoading.setVisibility(View.GONE);
		llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
		rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);	
		rlLayout.addView(llLayoutLoading, rlLayoutParams);
		
		
		mLoadingImg = new ImageView(GlobalContext.get());
		mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llLayoutLoading.addView(mLoadingImg, layoutParams);
		MapPoiPoiList.getInstance().setNumberOnClickListener(new  TextClickListener() {
			
			@Override
			public void onClick(int index) {
				mMapPoiControl.checkPoiDeal(index);
			}
		});
		mMapPoiControl.setNumberOnClickListener( new TextClickListener() {
			@Override
			public void onClick(int index) {
				MapPoiPoiList.getInstance().checkPoiDeal(index);	
			}
		});
		return llLayout;
	}

	private View createViewHalf(ViewData data) {
		MapPoiListViewData mapPoiViewData = (MapPoiListViewData) data;
		WinLayout.getInstance().vTips = mapPoiViewData.vTips;
		//ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
		//ListTitleView.getInstance().setIvIcon("nav");
		ViewAdapter titleViewAdapter;
		if (Poi.PoiAction.ACTION_NAV_HISTORY.equals(mapPoiViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}

		mCurPage = mapPoiViewData.mTitleInfo.curPage;
		mMaxPage = mapPoiViewData.mTitleInfo.maxPage;

		LogUtil.logd(WinLayout.logTag+ "mapPoiViewData: mCurPage:"+ mCurPage + "--mMaxPage:" + mMaxPage);

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents, layoutParams);

		RelativeLayout rlLayout = new RelativeLayout(GlobalContext.get());
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(rlLayout, layoutParams);

		llPager.updatePage(mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		ViewAdapter mapViewAdapter = mMapPoiControl.getView(mapPoiViewData);
		RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rlLayout.addView(mapViewAdapter.view, rlLayoutParams);

		//boolean isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;

		if(WinLayout.isVertScreen){
			mPoiListLayout = new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.VERTICAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			rlLayout.addView(mPoiListLayout, rlLayoutParams);

            View view = new View(GlobalContext.get());
            LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
            mPoiListLayout.addView(view, mPoilayoutParams);

			ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
			mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
		}else{
			mPoiListLayout= new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.HORIZONTAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			rlLayout.addView(mPoiListLayout, rlLayoutParams);

			ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
			LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,6);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);

			View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,4);
			mPoiListLayout.addView(view, mPoilayoutParams);
		}

		llLayoutLoading = new LinearLayout(GlobalContext.get());
		llLayoutLoading.setGravity(Gravity.CENTER);
		llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
		llLayoutLoading.setVisibility(View.GONE);
		llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
		rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rlLayout.addView(llLayoutLoading, rlLayoutParams);


		mLoadingImg = new ImageView(GlobalContext.get());
		mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llLayoutLoading.addView(mLoadingImg, layoutParams);
		MapPoiPoiList.getInstance().setNumberOnClickListener(new  TextClickListener() {

			@Override
			public void onClick(int index) {
				mMapPoiControl.checkPoiDeal(index);
			}
		});
		mMapPoiControl.setNumberOnClickListener( new TextClickListener() {
			@Override
			public void onClick(int index) {
				MapPoiPoiList.getInstance().checkPoiDeal(index);
			}
		});
		return llLayout;

	}

	private View createViewNone(ViewData data) {
		MapPoiListViewData mapPoiViewData = (MapPoiListViewData) data;
		WinLayout.getInstance().vTips = mapPoiViewData.vTips;
		//ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
		//ListTitleView.getInstance().setIvIcon("nav");
		ViewAdapter titleViewAdapter;
		if (Poi.PoiAction.ACTION_NAV_HISTORY.equals(mapPoiViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(mapPoiViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}

		mCurPage = mapPoiViewData.mTitleInfo.curPage;
		mMaxPage = mapPoiViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents, layoutParams);

		llPager.updatePage(mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(divider, layoutParams);

		RelativeLayout rlLayout = new RelativeLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llContents.addView(rlLayout, layoutParams);

		/*ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
		mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3*2);
		//mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
		llContents.addView(listViewAdapter.view, mPoilayoutParams);*/

		//ViewAdapter mapViewAdapter = mMapPoiControl.getView(mapPoiViewData);
		RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		//rlLayout.addView(mapViewAdapter.view, rlLayoutParams);

		boolean isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;

		if(isVertical){
			mPoiListLayout = new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.VERTICAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			rlLayout.addView(mPoiListLayout, rlLayoutParams);

			ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
			//mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
			LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);

			/*View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
			mPoiListLayout.addView(view, mPoilayoutParams);*/
		}else{
			mPoiListLayout= new LinearLayout(GlobalContext.get());
			mPoiListLayout.setOrientation(LinearLayout.HORIZONTAL);
			rlLayoutParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
			rlLayout.addView(mPoiListLayout, rlLayoutParams);

			ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
			//mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
            LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
			mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);

			/*View view = new View(GlobalContext.get());
			mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
			mPoiListLayout.addView(view, mPoilayoutParams);*/
		}

		llLayoutLoading = new LinearLayout(GlobalContext.get());
		llLayoutLoading.setGravity(Gravity.CENTER);
		llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
		llLayoutLoading.setVisibility(View.GONE);
		llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
		rlLayoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rlLayout.addView(llLayoutLoading, rlLayoutParams);

		mLoadingImg = new ImageView(GlobalContext.get());
		mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llLayoutLoading.addView(mLoadingImg, layoutParams);
		MapPoiPoiList.getInstance().setNumberOnClickListener(new  TextClickListener() {

			@Override
			public void onClick(int index) {
				mMapPoiControl.checkPoiDeal(index);
			}
		});
		mMapPoiControl.setNumberOnClickListener( new TextClickListener() {
			@Override
			public void onClick(int index) {
				MapPoiPoiList.getInstance().checkPoiDeal(index);
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
		MapPoiView.getInstance().init();
		setMapPoiContril(MapPoiView.getInstance().getMapPoiControl());
		MapPoiPoiList.getInstance().init();
		ListTitleView.getInstance().init();
	}
	
	@Override
	public void release() {
		LogUtil.logd(WinLayout.logTag+ "release: mappoilist");
		mPoiListLayout = null;
		//mPoilayoutParams = null;
		mLoadingImg= null;
		llLayoutLoading = null;

		/*if (llLayout != null){
			llLayout.removeAllViews();
			llLayout = null;
		}*/
		super.release();
	}

	@Override
	public Object updateView(ViewData data) {
		MapPoiListViewData viewData =(MapPoiListViewData) data;

		llLayoutLoading.setVisibility(View.GONE);
		LogUtil.logd(WinLayout.logTag+"updateView viewData.mMapAction="+viewData.mMapAction+"--"+viewData.isBus);
		if(viewData.mMapAction != null && viewData.mMapAction == MAP_ACTION_LOADING){
			llLayoutLoading.setVisibility(View.VISIBLE);
			((AnimationDrawable) mLoadingImg.getDrawable()).start();
            WinLayout.getInstance().vTips = null;
		}
		if (mMapPoiControl != null){
			mMapPoiControl.updata(data);
		}

		//放大缩小时，不更新列表界面
		if (viewData.mMapAction != null && (viewData.mMapAction == MAP_ACTION_ENLARGE || viewData.mMapAction == MAP_ACTION_NARROW)){
            return super.updateView(data);
        }

		mCurPage = viewData.mTitleInfo.curPage;
		mMaxPage = viewData.mTitleInfo.maxPage;
		llPager.updatePage(mCurPage,mMaxPage);
		ListTitleView.getInstance().isBusinessTitle = viewData.isBus;
		WinLayout.getInstance().vTips = viewData.vTips;
		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				WinLayoutFull.getInstance().setGuideText();
				//if(viewData.mMapAction == null){
                    ListTitleView.getInstance().updata(viewData,Poi.PoiAction.ACTION_NAV_HISTORY.equals(viewData.action));
					mPoiListLayout.removeAllViewsInLayout();
					View view = new View(GlobalContext.get());
                    LinearLayout.LayoutParams mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
					mPoiListLayout.addView(view, mPoilayoutParams);
					ViewAdapter listViewAdapter = MapPoiPoiList.getInstance().getView(data);
                    mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
					mPoiListLayout.addView(listViewAdapter.view, mPoilayoutParams);
				//}
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				WinLayoutHalf.getInstance().setGuideText();
				//if(viewData.mMapAction == null){
				if (WinLayout.isVertScreen){
					ListTitleView.getInstance().updata(viewData,Poi.PoiAction.ACTION_NAV_HISTORY.equals(viewData.action));
					mPoiListLayout.removeAllViewsInLayout();
					View view1 = new View(GlobalContext.get());
					LinearLayout.LayoutParams mPoilayoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f);
					mPoiListLayout.addView(view1, mPoilayoutParams1);
					ViewAdapter listViewAdapter1 = MapPoiPoiList.getInstance().getView(data);
					mPoilayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
					mPoiListLayout.addView(listViewAdapter1.view, mPoilayoutParams);
				}else {
					ListTitleView.getInstance().updata(viewData,Poi.PoiAction.ACTION_NAV_HISTORY.equals(viewData.action));
					mPoiListLayout.removeAllViewsInLayout();
					ViewAdapter listViewAdapter2 = MapPoiPoiList.getInstance().getView(data);
					LinearLayout.LayoutParams mPoilayoutParams2 = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,6);
					mPoiListLayout.addView(listViewAdapter2.view, mPoilayoutParams2);
					View view2 = new View(GlobalContext.get());
					mPoilayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,4);
					mPoiListLayout.addView(view2, mPoilayoutParams);
				}
				//}
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				break;
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
	public void updateItemSelect(int index) {
		// TODO Auto-generated method stub
        LogUtil.logd(WinLayout.logTag+ "mapPoiList updateItemSelect " + index);
		MapPoiPoiList.getInstance().updateItemSelect(index);
	}
	
}
