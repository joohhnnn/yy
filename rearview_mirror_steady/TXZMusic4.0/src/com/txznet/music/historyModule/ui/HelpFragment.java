package com.txznet.music.historyModule.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Created by brainBear on 2017/7/30.
 */

public class HelpFragment extends BaseFragment implements View.OnClickListener {

    private TextView tvTitle;
    private View vBack;
    private View vSetting;
    private ImageView ivTitleIcon;
    private ScrollView mViewDetail;
    private TextView mTvDetailTitle;
    private TextView mTvDetailDetail;

    private boolean mInterceptBack = false;

    private List<Object[]> mData = new ArrayList<>();

    @Override
    public void reqData() {

    }

    @Override
    public void bindViews() {
        tvTitle = (TextView) findViewById(R.id.tv_title);
        vBack = findViewById(R.id.layout_back);
        vSetting = findViewById(R.id.v_setting);
        mViewDetail = (ScrollView) findViewById(R.id.v_detail);
        mTvDetailTitle = (TextView) findViewById(R.id.tv_detail_title);
        mTvDetailDetail = (TextView) findViewById(R.id.tv_detail_detail);
        ivTitleIcon = (ImageView) findViewById(R.id.iv_icon);

        findViewById(R.id.v_music_play).setOnClickListener(this);
        findViewById(R.id.v_music_type).setOnClickListener(this);
        findViewById(R.id.v_music_control).setOnClickListener(this);
        findViewById(R.id.v_music_close).setOnClickListener(this);
        findViewById(R.id.v_music_mode).setOnClickListener(this);
        findViewById(R.id.v_music_play_favor).setOnClickListener(this);
        findViewById(R.id.v_music_favor).setOnClickListener(this);
        findViewById(R.id.v_radio_play).setOnClickListener(this);
        findViewById(R.id.v_novel_play).setOnClickListener(this);
        findViewById(R.id.v_news_play).setOnClickListener(this);
        findViewById(R.id.v_radio_type).setOnClickListener(this);
        findViewById(R.id.v_radio_control).setOnClickListener(this);
        findViewById(R.id.v_radio_close).setOnClickListener(this);
        findViewById(R.id.v_radio_play_favor).setOnClickListener(this);
        findViewById(R.id.v_radio_favor).setOnClickListener(this);
        findViewById(R.id.v_push).setOnClickListener(this);
        findViewById(R.id.v_wakeup).setOnClickListener(this);
        vBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

    }

    private void showDetail(int id) {
        mInterceptBack = true;
        mViewDetail.setVisibility(View.VISIBLE);
        vSetting.setVisibility(View.GONE);

        for (int i = 0; i < mData.size(); i++) {
            Object[] objects = mData.get(i);
            if ((Integer) objects[0] == id) {
                tvTitle.setTextColor(Color.WHITE);
                tvTitle.setText((String) objects[1]);
                mTvDetailTitle.setText((String) objects[2]);
                mTvDetailDetail.setText((String) objects[3]);
            }
        }

    }


    private void initView() {
        vSetting.setVisibility(View.VISIBLE);
        mViewDetail.setVisibility(View.GONE);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText("帮助");
        ivTitleIcon.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.ic_help_title));
        mInterceptBack = false;
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData(Bundle savedInstanceState) {
        initView();
        mData.add(new Object[]{R.id.v_music_play, "播放音乐", "播放音乐的说法", "\"播放音乐\",\"听音乐\",\"播音乐\",\"播放歌曲\",\"听歌曲\",\"播歌曲\",\"播歌\", \"听歌\", \"我要听音乐\""});
        mData.add(new Object[]{R.id.v_music_type, "点播类型歌曲", "我要听", "\"经典老歌\"、\"老歌\"、\"中文歌曲\"、\"国语歌\"、\"粤语歌\"、\"流行歌曲\"、\"电子音乐\"、\"欧美歌曲\"、\"英语歌\"、\"英文歌\"、\"日语歌\"、\"日语歌曲\"、\"韩语歌\"、\"韩国歌\"、\"摇滚乐\"、\"摇滚\"、\"儿歌\"、\"乡村音乐\"、\"小清新音乐\"、\"爵士乐\"、\"古典音乐\"、\"伤感音乐\"、\"感性音乐\"、\"蓝调\"、\"拉丁曲\""});
        mData.add(new Object[]{R.id.v_music_control, "控制播放器", "暂停播放的说法", "\"暂停音乐\", \"音乐暂停\", \"停止音乐\", \"暂停歌曲\", \"停止歌曲\", \"暂停播放\", \"暂停播放音乐\", \"暂停\", \"停止\", \"音乐停止\", \"停止播放\",\"暂停奏乐\",\"停止奏乐\"\n\n恢复播放的说法\n\n\"播放\", \"继续播放\", \"恢复播放\", \"奏乐\", \"继续奏乐\", \"恢复奏乐\"\n\n切换上一首的说法:\n\n\"上一首\", \"上一首歌\", \"上一首歌曲\", \"播放上一首\",\"前一首\",\"播放前一首\", \"上一曲\", \"播放上一曲\", \"前一首\", \"播放前一首\"\n\n切换下一首的说法:\n\n\"换一首\",\"换首歌\",\"换一首歌\",\"换个歌\",\"下一首\", \"下一首歌\", \"下一首歌曲\", \"播放下一首\",\"后一首\",\"播放后一首\", \"下一曲\", \"播放下一曲\", \"后一首\", \"播放后一首\",\"切换歌曲\",\"切歌\",\"转歌\",\"跳一首\",\"转一首歌\",\"重新放一首\",\"重放一首\"\n"});
        mData.add(new Object[]{R.id.v_music_close, "关闭音乐", "关闭音乐的说法", "\"关闭音乐\", \"关闭歌曲\", \"关闭播放\",\"退出音乐\",\"退出歌曲\",\"退出播放\", \"关掉音乐\",\"关掉歌曲\",\"关掉播放\", \"关闭播放器\", \"不想听歌\", \"不想听音乐\", \"音乐关了\", \"关了音乐\", \"关音乐\"\n"});
        mData.add(new Object[]{R.id.v_music_mode, "切换播放模式", "切换顺序播放的说法:", "\"顺序播放\",\"顺序播放模式\",\"顺序模式\",\"全部循环\",\"全部循环模式\",\"全部循环播放\",\"列表循环播放\",\"列表循环\",\"循环列表播放\"\n\n切换随机播放的说法:\n\n\"随机播放\",\"随机播放模式\",\"随机模式\"\n\n切换单曲循环的说法:\n\n\"单曲循环\",\"单曲循环模式\",\"单曲模式\",\"循环播放\",\"循环模式\",\"循环播放模式\",\"重复播放\",\"单曲播放\",\"单曲播放模式\"\n"});
        mData.add(new Object[]{R.id.v_music_play_favor,"播放收藏的音乐","播放收藏的音乐的说法","\"播放收藏\",\"播放收藏列表\",\"播放收藏音乐\",\"" +
                "播放收藏音乐列表\",\"播放收藏歌曲\",\"播放收藏歌曲列表\"\n"});
        mData.add(new Object[]{R.id.v_music_favor,"收藏音乐","收藏音乐的说法","\"收藏\",\"收藏歌曲\",\"收藏音乐\",\"收藏当前歌曲\",\"" +
                "收藏当前音乐\",\"收藏这首歌\",\"喜欢这首歌\",\"我要收藏\",\"加入收藏\"\n"});
        mData.add(new Object[]{R.id.v_radio_play, "点播电台", "播放电台的说法", "\"播放电台\", \"放个电台听\",\"我要听电台\", \"播放电台节目\",\"听电台\""});
        mData.add(new Object[]{R.id.v_novel_play, "点播小说", "播放小说的说法", "我要听--\n\"小说\", \"言情小说\",\"武侠小说\", \"玄幻小说\",\"推理小说\",\"军事小说\",\"悬疑小说\",\"都市小说\""});
        mData.add(new Object[]{R.id.v_news_play, "点播新闻", "播放新闻的说法", "我要听--\n\"新闻\", \"财经新闻\",\"国内新闻\", \"国际新闻\",\"体育新闻\",\"娱乐新闻\",\"军事新闻\""});
        mData.add(new Object[]{R.id.v_radio_type, "点播其他类型", "我要听", "\"相声\", \"笑话\",\"段子\", \"财经节目\",\"脱口秀\",\"儿童故事\",\"广播剧\"\n"});
        mData.add(new Object[]{R.id.v_radio_control, "控制播放器", "控制播放器的说法", "暂停播放的说法\n\n\"暂停电台节目\", \"停止电台节目\", \"暂停电台\", \"停止电台\"\n\n恢复播放的说法:\n\n\"播放\", \"继续播放\", \"恢复播放\"\n\n切换上一个的说法:\n\n\"上一个\",\"播放上一个\",\"前一个\",\"播放前一个\",\"上一个节目\",\"播放上一个节目\",\"前一个节目\",\"播放前一个节目\",\"播放上一集\",\"播放上一节\",\"上一集\",\"上一节\",\"播放前一集\",\"前一集\",\"播放前一个章节\",\"前一个章节\",\"播放上一章\",\"上一章\"\n\n切换下一个的说法:\n\n\"下一个\",\"播放下一个\",\"后一个\",\"播放后一个\",\"下一个节目\",\"播放下一个节目\",\"后一个节目\",\"播放后一个节目\",\"播放下一集\",\"播放下一节\",\"下一集\",\"下一节\",\"播放后一集\",\"后一集\",\"播放下一个章节\",\"下一个章节\",\"播放下一章\",\"下一章\"\n\n关闭电台的说法:\n\n\"关闭电台\", \"关闭电台节目\",\"退出电台\""});
        mData.add(new Object[]{R.id.v_radio_close, "关闭电台", "关闭电台的说法", "\"关闭电台\", \"关闭电台节目\",\"退出电台\"\n"});
        mData.add(new Object[]{R.id.v_radio_play_favor, "播放订阅的节目","播放订阅的节目的说法", "\"播放订阅\", \"播放订阅列表\", \"播放订阅节目\", \"播放订阅专辑\", \"播放订阅栏目\", \"播放订阅电台\"\n"});
        mData.add(new Object[]{R.id.v_radio_favor,"订阅节目","订阅节目的说法","\"我要订阅\", \"订阅音乐\", \"订阅这首歌\", \"订阅歌曲\", \"我要收藏\", \"收藏音乐\", \"收藏这首歌\", \"收藏歌曲\"\n"});
        mData.add(new Object[]{R.id.v_radio_favor,"订阅节目","订阅节目的说法","\"订阅\", \"订阅这节目\", \"订阅当前电台\", \"" +
                "订阅这个专辑\", \"订阅这个电台\", \"我要订阅\", \"订阅电台\", \"订阅这个节目\", \"加入订阅\", \"订阅这个栏目\"\n"});
        mData.add(new Object[]{R.id.v_push,
                "关闭" + getResources().getString(R.string.str_close_push_msg_init) + "的效果",
                "关闭" + getResources().getString(R.string.str_close_push_msg_init) + "的效果",
                "关闭" + getResources().getString(R.string.str_close_push_msg_init) + "后,用户每天将接收不到重要提醒和最新最热动态信息"});
        mData.add(new Object[]{R.id.v_wakeup, "关闭免唤醒语音指令的效果", "关闭免唤醒语音指令的效果", "关闭免唤醒语音指令后,只有把语音调起来才能控制播放器,直接声控免唤醒操作播放器将不起效果。"});
    }

    @Override
    public int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.fragment_help_phone_portrait;
        }
        return R.layout.fragment_help;
    }

    @Override
    public String getFragmentId() {
        return "HelpFragment";
    }

    @Override
    public void update(Observable o, Object arg) {

    }

    @Override
    public boolean onBackPressed() {
        if (mInterceptBack) {
            initView();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        showDetail(v.getId());
    }
}
