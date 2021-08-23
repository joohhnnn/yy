package com.txznet.txz.component.choice;

import java.util.List;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.list.CommWorkChoice;
import com.txznet.txz.component.choice.list.WorkChoice;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;

public abstract class ListHook<E> {
	protected CommWorkChoice<E> workChoice;

	public abstract String getReportId();

	public abstract String convItemToJson(E item);

	public abstract void onConvToJson(List<E> ts, JSONBuilder jsonBuilder);

	/**
	 * @param item
	 * @return 返回true表示直接执行，不能重新选择，重新选择指的是在选择界面播报TTS， TTS没播报完会停在选择页，打断TTS可以重新选择
	 */
	public abstract boolean onSelectItem(E item);

	public void onGetWorkSpace(CommWorkChoice<E> wp) {
		this.workChoice = wp;
	}

	public ResourcePage<List<E>, E> createPage(List<E> source, final int numPageSize) {
		return new ResListPage<E>(source) {

			@Override
			protected int numOfPageSize() {
				return numPageSize;
			}
		};
	}

	/**
	 * 允许重新选择情况下，真实选中后要通知选中
	 * 
	 * @param item
	 * @param idx
	 * @param fromVoice
	 */
	public void notifySelectEnd(E item, int idx, String fromVoice) {
		this.workChoice.putReport(WorkChoice.KEY_DETAIL, convItemToJson(item));
		this.workChoice.putReport(WorkChoice.KEY_INDEX, idx + "");
		this.workChoice.doReportSelectFinish(true,
				fromVoice != null ? WorkChoice.SELECT_TYPE_VOICE : WorkChoice.SELECT_TYPE_UNKNOW, fromVoice);
	}
	
	/**
	 * 当列表数据被删完的时候请求刷新页面会回调，返回false，将使用默认的取消操作
	 */
	public boolean onEmptyDataUpdate() {
		return false;
	}
	
	/**
	 * 删除数据的回调
	 * @param item
	 */
	public void onRemoveItem(E item) {

	}

	public void onAddWakeupCmds(AsrUtil.AsrComplexSelectCallback acsc,List<E> curData) {

	}

	public boolean onCmdSelected(String type,String command) {
		return false;
	}

	public boolean onIndexSelected(List<Integer> indexs, String command){
		return false;
	}
}