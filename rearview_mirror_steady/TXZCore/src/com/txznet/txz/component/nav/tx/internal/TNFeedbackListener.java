package com.txznet.txz.component.nav.tx.internal;

import android.os.Bundle;

public abstract class TNFeedbackListener {
	/**
	 *
	 * @param errorCode
	 *            //0:成功//-1:不支持//1:执行失败//2:2次交互->选择第几个//3:2次交互重试
	 * @param data
	 *            返回具体数据
	 * @param strTtsWording
	 *            建议TTS术语
	 */
	public abstract void onFeedback(int errorCode, Bundle data, String strTtsWording);

	/**
	 * 超时
	 */
	public abstract void onTimeOut();

	public abstract void onRevWhereAmI(int errorCode, String address, String strTtsWording);

	public abstract void onRevRemainTime(int errorCode, int timeAsSecond, String strTtsWording);

	public abstract void onRevRemainDistance(int errorCode, int distanceAsMeter, String strTtsWording);
}
