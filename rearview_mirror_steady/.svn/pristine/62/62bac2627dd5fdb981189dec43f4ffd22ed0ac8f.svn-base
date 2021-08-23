package com.txznet.feedback.ui;

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.service.QuestionService;
import com.txznet.feedback.service.QuestionService.OnAnalysisListener;

public class QuestionWin implements OnGroupExpandListener,
		OnGroupCollapseListener, OnAnalysisListener {

	private View mView;
	private QuestionAdapter mAdapter;
	private ExpandableListView mListView;

	private static QuestionWin instance = new QuestionWin();

	private QuestionWin() {
	}

	public static QuestionWin getInstance() {
		return instance;
	}

	public void init(View view) {
		QuestionService.getInstance().setOnAnalysisListener(this);
		this.mView = view;
		initView();
	}

	private void initView() {
		mListView = (ExpandableListView) mView.findViewById(R.id.listview);
		mAdapter = new QuestionAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnGroupExpandListener(this);
		mListView.setOnGroupCollapseListener(this);
		mListView.setGroupIndicator(null);
		mListView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@SuppressLint("NewApi")
					@Override
					public void onGlobalLayout() {
						mListView.getViewTreeObserver()
								.removeOnGlobalLayoutListener(this);
						bottomHeight = mListView.getHeight();
					}
				});
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		mAdapter.setCollapse(groupPosition);
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		mAdapter.setGroupExpand(groupPosition);
	}

	private class QuestionAdapter extends BaseExpandableListAdapter {
		List<String> titleList;
		// int position;

		int property;

		public QuestionAdapter() {
			titleList = QuestionService.getInstance().getTitleList();
			setCollapse(-1);
		}

		@Override
		public int getGroupCount() {
			return titleList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		public void setGroupExpand(int position) {
			property |= 1 << position;
			notifyDataSetChanged();
		}

		public void setCollapse(int groupPosition) {
			if (groupPosition == -1) {
				property = 0;
			} else {
				property &= ~(1 << groupPosition);
			}
			notifyDataSetChanged();
		}

		private boolean hasProperty(int pos) {
			return (property & (1 << pos)) == (1 << pos);
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder();
				convertView = LayoutInflater.from(AppLogic.getApp())
						.inflate(R.layout.question_item_layout, null);
				holder.mTitleTv = (TextView) convertView;
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}

			String title = titleList.get(groupPosition);
			holder.mTitleTv.setText(title);
			if (hasProperty(groupPosition)) {
				// if (position != -1 && position == groupPosition) {
				Drawable right = AppLogic.getApp().getResources()
						.getDrawable(R.drawable.feedback_arrow2);
				holder.mTitleTv.setCompoundDrawablesWithIntrinsicBounds(null,
						null, right, null);
			} else {
				Drawable right = AppLogic.getApp().getResources()
						.getDrawable(R.drawable.feedback_arrow1);
				holder.mTitleTv.setCompoundDrawablesWithIntrinsicBounds(null,
						null, right, null);
			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			convertView = QuestionService.getInstance().getViewByPos(
					groupPosition);
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		class Holder {
			TextView mTitleTv;
		}
	}

	@Override
	public void onSuccess() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mAdapter.notifyDataSetChanged();
			}
		}, 0);
	}

	private int bottomHeight;
	private TextView mEmptyView;

	private TextView createEmptyView() {
		if (mEmptyView == null) {
			mEmptyView = new TextView(AppLogic.getApp());
			mEmptyView.setLayoutParams(new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT,
					AbsListView.LayoutParams.MATCH_PARENT));
			mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
			mEmptyView.setGravity(Gravity.CENTER);
			mEmptyView.setMinHeight(bottomHeight);
			mEmptyView.setTextColor(Color.parseColor("#adb6cc"));
			mEmptyView.setText(AppLogic.getApp().getResources()
					.getString(R.string.activity_download_no_record));
		}

		return mEmptyView;
	}

	@Override
	public void noFile() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				try {
					List<String> titleList = QuestionService.getInstance()
							.getTitleList();
					if (mListView != null) {
						if (titleList != null && titleList.size() < 1) {
							mListView.removeHeaderView(createEmptyView());
							mListView.addHeaderView(createEmptyView());
							mListView.setAdapter(mAdapter);
						} else {
							mListView.removeHeaderView(createEmptyView());
						}
					}
					mAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					LogUtil.loge(e.toString());
				}
			}
		}, 0);
	}
}