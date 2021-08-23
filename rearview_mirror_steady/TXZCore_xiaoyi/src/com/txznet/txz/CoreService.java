package com.txznet.txz;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.component.call.sys.PhoneStatReceiver;
import com.txznet.txz.module.launch.MediaButtonReceiver;

/*
 *  保留进程运行
 */
public class CoreService extends Service {
	public static final String Tag = "CoreService";
	static int NOTIFICATION_ID = 9527;
	public static CoreService sSvr;
	private ContentObserver smsObserver;

	@Override
	public void onCreate() {
		super.onCreate();
		sSvr = this;

		// TODO 短信广播监控
		// ContentResolver resolver = getContentResolver();
		// smsObserver = new SMSObserver(new Handler(Looper.getMainLooper()));
		// resolver.registerContentObserver(Uri.parse("content://sms/"), true,
		// smsObserver);

		IntentFilter iFilterCall = new IntentFilter();
		PhoneStatReceiver recieverCall = new PhoneStatReceiver();
		iFilterCall.addAction("android.intent.action.PHONE_STATE");
		iFilterCall.addAction("android.intent.action.NEW_OUTGOING_CALL");
		iFilterCall.setPriority(Integer.MAX_VALUE);
		registerReceiver(recieverCall, iFilterCall);

		IntentFilter mediaButtonFilter = new IntentFilter();
		MediaButtonReceiver mediaButton = new MediaButtonReceiver();
		mediaButtonFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		mediaButtonFilter.setPriority(Integer.MAX_VALUE);
		registerReceiver(mediaButton, mediaButtonFilter);

		if (Build.VERSION.SDK_INT < 18) {
			startForeground(NOTIFICATION_ID, new Notification());
		} else {
			Intent i = new Intent(GlobalContext.get(), KernelService.class);
			i.putExtra("NotificationID", NOTIFICATION_ID);
			GlobalContext.get().startService(i);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {	
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(smsObserver);
	}

	public static class KernelService extends Service {

		@Override
		public void onCreate() {
			try {
				this.stopForeground(true);
			} catch (Exception e) {
				e = null;
			}

			super.onCreate();
			Log.d("MemoryManager", "KernelService.stopForegroundCompat: "
					+ Build.VERSION.SDK_INT);
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			if (null != intent) {
				int ID = intent.getIntExtra("NotificationID", 0);
				if (ID > 0 && null != sSvr) {
					Log.d("MemoryManager",
							"KernelService.startForegroundCompat: "
									+ Build.VERSION.SDK_INT);
					try {
						sSvr.startForeground(NOTIFICATION_ID,
								new Notification());
						this.startForeground(NOTIFICATION_ID,
								new Notification());
						sSvr.stopForeground(true);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
			return Service.START_NOT_STICKY;
		}

		@Override
		public void onDestroy() {

			Log.d("MemoryManager", "KernelService.stopForegroundCompat: "
					+ Build.VERSION.SDK_INT);
			try {
				this.stopForeground(true);
			} catch (Exception e) {
				e = null;
			}
			Log.d("MemoryManager", "KernelService.onDestroy");
			super.onDestroy();
		}

		@Override
		public IBinder onBind(Intent intent) {
			return null;
		}
	}
}
