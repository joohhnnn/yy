package com.txznet.music.localModule.ui.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;

import java.util.List;

public class LocalAudioAdapter extends RecyclerView.Adapter<ViewHolder> {

    public static final int ITEM_TYPE_NORMAL = 0;
    public static final int ITEM_TYPE_RESCAN = 1;
    public static final int ITEM_TYPE_BLANK = 2; //空白填充，最后一行item为奇数时
    private boolean isGridLayout = false;
    private boolean isShowRescan = false;
    private List<Audio> mAudios;
    private Context mCtx;
    private OnItemClickListener mItemClickListener;
    private OnClickListener mRescanListener;
    private Float mTvAvailableLength = GlobalContext.get().getResources().getDimension(R.dimen.x300);
    private boolean mLengthGotten = false;

    public LocalAudioAdapter(List<Audio> audios, Context ctx) {
        this.mAudios = audios;
        this.mCtx = ctx;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setShowRescanBtn(boolean showRescan, OnClickListener onClickListener) {
        this.mRescanListener = onClickListener;
        if (isShowRescan != showRescan) {
            this.isShowRescan = showRescan;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        if (!isShowRescan) {
            return mAudios == null ? 0 : mAudios.size();
        }
        if (mAudios == null || mAudios.size() == 0) {
            return 0;
        }
        if (!isGridLayout || mAudios.size() % 2 == 0) {
            return mAudios.size() + 1;
        }
        // 音频个数为奇数且显示为双排显示时，最后一行加一个补齐
        return mAudios.size() + 1 + 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            isGridLayout = true;
            GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = getItemViewType(position);
                    if (viewType == ITEM_TYPE_RESCAN) {
                        return 2;
                    }
                    return 1;
                }
            });
        }
        isGridLayout = false;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int pos) {
        if (viewHolder == null) {
            return;
        }
        if (viewHolder instanceof ScanViewHolder) {
            ScanViewHolder scanViewHolder = (ScanViewHolder) viewHolder;
            scanViewHolder.mBtnRefresh.setBackground(mCtx.getResources().getDrawable(R.drawable.btn_rescan));
            scanViewHolder.mBtnRefresh.setTextColor(mCtx.getResources().getColor(R.color.local_scan_btn_text));
            scanViewHolder.mBtnRefresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mRescanListener != null) {
                        mRescanListener.onClick(v);
                    }
                }
            });
        } else {
            final AudioViewHolder holder = (AudioViewHolder) viewHolder;
            final int position = pos;
            final Audio song = mAudios.get(position);
            String strTitle = song.getName();
            String artist;
            if (Utils.isSong(song.getSid())) {
                artist = StringUtils.toString(song.getArrArtistName());
            } else {
                artist = song.getAlbumName();
            }
            if (!TextUtils.isEmpty(artist)) {
                strTitle = strTitle + "<font color='#adb6cc'>" + " - " + artist + "</font>";
            }
            holder.mTitle.setText(Html.fromHtml(strTitle));
            if (!mLengthGotten) {
                holder.mTitle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        holder.mTitle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mTvAvailableLength = Float.valueOf(holder.mTitle.getMeasuredWidth());
                        mLengthGotten = true;
                    }
                });
            }
            if (PlayEngineFactory.getEngine().getCurrentAudio() != null
                    && PlayEngineFactory.getEngine().getCurrentAudio().getSid() == song.getSid()
                    && PlayEngineFactory.getEngine().getCurrentAudio().getId() == song.getId()) {
                holder.mTitle.setTextColor(mCtx.getResources().getColor(R.color.local_item_text_playing));
                holder.mTitle.setMarqueeRepeatLimit(-1);
                holder.mIvLeftIcon.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_playlist_status_playing));
            } else {
                holder.mTitle.setTextColor(mCtx.getResources().getColor(R.color.local_item_text_normal));
                holder.mTitle.setMarqueeRepeatLimit(0);
                holder.mIvLeftIcon.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_playlist_status_normal));
            }
            //TODO:如果已收藏则显示为红色
            if (FavorHelper.isFavour(song)) {
                holder.mIvFavour.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_favorite_small));
            } else {
                holder.mIvFavour.setImageDrawable(mCtx.getResources().getDrawable(R.drawable.ic_not_favorite_small));
            }

            holder.mLl_item.setBackground(mCtx.getResources().getDrawable(R.drawable.bg_local_item));
            // 删除该条记录
            holder.mIvDelete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (FileUtils.delFile(song.getStrDownloadUrl())) {
                        // 从本地中删除数据
                        DBManager.getInstance().removeLocalAudios(song);
                        // 更新界面
                        mAudios.remove(position);
                        notifyDataSetChanged();
                        ObserverManage.getObserver().send(InfoMessage.DELETE_LOCAL_MUSIC, song);
                        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                        if (null != currentAudio && 0 == currentAudio.getSid()) {
                            PlayInfoManager.getInstance().setPlayListTotalNum(mAudios.size());
                            ObserverManage.getObserver().send(InfoMessage.SET_PLAY_LIST_TOTAL_NUM);
                        }
                    } else {
                        ToastUtils.showShortOnUI("删除失败");
                    }
                }
            });

            holder.mTitle.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(null, null, position, 0);
                }
            });
            holder.mIvFavour.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FavorHelper.isFavour(song)) {
                        FavorHelper.unfavor(song,EnumState.Operation.manual);
                    } else {
                        FavorHelper.favor(song, EnumState.Operation.manual);
                    }
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = null;
        if (viewType == ITEM_TYPE_RESCAN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ll_song_local_scan, parent, false);
            holder = new ScanViewHolder(view);
        } else if (viewType == ITEM_TYPE_BLANK) {

        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(AudioViewHolder.getLayoutResourceId(), parent, false);
            holder = new AudioViewHolder(view);
        }
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        if (!isShowRescan) {
            return ITEM_TYPE_NORMAL;
        }
        if (isShowRescan && position >= getItemCount() - 1) {
            return ITEM_TYPE_RESCAN;
        }
        if (isGridLayout && mAudios.size() % 2 == 1 && position == getItemCount() - 2) {
            return ITEM_TYPE_BLANK;
        }
        return ITEM_TYPE_NORMAL;
    }


    public static class ScanViewHolder extends ViewHolder {
        private Button mBtnRefresh;

        public ScanViewHolder(View itemView) {
            super(itemView);
            mBtnRefresh = (Button) itemView.findViewById(R.id.refresh_scan_btn);
        }

    }
}
