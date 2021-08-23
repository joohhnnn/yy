package com.txznet.txz.module.text;

import java.util.LinkedList;
import java.util.List;

import com.txznet.txz.component.text.IText;
import com.txznet.txz.component.text.IText.IInitCallback;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.component.text.IText.PreemptLevel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

public class TextManager extends IModule{
  private static TextManager sModuleInstance = new TextManager();
  private IText mText = null;
  private boolean mInited = false;
  
  private TextManager(){
	   mInited = false;
  }
  
  public static TextManager getInstance(){
	  return sModuleInstance;
  }
  
  public void initializeComponent() {
	  if (mText != null){
		  return;
	  }
	  
		try {
			mText = (IText) Class.forName("com.txznet.txz.component.text.yunzhisheng_3_0.TextYunzhishengImpl").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		mText.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				JNIHelper.logd("text init:" + bSuccess);
	            mInited = bSuccess;
			}
		}); 
  }
  
  public void parseText(String text, ITextCallBack callBack){
	  if (mText == null || !mInited){
		  return;
	  }
	  ParserTask task = new ParserTask();
	  task.text = text;
	  task.callBack = callBack;
	  task.preemptLevel = PreemptLevel.PREEMPT_LEVEL_NONE;
	  insertText(task);
	  //mText.setText(text, callBack);
  }
  
  public void parseText(String text, ITextCallBack callBack, PreemptLevel level){
	  if (mText == null || !mInited){
		  return;
	  }
	  ParserTask task = new ParserTask();
	  task.text = text;
	  task.callBack = callBack;
	  task.preemptLevel = level;
	  insertText(task);
  }
  
  public void cancel(){
		if (mText == null || !mInited) {
			return;
		}
		synchronized (this) {
			if (mCurrTask != null) {
				mText.cancel();
			}
		}
  }
  
	private synchronized void insertText(ParserTask task) {
		if (task.preemptLevel == PreemptLevel.PREEMPT_LEVEL_NONE){
			mParserQueue.add(task);
		}else{
			mParserQueue.add(0, task);
		}
		
		//紧急任务先处理
		if (task.preemptLevel == PreemptLevel.PREEMPT_LEVEL_IMMEDIATELY){
			//当前有任务, 直接取消
			if (mCurrTask != null){
				JNIHelper.logd("cancel immeadiately");
				mText.cancel();
				return;
			}
		}
		
		if (mCurrTask == null) {
			parseNext();
		}
	}

  private void parseNext(){
	  if(mParserQueue.isEmpty()){
		  return;
	  }
	  
	  mCurrTask = mParserQueue.get(0);
	  mParserQueue.remove(0);
	  JNIHelper.logd("parseText text = " + mCurrTask.text);
	  mText.setText(mCurrTask.text, mSysCallBack);
  }
  
  private synchronized void doResult(String jsonResult){
	  if (null != mCurrTask || null != mCurrTask.callBack){
		  mCurrTask.callBack.onResult(jsonResult);
	  }
	  mCurrTask = null;
	  parseNext();
  }
  
  private synchronized void doError(int errorCode){
	  if (null != mCurrTask || null != mCurrTask.callBack){
		  mCurrTask.callBack.onError(errorCode);
	  }
	  mCurrTask = null;
	  parseNext();
  }
  
  private class ParserTask{
	  String text;
	  ITextCallBack callBack;
	  PreemptLevel preemptLevel;
  }
  
  private List<ParserTask> mParserQueue = new LinkedList<ParserTask>();
  private ParserTask mCurrTask = null;
  private ITextCallBack mSysCallBack = new ITextCallBack() { 
		public void onResult(String jsonResult) {
               doResult(jsonResult);
		}

		public void onError(int errorCode) {
               doError(errorCode);
		}
    };
}
