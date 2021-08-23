package com.txznet.audio.player;

import java.io.Serializable;

public class MediaError implements Serializable {
	private static final long serialVersionUID = 3644835386758647011L;
	
	//播放器错误
	public static final int ERR_UNKNOW = 1; // 未知错误
	public static final int ERR_SYS_PLAYER = 2; // 系统播放器错误
	public static final int ERR_CODEC = 3; // 解码错误
	public static final int ERR_DISCONNECT = 7; // 断开连接
	public static final int ERR_BAD_DATA = 8; // 错误数据
	public static final int ERR_REMOTE = 11; // 远程调用错误
	public static final int ERR_REMOTE_DISCONNECT = 12; // 远程服务断开
	public static final int ERR_CHECK_ERROR = 13;
	public static final int ERR_BIND = 16; // 绑定服务失败
	public static final int ERR_NULL_STATE = 18; // 请求地址发生错误
	
	//网络错误
	public static final int ERR_FILE_NOT_EXIST = 4; // 文件不存在
	public static final int ERR_IO = 5; // IO错误
	public static final int ERR_GATE_WAY = 6; // 网关错误
	public static final int ERR_URI = 9;// 播放地址错误
	public static final int ERR_FILE_FOBIDDEN = 10; // 文件禁止访问
	public static final int ERR_BAD_REQUEST = 14; // 文件禁止访问
	public static final int ERR_REQ_TIMEOUT = 15; // 路径请求超时
	public static final int ERR_REQ_SERVER = 17; // 请求地址发生错误
	
	//解码错误
	public static final int ERR_READ_FRAME = 19; // 从管道中读取数据错误
	public static final int ERR_CREATE_TRACK = 20; // 创建AudioTrack失败
	public static final int ERR_GET_AUDIO = 21; // 创建AudioTrack失败
	public static final int ERR_TRANSFER = 22; //传输异常
	
	

	private int errCode;
	private String errDesc;
	private String errHint;

	public MediaError() {

	}

	public MediaError(int code, String desc, String hint) {
		errCode = code;
		errDesc = desc;
		errHint = hint;
	}

	public int getErrCode() {
		return errCode;
	}

	public String getErrDesc() {
		return errDesc;
	}

	public String getErrHint() {
		return errHint;
	}

	@Override
	public String toString() {
		return "MediaError [errCode=" + errCode + ", errDesc=" + errDesc
				+ ", errHint=" + errHint + "]";
	}
}
