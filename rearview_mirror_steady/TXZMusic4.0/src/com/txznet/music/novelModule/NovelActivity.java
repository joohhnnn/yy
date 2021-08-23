package com.txznet.music.novelModule;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.percent.PercentRelativeLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumBaseAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRecommandActAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.data.entity.Category;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.layout.TXZGridLayoutManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SpaceItemDecoration;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.LoadingView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.txznet.music.playerModule.logic.PlayInfoManager.TYPE_NORMAL_ALBUM;

/**
 * Created by telenewbie on 2017/12/20.
 */

public class NovelActivity extends BaseBarActivity implements Observer {
    public static final String KEY_CATEGORY = "key_category";
    @Bind(R.id.ll_left_back)
    LinearLayout llLeftBack;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.iv_delete)
    ImageView ivDelete;
    @Bind(R.id.ll_delete_rage)
    PercentRelativeLayout llDeleteRage;
    @Bind(R.id.recyclerview)
    RecyclerView mALbumRecycleView;
    @Bind(R.id.layout_library_loading_view)
    LoadingView layoutLibraryLoadingView;
    @Bind(R.id.player_bar)
    BarPlayerView playerBar;

    protected ItemAlbumBaseAdapter mAlbumAdapter;
    @Bind(R.id.choice_bg)
    ImageView choiceBg;
    private Category mCategory;
    private List<Album> mAlbum = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;

    protected int lastVisibleItem;
    private int pageOff;
    private int playindex = -1;

    protected AdapterView.OnItemClickListener getAlbumItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) mAlbumAdapter.getItem(position);
                playALbum(album, false);
            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemIconClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) mAlbumAdapter.getItem(position);
                playALbum(album, true);
            }
        };
    }

    public RecyclerView.Adapter getAdapter() {
        if (mAlbumAdapter == null) {
            mAlbumAdapter = new ItemAlbumRecommandActAdapter(this, mAlbum);
            mAlbumAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
            mAlbumAdapter.setOnItemClickListener(getAlbumItemClickListener());
        }
        return mAlbumAdapter;
    }

    @Override
    protected String getActivityTag() {
        if (mCategory != null) {
            return "NovelActivity#" + this.hashCode() + "/" + mCategory.getDesc();
        } else {
            return "NovelActivity#" + this.hashCode();
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
        mCategory = (Category) getIntent().getSerializableExtra(KEY_CATEGORY);
        ObserverManage.getObserver().addObserver(this);

        initData();
        if (ScreenUtils.isPhonePortrait()) {
            tvTitle.setTextColor(Color.WHITE);
        }
        tvTitle.setText(mCategory.getDesc());
        layoutLibraryLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
        AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), 1);
        ivDelete.setVisibility(View.GONE);

        layoutLibraryLoadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), 1);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManage.getObserver().deleteObserver(this);
    }

    private void initData() {
        pageOff = 0;
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
            ViewGroup.LayoutParams layoutParams = mALbumRecycleView.getLayoutParams();
            layoutParams.width = right * 2 + (int) AttrUtils.getAttrDimension(this, R.attr.album_item_content_size_width, 0) * 2 + left * 2;
            mALbumRecycleView.setLayoutParams(layoutParams);
            itemDecoration = new SpaceItemDecoration(left, bottom, right);
        } else {
            mLayoutManager = new TXZLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            itemDecoration = new AlbumDecoration((int) AttrUtils.getAttrDimension(this, R.attr.album_item_gap, 0));
        }
        mALbumRecycleView.setLayoutManager(mLayoutManager);
        mALbumRecycleView.addItemDecoration(itemDecoration);
        mALbumRecycleView.setHasFixedSize(true);

        mALbumRecycleView.setAdapter(getAdapter());


        mALbumRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (null != mAlbumAdapter && mCategory != null && !mAlbumAdapter.isShowLoading() && newState == 0 && lastVisibleItem + 1 >= mAlbumAdapter.getItemCount() && isNewClick) {
                    mAlbumAdapter.setShowLoading(true);
                    AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), pageOff + 1);
                }
                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mLayoutManager instanceof TXZGridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                } else if (mLayoutManager instanceof LinearLayoutManager) {
                    lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                }
                isNewClick = true;
            }

        });
    }

    @OnClick(R.id.ll_left_back)
    public void onViewClicked() {
        finish();
    }


    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            LogUtil.logd(TAG + "[" + getActivityTag() + "]" + "reqData:info type:" + info.getType());
            switch (info.getType()) {
                case InfoMessage.NET_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET:
                    showLoading(Constant.RS_VOICE_SPEAK_NONE_NET);
                    break;
                case InfoMessage.NET_TIMEOUT_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT:
                    showLoading(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN:
                    showLoading(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
                    break;
                case InfoMessage.RESP_ALBUM:
                    ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info.getObj();
                    if (null != mCategory && String.valueOf(mCategory.getCategoryId()).equals(responseAlbum.getCategoryId())) {
                        pageOff = responseAlbum.getPageId();
                        handleAlbum(responseAlbum.getArrAlbum(), pageOff != 1);
                    } else {
                        LogUtil.logw(TAG + "[" + getActivityTag() + "]" + (mCategory != null ? mCategory.getCategoryId() : "null") + "/" + responseAlbum.getCategoryId());
                    }
                    break;
                case InfoMessage.PLAY:
                case InfoMessage.PAUSE:
                    if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                        playindex = mAlbum.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
                        LogUtil.logd(getActivityTag() + "notify item index i=" + playindex);
                        mAlbumAdapter.notifyItemChanged(playindex);
                    }
                    break;

                case InfoMessage.PLAYER_LIST:
                case InfoMessage.PLAYER_LOADING:
                    if (playindex >= 0) {
                        LogUtil.logd(getActivityTag() + "notify item index " + playindex);
                        mAlbumAdapter.notifyItemChanged(playindex);
                        playindex = -1;
                    }
            }
        }

    }

    private void showLoading(String content) {
        if (layoutLibraryLoadingView.isShowLoading()) {
            layoutLibraryLoadingView.showError(content, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
        } else {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 处理category分类
     */
    private void handleAlbum(List<Album> albums, boolean isAdd) {
        int startIndex = 0;
        int count = albums.size();

        mAlbumAdapter.setShowLoading(false);
        if (!isAdd) {
            int removeSize = mAlbum.size();
            layoutLibraryLoadingView.showContent();
            mAlbum.clear();
            mAlbumAdapter.notifyItemRangeRemoved(0, removeSize);
        } else {
            startIndex = mAlbum.size();
        }
        mAlbum.addAll(albums);
        mAlbumAdapter.notifyItemRangeInserted(startIndex, count);
    }


    private void playALbum(final Album album, final boolean needplay) {
        final long categoryID;
        if (CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
            categoryID = album.getArrCategoryIds().get(0);
        } else {
            categoryID =/*0;*/mCategory.getCategoryId();
        }
        Intent intent = new Intent(this, ReserveConfigSingleTaskActivity2.class);
        this.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_ALBUM, album, categoryID, null, TYPE_NORMAL_ALBUM));
    }
}
