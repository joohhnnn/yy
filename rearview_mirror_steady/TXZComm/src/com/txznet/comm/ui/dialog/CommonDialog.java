package com.txznet.comm.ui.dialog;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IWakeupAsrCallback;
import com.txznet.comm.ui.IKeepClass;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.QRUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 通用的弹窗类，可以显示普通的文本，也可以显示二维码图片，增加声控确定和取消的唤醒词
 * 由于是给插件使用，为了兼容性请不要删除已有的public方法和属性
 * 2.5.0版本上增加
 * @author Bear
 *
 */
public class CommonDialog extends WinDialog implements IKeepClass {

	private String[] mSureCmds = new String[] {};
	private String[] mCancelCmds = new String[] {};

	private ImageView ivIcon;
	private TextView tvTitle;
	private TextView tvContent;
	private TextView btnConfirm;
	private TextView btnCancel;
	private ImageView ivQrCode;
	private Resources mResources;
	private FrameLayout flCustomContentView;

	public CommonDialog (){
		super(true);
	}
	
	@Override
	protected View createView() {
		mResources = GlobalContext.get().getResources();
		LayoutInflater inflater = getLayoutInflater();

		View view = inflater.inflate(R.layout.dialog_common, null, false);

		ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvContent = (TextView) view.findViewById(R.id.tv_content);
		btnConfirm = (TextView) view.findViewById(R.id.tv_confirm);
		btnCancel = (TextView) view.findViewById(R.id.tv_cancel);
		ivQrCode = (ImageView) view.findViewById(R.id.iv_qrcode);
		flCustomContentView = (FrameLayout) view.findViewById(R.id.fl_custom_content);

		return view;
	}

	private void unregWakeupAsr() {
		AsrUtil.recoverWakeupFromAsr(CommonDialog.this.toString());
	}

	private void regWakeupAsr() {
		AsrUtil.cancel();
		IWakeupAsrCallback mWakeupAsrCallback = null;
		int cmdsLength = (mSureCmds == null ? 0 : mSureCmds.length) + (mCancelCmds == null ? 0 : mCancelCmds.length);
		if (cmdsLength > 0) {
			mWakeupAsrCallback = new AsrUtil.IWakeupAsrCallback() {

				@Override
				public boolean needAsrState() {
					return false;
				}

				@Override
				public String getTaskId() {
					return CommonDialog.this.toString();
				}

				public boolean onAsrResult(String text) {
					for (String cmd : mSureCmds) {
						if (TextUtils.equals(cmd, text)) {
							if (mPositiveClickListener != null) {
								mPositiveClickListener.onClick(btnConfirm);
							}
							return true;
						}
					}
					for (String cmd : mCancelCmds) {
						if (TextUtils.equals(cmd, text)) {
							if (mNegativeClickListener != null) {
								mNegativeClickListener.onClick(btnCancel);
							}
							return true;
						}
					}
					return false;
				}

				@Override
				public int getPriority() {
					return AsrUtil.WKASR_PRIORITY_NO_INSTANT_WK;
				}

				@Override
				public String[] genKeywords() {
					return genCmds(mSureCmds, mCancelCmds);
				}
			};

		}

		if (mWakeupAsrCallback != null) {
			AsrUtil.useWakeupAsAsr(mWakeupAsrCallback);
		}
	}

	private String[] genCmds(String[] sureCmds, String[] cancelCmds) {
		int cmdsLength = (sureCmds == null ? 0 : sureCmds.length) + (cancelCmds == null ? 0 : cancelCmds.length);
		String[] cmds = new String[cmdsLength];
		int k = 0;
		for (int i = 0; i < sureCmds.length; i++) {
			cmds[k] = sureCmds[i];
			k++;
		}
		for (int i = 0; i < cancelCmds.length; i++) {
			cmds[k] = cancelCmds[i];
			k++;
		}
		return cmds;
	}

	@Override
	public void show() {
		getWindow().setLayout((int) mResources.getDimension(R.dimen.x400), LayoutParams.WRAP_CONTENT);

		super.show();
	}

	public void setCustomContentView(View view) {
		tvContent.setVisibility(View.GONE);
		ivQrCode.setVisibility(View.GONE);
		flCustomContentView.addView(view);
	}

	public void setTitle(String title) {
		tvTitle.setVisibility(View.VISIBLE);
		tvTitle.setText(title);
	}

	public void setTitleIcon(int resId) {
		ivIcon.setVisibility(View.VISIBLE);
		ivIcon.setImageResource(resId);
	}

	public void setContent(String content) {
		tvContent.setVisibility(View.VISIBLE);
		tvContent.setText(content);
	}

	public void setContentGravity(int gravity) {
		tvContent.setGravity(gravity);
	}

	public void setQrCode(String content, int width) {
		ivQrCode.setVisibility(View.VISIBLE);
		Bitmap bm = null;
		try {
			bm = QRUtil.createQRCodeBitmap(content, width);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ivQrCode.setImageBitmap(bm);
	}

	public void setQrCode(String content) {
		setQrCode(content, (int) mResources.getDimension(R.dimen.y200));
	}

	private android.view.View.OnClickListener mPositiveClickListener;
	private android.view.View.OnClickListener mNegativeClickListener;

	public void setPositiveButton(String text, android.view.View.OnClickListener listener, String... cmds) {
		btnConfirm.setVisibility(View.VISIBLE);
		btnConfirm.setText(text);
		btnConfirm.setOnClickListener(listener);
		mPositiveClickListener = listener;
		if (cmds != null) {
			mSureCmds = cmds;
		}
	}

	public void setNegativeButton(String text, android.view.View.OnClickListener listener, String... cmds) {
		btnCancel.setVisibility(View.VISIBLE);
		btnCancel.setText(text);
		btnCancel.setOnClickListener(listener);
		mNegativeClickListener = listener;
		if (cmds != null) {
			mCancelCmds = cmds;
		}
	}

	@Override
	protected void onGetFocus() {
		regWakeupAsr();
		super.onGetFocus();
	}

	@Override
	protected void onLoseFocus() {
		unregWakeupAsr();
		super.onLoseFocus();
	}
}
