package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.sdk.bean.TxzPoi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Poi搜索管理器
 *
 */
public class TXZPoiSearchManager {
	private static TXZPoiSearchManager sInstance = new TXZPoiSearchManager();

	private TXZPoiSearchManager() {

	}

	/**
	 * 获取单例
	 */
	public static TXZPoiSearchManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetPoiSearchTool)
			if (mPoiSearchToolType == null) {
				setPoiSearchTool((PoiSearchToolType) null);
			} else if (mPoiSearchToolType instanceof PoiSearchToolType) {
				setPoiSearchTool((PoiSearchToolType) mPoiSearchToolType);
			}
		if (mPoiTool != null) {
			setPoiSearchTool(mPoiTool, mPoiConfig);
		}
		if (mIsUseList != null) {
			setPoiSearchResultList(mIsUseList);
		}
		if (mEnableMapView != null) {
			setMapPoiViewEnable(mEnableMapView);
		}
		if (mIsPlanning != null) {
			setGaoDeAutoPlanningRoute(mIsPlanning);
		}
		if (mIsPlayPoiTip != null) {
			setPoiPlayTipTts(mIsPlayPoiTip);
		}
		if (bNeedSearchingTip != null) {
			setNeedSearchingTip(bNeedSearchingTip);
		}
		if (mOnPoiViewStateListener != null) {
			setOnPoiViewStateListener(mOnPoiViewStateListener);
		}
	}

	/**
	 * 未知错误，通用错误
	 */
	public final static int ERROR_CODE_UNKNOW = 1;
	/**
	 * 结果为空
	 */
	public final static int ERROR_CODE_EMPTY = 2;
	/**
	 * 发生超时
	 */
	public final static int ERROR_CODE_TIMEOUT = 3;
	/**
	 * 部分功能因为导航没有打开而没有实现
	 */
	public final static int ERROR_CODE_NAVICLOSE = 4;
	/**
	 * 周边搜索默认距离，
	 * 大众点评最大搜索半径5000
	 * 高德周边搜索最大半径取值范围为：0-5000 https://lbs.amap.com/api/javascript-api/reference/search
	 * 百度周边检索半径，单位为米 当半径过大，超过中心点所在城市边界时，会变为城市范围检索，检索范围为中心点所在城市 http://mapopen-pub-androidsdk.cdn.bcebos.com/map/doc/v5.4.1/index.html
	 *
	 */
	public final static int DEFAULT_NEARBY_RADIUS = 5000;

	/**
	 * 默认搜索超时
	 */
	public final static int DEFAULT_SEARCH_TIMEOUT = 10000;

	/**
	 * 默认搜索数量
	 * 高德返回搜索结果数量的取值范围为1-50，默认是10 https://lbs.amap.com/api/javascript-api/reference/search
	 * 百度返回搜索结果数量的取值范围为1-20，默认是10 http://mapopen-pub-androidsdk.cdn.bcebos.com/map/doc/v5.4.1/index.html
	 */
	public final static int DEFAULT_SEARCH_AMOUNT = 10;

	/**
	 * 主页目的地搜索
	 */
	public void startSearch(String strDest, String city) {
		JSONObject json = new JSONObject();
		try {
			json.put("keywords", strDest);
			json.put("city", city);
		} catch (Exception ignored) {
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.poiSearch", json.toString().getBytes(), null);
	}

	/**
	 * POI搜索建议类
	 */
	public static class SearchPoiSuggestion {
		List<String> city;
		List<String> keywrods;

		/**
		 * 设置建议城市
		 */
		public SearchPoiSuggestion setCity(List<String> city) {
			this.city = city;
			return this;
		}

		/**
		 * 设置建议关键字
		 */
		public SearchPoiSuggestion setKeywrods(List<String> keywrods) {
			this.keywrods = keywrods;
			return this;
		}

		/**
		 * 获取建议城市
		 */
		public List<String> getCity() {
			return city;
		}

		/**
		 * 获取建议关键字
		 */
		public List<String> getKeywrods() {
			return keywrods;
		}
	}

	/**
	 * POI搜索结果回调类
	 *
	 */
	public static interface PoiSearchResultListener {
		public void onError(int errCode, String errDesc);

		public void onSuggestion(SearchPoiSuggestion suggestion);

		public void onResult(List<Poi> result);
	}

	/**
	 * 搜索请求类，用于取消操作
	 */
	public static interface SearchReq {
		public void cancel();
	}

	/**
	 *
	 * POI搜索相关信息
	 *
	 */
	public static class PoiSearchInfo{
		//后台搜索是否完成
		boolean txzPoiToolComplete=true;
		//poi工具前置过滤，将13位和第一位置0，默认关闭后台搜索
		int poiSourceConf=(~4097);
		//poi结果后置过滤
		int disShowEngine=0;
		//每一次搜索的重试次数
		int poiRetryCount=1;
		public boolean isTxzPoiToolComplete() {
			return txzPoiToolComplete;
		}
		public void setTxzPoiToolComplete(boolean txzPoiToolComplete) {
			this.txzPoiToolComplete = txzPoiToolComplete;
		}
		public int getPoiSourceConf() {
			return poiSourceConf;
		}
		public int getPoiRetryCount() {
			return poiRetryCount;
		}
		public void setPoiRetryCount(int poiRetryCount) {
			this.poiRetryCount = poiRetryCount;
		}
		public void setPoiSourceConf(int poiSourceConf) {
			this.poiSourceConf = poiSourceConf;
		}
		public int getDisShowEngine() {
			return disShowEngine;
		}
		public void setDisShowEngine(int disShowEngine) {
			this.disShowEngine = disShowEngine;
		}


	}


	/**
	 * POI搜索基础选项
	 *
	 */
	public static class PoiSearchOption {
		protected int num = DEFAULT_SEARCH_AMOUNT;
		protected String keywords;
		protected boolean mUseCurrentCity = false;

		public boolean isUseCurrentCity() {
			return mUseCurrentCity;
		}

		public PoiSearchOption setUseCurrentCity(final boolean useCurrentCity) {
			mUseCurrentCity = useCurrentCity;
			return this;
		}

		protected int timeout = DEFAULT_SEARCH_TIMEOUT;
		private PoiSearchInfo info= new PoiSearchInfo();
		/**
		 * 设置搜索超时
		 *
		 * @param timeout
		 *            超时时间，单位ms
		 * @return
		 */
		public PoiSearchOption setTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * 获取超时时间
		 *
		 * @return
		 */
		public int getTimeout() {
			return timeout;
		}

		/**
		 * 获取关键字
		 *
		 */
		public String getKeywords() {
			return keywords;
		}

		/**
		 * 设置关键字
		 */
		public PoiSearchOption setKeywords(String keywords) {
			this.keywords = keywords;
			return this;
		}

		/**
		 * 获取结果数量
		 */
		public int getNum() {
			return num;
		}

		/**
		 * 设置结果数量
		 */
		public PoiSearchOption setNum(int num) {
			this.num = num;
			return this;
		}

		public PoiSearchInfo getSearchInfo(){
			return info;
		}
		public PoiSearchInfo setSearchInfo(PoiSearchInfo info){
			return this.info=info;
		}
	}

	/**
	 * 城市搜索选项
	 *
	 * @author txz
	 *
	 */
	public static class CityPoiSearchOption extends PoiSearchOption {
		protected String city;
		protected String region;
		/**
		 * 设置搜索超时
		 *
		 * @param timeout
		 *            超时时间，单位ms
		 * @return
		 */
		public CityPoiSearchOption setTimeout(int timeout) {
			super.setTimeout(timeout);
			return this;
		}

		/**
		 * 获取城市
		 *
		 * @return
		 */
		public String getCity() {
			return city;
		}

		/**
		 * 设置城市
		 *
		 * @param city
		 */
		public CityPoiSearchOption setCity(String city) {
			this.city = city;
			return this;
		}

		/**
		 * 获取地区
		 *
		 * @return
		 */
		public String getRegion() {
			return region;
		}

		/**
		 * 设置地区
		 *
		 * @param region
		 */
		public CityPoiSearchOption setRegion(String region) {
			this.region = region;
			return this;
		}

		/**
		 * 设置关键字
		 */
		@Override
		public CityPoiSearchOption setKeywords(String keywords) {
			super.setKeywords(keywords);
			return this;
		}

		/**
		 * 设置结果数量
		 */
		@Override
		public CityPoiSearchOption setNum(int num) {
			super.setNum(num);
			return this;
		}
	}

	/**
	 * 周边搜索选项
	 *
	 * @author txz
	 *
	 */
	public static class NearbyPoiSearchOption extends CityPoiSearchOption {
		protected double lat;
		protected double lng;
		protected int radius = -1;

		/**
		 * 设置搜索超时
		 *
		 * @param timeout
		 *            超时时间，单位ms
		 * @return
		 */
		public CityPoiSearchOption setTimeout(int timeout) {
			super.setTimeout(timeout);
			return this;
		}

		/**
		 * 获取搜索半径，单位米
		 */
		public int getRadius() {
			return radius;
		}

		/**
		 * 设置搜索半径，单位米
		 */
		public NearbyPoiSearchOption setRadius(int radius) {
			this.radius = radius;
			return this;
		}

		/**
		 * 获取中心纬度
		 */
		public double getCenterLat() {
			return lat;
		}

		/**
		 * 设置中心纬度
		 */
		public NearbyPoiSearchOption setCenterLat(double lat) {
			this.lat = lat;
			return this;
		}

		/**
		 * 获取中心经度
		 */
		public double getCenterLng() {
			return lng;
		}

		/**
		 * 设置中心经度
		 */
		public NearbyPoiSearchOption setCenterLng(double lng) {
			this.lng = lng;
			return this;
		}

		/**
		 * 设置关键字
		 */
		@Override
		public NearbyPoiSearchOption setKeywords(String keywords) {
			super.setKeywords(keywords);
			return this;
		}

		/**
		 * 设置结果数量
		 */
		@Override
		public NearbyPoiSearchOption setNum(int num) {
			super.setNum(num);
			return this;
		}

		/**
		 * 设置搜索城市
		 */
		@Override
		public NearbyPoiSearchOption setCity(String city) {
			super.setCity(city);
			return this;
		}
	}

	public static class BoundPoiSearchOption extends CityPoiSearchOption {
		protected double minLat;
		protected double maxLat;
		protected double minLng;
		protected double maxLng;

		public double getMinLat() {
			return minLat;
		}

		public BoundPoiSearchOption setMinLat(double minLat) {
			this.minLat = minLat;
			return this;
		}

		public double getMaxLat() {
			return maxLat;
		}

		public BoundPoiSearchOption setMaxLat(double maxLat) {
			this.maxLat = maxLat;
			return this;
		}

		public double getMinLng() {
			return minLng;
		}

		public BoundPoiSearchOption setMinLng(double minLng) {
			this.minLng = minLng;
			return this;
		}

		public double getMaxLng() {
			return maxLng;
		}

		public BoundPoiSearchOption setMaxLng(double maxLng) {
			this.maxLng = maxLng;
			return this;
		}

		/**
		 * 设置关键字
		 */
		@Override
		public BoundPoiSearchOption setKeywords(String keywords) {
			super.setKeywords(keywords);
			return this;
		}

		/**
		 * 设置结果数量
		 */
		@Override
		public BoundPoiSearchOption setNum(int num) {
			super.setNum(num);
			return this;
		}

		/**
		 * 设置搜索城市
		 */
		@Override
		public BoundPoiSearchOption setCity(String city) {
			super.setCity(city);
			return this;
		}
	}

	/**
	 * Poi搜索工具
	 */
	public static interface PoiSearchTool {
		/**
		 * 城市poi搜索
		 */
		public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener);

		/**
		 * 周边poi搜索
		 */
		public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener);
		/**
		 * 终止工具搜索并返回空值
		 */
		public void stopPoiSearchTool(int disShowPoiType);

		/**
		 * 获取搜索工具类型
		 */
		public int getPoiSearchType();
	}

	private boolean mHasSetPoiSearchTool = false;
	private Object mPoiSearchToolType = null;
	private PoiTool mPoiTool;
	private PoiConfig mPoiConfig;

	/**
	 * 设置poi搜索工具类
	 *
	 * @param tool
	 */
	void setPoiSearchTool(PoiSearchTool tool) {

	}

	/**
	 *	内置poi搜索工具类型
	 */
	public static enum PoiSearchToolType {
		/**
		 * 同行者
		 */
		TXZ,
		/**
		 * 360地图
		 */
		QIHOO
	}

	public void setPoiSearchTool(PoiSearchToolType type) {
		mHasSetPoiSearchTool = true;
		mPoiSearchToolType = type;
		if (type == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.cleartool", null, null);
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.setInnerTool", type.name().getBytes(),
				null);
	}

	private Boolean mIsUseList = null;
	/**
	 * 设置POI显示的默认模式
	 * @param isList
	 */
	public void setPoiSearchResultList(boolean isList){
		mIsUseList = isList;
		JSONObject json = new JSONObject();
		try {
			json.put("isList", isList);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.setShowModel", json.toString().getBytes(),
				null);
	}

	private Boolean mEnableMapView = null;
	/**
	 * 禁用地图模式显示
	 * @param isEnable
	 */
	public void setMapPoiViewEnable(boolean isEnable){
		mEnableMapView = isEnable;
		JSONObject json = new JSONObject();
		try {
			json.put("isEnable", isEnable);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.stopMapPoiViewModle",json.toString().getBytes(),
				null);
	}

	private Boolean mIsPlanning = null;
	/**
	 * 设置高德选择POI后是否进入路线规划界面
	 * @param isPlanning
	 */
	public void setGaoDeAutoPlanningRoute(boolean isPlanning){
		mIsPlanning = isPlanning;
		JSONObject json = new JSONObject();
		try {
			json.put("isPlanning", isPlanning);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.setGaoDeAutoPlanningRoute", json.toString().getBytes(),
				null);
	}

	private Boolean mIsPlayPoiTip = null;
	/**
	 * 设置POI列表是否播报注册唤醒词的TTs提醒
	 * @param isPlayPoiTip
	 */
	public void setPoiPlayTipTts(boolean isPlayPoiTip){
		mIsPlayPoiTip = isPlayPoiTip;
		JSONObject json = new JSONObject();
		try {
			json.put("isPlayPoiTip", isPlayPoiTip);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.setPoiPlayTipTts", json.toString().getBytes(),
				null);
	}

	public void navNearbyPoint(){
		LogUtil.logd("TXZPoiSearchManager navNearbyPoint");
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.poi.nearbyPoint",null, null);
	}

	Boolean bNeedSearchingTip = null;
	/**
	 * 设置不直接提示poi搜索信息，当一段时间没出结果再进行提示
	 * @param bNeedSearchingTip
	 */
	public void setNeedSearchingTip(boolean bNeedSearchingTip){
		LogUtil.logd("TXZPoiSearchManager setNeedSearchingTip = " + bNeedSearchingTip);
		this.bNeedSearchingTip = bNeedSearchingTip;
		JSONObject json = new JSONObject();
		try {
			json.put("bNeedSearchingTip", bNeedSearchingTip);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.poi.searchingTip", json.toString().getBytes(), null);
	}

	public static class PoiDisplayStyle {

		/**
		 * 为true使用声控聊天界面显示Poi结果列表，false为单独开启结果显示页
		 */
		public boolean mPoiResultDisplayWinRecord = true;

		/**
		 * 当mPoiResultDisplayWinRecord为false时有效，如果该值为true，当搜索结果为空时显示二维码，否则不显示
		 */
		public boolean mShowQRCodeWhenNoResult = true;

	}

	/**
	 * 设置poi搜索工具类
	 *
	 * @param tool
	 */
	public void setPoiSearchTool(PoiTool tool, PoiConfig poiConfig) {
		mPoiTool = tool;
		mPoiConfig = poiConfig;
		if (tool == null) {
			TXZService.setCommandProcessor("tool.poi.", null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.tool.poi.clearTool", null, null);
		} else {
			TXZService.setCommandProcessor("tool.poi.", new TXZService.CommandProcessor() {
				@Override
				public byte[] process(String packageName, String command, byte[] data) {
					if ("search".equals(command)) {
						LogUtil.logd("poi tool search");
						if (data == null) {
							return null;
						}
						JSONBuilder json = new JSONBuilder(data);
						final int id = json.getVal("id", Integer.class, -1);
						if (id <= 0) {
							return null;
						}
						PoiOption option = PoiOption.parse(json);
						if (option == null) {
							return null;
						}
						mPoiTool.search(option, new onPoiSearchListener() {
							@Override
							public void onPoiSearched(PoiResult result) {
								JSONObject jsonObject = new JSONObject();
								try {
									jsonObject.put("id", id);
									if (result != null) {
                                        jsonObject.put("result", result.toJson());
                                    }
								} catch (JSONException e) {
									e.printStackTrace();
								}
								ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,"txz.tool.poi.result", jsonObject.toString().getBytes(), null);
							}
						});
					} else if ("cancel".equals(command)) {
						LogUtil.logd("poi tool cancel");
						mPoiTool.cancel();
					}
					return null;
				}
			});
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.tool.poi.setTool", PoiConfig.getBytes(poiConfig), null);
		}
	}

	public static class PoiConfig {

		public NetMode netMode = NetMode.NET_MODE_ALL;
		/**
		 * 是否使用内置工具作为补充搜索
		 */
		public boolean isUseOption = true;

		static byte[] getBytes(PoiConfig poiConfig) {
			JSONBuilder jsonBuilder = new JSONBuilder();
			if (poiConfig != null) {
				if (poiConfig.netMode != null) {
					jsonBuilder.put("netMode", poiConfig.netMode.toString());
				}
				jsonBuilder.put("isUseOption", poiConfig.isUseOption);
			}
			return jsonBuilder.toBytes();
		}

		public static PoiConfig parse(byte[] data) {
			PoiConfig poiConfig = new PoiConfig();
			JSONBuilder builder = new JSONBuilder(data);
			String strNetMode = builder.getVal("netMode", String.class);
			if (!TextUtils.isEmpty(strNetMode)) {
				poiConfig.netMode = NetMode.valueOf(strNetMode);
			}
			poiConfig.isUseOption = builder.getVal("isUseOption", Boolean.class, true);
			return poiConfig;
		}

		public enum NetMode {
			/**
			 * 有网络情况下，使用该搜素工具
			 */
			NET_MODE_ONLINE,
			/**
			 * 无网情况下，使用该搜素工具
			 */
			NET_MODE_OFFLINE,
			/**
			 * 无论有没有网络，都使用该搜素工具
			 */
			NET_MODE_ALL
		}

	}

	public interface PoiTool {
		void search(PoiOption option, onPoiSearchListener listener);

		void cancel();
	}

	public interface onPoiSearchListener {
		void onPoiSearched(PoiResult result);
	}

	public static class PoiResult {

		public final static int ERROR_CODE_SUCCESS = 0;

		/**
		 * 未知错误，通用错误
		 */
		public final static int ERROR_CODE_UNKNOW = TXZPoiSearchManager.ERROR_CODE_UNKNOW;
		/**
		 * 结果为空
		 */
		public final static int ERROR_CODE_EMPTY = TXZPoiSearchManager.ERROR_CODE_EMPTY;
		/**
		 * 发生超时
		 */
		public final static int ERROR_CODE_TIMEOUT = TXZPoiSearchManager.ERROR_CODE_TIMEOUT;

		public int errorCode;

		public String errDesc = "";

		public List<Poi> pois;

		JSONObject toJson() {
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("errorCode", this.errorCode);
			jsonBuilder.put("errDesc", this.errDesc);
			JSONArray jsonArray = new JSONArray();
			if (pois != null && pois.size() != 0) {
				for (int i = 0; i < pois.size(); i++) {
					Poi poi = pois.get(i);
					if (poi != null) {
						jsonArray.put(poi.toJsonObject());
					}
				}
			}
			jsonBuilder.put("pois", jsonArray);
			return jsonBuilder.build();
		}

		public static PoiResult parse(JSONObject jsonObject) {
			if (jsonObject == null) {
				return null;
			}
			PoiResult result = new PoiResult();
			result.errorCode = jsonObject.optInt("errorCode");
			result.errDesc = jsonObject.optString("errDesc");
			JSONArray obJsonArray = jsonObject.optJSONArray("pois");
			if (obJsonArray != null) {
				result.pois = new ArrayList<Poi>(obJsonArray.length());
				for (int i = 0; i < obJsonArray.length(); i++) {
					JSONObject jo = obJsonArray.optJSONObject(i);
					if (jo == null) {
						continue;
					}
					String objJson = jo.toString();
					int poitype = jo.optInt("poitype");
					Poi poi;
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
						default:
							poi = Poi.fromString(objJson);
					}
					poi.setSourceType(0);
					result.pois.add(poi);
				}
			}
				return result;
			}


	}

	public static class PoiOption {

		public enum PoiSearchType {
			/** 城市搜索 */
			TYPE_CITY,
			/** 参考点中心搜索 */
			TYPE_CENTER
		}

		/** 结果数量 */
		private int num;
		/** 搜索关键字 */
		private String keyword;
		/** 超时时间 */
		private int timeout;
		/** 城市 */
		private String city;
		/** 地区 */
		private String region;
		private boolean useCurrentCity;

        public boolean isUseCurrentCity() {
			return useCurrentCity;
		}

        public void setUseCurrentCity(final boolean useCurrentCity) {
			this.useCurrentCity = useCurrentCity;
		}

		private PoiSearchType poiSearchType = PoiSearchType.TYPE_CITY;

		/** 中心点纬度 */
		private double centerLatitude;
		/** 中心点经度 */
		private double centerLongitude;
		private int radius;

		public int getTimeout() {
			return timeout;
		}

		public String getKeyword() {
			return keyword;
		}

		public int getNum() {
			return num;
		}

		public String getCity() {
			return city;
		}

		public String getRegion() {
			return region;
		}

		public PoiSearchType getPoiSearchType() {
			return poiSearchType;
		}

		public double getCenterLatitude() {
			return centerLatitude;
		}

		public double getCenterLongitude() {
			return centerLongitude;
		}

		public int getRadius() {
			return radius;
		}

		public PoiOption setTimeout(int timeout) {
			this.timeout = timeout;
			return this;
		}

		public PoiOption setKeyword(String keyword) {
			this.keyword = keyword;
			return this;
		}

		public PoiOption setNum(int num) {
			this.num = num;
			return this;
		}

		public PoiOption setCity(String city) {
			this.city = city;
			return this;
		}

		public PoiOption setRegion(String region) {
			this.region = region;
			return this;
		}

		public PoiOption setPoiSearchType(PoiSearchType poiSearchType) {
			this.poiSearchType = poiSearchType;
			return this;
		}

		public PoiOption setCenterLatitude(double centerLatitude) {
			this.centerLatitude = centerLatitude;
			return this;
		}

		public PoiOption setCenterLongitude(double centerLongitude) {
			this.centerLongitude = centerLongitude;
			return this;
		}

		public PoiOption setRadius(int radius) {
			this.radius = radius;
			return this;
		}

		protected static PoiOption parse(JSONBuilder jsonBuilder) {
			if (jsonBuilder == null) {
				return null;
			}
			PoiOption option = new PoiOption();
			option.num = jsonBuilder.getVal("num", Integer.class, DEFAULT_SEARCH_AMOUNT);
			option.timeout = jsonBuilder.getVal("timeout", Integer.class, DEFAULT_SEARCH_TIMEOUT);

			option.keyword = jsonBuilder.getVal("keyword",String.class);
			option.useCurrentCity = jsonBuilder.getVal("useCurrentCity", Boolean.class);
			if (TextUtils.isEmpty(option.keyword)) {
				return null;
			}
			option.city = jsonBuilder.getVal("city",String.class);
			option.region = jsonBuilder.getVal("region",String.class);
			boolean hasCenter = jsonBuilder.getVal("hasCenter",Boolean.class, false);
			if (hasCenter) {
				option.poiSearchType = PoiSearchType.TYPE_CENTER;
				option.centerLatitude = jsonBuilder.getVal("centerLatitude", Double.class, 0.0);
				option.centerLongitude = jsonBuilder.getVal("centerLongitude", Double.class, 0.0);
				option.radius = jsonBuilder.getVal("radius", Integer.class, -1);
			} else {
				option.poiSearchType = PoiSearchType.TYPE_CITY;
			}
			return option;
		}
	}


	/**
	 * poi列表状态监听
	 */
	public static class OnPoiViewStateListener {
		/**
		 * 列表展示
		 *
		 * @param action poi的类型
		 * @param size   poi数据的大小
		 */
		public void onShow(String action, int size) {
		}

		/**
		 * tts播报开始
		 */
		public void onTtsStart() {
		}

		/**
		 * tts播报结束
		 */
		public void onTtsEnd() {
		}

		/**
		 * 列表选中
		 *
		 * @param index 当前页索引
		 */
		public void onSelect(int index) {
		}

		/**
		 * 翻页
		 *
		 * @param page 页面索引
		 */
		public void onPageChange(int page) {
		}

		/**
		 * 点击打开城市编辑界面
		 */
		public void onClickEditCity() {
		}

		/**
		 * 点击打开poi编辑界面内
		 */
		public void onClickEditPoi() {
		}

		/**
		 * 取消选择
		 */
		public void onCancel() {
		}

		/**
		 * 列表关闭
		 */
		public void onDismiss() {
		}
	}

	private OnPoiViewStateListener mOnPoiViewStateListener;

	public static final String CMD_PREFIX_POIVIEW_STATE = "txz.poi.poiview.status.";
	public static final String CMD_POIVIEW_ON_SHOW = "onShow";
	public static final String CMD_POIVIEW_ON_TTS_START = "onTtsStart";
	public static final String CMD_POIVIEW_ON_TTS_END = "onTtsEnd";
	public static final String CMD_POIVIEW_ON_SELECT = "onSelect";
	public static final String CMD_POIVIEW_ON_PAGE_CHANGE = "onPageChange";
	public static final String CMD_POIVIEW_ON_CLICK_EDIT_CITY = "onClickEditCity";
	public static final String CMD_POIVIEW_ON_CLICK_EDIT_POI = "onClickEditPoi";
	public static final String CMD_POIVIEW_ON_CANCEL = "onCancel";
	public static final String CMD_POIVIEW_ON_DISMISS = "onDismiss";

	public static final String INVOKE_PREFIX_POIVIEW_STATE_SET_LISTENER =
			"txz.poi.poiview.status.setListener";
	public static final String INVOKE_PREFIX_POIVIEW_STATE_CLEAR_LISTENER =
			"txz.poi.poiview.status.clearListener";

	/**
	 * 设置poi列表状态监听
	 *
	 * @param onPoiViewStateListener
	 */
	public void setOnPoiViewStateListener(
			final OnPoiViewStateListener onPoiViewStateListener) {
		mOnPoiViewStateListener = onPoiViewStateListener;
		if (mOnPoiViewStateListener == null) {
			TXZService.setCommandProcessor(CMD_PREFIX_POIVIEW_STATE, null);
			ServiceManager.getInstance()
					.sendInvoke(ServiceManager.TXZ, INVOKE_PREFIX_POIVIEW_STATE_CLEAR_LISTENER,
							null, null);
			return;
		}
		TXZService.setCommandProcessor(CMD_PREFIX_POIVIEW_STATE,
				new TXZService.CommandProcessor() {
					@Override
					public byte[] process(final String packageName, final String command,
							final byte[] data) {
						if (mOnPoiViewStateListener != null) {
							if (TextUtils.equals(command, CMD_POIVIEW_ON_SHOW)) {
								if (data != null && data.length != 0) {
									JSONBuilder jsonBuilder = new JSONBuilder(data);
									String action = jsonBuilder.getVal("action", String.class);
									int size = jsonBuilder.getVal("size", Integer.class, 0);
									mOnPoiViewStateListener.onShow(action, size);
								}
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_TTS_START)) {
								mOnPoiViewStateListener.onTtsStart();
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_TTS_END)) {
								mOnPoiViewStateListener.onTtsEnd();
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_SELECT)) {
								if (data != null && data.length != 0) {
									JSONBuilder jsonBuilder = new JSONBuilder(data);
									int index = jsonBuilder.getVal("index", Integer.class, 0);
									mOnPoiViewStateListener.onSelect(index);
								}
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_PAGE_CHANGE)) {
								if (data != null && data.length != 0) {
									JSONBuilder jsonBuilder = new JSONBuilder(data);
									int page = jsonBuilder.getVal("page", Integer.class, 0);
									mOnPoiViewStateListener.onPageChange(page);
								}
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_CLICK_EDIT_CITY)) {
								mOnPoiViewStateListener.onClickEditCity();
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_CLICK_EDIT_POI)) {
								mOnPoiViewStateListener.onClickEditPoi();
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_CANCEL)) {
								mOnPoiViewStateListener.onCancel();
							} else if (TextUtils.equals(command, CMD_POIVIEW_ON_DISMISS)) {
								mOnPoiViewStateListener.onDismiss();
							}
						}
						return new byte[0];
					}
				});
		ServiceManager.getInstance()
				.sendInvoke(ServiceManager.TXZ, INVOKE_PREFIX_POIVIEW_STATE_SET_LISTENER, null,
						null);
	}
}
