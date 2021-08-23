package com.txznet.feedback.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.util.LogUtil;

import android.text.TextUtils;

public class QBean {
	public static final int TYPE_INVALI = -1;
	public static final int TYPE_ONLY = 0;
	public static final int TYPE_MULTI = 1;
	
	public int type = TYPE_INVALI;
	public String description;
	public String answer;
	public List<QBean> mQBeans = new ArrayList<QBean>();
	
	public static QBean parseJson(String json){
		if(TextUtils.isEmpty(json)){
			return null;
		}
		
		QBean qBean = new QBean();
		try {
			JSONObject jo = new JSONObject(json);
			qBean.description = (String) jo.get("description");
			try {
				JSONArray ja = jo.getJSONArray("answer");
				if(ja != null){
					qBean.type = TYPE_MULTI;
					for(int i = 0;i<ja.length();i++){
						JSONObject object = (JSONObject) ja.get(i);
						QBean bean = parseJson(object.toString());
						qBean.mQBeans.add(bean);
					}
				}
			} catch (Exception e) {
				LogUtil.logd("问答");
				qBean.type = TYPE_ONLY;
				qBean.answer = jo.getString("answer");
			}
		} catch (JSONException e) {
			LogUtil.loge(e.toString());
			return null;
		}
		return qBean;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append("description:").append(description);
		sb.append(",answer:");
		if(type == TYPE_ONLY){
			sb.append(answer);
			return sb.toString();
		}
		
		for(QBean bean:mQBeans){
			sb.append("{");
			sb.append("description:");
			sb.append(bean.description);
			sb.append(",");
			sb.append("answer");
			sb.append(bean.answer);
			sb.append("},\n");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}