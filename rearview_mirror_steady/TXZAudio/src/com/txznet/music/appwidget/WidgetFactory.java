package com.txznet.music.appwidget;

import com.txznet.fm.bean.Configuration;

public class WidgetFactory {

	public static IWidget getWidget() {
		IWidget widget = null;
		switch (Configuration.getInstance().getInteger(Configuration.TXZ_WIDGET)) {
		case 1:
			widget = WidgetVersion1.getInstance();
			break;
		case 2:
			widget = WidgetVersion2.getInstance();
			break;
		default:
			widget = WidgetVersion1.getInstance();
			break;
		}
		return widget;
	}

}
