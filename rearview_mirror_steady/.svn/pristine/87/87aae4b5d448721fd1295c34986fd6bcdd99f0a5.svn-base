package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.LouHolder;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ConstellationMatchingData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IConstellationMatchingView;
import com.txznet.resholder.R;

import java.util.HashMap;

/**
 * 星座匹配
 * <p>
 * 2020-08-11 10:36
 *
 * @author xiaolin
 */
public class ConstellationMatchingView extends IConstellationMatchingView {

    private static ConstellationMatchingView sInstance = new ConstellationMatchingView();


    private ConstellationMatchingView() {
    }

    public static ConstellationMatchingView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ConstellationMatchingData constellationMatchingData = (ConstellationMatchingData) data;
        WinLayout.getInstance().vTips = constellationMatchingData.vTips;

        View view = createViewNone(constellationMatchingData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = BindDeviceView.getInstance();
        return adapter;

    }

    private View createViewNone(ConstellationMatchingData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.constellation_matching_view, (ViewGroup)null);
        LouHolder holder = LouHolder.createInstance(view);

        holder.putText(R.id.tvName1, data.name);
        holder.putImg(R.id.ivName1, ConstellationFortuneView.getConstellationDrawableRes(data.name));
        holder.putText(R.id.tvName2, data.matchName);
        holder.putImg(R.id.ivName2, ConstellationFortuneView.getConstellationDrawableRes(data.matchName));
        holder.putText(R.id.tvLevel, data.level+"%");
        holder.putText(R.id.tvDesc, data.desc);

        return view;
    }


    @Override
    public void init() {
        super.init();
    }


    public void onUpdateParams(int styleIndex) {

    }

}
