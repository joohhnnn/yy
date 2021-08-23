package com.txznet.music.exception;

/**
 * @author telenewbie
 * @version 创建时间：2016年3月25日 下午6:39:35
 * 
 */
public class TxzException extends Exception {

	private int errorCode = 0;

	public TxzException() {
		super();
	}

	public TxzException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public TxzException(int errorCode, String detailMessage) {
		super(detailMessage);
		this.errorCode = errorCode;
	}

	public TxzException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public TxzException(String detailMessage) {
		super(detailMessage);
	}

	public TxzException(Throwable throwable) {
		super(throwable);
	}

	public int getErrorCode() {
		return errorCode;
	}

}
