package com.txznet.music.localModule.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.FavourModule.adapter.ItemAudioAdapter;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.localModule.LocalContract;
import com.txznet.music.localModule.LocalMusicPresenter;
import com.txznet.music.localModule.ui.adapter.AudioViewHolder;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LoadingView;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by brainBear on 2018/1/10.
 */

public class LocalMusicFragment extends AudioBaseFragment implements LocalContract.View {

    private static final String TAG = "LocalMusicFragment";
    @Bind(R.id.tv_count)
    TextView tvCount;
    @Bind(R.id.btn_scan)
    Button btnScan;
    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @Bind(R.id.layout_library_loading_view)
    LoadingView loadingView;
    @Bind(R.id.swipe_target)
    RecyclerView rlLocal;
    private LocalMusicPresenter mPresenter;
    private List<Audio> mAudios;
    private ItemAudioAdapter<Audio> mAdapter;
    private boolean mScanning = false;

    @Override
    public String getFragmentId() {
        return "LocalMusicFragment";
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_local;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {


        mPresenter = new LocalMusicPresenter(this);
        mPresenter.register();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        mPresenter.unregister();
        super.onDestroyView();
    }

    @Override
    protected void initView(View view) {
        swipeToLoadLayout.setRefreshEnabled(false);
        swipeToLoadLayout.setLoadMoreEnabled(false);

        rlLocal.setLayoutManager(new TXZLinearLayoutManager(getActivity()));
        rlLocal.setAdapter(getAdapter());
    }

    @OnClick(R.id.btn_scan)
    public void onScanClick() {
        if (mScanning) {
            mPresenter.stopScan();
        } else {
            mPresenter.startScan();
        }
    }

    @Override
    public void setPresenter(LocalContract.Presenter presenter) {

    }

    @Override
    public void showScanning() {
        mScanning = true;
        loadingView.showLoading(R.drawable.fm_local_scan, R.drawable.fm_local_scan_logo, "正在为您扫描...");
        btnScan.setText("停止扫描");
    }

    @Override
    public void dismissScanning() {
        mScanning = false;
//        loadingView.hideLoading();
        loadingView.showContent();
        btnScan.setText("开始扫描");
    }

    @Override
    public void showLocalData(List<Audio> localAudios) {
        Logger.d(TAG, "show data:" + localAudios.size());
        mAudios = localAudios;
        mAdapter.replaceData(mAudios);
        mAdapter.notifyDataSetChanged();
        tvCount.setText(String.format(getActivity().getResources().getString(R.string.total_song), String.valueOf(localAudios.size())));
    }

    @Override
    public void showEmpty() {
        loadingView.showEmpty("你还没有本地的音乐哦", R.drawable.fm_me_no_file);
    }

    @Override
    public void showScanCount(int count) {
        loadingView.setLoadingText(String.format(Locale.getDefault(), "扫描到%d首歌曲", count));
    }

    private RecyclerView.Adapter getAdapter() {
        mAdapter = new ItemAudioAdapter<Audio>(getActivity(), null) {
            @Override
            public void bindView(AudioViewHolder holder, Audio song, int postion) {
                int songColor;
                int artistColor;
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                if (null != currentAudio && currentAudio.getId() == song.getId() && currentAudio.getSid() == song.getSid()) {
                    songColor = getResources().getColor(R.color.color_selected);
                    artistColor = getResources().getColor(R.color.color_selected);

                    holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_playing));
                    holder.mTitle.setTextColor(getResources().getColor(R.color.color_selected));
                } else {
                    songColor = Color.WHITE;
                    artistColor = Color.parseColor("#adb6cc");

                    holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_normal));
                    holder.mTitle.setTextColor(getResources().getColor(R.color.play_list_item_name_normal));
                }

                if (Utils.isSong(song.getSid()) && null != song.getArrArtistName() && !song.getArrArtistName().isEmpty()) {
                    String songName = song.getName();
                    String artistName = StringUtils.toString(song.getArrArtistName());
                    String name = String.format(Locale.getDefault(), "%s - %s", songName, artistName);
                    SpannableString spannableString = new SpannableString(name);
                    spannableString.setSpan(new ForegroundColorSpan(songColor), 0, songName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(artistColor)
                            , songName.length() + 1, name.length()
                            , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                    holder.mTitle.setText(spannableString);
                } else {
                    holder.mTitle.setText(song.getName());
                }

                if (FavorHelper.isFavour(song)) {
                    holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_favorite_small));
                } else {
                    holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_not_favorite_small));
                }
            }
        };
        mAdapter.setOnItemClickListener(new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                mPresenter.play(mAudios, position);
            }
        });
        mAdapter.setOnFavourListener(new ItemAudioAdapter.OnFavourListener() {
            @Override
            public void onFavour(int position) {
                Audio audio = mAudios.get(position);
                if (!FavorHelper.isFavour(audio)) {
                    mPresenter.favor(audio);
                } else {
                    mPresenter.unFavor(audio);
                }
            }
        });
        mAdapter.setOnDeleteListener(new ItemAudioAdapter.OnDeleteListener() {
            @Override
            public void onDelete(int position) {
                mPresenter.deleteLocalAudio(mAudios, position);
            }
        });
        return mAdapter;
    }

    @Override
    public void refreshItem(int position) {
        if (position < 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemChanged(position);
        }
        String format = String.format(getActivity().getResources().getString(R.string.total_song), String.valueOf(null == mAudios ? 0 : mAudios.size()));
        tvCount.setText(format);
    }
}
