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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.R;

public class SetLocationActivity extends BaseActivity {

	public final int LOCATION_COMPANY = 2;
	public final int LOCATION_HOME = 1;
	public final int LOCATION_NONE = 0;

	private int mIntWhere;
	private View mBtnSearch;
	private Button mBtnMapChoosePosition;
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

	@Override
	protected void onStart() {
		super.onStart();
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
	}

	private void initView() {
		mBtnSearch = findViewById(R.id.btnSearch);
		mBtnMapChoosePosition = (Button) findViewById(R.id.btnMapChoosePosition);
		mEtDest = (EditText) findViewById(R.id.etDest);
	}

	private void initOnClick() {
		mBtnSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				doSearch();
			}
		});

		mBtnMapChoosePosition.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				NavManager.getInstance().startCheckMap(mIntWhere);
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

	private void doSearch() {
		// 隐藏软键盘
		hideSoftInput();

		String strDest = mEtDest.getText().toString();
		if (strDest.equals(""))
			return;

		String city = new String();
		if (NavManager.getInstance().getLocationInfo().msgGeoInfo != null) {
			city = NavManager.getInstance().getLocationInfo().msgGeoInfo.strCity;
		} else {
			city = "深圳市";
		}

		NavManager.getInstance().startSearch(strDest, city, true, mIntWhere);
	}

	private void hideSoftInput() {
		if (mKeyBoard != null) {
			InputMethodManager imm = (InputMethodManager) MyApplication
					.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mKeyBoard.getWindowToken(), 0);
		}
	}
}
