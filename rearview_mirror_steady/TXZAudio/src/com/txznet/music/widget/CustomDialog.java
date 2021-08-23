package com.txznet.music.widget;

import com.txznet.music.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class CustomDialog extends Dialog {

	private View view;
	private TextView tvSure;
	private TextView tvCancle;
	private TextView tvMsg;
	private Window window;
	private LayoutParams lp;

	/**
	 * Context 可以使用堆栈来保存当前所在的Activity 并使用
	 * 
	 * @param context
	 */
	public CustomDialog(Context context) {
		super(context, R.style.gt_dialog);

		view = View.inflate(context, R.layout.dialog_ok_cancle, null);
		tvSure = (TextView) view.findViewById(R.id.tv_sure);
		tvCancle = (TextView) view.findViewById(R.id.tv_cancle);
		tvMsg = (TextView) view.findViewById(R.id.tv_msg);
		setContentView(view);
		window = getWindow();
		lp = window.getAttributes();
		lp.width = 500;// 设置弹框的高度
		window.setAttributes(lp);
	}

	public CustomDialog setSureListener(View.OnClickListener listener) {
		tvSure.setOnClickListener(listener);
		return this;
	}

	public CustomDialog setCancleListener(View.OnClickListener listener) {
		tvCancle.setOnClickListener(listener);
		return this;
	}

	public CustomDialog setMessage(String message) {
		tvMsg.setText(message);
		return this;
	}
}
