package com.txznet.nav.multinav;

import java.util.List;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.txznet.nav.multinav.MultiNavManager.LocationData;
import com.txznet.nav.ui.widget.DirectionCiv;
import com.txznet.nav.ui.widget.UserViewGroup;

public class UserNavPointThread extends Thread {
	private static final int SLEEP_DELAY = 100;

	private volatile boolean isWait;
	private volatile boolean mShutDown;

	private int BASE_TOP = 0;
	
	private UserViewGroup mUvg;
	private Rect mMapRect = new Rect();
	private Rect mTopRect = new Rect();
	private Rect mLeftTopRect = new Rect();

	public UserNavPointThread(UserViewGroup uvg) {
		mUvg = uvg;
		
		init();
	}
	
	private void init(){
		mMapRect = MultiNavService.getInstance().getMapViewRect();
//		mMapRect.top = BASE_TOP;
		mLeftTopRect = MultiNavService.getInstance().getNoteRect();
	}

	@Override
	public void run() {
		try {
			while ( !mShutDown ) {
				
				mTopRect = MultiNavService.getInstance().getTopRect();
				BASE_TOP = mTopRect.bottom;
//			mMapRect.top = BASE_TOP;
				
				if( isWait ){
					continue;
				}
				
				if(MultiNavManager.getInstance().isNeedReloadDc()){
					try {
						MultiNavManager.getInstance().setNeedReload(false);
						List<DirectionCiv> dcList = MultiNavManager.getInstance().getAllDic();
						mUvg.setUserList(dcList);
					} catch (Exception e) {
					}
					
					continue;
				}
				
				if(mUvg.getChildCount() < 1 && MultiNavManager.getInstance().getAllUserId().size() > 0){
					continue;
				}

				List<LocationData> mLdList = MultiNavManager.getInstance().getLocationDataList();
				if (mLdList != null && mLdList.size() > 0) {
					for (LocationData ld : mLdList) {
						int[] xy = MultiNavService.getInstance().getScreenPointByGeoPoint(ld.longitudeE6,ld.latitudeE6);
						if(xy == null){
							continue;
						}
						
						Point tagPoint = new Point();
						tagPoint.x = xy[0];
						tagPoint.y = xy[1];
						int x = tagPoint.x;
						int y = tagPoint.y;
						
						Log.d("MultiNav", "MultiNav ------> UserNavPointThread GeoPoint maping -------> "+tagPoint.toString());
						if (mMapRect.contains(tagPoint.x, tagPoint.y)) {
							DirectionCiv dc = null;
							if(mUvg.getChildCount() > 0){
								dc = (DirectionCiv) mUvg.getChildAt(0);
							}
							if(dc == null){
								continue;
							}
							if(mLeftTopRect.contains(tagPoint.x, tagPoint.y)){
								// 处理左上角 尖角向上
								tagPoint.x = tagPoint.x - (dc.getMeasuredWidth() / 2);
								if(tagPoint.x < dc.getMeasuredWidth() / 2){
									tagPoint.x = dc.getMeasuredWidth() / 2;
								}
								
								tagPoint.y = mLeftTopRect.bottom + dc.getMeasuredHeight();
							} else if(mTopRect.contains(tagPoint.x, tagPoint.y)){
								// 处理上边缘 尖角向上
								tagPoint.x = tagPoint.x - dc.getMeasuredWidth() / 2;
								if(tagPoint.x < (mTopRect.left + (dc.getMeasuredWidth() / 2))){
									tagPoint.x = mTopRect.left + (dc.getMeasuredWidth() / 2);
								}else if(tagPoint.x > (mTopRect.right - (dc.getMeasuredWidth() / 2))){
									tagPoint.x = mTopRect.right - (dc.getMeasuredWidth() / 2);
								}
								
								tagPoint.y = mTopRect.bottom + dc.getMeasuredHeight();
							}
							
							// 处理位于可移动范围
							if(ld.userId != null && !"".equals(ld.userId)){
								mUvg.refreshVisibleGeoPoint(ld.userId, tagPoint, Color.parseColor("#34bfff"));
							}
							
						} else {
							// 不可视范围，做旋转角度
							final int childCount = mUvg.getChildCount();
							for (int i = 0; i < childCount; i++) {
								final DirectionCiv dc = (DirectionCiv) mUvg.getChildAt(i);
								if (dc != null && ld.userId.equals(dc.getUserId())) {
									Log.d("UserViewGroup", "------------->w:"+dc.getMeasuredWidth()+",h:"+dc.getMeasuredHeight());
									
									{
										if(tagPoint.x <= 0 && tagPoint.y <=0){
											// 左上角
											tagPoint.x = dc.getMeasuredWidth() / 2;
											tagPoint.y = dc.getMeasuredHeight() + BASE_TOP;
										}
										
										if(tagPoint.x >= mMapRect.right && tagPoint.y <= 0){
											// 右上角
											tagPoint.x = mMapRect.right - (dc.getMeasuredWidth() / 2);
											tagPoint.y = dc.getMeasuredHeight() + BASE_TOP;
										}
										
										if(tagPoint.x <= 0 && tagPoint.y >= mMapRect.bottom){
											// 左下角
											tagPoint.x = dc.getMeasuredWidth() / 2;
											tagPoint.y = mMapRect.bottom;
										}
										
										if(tagPoint.x >= mMapRect.right && tagPoint.y >= mMapRect.bottom){
											// 右下角
											tagPoint.x = mMapRect.right - dc.getMeasuredWidth()/ 2;
											tagPoint.y = mMapRect.bottom;
										}
										
										if(tagPoint.x <= 0 && tagPoint.y >=0 && tagPoint.y <=mMapRect.bottom){
											// 靠左
											tagPoint.x = dc.getMeasuredWidth() / 2;
										}
										
										if(tagPoint.y <= 0 && tagPoint.x >=0 && tagPoint.x <= mMapRect.right){
											// 靠上
											tagPoint.y = dc.getMeasuredHeight() + BASE_TOP;
										}
										
										if(tagPoint.x >= mMapRect.right && tagPoint.y >=0 && tagPoint.y <= mMapRect.bottom){
											// 靠右
											tagPoint.x = mMapRect.right - dc.getMeasuredWidth() / 2;
										}
										
										if(tagPoint.y >= mMapRect.bottom && tagPoint.x >=0 && tagPoint.x <= mMapRect.right){
											// 靠下
											tagPoint.y = mMapRect.bottom;
										}
									}
									
									dc.playAnim(true);
									dc.setBorderColor(Color.parseColor("#ff5E00"));
									dc.setTag(tagPoint);
									dc.invalidateView();
									dc.updateFinger(x, y);
									break;
								}
							}
						}
					}
				}
				try {
					Thread.sleep(SLEEP_DELAY);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception e) {
		}
	}

	public boolean isShutDown() {
		return mShutDown;
	}

	public void shutDown() {
		mShutDown = true;
	}
	
	public void onWait(){
		isWait = true;
	}
	
	public void onResume(){
		isWait = false;
	}
}
