package com.txznet.txz.component.selector;

import java.util.List;

import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.selector.ISelectControl.OnItemSelectListener;
import com.txznet.txz.component.selector.PoiSelectorControl.PoisData;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;

public class SelectorHelper {
	public static boolean sShowPoiMap;
	public static boolean sUseNewSelector;
	public static int sSearchCount = 8;

	static int sPageCount = 4;
	static long sDismissDelay = 0;
	static boolean sExitWithBack = false;

	static ISelectControl mSelectControl;

	public static void entryPoisSelector(List<Poi> pois, String keywords, boolean isBus, String action, String city) {
		PoisData pd = new PoisData();
		pd.action = action != null ? action : PoiAction.ACTION_NAVI;
		pd.isBus = isBus;
		pd.keywords = keywords != null ? keywords : "";
		pd.city = city != null ? city : "";
		pd.mPois = pois;

		if (mSelectControl == null || !(mSelectControl instanceof PoiSelectorControl)) {
			mSelectControl = new PoiSelectorControl(sPageCount);
		}

		prepareSet(mSelectControl);
		((PoiSelectorControl) mSelectControl).showPoiSelectList(pd);
	}

	public static void entryMusicSelector(List<AudioShowData> asds, OnItemSelectListener listener) {
		if (mSelectControl == null || !(mSelectControl instanceof MusicSelectorControl)) {
			mSelectControl = new MusicSelectorControl(sPageCount);
		}

		if (listener != null) {
			mSelectControl.setOnItemSelectListener(listener);
		} else {
			mSelectControl.setOnItemSelectListener(null);
		}

		prepareSet(mSelectControl);
		((MusicSelectorControl) mSelectControl).showMusicSelects(asds);
	}

	public static void entryWxContactSelector(int event, WeChatContacts cons, String spkTxt) {
		if (mSelectControl == null || !(mSelectControl instanceof WxSelectorControl)) {
			mSelectControl = new WxSelectorControl(sPageCount);
		}
		prepareSet(mSelectControl);
		((WxSelectorControl) mSelectControl).showWxContactsList(event, cons, spkTxt);
	}

	private static void prepareSet(ISelectControl isc) {
		isc.mExitWithBack = sExitWithBack;
		if (!RecordInvokeFactory.hasThirdImpl()) {
			isc.mUseNewSelector = true;
		} else {
			isc.mUseNewSelector = sUseNewSelector;
		}
	}

	public static void clearIsSelecting() {
		if (mSelectControl != null) {
			mSelectControl.clearIsSelecting();
		}
	}

	public static void selectCancel() {
		if (mSelectControl != null) {
			mSelectControl.selectCancel(false);
		}
	}

	public static void backAsrWithCancel() {
		if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
			PoisData pds = ((PoiSelectorControl) mSelectControl).mPoisData;
			if (pds == null || pds.mPois == null || pds.mPois.size() < 1) {
				mSelectControl.backAsrWithCancel(ISelectControl.ASR_CANCEL_BACK_HINT);
			}
		}
	}

	public static void selectAgain() {
		if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
			mSelectControl.selectAgain();
		}
	}

	public static void removeDismissTask() {
		if (mSelectControl != null && mSelectControl.isSelecting()) {
			mSelectControl.removeDismissTask();
		}
	}

	public static void updateAutoDismissDelay(long delay) {
		JNIHelper.logd("dismissDelay:"+delay);
		sDismissDelay = delay;
	}

	public static void selectSure() {
		if (mSelectControl != null) {
			mSelectControl.selectSure(false);
		}
	}

	public static void onResumeDelayTask() {
		if (mSelectControl != null && mSelectControl.isSelecting()) {
			mSelectControl.checkDismissTask();
		}
	}

	private static void onItemSelect(byte[] data) {
		try {
			JSONBuilder json = new JSONBuilder(data);
			int index = json.getVal("index", Integer.class);
			JNIHelper.logd("item.selected:" + index);
			if (mSelectControl != null && !(mSelectControl instanceof PoiSelectorControl)
					|| !sShowPoiMap) {
				if (sUseNewSelector) {
					mSelectControl.selectIndexFromPage(index, null);
				} else {
					mSelectControl.selectIndexFromAll(index, null);
				}
				String type = "navi";
				if (mSelectControl instanceof WxSelectorControl) {
					type = "wechat";
				} else if (mSelectControl instanceof MusicSelectorControl) {
					type = "music";
				}
				ReportUtil.doReport(new ReportUtil.Report.Builder().setType(type).setAction("select")
						.putExtra("index", index).buildTouchReport());
				return;
			}

			if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
				((PoiSelectorControl) mSelectControl).OnItemClick(index);
				// 上报数据
				ReportUtil.doReport(new ReportUtil.Report.Builder().setType("navi").setAction("view")
						.putExtra("index", index).buildTouchReport());
			}
		} catch (Exception e) {
		}
	}

	public static byte[] procInvoke(String packageName, String command, byte[] data) {
		if ("txz.record.ui.event.display.count".equals(command)) {
			try {
				sPageCount = Integer.parseInt(new String(data));
				if (mSelectControl != null) {
					int show = mSelectControl.mPageCount;
					if (sPageCount != show) {
						mSelectControl.updatePageCount(sPageCount);
					}
				}
				JNIHelper.logd("txz.record.ui.event.display.count:" + sPageCount);
			} catch (Exception e) {
			}
		}

		if ("txz.record.ui.event.item.right".equals(command)) {
			try {
				int pos = Integer.parseInt(new String(data));
				if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
					((PoiSelectorControl) mSelectControl).onRightItemBtnPf(pos);
					// 上报数据
					ReportUtil.doReport(new ReportUtil.Report.Builder().setType("navi").setAction("select")
							.putExtra("index", pos).buildTouchReport());
				}
			} catch (Exception e) {
			}
		}

		if ("txz.record.ui.event.item.selected".equals(command)) {
			onItemSelect(data);
		}

		if ("txz.record.ui.event.display.tip".equals(command)) {
			if (mSelectControl != null) {
				mSelectControl.procInvoke(packageName, command, data);
				if (mSelectControl instanceof PoiSelectorControl) {
					// 上报数据
					ReportUtil.doReport(
							new ReportUtil.Report.Builder().setType("navi").setAction("edit").buildTouchReport());
				}

				removeDismissTask();
			}
		}
		if ("txz.record.ui.event.list.ontouch".equals(command)) {
			if (mSelectControl != null) {
				mSelectControl.procInvoke(packageName, command, data);
			}
		}

		if ("txz.selector.exitBack".equals(command)) {
			try {
				sExitWithBack = Boolean.parseBoolean(new String(data));
				JNIHelper.logd("mExitWithBack:" + sExitWithBack);
				if (mSelectControl != null) {
					mSelectControl.mExitWithBack = sExitWithBack;
				}
			} catch (Exception e) {
			}
		}

		if ("txz.selector.show.count".equals(command)) {
			try {
				sSearchCount = Integer.parseInt(new String(data));
				JNIHelper.logd("txz.selector.show.count" + sSearchCount);
			} catch (Exception e) {
			}
		}
		if ("txz.selector.useNewSelector".equals(command)) {
			try {
				sUseNewSelector = Boolean.parseBoolean(new String(data));
				JNIHelper.logd("sUseNewSelector:" + sUseNewSelector);
			} catch (Exception e) {
			}
		}
		if ("txz.record.ui.event.display.page".equals(command)) {
			try {
				JSONBuilder jb = new JSONBuilder(data);
				int type = jb.getVal("type", Integer.class);
				int clickType = jb.getVal("clicktype", Integer.class);
				if (type == 1) {
					if (mSelectControl != null) {
						PageHelper ph = mSelectControl.mPageHelper;
						if (clickType == 1) {
							ph.prevPager();
						} else if (clickType == 2) {
							ph.nextPager();
						}
					}
				}
				onResumeDelayTask();
			} catch (Exception e) {
			}
		}

		if ("txz.selector.poi.onItemNaviClick".equals(command)) {
			onOldVersionItemSelect(data);
		}

		if ("txz.selector.poi.onItemClick".equals(command)) {
			onOldVersionItemSelect(data);
		}

		if ("txz.selector.poi.edit".equals(command)) {
			if (mSelectControl != null) {
				mSelectControl.procInvoke(packageName, command, data);
				if (mSelectControl instanceof PoiSelectorControl) {
					// 上报数据
					ReportUtil.doReport(
							new ReportUtil.Report.Builder().setType("navi").setAction("edit").buildTouchReport());
				}

				removeDismissTask();
			}
		}

		if ("txz.selector.audio.selectIndex".equals(command)) {
			onItemSelect(data);
		}

		return null;
	}

	private static void onOldVersionItemSelect(byte[] data) {
		try {
			JSONBuilder json = new JSONBuilder(data);
			Integer pos = json.getVal("position", Integer.class);
			if (pos != null) {
				if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
					((PoiSelectorControl) mSelectControl).OnItemClick(pos);
					// 上报数据
					ReportUtil.doReport(new ReportUtil.Report.Builder().setType("navi").setAction("view")
							.putExtra("index", pos).buildTouchReport());
				}
				return;
			}

			String poiJson = json.getVal("poi", String.class);
			String action = json.getVal("action", String.class);

			if (mSelectControl != null && mSelectControl instanceof PoiSelectorControl) {
				PoiSelectorControl psc = (PoiSelectorControl) mSelectControl;
				psc.selectPoi(Poi.fromString(poiJson));
			}

		} catch (Exception e) {
		}
	}
}
