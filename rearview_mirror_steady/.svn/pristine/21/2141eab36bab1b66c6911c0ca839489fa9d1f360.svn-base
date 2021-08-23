package com.txznet.txz.module.news;

public interface IMutilPlayer {
	public interface IPlayCallBack{
		public void onBegin();
		public void onPause();
		public void onEnd();
	}
	
	public static class Model{
		public final static int TYPE_DEFAULT = 0;
		public final static int TYPE_TEXT_TTS = 1;
		public final static int TYPE_LOCAL_AUDIO = 2;
		public final static int TYPE_NET_AUDIO = 3;
		public int type = TYPE_DEFAULT;
		public String text;
		public String local_path;
		public String url;
	}
	
	public void play(Model model, IPlayCallBack cb);
	public void pause();
	public void stop();
	public boolean isBusy();
}
