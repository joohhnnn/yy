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
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.manager.NavManager;

public class SetLocationActivity extends BaseActivity {

	public final int LOCATION_HOME = 1;
	public final int LOCATION_NONE = 0;
	public final int LOCATION_COMPANY = 2;

	private int mIntWhere;
	private int from;
	private String mKey;
	private View mBtnSearch;
	private Button mBtnMapChoosePosition;
	private EditText mEtDest;
	private TextView mKeyBoard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.addActivity(this);
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
		if (!TextUtils.isEmpty(mKey)) {
			mEtDest.setText(mKey);
			mEtDest.setSelection(mKey.length());
			mEtDest.requestFocus();
		}
	}

	private void processIntent() {
		Intent intent = getIntent();
		mIntWhere = intent.getIntExtra("where", NavManager.LOCATION_NONE);
		mKey = intent.getStringExtra("key");
		from = intent.getIntExtra("from", 0);
	}

	private void initView() {
		mBtnSearch = findViewById(R.id.btnSearch);
		mBtnMapChoosePosition = (Button) findViewById(R.id.btnMapChoosePosition);
		mEtDest = (EditText) findViewById(R.id.etDest);

		AppLogic.removeUiGroundCallback(observer);
		AppLogic.runOnUiGround(observer, 2000);
	}

	Runnable observer = new Runnable() {

		@Override
		public void run() {
			if (from != 1) {
				return;
			}

			final View view = findViewById(R.id.layout);
			view.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@Override
						public void onGlobalLayout() {
							int height = view.getRootView().getHeight()
									- view.getHeight();
							LogUtil.logd("键盘高度差：" + height);
							if (height < 100) {
								LogUtil.logd("键盘隐藏");
								// setResult(1);
								// finish();
							} else {
								LogUtil.logd("键盘显示");
							}
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
		finish();
	}

	private void hideSoftInput() {
		if (mKeyBoard != null) {
			InputMethodManager imm = (InputMethodManager) AppLogic
					.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mKeyBoard.getWindowToken(), 0);
		}
	}
}
