package com.txznet.music.data.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.data.kaola.net.bean.KaolaAudio;
import com.txznet.music.data.netease.NeteaseSDK;
import com.txznet.music.data.netease.net.bean.NeteaseAudio;
import com.txznet.music.data.netease.net.bean.NeteaseUrl;
import com.txznet.music.data.utils.OnGetData;
import com.txznet.music.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by telenewbie on 2018/2/8.
 */

public class AdapterAudio {

    public final static int TYPE_KAOLA = 1;
    public final static int TYPE_UNKNOW = -1;
    public final static int TYPE_NETEASE = 2;


    KaolaAudio kaolaAudio;
    NeteaseAudio neteaseAudio;
    Album mAlbum;
    private List<String> artists = new ArrayList<>();

    int type = TYPE_UNKNOW;


    public AdapterAudio(KaolaAudio kaolaAudio, Album album) {
        this.kaolaAudio = kaolaAudio;

        mAlbum = album;
        type = TYPE_KAOLA;
    }

    public AdapterAudio(NeteaseAudio neteaseAudio) {
        this.neteaseAudio = neteaseAudio;
        type = TYPE_NETEASE;
    }


    public boolean isNetease() {
        return type == TYPE_NETEASE;
    }

    //歌曲信息,歌曲名称,图片,id

    public String getAudioName() {
        return isNetease() ? neteaseAudio.getName() : kaolaAudio.getAudioName();
    }


    public String getAlbumName() {
        return isNetease() ? neteaseAudio.getAlbumName() : kaolaAudio.getAlbumName();
    }

    public String getAudioPic() {
        return isNetease() ? neteaseAudio.getCoverUrl() : kaolaAudio.getAudioPic();
    }

    public String getAlbumPic() {
        return isNetease() ? neteaseAudio.getCoverUrl() : kaolaAudio.getAlbumPic();
    }

    public String getId() {
        return isNetease() ? neteaseAudio.getId() : String.valueOf(kaolaAudio.getAudioId());
    }

    public int getSid() {
        return isNetease() ? 1 : 2;
    }

    public String getAlbumId() {
        return isNetease() ? String.valueOf(neteaseAudio.getAlbumId()) : String.valueOf(kaolaAudio.getAlbumId());
    }

    public String getArtist() {
        return isNetease() ? neteaseAudio.getAlbumArtistName() : kaolaAudio.getHost().toString();
    }

    /**
     * 强制按照128的比特率进行请求,后期如果有需求进行变动,则可以进行修改
     *
     * @param getUrl
     */
    public void getUrl(final OnGetData<String> getUrl) {
        if (isNetease()) {
            NeteaseSDK.getInstance().getUrl(neteaseAudio.getId(), NeteaseSDK.getInstance().getBitrate(), new OnGetData<NeteaseUrl>() {
                @Override
                public void success(NeteaseUrl neteaseUrl) {
                    getUrl.success(neteaseUrl.getData().getUrl());
                }

                @Override
                public void failed(int errorCode) {
                    getUrl.failed(errorCode);
                }
            });
        } else {
            getUrl.success(kaolaAudio.getAacPlayUrl128());
        }
    }


    public Audio getAudio() {
        Audio audio = new Audio();
        if (isNetease()) {
            audio.setName(neteaseAudio.getName());
            if (StringUtils.isNotEmpty(neteaseAudio.getAlbumArtistName())) {
                artists.clear();
                artists.add(neteaseAudio.getAlbumArtistName());
                audio.setArrArtistName(artists);
            }
            audio.setStrId(neteaseAudio.getId());
            audio.setLogo(neteaseAudio.getCoverUrl());
            audio.setId(neteaseAudio.getId().hashCode());
            audio.setSid(AdapterAudio.TYPE_NETEASE);
            audio.setSourceFrom("网易云音乐");
            audio.setDownloadType("3");
        } else {
            audio.setName(getAudioName());
            artists.clear();
            artists.add(getArtist());
            audio.setArrArtistName(artists);
            if (null != mAlbum) {
                audio.setAlbumId(String.valueOf(mAlbum.getId()));
                audio.setAlbumName(mAlbum.getName());
            }
            audio.setLogo(getAudioPic());
            audio.setId(getId().hashCode());
            audio.setSid(AdapterAudio.TYPE_KAOLA);
            audio.setSourceFrom("考拉");
            audio.setStrDownloadUrl(kaolaAudio.getAacPlayUrl());
            audio.setDownloadType("2");
        }
        return audio;
    }

}
