package com.txznet.txz.component.media.model;

import org.json.JSONObject;

/**
 * 音乐Model
 * Created by J on 2018/5/2.
 */

public class MusicModel extends MediaModel {
    /**
     * 文件路径
     */
    protected String path;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public JSONObject toJsonObject() {
        JSONObject obj = super.toJsonObject();

        try {
            obj.put("path", path);
            obj.put("field", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }
}
