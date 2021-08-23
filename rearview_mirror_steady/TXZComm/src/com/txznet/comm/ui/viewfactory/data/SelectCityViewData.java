package com.txznet.comm.ui.viewfactory.data;

import java.util.List;

public class SelectCityViewData extends ViewData {
	public List<String> curCityList = null;
	public List<String> tarCityList = null;
	public List<String> nomCityList = null;
	public List<String> perCityList = null;
	public SelectCityViewData() {
		super(TYPE_SELECT_CITY_VIEW);
	}

}
