package com.txznet.txz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.txznet.txz.util.recordcenter.TXZAudioTrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

/*
 * note:该实现类只支持单任务。即一次只能播放一个文件。
 * 1、Wav文件或者PCM文件后端最好有一段静音，否则容易产生破音。 
 * 2、执行一个播放任务的流程： setDataSource -> prepare -> start。
 *      调用处必须捕获setDataSource可能抛出来的异常。发生异常，
 *      后面的流程执行无效果，并且不会有播放完成的回调。
 *      播放结束或者停止播放后，必须按照上述流程重新执行，才能播放。
 * 3、播放结束或者播放停止后，setDataSource和prepare才有效。
 * 4、release是一个阻塞接口，阻塞到本次播放任务完全退出。
 *      原因：保证用户调用了stop之后可以马上正常调用setDataSource->prepare->start。
 *      否则，出现由于上次播放任务尚未完全退出、导致下次播放任务启动无效。
 * 5、为避免重复创建AudioTrack，本实现类只有AudioTrack为空或者参数发生变化时，
 *     才重新创建AudioTrack。
 *  6、本实现类支持播放wav或则PCM文件，同时支持直接播放压缩包中的PCM和Wav文件。
 */

public class AudioTrackPlayer {
	 public final static int SAMPLE_RATE_8K = 8000;
	 public final static int SAMPLE_RATE_16K = 16000;
	 public final static int SAMPLE_RATE_32K = 32000;
	 public final static int SAMPLE_RATE_44K = 44100;
	 public final static int SAMPLE_RATE_48K = 48000;
	 public final static int WAV_HEADER_LEN = 44;
	 
	 private final static int BUFFER_SIZE = 1024;
	 private byte[] buffer = null;
     private int mStreamType = AudioManager.STREAM_ALARM;
     private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
     private int mSampleRateInHz = SAMPLE_RATE_16K;
     private int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
     private int  mMode = TXZAudioTrack.MODE_STREAM;
     private PlayState mPlayStated = PlayState.STATE_IDEL;
     
     private String mAudioSourcePath = null;
     private OnCompletionListener mOnCompletionListener = null;
     private Decryption mDecryption = null;
     private InputStream mIn = null;
     private Handler mHandler = null;
     private HandlerThread mPlayThread = null;
     private Handler mPlayHandler = null;
     private TXZAudioTrack mAudioTrack = null;
     private AudioTrackPlayer mInstance = null;
 	 private ZipFile mZipFile = null;
 	 private boolean mEnablePlay = false;
 	 
     private static enum PlayState{
    	 STATE_PLAYING, STATE_START,STATE_PAUSE, STATE_STOPED, STATE_IDEL
     }
     
     public interface OnCompletionListener{
         public void onCompletion(AudioTrackPlayer player);
     }
     
     public interface Decryption{
    	 //解密后的数据仍然存放在data数组中。即data既是输入参数又是输出参数。
    	 //offsetInFile表示data缓存区中的第一个字节在文件中的偏移位置
    	 public void decrypt(byte[] data, int offset, int size, long offsetInFile);
     }
     
     public AudioTrackPlayer(){
    	 this(AudioManager.STREAM_ALARM);
     }
     
     public AudioTrackPlayer(int streamType){
    	 this(streamType, SAMPLE_RATE_16K, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
     }
     
     public AudioTrackPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat){
    	 mStreamType = streamType;
    	 mSampleRateInHz = sampleRateInHz;
    	 mChannelConfig = channelConfig;
    	 mAudioFormat = audioFormat;
    	 mHandler = new Handler(Looper.getMainLooper());
    	 mPlayThread = new HandlerThread("pcm_play_thread");
    	 mPlayThread.start();
    	 mPlayHandler = new Handler(mPlayThread.getLooper());
    	 mPlayHandler.post(new Runnable() {
			@Override
			public void run() {
				TXZHandler.updateMaxPriority();
			}
		 });
    	 buffer = new byte[BUFFER_SIZE];
    	 mInstance = this;
     }
     
     public void setOnCompletionListener(OnCompletionListener oOnCompletionListener){
    	 mOnCompletionListener = oOnCompletionListener;
     }
     
     public void setDecryption(Decryption oDecryption){
    	 mDecryption = oDecryption;
     }
     
     public void setAudioStreamType(int streamType){
    	 mStreamType = streamType;
     }
     
     public void setParams(int sampleRateInHz, int channelConfig, int audioFormat){
    	 mSampleRateInHz = sampleRateInHz;
    	 mChannelConfig = channelConfig;
    	 mAudioFormat = audioFormat;
     }
     
     public synchronized void setDataSource(String path) throws Exception{
		if (mPlayStated == PlayState.STATE_IDEL) {
			mAudioSourcePath = path;
			if (!TextUtils.isEmpty(mAudioSourcePath)){
				if (mIn != null) {
					try {
						mIn.close();
					} catch (Exception e) {

					}
					mIn = null;
				}
				mIn = new FileInputStream(mAudioSourcePath);
			}
		}
     }
     
	public synchronized void setDataSource(String zipFilePath, String unzipName)throws Exception {
		if (mPlayStated == PlayState.STATE_IDEL) {
			mAudioSourcePath = zipFilePath + "://" + unzipName;
			// 释放上次没有释放的资源
			if (mZipFile != null) {
				try {
					mZipFile.close();
				} catch (Exception e) {
				}
				mZipFile = null;
			}

			File file = new File(zipFilePath);// 压缩文件路径
			mZipFile = new ZipFile(file);
			ZipEntry entry = mZipFile.getEntry(unzipName);
			InputStream in = null;
			if (entry != null) {
				try {
					in = mZipFile.getInputStream(entry);
				} catch (Exception e) {
				}
			}
			
			//获取解压文件输入流失败，则释放资源
			if (in == null) {
				if (mZipFile != null) {
					try {
						mZipFile.close();
					} catch (Exception e) {
					}
					mZipFile = null;
				}
				throw new Exception("fail to get message for " + unzipName + " from " + zipFilePath);
			}
			
			//MP3实时流
			if (mAudioSourcePath.endsWith(".mp3")) {
				try {
					in = new TXZMP3Stream(in, mDecryption);
				} catch (Exception e) {
					in.close();
					mZipFile.close();
					mZipFile = null;
					throw new Exception("fail to get mp3 stream for " + unzipName + " from " + zipFilePath);
				}
			}
			
			mIn = in;
			return;
		}
		//
		throw new Exception("last play task is busy...");
	}
     
	public synchronized void prepare() {
		if (mPlayStated == PlayState.STATE_IDEL) {
			//AudioTrack参数发生变化则另外创建一个AudioTrack，同时release上一次申请的资源
			if (mAudioTrack != null && !compareParams(mStreamType, mSampleRateInHz, mChannelConfig, mAudioFormat, mAudioTrack)){
				if (mAudioTrack.getPlayState() == TXZAudioTrack.PLAYSTATE_PLAYING) {
					mAudioTrack.stop();
				}
				mAudioTrack.flush();
				mAudioTrack.release();
				mAudioTrack = null;
			}
			
			if (mAudioTrack == null){
				mAudioTrack = createAudioTrack(mStreamType, mSampleRateInHz, mChannelConfig, mAudioFormat);
			}
		}
	}
     
	public synchronized void start() {
		if (mPlayStated == PlayState.STATE_IDEL) {
			mPlayStated = PlayState.STATE_START;
			mEnablePlay = true;
			if (mAudioTrack != null) {
				try {
					mAudioTrack.play();
				} catch (Exception e) {

				}
			}
			mPlayHandler.postDelayed(oPlayRun, 0);
			//mPlayHandler.postDelayed(oPlayRun, 500);//测试release接口使用
		}
	}
     
     public synchronized void pause(){
    	 if (mPlayStated == PlayState.STATE_PLAYING) {
 		}
     }
     
	public synchronized void stop() {
		if (mPlayStated == PlayState.STATE_START || mPlayStated == PlayState.STATE_PLAYING) {
			mEnablePlay = false;
			
			//关闭输入流
			if (mIn != null){
				try{
					mIn.close();
				}catch(Exception e){
					
				}
			}
			//中断AudioTrack的write操作
			if (mAudioTrack != null) {
				try {
					if (mAudioTrack.getPlayState() == TXZAudioTrack.PLAYSTATE_PLAYING) {
						mAudioTrack.stop();
					}
				} catch (Exception e) {

				}
			}
			//等待本次播放任务执行完成
			synchronized (oPlayRun) {
				while (mPlayStated != PlayState.STATE_IDEL) {
					try {
						oPlayRun.wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}
	}
	
    public boolean isPlaying(){ 
    	return mPlayStated == PlayState.STATE_START || mPlayStated == PlayState.STATE_PLAYING;
    }
    
     public synchronized void release(){
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == TXZAudioTrack.PLAYSTATE_PLAYING) {
				mAudioTrack.stop();
			}
			mAudioTrack.flush();
			mAudioTrack.release();
			mAudioTrack = null;
		}
     }
    
    private Runnable  oPlayRun = new Runnable() {
		@Override
		public void run() {
			playAsync();
		}
	};
	
	private TXZAudioTrack createAudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat) {
		TXZAudioTrack audioTrack = null;
		int bufferSizeInBytes = TXZAudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
		int buffersize = sampleRateInHz * 2 / 10; // 100ms单声道16位音频需要多少字节
		if (bufferSizeInBytes <= buffersize) {
			bufferSizeInBytes = bufferSizeInBytes * 2;
		} else if (bufferSizeInBytes <= buffersize * 2) {
			bufferSizeInBytes = buffersize * 2;
		}
		try {
			audioTrack = TXZAudioTrack.createAudioTrack(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mMode);
		} catch (Exception e) {
			
		}
		return audioTrack;
	}
	
	private boolean compareParams(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, TXZAudioTrack audioTrack) {
		boolean bRet = false;
		if (audioTrack != null) {
			bRet = audioTrack.getStreamType() == streamType
					&& audioTrack.getSampleRate() == sampleRateInHz
					&& audioTrack.getChannelConfiguration() == channelConfig
					&& audioTrack.getAudioFormat() == audioFormat;
		}
		return bRet;
	}
	
	private void reset() {
		// close input stream
		if (mIn != null) {
			try {
				mIn.close();
			} catch (Exception e) {
			}
			mIn = null;
		}

		// close zip file if use zip
		if (mZipFile != null) {
			try {
				mZipFile.close();
			} catch (Exception e) {
			}
			mZipFile = null;
		}

		// release audiotrack
		if (mAudioTrack != null) {
			if (mAudioTrack.getPlayState() == TXZAudioTrack.PLAYSTATE_PLAYING) {
					mAudioTrack.stop();
			}
			mAudioTrack.flush();
		}
	}
	
	private void playAsync() {
		synchronized (oPlayRun) {
			mPlayStated = PlayState.STATE_PLAYING;
			// play begin
			if (mAudioTrack != null && mIn != null && mEnablePlay) {
				try {
					long offsetInFile = 0;
					// 跳过wav文件的文件头44个字节
					if (mAudioSourcePath.endsWith(".wav")) {
						mIn.skip(WAV_HEADER_LEN);
						offsetInFile += WAV_HEADER_LEN;
					}
					while (mEnablePlay) {
						int read = 0;
						int quest = 0;
						int ret = 0;
						//需要读满指定个数字节,不然可能有噪音
						while(read < buffer.length){
							quest = buffer.length - read;
							ret = mIn.read(buffer, read, quest);
							if (ret < 0) {
								break; 
							}
							read += ret;
						}
						if (read > 0) {
							if (!mAudioSourcePath.endsWith(".mp3")) {
								decrypt(buffer, 0, read, offsetInFile);
								offsetInFile += read;
							}
							mAudioTrack.write(buffer, 0, read);
						}
						//已经到文件末尾了
						if (ret < 0){
							break;
						}
					}
				} catch (Exception e) {
				}
			}
			reset();
			// play end
			mPlayStated = PlayState.STATE_IDEL;
			try {
				//Thread.sleep(1000);//测试release接口使用
				oPlayRun.notifyAll();
			} catch (Exception e) {

			}
			onCompletion();
		}
	}
	
	private void onCompletion(){
		if (mOnCompletionListener != null){
			final OnCompletionListener oOnCompletionListener = mOnCompletionListener;
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					oOnCompletionListener.onCompletion(mInstance);
				}
			};
			mHandler.post(oRun);
		}
	}
	
	private void decrypt(byte[] data, int offset, int size, long offsetInFile){
		if (mDecryption != null){
			mDecryption.decrypt(data, offset, size, offsetInFile);
		}
	}
}
