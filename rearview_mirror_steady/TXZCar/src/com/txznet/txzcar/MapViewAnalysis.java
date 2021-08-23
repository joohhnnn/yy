package com.txznet.txzcar;

import android.widget.TextView;

import com.baidu.navisdk.comapi.mapcontrol.BNMapViewFactory;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;

public class MapViewAnalysis {
	
	private static final int mRemainTimeId = 1711866182;
	private static final int mRemainDistanceId = 1711866181;
	
	MapGLSurfaceView mMglsfv;
	
	private static MapViewAnalysis instance;
	
	private MapViewAnalysis(){ }
	
	public static MapViewAnalysis getInstance(){
		return instance;
	}
	
	/**
	 * 获取距离
	 * @return
	 */
	public String getDistance(){
		if(mMglsfv == null){
			mMglsfv = BNMapViewFactory.getInstance().getMainMapView();
		}
		
		if(mMglsfv == null){
			return "";
		}
		
		TextView tv = (TextView) mMglsfv.findViewById(mRemainDistanceId);
		String distance = tv.getText().toString();
		
		return distance;
	}
	
	/**
	 * 获取时间
	 * @return
	 */
	public String getTime(){
		if(mMglsfv == null){
			mMglsfv = BNMapViewFactory.getInstance().getMainMapView();
		}
		
		if(mMglsfv == null){
			return "";
		}
		
		TextView tv = (TextView) mMglsfv.findViewById(mRemainTimeId);
		String time = tv.getText().toString();
		
		return time;
	}
}