package com.txznet.launcher.domain.upgrade;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.launcher.R;

import me.wcy.htmltext.HtmlText;

/**
 * TXZ内部软件的升级弹框
 */
public abstract class UpgradeInnerDialog extends WinDialog{

	private UpgradeDialogBuildDate mUpgradeDialogBuildDate;

	private View mView;
	private TextView tvTips;
	private TextView tvWakeupTips;

	public UpgradeInnerDialog(UpgradeDialogBuildDate buildData) {
		super(buildData, true);
		mUpgradeDialogBuildDate = buildData;
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = super.mDialog.getLayoutInflater();
		mView = inflater.inflate(R.layout.upgrade_inner_notify_dialog, null, false);

		return mView;
	}

	@Override
	public void onShow() {
		Resources resources = GlobalContext.get().getResources();

		tvTips = (TextView) mView.findViewById(R.id.tv_upgrade_tips);
		tvWakeupTips = (TextView) mView.findViewById(R.id.tv_upgrade_wakeup_tips);

		if (TextUtils.isEmpty(mUpgradeDialogBuildDate.mTips)) {
			tvTips.setVisibility(View.GONE);
		} else {
			tvTips.setVisibility(View.VISIBLE);
			tvTips.setText(mUpgradeDialogBuildDate.mTips);
		}
		HtmlText.from("您可以说“<font color='#FFFFFF'>确定</font>”或“<font color='#FFFFFF'>取消</font>”")
				.into(tvWakeupTips);

		super.mDialog.getWindow().setLayout(320, 124);

		super.onShow();
	}
	
	
	
	@Override
	protected void onGetFocus() {

		super.onGetFocus();
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

	public static class UpgradeDialogBuildDate extends DialogBuildData {
		public boolean mShowBtn = true;
		public String mTitle;
		public String mTips;
		public String mContent;
		public String mSureText;
		public String mCancelText;
		public String[] mSureCmds;
		public String[] mCancelCmds;


		public UpgradeDialogBuildDate setShowBtn(boolean mShowBtn) {
			this.mShowBtn = mShowBtn;
			return this;
		}


		public UpgradeDialogBuildDate setTitle(String mTitle) {
			this.mTitle = mTitle;
			return this;
		}


		public UpgradeDialogBuildDate setTips(String mTips) {
			this.mTips = mTips;
			return this;
		}



		public UpgradeDialogBuildDate setContent(String mContent) {
			this.mContent = mContent;
			return this;
		}

		@Override
		public UpgradeDialogBuildDate setHintTts(String tts, TtsUtil.PreemptType type) {
			super.setHintTts(tts, type);
			return this;
		}


		public UpgradeDialogBuildDate setCancelText(String text, String[] cmds) {
			mCancelText = text;
			this.mCancelCmds = cmds;
			if (this.mCancelCmds != null) {
				this.addAsrTask(new DialogAsrCallback() {
					@Override
					public void onSpeak(WinDialog win, String cmd) {
						win.dismiss("speak");
						((UpgradeInnerDialog) win).onClickCancel();
					}

					@Override
					public String getReportId(WinDialog win) {
						return "cancel";
					}
				}, this.mCancelCmds);
			}
			return this;
		}

		public UpgradeDialogBuildDate setSureText(String text, String[] cmds) {
			mSureText = text;
			this.mSureCmds = cmds;
			if (this.mSureCmds != null) {
				this.addAsrTask(new DialogAsrCallback() {
					@Override
					public void onSpeak(WinDialog win, String cmd) {
						win.dismiss("speak");
						((UpgradeInnerDialog) win).onClickOk();
					}

					@Override
					public String getReportId(WinDialog win) {
						return "ok";
					}
				}, this.mSureCmds);
			}
			return this;
		}
	}



}
