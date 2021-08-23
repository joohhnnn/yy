package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.offline.DownloadMapManager;
import com.txznet.nav.offline.DownloadMapManager.OnInitListener;
import com.txznet.nav.offline.DownloadMapManager.OnRefreshListener;
import com.txznet.nav.ui.widget.CityItemView;
import com.txznet.nav.ui.widget.CityItemView.OnMapListener;
import com.txznet.txz.util.runnables.Runnable1;

public class DownloadWin {
	private ListView mContentView;
	private TextView mEmptyView;
	private DownloadAdapter mAdapter;
	private WinConfirm mConfirmExitWin;

	private List<String> mDownloadNames = new ArrayList<String>();
	private List<OfflineMapCity> mDataList = new ArrayList<OfflineMapCity>();

	private int mDeletePos = -1;
	private boolean isInit;

	private static DownloadWin instance = new DownloadWin();

	private DownloadWin() {
	}

	public static DownloadWin getInstance() {
		return instance;
	}

	public boolean isInit() {
		return isInit;
	}

	public void init(View view) {
		mContentView = (ListView) view;
		mContentView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				bottomHeight = mContentView.getHeight();
			}
		});

		mContentView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				int headCount = mContentView.getHeaderViewsCount();
				if (headCount > 0) {
					return false;
				}

				if (!mConfirmExitWin.isShowing()) {
					mConfirmExitWin.show();
				}

				mDeletePos = position;
				return true;
			}
		});

		mConfirmExitWin = new WinConfirm() {

			@Override
			public void onClickOk() {
				if (mDeletePos == -1) {
					return;
				}

				OfflineMapCity omc = mDataList.get(mDeletePos);
				if (omc == null) {
					return;
				}

				OfflineMapManager omm = DownloadMapManager.getInstance().getOfflineMapManager();
				if (omm != null) {
					omm.remove(omc.getCity());
				}

				AppLogic.runOnBackGround(new Runnable() {

					@Override
					public void run() {
						analysisDataSource();
					}
				}, 0);
			}

			@Override
			public void onClickCancel() {
				mDeletePos = -1;
			}

		}.setMessage("确定删除此数据包？");

		if (isInit) {
			mAdapter = null;
			AppLogic.runOnUiGround(mRefreshViewRunnable, 0);
			return;
		}

		DownloadMapManager.addOnInitListener(new OnInitListener() {

			@Override
			public void initSuccess() {
			}
		});

		DownloadMapManager.addOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				AppLogic.removeBackGroundCallback(mParserDataSourceRunnable);
				AppLogic.runOnBackGround(mParserDataSourceRunnable, 0);
			}
		});
	}

	private int bottomHeight;

	private TextView createEmptyView() {
		if (mEmptyView == null) {
			mEmptyView = new TextView(AppLogic.getApp());
			mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
			mEmptyView.setGravity(Gravity.CENTER);
			mEmptyView.setMinHeight(bottomHeight);
			mEmptyView.setTextColor(Color.parseColor("#adb6cc"));
			mEmptyView.setText(AppLogic.getApp().getResources().getString(R.string.activity_download_no_record));
			mEmptyView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
					AbsListView.LayoutParams.MATCH_PARENT));
		}

		return mEmptyView;
	}

	Runnable mParserDataSourceRunnable = new Runnable() {

		@Override
		public void run() {
			analysisDataSource();
		}
	};

	public void analysisDataSource() {
		OfflineMapManager omm = DownloadMapManager.getInstance().getOfflineMapManager();
		if (omm == null) {
			return;
		}

		List<OfflineMapCity> omcList = omm.getDownloadingCityList();
		List<OfflineMapProvince> ompList = omm.getDownloadingProvinceList();
		List<OfflineMapCity> omcedList = omm.getDownloadOfflineMapCityList();
		List<OfflineMapProvince> ompedList = omm.getDownloadOfflineMapProvinceList();

		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				mDataList.clear();
			}
		}, 0);
		mDownloadNames.clear();
		for (OfflineMapCity o : omcList) {
			AppLogic.runOnUiGround(new Runnable1<OfflineMapCity>(o) {

				@Override
				public void run() {
					mDataList.add(mP1);
				}
			}, 0);
			mDownloadNames.add(o.getCity());
		}

		for (OfflineMapProvince o : ompList) {
			if (mDownloadNames.contains(o.getProvinceName())) {
				continue;
			}

			AppLogic.runOnUiGround(new Runnable1<OfflineMapProvince>(o) {

				@Override
				public void run() {
					mDataList.add(DownloadMapManager.getInstance().provinceToCity(mP1));
				}
			}, 0);
			mDownloadNames.add(o.getProvinceName());
		}

		for (OfflineMapCity o : omcedList) {
			AppLogic.runOnUiGround(new Runnable1<OfflineMapCity>(o) {

				@Override
				public void run() {
					mDataList.add(mP1);
				}
			}, 0);
			mDownloadNames.add(o.getCity());
		}

		for (OfflineMapProvince o : ompedList) {
			if (mDownloadNames.contains(o.getProvinceName())) {
				continue;
			}

			AppLogic.runOnUiGround(new Runnable1<OfflineMapProvince>(o) {

				@Override
				public void run() {
					mDataList.add(DownloadMapManager.getInstance().provinceToCity(mP1));
				}
			}, 0);
			mDownloadNames.add(o.getProvinceName());
		}

		AppLogic.removeUiGroundCallback(mRefreshViewRunnable);
		AppLogic.runOnUiGround(mRefreshViewRunnable, 0);
	}

	Runnable mRefreshViewRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				if (mAdapter == null) {
					mAdapter = new DownloadAdapter();
					mContentView.setAdapter(mAdapter);
				}

				if (mContentView != null) {
					if (mDataList != null && mDataList.size() < 1) {
						mContentView.setAdapter(null);
						mContentView.removeHeaderView(createEmptyView());
						mContentView.addHeaderView(createEmptyView());
						mContentView.setAdapter(mAdapter);
					} else {
						mContentView.removeHeaderView(createEmptyView());
					}
				}
				mAdapter.notifyDataSetChanged();
				isInit = true;
				procRefreshViewed();
			} catch (Exception e) {
				LogUtil.loge(e.toString());
			}
		}
	};

	private void procRefreshViewed() {

	}

	private class DownloadAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(AppLogic.getApp()).inflate(R.layout.download_item, null);
				((CityItemView) convertView).setMode(CityItemView.MODE_DOWNLOAD);
			}

			final OfflineMapCity omc = (OfflineMapCity) getItem(position);
			if (omc == null) {
				return convertView;
			}

			try {
				// DownloadMapManager.getInstance().getOfflineMapManager()
				// .updateOfflineCityByCode(omc.getCode());
			} catch (Exception e) {
				LogUtil.logd("OfflineMapCity update AMapException:" + e.toString());
			}

			if (!(convertView instanceof CityItemView)) {
				return convertView;
			}

			CityItemView civ = (CityItemView) convertView;
			civ.setOnMapListener(new OnMapListener() {

				@Override
				public void pauseDownload() {
					DownloadMapManager.getInstance().pause();
					notifyDataSetChanged();
				}

				@Override
				public void deleteDownload() {
					DownloadMapManager.getInstance().remove(omc.getCity());
					notifyDataSetChanged();
				}

				@Override
				public void continueDownload() {
					DownloadMapManager.getInstance().downloadMapCity(-1, omc.getCity(), null);
					notifyDataSetChanged();
				}

				@Override
				public void cancelDownload() {
					DownloadMapManager.getInstance().remove(omc.getCity());
					notifyDataSetChanged();
				}

				@Override
				public void beginDownload() {
					DownloadMapManager.getInstance().downloadMapCity(-1, omc.getCity(), null);
					notifyDataSetChanged();
				}
			});

			civ.setName(omc.getCity());
			civ.setCompleteCode(omc.getcompleteCode());

			int state = omc.getState();
			switch (state) {
			case OfflineMapStatus.LOADING:
				civ.setSize(omc.getcompleteCode() + "%");
				civ.setStatus(CityItemView.STATUS_DOWNLOADING);
				break;

			case OfflineMapStatus.PAUSE:
				civ.setSize(omc.getcompleteCode() + "%");
				civ.setStatus(CityItemView.STATUS_DOWNLOAD_PAUSE);
				break;

			case OfflineMapStatus.UNZIP:
				civ.setSize(omc.getSize());
				civ.setUnzip(omc.getcompleteCode());
				civ.setStatus(CityItemView.STATUS_UNZIP);
				break;

			case OfflineMapStatus.WAITING:
				civ.setStatus(CityItemView.STATUS_DOWNLOADING);
				civ.setSize("等待中");
				break;

			case OfflineMapStatus.SUCCESS:
				civ.setSize(omc.getSize());
				civ.setStatus(CityItemView.STATUS_DOWNLOADED);
				break;

			default:
				civ.setStatus(CityItemView.STATUS_DOWNLOADED);
				civ.setSize(omc.getSize());
				break;
			}

			return convertView;
		}
	}
}
