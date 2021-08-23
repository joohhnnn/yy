package com.txznet.txz.component.poi.txz;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.util.KeywordsParser;

public class ChaosPoiSearchTXZImpl implements TXZPoiSearchManager.PoiSearchTool {
	public static enum SortType {
		DEFAULT, // 默认排序，按添加的tool结果依次排序
		DISTANCE, // 距离混排
		SCORE, // 评分
		PRICE, // 价格
	}

	private class ToolRecord {
		public ToolRecord(TXZPoiSearchManager.PoiSearchTool t, boolean o) {
			tool = t;
			optional = o;
		}

		public TXZPoiSearchManager.PoiSearchTool tool;
		public boolean optional;
		public boolean complete = false;
		public SearchReq searchReq = null;
		List<Poi> result = null;
		int err = TXZPoiSearchManager.ERROR_CODE_EMPTY;

		PoiSearchResultListener listener = new PoiSearchResultListener() {
			@Override
			public void onSuggestion(SearchPoiSuggestion suggestion) {
				onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			}

			@Override
			public void onResult(final List<Poi> result) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						if (mEnd)
							return;
						ToolRecord.this.result = result;
						ToolRecord.this.complete = true;
						if (ToolRecord.this.optional == false) {
							ChaosPoiSearchTXZImpl.this.mNeedCompleteCount--;
						}
						mHasResult = true;
						ChaosPoiSearchTXZImpl.this.comboResult();
					}
				}, 0);
			}

			@Override
			public void onError(final int errCode, String errDesc) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						if (mEnd)
							return;
						ToolRecord.this.err = errCode;
						ToolRecord.this.complete = true;
						if (ToolRecord.this.optional == false) {
							ChaosPoiSearchTXZImpl.this.mNeedCompleteCount--;
						}

						ChaosPoiSearchTXZImpl.this.comboResult();
					}
				}, 0);
			}
		};
	}

	private boolean mIsBussiness = false;
	private SortType mSortType = SortType.DEFAULT;
	private int mNeedCompleteCount = 0;
	private boolean mHasResult = false;
	private boolean mEnd = false;
	private List<ToolRecord> mToolList = new ArrayList<ToolRecord>();
	private PoiSearchResultListener mPoiSearchResultListener = null;
	CityPoiSearchOption mOption = null;

	public ChaosPoiSearchTXZImpl(boolean isBusiness) {
		this(isBusiness, SortType.DEFAULT);
	}

	public ChaosPoiSearchTXZImpl(boolean isBusiness, SortType sortType) {
		mIsBussiness = isBusiness;
		mSortType = sortType;
	}

	// 增加搜索工具，是否可选，可选时不需要等结果返回，先加的tool结果优先
	public ChaosPoiSearchTXZImpl addPoiSearchTool(
			TXZPoiSearchManager.PoiSearchTool tool, boolean optional) {
		mToolList.add(new ToolRecord(tool, optional));
		if (optional == false) {
			mNeedCompleteCount++;
		}
		return this;
	}

	List<Poi> mRetResults = new ArrayList<Poi>();

	SearchReq mAllSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			mEnd = true;
			for (ToolRecord tool : mToolList) {
				tool.searchReq.cancel();
			}
		}
	};

	private Poi converPoi(Poi in) {
		if (mIsBussiness) {
			return BusinessPoiDetail.fromString(in.toString());
		} else if (in instanceof BusinessPoiDetail) {
			return Poi.fromString(in.toString());
		}
		return in;
	}

	private boolean checkSameName(String s1, String s2) {
		if (s1.equals(s2))
			return true;
		Set<String> ss1 = KeywordsParser.splitKeywords(s1);
		Set<String> ss2 = KeywordsParser.splitKeywords(s2);
		Set<String> min, max;
		if (ss1.size() > ss2.size()) {
			min = ss2;
			max = ss1;
		} else {
			min = ss1;
			max = ss2;
		}
		int count = 0;
		for (String s : min) {
			if (max.contains(s)) {
				count++;
			} else {
				for (String t : max) {
					if (t.startsWith(s)
							|| (s.startsWith(t) && max.contains(s.substring(t
									.length())))) {
						count++;
						break;
					}
				}
			}
		}
		// 超过包含1半以上的关键字
		return count > min.size() / 2;
	}

	private boolean checkSamePoi(Poi p1, Poi p2) {
		try {
			double d = BDLocationUtil.calDistance(p1.getLat(), p1.getLng(),
					p2.getLat(), p2.getLng());
			if (d > 100) {
				return false;
			}
			return checkSameName(p1.getName(), p2.getName());
		} catch (Exception e) {
			return false;
		}
	}

	private void comboResult() {
		if (mEnd == true)
			return;

		if (mHasResult && ChaosPoiSearchTXZImpl.this.mNeedCompleteCount <= 0) {
			mEnd = true;
		}

		int endMark = 1;
		int maxError = TXZPoiSearchManager.ERROR_CODE_EMPTY;
		for (ToolRecord tool : mToolList) {
			endMark &= (tool.complete == false ? 0 : 1);
			if (mEnd == false && tool.complete == false) {
				// 整体还没有结束，遇到未完成的任务则先退掉循环
				break;
			}
			if (maxError == TXZPoiSearchManager.ERROR_CODE_EMPTY) {
				maxError = tool.err;
			} else if (maxError == TXZPoiSearchManager.ERROR_CODE_UNKNOW) {
				if (tool.err == TXZPoiSearchManager.ERROR_CODE_TIMEOUT) {
					maxError = TXZPoiSearchManager.ERROR_CODE_TIMEOUT;
				}
			}
			if (tool.result != null) {
				for (Poi p : tool.result) {
					// 去重
					boolean bSame = false;
					for (Poi r : mRetResults) {
						if (checkSamePoi(r, p)) {
							bSame = true;
							JNIHelper.logw("txz poi search skip same poi: \n"
									+ r.toString() + "\n" + p.toString());
							break;
						}
					}
					if (bSame) {
						continue;
					}
					// 排序
					int index = 0;
					switch (mSortType) {
					case DISTANCE:
						for (; index < mRetResults.size(); ++index) {
							if (mRetResults.get(index).getDistance() > p
									.getDistance()) {
								break;
							}
						}
						break;
					case PRICE:
						if (!(p instanceof BusinessPoiDetail)) {
							index = mRetResults.size();
							break;
						}
						for (; index < mRetResults.size(); ++index) {
							if (!(mRetResults.get(index) instanceof BusinessPoiDetail)) {
								break;
							}
							if (((BusinessPoiDetail) mRetResults.get(index))
									.getAvgPrice() > ((BusinessPoiDetail) p)
									.getAvgPrice()) {
								break;
							}
						}
						break;
					case SCORE:
						if (!(p instanceof BusinessPoiDetail)) {
							index = mRetResults.size();
							break;
						}
						for (; index < mRetResults.size(); ++index) {
							if (!(mRetResults.get(index) instanceof BusinessPoiDetail)) {
								break;
							}
							if (((BusinessPoiDetail) mRetResults.get(index))
									.getScore() < ((BusinessPoiDetail) p)
									.getScore()) {
								break;
							}
						}
						break;
					case DEFAULT:
					default:
						index = mRetResults.size();
						break;
					}
					mRetResults.add(index, converPoi(p));
				}
				tool.result = null;
			}
		}
		// 没有搜到结果，所有工具已经搜索完
		if (endMark == 1) {
			mEnd = true;
		}

		if (mRetResults.isEmpty()) {
			if (mEnd) {
				mPoiSearchResultListener.onError(maxError, "");
			}
		} else {
			// 数量已经够了
			if (mRetResults.size() >= mOption.getNum()) {
				mRetResults = mRetResults.subList(0, mOption.getNum() - 1);
				mEnd = true;
			}
			if (mEnd) {
				mPoiSearchResultListener.onResult(mRetResults);
			}
		}
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		mOption = option;
		mPoiSearchResultListener = listener;
		mRetResults.clear();
		for (final ToolRecord tool : mToolList) {
			JNIHelper.logd("txz poi search [" + option.getKeywords()
					+ "] in city [" + option.getCity() + "] with tool"
					+ tool.getClass().toString());
			tool.searchReq = tool.tool.searchInCity(option, tool.listener);
		}
		return mAllSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		mOption = option;
		mPoiSearchResultListener = listener;
		mRetResults.clear();
		for (final ToolRecord tool : mToolList) {
			JNIHelper.logd("txz poi search [" + option.getKeywords()
					+ "] in nearby [" + option.getCity() + "] with tool"
					+ tool.getClass().toString());
			tool.searchReq = tool.tool.searchNearby(option, tool.listener);
		}
		return this.mAllSearchReq;
	}

}
