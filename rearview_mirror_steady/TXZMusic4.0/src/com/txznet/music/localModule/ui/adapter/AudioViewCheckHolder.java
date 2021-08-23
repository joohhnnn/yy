package com.txznet.music.localModule.ui.adapter;

import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.baseModule.adapter.ResourceViewHolder;
import com.txznet.music.utils.ScreenUtils;

/**
 * Created by telenewbie on 2017/12/15.
 */
public class AudioViewCheckHolder extends ResourceViewHolder /*implements View.OnClickListener*/ {
    public TextView mTitle, mTitleAuthor;
    public RelativeLayout mLl_item;
//    public ImageView mIvLeftIcon;

    public AppCompatCheckBox cbCheck;
    public LinearLayout cbLayout;
    public ImageView mIvDelete, mIvFavour;

    public AudioViewCheckHolder(View itemView) {
        super(itemView);
        cbLayout = (LinearLayout) itemView.findViewById(R.id.cb_layout);
        mTitle = (TextView) itemView.findViewById(R.id.title);
        mLl_item = (RelativeLayout) itemView.findViewById(R.id.ll_item);
        cbCheck = (AppCompatCheckBox) itemView.findViewById(R.id.cb_check);
        mIvDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
        mIvFavour = (ImageView) itemView.findViewById(R.id.iv_favour);
        mTitleAuthor = itemView.findViewById(R.id.title_author);

    }


    public static int getLayoutResourceId() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.item_list_check_local_phone_portrait;
        }
        return R.layout.item_list_check_local;
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
