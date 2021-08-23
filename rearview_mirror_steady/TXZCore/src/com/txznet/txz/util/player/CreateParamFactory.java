package com.txznet.txz.util.player;

import android.media.AudioFormat;

/**
 * 设置参数（抽象工厂模式）
 * 
 * @author lenovo
 *
 */
public class CreateParamFactory {

	/**
	 * 根据类型创建声道
	 * 
	 * @param type
	 * @return
	 */
	public static int getChannel(int type) {
		int channel;
//		if (Constant.ISTEST) {
//			return AudioFormat.CHANNEL_OUT_MONO;
//		}

		if (type == 1) {// 单声道
			channel = AudioFormat.CHANNEL_OUT_MONO;
		} else {// 立体声,其他的按立体声处理
			channel = AudioFormat.CHANNEL_OUT_STEREO;
		}
		return channel;
	}

	/**
	 * 根据类型创建不同的分辨率
	 * 
	 * @param type
	 *            字节数(>=16位的都为2字节)
	 * @return
	 */
	public static int getAudioFormat(int type) {
		int format;

//		if (Constant.ISTEST) {
//			format = AudioFormat.ENCODING_PCM_16BIT;
//			return format;
//		}

		if (type == 1) {
			format = AudioFormat.ENCODING_PCM_8BIT;
		} else {// 其他的按16bit分辨率处理
			format = AudioFormat.ENCODING_PCM_16BIT;
		}
		return format;
	}
}
