package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IMoviePhoneNumQRView extends MsgViewBase {

    @Override
    public void init() {
        mViewType = ViewData.TYPE_FULL_MOVIE_PHONE_NUM_QRCODE;
    }

}
