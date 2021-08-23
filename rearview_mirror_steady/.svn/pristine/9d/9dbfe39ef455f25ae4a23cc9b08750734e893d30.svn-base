package com.txznet.music.bean.response;

import java.util.List;

public class ResponseSearch {

	public static final int SELECTPLAY = 0;// 直接播放
	public static final int GOPLAY = 1;// 直接播放
	public static final int DELAYPLAY = 2;// 延时播放

	private int errCode; // 错误码
	private List<Audio> arrAudio;
	private List<Album> arrAlbum;
	private int returnType;// 返回类型，3复合，1audio，2album有值；
	// private boolean play; // 直接播放
	private int playType;// 0 选择，1，直接播放2.延时播放
	private int delayTime;// 服务器协商(ms)
	private int playIndex;// 播放的下标

	private List<BaseAudio> arrMix;// 混合类型，包括Album，和Audio的混排

	public List<BaseAudio> getArrMix() {
		return arrMix;
	}

	public void setArrMix(List<BaseAudio> arrMix) {
		this.arrMix = arrMix;
	}

	public int getPlayType() {
		return playType;
	}

	public void setPlayType(int playType) {
		this.playType = playType;
	}

	public int getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}

	public int getPlayIndex() {
		return playIndex;
	}

	public void setPlayIndex(int playIndex) {
		this.playIndex = playIndex;
	}

	// public List<Audio> getArrMix() {
	// return arrMix;
	// }
	//
	// public void setArrMix(List<Audio> arrMix) {
	// this.arrMix = arrMix;
	// }

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public List<Audio> getArrAudio() {
		return arrAudio;
	}

	public void setArrAudio(List<Audio> arrAudio) {
		this.arrAudio = arrAudio;
	}

	public List<Album> getArrAlbum() {
		return arrAlbum;
	}

	public void setArrAlbum(List<Album> arrAlbum) {
		this.arrAlbum = arrAlbum;
	}

	public int getReturnType() {
		return returnType;
	}

	public void setReturnType(int returnType) {
		this.returnType = returnType;
	}

}
