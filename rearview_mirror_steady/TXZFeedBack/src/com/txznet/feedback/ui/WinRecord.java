package com.txznet.feedback.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.service.RecordService;

public class WinRecord extends Activity {

	static WinRecord mInstance;

	private ImageView mVolumeIv;
	private TextView mTimeTv;
	private TextView mOkTv;
	private String timeFormat = "停止说话，%s秒后自动发送";
	private LinearLayout mLayout;
	private Rect mHitRect = new Rect();
	
	private static OnRecordListener mOnRecordListener;
	
	private static volatile boolean isCancel;

	public static WinRecord getInstance() {
		return mInstance;
	}

	public static void navigate() {
		Intent intent = new Intent(AppLogic.getApp(), WinRecord.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		AppLogic.getApp().startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.win_record_layout);
		mInstance = this;
		init();
		
		RecordService.getInstance().record();
	}

	private void init() {
		mVolumeIv = (ImageView) findViewById(R.id.volume_iv);
		mTimeTv = (TextView) findViewById(R.id.time_tv);
		mOkTv = (TextView) findViewById(R.id.send_tv);
		mLayout = (LinearLayout) findViewById(R.id.ll);

		mLayout.getViewTreeObserver().addOnGlobalLayoutListener(
			new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				mLayout.getHitRect(mHitRect);
			}
		});

		mOkTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecorderUtil.stop();
			}
		});

		mVolumeIv.setImageResource(volumeDrawables[0]);
		isCancel = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		if (mHitRect != null && mHitRect.contains(x, y)) {
			return super.onTouchEvent(event);
		}
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			finish();
			Toast.makeText(AppLogic.getApp(), "已取消反馈", Toast.LENGTH_LONG).show();
			RecorderUtil.stop();
			isCancel = true;
			LogUtil.loge("取消反馈");
		}
		
		return true;
	}

	public void refreshVolume(int volume) {

	}
	
	public static boolean isCancel(){
		return isCancel;
	}

	public void refreshTimeRemain(final int seconds) {
		AppLogic.runOnUiGround(new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				if (seconds <= 0) {
					mTimeTv.setVisibility(View.INVISIBLE);
					mOkTv.setText("正在反馈");
					mOkTv.setEnabled(false);
					mOkTv.setBackground(AppLogic.getApp()
							 .getResources()
							 .getDrawable(R.drawable.cancel_enable_bg));
					AppLogic.runOnUiGround(new Runnable() {
						
						@Override
						public void run() {
							finish();
						}
					}, 1000);
					
					return;
				}

				String time = String.format(timeFormat, seconds);
				mTimeTv.setText(time);
			}
		}, 0);
	}

	@Override
	public void finish() {
		invokeListener();
		mInstance = null;
		super.finish();
	}

	@Override
	public void onBackPressed() {
		mInstance = null;
		super.onBackPressed();
	}

	private int[] volumeDrawables = new int[] { 
			R.drawable.icon_line_1,
			R.drawable.icon_line_2,
			R.drawable.icon_line_3,
			R.drawable.icon_line_4,
			R.drawable.icon_line_5,
			R.drawable.icon_line_6
			};
	
	public static void addRecordListener(OnRecordListener listener){
		mOnRecordListener = listener;
	}
	
	private void invokeListener(){
		if(mOnRecordListener != null){
			mOnRecordListener.onEnd();
		}
	}
	
	public interface OnRecordListener{
		public void onEnd();
	}
}
