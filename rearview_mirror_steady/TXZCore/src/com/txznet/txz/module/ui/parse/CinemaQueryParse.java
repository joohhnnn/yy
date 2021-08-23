package com.txznet.txz.module.ui.parse;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.interfaces.AbsTextJsonParse;
import com.txznet.txz.ui.widget.mov.CinemaLayout;
import com.txznet.txz.ui.widget.mov.CinemaLayout.CinemaBean;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public class CinemaQueryParse extends AbsTextJsonParse {
	public static int DEFAULT_CINEMA_LIST_SIZE = 4;
	public static final String WAKEUP_TASK_ID = "TASK_CINEMA_QUERY_ID";
	public static final String ACCESS_SCECE = "movie";
	public static final String ACCESS_ACTION = "query";

	boolean mHasThirdImpl;
	static boolean mHasWakeup;
	public static boolean mIsSelecting;

	PageHelper mPageHelper;
	List<CinemaItem> mCines;

	public CinemaQueryParse() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {

			@Override
			public void onShow() {
			}

			@Override
			public void onDismiss() {
				clear();
				if (cLayout != null) {
					cLayout.clear();
					cLayout = null;
				}
			}              
		});
	}
	
	public void snapPage(boolean isNext) {
		if (!mIsSelecting) {
			return;
		}

		boolean bSucc = true;
		if (isNext) {
			bSucc = mPageHelper.nextPage();
		} else {
			bSucc = mPageHelper.prePage();
		}

		if (bSucc) {
			showList();
		}
	}

	@Override
	public boolean acceptText(boolean hasThirdImpl, String strData) {
		mHasThirdImpl = hasThirdImpl;

		JSONBuilder jBuilder = new JSONBuilder(strData);
		String scene = jBuilder.getVal("scene", String.class);
		String action = jBuilder.getVal("action", String.class);
		if (ACCESS_SCECE.equals(scene) && ACCESS_ACTION.equals(action)) {
			return true;
		}
		return false;
	}

	String keywords;

	@Override
	public int parseStrData(String value) {
		JSONBuilder jBuilder = new JSONBuilder(value);
		JSONArray jsonArray = jBuilder.getVal("movies", JSONArray.class);
		keywords = jBuilder.getVal("keyword", String.class);
		String tts = jBuilder.getVal("tts", String.class);

		if (jsonArray == null) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakText(NativeData.getResString("RS_VOICE_SPEAK_PARSE_ERROR"), null);
			MonitorUtil.monitorCumulant("parse.cinema.E.dataNull");
			return TextParseResult.ERROR_ERROR;
		}

		mCines = getCinesFromJSONArray(jsonArray);
		if (mCines == null || mCines.size() < 1) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakText(tts, null);
			return TextParseResult.ERROR_SUCCESS;
		}

		int sizePage = ConfigUtil.getCinemaItemCount();
		if (hasOutput()) {
			sizePage = mCines.size();
		}
		
		mPageHelper = new PageHelper();
		mPageHelper.reset(mCines.size(), sizePage);

		RecorderWin.show();
		mIsSelecting = true;
		showList();
		beginWakeup();

		if (InterruptTts.getInstance().isInterruptTTS()) {
			speakText(tts);
		}else {
			mSpeechTaskId = TtsManager.getInstance().speakVoice(tts, TtsManager.BEEP_VOICE_URL);
		}

		return TextParseResult.ERROR_SUCCESS;
	}
	
	/**
	 * 是否上下页由初始化程序控制
	 */
	private boolean hasOutput() {
		return mHasThirdImpl && !ChoiceManager.getInstance().is2_0VersionChoice();
	}

	private void beginWakeup() {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			Runnable removeBackground = null;
			Runnable2<Object, String> taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;
			@Override
			public boolean needAsrState() {
				if (InterruptTts.getInstance().isInterruptTTS()) {//如果是识别模式，就不需要开启beep音
					return false;
				}else {
					return true;
				}
			}

			@Override
			public String getTaskId() {
				return WAKEUP_TASK_ID;
			}

			public void onSpeechEnd() {
				mLastSpeechEndTime = SystemClock.elapsedRealtime();
				if(removeBackground != null){
					AppLogic.removeBackGroundCallback(removeBackground);
				}
			};
			
			@Override
			public void onCommandSelected(String type, String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				
				taskRunnable = new Runnable2<Object, String>(type,command) {
					
					@Override
					public void run() {
						
						JNIHelper.logd("do onCommandSelected");
						
						String type = (String) mP1;
						String command = mP2;
						isEnd = true;
						if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
							//唤醒结果执行时，如果还在录音，则取消掉
							if (AsrManager.getInstance().isBusy()) {
								AsrManager.getInstance().cancel();
							}
						}
						if ("CINEMA_QUERY$CANCEL".equals(type)) {
							clear();
							RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
						} else if ("CINEMA_QUERY$NEXTPAGE".equals(type)) {
							snapPager(true, mPageHelper.nextPage(), command);
						} else if ("CINEMA_QUERY$PREPAGE".equals(type)) {
							snapPager(false, mPageHelper.prePage(), command);
						} else if (type.startsWith("CINEMA_PAGE_INDEX_")) {
							int index = Integer.parseInt(type.substring("CINEMA_PAGE_INDEX_".length()));
							speakText(NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", command));
							mPageHelper.selectPage(index);
							showList();
						}
					}
				};
				removeBackground = new Runnable() {
	
					@Override
					public void run() {
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
					if (isWakeupResult()) {//是唤醒的结果
						isEnd = false;
						//判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						}else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);							
						}
					} else if (!isEnd) {//识别到的唤醒词并且唤醒没有执行完成
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				}else {
					taskRunnable.run();
				}
			}
		}.addCommand("CINEMA_QUERY$CANCEL", NativeData.getResStringArray("RS_SELECT_WAKEUP_CANCEL"));

		// 注册上下页唤醒词
		if (mPageHelper.getMaxPage() > 1 || hasOutput()) {
			acsc.addCommand("CINEMA_QUERY$NEXTPAGE", NativeData.getResStringArray("RS_SELECT_WAKEUP_NEXTPAGE"));
			acsc.addCommand("CINEMA_QUERY$PREPAGE", NativeData.getResStringArray("RS_SELECT_WAKEUP_PREPAGE"));
		}
		
		// 注册第几页唤醒词
		if (mPageHelper.getMaxPage() > 1) {
			int pageSize = mPageHelper.getMaxPage();
			for (int i = 1; i <= pageSize; i++) {
				String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
				acsc.addCommand("CINEMA_PAGE_INDEX_" + i, "第" + strIndex + "页");
			}
		}

		mHasWakeup = true;
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}

	private void snapPager(boolean isNext, boolean bSucc, String command) {
		if (hasOutput()) {
			WinManager.getInstance().snapPager(isNext);
			return;
		}
		
		if (bSucc) {
			showList();
		}

		String endSpk = "";
		String pager = command;
		if (command.contains("翻")) {
			pager = NativeData.getResString("RS_SELECTOR_SELECT_PAGE").replace("%CMD%", command);
		} else {
			pager = NativeData.getResPlaceholderString("RS_SELECTOR_SELECT", "%CMD%", command);
		}

		if (!bSucc) {
			String slot = "";
			if (isNext) {
				slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
			} else {
				slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
			}

			endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND").replace("%NUM%", slot);
		}

		speakText(bSucc ? pager : endSpk);
	}

	private void speakText(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
			@Override
			public boolean isNeedStartAsr() {
				return true;
			}
		});
	}

	private List<CinemaItem> getCinesFromJSONArray(JSONArray jsonArray) {
		List<CinemaItem> cineList = new ArrayList<CinemaItem>();
		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				CinemaItem cb = new CinemaItem();
				JSONObject jo = (JSONObject) jsonArray.get(i);
				if (jo.has("post")) {
					cb.postUrl = jo.optString("post");
				}
				if (jo.has("name")) {
					cb.title = jo.optString("name");
				}
				if (jo.has("score")) {
					cb.score = Double.parseDouble(jo.optString("score"));
				}

				cineList.add(cb);
			} catch (JSONException e) {
				JNIHelper.logw("CinemaQuery parseStrData error:" + e.toString());
			}
		}
		return cineList;
	}

	private void showList() {
		final int curPage = mPageHelper.getCurPage();
		final int pageSize = mPageHelper.getPageSize();
		final int sIndex = curPage * pageSize;
		sendCineList(sIndex, pageSize);
	}

	private void sendCineList(int sIndex, int count) {
		JNIHelper.logd("sendCinList sIndex:" + sIndex + ",count:" + count + ",cines size:" + mCines.size());
		if (mCines == null) {
			return;
		}

		final int c = mCines.size();
		if (sIndex >= c) {
			return;
		}

		JSONBuilder jBuilder = new JSONBuilder();
		jBuilder.put("type", 7);
		jBuilder.put("keywords", keywords);
		jBuilder.put("prefix", NativeData.getResString("RS_DISPLAY_CINEMA_TITLE"));
		jBuilder.put("curPage", mPageHelper.getCurPage());
		jBuilder.put("maxPage", mPageHelper.getMaxPage());

		final List<CinemaBean> tmpList = new ArrayList<CinemaBean>();
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < count; i++) {
			if (sIndex >= mCines.size()) {
				break;
			}

			CinemaItem cb = mCines.get(sIndex);
			tmpList.add(convertCinemaBean(cb));
			JSONObject obj = new JSONBuilder().put("name", cb.title).put("post", cb.postUrl).put("score", cb.score)
					.getJSONObject();
			jsonArray.put(obj);
			sIndex++;
		}
		jBuilder.put("cines", jsonArray);
		jBuilder.put("count", jsonArray.length());

		if (mHasThirdImpl) {
			// 第三方直接发送列表数据
			RecorderWin.sendSelectorList(jBuilder.toString());
			return;
		}
		
		if (WinManager.getInstance().isRecordWin2()) {
			// 2.0框架
			RecorderWin.sendSelectorList(jBuilder.toString());
			return;
		}

		AppLogic.runOnUiGround(new Runnable1<JSONBuilder>(jBuilder) {

			@Override
			public void run() {
				// 直接发送给本地界面
				WinRecord.getInstance()
						.addMsg(ChatMsgFactory.createContainMsg(mP1.toString(), createCinemaLayout(tmpList)));
			}
		}, 0);
	}

	private CinemaBean convertCinemaBean(CinemaItem item) {
		CinemaBean cb = new CinemaBean();
		cb.post = item.postUrl;
		cb.score = item.score;
		cb.title = item.title;
		return cb;
	}

	CinemaLayout cLayout;

	private View createCinemaLayout(List<CinemaBean> cbs) {
		if (cLayout == null) {
			cLayout = (CinemaLayout) View.inflate(GlobalContext.get(), R.layout.view_cinema_layout, null);
			cLayout.setVisibleCount(DEFAULT_CINEMA_LIST_SIZE);
		}
		cLayout.setCineList(cbs);
		cLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				KeyEventManagerUI1.getInstance().updateFocusViews(cLayout.getFocusViews(),GlobalContext.get().getResources().getDrawable(R.drawable.white_range_layout));
				cLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		return cLayout;
	}

	static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

	public static void clear() {
		if (mHasWakeup) {
			mHasWakeup = false;
			mIsSelecting = false;
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			WakeupManager.getInstance().recoverWakeupFromAsr(WAKEUP_TASK_ID);
		}
	}

	public static class CinemaItem {
		public String title;
		public String postUrl;
		public double score;
	}

	public class PageHelper {
		public int curPage;
		public int maxPage;
		public int pageSize;

		public boolean nextPage() {
			if (curPage < (maxPage - 1)) {
				curPage++;
				return true;
			}
			return false;
		}

		public boolean selectPage(int page) {
			if (page <= maxPage && page > 0) {
				curPage = page - 1;
				return true;
			}
			return false;
		}

		public boolean prePage() {
			if (curPage > 0) {
				curPage--;
				return true;
			}
			return false;
		}

		public int getCurPage() {
			return curPage;
		}

		public int getMaxPage() {
			return maxPage;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void reset(int totalCount, int pageSize) {
			this.curPage = 0;
			this.maxPage = 0;
			this.pageSize = pageSize;

			if (totalCount > pageSize) {
				maxPage = totalCount / pageSize;
				if (totalCount % pageSize != 0) {
					maxPage++;
				}
			}
		}
	}
}