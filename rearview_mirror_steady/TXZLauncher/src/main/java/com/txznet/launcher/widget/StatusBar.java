package com.txznet.launcher.widget;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.launcher.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ASUS User on 2018/5/29.
 * 界面最上面的状态栏界面，主要是显示网站、gps等状态
 */

public class StatusBar extends FrameLayout {


    @Bind(R.id.iv_status_ap)
    ImageView ivStatusAp;
    @Bind(R.id.iv_status_fm)
    ImageView ivStatusFm;
    @Bind(R.id.tv_status_signal)
    TextView tvStatusSignal;
    @Bind(R.id.iv_status_location)
    ImageView ivStatusLocation;
    @Bind(R.id.iv_status_signal)
    ImageView ivStatusSignal;

    private int[] mSignalId = {R.drawable.ic_status_signal_6, R.drawable.ic_status_signal_5, R.drawable.ic_status_signal_4,
            R.drawable.ic_status_signal_3, R.drawable.ic_status_signal_2, R.drawable.ic_status_signal_1};

    public StatusBar(@NonNull Context context) {
        super(context);
        initView();
    }

    public StatusBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public StatusBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public StatusBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_status_bar, this, true);
        ButterKnife.bind(this, this);

    }

    private void runOnUI(Runnable runnable){
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            removeCallbacks(runnable);
            post(runnable);
        }
    }

    /**
     * 刷新信号强度
     * @param signal
     */
    public void refreshSignalIcon(int signal) {
        mSignal = signal;
        runOnUI(refreshSignalIcon);
    }
    int mSignal = -1;
    private Runnable refreshSignalIcon = new Runnable() {
        @Override
        public void run() {
            if (mSignal >= 0 && mSignal < 6) {
                ivStatusSignal.setVisibility(VISIBLE);
                ivStatusSignal.setImageResource(mSignalId[mSignal]);
            } else {
                ivStatusSignal.setVisibility(GONE);
            }
        }
    };

    /**
     * 刷新网络类型
     * @param signal
     */
    public void refreshSignalText(String signal) {
        this.mSignalText = signal;
        runOnUI(refreshSignalText);
    }

    String mSignalText = "";
    private Runnable refreshSignalText = new Runnable() {
        @Override
        public void run() {
            if (TextUtils.isEmpty(mSignalText)) {
                tvStatusSignal.setVisibility(GONE);
            } else {
                tvStatusSignal.setVisibility(VISIBLE);
                tvStatusSignal.setText(mSignalText);
            }
        }
    };

    /**
     * 刷新定位开启状态
     * @param isOpen
     */
    public void refreshLocation(boolean isOpen) {
        mLocationIsOpen = isOpen;
        runOnUI(refreshLocation);
    }

    private boolean mLocationIsOpen = false;
    private Runnable refreshLocation = new Runnable() {
        @Override
        public void run() {
            ivStatusLocation.setVisibility(mLocationIsOpen ? VISIBLE : GONE);
        }
    };


    /**
     * 刷新热点状态
     * @param isOpen
     */
    public void refreshAP(boolean isOpen) {
        mAPIsOpen = isOpen;
        runOnUI(refreshAP);
    }
    private boolean mAPIsOpen = false;
    private Runnable refreshAP = new Runnable() {
        @Override
        public void run() {
            ivStatusAp.setVisibility(mAPIsOpen ? VISIBLE : GONE);
        }
    };

    /**
     * 刷新FM状态
     * @param isOpen
     */
    public void refreshFM(boolean isOpen) {
        mFMIsOpen = isOpen;
        runOnUI(refreshFM);
    }

    private boolean mFMIsOpen = false;
    private Runnable refreshFM = new Runnable() {
        @Override
        public void run() {
            ivStatusFm.setVisibility(mFMIsOpen ? VISIBLE : GONE);
        }
    };


}
