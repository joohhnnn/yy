package com.txznet.comm.ui.theme.test.view;

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
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.NoTtsQrcodeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.INoTtsQrcodeView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;

public class NoTtsQrcodeView extends INoTtsQrcodeView{
	
	private static NoTtsQrcodeView sInstance = new NoTtsQrcodeView();
	
	private NoTtsQrcodeView(){
	}
	
	public static NoTtsQrcodeView getInstance(){
		return sInstance;
	}
	
	private TextView tvTitle;

	private ImageView qrIv;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		NoTtsQrcodeViewData viewData = (NoTtsQrcodeViewData) data;
		
		LinearLayout container = new LinearLayout(GlobalContext.get());
		container.setOrientation(LinearLayout.VERTICAL);
		container.addView(createTopView());
		container.addView(createBottomView());
		container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		tvTitle.setText(LanguageConvertor.toLocale(viewData.title));
		refreshQRCodeView(viewData.qrCode);
		qrIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, 
						TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TTS_QRCODE, 0, 0);
			}
		});
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = container;
		adapter.object = NoTtsQrcodeView.getInstance();
		return adapter;
	}
	
	private View createTopView() {
		int tvHeight = (int) LayouUtil.getDimen("y100");
		int textSize = (int) LayouUtil.getDimen("y20");
		int leftMargin = (int) LayouUtil.getDimen("x20");

		tvTitle = new TextView(GlobalContext.get());
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, tvHeight);
		lp.leftMargin = leftMargin;
		tvTitle.setLayoutParams(lp);
		tvTitle.setGravity(Gravity.CENTER_VERTICAL);

		TextViewUtil.setTextSize(tvTitle,textSize);
		TextViewUtil.setTextColor(tvTitle,Color.parseColor("#aaaaaa"));
		return tvTitle;
	}

	private View createBottomView() {
		LinearLayout container = new LinearLayout(GlobalContext.get());
		container.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
		params.gravity = Gravity.CENTER;
		container.setLayoutParams(params);
		container.addView(createQRCodeView());
		return container;
	}

	private View createQRCodeView() {
		int qrWidth = (int) LayouUtil.getDimen("y256");
		int qrHeight = (int) LayouUtil.getDimen("y256");
		
		LinearLayout qrContainer = new LinearLayout(GlobalContext.get());
		qrContainer.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
		params.gravity = Gravity.CENTER;
		qrContainer.setGravity(Gravity.CENTER);
		qrContainer.setLayoutParams(params);

		qrIv = new ImageView(GlobalContext.get());
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
			int h = (int) LayouUtil.getDimen("y300");
			final Bitmap qrb = QRUtil.createQRCode(url, h);
			UI2Manager.runOnUIThread(new Runnable() {

				@Override
				public void run() {
					if (qrIv == null || qrb == null) {
						return;
					}
					qrIv.setImageBitmap(qrb);
				}
			}, 0);
		} catch (WriterException e) {
		}
	}
	
	@Override
	public void init() {
		super.init();
	}
	
}
