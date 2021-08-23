package com.txznet.music.ui.webchatpush;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.WxPushActionCreator;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.QrCodeInfo;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.WxPushStore;
import com.txznet.music.ui.base.BaseCheckPlayerFragment;
import com.txznet.music.ui.base.IHeaderBar;
import com.txznet.music.ui.base.IHeaderView;
import com.txznet.music.ui.base.adapter.UnifyCheckAdapter;
import com.txznet.music.ui.base.adapter.UnifyCheckHolder;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.TXZAppUtils;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.widget.RefreshLoadingView;
import com.txznet.rxflux.Operation;
import com.txznet.txz.util.QRUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author telen
 * @date 2018/12/18,10:39
 */
public class WxPushFragment extends BaseCheckPlayerFragment<UnifyCheckAdapter> {
    private static final String TAG = Constant.LOG_TAG_WX_PUST + ":Frag";

    @Bind(R.id.recyclerView)
    EasyRecyclerView mRecyclerView;

    @Bind(R.id.fl_header_bar)
    ViewGroup mHeaderBarView;

    IHeaderBar mHeaderBar;

    private WxPushStore mWxPushStore;

    private ValueAnimator mRotateAnimator;

    @Override
    protected int getLayout() {
        return R.layout.wx_push_fragment;
    }

    @Override
    protected void initView(View view) {
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        layout.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));

        mRecyclerView.setProgressView(new RefreshLoadingView(getContext()));

        mRecyclerView.setAdapterWithProgress(getAdapter());
        mRecyclerView.setEmptyView(R.layout.wx_push_empty_view);
        mRecyclerView.getRecyclerView().setItemAnimator(null);
        mRecyclerView.getRecyclerView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            //有网，获取是否绑定过的状态，如果没有则展示二维码界面，如果有则展示empty界面
            WxPushActionCreator.getInstance().getWxPushQRCode();
        }

        tvTitle.setText("微信推送");

        mHeaderBar = new IHeaderBar() {
            @Override
            public void removeHeader() {
                mHeaderBarView.removeAllViews();
                mHeaderBarView.setVisibility(View.GONE);
            }

            @Override
            public void addHeader(IHeaderView headerView) {
                mHeaderBarView.removeAllViews();
                mHeaderBarView.addView(headerView.getView());
                mHeaderBarView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean hasHeader() {
                return mHeaderBarView.getChildCount() > 0;
            }
        };
    }

    private boolean isQrCodeReady;

    private Runnable mCancelLoadingTask = () -> {
        if (!isQrCodeReady) {
            cancelLoading(true);
        }
    };

    private void beginLoading() {
        ImageView ivLoading = mRecyclerView.getEmptyView().findViewById(R.id.iv_loading);
        if (ivLoading != null) {
            ivLoading.setVisibility(View.VISIBLE);
        }
        if (mRotateAnimator == null) {
            mRotateAnimator = ObjectAnimator.ofFloat(ivLoading, "rotation", 0, 360);
            mRotateAnimator.setDuration(2000);
            mRotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mRotateAnimator.setInterpolator(new LinearInterpolator());
        }
        mRotateAnimator.start();
    }

    private void cancelLoading(boolean shouldTip) {
        ImageView ivLoading = mRecyclerView.getEmptyView().findViewById(R.id.iv_loading);
        if (ivLoading != null) {
            ivLoading.setVisibility(View.GONE);
        }
        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
        }
        TextView tvRetry = mRecyclerView.getEmptyView().findViewById(R.id.tv_retry);
        if (tvRetry != null) {
            tvRetry.setVisibility(View.VISIBLE);
        }
        if (shouldTip) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mDeleteAudioDialog.setTitle("删除消息");

        mWxPushStore = ViewModelProviders.of(this).get(WxPushStore.class);

        mWxPushStore.getPushItem().observe(this, pushItems -> {
            // FIXME: 2019/4/22 兼容2.9.14以下版本(2.9.14以下版本不能获取绑定状态)
            boolean hasBind = false;
            if (mWxPushStore.getQrCodeInfo().getValue() != null) {
                hasBind = mWxPushStore.getQrCodeInfo().getValue().isbind;
            }
            if (TXZAppUtils.getCoreVerCode() >= 20914) {
                if (hasBind) {
                    if (pushItems.size() == 0) {
                        mRecyclerView.setEmptyView(R.layout.wx_push_empty_view);
                        mRecyclerView.showEmpty();
                        tvSubTitle.setText(null);
                        getHeaderBar().removeHeader();
                    } else {
                        updateData(pushItems, "已推送消息");
                        tvSubTitle.setVisibility(View.VISIBLE);
                        tvSubTitle.setText(String.format(Locale.CHINA, "已同步%d条推送", pushItems.size()));
                    }
                } else {
                    tvSubTitle.setVisibility(View.INVISIBLE);
                    WxPushActionCreator.getInstance().getWxPushQRCode();
                }
            } else {
                if (pushItems.size() > 0 || hasBind) {
                    updateData(pushItems, "已推送消息");
                    tvSubTitle.setVisibility(View.VISIBLE);
                    tvSubTitle.setText(String.format(Locale.CHINA, "已同步%d条推送", pushItems.size()));
                } else {
                    if (getAdapter().getCount() > 0) {
                        mRecyclerView.setEmptyView(R.layout.wx_push_empty_view);
                        mRecyclerView.showEmpty();
                        tvSubTitle.setText(null);
                        getHeaderBar().removeHeader();

                        QrCodeInfo qrCodeInfo = new QrCodeInfo();
                        qrCodeInfo.isbind = true;
                        qrCodeInfo.issuccess = true;
                        SharedPreferencesUtils.setQrCodeInfoCache(JsonHelper.toJson(qrCodeInfo));
                    }
                }
            }
        });

        mWxPushStore.getQrCodeInfo().observe(this, qrCodeInfo -> {
            Logger.d(TAG, "initData:" + (qrCodeInfo == null ? "" : qrCodeInfo.toString()));
            if (qrCodeInfo == null) { // 没有绑定过/没有初始化，此时数据库记录为空(升级例外
                if (TXZAppUtils.getCoreVerCode() >= 20914) {
                    mRecyclerView.setEmptyView(R.layout.wx_push_empty_qrcode_view);
                    ViewGroup vg = mRecyclerView.getEmptyView().findViewById(R.id.vg_qrcode);
                    vg.setBackgroundResource(R.drawable.wx_push_qrcode_empty_bg);
                    TextView tvRetry = mRecyclerView.getEmptyView().findViewById(R.id.tv_retry);
                    tvRetry.setVisibility(View.VISIBLE);
                    tvRetry.setOnClickListener(v -> {
                        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                            v.setVisibility(View.GONE);
                            beginLoading();
                            AppLogic.runOnUiGround(mCancelLoadingTask, 8000);
                            //有网，获取是否绑定过的状态，如果没有则展示二维码界面，如果有则展示empty界面
                            WxPushActionCreator.getInstance().getWxPushQRCode();
                        } else {
                            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                        }
                    });
                } else {
                    mRecyclerView.setEmptyView(R.layout.wx_push_empty_qrcode_view_compat);
                }
                mRecyclerView.showEmpty();
                tvSubTitle.setText(null);
                getHeaderBar().removeHeader();
            } else if (qrCodeInfo.isbind) { // 绑定
                WxPushActionCreator.getInstance().getWxPushData();
            } else if (qrCodeInfo.issuccess ||
                    (mWxPushStore.getPushItem().getValue() == null || mWxPushStore.getPushItem().getValue().isEmpty())) { // 没有绑定
                if (TXZAppUtils.getCoreVerCode() >= 20914) {
                    mRecyclerView.setEmptyView(R.layout.wx_push_empty_qrcode_view);
                    ViewGroup vg = mRecyclerView.getEmptyView().findViewById(R.id.vg_qrcode);
                    ImageView ivQrcode = mRecyclerView.getEmptyView().findViewById(R.id.iv_qrcode);
                    TextView tvRetry = mRecyclerView.getEmptyView().findViewById(R.id.tv_retry);
                    if (qrCodeInfo.qrcode == null) {
                        vg.setBackgroundResource(R.drawable.wx_push_qrcode_empty_bg);
                        tvRetry.setVisibility(View.VISIBLE);
                        tvRetry.setOnClickListener(v -> {
                            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                                v.setVisibility(View.GONE);
                                beginLoading();
                                AppLogic.runOnUiGround(mCancelLoadingTask, 8000);
                                //有网，获取是否绑定过的状态，如果没有则展示二维码界面，如果有则展示empty界面
                                WxPushActionCreator.getInstance().getWxPushQRCode();
                            } else {
                                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                            }
                        });
                    } else {
                        try {
                            ivQrcode.setImageBitmap(QRUtil.createQRCodeBitmapNoWhite(qrCodeInfo.qrcode, getResources().getDimensionPixelOffset(R.dimen.m152)));
                            ivQrcode.setOnClickListener(null);
                            vg.setBackgroundResource(R.drawable.wx_push_qrcode_bg);
                            isQrCodeReady = true;
                            AppLogic.removeUiGroundCallback(mCancelLoadingTask);
                            cancelLoading(false);
                            tvRetry.setVisibility(View.GONE);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    mRecyclerView.setEmptyView(R.layout.wx_push_empty_qrcode_view_compat);
                }
                mRecyclerView.showEmpty();
                tvSubTitle.setText(null);
                getHeaderBar().removeHeader();
            }
        });

        //开始请求
        WxPushActionCreator.getInstance().getWxPushData();

    }

    @Override
    protected UnifyCheckAdapter setAdapter() {
        return new UnifyCheckAdapter<PushItem>(getContext()) {

            @Override
            public UnifyCheckHolder getBaseViewHolder(ViewGroup parent) {
                return new UnifyCheckHolder<PushItem>(parent) {
                    @Override
                    public void setData(PushItem data) {
                        super.setData(data);

                        cbFavour.setVisibility(View.GONE);
                        /* hh:mm:ss*/
                        String timeStr = new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(new Date(data.timestamp));
                        this.tvArtist.setText(timeStr);
                        this.tvName.setText(data.name);
                        this.tvIndex.setText(getLayoutPosition() - getAdapter().getHeaderCount() + 1 + "");
                        setItemClickListener(v -> {
                            PlayerActionCreator.get().playWxPush(Operation.MANUAL, getAllData(), getLayoutPosition() - getAdapter().getHeaderCount());
                        });
                    }
                };
            }

            @Override
            protected void changePlayObj(@NonNull UnifyCheckHolder holder, int position) {
                super.changePlayObj(holder, position);
                PushItem item = (PushItem) getAdapter().getItem(position);
                item.status = PushItem.STATUS_READ;
            }

            @Override
            protected void changeUnPlayingStatus(@NonNull UnifyCheckHolder holder, int position) {
                super.changeUnPlayingStatus(holder, position);
                PushItem item = (PushItem) getAdapter().getItem(position);
                initColor(holder, item);
            }

            /**
             * 初始化颜色值
             */
            private void initColor(@NonNull UnifyCheckHolder holder, PushItem item) {
                if (item.status == PushItem.STATUS_READ) {
                    //已读的状态，置灰
                    holder.tvIndex.setTextColor(getContext().getResources().getColor(R.color.white_40));
                    holder.tvName.setTextColor(getContext().getResources().getColor(R.color.white_40));
                } else {
                    //未读的状态
                    holder.tvIndex.setTextColor(getContext().getResources().getColor(R.color.white));
                    holder.tvName.setTextColor(getContext().getResources().getColor(R.color.white));
                }
                holder.tvArtist.setTextColor(getContext().getResources().getColor(R.color.white_40));
            }
        };
    }

    @Override
    public void onClickDeleteEvent() {
        WxPushActionCreator.getInstance().deleteWxPushDatas(getAdapter().getCheckedData());
    }

    public void onTest() {
        int i = (int) (Math.random() * 10);
        ToastUtils.showShortOnUI(String.format(Locale.getDefault(), "测试阶段:添加%d数据", i));
        List<TXZAudio> audios = new ArrayList<>();
        for (int i1 = 0; i1 < i; i1++) {
            TXZAudio audio = new TXZAudio();
            audio.name = "测试" + i1;
            audio.arrArtistName = Collections.singletonList("测试艺术家");
            audios.add(audio);

        }
        for (int size = audios.size(); size > 0; size--) {
            List<PushItem> pushItems = AudioConverts.convert2List(audios, audio -> {
//                if (!isOk && mTXZAudio != null && audio.equals(mTXZAudio)) {
//                    isOk = true;
//                    return AudioConverts.convert2PushItem(audio, read);
//                }
                return AudioConverts.convert2PushItem(audio, PushItem.STATUS_UNREAD);
            });
            WxPushActionCreator.getInstance().saveWxPushData(pushItems);
        }
    }

    @OnClick(R.id.btn_test)
    public void onViewClicked() {
        onTest();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportEvent.reportUserWxEnter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ReportEvent.reportUserWxExit();
    }

    @Override
    protected IHeaderBar getHeaderBar() {
        return mHeaderBar;
    }

    @Override
    public void onDestroyView() {
        AppLogic.removeUiGroundCallback(mCancelLoadingTask);
        if (mRotateAnimator != null) {
            mRotateAnimator.cancel();
        }
        super.onDestroyView();
    }
}
