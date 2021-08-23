package com.txznet.txz.ui.win.nav;

import org.json.JSONObject;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.choice.list.PoiWorkChoice;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.util.QuickClickUtil;

import android.os.SystemClock;
import android.text.TextUtils;

/**
 * 统一管理poi列表界面，选择城市和修改关键字的对话框
 * 1. 支持从皮肤包中加载界面到对话框中
 * 2. 默认加载
 * {@link com.txznet.comm.ui.viewfactory.view.defaults.DefaultSelectCityView }
 * {@link com.txznet.comm.ui.viewfactory.view.defaults.DefaultSearchEditView }
 * 3. 目前没有考虑使用
 */
public class SearchEditManager {

    public final static int LOCATION_END = 4;
    public final static int LOCATION_JINGYOU = 3;
    public final static int LOCATION_COMPANY = 2;
    public final static int LOCATION_HOME = 1;
    public final static int LOCATION_NONE = 0;
    private int mIntWhere;
    private String mKey;
    private String mCity;

    private static String editAction;
    private static PoiWorkChoice mParentChoice;

    public static final int TYPE_SEARCH_EDIT = 0;
    public static final int TYPE_SELECT_CITY = 1;

    private static SearchEditManager instance;

    public static SearchEditManager getInstance() {
        if (instance == null) {
            synchronized (SearchEditManager.class) {
                if (instance == null) {
                    instance = new SearchEditManager();
                }
            }
        }
        return instance;
    }

    public static void attachParent(PoiWorkChoice choice) {
        mParentChoice = choice;
    }

    private SearchEditDialogBase mDialog;

    public boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    public void naviDefault(int type, String keyWord, String city) {
        if (isShowing()) {
            return;
        }
        editAction = PoiAction.ACTION_NAVI;
        mIntWhere = SearchEditManager.LOCATION_NONE;
        mKey = keyWord;
        mCity = city;
        if (type == TYPE_SEARCH_EDIT) {
            mDialog = SearchEditDialog.naviDefault(keyWord, city);
        } else {
            mDialog = SelectCityDialog.naviDefault(keyWord, city);
        }
    }

    public void naviHome(int type, String keyWord, String city) {
        if (isShowing()) {
            return;
        }
        editAction = PoiAction.ACTION_HOME;
        mIntWhere = SearchEditManager.LOCATION_HOME;
        mKey = keyWord;
        mCity = city;
        if (type == TYPE_SEARCH_EDIT) {
            mDialog = SearchEditDialog.naviHome(keyWord, city);
        } else {
            mDialog = SelectCityDialog.naviHome(keyWord, city);
        }
    }

    public void naviCompany(int type, String keyWord, String city) {
        if (isShowing() ) {
            return;
        }
        editAction = PoiAction.ACTION_COMPANY;
        mIntWhere = SearchEditManager.LOCATION_COMPANY;
        mKey = keyWord;
        mCity = city;
        if (type == TYPE_SEARCH_EDIT) {
            mDialog = SearchEditDialog.naviCompany(keyWord, city);
        } else {
            mDialog = SelectCityDialog.naviCompany(keyWord, city);
        }
    }

    public void naviJingYou(int type, String keyWord, String city) {
        if (isShowing()) {
            return;
        }
        editAction = PoiAction.ACTION_JINGYOU;
        mIntWhere = SearchEditManager.LOCATION_JINGYOU;
        mKey = keyWord;
        mCity = city;
        if (type == TYPE_SEARCH_EDIT) {
            mDialog = SearchEditDialog.naviJingYou(keyWord, city);
        } else {
            mDialog = SelectCityDialog.naviJingYou(keyWord, city);
        }

    }

    public void naviEnd(int type, String keyWord, String city) {
        if (isShowing()) {
            return;
        }
        editAction = PoiAction.ACTION_NAVI_END;
        mIntWhere = SearchEditManager.LOCATION_END;
        mKey = keyWord;
        mCity = city;
        if (type == TYPE_SEARCH_EDIT) {
            mDialog = SearchEditDialog.naviEnd(keyWord, city);
        } else {
            mDialog = SelectCityDialog.naviEnd(keyWord, city);
        }
    }

    private QuickClickUtil mQuickClickUtil = new QuickClickUtil();

    public byte[] invokeCommand(final String packageName, String command,
                                final byte[] data) {
        //检测是否是点击太快
        if (mQuickClickUtil.check()) {
            return null;
        }
        if (TextUtils.equals("txz.record.ui.event.search.edit.result", command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            doSearch(jsonBuilder.getVal("key", String.class, ""));
        } else if (TextUtils.equals("txz.record.ui.event.search.edit.cancel", command)) {
            if (mDialog != null) {
                mDialog.onBackPressed();
            }
        } else if (TextUtils.equals("txz.record.ui.event.select.city.cancel", command)) {
            setNeedCloseDialog(true);
            dismiss();
        } else if (TextUtils.equals("txz.record.ui.event.select.city.result", command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            doCitySelect(jsonBuilder.getVal("city", String.class, ""));
        }
        return null;
    }

    private void doSearch(String strDest) {
        if (!isShowing()) {
            return;
        }

        if (strDest.equals(""))
            return;
        String city = null;
        if (TextUtils.isEmpty(mCity)) {
            city = new String();

            LocationInfo info = LocationManager.getInstance().getLastLocation();
            if (info == null) {
                city = "深圳市";
            } else {
                if (info.msgGeoInfo != null) {
                    city = info.msgGeoInfo.strCity;
                } else {
                    city = "深圳市";
                }
            }
        } else {
            city = mCity;
        }


        JSONObject json = new JSONObject();
        try {
            json.put("keywords", strDest);
            json.put("city", city);
            json.put("where", mIntWhere);
        } catch (Exception e) {
            LogUtil.loge(e.toString());
        }

        setNeedCloseDialog(true);

        NavManager.getInstance().mSearchByEdit = true;

        // 将搜索前的编辑上报
        if (ChoiceManager.getInstance().isSelecting()) {
            mParentChoice.putReport(PoiWorkChoice.KEY_EDIT_TYPE, "keywords");
            mParentChoice.putReport(PoiWorkChoice.KEY_POI_ACTION, editAction);
            mParentChoice.doReportSelectFinish(false, PoiWorkChoice.SELECT_TYPE_UNKNOW, "");
//            ChoiceManager.getInstance().clearIsSelecting();
        }

        dismiss();
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.loading",
                null, null);
        NavManager.getInstance().invokeTXZNav(null, "inner.poiSearch", json.toString().getBytes());
        // 关闭页面
    }


    private void doCitySelect(String cityStr) {
        if (cityStr.equals(mCity) || TextUtils.isEmpty(cityStr)) {
            setNeedCloseDialog(true);
            dismiss();
            return;
        }
        JSONObject json = new JSONObject();
        try {
            json.put("keywords", mKey);
            json.put("city", cityStr);
            json.put("where", mIntWhere);
        } catch (Exception e) {
            LogUtil.loge(e.toString());
        }

        NavManager.getInstance().mSearchByEdit = true;
        NavManager.getInstance().mSearchBySelect = true;

        // 将搜索前的编辑上报
        if (ChoiceManager.getInstance().isSelecting()) {
            mParentChoice.putReport(PoiWorkChoice.KEY_EDIT_TYPE, "city");
            mParentChoice.putReport(PoiWorkChoice.KEY_EDIT_KWS, mCity);
            mParentChoice.putReport(PoiWorkChoice.KEY_POI_ACTION, editAction);
            mParentChoice.doReportSelectFinish(false, PoiWorkChoice.SELECT_TYPE_UNKNOW, "");

            ChoiceManager.getInstance().clearIsSelecting();
        }
        dismiss();
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.loading",
                null, null);
        NavManager.getInstance().invokeTXZNav(null, "inner.poiSearch", json.toString().getBytes());
    }

    public void setNeedCloseDialog(boolean isClose) {
        if (mDialog != null) {
            mDialog.setNeedCloseDialog(isClose);
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void updateDialogType(Integer wmType) {
        SearchEditDialog.mWindowType = wmType;
    }

    public void updateDialogWinFlag(Integer wmFlag){
        SearchEditDialog.mWindowFlag = wmFlag;
    }

    public void setIsFullSreenDialog(Boolean b) {
        SearchEditDialog.mFullScreen = b;
    }
}