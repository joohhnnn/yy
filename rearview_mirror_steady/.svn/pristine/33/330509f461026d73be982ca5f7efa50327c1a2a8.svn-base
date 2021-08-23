package com.txznet.txz.voice;


public interface IVoiceProcessor {
	public final static int INIT_FAIL = 0;
	public final static int INIT_SUCCESS = 1;
	public final static int AUTH_SUCCESS = 2;
	public final static int AUTH_FAIL = 3;

	public static interface IEvent{
		/**
		 *
		 * @param code
		 */
		public void onEvent(int code);
	}
	/**
	 * 云知声专用接口
	 * @param audioIn
	 * @return
	 */
	public byte[] process(byte[] audioIn, byte[] bytes1);
	public void release();
	public void setCallback(IEvent callback);

	/**
	 *
	 * @return
	 */
	public int getType();
}
