package com.txznet.music.net;

import com.txz.ui.audio.UiAudio;
import com.txznet.music.baseModule.bean.Error;

/**
 * Created by telenewbie on 2017/10/26.
 */

public class AdapterRequestCallback implements RequestRawCallBack {

    RequestCallBack mCallBack;

    public AdapterRequestCallback(RequestCallBack callBack) {
        mCallBack = callBack;
    }


    @Override
    public void onResponse(UiAudio.Resp_DataInterface respDataInterface) {
        mCallBack.onResponse(respDataInterface);
        adapterDoing(new String(respDataInterface.strData));
    }

    @Override
    public void onError(String cmd, Error error) {
        mCallBack.onError(cmd, error);
    }

    public  void adapterDoing(String adapterThing){

    }

}
