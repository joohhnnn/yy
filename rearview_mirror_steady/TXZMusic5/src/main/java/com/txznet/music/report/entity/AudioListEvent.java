package com.txznet.music.report.entity;

import java.util.List;

/**
 * 删除管理
 *
 * @author zackzhou
 * @date 2019/1/20,15:20
 */

public class AudioListEvent extends BaseEvent {

    public List<ReportAudio> audioList;

    public AudioListEvent(int eventId, List<ReportAudio> audioList) {
        super(eventId);
        this.audioList = audioList;
    }

}
