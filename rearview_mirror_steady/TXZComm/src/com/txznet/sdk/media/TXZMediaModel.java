package com.txznet.sdk.media;

import com.txznet.sdk.TXZMusicManager;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * sdk中使用的媒体类型
 *
 * Created by J on 2018/5/8.
 */

public class TXZMediaModel {
    private String mTitle;
    private String[] mArrKeywords;
    private String[] mArrArtists;
    private String mAsrText;
    private String mAlbum;
    private String mCategory;
    private String mSubCategory;

    public TXZMediaModel() {
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setKeywords(String[] keywords) {
        this.mArrKeywords = keywords;
    }

    public void setKeyword(String keyword) {
        this.mArrKeywords = new String[]{keyword};
    }

    public void setArtists(String[] artists) {
        this.mArrArtists = artists;
    }

    public void setArtist(String artist) {
        this.mArrArtists = new String[]{artist};
    }

    public void setAsrText(String asrText) {
        this.mAsrText = asrText;
    }

    public void setAlbum(String album) {
        this.mAlbum = album;
    }

    /**
     * 设置分类
     * @param category
     */
    public void setCategory(String category) {
        this.mCategory = category;
    }

    public void setSubCategory(String subCategory) {
        this.mSubCategory = subCategory;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * 获取关键字
     *
     * @return
     */
    public String[] getKeywords() {
        return mArrKeywords;
    }

    /**
     * 获取歌手
     *
     * @return
     */
    public String[] getArtists() {
        return mArrArtists;
    }


    /**
     * 获取识别文本内容
     *
     * @return
     */
    public String getAsrText() {
        return mAsrText;
    }

    /**
     * 获取专辑
     *
     * @return
     */
    public String getAlbum() {
        return mAlbum;
    }

    /**
     * 获取分类
     * @return
     */
    public String getCategory() {
        return mCategory;
    }

    /**
     * 获取子分类
     * @return
     */
    public String getSubCategory() {
        return mSubCategory;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", mTitle);
            //obj.put("keywords", mArrKeywords);
            //obj.put("artist", mArrArtists);
            obj.put("text", mAsrText);
            obj.put("album", mAlbum);
            obj.put("category", mCategory);
            obj.put("subcategory", mSubCategory);
            JSONArray jsonArtists = new JSONArray();
            if (mArrArtists != null) {
                for (int i = 0; i < mArrArtists.length; ++i) {
                    if (null != mArrArtists[i]) {
                        jsonArtists.put(mArrArtists[i]);
                    }
                }
            }
            obj.put("artist", jsonArtists);
            JSONArray jsonKeywords = new JSONArray();
            if (mArrKeywords != null) {
                for (int i = 0; i < mArrKeywords.length; ++i) {
                    if (null != mArrKeywords[i]) {
                        jsonKeywords.put(mArrKeywords[i]);
                    }
                }
            }
            obj.put("keywords", jsonKeywords);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return obj;
    }

    public static TXZMediaModel fromJSONObject(JSONObject obj) {
        if (null == obj) {
            return null;
        }

        TXZMediaModel model = new TXZMediaModel();
        try {
            if (obj.has("title")) {
                model.setTitle(obj.getString("title"));
            }

            if (obj.has("text")) {
                model.setAsrText(obj.getString("text"));
            }

            if (obj.has("album")) {
                model.setAlbum(obj.getString("album"));
            }

            if (obj.has("category")) {
                model.setCategory(obj.getString("category"));
            }

            if (obj.has("subcategory")) {
                model.setSubCategory(obj.getString("subcategory"));
            }

            if (obj.has("keywords")) {
                JSONArray jsonKeywords = obj.getJSONArray("keywords");
                String[] arrKeywords = new String[jsonKeywords.length()];
                for (int i = 0, len = jsonKeywords.length(); i < len; i++) {
                    arrKeywords[i] = jsonKeywords.getString(i);
                }
                model.setKeywords(arrKeywords);
            }

            if (obj.has("artist")) {
                JSONArray jsonArtists = obj.getJSONArray("artist");
                String[] arrArtists = new String[jsonArtists.length()];
                for (int i = 0, len = jsonArtists.length(); i < len; i++) {
                    arrArtists[i] = jsonArtists.getString(i);
                }
                model.setArtists(arrArtists);
            }

        } catch (Exception e) {
            return null;
        }

        return model;
    }

    public static TXZMediaModel fromMusicModel(TXZMusicManager.MusicModel model) {
        TXZMediaModel ret = new TXZMediaModel();
        ret.setTitle(model.getTitle());
        ret.setKeywords(model.getKeywords());
        ret.setArtists(model.getArtist());
        ret.setAlbum(model.getAlbum());
        ret.setAsrText(model.getText());
        ret.setSubCategory(model.getSubCategory());

        return ret;
    }
}
