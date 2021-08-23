package com.txznet.music.albumModule.ui;

import android.app.Fragment;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.InterestTag;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.ui.BaseActivity;
import com.txznet.music.utils.JsonHelper;

import java.util.List;

/**
 * Created by 58295 on 2018/4/24.
 */

public class InterestTagActivity extends BaseActivity {
    //区分电台和音乐兴趣标签
    public static String url;
    public static List<InterestTag> data;
    public BaseFragment fragment;

    @Override
    protected String getActivityTag() {
        return "InterestTagActivity";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        url = getIntent().getStringExtra("url");
        data = JsonHelper.toObject(getIntent().getStringExtra("data"), new TypeToken<List<InterestTag>>() {
        }.getType());
        fragment = (BaseFragment) getFragmentManager().findFragmentById(R.id.fragment_interest);
    }


    @Override
    public int getLayout() {
        return R.layout.act_interes_tag;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        url = null;
        if (data != null) {
            data.clear();
            data = null;
        }
    }

    @Override
    public void onBackPressed() {
        fragment.onBackPressed();
        super.onBackPressed();
    }

    //    @Override
//    public boolean onBackPressed() {
//        AlbumEngine.getInstance().skipInterestTag(InterestTagActivity.url);
//        return super.onBackPressed();
//    }
}
