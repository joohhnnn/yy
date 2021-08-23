package com.txznet.webchat.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * @author Ryan Tang
 */
public final class QRCodeHandler {
    private static final int BLACK = 0xff000000;
    private static final int WHILE = 0xffffffff;

    public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = BLACK;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap createQrImage(String url, int width) {
        if (url == null && "".equals(url)) {
            return null;
        }

        Hashtable<EncodeHintType, String> hint = new Hashtable<EncodeHintType, String>();
        hint.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, width, hint);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        if (bitMatrix == null) {
            return null;
        }

        int[] pixels = new int[width * width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                if (bitMatrix.get(i, j)) {
                    pixels[i * width + j] = BLACK;
                } else {
                    pixels[i * width + j] = WHILE;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, width, Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, width);
        return bitmap;
    }
}
