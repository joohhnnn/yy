package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.sdk.bean.TxzPoi;

public class PoiListViewData extends ListViewData {

	public PoiListViewData() {
		super(TYPE_FULL_LIST_POI);
	}
	
	private ArrayList<Poi> poiList = new ArrayList<Poi>();
	
	public ArrayList<Poi> getData() {
		return poiList;
	}
	
	public boolean isBus;
	public int mShowCount;

	@Override
	public void parseItemData(JSONBuilder data) {
		poiList.clear();
		String action = data.getVal("action", String.class);
		String city = data.getVal("city", String.class);
		String business = data.getVal("poitype", String.class);
		mShowCount = data.getVal("showcount", Integer.class);
		isBus = false;
		if (!TextUtils.isEmpty(business) && business.equals("business")) {
			isBus = true;
		}
		JSONArray obJsonArray = data.getVal("pois", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONObject jo = obJsonArray.getJSONObject(i);
					int poitype = Poi.POI_TYPE_POIDEATAIL;
					if (jo.has("poitype")) {
						poitype = jo.optInt("poitype");
					}
					
					String objJson = jo.toString();
					Poi poi = null;
//					if (isBus) {
//						poi = BusinessPoiDetail.fromString(objJson);
//					} else {
//						poi = PoiDetail.fromString(objJson);
//					}
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
					poi.setAction(action);
					poiList.add(poi);
				} catch (JSONException e) {
				}
			}
		}
	}
}
