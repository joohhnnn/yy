package com.txznet.txz.module.weixin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makewechatssesion.UiMakeWechatSession;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechatcontact.WechatContactData;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.camera.CameraManager.CapturePictureListener;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.widget.PhotoFloatView;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.ImageUtil;

public class WeixinManager extends IModule {
	static WeixinManager sModuleInstance = new WeixinManager();
	private OnQRCodeListener mInnerCodeListener;
	private List<OnQRCodeListener> mOnQRCodeListeners = new ArrayList<OnQRCodeListener>();

	private WeixinManager() {
		mInnerCodeListener = new OnQRCodeListener() {

			@Override
			public void onGetQrCode(boolean isBind, String url) {
				for (OnQRCodeListener listener : mOnQRCodeListeners) {
					listener.onGetQrCode(isBind, url);
				}

				JSONBuilder jb = new JSONBuilder();
				jb.put("isBind", isBind);
				jb.put("url", url);
				ServiceManager.getInstance().broadInvoke("wx.qrcode.broadcast",
						jb.toBytes());
			}
		};
	}

	public static WeixinManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_UNBIND_WX_SUCCESS);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_NAVIGATION);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_UPLOAD_PIC);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_UPLOAD_VOICE);

		regEvent(UiEvent.EVENT_WECHAT_MAKESESSION);

		regCommand("WECHAT_LANUCH");
		regCommand("WECHAT_MSG_RESPONSE");
		regCommand("WECHAT_MSG_SEND");
		regCommand("WECHAT_SESSION_MASK");
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_UPLOAD_PIC);
		regEvent(UiEvent.EVENT_ACTION_WECHAT,
				UiWechat.SUBEVENT_GET_WECHAT_LOGIN_STATE);
		regEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_ERR_NO_CONTACT);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	WinConfirmAsr mWinConfirmNavigate;

	UiMap.NavigateInfo mLastNavigateInfo;

	//private String mSessionCmd = "";

	@Override
	public int onCommand(String cmd) {
		JNIHelper.logd("cmd" + cmd);
		do {
			if (cmd.equals("WECHAT_LANUCH")) {
				if (SenceManager.getInstance().noneedProcSence("app",
						new JSONBuilder().put("sence", "weixin").put("action", "open").toBytes())) {
					break;
				}
				
				launchWebChat();
				break;
			}

			if (!checkEnabled()) {
				requestLogin();
				break;
			}

			if (cmd.equals("WECHAT_MSG_RESPONSE")) {
				mWeChatChoice = ON_CALL;
				//mSessionCmd = cmd;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSession();
				break;
			}

			if (cmd.equals("WECHAT_MSG_SEND")) {
				mWeChatChoice = ON_CALL;
				//mSessionCmd = cmd;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSession();
				break;
			}

			if (cmd.equals("WECHAT_SESSION_MASK")) {
				mWeChatChoice = ON_SHIELD;
				//mSessionCmd = cmd;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSession();
				break;
			}
		} while (false);
		// return super.onCommand(cmd);
		return 0;
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_UPLOAD_VOICE: {
				try {
					JNIHelper.logd("upload voice response");
					UiEquipment.Resp_UploadVoice res = UiEquipment.Resp_UploadVoice
							.parseFrom(data);
					if (!res.bOk) {
						JNIHelper.loge("upload voice error: " + res.strErrMsg);
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wx.upload.voice.error",
								res.strErrMsg.getBytes(), null);
						break;
					}
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.upload.voice.success",
							res.strUrl.getBytes(), null);
				} catch (Exception e) {
					JNIHelper.loge("upload voice exception");
					e.printStackTrace();
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.upload.voice.error",
							null, null);
				}
				break;
			}
			case UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL: {
				try {
					UiEquipment.Resp_GetBindWxUrl res = UiEquipment.Resp_GetBindWxUrl
							.parseFrom(data);
					// if (res.bOk == false) {
					// JNIHelper.logd("bind weixin err: " + res.strErrMsg);
					// WinWeixinAssitor.getInstance().notifyException();
					// } else {
					// JNIHelper.logd("bind=" + res.bIsBind + ", url=" +
					// res.strBindWxUrl);
					// WinWeixinAssitor.getInstance().updateBindUrl(res.strBindWxUrl);
					// if (res.bIsBind) {
					// WinWeixinAssitor.getInstance().updateUserInfo(res.msgWx);
					// } else {
					// WinWeixinAssitor.getInstance().updateUserInfo(null);
					// }
					// }

					// isError
					// isBind
					// qrCode url
					// user info
					JSONBuilder builder = new JSONBuilder();
					builder.put("issuccess", res.bOk);
					if (!res.bOk) {

					} else {
						builder.put("qrcode", res.strBindWxUrl);
						builder.put("isbind", res.bIsBind);
						if (res.bIsBind) {
							builder.put("nick", res.msgWx.strNick);
						}
						if (res.msgServerConfigWechatInfo != null) {
							UiEquipment.SC_WeChat info = res.msgServerConfigWechatInfo;
							builder.put("uint64Flag", info.uint64Flag);
						}
						invokeOnQrCodeListener(res.bIsBind, res.strBindWxUrl);
					}
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.info.qrcode",
							builder.toString().getBytes(), null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_UNBIND_WX_SUCCESS: {
				try {
					UiEquipment.Notify_UnbindWxSuccess notify = UiEquipment.Notify_UnbindWxSuccess
							.parseFrom(data);
					JNIHelper.logd("unbind: url=" + notify.strBindWxUrl);

					JSONBuilder builder = new JSONBuilder();
					builder.put("issuccess", true);
					builder.put("qrcode", notify.strBindWxUrl);
					builder.put("isbind", false);
					builder.put("nick", null);

					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.info.qrcode",
							builder.toString().getBytes(), null);
					invokeOnQrCodeListener(false, notify.strBindWxUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS: {
				try {
					UiEquipment.Notify_BindWxSuccess notify = UiEquipment.Notify_BindWxSuccess
							.parseFrom(data);
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.info.nick",
							notify.msgWx.strNick.getBytes(), null);
					invokeOnQrCodeListener(true, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_NAVIGATION: {
				try {
					final UiMap.NavigateInfo info = mLastNavigateInfo = UiMap.NavigateInfo
							.parseFrom(data);

					if (info.msgServerPushInfo != null) {
						JNIHelper.logd("time="
								+ info.msgServerPushInfo.uint32Time + ", nick="
								+ info.msgServerPushInfo.strFromWxNick
								+ ",now=" + System.currentTimeMillis());

						if (TextUtils.isEmpty(info.strTargetAddress)) {
						
							// 地址为空时通过地理信息转换重新获取
							try {
								GeocodeSearch geocodeSearch = new GeocodeSearch(GlobalContext.get());
								geocodeSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
									
									@Override
									public void onRegeocodeSearched(RegeocodeResult result, int resultID) {
										if (info != mLastNavigateInfo) {
											return;
										}
										mLastNavigateInfo = null;
										if(result!=null){
											RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress();
											if(regeocodeAddress!=null){
												info.strTargetAddress = regeocodeAddress.getFormatAddress();
												
											}else{
												info.strTargetAddress = "未知地址";
											}
										
										}else{
											info.strTargetAddress = "未知地址";
										}
										JNIHelper
										.sendEvent(
												UiEvent.EVENT_ACTION_EQUIPMENT,
												UiEquipment.SUBEVENT_NOTIFY_NAVIGATION,
												info);
										
									}
									
									@Override
									public void onGeocodeSearched(GeocodeResult result, int resultID) {
									}
								});
								LatLonPoint latLonPoint = new LatLonPoint(info.msgGpsInfo.dblLat,
										info.msgGpsInfo.dblLng);
								RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
								geocodeSearch.getFromLocation(regeocodeQuery);
								JNIHelper.logd("WeixinManager GeocodeSearch strTargetAddress:"+info.strTargetAddress);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							break;
						}

						// String time = "";
						// if (ProtoBufferUtil.isIntegerZero(notify.uint32Time)
						// ==
						// false) {
						// Date date = new Date(((long) notify.uint32Time) *
						// 1000);
						// time = new SimpleDateFormat("HH:mm",
						// Locale.CHINESE).format(date);
						// }

						if (info.msgServerPushInfo.uint32Type == UiMap.NT_MULTI_NATIGATION) {// 多车同行

							String message = new String();
							String tts = new String();
							if (info.msgServerPushInfo.uint64FromUid == NativeData
									.getUID()) {
								// 本人
								message = NativeData
										.getResString("RS_WX_MULTI_NAV_YOUR_HINT")
										.replace(
												"%TIME%",
												makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
										.replace(
												"%TAR%",
												info.strTargetName + "["
														+ info.strTargetAddress
														+ "]");
								tts = NativeData
										.getResString("RS_WX_MULTI_NAV_YOUR")
										.replace(
												"%TIME%",
												makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
										.replace(
												"%TAR%",
												info.strTargetName + "["
														+ info.strTargetAddress
														+ "]");
							} else {
								message = NativeData
										.getResString("RS_WX_MULTI_NAV_FRIEND_HINT")
										.replace(
												"%NAME%",
												info.msgServerPushInfo.strFromWxNick)
										.replace(
												"%TIME%",
												makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
										.replace(
												"%TAR%",
												info.strTargetName + "["
														+ info.strTargetAddress
														+ "]");

								tts = NativeData
										.getResString("RS_WX_MULTI_NAV_FRIEND")
										.replace(
												"%NAME%",
												info.msgServerPushInfo.strFromWxNick)
										.replace(
												"%TIME%",
												makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
										.replace(
												"%TAR%",
												info.strTargetName + "["
														+ info.strTargetAddress
														+ "]");
							}

							multiNavigateConfirm(message, tts,
									new Runnable() {
										@Override
										public void run() {
											// TtsManager.getInstance().speakText("好的，即将开始进入多车同行，并规划目的地");
										}
									}, info);

						} else {
							String hint = NativeData
									.getResString("RS_WX_RECEIVE_NAV_HINT")
									.replace(
											"%TIME%",
											makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
									.replace(
											"%NAME%",
											info.msgServerPushInfo.strFromWxNick)
									.replace(
											"%TAR%",
											info.strTargetName + "["
													+ info.strTargetAddress
													+ "]");
							String spk = NativeData
									.getResString("RS_WX_RECEIVE_NAV")
									.replace(
											"%TIME%",
											makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
									.replace(
											"%NAME%",
											info.msgServerPushInfo.strFromWxNick)
									.replace(
											"%TAR%",
											info.strTargetName + "["
													+ info.strTargetAddress
													+ "]");

							navigateConfirm(hint, spk,
									new Runnable() {
										@Override
										public void run() {
											// TtsManager.getInstance().speakText("好的，即将开始规划目的地");
										}
									}, info);
						}
					} else {
						JNIHelper.loge("info.msgServerPushInfo is null");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_UPLOAD_PIC: {
				// GlobalContext.get().sendBroadcast(
				// new Intent("com.android.camera.PHOTOCAPTURE"));
				capturePhoto(0, true, null);
				break;
			}
			case UiEquipment.SUBEVENT_RESP_UPLOAD_PIC:
				break;
			}
			break;
		case UiEvent.EVENT_WECHAT_MAKESESSION:
			doWeChatMakeSession(subEventId, data);
			break;

		case UiEvent.EVENT_ACTION_WECHAT:
			switch (subEventId) {
			case com.txz.ui.wechat.UiWechat.SUBEVENT_GET_WECHAT_LOGIN_STATE:
				doGetWechatLoginState();
				break;
			case UiWechat.SUBEVENT_ERR_NO_CONTACT:
				doWechatNoContact();
				break;
			}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	private void doWechatNoContact(){
		if(checkEnabled()){
			TtsUtil.speakTextOnRecordWin("未找到相关联系人", false, null);
		}else{
			requestLogin();
		}
	}

	/** 拍照后TIME_SHARE_PHOTO内可以分享照片 */
	public final int TIME_SHARE_PHOTO = 6000;

	/**
	 * 拍照后分享图片功能是否开启
	 */
	public boolean isSharePhotoEnabled() {
		if (isWeixinInstalled() && checkEnabled()) {
			return true;
		}
		return false;
	}

	private PhotoFloatView mPhotoFloatView;
	private String mShareUrl;

	public void startSharePhoto(final String url) {
		mShareUrl = url;
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mPhotoFloatView != null && mPhotoFloatView.isShowing()) {
					mPhotoFloatView.dismiss();
				}

				mPhotoFloatView = new PhotoFloatView(url);
				mPhotoFloatView.show();
			}
		}, 0);
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mPhotoFloatView != null) {
					mPhotoFloatView.dismiss();
				}
			}
		}, 2000);
		String spk = NativeData.getResString("RS_WX_SHARE_PIC");
		TtsManager.getInstance().speakText(spk,
				new ITtsCallback() {
					@Override
					public void onEnd() {
						AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
							@Override
							public String getTaskId() {
								return "WAKEUP_SHARE";
							}

							@Override
							public boolean needAsrState() {
								return true;
							}

							@Override
							public void onCommandSelected(String type,
									String command) {
								AppLogicBase
										.removeBackGroundCallback(mReleaseWakeupShare);
								mReleaseWakeupShare.run();
								procSharePhotoSence();
							}
						}.addCommand("SHARE", "分享照片"));
						AppLogicBase
								.removeBackGroundCallback(mReleaseWakeupShare);
						AppLogicBase.runOnBackGround(mReleaseWakeupShare,
								TIME_SHARE_PHOTO);
					}
				});
	}

	public void procSendMsgSence() {
		mWeChatChoice = ON_CALL;
		RecorderWin.show();
		String spk = NativeData.getResString("RS_WX_SEARCH_CONVERSATION");
		RecorderWin.addSystemMsg(spk);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
		requestRecentSession();
	}

	public void procSharePhotoSence() {
		mWeChatChoice = ON_PHOTO;
		RecorderWin.show();
		String spk = NativeData.getResString("RS_WX_SEARCH_CONVERSATION");
		RecorderWin.addSystemMsg(spk);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
		requestRecentSession();
	}

	static Runnable mReleaseWakeupShare = new Runnable() {
		@Override
		public void run() {
			AsrUtil.recoverWakeupFromAsr("WAKEUP_SHARE");
		}
	};

	public void capturePhoto(final long time,
			final boolean shouldUploadWithError, final Runnable runEnd) {
		CameraManager.getInstance().capturePicture(time,
				new CapturePictureListener() {
					private void onEnd() {
						if (runEnd != null)
							runEnd.run();
					}

					@Override
					public void onSave(String path) {
						JNIHelper.logd("capturePicture onSave, path=" + path);
						if (new File(path).exists() == false) {
							JNIHelper
									.logd("capturePicture file not exists, path="
											+ path);
							onError(7104, "请检查SD卡状态是否正常");
							return;
						}
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						String dirPath = Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/txz/cache/";
						new File(dirPath).mkdirs();
						String upload_path = dirPath + "~upload.jpg";
						String thumb_path = dirPath + "~upload_thumb.jpg";
						try {
							ImageUtil.resizeImage(path, upload_path, 0.5f, 80);
							ImageUtil.resizeImageAlignWidth(path, thumb_path,
									320, 50);
						} catch (Throwable e) {
							JNIHelper.loge("capturePicture resizeImage error["
									+ e.getClass() + "::" + e.getMessage()
									+ "]");
						}
						req.strPicPath = upload_path;
						req.strThumbPicPath = thumb_path;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);
						if (!shouldUploadWithError) {
							String spk = NativeData.getResString("RS_WX_CAPTURE_PICTURE");
							TtsManager.getInstance().speakText(spk);
						}

						if (WeixinManager.getInstance().isSharePhotoEnabled())
							WeixinManager.getInstance().startSharePhoto(
									upload_path);

						onEnd();
					}

					@Override
					public void onError(int errCode, String errDesc) {
						if (!shouldUploadWithError) {
							String spk = NativeData.getResPlaceholderString(
									"RS_WX_CAPTURE_ERROR", "%CMD%", errDesc);
							TtsUtil.speakText(spk);
							onEnd();
							return;
						}
						JNIHelper.logw("capture error: " + errCode + "-"
								+ errDesc);
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						req.uint32ErrCode = errCode;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);

						onEnd();
					}

					@Override
					public void onTimeout() {
						if (!shouldUploadWithError) {
							String spk = NativeData.getResString("RS_WX_CAPTURE_FAIL");
							TtsUtil.speakText(spk);
							onEnd();
							return;
						}
						UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
						req.uint32ErrCode = 7121;
						JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
								UiEquipment.SUBEVENT_REQ_UPLOAD_PIC, req);

						onEnd();
					}
				}, 10000);
	}

	// 根据时间差转换成友好提示
	// ms : 时间毫秒数
	private String makeFriendlyTime(long ms) {
		int minute = Math.round((System.currentTimeMillis() - ms)
				/ (60 * 1000f));
		if (minute < 5) {
			return "";
		} else if (minute <= 10) {
			return minute + "分钟前";
		} else if (minute < 60) {
			return Math.round(minute / 10f) * 10 + "分钟前";
		} else if (minute < 60 * 24) {
			return minute / 60 + "小时前";
		} else if (minute < 60 * 24 * 7) {
			return minute / 60 / 24 + "天";
		}
		return "";
	}

	class NavigateConfirmData {
		Runnable run;
		NavigateInfo info;
	}

	private long mDismissDelay;

	public void setConfirmDismissDelay(long delay) {
		this.mDismissDelay = delay;
	}

	Runnable mDismissTask = new Runnable() {

		@Override
		public void run() {
			if (mWinConfirmNavigate != null) {
				mWinConfirmNavigate.dismiss();
			}
		}
	};

	void checkDismissConfirm() {
		AppLogic.removeUiGroundCallback(mDismissTask);
		if (mDismissDelay >= 1000) {
			AppLogic.runOnUiGround(mDismissTask, mDismissDelay);
		}
	}

	void multiNavigateConfirm(String message, String hint, Runnable run,
			NavigateInfo info) {
		if (mWinConfirmNavigate != null) {
			mWinConfirmNavigate.dismiss();
		}

		NavigateConfirmData data = new NavigateConfirmData();
		data.info = info;
		data.run = run;
		mWinConfirmNavigate = new WinConfirmAsr(true) {
			@Override
			public void onClickOk() {
				NavigateConfirmData data = (NavigateConfirmData) getMessageData();
				NavManager.getInstance().NavigateTo(data.info);
			}

			@Override
			public void onSpeakOk() {
				this.dismiss();
				AppLogic.removeUiGroundCallback(mDismissTask);
				String spk = NativeData.getResString("RS_WX_PATH_PLAN");
				TtsManager.getInstance().speakText(spk,
						new ITtsCallback() {
							@Override
							public void onSuccess() {
								onClickOk();
							};
						});
			}
		}.setMessageData(data)/* .setTitle("导航确认") */.setMessage(message)
				.setHintTts(hint)
				.setSureText("加入", new String[] { "加入", "导航", "确定", "开始" })
				.setCancelText("取消", new String[] { "取消", "放弃", "返回" });
		mWinConfirmNavigate.setTtsEndRunnable(new Runnable() {

			@Override
			public void run() {
				checkDismissConfirm();
			}
		});
		mWinConfirmNavigate.show();
	}

	public void navigateConfirm(final String message, final String hint,
			final Runnable run, final NavigateInfo info) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mWinConfirmNavigate != null) {
					mWinConfirmNavigate.dismiss();
				}
				NavigateConfirmData data = new NavigateConfirmData();
				data.info = info;
				data.run = run;
				mWinConfirmNavigate = new WinConfirmAsr(true) {
					@Override
					public void onClickOk() {
						NavigateConfirmData data = (NavigateConfirmData) getMessageData();
						NavManager.getInstance().NavigateTo(data.info);
					}

					@Override
					public void onSpeakOk() {
						this.dismiss();
						AppLogic.removeUiGroundCallback(mDismissTask);
						String spk = NativeData.getResString("RS_WX_PATH_PLAN");
						TtsManager.getInstance().speakText(spk,
								new ITtsCallback() {
									@Override
									public void onSuccess() {
										onClickOk();
									};
								});
					}

					@Override
					public void onClickCancel() {
						String spk = NativeData.getResString("RS_WX_CANCEL");
						TtsManager.getInstance().speakText(spk);
					};
				}.setMessageData(data)/* .setTitle("导航确认") */.setMessage(message)
						.setHintTts(hint)
						.setSureText("导航", new String[] { "导航", "确定", "开始" })
						.setCancelText("取消", new String[] { "取消", "放弃", "返回" });
				mWinConfirmNavigate.setTtsEndRunnable(new Runnable() {

					@Override
					public void run() {
						checkDismissConfirm();
					}
				});
				mWinConfirmNavigate.show();
			}
		}, 0);
	}

	Thread mThreadGetLocation;

	public static final int OFF_LINE = 0;
	public static final int ON_UPDATING_USER = 1;
	public static final int ON_LINE = 2;
	public static final int ON_INIT = 0;
	public static final int ON_CALL = 1;
	public static final int ON_SHIELD = 2;
	public static final int ON_PLACE = 3;
	public static final int ON_HISTORY = 4;
	public static final int ON_PHOTO = 5;
	public static final int ON_UNSHILED = 6;

	public int mWeChatState = OFF_LINE;
	public int mWeChatChoice = ON_INIT;

	private void launchWebChat() {
		// 默认按getLaunchIntentForPackage方式启动
		final Intent intent = GlobalContext.get().getPackageManager()
				.getLaunchIntentForPackage("com.txznet.webchat");
		if (intent == null) {
			String spk = NativeData.getResString("RS_WX_NOT_INSTALL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		if (RecordInvokeFactory.isHudRecordWin()) {

		}

		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
		String spk = NativeData.getResString("RS_WX_WILL_OPEN");
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				launchWebchatByVersion(intent);
			}
		});
	}

	private void launchWebchatByVersion(Intent intent) {
		PackageInfo info = null;

		try {
			info = GlobalContext.get().getPackageManager()
					.getPackageInfo("com.txznet.webchat", 0);

			if (info.versionCode < 11) {
				performLaunchWebchat(intent);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
						"wechat.ctrl.launch", null, null);
			}
		} catch (Exception e) {
			performLaunchWebchat(intent);
		}
	}

	private void performLaunchWebchat(Intent intent) {
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// 添加标志位，登录成功后退出微信界面
			intent.putExtra("quitAfterLogin", true);
			GlobalContext.get().startActivity(intent);
			return;
		}
	}

	private void setLoginState(int state) {
		mWeChatState = state;
	}

	public boolean checkEnabled() {
		return mWeChatState == ON_LINE;
	}

	private void doWeChatMakeSession(final int subEventId, byte[] data) {
		mWeChatChoice = ON_INIT;
		boolean isSubEvent = true;
		switch (subEventId) {
		case UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_UNSHILED;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_HISTORY_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_HISTORY_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_HISTORY;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_PLACE_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_PLACE_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_PLACE;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_SHIELD;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_SESSION_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_CALL;
				isSubEvent = false;
			}
			JNIHelper.logd("mWeChoice=" + mWeChatChoice);
			if (!checkEnabled()) {
				requestLogin();
				return;
			}
			try {
				final WeChatContacts targets = WeChatContacts.parseFrom(data);
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				if (targets.cons != null && targets.cons.length != 0) {

					AppLogic.runOnBackGround(new Runnable() {

						@Override
						public void run() {
							SelectorHelper.entryWxContactSelector(subEventId,
									targets, "");
						}
					}, 50);
				} else {
					// 若是解除屏蔽，请求屏蔽列表
					if (mWeChatChoice == ON_UNSHILED) {
						requestMaskedSession();
					} else {
						requestRecentSession();
					}
				}
			} catch (Exception e) {
				if (mWeChatChoice == ON_UNSHILED) {
					requestMaskedSession();
				} else {
					requestRecentSession();
				}
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}

	public boolean isWeixinInstalled() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo("com.txznet.webchat", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo == null ? false : true;
	}

	public void requestLogin() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo("com.txznet.webchat", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageInfo == null) {
			String spk = NativeData.getResString("RS_WX_NOT_INSTALL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (packageInfo.versionCode <= 9) {
			AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
			String spk = NativeData.getResString("RS_WX_NOT_LOGIN");
			RecorderWin.open(spk);
		} else if (packageInfo.versionCode <= 10) {
			String spk = NativeData.getResString("RS_WX_NOT_LOGIN_CODE");
			TtsManager.getInstance().speakText(spk);
			JNIHelper.logi("send wx.contact.need.login");
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.need.login", null, null);
			RecorderWin.dismiss();
		} else {
			Boolean needTts = true;
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.need.login", needTts.toString().getBytes(),
					null);
			if (RecordInvokeFactory.isHudRecordWin()) {
				return;
			}

			RecorderWin.dismiss();
		}
	}

	/*
	 * function:发起微信会话 who:联系人昵称 id:联系人ID
	 */
	public void makeSession(String who, String id) {
		JSONBuilder doc = new JSONBuilder().put("name", who).put("id", id);
		JNIHelper.logd("send Message to weixin:" + mWeChatChoice);
		switch (mWeChatChoice) {
		case ON_SHIELD:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.mask", doc.toString().getBytes(), null);
			break;
		case ON_PLACE:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.sharePlace", doc.toString().getBytes(), null);
			break;
		case ON_HISTORY:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.history", doc.toString().getBytes(), null);
			break;
		case ON_PHOTO:
			doc.put("data", mShareUrl);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.sharePhoto", doc.toString().getBytes(), null);
			break;
		case ON_UNSHILED:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.unmask", doc.toString().getBytes(), null);
			break;
		default:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.make", doc.toString().getBytes(), null);
			break;
		}
		mWeChatChoice = ON_INIT;
	}

	/*
	 * 更新微信客户端登陆状态 status: 0:offline 2:online
	 */
	public void updateLoginState(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}

		if (!comm.equals("wx.loginstate.update")) {
			return;
		}

		String jsonStr = new String(data);
		try {
			JSONObject json = new JSONObject(jsonStr);
			int state = json.getInt("status");
			setLoginState(state);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 更新微信联系人 comm : wx.contact.update
	 */
	public void updateWeChatContact(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (comm.equals("wx.contact.update")) {
			WeChatContacts weChatContacts = null;
			String jsonStr = new String(data);
			try {
				weChatContacts = new WeChatContacts();

				JSONObject json = null;
				JSONArray jsonArray = null;
				do {
					json = new JSONObject(jsonStr);
					jsonArray = json.getJSONArray("list");

					if (null == jsonArray) {
						break;
					}

					int length = jsonArray.length();
					if (length <= 0) {
						break;
					}

					weChatContacts.cons = new WeChatContact[length];

					for (int i = 0; i < jsonArray.length(); ++i) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject == null) {
							continue;
						}

						WeChatContact con = new WeChatContact();
						con.id = jsonObject.getString("id");
						con.name = jsonObject.getString("name");
						try {
							con.uint32Type = jsonObject.getInt("type");
						} catch (JSONException e) {
							con.uint32Type = WechatContactData.TYPE_FRIEND;
						}
						weChatContacts.cons[i] = con;
					}

					JNIHelper
							.sendEvent(
									UiEvent.EVENT_WECHAT_FRIENDSHIP,
									WechatContactData.SUBEVENT_UPDATED_WECHAT_CONTACT_LIST,
									weChatContacts);

				} while (false);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * 更新微信最近会话 comm : "wx.contact.recentsession.update"
	 */
	public synchronized void updateWeChatRecentChat(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (mQuestTimeOutTask == null) {
			return;
		}
		if (comm.equals("wx.contact.recentsession.update")) {
			AppLogic.removeBackGroundCallback(mQuestTimeOutTask);
			mQuestTimeOutTask = null;

			WeChatContacts weChatContacts = null;
			String jsonStr = new String(data);
			try {
				weChatContacts = new WeChatContacts();

				JSONObject json = null;
				JSONArray jsonArray = null;
				do {
					json = new JSONObject(jsonStr);
					jsonArray = json.getJSONArray("list");

					if (null == jsonArray) {
						break;
					}

					int length = jsonArray.length();
					if (length <= 0) {
						break;
					}

					weChatContacts.cons = new WeChatContact[length];

					for (int i = 0; i < jsonArray.length(); ++i) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject == null) {
							continue;
						}

						WeChatContact con = new WeChatContact();
						con.id = jsonObject.getString("id");
						con.name = jsonObject.getString("name");
						weChatContacts.cons[i] = con;
					}

					String strText = NativeData
							.getResString("RS_WX_SELECT_LIST_SPK");;
					//strText = NativeData.getResString("RS_WX_SELECT_LIST_SPK").replace("%COUNT%", length+"");

//					WeixinSelectControl.showContactSelectList(
//							UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
//							weChatContacts, strText);
					SelectorHelper.entryWxContactSelector(UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
							weChatContacts, strText);
					return;
				} while (false);
				String spk = NativeData.getResString("RS_WX_CONTACTS_NOT_FOUND");
				RecorderWin.speakTextWithClose(spk, null);

			} catch (JSONException e) {
				e.printStackTrace();
				String spk = NativeData.getResString("RS_WX_CONTACTS_SYNC_FAIL");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}

	}

	/*
	 * 更新微信屏蔽列表 comm : "wx.contact.maskedsession.update"
	 */
	public synchronized void updateWeChatMaskedChat(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (mQuestTimeOutTask == null) {
			return;
		}
		if (comm.equals("wx.contact.maskedsession.update")) {
			AppLogic.removeBackGroundCallback(mQuestTimeOutTask);
			mQuestTimeOutTask = null;

			WeChatContacts weChatContacts = null;
			String jsonStr = new String(data);
			try {
				weChatContacts = new WeChatContacts();

				JSONObject json = null;
				JSONArray jsonArray = null;
				do {
					json = new JSONObject(jsonStr);
					jsonArray = json.getJSONArray("list");

					if (null == jsonArray) {
						break;
					}

					int length = jsonArray.length();
					if (length <= 0) {
						break;
					}

					weChatContacts.cons = new WeChatContact[length];

					for (int i = 0; i < jsonArray.length(); ++i) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject == null) {
							continue;
						}

						WeChatContact con = new WeChatContact();
						con.id = jsonObject.getString("id");
						con.name = jsonObject.getString("name");
						weChatContacts.cons[i] = con;
					}

					String strText = null;
					/*if (mSessionCmd.equals("WECHAT_MSG_RESPONSE")) {
						// strText = "为您找到以下微信会话，请选择第几个或说回复微信给谁";
						strText = "为您找到如下屏蔽联系人列表，请选择第几个或取消";
					} else if (mSessionCmd.equals("WECHAT_MSG_SEND")) {
						// strText = "为您找到以下微信会话，请选择第几个或说发微信给谁";
						strText = "为您找到如下屏蔽联系人列表，请选择第几个或取消";
					}*/
					strText = NativeData.getResString("RS_WX_SELECT_SHILED_SPK");

//					WeixinSelectControl.showContactSelectList(
//							UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT,
//							weChatContacts, strText);
					SelectorHelper.entryWxContactSelector(
							UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT,
							weChatContacts, strText);
					return;
				} while (false);
				mWeChatChoice = ON_INIT;
				String spk = NativeData.getResString("RS_WX_NOT_SHIELD_CONTACTS");
				RecorderWin.speakTextWithClose(spk, null);

			} catch (JSONException e) {
				e.printStackTrace();
				mWeChatChoice = ON_INIT;
				String spk = NativeData.getResString("RS_WX_SHIELD_SYNC_FAIL");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}

	}

	private Runnable mQuestTimeOutTask = null;

	/*
	 * 更新微信最近会话 comm : "wx.contact.recentsession.request"
	 */
	public synchronized void requestRecentSession() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.contact.recentsession.request", null, null);
		mQuestTimeOutTask = new Runnable() {
			@Override
			public void run() {
				questTimeOut();
			};
		};
		AppLogic.runOnBackGround(mQuestTimeOutTask, 5000);
	}

	/*
	 * 更新微信屏蔽会话 comm : "wx.contact.maskedsession.request"
	 */
	public synchronized void requestMaskedSession() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.contact.maskedsession.request", null, null);
		mQuestTimeOutTask = new Runnable() {
			@Override
			public void run() {
				questTimeOut();
			};
		};
		AppLogic.runOnBackGround(mQuestTimeOutTask, 2000);
	}

	private synchronized void questTimeOut() {
		if (mQuestTimeOutTask == null) {
			return;
		}
		mQuestTimeOutTask = null;
		String spk = NativeData.getResString("RS_WX_CONTACTS_SYNC_TIMEOUT");
		RecorderWin.speakTextWithClose(spk, null);
	}

	public void addOnQrCodeListener(OnQRCodeListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnQRCodeListeners.contains(listener)) {
			return;
		}

		mOnQRCodeListeners.add(listener);
	}

	public void onRemoveQrCodeListener(OnQRCodeListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnQRCodeListeners.contains(listener)) {
			mOnQRCodeListeners.remove(listener);
		}
	}

	private void invokeOnQrCodeListener(boolean isBind, String url) {
		mInnerCodeListener.onGetQrCode(isBind, url);
	}

	public interface OnQRCodeListener {
		public void onGetQrCode(boolean isBind, String url);
	}

	public void doGetWechatLoginState() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.state.login.req", null, null);
	}

	public void doReportWxchatLoginState(byte[] data) {
		try {
			JSONBuilder doc = new JSONBuilder(data);
			EquipmentManager.Req_WeChatLoginState state = new EquipmentManager.Req_WeChatLoginState();
			state.bLogin = doc.getVal("status", Integer.class) == 2;
			if (state.bLogin) {
				state.strNick = doc.getVal("nick", String.class);
				state.uint32LoginTime = doc.getVal("loginTime", Integer.class);
			} else {
				state.strCodeData = doc.getVal("code", String.class).getBytes();
			}
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT,
					UiWechat.SUBEVENT_NOTIFY_WECHAT_LOGIN_STATE, state);
			JNIHelper.logd("report_login_state:bLogin=" + state.bLogin
					+ ", nick=" + state.strNick + ", code="
					+ doc.getVal("code", String.class));
		} catch (Exception e) {

		}
	}
}
