package com.txznet.txzcar.ui;

import java.io.File;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.txznet.comm.base.StackActivity;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.txzcar.R;

public class MainActivity extends StackActivity {
	
	private boolean isInit;
	private boolean isVerify;
	private boolean isInitAccess;
	private String mSDCardPath = "";
	static final String APP_FOLDER_NAME = "MultiNavCar";
	
	private ImageView mAnim;
	private Button mClose;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
		setContentView(R.layout.activity_init);
		initView();
	}
	
	private void init(){
		if(!initDirs()){
			// 初始化SDcard路径失败
			return ;
		}
		
		NaviInitListener mNaviInitListener = new NaviInitListener() {
			
			@Override
			public void onAuthResult(int status, String msg) {
				if( 0 == status){
					// key校验成功
					Log.d("KEY", "key验证成功！");
					isVerify = true;
				}else {
					Log.e("KEY", "key验证失败，" + msg);
					isVerify = false;
				}
				
				if(isVerify && isInit){
					initComplete();
				}
			}
			
			@Override
			public void initSuccess() {
				isInit = true;
				if(isVerify){
					initComplete();
				}
				Log.d("KEY", "引擎初始化成功！");
			}
			
			@Override
			public void initStart() {
				Log.d("KEY", "引擎开始初始化！");
			}
			
			@Override
			public void initFailed() {
				isInit = false;
				Log.d("KEY", "引擎初始化失败！");
			}
		};
		
		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, mNaviInitListener, new BNOuterTTSPlayerCallback() {
			
			@Override
			public void stopTTS() {
			}
			
			@Override
			public void resumeTTS() {
			}
			
			@Override
			public void releaseTTSPlayer() {
			}
			
			@Override
			public int playTTSText(String arg0, int arg1) {
				TtsUtil.speakText(arg0);
				return 0;
			}
			
			@Override
			public void phoneHangUp() {
				
			}
			
			@Override
			public void phoneCalling() {
			}
			
			@Override
			public void pauseTTS() {
			}
			
			@Override
			public void initTTSPlayer() {
				
			}
			
			@Override
			public int getTTSState() {
				return 0;
			}
		});
	}
	
	private void initView(){
		mAnim = (ImageView) findViewById(R.id.imgInit_Anim);
		if (mAnim.getDrawable() != null && mAnim.getDrawable() instanceof AnimationDrawable) {
			AnimationDrawable animDrawable = (AnimationDrawable) mAnim.getDrawable();
			animDrawable.start();
		}
		mClose = (Button) findViewById(R.id.btnInit_Close);
		mClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void initComplete(){
		if(isInitAccess){
			return;
		}
		
		isInitAccess = true;
		finish();
		// 初始化成功，跳转
		RoutePlanActivity.navigateTo(this);
	}
	
	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if ( mSDCardPath == null ) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if ( !f.exists() ) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}
}
