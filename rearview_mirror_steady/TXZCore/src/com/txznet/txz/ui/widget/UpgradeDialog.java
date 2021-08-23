package com.txznet.txz.ui.widget;

import android.content.res.Resources;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.comm.ui.dialog2.WinMessageBox;
import com.txznet.txz.R;

public abstract class UpgradeDialog extends WinDialog implements View.OnClickListener {

	private UpgradeDialogBuildDate mUpgradeDialogBuildDate;

	private View mView;
	private TextView tvTitle;
	private TextView tvTips;
	private TextView tvContent;
	private LinearLayout llBtn;
	private TextView btnConfirm;
	private TextView btnCancel;

	public UpgradeDialog(UpgradeDialogBuildDate buildData) {
		super(buildData, true);
		mUpgradeDialogBuildDate = buildData;
	}

	private boolean mRegisted = false;
	private HomeObservable.HomeObserver mHomeObserver = new HomeObservable.HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss(REPORT_ACTION_TYPE_HOME);
			onClickCancel();
		}
	};

	@Override
	protected View createView() {
		LayoutInflater inflater = super.mDialog.getLayoutInflater();
		mView = inflater.inflate(R.layout.upgrade_notify_dialog, null, false);

		return mView;
	}

	@Override
	protected void onDismiss() {
		super.onDismiss();
		if (mRegisted) {
			mRegisted = false;
			try {
				GlobalObservableSupport.getHomeObservable().unregisterObserver(
						mHomeObserver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onShow() {
		if (!mRegisted) {
			mRegisted = true;
			try {
				GlobalObservableSupport.getHomeObservable().registerObserver(
						mHomeObserver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Resources resources = GlobalContext.get().getResources();

		tvTitle = (TextView) mView.findViewById(R.id.tv_upgrade_title);
		tvTips = (TextView) mView.findViewById(R.id.tv_upgrade_tips);
		tvContent = (TextView) mView.findViewById(R.id.tv_upgrade_content);
		llBtn = (LinearLayout) mView.findViewById(R.id.ll_upgrade_btn);
		btnConfirm = (TextView) mView.findViewById(R.id.tv_upgrade_confirm);
		btnCancel = (TextView) mView.findViewById(R.id.tv_upgrade_cancel);

		if (mUpgradeDialogBuildDate.mShowBtn) {
			llBtn.setVisibility(View.VISIBLE);

			btnConfirm.setOnClickListener(this);
			btnCancel.setOnClickListener(this);
		} else {
			llBtn.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mUpgradeDialogBuildDate.mContent)) {
			tvContent.setVisibility(View.VISIBLE);
			tvContent.setText(mUpgradeDialogBuildDate.mContent);
			if (mUpgradeDialogBuildDate.mContent.contains("\n")) {
				tvContent.setGravity(Gravity.START);
			} else {
				tvContent.setGravity(Gravity.CENTER);
			}
		} else {
			tvContent.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(mUpgradeDialogBuildDate.mTitle)) {
			tvTitle.setVisibility(View.GONE);
		} else {
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(mUpgradeDialogBuildDate.mTitle);
		}

		if (TextUtils.isEmpty(mUpgradeDialogBuildDate.mTips)) {
			tvTips.setVisibility(View.GONE);
		} else {
			tvTips.setVisibility(View.VISIBLE);
			tvTips.setText(mUpgradeDialogBuildDate.mTips);
		}
		
		if(!TextUtils.isEmpty(mUpgradeDialogBuildDate.mSureText)){
			btnConfirm.setText(mUpgradeDialogBuildDate.mSureText);
		}
		if(!TextUtils.isEmpty(mUpgradeDialogBuildDate.mCancelText)){
			btnCancel.setText(mUpgradeDialogBuildDate.mCancelText);
		}

		super.mDialog.getWindow().setLayout((int) resources.getDimension(R.dimen.x450), LayoutParams.WRAP_CONTENT);

		super.onShow();
	}
	
	
	
	@Override
	protected void onGetFocus() {

		super.onGetFocus();
	}

	@Override
	public void onClick(View v) {
		dismiss("click");
		if (v == btnConfirm) {
			onClickOk();
		}else {
			onClickCancel();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		dismiss("backPress");
		onClickCancel();
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
						((UpgradeDialog) win).onClickCancel();
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
						((UpgradeDialog) win).onClickOk();
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
