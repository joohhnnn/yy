package com.txznet.music.ui;

import java.util.ArrayList;
import java.util.Random;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txz.ui.music.UiMusic.MediaCategoryList;
import com.txz.ui.music.UiMusic.MediaItem;
import com.txz.ui.music.UiMusic.MediaList;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.Utils;
import com.txznet.music.service.MusicService;
import com.txznet.music.service.MusicService.PlayMode;

public class MainActivity extends BaseActivity implements
		MusicService.IMusicListner {

	static MainActivity sInstance;

	public static void finishAll() {
		if (sInstance != null)
			sInstance.finish();
	}

	private TextView mTxtTips;
	private CheckBox mCbCategory;

	private ImageView mImgMusicMode;
	private ImageView mImageView;
	private ImageView[] mImageViews;

	private ListView mListView;
	private TextView mTxtNoMusic;
	private ViewGroup mViewPoints;
	private LinearLayout mLlCategory;
	private RelativeLayout mRlMuisc; // 没有音乐界面
	private MusicMoveView mImgMusicCurPosition;

	private ViewPageAdapter mViewPageAdapter;
	private MusicListAdapter mMusicListAdapter;
	private ViewPageChangeListener mViewPageChangeListener;

	private MusicService mMusicService;
	private UpdateTips mUpdateTips;
	private Runnable mResetScorlledStateRunnable;

	private int mDeletePos = -1;
	private WinConfirm mWinConfirm;

	// 分类列表
	private ViewPager mViewPager;
	private ArrayList<View> mPageViews;

	private final int NUMS_PER_PAGE = 10;
	private final int UPDATE_TIPS_MILLIS = 5000;
	private final String[] mStrTips = { "小提示：你可以直接说'我要听汪峰的歌'",
			"小提示：你可以直接说'我要听小苹果'", "小提示：你可以直接说'播放月亮代表我的心'",
			"小提示：你可以直接说'播放陈奕迅的歌'", "小提示：你可以直接说'我要听摇滚歌曲'", };

	private final String DEFAULT_TITLE = "当前列表";
	private final String NO_MUISC_TEXT = "当前设备里没有歌曲，请检查网络是否正常！";
	private final String NO_MUISC_TEXT_LOCAL = "当前设备里没有歌曲！";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sInstance = this;
		super.onCreate(savedInstanceState);
		init();
		setContentView(R.layout.activity_music);
		initView();
		initListener();

		mMusicService.refreshCategoryList();
		MusicService.getInstance().syncMusicList();
	}

	private void init() {
		mMusicService = MusicService.getInstance();
		mMusicService.setListener(this);

		mUpdateTips = new UpdateTips();
		mMusicListAdapter = new MusicListAdapter();
		mResetScorlledStateRunnable = new Runnable() {
			@Override
			public void run() {
				if (mListView != null) {
					mListView.setSelection(mMusicService.getCurIndex());
				}
			}
		};

		mPageViews = new ArrayList<View>();
		mViewPageAdapter = new ViewPageAdapter();
		mViewPageChangeListener = new ViewPageChangeListener();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
		refreshView();
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
		mMusicService.setListener(null);
		sInstance = null;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private void refreshView() {
		if (mLlCategory != null)
			mLlCategory.setVisibility(View.INVISIBLE);
		onModeChanged();
		// 异步一下 新刷列表再刷左上角动画
		if (mImgMusicCurPosition != null)
			mImgMusicCurPosition.setVisibility(View.INVISIBLE);

		setTitle();
		// 没有音乐显示没有音乐界面
		if (isNoMusic()) {
			showMusicLayout(false);
		} else {
			showMusicLayout(true);
			refreshListView();
		}
	}

	private void refreshListView() {
		if (mListView != null)
			mListView.invalidateViews();
	}

	private void initView() {
		mImgMusicMode = (ImageView) findViewById(R.id.music_mode);
		mListView = (ListView) findViewById(android.R.id.list);
		mImgMusicCurPosition = (MusicMoveView) findViewById(R.id.music_cur_position);
		mCbCategory = (CheckBox) findViewById(R.id.cbCategory);
		mRlMuisc = (RelativeLayout) findViewById(R.id.rlMusic);
		mTxtNoMusic = (TextView) findViewById(R.id.txtNoMusic);
		mLlCategory = (LinearLayout) findViewById(R.id.llCategory);
		mTxtTips = (TextView) findViewById(R.id.txtTips);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	private void ensureDeleteWin() {
		mWinConfirm = new WinConfirm() {

			@Override
			public void onClickOk() {
				if (mDeletePos == -1) {
					return;
				}

				MusicService.getInstance().deleteMusic(mDeletePos, false);
			}

			@Override
			public void onClickRight() {
				if (mDeletePos == -1) {
					return;
				}

				MusicService.getInstance().deleteMusic(mDeletePos, true);
				onClickBlank();
			}

			@Override
			public void cancel() {
				if (mDeletePos == -1) {
					return;
				}

				MusicService.getInstance().deleteMusic(mDeletePos, true);
				onClickBlank();
			}

			@Override
			public void onClickBlank() {
				super.onBackPressed();
				mDeletePos = -1;
			}

		}.setMessage("是否要删除该音乐？").setSureText("从列表删除").setCancelText("从磁盘删除");
	}

	private void initListener() {
		mImgMusicCurPosition.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCbCategory == null || mListView == null
						|| mMusicService == null)
					return;
				mCbCategory.setChecked(false);
				mListView.setSelection(mMusicService.getCurIndex());
			}
		});

		mImgMusicMode.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mMusicService == null)
					return;
				if (mMusicService.getPlayMode() == PlayMode.PLAY_MODE_LOOP_ALL) {
					mMusicService.setPlayMode(PlayMode.PLAY_MODE_LOOP_SINGLE);
				} else if (mMusicService.getPlayMode() == PlayMode.PLAY_MODE_RANDOM) {
					mMusicService.setPlayMode(PlayMode.PLAY_MODE_LOOP_ALL);
				} else {
					mMusicService.setPlayMode(PlayMode.PLAY_MODE_RANDOM);
				}
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (mMusicService == null)
					return;
				// 播放暂停音乐
				if (mMusicService.getCurIndex() == arg2
						&& !mMusicService.isStop()) {
					if (mMusicService.isPlaying())
						mMusicService.pausePlay();
					else
						mMusicService.resumePlay();
				} else {
					mMusicService.playMusic(true, arg2, 0);
				}
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// int index = mMusicService.getCurrentCategory();
				// MediaCategory mc =
				// mMusicService.getMediaCategoryList().rptMsgCategoryList[index];
				// if (mc != null) {
				// if (mc.uint32CategoryType != null
				// && mc.uint32CategoryType == 1) { // 在线榜单
				// return false;
				// }
				// }

				if (mWinConfirm == null) {
					ensureDeleteWin();
				}

				if (!mWinConfirm.isShowing()) {
					mWinConfirm.show();
				}

				mDeletePos = position;
				return true;
			}
		});

		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (mResetScorlledStateRunnable == null)
					return;
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					AppLogic.removeUiGroundCallback(mResetScorlledStateRunnable);
					AppLogic.runOnUiGround(mResetScorlledStateRunnable, 10000);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		mCbCategory
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (mTxtTips == null || mLlCategory == null
								|| mUpdateTips == null)
							return;
						if (isChecked) {
							initViewPager();
							mTxtTips.setText(mStrTips[0]);
							mLlCategory.setVisibility(View.VISIBLE);
							AppLogic.runOnUiGround(mUpdateTips,
									UPDATE_TIPS_MILLIS);

							// 刷新分类
							mMusicService.refreshCategoryList();
						} else {
							mLlCategory.setVisibility(View.INVISIBLE);
							AppLogic.removeUiGroundCallback(mUpdateTips);
						}
					}
				});

		mListView.setAdapter(mMusicListAdapter);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // 一定要设置这个属性，否则ListView不会刷新
	}

	private void setTitle() {
		String title = null;
		if (mMusicService.getMediaList() == null
				|| mMusicService.getMediaList().strTitle == null
				|| mMusicService.getMediaList().strTitle.equals("")) {
			title = DEFAULT_TITLE;
		} else {
			title = mMusicService.getMediaList().strTitle;
		}
		if (mCbCategory != null)
			mCbCategory.setText(title);
	}

	private class UpdateTips implements Runnable {

		@Override
		public void run() {
			Random random = new Random();
			try {
				mTxtTips.setText(mStrTips[random.nextInt(mStrTips.length)]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			AppLogic.runOnUiGround(mUpdateTips, UPDATE_TIPS_MILLIS);
		}
	}

	@Override
	public void onMediaListChange(MediaList rptMediaItem) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				setTitle();
				if (isNoMusic()) {
					showMusicLayout(false);
				} else {
					showMusicLayout(true);
				}
				refreshListView();
			}
		}, 0);
	}

	@Override
	public void onPlayStart() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				refreshView();
			}
		}, 0);
	}

	@Override
	public void onPlayStop() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				refreshView();
			}
		}, 0);
	}

	@Override
	public void onModeChanged() {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mMusicService == null || mImgMusicMode == null)
					return;
				if (mMusicService.getPlayMode() == PlayMode.PLAY_MODE_LOOP_ALL) {
					mImgMusicMode
							.setImageResource(R.drawable.activity_music_mode_loop);
				} else if (mMusicService.getPlayMode() == PlayMode.PLAY_MODE_RANDOM) {
					mImgMusicMode
							.setImageResource(R.drawable.activity_music_mode_random);
				} else {
					mImgMusicMode
							.setImageResource(R.drawable.activity_music_mode_single);
				}
			}
		}, 0);
	}

	@Override
	public void onCategoryListUpdated(MediaCategoryList pbMediaCategoryList) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				initViewPager();
			}
		}, 0);
	}

	private class MusicListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			int count = 0;
			try {
				count = mMusicService.getMediaList().rptMediaItem.length;
			} catch (Exception e) {
				LogUtil.logw("count error!");
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			MediaItem mediaItem = null;
			try {
				mediaItem = mMusicService.getMediaList().rptMediaItem[position];
			} catch (Exception e) {
				LogUtil.logw("getMediaItem error!");
			}
			return mediaItem;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		private class ViewHolder {
			public ImageView imgMusicEarphone;
			public MusicMoveView musicMoveView;
			public TextView txtName; // 歌名
			public TextView txtSinger; // 歌手
			public CheckBox cbCollection; // 收藏
			public ImageView btnPlay; // 开始暂停
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Resources resources = MainActivity.this.getResources();
			LayoutInflater layoutInflater = LayoutInflater
					.from(MainActivity.this);

			ViewHolder holder;
			if (convertView == null) {
				convertView = layoutInflater.inflate(
						R.layout.activity_music_list, null);
				holder = new ViewHolder();
				holder.imgMusicEarphone = (ImageView) convertView
						.findViewById(R.id.musicEarphone);
				holder.musicMoveView = (MusicMoveView) convertView
						.findViewById(R.id.musicMoveView);
				holder.txtName = (TextView) convertView
						.findViewById(R.id.txtName);
				holder.txtSinger = (TextView) convertView
						.findViewById(R.id.txtSinger);
				holder.cbCollection = (CheckBox) convertView
						.findViewById(R.id.cbCollection);
				holder.btnPlay = (ImageView) convertView
						.findViewById(R.id.btnPlay);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 收藏
			holder.cbCollection.setTag(position);
			boolean bFavourite = false;
			try {
				bFavourite = mMusicService.getMediaList().rptMediaItem[position].msgMedia.bFavourite;
			} catch (Exception e) {
				LogUtil.logw("bFavourite error!");
			}
			if (bFavourite == false) {
				holder.cbCollection.setChecked(false);
			} else {
				holder.cbCollection.setChecked(true);
			}
			holder.cbCollection.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					CheckBox cbCollection = (CheckBox) v;
					int position = (Integer) v.getTag();
					boolean bFavourite = false;
					try {
						if (mMusicService.getMediaList().rptMediaItem[position].msgMedia.bFavourite == null)
							bFavourite = false;
						else
							bFavourite = mMusicService.getMediaList().rptMediaItem[position].msgMedia.bFavourite;
						mMusicService.getMediaList().rptMediaItem[position].msgMedia.bFavourite = !bFavourite;
						mMusicService.favouriteMusic(
								mMusicService.getMediaList().rptMediaItem[position].msgMedia,
								!bFavourite);
						cbCollection.setChecked(!bFavourite);
					} catch (Exception e) {
						LogUtil.loge("bFavourite error!");
					}
				}
			});

			// 播放
			holder.btnPlay.setTag(position);
			if (mMusicService.isPlaying()
					&& mMusicService.getCurIndex() == position) {
				holder.btnPlay.setImageResource(R.drawable.activity_music_stop);
				convertView
						.setBackgroundResource(R.color.win_music_item_pressed_bg);
				holder.txtSinger.setTextColor(resources
						.getColor(R.color.textSelected));
				holder.txtName.setTextColor(resources
						.getColor(R.color.textSelected));
				holder.imgMusicEarphone.setVisibility(View.INVISIBLE);
				holder.musicMoveView.setVisibility(View.VISIBLE);
			} else {
				holder.btnPlay.setImageResource(R.drawable.activity_music_play);
				holder.txtSinger.setTextColor(resources
						.getColor(R.color.text_color));
				holder.txtName.setTextColor(resources
						.getColor(R.color.text_color));
				convertView.setBackgroundResource(R.color.widget_color_normal);
				holder.imgMusicEarphone.setVisibility(View.VISIBLE);
				holder.musicMoveView.setVisibility(View.INVISIBLE);
			}

			String name = null;
			String singer = null;
			String[] singers = null;

			try {
				name = mMusicService.getMediaList().rptMediaItem[position].msgMedia.strTitle;
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				singers = mMusicService.getMediaList().rptMediaItem[position].msgMedia.rptStrArtist;
			} catch (Exception e) {
				e.printStackTrace();
				singers = new String[0];
			}

			// if
			// (mMusicService.getMediaList().rptMediaItem[position].msgMedia.strPath
			// .startsWith("http://"))
			// name = "*" + name;
			holder.txtName.setText(name);
			for (int i = 0; i < singers.length; i++) {
				if (i == 0)
					singer = singers[i];
				else
					singer += singers[i] + " ";
			}
			if (singer == null || singer.equals("")) {
				singer = "未知歌手";
			}
			if (mMusicService.isBufferProccessing()
					&& position == mMusicService.getCurIndex()) {
				singer += "  (缓冲中...)";
			}
			holder.txtSinger.setText(singer);
			refreshCurPoisitonIcon();
			return convertView;
		}
	}

	private class ViewPageAdapter extends PagerAdapter {

		// 销毁position位置的界面
		@Override
		public void destroyItem(View v, int position, Object arg2) {
			try {
				if (mPageViews != null) {
					((ViewPager) v).removeView(mPageViews.get(position));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		// 获取当前窗体界面数
		@Override
		public int getCount() {
			if (mPageViews != null)
				return mPageViews.size();
			return 0;
		}

		// 初始化position位置的界面
		@Override
		public Object instantiateItem(View v, int position) {
			((ViewPager) v).addView(mPageViews.get(position));

			// 测试页卡1内的按钮事件
			if (position == 1) {
			}

			return mPageViews.get(position);
		}

		// 判断是否由对象生成界面
		@Override
		public boolean isViewFromObject(View v, Object arg1) {
			return v == arg1;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	private class ViewPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int position) {
			for (int i = 0; i < mImageViews.length; i++) {
				mImageViews[position]
						.setBackgroundResource(R.drawable.activity_music_guide_page_indicator_focused);
				// 不是当前选中的page，其小圆点设置为未选中的状态
				if (position != i) {
					mImageViews[i]
							.setBackgroundResource(R.drawable.activity_music_guide_page_indicator);
				}
			}
		}
	}

	private class GridViewAdapter extends BaseAdapter implements
			AdapterView.OnItemClickListener {

		private MediaCategoryList mMediaCategoryList;
		private int mPageIndex;
		private int mPageCount;
		private int[] CATEGORY_BACKGROUND_COLOR = {
				R.drawable.activity_music_category_color_1,
				R.drawable.activity_music_category_color_2,
				R.drawable.activity_music_category_color_1,
				R.drawable.activity_music_category_color_3,
				R.drawable.activity_music_category_color_4,
				R.drawable.activity_music_category_color_4,
				R.drawable.activity_music_category_color_3,
				R.drawable.activity_music_category_color_4,
				R.drawable.activity_music_category_color_2,
				R.drawable.activity_music_category_color_3 };
		private int[] CATEGORY_BACKGROUND_COLOR_REVERSE = {
				R.drawable.activity_music_category_color_4,
				R.drawable.activity_music_category_color_3,
				R.drawable.activity_music_category_color_4,
				R.drawable.activity_music_category_color_2,
				R.drawable.activity_music_category_color_3,
				R.drawable.activity_music_category_color_1,
				R.drawable.activity_music_category_color_2,
				R.drawable.activity_music_category_color_1,
				R.drawable.activity_music_category_color_3,
				R.drawable.activity_music_category_color_4, };

		public GridViewAdapter(MediaCategoryList mediaCategoryList,
				int pageIndex) {
			mMediaCategoryList = mediaCategoryList;
			mPageIndex = pageIndex;
			mPageCount = mPageIndex * NUMS_PER_PAGE;
		}

		@Override
		public int getCount() {
			int count = 0;
			int length = mMediaCategoryList.rptMsgCategoryList.length;
			int tmp = length - (mPageIndex + 1) * NUMS_PER_PAGE;
			if (tmp > 0)
				count = NUMS_PER_PAGE;
			else
				count = length - mPageIndex * NUMS_PER_PAGE;
			return count;
		}

		@Override
		public Object getItem(int position) {
			return mMediaCategoryList.rptMsgCategoryList[mPageCount + position];
		}

		@Override
		public long getItemId(int position) {
			return mPageCount + position;
		}

		private class ViewHolder {
			public RelativeLayout rlCategoryBackground;
			public TextView txtCategoryName; // 分类名
			public TextView txtCategoryNum; // 分类歌手数
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.activity_music_category_item, null);
				holder = new ViewHolder();
				holder.rlCategoryBackground = (RelativeLayout) convertView
						.findViewById(R.id.rlCategoryBackground);
				holder.txtCategoryName = (TextView) convertView
						.findViewById(R.id.txtCategoryName);
				holder.txtCategoryNum = (TextView) convertView
						.findViewById(R.id.txtCategoryNum);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if ((mPageIndex % 2) == 0)
				holder.rlCategoryBackground
						.setBackgroundResource(CATEGORY_BACKGROUND_COLOR[position]);
			else
				holder.rlCategoryBackground
						.setBackgroundResource(CATEGORY_BACKGROUND_COLOR_REVERSE[position]);
			String categoryName = "";
			String categoryNum = "";
			try {
				categoryName = mMediaCategoryList.rptMsgCategoryList[mPageCount
						+ position].strCategoryName;
				categoryNum = String
						.valueOf(mMediaCategoryList.rptMsgCategoryList[mPageCount
								+ position].msgMediaList.rptMediaItem.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
			holder.txtCategoryName.setText(categoryName);
			holder.txtCategoryNum.setText(categoryNum);
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			mCbCategory.setChecked(false);
			// 收藏
			// if (mPageCount + arg2 == 1) {
			// if (mMediaCategoryList.rptMsgCategoryList[mPageCount +
			// arg2].msgMediaList == null
			// || mMediaCategoryList.rptMsgCategoryList[mPageCount
			// + arg2].msgMediaList.rptMediaItem == null
			// || mMediaCategoryList.rptMsgCategoryList[mPageCount
			// + arg2].msgMediaList.rptMediaItem.length == 0) {
			// showMusicLayout(false, NO_COLLECTION_TEXT);
			// return;
			// }
			// } else {
			// showMusicLayout(true, NO_COLLECTION_TEXT);
			// }
			mMusicService.setCategory(
					mMediaCategoryList.rptMsgCategoryList[mPageCount + arg2],
					true);
			mMusicService.setCurrCategory(mPageCount + arg2);
			mListView.setSelection(mMusicService.getCurIndex());
			refreshListView();
		}
	}

	private void refreshCurPoisitonIcon() {
		if (mMusicService == null || mListView == null
				|| mImgMusicCurPosition == null)
			return;
		int first = mListView.getFirstVisiblePosition();
		int last = mListView.getLastVisiblePosition();
		if (mMusicService.isPlaying()
				&& first != -1
				&& last != -1
				&& (first > mMusicService.getCurIndex() || last < mMusicService
						.getCurIndex())) {
			mImgMusicCurPosition.setVisibility(View.VISIBLE);
		} else {
			mImgMusicCurPosition.setVisibility(View.INVISIBLE);
		}
	}

	private void initViewPager() {
		MediaCategoryList mediaCategoryList = MusicService.getInstance()
				.getMediaCategoryList();
		if (mediaCategoryList == null) {
			LogUtil.loge("mediaCategoryList == null");
			return;
		}
		mViewPager = (ViewPager) findViewById(R.id.vpCategories);
		mPageViews.clear();
		int pages = 0;
		int categorySize = 0;
		int mod = 0;
		try {
			categorySize = mediaCategoryList.rptMsgCategoryList.length;
			pages = categorySize / NUMS_PER_PAGE;
			mod = categorySize % NUMS_PER_PAGE;
		} catch (Exception e) {
			LogUtil.loge("get categorySize error!");
		}
		if (mod != 0)
			pages++;
		for (int i = 0; i < pages; i++) {
			GridView category = (GridView) LayoutInflater.from(
					MainActivity.this).inflate(
					R.layout.activity_music_category, null);
			category.setSelector(new ColorDrawable(Color.TRANSPARENT));
			GridViewAdapter gridViewAdapter = new GridViewAdapter(
					mediaCategoryList, i);
			category.setAdapter(gridViewAdapter);
			category.setOnItemClickListener(gridViewAdapter);
			mPageViews.add(category);
		}

		// 创建imageviews数组，大小是要显示的图片的数量
		mImageViews = new ImageView[mPageViews.size()];
		mViewPager.setAdapter(mViewPageAdapter);
		mViewPager.setOnPageChangeListener(mViewPageChangeListener);
		mViewPoints = (ViewGroup) findViewById(R.id.indicator);
		mViewPoints.removeAllViews();

		// 添加小圆点的图片
		for (int i = 0; i < mPageViews.size(); i++) {
			mImageView = new ImageView(this);
			// 设置小圆点imageview的参数
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams

			(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(5, 0, 5, 0);
			mImageView.setLayoutParams(lp);
			// 将小圆点layout添加到数组中
			mImageViews[i] = mImageView;

			// 默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
			if (i == 0) {
				mImageViews[i]
						.setBackgroundResource(R.drawable.activity_music_guide_page_indicator_focused);
			} else {
				mImageViews[i]
						.setBackgroundResource(R.drawable.activity_music_guide_page_indicator);
			}

			// 将imageviews添加到小圆点视图组
			mViewPoints.addView(mImageViews[i]);
		}

		mViewPageAdapter.notifyDataSetChanged();
	}

	private boolean isNoMusic() {
		if (mMusicService == null)
			return true;
		MediaList mediaList = mMusicService.getMediaList();
		if (mediaList == null || mediaList.rptMediaItem == null
				|| mediaList.rptMediaItem.length == 0)
			return true;
		return false;
	}

	private void showMusicLayout(boolean b) {
		if (mRlMuisc == null || mTxtNoMusic == null)
			return;
		if (b) {
			mRlMuisc.setVisibility(View.GONE);
		} else {
//			if (Utils.checkQQMusicInstalled()) {
//				mTxtNoMusic.setText(NO_MUISC_TEXT);
//			} else {
				mTxtNoMusic.setText(NO_MUISC_TEXT_LOCAL);
//			}
			mRlMuisc.setVisibility(View.VISIBLE);
		}
	}
}
