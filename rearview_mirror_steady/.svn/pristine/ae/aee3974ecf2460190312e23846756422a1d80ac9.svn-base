package com.txznet.txz.module.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.app.UiApp;
import com.txz.ui.app.UiApp.AppInfo;
import com.txz.ui.contact.ContactData;
import com.txz.ui.contact.ContactData.MobileContact;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Resp_Weather;
import com.txz.ui.event.UiEvent;
import com.txz.ui.flight.FlightData;
import com.txz.ui.fm.FmData;
import com.txz.ui.fm.FmData.FMResultData;
import com.txz.ui.makecall.UiMakecall;
import com.txz.ui.makewechatssesion.UiMakeWechatSession;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NearbySearchInfo;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaList;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txz.ui.news.SimpleNewsData;
import com.txz.ui.radio.UiRadio;
import com.txz.ui.radio.UiRadio.RADIOModel;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseCommResult;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txz.ui.voice.VoiceData.WeatherData;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechatcontact.WechatContactData;
import com.txz.ui.wechatcontact.WechatContactData.QueryContacts;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.home.HomeControlManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ac.ACManager;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.constellation.ConstellationManager;
import com.txznet.txz.module.competition.CompetitionManager;
import com.txznet.txz.module.device.BindDeviceManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.home.CarControlHomeManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.stock.StockManager;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.ticket.TicketManager;
import com.txznet.txz.module.version.VisualUpgradeManager;
import com.txznet.txz.module.weather.WeatherManager;
import com.txznet.txz.ui.win.help.HelpHitTispUtil;
import com.txznet.txz.ui.win.help.HelpPreferenceUtil;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.util.StringUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.text.TextUtils;
import com.txznet.txz.util.StringUtils;

/**
 * 语义解析
 */
public class TextSemanticAnalysis {

	static TextSemanticAnalysis mInstance = new TextSemanticAnalysis();

	boolean mLocal = false; // 是否为本地

	boolean mNeedProc = true;

	String mAnswer = null; // 最终回答文本
	static String mLastText = null;// 上次识别原文本

	int mConfidence = 0;

	static Map<String, String> mapActions = new HashMap<String, String>();

	JSONObject mJsonMusic = new JSONObject();
	JSONObject mJsonDrive = new JSONObject();
	JSONObject mJsonCall = new JSONObject();

	private TextSemanticAnalysis() {
	}

	public static TextSemanticAnalysis getInstance() {
		return mInstance;
	}

    public static volatile boolean needHandleSemanticAnalysisResult = true;
	/**
	 * @param rawVoice
	 * @return
	 */
	public boolean parse(VoiceParseData rawVoice) {
        synchronized (TextSemanticAnalysis.class) {
            if (!needHandleSemanticAnalysisResult) {
                LogUtil.d("Don't handle SemanticAnalysis");
                needHandleSemanticAnalysisResult = true;
                return true;
            }
        }
		JNIHelper.logd("TextSemanticAnalysis rawVoice.uint32Sence:"
				+ rawVoice.uint32Sence);
		mJsonCall.clear();
		mJsonDrive.clear();
		JNIHelper.logd("before parse txz scene:" + rawVoice.strVoiceData);
		mNeedProc = true;
		JSONObject json = JSONObject.parseObject(rawVoice.strVoiceData);
		boolean strFuzzy = json.getBooleanValue("fuzzy");
		JNIHelper.logd("strFuzzy:" + strFuzzy);
		if (strFuzzy) {// 判断为模糊语义
			if (rawVoice.uint32Sence != null) {
				switch (rawVoice.uint32Sence) {
				case VoiceData.GRAMMAR_SENCE_MAKE_CALL:
					json.put("scene", "call");
					json.put("action", "make");
					json.put("name", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();
					JNIHelper.logd("TextSemanticAnalysis fuzzy json:"
							+ rawVoice.strVoiceData);
					break;
                    case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_NAVIGATE:
				case VoiceData.GRAMMAR_SENCE_NAVIGATE:
					JNIHelper.logd("TextSemanticAnalysis fuzzy sence:nav,text:"
							+ rawVoice.strVoiceData);
					json.put("scene", "nav");
					json.put("action", "search");
					json.put("keywords", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();
					break;
				case VoiceData.GRAMMAR_SENCE_SET_JINGYOU:
				case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_JINGYOU:
					JNIHelper.logd("TextSemanticAnalysis fuzzy action:pass,text:"
							+ rawVoice.strVoiceData);
					json.put("scene", "nav");
					json.put("action", "pass");
					json.put("keywords", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();
					break;
				case VoiceData.GRAMMAR_SENCE_SET_HOME:
                    case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_HOME:
					JNIHelper.logd("TextSemanticAnalysis fuzzy sence:nav,text:"
							+ rawVoice.strVoiceData);
					json.put("scene", "nav");
					json.put("action", "modifyHome");
					json.put("keywords", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();
					break;
				case VoiceData.GRAMMAR_SENCE_SET_COMPANY:
                    case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL_SET_COMPANY:
					JNIHelper.logd("TextSemanticAnalysis fuzzy sence:nav,text:"
							+ rawVoice.strVoiceData);
					json.put("action", "modifyCompany");
					json.put("scene", "nav");
					json.put("keywords", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();

					break;
				case VoiceData.GRAMMAR_SENCE_MUSIC:
					JNIHelper.logd("TextSemanticAnalysis fuzzy sence:music,text:"
							+ rawVoice.strVoiceData);
					json.put("action", "play");
					json.put("scene", "audio");
					json.put("keywords", rawVoice.strText);
					rawVoice.strVoiceData = json.toJSONString();

					break;
				default:
					JNIHelper
							.logd("TextSemanticAnalysis fuzzy sence:common,text:"
									+ rawVoice.strVoiceData);
					break;
				}
			}
		}

		String strScene = json.getString("scene");
		String strRawText = rawVoice.strText;
		String strVoiceData = json.getString("data");
		String strAction = json.getString("action");
		JNIHelper.logd("begin parse txz scene:" + strScene + ",action:"
				+ strAction + ",text:" + strRawText + " ,json:" + json);

		//若在上一轮语义提醒场景场景中，而本轮不在提醒场景(不说话场景除外)，则本轮交互将需要清空后台的提醒场景标志位置为true，在向后台发送请求时携带清空字段。
		if(ReminderManager.getInstance().getIsReminding()){
			if(!"empty".equals(strScene)){
				if(!"reminder".equals(strScene)){
					ReminderManager.getInstance().setIsNeedClearReminder(true);
					ReminderManager.getInstance().setIsReminding(false);
				}
			}
		}

        //帮助提示统计指令有没有说过
        WinHelpManager.getInstance().checkSpeakHelpTips(strRawText, true);
		//需要打断TTS的场景
		if (InterruptTts.getInstance().doInterrupt(strScene, rawVoice.uint32SessionId)) {
			return true;
		}
		//清空地址
		NavManager.getInstance().resetNavigateInfo();

        if (ConstellationManager.getInstance().isInAsr() && ConstellationManager.getInstance().handleAsrResult(strRawText)) {
            return true;
        }
		//广告位判断当前场景是否需要展示广告
		AdvertisingManager.getInstance().handle(json, rawVoice.floatTextScore.intValue());
		
		if (json.containsKey("local") && json.getBoolean("local")){
			mLocal = true;
		}
		else {
			mLocal = false;
		}
		if ("_yzs_local".equals(strScene)) {
			mLocal = true;
			VoiceParseData data = rawVoice;
			data.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
			data.strVoiceData = strVoiceData;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON, data);
            MonitorUtil.monitorCumulant("text.parse.I.yzsLocal");
		}
		if ("_yzs_net".equals(strScene)) {
			mLocal = false;
			VoiceParseData data = rawVoice;
			data.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
			data.strVoiceData = strVoiceData;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_TEXT_JSON, data);
            MonitorUtil.monitorCumulant("text.parse.I.yzsNet");
		}
		if ("_raw".equals(strScene)) {
			mLocal = false;
			VoiceParseData data = rawVoice;
			data.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
			data.strVoiceData = strVoiceData;
			parseRawVoice(data);
            MonitorUtil.monitorCumulant("text.parse.I.raw");
		}
		if ("_raw_online".equals(strScene)) {
			mLocal = false;
			VoiceParseData data = rawVoice;
			data.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
			data.strVoiceData = strRawText;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_PARSE_RAWTEXT_ONLINE, data);
            MonitorUtil.monitorCumulant("text.parse.I.rawOnline");
		}
        //将最终的结果在打字效果的View中追加上去，实际的文本可能会存在纠正
		RecorderWin.showPartMsg(strRawText);

		int confidence = rawVoice.floatTextScore.intValue();
		setRawText(strRawText, confidence);
		
		if (procSenceByRemote("all", rawVoice.strVoiceData)) {
			return true;
		}

		//发送场景给UI界面
		if(ConfigUtil.isUseSceneInfo()){
			JSONBuilder info = new JSONBuilder();
			info.put("scene", strScene);
			info.put("type", 1);
			RecorderWin.sendInformation(info.toString());
		}

		boolean ret = false;
		boolean endSelectScene = true;

		if ("wakeup".equals(strScene)) {
			// 不在这里处理
		} else if ("set_user_wakeup_keywords".equals(strScene)) {
			ret = parseSetUserWakeupKeywords(rawVoice, json, strScene, strAction);
		} else if ("command".equals(strScene)) {
			ret = parseCommand(rawVoice, json, strScene, strAction);
		} else if ("app".equals(strScene)) {
			ret = parseApp(rawVoice, json, strScene, strAction);
		} else if ("call".equals(strScene)) {
			ret = parseCall(rawVoice, json, strScene, strAction);
		} else if ("nav".equals(strScene)) {
			ret = parseNav(rawVoice, json, strScene, strAction);
			endSelectScene = false;
		} else if ("poi_choice".equals(strScene)) {
			ret = parsePoiChoice(rawVoice, json, strScene, strAction);
		} else if ("music".equals(strScene)) {
			ret = parseMusic(rawVoice, json, strScene, strAction);
		} else if ("audio".equals(strScene)) {
			ret = parseAudio(rawVoice, json, strScene, strAction);
		} else if ("weather".equals(strScene)) {
			ret = parseWeather(rawVoice, json, strScene, strAction);
		} else if ("stock".equals(strScene)) {
			ret = parseStock(rawVoice, json, strScene, strAction);
		} else if ("location".equals(strScene)) {
			ret = parseLocation(rawVoice, json, strScene, strAction);
		} else if ("traffic".equals(strScene)) {
			ret = parseTraffic(rawVoice, json, strScene, strAction);
		} else if ("limit_number".equals(strScene)) {
			ret = parseLimitNumber(rawVoice, json, strScene, strAction);
		} else if ("unsupport".equals(strScene)) {
			mLastText = strRawText;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_SET_LAST_VOICE_KEYWORDS,
					strRawText);
			ret = parseNotSupportOperate(rawVoice, json);
		} else if ("empty".equals(strScene)) {
			ret = parseEmpty(rawVoice, json, strScene, strAction);
		} else if ("unknown".equals(strScene)) {
			ret = parseUnknown(rawVoice, json, strScene, strAction);
		} else if ("fix".equals(strScene)) {
			ret = parseFix(rawVoice, json, strScene, strAction);
		} else if ("wechat".equals(strScene)) {
			ret = parseWechat(rawVoice, json, strScene, strAction);
		} else if ("query".equals(strScene)) {
            if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CUSTOM))
				ret = true;
			else {
				String result = "";
				result = json.getString("result");
				speakWords(result, false);
				ret = true;
			}
		} else if ("system".equals(strScene)) {
			ret = parseSystem(rawVoice, json, strScene, strAction);
		} else if ("radio".equals(strScene)) {
			ret = parseRadio(rawVoice, json, strScene, strAction);
		} else if ("help".equals(strScene)) {
            if (!SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_HELP))
                JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                        VoiceData.SUBEVENT_VOICE_SHOW_HELP);
            ret = true;
        } else if ("local".equals(strScene)) {
            ret = parseLocal(rawVoice, json, strScene, strAction);
        } else if ("movie".equals(strScene)) {
            HelpHitTispUtil.getInstance().hitMovieTips();
            if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_MOVIE))
				ret = true;
			//流量卡拦截了就不走下面的逻辑
			if (!ret) {
				if (procSenceByRemote("movie", json.toString())) {
					ret = true;
				} else {
//                    ViewPluginUtil vpu = WinManager.getInstance().getViewPluginUtil();
//                    vpu.showText(json.toString());
                	ChoiceManager.getInstance().showMovieList(json.toString());
					ret = true;
				}
			}
		}else if ("flight".equals(strScene)) {//机票 
			ret = parseTicket(rawVoice, json, strScene, strAction);
		}else if ("train".equals(strScene)) {//火车票
            ret = parseTrain(rawVoice, json, strScene, strAction);
		}else if ("airC".equals(strScene)) {
			ret = parseAC(rawVoice, json, strScene, strAction);
		}else if ("limit_speed".equals(strScene)) {
			ret = parseLimitSpeed(rawVoice, json, strScene, strAction);
		}else if ("flow".equals(strScene)) {
			ret = parseNetFlow(rawVoice, json, strScene, strAction);
        } else if ("joke".equals(strScene)) {
            ret = parseJoke(rawVoice, json, strScene, strAction);
        } else if ("reminder".equals(strScene)) {
        	ret = parseReminder(rawVoice, json, strScene, strAction);
        }else if ("news".equals(strScene)) {
        	ret = parseNews(rawVoice, json, strScene, strAction);
        } else if ("home_control".equals(strScene)) {
            ret = parseHomeControl(rawVoice, json, strScene, strAction);
        }else if("wan_mi".equals(strScene)){
            ret = parseWanMi(rawVoice, json, strScene, strAction);
			endSelectScene = false;
        }else if("qi_wu".equals(strScene)){
			ret = parseQiWu(rawVoice, json, strScene, strAction);
		}else if ("txzing".equals(strScene)) {
            ret = parseTxzing(rawVoice, json, strScene, strAction);
        } else if ("txz_operation".equals(strScene)) {
            ret = parseTxzOperation(rawVoice, json, strScene, strAction);
        } else if ("constellation".equals(strScene)) {
            ret = parseConstellation(rawVoice, json, strScene, strAction);
        } else if ("competition".equals(strScene)) {
            ret = parseCompetition(rawVoice, json, strScene, strAction);
        }
        if (!ret) {
            MonitorUtil.monitorCumulant("text.parse.E.unknowScene");
            parseRawData(rawVoice, strRawText, rawVoice.uint32SessionId);
        }
		if (mNeedProc) {
			mLastText = strRawText;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_SET_LAST_VOICE_KEYWORDS,
					strRawText);
			sendCommonResult(VoiceData.COMMON_RESULT_TYPE_PROCESSED, rawVoice, endSelectScene);
		}
		AsrManager.getInstance().printNlpTimeCast();
		return true;
    }
	
    private boolean parseCompetition(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        return CompetitionManager.getInstance().parseCompetition(rawVoice);
    }
    
    private boolean parseConstellation(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        return ConstellationManager.getInstance().handleResult(json);
    }

    private boolean parseWanMi(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        HelpHitTispUtil.getInstance().hitMovieTips();
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_MOVIE))
           return false;
        if (procSenceByRemote("wan_mi", json.toString())) {
            return false;
        }
        return FilmManager.getInstance().parseWanMi(json,strAction);
    }
    private boolean parseQiWu(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction){
		return QiWuTicketManager.getInstance().parseQiWu(json, strAction);
	}


    private boolean parseTxzing(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        if ("smart_home_control".equals(strAction)) {
            return CarControlHomeManager.getInstance().handleCarControlHomeResult(json);
        }
        return false;
    }

    private boolean parseTxzOperation(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        if ("official_account_bind".equals(strAction)) {
            return BindDeviceManager.getInstance().handleResult(json);
        }
        return false;
    }
    private boolean parseHomeControl(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        JSONObject result = json.getJSONObject(HomeControlManager.KEY_RESULT);
        Integer id = json.getInteger("id");
        JSONObject data = null;
        if (null == result) {
            JNIHelper.logd("the result object is null");
            return false;
        }
        data = result.getJSONObject(HomeControlManager.KEY_DATA);
        if (null == data) {
            JNIHelper.logd("the data object is null");
            return false;
        }
        if (HomeControlManager.ACTION_CONTROL.equals(strAction)) {
            String status = result.getString(HomeControlManager.KEY_STATUS);
            HomeControlManager.getInstance().setCurrentSessionId(id);
            HomeControlManager.getInstance().setReportJsonObject(json);
            return HomeControlManager.getInstance().handleControlResult(data, status);
        } else if(HomeControlManager.ACTION_AUTHORIZATION.equals(strAction)) {
            String url = data.getString(HomeControlManager.KEY_URL);
            if (TextUtils.isEmpty(url)) {
                JNIHelper.logd("The authorization url is null");
                return false;
            }
            HomeControlManager.getInstance().setCurrentSessionId(id);
            HomeControlManager.getInstance().showAuthorization(url);
            return true;
        }
        return false;
    }

	private boolean parseRawData(VoiceParseData rawVoice, String strRawText, Integer uint32SessionId) {
		JNIHelper.logd("begin parseUnknowVoice");
		parseUnknowVoice(rawVoice, rawVoice.uint32SessionId);
		return true;
	}

	private void parseUnknowVoice(VoiceParseData rawVoice, Integer uint32SessionId) {
		if (!InterruptTts.getInstance().doInterrupt(uint32SessionId)) {
			sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNKNOW, rawVoice);
		}
	}
	public void sendCommonResult(int type, VoiceParseData rawVoice){
		sendCommonResult(type, rawVoice ,true);
	}
	public void sendCommonResult(int type, VoiceParseData rawVoice, boolean endSelectScene) {
		mNeedProc = false;
		VoiceParseCommResult result = new VoiceParseCommResult();
		result.boolLocal = mLocal;
		if (rawVoice.boolManual != null && rawVoice.boolManual == 0) {
			result.boolManual = false;
		} else {
			result.boolManual = true;
		}
		result.uint32ResultType = type;
		result.strAnswerText = mAnswer;
		result.strUserText = rawVoice.strText;
		result.uint32SessionId = rawVoice.uint32SessionId;
		result.uint32GrammarId = rawVoice.uint32Sence;
		result.boolEndSelectScene = endSelectScene;
		// int status = isGrammarAllComplete();// 写成固定的，测试时再看，不行开接口
		int status = 0;
		result.uint32GrammarCompileStatus = status;
		int i = 0;
		while (true) {
			String str = NativeData.getResString("RS_IGNORE_PRODUCT", i++);
			if (TextUtils.isEmpty(str)) {
				break;
			}

			if (rawVoice.strText.indexOf(str) != -1) {
				String answer = NativeData
						.getResString("RS_VOICE_UNSUPPORT_OPERATE");
				result.strAnswerText = answer;
				break;
			}
		}
		JNIHelper.logd("voice enter sendCommonResult, grammar["
				+ rawVoice.uint32Sence + "/" + status + "], " + "session["
				+ rawVoice.uint32SessionId + "], manual[" + rawVoice.boolManual
				+ "], local[" + mLocal + "], " + "type[" + type + "], answer["
				+ mAnswer + "]");
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
		mAnswer = null;

    }
    
	private boolean parseNews(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
		do {
			if (!TextUtils.equals("search", strAction)) {
				break;
			}

			JSONObject objResult = null;
			JSONObject objData = null;
			// 必要字段,缺失按异常流程处理
			try {
				objResult = json.getJSONObject("result");
			} catch (Exception e) {
				break;
			}
			if (objResult == null) {
				break;
			}

			// 必要字段,缺失按异常流程处理
			try {
				objData = objResult.getJSONObject("data");
			} catch (Exception e) {
				break;
			}
			if (objData == null) {
				break;
			}

			SimpleNewsData.NewsInfos infos = new SimpleNewsData.NewsInfos();
			JSONArray objNewsList = null;
			try {
				objNewsList = objData.getJSONArray("newsList");
			} catch (Exception e) {

			}

			if (objNewsList != null) {
				int size = objNewsList.size();
				infos.rptMsgNewsList = new SimpleNewsData.NewsData[size];
				for (int i = 0; i < size; ++i) {
					JSONObject obj = objNewsList.getJSONObject(i);
					if (obj == null) {
						continue;
					}
					infos.rptMsgNewsList[i] = new SimpleNewsData.NewsData();
					try {
						infos.rptMsgNewsList[i].strEditTime = obj.getString(
								"editTime").getBytes();
					} catch (Exception e) {
					}
					try {
						infos.rptMsgNewsList[i].strContent = obj.getString(
								"content").getBytes();
					} catch (Exception e) {
					}
					try {
						infos.rptMsgNewsList[i].strFrom = obj.getString("from")
								.getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgNewsList[i].strId = obj.getString("id")
								.getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgNewsList[i].strSource = obj.getString(
								"source").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgNewsList[i].strTitle = obj.getString(
								"title").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgNewsList[i].strType = obj.getString("type")
								.getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgNewsList[i].strVoiceUrl = obj.getString(
								"voiceUrl").getBytes();
					} catch (Exception e) {

					}
				}

			}

			JNIHelper.sendEvent(UiEvent.EVENT_NEWS,
					SimpleNewsData.SUBEVENT_NEWS_QUERY,
					MessageNano.toByteArray(infos));
			return true;

		} while (false);
		return true;
	}
    
    private boolean parseTrain(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
        if ("query".equals(strAction)) {
            if (json.containsKey("result")) {
                boolean b = TicketManager.getInstance().checkTrainNLP(json.toString());
                if (b) {
                    return true;
                }
            } else {
//                TicketManager.getInstance().queryTicket(json.toString());
                speakWords(NativeData.getResString("RS_TRAIN_TICKET_ERROR"), false);
                return true;
            }
        } else if ("answer".equals(strAction)/* && ConfigManager.getInstance().enableQueryTicket*/) {
            String scene = "火车票";
            String answerString = json.getString("answer");
            String spk;

            if (!TextUtils.isEmpty(answerString) && answerString.contains("换个")) {
                spk = answerString;
            } else {
            	spk = NativeData.getResString("RS_TRAIN_TICKET_NO_RESULT");
//                spk = NativeData.getResString("RS_VOICE_GUIDE_INPUT_DES").replace("%TICKET%", scene);
            }
            speakWords(spk, false);
            return true;
        }

        speakWords(NativeData.getResString("RS_TRAIN_TICKET_TIP"), false);
        return true;
    }

    private boolean parseTicket(VoiceParseData rawVoice, JSONObject json, String strScene, String strAction) {
    	//后面航班语义数据
		do {
			if (!TextUtils.equals("search", strAction)) {
				break;
			}
			
			JSONObject objResult = null;
			JSONObject objData = null;
			//必要字段,缺失按异常流程处理
			try{
				objResult = json.getJSONObject("result");
			}catch(Exception e){
				break;
			}
			if (objResult == null){
				break;
			}
			
			//必要字段,缺失按异常流程处理
			try {
				objData = objResult.getJSONObject("data");
			} catch (Exception e) {
				break;
			}
			if (objData == null){
				break;
			}
			
			FlightData.FlightInfos infos = new FlightData.FlightInfos();
			//可缺失字段
			try {
				infos.strArrivalCity = objData.getString("arrivalCity").getBytes();
			} catch (Exception e) {

			}
			//可缺失字段
			try{
				infos.strDepartureCity = objData.getString("departureCity").getBytes();
			}catch(Exception e){
				
			}
			//可缺失字段
			try{
				infos.strDepartureDate = objData.getString("departureDate").getBytes();
			}catch(Exception e){
				
			}
			JSONArray objTicketList = null;
			try {
				objTicketList = objData.getJSONArray("planeTicketList");
			} catch (Exception e) {

			}
			
			if (objTicketList != null){
				int size = objTicketList.size();
				infos.rptMsgTickets = new FlightData.TicketData[size];
				for (int i = 0; i < size; ++i){
					JSONObject obj = objTicketList.getJSONObject(i);
					if (obj == null){
						continue;
					}
					infos.rptMsgTickets[i] = new FlightData.TicketData();
					try {
						infos.rptMsgTickets[i].strAirline = obj.getString("airline").getBytes();
					} catch (Exception e) {
					}
					try {
						infos.rptMsgTickets[i].strArrivalAirportName = obj.getString("arrivalAirportName").getBytes();
					} catch (Exception e) {
					}
					try {
						infos.rptMsgTickets[i].strArrivalTime = obj.getString("arrivalTime").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strArrivalTimeHm = obj.getString("arrivalTimeHm").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].uint64ArrivalUnixTimestamp = obj.getLong("arrivalUnixTimestamp");
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strDepartAirportName= obj.getString("departAirportName").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strDepartTime = obj.getString("departTime").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strDepartTimeHm = obj.getString("departTimeHm").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].uint64DepartUnixTimestamp = obj.getLong("departUnixTimestamp");
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strEconomyCabinDiscount = obj.getString("economyCabinDiscount").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].uint32EconomyCabinPrice = obj.getInteger("economyCabinPrice");
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].strFlightNum = obj.getString("flightNo").getBytes();
					} catch (Exception e) {

					}
					try {
						infos.rptMsgTickets[i].uint32TicketCount = obj.getInteger("ticketCount");
					} catch (Exception e) {
						
					}
				}
				
			}
			
			JNIHelper.sendEvent(UiEvent.EVENT_FLIGHT, FlightData.SUBEVENT_PLANE_QUERY, MessageNano.toByteArray(infos));
			return true;
			
		} while (false);
        if("flight".equals(strScene)){
            String text = NativeData.getResString("RS_VOICE_FLIGHT_SEARCH_ERROR_TIP");
            RecorderWin.speakText(text, null);
            return true;
        }

        if ("query".equals(strAction) && ConfigManager.getInstance().enableQueryTicket) {
		    TicketManager.getInstance().queryTicket(json.toString());
            return true;
        } else if ("answer".equals(strAction) && ConfigManager.getInstance().enableQueryTicket) {
            String scene = "机票";
            if ("train".equals(strScene)) {
                scene = "火车票";
            }

            String answerString = json.getString("answer");
            String spk;
            if (!TextUtils.isEmpty(answerString) && answerString.contains("换个")) {
                spk = answerString;
            } else {
                spk = NativeData.getResString("RS_VOICE_GUIDE_INPUT_DES").replace("%TICKET%", scene);
            }

            speakWords(spk, false);
            return true;
        }

		speakWords(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), false);
		return true;
	}
	
	private boolean parseAC(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
        if (ACManager.hasRemoteAcMgrToolImpl()) {
            int event =  json.getIntValue("event");
            if (event == UiEvent.EVENT_UI_BACK_GROUD_COMMAND) {
                JNIHelper.sendEvent(event, 0,json.getString("cmd"));
            }else {
                VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
                cmdWord.cmdData = json.getString("cmd");
                cmdWord.stringData = json.getString("text");
                cmdWord.voiceData = json.getString("voiceData");
                JNIHelper.sendEvent(event, 0,cmdWord);
            }
            return true;
        } else {
            return false;
        }
	}

	private boolean parseLocal(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if ("makeCall".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
					"CALL_MAKESURE_CALL");
		} else if ("cancelCall".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
					"CALL_CANCEL_CALL");
		} else if ("reject".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_REJECT_CALL);
		} else if ("accept".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_ACCEPT_CALL);
		} else if ("smsY".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
					"SAY_ENSURE_ACCEPT");
		} else if ("smsN".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
					"SAY_ENSURE_REJECT");
		}
		else {
			return false;
		}

		return true;
	}

	private boolean parseRadio(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_FM))
			return true;
        String strType = json.getString("waveband");
		String strFreq = json.getString("hz");
		String strUnit = json.getString("unit");
		if(strAction != null && "playName".equals(strAction)){
			String freqName = json.getString("freqName");
			strFreq = FmManager.getInstance().transFMName2Freq(freqName);
			LogUtil.logd("parse frep by name, freq = " + strFreq);
		}
		if (TextUtils.isEmpty(strType) || TextUtils.isEmpty(strFreq)
				|| TextUtils.isEmpty(strUnit)) {
			return false;
		}
		if ("fm".equals(strType)) {
			return toFMFreq(strFreq, strUnit);
		} else if ("am".equals(strType)) {
			return toAMFreq(strFreq, strUnit);
		} else {
            parseNotSupportOperateWithAnswer(rawVoice, json);
            return true;
		}
	}
	

	private boolean toAMFreq(String strFreq, String strUnit) {
		FMResultData fmResultData = new FMResultData();
		fmResultData.strFreq = strFreq;
		fmResultData.uint32Unit = 2;
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_AM, FmData.SUBEVENT_FM_TOFREQ,
				fmResultData);
		return true;
	}

	private boolean toFMFreq(String strFreq, String strUnit) {
		FMResultData fmResultData = new FMResultData();
		fmResultData.strFreq = strFreq;
		fmResultData.uint32Unit = 2;
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_FM, FmData.SUBEVENT_FM_TOFREQ,
				fmResultData);
		return true;
	}

	private boolean parseSystem(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_SYS_CTL))
			return true;
		String strCmd = null;
		strCmd = json.getString("choice");
		AsrManager.getInstance().setNeedCloseRecord(true);
        ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId().setType("system")
                .setAction(strAction).putExtra("cmd", strCmd).buildCommReport());
        if ("light".equals(strAction)) {
            HelpHitTispUtil.getInstance().hitUpdateLightTips();
            if ("up".equals(strCmd)) {
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "FEEDBACK_CMD_LIGHT_UP");
                return true;
            } else if ("down".equals(strCmd)) {
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "FEEDBACK_CMD_LIGHT_DOWN");
                return true;
            } else if ("max".equals(strCmd)) {
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "FEEDBACK_CMD_LIGHT_MAX");
                return true;
            } else if ("min".equals(strCmd)) {
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "FEEDBACK_CMD_LIGHT_MIN");
                return true;
            } else {
                parseNotSupportOperateWithAnswer(rawVoice, json);
                return true;
			}

        } else if ("volume".equals(strAction)) {
            if ("up".equals(strCmd)) {
                HelpHitTispUtil.getInstance().hitUpdateVolumeTips();
                if (json.containsKey("value")) {
                    VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
                    cmdWord.cmdData = "FEEDBACK_CMD_VOL_CTRLUP";
                    cmdWord.stringData = json.getString("text");
                    cmdWord.voiceData = json.getString("value");
                    JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW, 0,
                            cmdWord);
                }else {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_UP");
                }
				return true;
			} else if ("down".equals(strCmd)) {
                if (json.containsKey("value")){
                    VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
                    cmdWord.cmdData = "FEEDBACK_CMD_VOL_CTRLDOWN";
                    cmdWord.stringData = json.getString("text");
                    cmdWord.voiceData = json.getString("value");
                    JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW, 0,
                            cmdWord);
                } else {
                    JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                            "FEEDBACK_CMD_VOL_DOWN");
                }
				return true;
			} else if ("close".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_MUTE_ON");
				return true;
			} else if ("open".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_MUTE_OFF");
				return true;
			} else if ("max".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_MAX");
				return true;
			} else if ("min".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_MIN");
                return true;
            } else if ("set".equals(strCmd)) {
                VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
                cmdWord.cmdData = "FEEDBACK_CMD_VOL_CTRLTO";
                cmdWord.stringData = json.getString("text");
                cmdWord.voiceData = json.getString("value");
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND_NEW, 0,
                        cmdWord);
				return true;
			} else {
                parseNotSupportOperateWithAnswer(rawVoice, json);
                return true;
            }
        } else if ("wifi".equals(strAction)) {
            if ("open".equals(strCmd)) {
                HelpHitTispUtil.getInstance().hitUpdateWifiStatusTips();
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "GLOBAL_CMD_OPEN_WIFI");
                return true;
            } else if ("close".equals(strCmd)) {
                HelpHitTispUtil.getInstance().hitUpdateWifiStatusTips();
                JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
                        "GLOBAL_CMD_CLOSE_WIFI");
                return true;
            } else {
                parseNotSupportOperateWithAnswer(rawVoice, json);
                return true;
			}
		}
        parseNotSupportOperateWithAnswer(rawVoice, json);
        return true;
	}

	private boolean parseWechat(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (procSenceByRemote("wechat", json.toString())) {
			return true;
		}
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_WE_CHAT))
			return true;
//		if(NetworkManager.getInstance().checkLeastFlow()){
//			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
//			RecorderWin.speakText(resText, null);
//		}

        int msg = 0;
        if ("send".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_SESSION_DIRECT;
        } else if ("mask".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_DIRECT;
        } else if ("unmask".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT;
        } else if ("sharePlace".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_PLACE_DIRECT;
        } else if ("history".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_HISTORY_DIRECT;
        } else if ("sharePhoto".equals(strAction)) {
            msg = UiMakeWechatSession.SUBEVENT_MAKE_PHOTO_DIRECT;
        } else {
            parseNotSupportOperateWithAnswer(rawVoice, json);
            return true;
		}
		String keywords = null;
		int type = 0;
		keywords = json.getString("keywords");
		type = json.getIntValue("type");
		String expression = "";
		expression = json.getString("expression");
		handleResult(keywords, type, msg, expression);
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("wechat").setSessionId()
                .setAction(strAction).buildCommReport());
		return true;
	}

	private void handleResult(String strContactName, int type, int msg,
			String expression) {
		if (TextUtils.isEmpty(strContactName)) {
			makeSession(null, msg, expression);
			return;
		}
		WechatContactData.QueryContacts queryContacts = new QueryContacts();
		queryContacts.int32Score = 4000;
		queryContacts.int32Type = type;
		queryContacts.strName = strContactName;
		byte[] data = NativeData.getNativeData(
				UiData.DATA_ID_WECHAT_CONTACT_INFO_BY_NAME, queryContacts);

		WeChatContacts weChatContacts = new WeChatContacts();
		List<WeChatContact> fullList = new ArrayList<WeChatContact>();
		List<WeChatContact> eightList = new ArrayList<WeChatContact>();
		List<WeChatContact> sixList = new ArrayList<WeChatContact>();
		List<WeChatContact> fourList = new ArrayList<WeChatContact>();
		if (data == null) {
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT,
					UiWechat.SUBEVENT_ERR_NO_CONTACT);
			return;
		}
		try {
			weChatContacts = WeChatContacts.parseFrom(data);
		} catch (InvalidProtocolBufferNanoException e) {
			JNIHelper.loge(e.getMessage());
			return;
		}
		if (weChatContacts == null || weChatContacts.cons == null) {
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT,
					UiWechat.SUBEVENT_ERR_NO_CONTACT);
			return;
		}
		WeChatContact[] cons = weChatContacts.cons;
		for (int i = 0; i < cons.length; i++) {
			if (cons[i] != null && cons[i].score == 10000) {
				fullList.add(cons[i]);
			} else if (cons != null && cons[i].score >= 8500) {
				eightList.add(cons[i]);
			} else if (cons != null && cons[i].score >= 6000) {
				sixList.add(cons[i]);
			} else if (cons != null && cons[i].score >= 4000) {
				fourList.add(cons[i]);
			} else {
			}
		}
		if (fullList.size() > 0) {
			weChatContacts.cons = (WeChatContact[]) fullList
					.toArray(new WeChatContact[fullList.size()]);
			makeSession(weChatContacts, msg, expression);
			return;
		} else if (eightList.size() > 0) {
			weChatContacts.cons = (WeChatContact[]) eightList
					.toArray(new WeChatContact[eightList.size()]);
			makeSession(weChatContacts, msg, expression);
			return;
		} else if (sixList.size() > 0) {
			weChatContacts.cons = (WeChatContact[]) sixList
					.toArray(new WeChatContact[sixList.size()]);
			makeSession(weChatContacts, msg + 1, expression);
			return;
		} else if (fourList.size() > 0) {
			weChatContacts.cons = (WeChatContact[]) fourList
					.toArray(new WeChatContact[fourList.size()]);
			makeSession(weChatContacts, msg + 1, expression);
			return;
		} else {
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT,
					UiWechat.SUBEVENT_ERR_NO_CONTACT);
		}
	}

	private boolean makeSession(WeChatContacts pobjContacts, int flag,
			String expression) {
		JNIHelper.logd("makeSession");
		if (pobjContacts == null || pobjContacts.cons == null
				|| 0 == pobjContacts.cons.length) {
			JNIHelper
					.loge("pobjContacts==null || pobjContacts.cons==null||0 == pobjContacts.cons.length");
			JNIHelper.sendEvent(UiEvent.EVENT_WECHAT_MAKESESSION, flag, "");
		} else {
			pobjContacts.expression = expression;
			JNIHelper.sendEvent(UiEvent.EVENT_WECHAT_MAKESESSION, flag,
					pobjContacts);
		}
		return true;
	}

	private boolean parseFix(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (!json.containsKey("good") || !json.containsKey("bad")) {
			return false;
		}
		String good = json.getString("good");
		String bad = json.getString("bad");
		return fixResult(good, bad, rawVoice);
	}

	private boolean fixResult(String strGoodWord, String strBadWord,
			VoiceParseData rawVoice) {
		String strLast = mLastText;
		JNIHelper.logd("fixResult:begin find keyWord,last=" + strLast);
		if (strLast.indexOf(strBadWord) != -1) {
			if(strBadWord != null && strGoodWord != null){
				strLast = strLast.replace(strBadWord, strGoodWord);
			}
			parseRawVoiceOnline(strLast, VoiceData.GRAMMAR_SENCE_FIX_RESULT,
					rawVoice);
			return true;
		}
		return false;
	}

	private boolean parseRawVoiceOnline(String strRawText, int sence,
			VoiceParseData rawVoice) {
		VoiceData.VoiceParseData data = new VoiceData.VoiceParseData();
		data.clear();
		data.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		data.strText = strRawText;
		data.uint32Sence = sence;
		data.floatTextScore = 0f;
		if (VoiceData.GRAMMAR_SENCE_FIX_RESULT != sence) {
			data.strVoiceEngineId = rawVoice.strVoiceEngineId;
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE_NEW, data);
		return true;
	}

    private boolean parseUnknown(VoiceParseData rawVoice, JSONObject json,
                                 String strScene, String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CHAT))
            return true;
        if ("unknown".equals(strAction)) {
        	if ("set_constellation_ok".equals(json.getString("style"))) {
        		AsrManager.getInstance().setNeedCloseRecord(true);
				String text = json.getString("text");
				for (String constellation : ConstellationManager.CONSTELLATION_ARRAYS) {
					if (text.contains(constellation)) {
						ConstellationManager.getInstance().saveConstellation(constellation);
						break;
					}
				}
				RecorderWin.speakTextWithClose(json.getString("answer"), null);
        		return true;
			} else if ("set_constellation_fail".equals(json.getString("style"))) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(json.getString("answer"), null);
        		return true;
			}
		}
        if("unsupport".equals(strAction)){
			mLastText = rawVoice.strText;
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_SET_LAST_VOICE_KEYWORDS,
					rawVoice.strText);
			return parseNotSupportOperate(rawVoice, json);
		}
        mAnswer = json.getString("answer");

        if ("joke".equals(json.getString("style"))) {
            HelpHitTispUtil.getInstance().hitJokeTips();
        } /*else if ("baike".equals(json.getString("style"))) {
			HelpHitTispUtil.getInstance().hitBaiKeTips();
		}*/
		if("true".equals(json.getString("local")) && TextUtils.isEmpty(mAnswer)){
			sendCommonResult(VoiceData.COMMON_RESULT_TYPE_NET_REQUEST_FAIL, rawVoice);
		}else {
        	sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNKNOW, rawVoice);
		}
        return true;
    }

    private boolean parseJoke(VoiceParseData rawVoice, JSONObject json,
                                 String strScene, String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CHAT))
            return true;
        mAnswer = json.getString("answer");
        sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNKNOW, rawVoice);
        return true;
    }
    
    private boolean parseReminder(VoiceParseData rawVoice, JSONObject json,
            String strScene, String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CHAT))
            return true;
        LogUtil.logd("remind json = " + json.toString());
        //将提醒事件进行标志设为True，表示正在进行提醒事件
        ReminderManager.getInstance().setIsReminding(true);
        //将用户上一次不说话标志置为false，表示上一次说话
        ReminderManager.getInstance().setFirstReminderEmpty(false);
        if ("set".equals(strAction)) {//新增提醒
            return ReminderManager.getInstance().createReminder(json);
        }
        if("reply".equals(strAction)){
            return ReminderManager.getInstance().replyReminder(json);
        }
        if("unknow".equals(strAction)){
			return ReminderManager.getInstance().replyUnknow();
		}
        return false;
    }

    private boolean parseEmpty(VoiceParseData rawVoice, JSONObject json,
                               String strScene, String strAction) {

		if(ReminderManager.getInstance().getIsReminding() ){
			return ReminderManager.getInstance().parseEmpty();
		}

        sendCommonResult(VoiceData.COMMON_RESULT_TYPE_EMPTY, rawVoice);
        return true;
    }

    /**
     * 对不支持的操作，直接提示“抱歉，该操作我还没学会”
     *
     * @param rawVoice
     * @param json
     * @return
     */
    private boolean parseNotSupportOperateWithAnswer(VoiceParseData rawVoice, JSONObject json) {
        mAnswer = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
        sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNSUPPORT, rawVoice);
        return true;
    }

	private boolean parseNotSupportOperate(VoiceParseData rawVoice, JSONObject json) {
		mAnswer = json.getString("answer");
		sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNSUPPORT, rawVoice);
		return true;
	}

	private boolean parseLimitNumber(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (procSenceByRemote("limit_number", json.toString())) {
			return true;
		}

        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_TRAFFIC_LIMT))
			return true;
		
		if (json.containsKey("result")) {
			speakWords(json.getString("result"), false);
		} else {
			String str = NativeData
					.getResString("RS_VOICE_TRAFFIC_CONTROL_NOT_FOUND");
			if(json.containsKey("city")){
				str = str.replace("%CITY%", json.getString("city"));
			}
			speakWords(str, false);
		}
		return true;
	}
	
	private boolean parseLimitSpeed(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (NavManager.getInstance().broadLimitSpeech()) {
			return true;
		}
		speakWords(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), false);
		return true;
	}

	private boolean parseNetFlow(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (TextUtils.equals("setting", strAction)) {
			String strFlowValue = json.getString("flowValue");
			int flowValue = -1;
			if (isNumeric(strFlowValue)) {
				try {
					flowValue = Integer.valueOf(strFlowValue);
				} catch (Exception e) {
				}
			}else {
				try {
					flowValue = ExchangeHelper.chineseToNumber(strFlowValue);
				} catch (Exception e) {
				}
			}
			if (flowValue > -1) {
				SimManager.getInstance().setFlowThreshold(flowValue);
				return true;
			}
			
		}
        parseNotSupportOperateWithAnswer(rawVoice, json);
		return true;
	}
	
	public boolean isNumeric(String str){ 
		   Pattern pattern = Pattern.compile("[0-9]*"); 
		   Matcher isNum = pattern.matcher(str);
		   if( !isNum.matches() ){
		       return false; 
		   } 
		   return true; 
	}
	
	private boolean parseTraffic(VoiceParseData rawVoice, JSONObject json,// 以为您打开路况然后没反应
			String strScene, String strAction) {
		
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_TRAFFIC_COND))
			return true;
//		if(NetworkManager.getInstance().checkLeastFlow()){
//			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
//			RecorderWin.speakText(resText, null);
//		}
		
		if (procSenceByRemote("traffic", json.toString())) {
			return true;
		}
		VoiceData.RoadTrafficQueryInfo pbRoadTrafficQueryInfo = new VoiceData.RoadTrafficQueryInfo();
		pbRoadTrafficQueryInfo.strKeywords = json.getString("strKeywords");
		pbRoadTrafficQueryInfo.strDirection = json.getString("strDirection");
		pbRoadTrafficQueryInfo.strCity = json.getString("strCity");
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC,
				pbRoadTrafficQueryInfo);
		return true;
	}

    private boolean parseLocation(VoiceParseData rawVoice, JSONObject json,
                                  String strScene, String strAction) {
        HelpHitTispUtil.getInstance().hitLocationTips();
        if (procSenceByRemote("location", json.toString())) {
            return true;
        }
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CUR_POS))
			return true;
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SHOW_LOCATION);
		return true;
	}

	private boolean parseStock(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		try {
			if (procSenceByRemote("stock", json.toString())) {
				return true;
			}
           return StockManager.getInstance().parseStock(json);
		} catch (Exception e) {
			JNIHelper.loge(e.getMessage());
			JNIHelper
					.loge("TextSemanticAnalysis parseStock NumberFormatException");
            MonitorUtil.monitorCumulant("text.parse.E.stock");
			return false;
		}
	}

    private String formatStockString(String price){
        if (TextUtils.isEmpty(price)) {
            return "";
        }
        return StringUtils.subZeroAndDot(String.format("%.2f", Float.parseFloat(price)));
    }

    private boolean parseWeather(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
    	//是后台返回的天气结果，直接使用
//    	if (json.containsKey("textValue")) {
//    		handleWeatherData(json);
//		}else {
//			lastWeatherJson = json;
//			String city = json.getString("city");
//			String date = json.getString("date");
//			textWeatherTaskIds.add(NetDataManager.getInstance().procWeatherBackgroundInner(city, date,rawVoice.strText));
//		}
        HelpHitTispUtil.getInstance().hitWeatherTips();
        WeatherManager.getInstance().parseWeather(rawVoice,json);
		
		return true;
	}
    


    /**
	/**
	 * 语音播报
	 * 
	 * @param strWords
	 * @param closeRecord
	 */
	private void speakWords(String strWords, boolean closeRecord) {
		if (closeRecord) {
			AsrManager.getInstance().setNeedCloseRecord(true);
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_CLOSE_RECORD, strWords);
	}

	private boolean parseAudio(VoiceParseData rawVoice,
			com.alibaba.fastjson.JSONObject json, String strScene,
			String strAction) {
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_RADIO))
			return true;
//		if(NetworkManager.getInstance().checkLeastFlow()){
//			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
//			RecorderWin.speakText(resText, null);
//			return true;
//		}
		if (procSenceByRemote("audio", json.toString())) {
			return true;
		}
		
		if ("play".equals(strAction)) {
            if (!json.containsKey("model") || TextUtils.equals("SEARCH_RANDOM",json.getString("code"))) {
				return AudioManager.getInstance().onCommand("AUDIO_CMD_PLAY",
						rawVoice.strVoiceData, rawVoice.strVoiceData) == 0;
			}
			UiRadio.RADIOModel pbRadioModel = new RADIOModel();
			JSONObject jsonModel = json.getJSONObject("model");
			pbRadioModel.strTitle = jsonModel.getString("title");
            if (jsonModel.containsKey("audioIndex")) {
                JSONArray audioIndex = jsonModel.getJSONArray("audioIndex");
//                UiRadio.RADIOIndex
                pbRadioModel.rptRadioIndex = new UiRadio.RADIOIndex[audioIndex.size() / 2];
                int index = 0;
                for (int i = 0; i < audioIndex.size(); i += 2) {
                    UiRadio.RADIOIndex radioIndex = new UiRadio.RADIOIndex();
                    radioIndex.int32Number = Integer.parseInt(audioIndex.getString(i));
                    radioIndex.strUnit = audioIndex.getString(i+1).getBytes();
                    pbRadioModel.rptRadioIndex[index] = radioIndex;
                    index++;
                }

            }
			if (jsonModel.getJSONArray("artist") != null) {
				pbRadioModel.rptStrArtist = new String[jsonModel.getJSONArray(
						"artist").size()];
				for (int i = 0; i < jsonModel.getJSONArray("artist").size(); i++) {
					pbRadioModel.rptStrArtist[i] = jsonModel.getJSONArray(
							"artist").getString(i);
				}
			}
			pbRadioModel.strAlbum = jsonModel.getString("album");
			pbRadioModel.strTag = jsonModel.getString("tag");
			pbRadioModel.strEpisode = jsonModel.getString("episode");
			pbRadioModel.strCategory = jsonModel.getString("category");
			pbRadioModel.strSubCategory = jsonModel.getString("subCategory");
			pbRadioModel.strVoiceText = rawVoice.strText;
            if ("相声".equals(jsonModel.getString("category"))) {
				HelpHitTispUtil.getInstance().hitXiangShengTips();
			} else if (!"".equals(jsonModel.getString("album"))) {
				HelpHitTispUtil.getInstance().hitAlbumTips();
			}
			if (jsonModel.getJSONArray("keywords") != null) {
				pbRadioModel.rptStrKeywords = new String[jsonModel
						.getJSONArray("keywords").size()];
				for (int i = 0; i < jsonModel.getJSONArray("keywords").size(); i++) {
					pbRadioModel.rptStrKeywords[i] = jsonModel.getJSONArray(
							"keywords").getString(i);
				}
			}
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
			JNIHelper.sendEvent(UiEvent.EVENT_CUSTOM_RADIO,
					UiRadio.SUBEVENT_RADIO_PLAY, pbRadioModel);
			return true;
		} else if ("pause".equals(strAction)) {
			return AudioManager.getInstance().onCommand("AUDIO_CMD_STOP",
					rawVoice.strVoiceData, rawVoice.strVoiceData) == 0;
		} else if ("prev".equals(strAction)) {
			return AudioManager.getInstance().onCommand("AUDIO_CMD_PREV",
					rawVoice.strVoiceData, rawVoice.strVoiceData) == 0;
		} else if ("next".equals(strAction)) {
			return AudioManager.getInstance().onCommand("AUDIO_CMD_NEXT",
					rawVoice.strVoiceData, rawVoice.strVoiceData) == 0;
		} else if ("ask".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO, "");
			return true;
		} else if ("exit".equals(strAction)) {
			return AudioManager.getInstance().onCommand("AUDIO_CMD_EXIT",
					rawVoice.strVoiceData, rawVoice.strVoiceData) == 0;
		}
		JNIHelper.logd("parseAudio strAction:" + strAction);
        parseNotSupportOperateWithAnswer(rawVoice, json);
        return true;
	}

	private boolean parseMusic(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {

        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_MUSIC))
			return true;
//		if(NetworkManager.getInstance().checkLeastFlow()){
//			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
//			RecorderWin.speakText(resText, null);
//			return true;
//		}
		mJsonMusic = json;
		if (useMusicRemoteSenceTool(rawVoice)) {
			LogUtil.logd("useMusicRemoteSenceTool");
			return true;
		}
		if ("play".equals(strAction)) {
			if (!json.containsKey("model")) {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_PLAY);
				return true;
			}
			UiMusic.MediaModel pbMediaModel = new MediaModel();

			JSONObject jsonModel = json.getJSONObject("model");
			pbMediaModel.strTitle = jsonModel.getString("title");

			if (jsonModel.getJSONArray("artist") != null) {
				pbMediaModel.rptStrArtist = new String[jsonModel.getJSONArray(
						"artist").size()];
				for (int i = 0; i < jsonModel.getJSONArray("artist").size(); i++) {
					pbMediaModel.rptStrArtist[i] = jsonModel.getJSONArray(
							"artist").getString(i);
				}
			}
            pbMediaModel.strAlbum = jsonModel.getString("album");
			pbMediaModel.strType = json.getString("type");
			if (jsonModel.getJSONArray("keywords") != null) {
				pbMediaModel.rptStrKeywords = new String[jsonModel
						.getJSONArray("keywords").size()];
				for (int i = 0; i < jsonModel.getJSONArray("keywords").size(); i++) {
					pbMediaModel.rptStrKeywords[i] = jsonModel.getJSONArray(
							"keywords").getString(i);
				}
			}
            String title = jsonModel.getString("title");
            if (pbMediaModel.rptStrArtist.length != 0 && title != null && !title.equals("")) {
                HelpHitTispUtil.getInstance().hitSingerAndSongNameTips();
            } else if (pbMediaModel.rptStrArtist.length != 0) {
                HelpHitTispUtil.getInstance().hitSingerTips();
            } else if (title != null && !title.equals("")) {
                HelpHitTispUtil.getInstance().hitSongNameTips();
            }
			if (useMusicRemoteProcPlayTool(rawVoice.strText)) {
				LogUtil.logd("useMusicRemoteProcPlayTool");
				return true;
			}
			byte[] mediaListByte = NativeData.getUIData(
					UiData.DATA_ID_MUSIC_INFO_BY_MODEL,
					MediaModel.toByteArray(pbMediaModel));

			UiMusic.MediaList pbMediaList = null;
			try {
				pbMediaList = MediaList.parseFrom(mediaListByte);
			} catch (Exception e) {
				JNIHelper.loge(e.getMessage());
			}
			if (pbMediaList != null && (pbMediaList.rptMediaItem.length > 0)) {
				playMediaList(pbMediaList, true, false);
				return true;
			}
			findNoneMusic(pbMediaModel, rawVoice);
		} else if ("continue".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PLAY_ALL, rawVoice);
		} else if ("pause".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PAUSE);
		} else if ("prev".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PREV);
		} else if ("next".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_NEXT);
		} else if ("playFavourMusic".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
		} else if ("playRandom".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);
		} else if ("switchSong".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_RANDOM);
		} else if ("switchModeLoopOne".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
		} else if ("switchModeLoopAll".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
		} else if ("switchModeRandom".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);
		} else if ("favourMusic".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
		} else if ("unfavourMusic".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
		} else if ("hateMusic".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_HATE_CUR);
		} else if ("ask".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO, "");
		} else if ("exit".equals(strAction)) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_EXIT);
		} else {
			JNIHelper.logd("parseMusic strAction:" + strAction);
            parseNotSupportOperateWithAnswer(rawVoice, json);
            return true;
		}
		return true;
	}

	private void findNoneMusic(MediaModel ppbMediaModel, VoiceParseData rawVoice) {
		JNIHelper.logd("findNoneMusic");
		if (checkInstallQQMusic() == false) {
			String str = NativeData.getResString("RS_VOICE_MUSIC_NOT_FOUND");
			speakWords(str, true);
			return;
		}
		if (ppbMediaModel == null) {
			UiMusic.MediaModel pbMediaModel = new UiMusic.MediaModel();
			seachMusicOnline(pbMediaModel, rawVoice);
		} else {
			seachMusicOnline(ppbMediaModel, rawVoice);
		}
	}

	private void seachMusicOnline(MediaModel pbMediaModel,
			VoiceParseData rawVoice) {
		pbMediaModel.strSearchText = rawVoice.strText;

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
				UiMusic.SUBEVENT_MEDIA_SEARCH_MEDIA_ONLINE, pbMediaModel);

	}

	private boolean checkInstallQQMusic() {
		boolean ret = true;
		byte[] byteData = NativeData.getUIData(UiData.DATA_ID_CHECK_APP_EXIST,
				"com.tencent.qqmusic".getBytes());
		String strData = null;
		if (byteData != null) {
			strData = new String(byteData);
		}
		if (TextUtils.isEmpty(strData)) {
			byteData = NativeData.getUIData(UiData.DATA_ID_CHECK_APP_EXIST,
					"com.tencent.qqmusicpad".getBytes());
			if (byteData != null) {
				strData = new String(byteData);
			}
			if (TextUtils.isEmpty(strData)) {
				ret = false;
			}
		}
		JNIHelper.logd("checkInstallQQMusic:" + ret);
		return ret;
	}

	private void playMediaList(MediaList pbMediaList, boolean bRandom,
			boolean bReference) {
		if (pbMediaList.int32CurIndex == -2) {
			bRandom = false;
			bReference = false;
			pbMediaList.int32CurIndex = 0;
		}

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PLAY_MEDIA, pbMediaList);
	}

	private boolean useMusicRemoteProcPlayTool(String text) {
		byte[] byteData = NativeData.getUIData(UiData.DATA_ID_REMOTE_PROC_TOOL,
				"music".getBytes());
		String strData = null;
		if (byteData != null) {
			strData = new String(byteData);
		}
		if (!TextUtils.isEmpty(strData)) {
			JSONObject json = mJsonMusic;
			if (json.containsKey("model")) {
				json.getJSONObject("model").put("text", text);
				String strSenceData = null;
				strSenceData = mJsonMusic.getString("model");
				JNIHelper.logd("EVENT_REMOTE_PROC_PLAY_MUSIC:" + strSenceData);
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
				JNIHelper.sendEvent(UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC, 0,
						strSenceData);
				return true;
			}
		}
		if (checkSenceEnable("music") == false) {
			LogUtil.logd("music sence disable");
			return true;
		}

		return false;
	}

	private boolean checkSenceEnable(String sence) {
		String strData = null;
		byte[] byteData = NativeData.getUIData(
				UiData.DATA_ID_CHECK_SENCE_DISABLE_REASON, sence.getBytes());
		if (byteData != null) {
			strData = new String(byteData);
		}
		if (!TextUtils.isEmpty(strData)) {
			JNIHelper.logw(sence + " disabled:" + strData);
			speakWords(strData, true);
			return false;
		}
		return true;
	}

	private boolean parsePoiChoice(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		if (procSenceByRemote("poi_choice", json.toString())) {
			return true;
		}

		return false;
	}

	private boolean parseNav(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {

        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_NAVI))
            return true;
        String city = json.getString("city");

        //这边先简单处理了，判断city字段的最后一位是否包含 市 ，没有的话补上，解决poi搜索走到附近搜索的问题
        if (city != null && city.length() > 0 && city.lastIndexOf("市") == -1) {
            if (city.lastIndexOf("省") == -1) {
                city += "市";
                json.put("city", city);
            }
        }

        if ("pass".equals(strAction)) {
            NavigateInfo pbNavigateInfo = new NavigateInfo();
            pbNavigateInfo.strTargetName = json.getString("keywords");
            JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                    VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                    UiMap.SUBEVENT_MAP_NAVIGATE_PASS_BY_NAME, pbNavigateInfo);
            return true;
		} else if ("passToPoi".equals(strAction)) {
			NavigateInfo pbNavigateInfo = new NavigateInfo();
			pbNavigateInfo.strTargetName = json.getString("keywords");

			JSONObject obj = new JSONObject();
			obj.put("action", "passToPoi");
			obj.put("strToPoi", json.getString("strToPoi"));
			pbNavigateInfo.strExtraInfo = obj.toJSONString();
			// 暂用途经点的方式处理
            JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                    VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
            JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                    UiMap.SUBEVENT_MAP_NAVIGATE_PASS_BY_NAME, pbNavigateInfo);
            return true;
        } else if ("home".equals(strAction)) {
            HelpHitTispUtil.getInstance().hitGoHomeTips();
            return navigateHome(rawVoice);
        } else if ("company".equals(strAction)) {
            HelpHitTispUtil.getInstance().hitCompanyTips();
            return navigateCompany(rawVoice);
        } else if ("modifyHome".equals(strAction)) {
            String strTar = json.getString("keywords");
            if (TextUtils.isEmpty(strTar)) {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                        UiMap.SUBEVENT_MAP_MODIFY_HOME);
            } else {
				boolean fuzzy = json.getBooleanValue("fuzzy");
				if (!fuzzy) {
					NavManager.getInstance().setHcFlag(NavManager.MODIFY_HC_ADDRESS);
				}
                UiMap.NavigateInfo pbNavigateInfo = new NavigateInfo();
                pbNavigateInfo.strTargetName = strTar;
                String strCity = json.getString("city");
                if (!TextUtils.isEmpty(strCity)) {
                    pbNavigateInfo.strTargetCity = strCity;
                }
				String userText = rawVoice.strText.replace(strTar,"");
				WinHelpManager.getInstance().checkSpeakHelpTips(userText, false);
                return navigateHome(pbNavigateInfo, rawVoice);
            }
            return true;
        } else if ("modifyCompany".equals(strAction)) {
            String strTar = json.getString("keywords");
            if (TextUtils.isEmpty(strTar)) {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                        UiMap.SUBEVENT_MAP_MODIFY_COMPANY);
            } else {
				boolean fuzzy = json.getBooleanValue("fuzzy");
				if (!fuzzy) {
					NavManager.getInstance().setHcFlag(NavManager.MODIFY_HC_ADDRESS);
				}
                UiMap.NavigateInfo pbNavigateInfo = new NavigateInfo();
                pbNavigateInfo.strTargetName = strTar;
                String strCity = json.getString("city");
                if (!TextUtils.isEmpty(strCity)) {
                    pbNavigateInfo.strTargetCity = strCity;
                }
				String userText = rawVoice.strText.replace(strTar,"");
				WinHelpManager.getInstance().checkSpeakHelpTips(userText, false);
                return navigateCompany(rawVoice, pbNavigateInfo);
            }
            return true;
        } else if ("search".equals(strAction)) {
            String strKws = json.getString("keywords");
            int radius = json.getIntValue("radius");
            String type = json.getString("type");
            String region = json.getString("region");

            strKws = isNearbyOrBussinessAction(strKws, null);// 关键字处理
			String condition = json.getString("condition");
			if (!TextUtils.isEmpty(condition)) {
				NavManager.getInstance().setTemporaryNavPlanType(condition);
			}

			//家|公司附近的<poi>
			if ("LOC_OFFICE".equals(json.getString("poi"))) {
				NavigateInfo info = NavManager.getInstance().getCompanyNavigateInfo();
				if (info == null || info.msgGpsInfo == null) {
				    NavigateInfo navigateInfo = new NavigateInfo();
				    navigateInfo.strTargetName = strKws;
				    navigateInfo.strRegion = region;
				    NavManager.getInstance().setNavigateInfo(navigateInfo);
					return navigateCompany(rawVoice);
				}else if(info.msgGpsInfo != null){
					double lng = info.msgGpsInfo.dblLng;
					double lat = info.msgGpsInfo.dblLat;
					String centerCity = info.strTargetCity;
					NavManager.getInstance().navigateNearBy(lng,lat,centerCity,strKws,region);
					return true;
				}
			} else if ("LOC_HOME".equals(json.getString("poi"))) {
				NavigateInfo info = NavManager.getInstance().getHomeNavigateInfo();
				if (info == null || info.msgGpsInfo == null) {
					NavigateInfo navigateInfo = new NavigateInfo();
					navigateInfo.strTargetName = strKws;
					navigateInfo.strRegion = region;
					NavManager.getInstance().setNavigateInfo(navigateInfo);
					return navigateHome(rawVoice);
				} else if (info.msgGpsInfo != null) {
					double lng = info.msgGpsInfo.dblLng;
					double lat = info.msgGpsInfo.dblLat;
					String centerCity = info.strTargetCity;
					NavManager.getInstance().navigateNearBy(lng, lat, centerCity, strKws, region);
					return true;
				}
			} else if (json.containsKey("fromPOI") && !"CURRENT_LOC".equals(json.getString("fromPOI"))) {
				NavigateInfo pbNavigateInfo = new NavigateInfo();
				pbNavigateInfo.strTargetName = json.getString("keywords");

				JSONObject obj = new JSONObject();
				obj.put("action", "fromPoiNavigation");
				obj.put("fromPoi", json.getString("fromPOI"));
				pbNavigateInfo.strExtraInfo = obj.toJSONString();
				// 暂用途经点的方式处理
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
						VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
						UiMap.SUBEVENT_MAP_NAVIGATE_BY_NAME, pbNavigateInfo);
				return true;
			}

			if (TextUtils.isEmpty(json.getString("poi")) && isNearbyName(strKws)) {// 判断是否为周边关键字
                return navigateNearBy(strKws, radius, json.getString("city"), region,
                        json.getString("poi"), rawVoice);
            } else if (isBussinessName(strKws)) {// 判断是否为商圈关键字
                HelpHitTispUtil.getInstance().hitHungryTips();
                return navigateBussiness(strKws, radius,
                        json.getString("city"), json.getString("region"), json.getString("poi"), rawVoice);
            } else if (!TextUtils.isEmpty(json.getString("poi")) && json.getString("poi").equals("END_POI")) {
                return navigateNearBy(strKws, radius, json.getString("city"), region,
                        json.getString("poi"), rawVoice);
            }

            if (!TextUtils.isEmpty(type)) {// 类型不为空的时候
                if ("nearby".equals(type)) {// 附近搜索
					return navigateNearBy(strKws, radius,
							json.getString("city"), region, json.getString("poi"),
							rawVoice);
				} else if ("bussiness".equals(type)) {// 商圈搜索
					return navigateBussiness(strKws, radius,
							json.getString("city"), json.getString("region"), json.getString("poi"),
							rawVoice);
				}
            }

            if (json.containsKey("pass")) {
                return navigateToTargetName(strKws, json.getString("city"), json.getString("region"),
                        json.getString("pass"), rawVoice);
            } else {
                HelpHitTispUtil.getInstance().hitNavToTips();
                return navigateToTargetName(strKws, json.getString("city"), json.getString("region"), "",
                        rawVoice);
            }
        } else if ("open".equals(strAction)) {
            CmdManager.getInstance().onUI_TAB_NAV();
            return true;
        } else if ("passAround".equals(strAction)) {
            //支持搜一下沿途的XXX的说法
            UiMap.NearbySearchInfo pbneNearbySearchInfo = new NearbySearchInfo();
            String strKws = json.getString("keywords");
            if (TextUtils.isEmpty(strKws)) {
                return false;
            }
            pbneNearbySearchInfo.strKeywords = strKws;
            pbneNearbySearchInfo.strCenterPoi = "ON_WAY";
            if (isBussinessName(strKws)) {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                        UiMap.SUBEVENT_MAP_NAVIGATE_BUSSINESS, pbneNearbySearchInfo);
            } else {
                JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
                        UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbneNearbySearchInfo);
            }

			return true;
		}else if ("delPass".equals(strAction)) {
			//删除途经点
			
			NavigateInfo pbNavigateInfo = new NavigateInfo();
			pbNavigateInfo.strTargetName = json.getString("keywords");
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
					UiMap.SUBEVENT_MAP_NAVIGATE_PASS_DEL_BY_NAME, pbNavigateInfo);
			return true;
		}else if ("firmSearch".equals(strAction)) {
			//支持XXX的经销商/维修站说法
			UiMap.NearbySearchInfo pbneNearbySearchInfo = new NearbySearchInfo();
			String strKws = json.getString("keywords");
			if(TextUtils.isEmpty(strKws)){
				return false;
			}
			pbneNearbySearchInfo.strKeywords = strKws;
			pbneNearbySearchInfo.strCenterPoi = json.getString("centerPoi");
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
					UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbneNearbySearchInfo);
			return true;
		} else if ("openHst".equals(strAction)) {
			NavManager.getInstance().queryLocalHistory();
			// speakWords(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),
			// false);
			return true;
		}
        parseNotSupportOperateWithAnswer(rawVoice, json);
        return true;
	}

	private boolean navigateToTargetName(String strTarget, String strCity, String region,
			String strPass, VoiceParseData rawVoice) {
		UiMap.NavigateInfo pbNavigateInfo = new NavigateInfo();
		pbNavigateInfo.strTargetName = strTarget;
		pbNavigateInfo.strTargetCity = strCity;
		pbNavigateInfo.strRegion = region;
        pbNavigateInfo.strTextData = rawVoice.strVoiceData.getBytes();
		if (!TextUtils.isEmpty(strPass))
			pbNavigateInfo.strPassByName = strPass;
		navigateToTarget(pbNavigateInfo, rawVoice);
		return true;
	}

	private boolean navigateToTarget(NavigateInfo pbNavigateInfo,
			VoiceParseData rawVoice) {
		if (TextUtils.isEmpty(pbNavigateInfo.strTargetCity)) {
			Vector<String> vecOut = new Vector<String>();
			if (!vecOut.isEmpty()) {
				if ("_jia1_".equals(vecOut.get(0))
						|| "_jia1__li3_".equals(vecOut.get(0))) {
					navigateHome(rawVoice);
					return true;
				}
				if ("_gong1__si1_".equals(vecOut.get(0))
						|| "_gong1__shi1_".equals(vecOut.get(0))
						|| "_dan1__wei4_".equals(vecOut.get(0))) {
					navigateCompany(rawVoice);
					return true;
				}
			}
		}
		mJsonDrive.put("action", "search");
		if (!TextUtils.isEmpty(pbNavigateInfo.strTargetCity)) {
			mJsonDrive.put("city", pbNavigateInfo.strTargetCity);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strCountry)) {
			mJsonDrive.put("country", pbNavigateInfo.strCountry);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strProvince)) {
			mJsonDrive.put("province", pbNavigateInfo.strProvince);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strRegion)) {
			mJsonDrive.put("region", pbNavigateInfo.strRegion);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strArea)) {
			mJsonDrive.put("area", pbNavigateInfo.strArea);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strStreet)) {
			mJsonDrive.put("street", pbNavigateInfo.strStreet);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strTargetName)) {
			mJsonDrive.put("keywords", pbNavigateInfo.strTargetName);
		}
		if (!TextUtils.isEmpty(pbNavigateInfo.strTargetAddress)) {
			mJsonDrive.put("addr", pbNavigateInfo.strTargetAddress);
		}

		if (useDriveRemoteSenceTool(rawVoice)) {
			return true;
		}

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_BY_NAME, pbNavigateInfo);
		return true;

	}

	private boolean isBussinessName(String strName) {

		return NavManager.getInstance().isBussinessName(strName);
	}

	private boolean isNearbyName(String strName) {

		return NavManager.getInstance().isNearbyName(strName);
	}

	private String isNearbyOrBussinessAction(String strName,
			String strNamePinyinWithDiao) {

		if (mapActions.isEmpty()) {
			JSONObject jsonNearActions = JSONObject.parseObject(NativeData
					.getResJson("SEARCH_ACTION_CONVER_LIST"));
			if (jsonNearActions != null) {
				Set<String> keySet = jsonNearActions.keySet();
				String[] keyArray = (String[]) keySet.toArray(new String[keySet
						.size()]);
				for (int i = 0; i < keyArray.length; i++) {
					String szName = jsonNearActions.getString(keyArray[i]);
					if (TextUtils.isEmpty(szName) || szName.charAt(0) == '\0') {
						continue;
					}
					mapActions.put(keyArray[i], szName);
				}
			}
		}
		if (mapActions.containsKey(strName)) {
			return mapActions.get(strName);
		} else {
			return strName;
		}

	}

	private boolean navigateBussiness(String strName, int radius,
			String strCenterCity, String region,String strCenterPoi, VoiceParseData rawVoice) {
		mJsonDrive.put("action", "bussiness");
		mJsonDrive.put("keywords", strName);
		mJsonDrive.put("center_city", strCenterCity);
		mJsonDrive.put("center_poi", strCenterPoi);

		if (radius > 0) {
			mJsonDrive.put("radius", radius);
		}

		if (useDriveRemoteSenceTool(rawVoice)) {
			return true;
		}

		UiMap.NearbySearchInfo pbneNearbySearchInfo = new NearbySearchInfo();
		pbneNearbySearchInfo.strKeywords = strName;
		pbneNearbySearchInfo.strCenterCity = strCenterCity;
		pbneNearbySearchInfo.strCenterPoi = strCenterPoi;
		pbneNearbySearchInfo.strRegion=region;
        pbneNearbySearchInfo.strTextData = rawVoice.strVoiceData.getBytes();
		if (radius > 0) {
			pbneNearbySearchInfo.uint32Radius = radius;
		}

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_BUSSINESS, pbneNearbySearchInfo);

		return true;
	}

	private boolean navigateNearBy(String strName, int radius,
			String strCenterCity, String strRegion,String strCenterPoi, VoiceParseData rawVoice) {
		mJsonDrive.put("action", "nearby");
		mJsonDrive.put("keywords", strName);
		mJsonDrive.put("center_city", strCenterCity);
		mJsonDrive.put("center_poi", strCenterPoi);
		if (radius > 0) {
			mJsonDrive.put("radius", radius);
		}
		if (useDriveRemoteSenceTool(rawVoice)) {
			return true;
		}
		UiMap.NearbySearchInfo pbNearbySearchInfo = new NearbySearchInfo();
		pbNearbySearchInfo.strKeywords = strName;
		pbNearbySearchInfo.strCenterCity = strCenterCity;
		pbNearbySearchInfo.strCenterPoi = strCenterPoi;
		pbNearbySearchInfo.strRegion=strRegion;
        pbNearbySearchInfo.strTextData = rawVoice.strVoiceData.getBytes();
		if (radius > 0) {
			pbNearbySearchInfo.uint32Radius = radius;
		}

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbNearbySearchInfo);
		return true;
	}

	private boolean navigateCompany(VoiceParseData rawVoice,
			NavigateInfo pbNavigateInfo) {
		mJsonDrive.put("action", "company");
		mJsonDrive.put("name", pbNavigateInfo.strTargetName);
		if (useDriveRemoteSenceTool(rawVoice)) {
			return true;
		}

		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_COMPANY, pbNavigateInfo);

		return true;
	}

	private boolean navigateHome(NavigateInfo pbNavigateInfo,
			VoiceParseData rawVoice) {
		mJsonDrive.put("action", "home");
		mJsonDrive.put("name", pbNavigateInfo.strTargetName);

		if (useDriveRemoteSenceTool(rawVoice)) {
			return true;
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_HOME, pbNavigateInfo);
		return true;
	}

	private boolean navigateCompany(VoiceParseData rawVoice) {
		NavigateInfo pbNavigateInfo = new NavigateInfo();
		pbNavigateInfo.strTargetName = "";
		return navigateCompany(rawVoice, pbNavigateInfo);
	}

	private boolean navigateHome(VoiceParseData rawVoice) {
		UiMap.NavigateInfo pbNavigateInfo = new NavigateInfo();
		pbNavigateInfo.strTargetName = "";
		return navigateHome(pbNavigateInfo, rawVoice);
	}

	private boolean useMusicRemoteSenceTool(VoiceParseData rawVoice) {
		JSONObject json = mJsonMusic;
		json.put("scene", "music");
		json.put("text", rawVoice.strText);
		json.put("score", mConfidence);
		String strSenceData = json.toString();

		if (procSenceByRemote("music", strSenceData)) {
			return true;
		}
		byte[] byteData = NativeData.getUIData(UiData.DATA_ID_REMOTE_PROC_TOOL,
				"music".getBytes());
		String strData = null;
		if (byteData != null) {
			strData = new String(byteData);
		}

		if (TextUtils.isEmpty(strData)) {
			byteData = NativeData.getUIData(UiData.DATA_ID_CHECK_APP_EXIST,
					"com.txznet.music".getBytes());
			if (byteData != null) {
				strData = new String(byteData);
			}
			if (TextUtils.isEmpty(strData)) {
				String strWords = NativeData
						.getResString("RS_VOICE_NO_MUSIC_TOOL");
				speakWords(strWords, true);
				return true;
			}
		}
		return false;
	}

	private boolean useDriveRemoteSenceTool(VoiceParseData rawVoice) {
		JSONObject json = mJsonDrive;
		json.put("scene", "nav");
		json.put("text", rawVoice.strText);
		json.put("score", mConfidence);
		String strSenceData = json.toString();

        if (procSenceByRemote("nav", strSenceData)) {
            return true;
        }
        final String navDisableReason = NavManager.getInstance().getDisableResaon();
        if (!TextUtils.isEmpty(navDisableReason)) { // 没有导航工具
            if (NavManager.getInstance().preInvokeWhenNavNotExists(new Runnable() {
                @Override
                public void run() {
                    speakWords(navDisableReason, true);
                }
            })){
                AsrManager.getInstance().setNeedCloseRecord(true);
            }
            return true;
        }
        return false;
    }

	private boolean parseCall(VoiceParseData rawVoice, JSONObject json,
			String strScene, String strAction) {
		JSONArray jsonList = new JSONArray();
		String strName = json.getString("name");
		String strNumber = json.getString("number");
		String strType = json.getString("type");
		jsonList = json.getJSONArray("list");
		mJsonCall.put("name", strName);
		mJsonCall.put("number", strNumber);
		mJsonCall.put("type", strType);

        boolean fuzzy = json.getBooleanValue("fuzzy");
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_CALL))
            return true;
        if (fuzzy) {
			MobileContacts result = new MobileContacts();
			List<MobileContact> mobileContactList =  new ArrayList<MobileContact>();
			if (!TextUtils.isEmpty(strName)) {
				MobileContacts contactsByName = findContacts(json);
				if (contactsByName != null && contactsByName.cons != null && contactsByName.cons.length > 0) {
					for (int i = 0; i < contactsByName.cons.length; i++) {
						ListIterator<MobileContact> mobileContactListIterator =
								mobileContactList.listIterator();
						boolean needAdd = true;
						while (mobileContactListIterator.hasNext()) {
							MobileContact mobileContact = mobileContactListIterator.next();

							if (TextUtils.equals(contactsByName.cons[i].name, mobileContact.name)
									&& Arrays.equals(contactsByName.cons[i].phones, mobileContact.phones)) {
								needAdd = false;
								break;
							}
						}
						if (needAdd) {
							mobileContactListIterator.add(contactsByName.cons[i]);
						}
					}
				}
			}
			MobileContacts contactsByNumber = null;
			if (!TextUtils.isEmpty(strName) && TextUtils.isDigitsOnly(strName)) {
				contactsByNumber = NativeData.findContactsByNumber(strName);
			}
			if (contactsByNumber != null && contactsByNumber.cons != null && contactsByNumber.cons.length > 0) {
				for (int i = 0; i < contactsByNumber.cons.length; i++) {
					ListIterator<MobileContact> mobileContactListIterator =
							mobileContactList.listIterator();
					boolean needAdd = true;
					while (mobileContactListIterator.hasNext()) {
						MobileContact mobileContact = mobileContactListIterator.next();
						if (TextUtils.equals(contactsByNumber.cons[i].name, mobileContact.name)
								&& Arrays.equals(contactsByNumber.cons[i].phones, mobileContact.phones)) {
							needAdd = false;
							break;
						}
					}
					if (needAdd) {
						mobileContactListIterator.add(contactsByNumber.cons[i]);
					}
				}
			}

			if (mobileContactList.size() > 0) {
				result.cons = mobileContactList.toArray(new MobileContact[mobileContactList.size()]);
				makeCall(result, 4, strType, rawVoice);
			} else if (!TextUtils.isEmpty(strName) && TextUtils.isDigitsOnly(strName)) {
				if (useCallRemoteSenceTool(rawVoice)) {
					return true;
				}
				MobileContact con = new MobileContact();
				con.phones = new String[1];
				con.phones[0] = strName;
				con.score = 10000;
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
						UiMakecall.SUBEVENT_MAKE_CALL_NUMBER, con);
			} else {
				findNoneContact("", rawVoice);
			}
			return true;
		}
        if (jsonList == null || jsonList.isEmpty()) {// 呼叫列表为空
            if (TextUtils.isEmpty(strNumber)) {// 号码为空
                if (TextUtils.isEmpty(strName)) {// 姓名为空
                    CmdManager.getInstance().onUI_TAB_CALL();
                    return true;
                } else {
                    HelpHitTispUtil.getInstance().hitCallByNameTips();
                    ContactData.MobileContacts pobjContacts = findContacts(json);// 根据姓名来查联系人
                    makeCall(pobjContacts, 4, strType, rawVoice);// 拨打电话
                }
            } else {
                if (TextUtils.isEmpty(strName)) {// 有number没有name直接拨打
                    HelpHitTispUtil.getInstance().hitCallByNumberTips();
                    makeNumberCall(strNumber, strName, rawVoice);
                } else {// name不为空，先进行搜索
                    ContactData.MobileContacts pobjContacts = findContacts(json);
                    if (pobjContacts != null && pobjContacts.cons != null
                            && pobjContacts.cons.length != 0) {
                        makeCall(pobjContacts, 4, strType, rawVoice);
                    } else {
                        if (enableServiceContacts()) {// 是否可以使用黄页
                            makeNumberCall(strNumber, strName, rawVoice);
                        } else {
                            findNoneContact("", rawVoice);
                        }
                    }
                }
            }
        } else {
            MobileContacts pbMobileContacts = new MobileContacts();
            ArrayList<MobileContact> mobConList = new ArrayList<ContactData.MobileContact>();
            for (int i = 0; i < jsonList.size(); i++) {
                MobileContact pcon = new MobileContact();
                if (jsonList.getJSONObject(i) != null
                        && !jsonList.getJSONObject(i).isEmpty()) {
                    JSONArray jsonNums = jsonList.getJSONObject(i)
                            .getJSONArray("number");
                    if (jsonNums == null || jsonNums.isEmpty()) {
                        continue;
                    }
                    pcon.name = jsonList.getJSONObject(i).getString("name");
                    pcon.score = jsonList.getJSONObject(i).getInteger("score");
                    pcon.phones = new String[jsonNums.size()];
                    for (int j = 0; j < jsonNums.size(); j++) {
                        pcon.phones[j] = jsonNums.getString(j);
                    }
                }
                mobConList.add(pcon);
            }
            pbMobileContacts.cons = (MobileContact[]) mobConList
                    .toArray(new MobileContact[mobConList.size()]);
            makeCall(pbMobileContacts, 4, json.getString("type"), rawVoice);
        }
        return true;
    }

    private com.txz.ui.contact.ContactData.MobileContacts findContacts(JSONObject json) {
        String name = json.getString("name");
        int mold = json.getIntValue("mold");
        String strMobiletype = json.getString("p");
        int searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_SIMILAR;
        int mobileType = com.txz.ui.contact.ContactData.QUERY_MOBILE_TYPE_UNKNOW;
        byte[] prefix = null;
        byte[] suffix = null;
        if (mold == TextResultHandle.CALL_LOCAL_YZS_CH) {
            searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_ALIAS;
            String strPrefix = json.getString("prefix");
            if (!TextUtils.isEmpty(strPrefix)) {
                prefix = strPrefix.getBytes();
            }
            String strSuffix = json.getString("suffix");
            if (!TextUtils.isEmpty(strSuffix)) {
                suffix = strSuffix.getBytes();
            }
        } else if (mold == TextResultHandle.CALL_LOCAL_YZS_EN) {
            searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_ENGLISH;
            String strPrefix = json.getString("prefix");
            if (!TextUtils.isEmpty(strPrefix)) {
                prefix = strPrefix.getBytes();
            }
            String strSuffix = json.getString("suffix");
            if (!TextUtils.isEmpty(strSuffix)) {
                suffix = strSuffix.getBytes();
            }
        } else if (mold == TextResultHandle.CALL_NET_YZS) {
            if (name.matches("^[a-zA-Z0-9\\s?]+$")) {
				searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_ENGLISH;
			}else {
				searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_SIMILAR;
			}
		}else if (mold == TextResultHandle.CALL_NET_IFLY) {
            if (name.matches("^[a-zA-Z0-9\\s?]+$")) {
                searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_ENGLISH;
            } else {
                searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_SIMILAR;
            }
        } else {
            if (name.matches("^[a-zA-Z\\s?]+$")) {
				searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_ENGLISH;
			}else {
				searchType = com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_SIMILAR;
			}
		}
		if (TextUtils.equals("z", strMobiletype)) {
			mobileType = com.txz.ui.contact.ContactData.QUERY_MOBILE_TYPE_TELEPHONE;
		}else if (TextUtils.equals("m", strMobiletype)) {
			mobileType = com.txz.ui.contact.ContactData.QUERY_MOBILE_TYPE_MOBILE;
		}else if (TextUtils.equals("s", strMobiletype)) {
			mobileType = com.txz.ui.contact.ContactData.QUERY_MOBILE_TYPE_SHORT;
		}
		return NativeData.findContactsByName(name,prefix,suffix,searchType,mobileType);// 根据姓名来查联系人
	}

	private void makeNumberCall(String strNumber, String strName,
			VoiceParseData rawVoice) {
		mJsonCall.put("number", strNumber);
		if (strName != null) {
			mJsonCall.put("name", strName);
		}
		if (useCallRemoteSenceTool(rawVoice)) {
			return;
		}
		MobileContacts pobjContacts = NativeData
				.findContactsByNumber(strNumber);
		if (pobjContacts != null && pobjContacts.cons.length > 0) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_MAKE_CALL_NUMBER_DIRECT,
					pobjContacts.cons[0]);
		} else {
			MobileContact con = new MobileContact();
			con.phones = new String[1];
			if (strName != null) {
				con.name = strName;
			}
			con.phones[0] = strNumber;
			con.score = 10000;
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
					UiMakecall.SUBEVENT_MAKE_CALL_NUMBER, con);
		}
		return;
	}

	private boolean makeCall(MobileContacts pobjContacts, int type,
			String strPhoneType, VoiceParseData rawVoice) {
		if (!RecorderWin.isOpened())
			return true;
		boolean bCandidate = false;// 是否进行候选拨号
		if (pobjContacts != null && pobjContacts.cons.length != 0) {
			if (!TextUtils.isEmpty(strPhoneType)) {
				mJsonCall.put("type", strPhoneType);
			}

			JSONObject json = mJsonCall;
			for (int j = 0; j < pobjContacts.cons.length; j++) {
				JSONObject v = new JSONObject();
				if (pobjContacts.cons[j] != null) {
					String name = pobjContacts.cons[j].name;
					int score = pobjContacts.cons[j].score;
					String[] phones = pobjContacts.cons[j].phones;
					v.put("score", score);
					v.put("name", name);
					v.put("number", phones);// 可能会出问题
					if (pobjContacts.cons[j].phones!=null) {
                        for (int i = 0; i < phones.length; i++) {
                            if (TextUtils.equals(phones[i], "empty")) {
                                pobjContacts.cons[j].phones[i] = "";
                            }
                        }
                    }
				}
				if (json.getJSONArray("list") == null) {
					json.put("list", new JSONArray());
				}
				json.getJSONArray("list").add(v);
			}
			// 分析候选人分值，确定候选方案
			if (pobjContacts.cons.length > 1) {
				if ((pobjContacts.cons[0].score >= 9999 && pobjContacts.cons[1].score < 9500)
						|| (pobjContacts.cons[0].score > 8000 && pobjContacts.cons[1].score < pobjContacts.cons[0].score * 0.7)) {
					JNIHelper.logd("find perfect socre contact "
							+ pobjContacts.cons[0].name + " "
							+ pobjContacts.cons[0].score);
					bCandidate = true;
				}

				if (bCandidate) {// 只保留一个结果
					MobileContacts objContacts = new MobileContacts();// 暂存
					objContacts.cons = new MobileContact[1];
					objContacts.cons[0] = pobjContacts.cons[0];
					pobjContacts = objContacts;
				}
                // 结果清理
                ArrayList<MobileContact> conList = new ArrayList<ContactData.MobileContact>();
                int j = 0;
                for (int k = 0; k < pobjContacts.cons.length  // 最多存在getMaxShowContactCount个候补结果，且结果大于2500分，只对满足前面条件的进行判断
                        && pobjContacts.cons[k].score > 2500; k++) {
                    if (k > 0
                            && (pobjContacts.cons[k - 1].score // 跳跃判断，分差超过2000分且跳跃大于20%
                            - pobjContacts.cons[k].score >= 2000 || pobjContacts.cons[k - 1].score * 0.8 >= pobjContacts.cons[k].score)) {
                        break;
                    }

                    j += pobjContacts.cons[k].phones.length;
                    if (j > ProjectCfg.getMaxShowContactCount()) {
                        pobjContacts.cons[k].phones = Arrays.copyOfRange(pobjContacts.cons[k].phones,0,pobjContacts.cons[k].phones.length - (j - ProjectCfg.getMaxShowContactCount()));
                        conList.add(pobjContacts.cons[k]);
                        break;
                    } else if (j == ProjectCfg.getMaxShowContactCount()){
                        conList.add(pobjContacts.cons[k]);
                        break;
                    } else {
                        conList.add(pobjContacts.cons[k]);
                    }

                }
				MobileContact[] contactArray = (MobileContact[]) conList
						.toArray(new MobileContact[conList.size()]);
				pobjContacts.cons = contactArray;
			}

			if (useCallRemoteSenceTool(rawVoice)) {
				return true;
			}
		}
		// 呼叫联系人
		if (pobjContacts == null || pobjContacts.cons.length <= 0) {
			// 语音提示，没有找到相关联系人，请再说一遍
			if (type == 4) {
				findNoneContact("", rawVoice);
			} else {
				findNoneContact(strPhoneType, rawVoice);
			}
			return false;
		} else if (pobjContacts.cons.length == 1) {// 只有一个被叫号码
			if (pobjContacts.cons[0].score > 8000 && mConfidence > 80) {// 直接呼叫门槛
				if (pobjContacts.cons[0].phones.length == 1) {// 直接呼叫只有一个号码
					JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
							UiMakecall.SUBEVENT_MAKE_CALL_DIRECT,
							pobjContacts.cons[0]);

				} else {
					JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
							UiMakecall.SUBEVENT_MAKE_CALL_LIST_NUMBER,
							pobjContacts.cons[0]);
				}
			} else {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
						UiMakecall.SUBEVENT_MAKE_CALL_CHECK,
						pobjContacts.cons[0]);
			}
		} else if (pobjContacts.cons.length > 0) {
			if (bCandidate) {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
						UiMakecall.SUBEVENT_MAKE_CALL_CANDIDATE, pobjContacts);
			} else {
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_CALL,
						UiMakecall.SUBEVENT_MAKE_CALL_LIST, pobjContacts);
			}
		}
		return true;
	}

	private void findNoneContact(String strPhoneType, VoiceParseData rawVoice) {
		if (!TextUtils.isEmpty(strPhoneType)) {
			mJsonCall.put("type", strPhoneType);
		}
		if (useCallRemoteSenceTool(rawVoice)) {
			return;
		}
		if (TextUtils.isEmpty(strPhoneType)) {
			String str = NativeData.getResString("RS_VOICE_CONTACT_NOT_FOUND");
			// 没有找到联系人不关闭界面
			speakWords(str, false);
		} else {
			String str = NativeData.getResString(
					"RS_VOICE_CONTACT_PHONE_NOT_FOUND");
			if(strPhoneType != null){
				str = str.replace("%PHONE%", strPhoneType);
			}
			// 没有找到联系人不关闭界面
			speakWords(str, false);
		}
	}

	private boolean enableServiceContacts() {
		byte[] byteData = ("" + ContactManager.getInstance().mEnableServiceContact)
				.getBytes();
		String strData = null;
		if (byteData != null) {
			strData = new String(byteData);
		}
		if ("true".equals(strData)) {
			return true;
		}
		JNIHelper.logw("disabled service contacts");
		return false;
	}

	private boolean useCallRemoteSenceTool(VoiceParseData rawVoice) {
		com.alibaba.fastjson.JSONObject json = mJsonCall;
		json.put("scene", "call");
		json.put("action", "make");
		json.put("text", rawVoice.strText);
		json.put("score", mConfidence);
		String strSenceData = json.toString();
		if (procSenceByRemote("call", strSenceData)) {
			return true;
		}
		if (checkSenceEnable("call") == false) {
			return true;
		}

		return false;
	}

	private boolean parseApp(VoiceParseData rawVoice,
			JSONObject json, String strScene,
			String strAction) {
		if (procSenceByRemote("app", json.toString())) {
			return true;
		}
		
		boolean isOpen = false;
		String action = null;
		action = json.getString("action");
		if ("open".equals(action)) {
			isOpen = true;
		} else if ("close".equals(action)) {
			isOpen = false;
		} else {
            parseNotSupportOperateWithAnswer(rawVoice, json);
            JNIHelper.logd("parseApp action:" + action);
            return true;
		}

		String strName = null;
		String strPackage = null;
		strName = json.getString("name");
		strPackage = json.getString("package");
		if (TextUtils.isEmpty(strName)) {
			return false;
		}
		if (TextUtils.isEmpty(strPackage)) {
			
			UiApp.AppInfo pobjApp = NativeData.findAppInfoByName(strName);
			if (pobjApp != null && !TextUtils.isEmpty(pobjApp.strPackageName)) {
				JNIHelper.logd("parseApp AppInfo:" + pobjApp.toString());
				if (isOpen) {
					openApp(pobjApp, json.toString());
				} else {
					closeApp(pobjApp, json.toString());
				}
				return true;
			}
            parseNotSupportOperateWithAnswer(rawVoice, json);
            JNIHelper.logd("parseApp AppInfo:" + pobjApp);
            return true;

		}
		UiApp.AppInfo appInfo = new UiApp.AppInfo();
		appInfo.strAppName = strName;
		appInfo.strPackageName = strPackage;

		if (isOpen) {
			openApp(appInfo, json.toString());
		} else {
			closeApp(appInfo, json.toString());
		}
		return true;
	}

	private void openApp(AppInfo appInfo, String json) {
		if (procSenceByRemote("app", json)) {
			return;
		}
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_OPEN))
			return ;
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_APP, UiApp.SUBEVENT_OPEN_APP,
				appInfo);

	}

	private void closeApp(AppInfo appInfo, String json) {
		if (procSenceByRemote("app", json)) {
			return;
		}
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_APP, UiApp.SUBEVENT_CLOSE_APP,
				appInfo);
	}

	private boolean parseCommand(VoiceParseData rawVoice,
			com.alibaba.fastjson.JSONObject json, String strScene,
			String strAction) {
		if (procSenceByRemote("command", json.toString())) {
			return true;
		}
        String rCmd = "";
        if(json.containsKey("cmd")){
            rCmd = json.getString("cmd");
        }
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType("command").setAction(strAction)
                .setSessionId().putExtra("cmd", rCmd).buildCommReport());
		if ("exec".equals(strAction)) {
			String strCmd = null;
			strCmd = json.getString("cmd");
			if ("vol_up".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_UP");
			} else if ("vol_down".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_DOWN");
			} else if ("vol_off".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_MUTE_ON");
			} else if ("vol_on".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_MUTE_OFF");
			} else if ("vol_max".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_MAX");
			} else if ("vol_min".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_VOL_MIN");
			} else if ("light_up".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_LIGHT_UP");
			} else if ("light_down".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_LIGHT_DOWN");
			} else if ("light_max".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_LIGHT_MAX");
			} else if ("light_min".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"FEEDBACK_CMD_LIGHT_MIN");
			} else if ("wifi_on".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"GLOBAL_CMD_OPEN_WIFI");
			} else if ("wifi_off".equals(strCmd)) {
				JNIHelper.sendEvent(UiEvent.EVENT_UI_BACK_GROUD_COMMAND, 0,
						"GLOBAL_CMD_CLOSE_WIFI");
			} else
				sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNSUPPORT, rawVoice);
			return true;
		} else if ("event".equals(strAction)) {
			if (!json.containsKey("event")) {
				sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNSUPPORT, rawVoice);
				return true;
			}
			int event = json.getIntValue("event");
			int subEvent = json.getIntValue("subEvent");
			String data = json.getString("cmd");
			int type = json.getIntValue("type");
             if (UiEvent.EVENT_SYSTEM_MUSIC == event && UiMusic.SUBEVENT_MEDIA_PLAY == subEvent) {
                HelpHitTispUtil.getInstance().hitSuiBianTingTips();
            } else if (data.toUpperCase().indexOf("VOL") != -1 || "音量".equals(data)) {
                HelpHitTispUtil.getInstance().hitUpdateVolumeTips();
            } else if (data.toUpperCase().indexOf("WIFI") != -1) {
                HelpHitTispUtil.getInstance().hitUpdateWifiStatusTips();
            } else if (data.toUpperCase().indexOf("LIGHT") != -1 || "亮度".equals(data)) {
                HelpHitTispUtil.getInstance().hitUpdateLightTips();
            }
            if (type == UiData.DATA_ID_TEST_WAKEUP
                    || type == UiData.DATA_ID_TEST_REMOTE_KEYWORD
                    || type == UiData.DATA_ID_TEST_LOCAL_KEYWORD) {
                if (!((ChoiceManager.getInstance().isCoexistAsrAndWakeup()||InterruptTts.getInstance().isInterruptTTS())&&ChoiceManager.getInstance().isSelecting())) {
                    JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
                }
            }
			if (type == UiData.DATA_ID_TEST_REMOTE_KEYWORD) {
				String rmtData = json.getString("data");
				String serviceName = json.getString("service");
				VoiceData.RmtCmdInfo rmtInfo = new VoiceData.RmtCmdInfo();
				rmtInfo.rmtCmd = data;
				rmtInfo.rmtData = rmtData;
				rmtInfo.rmtServName = serviceName;
				JNIHelper.sendEvent(event, subEvent,rmtInfo);
			}
			else if (json.containsKey("voice") && json.containsKey("string")) {
				String voice = json.getString("voice");
				String text = json.getString("string");
				VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
				cmdWord.cmdData = data;
				cmdWord.stringData = text;
				cmdWord.voiceData = voice;
				JNIHelper.sendEvent(event, subEvent,cmdWord);
			}
			else if (json.containsKey("voiceData") && json.containsKey("text")) {
				String voiceData = json.getString("voiceData");
				String text = json.getString("text");
				VoiceData.CmdWord cmdWord = new VoiceData.CmdWord();
				cmdWord.cmdData = data;
				cmdWord.stringData = text;
				cmdWord.voiceData = voiceData;
				JNIHelper.sendEvent(event, subEvent,cmdWord);
			} else
				JNIHelper.sendEvent(event, subEvent, data);
			return true;
		} else if ("exeu".equals(strAction)) {
			String cmd = json.getString("cmd");
			byte[] result = NativeData.getNativeData(UiData.DATA_ID_TEST_WHOLE_INTERACTION_KEYWORD, cmd);
			if (new String(result).equals("0")) {
				sendCommonResult(VoiceData.COMMON_RESULT_TYPE_UNSUPPORT, rawVoice);
			}
			return true;
		}
		JNIHelper.logd("parseCommand strAction:" + strAction);
        parseNotSupportOperateWithAnswer(rawVoice, json);
        return true;
	}

	/**
	 * 设置唤醒词
	 * 
	 * @param rawVoice
	 * @param json
	 * @param strScene
	 * @param strAction
	 * @return
	 */
	private boolean parseSetUserWakeupKeywords(VoiceParseData rawVoice,
			com.alibaba.fastjson.JSONObject json, String strScene,
			String strAction) {
		if (procSenceByRemote("set_user_wakeup_keywords", json.toString())) {
			return true;
		}
		// 省略了style 测试时看
		String keywords = null;
		keywords = json.getString("keywords");
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SET_WAKEUP_KEYWORDS, keywords);
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_COMMAND_SENCE);
		String str = NativeData
				.getResString("RS_VOICE_SET_WAKEUP_KEYWORDS_FEEDBACK");
		String dis = NativeData.getResString("RS_VOICE_SET_WAKEUP_KEYWORDS_FEEDBACK_DISPLAY");
		if(keywords != null){
			str = str.replace("%KW%", keywords);
			dis = dis.replace("%KW%", keywords);
			rawVoice.strText  = rawVoice.strText.replace(keywords.replace("你好",""),"");
		}
		WinHelpManager.getInstance().checkSpeakHelpTips(rawVoice.strText, false);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextNotEqualsDisplay(str, dis);
		return true;
	}

	private void setRawText(String strText, int confidence) {
		mConfidence = confidence;
		if (strText == null)
			strText = "";
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_SHOW_DEBUG_TEXT, confidence + ":"
						+ strText);
		RecorderWin.setLastUserText(strText);
        //在这一步就将状态设置成识别结束状态，解决场景拦截的情况，造成点击声控图标界面卡住的问题
        RecorderWin.setState(RecorderWin.STATE.STATE_END);
	}

	private boolean parseRawVoice(VoiceParseData pbRawVoice) {
		pbRawVoice.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		pbRawVoice.floatTextScore = TextResultHandle.TEXT_SCORE_INVALID;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE_NEW, pbRawVoice);
		return true;
	}

	private boolean procSenceByRemote(String sence, String data) {
		byte[] b_isProc = SenceManager.getInstance().procSenceByRemote(sence,
				data.getBytes());
		if (b_isProc == null)
			return false;
		boolean isProc = Boolean.parseBoolean(new String(b_isProc));
        LogUtil.logd(MusicManager.TAG+"INTERCEPT:"+sence+"/"+isProc);
        if(isProc){
            ReportUtil.doReport(new ReportUtil.Report.Builder().setType("procSence").setSessionId()
                    .putExtra("sence", sence).buildCommReport());
        }
        return isProc;
    }

}
