package com.txznet.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.sdk.TXZService.CommandProcessor;

import android.text.TextUtils;

public class TXZAsrKeyManager {
	private static TXZAsrKeyManager sInstance = new TXZAsrKeyManager();

	private TXZAsrKeyManager() {

	}

	public static TXZAsrKeyManager getInstance() {
		return sInstance;
	}

	void onReconnectTXZ() {
		if (mAsrKeySources != null) {
			syncAsrkeySources(mAsrKeySources, null);
		}
		
		if (mTypes != null) {
			forbidAsrKeys(mTypes, null);
		}
		
		if (mUnFbKeys != null) {
			unForbidKeys(mUnFbKeys);
		}
		
		if (mModifyAkss != null) {
			modifyAsrKeyCmds(mModifyAkss, null);
		}
		
		if (mKeyTypeMap != null) {
			modifyAsrKeyCmds(mKeyTypeMap);
		}

		if (shieldKeys != null) {
			shieldWakeupKeys(shieldKeys);
		}
		
		if (mHasSetCmdsTool) {
			setCommCmdsTool(mCommCmdsTool);
		}
	}

	public static class AsrKeyType {
		public static final String NAV_RES_PREFIX = "RS_NAV_CMD_";
		/**
		 * 放大地图
		 */
		public static final String ZOOM_IN = "ZOOM_IN";
		/**
		 * 缩小地图
		 */
		public static final String ZOOM_OUT = "ZOOM_OUT";
		/**
		 * 黑夜模式
		 */
		public static final String NIGHT_MODE = "NIGHT_MODE";
		/**
		 * 白天模式
		 */
		public static final String LIGHT_MODE = "LIGHT_MODE";
		/**
		 * 自动模式
		 */
		public static final String AUTO_MODE = "AUTO_MODE";
		/**
		 * 退出导航
		 */
		public static final String EXIT_NAV = "EXIT_NAV";
		/**
		 * 取消导航路径
		 */
		public static final String CANCEL_NAV = "CANCEL_NAV";
		/**
		 * 关闭地图
		 */
		public static final String CLOSE_MAP = "CLOSE_MAP";
		/**
		 * 全览路线
		 */
		public static final String VIEW_ALL = "VIEW_ALL";
		/**
		 * 推荐路线
		 */
		public static final String TUIJIANLUXIAN = "TUIJIANLUXIAN";
		/**
		 * 躲避拥堵
		 */
		public static final String DUOBIYONGDU = "DUOBIYONGDU";
		/**
		 * 不走高速
		 */
		public static final String BUZOUGAOSU = "BUZOUGAOSU";
		/**
		 * 高速优先
		 */
		public static final String GAOSUYOUXIAN = "GAOSUYOUXIAN";
		/**
		 * 时间优先
		 */
		public static final String SHIJIANYOUXIAN = "SHIJIANYOUXIAN";
		/**
		 * 少收费
		 */
		public static final String LESS_MONEY = "LESS_MONEY";
		/**
		 * 少路程
		 */
		public static final String LESS_DISTANCE = "LESS_DISTANCE";
		/**
		 * 前面怎么走
		 */
		public static final String HOW_NAVI = "HOW_NAVI";
		/**
		 * 还有多久
		 */
		public static final String ASK_REMAIN = "ASK_REMAIN";
		/**
		 * 当前限速
		 */
		public static final String LIMIT_SPEED = "LIMIT_SPEED";
		/**
		 * 返回导航（用于全览状态）
		 */
		public static final String BACK_NAVI = "BACK_NAVI";
		/**
		 * 开始导航
		 */
		public static final String START_NAVI = "START_NAVI";
		/**
		 * 打开路况
		 */
		public static final String OPEN_TRAFFIC = "OPEN_TRAFFIC";
		/**
		 * 关闭路况
		 */
		public static final String CLOSE_TRAFFIC = "CLOSE_TRAFFIC";
		/**
		 * 2D模式
		 */
		public static final String TWO_MODE = "TWO_MODE";
		/**
		 * 3D模式
		 */
		public static final String THREE_MODE = "THREE_MODE";
		/**
		 * 车头朝上
		 */
		public static final String CAR_DIRECT = "CAR_DIRECT";
		/**
		 * 正北朝上
		 */
		public static final String NORTH_DIRECT = "NORTH_DIRECT";
		/**
		 * 前方路况
		 */
		public static final String FRONT_TRAFFIC = "FRONT_TRAFFIC";
        /**
         * 切换主路
         */
        public static final String SWITCH_MAIN_ROAD = "SWITCH_MAIN_ROAD";
        /**
		 * 切换主路
		 */
		public static final String SWITCH_SIDE_ROAD = "SWITCH_SIDE_ROAD";
		/**
		 * 刷新路线
		 */
		public static final String REFRESH_PATH = "REFRESH_PATH";
		/**
		 * 导航去收藏点
		 */
		public static final String QUERY_COLLECTION_POINT = "QUERY_COLLECTION_POINT";
		/**
		 * 打开精简模式
		 */
		public static final String OPEN_SIMPLE_MODE = "OPEN_SIMPLE_MODE";
		/**
		 * 关闭精简模式
		 */
		public static final String CLOSE_SIMPLE_MODE = "CLOSE_SIMPLE_MODE";
		/**
		 * 进入组队界面
		 */
		public static final String INTO_TEAM = "INTO_TEAM";
		/**
		 * 切换语音
		 */
		public static final String SWITCH_ROLE = "SWITCH_ROLE";
		/**
		 * 切换国语女声语音
		 */
		public static final String GUOYU_MM = "GUOYU_MM";
		/**
		 * 切换国语男声语音
		 */
		public static final String GUOYU_GG = "GUOYU_GG";
		/**
		 * 切换周星星语音
		 */
		public static final String ZHOUXINGXING = "ZHOUXINGXING";
		/**
		 * 切换广东话语音
		 */
		public static final String GUANGDONGHUA = "GUANGDONGHUA";
		/**
		 * 切换林志玲语音
		 */
		public static final String LINZHILIN = "LINZHILIN";
		/**
		 * 切换郭德纲语音
		 */
		public static final String GUODEGANG = "GUODEGANG";
		
		/**
		 * 东北话
		 */
		public static final String DONGBEIHUA = "DONGBEIHUA";
		
		/**
		 * 河南话
		 */
		public static final String HENANHUA = "HENANHUA";
		
		/**
		 * 湖南话
		 */
		public static final String HUNANHUA = "HUNANHUA";
		
		/**
		 * 四川话
		 */
		public static final String SICHUANHUA = "SICHUANHUA";
		
		/**
		 * 台湾话
		 */
		public static final String TAIWANHUA = "TAIWANHUA";
		
		/**
		 * 萌萌哒
		 */
		public static final String MENGMENGDA = "MENGMENGDA";
		
		/**
		 * 金莎
		 */
		public static final String JINSHA = "JINSHA";
		
		/**
		 * 沿途搜索命令
		 */
		public static final String NAV_WAY_POI_CMD_GAS = "NAV_WAY_POI_CMD_GAS";
		public static final String NAV_WAY_POI_CMD_BANK = "NAV_WAY_POI_CMD_BANK";
		public static final String NAV_WAY_POI_CMD_TOILET = "NAV_WAY_POI_CMD_TOILET";
		public static final String NAV_WAY_POI_CMD_SPOTS = "NAV_WAY_POI_CMD_SPOTS";
		public static final String NAV_WAY_POI_CMD_RESTAURANT = "NAV_WAY_POI_CMD_RESTAURANT";
		public static final String NAV_WAY_POI_CMD_HOTEL = "NAV_WAY_POI_CMD_HOTEL";
		public static final String NAV_WAY_POI_CMD_SERVICE = "NAV_WAY_POI_CMD_SERVICE";
		public static final String NAV_WAY_POI_CMD_PARK = "NAV_WAY_POI_CMD_PARK";
		public static final String NAV_WAY_POI_CMD_GO_GASTATION = "NAV_WAY_POI_CMD_GASTATION";
		public static final String NAV_WAY_POI_CMD_GO_TOILET = "NAV_WAY_POI_CMD_TOILET";
		public static final String NAV_WAY_POI_CMD_GO_REPAIR = "NAV_WAY_POI_CMD_REPAIR";
		public static final String NAV_WAY_POI_CMD_GO_ATM = "NAV_WAY_POI_CMD_ATM";

		/**
		 * 回家和公司
		 */
		public static final String BACK_HOME = "BACK_HOME";
		public static final String GO_COMPANY = "GO_COMPANY";
		
		/**
		 * 新手模式和专家模式和静音模式
		 */
		public static final String MEADWAR_MODE = "MEADWAR_MODE";
		public static final String EXPORT_MODE = "EXPERT_MODE";
		public static final String MUTE_MODE = "MUTE_MODE";
		
		/**
		 * 打开和关闭电子狗
		 */
		public static final String OPEN_DOG = "OPEN_DOG";
		public static final String CLOSE_DOG = "CLOSE_DOG";
	}

	public static class AsrKeySource {
		private String mKeyType;
		private String[] mCmds;

		public AsrKeySource(String type) {
			mKeyType = type;
		}

		public AsrKeySource(String type, List<String> cmds) {
			mKeyType = type;
			int size = cmds != null ? cmds.size() : 0;

			if (mCmds == null) {
				mCmds = new String[size];
			}
			mCmds = cmds.toArray(new String[size]);
		}

		public AsrKeySource(String type, String[] cmds) {
			mKeyType = type;
			mCmds = cmds;
		}

		public String getKeyType() {
			return mKeyType;
		}

		public String[] getKeyCmds() {
			return mCmds;
		}

		public void setKeyCmds(String[] cmds) {
			mCmds = cmds;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(mKeyType);
			sb.append(",");
			if (mCmds != null) {
				for (String keyword : mCmds) {
					sb.append(keyword);
					sb.append(",");
				}
			}
			return sb.toString();
		}

		public static AsrKeySource assign(String json) {
			if (!TextUtils.isEmpty(json)) {
				String[] arrays = json.split(",");
				if (arrays != null) {
					int len = arrays.length;
					String type = arrays[0];
					String[] cmds = new String[len - 1];
					System.arraycopy(arrays, 1, cmds, 0, len - 1);
					return new AsrKeySource(type, cmds);
				}
			}
			return null;
		}

		public AsrKeySource copy() {
			AsrKeySource aks = new AsrKeySource(mKeyType);
			if (mCmds != null) {
				String[] cmds = new String[mCmds.length];
				System.arraycopy(mCmds, 0, cmds, 0, mCmds.length);
				aks.setKeyCmds(cmds);
			}
			return aks;
		}
	}

	public static class AsrSources {
		List<AsrKeySource> mAsrKeySources;

		public List<AsrKeySource> getAsrKeySources() {
			return mAsrKeySources;
		}

		public void setAsrKeySources(List<AsrKeySource> akss) {
			mAsrKeySources = akss;
		}

		public void addAsrKeySource(AsrKeySource aks) {
			if (mAsrKeySources == null) {
				mAsrKeySources = new ArrayList<AsrKeySource>();
			}
			mAsrKeySources.add(aks);
		}

		public void addAsrKeySourceByTypeKeywords(String type, List<String> keywords) {
			AsrKeySource aks = new AsrKeySource(type, keywords);
			addAsrKeySource(aks);
		}

		public void addAsrKeySourceByTypeKeywords(String type, String... keywords) {
			AsrKeySource aks = new AsrKeySource(type, keywords);
			addAsrKeySource(aks);
		}

		public void modifyAsrKeyCmds(String type, String... cmds) {
			if (mAsrKeySources != null) {
				for (AsrKeySource aks : mAsrKeySources) {
					if (aks.getKeyType().equals(type)) {
						aks.setKeyCmds(cmds);
						break;
					}
				}
			}
		}

		public void removeAsrKeySourceByType(String type) {
			if (mAsrKeySources != null) {
				for (AsrKeySource aks : mAsrKeySources) {
					if (aks.getKeyType().equals(type)) {
						mAsrKeySources.remove(aks);
						break;
					}
				}
			}
		}

		public void removeAsrKeySourceByType(Collection<String> types) {
			if (types != null) {
				for (String type : types) {
					removeAsrKeySourceByType(type);
				}
			}
		}

		public void removeAsrKeySourceByType(String... types) {
			if (types != null) {
				for (String type : types) {
					removeAsrKeySourceByType(type);
				}
			}
		}

		public byte[] toBytes() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; mAsrKeySources != null && i < mAsrKeySources.size(); i++) {
				AsrKeySource aks = mAsrKeySources.get(i);
				String json = aks.toString();
				sb.append(json);
				sb.append(";");
			}
			String json = sb.toString();
			if (!TextUtils.isEmpty(json)) {
				return json.getBytes();
			}
			return null;
		}

		public static AsrSources assign(byte[] data) {
			if (data != null) {
				String json = new String(data);
				if (!TextUtils.isEmpty(json)) {
					String[] asrJson = json.split(";");
					if (asrJson != null) {
						AsrSources as = new AsrSources();
						for (String j : asrJson) {
							AsrKeySource aks = AsrKeySource.assign(j);
							as.addAsrKeySource(aks);
						}
						return as;
					}
				}
			}
			return null;
		}

		public AsrSources copy() {
			AsrSources asPos = new AsrSources();
			List<AsrKeySource> akss = getAsrKeySources();
			if (akss != null) {
				List<AsrKeySource> aksList = new ArrayList<TXZAsrKeyManager.AsrKeySource>();
				for (AsrKeySource aks : akss) {
					aksList.add(aks.copy());
				}
				asPos.setAsrKeySources(aksList);
			}
			return asPos;
		}
	}

	AsrKeySource[] mAsrKeySources;

	/**
	 * 地图界面支持的控制指令说法
	 * 
	 * @param akss
	 *            控制指令，传null为使用默认的控制指令说法
	 * @param gdc
	 *            唤醒任务的TASKID
	 */
	/*public*/ void syncAsrkeySources(AsrKeySource[] akss, GetDataCallback gdc) {
		mAsrKeySources = akss;
		if (mAsrKeySources != null) {
			AsrSources as = new AsrSources();
			for (AsrKeySource aks : akss) {
				as.addAsrKeySource(aks);
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.syncKeySources", as.toBytes(),
					gdc);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.syncKeySources", null, gdc);
		}
	}

	String[] mTypes;

	/**
	 * 通过唤醒类型禁用该唤醒说法
	 * 
	 * @param types
	 *            要禁用的唤醒类型列表
	 * @param gdc
	 *            返回唤醒任务TASKID
	 */
	public void forbidAsrKeys(String[] types, GetDataCallback gdc) {
		mTypes = types;
		if (mTypes != null) {
			StringBuilder sb = new StringBuilder();
			for (String type : types) {
				sb.append(type);
				sb.append(",");
			}

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.forbidKeys",
					sb.toString().getBytes(), gdc);
		}
	}

	private String[] shieldKeys;

	/**
	 * 屏蔽导航的免唤醒指令，但保留着命令字识别
	 *
	 * @param keys
	 */
	public void shieldWakeupKeys(String[] keys) {
		shieldKeys = keys;
		if (shieldKeys != null) {
			StringBuilder builder = new StringBuilder();
			for (String key : shieldKeys) {
				builder.append(key);
				builder.append(",");
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.shieldWpKeys", builder.toString().getBytes(), null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.shieldWpKeys", "".getBytes(), null);
		}
	}
	
	private String[] mUnFbKeys;
	
	/**
	 * 恢复被禁用的命令字
	 * @param types
	 */
	public void unForbidKeys(String[] types) {
		mUnFbKeys = types;
		if (mUnFbKeys != null) {
			StringBuilder sb = new StringBuilder();
			for (String type : types) {
				sb.append(type);
				sb.append(",");
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.unForbidKeys",
					sb.toString().getBytes(), null);
		}
	}

	AsrKeySource[] mModifyAkss;

	/**
	 * 修改类型的唤醒说法
	 * 
	 * @param modifyArrays
	 * @param gdc
     * @see  modifyAsrKeyCmds(String type, String... cmds)
	 */
	@Deprecated
	public void modifyAsrKeyCmds(AsrKeySource[] modifyArrays, GetDataCallback gdc) {
		mModifyAkss = modifyArrays;
		if (mModifyAkss != null) {
			AsrSources as = new AsrSources();
			for (AsrKeySource aks : mModifyAkss) {
				as.addAsrKeySource(aks);
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.modify", as.toBytes(), gdc);
		}
	}

	Map<String, Set<String>> mKeyTypeMap;

	public void modifyAsrKeyCmds(String type, String... cmds) {
		if (mKeyTypeMap == null) {
			mKeyTypeMap = new HashMap<String, Set<String>>();
		}

		Set<String> cds = mKeyTypeMap.get(type);
		if (cds == null) {
			cds = new HashSet<String>();
		}

		cds.clear();
		if (cmds != null) {
			for (String cmd : cmds) {
				cds.add(cmd);
			}
		}

		mKeyTypeMap.put(type, cds);
		modifyAsrKeyCmds(mKeyTypeMap);
	}

	private void modifyAsrKeyCmds(Map<String, Set<String>> keyMaps) {
		AsrSources as = new AsrSources();
		for (String key : keyMaps.keySet()) {
			as.addAsrKeySourceByTypeKeywords(key, keyMaps.get(key).toArray(new String[keyMaps.get(key).size()]));
		}

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.asr.key.modify", as.toBytes(), null);
	}

	private boolean mHasSetCmdsTool = false;
	private CommCmdsTool mCommCmdsTool;

	public void setCommCmdsTool(CommCmdsTool tool) {
		mHasSetCmdsTool = true;
		mCommCmdsTool = tool;
		if (mCommCmdsTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sys.commcmds.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.ccw.", new CommandProcessor() {

			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (mCommCmdsTool == null) {
					return (false + "").getBytes();
				}
				if (command.equals("handle_screen")) {
					return (mCommCmdsTool.handleScreen(Boolean.parseBoolean(new String(data))) + "").getBytes();
				}
				if (command.equals("handle_front_camera")) {
					return (mCommCmdsTool.handleFrontCamera(Boolean.parseBoolean(new String(data))) + "").getBytes();
				}
				if (command.equals("handle_back_camera")) {
					return (mCommCmdsTool.handleBackCamera(Boolean.parseBoolean(new String(data))) + "").getBytes();
				}
				if (command.equals("backHome")) {
					return (mCommCmdsTool.backHome() + "").getBytes();
				}
				if (command.equals("backNavi")) {
					return (mCommCmdsTool.backNavi() + "").getBytes();
				}
				return (mCommCmdsTool.procCmd(new String(data)) + "").getBytes();
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sys.commcmds.settool", null, null);
	}

	public static abstract class CommCmdsTool {

		/**
		 * @param isOpen 打开true or 关闭false
		 * @return
		 */
		public abstract boolean handleScreen(boolean isOpen);

		/**
		 * @param isOpen 打开true or 关闭false
		 * @return
		 */
		public abstract boolean handleFrontCamera(boolean isOpen);

		/**
		 * @param isOpen 打开true or 关闭false
		 * @return
		 */
		public abstract boolean handleBackCamera(boolean isOpen);

		public boolean backHome() {
			return false;
		}

		public boolean backNavi() {
			return false;
		}

		/**
		 * 
		 * @param cmd 命中的词
		 * @return
		 */
		public boolean procCmd(String cmd){
			return false;
		}
	}
}
