package com.txznet.music.ui.local;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.store.LocalAudioStore;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.dialog.DeleteAudioDialog;
import com.txznet.music.widget.dialog.LocalSortTypeDialog;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.extensions.aac.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 本地音乐界面
 *
 * @author zackzhou
 * @date 2018/12/4,14:56
 */
public class LocalMusicFragment extends BaseFragment {

    public static boolean isFirstLaunch = true; // 是否首次启动

    @Bind(R.id.cb_choice)
    CheckBox cbAll;
    @Bind(R.id.tv_choice)
    TextView tvChoice;

    @Bind(R.id.ll_delete_bar)
    ViewGroup ll_delete_bar;
    @Bind(R.id.rl_action_bar)
    ViewGroup rl_action_bar;
    @Bind(R.id.rv_data)
    EasyRecyclerView lv_queue;
    @Bind(R.id.tiv_scan)
    TextView btnScan;
    @Bind(R.id.tiv_delete)
    TextView btnDelete;
    @Bind(R.id.iv_logo)
    ImageView ivScanMini;

    TextView mInnerScanCount;

    LocalAudioStore mLocalStore; // View所需要的一些数据集合
    PlayInfoStore mPlayInfoStore;
    RecyclerAdapter<LocalAudio> mLocalAudioAdapter;
    List<LocalAudio> mCheckedItem;
    List<AudioV5> mFavourAudio;

    ValueAnimator mRotateAnimator;
    ValueAnimator mRotateAnimatorMini;

    boolean isDeleteMgr; // 是否处于删除模式

    @Override
    protected int getLayout() {
        return R.layout.local_fragment;
    }

    @Override
    protected void initView(View view) {
        mCheckedItem = new ArrayList<>();
        mFavourAudio = new ArrayList<>();

        tvTitle.setText(R.string.local_title);
        mLocalAudioAdapter = new RecyclerAdapter<LocalAudio>(getContext(), new ArrayList<>(0), R.layout.base_recycle_item_audio) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, LocalAudio item) {
                TextView tv_index = (TextView) holder.getView(R.id.tv_index);
                ImageView iv_playing = (ImageView) holder.getView(R.id.iv_playing);
                TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                TextView tv_artist = (TextView) holder.getView(R.id.tv_artist);
                CheckBox cb_checked = (CheckBox) holder.getView(R.id.cb_checked);
                CheckBox cb_favour = (CheckBox) holder.getView(R.id.cb_favour);

                if (isDeleteMgr) {
                    cb_checked.setVisibility(View.VISIBLE);
                    cb_favour.setVisibility(View.GONE);
                    tv_index.setVisibility(View.INVISIBLE);
                } else {
                    cb_checked.setVisibility(View.GONE);
                    cb_favour.setVisibility(View.VISIBLE);
                    tv_index.setVisibility(View.VISIBLE);
                    tv_index.setText(position + 1 + "");

                    if (item.isFavour) {
                        cb_favour.setChecked(true);
                    } else {
                        cb_favour.setChecked(false);
                    }
                    cb_favour.setOnClickListener(v -> {
                        CheckBox cb = (CheckBox) v;
                        if (cb.isChecked()) {
                            FavourActionCreator.getInstance().favour(Operation.MANUAL, AudioConverts.convertAudio2FavourAudio(item), "local");
                            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_FAVOUR);
                        } else {
                            FavourActionCreator.getInstance().unFavour(Operation.MANUAL, AudioConverts.convertAudio2FavourAudio(item), "local");
                            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_FAVOUR_CANCEL);
                        }
                    });
                }

                tv_name.setText(item.name);
                tv_artist.setText(StringUtils.toString(item.artist));
                if (TextUtils.isEmpty(tv_artist.getText())) {
                    tv_artist.setText(Constant.UNKNOWN);
                }

                if (!isDeleteMgr && item.equals(mPlayInfoStore.getCurrPlaying().getValue())) {
                    tv_index.setVisibility(View.INVISIBLE);
                    iv_playing.setVisibility(View.VISIBLE);
                    tv_name.setTextColor(getResources().getColor(R.color.red));
                    tv_artist.setTextColor(getResources().getColor(R.color.red_40));
                } else {
                    if (!isDeleteMgr) {
                        tv_index.setVisibility(View.VISIBLE);
                    }
                    iv_playing.setVisibility(View.GONE);
                    tv_name.setTextColor(getResources().getColor(R.color.white));
                    tv_artist.setTextColor(getResources().getColor(R.color.white_40));
                }
                cb_checked.setOnClickListener(v -> {
                    CheckBox rb = (CheckBox) v;
                    if (rb.isChecked()) {
                        mCheckedItem.add(item);
                    } else {
                        mCheckedItem.remove(item);
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                });

                if (mCheckedItem.contains(item)) {
                    cb_checked.setChecked(true);
                } else {
                    cb_checked.setChecked(false);
                }

                if (isDeleteMgr) {
                    holder.itemView.setOnClickListener(v -> {
                        cb_checked.performClick();
                    });
                } else {
                    holder.itemView.setOnClickListener(v -> {
                        PlayerActionCreator.get().playLocal(Operation.MANUAL, mLocalAudioAdapter.getData(), position);
                    });
                }

                if (getCount() != 0 && mCheckedItem.size() == getCount()) {
                    cbAll.setChecked(true);
                } else {
                    cbAll.setChecked(false);
                }
            }
        };

        // 全选按钮
        cbAll.setOnClickListener(v -> {
            CheckBox cb = (CheckBox) v;
            mCheckedItem.clear();
            if (cb.isChecked()) {
                mCheckedItem.addAll(mLocalAudioAdapter.getData());
            }
            mLocalAudioAdapter.notifyDataSetChanged();
        });
        tvChoice.setOnClickListener(v -> cbAll.performClick());

        lv_queue.setLayoutManager(new LinearLayoutManager(getContext()));
        lv_queue.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));

        mInnerScanCount = lv_queue.getProgressView().findViewById(R.id.tv_scan_count);
        lv_queue.getEmptyView().findViewById(R.id.btn_scan).setOnClickListener(v -> {
            lv_queue.showProgress();
            startScanAnim();
            hideSubTitle();
            LocalActionCreator.get().scan(Operation.MANUAL);
        });
        lv_queue.getProgressView().findViewById(R.id.btn_cancel_scan).setOnClickListener(v -> {
            LocalActionCreator.get().cancelScan(Operation.MANUAL);
        });
        lv_queue.getRecyclerView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        scanMask = lv_queue.getProgressView().findViewById(R.id.iv_scan_mask);
        mRotateAnimator = ObjectAnimator.ofFloat(scanMask, "rotation", 0, 360);
        mRotateAnimator.setDuration(1500);
        mRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnimator.setInterpolator(new LinearInterpolator());

        mRotateAnimatorMini = ObjectAnimator.ofFloat(ivScanMini, "rotation", 0, 360);
        mRotateAnimatorMini.setDuration(1500);
        mRotateAnimatorMini.setRepeatCount(ValueAnimator.INFINITE);
        mRotateAnimatorMini.setInterpolator(new LinearInterpolator());

        ll_delete_bar.setVisibility(View.GONE);
        rl_action_bar.setVisibility(View.VISIBLE);

        lv_queue.setAdapter(mLocalAudioAdapter);
    }

    private View scanMask;

    // 顶部栏显示扫描中
    private void showSubTitleScan() {
        tvSubTitle.setText("正在扫描");
        ivScanMini.setImageResource(R.drawable.local_scanning_icon_small);
        ivScanMini.setVisibility(View.VISIBLE);
        mRotateAnimatorMini.start();
        btnScan.setText("暂停扫描");
    }

    // 顶部栏显示扫描个数
    private void showSubTitleResult() {
        if (mLocalAudioAdapter.getCount() == 0) {
            tvSubTitle.setText(null);
        } else {
            tvSubTitle.setText(String.format("已同步%s首歌曲", mLocalAudioAdapter.getCount()));
        }
        ivScanMini.setImageResource(0);
        ivScanMini.setVisibility(View.GONE);
        mRotateAnimatorMini.cancel();
        btnScan.setText("开始扫描");
    }

    // 隐藏顶部栏扫内容
    private void hideSubTitle() {
        tvSubTitle.setText(null);
        ivScanMini.setImageResource(0);
        ivScanMini.setVisibility(View.GONE);
        mRotateAnimatorMini.cancel();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        // init store
        mLocalStore = ViewModelProviders.of(getActivity()).get(LocalAudioStore.class);

        // bind observer
        mLocalStore.getAudioList().observe(this, audioList -> {
            if (audioList == null) {
                return;
            }
            Boolean isScanning = mLocalStore.isScanning().getValue();
            if (audioList.isEmpty()) {
                rl_action_bar.setVisibility(View.GONE);
                mLocalAudioAdapter.refresh(audioList);
                mLocalAudioAdapter.notifyDataSetChanged();
                if (isScanning == null || !isScanning) {
                    showSubTitleResult();
                    stopScanAnim();
                } else {
                    lv_queue.showProgress();
                    startScanAnim();
                }
            } else {
                if (View.VISIBLE != ll_delete_bar.getVisibility()) {
                    rl_action_bar.setVisibility(View.VISIBLE);
                }
                mLocalAudioAdapter.refresh(audioList);
                mLocalAudioAdapter.notifyDataSetChanged();
                stopScanAnim();
                mInnerScanCount.setText(null);
                if (isScanning == null || !isScanning) {
                    showSubTitleResult();
                } else {
                    showSubTitleScan();
                }
            }
            if (lv_queue.getAdapter() == null) {
                lv_queue.getEmptyView().setVisibility(View.VISIBLE);
                lv_queue.getProgressView().setVisibility(View.VISIBLE);
                lv_queue.setAdapter(mLocalAudioAdapter);
            }
        });

        mLocalStore.isScanning().observe(this, isScanning -> {
            if (isScanning == null || !isScanning) {
                showSubTitleResult();
            } else {
                if (View.VISIBLE == lv_queue.getRecyclerView().getVisibility()) {
                    showSubTitleScan();
                } else {
                    rl_action_bar.setVisibility(View.GONE);
                    lv_queue.showProgress();
                    startScanAnim();
                }
            }
        });

        mLocalStore.getScanCount().observe(this, count -> {
            if (View.VISIBLE == lv_queue.getProgressView().getVisibility()) {
                mInnerScanCount.setText(String.format("已扫描到%s个音频", count == null ? 0 : count));
            }
        });

        mLocalStore.getSortType().observe(this, sortType -> {
        });

        // init play info store
        mPlayInfoStore = ViewModelProviders.of(getActivity()).get(PlayInfoStore.class);

        mPlayInfoStore.getCurrPlaying().observe(this, audioV5 -> {
            if (audioV5 == null) {
                return;
            }
            if (mLocalAudioAdapter.getData().contains(audioV5) || mPlayInfoStore.getCurrPlaying().getValue() != null) {
                mLocalAudioAdapter.notifyDataSetChanged();
            }
        });

        LocalActionCreator.get().getLocalAudio(Operation.AUTO);

        // 首次启动自动扫描，
        if (isFirstLaunch) {
            LocalActionCreator.get().scan(Operation.AUTO);
            isFirstLaunch = false;
        }
    }

    private void startScanAnim() {
        scanMask.setVisibility(View.VISIBLE);
        mRotateAnimator.start();
    }

    private void stopScanAnim() {
        scanMask.setVisibility(View.GONE);
        mRotateAnimator.cancel();
    }

    @OnClick({R.id.tiv_scan, R.id.tiv_delete, R.id.tiv_sort_type, R.id.btn_delete_cancel, R.id.btn_delete_sure})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                break;
            case R.id.tiv_scan: // 控制栏的开始扫描
                TextView tv = (TextView) view;
                if ("开始扫描".equals(tv.getText().toString())) {
                    rl_action_bar.setVisibility(View.GONE);
                    lv_queue.showProgress();
                    startScanAnim();
                    hideSubTitle();
                    LocalActionCreator.get().scan(Operation.MANUAL);
                } else {
                    LocalActionCreator.get().cancelScan(Operation.MANUAL);
                }
                break;
            case R.id.tiv_delete: // 删除管理
                switch2deleteMgr();
                break;
            case R.id.btn_delete_cancel:
                switch2Normal();
                break;
            case R.id.btn_delete_sure:
                if (mCheckedItem == null || mCheckedItem.isEmpty()) {
                    ToastUtils.showShortOnUI("你还没有选择内容");
                    return;
                }
                if (mDeleteAudioDialog == null) {
                    mDeleteAudioDialog = new DeleteAudioDialog(getContext(), new DeleteAudioDialog.OnClickCallback() {
                        @Override
                        public void onConfirm() {
                            LocalActionCreator.get().deleteLocal(Operation.MANUAL, new ArrayList<>(mCheckedItem));
                            switch2Normal();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
                mDeleteAudioDialog.show();
                break;
            case R.id.tiv_sort_type: // 排序方式
                if (mLocalSortTypeDialog == null) {
                    mLocalSortTypeDialog = new LocalSortTypeDialog(getContext());
                }
                mLocalSortTypeDialog.setSortType(mLocalStore.getSortType().getValue());
                mLocalSortTypeDialog.show();
                break;
            default:
                break;
        }
    }

    private LocalSortTypeDialog mLocalSortTypeDialog;
    private DeleteAudioDialog mDeleteAudioDialog;

    // 切换到删除管理模式
    private void switch2deleteMgr() {
        ll_delete_bar.setVisibility(View.VISIBLE);
        rl_action_bar.setVisibility(View.GONE);
        isDeleteMgr = true;
        mLocalAudioAdapter.notifyDataSetChanged();
    }

    // 切换到普通模式
    private void switch2Normal() {
        mCheckedItem.clear();
        ll_delete_bar.setVisibility(View.GONE);
        rl_action_bar.setVisibility(View.VISIBLE);
        isDeleteMgr = false;
        mLocalAudioAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mLocalStore.getAudioList().removeObservers(this);
        mLocalStore.isScanning().removeObservers(this);
        mLocalStore.getScanCount().removeObservers(this);
        mLocalStore.getSortType().removeObservers(this);
        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
            mRotateAnimator = null;
        }
        if (mRotateAnimatorMini != null) {
            mRotateAnimatorMini.cancel();
            mRotateAnimatorMini = null;
        }
        if (scanMask != null) {
            scanMask.clearAnimation();
            scanMask = null;
        }
        if (ivScanMini != null) {
            ivScanMini.clearAnimation();
            ivScanMini = null;
        }
        super.onDestroyView();
    }
}
