package com.txznet.txzcar.util;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txzcar.MyApplication;
import com.txznet.txzcar.OverlayManager;
import com.txznet.txzcar.R;
import com.txznet.txzcar.ui.widget.CircleImageView;

public class ResUtil {
	
	Map<String, Drawable> mId2DrawableMap = new HashMap<String,Drawable>();
	
	CircleImageView mCiv;
	Drawable mDefautlMarkDrawable;
	
	private static ResUtil instance = new ResUtil();
	
	private ResUtil(){
		MyApplication.getApp().runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				initCiv();
				createDefaultMark();
			}
		}, 1000);
	}
	
	public static ResUtil getInstance(){
		return instance;
	}
	
	private void initCiv(){
		mCiv = (CircleImageView) LayoutInflater.from(MyApplication.getApp()).inflate(R.layout.civ_layout, null);
		LayoutParams params = mCiv.getLayoutParams();
		int w = getPixelWidth();
		int h = getPixelHeight();
		if(params == null){
			params = new LayoutParams(w,h);
		}
		params.width = w;
		params.height = h;
		mCiv.setLayoutParams(params);
	}
	
	private void createDefaultMark(){
		mDefautlMarkDrawable = createCorner(mCiv, getDefaultDrawable());
	}
	
	/**
	 * 通过用户id取得头像
	 * @param uid
	 * @return
	 */
	public Drawable getDrawableByUid(final String uid,final String imagePath){
		synchronized (mId2DrawableMap) {
			Drawable drawable = mId2DrawableMap.get(uid);
			if(drawable == null){
//				String url = mId2ImageUrlMap.get(uid);
				if(!TextUtils.isEmpty(imagePath)){
					ImageLoader.getInstance().loadImage(imagePath, new SimpleImageLoadingListener(){
						
						@Override
						public void onLoadingComplete(String imageUri,View view, Bitmap loadedImage) {
							Log.d("ImageLoader", "ImageLoader -- >" + imageUri + ",Bitmap is:"+loadedImage + ",Thread is:" + Thread.currentThread().getName());
							if(loadedImage != null){
								MyApplication.getApp().runOnUiGround(new Runnable1<Bitmap>(loadedImage) {
									
									@Override
									public void run() {
										Drawable drawable = createCorner(createCiv(), mP1);
										if(drawable != null){
											synchronized (mId2DrawableMap) {
												mId2DrawableMap.put(uid, drawable);
											}
//									MyOverlayItem item = (MyOverlayItem) OverlayManager.getInstance().getOverlayByUserId(uid);
//									item.setHasSetDrawable(true);
											// 通知更新界面
											OverlayManager.getInstance().updateOverlayDrawable(uid, drawable);
										}
									}
								}, 0);
							}
						}
					});
					
//					mId2ImageUrlMap.put(uid, imagePath);
				}
			}
			return drawable;
		}
	}
	
	/**
	 * 生成圆角图像
	 * @param view
	 * @param drawable
	 * @return
	 */
	public static Drawable createCorner(CircleImageView view,Drawable drawable){
		view.setImageDrawable(drawable);
		view.setDrawingCacheEnabled(true);
		view.measure(MeasureSpec.makeMeasureSpec(getPixelWidth(), MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(getPixelHeight(), MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		if(bitmap == null){
			return null;
		}
		
		BitmapDrawable bd = new BitmapDrawable(MyApplication.getApp().getResources(), bitmap);
		return bd;
	}
	
	/**
	 * 生成圆角图
	 * @param view
	 * @param bitmap
	 * @return
	 */
	public static Drawable createCorner(CircleImageView view,Bitmap bitmap){
		view.setImageBitmap(bitmap);
		view.setDrawingCacheEnabled(true);
		view.measure(
				MeasureSpec.makeMeasureSpec(getPixelWidth(), MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(getPixelHeight(), MeasureSpec.EXACTLY));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bm = view.getDrawingCache();
		if(bm == null){
			return null;
		}
		
		BitmapDrawable bd = new BitmapDrawable(MyApplication.getApp().getResources(), bm);
		bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
		return bd;
	}
	
	private CircleImageView createCiv(){
		CircleImageView civ = (CircleImageView) LayoutInflater.from(MyApplication.getApp()).inflate(R.layout.civ_layout, null);
		LayoutParams params = civ.getLayoutParams();
		int w = getPixelWidth();
		int h = getPixelHeight();
		if(params == null){
			params = new LayoutParams(w,h);
		}
		params.width = w;
		params.height = h;
		civ.setLayoutParams(params);
		return civ;
	}
	
	public static int getPixelWidth(){
		int s = (int) MyApplication.getApp().getResources().getDimension(R.dimen.x80);
		return s;
	}
	
	public static int getPixelHeight(){
		int s = (int) MyApplication.getApp().getResources().getDimension(R.dimen.y80);
		return s;
	}
	
	public Drawable getDefaultMark(){
		return mDefautlMarkDrawable;
	}
	
	private static Drawable getDefaultDrawable(){
		Drawable drawable = MyApplication.getApp().getResources().getDrawable(R.drawable.default_headimage);
		drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
		return drawable;
	}
	
	public CircleImageView getCiv(){
		return mCiv;
	}
}
