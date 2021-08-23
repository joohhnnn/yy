package com.txznet.reserve.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.ImageView;

import com.txznet.music.R;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.historyModule.ui.AboutUsFragment;
import com.txznet.music.historyModule.ui.HelpFragment;
import com.txznet.music.ui.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brainBear on 2017/7/11.
 */
public class ReserveStandardActivity0 extends BaseActivity {


    @Bind(R.id.choice_bg)
    ImageView choiceBg;
    private BaseFragment mCurrentFragment;

    @Override
    protected String getActivityTag() {
        return "ReserveStandardActivity0";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", 0);
        if (type == 1) {
            mCurrentFragment = new HelpFragment();
        } else {
            mCurrentFragment = new AboutUsFragment();
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fl_main, mCurrentFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_help_about;
    }


    @Override
    public void onBackPressed() {
        if (!mCurrentFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
