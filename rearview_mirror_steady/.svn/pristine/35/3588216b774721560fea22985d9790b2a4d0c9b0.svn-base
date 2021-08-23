package com.txznet.music.SubscribeModule.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.SubscribeModule.ui.adapter.ItemAlbumSubscribeAdapter;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.ui.layout.TXZGridLayoutManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SpaceItemDecoration;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.LoadingView;
import com.txznet.music.widget.OnEffectiveClickListener;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;
import com.txznet.txz.util.TXZFileConfigUtil;

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

    RecyclerView.LayoutManager mLayoutManager;

    private int pageOff;
    private int playindex = -1;


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
                    ReportEvent.clickMineSubscribePlay(album);
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
            ToastUtils.showShortOnUI("该专辑已下架");
            return;
        }
        if (ActivityStack.getInstance().currentActivity().hashCode() == this.hashCode()) {
            Utils.jumpToPlayerUI(this, PlayInfoManager.DATA_SUBSCRIBE, album, categoryID);
        }
    }

    @Override
    public int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.act_subscribe_phone_portrait;
        }
        return R.layout.act_subscribe;
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        if (ScreenUtils.isPhonePortrait()) {
            tvTitle.setTextColor(Color.WHITE);
        }
        tvTitle.setText("订阅的节目");
        itemAlbumSubscribeAdapter = new ItemAlbumSubscribeAdapter(this, albums);
        itemAlbumSubscribeAdapter.notifyDataSetChanged();
        itemAlbumSubscribeAdapter.setOnItemClickListener(getAlbumItemIconClickListener());
        itemAlbumSubscribeAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());


        RecyclerView.ItemDecoration itemDecoration = null;
        if (ScreenUtils.isPhonePortrait()) {
            int colCount = 4;
            int defCol = 3;
            if (ScreenUtils.isPhonePortrait()) {
                defCol = 3;
            } else {
                defCol = 4;
            }
            colCount = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_ALBUM_COL_NUM, defCol);
            mLayoutManager = new TXZGridLayoutManager(this, colCount);
            int right = (int) AttrUtils.getAttrDimension(this, R.attr.album_item_horizontal_gap, 0);
            int left = (int) AttrUtils.getAttrDimension(this, R.dimen.y45, 45);
            int bottom = (int) AttrUtils.getAttrDimension(this, R.attr.album_item_gap, 0);
            ViewGroup.LayoutParams layoutParams = recyclerview.getLayoutParams();
            layoutParams.width = right * 2 + (int) AttrUtils.getAttrDimension(this, R.attr.album_item_content_size_width, 0) * 2 + left * 2;
            recyclerview.setLayoutParams(layoutParams);
            itemDecoration = new SpaceItemDecoration(left, bottom, right);
        } else {
            mLayoutManager = new TXZLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            itemDecoration = new AlbumDecoration((int) AttrUtils.getAttrDimension(this, R.attr.album_item_gap, 0));
        }

        recyclerview.setLayoutManager(mLayoutManager);
        recyclerview.addItemDecoration(itemDecoration);
        recyclerview.setAdapter(itemAlbumSubscribeAdapter);
        recyclerview.addItemDecoration(new AlbumDecoration((int) AttrUtils.getAttrDimension(this, R.attr.album_item_gap, 0)));
        recyclerview.setHasFixedSize(true);
        ivDelete.setVisibility(View.GONE);
        initLinstener();
        requestData();
        ObserverManage.getObserver().addObserver(this);
    }

    private void initLinstener() {
        loadingView.setEmptyHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到首页的音乐
                Intent intent = new Intent(MySubscribeActivity.this, ReserveConfigSingleTaskActivity1.class);
                intent.putExtra(Utils.KEY_TYPE, HomeActivity.RADIO_i);
                MySubscribeActivity.this.startActivity(intent);
                MySubscribeActivity.this.finish();
            }
        });

        loadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestData();
            }
        });


        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //得到当前显示的最后一个item的view
                boolean isLoad = false;
                if (mLayoutManager instanceof TXZGridLayoutManager) {
                    if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset()
                            >= recyclerView.computeVerticalScrollRange()) {

                        LogUtil.d("recyclerViewTest", "recyclerView.computeVerticalScrollExtent():" + recyclerView.computeVerticalScrollExtent());
                        LogUtil.d("recyclerViewTest", "recyclerView.computeVerticalScrollOffset():" + recyclerView.computeVerticalScrollOffset());
                        LogUtil.d("recyclerViewTest", "recyclerView.computeVerticalScrollRange():" + recyclerView.computeVerticalScrollRange());
                        isLoad = true;
                    }
                } else if (mLayoutManager instanceof LinearLayoutManager) {
                    if (recyclerView.computeHorizontalScrollExtent() + recyclerView.computeHorizontalScrollOffset()
                            >= recyclerView.computeHorizontalScrollRange()) {
                        LogUtil.d("recyclerViewTest", "recyclerView.computeHorizontalScrollExtent():" + recyclerView.computeHorizontalScrollExtent());
                        LogUtil.d("recyclerViewTest", "recyclerView.computeHorizontalScrollOffset():" + recyclerView.computeHorizontalScrollOffset());
                        LogUtil.d("recyclerViewTest", "recyclerView.computeHorizontalScrollRange():" + recyclerView.computeHorizontalScrollRange());
                        isLoad = true;
                    }
                }

                if (isLoad) {
                    if (null != itemAlbumSubscribeAdapter && !itemAlbumSubscribeAdapter.isShowLoading() && newState == 0 && isNewClick) {
                        final Album album = albums.get(albums.size() - 1);
                        itemAlbumSubscribeAdapter.setShowLoading(true);
                        FavorHelper.getSubscribeData(album.getSid(), album.getId(), album.getOperTime(), 10, new FavorHelper.SendFavourListener<SubscribeBean>() {
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
                }


                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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
        loadingView.showLoading(R.drawable.fm_local_scan, R.drawable.fm_local_scan_logo, "加载订阅的节目中...");
        FavorHelper.getSubscribeData(0, 0, 0L, 10, new FavorHelper.SendFavourListener<SubscribeBean>() {
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
                loadingView.showError("网络不给力，数据加载失败", R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
            }
        });
    }

    @Override
    protected String getActivityTag() {
        return "MySubscribeActivity#" + this.hashCode() + "/订阅的节目";
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
                if (CollectionUtils.isNotEmpty(itemAlbumSubscribeAdapter.getDeleteAlbums())) {
                    FavorHelper.unSubscribeRadio(itemAlbumSubscribeAdapter.getDeleteAlbums(), EnumState.Operation.manual);
                    ReportEvent.clickMineSubscribeDelete(itemAlbumSubscribeAdapter.getDeleteAlbums());
                } else {
                    ToastUtils.showShortOnUI("暂未选中任何专辑");
                }
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
