package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
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
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.FilterGridViewAdapter;
import com.txznet.music.adpter.ItemMusicAdapter;
import com.txznet.music.bean.req.ReqCategory;
import com.txznet.music.bean.req.ReqSearchAlbum;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Category;
import com.txznet.music.bean.response.Homepage;
import com.txznet.music.bean.response.ResponseSearchAlbum;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.Tool;
import com.txznet.music.widget.SpaceItemDecoration;

public class MusicFragment extends BaseFragment implements Observer{

	private TextView mType;
	private FilterGridViewAdapter filterGridViewAdapter;
	private PopupWindow popupWindow;
	private View popView;
	private LinearLayout llLoadingLayout;
	private ImageView ivLoading;
	private RecyclerView mRecyclerView;
	private ItemMusicAdapter adapter;
	private LinearLayoutManager manager;
	private TextView tv_type_filter;
	private LinearLayout llErrorView;
	private TextView btnRefresh;
	// ----------------------------
//	private static MusicFragment instance;

	private Set<Album> resSet = new LinkedHashSet<Album>();
	private List<Album> res = new ArrayList<Album>();
	public final static int PAGEITEMCOUNT = 3;
	private List<Category> filterData = new ArrayList<Category>();// 选择类目

	private String currentType = "热门";// 当前歌单分类，用于显示而已。
	private int lastVisibleItem;

	private boolean isFirst = true;// 是否是第一次加载
	public static boolean isDestory = false;
	public boolean changeAlbum = false;

	public static  int pageOff = 0;// 从第一页开始
	private  Category mCategory;
	private GridView mGridView;
	private TextView mTvShowTips;

	// ///////////////////////

	public MusicFragment() {
		adapter = new ItemMusicAdapter(this, res);
		filterGridViewAdapter = new FilterGridViewAdapter(GlobalContext.get());
	}

//	public static MusicFragment getInstance() {
//		if (null == instance) {
//			synchronized (MusicFragment.class) {
//				if (null == instance) {
//					instance = new MusicFragment();
//				}
//			}
//		}
//		return instance;
//	}

	@Override
	public int getLayout() {
		return R.layout.fragment_music;
	}

	@Override
	public void onStart() {
		LogUtil.logd("onStart");
		isDestory = false;
		super.onStart();
	}

	@Override
	public void initArguments() {
		if (getArguments() != null) {
			mCategory = JsonHelper.toObject(Category.class, getArguments()
					.getString(Constant.INTENT_CATEGORY));
			LogUtil.logd("send to next fragment =" + mCategory.toString());
		} else {
			mCategory=new Category();
			LogUtil.loge("category : nobody setting me ");
		}
		super.initArguments();
	}

	@Override
	public void bindViews() {
		tv_type_filter = (TextView) findViewById(R.id.type_filter);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
		mType = (TextView) findViewById(R.id.type);

		llLoadingLayout = (LinearLayout) findViewById(R.id.ll_loading);
		ivLoading = (ImageView) findViewById(R.id.iv_loading);
		llErrorView = (LinearLayout) findViewById(R.id.ll_error);
		btnRefresh = (TextView) llErrorView.findViewById(R.id.btn_refresh);
		mTvShowTips = (TextView) llErrorView.findViewById(R.id.tv_showtips);

		Drawable drawable1 = getResources().getDrawable(
				R.drawable.fm_item_screening_down);
		drawable1.setBounds(0, 0, 28, 28);// 第一0是距左边距离，第二0是距上边距离，40分别是长宽
		tv_type_filter.setCompoundDrawables(null, null, drawable1, null);//
		// 只放左边
		tv_type_filter.setCompoundDrawablePadding(2);
	}

	@Override
	public void initListener() {
		tv_type_filter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (llLoadingLayout.getVisibility() == View.GONE) {// 只有不在加载的时候才允许弹框
					popupWindow = Tool.showFillPop(popView,
							tv_type_filter.getRootView());
				}
			}
		});
		mRecyclerView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int newState) {
				if (isDestory) {
					return;
				}
				if (!adapter.isShowLoading() && newState == 0
						&& lastVisibleItem + 1 >= adapter.getItemCount()) {
					changeAlbum = false;
					adapter.setShowLoading(true);
//					adapter.notifyItemInserted(lastVisibleItem + 1);
					adapter.notifyDataSetChanged();
					reqAlbum(getPageOff());
				}
				super.onScrollStateChanged(recyclerView, newState);
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				lastVisibleItem = manager.findLastVisibleItemPosition();
			}

		});
		btnRefresh.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				hideNetErrorView();
				reqData();
			}
		});
	}

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
		mGridView = (GridView) popView.findViewById(R.id.type_select_grid_view);
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						mGridView.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
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
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mCategory = (Category) filterGridViewAdapter.getItem(position);
				currentType = mCategory.getDesc();
				setTypeName(currentType);
				readFromFile();
				changeAlbum = true;
				pageOff = 0;
				filterGridViewAdapter.setSelectedIndex(position);
				reqAlbum(1);
				// 移动到第一个位置
				manager.scrollToPosition(0);
				popupWindow.dismiss();
			}
		});
	}

	@Override
	public void initData() {
		ObserverManage.getObserver().addObserver(this);
		pageOff = 0;
		filterData.clear();
		if (mCategory!=null&&CollectionUtils.isNotEmpty(mCategory.getArrChild())) {
			tv_type_filter.setVisibility(View.VISIBLE);
			filterData.addAll(mCategory.getArrChild());
			currentType = mCategory.getArrChild().get(0).getDesc();
		} else {
			currentType = "大家都爱听";
			tv_type_filter.setVisibility(View.GONE);
		}
		// setCategoryId(String.valueOf(category.getCategoryId()));
		mType.setText(currentType);// 默认是显示第一个选项
		res.clear();
		resSet.clear();
		manager = new LinearLayoutManager(getActivity());
		manager.setOrientation(LinearLayoutManager.HORIZONTAL);
		manager.scrollToPosition(0);
		manager.scrollToPositionWithOffset(0, 0);
		// adapter = new ItemMusicAdapter(this, res);
		// 设置布局管理器
		mRecyclerView.setLayoutManager(manager);
		mRecyclerView.setHasFixedSize(true);
		// 设置adapter
		mRecyclerView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		// 设置Item增加、移除动画
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.scrollToPosition(0);
		int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.x20);

		mRecyclerView
				.addItemDecoration(new SpaceItemDecoration(spacingInPixels));
		initPopView();
		isFirst = true;
		Tool.init(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		filterGridViewAdapter.setData(filterData);
		mGridView.setAdapter(filterGridViewAdapter);
	}

	//
	// // 网络不给力 数据加载失败
	// public void showNetErrorView() {
	// AppLogic.runOnUiGround(new Runnable() {
	//
	// @Override
	// public void run() {
	// if (CollectionUtils.isEmpty(res)) {
	// showLoadingView(false);
	// if (null != llErrorView) {
	// llErrorView.setVisibility(View.VISIBLE);
	// }
	// } else {
	// TtsUtil.speakText(Constant.SPEAK_NETNOTCON_TIPS);
	// }
	// }
	// }, 0);
	// }

	public void showHiddenNetTimeOutView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (CollectionUtils.isEmpty(res)) {
					showLoadingView(false);
					if (null != llErrorView) {
						llErrorView.setVisibility(View.VISIBLE);
						mTvShowTips.setText("网络不给力，数据加载失败");
						btnRefresh.setVisibility(View.VISIBLE);
					}
				} else {
					adapter.setShowLoading(false);
					adapter.notifyDataSetChanged();
				}
			}
		}, 0);
	}

	public void hideNetErrorView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				showLoadingView(true);
				if (null != llErrorView) {
					llErrorView.setVisibility(View.GONE);
				}
			}
		}, 0);
	}

	public void showFilterData(final List<Category> categories) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				tv_type_filter.setVisibility(View.VISIBLE);
				filterData.clear();
				filterData.addAll(categories);
				mCategory = filterData.get(0);
				currentType = filterData.get(0).getDesc();
				mType.setText(currentType);// 默认是显示第一个选项
			}
		}, 0);
	}

	/**
	 * 刷新视图界面，当选中的时候
	 * 
	 * @param value
	 */
	private void setTypeName(String value) {
		try {
			mType.setText(value);
		} catch (Exception e) {
			LogUtil.logw("MusicFragment isDestroy =" + isDestory + ",e="
					+ e.getMessage());
		}
	}

	public void refreshData(final List<Album> data) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (isDestory) {
					return;
				}

				if (CollectionUtils.isEmpty(data)) {
					showLoadingView(false);
					adapter.setShowLoading(false);
					adapter.notifyDataSetChanged();
					// 展示没有数据的界面
					showNodataView();
					LogUtil.logd("update AlbumId：" + isFirst
							+ "，this not have any audios");
					return;
				}
				LogUtil.logd("/album/list=" + data.toString());

				if (null != llErrorView
						&& llErrorView.getVisibility() == View.VISIBLE) {
					llErrorView.setVisibility(View.GONE);
				}
				showLoadingView(false);

				if (isFirst) {
					res.clear();
					isFirst = false;
					resSet.clear();
				}
				if (changeAlbum) {
					res.clear();
					resSet.clear();
				}
				if (CollectionUtils.isNotEmpty(data)) {
					resSet.addAll(data);
					res.clear();
					// res.addAll(resSet);// 过滤相同数据
				}
				notifyData(resSet);
				saveToFile();
				setTypeName(currentType);
			}
		}, 0);
	}

	/**
	 * 没有数据的时候展示
	 */
	public void showNodataView() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (res.size() == 0) {
					showLoadingView(false);
					MonitorUtil.monitorCumulant(Constant.M_EMPTY_ALBUM);
					llErrorView.setVisibility(View.VISIBLE);
					mTvShowTips.setText("当前分类没有数据");
					btnRefresh.setVisibility(View.GONE);
				}
			}
		}, 0);
	}

	/**
	 * 是否显示加载中图像
	 * 
	 * @param show
	 */
	public void showLoadingView(boolean show) {
		if (null == llLoadingLayout) {
			return;
		}
		LogUtil.logd("show loading View :" + res.size() + ",showLoading="
				+ show);
		if (show) {
			if (llLoadingLayout.getVisibility() == View.GONE) {
				llLoadingLayout.setVisibility(View.VISIBLE);
				RotateAnimation animation = new RotateAnimation(0, 360,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setRepeatCount(Animation.INFINITE);
				animation.setDuration(1000);
				animation.setFillAfter(true);
				animation.setInterpolator(new LinearInterpolator());// 匀速
				ivLoading.startAnimation(animation);
			}
			if (null != llErrorView) {
				llErrorView.setVisibility(View.GONE);
			}
		} else {
			if (llLoadingLayout.getVisibility() == View.VISIBLE) {
				llLoadingLayout.setVisibility(View.GONE);
				ivLoading.clearAnimation();
			}
		}
	}

	public void setPageOff(int pageOff) {
		if (pageOff == 1) {
			resSet.clear();
			// res.clear();
		}
		this.pageOff = pageOff;
	}

	public int getPageOff() {
		return ++pageOff;
	}

	@Override
	public int getFragmentId() {
		return 2;
	}

	@Override
	public void onDestroy() {
		LogUtil.logd("onDestroy");
		manager = null;
		isDestory = true;
		res.clear();
		resSet.clear();
		mRecyclerView = null;
		adapter = null;
		if (popupWindow != null) {
			popupWindow.dismiss();
		}
		super.onDestroy();
		//注册
		ObserverManage.getObserver().deleteObserver(this);
	}

	private void saveToFile() {

		/*
		 * File file = FileUtils.getFilePath(Constant.SAVE_PATH, "Audio" +
		 * category.getCategoryId()); ObjectOutputStream oos = null; try { oos =
		 * new ObjectOutputStream(new FileOutputStream(file));
		 * oos.writeObject(resSet); oos.writeObject(pageOff); } catch (Exception
		 * e) { e.printStackTrace(); } finally { FileUtils.closeQuietly(oos); }
		 */
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				for (Album category : resSet) {
					category.setCategoryID(mCategory.getCategoryId());
				}
				// 删除之前的
				if (pageOff == 1) {
					boolean remove = AlbumDBHelper.getInstance().remove(
							AlbumDBHelper.TABLE_CATEGORYID + "= ?",
							new String[] { mCategory.getCategoryId() + "" });
					LogUtil.logd("remove:" + remove + ",album id="
							+ mCategory.getCategoryId());
				}
				AlbumDBHelper.getInstance().saveOrUpdate(res);
			}
		}, 0);
	}

	private void readFromFile() {
		// File file = FileUtils.getFilePath(Constant.SAVE_PATH, "Audio"
		// + category.getCategoryId());
		if (mCategory.getCategoryId() == 0 /* || !file.exists() */) {
			return;
		}
		try {
			resSet.clear();
			res.clear();
			resSet.addAll(AlbumDBHelper.getInstance().findAll(Album.class,
					AlbumDBHelper.TABLE_CATEGORYID + "= ?",
					new String[] { mCategory.getCategoryId() + "" }, "_index"));
			pageOff = resSet.size() / Constant.PAGECOUNT;
			notifyData(resSet);
			if (CollectionUtils.isNotEmpty(resSet)) {
				if (null != llErrorView) {
					llErrorView.setVisibility(View.GONE);
				}
				showLoadingView(false);
				isFirst = false;
			} else {
				showLoadingView(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ObjectInputStream ois = null;
		// FileInputStream fis = null;
		// try {
		// resSet.clear();
		// ois = new ObjectInputStream(fis = new FileInputStream(file));
		// resSet = (Set<Album>) ois.readObject();
		// res.clear();
		// if (CollectionUtils.isEmpty(resSet)) {
		// pageOff = 0;
		// } else
		// pageOff = (Integer) ois.readObject();
		// if (CollectionUtils.isNotEmpty(resSet)) {
		// if (null != llErrorView) {
		// llErrorView.setVisibility(View.GONE);
		// }
		// showLoadingView(false);
		// isFirst = false;
		// notifyData(resSet);
		// }
		// } catch (Exception e) {
		// } finally {
		// FileUtils.closeQuietly(ois);
		// FileUtils.closeQuietly(fis);
		// }
	}

	@Override
	public void reqData() {
		showLoadingView(true);
		res.clear();// 清空掉之前所有的数据
		if (CollectionUtils.isNotEmpty(mCategory.getArrChild())) {
			mCategory = mCategory.getArrChild().get(0);
			reqAlbum(1);
		} else {
			reqCategory();
		}
		readFromFile();
	}

	private void notifyData(Set<Album> value) {
		res.addAll(value);
		adapter.setShowLoading(false);
		adapter.notifyDataSetChanged();
	}

	private void reqAlbum(int pageOff) {
		ReqSearchAlbum album = new ReqSearchAlbum();
		album.setPageId(pageOff);
		album.setCategoryId(mCategory.getCategoryId());// 500000
		setTypeName(mCategory.getDesc());
		if (NetHelp.sendRequest(Constant.GET_SEARCH_LIST, album) == 0) {
			showHiddenNetTimeOutView();
		}
	}

	private void reqCategory() {
		ReqCategory reqcategory = new ReqCategory();
		reqcategory.setbAll(1);
		reqcategory.setCategoryId(mCategory.getCategoryId());
		if (NetHelp.sendRequest(Constant.GET_CATEGORY, reqcategory) == 0) {
			showHiddenNetTimeOutView();
		}
	}

	private String getCategoryId() {
		if (mCategory != null) {
			return String.valueOf(mCategory.getCategoryId());
		}
		return "";
	}

	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			LogUtil.logd(TAG + "reqData:info type:" + info.getType());
			switch (info.getType()) {
			case InfoMessage.REQ_CATEGORY_SINGLE:
				Homepage<Category> homepage = (Homepage<Category>) info.getObj();
				ArrayList<Category> arrCategory = (ArrayList<Category>) homepage
						.getArrCategory();
				ReqSearchAlbum album = new ReqSearchAlbum();
				// 当前有没有分类
				if (getCategoryId()
						.equals(String.valueOf(homepage.getReqType()))
						&& CollectionUtils.isNotEmpty(arrCategory)) {
					if (CollectionUtils
							.isNotEmpty(arrCategory.get(0).getArrChild())) {
						album.setCategoryId(arrCategory.get(0).getArrChild().get(0)
								.getCategoryId());
						showFilterData(
								arrCategory.get(0).getArrChild());
					} else {
						showNodataView();
						return;
					}
				} else {
					MonitorUtil.monitorCumulant(Constant.M_EMPTY_CATEGORY);
					album.setCategoryId(homepage.getReqType());
				}
				if (NetHelp.sendRequest(Constant.GET_SEARCH_LIST, album) == 0) {
					showHiddenNetTimeOutView();
				}
				
				
				
				break;
			case InfoMessage.RESP_ALBUM:
				ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info
						.getObj();
				if (null != mCategory
						&& String.valueOf(mCategory.getCategoryId()).equals(
								responseAlbum.getCategoryId())) {
					pageOff = responseAlbum.getPageId();
					Constant.categoryID = responseAlbum.getCategoryId();
					MusicFragment.pageOff = responseAlbum.getPageId();
					if (pageOff == 1) {// 如果是第一页
						isFirst=true;
						refreshData(responseAlbum.getArrAlbum());
					} else {
						isFirst=false;
						refreshData(responseAlbum.getArrAlbum());
					}
				}
				break;
			case InfoMessage.NET_ERROR:
				showHiddenNetTimeOutView();
				
//				showNetTimeOutView(Constant.SPEAK_NONE_NET);
				break;

			case InfoMessage.NET_TIMEOUT_ERROR:
				showHiddenNetTimeOutView();
//				showNetTimeOutView(Constant.SPEAK_TIPS_TIMEOUT);
				break;

			default:
				break;
			}
		}
	}
}
