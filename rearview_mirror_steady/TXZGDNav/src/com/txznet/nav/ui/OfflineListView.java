package com.txznet.nav.ui;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.offline.DownloadMapManager;
import com.txznet.nav.offline.DownloadMapManager.OnInitListener;
import com.txznet.nav.offline.DownloadMapManager.OnRefreshListener;
import com.txznet.nav.offline.IOffline.DownloadListener;
import com.txznet.nav.ui.widget.CityItemView;
import com.txznet.nav.ui.widget.CityItemView.OnMapListener;

public class OfflineListView {

	private ExpandAdapter mExpandAdapter;
	private ExpandableListView mContentView;
	private ProgressBar mProgressBar;

	private boolean[] mGroupIsOpen = null;

	private static OfflineListView instance = new OfflineListView();

	private OfflineListView() {
	}

	public static OfflineListView getInstance() {
		return instance;
	}

	public void init(View view) {
		mContentView = (ExpandableListView) view.findViewById(R.id.list);
		mProgressBar = (ProgressBar) view
				.findViewById(R.id.prgProgress_Progress);
		if (DownloadMapManager.getInstance().mHasInited) {
			mProgressBar.setVisibility(View.GONE);
			initAfterResourceDone();
		} else {
			mProgressBar.setVisibility(View.VISIBLE);
		}

		DownloadMapManager.addOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						if(mExpandAdapter != null){
							mExpandAdapter.notifyDataSetChanged();
							if (mProgressBar != null) {
								mProgressBar.setVisibility(View.GONE);
							}
						}
					}
				}, 0);
			}
		});

		DownloadMapManager.addOnInitListener(new OnInitListener() {

			@Override
			public void initSuccess() {
				initExpandableListView();
			}
		});
	}

	private void initExpandableListView() {
		mContentView.setGroupIndicator(null);
		mContentView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				mGroupIsOpen[groupPosition] = false;
			}
		});

		mContentView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				mGroupIsOpen[groupPosition] = true;
			}
		});

		mContentView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				List<OfflineMapCity> omcs = DownloadMapManager.getInstance()
						.getOfflineMapCities(groupPosition);
				if (omcs == null || omcs.size() <= childPosition) {
					return false;
				}

				OfflineMapCity omc = omcs.get(childPosition);
				if (omc == null) {
					return false;
				}

				String name = omc.getCity();
				String cityUrl = DownloadMapManager.getInstance()
						.getOfflineMapManager().getItemByCityName(name)
						.getUrl();
				LogUtil.logd("点击了:" + name + ",url:" + cityUrl);
				switch (groupPosition) {
				case 0:
				case 1:
				case 2:
					DownloadMapManager.getInstance().downloadMapProvince(
							groupPosition, name, new DownloadListener() {

								@Override
								public void onCityStatus(int status,
										int completeCode, String name) {
								}
							});
					break;

				default:
					if (childPosition == 0) {
						// 下载省份的数据
						String provName = DownloadMapManager.getInstance()
								.getProvinces().get(groupPosition)
								.getProvinceName();
						DownloadMapManager.getInstance().downloadMapProvince(
								groupPosition, provName,
								new DownloadListener() {

									@Override
									public void onCityStatus(int status,
											int completeCode, String name) {
									}
								});
					} else if (childPosition > 0) {
						DownloadMapManager.getInstance().downloadMapCity(
								groupPosition, name, new DownloadListener() {

									@Override
									public void onCityStatus(int status,
											int completeCode, String name) {
									}
								});
					}
					break;
				}
				return false;
			}
		});

		// isOpen = new boolean[DownloadMapManager.getInstance().getProvinces()
		// .size()];
		// mExpandAdapter = new ExpandAdapter();
		// mContentView.setAdapter(mExpandAdapter);
		initAfterResourceDone();
	}

	private void initAfterResourceDone() {
		if (mProgressBar != null) {
			mProgressBar.setVisibility(View.GONE);
		}

		List<OfflineMapProvince> proList = DownloadMapManager.getInstance()
				.getProvinces();
		int size = 0;
		if (proList != null) {
			size = proList.size();
		}

		mGroupIsOpen = new boolean[size];
		if (mExpandAdapter != null) {
			mExpandAdapter = null;
		}

		if (mExpandAdapter == null) {
			mExpandAdapter = new ExpandAdapter();
			mContentView.setAdapter(mExpandAdapter);
		}

		mExpandAdapter.notifyDataSetChanged();
	}

	private class ExpandAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {
			List<OfflineMapProvince> ompList = DownloadMapManager.getInstance()
					.getProvinces();
			if (ompList == null || ompList.size() < 1) {
				return 0;
			}
			return ompList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			List<OfflineMapCity> omcList = DownloadMapManager.getInstance()
					.getOfflineMapCities(groupPosition);
			if (omcList == null || omcList.size() < 1) {
				return 0;
			}
			return omcList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			List<OfflineMapProvince> ompList = DownloadMapManager.getInstance()
					.getProvinces();
			if (ompList == null || ompList.size() < 1
					|| ompList.size() <= groupPosition) {
				return groupPosition;
			}

			OfflineMapProvince omp = ompList.get(groupPosition);
			if (omp == null) {
				return groupPosition;
			}

			return omp.getProvinceName();
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			List<OfflineMapCity> omcList = DownloadMapManager.getInstance()
					.getOfflineMapCities(groupPosition);
			if (omcList == null || omcList.size() <= childPosition) {
				return null;
			}

			return omcList.get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(AppLogic.getApp()).inflate(
						R.layout.offline_group_layout, null);
			}

			TextView tv = (TextView) convertView;
			String provName = DownloadMapManager.getInstance().getProvinces()
					.get(groupPosition).getProvinceName();
			tv.setText(provName);
			if (mGroupIsOpen[groupPosition]) {
				Drawable right = AppLogic.getApp().getResources()
						.getDrawable(R.drawable.feedback_arrow2);
				right.setBounds(0, 0, right.getIntrinsicWidth(),
						right.getIntrinsicHeight());
				tv.setCompoundDrawables(null, null, right, null);
			} else {
				Drawable right = AppLogic.getApp().getResources()
						.getDrawable(R.drawable.feedback_arrow1);
				right.setBounds(0, 0, right.getIntrinsicWidth(),
						right.getIntrinsicHeight());
				tv.setCompoundDrawables(null, null, right, null);
			}
			return convertView;
		}

		@Override
		public View getChildView(final int groupPosition,
				final int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(AppLogic.getApp()).inflate(
						R.layout.download_item, null);
				((CityItemView) convertView).setMode(CityItemView.MODE_OFFLINE);
			}

			List<OfflineMapCity> omcList = DownloadMapManager.getInstance()
					.getOfflineMapCities(groupPosition);
			if (omcList == null || omcList.size() <= childPosition) {
				return convertView;
			}

			OfflineMapCity omc = omcList.get(childPosition);

			// 获取最新的状态
			if (groupPosition == 0 || groupPosition == 1 || groupPosition == 2) {
				omc = DownloadMapManager.getInstance().getOfflineMapManager()
						.getItemByCityName(omc.getCity());
			} else {
				if (childPosition == 0) {
					OfflineMapProvince aMapProvince = DownloadMapManager
							.getInstance().getOfflineMapManager()
							.getItemByProvinceName(omc.getCity());
					omc = DownloadMapManager.getInstance().provinceToCity(
							aMapProvince);
				} else {
					omc = DownloadMapManager.getInstance()
							.getOfflineMapManager()
							.getItemByCityName(omc.getCity());
				}
			}
			if (omc == null) {
				return convertView;
			}

			final OfflineMapCity omCity = omc;
			LogUtil.logd("OfflineListView getChildView -- > city:"
					+ omc.getCity() + ",state:" + omc.getState()
					+ ",completeCode:" + omc.getcompleteCode());

			CityItemView civ = (CityItemView) convertView;
			civ.setOnMapListener(new OnMapListener() {

				@Override
				public void pauseDownload() {
					DownloadMapManager.getInstance().pause();
					notifyDataSetChanged();
				}

				@Override
				public void deleteDownload() {
					DownloadMapManager.getInstance().remove(omCity.getCity());
					notifyDataSetChanged();
				}

				@Override
				public void continueDownload() {
					DownloadMapManager.getInstance().downloadMapCity(
							groupPosition, omCity.getCity(), null);
					notifyDataSetChanged();
				}

				@Override
				public void cancelDownload() {
					DownloadMapManager.getInstance().remove(omCity.getCity());
					notifyDataSetChanged();
				}

				@Override
				public void beginDownload() {
					ConnectivityManager cm = (ConnectivityManager) AppLogic
							.getApp().getSystemService(
									Context.CONNECTIVITY_SERVICE);
					NetworkInfo mActiveInfo = cm.getActiveNetworkInfo();
					if (mActiveInfo == null) {
						// 没有连接网络
						Toast.makeText(AppLogic.getApp(), "未连接网络！",
								Toast.LENGTH_LONG).show();
						return;
					}

					LogUtil.logd("开始下载 -- 》" + omCity.getCity());
					// 开始下载
					if (groupPosition == 0 || groupPosition == 1
							|| groupPosition == 2) {
						DownloadMapManager.getInstance().downloadMapProvince(
								groupPosition, omCity.getCity(), null);
					} else {
						if (childPosition == 0) {
							String provName = DownloadMapManager.getInstance()
									.getProvinces().get(groupPosition)
									.getProvinceName();
							DownloadMapManager.getInstance()
									.downloadMapProvince(groupPosition,
											provName, null);
						} else {
							DownloadMapManager.getInstance().downloadMapCity(
									groupPosition, omCity.getCity(), null);
						}
					}
					notifyDataSetChanged();
				}
			});

			civ.setName(omc.getCity());
			civ.setCompleteCode(omc.getcompleteCode());

			int state = omc.getState();
			if (state == OfflineMapStatus.SUCCESS) {
				civ.setSize(omc.getSize());
				civ.setStatus(CityItemView.STATUS_DOWNLOADED);
			} else if (state == OfflineMapStatus.LOADING) {
				civ.setStatus(CityItemView.STATUS_DOWNLOADING);
				civ.setSize(omc.getcompleteCode() + "%");
			} else if (state == OfflineMapStatus.WAITING) {
				civ.setStatus(CityItemView.STATUS_DOWNLOADING);
				civ.setSize("等待中");
			} else if (state == OfflineMapStatus.UNZIP) {
				civ.setSize(omc.getSize());
				civ.setStatus(CityItemView.STATUS_UNZIP);
				civ.setUnzip(omc.getcompleteCode());
			} else if (state == OfflineMapStatus.PAUSE) {
				civ.setStatus(CityItemView.STATUS_DOWNLOAD_PAUSE);
				civ.setSize(omc.getcompleteCode() + "%");
			} else {
				civ.setStatus(CityItemView.STATUS_NO_DOWNLOAD);
				civ.setSize(omc.getSize());
			}

			return civ;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
