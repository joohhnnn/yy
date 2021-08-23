package com.txznet.txz.module;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.module.nav.NavManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class SampleManager extends IModule {
	static SampleManager sModuleInstance = new SampleManager();

	private SampleManager() {

	}

	public static SampleManager getInstance() {
		return sModuleInstance;
	}

	///////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		initRecv();
		return super.initialize_AfterInitSuccess();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		return super.onEvent(eventId, subEventId, data);
	}

	private void initRecv() {
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// 微信分享POI
				Poi poi = new Poi();
				poi.setLat(22.568745);
				poi.setLng(113.684512);
				poi.setGeoinfo("深圳南山区中科大厦");
				NavManager.getInstance().sharePoiToWx(poi);
//				String type = intent.getStringExtra("TYPE");
//				String tool = intent.getStringExtra("TOOL");
//				if ("baidu".equals(type)) {
//					if ("PoiSearchToolBaiduImpl".equals(tool)) {
//					} else if ("PoiSearchToolBaiduLocalImpl".equals(tool)) {
//
//					} else if ("PoiSearchToolBaiduWebImpl".equals(tool)) {
//
//					}
//				} else if ("dzdp".equals(type)) {
//				} else if ("gaode".equals(type)) {
//					if ("PoiSearchToolGaodeImpl".equals(type)) {
//
//					} else if ("PoiSearchToolGaodeOnWay".equals(tool)) {
//
//					} else if ("PoiSearchToolGaodeWebImpl".equals(tool)) {
//
//					} else if ("PoiSearchToolGDLocalImpl".equals(tool)) {
//
//					}
//				} else if ("mx".equals(type)) {
//
//				} else if ("qihoo".equals(type)) {
//
//				} else if ("txz".equals(type)) {
//
//				}
			}
		}, new IntentFilter("TEST_POI_SEARCH"));
	}
}
