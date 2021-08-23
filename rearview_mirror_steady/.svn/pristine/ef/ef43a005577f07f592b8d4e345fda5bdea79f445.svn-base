package com.txznet.txz.ui.win.nav;

import org.json.JSONObject;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchEditDialog extends WinDialog {

	private SearchEditDialog() {
		super(true);
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3);
	}

	@Override
	protected View createView() {
		View view = LayoutInflater.from(getContext()).inflate(R.layout.activity_set_position, null);
		findWidget(view);
		return view;
	}

	public final static int LOCATION_COMPANY = 2;
	public final static int LOCATION_HOME = 1;
	public final static int LOCATION_NONE = 0;

	private int mIntWhere;
	private int from;
	private String mKey;
	private View mBtnSearch;
	private EditText mEtDest;
	private TextView mKeyBoard;

	private volatile boolean mNeedCloseDialog = true;

	private static SearchEditDialog mDialog;

	public static SearchEditDialog getInstance() {
		if (mDialog == null) {
			synchronized (SearchEditDialog.class) {
				if (mDialog == null) {
					mDialog = new SearchEditDialog();
				}
			}
		}
		return mDialog;
	}

	public static void naviDefault(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_NONE);
		intent.putExtra("key", keyWord);
		SearchEditDialog.getInstance().processIntent(intent);
		SearchEditDialog.getInstance().show();
	}

	public static void naviHome(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_HOME);
		intent.putExtra("key", keyWord);
		SearchEditDialog.getInstance().processIntent(intent);
		SearchEditDialog.getInstance().show();
	}

	public static void naviCompany(Context context, String keyWord) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("where", LOCATION_COMPANY);
		intent.putExtra("key", keyWord);
		SearchEditDialog.getInstance().processIntent(intent);
		SearchEditDialog.getInstance().show();
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

	public void processIntent(Intent intent) {
		Intent mIntent = intent;
		mIntWhere = mIntent.getIntExtra("where", NavManager.LOCATION_NONE);
		mKey = mIntent.getStringExtra("key");
		from = mIntent.getIntExtra("from", 0);
	}

	private void findWidget(View view) {
		mBtnSearch = view.findViewById(R.id.btnSearch);
		mEtDest = (EditText) view.findViewById(R.id.etDest);
		initOnClick();

		AppLogic.removeUiGroundCallback(observer);
		AppLogic.runOnUiGround(observer, 1000);
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
			city = "深圳市";
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

		mNeedCloseDialog = true;
		NavManager.getInstance().mSearchByEdit = true;
		SelectorHelper.clearIsSelecting();
		dismiss();
		NavManager.getInstance().invokeTXZNav(null, "inner.poiSearch", json.toString().getBytes());
		// 关闭页面
	}

	public void setNeedCloseDialog(boolean isClose) {
		this.mNeedCloseDialog = isClose;
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
		imm.showSoftInput(mEtDest, InputMethodManager.SHOW_FORCED);
	}

	@Override
	public void dismiss() {
		if (!mNeedCloseDialog) {
			return;
		}

		if (isShowing()) {
			AppLogic.removeUiGroundCallback(mDismissRunnable);
			mNeedCloseDialog = true;
			super.dismiss();
		}
	}

	@Override
	protected void onGetFocus() {
		checkForDismiss();
		super.onGetFocus();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		checkForDismiss();
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		SelectorHelper.backAsrWithCancel();
		mNeedCloseDialog = true;
		dismiss();
	}

	private void checkForDismiss() {
		AppLogic.removeUiGroundCallback(mDismissRunnable);
		// AppLogic.runOnUiGround(mDismissRunnable, 30000);
	}

	Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {

			mNeedCloseDialog = true;
			hideSoftInput();
			dismiss();
		}
	};
}