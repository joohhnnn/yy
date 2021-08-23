package com.txznet.comm.ui.dialog2;

import java.util.List;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.txznet.txz.comm.R;

public abstract class WinConfirmList extends WinConfirm {
	/**
	 * 构建对话框数据类型
	 * 
	 * @author pppi
	 *
	 */
	public static class WinConfirmListBuildData extends WinConfirmBuildData {
		List<String> mMsgList;
		String[] mMsgArray;
		ListAdapter mListAdapter;

		/**
		 * 通过适配器构建
		 * 
		 * @param adpter
		 *            消息列表适配器
		 */
		public WinConfirmListBuildData(ListAdapter adpter) {
			this.mListAdapter = adpter;
		}

		/**
		 * 通过消息列表构建
		 * 
		 * @param msgs
		 *            消息列表
		 */
		public WinConfirmListBuildData(String... msgs) {
			this.mMsgArray = msgs;
		}

		/**
		 * 通过消息列表构建
		 * 
		 * @param msgs
		 *            消息列表
		 */
		public WinConfirmListBuildData(List<String> msgs) {
			this.mMsgList = msgs;
		}

		@Override
		public void check() {
			super.check();
			this.addExtraInfo("dialogType", WinConfirmList.class.getSimpleName());
		}
	};

	/**
	 * 构建数据
	 */
	WinConfirmListBuildData mWinConfirmListBuildData;

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinConfirmList(WinConfirmListBuildData data) {
		this(data, true);
	}

	/**
	 * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
	 * 
	 * @param data
	 *            构建数据
	 * @param init
	 *            是否初始化，自己构造时传true，派生类构造时传false
	 */
	protected WinConfirmList(WinConfirmListBuildData data, boolean init) {
		super(data, false);

		mWinConfirmListBuildData = data;

		if (init) {
			initDialog();
		}
	}

	@Override
	protected View createView() {
		View view = super.createView();
		mViewHolder.mText.setVisibility(View.GONE);
		mViewHolder.mTextList.setVisibility(View.VISIBLE);

		updateListAdapter();
		return view;
	};

	protected void updateListAdapter() {
		if (mWinConfirmListBuildData.mListAdapter != null) {
			mViewHolder.mTextList
					.setAdapter(mWinConfirmListBuildData.mListAdapter);
		} else if (mWinConfirmListBuildData.mMsgList != null) {
			mViewHolder.mTextList.setAdapter(new ArrayAdapter<String>(
					getContext(), R.layout.comm_win_list_item,
					mWinConfirmListBuildData.mMsgList));
		} else if (mWinConfirmListBuildData.mMsgArray != null) {
			mViewHolder.mTextList.setAdapter(new ArrayAdapter<String>(
					getContext(), R.layout.comm_win_list_item,
					mWinConfirmListBuildData.mMsgArray));
		}
	}

	/**
	 * 设置列表消息列表，已废弃，建议使用构造数据传入
	 * 
	 * @param msgs
	 *            消息列表
	 * @return
	 */
	@Deprecated
	public WinConfirmList setListAdapter(List<String> msgs) {
		mWinConfirmListBuildData.mListAdapter = null;
		mWinConfirmListBuildData.mMsgList = msgs;
		mWinConfirmListBuildData.mMsgArray = null;

		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateListAdapter();
			}
		}, 0);

		return this;
	}

	/**
	 * 设置列表消息列表，已废弃，建议使用构造数据传入
	 * 
	 * @param msgs
	 *            消息列表
	 * @return
	 */
	@Deprecated
	public WinConfirmList setListAdapter(String[] msgs) {
		mWinConfirmListBuildData.mListAdapter = null;
		mWinConfirmListBuildData.mMsgList = null;
		mWinConfirmListBuildData.mMsgArray = msgs;

		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateListAdapter();
			}
		}, 0);

		return this;
	}

	/**
	 * 设置列表适配器，已废弃，建议使用构造数据传入
	 * 
	 * @param adapter
	 *            适配器
	 * @return
	 */
	@Deprecated
	public WinConfirmList setListAdapter(ListAdapter adapter) {
		mWinConfirmListBuildData.mListAdapter = adapter;
		mWinConfirmListBuildData.mMsgList = null;
		mWinConfirmListBuildData.mMsgArray = null;

		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateListAdapter();
			}
		}, 0);

		return this;
	}
}
