package com.txznet.txz.util;

import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.txznet.loader.AppLogicBase;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.Nullable;
import android.widget.ImageView;

public class QRUtil {
	private static final int BLACK = 0xff000000;
	private static final int WHILE = 0xffffffff;
	
	@Deprecated
	public static Bitmap createQRCode(String str,int widthAndHeight) throws WriterException {
		return createQRCodeBitmap(str, widthAndHeight, 4);
	}
	
	
	public static Bitmap createQRCodeBitmap(String str,int widthAndHeight) throws WriterException {
		return createQRCodeBitmap(str, widthAndHeight, 0);
	}
	
	/**
	 * 生成二维码的bitmap
	 * @param str 内容
	 * @param widthAndHeight 生成二维码的宽高 单位px
	 * @param margin 生成二维码的白边 取值0-4
	 * @return 生成的bitmap
	 * @throws WriterException 异常情况
	 */
	public static Bitmap createQRCodeBitmap(String str,int widthAndHeight, int margin) throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();  
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8"); 
		hints.put(EncodeHintType.MARGIN, margin);
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				}else {
					pixels[y * width + x] = WHILE;
				}
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public static void showQrCode(final ImageView imageView, final String str,
								  final int widthAndHeight, final int margin) {
		AppLogicBase.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				try {
					final Bitmap bitmap = createQRCodeBitmap(str, widthAndHeight, margin);
					imageView.post(new Runnable() {
						@Override
						public void run() {
							imageView.setImageBitmap(bitmap);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static Bitmap createQRCodeBitmapNoWhite(String str,int widthAndHeight) throws WriterException {
		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		hints.put(EncodeHintType.MARGIN, 1);
		BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
		matrix = deleteWhite(matrix);
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					pixels[y * width + x] = BLACK;
				}else {
					pixels[y * width + x] = WHILE;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	private static BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1]))
					resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	/**
	 * 生成带logo的二维码，logo默认为二维码的1/5
	 *
	 * @param text           需要生成二维码的文字、网址等
	 * @param widthAndHeight 需要生成二维码的大小（）
	 * @param logoBitmap     logo文件
	 * @return bitmap
	 */
	public static Bitmap createQRCodeWithLogo(String text, int widthAndHeight, int margin, Bitmap logoBitmap, int logoWidth) {
		return createQRCodeWithLogo(text, widthAndHeight, margin, null, logoBitmap, logoWidth);
	}

	/**
	 * 生成带logo的二维码，logo默认为二维码的1/5
	 *
	 * @param text           需要生成二维码的文字、网址等
	 * @param widthAndHeight 需要生成二维码的大小（）
	 * @param logoBitmap     logo文件
	 * @param errorCorrectionLevel 二维码的纠错级别，由于添加了logo后会导致识别不到，最好设置下这个的值。 可为null，null的时候使用的是默认值，即L。
	 * @return bitmap
	 */
	public static Bitmap createQRCodeWithLogo(String text, int widthAndHeight, int margin, @Nullable ErrorCorrectionLevel errorCorrectionLevel, Bitmap logoBitmap, int logoWidth) {
		try {
			Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			/*
			 * 设置容错级别，默认为ErrorCorrectionLevel.L
			 * 因为中间加入logo所以建议你把容错级别调至H,否则可能会出现识别不了
			 */
			if (errorCorrectionLevel != null) {
				hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
			}
			// 设置空白边距的宽度
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
			//设置空白边距的宽度
			hints.put(EncodeHintType.MARGIN, margin); //default is 4
			BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);
			bitMatrix = deleteWhite(bitMatrix);
			int width = bitMatrix.getWidth();//矩阵高度
			int height = bitMatrix.getHeight();//矩阵宽度

			Matrix m = new Matrix();
			float sx = (float) logoWidth / logoBitmap.getWidth();
			float sy = (float) logoWidth
					/ logoBitmap.getHeight();
			m.setScale(sx, sy);
			//设置缩放信息
			//将logo图片按martix设置的信息缩放
			logoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0,
					logoBitmap.getWidth(), logoBitmap.getHeight(), m, false);

			int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (bitMatrix.get(x, y)) {
						pixels[y * width + x] = BLACK;
					} else {
						pixels[y * width + x] = WHILE;
					}
				}
			}
			Bitmap bitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return addLogo(bitmap, widthAndHeight, logoBitmap, logoWidth);
		} catch (WriterException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Bitmap addLogo(Bitmap qrCodeBitmap, int qrCodeWidth, Bitmap logoBitmap, int logoWidth) {
		if (qrCodeBitmap == null) {
			return null;
		}

		if (logoBitmap == null) {
			return qrCodeBitmap;
		}

		Matrix m = new Matrix();
		float sx = (float) logoWidth / logoBitmap.getWidth();
		float sy = (float) logoWidth
				/ logoBitmap.getHeight();
		m.setScale(sx, sy);
		//设置缩放信息
		//将logo图片按martix设置的信息缩放
		logoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0,
				logoBitmap.getWidth(), logoBitmap.getHeight(), m, false);

		if (qrCodeWidth == 0) {
			return null;
		}

		if (logoWidth == 0) {
			return qrCodeBitmap;
		}

		Matrix m1 = new Matrix();
		float sx1 = (float) qrCodeWidth / qrCodeBitmap.getWidth();
		float sy1 = (float) qrCodeWidth
				/ qrCodeBitmap.getHeight();
		m1.setScale(sx1, sy1);
		//设置缩放信息
		//将logo图片按martix设置的信息缩放
		qrCodeBitmap = Bitmap.createBitmap(qrCodeBitmap, 0, 0,
				qrCodeBitmap.getWidth(), qrCodeBitmap.getHeight(), m1, false);

		Bitmap bitmap = Bitmap.createBitmap(qrCodeWidth, qrCodeWidth, Bitmap.Config.ARGB_4444);
		try {
			Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(qrCodeBitmap, 0, 0, null);
			canvas.drawBitmap(logoBitmap, (qrCodeWidth - logoWidth) / 2, (qrCodeWidth - logoWidth) / 2, null);

			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
		} catch (Exception e) {
			bitmap = null;
			e.getStackTrace();
		}
		return bitmap;
	}
}
