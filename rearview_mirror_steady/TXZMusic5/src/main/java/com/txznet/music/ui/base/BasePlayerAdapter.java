package com.txznet.music.ui.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.widget.PlayingStateView;

import java.util.List;

/**
 * 通用的当前播放状态的信息的同步
 *
 * @author telen
 * @date 2018/12/12,15:57
 */
public abstract class BasePlayerAdapter<T, VH extends BaseViewHolder> extends RecyclerArrayAdapter<T> {
    //    protected List<T> mData;
    private AudioV5 mPlayingAudio;
    private Album mPlayingAlbum;
    protected boolean mIsPlaying = false;

    public BasePlayerAdapter(Context context) {
        super(context);
    }

    public BasePlayerAdapter(Context context, T[] objects) {
        super(context, objects);
    }

    public BasePlayerAdapter(Context context, List<T> objects) {
        super(context, objects);
    }

    public void setPlayingAudio(AudioV5 audioV5) {
        //如果当前的列表数据还没有刷出来
        if (mObjects == null) {
            mPlayingAudio = audioV5;
            return;
        }

        int endIndex = 0;
        int startIndex = -1;
        if (mPlayingAudio != null) {
            startIndex = mObjects.indexOf(mPlayingAudio);
        }
        if (startIndex != -1) {
            notifyItemContentChanged(headers.size() + startIndex);
        }

        mPlayingAudio = audioV5;
        endIndex = mObjects.indexOf(mPlayingAudio);
        if (endIndex != -1) {
            notifyItemContentChanged(headers.size() + endIndex);
        }
    }

    public void setPlayingAlbum(Album album) {
// TODO: 2018/12/26 需要区分不同的类型,来进行选择性的赋值.
//        如果data为album类型,则这里就有用,否则audio才有用
        //如果当前的列表数据还没有刷出来
        if (mObjects == null) {
            mPlayingAlbum = album;
            return;
        }

        int endIndex = 0;
        int startIndex = -1;
        if (mPlayingAlbum != null) {
            startIndex = mObjects.indexOf(mPlayingAlbum);
        }
        if (startIndex != -1) {
            notifyItemContentChanged(headers.size() + startIndex);
        }
        mPlayingAlbum = album;
        endIndex = mObjects.indexOf(mPlayingAlbum);
        if (endIndex != -1) {
            notifyItemContentChanged(headers.size() + endIndex);
        }
    }

    /**
     * 获取播放项目的索引值
     *
     * @return
     */
    private int getPlayingItem() {
        int resultIndex = -1;
        if (mPlayingAlbum != null) {
            resultIndex = mObjects.indexOf(mPlayingAlbum);
        } else if (mPlayingAudio != null) {
            resultIndex = mObjects.indexOf(mPlayingAudio);
        }
        return resultIndex;
    }


    public void setPlayState(boolean isPlaying) {
        if (mPlayingAudio != null || mPlayingAlbum != null) {
            mIsPlaying = isPlaying;
        } else {
            mIsPlaying = false;
        }
        int playingItem = getPlayingItem();
        if (playingItem >= 0) {
            notifyItemContentChanged(headers.size() + playingItem);
        }

    }

    protected void notifyItemContentChanged(int index) {
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull VH holder, int position) {
//        if (mPlayingAudio != null && mData.get(position).equals(mPlayingAudio)) {
//            changePlayObj(holder);
//        } else {
//            changeUnPlayingStatus(holder);
//        }
//    }


    @Override
    public void OnBindViewHolder(BaseViewHolder holder, int position) {
        super.OnBindViewHolder(holder, position);

        T t = mObjects.get(position);
        if (t.equals(mPlayingAudio)) {
            changePlayObj((VH) holder, position);
        } else if (t.equals(mPlayingAlbum)) {
//            changePlayingAlbumStatus((VH) holder);
            // FIXME: 2019/3/20 AI电台需要音频符合才描红
            if (AlbumUtils.isAiRadio(mPlayingAlbum)) {
                if (t instanceof HistoryAlbum) {
                    if (((HistoryAlbum) t).audio.equals(mPlayingAudio)) {
                        changePlayObj((VH) holder, position);
                    } else {
                        changeUnPlayingStatus((VH) holder, position);
                    }
                }
            } else {
                changePlayObj((VH) holder, position);
            }
        } else {
            changeUnPlayingStatus((VH) holder, position);
        }

        if (holder instanceof IPlayerStateViewHolder) {
            PlayingStateView playingStateView = ((IPlayerStateViewHolder) holder).getPlayingStateView();
            if (playingStateView.getVisibility() == View.VISIBLE) {
                if (mIsPlaying) {
                    playingStateView.onPlay();
                } else {
                    playingStateView.onPause();
                }
            }
        }
    }

    protected abstract void changePlayObj(@NonNull VH holder, int position);


//    protected void changePlayingAlbumStatus(@NonNull VH holder) {
//
//    }

    protected abstract void changeUnPlayingStatus(@NonNull VH holder, int position);

}
