package com.txznet.txz.module.ui.view.plugin.sample;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.record.util.QRCodeHandler;
import com.txznet.txz.comm.R;
import com.txznet.txz.module.ui.view.BPView;
import com.txznet.txz.util.runnables.Runnable2;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TTSNoResultView extends BPView {
	public static final int ID_TOP_VIEW = 0x0001;
	public static final int ID_QRCODE_VIEW = 0x0002;
	public static final int ID_NOTICE_VIEW = 0x0003;

	public TTSNoResultView(String jsonData) {
		super(GlobalContext.get(), jsonData);
	}

	@Override
	public View createView() {
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(VERTICAL);
		container.addView(createTopView());
		container.addView(createBottomView());
		container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return container;
	}

	@Override
	public LayoutParams getLayoutParams() {
		return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	private View createTopView() {
		int tvHeight = (int) getContext().getResources().getDimension(R.dimen.y100);
		int textSize = (int) getContext().getResources().getDimension(R.dimen.y20);
		int leftMargin = (int) getContext().getResources().getDimension(R.dimen.x20);

		TextView tv = new TextView(getContext());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, tvHeight);
		lp.leftMargin = leftMargin;
		tv.setLayoutParams(lp);
		tv.setGravity(Gravity.CENTER_VERTICAL);

		tv.setTextSize(textSize);
		tv.setTextColor(Color.parseColor("#aaaaaa"));
		tv.setId(ID_TOP_VIEW);
		return tv;
	}

	private View createBottomView() {
		LinearLayout container = new LinearLayout(getContext());
		container.setOrientation(HORIZONTAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
		params.gravity = Gravity.CENTER;
		container.setLayoutParams(params);
		container.addView(createQRCodeView());
		return container;
	}

	private View createQRCodeView() {
		int qrWidth = (int) getContext().getResources().getDimension(R.dimen.y256);
		int qrHeight = (int) getContext().getResources().getDimension(R.dimen.y256);
		
		LinearLayout qrContainer = new LinearLayout(getContext());
		qrContainer.setOrientation(HORIZONTAL);
		LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
		params.gravity = Gravity.CENTER;
		qrContainer.setGravity(Gravity.CENTER);
		qrContainer.setLayoutParams(params);

		final ImageView qrIv = new ImageView(getContext());
		LayoutParams lp = (LayoutParams) qrIv.getLayoutParams();
		if (lp == null) {
			lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		lp.width = qrWidth;
		lp.height = qrHeight;
		lp.gravity = Gravity.CENTER;
		qrIv.setLayoutParams(lp);
		qrIv.setScaleType(ScaleType.MATRIX);
		qrIv.setBackgroundColor(Color.WHITE);
		qrIv.setId(ID_QRCODE_VIEW);
		qrIv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				if (qrIv.getWidth() == 0 || qrIv.getHeight() == 0) {
					return;
				}
				if (qrIv.getDrawable() != null) {
					qrIv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					float width = qrIv.getWidth();
					float height = qrIv.getHeight();
					float imgWidth = qrIv.getDrawable().getIntrinsicWidth();
					float imgHeight = qrIv.getDrawable().getIntrinsicHeight();
					Matrix matrix = new Matrix();
					// 移动到中心点
					matrix.preTranslate(width / 2 - imgWidth / 2, height / 2 - imgHeight / 2);
					// 拉伸到最大面积 + 额外
					matrix.postScale(width / imgWidth + 0.15f, height / imgHeight + 0.15f, width / 2, height / 2);
					qrIv.setImageMatrix(matrix);
				}
			}
		});
		qrContainer.addView(qrIv);
		return qrContainer;
	}

	/**
	 * 刷新二维码
	 * 
	 * @param url
	 */
	public void refreshQRCodeView(String url) {
		if (TextUtils.isEmpty(url)) {
			return;
		}

		try {
			int h = (int) getContext().getResources().getDimension(R.dimen.y300);
			final Bitmap qrb = QRCodeHandler.createQRCode(url, h);
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					ImageView qrIv = (ImageView) findViewById(ID_QRCODE_VIEW);
					if (qrIv == null || qrb == null) {
						return;
					}
					qrIv.setImageBitmap(qrb);
				}
			}, 0);
		} catch (WriterException e) {
		}
	}

	/**
	 * jsonData: title: qrCode
	 */
	@Override
	public void refreshView(final View contentView, String jsonData) {
		if (contentView == null) {
			return;
		}

		if (TextUtils.isEmpty(jsonData)) {
			return;
		}

		String title = "";
		String qrCode = "";
		String notice = "";

		try {
			JSONObject jObj = new JSONObject(jsonData);
			title = jObj.optString("title");
			qrCode = jObj.optString("qrCode");
			notice = jObj.optString("notice");
		} catch (JSONException e) {
		}
		AppLogic.runOnUiGround(new Runnable2<String, String>(title, notice) {

			@Override
			public void run() {
				final TextView titleTv = (TextView) contentView.findViewById(ID_TOP_VIEW);
				final TextView noticeTv = (TextView) contentView.findViewById(ID_NOTICE_VIEW);

				if (titleTv != null) {
					titleTv.setText(mP1);
				}

				if (noticeTv != null) {
					noticeTv.setText(mP2);
				}
			}
		}, 0);

		refreshQRCodeView(qrCode);
	}
}