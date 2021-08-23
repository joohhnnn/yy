package com.txznet.txz.ui.win.nav;

import android.view.WindowManager;

import com.txznet.comm.ui.dialog2.WinDialog;

/**
 * Created by ASUS User on 2018/7/20.
 */

public abstract class SearchEditDialogBase extends WinDialog {
    public static int mWindowType = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3;
    public static boolean mFullScreen = true;
    public static Integer mWindowFlag;

    public SearchEditDialogBase(DialogBuildData data) {
        super(data);
    }

    public abstract void dismiss();

    public abstract void setNeedCloseDialog(boolean needCloseDialog);
}
