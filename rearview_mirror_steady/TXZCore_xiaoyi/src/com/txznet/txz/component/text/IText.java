package com.txznet.txz.component.text;

public interface IText {
	public static final int NetError = -1;
	public static final int ParseError = -2;
	public static final int InterruptedError = -3;
	public static final int NetTimeOutError = -4;
	public static final int UnkownError = -5;

	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}
   
	public static enum PreemptLevel{
		PREEMPT_LEVEL_NONE,
		PREEMPT_LEVEL_IMMEDIATELY, 
		PREEMPT_LEVEL_NEXT
	}
	
	public static abstract class ITextCallBack {
		public void onResult(String jsonResult) {
		}

		public void onError(int errorCode) {
		}
	}

	public int initialize(final IInitCallback oRun);

	public int setText(String text, ITextCallBack callBack);

	public void cancel();

	public void release();

}
