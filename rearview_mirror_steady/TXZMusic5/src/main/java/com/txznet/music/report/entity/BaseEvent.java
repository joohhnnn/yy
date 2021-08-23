package com.txznet.music.report.entity;

import com.txznet.music.util.ProgramUtils;

/**
 * 上报基准，5.0版本定义
 *
 * @author zackzhou
 * @date 2018/12/26,19:53
 */
public class BaseEvent {
    public long seqId;
    public final int eventId;
    public long tmStamp;
    public int version;
    public int cliType; // 终端类型：同听APK（2）， 同听小程序（1）
    public String svrData = ""; // 预留字段，必须传递

    public BaseEvent(int eventId) {
        this.eventId = eventId;
        this.cliType = ProgramUtils.isProgram() ? 1 : 2;
    }
}
