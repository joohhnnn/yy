package com.txznet.nav.multinav;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.nplatform.comapi.basestruct.GeoPoint;
import com.baidu.nplatform.comapi.basestruct.Point;
import com.baidu.nplatform.comapi.map.MapController;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.R;
import com.txznet.nav.ui.widget.UserViewGroup;

public class MultiNavService {

	private TextView mDistance;
	private TextView mTime;
	
	private View mTopView;
	private View mNoteView;
	
	private Rect mNoteRect = new Rect();
	private Rect mTopRect = new Rect();
	private Rect mMapRect = new Rect();
	
	private View mNavView;
	private UserViewGroup mUvg;
	private MapGLSurfaceView mMglsfv;
	private MapController mapController;
	
	private UserNavPointThread mUnpt;
	private InvalidateTask mInvalidateTask;
	
	private static MultiNavService INSTANCE;
	
	private MultiNavService(){ }

	public static MultiNavService getInstance(){
		if(INSTANCE == null){
			synchronized (MultiNavService.class) {
				if(INSTANCE == null){
					INSTANCE = new MultiNavService();
				}
			}
		}
		return INSTANCE;
	}
	
	public MultiNavService init(MapGLSurfaceView msfv,View navView){
		this.mNavView = navView;
		this.mMglsfv = msfv;

		this.mapController = mMglsfv.getController();
		if(MultiNavManager.isDebug()){
			checkViewGroup(mNavView);
		}
		
		initView();
		return INSTANCE;
	}
	
	private void initView(){
		findWidget();
		initMultiNavMapView();
	}
	
	/**
	 * 根据Geo坐标得到屏幕坐标
	 * @param lat
	 * @param lng
	 * @return
	 */
	public int[] getScreenPointByGeoPoint(int lng,int lat){
		int[] mResult = new int[2];
		Point mScrPos = null;
		if(mapController != null){
			try {
				GeoPoint mGeoPoint = new GeoPoint();
				mGeoPoint.setLatitudeE6(lat);
				mGeoPoint.setLongitudeE6(lng);
				mScrPos = mapController.getScreenPosByGeoPos(mGeoPoint);
			} catch (Exception e) {
			}
		}
		
		if(mScrPos == null){
			return null;
		}
		
		mResult[0] = mScrPos.x;
		mResult[1] = mScrPos.y;
		return mResult;
	}

	/**
	 * 距离 2131165336  剩余时间 2131165342
	 */
	private void findWidget(){
		mDistance = (TextView) mNavView.findViewById(2131165336);
		mTime = (TextView) mNavView.findViewById(2131165342);
		
		mTopView = mNavView.findViewById(2131165331);
		mNoteView = mNavView.findViewById(2131165327);
	}
	
	/**
	 * 获取左上角控件的Rect
	 * @return
	 */
	public Rect getNoteRect(){
		if(mNoteView != null){
			mNoteView.getHitRect(mNoteRect);
		}
		return mNoteRect;
	}
	
	/**
	 * 获取上边缘ViewGroup的Rect
	 * @return
	 */
	public Rect getTopRect(){
		if(mTopView != null){
			mTopView.getHitRect(mTopRect);
		}
		return mTopRect;
	}
	
	/**
	 * 得到地图全局Rect
	 * @return
	 */
	public Rect getMapViewRect(){
		if(mMglsfv != null){
			mMglsfv.getHitRect(mMapRect);
		}
		return mMapRect;
	}
	
	public void onResume(){
		if(mUnpt != null){
			mUnpt.onResume();
			mInvalidateTask.onResume();
			mUvg.setVisibility(View.VISIBLE);
		}
	}
	
	public void onPause(){
		if(mUnpt != null){
			mUnpt.onWait();
			mInvalidateTask.onWait();
			mUvg.setVisibility(View.GONE);
		}
	}
	
	public void onStop(){
		if(mUnpt != null){
			mUnpt.onWait();
			mInvalidateTask.onWait();
			mUvg.setVisibility(View.GONE);
		}
	}
	
	public void onDestroy(){
		if(mUnpt != null){
			mUnpt.shutDown();
			mInvalidateTask.shutDown();
			mUvg.setVisibility(View.GONE);
		}
		
		INSTANCE = null;
	}
	
	/**
	 * 多车同行初始化
	 */
	private void initMultiNavMapView(){
		if(!NavManager.getInstance().isMultiNav()){
			return;
		}
		
		mUvg = (UserViewGroup) LayoutInflater.from(MyApplication.getApp()).inflate(R.layout.overlay_view, null);
		mMglsfv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				mMglsfv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				if(NavManager.getInstance().isMultiNav()){
					mUnpt = new UserNavPointThread(mUvg);
					mInvalidateTask = new InvalidateTask(mUvg);
					mUnpt.start();
					mInvalidateTask.start();
				}
			}
		});
		
		mMglsfv.setMapViewListener(new MultiMapViewListener(mUvg));
		
		if(NavManager.getInstance().isMultiNav()){
			mUvg.setVisibility(View.VISIBLE);
		}
		
		mNavView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				mNavView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				((ViewGroup)mNavView).addView(mUvg, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			}
		});
	}
	
	/**
	 * 获取剩余的距离
	 * @return
	 */
	public String getRemainDistance(){
		return mDistance.getText().toString();
	}
	
	/**
	 * 获取剩余时间
	 * @return
	 */
	public String getRemainTime(){
		return mTime.getText().toString();
	}
	
	/**
	 * 解析View中所有的ViewGroup和控件
	 * @param view
	 */
	private void checkViewGroup(View view){
		if(view instanceof ViewGroup){
			ViewGroup v = (ViewGroup)view;
			Rect outRect = new Rect();
			v.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view ViewGroup childCount is:"+v.getChildCount()+",id is:"+v.getId()+",Rect is:"+outRect);
			for(int i = 0;i<v.getChildCount();i++){
				checkViewGroup(v.getChildAt(i));
			}
		}else if(view instanceof TextView){
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view TextView rect is:"+outRect.toShortString()+",text is:"+((TextView)view).getText().toString()+",id is:"+view.getId());
		}else if(view instanceof ImageView){
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view ImageView rect is:"+outRect.toShortString()+",id is:"+view.getId());
		}else {
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view rect is:"+outRect.toShortString() + ",name is:"+view.getClass().getName());
		}
	}
}