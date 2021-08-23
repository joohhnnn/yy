package com.txznet.txz;

import android.content.Intent;
import android.os.Bundle;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.txz.entry.LaunchActivity;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.service.NavActionReciever;

public class MainActivity extends LaunchActivity {
	public void doLaunch(Intent intent) {
		JNIHelper.recordLogStack(1);
		JNIHelper.logd("MainActivity doLaunch: " + intent.getAction());

		while (intent.getAction() != null) {
			if (intent.getAction().equals("com.txznet.txz.nav")) {
				LaunchManager.getInstance().launchWithNav();
				break;
			}

			if (intent.getAction().equals("com.txznet.txz.music")) {
				LaunchManager.getInstance().launchWithMusic();
				break;
			}

			if (intent.getAction().equals("com.txznet.txz.help")) {
				LaunchManager.getInstance().launchWithHelpDetail();
				break;
			}

			if (intent.getAction().equals("com.txznet.txz.record")) {
				LaunchManager.getInstance().launchWithRecord();
				this.finish();
				break;
			}
			
			if (intent.getAction().equals("com.txznet.txz.weixin")) {
				LaunchManager.getInstance().launchWithWeixinAssistor();
				break;
			}

			if (intent.getAction().equals("com.txznet.txz.nav.plan")) {
				NavigateInfo navigateInfo = NavActionReciever.readNavigateInfo(intent);
				if (navigateInfo != null && navigateInfo.msgGpsInfo != null && navigateInfo.msgGpsInfo.dblLat != null
						&& navigateInfo.msgGpsInfo.dblLng != null) {
					NavManager.getInstance().NavigateToByTXZ(navigateInfo);
				}
				return;
			}
			
			this.finish();
			
			break;
		}
	}

	@Override
	public void onLaunch() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doLaunch(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		doLaunch(intent);
	}

}
