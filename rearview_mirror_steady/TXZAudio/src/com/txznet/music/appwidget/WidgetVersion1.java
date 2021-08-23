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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetVersion1 implements IWidget {

	private static final String TAG = "Music:WidgetVersion1";
	private static Bitmap bitmapRef;
	private static LayerDrawable baseInnerPic;

	private static IWidget mInstance;

	private WidgetVersion1() {
	}

	public static IWidget getInstance() {
		if (mInstance == null) {
			synchronized (IWidget.class) {
				if (mInstance == null) {
					mInstance = new WidgetVersion1();
				}
			}
		}
		return mInstance;
	}

	@Override
	public RemoteViews getView(Context context) {
		return new RemoteViews(context.getPackageName(), R.layout.appwidget_layout_v1);
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

	@Override
	public void updatePlayStatus(Context context, RemoteViews remoteViews, boolean isPlaying) {
		if (isPlaying) {
			remoteViews.setImageViewResource(R.id.Widget_Control, R.drawable.appwidget_stop_v1);
			if (bitmapRef != null && !bitmapRef.isRecycled()) {
				replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_v1
				/* R.layout.appwidget_layout_inner_pic_rotate_rc */);
			}

			regClickEvent(context, remoteViews, WidgetListener.PAUSE);
		} else {
			remoteViews.setImageViewResource(R.id.Widget_Control, R.drawable.appwidget_play_v1);
			replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_v1);
			regClickEvent(context, remoteViews, WidgetListener.PLAY);
			// regClickEventPlay(context, remoteViews);
		}
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(componentName, remoteViews);
	}

	@Override
	public void updatePlayModel(final Context context, final RemoteViews remoteViews, String title, String artist) {
		LogUtil.logd(TAG + "title:" + title);
		updatePlayStatus(context, remoteViews, MediaPlayerActivityEngine.getInstance().isPlaying());
		if (TextUtils.isEmpty(title) && TextUtils.isEmpty(artist)) {
			remoteViews.setTextViewText(R.id.Widget_Title, context.getText(R.string.app_name));
			remoteViews.setTextViewText(R.id.Widget_Artist, "");
		} else {
			remoteViews.setTextViewText(R.id.Widget_Title, title);
			remoteViews.setTextViewText(R.id.Widget_Artist, artist);
			Album album = AlbumDBHelper.getInstance().findOne(Album.class, "id=?",
					new String[] { String.valueOf(MediaPlayerActivityEngine.getInstance().getCurrentAlbum()) });
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
							replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_v1);
							ComponentName componentName = new ComponentName(context, WidgetProvider.class);
							AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(componentName,
									remoteViews);
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
								replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_v1);
							} finally {
								ComponentName componentName = new ComponentName(context, WidgetProvider.class);
								AppWidgetManager.getInstance(context.getApplicationContext())
										.updateAppWidget(componentName, remoteViews);
							}
						}

						@Override
						public void onLoadingCancelled(String s, View view) {
							LogUtil.logd(TAG + "[image] onLoadingCancelled");
						}
					});
					return;
				}
			}
		}
		bitmapRef = null;
		replaceImageView(context, remoteViews, R.layout.appwidget_layout_inner_pic_v1);
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(componentName, remoteViews);
	}

	private void replaceImageView(Context context, RemoteViews remoteViews, int layout_id) {
		RemoteViews subViews = new RemoteViews(context.getPackageName(), layout_id);
		remoteViews.removeAllViews(R.id.Widget_Inner);
		remoteViews.addView(R.id.Widget_Inner, subViews);
		if (bitmapRef != null && !bitmapRef.isRecycled()) {
			remoteViews.setImageViewBitmap(R.id.Widget_Pic, bitmapRef);
		} else {
			remoteViews.setImageViewResource(R.id.Widget_Pic, R.drawable.appwidget_pic_default_v1);
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

	private Bitmap mergeBitmap(Context context, Bitmap bitmap) {
		if (baseInnerPic == null) {
			baseInnerPic = (LayerDrawable) context.getResources().getDrawable(R.drawable.appwidget_inner_rc);
		}
		baseInnerPic.setBounds(new Rect(0, 0, 280, 280));

		Bitmap outBitmap = Bitmap.createBitmap(280, 280, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(outBitmap);

		Drawable bg = baseInnerPic.findDrawableByLayerId(R.id.Widget_Inner_Background);
		bg.setBounds(0, 0, 280, 280);
		bg.draw(canvas);

		Bitmap circleBitmap = Bitmap.createBitmap(280, 280, Bitmap.Config.ARGB_8888);
		Canvas circleCanvas = new Canvas(circleBitmap);
		Drawable mask = baseInnerPic.findDrawableByLayerId(R.id.Widget_Inner_Mask);
		mask.setBounds(0, 0, 280, 280);
		mask.draw(circleCanvas);
		Paint paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

		int half = 280 / 2;
		int part = 173 / 2;

		circleCanvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()),
				new Rect(half - part, half - part, half + part, half + part), paint);

		canvas.drawBitmap(circleBitmap, 0, 0, null);
		return outBitmap;
	}

}
