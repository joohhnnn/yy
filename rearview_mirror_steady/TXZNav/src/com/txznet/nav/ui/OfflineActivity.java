package com.txznet.nav.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.baidu.navisdk.ui.download.BNDownloadNotifyManager;
import com.baidu.navisdk.ui.download.BNDownloadUIManager;
import com.txznet.nav.R;

public class OfflineActivity extends BaseActivity {

	private BNDownloadUIManager mDownloadUIManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initDownloadUIManager();
		initNotification();
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

	private void initDownloadUIManager() {
		if (null == mDownloadUIManager) {
			mDownloadUIManager = new BNDownloadUIManager(this);
			View v = mDownloadUIManager.createView(this);
			// 不设置该listener时，不显示返回按钮
			mDownloadUIManager
					.setBackBtnOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onBackPressed();
						}
					});
			setContentView(v);
		}
	}

	private void initNotification() {
		// 设置导航下载中Notification绑定的intent
		Intent intent = new Intent(this, this.getClass());

		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.activity_offline_download_notify_progress);
		BNDownloadNotifyManager.getInstance().init(this, intent,
				R.drawable.ic_launcher, contentView, R.id.title,
				R.id.progress_bar, R.id.progress_text);
	}

}
