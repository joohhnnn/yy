//package com.txznet.music.historyModule.ui;
//
//import android.annotation.SuppressLint;
//import android.app.FragmentTransaction;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewTreeObserver;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.txznet.fm.bean.InfoMessage;
//import com.txznet.fm.manager.ObserverManage;
//import com.txznet.music.R;
//import com.txznet.music.baseModule.ui.BaseFragment;
//
//import java.util.Observable;
//
///**
// * @author telenewbie
// */
//public class MineFragment extends BaseFragment implements OnClickListener {
//
//	private static final String HISTORYMUSIC = "historyMusic";
//	private static final String HISTORYRADIO = "historyRadio";
//	private static final String SETTING = "setting";
//	private LinearLayout mLl_history_title;
//
//	// Content View Elements
//	private Resources mRes;
//	private RelativeLayout mRlHistoryMusic;
//	private TextView mTvHistoryMusic;
//	private ImageView mIvHistoryMusicSelect;
//
//	private RelativeLayout mRlHistoryRadio;
//	private TextView mTvHistoryRadio;
//	private ImageView mIvHistoryRadioSelect;
//
//	private RelativeLayout mRlSetting;
//	private TextView mTvSetting;
//	private ImageView mIvSettingSelect;
//
//	private MusicHistoryFragment mMusicHistoryFragment;
//	private RadioHistoryFragment mRadioHistoryFragment;
//	private SettingFragment mSettingFragment;
//
//
//
//
//
//    @Override
//	public void update(Observable observable, Object data) {
//		if (data instanceof InfoMessage) {
//			InfoMessage info = (InfoMessage) data;
//			switch (info.getType()) {
//                default:
//					break;
//			}
//		}
//	}
//
//	// End Of Content View Elements
//
//	@Override
//	public void bindViews() {
//		mRes = getActivity().getResources();
//
//		mRlHistoryMusic = (RelativeLayout) findViewById(R.id.rl_history_music);
//		mTvHistoryMusic = (TextView) findViewById(R.id.tv_history_music);
//		mIvHistoryMusicSelect = (ImageView) findViewById(R.id.iv_history_music_select);
//
//		mRlHistoryRadio = (RelativeLayout) findViewById(R.id.rl_history_radio);
//		mTvHistoryRadio = (TextView) findViewById(R.id.tv_history_radio);
//		mIvHistoryRadioSelect = (ImageView) findViewById(R.id.iv_history_radio_select);
//
//		mRlSetting = (RelativeLayout) findViewById(R.id.rl_history_setting);
//        mTvSetting = (TextView) findViewById(R.id.tv_setting);
//		mIvSettingSelect = (ImageView) findViewById(R.id.iv_setting_select);
//
//		mTvHistoryMusic.setText(mRes.getString(R.string.history_title_music));
//		mTvHistoryRadio.setText(mRes.getString(R.string.history_title_radio));
//	}
//
//
//
//
//	@Override
//	public void onDestroy() {
//		ObserverManage.getObserver().deleteObserver(this);
//		super.onDestroy();
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//	}
//
//	@Override
//	public void reqData() {
//
//	}
//
//	@Override
//	public void initListener() {
//		mRlHistoryMusic.setOnClickListener(this);
//		mRlHistoryRadio.setOnClickListener(this);
//		mRlSetting.setOnClickListener(this);
//	}
//
//	@Override
//	public void initData() {
//		mTvHistoryMusic.setBackground(mRes.getDrawable(R.drawable.mine_title_btn_bg));
//		mTvHistoryRadio.setBackground(mRes.getDrawable(R.drawable.mine_title_btn_bg));
//		mTvSetting.setBackground(mRes.getDrawable(R.drawable.mine_title_btn_bg));
//		changeFragment(FRAGMENT_MUSIC);
//		setTitleSelected(mRlHistoryMusic);
//	}
//
//	@Override
//	public void onStart() {
////		((SingleActivity) getActivity()).jumpTypeFragment(R.id.fl_history,
////				MusicHistoryFragment.MyInstance.instance, HISTORYMUSIC);
//		//统一使用show，hidden的形式
//		super.onStart();
//	}
//
//	public final int FRAGMENT_MUSIC = 1;
//	public final int FRAGMENT_RADIO = 2;
//	public final int FRAGMENT_SETTING = 3;
//
//	@SuppressLint("NewApi")
//	private void changeFragment(int i) {
//		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//		hiddenAll(fragmentTransaction);
//		switch (i) {
//			case FRAGMENT_MUSIC://显示历史音乐
//				if (mMusicHistoryFragment != null) {
//					fragmentTransaction.show(mMusicHistoryFragment);
//				} else {
//					mMusicHistoryFragment = new MusicHistoryFragment();
//					fragmentTransaction.add(R.id.fl_history, mMusicHistoryFragment, HISTORYMUSIC);
//				}
//				break;
//			case FRAGMENT_RADIO://显示历史电台
//				if (mRadioHistoryFragment != null) {
//					fragmentTransaction.show(mRadioHistoryFragment);
//				} else {
//					mRadioHistoryFragment = new RadioHistoryFragment();
//					fragmentTransaction.add(R.id.fl_history, mRadioHistoryFragment, HISTORYRADIO);
//				}
//				break;
//			case FRAGMENT_SETTING:
//				if (mSettingFragment != null) {
//					fragmentTransaction.show(mSettingFragment);
//				} else {
//					mSettingFragment = new SettingFragment();
//					fragmentTransaction.add(R.id.fl_history, mSettingFragment, SETTING);
//				}
//				break;
//		}
//		fragmentTransaction.commitAllowingStateLoss();
//	}
//
//
//
//	/**
//	 * 隐藏全部Fragment
//	 *
//	 * @param fragmentTransaction
//	 */
//	private void hiddenAll(FragmentTransaction fragmentTransaction) {
//		if (mMusicHistoryFragment != null) {
//			fragmentTransaction.hide(mMusicHistoryFragment);
//		}
//		if (mRadioHistoryFragment != null) {
//			fragmentTransaction.hide(mRadioHistoryFragment);
//		}
//		if (mSettingFragment != null) {
//			fragmentTransaction.hide(mSettingFragment);
//		}
//	}
//
//	@Override
//	public int getLayout() {
//		return R.layout.fragment_history;
//	}
//
//	@Override
//	public String getFragmentId() {
//		return "MineFragment#"+this.hashCode()+"/我的";
//	}
//
//	@Override
//	public void onClick(View v) {
//
//		switch (v.getId()) {
//			case R.id.rl_history_radio:
//				// 切换到电台界面
//				changeFragment(FRAGMENT_RADIO);
//				setTitleSelected(mRlHistoryRadio);
//				break;
//			case R.id.rl_history_music:
//				// 切换到音乐界面
//				changeFragment(FRAGMENT_MUSIC);
//				setTitleSelected(mRlHistoryMusic);
//				// 变色
//				break;
//			case R.id.rl_history_setting:
//				changeFragment(FRAGMENT_SETTING);
//				setTitleSelected(mRlSetting);
//				break;
//		}
//	}
//
//	public void setTitleSelected(View view) {
//		mRlHistoryMusic.setSelected(false);
//		mRlHistoryRadio.setSelected(false);
//		mRlSetting.setSelected(false);
//
//		view.setSelected(true);
//	}
//
//
//	@Override
//	public void onHiddenChanged(boolean hidden) {
//		super.onHiddenChanged(hidden);
//	}
//
//	@Override
//	public void onSaveInstanceState(Bundle outState) {
//	}
//
//}
