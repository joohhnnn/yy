package com.txznet.txz.component.text;

import com.txz.ui.voice.VoiceData.VoiceParseData;

public interface IText {
	public static final int PRIORITY_LEVEL_MAX = 99;
	public static final int PRIORITY_LEVEL_HIGH = 90;
	public static final int PRIORITY_LEVEL_LOCAL_HIGH = 80;
	public static final int PRIORITY_LEVEL_NORMAL = 50;
	public static final int PRIORITY_LEVEL_LOW = 25;
	public static final int PRIORITY_LEVEL_MIN = 0;
	public static final int PRIORITY_LEVEL_GOD = PRIORITY_LEVEL_MAX;
	
	public static final int NetError = -1;
	public static final int ParseError = -2;
	public static final int InterruptedError = -3;
	public static final int NetTimeOutError = -4;
	public static final int UnkownError = -5;
	
	public static final int HighErrorBegin = -1000;
	public static final int HighErrorEnd = -2000;
	
	public static final int TxzErrorEnd = -3000;
	public static final int TxzErrorBegin = -4000;
	public static final int TxzErrorUnknown = -3001;
	public static final int TxzErrorDataNull = -3002;
	public static final int TxzErrorValueNull = -3003;
	public static final int TxzErrorGPSNull = -3004;
	public static final int TxzErrorInputNull = -3005;
	public static final int TxzErrorIdNull = -3006;
	
	public static final String TxzErrorDataNullStr = "dataNull";
	public static final String TxzErrorValueNullStr = "valueNull";
	public static final String TxzErrorUnknownStr = "unknown";
	public static final String TxzErrorGPSNullStr = "gpsNull";
	public static final String TxzErrorInputNULLStr = "inputNull";
	public static final String TxzErrorIdNULLStr = "idNull";
	
	public static final String NetTimeOutErrorStr = "timeOut";
	public static final String ParseErrorStr = "parse";
	public static final String InterruptedErrorStr = "interrupted";
	public static final String ALL = "all";
	
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}
   
	public static enum PreemptLevel{
		PREEMPT_LEVEL_NONE,
		PREEMPT_LEVEL_IMMEDIATELY, 
		PREEMPT_LEVEL_NEXT
	}
	
	public static abstract class ITextCallBack {
		
		/**
		 * ???????????????????????????????????????????????????json
		 * @param jsonResult
		 */
		@Deprecated
		public void onResult(String jsonResult,int priority) {
			onResult(jsonResult);
		}
		@Deprecated
		public void onResult(String jsonResult) {
		}
		/**
		 * ?????????????????? ??????????????????????????????????????????????????????
		 * @param byteResult ?????????protoBuf?????????
		 */
		public void onResult(byte[] byteResult,int priority) {
		}
		/**
		 * ???????????????????????????
		 * @param errorCode
		 */
		public void onError(int errorCode,int priority) {
		}
		public void onError(int errorCode) {
		}
		/**
		 * ????????????????????????TXZ?????????????????????
		 * @param dataResult
		 */
		public void onResult(VoiceParseData dataResult,int priority) {
			if (dataResult != null)
				onResult(dataResult.strVoiceData,priority);
		}
	}

	public int initialize(final IInitCallback oRun);

	@Deprecated
	public int setText(String text, ITextCallBack callBack);
	
	public int setVoiceData(VoiceParseData parseData, ITextCallBack callBack);

	public void cancel();

	public void release();
	
	public void setPriority(int priority);
	
	public int getPriority();
}
