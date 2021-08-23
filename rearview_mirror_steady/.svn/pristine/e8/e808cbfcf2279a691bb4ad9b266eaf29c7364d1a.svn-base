package com.txznet.txz.component.text.ifly;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.TestResp;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.text.TextResultHandle;

import android.text.TextUtils;

public class IflyJsonConver {

	private String mJson;
	private String mRawText;
	private float mScore;
	private VoiceParseData mParseData;
	private String mScene;

	public IflyJsonConver(VoiceParseData parseData) {
		this.mParseData = parseData;
		mJson = JsonConver(parseData.strVoiceData);
	}

	public String getJson() {
		return mJson;
	}

	public String getRawText() {
		return mRawText;
	}

	public String getScene() {
		return mScene;
	}

	public float getScore() {
		return mScore;

	}

	private boolean parseCall(String service, String operation, JSONObject src,
			JSONObject des) throws JSONException {

		if (TextUtils.equals(service, "telephone")) {
			if (TextUtils.equals(operation, "CALL")) {
				mScore = TextResultHandle.TEXT_SCORE_MIDDLE;
				des.put("scene", "call");
				des.put("action", "make");
				des.put("mold", TextResultHandle.CALL_NET_IFLY);
				mScene = "call";

				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");

						if (slots.has("category")) {
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
							des.put("type", slots.getString("category"));
						}
						
						if (slots.has("code")) {
							String number = slots.getString("code");
							des.put("number", number);
							if (mRawText.indexOf(slots.getString("code")) != -1
									&& mRawText
											.substring(
													mRawText.length()
															- number.length())
											.equals(number)) {
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
								return true;
							} else {
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
							}
						}

						if (slots.has("name")) {
							if (mRawText.indexOf(slots.getString("name")) == -1) {
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
							}
							else {
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
							}
							des.put("name", slots.getString("name"));
							//视为模糊语义
							if(!TextUtils.isEmpty(slots.getString("name")) && slots.getString("name").equals(mRawText)){
								des.put("fuzzy", true);
							}
							
						}
					}
				}
				return true;
			}
		}
		return false;

	}

	private boolean parseQA(String service, String operation, JSONObject src,
			JSONObject des) throws JSONException {

		if (TextUtils.equals(service, "openQA")
				|| TextUtils.equals(service, "faq")
				|| TextUtils.equals(service, "baike")
				|| TextUtils.equals(service, "datetime")
				|| TextUtils.equals(service, "chat")
				|| TextUtils.equals(service, "calc")

		) {
			if (TextUtils.equals(operation, "ANSWER")) {
				if (src.has("answer")) {
					JSONObject answerJo = src.getJSONObject("answer");
					if (answerJo.has("type")) {
						if (TextUtils.equals("T", answerJo.getString("type"))) {
							String answer = answerJo.getString("text");

							if (TextUtils.equals(service, "openQA")
									&& answer.length() > 4
									&& TextUtils.equals(answer.substring(0, 4),
											"TXZ_")) {
								des.put("scene", "command");
								des.put("action", "exec");
								des.put("cmd", answer.substring(4));
								des.put("text", answer.substring(4));
								des.put("fuzzy", true);
								mScene = "command";
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_MORE;
								return true;
							}

							if (TextUtils.equals(service, "datetime")) {
								des.put("scene", "query");
								des.put("action", "date");
								mScene = "query";
								des.put("fuzzy", true);
								des.put("result", answer);
								mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
								return true;
							}

							if (TextUtils.equals(service, "openQA")
									|| TextUtils.equals(service, "faq")
									|| TextUtils.equals(service, "baike")
									|| TextUtils.equals(service, "chat")
									|| TextUtils.equals(service, "calc")) {
								String text = src.getString("text");
								if (text.contains("限行")) {
									mScore = TextResultHandle.TEXT_SCORE_LOW;
									des.put("scene", "limit_number");
									des.put("action", "query");
									des.put("text", text);
									des.put("result", answer);
									mScene = "limit_number";
									return true;
								}else if (text.contains("放假")){
									mScore = TextResultHandle.TEXT_SCORE_LOW;
								}else if (TextUtils.equals(service, "calc")) {
									mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
									des.put("style", "calculator");
								}else if (!TextUtils.isEmpty(answer) 
										&& (text.endsWith("怎么做")
												||text.endsWith("的做法")
												||text.endsWith("的烹饪方式")
												||text.endsWith("怎么煮"))) {
									mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
									des.put("style", "cookbook");
								}else
									mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
								des.put("scene", "unknown");
								des.put("action", "unknown");
								des.put("text", text);
								des.put("answer", answer);
								des.put("fuzzy", true);
								mScene = "unknown";
								return true;
							}

							des.put("scene", "unknown");
							des.put("action", "unknown");
							des.put("text", src.getString("text"));
							des.put("answer", answer);
							des.put("fuzzy", true);
							mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
							mScene = "unknown";
							return true;
						}
					}
				}
			}
			des.put("scene", "unknown");
			des.put("action", "unknown");
			des.put("fuzzy", true);
			des.put("text", src.getString("text"));
			mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
			mScene = "unknown";
			return true;
		} else {
			if (src.has("moreResults")) {
				JSONArray results = src.getJSONArray("moreResults");
				for (int i = 0; i < results.length(); i++) {
					JSONObject jo = results.getJSONObject(i);
					if (jo.has("service") && jo.has("operation")) {
						if (parseQA(jo.getString("service"),
								jo.getString("operation"), jo, des)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean parseNav(String service, String operation, JSONObject src,
			JSONObject des) throws JSONException {

		if (TextUtils.equals(service, "map")) {
			if (TextUtils.equals(operation, "ROUTE")) {
				des.put("scene", "nav");
				des.put("action", "search");
				mScene = "nav";

				mScore = TextResultHandle.TEXT_SCORE_MIDDLE;
				
				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");
						if (slots.has("endLoc")) {
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
							JSONObject endloc = slots.getJSONObject("endLoc");
							String strBasic = null;
							String strRegion = null;
							if (endloc.has("country")) {
								strBasic = endloc.getString("country");
							}
							if (endloc.has("province")) {
								strBasic = endloc.getString("province");
							}
							if (endloc.has("city")) {
								strBasic = endloc.getString("city");
								if (!strBasic.equals("CURRENT_CITY"))
									des.put("city", endloc.getString("city"));
								else
									des.put("city", "");
							}
							if (endloc.has("area")) {
								strBasic = endloc.getString("area");
								strRegion=endloc.getString("area");
							}
							if (endloc.has("street")) {
								strBasic = endloc.getString("street");
							}
							if (endloc.has("region")) {
								strBasic = endloc.getString("region");
								
							}

							if (endloc.has("type")) {
								String type = endloc.getString("type");

								if (TextUtils.equals(type, "LOC_POI")) {
									if (endloc.has("poi")) {
										String poi = endloc.getString("poi");
										des.put("keywords", poi);
									}
								} else if (TextUtils.equals(type, "LOC_BASIC")) {
									if (!TextUtils.isEmpty(strBasic)) {
										des.put("keywords", strBasic);
									}
								} else if (TextUtils.equals(type, "LOC_STREET")) {
									if (!TextUtils.isEmpty(strBasic)) {
										des.put("keywords", strBasic);
									}
								} else if (TextUtils.equals(type, "LOC_CROSS")) {
									String street = "";
									String streets = "";
									if (endloc.has("street")) {
										street = endloc.getString("street");
									}
									if (endloc.has("streets")) {
										streets = endloc.getString("streets");
									}
									des.put("keywords", street + streets);
								} else if (TextUtils.equals(type, "LOG_REGION")) {
									if (!TextUtils.isEmpty(strBasic)) {
										des.put("keywords", strBasic);
									}
								}
							}
							
							if (endloc.has("keywords")) {
								String keywords = endloc.getString("keywords");
								des.put("keywords", keywords);
							}
							des.put("region", strRegion);
							//视为模糊语义
							if(!TextUtils.isEmpty(des.getString("keywords")) && des.getString("keywords").equals(mRawText)){
								des.put("fuzzy", true);
							}

						}
					}
				}
			} else if (TextUtils.equals(operation, "POSITION")) {
				des.put("scene", "location");
				des.put("action", "query");
				mScene = "location";

				mScore = TextResultHandle.TEXT_SCORE_MIDDLE;
				
				if (getRawText().contains("地图")) {
					mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_MORE;
					return true;
				}

				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");
						if (slots.has("location")) {
							JSONObject location = slots
									.getJSONObject("location");

							if (location.has("city")) {
								des.put("city", location.getString("city"));
								mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
							}

							if (location.has("poi")) {
								des.put("poi", location.getString("poi"));
								mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_MORE;
							}
							
							if (mRawText.contains("在哪") || mRawText.contains("哪一家")) {
								mScore = TextResultHandle.TEXT_SCORE_LOW;
							}
							
							if(TextUtils.equals(location.getString("poi"), mRawText)){
								des.put("fuzzy", true);
							}
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	private boolean parseApp(String service, String operation, JSONObject src,
			JSONObject des) throws JSONException {
		if (TextUtils.equals(service, "app")) {
			des.put("scene", "app");
			mScene = "app";

			mScore = TextResultHandle.TEXT_SCORE_MIDDLE;
			if (TextUtils.equals(operation, "LAUNCH")) {
				des.put("action", "open");

				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");
						if (slots.has("name")) {
							des.put("name", slots.getString("name"));
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
							if(TextUtils.equals(slots.getString("name"), mRawText)){
								des.put("fuzzy", true);
							}
						}
					}
				}
				return true;
			} else if (TextUtils.equals(operation, "EXIT")) {
				des.put("action", "close");

				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");
						if (slots.has("name")) {
							des.put("name", slots.getString("name"));
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
							if(TextUtils.equals(slots.getString("name"), mRawText)){
								des.put("fuzzy", true);
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean parseMusic(String service, String operation,
			JSONObject src, JSONObject des) throws JSONException {
		if (TextUtils.equals(service, "music")) {
			des.put("scene", "music");
			mScene = "music";
			mScore = TextResultHandle.TEXT_SCORE_MIDDLE;

			if (TextUtils.equals(operation, "PLAY")) {
				des.put("action", "play");
			} else if (TextUtils.equals(operation, "SEARCH")) {
				des.put("action", "searchOnline");
			} else {
				des.put("action", "error");
				des.put("reason", "not support operate");
				mScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE_LITTLE;
				return true;
			}

			if (src.has("semantic")) {
				JSONObject semantic = src.getJSONObject("semantic");
				if (semantic.has("slots")) {
					JSONObject model = new JSONObject();
					JSONObject slots = semantic.getJSONObject("slots");
					if (slots.has("song")) {
						model.put("title", slots.getString("song"));
//						int songLen = slots.getString("song").length();
//						if (mRawText.length() > 5
//								&& songLen * 2 < mRawText.length())
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
//						else
//							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
						if (TextUtils.equals(slots.getString("song"), mRawText)) {
							des.put("fuzzy", true);
						}
					}

					if (slots.has("artist")) {
						model.put("artist",
								new JSONArray().put(slots.getString("artist")));
						mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
						if(TextUtils.equals(slots.getString("artist"), mRawText)){
							des.put("fuzzy", true);
						}
					}
					des.put("model", model);
				}
			}
			return true;
		}
		return false;
	}

	private boolean parseFM(String service, String operation, JSONObject src,
			JSONObject des) throws JSONException {
		if (TextUtils.equals(service, "radio")) {
			if (TextUtils.equals(operation, "LAUNCH")) {

				if ((!getRawText().matches(
						"^(?:.*?)(?:调频到|调频|调幅到|调幅|(?i)FM(?-i)|(?i)AM(?-i))(?:..+)(?:兆赫|千赫|赫兹)?$")
						&& (AudioManager.getInstance().isAudioToolSet() || AudioManager
								.getInstance().hasRemoteTool())) && ProjectCfg.isUseRadioAsAudio()) {
					des.put("scene", "audio");
					des.put("action", "play");
					mScene = "audio";
					mScore = TextResultHandle.TEXT_SCORE_MIDDLE;
					if (src.has("semantic")) {
						JSONObject semantic = src.getJSONObject("semantic");
						JSONObject model = new JSONObject();

						if (semantic.has("slots")) {
							JSONObject slots = semantic.getJSONObject("slots");
							if (slots.has("name")) {
								model.put("keywords", new JSONArray().put(slots
										.getString("name")));
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
								if(TextUtils.equals(slots.getString("name"), mRawText)){
									des.put("fuzzy", true);
								}
							}else if (slots.has("category")) {
								model.put("keywords", new JSONArray().put(slots
										.getString("category")));
								mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
								if(TextUtils.equals(slots.getString("category"), mRawText)){
									des.put("fuzzy", true);
								}
							}
						}
						des.put("model", model);
					}
					return true;
				}

				des.put("scene", "radio");
				des.put("action", "play");

				mScene = "radio";
				mScore = TextResultHandle.TEXT_SCORE_MIN;

				if (src.has("semantic")) {
					JSONObject semantic = src.getJSONObject("semantic");
					if (semantic.has("slots")) {
						JSONObject slots = semantic.getJSONObject("slots");

						if (slots.has("waveband")) {
							String waveband = slots.getString("waveband");
							if (TextUtils.equals(waveband, "fm")) {
								des.put("waveband", "fm");
								des.put("unit", "MHZ");
							} else if (TextUtils.equals(waveband, "am")) {
								des.put("waveband", "am");
								des.put("unit", "KHZ");
							}
						}

						if (slots.has("code")) {
							mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
							String code = slots.getString("code");
							String text = "调频";
							if (code.indexOf('.') != -1) {
								text = text + code.replace('.', '点');
							}
							else {
								text = text + code;
							}
							byte result[] = NativeData.getNativeData(UiData.DATA_ID_TEST_WHOLE_REMOTE_KEYWORD,text);
							try {
								TestResp resp;
								resp = TestResp.parseFrom(result);
								if (resp.success) {
									mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
									des.put("event",resp.event);
									des.put("subEvent", resp.subEvent);
//									des.put("cmd",new String(resp.data));
									VoiceData.RmtCmdInfo info = VoiceData.RmtCmdInfo.parseFrom(resp.data);
									des.put("data", info.rmtData);
									des.put("cmd", info.rmtCmd);
									des.put("service", info.rmtServName);
									des.put("scene","command");
									des.put("action", "event");
									des.put("type", UiData.DATA_ID_TEST_REMOTE_KEYWORD);
									return true;
								}
							} catch (InvalidProtocolBufferNanoException e) {}
							des.put("hz", code);
						}

					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean parseWeather(String service, String operation,
			JSONObject src, JSONObject des) throws JSONException {
		if (TextUtils.equals(service, "weather")) {
			 des.put("scene", "weather");
			 des.put("action", "query");
			 mScene = "weather";
			// mScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
			//
			// JSONObject semantic = src.getJSONObject("semantic");
			// if (semantic.has("slots")) {
			// JSONObject slots = semantic.getJSONObject("slots");
			//
			// if (slots.has("location")) {
			// JSONObject location = slots.getJSONObject("location");
			// if (!location.has("city")) {
			// des.put("city", location.getString("city"));
			// }
			// }
			//
			// if (slots.has("datetime")) {
			// JSONObject datetime = slots.getJSONObject("datetime");
			// if (datetime.has("date")) {
			// des.put("date", datetime.getString("date"));
			// }
			// }
			// }
//			des.put("scene", "unsupport");
//			des.put("action", "unsupport");
			mScore = TextResultHandle.TEXT_SCORE_MIN;
			return true;
		}
		return false;
	}

	private boolean parseStock(String service, String operation,
			JSONObject src, JSONObject des) throws JSONException {

		if (TextUtils.equals(service, "stock")) {
			 des.put("scene", "stock");
			 des.put("action", "query");
			 mScene = "stock";
			// mScore = TextResultHandle.TEXT_SCORE_LOW;
			//
			// JSONObject semantic = src.getJSONObject("semantic");
			// if (semantic.has("slots")) {
			// JSONObject slots = semantic.getJSONObject("slots");
			// if (slots.has("name")) {
			// des.put("name", slots.getString("name"));
			// }
			// }
//			des.put("scene", "unsupport");
//			des.put("action", "unsupport");
			mScore = TextResultHandle.TEXT_SCORE_MIN;
			return true;
		}
		return false;
	}

	private String JsonConver(String srcJson) {
		JSONObject des = null;
		try {
			JSONObject src = new JSONObject(srcJson);
			String text = src.getString("text");
			mRawText = text;

			des = new JSONObject();
			des.put("text", mRawText);

			if (!src.has("rc") || src.getInt("rc") != 0) {
				//MonitorUtil.monitorCumulant(NetAsrIflytekImpl.MONITOR_ERROR+NetAsrIflytekImpl.MONITOR_NO_NLP);
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
				mScore = TextResultHandle.TEXT_SCORE_LOW;
				return des.toString();
			}

			String service = src.getString("service");
			String operation = src.getString("operation");

			switch (mParseData.uint32Sence) {
			case VoiceData.GRAMMAR_SENCE_MAKE_CALL: {
				if (parseCall(service, operation, src, des)) {
					break;
				}
				if (parseQA(service, operation, src, des)) {
					break;
				}
				if (parseNav(service, operation, src, des)) {
					break;
				}
				if (parseApp(service, operation, src, des)) {
					break;
				}
				if (parseMusic(service, operation, src, des)) {
					break;
				}
				if (parseFM(service, operation, src, des)) {
					break;
				}
				if (parseWeather(service, operation, src, des)) {
					break;
				}
				if (parseStock(service, operation, src, des)) {
					break;
				}
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
				des.put("fuzzy", true);
				mScore = TextResultHandle.TEXT_SCORE_MIN;
				break;
			}
			case VoiceData.GRAMMAR_SENCE_NAVIGATE:
			case VoiceData.GRAMMAR_SENCE_SET_HOME:
			case VoiceData.GRAMMAR_SENCE_SET_COMPANY: {
				if (parseNav(service, operation, src, des)) {
					break;
				}
				if (parseQA(service, operation, src, des)) {
					break;
				}
				if (parseCall(service, operation, src, des)) {
					break;
				}
				if (parseApp(service, operation, src, des)) {
					break;
				}
				if (parseMusic(service, operation, src, des)) {
					break;
				}
				if (parseFM(service, operation, src, des)) {
					break;
				}
				if (parseWeather(service, operation, src, des)) {
					break;
				}
				if (parseStock(service, operation, src, des)) {
					break;
				}
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
				mScore = TextResultHandle.TEXT_SCORE_MIN;
				break;
			}
			default:
				if (parseQA(service, operation, src, des)) {
					break;
				}
				if (parseCall(service, operation, src, des)) {
					break;
				}
				if (parseApp(service, operation, src, des)) {
					break;
				}
				if (parseMusic(service, operation, src, des)) {
					break;
				}
				if (parseNav(service, operation, src, des)) {
					break;
				}
				if (parseFM(service, operation, src, des)) {
					break;
				}
				if (parseWeather(service, operation, src, des)) {
					break;
				}
				if (parseStock(service, operation, src, des)) {
					break;
				}
				mScene = "unknown";
				des.put("scene", "unknown");
				des.put("action", "unknown");
//				des.put("fuzzy", true);
				mScore = TextResultHandle.TEXT_SCORE_MIN;
				break;
			}

		} catch (JSONException e) {
			e.printStackTrace();
			if (des != null && des.has("text")) {
				mScore = TextResultHandle.TEXT_SCORE_LOW;
				return des.toString();
			}
			return null;
		}
		return des.toString();
	}
}
