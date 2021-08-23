package com.txznet.music.ui.helper;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.ui.helper.entity.BaseEntity;
import com.txznet.music.ui.helper.entity.Group;
import com.txznet.music.ui.helper.entity.Group1;
import com.txznet.music.ui.helper.entity.GroupChild;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * @author telen
 * @date 2019/1/2,10:33
 */
public class HelpFragment extends BaseFragment {
    @Bind(R.id.rv_data)
    RecyclerView mRecyclerView;

    private Adapter mAdapter;

    @Override
    protected int getLayout() {
        return R.layout.help_fragment;
    }

    @Override
    protected void initView(View view) {
        Context appCtx = GlobalContext.get();//要养成好的习惯，除非需要Activity作为Context，否则能用ApplicationContext就尽量使用，减少对Activity的强引用

        tvTitle.setText("使用帮助");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(appCtx));
        mRecyclerView.setAdapter(mAdapter = new Adapter(appCtx));
        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));

        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Group group = new Group("请说“你好，XX”唤醒语音，然后这样说");
        int groupID = mAdapter.addGroup(group);
        List<BaseEntity> groupChild = new ArrayList<>();
        groupChild.add(new GroupChild("点播歌手", "我要听刘德华的歌", false, null));
        groupChild.add(new GroupChild("点播指定歌曲", "我要听刘德华的忘情水", false, null));
        groupChild.add(new GroupChild("点播歌曲", "我要听花房姑娘", false, null));
        groupChild.add(new GroupChild("点播类型歌曲", "我要听粤语歌", false, null));

        groupChild.add(new GroupChild("播放音乐", "播放音乐", true, v -> {
            HelpDetailFragment.newInstance("播放音乐",
                    "“ 打开音乐、播放音乐、听音乐、播音乐、播放歌曲、听歌曲、播歌曲、播歌、听歌、我要听音乐、随便听听、随意听听、随便来首歌、随便来首音乐、随便来点歌、你随便唱吧、好听的歌有哪些、放首歌听、放首歌听听、放首歌 ”").show(getChildFragmentManager(), "播放音乐");
        }));
        groupChild.add(new GroupChild("播放电台", "播放电台", true, v -> {
            HelpDetailFragment.newInstance("播放电台",
                    "“ 打开电台、播放电台、放个电台听、我要听电台、播放电台节目、听电台 ”").show(getChildFragmentManager(), "播放电台");
        }));
        groupChild.add(new GroupChild("关闭/退出", "关闭播放", true, v -> {
            HelpDetailFragment.newInstance("关闭/退出",
                    "“ 关闭音乐、关闭歌曲、退出音乐、退出歌曲、关掉音乐、关掉歌曲、不想听歌、不想听音乐、音乐关了、关了音乐、关音乐、关闭播放、退出播放、关掉播放、关闭播放器、关闭电台、关闭电台节目、退出电台 ”").show(getChildFragmentManager(), "关闭/退出");
        }));
        groupChild.add(new GroupChild("控制播放器", "播放/暂停/上一首/下一首", true, v -> {
            HelpDetailFragment.newInstance("控制播放器",
                    "继续播放：“ 继续播放、播放、继续播放、恢复播放 ”\n" +
                            "\n" +
                            "暂停播放：“ 暂停播放、暂停音乐、音乐暂停、停止音乐、暂停歌曲、停止歌曲、暂停播放、暂停播放音乐、音乐停止、暂停、停止、暂停播放、停止播放、暂停电台节目、停止电台节目、暂停电台、停止电台 ”\n" +
                            "\n" +
                            "下一首：“ 下一首、下一首歌、下一首歌曲、切换歌曲、切歌、转歌、转一首歌、换首歌、换一首歌、换个歌、随意换一批歌曲、随意换一批歌、随便换一批歌曲、随便换一批歌、换一批歌曲、换一批歌 ”\n" +
                            "\n" +
                            "上一首：“ 上一首、上一首歌、上一首歌曲 ”").show(getChildFragmentManager(), "控制播放器");
        }));
        groupChild.add(new GroupChild("切换播放模式", "顺序播放/随机播放/单曲循环", true, v -> {
            HelpDetailFragment.newInstance("切换播放模式",
                    "顺序播放：“ 顺序播放、顺序播放模式、顺序模式 ”\n" +
                            "\n" +
                            "随机播放：“ 随机播放、随机播放模式、随机模式 ”\n" +
                            " \n" +
                            "单曲循环：“ 单曲循环、单曲循环模式、单曲模式、重复播放、单曲播放、单曲播放模式 ”").show(getChildFragmentManager(), "切换播放模式");
        }));
        groupChild.add(new GroupChild("收藏/取消收藏", "我要收藏/取消收藏", true, v -> {
            HelpDetailFragment.newInstance("收藏/取消收藏",
                    "收藏：“ 我要收藏、收藏歌曲、收藏音乐、收藏当前歌曲、收藏当前音乐、收藏这首歌、喜欢这首歌、收藏、加入收藏 ”\n" +
                            "\n" +
                            "取消收藏：“ 取消收藏、取消收藏这首歌、取消收藏当前歌曲、不喜欢这首歌、讨厌这首歌、不好听、这首歌不好听 ”").show(getChildFragmentManager(), "收藏/取消收藏");
        }));
        groupChild.add(new GroupChild("订阅/取消订阅", "我要订阅/取消订阅", true, v -> {
            HelpDetailFragment.newInstance("订阅/取消订阅",
                    "订阅：“ 订阅、订阅这节目、订阅当前电台、订阅这个专辑、订阅这个电台、我要订阅、订阅电台、订阅这个节目、加入订阅、订阅这个栏目 ”\n" +
                            "\n" +
                            "取消订阅：“ 取消订阅 ”").show(getChildFragmentManager(), "订阅/取消订阅");
        }));
        groupChild.add(new GroupChild("播放收藏的音乐", "播放收藏", true, v -> {
            HelpDetailFragment.newInstance("播放收藏的音乐",
                    "“ 播放收藏、播放收藏列表、播放收藏音乐、播放收藏音乐列表、播放收藏歌曲、播放收藏歌曲列表 ”").show(getChildFragmentManager(), "播放收藏的音乐");
        }));
        groupChild.add(new GroupChild("播放订阅的节目", "播放订阅", true, v -> {
            HelpDetailFragment.newInstance("播放订阅的节目",
                    "“ 播放订阅、播放订阅列表、播放订阅节目、播放订阅专辑、播放订阅栏目、播放订阅电台 ”").show(getChildFragmentManager(), "播放订阅的节目");
        }));

        mAdapter.addGroupChild(groupID, groupChild);

        Group1 group2 = new Group1("你可以直接说");
        int groupID2 = mAdapter.addGroup(group2);
        mAdapter.addGroupChild(groupID2, new GroupChild("免唤醒指令", "上一首、下一首、暂停播放、继续播放、加入收藏、取消收藏、加入订阅、取消订阅", false, null));
    }

}
