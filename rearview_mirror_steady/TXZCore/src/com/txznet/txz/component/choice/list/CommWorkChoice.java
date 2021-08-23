package com.txznet.txz.component.choice.list;

import java.util.List;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.ListHook;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.option.ListHookCompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;

public class CommWorkChoice<E> extends WorkChoice<List<E>, E> {
	protected ListHook<E> mHook;

	public CommWorkChoice(CompentOption<E> option) {
		super(option);
		if (option instanceof ListHookCompentOption) {
			mHook = ((ListHookCompentOption) option).getHook();
			mHook.onGetWorkSpace(this);
		}
	}

	@Override
	public void updateCompentOption(CompentOption<E> option, boolean updateShow) {
		if (option instanceof ListHookCompentOption) {
			mHook = ((ListHookCompentOption) option).getHook();
			mHook.onGetWorkSpace(this);
		}
		super.updateCompentOption(option, updateShow);
	}

	@Override
	public String getReportId() {
		return mHook != null ? mHook.getReportId() : "null";
	}

	@Override
	protected void onConvToJson(List<E> ts, JSONBuilder jsonBuilder) {
		if (mHook != null) {
			mHook.onConvToJson(ts, jsonBuilder);
		}
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, List<E> data) {
		super.onAddWakeupAsrCmd(acsc, data);
		if (mHook != null) {
			mHook.onAddWakeupCmds(acsc, data);
		}
	}

	@Override
	protected boolean onCommandSelect(String type, String command) {
		if (mHook != null) {
			if (mHook.onCmdSelected(type, command)) {
				return true;
			}
		}
		return super.onCommandSelect(type, command);
	}

	@Override
	protected boolean onIndexSelect(List<Integer> indexs, String command) {
		if(mHook != null) {
			if (mHook.onIndexSelected(indexs, command)) {
				return true;
			}
		}
		return super.onIndexSelect(indexs, command);
	}

	@Override
	protected void onSelectIndex(E item, boolean isFromPage, int idx, String fromVoice) {
		boolean bSelected = true;
		if (mHook != null) {
			bSelected = mHook.onSelectItem(item);
		}
		if (bSelected) {
			String objJson = convItemToString(item);
			putReport(KEY_DETAIL, objJson);
			putReport(KEY_INDEX, idx + "");
			doReportSelectFinish(true, fromVoice != null ? SELECT_TYPE_VOICE : SELECT_TYPE_UNKNOW, fromVoice);
		}
	}
	
	@Override
	protected void onItemSelect(E item, boolean isFromPage, int idx, String fromVoice) {
	}

	@Override
	protected ResourcePage<List<E>, E> createPage(List<E> sources) {
		if (mHook != null) {
			return mHook.createPage(sources, getOption().getNumPageSize());
		}

		return new ResListPage<E>(sources) {

			@Override
			protected int numOfPageSize() {
				if (!is2_0Version()) {
					return mSourceRes.size();
				}
				return getOption().getNumPageSize();
			}
		};
	}
	
	@Override
	protected String convItemToString(E item) {
		if (mHook != null) {
			return mHook.convItemToJson(item);
		}
		return null;
	}
}