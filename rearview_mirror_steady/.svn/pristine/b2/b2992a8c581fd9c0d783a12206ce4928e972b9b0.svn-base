package com.txznet.music.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.ui.TimeOfThemeClickListener;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.zip.Inflater;

import butterknife.Bind;

import static com.txznet.music.albumModule.bean.Album.FLAG_CURRENT_TIME_ZONE;

/**
 * Created by 58295 on 2018/4/16.
 */

public class ItemRadioRecommendAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    public Context mContext;
    public List<T> mList;
    public TimeOfThemeClickListener mListener;
    public int selectPosition = -1;

    public ItemRadioRecommendAdapter(Context context, List<T> list) {
        mContext = context;
        mList = list;
    }

    public void setData(List<T> list) {
        mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_radio_recomend, parent, false);
        return new RadioRecommendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final RadioRecommendViewHolder v = (RadioRecommendViewHolder) holder;
        Album album = (Album) mList.get(position);
        v.tvName.setText(album.getName());

        //进入先默认选中标识位下的额，如果点击后就执行第二个


        if (album.equals(CarFmUtils.getInstance().getIsPlayingAlbum()) && selectPosition == -1) {
            v.layout.setBackgroundColor(mContext.getResources().getColor(R.color.menu_text_selected));
            selectPosition = position;
        } else if (selectPosition == position) {
            v.layout.setBackgroundColor(mContext.getResources().getColor(R.color.menu_text_selected));
            selectPosition = position;
        } else {
            v.layout.setBackgroundColor(mContext.getResources().getColor(R.color.bg_blur_filter));
        }
        v.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    int index = v.getAdapterPosition();
                    if (index != RecyclerView.NO_POSITION && selectPosition != index) {
                        selectPosition = index;
                        notifyDataSetChanged();
                        mListener.clickTheme((Album) mList.get(index));
                    }
                }
            }
        });

    }

    public void setOnItemClickListener(TimeOfThemeClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(mList)) {
            return mList.size();
        }
        return 0;
    }

    public static class RadioRecommendViewHolder extends ViewHolder {

        public TextView tvName;
        public RelativeLayout layout;

        public RadioRecommendViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            layout = (RelativeLayout) itemView.findViewById(R.id.item_radio_recommend_layout);
        }

    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
}
