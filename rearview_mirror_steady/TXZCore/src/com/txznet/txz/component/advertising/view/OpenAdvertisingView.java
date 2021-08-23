package com.txznet.txz.component.advertising.view;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.advertising.util.AdvertisingUtils;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.util.DeviceInfo;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


public class OpenAdvertisingView {
    public static final int SOURCE_IMAGE_TYPE = 1;
    public static final int SOURCE_GIF_TYPE = 2;
    public static final int SOURCE_VIDEO_TYPE = 3;

    public static View createView(int type, String url, String btnText, String redirectUrl) {
        View view = null;
        switch (type) {
            case SOURCE_IMAGE_TYPE:
                if (AdvertisingUtils.compareWidthAndHeight(url)) {
                    view = createImageTypeView(url, btnText, redirectUrl);
                } else {
                    view = createImageTypeView2(url, btnText, redirectUrl);
                }
                break;
            case SOURCE_GIF_TYPE:
                if (AdvertisingUtils.compareWidthAndHeight(url)) {
                    view = createGifTypeView(url, btnText, redirectUrl);
                } else {
                    view = createGifTypeView2(url, btnText, redirectUrl);
                }
                break;
            case SOURCE_VIDEO_TYPE:

                break;
            default:
                break;

        }
        return view;
    }

    private static View createImageTypeView(String url, String btnText, final String redirectUrl) {
        RelativeLayout layout = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        layout.setBackground(AdvertisingUtils.getDrawable(url,DeviceInfo.getScreenHeight(),DeviceInfo.getScreenWidth()));
//        layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AppLogic.runOnUiGround(new Runnable() {
//                    @Override
//                    public void run() {
//                        WebViewDialog webViewDialog = new WebViewDialog();
//                        webViewDialog.setUrl(redirectUrl);
//                        webViewDialog.show();
//                        AdvertisingManager.getInstance().closeOpenAd();
//                    }
//                });
//            }
//        });
        Button btn = new Button(GlobalContext.get());
        btn.setText(btnText);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m16"));
        btn.setBackgroundResource(R.drawable.shape_advertising_back);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x94"),
                (int) LayouUtil.getDimen("y40"));
        params.setMargins(0, (int) LayouUtil.getDimen("y16"), (int) LayouUtil.getDimen("y16"), 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeOpenAd();
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
        tvParams.setMargins(0,0,(int) LayouUtil.getDimen("y8"),(int) LayouUtil.getDimen("y8"));
        textView.setLayoutParams(tvParams);
        layout.addView(textView);
        return layout;
    }

    private static View createImageTypeView2(String url, String btnText, final String redirectUrl) {
        RelativeLayout layout = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(layoutParams);
        layout.setBackgroundColor(Color.parseColor("#1C1C23"));
//        layout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AppLogic.runOnUiGround(new Runnable() {
//                    @Override
//                    public void run() {
//                        WebViewDialog webViewDialog = new WebViewDialog();
//                        webViewDialog.setUrl(redirectUrl);
//                        webViewDialog.show();
//                        AdvertisingManager.getInstance().closeOpenAd();
//                    }
//                });
//            }
//        });

        ImageView iv = new ImageView(GlobalContext.get());
        iv.setImageBitmap(AdvertisingUtils.getBitmap(url, DeviceInfo.getScreenHeight(), DeviceInfo.getScreenWidth()));
        int width = (int) (DeviceInfo.getScreenWidth() * 0.64);
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(width, RelativeLayout.LayoutParams.MATCH_PARENT);
        ivParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        iv.setLayoutParams(ivParams);
        iv.setScaleType(ImageView.ScaleType.FIT_XY);
        layout.addView(iv);

        Button btn = new Button(GlobalContext.get());
        btn.setText(btnText);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m16"));
        btn.setBackgroundResource(R.drawable.shape_advertising_back);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x94"),
                (int) LayouUtil.getDimen("y40"));
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.setMargins(0, (int) LayouUtil.getDimen("y16"), (int) LayouUtil.getDimen("y16"), 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeOpenAd();
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
        int marginRight = (int) ((DeviceInfo.getScreenWidth() * 0.18) + LayouUtil.getDimen("y8"));
        tvParams.setMargins(0, 0, marginRight, (int) LayouUtil.getDimen("y8"));
        textView.setLayoutParams(tvParams);
        layout.addView(textView);
        return layout;
    }


    private static View createGifTypeView(String url, String btnText, final String redirectUrl) {
        RelativeLayout rl = new RelativeLayout(GlobalContext.get());
        LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        rl.setLayoutParams(rlParams);
//        rl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AppLogic.runOnUiGround(new Runnable() {
//                    @Override
//                    public void run() {
//                        WebViewDialog webViewDialog = new WebViewDialog();
//                        webViewDialog.setUrl(redirectUrl);
//                        webViewDialog.show();
//                        AdvertisingManager.getInstance().closeOpenAd();
//                    }
//                });
//            }
//        });
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
        Button btn = new Button(GlobalContext.get());
        btn.setText(btnText);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m16"));
        btn.setBackgroundResource(R.drawable.shape_advertising_back);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x94"),
                (int) LayouUtil.getDimen("y40"));
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.setMargins(0, (int) LayouUtil.getDimen("y16"), (int) LayouUtil.getDimen("y16"), 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeOpenAd();
            }
        });
        rl.addView(btn);

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

    private static View createGifTypeView2(String url, String btnText, final String redirectUrl) {
        RelativeLayout rl = new RelativeLayout(GlobalContext.get());
        rl.setBackgroundColor(Color.parseColor("#1C1C23"));
        LinearLayout.LayoutParams rlParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        rl.setLayoutParams(rlParams);
//        rl.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AppLogic.runOnUiGround(new Runnable() {
//                    @Override
//                    public void run() {
//                        WebViewDialog webViewDialog = new WebViewDialog();
//                        webViewDialog.setUrl(redirectUrl);
//                        webViewDialog.show();
//                        AdvertisingManager.getInstance().closeOpenAd();
//                    }
//                });
//            }
//        });
        GifImageView gifImageView = new GifImageView(GlobalContext.get());
        gifImageView.setScaleType(GifImageView.ScaleType.FIT_XY);
        int width = (int) (DeviceInfo.getScreenWidth() * 0.64);
        RelativeLayout.LayoutParams gifParams = new RelativeLayout.LayoutParams(width,
                RelativeLayout.LayoutParams.MATCH_PARENT);
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
        Button btn = new Button(GlobalContext.get());
        btn.setText(btnText);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m16"));
        btn.setBackgroundResource(R.drawable.shape_advertising_back);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x94"),
                (int) LayouUtil.getDimen("y40"));
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.setMargins(0, (int) LayouUtil.getDimen("y16"), (int) LayouUtil.getDimen("y16"), 0);
        btn.setLayoutParams(params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvertisingManager.getInstance().closeOpenAd();
            }
        });
        rl.addView(btn);

        TextView textView = new TextView(GlobalContext.get());
        textView.setText("广告");
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(LayouUtil.getDrawable("shape_advertising_text"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m11"));
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x31"),
                (int) LayouUtil.getDimen("y19"));
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tvParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        int marginRight = (int) ((DeviceInfo.getScreenWidth() * 0.18) + LayouUtil.getDimen("y8"));
        tvParams.setMargins(0, 0, marginRight, (int) LayouUtil.getDimen("y8"));
        textView.setLayoutParams(tvParams);
        rl.addView(textView);
        return rl;
    }
}
