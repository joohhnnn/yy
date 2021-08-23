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

package com.txznet.launcher.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.launcher.R;
import com.txznet.loader.AppLogic;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DigitalClockWithDate extends LinearLayout {
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

	private TextView txtDay;

    public DigitalClockWithDate(Context context) {
        super(context);
        initClock();
    }

    public DigitalClockWithDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_digital_date_clock, this);
        mNumbers = new ImageView[]{
                (ImageView) findViewById(R.id.imgDigital_Clock_Number1),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number2),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number3),
                (ImageView) findViewById(R.id.imgDigital_Clock_Number4)
        };
        txtDay = (TextView) findViewById(R.id.txt_Day);
        initClock();
    }

    private void initClock() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
    }
    
    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                refreshDayInfo(0);
            }
        }
    };

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
        refreshDayInfo(0);
        getContext().registerReceiver(mTimeReceiver,  new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
        getContext().unregisterReceiver(mTimeReceiver);
    }

    private void refreshView(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        mNumbers[0].setImageResource(TEMPERATURE_DRAWABLE_RES[hour / 10]);
        mNumbers[1].setImageResource(TEMPERATURE_DRAWABLE_RES[hour % 10]);
        mNumbers[2].setImageResource(TEMPERATURE_DRAWABLE_RES[minute / 10]);
        mNumbers[3].setImageResource(TEMPERATURE_DRAWABLE_RES[minute % 10]);
    }
    
    public void refreshDayInfo(long delay) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat=new  SimpleDateFormat("yyyy-MM-dd E");
                txtDay.setText(dateFormat.format(calendar.getTime()));
            }
        }, delay);
    }
    
}
