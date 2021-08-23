package com.txznet.txz.util.focus_supporter.focusfinder;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.log.FocusLog;

/**
 * “组合”的FocusFinder
 * 由LinearFocusFinder和RelativeFocusFinder组合而来，操作方控的上下左右时采用RelativeFocusFinder
 * 进行焦点的查找，操作滚轮进行滚动时采用LinearFocusFinder的逻辑进行线性方向上的焦点查找。
 *
 * Created by J on 2017/5/4.
 */

public class ComplexFocusFinder implements IFocusFinder {
    private IFocusFinder mLinearFocusFinder;
    private IFocusFinder mRelativeFocusFinder;
    @Override
    public Object findFocus(Object currentFocus, Object[] focusList, int op) {
        if (FocusSupporter.NAV_BTN_LEFT == op || FocusSupporter.NAV_BTN_RIGHT == op || FocusSupporter.NAV_BTN_UP == op || FocusSupporter.NAV_BTN_DOWN == op) {
            return findRelativeFocus(currentFocus, focusList, op);
        }else if (FocusSupporter.NAV_BTN_NEXT == op || FocusSupporter.NAV_BTN_PREV == op) {
            return findLinearFocus(currentFocus, focusList, op);
        }

        FocusLog.e("ComplexFocusFinder::findFocus: op not supported: " + op);

        return null;
    }

    private Object findLinearFocus(Object currentFocus, Object[] focusList, int op) {
        if (null == mLinearFocusFinder) {
            mLinearFocusFinder = new LinearFocusFinder();
        }

        return mLinearFocusFinder.findFocus(currentFocus, focusList, op);
    }

    private Object findRelativeFocus(Object currentFocus, Object[] focusList, int op) {
        if (null == mRelativeFocusFinder) {
            mRelativeFocusFinder = new RelativeFocusFinder();
        }

        return mRelativeFocusFinder.findFocus(currentFocus, focusList, op);
    }
}
