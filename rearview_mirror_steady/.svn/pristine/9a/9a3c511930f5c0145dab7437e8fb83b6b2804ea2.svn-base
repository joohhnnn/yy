package com.txznet.txz.util.recordcenter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

import android.os.Environment;
import android.text.TextUtils;

import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.CipherUtil;
import com.txznet.txz.util.MD5Util;

/**
 * 录音文件<br>
 * 
 * 1、头部包含信息<br>
 * 'R' + 'F' + 4个字节的version + 8个字节的crc + 8个字节"00000000" + 4个字节的PB的长度 + 4个字节的文件总长度 = 30
 * 
 * 2、以下是数据段 以pb的形式实现<br>
 * 录音时间：后台时间(如果没有，取客户端时间)+开机经过的时间<br>
 * 是否可信：是否拿到过后台时间<br>
 * 录音类型：识别、唤醒词、唤醒指令、点击、后台下发关键字<br>
 * 录音参数：采样率、位率<br>
 * 信道类型：原始信号、参考、aec、inner<br>
 * 录音结果：识别是识别的语义json，唤醒词是唤醒词、唤醒指令是唤醒指令、点击是当前所有唤醒词、关键字即命中关键字<br>
 *
 * 3、录音数据加密：使用客户端key+后台key构成 在线解密
 *
 */
public class RecordFile {
	 public final static int SAMPLE_RATE_16K = 16000;
	 public final static int SAMPLE_RATE_22K = 22050;
	 public final static int SAMPLE_RATE_32K = 32000;
	
	public static final String SUFFIX_PCM = ".pcm";
	public static final String SUFFIX_RF = ".rf";
	
	public static final String TAG = "RecordFile:";
	
	private static String encryptKey = "";//加密key组成
	static final int RF_HEAD_LENGTH = 30;//RF文件头部的长度
	
	
	private File mRawFile;//保存的文件
	private RandomAccessFile mFile;//保存的文件，可随机读写
	private int mVersion;//文件版本号
	private long mCrc;//CRC校验
	private UiRecord.RecordData mRecordData;//数据段
	private int mRecordDataSize = 0;
	private int mTotalSize = 0;

	public static String mDefinitVoiceName;
	public static boolean ENABLE_TEST_DEFINIT_VOICE_NAME = new File(getDebugRoot(),
			"test_definit_voice_name.debug").exists();

	public static File getDebugRoot() {
		try {
			return new File(Environment.getExternalStorageDirectory(), "txz");
		} catch (Exception e) {
			return new File(".");
		}
	}
	/**
	 * 创建一个RF文件
	 * @param f
	 * @param mRecordData
	 * @return
	 */
	public static RecordFile createFile(File f, RecordData mRecordData){
		if(f ==null || mRecordData == null){
			return null;
		}
		if(TextUtils.isEmpty(encryptKey)){
			LogUtil.loge(TAG + "key is null");
			return null;
		}
		RecordFile mRecordFile = new RecordFile();
		byte[] mRecordDataBuffer = RecordData.toByteArray(mRecordData);
		mRecordFile.mVersion = 1;
		mRecordFile.mCrc = 0L;//需要根据音频算出来
		mRecordFile.mRecordData = mRecordData;
		mRecordFile.mRecordDataSize = mRecordDataBuffer.length;
		mRecordFile.mRawFile = f;
		try {
			mRecordFile.mFile = new RandomAccessFile(f, "rw");
			mRecordFile.mFile.writeByte('R');
			mRecordFile.mFile.writeByte('F');
			mRecordFile.mFile.writeInt(mRecordFile.mVersion);
			mRecordFile.mFile.writeLong(mRecordFile.mCrc);
			mRecordFile.mFile.writeBytes("00000000");
			mRecordFile.mFile.writeInt(mRecordFile.mRecordDataSize);
			mRecordFile.mFile.writeInt(mRecordFile.mTotalSize);
			mRecordFile.mFile.write(mRecordDataBuffer);
		} catch (IOException e) {
			return null;
		}finally{
			if(mRecordFile.mFile != null){
				try {
					mRecordFile.mFile.close();
				} catch (IOException e) {
				}
			}
		}
		return mRecordFile;
	}
	
	public static RecordFile createFile(File f, RecordData mRecordData, File rawFile){
		RecordFile rf = createFile(f, mRecordData);
		if(rf != null){
			rf.completeRecordFile(rawFile);
		}
		return rf;
	}
	
	public static void setEncryptKey(String key){
		if(!TextUtils.isEmpty(key)){
			encryptKey = key;
		}
	}
	public static String getEncryptKey(){
		return encryptKey;
	}
	
	/**
	 * 打开一个RF文件，文件存在则对文件进行校验，校验成功后返回当前对象,失败返回null<br>
	 * 文件不存在则创建一个文件
	 * 
	 * @param f
	 * @return
	 */
	public static RecordFile openFile(File f){ 
		RecordFile mRecordFile = new RecordFile();
		try {
			if (f != null && f.exists()) {
				mRecordFile.mRawFile = f;
				mRecordFile.mFile = new RandomAccessFile(mRecordFile.mRawFile,"rw");
				byte R = mRecordFile.mFile.readByte();
				byte F = mRecordFile.mFile.readByte();
				if(R != 'R' || F != 'F'){//文件头不匹配
					return null;
				}
				mRecordFile.mVersion = mRecordFile.mFile.readInt();
				mRecordFile.mCrc = mRecordFile.mFile.readLong();
				mRecordFile.mFile.skipBytes(8);//跳过KEY
				mRecordFile.mRecordDataSize = mRecordFile.mFile.readInt();
				mRecordFile.mTotalSize = mRecordFile.mFile.readInt();
				byte[] mRecordDataBuffer = new byte[mRecordFile.mRecordDataSize];
				if(mRecordFile.mFile.read(mRecordDataBuffer) == -1){
					return null;//文件不完整
				}
				mRecordFile.mFile.close();
				mRecordFile.mRecordData = RecordData.parseFrom(mRecordDataBuffer);
				LogUtil.logd(TAG + "open name = "+mRecordFile.mRawFile.getName()+" ,Version = "+mRecordFile.mVersion+" ,Crc = "+mRecordFile.mCrc
						+" ,RecorderDataSize = "+mRecordFile.mRecordDataSize+" ,TotalSize = "+mRecordFile.mTotalSize);
				return mRecordFile;
			} else {
//				return createFile(f, new RecordData());
				return null;
			}
		} catch (IOException e) {
			LogUtil.logw(e.getMessage());
			return null;
		}finally{
			if(mRecordFile.mFile != null){
				try {
					mRecordFile.mFile.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	
	/**
	 * 补全RF文件，计算出录音的crc，进行加密，存储进RF文件，然后删除本地音频文件
	 */
	public void completeRecordFile(File file){
		try {
			mFile = new RandomAccessFile(mRawFile, "rw");
			InputStream fis = new FileInputStream(file);
			int voiceSize = (int) file.length();
			byte[] mBuffer = new byte[(int) file.length()];
			int offset = 0;
			int len = 0;
			while ((fis.read(mBuffer, offset, voiceSize-offset)) > 0 && offset < voiceSize) {
				offset += len;
			}
			fis.close();
			CRC32 mCrc32 = new CRC32();
			mCrc32.update(mBuffer);
			mCrc = mCrc32.getValue();
			String key = MD5Util.generateMD5(mRecordData.uint32Uid+""+mCrc+encryptKey);
			byte[] mEncryptedBuffer = CipherUtil.enCrypt(key, mBuffer);
			mFile.seek(6);
			mFile.writeLong(mCrc);
			mFile.seek(RF_HEAD_LENGTH+mRecordDataSize);
			mFile.write(mEncryptedBuffer);
			mTotalSize = (int) mFile.length();
			mFile.seek(26);
			mFile.writeInt(mTotalSize);
			LogUtil.logd("RecordFile completeRecordFile name = "+mRawFile.getName()+" ,CRC = "+mCrc+" ,TotalSize = "+mTotalSize);
		} catch (IOException e) {
		}finally{
			if(mFile != null){
				try {
					mFile.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	
	/**
	 * 获得原生的音频文件
	 */
	public void getRawVoiceFile(String path){
		try {
			File file = new File(path);
			OutputStream os = new FileOutputStream(file);
			int voiceSize = mTotalSize-RF_HEAD_LENGTH-mRecordDataSize;
			FileInputStream fis = new FileInputStream(mRawFile);
			FileOutputStream fos = new FileOutputStream("/mnt/sdcard/txz/voice/true.pcm");
			byte[] bytes = new byte[voiceSize];
			int len = 0;
			int offset = 0;
			fis.skip(RF_HEAD_LENGTH + mRecordDataSize);
			while(offset < voiceSize && (len = fis.read(bytes,offset,voiceSize-offset)) > 0){
				os.write(bytes, offset, len);
				offset += len;
			}
			byte[] voiceByte = CipherUtil.deCrypt(MD5Util.generateMD5(mRecordData.uint32Uid+mCrc+encryptKey), bytes);
			fos.write(voiceByte);
			fis.close();
			os.close();
			fos.close();
		} catch (Exception e) {
			LogUtil.logw("RecordFile getRawVoiceFile = "+e.getMessage());
		}
		
	}
	
	/**
	 * 检查文件中的内容
	 * 测试用
	 */
	public static void checkInfo(String path){
		RecordFile rf = openFile(new File(path));
		try {
			rf.mFile = new RandomAccessFile(rf.mRawFile, "rw");
			byte R = rf.mFile.readByte();
			byte F = rf.mFile.readByte();
			int mVersion = rf.mFile.readInt();
			long mCrc = rf.mFile.readLong();
			byte[] bytes = new byte[8];
			rf.mFile.read(bytes);
			String key = new String(bytes);
			int mRecordDataSize = rf.mFile.readInt();
			int totalSize = rf.mFile.readInt();
			LogUtil.logd("RecordFile checkInfo R = "+R+" ,F = "+F+" ,version = "+mVersion+" ,mCrc = "+mCrc+" ,key = "+key+" ,RecordDataSize = "+mRecordDataSize+" ,totalSize = "+totalSize);
			RecordData mRecordData = rf.mRecordData;
			LogUtil.logd("RecordFile checkInfo RecordData uint64RecordTime = "+mRecordData.uint64RecordTime+" ,boolRecordTime = "+mRecordData.boolRecordTime
					+" ,uint32RecordType = "+mRecordData.uint32RecordType+" ,uint32SampleRate = "+mRecordData.uint32SampleRate
					+" ,uint32SignalType = "+mRecordData.uint32SignalType+" ,bytesRecordResult = "+new String(mRecordData.bytesRecordResult));
			rf.getRawVoiceFile("/mnt/sdcard/txz/voice/true.pcm");
		} catch (Exception e) {
			LogUtil.loge("checkInfo = "+e.getMessage());
		}
	}
	
	/**
	 * 设置录音的结果
	 * @param result
	 */
	public void updateRecordResult(String result) {
		if (result != null) {
			try {
				//先获取出录音数据
				FileInputStream fis = new FileInputStream(mRawFile);
				int voiceSize = mTotalSize-RF_HEAD_LENGTH-mRecordDataSize;
				byte[] bytes = new byte[voiceSize];
				int len = 0;
				int offset = 0;
				fis.skip(RF_HEAD_LENGTH + mRecordDataSize);
				while(offset < voiceSize && (len = fis.read(bytes,offset,voiceSize-offset)) > 0){
					offset += len;
				}
				fis.close();
				//对文件进行修改
				mFile = new RandomAccessFile(mRawFile, "rw");
				mRecordData.bytesRecordResult = result.getBytes();
				byte[] recordDataBytes = RecordData.toByteArray(mRecordData);
				mRecordDataSize = recordDataBytes.length;
				mTotalSize = RF_HEAD_LENGTH + recordDataBytes.length + voiceSize;
				//修改PB大小
				mFile.seek(22);
				mFile.writeInt(mRecordDataSize);
				mFile.writeInt(mTotalSize);
				mFile.write(recordDataBytes);
				mFile.write(bytes);
			} catch (Exception e) {
			}finally{
				if(mFile != null){
					try {
						mFile.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
	}
	
	
}
