package com.txznet.txz.util;

import java.io.File;
import java.io.FileOutputStream;

import com.txznet.txz.jni.JNIHelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class ImageUtil {

	public static void saveJpeg(Bitmap bitmap, String dst) {
		saveJpeg(bitmap, dst, 80);
	}

	public static void saveJpeg(Bitmap bitmap, String dst, int quality) {
		File file = new File(dst);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)) {
				fos.flush();
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Bitmap resizeBitmapAlignWidth(Bitmap bitmap, int newWidth) {
		int width = bitmap.getWidth();

		float scaleWidth = ((float) newWidth) / width;

		return resizeBitmap(bitmap, scaleWidth, scaleWidth);
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		return resizeBitmap(bitmap, scaleWidth, scaleHeight);
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, float scaleWidth,
			float scaleHeight) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	}

	public static Bitmap resizeBitmap(Bitmap bitmap, float scale) {
		return resizeBitmap(bitmap, scale, scale);
	}

	public static Bitmap loadBitmap(String pathName) {
		return BitmapFactory.decodeFile(pathName);
	}

	public static void resizeImage(String src, String dst, int newWidth,
			int newHeight, int quality) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmap(bitmap, newWidth, newHeight);
		bitmap.recycle();
		saveJpeg(newBitmap, dst, quality);
		newBitmap.recycle();
	}

	public static void resizeImage(String src, String dst, float scaleWidth,
			float scaleHeight, int quality) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmap(bitmap, scaleWidth, scaleHeight);
		bitmap.recycle();
		saveJpeg(newBitmap, dst, quality);
		newBitmap.recycle();
	}

	public static void resizeImage(String src, String dst, float scale,
			int quality) {
		Bitmap bitmap = loadBitmap(src);
		if(bitmap == null){
			JNIHelper.loge("capturePicture error, Unable to decode " + src);
		}
		Bitmap newBitmap = resizeBitmap(bitmap, scale);
		bitmap.recycle();
		saveJpeg(newBitmap, dst, quality);
		newBitmap.recycle();
	}

	public static void resizeImageAlignWidth(String src, String dst,
			int newWidth, int quality) {
		Bitmap bitmap = loadBitmap(src);
		if(bitmap == null){
			JNIHelper.loge("capturePicture error, Unable to decode " + src);
		}
		Bitmap newBitmap = resizeBitmapAlignWidth(bitmap, newWidth);
		bitmap.recycle();
		saveJpeg(newBitmap, dst, quality);
		newBitmap.recycle();
	}

	public static void resizeImage(String src, String dst, int newWidth,
			int newHeight) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmap(bitmap, newWidth, newHeight);
		bitmap.recycle();
		saveJpeg(newBitmap, dst);
		newBitmap.recycle();
	}

	public static void resizeImage(String src, String dst, float scaleWidth,
			float scaleHeight) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmap(bitmap, scaleWidth, scaleHeight);
		bitmap.recycle();
		saveJpeg(newBitmap, dst);
		newBitmap.recycle();
	}

	public static void resizeImage(String src, String dst, float scale) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmap(bitmap, scale);
		bitmap.recycle();
		saveJpeg(newBitmap, dst);
		newBitmap.recycle();
	}

	public static void resizeImageAlignWidth(String src, String dst,
			int newWidth) {
		Bitmap bitmap = loadBitmap(src);
		Bitmap newBitmap = resizeBitmapAlignWidth(bitmap, newWidth);
		bitmap.recycle();
		saveJpeg(newBitmap, dst);
		newBitmap.recycle();
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}
}
