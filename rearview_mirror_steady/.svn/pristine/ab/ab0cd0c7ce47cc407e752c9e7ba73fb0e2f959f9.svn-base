package com.txznet.music.appwidget;

import android.content.Context;
import android.widget.RemoteViews;

public interface IWidget {

	RemoteViews getView(Context context);
	
	void regClickEvent(Context context, RemoteViews remoteViews, String keyExtra);
	
	void updatePlayStatus(Context context, RemoteViews remoteViews, boolean isPlaying);
	
	void updatePlayModel(Context context, RemoteViews remoteViews, String title, String artist);
}
