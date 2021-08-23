package com.txznet.feedback.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.TextUtils;
import android.widget.Toast;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.RecorderUtil;
import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.StatusUtil.GetStatusCallback;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.data.Message;
import com.txznet.feedback.ui.WinRecord;
import com.txznet.feedback.util.FileUtil;
import com.txznet.feedback.volley.ResourceModule;
import com.txznet.feedback.volley.ResourceModule.DownloadCallback;
import com.txznet.feedback.volley.ResourceModule.UploadCallback;
import com.txznet.txz.util.runnables.Runnable2;

/**
 * 录音模块：（完成录音、保存到本地路径和数据库、提交网络）
 * 1、录音环境监测，开始录音，将录音文件保存到指定的文件夹中
 * 2、将生成的文件夹名称和相关参数保存到本地数据库中
 * 3、向服务器提交录音的数据信息
 * 
 * 数据库中的数据不一定与实际音频同时存在，音频存在 数据库则一定存在
 * 
 * 消息模块：
 * 1、网络上获取官方消息，将获取下来的数据插入到数据库中
 * 2、从数据库中读取数据，并按照时间的先后排序展示到页面上
 * 3、点击音频聊天内容，取得File文件判断本地是否存在，存在则开始调用播放，不存在则按照Item的属性从网络上下载，并指定好名称
 *
 * 上传网络携带的数据：
 * 1、当前设备的ID，用于唯一识别该设备的所有问题及反馈
 * 2、数据流文件
 * 3、数据流对应的ID
 * 
 */
public class RecordService {

	public static final String SAVE_RECORD_PATH = FileUtil.getSdCardDir() + "/txz/feedback/cache/record";

	private MediaPlayer mMediaPlayer;
	private OnMediaPlayListener mListener;

	// SD卡中存在的录音文件
	private List<File> mAllFiles = new ArrayList<File>();
	// 真实存在录音文件的名称
	private List<String> fileNameList = new ArrayList<String>();

	private static RecordService instance = new RecordService();

	private RecordService() {
	}

	public static RecordService getInstance() {
		return instance;
	}
	
	private String currentRecordName;

	/**
	 * 开始录音
	 */
	public void record() {
		currentRecordName = SAVE_RECORD_PATH + "/" + System.currentTimeMillis();
		
		File file = new File(currentRecordName).getParentFile();
		if(!file.exists()){
			file.mkdirs();
		}

		StatusUtil.getStatus(new GetStatusCallback() {

			@Override
			public void onGet() {
				if (isBusyCall) {
					TtsUtil.speakText("电话中无法使用录音");
					return;
				}

				if (isBusyAsr) {
					TtsUtil.speakText("录音设备使用中，请稍后");
					return;
				}
				
				RecorderUtil.start(mRecordCallback, new RecordOption()
						.setEncodeMp3(true)
						.setSkipMute(false)
						.setMaxMute(5000)
						.setMaxSpeech(60000)
						.setSavePathPrefix(currentRecordName));
			}
		});
	}

	private RecordCallback mRecordCallback = new RecordCallback() {

		@Override
		public void onVolume(int arg0) {
			LogUtil.logi("RecordCallback onVolume -- >"+arg0);
		}

		@Override
		public void onSpeechTimeout() {
			onEnd(60000);
			if(WinRecord.getInstance() != null){
				WinRecord.getInstance().finish();
			}
		}

		@Override
		public void onPCMBuffer(short[] arg0, int arg1) {
		}

		@Override
		public void onMuteTimeout() {
		}

		@Override
		public void onMute(int arg0) {
			int seconds = 5 - (arg0 / 1000);
			if(WinRecord.getInstance() != null){
				WinRecord.getInstance().refreshTimeRemain(seconds);
			}
		}

		@Override
		public void onMP3Buffer(byte[] arg0) {
		}

		@Override
		public void onError(int arg0) {
			if(WinRecord.getInstance() != null){
				WinRecord.getInstance().finish();
			}
		}

		@Override
		public void onEnd(int speechLength) {
			
			if(WinRecord.isCancel()){
				FileUtil.deleteFile(currentRecordName);
				return;
			}
			
			if((speechLength / 1000) < 1){
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(AppLogic.getApp(), "说话时间太短！", Toast.LENGTH_SHORT).show();
					}
				}, 0);
				deleteRealFile(currentRecordName + ".mp3");
				return;
			}
			
			// 录制成功，将参数保存到数据库中
			Message msg = new Message();
			
			String timeSpan = getTimeSpan(currentRecordName);
			msg.id = Long.parseLong(timeSpan) / 1000;
			msg.type = Message.TYPE_SELF;
			msg.msg = currentRecordName + ".mp3";
			msg.note = String.valueOf((speechLength / 1000) + "'");
			msg.time = msg.id;
			
			// 将数据信息插入到数据库中
			MsgService.getInstance().addMessage(msg);
			
			// 向网络保存一份数据
			AppLogic.runOnBackGround(new Runnable2<String,Long>(msg.msg,msg.id) {
				
				@Override
				public void run() {
					ResourceModule.getInstance().uploadVoice(mP1, mP2, new UploadCallback() {
						
						@Override
						public void onSuccess() {
							AppLogic.runOnUiGround(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(AppLogic.getApp(), "反馈成功！", Toast.LENGTH_SHORT).show();
								}
							}, 0);
						}
						
						@Override
						public void onError() {
							AppLogic.runOnUiGround(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(AppLogic.getApp(), "反馈失败！", Toast.LENGTH_SHORT).show();
								}
							}, 0);
						}
					});
				}
			}, 0);
		}

		@Override
		public void onCancel() {
			if(WinRecord.getInstance() != null){
				WinRecord.getInstance().finish();
			}
		}

		@Override
		public void onBegin() {
		}
	};
	
	private String getTimeSpan(String fileName){
		int start = fileName.lastIndexOf("/") + 1;
		return fileName.substring(start);
	}

	public void playVoice(String source) {
		onPause();
		
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					if(mListener != null){
						mListener.onEnd();
					}
					
					mMediaPlayer.stop();
				}
			});
		}

		File file = new File(source);
		if (!file.exists()) {
			/**
			 * 执行下载操作
			 */
			String voiceId = MsgService.getInstance().getMessageIdByFilePathName(source);
			if(TextUtils.isEmpty(voiceId)){
				LogUtil.loge("voiceId 为空，不能从服务器取回！");
				return;
			}
			ResourceModule.getInstance().downloadVoice(source, voiceId, new DownloadCallback() {
				
				@Override
				public void onSuccess(String id, String filePath) {
					Toast.makeText(AppLogic.getApp(),"语音下载成功，点击播放", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onError() {
					Toast.makeText(AppLogic.getApp(),"语音下载失败，请稍后重试", Toast.LENGTH_SHORT).show();
				}
				
				@Override
				public void onDuplicate() {
				}
			});
			Toast.makeText(AppLogic.getApp(), "正在下载，请稍后", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			mMediaPlayer.setDataSource(source);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
			Toast.makeText(AppLogic.getApp(), "文件不合法，无法播放", Toast.LENGTH_SHORT).show();
			if(mListener != null){
				mListener.onEnd();
				return;
			}
		}

		try {
			mMediaPlayer.prepare();
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}

		mMediaPlayer.start();
		if(mListener != null){
			mListener.onPlay();
		}
	}
	
	/**
	 * 删除音频文件
	 * @param fileName
	 */
	public void deleteRealFile(String fileName){
		List<File> resultFiles = new ArrayList<File>();
		int start = fileName.lastIndexOf("/") + 1;
		int end = fileName.lastIndexOf(".");
		String fileRegex = fileName.subSequence(start, end).toString();
		FileUtil.findFileByName(new File(RecordService.SAVE_RECORD_PATH),fileRegex,resultFiles);
		for(File f:resultFiles){
			if (f.exists()) {
				f.delete();
			}
		}
	}
	
	public boolean isPlaying(){
		if(mMediaPlayer == null){
			return false;
		}
		
		return mMediaPlayer.isPlaying();
	}

	public void onPause() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
			if(mListener != null){
				mListener.onEnd();
			}
		}
	}

	/**
	 * 获取目录下所有的mp3文件
	 */
	public void scanAllFiles() {
		FileUtil.findFileByName(new File(SAVE_RECORD_PATH), ".mp3", mAllFiles);
		for (File file : mAllFiles) {
			fileNameList.add(file.getName());
		}
	}
	
	/**
	 * 删除所有的音频文件
	 */
	public void deleteAllVoiceFiles(){
		for(File f:mAllFiles){
			if(f.exists()){
				f.delete();
			}
		}
	}
	
	public void setOnMediaPlayListener(OnMediaPlayListener listener){
		mListener = listener;
	}
	
	public interface OnMediaPlayListener{
		public void onPlay();
		public void onEnd();
	}
}