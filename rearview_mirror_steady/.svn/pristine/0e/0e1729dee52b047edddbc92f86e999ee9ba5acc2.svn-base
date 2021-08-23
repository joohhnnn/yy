package com.txznet.txz.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

import android.content.Intent;

/**
 * 应方案商需求，在每次执行响应的操作前，发送广播。
 * 
 * @author telenewbie
 *
 */
public class SendBroadcast {

	public static final String BROAD_PLAY = "com.txznet.extra.play";//播放
	public static final String BROAD_PAUSE = "com.txznet.extra.pause";//暂停
	public static final String BROAD_NEXT = "com.txznet.extra.next";//下一首
	public static final String BROAD_PRE = "com.txznet.extra.pre";//上一首
	public static final String BROAD_EXIT = "com.txznet.extra.exit";//退出播放器
	public static final String BROAD_EXIT_IMMEDIATELY = "com.txznet.extra.exit";
	public static final String BROAD_SWITCH_MODE_LOOP_ALL="com.txznet.extra.mode.loop.all";//全部循环播放
	public static final String BROAD_SWITCH_MODE_LOOP_ONE="com.txznet.extra.mode.loop.one";//单曲循环
	public static final String BROAD_SWITCH_MODE_RANDOM = "com.txznet.extra.random";//随机播放
	public static final String BROAD_SWITCH_Song="com.txznet.extra.song";//切歌
	public static final String BROAD_PLAY_RANDOM="com.txznet.extra.play.random";//随便听听
	public static final String BROAD_PLAY_MUSIC="com.txznet.extra.play.music";//播放指定音乐
	
	private static final String TAG = "core:broadcast:";

	public static void sendplay() {
		LogUtil.logd(TAG+BROAD_PLAY);
		Intent intent = new Intent(BROAD_PLAY);
		GlobalContext.get().sendBroadcast(intent);
	}

	public static void sendPause() {
		LogUtil.logd(TAG+BROAD_PAUSE);
		Intent intent = new Intent(BROAD_PAUSE);
		GlobalContext.get().sendBroadcast(intent);
	}

	public static void sendNext() {
		LogUtil.logd(TAG+BROAD_NEXT);
		Intent intent = new Intent(BROAD_NEXT);
		GlobalContext.get().sendBroadcast(intent);
	}

	public static void sendPre() {
		LogUtil.logd(TAG+BROAD_PRE);
		Intent intent = new Intent(BROAD_PRE);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendExit(){
		LogUtil.logd(TAG+BROAD_EXIT);
		Intent intent = new Intent(BROAD_EXIT);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendExitImmediately(){
		LogUtil.logd(TAG+BROAD_EXIT_IMMEDIATELY);
		Intent intent = new Intent(BROAD_EXIT_IMMEDIATELY);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendSwitchModeLoopAll(){
		LogUtil.logd(TAG+BROAD_SWITCH_MODE_LOOP_ALL);
		Intent intent = new Intent(BROAD_SWITCH_MODE_LOOP_ALL);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendSwitchModeLoopOne(){
		LogUtil.logd(TAG+BROAD_SWITCH_MODE_LOOP_ONE);
		Intent intent = new Intent(BROAD_SWITCH_MODE_LOOP_ONE);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendSwitchModeRandom(){
		LogUtil.logd(TAG+BROAD_SWITCH_MODE_RANDOM);
		Intent intent = new Intent(BROAD_SWITCH_MODE_RANDOM);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendSwitchSong(){
		LogUtil.logd(TAG+BROAD_SWITCH_Song);
		Intent intent = new Intent(BROAD_SWITCH_Song);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendPlayRandom(){
		LogUtil.logd(TAG+BROAD_PLAY_RANDOM);
		Intent intent = new Intent(BROAD_PLAY_RANDOM);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	public static void sendPlayMusic(){
		LogUtil.logd(TAG+BROAD_PLAY_MUSIC);
		Intent intent = new Intent(BROAD_PLAY_MUSIC);
		GlobalContext.get().sendBroadcast(intent);
	}
}
