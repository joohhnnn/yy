package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.ui.viewfactory.data.SmartHandySetTravelModeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandySetTravelModeView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：设置出行模式
 *
 * @author xiaolin
 * create at 2020-11-10 11:24
 */
public class SmartHandySetTravelModeView extends ISmartHandySetTravelModeView {

    private static SmartHandySetTravelModeView instance = new SmartHandySetTravelModeView();

    public static SmartHandySetTravelModeView getInstance() {
        return instance;
    }

    private View mRootView;
    private ViewGroup mItemContainer;
    private TextView tvTitle;
    private ImageView ivAddressIcon;// 地址类型图标
    private TextView tvAddress;

    @Override
    public void init() {
        super.init();
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_set_travel_mode, (ViewGroup) null);
        mItemContainer = mRootView.findViewById(R.id.itemContainer);
        tvTitle = mRootView.findViewById(R.id.tvTitle);
        ivAddressIcon = mRootView.findViewById(R.id.ivAddressIcon);
        tvAddress = mRootView.findViewById(R.id.tvAddress);

        // 返回
        mRootView.findViewById(R.id.imgBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_BACK_TO_HOME,
                        0, 0, 0);
            }
        });
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        updateView(viewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = viewData.getType();
        viewAdapter.view = mRootView;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();
        return viewAdapter;
    }

    @Override
    public Object updateView(ViewData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        final SmartHandySetTravelModeViewData viewData = (SmartHandySetTravelModeViewData) data;

        // 点击设置地址
        mRootView.findViewById(R.id.wrapAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NAV_SET_ADDRESS,
                        0, 0, 0, viewData.mode);
            }
        });

        tvTitle.setText(viewData.modeName);
        tvAddress.setText(viewData.hitAddress);
        ivAddressIcon.setImageResource(getNavIcon(viewData.mode));

        mItemContainer.removeAllViews();
        for (int i = 0; i < viewData.beans.size(); i++) {
            final SmartHandySetTravelModeViewData.Bean bean = viewData.beans.get(i);
            View itemView = LayoutInflater.from(context).inflate(R.layout.smart_handy_set_travel_mode_item, (ViewGroup) null);
            ViewGroup wrap = itemView.findViewById(R.id.wrap);
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            final CheckBox checkBox = itemView.findViewById(R.id.checkbox);

            tvTitle.setText(bean.title);
            checkBox.setChecked(bean.check);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    JSONBuilder jb = new JSONBuilder();
                    jb.put("mode", viewData.mode);
                    jb.put("cmd", bean.cmd);
                    jb.put("enable", isChecked);
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NAV_TRAVEL_MODE_UPDATE_DIRECT,
                            0, 0, 0, jb.toString());
                }
            });

            wrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkBox.setChecked(!checkBox.isChecked());

                    JSONBuilder jb = new JSONBuilder();
                    jb.put("mode", viewData.mode);
                    jb.put("cmd", bean.cmd);
                    jb.put("enable", checkBox.isChecked());
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NAV_TRAVEL_MODE_UPDATE_DIRECT,
                            0, 0, 0, jb.toString());
                }
            });

            mItemContainer.addView(itemView);
        }
        return null;
    }

    /**
     * 地址图标
     *
     * @param mode {@link SmartHandySetTravelModeViewData}
     * @return
     */
    private int getNavIcon(String mode) {
        switch (mode) {
            case SmartHandySetTravelModeViewData.MODE_HOME:
                return R.drawable.smart_handy_icon_home;
            case SmartHandySetTravelModeViewData.MODE_COMPANY:
                return R.drawable.smart_handy_icon_company;
        }

        return R.drawable.smart_handy_icon_home;
    }
}
