package com.txznet.nav.ui.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.nav.MyApplication;
import com.txznet.txz.util.runnables.Runnable1;

public class UserViewGroup extends ViewGroup{
	
	List<DirectionCiv> mDcList;
	
	public UserViewGroup(Context context) {
		super(context);
	}
	
	public UserViewGroup(Context context,AttributeSet attr) {
		super(context,attr);
	}
	
	public UserViewGroup(Context context,AttributeSet attr,int defValue) {
		super(context,attr,defValue);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		int childCount = getChildCount();
		for(int i = 0;i<childCount;i++){
			DirectionCiv view = (DirectionCiv) getChildAt(i);
			if(view.getVisibility() == GONE){
				continue;
			}
			
			int w = view.getMeasuredWidth();
			int h = view.getMeasuredHeight();
			int[] point = getViewPoint(view, w, h);
			Log.d("MultiNav", "MultiNav ----------- UserViewGroup Children layout point ------------->w:"+w+",h:"+h+","+"x:"+point[0]+",y:"+point[1]);
			if(point != null){
				view.layout(point[0], point[1], point[0] + w, point[1] + h);
			}
		}
		super.dispatchDraw(canvas);
	}
	
	private int[] getViewPoint(View view,int w,int h){
		Point point = (Point) view.getTag();
		int[] p = new int[2];
		if(point != null){
			p[0] = point.x - w / 2;
			p[1] = point.y - h;
		}
		
		return p;
	}
	
	public void setUserList(List<DirectionCiv> mDc){
		MyApplication.getApp().runOnUiGround(new Runnable1<List<DirectionCiv>>(mDc) {
			
			@Override
			public void run() {
				removeAllViewsInLayout();
				addChilds(mP1);
			}
		}, 0);
	}
	
	private void addChilds(List<DirectionCiv> mCivs){
		Log.d("MultiNav", "MultiNav------->UserViewGroup:addChilds size is:" + mCivs.size());
		if(mCivs != null){
			for(DirectionCiv civ:mCivs){
				LayoutParams params = civ.getLayoutParams();
				if(params == null){
					params = new LayoutParams(64,64);
				}
				
				int widthSpec = MeasureSpec.makeMeasureSpec(64, MeasureSpec.EXACTLY);
				int heightSpec = MeasureSpec.makeMeasureSpec(64, MeasureSpec.EXACTLY);
				measureChildren(widthSpec, heightSpec);
				
				addViewInLayout(civ, 0, params, true);
			}
		}
		
		requestLayout();
	}
	
	public void refreshVisibleGeoPoint(String uid,Point tagPoint,int color){
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final DirectionCiv dc = (DirectionCiv) getChildAt(i);
			if (dc != null && uid.equals(dc.getUserId())) {
				dc.playAnim(false);
				dc.setRotateDegree(180);
				dc.setTag(tagPoint);
				dc.setBorderColor(color);
				break;
			}
		}
	}
	
	public void refreshOutGeoPoint(String uid,Point tagPoint){
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			final DirectionCiv dc = (DirectionCiv) getChildAt(i);
			if (dc != null && uid.equals(dc.getUserId())) {
				Log.d("UserViewGroup", "------------->w:"+dc.getMeasuredWidth()+",h:"+dc.getMeasuredHeight());
				dc.playAnim(true);
				dc.setBorderColor(Color.parseColor("#ff5E00"));
				dc.setTag(tagPoint);
				dc.invalidateView();
				dc.updateFinger(tagPoint.x, tagPoint.y);
				break;
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("onTouchEvent", "------------->onTouchEvent");
		return false;
	}
	
	public void invalidateRefresh(){
		if(Looper.myLooper() == Looper.getMainLooper()){
			invalidate();
		}else {
			postInvalidate();
		}
	}
}
