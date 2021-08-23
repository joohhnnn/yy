package com.txznet.record.util;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

public class ListViewItemAnim {
	public static LayoutAnimationController getAnimationController() {  
        
          
  
        LayoutAnimationController controller = new LayoutAnimationController(getAnimationSet(), 0.08f);  
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);  
        return controller;  
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
}
