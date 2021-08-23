package com.txznet.record.bean;

import com.txz.ui.voice.VoiceData.WeatherInfos;

public class WeatherMessage  extends ChatMessage{

	public static final int TYPE_FROM_SYS_WEATHER = 20; // 从系统发送来的天气
	
	public WeatherMessage(int type) {
		super(type);
	}

	public WeatherInfos mWeatherInfos;
}
