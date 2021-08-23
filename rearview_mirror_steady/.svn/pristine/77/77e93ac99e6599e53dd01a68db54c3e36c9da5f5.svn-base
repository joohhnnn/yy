package com.txznet.music.appwidget;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.Configuration;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.music.R;
import com.txznet.music.bean.response.Album;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.receiver.WidgetListener;
import com.txznet.music.ui.MediaPlayerActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetVersion2 implements IWidget {

	private static final String TAG = "Music:WidgetVersion2";
	private static Bitmap bitmapRef;

	private static IWidget mInstance;

	private WidgetVersion2() {
	}

	public static IWidget getInstance() {
		if (mInstance == null) {
			synchronized (IWidget.class) {
				if (mInstance == null) {
					mInstance = new WidgetVersion2();
				}
			}
		}
		return mInstance;
	}

	@Override
	public RemoteViews getView(Context context) {
		return new RemoteViews(context.getPackageName(), R.layout.appwidget_layout_rc);
	}

	@Override
	public void updatePlayStatus(Context context, RemoteViews remoteViews, boolean isPlaying) {
		if (isPlaying) {
			remoteViews.setImageViewResource(R.id.Widget_Control, R.drawable.appwidget_stop_rc);
			if (bitmapRef != null && !bitmapRef.isRecycled()) {
				replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_normal_rc
				/* R.layout.appwidget_layout_inner_pic_rotate_rc */);
			}

			regClickEvent(context, remoteViews, WidgetListener.PAUSE);
		} else {
			remoteViews.setImageViewResource(R.id.Widget_Control, R.drawable.appwidget_play_rc);
			replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_normal_rc);
			regClickEvent(context, remoteViews, WidgetListener.PLAY);
			// regClickEventPlay(context, remoteViews);
		}
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(componentName, remoteViews);
	}

	@Override
	public void updatePlayModel(final Context context, final RemoteViews remoteViews, String title, String artist) {
		LogUtil.logd(TAG + "title:" + title);
		if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
			remoteViews.setTextViewText(R.id.Widget_Title, context.getText(R.string.app_name));
			remoteViews.setTextViewText(R.id.Widget_Artist, "");
		} else {
			remoteViews.setTextViewText(R.id.Widget_Title, title);
			remoteViews.setTextViewText(R.id.Widget_Artist, artist);
			Album album = AlbumDBHelper.getInstance().findOne(Album.class, "id=?",
					new String[] { String.valueOf(MediaPlayerActivityEngine.getInstance()
							.getCurrentAlbum())/*
												 * String.valueOf(
												 * SharedPreferencesUtils
												 * .getCurrentAlbumID())
												 */ });
			LogUtil.logd(TAG + "album is null?" + (album == null));
			if (album != null) {
				LogUtil.logd(TAG + "Album [sid=" + album.getSid() + ", id=" + album.getId() + ", name="
						+ album.getName() + ", logo=" + album.getLogo() + "]");
				String logo = album.getLogo();
				if (!TextUtils.isEmpty(logo)) {
					ImageLoader.getInstance().loadImage(logo, new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String s, View view) {
							LogUtil.logd(TAG + "[image] onLoadingStarted");
						}

						@Override
						public void onLoadingFailed(String s, View view, FailReason failReason) {
							LogUtil.logd(TAG + "[image] onLoadingFailed ,"
									+ (failReason != null ? failReason.getCause() : "null"));
							bitmapRef = null;
							replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_normal_rc);
						}

						@Override
						public void onLoadingComplete(String s, View view, Bitmap bitmap) {
							LogUtil.logd(TAG + "[image] onLoadingComplete");
							try {
								Bitmap outBitmap = mergeBitmap(context, bitmap);
								bitmapRef = outBitmap;
								remoteViews.setImageViewBitmap(R.id.Widget_Pic, outBitmap);
							} catch (Exception e) {
								LogUtil.logd(TAG + "[image] exception " + e.toString());
								bitmapRef = null;
								replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_normal_rc);
							}
						}

						@Override
						public void onLoadingCancelled(String s, View view) {
							LogUtil.logd(TAG + "[image] onLoadingCancelled");
						}
					});
				} else {

				}
			} else {
				bitmapRef = null;
				replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_normal_rc);
			}
		}
	}

	private void replaceImageView(Context context, RemoteViews remoteViews, int layout_id) {
		RemoteViews subViews = new RemoteViews(context.getPackageName(), layout_id);
		remoteViews.removeAllViews(R.id.Widget_Inner);
		remoteViews.addView(R.id.Widget_Inner, subViews);
		if (bitmapRef != null && !bitmapRef.isRecycled()) {
			remoteViews.setImageViewBitmap(R.id.Widget_Pic, bitmapRef);
			remoteViews.setViewVisibility(R.id.Widget_Pic_Bg, View.VISIBLE);
		} else {
			remoteViews.setImageViewResource(R.id.Widget_Pic, R.drawable.appwidget_pic_default_rc);
			remoteViews.setViewVisibility(R.id.Widget_Pic_Bg, View.INVISIBLE);
		}
		Intent backIntent = null;
		try {
			backIntent = new Intent(GlobalContext.get(), Class.forName(Configuration.getInstance().getString("main")));
		} catch (ClassNotFoundException e) {
			LogUtil.loge("[Exception] " + " ClassNotFoundException");
			return;
		}
		// LogUtil.logd(TAG + " regMainEvent:"
		// + Configuration.getInstance().getString("main"));
		backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		remoteViews.setOnClickPendingIntent(R.id.Widget_Pic,
				PendingIntent.getActivity(context, 0, backIntent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	@Override
	public void regClickEvent(Context context, RemoteViews remoteViews, String keyExtra) {
		Intent backIntent = new Intent(context, MediaPlayerActivity.class);
		backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		remoteViews.setOnClickPendingIntent(R.id.Widget_Pic, PendingIntent.getActivity(context, 0, backIntent, 0));
		LogUtil.logd(TAG + " regClickEvent:" + keyExtra);
		Intent intent = new Intent(WidgetListener.LISTENER);
		intent.putExtra(WidgetListener.OPERATOR, keyExtra);
		intent.setPackage(context.getPackageName());
		remoteViews.setOnClickPendingIntent(R.id.Widget_Control,
				PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));

	}

	private Bitmap mergeBitmap(Context context, Bitmap bitmap) {
		int width = (int) GlobalContext.get().getResources().getDimension(R.dimen.y150);
		Bitmap zoomImg = zoomImg(bitmap, width, width);
		int radius = width / 2;
		BitmapShader bitmapShader = new BitmapShader(zoomImg, BitmapShader.TileMode.REPEAT,
				BitmapShader.TileMode.REPEAT);
		Bitmap dest = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(dest);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setShader(bitmapShader);
		c.drawCircle(radius, radius, radius, paint);
		return dest;
	}

	private Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
		// 获得图片的宽高
		int width = bm.getWidth();
		int height = bm.getHeight();
		// 计算缩放比例
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// 取得想要缩放的matrix参数
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片 www.2cto.com
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
		return newbm;
	}

}
