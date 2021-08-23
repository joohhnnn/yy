package com.txznet.txz.service;

import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;

import com.txz.ui.data.UiData.UserConfig;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txz.ui.music.UiMusic;
import com.txz.ui.platform.UiPlatform;
import com.txznet.comm.base.BaseForegroundService;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceCallerCheck;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.remote.util.TtsUtil.VoiceTask;
import com.txznet.comm.remote.util.VoiceprintRecognitionUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZCarControlHomeManager;
import com.txznet.sdk.TXZConfigManager.AsrMode;
import com.txznet.sdk.TXZConfigManager.AsrServiceMode;
import com.txznet.sdk.TXZConfigManager.InterruptMode;
import com.txznet.sdk.TXZDownloadManager;
import com.txznet.sdk.TXZStockManager;
import com.txznet.sdk.TXZTicketManager;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.sdkinner.TXZInnerUpgradeManager;
import com.txznet.sdk.TXZTtsManager.TtsTheme;
import com.txznet.sdk.TXZWeatherManager;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.AsrProxy;
import com.txznet.txz.component.asr.remote.AsrRemoteImpl;
import com.txznet.txz.component.choice.list.MovieWorkChoice;
import com.txznet.txz.component.command.CommandManager;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.poi.remote.PoiSearchToolRemoteImpl;
import com.txznet.txz.component.tts.remote.TtsRemoteImpl;
import com.txznet.txz.component.wakeup.mix.WakeupProxy;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.ac.ACManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.constellation.ConstellationManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.dns.DnsManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.home.CarControlHomeManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.stock.StockManager;
import com.txznet.txz.module.sys.SysTool;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.ticket.TicketManager;
import com.txznet.txz.module.transfer.TransferManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ttsplayer.TtsPlayerManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.version.UpgradeManager;
import com.txznet.txz.module.version.VisualUpgradeManager;
import com.txznet.txz.module.voiceprintrecognition.VoiceprintRecognitionManager;
import com.txznet.txz.module.voiceprintrecognition.voiceai.VPRCSDKWrapper;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weather.WeatherManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.module.wheelcontrol.WheelControlManager;
import com.txznet.txz.notification.NotificationManager;
import com.txznet.txz.udprpc.TXZUdpServer;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import static com.txznet.comm.ui.ReverseObservable.NOTIFY_OBSERVER_ACTION_REVERSE;

public class TXZService extends BaseForegroundService {
    private class TxzBinder extends IService.Stub {
        @Override
        public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
            try {
				if (ServiceCallerCheck.checkBinderCaller(packageName, command, data) == false) {
					return null;
				}
                return _sendInvoke(packageName, command, data);
            } catch (Exception e) {
                // 被调过程中的异常会被调用端捕获，此处强制杀进程将异常抛出
                CrashCommonHandler.getInstance().uncaughtException(Thread.currentThread(), e);
            }

            return null;
        }

        public byte[] _sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
            if (TextUtils.isEmpty(command))
                return null;
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            if (command.equals("comm.exitTXZ")) {
                MtjModule.getInstance().eventEnd(MtjModule.EVENTID_USE_TIME);
                // 通知所有调用程序已结束
                ServiceManager.getInstance().broadInvoke("comm.exitTXZ.exited", null);
                TXZPowerControl.releaseTXZ();
                return null;
            }
            if (command.equals("comm.restartProcess")) {
                LogUtil.logd("comm.restartProcess");
                AppLogic.restartProcess();
                return null;
            }
            if (command.startsWith("txz.sdk.")) {
                return invokeSDK(packageName, command, data);
            }
            // 放在前面是为了避免刚初始化成功时第三方界面还未设置成功时，用户点击悬浮图标显示了默认界面
            if (command.startsWith("txz.record.win.prepare")) {
                return WinManager.getInstance().invokeRecordWin(packageName, command.substring("txz.record.win.".length()),
                        data);
            }
            if (command.startsWith("txz.recordwin2.set")) {
                return WinManager.getInstance().invokeRecordWin2(packageName, command.substring("txz.recordwin2.".length()), data);
            }
            //浅休眠后保持微信的远程功能
            if (command.startsWith("txz.camera.")) {
                return CameraManager.getInstance().invokeTXZCamera(packageName,
                        command.substring("txz.camera.".length()), data);
            }
            // 未初始化也要保证udp功能可用
            if (command.equals("txz.udp.init")) {
                return TXZUdpServer.getInstance().onInvoke(packageName, command, data);
            }
            if (AppLogic.isInited() == false && blockKeyword(command)) {
                return null;
            }
            // 需要先执行初始化
            // if (!command.equals("comm.log"))
            // JNIHelper.logd("packageName=" + packageName + ", command="
            // + command + ", data=" + data);
            byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
            if ("comm.log".equals(command)) { //这个日志接口是相等
                return invokeCommLog(packageName, command, data);
            }
            if (command.startsWith("comm.report.type.")) {
            	AppLogic.runOnBackGround(new Runnable2<String, byte[]>(command,data) {
            		@Override
            		public void run() {
                        try {
                            JNIHelper.doReport(Integer.parseInt(mP1.substring("comm.report.type.".length())), mP2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
            		}
				});
                return null;
            }
            if (command.startsWith("comm.report.imme.")) {
				AppLogic.runOnBackGround(new Runnable2<String, byte[]>(command, data) {
					@Override
					public void run() {
						try {
							JNIHelper.doReportImmediate(Integer.parseInt(mP1.substring("comm.report.type.".length())),
									mP2);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
                return null;
            }
            if (command.equals("comm.monitor")) {
                try {
                    JSONBuilder json = new JSONBuilder(data);
                    JNIHelper.monitor(json.getVal("type", Integer.class), json.getVal("val", Integer.class), json.getVal("attrs", String[].class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            if (command.startsWith("txz.transfer.")) {
                return TransferManager.getInstance().invokeTransfer(packageName,command.substring("txz.transfer.".length()), data);
            }
            if (command.startsWith("comm.tts.")) {
                return invokeCommTts(packageName, command, data);
            }
            if (command.startsWith(TXZTtsPlayerManager.TTS_PLAYER_INVOKE_PREFIX)) {
                return TtsPlayerManager.getInstance().invokeCommTts(packageName, command, data);
            }
            if (command.startsWith(VoiceprintRecognitionUtil.PREFIX_SEND)) {
                return VoiceprintRecognitionManager.getInstance().invokeCommVoiceprintRecognition(packageName, command, data);
            }
            if (command.startsWith("comm.location")) {
                return invokeCommLocation(packageName, command, data);
            }
            if (command.startsWith("comm.asr.")) {
                return AsrManager.getInstance().invokeCommAsr(packageName, command, data);
            }
            if (command.startsWith("comm.record")) {
                return RecordManager.getInstance().invokeCommRecord(packageName, command, data);
            }
            if (command.startsWith("comm.status.")) {
                return invokeCommStatus(packageName, command, data);
            }
            if (command.startsWith("comm.power.")) {
                return invokeCommPower(packageName, command.substring("comm.power.".length()), data);
            }

            if (command.startsWith("wakeup.")) {
				//在倒车影像中，不允许启动唤醒
				if (TXZPowerControl.isEnterReverse()) {
					return null;
				}
                return invokeWakeup(packageName, command, data);
            }
            if (command.startsWith("team.")) {
                return invokeTeam(packageName, command, data);
            }
            if (command.startsWith("txz.music.")) {
                String cmd = command.substring("txz.music.".length());
                if (cmd.equals("play")) {
                	cmd += ".extra";
                }
                return MusicManager.getInstance().invokeTXZMusic(packageName,
                		cmd, data);
            }
            if (command.startsWith("txz.audio.")) {
                return AudioManager.getInstance().invokeTXZAudio(packageName, command.substring("txz.audio.".length()),
                        data);
            }
            if (command.startsWith("txz.call.")) {
                return CallManager.getInstance().invokeTXZCall(packageName, command.substring("txz.call.".length()),
                        data);
            }
            if (command.startsWith("txz.sim.")) {
                return SimManager.getInstance().invokeTXZSim(packageName, command.substring("txz.sim.".length()), data);
            }
            if (command.startsWith("txz.reminder.")) {
                return ReminderManager.getInstance().invokeReminder(packageName, command, data);
            }
            if (command.startsWith("txz.constellation.")) {
                return ConstellationManager
                        .getInstance().invokeConstellation(packageName, command, data);
            }
            if (command.startsWith("txz.mtj.")) {
                return MtjModule.getInstance().invokeTXZMtj(packageName, command.substring("txz.mtj.".length()), data);
            }
            if (command.startsWith("txz.config.")) {
                return ConfigManager.getInstance().invokeTXZConfig(packageName,
                        command.substring("txz.config.".length()), data);
            }
            if (command.startsWith("txz.record.ui.")) {
                return RecorderWin.dealRecorderUIEvent(command, data);
            }
            if (command.startsWith("txz.record.win.")) {
                return WinManager.getInstance().invokeRecordWin(packageName, command.substring("txz.record.win.".length()),
                        data);
            }
            if (command.startsWith("txz.recordwin2.")) {
                return WinManager.getInstance().invokeRecordWin2(packageName, command.substring("txz.recordwin2.".length()), data);
            }
            if (command.startsWith("txz.nav.")) {
                return NavManager.getInstance().invokeTXZNav(packageName, command.substring("txz.nav.".length()), data);
            }
            if (command.startsWith("txz.multi.nav.")) {
                return NavManager.getInstance().invokeMultiNav(packageName,
                        command.substring("txz.multi.nav.".length()), data);
            }
            if (command.startsWith("txz.sence.")) {
                return SenceManager.getInstance().invokeTXZSence(packageName, command.substring("txz.sence.".length()),
                        data);
            }
            if (command.startsWith("wx.")) {
                return invokeWebChat(packageName, command, data);
            }
            if (command.startsWith("txz.wakeup.")) {
                return WakeupManager.getInstance().invokeTXZWakeup(packageName,
                        command.substring("txz.wakeup.".length()), data);
            }
            if (command.startsWith("txz.resource.")) {
                return ResourceManager.getInstance().invokeTXZResource(packageName,
                        command.substring("txz.resource.".length()), data);
            }
            if (command.startsWith("comm.text")) {
                return invokeCommText(packageName, command, data);
            }
            if (command.startsWith("txz.notification")) {
                return NotificationManager.getInstance().invokeTXZNotification(packageName, command, data);
            }
            if (command.startsWith("txz.help")) {
                return WinHelpManager.getInstance().invokeWinHelp(packageName, command, data);
            }
            if (command.startsWith("txz.loc.")) {
                return LocationManager.getInstance().processRemoteCommand(packageName,
                        command.substring("txz.loc.".length()), data);
            }
            if (command.startsWith("txz.tool.tts.")) {
                return TtsRemoteImpl.procRemoteResponse(packageName, command.substring("txz.tool.tts.".length()), data);
            }
            if (command.startsWith("txz.tool.asr.")) {
                return AsrRemoteImpl.procRemoteResponse(packageName, command.substring("txz.tool.asr.".length()), data);
            }
            if (command.startsWith("txz.tool.poi.")) {
                return PoiSearchToolRemoteImpl.procRemoteResponse(packageName, command.substring("txz.tool.poi.".length()), data);
            }
            if (command.startsWith(TXZCarControlHomeManager.PREFIX_SEND)) {
                return CarControlHomeManager.procRemoteResponse(packageName, command, data);
            }
            if (command.startsWith("txz.sys.")) {
                return SysTool.invokeTXZSysTool(packageName, command.substring("txz.sys.".length()), data);
            }
            if (command.startsWith("txz.fm.")) {
                return FmManager.getInstance().invokeTXZFM(packageName, command.substring("txz.fm.".length()), data);
            }
            if (command.startsWith("txz.am.")) {
                return FmManager.getInstance().invokeTXZAM(packageName,
                        command.substring("txz.am.".length()), data);
            }
            if (command.startsWith("txz.selector.")) {
//				if (command.startsWith("txz.selector.poi.")) {
//					return Selector.getSelector() != null ? Selector
//							.getSelector().procInvoke(
//									packageName,
//									command.substring("txz.selector.poi."
//											.length()), data) : null;
//				} else if (command.startsWith("txz.selector.audio.")) {
//					return Selector.getSelector() != null ? Selector
//							.getSelector().procInvoke(
//									packageName,
//									command.substring("txz.selector.audio."
//											.length()), data) : null;
//				} else {
//					return ListSelector.procStaticInvoke(packageName, command,
//							data);
//				}
                return ChoiceManager.getInstance().invokeCommand(packageName, command, data);
            }
            if (command.startsWith("txz.package.")) {
                return PackageManager.getInstance().processInvoke(packageName,
                        command.substring("txz.package.".length()), data);
            }
            if (command.startsWith("comm.netdata.req.")) {
                return NetDataManager.getInstance().processInvoke(packageName,
                        command.substring("comm.netdata.req.".length()), data);
            }
            if (command.startsWith("txz.poi.")) {
                return NavManager.getInstance().processInvoke(packageName, command, data);
            }
            if (command.startsWith("comm.wheelcontrol.")) {
                return WheelControlManager.getInstance().invokeCommWheelControl(packageName, command, data);
            }
            if (command.startsWith("txz.ac.")) {
                return ACManager.getInstance().invokeTXZAC(packageName, command.substring("txz.ac.".length()), data);
            }
            if (command.startsWith("comm.dns.")) {
                return DnsManager.getInstance().invokeTXZDns(packageName, command.substring("comm.dns.".length()), data);
            }
            if (command.startsWith("txz.upgrade.invoke.")) {
                return VisualUpgradeManager.getInstance().processInvoke(packageName, command.substring("txz.upgrade.invoke.".length()), data);
            }
			if (command.startsWith(TXZWeatherManager.WEATHER_INVOKE_PREFIX)) {
                return WeatherManager.getInstance().invokeWeather(packageName,command,data);
            }
            if (command.startsWith(TXZStockManager.STOCK_INVOKE_PREFIX)) {
                return StockManager.getInstance().invoke(packageName,command,data);
            }
			if(command.startsWith(TXZTicketManager.TRAIN_INVOKE_PREFIX)){
                return TicketManager.getInstance().invokeTrain(packageName,command,data);
            }
            if(command.startsWith(TXZTicketManager.FLIGHT_INVOKE_PREFIX)){
                return TicketManager.getInstance().invokeFlight(packageName,command,data);
            }
            if (command.startsWith(TXZDownloadManager.DOWNLOAD_INVOKE_PREFIX)) {
                return DownloadManager.getInstance().invokeCommDownload(packageName,command,data);
            }
            if (command.startsWith(TXZInnerUpgradeManager.UPGRADE_INVOKE_PREFIX)) {
                return UpgradeManager.getInstance().processInvoke(packageName,command.substring(TXZInnerUpgradeManager.UPGRADE_INVOKE_PREFIX.length()),data);
            }
            if (command.startsWith("txz.news")) {
                return NewsManager.getInstance().processInvoke(packageName, command.substring("txz.news.".length()), data);
            }
            if (command.startsWith("txz.film.")){
                return FilmManager.getInstance().invokeCommand(packageName, command, data);
            }
            if(command.startsWith("txz.tool.movie.")){
                return MovieWorkChoice.procRemoteResponse(packageName, command.substring("txz.tool.movie.".length()), data);
            }
            if(command.startsWith("txz.themeStyle.movie.")){
                return FilmManager.procRemoteResponse(packageName, command.substring("txz.themeStyle.movie.".length()), data);
            }
            if(command.startsWith("txz.control.film.")){
                return FilmManager.procRemoteResponse(packageName, command.substring("txz.control.film.".length()), data);
            }
            if(command.startsWith("txz.ticket.")){
                return QiWuTicketManager.getInstance().invokeCommand(packageName, command.substring("txz.ticket.".length()), data);
            }
            return ret;
        }

        private boolean blockKeyword(String command) {
            if ("comm.PackageInfo".equals(command)) {
                return false;
            }

            return true;
        }

        private byte[] invokeWakeup(String packageName, String command,
                                    byte[] data) {
            WakeupManager.getInstance().invokeWakeup(packageName, command, data);
            return null;
        }
    }

    private byte[] invokeCommText(final String packageName, String command, byte[] data) {
        if (command.equals("comm.text.parse")) {
            if (data == null) {
                return null;
            }
            try {
                JSONObject json = new JSONObject(new String(data));
                String text = json.getString("text");
                if (text.contains("天气")) {
                    JSONObject jsonReq = new JSONObject();
                    jsonReq.put("city", "cur");
                    NetDataManager.getInstance()
                            .procWeatherBackground_1_0(packageName, command,
                                    jsonReq.toString().getBytes());
                }
            } catch (Exception e) {

            }
        } else if (command.equals("comm.text.test")) {
            return TextResultHandle.getInstance().invokeCommTextTest(
                    packageName, command, data);
        }
        return null;
    }

    private byte[] invokeCommStatus(final String packageName, String command, byte[] data) {
        if (command.equals("comm.status.get")) {
            JSONObject json = new JSONObject();
            try {
                json = new JSONObject(new String(data));
                if (json.has("asr"))
                    json.put("asr", AsrManager.getInstance().isBusy() || RecordManager.getInstance().isBusy());
                if (json.has("tts"))
                    json.put("tts", TtsManager.getInstance().isBusy());
                if (json.has("call"))
                    json.put("call", CallManager.getInstance().isIdle() == false);
                return json.toString().getBytes();
            } catch (Exception e) {
                return json.toString().getBytes();
            }
        }
        return null;
    }

    private long mLastShockWakeupTime = 0;

    private byte[] invokeCommPower(final String packageName, String command, byte[] data) {
        final String TAG = "POWER::";
        if (command.equals("POWER_ON")) {
            LogUtil.logd(TAG + "POWER_ON");
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_POWER_ON);
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_POWER_ON);
            return null;
        }
        if (command.equals("BEFORE_SLEEP")) {
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_SLEEP);
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_BEFORE_SLEEP);
            WakeupManager.getInstance().stopComplete();
            AppLogic.runOnBackGround(new Runnable() {

                @Override
                public void run() {
//                    LocationManager.getInstance().quickLocation(false);
                    LogUtil.logd(TAG + "BEFORE_SLEEP");
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.sleep", null, null);
                }
            }, 0);
            return null;
        }
        if (command.equals("SLEEP")) {
        	HelpGuideManager.getInstance().finishGuideAnim();
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_SLEEP);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_SLEEP);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
            LogUtil.logd(TAG + "SLEEP");
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.sleep", null, null);
            return null;
        }
        if (command.equals("WAKEUP")) {
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_WAKEUP);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_WAKEUP);
            WakeupManager.getInstance().start();
            AppLogic.runOnBackGround(new Runnable() {

                @Override
                public void run() {
                    LocationManager.getInstance().quickLocation(true);
                    LogUtil.logd(TAG + "WAKEUP");
                    ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.wakeup", null, null);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.wakeup", null, null);
                }
            }, 0);

            return null;
        }
        if (command.equals("BEFORE_POWER_OFF")) {
        	HelpGuideManager.getInstance().finishGuideAnim();
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_BEFORE_POWER_OFF);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_POWER_OFF);
            LogUtil.logd(TAG + "BEFORE_POWER_OFF");
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.exit", null, null);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
            return null;
        }
        if (command.equals("POWER_OFF")) {
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_POWER_OFF);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_POWER_OFF);
            LogUtil.logd(TAG + "POWER_OFF");
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.exit", null, null);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
            return null;
        }
        if (command.equals("SHOCK_WAKEUP")) {
            LogUtil.logd(TAG + "SHOCK_WAKEUP");
            if (SystemClock.elapsedRealtime() - mLastShockWakeupTime > 10 * 1000) {
                mLastShockWakeupTime = SystemClock.elapsedRealtime();
                ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_SHOCK_WAKEUP);
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_SHOCK_WAKEUP);
            }
            return null;
        }

        if (command.equals("ENTER_REVERSE")) {
            LogUtil.logd(TAG + "ENTER_REVERSE");
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_ENTER_REVERSE);
            // TODO: 2017/11/1 倒车状态处理
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.enter_reverse", null, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.enter_reverse", null, null);
            HelpGuideManager.getInstance().enter_reverse();
	    	TXZPowerControl.setEnterReverse(true);
            return null;
        }
        if (command.equals("QUIT_REVERSE")) {
            LogUtil.logd(TAG + "QUIT_REVERSE");
            ConfigManager.getInstance().reportDeviceStatus(UiEquipment.DEVICE_STATUS_QUIT_REVERSE);
            // TODO: 2017/11/1 退出倒车状态处理
            ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.quit_reverse", null, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.client.quit_reverse", null, null);
            HelpGuideManager.getInstance().resumeGuideAnim();
	    	TXZPowerControl.setEnterReverse(false);
            return null;
        }
        return null;
    }

    private byte[] invokeCommLocation(final String packageName, String command, byte[] data) {
        if (command.equals("comm.location.gethome")) {
            UserConfig userConfig = NativeData.getCurUserConfig();
            if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgHomeLoc != null) {
                NavigateInfo home = userConfig.msgNetCfgInfo.msgHomeLoc;
                return NavigateInfo.toByteArray(home);
            }
        } else if (command.equals("comm.location.getcompany")) {
            UserConfig userConfig = NativeData.getCurUserConfig();
            if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgCompanyLoc != null) {
                NavigateInfo company = userConfig.msgNetCfgInfo.msgCompanyLoc;
                return NavigateInfo.toByteArray(company);
            }
        } else if (command.equals("comm.location.sethome")) {
            try {
                NavigateInfo home = NavigateInfo.parseFrom(data);
                NavManager.getInstance().setHomeLocation(home.strTargetName, home.strTargetAddress,
                        home.msgGpsInfo.dblLat, home.msgGpsInfo.dblLng, home.msgGpsInfo.uint32GpsType);
            } catch (Exception e) {
                LogUtil.loge("set home fail!");
            }
        } else if (command.equals("comm.location.setcompany")) {
            try {
                NavigateInfo company = NavigateInfo.parseFrom(data);
                NavManager.getInstance().setCompanyLocation(company.strTargetName, company.strTargetAddress,
                        company.msgGpsInfo.dblLat, company.msgGpsInfo.dblLng, company.msgGpsInfo.uint32GpsType);
            } catch (Exception e) {
                LogUtil.loge("set company fail!");
            }
        } else if (command.equals("comm.location.gethistorylist")) {
            UserConfig userConfig = NativeData.getCurUserConfig();
            if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgHistoryLocs != null
                    && userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem != null) {
                NavigateInfoList history = userConfig.msgNetCfgInfo.msgHistoryLocs;
                return NavigateInfoList.toByteArray(history);
            }
        } else if (command.equals("comm.location.sethistory")) {
            try {
                NavigateInfo history = NavigateInfo.parseFrom(data);
                NavManager.getInstance().setHistroyLocation(history, false);
            } catch (Exception e) {
                LogUtil.loge("set history fail!");
            }
        }
        return null;
    }

    private byte[] invokeCommTts(final String packageName, String command, byte[] data) {
        try {
            if (command.equals("comm.tts.speak")) {
                JSONBuilder jsonDoc = new JSONBuilder(new String(data));
                int iStream = jsonDoc.getVal("iStream", Integer.class);
                String sText = jsonDoc.getVal("sText", String.class);
                String[] voiceUrls = jsonDoc.getVal("voiceUrls", String[].class);
                String resId = jsonDoc.getVal("resId", String.class);
                String[] resArgs = jsonDoc.getVal("resArgs", String[].class);
                long delay = jsonDoc.getVal("delay", Long.class, 0l);
                JSONArray jsonArray = jsonDoc.getVal("voiceTask", JSONArray.class);
                if (resId != null) {
                    //String res = NativeData.getResString(resId);
                    String res = NativeData.getResPlaceholderString(resId, resArgs);
                    if (res != null && res.length() != 0) {
                        sText = res;
                        JNIHelper.logd("use res " + res + " instead of " + sText);
                    }
                }
                VoiceTask[] voiceTasks = VoiceTask.parseJsonArray(jsonArray);
                PreemptType bPreempt = PreemptType.valueOf(jsonDoc.getVal("bPreempt", String.class));
                int remoteTtsId = TtsManager.getInstance().speakVoiceTask(iStream, sText, voiceUrls, delay, bPreempt, true, voiceTasks,
                        new ITtsCallback() {
                            @Override
                            public void onBegin() {
                                String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                                ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.begin",
                                        data.getBytes(), null);
                            }

                            @Override
                            public void onCancel() {
                                String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                                ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.cancel",
                                        data.getBytes(), null);
                            }

                            @Override
                            public void onSuccess() {
                                String data = new JSONBuilder().put("ttsId", mTaskId).toString();
                                ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.success",
                                        data.getBytes(), null);
                            }

                            @Override
                            public void onError(int iError) {
                                String data = new JSONBuilder().put("ttsId", mTaskId).put("error", iError).toString();
                                ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.error",
                                        data.getBytes(), null);
                            }
                        });
                return (remoteTtsId + "").getBytes();
            }
            if (command.equals("comm.tts.cancel")) {
                TtsManager.getInstance().cancelSpeak(Integer.parseInt(new String(data)));
                return null;
            }
            if (command.equals("comm.tts.speakTextOnRecordWin")) {
                JSONBuilder json = new JSONBuilder(data);
                AsrManager.getInstance().setNeedCloseRecord(json.getVal("close", Boolean.class, true));
                String[] resArgs = json.getVal("resArgs", String[].class);
                String resId = json.getVal("resId", String.class);
                String sText = json.getVal("text", String.class);
                //是否取消的时候据需执行Runnable
                boolean isCancleExecute = json.getVal("isCancleExecute", Boolean.class, true);


                if (resId != null) {
                    //String res = NativeData.getResString(resId);
                    String res = NativeData.getResPlaceholderString(resId, resArgs);
                    if (res != null && res.length() != 0) {
                        sText = res;
                        JNIHelper.logd("use res " + res + " instead of " + sText);
                    }
                }


                RecorderWin.speakTextWithClose(sText, json.getVal("needAsr", Boolean.class, true), isCancleExecute, new Runnable() {
                    @Override
                    public void run() {
                        ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.speakTextOnRecordWin.end",
                                null, null);
                    }
                });
                return null;
            }
            if (command.equals("comm.tts.set.voicespeed")) {
                if (data == null) {
                    return null;
                }
                int speed = Integer.parseInt(new String(data));
                TtsManager.getInstance().setVoiceSpeed(speed);
                return null;
            }
            if (command.equals("comm.tts.set.buffettime")) {
                if (data == null) {
                    return null;
                }
                try {
                    int nTime = Integer.parseInt(new String(data));
                    TtsManager.getInstance().setBufferTime(nTime);
                } catch (Exception e) {

                }

                return null;
            }
            if (command.equals("comm.tts.set.modelrole")) {
                if (data == null) {
                    return null;
                }
                try {
                    String strModel = new String(data);
                    TtsManager.getInstance().setTtsModel(strModel);
                } catch (Exception e) {

                }
                return null;
            }
            if (command.equals("comm.tts.set.enableRemoteTtsTool")) {
                if (data == null) {
                    return null;
                }
                try {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    Boolean enableRemoteTtsTool = jsonBuilder.getVal("enableRemoteTtsTool", Boolean.class, true);
                    if (!enableRemoteTtsTool) {
                        if (!TXZService.isTXZSelfService(packageName)) {
                            TtsRemoteImpl.setRemoteTtsService(null);
                        }
                    }
                    DebugCfg.DISABLE_REMOTE_TTS_TOOL = enableRemoteTtsTool;
                } catch (Exception e) {

                }
                return null;
            }
            if (command.equals("comm.tts.set.ttsdelay")) {
                if (data == null) {
                    return null;
                }
                String strDelay = new String(data);
                long delay = Long.parseLong(strDelay);
                TtsManager.getInstance().setTtsDelay(delay);
                return null;
            }
            if (command.equals("comm.tts.set.beep")) {
                String beepPath = null;
                if (data != null) {
                    beepPath = new String(data);
                }
                TtsManager.getInstance().setBeepResources(beepPath);
                return null;
            }
            if ("comm.tts.getFeatures".equals(command)) {
                JSONBuilder json = new JSONBuilder();
                json.put("isSupportOnBegin", true); //新版本支持开始的回调
                return json.toBytes();
            }
            if ("comm.tts.set.replaceword".equals(command)) {
                if (data == null) {
                    return null;
                }
                TtsManager.getInstance().setReplaceSpeakWord(new String(data));
                return null;
            }
            if ("comm.tts.set.enableDownVolume".equals(command)) {
                if (data == null) {
                    return null;
                }
                boolean enable = Boolean.parseBoolean(new String(data));
                TtsManager.getInstance().enableDownVolumeWhenNav(enable);
                return null;
            }
            if ("comm.tts.set.forceShowChoiceView".equals(command)) {
                if (data == null) {
                    return null;
                }
                boolean enable = Boolean.parseBoolean(new String(data));
                TtsManager.getInstance().forceShowChoiceView(enable);
                return null;
            }
            if ("comm.tts.getTtsThemes".equals(command)) {
                byte[] bytes = null;
                JSONArray jsonArray = TtsTheme.toJsonArray(TtsManager.getInstance().getTtsThemes());
                if (jsonArray != null) {
                    bytes = jsonArray.toString().getBytes();
                }
                return bytes;
            }
            if ("comm.tts.set.ttstheme".equals(command)) {
                if (data == null) {
                    return null;
                }
                JSONObject jsonObject = new JSONObject(new String(data));
                int themeid = jsonObject.optInt("themeid");
                String themename = jsonObject.optString("themename");
                TtsManager.getInstance().setTtsTheme(themeid, themename);
                return null;
            }
            if ("comm.tts.set.themeChangeListener".equals(command)) {
                TtsManager.getInstance().setPackageName(packageName);
                return null;
            }
            if("comm.tts.clear.themeChangeListener".equals(command)){
                TtsManager.getInstance().setPackageName(null);
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] invokeCommLog(String packageName, String command, byte[] data) {
        JSONBuilder jsonDoc = new JSONBuilder(new String(data));
        int level = jsonDoc.getVal("level", Integer.class);
        String tag = jsonDoc.getVal("tag", String.class);
        String content = jsonDoc.getVal("content", String.class);
        Integer pid = jsonDoc.getVal("pid", Integer.class, 0);
        Long tid = jsonDoc.getVal("tid", Long.class, 0L);
        JNIHelper._logRaw(pid, tid, packageName, level, tag, content);
        return null;
    }

    private byte[] invokeWebChat(final String packageName, String command, byte[] data) {
        if (command.equals("wx.subscribe.qrcode")) {
            // 通知服务器订阅模块事件
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL);
            return null;
        }

        if (command.equals("wx.upload.voice")) {
            // 上传语音
            try {
                JSONObject json = new JSONObject(new String(data));
                UiEquipment.Req_UploadVoice req = new UiEquipment.Req_UploadVoice();
                req.strPath = json.getString("path");
                req.uint32VoiceTimeLength = json.getInt("length");
                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_UPLOAD_VOICE, req);
            } catch (Exception e) {
            }
            return null;
        }
        if (command.equals("wx.contact.update")) {
            // 更新微信联系人
            WeixinManager.getInstance().updateWeChatContact(command, data);
            return null;
        }

        if (command.equals("wx.loginstate.update")) {
            // 更新微信登陆状态
            WeixinManager.getInstance().updateLoginState(command, data);
            WeixinManager.getInstance().doReportWxchatLoginState(data);
            return null;
        }

        if (command.equals("wx.contact.recentsession.update")) {
            WeixinManager.getInstance().updateWeChatRecentChat(command, data);
            return null;
        }

        if (command.equals("wx.contact.maskedsession.update")) {
            WeixinManager.getInstance().updateWeChatMaskedChat(command, data);
            return null;
        }

        if (command.equals("wx.cmd.proc.send")) {
            WeixinManager.getInstance().procSendMsgSence();
            return null;
        }
        return null;
    }

    private byte[] invokeTeam(final String packageName, String command, byte[] data) {
        if (command.equals("team.subscribe.qrcode")) {
            // 通知服务器订阅模块事件
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_CAR_TEAM_URL);
            return null;
        }

        return null;
    }

    boolean mSetOnlyByLauncher = true; // 只被launcher设置过初始化参数
    static boolean mFirstCheckInit = true;

    public static boolean checkSdkInitResult() {
//		if (!TextUtils.isEmpty(ImplCfg.getAsrImplClass())) {
//			if (!AsrManager.getInstance().isInited())
//				return false;
//			if (!AsrManager.getInstance().isInitSuccessed()) {
//				ServiceManager.getInstance().broadInvoke("sdk.init.error.asr", null);
//				return true;
//			}
//		}
        if (!TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
            if (!TtsManager.getInstance().isInited())
                return false;
            if (!TtsManager.getInstance().isInitSuccessed()) {
                ServiceManager.getInstance().broadInvoke("sdk.init.error.tts", null);
                return true;
            }
        }
        if (!WakeupManager.getInstance().isInited())
            return false;
        if (!WakeupManager.getInstance().isInitSuccessed()) {
            ServiceManager.getInstance().broadInvoke("sdk.init.error.wakeup", null);
            return true;
        }
        if (!WinManager.getInstance().isInited() || !WinManager.getInstance().isInitSuccessed())
            return false;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    ServiceManager.getInstance().broadInvoke("sdk.init.success", null,
                            ServiceManager.DEFAULT_TIMEOUT_LONG);
                    if (mFloatToolClickInterval != null) {
                        setFloatToolClickIntervalInner(mFloatToolClickInterval);
                    }
                    HelpGuideManager.getInstance().showFloatAfterInitSuccess();
                    if (mFirstCheckInit) {
                        mFirstCheckInit = false;
                        // TtsManager.getInstance().speakText("语音助理初始化完成");
                        JNIHelper.logd("txz app init ready");
                        setFloatToolTypeInner(mFloatToolType);
                        setFloatToolUrlInner(mFloatToolUrl_N, mFloatToolUrl_P);
                        LicenseManager.getInstance().initSuccessCheck();
                        ModuleManager.getInstance().initialize_AfterInitSuccess();
                    }
                }
            });
            return true;
        }
        ServiceManager.getInstance().broadInvoke("sdk.init.success", null,
                ServiceManager.DEFAULT_TIMEOUT_LONG);
        if (mFloatToolClickInterval != null) {
            setFloatToolClickIntervalInner(mFloatToolClickInterval);
        }
        HelpGuideManager.getInstance().showFloatAfterInitSuccess();
        if (mFirstCheckInit) {
            mFirstCheckInit = false;
            // TtsManager.getInstance().speakText("语音助理初始化完成");
            JNIHelper.logd("txz app init ready");
            setFloatToolTypeInner(mFloatToolType);
            setFloatToolUrlInner(mFloatToolUrl_N, mFloatToolUrl_P);
            LicenseManager.getInstance().initSuccessCheck();
            ModuleManager.getInstance().initialize_AfterInitSuccess();
        }
        return true;
    }

    private static Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            if (PackageManager.getInstance().isHome()) {
                SDKFloatView.getInstance().open();
            } else {
                SDKFloatView.getInstance().close();
            }
            AppLogic.removeUiGroundCallback(mHandlerTask);
            AppLogic.runOnUiGround(mHandlerTask, 500);
        }
    };

    private static void setFloatToolTypeInner(String floatToolType) {
        AppLogic.removeUiGroundCallback(mHandlerTask);
        if (TXZPowerControl.hasReleased()) {
            return;
        }
		if (TXZPowerControl.isEnterReverse()){
			return;
		}
        if (floatToolType.equals("FLOAT_TOP")) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                	SDKFloatView.getInstance().beforeState = SDKFloatView.TYPE_FLOAT_TOP;
                    SDKFloatView.getInstance().setFloatToolType(SDKFloatView.TYPE_FLOAT_TOP , true);
                }
            }, 0);
        } else if (floatToolType.equals("FLOAT_NORMAL")) {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                	SDKFloatView.getInstance().beforeState = SDKFloatView.TYPE_FLOAT_NORMAL;
                    SDKFloatView.getInstance().setFloatToolType(SDKFloatView.TYPE_FLOAT_NORMAL , true);
                }
            }, 0);
            AppLogic.runOnUiGround(mHandlerTask, 0); // launcher界面显示， 非launcher界面就隐藏
        } else if (floatToolType.equals("FLOAT_NONE")) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                	SDKFloatView.getInstance().beforeState = SDKFloatView.TYPE_FLOAT_NONE;
                    SDKFloatView.getInstance().setFloatToolType(SDKFloatView.TYPE_FLOAT_NONE , true);
                }
            }, 0);
        }
    }

    private static void setFloatToolUrlInner(final String floatToolUrl_N, final String floatToolUrl_P) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                SDKFloatView.getInstance().setImageBitmap(floatToolUrl_N, floatToolUrl_P);
            }
        }, 0);
    }

    private static void setFloatToolClickIntervalInner(final long interval) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                SDKFloatView.getInstance().setClickInteval(interval);
            }
        }, 0);
    }

    private static void setFloatViewEnableAutoAdjust() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                SDKFloatView.getInstance().enableAutoAdjust();
            }
        }, 0);
    }

    private static void setFloatViewDisableAutoAdjust() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                SDKFloatView.getInstance().disableAutoAdjust();
            }
        }, 0);
    }

    private static void setFloatToolPosition(final Integer ftX, final Integer ftY) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                SDKFloatView.getInstance().setFloatViewPosition(ftX, ftY);
            }
        }, 0);
    }

    protected void setFloatToolSize(final Integer floatToolWidth, final Integer floatToolHeight) {
        AppLogic.runOnUiGround(new Runnable() {

            @Override
            public void run() {
                SDKFloatView.getInstance().setFloatToolSize(floatToolWidth, floatToolHeight);
            }
        }, 0);
    }

    private static String mFloatToolType = "FLOAT_TOP";

    public static String getFloatToolType() {
        return mFloatToolType;
    }

    public static void setFloatToolType(String floatToolType) {
        mFloatToolType = floatToolType;
        if (TtsManager.getInstance().isInitSuccessed()
                && WakeupManager.getInstance().isInitSuccessed()
                && WinManager.getInstance().isInited()
                && WinManager.getInstance().isInitSuccessed()) {
            setFloatToolTypeInner(mFloatToolType);
        }
        ConfigManager.getInstance().notifyRemoteSync();
    }

    private static String mFloatToolUrl_N = null;
    private static String mFloatToolUrl_P = null;

    public static void setFloatToolUrl(String floatToolUrl_N, String floatToolUrl_P) {
        mFloatToolUrl_N = floatToolUrl_N;
        mFloatToolUrl_P = floatToolUrl_P;
        if (TtsManager.getInstance().isInitSuccessed()
                && WakeupManager.getInstance().isInitSuccessed()
                && WinManager.getInstance().isInited()
                && WinManager.getInstance().isInitSuccessed()) {
            setFloatToolUrlInner(mFloatToolUrl_N, mFloatToolUrl_P);
        }
    }

    private static Long mFloatToolClickInterval = null;

    public static void setFloatToolClickInterval(long interval) {
        mFloatToolClickInterval = interval;
        setFloatToolClickIntervalInner(interval);
    }

    private static Set<String> mSetNoVoiceService = new HashSet<String>();

    public static void notifyInited() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (!AppLogic.isInited())
                    return;
                for (String s : mSetNoVoiceService) {
                    ServiceManager.getInstance().sendInvoke(s, "sdk.init.success", null, null,
                            ServiceManager.DEFAULT_TIMEOUT_LONG);
                }
                mSetNoVoiceService.clear();
            }
        }, 0);
    }

    private byte[] invokeSDK(final String packageName, String command, final byte[] data) {
        if (command.equals("txz.sdk.init")) {
            JNIHelper.logd(packageName + " init sdk");
            if (SystemClock.elapsedRealtime() - TXZPowerControl.mLastReleaseTime < 500) {
                // 如果收到释放连接500ms
                this.stopSelf();
                return null;
            }
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    if (data == null) {
                        if (!mFirstCheckInit) {
                            ServiceManager.getInstance().sendInvoke(packageName, "sdk.init.success",
                                    null, null, ServiceManager.DEFAULT_TIMEOUT_LONG);
                        } else {
                            ServiceManager.getInstance().addServiceRecord(packageName);
                        }
                        return;
                    }
                    ServiceManager.getInstance().sendInvoke(packageName, "", null, new GetDataCallback() {
                        @Override
                        public void onGetInvokeResponse(ServiceData data) {
                            checkSdkInitResult();
                        }
                    });
                    if (!ServiceManager.LAUNCHER.equals(packageName))
                        mSetOnlyByLauncher = false;
                    if (mSetOnlyByLauncher == false && ServiceManager.LAUNCHER.equals(packageName)) {
                        // 如果被其他非launcher程序初始化过，则不再执行launcher的初始化参数了
                        return;
                    }
                    String appId = null;
                    String appToken = null;
                    String adapterLocalCommandBackupPath = null;
                    String adapterLocalCommandLoadPath = null;
                    Double voiceprintRecognitionScore = null;
                    String voiceprintRecognitionPackageName = null;
                    String appCustomId = null;
                    String uuid = null;
                    String vin = null;
                    String neverFormatRoot = null;
                    String ttsType = null;
                    String asrType = null;
                    String ftType = null;
                    String ftUrl_N = null;
                    String ftUrl_P = null;
                    Long ftInterval = null;
                    Integer ftX = null;
                    Integer ftY = null;
                    String[] wakeupKeywords = null;
                    String jsonScoreKws = null;
                    Boolean enableInstantAsr = null;
//					String[] instantWakeupKeywords = null;
                    Boolean enableServiceContact = null;
                    Boolean fixCallFunction = null;
                    String defaultNavTool = null;
                    AsrMode asrMode = null;
                    Boolean coexistAsrAndWakeup = null;
                    Float wakeupThreshHold = null;
                    Float asrWakeupThreshHold = null;
                    Boolean autoRunAsr = null;
                    Integer beepTimeOut = null;
                    Integer filterNoiseType = null;
                    AsrServiceMode asrServiceMode = null;
                    Integer ttsVoiceSpeed = null;
                    Integer maxAsrRecordTime = null;
                    Boolean zeroVolToast = null;
                    Integer txzStream = null;
                    Boolean useExternalAudioSource = null;
                    Boolean enableBlackHole = null;
                    Boolean forceStopWkWhenTts = null;
                    Boolean enableProtectWakeup = null;
                    Integer audioSourceForRecord = null;
                    Integer extAudioSourceType = null;
                    Boolean addDefaultMusicType = null;
                    Boolean useHQualityWakeupModel = null;
                    String extAudioSourcePkg = null;
                    Integer winType = null;
                    String strSdkVersionInfo = null;

                    String resApkPath = null;
                    Boolean forceUseUI1 = null;
                    JSONBuilder json = new JSONBuilder(data);
                    InterruptMode interruptMode = null;
                    Float winBgAlpha = null;
                    Boolean useLocalNetAsr = null;
                    Boolean cancelable = null;
                    Integer floatToolWidth = null;
                    Integer floatToolHeight = null;
                    String settingPackageName = null;
                    Integer winRecordImpl = null;
                    Boolean enableFullScreen = null;
                    Boolean useRadioAsAudio = null;
                    Integer netModule = null;
					Boolean aecPreventFalseWakeup = null;
					Integer dialogTimeout = null;
					Integer messageDialogType = null;
                    String strNeedSpeechStateTaskId = null;
                    Integer memMode = null;
					Boolean useTypingEffect = null;
                    Boolean canceledOnTouchOutside = null;
                    Boolean allowOutSideClickSentToBehind = null;
					String fmNamesPath = null;
					byte[] hardwareParams = null;
					Boolean enableTtsPlayer = null;
					Integer recorderBufferSize = null;
                    try {
                        strSdkVersionInfo = json.getVal("version", String.class, "UNKNOW");
                        JNIHelper.logd(
                                packageName + " init sdk version: " + strSdkVersionInfo);
                        appId = json.getVal("appId", String.class);
                        appToken = json.getVal("appToken", String.class);
                        adapterLocalCommandBackupPath = json.getVal("adapterLocalCommandBackupPath", String.class);
                        adapterLocalCommandLoadPath = json.getVal("adapterLocalCommandLoadPath", String.class);
                        voiceprintRecognitionScore = json.getVal("voiceprintRecognitionScore", Double.class);
                        voiceprintRecognitionPackageName = json.getVal("voiceprintRecognitionPackageName", String.class);
                        appCustomId = json.getVal("appCustomId", String.class);
                        uuid = json.getVal("uuid", String.class);
                        vin = json.getVal("vin",String.class);
                        neverFormatRoot = json.getVal("neverFormatRoot", String.class);
                        ttsType = json.getVal("ttsType", String.class);
                        asrType = json.getVal("asrType", String.class);
                        ftType = json.getVal("ftType", String.class);
                        ftUrl_N = json.getVal("ftUrl_N", String.class);
                        ftUrl_P = json.getVal("ftUrl_P", String.class);
                        ftInterval = json.getVal("ftInterval", Long.class);
                        ftX = json.getVal("ftX", Integer.class);
                        ftY = json.getVal("ftY", Integer.class);
                        String strAsrMode = json.getVal("asrMode", String.class);
                        if (strAsrMode != null) {
                            asrMode = AsrMode.valueOf(strAsrMode);
                        }
                        wakeupKeywords = json.getVal("wakeupKeywords", String[].class);
                        jsonScoreKws = json.getVal("jsonScoreKws", String.class);
                        enableInstantAsr = json.getVal("enableInstantAsr", Boolean.class);
//						instantWakeupKeywords = json.getVal("instantWakeupKeywords", String[].class);
                        enableServiceContact = json.getVal("enableServiceContact", Boolean.class);
                        fixCallFunction = json.getVal("fixCallFunction", Boolean.class);
                        defaultNavTool = json.getVal("defaultNavTool", String.class);
                        coexistAsrAndWakeup = json.getVal("coexistAsrAndWakeup", Boolean.class);
                        wakeupThreshHold = json.getVal("wakeupThreshHold", Float.class);
                        asrWakeupThreshHold = json.getVal("asrWakeupThreshHold", Float.class);
                        autoRunAsr = json.getVal("autoRunAsr", Boolean.class);
                        beepTimeOut = json.getVal("beepTimeOut", Integer.class);
                        ttsVoiceSpeed = json.getVal("ttsVoiceSpeed", Integer.class);
                        String strAsrSvrMode = json.getVal("asrServiceMode", String.class);
                        if (strAsrSvrMode != null) {
                            try {
                                asrServiceMode = AsrServiceMode.valueOf(strAsrSvrMode);
                            } catch (Exception e) {

                            }
                        }
                        filterNoiseType = json.getVal("filterNoiseType", Integer.class);
                        maxAsrRecordTime = json.getVal("maxAsrRecordTime", Integer.class);
                        zeroVolToast = json.getVal("zeroVolToast", Boolean.class);
                        txzStream = json.getVal("txzStream", Integer.class);
                        useExternalAudioSource = json.getVal("useExternalAudioSource", Boolean.class);
                        enableBlackHole = json.getVal("enableBlackHole", Boolean.class);
                        audioSourceForRecord = json.getVal("audioSourceForRecord", Integer.class);
                        forceStopWkWhenTts = json.getVal("forceStopWkWhenTts", Boolean.class);
                        enableProtectWakeup = json.getVal("enableProtectWakeup", Boolean.class);
                        extAudioSourceType = json.getVal("extAudioSourceType", Integer.class);
                        addDefaultMusicType = json.getVal("addDefaultMusicType", Boolean.class);
                        useHQualityWakeupModel = json.getVal("useHQualityWakeupModel", Boolean.class);
                        extAudioSourcePkg = json.getVal("extAudioSourcePkg", String.class);
                        winType = json.getVal("winType", Integer.class);
                        resApkPath = json.getVal("resApkPath", String.class);
                        forceUseUI1 = json.getVal("forceUseUI1", Boolean.class);
                        String strInterruptMode = json.getVal("interruptTTSType", String.class);
                        winBgAlpha = json.getVal("winBgAlpha", Float.class);
                        useLocalNetAsr = json.getVal("useLocalNetAsr", Boolean.class);
                        cancelable = json.getVal("cancelable", Boolean.class);
                        floatToolWidth = json.getVal("floatToolWidth", Integer.class);
                        floatToolHeight = json.getVal("floatToolHeight", Integer.class);

                        winRecordImpl = json.getVal("winRecordImpl", Integer.class);
                        enableFullScreen = json.getVal("enableFullScreen", Boolean.class);
                        useRadioAsAudio = json.getVal("useRadioAsAudio", Boolean.class);
                        netModule = json.getVal("netModule", Integer.class);
						aecPreventFalseWakeup = json.getVal("aecPreventFalseWakeup", Boolean.class);
						dialogTimeout = json.getVal("dialogTimeout", Integer.class);
						messageDialogType = json.getVal("messageDialogType",Integer.class);
						strNeedSpeechStateTaskId = json.getVal("needSpeechStateTaskId", String.class);

						useTypingEffect = json.getVal("useTypingEffect", Boolean.class);

                        canceledOnTouchOutside = json.getVal("canceledOnTouchOutside", Boolean.class);
                        allowOutSideClickSentToBehind = json.getVal("allowOutSideClickSentToBehind", Boolean.class);
						fmNamesPath = json.getVal("fmNamesPath", String.class);
						String strBase64 = json.getVal("hardwareParams", String.class);
                        enableTtsPlayer = json.getVal("enableTtsPlayer", Boolean.class);
                        recorderBufferSize = json.getVal("recorderBufferSize", Integer.class);
						if (strBase64 != null) {
							try {
								hardwareParams = Base64.decode(strBase64, Base64.DEFAULT);
							} catch (Exception e) {

							}
						}

						if(fmNamesPath != null){
						    ProjectCfg.setFMNamesPathAdapter(fmNamesPath);
                        }
						
                        if (strInterruptMode != null) {
                            try {
                                interruptMode = InterruptMode.valueOf(strInterruptMode);
                            } catch (Exception e) {
                            }
                        }

                        settingPackageName = json.getVal("settingPackageName", String.class);

                        memMode = json.getVal("memMode", Integer.class);
                        
                    } catch (Exception e) {
                    }

                    if (appId != null && appToken != null) {
                        //先把子进程拉起来
                        if (useLocalNetAsr != null) {
                            ProjectCfg.useLocalNetAsr = useLocalNetAsr;
                        }
                        if (!ProjectCfg.useLocalNetAsr) {
                            AsrProxy.getProxy();
                        }

                        //设置apk的包名，可能会影响到设置配置的存储，放到前面
                        if (settingPackageName != null) {
                            ProjectCfg.setSDKSettingPackage(settingPackageName);
                        }

                        if (enableTtsPlayer != null) {
                            TtsPlayerManager.setEnableTtsPlayer(enableTtsPlayer);
                        }

                        //强制使用1.0的加在启动之前，sdk强制使用1.0的情况，避免在存在皮肤包的情况下，license ok就直接先加载皮肤包
                        if (forceUseUI1 != null) {
                            WinManager.getInstance().setUseUI1(forceUseUI1);
                        }
                        CommandManager.setAdapterLocalCommandBackupPath(adapterLocalCommandBackupPath);
                        CommandManager.setAdapterLocalCommandLoadPath(adapterLocalCommandLoadPath);
                        VoiceprintRecognitionManager.getInstance().setVoiceprintRecognitionPackageName(voiceprintRecognitionPackageName);
                        VPRCSDKWrapper.getInstance().setVoiceprintRecognition(voiceprintRecognitionScore);
                        WakeupProxy.getProxy();
                        AppLogic.initWhenStart();
                    } else if (AppLogic.isInited() == false) {
                        JNIHelper.logd(packageName
                                + " init sdk waiting for appId");
                        return;
                    }

                    //保存SDK的版本信息
                    ProjectCfg.sStrSdkVersionInfo = strSdkVersionInfo;

                    if (uuid != null) {
                        DeviceInfo.setUUID(uuid);
                    }
                    if (vin != null){
                        ProjectCfg.setVin(vin);
                    }
                    if (neverFormatRoot != null) {
                        DeviceInfo.setNeverFormatRoot(neverFormatRoot);
                    }
                    
                    if (hardwareParams != null){
                    	DeviceInfo.setHardwareParams(hardwareParams);
                    }
                    
                    // 设置tts实现类
                    if (ttsType != null) {
                        if (ttsType.equals("NONE")) {
                            ImplCfg.setTtsImplClass("");
                        } else if (ttsType.equals("TTS_SYSTEM")) {
                            ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.sys.TtsSysImpl");
                        } else if (ttsType.equals("TTS_IFLY")) {
                            ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.ifly.TtsIflyImpl");
                        } else {
                            // ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.yunzhisheng.TtsYunzhishengImpl");
                            ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.yunzhisheng_3_0.TtsYunzhishengImpl");
                        }
                    }

                    // 设置asr实现类
//					if (asrType != null) {
//						if (asrType.equals("NONE")) {
//							ImplCfg.setAsrImplClass("");
//						} else if (asrType.equals("ASR_IFLY")) {
//							ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.ifly.AsrIflyImpl");
//						} else {
//							// ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.yunzhisheng.AsrYunzhishengImpl");
//							//ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.yunzhisheng_3_0.AsrYunzhishengImpl");
//							ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.mix.AsrMixImpl");
//						}
//					}
                    ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.mix.AsrMixImpl");

                    if (ProjectCfg.useLocalNetAsr) {
                        ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.mix.AsrContainer");
                    }
                    // 设置悬浮工具类型
                    if (ftType != null) {
                        JNIHelper.logd("init setFloatToolType: " + ftType);
                        setFloatToolType(ftType);
                    }

                    // 设置浮动工具图片
                    JNIHelper.logd("init setFloatToolUrl: " + ftUrl_N + ", " + ftUrl_P);
                    setFloatToolUrl(ftUrl_N, ftUrl_P);

                    // 设置悬浮工具点击间隔
                    if (ftInterval != null) {
                        JNIHelper.logd("init setFloatToolClickInterval: " + ftInterval);
                        setFloatToolClickIntervalInner(ftInterval);
                    }

                    //设置悬浮工具位置
                    if (ftX != null && ftY != null) {
                        JNIHelper.logd("init setFloatToolPosition ftX:" + ftX + ",ftY:" + ftY);
                        setFloatToolPosition(ftX, ftY);
                    }

                    //设置唤醒大模型
                    if (useHQualityWakeupModel != null) {
                        ProjectCfg.mUseHQualityWakeupModel = useHQualityWakeupModel;
                    }

                    // 设置唤醒词
                    if (wakeupKeywords != null) {
                        WakeupManager.getInstance().updateWakupKeywords_Sdk(wakeupKeywords);
                    }

                    // 针对唤醒词设置不同的阈值
                    if (jsonScoreKws != null) {
                        WakeupManager.getInstance().setWakeupKeywordsThreshold(jsonScoreKws);
                    }

                    // 设置免唤醒开启状态
                    if (enableInstantAsr != null) {
                        WakeupManager.getInstance().setInstantAsrEnabled(enableInstantAsr);
                    }

                    // 设置免唤醒命令词
//					if (instantWakeupKeywords != null) {
//						WakeupManager.getInstance().updateInstantWakeupKeywords(instantWakeupKeywords);
//					}

                    // 设置唤醒阀值
                    if (wakeupThreshHold != null) {
                        WakeupManager.getInstance().setWakeupThreshhold(wakeupThreshHold);
                    }

                    // 设置识别唤醒阀值
                    if (asrWakeupThreshHold != null) {
                        WakeupManager.getInstance().setAsrWakeupThreshhold(asrWakeupThreshHold);
                    }

                    // 设置tts播报速度
                    if (ttsVoiceSpeed != null) {
                        TtsManager.getInstance().setVoiceSpeed(ttsVoiceSpeed);
                    }

                    if (autoRunAsr != null) {
                        AsrManager.getInstance().enableAutoRun(autoRunAsr);
                    }

                    if (beepTimeOut != null) {
                        AsrManager.getInstance().setBeepTimeout(beepTimeOut);
                    }

                    // TODO 设置是否允许服务号联系人
                    if (enableServiceContact != null) {
                        ContactManager.getInstance().mEnableServiceContact = enableServiceContact;
                    }

                    // 设置是否固定调用接口
                    if (fixCallFunction != null) {
                        ProjectCfg.setFixCallFunction(fixCallFunction);
                    }

                    if (defaultNavTool != null) {
                        ProjectCfg.setDefaultNavTool(defaultNavTool);
                    }

                    // 设置识别模式
                    if (asrMode != null) {
                        AsrManager.getInstance().setAsrMode(asrMode);
                    }

                    // 设置引擎识别模式
                    if (asrServiceMode != null) {
                        AsrManager.getInstance().setAsrServiceMode(asrServiceMode);
                    }

                    if (maxAsrRecordTime != null) {
                        AsrManager.getInstance().setKeySpeechTimeout(maxAsrRecordTime);
                    }

                    if (zeroVolToast != null) {
                        VolumeManager.getInstance().enableZeroVolToast(zeroVolToast);
                    }
                    if (txzStream != null) {
                        TtsUtil.DEFAULT_TTS_STREAM = txzStream;
                    }
                    // 设置声控和唤醒是否可以共存
                    if (coexistAsrAndWakeup != null) {
                        ProjectCfg.mCoexistAsrAndWakeup = coexistAsrAndWakeup;
                    }
//					if (ProjectCfg.mCoexistAsrAndWakeup) {
//						ImplCfg.setWakeupImplClass(
//								"com.txznet.txz.component.wakeup.yunzhishengremote.WakeupYunzhishengRemoteImpl");
//					} else {
                    if (PackageManager.getInstance().checkAppExist(ServiceManager.WAKEUP)) {
                        ImplCfg.setWakeupImplClass("com.txznet.txz.component.wakeup.txz.WakeupMixImpl");
                    } else {
                        ImplCfg.setWakeupImplClass("com.txznet.txz.component.wakeup.mix.WakeupImpl");
                    }
//					}

                    if (filterNoiseType != null) {
                        ProjectCfg.setFilterNoiseType(filterNoiseType);
                        JNIHelper.logd("init filterNoiseType = " + filterNoiseType);
                        if (filterNoiseType == 0) {//没有回音消除的设备，关闭打断
                            ProjectCfg.forceStopWkWhenTts(true);
                        } else {//有回音消除的设备，打开打断
                            ProjectCfg.forceStopWkWhenTts(false);
                        }
                    }

                    if (useExternalAudioSource != null) {
                        ProjectCfg.useExtAudioSource(useExternalAudioSource);
                    }

                    if (enableBlackHole != null) {
                        ProjectCfg.enableBlackHole(enableBlackHole);
                    }

                    if (audioSourceForRecord != null) {
                        ProjectCfg.setAudioSourceForRecord(audioSourceForRecord);
                    }

                    if (enableProtectWakeup != null) {
                        ProjectCfg.enableProtectWakeup(enableProtectWakeup);
                    }

                    if (forceStopWkWhenTts != null) {
                        ProjectCfg.forceStopWkWhenTts(forceStopWkWhenTts);
                    }

                    if (extAudioSourceType != null) {
                        ProjectCfg.setExtAudioSourceType(extAudioSourceType);
                    }

                    if (addDefaultMusicType != null) {
                        if (addDefaultMusicType == false) {
                            ProjectCfg.setAddDefaultMusicType(false);
                            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_IF_ADD_MUSIC_TYPE, "false");
                        } else {
                            ProjectCfg.setAddDefaultMusicType(true);
                            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NOTIFY_IF_ADD_MUSIC_TYPE, "true");
                        }
                    }

                    if (extAudioSourcePkg != null) {
                        ProjectCfg.ext_audiosource_pkg = extAudioSourcePkg;
                    }

                    if (winType != null) {
                    	com.txznet.comm.ui.dialog.WinDialog.mType = winType;
                        com.txznet.comm.ui.dialog2.WinDialog.mType = winType;
                        HelpGuideManager.getInstance().setWinType(winType);
						SearchEditManager.getInstance().updateDialogType(winType);
                    }

					if (dialogTimeout != null) {
						NavThirdApp.DIALOG_TIME_OUT = dialogTimeout;
						LogUtil.logd("DIALOG_TIME_OUT:" + dialogTimeout);
					}

                    if (resApkPath != null) {
                        UIResLoader.getInstance().setUserConfigResApkPath(resApkPath);
                    }
                    if (interruptMode != null) {
                        InterruptTts.getInstance().setSDKInterruptMode(interruptMode);
                    }

                    if (winBgAlpha != null) {
                        AppLogic.runOnUiGround(new Runnable1<Float>(winBgAlpha) {
                            @Override
                            public void run() {
                                if (WinManager.getInstance().isRecordWin2()) {
                                    RecordWin2.getInstance().setWinBgAlpha(mP1);
                                }
                                WinRecord.getInstance().setWinBgAlpha(mP1);
                            }
                        });
                    }

                    if (floatToolWidth != null && floatToolHeight != null) {
                        JNIHelper.logd("init setFloatToolSize width::" + floatToolWidth + " height::" + floatToolHeight);
                        setFloatToolSize(floatToolWidth, floatToolHeight);
                    }

                    if (cancelable != null) {
                        setCancelable(cancelable);
                    }

                    if (winRecordImpl != null) {
                        WinManager.getInstance().setWinRecordImpl(winRecordImpl);
                    }

                    // 设置语音界面全屏
                    if (enableFullScreen != null) {
                        WinManager.getInstance().enableFullScreen(enableFullScreen);
                    }

                    if (useRadioAsAudio != null) {
                        ProjectCfg.setUseRadioAsAudio(useRadioAsAudio);
                    }

                    if (netModule != null) {
                        ProjectCfg.setNetModule(netModule);
                    }
					
					if (aecPreventFalseWakeup != null) {
						ProjectCfg.setAECPreventFalseWakeup(aecPreventFalseWakeup);
					}

					if (messageDialogType != null) {
						//暂时还需要保留部分dialog1.0的代码
						com.txznet.comm.ui.dialog.WinMessageBox.setMessageDialogType(messageDialogType);
						com.txznet.comm.ui.dialog2.WinMessageBox.setMessageDialogType(messageDialogType);
					}
                    
                    if (!TextUtils.isEmpty(strNeedSpeechStateTaskId)) {
                        ProjectCfg.setNeedSpeechStateTaskId(strNeedSpeechStateTaskId);
                    }
                    
                    if (memMode != null){
                    	ProjectCfg.setMemMode(memMode);
                    }
					
					
					if (useTypingEffect != null) {
                        ProjectCfg.setSDKTypingEffect(useTypingEffect);
                    }
                    
                    if (canceledOnTouchOutside != null) {
                        WinManager.getInstance().setCanceledOnTouchOutside(canceledOnTouchOutside);
                    }
                    
                    if (allowOutSideClickSentToBehind != null) {
                        WinManager.getInstance().setAllowOutSideClickSentToBehind(allowOutSideClickSentToBehind);
                    }

                    if (recorderBufferSize != null) {
                        ProjectCfg.setRecorderBufferSize(recorderBufferSize);
                    }

                    // 获取license，并初始化声控引擎
                    if (appId != null && appToken != null) {
                        LicenseManager.getInstance().initSDK(packageName, appId, appToken, appCustomId);
                    }
                }
            }, 0);
            return null;
        } else if (command.equals("txz.sdk.ft.status.type")) {
            String floatToolType = new JSONBuilder(new String(data)).getVal("floatToolType", String.class);
            JNIHelper.logd("config setFloatToolType: " + floatToolType);
            setFloatToolType(floatToolType);
        } else if (command.equals("txz.sdk.ft.status.icon")) {
            String floatToolUrl_N = new JSONBuilder(new String(data)).getVal("floatToolUrl_N", String.class);
            String floatToolUrl_P = new JSONBuilder(new String(data)).getVal("floatToolUrl_P", String.class);
            JNIHelper.logd("config setFloatToolUrl: " + floatToolUrl_N + ", " + floatToolUrl_P);
            setFloatToolUrl(floatToolUrl_N, floatToolUrl_P);
        } else if (command.equals("txz.sdk.ft.status.interval")) {
            Long ftInterval = new JSONBuilder(new String(data)).getVal("ftInterval", Long.class);
            JNIHelper.logd("config setFloatToolInterval: " + ftInterval);
            setFloatToolClickInterval(ftInterval);
        }else if (command.equals("txz.sdk.ft.status.enableAutoAdjust")) {
            JNIHelper.logd("config setFloatViewEnableAutoAdjust: ");
            setFloatViewEnableAutoAdjust();
        }else if (command.equals("txz.sdk.ft.status.disableAutoAdjust")) {
            JNIHelper.logd("config setFloatViewDisableAutoAdjust: ");
            setFloatViewDisableAutoAdjust();
        }
        return null;
    }


    private void setCancelable(boolean cancelable) {
        LogUtil.logd("init setCancelable::" + cancelable);
        WinManager.getInstance().setCancelable(cancelable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (SystemClock.elapsedRealtime() - TXZPowerControl.mLastReleaseTime < 500) {
            // 释放500ms内的连接操作都禁止掉
            JNIHelper.logw("disable connect durning release time");
            return null;
        }
        return new TxzBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // setForeground();
    }

    // private static final int TXZ_NOTIFICATION_ID = 0x11000001;
    // private Notification mTXZNotification;
    // private WakeupManager.WakeupWordsChangedListener mWakeupWordsListener;
    //
    // private void setForeground() {
    // mWakeupWordsListener = new WakeupWordsChangedListener(){
    // public void onWordsChanged(String [] words){
    // if(words != null){
    // startForeground(TXZ_NOTIFICATION_ID, buildNotification(words));
    // }else{
    // stopForeground(true);
    // }
    // }
    // };
    // WakeupManager.getInstance().addWakeupWordsListener(mWakeupWordsListener);
    // }

    // private Notification buildNotification(String[] wakeUpWords){
    // String title = "语音唤醒运行中";
    // String content = null;
    // if(wakeUpWords != null && wakeUpWords.length > 0){
    // StringBuilder wakeUpWordsStr = new StringBuilder();
    // for (int i = 0; i < wakeUpWords.length; i++) {
    // if(i > 0){
    // wakeUpWordsStr.append(", ");
    // }
    // wakeUpWordsStr.append(wakeUpWords[i]);
    // }
    // content = String.format("您可以说”%s“，来唤醒语音识别", wakeUpWordsStr.toString());
    // }
    //
    // // 模拟媒体按键
    // Intent intent = new Intent("txz.intent.action.BUTTON");
    // intent.putExtra("txz.intent.action.KEY_EVENT", 0xff00);
    // final PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
    //
    // if(mTXZNotification != null){
    // mTXZNotification.setLatestEventInfo(TXZService.this, title, content, pi);
    // return mTXZNotification;
    // }
    //
    // mTXZNotification = new NotificationCompat.Builder(this)
    // .setContentTitle(title)
    // .setContentText(content)
    // .setSmallIcon(R.drawable.widget_voice_assistant_normal)
    // .setContentIntent(pi)
    // .build();
    // return mTXZNotification;
    // }

    // @Override
    // public void onDestroy() {
    // if(mWakeupWordsListener != null){
    // WakeupManager.getInstance().delWakeupWordsListener(mWakeupWordsListener);
    // }
    // super.onDestroy();
    // }

    public static boolean isTXZSelfService(String service) {
        if (service == null)
            return false;
        return service.startsWith("com.txznet.");
    }
}
