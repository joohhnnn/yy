package com.txznet.music.utils;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description 图片工具类
 * @2015/7/21 10:08
 */
public class ImageUtils {

	private static final String IMAGE_CROP_ACTION_ = "com.android.camera.action.CROP";

	private static final String IMAGE_TMP_NAME_FORMAT = "'IMG'_yyyyMMdd_HHmmss";

	private static final String IMAGE_SEE_PATH = "image/*";

	/**
	 * @param imagePath
	 *            图片的路径
	 * @return
	 */
	public static Bitmap getSuitableBitmap(String imagePath) {

		if (!new File(imagePath).exists() || new File(imagePath).isDirectory()) {
			return null;
		}
		int tempPix = 1280;
		int maxNumOfPixels = tempPix * tempPix;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, maxNumOfPixels);
		opts.inJustDecodeBounds = false;
		try {
			return BitmapFactory.decodeFile(imagePath, opts);
		} catch (OutOfMemoryError err) {
		}
		return null;
	}

	public static Bitmap getSuitableBitmap(ContentResolver resolver, Uri uri)
			throws FileNotFoundException {
		int tempPix = 1280;
		int maxNumOfPixels = tempPix * tempPix;
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(resolver.openInputStream(uri), null,
					opts);
			opts.inSampleSize = computeSampleSize(opts, -1, maxNumOfPixels);
			opts.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(resolver.openInputStream(uri),
					null, opts);
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
		}
		return null;
	}

	private static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize = initialSize;
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.round(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static final DisplayImageOptions initDefault(int resId, int roundNum) {
		return initDefault(resId, resId, roundNum);
	}

	public static final DisplayImageOptions initDefault() {
		return initDefault(R.drawable.fm_item_default, 8);
	}


	public static final DisplayImageOptions initDefault(int resId,
			int failResId, int roundNum) {
			return new DisplayImageOptions.Builder()
					.showImageOnFail(failResId)
					.showImageForEmptyUri(failResId)
					.showImageOnLoading(resId)
					.displayer(new RoundedBitmapDisplayer(roundNum))
					/*
					 * EXACTLY :图像将完全按比例缩小的目标大小 
					 * EXACTLY_STRETCHED:图片会缩放到目标大小完全
					 * IN_SAMPLE_INT:图像将被二次采样的整数倍
					 * IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
					 * NONE:图片不会调整
					 */
					// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.cacheInMemory(true).cacheOnDisk(true)
//					.bitmapConfig(Bitmap.Config.ARGB_4444)
					.build();
	}
	
	public static final DisplayImageOptions initDefault(int resId,
			int failResId, int logdingDrawable, int roundNum) {
			return new DisplayImageOptions.Builder()
					.showImageOnFail(failResId)
					.showImageForEmptyUri(failResId)
					.showImageOnLoading(logdingDrawable)
					.displayer(new RoundedBitmapDisplayer(roundNum))
					/*
					 * EXACTLY :图像将完全按比例缩小的目标大小 
					 * EXACTLY_STRETCHED:图片会缩放到目标大小完全
					 * IN_SAMPLE_INT:图像将被二次采样的整数倍
					 * IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
					 * NONE:图片不会调整
					 */
					// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
					.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
					.cacheInMemory(true).cacheOnDisk(true)
//					.bitmapConfig(Bitmap.Config.ARGB_4444)
					.build();
	}

	/**
	 * 将bitmap转换为drawale
	 *
	 * @param bitmap
	 * @return
	 */
	public static Drawable transfromBitmapToDrawable(Bitmap bitmap) {
		Drawable drawable = new BitmapDrawable(bitmap);
		return drawable;
	}

	public static Bitmap transfromDrawableToBitmap(Drawable pDrawable) {
		BitmapDrawable lBitmapDrawable = (BitmapDrawable) pDrawable;
		return lBitmapDrawable.getBitmap();
	}

	public static String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				IMAGE_TMP_NAME_FORMAT);
		return dateFormat.format(date) + ".jpg";
	}

	/*
	 * 裁剪图片
	 */
	public static Intent cameraCrop(File file, Uri uri, int type) {
		Intent intent = new Intent(IMAGE_CROP_ACTION_);
		if (type == 1) {
			intent.setDataAndType(Uri.fromFile(file), IMAGE_SEE_PATH);// 设置要裁剪的图片
		} else {
			intent.setDataAndType(uri, IMAGE_SEE_PATH);// 设置要裁剪的图片
		}
		intent.putExtra("crop", "true");// crop=true 有这句才能出来最后的裁剪页面.
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 200);// 保存到原文件
		intent.putExtra("outputY", 200);// 保存到原文件
		// intent.putExtra("noFaceDetection", true);
		intent.putExtra("scale", true);// 黑边
		intent.putExtra("scaleUpIfNeeded", true);// 黑边
		intent.putExtra("output", Uri.fromFile(file));// 保存到原文件
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());// 返回格式
		return intent;
	}

	/**
	 * 获得圆角图片的方法
	 *
	 * @return 如果转换失败则返回传进来的图片
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		try {
			Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);
			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			// FileUtils.recycleBitmap(bitmap);
			return output;
		} catch (OutOfMemoryError err) {
			// Logger.e("图片处理大小:%s", err.toString());
			return bitmap;
		} catch (Exception e) {
			// Logger.e("error:%s", e.toString());
			return bitmap;
		}

	}
}
