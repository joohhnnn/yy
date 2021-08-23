package com.txznet.record.view;

import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.record.adapter.ChatDisplayAdapter;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.lib.R;
import com.txznet.record.util.ListViewItemAnim;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

public class DisplayLvRef extends FrameLayout implements OnPagerListener {

	private TitleView mTitleView;
	private DisplayLvEx mDisplayLvEx;
	private FrameLayout mContainLy;
	private FrameLayout mPoiMapLy;
	private ChatDisplayAdapter mAdapter;

	private OnTouchListener mOnTouchListener;

	public DisplayLvRef(Context context) {
		this(context, null);
	}

	public DisplayLvRef(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public DisplayLvRef(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		attach();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mOnTouchListener != null) {
			mOnTouchListener.onTouch(mDisplayLvEx, ev);
		}
		return super.dispatchTouchEvent(ev);
	} 

	private void attach() {
		removeAllViews();
		View.inflate(getContext(), R.layout.display_lv_container, this);
		mTitleView = (TitleView) findViewById(R.id.title_view);
		mDisplayLvEx = (DisplayLvEx) findViewById(R.id.list_ex);
		mDisplayLvEx.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		mContainLy = (FrameLayout) findViewById(R.id.display_content_ly);
		mPoiMapLy = (FrameLayout) findViewById(R.id.display_poimap_ly);
	}

	public void setAdapter(ChatDisplayAdapter adapter) {
		
		this.mAdapter = adapter;
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setAdapter(mAdapter);
		}
		int h = ScreenUtil.getDisplayLvItemH(false) * getVisibleCount();
		if (h > 0) { // 如果计算出的高度不为0，则设置
			ViewGroup.LayoutParams params = mDisplayLvEx.getLayoutParams();
			params.height = h;
			mDisplayLvEx.setLayoutParams(params);
		}
		checkVisible(DISPLAY_MODEL_LIST);
		mDisplayLvEx.startLayoutAnimation();
	}

	public void refreshLists(List items) {
		if (this.mAdapter != null) {
			mAdapter.setDisplayList(items);
			mDisplayLvEx.updateScrollBar();
			mDisplayLvEx.setSelection(0);
		}
		int h = ScreenUtil.getDisplayLvItemH(false) * getVisibleCount();
		if (h > 0) { // 如果计算出的高度不为0，则设置
			ViewGroup.LayoutParams params = mDisplayLvEx.getLayoutParams();
			params.height = h;
			mDisplayLvEx.setLayoutParams(params);
		}
		checkVisible(DISPLAY_MODEL_LIST);
		mDisplayLvEx.startLayoutAnimation();
	}

	public ListView getCurListView() {
		return mDisplayLvEx;
	}
	
	/**
	 * 动态插入地图显示
	 * 
	 * @param conView
	 */
	public void replacePoiMapView(View conView) {
		if (conView == null) {
			return;
		}
		mPoiMapLy.removeAllViewsInLayout();
		
		ViewParent viewParent = conView.getParent();
		if (viewParent != null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup) viewParent).removeView(conView);
			}
		}
		
		mPoiMapLy.addView(conView);
		mPoiMapLy.requestLayout();
		checkVisible(DISPLAY_MODEL_POIMAP);
	}
	
	/**
	 * poiMap是否正在显示
	 */
	public boolean poiMapVisible(){
		return mPoiMapLy.getVisibility() == View.VISIBLE;
	}
	/**
	 * 动态插入View显示
	 * 
	 * @param conView
	 */
	public void replaceView(View conView) {
		if (conView == null) {
			return;
		}
		mContainLy.removeAllViewsInLayout();
		
		ViewParent viewParent = conView.getParent();
		if (viewParent != null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup) viewParent).removeView(conView);
			}
		}
		
		mContainLy.addView(conView);
		mContainLy.requestLayout();

		checkVisible(DISPLAY_MODEL_CONTAIN);
	}
	public static final int DISPLAY_MODEL_LIST = 1;
	public static final int DISPLAY_MODEL_CONTAIN = 2;
	public static final int DISPLAY_MODEL_POIMAP=  3;
	
	private void checkVisible(int  showModel) {
		if(showModel == DISPLAY_MODEL_LIST){
			mContainLy.setVisibility(View.GONE);
			mDisplayLvEx.setVisibility(View.VISIBLE);	
			mPoiMapLy.setVisibility(View.GONE);
			WinPoiShow.getIntance().dismiss();
		}else if(showModel == DISPLAY_MODEL_CONTAIN){
			mDisplayLvEx.setVisibility(View.GONE);
			mContainLy.setVisibility(View.VISIBLE);	
			mPoiMapLy.setVisibility(View.GONE);
			WinPoiShow.getIntance().dismiss();
		}else if(showModel == DISPLAY_MODEL_POIMAP){
			mDisplayLvEx.setVisibility(View.GONE);
			mContainLy.setVisibility(View.GONE);	
			mPoiMapLy.setVisibility(View.VISIBLE);			
		}
	}

	@Override
	public void setOnTouchListener(OnTouchListener l) {
		mOnTouchListener = l;
	}

	public void refreshTitleView(BaseDisplayMsg bdm) {
		if (mTitleView != null && bdm != null && bdm.mTitleInfo != null) {
			mTitleView.setVisibility(View.VISIBLE);
			mTitleView.update(bdm.mTitleInfo);
		}else{
			if(mTitleView != null)
				mTitleView.setVisibility(View.GONE);
		}
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setOnItemClickListener(listener);
		}
	}
	
	public void setOnTitleClickListener(OnClickListener listener) {
		if (mTitleView != null) {
			mTitleView.setOnClickListener(listener);
		}
	}
	public void setOnTitleCityClickListener(OnClickListener listener) {
		if (mTitleView != null) {
			mTitleView.setCityOnClickListener(listener);
		}
	}

	public void setOnPagerSelectedListener(OnPagerListener listener) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setOnPagerListener(listener);
		}
	}

	@Override
	public void onPrePager(int sel) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setPrePager();
		}
	}

	@Override
	public void onNextPager(int sel) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setNextPager();
		}
	}

	public int getFirstVisiblePos() {
		if (mDisplayLvEx != null) {
			return mDisplayLvEx.getFirstPos();
		}
		return 0;
	}

	public int getVisibleCount() {
		if (mDisplayLvEx != null) {
			// return mDisplayLvEx.getSnapCount();
			// 返回计算出来的值
			return ScreenUtil.getVisbileCount();
		}
		return 0;
	}

	public void setSelection(int sel) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.setSelection(sel);
		}
	}

	public void updateScrollBar(int visible) {
		if (mDisplayLvEx != null) {
			mDisplayLvEx.updateScrollBar(visible);
		}
	}
}