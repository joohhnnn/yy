package com.txznet.music.historyModule.ui;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.ReportEventConst;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.utils.ScreenUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by telenewbie on 2017/12/15.
 */

public class MyHistoryActivity extends BaseBarActivity {
    private static final String HISTORYMUSIC = "historyMusic";
    private static final String HISTORYRADIO = "historyRadio";
    public final int FRAGMENT_MUSIC = 0;
    public final int FRAGMENT_RADIO = 1;
    @Bind(R.id.ll_left_back)
    LinearLayout llLeftBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_title_2)
    TextView tvTitle2;
    @Bind(R.id.iv_delete)
    ImageView ivDelete;
    @Bind(R.id.prl_title)
    PercentRelativeLayout prlTitle;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;
    private Fragment mMusicHistoryFragment;
    private Fragment mRadioHistoryFragment;
    private int currentId = -1;

    @Override
    protected String getActivityTag() {
        return "MyHistoryActivity#" + this.hashCode() + "/我的历史";
    }

    @Override
    public int getLayout() {
        if(ScreenUtils.isPhonePortrait()){
            return R.layout.act_history_phone_portrait;
        }
        return R.layout.act_history;
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        tvTitle.setText("历史音乐");
        tvTitle2.setVisibility(View.VISIBLE);
        tvTitle2.setText("历史电台");
        ivDelete.setVisibility(View.GONE);
        int position = 0;
        if (null != savedInstanceState) {
            position = savedInstanceState.getInt("position", 0);
        }
        if (position != FRAGMENT_MUSIC && position != FRAGMENT_RADIO) {
            position = FRAGMENT_MUSIC;
        }

        //定位到第一个页面
        changeFragment(position);
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    @SuppressLint("NewApi")
    private void changeFragment(int i) {
        if (currentId == i) {//如果点击已经在的分类则不响应
            return;
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        hiddenAll(fragmentTransaction);
        switch (i) {
            case FRAGMENT_MUSIC://显示历史音乐
                ReportEvent.reportCommEvent(ReportEventConst.CLICK_HISTORY_MUSIC_TAB);
                if (mMusicHistoryFragment != null) {
                    fragmentTransaction.show(mMusicHistoryFragment);
                } else {
//                    mMusicHistoryFragment = new MusicHistoryFragment();
                    mMusicHistoryFragment = HistoryFragment.newInstance(HistoryContract.TYPE_MUSIC);
                    fragmentTransaction.add(R.id.fl_history, mMusicHistoryFragment, HISTORYMUSIC);
                }
                break;
            case FRAGMENT_RADIO://显示历史电台
                ReportEvent.reportCommEvent(ReportEventConst.CLICK_HISTORY_RADIO_TAB);
                if (mRadioHistoryFragment != null) {
                    fragmentTransaction.show(mRadioHistoryFragment);
                } else {
//                    mRadioHistoryFragment = new RadioHistoryFragment();
                    mRadioHistoryFragment = HistoryFragment.newInstance(HistoryContract.TYPE_RADIO);
                    fragmentTransaction.add(R.id.fl_history, mRadioHistoryFragment, HISTORYRADIO);
                }
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
        setTitleSelected(i);
    }

    /**
     * 隐藏全部Fragment
     *
     * @param fragmentTransaction
     */
    private void hiddenAll(FragmentTransaction fragmentTransaction) {
        if (mMusicHistoryFragment != null) {
            fragmentTransaction.hide(mMusicHistoryFragment);
        }
        if (mRadioHistoryFragment != null) {
            fragmentTransaction.hide(mRadioHistoryFragment);
        }
    }


    @OnClick(R.id.ll_left_back)
    public void onViewClicked() {
        reportBackEvent();
        finish();
    }

    @OnClick({R.id.tv_title, R.id.tv_title_2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_title:
                changeFragment(FRAGMENT_MUSIC);
                break;
            case R.id.tv_title_2:
                changeFragment(FRAGMENT_RADIO);
                break;
        }
    }

    public void setTitleSelected(int position) {
        currentId = position;
        for (int i = 0; i < prlTitle.getChildCount(); i++) {
            prlTitle.getChildAt(i).setSelected(position == i);
        }
    }

    @Override
    public void onBackPressed() {
        reportBackEvent();
        super.onBackPressed();
    }


    private void reportBackEvent() {
        if (currentId == FRAGMENT_MUSIC) {
            ReportEvent.clickHistoryMusicBack();
        } else if (currentId == FRAGMENT_RADIO) {
            ReportEvent.clickHistoryRadioBack();
        }
    }

}
