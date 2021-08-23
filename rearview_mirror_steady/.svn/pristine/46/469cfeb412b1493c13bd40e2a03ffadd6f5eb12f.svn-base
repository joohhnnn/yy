package com.txznet.music.albumModule.ui.adapter;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.utils.AttrUtils;

/**
 * Created by telenewbie on 2017/9/25.
 */

public class SingerViewHolder extends AlbumBaseViewHolder {

    public TextView tvIntro;
    public ImageView ivType;
    public Resources mRes;
    public ImageView ivFocus;
    public ImageView ivPlaying;
    public LinearLayout mLlAlbum;

    public SingerViewHolder(View itemView) {
        super(itemView);
        ivType = (ImageView) itemView.findViewById(R.id.type_iv);
        tvIntro = (TextView) itemView.findViewById(R.id.intro_tv);
        mRes = GlobalContext.get().getResources();
        tvIntro.setTextColor(mRes.getColor(R.color.album_intro_text_color));
        tvIntro.setTextSize(TypedValue.COMPLEX_UNIT_PX, AttrUtils.getAttrDimension(GlobalContext.get(), R.attr.text_size_h4, 0));
        ivFocus = (ImageView) itemView.findViewById(R.id.iv_focus);
        ivPlaying = (ImageView) itemView.findViewById(R.id.iv_playing);
        mLlAlbum = (LinearLayout) itemView.findViewById(R.id.ll_album);
    }

    @Override
    public ImageView getPlayingView() {
        return ivPlaying;
    }

    @Override
    public View getClickRange() {
        return ivType;
    }

}