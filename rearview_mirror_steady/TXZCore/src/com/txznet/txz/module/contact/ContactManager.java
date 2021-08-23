package com.txznet.txz.module.contact;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

/**
 * 联系人管理模块，负责联系人同步和更新监听
 * 
 * @author bihongpi
 *
 */
public class ContactManager extends IModule {
	static ContactManager sModuleInstance = new ContactManager();

	private ContactManager() {
	}

	public static ContactManager getInstance() {
		return sModuleInstance;
	}

	// //////////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_AfterInitSuccess() {
		syncContacts();
		if (DebugCfg.ENABLE_CONTACT_DEBUG) {
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(final Context context, final Intent intent) {
					String act = intent.getStringExtra("action");
					if (TextUtils.isEmpty(act)) {
						LogUtil.loge("contact test : act is empty");
					} else if (TextUtils.equals(act,"getContacts")) {
						LogUtil.loge("contact test : act = getContacts");
						MobileContacts mobileContacts = getMobileContacts();
						if (mobileContacts != null && mobileContacts.cons != null) {
							LogUtil.loge("contact test : act = getContacts , contacts size "+ mobileContacts.cons.length+"!" );
							for (MobileContact con : mobileContacts.cons) {
								LogUtil.loge("contact test :"+ con.name + "-" + Arrays.toString(con.phones));
							}
						} else {
							LogUtil.loge("contact test : act = getContacts , contacts is empty!" );
						}
						LogUtil.loge("contact test : act = getContacts end!" );
					}
				}
			},new IntentFilter("com.txznet.txz.contact.test"));
		}
		return super.initialize_AfterInitSuccess();
	}

	// //////////////////////////////////////////////////////////////////////////////
	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.contact.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				if("findByName".equals(command)){
					String name = (String) args[0];
					int score = 6000;
					int maxCount = 3;
					if(args[1] != null){
						score = (Integer) args[1];
					}
					if(args[2] != null){
						maxCount = (Integer) args[2];
					}
					ContactData.MobileContacts cons = NativeData.findContactsByName(name, score, maxCount);
					return cons;
				}else if("findByNumber".equals(command)){
					String number = (String) args[0];
					ContactData.MobileContacts cons = NativeData.findContactsByNumber(number);
					return cons;
				}else if("getContacts".equals(command)){
					return getMobileContacts();
				}
				
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}
	
	ContentObserver mObserver = null;
	ContentObserver mCallLogObserver = null;
	boolean mReadContactsEmpty = true;
	public boolean mEnableServiceContact = true;

	Runnable mGetContactsRunnable = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logi("queryContact start");
			ArrayList<MobileContact> consList = new ArrayList<MobileContact>();
			getPhoneContactsReal(consList);
			// getSIMContacts(consList);
			if (consList.size() <= 0) {
				// 获取不到联系人认为没有权限，3分钟定时重试
				AppLogic.removeSlowGroundCallback(mGetContactsRunnable);
				AppLogic.runOnSlowGround(mGetContactsRunnable, 180000);
			} else {
				mReadContactsEmpty = false;
			}
			MobileContacts contacts = new MobileContacts();
			contacts.cons = new MobileContact[consList.size()];
			for (int i = 0; i < consList.size(); ++i) {
				contacts.cons[i] = consList.get(i);
			}
			mLocalMCons = contacts;

			if (mSyncedRemoteContacts)
				return;
			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_FRIENDSHIP,
					ContactData.SUBEVENT_UPDATED_MOBILE_CONTACT_LIST,
					MessageNano.toByteArray(contacts));
		}
	};
	
	private MobileContacts mLocalMCons;
	private MobileContacts mRemoteMCons;
	
	public MobileContacts getMobileContacts() {
		if (mRemoteMCons != null) {
			return mRemoteMCons;
		}

		return mLocalMCons;
	}

	Runnable mGetContactsRunnableFirst = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mGetContactsRunnable.run();
			if (mReadContactsEmpty) {
				// TtsManager.getInstance().speakText("没有同步到任何联系人");
				// TXZApp.showToast("没有同步到任何联系人");
			}
		}
	};

	public synchronized void syncContacts() {
		if (mObserver == null) {
			mObserver = new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					AppLogic.removeSlowGroundCallback(mGetContactsRunnable);
					AppLogic.runOnSlowGround(mGetContactsRunnable, 10000);
				}

			};
			mCallLogObserver = new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					AppLogic.removeSlowGroundCallback(mGetContactsRunnable);
					AppLogic.runOnSlowGround(mGetContactsRunnable, 10000);
				}
			};

			// 注册监听通话记录数据库
			GlobalContext
					.get()
					.getContentResolver()
					.registerContentObserver(CallLog.Calls.CONTENT_URI, true,
							mCallLogObserver);
			// 注册监听联系人数据库
			GlobalContext
					.get()
					.getContentResolver()
					.registerContentObserver(
							ContactsContract.Contacts.CONTENT_URI, true,
							mObserver);
		}

		AppLogic.runOnSlowGround(mGetContactsRunnableFirst, 1000);

	}

	private static String[] PHONES_PROJECTION = new String[] {
			Phone.DISPLAY_NAME, // 显示的名字
			Phone.NUMBER, // 电话号码
			Phone.LAST_TIME_CONTACTED, // 最后联系时间
			Phone.TIMES_CONTACTED, // 联系的次数
			Phone.CONTACT_STATUS_TIMESTAMP // 联系人最后更新的时间戳
	};

	private void addContactRecToList(ArrayList<MobileContact> consList,
			Cursor phoneCursor) {
		if (phoneCursor != null) {
			while (phoneCursor.moveToNext()) {
				AppLogic.heartbeatSlowGround();
				try {
					String contactName = phoneCursor.getString(0).trim();
					String phoneNumber = phoneCursor.getString(1).trim();
					if(TextUtils.isEmpty(contactName)) {
						continue;
					}
					if (TextUtils.isEmpty(phoneNumber)) {
						phoneNumber = "empty";
					}
					MobileContact contact = new MobileContact();
					contact.name = contactName;
					contact.phones = new String[] { phoneNumber };
					contact.uint32LastTimeContacted = (int) (phoneCursor
							.getLong(2) / 1000);
					contact.uint32TimesContacted = phoneCursor.getInt(3);
					contact.uint32LastTimeUpdated = (int) (phoneCursor
							.getLong(4) / 1000);
					consList.add(contact);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
			phoneCursor.close();
		}
	}

	public void getPhoneContactsReal(ArrayList<MobileContact> consList) {
		ContentResolver resolver = GlobalContext.get().getContentResolver();
		try {
			Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
					PHONES_PROJECTION, null, null, null);
			addContactRecToList(consList, phoneCursor);
			phoneCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getSIMContacts(ArrayList<MobileContact> consList) {
		// TODO xiaomi crash
		try {
			ContentResolver resolver = GlobalContext.get().getContentResolver();
			Uri uri = Uri.parse("content://icc/adn");
			Cursor phoneCursor = resolver.query(uri, PHONES_PROJECTION, null,
					null, null);
			addContactRecToList(consList, phoneCursor);
			phoneCursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean mSyncedRemoteContacts = false;

	/**
	 * 同步远程联系人，调用后将不再监听系统联系人
	 * 
	 * @param cons
	 */
	public synchronized void syncRemoteContacts(MobileContacts contacts) {
		JNIHelper.logd(" contacts:"+contacts.cons.length);
		mRemoteMCons = contacts;
		mSyncedRemoteContacts = true;
		AppLogic.removeSlowGroundCallback(mGetContactsRunnableFirst);
		AppLogic.removeSlowGroundCallback(mGetContactsRunnable);
		if (mCallLogObserver != null) {
			GlobalContext.get().getContentResolver()
					.unregisterContentObserver(mCallLogObserver);
			mCallLogObserver = null;
		}
		if (mObserver != null) {
			GlobalContext.get().getContentResolver()
					.unregisterContentObserver(mObserver);
			mObserver = null;
		}

		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_FRIENDSHIP,
				ContactData.SUBEVENT_UPDATED_MOBILE_CONTACT_LIST,
				MessageNano.toByteArray(contacts));
	}
}
