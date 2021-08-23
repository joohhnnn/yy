package com.txznet.webchat.util;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.txznet.loader.AppLogic;

/**
 * Created by J on 2017/4/20.
 */

public class ImageUtil {
    public static void showImageString(final ImageView imageView, final String str, final int defaultResId) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                final Bitmap bmp = Base64Converter.string2Bitmap(str);
                if (null != imageView) {
                    /*imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (null == bmp) {
                                imageView.setImageResource(defaultResId);
                            } else {
                                imageView.setImageBitmap(bmp);
                            }
                        }
                    });*/

                    AppLogic.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            if (null == bmp) {
                                imageView.setImageResource(defaultResId);
                            } else {
                                imageView.setImageBitmap(bmp);
                            }
                        }
                    }, 0);
                }
            }
        }, 0);
    }
}
