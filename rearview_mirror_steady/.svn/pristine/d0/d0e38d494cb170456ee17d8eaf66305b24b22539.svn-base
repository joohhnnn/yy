package com.txznet.music.localModule.ui;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.FavourModule.adapter.ItemAudioCheckAdapter;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.localModule.LocalContract;
import com.txznet.music.localModule.LocalMusicPresenter;
import com.txznet.music.localModule.ui.adapter.AudioViewCheckHolder;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.LoadingProgress;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LoadingView;
import com.txznet.music.widget.TipsDialog;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by brainBear on 2018/1/10.
 */

public class LocalMusicFragment extends AudioBaseFragment implements LocalContract.View {

    private static final String TAG = "LocalMusicFragment";
    //    @Bind(R.id.tv_count)
//    TextView tvCount;
    @Bind(R.id.btn_delete)
    Button btnDelete;
    @Bind(R.id.btn_scan)
    Button btnScan;

    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @Bind(R.id.layout_library_loading_view)
    LoadingView loadingView;
    @Bind(R.id.swipe_target)
    RecyclerView rlLocal;
    @Bind(R.id.checkbox1)
    AppCompatCheckBox checkBox;
    private LocalMusicPresenter mPresenter;
    private List<Audio> mAudios;
    private ItemAudioCheckAdapter<Audio> mAdapter;
    private boolean mScanning = false;
    int btnDeleteWidth = 0;

    @Override
    public String getFragmentId() {
        return "LocalMusicFragment";
    }

    @Override
    protected int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.fragment_local_phone_portrait;
        }
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

        //设置CheckBox的图片大小
        //创建Drawable对象
        final Drawable drawable = getActivity().getResources().getDrawable(R.drawable.message_selector);
        //设置drawable的位置,宽高
        drawable.setBounds(0, 0, getActivity().getResources().getDimensionPixelOffset(R.dimen.m24), getActivity().getResources().getDimensionPixelOffset(R.dimen.m24));
        checkBox.setCompoundDrawables(drawable, null, null, null);
        checkBox.setCompoundDrawablePadding(getActivity().getResources().getDimensionPixelOffset(R.dimen.m16));
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudios == null) {
                    return;
                }

                mAdapter.checkItemsAll(mAudios, checkBox.isChecked());
                mPresenter.checkItems(mAudios, checkBox.isChecked());

                refreshItem(-1);
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getCheckedList().size() == 0) {
                    ToastUtils.showShort("你还没有选中要删除的歌曲哦");
                } else {
                    TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
                    tipsDialogBuildData.setTitle("确定删除选中的歌曲?");
                    tipsDialogBuildData.setContext(getActivity());
                    tipsDialogBuildData.setWindowType(WindowManager.LayoutParams.TYPE_APPLICATION);
                    final TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
                    tipsDialog.setSureListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mPresenter.deleteLocalAudio(mAdapter.getCheckedList(), true);
                            mAdapter.checkItemsAll(mAdapter.getCheckedList(), false);
                        }
                    });
                    tipsDialog.showImediately();

                }
            }
        });

        ViewGroup.LayoutParams layoutParams = btnDelete.getLayoutParams();
        btnDeleteWidth = layoutParams.width;

        if (mAudios == null) {
            hideViewWhenScanBegin();
        }
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

        hideViewWhenScanBegin();

    }

    public void hideViewWhenScanBegin() {
        checkBox.setVisibility(View.GONE);
        ViewGroup.LayoutParams layoutParams = btnDelete.getLayoutParams();
        layoutParams.width = 0;
        btnDelete.setLayoutParams(layoutParams);
        btnDelete.setVisibility(View.INVISIBLE);
    }

    public void showViewWhenScanEnd() {
        checkBox.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = btnDelete.getLayoutParams();
        layoutParams.width = btnDeleteWidth;
        btnDelete.setLayoutParams(layoutParams);
        btnDelete.setVisibility(View.VISIBLE);
    }


    @Override
    public void dismissScanning() {
        mScanning = false;
//        loadingView.hideLoading();
        showContent();
        btnScan.setText("开始扫描");
    }

    @Override
    public void showLocalData(List<Audio> localAudios) {
        Logger.d(TAG, "show data:" + localAudios.size());
        mAudios = localAudios;
        mAdapter.replaceData(mAudios);
        mAdapter.notifyDataSetChanged();
        if (CollectionUtils.isNotEmpty(mAudios)) {
            if (loadingView.isShowEmpty()) {
                showContent();
            }
            showViewWhenScanEnd();
        }
//        tvCount.setText(String.format(getActivity().getResources().getString(R.string.total_song), String.valueOf(localAudios.size())));
    }

    @Override
    public void showEmpty() {
        loadingView.showEmpty("你还没有本地的音乐哦", R.drawable.fm_me_no_file);
    }

    @Override
    public void showContent() {
        loadingView.showContent();
    }

    @Override
    public void showScanCount(int count) {
        loadingView.setLoadingText(String.format(Locale.getDefault(), "扫描到%d首歌曲", count));
    }

    private RecyclerView.Adapter getAdapter() {
        mAdapter = new ItemAudioCheckAdapter<Audio>(getActivity(), null) {
            @Override
            public void bindView(AudioViewCheckHolder holder, Audio song, int postion) {
                int songColor;
                int artistColor;
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                //本地音乐的高亮，只有在本地进入播放的时候才高亮。用户在专辑播放的时候，本地音乐不高亮。用户在语音搜索歌曲播放的时候不高亮。
                if (null != currentAudio && currentAudio.getId() == song.getId() && currentAudio.getSid() == song.getSid() && PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_LOCAL) {
                    songColor = getResources().getColor(R.color.color_selected);
                    artistColor = getResources().getColor(R.color.color_selected);
//                    holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_playing));
                    holder.mTitle.setTextColor(getResources().getColor(R.color.color_selected));
                } else {
                    songColor = Color.WHITE;
                    artistColor = Color.parseColor("#adb6cc");

//                    holder.mIvLeftIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_playlist_status_normal));
                    holder.mTitle.setTextColor(getResources().getColor(R.color.play_list_item_name_normal));
                }

                holder.cbCheck.setChecked(isCheck(song));
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

                if (FavorHelper.isFavour(song)) {
                    holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_favorite_small));
                } else {
                    holder.mIvFavour.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_not_favorite_small));
                }
            }
        };
        mAdapter.setOnItemClickListener(new ItemAudioCheckAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                mPresenter.play(mAudios, position);
            }
        });
        mAdapter.setOnFavourListener(new ItemAudioCheckAdapter.OnFavourListener() {
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
        mAdapter.setOnCheckListener(new ItemAudioCheckAdapter.OnCheckListener() {
            @Override
            public void onCheck(int position, boolean b) {
                Logger.d("test:::check", "onCheck:" + position + "," + b);
                Audio audio = mAudios.get(position);
                mPresenter.checkItem(audio, b);
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
//        tvCount.setText(format);
    }

    @Override
    public void deleteAudios(List<Audio> audios, boolean isSuccess) {
        mAudios.removeAll(audios);
        if (isSuccess) {
            if (CollectionUtils.isEmpty(mAudios)) {
                //TODO 刪除完歌曲
                loadingView.showEmpty("当前文件被飞船吸走了", R.drawable.fm_me_no_file);
            }
            refreshItem(-1);
        } else {
            ToastUtils.showShortOnUI("删除成功,但是部分歌曲不能删除文件");
        }
    }

    @Override
    public void hideCheckAllBtn(boolean hideBtn) {
        if (hideBtn) {
            checkBox.setVisibility(View.GONE);
            ViewGroup.LayoutParams layoutParams = btnDelete.getLayoutParams();
            layoutParams.width = 0;
            btnDelete.setLayoutParams(layoutParams);
            btnDelete.setVisibility(View.INVISIBLE);
        }
    }

    Dialog dialog = null;

    @Override
    public void showLoadingView(boolean b) {
        if (b) {
            LoadingProgress.getInstance(getActivity()).show("正在删除中...");
        } else {
            LoadingProgress.getInstance(getActivity()).dismiss();
        }

//        if (b) {
//            TipsDialog.TipsDialogBuildData data = new TipsDialog.TipsDialogBuildData();
//            data.setTitle("test");
//            data.setContent("这是加载动画");
//            if (tipsDialog == null) {
//                tipsDialog = new TipsDialog(data);
//            }
//            tipsDialog.show();
//        } else {
//            if (tipsDialog != null) {
//                tipsDialog.dismiss("auto");
//            }
//        }

        //弹框
    }

    @Override
    public void showAllCheckState(boolean isCheckState) {
        checkBox.setChecked(isCheckState);
    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
////        hidden
//        Log.e(TAG, "onHiddenChanged: " + hidden);
//        if (!hidden && !mScanning) {
//            mPresenter.getLocalAudio();
//        }
//        super.onHiddenChanged(hidden);
//    }
}
