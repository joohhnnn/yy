package com.txznet.music.playerModule.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.PlayListItem;
import com.txznet.music.playerModule.ui.PlayListOnItemClickListener;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Locale;

/**
 * Created by brainBear on 2017/9/25.
 */

public class PlayListAdapterV41 extends RecyclerView.Adapter<PlayListAdapterV41.PlayListViewHolderV41> {

    private final String TAG = "PlayListAdapterV41:";
    private List<PlayListItem> mPlayListItems;
    private Context mCtx;
    private PlayListOnItemClickListener mItemClickListener;


    public PlayListAdapterV41(Context context) {
        this.mCtx = context;
    }


    @Override
    public PlayListViewHolderV41 onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (ScreenUtils.isPhonePortrait()) {
            view = LayoutInflater.from(mCtx).inflate(R.layout.item_player_list_v41_phone_portrait, parent, false);
        } else {
            view = LayoutInflater.from(mCtx).inflate(R.layout.item_player_list_v41, parent, false);
        }

        return new PlayListViewHolderV41(view);
    }

    @Override
    public void onBindViewHolder(final PlayListViewHolderV41 holder, final int position) {
        PlayListItem playListItem = mPlayListItems.get(position);
        Audio audio = playListItem.getAudio();
        if (ScreenUtils.isPhonePortrait()) {
            holder.isPhoneSettingItemTitle(audio, playListItem.getStyle() == PlayListItem.STYLE_HIGHLINGHT);
        } else {
            holder.setName(audio, playListItem.getStyle() == PlayListItem.STYLE_HIGHLINGHT);
        }

        holder.setFavorStatus(playListItem.isShowFavor(), playListItem.isFavor(), playListItem.isFavorEnable());
        holder.setProgress(playListItem.isShowProgress() && playListItem.getStyle() != PlayListItem.STYLE_HIGHLINGHT, playListItem.getProgress());

        if (playListItem.getStyle() == PlayListItem.STYLE_HIGHLINGHT) {
            holder.setPlayStatus(PlayListViewHolderV41.STATUS_PLAYING);
        } else {
            if (playListItem.getStyle() == PlayListItem.STYLE_GREY) {
                holder.setPlayStatus(PlayListViewHolderV41.STATUS_GREY);
            } else {
                holder.setPlayStatus(PlayListViewHolderV41.STATUS_NORMAL);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemClickListener) {
                    int index = holder.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        mItemClickListener.onPlay(mPlayListItems.get(index).getAudio());
                    }
                }
            }
        });

        holder.cbFavor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemClickListener) {
                    int index = holder.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        mItemClickListener.onFavor(mPlayListItems.get(index).getAudio(), holder.cbFavor.isChecked());
                    }
                }
            }
        });

    }


    public void setOnItemClickListener(PlayListOnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mPlayListItems == null ? 0 : mPlayListItems.size();
    }


    public void setData(List<PlayListItem> playListItems) {
        this.mPlayListItems = playListItems;
    }

    public static class PlayListViewHolderV41 extends RecyclerView.ViewHolder {


        public static final int STATUS_NORMAL = 0;
        public static final int STATUS_PLAYING = 1;
        public static final int STATUS_GREY = 2;

        CheckBox cbFavor;
        ImageView ivStatus;
        TextView tvName;
        TextView tvArtist;
        TextView tvProgress;
        Resources mRes;

        public PlayListViewHolderV41(View itemView) {
            super(itemView);
            mRes = GlobalContext.get().getResources();

            ivStatus = (ImageView) itemView.findViewById(R.id.iv_status);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
            tvProgress = (TextView) itemView.findViewById(R.id.tv_progress);
            cbFavor = (CheckBox) itemView.findViewById(R.id.cb_favor);

            Activity activity = ActivityStack.getInstance().currentActivity();
            if(ScreenUtils.isPhonePortrait()){
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(activity, R.attr.text_size_h3, -1));
            }else{
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(activity, R.attr.text_size_h2, -1));
            }
            tvArtist.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(activity, R.attr.text_size_h3, -1));
        }


        public void setName(Audio audio, boolean isHighlight) {
            int songColor;
            int artistColor;
            if (isHighlight) {
                songColor = mRes.getColor(R.color.color_selected);
                artistColor = mRes.getColor(R.color.color_selected);
            } else {
                songColor = Color.WHITE;
                artistColor = Color.parseColor("#939393");
            }

            if (Utils.isSong(audio.getSid()) && null != audio.getArrArtistName() && !audio.getArrArtistName().isEmpty()) {
                String songName = audio.getName();
                String artistName = StringUtils.toString(audio.getArrArtistName());
                String name = String.format(Locale.getDefault(), "%s - %s", songName, artistName);
                SpannableString spannableString = new SpannableString(name);
                spannableString.setSpan(new ForegroundColorSpan(songColor), 0, songName.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannableString.setSpan(new ForegroundColorSpan(artistColor)
                        , songName.length() + 1, name.length()
                        , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                tvName.setText(spannableString);
            } else {
                tvName.setText(audio.getName());
            }
//            tvName.setText(audio.getName());
        }

        public void isPhoneSettingItemTitle(Audio audio, boolean isSelect) {
            if (isSelect) {
                tvName.setTextColor(mRes.getColor(R.color.color_selected));
                tvArtist.setTextColor(mRes.getColor(R.color.color_selected));
            } else {
                tvName.setTextColor(Color.WHITE);
                tvArtist.setTextColor(Color.parseColor("#939393"));
            }
            tvName.setText(audio.getName());
            if (Utils.isSong(audio.getSid()) && null != audio.getArrArtistName() && !audio.getArrArtistName().isEmpty() && !audio.getArrArtistName().get(0).equals("")) {
                if (tvArtist != null) {
                    tvArtist.setVisibility(View.VISIBLE);
                    if (isSelect) {
                        tvArtist.setTextColor(mRes.getColor(R.color.color_selected));
                    }
                    tvArtist.setText(StringUtils.toString(audio.getArrArtistName()));
                }
            } else {
                tvArtist.setVisibility(View.GONE);
            }
        }

        public void setPlayStatus(@STATUS int status) {
            switch (status) {
                case STATUS_NORMAL:
                    ivStatus.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_normal));
                    tvName.setTextColor(mRes.getColor(R.color.play_list_item_name_normal));
//                    tvArtist.setTextColor(mRes.getColor(R.color.play_list_item_artist_normal));
                    break;
                case STATUS_PLAYING:
                    ivStatus.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_playing));
                    tvName.setTextColor(mRes.getColor(R.color.color_selected));
//                    tvArtist.setTextColor(mRes.getColor(R.color.color_selected));
                    break;
                case STATUS_GREY:
                    ivStatus.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_normal));
                    tvName.setTextColor(mRes.getColor(R.color.play_list_item_name_grey));
//                    tvArtist.setTextColor(mRes.getColor(R.color.play_list_item_artist_grey));
                    break;
            }
        }

        public void setArtist(boolean isShow, String artist) {
            if (!isShow || TextUtils.isEmpty(artist)) {
                tvArtist.setVisibility(View.INVISIBLE);
            } else {
                tvArtist.setVisibility(View.VISIBLE);
                tvArtist.setText(String.format(" - %s", artist));
            }
        }

        public void setFavorStatus(boolean visible, boolean check, boolean enable) {
            if (visible) {
                cbFavor.setVisibility(View.VISIBLE);
                cbFavor.setChecked(check);
                cbFavor.setEnabled(enable);
            } else {
                cbFavor.setVisibility(View.GONE);
            }
        }


        public void setProgress(boolean visible, float percent) {
            if (visible) {
                tvProgress.setVisibility(View.VISIBLE);
                tvProgress.setText(String.format(Locale.getDefault(), "已播放%.1f%%", percent * 100));
            } else {
                tvProgress.setVisibility(View.GONE);
            }
        }

        @IntDef({STATUS_NORMAL, STATUS_PLAYING, STATUS_GREY})
        @Retention(RetentionPolicy.SOURCE)
        private @interface STATUS {
        }
    }

}
