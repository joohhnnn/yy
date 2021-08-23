package com.txznet.txz.ui.win.login;

import com.txznet.txz.ui.win.BaseContract;

/**
 * Created by TXZ-METEORLUO on 2017/5/18.
 */
public interface SimContract {
    interface View extends BaseContract.View {

        void showNoSim();

        void showSimDone();

        void showNetChecking();

        void showNetWell();

        void showNetError();

    }

    interface Presenter extends BaseContract.Presenter<View> {

    }
}
