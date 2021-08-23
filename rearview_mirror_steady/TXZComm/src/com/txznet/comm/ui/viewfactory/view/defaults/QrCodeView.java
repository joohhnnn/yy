package com.txznet.comm.ui.viewfactory.view.defaults;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.QrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IQrCodeView;
import com.txznet.txz.util.QRUtil;

public class QrCodeView extends IQrCodeView{
	
	private static QrCodeView sInstance = new QrCodeView();
	
	private QrCodeView(){
	}

	public static QrCodeView getInstance(){
		return sInstance;
	}
	
	ImageView iv;
	LinearLayout llOut;
	LinearLayout llIn;
	@Override
	public ViewAdapter getView(ViewData data) {
		QrCodeViewData viewData = (QrCodeViewData) data;
		llOut = new LinearLayout(GlobalContext.get());
		llOut.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
		llOut.setGravity(Gravity.CENTER_HORIZONTAL);
		llIn = new LinearLayout(GlobalContext.get());
		llIn.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
		llIn.setBackgroundDrawable(LayouUtil.getDrawable("shape_qrcode_bg"));
		llIn.setGravity(Gravity.CENTER_HORIZONTAL);
		int paddingOut = (int) LayouUtil.getDimen("y15");
		llIn.setPadding(paddingOut, paddingOut, paddingOut, paddingOut);
		iv = new ImageView(GlobalContext.get());
		iv.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
		iv.setBackgroundColor(Color.WHITE);
		iv.setScaleType(ScaleType.CENTER);
		int paddingIn = (int) LayouUtil.getDimen("y5");
		iv.setPadding(paddingIn, paddingIn, paddingIn, paddingIn);
		llOut.addView(llIn);
		llIn.addView(iv);
		int h = (int) LayouUtil.getDimen("y150");
		try {
			final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.qrCode, h);
			UI2Manager.runOnUIThread(new Runnable() {
				
				@Override
				public void run() {
					if (iv == null || bitmap == null) {
						return;
					}
					iv.setImageBitmap(bitmap);
					int padding = (int) LayouUtil.getDimen("y5");
					iv.setPadding(padding, padding, padding, padding);
				}
			}, 0);
		} catch (WriterException e) {
		}
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = llOut;
		adapter.object = QrCodeView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		super.init();
	}
	
}
