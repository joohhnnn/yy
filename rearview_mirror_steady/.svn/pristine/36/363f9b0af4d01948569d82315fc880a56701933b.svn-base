package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.loader.AppLogic;
import com.txznet.nav.NavService;
import com.txznet.nav.NavService.OnHistoryNavigateInfoChangeListener;
import com.txznet.nav.manager.NavManager;
import com.txznet.nav.R;
import com.txznet.txz.util.runnables.Runnable1;

public class HistoryActivity extends BaseActivity implements
		OnItemLongClickListener {

	private Button mBtnGoHome;
	private Button mBtnGoCompany;
	private ListView mListView;
	private WinConfirm mWinConfirm;
	private HistoryAdapter mAdapter;
	private TextView mEmptyTextView;
	private int mIntWhere;

	private int mDeletePos = -1;
	private WinConfirm mConfirmDeleteWin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.addActivity(this);
		setContentView(R.layout.activity_history);
		initView();
		initOnClick();
	}

	@Override
	protected void onStart() {
		super.onStart();
		NavService.getInstance().requestHistory();
		NavService.getInstance().addHistoryChangeListener(
				new OnHistoryNavigateInfoChangeListener() {

					@Override
					public void onNavigateListChange() {
						AppLogic.removeUiGroundCallback(
								mRefreshRunnable);
						AppLogic.runOnUiGround(mRefreshRunnable,
								100);
					}
				});
	}

	Runnable mRefreshRunnable = new Runnable() {

		@Override
		public void run() {
			refreshList();
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		refreshList();
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

	private void initView() {
		mBtnGoHome = (Button) findViewById(R.id.btnGoHome);
		mBtnGoCompany = (Button) findViewById(R.id.btnGoCompany);
		mListView = (ListView) findViewById(android.R.id.list);
		mEmptyTextView = (TextView) findViewById(R.id.empty_tv);
		mListView.setOnItemLongClickListener(this);

		mConfirmDeleteWin = new WinConfirm() {
			@Override
			public void onClickOk() {
				if (mDeletePos != -1) {
					NavigateInfo info = niList.get(mDeletePos);
					deleteHistoryRecord(info);
				}
			}

			@Override
			public void cancel() {
				mDeletePos = -1;
			}

		}.setMessage("确定删除该记录？");
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (!mConfirmDeleteWin.isShowing()) {
			mConfirmDeleteWin.show();
		}

		mDeletePos = position;
		return true;
	}

	private void initOnClick() {
		mBtnGoHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isSetHome())
					showMyDialog(NavManager.LOCATION_HOME);
				else
					NavManager.getInstance().navigateHome();
			}
		});
		mBtnGoHome.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showMyDialog(NavManager.LOCATION_HOME);
				return true;
			}
		});

		mBtnGoCompany.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isSetCompany())
					showMyDialog(NavManager.LOCATION_COMPANY);
				else
					NavManager.getInstance().navigateCompany();
			}
		});
		mBtnGoCompany.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showMyDialog(NavManager.LOCATION_COMPANY);
				return true;
			}
		});

		// mListView.setOnItemClickListener(new
		// AdapterView.OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// onItemSelected(arg2);
		// }
		// });

		AppLogic.removeUiGroundCallback(
				mEnableOnItemClickRunnable);
		AppLogic.runOnUiGround(mEnableOnItemClickRunnable, 0);
	}

	public void refreshList() {
		Resources resources = getResources();
		String text;
		if (isSetHome()) {
			text = resources.getString(R.string.activity_history_go_home_text)
					+ resources.getString(R.string.activity_history_set_text);

		} else {
			text = resources.getString(R.string.activity_history_go_home_text)
					+ resources
							.getString(R.string.activity_history_not_set_text);
		}

		SpannableString msp = new SpannableString(text);
		// 设置字体大小（相对值,单位：像素） 参数表示为默认字体大小的多少倍
		msp.setSpan(new RelativeSizeSpan(0.6f), 2, 7,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 0.5f表示默认字体大小的一半

		if (mBtnGoHome != null)
			mBtnGoHome.setText(msp);

		if (isSetCompany()) {
			text = resources
					.getString(R.string.activity_history_go_company_text)
					+ resources.getString(R.string.activity_history_set_text);

		} else {
			text = resources
					.getString(R.string.activity_history_go_company_text)
					+ resources
							.getString(R.string.activity_history_not_set_text);
		}

		msp = new SpannableString(text);
		// 设置字体大小（相对值,单位：像素） 参数表示为默认字体大小的多少倍
		msp.setSpan(new RelativeSizeSpan(0.6f), 3, 8,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 0.5f表示默认字体大小的一半

		if (mBtnGoCompany != null)
			mBtnGoCompany.setText(msp);

		refreshData();
		if (mAdapter == null) {
			mAdapter = new HistoryAdapter();

			if (mListView != null) {
				mListView.setAdapter(mAdapter);
			}
		}

		mAdapter.notifyDataSetChanged();

		if (mListView == null || niList == null) {
			return;
		}

		if (niList.size() < 1) {
			mListView.setVisibility(View.GONE);
			mEmptyTextView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mEmptyTextView.setVisibility(View.GONE);
		}
	}

	private boolean isSetHome() {
		NavigateInfo home = NavManager.getInstance().getHome();
		if (home == null)
			return false;
		else
			return true;
	}

	private boolean isSetCompany() {
		NavigateInfo company = NavManager.getInstance().getCompany();
		if (company == null)
			return false;
		else
			return true;
	}

	private void onItemSelected(int index) {
		if (niList == null)
			return;
		if (index < 0 || index >= niList.size())
			return;

		NavigateInfo info = niList.get(index);
		if (info != null) {
			NavManager.getInstance().NavigateTo(info);
		}
	}

	Runnable mDisableOnItemClickRunnable = new Runnable() {

		@Override
		public void run() {
			mListView.setOnItemClickListener(null);
			AppLogic.removeUiGroundCallback(
					mEnableOnItemClickRunnable);
			AppLogic.runOnUiGround(mEnableOnItemClickRunnable,
					4000);
		}
	};

	Runnable mEnableOnItemClickRunnable = new Runnable() {

		@Override
		public void run() {
			mListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					AppLogic.removeUiGroundCallback(
							mDisableOnItemClickRunnable);
					AppLogic.runOnUiGround(
							mDisableOnItemClickRunnable, 0);
					AppLogic.runOnBackGround(
							new Runnable1<Integer>(position) {

								@Override
								public void run() {
									onItemSelected(mP1);
								}
							}, 0);
				}
			});
		}
	};

	private void showMyDialog(int where) {
		String msg = null;
		String title = "家";
		mIntWhere = where;
		boolean set = false;
		if (NavManager.LOCATION_COMPANY == mIntWhere) {
			title = "公司";
			set = isSetCompany();
		} else if (NavManager.LOCATION_HOME == mIntWhere) {
			set = isSetHome();
		}

		if (set)
			msg = "您将要修改" + title + "的位置，请确认？";
		else
			msg = "您还没有设置" + title + "的位置，是否马上设置？";

		if (mWinConfirm == null) {
			mWinConfirm = new WinConfirm() {

				@Override
				public void onClickOk() {
					// TODO 启动设置地址界面
					NavManager.getInstance().startSetLocation(mIntWhere, 0, "");
				}
			};
		}
		mWinConfirm.setMessage(msg);
		mWinConfirm.show();
	}

	private List<NavigateInfo> niList = new ArrayList<NavigateInfo>();

	private void refreshData() {
		NavigateInfoList historyList = NavManager.getInstance()
				.getHistoryList();
		int count = 0;
		if (historyList != null && historyList.rptMsgItem != null)
			count = historyList.rptMsgItem.length;
		if (niList == null) {
			niList = new ArrayList<NavigateInfo>();
		}
		niList.clear();
		for (int i = 0; i < count; i++) {
			niList.add(historyList.rptMsgItem[i]);
		}
	}

	private void deleteHistoryRecord(NavigateInfo info) {
		if (info == null) {
			return;
		}

		NavManager.getInstance().deleteHistoryNaviateInfo(info);
	}

	private class HistoryAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return niList.size();
		}

		@Override
		public Object getItem(int position) {
			return niList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(HistoryActivity.this)
						.inflate(R.layout.activity_history_list, null);
				holder = new ViewHolder();

				holder.iv = (ImageView) convertView.findViewById(R.id.img);
				holder.nameTv = (TextView) convertView.findViewById(R.id.name);
				holder.noteTv = (TextView) convertView.findViewById(R.id.note);
				holder.addressTv = (TextView) convertView
						.findViewById(R.id.address);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			NavigateInfo info = (NavigateInfo) getItem(position);
			if (info != null) {
				if (info.msgServerPushInfo != null
						&& info.msgServerPushInfo.uint32Type == 1) {
					holder.iv.setImageResource(R.drawable.icon_multi_car);
					holder.noteTv.setVisibility(View.VISIBLE);
				} else {
					holder.noteTv.setVisibility(View.GONE);
					holder.iv
							.setImageResource(R.drawable.activity_history_ic_list);
				}

				if (TextUtils.isEmpty(info.strTargetName)) {
					holder.nameTv.setText("地图选点");
				} else {
					holder.nameTv.setText(info.strTargetName);
				}
				holder.addressTv.setText(info.strTargetAddress);
			}

			return convertView;
		}

		class ViewHolder {
			ImageView iv;
			TextView nameTv;
			TextView noteTv;
			TextView addressTv;
		}
	}
}
