package com.txznet.txz.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.activity.ReserveSingleTaskActivity2;
import com.txznet.txz.R;
import com.txznet.txz.module.tts.TtsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimWebDialog extends WinDialog{

	private WebView mWebView;
	private ImageView mImageView;
	private AnimationDrawable mAnimDrawable;
	private RelativeLayout mRlError;
	private TextView mTvRefresh, mTvBack;

	private final static String TAG = "SimWebDialog::";

	private boolean loadError = false;
	private boolean bNeedClearHistory = false;

	private String mUrl;
	private String mParams;

	private static SimWebDialog mSimWebDialog;

	private SimWebDialog(DialogBuildData buildData) {
		super(buildData);
	}

	public static void showWeb(final Context context, final String url, final String headerParams) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if(mSimWebDialog == null){
					mSimWebDialog = new SimWebDialog(new DialogBuildData().setFullScreen(true)
							.setContext(context));
				}
				mSimWebDialog.setUrl(url);
				mSimWebDialog.setParams(headerParams);
				if(mSimWebDialog.isShowing()){
					mSimWebDialog.refreshView();
				}else{
					mSimWebDialog.show();
				}
			}
		}, 0);

	}

	public static void closeWebDialog(String strRes){
		LogUtil.logd(TAG + "closeWebDialog strRes = " + strRes);
		if(mSimWebDialog != null && mSimWebDialog.isShowing()){
			mSimWebDialog.dismiss(strRes);
		}
	}

	private void refreshView() {
		LogUtil.logd(TAG + "refreshView");
		if(mImageView != null){
			mImageView.setVisibility(View.VISIBLE);
		}
		if(mWebView != null){
			bNeedClearHistory = true;
		}
		initWithIntent();
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = super.mDialog.getLayoutInflater();
		mView = inflater.inflate(R.layout.web_view_sim, null, false);
		return mView;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}



	private void initWithIntent() {
		if (TextUtils.isEmpty(mUrl)) {
			dismiss("empty url");
			return;
		}
		Map<String, String> heads = new HashMap<String, String>();
		if (!TextUtils.isEmpty(mParams)) {
			try {
				JSONObject jsonObject = new JSONObject(mParams);
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
			showUrl(mUrl, heads);
		} catch (Exception e) {
			LogUtil.logw(e.getMessage());
			e.printStackTrace();
		}
	}

	public void setUrl(String url){
		mUrl = url;
	}

	public void setParams(String params){
		mParams = params;
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

	private void initWebView() {
		mWebView = (WebView) mView.findViewById(R.id.web_view_sim);
		mImageView = (ImageView) mView.findViewById(R.id.iv_web_sim);
		mRlError = (RelativeLayout) mView.findViewById(R.id.rl_web_sim_error);
		mTvBack = (TextView) mView.findViewById(R.id.tv_web_sim_back);
		mTvRefresh = (TextView) mView.findViewById(R.id.tv_web_sim_refresh);
		mImageView.setVisibility(View.VISIBLE);
		mAnimDrawable = (AnimationDrawable) mImageView.getDrawable();

		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.addJavascriptInterface(new SimWebDialog.Cross(), "cross");
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

		mWebView.setOnLongClickListener(new View.OnLongClickListener() {

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

		mTvBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mRlError.setVisibility(View.GONE);
				if(!mWebView.canGoBack()){
					dismiss("click");
				}else{
					mWebView.goBack();
				}
				LogUtil.logd(TAG + "error page back click");
			}
		});

		mTvRefresh.setOnClickListener(new View.OnClickListener() {

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

		GlobalObservableSupport.getHomeObservable().registerObserver(mHomeBoserver);
	}

	HomeObservable.HomeObserver mHomeBoserver = new HomeObservable.HomeObserver() {
		@Override
		public void onHomePressed() {
			LogUtil.logd(TAG + "Home pressed");
			dismiss("home click");
		}
	};

	@Override
	protected void onStart() {
		initWebView();
		super.onStart();
	}

	@Override
	protected void onShow() {
		LogUtil.logd(TAG + "onShow");
		initWithIntent();
		super.onShow();
	}

	@Override
	protected void onGetFocus() {
		mAnimDrawable.start();
		super.onGetFocus();
	}

	@Override
	protected void onStop() {
		if(mAnimDrawable != null){
			mAnimDrawable.stop();
		}
		super.onStop();
	}

	@Override
	protected void onDismiss() {
		LogUtil.logd(TAG + "onDismiss");
		if(mWebView != null){
			mWebView.destroy();
		}
		super.onDismiss();
		mSimWebDialog = null;
		GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeBoserver);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
			mWebView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public String getReportDialogId() {
		return "sim_web";
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
			dismiss("web exceed");
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
