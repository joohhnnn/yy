package com.txznet.music.data.netease.net.bean;

/**
 * Created by telenewbie on 2018/2/8.
 */

public class NeteaseUrl {
    /**
     * code : 200
     * data : {"code":200,"gain":-2.44,"br":128,"url":"http://m10.music.126.net/20180208171332/d39b211c54b2c891c8e3703b4ac500be/ymusic/e278/1de5/0f3d/3d59c4baafb2dd07b469f8db06b76513.mp3?u=y3brjXu31riTqK8MrN4Bqw==","md5":"3d59c4baafb2dd07b469f8db06b76513","size":3209552}
     */

    private int code;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * code : 200
         * gain : -2.44
         * br : 128
         * url : http://m10.music.126.net/20180208171332/d39b211c54b2c891c8e3703b4ac500be/ymusic/e278/1de5/0f3d/3d59c4baafb2dd07b469f8db06b76513.mp3?u=y3brjXu31riTqK8MrN4Bqw==
         * md5 : 3d59c4baafb2dd07b469f8db06b76513
         * size : 3209552
         */

        private int code;
        private double gain;
        private int br;
        private String url;
        private String md5;
        private int size;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public double getGain() {
            return gain;
        }

        public void setGain(double gain) {
            this.gain = gain;
        }

        public int getBr() {
            return br;
        }

        public void setBr(int br) {
            this.br = br;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
