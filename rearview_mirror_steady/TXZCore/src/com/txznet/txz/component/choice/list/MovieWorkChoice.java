package com.txznet.txz.component.choice.list;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.R;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.widget.mov.CinemaLayout;
import com.txznet.txz.ui.widget.mov.CinemaLayout.CinemaBean;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import android.os.SystemClock;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

public class MovieWorkChoice extends WorkChoice<List<MovieWorkChoice.CinemaItem>, MovieWorkChoice.CinemaItem> {
	public static final String ACCESS_SCECE = "movie";
	public static final String ACCESS_ACTION = "query";
	
	// 当前关键字，需要在showChoice前调用
	public static String keywords;

	//判断是否外放电影资源给第三方替换,为空则未设置第三方
	private static String mRemoteService;
	private static int mOutTime = 0;

	public static Integer mUserMovieDisplayCount = null;

	//电影资源
	private String mJson;

	public static class CinemaItem {
		public String title;
		public String postUrl;
		public double score;
	}
	
	public MovieWorkChoice(CompentOption<CinemaItem> option) {
		super(option);
	}

	public static byte[] procRemoteResponse(String serviceName, String command,
											byte[] data) {

		if ("setTool".equals(command)) {
			setRemoteService(serviceName);
			int outTime = Integer.parseInt(new String(data));
			if(outTime > 0){
				mOutTime  = outTime;
				JNIHelper.logd("CinemaQuery outTime ="+mOutTime);
			}

			return null;
		}
		return null;
	}

	public static void setRemoteService(String serviceName) {
		mRemoteService = serviceName;
	}

	private  Runnable movieParseTextRunnable = new Runnable(){
		@Override
		public void run() {
			parseText();
		}
	};

	public boolean showMovieList(String json) {
		if (!acceptJson(json)) {
			return false;
		}
		mJson = json;
		final long oldTime = SystemClock.elapsedRealtime();
		if(!TextUtils.isEmpty(mRemoteService)){
			JSONBuilder jBuilder = new JSONBuilder(json);
			JSONObject newJSon = new JSONObject();
			try {
				newJSon.put("scene",jBuilder.getVal("scene",String.class));
				newJSon.put("action",jBuilder.getVal("action",String.class));
				newJSon.put("tts",jBuilder.getVal("tts",String.class));
				String spk;
				spk = NativeData.getResString("RS_DISPLAY_MOVIE_TITLE");
				if(!TextUtils.isEmpty(spk)){
					newJSon.put("tts",spk);
				}
				newJSon.put("keyword",jBuilder.getVal("keyword",String.class));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			ServiceManager.getInstance().sendInvoke(mRemoteService,"tool.movie.search", newJSon.toString().getBytes(), new ServiceManager.GetDataCallback(){
				@Override
				public void onGetInvokeResponse(ServiceManager.ServiceData data) {
					if(data == null){
						return;
					}
					try {
						JSONObject json = new JSONObject(data.getString());
						if(json.has("scene") && json.has("action") &&
								json.has("movies") && json.has("tts") &&
								json.has("keyword")){
							mJson = data.getString();
							long newTime = SystemClock.elapsedRealtime();
							JNIHelper.logd("CinemaQuery get new json time ="+(newTime - oldTime));
						}
					} catch (JSONException e) {
						JNIHelper.loge("CinemaQuery get new json error:" +e.toString());
					}
				}
			});
			AppLogic.runOnBackGround(movieParseTextRunnable,mOutTime);
		}else {
			parseText();
		}
		return true;
	}
	
	private boolean acceptJson(String json) {
		JSONBuilder jBuilder = new JSONBuilder(json);
		String scene = jBuilder.getVal("scene", String.class);
		String action = jBuilder.getVal("action", String.class);
		if (ACCESS_SCECE.equals(scene) && ACCESS_ACTION.equals(action)) {
			return true;
		}
		return false;
	}
	
	private void parseText() {
		if(TextUtils.isEmpty(mJson)){
			return;
		}
		JSONBuilder jBuilder = new JSONBuilder(mJson);;
		JSONArray jsonArray = jBuilder.getVal("movies", JSONArray.class);
		keywords = jBuilder.getVal("keyword", String.class);
		String tts = jBuilder.getVal("tts", String.class);
		String spk;
		spk = NativeData.getResString("RS_DISPLAY_MOVIE_TITLE");
		if(!TextUtils.isEmpty(spk)){
			tts = spk;
		}
		List<CinemaItem> items = getCinesFromJSONArray(jsonArray);
		if (items == null || items.size() < 1) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakText(tts, null);
			return;
		}

//		if (getOption().getTtsText() == null) {
			getOption().setTtsText(tts);
//		}
		if (mUserMovieDisplayCount == null) {
			if (getOption().getNumPageSize() == null) {
				getOption().setNumPageSize(ConfigUtil.getCinemaItemCount());
			}
		} else {
			getOption().setNumPageSize(mUserMovieDisplayCount);
		}

		ChoiceManager.getInstance().setPageSizeOption(getOption(), TXZConfigManager.PageType.PAGE_TYPE_MOVIE_LIST.name());


		if (!is2_0Version()) {
			getOption().setNumPageSize(items.size());
		}
		RecorderWin.showUserText();
		showChoices(items);
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
					try {
						cb.score = Double.parseDouble(jo.optString("score"));
					}catch (Exception e) {
						JNIHelper.logw("CinemaQuery parseStrData score:" + jo.optString("score"));
					}

				}

				cineList.add(cb);
			} catch (JSONException e) {
				JNIHelper.logw("CinemaQuery parseStrData error:" + e.toString());
			}
		}
		return cineList;
	}
	
	@Override
	public String getReportId() {
		return "Movie_Select";
	}

	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, List<CinemaItem> data) {
		acsc.addCommand("CANCEL", NativeData.getResStringArray("RS_CMD_SELECT_CANCEL"));
		if (mPage.getMaxPage() > 1 || !is2_0Version()) {
			acsc.addCommand("PRE_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_PRE"));
			acsc.addCommand("NEXT_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_NEXT"));
		}

		if (is2_0Version()) {
			if (mPage.getMaxPage() > 1) {
				int i = 1;
				for (; i <= mPage.getMaxPage(); i++) {
					String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
					acsc.addCommand("PAGE_INDEX_" + i, "第" + strIndex + "页");
				}
				acsc.addCommand("PAGE_INDEX_" + (i - 1), "最后一页");
			}
		}
	}

	@Override
	protected void updateDisplay(List<CinemaItem> items) {
		// 重写刷新界面
		if (WinManager.getInstance().hasThirdImpl() || WinManager.getInstance().isRecordWin2()) {
			super.updateDisplay(items);
			return;
		}

		if (mPage == null) {
			return;
		}
		String strData = convToJson(mPage.getResource()).toString();
		LogUtil.logd("send data:" + strData);
		AppLogic.runOnUiGround(new Runnable1<String>(strData) {

			@Override
			public void run() {
				WinRecord.getInstance().addMsg(
						ChatMsgFactory.createContainMsg(mP1, createCinemaLayout(convCinemaBean(mPage.getResource()))));
			}
		});
	}

	private CinemaBean convertCinemaBean(CinemaItem item) {
		CinemaBean cb = new CinemaBean();
		cb.post = item.postUrl;
		cb.score = item.score;
		cb.title = item.title;
		return cb;
	}

	private CinemaLayout cLayout;

	private View createCinemaLayout(List<CinemaBean> cbs) {
		if (cLayout == null) {
			cLayout = (CinemaLayout) View.inflate(GlobalContext.get(), R.layout.view_cinema_layout, null);
			cLayout.setVisibleCount(getOption().getNumPageSize());
		}
		cLayout.setCineList(cbs);
		cLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				if (cLayout == null) {
					return;
				}
				if (cLayout.getFocusViews() != null && cLayout.getFocusViews().size() > 0) {
					KeyEventManagerUI1.getInstance().updateFocusViews(cLayout.getFocusViews(),
							GlobalContext.get().getResources().getDrawable(R.drawable.white_range_layout));
				}
				cLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
		cLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		return cLayout;
	}

	@Override
	protected void onConvToJson(List<CinemaItem> ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", 7);
		jsonBuilder.put("keywords", keywords);
		jsonBuilder.put("prefix", NativeData.getResString("RS_DISPLAY_CINEMA_TITLE"));

		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < ts.size(); i++) {
			CinemaItem cb = ts.get(i);
			JSONObject obj = new JSONBuilder().put("name", cb.title).put("post", cb.postUrl).put("score", cb.score)
					.getJSONObject();
			jsonArray.put(obj);
		}
		jsonBuilder.put("cines", jsonArray);
		jsonBuilder.put("count", jsonArray.length());
		jsonBuilder.put("vTips",getTips());
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getMaxPage() == 1) {
					tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_LAST");
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE");
			}
		}
		return tips;
	}

	private List<CinemaBean> convCinemaBean(List<CinemaItem> items) {
		List<CinemaBean> beans = new ArrayList<CinemaLayout.CinemaBean>();
		for (int i = 0; i < items.size(); i++) {
			CinemaItem cb = items.get(i);
			beans.add(convertCinemaBean(cb));
		}
		return beans;
	}

	@Override
	protected void onSelectIndex(CinemaItem item, boolean isFromPage, int idx, String fromVoice) {
	}

	@Override
	protected void onClearSelecting() {
		if(!TextUtils.isEmpty(mRemoteService) && !TextUtils.isEmpty(mJson)){
			ServiceManager.getInstance().sendInvoke(mRemoteService,"tool.movie.onCancel",null,null);
			mJson = null;
		}

		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (cLayout != null) {
					cLayout.clear();
					cLayout = null;
				}
			}
		});
	}

	@Override
	protected ResourcePage<List<CinemaItem>, CinemaItem> createPage(List<CinemaItem> sources) {
		return new ResListPage<MovieWorkChoice.CinemaItem>(sources) {

			@Override
			protected int numOfPageSize() {
				return getOption().getNumPageSize();
			}
		};
	}
	
	@Override
	protected String convItemToString(CinemaItem item) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("postUrl", item.postUrl);
		jsonBuilder.put("score", item.score);
		jsonBuilder.put("title", item.title);
		return jsonBuilder.toString();
	}
}