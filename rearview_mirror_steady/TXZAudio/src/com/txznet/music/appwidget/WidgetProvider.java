package com.txznet.music.appwidget;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.receiver.WidgetListener;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	private static final String TAG = "[widget] ";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		LogUtil.logd(TAG + "onUpdate");
		IWidget widget = WidgetFactory.getWidget();
		RemoteViews remoteViews = widget.getView(context);
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		widget.regClickEvent(context, remoteViews, WidgetListener.PLAY);
		appWidgetManager.updateAppWidget(componentName, remoteViews);
		context.sendBroadcast(new Intent("com.txznet.music.action.REQ_SYNC"));
	}

	@Override
	public void onEnabled(Context context) {
		LogUtil.logd(TAG + "onEnabled");
		super.onEnabled(context);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(
				new ComponentName(context.getPackageName(), context.getPackageName() + ".appwidget.WidgetProvider"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		context.sendBroadcast(new Intent("com.txznet.music.action.REQ_SYNC"));
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		LogUtil.logd(TAG + "onDeleted");
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		LogUtil.logd(TAG + "onDisabled");
		// PackageManager pm = context.getPackageManager();
		// pm.setComponentEnabledSetting(
		// new ComponentName(context.getPackageName(), context.getPackageName()
		// + ".appwidget.WidgetProvider"),
		// PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		// PackageManager.DONT_KILL_APP);
		super.onDisabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.logd(TAG + "onReceive:" + intent);
		ComponentName componentName = new ComponentName(context, WidgetProvider.class);
		RemoteViews remoteViews = WidgetFactory.getWidget().getView(context);
		if ("com.txznet.music.action.PLAY_STATUS_CHANGE".equals(intent.getAction())) {
			int status = intent.getIntExtra("status", 0);
			LogUtil.logd(TAG + "change status:" + status);

			WidgetFactory.getWidget().updatePlayStatus(context, remoteViews, status == 2);

		} else if ("com.txznet.music.action.MUSIC_MODEL_CHANGE".equals(intent.getAction())) {

			Audio audio = MediaPlayerActivityEngine.getInstance().getCurrentAudio();
			LogUtil.logd(TAG + "audio:" + (audio == null ? "null" : audio.toString()));
			if (audio == null) {
				WidgetFactory.getWidget().updatePlayModel(context, remoteViews, null, null);
			} else {
				WidgetFactory.getWidget().updatePlayModel(context, remoteViews, audio.getName(),
						(audio.getArrArtistName() == null || audio.getArrArtistName().size() < 1) ? ""
								: audio.getArrArtistName().get(0));
			}
//			 WidgetFactory.getWidget().regClickEvent(context, remoteViews, WidgetListener.STATUS_CHANGE);
//			AppWidgetManager.getInstance(context.getApplicationContext()).updateAppWidget(componentName, remoteViews);

		}
		super.onReceive(context, intent);
	}

}
