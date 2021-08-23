package com.txznet.txz.module.wakeup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.os.Environment;

import com.txz.ui.data.UiData.TTime;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderBase;
import com.txznet.txz.util.recordcenter.cache.DataWriter;
import com.txznet.txz.util.runnables.Runnable3;

public class WakeupPcmHelper {
	static boolean sEnableVoiceChannel = true;
	static int sSaveTime = 3;//唤醒等的保存时长
	
	public static void savePcm(String result, int type, String taskId) {
		if(!ProjectCfg.enableSaveVoice()){
			LogUtil.logd("enable save voice false");
			return;
		}
		AppLogic.runOnSlowGround(new Runnable3<String, Integer, String>(result, type, taskId) {

					@Override
					public void run() {
						String voiceType = null;
						String rawFileName = "."+android.os.Process.myPid()+mP3;
						switch (mP2) {
						case UiRecord.RECORD_TYPE_CLICK:
							voiceType = "txz_click_";
							if (ProjectCfg.enableSaveRawPCM()) {
								rawFileName = voiceType + mP3 + RecordFile.SUFFIX_PCM;
							}
							break;
						case UiRecord.RECORD_TYPE_TRIGGER_KW:
							voiceType = "txz_tag_";
							break;
						case UiRecord.RECORD_TYPE_WAKEUP_CMD:
							voiceType = "txz_cmd_";
							if (ProjectCfg.enableSaveRawPCM()) {
								rawFileName = voiceType + mP3 + RecordFile.SUFFIX_PCM;
							}
							break;
						default:
							voiceType = "txz_wakeup_";
							if (ProjectCfg.enableSaveRawPCM()) {
								rawFileName = voiceType + mP3 + RecordFile.SUFFIX_PCM;
							}
							break;
						}
						File f = new File(Environment.getExternalStorageDirectory(),
								"txz/voice/");
						f.mkdirs();
						File tmpFile = new File(f, rawFileName);
						try {
							final FileOutputStream out = new FileOutputStream(tmpFile);
							TXZSourceRecorderBase.saveLastCacheBuffer(new DataWriter() {
								@Override
								public int  writeData(byte[] data, int offset, int len) {
									try {
										out.write(data, offset, len);
									} catch (IOException e) {
									}
									return len;
								}
							}, 64 * 1000 * sSaveTime);
							
							out.close();
							createRecordFile(ProjectCfg.AUDIO_SAVE_PATH + "/" + voiceType + mP3 + RecordFile.SUFFIX_RF,
									mP1, tmpFile, mP2);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if(tmpFile != null){
								if(tmpFile.getName().startsWith(".")) {
									tmpFile.delete();
								}
							}
						}

					}
				}, 0);

	}
	
	private static void createRecordFile(String path, String result, File rawFile, int type) {
		RecordData mRecordData = new RecordData();
		TTime tTime = NativeData.getMilleServerTime();
		mRecordData.boolRecordTime = tTime.boolConfidence;
		mRecordData.uint64RecordTime = tTime.uint64Time;
		mRecordData.bytesRecordResult = result.getBytes();
		mRecordData.uint32RecordType = type;
		mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_32K;
		mRecordData.uint32Uid = NativeData.getUID();
		mRecordData.uint32SignalType = ProjectCfg.mEnableAEC ? UiRecord.SIGNAL_TYPE_AEC:UiRecord.SIGNAL_TYPE_RAW;
		mRecordData.uint32FilterNoiseType = ProjectCfg.getFilterNoiseType();
		RecordFile.createFile(new File(path), mRecordData, rawFile);
	}

	public static void pushPcm(byte[] data, int offset, int len) {
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
	
	/**
	 * 设置唤醒等的录音时间
	 * 单位：秒
	 * @param second
	 */
	public static void setRecordTime(int second){
		if(second>=1 && second <= 5){
			sSaveTime = second;
		}
	}
	
}
