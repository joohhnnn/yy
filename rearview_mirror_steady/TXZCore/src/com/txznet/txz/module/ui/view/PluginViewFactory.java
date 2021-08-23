package com.txznet.txz.module.ui.view;

import java.util.HashMap;
import java.util.Map;

import com.txznet.txz.module.ui.view.plugin.sample.TTSNoResultView;

public class PluginViewFactory {
	public static final int TYPE_TTS_NO_RESULT_VIEW = 1;
	public static final int TYPE_TTS_SWITCH_VIEW = 2;

	static Map<Integer, BPView> mReqViewMap = new HashMap<Integer, BPView>();

	public static BPView genPluginViewByJson(int reqId, String strData) {
		BPView view = mReqViewMap.get(reqId);
		if (view != null) {
			view.refreshView(view, strData);
			return view;
		}

		BPView bpView = null;
		if (reqId == TYPE_TTS_NO_RESULT_VIEW) {
			bpView = new TTSNoResultView(strData);
		}

		if (bpView != null) {
			mReqViewMap.put(reqId, bpView);
		}

		return bpView;
	}
}