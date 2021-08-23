package com.txznet.txz.module.wakeup;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

import android.os.Environment;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;

public class WakeupPcmHelper {
	static final int PCM_QUEUE_LEN = 5*16000*16/8; //保留5秒的录音
	static byte[] sPcmDatas = new byte[PCM_QUEUE_LEN];
	static int sPcmDatasLen = 0;
	static int sPcmDatasPos = 0; //循环写的起始位置
	static boolean sEnableVoiceChannel = true;
	
	public static void savePcm(String result, int append) {
		LogUtil.logd("save wakeup pcm data size=" + sPcmDatasLen);
		if (!ProjectCfg.mSaveEngineData){
			return;
		}
		if (sPcmDatasLen <= 0)return;
		long id = System.currentTimeMillis();
		try {
			File f = new File( Environment.getExternalStorageDirectory(), "txz/voice/");
			f.mkdirs();
			FileOutputStream out = new FileOutputStream(new File(f, "txz_" + id + ".pcm"));
			synchronized (sPcmDatas) {
				out.write(sPcmDatas, sPcmDatasPos, sPcmDatasLen-sPcmDatasPos);
				if (sPcmDatasPos > 0) {
					out.write(sPcmDatas, 0, sPcmDatasPos);
					sPcmDatasPos = 0;
				}
				sPcmDatasLen = 0;
			}
			byte[] mutePcm = new byte[]{0, 0};
			if (ProjectCfg.mEnableAEC) {
				append += 10; //回音消除的是立体声录音，多加20个字节
			}
			while (append-- > 0) {
				out.write(mutePcm);
			}
			out.close();
//			this is for upload user's action.this is moves to AsrUtil.java now.
//			VoiceData.VoiceParseData data = new VoiceData.VoiceParseData();
//			data.strVoiceData = result;
//			data.uint64VoiceFileId = id;
//			data.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_WAKEUP;
//			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_PARSE, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void pushPcm(byte[] data, int offset, int len) {
		synchronized (sPcmDatas) {
			if (sPcmDatasLen == PCM_QUEUE_LEN) {
				if (PCM_QUEUE_LEN-sPcmDatasPos > len) {
					System.arraycopy(data, offset, sPcmDatas, sPcmDatasPos, len);
					sPcmDatasPos += len;
				} else {
					System.arraycopy(data, offset, sPcmDatas, sPcmDatasPos, PCM_QUEUE_LEN-sPcmDatasPos);
					sPcmDatasPos = len-(PCM_QUEUE_LEN-sPcmDatasPos);
					if (sPcmDatasPos > 0) {
						System.arraycopy(data, offset, sPcmDatas, 0, sPcmDatasPos);
					}
				}
			}
			else if (sPcmDatasLen + len > PCM_QUEUE_LEN) {
				System.arraycopy(data, offset, sPcmDatas, sPcmDatasLen, PCM_QUEUE_LEN-sPcmDatasLen);
				sPcmDatasPos = len-(PCM_QUEUE_LEN-sPcmDatasLen);
				System.arraycopy(data, offset + PCM_QUEUE_LEN-sPcmDatasLen, sPcmDatas, 0, sPcmDatasPos);
				sPcmDatasLen = PCM_QUEUE_LEN;
			} else {
				System.arraycopy(data, offset, sPcmDatas, sPcmDatasLen, len);
				sPcmDatasLen += len;
			}
		}
	}
	
	public static void fillEmptyData(byte[] data, int offset, int len){
		Arrays.fill(data, offset, len, (byte)0);
	}
	
	public static void enableVoiceChannel(boolean enable){
         sEnableVoiceChannel = enable;
	}
	
	public static boolean  channelEnable(){
		return sEnableVoiceChannel;
	}
	
}
