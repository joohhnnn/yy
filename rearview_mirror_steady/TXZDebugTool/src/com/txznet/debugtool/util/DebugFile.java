package com.txznet.debugtool.util;

public class DebugFile {

	private int txzDebugFileId;
	private String txzDebugFileName;
	private String txzDebugFilePath;
	public DebugFile(int txzDebugFileId, String txzDebugFileName,
			String txzDebugFilePath) {
		super();
		this.txzDebugFileId = txzDebugFileId;
		this.txzDebugFileName = txzDebugFileName;
		this.txzDebugFilePath = txzDebugFilePath;
	}
	public int getTxzDebugFileId() {
		return txzDebugFileId;
	}
	public void setTxzDebugFileId(int txzDebugFileId) {
		this.txzDebugFileId = txzDebugFileId;
	}
	public String getTxzDebugFileName() {
		return txzDebugFileName;
	}
	public void setTxzDebugFileName(String txzDebugFileName) {
		this.txzDebugFileName = txzDebugFileName;
	}
	public String getTxzDebugFilePath() {
		return txzDebugFilePath;
	}
	public void setTxzDebugFilePath(String txzDebugFilePath) {
		this.txzDebugFilePath = txzDebugFilePath;
	}
	
	
}
