/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.txznet.launcher.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.launcher.R;

import java.util.Calendar;
import java.util.TimeZone;

public class DigitalClockPlugin extends LinearLayout {
	private static final String TAG = DigitalClockPlugin.class.getSimpleName();
    Calendar mCalendar;

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;

    private final int[] TEMPERATURE_DRAWABLE_RES = {
            R.drawable.number_0, R.drawable.number_1,
            R.drawable.number_2, R.drawable.number_3,
            R.drawable.number_4, R.drawable.number_5,
            R.drawable.number_6, R.drawable.number_7,
            R.drawable.number_8, R.drawable.number_9};

    private ImageView[] mNumbers;

    public DigitalClockPlugin(Context context) {
        super(context);
        initClock();
    }

    public DigitalClockPlugin(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_digital_clock, this);
        mNumbers = new ImageView[]{
                (ImageView) findViewById(R.id.imgDigital_Clock_Number1),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number2),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number3),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number4)
        };
        initClock();
    }
    
    private BroadcastReceiver mTimeZoomReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)){
				 String tz = intent.getStringExtra("time-zone");
	             if(mCalendar != null){
	            	 mCalendar.setTimeZone(TimeZone.getTimeZone(tz));
	             }else {
	                 mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
	             }
	             Log.d(TAG, "mCalendar =" + mCalendar + "TimeZone.getTimeZone(tz) =" + TimeZone.getTimeZone(tz));
	             refreshView(mCalendar);
			}
		}
    	
    };
    
    private boolean mRegisted = false;
    
    private void registerTimeZoomReceiver(){
    	if(!mRegisted){
    		IntentFilter filter = new IntentFilter();
    		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    		getContext().registerReceiver(mTimeZoomReceiver, filter);
    		mRegisted = true;
    	}
    }
    

    private void initClock() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable() {
            public void run() {
                if (mTickerStopped) return;
                mCalendar.setTimeInMillis(System.currentTimeMillis());
                refreshView(mCalendar);
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mTicker.run();
        registerTimeZoomReceiver();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
        if(mRegisted){
        	getContext().unregisterReceiver(mTimeZoomReceiver);
        	mRegisted = false;
        }
    }

    private void refreshView(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        mNumbers[0].setImageResource(TEMPERATURE_DRAWABLE_RES[hour / 10]);
        mNumbers[1].setImageResource(TEMPERATURE_DRAWABLE_RES[hour % 10]);
        mNumbers[2].setImageResource(TEMPERATURE_DRAWABLE_RES[minute / 10]);
        mNumbers[3].setImageResource(TEMPERATURE_DRAWABLE_RES[minute % 10]);
    }
}
