package com.txznet.feedback.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.base.BaseActivity;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.service.MsgService;
import com.txznet.feedback.service.QuestionService;
import com.txznet.feedback.ui.widget.TabViewGroup;
import com.txznet.feedback.ui.widget.ViewPagerEx;
import com.txznet.feedback.volley.ResourceModule;

public class MainActivity extends BaseActivity {

	private TabViewGroup mTabVg;
	private ViewPagerEx mViewPagerEx;
	private ViewPagerAdapter mViewPagerAdapter;
	private List<View> mViewsList = new ArrayList<View>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				QuestionService.getInstance().init();
				ResourceModule.getInstance().executeGetNetMessage();
			}
		}, 0);
		setContentView(R.layout.main_activity);
		findWidget();
		init();
	}

	private void findWidget() {
		mTabVg = (TabViewGroup) findViewById(R.id.tab_tvg);
		mViewPagerEx = (ViewPagerEx) findViewById(R.id.main_viewpager);
	}

	private void init() {
		View lv = LayoutInflater.from(MainActivity.this).inflate(R.layout.list,
				null);
		View feedback = LayoutInflater.from(MainActivity.this).inflate(
				R.layout.feedback_layout, null);

		mViewsList.add(lv);
		mViewsList.add(feedback);

		QuestionWin.getInstance().init(lv);
		FeedBackWin.getInstance().init(feedback);

		mTabVg.setViewPager(mViewPagerEx);
		mViewPagerAdapter = new ViewPagerAdapter(MainActivity.this, mViewsList);
		mViewPagerEx.setAdapter(mViewPagerAdapter);
		mViewPagerEx.setOffscreenPageLimit(0);
		mTabVg.setViewPager(mViewPagerEx);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FeedBackWin.getInstance().setShowRedPoint(
				MsgService.getInstance().isNewIn());
	}

	private class ViewPagerAdapter extends PagerAdapter {

		List<View> mViewList;

		public ViewPagerAdapter(Context context, List<View> views) {
			mViewList = views;
			if (mViewList == null) {
				mViewList = new ArrayList<View>();
			}
		}

		@Override
		public int getCount() {
			return mViewList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView(mViewList.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(mViewList.get(position), 0);
			return mViewList.get(position);
		}
	}
}
