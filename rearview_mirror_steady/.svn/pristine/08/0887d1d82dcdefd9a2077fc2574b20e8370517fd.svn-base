package com.txznet.txz.component.choice.page;

import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txznet.txz.component.choice.list.CallWorkChoice.Contacts;

public abstract class ResMobilePage extends ResourcePage<Contacts, MobileContact> {
	protected boolean isMultiName;

	public ResMobilePage(Contacts resources, int totalSize, boolean isMultiName) {
		super(resources, totalSize);
		this.isMultiName = isMultiName;
	}

	@Override
	protected void clearCurrRes(Contacts currRes) {
		currRes = null;
	}

	@Override
	protected Contacts notifyPage(int sIdx, int len, Contacts con) {
		final MobileContacts sourceRes = con.cons;
		Contacts contacts = new Contacts();
		contacts.event = con.event;
		MobileContacts cons = new MobileContacts();
		for (int i = 0; i < len; i++) {
			if (isMultiName) {
				if (cons.cons == null || cons.cons.length != len) {
					cons.cons = new MobileContact[len];
				}
				cons.cons[i] = sourceRes.cons[sIdx + i];
			} else {
				if (cons.cons == null || cons.cons.length <= 0) {
					cons.cons = new MobileContact[1];
					cons.cons[0] = new MobileContact();
					cons.cons[0].name = sourceRes.cons[0].name;
					cons.cons[0].score = sourceRes.cons[0].score;
					cons.cons[0].uint32LastTimeContacted = sourceRes.cons[0].uint32LastTimeContacted;
					cons.cons[0].uint32LastTimeUpdated = sourceRes.cons[0].uint32LastTimeUpdated;
					cons.cons[0].uint32TimesContacted = sourceRes.cons[0].uint32TimesContacted;
				}
				if (cons.cons[0].phones == null || cons.cons[0].phones.length != len) {
					cons.cons[0].phones = new String[len];
				}
				cons.cons[0].phones[i] = sourceRes.cons[0].phones[sIdx + i];
			}
		}
		contacts.cons = cons;
		return contacts;
	}

	@Override
	protected int getCurrResSize(Contacts con) {
		final MobileContacts currRes = con.cons;
		if (currRes != null) {
			if (isMultiName) {
				return currRes.cons.length;
			} else {
				return currRes.cons[0].phones.length;
			}
		}
		return 0;
	}

	@Override
	public MobileContact getItemFromCurrPage(int idx) {
		if (mCurrRes != null) {
			if (isMultiName) {
				return mCurrRes.cons.cons[idx];
			} else {
				return mCurrRes.cons.cons[0];
			}
		}
		return null;
	}
	
	@Override
	public MobileContact getItemFromSource(int idx) {
		if (mSourceRes != null) {
			if (isMultiName) {
				return mSourceRes.cons.cons[idx];
			} else {
				return mSourceRes.cons.cons[0];
			}
		}
		return null;
	}
}
