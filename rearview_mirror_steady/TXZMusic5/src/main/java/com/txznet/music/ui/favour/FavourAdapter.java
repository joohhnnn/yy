package com.txznet.music.ui.favour;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.ui.base.BasePlayerAdapter;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.widget.AlphaCheckBox;
import com.txznet.rxflux.Operation;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FavourAdapter extends BasePlayerAdapter<FavourAudio, FavourAdapter.MyHolder> {

    public FavourAdapter(Context context) {
        super(context);
    }

    @Override
    protected void changePlayObj(@NonNull MyHolder holder, int position) {
        holder.tvIndex.setVisibility(View.INVISIBLE);
        holder.ivPlaying.setVisibility(View.VISIBLE);
        holder.tvName.setTextColor(getContext().getResources().getColor(R.color.red));
        holder.tvArtist.setTextColor(getContext().getResources().getColor(R.color.red_40));
    }

    @Override
    protected void changeUnPlayingStatus(@NonNull MyHolder holder, int position) {
        holder.tvIndex.setVisibility(View.VISIBLE);
        holder.ivPlaying.setVisibility(View.GONE);
        holder.tvName.setTextColor(getContext().getResources().getColor(R.color.white));
        holder.tvArtist.setTextColor(getContext().getResources().getColor(R.color.white_40));
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyHolder(parent);
    }

    public class MyHolder extends BaseViewHolder<FavourAudio> /*implements IPlayerStateViewHolder */ {
        @Bind(R.id.tv_index)
        TextView tvIndex;
        @Bind(R.id.iv_playing)
        ImageView ivPlaying;
        @Bind(R.id.tv_name)
        TextView tvName;
        @Bind(R.id.tv_artist)
        TextView tvArtist;
        @Bind(R.id.cb_favour)
        AlphaCheckBox cbFavour;

        public MyHolder(ViewGroup parent) {
            super(parent, R.layout.base_recycle_item_audio);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void setData(FavourAudio favourAudio) {
            super.setData(favourAudio);
            tvIndex.setText(getAdapterPosition() + 1 + "");
            tvName.setText(favourAudio.name);
            if (favourAudio.artist != null) {
                tvArtist.setText(StringUtils.toString(favourAudio.artist));
            }
            if (TextUtils.isEmpty(tvArtist.getText())) {
                tvArtist.setText(Constant.UNKNOWN);
            }
            cbFavour.setChecked(true);
            cbFavour.setOnClickListener(view -> {
                FavourActionCreator.getInstance().unFavour(Operation.MANUAL, favourAudio, "favour");
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_FAVOUR_CANCEL);
            });
            itemView.setOnClickListener(view -> {
                //点击播放
                PlayerActionCreator.get().playFavour(Operation.MANUAL, mObjects, getAdapterPosition());
            });
        }
    }
}
