package com.txznet.txz.component.media.model;

import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.media.TXZMediaModel;
import com.txznet.txz.module.media.plugin.PluginMediaModel;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 媒体model
 * Created by J on 2018/4/28.
 */

public class MediaModel {
    private String mTitle;
    private String[] mArrKeywords;
    private String[] mArrArtists;
    private String mAsrText;
    private String mAlbum;
    private String mCategory;
    private String mSubCategory;

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

    public static MediaModel fromJsonObject(JSONObject obj) {
        MediaModel model = new MediaModel();

        try {
            if (obj.has("title")) {
                model.setTitle(obj.getString("title"));
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

            if (obj.has("artist")) {
                JSONArray jsonArtists = obj.getJSONArray("artist");
                if (null != jsonArtists && jsonArtists.length() > 0) {
                    String[] arrArtists = new String[jsonArtists.length()];
                    for (int i = 0; i < jsonArtists.length(); i++) {
                        arrArtists[i] = jsonArtists.getString(i);
                    }

                    model.setArtists(arrArtists);
                }
            }

            if (obj.has("keywords")) {
                JSONArray jsonKeywords = obj.getJSONArray("keywords");
                if (null != jsonKeywords && jsonKeywords.length() > 0) {
                    String[] arrKeywords = new String[jsonKeywords.length()];
                    for (int i = 0; i < jsonKeywords.length(); i++) {
                        arrKeywords[i] = jsonKeywords.getString(i);
                    }

                    model.setArtists(arrKeywords);
                }
            }

            return model;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONObject toJsonObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", mTitle);
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

    public TXZMusicManager.MusicModel toMusicModel() {
        TXZMusicManager.MusicModel model = new TXZMusicManager.MusicModel();
        model.setText(mAsrText);
        model.setAlbum(mAlbum);
        model.setField(1);
        model.setArtist(mArrArtists);
        model.setKeywords(mArrKeywords);
        model.setSubCategory(mSubCategory);
        model.setTitle(mTitle);

        return model;
    }

    public static MediaModel fromMusicModel(TXZMusicManager.MusicModel musicModel) {
        if (null == musicModel) {
            return null;
        }

        MediaModel model = new MediaModel();
        model.setAlbum(musicModel.getAlbum());
        model.setArtists(musicModel.getArtist());
        model.setKeywords(musicModel.getKeywords());
        model.setSubCategory(musicModel.getSubCategory());
        model.setTitle(musicModel.getTitle());

        return model;
    }

    public TXZMediaModel toTXZMediaModel() {
        TXZMediaModel model = new TXZMediaModel();
        model.setAsrText(mAsrText);
        model.setAlbum(mAlbum);
        model.setArtists(mArrArtists);
        model.setKeywords(mArrKeywords);
        model.setCategory(mCategory);
        model.setSubCategory(mSubCategory);
        model.setTitle(mTitle);

        return model;
    }

    public PluginMediaModel toPluginMediaModel() {
        PluginMediaModel model = new PluginMediaModel();
        model.setAsrText(mAsrText);
        model.setAlbum(mAlbum);
        model.setArtists(mArrArtists);
        model.setKeywords(mArrKeywords);
        model.setCategory(mCategory);
        model.setSubCategory(mSubCategory);
        model.setTitle(mTitle);

        return model;
    }

    public static MediaModel fromPluginMediaModel(PluginMediaModel pModel) {
        MediaModel model = new MediaModel();
        model.setAsrText(pModel.getAsrText());
        model.setAlbum(pModel.getAlbum());
        model.setArtists(pModel.getArtists());
        model.setKeywords(pModel.getKeywords());
        model.setCategory(pModel.getCategory());
        model.setSubCategory(pModel.getSubCategory());
        model.setTitle(pModel.getTitle());

        return model;
    }
}
