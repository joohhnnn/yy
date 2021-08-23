package com.txznet.music.historyModule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.music.R;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity3;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity4;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity5;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity6;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity8;

import java.util.Observable;
import java.util.concurrent.Callable;

import butterknife.Bind;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author telenewbie
 */
public class MineFragmentV42 extends BaseFragment implements OnClickListener {

    @Bind(R.id.rl_favour)
    RelativeLayout rlFavour;
    @Bind(R.id.rl_history)
    RelativeLayout rlHistory;
    @Bind(R.id.rl_subscribe)
    RelativeLayout rlSubscribe;
    @Bind(R.id.ll_message)
    LinearLayout llMessage;
    @Bind(R.id.ll_setting)
    LinearLayout llSetting;
    @Bind(R.id.red_dot)
    View redDot;

    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCompositeDisposable.clear();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.rl_favour:
                ReportEvent.clickMineBtn(1);
                intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity3.class);
                startActivity(intent);
                break;
            case R.id.rl_subscribe:
                ReportEvent.clickMineBtn(2);
                intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity4.class);
                startActivity(intent);

                break;
            case R.id.rl_history:
                ReportEvent.clickMineBtn(3);
                intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity5.class);
                startActivity(intent);
                break;
            case R.id.ll_message:
                ReportEvent.clickMineBtn(4);
                intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity8.class);
                startActivity(intent);
                break;
            case R.id.ll_setting:
                ReportEvent.clickMineBtn(5);
                intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity6.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void reqData() {

    }

    @Override
    public void bindViews() {
    }

    @Override
    public void initListener() {
        rlFavour.setOnClickListener(this);
        rlHistory.setOnClickListener(this);
        rlSubscribe.setOnClickListener(this);
        llMessage.setOnClickListener(this);
        llSetting.setOnClickListener(this);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public int getLayout() {
        if(ScreenUtils.isPhonePortrait()){
            return R.layout.fragment_history_v42_phone_portrait;
        }
        return R.layout.fragment_history_v42;
    }

    @Override
    public String getFragmentId() {
        return "MineFragmentV42#" + this.hashCode() + "/我的";
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {

                case InfoMessage.MESSAGE_CLEAR_UNREAD:
                    redDot.setVisibility(View.INVISIBLE);
                    ReportEvent.showReddot(2);
                    break;
                case InfoMessage.MESSAGE_NEW_UNREAD:
                    redDot.setVisibility(View.VISIBLE);
                    ReportEvent.showReddot(1);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkRedDot();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            checkRedDot();
        }
    }


    private void checkRedDot() {
        Disposable disposable = io.reactivex.Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return DBManager.getInstance().checkUnreadMessage();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            redDot.setVisibility(View.VISIBLE);
                        } else {
                            redDot.setVisibility(View.INVISIBLE);
                        }
                    }
                });

        mCompositeDisposable.add(disposable);
    }
}
