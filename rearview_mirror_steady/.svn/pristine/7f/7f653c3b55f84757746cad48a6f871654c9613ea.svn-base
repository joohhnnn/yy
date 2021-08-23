package com.txznet.nav.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.amap.api.navi.AMapNavi;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.loader.AppLogic;
import com.txznet.nav.NavService;
import com.txznet.nav.R;
import com.txznet.nav.helper.NavInfoCache;
import com.txznet.nav.manager.NavManager;

public class MainActivity extends BaseActivity implements OnClickListener {

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
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// 点击回车，触发搜索点击事件
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

		checkNavCache();
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
			NavManager.getInstance().startSearch(strDest, null, true, NavManager.LOCATION_NONE);
		} catch (Exception e) {
			LogUtil.loge("获取当前位置为空，正在重新获取!");
			NavService.getInstance().quickLocation();
		}
	}

	private void hideSoftInput() {
		if (mKeyBoard != null) {
			InputMethodManager imm = (InputMethodManager) AppLogic.getApp()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mKeyBoard.getWindowToken(), 0);
		}
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
			intent = new Intent(this, DownloadActivity.class);
			break;
		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

	@Override
	public void onBackPressed() {
		ActivityStack.getInstance().exit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AMapNavi.getInstance(AppLogic.getApp()).destroy();
	}

	public void checkNavCache() {
		int distance = NavInfoCache.getInstance().getDistance();
		LogUtil.logd("remain distance:" + distance);
		if (distance < 500 && distance > 0) {
			return;
		}

		if (NavInfoCache.getInstance().hasCache()) {
			final NavigateInfo info = NavInfoCache.getInstance().getCache();
			if (info == null) {
				return;
			}
			String hint = null;
			if (!TextUtils.isEmpty(info.strTargetName) && !info.strTargetName.equals("地图选点")) {
				hint = "是否恢复上次导航？\n\n目的地：" + info.strTargetName;
			} else {
				if (TextUtils.isEmpty(info.strTargetAddress)) {
					hint = "是否恢复上次导航？\n\n目的地：地图选点";
				} else {
					hint = "是否恢复上次导航？\n\n目的地：" + info.strTargetAddress;
				}
			}
			new WinConfirmAsr() {
				@Override
				public void onClickOk() {
					NavInfoCache.getInstance().reset();
					NavManager.getInstance().NavigateTo(info);
				}

				@Override
				public void onSpeakOk() {
					this.dismiss();
					TtsUtil.speakText("好的，即将为您重新规划路径", new ITtsCallback() {
						public void onEnd() {
							onClickOk();
						};
					});
				};

				@Override
				public void onClickCancel() {
					super.onClickCancel();
					NavInfoCache.getInstance().reset();
				};

				@Override
				public void onSpeakCancel() {
					super.onSpeakCancel();
					NavInfoCache.getInstance().reset();
				};
			}.setMessage(hint).setHintTts(hint).setSureText("恢复", new String[] { "恢复", "确定", "是" })
					.setCancelText("取消", new String[] { "取消", "放弃", "返回" }).show();
		}
	}
}