package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.widget.WordPoolLayout;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：帮助卡片
 *
 * @author xiaolin
 * create at 2020-11-07 10:18
 */
public class HomeHelpHolder {

    private static HomeHelpHolder instance = new HomeHelpHolder();
    public static HomeHelpHolder getInstance(){
        return instance;
    }

    private View mRootView;
    private WordPoolLayout wordPoolLayout;

    public View getView(){
        if(mRootView == null){
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_help, (ViewGroup)null);
            init();
        }
        return mRootView;
    }

    private void init(){
        wordPoolLayout = mRootView.findViewById(R.id.wordPoolLayout);
        ImageButton imgBtnMore = mRootView.findViewById(R.id.imgBtnMore);
        imgBtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_HELP_MORE, 0, 0);
            }
        });
    }

    public void update(SmartHandyHomeViewData.HelpData data){
        if(data == null){
            return;
        }

        wordPoolLayout.setWords(data.words.toArray(new String[]{}));
    }
}
