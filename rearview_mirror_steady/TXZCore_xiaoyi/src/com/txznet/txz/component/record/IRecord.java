package com.txznet.txz.component.record;

import com.txznet.comm.remote.util.RecorderUtil.RecordCallback;
import com.txznet.comm.remote.util.RecorderUtil.RecordOption;


public interface IRecord {
	public static final  int RECORD_MUTE_VOL = 20;//低于这个值就认为录音出现静音
	public static final int NO_ERROR = 0;
	public static final int ERROR  = -1;
	public void start(RecordCallback callback);
	public void start(RecordCallback callback, RecordOption option);
	public void stop();
	public boolean isBusy();
}
