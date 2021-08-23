package com.txznet.txz.component.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;

import com.txz.ui.map.UiMap;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.poi.PoiComparator_Distance;
import com.txznet.record.poi.PoiComparator_Price;
import com.txznet.record.poi.PoiComparator_Score;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.nav.SearchEditDialog;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.KeywordsParser;
import com.txznet.txz.util.runnables.Runnable4;

import android.text.TextUtils;

public class PoiSelectorControl extends ISelectControl {

	public PoiSelectorControl(int pageCount) {
		super(pageCount);
	}

	public static class PoisData {
		public String city;
		public String keywords;
		public String action;
		public boolean isBus;
		public List<Poi> mPois;
	}

	private boolean mHasPrices;
	private boolean mHasScore;
	private boolean mExistSameCity;
	protected PoisData mPoisData;

	public void showPoiSelectList(PoisData pd) {
		this.mPoisData = pd;
		if (mPoisData == null) {
			return;
		}

		if (mPoisData.mPois == null) {
			// TODO 处理结果为空的情况
			return;
		}
		
		preParse(mPoisData);

		beginSelectorParse(pd.mPois, "");
	}

	@Override
	protected String getBeginSelectorHint() {
		List<Poi> pois = mPoisData.mPois;
		String action = mPoisData.action;
		if (action == null) {
			action = PoiAction.ACTION_NAVI;
		}
		String city = mPoisData.city;
		if (city == null) {
			city = "";
		}
		String keywords = mPoisData.keywords;
		if (keywords == null) {
			keywords = "";
		}
		
		if (pois == null || pois.size() == 0) {
			String txt = "";
			if (RecordInvokeFactory.hasThirdImpl()) {
				txt = replaceStr("RS_POI_SELECT_NO_RESULT_THIRD", "%KEYWORDS%", keywords);
				RecorderWin.addSystemMsg(txt);
			} else {
				txt = replaceStr("RS_POI_SELECT_NO_RESULT", "%KEYWORDS%", keywords);
			}
			return txt;
		}
		
		int count = pois.size();
		if(PoiAction.ACTION_HOME.equals(action) || PoiAction.ACTION_COMPANY.equals(action)){
			String type = "";
			if(PoiAction.ACTION_HOME.equals(action)){
				type = "家";
			} else if(PoiAction.ACTION_COMPANY.equals(action)){
				type = "公司";
			}
			
			if (count == 1) {
				//mUseAutoPerform = true;
				return replaceStr("RS_POI_SELECT_SET_SINGLE_SPK", "%TYPE%", type);
			} else {
				//mUseAutoPerform = false;
				String hint = replaceStr("RS_POI_SELECT_SET_LIST_SPK", "%CITY%", city);
				try {
					hint = hint.replace("%POINAME%", keywords);
					hint = hint.replace("%COUNT%", count + "");
					hint = hint.replace("%TYPE%", type);
				} catch (Exception e) {
				}
				return hint;
			}
		}
		
		if (PoiAction.ACTION_JINGYOU.equals(action)) {
			if (count == 1) {
				//mUseAutoPerform = true;
				return replaceStr("RS_POI_SELECT_JINGYOU_SINGLE_SPK", "%POINAME%", keywords);
			}
		}

		// 默认是导航场景
		if (count == 1) {
			//mUseAutoPerform = true;
			return replaceStr("RS_POI_SELECT_NAV_SINGLE_SPK", "", "");
		}else{
			//mUseAutoPerform = false;
		}

		String slotCity = city;
		if (mExistSameCity) {
			slotCity = "";
		}
		
		String resHint = replaceStr("RS_POI_SELECT_NAV_LIST_SPK", "%CITY%", slotCity);
		
		try {
			resHint = resHint.replace("%POINAME%", mPoisData.keywords);
			resHint = resHint.replace("%COUNT%", count+"");
		} catch (Exception e) {
		}
		return resHint;
	}
	
	private String replaceStr(String resId, String srcStr, String desStr) {
		String resStr = NativeData.getResString(resId);
		if(resStr == null){
			return "";
		}
		
		try {
			return resStr.replace(srcStr, desStr);
		} catch (Exception e) {
			return resStr;
		}
	}
	
	@Override
	protected void onSrcListUpdate(List tmp) {
		JNIHelper.logd("onSrcListUpdate:" + tmp);
		if (mPoisData == null) {
			return;
		}

		JSONBuilder jb = new JSONBuilder();
		jb.put("type", 2);
		jb.put("keywords", mPoisData.keywords);
		jb.put("poitype", mPoisData.isBus ? "business" : "");
		jb.put("action", mPoisData.action);
		jb.put("city", mPoisData.city);
		jb.put("count", mTmpList.size());
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < mTmpList.size(); i++) {
			Poi poi = (Poi) mTmpList.get(i);
			jsonArray.put(poi.toJsonObject());
		}
		jb.put("pois", jsonArray);

		// 添加上下页信息
		jb.put("curPage", mPageHelper.getCurPager());
		jb.put("maxPage", mPageHelper.getMaxPager());
		
		if (PoiAction.ACTION_NAVI.equals(mPoisData.action)) {
			addNavTips(jb, NativeData.getResString("RS_POI_DISPLAY_NAV_HINT"));
		} else if (PoiAction.ACTION_JINGYOU.equals(mPoisData.action)) {
			addSetShowTips(jb, "途经点");
		} else if (PoiAction.ACTION_HOME.equals(mPoisData.action)) {
			addSetShowTips(jb, "家");
		} else if (PoiAction.ACTION_COMPANY.equals(mPoisData.action)) {
			addSetShowTips(jb, "公司");
		}

		RecorderWin.sendSelectorList(jb.toString());
	}
	
	private void addNavTips(JSONBuilder jb,String showTxt){
		try {
			showTxt = replaceTips(showTxt, "%CITY%", mPoisData.city);
			showTxt = replaceTips(showTxt, "%COUNT%", mSourceList.size() + "");
			if (showTxt.contains("%POINAME%")) {
				String[] tips = showTxt.split("%POINAME%");
				int len = tips.length;
				if (tips != null && len > 0) {
					if (len == 1) {
						if (showTxt.startsWith("%POINAME%")) {
							showTxt = showTxt.replace("%POINAME%", "");
							jb.put("titlefix", mPoisData.keywords);
							jb.put("aftfix", showTxt);
						}else {
							showTxt = showTxt.replace("%POINAME%", "");
							jb.put("prefix", showTxt);
							jb.put("titlefix", mPoisData.keywords);
						}
					} else if (len == 2) {
						showTxt = showTxt.replace("%POINAME%", "");
						jb.put("titlefix", mPoisData.keywords);
						jb.put("prefix", tips[0]);
						jb.put("aftfix", tips[1]);
					}
				} else {
					jb.put("titlefix", showTxt.replace("%POINAME%", mPoisData.keywords));
				}
			} else {
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
	
	private void addSetShowTips(JSONBuilder jb,String type){
		String showTxt = NativeData.getResString("RS_POI_DISPLAY_SET_HINT");
		showTxt = showTxt.replace("%TYPE%", type);
		addNavTips(jb, showTxt);
	}

	@Override
	protected String getAsrTaskId() {
		return "CTRL_PoiSelectorControl";
	}

	@Override
	protected int getSenceGrammar() {
		return VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL;
	}

	public void OnItemClick(int pos) {
		if (mDelayTask != null) {
			mDelayTask.clearProgress();
		}
		stopTtsAndAsr();

		int position = mPageHelper.getCurPager() * mPageHelper.getPagerCount() + pos;
		List<BusinessPoiDetail> mBusPois = new ArrayList<BusinessPoiDetail>();
		if (mPoisData.isBus) {
			for (Object poi : mPoisData.mPois) {
				mBusPois.add((BusinessPoiDetail) poi);
			}
		}
		if (procByWinDialog(mBusPois, mPoisData.mPois, mPoisData.isBus, position, mPoisData.action)) {
			return;
		}
	}

	public boolean procByWinDialog(final List<BusinessPoiDetail> mBus, final List<Poi> pois, final boolean isBus,
			final int selectIndex, final String action) {
		LogUtil.logd("procByWinDialog");
		AppLogic.runOnUiGround(
				new Runnable4<List<BusinessPoiDetail>, List<Poi>, Boolean, Integer>(mBus, pois, isBus, selectIndex) {

					@Override
					public void run() {
						int type = 0;
						if (PoiAction.ACTION_NAVI.equals(action)) {
							type = 0;
						} else if (PoiAction.ACTION_HOME.equals(action)) {
							type = 1;
						} else if (PoiAction.ACTION_COMPANY.equals(action)) {
							type = 2;
						}

						if (mP3) {
							//WinMapDialog.getInstance().refreshWithPoiResult(mP1, mP4, true, type);
						} else {
							//WinMapDialog.getInstance().refreshWithPoiResult(mP2, mP4, type);
						}
					}
				}, 0);
		return true;
	}

	@Override
	protected void onItemSelect(final Object obj, int index, String fromVoice) {
		if (obj == null || (obj instanceof Poi) == false) {
			RecorderWin.close();
			return;
		}

		if (fromVoice == null) {
			selectPoi((Poi) obj);
		} else {
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.getInstance().speakText(fromVoice, new ITtsCallback() {
				@Override
				public void onSuccess() {
					selectPoi((Poi) obj);
				}
			});
		}
	}

	@Override
	protected void onCommandSelect(final List tmp, int index, String speech) {
		final Poi poi = (Poi) tmp.get(index);
		if (!TextUtils.isEmpty(speech)) {
			clearIsSelecting();
			String hint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%CMD%", speech);
			String ttsHint = NativeData.getResString("RS_MAP_PATH_FAIL");

			if (mPoisData.action.equals(PoiAction.ACTION_JINGYOU)) {
				hint = NativeData.getResString("RS_NAV_PATH_THROUGH").replace("%CMD%", speech);
				ttsHint = NativeData.getResString("RS_NAV_THROUGH_POINT_FAIL");
				if (NavManager.getInstance().isNavi()) {
					hint = NativeData.getResString("RS_NAV_PATH_REPLANNING").replace("%CMD%", speech);
				}
			}
			NavManager.getInstance().setSpeechAfterPlanError(true, ttsHint);
			mSpeechTaskId = TtsManager.getInstance().speakText(hint, new ITtsCallback() {

				@Override
				public void onSuccess() {
					selectPoi(poi);
				}
			});
		} else {
			selectPoi(poi);
		}
	}

	public void selectPoi(final Poi mPoi) {
		if (PoiAction.ACTION_NAVI.equals(mPoisData.action)) {
		} else if (PoiAction.ACTION_HOME.equals(mPoisData.action)) {
			NavManager.getInstance().setHomeLocation(mPoi.getName(), mPoi.getGeoinfo(), mPoi.getLat(), mPoi.getLng(),
					UiMap.GPS_TYPE_GCJ02);
		} else if (PoiAction.ACTION_COMPANY.equals(mPoisData.action)) {
			NavManager.getInstance().setCompanyLocation(mPoi.getName(), mPoi.getGeoinfo(), mPoi.getLat(), mPoi.getLng(),
					UiMap.GPS_TYPE_GCJ02);
		} else if (PoiAction.ACTION_JINGYOU.equals(mPoisData.action)) {
			boolean bSucc = NavManager.getInstance().procJingYouPoi(mPoi);
			if (bSucc) {
				RecorderWin.close();
				return;
			} else {
				String spk = NativeData.getResString("RS_MAP_THROUGH_POINT_FAIL_NAV");
				TtsManager.getInstance().speakText(spk, new ITtsCallback() {

					@Override
					public void onSuccess() {
						RecorderWin.close();
						navigateTo(mPoi, mPoisData.action);
					}
				});
				return;
			}
		}

		RecorderWin.close();
		navigateTo(mPoi, mPoisData.action);
	}

	private static void navigateTo(Poi poi, String action) {
		JNIHelper.logd("navi poi:" + poi.toString());
		NavThirdApp navThirdApp = NavManager.getInstance().getLocalNavImpl();
		if (navThirdApp != null) {
			if (PoiAction.ACTION_HOME.equals(action) || PoiAction.ACTION_COMPANY.equals(action)) {
				boolean willNav = (navThirdApp).willNavAfterSet();
				if (!willNav) {
					return;
				}
			}
		}

		NavManager.getInstance().NavigateTo(poi);
	}
	
	private void preParse(PoisData pd) {
		mHasPrices = false;
		mHasScore = false;
		mExistSameCity = false;
		List items = mPoisData.mPois;
		if (items == null || items.size() < 1)
			return;
		
		final boolean isBus = pd.isBus;
		List<String> citys = new ArrayList<String>();
		for (int i = 0; i < items.size(); i++) {
			Poi poi = (Poi) items.get(i);
			if (citys.contains(poi.getCity())) {
				mExistSameCity = true;
			}
			
			if (isBus) {
				BusinessPoiDetail bpd = (BusinessPoiDetail) items.get(i);
				if (bpd.getAvgPrice() > 0) {
					mHasPrices = true;
					JNIHelper.logd("BusinessPoi has price");
					if (mHasScore && mExistSameCity) {
						break;
					}
				}

				if (bpd.getScore() > 0) {
					mHasScore = true;
					JNIHelper.logd("BusinessPoi has score");
					if (mHasPrices && mExistSameCity) {
						break;
					}
				}
			}
		}
	}

	public void onRightItemBtnPf(int pos) {
		JNIHelper.logd("onRightItemBtnPf:" + pos);
		if (pos < 0) {
			return;
		}

		if (mTmpList != null && pos < mTmpList.size()) {
			// TODO 点击右侧按钮的动作
			onItemSelect(mTmpList.get(pos), pos, null);
		}
	}

	@Override
	protected void onAsrComplexSelect(AsrComplexSelectCallback acsc) {
		if (mPoisData.mPois.size() > 1) {
			acsc.addCommand("SORT_DISTANCE", "距离排序", "远近排序");
			if (mHasScore) {
				acsc.addCommand("SORT_SCORE", "分数排序", "评分排序", "评价排序", "好评排序");
			}
			if (mHasPrices)
				acsc.addCommand("SORT_PRICE", "价钱排序", "价格排序", "消费排序");
		}

		int minDistance = -1;
		int minDistanceIndex = -1;
		for (int i = 0; i < mPoisData.mPois.size(); i++) {
			Poi poi = mPoisData.mPois.get(i);
			if (mPoisData.isBus) {
				BusinessPoiDetail businessPoi = null;
				if (poi instanceof BusinessPoiDetail) {
					businessPoi = (BusinessPoiDetail) poi;
				} else {
					LogUtil.loge("!!!!!!!!!!BusinessPoiDetail CastException!");
					continue;
				}
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
				if (categories != null) {
					for (String c : regions) {
						addIndex(acsc, i, c);
					}
				}
				// 团购优惠其他
				if (businessPoi.isHasDeal())
					acsc.addIndex(i, "有团购");
				if (businessPoi.isHasCoupon())
					acsc.addIndex(i, "有优惠");
				if (businessPoi.isHasPark())
					acsc.addIndex(i, "有停车场");
				if (businessPoi.isHasWifi())
					acsc.addIndex(i, "有WIFI");
				if (!TextUtils.isEmpty(businessPoi.getTelephone()))
					acsc.addIndex(i, "有电话");
			}

			int d = mPoisData.mPois.get(i).getDistance();
			if (minDistance < 0 || d < minDistance) {
				minDistanceIndex = i;
				minDistance = d;
			}

			for (String kw : KeywordsParser.splitKeywords(poi.getName())) {
				if (mPoisData.keywords.equals(kw))
					continue;
				if (kw.startsWith(mPoisData.keywords)) {
					addIndex(acsc, i, kw.substring(mPoisData.keywords.length()));
				} else if (kw.endsWith(mPoisData.keywords)) {
					addIndex(acsc, i, kw.substring(0, kw.length() - mPoisData.keywords.length()));
				} else {
					addIndex(acsc, i, kw);
				}
			}
			for (String kw : KeywordsParser.splitAddressKeywords(poi.getGeoinfo())) {
				if (kw.startsWith(mPoisData.keywords)) {
					kw = kw.substring(mPoisData.keywords.length());
				}
				if (kw.endsWith(mPoisData.keywords)) {
					kw = kw.substring(0, kw.length() - mPoisData.keywords.length());
				}
				if (kw.isEmpty())
					continue;
				addIndex(acsc,i,kw);
			}
		}

		if (minDistanceIndex >= 0) {
			acsc.addIndex(minDistanceIndex, "最近那个");
		}
	}
	
	private void addIndex(AsrComplexSelectCallback acsc,int index,String keywords){
		if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
			acsc.addIndex(index, keywords);
		}
	}

	@Override
	protected boolean onWakeupItemSelect(boolean isWakeupResult, String type, String command) {
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
		return false;
	}

	private void sortListByComparator(String speech, Comparator<? super Poi> comparator) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
		if (!TextUtils.isEmpty(sortSpk)) {
			sortSpk = sortSpk.replace("%SORTSLOT%", speech);
		}
		mSpeechTaskId = TtsManager.getInstance()
				.speakVoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"), mVoiceUrl);
		Collections.sort(mPoisData.mPois, comparator);
		mPageHelper.onUpdate(mPoisData.mPois, mPageCount, mUseNewSelector);
	}

	@Override
	protected boolean onWakeupIndexSelect(boolean isWakeupResult, final List<Integer> indexs, final String command) {
		if (indexs.size() != 1) {
			String newCommand;
			if (command.endsWith("那个"))
				newCommand = command.substring(0, command.length() - 2);
			else {
				newCommand = command;
			}
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mHasPrices = false;
					List<Poi> pois = new ArrayList<Poi>();
					for (Integer idx : indexs) {
						if (idx < mPoisData.mPois.size()) {
							pois.add((Poi) mPoisData.mPois.get(idx));
							if (mPoisData.isBus && ((BusinessPoiDetail) mPoisData.mPois.get(idx)).getAvgPrice() > 0) {
								mHasPrices = true;
							}
						}
					}
					mPoisData.mPois = pois;
					mSourceList = mPoisData.mPois;
					mPageHelper.onUpdate(mPoisData.mPois, mPageCount, mUseNewSelector);
				}
			}, 0);
			String text = NativeData.getResString("RS_VOICE_MULTIPLE_SELECTOR").replace("%CMD%", String.valueOf(indexs.size()));
			String mLastHintText = newCommand + text;
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			mSpeechTaskId = TtsManager.getInstance().speakVoice(mLastHintText, mVoiceUrl);
			return true;
		}

		final int n = indexs.get(0);
		final String idxStr = command;
		onCommandSelect(mSourceList, n, idxStr);
		return true;
	}

	@Override
	public byte[] procInvoke(String packageName, String command, byte[] data) {
		if ("txz.record.ui.event.display.tip".equals(command)) {
			try {
				if (mDelayTask != null) {
					mDelayTask.clearProgress();
				}
				stopTtsAndAsr();

				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						SearchEditDialog.getInstance().setNeedCloseDialog(true);
						SearchEditDialog.getInstance().dismiss();
						if (mPoisData.mPois.size() < 1) {
							SearchEditDialog.getInstance().setNeedCloseDialog(false);
						}

						if (PoiAction.ACTION_NAVI.equals(mPoisData.action)) {
							SearchEditDialog.naviDefault(GlobalContext.get(), mPoisData.keywords);
						}
						if (PoiAction.ACTION_COMPANY.equals(mPoisData.action)) {
							SearchEditDialog.naviCompany(GlobalContext.get(), mPoisData.keywords);
						}
						if (PoiAction.ACTION_HOME.equals(mPoisData.action)) {
							SearchEditDialog.naviHome(GlobalContext.get(), mPoisData.keywords);
						}
					}
				}, 0);

				ReportUtil.doReport(
						new ReportUtil.Report.Builder().setAction("edit").setKeywords("导航地址").buildTouchReport());
			} catch (Exception e) {
			}
		}
		return super.procInvoke(packageName, command, data);
	}
}