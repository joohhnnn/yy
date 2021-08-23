package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.anim.LogoAnimView;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.data.LogoAnimationViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ILogoAnimationView;
import com.txznet.resholder.R;

/**
 * 说明：logo动画
 *
 * @author xiaolin
 * create at 2020-10-16 17:44
 */
public class LogoAnimationView extends ILogoAnimationView implements ExtViewAdapter.Callback {

    private static LogoAnimationView mLogoAnimationView = new LogoAnimationView();

    private LogoAnimationView() {
    }

    public static LogoAnimationView getInstance() {
        return mLogoAnimationView;
    }

    private MediaPlayer mediaPlayer = null;

    @Override
    public ExtViewAdapter getView(ViewData viewData) {
        LogoAnimationViewData logoAnimationViewData = (LogoAnimationViewData) viewData;

        View view = getView(logoAnimationViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = viewData.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = FloatView.getInstance();
        viewAdapter.showRecordView = false;
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;
        viewAdapter.callback = this;

        return viewAdapter;
    }

    private View getView(LogoAnimationViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.logo_animation_view, (ViewGroup) null);
        LogoAnimView ivLogo = view.findViewById(R.id.ivLogo);

        AssetFileDescriptor file;

        switch (viewData.animationType) {
            case "dance": {// 跳舞
                ivLogo.play(LogoAnimView.Anim.DANCE);
                file = context.getResources().openRawResourceFd(R.raw.music_dance);
                break;
            }
            case "spoiled": {// 撒娇
                ivLogo.play(LogoAnimView.Anim.SPOILED);
                file = context.getResources().openRawResourceFd(R.raw.music_spoiled);
                break;
            }
            case "smile": {// 笑
                ivLogo.play(LogoAnimView.Anim.SMILE);
                file = context.getResources().openRawResourceFd(R.raw.music_smile);
                break;
            }
            case "cool": {// 耍酷
                ivLogo.play(LogoAnimView.Anim.COOL);
                file = context.getResources().openRawResourceFd(R.raw.music_cool);
                break;
            }
            default: {
                ivLogo.play(LogoAnimView.Anim.DANCE);
                file = context.getResources().openRawResourceFd(R.raw.music_dance);
                break;
            }
        }

        if(mediaPlayer != null){
            try {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.setVolume(1F, 1F);
            mediaPlayer.start();

            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void show() {

    }

    @Override
    public void dismiss() {
        if (mediaPlayer != null) {
            try {
                if(mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
