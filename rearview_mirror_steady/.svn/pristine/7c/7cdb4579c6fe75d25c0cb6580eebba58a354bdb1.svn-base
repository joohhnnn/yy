package com.txznet.music.utils;

import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.favor.bean.FavourBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Audio 互转工具类
 */
public class AudioUtils {


    public static Audio fromFavourBean(FavourBean favourBean) {
        if (favourBean != null) {
            Audio audio = favourBean.getAudio();
            audio.setOperTime(favourBean.getTimestamp());
            return audio;
        }
        return null;
    }

    public static List<Audio> fromFavourBeans(List<FavourBean> favourBeans) {
        if (favourBeans != null && favourBeans.size() > 0) {
            List<Audio> audios = new ArrayList<>();
            for (FavourBean favourBean : favourBeans) {
                audios.add(fromFavourBean(favourBean));
            }
            return audios;
        }
        return null;
    }


}
