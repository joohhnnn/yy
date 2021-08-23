package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.choice.ListHook;
import com.txznet.txz.component.choice.option.AsyncRepoCompentOption;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.choice.page.ResourcePage.OnGetDataCallback;
import com.txznet.txz.component.choice.repo.Repo;
import com.txznet.txz.component.choice.repo.Repo.OnRepoCallback;
import com.txznet.txz.jni.data.NativeData;

import android.text.TextUtils;

public class AsyncWorkChoice<E> extends CommWorkChoice<E> {
	private Repo<E> mRepo;
	private String mUserVoice;
	private int mTmpPage;
	private List<E> mCurrentList;
	private OnGetDataCallback<List<E>> mCallback;
	private List<E> mDeletedLists = new ArrayList<E>();

	public void resetRepo(Repo repo) {
		mRepo.reset();
		mRepo = null;
		mRepo = repo;
		mRepo.inject(mOnRepoCallback);
	}

	private OnRepoCallback<E> mOnRepoCallback = new OnRepoCallback<E>() {

		@Override
		public void onGetList(List<E> list) {
			LogUtil.logd("onGetList:" + list);
			mCurrentList = list;
			mDeletedLists.clear();
			if (mCurrentList == null || mCurrentList.isEmpty()) {
				doEmptyDataList();
				return;
			}
			
			if (mCallback != null) {
				mCallback.onGetData(list);
			}
		}

		@Override
		public void onNextPage(boolean bSucc) {
			if (bSucc) {
				mPage.nextPage();
			}
			onSnapPager(true, bSucc, mUserVoice);
		}

		@Override
		public void onLastPage(boolean bSucc) {
			if (bSucc) {
				mPage.lastPage();
			}
			onSnapPager(false, bSucc, mUserVoice);
		}

		@Override
		public void onSelectPage(boolean bSucc) {
			if (bSucc) {
				mPage.selectPage(mTmpPage);
			}
			if (!TextUtils.isEmpty(mUserVoice)) {
				selectSpeech(NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", mUserVoice));
			}
		}
	};
	
	private void doEmptyDataList() {
		if (getOption() instanceof AsyncRepoCompentOption) {
			ListHook<E> hook = ((AsyncRepoCompentOption<E>) getOption()).getHook();
			if (hook != null) {
				hook.onEmptyDataUpdate();
				return;
			}
		}

		selectCancel(SELECT_TYPE_BUSNIESS, null);
	}

	public AsyncWorkChoice(CompentOption<E> option) {
		super(option);
		if (option instanceof AsyncRepoCompentOption) {
			mRepo = ((AsyncRepoCompentOption) option).getRepo();
			mRepo.inject(mOnRepoCallback);
		}
	}

	@Override
	public void updateCompentOption(CompentOption<E> option, boolean updateShow) {
		if (option instanceof AsyncRepoCompentOption) {
			mRepo = ((AsyncRepoCompentOption) option).getRepo();
			mRepo.inject(mOnRepoCallback);
		}
		super.updateCompentOption(option, updateShow);
	}

	@Override
	protected void addWakeupCmds(List<E> data) {
		// 替换成当前的数据
		super.addWakeupCmds(mCurrentList);
	}

	@Override
	public boolean nextPage(String fromVoice) {
		mUserVoice = fromVoice;
		if (mRepo != null) {
			boolean b = mRepo.nextPage();
			if (!b) {
				onSnapPager(true, false, mUserVoice);
			}
			return b;
		}
		return false;
	}

	@Override
	public boolean lastPage(String fromVoice) {
		mUserVoice = fromVoice;
		if (mRepo != null) {
			boolean b = mRepo.lastPage();
			if (!b) {
				onSnapPager(false, false, fromVoice);
			}
			return b;
		}
		return false;
	}

	@Override
	public boolean selectPage(int page, String fromVoice) {
		mUserVoice = fromVoice;
		if (mRepo != null) {
			mTmpPage = page;
			return mRepo.selectPage(page, true, true);
		}
		return false;
	}
	
	@Override
	protected void onClearSelecting() {
		if (mRepo != null) {
			mRepo.reset();
		}
		super.onClearSelecting();
	}

	@Override
	protected ResourcePage<List<E>, E> createPage(List<E> sources) {
		return new ResListPage<E>(((AsyncRepoCompentOption<E>) getOption()).getTotalSize()) {

			@Override
			protected int numOfPageSize() {
				return getOption().getNumPageSize();
			}

			@Override
			public List<E> getResource() {
				return mCurrentList;
			}

			@Override
			public int getCurrPageSize() {
				return mCurrentList != null ? mCurrentList.size() : 0;
			}

			@Override
			public E getItemFromCurrPage(int idx) {
				if (mCurrentList != null || mCurrentList.size() > idx && idx >= 0) {
					return mCurrentList.get(idx);
				}
				return null;
			}

			@Override
			public E getItemFromSource(int idx) {
				return getItemFromCurrPage(idx);
			}

			@Override
			public void requestCurrPage(OnGetDataCallback<List<E>> callback) {
				mCallback = callback;
				if (mRepo != null) {
					boolean b = mRepo.selectPage(currPage + 1, false, true);
					// 请求数据失败
					if (!b) {
						doEmptyDataList();
					}
				}
			}

			@Override
			protected void notifyPageInner() {
				super.notifyPageInner();
			}

			@Override
			public E removeFromSource(int cIdx, int tIdx) {
				if (mRepo != null) {
					if (cIdx >= 0 && cIdx < mCurrentList.size()) {
						E e = mCurrentList.get(cIdx);
						mDeletedLists.add(e);
						return mRepo.removeFromSource(e);
					}
				}
				return null;
			}

			@Override
			public E notifyRemoveIdx(int idx, boolean isCurrPage) {
				if (mDeletedLists.contains(mCurrentList.get(idx))) {
					return null;
				}
				return super.notifyRemoveIdx(idx, isCurrPage);
			}

			@Override
			public ResourcePage<List<E>, E> reset() {
				if (mRepo != null) {
					mRepo.reset();
				}
				return super.reset();
			}
		};
	}

}