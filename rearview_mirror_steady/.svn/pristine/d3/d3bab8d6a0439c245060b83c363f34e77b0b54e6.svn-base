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
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.txz.R;

public class SimNoticeDialog extends WinDialog implements android.view.View.OnClickListener {

	private boolean mShowBtn = true;
	private String mContent;
	private String mSureText;
	private String mCancelText;
	private String[] mSureCmds;
	private String[] mCancelCmds;
	private android.view.View.OnClickListener mPositiveClickListener = null;
	private android.view.View.OnClickListener mNegativeClickListener = null;

	private View mView;
	private TextView tvContent;
	private LinearLayout llBtn;
	private TextView btnConfirm;
	private TextView btnCancel;

	public SimNoticeDialog(WinDialog.DialogBuildData buildData) {
		super(buildData, true);
		mSureCmds = new String[]{};
		mCancelCmds = new String[]{};
	}
	
	@Override
	protected View createView() {
		LayoutInflater inflater = super.mDialog.getLayoutInflater();
		mView = inflater.inflate(R.layout.sim_notice_dialog, null, false);

		return mView;
	}

	public void setContents(String content) {
		this.mContent = content;
	}
	
	public void setPositiveBtn(String sureText, android.view.View.OnClickListener listener, String... cmds){
		this.mSureText = sureText;
		this.mPositiveClickListener = listener;
		if(cmds != null){
			this.mSureCmds = cmds;
		}
	}
	
	public void setNegativeBtn(String cancelText, android.view.View.OnClickListener listener, String... cmds ){
		this.mCancelText = cancelText;
		this.mNegativeClickListener = listener;
		if(cmds != null){
			this.mCancelCmds = cmds;
		}
	}
	
	public void setBtnShow(boolean isShow){
		this.mShowBtn = isShow;
	}
	
	@Override
	public void onShow() {
		Resources resources = GlobalContext.get().getResources();

		tvContent = (TextView) mView.findViewById(R.id.tv_notice_dialog_content);
		llBtn = (LinearLayout) mView.findViewById(R.id.ll_notice_dialog_btn);
		btnConfirm = (TextView) mView.findViewById(R.id.tv_notice_dialog_confirm);
		btnCancel = (TextView) mView.findViewById(R.id.tv_notice_dialog_cancel);

		if (mShowBtn) {
			llBtn.setVisibility(View.VISIBLE);
			if(mPositiveClickListener != null){
				btnConfirm.setOnClickListener(mPositiveClickListener);
			}else{
				btnConfirm.setOnClickListener(this);
			}
			if(mNegativeClickListener != null){
				btnCancel.setOnClickListener(mNegativeClickListener);
			}else{
				btnCancel.setOnClickListener(this);
			}
		} else {
			llBtn.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mContent)) {
			tvContent.setVisibility(View.VISIBLE);
			tvContent.setText(mContent);
			if (mContent.contains("\n")) {
				tvContent.setGravity(Gravity.START);
			} else {
				tvContent.setGravity(Gravity.CENTER);
			}
		} else {
			tvContent.setVisibility(View.GONE);
		}
		
		if(!TextUtils.isEmpty(mSureText)){
			btnConfirm.setText(mSureText);
		}
		if(!TextUtils.isEmpty(mCancelText)){
			btnCancel.setText(mCancelText);
		}

		super.mDialog.getWindow().setLayout((int) resources.getDimension(R.dimen.x400), LayoutParams.WRAP_CONTENT);

		super.show();
	}
	
	
	
	@Override
	protected void onGetFocus() {
		AsrUtil.cancel();

		AsrUtil.AsrConfirmCallback mWakeupAsrCallback = null;
		if (mSureCmds != null && mCancelCmds != null) {
			mWakeupAsrCallback = new AsrUtil.AsrConfirmCallback(mSureCmds, mCancelCmds) {
				@Override
				public String getTaskId() {
					return SimNoticeDialog.this.toString();
				}

				@Override
				public boolean needAsrState() {
					return true;
				}

				@Override
				public void onSure() {
					if(mPositiveClickListener != null){
						mPositiveClickListener.onClick(btnConfirm);
					}else{
						onClick(btnConfirm);
					}
				}

				@Override
				public void onCancel() {
					if(mNegativeClickListener != null){
						mNegativeClickListener.onClick(btnCancel);
					}else{
						onClick(btnCancel);
					}
				}

				@Override
				public String needTts() {
					return null;
				}

				@Override
				public void onTtsEnd() {
				}
				
				@Override
				public int getPriority() {
					return AsrUtil.WKASR_PRIORITY_NO_INSTANT_WK;
				}
			};
		}

		if (mWakeupAsrCallback != null) {
			AsrUtil.useWakeupAsAsr(mWakeupAsrCallback);
		}

		super.onGetFocus();
	}

	@Override
	public void onClick(View v) {
		dismiss("click");
	}

	@Override
	public String getReportDialogId() {
		return "sim_notice";
	}

}
