package com.txznet.music.albumModule.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.PlayListItem;
import com.txznet.music.playerModule.ui.PlayListOnItemClickListener;
import com.txznet.music.playerModule.ui.adapter.PlayListAdapterV41;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.Locale;

/**
 * Created by 58295 on 2018/4/17.
 */

public class RadioListAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    private Context mContext;
    List<T> mPlayListItems;
    private PlayListOnItemClickListener mItemClickListener;

    public RadioListAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<T> playListItems) {
        mPlayListItems = playListItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (ScreenUtils.isPhonePortrait()) {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_radio_list_phone_portrait, parent, false);
        }else{
            v = LayoutInflater.from(mContext).inflate(R.layout.item_radio_list, parent, false);
        }
        return new RadioListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlayListItem playListItem = (PlayListItem) mPlayListItems.get(position);
        Audio audio = playListItem.getAudio();
        final RadioListViewHolder listHolder = (RadioListViewHolder) holder;
//        listHolder.setName(audio, playListItem.getStyle() == PlayListItem.STYLE_HIGHLINGHT);
        listHolder.setName(audio, playListItem.getStyle());
//        listHolder.tvAlbum.setText("");
//        if (playListItem.getStyle() == PlayListItem.STYLE_HIGHLINGHT) {
//            listHolder.setPlayStatus(PlayListAdapterV41.PlayListViewHolderV41.STATUS_PLAYING);
//        } else {
//            if (playListItem.getStyle() == PlayListItem.STYLE_GREY) {
//                listHolder.setPlayStatus(PlayListAdapterV41.PlayListViewHolderV41.STATUS_GREY);
//            } else {
//                listHolder.setPlayStatus(PlayListAdapterV41.PlayListViewHolderV41.STATUS_NORMAL);
//            }
//        }


        listHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemClickListener) {
                    int index = listHolder.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION) {
                        mItemClickListener.onPlay(((PlayListItem) mPlayListItems.get(index)).getAudio());
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
//        LogUtil.logd("RadioListAdapter getItemCount " + mPlayListItems.size());
        if (CollectionUtils.isNotEmpty(mPlayListItems)) {
            LogUtil.logd("RadioListAdapter getItemCount " + mPlayListItems.size());
            return mPlayListItems.size();
        }
        return 0;
    }

    public static class RadioListViewHolder extends ViewHolder {

        public TextView tvName;
        public ImageView ivStatus;
        Resources mRes;

        public static final int STATUS_NORMAL = 0;
        public static final int STATUS_PLAYING = 1;
        public static final int STATUS_GREY = 2;

        public RadioListViewHolder(View itemView) {
            super(itemView);
            mRes = GlobalContext.get().getResources();
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            ivStatus = (ImageView) itemView.findViewById(R.id.iv_status);
        }

        public void setName(Audio audio, int textStyle) {
            String albumName = audio.getAlbumName();
            if (audio.getAlbum() != null) {
                albumName = audio.getAlbum().getName();
            }
            if (ScreenUtils.isPhonePortrait() && ivStatus != null) {
                if(textStyle == STATUS_NORMAL){
                    ivStatus.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_normal));
                }else if(textStyle == STATUS_PLAYING){
                    ivStatus.setImageDrawable(mRes.getDrawable(R.drawable.ic_playlist_status_playing));
                }
            }
            tvName.setText(Utils.getTitleForAudioNameWithArtists(audio.getName(), albumName, textStyle));
        }

//        public void setPlayStatus(int status) {
//            switch (status) {
//                case STATUS_NORMAL:
//                    tvName.setTextColor(mRes.getColor(R.color.play_list_item_name_normal));
////                    tvArtist.setTextColor(mRes.getColor(R.color.play_list_item_artist_normal));
//                    break;
//                case STATUS_PLAYING:
//                    tvName.setTextColor(mRes.getColor(R.color.color_selected));
////                    tvArtist.setTextColor(mRes.getColor(R.color.color_selected));
//                    break;
//                case STATUS_GREY:
//                    tvName.setTextColor(mRes.getColor(R.color.play_list_item_name_grey));
////                    tvArtist.setTextColor(mRes.getColor(R.color.play_list_item_artist_grey));
//                    break;
//            }
//        }
    }
}
