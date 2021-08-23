package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyFloatView;
import com.txznet.resholder.R;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-04 14:59
 */
public class SmartHandyFloatView extends ISmartHandyFloatView {

    private static SmartHandyFloatView instance = new SmartHandyFloatView();
    public static SmartHandyFloatView getInstance() {
        return instance;
    }

    @Override
    public void init() {

    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.smart_handy_float_view, (ViewGroup) null);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();
        return viewAdapter;
    }
}
