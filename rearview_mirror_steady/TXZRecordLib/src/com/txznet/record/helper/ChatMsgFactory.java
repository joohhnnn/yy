package com.txznet.record.helper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.LinearLayout;

import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.data.MoviePhoneNumQRViewData;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.MovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.data.MovieWaitingPayQRViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.AuthorizationView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMoviePhoneNumQRView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieWaitingPayQRView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.adapter.ChatAudioAdapter.AudioItem;
import com.txznet.record.adapter.ChatCarControlHomeAdapter;
import com.txznet.record.adapter.ChatConsAdapter;
import com.txznet.record.adapter.ChatConsAdapter.ContactItem;
import com.txznet.record.adapter.ChatContactListAdapter;
import com.txznet.record.adapter.ChatFlightAdapter;
import com.txznet.record.adapter.ChatMovieTheaterAdapter;
import com.txznet.record.adapter.ChatMovieTimeAdapter;
import com.txznet.record.adapter.ChatMusicAdapter.MusicItem;
import com.txznet.record.adapter.ChatPoiAdapter.PoiItem;
import com.txznet.record.adapter.ChatReminderAdapter.ReminderItem;
import com.txznet.record.adapter.ChatSimRechargeAdapter.SimRechargeItem;
import com.txznet.record.adapter.ChatSimpleAdapter.SimpleItem;
import com.txznet.record.adapter.ChatTrainAdapter;
import com.txznet.record.adapter.ChatTtsThemeAdapter.TtsThemeItem;
import com.txznet.record.adapter.ChatWxContactListAdapter.WxContactItem;
import com.txznet.record.adapter.FlightTicketListAdapter;
import com.txznet.record.adapter.QiWuTicketPayAdapter;
import com.txznet.record.adapter.TrainTicketListAdapter;
import com.txznet.record.bean.AudioInfo;
import com.txznet.record.bean.AudioMsg;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.BindDeviceMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.CompetitionListMsg;
import com.txznet.record.bean.CompetitionMsg;
import com.txznet.record.bean.ConstellationFortuneMsg;
import com.txznet.record.bean.ConstellationMatchingMsg;
import com.txznet.record.bean.DisplayConMsg;
import com.txznet.record.bean.FeedbackMsg;
import com.txznet.record.bean.FlightInfo;
import com.txznet.record.bean.FlightMsg;
import com.txznet.record.bean.HelpMsg;
import com.txznet.record.bean.AudioBean;
import com.txznet.record.bean.HelpTipBean;
import com.txznet.record.bean.HelpTipMsg;
import com.txznet.record.bean.LogoQrCodeMsg;
import com.txznet.record.bean.MovieSeatPlanMsg;
import com.txznet.record.bean.MusicMsg;
import com.txznet.record.bean.OfflinePromoteMsg;
import com.txznet.record.bean.PhoneConsMsg;
import com.txznet.record.bean.PhoneContact;
import com.txznet.record.bean.PluginMessage;
import com.txznet.record.bean.PluginMessage.PluginData;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.bean.ReminderInfo;
import com.txznet.record.bean.ReminderMsg;
import com.txznet.record.bean.SimRechargeInfo;
import com.txznet.record.bean.SimRechargeMsg;
import com.txznet.record.bean.SimpleBean;
import com.txznet.record.bean.SimpleMsg;
import com.txznet.record.bean.StockMessage;
import com.txznet.record.bean.TrainInfo;
import com.txznet.record.bean.TrainMsg;
import com.txznet.record.bean.TtsThemeInfo;
import com.txznet.record.bean.TtsThemeMsg;
import com.txznet.record.bean.WeatherMessage;
import com.txznet.record.bean.WxContact;
import com.txznet.record.bean.WxContactMsg;
import com.txznet.record.lib.R;
import com.txznet.record.view.TitleView.Info;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.sdk.bean.TxzPoi;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;

public class ChatMsgFactory {
	private ChatMsgFactory() {
	}

	public static ChatMessage getSysTextMsg(String text) {
		ChatMessage msg = new ChatMessage(ChatMessage.TYPE_FROM_SYS_TEXT);
		msg.owner = ChatMessage.OWNER_SYS;
		msg.text = text;
		return msg;
	}

	public static ChatMessage getTextMsg(String text) {
		ChatMessage msg = new ChatMessage(ChatMessage.TYPE_TO_SYS_TEXT);
		msg.owner = ChatMessage.OWNER_USER;
		msg.text = text;
		return msg;
	}

	public static ChatMessage getTextPartMsg(String text) {
		ChatMessage msg = new ChatMessage(ChatMessage.TYPE_TO_SYS_PART_TEXT);
		msg.owner = ChatMessage.OWNER_USER_PART;
		msg.text = text;
		return msg;
	}

	public static ChatMessage getSysHelpMsg(String text, Context context, boolean ifShowText,
			OnClickListener listener) {
		HelpMsg msg = new HelpMsg();
		msg.ifShowText = ifShowText;
		msg.owner = ChatMessage.OWNER_SYS;
		msg.text = text;
		msg.icon = context.getResources().getDrawable(R.drawable.question_mark);
		msg.iconCallback = listener;
		return msg;
	}

	public static ChatMessage getSysContactListMsg(JSONBuilder doc) {
		String strPrefix = doc.getVal("strPrefix", String.class);
		String strName = doc.getVal("strName", String.class);
		String strSuffix = doc.getVal("strSuffix", String.class);
		Boolean isMultiName = doc.getVal("isMultiName", Boolean.class);
		JSONObject[] contacts = doc.getVal("contacts", JSONObject[].class);
		String title = strPrefix + strName + strSuffix;

		List<ChatContactListAdapter.ContactItem> items = new ArrayList<ChatContactListAdapter.ContactItem>();
		if (contacts != null) {
			for (int i = 0; i < contacts.length; i++) {
				JSONBuilder contactJson = new JSONBuilder(contacts[i]);
				ChatContactListAdapter.ContactItem item = new ChatContactListAdapter.ContactItem();
				String province = contactJson.getVal("province", String.class);
				String city = contactJson.getVal("city", String.class);
				String isp = contactJson.getVal("isp", String.class);

				item.province = province;
				item.city = city;
				if (!isMultiName) {
					item.main = contactJson.getVal("number", String.class);
					item.isp = isp;
				} else {
					item.main = contactJson.getVal("name", String.class);
					item.isp = isp;
				}

				items.add(item);
			}
		}

		return getSysContactListMsg(title, items);
	}
	
	public static PhoneConsMsg getSysContactMsg(JSONBuilder doc) {
		String strPrefix = doc.getVal("strPrefix", String.class);
		String strName = doc.getVal("strName", String.class);
		String strSuffix = doc.getVal("strSuffix", String.class);
		String prefix = doc.getVal("prefix", String.class);
		Boolean isMultiName = doc.getVal("isMultiName", Boolean.class);

		int curPage = doc.getVal("curPage", Integer.class);
		int maxPage = doc.getVal("maxPage", Integer.class);

		Info info = new Info();
		info.prefix = prefix;
		info.curPage = curPage;
		info.maxPage = maxPage;
		
		PhoneConsMsg pcm = new PhoneConsMsg();
		pcm.mTitleInfo = info;
		
		try {
			ArrayList<ChatConsAdapter.ContactItem> itemList = new ArrayList<ChatConsAdapter.ContactItem>();
			JSONObject[] cons = doc.getVal("contacts", JSONObject[].class);
			for (int i = 0; i < cons.length; i++) {
				JSONObject jObj = cons[i];
				String province = jObj.optString("province");
				String city = jObj.optString("city");
				String isp = jObj.optString("isp");

				PhoneContact pc = new PhoneContact();
				pc.province = province;
				pc.city = city;
				if (isMultiName == null || !isMultiName) {
					pc.isp = isp;
					pc.main = jObj.optString("name");
					pc.phone = jObj.optString("number");
				} else {
					pc.isp = isp;
					pc.main = jObj.optString("name");
					pc.phone = jObj.optString("number");
				}

				ContactItem item = new ContactItem();
				item.mItem = pc;

				itemList.add(item);
			}
			
			pcm.mItemList = itemList;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return pcm;
	}

	public static ChatMessage getSysContactListMsg(String title, List<ChatContactListAdapter.ContactItem> items) {
		ChatMessage msg = new ChatMessage(ChatMessage.TYPE_FROM_SYS_CONTACT);
		msg.owner = ChatMessage.OWNER_SYS;
		msg.title = title;
		msg.items = items;
		return msg;
	}

	public static WeatherMessage getWeatherMessage(WeatherInfos infos) {
		WeatherMessage msg = new WeatherMessage(WeatherMessage.TYPE_FROM_SYS_WEATHER);
		msg.mWeatherInfos = infos;
		return msg;
	}

	public static StockMessage getStockMessage(StockInfo infos) {
		StockMessage msg = new StockMessage(StockMessage.TYPE_FROM_SYS_STOCK);
		msg.mStockInfo = infos;
		return msg;
	}

	public static HelpTipMsg getHelpTipMsg(JSONBuilder jsonBuilder){
		HelpTipMsg helpTipMsg = new HelpTipMsg();
		helpTipMsg.mTitle = jsonBuilder.getVal("title",String.class);
		JSONArray jsonArray = jsonBuilder.getVal("data",JSONArray.class);
		if (jsonArray != null) {
			helpTipMsg.mCount = jsonArray.length();
			helpTipMsg.mHelpTipBeen = new ArrayList<HelpTipBean>(helpTipMsg.mCount);
			JSONBuilder jsonBean;
			HelpTipBean mHelpTipBean;
			for (int i = 0; i < helpTipMsg.mCount; i++) {
				try {
					jsonBean = new JSONBuilder(jsonArray.getJSONObject(i));
					mHelpTipBean = new HelpTipBean();
					mHelpTipBean.resId = jsonBean.getVal("resId",String.class);
					mHelpTipBean.label = jsonBean.getVal("label",String.class);
					helpTipMsg.mHelpTipBeen.add(mHelpTipBean);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
		return helpTipMsg;
	}
	public static PoiMsg getPoiMessageRef(List<com.txznet.record.adapter.ChatPoiAdapter.PoiItem> pois, String keyWord,
			boolean isBus, String action, OnItemClickListener listener, OnClickListener onClickListener) {
		PoiMsg pm = new PoiMsg();
		pm.mItemList = pois;
		pm.mKeywords = keyWord;
		pm.mIsBusiness = isBus;
		pm.mNeedNotify = true;
		pm.action = action;
		pm.mOnItemClickListener = listener;
		pm.mOnTitleClickListener = onClickListener;
		return pm;
	}

	public static WxContactMsg getSysWxPickerMsg(String action, String title, List<WxContactItem> items,
			OnItemClickListener listener) {
		WxContactMsg msg = new WxContactMsg();
		msg.action = action;
		msg.mItemList = items;
		msg.title = title;
		msg.mNeedNotify = true;
		msg.mOnItemClickListener = listener;
		return msg;
	}

	public static AudioMsg getAudioMessageRef(List<com.txznet.record.adapter.ChatAudioAdapter.AudioItem> audios,
			String keyWord, OnItemClickListener listener) {
		AudioMsg am = new AudioMsg();
		am.mNeedNotify = true;
		am.mItemList = audios;
		am.mKeywords = keyWord;
		am.mOnItemClickListener = listener;
		return am;
	}

	public static MusicMsg getMusicMessageRef(List<MusicItem> audios, String keywords, OnItemClickListener listener) {
		MusicMsg mm = new MusicMsg();
		mm.mNeedNotify = true;
		mm.mItemList = audios;
		mm.mKeywords = keywords;
		mm.mOnItemClickListener = listener;
		return mm;
	}

	public static MusicMsg getMusicMessageRef(List<MusicItem> audios, String keywords, OnItemClickListener listener,
			OnTouchListener onTouchListener) {
		MusicMsg mm = getMusicMessageRef(audios, keywords, listener);
		mm.mOnTouchListener = onTouchListener;
		return mm;
	}

	public static ChatMessage parseListMsgFromJson(String strData) {
		JSONBuilder doc = new JSONBuilder(strData);
		Integer type = doc.getVal("type", Integer.class);
		if (type != null && type != 0) {
			return getDisplayMsgFromJson(strData);
		}
		// 抽出带有上下页的联系人
		Integer curPage = doc.getVal("curPage", Integer.class);
		if (curPage != null) {
			return getSysContactMsg(doc);
		}
		
		return getSysContactListMsg(doc);
	}

	public static BaseDisplayMsg getDisplayMsgFromJson(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		JSONBuilder jb = new JSONBuilder(json);

		int type = jb.getVal("type", Integer.class);
		int count = jb.getVal("count", Integer.class);
		int curPage = jb.getVal("curPage", Integer.class);
		int maxPage = jb.getVal("maxPage", Integer.class);
		LogUtil.logd("getDisplayMsgFromJson type:" + type);

		String action = jb.getVal("action", String.class);
		String prefix = jb.getVal("prefix", String.class);
		String titlefix = jb.getVal("titlefix", String.class);
		String aftfix = jb.getVal("aftfix", String.class);
		String cityfix = jb.getVal("city", String.class);
		String midfix = jb.getVal("midfix", String.class);
		Info info = new Info();
		info.prefix = prefix;
		info.titlefix = titlefix;
		info.aftfix = aftfix;
		info.midfix = midfix;
		info.cityfix = cityfix;
		info.curPage = curPage;
		info.maxPage = maxPage;
		if (type == 17) {
			return ChatMovieTheaterAdapter.generateMovieTheaterBean(jb, info);
		}
		if (type == 18) {
			return ChatMovieTimeAdapter.generateMovieTheaterBean(jb, info);
		}
		if (type == 22) {
			return ChatCarControlHomeAdapter.generateMiHomeItem(jb, info);
		}

		// 齐悟火车票列表
		if(type == 25){
			return TrainTicketListAdapter.generateTrainTicketMsg(jb,info);
		}

		// 齐悟飞机票列表
		if(type == 26){
			return FlightTicketListAdapter.generateFlightTicketMsg(jb,info);
		}

		if(type == 27){
			return QiWuTicketPayAdapter.generateQiWuTickectPay(jb,info);
		}
		// Poi列表
		if (type == 2) {
			String keywords = jb.getVal("keywords", String.class);
			String city = jb.getVal("city", String.class);
			String business = jb.getVal("poitype", String.class);
			Integer showCount = jb.getVal("showcount", Integer.class);
			Integer mapAction = jb.getVal("mapAction", Integer.class);
			Double locationLat = jb.getVal("locationLat", Double.class);
			Double locationLng = jb.getVal("locationLng", Double.class);
			Double destinationLat = jb.getVal("destinationLat", Double.class);
			Double destinationLng = jb.getVal("destinationLng", Double.class);
			Boolean isListModel =  jb.getVal("listmodel", Boolean.class);
			
			boolean isBus = false;
			if (!TextUtils.isEmpty(business) && business.equals("business")) {
				isBus = true;
			}
			

			List<PoiItem> poiItems = new ArrayList<PoiItem>();
			PoiMsg poiMsg = new PoiMsg();
			poiMsg.mKeywords = keywords;
			poiMsg.mNeedNotify = true;
			poiMsg.mTitleInfo = info;
			poiMsg.action = action;
			poiMsg.mShowCount = showCount;
			poiMsg.mMapAction = mapAction;
			poiMsg.mIsListModel = isListModel;
			poiMsg.mDestinationLat = destinationLat;
			poiMsg.mDestinationLng = destinationLng;
			poiMsg.mLocationLat = locationLat;
			poiMsg.mLocationLng = locationLng;
			poiMsg.mIsBusiness = isBus;
			
			JSONArray obJsonArray = jb.getVal("pois", JSONArray.class);
			if (obJsonArray != null) {
				for (int i = 0; i < count; i++) {
					try {
						JSONObject jo = obJsonArray.getJSONObject(i);
						String objJson = jo.toString();
						int poitype = jo.optInt("poitype");
						Poi poi = null;
//						if (isBus) {
//							poi = BusinessPoiDetail.fromString(objJson);
//						} else {
//							poi = PoiDetail.fromString(objJson);
//						}
						switch (poitype) {
						case Poi.POI_TYPE_BUSINESS:
							poi = BusinessPoiDetail.fromString(objJson);
							break;

						case Poi.POI_TYPE_TXZ:
							poi = TxzPoi.fromString(objJson);
							break;

						case Poi.POI_TYPE_POIDEATAIL:
							poi = PoiDetail.fromString(objJson);
							break;
						}
						poi.setAction(action);

						PoiItem poiItem = new PoiItem();
						poiItem.mIsBus = isBus;
						poiItem.mItem = poi;
						poiItems.add(poiItem);
					} catch (JSONException e) {
					}
				}
			}
			poiMsg.mItemList = poiItems;
			return poiMsg;
		}

		// 音乐列表
		if (type == 4) {
			JSONArray jsonArray = jb.getVal("audios", JSONArray.class);
			String keywords = jb.getVal("keywords", String.class);
			List<MusicItem> mis = new ArrayList<MusicItem>();
			if (jsonArray != null) {
				for (int i = 0; i < count; i++) {
					try {
						MusicItem mi = new MusicItem();
						JSONObject jo = jsonArray.getJSONObject(i);
						String title = "";
						if (jo.has("title")) {
							title = jo.getString("title");
						}
						String name = "";
						if (jo.has("name")) {
							name = jo.getString("name");
						}
						boolean lastPlay = false;
						if (jo.has("listened")) {
							lastPlay = jo.getBoolean("listened");
						}
						boolean paid = false;
						if (jo.has("paid")) {
							paid = jo.getBoolean("paid");
						}
						int novelStatus = 0;
						if (jo.has("novelStatus")) {
							novelStatus = jo.getInt("novelStatus");
						}
						boolean latest = false;
						if(jo.has("latest")){
							latest = jo.getBoolean("latest");
						}
						mi.mItem = new AudioInfo(title, name);
						mi.mItem.setPaid(paid);
						mi.mItem.setLastPlay(lastPlay);
						mi.mItem.setNovelStatus(novelStatus);
						mi.mItem.setLatest(latest);
						mis.add(mi);
					} catch (Exception e) {
						continue;
					}
				}
			}
			MusicMsg mm = new MusicMsg();
			mm.mNeedNotify = true;
			mm.mTitleInfo = info;
			mm.mItemList = mis;
			mm.mKeywords = keywords;
			return mm;
		}

		// 联系人
		if (type == 1) {
			JSONObject[] arrayObjs = jb.getVal("contacts", JSONObject[].class);
			String title = jb.getVal("title", String.class);
			String keywords = jb.getVal("keywords", String.class);
			WxContactMsg wcm = new WxContactMsg();
			wcm.action = action;
			wcm.mNeedNotify = true;
			wcm.title = title;
			wcm.mTitleInfo = info;
			wcm.mKeywords = keywords;

			try {
				List<WxContactItem> wcis = new ArrayList<WxContactItem>();
				for (int i = 0; i < count; i++) {
					JSONObject jobj = arrayObjs[i];
					String id = jobj.getString("id");
					String name = jobj.getString("name");
					WxContact wc = new WxContact();
					wc.nick = name;
					wc.openid = id;
					WxContactItem obj = new WxContactItem();
					obj.curPrg = 0;
					obj.mItem = wc;
					wcis.add(obj);
				}
				wcm.mItemList = wcis;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return wcm;
		}

		// TTS主题选择列表
		if (type == 6) {
			JSONObject[] arrayObjs = jb.getVal("themes", JSONObject[].class);
			String title = jb.getVal("title", String.class);
			TtsThemeMsg themeMsg = new TtsThemeMsg();
			themeMsg.action = action;
			themeMsg.mNeedNotify = true;
			themeMsg.title = title;
			themeMsg.mTitleInfo = info;

			try {
				List<TtsThemeItem> themeList = new ArrayList<TtsThemeItem>();
				for (int i = 0; i < count; i++) {
					JSONObject jobj = arrayObjs[i];
					String name = jobj.getString("name");
					int id = jobj.getInt("id");
					TtsThemeInfo themeInfo = new TtsThemeInfo(name, id);
					TtsThemeItem obj = new TtsThemeItem();
					obj.curPrg = 0;
					obj.mItem = themeInfo;
					themeList.add(obj);
				}
				themeMsg.mItemList = themeList;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return themeMsg;
		}

		// 流量卡
		if (5 == type) {
			JSONObject[] arrayObjs = jb.getVal("data", JSONObject[].class);
			String title = jb.getVal("title", String.class);
			String keywords = jb.getVal("keywords", String.class);

			SimRechargeMsg msg = new SimRechargeMsg();
			msg.action = action;
			msg.mNeedNotify = true;
			msg.title = title;
			msg.mTitleInfo = info;
			msg.mKeywords = title;

			try {
				List<SimRechargeItem> items = new ArrayList<SimRechargeItem>();
				for (JSONObject jObj : arrayObjs) {
					int id = jObj.getInt("id");
					String rechargeTitle = jObj.getString("title");
					int price = jObj.getInt("price");
					int rawPrice = jObj.getInt("rawPrice");
					String qrcode = jObj.getString("qrcode");

					SimRechargeItem rItem = new SimRechargeItem();
					SimRechargeInfo rInfo = new SimRechargeInfo();
					rInfo.mId = id;
					rInfo.mName = rechargeTitle;
					rInfo.mPrice = price;
					rInfo.mPriceRaw = rawPrice;
					rInfo.mQRCode = qrcode;
					rItem.mItem = rInfo;
					items.add(rItem);
				}

				msg.mItemList = items;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return msg;
		}

		// 电台
		if (type == 3) {
			try {
				String keywords = jb.getVal("keywords", String.class);
				List<AudioItem> mAudioItems = new ArrayList<AudioItem>();
				for (int i = 0; i < count; i++) {
					AudioItem item = new AudioItem();
					AudioBean music = AudioBean.fromString(jb.getVal("music" + i, String.class));
					item.mItem = music;
					mAudioItems.add(item);
				}
				AudioMsg mm = new AudioMsg();
				mm.mNeedNotify = true;
				mm.mTitleInfo = info;
				mm.mItemList = mAudioItems;
				mm.mKeywords = keywords;
				return mm;
			} catch (Exception e) {
			}
		}

		// 单独的上下页界面消息类型
		if (type == 7 || type == 16) {
			try {
				String keywords = jb.getVal("keywords", String.class);
				DisplayConMsg mm = new DisplayConMsg();
				mm.mTitleInfo = info;
				mm.mKeywords = keywords;
				return mm;
			} catch (Exception e) {
			}
		}	
		
		// 简单的列表消息类型
		if (type == 11) {
			try {
				SimpleMsg sm = new SimpleMsg();
				sm.mTitleInfo = info;
				String[] jsonObjects = jb.getVal("beans", String[].class);
				List<SimpleItem> items = new ArrayList<SimpleItem>();
				for (int i = 0; i < jsonObjects.length; i++) {
					SimpleItem object = new SimpleItem();
					SimpleBean bean = new SimpleBean();
					bean.title = jsonObjects[i];
					object.mItem = bean;
					items.add(object);
				}
				sm.mItemList = items;
				
				return sm;
			} catch (Exception e) {
				LogUtil.loge(e.getMessage());
				e.printStackTrace();
			}
		}
		if (type == 12) {
			JSONObject[] jsonObjects = jb.getVal("reminders",JSONObject[].class);
			ReminderMsg reminderMsg = new ReminderMsg();
			try {
				List<ReminderItem> items = new ArrayList<ReminderItem>();
				for (int i = 0; i < jsonObjects.length; i++) {
					JSONObject jobj = jsonObjects[i];
					String content = jobj.getString("content");
					String time = jobj.getString("time");
					String position = jobj.getString("position");
					ReminderInfo rInfo = new ReminderInfo();
					rInfo.mContent = content;
					rInfo.time = time;
					rInfo.position = position;
					ReminderItem obj = new ReminderItem();
					obj.curPrg = 0;
					obj.mItem = rInfo;
					items.add(obj);
				}
				reminderMsg.mTitleInfo = info;
				reminderMsg.mItemList = items;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return reminderMsg;
		}
		if (type == 13) {
			JSONObject[] jsonObjects = jb.getVal("flights",JSONObject[].class);
			FlightMsg flightMsg = new FlightMsg();
			try {
				List<ChatFlightAdapter.FlightItem> items = new ArrayList<ChatFlightAdapter.FlightItem>();
				for (int i = 0; i < jsonObjects.length; i++) {
					JSONObject jobj = jsonObjects[i];
					if(jobj == null){
						continue;
					}
					FlightInfo fInfo = new FlightInfo();
					fInfo.airline = jobj.optString("airline");
					fInfo.flightNo = jobj.optString("flightNo");
					fInfo.departAirportName = jobj.optString("departAirportName");
					fInfo.departTime = jobj.optString("departTime");
					fInfo.departTimeHm = jobj.optString("departTimeHm");
					fInfo.departTimestamp = jobj.optLong("departTimestamp", 0l);
					fInfo.arrivalAirportName = jobj.optString("arrivalAirportName");
					fInfo.arrivalTime = jobj.optString("arrivalTime");
					fInfo.arrivalTimeHm = jobj.optString("arrivalTimeHm");
					fInfo.arrivalTimestamp = jobj.optLong("arrivalTimestamp", 0l);
					fInfo.economyCabinPrice = jobj.optInt("economyCabinPrice", 0);
					fInfo.economyCabinDiscount = jobj.optString("economyCabinDiscount");
					fInfo.ticketCount = jobj.optInt("ticketCount", 0);
					fInfo.addDate = jobj.optString("addDate");
					
					ChatFlightAdapter.FlightItem obj = new ChatFlightAdapter.FlightItem();
					obj.curPrg = 0;
					obj.mItem = fInfo;
					items.add(obj);
				}
				info.hideDrawable = true;
				flightMsg.mTitleInfo = info;
				flightMsg.mItemList = items;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return flightMsg;
		}
		if (type == 14) {
			TrainMsg trainMsg = new TrainMsg();
			JSONObject result = jb.getVal("result", JSONObject.class);
			if (result == null) {
				return trainMsg;
			}
			JSONArray jsonArray = result.optJSONArray("ticketList");
			if (jsonArray == null) {
				return trainMsg;
			}
			List<ChatTrainAdapter.TrainItem> items = new ArrayList<ChatTrainAdapter.TrainItem>();
			for (int i = 0; i < jsonArray.length(); i++) {
				TrainInfo trainInfo = TrainInfo.parseItem(jsonArray.optJSONObject(i));
				if (trainInfo != null) {
					ChatTrainAdapter.TrainItem obj = new ChatTrainAdapter.TrainItem();
					obj.curPrg = 0;
					obj.mItem = trainInfo;
					items.add(obj);
				}
			}
			info.hideDrawable = true;
			trainMsg.mTitleInfo = info;
			trainMsg.mItemList = items;
			return trainMsg;
		}
		if (type == 8) {
			DisplayConMsg displayConMsg = new DisplayConMsg();
			displayConMsg.mTitleInfo = info;
			return displayConMsg;
		}
		if(type == 19 || type == 20  || type == 21){
			DisplayConMsg displayConMsg = new DisplayConMsg();
			return displayConMsg;
		}

		if (type == 23) {//赛事列表界面
			CompetitionListMsg message = new CompetitionListMsg();
			message.mTitleInfo = info;
			message.parseData(jb);
			return message;
		}
		return null;
	}
	
	/**
	 * 获取Data的ChatMessage
	 * @param str json格式字符串
	 * @return 需要判空
	 */
	public static ChatMessage getDataMessage(String str){
		JSONBuilder jsonBuilder = new JSONBuilder(str);
		int type = jsonBuilder.getVal("type", Integer.class, 0);
		ChatMessage message = null;
		switch (type) {
		case 3://QrCode
			message = new ChatMessage(ChatMessage.TYPE_FROM_SYS_QRCODE);
			message.text = jsonBuilder.getVal("qrCode", String.class, "");
			break;
		case 4://Highlight Text
			message = new ChatMessage(ChatMessage.TYPE_FROM_SYS_TEXT_HL);
			message.text = jsonBuilder.getVal("rawText", String.class, "");
			break;
		case 5://Sys ChatMessage With interrupt tips
			message = new ChatMessage(ChatMessage.TYPE_FROM_SYS_TEXT_WITH_INTERRUPT_TIPS);
			message.text = jsonBuilder.getVal("text", String.class);
			message.title = jsonBuilder.getVal("tips", String.class);
			break;
		case 6://HelpTips
			message = getHelpTipMsg(jsonBuilder);
			break;
			case 8: // AuthorizationView
				message = createContainMsg(jsonBuilder.toString(), AuthorizationView.getInstance().createView(jsonBuilder));
				break;
	    	case 9: // BindDevice
				message = new BindDeviceMsg();
				((BindDeviceMsg) message).imageUrl = jsonBuilder.getVal("imageUrl", String.class, "");
				((BindDeviceMsg) message).qrCode = jsonBuilder.getVal("qrCode", String.class, "");
				break;
			case 10:
				message = new ConstellationFortuneMsg();
				((ConstellationFortuneMsg) message).name = jsonBuilder.getVal("name", String.class, "");
				((ConstellationFortuneMsg) message).level = jsonBuilder.getVal("level", int.class, 0);
				((ConstellationFortuneMsg) message).fortuneType = jsonBuilder.getVal("fortuneType", String.class, "");
				((ConstellationFortuneMsg) message).desc = jsonBuilder.getVal("desc", String.class, "");
				break;
			case 11:
				message = new ConstellationMatchingMsg();
				((ConstellationMatchingMsg) message).name = jsonBuilder.getVal("name", String.class, "");
				((ConstellationMatchingMsg) message).level = jsonBuilder.getVal("level", int.class, 0);
				((ConstellationMatchingMsg) message).matchName = jsonBuilder.getVal("matchName", String.class, "");
				((ConstellationMatchingMsg) message).desc = jsonBuilder.getVal("desc", String.class, "");
				break;
			case 12:
				message = new FeedbackMsg();
				((FeedbackMsg) message).tips = jsonBuilder.getVal("tips", String.class, "");
				((FeedbackMsg) message).qrCode = jsonBuilder.getVal("qrCode", String.class, "");
				break;
			case 19:
				MovieSeatPlanViewData movieSeatPlanViewData = new MovieSeatPlanViewData();
				movieSeatPlanViewData.seatPlanImageUrl = jsonBuilder.getVal("SeatPlanUrl", String.class);
				movieSeatPlanViewData.vTips = jsonBuilder.getVal("vTips", String.class);
				View seatPlanView = DefaultMovieSeatPlanViewData.getInstance().getView(movieSeatPlanViewData).view;
				int contentHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, contentHeight);
				seatPlanView.setLayoutParams(layoutParams);
				message = createContainMsg(jsonBuilder.toString(), seatPlanView);
				break;
			case 20:
				MoviePhoneNumQRViewData moviePhoneNumQRViewData = new MoviePhoneNumQRViewData();
				moviePhoneNumQRViewData.phoneNumQRUrl = jsonBuilder.getVal("phoneNumUrl", String.class);
				View phoneView = DefaultMoviePhoneNumQRView.getInstance().getView(moviePhoneNumQRViewData).view;
				int phoneViewHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();
				LinearLayout.LayoutParams phoneViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, phoneViewHeight);
				phoneView.setLayoutParams(phoneViewLayoutParams);
				message = createContainMsg(jsonBuilder.toString(), phoneView);
				break;
			case 21:
				MovieWaitingPayQRViewData movieWaitingPayQRViewData = new MovieWaitingPayQRViewData();
				movieWaitingPayQRViewData.phoneNum = jsonBuilder.getVal("phoneNum", String.class, "");
				movieWaitingPayQRViewData.replacePhoneUrl = jsonBuilder.getVal("replacePhoneUrl", String.class, "");
				movieWaitingPayQRViewData.WXPayURL = jsonBuilder.getVal("WXPayURL", String.class, "");
				movieWaitingPayQRViewData.ZFBPayURL = jsonBuilder.getVal("ZFBPayURL", String.class, "");
				movieWaitingPayQRViewData.moiveName = jsonBuilder.getVal("moiveName",String.class);
				movieWaitingPayQRViewData.cinemaName = jsonBuilder.getVal("cinemaName",String.class);
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

				View payQRView = DefaultMovieWaitingPayQRView.getInstance().getView(movieWaitingPayQRViewData).view;
				//int payQRViewHeight = ScreenUtil.getDisplayLvItemH(false) * ScreenUtil.getVisbileCount();
				LinearLayout.LayoutParams payQRViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,  LinearLayout.LayoutParams.MATCH_PARENT);
				payQRView.setLayoutParams(payQRViewLayoutParams);
				message = createContainMsg(jsonBuilder.toString(), payQRView);
				break;
			case 24:
				message = new CompetitionMsg();
				((CompetitionMsg)message).parseData(jsonBuilder);
				break;
			case 25://LogoQrCodeView
				message = new LogoQrCodeMsg();
				((LogoQrCodeMsg) message).qrCode = jsonBuilder.getVal("qrCode",String.class);
				break;
			case 26://offline promote
				message = new OfflinePromoteMsg();
				((OfflinePromoteMsg) message).qrCode = jsonBuilder.getVal("qrCode",String.class);;
				((OfflinePromoteMsg) message).text = jsonBuilder.getVal("text",String.class);;
				break;

		default:
			break;
		}
		return message;

	}
	private static MovieSeatPlanMsg getMovieSeatPlanMsg(JSONBuilder builder){
		MovieSeatPlanMsg msg = new MovieSeatPlanMsg();
		msg.seatPlanImageUrl = builder.getVal("SeatPlanUrl", String.class, "");
		return msg;
	}

	/**
	 * 创建一个放入上下页内容的View消息类型
	 * 
	 * @param infoData
	 * @param conView
	 * @return
	 */
	public static BaseDisplayMsg createContainMsg(String infoData, View conView) {
		BaseDisplayMsg bdms = getDisplayMsgFromJson(infoData);
		if (bdms != null && bdms instanceof DisplayConMsg) {
			((DisplayConMsg) bdms).mConView = conView;
		}
		return bdms;
	}
//	public static BaseDisplayMsg createPoiMsg(String infoData, View conView) {
//		BaseDisplayMsg bdms = getDisplayMsgFromJson(infoData);
//		if (bdms != null && bdms instanceof PoiMsg) {
//			try {
//				JSONObject json =new JSONObject(infoData);
//				if(json != null){
//					if(json.has("showcount")){
//						((PoiMsg) bdms).mShowCount = json.getInt("showcount");
//					}
//					if(json.has("business")){
//						((PoiMsg) bdms).mIsBusiness = json.getBoolean("business");
//					}
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			((PoiMsg) bdms).mMapView = conView;
//		}
//		return bdms;
//	}

	// TODO
	public static PluginMessage getPluginMessage(String typeId, View pluginView, boolean isReplace, boolean isInDep) {
		PluginMessage pm = new PluginMessage();
		pm.owner = ChatMessage.OWNER_SYS;
		pm.mPluginData = new PluginData();
		pm.mPluginData.typeId = typeId;
		pm.mPluginData.mView = pluginView;
		pm.mPluginData.mReplace = isReplace;
		pm.mPluginData.mIsDepend = isInDep;
		return pm;
	}
}
