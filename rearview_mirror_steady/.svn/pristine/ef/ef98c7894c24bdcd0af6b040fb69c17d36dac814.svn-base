package com.txznet.comm.ui.dialog2;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.txz.comm.R;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class WinProgress extends WinDialog {
	/**
	 * 默认处理进度文本
	 */
	public static final String DEFAULT_TEXT_PROGRESS = "正在处理中...";

	/**
	 * 进度对话框构建数据
	 * 
	 * @author pppi
	 *
	 */
	public static class WinProgressBuildData extends WinDialog.DialogBuildData {
		/**
		 * 处理中消息文本
		 */
		String mMessageText;
		/**
		 * 处理中动画的资源ID
		 */
		int mDrawableId;

		@Override
		public void check() {
			// 处理框默认优先级是最高的
			if (this.mPreemptType == null) {
				this.setHintType(PreemptType.PREEMPT_TYPE_IMMEADIATELY);
			}

			// 默认消息设置
			if (mMessageText == null) {
				mMessageText = DEFAULT_TEXT_PROGRESS;
			}
			super.check();
			addExtraInfo("mMessageText", mMessageText);
			addExtraInfo("dialogType", WinProgress.class.getSimpleName());
		}

		/**
		 * 设置消息文本
		 * 
		 * @param text
		 * @return
		 */
		public WinProgressBuildData setMessageText(String text) {
			this.mMessageText = text;
			return this;
		}

		/**
		 * 设置处理中动画的资源ID
		 * 
		 * @param id
		 * @return
		 */
		public WinProgressBuildData setDrawableId(int id) {
			this.mDrawableId = id;
			return this;
		}
	}

	/**
	 * 处理中文本的TextView
	 */
	protected TextView mText;
	/**
	 * 动画对象
	 */
	protected AnimationDrawable mAnim;
	/**
	 * 动画图片的ImageView
	 */
	protected ImageView mAnimImage;

	/**
	 * 默认构造
	 */
	public WinProgress() {
		this(new WinProgressBuildData());
	}

	/**
	 * 快速构造
	 * 
	 * @param text
	 *            处理中文本
	 */
	public WinProgress(String text) {
		this(new WinProgressBuildData().setMessageText(text));
	}

	/**
	 * 快速构造
	 * 
	 * @param text
	 *            处理中文本
	 * @param drawableId
	 *            动画资源ID
	 */
	public WinProgress(String text, int drawableId) {
		this(new WinProgressBuildData().setMessageText(text).setDrawableId(
				drawableId));
	}

	/**
	 * 快速构造
	 * 
	 * @param text
	 *            处理中文本
	 * @param isSystem
	 *            是否为系统弹窗
	 */
	public WinProgress(String text, boolean isSystem) {
		this((WinProgressBuildData) new WinProgressBuildData().setMessageText(
				text).setSystemDialog(isSystem));
	}

	/**
	 * 快速构造
	 * 
	 * @param text
	 *            处理中文本
	 * @param drawableId
	 *            动画资源ID
	 * @param isSystem
	 *            是否为系统弹窗
	 */
	public WinProgress(String text, int drawableId, boolean isSystem) {
		this((WinProgressBuildData) new WinProgressBuildData()
				.setMessageText(text).setDrawableId(drawableId)
				.setSystemDialog(isSystem));
	}

	/**
	 * 特有构建数据
	 */
	WinProgressBuildData mWinProgressBuildData;

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinProgress(WinProgressBuildData data) {
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
	protected WinProgress(WinProgressBuildData data, boolean init) {
		super(data, false);

		mWinProgressBuildData = data;

		if (init) {
			initDialog();
		}
	}

	@SuppressLint("InflateParams")
	@Override
	protected View createView() {
		View context = LayoutInflater.from(getContext()).inflate(
				R.layout.comm_win_progress, null);
		mText = (TextView) context.findViewById(R.id.prgProgress_Text);
		if (this.mWinProgressBuildData.mMessageText != null) {
			mText.setText(this.mWinProgressBuildData.mMessageText);
		}
		mAnimImage = (ImageView) context.findViewById(R.id.imgProgress_Anim);
		if (mWinProgressBuildData.mDrawableId != 0) {
			// 外部设置图片进入
			mAnimImage.setImageResource(mWinProgressBuildData.mDrawableId);
			RotateAnimation animation = new RotateAnimation(0, 360,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mAnimImage.startAnimation(animation);
		} else if (mAnimImage.getDrawable() != null
				&& mAnimImage.getDrawable() instanceof AnimationDrawable) {
			mAnim = (AnimationDrawable) mAnimImage.getDrawable();
			mAnim.start();
		}
		return context;
	}

	/**
	 * 更新对话框
	 * 
	 * @param txt
	 *            处理中文本
	 */
	public void updateProgress(String txt) {
		this.mWinProgressBuildData.mMessageText = txt;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mText.setText(WinProgress.this.mWinProgressBuildData.mMessageText);
			}
		}, 0);
	}

	/**
	 * 更新对话框
	 * 
	 * @param drawableId
	 *            动画资源id
	 */
	public void updateProgress(int drawableId) {
		this.mWinProgressBuildData.mDrawableId = drawableId;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mAnimImage.setImageResource(WinProgress.this.mWinProgressBuildData.mDrawableId);
			}
		}, 0);
	}

	/**
	 * 更新对话框
	 * 
	 * @param drawableId
	 *            动画资源id
	 * @param txt
	 *            处理中文本
	 */
	public void updateProgress(int drawableId, String txt) {
		this.mWinProgressBuildData.mDrawableId = drawableId;
		this.mWinProgressBuildData.mMessageText = txt;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mAnimImage.setImageResource(WinProgress.this.mWinProgressBuildData.mDrawableId);
				mText.setText(WinProgress.this.mWinProgressBuildData.mMessageText);
			}
		}, 0);
	}

	/**
	 * 倒计时关闭
	 * 
	 * @param text
	 *            处理中格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 * @param end
	 *            倒计时到达后执行的操作
	 */
	public void dismissCountDown(final String text, final int time,
			final Runnable end) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(mText, text, time, new Runnable() {
					@Override
					public void run() {
						if (end != null) {
							end.run();
						}
						WinProgress.this.dismissInner();
					}
				});
			}
		}, 0);
	}

	/**
	 * 获取调试字符串
	 * 
	 * @return
	 */
	public String getDebugString() {
		return this.toString() + "[" + this.mWinProgressBuildData.mMessageText
				+ "]";
	}
}
