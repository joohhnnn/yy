package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.CategoryDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.MyCategoryAdapter;
import com.txznet.music.bean.req.ReqCategory;
import com.txznet.music.bean.req.ReqCheck;
import com.txznet.music.bean.response.Category;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.ViewUtils;
import com.txznet.music.widget.LoadingUtils;
import com.txznet.music.widget.ScreenUtils;
import com.txznet.music.widget.ScreenUtils.MechainInfo;

public class HomepageFragment extends BaseFragment implements Observer {
	// 一页八张
	public static int LENGTH = 100;
	public static int imageWidth_Height = 150;
	public static int itemLineCount;
	public static int itemColCount;
	private boolean isMesureFinished = true;
	private static int maxHeight = 0;
	private static int minHeight = 0;

	private LinearLayout mTab_index;
	private ViewPager mViewPager;
	private MyCategoryAdapter adapter;

	private List<Category> res = new ArrayList<Category>();

	private long logoTag;

	static {
		if (Constant.defaultCategorys.isEmpty()) {
			Constant.defaultCategorys.put(1, new Category(1,
					R.drawable.fm_home_icon_me, "个人"));
			Constant.defaultCategorys.put(100000, new Category(100000,
					R.drawable.fm_home_icon_music, "音乐"));
			Constant.defaultCategorys.put(300000, new Category(300000,
					R.drawable.fm_home_icon_news, "新闻"));
			Constant.defaultCategorys.put(500000, new Category(500000,
					R.drawable.fm_home_icon_novel, "小说"));
			Constant.defaultCategorys.put(600000, new Category(600000,
					R.drawable.fm_home_icon_emotion, "情感"));
			Constant.defaultCategorys.put(700000, new Category(700000,
					R.drawable.fm_home_icon_funny, "搞笑段子"));
			Constant.defaultCategorys.put(800000, new Category(800000,
					R.drawable.fm_home_icon_finance, "财经"));
			Constant.defaultCategorys.put(900000, new Category(900000,
					R.drawable.fm_home_icon_crosstalk, "相声小品"));
			Constant.defaultCategorys.put(1000000, new Category(1000000,
					R.drawable.fm_home_icon_talkshow, "脱口秀"));
			Constant.defaultCategorys.put(1100000, new Category(1100000,
					R.drawable.fm_home_icon_sports, "体育"));
			Constant.defaultCategorys.put(1200000, new Category(1200000,
					R.drawable.fm_home_icon_parenting, "亲子"));
			Constant.defaultCategorys.put(1300000, new Category(1300000,
					R.drawable.fm_home_icon_radio_play, "广播剧"));
			Constant.defaultCategorys.put(1400000, new Category(1400000,
					R.drawable.fm_home_icon_bisexual, "两性"));
			Constant.defaultCategorys.put(1500000, new Category(1500000,
					R.drawable.fm_home_icon_cars, "汽车"));
			Constant.defaultCategorys.put(1600000, new Category(1600000,
					R.drawable.fm_home_icon_course, "公开课"));
			Constant.defaultCategorys.put(1700000, new Category(1700000,
					R.drawable.fm_home_icon_history, "历史人文"));
			Constant.defaultCategorys.put(1800000, new Category(1800000,
					R.drawable.fm_home_icon_military, "军事"));
			Constant.defaultCategorys.put(1900000, new Category(1900000,
					R.drawable.fm_home_icon_tech, "科技"));
			Constant.defaultCategorys.put(2000000, new Category(2000000,
					R.drawable.fm_home_icon_travel, "旅游"));
			Constant.defaultCategorys.put(2100000, new Category(2100000,
					R.drawable.fm_home_icon_opera, "评书戏曲"));
			Constant.defaultCategorys.put(2200000, new Category(2200000,
					R.drawable.fm_home_icon_life_style, "生活"));
			Constant.defaultCategorys.put(2300000, new Category(2300000,
					R.drawable.fm_home_icon_languages, "外语"));
			Constant.defaultCategorys.put(2400000, new Category(2400000,
					R.drawable.fm_home_icon_entertainment, "娱乐综艺"));
		}
	}

	/**
	 * 初始化视图
	 */
	public void bindViews() {
		mTab_index = (LinearLayout) view.findViewById(R.id.tab_index);
		// if (ScreenUtils.isLargeScreen(getActivity(), 440)) {// 适配“协创”
		LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.bottomMargin = 10;
		mTab_index.setLayoutParams(params);
		// }
		mViewPager = (ViewPager) view.findViewById(R.id.vpCategories);

		mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						mViewPager.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						int height = mViewPager.getHeight();
						int width = mViewPager.getWidth();
						MechainInfo info = ScreenUtils.getPoint(getActivity()
								.getApplicationContext());
						// 求一个Item的宽
						LogUtil.logd("剩余宽：" + width + ",height高：" + height
								+ "，密度：" + info.density);
						maxHeight = getActivity().getResources()
								.getDimensionPixelSize(R.dimen.y150);
						// int maxHeight = (int) (150 * info.density);
						int itemWidth = maxHeight;
						int itemLineCount = height / maxHeight;
						int itemColCount = width / itemWidth;
						if (itemLineCount == 0 || itemLineCount == 1) {
							itemLineCount = 2;
						}
						if (itemColCount == 0 || itemColCount == 1) {
							itemColCount = 2;
						}
						if (width >= height) {
							int detaheight = height % maxHeight;
							int everyDetaHeight = detaheight / itemLineCount;
							if (everyDetaHeight > getActivity().getResources()
									.getDimensionPixelSize(R.dimen.y50)) {
								maxHeight = (int) (getActivity().getResources()
										.getDimensionPixelSize(R.dimen.y200));
							} else {
								maxHeight += everyDetaHeight;
							}
							itemColCount = width / maxHeight;
							itemLineCount = height / maxHeight;
							itemWidth = maxHeight;

						} else {
							int detaWidth = width % itemColCount;
							int everyDetaWidth = detaWidth / itemColCount;
							if (everyDetaWidth > getActivity().getResources()
									.getDimensionPixelSize(R.dimen.x50)) {
								itemWidth = getActivity().getResources()
										.getDimensionPixelSize(R.dimen.x200);
							} else {
								itemWidth += everyDetaWidth;
							}
							itemLineCount = height / itemWidth;
							itemColCount = width / itemWidth;
							maxHeight = itemWidth;
						}
						if (itemLineCount == 0 || itemLineCount == 1) {
							itemLineCount = 2;
						}
						if (itemColCount == 0 || itemColCount == 1) {
							itemColCount = 2;
						}

						LENGTH = itemLineCount * itemColCount;
						HomepageFragment.itemColCount = itemColCount;
						HomepageFragment.itemLineCount = itemLineCount;
						LogUtil.logd("heigth:" + maxHeight + ",itemLineCount:"
								+ itemLineCount + ",width：" + itemWidth
								+ ",itemColCount：" + itemColCount);
						imageWidth_Height = (int) (maxHeight - getActivity()
								.getResources().getDimensionPixelSize(
										R.dimen.y7));
						adapter.notifyDataSetChanged();
						ViewUtils.setIndicator(mViewPager, mTab_index, res,
								LENGTH);// 默认为一
					}
				});
	}

	/**
	 * 初始化GridView的资源
	 */
	public void initData() {
		ObserverManage.getObserver().addObserver(this);
		adapter = new MyCategoryAdapter(res, this);
		mViewPager.setAdapter(adapter);
	}

	public void getDataFromDB() {
		List<Category> queryData = CategoryDBHelper.getInstance().findAll(Category.class);

		LogUtil.logd("getDataFromDB::" + queryData.toString());
		if (CollectionUtils.isNotEmpty(queryData)) {
			refreshData(queryData);
			if (queryData.size() < 2) {// 重新请求数据
				sendRequest();
			}
		} else {
			refreshData(setDefaultData());
			// sendRequest();
		}
	}

	public synchronized void refreshData(final List<Category> data) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				setData(data);

				ViewUtils.setIndicator(mViewPager, mTab_index, data, LENGTH);
				LoadingUtils.dismissDialog();
			}
		}, 0);
	}

	@Override
	public int getLayout() {
		return R.layout.fragment_homepage;
	}

	@Override
	public void initListener() {
		mViewPager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int point = event.getPointerCount();
				if (point != 1) {
					return true;
				}

				return false;
			}
		});
	}

	public void setData(List<Category> Categorys) {
		if (CollectionUtils.isNotEmpty(Categorys)) {
			res.clear();
			res.addAll(Categorys);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 发送请求
	 */
	public void sendRequest() {
		ReqCategory category = new ReqCategory();
		category.setbAll(1);
		NetHelp.sendRequest(Constant.GET_CATEGORY, category);
	}

	public void reqTag() {
		ReqCheck check = new ReqCheck();
		check.setLogoTag(logoTag);
		NetHelp.sendRequest(Constant.GET_TAG, check);
	}

	/**
	 * 默认数据
	 */
	private List<Category> setDefaultData() {
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category(1, R.drawable.fm_home_icon_me, "个人"));
		categories
				.add(new Category(100000, R.drawable.fm_home_icon_music, "音乐"));
		// categories.add(new Category(200000,
		// R.drawable.fm_home_icon_hot_radio, "热门节目"));
		categories
				.add(new Category(300000, R.drawable.fm_home_icon_news, "新闻"));
		// categories.add(new Category(400000, R.drawable.fm_home_icon_live,
		// "广播"));
		categories
				.add(new Category(500000, R.drawable.fm_home_icon_novel, "小说"));
		categories.add(new Category(600000, R.drawable.fm_home_icon_emotion,
				"情感"));
		categories.add(new Category(700000, R.drawable.fm_home_icon_funny,
				"搞笑段子"));
		categories.add(new Category(800000, R.drawable.fm_home_icon_finance,
				"财经"));
		categories.add(new Category(900000, R.drawable.fm_home_icon_crosstalk,
				"相声小品"));
		categories.add(new Category(1000000, R.drawable.fm_home_icon_talkshow,
				"脱口秀"));
		categories.add(new Category(1100000, R.drawable.fm_home_icon_sports,
				"体育"));
		categories.add(new Category(1200000, R.drawable.fm_home_icon_parenting,
				"亲子"));
		categories.add(new Category(1300000,
				R.drawable.fm_home_icon_radio_play, "广播剧"));
		categories.add(new Category(1400000, R.drawable.fm_home_icon_bisexual,
				"两性"));
		categories
				.add(new Category(1500000, R.drawable.fm_home_icon_cars, "汽车"));
		// categories.add(new Category(1600000, R.drawable.fm_home_icon_course,
		// "公开课"));
		// categories.add(new Category(1700000, R.drawable.fm_home_icon_history,
		// "历史人文"));
		// categories.add(new Category(1800000,
		// R.drawable.fm_home_icon_military,
		// "军事"));
		// categories
		// .add(new Category(1900000, R.drawable.fm_home_icon_tech, "科技"));
		// categories.add(new Category(2000000, R.drawable.fm_home_icon_travel,
		// "旅游"));
		// categories.add(new Category(2100000, R.drawable.fm_home_icon_opera,
		// "评书戏曲"));
		// categories.add(new Category(2200000,
		// R.drawable.fm_home_icon_life_style, "生活"));
		// categories.add(new Category(2300000,
		// R.drawable.fm_home_icon_languages,
		// "外语"));
		// categories.add(new Category(2400000,
		// R.drawable.fm_home_icon_entertainment, "娱乐综艺"));
		return categories;
	}

	@Override
	public int getFragmentId() {
		return 1;
	}

	@Override
	public void reqData() {
		getDataFromDB();
		reqTag();
	}

	@Override
	public void update(Observable observable, Object data) {

		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.REQ_CATEGORY_ALL:
				LogUtil.logd(TAG+"reqData:REQ_CATEGORY_ALL");
				List<Category> arrCategory = (List<Category>) info.getObj();
				refreshData(arrCategory);
				break;

			default:
				break;
			}
		}
	}
	
	@Override
	public void onDestroy() {
		ObserverManage.getObserver().deleteObserver(this);
		super.onDestroy();
	}

}
