package com.txznet.record.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatAudioAdapter.AudioItem;
import com.txznet.record.adapter.ChatContactListAdapter;
import com.txznet.record.adapter.ChatMusicAdapter.MusicItem;
import com.txznet.record.adapter.ChatPoiAdapter.PoiItem;
import com.txznet.record.adapter.ChatWxContactListAdapter.WxContactItem;
import com.txznet.record.bean.AudioInfo;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.MusicBean;
import com.txznet.record.bean.WxContact;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.service.IService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class RecordService extends Service {
	private static final String TAG = RecordService.class.getSimpleName();

	public class RecordBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, final String command,
				final byte[] data) throws RemoteException {
			// if (!(command.equals("txz.record.ui.refresh.progressbar") ||
			// command.equals("txz.record.ui.refresh.volume"))) {
			// Log.d(TAG, "command=" + command + ", data=" + (data == null ?
			// "null" : new String(data)));
			// }
			byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
			/*
			 * ****************** command_list *******************
			 * txz.record.ui.show 打开窗口 txz.record.ui.dismiss 关闭窗口
			 * txz.record.ui.refresh 刷新视图状态 data -> {"status" : 0 tts播报状态; 1
			 * 录音开始; 2 处理中状态 } txz.record.ui.refresh.volume 音量改变
			 * txz.record.ui.chat 添加聊天信息 data -> {"owner" : 0 从设备发送 ; 1 发送给设备,
			 * "text" : "文本" } txz.record.ui.list 刷新列表 data -> {"strPrefix" ;
			 * "strName" ; strSuffix" ; "isMultiName" ; "contacts" : [{"name", "
			 * number","area"}]}
			 */
			if (command.startsWith("txz.record.ui")) {
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						if (command.equals("txz.record.ui.show")) {
							WinRecord.getInstance().show();
						} else if (command.equals("txz.record.ui.dismiss")) {
							WinRecord.getInstance().dismiss();
						} else if (command.equals("txz.record.ui.refresh")) {
							Integer status = new JSONBuilder(data).getVal(
									"status", Integer.class);
							if (status != null) {
								WinRecord.getInstance().notifyUpdateLayout(
										status);
							}
						} else if (command
								.equals("txz.record.ui.refresh.volume")) {
							Integer volume = new JSONBuilder(data).getVal(
									"volume", Integer.class);
							if (volume != null) {
								WinRecord.getInstance().notifyUpdateVolume(
										volume);
							}
						} else if (command
								.equals("txz.record.ui.refresh.progressbar")) {
							Integer progress = new JSONBuilder(data).getVal(
									"progress", Integer.class);
							if (progress != null) {
								WinRecord.getInstance().notifyUpdateProgress(
										progress, 0);
							}
						} else if (command.equals("txz.record.ui.chat")) {
							if (data != null) {
								sendChatMsg(data);
							}
						} else if (command.equals("txz.record.ui.list")) {
							if (data != null) {
								sendListMsg(data);
							}
						} else if (command.equals("txz.record.ui.show.weather")) {
							if (data != null) {
								sendWeatherMsg(data);
							}
						} else if (command.equals("txz.record.ui.show.stock")) {
							if (data != null) {
								sendStockMsg(data);
							}
						} else if (command.equals("txz.record.ui.list.next")) {
//							WinRecord.getInstance().nextPager();
						} else if (command.equals("txz.record.ui.list.pre")) {
//							WinRecord.getInstance().prePager();
						}
					}
				}, 0);
			}
			return ret;
		}
	}

	private void sendChatMsg(byte[] data) {
		JSONBuilder doc = new JSONBuilder(data);
		int owner = doc.getVal("owner", Integer.class);
		String text = doc.getVal("text", String.class);
		if (owner == ChatMessage.OWNER_SYS) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getSysTextMsg(text));
		} else {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getTextMsg(text));
		}
	}

	private void sendWeatherMsg(byte[] data) {
		WeatherInfos infos = null;
		try {
			infos = WeatherInfos.parseFrom(data);
		} catch (Exception e) {
			LogUtil.loge("WeatherData parse error!");
			return;
		}
		WinRecord.getInstance().addMsg(ChatMsgFactory.getWeatherMessage(infos));
	}

	private void sendStockMsg(byte[] data) {
		StockInfo infos = null;
		try {
			infos = StockInfo.parseFrom(data);
		} catch (Exception e) {
			LogUtil.loge("Stockdata parse error!");
			return;
		}
		WinRecord.getInstance().addMsg(ChatMsgFactory.getStockMessage(infos));
	}

	/*
	 * record.ui.list 刷新列表 data -> {"strPrefix" ; "strName" ; "strSuffix" ;
	 * "isMultiName" ; "contacts" : [{"name", "number", "area"}]}
	 */
	private void sendListMsg(byte[] data) {
		JSONBuilder doc = new JSONBuilder(data);
		Integer type = doc.getVal("type", Integer.class);
		// 发送微信列表
		if (type != null && type == 1) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getDisplayMsgFromJson(new String(data)));
//			String title = doc.getVal("title", String.class);
//			String action = doc.getVal("action", String.class);
//			JSONObject[] contacts = doc.getVal("contacts", JSONObject[].class);
//			if (title != null && contacts != null) {
//				List<WxContactItem> contactList = new ArrayList<WxContactItem>();
//				for (JSONObject contact : contacts) {
//					JSONBuilder contactDoc = new JSONBuilder(contact);
//					String id = contactDoc.getVal("id", String.class);
//					String name = contactDoc.getVal("name", String.class);
//					if (id != null && name != null) {
//						WxContactItem item = new WxContactItem();
//						item.mItem = new WxContact(id, name);
//						contactList.add(item);
//					}
//				}
//
//				OnItemClickListener listener = new OnItemClickListener() {
//
//					@Override
//					public void onItemClick(AdapterView<?> parent, View view,
//							int position, long id) {
//						// AudioSelector.selectIndex(position, "");
//						String data = new JSONBuilder().put("index", position)
//								.put("type", 1).toString();
//						ServiceManager.getInstance().sendInvoke(
//								ServiceManager.TXZ,
//								"txz.record.ui.event.item.selected",
//								data.getBytes(), null);
//					}
//				};
//
//				if (contactList.size() > 0) {
//					WinRecord.getInstance().addMsg(
//							ChatMsgFactory.getSysWxPickerMsg(action, title,
//									contactList, listener));
//				}
//			}
		}

		else if (type != null && type == 2) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getDisplayMsgFromJson(new String(data)));
//			final String keyWord = doc.getVal("keywords", String.class);
//			final boolean mIsBusiness = "business".equals(doc.getVal("poitype",
//					String.class)) ? true : false;
//			final String action = doc.getVal("action", String.class);
//			final String city = doc.getVal("city", String.class);
//			final int count = doc.getVal("count", Integer.class);
//			final List<PoiItem> mPoiItems = new ArrayList<PoiItem>();
//			final List<Poi> mPois = new ArrayList<Poi>();
//			final List<BusinessPoiDetail> mBusPois = new ArrayList<BusinessPoiDetail>();
//
//			JSONArray mPoiJsonArray = doc.getVal("pois", JSONArray.class);
//			if (mPoiJsonArray != null) {
//				for (int i = 0; i < count; i++) {
//					PoiItem item = new PoiItem();
//					item.mIsBus = mIsBusiness;
//					Poi poi = null;
//					String jData = null;
//					try {
//						jData = mPoiJsonArray.getJSONObject(i).toString();
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//					if (mIsBusiness) {
//						poi = BusinessPoiDetail.fromString(jData);
//						mBusPois.add((BusinessPoiDetail) poi);
//					} else {
//						poi = Poi.fromString(jData);
//						mPois.add(poi);
//					}
//					poi.setAction(action);
//
//					final Poi navPoi = poi;
//					item.onClickListener = new OnClickListener() {
//
//						@Override
//						public void onClick(View v) {
//							JSONBuilder jb = new JSONBuilder();
//							jb.put("poi", navPoi.toString());
//							jb.put("action", action);
//							ServiceManager.getInstance().sendInvoke(
//									ServiceManager.TXZ,
//									"txz.selector.poi.onItemNaviClick",
//									jb.toBytes(), null);
//						}
//					};
//					item.mItem = poi;
//					mPoiItems.add(item);
//				}
//			}
//
//			OnItemClickListener listener = new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view,
//						int position, long id) {
//					JSONBuilder jb = new JSONBuilder();
//					jb.put("position", position);
//					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//							"txz.selector.poi.onItemClick", jb.toBytes(), null);
//				}
//			};
//
//			OnClickListener onClickListener = new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					JSONBuilder jb = new JSONBuilder();
//					jb.put("action", action);
//					jb.put("keywords", keyWord);
//					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//							"txz.selector.poi.edit", jb.toBytes(), null);
//				}
//			};
//
//			WinRecord.getInstance().addMsg(
//					ChatMsgFactory.getPoiMessageRef(mPoiItems, keyWord,
//							mIsBusiness, action, listener, onClickListener));
		}

		// 发送电台搜索列表
		else if (type != null && type == 3) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getDisplayMsgFromJson(new String(data)));
//			final String keyWord = doc.getVal("keywords", String.class);
//			final int count = doc.getVal("count", Integer.class);
//			List<AudioItem> mAudioItems = new ArrayList<AudioItem>();
//			for (int i = 0; i < count; i++) {
//				AudioItem item = new AudioItem();
//				MusicBean music = MusicBean.fromString(doc.getVal("music" + i,
//						String.class));
//				item.mItem = music;
//				mAudioItems.add(item);
//			}
//
//			OnItemClickListener listener = new OnItemClickListener() {
//
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view,
//						int position, long id) {
//					// AudioSelector.selectIndex(position, "");
//					JSONBuilder jb = new JSONBuilder();
//					jb.put("index", position);
//					jb.put("hint", "");
//					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//							"txz.selector.audio.selectIndex", jb.toBytes(),
//							null);
//				}
//			};
//			WinRecord.getInstance().addMsg(
//					ChatMsgFactory.getAudioMessageRef(mAudioItems, keyWord,
//							listener));
		} else if (type != null && type == 4) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getDisplayMsgFromJson(new String(data)));
//			JSONObject[] contacts = doc.getVal("audios", JSONObject[].class);
//			if (contacts != null) {
//				List<MusicItem> contactList = new ArrayList<MusicItem>();
//				for (JSONObject contact : contacts) {
//					JSONBuilder contactDoc = new JSONBuilder(contact);
//					String title = contactDoc.getVal("title", String.class);
//					String text = contactDoc.getVal("text", String.class);
//					if (text != null && title != null) {
//						// contactList.add(new AudioInfo(title, text));
//						MusicItem music = new MusicItem();
//						music.mItem = new AudioInfo(title, text);
//						contactList.add(music);
//					}
//				}
//				if (contactList.size() > 0) {
//					WinRecord.getInstance().addMsg(
//							ChatMsgFactory.getMusicMessageRef(contactList, "",
//									null));
//				}
//			}
		}
		// 发送联系人列表
		else {
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
					String province = contactJson.getVal("province",
							String.class);
					String city = contactJson.getVal("city", String.class);
					String isp = contactJson.getVal("isp", String.class);

					item.province = province;
					item.city = city;
					if (!isMultiName) {
						item.main = contactJson.getVal("number", String.class);
						item.isp = isp;
					} else {
						item.main = contactJson.getVal("name", String.class);
					}

					items.add(item);
				}
			}
			WinRecord.getInstance().addMsg(
					ChatMsgFactory.getSysContactListMsg(title, items));
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new RecordBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
}
