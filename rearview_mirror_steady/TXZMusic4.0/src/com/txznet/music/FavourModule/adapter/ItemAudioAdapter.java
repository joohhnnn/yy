package com.txznet.music.FavourModule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.localModule.ui.adapter.AudioViewHolder;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.OnEffectiveClickListener;

import java.util.List;
import java.util.Locale;

/**
 * Created by telenewbie on 2017/12/15.
 */

public abstract class ItemAudioAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context ctx;
    List<T> t;

    public ItemAudioAdapter(Context ctx, List<T> t) {
        this.ctx = ctx;
        this.t = t;
    }


    public abstract void bindView(AudioViewHolder v, T t, int index);


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(ctx, AudioViewHolder.getLayoutResourceId(), null);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        AudioViewHolder v = (AudioViewHolder) holder;
        v.mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnDeleteListener) {
                    mOnDeleteListener.onDelete(t.get(position));
                }
            }
        });
        v.mIvFavour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mFavourListener) {
                    mFavourListener.onFavour(position);
                }
            }
        });
        v.mLl_item.setOnClickListener(new OnEffectiveClickListener() {
            @Override
            public void onEffectiveClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position);
                }
            }

            @Override
            public long getTimeInterval() {
                return 1000;
            }
        });
        bindView(v, t.get(position), position);
    }


//    public void replaceData(List<T> data) {
//        this.t = data;
//    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(t)) {
            return t.size();
        }
        return 0;
    }

    public interface OnDeleteListener<T> {
        void onDelete(T t);
    }

    private OnDeleteListener<T> mOnDeleteListener;

    public void setOnDeleteListener(OnDeleteListener<T> ondeleteListener) {
        mOnDeleteListener = ondeleteListener;
    }

    public interface OnFavourListener {
        void onFavour(int position);
    }

    private OnFavourListener mFavourListener;

    public void setOnFavourListener(OnFavourListener favourListener) {
        mFavourListener = favourListener;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public SpannableString getTitleForAudioNameWithArtists(Audio audio, boolean isHighlight) {
        SpannableString spannableString;
        int songColor;
        int artistColor;
        if (isHighlight) {
            songColor = ctx.getResources().getColor(R.color.color_selected);
            artistColor = ctx.getResources().getColor(R.color.color_selected);
        } else {
            songColor = Color.WHITE;
            artistColor = Color.parseColor("#939393");
        }

        String songName = audio.getName();
        if (Utils.isSong(audio.getSid()) && null != audio.getArrArtistName() && !audio.getArrArtistName().isEmpty()) {
            String artistName = StringUtils.toString(audio.getArrArtistName());
            String name = String.format(Locale.getDefault(), "%s - %s", songName, artistName);
            spannableString = new SpannableString(name);
            spannableString.setSpan(new ForegroundColorSpan(songColor), 0, songName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(artistColor)
                    , songName.length() + 1, name.length()
                    , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        } else {
            spannableString = new SpannableString(audio.getName());
            spannableString.setSpan(new ForegroundColorSpan(songColor), 0, songName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return spannableString;
    }


    public void isPhoneSettingItemTitle(AudioViewHolder v, Audio audio, boolean isSelect) {
        if (isSelect) {
            v.mTitle.setTextColor(ctx.getResources().getColor(R.color.color_selected));
            v.mTitleAuthor.setTextColor(ctx.getResources().getColor(R.color.color_selected));
        } else {
            v.mTitle.setTextColor(Color.WHITE);
            v.mTitleAuthor.setTextColor(Color.parseColor("#939393"));
        }
        v.mTitle.setText(audio.getName());
        if (Utils.isSong(audio.getSid()) && null != audio.getArrArtistName() && !audio.getArrArtistName().isEmpty() && !audio.getArrArtistName().get(0).equals("")) {
            if (v.mTitleAuthor != null) {
                v.mTitleAuthor.setVisibility(View.VISIBLE);
                if (isSelect) {
                    v.mTitleAuthor.setTextColor(ctx.getResources().getColor(R.color.color_selected));
                }
                v.mTitleAuthor.setText(StringUtils.toString(audio.getArrArtistName()));
            }
        } else {
            v.mTitleAuthor.setVisibility(View.GONE);
        }
    }


}
