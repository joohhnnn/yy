package com.txznet.sdk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZService.CommandProcessor;


/**
 * 类名称：场景管理器
 * 类描述：语音交互中，根据语义理解会解析了出不同场景，进行消息分发
 *		   根据需要的语义场景可以通过场景工具类进行语义拦截，从而修改语音交互
 *         @see TXZSenceManager 已废弃类，通过此类替代
 */
public class TXZSceneManager {
	private static TXZSceneManager sInstance = new TXZSceneManager();

	private TXZSceneManager() {

	}

	/**
	 * 获取单例
	 *
	 * @return 类实例
	 */
	public static TXZSceneManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		for (Map.Entry<SceneType, SceneTool> entry : mSceneToolMap.entrySet()) {
			setSceneToolInner(entry.getKey(), entry.getValue());
		}
		if (mEnablePartScene != null) {
			enablePartScene(mEnablePartScene);
		}
	}

	private Boolean mEnablePartScene = null;


	/**
	 * 类名称：场景类型
	 * 类描述：场景类型枚举类，不同场景的枚举
	 */
	public static enum SceneType {
		/**
		 * 枚举名称：全场景
		 * 枚举描述：设置此类型时，可以拦截全部语义。
		 */
		SCENE_TYPE_ALL,
		/**
		 * 枚举名称：唤醒场景
		 * 枚举描述：设置此类型时，仅拦截用户输入唤醒词的场景。
		 */
		SCENE_TYPE_WAKEUP,

		/**
		 * 枚举名称：设置唤醒词场景
		 * 枚举描述：设置此类型时，仅拦截用户修改名字语义。
		 */
		SCENE_TYPE_SET_USER_WAKEUP_KEYWORDS,

		/**
		 * 枚举名称：命令字识别场景
		 * 枚举描述：设置此类型时，仅拦截为固定式命令类(Command)语义。
		 */
		SCENE_TYPE_COMMAND,

		/**
		 * 枚举名称：APP场景
		 * 枚举描述：设置此类型时，仅拦截为与APP相关语义，如应用打开/关闭
		 */
		SCENE_TYPE_APP,

		/**
		 * 枚举名称：电话场景
		 * 枚举描述：设置此类型时，仅拦截电话相关语义
		 */
		SCENE_TYPE_CALL,

		/**
		 * 枚举名称：导航场景
		 * 枚举描述：设置此类型时，仅拦截导航类相关语义
		 */
		SCENE_TYPE_NAV,

		/**
		 * 枚举名称：POI选择场景
		 * 枚举描述：设置此类型时，仅拦截地图POI列表选中时语义
		 */
		SCENE_TYPE_POI_CHOICE,

		/**
		 * 枚举名称：音乐场景
		 * 枚举描述：设置此类型时，仅拦截音乐相关语义
		 */
		SCENE_TYPE_MUSIC,

		/**
		 * 枚举名称：电台场景
		 * 枚举描述：设置此类型时，仅拦截电台相关语义
		 */
		SCENE_TYPE_AUDIO,

		/**
		 * 枚举名称：天气场景
		 * 枚举描述：设置此类型时，仅拦截天气类语义
		 */
		SCENE_TYPE_WEATHER,

		/**
		 * 枚举名称：股票场景
		 * 枚举描述：设置此类型时，仅拦截股票类语义
		 */
		SCENE_TYPE_STOCK,

		/**
		 * 枚举名称：定位场景
		 * 枚举描述：设置此类型时，仅拦截定位查询类语义，如：我现在在哪
		 */
		SCENE_TYPE_LOCATION,

		/**
		 * 枚举名称：路况查询场景
		 * 枚举描述：设置此类型时，仅拦截路况类语义，如：世界之窗堵不堵
		 */
		SCENE_TYPE_TRAFFIC,

		/**
		 * 枚举名称：限号查询场景
		 * 枚举描述：设置此类型时，仅拦截限号查询类语义，如：北京今天限行信息
		 */
		SCENE_TYPE_LIMIT_NUMBER,

		/**
		 * 枚举名称：不可识别的场景
		 * 枚举描述：设置此类型时，仅拦截无法识别的相关语义
		 */
		SCENE_TYPE_UNKNOW,

		/**
		 * 枚举名称：不支持的指令场景
		 * 枚举描述：设置此类型时，仅拦截当前代码下不支持的语义
		 */
		SCENE_TYPE_UNSUPPORT,

		/**
		 * 枚举名称：未说话场景
		 * 枚举名称：设置此类型时，仅拦截没有说话时的语义
		 */
		SCENE_TYPE_EMPTY,

		/**
		 * 枚举名称：帮助场景
		 * 枚举描述：设置此类型时，仅拦截帮助相关语义，如：打开帮助
		 */
		SCENE_TYPE_HELP,

		/**
		 * 枚举名称：列表选择场景
		 * 枚举描述：设置此类型时，仅拦截列表选择时语义
		 */
		SCENE_TYPE_SELECTOR,

		/**
		 * 枚举名称：电影场景
		 * 枚举描述：设置此类型时，仅拦截电影类语义
		 */
		SCENE_TYPE_MOVIE,

		/**
		 * 枚举名称：微信场景
		 * 枚举描述：设置此类型时，仅拦截微信助手相关语义，如：发微信给同行者
		 */
		SCENE_TYPE_WECHAT,

		/**
		 * 枚举名称：分块场景
		 * 枚举描述：设置此类型时，仅接收识别过程中的文本场景
		 */
		SCENE_TYPE_PART
	}

	/**
	 * 接口名称：场景工具
	 * 接口描述：场景工具回调接口
	 */
	public static interface SceneTool {
		/**
		 * 方法名称：场景回调
		 * 方法描述：获取特定场景及对应语义内容，并决定是否要拦截处理
		 *
		 * @param type 场景类型
		 * @param data 场景数据，一般为json，特殊场景如唤醒直接返回唤醒词
		 * @return 表示是否需要拦截相关语义
		 * 			true 需要拦截且已处理相关交互
		 * 			false 继续交由同行者处理并进行默认交互
		 */
		public boolean process(SceneType type, String data);
	}

	private Map<SceneType, SceneTool> mSceneToolMap = new ConcurrentHashMap<SceneType, SceneTool>();

	private SceneTool mDefaultSceneTool = new SceneTool() {
		@Override
		public boolean process(SceneType type, String json) {
			return false;
		}
	};

	/**
	 * 方法名称：设置场景处理工具
	 * 方法描述：根据对应需求拦截场景的类型，并设置场景工具，以拦截语义并处理
	 *
	 * @param type 需要获取场景语义类型
	 * @param tool 场景处理工具
	 */
	public void setSceneTool(final SceneType type, final SceneTool tool) {
		if (tool == null)
			mSceneToolMap.put(type, mDefaultSceneTool);
		else
			mSceneToolMap.put(type, tool);
		setSceneToolInner(type, tool);
	}

	private void setSceneToolInner(final SceneType type, final SceneTool tool) {
		String scene;
		switch (type) {
		case SCENE_TYPE_ALL:
			scene = "all";
			break;
		case SCENE_TYPE_WAKEUP:
			scene = "wakeup";
			break;
		case SCENE_TYPE_CALL:
			scene = "call";
			break;
		case SCENE_TYPE_MUSIC:
			scene = "music";
			break;
		case SCENE_TYPE_NAV:
			scene = "nav";
			break;
		case SCENE_TYPE_EMPTY:
			scene = "empty";
			break;
		case SCENE_TYPE_UNKNOW:
			scene = "unknow";
			break;
		case SCENE_TYPE_UNSUPPORT:
			scene = "unsupport";
			break;
		case SCENE_TYPE_APP:
			scene = "app";
			break;
		case SCENE_TYPE_COMMAND:
			scene = "command";
			break;
		case SCENE_TYPE_LIMIT_NUMBER:
			scene = "limit_number";
			break;
		case SCENE_TYPE_LOCATION:
			scene = "location";
			break;
		case SCENE_TYPE_POI_CHOICE:
			scene = "poi_choice";
			break;
		case SCENE_TYPE_SET_USER_WAKEUP_KEYWORDS:
			scene = "set_user_wakeup_keywords";
			break;
		case SCENE_TYPE_STOCK:
			scene = "stock";
			break;
		case SCENE_TYPE_TRAFFIC:
			scene = "traffic";
			break;
		case SCENE_TYPE_WEATHER:
			scene = "weather";
			break;
		case SCENE_TYPE_HELP:
			scene = "help";
			break;
		case SCENE_TYPE_AUDIO:
			scene = "audio";
			break;
		case SCENE_TYPE_SELECTOR:
			scene = "selector";
			break;
		case SCENE_TYPE_MOVIE:
			scene = "movie";
			break;
		case SCENE_TYPE_WECHAT:
			scene = "wechat";
			break;
		case SCENE_TYPE_PART:
			scene = "part";
			break;
		default:
			return;
		}
		String cmd;
		if (tool == null || mDefaultSceneTool == tool)
			cmd = "txz.sence.clear.";
		else
			cmd = "txz.sence.set.";
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				cmd + scene, null, null);
		TXZService.setCommandProcessor("tool.sence." + scene,
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
	 * 启用识别过程中的文本通过场景工具流式返回
	 */
	public void enablePartScene(boolean enable){
		mEnablePartScene = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.sence.enablePart", String.valueOf(enable).getBytes(), null);
	}

	/**
	 * 触发内置场景
	 * 
	 * @param type
	 *            场景类型
	 * @param data
	 *            参考场景协议数据定义
	 */
	public void triggerScene(SceneType type, String data) {

	}
}
