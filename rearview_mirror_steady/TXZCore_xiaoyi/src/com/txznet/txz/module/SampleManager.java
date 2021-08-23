package com.txznet.txz.module;

import com.txznet.txz.module.IModule;

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
		//注册需要处理的事件
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		//发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		//处理事件
		return super.onEvent(eventId, subEventId, data);
	}

}
