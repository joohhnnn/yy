package com.txznet.txz.component.tts;

public interface ITts {
	public interface ITtsCallBack{
		public void onSuccess();
		public void onCancel();
		public void onError();
	}
	public interface IInitCallBack{
		public void onInit(boolean successed);
	}
    public void init(IInitCallBack cb);
    public void start(int stream, String text, ITtsCallBack cb);
    public void stop();
}
