package com.txznet.txz.module.resource;

import android.content.IntentFilter;
import android.util.Log;

import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.util.PreferenceUtil;

public class ResourceManager extends IModule {
	static ResourceManager sModuleInstance = new ResourceManager();

	private ResourceManager() {

	}

	public static ResourceManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_UPDATED_STYLE);
		
		// 动态注册dialog广播接收器
		IntentFilter dialogFilter = new IntentFilter();
		DialogActionReceiver dialogReceiver = new DialogActionReceiver();
		dialogFilter.addAction("com.txznet.txz.dialog.confirmAsr");
		dialogFilter.setPriority(Integer.MAX_VALUE);
		GlobalContext.get().registerReceiver(dialogReceiver, dialogFilter);
		
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件

		// 设置对应的语音风格
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SET_STYLE,
				PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_VOICE_STYLE, ""));


		return super.initialize_AfterStartJni();
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_VOICE: {
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_UPDATED_STYLE: {
				JNIHelper.logd("updated style: " + new String(data));
				PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_VOICE_STYLE, new String(data));
				ServiceManager.getInstance().broadInvoke("userconfig.onChangeCommunicationStyle", data);
				break;
			}
			}
			break;
		}
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public byte[] invokeTXZResource(final String packageName, String command, byte[] data) {
		if (command.equals("updateResource")) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_UPDATE_RESOURCE, data);
			return null;
		}
		if (command.equals("updateResourceFile")) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_UPDATE_RESOURCE_BY_PATH, data);
			return null;
		}
		if (command.equals("setStyle")) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SET_STYLE, data);
			return null;
		}
		if (command.equals("replaceResource")) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_REPLACE_RESOURCE, data);
			return null;
		}
		if (command.equals("replaceResourceFile")) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_REPLACE_RESOURCE_BY_PATH, data);
			return null;
		}
		return null;
	}

	public void setTmpStyle(String style) {
		if (style == null) {
			NativeData.getNativeData(UiData.DATA_ID_RESET_RES_STR_STYLE);
		} else {
			NativeData.getNativeData(UiData.DATA_ID_SET_RES_STR_STYLE, style);
		}
	}
}
