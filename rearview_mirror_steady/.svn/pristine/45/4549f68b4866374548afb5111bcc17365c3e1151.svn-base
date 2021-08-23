package com.txznet.txz.ui.win.nav;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.util.TXZFileConfigUtil;

public class SearchActivity extends BaseActivity {

	public final static int LOCATION_COMPANY = 2;
	public final static int LOCATION_HOME = 1;
	public final static int LOCATION_NONE = 0;

	private int mIntWhere;
	private int from;
	private String mKey;
	private View mBtnSearch;
	private EditText mEtDest;
	private TextView mKeyBoard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_position);
		initView();
		initOnClick();
		processIntent();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent();
	}

	public static Intent getNoneIntent(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_NONE);
		intent.putExtra("key", keyWord);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	public static Intent getHomeIntent(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_HOME);
		intent.putExtra("key", keyWord);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	public static Intent getCompanyIntent(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_COMPANY);
		intent.putExtra("key", keyWord);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!TextUtils.isEmpty(mKey)) {
			mEtDest.setText(mKey);
			mEtDest.setSelection(mKey.length());
		}
		mEtDest.setFocusable(true);
		mEtDest.setFocusableInTouchMode(true);
		mEtDest.requestFocus();
		showSoftInput();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		LogUtil.logd("onConfigurationChanged -- >");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void processIntent() {
		Intent intent = getIntent();
		mIntWhere = intent.getIntExtra("where", NavManager.LOCATION_NONE);
		mKey = intent.getStringExtra("key");
		from = intent.getIntExtra("from", 0);
	}

	private void initView() {
		mBtnSearch = findViewById(R.id.btnSearch);
		mEtDest = (EditText) findViewById(R.id.etDest);

		AppLogic.removeUiGroundCallback(observer);
		AppLogic.runOnUiGround(observer, 1000);

		if (TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_KEYBOARD_FULL_SCREEN, false)) {
		    int imeOptions = mEtDest.getImeOptions();
		    imeOptions &= ~EditorInfo.IME_FLAG_NO_EXTRACT_UI;
            imeOptions &= ~EditorInfo.IME_FLAG_NO_FULLSCREEN;
		    mEtDest.setImeOptions(imeOptions);
		}
	}

	Runnable observer = new Runnable() {

		@Override
		public void run() {
			if (from != 1) {
				return;
			}

			final View view = findViewById(R.id.layout);
			view.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					int height = view.getRootView().getHeight() - view.getHeight();
					LogUtil.logd("键盘高度差：" + height);
					// boolean isShow = isShowSoftInput();
					// if(!isShow){
					// // 关闭了键盘
					// finish();
					// }
				}
			});
		}
	};

	private void initOnClick() {
		mBtnSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doSearch();
			}
		});

		mEtDest.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mKeyBoard = (TextView) v;
			}
		});

		// 增加点击回车进行搜索
		mEtDest.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// 点击回车，触发搜索点击事件
				if (event != null) {
					LogUtil.loge("KeyEvent:" + event.getAction());
				}
				doSearch();
				return true;
			}
		});

		mEtDest.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (TextUtils.isEmpty(mEtDest.getEditableText().toString())) {
					mBtnSearch.setSelected(false);
				} else {
					mBtnSearch.setSelected(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	private void doSearch() {
		// 隐藏软键盘
		hideSoftInput();

		String strDest = mEtDest.getText().toString();
		if (strDest.equals(""))
			return;

		String city = new String();

		LocationInfo info = LocationManager.getInstance().getLastLocation();
		if (info == null) {
			// city = "深圳市";
		} else {
			if (info.msgGeoInfo != null) {
				city = info.msgGeoInfo.strCity;
			} else {
				city = "深圳市";
			}
		}

		JSONObject json = new JSONObject();
		try {
			json.put("keywords", strDest);
			// json.put("city", city);
			json.put("where", mIntWhere);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}

		NavManager.getInstance().invokeTXZNav("", "inner.poiSearch", json.toString().getBytes());
		// 关闭页面
		finish();
	}

	private void hideSoftInput() {
		if (mKeyBoard != null) {
			InputMethodManager imm = (InputMethodManager) GlobalContext.get()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mKeyBoard.getWindowToken(), 0);
		}
	}

	private void showSoftInput() {
		InputMethodManager imm = (InputMethodManager) GlobalContext.get()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mEtDest, 0);
	}

	private boolean isShowSoftInput() {
		int mode = getWindow().getAttributes().softInputMode;
		LogUtil.logd("SoftInputMode:" + mode);
		return false;
	}
}