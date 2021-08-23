package com.txznet.txz.component.choice.repo;

import java.util.ArrayList;
import java.util.List;

import com.txz.ui.map.UiMap.NavInfo;
import com.txznet.txz.module.nav.NavInscriber;
import com.txznet.txz.module.nav.NavInscriber.DbNavInfo;
import com.txznet.txz.module.nav.NavInscriber.NavInfoCache;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class RepoNavInscriber extends Repo<DbNavInfo> {
	private static final int NEXT_PAGE = 1;
	private static final int LAST_PAGE = 2;
	private static final int SELECT_PAGE = 3;
	private static final int REQUEST_PAGE = 4;
	private static final int NOTIFY_DELETE = 5;

	private NavInfoCache mCache;
	private Handler mHandler = null;
	private HandlerThread mHandlerThread = null;
	private boolean mIsDestination;

	public RepoNavInscriber(NavInfoCache cache, boolean isDestination) {
		this.mCache = cache;
		this.mIsDestination = isDestination;
		init();
	}

	private void init() {
		mHandlerThread = new HandlerThread("RepoNavHistory");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper()) {

			@Override
			public void handleMessage(Message msg) {
				handleMsg(msg);
			}
		};
	}

	private void handleMsg(Message msg) {
		List<DbNavInfo> navInfos = new ArrayList<DbNavInfo>();
		switch (msg.what) {
		case NEXT_PAGE:
			if (mCallback != null) {
				mCallback.onNextPage(true);
			}
			break;
		case LAST_PAGE:
			if (mCallback != null) {
				mCallback.onLastPage(true);
			}
			break;
		case SELECT_PAGE:
			Object obj = msg.obj;
			if (mCallback != null && obj != null && ((Boolean) obj)) {
				mCallback.onSelectPage(true);
			}
			break;
		case NOTIFY_DELETE:
			DbNavInfo info = (DbNavInfo) msg.obj;
//			mCache.delete(info.uint32Time);
			if (mIsDestination) {
				NavInscriber.getInstance().removeDestRecord(info);
			} else {
				NavInscriber.getInstance().removeFromRecord(info);
			}
			return;
		default:
			break;
		}
		List<DbNavInfo> infos = mCache.queryByCurrPage();
		if (infos != null) {
			navInfos.addAll(infos);
		}
		if (mCallback != null) {
			mCallback.onGetList(navInfos);
		}
	}

	@Override
	public boolean nextPage() {
		boolean bNext = mCache.nextPage();
		if (bNext) {
			Message msg = Message.obtain();
			msg.what = NEXT_PAGE;
			mHandler.sendMessage(msg);
		}
		return bNext;
	}

	@Override
	public boolean lastPage() {
		boolean bLast = mCache.lastPage();
		if (bLast) {
			Message msg = Message.obtain();
			msg.what = LAST_PAGE;
			mHandler.sendMessage(msg);
		}
		return bLast;
	}

	@Override
	public boolean selectPage(int page, boolean needVoice, boolean needData) {
		boolean bSelect = mCache.selectPage(page);
		if (!needData) {
			return bSelect;
		}
		
		if (bSelect) {
			Message msg = Message.obtain();
			msg.what = SELECT_PAGE;
			msg.arg1 = page;
			msg.obj = needVoice;
			mHandler.sendMessage(msg);
		}
		return bSelect;
	}

	@Override
	public void reset() {
		mCache.clear();
	}

	@Override
	public void requestCurrPage() {
		Message msg = Message.obtain();
		msg.what = REQUEST_PAGE;
		mHandler.sendMessage(msg);
	}
	
	@Override
	public DbNavInfo removeFromSource(DbNavInfo info) {
		Message msg = Message.obtain();
		msg.what = NOTIFY_DELETE;
		msg.obj = info;
		mHandler.sendMessage(msg);
		return info;
	}
}