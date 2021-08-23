package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.ViewBase;

/**
 * Created by ASUS User on 2018/7/19.
 */

public abstract class ISearchEditView extends ViewBase {
    public abstract void onStart(String keyWord);
    public abstract void onShow();
    public abstract void onDismiss();
}
