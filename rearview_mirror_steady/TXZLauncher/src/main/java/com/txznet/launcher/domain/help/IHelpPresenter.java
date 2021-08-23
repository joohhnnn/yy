package com.txznet.launcher.domain.help;

import android.support.annotation.Size;

import com.txznet.launcher.component.BasePresenter;
import com.txznet.launcher.domain.help.bean.HelpCommand;

/**
 * Created by daviddai on 2018/8/27
 */
public interface IHelpPresenter extends BasePresenter{

    // 展示指定某一页
    void showSpecifiedPage(int specifiedPage);
    // 展示下一页
    void nextPage();
    // 展示上一页
    void prePage();
    // 是否是第一页
    boolean isFirstPage();
    // 是否是最后一页
    boolean isLastPage();
    // 当前是多少页
    int getCurrentPage();
    // 获取一共有多少页
    int getPageCount();
    // 展示指定的数据
    void showCommand(@Size(3) HelpCommand[] commands);
}
