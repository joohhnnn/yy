package com.txznet.feedback.dtool;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.data.QBean;
import com.txznet.txz.util.runnables.Runnable1;

public class JsonParser {
	
	private ParserListener mParserListener;
	private static JsonParser instance = new JsonParser();
	
	private JsonParser(){ }
	
	public static JsonParser getInstance(){
		return instance;
	}
	
	public void parseJson(String input){
		AppLogic.runOnBackGround(new Runnable1<String>(input) {
			
			@Override
			public void run() {
				List<QBean> qBeans = new ArrayList<QBean>();
				try {
					JSONArray ja = new JSONArray(mP1);
					for(int i = 0;i<ja.length();i++){
						QBean bean = QBean.parseJson(ja.getString(i));
						if(bean != null){
							qBeans.add(bean);
						}
					}
				} catch (JSONException e) {
					LogUtil.loge("无法解析出JSONArray");
				}
				
				invokeParserListener(qBeans);
			}
		}, 0);
	}
	
	public void setParserListener(ParserListener listener){
		mParserListener = listener;
	}
	
	private void invokeParserListener(List<QBean> result){
		if (mParserListener != null) {
			mParserListener.onParserEnd(result);
		}
	}
	
	public interface ParserListener {
		public void onParserEnd(List<QBean> result);
	}
}
