package com.txznet.reserve.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.tts.TtsManager;

public class ReserveSingleTaskActivity2 extends BaseActivity {
	public static final String KEY_EXTRA = "extra";
	public static final String KEY_URI = "uri";
	public static final String SPLIT_CHAR = ";";
	public static final String KEY_HEADER = "headers";

	private final static String TAG = "ResSingleTaskActivity::";
	
	private boolean loadError = false;
	private boolean bNeedClearHistory = false;
	
	public static void showWeb(Context context, String url, String headerParams) {
		Intent intent = new Intent(context, ReserveSingleTaskActivity2.class);
		intent.putExtra(KEY_EXTRA, url);
		intent.putExtra(KEY_HEADER, headerParams);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	private WebView mWebView;
	private ImageView mImageView;
	private AnimationDrawable mAnimDrawable;
	private RelativeLayout mRlError;
	private TextView mTvRefresh, mTvBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
		setContentView(R.layout.web_view_sim);
		initWebView();
		initWithIntent();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if(mImageView != null){
			mImageView.setVisibility(View.VISIBLE);
		}
		if(mWebView != null){
			bNeedClearHistory = true;
		}
		setIntent(intent);
		initWithIntent();

	}
	
	@Override
	protected void onResume() {

		super.onResume();
	}
	

	@Override
	protected void onDestroy() {
		if(mWebView != null){
			mWebView.destroy();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		if(mAnimDrawable != null){
			mAnimDrawable.stop();
		}
		super.onStop();
	}
	
	@Override
	public void onGetFocus() {
		mAnimDrawable.start();
		super.onGetFocus();
	}
	
	private void initWebView() {
		mWebView = (WebView) findViewById(R.id.web_view_sim);
		mImageView = (ImageView) findViewById(R.id.iv_web_sim);
		mRlError = (RelativeLayout) findViewById(R.id.rl_web_sim_error);
		mTvBack = (TextView) findViewById(R.id.tv_web_sim_back);
		mTvRefresh = (TextView) findViewById(R.id.tv_web_sim_refresh);
		mImageView.setVisibility(View.VISIBLE);
		mAnimDrawable = (AnimationDrawable) mImageView.getDrawable();
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new Cross(), "cross");
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.setBackgroundColor(Color.BLACK);
		if (Build.VERSION.SDK_INT >= 19) {// 硬件加速器的使用
			mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		mWebView.getSettings().setUserAgentString(
				"Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
		mWebView.getSettings().setUserAgentString(
				"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Mobile Safari/537.36");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView arg0, String arg1) {
				return false;
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
			        String description, String failingUrl) {
			    loadError = true;
			}

            @Override
            public void onPageFinished(WebView view, String url) {
                if(loadError){
                    AppLogic.removeBackGroundCallback(runnableLoadingTimeout);
                    mRlError.setVisibility(View.VISIBLE);
                    LogUtil.logd(TAG + "error page show");
                    loadError = false;
                }
                if(bNeedClearHistory){
                	LogUtil.logd(TAG + "before history size = " + mWebView.copyBackForwardList().getSize());
                	mWebView.clearHistory();
                	LogUtil.logd(TAG + "after history size = " + mWebView.copyBackForwardList().getSize());
                	bNeedClearHistory = false;
                }
            }
		});
		
		mWebView.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		
		mWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //判断标题 title 中是否包含有“error”字段，如果包含“error”字段，则设置加载失败，显示加载失败的视图
                if(!TextUtils.isEmpty(title)&&title.toLowerCase().contains("error")){
                    loadError = true;
                }
            }
		    
		});
		
		mTvBack.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mRlError.setVisibility(View.GONE);
                if(!mWebView.canGoBack()){
                    finish();
                }else{
                    mWebView.goBack();
                }
                LogUtil.logd(TAG + "error page back click");
            }
        });
		
		mTvRefresh.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mWebView.reload();
                mRlError.setVisibility(View.GONE);
                if(mImageView != null){
					mImageView.setVisibility(View.VISIBLE);
				}
				if(mAnimDrawable != null){
					mAnimDrawable.start();
				}
                LogUtil.logd(TAG + "error page refresh click");
            }
        });
		
	}
	
	Runnable runnableLoadingTimeout = new Runnable() {
        
        @Override
        public void run() {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    LogUtil.logd(TAG + "Loading view outtime.");
                    if (mAnimDrawable != null) {
                        mAnimDrawable.stop();
                    }
                    if (mImageView != null) {
                        mImageView.setVisibility(View.GONE);
                    }
                    if(mRlError != null){
                    	mRlError.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    };

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void initWithIntent() {
		Intent intent = getIntent();
		if (intent == null) {
			this.finish();
			return;
		}
		
		String uri = intent.getStringExtra(KEY_EXTRA);
		if (TextUtils.isEmpty(uri)) {
			this.finish();
			return;
		}
		Map<String, String> heads = new HashMap<String, String>();
		String headParam = intent.getStringExtra(KEY_HEADER);
		if (!TextUtils.isEmpty(headParam)) {
			try {
				JSONObject jsonObject = new JSONObject(headParam);
				Iterator<String> keyIterator = jsonObject.keys();
				while (keyIterator.hasNext()) {
					String key = keyIterator.next();
					heads.put(key, jsonObject.optString(key));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		try {
			showUrl(uri, heads);
		} catch (Exception e) {
			LogUtil.logw(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void showUrl(String uri, Map<String, String> headers) {
	    loadError = false;
		if (!uri.contains("http") && !uri.startsWith("file://")) {
			uri = "file:///" + uri;
		}
		LogUtil.logd("showUrl:" + uri);
		mWebView.loadUrl("javascript:localStorage.clear()");
		if (headers != null && headers.size() > 0) {
			mWebView.loadUrl(uri, headers);
		} else {
			mWebView.loadUrl(uri);
		}
		AppLogic.runOnBackGround(runnableLoadingTimeout, 30 * 1000);
	}
	
	public class Cross{
    	@JavascriptInterface
    	public void speakText(String text){
    		LogUtil.logd(TAG+"speakText = "+text);
    		if(text != null && text.contains("M")){
    			text = text.replace("M", "兆");
    		}
    		TtsManager.getInstance().speakText(text);
    	}
    	
    	@JavascriptInterface
    	public void closeWindow(){
    		LogUtil.logd(TAG+"closeWindow");
    		finish();
    	}
    	
    	@JavascriptInterface
    	public void time(String time){
    	    LogUtil.logd(TAG+"LoadingTime = "+time);
    	}
    	
    	@JavascriptInterface
    	public void hide(){
    	    LogUtil.logd(TAG+"hide");
    		if(mAnimDrawable != null){
				mAnimDrawable.stop();
			}
    		AppLogic.runOnUiGround(new Runnable() {
                
                @Override
                public void run() {
                    mImageView.setVisibility(View.GONE);
                }
            });
    	    AppLogic.removeBackGroundCallback(runnableLoadingTimeout);
    	}
    	
    	@JavascriptInterface
    	public void log(String s){
    		LogUtil.logd(TAG+s);
    	}
    	
    }
}