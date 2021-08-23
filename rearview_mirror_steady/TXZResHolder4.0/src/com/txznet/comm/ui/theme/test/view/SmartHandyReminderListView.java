package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.ui.viewfactory.data.SmartHandyReminderListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyReminderListView;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：智能捷径，提醒事项列表
 *
 * @author xiaolin
 * create at 2020-11-06 10:30
 */
public class SmartHandyReminderListView extends ISmartHandyReminderListView {

    private static SmartHandyReminderListView instance;
    public static SmartHandyReminderListView getInstance(){
        if(instance == null){
            instance = new SmartHandyReminderListView();
            instance.init();
        }
        return instance;
    }

    private View mRootView;
    private ScrollView mScrollView;
    private ViewGroup mItemContainer;

    @Override
    public void init(){
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_reminder_container, (ViewGroup)null);
        mScrollView = mRootView.findViewById(R.id.scrollView);
        mItemContainer = mRootView.findViewById(R.id.itemContainer);

        mRootView.findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
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
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;

        return viewAdapter;
    }

    @Override
    public Object updateView(ViewData data) {
        if(data == null){
            return null;
        }

        SmartHandyReminderListViewData viewData = (SmartHandyReminderListViewData) data;

        mScrollView.scrollTo(0, 0);
        mItemContainer.removeAllViews();
        Context context = UIResLoader.getInstance().getModifyContext();
        for(final SmartHandyHomeViewData.ReminderData.ReminderItemBean row: viewData.reminderItemBeans) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.smart_handy_reminder_item, (ViewGroup) null);
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            TextView tvTime = itemView.findViewById(R.id.tvTime);
            TextView tvClose = itemView.findViewById(R.id.tvClose);

            tvTitle.setText(row.content);
            tvTime.setText(row.time);
            tvClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 删除待办事项
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_REMINDER_DELETE,
                            0, 0, 0, row.id);
                }
            });
            mItemContainer.addView(itemView);
        }
        return null;
    }

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void updateItemSelect(int i) {

    }
}
