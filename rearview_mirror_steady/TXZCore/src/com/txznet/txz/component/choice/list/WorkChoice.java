package com.txznet.txz.component.choice.list;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 上报，兼容和一些常用的处理
 * 
 * @param <T>
 * @param <E>
 */
public abstract class WorkChoice<T, E> extends AbsWorkChoice<T, E> {
	public static final String KEY_SCENE = "scene";
	public static final String KEY_CURPAGE = "curPage";
	public static final String KEY_MAXPAGE = "maxPage";
	public static final String KEY_SHOW_TIME = "showTime";
	public static final String KEY_TYPE = "type";
	public static final String KEY_SELECT_TYPE = "selectType";
	public static final String KEY_DETAIL = "detail";
	public static final String KEY_INDEX = "index";
	public static final String KEY_CMDS = "cmds";
	public static final String KEY_SELECT_TIME = "selectTime";
	public static final String KEY_KEYWORDS = "keywords";
	public static final String KEY_ACTION = "action";
	public static final String KEY_IS_SELECT = "isSelect";
	public static final String KEY_FROM_VOICE = "fromVoice";
	public static final String KEY_PROGRESS_DELAY = "progressDelay";
	public static final String KEY_HAS_PROGRESSED = "curr_progress";
	public static final String VALUE_VOICE = "voice";
	public static final String VALUE_TOUCH = "touch";
	public static final String KEY_REPORT_TIME = "reportTime";
	public static final String KEY_TTS_TIME = "ttsTime";
	
	public static boolean sExitBack = true;

	public WorkChoice(CompentOption<E> option) {
		super(option);
	}

	@Override
	public void showChoices(T data) {
		super.showChoices(data);

		if (mPage != null) {
			// 兼容老的识别上下页回调
			mPage.setInterceptPage(!is2_0Version());
		}

		// 记录显示的时间
		mBeginSelectTime = SystemClock.elapsedRealtime();
		putReport(KEY_SHOW_TIME, NativeData.getMilleServerTime().uint64Time + "");
	}

	@Override
	protected void onSpeakTtsBegin() {
		super.onSpeakTtsBegin();
		mTtsSuccess = false;
		mTimeBeginTts = SystemClock.elapsedRealtime();
	}

	@Override
	protected void onSpeakTtsEnd() {
		super.onSpeakTtsEnd();
		mTimeEndTts = SystemClock.elapsedRealtime();
	}
	
	@Override
	protected void onSpeakTtsSuccess() {
		mTtsSuccess = true;
		super.onSpeakTtsSuccess();
	}

	@Override
	protected boolean needSureCmd() {
		return getOption().getCanSure() != null ? getOption().getCanSure() : false;
	}

	@Override
	public void selectCancel(int selectType, String fromVoice) {
		doReportSelectFinish(false, selectType, fromVoice);
		LogUtil.logd("selectCancel:" + fromVoice + ",sExitBack:" + sExitBack);
		if (!sExitBack) {
			if (!TextUtils.isEmpty(fromVoice))
				TtsManager.getInstance().speakText(NativeData.getResString("RS_SELECTOR_OPERATION_CANCEL"));
			clearIsSelecting();
			RecorderWin.dismiss();
			return;
		}
		super.selectCancel(selectType, fromVoice);
	}

	@Override
	protected final JSONBuilder convToJson(T ts) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		if (mPage != null) {
			jsonBuilder.put("curPage", mPage.getCurrPage());
			jsonBuilder.put("maxPage", mPage.getMaxPage());
		} else {
			jsonBuilder.put("curPage", 0);
			jsonBuilder.put("maxPage", 0);
		}
		if (getOption().getNumPageSize() != null) {
			jsonBuilder.put("showcount", getOption().getNumPageSize());
		}
		onConvToJson(ts, jsonBuilder);
		return jsonBuilder;
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, T data) {
		if (!is2_0Version()) {
			acsc.addCommand("PRE_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_PRE"));
			acsc.addCommand("NEXT_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_NEXT"));
		}
		super.onAddWakeupAsrCmd(acsc, data);
	}

	@Override
	protected boolean is2_0Version() {
		return mCompentOption.getIs2_0Version() != null ? mCompentOption.getIs2_0Version() : true;
	}

	@Override
	protected void onSnapPager(boolean isNext, boolean bSucc, String command) {
		if (!is2_0Version()) {
			WinManager.getInstance().getAdapter().snapPager(isNext);
			return;
		}
		super.onSnapPager(isNext, bSucc, command);
	}

	protected abstract void onConvToJson(T ts, JSONBuilder jsonBuilder);

	@Override
	protected void onPreWakeupSelect(String command) {
		putReport(KEY_CMDS, command);
		super.onPreWakeupSelect(command);
	}
	
	@Override
	protected void onProgressEnd() {
		putReport(KEY_SELECT_TYPE, SELECT_TYPE_COUNT_DOWN + "");
		super.onProgressEnd();
	}
	
	@Override
	public void selectSure(String fromVoice) {
		if (fromVoice != null) {
			putReport(KEY_SELECT_TYPE, SELECT_TYPE_VOICE + "");
		}
		super.selectSure(fromVoice);
	}

	/**
	 * 默认选中后即代表已经做了选择，在TTS播报区间可以重新选择的重写该方法，真正选中后再通知上报数据
	 */
	@Override
	protected void onItemSelect(E item, boolean isFromPage, int idx, String fromVoice) {
		String objJson = convItemToString(item);
		putReport(KEY_DETAIL, objJson);
		putReport(KEY_INDEX, idx + "");
		doReportSelectFinish(true, fromVoice != null ? SELECT_TYPE_VOICE : SELECT_TYPE_UNKNOW, fromVoice);
	}

	protected abstract String convItemToString(E item);

	/**
	 * 播报语音后选中或者取消要调用该方法，以便上报数据
	 */
	@Override
	public void doReportSelectFinish(boolean bSelected, int selectType, String fromVoice) {
		final JSONBuilder jsonBuilder = getBaseReport();
		if (bSelected) {
			if (mPage != null) {
				putReport(KEY_CURPAGE, mPage.getCurrPage() + "");
				putReport(KEY_MAXPAGE, mPage.getMaxPage() + "");
			}
		}
		if (fromVoice != null) {
			putReport(KEY_FROM_VOICE, fromVoice);
			putReport(KEY_SCENE, VALUE_VOICE);
		} else {
			putReport(KEY_SCENE, VALUE_TOUCH);
		}
		if (selectType != SELECT_TYPE_UNKNOW) {
			putReport(KEY_SELECT_TYPE, selectType + "");
		}
		putReport(KEY_IS_SELECT, bSelected + "");
		putReport(KEY_REPORT_TIME, NativeData.getMilleServerTime().uint64Time + "");
		mLastReportParams = mReportBuilder;
		ReportUtil.doReport(new ReportUtil.Report.Builder(jsonBuilder.toString()).setSessionId().buildSelectReport());
	}

	protected JSONBuilder mReportBuilder;
	private JSONBuilder mLastReportParams;

	/**
	 * 获取上次上报的选项
	 * 
	 * @return
	 */
	public JSONBuilder getLastReport() {
		return mLastReportParams;
	}

	/**
	 * 添加上报字段，如果key一样，则放在数组中
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public JSONBuilder putReport(String key, String value) {
		if (mReportBuilder == null) {
			mReportBuilder = new JSONBuilder();
		}

		String val = mReportBuilder.getVal(key, String.class);
		if (val != null) {
			val += "," + value;
			mReportBuilder.put(key, val);
		} else {
			mReportBuilder.put(key, value);
		}

		return this.mReportBuilder;
	}

	private long mTimeEndTts;
	private long mTimeBeginTts;
	private boolean mTtsSuccess;
	private long mBeginSelectTime;

	protected JSONBuilder getBaseReport() {
		long t = SystemClock.elapsedRealtime();
		if (mReportBuilder == null) {
			mReportBuilder = new JSONBuilder();
		}

		Integer proDelay = getOption().getProgressDelay();
		if (proDelay != null && mProgressBeginTime > 0) {
			putReport(KEY_PROGRESS_DELAY, proDelay + "");
			putReport(KEY_HAS_PROGRESSED, (t - mProgressBeginTime) + "");
		}

		if (mTtsSuccess) {
			putReport(KEY_TTS_TIME, (mTimeEndTts - t) + "");
		} else {
			putReport(KEY_TTS_TIME, (t - mTimeBeginTts) + "");
		}

		putReport(KEY_SELECT_TIME, (t - mBeginSelectTime) + "");
		putReport(KEY_TYPE, getReportId());
		return mReportBuilder;
	}
	
	@Override
	protected void onClearSelecting() {
		mReportBuilder = null;
		mBeginSelectTime = mTimeBeginTts = mTimeEndTts = mBeginSelectTime = 0;
		super.onClearSelecting();
	}
}
