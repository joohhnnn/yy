package com.txznet.txz.ui.win.login;

import com.txznet.txz.R;
import com.txznet.txz.ui.win.BaseFragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public class SimCheckFragment extends BaseFragment<SimContract.Presenter> implements SimContract.View ,View.OnClickListener{

	private TextView mWifiTv;

	@Override
	public int getLayoutId() {
		return R.layout.fragment_sim_ly;
	}

	@Override
	protected void onViewCreated(View view) {
		super.onViewCreated(view);
		mWifiTv = (TextView) view.findViewById(R.id.go_wifi);
		mWifiTv.setOnClickListener(this);
	}

	@Override
	public SimContract.Presenter createPresenter() {
		return new SimPresenter();
	}

	@Override
	public void showNoSim() {
		findViewById(R.id.sim_status_bg).setBackgroundResource(R.drawable.icon_no_sim);
		((ImageView) findViewById(R.id.sim_status)).setImageDrawable(null);
		((TextView) findViewById(R.id.sim_txt)).setText(getString(R.string.string_no_sim));
	}

	@Override
	public void showSimDone() {
		findViewById(R.id.sim_status_bg).setBackgroundResource(R.drawable.icon_sim);
		((ImageView) findViewById(R.id.sim_status)).setImageResource(R.drawable.icon_done);
		((TextView) findViewById(R.id.sim_txt)).setText(getString(R.string.string_sim_done));
	}

	@Override
	public void showNetChecking() {
		findViewById(R.id.sim_status_bg).setBackgroundResource(R.drawable.icon_net);
		((ImageView) findViewById(R.id.sim_status)).setImageResource(R.drawable.icon_search);
		((TextView) findViewById(R.id.sim_txt)).setText(getString(R.string.string_net_check));
	}

	@Override
	public void showNetWell() {
		findViewById(R.id.sim_status_bg).setBackgroundResource(R.drawable.icon_net);
		((ImageView) findViewById(R.id.sim_status)).setImageResource(R.drawable.icon_done);
		((TextView) findViewById(R.id.sim_txt)).setText(getString(R.string.string_net_well));
	}

	@Override
	public void showNetError() {
		findViewById(R.id.sim_status_bg).setBackgroundResource(R.drawable.icon_net);
		((ImageView) findViewById(R.id.sim_status)).setImageResource(R.drawable.icon_warn);
		((TextView) findViewById(R.id.sim_txt)).setText(getString(R.string.string_net_error));
	}

	@Override
	public void onClick(View v) {
		LoginView.getInstance(getContext()).goWifiSettings();
	}
}
