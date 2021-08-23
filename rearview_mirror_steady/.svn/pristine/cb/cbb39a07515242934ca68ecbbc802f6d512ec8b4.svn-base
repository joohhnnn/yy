package com.txznet.txz.component.choice.list;

import android.text.TextUtils;
import android.view.animation.Animation;

import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.poi.PoiComparator_Distance;
import com.txznet.record.poi.PoiComparator_Price;
import com.txznet.record.poi.PoiComparator_Score;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.sdk.bean.PoiDeepInfo;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResPoiPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupCmdTask;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.KeywordsParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PoiWorkChoice extends WorkChoice<PoiWorkChoice.PoisData, Poi> {
	public static final String TASK_WAKE_ID = "Poi_Select";
	
	public static class PoisData {
		public String city;
		public String keywords = "";
		public String action;
		public boolean isBus;
		public List<Poi> mPois;
		public String tips;
	}
	
	private boolean mHasPrices;
	private boolean mHasScore;
	private boolean mNeedSort = true;
	private boolean mExistSameCity;
	
	private PoisData mCurrPoisData;
	private boolean mIsBusiness;
	private Map<String,String> mMapKeyWord = new HashMap<String, String>();

	@Override
	protected void selectIndex(boolean isPage, int idx, String fromVoice) {
		super.selectIndex(isPage, idx, fromVoice);
	}

	public PoiWorkChoice(CompentOption<Poi> option) {
		super(option);
	}
	
	@Override
	public void showChoices(PoisData data) {
		mCurrPoisData = data;
		if (data == null) {
			return;
		}

		if (data.mPois == null || data.mPois.isEmpty()) {
			NavManager.getInstance().mSearchBySelect = false;
			noResultHandle(data);
			return;
		}
		
		preParse(data.mPois);
//取消自动选择
		boolean isProgress = canAutoProgress(data);
//		Integer delay = getOption().getProgressDelay();
//		if (isProgress) {
//			if (delay == null || delay < 1) {
//				getOption().setProgressDelay(TOTAL_AUTO_CALL_TIME);
//			}
//		} else {
//			getOption().setProgressDelay(null);
//		}
//
		if (mCompentOption.getCanSure() == null) {
			mCompentOption.setCanSure(isProgress);
		}
		if(data.mPois.size() == 1 && mData != null && PoiAction.ACTION_DEL_JINGYOU.equals(data.action)){
			mData = data;
			TtsManager.getInstance().speakText(NativeData.getResString("RS_POI_ALREADY_JINGYOU_DEL_SPK"));
			navigateTo(data.mPois.get(0), 0, SELECT_TYPE_UNKNOW, null, data.action);
			return;
		}
		if (mCompentOption.getTtsText() == null) {
				mCompentOption.setTtsText(getSpeakTtsText(data));
		}
		super.showChoices(data);

		NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_SHOW,
				new JSONBuilder().put("action", data.action).put("size", data.mPois.size())
						.toString());
	}
	
	private void noResultHandle(PoisData data) {
		String tts = getNoResultTts(data.keywords);
		RecorderWin.show();
		if (WinManager.getInstance().isRecordWin2() && ChoiceManager.getInstance().isRecordWin2JustNoResultText()) {
			RecorderWin.addSystemMsg(tts);
		} else {
			JSONBuilder jsonBuilder = convToJson(data);
			jsonBuilder.put("tts", tts);
			RecorderWin.sendSelectorList(jsonBuilder.toString());
		}
		mSpeechTaskId = TtsManager.getInstance().speakText(tts, PreemptType.PREEMPT_TYPE_NEXT,
				new ITtsCallback() {
					@Override
					public void onBegin() {
						super.onBegin();
						RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
					}

					@Override
					public void onSuccess() {
						RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
						int grammarId = AsrManager.getInstance().getLastGrammarId();
						if (grammarId == VoiceData.GRAMMAR_SENCE_SET_HOME
								|| grammarId == VoiceData.GRAMMAR_SENCE_SET_COMPANY) {
							AsrManager.getInstance().start(new AsrOption().setGrammar(grammarId));
						} else {
							AsrManager.getInstance().start();
						}
						super.onSuccess();
					}
				});
	}
	
	public static String getNoResultTts(String keywords) {
		String txt = "";
		if (WinManager.getInstance().hasThirdImpl()) {
			txt = replaceStr("RS_POI_SELECT_NO_RESULT_THIRD", "%KEYWORDS%", keywords);
		} else {
			txt = replaceStr("RS_POI_SELECT_NO_RESULT", "%KEYWORDS%", keywords);
		}
		return txt;
	}
	
	private boolean canAutoProgress(PoisData poisData) {
		if (poisData.mPois != null && poisData.mPois.size() == 1) {
			return true;
		}
		return false;
	}
	
	private String getSpeakTtsText(PoisData data) {
		List<Poi> pois = data.mPois;
		String action = data.action;
		if (action == null) {
			action = PoiAction.ACTION_NAVI;
		}
		String city = data.city;
		if (city == null) {
			city = "";
		}
		String keywords = data.keywords;
		if (keywords == null) {
			keywords = "";
		}
		String resHint = "";
		int count = 0;
		do {
			if (pois == null || pois.size() == 0) {
				if (WinManager.getInstance().hasThirdImpl()) {
					resHint = replaceStr("RS_POI_SELECT_NO_RESULT_THIRD", "%KEYWORDS%", keywords);
				} else {
					resHint = replaceStr("RS_POI_SELECT_NO_RESULT", "%KEYWORDS%", keywords);
				}
				break;
			}

			count = pois.size();
			if (PoiAction.ACTION_HOME.equals(action) || PoiAction.ACTION_COMPANY.equals(action)) {
				String type = "";
				if (PoiAction.ACTION_HOME.equals(action)) {
					type = "家";
				} else if (PoiAction.ACTION_COMPANY.equals(action)) {
					type = "公司";
				}

				if (count == 1) {
					resHint = replaceStr("RS_POI_SELECT_SET_SINGLE_SPK", "%TYPE%", type);
					break;
				} else {
					resHint = replaceStr("RS_POI_SELECT_SET_LIST_SPK", "%CITY%", city);
					try {
						resHint = resHint.replace("%POINAME%", keywords);
						resHint = resHint.replace("%COUNT%", count + "");
						resHint = resHint.replace("%TYPE%", type);
					} catch (Exception e) {
					}
					break;
				}
			}

			if (PoiAction.ACTION_JINGYOU.equals(action)) {
				if (count == 1) {
					resHint = replaceStr("RS_POI_SELECT_JINGYOU_SINGLE_SPK", "%POINAME%", keywords);
				}else {
					String newKeyword = getConverKeyWord(keywords);
					if (newKeyword != null) {
						keywords = newKeyword;
						resHint = replaceStr("RS_POI_SELECT_JINGYOU_BUSSINESS_LIST_SPK","","");
					}else{
						resHint = replaceStr("RS_POI_SELECT_JINGYOU_SPK","%POINAME%",keywords);
					}
					resHint = resHint.replace("%COUNT%",String.valueOf(count));
				}
				break;
			}
			if (PoiAction.ACTION_DEL_JINGYOU.equals(action)) {
				if (count == 1) {
					resHint = replaceStr("RS_POI_SELECT_JINGYOU_SINGLE_DEL_SPK", "", "");
				}else {
					resHint = replaceStr("RS_POI_SELECT_JINGYOU_DEL_SPK","%COUNT%",String.valueOf(count));
				}
				break;
			}

			if(PoiAction.ACTION_THIRD_POI.equals(action)){
				resHint = "";
				break;
			}

			// 默认是导航场景
			if (count == 1) {
				String newKeyWord = getConverKeyWord(keywords);
				if (newKeyWord != null) {
					keywords = newKeyWord;
					resHint = replaceStr("RS_POI_SELECT_NAV_BUSSINESS_SINGLE_SPK", "", "");
					resHint = resHint.replace("%POINAME%", keywords);
				}else{
					resHint = replaceStr("RS_POI_SELECT_NAV_SINGLE_SPK", "", "");
				}
				break;
			}

			String slotCity = city;
			if (mExistSameCity) {
				slotCity = "";
			}

			//将“美食”的搜索结果替换为“餐厅”
//			if(keywords.equals("美食")){
//				keywords = "餐厅";
//			}

			// TODO 去掉城市的播报
			slotCity = "";
			String newKeyword = getConverKeyWord(keywords);
			if (newKeyword != null) {
				keywords = newKeyword;
				resHint = replaceStr("RS_POI_SELECT_NAV_BUSSINESS_LIST_SPK", "%CITY%", slotCity);
			}else {
				resHint = replaceStr("RS_POI_SELECT_NAV_LIST_SPK", "", "");
			}
		} while (false);
		
		String countStr = NativeData.getResString("RS_VOICE_DIGITS", count);
		if ("二".equals(countStr)) {
			countStr = "两";
		}
		try {
			resHint = resHint.replace("%POINAME%", keywords);
			resHint = resHint.replace("%COUNT%", countStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resHint;
	}
	
	private static String replaceStr(String resId, String srcStr, String desStr) {
		String resStr = NativeData.getResString(resId);
		if (resStr == null) {
			return "";
		}

		try {
			return resStr.replace(srcStr, desStr);
		} catch (Exception e) {
			return resStr;
		}
	}
	
	@Override
	protected int getSenceGrammar() {
		if (PoiAction.ACTION_RECOMM_COMPANY.equals(mData.action)
				|| PoiAction.ACTION_COMPANY.equals(mData.action)) {
			return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_COMPANY;
		}
		if (PoiAction.ACTION_RECOMM_HOME.equals(mData.action)
				|| PoiAction.ACTION_HOME.equals(mData.action)) {
			return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_HOME;
		}
		if (PoiAction.ACTION_NAV_RECOMMAND.equals(mData.action)) {
			return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_NAVIGATE;
		}
		if (PoiAction.ACTION_JINGYOU.equals(mData.action)) {
			return VoiceData.GRAMMAR_SENCE_SET_JINGYOU;
		}

		return super.getSenceGrammar();
	}
	
	@Override
	public boolean isCoexistAsrAndWakeup() {
		//合并唤醒和识别进程后,不支持唤醒和识别一起开启
		if (ProjectCfg.getMemMode() == ProjectCfg.MEM_MODE_PREBUILD_MERGE){
			return false;
		}
		
		if (mData == null) {
			return super.isCoexistAsrAndWakeup();
		}

		if (!ChoiceManager.getInstance().isPoiUseDefaultCoexistAsrAndWakeup() &&
				(PoiAction.ACTION_RECOMM_COMPANY.equals(mData.action)
						|| PoiAction.ACTION_RECOMM_HOME.equals(mData.action)
						|| PoiAction.ACTION_NAV_RECOMMAND.equals(mData.action))) {
			return true;
		}
		
		return super.isCoexistAsrAndWakeup();
	}

	@Override
	protected boolean isDelayAddWkWords() {
		return WinManager.getInstance().isDelayAddWkWords();
	}
	
	
	boolean  isNeadAddAddresssTts = true;
	@Override
	protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, PoisData poisData) {
		super.onAddWakeupAsrCmd(acsc, poisData);
		final PoisData mPoisData = mData;
		NavAmapValueService.getInstance().destroyPlanningSelect();
		aWakeUpWord.clear();
		if (mPoisData.mPois.size() > 1 && mNeedSort) {
			addCommand(acsc,"SORT_DISTANCE", "距离排序", "远近排序","按距离排个序","按远近排个序");
			addWakeUpTts("距离排序",false);
			if (mHasScore) {
				addCommand(acsc,"SORT_SCORE", "分数排序", "评分排序", "评价排序", "好评排序", "按分数排个序", "按评分排个序", "按评价排个序", "按好评排个序");
			}
			if (mHasPrices){
				addCommand(acsc,"SORT_PRICE", "价钱排序", "价格排序", "消费排序","按价钱排个序", "按价格排个序", "按消费排个序");
				addWakeUpTts("价格排序",false);
			}
			if( !mHasPrices && mHasScore){
				addWakeUpTts("评分排序",false);
			}
		}

		if (mPoisData.mPois.size() == 1) {
			if (PoiAction.ACTION_NAV_RECOMMAND.equals(mPoisData.action)) {
				addCommand(acsc, "TYPE_START_NAV", "开始导航");
			}
		}
		addMapCommand(acsc);
		int minDistance = -1;
		int minDistanceIndex = -1;
		boolean isAddAddress = isNeadAddAddresssTts;
		// 根据全部数据来插index
		for (int i = 0; i < mPoisData.mPois.size(); i++) {
			Poi poi = mPoisData.mPois.get(i);
			addBusinessCmds(acsc, poi, i);

			int d = poi.getDistance();
			if (minDistance < 0 || d < minDistance) {
				minDistanceIndex = i;
				minDistance = d;
			}
			if (isCoexistAsrAndWakeup()) {
				continue;
			}

			for (String kw : KeywordsParser.splitKeywords(poi.getName())) {
				if (TextUtils.isEmpty(kw)) {
					continue;
				}
				//将长度小于2的字符串过滤掉，因为在addIndex中并未将其插入到唤醒词中
				if (kw.length() < 2) {
					continue;
				}
				if (mPoisData.keywords != null &&mPoisData.keywords.equals(kw))
					continue;
				if (mPoisData.keywords != null && kw.startsWith(mPoisData.keywords)) {
					addIndex(acsc, i, kw.substring(mPoisData.keywords.length()));
				} else if (mPoisData.keywords != null && kw.endsWith(mPoisData.keywords)) {
					addIndex(acsc, i, kw.substring(0, kw.length() - mPoisData.keywords.length()));
				} else {
					addIndex(acsc, i, kw);
				}
				if( isAddAddress){
					isAddAddress = false;
					mAddressWord = kw;
					addWakeUpTts(kw, false);
				}
			}
			for (String kw : KeywordsParser.splitAddressKeywords(poi.getGeoinfo())) {
				if (mPoisData.keywords != null && kw.startsWith(mPoisData.keywords)) {
					kw = kw.substring(mPoisData.keywords.length());
				}
				if (mPoisData.keywords != null && kw.endsWith(mPoisData.keywords)) {
					kw = kw.substring(0, kw.length() - mPoisData.keywords.length());
				}
				if (kw.isEmpty())
					continue;
				addIndex(acsc, i, kw);
				if(  isAddAddress){
					isAddAddress = false;
					mAddressWord = kw;
					addWakeUpTts(kw, false);
				}
			}
		}
		if (minDistanceIndex >= 0) {
			addIndex(acsc,minDistanceIndex, "最近那个");
			addWakeUpTts("最近那个",false);
		}
		if(mPoisData.mPois.size() == 1){
			aWakeUpWord.clear();
			addWakeUpTts("请说确定还是取消",true);
		}else if(NavManager.getInstance().isSupportMapModle()){
			if(NavManager.getInstance().getPoiShowIsList()){
				addWakeUpTts("地图模式",false);
			}else{
				addWakeUpTts("列表模式",false);
			}				
		}

		if (mPage!=null) {
			if (mPage.getMaxPage() > 1) {
				if (mPage.getCurrPage() == 0) {
					String nextPage = NativeData.getResString("RS_CMD_SELECT_NEXT");
					if (!TextUtils.isEmpty(nextPage)) {
						addWakeUpTts(nextPage, false);
					}
				} else {
					String prePage = NativeData.getResString("RS_CMD_SELECT_PRE");
					if (!TextUtils.isEmpty(prePage)) {
						addWakeUpTts(prePage, false);
					}
				}
			}
			final int currPageSize = mPage.getCurrPageSize();

			String strIndex = NativeData.getResString("RS_VOICE_DIGITS", currPageSize);
			acsc.addCommand("ITEM_INDEX_" + (currPageSize - 1), "第" + strIndex + "个", "第" + strIndex + "条","最后一个","最后一条");

		}

		String action = mPoisData.action;
		LogUtil.d("onAddWakeupAsrCmd: " + action);
		if ((PoiAction.ACTION_NAVI.equals(action) || PoiAction.ACTION_JINGYOU.equals(action))&& ProjectCfg.isSupportCorrectSpeech()) {
			addCommand(acsc, TYPE_WAKEUP_CORRECT_SPEECH, NativeData.getResStringArray("RS_NAV_CMD_CORRECT_SPEECH"));
		}
	}

	public static final String TYPE_WAKEUP_CORRECT_SPEECH = "CORRECT_SPEECH";
	
	private String mTipCityStr = null;
	@Override
	protected void analyzeOption(CompentOption<Poi> option) {
		if(!mNeedPlay){
			mNeedPlay = true;
			return;
		}
		mTipCityStr = NavManager.getInstance().getTipCityString();
		JNIHelper.logd("analyzeOption mSearchBySelect = "+NavManager.getInstance().mSearchBySelect
				+" mCurrPoisData.city = "+mCurrPoisData.city+
				"  showCity "+mTipCityStr);
		if(NavManager.getInstance().mSearchBySelect){
			if(!mTipCityStr.equals("LOADING")){
				NavManager.getInstance().mSearchBySelect  = false;
				if(mTipCityStr.equals("附近") || mTipCityStr.equals("多个城市") || mCurrPoisData.city.equals(mTipCityStr)){
					mCompentOption.setTtsText(getSpeakTtsText(mCurrPoisData));
				}else{
					mCurrPoisData.city = mTipCityStr;
					mCompentOption.setTtsText("未找到结果，已为您显示"+mTipCityStr+"的结果");
				}
			}else{
				return;
			}
		}
		

		super.analyzeOption(option);
	}
	
	private void addBusinessCmds(AsrComplexSelectCallback acsc, Poi poi, int i) {
		if (poi instanceof BusinessPoiDetail) {
			BusinessPoiDetail businessPoi = (BusinessPoiDetail) poi;
			if (!TextUtils.isEmpty(businessPoi.getBranchName())) {
				// 分店
				addIndex(acsc, i, businessPoi.getBranchName());
			}
			// 分类
			String[] categories = businessPoi.getCategories();
			if (categories != null) {
				for (String c : categories) {
					if (c.endsWith("菜馆")) {
						addIndex(acsc, i, c.substring(0, c.length() - 1));
					} else {
						addIndex(acsc, i, c);
					}
				}
			}
			// 区域
			String[] regions = businessPoi.getRegions();
			if (regions != null) {
				for (String c : regions) {
					addIndex(acsc, i, c);
				}
			}
			// 团购优惠其他
			if (businessPoi.isHasDeal()){
				addIndex(acsc,i, "有团购","找出有团购");
				addWakeUpTts("有团购的",false);				
			}
			if (businessPoi.isHasCoupon()){
				addIndex(acsc,i, "有优惠","找出有优惠");
				addWakeUpTts("有优惠的",false);
			}			
			if (businessPoi.isHasPark()){
				addIndex(acsc,i, "有停车场","找出有停车场");
				addWakeUpTts("有停车场的",false);				
			}
			if (businessPoi.isHasWifi()){
				addIndex(acsc,i, "有WIFI","找出有WIFI");
				addWakeUpTts("有WIFI的",false);
			}				
			if (!TextUtils.isEmpty(businessPoi.getTelephone())){
				addIndex(acsc,i, "有电话","找出有电话");				
			}

		}
		String extra = poi.getExtraStr();
		if (!TextUtils.isEmpty(extra)) {
			PoiDeepInfo deepInfo = PoiDeepInfo.parseFromString(extra);
			if (deepInfo == null || deepInfo.feature == null)
				return;
			for (String cmd : deepInfo.feature) {
				if (!TextUtils.isEmpty(cmd)){
					addIndex(acsc,i, cmd);
					addWakeUpTts(cmd,false);							
					JNIHelper.logd("POISearchLog: add cmd = " + cmd);
				}				
			}

		}
	}

	private List<String> aWakeUpWord = new ArrayList<String>();
	private List<String> mHadPlayWord = new ArrayList<String>();
	private String mAddressWord = null;
	private void addWakeUpTts(String str , boolean isOnly){
		if(!NavManager.getInstance().getPoiPlayTipTts()){
			return;
		}
		if(mHadPlayWord.contains(str) || aWakeUpWord.contains(str)){
			return ;
		}else{
			//V3预编译优化内存模式下,非预编译的唤醒词不应该提示
			if (ProjectCfg.wakeupPrebuiltGrammar() 
					&& !WakeupCmdTask.contains(str, WakeupCmdTask.STATIC_POI_SELECT_CMDS)){
				return;
			}
			aWakeUpWord.add(str);			
		}
	}
	private Runnable mWakeupWordTip = new Runnable() {
		
		@Override
		public void run() {
			if(aWakeUpWord.size() == 0 ||
					mCurrPoisData.action.equals(PoiAction.ACTION_NAV_HISTORY) ||
					mCurrPoisData.action.equals(PoiAction.ACTION_NAV_RECOMMAND) ||
					mCurrPoisData.action.equals(PoiAction.ACTION_RECOMM_HOME) ||
					mCurrPoisData.action.equals(PoiAction.ACTION_RECOMM_COMPANY) ){
				return ;
			}
			Random random = new Random();
			int index = 0;
			if(aWakeUpWord.size() <2 ){
				index = 0;
			}else{
				index = random.nextInt(aWakeUpWord.size());				
			}
			String tip =aWakeUpWord.get(index);
			if(tip.equals("地图模式") || tip.equals("列表模式")){
				boolean poiShowIsList = NavManager.getInstance().getPoiShowIsList();
				if( (poiShowIsList && tip.equals("列表模式")) ||
						(!poiShowIsList && tip.equals("地图模式"))){
					aWakeUpWord.remove(index);
					AppLogic.runOnBackGround(mWakeupWordTip);
					return ;
				}
			}else if(!TextUtils.isEmpty(mAddressWord) && tip.equals(mAddressWord)){
				isNeadAddAddresssTts = false;
			}
			aWakeUpWord.remove(index);
			if( !tip.equals("请说确定还是取消") ){
				tip = "你还可以说"+tip;
			}
			mHadPlayWord.add(tip);
			speakTips(tip);
		}
	};
	
	private void speakTips(String text) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.getInstance().speakVoice(text,
				InterruptTts.getInstance().isInterruptTTS() ? "" : TtsManager.BEEP_VOICE_URL,
				new TtsUtil.ITtsCallback() {
					@Override
					public void onSuccess() {
						RecorderWin.refreshState(RecorderWin.STATE_RECORD_START);
						AsrManager.getInstance().mSenceRepeateCount = 0;

						JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
						if (isCoexistAsrAndWakeup() && !InterruptTts.getInstance().isInterruptTTS()) {
							AsrManager.getInstance().mSenceRepeateCount++;
							if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
								AsrManager.getInstance().start(createSelectAgainAsrOption());
							}
						}
						
						if(aWakeUpWord.size() > 0){
							AppLogic.runOnBackGround(mWakeupWordTip,10000);
						}
					}

					@Override
					public void onBegin() {
						super.onBegin();
						RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
						NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_TTS_START, null);
					}

					@Override
					public void onEnd() {
						super.onEnd();
						NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_TTS_END, null);
					}

					@Override
					public boolean isNeedStartAsr() {
						return true;
					}
				});
	}

	@Override
	protected void onSpeakTtsSuccess() {
		super.onSpeakTtsSuccess();
		isNeadAddAddresssTts = true;
		AppLogic.runOnBackGround(mWakeupWordTip, 10000);
	}

	@Override
	protected void onClearSelecting() {
		super.onClearSelecting();
		NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_CANCEL,null);
		NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_DISMISS, null);
		cancleContinuityTip();
		
	}
	
	private void cancleContinuityTip(){
		JNIHelper.logd("cancleContinuityTip ");
		aWakeUpWord.clear();
		mHadPlayWord.clear();
		isNeadAddAddresssTts = true;
		AppLogic.removeBackGroundCallback(mWakeupWordTip);
		
	}

	private void pauseContinuityTip() {
		LogUtil.logd("pauseContinuityTip");
		isNeadAddAddresssTts = true;
		AppLogic.removeBackGroundCallback(mWakeupWordTip);
	}

	public void resumeContinuityTip() {
		LogUtil.logd("resumeContinuityTip");
		AppLogic.runOnBackGround(mWakeupWordTip, 10000);
	}

	public static void addMapCommand(AsrComplexSelectCallback acsc){	
		if(NavManager.getInstance().isSupportMapModle()){
			if(NavManager.getInstance().getPoiShowIsList()){
				acsc.addCommand("MAP_MODLE","地图模式");
			}else{
			    acsc.addCommand("ZOOMIN_MODLE","放大地图");
                acsc.addCommand("ZOOMOUT_MODLE","缩小地图");
				acsc.addCommand("LIST_MODLE","列表模式");
			}			
		}
	}
	
	public static Integer onMapCommandSelect(String type, String command){
		if ("ZOOMIN_MODLE".equals(type)) {
			return PoiMsg.MAP_ACTION_ENLARGE;
		}
		if ("ZOOMOUT_MODLE".equals(type)) {
			return PoiMsg.MAP_ACTION_NARROW;
		}
		if ("MAP_MODLE".equals(type)) {
			NavManager.getInstance().setPoiShowIsList(false);
			RecordWin2Manager.mapActionResult(PoiMsg.MAP_ACTION_MAP, true);
			return PoiMsg.MAP_ACTION_MAP;
		}
		if ("LIST_MODLE".equals(type)) {
			NavManager.getInstance().setPoiShowIsList(true);
			RecordWin2Manager.mapActionResult(PoiMsg.MAP_ACTION_LIST, true);
			return PoiMsg.MAP_ACTION_LIST;
		}
		return null;
	}
	
	public static final String KEY_POI_DISPLAY_MODE = "poiDisplayMode";
	
	@Override
	public void doReportSelectFinish(boolean bSelected, int selectType, String fromVoice) {
		String modeKw = "list";
		boolean isList = NavManager.getInstance().getPoiShowIsList();
		if (!isList) {
			modeKw = "map";
		}
		putReport(KEY_POI_DISPLAY_MODE, modeKw);
		super.doReportSelectFinish(bSelected, selectType, fromVoice);
	}
	
	private void addCommand(AsrComplexSelectCallback acsc,String type, String... cmds){
		acsc.addCommand(type,cmds);
	}
	
	private void addIndex(AsrComplexSelectCallback acsc, int index, String... cmds) {
		if (cmds != null) {
			for (String keywords : cmds) {
				if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
					acsc.addIndex(index, keywords);
				}
			}
		}
	}
	
	private void preParse(List<Poi> items) {
		mHasPrices = false;
		mHasScore = false;
		mExistSameCity = false;
		mNeedSort = true;
		if (items == null || items.size() < 1)
			return;

		List<String> citys = new ArrayList<String>();
		for (int i = 0; i < items.size(); i++) {
			Poi poi = (Poi) items.get(i);
			if (citys.contains(poi.getCity())) {
				mExistSameCity = true;
			}
			if(mNeedSort == true && isEvCard(poi)){//EVCARD网点信息POI列表不支持排序
				mNeedSort = false;
			}
			if (poi instanceof BusinessPoiDetail) {
				BusinessPoiDetail bpd = (BusinessPoiDetail) items.get(i);
				if (bpd.getAvgPrice() > 0) {
					JNIHelper.logd("has prices");

					mHasPrices = true;
					// 有评分退出
					if (mHasScore && mExistSameCity) {
						break;
					}
				}

				if (bpd.getScore() > 0) {
					JNIHelper.logd("has scores");

					mHasScore = true;
					// 有价格退出
					if (mHasPrices && mExistSameCity) {
						break;
					}
				}
			}
		}
	}

	/**
	 * 判断是否是EVCard网点的poi
	 * @return
	 * @param info
	 */
	private boolean isEvCard(Poi info) {
		if(info == null || info.getExtraStr() == null){
			return false;
		}
		try {
			JSONObject json = new JSONObject(info.getExtraStr());
			if(json.has("shop_seq")){
				return true;
			}
		} catch (JSONException e) {
		}
		return false;
	}

	private Integer mPoiMapAction = null;
	@Override
	protected void commandSelect(String type, String command) {
		if (command.contains("那个")) {
			command = command.replace("那个", "的");
		}
		super.commandSelect(type, command);
	}

	@Override
	protected boolean onCommandSelect(String type, String speech) {
		final String command = speech;
		mPoiMapAction = null;
		if (TYPE_WAKEUP_CORRECT_SPEECH.equals(type)) {
			int grammarId = VoiceData.GRAMMAR_SENCE_NAVIGATE;
			if (PoiAction.ACTION_JINGYOU.equals(mData.action)) {
				grammarId = VoiceData.GRAMMAR_SENCE_SET_JINGYOU;
			} else if (PoiAction.ACTION_NAVI.equals(mData.action)) {
				grammarId = VoiceData.GRAMMAR_SENCE_NAVIGATE;
			} else {
				LogUtil.e("onCommandSelect: " + mData.action);
			}
			clearIsSelecting();
			RecorderWin.open(NativeData.getResString("RS_VOICE_CORRECT_SPEECH_TIPS"), grammarId);
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.putExtra("scene", "navigate")
					.putExtra("command", command)
					.buildCommReport());
			return true;
		}
		if ("SORT_DISTANCE".equals(type)) {
			sortListByComparator(command, new PoiComparator_Distance());
			return true;
		}
		if ("SORT_PRICE".equals(type)) {
			sortListByComparator(command, new PoiComparator_Price());
			return true;
		}
		if ("SORT_SCORE".equals(type)) {
			sortListByComparator(command, new PoiComparator_Score());
			return true;
		}
		if ("TYPE_START_NAV".equals(type)) {
			final Poi poi = mPage.getItemFromCurrPage(0);
			TtsManager.getInstance().speakText(NativeData.getResString("RS_NAV_START_NAV"), new ITtsCallback() {

				@Override
				public void onSuccess() {
					selectFinal(poi, 0, SELECT_TYPE_VOICE, command);
				}
			});
			return true;
		}
		mPoiMapAction = onMapCommandSelect(type, command);
		if(mPoiMapAction != null){
			if (mPoiMapAction == PoiMsg.MAP_ACTION_MAP ){
				if (mIsBusiness) {
                    getOption().setListPageName(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_MAP_LIST.name());
                } else {
                    getOption().setListPageName(TXZConfigManager.PageType.PAGE_TYPE_POI_MAP_LIST.name());
                }
			} else if (mPoiMapAction ==  PoiMsg.MAP_ACTION_LIST) {
			    if (mIsBusiness) {
                    getOption().setListPageName(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_LIST.name());
                } else {
                    getOption().setListPageName(TXZConfigManager.PageType.PAGE_TYPE_POI_LIST.name());
                }
			}
			ChoiceManager.getInstance().setPageSizeOption(getOption(), getOption().getListPageName());
			mPage.reCompute();
			if (mPage.getCurrPage() > mPage.getMaxPage() - 1) {
				mPage.selectPage(mPage.getMaxPage() - 1);
				updateDisplay(mPage.getResource());
				useWakeupAsrTask();
			} else {
				refreshCurrPage();
			}
			return true;
		}
		return super.onCommandSelect(type, command);
	}

	private void sortListByComparator(final String speech, Comparator<? super Poi> comparator) {
		Collections.sort(mData.mPois, comparator);
		// 更新数据
		refreshData(mData);
		if (isDelayAddWkWords()) {
			WinManager.getInstance().addViewStateListener(new IViewStateListener() {
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if (IViewStateListener.STATE_ANIM_ON_START != state) {
						return;
					}
					String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
					if (!TextUtils.isEmpty(sortSpk)) {
						if(speech.startsWith("按")) {
							sortSpk=sortSpk.replace("按","");
						}
						sortSpk = sortSpk.replace("%SORTSLOT%", speech);
					}
					//speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
					speakTtsInChoice(sortSpk );
					WinManager.getInstance().removeViewStateListener(this);
				}
			});
			return;
		}
		String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
		if (!TextUtils.isEmpty(sortSpk)) {
			if(speech.startsWith("按")) {
				sortSpk=sortSpk.replace("按","");
			}
			sortSpk = sortSpk.replace("%SORTSLOT%", speech);
		}
		//speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
		speakTtsInChoice(sortSpk );
	}
	
	@Override
	protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
		if (indexs.size() != 1) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					List<Poi> pois = new ArrayList<Poi>();
					for (Integer idx : indexs) {
						if (idx < mData.mPois.size()) {
							pois.add((Poi) mData.mPois.get(idx));
						}
					}
					preParse(pois);
					mData.mPois = pois;
					refreshData(mData);
					if (isDelayAddWkWords()) {
						WinManager.getInstance().addViewStateListener(new IViewStateListener() {
							@Override
							public void onAnimateStateChanged(Animation animation, int state) {
								if (IViewStateListener.STATE_ANIM_ON_START != state) {
									return;
								}
								String mLastHintText = NativeData.getResString("RS_VOICE_MULTIPLE_SELECTOR")
										.replace("%NUM%", String.valueOf(indexs.size()));
								speakTtsInChoice(mLastHintText);
								WinManager.getInstance().removeViewStateListener(this);
								super.onAnimateStateChanged(animation, state);
							}
						});
						return;
					}
					String mLastHintText;
					if(command.equals("有团购")||command.equals("有优惠")||command.equals("有停车场")||command.equals("有WIFI")){
						mLastHintText = NativeData.getResString("RS_VOICE_AREADY_SELECTOR").replace("%CMD%",command);
					}else{
						mLastHintText = NativeData.getResString("RS_VOICE_MULTIPLE_SELECTOR").replace("%NUM%",
								String.valueOf(indexs.size()));
					}
					speakTtsInChoice(mLastHintText);
				}
			}, 0);
			return true;
		}
		return false;
	}

	@Override
	public String getReportId() {
		return TASK_WAKE_ID;
	}
	
	@Override
	protected JSONBuilder getBaseReport() {
		JSONBuilder jsonBuilder = super.getBaseReport();
		jsonBuilder.put(KEY_KEYWORDS, mData.keywords);
		jsonBuilder.put(KEY_ACTION, mData.action);
		return jsonBuilder;
	}
	
	@Override
	protected void onItemSelect(Poi item, boolean isFromPage, int idx, String fromVoice) {
	}

	@Override
	protected void onSelectIndex(Poi item, boolean fromPage, int idx, String fromVoice) {
		selectPoi(item, idx, fromVoice != null ? SELECT_TYPE_VOICE : SELECT_TYPE_UNKNOW, fromVoice);
	}
	
	/**
	 * 判断是不是修改家和公司地址
	 * @return
	 */
	private boolean isSetAddress() {
		return (mData.action.equals(PoiAction.ACTION_COMPANY) 
				|| mData.action.equals(PoiAction.ACTION_HOME)
				|| mData.action.equals(PoiAction.ACTION_RECOMM_COMPANY)
				|| mData.action.equals(PoiAction.ACTION_RECOMM_HOME)) 
				&& justModifyHc();
	}
	
	public void selectPoi(final Poi poi, final int idx, final int selectType, final String fromVoice) {
		AppLogic.removeBackGroundCallback(mWakeupWordTip);
		if (!TextUtils.isEmpty(fromVoice)) {
			boolean addOnWay = NavManager.getInstance().mIsAddOnWay != 1;
			String hint = null;
			String ttsHint = null;
			if (isSetAddress()) {
				hint = "";
			} else if (mData.action.equals(PoiAction.ACTION_DEL_JINGYOU)) {
				hint = NativeData.getResString("RS_NAV_PATH_PLAN");
				ttsHint = NativeData.getResString("RS_NAV_DEL_JINGYOU_FAIL");
			} else if (addOnWay) {
				hint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%POINT%", fromVoice);
				ttsHint = NativeData.getResString("RS_MAP_PATH_FAIL");
			} else {
				hint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%PATH%", fromVoice);
				ttsHint = NativeData.getResString("RS_MAP_PATH_FAIL");
			}

			if (mData.action.equals(PoiAction.ACTION_JINGYOU)) {
				hint = NativeData.getResString("RS_NAV_PATH_THROUGH").replace("%POINT%", fromVoice);
				ttsHint = NativeData.getResString("RS_NAV_THROUGH_POINT_FAIL");
				if (NavManager.getInstance().isNavi()) {
					hint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%POINT%", fromVoice);
				}
			}
			NavManager.getInstance().setSpeechAfterPlanError(true, ttsHint);
			if (isSelectFinalAction(mData.action)) {
				selectFinal(poi, idx, selectType, fromVoice);
				return;
			}
			mSpeechTaskId = TtsManager.getInstance().speakText(hint, new ITtsCallback() {

				@Override
				public void onSuccess() {
					selectFinal(poi, idx, selectType, fromVoice);
				}
			});
		} else {
			selectFinal(poi, idx, selectType, fromVoice);
		}
	}

	private boolean procSenceByRemote(int idx,Poi poi){
		JSONBuilder json = new JSONBuilder();
		json.put("scene","nav");
		json.put("type","poiChoice");
		json.put("idx",idx);
		json.put("poi",poi.toString());
		json.put("action", mData.action);
		return SenceManager.getInstance().noneedProcSence("nav", json.toBytes());
	}

	private boolean isSelectFinalAction(String action) {
		return NavManager.getInstance().needSelectAction(action);
	}
	
	private boolean justModifyHc() {
		return NavManager.getInstance().isJustModifyAddress();
	}
	
	private void speakSetAddressSuccess() {
		if (NavManager.getInstance().isCloseWhenSetHcAddr()) {
			String ttsSucc = NativeData.getResString("RS_NAV_SET_HC_ADDR");
			String type = "";
			if (PoiAction.ACTION_HOME.equals(mData.action)) {
				type = "家";
			} else if (PoiAction.ACTION_COMPANY.equals(mData.action)) {
				type = "公司";
			}
			ttsSucc = ttsSucc.replace("%TYPE%",type);
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(ttsSucc, null);
			return;
		}
		String ttsSucc = NativeData.getResString("RS_NAV_SET_ADDRESS_SUCCESS");
		RecorderWin.speakText(ttsSucc, null);
	}
	
	private void selectFinal(final Poi poi, int idx, int selectType, String fromVoice) {
		if (poi == null){
			return;
		}
		NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_SELECT,
				new JSONBuilder().put("index", idx).toString());
		doReport(fromVoice != null, "select", poi);
		if (PoiAction.ACTION_NAVI.equals(mData.action)) {
		} else if (PoiAction.ACTION_HOME.equals(mData.action) || PoiAction.ACTION_RECOMM_HOME.equals(mData.action)) {
			NavManager.getInstance().setHomeLocation(poi.getName(), poi.getGeoinfo(), poi.getLat(), poi.getLng(),
					UiMap.GPS_TYPE_GCJ02);

			if(NavManager.getInstance().getNavigateInfo() != null){
				clearIsSelecting();
				NavManager.getInstance().navigateNearByHc(poi.getLng(), poi.getLat());
				return;
			}

			if (justModifyHc()) {
				clearIsSelecting();
				speakSetAddressSuccess();
				return;
			}
		} else if (PoiAction.ACTION_COMPANY.equals(mData.action)
				|| PoiAction.ACTION_RECOMM_COMPANY.equals(mData.action)) {
			NavManager.getInstance().setCompanyLocation(poi.getName(), poi.getGeoinfo(), poi.getLat(), poi.getLng(),
					UiMap.GPS_TYPE_GCJ02);

            if (NavManager.getInstance().getNavigateInfo() != null) {
                clearIsSelecting();
                NavManager.getInstance().navigateNearByHc(poi.getLng(), poi.getLat());
                return;
            }

			if (justModifyHc()) {
				clearIsSelecting();
				speakSetAddressSuccess();
				return;
			}
		} else if(PoiAction.ACTION_THIRD_POI.equals(mData.action)){
			procSenceByRemote(idx,poi);
			if(isNeedCloseWin){
				RecorderWin.close();
			}else{
				clearIsSelecting();
			}
			isNeedCloseWin = true;//恢复状态
			return;
		}
		navigateTo(poi, idx, selectType, fromVoice, mData.action);
		if (NetworkManager.getInstance().checkLeastFlow()) {
			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
			TtsManager.getInstance().speakText(resText);
		}
	}

	private boolean isNeedCloseWin = true;

	public void setIsNeedCloseWin(boolean isNeed){
		isNeedCloseWin = isNeed;
	}
	
	@Override
	public void cancelWithClose() {
		doReport(false, "cancel", null);
		super.cancelWithClose();
	}
	
	@Override
	public void selectCancel(int selectType, String fromVoice) {
		doReport(fromVoice != null, "cancel", null);
		putReport("poiAction", mData.action);
		if (PoiAction.ACTION_RECOMM_HOME.equals(mData.action) || PoiAction.ACTION_RECOMM_COMPANY.equals(mData.action)) {
			doReportSelectFinish(false, selectType, fromVoice);
			clearIsSelecting();
			String type = null;
			int grammar = 0;
			if (PoiAction.ACTION_RECOMM_HOME.equals(mData.action)) {
				type = "家";
				grammar = VoiceData.GRAMMAR_SENCE_SET_HOME;
			} else {
				type = "公司";
				grammar = VoiceData.GRAMMAR_SENCE_SET_COMPANY;
			}
			String spk = NativeData.getResString("RS_NAV_RECOMMAND_ERROR").replace("%ADDRESS%", type);
			if (!TextUtils.isEmpty(fromVoice)) {
				RecorderWin.setLastUserText(fromVoice);
			}
			RecorderWin.open(spk, grammar);
			return;
		}
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                WinLayoutManager.getInstance().releaseMapView();
            }
        });
		super.selectCancel(selectType, fromVoice);

		// 搜索列表出现时候触发onAddWakeupAsrCmd，会移除免唤醒插词，相对的在取消时候重新插入(若当前处于路径规划中)
		NavAmapValueService.getInstance().buildPlanningSelect(true);
	}
	
	/**
	 * 消息冲掉也属于取消
	 */
	public void reportCancel() {
		doReport(false, "cancel", null);
	}

	boolean isReported = true;

	private void doReport(boolean fromVoice, String action, Poi desPoi) {
		if (isReported) {
			return;
		}
		isReported = true;

		JSONBuilder report = new JSONBuilder().put("keywords", mData.keywords).put("fromVoice", fromVoice);
		if (desPoi != null) {
			report.put("poiDes", desPoi.toString());
			report.put("index", mData.mPois.indexOf(desPoi));
		}

		ReportUtil.doReport(
				new Report.Builder().setType("poi").setAction(action).setKeywords(report.toString()).buildCommReport());
	}
	
	private void navigateTo(final Poi poi, final int idx, final int selectType, final String fromVoice,
			final String action) {
		putReport(KEY_DETAIL, convItemToString(poi));
		// Poi中的action不一定是实际运用的action，途经点设置失败状态导航的时候有这种情况
		putReport("poiAction", action);
		putReport(KEY_INDEX, idx + "");
		putReport("navPkn", NavManager.getInstance().getLocalNavImpl().getPackageName());
		doReportSelectFinish(true, selectType, fromVoice);

		JNIHelper.logd("navi poi:" + poi.toString() + ",action:" + action);
		if (PoiAction.ACTION_JINGYOU.equals(action)) {
			boolean bSucc = NavManager.getInstance().procJingYouPoi(poi);
			if (bSucc) {
				RecorderWin.close();
				return;
			} else {
				String spk = NativeData.getResString("RS_MAP_THROUGH_POINT_FAIL_NAV");
				TtsManager.getInstance().speakText(spk, new ITtsCallback() {

					@Override
					public void onSuccess() {
						navigateTo(poi, idx, selectType, fromVoice, PoiAction.ACTION_NAVI);
					}
				});
				return;
			}
		} else if (PoiAction.ACTION_DEL_JINGYOU.equals(mData.action)) {
			boolean bSucc = NavManager.getInstance().deleteJingYou(poi);
			if (bSucc) {
				RecorderWin.close();
			}
			return;
		}

		NavThirdApp navThirdApp = NavManager.getInstance().getLocalNavImpl();
		if (navThirdApp != null) {
			if (PoiAction.ACTION_HOME.equals(action) || PoiAction.ACTION_COMPANY.equals(action)
					|| PoiAction.ACTION_RECOMM_HOME.equals(action) || PoiAction.ACTION_RECOMM_COMPANY.equals(action)) {
				boolean willNav = (navThirdApp).willNavAfterSet();
				if (!willNav) {
					return;
				}
			}
		}

		NavManager.getInstance().NavigateTo(poi);
	}

	@Override
	protected ResourcePage<PoisData, Poi> createPage(PoisData sources) {
		final int allSize = sources.mPois.size();
		return new ResPoiPage(sources) {

			@Override
			protected int numOfPageSize() {
				if (!is2_0Version()) {
					return allSize;
				}
				return getOption().getNumPageSize();
			}

			@Override
			public Poi removeFromSource(int cIdx, int tIdx) {
				if (tIdx >= 0 && tIdx < mSourceRes.mPois.size()) {
					return mSourceRes.mPois.remove(tIdx);
				}
				return null;
			}
		};
	}

	@Override
	protected void onConvToJson(PoisData ts, JSONBuilder jsonBuilder) {
		jsonBuilder.put("type", RecorderWin.PoiChoiceSence);
		jsonBuilder.put("keywords", ts.keywords);
		jsonBuilder.put("poitype", ts.isBus ? "business" : "");
		jsonBuilder.put("action", ts.action);
		jsonBuilder.put("city", ts.city);
		jsonBuilder.put("count", ts.mPois.size());
		jsonBuilder.put("showcount",getOption().getNumPageSize());
		if(mPoiMapAction != null){
			jsonBuilder.put("mapAction",mPoiMapAction);
			mPoiMapAction = null;
			mNeedPlay = true;
		}
		boolean isList = NavManager.getInstance().getPoiShowIsList();
		jsonBuilder.put("listmodel", isList);
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < ts.mPois.size(); i++) {
			Poi poi = (Poi) ts.mPois.get(i);
			try {
				JNIHelper.logd("TXZPoiSearchTool poitype: " + poi.toJsonObject().put("poitype", poi.getPoiType()));
				jsonArray.put(poi.toJsonObject().put("poitype", poi.getPoiType()));
			} catch (JSONException e) {
				e.printStackTrace();
				LogUtil.loge("PoiWorkChoice convToJson error : " + e.getLocalizedMessage());
			}
		}
		jsonBuilder.put("pois", jsonArray);

		//出现个jsonArray与ts.mPois 个数不一致的bug，以jsonArray的为准
		jsonBuilder.put("count", jsonArray.length());
		boolean isAddDestina = ts.action.equals(PoiAction.ACTION_JINGYOU) || 
											  ts.action.equals(PoiAction.ACTION_DEL_JINGYOU) ||
											  ts.action.equals(PoiAction.ACTION_NAVI_END) ;
		if(isAddDestina){
			double[] destination = NavManager.getInstance().getLocalNavImpl().getDestinationLatlng();
			if(destination != null && destination.length ==2){
				JNIHelper.logd("zsbin: destination[0]= "+destination[0]+" destination[1]="+destination[1]);
				jsonBuilder.put("destinationLat",destination[0]);
				jsonBuilder.put("destinationLng",destination[1]);
			}
		}
		LocationInfo lastLocation = LocationManager.getInstance().getLastLocation();
		if(lastLocation != null && lastLocation.msgGpsInfo != null ){
			jsonBuilder.put("locationLat", lastLocation.msgGpsInfo.dblLat);
			jsonBuilder.put("locationLng",lastLocation.msgGpsInfo.dblLng);
		}

		jsonBuilder.put("isHistory",true);
		jsonBuilder.put("business",ts.isBus);
		mIsBusiness = ts.isBus;
		if (PoiAction.ACTION_NAVI.equals(ts.action) || PoiAction.ACTION_NAVI_END.equals(ts.action)) {
			addNavTips(jsonBuilder, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT_NEW"), ts);
		} else if (PoiAction.ACTION_JINGYOU.equals(ts.action)) {
			addNavTips(jsonBuilder, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT_NEW"), ts);
		} else if (PoiAction.ACTION_HOME.equals(ts.action)) {
			addNavTips(jsonBuilder, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT_NEW"), ts);
//			addSetShowTips(jsonBuilder, "家", ts);
		} else if (PoiAction.ACTION_COMPANY.equals(ts.action)) {
			addNavTips(jsonBuilder, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT_NEW"), ts);
//			addSetShowTips(jsonBuilder, "公司", ts);
		} else if (PoiAction.ACTION_DEL_JINGYOU.equals(ts.action)) {
			addSetShowTipsDelJingYou(jsonBuilder, "途经点", ts);
		} else if (PoiAction.ACTION_NAV_RECOMMAND.equals(ts.action)) {
			jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_RECOMMAND"));
		} else if (PoiAction.ACTION_RECOMM_HOME.equals(ts.action)) {
			jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_RECOMMAND_ADDRESS").replace("%ADDRESS%", "家"));
		} else if (PoiAction.ACTION_RECOMM_COMPANY.equals(ts.action)) {
			jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_RECOMMAND_ADDRESS").replace("%ADDRESS%", "公司"));
		} else if(PoiAction.ACTION_NAV_COLLECTION_POINT.equals(ts.action)){
			jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_COLLECTION_POINT"));
		} else if(PoiAction.ACTION_THIRD_POI.equals(ts.action)){
			jsonBuilder.put("prefix", ts.tips);
		} else if(PoiAction.ACTION_PASS_NAV.equals(ts.action)) {
			jsonBuilder.put("prefix", getOption().getTtsText());
			// addNavTips(jsonBuilder, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT_NEW"), ts);
		} else if(PoiAction.ACTION_WITH_FROM_POI_NAV.equals(ts.action)){
			jsonBuilder.put("prefix", getOption().getTtsText());
		}

		jsonBuilder.put("vTips",getTips(isList,ts.action));
	}

	private String getTips(boolean isList,String action){
		String tips = "";
		if (mPage != null) {
			if (PoiAction.ACTION_NAV_RECOMMAND.equals(action)) {
				if (mPage.getCurrPageSize() == 1) {
					tips = NativeData.getResString("RS_VOICE_TIPS_RECOMMAND_POI_ONE");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_RECOMMAND_POI_MORE");
				}
			} else if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getCurrPageSize() == 1) {
					tips =  NativeData.getResString(mPage.getCurrPage() == 0 ? "RS_VOICE_TIPS_POI_ONE" : "RS_VOICE_TIPS_POI_ONE_LAST");
				} else if (mPage.getCurrPageSize() == 2) {
					tips = NativeData.getResString("RS_VOICE_TIPS_POI_TWO");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_POI_MORE");
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                if (mPage.getCurrPageSize() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_POI_FIRST_PAGE_ONLY_ONE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_POI_FIRST_PAGE");
                }
            } else { //其他中间页
                if (mPage.getCurrPageSize() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_POI_OTHER_PAGE_ONLY_ONE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_POI_OTHER_PAGE");
                }
            }
		}
		if (!TextUtils.isEmpty(tips)) {
			tips = tips.replaceAll((isList ?"；列表模式":"；地图模式"),"");
		}
		return tips;
	}

	private void addNavTips(JSONBuilder jb, String showTxt, final PoisData mPoisData) {
		JNIHelper.logd("zsbin: addNavTips");
		if(mPage != null ){
			showTxt = replaceTips(showTxt, "%COUNT%", mPage.getTotalSize() + "");
		}else{
			showTxt = replaceTips(showTxt, "%COUNT%",  "0");
		}
		
		mTipCityStr = NavManager.getInstance().getTipCityString();
		String keyWord = null;
			if(TextUtils.isEmpty(keyWord)){
				keyWord = mPoisData.keywords;
			}
		try {
			if (showTxt.contains("%POINAME%")) {
				String[] tips = showTxt.split("%POINAME%");
				int len = tips.length;
				if (tips != null && len > 0) {
					if (len == 1) {
						if (showTxt.startsWith("%POINAME%")) {
							showTxt = showTxt.replace("%POINAME%", "");
							jb.put("aftfix", showTxt);
						}
						;
					} else if (len == 2) {
						jb.put("aftfix", tips[1]);
					}
					jb.put("titlefix", keyWord);
					if (!tips[0].contains("%CITY%")) {
						jb.put("prefix", tips[0]);
					}
				}

			}
			if (showTxt.contains("%CITY%")) {
				String[] tips = showTxt.split("%CITY%");
				int len = tips.length;
				if (tips != null && len > 0) {
					if (len == 1) {
						if (showTxt.endsWith("%CITY%")) {
							showTxt = showTxt.replace("%CITY%", "");
							jb.put("prefix", showTxt);
						}
					} else if (len == 2) {
						jb.put("prefix", tips[0]);
					}
					
					jb.put("city", mTipCityStr);
					
					if (!tips[tips.length - 1].contains("%POINAME%")) {
						jb.put("aftfix", tips[tips.length - 1]);
					}
				}
			}else{
				String city = jb.getVal("city", String.class);
				if(city != null){
					jb.remove("city");
				}
			}
			if (showTxt.contains("%CITY%") && showTxt.contains("%POINAME%")) {
				String[] split = showTxt.split("%POINAME%")[0].split("%CITY%");
				int len = split.length;
				if (split != null && len > 0) {
					jb.put("midfix", split[len - 1]);
				}
			}
			if (!showTxt.contains("%CITY%") && !showTxt.contains("%POINAME%")) {
				jb.put("prefix", showTxt);
			}
		} catch (Exception e) {
			JNIHelper.loge(e.toString());
		}
	}
	
	private String replaceTips(String src, String tips, String des) {
		if (TextUtils.isEmpty(src)) {
			return src;
		}
		if (des == null) {
			des = "";
		}

		if (src.contains(tips)) {
			src = src.replace(tips, des);
		}
		return src;
	}

	int mPoiGeoCodeReturn = 0;
	private boolean mNeedPlay = true;

	private void addSetShowTipsDelJingYou(JSONBuilder jb, String type, PoisData poisData) {
		String showTxt = NativeData.getResString("RS_POI_DISPLAY_DEL_HINT");
		showTxt = showTxt.replace("%TYPE%", type);
		addNavTips(jb, showTxt, poisData);
	}

	private void addSetShowTips(JSONBuilder jb, String type, PoisData poisData) {
		String showTxt = NativeData.getResString("RS_POI_DISPLAY_SET_HINT");
		showTxt = showTxt.replace("%TYPE%", type);
		addNavTips(jb, showTxt, poisData);
	}

	public static final String KEY_EDIT_TYPE = "editType";
	public static final String KEY_EDIT_KWS = "editKws";
	public static final String KEY_POI_ACTION = "poiAction";
	
	/**
	 * 通知点击了编辑入口
	 */
	public void notifyShowEditPoiPage() {
		try {
			NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_CLICK_EDIT_POI, null);
			stopTtsAndAsr();
			pauseContinuityTip();
			BeepPlayer.cancelMusic();
			final String city ;
			if( TextUtils.isEmpty(mTipCityStr)||
				mTipCityStr.equals("多个城市") ||
				mTipCityStr.equals("LOADING") ||
				mTipCityStr.equals("附近") ){
				city = mCurrPoisData.city;
			}else{
				city = mTipCityStr;
			}
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					SearchEditManager.attachParent(PoiWorkChoice.this);
					SearchEditManager.getInstance().setNeedCloseDialog(true);
					SearchEditManager.getInstance().dismiss();
					if (mCurrPoisData.mPois.size() < 1) {
						SearchEditManager.getInstance().setNeedCloseDialog(false);
					}

					if (PoiAction.ACTION_NAVI.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviDefault(SearchEditManager.TYPE_SEARCH_EDIT, mCurrPoisData.keywords,city);
					}
					if (PoiAction.ACTION_COMPANY.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviCompany(SearchEditManager.TYPE_SEARCH_EDIT, mCurrPoisData.keywords,city);
					}
					if (PoiAction.ACTION_HOME.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviHome(SearchEditManager.TYPE_SEARCH_EDIT, mCurrPoisData.keywords,city);
					}
					if (PoiAction.ACTION_JINGYOU.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviJingYou(SearchEditManager.TYPE_SEARCH_EDIT, mCurrPoisData.keywords,city);
					}
					if (PoiAction.ACTION_NAVI_END.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviEnd(SearchEditManager.TYPE_SEARCH_EDIT, mCurrPoisData.keywords,mCurrPoisData.city);
					}
				}
			}, 0);

			ReportUtil
					.doReport(new ReportUtil.Report.Builder().setAction("edit").setKeywords("导航地址").buildTouchReport());

		} catch (Exception e) {
		}
	}
	
	/**
	 * 通知点击了城市编辑入口
	 */
	public void notifyShowEditCityPage() {
		try {
			NavManager.getInstance().notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_CLICK_EDIT_CITY, null);
			stopTtsAndAsr();
			BeepPlayer.cancelMusic();
			pauseContinuityTip();
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					SearchEditManager.attachParent(PoiWorkChoice.this);
					String cityName = mTipCityStr;
					if (PoiAction.ACTION_NAVI.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviDefault(SearchEditManager.TYPE_SELECT_CITY, mCurrPoisData.keywords,mTipCityStr);
					}
					if (PoiAction.ACTION_COMPANY.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviCompany(SearchEditManager.TYPE_SELECT_CITY, mCurrPoisData.keywords,mTipCityStr);
					}
					if (PoiAction.ACTION_HOME.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviHome(SearchEditManager.TYPE_SELECT_CITY, mCurrPoisData.keywords,mTipCityStr);
					}
					if (PoiAction.ACTION_JINGYOU.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviJingYou(SearchEditManager.TYPE_SELECT_CITY, mCurrPoisData.keywords,mTipCityStr);
					}
					if (PoiAction.ACTION_NAVI_END.equals(mCurrPoisData.action)) {
						SearchEditManager.getInstance().naviEnd(SearchEditManager.TYPE_SELECT_CITY, mCurrPoisData.keywords,mTipCityStr);
					}
				}
			}, 0);

			ReportUtil
					.doReport(new ReportUtil.Report.Builder().setAction("edit").setKeywords("导航城市").buildTouchReport());

		} catch (Exception e) {
		}
	}
	public void mapPoiViewLoading() {
		if(NavManager.getInstance().getIsHaveResultPre()){
			mPoiMapAction = PoiMsg.MAP_ACTION_LOADING;
			mNeedPlay = false;
			refreshCurrPage();			
		}
	}
	
	public void speakTtsInChoice(String spk) {
		if(aWakeUpWord.size() > 0){
			AppLogic.removeBackGroundCallback(mWakeupWordTip);
			AppLogic.runOnBackGround(mWakeupWordTip,10000);
		}
		speakWithTips(spk);
	}
	
	public void mapActionResult(int action, boolean result) {
		if (aWakeUpWord.size() > 0) {
			AppLogic.removeBackGroundCallback(mWakeupWordTip);
			AppLogic.runOnBackGround(mWakeupWordTip, 10000);
		}
		String spk = getMapTtsStrData(action, result);
		speakWithTips(spk);
	}
	
	public static String getMapTtsStrData(int action, boolean result) {
		String spk = null;
		if (action == PoiMsg.MAP_ACTION_ENLARGE || action == PoiMsg.MAP_ACTION_NARROW) {
			String actionStr = null;
			String reason = null;
			if (action == PoiMsg.MAP_ACTION_ENLARGE) {
				actionStr = "放大";
				reason = "最大倍数";
			} else {
				actionStr = "缩小";
				reason = "最小倍数";
			}
			if (result) {
				spk = NativeData.getResString("RS_MAP_ACTION_SUCCESS_HINT").replace("%ACTION%", actionStr);
			} else {
				spk = NativeData.getResString("RS_MAP_ACTION_FAULT_HINT").replace("%REASON%", reason)
						.replace("%ACTION%", actionStr);
			}
		}else if(action ==  PoiMsg.MAP_ACTION_LIST){
			//spk = NativeData.getResString("RS_MAP_ACTION_CHANGE_MODEL_HINT").replace("%MODEL%", "列表");
			spk = NativeData.getResString("RS_MAP_ACTION_CHANGE_MODEL_HINT");
		}else if(action ==  PoiMsg.MAP_ACTION_MAP){
			//spk = NativeData.getResString("RS_MAP_ACTION_CHANGE_MODEL_HINT").replace("%MODEL%", "地图");
			spk = NativeData.getResString("RS_MAP_ACTION_CHANGE_MODEL_HINT");
		}
		return spk;
	}
	@Override
	public boolean nextPage(String fromVoice) {
		JNIHelper.logd("zsbin: nextPage");
		if (aWakeUpWord.size() > 0) {
			AppLogic.removeBackGroundCallback(mWakeupWordTip);
			AppLogic.runOnBackGround(mWakeupWordTip, 10000);
		}

		boolean ret = super.nextPage(fromVoice);

		if (mPage != null) {
			NavManager.getInstance()
					.notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_PAGE_CHANGE,
							new JSONBuilder().put("page", mPage.getCurrPage()).toString());
		}

		return ret;
	}
	@Override
	public boolean lastPage(String fromVoice) {
		JNIHelper.logd("zsbin: lastPage");
		if (aWakeUpWord.size() > 0) {
			AppLogic.removeBackGroundCallback(mWakeupWordTip);
			AppLogic.runOnBackGround(mWakeupWordTip, 10000);
		}

		boolean ret = super.lastPage(fromVoice);

		if (mPage != null) {
			NavManager.getInstance()
					.notifyPoiViewState(TXZPoiSearchManager.CMD_POIVIEW_ON_PAGE_CHANGE,
							new JSONBuilder().put("page", mPage.getCurrPage()).toString());
		}

		return ret;
	}
	@Override
	protected String convItemToString(Poi item) {
		if (item == null){
			return "";
		}
		return item.toString();
	}

	public String getConverKeyWord(String keyword) {
		if (mMapKeyWord.isEmpty()) {
			com.alibaba.fastjson.JSONObject jsonNearActions = com.alibaba.fastjson.JSONObject.parseObject(NativeData
					.getResJson("SEARCH_KEYWORD_CONVER_LIST"));
			if (jsonNearActions != null) {
				Set<String> keySet = jsonNearActions.keySet();
				String[] keyArray = (String[]) keySet.toArray(new String[keySet
						.size()]);
				for (int i = 0; i < keyArray.length; i++) {
					String szName = jsonNearActions.getString(keyArray[i]);
					if (TextUtils.isEmpty(szName) || szName.charAt(0) == '\0') {
						continue;
					}
					mMapKeyWord.put(keyArray[i], szName);
				}
			}
		}
		if (mMapKeyWord.containsKey(keyword)) {
			return mMapKeyWord.get(keyword);
		}
		return null;
	}

}