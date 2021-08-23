package com.txznet.music.FavourModule.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.FavourModule.adapter.ItemAudioAdapter;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.adapter.ResourceViewHolder;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.localModule.ui.adapter.AudioViewHolder;
import com.txznet.music.playerModule.logic.PlayEngineCoreDecorator;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.ui.adapter.PlayListAdapterV41;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.LoadingView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by telenewbie on 2017/12/14.
 */

public class MyFavourActivity extends BaseBarActivity implements OnRefreshListener, OnLoadMoreListener, Observer {

    @Bind(R.id.iv_left_back)
    ImageView ivLeftBack;
    @Bind(R.id.ll_left_back)
    LinearLayout llLeftBack;
    @Bind(R.id.tv_back)
    TextView tvBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_delete)
    ImageView ivDelete;
    @Bind(R.id.tv_choice_all)
    TextView tvChoiceAll;
    @Bind(R.id.tv_delete)
    TextView tvDelete;
    @Bind(R.id.ll_delete_rage)
    PercentRelativeLayout llDeleteRage;

    @Bind(R.id.player_bar)
    BarPlayerView playerBar;
    @Bind(R.id.layout_library_loading_view)
    LoadingView loadingView;


    @Bind(R.id.tv_title_2)
    TextView tvTitle2;
    @Bind(R.id.prl_title)
    PercentRelativeLayout prlTitle;
    @Bind(R.id.swipe_target)
    RecyclerView swipeTarget;
    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;

    ItemAudioAdapter<FavourBean> itemAudioAdapter;
    List<FavourBean> favourBeans = new ArrayList<>();
    int playindex = -1;


    public RecyclerView.Adapter getAdapter() {
        itemAudioAdapter = new ItemAudioAdapter<FavourBean>(this, favourBeans) {
            @Override
            public void bindView(AudioViewHolder v, FavourBean favourBean, int position) {
                v.mIvFavour.setVisibility(View.GONE);
                Resources mRes = ctx.getResources();
                if (favourBean != null && favourBean.getAudio() != null) {
                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                    if (currentAudio != null && favourBean.getAudio().equals(currentAudio)) {
                        playindex = position;
                        Logger.d(getActivityTag(), "current:index:" + playindex);
                        v.mIvLeftIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_playing));
                        v.mTitle.setText(getTitleForAudioNameWithArtists(favourBean.getAudio(), true));
                    } else {
                        if (playindex == position) {
                            playindex = -1;
                        }
                        v.mIvLeftIcon.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_normal));
                        v.mTitle.setText(getTitleForAudioNameWithArtists(favourBean.getAudio(), false));
                    }
                }
            }
        };
        itemAudioAdapter.setOnDeleteListener(new ItemAudioAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position) {
                LogUtil.logd(getActivityTag() + " setOnDeleteListener " + position);
                //从数据库中删除
                final Audio audio = favourBeans.get(position).getAudio();
                FavorHelper.unfavor(audio, EnumState.Operation.manual);
                ReportEvent.clickMineFavourDelete(audio.getSid(), audio.getId(), audio.getName());
            }
        });
        itemAudioAdapter.setOnItemClickListener(new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                LogUtil.logd(getActivityTag() + " setOnItemClickListener " + position);
                List<Audio> audios = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(favourBeans)) {
                    for (FavourBean bean : favourBeans) {
                        audios.add(bean.getAudio());
                    }
                }
                PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, position, PlayInfoManager.DATA_FAVOUR);
                PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                ReportEvent.clickMineFavourAudio(audios.get(position).getSid(), audios.get(position).getId(), audios.get(position).getName());
            }
        });
        return itemAudioAdapter;
    }

    @Override
    public int getLayout() {
        return R.layout.act_favour;
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        tvTitle.setText("我的收藏");
        ivDelete.setVisibility(View.GONE);
        swipeTarget.setLayoutManager(new TXZLinearLayoutManager(this));
        swipeTarget.setAdapter(getAdapter());
        requestData();
        initLinstener();
        ObserverManage.getObserver().addObserver(this);
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    @Override
    public BarPlayerView getBarPlayerView() {
        return playerBar;
    }

    private void initLinstener() {
        swipeToLoadLayout.setOnLoadMoreListener(this);
        swipeToLoadLayout.setOnRefreshListener(this);
        loadingView.setEmptyHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页的音乐
                Intent intent = new Intent(MyFavourActivity.this, ReserveConfigSingleTaskActivity1.class);
                intent.putExtra(HomeActivity.KEY_TAB, HomeActivity.MUSIC_i);
                MyFavourActivity.this.startActivity(intent);
                MyFavourActivity.this.finish();
            }
        });
    }

    //获取数据
    private void requestData() {
        loadingView.showLoading(R.drawable.fm_local_scan, R.drawable.fm_local_scan_logo, "加载我的收藏中...");

        FavorHelper.getFavourData(0, 0, 0, 100, new FavorHelper.SendFavourListener<FavourBean>() {
            @Override
            public void onResponse(List<FavourBean> list) {
                if (CollectionUtils.isNotEmpty(list)) {
                    loadingView.showContent();
                    favourBeans.clear();
                    favourBeans.addAll(list);
                    itemAudioAdapter.notifyDataSetChanged();
                } else {
                    loadingView.showEmpty("你还没有收藏的内容哦", R.drawable.fm_mine_favour_none, "去看看");
                }
            }

            @Override
            public void onError() {
                loadingView.showError("网络不给力，数据加载失败", R.drawable.fm_me_no_result_network);
            }
        });
    }


    @Override
    protected String getActivityTag() {
        return "MyFavourActivity#" + this.hashCode() + "/我的收藏";
    }

    @OnClick({R.id.ll_left_back, R.id.tv_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_left_back:
                ReportEvent.clickMineFavourBack();
                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {
        swipeToLoadLayout.setRefreshing(false);
    }

    @Override
    public void onLoadMore() {
        FavourBean lastFavourBean = null;
        if (CollectionUtils.isNotEmpty(favourBeans)) {
            lastFavourBean = favourBeans.get(favourBeans.size() - 1);
        } else {
            lastFavourBean = new FavourBean();
            lastFavourBean.setId(0);
            lastFavourBean.setSid(0);
        }

        FavorHelper.getFavourData(lastFavourBean.getSid(), lastFavourBean.getId(), lastFavourBean.getTimestamp(), 100, new FavorHelper.SendFavourListener<FavourBean>() {
            @Override
            public void onResponse(List<FavourBean> list) {
                swipeToLoadLayout.setLoadingMore(false);
                if (CollectionUtils.isNotEmpty(list)) {
                    favourBeans.addAll(list);
                    itemAudioAdapter.notifyDataSetChanged();
                } else {
                }
            }

            @Override
            public void onError() {
                swipeToLoadLayout.setLoadingMore(false);
                ToastUtils.showShort("网络不给力，数据加载失败");
            }
        });
    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            LogUtil.logd(getActivityTag() + "reqData:info type:" + info.getType());
            Audio obj = null;
            switch (info.getType()) {
                case InfoMessage.FAVOUR_MUSIC:
                    obj = (Audio) info.getObj();
                    FavourBean favourBean = new FavourBean(obj);
                    Logger.d(getActivityTag(), favourBeans);
                    Logger.d(getActivityTag(), favourBean);
                    Logger.d(getActivityTag(), favourBeans.contains(favourBean));
                    if (!favourBeans.contains(favourBean)) {
                        favourBeans.add(0, favourBean);
                        itemAudioAdapter.notifyItemInserted(0);
                        swipeTarget.smoothScrollToPosition(0);
                    }

                    break;

                case InfoMessage.UNFAVOUR_MUSIC:
                    obj = (Audio) info.getObj();
                    if (Utils.isSong(obj.getSid())) {
                        int index = -1;
                        for (int i = 0; i < favourBeans.size(); i++) {
                            if (favourBeans.get(i).getId() == obj.getId()) {
                                index = i;
                                break;
                            }
                        }
                        LogUtil.logd(getActivityTag() + "notify item index " + index);
                        if (index >= 0) {
                            ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_UNFAVOUR_TIPS);
                            FavourBean remove = favourBeans.remove(index);
                            itemAudioAdapter.notifyItemRemoved(index);
                            if (favourBeans.size() == 0) {
                                loadingView.showEmpty("你还没有收藏的内容哦", R.drawable.fm_mine_favour_none, "去看看");
                            } else {
                                itemAudioAdapter.notifyItemRangeChanged(index, favourBeans.size() - index);
                            }
                            if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_FAVOUR) {
                                if (remove != null && remove.getAudio() != null && remove.getAudio().equals(PlayInfoManager.getInstance().getCurrentAudio())) {
                                    PlayEngineFactory.getEngine().next(EnumState.Operation.auto);
                                }
                                PlayInfoManager.getInstance().removePlayListAudio(obj);//自己会进行切歌的处理,
                            }else{
                                PlayInfoManager.getInstance().notifyPlayListChanged();
                            }
                        }
                    }
                    break;
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    obj = (Audio) info.getObj();
                    if (playindex >= 0) {
                        itemAudioAdapter.notifyItemChanged(playindex);
                    }

                    if (obj != null && Utils.isSong(obj.getSid())) {
                        int index = -1;
                        for (int i = 0; i < favourBeans.size(); i++) {
                            if (favourBeans.get(i).getId() == obj.getId()) {
                                index = i;
                                break;
                            }
                        }
                        LogUtil.logd(getActivityTag() + "notify item index " + index);
                        if (index >= 0) {
                            itemAudioAdapter.notifyItemChanged(index);
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManage.getObserver().deleteObserver(this);
    }
}
