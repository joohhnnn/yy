package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.manager.NaviDataManager;
import com.txznet.nav.manager.MultiNavManager;
import com.txznet.nav.ui.widget.CircleImageView;

public class DistancePopWin {

	private View mParent;
	private View mContentView;
	private ListView mListView;
	private UserAdapter mAdapter;

	private List<String> mUserIds = new ArrayList<String>();

	private PopupWindow mPopupWindow;

	public DistancePopWin(View parent) {
		mParent = parent;
		mContentView = LayoutInflater.from(AppLogic.getApp()).inflate(
				R.layout.popupwin_layout, null);
		initWidget(mContentView);
	}

	private void initWidget(View view) {
		mListView = (ListView) view.findViewById(R.id.user_list_lv);
	}

	public void showPopupWin() {
		if (mPopupWindow == null) {
			int width = (int) AppLogic.getApp().getResources()
					.getDimension(R.dimen.x500);
			mPopupWindow = new PopupWindow(mContentView, width,
					LayoutParams.WRAP_CONTENT);
		}

		mUserIds = NaviDataManager.getInstance().getAllUserList();
		if (mUserIds == null || mUserIds.size() < 1) {
			return;
		}

		if (mAdapter == null) {
			mAdapter = new UserAdapter();
			mListView.setAdapter(mAdapter);
		}

		mAdapter.notifyDataSetChanged();

		if (mPopupWindow.isShowing()) {
			mPopupWindow.update();
		} else {
			mPopupWindow.showAtLocation(mParent, Gravity.LEFT
					| Gravity.CENTER_VERTICAL, 0, 20);
		}

		AppLogic.removeUiGroundCallback(update);
		AppLogic.runOnUiGround(update, 3000);
	}

	public boolean isShowing() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			return true;
		}

		return false;
	}

	public void dismiss() {
		if (mPopupWindow != null && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}

		AppLogic.removeUiGroundCallback(update);
	}

	Runnable update = new Runnable() {

		@Override
		public void run() {
			mAdapter.notifyDataSetChanged();
			AppLogic.runOnUiGround(update, 5000);
		}
	};

	private class UserAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mUserIds.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mHolder = null;
			if (convertView == null) {
				mHolder = new ViewHolder();
				convertView = LayoutInflater.from(AppLogic.getApp())
						.inflate(R.layout.popup_item_layout, null);

				mHolder.mCiv = (CircleImageView) convertView
						.findViewById(R.id.user_head_civ);
				mHolder.mNameTv = (TextView) convertView
						.findViewById(R.id.user_name_tv);
				mHolder.mDistanceTv = (TextView) convertView
						.findViewById(R.id.distance_tv);
				mHolder.mTimeTv = (TextView) convertView
						.findViewById(R.id.time_tv);

				mHolder.mCiv.setDrawCover(false);

				convertView.setTag(mHolder);
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}

			String uid = mUserIds.get(position);
			if (TextUtils.isEmpty(uid)) {
				return convertView;
			}

			final ViewHolder holder = mHolder;

			String imagepath = NaviDataManager.getInstance().getUserImagePath(uid);
			String nickName = NaviDataManager.getInstance().getUserNickName(uid);
			String distance = Math.round(NaviDataManager.getInstance()
					.getDistanceByUserId(uid) / 1000) + "";
			String time = NaviDataManager.getInstance().getTimeByUserId(uid) / 60
					+ "";
			if (uid.equals(String.valueOf(MultiNavManager.getInstance()
					.getMyId()))) {
				distance = Math.round(NaviDataManager.getInstance()
						.getRemainDistance() / 1000) + "";
				time = NaviDataManager.getInstance().getRemainTime() / 60 + "";
			}

			ImageLoader.getInstance().loadImage(imagepath,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							super.onLoadingComplete(imageUri, view, loadedImage);
							try {
								if (loadedImage != null) {
									holder.mCiv.setImageBitmap(loadedImage);
									notifyDataSetChanged();
								}
							} catch (Exception e) {
								LogUtil.loge(e.toString());
							}
						}
					});

			holder.mNameTv.setText(nickName);
			holder.mDistanceTv.setText(distance);
			holder.mTimeTv.setText(time);

			return convertView;
		}

		class ViewHolder {
			CircleImageView mCiv;
			TextView mNameTv;
			TextView mDistanceTv;
			TextView mTimeTv;
		}
	}
}