package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.MD5Util;

import java.util.List;

/**
 * 说明：待办事项卡片
 *
 * @author xiaolin
 * create at 2020-11-07 10:22
 */
public class HomeRemindHolder {

    private static HomeRemindHolder instance = new HomeRemindHolder();

    public static HomeRemindHolder getInstance() {
        return instance;
    }

    private View mRootView;
    private ViewGroup wrapCreate;
    private ViewGroup itemContainer;
    private ImageButton imgBtnMore;

    private String lastDataMd5 = "";

    public View getView() {
        if (mRootView == null) {
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_reminder, (ViewGroup) null);
            init();
        }
        return mRootView;
    }

    private void init() {
        wrapCreate = mRootView.findViewById(R.id.wrapCreate);
        itemContainer = mRootView.findViewById(R.id.itemContainer);
        imgBtnMore = mRootView.findViewById(R.id.imgBtnMore);

        itemContainer.setVisibility(View.GONE);
        wrapCreate.setVisibility(View.VISIBLE);
        wrapCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建事项
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_REMINDER_CREATE,
                        0, 0);
            }
        });
        imgBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 更多待办事项
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_REMINDER_MORE, 0, 0);
            }
        });
    }

    public void update(SmartHandyHomeViewData.ReminderData data) {
        try {
            String md5 = MD5Util.generateMD5(JSONObject.toJSONString(data));
            if (md5.equals(lastDataMd5)) {
                return;
            }
            lastDataMd5 = md5;
        } catch (Exception e) {
            e.printStackTrace();
            lastDataMd5 = null;
        }

        List<SmartHandyHomeViewData.ReminderData.ReminderItemBean> list = data.reminderItemBeans;
        int count = Math.min(3, list.size());// 最多显示三项
        if (count > 0) {
            wrapCreate.setVisibility(View.GONE);
            itemContainer.removeAllViews();
            itemContainer.setVisibility(View.VISIBLE);

            Context context = UIResLoader.getInstance().getModifyContext();
            for (int i = 0; i < count; i++) {
                final SmartHandyHomeViewData.ReminderData.ReminderItemBean bean = list.get(i);
                View itemView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_reminder_item, (ViewGroup) null);
                TextView tvTitle = itemView.findViewById(R.id.tvTitle);
                TextView tvTime = itemView.findViewById(R.id.tvTime);
                TextView tvClose = itemView.findViewById(R.id.tvClose);

                tvTitle.setText(bean.content);
                tvTime.setText(bean.time);
                tvClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除待办事项
                        RecordWin2Manager.getInstance().operateView(
                                TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                                TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_REMINDER_DELETE,
                                0, 0, 0, bean.id);
                    }
                });
                itemContainer.addView(itemView);
            }
        } else {
            itemContainer.removeAllViews();
            itemContainer.setVisibility(View.GONE);
            wrapCreate.setVisibility(View.VISIBLE);
        }
    }

}
