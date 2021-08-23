package com.txznet.comm.ui.keyevent;

import java.util.HashSet;
import java.util.Set;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZWheelControlEvent;

import android.view.KeyEvent;
/**
 *	用来分发方控按键
 * 
 * @author Terry
 */
public class KeyEventManager {

	private static KeyEventManager sInstance = new KeyEventManager();

	private KeyEventDispatcherBase mKeyEventDispatcher;
	private int mWheelControlState = 0;
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTED = 1;

	public static final int MODE_TWO_DIRECTION = 1;
	public static final int MODE_FOUR_DIRECTION = 2;
	
	
	private KeyEventManager() {
	}

	public static KeyEventManager getInstance() {
		return sInstance;
	}
	
	public void init() {
		String classKeyEvent = ConfigUtil.getKeyEventDispatcher();
		try {
			mKeyEventDispatcher = (KeyEventDispatcherBase) UIResLoader.getInstance().getClassInstance(classKeyEvent);
		} catch (Exception e) {
			LogUtil.loge("[UI2.0] get keyEventDispatcher error");
			// e.printStackTrace();
		}
	}
	
	public void onChatViewChange(ViewBase viewBase) {
		if (mKeyEventDispatcher != null) {
			mKeyEventDispatcher.onChatViewChange(viewBase);
		} else {
			LogUtil.loge("[UI2.0] mKeyEventDispatcher is null,update content view failed");
		}
	}
	
	public void onUpdateProgress(int selection, int value) {
		if (mKeyEventDispatcher != null) {
			mKeyEventDispatcher.onUpdateProgress(selection, value);
		} else {
			
		}
	}
	
	public boolean onKeyEvent(int keyEvent){
		switch (keyEvent) {
		case KeyEvent.KEYCODE_BACK:
		case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
			LogUtil.logd("[UI2.0] on KEYCODE_BACK pressed");
			dispatchKeyEvent(KeyEvent.KEYCODE_BACK);
			TXZAsrManager.getInstance().cancel();
			return true;
		case TXZWheelControlEvent.LEVOROTATION_EVENTID:
		case KeyEvent.KEYCODE_DPAD_UP:
			return dispatchKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
		case TXZWheelControlEvent.DEXTROROTATION_EVENTID:
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return dispatchKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
		case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			return dispatchKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER);
		case TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID:
			return dispatchKeyEvent(KeyEventDispatcherBase.KEYCODE_VOICE);
		default:
			return dispatchKeyEvent(keyEvent);
		}
	}
	
	
	private Set<FocusSupportListener> listeners = new HashSet<KeyEventManager.FocusSupportListener>();

	public void onWheelControlStateChanged(int state) {
		LogUtil.logd("[UI2.0] onWheelControlStateChanged :" + state);
		if(mWheelControlState == state){
			return;
		}
		mWheelControlState = state;
		for (FocusSupportListener listener : listeners) {
			if (state == STATE_CONNECTED) {
				listener.onStateChanged(true);
			}
			if (state == STATE_DISCONNECTED) {
				listener.onStateChanged(false);
			}
		}
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
	
	
	
	private boolean dispatchKeyEvent(int keyEvent) {
		if (mKeyEventDispatcher != null) {
			return mKeyEventDispatcher.onKeyEvent(keyEvent);
		} else {
			LogUtil.loge("[UI2.0] mKeyEventDispatcher is null,ingore keyEvent:" + keyEvent);
			return false;
		}
		// ViewBase viewBase = WinLayoutManager.getInstance().getCurMsgView();
		// if (viewBase != null && viewBase.supportKeyEvent()) {
		// viewBase.onKeyEvent(keyEvent);
		// } else {
		// LogUtil.loge("current view don't support key event,ignore");
		// }
	}
}
