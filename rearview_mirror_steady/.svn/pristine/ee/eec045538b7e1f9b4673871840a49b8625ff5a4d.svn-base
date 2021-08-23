package com.txznet.txz.component.tmc;

import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.R;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.tmc.TMCManager.TrafficData;
import com.txznet.txz.module.tmc.TMCManager.TrafficData.TrafficStep;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrafficLightCross extends AbsLayout {
	private ImageView mHcIv;
	private TextView mMainTv;
	private TextView mDescTv;
	private LinearLayout mTrafficLayout;
	private TextView mStartNavBtn;
	private TextView mToastView;

	public TrafficLightCross(Context context) {
		super(context);
	}

	public void refreshView(TrafficData data) {
		mMainTv.setText(data.getTitle());
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(data.getTotalDistance());
		stringBuilder.append("   ");
		stringBuilder.append(data.getStatusDesc());
		mDescTv.setText(stringBuilder.toString());
		if (data.getIconType() == TrafficData.ICON_COMPANY) {
			mHcIv.setImageResource(R.drawable.traffic_icon_company);
		} else {
			mHcIv.setImageResource(R.drawable.traffic_icon_home);
		}
		if (data.getTrafficSteps() == null || data.getTrafficSteps().isEmpty()) {
			insertColorStatus("未知", 1);
			return;
		}

		for (TrafficStep step : data.getTrafficSteps()) {
			insertColorStatus(step.status, step.distance);
		}
	}
	
	public void showToastView() {
		mToastView.setVisibility(View.VISIBLE);
	}
	
	public void dismissToastView() {
		AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mToastView != null) {
					mToastView.setVisibility(View.GONE);
				}
			}
		});
		anim.setDuration(500);
		mToastView.startAnimation(anim);
	}

	private void insertColorStatus(String status, int weight) {
		int color = getColorByTrafficStatus(status);
		View v = new View(GlobalContext.get());
		v.setBackgroundColor(color);
		LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
		params.weight = weight;
		mTrafficLayout.addView(v, params);
	}

	@Override
	protected View getLayoutView() {
		View layout = View.inflate(getContext(), R.layout.tmc_spk_float_ly, null);
		mHcIv = (ImageView) layout.findViewById(R.id.hc_icon_iv);
		mMainTv = (TextView) layout.findViewById(R.id.main_tv);
		mDescTv = (TextView) layout.findViewById(R.id.desc_tv);
		mTrafficLayout = (LinearLayout) layout.findViewById(R.id.traffic_ly);
		mStartNavBtn = (TextView) layout.findViewById(R.id.start_nav);
		mStartNavBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FakeReqManager.getInstance().startNavi(false);
			}
		});
		mToastView = (TextView) layout.findViewById(R.id.toast_tv);
		return layout;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			JSONObject obj = FakeReqManager.getInstance().mRepoObj;
			if (obj != null) {
				try {
					String value = event.getX() + "," + event.getY();
					if (obj.has(FakeReqManager.KEY_TOUCH_XY)) {
						String touchXY = obj.optString(FakeReqManager.KEY_TOUCH_XY);
						obj.put(FakeReqManager.KEY_TOUCH_XY, touchXY + ";" + value);
					} else {
						obj.put(FakeReqManager.KEY_TOUCH_XY, value);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return super.onTouchEvent(event);
	}
}