package com.txznet.music.data.entity;

/**
 * @author telen
 * @date 2019/1/2,11:04
 */
public interface IHaveTimeStamp {
//    public long timestamp;

    static String getQueryCondition(long starttime, long endTime) {
        StringBuilder stringBuilder = new StringBuilder();
        if (starttime == 0) {
            stringBuilder.append("timestamp >= ");
            stringBuilder.append(endTime);
        } else if (endTime == 0) {
            stringBuilder.append("timestamp < ");
            stringBuilder.append(starttime);
        } else {
            stringBuilder.append("timestamp >= ");
            stringBuilder.append(endTime);
            stringBuilder.append("timestamp < ");
            stringBuilder.append(starttime);
        }
        return stringBuilder.toString();
    }

}
