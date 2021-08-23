package com.txznet.music.albumModule.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.AnimationUtil;
import com.txznet.music.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/14.
 */

public abstract class ItemAlbumBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private static final String TAG = "Music:ItemAlbumAdapter:";
    protected List<Album> albums;
    private AdapterView.OnItemClickListener listener;
    private AdapterView.OnItemClickListener iconListener;

    private Album mPlayingAlbum;// 当前播放的专辑，用于变色

    private boolean isShowLoading;

    private static final int TYPE_ALBUM = -1;
    private static final int TYPE_LOADING = -2;

    public static final int SHOWTYPE_UNDEFINE = 0;//未定义
    public static final int SHOWTYPE_RECOMMAND = 1;//推荐
    public static final int SHOWTYPE_RANKING_LIST = 2;//排行榜
    public static final int SHOWTYPE_SINGER = 3;//歌手
    public static final int SHOWTYPE_RANKING_CLASSIFY = 4;//分类
    public static final int SHOWTYPE_RANKING_OTHER = 5;//其他分类

    public ItemAlbumBaseAdapter(Context ctx, List<Album> albums) {
        this.albums = albums;
        if (null != ctx) {
            loadingViewHolder = new LoadingViewHolder(View.inflate(ctx, R.layout.item_album_loading, null));
        }
    }

    public boolean isShowLoading() {
        return isShowLoading;
    }


//    public void updateCurrentAlbum(Album album) {
//        this.mPlayingAlbum = album;
//    }

    public Album getCurrentPlayingAlbum() {
        return PlayInfoManager.getInstance().getCurrentAlbum();
    }

    /**
     * 是否加载中
     *
     * @param show
     */
    public void setShowLoading(boolean show) {
        LogUtil.d(TAG + "show loading " + show);
        if (null != loadingViewHolder) {
            if (show) {
                loadingViewHolder.startLoading();
                notifyItemInserted(getItemCount());
            } else {
                loadingViewHolder.stopLoading();
                notifyItemRemoved(getItemCount());
            }
        }
        isShowLoading = show;
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(albums)) {
            if (isShowLoading) {
                return albums.size() + 1;
            }
            return albums.size();
        }
        return 0;
    }

    public abstract void onChildBindViewHolder(final RecyclerView.ViewHolder holder, Album album);

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Album album;
        if (null == holder) {
            return;
        }
        if (holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).startLoading();
        } else {
            album = albums.get(position);
            onChildBindViewHolder(holder, album);
            if (holder != null) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (albums == null || albums.size() <= position) {
                            LogUtil.loge(TAG + " click a position out of bounds, refresh view");
                            notifyDataSetChanged();
                            return;
                        }
                        mPlayingAlbum = albums.get(position);
                        SharedPreferencesUtils.setAudioSource(Constant.TYPE_SHOW);
                        if (null != listener) {
                            listener.onItemClick(null, v, position, getItemId(position));
                            notifyDataSetChanged();
                        }
                    }
                });
                ((MyClickSetter) holder).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (albums == null || albums.size() <= position) {
                            LogUtil.loge(TAG + " click a position out of bounds, refresh view");
                            notifyDataSetChanged();
                            return;
                        }
                        mPlayingAlbum = albums.get(position);
                        SharedPreferencesUtils.setAudioSource(Constant.TYPE_SHOW);
                        if (null != listener) {
                            listener.onItemClick(null, v, position, getItemId(position));
                            notifyDataSetChanged();
                        }
                    }
                });

                ((MyClickSetter) holder).setOnIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != iconListener) {
                            iconListener.onItemClick(null, v, position, getItemId(position));
                            notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemIconClickListener(AdapterView.OnItemClickListener listener) {
        this.iconListener = listener;
    }

    public Object getItem(int position) {
        if (position < albums.size()) {
            return albums.get(position);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowLoading && position >= albums.size()) {
            return TYPE_LOADING;
        }
        return 0;
    }

    public abstract RecyclerView.ViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType);

    private LoadingViewHolder loadingViewHolder;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOADING) {
            return loadingViewHolder;
        }
        return onCreateChildViewHolder(parent, viewType);
    }


    public int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }


    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivLoadingView, ivLoadingCenterIcon;
        protected Resources mRes;
        Animation animation = null;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            ivLoadingView = (ImageView) itemView.findViewById(R.id.iv_loading);
            ivLoadingCenterIcon = (ImageView) itemView.findViewById(R.id.iv_loading_center_icon);
            mRes = itemView.getResources();
            ivLoadingView.setImageDrawable(mRes.getDrawable(R.drawable.fm_album_loading_rotate));
            ivLoadingCenterIcon.setImageDrawable(mRes.getDrawable(R.drawable.fm_album_loading_icon));
            animation = AnimationUtil.createSmoothForeverAnimation(null);
        }

        public void startLoading() {
            ivLoadingView.startAnimation(animation);
        }

        public void stopLoading() {
            ivLoadingView.clearAnimation();
        }
    }


    public static interface MyClickSetter {
        public void setOnClickListener(View.OnClickListener listener);

        public void setOnIconClickListener(View.OnClickListener listener);
    }


    private boolean isFresco() {
        return ImageFactory.getInstance().getImageType() == 4;
    }
}
