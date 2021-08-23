package com.txznet.record.keyevent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatDisplayAdapter;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZWheelControlEvent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
/**
 *	用来分发方控按键
 * 
 * @author Terry
 */
public class KeyEventManagerUI1 {

	private static KeyEventManagerUI1 sInstance = new KeyEventManagerUI1();

	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTED = 1;

	public static final int MODE_TWO_DIRECTION = 1;
	public static final int MODE_FOUR_DIRECTION = 2;
	
	private boolean mDisplayListVisible = false;
	
	private KeyEventManagerUI1() {
		IntentFilter intentFilter = new IntentFilter("com.txznet.txz.DisplayLv_ACTION");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				mDisplayListVisible = intent.getBooleanExtra("visible", false);
			}
		}, intentFilter);
	}

	public static KeyEventManagerUI1 getInstance() {
		return sInstance;
	}

	public void onUpdateProgress(int selection, int value) {
		KeyEventDispatcherUI1.getInstance().onUpdateProgress(selection, value);
	}

	public boolean onKeyEvent(KeyEvent keyEvent){
		switch (keyEvent.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
		case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
			if (KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
				TXZAsrManager.getInstance().cancel();
			}
			return true;
		case TXZWheelControlEvent.LEVOROTATION_EVENTID:
		case KeyEvent.KEYCODE_DPAD_UP:
			if (KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
				KeyEventDispatcherUI1.getInstance().onKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
			}
			return true;
		case TXZWheelControlEvent.DEXTROROTATION_EVENTID:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
				KeyEventDispatcherUI1.getInstance().onKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
			}
			return true;
		case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
				KeyEventDispatcherUI1.getInstance().onKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
			}
			return true;
		case TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID:
			if (KeyEvent.ACTION_DOWN == keyEvent.getAction()) {
				if (!WinRecord.getInstance().isShowing()) {
					TXZAsrManager.getInstance().triggerRecordButton();
				} else {
					if (mDisplayListVisible) {
						TXZAsrManager.getInstance().restart("");
					} else {
						clickRecord();
					}
				}
			}
			return true;
		default:
			break;
		}
		return false;
	}
	
	private boolean isJustClick;

	private void clickRecord() {
		LogUtil.logd("clickRecord");
		if (isJustClick)
			return;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.record", null, null);
		isJustClick = true;
		AppLogicBase.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				isJustClick = false;
			}
		}, 1000);
	}
	
	
	
	private Set<FocusSupportListener> listeners = new HashSet<FocusSupportListener>();

	public void onWheelControlStateChanged(int state) {
		LogUtil.logd("[UI1.0] onWheelControlStateChanged :" + state);
		KeyEventDispatcherUI1.getInstance().onBLEStateChanged(state == STATE_CONNECTED);
	}

	public void addFocusSupportListener(FocusSupportListener listener) {
		listeners.add(listener);
	}
	
	public void removeFocusSupportListener(FocusSupportListener listener){
		listeners.remove(listener);
	}
	
	public interface FocusSupportListener{
		public void onStateChanged(boolean support);
	}
	
	public void updateFocusViews(List<View> focusViews){
		KeyEventDispatcherUI1.getInstance().updateFocusViews(focusViews);
	}
	
	public void updateFocusViews(List<View> focusViews, Drawable bgNormal){
		KeyEventDispatcherUI1.getInstance().updateFocusViews(focusViews);
		KeyEventDispatcherUI1.getInstance().updateNormalBg(bgNormal);
	}
	
	public void updateListView(ListView listView, Drawable drawableNor) {
		updateListView(listView);
		KeyEventDispatcherUI1.getInstance().updateNormalBg(drawableNor);
	}
	
	public void updateListView(ListView listView) {
		LogUtil.logd("[UI1.0] updateListView:" + listView);
		if (listView == null) {
			KeyEventDispatcherUI1.getInstance().updateFocusViews(null);
			return;
		}

		int count = listView.getCount();
		List<View> itemViews = new ArrayList<View>();
		for (int i = 0; i < count; i++) {
			View itemView = listView.getChildAt(i);
			itemViews.add(itemView);
		}
		LogUtil.logd("[UI1.0]updateFocusViews :" + itemViews.size());
		KeyEventDispatcherUI1.getInstance().updateFocusViews(itemViews);
	}
	
	public void updateListAdapter(ChatDisplayAdapter adapter){
		LogUtil.logd("[UI1.0] updateListAdapter:" + adapter);
		KeyEventDispatcherUI1.getInstance().updateListAdapter(adapter);
	}
	
	public void release(){
		KeyEventDispatcherUI1.getInstance().release();
	}
	
}
