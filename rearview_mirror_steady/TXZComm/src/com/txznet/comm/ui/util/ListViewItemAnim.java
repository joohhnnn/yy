package com.txznet.comm.ui.util;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

public class ListViewItemAnim {
	public static LayoutAnimationController getAnimationController() {
		if (sEnableListAnim != null && !sEnableListAnim) {
			return null;
		}
        LayoutAnimationController controller = new LayoutAnimationController(getAnimationSet(), 0.08f);  
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);  
        return controller;  
    }  

	private static Boolean sEnableListAnim;

	public static void enableListAnim(boolean enable) {
		sEnableListAnim = enable;
	}

	public static AnimationSet getAnimationSet() {
		int duration=300;  
		AnimationSet set = new AnimationSet(true);  
		  
        Animation animation = new AlphaAnimation(0.2f, 1.0f);  
        animation.setDuration(duration);  
        set.addAnimation(animation);  
  
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,  
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,  
                0.9f, Animation.RELATIVE_TO_SELF, 0.0f);  
        animation.setDuration(duration);  
        set.addAnimation(animation);
        return set;
	}
	
	public static final int TYPE_TRANSLATE = 1;
	public static final int TYPE_ROTATEANIMATION = 2;
	
	public static LayoutAnimationController getAnimationController(int type) {  
        
		Animation animation = null;
		switch (type) {
		case TYPE_ROTATEANIMATION:
			animation = getFlipVertical();
			break;
		case TYPE_TRANSLATE:
			animation = getAnimationSet();
		default:
			break;
		}
        LayoutAnimationController controller = new LayoutAnimationController(animation, 0.08f);  
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);  
        return controller;  
    }
	
	public static Animation getFlipVertical() {
		int duration=600;  
		Animation animation = new FlipAnimation(0, -360, Animation.RELATIVE_TO_SELF, 0.5f,  
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);  
        return animation;
	}
}
