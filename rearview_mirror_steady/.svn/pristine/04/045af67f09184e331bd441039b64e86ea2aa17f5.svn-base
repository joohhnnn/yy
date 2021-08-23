package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.fm.dao.CategoryDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.ItemAlbumAdapter;
import com.txznet.music.adpter.TypeGridAdapter;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Category;
import com.txznet.music.bean.response.ResponseSearchAlbum;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.fragment.manager.DBManage;
import com.txznet.music.helper.RequestHelpe;
import com.txznet.music.ui.SingleActivity;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.MyAsyncTask;
import com.txznet.music.utils.Tool;
import com.txznet.music.widget.LinearLayoutManagerWrapper;
import com.txznet.music.widget.NavGridView;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.NavRecyclerView;
import com.txznet.music.widget.SpaceItemDecoration;

public class SingleMusicFragment extends BaseDataFragment<Category> implements
		OnClickListener {

	private int lastPostion = 0;// 用于记录上一次点击的分类位置

	// 使用单例
	public static class MyInstance {
		public static SingleMusicFragment instance = new SingleMusicFragment();
	}

	private RelativeLayout mType_name;
	private TextView mType;
	private TextView mType_filter;
	private NavRecyclerView mRecyclerView;

	private LinearLayout llLoadingLayout;
	private ImageView ivLoading;
	private LinearLayout llErrorView;
	private TextView btnRefresh;
	private TextView mTvShowTips;
	
	private LinearLayout llAlbumContent;
	private RelativeLayout rlFilterLayout;

	private Handler handler = new Handler() {
		public void dispatchMessage(android.os.Message msg) {
			switch (msg.arg1) {
			case START_FLAG:
				break;
			case END_FLAG:
				List<Album> albums = (List<Album>) msg.obj;
				if (CollectionUtils.isNotEmpty(albums)) {
					notifyAlbum(albums, false);
				}
				pageOff = 1;
				RequestHelpe.reqAlbum(mCategory.getCategoryId(), pageOff);
				break;
			}
			super.dispatchMessage(msg);
		};
	};

	@Override
	public void bindViews() {
		llLoadingLayout = (LinearLayout) findViewById(R.id.ll_loading);
		ivLoading = (ImageView) findViewById(R.id.iv_loading);
		llErrorView = (LinearLayout) findViewById(R.id.ll_error);
		btnRefresh = (TextView) llErrorView.findViewById(R.id.btn_refresh);
		mTvShowTips = (TextView) llErrorView.findViewById(R.id.tv_showtips);
		llAlbumContent = (LinearLayout) findViewById(R.id.ll_album_list);
		rlFilterLayout = (RelativeLayout) findViewById(R.id.rl_filter);
		rlFilterLayout.setVisibility(View.VISIBLE);
		mType_name = (RelativeLayout) findViewById(R.id.type_name);
		mType = (TextView) findViewById(R.id.type);
		mType_filter = (TextView) findViewById(R.id.tv_filter_name);
		mRecyclerView = (NavRecyclerView) findViewById(R.id.recyclerview);
	}

	View[] mViewList = new View[]{};
	
	private void setNavCurrentView(View view){
		if(!isHidden()){
			ObserverManage.getObserver().send(InfoMessage.SET_CURRENT_VIEW, view);
		}
	}
	
	private void setNavViewList(View[] views){
		if(!isHidden()){
			LogUtil.logd("NAVBtn:music fragment set views " + (views == null ? 0 : views.length));
			ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, views);
		}
	}
	
	@Override
	public void onResume() {
//		ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, mViewList);
		setNavViewList(mViewList);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		ObserverManage.getObserver().send(InfoMessage.DELETE_VIEW_LIST, mViewList);
		super.onPause();
	}
	
	private View popView;
	private PopupWindow popupWindow;
	private NavGridView mGridView;
	private List<Category> mCategorys = new ArrayList<Category>();// 选择类目
	private TypeGridAdapter filterGridViewAdapter;
	private Category mCategory;
	private MyAsyncTask<Void, List<Album>> getAlbumTask;
	// private AsyncTask<Void, Void, List<Album>> getAlbumAndRequestTask;

	private int pageOff = 1;// 当前页码数,从1 开始
	private int lastVisibleItem;

	public void initPopView() {
		popView = View.inflate(getActivity(), R.layout.pop_type_select_layout,
				null);
		popView.findViewById(R.id.close).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (null != popupWindow && popupWindow.isShowing()) {
							popupWindow.dismiss();
						}
					}
				});
		mGridView = (NavGridView) popView.findViewById(R.id.type_select_grid_view);
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						mGridView.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						if (SingleActivity.isNavBtnEnable()) {
							popView.findViewById(R.id.close).setVisibility(View.GONE);
						}
						int itemTotalHeight = mGridView.getMeasuredHeight();
						LogUtil.logd("mGridView,w="
								+ mGridView.getMeasuredWidth() + ",h="
								+ mGridView.getMeasuredHeight());
						int itemPreHeight = GlobalContext.get().getResources()
								.getDimensionPixelSize(R.dimen.y94);
						int itemSize = (int) (itemTotalHeight * 1f
								/ itemPreHeight + 0.5);
						filterGridViewAdapter.setItemHeight(itemTotalHeight
								/ itemSize);
					}
				});
		
		OnItemClickListener mOnGridItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				mCategory = (Category) filterGridViewAdapter.getItem(position);
				filterGridViewAdapter.setSelectedIndex(position);
				getAlbumTask = new MyAsyncTask<Void, List<Album>>() {
					@Override
					protected void onPreExecute() {
						setTypeName(mCategory.getDesc());
						showLoadingView(true);
						popupWindow.dismiss();
						super.onPreExecute();
					}

					@Override
					protected void onPostExecute(List<Album> result) {
						notifyAlbum(result, false, false);
						if (CollectionUtils.isEmpty(result)) {
							pageOff = 1;
							RequestHelpe.reqAlbum(mCategory.getCategoryId(),
									pageOff);
						}
						lastPostion = position;// 将标志位置
						setNavCurrentView(rlFilterLayout);
						super.onPostExecute(result);
					}

					@Override
					protected List<Album> doInBackground(Void... params) {
						return AlbumDBHelper.getInstance()
								.findAll(
										Album.class,
										AlbumDBHelper.TABLE_CATEGORYID + "= ?",
										new String[] { mCategory
												.getCategoryId() + "" },
										"_index");
					}
				}.execute();
				// changeAlbum = true;
				// // 移动到第一个位置
				// manager.scrollToPosition(0);
			}
		};
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mGridView.setAdapter(filterGridViewAdapter);
		mGridView.setNavListener(filterGridViewAdapter);
		filterGridViewAdapter.setOnItemClickListener(mOnGridItemClickListener);
		mGridView.setOnItemClickListener(mOnGridItemClickListener);
	}

	/**
	 * 刷新视图界面，当选中的时候
	 * 
	 * @param value
	 */
	private void setTypeName(String value) {
		if (StringUtils.isEmpty(value)) {
			mType_filter.setVisibility(View.INVISIBLE);
		} else {
			mType_filter.setText(value);
			mType_filter.setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 刷新数据
	 * 
	 * @param result
	 *            添加的值
	 * @param add
	 *            是否累加
	 */
	private void notifyAlbum(List<Album> result, boolean add) {
		notifyAlbum(result, add, true);
	}

	/**
	 * 刷新数据
	 * 
	 * @param result
	 *            添加的值
	 * @param add
	 *            是否累加
	 */
	private void notifyAlbum(List<Album> result, boolean add,
			boolean showNodataView) {
		LogUtil.logd(TAG + " notify albums size ="
				+ (result != null ? result.size() : 0) + ", is add?:" + add);
		if (!add) {
			showLoadingView(false);
			albums.clear();
		} else {
			adapter.setShowLoading(false);
		}
		if (CollectionUtils.isNotEmpty(result)) {
			albums.addAll(result);
		}

		if (showNodataView && CollectionUtils.isEmpty(albums)) {
			// 展示没有数据
			showNodataView();
		}
		adapter.notifyDataSetChanged();
		if (!add) {
			manager.scrollToPositionWithOffset(0, 0);
		}
	}

	@Override
	public void reqData() {

	}

	@Override
	public void initListener() {
		findViewById(R.id.type).setOnClickListener(this);
		rlFilterLayout.setOnClickListener(this);
		btnRefresh.setOnClickListener(this);

		adapter.setOnRefreshListener(new NavListener.OnRefreshListener() {
			
			@Override
			public void onRefresh(int position) {
				adapter.setShowLoading(true);
//				adapter.notifyItemInserted(position + 1);
				adapter.notifyDataSetChanged();
				RequestHelpe.reqAlbum(mCategory.getCategoryId(),
						getPageOff() + 1);
			}
		});
		
		mRecyclerView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				if (mCategory != null && !adapter.isShowLoading()
						&& newState == 0
						&& lastVisibleItem + 1 >= adapter.getItemCount()) {
					adapter.setShowLoading(true);
//					adapter.notifyItemInserted(lastVisibleItem + 1);
					adapter.notifyDataSetChanged();
					RequestHelpe.reqAlbum(mCategory.getCategoryId(),
							getPageOff() + 1);
				}
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				lastVisibleItem = manager.findLastVisibleItemPosition();
			}

		});
	}

	public int getPageOff() {
		return pageOff;
	}

	private ItemAlbumAdapter adapter;
	private LinearLayoutManager manager;
	private List<Album> albums = new ArrayList<Album>();

	@Override
	public void initData() {
		pageOff = 0;
		mCategorys.clear();
		setTypeName("");
		manager = new LinearLayoutManagerWrapper(getActivity(), "SingleMusicFragment");
		manager.setOrientation(LinearLayoutManager.HORIZONTAL);
		manager.scrollToPosition(0);
		manager.scrollToPositionWithOffset(0, 0);
		// adapter = new ItemMusicAdapter(this, res);
		// 设置布局管理器
		mRecyclerView.setLayoutManager(manager);
		mRecyclerView.setHasFixedSize(true);
		// 设置adapter
		adapter = new ItemAlbumAdapter(this, albums);

		adapter.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				MediaPlayerActivityEngine.getInstance().stopAndLoading();
				RequestHelpe.reqAudio((Album) adapter.getItem(position),
						mCategory.getCategoryId());
			}
		});

		filterGridViewAdapter = new TypeGridAdapter(GlobalContext.get(),
				mCategorys);

		mRecyclerView.setAdapter(adapter);
		mRecyclerView.setNavListener(adapter);
		adapter.notifyDataSetChanged();
		// 设置Item增加、移除动画
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.scrollToPosition(0);
		int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.x20);

		mRecyclerView
				.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
		initPopView();
		Tool.init(getActivity());

	}

	@Override
	public int getLayout() {
		return R.layout.fragment_music_1;
	}

	@Override
	public int getFragmentId() {
		return 3;
	}

	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			LogUtil.logd(TAG + "[" + this.hashCode() + "]"
					+ "reqData:info type:" + info.getType());
			switch (info.getType()) {
			case InfoMessage.REQ_CATEGORY_ALL:
				List<Category> arrCategory = (List<Category>) info.getObj();
				notify(arrCategory);
				break;
			case InfoMessage.RESP_ALBUM:
				ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info
						.getObj();
				if (null != mCategory
						&& String.valueOf(mCategory.getCategoryId()).equals(
								responseAlbum.getCategoryId())) {
					pageOff = responseAlbum.getPageId();
					if (pageOff == 1) {// 如果是第一页
						notifyAlbum(responseAlbum.getArrAlbum(), false);
					} else {
						notifyAlbum(responseAlbum.getArrAlbum(), true);
					}
				} else {
					LogUtil.logw(TAG
							+ "["
							+ this.hashCode()
							+ "]"
							+ (mCategory != null ? mCategory.getCategoryId()
									: "null") + "/"
							+ responseAlbum.getCategoryId());
				}
				break;
			case InfoMessage.NET_ERROR:
				showNetTimeOutView(Constant.RS_VOICE_SPEAK_NONE_NET);
				break;

			case InfoMessage.NET_TIMEOUT_ERROR:
				showNetTimeOutView(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void beginGetData() {
		// 加载中
		showLoadingView(true);
		super.beginGetData();
	}

	@Override
	public List<Category> getDataFromLocal() {
		return CategoryDBHelper.getInstance().findAll(Category.class);
		// return DBSQLHelper1.queryData(Category.class,
		// new TypeToken<List<Category>>() {
		// }.getType(), "");
	}

	@Override
	public void notify(List<Category> result) {
		LogUtil.logd(TAG + " notify category size ="
				+ (result != null ? result.size() : 0));

		if (CollectionUtils.isNotEmpty(result)) {
			// 过滤音乐数据
			mCategorys.clear();
			if (result.size() > lastPostion) {
				mCategory = result.get(lastPostion);
			} else {
				lastPostion = 0;
				mCategory = result.get(lastPostion);
			}
			for (Category category : result) {
				if (category.getCategoryId() == 100000) {// 过滤掉音乐和本地
					mCategorys.addAll(category.getArrChild());
				}
			}
			if (CollectionUtils.isNotEmpty(mCategorys)
					&& mCategorys.size() > lastPostion) {
				mCategory = mCategorys.get(lastPostion);
				setTypeName(mCategory.getDesc());
			} else {
				LogUtil.loge("no child type size:" + mCategory.getCategoryId()
						+ ",lastPostion:" + lastPostion);
			}
			filterGridViewAdapter.setSelectedIndex(lastPostion);
			filterGridViewAdapter.notifyDataSetChanged();

			// 二次请求，请求Album数据
			pageOff = 1;

			new Thread(new Runnable() {

				@Override
				public void run() {
					handler.sendEmptyMessage(START_FLAG);
					Message msg = Message.obtain();
					msg.arg1 = END_FLAG;
					msg.obj = AlbumDBHelper.getInstance().findAll(Album.class,
							AlbumDBHelper.TABLE_CATEGORYID + "= ?",
							new String[] { mCategory.getCategoryId() + "" },
							"_index");
					handler.sendMessage(msg);
				}
			}).start();
		} else {
			RequestHelpe.reqCategory();
		}
	}

	@Override
	public void onStop() {
		// AsyncTaskManager.cancelTask(getAlbumAndRequestTask);
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		super.onStop();
	}

	public void hideNetErrorView() {
		showLoadingView(true);
		if (null != llErrorView) {
			llErrorView.setVisibility(View.GONE);
		}
	}

	public void showNetTimeOutView(String tips) {
		LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "[view][timeout]"
				+ "[tip]:" + tips + ",albums:"
				+ (albums != null ? albums.size() : 0));
		if (CollectionUtils.isEmpty(albums)) {
			setViewVisible(llErrorView);
			mTvShowTips.setText(tips);
			btnRefresh.setVisibility(View.VISIBLE);
			
			mViewList = new View[]{rlFilterLayout, btnRefresh};
			setNavViewList(mViewList);
		} else {
			adapter.setShowLoading(false);
			adapter.notifyDataSetChanged();
		}
		// 重新设置监听器，否则加载超时后无法点击分类
		rlFilterLayout.setOnClickListener(this);
	}

	/**
	 * 没有数据的时候展示
	 */
	public void showNodataView() {
		LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "[view][empty]");
		MonitorUtil.monitorCumulant(Constant.M_EMPTY_ALBUM);
		mTvShowTips.setText("当前分类没有数据");
		setViewVisible(llErrorView);
		btnRefresh.setVisibility(View.GONE);
		
		setNavViewList(new View[]{rlFilterLayout});
	}

	/**
	 * 是否显示加载中图像
	 * 
	 * @param show
	 */
	public void showLoadingView(boolean show) {
		LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "[view][load]" + show);
		if (show) {
			setViewVisible(llLoadingLayout);
			RotateAnimation animation = new RotateAnimation(0, 360,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setRepeatCount(Animation.INFINITE);
			animation.setDuration(1000);
			animation.setFillAfter(true);
			animation.setInterpolator(new LinearInterpolator());// 匀速
			ivLoading.startAnimation(animation);
			mType_filter.setAlpha(0.8f);
			rlFilterLayout.setOnClickListener(null);
			
			mViewList = new View[]{};
			setNavViewList(mViewList);
		} else {
			setViewVisible(llAlbumContent);
			rlFilterLayout.setOnClickListener(this);
			ivLoading.clearAnimation();
			
			mViewList = new View[]{rlFilterLayout, mRecyclerView};
			setNavViewList(mViewList);
		}
	}

	/**
	 * 设置是否显示界面
	 */
	public void setViewVisible(View view) {
		llErrorView.setVisibility(View.GONE);
		llLoadingLayout.setVisibility(View.GONE);
		llAlbumContent.setVisibility(View.GONE);
		view.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_filter:
			// 弹出框
			popupWindow = Tool.showFillPop(popView, mType_filter.getRootView());
			mGridView.setNavIn(true);
			setNavViewList(new View[]{mGridView});
			mGridView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
						int oldBottom) {
					mGridView.removeOnLayoutChangeListener(this);
					setNavCurrentView(mGridView);
				}
			});
			
			mGridView.setFocusableInTouchMode(true);
			mGridView.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					return getActivity().dispatchKeyEvent(event);
				}
			});
			Tool.setOnDismissListener(new PopupWindow.OnDismissListener() {
				
				@Override
				public void onDismiss() {
					setNavViewList(mViewList);
					setNavCurrentView(rlFilterLayout);
				}
			});
			break;
		case R.id.btn_refresh:
			hideNetErrorView();
			RequestHelpe.reqCategory();
			break;
		case R.id.type:
			Log.d(TAG, "开启调试");
			Constant.ISNEED = true;
			break;
		default:
			break;
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		LogUtil.logd(TAG + "music onHiddenChanged:" + hidden);
		if (!hidden && null != filterGridViewAdapter) {
			filterGridViewAdapter.setSelectedIndex(lastPostion);
			filterGridViewAdapter.notifyDataSetChanged();
		}
		if(hidden){
			setNavViewList(new View[]{});
		}else{
			setNavViewList(mViewList);
		}
		super.onHiddenChanged(hidden);
	}

}
