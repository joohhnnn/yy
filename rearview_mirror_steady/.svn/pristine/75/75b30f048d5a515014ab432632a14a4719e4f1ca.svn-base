package com.txznet.txz.util.recordcenter;

import com.txznet.txz.util.recordcenter.cache.DataWriter;

public interface ITXZSourceRecorder {
	public final static int READER_TYPE_MIC = 1; //MIC数据信号
	public final static int READER_TYPE_REFER = 2; //回音消除参考信号
	public final static int READER_TYPE_AEC = 3; //回音消除后信号
	public final static int READER_TYPE_INNER = 4; //内部信号

	public int startRecorder(Runnable runIdle);

	public void stopRecorder();
	
	public int preStartRecorder();

	public void preStopRecorder();

	public boolean isRecording();

	public void releaseRecorder();

	// 设置数据读取器
	public void setDataWriter(int type, DataWriter writer);

	// 设置录音异常执行的逻辑
	public void setErrorRunnable(Runnable run);
	
	public boolean isLive();
	
	public void die();
	
	public void notifyError(int errorCode);
}
