package com.txznet.txz.component.advertising.view;

import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.txz.R;
import com.txznet.txz.component.advertising.util.AdvertisingUtils;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.util.DeviceInfo;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class BannerAdvertisingView {
    private static final int SOURCE_IMAGE_TYPE = 1;
    private static final int SOURCE_GIF_TYPE = 2;
    private static final int SOURCE_VIDEO_TYPE = 3;

    public static View createView(int type, String url) {
        View view = null;
        switch (type) {
            case SOURCE_IMAGE_TYPE:
                view = createImageTypeView(url);
                break;
            case SOURCE_GIF_TYPE:
                view = createGifTypeView(url);
                break;
            case SOURCE_VIDEO_TYPE:

                break;
            default:
                break;

        }
        return view;
    }

    private static View createImageTypeView(String url) {
        RelativeLayout layout = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(getWidth(), getHeight());
        layout.setLayoutParams(llParams);
//        Bitmap bitmap = AdvertisingUtils.cropBitmap(AdvertisingUtils.getBitmap(url),getHeight());
        layout.setBackground(AdvertisingUtils.getDrawable(url, getHeight(), getWidth()));
        Button btn = new Button(GlobalContext.get());
        btn.setBackgroundResource(R.drawable.selector_advertising_close);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m56"),
                (int) LayouUtil.getDimen("m56"));
//        params.setMargins(0,(int)LayouUtil.getDimen("m7"),(int)LayouUtil.getDimen("m7"),0);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeBannerAd();
            }
        });
        layout.addView(btn);

        TextView textView = new TextView(GlobalContext.get());
        textView.setText("广告");
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(LayouUtil.getDrawable("shape_advertising_text"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m11"));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x31"),
                (int) LayouUtil.getDimen("y19"));
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tvParams.setMargins(0, 0, (int) LayouUtil.getDimen("y8"), (int) LayouUtil.getDimen("y8"));
        textView.setLayoutParams(tvParams);
        layout.addView(textView);
        return layout;
    }

    private static View createGifTypeView(String url) {
        RelativeLayout rl = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(getWidth(), getHeight());
        rlParams.setMargins(0, 0, 0, 0);
        rl.setLayoutParams(rlParams);
        GifImageView gifImageView = new GifImageView(GlobalContext.get());
        gifImageView.setScaleType(GifImageView.ScaleType.CENTER);
        RelativeLayout.LayoutParams gifParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        gifParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        gifImageView.setLayoutParams(gifParams);
        File file = new File(url);
        try {
            GifDrawable drawable = new GifDrawable(file);
            gifImageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        rl.addView(gifImageView);
        ImageView iv = new ImageView(GlobalContext.get());
        iv.setBackgroundResource(R.drawable.selector_advertising_close);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m56"),
                (int) LayouUtil.getDimen("m56"));
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        params.setMargins(0,(int)LayouUtil.getDimen("m7"),(int)LayouUtil.getDimen("m7"),0);
        iv.setLayoutParams(params);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeBannerAd();
            }
        });
        rl.addView(iv);
        TextView textView = new TextView(GlobalContext.get());
        textView.setText("广告");
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(LayouUtil.getDrawable("shape_advertising_text"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m11"));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x31"),
                (int) LayouUtil.getDimen("y19"));
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tvParams.setMargins(0, 0, (int) LayouUtil.getDimen("y8"), (int) LayouUtil.getDimen("y8"));
        textView.setLayoutParams(tvParams);
        rl.addView(textView);
        return rl;
    }

    public static int getWidth() {
        return getHeight() * 4;
    }

    public static int getHeight() {
        return DeviceInfo.getScreenHeight() / 4;
    }

}
