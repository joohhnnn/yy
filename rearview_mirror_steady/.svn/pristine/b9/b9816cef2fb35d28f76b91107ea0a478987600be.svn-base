package com.txznet.marketing.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by JackPan on 2019/4/29
 * Describe: 音频焦点的管理工具类
 */
public class AudioFocusUtil {

    /**
     * @Date 创建时间：2019/4/29
     * @author : JackPan
     * @Description : 用AudioManager获取音频焦点避免视频声音并发的问题
     */
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mFocusListener;

    public int requestAudioFocus(final AudioListener audioListener, Context context){
        if (mAudioManager == null){
            mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        }

        if (mFocusListener == null){
            mFocusListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_GAIN:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                            //播放操作
                            audioListener.start();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            //暂停操作
                            audioListener.pause();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        //下面两个常量参数试过很多 都无效，最终反编译了其他app才搞定，汗~
        int requestFocusResult = mAudioManager.requestAudioFocus(mFocusListener,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        return requestFocusResult;
    }

    //暂停、播放完成或退到后台释放音频焦点
    public void releaseTheAudioFocus() {
        if (mAudioManager != null && mFocusListener != null) {
            mAudioManager.abandonAudioFocus(mFocusListener);
        }
    }

    public interface AudioListener{
        void start();
        void pause();
    }

}
