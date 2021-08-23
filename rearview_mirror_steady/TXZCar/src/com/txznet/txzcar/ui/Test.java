package com.txznet.txzcar.ui;

import com.txznet.txzcar.MyApplication;
import com.txznet.txzcar.R;
import com.txznet.txzcar.util.ResUtil;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

public class Test extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		
		final ImageView iv = (ImageView) findViewById(R.id.iv);
		
		MyApplication.getApp().runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				Drawable drawable = getResources().getDrawable(R.drawable.icon_head_back);
				Drawable d = ResUtil.createCorner(ResUtil.getInstance().getCiv(), drawable);
				if(d != null){
					d.setBounds(0,0,d.getIntrinsicWidth(),d.getIntrinsicHeight());
					iv.setImageDrawable(d);
				}
			}
		}, 3000);
	}
}
