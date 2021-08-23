package com.txznet.txz.component.choice.option;

import com.txznet.comm.ui.IKeepClass;
import com.txznet.txz.component.choice.OnItemSelectListener;

/**
 * 通过传参的方式控制，为空则按默认处理
 * 
 * @param <V>
 */
public class CompentOption<V> implements IKeepClass {
	/**
	 * 选择器状态通知回调
	 */
	public static abstract class ChoiceCallback {
		public abstract void onClearIsSelecting();
	}
	
	// 超时时间，为空没有超时
	private Long timeout;
	// 用于区分比较老的选择器
	private Boolean is2_0Version;
	// 首次进入的TTS文本
	private String ttsText;
	// 限定每页的数量
	private Integer numPageSize;
	// 选择回调
	private OnItemSelectListener<V> callbackListener;
	// 是否需要插确定
	private Boolean canSure;
	// 是否需要走进度条，将进度条换成数字，可控制时长，为空或者=0情况下不走进度条
	private Integer progressDelay;
	// 设置状态监听器
	private ChoiceCallback callback;
	// 是否禁用唤醒词
	private Boolean banWakeup;

	//当前列表的命名
	private String listPageName;

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public Boolean getIs2_0Version() {
		return is2_0Version;
	}

	public void setIs2_0Version(Boolean is2_0Version) {
		this.is2_0Version = is2_0Version;
	}

	public String getTtsText() {
		return ttsText;
	}

	public void setTtsText(String ttsText) {
		this.ttsText = ttsText;
	}

	public Integer getNumPageSize() {
		return numPageSize;
	}

	public void setNumPageSize(Integer numPageSize) {
		this.numPageSize = numPageSize;
	}

	public OnItemSelectListener<V> getCallbackListener() {
		return callbackListener;
	}

	public void setCallbackListener(OnItemSelectListener<V> callbackListener) {
		this.callbackListener = callbackListener;
	}

	public Boolean getCanSure() {
		return canSure;
	}

	public void setCanSure(Boolean canSure) {
		this.canSure = canSure;
	}

	public Integer getProgressDelay() {
		return progressDelay;
	}

	public void setProgressDelay(Integer progressDelay) {
		this.progressDelay = progressDelay;
	}
	
	public void setChoiceCallback(ChoiceCallback callback) {
		this.callback = callback;
	}

	public ChoiceCallback getChoiceCallback() {
		return callback;
	}
	
	public void setBanWakeup(Boolean ban) {
		this.banWakeup = ban;
	}
	
	public Boolean getBanWakeup() {
		return this.banWakeup;
	}

	public void setListPageName(String listPageName) {
		this.listPageName = listPageName;
	}

	public String getListPageName() {
		return listPageName;
	}
}