package com.txznet.music.ui.subscribe;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.SubscribeActionCreator;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.base.BasePlayerAdapter;
import com.txznet.music.ui.base.IPlayerStateViewHolder;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.widget.PlayingStateView;
import com.txznet.rxflux.Operation;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SubscribeAdapter extends BasePlayerAdapter<SubscribeAlbum, SubscribeAdapter.MyHolder> {

    private Fragment fragment;

    public SubscribeAdapter(Fragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }


    @Override
    protected void changePlayObj(@NonNull MyHolder holder, int position) {
        holder.animationView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void changeUnPlayingStatus(@NonNull MyHolder holder, int position) {
        holder.animationView.setVisibility(View.GONE);
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(parent);
    }


    public class MyHolder extends BaseViewHolder<SubscribeAlbum> implements IPlayerStateViewHolder {
        @Bind(R.id.iv_subscribe)
        ImageView ivSubscribe;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.iv_bg)
        ImageView ivBg;
        @Bind(R.id.animation_view)
        PlayingStateView animationView;

        public MyHolder(ViewGroup parent) {
            super(parent, R.layout.subscribe_album_item);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(SubscribeAlbum data) {
            super.setData(data);
            tvName.setText(data.name);
            GlideHelper.loadWithCorners(fragment, data.logo, 0, ivBg);
            ivSubscribe.setOnClickListener(view -> {
                SubscribeActionCreator.getInstance().unSubscribe(Operation.MANUAL, data, "subscribe");
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_CANCEL);
            });
            ivBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BuildConfig.DEBUG) {
                        Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",itemView:onClick");
                    }

                    if (animationView.isPlaying()) {
                        PlayerActionCreator.get().pause(Operation.MANUAL);
                    } else {
                        //点击播放
                        ReportEvent.reportUserSubscribeItemClick(data);
                        PlayerActionCreator.get().playSubscribe(Operation.MANUAL, data);
                    }
                }
            });
        }

        @Override
        public PlayingStateView getPlayingStateView() {
            return animationView;
        }
    }
}
