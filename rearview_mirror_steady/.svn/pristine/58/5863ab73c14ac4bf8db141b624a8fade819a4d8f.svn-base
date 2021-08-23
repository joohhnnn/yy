package com.txznet.sdk.bean;

import org.json.JSONObject;

import com.txznet.comm.remote.util.LocationUtil;
import com.txznet.comm.util.JSONBuilder;

/**
 * 导航点
 * 
 * @author txz
 *
 */
public class Poi {

	public static class PoiAction {
		public static final String ACTION_NAVI = "nav";
		public static final String ACTION_HOME = "setHome";
		public static final String ACTION_COMPANY = "setCompany";
		public static final String ACTION_AUDIO = "audio";
		public static final String ACTION_HUIBI = "setHuiBi";
		public static final String ACTION_JINGYOU = "setJingYou";
		public static final String ACTION_DEL_JINGYOU = "delJingYou";
		public static final String ACTION_NAV_HISTORY = "nav_history";
		public static final String ACTION_NAV_RECOMMAND = "nav_recommand";
		public static final String ACTION_RECOMM_HOME = "recomm_home";
		public static final String ACTION_RECOMM_COMPANY = "recomm_company";
		public static final String ACTION_NAVI_END= "navEnd";
		public static final String ACTION_PASS_NAV = "passNav";// 带途经点的导航
		public static final String ACTION_WITH_FROM_POI_NAV = "withFromPoiNav";// 带起点的导航
		public static final String ACTION_NAV_COLLECTION_POINT = "nav_collection_point";// 导航去收藏点
		public static final String ACTION_THIRD_POI = "third_poi";//外放的POI界面
	}

	/**
	 * POI来源：同行者
	 */
	public final static int POI_SOURCE_TXZ = 1;
	/**
	 * POI来源：大众点评
	 */
	public final static int POI_SOURCE_DZDP = 2;
	/**
	 * POI来源：高德
	 */
	public final static int POI_SOURCE_GAODE_IMPL = 3;
	public final static int POI_SOURCE_GAODE_LOCAL = 4;
	public final static int POI_SOURCE_GAODE_WEB = 5;
	/**
	 * POI来源：百度
	 */
	public final static int POI_SOURCE_BAIDU_IMPL = 6;
	public final static int POI_SOURCE_BAIDU_LOCAL = 7;
	public final static int POI_SOURCE_BAIDU_WEB = 8;
	/**
	 * POI来源：凯立德
	 */
	public final static int POI_SOURCE_KAILIDE = 9;
	/**
	 * POI来源：腾讯
	 */
	public final static int POI_SOURCE_TENCENT = 10;
	/**
	 * POI来源：美行
	 */
	public final static int POI_SOURCE_MEIXING = 11;
	/**
	 * POI来源：360好搜
	 */
	public final static int POI_SOURCE_QIHOO = 12;
	/**
	 * POI来源：同行者后台
	 */
	public final static int POI_SOURCE_TXZ_POI = 13;
	/**
	 * POI来源：后台大众点评结果
	 */
	public final static int POI_SOURCE_TXZ_DZDP = 14;
	/**
	 * POI来源：后台美团北极星结果
	 */
	public final static int POI_SOURCE_TXZ_BEIJIXING = 15;
	/**
	 * Poi类型
	 */
	public final static int POI_TYPE_POIDEATAIL = 1;
	/**
	 * 大众点评类型
	 */
	public final static int POI_TYPE_BUSINESS = 2;
	/**
	 * TXZ置顶类型
	 */
	public final static int POI_TYPE_TXZ = 3;
	
	public static enum CoordType {
		// 百度坐标
		BAIDU,
		// 国测局
		GCJ02
	}

	/**
	 * 纬度，gcj02坐标系
	 */
	double lat;
	/**
	 * 经度，gcj02坐标系
	 */
	double lng;
	/**
	 * 距离，单位米
	 */
	int distance;
	/**
	 * POI名称
	 */
	String name;
	/**
	 * 所属城市
	 */
	String city;
	/**
	 * 地理信息字符串
	 */
	String geo;
	/**
	 * 别名
	 */
	String[] alias;

	/**
	 * 坐标点对应的应用场景
	 */
	String action;
	
	/**
	 * 携带额外数据
	 */
	String extraStr;

	/**
	 * 坐标类型
	 */
	CoordType type = CoordType.GCJ02;
	
	/**
	 * Poi来源
	 */
	int source = POI_SOURCE_TXZ;
	
	/**
	 * Poi类型（商圈、Poi等）
	 */
	int poiType = POI_TYPE_POIDEATAIL;

	/**
	 * 获取纬度（国测局坐标）
	 * 
	 * @return
	 */
	public double getLat() {
		if (type == CoordType.BAIDU) {
			double[] orgLatLng = LocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			if (orgLatLng == null) {
				return lat;
			}
			return orgLatLng[0];
		}

		return lat;
	}

	/**
	 * 设置纬度
	 * 
	 * @param lat
	 */
	public Poi setLat(double lat) {
		this.lat = lat;
		return this;
	}

	/**
	 * 获取经度（国测局坐标）
	 * 
	 * @return
	 */
	public double getLng() {
		if (type == CoordType.BAIDU) {
			double[] orgLatLng = LocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			if (orgLatLng == null) {
				return lng;
			}
			return orgLatLng[1];
		}
		return lng;
	}

	/**
	 * 返回原始的坐标
	 * 
	 * @return
	 */
	public double getOriginalLat() {
		return lat;
	}

	/**
	 * 返回原始的Lng坐标
	 * 
	 * @return
	 */
	public double getOriginalLng() {
		return lng;
	}

	/**
	 * 设置经度
	 * 
	 * @param lng
	 */
	public Poi setLng(double lng) {
		this.lng = lng;
		return this;
	}

	/**
	 * 获取距离
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * 设置距离
	 * 
	 * @param distance
	 */
	public Poi setDistance(int distance) {
		this.distance = distance;
		return this;
	}

	/**
	 * 获取名字
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置名字
	 * 
	 * @param name
	 */
	public Poi setName(String name) {
		this.name = name;
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
	public Poi setCity(String city) {
		this.city = city;
		return this;
	}

	/**
	 * 获取地理信息
	 * 
	 * @return
	 */
	public String getGeoinfo() {
		return geo;
	}

	/**
	 * 设置地理信息
	 * 
	 * @param geoinfo
	 */
	public Poi setGeoinfo(String geoinfo) {
		this.geo = geoinfo;
		return this;
	}

	/**
	 * 获取别名
	 * 
	 * @return
	 */
	public String[] getAlias() {
		return this.alias;
	}

	/**
	 * 设置别名
	 * 
	 * @param alias
	 */
	public Poi setAlias(String[] alias) {
		this.alias = alias;
		return this;
	}

	/**
	 * 获取Poi点类型
	 * 
	 * @return
	 */
	public String getAction() {
		return this.action;
	}

	/**
	 * 设置类型
	 * 
	 * @return
	 */
	public Poi setAction(String action) {
		this.action = action;
		return this;
	}
	
	/**
	 * 获取携带的额外数据
	 * @return
	 */
	public String getExtraStr(){
		return this.extraStr;
	}
	
	/**
	 * 设置携带的String信息
	 * @param extra
	 * @return
	 */
	public Poi setExtraStr(String extra){
		this.extraStr = extra;
		return this;
	}

	/**
	 * 设置坐标类型
	 * 
	 * @param type
	 * @return
	 */
	public Poi setCoordType(CoordType type) {
		this.type = type;
		return this;
	}

	/**
	 * 获取坐标类型
	 * 
	 * @return
	 */
	public CoordType getType() {
		return type;
	}
	
	/**
	 * 获取来源类型
	 * @return
	 */
	public int getSourceType() {
		return source;
	}
	
	/**
	 * 设置来源类型
	 * @return
	 */
	public Poi setSourceType(int source) {
		this.source = source;
		return this;
	}
	
	/**
	 * 获取Poi类型（商圈、poi等）
	 * @return
	 */
	public int getPoiType(){
		return poiType;
	}

	/**
	 * 设置Poi类型 （商圈等）
	 * @param type
	 * @return
	 */
	public Poi setPoiType(int type) {
		this.poiType = type;
		return this;
	}

	protected JSONBuilder toJsonObj() {
		JSONBuilder json = new JSONBuilder();
		json.put("lat", getLat());
		json.put("lng", getLng());
		json.put("city", getCity());
		json.put("name", getName());
		json.put("geo", getGeoinfo());
		json.put("distance", getDistance());
		json.put("action", getAction());
		json.put("coordtype", getType());
		json.put("extre", getExtraStr());
		json.put("source", getSourceType());
		json.put("poitype", poiType);
		return json;
	}

	public String toString() {
		JSONBuilder json = toJsonObj();
		return json.toString();
	}

	public JSONObject toJsonObject() {
		JSONBuilder json = toJsonObj();
		return json.build();
	}

	protected void fromJsonObject(JSONBuilder json) {
		this.lat = json.getVal("lat", Double.class, 0.0);
		this.lng = json.getVal("lng", Double.class, 0.0);
		this.city = json.getVal("city", String.class);
		this.name = json.getVal("name", String.class);
		this.geo = json.getVal("geo", String.class);
		this.distance = json.getVal("distance", Integer.class, 0);
		this.action = json.getVal("action", String.class);
		String type = json.getVal("coordtype", String.class);
		this.source = json.getVal("source", Integer.class, POI_SOURCE_TXZ);
		this.extraStr= json.getVal("extre", String.class);
		this.poiType = json.getVal("poitype", Integer.class, POI_TYPE_POIDEATAIL);
		if (type != null && !type.equals("")) {
			// TODO 默认只有百度和国测局的转换
			if (type.equals("BAIDU")) {
				this.type = CoordType.BAIDU;
			} else {
				this.type = CoordType.GCJ02;
			}
		} else {
			this.type = CoordType.GCJ02;
		}
	}

	public static Poi fromString(String data) {
		Poi p = new Poi();
		JSONBuilder json = new JSONBuilder(data);
		p.fromJsonObject(json);
		return p;
	}

	private static final double PI = 3.14159265358979324;
	private static double X_PI = PI * 3000.0 / 180.0;

	/**
	 * 将百度坐标系转为国测局坐标，返回值下标0为Lat，1为Lng
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static double[] Convert_BD09_To_GCJ02(double lat, double lng) {
		double x = lng - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
		lng = z * Math.cos(theta);
		lat = z * Math.sin(theta);
		double[] point = new double[2];
		point[0] = lat;
		point[1] = lng;
		return point;
	}
}