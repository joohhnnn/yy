package com.txznet.txz.component.choice.page;

import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;

import java.util.ArrayList;

public abstract class ResTrainTicketPage extends ResourcePage<QiWuTrainTicketData, QiWuTrainTicketData.TrainTicketBean> {
    public ResTrainTicketPage(QiWuTrainTicketData resources, int totalSize) {
        super(resources, totalSize);
    }

    @Override
    protected void clearCurrRes(QiWuTrainTicketData currRes) {
        if (currRes == null) {
            return;
        }
        currRes.arrivalCity = "";
        currRes.date = "";
        currRes.departureCity = "";
        if (currRes.mTrainTicketBeans == null || currRes.mTrainTicketBeans.size() == 0) {
            return;
        }
        currRes.mTrainTicketBeans.clear();
    }

    @Override
    protected QiWuTrainTicketData notifyPage(int sIdx, int len, QiWuTrainTicketData sourceRes) {
        QiWuTrainTicketData data = null;
        if (sourceRes == null) {
            return null;
        }
        data = new QiWuTrainTicketData();
        //data.mTrainTicketBeans = sourceRes.mTrainTicketBeans;
        data.arrivalCity = sourceRes.arrivalCity;
        data.date = sourceRes.date;
        data.departureCity = sourceRes.departureCity;
        if(sourceRes.mTrainTicketBeans == null){
            return null;
        }
        data.mTrainTicketBeans = new ArrayList<QiWuTrainTicketData.TrainTicketBean>(len);
        for (int i = sIdx; i < sIdx + len; i++) {
            if (i >= 0 && i < sourceRes.mTrainTicketBeans.size()) {
                data.mTrainTicketBeans.add(sourceRes.mTrainTicketBeans.get(i));
            }
        }
        return data;
    }

    @Override
    protected int getCurrResSize(QiWuTrainTicketData currRes) {
        if (currRes == null || currRes.mTrainTicketBeans == null) {
            return 0;
        }
        return currRes.mTrainTicketBeans.size();
    }

    @Override
    public QiWuTrainTicketData.TrainTicketBean getItemFromCurrPage(int idx) {
        QiWuTrainTicketData currRes = getResource();
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mTrainTicketBeans.get(idx);
        }
        return null;
    }

    @Override
    public QiWuTrainTicketData.TrainTicketBean getItemFromSource(int idx) {
        QiWuTrainTicketData currRes = mSourceRes;
        int size = getCurrResSize(currRes);
        if (idx >= 0 && idx < size) {
            return currRes.mTrainTicketBeans.get(idx);
        }
        return null;
    }
}
