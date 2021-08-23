package com.txznet.comm.ui.viewfactory.view.defaults;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ILogoQrCodeView;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.QRUtil;

public class DefaultLogoQrCodeView extends ILogoQrCodeView {
    private static DefaultLogoQrCodeView sInstance = new DefaultLogoQrCodeView();

    public DefaultLogoQrCodeView(){

    }

    public static DefaultLogoQrCodeView getInstance() {
        return sInstance;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        LogoQrCodeViewData viewData = (LogoQrCodeViewData) data;

        LinearLayout layout = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        Bitmap logoBitmap = BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.logo);
        int logoWidth = (int) LayouUtil.getDimen("m23");
        int width = (int) LayouUtil.getDimen("m136");
        Bitmap bitmap = QRUtil.createQRCodeWithLogo(viewData.qrCode,width,4,logoBitmap,logoWidth);
        ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setBackgroundColor(Color.WHITE);
        int padding = (int) LayouUtil.getDimen("m8");
        imageView.setPadding(padding, padding, padding, padding);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = (int) LayouUtil.getDimen("m16");
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(bitmap);
        layout.addView(imageView);

        ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
        adapter.type = data.getType();
        adapter.view = layout;
        adapter.object = QrCodeView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }
}
