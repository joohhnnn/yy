package com.txznet.txz.component.choice.page;

import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;

import java.util.ArrayList;

public abstract class ResFlightTicketPage extends ResourcePage<QiWuFlightTicketData, QiWuFlightTicketData.FlightTicketBean> {

    public ResFlightTicketPage(QiWuFlightTicketData resources, int totalSize) {
        super(resources, totalSize);
    }

    @Override
    protected void clearCurrRes(QiWuFlightTicketData currRes) {
        if (currRes == null) {
            return;
        }
        currRes.arrivalCity = "";
        currRes.date = "";
        currRes.departureCity = "";
        if (currRes.mFlightTicketBeans == null || currRes.mFlightTicketBeans.size() == 0) {
            return;
        }
        currRes.mFlightTicketBeans.clear();
    }

    @Override
    protected QiWuFlightTicketData notifyPage(int sIdx, int len, QiWuFlightTicketData sourceRes) {
        QiWuFlightTicketData data = null;
        if (sourceRes == null) {
            return null;
        }
        data = new QiWuFlightTicketData();
        //data.mTrainTicketBeans = sourceRes.mTrainTicketBeans;
        data.arrivalCity = sourceRes.arrivalCity;
        data.date = sourceRes.date;
        data.departureCity = sourceRes.departureCity;
        if(sourceRes.mFlightTicketBeans == null){
            return null;
        }
        data.mFlightTicketBeans = new ArrayList<QiWuFlightTicketData.FlightTicketBean>(len);
        for (int i = sIdx; i < sIdx + len; i++) {
            if (i >= 0 && i < sourceRes.mFlightTicketBeans.size()) {
                data.mFlightTicketBeans.add(sourceRes.mFlightTicketBeans.get(i));
            }
        }
        return data;
    }

    @Override
    protected int getCurrResSize(QiWuFlightTicketData currRes) {
        if (currRes == null || currRes.mFlightTicketBeans == null) {
            return 0;
        }
        return currRes.mFlightTicketBeans.size();
    }

    @Override
    public QiWuFlightTicketData.FlightTicketBean getItemFromCurrPage(int idx) {
        QiWuFlightTicketData currRes = getResource();
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mFlightTicketBeans.get(idx);
        }
        return null;
    }

    @Override
    public QiWuFlightTicketData.FlightTicketBean getItemFromSource(int idx) {
        QiWuFlightTicketData currRes = mSourceRes;
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mFlightTicketBeans.get(idx);
        }
        return null;
    }
}
