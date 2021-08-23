package com.txznet.music.FavourModule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.localModule.ui.adapter.AudioViewCheckHolder;
import com.txznet.music.localModule.ui.adapter.AudioViewHolder;
import com.txznet.music.util.TestUtil;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by telenewbie on 2017/12/15.
 */

public abstract class ItemAudioCheckAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public Context ctx;
    List<T> t;
    List<T> choiceT;

    public ItemAudioCheckAdapter(Context ctx, List<T> t) {
        this.ctx = ctx;
        this.t = t;
        choiceT = new ArrayList<>();
    }

    public boolean isCheck(Audio audio) {
        return choiceT.contains(audio);
    }

    public abstract void bindView(AudioViewCheckHolder v, T t, int index);


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(ctx, AudioViewCheckHolder.getLayoutResourceId(), null);
        return new AudioViewCheckHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final AudioViewCheckHolder v = (AudioViewCheckHolder) holder;
//        v.mIvDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mOnDeleteListener) {
//                    mOnDeleteListener.onDelete(position);
//                }
//            }
//        });

        v.cbLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                v.cbCheck.setChecked(!v.cbCheck.isChecked());
                if (v.cbCheck.isChecked()) {
                    choiceT.add(t.get(position));
                } else {
                    choiceT.remove(t.get(position));
                }
                if (null != mOnchecklistener) {
                    mOnchecklistener.onCheck(position, v.cbCheck.isChecked());
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
        v.mLl_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) {
                    mOnItemClickListener.onClick(position);
                }
            }
        });
        bindView(v, t.get(position), position);
    }

    public List<T> getCheckedList() {
        return choiceT;
    }

    public void checkItemsAll(List<T> audios, boolean isCheck) {
        if (choiceT == null || audios == null) {
            return;
        }

        if (isCheck) {
            choiceT.addAll(audios);
        } else {
            choiceT.removeAll(audios);
        }
    }


    public void replaceData(List<T> data) {
        this.t = data;
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(t)) {
            return t.size();
        }
        return 0;
    }

    public interface OnCheckListener {
        void onCheck(int position, boolean b);
    }

    private OnCheckListener mOnchecklistener;

    public void setOnCheckListener(OnCheckListener onchecklistener) {
        mOnchecklistener = onchecklistener;
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

    public void isPhoneSettingItemTitle(AudioViewCheckHolder v, Audio audio, boolean isSelect) {
        if (isSelect) {
            v.mTitle.setTextColor(ctx.getResources().getColor(R.color.color_selected));
            v.mTitleAuthor.setTextColor(ctx.getResources().getColor(R.color.color_selected));
        }else{
            v.mTitle.setTextColor(Color.WHITE);
            v.mTitleAuthor.setTextColor(Color.parseColor("#939393"));
        }
        v.mTitle.setText(audio.getName());
        if (Utils.isSong(audio.getSid()) && null != audio.getArrArtistName() && !audio.getArrArtistName().isEmpty()&&!audio.getArrArtistName().get(0).equals("")) {
            if (v.mTitleAuthor != null) {
                v.mTitleAuthor.setVisibility(View.VISIBLE);
                if (isSelect) {
                    v.mTitleAuthor.setTextColor(ctx.getResources().getColor(R.color.color_selected));
                }
                v.mTitleAuthor.setText(StringUtils.toString(audio.getArrArtistName()));
            }
        }else{
            v.mTitleAuthor.setVisibility(View.GONE);
        }
    }
}
