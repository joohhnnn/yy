package com.txznet.txz.plugin.interfaces;

/**
 * strData:{"key":0,"value":{data}}
 */
public abstract class AbsTextJsonParse {
	public static final int TYPE_TTS_NO_RESULT = 1;
	public static final int TYPE_TTS_SELECT = 2;

	public int type;
	public String value;

	public static class TextParseResult {
		public static final int ERROR_ERROR = -1;
		public static final int ERROR_UNKNOW = 0;
		public static final int ERROR_SUCCESS = 1;

		private String mSourceStr;
		private int mErrorCode = ERROR_UNKNOW;

		public TextParseResult(String strData) {
			this.mSourceStr = strData;
		}

		public TextParseResult setErrorCode(int errorCode) {
			this.mErrorCode = errorCode;
			return this;
		}

		public String getSourceStr() {
			return mSourceStr;
		}

		public int getErrorCode() {
			return mErrorCode;
		}
	}

	public String getName() {
		return getClass().getName();
	}

	public int parseStrData(String value) {
		return TextParseResult.ERROR_UNKNOW;
	}

	public abstract boolean acceptText(boolean hasThirdImpl, String strData);
}