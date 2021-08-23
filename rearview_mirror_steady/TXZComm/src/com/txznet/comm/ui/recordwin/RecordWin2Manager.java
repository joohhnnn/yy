package com.txznet.comm.ui.recordwin;

import android.content.Intent;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.keyevent.KeyEventManager;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.util.BaseSceneInfoForward;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.txz.util.runnables.Runnable1;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 
 * UI2.0界面管理类,负责接收并分发TXZCore传过来的信息
 *
 */
public class RecordWin2Manager{

	private static RecordWin2Manager sInstance = new RecordWin2Manager();
	private int mLastViewType = -1;
	private boolean mForceUseUI1 = false; // 是否强制使用UI1.0
	private boolean mDisableThirdWin = false;
	
	private RecordWin2Manager(){
	}
	
	public void init() {
	}
	
	public static RecordWin2Manager getInstance(){
		return sInstance;
	}
	
	private int mLastMsgType = 0;
	/**
	 * 接收分发Core的信息
	 */
	public void showData(final String data) {
		if (!WinLayoutManager.getInstance().viewInited) {
			return;
		}
		try {
			final JSONBuilder jsonBuilder = new JSONBuilder(data);
			String action = jsonBuilder.getVal("action", String.class);
			if ("addMsg".equals(action)) {
				ViewData viewData = ViewFactory.parseData(data);
				if(viewData==null){
					return;
				}
				UI2Manager.runOnUIThread(new Runnable1<ViewData>(viewData) {
					@Override
					public void run() {
						if (!RecordWin2.getInstance().isShowing()) {
							return;
						}
						// 用于后续例如打字效果这样的需求
						boolean update = jsonBuilder.getVal("update", Boolean.class, false);
						boolean shouldUpdate = shouldUpdate(mP1.getType());
						if (update || shouldUpdate) {
							MsgViewBase msgViewBase = WinLayoutManager.getInstance().getCurMsgView();
							if (msgViewBase != null && msgViewBase.getFlags() != null
									&&msgViewBase.getViewType()==mP1.getType()
									&&msgViewBase.getViewType()!=0
									&&mLastMsgType == mP1.getType()
									&& ((msgViewBase.getFlags() & ViewBase.MASK_UPDATEABLE) == ViewBase.UPDATEABLE)) {
								LogUtil.logd("[UI2.0] target view support updateView,just update view");
								msgViewBase.updateView(mP1);
								WinLayoutManager.getInstance().updateCurMsgView(msgViewBase);
								return;
							}
						}

						if (mLastMsgType == ViewData.TYPE_CHAT_TO_SYS_PART || mLastMsgType == ViewData.TYPE_CHAT_HELP_TIPS) {
							removeLastView();
						}

						ViewAdapter viewAdapter = ViewFactory.generateView(mP1);
						if (viewAdapter != null) {
							int targetView = 0;
							if (viewAdapter.isFullContent != null) {
								targetView = viewAdapter.isFullContent ? RecordWinController.TARGET_CONTENT_FULL
										: RecordWinController.TARGET_CONTENT_CHAT;
							} else {
								targetView = getTargetByType(viewAdapter.type);
							}
							if (viewAdapter.isListView && viewAdapter.object instanceof IListView) {
								WinLayoutManager.getInstance().updateListView((IListView) viewAdapter.object);
							}
							if (viewAdapter.object instanceof MsgViewBase) {
								WinLayoutManager.getInstance().updateCurMsgView((MsgViewBase) viewAdapter.object);
							}
							mLastViewType = viewAdapter.type;
							addViewInner(targetView, viewAdapter.view);
						}
						mLastMsgType = mP1.getType();
					}
				}, 0);
			} else if ("updateVolume".equals(action)) {
				UI2Manager.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (!RecordWin2.getInstance().isShowing()) {
							return;
						}
						updateVolumeInner(jsonBuilder.getVal("value", Integer.class));
					}
				}, 0);
			} else if ("snapPage".equals(action)) {
				UI2Manager.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (!RecordWin2.getInstance().isShowing()) {
							return;
						}
						Boolean next = jsonBuilder.getVal("next", Boolean.class);
						snapPage(next);
					}
				}, 0);
			} else if ("updateProgress".equals(action)) {
				final Integer progress = jsonBuilder.getVal("value", Integer.class);
				final Integer selection = jsonBuilder.getVal("selection", Integer.class);
				if (progress != null && selection != null) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (!RecordWin2.getInstance().isShowing()) {
								return;
							}
							updateProgressInner(progress, selection);
						}
					}, 0);
				}
			} else if ("updateState".equals(action)) {
				final Integer state = jsonBuilder.getVal("state", Integer.class);
				final String type = jsonBuilder.getVal("type", String.class);
				if ("wheelControl".equals(type)) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							updateWheelControlState(state);
						}
					}, 0);
					return;
				}
				if (state != null) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (!RecordWin2.getInstance().isShowing()) {
								return;
							}
							updateRecordStateInner(state);
						}
					}, 0);
				}
			} else if ("updateItemSelect".equals(action)) {
				final Integer selection = jsonBuilder.getVal("selection", Integer.class);
				if (selection != null) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (!RecordWin2.getInstance().isShowing()) {
								return;
							}
							updateItemSelectInner(selection);
						}
					}, 0);
				}
			}else if ("onKeyEvent".equals(action)) {
				final Integer keyEvent = jsonBuilder.getVal("keyEvent", Integer.class);
				LogUtil.logd("receive keyEvent:" + keyEvent);
				if (keyEvent != null) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (keyEvent == TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID) {
								clickWheelVoiceBtn();
								return;
							}
							if (!RecordWin2.getInstance().isShowing()) {
								return;
							}
							onKeyEvent(keyEvent);
						}
					}, 0);
				}
			}else if ("sendInformation".equals(action)) {
				Integer type = jsonBuilder.getVal("type", Integer.class,-1);
				if (type == 0) {//更新帮助小红点的显示
					ConfigUtil.setShowHelpNewTag(jsonBuilder.getVal("showHelpNewTag", Boolean.class, false));
				}else if(type == 1){
					BaseSceneInfoForward infoForward = com.txznet.comm.ui.util.ConfigUtil.getSceneInfoForward();
					infoForward.updateSceneInfo(data);
				}
			}
		} catch (Exception e) {
			LogUtil.loge("[UI2.0] error :"+e.getMessage());
			e.printStackTrace();
		}
	}

	public Object operateView(int view) {
		return operateView(RecordWinController.OPERATE_CLICK, view, 0, 0);
	}
	public static  void mapActionResult(int  action ,boolean result){
		JSONBuilder jb = new JSONBuilder();
		jb.put("action",action);
		jb.put("result", result);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.poimap.action.result",
				jb.toBytes(), null);
	}

	/**
	 *
	 */
	public  void doEditSearchResult(String  keyword){
		JSONBuilder jb = new JSONBuilder();
		jb.put("key",keyword);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.search.edit.result",
				jb.toBytes(), null);
	}

	public void doEditSearchClickCancel(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.search.edit.cancel",
				null, null);
	}
	public  void doSelectCityResult(String  city){
		JSONBuilder jb = new JSONBuilder();
		jb.put("city",city);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.select.city.result",
				jb.toBytes(), null);
	}

	public void doSelectCityClickCancel(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.select.city.cancel",
				null, null);
	}
	
	/**
	 * @deprecated use EVENT_TYPE_ANIMATION_STATE instead
	 */
	@Deprecated 
	public static final int EVENT_ANIMATION_STATE = 1;
	/**
	 * @deprecated use EVENT_TYPE_UI_EVENT instead
	 */
	@Deprecated 
	public static final int EVENT_UI_EVENT = 2;
	public static final int EVENT_TYPE_ANIMATION_STATE = 1;
	public static final int EVENT_TYPE_UI_EVENT = 2;
	public static final String EVENT_EVENT_CLEAR_PROGRESS = "txz.record.ui.event.clearProgress";

	private IViewStateListener mViewStateTransfer;

	/**
	 * 发送消息到Core，目前只是同进程通过反射调用，为了以后兼容跨进程处理，封装成一个方法
	 */
	public void sendEventToCore(int type, String event,Object... objects) {
		switch (type) {
		case EVENT_TYPE_UI_EVENT:
			if ("txz.record.ui.event.clearProgress".equals(event)) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.clearProgress", null, null);
			}
			break;
		case EVENT_TYPE_ANIMATION_STATE:
			if (mViewStateTransfer == null) {
				try {
					Class<?> clazzWinManager = Class.forName("com.txznet.txz.module.ui.WinManager");
					Method instanceMethod = clazzWinManager.getDeclaredMethod("getInstance");
					Object object = instanceMethod.invoke("getInstance");
					Field field = clazzWinManager.getField("mViewStateTransfer");
					mViewStateTransfer = (IViewStateListener) field.get(object);
				} catch (Exception e) {
					LogUtil.loge("send ui event to core error!");
				}
			}
			if (mViewStateTransfer != null && objects.length >= 2) {
				mViewStateTransfer.onAnimateStateChanged((Animation) objects[0], (Integer) objects[1]);
			}
			break;

		default:
			break;
		}
	}
	
	
	/**
	 * 更新语音界面显示区域
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void updateDisplayArea(int x, int y, int width, int height) {
		if(!WinLayoutManager.getInstance().viewInited){
			return;
		}
		RecordWin2.getInstance().updateDisplayArea(x, y, width, height);
		com.txznet.comm.ui.util.ConfigUtil.updateDisplayArea(x, y, width, height);
	}
	
	public void sendEventToCore(String event){
		sendEventToCore(EVENT_TYPE_UI_EVENT, event);
	}
	
	public void sendEventToCore(int type,Object... objects){
		sendEventToCore(type, null, objects);
	}

	public Object operateView(int actionType, int view) {
		return operateView(actionType, view, 0, 0);
	}

	public Object operateView(int actionType, int view, int listType, int listIndex) {
		return operateView(actionType, view, listType, listIndex, RecordWinController.OPERATE_SOURCE_TOUCH);
	}

	public Object operateView(int actionType, int view, int listType, int listIndex, int operateSource) {
		return operateView(actionType, view, listType, listIndex, operateSource,null);
	}

	/**
	 *
	 * @param actionType
	 * 			操作类型，点击，触摸等
	 * @param view
	 * 			具体操作的VIew，帮助、设置、列表等
	 * @param listType
	 * @param listIndex
	 * @param operateSource
	 * @return
	 */
	public Object operateView(int actionType, int view, int listType, int listIndex, int operateSource, String extraString) {
		LogUtil.logd("operateView actionType:" + actionType + ",view:" + view + ",listType:" + listType +
				",listIndex" + listIndex + ",operateSource:" + operateSource);
		if (mForceUseUI1) {
			return null;
		}
		byte[] data = null;
		if (operateSource != 0 || listIndex >= 0 || view == RecordWinController.VIEW_LIST_NEXTPAGE ||
				view == RecordWinController.VIEW_LIST_PREPAGE) {
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("operateSource", operateSource);
			jsonBuilder.put("index", listIndex);
			if(view == RecordWinController.VIEW_LIST_NEXTPAGE){
				jsonBuilder.put("type", 1);
				jsonBuilder.put("clicktype", 2);
			}
			if(view == RecordWinController.VIEW_LIST_PREPAGE){
				jsonBuilder.put("type", 1);
				jsonBuilder.put("clicktype", 1);
			}
			if(!TextUtils.isEmpty(extraString)){
				jsonBuilder.put("extraString", extraString);
			}
			data = jsonBuilder.toBytes();
		}
		if (actionType == RecordWinController.OPERATE_CLICK) {
			switch (view) {
				case RecordWinController.VIEW_HELP:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.open", data,
							null);
					break;
				case RecordWinController.VIEW_SETTING:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.setting", data, null);
					break;
				case RecordWinController.VIEW_CLOSE:
					GlobalContext.get().sendBroadcast(new Intent("com.txznet.txz.record.dismiss.button"));
					RecordWin2.getInstance().dismiss();
					break;
				case RecordWinController.VIEW_BACK:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.back", data, null);
					break;
				case RecordWinController.VIEW_RECORD:
					clickRecord();
					break;
				case RecordWinController.VIEW_LIST_ITEM:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
							data, null);
					break;
				case RecordWinController.VIEW_LIST_NEXTPAGE:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
							data, null);
					break;
				case RecordWinController.VIEW_LIST_PREPAGE:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
							data, null);
					break;
				case RecordWinController.VIEW_TIPS:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.tip",
							data, null);
					break;
				case RecordWinController.VIEW_CITY:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.city",
							null, null);
					break;
				case RecordWinController.VIEW_HELP_BACK:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.help.back", data, null);
					break;
				case RecordWinController.VIEW_TTS_QRCODE:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.subscribe.qrcode", null, null);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.list.ontouch",
							(MotionEvent.ACTION_DOWN + "").getBytes(), null);
					break;
				case RecordWinController.VIEW_FILM_REPLACE_PHONE:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.film.replace.phone", data, null);
					break;
				case RecordWinController.VIEW_HELP_QRCODE:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode", data, null);
					break;
                case RecordWinController.VIEW_TICKET_INFO_COMMIT:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.ticket.info.commit", data, null);
                    break;
                 case  RecordWinController.VIEW_TICKET_INFO_CANCEL:
					 ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.ticket.info.cancel", data, null);
					break;
				default:
					break;
			}
		} else if (actionType == RecordWinController.OPERATE_TOUCH) {
			switch (view) {
				case RecordWinController.VIEW_LIST_ITEM:
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.list.ontouch",
							null, null);
					break;

				default:
					break;
			}
		}
		return null;
	}
	
	private boolean isJustClick;

	private void clickRecord() {
		if (isJustClick)
			return;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.record", null, null);
		isJustClick = true;
		UI2Manager.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				isJustClick = false;
			}
		}, 1000);
	}

	/**
	 * 强制使用UI1.0，不再处理SDK端的请求
	 */
	public void forceUseUI1() {
		LogUtil.logd("forceUseUI1");
		mForceUseUI1 = true;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.recordwin2.forceUI1", null, null);
	}

	/**
	 * 使用TXZCore的界面
	 */
	public void disableThirdWin(boolean disable) {
		if (mDisableThirdWin != disable) {
			mDisableThirdWin = disable;
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.recordwin2.disableThirdWin",
					("" + disable).getBytes(), null);
		}
	}

	/**
	 * 点击了腾讯方控上的声控按钮
	 */
	public void clickWheelVoiceBtn() {
		if (!RecordWin2.getInstance().isShowing()) {
			TXZAsrManager.getInstance().triggerRecordButton();
		} else {
			ViewBase mCurChatView = WinLayoutManager.getInstance().getCurMsgView();
			if (mCurChatView != null && mCurChatView instanceof IListView) {
				TXZAsrManager.getInstance().restart("");
			} else {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_RECORD, 0, 0);
			}
		}
	}

	public boolean isDisableThirdWin() {
		return mDisableThirdWin;
	}

	/**
	 * 当前View应该重新生成还是只是更新
	 * @param type
	 * @return
	 */
	private boolean shouldUpdate(int type){
		switch (type) {
		case ViewData.TYPE_CHAT_FROM_SYS:
		case ViewData.TYPE_CHAT_TO_SYS:
		case ViewData.TYPE_CHAT_WEATHER:
		case ViewData.TYPE_CHAT_SHARE:
		case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
			return false;
		case ViewData.TYPE_FULL_LIST_AUDIO:
		case ViewData.TYPE_FULL_LIST_CALL:
		case ViewData.TYPE_FULL_LIST_POI:
		case ViewData.TYPE_FULL_LIST_WECHAT:
		case ViewData.TYPE_FULL_LIST_CINEMA:
		case ViewData.TYPE_FULL_LIST_HELP:
		case ViewData.TYPE_FULL_LIST_SIM:
		case ViewData.TYPE_FULL_LIST_TTS:
		case ViewData.TYPE_FULL_NO_TTS_QRCORD:
		case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
		case ViewData.TYPE_CHAT_MAP:
		case ViewData.TYPE_FULL_LIST_MAPPOI:
		case ViewData.TYPE_CHAT_HELP_TIPS:
		case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
		case ViewData.TYPE_FULL_LIST_REMINDER:
		case ViewData.TYPE_FULL_LIST_FLIGHT:
		case ViewData.TYPE_FULL_LIST_TRAIN:
		case ViewData.TYPE_CHAT_TO_SYS_PART:
		case ViewData.TYPE_FULL_LIST_STYLE:
		case ViewData.TYPE_FULL_FILM_LIST:
		case ViewData.TYPE_FULL_MOVIE_THEATER_LIST:
		case ViewData.TYPE_FULL_MOVIE_TIME_LIST:
		case ViewData.TYPE_FULL_LIST_TRAIN_TICKET:
		case ViewData.TYPE_FULL_LIST_FLIGHT_TICKET:
		case ViewData.TYPE_FULL_TICKET_PAY:
		case ViewData.TYPE_FULL_MOVIE_SEATING_PLAN:
		case ViewData.TYPE_FULL_MOVIE_PHONE_NUM_QRCODE:
		case ViewData.TYPE_FULL_MOVIE_WAITING_PAY_QRCODE:
		case ViewData.TYPE_FULL_LIST_COMPETITION:
		case ViewData.TYPE_CHAT_FEEDBACK:
			return true;
		default:
			break;
		}
		return false;
	}
	
	/**
	 * 根据View的类型得到默认的希望添加到什么targetView上面去
	 */
	private int getTargetByType(int type) {
		switch (type) {
		case ViewData.TYPE_CHAT_FROM_SYS:
		case ViewData.TYPE_CHAT_TO_SYS:
		case ViewData.TYPE_CHAT_WEATHER:
		case ViewData.TYPE_CHAT_SHARE:
		case ViewData.TYPE_QRCODE:
		case ViewData.TYPE_CHAT_BIND_DEVICE_QRCODE:
		case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:
		case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:
		case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
		case ViewData.TYPE_CHAT_FEEDBACK:
			return RecordWinController.TARGET_CONTENT_CHAT;
		case ViewData.TYPE_FULL_LIST_AUDIO:
		case ViewData.TYPE_FULL_LIST_CALL:
		case ViewData.TYPE_FULL_LIST_POI:
		case ViewData.TYPE_FULL_LIST_WECHAT:
		case ViewData.TYPE_FULL_LIST_CINEMA:
		case ViewData.TYPE_FULL_LIST_HELP:
		case ViewData.TYPE_FULL_LIST_SIM:
		case ViewData.TYPE_FULL_LIST_TTS:
		case ViewData.TYPE_FULL_NO_TTS_QRCORD:
		case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
		case ViewData.TYPE_CHAT_MAP:
		case ViewData.TYPE_FULL_LIST_MAPPOI:
		case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
		case ViewData.TYPE_FULL_LIST_REMINDER:
		case ViewData.TYPE_FULL_LIST_FLIGHT:
		case ViewData.TYPE_FULL_LIST_TRAIN:
		case ViewData.TYPE_FULL_LIST_STYLE:
		case ViewData.TYPE_AUTHORIZATION_VIEW:
		case ViewData.TYPE_FULL_LIST_SIMPLE_LIST:
		case ViewData.TYPE_FULL_FILM_LIST:
		case ViewData.TYPE_FULL_MOVIE_THEATER_LIST:
		case ViewData.TYPE_FULL_MOVIE_TIME_LIST:
		case ViewData.TYPE_FULL_LIST_TRAIN_TICKET:
		case ViewData.TYPE_FULL_LIST_FLIGHT_TICKET:
        case ViewData.TYPE_FULL_TICKET_PAY:
        case ViewData.TYPE_FULL_MOVIE_SEATING_PLAN:
		case ViewData.TYPE_FULL_MOVIE_PHONE_NUM_QRCODE:
		case ViewData.TYPE_FULL_MOVIE_WAITING_PAY_QRCODE:
		case ViewData.TYPE_FULL_LIST_COMPETITION:
			return RecordWinController.TARGET_CONTENT_FULL;
		default:
			break;
		}
		return RecordWinController.TARGET_CONTENT_CHAT;
	}

	private void addViewInner(final int targetView, final View view) {
		WinLayoutManager.getInstance().addView(targetView, view);
	}

	private void removeLastView() {
		WinLayoutManager.getInstance().removeLastView();
	}
	
	public Object addView(final int targetView, final View view) {
		if (mForceUseUI1) {
			return null;
		}
		switch (targetView) {
		case RecordWinController.TARGET_VIEW_MIC:
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					WinLayoutManager.getInstance().addThirdRecordView(view);
				}
			}, 0);
			break;
		case RecordWinController.TARGET_CONTENT_CHAT:
		case RecordWinController.TARGET_CONTENT_FULL:
		case RecordWinController.TARGET_VIEW_BANNER_AD:
		default:
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					addViewInner(targetView, view);
				}
			}, 0);
			break;
		}
		return null;
	}

	// public Object addViewToWindow(final View view, final
	// FrameLayout.LayoutParams layoutParams) {
	// if (mForceUseUI1) {
	// return null;
	// }
	// UI2Manager.runOnUIThread(new Runnable() {
	// @Override
	// public void run() {
	// WinLayoutManager.getInstance().addViewToWindow(view, layoutParams);
	// }
	// }, 0);
	// return null;
	// }
	
	
	public void updateProgress(final int progress,final int selection) {
		if(!WinLayoutManager.getInstance().viewInited){
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!RecordWin2.getInstance().isShowing()) {
					return;
				}
				updateProgressInner(progress, selection);
			}
		}, 0);
	}

	public void updateItemSelect(final int selection) {
		if(!WinLayoutManager.getInstance().viewInited){
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!RecordWin2.getInstance().isShowing()) {
					return;
				}
				updateItemSelectInner(selection);
			}
		}, 0);
	}
	
	public void updateRecordState(final int state){
		if(!WinLayoutManager.getInstance().viewInited){
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!RecordWin2.getInstance().isShowing()) {
					return;
				}
				updateRecordStateInner(state);
			}
		}, 0);
	}
	
	public void updateVolume(final int volume){
		if(!WinLayoutManager.getInstance().viewInited){
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				if (!RecordWin2.getInstance().isShowing()) {
					return;
				}
				updateVolumeInner(volume);
			}
		}, 0);
	}
	
	
	private void updateRecordStateInner(int state) {
		 WinLayoutManager.getInstance().updateState(state);
	}
	
	private void updateVolumeInner(int volume){
		WinLayoutManager.getInstance().updateVolume(volume);
	}
	
	private void updateProgressInner(int progress, int selection) {
		WinLayoutManager.getInstance().updateProgress(progress, selection);
	}

	private void updateItemSelectInner(int selection) {
		WinLayoutManager.getInstance().updateItemSelect(selection);
	}
	
	private void updateWheelControlState(int state){
		KeyEventManager.getInstance().onWheelControlStateChanged(state);
	}
	
	private void snapPage(boolean next){
		WinLayoutManager.getInstance().snapPage(next);
	}
	
	public boolean onKeyEvent(int keyEvent){
		return KeyEventManager.getInstance().onKeyEvent(keyEvent);
	}
	
	public void show(){
		if (!WinLayoutManager.getInstance().viewInited) {
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				RecordWin2.getInstance().show();
			}
		}, 0);
	}
	
	public void dismiss(){
		if (!WinLayoutManager.getInstance().viewInited) {
			return;
		}
		UI2Manager.runOnUIThread(new Runnable() {
			@Override
			public void run() {
				RecordWin2.getInstance().dismiss();
				WinLayoutManager.getInstance().updateCurMsgView(null);
				WinLayoutManager.getInstance().updateListView(null);
				WinLayoutManager.getInstance().releaseMsgView();
			}
		}, 0);
	}
	
	public int getLastViewType() {
		return mLastViewType;
	}

	public boolean isForceUseUI1() {
		return mForceUseUI1;
	}
}
