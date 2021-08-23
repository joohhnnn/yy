package com.txznet.music.settingModule.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.image.glide.GlideImageLoader;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by telenewbie on 2017/12/15.
 */

public class SettingActivity extends BaseActivity {
    @Bind(R.id.ll_left_back)
    LinearLayout llLeftBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_delete)
    ImageView ivDelete;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;


    @Override
    protected String getActivityTag() {
        return "SettingActivity#" + this.hashCode() + "/我的设置";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvTitle.setText("我的设置");
        ivDelete.setVisibility(View.GONE);
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    @Override
    public int getLayout() {
        return R.layout.act_setting;
    }


    @OnClick(R.id.ll_left_back)
    public void onViewClicked() {
        finish();
        ReportEvent.clickSettingPageBack();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ReportEvent.clickSettingPageBack();
    }
}
