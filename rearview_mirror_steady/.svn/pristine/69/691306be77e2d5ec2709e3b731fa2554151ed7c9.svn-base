package com.txznet.launcher.component.nav;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/2/11.
 * 下发地址相关方法
 */

public interface PoiIssuedTool {

    interface OnPoiIssuedListener {
        void onPoiIssued(IssuedPoi item);
    }

        class IssuedPoi {
            public String source;
            public String poiName;
            public String poiAddress;
            public double lat, lng;
            public String message;

            @Override
            public String toString() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("poiName", poiName);
                    jsonObject.put("poiAddress", poiAddress);
                    jsonObject.put("lat", lat);
                    jsonObject.put("lng", lng);
                    jsonObject.put("message",message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return jsonObject.toString();
            }

            public static IssuedPoi fromString(String jsonStr) {
                try {
                    IssuedPoi poi = new IssuedPoi();
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    poi.poiName = jsonObject.optString("poiName");
                    poi.poiAddress = jsonObject.optString("poiAddress");
                    poi.lat = jsonObject.optDouble("lat");
                    poi.lng = jsonObject.optDouble("lng");
                    poi.message = jsonObject.optString("message");
                    return poi;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

    /**
     * 请求Poi通知列表
     */
    void reqPois();

    /**
     * 设置Poi目的地下发监听器
     *
     * @param listener
     */
    void setPoiIssuedListener(OnPoiIssuedListener listener);
}