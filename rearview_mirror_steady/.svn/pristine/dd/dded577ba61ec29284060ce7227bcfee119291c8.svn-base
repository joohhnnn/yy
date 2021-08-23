package com.txznet.music.fragment;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.ui.SingleActivity;

/**
 * 
 * 
 * @author telenewbie
 * 
 */
public class HistoryFragment extends BaseFragment implements OnClickListener, Observer {

	// 使用单例
	public static class MyInstance {
		public static HistoryFragment instance = new HistoryFragment();
	}

	// Content View Elements

	private static final String HISTORYMUSIC = "historyMusic";
	private static final String HISTORYRADIO = "historyRadio";

	private LinearLayout mLl_history_music;
	private TextView mTv_history_music;
	private View mLine_history_music;
	private LinearLayout mLl_history_radio;
	private TextView mTv_history_radio;
	private View mLine_history_radio;
	private FrameLayout mFl_history;

	
	private MusicHistoryFragment mMusicHistoryFragment;
	private RadioHistoryFragment mRadioHistoryFragment;
	
	// End Of Content View Elements

	@Override
	public void bindViews() {
		mLl_history_music = (LinearLayout) findViewById(R.id.ll_history_music);
		mTv_history_music = (TextView) findViewById(R.id.tv_history_music);
		mLine_history_music = (View) findViewById(R.id.line_history_music);
		mLl_history_radio = (LinearLayout) findViewById(R.id.ll_history_radio);
		mTv_history_radio = (TextView) findViewById(R.id.tv_history_radio);
		mLine_history_radio = (View) findViewById(R.id.line_history_radio);
		mFl_history = (FrameLayout) findViewById(R.id.fl_history);
	}

	View[] mViewList = new View[]{};
	
	private void setNavViewList(View[] views){
		if(!isHidden()){
			LogUtil.logd("NAVBtn:history fragment set views " + (views == null ? 0 : views.length));
			ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, views);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ObserverManage.getObserver().addObserver(this);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onDestroy() {
		ObserverManage.getObserver().deleteObserver(this);
		super.onDestroy();
	}
	
	@Override
	public void onResume() {
//		ObserverManage.getObserver().send(InfoMessage.ADD_VIEW_LIST, mViewList);
		setNavViewList(mViewList);
		super.onResume();
	}
	
	@Override
	public void onPause() {
		ObserverManage.getObserver().send(InfoMessage.DELETE_VIEW_LIST, mViewList);
		super.onPause();
	}
	
	@Override
	public void reqData() {

	}

	@Override
	public void initListener() {
		mLl_history_music.setOnClickListener(this);
		mLl_history_radio.setOnClickListener(this);
	}

	@Override
	public void initData() {
		changeFragment(1);
	}

	@Override
	public void onStart() {
//		((SingleActivity) getActivity()).jumpTypeFragment(R.id.fl_history,
//				MusicHistoryFragment.MyInstance.instance, HISTORYMUSIC);
		//统一使用show，hidden的形式
		super.onStart();
	}

	@SuppressLint("NewApi")
	private void changeFragment(int i) {
		FragmentTransaction fragmentTransaction=getChildFragmentManager().beginTransaction();
		hiddenAll(fragmentTransaction);	
		switch (i) {
		case 1://显示历史音乐
			if (mMusicHistoryFragment!=null) {
				fragmentTransaction.show(mMusicHistoryFragment);
			}else{
				mMusicHistoryFragment=new MusicHistoryFragment();
				fragmentTransaction.add(R.id.fl_history, mMusicHistoryFragment, HISTORYMUSIC);
			}
			break;
		case 2://显示历史电台
			if (mRadioHistoryFragment!=null) {
				fragmentTransaction.show(mRadioHistoryFragment);
			}else{
				mRadioHistoryFragment=new RadioHistoryFragment();
				fragmentTransaction.add(R.id.fl_history, mRadioHistoryFragment, HISTORYRADIO);
			}
			break;
		}
		fragmentTransaction.commitAllowingStateLoss();
		
	}

	/**
	 *隐藏全部Fragment
	 * @param fragmentTransaction 
	 */
	private void hiddenAll(FragmentTransaction fragmentTransaction) {
			if (mMusicHistoryFragment!=null) {
				fragmentTransaction.hide(mMusicHistoryFragment);
			}
			if (mRadioHistoryFragment!=null) {
				fragmentTransaction.hide(mRadioHistoryFragment);
			}
	}

	@Override
	public int getLayout() {
		return R.layout.fragment_history;
	}

	@Override
	public int getFragmentId() {
		return 3;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ll_history_radio:
			// 切换到电台界面
//			((SingleActivity) getActivity()).jumpTypeFragment(R.id.fl_history,
//					RadioHistoryFragment.MyInstance.instance, HISTORYRADIO);
			changeFragment(2);
			mLine_history_music.setVisibility(View.INVISIBLE);
			mLine_history_radio.setVisibility(View.VISIBLE);
			break;
		case R.id.ll_history_music:
			// 切换到音乐界面
//			((SingleActivity) getActivity()).jumpTypeFragment(R.id.fl_history,
//					MusicHistoryFragment.MyInstance.instance, HISTORYMUSIC);
			changeFragment(1);
			mLine_history_music.setVisibility(View.VISIBLE);
			mLine_history_radio.setVisibility(View.INVISIBLE);
			// 变色
			break;
		}
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		LogUtil.logd(TAG + "history onHiddenChanged:" + hidden);
		super.onHiddenChanged(hidden);
//		if (!hidden) {
//
//			if (RadioHistoryFragment.MyInstance.instance.isVisible()) {
//				RadioHistoryFragment.MyInstance.instance.onHiddenChanged(hidden);
//			}
//			if (MusicHistoryFragment.MyInstance.instance.isVisible()) {
//				MusicHistoryFragment.MyInstance.instance.onHiddenChanged(hidden);
//			}
//		}
		if (hidden) {
			setNavViewList(new View[] {});
		} else {
			setNavViewList(mViewList);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data instanceof InfoMessage) {
			InfoMessage info = (InfoMessage) data;
			switch (info.getType()) {
			case InfoMessage.ADD_HISTORY_VIEW_LIST:
				Object obj = info.getObj();
				if(obj != null && obj instanceof View[]){
					View[] views = (View[]) obj;
					mViewList = new View[]{mLl_history_music, mLl_history_radio, views[0]};
					setNavViewList(mViewList);
				}else{
					View[] views = (View[]) obj;
					mViewList = new View[]{mLl_history_music, mLl_history_radio};
					setNavViewList(mViewList);
				}
				break;

			case InfoMessage.DELETE_HISTORY_VIEW_LIST:
				break;
			default:
				break;
			}
		}
	}
}
