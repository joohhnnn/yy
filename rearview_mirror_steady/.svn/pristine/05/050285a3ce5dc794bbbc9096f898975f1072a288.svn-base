package com.txznet.txz.component.nav.baidu;

import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGAuthSuccess;
import com.baidu.navisdk.hudsdk.client.BNRemoteVistor;
import com.baidu.navisdk.hudsdk.client.HUDSDkEventCallback.OnConnectCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;

public class BaiduConnect implements OnConnectCallback {
	public static final int MAX_RETRY_COUNT = 10;

	int mRetryCount;
	NavBaiduDeepImpl mNavBaiduDeepImpl;

	public BaiduConnect(NavBaiduDeepImpl nbdi) {
		this.mNavBaiduDeepImpl = nbdi;
	}

	@Override
	public void onAuth(BNRGAuthSuccess arg0) {
		JNIHelper.logd("onAuth:" + arg0);
	}

	@Override
	public void onClose(int arg0, String arg1) {
		JNIHelper.logd("onClose:" + arg1 + "," + getErrorDes(arg0));
		if (OnConnectCallback.CLOSE_LBS_AUTH_FALIED == arg0) {
			AppLogic.removeBackGroundCallback(mRetryConnRunnable);
			AppLogic.runOnBackGround(mRetryConnRunnable, 1000);
		}
	}

	private String getErrorDes(int arg0) {
		switch (arg0) {
		case OnConnectCallback.CLOSE_AUTH_FAILED:
			return "认证失败";
		case OnConnectCallback.CLOSE_LBS_AUTH_FALIED:
			return "LBS开发平台鉴权失败";
		case OnConnectCallback.CLOSE_NETWORK_NOT_OPEN:
			return "两台设置之间互联，WIFI没有打开";
		case OnConnectCallback.CLOSE_NORMAL:
			return "正常关闭";
		case OnConnectCallback.CLOSE_PROTOCOL_ERROR:
			return "内部协议错误";
		case OnConnectCallback.CLOSE_READ_TIMEOUT:
			return "读取超时";
		case OnConnectCallback.CLOSE_SEND_TIMEOUT:
			return "机动点发送数据超时";
		case OnConnectCallback.CLOSE_SERVER_ERROR:
			return "服务器错误";
		case OnConnectCallback.CLOSE_SOCKET_ERROR:
			return "Socket错误";
		case OnConnectCallback.CLOSE_WAIT_LBS_AUTH_RESULT:
			return "LBS授权尚未完成，请等待授权结果再次尝试";
		}
		return "未知返回值";
	}

	@Override
	public void onConnected() {
		JNIHelper.logd("onConnected");
	}

	@Override
	public void onEndLBSAuth(int arg0, String arg1) {
		JNIHelper.logd("onEndLBSAuth errCode:" + arg0 + ",errDes:" + arg1);
		try {
			if (arg0 != 0) { // 为0成功，否则失败，文档提示失败需要重新init
				AppLogic.removeBackGroundCallback(mRetryConnRunnable);
				AppLogic.runOnBackGround(mRetryConnRunnable, 1000);
			} else {
				if (!BNRemoteVistor.getInstance().isConnect()) {
					BNRemoteVistor.getInstance().open();
				}
			}
		} catch (Exception e) {
			JNIHelper.logw("onEndLBSAuth error:" + e.toString());
		}
	}

	public void checkToConnect() {
		JNIHelper.logd("checkToConnect");
		BNRemoteVistor.getInstance().open();
		mRetryCount = 0;
		AppLogic.removeBackGroundCallback(mRetryConnRunnable);
		AppLogic.runOnBackGround(mRetryConnRunnable, 1000);
	}

	Runnable mRetryConnRunnable = new Runnable() {

		@Override
		public void run() {
			if (BNRemoteVistor.getInstance().isConnect()) {
				JNIHelper.logw("BaiduHud isConnect");
				return;
			}

			if (mRetryCount <= MAX_RETRY_COUNT) {
				mRetryCount++;
				JNIHelper.logd("retry LBSAuth : " + mRetryCount);
				mNavBaiduDeepImpl.connectHudSDK(true);

				// 尝试初始化MAX_RETRY_COUNT次
				AppLogic.removeBackGroundCallback(this);
				AppLogic.runOnBackGround(this, 1000);
			}
		}
	};

	@Override
	public void onReConnected() {
		JNIHelper.logd("onReConnected");
	}

	@Override
	public void onStartLBSAuth() {
		JNIHelper.logd("onStartLBSAuth");
	}
}
