package com.txznet.record.poi;


public class PoiViewHelper {
	// private static final String ASR_TASK_ID = "RECORD_POI_SELECTOR";
	//
	// private static PoiViewHelper instance;
	//
	// private PoiContentView mView;
	//
	// private PoiViewHelper() {
	// TXZQRCodeManager.getInstance().registGetQRListener(mOnGetListener);
	// }
	//
	// public static PoiViewHelper getInstance() {
	// if (instance == null) {
	// synchronized (PoiViewHelper.class) {
	// if (instance == null) {
	// instance = new PoiViewHelper();
	// }
	// }
	// }
	// return instance;
	// }
	//
	// public void useHelper(List<Poi> pois, boolean isBusiness, String keyWord,
	// PoiContentView pcv) {
	// this.mListPoi = pois;
	// this.mIsBus = isBusiness;
	// this.mKeywords = keyWord;
	// this.mView = pcv;
	//
	// startEntry();
	// }
	//
	// private void startEntry() {
	// // 解析数据
	// mView.updatePoiSearchResultList(mListPoi, mIsBus, mKeywords);
	// CountMode cm = parseResultCountMode();
	// if (cm == CountMode.NONE) {
	// getWxBindState();
	// }
	//
	// // 声控播报处理
	// useTtsWakeup();
	// }
	//
	// public void releaseSource() {
	// this.mListPoi.clear();
	// this.mKeywords = null;
	// this.mView = null;
	// TXZQRCodeManager.getInstance().unRegistListener(mOnGetListener);
	// }
	//
	// OnGetQRListener mOnGetListener = new OnGetQRListener() {
	//
	// @Override
	// public void onGetQR(String url, boolean isBind) {
	// if (mView != null) {
	// mView.refreshCodeLayout(url, isBind);
	// }
	// }
	// };
	//
	// private void getWxBindState() {
	// TXZQRCodeManager.getInstance().getQRCodeFromTXZ();
	// }
	//
	// public CountMode parseResultCountMode() {
	// if (mListPoi == null) {
	// return CountMode.NONE;
	// }
	//
	// if (mListPoi.size() == 1) {
	// return CountMode.ONCE;
	// }
	//
	// return CountMode.MORE;
	// }
	//
	// private List<Poi> mListPoi;
	// private boolean mIsBus;
	// private String mKeywords;
	//
	// private boolean mCanAutoPerformNavi = false;
	// private String mLastHintText = "";
	// private int mSpeakSearchResultTaskId = -1;
	//
	// private static final String COMMON_HINT = "找到如下结果，请说第几个选择，或取消";
	// private static final String NO_RESULT_HINT =
	// "没有找到C信息，您可以扫描微信二维码绑定设备直接发送地址到设备";
	// private static final String ONE_RESULT_HINT = "找到一个结果，即将开始导航，确定还是取消";
	//
	// public void useTtsWakeup() {
	// String text = COMMON_HINT;// "RS_VOICE_NAV_SELECT"
	// CountMode cm = parseResultCountMode();
	// if (cm == CountMode.NONE) {
	// text = NO_RESULT_HINT.replace("C", mKeywords);
	// } else if (cm == CountMode.ONCE) {
	// text = ONE_RESULT_HINT;
	// mCanAutoPerformNavi = true;
	// }
	// mLastHintText = text;
	// if (mListPoi.size() == 1)
	// mLastHintText = "找到一个结果，确定还是取消";
	//
	// MyApplication.getApp().runOnUiGround(new Runnable1<String>(text) {
	// @Override
	// public void run() {
	// mSpeakSearchResultTaskId = TtsUtil.speakText(mP1,
	// new ITtsCallback() {
	// @Override
	// public void onEnd() {
	// // mIsInitSpeakEnd = true;
	// // mRunnableSelectAgain.run();
	// }
	//
	// @Override
	// public void onSuccess() {
	// // mIsInitSpeakEnd = true;
	// // if (mListPoi.size() == 1) {
	// // if (mAutoSelectProgress != null) {
	// // mProgressBeginTime = System
	// // .currentTimeMillis();
	// // if (mCanAutoPerformNavi)
	// // mAutoSelectProgress
	// // .resumeProgress();
	// // }
	// // }
	// if (parseResultCountMode() == CountMode.ONCE) {
	// mView.startAutoProgress();
	// }
	//
	// useWakeup();
	// }
	// });
	// }
	// }, 0);
	// }
	//
	// Runnable mRunnableSelectAgain = new Runnable() {
	//
	// @Override
	// public void run() {
	// useWakeup();
	// }
	// };
	//
	// private void useWakeup() {
	// AsrComplexSelectCallback wakeupAsr = new AsrComplexSelectCallback() {
	// @Override
	// public String getTaskId() {
	// return ASR_TASK_ID;
	// }
	//
	// @Override
	// public boolean needAsrState() {
	// return true;
	// }
	//
	// @Override
	// public void onVolume(int volume) {
	// LogUtil.logd(">>>>>onVolume:" + volume);
	// mView.onAsrVolume(volume);
	// }
	//
	// public void onCommandSelected(String type, String command) {
	// LogUtil.logd(">>>>>onCommandSelected:" + command);
	// if (mListPoi.size() == 1)
	// mLastHintText = "找到一个结果，确定还是取消";
	// else
	// mLastHintText = "请说第几个选择或取消";
	//
	// if ("CANCEL".equals(type)) {
	// TtsUtil.speakText("操作已取消");
	// WinRecord.getInstance().dismiss();
	// return;
	// }
	// if ("SORT_DISTANCE".equals(type)) {
	// // AsrManager.getInstance().mSenceRepeateCount = -1;
	// // AsrManager.getInstance().setNeedCloseRecord(true);
	// // mSpeakSearchResultTaskId =
	// // RecorderWin.speakTextWithClose(
	// // "已按" + command + "，请说第几个选择或取消",
	// // mRunnableSelectAgain);
	// // TXZApp.getApp().runOnUiGround(new Runnable() {
	// // @Override
	// // public void run() {
	// // Collections.sort(mListPoi,
	// // new PoiComparator_Distance());
	// // refreshListView(true, false);
	// // }
	// // }, 0);
	// mSpeakSearchResultTaskId = TtsUtil.speakText("已按" + command
	// + "，请说第几个选择或取消", new ITtsCallback() {
	// @Override
	// public void onSuccess() {
	// mRunnableSelectAgain.run();
	// }
	// });
	// MyApplication.getApp().runOnUiGround(new Runnable() {
	//
	// @Override
	// public void run() {
	// Collections.sort(mListPoi,
	// new PoiComparator_Distance());
	// mView.updatePoiSearchResultList(mListPoi, mIsBus,
	// mKeywords);
	// }
	// }, 0);
	// return;
	// }
	// if ("SORT_PRICE".equals(type)) {
	// // AsrManager.getInstance().mSenceRepeateCount = -1;
	// // AsrManager.getInstance().setNeedCloseRecord(true);
	// // mSpeakSearchResultTaskId =
	// // RecorderWin.speakTextWithClose(
	// // "已按" + command + "，请说第几个选择或取消",
	// // mRunnableSelectAgain);
	// // TXZApp.getApp().runOnUiGround(new Runnable() {
	// // @Override
	// // public void run() {
	// // Collections.sort(mListPoi,
	// // new PoiComparator_Price());
	// // refreshListView(true, false);
	// // }
	// // }, 0);
	// mSpeakSearchResultTaskId = TtsUtil.speakText("已按" + command
	// + "，请说第几个选择或取消", new ITtsCallback() {
	// @Override
	// public void onSuccess() {
	// mRunnableSelectAgain.run();
	// }
	// });
	// MyApplication.getApp().runOnUiGround(new Runnable() {
	//
	// @Override
	// public void run() {
	// Collections.sort(mListPoi,
	// new PoiComparator_Price());
	// mView.updatePoiSearchResultList(mListPoi, mIsBus,
	// mKeywords);
	// }
	// }, 0);
	// return;
	// }
	// if ("SORT_SCORE".equals(type)) {
	// // AsrManager.getInstance().mSenceRepeateCount = -1;
	// // AsrManager.getInstance().setNeedCloseRecord(true);
	// // mSpeakSearchResultTaskId =
	// // RecorderWin.speakTextWithClose(
	// // "已按" + command + "，请说第几个选择或取消",
	// // mRunnableSelectAgain);
	// // TXZApp.getApp().runOnUiGround(new Runnable() {
	// // @Override
	// // public void run() {
	// // Collections.sort(mListPoi,
	// // new PoiComparator_Score());
	// // refreshListView(true, false);
	// // }
	// // }, 0);
	// mSpeakSearchResultTaskId = TtsUtil.speakText("已按" + command
	// + "，请说第几个选择或取消", new ITtsCallback() {
	// @Override
	// public void onSuccess() {
	// mRunnableSelectAgain.run();
	// }
	// });
	// MyApplication.getApp().runOnUiGround(new Runnable() {
	//
	// @Override
	// public void run() {
	// Collections.sort(mListPoi,
	// new PoiComparator_Score());
	// mView.updatePoiSearchResultList(mListPoi, mIsBus,
	// mKeywords);
	// }
	// }, 0);
	// return;
	// }
	// if ("SURE".equals(type)) {
	// WinRecord.getInstance().dismiss();
	// mView.autoRunnable();
	// }
	// }
	//
	// public void onIndexSelected(final List<Integer> indexs,
	// String command) {
	// LogUtil.logd(">>>>>>>command:" + command);
	// if (indexs.size() != 1) {
	// if (command.endsWith("那个"))
	// command = command.substring(0, command.length() - 2);
	// MyApplication.getApp().runOnUiGround(new Runnable() {
	// @Override
	// public void run() {
	// List<Poi> pois = new ArrayList<Poi>();
	// for (Integer idx : indexs) {
	// if (idx < mListPoi.size())
	// pois.add(mListPoi.get(idx));
	// }
	// mListPoi = pois;
	// mView.updatePoiSearchResultList(mListPoi, mIsBus,
	// mKeywords);
	// }
	// }, 0);
	// mLastHintText = command + "有" + indexs.size() + "个结果，请重新选择";
	// mSpeakSearchResultTaskId = TtsUtil.speakText(mLastHintText,
	// new ITtsCallback() {
	//
	// @Override
	// public void onSuccess() {
	// mRunnableSelectAgain.run();
	// };
	// });
	// return;
	// }
	// final int n = indexs.get(0);
	// final String idxStr = command;
	// MyApplication.getApp().runOnBackGround(new Runnable() {
	// @Override
	// public void run() {
	// if (n < 0 || n >= mListPoi.size())
	// return;
	// // WakeupManager.getInstance().recoverWakeupFromAsr(
	// // WAKEUP_TASK_ID);
	// TXZAsrManager.getInstance().recoverWakeupFromAsr(
	// ASR_TASK_ID);
	// String spkTxt = "好的，将为您规划" + idxStr + "地址路线";
	// // if (!ACTION_NAV.equals(mAction)) {
	// // if (ACTION_SET_COMPANY.equals(mAction)) {
	// // spkTxt = "好的，将为您设置" + idxStr + "唯公司的地址并开始规划路线";
	// // } else if (ACTION_SET_HOME.equals(mAction)) {
	// // spkTxt = "好的，将为您设置" + idxStr + "唯家的地址并开始规划路线";
	// // }
	// // }
	//
	// // AsrManager.getInstance().setNeedCloseRecord(true);
	// // RecorderWin.speakTextWithClose(spkTxt, new Runnable()
	// // {
	// // @Override
	// // public void run() {
	// // TXZApp.getApp().runOnUiGround(new Runnable() {
	// // @Override
	// // public void run() {
	// // mIntIndex = n;
	// // if (mIntIndex < 0
	// // || mIntIndex >= mListPoi.size())
	// // return;
	// // mTxtName.setText(mListPoi
	// // .get(mIntIndex).getName());
	// // mTxtDes.setText(mListPoi.get(mIntIndex)
	// // .getGeoinfo());
	// // mTxtNamePoi.setText(mListPoi.get(
	// // mIntIndex).getName());
	// // mTxtDesPoi.setText(mListPoi.get(
	// // mIntIndex).getGeoinfo());
	// // mBtnStartNav.performClick();
	// // }
	// //
	// // }, 0);
	// // }
	// // });
	// mSpeakSearchResultTaskId = TtsUtil.speakText(spkTxt,
	// new ITtsCallback() {
	// @Override
	// public void onSuccess() {
	// if (n < 0 || n >= mListPoi.size())
	// return;
	// Poi poi = mListPoi.get(n);
	// TXZNavManager.getInstance().navToLoc(
	// poi);
	// };
	// });
	// }
	// }, 0);
	//
	// };
	// }.addCommand("CANCEL", "取消", "返回", "放弃");
	//
	// if (mListPoi.size() > 1) {
	// wakeupAsr.addCommand("SORT_DISTANCE", "距离排序", "远近排序");
	// if (mIsBus)
	// wakeupAsr.addCommand("SORT_SCORE", "分数排序", "评分排序", "评价排序")
	// .addCommand("SORT_PRICE", "价钱排序", "价格排序", "消费排序");
	// }
	//
	// if (mListPoi.size() == 1) {
	// wakeupAsr.addCommand("SURE", "确定", "好的", "开始");
	// }
	//
	// // LocationInfo myLocation = LocationManager.getInstance()
	// // .getLastLocation();
	//
	// int minDistance = -1;
	// int minDistanceIndex = -1;
	//
	// // if (myLocation == null) {
	// // return;
	// // }
	//
	// for (int i = 0; i < mListPoi.size(); ++i) {
	// // wakeupAsr.addIndex(i,
	// // "第" + NativeData.getResString("RS_VOICE_DIGITS", i + 1)
	// // + "个");
	// wakeupAsr.addIndex(i, "第" + (i + 1) + "个");
	//
	// Poi poi = mListPoi.get(i);
	// if (mIsBus) {
	// BusinessPoiDetail businessPoi = null;
	// if (poi instanceof BusinessPoiDetail) {
	// businessPoi = (BusinessPoiDetail) poi;
	// } else {
	// LogUtil.loge("!!!!!!!!!!BusinessPoiDetail CastException!");
	// }
	// // 分店
	// wakeupAsr.addIndex(i, businessPoi.getBranchName());
	// // 分类
	// String[] categories = businessPoi.getCategories();
	// if (categories != null) {
	// for (String c : categories) {
	// if (c.endsWith("菜馆")) {
	// wakeupAsr.addIndex(i,
	// c.substring(0, c.length() - 1));
	// } else {
	// wakeupAsr.addIndex(i, c);
	// }
	// }
	// }
	// // 区域
	// String[] regions = businessPoi.getRegions();
	// if (categories != null) {
	// for (String c : regions) {
	// wakeupAsr.addIndex(i, c);
	// }
	// }
	// // 团购优惠其他
	// if (businessPoi.isHasDeal())
	// wakeupAsr.addIndex(i, "有团购");
	// if (businessPoi.isHasCoupon())
	// wakeupAsr.addIndex(i, "有优惠");
	// if (businessPoi.isHasPark())
	// wakeupAsr.addIndex(i, "有停车场");
	// if (businessPoi.isHasWifi())
	// wakeupAsr.addIndex(i, "有WIFI");
	// if (!TextUtils.isEmpty(businessPoi.getTelephone()))
	// wakeupAsr.addIndex(i, "有电话");
	// }
	//
	// int d = mListPoi.get(i).getDistance();
	// if (minDistance < 0 || d < minDistance) {
	// minDistanceIndex = i;
	// minDistance = d;
	// }
	//
	// for (String kw : KeywordsParser.splitKeywords(poi.getName())) {
	// if (mKeywords.equals(kw))
	// continue;
	// if (kw.startsWith(mKeywords)) {
	// wakeupAsr.addIndex(i, kw.substring(mKeywords.length()));
	// } else if (kw.endsWith(mKeywords)) {
	// wakeupAsr.addIndex(i,
	// kw.substring(0, kw.length() - mKeywords.length()));
	// } else {
	// wakeupAsr.addIndex(i, kw);
	// }
	// // if (poi.getName().startsWith(kw))
	// // wakeupAsr.addIndex(i, kw);
	// // else
	// // wakeupAsr.addIndex(i, kw + "那个");
	// }
	// for (String kw : KeywordsParser.splitAddressKeywords(poi
	// .getGeoinfo())) {
	// if (kw.startsWith(mKeywords)) {
	// kw = kw.substring(mKeywords.length());
	// }
	// if (kw.endsWith(mKeywords)) {
	// kw = kw.substring(0, kw.length() - mKeywords.length());
	// }
	// if (kw.isEmpty())
	// continue;
	// wakeupAsr.addIndex(i, kw);
	// }
	// }
	//
	// if (minDistanceIndex >= 0)
	// wakeupAsr.addIndex(minDistanceIndex, "最近那个");
	//
	// if (mListPoi.size() == 2) {
	// wakeupAsr.addIndex(0, "上面那个", "前面那个");
	// wakeupAsr.addIndex(1, "下面那个", "后面那个");
	// } else if (mListPoi.size() > 2) {
	// wakeupAsr.addIndex(0, "最上面那个", "最前面那个");
	// wakeupAsr.addIndex(mListPoi.size() - 1, "最下面那个", "最后面那个", "最后一个");
	// }
	//
	// TXZAsrManager.getInstance().useWakeupAsAsr(wakeupAsr);
	// }
}