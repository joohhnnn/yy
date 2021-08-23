package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.nav.NavManager;
import com.txznet.nav.R;

public class HistoryActivity extends BaseActivity {

	private Button mBtnGoHome;
	private Button mBtnGoCompany;
	private ListView mListView;
	private WinConfirm mWinConfirm;
	private int mIntWhere;

	// 列表数据
	private List<Map<String, Object>> mList;

	// 列表显示要用的
	private static final String NAME = "name";
	private static final String ADDRESS = "address";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String GPSTYPE = "gpstype";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history);
		initView();
		initOnClick();
	}

	@Override
	protected void onStart() {
		super.onStart();
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

		refreshList();
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
	}

	private void initView() {
		mBtnGoHome = (Button) findViewById(R.id.btnGoHome);
		mBtnGoCompany = (Button) findViewById(R.id.btnGoCompany);
		mListView = (ListView) findViewById(android.R.id.list);
	}

	private void initOnClick() {
		mBtnGoHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isSetHome())
					showMyDialog(NavManager.LOCATION_HOME);
				else
					NavManager.getInstance().NavigateHome();
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
					NavManager.getInstance().NavigateCompany();
			}
		});
		mBtnGoCompany.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				showMyDialog(NavManager.LOCATION_COMPANY);
				return true;
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				onItemSelected(arg2);
			}

		});
	}

	public void refreshList() {
		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.activity_history_list, new String[] { NAME, ADDRESS },
				new int[] { R.id.name, R.id.address });
		if (mListView != null)
			mListView.setAdapter(adapter);
	}

	private boolean needRefreshList;
	
	public void setNeedRefreshList(boolean b) {
		needRefreshList = b;
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
		if (mList == null)
			return;
		if (index < 0 || index > mList.size())
			return;

		double latitude = (Double) (mList.get(index).get(LATITUDE));
		double longitude = (Double) (mList.get(index).get(LONGITUDE));
		String name = (String) mList.get(index).get(NAME);
		String address = (String) mList.get(index).get(ADDRESS);
		int gpsType = (Integer) mList.get(index).get(GPSTYPE);

		NavigateInfo info = new NavigateInfo();
		info.strTargetName = name;
		info.strTargetAddress = address;
		info.msgGpsInfo = new GpsInfo();
		info.msgGpsInfo.uint32GpsType = gpsType;
		info.msgGpsInfo.dblLat = latitude;
		info.msgGpsInfo.dblLng = longitude;
		NavManager.getInstance().NavigateTo(info);
	}

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
					NavManager.getInstance().startSetLocation(mIntWhere);
				}
			};
		}
		mWinConfirm.setMessage(msg);
		mWinConfirm.show();
	}

	private List<Map<String, Object>> getData() {

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		NavigateInfoList historyList = NavManager.getInstance()
				.getHistoryList();

		int count = 0;
		if (historyList != null && historyList.rptMsgItem != null)
			count = historyList.rptMsgItem.length;
		for (int i = 0; i < count; i++) {
			String name = historyList.rptMsgItem[i].strTargetName;
			String address = historyList.rptMsgItem[i].strTargetAddress;
			double latitude = historyList.rptMsgItem[i].msgGpsInfo.dblLat;
			double longitude = historyList.rptMsgItem[i].msgGpsInfo.dblLng;
			int gpsType = historyList.rptMsgItem[i].msgGpsInfo.uint32GpsType;

			Map<String, Object> map = new HashMap<String, Object>();
			map.put(NAME, name);
			map.put(ADDRESS, address);
			map.put(LATITUDE, latitude);
			map.put(LONGITUDE, longitude);
			map.put(GPSTYPE, gpsType);
			list.add(map);
		}

		mList = list;
		return list;
	}
}
