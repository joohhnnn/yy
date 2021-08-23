package com.txznet.music.data.http.resp;

/**
 * 请求当前分时段的响应体
 * 'next_category_id': 200001,
 * 'next_album_id': 1000757,
 * 'next_audio_id': 0,
 * 'remain_time': 3600
 */
public class RespCarFmCurTops extends BaseResponse {


    private long next_category_id;// 必定有值
    private long next_album_id;  // 0
    private int next_album_sid;  // 0
    private long next_audio_id; //
    private long remain_time; // 必定有值 >0  则开启定时器
    private long timer;//单位都为s ,  ==0  则不轮询 >0 则轮询  (只是测试使用,方便测试)
    private String next_album_name;//下一个专辑的名称


    public RespCarFmCurTops() {
    }

    public long getNext_category_id() {
        return next_category_id;
    }

    public void setNext_category_id(long next_category_id) {
        this.next_category_id = next_category_id;
    }

    public long getNext_album_id() {
        return next_album_id;
    }

    public void setNext_album_id(long next_album_id) {
        this.next_album_id = next_album_id;
    }

    public long getNext_audio_id() {
        return next_audio_id;
    }

    public void setNext_audio_id(long next_audio_id) {
        this.next_audio_id = next_audio_id;
    }

    public long getRemain_time() {
        return remain_time;
    }

    public void setRemain_time(long remain_time) {
        this.remain_time = remain_time;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public String getNext_album_name() {
        return next_album_name;
    }

    public void setNext_album_name(String next_album_name) {
        this.next_album_name = next_album_name;
    }

    public int getNext_album_sid() {
        return next_album_sid;
    }

    public void setNext_album_sid(int next_album_sid) {
        this.next_album_sid = next_album_sid;
    }

    @Override
    public String toString() {
        return "RespCarFmCurTops{" +
                "next_category_id=" + next_category_id +
                ", next_album_id=" + next_album_id +
                ", next_album_sid=" + next_album_sid +
                ", next_audio_id=" + next_audio_id +
                ", remain_time=" + remain_time +
                ", timer=" + timer +
                ", next_album_name='" + next_album_name + '\'' +
                '}';
    }
}
