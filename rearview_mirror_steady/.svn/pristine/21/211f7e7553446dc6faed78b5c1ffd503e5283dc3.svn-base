package com.txznet.txz.ui.widget;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsManager.TtsCallback;
import com.txznet.txz.R;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.runnables.Runnable1;

public class SimAlertDialog extends WinDialog implements android.view.View.OnClickListener {

	private boolean mShowBtn;
	private int mIconId;
	private String mTitle;
	private String mContent;
	private long mDelay;
	private boolean mDisableCancel;
	private String mQRCodeUrl;
	private ITtsCallback mTtsCallback;

	public boolean isDisableCancel() {
		return mDisableCancel;
	}

	private View mView;
	private ImageView ivIcon;
	private TextView tvTitle;
	private TextView tvContent;
	private FrameLayout flBtn;
	private TextView btnConfirm;
	private ImageView ivQrCode;

	private SimAlertDialog(Builder builder) {
		super(builder, true);
		this.mIconId = builder.getIconId();
		this.mTitle = builder.getTitle();
		this.mContent = builder.getContent();
		this.mShowBtn = builder.isShowBtn();
		this.mDelay = builder.getDelay();
		this.mDisableCancel = builder.isDisableCancel();
		this.mQRCodeUrl = builder.getQRCodeUrl();
		this.mTtsCallback = builder.getTtsCallback();
	}

	@Override
	protected View createView() {
		LayoutInflater inflater = super.mDialog.getLayoutInflater();
		mView = inflater.inflate(R.layout.sim_alarm_dialog, null, false);

		return mView;
	}

	public void setContents(String content) {
		this.mContent = content;
	}

	@Override
	public void onShow() {
		Resources resources = GlobalContext.get().getResources();

		ivIcon = (ImageView) mView.findViewById(R.id.iv_icon);
		tvTitle = (TextView) mView.findViewById(R.id.tv_title);
		tvContent = (TextView) mView.findViewById(R.id.tv_content);
		flBtn = (FrameLayout) mView.findViewById(R.id.fl_btn);
		btnConfirm = (TextView) mView.findViewById(R.id.tv_confirm);
		ivQrCode = (ImageView) mView.findViewById(R.id.iv_qrcode);

		if (mDisableCancel) {
			setCanceledOnTouchOutside(false);
			setCancelable(false);
			mShowBtn = false;
		} else {
			if (mDelay > 0) {
				AppLogic.runOnUiGround(new Runnable1<SimAlertDialog>(this) {

					@Override
					public void run() {
						if(mP1!= null && mP1.isShowing()){
							mP1.dismiss("time exceed");
						}
					}
				}, mDelay);
			}
		}

		if (mShowBtn) {
			flBtn.setVisibility(View.VISIBLE);
			btnConfirm.setOnClickListener(this);
		} else {
			flBtn.setVisibility(View.GONE);
		}

		if (mIconId != -1) {
			ivIcon.setVisibility(View.VISIBLE);
			ivIcon.setImageResource(mIconId);
		} else {
			ivIcon.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mTitle)) {
			tvTitle.setVisibility(View.VISIBLE);
			tvTitle.setText(mTitle);
		} else {
			tvTitle.setVisibility(View.GONE);
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

		if (TextUtils.isEmpty(mQRCodeUrl)) {
			ivQrCode.setVisibility(View.GONE);
		} else {
			ivQrCode.setVisibility(View.VISIBLE);
			Bitmap bm = null;
			try {
				bm = QRUtil.createQRCodeBitmap(mQRCodeUrl, (int) resources.getDimension(R.dimen.y200));
			} catch (Exception e) {
				LogUtil.loge("Sim:Exception", e);
				e.printStackTrace();
			}
			ivQrCode.setImageBitmap(bm);
		}

		super.mDialog.getWindow().setLayout((int) resources.getDimension(R.dimen.x400), LayoutParams.WRAP_CONTENT);

		super.show();
	}

	@Override
	public void onClick(View v) {
		dismiss("click");
	}

	public static class Builder extends WinDialog.DialogBuildData{
		private boolean mShowBtn = false;
		private int mIconId = -1;
		private String mTitle;
		private String mContent;
		private long mDelay = 0;
		private boolean mDisableCancel = false;
		private String mQRCodeUrl;
		private ITtsCallback mTtsCallback;

		public String getQRCodeUrl() {
			return mQRCodeUrl;
		}

		public Builder setQRCodeUrl(String mQRCodeUrl) {
			this.mQRCodeUrl = mQRCodeUrl;
			return this;
		}

		public SimAlertDialog getDialog() {
			return new SimAlertDialog(this);
		}

		public boolean isShowBtn() {
			return mShowBtn;
		}

		public Builder setShowBtn(boolean showBtn) {
			this.mShowBtn = showBtn;
			return this;
		}

		public int getIconId() {
			return mIconId;
		}

		public Builder setIconId(int IconId) {
			this.mIconId = IconId;
			return this;
		}

		public String getTitle() {
			return mTitle;
		}

		public Builder setTitle(String title) {
			this.mTitle = title;
			return this;
		}

		public String getContent() {
			return mContent;
		}

		public Builder setContent(String content) {
			this.mContent = content;
			return this;
		}

		public long getDelay() {
			return mDelay;
		}

		public Builder setDelay(long delay) {
			this.mDelay = delay;
			return this;
		}
		
		public Builder setTtsCallBack(ITtsCallback ttsCallback) {
			mTtsCallback = ttsCallback;
			return this;
		}

		public ITtsCallback getTtsCallback() {
			return mTtsCallback;
		}

		public boolean isDisableCancel() {
			return mDisableCancel;
		}

		public Builder setDisableCancel(boolean disableCancel) {
			this.mDisableCancel = disableCancel;
			return this;
		}

	}
	
	@Override
	protected void onEndTts() {		
		super.onEndTts();
		if (mTtsCallback != null) {
			mTtsCallback.onEnd();
		}
	}

	@Override
	protected void onBeginTts() {
		super.onBeginTts();
		if (mTtsCallback != null) {
			mTtsCallback.onBegin();
		}
	}
	
	@Override
	public String getReportDialogId() {
		return "sim_alert";
	}
}
