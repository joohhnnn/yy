package com.txznet.music.fragment.base;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v4.app.FragmentManager.BackStackEntry;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

	public final static String SONG_EXTRAS = "song_extras";
	public static String TAG = "[MUSIC][Fragment] ";
	protected View view;
	private FragmentTransaction transaction;

	protected View findViewById(int id) {
		return view.findViewById(id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onCreate");
		}
		if (getArguments() != null) {
			initArguments();
		}
	}

	public void initArguments() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onCreateView");
		}
		view = inflater.inflate(getLayout(), null);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]"
					+ "onActivityCreated");
		}
		bindViews();
		initData();
		initListener();
		reqData();
		
	}

	/**
	 * 请求数据
	 */
	public abstract void reqData();

	/**
	 * 初始化视图
	 */
	public abstract void bindViews();

	/**
	 * 初始化事件
	 */
	public abstract void initListener();

	/**
	 * 初始化数据
	 */
	public abstract void initData();

	/**
	 * fragment的布局
	 * 
	 * @return
	 */
	public abstract int getLayout();

	public synchronized void jumpToOtherFragment(String title,
			BaseFragment fragment, Intent intent) {

		FragmentManager supportFragmentManager = getActivity()
				.getFragmentManager();
		try {

			transaction = supportFragmentManager.beginTransaction();
			if (!fragment.isAdded()) {
				transaction.hide(this);
				// transaction.remove(fragment).commit();
				transaction.addToBackStack(title);
				transaction.add(R.id.content, fragment, title);
				if (null != intent) {
					Log.d("xxx传递给下一个页面", intent.getExtras().toString());
					fragment.setArguments(intent.getExtras());
				}
				transaction.commitAllowingStateLoss();
			} else {
				transaction.show(fragment).commit();
			}
		} catch (Exception e) {
			Log.d(TAG, title + "已存在");
			if (null != transaction) {
				transaction.remove(fragment);
			}
		}
	}

	public abstract int getFragmentId();

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onHiddenChanged("
					+ hidden + ")");
		}
		// TODO Auto-generated method stub
	}

	@Override
	public void onStart() {
		super.onStart();
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onStart");
		}
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onResume");
		}
		
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]"
					+ "onSaveInstanceState");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onPause");
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onStop");
		}
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]" + "onTrimMemory("
					+ level + ")");
		}
	}

	@Override
	public void onDestroy() {
		if (Constant.ISTEST) {
			LogUtil.logd(TAG + "[" + this.hashCode() + "]"+getFragmentId()+"/" + "onDestroy");
		}
//		if (getActivity() != null && !getActivity().isFinishing()) {
//			FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
//			beginTransaction.remove(this);
//			beginTransaction.commitAllowingStateLoss();
//		}
		super.onDestroy();
	}
	
}
