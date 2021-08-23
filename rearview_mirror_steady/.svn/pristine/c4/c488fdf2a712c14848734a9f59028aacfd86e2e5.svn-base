package com.txznet.audio.player;

interface IAidlPlayer {
    void createPlayer(int sid, in Map infos);
    String getPath(int sid);
    void prepareAsync(int sid);
    void start(int sid);
    void stop(int sid);
    void pause(int sid);
	boolean isPlaying(int sid);
	void seekTo(int sid, long msec);
	long getCurrentPosition(int sid);
    long getDuration(int sid);
    void release(int sid);
    void reset(int sid);
    void setVolume(int sid, float leftVol, float rightVol);
    void setStreamType(int sid, int streamtype);
    void syncConfig(int sid, in Map infos);
    void destroy(int sid);
}