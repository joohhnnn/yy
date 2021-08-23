package com.txznet.music.localModule.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.adapter.ResourceViewHolder;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.ToastUtils;

/**
 * Created by telenewbie on 2017/12/15.
 */
public class AudioViewHolder extends ResourceViewHolder /*implements View.OnClickListener*/ {
    public TextView mTitle, mTitleAuthor;
    public RelativeLayout mLl_item;
    public ImageView mIvLeftIcon;
    public ImageView mIvDelete, mIvFavour;

    public AudioViewHolder(View itemView) {
        super(itemView);
        mTitle = (TextView) itemView.findViewById(R.id.title);
        mLl_item = (RelativeLayout) itemView.findViewById(R.id.ll_item);
        mIvLeftIcon = (ImageView) itemView.findViewById(R.id.iv_left_list);
        mIvDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
        mIvFavour = (ImageView) itemView.findViewById(R.id.iv_favour);
        mTitleAuthor = (TextView) itemView.findViewById(R.id.title_author);

    }


    public static int getLayoutResourceId() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.item_list_history_music_phone_portrait;
        }
        return R.layout.item_list_history_music;
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.iv_delete:
//                if (FileUtils.delFile(song.getStrDownloadUrl())) {
//                    // 从本地中删除数据
//                    DBManager.getInstance().removeLocalAudios(song);
//                    // 更新界面
//                    mAudios.remove(position);
//                    notifyDataSetChanged();
//                    ObserverManage.getObserver().send(InfoMessage.DELETE_LOCAL_MUSIC, song);
//                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
//                    if (null != currentAudio && 0 == currentAudio.getSid()) {
//                        PlayInfoManager.getInstance().setPlayListTotalNum(mAudios.size());
//                        ObserverManage.getObserver().send(InfoMessage.SET_PLAY_LIST_TOTAL_NUM);
//                    }
//                } else {
//                    ToastUtils.showShortOnUI("删除失败");
//                }
//                break;
//            case R.id.iv_favour:
//                break;
//        }
//    }
}
