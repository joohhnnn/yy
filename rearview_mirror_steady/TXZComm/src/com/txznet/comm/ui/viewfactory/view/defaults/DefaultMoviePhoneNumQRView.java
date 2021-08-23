package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMoviePhoneNumQRView;
import com.txznet.comm.ui.viewfactory.data.MoviePhoneNumQRViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.QRUtil;

public class DefaultMoviePhoneNumQRView extends IMoviePhoneNumQRView {

    private static DefaultMoviePhoneNumQRView sInstance = new DefaultMoviePhoneNumQRView();

    private DefaultMoviePhoneNumQRView(){}

    public static DefaultMoviePhoneNumQRView getInstance(){
        return sInstance;
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MoviePhoneNumQRViewData viewData = (MoviePhoneNumQRViewData) data;
        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setOrientation(LinearLayout.VERTICAL);
        lyContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        LinearLayout.LayoutParams contentsLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.setLayoutParams(contentsLP);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("手机号验证");
        tvTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTitle,LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.WHITE);
        lyContents.addView(tvTitle,layoutParams);

        RelativeLayout rlContents = new RelativeLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,5);
        lyContents.addView(rlContents,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("divider_h"));
        RelativeLayout.LayoutParams rlLayParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        rlLayParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rlContents.addView(divider,rlLayParams);
        LinearLayout lyContent = new LinearLayout(GlobalContext.get());
        lyContent.setOrientation(LinearLayout.HORIZONTAL);
        lyContent.setGravity(Gravity.CENTER);
        rlLayParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        rlLayParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        rlContents.addView(lyContent,rlLayParams);

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int h = (int) LayouUtil.getDimen("m260");
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.phoneNumQRUrl, h,1);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyContent.addView(imageView,layoutParams);
        TextView tvHint = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvHint,LayouUtil.getDimen("m21"));
        tvHint.setTextColor(Color.WHITE);
        tvHint.setSingleLine(false);
        tvHint.setText("注意：填写电话号码过程中请勿关闭此页面！");
        layoutParams.leftMargin = (int) LayouUtil.getDimen("x24");
        lyContent.addView(tvHint,layoutParams);
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = lyContents;
        viewAdapter.object = DefaultMoviePhoneNumQRView.getInstance();
        return viewAdapter;
    }
}
