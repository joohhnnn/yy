package com.txznet.music.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.NavPlayListAdapter;
import com.txznet.music.bean.req.ReqDataStats.Action;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.HistoryFragment;
import com.txznet.music.fragment.LocalMusicFragment;
import com.txznet.music.fragment.SingleMusicFragment;
import com.txznet.music.fragment.SingleRadioFragment;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.service.MediaPlaybackService;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Tool;
import com.txznet.music.utils.Utils;
import com.txznet.music.view.IMediaPlayer;
import com.txznet.music.widget.CustomSeekBar;
import com.txznet.music.widget.LinearLayoutManagerWrapper;
import com.txznet.music.widget.NavListener.OnRefreshListener;
import com.txznet.music.widget.NavRecyclerView;
import com.txznet.txz.util.NavBtnSupporter.NavBtnSupporter;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SingleActivity extends BaseActivity implements OnClickListener, IMediaPlayer, Observer {

	private static final String LOCAL = "local";
	private static final String HISTORY = "history";
	private static final String RADIO = "radio";
	private static final String MUSIC = "music";
	private static final int LOCAL_i = 0;
	private static final int MUSIC_i = 1;
	private static final int RADIO_i = 2;
	private static final int HISTORY_i = 3;

	int origx = 0, origy = 0;
	int detaX = 0, detaY = 0;

	private TextView mTv_local;
	private TextView mTv_music;
	private TextView mTv_radio;
	private TextView mTv_history;
	// private LinearLayout mRl_below;
	// private com.txznet.music.widget.CustomShowBar mCustomSeekBar1;
	private SeekBar mCustomSeekBar1;
	private RelativeLayout mLl_info;
	private TextView mTv_song_name;
	private TextView mTv_source;
	private TextView mTv_artist_name;
	private com.txznet.music.widget.ShadeImageView mIv_prev;
	private com.txznet.music.widget.ShadeImageView mIv_play;
	private com.txznet.music.widget.ShadeImageView mIv_next;
	private com.txznet.music.widget.ShadeImageView mIv_play_mode;
	private com.txznet.music.widget.ShadeImageView mIv_play_list;
	private FrameLayout mFm_content;

	// Content View Elements

	private LinearLayout mLl_local;
	private ImageView mIv_local;
	private LinearLayout mLl_music;
	private ImageView mIv_music;
	private LinearLayout mLl_radio;
	private ImageView mIv_radio;
	private LinearLayout mLl_history;
	private ImageView mIv_history;

	private ImageView ivBufferBtn, ivRount;

	int mTabIndex;

	HistoryFragment mHistoryFragment;
	LocalMusicFragment mLocalMusicFragment;
	SingleMusicFragment mSingleMusicFragment;
	SingleRadioFragment mSingleRadioFragment;

	private RotateAnimation animation1;

	// End Of Content View Elements

	private void bindViews() {
		mLl_local = (LinearLayout) findViewById(R.id.ll_local);
		mIv_local = (ImageView) findViewById(R.id.iv_local);
		mTv_local = (TextView) findViewById(R.id.tv_local);
		mLl_music = (LinearLayout) findViewById(R.id.ll_music);
		mIv_music = (ImageView) findViewById(R.id.iv_music);
		mTv_music = (TextView) findViewById(R.id.tv_music);
		mLl_radio = (LinearLayout) findViewById(R.id.ll_radio);
		mIv_radio = (ImageView) findViewById(R.id.iv_radio);
		mTv_radio = (TextView) findViewById(R.id.tv_radio);
		mLl_history = (LinearLayout) findViewById(R.id.ll_history);
		mIv_history = (ImageView) findViewById(R.id.iv_history);
		mTv_history = (TextView) findViewById(R.id.tv_history);

		ivRount = (ImageView) findViewById(R.id.iv_round);
		ivBufferBtn = (ImageView) findViewById(R.id.iv_buffer_btn);

		mTv_local = (TextView) findViewById(R.id.tv_local);
		mTv_music = (TextView) findViewById(R.id.tv_music);
		mTv_radio = (TextView) findViewById(R.id.tv_radio);
		mTv_history = (TextView) findViewById(R.id.tv_history);
		// mRl_below = (LinearLayout) findViewById(R.id.rl_below);
		// mCustomSeekBar1 = (com.txznet.music.widget.CustomShowBar)
		// findViewById(R.id.customSeekBar1);
		mCustomSeekBar1 = (SeekBar) findViewById(R.id.customSeekBar1);
		((CustomSeekBar) mCustomSeekBar1).setPadding(0);
		mLl_info = (RelativeLayout) findViewById(R.id.ll_info);
		mTv_song_name = (TextView) findViewById(R.id.tv_song_name);
		mTv_artist_name = (TextView) findViewById(R.id.tv_artist_name);
		mTv_source = (TextView) findViewById(R.id.tv_source);

		mIv_prev = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_prev);
		mIv_play = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play);
		mIv_next = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_next);
		mIv_play_mode = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play_mode);
		mIv_play_list = (com.txznet.music.widget.ShadeImageView) findViewById(R.id.iv_play_list);
		mFm_content = (FrameLayout) findViewById(R.id.fm_content);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LogUtil.logd(TAG + "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_single_layout);

		RequestHelpe.reqTag();	//请求配置数据
		animation1 = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation1.setRepeatMode(Animation.INFINITE);
		animation1.setRepeatCount(-1);
		animation1.setDuration(3000);
		bindViews();
		initPopView();
		// 主界面
		// 四个子界面
		findViewById(R.id.ll_local).setOnClickListener(this);
		findViewById(R.id.ll_history).setOnClickListener(this);
		findViewById(R.id.ll_music).setOnClickListener(this);
		findViewById(R.id.ll_radio).setOnClickListener(this);

		// 默认展示音乐模块
		findViewById(R.id.iv_next).setOnClickListener(this);
		findViewById(R.id.iv_prev).setOnClickListener(this);
		findViewById(R.id.iv_play).setOnClickListener(this);
		findViewById(R.id.iv_play_mode).setOnClickListener(this);
		findViewById(R.id.iv_play_list).setOnClickListener(this);
		if (mCustomSeekBar1 instanceof CustomSeekBar) {
			mCustomSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					Log.d(TAG, "onStopTrackingTouch");
					MediaPlayerActivityEngine.getInstance().seekTo(seekBar.getProgress() * 0.01F);
					MediaPlayerActivityEngine.getInstance().mManuTouch = false;
					seekBar.setEnabled(false);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					MediaPlayerActivityEngine.getInstance().mManuTouch = true;
					Log.d(TAG, "onStartTrackingTouch");
					// engine.update(null, false);
				}

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				}
			});
		}

		// 滑动
		findViewById(R.id.ll_info).setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					origx = (int) event.getX();
					origy = (int) event.getY();
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					detaX = (int) (origx - event.getX());
					detaY = (int) (origy - event.getY());
					// 如果Y轴滑动大于x轴滑动，则不处理
					if (Math.abs(detaY) < Math.abs(detaX) && Math.abs(detaX) > 40) {
						// 左滑（下一首）
						if (detaX > 0) {
							MediaPlayerActivityEngine.getInstance().next();
						}
						// 右滑(上一首)
						else {
							MediaPlayerActivityEngine.getInstance().last();
						}
					}
					break;
				}

				return true;
			}
		});
		if (savedInstanceState != null) {
			mHistoryFragment = (HistoryFragment) getFragmentManager()
					.findFragmentByTag(HistoryFragment.class.getSimpleName());
			mLocalMusicFragment = (LocalMusicFragment) getFragmentManager()
					.findFragmentByTag(LocalMusicFragment.class.getSimpleName());
			mSingleMusicFragment = (SingleMusicFragment) getFragmentManager()
					.findFragmentByTag(SingleMusicFragment.class.getSimpleName());
			mSingleRadioFragment = (SingleRadioFragment) getFragmentManager()
					.findFragmentByTag(SingleRadioFragment.class.getSimpleName());

		}
		// 不支持拖动
		mCustomSeekBar1.setEnabled(false);
		changeToFragment(1);
		// jumpTypeFragment(R.id.fm_content,
		// SingleMusicFragment.MyInstance.instance, MUSIC);
		setSelected(mTv_music, mIv_music);

		ObserverManage.getObserver().addObserver(this);

		mNavSup = NavBtnSupporter.attach(this).setFocusResource(R.drawable.shape_focus_rect);
	}

	private void initNavView() {
		mViewList.clear();
		mViewList.add(mLl_local);
		mViewList.add(mLl_music);
		mViewList.add(mLl_radio);
		mViewList.add(mLl_history);
		mViewList.add(mIv_prev);
		mViewList.add(mIv_play);
		mViewList.add(mIv_next);
		mViewList.add(mIv_play_mode);
		mViewList.add(mIv_play_list);
	}

	List<View> mViewList = new ArrayList<View>();

	private void setNavViewList(List<View> views) {
		View[] viewlist = new View[views.size()];
		for (int i = 0; i < views.size(); i++) {
			viewlist[i] = views.get(i);
		}
		mNavSup.setViewList(viewlist, true);
	}

	private View popContent;
	private PopupWindow showPop;
	private NavRecyclerView listView;
	// private ListView lvListView;
	private LinearLayoutManager mLayoutManager;

	private NavPlayListAdapter adapter;

	public void initPopView() {
		Tool.init(this);// 用于初始化Tool
		// 初始化弹出框视图
		popContent = View.inflate(getApplicationContext(), R.layout.pop_nav_play_list, null);
		LinearLayout.LayoutParams params = new LayoutParams(AppLogic.width / 2, LayoutParams.MATCH_PARENT);
		listView = (NavRecyclerView) popContent.findViewById(R.id.listview);
		listView.setLayoutParams(params);
		adapter = new NavPlayListAdapter(getApplicationContext());
		mLayoutManager = new LinearLayoutManagerWrapper(this, "SingleActivity");
		listView.setLayoutManager(mLayoutManager);
		listView.setAdapter(adapter);
		listView.setNavListener(adapter);

		adapter.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(int position) {
				MediaPlayerActivityEngine.getInstance().searchListData(true);
			}
		});

		adapter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MediaPlayerActivityEngine.getInstance().start(position);
				if (null != showPop && showPop.isShowing()) {
					showPop.dismiss();
				}
			}

		});

		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				int lastPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
				if (mIsPlayListShow && newState == RecyclerView.SCROLL_STATE_IDLE
						&& lastPosition + 1 >= adapter.getItemCount()) {
					MediaPlayerActivityEngine.getInstance().searchListData(true);
				}
				super.onScrollStateChanged(recyclerView, newState);
			}
		});
	}

	@Override
	protected void onStart() {

		LogUtil.logd(TAG + "onStart");
		// 初始化播放器组件
		MediaPlayerActivityEngine.getInstance().setListener(this);
		MediaPlayerActivityEngine.getInstance().init();

		super.onStart();
	}

	private long startTime = 0;
	private static final int defaultClickGap = 1000;
	private NavBtnSupporter mNavSup;

	private void setSelected(View view, ImageView iv) {
		mTv_radio.setSelected(false);
		mTv_history.setSelected(false);
		mTv_music.setSelected(false);
		mTv_local.setSelected(false);

		mIv_history.setSelected(false);
		mIv_radio.setSelected(false);
		mIv_music.setSelected(false);
		mIv_local.setSelected(false);
		mLl_history.setBackgroundResource(0);
		mLl_local.setBackgroundColor(0);
		mLl_music.setBackgroundColor(0);
		mLl_radio.setBackgroundColor(0);

		iv.setSelected(true);
		((ViewGroup) iv.getParent()).setBackgroundColor(getResources().getColor(R.color.c_type_selected));
		view.setSelected(true);
	}

	// 跳转页面
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ll_local:
			// jumpTypeFragment(R.id.fm_content,
			// LocalMusicFragment.MyInstance.instance, LOCAL);
			changeToFragment(LOCAL_i);
			setSelected(mTv_local, mIv_local);
			break;
		case R.id.ll_music:
			changeToFragment(MUSIC_i);
			// jumpTypeFragment(R.id.fm_content,
			// SingleMusicFragment.MyInstance.instance, MUSIC);
			setSelected(mTv_music, mIv_music);
			break;
		case R.id.ll_radio:
			changeToFragment(RADIO_i);
			// jumpTypeFragment(R.id.fm_content,
			// SingleRadioFragment.MyInstance.instance, RADIO);
			setSelected(mTv_radio, mIv_radio);
			break;
		case R.id.ll_history:
			changeToFragment(HISTORY_i);
			// jumpTypeFragment(R.id.fm_content,
			// HistoryFragment.MyInstance.instance, HISTORY);
			setSelected(mTv_history, mIv_history);
			break;
		case R.id.iv_next:
			if (SystemClock.elapsedRealtime() - startTime > defaultClickGap) {
				startTime = SystemClock.elapsedRealtime();
				MediaPlayerActivityEngine.getInstance().next();
				NetHelp.sendReportData(Action.NEXT);
			}
			break;
		case R.id.iv_prev:
			if (SystemClock.elapsedRealtime() - startTime > defaultClickGap) {// 状态保护
				startTime = SystemClock.elapsedRealtime();
				MediaPlayerActivityEngine.getInstance().last();
				NetHelp.sendReportData(Action.PREVIOUS);
			}
			break;
		case R.id.iv_play:
			MediaPlayerActivityEngine.getInstance().playOrPause();
			if (MediaPlayerActivityEngine.getInstance().getStatus() == MediaPlayerActivityEngine.startState) {
				NetHelp.sendReportData(Action.PLAY);
				startService(new Intent(this, MediaPlaybackService.class));
			} else if (MediaPlayerActivityEngine.getInstance().getStatus() == MediaPlayerActivityEngine.pauseState) {
				NetHelp.sendReportData(Action.PAUSE);
			}

			break;
		case R.id.iv_play_mode:
			MediaPlayerActivityEngine.getInstance().changeMode();
			break;
		case R.id.iv_play_list:
			if (!mIsPlayListShow) {
				adapter.setData(mPlayListCache, false);
			}
			mIsPlayListShow = true;
			showPop = Tool.showPop(popContent, mIv_play_list.getRootView());
			if (mNavSup.getCurrentFocusIndex() != -1) {
				listView.setNavIn(true);
				mNavSup.setViewList(new View[] { listView });
				listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
							int oldTop, int oldRight, int oldBottom) {
						// TODO Auto-generated method stub
						listView.removeOnLayoutChangeListener(this);
						mNavSup.setCurrentFocusView(listView);
					}
				});

				listView.setFocusableInTouchMode(true);
				listView.setOnKeyListener(new View.OnKeyListener() {

					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						return dispatchKeyEvent(event);
					}
				});
				Tool.setOnDismissListener(new PopupWindow.OnDismissListener() {

					@Override
					public void onDismiss() {
						mNavSup.setViewList(mViewList);
						mNavSup.setCurrentFocusView(mIv_play_list);
						mIsPlayListShow = false;
					}
				});
			}
			break;
		}
	}

	public void changeToFragment(int mType) {
		if (mType == mTabIndex) {
			return;
		}
		mTabIndex = mType;

		// 管理fragment
		// Fragment事务
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		hide(transaction);
		switch (mType) {
		case 0:
			mLocalMusicFragment = (LocalMusicFragment) getFragmentManager()
					.findFragmentByTag(LocalMusicFragment.class.getSimpleName());
			if (null != mLocalMusicFragment) {
				transaction.show(mLocalMusicFragment);
			} else {
				mLocalMusicFragment = new LocalMusicFragment();
				transaction.add(R.id.fm_content, mLocalMusicFragment, LocalMusicFragment.class.getSimpleName());
			}
			break;
		case 1:
			mSingleMusicFragment = (SingleMusicFragment) getFragmentManager()
					.findFragmentByTag(SingleMusicFragment.class.getSimpleName());
			if (null != mSingleMusicFragment) {
				transaction.show(mSingleMusicFragment);
			} else {
				mSingleMusicFragment = new SingleMusicFragment();
				transaction.add(R.id.fm_content, mSingleMusicFragment, SingleMusicFragment.class.getSimpleName());
			}
			break;
		case 2:
			mSingleRadioFragment = (SingleRadioFragment) getFragmentManager()
					.findFragmentByTag(SingleRadioFragment.class.getSimpleName());
			if (null != mSingleRadioFragment) {
				transaction.show(mSingleRadioFragment);
			} else {
				mSingleRadioFragment = new SingleRadioFragment();
				transaction.add(R.id.fm_content, mSingleRadioFragment, SingleRadioFragment.class.getSimpleName());
			}
			break;
		case 3:
			mHistoryFragment = (HistoryFragment) getFragmentManager()
					.findFragmentByTag(HistoryFragment.class.getSimpleName());
			if (null != mHistoryFragment) {
				transaction.show(mHistoryFragment);
			} else {
				mHistoryFragment = new HistoryFragment();
				transaction.add(R.id.fm_content, mHistoryFragment, HistoryFragment.class.getSimpleName());
			}
			break;
		default:
			break;
		}
		transaction.commitAllowingStateLoss();
	}

	/**
	 * hide 所有的fragment
	 *
	 * @param ft
	 */
	private void hide(FragmentTransaction ft) {
		if (null != mLocalMusicFragment) {
			ft.hide(mLocalMusicFragment);
		}
		if (null != mSingleMusicFragment) {
			ft.hide(mSingleMusicFragment);
		}
		if (null != mSingleRadioFragment) {
			ft.hide(mSingleRadioFragment);
		}
		if (null != mHistoryFragment) {
			ft.hide(mHistoryFragment);
		}
	}

	public static boolean isNavBtnEnable() {
		return mIsNavBtnEnable;
	}

	// @Override
	// protected void onSaveInstanceState(Bundle outState) {
	//
	// }
	private static boolean mIsNavBtnEnable = false;
	private static final int KEYCODE_DEFAULT_PREV = 21;
	private static final int KEYCODE_DEFAULT_NEXT = 22;
	private static final int KEYCODE_DEFAULT_CONFIRM = 23;
	private static final int KEYCODE_DEFAULT_BACK = 4;

	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KEYCODE_DEFAULT_NEXT:
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				mIsNavBtnEnable = true;
				mNavSup.performNext();
			}
			return true;

		case KEYCODE_DEFAULT_CONFIRM:
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				mNavSup.performClick();
			}
			return true;

		case KEYCODE_DEFAULT_PREV:
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				mIsNavBtnEnable = true;
				mNavSup.performPrev();
			}
			return true;

		case KEYCODE_DEFAULT_BACK:
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				mNavSup.performBack();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			ActivityStack.getInstance().exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		// 默认选择第二个
		LogUtil.logd(TAG + "onResume");
		super.onResume();
	}

	@Override
	public void showPause() {
		closeBufferButton();
		setResourceOnUI(mIv_play, R.drawable.fm_player_stop1);
	}

	@Override
	public void showPlay() {
		closeBufferButton();
		setResourceOnUI(mIv_play, R.drawable.fm_player_play1);
	}

	@Override
	public void notifyMusicInfo(final Audio info, final boolean isLoading) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				LogUtil.logd(TAG + (info == null ? "null" : info.toString()));
				if (info == null) {
					if (isLoading) {
						mTv_song_name.setText(R.string.str_loding_song);
					} else {
						mTv_song_name.setText(R.string.str_no_play_song);
					}
					mTv_artist_name.setText("");
					mTv_source.setText("");
				} else {
					mTv_song_name.setText(info.getName());
					// 显示专辑名称
					if (Utils.isSong(info.getSid())) {
						mTv_artist_name.setText(StringUtils.toString(info.getArrArtistName()));
					} else {
						mTv_artist_name.setText(MediaPlayerActivityEngine.getInstance().getCurrentAlbumName());
					}
					mTv_source.setText(
							"来源:" + (StringUtils.isEmpty(info.getSourceFrom()) ? "未知音乐" : info.getSourceFrom()));

					if (!Utils.isSong(info.getSid())) {
						showSequenceMode();
						// 设置透明度为50%
						mIv_play_mode.setAlpha(0.5f);
					} else {
						mIv_play_mode.setAlpha(1f);
					}

				}
			}
		}, 0);
	}

	private List<Audio> mPlayListCache;
	private boolean mIsPlayListShow = false;

	@Override
	public void notifyMusicListInfo(final List<Audio> infos, final boolean isAdd) {
		mPlayListCache = infos;
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				// listView.onRefreshComplete();

				if (CollectionUtils.isNotEmpty(infos)) {
					int preCount = adapter.getItemCount();
					if (mIsPlayListShow) {
						adapter.setData(infos, isAdd);
					}
					if (isAdd) {
						listView.smoothScrollToPosition(preCount);
					}
				} else {
					if (adapter.getItemCount() != 0) {
						ToastUtils.showShort(Constant.RS_VOICE_SPEAK_NODATA_TIPS);
						// TtsUtil.speakText(Constant.SPEAK_NODATA_TIPS);
					}
				}
			}
		}, 0);

	}

	@Override
	public void showSequenceMode() {
		setResourceOnUI(mIv_play_mode, R.drawable.fm_player_sequential_playing1);
	}

	@Override
	public void showRandomMode() {
		setResourceOnUI(mIv_play_mode, R.drawable.fm_player_random_play1);
	}

	@Override
	public void showSingleCircleMode() {
		setResourceOnUI(mIv_play_mode, R.drawable.fm_player_single_cycle1);
	}

	@Override
	public void setFinishedProgress(final float value, long currentTime, long endTime) {

		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mCustomSeekBar1.setProgress((int) (value * mCustomSeekBar1.getMax()));
			}
		}, 0);

	}

	@Override
	public void setBufferProgress(final List<LocalBuffer> value) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				((CustomSeekBar) mCustomSeekBar1).setBufferRange(value);
			}
		}, 0);
	}

	@Override
	public void notifyAndLocation(final int location, final boolean showCurrent) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				LogUtil.logd(TAG + "location:" + location + " showCurrent:" + showCurrent);
				if (showCurrent) {
					adapter.notifyItemChanged(adapter.getCurrentPosition());
					adapter.setCurrentPosition(location);
					adapter.notifyItemChanged(adapter.getCurrentPosition());
				}
				// listView.smoothScrollToPosition(location);
				// AppLogic.runOnUiGround(new Runnable() {
				//
				// @Override
				// public void run() {
				// lvListView.setSelection(location);
				// }
				// }, 200);
				// SongListFragment.getInstance().refreshPosition();
			}
		}, 0);
	}

	@Override
	public void showBufferButton() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play.setVisibility(View.INVISIBLE);
				ivRount.setVisibility(View.VISIBLE);
				ivBufferBtn.setVisibility(View.VISIBLE);
				ivRount.startAnimation(animation1);
			}
		}, 0);
	}

	@Override
	public void closeBufferButton() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play.setVisibility(View.VISIBLE);
				ivRount.clearAnimation();
				ivRount.setVisibility(View.GONE);
				ivBufferBtn.setVisibility(View.GONE);
			}
		}, 0);
	}

	@Override
	public void showLoadingView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mTv_song_name.setText(R.string.str_loding_song);
			}
		}, 0);
	}

	@Override
	public void hiddenMode(boolean b) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mIv_play_mode.setImageResource(R.drawable.fm_player_sequential_playing_disabled);
				mIv_play_mode.setOnClickListener(null);
			}
		}, 0);
	}

	@Override
	public void dismiss() {
		// onBackPressed();
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
				// listView.onRefreshComplete();
			}
		}, 1000);
	}

	@Override
	public void showTimeOutView() {
		if (mTv_song_name != null) {// 当前正在显示
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					mTv_song_name.setText(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
					mTv_artist_name.setText("");
					mTv_source.setText("");
				}
			}, 0);
		}
		notifyMusicListInfo(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
	}

	@Override
	public void showSoundValueView() {

	}

	@Override
	public void setSeekBarEnable(final boolean value) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mCustomSeekBar1 != null) {
					mCustomSeekBar1.setEnabled(value);
				}
			}
		}, 0);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void setResourceOnUI(final ImageView view, final int resId) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				view.setImageResource(resId);
			}
		});
	}

	@Override
	protected void onRestart() {
		LogUtil.logd(TAG + "onRestart");
		super.onRestart();
	}

	@Override
	protected void onStop() {
		LogUtil.logd(TAG + "onStop");
		if (showPop != null) {
			showPop.dismiss();
		}
		super.onStop();
	}

	@Override
	protected void onPause() {
		LogUtil.logd(TAG + "onPause");
		// finish();// 品旭的生命走起不走该方法。
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		LogUtil.logd(TAG + "onDestroy");
		mNavSup.detach();
		super.onDestroy();
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

	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			LogUtil.logd(TAG + "reqData:info type:" + info.getType());
			switch (info.getType()) {
			case InfoMessage.PLAYER_LOADING:
				// 暂停音乐
				showPlay();
				// 恢复最初的状态
				notifyMusicInfo(null, true);
				break;
			case InfoMessage.ADD_VIEW_LIST:
				initNavView();
				Object obj = info.getObj();
				if (obj instanceof View[]) {
					View[] views = (View[]) obj;
					StringBuilder sb = new StringBuilder();
					for (View v : views) {
						sb.append(v.getId() + "  ");
					}
					LogUtil.logd("NAVBtn:SingleActivity set view list:" + sb.toString() + views.length);

					for (int i = 0; i < views.length; i++) {
						mViewList.add(4 + i, views[i]);
					}
				}
				setNavViewList(mViewList);
				break;
			case InfoMessage.DELETE_VIEW_LIST:
				LogUtil.logd("NAVBtn:DELETE_VIEW_LIST");
				// initNavView();
				// setNavViewList(mViewList);
				break;

			case InfoMessage.SET_CURRENT_VIEW:
				LogUtil.logd("NAVBtn:SET_CURRENT_VIEW");
				if (!mIsNavBtnEnable) {
					return;
				}
				if (info.getObj() != null && info.getObj() instanceof View) {
					View view = (View) info.getObj();
					mNavSup.setCurrentFocusView(view);
				}
				break;
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	}

}
