package com.txznet.music.baseModule.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.baseModule.Constant;

import java.util.Observer;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements Observer {

    public final static String SONG_EXTRAS = "song_extras";
    public static String TAG = "Music:Fragment:";
    protected View view;
    private FragmentTransaction transaction;

    protected View findViewById(int id) {
        return view.findViewById(id);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ObserverManage.getObserver().addObserver(this);
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onCreate");
        }
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            initArguments();
        }
    }

    public void initArguments() {
        LogUtil.logd(TAG + " super Arguments :");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onCreateView");
        }
        view = inflater.inflate(getLayout(), null);
        ButterKnife.bind(this, view);
        bindViews();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]"
                    + "onActivityCreated");
        }

        initData();
        initListener();
        reqData();
        super.onActivityCreated(savedInstanceState);
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


    public abstract String getFragmentId();

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onHiddenChanged("
                    + hidden + ")");
        }
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStart() {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onStart");
        }
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onResume() {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onResume");
        }
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]"
                    + "onSaveInstanceState");
        }
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onPause");
        }
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onStop() {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onStop");
        }
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    public void onTrimMemory(int level) {
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onTrimMemory("
                    + level + ")");
        }
        // TODO Auto-generated method stub
        super.onTrimMemory(level);
    }

    @Override
    public void onDestroy() {
        ObserverManage.getObserver().deleteObserver(this);

        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onDestroy");
        }
        AppLogic.getRefWatcher().watch(this);
        if (getActivity() != null && !getActivity().isFinishing()) {
            FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
            beginTransaction.remove(this);
            beginTransaction.commitAllowingStateLoss();
        }
        super.onDestroy();
    }


    /**
     * 是否需要拦截返回操作
     *
     * @return true 拦截
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onDetach");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onAttach");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "onDestroyView");
        }
        ButterKnife.unbind(this);
    }
}
