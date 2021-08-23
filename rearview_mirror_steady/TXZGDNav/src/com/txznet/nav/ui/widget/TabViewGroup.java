package com.txznet.nav.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.txznet.nav.R;

public class TabViewGroup extends FrameLayout implements OnPageChangeListener,OnClickListener{
	public static final int TAB_INDEX_INVALIDATE = -1;
	public static final int TAB_INDEX_ONE = 0;
	public static final int TAB_INDEX_TWO = 1;
	public static final int TAB_INDEX_THREE = 2;
	public static final int TAB_INDEX_FOUR = 3;
	
	private List<IconTextView> mIconTextViews;
	
	private ViewPager mViewPager;
	
	private View mContentView;
	
	public TabViewGroup(Context context){
		this(context,null);
	}
	
	public TabViewGroup(Context context,AttributeSet attr){
		this(context,attr,0);
	}
	
	public TabViewGroup(Context context,AttributeSet attr,int defValue){
		super(context, attr, defValue);
		setUpView(context);
		init();
	}
	
	@SuppressLint("InflateParams")
	private void setUpView(Context context){
		mIconTextViews = new ArrayList<IconTextView>();
		mContentView = LayoutInflater.from(context).inflate(R.layout.icon_text_view_list_layout, null);
		
		IconTextView mPositionOne = (IconTextView) mContentView.findViewById(R.id.id_indicator_one);
		IconTextView mPositionTwo = (IconTextView) mContentView.findViewById(R.id.id_indicator_two);
		
		mIconTextViews.add(mPositionOne);
		mIconTextViews.add(mPositionTwo);
		
		for(IconTextView itv:mIconTextViews){
			itv.setOnClickListener(this);
		}
		
		removeAllViews();
		addView(mContentView);
	}
	
	/**
	 * 初始化，第一个选项选中
	 */
	public void init(){
		resetOtherTab(-1);
		mIconTextViews.get(0).setIconAlpha(1.0f);
		mIconTextViews.get(0).setSel(true);
	}
	
	/**
	 * 获取视图
	 * @return
	 */
	public View getTabView(){
		return mContentView;
	}
	
	/**
	 * 绑定ViewPager
	 * @param viewPager
	 */
	public void setViewPager(ViewPager viewPager){
		if(viewPager == null){
			return ;
		}
		this.mViewPager = viewPager;
		this.mViewPager.setOnPageChangeListener(this);
	}
	
	/**
	 * 根据index点击某一个tab
	 * @param index
	 */
	public void onClickTabIndex(int index){
		onClick(mIconTextViews.get(index));
	}
	
	/**
	 * 启用点击效果
	 * @param enable
	 * @param index
	 */
	public void enableTabClick(boolean enable,int index){
		for(int i = 0;i<mIconTextViews.size();i++){
			if(i == index){
				mIconTextViews.get(i).setEnabled(enable);
			}else {
				mIconTextViews.get(i).setEnabled(!enable);
			}
		}
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}

	@Override
	public void onPageSelected(int position) {
		for(int i = 0;i<mIconTextViews.size();i++){
			if(i == position){
				continue;
			}
			mIconTextViews.get(i).setSel(false);
		}
		mIconTextViews.get(position).setSel(true);
	}

	@Override
	public void onClick(View v) {
		resetOtherTab(-1);
		int []viewId = getViewIdArray();
		for(int i = 0;i<viewId.length;i++){
			if(viewId[i] == v.getId()){
				switchToTab(i, false);
				break;
			}
		}
	}
	
	private void resetOtherTab(int position){
		for(int i = 0;i<mIconTextViews.size();i++){
			if(i == position){
				continue;
			}
			mIconTextViews.get(i).setIconAlpha(0.0f);
			mIconTextViews.get(i).setSel(false);
		}
	}
	
	private void switchToTab(int index,boolean smoothScroll){
		mIconTextViews.get(index).setIconAlpha(1.0f);
		mIconTextViews.get(index).setSel(true);
		mViewPager.setCurrentItem(index, smoothScroll);
	}
	
	private int[] getViewIdArray(){
		return new int[]{R.id.id_indicator_one,R.id.id_indicator_two};
	}
}