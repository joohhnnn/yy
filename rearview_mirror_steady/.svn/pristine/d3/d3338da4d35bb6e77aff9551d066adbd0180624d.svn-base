package com.txznet.marketing;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.txznet.marketing.HttpRequest.HttpUtil;
import com.txznet.marketing.HttpRequest.NetWorkUtil;
import com.txznet.marketing.HttpRequest.SharedPrefencesUtil;
import com.txznet.marketing.bean.CommandPoint;
import com.txznet.marketing.ui.MediaPlayerSurfaceView;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.TXZTtsManager.ITtsCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private static MainActivity instance;
	private RelativeLayout main_rel;
	//动态注册语音界面显示状态的广播
	private mBroadcastReceiver mReceicver;
	//语音界面显示状态
	public boolean voiceState = false;
	//当前是否能够app功能
	private Boolean isSuccessful = false;
	//core是否初始化成功
    private boolean isCoreInitSuccessful = false;
    //应用是否在前台运行
	public boolean isRunningFont = false;

	public static MainActivity getInstance() {
		return instance;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View main_layout = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
		setContentView(main_layout);
		main_rel = (RelativeLayout) main_layout.findViewById(R.id.main_rel);
		instance = this;

        SharedPrefencesUtil.getmInstance().init(this);
        isSuccessful = SharedPrefencesUtil.getmInstance().getCanUse();
		Log.d(TAG, "read info: "+isSuccessful);

        isCoreInitSuccessful = TXZConfigManager.getInstance().isInitedSuccess();
        TXZConfigManager.getInstance().setConnectListener(new TXZConfigManager.ConnectListener() {
            @Override
            public void onConnect() {
                isCoreInitSuccessful = true;
                Log.d(TAG, "onConnect");
            }

            @Override
            public void onDisconnect() {
                isCoreInitSuccessful = false;
                Log.d(TAG, "onDisConnect");
            }

            @Override
            public void onExcepiton() {
                isCoreInitSuccessful = false;
                Log.d(TAG, "onError");
            }
        });

        if (NetWorkUtil.isNetConnected(this)){
            Log.d(TAG, "begin to connection ");
            HttpUtil.getmInstance().sendRequset(new HttpUtil.HttpCallbackListener() {
                @Override
                public void onFinish(Boolean canUse) {
                    isSuccessful = canUse;
                    SharedPrefencesUtil.getmInstance().setCanUse(isSuccessful);
                    beginPlay(isSuccessful);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, "HttpRequest onError: "+e);
                    beginPlay(isSuccessful);
                }
            });
        }else {
            beginPlay(isSuccessful);
        }


		//TXZConfigManager.getInstance().enableWakeup(false);
		//TXZConfigManager.getInstance().setWakeupKeywordsNew(null);
		//TXZConfigManager.getInstance().enableChangeWakeupKeywords(false);
	}

	private void beginPlay(boolean isCnaUse){
        //if (TXZConfigManager.getInstance().isInitedSuccess()) {
        if (isCoreInitSuccessful) {
            Log.d(TAG, "onCreate: "+isCnaUse);
            //if (TXZConfigManager.getInstance().getEnableSelfMarkting()){
            if (isCnaUse){
                //注册广播
                IntentFilter mFilter = new IntentFilter();
                mFilter.addAction("com.txznet.txz.record.show");
                mFilter.addAction("com.txznet.txz.record.dismiss");
                mReceicver = new mBroadcastReceiver();
                this.registerReceiver(mReceicver,mFilter);

                beginPlayMedia();

            }else {
                SDKDemoApp.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.getInstance(), "暂未获得使用授权", Toast.LENGTH_LONG).show();
                    }
                },0);
            }
        } else {
            SDKDemoApp.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.getInstance(), "同行者语音初始化失败", Toast.LENGTH_LONG).show();
                }
            },0);
        }
    }

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume: ");
		isRunningFont = true;

		/*//yunOS保持屏幕常亮
		try {
			Class SystemProperties = Class.forName("android.os.SystemProperties");
			Method set = SystemProperties.getDeclaredMethod("set", String.class, String.class);
			set.setAccessible(true);
			set.invoke(SystemProperties, "sys.refresh.systemui", "true");
			Log.d(TAG, "sys.refresh.systemui = true");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}*/
		//yunOS保持屏幕常亮
		/*Intent intent = new Intent("com.kayle.test.open");
		sendBroadcast(intent);*/

		//隐藏语音图标
		//TXZConfigManager.getInstance().showFloatTool(TXZConfigManager.FloatToolType.FLOAT_NONE);
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause: ");
		isRunningFont = false;

		/*//取消屏幕常亮
		try {
			Class SystemProperties = Class.forName("android.os.SystemProperties");
			Method set = SystemProperties.getDeclaredMethod("set", String.class, String.class);
			set.setAccessible(true);
			set.invoke(SystemProperties, "sys.refresh.systemui", "false");
			Log.d(TAG, "sys.refresh.systemui = false");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}*/
		//yunOS取消屏幕常亮
		/*Intent intent = new Intent("com.kayle.test.close");
		sendBroadcast(intent);*/
        if (isSuccessful){
            MediaPlayerSurfaceView.getInstance().release();
            Log.d(TAG, "onPause: 1");
        }
        Log.d(TAG, "onPause: 2");
		//显示语音图标
		//TXZConfigManager.getInstance().showFloatTool(TXZConfigManager.FloatToolType.FLOAT_TOP);
		super.onPause();
		finish();
	}

	@Override
	protected void onDestroy() {
		//注销广播
        if (isSuccessful) {
            unregisterReceiver(mReceicver);
        }
		setWakeupKeyWordsThreshold(thresholde2);
		//显示语音图标
		//TXZConfigManager.getInstance().showFloatTool(TXZConfigManager.FloatToolType.FLOAT_TOP);
		MediaPlayerSurfaceView.getInstance().currIndex = 0;
		/*TXZConfigManager.getInstance().enableChangeWakeupKeywords(true);
		TXZConfigManager.getInstance().setWakeupKeywordsNew(null);*/
		super.onDestroy();
	}

	public void beginPlayTts() {
		if (TXZConfigManager.getInstance().isInitedSuccess()) {
			TXZTtsManager.getInstance().speakText("欢迎您的到来，我是您的行车服务管家，我叫小踢，专注驾驶，让双手回归方向盘。" + "我可以帮您全局语音发微信，打电话，放音乐，导航至目的地，找加油站，我还可以为您做很多事噢，让我们一起互动体验吧。", new ITtsCallback() {
				@Override
				public void onEnd() {
					super.onEnd();
					beginPlayMedia();
				}
			});
		} else {
			Toast.makeText(this, "语音功能失效", Toast.LENGTH_LONG).show();
		}
	}

	//使用中阈值()
	private static double thresholde = -4.0;
	//退出后还原阈值
	private static double thresholde2 = -3.1;

	public void beginPlayMedia() {
		//加载反馈声音资源
		//VocieUtil.getInstance().loadVoice();
		setWakeupKeyWordsThreshold(thresholde);

		SDKDemoApp.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				main_rel.removeAllViews();
				main_rel.addView(new MediaPlayerSurfaceView(MainActivity.getInstance()));
			}
		}, 500);
	}

	//设置免唤醒词阈值
	private void setWakeupKeyWordsThreshold(double threshold){
		Log.d(TAG, "setWakeupKeyWordsThreshold: ");
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject;
		for (int i = 0;i < CommandPoint.commandArr.length - 1;i++){
			try {
				jsonObject = new JSONObject();
				jsonObject.put("keyWords",CommandPoint.commandArr[i]);
				jsonObject.put("threshold",threshold);
				jsonArray.put(jsonObject);
				//Log.d(TAG, "setWakeupKeyWordsThreshold-----: "+((JSONObject)jsonArray.get(i)).getString("keyWords"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		//Log.d(TAG, "setWakeupKeyWordsThreshold: "+jsonArray.length());
		TXZConfigManager.getInstance().setWakeupKeyWordsThreshold(jsonArray.toString());

	}


	//接收语音界面显示状态的广播
	private class mBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Boolean keyTape = true;
			keyTape = intent.getBooleanExtra("key_type",true);
			switch(action){
				// 语音界面启动
				case "com.txznet.txz.record.show":
					/*if (keyTape){
						voiceState = true;
						MediaPlayerSurfaceView.getInstance().pause();
					}*/
					voiceState = true;
					MediaPlayerSurfaceView.getInstance().pause();
					break;
				// 语音界面消失
				case "com.txznet.txz.record.dismiss":
					voiceState = false;
					MediaPlayerSurfaceView.getInstance().requestAudioFocus();
					//MediaPlayerSurfaceView.getInstance().start();
					/*if (keyTape){
						voiceState = false;
						MediaPlayerSurfaceView.getInstance().start();
						//yunOS保持屏幕常亮
						Intent intent0 = new Intent("com.txznet.txz.record.show");
						intent0.putExtra("key_type",false);
						sendBroadcast(intent0);
					}*/
					break;
				default:
					break;
			}
		}
	}

	private int requestCode = 0;
	//弹窗权限申请
	private void isHasPermission(){
		//8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
			this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);

		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){//6.0申请弹窗权限
			if (ContextCompat.checkSelfPermission(MainActivity.getInstance(),SYSTEM_ALERT_WINDOW)
					!= PackageManager.PERMISSION_GRANTED ){
				ActivityCompat.requestPermissions(MainActivity.getInstance(),new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},requestCode);
			}
		}

	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == this.requestCode){
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "用户拒绝了弹窗授权");
                Toast.makeText(this,"“智能体验”需要获取弹窗权限来完成交互",Toast.LENGTH_SHORT).show();
            }
        }
    }

}