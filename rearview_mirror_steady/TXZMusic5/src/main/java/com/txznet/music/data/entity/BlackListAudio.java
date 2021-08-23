package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.text.TextUtils;

import com.txznet.comm.util.StringUtils;

/**
 * @author zackzhou
 * @date 2019/1/10,15:36
 */
@Entity
public class BlackListAudio extends AudioV5 {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioV5)) return false;

        AudioV5 audio = (AudioV5) o;

        if (sourceUrl != null && !sourceUrl.equals(audio.sourceUrl)) {
            return false;
        }

        if (0 == sid && audio.sid == sid) {
            if (!TextUtils.isEmpty(StringUtils.toString(artist)) && TextUtils.equals(audio.name, name) && TextUtils.equals(StringUtils.toString(audio.artist), StringUtils.toString(artist))) {
                return true;
            }
        }
        if (id != audio.id) return false;
        return sid == audio.sid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (sid != 0) {
            result = prime * result + (int) (id ^ (id >>> 32));
        }
        result = prime * result + sid;
        return result;
    }

}
