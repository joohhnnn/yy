package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ILogoQrCodeView;
import com.txznet.comm.ui.viewfactory.view.defaults.QrCodeView;
import com.txznet.txz.util.QRUtil;

public class LogoQrCodeView extends ILogoQrCodeView {
    private static LogoQrCodeView sInstance = new LogoQrCodeView();
    private int mWidth;

    public LogoQrCodeView(){

    }

    public static LogoQrCodeView getInstance() {
        return sInstance;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        LogoQrCodeViewData viewData = (LogoQrCodeViewData) data;

        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);
        Bitmap logoBitmap = BitmapFactory.decodeResource(LayouUtil.getModifyContext().getResources(), getIdByName("logo"));
        int logoWidth = (int) LayouUtil.getDimen("m23");
        int width = mWidth;
        Bitmap bitmap = QRUtil.createQRCodeWithLogo(viewData.qrCode,width,0,logoBitmap,logoWidth);
        ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setBackgroundColor(Color.WHITE);
        int padding = (int) LayouUtil.getDimen("m8");
        imageView.setPadding(padding, padding, padding, padding);
        params = new LinearLayout.LayoutParams(width + padding, width + padding);
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

    public int getIdByName(String name){
        int id = LayouUtil.getModifyContext().getResources().getIdentifier(name,"drawable","com.txznet.resholder");
        return id;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
        switch (styleIndex){
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                mWidth = (int) LayouUtil.getDimen("m136");
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                mWidth = (int) LayouUtil.getDimen("m120");
                break;
            default:
                break;
        }
    }
}
