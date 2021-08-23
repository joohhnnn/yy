package com.txznet.txz.ui.widget;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.txz.R;
import com.txznet.txz.util.QRUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 显示sim卡充值二维码的Dialog
 * @author J
 *
 */
public class SimRechargeQRDialog extends WinDialog{
	private TextView mTvTitle;
	private TextView mTvPrice;
	private TextView mTvPriceRaw;
	private ImageView mIvQR;
	LinearLayout mRlRoot;
	
	private static final String WAKEUP_EXIT_TASK_ID = "TASK_SIM_RECHARGE_DIALOG_EXIT";
	
	private String mTitle;
	private String mQRStr;
	private Bitmap mQRBitmap;
	private double mPrice;
	private double mPriceRaw;
	
	public SimRechargeQRDialog(String title, String qr, double price, double priceRaw) {
		super(new DialogBuildData(), true);
		mQRStr = qr;
		mTitle = title;
		mPrice = price;
		mPriceRaw = priceRaw;
		try {
			mQRBitmap = QRUtil.createQRCode(mQRStr, 350);
		} catch (WriterException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected View createView() {
		View v = LayoutInflater.from(getContext()).inflate(R.layout.win_sim_recharge_qr, null);
		// bind view components
		mTvTitle = (TextView) v.findViewById(R.id.tv_sim_recharge_title);
		mTvPrice = (TextView) v.findViewById(R.id.tv_sim_recharge_price);
		mTvPriceRaw = (TextView) v.findViewById(R.id.tv_sim_recharge_price_raw);
		mIvQR = (ImageView) v.findViewById(R.id.iv_sim_recharge_qr);
		mRlRoot = (LinearLayout) v.findViewById(R.id.rl_sim_recharge_root);
		
		// init listeners
		mRlRoot.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss("onClick");
			}
		});
		
		return v;
	}

	@Override
	public void onShow() {
		mTvTitle.setText(mTitle);
		mIvQR.setImageBitmap(mQRBitmap);
		mTvPrice.setText("￥" + mPrice);
		mTvPriceRaw.setText("￥" + mPriceRaw);
		mTvPriceRaw.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
		
		IntentFilter filter = new IntentFilter("com.txznet.txz.record.show");
		getContext().registerReceiver(mRecordWinReceiver, filter);
		
		AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
			
			@Override
			public void onCommandSelected(String type, String command) {
				dismiss("onCommandSelected");
			}

			@Override
			public boolean needAsrState() {
				return true;
			}
			
			@Override
			public String getTaskId() {
				return WAKEUP_EXIT_TASK_ID;
			}
		}.addCommand("CMD_EXIT", "关闭", "取消"));
		
		super.show();
	}

	@Override
	public void onDismiss() {
		AsrUtil.recoverWakeupFromAsr(WAKEUP_EXIT_TASK_ID);
		getContext().unregisterReceiver(mRecordWinReceiver);
		
		super.onDismiss();;
	}
	
	BroadcastReceiver mRecordWinReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			dismiss("record win show");
		}
	};

	@Override
	public String getReportDialogId() {
		return "sim_recharge";
	}
}
