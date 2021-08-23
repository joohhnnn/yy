package com.txznet.txz.component.call;

public interface ICall {
	/**
	 * 空闲
	 */
	public static final int STATE_IDLE = 0;
	/**
	 * 来电振铃中
	 */
	public static final int STATE_RINGING = 1;
	/**
	 * 去电发起中
	 */
	public static final int STATE_MAKING = 2;
	/**
	 * 接通
	 */
	public static final int STATE_OFFHOOK = 3;

	/**
	 * 获取状态
	 * @return
	 */
	public int getState();
	
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public int initialize(final IInitCallback oRun);

	public void release();


	/**
	 * 发起呼叫
	 * 
	 * @param num
	 *            号码
	 * @param name
	 *            显示名字
	 */
	public boolean makeCall(String num, String name);

	/**
	 * 停止呼叫
	 */
	public boolean stopCall();

	/**
	 * 接听来电
	 */
	public boolean answerCall();

	/**
	 * 拒接来电
	 */
	public boolean rejectCall();

	/**
	 * 来电静音
	 */
	public boolean silenceCall();

	/**
	 * 发送数字按键
	 * 
	 * @param key
	 *            发送的按键字符串
	 */
	public boolean sendKey(String key);

	/**
	 * 状态监听器 去电时序：onBusy->onMakecall->[onOffhook]->onCallStop
	 * 来电时序：onBusy->onIncomingRing
	 * ->[onIncomingAnswer->onOffhook/onIncomingReject]->onCallStop
	 * 未知来电(如扫描到收到来电，而非系统通知)：onBusy->onCallStop
	 */
	public abstract class ICallStateListener {
		/**
		 * 电话模块处于工作时调用，可能未正常捕获来电
		 */
		public void onBusy() {
		}

		/**
		 * 电话拨号呼出
		 * 
		 * @param num
		 * @param name
		 */
		public void onMakecall(String num, String name) {
		}

		/**
		 * 来电振铃开始
		 * 
		 * @param num
		 * @param name
		 */
		public void onIncomingRing(String num, String name) {
		}

		/**
		 * 用户接听来电
		 * 
		 * @param num
		 * @param name
		 */
		public void onIncomingAnswer(String num, String name) {
		}

		/**
		 * 用户拒接来电，之后会再次调用onCallStop
		 * 
		 * @param num
		 * @param name
		 */
		public void onIncomingReject(String num, String name) {
		}

		/**
		 * 电话接通
		 */
		public void onOffhook() {
		}

		/**
		 * 来电或去电结束
		 * 
		 * @param num
		 * @param name
		 */
		public void onCallStop() {
		}
	}

	/**
	 * 设置状态监听器
	 * 
	 * @param listener
	 */
	public boolean setStateListener(ICallStateListener listener);
}
