package com.txznet.txz.component.nav.tx.internal;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.nav.tool.NavInterceptTransponder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class TNBroadcastReceiver {
	private NavThirdApp mParentApp;
    private boolean isForeground = false;
    private OnNaviMsgListener mMsgListener;
    private static class LayzerHolder {
        private static final TNBroadcastReceiver sIntance = new TNBroadcastReceiver();
    }
    private TNBroadcastReceiver() {
    }
    public static TNBroadcastReceiver getInstance() {
        return LayzerHolder.sIntance;
    }

    public void init() {
        TNBroadcastManager.getInstance().getContext().registerReceiver(mReceiver,new IntentFilter(ExternalDefaultBroadcastKey.BROADCAST_ACTION.SEND));
    }
    
	public void assignParent(NavThirdApp app) {
		mParentApp = app;
	}

    public void unInit() {
        TNBroadcastManager.getInstance().getContext().unregisterReceiver(mReceiver);
    }


	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			handleIntent(intent);
		}
	};
	
	public void handleIntent(Intent intent) {
		int key_type = intent.getIntExtra(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, -1);
		LogUtil.logd("TNBroadcastReceiver key_type:" + key_type + "," + intent.getExtras());
		switch (key_type) {
		case ExternalDefaultBroadcastKey.TYPE.NAVI_STATUS:
			onReceiveNaviStatus(intent);
			break;
		default:
			if (mMsgListener != null) {
				mMsgListener.onNavRecv(intent);
			}
			break;
		}
	}

    private void onReceiveNaviStatus(Intent intent) {
        int type = intent.getIntExtra(ExternalDefaultBroadcastKey.KEY.TYPE,-1);
        switch(type) {
			case ExternalDefaultBroadcastKey.STATUS_TYPE.FOREGROUND:
				if (mParentApp != null) {
					if (NavInterceptTransponder.getInstance().interceptGroundIntent(mParentApp, intent, true)) {
						break;
					}
				}
            	
                isForeground = true;
                if (mMsgListener != null) {
                    mMsgListener.onForeground();
                }
                break;
            case ExternalDefaultBroadcastKey.STATUS_TYPE.BACKGROUD:
            	if (mParentApp != null) {
					if (NavInterceptTransponder.getInstance().interceptGroundIntent(mParentApp, intent, false)) {
						break;
					}
				}
            	
                isForeground = false;
                if (mMsgListener != null) {
                    mMsgListener.onBackground();
                }
                break;
            case ExternalDefaultBroadcastKey.STATUS_TYPE.START_NAV:
                if (mMsgListener != null) {
                    mMsgListener.onStartNav();;
                }
            	break;
            case ExternalDefaultBroadcastKey.STATUS_TYPE.END_NAV:
                if (mMsgListener != null) {
                    mMsgListener.onEndNav();
                }
            	break;
            case ExternalDefaultBroadcastKey.STATUS_TYPE.EXIT:
            	if (mMsgListener != null) {
            		mMsgListener.onNavExit();
            	}
            	break;
        }
    }


    public boolean isForeground() {
        return isForeground;
    }

	public interface OnNaviMsgListener {
		void onForeground();

		void onBackground();

		void onStartNav();

		void onEndNav();
		
		void onNavExit();

		void onNavRecv(Intent intent);
	}

    public void setMsgListener(OnNaviMsgListener listener) {
        mMsgListener = listener;
    }

}
