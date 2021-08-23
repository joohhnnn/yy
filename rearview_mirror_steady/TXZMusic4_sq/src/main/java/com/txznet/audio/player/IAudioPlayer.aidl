package com.txznet.audio.player;
interface IAudioPlayer{
	int getDuration(int sid);
	float getPlayPercent(int sid);
	float getBufferingPercent(int sid);
	boolean isPlaying(int sid);
	boolean isBuffering(int sid);
	boolean needMoreData(int sid);
	long getDataPieceSize(int sid);
	void setVolume(int sid, float leftVol, float rightVol);
	void prepareAsync(int sid);
	void start(int sid);
	void pause(int sid);
	void stop(int sid);
	void seekTo(int sid, float percent);
	void release(int sid);
	int createAudioPlayer(in byte[] audio,int pid,in byte[] key,int sid);
	void forceNeedMoreData(boolean isForce);
}