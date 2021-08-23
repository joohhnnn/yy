package com.txznet.music.albumModule.ui.adapter;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.widget.ShadeImageView;

/**
 * Created by telenewbie on 2017/9/25.
 */

public class AlbumViewHolder extends AlbumBaseViewHolder {

    private TextView tvIntro;
    private ShadeImageView ivType;
    private Resources mRes;
    private ImageView ivFocus;
    private ImageView ivPlaying;
    private LinearLayout mLlAlbum;
    public ImageView ivNovelStatus, ivIconNew;

    public AlbumViewHolder(View itemView) {
        super(itemView);
        ivType = (ShadeImageView) itemView.findViewById(R.id.type_iv);
        tvIntro = (TextView) itemView.findViewById(R.id.intro_tv);
        mRes = GlobalContext.get().getResources();
        tvIntro.setTextColor(mRes.getColor(R.color.album_intro_text_color));
        tvIntro.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRes.getDimension(R.dimen.album_intro_text_size));
        ivFocus = (ImageView) itemView.findViewById(R.id.iv_focus);
        ivPlaying = (ImageView) itemView.findViewById(R.id.iv_playing);
        mLlAlbum = (LinearLayout) itemView.findViewById(R.id.ll_album);
        ivNovelStatus = (ImageView) itemView.findViewById(R.id.iv_novel_status);
        ivIconNew = (ImageView) itemView.findViewById(R.id.iv_novel_new);
    }

    public void setIntro(String text) {
        if (StringUtils.isEmpty(text)) {
            tvIntro.setVisibility(View.GONE);
            return;
        }
        tvIntro.setVisibility(View.VISIBLE);
        // tvIntro.setHeight(getFontHeight(tvIntro.getTextSize()) * 3);
        tvIntro.setText(text);
    }

    public void updateNovelStatus(int novelStatus) {
        if (Album.NOVEL_STATUS_INVALID == novelStatus) {
            ivNovelStatus.setVisibility(View.GONE);
        } else if (Album.NOVEL_STATUS_SERIALIZE == novelStatus) {
            ivNovelStatus.setVisibility(View.VISIBLE);
            ivNovelStatus.bringToFront();
            ivNovelStatus.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.novel_serialize));
        } else if (Album.NOVEL_STATUS_ENDED == novelStatus) {
            ivNovelStatus.setVisibility(View.VISIBLE);
            ivNovelStatus.bringToFront();
            ivNovelStatus.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.novel_ended));
        } else {
            ivNovelStatus.setVisibility(View.GONE);
        }
    }

    public ImageView getImageView() {
        return ivType;
    }

    public TextView getTitle() {
        return tvIntro;
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