package com.txznet.music.historyModule.ui;

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.R;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.push.PushManager;
import com.txznet.music.service.MusicInteractionWithCore;

import java.util.Observable;

/**
 * Created by brainBear on 2017/7/28.
 */

public class AboutUsFragment extends BaseFragment {

    private LinearLayout llBack;
    private TextView tvTitle, tvVersion;
    private ImageView ivLogo;
    private ImageView ivTitleIcon;

    @Override
    public void reqData() {

    }

    private long currentTime = 0;
    private int clickCount = 0;
    private long lastTestTime = 0;

    @Override
    public void bindViews() {
        llBack = (LinearLayout) findViewById(R.id.layout_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivLogo = (ImageView) findViewById(R.id.iv_about_logo);
        ivTitleIcon = (ImageView) findViewById(R.id.iv_icon);
        tvVersion = (TextView) findViewById(R.id.tv_version);

        tvTitle.setText("关于同听");
        tvVersion.setText("同听:V" + BuildConfig.VERSION_NAME + "." + BuildConfig.BUILD_TIME);
        ivTitleIcon.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.ic_info_title));
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        ivLogo.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_setting_launcher));
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //仅用作快报特殊入口
                long lastTime = currentTime;
                currentTime = SystemClock.elapsedRealtime();
                if (currentTime - lastTime < 800) {
                    clickCount++;
                } else {
                    clickCount = 0;
                }
                if (clickCount > 1) {
                    long l = SystemClock.elapsedRealtime() - lastTestTime;
                    if (l > 20 * 1000) {
                        PushManager.getInstance().test();
                        lastTestTime = SystemClock.elapsedRealtime();
                    }
                    clickCount = 0;
                }
            }
        });

    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_about_us;
    }

    @Override
    public String getFragmentId() {
        return "AboutUsFragment";
    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
