package com.txznet.txz.component.wakeup.mix;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.wakeup.mix.CmdCompileTask.TaskType;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
 
public class CmdCompiler implements ICmdCompiler{
	private SpeechUnderstander mEngine = null;
	private enum CompilerInitStatus{
		INIT_BEGIN, INIT_END, INIT_IDEL
	}
	
	private CompilerInitStatus mCompilerInitStatus = CompilerInitStatus.INIT_IDEL;
	private CmdCompileTask mCurrentCompileTask = null;
	private List<CmdCompileTask> mWaitCompileTasks = new LinkedList<CmdCompileTask>();
	private final static int COMPILE_MAX_PRONUNCIATION = 6 * 6 * 4 * 4 * 2 * 2; 
	private boolean mIsJsgfLoaded = false;
	private boolean mHasAsrCmdTask = false;
	private static final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock(false);
	private Handler mWorkHandler = null;
	private HandlerThread mWorkThread = null;
	private final int MSG_COMPILER_COMPILE = 2;
	private final int MSG_COMPILER_INIT = 1;
	private final int MSG_COMPILER_RELEASE = 0;
	
	public CmdCompiler(SpeechUnderstander engine){
		mEngine = engine;
		mWorkThread = new HandlerThread("txzCmdCompiler");
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper()){
			@Override
			public void handleMessage(Message msg) {
				compilerMsgQueue(msg.what);
			}
		};
	}
	
	public TaskType getCurrTaskType(){
		final CmdCompileTask task = mCurrentCompileTask;
		return task != null ? task.getTaskType() : null;
	}
	
	public void lock(){
		mReadWriteLock.writeLock().lock();
	}
	
	public void unlock(){
		mReadWriteLock.writeLock().unlock();
	}
	
	public void onCompileDone() {
		lock();
		if (mCurrentCompileTask != null) {
			mCurrentCompileTask.savePreBuild();
			mCurrentCompileTask = null;
			execCompileTask();
		}
		unlock();
	}
	
	public void onInitDone() {
		lock();
		mIsJsgfLoaded = false;
		mCompilerInitStatus = CompilerInitStatus.INIT_END;
		execCompileTask();
		unlock();
	}
	
	public void onDestoryDone(){
		mIsJsgfLoaded = false;
	}
	
	public boolean isCurrCompileTask(CmdCompileTask task){
		boolean bRet = false;
		final CmdCompileTask currTask = mCurrentCompileTask;
		bRet = currTask != null ? currTask.equals(task): false;
		return bRet;
	}
	
	private void compilerMsgQueue(int cmd){
		lock();
		switch(cmd){
		case MSG_COMPILER_INIT:
			if (mCompilerInitStatus == CompilerInitStatus.INIT_IDEL) {
				LogUtil.logd("Initialization  compiler begin");
				mEngine.initCompiler();// 异步接口
				LogUtil.logd("Initialization  compiler end");
				mCompilerInitStatus = CompilerInitStatus.INIT_BEGIN;
			}
			break;
		case MSG_COMPILER_RELEASE:
			if (mCurrentCompileTask != null){
				LogUtil.logd("compiler compiling wait...");
				break;
			}
			
			if (!mWaitCompileTasks.isEmpty()){
				LogUtil.logd("compiler task  wait...");
				break;
			}
			
			if (mCompilerInitStatus == CompilerInitStatus.INIT_END){
				LogUtil.logd("Destory compiler begin");
				mEngine.destoryCompiler();// 同步接口，耗时400ms左右
				LogUtil.logd("Destory compiler end");
				mCompilerInitStatus = CompilerInitStatus.INIT_IDEL; 
				mIsJsgfLoaded = false;
				mHasAsrCmdTask = false;
			}
			break;
		case MSG_COMPILER_COMPILE:
			if (mCurrentCompileTask != null){
				LogUtil.logd("compiler compiling wait...");
				break;
			}
			if (mCompilerInitStatus != CompilerInitStatus.INIT_END){
				if (mCompilerInitStatus == CompilerInitStatus.INIT_IDEL){
					Message msg = Message.obtain();
					msg.what = MSG_COMPILER_INIT;
					mWorkHandler.sendMessage(msg);
				}
				break;
			}
			
			if (mWaitCompileTasks.isEmpty()){
				long delay = mHasAsrCmdTask ? 5000 : 0;
				Message msg = Message.obtain();
				msg.what = MSG_COMPILER_RELEASE;
				mWorkHandler.sendMessageDelayed(msg, delay);
				break;
			}
			
			mWorkHandler.removeMessages(MSG_COMPILER_RELEASE);
			mCurrentCompileTask = mWaitCompileTasks.remove(0);
			mCurrentCompileTask.compile(this);
			break;
		}
		unlock();
	}
	
	private void execCompileTask() {
		Message msg = Message.obtain();
		msg.what = MSG_COMPILER_COMPILE;
		mWorkHandler.sendMessage(msg);
	}
	
	public void addCompileTask(CmdCompileTask oTask){
		lock();
		addTask(oTask);
		unlock();
	}
	
	private void addTask(CmdCompileTask oTask){
		if (oTask == null){
			return;
		}
		
		if (oTask.checkPreBuild()){
			LogUtil.logw("keywords compiled");
			return;
		}
		
		final CmdCompileTask currTask = mCurrentCompileTask;
		if (currTask != null) {
			if (currTask.equals(oTask)) {
				LogUtil.logw("The task is compiling");
				return;
			}
		}
		
		for (int i = 0; i < mWaitCompileTasks.size(); i++) {
			if (oTask.equals(mWaitCompileTasks.get(i))) {
				LogUtil.logw("The task has already existed");
				return;
			}
		}
		
		mWaitCompileTasks.add(oTask);
		
		if (oTask.getTaskType() == TaskType.TYPE_ASR){
			mHasAsrCmdTask = true;
		}
		
		execCompileTask();
	}
	
	public boolean isEmpty(){
		return mWaitCompileTasks.isEmpty();
	}

	
	@Override
	public boolean compileWakeupKws(List<String> kws) {
		LogUtil.logd("compileWakeupKws");
		mEngine.setWakeupWord(kws);
		return true;
	}

	@Override
	public boolean compileAsrKws(List<String> kws, String strSlot) {
		int ret = 0;
		if (!mIsJsgfLoaded){
			mEngine.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir + "/data/txz.dat");// 加载编译语法
			mEngine.setOption(SpeechConstants.ASR_OPT_SET_COMPILE_MAX_PRONUNCIATION, COMPILE_MAX_PRONUNCIATION);// 设置最大发音长度
			mIsJsgfLoaded = true;
		}
		LogUtil.logd("compileAsrKws:" + strSlot);
		ret = mEngine.insertVocab(kws, strSlot);
		return ret == 0;
	}
	
}
