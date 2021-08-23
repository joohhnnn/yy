package com.txznet.nav.multinav;

import com.txznet.nav.ui.widget.UserViewGroup;

public class InvalidateTask extends Thread{
	
	private volatile boolean shutDown;
	private volatile boolean isWait;
	private UserViewGroup mUvg;
	
	public InvalidateTask(UserViewGroup uvg){
		mUvg = uvg;
	}
	
	public void run() {
		try {
			while (!shutDown) {
				
				if(isWait){
					continue;
				}
				
//			Log.d("Invalidate", "Invalidate -- > Distance is:"+MultiNavService.getInstance().getRemainDistance()+",time is:"+MultiNavService.getInstance().getRemainTime());
				mUvg.invalidateRefresh();
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
				}
			}
		} catch (Exception e) {
		}
	};
	
	public void shutDown(){
		shutDown = true;
	}
	
	public void onWait(){
		isWait = true;
	}
	
	public void onResume(){
		isWait = false;
	}
}
