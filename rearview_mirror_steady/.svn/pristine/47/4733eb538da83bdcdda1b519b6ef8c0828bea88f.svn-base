package com.txznet.music.data.entity;

import java.util.List;
import java.util.Objects;

/**
 * 映射关系
 * 用于后台下发配置，决定sid对应的类型
 */
public class PlayConfs {
    public List<PlayConf> arrPlay;

    public FlashPage flashPage;

    public List<FlashPage> launchPage;

    public List<String> launchLogo;

    public class FlashPage {
        /*
            "flashPage": {
                "url": "http://img.kaolafm.net/mz/images/201308/108c443e-19cd-4745-bd8b-3f7ee0bd2121/default.jpg",
                "time": 5000,
                "start_time": "2019-01-15",
                "end_time": "2019-01-16"
            },
         */

        public String url;
        public long time;
        public String start_time;
        public String end_time;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FlashPage flashPage = (FlashPage) o;

            if (time != flashPage.time) return false;
            if (!Objects.equals(url, flashPage.url)) return false;
            if (!Objects.equals(start_time, flashPage.start_time))
                return false;
            return Objects.equals(end_time, flashPage.end_time);
        }

        @Override
        public int hashCode() {
            int result = url != null ? url.hashCode() : 0;
            result = 31 * result + (int) (time ^ (time >>> 32));
            result = 31 * result + (start_time != null ? start_time.hashCode() : 0);
            result = 31 * result + (end_time != null ? end_time.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FlashPage{" +
                    "url='" + url + '\'' +
                    ", time=" + time +
                    ", start_time='" + start_time + '\'' +
                    ", end_time='" + end_time + '\'' +
                    '}';
        }
    }

    public static class PlayConf {
        public static final int MUSIC_TYPE = 1;
        public static final int RADIO_TYPE = 2;
        public static final int NEWS_TYPE = 3;

        public int sid;
        public int play; // 1： 同行者播放， 0：原应用app播放
        public int type;// 1,music,2.电台，3 新闻
        public String logo;//来源的LOGO
        public String sourceFrom; // 来源

        @Override
        public String toString() {
            return "PlayConf{" +
                    "sid=" + sid +
                    ", play=" + play +
                    ", type=" + type +
                    ", logo='" + logo + '\'' +
                    ", sourceFrom='" + sourceFrom + '\'' +
                    '}';
        }
    }
}
