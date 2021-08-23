package com.txznet.music.ui.about;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.music.BuildConfig;
import com.txznet.music.R;
import com.txznet.music.ui.base.BaseFragment;

import butterknife.Bind;

/**
 * Created by brainBear on 2017/7/28.
 */

public class AboutUsFragment extends BaseFragment {

    @Bind(R.id.iv_about_logo)
    ImageView ivAboutLogo;
    @Bind(R.id.tv_version)
    TextView tvVersion;

    private int clickCount;

    @Override
    public void initData(Bundle savedInstanceState) {

    }

    @Override
    public int getLayout() {
        return R.layout.about_us_fragment;
    }

    @Override
    protected void initView(View view) {
        tvTitle.setText("关于我们");
        tvVersion.setText("同听 V" + BuildConfig.VERSION_NAME);
        ivAboutLogo.setImageDrawable(getResources().getDrawable(R.drawable.about_big_launcher_icon_iv));
        ivAboutLogo.setOnClickListener(v -> {
            ++clickCount;
            if (clickCount % 5 == 0) {
                Toast.makeText(getContext(), "同听:V" + BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_TIME + "." + BuildConfig.CODE_VERSION, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
