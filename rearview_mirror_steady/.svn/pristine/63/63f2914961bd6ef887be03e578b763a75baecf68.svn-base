package com.txznet.music.ui.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.txznet.music.R;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.base.BaseFragment;

import butterknife.Bind;

/**
 * @author telen
 * @date 2018/12/26,14:29
 */
public class HistoryFragment extends BaseFragment {
    @Bind(R.id.tab_layout_title)
    TabLayout tabLayoutTitle;
    @Bind(R.id.vp_content)
    ViewPager vpContent;

    private String[] historyTitles = new String[]{
            "历史音乐",
            "历史电台"
    };
    private Fragment[] historyContents = new Fragment[]{
            new HistoryMusicFragment(),
            new HistoryAlbumFragment()
    };


    @Override
    protected int getLayout() {
        return R.layout.history_fragment;
    }

    @Override
    protected void initView(View view) {
        vpContent.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public int getCount() {
                return historyTitles.length;
            }

            @Override
            public Fragment getItem(int position) {
                return historyContents[position];
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return historyTitles[position];
            }
        });
        tabLayoutTitle.setupWithViewPager(vpContent, true);
        tabLayoutTitle.setSelectedTabIndicatorHeight(0);
        tabLayoutTitle.setTabTextColors(getResources().getColor(R.color.history_normal_tv_color), getResources().getColor(R.color.history_selected_tv_color));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportEvent.reportUserHistoryEnter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ReportEvent.reportUserHistoryExit();
    }
}
