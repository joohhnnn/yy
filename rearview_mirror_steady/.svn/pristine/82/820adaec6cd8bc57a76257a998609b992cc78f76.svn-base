package com.txznet.music.historyModule.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.FavourModule.adapter.ItemAudioAdapter;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.localModule.ui.adapter.AudioViewHolder;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;

/**
 * Created by brainBear on 2018/1/16.
 */

public class HistoryFragment extends AudioBaseFragment implements HistoryContract.View {

    private static final String TAG = "HistoryFragment";
    private static final String KEY_TYPE = "KEY_TYPE";

    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @Bind(R.id.layout_library_loading_view)
    LoadingView loadingView;
    @Bind(R.id.swipe_target)
    RecyclerView rlHistory;

    private int mType;
    private HistoryContract.Presenter mPresenter;
    private ItemAudioAdapter<HistoryData> mAdapter;
    private List<HistoryData> mHistoryData = new ArrayList<>();

    public static HistoryFragment newInstance(int type) {
        HistoryFragment historyFragment = new HistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TYPE, type);
        historyFragment.setArguments(bundle);

        return historyFragment;
    }


    @Override
    public void setPresenter(HistoryContract.Presenter presenter) {
    }


    @Override
    public void showHistory(List<HistoryData> historyData) {
        Logger.d(TAG, "show history:" + historyData.size());
        mHistoryData.clear();
        mHistoryData.addAll(historyData);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeHistory(HistoryData historyData) {
        int position = mHistoryData.indexOf(historyData);
        if (position >= 0) {
            mHistoryData.remove(position);
            mAdapter.notifyItemRemoved(position);
            mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
        }
    }

    @Override
    public void showLoading() {
        loadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
    }

    @Override
    public void hideLoading() {
        loadingView.showContent();
    }

    @Override
    public void showEmpty() {
        loadingView.showEmpty("当前没有收听记录", R.drawable.local_noresult);
    }

    @Override
    public void refreshItem(int position) {
        if (position < 0) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public Context getJumpContext() {
        return getActivity();
    }

    @Override
    public String getFragmentId() {
        return "HistoryFragment";
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_history_content;
    }

    @Override
    protected void initView(View view) {
        swipeToLoadLayout.setLoadMoreEnabled(false);
        swipeToLoadLayout.setRefreshEnabled(false);

        rlHistory.setLayoutManager(new TXZLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rlHistory.setAdapter(getAdapter());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mType = bundle.getInt(KEY_TYPE);

        if (mType == HistoryContract.TYPE_MUSIC) {
            mPresenter = new HistoryPresenter(this, HistoryContract.TYPE_MUSIC);
        } else {
            mPresenter = new HistoryPresenter(this, HistoryContract.TYPE_RADIO);
        }

        mPresenter.register();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
        mPresenter = null;
    }


    private RecyclerView.Adapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ItemAudioAdapter<HistoryData>(getActivity(), mHistoryData) {
                @Override
                public void bindView(AudioViewHolder holder, HistoryData historyData, int postion) {
                    if (historyData.getAlbum() == null && historyData.getAudio() == null) {
                        Logger.d(TAG, "history empty:id=" + historyData.getId() + ",sid=" + historyData.getSid() + ",albumId=" + historyData.getAlbumRowId() + ",audioId=" + historyData.getAudioRowId());
                        return;
                    }

                    if (holder.mTitleAuthor != null) {
                        holder.mTitleAuthor.setVisibility(View.GONE);
                    }

                    if (historyData.getType() == HistoryData.TYPE_AUDIO) {
                        Audio song = historyData.getAudio();
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
                        if (FavorHelper.isSupportFavour(song)) {
                            holder.mIvFavour.setVisibility(View.VISIBLE);
                            if (FavorHelper.isFavour(song)) {
                                holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_favorite_small));
                            } else {
                                holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_not_favorite_small));
                            }
                        } else {
                            holder.mIvFavour.setVisibility(View.GONE);
                        }
                        //瞎来
                        if (ScreenUtils.isPhonePortrait()) {
                            if (songColor == Color.WHITE) {
                                isPhoneSettingItemTitle(holder, song, false);
                            } else {
                                isPhoneSettingItemTitle(holder, song, true);
                            }
                        } else {
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
                        }


                    } else {
                        Album album = historyData.getAlbum();
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (null != currentAlbum && currentAlbum.getId() == album.getId() && currentAlbum.getSid() == album.getSid()) {
                            holder.mTitle.setTextColor(getResources().getColor(R.color.color_selected));

                            holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_playing));
                            holder.mTitle.setTextColor(getResources().getColor(R.color.color_selected));
                        } else {
                            holder.mTitle.setTextColor(Color.WHITE);

                            holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_normal));
                            holder.mTitle.setTextColor(getResources().getColor(R.color.play_list_item_name_normal));
                        }

                        holder.mTitle.setText(album.getName());
                        if (FavorHelper.isSupportSubscribe(album)) {
                            holder.mIvFavour.setVisibility(View.VISIBLE);
                            if (FavorHelper.isSubscribe(album)) {
                                holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_subscribed_small));
                            } else {
                                holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_not_subscribe_small));
                            }
                        } else {
                            holder.mIvFavour.setVisibility(View.INVISIBLE);
                        }

                    }
                }
            };
            mAdapter.setOnItemClickListener(new ItemAudioAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    mPresenter.play(mHistoryData, position);
                }
            });
            mAdapter.setOnFavourListener(new ItemAudioAdapter.OnFavourListener() {
                @Override
                public void onFavour(int position) {
                    mPresenter.favor(mHistoryData.get(position));
                }
            });
            mAdapter.setOnDeleteListener(new ItemAudioAdapter.OnDeleteListener<HistoryData>() {
                @Override
                public void onDelete(HistoryData historyData) {
                    //这里要是双击就会发生crash
                    mPresenter.delete(historyData);
                }
            });
        }
        return mAdapter;
    }
}
