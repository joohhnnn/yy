package com.txznet.fm.bean;

import java.util.List;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.fm.bean.interfase.IMessage;
import com.txznet.music.bean.response.Audio;

/**
 * 观察的消息传递类
 * 
 * @author ASUS User
 *
 */
public class InfoMessage implements IMessage {

	// 扫描完成
	public static final int SCAN_FINISHED = 0;
	// 扫描开始
	public static final int SCAN_STATED = 1;

	//播放状态
	public static final int PLAY = 2;

	//暂停状态
	public static final int PAUSE = 3;

	// 播放指定歌曲
	public static final int PLAYCHOICE = 4;

	// 更新缓冲进度条
	public static final int UPDATE_BUFFER = 5;
	// 播放完成
	public static final int PLAY_FINISHED = 6;

	// 播放发生错误
	public static final int PLAY_ERROR = 7;
	// 播放进度条
	public static final int PLAY_PROGRESS = 8;
	// 删除本地音乐
	public static final int DELETE_LOCAL_MUSIC = 9;

	// 获取全部的分类
	public static final int REQ_CATEGORY_ALL = 10;

	// 获取单个分类的数据
	public static final int REQ_CATEGORY_SINGLE = 11;
	//Album的相应
	public static final int RESP_ALBUM = 12;
	
	//网络不通
	public static final int NET_ERROR= 13;
	//网络超时
	public static final int NET_TIMEOUT_ERROR= 14;
	//通知本地音乐位置
	public static final int NOTIFY_LOCAL_AUDIO= 15;
	

	//释放掉资源
	public static final int RELEASE = 16;
	//刷新本地播放位置
	public static final int REFRESH_LOCAL_POSITION = 17;
	//播放列表中没有歌曲
	public static final int PLAYER_NO_SONGS=18;
	//当前播放的歌曲
	public static final int PLAYER_CURRENT_AUDIO=19;
	//缓冲新的音频列表
	public static final int PLAYER_LOADING=20;

	public static final int ADD_VIEW_LIST = 21;
	public static final int DELETE_VIEW_LIST = 22;
	public static final int ADD_HISTORY_VIEW_LIST = 23;
	public static final int DELETE_HISTORY_VIEW_LIST = 24;
	public static final int SET_CURRENT_VIEW = 25;
	
	// 当前消息的类型
	private int type;

	private Audio mAudio;

	// 缓冲进度
	private List<LocalBuffer> buffers;

	// 存放的内容
	private Object obj;

	private int errCode;
	private String errMessage;

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

	public List<LocalBuffer> getBuffers() {
		return buffers;
	}

	public void setBuffers(List<LocalBuffer> buffers) {
		this.buffers = buffers;
	}

	public InfoMessage() {

	}

	public InfoMessage(int type) {
		super();
		this.type = type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int getType() {
		return type;
	}

	public Audio getmAudio() {
		return mAudio;
	}

	public void setmAudio(Audio mAudio) {
		this.mAudio = mAudio;
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

}
