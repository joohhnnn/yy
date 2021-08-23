package com.txznet.txz.component.media.model;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 电台节目model
 * Created by J on 2018/5/2.
 */

public class AudioModel extends MediaModel {
    private String mTag;
    private String[] mArrAudioIndex;

    /**
     * 设置tag
     * @param tag
     */
    public void setTag(String tag) {
        this.mTag = tag;
    }

    public void setArrAudioIndex(String[] index) {
        this.mArrAudioIndex = index;
    }

    /**
     * 获取tag
     * @return
     */
    public String getTag() {
        return mTag;
    }

    public String[] getArrAudioIndex() {
        return mArrAudioIndex;
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject obj = super.toJsonObject();

        try {
            obj.put("tag", mTag);
            obj.put("field", 2);
            JSONArray jsonIndexs = new JSONArray();
            if (mArrAudioIndex != null) {
                for (int i = 0; i < mArrAudioIndex.length; ++i) {
                    if (null != mArrAudioIndex[i]) {
                        jsonIndexs.put(mArrAudioIndex[i]);
                    }
                }
            }
            obj.put("audioIndex", jsonIndexs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }
}
