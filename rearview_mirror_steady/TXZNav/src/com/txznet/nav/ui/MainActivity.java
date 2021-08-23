package com.txznet.nav.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.NavService;
import com.txznet.nav.R;

public class MainActivity extends BaseActivity implements View.OnClickListener {

	private EditText mEtDest;
	private View mBtnSearch;
	private TextView mKeyBoard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBtnSearch = findViewById(R.id.btnSearch);
		mBtnSearch.setOnClickListener(this);
		findViewById(R.id.btnCheckLocation).setOnClickListener(this);
		findViewById(R.id.btnNavHistory).setOnClickListener(this);
		findViewById(R.id.btnOfflineMap).setOnClickListener(this);

		mEtDest = (EditText) findViewById(R.id.etDest);
		// 增加点击回车进行搜索
		mEtDest.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				mKeyBoard = (TextView) v;
			}
		});

		mEtDest.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// 点击回车，触发搜索点击事件
				doSearch();
				return true;
			}
		});

		mEtDest.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (TextUtils.isEmpty(mEtDest.getEditableText().toString())) {
					mBtnSearch.setSelected(false);
				} else {
					mBtnSearch.setSelected(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		Intent intent = null;
		switch (id) {
		case R.id.btnSearch:
			doSearch();
			break;
		case R.id.btnCheckLocation:
			intent = new Intent(this, CheckMapActivity.class);
			break;
		case R.id.btnNavHistory:
			intent = new Intent(this, HistoryActivity.class);
			break;
		case R.id.btnOfflineMap:
			intent = new Intent(this, OfflineActivity.class);
			break;
		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

	private void doSearch() {
		// 隐藏软键盘
		hideSoftInput();
		if (mEtDest == null)
			return;

		String strDest = mEtDest.getText().toString();
		if (strDest == null || strDest.equals(""))
			return;

		try {
			NavManager
					.getInstance()
					.startSearch(
							strDest,
							NavManager.getInstance().getLocationInfo().msgGeoInfo.strCity,
							true, NavManager.LOCATION_NONE);
		} catch (Exception e) {
			LogUtil.loge("获取当前位置为空，正在重新获取!");
			NavManager.getInstance().quickLocation(false);
		}
	}

	private void hideSoftInput() {
		if (mKeyBoard != null) {
			InputMethodManager imm = (InputMethodManager) MyApplication
					.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mKeyBoard.getWindowToken(), 0);
		}
	}
}
