package com.txznet.sdk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZService.CommandProcessor;

/**
 * 已废弃
 * 新的场景工具类为TXZSceneManager
 */
@Deprecated
public class TXZSenceManager {
	private static TXZSenceManager sInstance = new TXZSenceManager();

	private TXZSenceManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZSenceManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		for (Map.Entry<SenceType, SenceTool> entry : mSenceToolMap.entrySet()) {
			setSenceToolInner(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 场景类型
	 */
	public static enum SenceType {
		/**
		 * 所有场景，未设置细化场景时进入这个场景
		 */
		SENCE_TYPE_ALL,
		/**
		 * 唤醒场景，携带识别到的唤醒词
		 */
		SENCE_TYPE_WAKEUP,

		/**
		 * 设置用户唤醒词
		 */
		SENCE_TYPE_SET_USER_WAKEUP_KEYWORDS,

		/**
		 * 命令字场景，识别到的命令，如音量调整等等
		 */
		SENCE_TYPE_COMMAND,

		/**
		 * 应用相关
		 */
		SENCE_TYPE_APP,

		/**
		 * 电话场景
		 */
		SENCE_TYPE_CALL,
		/**
		 * 导航场景
		 */
		SENCE_TYPE_NAV,
		/**
		 * Poi选择
		 */
		SENCE_TYPE_POI_CHOICE,
		/**
		 * 音乐
		 */
		SENCE_TYPE_MUSIC,
		/**
		 * 电台
		 */
		SENCE_TYPE_AUDIO,

		/**
		 * 天气场景，不带天气信息
		 */
		SENCE_TYPE_WEATHER,

		/**
		 * 股票查询，不带股票数据
		 */
		SENCE_TYPE_STOCK,

		/**
		 * 定位场景，询问现在在哪里
		 */
		SENCE_TYPE_LOCATION,

		/**
		 * 路况查询，不带路况信息
		 */
		SENCE_TYPE_TRAFFIC,

		/**
		 * 限号查询，不带限号信息
		 */
		SENCE_TYPE_LIMIT_NUMBER,

		/**
		 * 不可识别
		 */
		SENCE_TYPE_UNKNOW,
		/**
		 * 不可不支持
		 */
		SENCE_TYPE_UNSUPPORT,
		/**
		 * 没有说话
		 */
		SENCE_TYPE_EMPTY,
		/**
		 * 帮助页面
		 */
		SENCE_TYPE_HELP,
		/**
		 * 列表选择
		 */
		SENCE_TYPE_SELECTOR,
		/**
		 * 电影场景
		 */
		SENCE_TYPE_MOVIE
	}

	/**
	 * 场景处理工具
	 * 
	 * @author txz
	 *
	 */
	public static interface SenceTool {
		/**
		 * 处理场景回调
		 * 
		 * @param type
		 *            场景类型
		 * @param data
		 *            场景数据，一般为json，特殊场景如唤醒直接返回唤醒词
		 * @return 返回是否被工具处理，否则继续交给同行者处理
		 */
		public boolean process(SenceType type, String data);
	}

	private Map<SenceType, SenceTool> mSenceToolMap = new ConcurrentHashMap<SenceType, SenceTool>();

	private SenceTool mDefaultSenceTool = new SenceTool() {
		@Override
		public boolean process(SenceType type, String json) {
			return false;
		}
	};

	/**
	 * 设置场景处理工具
	 * 
	 * @param type
	 *            场景类型
	 * @param tool
	 *            处理工具
	 */
	public void setSenceTool(final SenceType type, final SenceTool tool) {
		if (tool == null)
			mSenceToolMap.put(type, mDefaultSenceTool);
		else
			mSenceToolMap.put(type, tool);
		setSenceToolInner(type, tool);
	}

	private void setSenceToolInner(final SenceType type, final SenceTool tool) {
		String sence;
		switch (type) {
		case SENCE_TYPE_ALL:
			sence = "all";
			break;
		case SENCE_TYPE_WAKEUP:
			sence = "wakeup";
			break;
		case SENCE_TYPE_CALL:
			sence = "call";
			break;
		case SENCE_TYPE_MUSIC:
			sence = "music";
			break;
		case SENCE_TYPE_NAV:
			sence = "nav";
			break;
		case SENCE_TYPE_EMPTY:
			sence = "empty";
			break;
		case SENCE_TYPE_UNKNOW:
			sence = "unknow";
			break;
		case SENCE_TYPE_UNSUPPORT:
			sence = "unsupport";
			break;
		case SENCE_TYPE_APP:
			sence = "app";
			break;
		case SENCE_TYPE_COMMAND:
			sence = "command";
			break;
		case SENCE_TYPE_LIMIT_NUMBER:
			sence = "limit_number";
			break;
		case SENCE_TYPE_LOCATION:
			sence = "location";
			break;
		case SENCE_TYPE_POI_CHOICE:
			sence = "poi_choice";
			break;
		case SENCE_TYPE_SET_USER_WAKEUP_KEYWORDS:
			sence = "set_user_wakeup_keywords";
			break;
		case SENCE_TYPE_STOCK:
			sence = "stock";
			break;
		case SENCE_TYPE_TRAFFIC:
			sence = "traffic";
			break;
		case SENCE_TYPE_WEATHER:
			sence = "weather";
			break;
		case SENCE_TYPE_HELP:
			sence = "help";
			break;
		case SENCE_TYPE_AUDIO:
			sence = "audio";
			break;
		case SENCE_TYPE_SELECTOR:
			sence = "selector";
			break;
		case SENCE_TYPE_MOVIE:
			sence = "movie";
			break;
		default:
			return;
		}
		String cmd;
		if (tool == null || mDefaultSenceTool == tool)
			cmd = "txz.sence.clear.";
		else
			cmd = "txz.sence.set.";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				cmd + sence, null, null);
		TXZService.setCommandProcessor("tool.sence." + sence,
				new CommandProcessor() {
					@Override
					public byte[] process(String packageName, String command,
							byte[] data) {
						return ("" + tool.process(type, new String(data)))
								.getBytes();
					}
				});
	}

	/**
	 * 触发内置场景
	 * 
	 * @param type
	 *            场景类型
	 * @param data
	 *            参考场景协议数据定义
	 */
	public void triggerSence(SenceType type, String data) {

	}
}
