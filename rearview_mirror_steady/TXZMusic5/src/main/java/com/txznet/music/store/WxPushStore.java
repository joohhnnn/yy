package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.QrCodeInfo;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author telen
 * @date 2018/12/22,11:40
 */
public class WxPushStore extends Store {
    List<PushItem> mPushItems = new ArrayList<>();

    MutableLiveData<List<PushItem>> mPushItemLiveData = new MutableLiveData<>();

    MutableLiveData<QrCodeInfo> mQrCodeInfo = new MutableLiveData<>();

    LiveData<List<PushItem>> mPushItemLiveDataSorted = Transformations.map(mPushItemLiveData, input -> {
        if (input != null) {
            Collections.sort(input, (o1, o2) -> {
                if (o1.timestamp == o2.timestamp) {
                    return 0;
                }
                return o1.timestamp > o2.timestamp ? -1 : 1;
            });
        }
        return input;
    });


    enum WXPushStatus {
        WX_ERROR,
        WX_EMPTY,
    }


    public WxPushStore() {
        String qrcodeInfo = SharedPreferencesUtils.getQrCodeInfoCache();
        if (qrcodeInfo == null) {
            mQrCodeInfo.setValue(null);
        } else {
            mQrCodeInfo.setValue(JsonHelper.fromJson(qrcodeInfo, QrCodeInfo.class));
        }
    }


    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_WXPUSH_EVENT_GET,
                ActionType.ACTION_WXPUSH_EVENT_GET_QRCODE,
                ActionType.ACTION_WXPUSH_EVENT_DELETE,
                ActionType.ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO,
                ActionType.ACTION_WXPUSH_EVENT_SAVE
        };
    }

    @Override
    protected void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_WXPUSH_EVENT_UPDATE_QRCODE_INFO:
                updateQrcodeInfo(action);
                break;
            default:
                break;

        }
    }

    private void updateQrcodeInfo(RxAction action) {
        //更新qrcode的信息
        QrCodeInfo qrCodeInfo = (QrCodeInfo) action.data.get(Constant.WxPushConstant.KEY_QRCODE_INFO);
        SharedPreferencesUtils.setQrCodeInfoCache(JsonHelper.toJson(qrCodeInfo));
        mQrCodeInfo.setValue(qrCodeInfo);
    }


    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_WXPUSH_EVENT_GET:
                mPushItems.clear();
                mPushItems.addAll((List<PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS));
                mPushItemLiveData.setValue(mPushItems);
                break;
            case ActionType.ACTION_WXPUSH_EVENT_SAVE:
                mPushItems.addAll(0, (Collection<? extends PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS));
                //去重
                Utils.deleteSameAudiosFromSource(mPushItems);
                mPushItemLiveData.setValue(mPushItems);
                break;

            case ActionType.ACTION_WXPUSH_EVENT_DELETE:
                mPushItems.removeAll((Collection<? extends PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS));
                mPushItemLiveData.setValue(mPushItems);
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {

    }

    public LiveData<List<PushItem>> getPushItem() {
        return mPushItemLiveDataSorted;
    }

    public LiveData<QrCodeInfo> getQrCodeInfo() {
        return mQrCodeInfo;
    }
}
