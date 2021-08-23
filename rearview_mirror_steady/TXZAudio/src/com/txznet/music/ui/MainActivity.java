package com.txznet.music.ui;

import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.HomepageFragment;
import com.txznet.music.utils.JumpUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.widget.MusicMoveView;
import com.txznet.music.widget.ShadeImageView;

public class MainActivity extends BaseActivity implements OnClickListener {

	private com.txznet.music.widget.MusicMoveView mMusicMoveView1;
	private FragmentTransaction transaction;
	private FragmentManager fragmentManager;
	private TextView title;
	private ShadeImageView iv_back;
	private BroadcastReceiver br;
	private RelativeLayout rlMusic;

	private void bindViews() {
		mMusicMoveView1 = (MusicMoveView) findViewById(R.id.musicMoveView1);
		rlMusic = (RelativeLayout) findViewById(R.id.rl_music);
		title = (TextView) findViewById(R.id.title);
		iv_back = (ShadeImageView) findViewById(R.id.back);
	}

	@Override
	public void onAttachedToWindow() {
		LogUtil.logd("onAttachedToWindow[" + this.hashCode() + "]");
		super.onAttachedToWindow();
	}

	public void onAttachFragment(android.app.Fragment fragment) {
		LogUtil.logd("onAttachFragment[" + this.hashCode() + "]");
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.logi("life::onCreate[" + this.hashCode() + "]");
		setContentView(R.layout.activity_main);

		bindViews();
		initData();
		initListener();
		// 去获取最新的端口号，避免端口号的占用
		// MediaPlayerActivityEngine.getInstance().refreshCategoryList();
	}

	/**
	 * 注册监听事件
	 */
	private void initListener() {
		iv_back.setOnClickListener(this);
		fragmentManager
				.addOnBackStackChangedListener(new OnBackStackChangedListener() {

					@Override
					public void onBackStackChanged() {
						String name;
						try {
							BackStackEntry stackTop = fragmentManager
									.getBackStackEntryAt(fragmentManager
											.getBackStackEntryCount() - 1);
							name = stackTop.getName();
							FragmentTransaction transaction = fragmentManager
									.beginTransaction();
							transaction.show(fragmentManager
									.findFragmentByTag(name));
							transaction.commitAllowingStateLoss();
							setBackVisible(true);

						} catch (Exception e) {
							name = getString(R.string.app_name);
							setBackVisible(false);
						}
						LogUtil.logd(TAG + "title:" + name);
						setAppTitle(name);// 设置页头
					}
				});
		rlMusic.setOnClickListener(this);
		if (Constant.ISTEST) {
			title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					MainActivity.this.startActivity(new Intent(GlobalContext
							.get(), TestActivity.class));
				}
			});
		}
	}

	public void setBackVisible(boolean visible) {
		if (visible) {
			iv_back.setVisibility(View.VISIBLE);
		} else {
			iv_back.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onResume() {
		LogUtil.logi("life::onResume" + this.hashCode());
		isShowButton();
		super.onResume();
	}

	/**
	 * 是否显示音乐滚动条
	 */
	public void isShowButton() {
		// 从配置文件中获取，是否播放过文件

		boolean object = SharedPreferencesUtils.isFirst();
		if (object) {
			rlMusic.setVisibility(View.GONE);
		} else {
			rlMusic.setVisibility(View.VISIBLE);
			if (MediaPlayerActivityEngine.getInstance().isPlaying()) {
				mMusicMoveView1.start();
			} else {
				mMusicMoveView1.stop();
			}
		}
	}

	private void initData() {

		fragmentManager = getFragmentManager();
		transaction = fragmentManager.beginTransaction();
		transaction.replace(R.id.content, new HomepageFragment(),
				getString(R.string.app_name));
		transaction.commitAllowingStateLoss();
		br = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction() == Constant.ACTION_SHOW_BUTTON) {
					if (rlMusic.getVisibility() == View.GONE) {// 只有当它是gong的时候才去响应
						isShowButton();
					} else {
						if (SharedPreferencesUtils.isFirst()) {
							rlMusic.setVisibility(View.GONE);
						}
					}
				} else if (intent.getAction() == Constant.ACTION_MUSIC_PAUSE) {
					if (intent.getExtras().getBoolean(
							Constant.PARAM_PAUSE_OR_NOT)) {
						mMusicMoveView1.start();
					} else {
						mMusicMoveView1.stop();
					}
				} else if (intent.getAction() == Constant.ACTION_MAIN_FINISH) {
					MainActivity.this.finish();
				}
			}
		};

		IntentFilter filter = new IntentFilter(Constant.ACTION_SHOW_BUTTON);
		filter.addAction(Constant.ACTION_MUSIC_PAUSE);
		filter.addAction(Constant.ACTION_MAIN_FINISH);

		LocalBroadcastManager.getInstance(getApplicationContext())
				.registerReceiver(br, filter);
	}

	public void setAppTitle(String name) {
		title.setText(name);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		LogUtil.logd("onSaveInstanceState[" + this.hashCode() + "]");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		LogUtil.logd("onRestoreInstanceState[" + this.hashCode() + "]");
	}

	@Override
	protected void onDestroy() {
		LogUtil.logi("life::onDestroy[" + this.hashCode() + "]");
		// ToastUtils.showLong("you are die");
		LocalBroadcastManager.getInstance(getApplicationContext())
				.unregisterReceiver(br);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_music:
			JumpUtils.jumpFrom(GlobalContext.get(), MediaPlayerActivity.class,
					Constant.TYPE_BUTTON);
			break;
		case R.id.back:
			onBackPressed();
			break;
		}
	}

	@Override
	protected void onStart() {
		LogUtil.logi("life::onStart[" + this.hashCode() + "]");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		LogUtil.logi("life::onRestart[" + this.hashCode() + "]");
		super.onRestart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& getResources().getString(R.string.app_name).equals(
						title.getText())) {
			// android.os.Process.killProcess(Process.myPid());
			// 相当于点击主屏就可以了
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addCategory(Intent.CATEGORY_HOME);
			startActivity(i);
			// onPause();
			// onStop();
			//
			// MainActivity.this.finish();
			// ActivityStack.getInstance().exit();
		}
		return super.onKeyDown(keyCode, event);
	}
}
