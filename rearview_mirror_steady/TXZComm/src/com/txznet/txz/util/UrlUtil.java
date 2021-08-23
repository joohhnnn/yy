package com.txznet.txz.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;

public class UrlUtil {
	public static final String URL_AHEAD_SIGN = "{";

	public static class ConnectInfo {
		public String url;
		public String host;
		public Map<String, String> headers;
	}

	/**
	 * 
	 */
	public static ConnectInfo parseUrl(String orginalUrl) {
		ConnectInfo info = new ConnectInfo();
		if (!orginalUrl.startsWith(URL_AHEAD_SIGN)) {
			info.url = orginalUrl;
			return info;
		}

		try {
			JSONBuilder jBuilder = new JSONBuilder(orginalUrl);
			String url = jBuilder.getVal("url", String.class);
			info.url = url;
			String host = jBuilder.getVal("host", String.class);
			info.host = host;
			JSONObject header = jBuilder.getVal("headers", JSONObject.class);
			Map<String, String> headMap = new HashMap<String, String>();
			Iterator<String> key = header.keys();
			while (key.hasNext()) {
				String k = key.next();
				String val = (String) header.opt(k);
				headMap.put(k, val);
			}
			info.headers = headMap;
		} catch (Exception e) {
			LogUtil.logw("UrlUtil parseUrl error！");
		}

		return info;
	}
}