package com.txznet.sdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;

import android.text.TextUtils;

/**
 * 类名：呼叫管理器
 * 类描述：电话功能的管理类，负责语音发起电话相关操作的实现与控制
 *        主要包含电话工具实现类（CallTool）和蓝牙状态监听类(CallToolStatusListener)
 */
public class TXZCallManager {
	private static TXZCallManager sInstance = new TXZCallManager();

	private TXZCallManager() {

	}

	/**
	 * 获取单例
	 *
	 * @return 实例
	 */
	public static TXZCallManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {

		if (mHasSetTool)
			setCallTool(mCallTool);

		syncContactsAgain();

		if (mHasSetTool && mCallTool != null) {
			if (mDisableReason == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.enable", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.disable", mDisableReason.getBytes(), null);
			}
		}

		if (mHasSyncLocalBt) {
			syncLocalBluetoothInfo(mLocalBtName, mLocalBtMac);
		}

		if (mHasSyncRemoteBt) {
			syncLocalBluetoothInfo(mRemoteBtName, mRemoteBtMac);
		}
		
		if (mCanAutoCall != null) {
			setCanAutoCall(mCanAutoCall);
		}
	}

	/**
	 * 再次同步联系人
	 */
	void syncContactsAgain() {
		if (mLastContactData != null)
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.call.sync", mLastContactData, null);
	}

	/**
	 * 类名：联系人
	 * 类描述：电话联系人抽象化，定义联系人的相关属性
	 */
	public static class Contact {

		/**
		 * 变量名：联系人姓名
		 */
		protected String name;

		/**
		 * 变量名：联系人号码
		 */
		protected String number;

		/**
		 * 变量名：与此联系人最后联系时间，搜索同一联系人有多结果时，会依据此时间排序
		 */
		protected long lastTimeContacted;

		/**
		 * 方法名：获取名字
		 * 方法描述：获取联系人名
		 *
		 * @return 联系人名字符串
		 */
		public String getName() {
			return name;
		}

		/**
		 * 方法名：设置名字
		 * 方法描述：设置联系人名
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * 方法名：获取号码
		 * 方法描述：获取联系人号码
		 *
		 * @return 联系人号码符串
		 */
		public String getNumber() {
			return number;
		}

		/**
		 * 方法名：设置号码
		 * 方法描述：设置联系人号码
		 */
		public void setNumber(String number) {
			this.number = number;
		}

		/**
		 * 方法名：获取最近联系时间
		 * 方法描述：获取联系人号码最近一次联系时间
		 *
		 * @return 最近一次联系的具体时间
		 */
		public long getLastTimeContacted() {
			return lastTimeContacted;
		}

		/**
		 * 方法名：设置联系时间
		 * 方法描述：设置联系人最近一次联系时间
		 */
		public void setLastTimeContacted(long lastTimeContacted) {
			this.lastTimeContacted = lastTimeContacted;
		}
	}

	byte[] mLastContactData = null;

	/**
	 * 方法名：同步联系人
	 * 方法描述：车机连接蓝牙后，主动同步手机端端联系人至语音，以实现离线下语音拨打电话功能
	 *
	 * @param cons 联系人集合
	 */
	public void syncContacts(Collection<Contact> cons) {
		MobileContacts contacts = new MobileContacts();
		contacts.cons = new MobileContact[cons.size()];
		int i = 0;
		Map<String, Integer> conMap = new HashMap<String, Integer>();
		for (Contact con : cons) {
			if (TextUtils.isEmpty(con.name))
				continue;
			if (TextUtils.isEmpty(con.number))
				con.number = "empty";
			if (conMap.containsKey(con.name)) {
				conMap.put(con.name, conMap.get(con.name) + 1);
				if (conMap.get(con.name) > 10) {
					continue;
				}
			} else {
				conMap.put(con.name, 1);
			}
			contacts.cons[i] = new MobileContact();
			contacts.cons[i].name = con.name;
			contacts.cons[i].phones = new String[] { con.number };
			contacts.cons[i].uint32LastTimeContacted = (int) (con.lastTimeContacted / 1000);
			++i;
		}
		mLastContactData = MessageNano.toByteArray(contacts);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.call.sync", mLastContactData, null);
	}

	/**
	 * 接口名：电话工具状态监听类
	 * 接口描述：车机与手机端蓝牙连接发生改变时，通过此接口通知语音蓝牙变化后的状态
	 *           语音会根据蓝牙状态，做出对应状态处理
	 */
	public static interface CallToolStatusListener {

		/**
		 * 方法名：车机发起呼叫
		 * 方法描述：用户主动拨号时，使用此接口通知语音
		 *
		 * @param con 主动呼叫的联系人
		 */
		public void onMakeCall(Contact con);

		/**
		 * 方法名：车机有来电
		 * 方法描述：车机与手机蓝牙连接，且车机端收到外部来电时，使用此接口通知语音，语音会根据调用信息作出语音播报
		 *
		 * @param con     来电的联系人
		 * @param needTts 是否需要语音进行来电TTS播报
		 * @param needAsr 是否支持“接听 ”、“挂断”的声控来电功能
		 */
		public void onIncoming(Contact con, boolean needTts, boolean needAsr);

		/**
		 * 方法名：蓝牙空闲中
		 * 方法描述：车机与手机蓝牙连接，且蓝牙空闲并可用时通过此接口通知语音
		 */
		public void onIdle();

		/**
		 * 方法名：蓝牙电话接通中
		 * 方法描述：车机与手机蓝牙连接，且蓝牙处理通话中时，通过此接口通知语音
		 */
		public void onOffhook();

		/**
		 * 方法名：蓝牙电话可用
		 * 方法描述：车机与手机蓝牙连接后，通过此接口通知语音
		 */
		public void onEnabled();

		/**
		 * 方法名：蓝牙电话禁用
		 * 方法描述：车机端蓝牙需要禁用时，通过此接口通知语音
		 *
		 * @param reason 禁用蓝牙电话的原因，用户发起蓝牙语义时会播报此原因
		 */
		public void onDisabled(String reason);
	}

	/**
	 * 接口名：呼叫工具类
	 * 接口描述：蓝牙电话相关控制逻辑实现接口，语音将回调此接口控制车机作出相应动作
	 *           如：用户发起打电话给小踢，则此接口内onMakeCall会被语音回调。
	 *
	 * @author txz
	 */
	public static interface CallTool {

		/**
		 * 枚举类名：呼叫状态类
		 * 枚举类描述：呼叫状态枚举类
		 */
		public static enum CallStatus {

			/**
			 * 枚举名称：呼叫空闲
			 * 枚举描述：呼叫状态空闲中
			 */
			CALL_STATUS_IDLE,

			/**
			 * 枚举名称：呼叫响铃中
			 * 枚举描述：呼叫状态响铃中
			 */
			CALL_STATUS_RINGING,

			/**
			 * 枚举名称：呼叫通话中
			 * 枚举描述：呼叫状态通话中
			 */
			CALL_STATUS_OFFHOOK
		}

		/**
		 * 方法名：获取当前通话状态
		 * 方法描述：首次设置蓝牙电话工具时，语音会回调此方法，以获取蓝牙实时状态
		 *
		 * @return 当前蓝牙状态类型
		 */
		public CallStatus getStatus();

		/**
		 * 方法名：语音发起呼叫
		 * 方法描述：呼叫动作的实现，用户通过语音发起呼叫动作时，会回调此方法
		 *
		 * @param con 呼叫联系人
		 * @return 此返回值预留
		 */
		public boolean makeCall(Contact con);

		/**
		 * 方法名：语音接听来电
		 * 方法描述：接听来电动作的实现，使用语音的来电“接听”功能时，会回调此方法
		 *
		 * @return 此返回值预留
		 */
		public boolean acceptIncoming();

		/**
		 * 方法名：语音拒接来电
		 * 方法描述：拒接来电动作的实现，使用语音的来电“拒接”功能时，会回调此方法
		 *
		 * @return 此返回值预留
		 */
		public boolean rejectIncoming();

		/**
		 * 方法名：语音挂断来电（此方法预留）
		 * 方法描述：拒接来电动作的实现，使用语音的来电“拒接”功能时，会回调此方法
		 *
		 * @return 此返回值预留
		 */
		public boolean hangupCall();

		/**
		 * 方法名：设置蓝牙状态监听器
		 * 方法描述：获取语音状态监听器，通过使用此监听器通知语音实时蓝牙状态的变化
		 *
		 * @param listener 状态监听器，可手动保存，在蓝牙变化时，使用其通知语音
		 */
		public void setStatusListener(CallToolStatusListener listener);
	}

	private boolean mHasSetTool = false;
	private CallTool mCallTool = null;
	private String mDisableReason = null;

	/**
	 * 方法名：设置呼叫工具
	 * 方法描述：设置呼叫工具，通过此工具实现车机端蓝牙操作，如接听电话、拨打电话等
	 * 			设置工具后，才可以使用电话声控功能
	 *
	 * @param tool 呼叫工具实例
	 */
	public void setCallTool(CallTool tool) {
		mHasSetTool = true;
		mCallTool = tool;

		if (tool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.call.cleartool", null, null);
			return;
		}
		tool.setStatusListener(new CallToolStatusListener() {
			@Override
			public void onOffhook() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.notifyOffhook", null, null);
			}

			@Override
			public void onMakeCall(Contact con) {
				JSONObject json = new JSONObject();
				try {
					json.put("name", con.name);
					json.put("num", con.number);
				} catch (Exception e) {
				}
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.notifyMakeCall",
						json.toString().getBytes(), null);
			}

			@Override
			public void onIncoming(Contact con, boolean needTts, boolean needAsr) {
				JSONObject json = new JSONObject();
				try {
					json.put("tts", needTts);
					json.put("asr", needAsr);
					json.put("name", con.name);
					json.put("num", con.number);
				} catch (Exception e) {
				}
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.notifyIncoming",
						json.toString().getBytes(), null);
			}

			@Override
			public void onIdle() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.notifyIdle", null, null);
			}

			@Override
			public void onEnabled() {
				mDisableReason = null;
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.enable", null, null);
			}

			@Override
			public void onDisabled(String reason) {
				mDisableReason = reason;
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.call.disable", reason.getBytes(), null);
			}
		});
		TXZService.setCommandProcessor("tool.call.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("getStatus")) {
					try {
						switch (mCallTool.getStatus()) {
						case CALL_STATUS_IDLE:
							return "idle".getBytes();
						case CALL_STATUS_OFFHOOK:
							return "offhook".getBytes();
						case CALL_STATUS_RINGING:
							return "ringing".getBytes();
						default:
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				if (command.equals("makeCall")) {
					try {
						Contact con = new Contact();
						JSONObject json = new JSONObject(new String(data));
						con.setName(json.getString("name"));
						con.setNumber(json.getString("num"));
						mCallTool.makeCall(con);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				if (command.equals("acceptIncoming")) {
					try {
						mCallTool.acceptIncoming();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				if (command.equals("rejectIncoming")) {
					try {
						mCallTool.rejectIncoming();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				if (command.equals("hangupCall")) {
					try {
						mCallTool.hangupCall();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				return null;
			}
		});

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.call.settool", null, null);
	}

	private boolean mHasSyncLocalBt = false;
	private String mLocalBtName;
	private String mLocalBtMac;

	/**
	 * 方法名：同步当前蓝牙信息至语音（方法预留）
	 * 方法描述：将当前设备的详细蓝牙信息同步给语音
	 *
	 * @param name 蓝牙设备名
	 * @param mac  蓝牙MAC地址
	 */
	public void syncLocalBluetoothInfo(String name, String mac) {
		mLocalBtName = name;
		mLocalBtMac = mac;
		mHasSyncLocalBt = true;

		JSONBuilder json = new JSONBuilder();
		json.put("name", mLocalBtName);
		json.put("mac", mLocalBtMac);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.bt.localinfo", json.toBytes(), null);
	}

	private boolean mHasSyncRemoteBt = false;
	private String mRemoteBtName;
	private String mRemoteBtMac;

	/**
	 * 方法名：同步远程端（与设备连接的对端）蓝牙信息，连接的手机的蓝牙信息（方法预留）
	 * 方法描述：将远程蓝牙设备（一般为手机端）的详细蓝牙信息同步给语音
	 *
	 * @param name 蓝牙设备名
	 * @param mac  蓝牙MAC地址
	 */
	public void syncRemoteBluetoothInfo(String name, String mac) {
		mRemoteBtName = name;
		mRemoteBtMac = mac;
		mHasSyncRemoteBt = true;

		JSONBuilder json = new JSONBuilder();
		json.put("name", mRemoteBtName);
		json.put("mac", mRemoteBtMac);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.bt.remoteinfo", json.toBytes(), null);
	}
	
	Boolean mCanAutoCall;

	/**
	 * 方法名：设置联系人选择时是否支持自动拨打电话
	 * 方法描述：语音搜索联系人时，默认进度条超时后，会自动拨打号码，可以通过此接口开关对应功能
	 *
	 * @param canAuto 是否支持
	 */
	public void setCanAutoCall(boolean canAuto) {
		this.mCanAutoCall = canAuto;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.call.canProgress", (canAuto + "").getBytes(),
				null);
	}
}
