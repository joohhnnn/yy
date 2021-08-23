package com.txznet.launcher.module.record.bean;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.sdk.bean.TxzPoi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 位置列表数据
 */

public class PoiListMsgData extends BaseListMsgData<PoiListMsgData.PoiItem> {
    public String keywords;
    public String city;
    public Integer showCount;
    public Integer mapAction;
    public Double locationLat;
    public Double locationLng;
    public Double destinationLat;
    public Double destinationLng;
    public Boolean isListModel;

    public PoiListMsgData() {
        super(TYPE_FULL_LIST_POI);
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        keywords = jsData.getVal("keywords", String.class);
        city = jsData.getVal("city", String.class);
        String business = jsData.getVal("poitype", String.class);
        showCount = jsData.getVal("showcount", Integer.class);
        mapAction = jsData.getVal("mapAction", Integer.class);
        locationLat = jsData.getVal("locationLat", Double.class);
        locationLng = jsData.getVal("locationLng", Double.class);
        destinationLat = jsData.getVal("destinationLat", Double.class);
        destinationLng = jsData.getVal("destinationLng", Double.class);
        isListModel =  jsData.getVal("listmodel", Boolean.class);
        boolean isBus = false;
        if (!TextUtils.isEmpty(business) && business.equals("business")) {
            isBus = true;
        }

        mDatas = new ArrayList<PoiItem>();
        JSONArray obJsonArray = jsData.getVal("pois", JSONArray.class);
        if (obJsonArray != null) {
            for (int i = 0; i < mTitleInfo.count; i++) {
                try {
                    JSONObject jo = obJsonArray.getJSONObject(i);
                    String objJson = jo.toString();
                    int poitype = jo.optInt("poitype");
                    Poi poi = null;
                    switch (poitype) {
                        case Poi.POI_TYPE_BUSINESS:
                            poi = BusinessPoiDetail.fromString(objJson);
                            break;

                        case Poi.POI_TYPE_TXZ:
                            poi = TxzPoi.fromString(objJson);
                            break;

                        case Poi.POI_TYPE_POIDEATAIL:
                            poi = PoiDetail.fromString(objJson);
                            break;
                    }
                    poi.setAction(mTitleInfo.action);

                    PoiItem poiItem = new PoiItem();
                    poiItem.isBus = isBus;
                    poiItem.item = poi;
                    mDatas.add(poiItem);
                } catch (JSONException e) {
                }
            }
        }
    }

    public class PoiItem {
        public boolean isBus;
        public Poi item;
    }
}
