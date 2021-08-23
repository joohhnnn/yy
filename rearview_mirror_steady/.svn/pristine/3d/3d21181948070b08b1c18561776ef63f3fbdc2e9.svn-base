package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZCameraManager;
import com.txznet.sdk.TXZCameraManager.CameraTool;
import com.txznet.sdk.TXZCameraManager.CapturePictureListener;
import com.txznet.sdk.TXZCameraManager.CaptureVideoListener;
import com.txznet.sdkdemo.SDKDemoApp;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class CameraActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "设置抓拍工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZCameraManager.getInstance().setCameraTool(mCameraTool);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "取消抓拍工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZCameraManager.getInstance().setCameraTool(null);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "启用全局唤醒抓拍", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZCameraManager.getInstance().useWakeupCapturePhoto(true);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "取消全局唤醒抓拍", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZCameraManager.getInstance().useWakeupCapturePhoto(false);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));
	}

	public static CameraTool mCameraTool = new CameraTool() {

		public boolean capturePicure(long time,
				final CapturePictureListener listener) {
			// TODO 抓拍实现
			SDKDemoApp.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					// TODO 保存
					// listener.onSave("/camera/123.jpg");
					// TODO 出错
					listener.onError(TXZCameraManager.CAPTURE_ERROR_NO_CAMERA,
							"没有摄像头");
				}
			}, 2000);
			
			DebugUtil.showTips("收到抓拍请求");
			
			return true;
		}

		@Override
		public boolean captureVideo(CaptureVideoListener arg0,
				CaptureVideoListener arg1) {
			// TODO Auto-generated method stub
			return false;
		}
	};
}
