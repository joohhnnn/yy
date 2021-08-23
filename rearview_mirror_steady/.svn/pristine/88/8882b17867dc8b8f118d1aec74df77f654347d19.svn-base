package com.txznet.txz.module.news;

import java.util.LinkedList;
import java.util.List;

import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.news.SimpleNewsData;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.news.NewsTipsView.INewsWinEvent;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class NewsManager extends IModule {
	private static NewsManager sInstance = new NewsManager();
	private final static int MSG_START = 1;
	private final static int MSG_STOP = 2;//外部终止
	private final static int MSG_NEXT = 3;
	private final static int MSG_FINISH = 4;//声控终止
	private final static int MSG_NOTIFY_AUDIO_FOCUS_CHANGE = 5;//通知音频焦点发生变化
	private final static int ENQUIRE_TIMEOUT = 6000;
	
	private final static int CMD_TYPE_DEFAULT= 0;//CMD默认类型
	private final static int CMD_FINISH_TYPE_CTRL= 1;//声控"停止播报"终止
	private final static int CMD_FINISH_TYPE_INTERRUPT = 2;//中途声控"取消"终止
	private final static int CMD_FINISH_TYPE_COMPLETE = 5;//列表播完终止
	private final static int CMD_NEXT_TYPE_AUTO= 3;//顺序播放
	private final static int CMD_NEXT_TYPE_VOICE = 4;//声控下一条
	private final static int CMD_NEXT_TYPE_OK = 6;//声控确定
	
	private final static int MAX_NEWS_COUNT = 10;//最大新闻条数;测试时限制最多5则新闻，正式发布之前需要去掉该限制
	private final static int INTERRUPT_ID = MAX_NEWS_COUNT + 100;//不需要提示//6;//测试的时候填3，发布之前需要改为6
    private final static String NEWS_CTRL_TASK_ID = "new_ctrl";
    private final static String NEWS_INTERRUPT_TASK_ID = "new_interrupt";
    private final static long AUTO_NEXT_PERIOD = 1000;//自动切换的时间间隔，单位毫秒
    private final static long PLAY_BUFFER_TIME = 1000;//
    
    private Runnable oEnquireTimeOutCheckRun = null;
    
	public static class NewsTask{
		public boolean wait = false;//该任务是否在等待确认
		public long id;
		public String content;
		public String audioUrl;
		public String type;
	}
	
	private static enum ProcessStatus{
		STATUS_BUFFERING,
		STATUS_PROCCESSING,
		STATUS_END
	}
	private Handler mWorkHandler = null;
	private HandlerThread mWorkThread = null;
	private ProcessStatus mProcessStatus = ProcessStatus.STATUS_END;
	private List<NewsTask> mNewsQueue = new LinkedList<NewsTask>();
	private NewsTask mCurrNewsTask = null;
	private IMutilPlayer mMutilPlayer = new SimpleMutilPlayer();
	private IMutilPlayer.IPlayCallBack mPlayCallBack = new IMutilPlayer.IPlayCallBack() {
		
		@Override
		public void onPause() {
			
		}
		
		@Override
		public void onEnd() {
			next(CMD_NEXT_TYPE_AUTO);
		}
		
		@Override
		public void onBegin() {
			
		}
	};
	
	private NewsManager(){
	}
	
	public static NewsManager getInstance() {
		return sInstance;
	}
	
	@Override
	public int initialize_AfterStartJni() {
		regEvent(UiEvent.EVENT_NEWS, SimpleNewsData.SUBEVENT_NEWS_QUERY);
		return super.initialize_AfterStartJni();
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (UiEvent.EVENT_NEWS == eventId) {//TXZ后台新闻语义处理
			switch (subEventId) {
			case SimpleNewsData.SUBEVENT_NEWS_QUERY:
				final List<NewsTask> queue = parseNewsData(data);
				if (!queue.isEmpty()) {
					String strTts =  NativeData.getResString("RS_VOICE_NEWS_PLAY_BEGIN");
					AsrManager.getInstance().setNeedCloseRecord(true);
					mProcessStatus = ProcessStatus.STATUS_BUFFERING;
					RecorderWin.speakTextWithClose(strTts, new Runnable() {
						
						@Override
						public void run() {
							Message msg = Message.obtain();
							msg.what = MSG_START;
							msg.obj = queue;
							sendMsg(msg, PLAY_BUFFER_TIME);//延时1000ms，解决关闭界面后，被其他音乐播放器抢焦点的问题
							
						}
					});
				}else{
					//提示没有获取到对应的新闻数据
					String sTts = NativeData.getResString("RS_VOICE_NEWS_NO_DATA");
					RecorderWin.speakTextWithClose(sTts, true, null);
				}
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	private void sendMsg(Message msg, long delayMillis){
		if (mWorkHandler == null){
			synchronized (this) {
				if (mWorkHandler == null){
					oEnquireTimeOutCheckRun = new Runnable() {
						@Override
						public void run() {
							final NewsTask task = mCurrNewsTask;
							if (task.wait){
								finish(CMD_FINISH_TYPE_INTERRUPT);
							}
						}
					};
					
					mWorkThread = new HandlerThread("news_worker");
					mWorkThread.start();
					mWorkHandler = new Handler(mWorkThread.getLooper()){
						@Override
						public void handleMessage(Message msg) {
							handleNewsMsg(msg);
						}
					};
				}
			}
		}
		
		if (mWorkHandler != null){
			mWorkHandler.sendMessageDelayed(msg, delayMillis);
		}
	}
	
	private void removeMsg(int what){
		if (mWorkHandler != null){
			mWorkHandler.removeMessages(what);
		}
	}
	
	private void handleNewsMsg(Message msg){
		switch (msg.what) {
		case MSG_START:
			try {
				@SuppressWarnings("unchecked")
				List<NewsTask> queue = (List<NewsTask>) msg.obj;
				handleMsgStart(queue);
			} catch (Exception e) {
				mProcessStatus = ProcessStatus.STATUS_END;
			}
			break;
		case MSG_STOP:
			handleMsgStop();
			break;
		case MSG_NEXT:
			handleMsgNext(msg.arg1);
			break;
		case MSG_FINISH:
			handleMsgFinish(msg.arg1);
			break;
		case MSG_NOTIFY_AUDIO_FOCUS_CHANGE:
			handleMsgAudioFocusChange(msg.arg1);
			break;
		default:
			break;
		}
	}
	
	private List<NewsTask> parseNewsData(byte[] data){
		List<NewsTask> queue = new LinkedList<NewsTask>();
		SimpleNewsData.NewsInfos info = null;
		try {
			info = SimpleNewsData.NewsInfos.parseFrom(data);
		} catch (Exception e) {

		}
		
		do {
			if (info == null) {
				break;
			}
			final SimpleNewsData.NewsData[] newsData= info.rptMsgNewsList;
			if (newsData == null){
				break;
			}
			
			for (int i = 0; i < newsData.length && i < MAX_NEWS_COUNT; ++i){//测试时限制最多5则新闻，正式发布之前需要去掉该限制
				SimpleNewsData.NewsData news = newsData[i];
				if (news == null){
					continue;
				}
				//添加有效的news到播报列表
				do{
					NewsTask task = new NewsTask();
					task.content = bytes2String(news.strContent);
					//新闻正文不能为空
					if (TextUtils.isEmpty(task.content)){
						break;
					}
					task.id = i+1;
					task.audioUrl = bytes2String(news.strVoiceUrl);
					task.type = bytes2String(news.strVoiceUrl);
					queue.add(task);
				}while(false);
			}
			
		} while (false);
		
		return queue;
	}

	
	private void process(int type){
		//被取消了
		if (mProcessStatus == ProcessStatus.STATUS_END){
			return;
		}
		
		if (mNewsQueue.isEmpty()){
			onFinish(CMD_FINISH_TYPE_COMPLETE);
			return;
		}
		
		mCurrNewsTask = mNewsQueue.remove(0);
		if (mCurrNewsTask != null){
			//
			if (!mCurrNewsTask.wait && mCurrNewsTask.id == INTERRUPT_ID){
				mCurrNewsTask.wait = true;
				mNewsQueue.add(0, mCurrNewsTask);//需要重新放回去，不然该任务会被跳过
				enquire();
				return;
			}
			mCurrNewsTask.wait = false;
			//主动stop上一次的任务,并且不会有onEnd回调
			mMutilPlayer.stop();
			
			IMutilPlayer.Model model = new IMutilPlayer.Model(); 
			//此处决定播放策略
			model.type = IMutilPlayer.Model.TYPE_TEXT_TTS;
			String header = "";
			if (mCurrNewsTask.id != 1){//第一条和声控直接喊下一条时，不播报提示
				header = NativeData.getResString("RS_VOICE_NEWS_HEADER_TIP").replace("%ID%", "" + mCurrNewsTask.id);
			}
			model.text = String.format("%s,%s", header, mCurrNewsTask.content);//中间添加几个逗号是为了停顿
			model.url = mCurrNewsTask.audioUrl;
			mMutilPlayer.play(model, mPlayCallBack);
		}
	}
	
	private void beginCtrlCmdTask(){
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback(){

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return NEWS_CTRL_TASK_ID;
			}
			@Override
			public void onCommandSelected(String type, String command) {
				if (TextUtils.equals(type, "CMD_NEXT")){
					next(CMD_NEXT_TYPE_VOICE);
				}else if (TextUtils.equals(type, "CMD_STOP")){
					finish(CMD_FINISH_TYPE_CTRL);
				}
				//endCtrlCmdTask();
				cancelEnquireTimeOutCheck();
			}
			
		};
		acsc.addCommand("CMD_NEXT", new String[]{"下一条"});
		acsc.addCommand("CMD_STOP", new String[]{"停止播放"});
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}
	
	private void endCtrlCmdTask(){
		WakeupManager.getInstance().recoverWakeupFromAsr(NEWS_CTRL_TASK_ID);
	}
	
	private void beginInterruptCmdTask(){
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback(){

			@Override
			public boolean needAsrState() {
				return true;//需要屏蔽掉其他声音,有六秒超时，所以不怕不会恢复
			}

			@Override
			public String getTaskId() {
				return NEWS_INTERRUPT_TASK_ID;
			}
			@Override
			public void onCommandSelected(String type, String command) {
				if (TextUtils.equals(type, "CMD_OK")){
					next(CMD_NEXT_TYPE_OK);
				}else if (TextUtils.equals(type, "CMD_CANCEL")){
					finish(CMD_FINISH_TYPE_INTERRUPT);
				}
				endInterruptCmdTask();
				cancelEnquireTimeOutCheck();
			}
			
		};
		acsc.addCommand("CMD_OK", new String[]{"需要", "继续", "确定"});
		acsc.addCommand("CMD_CANCEL", new String[]{"取消"});
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}
	
	private void endInterruptCmdTask(){
		WakeupManager.getInstance().recoverWakeupFromAsr(NEWS_INTERRUPT_TASK_ID);
	}
	
	private void  enquire(){
		String strTts = NativeData.getResString("RS_VOICE_NEWS_ENQUIRE");
		beginInterruptCmdTask();
	    TtsManager.getInstance().speakText(strTts, new ITtsCallback() {
			@Override
			public void onEnd() {
				beginEnquireTimeOutCheck(ENQUIRE_TIMEOUT);
			}
		});
	}
	
	private void beginEnquireTimeOutCheck(long delay){
		if (oEnquireTimeOutCheckRun != null){
			AppLogic.runOnBackGround(oEnquireTimeOutCheckRun, delay);
		}
	}
	
	private void cancelEnquireTimeOutCheck(){
		if (oEnquireTimeOutCheckRun != null){
			AppLogic.removeBackGroundCallback(oEnquireTimeOutCheckRun);
		}
	}
	
	public void stop() {
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING) {
			Message msg = Message.obtain();
			msg.what = MSG_STOP;
			sendMsg(msg, 0);
		}
	}
	
	public void onAudioFocusChange(int focusChange) {
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING) {
			Message msg = Message.obtain();
			msg.what = MSG_NOTIFY_AUDIO_FOCUS_CHANGE;
			msg.arg1 = focusChange;
			sendMsg(msg, 0);
		}
	}
	
	private void handleMsgStart(List<NewsTask> queue){
		//正常情况下声控页面打开状态下,直接停止该次新闻播报任务。
		if(RecorderWin.isOpened()){
			mProcessStatus = ProcessStatus.STATUS_END;
			return;
		}
		beginCtrlCmdTask();
		mProcessStatus = ProcessStatus.STATUS_PROCCESSING;
		//在插词过程中若打开声控，则需要重新处理放弃当前任务的操作，因为偶现插词时间过长的情况，会导致在插词过程中打开声控。
		if(RecorderWin.isOpened()){
			NewsManager.getInstance().stop();
			return;
		}
		mNewsQueue = queue;
		show();
		process(CMD_TYPE_DEFAULT);
	}
	
	private void handleMsgNext(int type){
		mCurrNewsTask = null;
		process(type);
	}
	
	private void handleMsgStop(){	 
		end();
	}
	
	private void handleMsgFinish(int type){	 
		onFinish(type);
	}
	
	private void handleMsgAudioFocusChange(int focusChange){
		if (mProcessStatus != ProcessStatus.STATUS_PROCCESSING){
			return;
		}
		
		JNIHelper.logd("News_foucsChange:" + focusChange);
		switch(focusChange){
		case AudioManager.AUDIOFOCUS_LOSS://长时间失去焦点,停止播报
			stop();
			break;
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:			
			break;
		case AudioManager.AUDIOFOCUS_GAIN:
		case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
		case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK://可以降低音量混播, 此处不处理,因为TTS不支持
			break;
		}
		
	}
	
	private void show(){
		JSONBuilder builder = new JSONBuilder();
		builder.put("type", 7);//必要字段，不能和其他的类型冲突
		builder.put("tipTag", "您可以说");//提示标签
		builder.put("tipText", "停止播放;下一条");//提示的具体内容,";"作为分隔符
		//WinManager.getInstance().getAdapter().addData(builder.toString());
		NewsTipsView.showView(builder.toString(), new INewsWinEvent() {
			
			@Override
			public void onShow() {
				
			}
			
			@Override
			public void onDismiss() {
				stop();
			}
		});
	}
	
	private void end(){
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING){
			mProcessStatus = ProcessStatus.STATUS_END;
			endLogic();
			endUI();
		}
	}
	
	private void endLogic(){
		mNewsQueue.clear();
		mMutilPlayer.stop();
		endCtrlCmdTask();
		endInterruptCmdTask();
		cancelEnquireTimeOutCheck();
	}
	
	private void endUI(){
		NewsTipsView.dismissView();
	}
	
	private void onFinish(int type){
		mProcessStatus = ProcessStatus.STATUS_END;
		endLogic();
		switch(type){
		case CMD_FINISH_TYPE_COMPLETE:{
			String strTts =  NativeData.getResString("RS_VOICE_NEWS_PLAY_FINISH");
			TtsManager.getInstance().speakText(strTts, new ITtsCallback() {
				@Override
				public void onEnd() {
					endUI();
				}
			});
			break;
		}
		case CMD_FINISH_TYPE_CTRL:
		case CMD_FINISH_TYPE_INTERRUPT:
		default:
			endUI();
		}
	}
	
	private void finish(int type){
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING) {
			Message msg = Message.obtain();
			msg.what = MSG_FINISH;
			msg.arg1 = type;
			sendMsg(msg, 0);
		}
	}
	
	private void next(int type){
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING) {
			removeMsg(MSG_NEXT);//解决切换期间，说“下一条”时，多跳过1次
			Message msg = Message.obtain();
			msg.what = MSG_NEXT;
			msg.arg1 = type;
			long delay = 0;
			do {
				if (type != CMD_NEXT_TYPE_AUTO){
					break;
				}
				//即将执行询问操作时,不应该延时执行下一个步骤
				final NewsTask task = mCurrNewsTask;
				if (task == null){
					break;
				}
				if (!task.wait && task.id == INTERRUPT_ID - 1){
					break;
				}
				//播放队列结束,不应该延时执行下一个步骤
				final List<NewsTask> queue = mNewsQueue;
				if (queue == null){
					break;
				}
				if (queue.isEmpty()){
					break;
				}
				delay = AUTO_NEXT_PERIOD;
			} while (false);
			sendMsg(msg, delay);
		}
	}
	
	private String bytes2String(byte[] data){
		String s = null;
		do {
			if (data == null){
				break;
			}
			
			try {
				s = new String(data);
			} catch (Exception e) {

			}
		} while (false);
		
		return s;
	}
	
	
	public byte[] processInvoke(final String packageName, String command,
			byte[] data) {
		if ("stop".equals(command)) {
			stop();
			return null;
		}
		
		return null;

	}

	public boolean isPlaying() {
		if (mProcessStatus == ProcessStatus.STATUS_PROCCESSING) {
			return true;
		}
		return false;
	}

	public boolean isBuffering() {
		if (mProcessStatus == ProcessStatus.STATUS_BUFFERING) {
			return true;
		}
		return false;
	}
	
}