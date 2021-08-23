package com.txznet.reserve.activity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.R;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ReserveSingleTaskActivity1 extends BaseActivity {
	public static final String KEY_EXTRA = "extra";
	public static final String KEY_HEADER = "headers";
	
	public static void showTicket(Context context, String url, String headerParams) {
		Intent intent = new Intent(context, ReserveSingleTaskActivity1.class);
		intent.putExtra(KEY_EXTRA, url);
		intent.putExtra(KEY_HEADER, headerParams);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		//长风知豆要求发送此广播以兼容双屏
		context.sendBroadcast(new Intent("com.txznet.txz.ReserveSingleTaskActivity1.onCreate"));
	}
	
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.web_view_ly);
		initWebView();
		initWithIntent();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		initWithIntent();
		LogUtil.logd("onNewIntent");
	}
	
	@Override
	protected void onResume() {
		LogUtil.logd("onResume");
		super.onResume();
	}
	
	private void initWebView() {
		mWebView = (WebView) findViewById(R.id.web_view);
		mWebView.getSettings().setTextZoom(120);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
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
				try {
					arg0.loadUrl(arg1);
				} catch (Exception e) {
					LogUtil.logw(e.getMessage());
					e.printStackTrace();
				}
				return true;
			}
		});
		mWebView.setWebChromeClient(new WebChromeClient(){
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if (newProgress <= 1 || newProgress >= 99) {
					LogUtil.logd("onProgressChanged:" + newProgress);
				}
			}
		});
	}

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
		if (!uri.contains("http") && !uri.startsWith("file://")) {
			uri = "file://" + uri;
		}
		LogUtil.logd("showUrl:" + uri);
		mWebView.loadUrl("javascript:localStorage.clear()");
		if (headers != null && headers.size() > 0) {
			mWebView.loadUrl(uri, headers);
		} else {
			mWebView.loadUrl(uri);
		}
	}

	@Override
	protected void onDestroy() {
		//长风知豆要求发送此广播以兼容双屏
		this.sendBroadcast(new Intent("com.txznet.txz.ReserveSingleTaskActivity1.onDestroy"));
		super.onDestroy();
	}
}