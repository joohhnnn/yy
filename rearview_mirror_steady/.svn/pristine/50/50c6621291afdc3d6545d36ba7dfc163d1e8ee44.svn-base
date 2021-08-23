package com.txznet.comm.ui.viewfactory;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData;
import com.txznet.comm.ui.viewfactory.data.AuthorizationViewData;
import com.txznet.comm.ui.viewfactory.data.BindDeviceViewData;
import com.txznet.comm.ui.viewfactory.data.CallListViewData;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ChatMapViewData;
import com.txznet.comm.ui.viewfactory.data.ChatShockViewData;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.ChatSysInterruptTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ChatToSysPartViewData;
import com.txznet.comm.ui.viewfactory.data.ChatToSysViewData;
import com.txznet.comm.ui.viewfactory.data.ChatWeatherViewData;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData;
import com.txznet.comm.ui.viewfactory.data.ConstellationFortuneData;
import com.txznet.comm.ui.viewfactory.data.ConstellationMatchingData;
import com.txznet.comm.ui.viewfactory.data.CompetitionDetailViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.FeedbackViewData;
import com.txznet.comm.ui.viewfactory.data.FilmListViewData;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpDetailImageViewData;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ListViewData;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.MapPoiListViewData;
import com.txznet.comm.ui.viewfactory.data.MoviePhoneNumQRViewData;
import com.txznet.comm.ui.viewfactory.data.MovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.data.MovieTheaterListViewData;
import com.txznet.comm.ui.viewfactory.data.MovieTimeListViewData;
import com.txznet.comm.ui.viewfactory.data.MovieWaitingPayQRViewData;
import com.txznet.comm.ui.viewfactory.data.NavAppListViewData;
import com.txznet.comm.ui.viewfactory.data.NoTtsQrcodeViewData;
import com.txznet.comm.ui.viewfactory.data.OfflinePromoteViewData;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.ui.viewfactory.data.QrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData;
import com.txznet.comm.ui.viewfactory.data.SimListViewData;
import com.txznet.comm.ui.viewfactory.data.StyleListViewData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData;
import com.txznet.comm.ui.viewfactory.data.TtsListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.data.WeChatListViewData;
import com.txznet.comm.ui.viewfactory.view.IAudioListView;
import com.txznet.comm.ui.viewfactory.view.ICallListView;
import com.txznet.comm.ui.viewfactory.view.IChatFromSysView;
import com.txznet.comm.ui.viewfactory.view.IChatMapView;
import com.txznet.comm.ui.viewfactory.view.IChatShockView;
import com.txznet.comm.ui.viewfactory.view.IChatToSysPartView;
import com.txznet.comm.ui.viewfactory.view.IChatToSysView;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView;
import com.txznet.comm.ui.viewfactory.view.ICinemaListView;
import com.txznet.comm.ui.viewfactory.view.ICompetitionDetailView;
import com.txznet.comm.ui.viewfactory.view.ICompetitionView;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailImageView;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailListView;
import com.txznet.comm.ui.viewfactory.view.IHelpListView;
import com.txznet.comm.ui.viewfactory.view.IHelpTipsView;
import com.txznet.comm.ui.viewfactory.view.IMapPoiListView;
import com.txznet.comm.ui.viewfactory.view.INoTtsQrcodeView;
import com.txznet.comm.ui.viewfactory.view.IPoiListView;
import com.txznet.comm.ui.viewfactory.view.ISimListView;
import com.txznet.comm.ui.viewfactory.view.ITtsListView;
import com.txznet.comm.ui.viewfactory.view.IWechatListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.LanguageConvertor;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;

/**
 * 根据ViewConfig的配置以及json数据生成需要的View
 * @author ASUS User
 *
 */
public class ViewFactory {
	

	public static class ViewAdapter{
		public int type;
		public View view;
		public boolean isListView = false;
		public Object object = null;
		/**
		 * 希望手动设定本条消息是否全屏时修改该属性
		 */
		public Boolean isFullContent;
		/**
		 * 一些属性或者标志位，预留
		 */
		public Integer flags = null;
	}
	
	public static ViewAdapter createView(String data) {
		ViewData object = parseData(data);
		if (object != null) {
			return generateView(object);
		}
		return null;
	}

	public static ViewData parseData(String data){
		LogUtil.logd(data);
		JSONBuilder jsonBuilder = new JSONBuilder(data);
		String type = jsonBuilder.getVal("type", String.class);
		if (TextUtils.equals(type, "toSys")) {
			//解析json数据，生成对饮的ViewData
			ChatToSysViewData viewData = new ChatToSysViewData();
			viewData.setTextContent(jsonBuilder.getVal("msg", String.class));
			return viewData;
		}else if (TextUtils.equals(type, "fromSys")) {
			ChatFromSysViewData viewData = new ChatFromSysViewData();
			viewData.setTextContent(jsonBuilder.getVal("msg", String.class));
			return viewData;
		} else if (TextUtils.equals(type,"toSysPart")){
			ChatToSysPartViewData viewData = new ChatToSysPartViewData();
			viewData.setTextContent(jsonBuilder.getVal("msg", String.class));
			return viewData;
		}else if (TextUtils.equals(type, "weather")) {
			ChatWeatherViewData viewData = new ChatWeatherViewData();
			viewData.setWeather(data);
			return viewData;
		}else if (TextUtils.equals(type, "shock")) {
			ChatShockViewData viewData = new ChatShockViewData();
			viewData.parseData(data);
			return viewData;
		}else if (TextUtils.equals(type, "list")) {
			String listData = jsonBuilder.getVal("data", String.class);
			JSONBuilder jsonBuilder2 = new JSONBuilder(listData);
			int listType = jsonBuilder2.getVal("type", Integer.class);
			ListViewData viewData = null;
			switch (listType) {
			case 2://poi
				Boolean isList = jsonBuilder2.getVal("listmodel", Boolean.class);
				if (isList != null && !isList) {
					viewData = new MapPoiListViewData();
				} else {
					viewData = new PoiListViewData();
				}
				break;
			case 4://audio
				viewData = new AudioListViewData();
				break;
			case 1://wechat
				viewData = new WeChatListViewData();
				break;
			case 5://sim
				viewData = new SimListViewData();
				break;
			case 6://tts
				viewData = new TtsListViewData();
				break;
			case 0://联系人
				viewData = new CallListViewData();
				break;
			case 8://帮助
				viewData = new HelpListViewData();
				break;
			case 7://电影
				viewData = new CinemaListViewData();
				break;
			case 9://帮助详情列表
				viewData = new HelpDetailListViewData();
				break;
			case 11://简单的数据类型（类似导航APP选择）
				viewData = new NavAppListViewData();
				break;
			case 10://帮助图片详情列表
				viewData = new HelpDetailImageViewData();
				break;
			case 12://提醒列表
				viewData = new ReminderListViewData();
				break;
			case 13://flight
				viewData = new FlightListViewData();
				break;
			case 14: // 火车票
				viewData = new TrainListViewData();
				break;
			case 15: //主题样式
				viewData = new StyleListViewData();
				break;
			case 16: //电影票场景的电影列表
				viewData = new FilmListViewData();
				break;
			case 17: //电影票场景中的电影院选择页面
				viewData = new MovieTheaterListViewData();
				break;
			case 18://电影票场景中的场次选择页面
				viewData = new MovieTimeListViewData();
				break;
			case 23://赛事界面
				viewData = new CompetitionViewData();
				break;
				case 25://齐悟火车票
					    viewData = new QiWuTrainTicketData();
					    break;
				case 26://齐悟飞机票
						viewData = new QiWuFlightTicketData();
						break;
				case 27:
					viewData = new QiwuTrainTicketPayViewData();
					break;
			default:
				break;
			}
			if (viewData!=null) {
				viewData.parseData(listData);
				if (viewData.count > 0) {
					return viewData;
				}else {
					ChatFromSysViewData chatFromSysViewData = new ChatFromSysViewData();
					switch (listType) {
					case 2:
						chatFromSysViewData.setTextContent(LanguageConvertor.toLocale(getNoResultBuilderTxt(viewData.keywords).toString()));
						chatFromSysViewData.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
										"txz.record.ui.event.display.tip", null, null);
							}
						});
						break;
					case 4:
						String keywords = viewData.keywords;
						if (TextUtils.isEmpty(keywords)) {
							keywords = "没有找到相关结果";
						}
						chatFromSysViewData.setTextContent(LanguageConvertor.toLocale(keywords));
						break;
					case 1:
						chatFromSysViewData.setTextContent("未找到微信联系人");
						break;
					}
					return chatFromSysViewData;
				}
			}
			
		}else if (TextUtils.equals("data", type)) {
			int dataType = jsonBuilder.getVal("dataType", Integer.class, 0);
			switch (dataType) {
			case 2://ttsNoResult
				NoTtsQrcodeViewData viewData = new NoTtsQrcodeViewData();
				viewData.setKey(jsonBuilder.getVal("key", Integer.class));
				JSONBuilder value = new JSONBuilder(jsonBuilder.getVal("value", String.class));
				viewData.setTitle(value.getVal("title", String.class));
				viewData.setQrCode(value.getVal("qrCode", String.class));
				return viewData;
			case 3://Qrcode
				QrCodeViewData qrViewData = new QrCodeViewData();
				qrViewData.setQrCode(jsonBuilder.getVal("qrCode", String.class, ""));
				return qrViewData;
			case 4://highLight Text
				ChatSysHighlightViewData highlightViewData = new ChatSysHighlightViewData();
				highlightViewData.setTextContent(jsonBuilder.getVal("rawText", String.class, ""));
				return highlightViewData;
			case 5://InterruptTips SysText
				ChatSysInterruptTipsViewData interruptViewData = new ChatSysInterruptTipsViewData();
				interruptViewData.setTextContent(jsonBuilder.getVal("text", String.class));
				interruptViewData.setTitleContent(jsonBuilder.getVal("tips", String.class, ""));
				return interruptViewData;
			case 6://
				HelpTipsViewData helpTipsViewData = new HelpTipsViewData();
				helpTipsViewData.parseItemData(jsonBuilder);
				return helpTipsViewData;
			case 8://Authorization
                AuthorizationViewData authorizationViewData = new AuthorizationViewData();
                authorizationViewData.mUrl = jsonBuilder.getVal(AuthorizationViewData.KEY_URL, String.class, "");
                authorizationViewData.mTips = jsonBuilder.getVal(AuthorizationViewData.KEY_TIPS, String.class, "");
                authorizationViewData.vTips = jsonBuilder.getVal(AuthorizationViewData.KEY_VIEW_TIPS, String.class, "");
                authorizationViewData.mTitle = jsonBuilder.getVal(AuthorizationViewData.KEY_TITLE, String.class, "");
                authorizationViewData.mSubTitle = jsonBuilder.getVal(AuthorizationViewData.KEY_SUB_TITLE, String.class, "");
                return authorizationViewData;
             case 9:
				 BindDeviceViewData bindDeviceViewData = new BindDeviceViewData();
				 bindDeviceViewData.qrCode = jsonBuilder.getVal("qrCode", String.class, "");
				 bindDeviceViewData.imageUrl = jsonBuilder.getVal("imageUrl", String.class, "");
             	return bindDeviceViewData;
			case 10:
				ConstellationFortuneData constellationFortuneData = new ConstellationFortuneData();
				constellationFortuneData.desc = jsonBuilder.getVal("desc", String.class, "");
				constellationFortuneData.fortuneType = jsonBuilder.getVal("fortuneType", String.class, "");
				constellationFortuneData.name = jsonBuilder.getVal("name", String.class, "");
				constellationFortuneData.level = jsonBuilder.getVal("level", int.class, 0);
				constellationFortuneData.vTips = jsonBuilder.getVal("vTips", String.class, "");
				return constellationFortuneData;
			case 11:
				ConstellationMatchingData constellationMatchingData = new ConstellationMatchingData();
				constellationMatchingData.desc = jsonBuilder.getVal("desc", String.class, "");
				constellationMatchingData.matchName = jsonBuilder.getVal("matchName", String.class, "");
				constellationMatchingData.name = jsonBuilder.getVal("name", String.class, "");
				constellationMatchingData.level = jsonBuilder.getVal("level", int.class, 0);
				constellationMatchingData.vTips = jsonBuilder.getVal("vTips", String.class, "");
				return constellationMatchingData;
			case 12:
				FeedbackViewData feedbackViewData = new FeedbackViewData();
				feedbackViewData.tips = jsonBuilder.getVal("tips", String.class, "");
				feedbackViewData.vTips = jsonBuilder.getVal("vTips", String.class, "");
				feedbackViewData.qrCode = jsonBuilder.getVal("qrCode", String.class, "");
				return feedbackViewData;
			case 19:
				MovieSeatPlanViewData movieSeatPlanViewData = new MovieSeatPlanViewData();
				movieSeatPlanViewData.seatPlanImageUrl = jsonBuilder.getVal("SeatPlanUrl", String.class, "");
				movieSeatPlanViewData.vTips = jsonBuilder.getVal("vTips",String.class);
				return  movieSeatPlanViewData;
			case 20:
				MoviePhoneNumQRViewData moviePhoneNumQRViewData = new MoviePhoneNumQRViewData();
				moviePhoneNumQRViewData.phoneNumQRUrl = jsonBuilder.getVal("phoneNumUrl",String.class, "");
				moviePhoneNumQRViewData.vTips = jsonBuilder.getVal("vTips",String.class);
				return moviePhoneNumQRViewData;
			case 21:
				MovieWaitingPayQRViewData movieWaitingPayQRViewData = new MovieWaitingPayQRViewData();
				movieWaitingPayQRViewData.phoneNum = jsonBuilder.getVal("phoneNum",String.class, "");
				movieWaitingPayQRViewData.replacePhoneUrl = jsonBuilder.getVal("replacePhoneUrl",String.class, "");
				movieWaitingPayQRViewData.WXPayURL = jsonBuilder.getVal("WXPayURL",String.class, "");
				movieWaitingPayQRViewData.ZFBPayURL = jsonBuilder.getVal("ZFBPayURL",String.class, "");
				movieWaitingPayQRViewData.vTips = jsonBuilder.getVal("vTips",String.class);
				movieWaitingPayQRViewData.moiveName = jsonBuilder.getVal("moiveName",String.class);
				movieWaitingPayQRViewData.cinemaName = jsonBuilder.getVal("cinemaName",String.class);
				movieWaitingPayQRViewData.hallName = jsonBuilder.getVal("hallName",String.class);
				JSONArray seats = jsonBuilder.getVal("seats",JSONArray.class);
				movieWaitingPayQRViewData.seats  = new LinkedList<String>();
				for(int i = 0; i < seats.length(); i++){
					try {
						movieWaitingPayQRViewData.seats.add(seats.getString(i)+" ");
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				movieWaitingPayQRViewData.showTime = jsonBuilder.getVal("showTime",String.class);
				movieWaitingPayQRViewData.showVersion = jsonBuilder.getVal("showVersion", String.class);
				return movieWaitingPayQRViewData;

			case 24:
				CompetitionDetailViewData competitionDetailViewData = new CompetitionDetailViewData();
				competitionDetailViewData.parseItemData(jsonBuilder);
				return competitionDetailViewData;
			case 25://LogoQrCodeView
				LogoQrCodeViewData qrCodeViewData = new LogoQrCodeViewData();
				qrCodeViewData.qrCode = jsonBuilder.getVal("qrCode",String.class);
				return qrCodeViewData;
			case 26://offline promote
				OfflinePromoteViewData offlinePromoteViewData = new OfflinePromoteViewData();
				offlinePromoteViewData.qrCode = jsonBuilder.getVal("qrCode",String.class);
				offlinePromoteViewData.text = jsonBuilder.getVal("text",String.class);
				return offlinePromoteViewData;
			default:
				break;
			}
		}else if (TextUtils.equals(type, "map")) {
			ChatMapViewData viewData = new ChatMapViewData();
			viewData.parseData(jsonBuilder.getVal("data", String.class));
			return viewData;
		}
		return null;
	}
	
	public static boolean isSameView(ViewData data, MsgViewBase viewBase) {
		if (viewBase.getViewType() == data.getType()) {
			return true;
		}
		// 默认是0，如果不等于0又不跟ViewData type相同，则一定不同
		if (viewBase.getViewType() != 0) {
			return false;
		}
		switch (data.getType()) {
		case ViewData.TYPE_CHAT_FROM_SYS:
			return viewBase instanceof IChatFromSysView;
		case ViewData.TYPE_CHAT_TO_SYS:
			return viewBase instanceof IChatToSysView;
		case ViewData.TYPE_CHAT_TO_SYS_PART:
			return viewBase instanceof IChatToSysPartView;
		case ViewData.TYPE_CHAT_WEATHER:
			return viewBase instanceof IChatWeatherView;
		case ViewData.TYPE_CHAT_SHARE:
			return viewBase instanceof IChatShockView;
		case ViewData.TYPE_FULL_LIST_POI:
			return viewBase instanceof IPoiListView;
		case ViewData.TYPE_FULL_LIST_AUDIO:
			return viewBase instanceof IAudioListView;
		case ViewData.TYPE_FULL_LIST_WECHAT:
			return viewBase instanceof IWechatListView;
		case ViewData.TYPE_FULL_LIST_SIM:
			return viewBase instanceof ISimListView;
		case ViewData.TYPE_FULL_LIST_TTS:
			return viewBase instanceof ITtsListView;
		case ViewData.TYPE_FULL_LIST_CALL:
			return viewBase instanceof ICallListView;
		case ViewData.TYPE_FULL_LIST_HELP:
			return viewBase instanceof IHelpListView;
		case ViewData.TYPE_FULL_LIST_CINEMA:
			return viewBase instanceof ICinemaListView;
		case ViewData.TYPE_FULL_NO_TTS_QRCORD:
			return viewBase instanceof INoTtsQrcodeView;
		case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
			return viewBase instanceof IHelpDetailListView;
		case ViewData.TYPE_CHAT_MAP:
			return viewBase instanceof IChatMapView;
		case ViewData.TYPE_FULL_LIST_MAPPOI:
			return viewBase instanceof IMapPoiListView;
		case ViewData.TYPE_CHAT_HELP_TIPS:
			return viewBase instanceof IHelpTipsView;
		case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
			return viewBase instanceof IHelpDetailImageView;
		case ViewData.TYPE_FULL_LIST_COMPETITION:
			return viewBase instanceof ICompetitionView;
		case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
			return viewBase instanceof ICompetitionDetailView;
		default:
			break;
		}
		return false;
	}
	
	public static ViewAdapter generateView(ViewData viewData){
		switch (viewData.getType()) {
		case ViewData.TYPE_CHAT_FROM_SYS:
			return WinLayoutManager.getInstance().getChatFromSysView().getView(viewData);
		case ViewData.TYPE_CHAT_TO_SYS:
			return WinLayoutManager.getInstance().getChatToSysView().getView(viewData);
		case ViewData.TYPE_CHAT_TO_SYS_PART:
			return WinLayoutManager.getInstance().getChatToSysPartView().getView(viewData);
		case ViewData.TYPE_CHAT_WEATHER:
			return WinLayoutManager.getInstance().getChatWeatherView().getView(viewData);
		case ViewData.TYPE_CHAT_SHARE:
			return WinLayoutManager.getInstance().getChatShockView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_POI:
//			PoiListViewData poiListViewData = (PoiListViewData) viewData;
//			Integer flag = WinLayoutManager.getInstance().getPoiListView().getFlags();
//			if (flag == null || (flag.intValue() & ViewBase.SUPPORT_CITY_TITLE) != ViewBase.SUPPORT_CITY_TITLE) {
//				return DefaultPoiListView.getInstance().getView(viewData);
//			}
//			if (PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)) { // 是否是历史
//				if (flag != null && ((flag.intValue() & ViewBase.SUPPORT_DELETE) == ViewBase.SUPPORT_DELETE)) { // 皮肤包是否支持删除按钮
//					return WinLayoutManager.getInstance().getPoiListView().getView(viewData);
//				} else {
//					return DefaultPoiListView.getInstance().getView(viewData);
//				}
//			} else {
//				return WinLayoutManager.getInstance().getPoiListView().getView(viewData);
//			}
			/*
			 * TXZ-13538，皮肤包兼容问题,策略改为老版本皮肤包优先
			 * Date: 2018-04-03
			 * zackzhou
			 */
			return WinLayoutManager.getInstance().getPoiListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_AUDIO:
			return WinLayoutManager.getInstance().getAudioListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_WECHAT:
			return WinLayoutManager.getInstance().getWechatListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_SIM:
			return WinLayoutManager.getInstance().getSimListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_TTS:
			return WinLayoutManager.getInstance().getTtsListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_CALL:
			return WinLayoutManager.getInstance().getCallListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_HELP:
			return WinLayoutManager.getInstance().getHelpListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_CINEMA:
			return WinLayoutManager.getInstance().getCinemaListView().getView(viewData);
		case ViewData.TYPE_FULL_NO_TTS_QRCORD:
			return WinLayoutManager.getInstance().getNoTtsQrcodeView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
			return WinLayoutManager.getInstance().getHelpDetailListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_MAPPOI:
			return WinLayoutManager.getInstance().getMapPoiListView().getView(viewData);
		case ViewData.TYPE_QRCODE:
			return WinLayoutManager.getInstance().getQrCodeView().getView(viewData);	
		case ViewData.TYPE_CHAT_BIND_DEVICE_QRCODE:
			return WinLayoutManager.getInstance().getBindDeviceView().getView(viewData);
		case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:
			return WinLayoutManager.getInstance().getConstellationFortuneView().getView(viewData);
		case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:
			return WinLayoutManager.getInstance().getConstellationMatchingView().getView(viewData);
		case ViewData.TYPE_CHAT_FROM_SYS_HL:
			return WinLayoutManager.getInstance().getChatSysHighlight().getView(viewData);
		case ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT:
			return WinLayoutManager.getInstance().getChatSysInterrupt().getView(viewData);
		case ViewData.TYPE_FULL_LIST_SIMPLE_LIST:
			return WinLayoutManager.getInstance().getNavAppListView().getView(viewData);
		case ViewData.TYPE_CHAT_HELP_TIPS:
			return WinLayoutManager.getInstance().getHelpTipsView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
			return WinLayoutManager.getInstance().getHelpDetailImageView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_REMINDER:
			return WinLayoutManager.getInstance().getReminderListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_FLIGHT:
			return WinLayoutManager.getInstance().getFlightListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_TRAIN:
			return WinLayoutManager.getInstance().getTrainListView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_STYLE:
			return WinLayoutManager.getInstance().getStyleListView().getView(viewData);
		case ViewData.TYPE_AUTHORIZATION_VIEW:
				return WinLayoutManager.getInstance().getAuthorizationView().getView(viewData);
		case ViewData.TYPE_FULL_FILM_LIST:
			return  WinLayoutManager.getInstance().getFilmListView().getView(viewData);
		case ViewData.TYPE_FULL_MOVIE_THEATER_LIST:
			return  WinLayoutManager.getInstance().getMovieTheaterListView().getView(viewData);
		case ViewData.TYPE_FULL_MOVIE_TIME_LIST:
			return WinLayoutManager.getInstance().getMovieTimeListView().getView(viewData);
		case ViewData.TYPE_FULL_MOVIE_SEATING_PLAN:
			return WinLayoutManager.getInstance().getMovieSeatPlanView().getView(viewData);
		case ViewData.TYPE_FULL_MOVIE_PHONE_NUM_QRCODE:
			return WinLayoutManager.getInstance().getMoviePhoneNumQRView().getView(viewData);
		case ViewData.TYPE_FULL_MOVIE_WAITING_PAY_QRCODE:
			return WinLayoutManager.getInstance().getMovieWaitingPayQRView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_COMPETITION:
			return WinLayoutManager.getInstance().getCompetitionView().getView(viewData);
		case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
			return WinLayoutManager.getInstance().getCompetitionDetailView().getView(viewData);
		case ViewData.TYPE_CHAT_FEEDBACK:
			return WinLayoutManager.getInstance().getFeedbackView().getView(viewData);
		case ViewData.TYPE_FULL_LIST_TRAIN_TICKET:
			return WinLayoutManager.getInstance().getmTrainTicketList().getView(viewData);
			case ViewData.TYPE_FULL_LIST_FLIGHT_TICKET:
				return WinLayoutManager.getInstance().getFlightTicketList().getView(viewData);
			case ViewData.TYPE_FULL_TICKET_PAY:
				return WinLayoutManager.getInstance().getmTicketPayView().getView(viewData);
		case ViewData.TYPE_CHAT_LOGO_QRCODE:
			return WinLayoutManager.getInstance().getLogoQrCodeView().getView(viewData);
		case ViewData.TYPE_CHAT_OFFLINE_PROMOTE:
			return WinLayoutManager.getInstance().getOfflinePromoteView().getView(viewData);
		default:
			break;
		}
		return null;
	}
	
	
	private ViewFactory() {
	}
	
	private static SpannableStringBuilder getNoResultBuilderTxt(String keywords) {
		int length = keywords.length();
		keywords = "没有找到" + keywords + "相关结果，请再说一次或点击本消息手动修改";
		SpannableStringBuilder ssb = new SpannableStringBuilder(keywords);
		ForegroundColorSpan norfcs = new ForegroundColorSpan(Color.WHITE);
		ForegroundColorSpan keyfcs = new ForegroundColorSpan(Color.parseColor("#27d7fd"));
		ssb.setSpan(norfcs, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(keyfcs, 4, 4 + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(norfcs, 4 + length, keywords.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return ssb;
	}

}
