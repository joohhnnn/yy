package com.txznet.txz.module.contact;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

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
		return super.initialize_AfterInitSuccess();
	}

	// //////////////////////////////////////////////////////////////////////////////

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

			if (mSyncedRemoteContacts)
				return;
			JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_FRIENDSHIP,
					ContactData.SUBEVENT_UPDATED_MOBILE_CONTACT_LIST,
					MessageNano.toByteArray(contacts));
		}
	};

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

	public void syncContacts() {
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
				try {
					String contactName = phoneCursor.getString(0).trim();
					String phoneNumber = phoneCursor.getString(1).trim();
					if (TextUtils.isEmpty(phoneNumber)
							|| TextUtils.isEmpty(contactName))
						continue;
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
	public void syncRemoteContacts(MobileContacts contacts) {
		mSyncedRemoteContacts = true;
		AppLogic.removeSlowGroundCallback(mGetContactsRunnableFirst);
		AppLogic.removeSlowGroundCallback(mGetContactsRunnable);
		GlobalContext.get().getContentResolver()
				.unregisterContentObserver(mCallLogObserver);
		GlobalContext.get().getContentResolver()
				.unregisterContentObserver(mObserver);

		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_FRIENDSHIP,
				ContactData.SUBEVENT_UPDATED_MOBILE_CONTACT_LIST,
				MessageNano.toByteArray(contacts));
	}

}
