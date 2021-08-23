package com.txznet.nav.multinav;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.nav.MyApplication;
import com.txznet.nav.R;
import com.txznet.nav.ui.widget.DirectionCiv;

public class WTDUtil {
	
	@SuppressLint("InflateParams")
	public static DirectionCiv generateDc(int color,float degress,String imagePath,String uid){
		final DirectionCiv dCiv = (DirectionCiv) LayoutInflater.from(MyApplication.getApp()).inflate(R.layout.map_user_ly, null);
		dCiv.initalize(uid);
		
		int size = (int) MyApplication.getApp().getResources().getDimension(R.dimen.x32);
		dCiv.setCircleImageSize(size);
		dCiv.setBorderColor(color);
		dCiv.rotateDirect(degress);
		dCiv.setUserDrawable(getDefaultDrawable());
		dCiv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		
		try {
			ImageLoader.getInstance().loadImage(imagePath, new SimpleImageLoadingListener(){
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					super.onLoadingComplete(imageUri, view, loadedImage);
					Log.d("WTDUtil", "WTDUtil ----------- > gen onLoadingComplete loadedImage " + imageUri);
					if(loadedImage != null){
						dCiv.setUserBitmap(loadedImage);
					}
				}
			});
		} catch (Exception e) {
		}
		return dCiv;
	}
	
	public static void setDirectionDiv(final DirectionCiv dc,String imagePath){
		try {
			ImageLoader.getInstance().loadImage(imagePath, new SimpleImageLoadingListener(){
				
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					super.onLoadingComplete(imageUri, view, loadedImage);
					Log.d("WTDUtil", "WTDUtil ----------- > set onLoadingComplete loadedImage :" + imageUri);
					if(loadedImage != null){
						dc.setUserBitmap(loadedImage);
					}
				}
			});
		} catch (Exception e) {
		}
	}
	
	public static Drawable getDefaultDrawable(){
		return MyApplication.getApp().getResources().getDrawable(R.drawable.default_headimage);
	}
}