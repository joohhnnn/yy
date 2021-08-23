package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-06 20:34
 */
public class HomeMusicHolder {

    private static HomeMusicHolder instance = new HomeMusicHolder();
    public static HomeMusicHolder getInstance(){
        return instance;
    }

    private View mRootView;
    private ImageButton btnMore;
    private TextView tvSongName;
    private TextView tvAuthor;
    private ImageButton imgBtnPre;
    private ImageButton imgBtnNext;
    private ImageButton imgBtnPlay;

    public View getView(){
        if(mRootView == null){
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_music, (ViewGroup)null);
            init();
        }
        return mRootView;
    }

    private void init(){
        btnMore = mRootView.findViewById(R.id.imgBtnMore);
        tvSongName = mRootView.findViewById(R.id.tvSongName);
        tvAuthor = mRootView.findViewById(R.id.tvAuthor);
        imgBtnPre = mRootView.findViewById(R.id.imgBtnPre);
        imgBtnNext = mRootView.findViewById(R.id.imgBtnNext);
        imgBtnPlay = mRootView.findViewById(R.id.imgBtnPlay);

        // 点击更多
        mRootView.findViewById(R.id.wrapMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_MUSIC_MORE,
                        0, 0, 0);
            }
        });
        // 点击更多
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_MUSIC_MORE,
                        0, 0, 0);
            }
        });
        // 点击播放
        imgBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_MUSIC_PLAY,
                        0, 0, 0);
            }
        });
        // 点击上一曲
        imgBtnPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_MUSIC_PRE,
                        0, 0, 0);
            }
        });
        // 点击下一曲
        imgBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_MUSIC_NEXT,
                        0, 0, 0);
            }
        });
    }

    public void update(SmartHandyHomeViewData.MusicData data){
        if(data == null){
            return;
        }
        if(TextUtils.isEmpty(data.songName)){
            tvSongName.setText("暂无歌曲");
        } else {
            tvSongName.setText(data.songName);
        }
        if(TextUtils.isEmpty(data.author)){
            tvAuthor.setText("未知");
        } else {
            tvAuthor.setText(data.author);
        }

        if(data.isPlay){
            imgBtnPlay.setImageResource(R.drawable.smart_handy_icon_music_pause);
        } else {
            imgBtnPlay.setImageResource(R.drawable.smart_handy_icon_music_play);
        }

    }

}
