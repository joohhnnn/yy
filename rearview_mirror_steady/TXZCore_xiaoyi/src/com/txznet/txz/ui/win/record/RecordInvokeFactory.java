package com.txznet.txz.ui.win.record;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.adapter.ChatContactListAdapter;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.ui.win.help.WinHelpDetail;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;

import android.text.TextUtils;

public class RecordInvokeFactory {
	public static interface RecordInvokeAdapter {
		public void show();

		public void dismiss();

		public void refreshState(int state);

		public void refreshVolume(int volume);

		public void refreshProgress(int val, int selection);

		public void addMsg(int owner, String txt);

		public void addListMsg(String data);

		public void showStock(byte[] data);

		public void showWeather(byte[] data);

		public void snapPager(boolean next);
	}

	private static final RecordInvokeAdapter LOCAL_ADAPTER_INNER = new RecordInvokeAdapter() {
		@Override
		public void show() {
			WinRecord.getInstance().show();
		}

		@Override
		public void dismiss() {
			WinRecord.getInstance().dismiss();
		}

		@Override
		public void refreshState(int status) {
			WinRecord.getInstance().notifyUpdateLayout(status);
		}

		@Override
		public void refreshVolume(int volume) {
			WinRecord.getInstance().notifyUpdateVolume(volume);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			WinRecord.getInstance().notifyUpdateProgress(val, selection);
		}

		@Override
		public void addMsg(int owner, String txt) {
			if (owner == ChatMessage.OWNER_SYS) {
				WinRecord.getInstance().addMsg(ChatMsgFactory.getSysTextMsg(txt));
			} else {
				WinRecord.getInstance().addMsg(ChatMsgFactory.getTextMsg(txt));
			}
		}

		@Override
		public void addListMsg(String data) {
			JSONBuilder doc = new JSONBuilder(data);
			Integer type = doc.getVal("type", Integer.class);
			if (type != null && type != 0) {
				WinRecord.getInstance().addMsg(ChatMsgFactory.getDisplayMsgFromJson(data));
			}
			// 发送联系人列表
			else {
				String strPrefix = doc.getVal("strPrefix", String.class);
				String strName = doc.getVal("strName", String.class);
				String strSuffix = doc.getVal("strSuffix", String.class);
				Boolean isMultiName = doc.getVal("isMultiName", Boolean.class);
				JSONObject[] contacts = doc.getVal("contacts", JSONObject[].class);
				String title = strPrefix + strName + strSuffix;

				List<ChatContactListAdapter.ContactItem> items = new ArrayList<ChatContactListAdapter.ContactItem>();
				if (contacts != null) {
					for (int i = 0; i < contacts.length; i++) {
						JSONBuilder contactJson = new JSONBuilder(contacts[i]);
						ChatContactListAdapter.ContactItem item = new ChatContactListAdapter.ContactItem();
						String province = contactJson.getVal("province", String.class);
						String city = contactJson.getVal("city", String.class);
						String isp = contactJson.getVal("isp", String.class);

						item.province = province;
						item.city = city;
						if (!isMultiName) {
							item.main = contactJson.getVal("number", String.class);
							item.isp = isp;
						} else {
							item.main = contactJson.getVal("name", String.class);
						}

						items.add(item);
					}
				}
				WinRecord.getInstance().addMsg(ChatMsgFactory.getSysContactListMsg(title, items));
			}
		}

		@Override
		public void showStock(byte[] data) {
			StockInfo infos = null;
			try {
				infos = StockInfo.parseFrom(data);
			} catch (Exception e) {
				LogUtil.loge("StockData parse error!");
				return;
			}
			WinRecord.getInstance().addMsg(ChatMsgFactory.getStockMessage(infos));
		}

		@Override
		public void showWeather(byte[] data) {
			WeatherInfos infos = null;
			try {
				infos = WeatherInfos.parseFrom(data);
			} catch (Exception e) {
				LogUtil.loge("WeatherData parse error!");
				return;
			}
			WinRecord.getInstance().addMsg(ChatMsgFactory.getWeatherMessage(infos));
		}

		@Override
		public void snapPager(boolean next) {
			// WinRecord.getInstance().snapPager(next);
		}
	};

	private static final RecordInvokeAdapter REMOTE_ADAPTER = new RecordInvokeAdapter() {

		@Override
		public void show() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show", null, null);
		}

		@Override
		public void dismiss() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.dismiss", null, null);
		}

		@Override
		public void refreshState(int status) {
			String data = new JSONBuilder().put("status", status).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh", data.getBytes(),
					null);
		}

		@Override
		public void refreshVolume(int volume) {
			String data = new JSONBuilder().put("volume", volume).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh.volume",
					data.getBytes(), null);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			String data = new JSONBuilder().put("progress", val).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh.progressbar",
					data.getBytes(), null);
		}

		@Override
		public void addMsg(int owner, String txt) {
			String data = new JSONBuilder().put("owner", owner).put("text", txt).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.chat", data.getBytes(), null);
		}

		@Override
		public void addListMsg(String data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list", data.getBytes(), null);
		}

		@Override
		public void showStock(byte[] data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show.stock", data, null);
		}

		@Override
		public void showWeather(byte[] data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show.weather", data, null);
		}

		@Override
		public void snapPager(boolean next) {
			if (next) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list.next", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list.pre", null, null);
			}
		}
	};

	private static final RecordInvokeAdapter THIRD_ADAPTER = new RecordInvokeAdapter() {
		@Override
		public void show() {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.show", null, null);
		}

		@Override
		public void dismiss() {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.dismiss", null, null);
		}

		@Override
		public void refreshState(int status) {
			String data = new JSONBuilder().put("status", status).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.status", data.getBytes(), null);
		}

		@Override
		public void refreshVolume(int volume) {
			String data = new JSONBuilder().put("volume", volume).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.volume", data.getBytes(), null);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			String data = new JSONBuilder().put("progress", val).put("selection", selection).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.progress", data.getBytes(), null);
		}

		@Override
		public void addMsg(int owner, String txt) {
			String data = new JSONBuilder().put("text", txt).toString();
			if (owner == ChatMessage.OWNER_SYS) {
				ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.chat.sys", data.getBytes(), null);
			} else if (owner == ChatMessage.OWNER_USER) {
				ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.chat.usr", data.getBytes(), null);
			}
		}

		@Override
		public void addListMsg(String data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.list", data.getBytes(), null);
		}

		@Override
		public void showStock(byte[] data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.stock", data, null);
		}

		@Override
		public void showWeather(byte[] data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.weather", data, null);
		}

		@Override
		public void snapPager(boolean next) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.list.pager", (next + "").getBytes(), null);
		}
	};

	static class RecordProxy implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					try {
						method.invoke(LOCAL_ADAPTER_INNER, args);
					} catch (Exception e) {
					}
				}
			}, 0);
			return null;
		}
	}

	private final static RecordInvokeAdapter LOCAL_ADAPTER = (RecordInvokeAdapter) Proxy.newProxyInstance(
			LOCAL_ADAPTER_INNER.getClass().getClassLoader(), LOCAL_ADAPTER_INNER.getClass().getInterfaces(),
			new RecordProxy());

	static class RecordProxy2 implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			try {
				method.invoke(THIRD_ADAPTER, args);
			} catch (Exception e) {
			}
			try {
				return method.invoke(LOCAL_ADAPTER, args);
			} catch (Exception e) {
			}
			return null;
		}
	}

	private final static RecordInvokeAdapter COPY_ADAPTER = (RecordInvokeAdapter) Proxy.newProxyInstance(
			LOCAL_ADAPTER_INNER.getClass().getClassLoader(), LOCAL_ADAPTER_INNER.getClass().getInterfaces(),
			new RecordProxy2());

	public static RecordInvokeAdapter getAdapter() {
		RecordInvokeAdapter NewRecordInvokeAdapter;
		if (hasThirdImpl()) {
			if (mReserveInnerRecordWin) {
				NewRecordInvokeAdapter = COPY_ADAPTER;
			} else {
				NewRecordInvokeAdapter = THIRD_ADAPTER;
			}
		} else {
			if (PackageManager.getInstance().mInstalledRecord) {
				NewRecordInvokeAdapter = REMOTE_ADAPTER;
			} else {
				NewRecordInvokeAdapter = LOCAL_ADAPTER;
			}
		}
		if (mCurRecordInvokeAdapter != null && mCurRecordInvokeAdapter != NewRecordInvokeAdapter) {
			if (mCurRecordInvokeAdapter == LOCAL_ADAPTER_INNER || mCurRecordInvokeAdapter == REMOTE_ADAPTER) {
				mCurRecordInvokeAdapter.dismiss();
			}
		}
		mCurRecordInvokeAdapter = NewRecordInvokeAdapter;
		return NewRecordInvokeAdapter;
	}

	private static boolean mReserveInnerRecordWin = false;
	// TODO 还原正确的值
	private static boolean mIsInnerHudRecordWin = false;
	private static String mThirdImpl;
	private static RecordInvokeAdapter mCurRecordInvokeAdapter;

	public static boolean hasThirdImpl() {
		return !TextUtils.isEmpty(mThirdImpl);
	}

	public static boolean isReserveInnerRecordWin() {
		return mReserveInnerRecordWin;
	}

	public static boolean isHudRecordWin() {
		return mIsInnerHudRecordWin;
	}

	public static byte[] invokeRecordWin(final String packageName, String command, byte[] data) {
		if (command.equals("prepare")) {
			mReserveInnerRecordWin = false;
			if (data != null) {
				try {
					JSONBuilder cfg = new JSONBuilder(data);
					mReserveInnerRecordWin = cfg.getVal("reserveInner", Boolean.class, false);
				} catch (Exception e) {
				}
			}
			mThirdImpl = packageName;
			return null;
		}
		if (command.equals("prepare.hud")) {
			try {
				if (data != null) {
					mIsInnerHudRecordWin = Boolean.parseBoolean(new String(data));
					JNIHelper.logd("mIsInnerHudRecordWin:" + mIsInnerHudRecordWin);
				}
			} catch (Exception e) {
			}
		}
		if (command.equals("clear")) {
			mReserveInnerRecordWin = false;
			mIsInnerHudRecordWin = false;
			mThirdImpl = null;
			return null;
		}
		if (command.equals("closeHelpWin")) {
			WinHelpDetail.getInstance().dismiss();
			WinHelpDetailTops.getInstance().dismiss();
		}
		if (command.equals("dissmiss")) {
			RecorderWin.close();
			return null;
		}
		if (command.equals("cancelClose")) {
			RecorderWin.cancelClose();
		}
		if (command.equals("showSysText")) {
			RecorderWin.addSystemMsg(new String(data));
			return null;
		}
		if (command.equals("enterSpecifyAsrSence")) {
			int sence = Integer.parseInt(new String(data));
			if (sence == 1) {
				String spk = NativeData.getResString("RS_RECORD_NAV");
				RecorderWin.open(spk, VoiceData.GRAMMAR_SENCE_NAVIGATE);
			} else if (sence == 2) {
				String spk = NativeData.getResString("RS_RECORD_CALL");
				RecorderWin.open(spk, VoiceData.GRAMMAR_SENCE_MAKE_CALL);
			}
		}
		if (command.equals("speakTextOnRecordWin")) {
			JSONBuilder json = new JSONBuilder(data);
			AsrManager.getInstance().setNeedCloseRecord(json.getVal("close", Boolean.class, true));
			RecorderWin.speakTextWithClose(json.getVal("text", String.class), new Runnable() {
				@Override
				public void run() {
					ServiceManager.getInstance().sendInvoke(packageName, "sdk.record.win.speakTextOnRecordWin.end",
							null, null);
				}
			});
			return null;
		}
		if(command.equals("enableAnim")){
			Boolean enable = Boolean.parseBoolean(new String(data));
			WinRecord.getInstance().enableAnim(enable);
			return null;
		}
		return null;
	}
}