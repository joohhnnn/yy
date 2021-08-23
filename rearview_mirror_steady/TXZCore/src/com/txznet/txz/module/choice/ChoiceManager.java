package com.txznet.txz.module.choice;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSONArray;
import com.txz.equipment_manager.EquipmentManager.TTSTheme_Info;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.map.UiMap.NavInfo;
import com.txz.ui.map.UiMap.NavPointInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.theme.ThemeStyle;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ScreenUtil.ListOptionHook;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.bean.PoiMsg;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.audio.AudioSelector.AudioSelectorListener;
import com.txznet.txz.component.audio.AudioSelector.Music;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.component.choice.ListHook;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.list.AbsWorkChoice;
import com.txznet.txz.component.choice.list.AbstractChoice;
import com.txznet.txz.component.choice.list.AsyncWorkChoice;
import com.txznet.txz.component.choice.list.CallWorkChoice;
import com.txznet.txz.component.choice.list.CallWorkChoice.Contacts;
import com.txznet.txz.component.choice.list.CarControlHomeWorkChoice;
import com.txznet.txz.component.choice.list.CommWorkChoice;
import com.txznet.txz.component.choice.list.CompetitionWorkChoice;
import com.txznet.txz.component.choice.list.FilmWorkChoice;
import com.txznet.txz.component.choice.list.FlightTicketWorkChioce;
import com.txznet.txz.component.choice.list.FlightWorkChoice;
import com.txznet.txz.component.choice.list.FlightWorkChoice.FlightDataBean;
import com.txznet.txz.component.choice.list.MovieTheaterWorkChoice;
import com.txznet.txz.component.choice.list.MovieTimeWorkChoice;
import com.txznet.txz.component.choice.list.MovieWorkChoice;
import com.txznet.txz.component.choice.list.MusicWorkChoice;
import com.txznet.txz.component.choice.list.MusicWorkChoice.MusicData;
import com.txznet.txz.component.choice.list.PluginWorkChoice;
import com.txznet.txz.component.choice.list.PoiWorkChoice;
import com.txznet.txz.component.choice.list.PoiWorkChoice.PoisData;
import com.txznet.txz.component.choice.list.QiWuFlightTicketPayWorkChioce;
import com.txznet.txz.component.choice.list.ReminderWorkChoice;
import com.txznet.txz.component.choice.list.ReminderWorkChoice.ReminderItem;
import com.txznet.txz.component.choice.list.StyleWorkChoice;
import com.txznet.txz.component.choice.list.TrainTicketWorkChoice;
import com.txznet.txz.component.choice.list.TrainWorkChoice;
import com.txznet.txz.component.choice.list.TtsWorkChoice;
import com.txznet.txz.component.choice.list.WorkChoice;
import com.txznet.txz.component.choice.list.WxWorkChoice;
import com.txznet.txz.component.choice.list.WxWorkChoice.WxData;
import com.txznet.txz.component.choice.option.AsyncRepoCompentOption;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.option.CompentOption.ChoiceCallback;
import com.txznet.txz.component.choice.option.ListHookCompentOption;
import com.txznet.txz.component.choice.repo.RepoNavInscriber;
import com.txznet.txz.component.film.MoviePhoneNumQRControl;
import com.txznet.txz.component.film.MovieSeatPlanControl;
import com.txznet.txz.component.film.MovieWaitingPayQRControl;
import com.txznet.txz.component.home.AuthorizationViewManager;
import com.txznet.txz.component.selector.IPluginSelectorControl;
import com.txznet.txz.component.selector.ISelectControl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.music.bean.AudioShowData;
import com.txznet.txz.module.nav.NavInscriber;
import com.txznet.txz.module.nav.NavInscriber.DbNavInfo;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.nav.tool.NavAppManager.NavAppBean;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.ticket.TrainTicketData;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.ui.parse.TTSNoResultParse;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.widget.TicketUseInfoDialog;
import com.txznet.txz.ui.win.help.WinHelpDetailSelector;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.QuickClickUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;

public class ChoiceManager {
	// 界面动态计算的显示数量
	private Integer mAutoFixNumber;
	// 用户配置的信息
	private CompentOption<?> mUserConfigOption = null;

	private static ChoiceManager sManager = null;

	public static ChoiceManager getInstance() {
		if (sManager == null) {
			synchronized (ChoiceManager.class) {
				if (sManager == null) {
					sManager = new ChoiceManager();
				}
			}
		}
		return sManager;
	}

	private ChoiceManager() {
		init();
	}

	public void init() {
		mUserConfigOption = new CompentOption<Void>();
		PluginManager.addCommandProcessor("txz.selector.", new CommandProcessor() {
			@Override
			public Object invoke(String command, Object[] args) {
				if ("entrySelector".equals(command)) {
					if (args != null && args.length > 2) {
						if (args[0] instanceof IPluginSelectorControl && args[1] instanceof List
								&& args[2] instanceof ISelectControl.OnItemSelectListener) {
							// TODO
							entryPluginSelector((IPluginSelectorControl) args[0], (List) args[1],
									(ISelectControl.OnItemSelectListener) args[2]);
							return 1;
						}

						if (args[0] instanceof PluginWorkChoice && args[1] instanceof List
								&& args[2] instanceof OnItemSelectListener) {
							showPluginList((PluginWorkChoice) args[0], (List) args[1], (OnItemSelectListener) args[2]);
							return 2;
						}
					}
				}
				return null;
			}
		});
	}

	public void showMiHomeList(CarControlHomeWorkChoice.CarControlHomeData data, CompentOption<CarControlHomeWorkChoice.CarControlHomeData> option) {
		sendBroadcastForTest("MiHome", data.mMiHomeItemList);
		if (option == null) {
			option = new CompentOption<CarControlHomeWorkChoice.CarControlHomeData>();
		}
		if(data.mMiHomeItemList.size() > 1) {
			TtsManager.getInstance().speakText("为您找到以下设备");
		}
		//TODO set the pageName for this list
		showList(CarControlHomeWorkChoice.class, data, option);
	}

	private  boolean hasInitCall;

	private void initCallConfig() {
		if (hasInitCall) {
			return;
		}
		List<String> configs = new ArrayList<String>();
		configs.add(TXZFileConfigUtil.KEY_CONTACTS_HOLD_ORIGINAL);
		Map<String, String> cMap = TXZFileConfigUtil.getConfig(configs);
		if (cMap != null && cMap.containsKey(TXZFileConfigUtil.KEY_CONTACTS_HOLD_ORIGINAL)) {
			String configVal = cMap.get(TXZFileConfigUtil.KEY_CONTACTS_HOLD_ORIGINAL);
			LogUtil.logd("init configVal:" + configVal);
			if (!TextUtils.isEmpty(configVal)) {
				try {
					Boolean bHold = Boolean.parseBoolean(configVal);
					if (bHold != null) {
						CallWorkChoice.setHoldOriginal(bHold);
					}
				} catch (Exception e) {
				}
			} else if (WinManager.getInstance().isUI1_0Adapter()) {
				LogUtil.logd("isUI1_0Adapter");
				CallWorkChoice.setHoldOriginal(true);
			}
		} else if (WinManager.getInstance().isUI1_0Adapter()) {
			LogUtil.logd("isUI1_0Adapter");
			CallWorkChoice.setHoldOriginal(true);
		}
		hasInitCall = true;
	}

	private boolean mJustResultText = false;

	public boolean isRecordWin2JustNoResultText() {
		return mJustResultText;
	}

	private boolean mPoiUseDefaultCoexistAsrAndWakeup = false;

	public boolean isPoiUseDefaultCoexistAsrAndWakeup(){
		return mPoiUseDefaultCoexistAsrAndWakeup;
	}

	public boolean isCoexistAsrAndWakeup() {
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof AbstractChoice) {
			return ((AbstractChoice) mCurrActiveChoice).isCoexistAsrAndWakeup();
		}

		return ProjectCfg.mCoexistAsrAndWakeup;
	}

	/**
	 * 清除选择
	 */
	public void clearIsSelecting() {
		if (mCurrActiveChoice != null) {
			mCurrActiveChoice.clearIsSelecting();
		}
		if (mSelectorControl != null) {
			mSelectorControl.clearIsSelecting();
		}

		WinHelpDetailTops.getInstance().clearIsSelecting();
		WinHelpDetailSelector.getInstance().clearIsSelecting();
		AuthorizationViewManager.getAuthorizationViewManager().clearIsSelecting();
		// 关掉地址编辑界面
		closeAllWin();
		//关闭齐悟车票的所有弹窗
		QiWuTicketManager.getInstance().closeAllDialog();
	}

	/**
	 * 清除选择，除了帮助界面，在帮助界面点击
	 */
	public void clearIsSelectingClickHelp() {
		if (mCurrActiveChoice != null) {
			mCurrActiveChoice.clearIsSelecting();
		}
		if (mSelectorControl != null) {
			mSelectorControl.clearIsSelecting();
		}

//		WinHelpDetailTops.getInstance().clearIsSelecting();
		WinHelpDetailSelector.getInstance().clearIsSelecting();
		// 关掉地址编辑界面
		closeAllWin();
	}

	void closeAllWin() {
		AppLogic.removeUiGroundCallback(mDismissRunnable);
		AppLogic.runOnUiGround(mDismissRunnable, 20);
	}

	Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {
			SearchEditManager.getInstance().setNeedCloseDialog(true);
			SearchEditManager.getInstance().dismiss();
		}
	};

	/**
	 * 获取上次上报内容项
	 * @return
	 */
	public JSONBuilder getLastReport() {
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof WorkChoice) {
			return ((WorkChoice) mCurrActiveChoice).getLastReport();
		}
		return null;
	}

	/**
	 * 当前是否在选择
	 *
	 * @return
	 */
	public boolean isSelecting() {
		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			return true;
		}

		IChoice<?> choice = mCurrActiveChoice;
		if (choice == null || !choice.isSelecting()) {
			choice = WinHelpDetailTops.getInstance();
		}
		if (choice == null || !choice.isSelecting()) {
			choice = WinHelpDetailSelector.getInstance();
		}
		return choice != null ? choice.isSelecting() : false;
	}

	/**
	 * 重计超时
	 */
	public void checkTimeout(boolean clearProgress) {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				((AbsWorkChoice) mCurrActiveChoice).checkTimeout(clearProgress);
			}
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.checkDismissTask();
		}
	}

	/**
	 * 清除倒计时
	 */
	public void clearTimeout() {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				((AbsWorkChoice) mCurrActiveChoice).clearTimeout();
			}
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.removeDismissTask();
		}
	}

	/**
	 * 取消进度条
	 */
	public void clearProgress() {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				((AbsWorkChoice) mCurrActiveChoice).clearProgress();
			}
		}
		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.cancelProgress();
		}
	}

	private QuickClickUtil mQuickClickUtil = new QuickClickUtil();

	/**
	 * 进入编辑关键字
	 */
	public void notifyEditPage() {
		if (mQuickClickUtil.check()){
			LogUtil.logd("EditPage : click too fast");
			return;
		}
		clearTimeout();
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof PoiWorkChoice) {
			((PoiWorkChoice) mCurrActiveChoice).notifyShowEditPoiPage();
		}
	}

	/**
	 * 进入编辑城市
	 */
	public void notifyCityEditPage() {
		if (mQuickClickUtil.check()){
			LogUtil.logd("CityEditPage : click too fast");
			return;
		}
		clearTimeout();
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof PoiWorkChoice) {
			((PoiWorkChoice) mCurrActiveChoice).notifyShowEditCityPage();
		}
	}

	/**
	 */
	public void mapPoiViewLoading() {
		if(mCurrActiveChoice instanceof PoiWorkChoice){
			((PoiWorkChoice) mCurrActiveChoice).mapPoiViewLoading();
		}
	}

	/**
	 * 地图界面的Tts播报
	 */
	public void mapActionResult(String spk) {
		JSONBuilder json = new JSONBuilder(spk);
		Integer action = json.getVal("action", Integer.class);
		Boolean result = json.getVal("result", Boolean.class);
		if (action != null && result != null && mCurrActiveChoice != null
				&& mCurrActiveChoice instanceof PoiWorkChoice) {
			((PoiWorkChoice) mCurrActiveChoice).mapActionResult(action, result);
		} else if (action != null && result != null && mCurrActiveChoice != null
				&& mCurrActiveChoice instanceof AsyncWorkChoice) {
			String tts = PoiWorkChoice.getMapTtsStrData(action, result);
			TtsManager.getInstance().speakText(tts);
		}
	}

	/**
	 * 上下页操作
	 *
	 * @param isNext
	 * @return
	 */
	public boolean snapPage(boolean isNext) {
		WinHelpDetailSelector.getInstance().snapPage(isNext);
		WinManager.getInstance().getViewPluginUtil().snapPage(isNext);
		WinHelpDetailTops.getInstance().snapPage(isNext);

		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbstractChoice) {
				if (isNext) {
					return ((AbstractChoice) mCurrActiveChoice).nextPage(null);
				} else {
					return ((AbstractChoice) mCurrActiveChoice).lastPage(null);
				}
			}
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			if (isNext) {
				return mSelectorControl.mPageHelper.nextPager();
			} else {
				return mSelectorControl.mPageHelper.prevPager();
			}
		}
		return false;
	}

	/**
	 * TTS播报重新选择
	 */
	public void selectAgain() {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()
				&& mCurrActiveChoice instanceof AbstractChoice) {
			((AbstractChoice) mCurrActiveChoice).selectAgain();
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.selectAgain();
		}
	}

	/**
	 * 从列表中选择idx
	 *
	 * @param idx
	 */
	public void selectIdx(int idx, String action, Integer operateSource) {
		boolean currPage = is2_0VersionChoice();
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbstractChoice) {
				final AbstractChoice choice = (AbstractChoice) mCurrActiveChoice;
				if (action == null) {
					if (choice instanceof WorkChoice) {
						int clickType = WorkChoice.SELECT_TYPE_CLICK;
						if (operateSource != null) {
							switch (operateSource) {
								case 2:
									clickType = WorkChoice.SELECT_TYPE_PARTY_CONTROL;
									break;
								default:
									break;
							}
						}
						((WorkChoice) choice).putReport(WorkChoice.KEY_SELECT_TYPE, clickType + "");
					}
					if (currPage) {
						choice.selectIndex(idx, null);
					} else {
						choice.selectAllIndex(idx, null);
					}
				} else {
					choice.selectIdxWithAction(idx, currPage, action);
				}
			}
		}
		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			if (currPage) {
				mSelectorControl.selectIndexFromPage(idx, null);
			} else {
				mSelectorControl.selectIndexFromAll(idx, null);
			}
		}

		WinHelpDetailTops.getInstance().onItemSelected(idx,false);
	}

	/**
	 * 取消选择
	 */
	public void selectCancel(int selectType) {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()
				&& mCurrActiveChoice instanceof AbstractChoice) {
			((AbstractChoice) mCurrActiveChoice).selectCancel(selectType, null);
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.selectCancel(false);
		}

		WinHelpDetailTops.getInstance().clearIsSelecting();
		WinHelpDetailSelector.getInstance().clearIsSelecting();
	}

	/**
	 * 关掉
	 */
	public void selectCallCancel() {
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof CallWorkChoice) {
			((AbstractChoice) mCurrActiveChoice).cancelWithClose();
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.clearIsSelecting();
			RecorderWin.close();
		}
	}

	/**
	 * 编辑界面退回
	 */
	public void selectBackAsr() {
		if (mCurrActiveChoice != null && mCurrActiveChoice instanceof PoiWorkChoice
				&& !mCurrActiveChoice.isSelecting()) {
			String spk = NativeData.getResString("RS_SELECTOR_HELP");
			RecorderWin.open(spk);
			return;
		}

//		selectCancel(AbstractChoice.SELECT_TYPE_BACK);
	}

	/**
	 * 确认
	 */
	public void selectSure() {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()
				&& mCurrActiveChoice instanceof AbstractChoice) {
			((AbstractChoice) mCurrActiveChoice).selectSure(null);
		}

		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.selectSure(false);
		}
	}

	/**
	 * 是否启用上下分页选择（与旧版本的选择区分）
	 *
	 * @return
	 */
	public boolean is2_0VersionChoice() {
		boolean b = !WinManager.getInstance().hasThirdImpl();
		if (b) {
			return true;
		}

		if (mUserConfigOption != null && mUserConfigOption.getIs2_0Version() != null) {
			return mUserConfigOption.getIs2_0Version();
		}

		return false;
	}

	/**
	 * 设置超时时间
	 *
	 * @param delay
	 */
	public void setTimeoutDelay(long delay) {
		LogUtil.logd("setTimeoutDelay:" + delay);
		mUserConfigOption.setTimeout(delay);
		ISelectControl.sDismissDelay = delay;
	}

	/**
	 * 暂停
	 */
	public void stopTtsAndAsr() {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()
				&& mCurrActiveChoice instanceof AbstractChoice) {
			((AbstractChoice) mCurrActiveChoice).stopTtsAndAsr();
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				((AbsWorkChoice) mCurrActiveChoice).clearProgress();
			}
		}
		if (mSelectorControl != null && mSelectorControl.isSelecting()) {
			mSelectorControl.cancelProgress();
		}
	}

	/**
	 * 自动适应分页数量
	 *
	 * @param numSize
	 */
	public void autoFixNumPageSize(int numSize) {
		if (mCurrActiveChoice == null || !mCurrActiveChoice.isSelecting()) {
			return;
		}
		CompentOption<?> option = getChoiceOption();
		if (option == null) {
			LogUtil.loge("autoFixNumPageSize fail option is null！");
			return;
		}
		Integer optionNum = option.getNumPageSize();

		Integer newUserNum = getNumPageSize(option.getListPageName());

		if (optionNum != null && optionNum == newUserNum) {
			LogUtil.loge("autoFixNumPageSize use userConfig");
			return;
		}
		if (newUserNum != null) {
			numSize = newUserNum;
		}
		if (optionNum != numSize) {
			LogUtil.logd("updateChoiceOption from:" + optionNum + " to:" + numSize);
			option.setNumPageSize(numSize);
			updateChoiceOption(option);
			return;
		}

		Integer userNum = mUserConfigOption.getNumPageSize();
		if (optionNum != null && optionNum == userNum) {
			LogUtil.loge("autoFixNumPageSize use userConfig");
			return;
		}
		if (userNum != null) {
			numSize = userNum;
		}
		if (optionNum != numSize) {
			LogUtil.logd("updateChoiceOption from:" + optionNum + " to:" + numSize);
			option.setNumPageSize(numSize);
			updateChoiceOption(option);
		}
	}

	/**
	 * 自动适应分页数量
	 *
	 */
	public void autoFixNumPageSize(String listPageName) {
		if (mCurrActiveChoice == null || !mCurrActiveChoice.isSelecting()) {
			return;
		}

		CompentOption<?> option = getChoiceOption();
		if (option == null) {
			LogUtil.loge("autoFixNumPageSize fail option is null！");
			return;
		}
		if (!option.getListPageName().equals(listPageName)) {
			return;
		}
		Integer optionNum = option.getNumPageSize();

		Integer userNum = getNumPageSize(option.getListPageName());
		if (optionNum != null && optionNum == userNum) {
			LogUtil.loge("autoFixNumPageSize use userConfig");
			return;
		}

		if (optionNum != userNum) {
			LogUtil.logd("updateChoiceOption from:" + optionNum + " to:" + userNum);
			option.setNumPageSize(userNum);
			updateChoiceOption(option);
		}
	}

	public void updatePageTimeout() {
		if (mCurrActiveChoice == null || !mCurrActiveChoice.isSelecting()) {
			return;
		}
		CompentOption<?> option = getChoiceOption();
		if (option == null) {
			LogUtil.loge("updatePageTimeout fail option is null！");
			return;
		}
		Long optionTimeout = option.getTimeout();

		Long userTimeout = getPageTimeout(option.getListPageName());
		if (optionTimeout != null && optionTimeout.equals(userTimeout) ) {
			LogUtil.loge("updatePageTimeout use userConfig");
			return;
		}

		if (userTimeout != null) {
			LogUtil.logd("updateChoiceOption from:" + optionTimeout + " to:" + userTimeout);
			option.setTimeout(userTimeout);
			updateChoiceOption(option);
		}
	}

	/**
	 * 更新列表页数的通用方法
	 * @param option 参数配置
	 * @param pageName 列表名字
	 * @param <E>
	 */
	public <E> void setPageSizeOption(CompentOption<E> option, String pageName){
		if (option != null) {
			Integer pageSize = mPagingSizeMap.get(pageName);
			if (pageSize != null && pageSize > 0) {
				LogUtil.logd("setPageSizeOption : " + pageName + " ; pageSize : " + pageSize);
				option.setNumPageSize(pageSize);
			}
		}
	}

	ConcurrentHashMap<String,Integer> mPagingSizeMap = new ConcurrentHashMap<String, Integer>();
	ConcurrentHashMap<String,Long> mPagingTimeoutMap = new ConcurrentHashMap<String, Long>();

	/**
	 * 根据各个界面
	 * @param pageName
	 * @return
	 */
	public Integer getNumPageSize(String pageName){
		if (!TextUtils.isEmpty(pageName)) {
			Integer pageSize = mPagingSizeMap.get(pageName);
			if (pageSize != null && pageSize > 0) {
				return pageSize;
			}
		}
		return null;
	}

	/**
	 * 根据各个界面
	 * @param pageName
	 * @return
	 */
	public Long getPageTimeout(String pageName){
		if (!TextUtils.isEmpty(pageName)) {
			Long timeout = mPagingTimeoutMap.get(pageName);
			if (timeout != null && timeout > 0) {
				return timeout;
			}
		}
		return null;
	}

	/**
	 * 获取当前页面显示个数
	 *
	 * @return
	 */
	public int getNumPageSize() {
		if (mUserConfigOption.getNumPageSize() != null && mUserConfigOption.getNumPageSize() > 0) {
			return mUserConfigOption.getNumPageSize();
		}
		int num = mAutoFixNumber != null ? mAutoFixNumber : 4;
		if (num <= 0) {
			num = 4;
		}
		return num;
	}

	//////////////////////////////////////// 显示List的重载方法/////////////////////////////////////////

	/**
	 * Poi列表
	 *
	 * @param poisData
	 */
	public void showPoiList(PoisData poisData) {
		showPoiList(poisData, null);
	}

	/**
	 * Poi重载方法
	 * @param poisData
	 * @param option
	 */
	public void showPoiList(final PoisData poisData, CompentOption<Poi> option) {
		if (poisData.action == null) {
			poisData.action = PoiAction.ACTION_NAVI;
		}
		sendBroadcastForTest("pois", poisData.mPois);

		if (option == null) {
			option = new CompentOption<Poi>();
			option.setNumPageSize(getPoiShowCount(poisData.isBus));
		}

		String pageName = "";
		if(NavManager.getInstance().getPoiShowIsList()) {
			if (poisData.isBus) {
				pageName = TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_LIST.name();
			} else {
				pageName = TXZConfigManager.PageType.PAGE_TYPE_POI_LIST.name();
			}
		} else {
			if (poisData.isBus) {
				pageName = TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_MAP_LIST.name();
			} else {
				pageName = TXZConfigManager.PageType.PAGE_TYPE_POI_MAP_LIST.name();
			}
		}
		option.setListPageName(pageName);

		if(poisData.mPois.size() == 1){
			option.setCanSure(true);
		}

		if (option != null && option.getChoiceCallback() == null) {
			option.setChoiceCallback(new ChoiceCallback() {

				@Override
				public void onClearIsSelecting() {
					ScreenUtil.setHook(null);
				}
			});
		}
		ListOptionHook<CompentOption<Poi>> hook = new ListOptionHook<CompentOption<Poi>>() {// 注册钩子，返回itemHeight和VisibleCount
			@Override
			public int getItemHeight() {
				int ih = ScreenUtil.mListViewRectHeight / getVisibleCount();
				LogUtil.logd("showPoi getItemHeight:" + ih);
				return ih;
			}

			@Override
			public int getVisibleCount() {
				int showCount = 0;
				if (option != null) {
					showCount = option.getNumPageSize();
				} else {
					showCount = getPoiShowCount(poisData.isBus);
				}
				LogUtil.logd("showPoi getVisibleCount:" + showCount);
				return showCount;
			}
		};
		hook.option = option;

		clearIsSelecting();
		ScreenUtil.setHook(hook);
		showListWithoutClearIsSelecting(PoiWorkChoice.class, poisData, option);
	}
	public int getPoiShowCount(boolean isBussiness){
		int count  = getNumPageSize();
		if(isBussiness){
			return count*3/4;
//			if(!NavManager.getInstance().getPoiShowIsList()){
//				return count*3/4;
//			}else if(!WinManager.getInstance().isRecordWin2()){
//				return count*3/4;
//			}		
		}
		return count;
	}
	/**
	 * 显示导航历史出发地
	 * @param totalSize
	 */
	public void showNavReversal(int totalSize) {
		showNavRecord(totalSize, false);
	}

	/**
	 * 显示导航历史目的地
	 *
	 * @param totalSize
	 */
	public void showNavHistory(int totalSize) {
		showNavRecord(totalSize, true);
	}

	/**
	 * 显示导航历史
	 *
	 * @param totalSize
	 *            总记录数
	 * @param showDestination
	 *            是否是目的地显示
	 */
	private void showNavRecord(int totalSize, final boolean showDestination) {
		final AsyncRepoCompentOption<DbNavInfo> option = new AsyncRepoCompentOption<DbNavInfo>();
		option.setTotalSize(totalSize);
		final ListHook<DbNavInfo> hook = new ListHook<DbNavInfo>() {

			@Override
			public String getReportId() {
				return "Nav_History_Select";
			}

            @Override
            public boolean onCmdSelected(String type, String command) {
                mPoiMapAction = PoiWorkChoice.onMapCommandSelect(type, command);
                if (mPoiMapAction != null) {
					if (mPoiMapAction == PoiMsg.MAP_ACTION_MAP) {
						option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST.name());
					} else if (mPoiMapAction == PoiMsg.MAP_ACTION_LIST) {
						option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST.name());
					}
					setPageSizeOption(option, option.getListPageName());
					Integer size = getNumPageSize(mPoiMapAction == PoiMsg.MAP_ACTION_LIST
							? TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST.name()
							: TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST.name());
					if (size == null) {
						size = ChoiceManager.getInstance().getNumPageSize();
					}
					((AsyncWorkChoice) workChoice).resetRepo(new RepoNavInscriber(NavInscriber.getInstance().rebuildCache(GlobalContext.get(), size), showDestination));
					workChoice.getPageControl().reset();
					this.workChoice.refreshCurrPage();
					return true;
				}
				return super.onCmdSelected(type, command);
            }

			@Override
			public void onAddWakeupCmds(AsrUtil.AsrComplexSelectCallback acsc,  List<DbNavInfo> curData) {
				PoiWorkChoice.addMapCommand(acsc);
				if(curData.size() == 1){
					option.setCanSure(true);
					acsc.addCommand("SURE", NativeData.getResStringArray("RS_CMD_SELECT_SURE"));
				}
			}

			private Integer mPoiMapAction;

			@Override
			public void onConvToJson(List<DbNavInfo> ts, JSONBuilder jsonBuilder) {
				sendBroadcastForTest("nav_historys", ts);
				if (ts != null && !ts.isEmpty()) {
					jsonBuilder.put("type", RecorderWin.PoiChoiceSence);
					jsonBuilder.put("keywords", "");
					jsonBuilder.put("poitype", "");
					jsonBuilder.put("action", PoiAction.ACTION_NAV_HISTORY);
					jsonBuilder.put("city", "");
					jsonBuilder.put("count", ts.size());
					jsonBuilder.put("showcount",getNumPageSize());
					jsonBuilder.put("listmodel", NavManager.getInstance().getPoiShowIsList()); //是否为列表模式
					jsonBuilder.put("mapAction",mPoiMapAction); //地图的操作
					org.json.JSONArray jsonArray = new org.json.JSONArray();
					for (int i = 0; i < ts.size(); i++) {
						NavInfo navInfo = ts.get(i).navInfo;
						Poi poi = convFromNavInfo(navInfo);
						try {
							JNIHelper.logd(
									"TXZPoiSearchTool poitype: " + poi.toJsonObject().put("poitype", poi.getPoiType()));
							jsonArray.put(poi.toJsonObject().put("poitype", poi.getPoiType()));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					jsonBuilder.put("pois", jsonArray);
					if (showDestination) {
						jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_HISTORY_HINT"));
					} else {
						if (ts.size() == 1) {
							jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_REVERSAL_SINGLE_TTS"));
						}else{
							jsonBuilder.put("prefix", NativeData.getResString("RS_NAV_HISTORY"));
						}
					}

					jsonBuilder.put("vTips",getNavRecordTips());
				}
			}


			private String getNavRecordTips(){
				String tips = "";
				NavInscriber.NavInfoCache mNavInfoCache = NavInscriber.getInstance().getCache();
				if ( mNavInfoCache != null) {
					if (mNavInfoCache.getPageSize() == (mNavInfoCache.getCurrPage() + 1)) { //是最后一页或者只有一页
						if (mNavInfoCache.getCurrPageNumSize() == 1) {
							tips = NativeData.getResString(mNavInfoCache.getCurrPage() == 0 ? "RS_VOICE_TIPS_POI_HISTORY_ONE" : "RS_VOICE_TIPS_POI_HISTORY_ONE_LAST" );
						} else if (mNavInfoCache.getCurrPageNumSize() == 2) {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_TWO");
						} else {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_MORE");
						}
					} else if ((mNavInfoCache.getCurrPage() + 1) == 1) {  //第一页
						if (mNavInfoCache.getCurrPageNumSize() == 1) {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_FIRST_PAGE_ONLY_ONE");
						} else {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_FIRST_PAGE");
						}
					} else { //其他中间页
						if (mNavInfoCache.getCurrPageNumSize() == 1) {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_OTHER_PAGE_ONLY_ONE");
						} else {
							tips = NativeData.getResString("RS_VOICE_TIPS_POI_HISTORY_OTHER_PAGE");
						}
					}
				}
				return tips;
			}

			private Poi convFromNavInfo(NavInfo navInfo) {
				NavPointInfo info = showDestination ? navInfo.msgEndAddress : navInfo.msgBeginAddress;
				Poi poi = new Poi();
				poi.setLat(info.msgGpsInfo.dblLat);
				poi.setLng(info.msgGpsInfo.dblLng);
				poi.setDistance(BDLocationUtil.calDistance(info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng));
				poi.setAction(PoiAction.ACTION_NAV_HISTORY);
				poi.setName(info.strPoiName);
				poi.setGeoinfo(info.strPoiAddress);
				return poi;
			}

			@Override
			public boolean onEmptyDataUpdate() {
				String tts = NativeData.getResString("RS_NAV_HISTORY_NO_FOUND");
				if (!showDestination) {
					tts = NativeData.getResString("RS_NAV_REVERSAL_NO_FOUND");
				}
				RecorderWin.open(tts, VoiceData.GRAMMAR_SENCE_NAVIGATE);
				return true;
			}

			@Override
			public void onRemoveItem(DbNavInfo item) {
				if (showDestination) {
					NavInscriber.getInstance().removeDestRecord(item);
				} else {
					NavInscriber.getInstance().removeFromRecord(item);
				}
			}

			@Override
			public boolean onSelectItem(DbNavInfo item) {
				return false;
			}

			private CommWorkChoice<DbNavInfo> mSpace;

			@Override
			public void onGetWorkSpace(CommWorkChoice<DbNavInfo> wp) {
				super.onGetWorkSpace(wp);
				this.mSpace = wp;
			}

			@Override
			public String convItemToJson(DbNavInfo item) {
				return convFromNavInfo(item.navInfo).toString();
			}
		};
		option.setCallbackListener(new OnItemSelectListener<DbNavInfo>() {

			@Override
			public boolean onItemSelected(boolean isPreSelect, final DbNavInfo v, boolean fromPage, final int idx,
										  final String fromVoice) {
				if (fromVoice != null) {
					String ttsHint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%PATH%", fromVoice);
					NavManager.getInstance().setSpeechAfterPlanError(true, NativeData.getResString("RS_MAP_PATH_FAIL"));
					final int mSpeechTaskId = TtsManager.getInstance().speakText(ttsHint, new ITtsCallback() {

						@Override
						public void onSuccess() {
							hook.notifySelectEnd(v, idx, fromVoice);
							NavManager.getInstance().NavigateTo(convFromNavInfo(v.navInfo));
						}
					});
					RecorderWin.addCloseRunnable(new Runnable() {

						@Override
						public void run() {
							TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
						}
					});
				} else {
					hook.notifySelectEnd(v, idx, fromVoice);
					NavManager.getInstance().NavigateTo(convFromNavInfo(v.navInfo));
				}
				return true;
			}

			private Poi convFromNavInfo(NavInfo navInfo) {
				NavPointInfo info = showDestination ? navInfo.msgEndAddress : navInfo.msgBeginAddress;
				Poi poi = new Poi();
				poi.setLat(info.msgGpsInfo.dblLat);
				poi.setLng(info.msgGpsInfo.dblLng);
				poi.setDistance(BDLocationUtil.calDistance(info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng));
				poi.setAction(PoiAction.ACTION_NAVI);
				poi.setName(info.strPoiName);
				poi.setGeoinfo(info.strPoiAddress);
				return poi;
			}
		});
        Integer size = getNumPageSize(NavManager.getInstance().getPoiShowIsList()
                ? TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST.name()
                : TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST.name());
        if (size == null) {
        	size = ChoiceManager.getInstance().getNumPageSize();
		}
        option.setRepo(new RepoNavInscriber(NavInscriber.getInstance().rebuildCache(GlobalContext.get(), size), showDestination));
		if (totalSize == 1) {
			String tts = showDestination ? NativeData.getResString("RS_NAV_HISTORY_SINGLE_TTS")
					: NativeData.getResString("RS_NAV_REVERSAL_SINGLE_TTS");
			option.setTtsText(tts);
			option.setCanSure(true);
		} else {
			option.setTtsText(NativeData.getResString("RS_NAV_HISTORY"));
		}

		option.setListPageName(NavManager.getInstance().getPoiShowIsList()
				? TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST.name()
				: TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST.name());
		if (option != null && option.getChoiceCallback() == null) {
			option.setChoiceCallback(new ChoiceCallback() {

				@Override
				public void onClearIsSelecting() {
					ScreenUtil.setHook(null);
				}
			});
		}
		ListOptionHook<CompentOption<DbNavInfo>> listOptionHook = new ListOptionHook<CompentOption<DbNavInfo>>() {// 注册钩子，返回itemHeight和VisibleCount
			@Override
			public int getItemHeight() {
				int ih = ScreenUtil.mListViewRectHeight / getVisibleCount();
				LogUtil.logd("showNavRecord getItemHeight:" + ih);
				return ih;
			}

			@Override
			public int getVisibleCount() {
				int showCount = 0;
				if (option != null) {
					showCount = option.getNumPageSize();
				} else {
					showCount = ChoiceManager.getInstance().getNumPageSize();
				}
				LogUtil.logd("showNavRecord getVisibleCount:" + showCount);
				return showCount;
			}
		};
		listOptionHook.option = option;
        clearIsSelecting();
        option.setHook(hook);
        ScreenUtil.setHook(listOptionHook);
        showListWithoutClearIsSelecting(AsyncWorkChoice.class, new ArrayList(), option);
	}

	// TODO HookCompentOption需要子类，可以往工作区添加自定义免唤醒词
	public void showNavAppsList(final List<NavAppBean> apps, String ttsSpk,
								final OnItemSelectListener<NavAppBean> selectListener) {
		ListHookCompentOption<NavAppBean> option = new ListHookCompentOption<NavAppBean>();
		option.setHook(new ListHook<NavAppBean>() {
			private WorkChoice<?, NavAppBean> wp;

			@Override
			public String getReportId() {
				return "Nav_Tools_Select";
			}

			@Override
			public void onConvToJson(List<NavAppBean> ts, JSONBuilder jsonObject) {
				List<String> names = new ArrayList<String>();
				for (int i = 0; i < ts.size(); i++) {
					NavAppBean appInfo = (NavAppBean) ts.get(i);
					names.add(appInfo.strAppName + ":" + appInfo.strPackageName);
				}

				jsonObject.put("type", RecorderWin.SimpleSence);
				jsonObject.put("title", "");
				jsonObject.put("action", "navtools");
				jsonObject.put("totalCount", this.wp.getPageControl().getTotalSize());
				jsonObject.put("count", ts.size());
				jsonObject.put("beans", names.toArray(new String[names.size()]));
				jsonObject.put("prefix", this.wp.getOption().getTtsText());
			}

			@Override
			public boolean onSelectItem(NavAppBean item) {
				clearIsSelecting();
				if (selectListener != null) {
					return selectListener.onItemSelected(true, item, true, 0, null);
				}
				NavAppManager.getInstance().handleSwitchNavApp(item);
				return true;
			}

			private Map<String, NavAppBean> cmdTypes = new HashMap<String, NavAppManager.NavAppBean>();

			@Override
			public void onAddWakeupCmds(AsrComplexSelectCallback acsc, List<NavAppBean> curData) {
				for (NavAppBean bean : curData) {
					acsc.addCommand(bean.strAppName, bean.strAppName);
					cmdTypes.put(bean.strAppName, bean);
				}
			}

			@Override
			public boolean onCmdSelected(String type, String command) {
				if (cmdTypes.containsKey(type)) {
					NavAppBean bean = cmdTypes.get(type);
					int idx = apps.indexOf(bean);
					String objJson = convItemToJson(bean);
					workChoice.putReport(CommWorkChoice.KEY_DETAIL, objJson);
					workChoice.putReport(CommWorkChoice.KEY_INDEX, idx + "");
					workChoice.doReportSelectFinish(true, CommWorkChoice.SELECT_TYPE_VOICE, command);
					onSelectItem(bean);
					return true;
				}
				return super.onCmdSelected(type, command);
			}

			@Override
			public void onGetWorkSpace(CommWorkChoice<NavAppBean> wp) {
				super.onGetWorkSpace(wp);
				this.wp = wp;
			}

			@Override
			public String convItemToJson(NavAppBean item) {
				JSONBuilder jsonBuilder = new JSONBuilder();
				jsonBuilder.put("navType", item.navType);
				jsonBuilder.put("strAppName", item.strAppName);
				jsonBuilder.put("strPackageName", item.strPackageName);
				return jsonBuilder.toString();
			}
		});
		option.setCallbackListener(new OnItemSelectListener<NavAppManager.NavAppBean>() {

			@Override
			public boolean onItemSelected(boolean isPreSelect, NavAppBean v, boolean fromPage, int idx,
										  String fromVoice) {
				if (selectListener != null) {
					return selectListener.onItemSelected(isPreSelect, v, fromPage, idx, fromVoice);
				}
				return false;
			}
		});
		String ttsText = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_LIST_MORE_SPK");
		if (apps.size() == 1) {
			option.setCanSure(true);
			// option.setProgressDelay(AbsWorkChoice.TOTAL_AUTO_CALL_TIME);
			ttsText = NativeData.getResString("RS_VOICE_TTS_THEME_SELECT_LIST_ONE_SPK");
		}
		if (TextUtils.isEmpty(ttsSpk)) {
			ttsSpk = ttsText;
		}
		option.setTtsText(ttsSpk);
		sendBroadcastForTest("navtools", apps);

		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_NAV_APP_LIST.name());

		showCommList(apps, option);
	}

	/**
	 * 联系人列表
	 *
	 * @param event
	 * @param cons
	 */
	public void showContactSelectList(int event, MobileContacts cons) {
		// 保留UI1.0的配置
		initCallConfig();

		Contacts contacts = new Contacts();
		contacts.event = event;
		contacts.cons = cons;
		sendBroadcastForTest("call", Arrays.asList(cons.cons));
		CompentOption<MobileContact> option = new CompentOption<MobileContact>();
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_CALL_LIST.name());
		showList(CallWorkChoice.class, contacts, option);
	}

	/**
	 * 微信联系人列表
	 *
	 * @param event
	 * @param cons
	 * @param spk
	 */
	public void showWxList(int event, WeChatContacts cons, String spk) {
		WxData wxData = new WxData();
		wxData.event = event;
		wxData.cons = cons;
		wxData.ttsSpk = spk;
		sendBroadcastForTest("winxin", Arrays.asList(cons.cons));
		CompentOption<WeChatContact> option = new CompentOption<WeChatContact>();
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_WECHAT_LIST.name());
		showList(WxWorkChoice.class, wxData, option);
	}

	IPluginSelectorControl mSelectorControl = null;

	// 兼容老插件
	public void entryPluginSelector(IPluginSelectorControl selector, List list,
									ISelectControl.OnItemSelectListener listener) {
		mSelectorControl = selector;
		mSelectorControl.setPageCount(getNumPageSize());
		if (listener != null) {
			mSelectorControl.setOnItemSelectListener(listener);
		} else {
			mSelectorControl.setOnItemSelectListener(null);
		}
		sendBroadcastForTest("plugin", list);
		prepareSet(mSelectorControl);
		((IPluginSelectorControl) mSelectorControl).showList(list);
	}

	// 兼容老插件
	private void prepareSet(ISelectControl isc) {
		isc.mExitWithBack = WorkChoice.sExitBack;
		if (!WinManager.getInstance().hasThirdImpl()) {
			isc.mUseNewSelector = true;
		} else {
			isc.mUseNewSelector = is2_0VersionChoice();
		}
	}

	/**
	 * 显示插件的选择
	 *
	 * @param workChoice
	 * @param list
	 * @param listener
	 */
	public <E> void showPluginList(PluginWorkChoice<E> workChoice, List<E> list, OnItemSelectListener<E> listener) {
		if (workChoice == null) {
			return;
		}

		CompentOption<E> option = workChoice.getOption();
		if (option == null) {
			option = new CompentOption<E>();
		}
		if (option.getIs2_0Version() == null) {
			option.setIs2_0Version(is2_0VersionChoice());
		}

		if (option.getNumPageSize() == null || option.getNumPageSize() <= 0) {
			int num = mAutoFixNumber != null ? mAutoFixNumber : 4;
			if (num <= 0) {
				num = 4;
			}
			option.setNumPageSize(num);
		}
		option.setCallbackListener(listener);

		useMethod(option, mUserConfigOption, true);
		workChoice.updateCompentOption(option, false);
		workChoice.showChoices(list);
		sendBroadcastForTest("plugin", list);
		mCurrActiveChoice = workChoice;
		LogUtil.logd("activeChoice:" + workChoice);
	}

	/**
	 * 音乐列表
	 *
	 * @param musics
	 * @param keyWord
	 * @param listener
	 */
	public void showMusicList(final List<Music> musics, String keyWord, final AudioSelectorListener listener) {
		if (musics == null) {
			JNIHelper.logd("entryAudioSelector musics is null");
			return;
		}

		List<AudioShowData> asds = new ArrayList<AudioShowData>();
		for (Music m : musics) {
			AudioShowData asd = new AudioShowData();
			asd.setId(0);
			asd.setName(m.authorName);
			asd.setTitle(m.audioName);
			asd.setAlbumId(m.audioId);
			asd.setAlbumName(m.sourceName);
			asd.setAlbumIntro(m.albumIntro);
			asd.setAlbumTrackCount(m.includeTrackCount);
			asds.add(asd);
		}

		showMusicList(asds, new OnItemSelectListener<AudioShowData>() {

			@Override
			public boolean onItemSelected(boolean isPreSelect, AudioShowData v, boolean fromPage, int idx,
										  String fromVoice) {
				if (isPreSelect) {
					return false;
				}

				if (listener != null) {
					listener.onAudioSelected(musics.get(idx), idx);
				}
				return true;
			}
		});
	}

	/**
	 * 音乐重载
	 *
	 * @param datas
	 * @param callbackListener
	 */
	public void showMusicList(List<AudioShowData> datas, OnItemSelectListener<AudioShowData> callbackListener) {
		CompentOption<AudioShowData> cOption = new CompentOption<AudioShowData>();
		cOption.setCallbackListener(callbackListener);

		MusicData md = new MusicData();
		md.datas = datas;
		md.isAuto = datas != null && (datas.size() == 1);
		sendBroadcastForTest("music", datas);

		setMusicPageName(md, datas, cOption);
		// 最后调用MusicWorkChoice的showChoice方法去显示列表
		showList(MusicWorkChoice.class, md, cOption);
	}

	private void setMusicPageName(MusicData musicData, List<AudioShowData> data, CompentOption<AudioShowData> cOption) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getNovelStatus() != 0 || data.get(i).isLatest() || data.get(i).isListened() || data.get(i).isPaid() || data.get(i).isLastPlay()) {
				cOption.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_WITH_TAG.name());
				musicData.isMusic = false;
				return;
			} else {
				cOption.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_LIST.name());
				musicData.isMusic = true;
			}
		}
	}

	/**
	 * 音乐重载
	 *
	 * @param asds
	 * @param isAuto
	 * @param delayTime
	 * @param listener
	 * @param continuePlay
	 */
	public void showMusicList(List<AudioShowData> asds, boolean isAuto, long delayTime,
							  final OnItemSelectListener<AudioShowData> listener, boolean continuePlay) {
		CompentOption<AudioShowData> cOption = new CompentOption<AudioShowData>();
		cOption.setCallbackListener(new OnItemSelectListener<AudioShowData>() {

			@Override
			public boolean onItemSelected(boolean isPreSelect, AudioShowData v, boolean fromPage, int idx,
										  String fromVoice) {
				if (listener != null) {
					return listener.onItemSelected(isPreSelect, v, fromPage, idx, fromVoice);
				}
				return false;
			}
		});
		MusicData md = new MusicData();
		md.isAuto = isAuto;
		md.delayTime = delayTime;
		md.continuePlay = continuePlay;
		md.datas = asds;
		sendBroadcastForTest("music", asds);

		setMusicPageName(md, asds, cOption);
		showList(MusicWorkChoice.class, md, cOption);
	}

	@Deprecated // 用music
	public void showAudioList() {
	}

	/**
	 * 显示TTS主题
	 *
	 * @param themeList
	 */
	public void showTtsList(List<TTSTheme_Info> themeList) {
		sendBroadcastForTest("tts", themeList);
		CompentOption<TTSTheme_Info> option = new CompentOption<TTSTheme_Info>();
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_TTS_LIST.name());
		showList(TtsWorkChoice.class, themeList, option);
	}

	/**
	 * 显示TTS扫码界面
	 *
	 * @param json
	 * @return
	 */
	public boolean showTtsQr(String json) {
		clearIsSelecting();
		if (mCurrActiveChoice == null || !(mCurrActiveChoice instanceof TTSNoResultParse)) {
			mCurrActiveChoice = new TTSNoResultParse();
		}

		return ((TTSNoResultParse) mCurrActiveChoice).showQR(WinManager.getInstance().hasThirdImpl(), json);
	}

	/**
	 * 显示提醒列表
	 * @param reminders
	 */
	public void showReminderList(List<ReminderItem> reminders, CompentOption<ReminderItem> option){
		sendBroadcastForTest("reminder", reminders);
		if (option == null) {
			option = new CompentOption<ReminderItem>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_REMINDER_LIST.name());
		showList(ReminderWorkChoice.class, reminders, option);
	}

	public void showFlightList(FlightDataBean data, CompentOption<FlightWorkChoice.FlightItem> option){
		sendBroadcastForTest("flights", data.datas);
		if (option == null) {
			option = new CompentOption<FlightWorkChoice.FlightItem>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_LIST.name());
		showList(FlightWorkChoice.class, data, option);
	}

	public void showStyleList(List<ThemeStyle.Style> data, CompentOption<ThemeStyle.Style> option){
		sendBroadcastForTest("styles", data);
		if (option == null) {
			option = new CompentOption<ThemeStyle.Style>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_STYLE_LIST.name());
		showList(StyleWorkChoice.class, data, option);
	}
	
	/**
	 * 流量卡充值（套用通用的列表选择）
	 */
//	public void showSimList(List<UiEquipment.Sim_data_plan> plans) {
//		ListHookCompentOption<UiEquipment.Sim_data_plan> option = new ListHookCompentOption<UiEquipment.Sim_data_plan>();
//		option.setHook(new ListHook<UiEquipment.Sim_data_plan>() {
//
//			@Override
//			public String getReportId() {
//				return "Sim_Recharge_Select";
//			}
//
//			@Override
//			public void onConvToJson(List<Sim_data_plan> ts, JSONBuilder jsonBuilder) {
//				jsonBuilder.put("type", 5);
//				jsonBuilder.put("count", ts.size());
//				jsonBuilder.put("title", NativeData.getResString("RS_SIM_RECHARGE_DISPLAY_LIST_SELECT"));
//				jsonBuilder.put("prefix", NativeData.getResString("RS_SIM_RECHARGE_DISPLAY_LIST_SELECT"));
//				// 添加套餐信息
//				List<JSONObject> jsonList = new ArrayList<JSONObject>();
//				for (int i = 0, len = ts.size(); i < len; i++) {
//					UiEquipment.Sim_data_plan info = (UiEquipment.Sim_data_plan) ts.get(i);
//					JSONBuilder infoBuilder = new JSONBuilder();
//					infoBuilder.put("id", info.uint32Id);
//					infoBuilder.put("title", info.strName);
//					infoBuilder.put("price", info.uint32SellPrice);
//					infoBuilder.put("rawPrice", info.uint32Price);
//					infoBuilder.put("qrcode", info.strQrcodeUrl);
//					jsonList.add(infoBuilder.build());
//				}
//				jsonBuilder.put("data", jsonList.toArray());
//			}
//			
//			@Override
//			public String convItemToJson(Sim_data_plan item) {
//				JSONBuilder jsonBuilder = new JSONBuilder();
//				jsonBuilder.put("expireTime", item.expireTime);
//				jsonBuilder.put("strName", item.strName);
//				jsonBuilder.put("strQrcodeUrl", item.strQrcodeUrl);
//				jsonBuilder.put("uint32Id", item.uint32Id);
//				jsonBuilder.put("uint32Price", item.uint32Price);
//				jsonBuilder.put("uint32SellPrice", item.uint32SellPrice);
//				return jsonBuilder.toString();
//			}
//
//			@Override
//			public boolean onSelectItem(Sim_data_plan item) {
//				RecorderWin.close();
//				SimManager.getInstance().showRechargeQR(item);
//				return true;
//			}
//		});
//
//		option.setTtsText(NativeData.getResString("RS_SIM_RECHARGE_HINT_LIST_SELECT"));
//		sendBroadcastForTest("sim", plans);
//		showCommList(plans, option);
//	}

	/**
	 * 电影列表
	 */
	public boolean showMovieList(String json) {
		clearIsSelecting();
		if (mCurrActiveChoice == null || !(mCurrActiveChoice instanceof MovieWorkChoice)) {
			CompentOption<MovieWorkChoice.CinemaItem> option = new CompentOption<MovieWorkChoice.CinemaItem>();
			option.setIs2_0Version(is2_0VersionChoice());
			mCurrActiveChoice = new MovieWorkChoice(option);
		}

		return ((MovieWorkChoice) mCurrActiveChoice).showMovieList(json);
	}


	/***
	 * 电影票场景中电影列表showList接口*/
	public void showMovieList(List<FilmWorkChoice.FilmItem> data, CompentOption<FilmWorkChoice.FilmItem> option) {
		sendBroadcastForTest("Movie", data);
		if (option == null) {
			option = new CompentOption<FilmWorkChoice.FilmItem>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_FILM_LIST.name());
		showList(FilmWorkChoice.class,data,option);
	}

	public void showMovieTheatsList(List<MovieTheaterWorkChoice.MovieTheaterItem> data, CompentOption<MovieTheaterWorkChoice.MovieTheaterItem> option){
		sendBroadcastForTest("Movie", data);
		if (option == null) {
			option = new CompentOption<MovieTheaterWorkChoice.MovieTheaterItem>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_MOVIE_THEATER_LIST.name());
		showList(MovieTheaterWorkChoice.class, data, option);
	}

	public void showMovieTimesList(List<MovieTimeWorkChoice.MovieTimeItem> data, CompentOption<MovieTimeWorkChoice.MovieTimeItem> option){
		sendBroadcastForTest("Movie", data);
		if (option == null) {
			option = new CompentOption<MovieTimeWorkChoice.MovieTimeItem>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_MOVIE_TIMES_LIST.name());
		showList(MovieTimeWorkChoice.class, data, option);
	}

	public void showMovieSeatPlan(){
		clearIsSelecting();
		if (mCurrActiveChoice == null || !(mCurrActiveChoice instanceof MovieSeatPlanControl)) {
			mCurrActiveChoice = MovieSeatPlanControl.getInstance();
		}
	}

	public void showMoviePhoneNumQR(){
		clearIsSelecting();
		if (mCurrActiveChoice == null || !(mCurrActiveChoice instanceof MoviePhoneNumQRControl)) {
			mCurrActiveChoice = MoviePhoneNumQRControl.getInstance();
		}
	}

	public void showMovieWaitingPayQR(){
		clearIsSelecting();
		if (mCurrActiveChoice == null || !(mCurrActiveChoice instanceof MovieWaitingPayQRControl)) {
			mCurrActiveChoice = MovieWaitingPayQRControl.getInstance();
		}
	}
	public void showTrainList(TrainTicketData data, CompentOption<TrainTicketData.ResultBean.TicketListBean> option) {
		sendBroadcastForTest("trains", data.result.ticketList);
		if (option == null) {
			option = new CompentOption<TrainTicketData.ResultBean.TicketListBean>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_LIST.name());
		showList(TrainWorkChoice.class, data, option);
	}

	public void showTrainTicketList(QiWuTrainTicketData trainTicketData, CompentOption<QiWuTrainTicketData.TrainTicketBean> option){
		sendBroadcastForTest("trains", trainTicketData.mTrainTicketBeans);
		if (option == null) {
			option = new CompentOption<QiWuTrainTicketData.TrainTicketBean>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_TICKET_LIST.name());
		showList(TrainTicketWorkChoice.class, trainTicketData, option);
	}

	public void showTicketPayList(QiwuTrainTicketPayViewData ticketPayViewData, CompentOption<QiwuTrainTicketPayViewData.TicketPayBean> option){
		sendBroadcastForTest("ticketPay", ticketPayViewData.mTicketBeans);
		if (option == null) {
			option = new CompentOption<QiwuTrainTicketPayViewData.TicketPayBean>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_TICKET_PAY_LIST.name());
		option.setNumPageSize(1);
		showList(QiWuFlightTicketPayWorkChioce.class, ticketPayViewData, option);
	}

	public void showFlightTicketList(QiWuFlightTicketData flightTicketData, CompentOption<QiWuFlightTicketData.FlightTicketBean> option){
		sendBroadcastForTest("trains", flightTicketData.mFlightTicketBeans);
		if (option == null) {
			option = new CompentOption<QiWuFlightTicketData.FlightTicketBean>();
		}
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_TICKET_LIST.name());
		showList(FlightTicketWorkChioce.class, flightTicketData, option);
	}

	/**
	 * TODO 帮助页使用原来的
	 */
	public void showHelpList() {

	}
	
	/**
	 * 展示体育赛事
	 * @param competitionData
	 */
	public void showCompetition(CompetitionViewData.CompetitionData competitionData){

		sendBroadcastForTest("competitions", competitionData.mCompetitionBeans);
		CompentOption<CompetitionViewData.CompetitionData.CompetitionBean> option = new CompentOption<CompetitionViewData.CompetitionData.CompetitionBean>();
		option.setListPageName(TXZConfigManager.PageType.PAGE_TYPE_COMPETITION_LIST.name());
		showList(CompetitionWorkChoice.class, competitionData, option);
	}

	/**
	 * 通用的选择器，提供选择上下翻页
	 *
	 * @param list
	 * @param hook
	 */
	public <T> void showCommList(List<T> list, ListHook<T> hook) {
		ListHookCompentOption<T> hco = new ListHookCompentOption<T>();
		hco.setHook(hook);

		showCommList(list, hco);
	}

	/**
	 * 通用的选择器，提供选择上下翻页
	 *
	 * @param list
	 * @param option
	 */
	public void showCommList(List<?> list, ListHookCompentOption<?> option) {
		showList(CommWorkChoice.class, list, option);
	}

	<V, E> void showList(Class<?> cls, V data, CompentOption<E> option) {
		option = updateCompentOption(option);
		invokeChoices(cls, option, data);
	}

	private <V, E> void showListWithoutClearIsSelecting(final Class<?> cls, final V data,
			CompentOption<E> option) {
		option = updateCompentOption(option);
		invokeChoicesWithoutClearIsSelecting(cls, option, data);
	}

	private <E> CompentOption<E> updateCompentOption(CompentOption<E> option) {
		if (option == null) {
			option = new CompentOption<E>();
		}
		if (option.getIs2_0Version() == null) {
			option.setIs2_0Version(is2_0VersionChoice());
		}

		if (option.getNumPageSize() == null || option.getNumPageSize() <= 0) {
			int num = mAutoFixNumber != null ? mAutoFixNumber : 4;
			if (num <= 0) {
				num = 4;
			}
			option.setNumPageSize(num);
		}

		useMethod(option, mUserConfigOption, true);

		Integer mUserPageSize = getNumPageSize(option.getListPageName());
		if (mUserPageSize != null && mUserPageSize > 0) {
			option.setNumPageSize(mUserPageSize);
		}

		Long mUserPageTimeout = getPageTimeout(option.getListPageName());
		if (mUserPageTimeout != null && mUserPageTimeout > 0) {
			option.setTimeout(mUserPageTimeout);
		}
		return option;
	}

	/**
	 * 如果Option字段为空，则选用topOption，replaceUse表示两个对象的字段不为空的时候
	 * 为true则用topOption，否则用option
	 *
	 * @param option
	 * @param topOption
	 * @param replaceUserConfig
	 */
	private void useMethod(CompentOption<?> option, CompentOption<?> topOption, boolean replaceUserConfig) {
		if (option == null || topOption == null) {
			return;
		}

		Class<?> topCls = topOption.getClass();
		Field[] fields = null;
		if (topCls.isAssignableFrom(option.getClass())) {
			fields = topCls.getDeclaredFields();
		} else {
			fields = option.getClass().getDeclaredFields();
		}
		if (fields != null) {
			for (Field f : fields) {
				try {
					f.setAccessible(true);
					Object topObj = f.get(topOption);
					Object obj = f.get(option);
					if (obj == null) {
						replaceMemberObj(f, topObj, option);
					} else if (replaceUserConfig) {
						replaceMemberObj(f, topObj, option);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 替换字段的值
	 *
	 * @param f
	 * @param fieldObj
	 * @param targetObj
	 */
	private void replaceMemberObj(Field f, Object fieldObj, Object targetObj) {
		Object obj = null;
		if (fieldObj != null) {
			Class<?> clazz = fieldObj.getClass();
			if (clazz == Double.class) {
				obj = Double.valueOf((Double) fieldObj);
			} else if (clazz == Float.class) {
				obj = Float.valueOf((Float) fieldObj);
			} else if (clazz == Integer.class) {
				obj = Integer.valueOf((Integer) fieldObj);
			} else if (clazz == Long.class) {
				obj = Long.valueOf((Long) fieldObj);
			} else {
				// 可能会造成fieldObj被修改
				obj = fieldObj;
			}
		}

		try {
			if (obj != null) {
				f.setAccessible(true);
				f.set(targetObj, obj);
				LogUtil.logd("replaceMemberObj fName:" + f.getName() + " obj:" + obj);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private void sendBroadcastForTest(String scene, List list) {
		if (DebugCfg.ENABLE_TEST_LOG) {
			Intent intent = new Intent("com.txznet.txz.selector.test");
			intent.putExtra("scene", scene);
			JSONArray jsonArray = new JSONArray(list);
			intent.putExtra("data", jsonArray.toJSONString().getBytes());
			GlobalContext.get().sendBroadcast(intent);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////

	private IChoice<?> mCurrActiveChoice;

	private <V, E> void invokeChoices(Class<?> cls, CompentOption<E> option, V data) {
		clearIsSelecting();
		invokeChoicesWithoutClearIsSelecting(cls, option, data);
	}

	private <V, E> void invokeChoicesWithoutClearIsSelecting(final Class<?> cls,
			final CompentOption<E> option, final V data) {
		do {
			if (mCurrActiveChoice != null) {
				if (isSuperClass(cls, AbsWorkChoice.class)) {
					if (mCurrActiveChoice.getClass().equals(cls)) {
						if (option == null) {
							throw new NullPointerException("CompentOption is null！");
						}
						((AbsWorkChoice<V, E>) mCurrActiveChoice).updateCompentOption(option, false);
						((AbsWorkChoice<V, E>) mCurrActiveChoice).showChoices(data);
						break;
					}
				}
			}

			if (isSuperClass(cls, AbsWorkChoice.class)) {
				try {
					AbsWorkChoice<V, E> choice = null;
					Constructor<?>[] cons = cls.getConstructors();
					if (cons != null) {
						for (Constructor<?> con : cons) {
							Class<?>[] tCls = con.getParameterTypes();
							if (tCls != null && tCls.length == 1 && tCls[0].equals(CompentOption.class)) {
								choice = (AbsWorkChoice<V, E>) con.newInstance(option);
								break;
							}
						}
					}

					if (choice != null) {
						LogUtil.logd("showChoice:" + choice.getReportId());
						choice.showChoices(data);
					} else {
						LogUtil.loge("no found constructor！");
					}
					mCurrActiveChoice = choice;
				} catch (Exception e) {
					e.printStackTrace();
					LogUtil.loge(e.getMessage());
				}
			}
		} while (false);

		LogUtil.logd("activeChoice:" + mCurrActiveChoice);
	}

	/**
	 * 判断cls是否是pCls的子类
	 *
	 * @param cls
	 * @param pCls
	 * @return
	 */
	private boolean isSuperClass(Class<?> cls, Class<?> pCls) {
		if (cls.equals(pCls)) {
			return true;
		}

		Class<?> bCls = cls.getSuperclass();
		while (bCls != null) {
			if (bCls.equals(pCls)) {
				return true;
			}
			bCls = bCls.getSuperclass();
		}
		return false;
	}

	/**
	 * 更新配置
	 *
	 * @param option
	 */
	private <V> void updateChoiceOption(CompentOption<V> option) {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				((AbsWorkChoice) mCurrActiveChoice).updateCompentOption(option, true);
			}
		}
	}

	/**
	 * 开关选择列表的唤醒
	 * @param isBanWp
	 */
	private <V> void resumeWakeupAsr(boolean isBanWp) {
		if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				getChoiceOption().setBanWakeup(isBanWp);
				((AbsWorkChoice) mCurrActiveChoice).resumeWakeupAsrOption();
			}
		}
	}

	/**
	 * 获取当前配置
	 *
	 * @return
	 */
	private <V> CompentOption<V> getChoiceOption() {
		if (mCurrActiveChoice != null) {
			if (mCurrActiveChoice instanceof AbsWorkChoice) {
				return ((AbsWorkChoice) mCurrActiveChoice).getOption();
			}
		}
		return null;
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////
	private static final String CMD_TOGGLE_WAKEUP_ASR = "txz.record.ui.event.toggleWp";
	private static final String CMD_SET_DISPLAY_COUNT = "txz.record.ui.event.setDisplayCount";// 用户设置的数量
	private static final String CMD_SET_DISPLAY_COUNT_2 = "txz.record.ui.event.setDisplayCount2";// 用户设置的数量
	private static final String CMD_SET_DISPLAY_TIMEOUT = "txz.record.ui.event.setDisplayTimeout";// 用户设置的超时时间
	private static final String CMD_SET_MOVIE_DISPLAY_COUNT = "txz.record.ui.event.setMovieDisplayCount";// 用户设置的显示电影数量
	private static final String CMD_DISPLAY_COUNT = "txz.record.ui.event.display.count";
	private static final String CMD_OLD_ITEM_RIGHT = "txz.record.ui.event.item.right";
	private static final String CMD_ITEM_SELECTED = "txz.record.ui.event.item.selected";
	private static final String CMD_DISPLAY_TIP = "txz.record.ui.event.display.tip";
	private static final String CMD_POIMAP_ACTION = "txz.record.ui.event.poimap.action.result";
	private static final String CMD_POIMAP_LOADING = "txz.record.ui.event.item.loading";
	private static final String CMD_LIST_ONTOUCH = "txz.record.ui.event.list.ontouch";
	private static final String CMD_EXITBACK = "txz.selector.exitBack";
	private static final String CMD_SEARCH_COUNT = "txz.selector.show.count";
	private static final String CMD_USE_2_0_VERSION = "txz.selector.useNewSelector";
	private static final String CMD_DISPLAY_PAGE = "txz.record.ui.event.display.page";
	private static final String CMD_DISPLAY_CITY= "txz.record.ui.event.display.city";
	private static final String CMD_OLD_NAVI_CLICK = "txz.selector.poi.onItemNaviClick";
	private static final String CMD_OLD_ITEM_CLICK = "txz.selector.poi.onItemClick";
	private static final String CMD_OLD_POI_EDIT = "txz.selector.poi.edit";
	private static final String CMD_OLD_AUDIO_SELECT = "txz.selector.audio.selectIndex";
	private static final String CMD_SHOW_SELECT_LIST = "txz.record.ui.event.ui.showList";
	private static final String CMD_SHOW_THIRD_POI_LIST = "txz.record.ui.event.ui.showThirdPoiList";
	private static final String CMD_SET_POI_NO_RESULT_TTS = "txz.record.ui.event.poinoresult";
	private static final String CMD_POI_USE_DEFAULT_COEXIST_ASR_AND_WAKEUP = "txz.selector.poi.useDefaultCoexistAsrAndWakeup";

	/**
	 * 不能携带返回值，往上看调用处
	 * @param packageName
	 * @param command
	 * @param data
	 * @return
	 */
	public byte[] invokeCommand(final String packageName, String command, byte[] data) {
		if (CMD_TOGGLE_WAKEUP_ASR.equals(command)) {
			Boolean banWp = Boolean.parseBoolean(new String(data));
			if (banWp != null) {
				mUserConfigOption.setBanWakeup(banWp);
				resumeWakeupAsr(banWp);
			}
			LogUtil.logd("banWp:" + banWp);
		} else if (CMD_SET_DISPLAY_COUNT.equals(command)) {
			try {
				Integer oriCount = getNumPageSize();
				Integer count = Integer.parseInt(new String(data));
				if (count != null && count > 0) {
					mUserConfigOption.setNumPageSize(count);
					autoFixNumPageSize(count);
				}
				LogUtil.logd("setDisplayCount:" + count);
			} catch (Exception e) {
			}
		} else if (CMD_SET_DISPLAY_COUNT_2.equals(command)) {
			try {
				JSONBuilder jsonBuilder = new JSONBuilder(data);
				String page = jsonBuilder.getVal("page",String.class,null);
				Integer pageSize = jsonBuilder.getVal("count",Integer.class,0);
				if (!TextUtils.isEmpty(page)) {
					if (pageSize != null && pageSize > 0) {
						LogUtil.logd("setDisplayCount2:" + page + " ; size : " + pageSize);
						mPagingSizeMap.put(page, pageSize);
					} else {
						LogUtil.logd("setDisplayCount2:" + page);
						mPagingSizeMap.remove(page);
					}
					autoFixNumPageSize(page);
				}
			} catch (Exception e) {

			}
		} else if(CMD_SET_DISPLAY_TIMEOUT.equals(command)) {
			try {
				JSONBuilder jsonBuilder = new JSONBuilder(data);
				String page = jsonBuilder.getVal("page",String.class,null);
				Long timeout = jsonBuilder.getVal("timeout",Long.class,0L);
				if (!TextUtils.isEmpty(page)) {
					if (timeout != null && timeout > 0) {
						LogUtil.logd("CMD_SET_DISPLAY_TIMEOUT:" + page + " ; timeout : " + timeout);
						mPagingTimeoutMap.put(page, timeout);
					} else {
						LogUtil.logd("CMD_SET_DISPLAY_TIMEOUT:" + page);
						mPagingTimeoutMap.remove(page);
					}
					updatePageTimeout();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (CMD_SET_MOVIE_DISPLAY_COUNT.equals(command)) {
			try {
				Integer count = Integer.parseInt(new String(data));
				if (count != null && count > 0) {
					MovieWorkChoice.mUserMovieDisplayCount = count;
				}
				LogUtil.logd("setDisplayMovieCount:" + count);
			} catch (Exception e) {
			}
		} else if (CMD_DISPLAY_COUNT.equals(command)) {
			try {
				Integer count = Integer.parseInt(new String(data));
				LogUtil.logd("autofix num from:" + mAutoFixNumber + " to:" + count);
				if (count != null && count > 0 && count != mAutoFixNumber) {
					mAutoFixNumber = count;
					autoFixNumPageSize(mAutoFixNumber);
				}
			} catch (Exception e) {
			}
		} else if (CMD_OLD_ITEM_RIGHT.equals(command)) {
			try {
				Integer pIdx = Integer.parseInt(new String(data));
				if (pIdx != null) {
					selectIdx(pIdx, null, null);
				}
			} catch (Exception e) {
			}
		} else if (CMD_ITEM_SELECTED.equals(command)) {
			selectItem(data);
		} else if (CMD_DISPLAY_TIP.equals(command)) {
			notifyEditPage();
		} else if(CMD_DISPLAY_CITY.equals(command)){
			notifyCityEditPage();
		}else if (CMD_POIMAP_ACTION.equals(command)) {
			mapActionResult(new String(data));
		}else if(CMD_POIMAP_LOADING.equals(command)){
			mapPoiViewLoading();
		}else if (CMD_LIST_ONTOUCH.equals(command)) {
			checkTimeout(true);
		} else if (CMD_EXITBACK.equals(command)) {
			WorkChoice.sExitBack = Boolean.parseBoolean(new String(data));
			LogUtil.logd("WorkChoice.sExitBack:" + WorkChoice.sExitBack);
		} else if (CMD_SEARCH_COUNT.equals(command)) {
			try {
				Integer count = Integer.parseInt(new String(data));
				if (count != null && count > 0) {
					NavManager.sSearchCount = count;
				}
				LogUtil.logd("set search poi count:" + count);
			} catch (Exception e) {
			}
		} else if (CMD_USE_2_0_VERSION.equals(command)) {
			try {
				Boolean b2Version = Boolean.parseBoolean(new String(data));
				if (b2Version != null) {
					mUserConfigOption.setIs2_0Version(b2Version);
				}
				LogUtil.logd("use 2.0 version choice:" + b2Version);
			} catch (Exception e) {
			}
		} else if (CMD_DISPLAY_PAGE.equals(command)) {
			JSONBuilder jsonBuilder = new JSONBuilder(data);
			Integer t = jsonBuilder.getVal("type", Integer.class);
			Integer ct = jsonBuilder.getVal("clicktype", Integer.class);
			if (t != null && t == 1) { // 1表示点击事件
				snapPage(ct == 1 ? false : true);
			}
			checkTimeout(true);
		} else if (CMD_OLD_NAVI_CLICK.equals(command)) {
			oldSelectItem(data);
		} else if (CMD_OLD_ITEM_CLICK.equals(command)) {
			oldSelectItem(data);
		} else if (CMD_OLD_POI_EDIT.equals(command)) {
			notifyEditPage();
		} else if (CMD_OLD_AUDIO_SELECT.equals(command)) {
			selectItem(data);
		} else if (CMD_SHOW_SELECT_LIST.equals(command)) {
			showThirdList(data);
		} else if(CMD_SHOW_THIRD_POI_LIST.equals(command)){
			showThirdPoiList(data);
		}else if (CMD_SET_POI_NO_RESULT_TTS.equals(command)) {
			mJustResultText = Boolean.parseBoolean(new String(data));
			LogUtil.logd("mJustResultText:" + mJustResultText);
		} else if (CMD_POI_USE_DEFAULT_COEXIST_ASR_AND_WAKEUP.equals(command)) {
			mPoiUseDefaultCoexistAsrAndWakeup =Boolean.parseBoolean(new String(data));
			LogUtil.logd("mPoiUseDefaultCoexistAsrAndWakeup:" + mPoiUseDefaultCoexistAsrAndWakeup);
		} else {
			if (TextUtils.equals("txz.record.ui.event.search.edit.cancel", command) || (TextUtils.equals("txz.record.ui.event.select.city.cancel", command))) {
				if (mCurrActiveChoice != null && mCurrActiveChoice instanceof PoiWorkChoice) {
					((PoiWorkChoice) mCurrActiveChoice).resumeContinuityTip();
				}
			}
			return SearchEditManager.getInstance().invokeCommand(packageName, command, data);
		}
		return null;
	}

	private void showThirdList(byte[] data) {
		try {
			JSONObject obj = new JSONObject(new String(data));
			int type = obj.optInt("type");
			String kws = obj.optString("keywords");
			String city = obj.optString("city");
			org.json.JSONArray array = obj.optJSONArray("pois");
			List<Poi> pois = new ArrayList<Poi>();
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					pois.add(Poi.fromString(array.optJSONObject(i).toString()));
				}
			}
			if (1 == type) { // 1为显示POI列表
				PoisData poisData = new PoisData();
				poisData.action = PoiAction.ACTION_NAVI;
				poisData.keywords = kws;
				poisData.city = city;
				poisData.isBus = false;
				poisData.mPois = pois;
				// 取消识别
				AsrManager.getInstance().cancel();
				showPoiList(poisData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 外放POI页面且选中后将结果返回给第三方
	 * @param data
	 */
	public void showThirdPoiList(byte[] data){
		try {
			JSONObject obj = new JSONObject(new String(data));
			String kws = obj.optString("keywords");
			String tips = obj.optString("tips");
			boolean isNeedCloseWin = obj.optBoolean("isCloseWin",true);
			org.json.JSONArray array = obj.optJSONArray("pois");
			List<Poi> pois = new ArrayList<Poi>();
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					pois.add(Poi.fromString(array.optJSONObject(i).toString()));
				}
			}
			PoisData poisData = new PoisData();
			poisData.action = PoiAction.ACTION_THIRD_POI;
			poisData.keywords = kws;
			poisData.isBus = false;
			poisData.mPois = pois;
			poisData.tips = tips;
			// 取消识别
			AsrManager.getInstance().cancel();
			showPoiList(poisData);
			if(mCurrActiveChoice instanceof PoiWorkChoice){
				((PoiWorkChoice) mCurrActiveChoice).setIsNeedCloseWin(isNeedCloseWin);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击事件
	 *
	 * @param data
	 */
	private void selectItem(byte[] data) {
		try {
			JSONBuilder json = new JSONBuilder(data);
			Integer idx = json.getVal("index", Integer.class);
			String spkWds = json.getVal("speech", String.class);
			String action = json.getVal("action", String.class, null);
			Integer operateSource = json.getVal("operateSource", Integer.class);
			LogUtil.logd("item.selected:" + idx + ",speech:" + spkWds + ",action:" + action + ",operateSource:"
					+ operateSource);
			if (idx != null) {
				selectIdx(idx, action, operateSource);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 老版本的点击事件
	 *
	 * @param data
	 */
	private void oldSelectItem(byte[] data) {
		try {
			JSONBuilder jsonBuilder = new JSONBuilder(data);
			Integer pIdx = jsonBuilder.getVal("position", Integer.class);
			if (pIdx != null) {
				// 老的交互是打开POI预览界面
				return;
			}
			String pJson = jsonBuilder.getVal("poi", String.class);
			// String action = jsonBuilder.getVal("action", String.class);
			if (mCurrActiveChoice != null && mCurrActiveChoice.isSelecting()
					&& mCurrActiveChoice instanceof PoiWorkChoice) {
				((PoiWorkChoice) mCurrActiveChoice).selectPoi(Poi.fromString(pJson), 0,
						AbstractChoice.SELECT_TYPE_CLICK, null);
			}
		} catch (Exception e) {
		}
	}
}