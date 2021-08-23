package com.txznet.sdk;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherData;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZResourceManager.RecordWin.RecordStatus;
import com.txznet.sdk.TXZResourceManager.RecordWin.RecordWinOperateListener;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.bean.Poi;

import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 类名：语音资源管理器（英译中）
 * 类描述：语音默认UI及TTS资源不符合需求时，通过此类对语音资源进行替换
 *         包括：设置语音风格、替换语音反馈语、更改语音资源路径、自定义帮助界面、自定义语音界面、自定
 *         义语音可控弹窗、播报需要显示文本类TTS等
 */
public class TXZResourceManager {

	/**
	 * 默认语音反馈风格
	 */
	public final static String STYLE_DEFAULT = "";

	/**
	 * 宫廷语音风格
	 */
	public final static String STYLE_KING = "KING";

	private static TXZResourceManager sInstance = new TXZResourceManager();

	private TXZResourceManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return 类实例
	 */
	public static TXZResourceManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mVoiceStyle != null)
			setVoiceStyle(mVoiceStyle);
		if (mAllResourceFile != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.resource.replaceResourceFile",
					mAllResourceFile.getBytes(), null);
		}
		if (mAllResourceData != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.resource.replaceResource",
					mAllResourceData.getBytes(), null);
		}
		if (mUpdateResourceData != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.resource.updateResource",
					mUpdateResourceData.getBytes(), null);
		}
		if (mHasSetTool) {
			if (mRecordWin == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.win.clear", null, null);
			} else {
				LogUtil.logd("mHasSetHudRecordWin:" + mHasSetHudRecordWin);
				if(mHasSetHudRecordWin){
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
							"txz.record.win.prepare.hud", "true".getBytes(),
							null);
				}else {
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
							"txz.record.win.prepare.hud", "false".getBytes(),
							null);
				}
				setRecordWin(mRecordWin);
			}
		}
		if (mHasSetHelpWin) {
			setHelpWin(mHelpWin);
		}
		if (mJustText != null) {
			setRecordWin2PoiNoResultMsgType(mJustText);
		}
	}

	String mVoiceStyle = null;

	/**
	 *  方法名：设置语音交互风格
	 *  方法描述：更改语音交互风格，根据风格不同，语音交互TTS文本有变化。
	 *            参考语音资源文件res_string_zh-CN.json
	 *
	 * @param style 语音风格。语音默认包含以下两种：
	 *              - null 默认风格
	 *              - king 宫廷风
	 */
	public void setVoiceStyle(String style) {
		if (style == null)
			style = STYLE_DEFAULT;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.resource.setStyle", style.getBytes(), null);
	}

	public String mAllResourceFile = null;
	public String mAllResourceData = null;
	public String mUpdateResourceData = null;

	/**
	 *  方法名：加载语音资源文件（文件）
	 *  方法描述：通过此接口替换语音默认资源，主要包括语音反馈语等
	 *           替换方式为资源路径，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param path 已更改资源json文件路径
	 * @param all 是否是全量更新
	 *            true 全量更新
	 *            false 增量更新
	 */
	public void loadResourceFile(String path, boolean all) {
		if (all) {
			mAllResourceFile = path;
			mAllResourceData = null;
			mUpdateResourceData = null;
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.resource.replaceResourceFile",
					mAllResourceFile.getBytes(), null);
			return;
		}
		try {
			File f = new File(path);
			FileInputStream in = new FileInputStream(path);
			byte[] bs = new byte[(int) f.length()];
			in.read(bs);
			in.close();
			loadResourceData(new String(bs), all);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *  方法名：加载语音资源文件（json）
	 *  方法描述：通过此接口替换语音默认资源，主要包括语音反馈语等
	 *  		 替换方式为json，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param data json数据
	 * @param all 是否是全量更新
	 *            true 全量更新
	 *            false 增量更新
	 */
	public void loadResourceData(JSONObject data, boolean all) {
		loadResourceData(data.toString(), all);
	}

	/**
	 *  方法名：更新json对象里的数据
	 *  方法描述：从json对象中更新需要替换的json字段数据
	 *
	 * @param tar 源数据json
	 * @param data 需要更新的部分字段数据json
	 */
	private void updateJson(JSONObject tar, JSONObject data) {
		try {
			Iterator<String> it = data.keys();
			if (it == null)
				return;
			while (it.hasNext()) {
				String k = it.next();
				Object v = data.get(k);
				if (v instanceof JSONObject) {
					JSONObject n;
					if (tar.has(k)) {
						Object old = tar.get(k);
						if (!(old instanceof JSONObject)) {
							JSONObject t = new JSONObject();
							t.put("", old);
							tar.put(k, t);
						}
						n = tar.getJSONObject(k);
					} else {
						n = new JSONObject();
					}
					updateJson(n, (JSONObject) v);
					tar.put(k, n);
					continue;
				}
				tar.put(k, v);
			}
		} catch (Exception e) {
		}
	}

	/**
	 *  方法名：加载语音资源文件（文本）
	 *  方法描述：通过此接口替换语音默认资源
	 *  		 替换方式为文本字符串，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param data 文本字符串数据
	 * @param all 是否是全量更新
	 *            true 全量更新
	 *            false 增量更新
	 */
	public void loadResourceData(String data, boolean all) {
		if (all) {
			mAllResourceFile = null;
			mAllResourceData = data;
			mUpdateResourceData = null;
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.resource.replaceResource",
					mAllResourceData.getBytes(), null);
			return;
		}
		JSONBuilder jsonOld = new JSONBuilder(mUpdateResourceData);
		JSONBuilder jsonNew = new JSONBuilder(data);
		updateJson(jsonOld.getJSONObject(), jsonNew.getJSONObject());
		mUpdateResourceData = jsonOld.toString();
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.resource.updateResource", mUpdateResourceData.getBytes(),
				null);
	}

	/**
	 * 方法名：设置文本资源数据
	 * 方法描述：使用此方法根据RES_ID替换内容
	 * 			替换方式为键值型替换，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param resId 资源ID，参考RES_ID
	 * @param style 需要修改的风格
	 * @param data  替换的资源
	 */
	public void setTextResourceString(String resId, String style, String data) {
		try {
			JSONObject json = new JSONObject();
			json.put(style, data);
			JSONObject j = new JSONObject();
			j.put(resId, json);
			loadResourceData(j.toString(), false);
		} catch (Exception e) {

		}
	}

	/**
	 * 方法名：设置文本资源数据
	 * 方法描述：使用此方法根据RES_ID替换内容
	 * 			替换方式为键值型替换，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param resId 资源ID，参考RES_ID
	 * @param style 需要修改的风格
	 * @param data  替换的字符串数组资源，长度大于1时，随机使用其中一个
	 */
	public void setTextResourceString(String resId, String style, String[] data) {
		try {
			JSONBuilder array = new JSONBuilder();
			array.put(style, data);
			JSONObject json = new JSONObject(array.toString());
			JSONObject j = new JSONObject();
			j.put(resId, json);
			loadResourceData(j.toString(), false);
		} catch (Exception e) {

		}
	}

	/**
	 * 方法名：设置文本资源数据
	 * 方法描述：使用此方法根据RES_ID替换资源，替换默认风格
	 * 			替换方式为键值型替换，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param resId 资源ID，参考RES_ID
	 * @param data  设置的数据
	 */
	public void setTextResourceString(String resId, String data) {
		JSONBuilder json = new JSONBuilder();
		json.put(resId, data);
		loadResourceData(json.toString(), false);
	}

	/**
	 * 方法名：设置文本资源数据
	 * 方法描述：使用此方法根据RES_ID替换资源，替换默认风格
	 * 			替换方式为键值型替换，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param resId 资源ID，参考RES_ID
	 * @param data  替换的字符串数组资源，长度大于1时，随机使用其中一个
	 */
	public void setTextResourceString(String resId, String[] data) {
		JSONBuilder json = new JSONBuilder();
		json.put(resId, data);
		loadResourceData(json.toString(), false);
	}

	/**
	 *  方法名：加载语音资源文件（文本）
	 *  方法描述：通过此接口替换语音默认资源，默认增量更新
	 *  		 替换方式为文本字符串，此接口需要使用到语音res_string_zh-CN.json文件，请联系同行者相关支持人员
	 *
	 * @param jsonData 文本字符串数据
	 */
	public void setTextResourceString(String jsonData) {
		loadResourceData(jsonData, false);
	}

	/**
	 * 接口名：自定义语音展示界面接口
	 * 接口描述：同行者UI1.0接口实现类
	 * 			通过接口回调语音界面相关状态，实现对应接口和方法以完成自定义界面展示
	 */
	public static interface RecordWin {

		/**
		 * 接口名：语音展示界面监听类
		 * 接口描述：UI状态监听接口，通过此接口通知语音修改对应状态或者参数
		 */
		public static interface RecordWinOperateListener {

            /**
             *  枚举类名：列表的枚举类型
			 *  枚举类描述：语音界面展示的不同列表类型
             */
			public enum ListType{
				ContactList,
				PoiList,
				WxContactList,
				AudioList,
				CommList
			}

			/**
			 *  枚举类名：点击的枚举类型
			 *  枚举类描述：上一页和下一页
			 */
			public enum ClickType {
				PREPAGE, NEXTPAGE
			}

			/**
			 * 方法名：调整屏幕显示item数
			 * 方法描述：通过此方法调整列表页一页显示数据条数
			 *
			 * @param count 数据条数
			 */
			public void onScreenSupportCount(int count);

			/**
			 * 方法名：是否使用默认的选择器
			 * 方法描述：通过此方法启用/关闭列表页默认选择器
			 *
			 * @param useDefault 是否启用，默认启用
			 */
			public void useDefaultSelector(boolean useDefault);

			/**
			 * 方法名：确认列表点击
			 * 方法描述：通过此方法，模拟点击列表对应条目索引
			 *
			 * @param position 点击条目索引
			 */
			public void onSelectItemRight(int position);

			/**
			 * 方法名：上下页点击
			 * 方法描述：通过此方法，模拟点击列表页上一页下一页
			 *
			 * @param eventType 列表类型
			 * @param type      点击类型
			 */
			public void onDisplayPageClick(ListType eventType, ClickType type);

			/**
			 * 方法名：列表点击事件
			 * 方法描述：通过此方法，模拟列表页点击事件
			 *
			 * @param motionEventAction 点击事件，参考MotionEvent
			 */
			public void onDisplayLvOnTouchListener(int motionEventAction);

			/**
			 * 方法名：点击确定
			 * 方法描述：当列表仅有一项，进行选择时可以使用该函数，也可使用onSelectContact(0)代替
			 */
			public void onClickSure();

			/**
			 * 方法名：点击取消
			 * 方法描述：通过此方法，展示列表页时，模拟取消
			 */
			public void onClickCancel();

			/**
			 * 方法名：点击语音形象
			 * 方法描述：通过此方法，模拟点击语音形象，语音形象不同阶段时，点击效果不同
			 *  		 语音形象录音时，点击效果为停止录音，进入识别状态
			 *  		 语音形象播报时，点击效果为进入录音，停止播报状态
			 */
			public void onTouch();

			/**
			 * 方法名：点击关闭按钮
			 * 方法描述：通过此方法，模拟语音界面点击关闭按钮，关闭语音界面
			 */
			public void onClose();

			/**
			 * 方法名：联系人选择
			 * 方法描述：通过此方法，模拟联系人选择页点击对应Item点击事件
			 *
			 * @param index 选择的索引，从0开始
			 */
			public void onSelectContact(int index);

			/**
			 * 方法名：微信联系人选择
			 * 方法描述：通过此方法，模拟微信联系人选择页Item点击事件
			 *
			 * @param index 选择的索引，从0开始
			 */
			public void onSelectWxContact(int index);

			/**
			 * 方法名：点击帮助按钮
			 * 方法描述：通过此方法，模拟语音点击帮助按钮
			 */
			public void onClickHelpIcon();

			/**
			 * 方法名：选择类型列表某项被选中
			 * 方法描述：通过此方法，模拟选择类型列表某项被选中
			 *
			 * @param listType 列表类型
			 * @param index    列表索引
			 * @param speech   播报字符串
			 */
			public void onSelectListItem(ListType listType,int index,String speech);
		}

		/**
		 * 方法名：设置窗口操作监听器
		 * 方法描述：语音界面监听器，手动保存此监听器，用于控制语音相关状态和参数
		 *
		 * @param listener 窗口操作监听器
		 */
		public void setOperateListener(RecordWinOperateListener listener);

		/**
		 * 方法名：打开录音界面
		 * 方法描述：UI实现方法，收到此回调时，展示语音界面
		 */
		public void open();

		/**
		 * 方法名：打开录音界面
		 * 方法描述：UI实现方法，收到此回调时，关闭语音界面
		 */
		public void close();

		/**
		 * 方法名：打开录音界面
		 * 方法描述：UI实现方法，语音识别到外部声音大小变化时，回调此方法
		 */
		public void onVolumeChange(int volume);

		/**
		 * 方法名：进度计时通知
		 * 方法描述：UI实现方法，列表选择页进度变化时，回调此方法
		 */
		public void onProgressChanged(int progress);

		/**
		 * 枚举类名：录音形象状态
		 * 枚举类描述：语音录音形象状态
		 */
		public static enum RecordStatus {

			/**
			 * 枚举名：空闲或结束
			 * 枚举描述：代表语音播报状态
			 */
			STATUS_IDLE,

			/**
			 * 枚举名：录音中
			 * 枚举描述：代表语音录音状态
			 */
			STATUS_RECORDING,

			/**
			 * 枚举名：识别中
			 * 枚举描述：代表语音识别状态
			 */
			STATUS_RECOGONIZING,
		}

		/**
		 * 方法名：语音状态变化通知
		 * 方法描述：UI实现方法，语音状态变化时，回调此方法
		 *
		 * @param status 录音形象状态
		 * @see RecordStatus
		 */
		public void onStatusChange(RecordStatus status);

		/**
		 * 方法名：显示用户输入文本
		 * 方法描述：UI实现方法，显示由用户语音输入的文本内容，即识别的结果文字
		 *
		 * @param text 识别文本
		 */
		public void showUsrText(String text);

		/**
		 * 方法名：显示用户输入文本(连续)
		 * 方法描述：UI实现方法，显示由用户语音输入的文本内容，实时识别，一次识别中会多次回调此方法
		 *
		 * @param text 实时识别文本
		 */
		public void showUsrPartText(String text);

		/**
		 * 方法名：显示系统文本
		 * 方法描述：UI实现方法，显示由系统展示的文本内容时，会回调此方法
		 *
		 * @param text 实时识别文本
		 */
		public void showSysText(String text);

		/**
		 * 方法名：显示天气信息
		 * 方法描述：UI实现方法，需要展示天气信息时，回调此方法
		 *
		 * @param data 天气数据
		 */
		public void showWheatherInfo(String data);

		/**
		 * 方法名：显示股票信息
		 * 方法描述：UI实现方法，需要展示股票信息时，回调此方法
		 *
		 * @param data 股票数据
		 */
		public void showStockInfo(String data);

		/**
		 * 方法名：显示联系人选择
		 * 方法描述：UI实现方法，需要展示联系人选择列表时，回调此方法
		 *
		 * @param data 联系人数据
		 */
		public void showContactChoice(String data);

		/**
		 * 方法名：显示地址选择
		 * 方法描述：UI实现方法，需要展示地址选择列表时，回调此方法
		 *
		 * @param data 地址数据
		 */
		public void showAddressChoice(String data);

		/**
		 * 方法名：显示微信联系人选择
		 * 方法描述：UI实现方法，需要展示微信联系人选择列表时，回调此方法
		 *
		 * @param data 微信联系人数据
		 */
		public void showWxContactChoice(String data);

		/**
		 * 方法名：显示音频选择
		 * 方法描述：UI实现方法，需要展示音频选择列表时，回调此方法
		 *
		 * @param data 音频数据
		 */
		public void showAudioChoice(String data);

		/**
		 * 方法名：显示列表选择
		 * 方法描述：UI实现方法，需要展示其它列表选择时，回调此方法
		 *
		 * @param data 列表数据
		 */
		public void showListChoice(int type, String data);

		/**
		 * 方法名：显示数据
		 * 方法描述：UI实现方法，数据类型非文本时，回调此方法
		 *
		 * @param data 需要展示的数据
		 */
		public void showData(String data);

		/**
		 * 方法名：上下翻页
		 * 方法描述：UI实现方法，调整列表页数时，回调此方法
		 *
		 * @param next false:上一页 true:下一页
		 */
		public void snapPager(boolean next);

		/**
		 * 当前页选中的下标
		 * @param selection 从0开始
		 */
		public void onItemSelect(int selection);
	}

	private boolean mHasSetTool = false;
	private RecordWin mRecordWin = null;

	/**
	 *  方法名：设置录音窗口接口
	 *  方法描述：同行者语音UI 1.0，通过实现语音状态回调，实现自定义UI界面，默认直接替换内置界面
	 * 
	 * @param win 录音窗口实现类
	 */
	public void setRecordWin(final RecordWin win) {
		setRecordWin(win, false);
	}

	/**
	 * 方法名：设置录音窗口接口
	 * 方法描述：同行者语音UI 1.0，通过实现语音状态回调，实现自定义UI界面，可以选择保留内置窗口
	 *
	 * @param win          录音窗口实现类
	 * @param reserveInner 是否保留内部录音窗口，用于实现多屏互动
	 */
	public void setRecordWin(final RecordWin win, final boolean reserveInner) {
		mHasSetTool = true;
		mHasSetHudRecordWin = false;
		mRecordWin = win;
		if (null == mRecordWin) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.record.win.clear", null, null);
			return;
		}

        //此处实现了RecordWinOperateListener接口的方法，上面setOperateListener方法传递过来的即此对象
		win.setOperateListener(new RecordWinOperateListener() {
			private boolean mMultiHelpClickLock = false;

			@Override
			public void onTouch() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.button.pause", null, null);
			}

			@Override
			public void onSelectContact(int index) {
				String data = new JSONBuilder().put("index", index)
						.put("type", 0).toString();
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.item.selected", data.getBytes(),
						null);
			}
			
			@Override
			public void onSelectWxContact(int index) {
				String data = new JSONBuilder().put("index", index)
						.put("type", 1).toString();
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.item.selected", data.getBytes(),
						null);
			}

			@Override
			public void onClose() {
				mMultiHelpClickLock = false;
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.dismiss", null, null);
			}

			@Override
			public void onClickSure() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.button.ok", null, null);
			}

			@Override
			public void onClickCancel() {
				String data = new JSONBuilder().put("type", 0).toString();
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.button.cancel", data.getBytes(),
						null);
			}

			@Override
			public void onClickHelpIcon() {
				mMultiHelpClickLock = true;
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.help.ui.detail.open", null, new GetDataCallback() {
							@Override
							public void onGetInvokeResponse(ServiceData data) {
								mMultiHelpClickLock = false;
							}
						});
			}

			@Override
			public void onSelectListItem(ListType listType, int index, String speech) {
				int type = -1;
				switch (listType) {
				case ContactList:
					type = 0;
					break;
				case WxContactList:
					type = 1;
					break;
				case AudioList:
					type = 4;
					break;
				case PoiList:
					type = 5;
					break;
				default:
					break;
				}
				if (type != -1) {
					String data = new JSONBuilder().put("index", index).put("type", type).put("speech", speech)
							.toString();
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
							data.getBytes(), null);
				}
			}

			@Override
			public void onScreenSupportCount(int count) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.count",
						(count + "").getBytes(), null);
			}
			
			@Override
			public void useDefaultSelector(boolean useDefault) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.selector.useNewSelector",
						(useDefault + "").getBytes(), null);
			}

			@Override
			public void onSelectItemRight(int position) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.right",
						(position + "").getBytes(), null);
			}

			@Override
			public void onDisplayLvOnTouchListener(int motionEventAction) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.record.ui.event.list.ontouch",
						(motionEventAction + "").getBytes(), null);
			}
			
			@Override
			public void onDisplayPageClick(ListType eventType, ClickType type) {
				JSONBuilder jb = new JSONBuilder();
				if (eventType == ListType.AudioList || eventType == ListType.PoiList
						|| eventType == ListType.WxContactList || eventType == ListType.CommList) {
					jb.put("type", 1);
				}
				
				if (type == ClickType.PREPAGE) {
					jb.put("clicktype", 1);
				} else {
					jb.put("clicktype", 2);
				}
				
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
						jb.toBytes(), null);
			}
		});

		TXZService.setCommandProcessor("win.record.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, final String command,
					final byte[] data) {
				if (command.equals("show")) {
					win.open();
				} else if (command.equals("dismiss")) {
					win.close();
				} else if (command.equals("status")) {
					Integer status = new JSONBuilder(data).getVal("status",
							Integer.class);
					if (status != null) {
						if (status == 1) {
							win.onStatusChange(RecordStatus.STATUS_RECORDING);
						} else if (status == 2) {
							win.onStatusChange(RecordStatus.STATUS_RECOGONIZING);
						} else {
							win.onStatusChange(RecordStatus.STATUS_IDLE);
						}
					}
				} else if (command.equals("volume")) {
					Integer volume = new JSONBuilder(data).getVal("volume",
							Integer.class);
					if (volume != null) {
						win.onVolumeChange(volume);
					}
				} else if (command.equals("progress")) {
					Integer progress = new JSONBuilder(data).getVal("progress",
							Integer.class);
					if (progress != null) {
						win.onProgressChanged(progress);
					}
				} else if (command.equals("chat.sys")) {
					JSONBuilder doc = new JSONBuilder(data);
					String text = doc.getVal("text", String.class);
					win.showSysText(text);
				} else if (command.equals("chat.usr")) {
					JSONBuilder doc = new JSONBuilder(data);
					String text = doc.getVal("text", String.class);
					win.showUsrText(text);
				} else if (command.equals("chat.usr_part")) {
					JSONBuilder doc = new JSONBuilder(data);
					String text = doc.getVal("text", String.class);
					win.showUsrPartText(text);
				}else if(command.equals("data")) {
					if(data != null) {
						win.showData(new String(data));
					}
				}else if (command.equals("list")) {
					if (data != null) {
						try{
							JSONBuilder doc = new JSONBuilder(data);
							Integer type = doc.getVal("type", Integer.class);
							if (type != null && type != 0) {
								if (type == 2) {
									win.showAddressChoice(new String(data));
									return null;
								}
								if (type == 1) {
									win.showWxContactChoice(new String(data));
									return null;
								}
								if (type == 4) {
									win.showAudioChoice(new String(data));
									return null;
								}
								
								win.showListChoice(type, new String(data));
								return null;
							}
							if (type != null && type == 0) {
								win.showContactChoice(new String(data));
							}
						}catch(Exception e){
							
						}
					}
				} else if (command.equals("list.pager")) {
					Boolean next = Boolean.parseBoolean(new String(data));
					win.snapPager(next);
					return null;
				} else if (command.equals("stock")) {
					if (data != null) {
						try {
							StockInfo info = StockInfo.parseFrom(data);
							JSONObject jObj = new JSONObject();
							jObj.put("strName", info.strName);
							jObj.put("strCode", info.strCode);
							jObj.put("strUrl", info.strUrl);
							jObj.put("strCurrentPrice", info.strCurrentPrice);
							jObj.put("strChangeAmount", info.strChangeAmount);
							jObj.put("strChangeRate", info.strChangeRate);
							jObj.put("strHighestPrice", info.strHighestPrice);
							jObj.put("strLowestPrice", info.strLowestPrice);
							jObj.put("strTradingVolume", info.strTradingVolume);
							jObj.put("strYestodayClosePrice",
									info.strYestodayClosePrice);
							jObj.put("strTodayOpenPrice",
									info.strTodayOpenPrice);
							jObj.put("strUpdateTime", info.strUpdateTime);
							win.showStockInfo(jObj.toString());
						} catch (Exception e) {
						}
					}
				} else if (command.equals("weather")) {
					if (data != null) {
						try {
							WeatherInfos info = WeatherInfos.parseFrom(data);
							JSONObject jObj = new JSONObject();
							jObj.put("strCityName", info.strCityName);
							jObj.put("uint32FocusIndex", info.uint32FocusIndex);
							WeatherData[] weatherDatas = info.rptMsgWeather;
							JSONArray jWeatherArr = new JSONArray();
							for (int i = 0; i < weatherDatas.length; i++) {
								WeatherData weatherData = weatherDatas[i];
								JSONObject jWeather = new JSONObject();
								jWeather.put("uint32Year",
										weatherData.uint32Year);
								jWeather.put("uint32Month",
										weatherData.uint32Month);
								jWeather.put("uint32Day", weatherData.uint32Day);
								jWeather.put("uint32DayOfWeek",
										weatherData.uint32DayOfWeek);
								jWeather.put("strWeather",
										weatherData.strWeather);
								jWeather.put("int32CurTemperature",
										weatherData.int32CurTemperature);
								jWeather.put("int32LowTemperature",
										weatherData.int32LowTemperature);
								jWeather.put("int32HighTemperature",
										weatherData.int32HighTemperature);
								jWeather.put("int32Pm25", weatherData.int32Pm25);
								jWeather.put("strAirQuality",
										weatherData.strAirQuality);
								jWeather.put("strWind", weatherData.strWind);
								jWeather.put("strCarWashIndex",
										weatherData.strCarWashIndex);
								jWeather.put("strCarWashIndexDesc",
										weatherData.strCarWashIndexDesc);
								jWeather.put("strTravelIndex",
										weatherData.strTravelIndex);
								jWeather.put("strTravelIndexDesc",
										weatherData.strTravelIndexDesc);
								jWeather.put("strSportIndex",
										weatherData.strSportIndex);
								jWeather.put("strSportIndexDesc",
										weatherData.strSportIndexDesc);
								jWeather.put("strSuggest",
										weatherData.strSuggest);
								jWeather.put("strComfortIndex",
										weatherData.strComfortIndex);
								jWeather.put("strComfortIndexDesc",
										weatherData.strComfortIndexDesc);
								jWeather.put("strColdIndex",
										weatherData.strColdIndex);
								jWeather.put("strColdIndexDesc",
										weatherData.strColdIndexDesc);
								jWeather.put("strMorningExerciseIndex",
										weatherData.strMorningExerciseIndex);
								jWeather.put("strMorningExerciseIndexDesc",
										weatherData.strMorningExerciseIndexDesc);
								jWeather.put("strDressIndex",
										weatherData.strDressIndex);
								jWeather.put("strDressIndexDesc",
										weatherData.strDressIndexDesc);
								jWeather.put("strUmbrellaIndex",
										weatherData.strUmbrellaIndex);
								jWeather.put("strUmbrellaIndexDesc",
										weatherData.strUmbrellaIndexDesc);
								jWeather.put("strSunBlockIndex",
										weatherData.strSunBlockIndex);
								jWeather.put("strSunBlockIndexDesc",
										weatherData.strSunBlockIndexDesc);
								jWeather.put("strDryingIndex",
										weatherData.strDryingIndex);
								jWeather.put("strDryingIndexDesc",
										weatherData.strDryingIndexDesc);
								jWeather.put("strDatingIndex",
										weatherData.strDatingIndex);
								jWeather.put("strDatingIndexDesc",
										weatherData.strDatingIndexDesc);
								jWeatherArr.put(jWeather);
							}
							jObj.put("rptMsgWeather", jWeatherArr);
							win.showWheatherInfo(jObj.toString());
						} catch (Exception e) {

						}
					}
					return null;
				}else if (command.equals("onItemSelect")) {
					JSONBuilder doc = new JSONBuilder(data);
					Integer selection = doc.getVal("selection", Integer.class);
					if (selection != null) {
						win.onItemSelect(selection);
					}
					return null;
				}
				return null;
			}
		});
		JSONBuilder cfg = new JSONBuilder();
		cfg.put("reserveInner", reserveInner);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.prepare", cfg.toBytes(), null);
		LogUtil.logd("txz.record.win.prepare.hud.false");
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.prepare.hud", "false".getBytes(), null);
	}
	
	boolean mHasSetHudRecordWin;

	/**
	 * 方法名：设置Hud项目的录音界面（方法预留）
	 * 方法描述：针对HUD项目设置录音界面
	 *
	 * @param recordWin 录音界面实例
	 */
	public void setHudRecordWin(final RecordWin recordWin){
		mHasSetTool = true;
		mRecordWin = recordWin;
		setRecordWin(recordWin);
		mHasSetHudRecordWin = true;
		LogUtil.logd("txz.record.win.prepare.hud.true");
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.prepare.hud", "true".getBytes(), null);
	}


	/**
	 * 接口名：自定义帮助接口
	 * 接口描述：通过实现该接口，自定义帮助界面
	 */
	public static interface HelpWin{
		public void show();
		public void close();
	}
	
	private boolean mHasSetHelpWin = false;
	private HelpWin mHelpWin;

	/**
	 * 方法名：设置帮助界面
	 * 方法描述：语音UI1.0，自定义语音帮助界面
	 *
	 * @param helpWin 打开/关闭帮助时的回调
	 */
	public void setHelpWin(final HelpWin helpWin){
		mHasSetHelpWin = true;
		mHelpWin = helpWin;
		if(null == mHelpWin){
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.win.clear", null, null);
			return;
		}
		TXZService.setCommandProcessor("help.win.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if ("show".equals(command)) {
					helpWin.show();
				} else if ("dismiss".equals(command)) {
					helpWin.close();
				}
				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.win.set", null, null);
	}

	/**
	 * 方法名：取消关闭语音界面
	 * 方法描述：远程命令字（Command）处理完时会默认关闭语音界面，可通过此方法进行取消
	 */
	public void cancelCloseRecordWin() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.cancelClose", null, null);
	}

	/**
	 * @see AsrScene 新版本
	 * 枚举类名：识别场景
	 * 枚举类名：识别的场景类型，用于特定指定类型
	 * @deprecated 已废弃
	 */
	public enum AsrSence{
		PoiSence,
		CallSence;
	}

	/**
	 * @see AsrScene
	 * 方法名：进入指定的解析情景
	 * 方法描述：进入指定的解析情景，如：PoiSence，则用户下一句所说的话会放在导航目的地进行处理
	 * @deprecated 已废弃
	 */
	public void enterSpecifyAsrSence(AsrSence asrSence){
		int sence = 0;
		switch (asrSence) {
		case PoiSence:
			sence = 1;
			break;
		case CallSence:
			sence = 2;
			break;
		default:
			break;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.enterSpecifyAsrSence", (""+sence).getBytes(), null);
	}

	/**
	 * 枚举类名：识别场景
	 * 枚举类名：识别的场景类型，用于特定指定类型
	 */
	public enum AsrScene{
		PoiScene,
		CallScene,
		MusicScene;
	}

	/**
	 * 方法名：进入指定的解析情景
	 * 方法描述：指定的解析情景。打开声控后，下一次识别的结果如果是模糊语义优先按哪个场景处理
	 * 			如导航场景，则用户下一句所说的话会放在导航目的地进行处理
	 *
	 * @param asrScene  指定场景
	 * @param hintText  显示的声控界面的文本
	 * @param keepScene 将该场景设置成默认场景，在本次语音交互中如果是模糊语义则优先按当前场景处理，界面关闭后失效
	 * @param needSpeak 是否需要将显示在界面上的文本播报出来
	 * @param data      为方便未来扩展设置的额外的参数，目前没用
	 */
	public void enterSpecifyAsrScene(AsrScene asrScene, String hintText, boolean keepScene, boolean needSpeak, String data){
		int scene = 0;
		switch (asrScene) {
		case PoiScene:
			scene = 1;
			break;
		case CallScene:
			scene = 2;
			break;
		case MusicScene:
			scene = 3;
			break;
		default:
			break;
		}
		JSONBuilder jObj = new JSONBuilder();
		jObj.put("scene", scene);
		jObj.put("hintText", hintText);
		jObj.put("keepScene", keepScene);
		jObj.put("needSpeak", needSpeak);
		jObj.put("data", data);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.enterSpecifyAsrScene", jObj.toBytes(), null);
	}

	/**
	 * 方法名：翻页点击事件
	 * 方法描述：默认列表类型传 1 默认点击类型 1上一页 2下一页
	 *
	 * @param eventType 事件类型，默认点击类型1, 1:上一页 2:下一页
	 * @param clickType 点击类型，1：确认，2：取消
	 */
	public void onPageInfoClick(int eventType, int clickType) {
		JSONBuilder jb = new JSONBuilder();
		jb.put("type", eventType);
		jb.put("clicktype", clickType);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page", jb.toBytes(),
				null);
	}

	/**
	 * 方法名：关闭语音窗口
	 * 方法描述：强行关闭语音界面
	 */
	public void dissmissRecordWin() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.dissmiss", null, null);
	}

	/**
	 * 方法名：关闭帮助界面
	 * 方法描述：强行关闭帮助界面
	 */
	public void dismissHelpWin() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.closeHelpWin", null, null);
	}

	/**
	 * 方法名：打开帮助界面
	 * 方法描述：手动显示帮助界面
	 */
	public void showHelpWin(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.open", "sdk".getBytes(), null);
	}

	/**
	 * 方法名：显示系统文本
	 * 方法描述：在语音界面显示指定的文字
	 *
	 * @param text 需要显示的文本
	 */
	public void showSysText(String text) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.showSysText", text.getBytes(), null);
	}

	/**
	 * 方法名：展示POI列表
	 * 方法描述：主动调用，在语音界面展示POI列表
	 *
	 * @param pois     POI信息
	 * @param city     城市信息
	 * @param keywords 关键字信息
	 */
	public void showPoiList(List<Poi> pois, String city, String keywords) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("type", 1);
			jsonObject.put("city", city);
			jsonObject.put("keywords", keywords);
			if (pois != null && pois.size() > 0) {
				JSONArray jsonArray = new JSONArray();
				for (Poi poi : pois) {
					jsonArray.put(poi.toJsonObject());
				}
				jsonObject.put("pois", jsonArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.ui.showList",
				jsonObject.toString().getBytes(), null);
	}

	/**
	 * 外放POI页面且选中后将结果返回给第三方
	 * @param pois
	 * @param tips  标题提示语
	 * @param keywords
	 */
	public void showThirdPoiList(List<Poi> pois, String tips, String keywords) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("tips", tips);
			jsonObject.put("keywords", keywords);
			if (pois != null && pois.size() > 0) {
				JSONArray jsonArray = new JSONArray();
				for (Poi poi : pois) {
					jsonArray.put(poi.toJsonObject());
				}
				jsonObject.put("pois", jsonArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.ui.showThirdPoiList",
				jsonObject.toString().getBytes(), null);
	}

	/**
	 * 外放POI页面且选中后将结果返回给第三方
	 * @param pois
	 * @param tips  标题提示语
	 * @param keywords
	 */
	public void showThirdPoiList(List<Poi> pois, String tips, String keywords,boolean isCloseWin) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("tips", tips);
			jsonObject.put("keywords", keywords);
			jsonObject.put("isCloseWin",isCloseWin);
			if (pois != null && pois.size() > 0) {
				JSONArray jsonArray = new JSONArray();
				for (Poi poi : pois) {
					jsonArray.put(poi.toJsonObject());
				}
				jsonObject.put("pois", jsonArray);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.ui.showThirdPoiList",
				jsonObject.toString().getBytes(), null);
	}
	
	private Boolean mJustText;

	/**
	 * 方法名：设置POI搜索未找到信息时是否展示手动调整按钮
	 * 方法描述：当POI找不到结果显示的类型，设置是否展示手动调整按钮
	 *
	 * @param justText 是否支持调整按钮，默认支持
	 *                 true：只显示一条简单的消息，不支持手动点击修改
	 *                 false：显示复合信息，支持手动点击修改
	 */
	public void setRecordWin2PoiNoResultMsgType(boolean justText) {
		mJustText = justText;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.poinoresult",
				(justText + "").getBytes(), null);
	}

	/**
	 * 方法名：播报文本方法，同时在录音窗口显示对应文字
	 * 方法描述：调用语音播报文本，如果在录音页面时，会在用户侧显示出播报文字
	 *
	 * @param text        播报的文本
	 * @param close       是否在播报结束后关闭录音窗口，不关闭将会再次启动声控
	 * @param endRunnable 结束的回调
	 */
	public void speakTextOnRecordWin(String text, final boolean close,
			final Runnable endRunnable) {
		speakTextOnRecordWin("", text, close, endRunnable);
	}
	
	private Map<Long, Runnable> ttsRunnableMap = new HashMap<Long, Runnable>();

	/**
	 * 方法名：播报文本方法，同时在录音窗口显示对应文字
	 * 方法描述：调用语音播报文本，如果在录音页面时，会在用户侧显示出播报文字
	 *
	 * @param resId 需要播报的文本
	 * @param text  ID未定义时播报的文本
	 * @param close 是否在播报结束后关闭录音窗口，不关闭将会再次启动声控
	 * @param oRun  结束的回调
	 */
	public void speakTextOnRecordWin(String resId,String text, boolean close, Runnable oRun) {
		speakTextOnRecordWin(resId,text,close,true,oRun);
	}

	/**
	 * 方法名：播报文本方法，同时在录音窗口显示对应文字
	 * 方法描述：调用语音播报文本，如果在录音页面时，会在用户侧显示出播报文字
	 *
	 * @param resId           需要播报的文本
	 * @param text            ID未定义时播报的文本
	 * @param close           是否在播报结束后关闭录音窗口，不关闭将会再次启动声控
	 * @param isCancleExecute 是否立即取消当前执行的任务
	 * @param endRunnable     结束的回调
	 */
	public void speakTextOnRecordWin(String resId, String text, final boolean close,boolean isCancleExecute, final Runnable endRunnable) {
		long taskId = SystemClock.elapsedRealtime();
		ttsRunnableMap.put(taskId, endRunnable);
		TXZService.setCommandProcessor(
				"sdk.record.win.speakTextOnRecordWin.end",
				new CommandProcessor() {
					@Override
					public byte[] process(String packageName, String command,
							byte[] data) {
						if (data != null) {
							long taskId = Long.parseLong(new String(data));
							if (ttsRunnableMap.containsKey(taskId)
									&& ttsRunnableMap.get(taskId) != null) {
								ttsRunnableMap.get(taskId).run();
								ttsRunnableMap.remove(taskId);
							}
						}else{
							endRunnable.run();
						}
						return null;
					}
				});
		JSONBuilder json = new JSONBuilder();
		json.put("text", text);
		json.put("close", close);
		json.put("resId", resId);
		json.put("taskId", taskId);
		json.put("isCancleExecute", isCancleExecute);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.record.win.speakTextOnRecordWin", json.toBytes(), null);
	}

	/**
	 *  接口名：自定义弹窗监听
	 *  接口描述：监听弹窗的点击事件，根据点击的确定或者取消完成不同的实现
	 */
	public static interface WinConfirmAsrListener{
		void onClickOk();
		void onClickCancel();
	}
	
	private static int mNextDialogId = 1;

	/**
	 * 方法名：创建自定义语音识别弹窗
	 * 方法描述：自定义弹窗，非自定义弹窗的样式，根据参数设置自己想要的效果
	 * 			可以通过cancleDialog(id)强制关闭
	 *
	 * @param message        弹窗上显示的文本
	 * @param sureText       确定选择的显示文本
	 * @param sureCmds       确定选择命令
	 * @param cancelText     取消选择的显示文本
	 * @param cancelCmds     取消选择命令
	 * @param hintText       弹窗时播报的文本
	 * @param listener       选择回调监听器
	 * @param ttsEndRunnable 播报结束回调
	 * @return 弹窗ID
	 */
	public int createWinConfirmAsr(String message, String sureText,
			String[] sureCmds, String cancelText, String[] cancelCmds,
			String hintText,final WinConfirmAsrListener listener,final Runnable ttsEndRunnable){
		if (TextUtils.isEmpty(message) 
				|| TextUtils.isEmpty(sureText) 
				|| TextUtils.isEmpty(cancelText) 
				|| TextUtils.isEmpty(hintText)
				|| sureCmds == null
				|| cancelCmds == null) {
			return -1;
		}
		TXZService.setCommandProcessor("sdk.record.win.dialog", new CommandProcessor() {
			
			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (data!=null) {
					String cmd = new String(data);
					if ("ok".equals(cmd) && listener != null) {
						listener.onClickOk();
					}
					if ("cancel".equals(cmd) && listener != null) {
						listener.onClickCancel();
					}
					if ("runnable".equals(cmd) && ttsEndRunnable != null) {
						ttsEndRunnable.run();
					}
				}
				return null;
			}
		});
		JSONObject job = new JSONObject();
		JSONArray jry = new JSONArray();
		int id = mNextDialogId++;
		try {
			job.put("taskId", id);
			job.put("message", message);
			job.put("sureText", sureText);
			for (int i = 0; i < sureCmds.length; i++) {
				jry.put(sureCmds[i]);
			}
			job.put("sureCmds", jry.toString());
			job.put("cancelText", cancelText);
			jry = new JSONArray();
			for (int i = 0; i < cancelCmds.length; i++) {
				jry.put(cancelCmds[i]);
			}
			job.put("cancelCmds", jry.toString());
			job.put("hintText", hintText);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.dialog", job.toString().getBytes(), null);
		return id;
	}

	/**
	 * 方法名：关闭自定义弹窗
	 * 方法描述：通过createWinConfirmAsr弹窗时获取的taskid，将弹窗关闭
	 *
	 * @param taskId 弹窗ID值
	 */
	public void cancelDialog(int taskId){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.cancel.dialog", (taskId + "").getBytes(), null);
	}

	/**
	 * 方法名：获取帮助数据
	 * 方法描述：获取语音帮助界面的所有数据
	 *
	 * @param callback 帮助数据回调
	 */
	public void getHelpDetailItems(final OnGetHelpDetailCallback callback) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.getHelpDetailItems", null, new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null && callback != null) {
					callback.onGetHelpDetail(data.getString());
				}
			}
		});
	}

	/**
	 * 接口名：获取帮助信息的回调
	 * 接口描述：通过getHelpDetailItems方法，获取帮助界面信息
	 */
	public static interface OnGetHelpDetailCallback {
		void onGetHelpDetail(String jsonData);
	}
}
