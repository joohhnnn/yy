package com.txznet.sdk.bean;

import com.txznet.comm.util.JSONBuilder;

/**
 * 导航点
 * 
 * @author txz
 *
 */
public class PoiDetail extends Poi {
	
	public PoiDetail() {
		setPoiType(POI_TYPE_POIDEATAIL);
	}
	
	/**
	 * 所属省
	 */
	String province;
	/**
	 * 电话信息
	 */
	String telephone;

	/**
	 * 邮政编码
	 */
	String postcode;

	/**
	 * 网址
	 */
	String website;

	/**
	 * 获取电话号码
	 * 
	 * @return
	 */
	public String getTelephone() {
		return telephone;
	}

	/**
	 * 设置电话号码
	 * 
	 * @param telephone
	 */
	public PoiDetail setTelephone(String telephone) {
		this.telephone = telephone;
		return this;
	}

	/**
	 * 获取省份
	 * 
	 * @return
	 */
	public String getProvince() {
		return province;
	}

	/**
	 * 设置省份
	 * 
	 * @param province
	 */
	public PoiDetail setProvince(String province) {
		this.province = province;
		return this;
	}

	/**
	 * 获取邮政编码
	 * 
	 * @return
	 */
	public String getPostcode() {
		return postcode;
	}

	/**
	 * 设置邮政编码
	 * 
	 * @param postcode
	 */
	public PoiDetail setPostcode(String postcode) {
		this.postcode = postcode;
		return this;
	}

	/**
	 * 设置距离
	 * 
	 * @param distance
	 */
	public PoiDetail setDistance(int distance) {
		super.setDistance(distance);
		return this;
	}

	/**
	 * 获取主页
	 * 
	 * @return
	 */
	public String getWebsite() {
		return website;
	}

	/**
	 * 设置主页
	 * 
	 * @param website
	 */
	public PoiDetail setWebsite(String website) {
		this.website = website;
		return this;
	}

	/**
	 * 设置纬度
	 * 
	 * @param lat
	 */
	public PoiDetail setLat(double lat) {
		super.setLat(lat);
		return this;
	}

	/**
	 * 设置经度
	 * 
	 * @param lng
	 */
	public PoiDetail setLng(double lng) {
		super.setLng(lng);
		return this;
	}

	/**
	 * 设置名字
	 * 
	 * @param name
	 */
	public PoiDetail setName(String name) {
		super.setName(name);
		return this;
	}

	/**
	 * 设置城市
	 * 
	 * @param city
	 */
	public PoiDetail setCity(String city) {
		super.setCity(city);
		return this;
	}

	/**
	 * 设置地理信息
	 * 
	 * @param geoinfo
	 */
	public PoiDetail setGeoinfo(String geoinfo) {
		super.setGeoinfo(geoinfo);
		return this;
	}
	
	/**
	 * 设置别名
	 * 
	 * @param alias
	 */
	public PoiDetail setAlias(String[] alias) {
		this.alias = alias;
		return this;
	}
	
	/**
	 * 设置来源类型
	 * @return
	 */
	public PoiDetail setSourceType(int source) {
		super.setSourceType(source);
		return this;
	}

	@Override
	protected JSONBuilder toJsonObj() {
		JSONBuilder json = super.toJsonObj();
		json.put("province", this.province);
		json.put("postcode", this.postcode);
		json.put("telephone", this.telephone);
		json.put("website", this.website);
		return json;
	}

	@Override
	public String toString() {
		JSONBuilder json = toJsonObj();
		return json.toString();
	}

	@Override
	protected void fromJsonObject(JSONBuilder json) {
		super.fromJsonObject(json);
		this.province = json.getVal("province", String.class);
		this.postcode = json.getVal("postcode", String.class);
		this.telephone = json.getVal("telephone", String.class);
		this.website = json.getVal("website", String.class);
	}

	public static PoiDetail fromString(String data) {
		PoiDetail p = new PoiDetail();
		JSONBuilder json = new JSONBuilder(data);
		p.fromJsonObject(json);
		return p;
	}
}
