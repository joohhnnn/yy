package com.txznet.txz.module.tmc;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.tmc.AbsLayout;
import com.txznet.txz.component.tmc.TrafficLightCross;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.tmc.TMCManager.TrafficData.TrafficStep;
import com.txznet.txz.util.runnables.Runnable1;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Toast;

public class TMCManager {
	public static class TrafficData {
		public static final int ICON_COMPANY = 0;
		public static final int ICON_HOME = 1;

		public static class TrafficStep {
			public String status;
			public int distance;
			public String road;
		}

		private int resIconId;
		private String title;
		private String statusDesc;
		private String totalDistanceDesc;
		private List<TrafficStep> steps;

		public TrafficData(String distance, String status) {
			totalDistanceDesc = distance;
			statusDesc = status;
		}

		public void addStep(TrafficStep step) {
			if (steps == null) {
				steps = new ArrayList<TrafficStep>();
			}
			steps.add(step);
		}

		public void setIconType(int id) {
			resIconId = id;
		}

		public int getIconType() {
			return resIconId;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return this.title;
		}

		public String getTotalDistance() {
			return totalDistanceDesc;
		}

		public String getStatusDesc() {
			return statusDesc;
		}

		public List<TrafficStep> getTrafficSteps() {
			return steps;
		}
	}

	private static TMCManager sManager = new TMCManager();

	private AbsLayout mCurrNotiLayout;

	public static TMCManager getInstance() {
		return sManager;
	}

	/**
	 * 解析数据并显示弹窗
	 * 
	 * @param hc
	 *            （TrafficData.ICON_HOME，TrafficData.ICON_COMPANY）
	 * @param trafficData
	 */
	public void notifyTrafficDialog(String title, JSONObject jsonObject) {
		if (jsonObject != null) {
			String total = null;
			String status = null;
			TrafficData td = null;
			int target = 0;
			if (jsonObject.has("total")) {
				total = jsonObject.optString("total");
			}
			if (jsonObject.has("traffic")) {
				status = jsonObject.optString("traffic");
			}
			if (jsonObject.has("target")) {
				target = jsonObject.optInt("target");
			}
			td = new TrafficData(total, status);
			td.setTitle(title);
			td.setIconType(target);
			if (jsonObject.has("data")) {
				JSONArray jsonArray = jsonObject.optJSONArray("data");
				if (jsonArray != null && jsonArray.length() > 0) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject object = (JSONObject) jsonArray.opt(i);
						if (object != null) {
							TrafficStep step = new TrafficStep();
							if (object.has("status")) {
								step.status = object.optString("status");
							}
							if (object.has("distance")) {
								step.distance = object.optInt("distance");
							}
							if (object.has("road")) {
								step.road = object.optString("road");
							}
							td.addStep(step);
						}
					}
				}
			}
			notifyTrafficDialog(td);
		}
	}

	public void notifyTrafficDialog(TrafficData data) {
		if (mCurrNotiLayout != null && mCurrNotiLayout instanceof TrafficLightCross && mCurrNotiLayout.isShowing()) {
			((TrafficLightCross) mCurrNotiLayout).refreshView(data);
			return;
		} else if (mCurrNotiLayout != null && mCurrNotiLayout.isShowing()) {
			mCurrNotiLayout.dismiss();
		}

		mCurrNotiLayout = new TrafficLightCross(GlobalContext.get());
		mCurrNotiLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				mCurrNotiLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				View view = mCurrNotiLayout.findViewById(R.id.float_ly);
				JSONObject obj = FakeReqManager.getInstance().mRepoObj;
				if (obj != null) {
					try {
						obj.put(FakeReqManager.KEY_WIDGET_WIDTH_HEIGHT, view.getWidth() + "," + view.getHeight());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		});
		((TrafficLightCross) mCurrNotiLayout).refreshView(data);
		mCurrNotiLayout.open();
		if (FakeReqManager.getInstance().enableToast()) {
			FakeReqManager.getInstance().incToastCount();
			((TrafficLightCross) mCurrNotiLayout).showToastView();
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					if (mCurrNotiLayout != null) {
						((TrafficLightCross) mCurrNotiLayout).dismissToastView();
					}
				}
			}, 5 * 1000);
		}
	}

	public void dismiss() {
		if (mCurrNotiLayout != null && mCurrNotiLayout.isShowing()) {
			mCurrNotiLayout.dismiss();
			mCurrNotiLayout = null;
		}
	}
}