package com.txznet.music.data.kaola;

import android.util.Log;

import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.data.kaola.net.bean.KaolaAudio;
import com.txznet.music.data.kaola.net.bean.RespItem;
import com.txznet.music.data.utils.OnGetData;

/**
 * Created by telenewbie on 2018/2/7.
 * 考拉操作相关的操作
 * 考拉只支持播放专辑
 */

public class KaolaPlayHelper {


    //##创建一个单例类##
    private volatile static KaolaPlayHelper singleton;

    private KaolaPlayHelper() {
    }

    public static KaolaPlayHelper getInstance() {
        if (singleton == null) {
            synchronized (KaolaPlayHelper.class) {
                if (singleton == null) {
                    singleton = new KaolaPlayHelper();
                }
            }
        }
        return singleton;
    }


    /**
     * 播放电台节目
     *
     * @param id
     */
    public void playAlbum(long id) {
        //从缓存中判断是否有播放过.
        //TODO:需要从数据库中判断有记录,功能:断点续播
        KaoLaSDK.getInstance().getAudios(id, 0, new OnGetData<RespItem<KaolaAudio>>() {
            @Override
            public void success(RespItem<KaolaAudio> kaolaAudioRespItem) {
                if (kaolaAudioRespItem != null) {
                    if (!kaolaAudioRespItem.getDataList().isEmpty()) {
                        for (KaolaAudio kaolaAudio : kaolaAudioRespItem.getDataList()) {
                            Log.d("playAlbum:", kaolaAudio.toString());


//                            PlayEngineFactory.getEngine().play

                        }
                    }
                }
            }

            @Override
            public void failed(int errorCode) {
                Log.d("playAlbum:", "errorCode:"+errorCode);
            }
        });
    }

}
