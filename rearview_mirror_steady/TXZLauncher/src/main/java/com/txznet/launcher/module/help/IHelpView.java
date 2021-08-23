package com.txznet.launcher.module.help;

import android.support.annotation.Size;

import com.txznet.launcher.domain.help.bean.HelpCommand;

/**
 * Created by daviddai on 2018/8/27
 * 帮助功能的mvp模式的v
 */
public interface IHelpView {

    /**
     * 展示内容
     * @param commands 待展示的数据，这里指定三个
     * @param currentPage 当前页数
     * @param firstPage 第一页
     * @param lastPage 最后一页
     */
    void showPage(@Size(3)HelpCommand[] commands,int currentPage,int firstPage,int lastPage);
}
