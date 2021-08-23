package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;

public class TextUtil {
	private static ITextCallBack mCallBack;
	public static final int NetError = 0;
	public static final int ParseError = 1;
	public static final int UnkownError = -1;

	public static abstract class ITextCallBack {
		public void onResult(String jsonResult) {

		}

		public void onError(int errorCode) {

		}
	}
	/**回调返回同行者json*/
	public static void parseText(String text, ITextCallBack callBack) {
		mCallBack = callBack;
		JSONBuilder jsonData = new JSONBuilder();
		jsonData.put("text", text);

		ServiceManager.getInstance().sendInvoke(TXZ, "comm.text.parse",
				jsonData.toString().getBytes(), null);
	}

	public static void notifyTextCallback(String cmd, byte[] data) {
		if (cmd.equals("result")) {
			if (mCallBack != null) {
				if (data == null) {
					return;
				}
				mCallBack.onResult(new String(data));
			}
		}

		if (cmd.equals("error")) {
			if (data == null) {
				return;
			}
			String str = new String(data);

			mCallBack.onError(str2int(str, UnkownError));
		}
	}

	private static int str2int(String str, int defaultValue) {
		int value = 0;//
		try {
			value = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			value = defaultValue;
		}

		return value;
	}
}
