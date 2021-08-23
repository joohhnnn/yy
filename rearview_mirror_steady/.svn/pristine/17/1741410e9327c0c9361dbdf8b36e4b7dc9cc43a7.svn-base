package com.txznet.txz.component.choice.page;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

public abstract class ResourcePage<T, E> {

	public static interface OnGetDataCallback<T> {
		public void onGetData(T res);
	}

	/**
	 * 请求的页数
	 */
	protected int currPage;

	/**
	 * 当前页的个数
	 */
	protected int maxPage;

	/**
	 * 总数
	 */
	protected int totalSize;

	/**
	 * 当前页数量
	 */
	protected int currPageSize;

	/**
	 * 当前页数据
	 */
	protected T mCurrRes;
	/**
	 * 数据源
	 */
	protected T mSourceRes;

	/**
	 * 是否拦截切换页（兼容老版本用）
	 */
	private boolean isIntercepPage;

	public ResourcePage(T resources, int totalSize) {
		this.mSourceRes = resources;
		this.totalSize = totalSize;
		reset();
	}

	public ResourcePage<T, E> reset() {
		currPage = 0;
		clearCurrRes(mCurrRes);
		reCompute();
		return this;
	}
	
	public ResourcePage<T, E> reCompute() {
		int size = numOfPageSize();
		maxPage = totalSize / size;
		if (totalSize % size != 0) {
			maxPage++;
		}
		return this;
	}

	public void clearPage() {
		mCurrRes = null;
		mSourceRes = null;
	}

	protected abstract void clearCurrRes(T currRes);

	protected abstract T notifyPage(int sIdx, int len, T sourceRes);

	protected abstract int numOfPageSize();

	protected abstract int getCurrResSize(T currRes);

	public boolean interceptPage() {
		return this.isIntercepPage;
	}

	public void setInterceptPage(boolean isIntercept) {
		LogUtil.logd("setInterceptPage:" + isIntercept);
		this.isIntercepPage = isIntercept;
	}

	public int getMaxPage() {
		return maxPage;
	}

	/**
	 * 返回当前页
	 * 
	 * @return
	 */
	public int getCurrPage() {
		return currPage;
	}

	/**
	 * 获取数据总数
	 * 
	 * @return
	 */
	public int getTotalSize() {
		return this.totalSize;
	}

	/**
	 * 获取当前页的数量
	 * 
	 * @return
	 */
	public int getCurrPageSize() {
		return getCurrResSize(mCurrRes);
	}

	public abstract E getItemFromCurrPage(int idx);

	public abstract E getItemFromSource(int idx);

	/**
	 * 下一页
	 * 
	 * @return
	 */
	public boolean nextPage() {
		currPage++;
		if (currPage > maxPage - 1) {
			currPage = maxPage - 1;
			return false;
		}

		if (interceptPage()) {
			onNextPage();
			return true;
		}

		notifyPageInner();
		return true;
	}

	public void onNextPage() {

	}

	/**
	 * 默认是notifyPage通知方法
	 */
	protected void notifyPageInner() {
		int sIdx = numOfPageSize() * currPage;
		int len = totalSize - sIdx;
		if (len > numOfPageSize()) {
			len = numOfPageSize();
		}
		LogUtil.logd("notifyPageInner curr:" + currPage + ",sIdx:" + sIdx + ",len:" + len);

		clearCurrRes(mCurrRes);
		mCurrRes = notifyPage(sIdx, len, mSourceRes);
	}

	/**
	 * 上一页
	 * 
	 * @return
	 */
	public boolean lastPage() {
		currPage--;
		if (currPage < 0) {
			currPage = 0;
			return false;
		}
		if (interceptPage()) {
			onLastPage();
			return true;
		}

		notifyPageInner();
		return true;
	}

	public void onLastPage() {

	}
	
	/**
	 * 从0开始
	 * @param page
	 * @return
	 */
	protected boolean isOverPage(int page) {
		if (page < 0 || page > maxPage - 1) {
			return true;
		}
		return false;
	}

	/**
	 * 请求某一页的数据
	 * 
	 * @param i
	 * @return
	 */
	public boolean selectPage(int i) {
		int page = i - 1;
		if (isOverPage(page)) {
			LogUtil.loge("selectPage error page:" + page + ",maxPage:" + maxPage);
			return false;
		}
		if (currPage == page) {
			LogUtil.logw("selectPage equals page:" + currPage);
			return false;
		}

		currPage = page;
		if (interceptPage()) {
			return true;
		}

		notifyPageInner();
		return true;
	}

	/**
	 * 请求当前页数据
	 */
	public void requestCurrPage(OnGetDataCallback<T> callback) {
		notifyPageInner();
		if (callback != null) {
			callback.onGetData(mCurrRes);
		}
	}
	
	/**
	 * 通知移除item
	 * @param idx
	 * @param isCurrPage
	 */
	public E notifyRemoveIdx(int idx, boolean isCurrPage) {
		int allIdx = idx;
		if (isCurrPage) {
			allIdx = currPage * numOfPageSize() + idx;
		}

		E b = removeFromSource(idx, allIdx);
		LogUtil.logd("notifyRemoveIdx:" + allIdx + "," + b);
		if (b != null) {
			totalSize--;
			reCompute();
			// reset();
			if (isOverPage(currPage)) {
				selectPage(currPage);
			} else {
				selectPage(currPage + 1);
			}
		}
		return b;
	}

	/**
	 * 从数据源删除某一个idx数据
	 * 
	 * @param cIdx
	 *            当前页索引
	 * @param tIdx
	 *            总数据索引
	 */
	public E removeFromSource(int cIdx, int tIdx) {
		// 如果允许删除，则需要重写该方法
		return null;
	}

	/**
	 * 获取当前页的数据
	 * 
	 * @return
	 */
	public T getResource() {
		return this.mCurrRes;
	}
}