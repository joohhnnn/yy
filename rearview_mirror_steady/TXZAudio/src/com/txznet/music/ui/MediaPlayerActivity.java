package com.txznet.music.ui;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.ItemPlayerListAdapter;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.utils.AudioManagerHelper;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Tool;
import com.txznet.music.utils.Utils;
import com.txznet.music.view.IMediaPlayer;
import com.txznet.music.widget.CustomSeekBar;

/**
 * 测试代码。将ViewPager改为LinearLayout 02-20 14:56:56.158: I/Choreographer(2904):
 * Skipped 99 frames! The application may be doing too much work on its main
 * thread.
 * 
 * @author telenewbie
 * @version 2016年2月20日
 */
public class MediaPlayerActivity extends BaseActivity implements OnClickListener, IMediaPlayer, Observer {

	private String TAG = "[MUSIC][MediaPlayerAct]";
	// Content View Elements
	private TextView mTv_source, mTv_end_time, mTv_current_time, mTvSoundValue;
	private com.txznet.music.widget.ShadeImageView mIv_close, mIv_play_mode, mIv_prev, mIv_play, mIv_next,
			mIv_play_list;
	private ImageView ivBufferBtn, ivRount;
	// private ViewPager vpMediaPlayer;
	private CustomSeekBar seekbar;
	private RotateAnimation animation1;

	private View popContent;
	private PopupWindow showPop;
	private PullToRefreshListView listView;
	private ListView lvListView;

	private ItemPlayerListAdapter adapter;
	// private ItemPlayerPageAdapter pageAdapter;

	private MediaPlayerActivityEngine engine;
	private TextView tvSongTitle;
	private TextView tvSingler;
	private TextView tvLoading;
	private LinearLayout llConetent;
	private BroadcastReceiver br;
	private long startTime = 0;
	protected int startY = 0;
	private static final int SHOWPOPINT = 1;
	private static final int DISMISSPOPINT = 2;
	protected static final int CHANGESOUND = 3;
	private View soundView;
	private PopupWindow soundPopView;

	private static final int defaultClickGap = 1000;

	private View flContent;
	private TextView tvSound;
	private PopupWindow mSoundTips;
	private View mTipSoundView;
	private WinDialog winDialog;

	@Override
	public boolean onNavigateUp() {
		LogUtil.logd("onNavigateUp");
		return super.onNavigateUp();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LogUtil.logd(TAG + "onCreate:" + this.hashCode());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_music_player_layout);
		bindViews();
		initListener();
		initData();
		initPopView();
		ObserverManage.getObserver().addObserver(this);
	}

	@Override
	protected void onPause() {
		LogUtil.logd(TAG + "onPause:" + this.hashCode());
		dismissPopupWindow();
		super.onPause();
	}

	@Override
	protected void onStart() {
		LogUtil.logd(TAG + "onStart:" + this.hashCode());

		if (SharedPreferencesUtils.getShouldTip() && !SharedPreferencesUtils.isCloseVolume()) {
			SharedPreferencesUtils.setShouldTip(false);
			dialog = new Dialog(this, R.style.gt_dialog);
			dialog.setCancelable(true);
			dialog.setContentView(R.layout.tip_sound);
			dialog.show();
			AppLogic.runOnSlowGround(new Runnable() {

				@Override
				public void run() {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
						dialog = null;
					}
				}
			}, 6000);
		}

		// initState();
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		LogUtil.logd(TAG + "onDestroy:" + this.hashCode());
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(br);
		br = null;

		try {
			Intent intent = new Intent("com.txznet.music.Action.doing");
			intent.putExtra("foreground", false);
			GlobalContext.get().sendBroadcast(intent);
		} catch (Exception e) {
			LogUtil.loge("send broadCast[com.txznet.music.Action.doing] have error");
		}
		if (mSoundTips != null && mSoundTips.isShowing()) {
			mSoundTips.dismiss();
		}

		super.onDestroy();
	}

	@Override
	protected void onStop() {
		LogUtil.logd(TAG + "onStop:" + this.hashCode());
		// 解决快速点击导致Engine中的actView为空
		// engine.setListener(null);
		// engine = null;
		super.onStop();
	}

	/**
	 * 初始化状态
	 */
	private void initState() {
		engine.init();
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		animation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation1.setRepeatMode(Animation.INFINITE);
		animation1.setRepeatCount(-1);
		animation1.setDuration(3000);

		// pageAdapter = new ItemPlayerPageAdapter(GlobalContext.get());
		// vpMediaPlayer.setAdapter(pageAdapter);
		if (null == br) {
			br = new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					MediaPlayerActivity.this.finish();
				}
			};
			IntentFilter intent = new IntentFilter(Constant.ACTION_MEDIA_FINISH);
			LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(br, intent);
		}

		if (null == engine) {
			engine = MediaPlayerActivityEngine.getInstance();
			engine.setListener(this);
		}
		Intent intent = getIntent();
		if (intent.hasExtra(Constant.PAGENAMEEXTRA)) {// 手动点击专辑
			LogUtil.logd(TAG + "has page name extra");
			Album album = (Album) intent.getSerializableExtra(Constant.PAGENAMEEXTRA);// 1100000000134
			// if (!Utils.isSong(album.getSid())) {
			// engine.setCurrentAlbumName(album.getName());
			// // engine.changeMode(PlayMode.SEQUENCE.ordinal());// 电台默认进来为顺序循环
			// // 不可点击
			// // hiddenMode(true);
			// }
			if (MediaPlayerActivityEngine.getInstance().getCurrentAlbum() != album.getId()) {
				notifyMusicInfo(null, false);
				engine.stop();

				// ReqAlbumAudio reqData = new ReqAlbumAudio();
				// reqData.setSid(album.getSid());
				// reqData.setId(album.getId());
				// reqData.setCategoryId(MusicFragment.getInstance()
				// .getCategoryId());
				// reqData.setOffset(Constant.PAGECOUNT);
				// if (NetHelp.sendRequest(Constant.GET_ALBUM_AUDIO, reqData) ==
				// 0) {
				// // 当前无网络
				// }
				RequestHelpe.reqAudio(album, album.getCategoryID());
			} else {
				// initState();
			}
			//解决bug：TXZ-5019
			intent.removeExtra(Constant.PAGENAMEEXTRA);
		} else {
			LogUtil.logd(TAG + "didn't has extra or is playing");
			// initState();
		}
	}

	public void initPopView() {
		Tool.init(this);// 用于初始化Tool
		// 初始化弹出框视图
		popContent = View.inflate(getApplicationContext(), R.layout.pop_play_list1, null);
		LinearLayout.LayoutParams params = new LayoutParams(AppLogic.width / 2, LayoutParams.MATCH_PARENT);
		listView = (PullToRefreshListView) popContent.findViewById(R.id.listview);
		listView.setLayoutParams(params);
		adapter = new ItemPlayerListAdapter(getApplicationContext());
		listView.setAdapter(adapter);

		listView.setMode(Mode.PULL_FROM_END);
		listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				engine.searchListData(true);
			}
		});

		lvListView = listView.getRefreshableView();
		lvListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				position--;
				engine.start(position);
				if (null != showPop && showPop.isShowing()) {
					showPop.dismiss();
				}
			}
		});
	}

	/**
	 * 初始化监听器
	 */
	private void initListener() {
		mIv_close.setOnClickListener(this);
		mIv_play_mode.setOnClickListener(this);
		mIv_prev.setOnClickListener(this);
		mIv_play.setOnClickListener(this);
		mIv_next.setOnClickListener(this);
		mIv_play_list.setOnClickListener(this);
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Log.d(TAG, "onStopTrackingTouch");
				engine.seekTo(seekBar.getProgress() * 0.01F);
				engine.mManuTouch = false;
				seekBar.setEnabled(false);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				engine.mManuTouch = true;
				Log.d(TAG, "onStartTrackingTouch");
				// engine.update(null, false);
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!SharedPreferencesUtils.isCloseVolume()) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startY = (int) event.getRawY();
				// TODO：显示声音界面
				// mSoundHandler.sendEmptyMessage(SHOWPOPINT);
				break;
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(event.getRawY() - startY) > 30 && startY > 0) {
					if (event.getRawY() - startY > 0) {
						AudioManagerHelper.reduceSound();
					} else {
						AudioManagerHelper.addSound();
					}
					startY = (int) event.getRawY();
					Utils.showSoundView();
					// mSoundHandler.sendEmptyMessage(CHANGESOUND);
				}
				break;
			case MotionEvent.ACTION_UP:
				startY = 0;
				break;
			default:
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	// End Of Content View Elements
	@SuppressLint("CutPasteId")
	private void bindViews() {
		seekbar = (CustomSeekBar) findViewById(R.id.seekBar1);
		mTv_source = (TextView) findViewById(R.id.tv_source);
		mTv_current_time = (TextView) findViewById(R.id.tv_current_time);
		mTv_end_time = (TextView) findViewById(R.id.tv_end_time);
		mIv_play_mode = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play_mode);
		mIv_prev = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_prev);
		mIv_play = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play);
		mIv_next = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_next);
		mIv_play_list = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play_list);
		mTvSoundValue = (TextView) findViewById(R.id.tv_soundValue);
		// vpMediaPlayer = (ViewPager) findViewById(R.id.vp_media_player);
		mIv_close = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_back);
		ivRount = (ImageView) findViewById(R.id.iv_round);
		ivBufferBtn = (ImageView) findViewById(R.id.iv_buffer_btn);
		tvSongTitle = (TextView) findViewById(R.id.tv_song_title);
		tvSongTitle.setTypeface(Constant.typeFace);
		tvSongTitle.requestFocus();// 获取焦点
		tvSingler = (TextView) findViewById(R.id.tv_singer);
		tvSingler.setTypeface(Constant.typeFace);
		tvLoading = (TextView) findViewById(R.id.loading_tv);
		llConetent = (LinearLayout) findViewById(R.id.ll_content);
		flContent = findViewById(R.id.fl_content);
		mTipSoundView = View.inflate(this, R.layout.tip_sound, null);
		soundView = View.inflate(getApplicationContext(), R.layout.soundview, null);
		tvSound = (TextView) soundView.findViewById(R.id.tv_sound);
		winDialog = new WinDialog(true) {

			@Override
			protected View createView() {
				return soundView;
			}
		};
	}

	@Override
	public void onClick(View v) {
		// 两秒才可以支持继续点击

		switch (v.getId()) {
		case R.id.iv_back:
			onBackPressed();
			break;
		case R.id.seekBar1:
			break;
		case R.id.tv_end_time:
			break;
		case R.id.iv_play_mode:
			if (engine.getCurrentAudio() != null && !Utils.isSong(engine.getCurrentAudio().getSid())) {
				AppLogic.removeUiGroundCallback(toastTip);
				AppLogic.runOnUiGround(toastTip, 500);
			} else {
				engine.changeMode();
			}
			break;
		case R.id.iv_prev:
			if (SystemClock.elapsedRealtime() - startTime > defaultClickGap) {// 状态保护
				startTime = SystemClock.elapsedRealtime();
				engine.last();
				NetHelp.sendReportData(Action.PREVIOUS);
			}
			break;
		case R.id.iv_play:
			// PlayCommandFactory.createCommand(1).execute();
			// if (SystemClock.elapsedRealtime() - startTime > defaultClickGap)
			// {
			// startTime = SystemClock.elapsedRealtime();
			engine.playOrPause();
			if (engine.getStatus() == MediaPlayerActivityEngine.startState) {
				NetHelp.sendReportData(Action.PLAY);
			} else if (engine.getStatus() == MediaPlayerActivityEngine.pauseState) {
				NetHelp.sendReportData(Action.PAUSE);
			}
			// }
			break;
		case R.id.iv_next:
			if (SystemClock.elapsedRealtime() - startTime > defaultClickGap) {
				startTime = SystemClock.elapsedRealtime();
				engine.next();
				NetHelp.sendReportData(Action.NEXT);
			}
			break;
		case R.id.iv_play_list:
			// if (SystemClock.elapsedRealtime() - startTime > defaultClickGap)
			// {
			// startTime = SystemClock.elapsedRealtime();
			showPop = Tool.showPop(popContent, mIv_play_list.getRootView());
			// }
			break;
		}
	}

	Runnable toastTip = new Runnable() {
		public void run() {
			ToastUtils.showShort(Constant.RS_VOICE_SPEAK_CANTSUPPORT_TIPS);
		}
	};
	private Dialog dialog;

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
			Intent intent = new Intent(this, MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);// 跳转到播放器页面
			// overridePendingTransition(0, R.anim.menu_out);
			this.finish();
		} catch (Exception e) {
		}
	}

	@Override
	public void showPause() {
		closeBufferButton();
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mIv_play.setImageResource(R.drawable.fm_player_stop);
			}
		}, 0);
	}

	@Override
	public void showPlay() {
		closeBufferButton();
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mIv_play.setImageResource(R.drawable.fm_player_play);
			}
		}, 0);
	}

	@Override
	public void notifyMusicInfo(final Audio info, boolean isLoading) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "tele::" + info);
				if (info != null) {
					tvLoading.setVisibility(View.GONE);
					llConetent.setVisibility(View.VISIBLE);
					doNotClick(true);

					mTv_current_time.setText("0:00");
					mTv_end_time.setText(getTime(info.getDuration()));
					if (info.isbShowSource() && StringUtils.isNotEmpty(info.getSourceFrom())) {
						mTv_source.setVisibility(View.VISIBLE);
						// mTv_source.setText(info.getSourceFrom());
						mTv_source.setText(
								Html.fromHtml(getResources().getString(R.string.source, info.getSourceFrom())));
					} else {
						LogUtil.logd("i'm not visible,because bshow= " + info.isbShowSource());
						mTv_source.setVisibility(View.INVISIBLE);
					}
					tvSongTitle.setText(info.getName());
					if (Utils.isSong(info.getSid())) {
						if (CollectionUtils.isNotEmpty(info.getArrArtistName())) {
							tvSingler.setText(CollectionUtils.toString(info.getArrArtistName()));
						} else {
							tvSingler.setText("");
						}
					} else {
						if (null != engine) {
							if (StringUtils.isEmpty(engine.getCurrentAlbumName())) {
								tvSingler.setText(CollectionUtils.toString(info.getArrArtistName()));
							} else {
								tvSingler.setText(engine.getCurrentAlbumName());
							}
						}
						// 更新播放模式
						showSequenceMode();
					}
				} else {
					showLoadingView();
				}
			}
		}, 0);
	}

	public String getTime(long time) {
		if (time < 0) {
			time = Math.abs(time);
		}
		long second = time / 1000;
		int min = (int) (second / 60);
		int sec = (int) (second % 60);
		if (sec < 10) {
			return min + ":0" + sec;
		}
		return min + ":" + sec;
	}

	@Override
	public void notifyMusicListInfo(final List<Audio> infos,final boolean isAdd) {

		LogUtil.logd(TAG + "play list :" + (CollectionUtils.isEmpty(infos) ? "0" : infos.size()));

		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				listView.onRefreshComplete();

				if (CollectionUtils.isNotEmpty(infos)) {
					adapter.setData(infos,isAdd);
				} else {
					if (adapter.getCount() != 0) {
						ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
						// TtsUtil.speakText(Constant.SPEAK_NODATA_TIPS);
					}
				}
			}
		}, 0);

	}

	@Override
	public void showSequenceMode() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play_mode.setImageResource(R.drawable.fm_player_sequential_playing);
			}
		}, 0);
	}

	@Override
	public void showRandomMode() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play_mode.setImageResource(R.drawable.fm_player_random_play);
			}
		}, 0);
	}

	@Override
	public void showSingleCircleMode() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play_mode.setImageResource(R.drawable.fm_player_single_cycle);
			}
		}, 0);
	}

	@Override
	public void setFinishedProgress(final float value, final long currentTime, final long endTime) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				// LogUtil.logd("mediaPlayer::currentTime::" + currentTime);
				seekbar.setProgress((int) (value * seekbar.getMax()));
				// seekbar.setFinishedProgress(value);
				mTv_current_time.setText(getTime(currentTime));
				if (endTime > 0) {
					mTv_end_time.setText(getTime(endTime));
				}
				seekbar.invalidate();
			}
		}, 0);
	}

	@Override
	public void setBufferProgress(final List<LocalBuffer> value) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				seekbar.setBufferRange(value);
			}
		}, 0);
	}

	/**
	 * 显示缓冲中状态
	 */
	@Override
	public void showBufferButton() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mIv_play.getVisibility() == View.VISIBLE) {
					mIv_play.setVisibility(View.INVISIBLE);
					ivRount.setVisibility(View.VISIBLE);
					ivBufferBtn.setVisibility(View.VISIBLE);
					ivRount.startAnimation(animation1);
				}
			}
		}, 0);
	}

	@Override
	public void closeBufferButton() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mIv_play.getVisibility() == View.INVISIBLE) {
					mIv_play.setVisibility(View.VISIBLE);
					ivRount.clearAnimation();
					ivRount.setVisibility(View.GONE);
					ivBufferBtn.setVisibility(View.GONE);
				}

			}
		}, 0);
	}

	/**
	 * 从哪个界面跳转到该界面
	 * 
	 * @param source
	 *            进入的来源
	 * @param activity
	 */
	public static void jumpFrom(Activity activity, int source) {
		Intent intent = new Intent(activity, MediaPlayerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(Constant.SOURCE, source);
		activity.startActivity(intent);// 跳转到播放器页面
	}

	/**
	 * 刷新列表数据（更改视图）以及定位到哪个位置
	 * 
	 */
	@Override
	public void notifyAndLocation(final int location, final boolean showCurrent) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				LogUtil.logd(TAG + "location:" + location + " showCurrent:" + showCurrent);
				if (showCurrent) {
					adapter.setCurrentPosition(location);
					adapter.notifyDataSetInvalidated();// 通知adapter数据有变化
				}
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						lvListView.setSelection(location);
					}
				}, 200);
				// SongListFragment.getInstance().refreshPosition();
			}
		}, 0);
	}

	@Override
	public void showLoadingView() {
		// AppLogic.runOnUiGround(new Runnable() {
		//
		// @Override
		// public void run() {
		LogUtil.logd("tele::tvLoading.setVisibility(View.VISIBLE)");
		tvLoading.setVisibility(View.VISIBLE);
		llConetent.setVisibility(View.GONE);

		doNotClick(false);
		// }
		// }, 0);
	}

	/**
	 * 是否可以点击
	 * 
	 * @param canClick
	 */
	private void doNotClick(final boolean canClick) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (canClick) {
					mIv_play.setAlpha(1f);
					mIv_play.setOnClickListener(MediaPlayerActivity.this);
					mIv_next.setAlpha(1f);
					mIv_next.setOnClickListener(MediaPlayerActivity.this);
					mIv_prev.setAlpha(1f);
					mIv_prev.setOnClickListener(MediaPlayerActivity.this);
					mIv_play_list.setAlpha(1f);
					mIv_play_list.setOnClickListener(MediaPlayerActivity.this);
					if (engine.getCurrentAudio() != null && Utils.isSong(engine.getCurrentAudio().getSid())) {
						mIv_play_mode.setAlpha(1f);
					} else {
						mIv_play_mode.setAlpha(0.5f);
					}
					mIv_play_mode.setOnClickListener(MediaPlayerActivity.this);
				} else {
					mIv_play.setAlpha(0.5f);
					mIv_play.setOnClickListener(null);
					mIv_next.setAlpha(0.5f);
					mIv_next.setOnClickListener(null);
					mIv_prev.setAlpha(0.5f);
					mIv_prev.setOnClickListener(null);
					mIv_play_list.setAlpha(0.5f);
					mIv_play_list.setOnClickListener(null);
					mIv_play_mode.setAlpha(0.5f);
					mIv_play_mode.setOnClickListener(null);
				}
			}
		}, 0);
	}

	@Override
	public void hiddenMode(final boolean b) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play_mode.setImageResource(R.drawable.fm_player_sequential_playing_disabled);
				mIv_play_mode.setOnClickListener(null);
				// mIv_play_mode.setVisibility(b ? View.INVISIBLE :
				// View.VISIBLE);
			}
		}, 0);
	}

	@Override
	public void dismiss() {
		// 这个界面finish掉，并且打开首界面
		onBackPressed();
		// onBackPressed();

	}

	private void dismissPopupWindow() {
		if (showPop != null) {
			showPop.dismiss();
			showPop = null;
		}
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
	}

	@Override
	public void notifyMusicListInfo(final String ttsText) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				ToastUtils.showShort(ttsText);
			}
		}, 0);
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				listView.onRefreshComplete();
			}
		}, 1000);
	}

	@Override
	protected void onResume() {
		LogUtil.logd(TAG + "onResume:" + this.hashCode());
		try {
			Intent intent = new Intent("com.txznet.music.Action.doing");
			intent.putExtra("foreground", true);
			GlobalContext.get().sendBroadcast(intent);
		} catch (Exception e) {
			LogUtil.loge("send broadCast[com.txznet.music.Action.doing] have error");
		}
		// 设置界面的信息
		// engine.init();
		initState();

		super.onResume();
	}

	@Override
	public void showTimeOutView() {
		if (tvLoading.getVisibility() == View.VISIBLE) {// 当前正在显示
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					tvLoading.setText(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
					TtsUtil.speakResource("RS_VOICE_SPEAK_TIPS_TIMEOUT", Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
				}
			}, 0);
		}
		notifyMusicListInfo(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
	}

	public void showSoundValueView() {
		// if (!SharedPreferencesUtils.isCloseVolume()) {
		// AppLogic.runOnUiGround(new Runnable() {
		// @Override
		// public void run() {
		// if (mTvSoundValue != null) {
		// mTvSoundValue
		// .setText("音量:"
		// + ((int) (Constant.currentSound * 10 + 0.5) * 10)
		// + "%");
		// }
		// }
		// }, 0);
		// }
	}

	@Override
	public void setSeekBarEnable(final boolean value) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (seekbar != null) {
					seekbar.setEnabled(value);
				}
			}
		}, 0);
	}

	@Override
	public void update(Observable observable, Object data) {
		// 如果播放

		// 如果暂停
	}

	@Override
	public void showPop(final boolean show) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (show) {
					// if (null != showPop && !showPop.isShowing()) {
					// showPop.sho
					// }
				} else {
					if (null != showPop && showPop.isShowing()) {
						showPop.dismiss();
					}
				}
			}
		}, 0);

	}

}
