package com.txznet.music.ui.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragmentFixed;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.GlideApp;
import com.txznet.music.R;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ProgramUtils;
import com.txznet.music.widget.dialog.ExitAppDialog;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class BaseFragment extends DialogFragmentFixed {
    public static String TAG = Constant.LOG_TAG_FRAGMENT;

    @Nullable
    @Bind(R.id.ll_back)
    protected ViewGroup llBack;

    @Nullable
    @Bind(R.id.iv_back)
    protected ImageView ivBack;

    @Nullable
    @Bind(R.id.iv_close)
    protected ImageView ivClose;

    @Bind(R.id.tv_title)
    @Nullable
    protected TextView tvTitle;
    @Bind(R.id.tv_sub_title)
    @Nullable
    protected TextView tvSubTitle;
    private ExitAppDialog mExitAppDialog;

    protected abstract int getLayout();

    protected abstract void initView(View view);

    protected abstract void initData(Bundle savedInstanceState);


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onCreateView");
        View view = inflater.inflate(getLayout(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        initData(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        Logger.d(TAG, getClass().getSimpleName() + ":onDestroyView");
        ButterKnife.unbind(this);
        ivBack = null;
        ivClose = null;
        llBack = null;
        tvTitle = null;
        tvSubTitle = null;
        if (mExitAppDialog != null) {
            mExitAppDialog.dismiss();
        }
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onCreateDialog");
        setStyle(STYLE_NO_TITLE, R.style.Dialog_FullScreen);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        Logger.d(TAG, getClass().getSimpleName() + ":onStart");
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        Logger.d(TAG, getClass().getSimpleName() + ":onAttach");
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        Logger.d(TAG, getClass().getSimpleName() + ":onDetach");
        super.onDetach();
        DialogFragmentStack.get().pop(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Logger.d(TAG, getClass().getSimpleName() + ":onCancel");
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, getClass().getSimpleName() + ":onDismiss");
        }

        super.onDismiss(dialog);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, getClass().getSimpleName() + ":onSaveInstanceState");
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        Logger.d(TAG, getClass().getSimpleName() + ":onStop");
        GlideApp.with(this).pauseRequestsRecursive();
        GlideApp.with(this).pauseAllRequests();
        super.onStop();
    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d(TAG, getClass().getSimpleName() + ":onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        Logger.d(TAG, getClass().getSimpleName() + ":onAttachFragment");
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Logger.d(TAG, getClass().getSimpleName() + ":onViewStateRestored");
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        Logger.d(TAG, getClass().getSimpleName() + ":onResume");
        super.onResume();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.d(TAG, getClass().getSimpleName() + ":onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        Logger.d(TAG, getClass().getSimpleName() + ":onPause");
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        Logger.d(TAG, getClass().getSimpleName() + ":onLowMemory");
        super.onLowMemory();
    }

    @Nullable
    @OnClick({R.id.ll_back, R.id.iv_back, R.id.iv_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
            case R.id.iv_back:
                dismissAllowingStateLoss();
                break;
            case R.id.iv_close:
                FragmentActivity activity = getActivity();
                if (activity != null && !activity.isDestroyed()) {
                    if (mExitAppDialog == null) {
                        mExitAppDialog = new ExitAppDialog(activity);
                        mExitAppDialog.setClickCallback(new ExitAppDialog.ExitAppCallback(activity, mExitAppDialog));
                    }
                    mExitAppDialog.show();
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, getClass().getSimpleName() + ":onViewCreated:" + activity + ", " + (activity.isDestroyed()));
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, getClass().getSimpleName() + ":onDestroy");
        super.onDestroy();
        if (!ProgramUtils.isProgram() && BuildConfig.DEBUG) {
            if (AppLogic.getRefWatcher() != null) {
                AppLogic.getRefWatcher().watch(this);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, getClass().getSimpleName() + ":onCreateOptionsMenu");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    private static long mLastShowTime;

    @Override
    public void show(FragmentManager manager, String tag) {
        long now = SystemClock.elapsedRealtime();
        long offset = now - mLastShowTime;
        mLastShowTime = SystemClock.elapsedRealtime();
        if (offset < 600) {
            return;
        }
        if (!DialogFragmentStack.get().isHadShow(this)) {
            DialogFragmentStack.get().push(this);
            super.show(manager, tag);
        }
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        DialogFragmentStack.get().push(this);
        return super.show(transaction, tag);
    }

    @Override
    public void showNow(FragmentManager manager, String tag) {
        DialogFragmentStack.get().push(this);
        super.showNow(manager, tag);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss();
    }
}
