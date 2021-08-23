package com.txznet.alldemo.ui;

import java.util.List;
import com.txznet.alldemo.MyApplication;

public class ActionManager {
	public static final String TAG = "ActionManager";

	public static void fillAction(List<AutoAction> actionList) {
		if (actionList == null) {
			return;
		}
		actionList.clear();
		// add action into action list
		actionList.add(new AutoAction() {
			@Override
			public void aciton() {
				MyApplication.getApp().showMsg("执行命令");
			}
		}.setName("执行命令"));
	}
}
