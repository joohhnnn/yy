package com.txznet.txz.util.focus_supporter.focusfinder;

import com.txznet.txz.util.focus_supporter.FocusSupporter;

/**
 * “线性” 的FocusFinder
 * 提供单一维度的焦点切换， 仅根据列表位置， 返回线性列表中当前方向上下一个位置上的对象
 * Created by J on 2017/4/25.
 */

public class LinearFocusFinder extends AbsFocusFinder {
    @Override
    public Object findNextFocus(Object currentFocus, Object[] focusList, int op) {
        int index = getFocusIndex(currentFocus, focusList);

        if (FocusSupporter.NAV_BTN_NEXT == op || FocusSupporter.NAV_BTN_RIGHT == op || FocusSupporter.NAV_BTN_DOWN == op) {
            index++;
            if (index >= focusList.length) {
                index = focusList.length - 1;
            }
        } else if (FocusSupporter.NAV_BTN_PREV == op || FocusSupporter.NAV_BTN_UP == op || FocusSupporter.NAV_BTN_LEFT == op) {
            index--;
            if (index < 0) {
                index = 0;
            }
        }

        return focusList[index];
    }

    private int getFocusIndex(Object target, Object[] focusList) {
        for (int i = 0, len = focusList.length; i < len; i++) {
            if (target == focusList[i]) {
                return i;
            }
        }

        return -1;
    }
}
