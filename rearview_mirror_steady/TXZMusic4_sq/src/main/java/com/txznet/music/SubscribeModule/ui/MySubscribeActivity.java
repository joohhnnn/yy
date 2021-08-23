package com.txznet.music.SubscribeModule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.loader.AppLogic;
import com.txznet.music.SubscribeModule.ui.adapter.ItemAlbumSubscribeAdapter;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.LoadingView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;

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

public class MySubscribeActivity extends BaseBarActivity implements Observer {
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
    @Bind(R.id.recyclerview)
    RecyclerView recyclerview;
    @Bind(R.id.player_bar)
    BarPlayerView playerBar;
    List<Album> albums = new ArrayList<>();

    ////
    ItemAlbumSubscribeAdapter itemAlbumSubscribeAdapter;
    @Bind(R.id.layout_library_loading_view)
    LoadingView loadingView;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;

    TXZLinearLayoutManager txzLinearLayoutManager = null;

    protected int lastVisibleItem;
    private int pageOff;
    private int playindex = -1;


    protected AdapterView.OnItemClickListener getAlbumItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) itemAlbumSubscribeAdapter.getItem(position);
                DBManager.getInstance().updateMessageUnRead(album.getId(), album.getSid());
                if (itemAlbumSubscribeAdapter.isEnterChoiceMode()) {
                    itemAlbumSubscribeAdapter.setChoiceAlbum(album, !itemAlbumSubscribeAdapter.isChoiceAlbum(album));
                    itemAlbumSubscribeAdapter.notifyItemChanged(position);
                } else {
                    playALbum(album, false);
                    ReportEvent.clickMineSubscribeIcon(album.getSid(), album.getId());
                }

            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemIconClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) itemAlbumSubscribeAdapter.getItem(position);
                DBManager.getInstance().updateMessageUnRead(album.getId(), album.getSid());
                if (itemAlbumSubscribeAdapter.isEnterChoiceMode()) {
                    itemAlbumSubscribeAdapter.setChoiceAlbum(album, !itemAlbumSubscribeAdapter.isChoiceAlbum(album));
                    itemAlbumSubscribeAdapter.notifyItemChanged(position);
                } else {
                    playALbum(album, true);
                    ReportEvent.clickMineSubscribePlay(album.getSid(), album.getId());
                }
            }
        };
    }

    private void playALbum(final Album album, final boolean needplay) {
        final long categoryID;
        if (com.txznet.comm.util.CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
            categoryID = album.getArrCategoryIds().get(0);
        } else {
            LogUtil.loge("music:click error", new RuntimeException("categoryId is Null"));
            return;
        }
        Album playingAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();

        try {
            if (playingAlbum != null && playingAlbum.equals(album)) {
                if (PlayEngineFactory.getEngine().isPlaying()) {
                    return;
                }
                if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {
                    PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                    return;
                }
            }
            PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    if (Utils.isSong(album.getSid())) {
                        AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, categoryID, needplay, null);
                    } else {
                        AlbumEngine.getInstance().playAlbumWithBreakpoint(EnumState.Operation.manual, album, categoryID, needplay);
                    }
                }
            }, 0);
        } finally {
            if (ActivityStack.getInstance().currentActivity().hashCode() == this.hashCode()) {
                Intent intent = new Intent(this, ReserveConfigSingleTaskActivity2.class);
                this.startActivity(intent);
            }
        }

    }

    @Override
    public int getLayout() {
        return R.layout.act_subscribe;
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        tvTitle.setText("我的订阅");
        itemAlbumSubscribeAdapter = new ItemAlbumSubscribeAdapter(this, albums);
        itemAlbumSubscribeAdapter.notifyDataSetChanged();
        itemAlbumSubscribeAdapter.setOnItemClickListener(getAlbumItemClickListener());
        itemAlbumSubscribeAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
        txzLinearLayoutManager = new TXZLinearLayoutManager(this);
        txzLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerview.setLayoutManager(txzLinearLayoutManager);
        recyclerview.setAdapter(itemAlbumSubscribeAdapter);
        recyclerview.addItemDecoration(new AlbumDecoration((int) AttrUtils.getAttrDimension(this, R.attr.album_item_gap, 0)));
        recyclerview.setHasFixedSize(true);
        ivDelete.setVisibility(View.GONE);
        requestData();
        initLinstener();
        ObserverManage.getObserver().addObserver(this);
    }

    private void initLinstener() {
        loadingView.setEmptyHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页的音乐
                Intent intent = new Intent(MySubscribeActivity.this, ReserveConfigSingleTaskActivity1.class);
                intent.putExtra(HomeActivity.KEY_TAB, HomeActivity.RADIO_i);
                MySubscribeActivity.this.startActivity(intent);
                MySubscribeActivity.this.finish();
            }
        });


        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (null != itemAlbumSubscribeAdapter && !itemAlbumSubscribeAdapter.isShowLoading() && newState == 0 && lastVisibleItem + 1 >= itemAlbumSubscribeAdapter.getItemCount() && isNewClick) {
                    final Album album = albums.get(albums.size() - 1);
                    itemAlbumSubscribeAdapter.setShowLoading(true);
                    FavorHelper.getSubscribeData(album.getSid(), album.getId(), 10, new FavorHelper.SendFavourListener<SubscribeBean>() {
                        @Override
                        public void onResponse(List<SubscribeBean> list) {
                            int startindex = albums.size();
                            int count = 1;
                            itemAlbumSubscribeAdapter.setShowLoading(false);
                            List<Album> tempAlbums = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(list)) {
                                for (SubscribeBean subscribeBean : list) {
                                    tempAlbums.add(subscribeBean.getAlbum());
                                }
                                ivDelete.setVisibility(View.VISIBLE);
                                count = list.size();
                            } else {
                                ToastUtils.showShort("没有更多数据了");
                            }
                            albums.addAll(tempAlbums);
                            itemAlbumSubscribeAdapter.notifyItemRangeChanged(startindex, count);
                        }

                        @Override
                        public void onError() {
                            itemAlbumSubscribeAdapter.setShowLoading(false);
                            ToastUtils.showShort("网络不给力，数据加载失败");
                        }
                    });
                }
                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = txzLinearLayoutManager.findLastVisibleItemPosition();
                isNewClick = true;
            }

        });
    }

    @Override
    public BarPlayerView getBarPlayerView() {
        return playerBar;
    }

    @Override
    public ImageView getBg() {
        return choiceBg;
    }

    //获取数据
    private void requestData() {
        loadingView.showLoading(R.drawable.fm_local_scan, R.drawable.fm_local_scan_logo, "加载我的订阅中...");
        FavorHelper.getSubscribeData(0, 0, 100, new FavorHelper.SendFavourListener<SubscribeBean>() {
            @Override
            public void onResponse(List<SubscribeBean> list) {
                if (CollectionUtils.isNotEmpty(list)) {
                    loadingView.showContent();
                    for (SubscribeBean subscribeBean : list) {
                        albums.add(subscribeBean.getAlbum());
                    }
                    itemAlbumSubscribeAdapter.notifyDataSetChanged();
                    ivDelete.setVisibility(View.VISIBLE);
                } else {
                    loadingView.showEmpty("你还没有订阅的内容哦", R.drawable.fm_mine_subscribe_none, "去看看");
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
        return "MySubscribeActivity#" + this.hashCode() + "/我的订阅";
    }

    public boolean isChoiceAll = false;

    @OnClick({R.id.ll_left_back, R.id.tv_back, R.id.iv_delete, R.id.tv_choice_all, R.id.tv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_left_back:
                finish();
                ReportEvent.clickMineSubscribeBack();
                break;
            case R.id.tv_back:
                llLeftBack.setVisibility(View.VISIBLE);
                ivDelete.setVisibility(View.VISIBLE);
                llDeleteRage.setVisibility(View.GONE);
                tvBack.setVisibility(View.GONE);

                //所有的专辑列表的播放图标,都变为带选中状态
                itemAlbumSubscribeAdapter.enterChoiceMode(false);
                itemAlbumSubscribeAdapter.notifyDataSetChanged();
                break;
            case R.id.iv_delete:
                llLeftBack.setVisibility(View.GONE);
                ivDelete.setVisibility(View.GONE);
                llDeleteRage.setVisibility(View.VISIBLE);
                tvBack.setVisibility(View.VISIBLE);

                //所有的专辑列表的播放图标,都变为带选中状态
                itemAlbumSubscribeAdapter.enterChoiceMode(true);
                itemAlbumSubscribeAdapter.notifyDataSetChanged();

                break;
            case R.id.tv_choice_all:
                if (isChoiceAll) {
                    isChoiceAll = false;
                    tvChoiceAll.setText("全选");
                    itemAlbumSubscribeAdapter.choiceAll(false);
                } else {
                    isChoiceAll = true;
                    itemAlbumSubscribeAdapter.choiceAll(true);
                    tvChoiceAll.setText("全不选");
                }
                itemAlbumSubscribeAdapter.notifyDataSetChanged();
                break;
            case R.id.tv_delete:
                FavorHelper.unSubscribeRadio(itemAlbumSubscribeAdapter.getDeleteAlbums(), EnumState.Operation.manual);
                ReportEvent.clickMineSubscribeDelete(itemAlbumSubscribeAdapter.getDeleteAlbums());
                break;
        }
    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            LogUtil.logd(getActivityTag() + "reqData:info type:" + info.getType());

            switch (info.getType()) {
                case InfoMessage.SUBSCRIBE_RADIO:
                    Album album = (Album) info.getObj();
                    if (!albums.contains(album)) {
                        albums.add(0, album);
                        itemAlbumSubscribeAdapter.notifyItemInserted(0);
                        recyclerview.smoothScrollToPosition(0);
                    }

                    break;
                case InfoMessage.UNSUBSCRIBE_MULTI_RADIO:
                    List<Album> obj = (List<Album>) info.getObj();
                    albums.removeAll(obj);
                    itemAlbumSubscribeAdapter.notifyDataSetChanged();
                    if (albums.size() == 0) {
                        tvBack.post(new Runnable() {
                            @Override
                            public void run() {
                                tvBack.performClick();
                                ivDelete.setVisibility(View.GONE);
                            }
                        });

                        loadingView.showEmpty("你还没有订阅的内容哦", R.drawable.fm_mine_subscribe_none, "去看看");
                    }

                    break;
                case InfoMessage.PLAY:
                case InfoMessage.PAUSE:
                    if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                        playindex = albums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
                        LogUtil.logd(getActivityTag() + "notify item index i=" + playindex);
                        itemAlbumSubscribeAdapter.notifyItemChanged(playindex);
                    }
                    break;

                case InfoMessage.PLAYER_LIST:
                case InfoMessage.PLAYER_LOADING:
                    if (playindex >= 0) {
                        LogUtil.logd(getActivityTag() + "notify item index " + playindex);
                        itemAlbumSubscribeAdapter.notifyItemChanged(playindex);
                        playindex = -1;
                    }
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
