package com.txznet.comm.ui.dialog2;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 等待处理处理，用户按返回关闭，按确定执行某操作
 *
 * @author pppi
 */
public abstract class WinConfirm extends WinMessageBox {
	/**
	 * 对话框构建数据类型
	 * 
	 * @author pppi
	 *
	 */
	public static class WinConfirmBuildData extends WinMessageBoxBuildData {
		@Override
		public void check() {
			// 设置默认确定按钮文本
			if (this.mLeftText == null) {
				this.setSureText(DEFAULT_TEXT_SURE);
			}
			// 设置默认取消按钮文本
			if (this.mRightText == null) {
				this.setCancelText(DEFAULT_TEXT_CANCEL);
			}
			super.check();
			addExtraInfo("dialogType", WinConfirm.class.getSimpleName());
		}

		/**
		 * 设置取消按钮文本
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinConfirmBuildData setCancelText(String text) {
			super.setRightText(text);
			return this;
		}

		/**
		 * 设置确定按钮文本
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinConfirmBuildData setSureText(String text) {
			super.setLeftText(text);
			return this;
		}
	};

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinConfirm(WinConfirmBuildData data) {
		this(data, true);
	}

	/**
	 * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
	 * 
	 * @param data
	 *            构建数据
	 * @param init
	 *            是否初始化，自己构造时传true，派生类构造时传false
	 */
	protected WinConfirm(WinConfirmBuildData data, boolean init) {
		super(data, false);

		if (init) {
			initDialog();
		}
	}

	/**
	 * 初始化需要方控控制的焦点列表
	 */
	@Override
	protected void onInitFocusView() {
		List<FocusView> focusViews = new ArrayList<FocusView>(2);
		focusViews.add(0, new FocusView(mViewHolder.mLeftButton, "ButtonOk"));
		focusViews.add(1, new FocusView(mViewHolder.mRightButton, "ButtonCancel"));
		setFocusViews(focusViews, -1);
	}

	@Deprecated
	public WinConfirm setCancelText(String s) {
		super.setRightButton(s);
		return this;
	}

	@Deprecated
	public WinConfirm setSureText(String s) {
		super.setLeftButton(s);
		return this;
	}

	/**
	 * 确定回调
	 */
	public abstract void onClickOk();

	/**
	 * 取消回调
	 */
	public void onClickCancel() {

	}

	@Override
	public void onClickLeft() {
		onClickOk();
		dismissInner();
	}

	@Override
	public void onClickRight() {
		onClickCancel();
		dismissInner();
	}

	/**
	 * 点击空白处回调
	 */
	@Override
	public void onClickBlank() {
		DialogBuildData oBuildData = mBuildData;
		if (oBuildData != null){
			//决定对话框是否可以响应界面外的touch事件。注意:这两个参数默认值是null,null应该等同于true。
			if (oBuildData.mCancelOutside != null && oBuildData.mCancelOutside == false) {
				return;
			}
		}
		onClickRight();
	}

	/**
	 * 返回按钮回调
	 */
	@Override
	public void onBackPressed() {
		onClickCancel();
		dismissInner();
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickOkCountDown(int time) {
		super.clickLeftCountDown(mWinMessageBoxBuildData.mLeftText
				+ DEFAULT_COUNT_DOWN_SUFFIX, time);
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param text
	 *            格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickOkCountDown(String text, int time) {
		super.clickLeftCountDown(text, time);
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickCancelCountDown(int time) {
		super.clickRightCountDown(mWinMessageBoxBuildData.mRightText
				+ DEFAULT_COUNT_DOWN_SUFFIX, time);
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param text
	 *            格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickCancelCountDown(String text, int time) {
		super.clickRightCountDown(text, time);
	}
}
