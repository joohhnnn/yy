package com.txznet.txz.util;

public class VoiceGainHelper {
	private static float sRate = 1.0f;
	
	private static boolean sEnable = false;
	
	public static boolean enable(){
		return sEnable;
	}
	
	public static float getRate(){
		return sRate;
	}
	
	static {
		try{
			UserVoiceConfig.init();
			if (UserVoiceConfig.sVoiceGainRate != null && UserVoiceConfig.sVoiceGainRate > 0){
				sEnable = true;
				sRate = (float)((double)UserVoiceConfig.sVoiceGainRate);
			}
		}catch(Exception e){
			
		}
	}
	
	//小端模式:低字节低地址
	public static int adjustGain(byte[] data, int offset, int len, float rate){
		int outlen = 0;
		for (int i = offset; i < len; i+=2){
			short value = (short) ((data[i + 1] << 8) & 0xff00);//&或者|等位操作会被先强转为int然后被执行位操作
			value = (short) (value | (data[i] & 0x00ff));//如果data[i]为负数的话,高位会被补1
			
			value *= rate;
			
			data[i] = (byte) (value & 0x00ff);
			data[i + 1] =(byte) (( value >> 8)&0x00ff);
			
			outlen += 2;
		}
		
		return outlen;
	}
}
